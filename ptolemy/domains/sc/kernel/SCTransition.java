/* A SCTransition connects two SCStates.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package ptolemy.domains.sc.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.automata.util.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// SCTransition
/**
A SCTransition connects two SCStates. It has a trigger event, a trigger
condition, and a set of trigger actions.

@author Xiaojun Liu
@version @(#)SCTransition.java	1.2 11/27/98
*/
public class SCTransition extends ComponentRelation {

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // List trigger event and trigger condition.
    VariableList _trigger = null;

    // Trigger event variable.
    Variable _te;

    // Trigger condition variable.
    Variable _tc;

    // List trigger actions.
    VariableList _actions = null;

    // List local variable updates.
    VariableList _localVarUpdates = null;

    // If true, this transition is preemptive.
    boolean _preemptive = false;

    // If true, the destination state's subsystem will be initialized
    // each time this transition is taken.
    boolean _initEntry = false;

    // Source state.
    SCState _source = null;

    // Destination state.
    SCState _dest = null;

    // Version of source/dest states.
    long _stateVersion = -1;

    // Trigger event set.
    boolean _teSet = false;

    // Trigger condition set.
    boolean _tcSet = false;

    // List of transition actions.
    LinkedList _transActions = null;

    public void addTransitionAction(TransitionAction act) {
        if (act == null) {
            return;
        }
        if (_transActions == null) {
            _transActions = new LinkedList();
        }
        _transActions.insertFirst(act);
    }

    public void executeTransitionActions() {
        if (_transActions == null) {
            return;
        }
        Enumeration tas = _transActions.elements();
        while (tas.hasMoreElements()) {
            TransitionAction ta = (TransitionAction)tas.nextElement();
            ta.execute();
        }
    }

    private void _createVarLists() {
        try {
            _trigger = new VariableList(this, "Trigger");
            _trigger.setRespondToChange(false);
            _te = new Variable(_trigger, "TriggerEvent");
            _tc = new Variable(_trigger, "TriggerCondition");
            _actions = new VariableList(this, "Actions");
            _actions.setRespondToChange(false);
            _localVarUpdates = new VariableList(this, "LocalVarUpdates");
            _localVarUpdates.setRespondToChange(false);
        } catch (IllegalActionException ex) {
            // this should not happen
        } catch (NameDuplicationException ex) {
            // this should not happen
        }
    }

    /** Construct a transition in the default workspace with an empty string
     *  as its name. Add the transition to the directory of the workspace.
     */
    public SCTransition() {
        super();
    }

    /** Construct a transition in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the transition to the workspace directory.
     *  @param workspace The workspace that will list the transition.
     */
    public SCTransition(Workspace workspace) {
	super(workspace);
    }

    /** Construct a transition with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public SCTransition(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        SCTransition newobj = (SCTransition)super.clone(ws);
        newobj._stateVersion = -1;
        if (_trigger != null) {
            newobj._trigger = (VariableList)newobj.getAttribute("Trigger");
            newobj._te = (Variable)newobj._trigger.getAttribute("TriggerEvent");
            newobj._tc = (Variable)newobj._trigger.getAttribute("TriggerCondition");
            newobj._actions = (VariableList)newobj.getAttribute("Actions");
            newobj._localVarUpdates = (VariableList)newobj.getAttribute("LocalVarUpdates");
        }
        return newobj;
    }

    /** If the argument is true, set the transition to be preemptive.
     */
    public void setPreemptive(boolean preempt) {
        _preemptive = preempt;
    }

    /** Return whether this transition is preemptive.
     */
    public boolean isPreemptive() {
        return _preemptive;
    }

    /** If the argument is true, this transition will initialize the
     *  destination state's subsystem when taken.
     */
    public void setInitEntry(boolean init) {
        _initEntry = init;
    }

    /** Return whether this transition will initialize the destination
     *  state's subsystem when taken.
     */
    public boolean isInitEntry() {
        return _initEntry;
    }

    /** Return the source state of this transition.
     */
    public SCState sourceState() {
        if (_stateVersion == workspace().getVersion()) {
            return _source;
        }
        _getStates();
        return _source;
    }

    /** Return the destination state of this transition.
     */
    public SCState destinationState() {
        if (_stateVersion == workspace().getVersion()) {
            return _dest;
        }
        _getStates();
        return _dest;
    }

    /** Get the source/destination states of this transition.
     */
    private void _getStates() {
        try {
            workspace().getReadAccess();
            Enumeration ports = linkedPorts();
            Port p;
            _source = null;
            _dest = null;
            while (ports.hasMoreElements()) {
                p = (Port)ports.nextElement();
                if (p.getName().compareTo(SCState.INCOMING_PORT) == 0) {
                    _dest = (SCState)p.getContainer();
                }
                if (p.getName().compareTo(SCState.OUTGOING_PORT) == 0) {
                    _source = (SCState)p.getContainer();
                }
            }
            _stateVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    /** Set the trigger event of this transition.
     */
    public void setTriggerEvent(String te) {
        if (_te == null) {
            _createVarLists();
        }
        _te.setExpression(te);
        _teSet = true;
    }

    /** Set the trigger condition of this transition.
     */
    public void setTriggerCondition(String tc) {
        if (_tc == null) {
            _createVarLists();
        }
        _tc.setExpression(tc);
        _tcSet = true;
    }

    /** Setup the scope of trigger event, trigger condition, and trigger
     *  actions.
     */
    public void setupScope() throws NameDuplicationException {
        SCState src = sourceState();
        VariableList localInputVarS =
                (VariableList)src.getAttribute(SCState.LOCAL_INPUT_STATUS_VAR_LIST);
        VariableList localInputVarV =
                (VariableList)src.getAttribute(SCState.LOCAL_INPUT_VALUE_VAR_LIST);
        VariableList inputVarS = null;
        VariableList inputVarV = null;
        VariableList localVars = null;
        SCController ctrl = (SCController)getContainer();
        if (ctrl != null) {
            inputVarS = (VariableList)ctrl.getAttribute(SCController.INPUT_STATUS_VAR_LIST);
            inputVarV = (VariableList)ctrl.getAttribute(SCController.INPUT_VALUE_VAR_LIST);
            localVars = (VariableList)ctrl.getAttribute(SCController.LOCAL_VARIABLE_LIST);
        }
        _te.addToScope(inputVarS);
        _te.addToScope(localVars);
        _tc.addToScope(inputVarV);
        _tc.addToScope(localVars);
        if (!isPreemptive()) {
            _te.addToScope(localInputVarS);
            _tc.addToScope(localInputVarV);
        }
        Enumeration actvars = _actions.getVariables();
        Variable act;
        while (actvars.hasMoreElements()) {
            act = (Variable)actvars.nextElement();
            act.addToScope(inputVarV);
            act.addToScope(localVars);
            if (!isPreemptive()) {
                act.addToScope(localInputVarV);
            }
        }
        Enumeration varUpdates = _localVarUpdates.getVariables();
        Variable update;
        while (varUpdates.hasMoreElements()) {
            update = (Variable)varUpdates.nextElement();
            update.addToScope(inputVarV);
            update.addToScope(localVars);
            if (!isPreemptive()) {
                update.addToScope(localInputVarV);
            }
        }

        // initialize the transition actions
        if (_transActions != null) {
            Enumeration tas = _transActions.elements();
            while (tas.hasMoreElements()) {
                TransitionAction ta = (TransitionAction)tas.nextElement();
                ta.initialize();
            }
        }

    }

    public VariableList getTriggerActions() {
        return _actions;
    }

    /** Add a trigger action.
     */
    public void addTriggerAction(String name, String expr)
            throws NameDuplicationException {
        if (_actions == null) {
            _createVarLists();
        }
        try {
            Variable var = new Variable(_actions, name);
            if (expr == null) {
                try {
                    var.setType(Class.forName("ptolemy.data.Token"));
                    var.setToken(new ptolemy.data.Token());
                } catch (ClassNotFoundException ex) {
                    // ignore for now
                }
            } else {
                var.setExpression(expr);
            }
        } catch (IllegalActionException ex) {
            // this should not happen
        }
    }

    /** Add a local variable update.
     */
    public void addLocalVariableUpdate(String name, String expr)
            throws NameDuplicationException {
        if (_localVarUpdates == null) {
            _createVarLists();
        }
        try {
            Variable var = new Variable(_localVarUpdates, name);
            if (expr == null) {
                // We can just forbid expr to be null.
                try {
                    var.setType(Class.forName("ptolemy.data.Token"));
                    var.setToken(new ptolemy.data.Token());
                } catch (ClassNotFoundException ex) {
                    // ignore for now
                }
            } else {
                var.setExpression(expr);
            }
        } catch (IllegalActionException ex) {
            // this should not happen
        }
    }

    public VariableList getLocalVariableUpdates() {
        return _localVarUpdates;
    }

    /** Return true if this transition is enabled.
     */
    public boolean isEnabled() {

        if (_teSet) {

System.out.println("Testing trigger event of " + this.getFullName());

            _te.evaluate();
            if (((BooleanToken)_te.getToken()).booleanValue() == false) {
                return false;
            }
        }
        if (_tcSet) {

System.out.println("Testing condition of " + this.getFullName());

            _tc.evaluate();
            if (((BooleanToken)_tc.getToken()).booleanValue() == false) {
                return false;
            }
        }
        return true;
    }

    /** Check whether we can connect the port to this transition.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        if (!(port.getContainer() instanceof SCState)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "SCTransition can only connect to SCState.");
        }
        if (numLinks() == 0) {
            return;
        }
        if (numLinks() == 2) {
            throw new IllegalActionException(this,
                    "SCTransition can only connect two SCStates.");
        }
        Enumeration ports = linkedPorts();
        Port pt = (Port)ports.nextElement();
        if (port.getName().compareTo(pt.getName()) == 0) {
            throw new IllegalActionException(this,
                    "SCTransition can only have one source/destination.");
        }
        return;
    }

}














