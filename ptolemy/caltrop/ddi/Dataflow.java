/*
 @Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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



 */
package ptolemy.caltrop.ddi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.ddi.util.DataflowActorInterpreter;
import ptolemy.kernel.util.IllegalActionException;
import caltrop.interpreter.Context;
import caltrop.interpreter.InputPort;
import caltrop.interpreter.SingleInputPort;
import caltrop.interpreter.SingleOutputPort;
import caltrop.interpreter.ast.Action;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.PortDecl;
import caltrop.interpreter.ast.QID;
import caltrop.interpreter.ast.Transition;
import caltrop.interpreter.environment.Environment;
import caltrop.interpreter.util.PriorityUtil;

//////////////////////////////////////////////////////////////////////////
//// Dataflow

/**
 @author J&#246;rn W. Janneck, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Dataflow extends AbstractDDI {
    public Dataflow(TypedAtomicActor ptActor, Actor actor, Context context,
            Environment env) {
        _ptActor = ptActor;
        _actor = actor;
        _actions = PriorityUtil.prioritySortActions(_actor);
        _context = context;
        _env = env;
        _inputPorts = createPortMap(_actor.getInputPorts(), true);
        _outputPorts = createPortMap(_actor.getOutputPorts(), false);
        _actorInterpreter = new DataflowActorInterpreter(_actor, _context,
                _env, _inputPorts, _outputPorts);
    }

    private Map createPortMap(PortDecl[] ports, boolean isInput) {
        Map portMap = new HashMap();

        for (PortDecl port2 : ports) {
            String name = port2.getName();
            TypedIOPort port = (TypedIOPort) _ptActor.getPort(name);

            if (isInput) {
                portMap.put(name, new SingleInputPort(name, new DFInputChannel(
                        port, 0)));
            } else {
                portMap.put(name, new SingleOutputPort(name,
                        new DFOutputChannel(port, 0)));
            }
        }

        return portMap;
    }

    protected TypedAtomicActor _ptActor;

    protected Actor _actor;

    protected Action[] _actions;

    protected Context _context;

    protected Environment _env;

    protected Set _currentStateSet;

    protected Transition[] _currentTransitions;

    protected DataflowActorInterpreter _actorInterpreter;

    protected Map _inputPorts;

    protected Map _outputPorts;

    protected Action _lastFiredAction;

    @Override
    public boolean isLegalActor() {
        return PriorityUtil.isValidPriorityOrder(_actor);
    }

    @Override
    public void setupActor() {
    }

    @Override
    public String getName() {
        return "Default";
    }

    /**
     * Executes the selected action on the first {@link #fire()
     * fire()} call. On successive calls, it rolls back previous state
     * changes, selects a new action and executes it.
     * <p>
     *  <b>Note: Is this correct behavior? What is the contract
     * between the result of prefire() and successive calls to
     * fire()?</b>
     *
     * @exception IllegalActionException If an error occurs during the
     * interpretation of the action.

     */
    @Override
    public void fire() throws IllegalActionException {
        // Don't call super.fire(); here, super.fire() is abstract.;
        // FIXMELATER: state transition and potentially rollback
        try {
            if (_actorInterpreter.currentAction() == null) {
                // This point is reached iff this is not the first
                // fire() call of this iteration.
                // Hence we could put rollback work here.
                _selectAction();
            }

            if (_actorInterpreter.currentAction() != null) {
                _lastFiredAction = _actorInterpreter.currentAction();
                _actorInterpreter.actionStep();
                _actorInterpreter.actionComputeOutputs();
                _actorInterpreter.actionClear(); // sets .currentAction() to null
            }
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Could not fire CAL actor '" + _actor.getName() + "'");
        }
    }

    /** Preinitialize this actor.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
    }

    /**
     * This method picks an action for which the actor interpreter
     * evaluates the guard to true. Note that this does not
     * necessarily mean that <em>all</em> preconditions for firing are
     * satisfied---the amount of "prechecking" depends on the model of
     * computation ddi. (FIXMELATER)
     *
     * @return The action number that was selected, a value <0 if no
     * action was selected.
     */
    protected int _selectAction() {
        _rollbackInputChannels();

        for (int i = 0; i < _actions.length; i++) {
            if (this.isEligibleAction(_actions[i])) {
                // Note: could we perhaps reuse environment?
                _rollbackInputChannels();
                _actorInterpreter.actionSetup(_actions[i]);

                if (_actorInterpreter.actionEvaluatePrecondition()) {
                    return i;
                } else {
                    _actorInterpreter.actionClear();
                }
            }
        }

        _rollbackInputChannels();
        return -1;
    }

    protected int _selectInitializer() {
        Action[] actions = _actor.getInitializers();

        for (int i = 0; i < actions.length; i++) {
            // Note: could we perhaps reuse environment?
            _rollbackInputChannels();
            _actorInterpreter.actionSetup(actions[i]);

            if (_actorInterpreter.actionEvaluatePrecondition()) {
                return i;
            } else {
                _actorInterpreter.actionClear();
            }
        }

        return -1;
    }

    /**
     * In SDF, selecting which initializer to fire is already done in
     * preinitialize().
     * @exception IllegalActionException
     */
    @Override
    public void initialize() throws IllegalActionException {
        if (_actor.getScheduleFSM() == null) {
            _currentStateSet = null;
            _currentTransitions = new Transition[0];
        } else {
            _currentStateSet = Collections.singleton(_actor.getScheduleFSM()
                    .getInitialState());
            _computeNextTransitions();
        }

        try {
            _selectInitializer();
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Error during initializer selection in actor '"
                            + _actor.getName() + "'");
        }

        try {
            if (_actorInterpreter.currentAction() != null) {
                _actorInterpreter.actionStep();
                _actorInterpreter.actionComputeOutputs();
                _actorInterpreter.actionClear();
            }
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Could not fire initializer in CAL actor '"
                            + _actor.getName() + "'");
        }
    }

    private void _commitInputChannels() {
        for (Iterator iterator = _inputPorts.values().iterator(); iterator
                .hasNext();) {
            InputPort inputPort = (InputPort) iterator.next();

            for (int i = 0; i < inputPort.width(); i++) {
                DFInputChannel c = (DFInputChannel) inputPort.getChannel(i);
                c.commit();
            }
        }
    }

    private void _rollbackInputChannels() {
        for (Iterator iterator = _inputPorts.values().iterator(); iterator
                .hasNext();) {
            InputPort inputPort = (InputPort) iterator.next();

            for (int i = 0; i < inputPort.width(); i++) {
                DFInputChannel c = (DFInputChannel) inputPort.getChannel(i);
                c.rollback();
            }
        }
    }

    /**
     * Postfire this actor.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        computeNextStateSet(_lastFiredAction);
        _commitInputChannels();
        _lastFiredAction = null;
        return true;
    }

    /**
     * Select a fireable action among the actions of the actor, if possible.
     *
     * @return True, if an action could be selected.
     * @exception IllegalActionException If an error occurred during the
     * action selection.
     *
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        _lastFiredAction = null;

        try {
            _selectAction();

            //            if (_actorInterpreter.currentAction() != null)
            //                return true;
            //            else
            //                return _ptActor.superPrefire();
            return true;
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Error during action selection in actor '"
                            + _actor.getName() + "'");
        }
    }

    private boolean isEligibleAction(Action a) {
        QID tag = a.getTag();

        if (tag != null && _currentStateSet != null) {
            int length = _currentTransitions.length;

            for (int i = 0; i < length; i++) {
                Transition t = _currentTransitions[i];

                if (isPrefixedByTagList(tag, t.getActionTags())) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }

    private void computeNextStateSet(Action a) {
        if (_currentStateSet == null) {
            // No change
            return;
        }

        if (a == null || a.getTag() == null) {
            // No change
            return;
        }

        // The new state set.
        Set ns = new HashSet();
        QID tag = a.getTag();

        int length = _currentTransitions.length;

        for (int i = 0; i < length; i++) {
            Transition t = _currentTransitions[i];

            if (isPrefixedByTagList(tag, t.getActionTags())) {
                ns.add(t.getDestinationState());
            }
        }

        _currentStateSet = ns;

        _computeNextTransitions();
    }

    private void _computeNextTransitions() {
        // The set of transitions that we can take in the current state.
        ArrayList nt = new ArrayList();
        Transition[] ts = _actor.getScheduleFSM().getTransitions();

        for (Transition t : ts) {
            if (_currentStateSet.contains(t.getSourceState())) {
                nt.add(t);
            }
        }

        _currentTransitions = (Transition[]) nt.toArray(new Transition[nt
                .size()]);
    }

    private static boolean isPrefixedByTagList(QID tag, QID[] tags) {
        for (QID tag2 : tags) {
            if (tag2.isPrefixOf(tag)) {
                return true;
            }
        }

        return false;
    }
}
