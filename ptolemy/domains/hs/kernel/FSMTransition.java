/* A FSMTransition connects two FSMStates.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.hs.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.data.expr.Variable;
import ptolemy.domains.hs.kernel.util.VariableList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// FSMTransition
/**
A FSMTransition connects two FSMStates. It has a trigger event, a trigger
condition, and a set of trigger actions.

@author Xiaojun Liu
@version $Id$
*/
public class FSMTransition extends ComponentRelation {

    public void addTransitionAction(TransitionAction act) {
        if (act == null) {
            return;
        }
        if (_transActions == null) {
            _transActions = new LinkedList();
        }
        _transActions.addFirst(act);
    }

    public void executeTransitionActions() throws IllegalActionException {
        if (_transActions == null) {
            return;
        }
        Iterator tas = _transActions.iterator();
        while (tas.hasNext()) {
            TransitionAction ta = (TransitionAction)tas.next();
            ta.execute();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected void _createVarLists() {
        try {
            _trigger = new VariableList(this, "Trigger");
            _trigger.setRespondToChange(false);
            _te = new Variable(_trigger, "TriggerEvent");
            _tc = new Variable(_trigger, "TriggerCondition");
            _actions = new VariableList(this, "Actions");
            _actions.setRespondToChange(false);
            _localVarUpdates = new VariableList(this, "LocalVarUpdates");
            _localVarUpdates.setRespondToChange(false);

        } catch (Exception ex) {
            // FIXME: This should not be happening
            throw new InvalidStateException(ex.getMessage());
        }
    }

    /** Construct a transition in the default workspace with an empty string
     *  as its name. Add the transition to the directory of the workspace.
     */
    public FSMTransition() {
        super();
    }

    /** Construct a transition in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the transition to the workspace directory.
     *  @param workspace The workspace that will list the transition.
     */
    public FSMTransition(Workspace workspace) {
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
    public FSMTransition(CompositeEntity container, String name)
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
        FSMTransition newobj = (FSMTransition)super.clone(ws);
        newobj._stateVersion = -1;
        if (_trigger != null) {
            newobj._trigger = (VariableList)newobj.getAttribute("Trigger");
            newobj._te =
                (Variable)newobj._trigger.getAttribute("TriggerEvent");
            newobj._tc =
                (Variable)newobj._trigger.getAttribute("TriggerCondition");
            newobj._actions =
                (VariableList)newobj.getAttribute("Actions");
            newobj._localVarUpdates =
                (VariableList)newobj.getAttribute("LocalVarUpdates");
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
    public FSMState sourceState() {
        if (_stateVersion == workspace().getVersion()) {
            return _source;
        }
        _getStates();
        return _source;
    }

    /** Return the destination state of this transition.
     */
    public FSMState destinationState() {
        if (_stateVersion == workspace().getVersion()) {
            return _dest;
        }
        _getStates();
        return _dest;
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
    public void setupScope()
            throws NameDuplicationException, IllegalActionException {
        FSMState src = sourceState();
        VariableList localInputVarS =
            (VariableList)src.getAttribute(
                    FSMState.LOCAL_INPUT_STATUS_VAR_LIST);
        VariableList localInputVarV =
            (VariableList)src.getAttribute(
                    FSMState.LOCAL_INPUT_VALUE_VAR_LIST);


        VariableList inputVarS = null;
        VariableList inputVarV = null;
        VariableList localVars = null;
        FSMController ctrl = (FSMController)getContainer();
        if (ctrl != null) {
            inputVarS = (VariableList)ctrl.getAttribute(
                    FSMController.INPUT_STATUS_VAR_LIST);
            inputVarV = (VariableList)ctrl.getAttribute(
                    FSMController.INPUT_VALUE_VAR_LIST);
            localVars = (VariableList)ctrl.getAttribute(
                    FSMController.LOCAL_VARIABLE_LIST);
        }

        if (inputVarS != null) {
            if (_te == null) {
                _createVarLists();
            }
            _te.addToScope(inputVarS.getVariables());
            _tc.addToScope(inputVarV.getVariables());

        }
        if (localVars != null) {
            _te.addToScope(localVars.getVariables());
            _tc.addToScope(localVars.getVariables());
        }
        if (!isPreemptive() && localInputVarS != null) {
            _te.addToScope(localInputVarS.getVariables());
            _tc.addToScope(localInputVarV.getVariables());
        }
        Enumeration actionVariables = _actions.getVariables();
        Variable act;
        while (actionVariables.hasMoreElements()) {
            act = (Variable)actionVariables.nextElement();
            if (inputVarV != null) {
                act.addToScope(inputVarV.getVariables());
            }
            if (localVars != null) {
                act.addToScope(localVars.getVariables());
            }
            if (!isPreemptive() && localInputVarV != null) {
                act.addToScope(localInputVarV.getVariables());
            }
        }
        Enumeration varUpdates = _localVarUpdates.getVariables();
        Variable update;
        while (varUpdates.hasMoreElements()) {
            update = (Variable)varUpdates.nextElement();
            if (inputVarV != null) {
                update.addToScope(inputVarV.getVariables());
            }
            if (localVars != null) {
                update.addToScope(localVars.getVariables());
            }
            if (!isPreemptive() && localInputVarV != null) {
                update.addToScope(localInputVarV.getVariables());
            }
        }

        // initialize the transition actions
        if (_transActions != null) {
            Iterator tas = _transActions.iterator();
            while (tas.hasNext()) {
                TransitionAction ta = (TransitionAction)tas.next();
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
                var.setTypeEquals(BaseType.GENERAL);
                var.setToken(new ptolemy.data.Token());
            } else {
                var.setExpression(expr);
            }
        } catch (IllegalActionException ex) {
            // this should not happen
            throw new InternalErrorException(ex.getMessage());
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
                var.setTypeEquals(BaseType.GENERAL);
                var.setToken(new ptolemy.data.Token());
            } else {
                var.setExpression(expr);
            }
        } catch (IllegalActionException ex) {
            // this should not happen
            throw new InternalErrorException(ex.getMessage());
        }
    }

    public VariableList getLocalVariableUpdates() {
        return _localVarUpdates;
    }

    /** Return true if this transition is enabled.
     */
    public boolean isEnabled() throws IllegalActionException {

        if (_teSet) {

            //System.out.println("Testing trigger event of " +
            //     this.getFullName());

            _te.getToken();
            if (((BooleanToken)_te.getToken()).booleanValue() == false) {
                return false;
            }
        }
        if (_tcSet) {

            //System.out.println("Testing condition of " + this.getFullName());

            _tc.getToken();
            if (((BooleanToken)_tc.getToken()).booleanValue() == false) {
                return false;
            }
        }
        return true;
    }

    /** Check whether we can connect the port to this transition.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        if (!(port.getContainer() instanceof FSMState)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "FSMTransition can only connect to FSMState.");
        }
        if (numLinks() == 0) {
            return;
        }
        if (numLinks() == 2) {
            throw new IllegalActionException(this,
                    "FSMTransition can only connect two FSMStates.");
        }
        Enumeration ports = linkedPorts();
        Port pt = (Port)ports.nextElement();
        if (port.getName().compareTo(pt.getName()) == 0) {
            throw new IllegalActionException(this,
                    "FSMTransition can only have one source/destination.");
        }
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** @serial List trigger event and trigger condition. */
    protected VariableList _trigger = null;

    /** @serial Trigger event variable. */
    protected Variable _te;

    /** @serial Trigger condition variable. */
    protected Variable _tc;

    /** @serial List trigger actions. */
    protected VariableList _actions = null;

    /** @serial List local variable updates. */
    protected VariableList _localVarUpdates = null;

    /** @serial If true, this transition is preemptive. */
    protected boolean _preemptive = false;

    /** @serial If true, the destination state's subsystem will be initialized
     * each time this transition is taken.
     */
    protected boolean _initEntry = false;

    /** @serial Source state. */
    protected FSMState _source = null;

    /** @serial Destination state. */
    protected FSMState _dest = null;

    /** @serial Version of source/dest states. */
    protected long _stateVersion = -1;

    /** @serial Trigger event set. */
    protected boolean _teSet = false;

    /** @serial Trigger condition set. */
    protected boolean _tcSet = false;

    /** @serial List of transition actions. */
    protected LinkedList _transActions = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

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
                if (p.getName().compareTo(FSMState.INCOMING_PORT) == 0) {
                    _dest = (FSMState)p.getContainer();
                }
                if (p.getName().compareTo(FSMState.OUTGOING_PORT) == 0) {
                    _source = (FSMState)p.getContainer();
                }
            }
            _stateVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }
}
