/*
@Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.tdl.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.ModalDirector;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.domains.modal.modal.RefinementPort;
import ptolemy.graph.Node;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * Director for a TDL (= Timing Definition Language) module. A TDL module
 * consists of modes, modes consist of TDL tasks and TDL tasks are SDF actors.
 * All actions inside a TDL module are executed periodically and the timing
 * information is specified in parameters. This director parses the parameters
 * and builds a schedule for all the TDL actions. The schedule is represented in
 * a graph showing the dependencies between the TDL actions (see
 * TDLActionsGraph).
 * <p>
 * In the initialization, output ports and actuators are initialized with values
 * specified in the parameters of the ports. The schedule is generated and
 * events are scheduled. Events that are safe to process at current model time
 * are executed, then the fireAt(time) of the enclosing director is called with
 * the time stamp of the next event. Events are processed in the order specified
 * in the graph.
 *
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
 */
public class TDLModuleDirector extends ModalDirector {

    /**
     * Construct a director in the given container with the given name. The
     * container argument must not be null, or a NullPointerException will be
     * thrown. If the name argument is null, then the name is set to the empty
     * string. Increment the version number of the workspace.
     *
     * @param container
     *            Container of this director.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the name has a period in it, or the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public TDLModuleDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Select all actions that can be fired at the current model time. After
     * executing an action, schedule actions that are executed next according to the schedule. If an
     * action with a WCET > 0 was started, schedule a refiring and return.
     */
    @Override
    public void fire() throws IllegalActionException {
        boolean iterate = true;
        Time modePeriod = getModePeriod(getController().currentState());
        Time scheduleTime = new Time(this, getModelTime().getLongValue()
                % modePeriod.getLongValue());
        while (iterate) {
            iterate = false;
            System.out.println(_nextEventsTimeStamps.size() + " "
                    + _nodesDependentOnPreviousActions.size());
            _currentWCET = 0;

            List<Node> eventsToFire = _getEventsToFire(scheduleTime, modePeriod);
            boolean doneAction;
            for (Node node : eventsToFire) {
                System.out.println("  TDL: " + getModelTime() + " " + node);
                doneAction = true;
                TDLAction action = (TDLAction) node.getWeight();
                Object obj = action.object;
                if (!_hasGuard((NamedObj) obj) || _guardIsTrue((NamedObj) obj)) {
                    // execute task
                    if (action.actionType == TDLAction.EXECUTETASK) {
                        Actor actor = (Actor) obj;
                        actor.prefire(); // will never return false, receivers ensure inputs availability
                        actor.iterate(1);
                        actor.postfire();
                        _currentWCET = getWCETParameter(actor);

                    } else if (action.actionType == TDLAction.WRITEACTUATOR) {
                        _updateActuator((IOPort) obj);
                    } else if (action.actionType == TDLAction.WRITEOUTPUT) {
                        _updateOutputPort((IOPort) obj);
                        _currentWCET = 0.0;
                    } else if (action.actionType == TDLAction.READSENSOR) {
                        // before reading a sensor, return control to the outside to see if other actors want to fire
                        doneAction = ((TDLActor) getController())
                                .inputIsSafeToProcess((IOPort) action.object);
                        ((TDLActor) getController()).readInput(node,
                                (IOPort) action.object,
                                modePeriod.getLongValue());
                        if (doneAction) {
                            scheduleEventsAfterAction(node);
                        }
                    } else if (action.actionType == TDLAction.READINPUT) {
                        _updateInputPort((IOPort) obj);
                    } else if (action.actionType == TDLAction.MODESWITCH) {
                        State targetState;
                        if (!_chooseTransition((Transition) obj)) {
                            targetState = getController().currentState();
                        } else {
                            targetState = ((Transition) obj).destinationState(); // choose transition in the graph
                            iterate = true;
                        }
                        _nextEventsTimeStamps.clear();
                        _nodesDependentOnPreviousActions.clear();
                        Node startNode = _graph.getNode(new TDLAction(new Time(
                                this, 0.0), TDLAction.AFTERMODESWITCH,
                                targetState));

                        _fireAt(startNode, getModelTime());
                        scheduleEventsAfterAction(startNode);
                    } else if (action.actionType == TDLAction.AFTERMODESWITCH) {
                        // nothing to do
                    } else {
                        throw new IllegalArgumentException(obj
                                + " cannot be executed.");
                    }
                }
                if (doneAction) {
                    if (_nextEventsTimeStamps.size() == 1
                            && _nodesDependentOnPreviousActions.size() == 0) {
                        _fireAt(node, getModelTime());
                        scheduleEventsAfterAction(node);
                        iterate = true;
                    }
                    _nextEventsTimeStamps.remove(node);

                    if (_nodesDependentOnPreviousActions != null
                            && _nodesDependentOnPreviousActions.size() != 0) {
                        Set<Node> s = new HashSet();
                        s.addAll(_nodesDependentOnPreviousActions.keySet());
                        for (Node n : s) {
                            List<TDLAction> actionsToRemove = new ArrayList();

                            if (_nodesDependentOnPreviousActions.get(n) != null
                                    && _nodesDependentOnPreviousActions.size() > 0) {
                                for (TDLAction waitForAction : _nodesDependentOnPreviousActions
                                        .get(n)) {
                                    if (waitForAction.sameActionAs(action,
                                            modePeriod)) {
                                        actionsToRemove.add(waitForAction);
                                    }
                                }
                                for (TDLAction actionToRemove : actionsToRemove) {
                                    _nodesDependentOnPreviousActions.get(n)
                                    .remove(actionToRemove);
                                }
                            }
                            if (_nodesDependentOnPreviousActions.get(n) == null
                                    || _nodesDependentOnPreviousActions.get(n)
                                    .size() == 0) {
                                if (!_nextEventsTimeStamps.keySet().contains(n)) {
                                    _fireAt(n, getModelTime());
                                }
                                scheduleEventsAfterAction(n);
                                _previousAdditionalScheduleTime = new Time(
                                        this, 0.0);
                                //nodesToRemove.add(n);
                                //iterate = true;
                            }
                        }
                    }
                }
                if (!doneAction) {
                    _fireAt(null, getModelTime());
                    //return;
                }
                if (_currentWCET > 0.0) {
                    _fireAt(null, _getSmallestTimeStampInEventsToFire());
                    return;
                }
            }
        }
    }

    /**
     * Get mode period from state parameter "period".
     *
     * @param obj
     *            The object
     * @return The value of the "period" parameter. If there is no period
     *         parameter or it cannot be converted to a double, then return 1.0.
     */
    public Time getModePeriod(NamedObj obj) {
        try {
            Parameter parameter = (Parameter) obj.getAttribute("period");

            if (parameter != null) {
                DoubleToken token = (DoubleToken) parameter.getToken();

                return new Time(this, token.doubleValue());
            } else {
                return null;
            }
        } catch (ClassCastException ex) {
            return null;
        } catch (IllegalActionException ex) {
            return null;
        }
    }

    /**
     * Return the current model time which is the model time of the executive
     * director.
     * @return the model time of the executive directory
     */
    @Override
    public Time getModelTime() {
        // We don't call super.getModelTime() because we want to get
        // the model time of the exective director.
        return ((Actor) this.getContainer()).getExecutiveDirector()
                .getModelTime();
    }

    /**
     * Return the worst case execution time of the actor or 0 if no worst case
     * execution time was specified.
     *
     * @param actor
     *            The actor for which the worst case execution time is
     *            requested.
     * @return The worst case execution time.
     */
    public static double getWCETParameter(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("WCET");

            if (parameter != null) {
                DoubleToken token = (DoubleToken) parameter.getToken();

                return token.doubleValue();
            } else {
                return 0.0;
            }
        } catch (ClassCastException ex) {
            return 0.0;
        } catch (IllegalActionException ex) {
            return 0.0;
        }
    }

    /**
     * Return the worst case execution time of the actor or 0 if no worst case
     * execution time was specified.
     *
     * @return The worst case execution time.
     * @exception IllegalActionException
     */
    public double getWCET() throws IllegalActionException {
        if (_currentWCET > 0) {
            return _currentWCET;
        } else {

            return 0.0;
        }
    }

    /**
     * Initialize the director, calculate schedule and schedule first firing.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _resetReceivers();
        _graph = new TDLActionsGraph((TDLModule) getContainer(),
                getController());
        _graph.buildGraph(getController().currentState());
        _initializeOutputPorts();
        fireAt((TDLModule) getContainer(), getModelTime());
        _nodesDependentOnPreviousActions = new HashMap();
        _nextEventsTimeStamps = new HashMap();
        //nextEvents.add(new TDLAction(0, TDLAction.AFTERMODESWITCH, getController().currentState()));

        Node startNode = _graph.getNode(new TDLAction(new Time(this, 0.0),
                TDLAction.AFTERMODESWITCH, getController().getInitialState()));
        _nextEventsTimeStamps.put(startNode, new Time(this, 0.0));
        _fireAt(startNode, new Time(this, 0));
        scheduleEventsAfterAction(startNode);
        //nextJoinNodes.put(_graph.getNode(new TDLAction(0, TDLAction.AFTERMODESWITCH, getController().getInitialState())), null);
    }

    /**
     * Find out if task (=actor) or actuator (=output port) is fast task.
     *
     * @param obj
     *            The object that could be a fast task or actuator.
     * @return True if it is a fast task.
     */
    public static boolean isFast(NamedObj obj) {
        try {
            Parameter parameter = (Parameter) obj.getAttribute("fast");

            if (parameter != null) {
                BooleanToken intToken = (BooleanToken) parameter.getToken();
                return intToken.booleanValue();
            } else {
                return false;
            }
        } catch (ClassCastException ex) {
            return false;
        } catch (IllegalActionException ex) {
            return false;
        }
    }

    /** Return a causality interface for the composite actor that
     *  contains this director. This class returns an
     *  instance of {@link TDLCausalityInterface}.
     *  @return A representation of the dependencies between input ports
     *   and output ports of the container.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        return new TDLCausalityInterface((Actor) getContainer(),
                defaultDependency());
    }

    /**
     * Get frequency of the task.
     *
     * @param obj
     *            The object that could be a fast task or actuator.
     * @return True if it is a fast task.
     */
    public static int getFrequency(NamedObj obj) {
        try {
            Parameter parameter = (Parameter) obj.getAttribute("frequency");

            if (parameter != null) {
                IntToken token = (IntToken) parameter.getToken();
                return token.intValue();
            } else {
                return 1;
            }
        } catch (ClassCastException ex) {
            return 1;
        } catch (IllegalActionException ex) {
            return 1;
        }
    }

    /**
     * Get frequency of the task.
     *
     * @param obj
     *            The object that could be a fast task or actuator.
     * @return True if it is a fast task.
     */
    public static String getSlots(NamedObj obj) {
        try {
            Parameter parameter = (Parameter) obj.getAttribute("slots");

            if (parameter != null) {
                StringToken token = (StringToken) parameter.getToken();
                return token.stringValue();
            } else {
                return "'1*'";
            }
        } catch (ClassCastException ex) {
            return "'1*'";
        } catch (IllegalActionException ex) {
            return "'1*'";
        }
    }

    /**
     * Return a new TDLReceiver.
     *
     * @return A new TDL receiver.
     */
    @Override
    public Receiver newReceiver() {
        Receiver receiver = new TDLReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /**
     * Check if at the current time there is something to do.
     *
     * @return True if there is something to do now.
     * @exception IllegalActionException
     *             Thrown if execution was missed, input ports could not be
     *             transferred or by parent class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // read inputs although they are not used to avoid piling up tokens
        //

        for (Iterator it = ((TDLModule) getContainer()).inputPortList()
                .iterator(); it.hasNext();) {
            IOPort port = (IOPort) it.next();
            if (_debugging) {
                _debug("Calling transferInputs on port: " + port.getFullName());
            }
            if (!port.isInput() || !port.isOpaque()) {
                throw new IllegalActionException(this, port,
                        "Attempted to transferInputs on a port is not an opaque"
                                + "input port.");
            }
            for (int i = 0; i < port.getWidth(); i++) {
                try {
                    if (i < port.getWidthInside()) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);
                            if (_debugging) {
                                _debug(getName(), "transferring input from "
                                        + port.getName());
                            }
                            port.sendInside(i, t);
                        }
                    } else {
                        // No inside connection to transfer tokens to.
                        // In this case, consume one input token if there is one.
                        if (_debugging) {
                            _debug(getName(), "Dropping single input from "
                                    + port.getName());
                        }
                        if (port.hasToken(i)) {
                            port.get(i);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }

        }

        if (((Actor) getContainer().getContainer()).getDirector() instanceof DEDirector) {
            return super.prefire();
        }

        Time modePeriod = getModePeriod(getController().currentState());
        Time scheduleTime = new Time(this, getModelTime().getLongValue()
                % modePeriod.getLongValue());
        boolean fire = false;
        List<Node> eventsToFire = _getEventsToFire(scheduleTime, modePeriod);
        for (Node node : eventsToFire) {
            TDLAction action = (TDLAction) node.getWeight();
            if (action.actionType == TDLAction.READSENSOR) {
                if (((TDLActor) getController())
                        .inputIsSafeToProcess((IOPort) action.object)) {
                    fire = true;
                }
            } else {
                fire = true;
            }
        }
        return fire;
    }

    /**
     * Schedules actions which depend on the action specified in the given node.
     *
     * @param node
     *            Given node.
     * @exception IllegalActionException
     *             Not thrown here but in the base class.
     */
    public void scheduleEventsAfterAction(Node node)
            throws IllegalActionException {
        _nodesDependentOnPreviousActions.remove(node);
        List<Node> events = _graph.getEventsFollowingAction(node);
        HashMap<Node, List<TDLAction>> table = _graph.getNextJoinNodes(node,
                node, new ArrayList());
        for (Node n : table.keySet()) {
            if (!_nodesDependentOnPreviousActions.keySet().contains(n)) {
                _nodesDependentOnPreviousActions.put(n, table.get(n));
            }
        }
        for (Node n : events) {
            _fireAt(n, getModelTime());
        }
    }

    /**
     * Outputs are only transferred when scheduled, therefore do nothing if
     * transfer outputs is called by another actor.
     *
     * @param port
     *            output port.
     * @return True.
     */
    @Override
    public boolean transferOutputs(IOPort port) {
        return true;
    }

    /**
     * Don't read inputs as this is specifically scheduled by a TDLModule.
     *
     * @param port
     *            Input port.
     * @return True if ports transferred inputs.
     * @exception IllegalActionException
     *             Thrown if inputs are about to be transferred for a non opaque
     *             input port.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        return true;
    }

    /**
     * Clear private variables and lists.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_nextEventsTimeStamps != null) {
            _nextEventsTimeStamps.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Check if transition (=mode switch) should be executed.
     *
     * @param transition
     *            The mode switch.
     * @return True if the mode switch should be done.
     * @exception IllegalActionException
     *             If an error occurs during the transition.
     */
    private boolean _chooseTransition(Transition transition)
            throws IllegalActionException {
        _updateInputs();
        try {
            if (transition.isEnabled()) {
                System.out.println(transition.getGuardExpression());
                getController().setLastChosenTransition(transition);
                _transferTaskInputs(transition);
                fireAt((TDLModule) getContainer(), getModelTime());
                return true;
            }
        } catch (UndefinedConstantOrIdentifierException ex) { // can happen if no input tokens are available
            // nothing to do.
        }
        return false;
    }

    /**
     * Schedule a refiring of this actor for a TDL action.
     *
     * @param node
     *            Node containing a TDL action that is scheduled to execute at
     *            the given time.
     * @param additionalTime
     *            Time to be added to the current model time and the schedule
     *            time of the TDL action node.
     * @exception IllegalActionException
     *             Thrown if fireAt() returns false.
     */
    private void _fireAt(Node node, Time additionalTime)
            throws IllegalActionException {
        Time modePeriod = getModePeriod(getController().currentState());
        Time scheduleTime = new Time(this, getModelTime().getLongValue()
                % modePeriod.getLongValue());
        Time time;
        if (node != null) {
            Time t = ((TDLAction) node.getWeight()).time.subtract(scheduleTime);
            if (t.getDoubleValue() > 0.0) {
                _previousAdditionalScheduleTime = t;
            }

            time = ((TDLAction) node.getWeight()).time.subtract(scheduleTime)
                    .add(additionalTime);
            if (((TDLAction) node.getWeight()).time.getDoubleValue() == 0.0
                    && _previousAdditionalScheduleTime.getDoubleValue() > 0.0) {
                time = time.add(modePeriod);
            }

        } else {
            time = additionalTime;
        }
        if (node != null) {

            _nextEventsTimeStamps.put(node, time);

        }
        System.out.println("fireAt " + time + " " + node);
        super.fireAt((Actor) getContainer(), time);
    }

    /**
     * Get all tasks for a module.
     *
     * @return A list of all tasks.
     */
    private Collection _getAllTasks() {
        Collection tasks = new ArrayList();
        Iterator it = ((TDLModule) getContainer()).entityList().iterator();
        while (it.hasNext()) {
            Object object = it.next();
            if (object instanceof Refinement) {
                Refinement refinement = (Refinement) object;
                Iterator entIt = refinement.entityList().iterator();
                while (entIt.hasNext()) {
                    Actor task = (Actor) entIt.next();
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    /**
     * Return events that can be fired at current model time.
     *
     * @param scheduleTime
     *            Time in the schedule (= between 0 and modePeriod) which is
     * @param modePeriod
     *            Period of the current mode.
     * @return List of events that can be fired at current model time.
     */
    private List<Node> _getEventsToFire(Time scheduleTime, Time modePeriod) {
        List<Node> eventsToFire = new ArrayList();
        for (Node node : _nextEventsTimeStamps.keySet()) {
            if (_nextEventsTimeStamps.get(node).equals(getModelTime())) {
                eventsToFire.add(node);
            }
        }
        return eventsToFire;
    }

    /**
     * Returns the smallest time stamp of the events that will be fired next.
     *
     * @return The smallest time stamp of the next events.
     */
    private Time _getSmallestTimeStampInEventsToFire() {
        Time time = Time.POSITIVE_INFINITY;
        for (Node node : _nextEventsTimeStamps.keySet()) {
            Time t = _nextEventsTimeStamps.get(node);
            if (t.compareTo(time) < 0) {
                time = t;
            }
        }
        return time;
    }

    /**
     * Test a guard expression on an actor.
     *
     * @param obj
     *            The object containing a guard expression.
     * @return True if the guard expression evaluates to true.
     * @exception IllegalActionException
     *             Thrown if guard expression could not be read.
     */
    private boolean _guardIsTrue(NamedObj obj) throws IllegalActionException {
        _updateInputs();

        Parameter parameter = (Parameter) obj.getAttribute("guard");
        StringToken token = (StringToken) parameter.getToken();
        ParseTreeEvaluator parseTreeEvaluator = getParseTreeEvaluator();
        FSMActor fsmActor = getController();
        ASTPtRootNode _guardParseTree = null;
        String expr = token.stringValue();

        // Parse the guard expression.
        PtParser parser = new PtParser();
        try {
            _guardParseTree = parser.generateParseTree(expr);
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to parse guard expression \"" + expr + "\"");
        }
        Token tok = parseTreeEvaluator.evaluateParseTree(_guardParseTree,
                fsmActor.getPortScope());
        return ((BooleanToken) tok).booleanValue();
    }

    /**
     * Test if an object has a guard expression.
     *
     * @param obj
     *            Object that might have a guard expression.
     * @return True if the object has a guard parameter.
     */
    private boolean _hasGuard(NamedObj obj) {
        Parameter parameter = (Parameter) obj.getAttribute("guard");
        return parameter != null;
    }

    /**
     * Initialize a port with an initial token.
     *
     * @param port
     *            Port to be initialized.
     * @exception IllegalActionException
     *             Thrown if the initial value parameter could not be read.
     */
    private void _initializePort(IOPort port) throws IllegalActionException {
        Parameter initialValueParameter = (Parameter) ((NamedObj) port)
                .getAttribute("initialValue");
        Token token;
        if (initialValueParameter != null) {
            token = initialValueParameter.getToken();
        } else {
            token = new IntToken(0);
        }
        Receiver[][] channelArray = port.getRemoteReceivers();
        for (Receiver[] receiverArray : channelArray) {
            for (Receiver element : receiverArray) {
                TDLReceiver receiver = (TDLReceiver) element;
                receiver.init(token);
            }
        }
    }

    /**
     * Initialize output ports by reading initial value and initializing the
     * receivers.
     *
     * @exception IllegalActionException
     *             Thrown if the ports could not be initialized.
     */
    private void _initializeOutputPorts() throws IllegalActionException {

        Iterator it = _getAllTasks().iterator();
        while (it.hasNext()) {
            Actor task = (Actor) it.next();
            if (!isFast((NamedObj) task)) {
                Iterator portIterator = task.outputPortList().iterator();
                while (portIterator.hasNext()) {
                    IOPort port = (IOPort) portIterator.next();
                    _initializePort(port);
                }
            }
        }

        // transfer outputs
        _updateReceivers(((TDLModule) getContainer()).outputPortList());
        getController().readOutputsFromRefinement();

        // transfer inputs
        _updateReceivers(((TDLModule) getContainer()).inputPortList());
        getController().readInputs();

        // init actuators
        Iterator portIterator = getController().outputPortList().iterator();
        while (portIterator.hasNext()) {
            IOPort port = (IOPort) portIterator.next();
            if (!isFast(port)) {
                _initializePort(port);
            }
        }

        // transfer outputs
        _updateReceivers(((TDLModule) getContainer()).outputPortList());
        getController().readOutputsFromRefinement();
    }

    /**
     * Reset the TDL Receivers.
     */
    private void _resetReceivers() {
        ListIterator receivers = _receivers.listIterator();
        while (receivers.hasNext()) {
            TDLReceiver receiver = (TDLReceiver) receivers.next();
            if (receiver.getContainer() != null) {
                receiver.reset();
            } else {
                receivers.remove();
            }
        }
    }

    /**
     * Update actuator by transferring the outputs.
     *
     * @param port
     *            Actuator that should be updated.
     * @exception IllegalActionException
     *             Thrown if outputs could not be transferred.
     */
    private void _updateActuator(IOPort port) throws IllegalActionException {
        RefinementPort rport = (RefinementPort) port;
        List l = rport.deepConnectedOutPortList();
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) instanceof TypedIOPort) {
                super.transferOutputs((TypedIOPort) l.get(i));
            }
        }
    }

    /**
     * Update input port, for TDL that means a sensor value is read.
     *
     * @param port
     *            Port to be updated.
     */
    private void _updateInputPort(IOPort port) {
        Receiver[][] channelArray = port.getReceivers();
        for (Receiver[] receiverArray : channelArray) {
            for (Receiver element : receiverArray) {
                TDLReceiver receiver = (TDLReceiver) element;
                receiver.update();
            }
        }
    }

    /**
     * Read input values and update inputMap the updated inputMap is required
     * when guards are evaluated.
     *
     * @exception IllegalActionException
     *             Thrown if the controller could not be retrieved or inputs
     *             could not be read.
     */
    private void _updateInputs() throws IllegalActionException {
        Iterator it = ((TDLModule) getContainer()).inputPortList().iterator();
        while (it.hasNext()) {
            IOPort port = (IOPort) it.next();
            Receiver[][] channelArray = port.deepGetReceivers();
            for (Receiver[] receiverArray : channelArray) {
                for (Receiver element : receiverArray) {
                    TDLReceiver receiver = (TDLReceiver) element;
                    receiver.update();
                }
            }
        }
        getController().readInputs();
    }

    /**
     * Update output port, for TDL this means an actuator is updated.
     *
     * @param port
     *            The output port.
     * @exception IllegalActionException
     *             Thrown if output ports from refinement could not be read.
     */
    private void _updateOutputPort(IOPort port) throws IllegalActionException {
        Receiver[][] channelArray = port.getRemoteReceivers();
        for (Receiver[] receiverArray : channelArray) {
            for (Receiver element : receiverArray) {
                TDLReceiver receiver = (TDLReceiver) element;
                receiver.update();
            }
        }
        getController().readOutputsFromRefinement();
    }

    /**
     * Update the TDL receivers. An update of a TDL receiver means that a value
     * previously sent to this port will now be accessible too.
     *
     * @param portList
     *            Ports containing TDL receivers that should be updated.
     * @exception IllegalActionException
     * @exception InvalidStateException
     */
    private void _updateReceivers(Collection portList)
            throws InvalidStateException, IllegalActionException {
        Iterator it = portList.iterator();
        while (it.hasNext()) {
            IOPort port = (IOPort) it.next();
            // super.transferOutputs(port);
            Receiver[][] channelArray = port.deepGetReceivers();
            for (Receiver[] receiverArray : channelArray) {
                for (Receiver element : receiverArray) {
                    TDLReceiver receiver = (TDLReceiver) element;
                    receiver.update();
                }
            }
        }
    }

    /**
     * After a mode switch, tasks that exist in the source and the target state
     * must have the same port values. This method transfers input ports.
     *
     * @param transition
     *            Mode switch that has been made.
     * @exception IllegalActionException
     *             If refinement or Controller could not be retrieved.
     */
    private void _transferTaskInputs(Transition transition)
            throws IllegalActionException {
        Refinement oldRefinement = (Refinement) getController().currentState()
                .getRefinement()[0];
        Refinement newRefinement = (Refinement) transition.destinationState()
                .getRefinement()[0];

        List oldTasks = oldRefinement.entityList();
        List newTasks = newRefinement.entityList();

        for (int i = 0; i < newTasks.size(); i++) {
            Actor actor = (Actor) newTasks.get(i);
            for (int j = 0; j < oldTasks.size(); j++) {
                Actor oldActor = (Actor) oldTasks.get(j);
                if (actor.getName().equals(oldActor.getName())) {
                    // same actor -> copy input ports from oldActor to newActor
                    // TODO: check if really same actor
                    for (int k = 0; k < oldActor.inputPortList().size(); k++) {
                        IOPort port = (IOPort) oldActor.inputPortList().get(k);
                        IOPort newPort = (IOPort) actor.inputPortList().get(k);
                        Receiver[][] channelArray = port.getReceivers();
                        Receiver[][] newChannelArray = newPort.getReceivers();
                        for (int l = 0; l < channelArray.length; l++) {
                            Receiver[] receiverArray = channelArray[l];
                            Receiver[] newReceiverArray = newChannelArray[l];
                            for (int m = 0; m < receiverArray.length; m++) {
                                TDLReceiver receiver = (TDLReceiver) receiverArray[m];
                                TDLReceiver newReceiver = (TDLReceiver) newReceiverArray[m];
                                receiver.copyTokensTo(newReceiver);
                            }
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Current worst case execution time, this is set when the execution of a
     * TDL task is started
     */
    private double _currentWCET = 0.0;
    /**
     * Current node in the TDL actions graph.
     */
    private TDLActionsGraph _graph;

    /**
     * Store time stamps from _fireAt() to detect missed executions
     */
    private HashMap<Node, Time> _nextEventsTimeStamps;

    /** Nodes containing actions that depend on previous actions. */
    private HashMap<Node, List<TDLAction>> _nodesDependentOnPreviousActions;

    /**
     * The minimum time to be added to schedule the next action in the _fireAt()
     * method.
     */
    private Time _previousAdditionalScheduleTime = new Time(this, 0.0);

    /**
     * All receivers.
     */
    private LinkedList _receivers = new LinkedList();

}
