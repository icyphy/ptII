/* A transition in an FSMActor.

 Copyright (c) 1999 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.ComponentRelation;

import ptolemy.kernel.Port;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Transition
/**
A Transition has a source state and a destination state. A transition has
a guard expression and a trigger expression. The trigger of a transition
should imply the guard. A transition is enabled and can be taken when its
guard is true. A transition is triggered and must be taken when its trigger
is true.
<p>
A transition can contain a set of actions. The actions are executed when
the FSMActor containing the transition is fired and the transition is chosen,
or when the FSMActor is postfired and the transition is the last chosen
transition.
<p>
A transition can be preemptive or non-preemptive. When a preemptive transition
is chosen, the refinement of its source State is not fired. A non-preemptive
transition is only chosen after the refinement of its source State is fired.

@author Xiaojun Liu
@version $Id$
@see State
@see Action
@see FSMActor
*/
public class Transition extends ComponentRelation {

    /** Construct a transition with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public Transition(FSMActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Create the variables for evaluating guard and trigger.
        _guardVar = new Variable(this, "_Guard");
        _triggerVar = new Variable(this, "_Trigger");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of choice actions contained by this transition.
     *  @return A list of choice actions.
     */
    public List choiceActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }
        return _choiceActionList;
    }

    /** Return the list of commit actions contained by this transition.
     *  @return A list of commit actions.
     */
    public List commitActionList() {
        if (_actionListsVersion != workspace().getVersion()) {
            _updateActionLists();
        }
        return _commitActionList;
    }

    /** Return the destination state of this transition.
     *  @return A State.
     */
    public State destinationState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }
        return _destinationState;
    }

    /** Return the guard expression.
     *  @return The guard expression.
     */
    public String getGuardExpression() {
        return _guardVar.getExpression();
    }

    /** Return the trigger expression.
     *  @return The trigger expression.
     */
    public String getTriggerExpression() {
        return _triggerVar.getExpression();
    }

    /** Return true if the transition is enabled, that is the guard is true.
     *  @return A boolean.
     *  @exception IllegalActionException If thrown when evaluating guard.
     */
    public boolean isEnabled() throws IllegalActionException {
        Token tok = _guardVar.getToken();
        return ((BooleanToken)tok).booleanValue();
    }

    /** Return true if this transition is preemptive.
     *  @return A boolean.
     */
    public boolean isPreemptive() {
        return _preemptive;
    }

    /** Return true if the transition is triggered.
     *  @return A boolean.
     *  @exception IllegalActionException If thrown when evaluating trigger.
     */
    public boolean isTriggered() throws IllegalActionException {
        Token tok = _triggerVar.getToken();
        return ((BooleanToken)tok).booleanValue();
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of FSMActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the transition from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the transition would result
     *   in a recursive containment structure, or if
     *   this transition and container are not in the same workspace, or
     *   if the argument is not a FSMActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this transition.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof FSMActor) &&
                (container != null)) {
            throw new IllegalActionException(container, this,
                    "Transition can only be contained by instances of " +
                    "FSMActor.");
        }
        super.setContainer(container);
    }

    /** Set the guard expression.
     *  @param expression The guard expression.
     */
    public void setGuardExpression(String expression) {
        _guardVar.setExpression(expression);
    }

    /** If the argument is true, set the transition to be preemptive.
     *  Increment the version number of the workspace.
     *  @param t True to set the transition preemptive.
     */
    public void setPreemptive(boolean t) {
        _preemptive = t;
        workspace().incrVersion();
    }

    /** Set the trigger expression.
     *  @param expression The trigger expression.
     */
    public void setTriggerExpression(String expression) {
        _triggerVar.setExpression(expression);
    }

    /** Return the source state of this transition.
     *  @return A State.
     */
    public State sourceState() {
        if (_stateVersion != workspace().getVersion()) {
            _checkConnectedStates();
        }
        return _sourceState;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an IllegalActionException if the port cannot be linked
     *  to this transition. A transition has a source state and a
     *  destination state. A transition is only linked to the outgoing
     *  port of its source state and the incoming port of its destination
     *  state.
     *  @exception IllegalActionException If the port cannot be linked
     *   to this transition.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        super._checkPort(port);
        if (!(port.getContainer() instanceof State)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only connect to instances of State.");
        }
        State st = (State)port.getContainer();
        if (port != st.incomingPort && port != st.outgoingPort) {
            throw new IllegalActionException(this, port.getContainer(),
                    "Transition can only be linked to incoming or outgoing "
                    + "port of State.");
        }
        if (numLinks() == 0) {
            return;
        }
        if (numLinks() >= 2) {
            throw new IllegalActionException(this,
                    "Transition can only connect two States.");
        }
        Iterator ports = linkedPortList().iterator();
        Port pt = (Port)ports.next();
        State s = (State)pt.getContainer();
        if ((pt == s.incomingPort && port == st.incomingPort) ||
            (pt == s.outgoingPort && port == st.outgoingPort)) {
            throw new IllegalActionException(this,
                    "Transition can only have one source/destination.");
        }
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check the states connected by this transition, cache the result.
    // This method is read-synchronized on the workspace.
    private void _checkConnectedStates() {
        try {
            workspace().getReadAccess();
            Iterator ports = linkedPortList().iterator();
            _sourceState = null;
            _destinationState = null;
            while (ports.hasNext()) {
                Port p = (Port)ports.next();
                State s = (State)p.getContainer();
                if (p == s.incomingPort) {
                    _destinationState = s;
                } else {
                    _sourceState = s;
                }
            }
            _stateVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    // Update the cached lists of actions.
    // This method is read-synchronized on the workspace.
    private void _updateActionLists() {
        try {
            workspace().getReadAccess();
            _choiceActionList.clear();
            _commitActionList.clear();
            Iterator actions = attributeList(Action.class).iterator();
            while (actions.hasNext()) {
                Action action = (Action)actions.next();
                if (action instanceof ChoiceAction) {
                    _choiceActionList.add(action);
                }
                if (action instanceof CommitAction) {
                    _commitActionList.add(action);
                }
            }
            _actionListsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Version of cached lists of actions.
    private long _actionListsVersion = -1;

    // Cached list of choice actions contained by this transition.
    private List _choiceActionList = new LinkedList();

    // Cached list of commit actions contained by this Transition.
    private List _commitActionList = new LinkedList();

    // Cached destination state of this transition.
    private State _destinationState = null;

    // Variable for evaluating guard.
    private Variable _guardVar = null;

    // Set to true when the transition is preemptive.
    private boolean _preemptive = false;

    // Cached source state of this transition.
    private State _sourceState = null;

    // Version of cached source/destination state.
    private long _stateVersion = -1;

    // Variable for evaluating trigger.
    private Variable _triggerVar = null;

}
