/* A Parameter is an Attribute that is also a container for a token.

 Copyright (c) 1998 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 
                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)

*/

package pt.data.expr;

import pt.kernel.*;
import pt.kernel.util.*;
import pt.data.Token;
import pt.data.TokenPublisher;
import pt.data.TypeCPO;
import java.util.*;
import pt.graph.CPO;

//////////////////////////////////////////////////////////////////////////
//// Parameter
/**
 * A Parameter is an Attribute that is also a container for a token. 
 * The type of a Parameter is set by the first non-null Token placed in it. 
 * The type can be changed later via a method call. However the 
 * new type for the parameter must be able to contain the previous token.
 * A parameter can be given a Token or a String as its value. 
 * If a String is given, it uses PtParser to obtain the Token
 * resulting from parsing and evaluating the expression. 
 * If another Object (e.g. Parameter) wants to Observe this Parameter, it must 
 * register its interest with the TokenPublisher associated with the 
 * contained Token.
 * At any stage a new Token or String can be given to the Parameter. The type
 * of the new/resulting Token is checked to see if it can be converted to 
 * the type of the Parameter in a lossless manner.
 * If you want to create a parameter from a string, it is necessary 
 * to create the parameter with the appropriate container and name, 
 * then call setTokenFromExpr() to set its value.
 * 
 * FIXME:  synchronization issues
 * 
 * @author Neil Smyth
 * @version $Id$
 * @see pt.kernel.util.Attribute
 * @see pt.data.expr.PtParser 
 * @see pt.data.Token 
*/
public class Parameter extends pt.kernel.util.Attribute implements Observer {

    /** Construct a parameter in the default workspace with an empty string
     *  as its name.
     *  The parameter is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public Parameter() {
        super();
    }

    /** Construct a parameter in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public Parameter(Workspace workspace) {
        super(workspace);
    }

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public Parameter(NamedObj container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty 
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
     public Parameter(NamedObj container, String name, pt.data.Token token)
            throws IllegalActionException, NameDuplicationException {
         super(container, name);
         if (token != null) {
             try {
                 _origToken = (pt.data.Token)token.clone();
                 _noTokenYet = false;
                 _paramType = token.getClass();
             } catch (CloneNotSupportedException c) {
                 _origToken = token;
             }
         }
         _token = token;
     }
     
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Clone the parameter. 
     *  The state of the cloned parameter will be identical to the original 
     *  parameter, but without the Observer/Observable dependencies set up. 
     *  To achieve this update() should be called after cloning the 
     *  parameter.  Update() should only be called after all 
     *  the parameters on which this parameter depends have been created. 
     *  @param The workspace in which to place the cloned Parameter.
     *  @exception CloneNotSupportedException If the parameter
     *   cannot be cloned.
     *  @see java.lang.Object#clone()
     *  @return An identical Parameter.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
      Parameter result = (Parameter)super.clone(ws);
      if (_token != null) {
          result._token = (pt.data.Token)_token.clone();
      }
      if (_origToken != null) {
          result._origToken = (pt.data.Token)_origToken.clone();
      }
      if (_paramType != null) {
          try {
              result._paramType = (Class)_paramType.newInstance().getClass();
          } catch (Exception ex) {
              // do nothing as must be able to get a new instance
          }
      }
      result._currentValue = _currentValue;
      result._dependencyLoop = false;
      result._initialValue = _initialValue;
      result._lastVersion = 0;
      result._noTokenYet = _noTokenYet;      
      result._parser = null;
      result._parseTreeRoot = null;
      result._scope = null;
      return result;
    }

    /** Return a description of the object.
     *  @param verbosity The level of verbosity.
     *  @return A String describing the Parameter.
     *  FIXME: needs to be finished.
     */
    public String description(int verbosity) {
        return toString();
    }
    
    /** Obtain a NamedList of the parameters that the value of this 
     *  Parameter can depend on. The scope is limited to the parameters in the 
     *  same NamedObj and those one level up in the hierarchy.
     *  It catches any exceptions thrown by NamedList as 1) the parameter must 
     *  have a container with a NamedList of Parameters, and 2) if there is
     *  a clash in the names of the two scopeing levels, the parameter from 
     *  the top level is considered not to be visible in the scope of this 
     *  Parameter
     *  @return The parameters on which this parameter can depend.
     */
    public NamedList getScope() {
        if ( (_scope != null) && (_lastVersion == workspace().getVersion()) ) {
            return _scope;
        } else {
            _scope = new NamedList();       
            NamedObj paramContainer = (NamedObj)getContainer();
            if (paramContainer !=null) {
                NamedObj paramContainerContainer =
                    ((NamedObj)paramContainer.getContainer());
                Enumeration level = paramContainer.getAttributes();       
                Parameter p;
                while (level.hasMoreElements() ) {
                    // now  copy the namedlist, omitting the current Parameter
                    if ( (p=(Parameter)level.nextElement()) != this ) {
                        try {
                            _scope.append(p);
                        } catch (Exception ex) {
                            // since we're basically copying a namedlist,  
                            // these exceptions cannot occur
                        }
                    }
                }
                if (paramContainerContainer != null) {
                    level = paramContainerContainer.getAttributes();
                    while (level.hasMoreElements() ) {
                        p=(Parameter)level.nextElement();
                        try {
                            _scope.append(p);
                        } catch (Exception ex) {
                            // name clash between the two levels of scope
                        }
                    }
                }
            }
            _lastVersion = workspace().getVersion();
            return _scope;
        }
    }
        
     /** Get the Token this Parameter contains. It may be null.
      *  @return The token contained by this parameter.
     */
    public pt.data.Token getToken() {
        return _token;
    }
   

    /** Reset the current value of this parameter to the first seen 
     *  token or string. If the Parameter was initially given a 
     *  Token, set the current Token to that Token. Else, parse the
     *  String given in the constructor.
     *  @exception IllegalArgumentException Thrown if the Parameter 
     *  was created from an expression, and reevaluating that expression
     *  now yields a Token incompatible with the type of this Parameter.
     */	
   
    public void reset() throws IllegalArgumentException {
        if (_noTokenYet) return;
        if (_origToken != null) {
            _token = _origToken;
            _currentValue = null;
            _parseTreeRoot = null;
        } else  { //must have an _initialValue
            pt.data.Token oldToken = _token;
            if (_parser == null) {
                _parser = new PtParser(this);
            }
            _currentValue = _initialValue;
            _parseTreeRoot = _parser.generateParseTree(_initialValue, getScope());
            _token = _parseTreeRoot.evaluateParseTree();
            _checkType(_token);
            TokenPublisher publisher = oldToken.getPublisher();
            if ( publisher != null ) {
                _token.setPublisher(publisher);
            }
        }
        _token.notifySubscribers();
    }

    /** Put a new Token in this Parameter. This is the way to give the 
     *  give the Parameter a new simple value.
     *  @param token The new Token to be stored in this Parameter.
     * FIXME: synchronization needs to be looked at.
     */
    public void setToken(pt.data.Token token) {
        if (_noTokenYet && (token !=null)) {
            _origToken = token;
            _noTokenYet = false;
            _paramType = token.getClass();
        }
        _parseTreeRoot = null;
        _currentValue = null;
        pt.data.Token oldToken = _token;
        _token = token;
        _checkType(_token);
        // Now transfer the TokenPublisher between the old & new tokens
        TokenPublisher publisher = oldToken.getPublisher();
        if ( publisher != null ) {
            _token.setPublisher(publisher);
        }
        _token.notifySubscribers();
    }

    /** Set the parameter by parsing and evaluating the given String argument.
     * The string must be non-null. If the parameter has not been given 
     * a string or a Token yet, the string argument is used to set the 
     * initial state(value and type) of the parameter.
     * @param str The string to be evaluated to set the value of the parameter.
     * @exception IllegalArgumentException Thrown if try to set the value 
     *  of this parameter from a null string.
     */
    public void setTokenFromExpr(String str) throws IllegalActionException {
        if (str == null) {
            String tmp = "Cannot set the value of a parameter from a null ";
            throw new IllegalArgumentException(tmp + "string");
        }
        if (_parser == null) {
            _parser = new PtParser(this);
        }
        _currentValue = str;
        pt.data.Token prevToken = _token;
        synchronized(workspace()) {
            _parseTreeRoot = _parser.generateParseTree(str, getScope());
            _token = _parseTreeRoot.evaluateParseTree();
            //_parseTreeRoot.displayParseTree(" ");
        }
        if (_noTokenYet) {
            _initialValue = str;
            _noTokenYet = false;
            _paramType = _token.getClass();
            // don't need to check type as first token in
        } else {
            _checkType(_token);
        }
        // Now transfer the TokenPublisher between the old & new tokens
        if (prevToken != null ){
            TokenPublisher publisher = prevToken.getPublisher();
            if ( publisher != null ) {
                _token.setPublisher(publisher);
            }
        }
        _token.notifySubscribers();
    }
    
    /** Set the types of Tokens that this parameter can contain.
     *  It must be possible to losslessly convert the currently 
     *  contained Token to the new type, or else an exception will 
     *  be thrown. If so, the state of the parameter is unchanged.
     *  @param newType The class object representing the new type 
     *   of this parameter.
     *  @exception IllegalArgumentException Thrown if the new type 
     *   is too restrictive for the currently contained token.
     */
    public void setType(Class newType) throws IllegalArgumentException {
        Class oldType = _paramType;
        _paramType = newType;
        try {
            _checkType(_token.getClass());
        } catch (IllegalArgumentException ex) {
            _paramType = oldType;
            String str = "Cannot set the type of Parameter " + getName();
            str += " to type: " + newType.getName() + ", when the ";
            str += "currently contained Token is of type: ";
            str += _token.getClass().getName();
            throw new IllegalArgumentException(str);
        }
    }


    /** Get a string representation of the current parameter value.
     *  @return A String representing the class and the current token.
     */	
    public String toString() {
        String s =  super.toString() + " " + getToken().toString();
        return s;
    }

    /** Normally this method is called by an object this Parameter is
     *  observing. It is also called to re-establish dependencies 
     *  between parameters after a NamedObj has been cloned.
     *  @param o the Observable object that called this method.
     *  @param t not used.
     *  @exception IllegalArgumentException Thrown if type of the 
     *   resulting Token is not allowed in this Parameter.
     */
    public void update(Observable o, Object t) throws IllegalArgumentException {
        if (_dependencyLoop) {
            String str = "Found dependency loop in " + getFullName();
            str += ": " + _currentValue;
            throw new IllegalArgumentException(str);
        }
        _dependencyLoop = true;
        try {
            if ( _parseTreeRoot != null) {
                pt.data.Token oldToken = _token;
                _token = _parseTreeRoot.evaluateParseTree();
                _checkType(_token.getClass());
                TokenPublisher publisher = oldToken.getPublisher();
                if ( publisher != null ) {
                    _token.setPublisher(publisher);
                }
                _token.notifySubscribers();
                // _parseTreeRoot.displayParseTree(" ");
            } else if (_currentValue != null) { 
                // this method must be being invoked following a clone
                _parser = new PtParser(this);
                _parseTreeRoot = _parser.generateParseTree(_currentValue);
            }
        } catch (IllegalArgumentException ex) {
            _dependencyLoop = false;
            throw ex;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////
    
    /** Checks to see if the new token type is compatible with the initial 
     *  Token type stored. If the new Token cannot be converted in a lossless 
     *  manner to the original type, an exception is thrown.
     *  @param tryType The class of the token that is trying to be placed 
     *   in the Parameter.
     *  @exception IllegalArgumentException thrown if incompatible types
     */
    protected void _checkType(Class tryType) 
            throws IllegalArgumentException {
        int typeInfo = TypeCPO.compare(_paramType, tryType);
        if (typeInfo == CPO.STRICT_GREATER) return;
        if (typeInfo == CPO.EQUAL) return;
        // Incompatible type!
        String str = "Cannot store a Token of type ";
        str += tryType.getName() + " in a Parameter restricted to ";
        str += "tokens of type " + _paramType.getName() + " or lower";
        throw new IllegalArgumentException(str);
    }

    /** Checks to see if the new token type is compatible with the initial 
     *  Token type stored. If the new Token cannot be converted in a lossless 
     *  manner to the original type, an exception is thrown.
     *  @param tok The token that is trying to be placed in the Parameter.
     *  @exception IllegalArgumentException thrown if incompatible types
     */
    protected void _checkType(pt.data.Token tok) 
            throws IllegalArgumentException {
        if (tok == null) return; 
        else {
            _checkType(tok.getClass());
        }
    }

       
    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////


    // Stores the string used to set this expression. It is null if 
    // the parameter was set using a token.
    private String _currentValue;

    // used to check for dependency loops amongst parameters.
    private boolean _dependencyLoop = false;

    // Stores the string used to initialize the parameter. It is null if 
    // the first token placed in the parameter was not parsed from a string.
    private String _initialValue;

    // Used to test if the version of the workspace has changed since the 
    // last time getScope() was called
    private long _lastVersion = 0;

    // Tests if the parameter has not yet contained a Token.
    private boolean _noTokenYet = true;

    // Stores the first Token placed in this parameter. It is null if the
    // first token contained by the parameter was parsed from an expression.
    private pt.data.Token _origToken;

    // Stores the type information for this parameter.
    private Class _paramType;

    // The parser used by this parameter to parse expressions.
    private PtParser _parser;

    // If the parameter was last set from an expression, this stores 
    // the parse tree for that expression.
    private ASTPtRootNode _parseTreeRoot;

    // The parameters this parameter may reference in an expression.
    private NamedList _scope;

    // The token contained by this parameter.
    private pt.data.Token _token;   
}
