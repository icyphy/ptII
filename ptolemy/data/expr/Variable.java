/* A Variable is a Parameter with an extensible scope.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Variable
/**
A Variable contains a token and can be referenced in expressions. It
extends the Parameter class by allowing other variables to be added
to its scope. By default, the scope of a
variable includes the parameters of the container
and the container's container. The scope can be enlarged by
adding multiple lists of variables with the addToScope() method.
If there are name duplications, then the most recently added variable
will always be the one used.  The default scope from the base class,
therefore, is the last to be searched for a name.

@author Xiaojun Liu, Edward A. Lee
@version $Id$
*/

public class Variable extends Parameter {
    
    /** Construct a variable in the default workspace with an empty string
     *  as its name.
     *  The variable is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */    
    public Variable() {
        super();
    }

    /** Construct a variable in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The variable is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the variable.
     */
    public Variable(Workspace workspace) {
        super(workspace);
    }

    /** Construct a variable with the given name contained as an attribute
     *  by the specified entity. The container argument must not be null, 
     *  or a NullPointerException will be thrown. This variable will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the variable.
     *  @exception IllegalActionException If the variable is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   any attribute already in the container.
     */
    public Variable(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a variable with the given container, name, and token.
     *  The container argument must not be null, or a NullPointerException 
     *  will be thrown. This variable will use the workspace of the 
     *  container for synchronization and version counts. If the name 
     *  argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name.
     *  @param token The token contained by this variable.
     *  @exception IllegalActionException If the variable is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   any attribute already in the container.
     */
    public Variable(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the variables or parameters contained in the argument
     *  to the scope of this variable. If any of the variables bears
     *  the same name as one already in the scope, then it will shadow
     *  the one in the scope.  If any of the items in the list is not
     *  of class Parameter (or a derived class), then a class-cast
     *  exception may eventually result (when the items in the scope
     *  are accessed).
     *  @param varlist The list of parameters to be added to scope.
     */
    public void addToScope(NamedList varlist) {
        if (varlist == null) {
            return;
        }
        if (_addedVarLists == null) {
            _addedVarLists = new LinkedList();
        }
        _addedVarLists.insertFirst(varlist);
        _scopeVersion = -1;
    }

    /** Clone the variable.
     *  The state of the cloned variable will be identical to this 
     *  variable, but without any of the added scope.
     *  @param ws The workspace in which to place the cloned variable.
     *  @exception CloneNotSupportedException If this variable 
     *   cannot be cloned.
     *  @see ptolemy.data.expr.Parameter#clone()
     *  @return A clone of this Variable.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Variable newvar = (Variable)super.clone(ws);
        newvar._addedVarLists = null;
        newvar._scopeVersion = -1;
        return newvar;
    }

    /** Obtain a NamedList of parameters that the value 
     *  of this variable can depend on. This includes those added
     *  by addToScope() as wells as parameters of the container and
     *  the container's container. If there is a clash in the 
     *  names in these sets of parameters, then the most parameters
     *  most recently added to the scope will shadow those that were
     *  added before and those inherited from the container.
     *  This method is read-synchronized on the workspace.
     *  @return Variables and parameters on which this variable can depend.
     */
    public NamedList getScope() {
        if (_scopeVersion == workspace().getVersion()) {
            return _scope;
        }
        try {
            workspace().getReadAccess();        
            // get the list of parameters visible to this variable
            NamedList paramlist = super.getScope();
            if (_addedVarLists == null) return paramlist;

            // combine paramlist with the added variables
            NamedList result = new NamedList();
            Enumeration scopelists = _addedVarLists.elements();
            while (scopelists.hasMoreElements()) {
                NamedList scopelist = (NamedList)scopelists.nextElement();
                Enumeration variables = scopelist.elements();
                while (variables.hasMoreElements()) {
                    Parameter param = (Parameter)variables.nextElement();
                    try {
                        result.prepend(param);
                    } catch (IllegalActionException ex) {
                        // this should not happen, parameters definitely
                        // have names
                    } catch (NameDuplicationException ex) {
                        // there is a variable with the same name as param
                        // added to this variable's scope, so param will be
                        // invisible to this variable
                    }
                }              
            }

            Enumeration params = paramlist.elements();
            while (params.hasMoreElements()) {
                Nameable param = (Nameable)params.nextElement();
                try {
                    result.prepend(param);
                } catch (IllegalActionException ex) {
                    // this should not happen, parameters definitely
                    // have names
                } catch (NameDuplicationException ex) {
                    // there is a variable with the same name as param
                    // added to this variable's scope, so param will be
                    // invisible to this variable
                }
            }
            _scope = result;
            _scopeVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
        return _scope;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Stores the VariableLists whose contained variables have been 
    // added to the scope of this variable.
    private LinkedList _addedVarLists = null;
}
