/* A Parameter is a container for a token.

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

*/

package pt.data;

import pt.kernel.*;
import pt.data.parser.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// Parameter
/**
 * A Parameter is a container for a token. The type of a Parameter is
 * set by the first Token placed in it. A Parameters type can be changed
 * later via a method call.
 * It can be given a Token or a String as its value. 
 * If a String is given, it uses PtParser to obtain the Token
 * resulting from parsing and evaluating the expression. 
 * If another Object (eg Parameter) wants to Observe this Parameter, it must 
 * register its interest with the TokenPublisher associated with the 
 * contained Token.
 * At any stage a new Token or String can be given to the Parameter. The 
 * new/resulting Tokens type is checked to see if it can be converted 
 * to the Parameters type in a lossles manner.
 * 
 * FIXME: the exception structure needs to be worked out, as does 
 * synchroniation isssues
 * 
 * @author Neil Smyth
 * @version $Id$
 * @see pt.data.parser.PtParser 
 * @see pt.data.Token 
*/
public class Parameter extends NamedObj implements Observer, Cloneable {
    
    /** Construct a Parameter with the given Token in the workspace of its 
     *  container with the given name. 
     *  If the name argument is null, then the name is set to the empty 
     *  string.
     *  @param container The NamedObj which contains this parameter
     *  @param name The name of this object, given to the superclass
     *  @param token The Token contained by this Parameter.
     */
     public Parameter(NamedObj container, String name, Token token) {
         super(container.workspace(), name);
         _container = container;
         try {
             _origToken = (Token)token.clone();
         } catch (CloneNotSupportedException c) {
             _origToken = token;
         }
         _token = token;
     }

     /** Construct a Parameter from the given String in the workspace of its 
     *   container with the given name. 
     *   The objects name is set to the empty string if name is null.
     *   @param container The NamedObj which contains this parameter.
     *   @param name The name for this object.
     *   @param value The String to be parsed to a Token.
     */
    public Parameter(NamedObj container, String name, String value) {
       super(container.workspace(), name);
       _container = container;
       _initialValue = value;
       _parser = new PtParser(this);
       
       // now need to sync as could depend on other Parameters
       synchronized(workspace()) {
           NamedList scope = getScope();
           _parseTreeRoot = _parser.generateParseTree(value, scope);
           _token = _parseTreeRoot.evaluateParseTree();
           //_parseTreeRoot.displayParseTree(" ");
       }
    }
    
     


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Promote the clone method of the base class Object to public.
     *  FIXME: needs to be fully implemented to return a proper clone.
     *  @param The workspace the cloned Parameter should be place in.
     *  @see java.lang.Object#clone()
     *  @return An identical Parameter.
     */
    public Object clone(Workspace newWS) throws CloneNotSupportedException {
        //Token newToken = _token.clone();
        //String name = getName();
        
        //if (_origToken != null) {
        //   ....            
        return super.clone();
    }

    /** Return a description of the object.
     *  @param verbosity The level of verbosity.
     *  FIXME: needs to be finisihed.
     */
    public String description(int verbosity) {
        return toString();
    }


    /** Get the Nameable this Parameter is attached to. It should be cast to 
     *  a Namedobj as Parameters can only be attached to NamedObj objects
     */
    public Nameable getContainer() {
        return _container;
    }
    
    /** Obtain a NamedList of the parameters that the value of this 
     *  Parameter can depend on. The scope is limited to the params in the 
     *  same NamedObj and those one level up in the hierarchy.
     *  It catches any exceptions thrown by NamedList as 1) the param must 
     *  have a container with a NamedList of Parameters, and 2) if there is a
     *  clash in the names of the two scopeing levels, the parameter from 
     *  the top level is considered not to be visible in the scope of this 
     *  Parameter
     *  
     */
    public NamedList getScope() {
        if ( (_scope != null) && (_lastVersion == workspace().getVersion()) ) {
            return _scope;
        } else {
            _scope = new NamedList();       
            NamedObj paramContainer = (NamedObj)getContainer();
            NamedObj paramContainerContainer = ((NamedObj)paramContainer.getContainer());
            Enumeration level = paramContainer.getParameters();       
            Parameter p;
            while (level.hasMoreElements() ) {
                // now  copy the nmaedlist, omitting the current Parameter
                if ( (p=(Parameter)level.nextElement()) != this ) {
                    try {
                        _scope.append(p);
                    } catch (Exception ex) {
                        // since we're basically copying a namedlist, these 
                        // exceptions cannot occer
                    }
                }
            }
            if (paramContainerContainer != null) {
                level = paramContainerContainer.getParameters();
                while (level.hasMoreElements() ) {
                    p=(Parameter)level.nextElement();
                    try {
                        _scope.append(p);
                    } catch (Exception ex) {
                        // name clash between the two levels of scope
                    }
                }
            }
            _lastVersion = workspace().getVersion();
            return _scope;
        }
    }
        
     /** Get the Token this Parameter contains
     */
    public Token getToken() {
        return _token;
    }
   

    /** Reset the current value of the state to the one that was given
     *  to it when it was created. If the Parameter was initially given a 
     *  Token, set the current Token to that Token. Else, parse the
     *  String given in the constructor.
     *  @exception IllegalArgumentException Thrown if the Parameter 
     *  was created from an expression, and reevaluating that expression
     *  now yields a Token incompatible with this Parameters type.
     */	
   
    public void reset() throws IllegalArgumentException {
        if (_origToken != null) {
            _token = _origToken;
        } else {
            Token oldToken = _token;
            _parseTreeRoot = _parser.generateParseTree(_initialValue, getScope());
            _token = _parseTreeRoot.evaluateParseTree();
            _checkType(_token);
            TokenPublisher publisher = oldToken.getPublisher();
            if ( publisher != null ) {
                _token.setPublisher(publisher);
            }
            _token.notifySubscribers();
        }
    }
   

    /** Put a new Token in this Parameter. This is the way to give the 
     *  give the Parameter a new simple value.
     *  @param token The new Token to be stored in this Parameter.
     * FIXME: synchronization needs to be looked at.
     */
    public void setToken(Token token) {
        _parseTreeRoot = null;
        Token oldToken = _token;
        _token = token;
        _checkType(_token);
        // Now transfer the TokenPublisher between the old & new tokens
        TokenPublisher publisher = oldToken.getPublisher();
        if ( publisher != null ) {
            _token.setPublisher(publisher);
        }
        _token.notifySubscribers();
    }

    /** Set the param by parsing and evaluating the given String argument.
     * FIXME: I'm not sure about the synchronization aspects here...
     * @param str The string to be evaluated to set the params value
     */
    public void setTokenFromExpr(String str) {
        if (_parser == null) {
            _parser = new PtParser(this);
        }
        Token oldToken = _token;
        synchronized(workspace()) {
            _parseTreeRoot = _parser.generateParseTree(str, getScope());
            _token = _parseTreeRoot.evaluateParseTree();
        }
        _checkType(_token);
        // Now transfer the TokenPublisher between the old & new tokens
        TokenPublisher publisher = oldToken.getPublisher();
        if ( publisher != null ) {
            _token.setPublisher(publisher);
        }
        _token.notifySubscribers();
    }
    

    /** Get a string representation of the current parameter value.
     * @return A String representing the current value.
     */	
    public String toString() {
        String s =  (super.toString() + " " + getToken().toString());
        return s;
    }

    /** Normally this method is called by an object this Parameter is
     *  observing. Also called if want to re-evaluate the current 
     *  Tokens value.
     *  @param o the Observable object that called this method
     *  @param t not used.
     *  @exception IllegalArgumentException Thrown if the resulting 
     *  Token type is not allowed in this Parameter.
     */
    public void update(Observable o, Object t) throws IllegalArgumentException {
        if ( _parseTreeRoot != null) {
            Token oldToken = _token;
            _token = _parseTreeRoot.evaluateParseTree();
            _checkType(_token);
            TokenPublisher publisher = oldToken.getPublisher();
            if ( publisher != null ) {
                _token.setPublisher(publisher);
            }
            _token.notifySubscribers();
            // _parseTreeRoot.displayParseTree(" ");
        }  
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////
    
    /** Checks to see if the new token type is compatible with the initial 
     *  Token type stored. If the new Token cannot be converted in a lossless 
     *  manner to the original type, an exception is thrown.
     *  @exception IllegalArgumentException thrown if incompatible types
     *  FIXME: currently this just checks if they are the same type!!
     */
    protected void _checkType(Token t) {
        String type1 = this.getToken().getClass().getName();
        if( !(type1.equals(t.getClass().getName()))) {
            String str = "Cannot store a Token of type ";
            str = str + t.getClass().getName() + " in a Parameter restricted";
            str = str + " to tokens of type " + type1 + "or lower";
            throw new IllegalArgumentException(str);
        }
    }
    
        
    
    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private NamedObj _container;
    private Token _token;
    private Token _origToken;
    private String _initialValue;
    private PtParser _parser;
    private ASTPtRootNode _parseTreeRoot;
    private NamedList _scope;
    private long _lastVersion = 0;
}
