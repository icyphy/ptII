/* A Variable contains a token and can be referenced in expressions.

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

// this class is general enough, may well belong to ptolemy.data.expr
package ptolemy.automata.util;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Variable
/**
A Variable contains a token and can be referenced in expressions. It is
derived from the Parameter class.
<p>
The first difference from the Parameter class is scope. By default, a
variable's scope includes the parameters of the variable's container
and the variable's container's container. The scope can be enlarged by
adding multiple lists of variables to it with the addToScope() method. 
<p>
The second difference is when variables are contained in variable lists,
their behavior when values change can be controlled on a per list basis.
More specifically, if setRespondToChange(false) has been called on a 
variable list, then the variables in the list will not automatically
reevaluate when the values of the parameters or variables on which they 
depend change. If setReportChange(false) has been called on a list, the 
variables in the list will not notify their listeners of changes in 
their value.

@author Xiaojun Liu
@version $Id$
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
     *  are two variables with the same name in the scope.
     *  This method is read-synchronized on the workspace.
     *  @param varlist The list of variables to be added to scope.
     *  @exception NameDuplicationException If there are two variables
     *   with the same name in the scope.
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
     *  The state of the cloned variable will be identical to this 
     *  varialbe, but without the ParameterListener dependencies 
     *  set up. These are set up only after first creating all the 
     *  parameters and variables on which the cloned variable will
     *  depend, adding these variables to the scope of the cloned
     *  variable, AND evaluate() is called on the cloned variable.
     *  @param The workspace in which to place the cloned varialbe.
     *  @exception CloneNotSupportedException If this variable 
     *   cannot be cloned.
     *  @see ptolemy.data.expr.Parameter#clone()
     *  @return An identical Variable.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Variable newvar = (Variable)super.clone(ws);
        newvar._addedVarLists = null;
        newvar._addedVars = null;
        newvar._scopeVersion = -1;
        return newvar;
    }

    /** Obtain a NamedList of variables and parameters that the value 
     *  of this variable can depend on. The variables are those added 
     *  to the scope of this variable by addToScope(). The parameters 
     *  are limited to those of this variable's container and this
     *  variable's container's container. If there is a clash in the 
     *  names of these two scoping levels, the parameter from the top 
     *  level is considered not to be visible in the scope of this
     *  variable. If a parameter has the same name as an added variable, 
     *  then the parameter is considered invisible. A variable also 
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
            NamedList result = null;
            if (_addedVars != null) {
                result = new NamedList(_addedVars);
            } else {
                result = new NamedList();
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

    /** A parameter/variable which the expression of this variable 
     *  references has changed. If this variable is contained in a 
     *  VariableList and setRespondToChange(false) has been called
     *  on it then do nothing. Otherwise we just call evaluate() 
     *  to obtain the new value of this variable.
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
     *  This method is read-synchronized on the workspace.
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

    /** If this variable is contained in a VariableList and 
     *  setReportChange(false) has been called on it then do nothing. 
     *  Otherwise, notify the ParameterListeners that have registered 
     *  a dependency on this variable that this variable has changed. 
     */
    protected void _notifyListeners(ParameterEvent event) {
        Nameable container = getContainer();
        if (container != null && (container instanceof VariableList)) {
            if (((VariableList)container)._reportChange) {
                super._notifyListeners(event);
            }
        }
    }

    /** Add the argument to the scope of this variable. This method is 
     *  only called when the argument is added to a VariableList which
     *  has been added to this variable's scope.
     *  @param var The variable to be added.
     *  @exception NameDuplicationException If there is a variable with
     *   the same name as the argument already in this variable's scope.
     */
    protected void _addVarToScope(Variable var)
            throws NameDuplicationException {
        if (var == null) {
            return;
        }
        if (_addedVars == null) {
            _addedVars = new NamedList(this);
        }
        if (_addedVars.includes(var)) {
            throw new NameDuplicationException(this, var,
                    "There is already a variable with name "
                    + var.getName() + " in this variable's scope.");
        }
        try {
            _addedVars.prepend(var);
        } catch (IllegalActionException ex) {
            // this will not happen, since variables are Nameable objects
        }
        _scopeVersion = -1;
        return;
    }

    /** Remove the argument from the scope of this variable. This method 
     *  is only called when the argument is removed from a VariableList 
     *  which has been added to this variable's scope.
     *  @param The variable to be removed.
     */
    protected void _removeVarFromScope(Variable var) {
        if (var == null) {
            return;
        }
        _addedVars.remove((Nameable)var);
        _scopeVersion = -1;
        _rebuildDependencies();
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

