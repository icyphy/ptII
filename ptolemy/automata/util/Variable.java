/* A Variable contains a token and can be referenced in expressions.

 Copyright (c) 1997-%Q% The Regents of the University of California.
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

// this class is general enough, may well belong to ptolemy.data.expr
package ptolemy.automata.util;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import collections.LinkedList;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// Variable
/**
A Variable contains a token and can be referenced in expressions.
<p>
The first difference from the Parameter class is scope. By default, a
variable's scope includes the parameters of the variable's container
and the variable's container's container. The user can change the scope
by adding multiple lists of variables to the scope with addToScope() method. 
<p>
The second difference is when variables are contained in variable lists,
their behavior when values change can be controlled on a per list basis.
More specifically, if a variable list's _respondToChange flag is false,
the variables in the list will not reevaluate automatically when the
values of the parameters/variables they depend on change. If the list's
_reportChange flag is false, then the variables in the list will not 
notify their listeners of changes in their value.
<p>

@author Xiaojun Liu
@version %W% %G%
@see ptolemy.data.expr.Parameter
@see ptolemy.automata.util.VariableList
*/

public class Variable extends Parameter {
    
    // All the constructors are wrappers of the super class constructors.

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

    /** Add the variables contained in the argument to the scope of 
     *  this variable. A NameDuplicationException is thrown if there
     *  is a clash of name among the added variables.
     *  This method is write-synchronized on the workspace.
     *  @param varlist The list of variables to be added to scope.
     *  @exception NameDuplicationException There is a clash of name
     *   among the added variables.
     */
    public void addToScope(VariableList varlist) 
            throws NameDuplicationException {
        if (varlist == null) {
            return;
        }
        if (_addedVarLists != null) {
            if (_addedVarLists.includes(varlist)) {
                return;
            }
        } else {
            _addedVarLists = new LinkedList();
        }
        if (_addedVars == null) {
            _addedVars = new NamedList(this);
        } 
        try {
            workspace().getReadAccess();
            NamedList newlist = new NamedList(_addedVars);
            Enumeration vars = varlist.getVariables();
            while (vars.hasMoreElements()) {
                try {
                    newlist.prepend((Nameable)vars.nextElement());
                } catch (IllegalActionException ex) {
                    // this should not happen, variables definitely
                    // have names
                }
            }
            _addedVars = newlist;
            _addedVarLists.insertFirst(varlist);
            _scopeVersion = -1;
        } finally {
            workspace().doneReading();
        }
    }
        
    /** Clone the variable.
     *  The state of the cloned variable will be identical to the original
     *  varialbe, but without the ParameterListener dependencies set up.
     *  These are set up only after first creating all the parameters and
     *  variables on which this variable depends, add these variables to 
     *  the scope of this variable, AND evaluate() is called.
     *  @param The workspace in which to place the cloned varialbe.
     *  @exception CloneNotSupportedException If the variable cannot be 
     *   cloned.
     *  @see java.lang.Object#clone()
     *  @return An identical variable.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Variable newvar = (Variable)super.clone(ws);
        newvar._addedVarLists = null;
        newvar._addedVars = null;
        return newvar;
    }

    /** Obtain a NamedList of variables and parameters that the value of 
     *  this variable can depend on. The variables are those added to the
     *  scope of this variable by addToScope(). The parameters are limited 
     *  to those contained by the same NamedObj and those one level up the 
     *  hierarchy. It catches any exceptions thrown by NamedList because 
     *  if there is a clash in the names of the two scoping levels, then 
     *  the parameter from the upper level is considered to be invisible 
     *  to this variable, and if a parameter has the same name as an added
     *  variable, then the parameter is made invisible. A variable also 
     *  cannot reference itself.
     *  This method is read-synchronized on the workspace.
     *  @return The variables and parameters on which this variable can 
     *   depend.
     */
    public NamedList getScope() {
        if (_scopeVersion == workspace().getVersion()) {
            return _scope;
        }
        try {
            workspace().getReadAccess();        
            // get the list of parameters visible to this variable
            NamedList paramlist = super.getScope();
            // combine paramlist with the added variables
            NamedList result = new NamedList(_addedVars);
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

    /** A parameter/variable which the expression of this variable 
     *  references has changed value. If this variable is contained
     *  in a VariableList and the container's _respondToChange flag
     *  is false then do nothing. Otherwise we just call evaluate() 
     *  to obtain the new value to be stored in this variable.
     *  @param event The ParameterEvent containing the information
     *   about why the referenced parameter/variable changed.
     */
    public void parameterChanged(ParameterEvent event) {
        Nameable container = getContainer();
        if (container != null && (container instanceof VariableList)) {
            if (((VariableList)container)._respondToChange) {
                evaluate();
            }
        }
    }

    /** Remove the variables contained in the argument from the scope 
     *  of this variable.
     *  This method is write-synchronized on the workspace.
     *  @param varlist The list of variables to be removed from scope.
     */
    public void removeFromScope(VariableList varlist) {
        if (!_addedVarLists.includes(varlist)) {
            return;
        }
        try {
            workspace().getReadAccess();
            Enumeration vars = varlist.getVariables();
            while (vars.hasMoreElements()) {
                Nameable var = (Nameable)vars.nextElement();
                _addedVars.remove(var);
            }
            _addedVarLists.exclude(varlist);
            varlist._removeDependent(this);
            _scopeVersion = -1;
        } finally {
            workspace().doneReading();
        }
        _rebuildDependencies();
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /*  If this variable is contained in a VariableList and the 
     *  container's _reportChange flag is false then do nothing. 
     *  Otherwise, notify the ParameterListeners that have registered 
     *  an interest/dependency in this variable that the variable's 
     *  value has changed. 
     */
    protected void _notifyListeners(ParameterEvent event) {
        Nameable container = getContainer();
        if (container != null && (container instanceof VariableList)) {
            if (((VariableList)container)._reportChange) {
                super._notifyListeners(event);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Stores the VariableLists whose contained variables have been 
    // added to the scope of this variable.
    private LinkedList _addedVarLists = null;

    // Stores the Variables which have been added to the scope of 
    // this variable.
    private NamedList _addedVars = null;

}









