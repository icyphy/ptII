/* A VariableList contains a list of Variables.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel.util;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;


//////////////////////////////////////////////////////////////////////////
//// VariableList
/**
A VariableList contains a list of Variables.
<p>
Besides its function as an aggregation of a set of variables, the
variable list affects the behavior of the variables contained in it in
two ways: if setReportChange(false) has been called, the variables in
the list will not notify the parameters/variables that depends on them
when their values change. If setRespondToChange(false) has been called,
then the variables in the list will not re-evaluate automatically when
notified of changes in the parameters/variables they depend on, so it
is the user's responsibility to call evaluate() on these variables
before reading their values.

@author Xiaojun Liu
@version $Id$
@see Variable
*/

public class VariableList extends Attribute {

    // All the constructors are wrappers of the super class constructors.

    /** Construct a variable list in the default workspace with an empty
     *  string as its name.
     *  The variable list is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public VariableList() {
	super();
    }

    /** Construct a variable list in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The variable list is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the variable list.
     */
    public VariableList(Workspace workspace) {
	super(workspace);
    }

    /** Construct a variable list with the given name contained as an
     *  attribute by the specified entity. The container argument must
     *  not be null, or a NullPointerException will be thrown. This
     *  variable list will use the workspace of the container for
     *  synchronization and version counts. If the name argument is
     *  null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this variable list.
     *  @exception IllegalActionException If the variable list is not of
     *   an acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   any attribute already in the container.
     */
    public VariableList(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a variable to this list. Notify the variables that have
     *  included this list of variables in their scope. If there is
     *  already a variable with the same name in the list, throw a
     *  NameDuplicationException.
     *  @param var The variable to be added to the list.
     *  @exception IllegalActionException If the variable is not in the
     *   same workspace as this list, or the proposed operation would
     *   result in recursive containment.
     *  @exception NameDuplicationException If there is already a
     *   variable with the same name in the list.
     */
    public void addVariable(Variable var)
            throws IllegalActionException, NameDuplicationException {
        // this._addAttribute(var) will be called consequently, and
        // the notification is done there
        var.setContainer(this);
    }

    /** Clone the variable list into the specified workspace. The new
     *  variable list is <i>not</i> added to the directory of that
     *  workspace (you must do this yourself if you want it there).
     *  The result is a variable list with no container, and all the
     *  dependency information of the variables in the list is lost.
     *  @param ws The workspace for the cloned variable list.
     *  @exception CloneNotSupportedException If a variable in the list
     *   cannot be cloned.
     *  @return The new variable list.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        VariableList newobj = (VariableList)super.clone(ws);
        _dependents = null;
        return newobj;
    }

    /** Create new variables in this list with the same name as the
     *  Nameable objects in the enumeration argument. Notify the
     *  variables that have included this list of variables in their
     *  scope.
     *  @param nameables An enumeration of Nameable objects specifying
     *   the names of variables to be created.
     *  @exception NameDuplicationException If there is already a
     *   variable with the same name in the list, or if there are two
     *   Nameable objects with the same name in the argument.
     */
    public void createVariables(Enumeration nameables)
            throws NameDuplicationException {
        try {
            while (nameables.hasMoreElements()) {
                Nameable obj = (Nameable)nameables.nextElement();
                Variable newvar = new Variable(this, obj.getName());
            }
        } catch (IllegalActionException ex) {
            // this should not happen since we are creating variables
            // in a variable list
        }
    }

    /** Enumerate the variables in this list.
     *  @return An enumeration of the variables in this list.
     */
    public Enumeration getVariables() {
        return Collections.enumeration(attributeList());
    }

    /** Get the current value of the variable named varname.
     *  @param varname The name of the variable.
     *  @return The current value of the variable.
     *  @exception NoSuchElementException There is no variable named
     *   varname in this variable list.
     */
    public ptolemy.data.Token getVarValue(String varname)
            throws NoSuchElementException, IllegalActionException {
        Variable var = (Variable)getAttribute(varname);
        if (var != null) {
            return var.getToken();
        } else {
            throw new NoSuchElementException(
                    "No variable named " + varname +
                    " in variable list " + this.getFullName());
        }
    }

    /** Remove a variable from this list. Do nothing if the variable
     *  to be removed is not in this list. Notify the variables that
     *  have included this list of variables in their scope.
     *  @param var The variable to be removed from this list.
     */
    public void removeVariable(Variable var) {
        if (var.getContainer() != this) {
            return;
        }
        try {
            // this._addAttribute(var) will be called consequently,
            // and the notification is done there
            var.setContainer(null);
        } catch (IllegalActionException ex) {
            // this should not happen
        } catch (NameDuplicationException ex) {
            // this should not happen
        }
    }

    /** Set the value of all the variables in this list to token.
     *  An IllegalArgumentException will be thrown if not all of
     *  the variables in this list can take token as value.
     *  @param token The value to be set to the variables.
     *  @exception IllegalActionException The token cannot be
     *   placed in some variables in this list.
     */
    public void setAllVariables(ptolemy.data.Token token)
            throws IllegalActionException {
        Enumeration enum = Collections.enumeration(attributeList());
        while (enum.hasMoreElements()) {
            Variable nextvar = (Variable)enum.nextElement();
            nextvar.setToken(token);
        }
    }

    /** Specify the container NamedObj. Add this variable list to the
     *  list of attributes of the argument if it is not null. Remove
     *  this variable list from the list of attributes of the current
     *  container and notify the variables that have included this
     *  list of variables in their scope that this list is removed.
     *  If the container already contains an attribute with the same
     *  name, then throw an exception and make no change. Similarly,
     *  if the container is not in the same workspace as this list,
     *  throw an exception. If this list is already contained by the
     *  container, do nothing. If this list is on the directory of the
     *  workspace, then remove it from there.
     *  This method is write synchronized on the workspace and increments
     *  its version number.
     *  @param container The container to attach this list as an attribute.
     *  @exception IllegalActionException If this variable list is not of
     *   the expected class for the container, or it has no name, or this
     *   variable list and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   any attribute with the same name as this list.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        if (container == getContainer()) {
            return;
        }
        if (_dependents != null) {
            LinkedList newlist = new LinkedList();
            Iterator vars = _dependents.iterator();
            while (vars.hasNext()) {
                Variable next = (Variable)vars.next();
                newlist.addFirst(next);
            }
            vars = newlist.iterator();
            while (vars.hasNext()) {
                Variable next = (Variable)vars.next();
                next.removeFromScope(getVariables());
            }
            // CHECK
            // here this list should be empty
            //_dependents.clear();
            vars = _dependents.iterator();
            if (vars.hasNext()) {
                throw new InvalidStateException(this, "Dangling dependencies.");
            }
        }
        super.setContainer(container);
        return;
    }

    /** If the argument is false, the variables in this list will not
     *  notify their dependents of changes in value.
     *  @param change If false, the variables in this list will not
     *   propagate changes in value.
     */
    public void setReportChange(boolean change) {
        _reportChange = change;
    }

    /** If the argument is false, the variables in this list will not
     *  respond to changes in value of the parameters/variables on
     *  which they depend. The variables' evaluate() method should be
     *  called before accessing their value.
     *  @param respond If false, the variables in the list will not
     *   respond to changes in value of the parameters/variables on
     *   which they depend.
     */
    public void setRespondToChange(boolean respond) {
        _respondToChange = respond;
    }

    /** Set the value of the variable with name varname to token.
     *  @param varname The name of the variable to be set.
     *  @param token The desired value of the variable.
     *  @exception IllegalActionException If the token is not of
     *   an acceptable type of the variable.
     */
    public void setVarValue(String varname, ptolemy.data.Token token)
            throws IllegalActionException {
        Variable var = (Variable)getAttribute(varname);
        if (var != null) {
            var.setToken(token);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to throw an exception if the attribute
     *  is not a Variable. If it is, then invoke the base class method.
     *  Notify the variables that have included this list of variables
     *  in their scope. NameDuplicationException is thrown if there is
     *  a variable with the same name as the argument in the scope of
     *  any of the variables which have included this list of variables
     *  in their scope.
     *  This method should not be used directly. Use the setContainer()
     *  method of the variable instead.
     *  @param attr The attribute (variable) to be added.
     *  @exception NameDuplicationException If this list already has a
     *   variable with the same name, or there is a variable with the
     *   same name as the argument in the scope of any of the variables
     *   which have included this list of variables in their scope.
     *  @exception IllegalActionException If the attribute is not an
     *   instance of Variable.
     */
    protected void _addAttribute(Attribute attr)
            throws NameDuplicationException, IllegalActionException {
        if (!(attr instanceof Variable)) {
            throw new IllegalActionException(this, attr,
                    "Cannot add to variable list.");
        }
        super._addAttribute(attr);
        _notifyAddVar((Variable)attr);
    }

    /** Add the variable as a dependent of this variable list. A
     *  dependent is a variable having this list of variables in
     *  its scope. These dependents are notified when a variable
     *  is added to or removed from this variable list, or this
     *  list is moved to another container.
     *  @param var The dependent variable.
     */
    protected void _addDependent(Variable var) {
        if (_dependents == null) {
            _dependents = new LinkedList();
        } else if (_dependents.contains(var)) {
            return;
        }
        _dependents.addFirst(var);
    }

    /** Remove the given attribute. The attribute should be a variable
     *  so notify dependents.
     *  @param attr The attribute to be removed.
     */
    protected void _removeAttribute(Attribute param) {
        if (!(param instanceof Variable)) {
            throw new InvalidStateException(this, param,
                    "VariableList can only have Variable as attribute.");
        }
        super._removeAttribute(param);
        _notifyRemoveVar((Variable)param);
    }

    /** Remove the variable from the list of dependents. Do nothing
     *  if the variable is not in the dependent list.
     *  @param var The variable to be removed from dependent list.
     */
    protected void _removeDependent(Variable var) {
        _dependents.remove(var);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Notify the dependent variables that the argument has been
     *  added to this variable list.
     *  @param var The variable added to this variable list.
     *  @exception NameDuplicationException If there is a variable
     *   with the same name as the argument already in a dependent
     *   variable's scope.
     */
    private void _notifyAddVar(Variable var)
            throws NameDuplicationException {
        if (_dependents == null ) {
            return;
        }
        Iterator vars = _dependents.iterator();
        while (vars.hasNext()) {
            Variable next = (Variable)vars.next();
            next.addToScope(var);
        }
    }

    /*  Notify the dependent variables that the argument is removed
     *  from this variable list.
     *  @param var The variable removed from this variable list.
     */
    private void _notifyRemoveVar(Variable var) {
        if (_dependents == null ) {
            return;
        }
        Iterator vars = _dependents.iterator();
        while (vars.hasNext()) {
            Variable next = (Variable)vars.next();
            next.removeFromScope(var);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** If false, the variables in this list will not notify their
     *  dependents of changes in value.
     */
    protected boolean _reportChange = true;

    /** If false, the variables in this list will not respond to
     *  changes in value of the parameters/variables on which they
     *  depend. The variables' evaluate() method should be called
     *  before accessing their value.
     */
    protected boolean _respondToChange = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Stores the Variables which have added the variables in this list
    // to their scope.
    private LinkedList _dependents = null;

}
