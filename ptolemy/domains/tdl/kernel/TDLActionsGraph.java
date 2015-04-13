/*
 A representation of the schedule of all actions in a TDL module in a graph.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
 */
package ptolemy.domains.tdl.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalPort;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A representation of the schedule of all actions in a TDL module in
 * a graph. Nodes in this graph describe TDLActions (@see #TDLAction), edges
 * describe the causal dependencies between those actions. The weight of nodes
 * are the TDLAction objects, the weight of edges are the time that passes
 * between two actions.
 *
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
 */
public class TDLActionsGraph {

    /**
     * Create a new TDLActionsGraph for a TDL module.
     *
     * @param module
     *            The TDL module.
     * @param controller
     *            The controller of the TDL module.
     */
    public TDLActionsGraph(TDLModule module, FSMActor controller) {
        this._controller = controller;
        this._module = module;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Build the graph by iterating through all the modes of the TDL module.
     * Create nodes for transitions, TDL tasks and actuators and connect them.
     *
     * @param startmode
     *            This is the initial mode of the TDL module.
     * @exception IllegalActionException
     *             Thrown if TDL parameters in the module cannot be read.
     */
    public void buildGraph(State startmode) throws IllegalActionException {
        _graph = new DirectedGraph();
        List<State> states = _controller.entityList();
        for (State state : states) {
            Refinement refinement = (Refinement) state.getRefinement()[0];
            try {
                Time modePeriod = ((TDLModuleDirector) _module.getDirector())
                        .getModePeriod(state);
                _getTransitions(state, refinement, modePeriod);
                _getTasks(state, refinement, modePeriod);
                _getActuators(refinement, modePeriod);
            } catch (Exception e) {
                throw new IllegalActionException("Schedule could not be "
                        + "computed; " + e.getMessage());
            }

            _resetsTempVars();
        }
        _connectModes();
        System.out.println(_graph.nodeCount());
        Node startNode = getNode(new TDLAction(new Time(_module.getDirector(),
                0.0), TDLAction.AFTERMODESWITCH, startmode));
        if (startNode == null) {
            startNode = getNode(new TDLAction(
                    ((TDLModuleDirector) _module.getDirector())
                    .getModePeriod(startmode),
                    TDLAction.AFTERMODESWITCH, startmode));
        }
    }

    /**
     * Returns all forward reachable nodes in the graph that are connected to
     * the given node. Those forward reachable nodes do not depend on other
     * nodes and the actions defined in the nodes are scheduled to happen at the
     * same time as the action in the given node.
     *
     * @param node
     *            Given node.
     * @return List of nodes that are forward reachable from the given node.
     */
    public List<Node> getEventsFollowingAction(Node node) {
        return _getForwardReachableIndependentNodes(node, new ArrayList());
    }

    /**
     * Recursively compute the set of nodes reachable from a given node that
     * depend on more than one node or are scheduled to happen at a future time.
     * Examples for the latter are nodes describing the writing of an output
     * port
     *
     * @param justExecuted
     *            Node that was just executed.
     * @param node
     *            Node from which the reachable Nodes are computed.
     * @param visited
     *            Already visited nodes, used to avoid loops.
     * @return Set of reachable nodes that depend on more than one input port or
     *         contain actions that should happen later than the given node.
     */
    public HashMap<Node, List<TDLAction>> getNextJoinNodes(Node justExecuted,
            Node node, List<Node> visited) {
        if (visited.contains(node)) {
            return new HashMap();
        } else {
            visited.add(node);
        }
        HashMap<Node, List<TDLAction>> events = new HashMap();
        List<Edge> edges = (List<Edge>) _graph.outputEdges(node);
        for (Edge edge : edges) {
            Node targetNode = edge.sink();

            if (((TDLAction) targetNode.getWeight()).actionType == TDLAction.MODESWITCH
                    || ((TDLAction) targetNode.getWeight()).actionType == TDLAction.WRITEOUTPUT
                    || ((TDLAction) targetNode.getWeight()).actionType == TDLAction.AFTERMODESWITCH
                    || _graph.inputEdges(targetNode).size() > 1) {
                List<TDLAction> actions = new ArrayList();
                Collection<Edge> backwardEdges = _graph.inputEdges(targetNode);
                for (Edge backwardNode : backwardEdges) {
                    if (!justExecuted.equals(backwardNode.source())) {
                        actions.add((TDLAction) backwardNode.source()
                                .getWeight());
                    }
                }
                if (actions.size() > 0) {
                    events.put(targetNode, actions);
                } else {
                    events.putAll(getNextJoinNodes(justExecuted, targetNode,
                            visited));
                }
            } else {
                events.putAll(getNextJoinNodes(justExecuted, targetNode,
                        visited));
            }

        }
        return events;
    }

    /**
     * Return the node that is used for the execution of an actor at a certain
     * time.
     *
     * @param invocationTime
     *            Time the actor is being invoked.
     * @param actor
     *            Actor that is scheduled for that time.
     * @return The node that is used for the execution of an actor at a certain
     *         time.
     */
    public Node getNode(Time invocationTime, Object actor) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.time.equals(invocationTime)
                    && (gnode.object != null && gnode.object.equals(actor) || gnode.object == null
                    && actor == null)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Get node for a given TDLAction.
     *
     * @param action
     *            Given TDLAction.
     * @return Node for the given TDLAction.
     */
    public Node getNode(TDLAction action) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.equals(action)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Get a node which executes a given actor. This output port is written in a
     * certain time frame defined by the two parameters lower and upper.
     *
     * @param actor
     *            Actor that is executed.
     * @param lower
     *            Lower time bound.
     * @param upper
     *            Upper time bound.
     * @return The node used to execute the actor.
     */
    public Node getNode(Object actor, Time lower, Time upper) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.object.equals(actor) && lower.compareTo(gnode.time) <= 0
                    && upper.compareTo(gnode.time) > 0) {
                return node;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Add the connections between task output ports and other tasks input
     * ports.
     */
    private void _addConnectionsBetweenTaskPorts() {
        Set<IOPort> inputPorts = _tmpConnectedTaskPorts.keySet();
        for (IOPort inputPort : inputPorts) {

            List<IOPort> outputPorts = (List<IOPort>) _tmpConnectedTaskPorts
                    .get(inputPort);
            for (IOPort outputPort : outputPorts) {
                // get times when input port is read
                List<Time> readTimes = _tmpReadTaskInputPorts.get(inputPort);

                // get time when mode switch is done before that
                for (Time readTime : readTimes) {

                    Time modeSwitchTimeBeforeRead = new Time(
                            _module.getDirector(), 0);

                    Set<Time> invocationTimes = _tmpModeSwitchEnds.keySet();
                    for (Time invocationTime : invocationTimes) {
                        if (invocationTime.compareTo(readTime) < 0
                                && modeSwitchTimeBeforeRead
                                .compareTo(invocationTime) < 0) {
                            modeSwitchTimeBeforeRead = invocationTime;
                        }
                    }

                    // get time of writing the task output; this time is >=
                    // modeSwitchTime and <= inputPortReadTime
                    Node outNode = getNode(outputPort,
                            modeSwitchTimeBeforeRead, readTime);
                    if (outNode != null) {
                        Node inNode = getNode(readTime, inputPort);
                        Edge edge = new Edge(outNode, inNode, 0); // TODO:
                        // outnode.time
                        // -
                        // innode.time
                        _graph.addEdge(edge);
                    }
                }
            }
        }
    }

    /**
     * Analyze the slot selection string.
     *
     * @param actor
     * @param modePeriodTime
     * @return The LetTask
     * @exception IllegalActionException
     * @exception TDLModeSchedulerException
     */
    private LetTask _analyzeSlotSelection(TDLTask actor, Time modePeriodTime)
            throws IllegalActionException {
        long modePeriod = modePeriodTime.getLongValue();
        String slots = TDLModuleDirector.getSlots(actor);
        int frequency = TDLModuleDirector.getFrequency(actor);
        ArrayList invocations = _getInvocations(slots, frequency);
        // if task is periodic, it is a let task. otherwise schedule it as a
        // special action
        Long offset, let = Long.valueOf(0), inv = Long.valueOf(0);
        boolean periodic = true;
        // frequency must be divide able by amount of invocations without rest
        long newlet = 0;
        long newInv = 0;
        if (frequency % (invocations.size() / 2) != 0) {
            periodic = false;
        } else {
            // lets must be the same
            for (int i = 0; i < invocations.size(); i += 2) {
                newlet = Math.abs((Integer) invocations.get(i + 1)
                        - (Integer) invocations.get(i));
                if (let == 0) {
                    let = newlet;
                } else if (newlet != let) {
                    periodic = false;
                }
            }
            // invocation periods must be the same
            if (invocations.size() > 2) {
                for (int i = 0; i < invocations.size(); i += 2) {
                    if (invocations.size() > i + 2) {
                        newInv = (Integer) invocations.get(i + 2)
                                - (Integer) invocations.get(i);
                    } else {
                        newInv = ((Integer) invocations.get(0) + frequency)
                                % frequency;
                    }
                    if (inv == 0) {
                        inv = newInv;
                    } else if (newInv != inv) {
                        periodic = false;
                    }
                }
            } else {
                inv = Long.valueOf(1);
            }
        }
        if (periodic) {
            offset = modePeriod / frequency
                    * ((Integer) invocations.get(0) - 1);
            let = modePeriod / let / frequency;
            inv = modePeriod / inv / frequency;
            return new LetTask(actor, let, inv, offset);
        } else { // schedule single task as a set of tasks with different
            // lets and invocation periods
            throw new IllegalActionException("Task " + actor.getName()
                    + " is not periodic, slot selection string: " + slots);
        }
    }

    /**
     * Connect partial graphs for modes. The connection points are the mode
     * switch and after mode switch actions.
     *
     * @exception IllegalActionException
     *             If new Time cannot be created.
     */
    private void _connectModes() throws IllegalActionException {
        // connect mode switches of all modes.
        Set<State> states = _modeSwitches.keySet();
        for (State state : states) {
            List<Object[]> nodePairs = _modeSwitches.get(state);
            for (Object[] nodePair : nodePairs) {
                Node sourceModeNode = (Node) nodePair[0];
                TDLTransition transition = (TDLTransition) ((TDLAction) sourceModeNode
                        .getWeight()).object;
                State targetMode = transition.destinationState();

                List<Object[]> targetNodePairs = _modeSwitches.get(targetMode);
                for (Object[] targetNodePair : targetNodePairs) {
                    Node targetModeNode = (Node) targetNodePair[1];
                    if (((TDLAction) targetModeNode.getWeight()).time
                            .equals(new Time(_module.getDirector(), 0.0))) {
                        Edge edge = new Edge(sourceModeNode, targetModeNode, 0);
                        _graph.addEdge(edge);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Connect a node for a TDL action to the next mode switch in time.
     *
     * @param prev
     *            Previous node to connect to.
     * @param actorInvocationTime
     *            Invocation time of that actor.
     * @param actorInvocationPeriod
     *            Invocation period of that actor.
     */
    private void _connectToIntermediateModeSwitch(Node prev,
            Time actorInvocationTime, Time actorInvocationPeriod) {
        // if there is a mode switch between now and the next invocation, add
        // edge to graphStart and graphEnd
        Time nextModeSwitchTime = new Time(_module.getDirector(), 0);
        Time modeSwitchTimeBeforeNextTaskInvocation = new Time(
                _module.getDirector(), 0);

        if (_tmpModeSwitchStarts.size() == 0) {
            return;
        }
        Set<Time> invocationTimes = _tmpModeSwitchStarts.keySet();
        for (Time invocationTime : invocationTimes) {
            if (invocationTime.compareTo(actorInvocationTime) > 0
                    && invocationTime.compareTo(actorInvocationTime
                            .add(actorInvocationPeriod)) < 0) {
                nextModeSwitchTime = invocationTime;
                modeSwitchTimeBeforeNextTaskInvocation = invocationTime;
            }

        }
        if (modeSwitchTimeBeforeNextTaskInvocation.equals(new Time(_module
                .getDirector(), 0))) {
            return;
        }

        Node modeSwitch = _tmpModeSwitchStarts.get(nextModeSwitchTime);
        if (modeSwitch != null) {
            Edge edge = new Edge(prev, modeSwitch, 0);
            _graph.addEdge(edge);
        }
    }

    /**
     * Connect an input port to the output port of another task that this input
     * port reads from.
     *
     * @param taskInputPort
     */
    private void _connectToOtherTasksOutputPorts(IOPort taskInputPort) {
        List<IOPort> inputPorts = taskInputPort.connectedPortList();
        for (IOPort inputPort : inputPorts) {
            Actor actor = (Actor) inputPort.getContainer();
            // if input port reads from another tasks output port
            if (actor instanceof TDLTask
                    && !actor.equals(taskInputPort.getContainer())) {
                // if task output port is written and there is no mode switch in
                // between, add an edge
                // this information is not clear yet, these connections can only
                // be added in the end
                // store the ports and add the conections later
                List l = (List) _tmpConnectedTaskPorts.get(taskInputPort);
                if (l == null) {
                    l = new ArrayList();
                    _tmpConnectedTaskPorts.put(taskInputPort, l);
                }
                l.add(inputPort);
            }
        }
    }

    /**
     * Create a TDL action and a new node with an edge to the previous node
     *
     * @param invocationTime
     *            Invocation time of the node.
     * @param actionType
     *            Action type of the TDL action in the node.
     * @param actor
     *            Actor of the TDL action.
     * @param previous
     *            Previous node.
     * @return New node.
     */
    private Node _createNode(Time invocationTime, int actionType, Object actor,
            Node previous) {
        Node node = new Node(new TDLAction(invocationTime, actionType, actor));
        _graph.addNode(node);
        if (previous != null) {
            Edge edge = new Edge(previous, node,
                    ((TDLAction) node.getWeight()).time
                    .subtract(((TDLAction) previous.getWeight()).time));
            _graph.addEdge(edge);
        }
        return node;
    }

    /**
     * Create TDL action and a new node.
     *
     * @param invocationTime
     *            Invocation time of the node.
     * @param actionType
     *            Action type of the TDL action in the node.
     * @param actor
     *            Actor of the TDL action.
     * @return New node.
     */
    private Node _createNode(Time invocationTime, int actionType, Object actor) {
        Node node = new Node(new TDLAction(invocationTime, actionType, actor));
        _graph.addNode(node);
        return node;
    }

    /**
     * Add actuator updates to the graph.
     *
     * @param refinement
     *            Refinement containing the actuator ports.
     * @param modePeriod
     *            Period of the mode defined by the refinement.
     */
    private void _getActuators(Refinement refinement, Time modePeriod) {
        List<IOPort> outputPorts = refinement.outputPortList();
        for (IOPort outputPort : outputPorts) {

            int frequency = TDLModuleDirector.getFrequency(outputPort);

            IOPort connectedPort = null;

            // get connected task output ports
            List<IOPort> taskOutputPorts = outputPort.deepInsidePortList();
            for (IOPort taskOutputPort : taskOutputPorts) {
                if (taskOutputPort.isOutput()
                        && taskOutputPort.getContainer() instanceof TDLTask) {
                    connectedPort = taskOutputPort; // only one task, not multiple
                    // tasks, can write to one
                    // actuator!!
                }
            }

            if (connectedPort != null && connectedPort.isOutput()) {
                for (int i = 1; i <= frequency; i++) {
                    Time invocationEndTime = new Time(_module.getDirector(),
                            modePeriod.getLongValue() / frequency * i);

                    Node node = _getLastNodeBeforeTime(connectedPort,
                            invocationEndTime);
                    Node next = null;
                    Edge edge = null;
                    Collection<Edge> edges = _graph.outputEdges(node);
                    for (Edge edge1 : edges) {
                        edge = edge1;
                        if (edge.source().equals(node)) {
                            next = edge.sink();
                        }
                    }

                    // remove edge between node and next and insert actuator in
                    // between
                    _graph.removeEdge(edge);
                    Node n = new Node(new TDLAction(invocationEndTime,
                            TDLAction.WRITEACTUATOR, outputPort));
                    _graph.addNode(n);
                    _graph.addEdge(new Edge(node, n, 0));
                    _graph.addEdge(new Edge(n, next, 0));
                }
            }
        }
    }

    /**
     * Recursively compute the list of forward reachable nodes that only depend
     * on one previous node and are scheduled for the same time as the current
     * node.
     *
     * @param node
     *            Node from which the reachable nodes are computed from.
     * @param visited
     *            Already visited nodes to avoid loops.
     * @return List of forward reachable nodes.
     */
    private List<Node> _getForwardReachableIndependentNodes(Node node,
            List<Node> visited) {
        if (visited.contains(node)) {
            return new ArrayList();
        } else {
            visited.add(node);
        }
        List<Node> events = new ArrayList();
        List<Edge> edges = (List<Edge>) _graph.outputEdges(node);
        for (Edge edge : edges) {
            Node targetNode = edge.sink();
            if (_graph.inputEdgeCount(targetNode) == 1) {
                events.add(targetNode);
                if (((TDLAction) targetNode.getWeight()).actionType != TDLAction.READSENSOR
                        && ((TDLAction) targetNode.getWeight()).actionType != TDLAction.MODESWITCH
                        && ((TDLAction) targetNode.getWeight()).actionType != TDLAction.EXECUTETASK
                        && ((TDLAction) targetNode.getWeight()).actionType != TDLAction.AFTERMODESWITCH) {
                    events.addAll(_getForwardReachableIndependentNodes(
                            targetNode, visited));
                }
            }

        }
        return events;
    }

    /**
     * Analyze the slot selection string.
     *
     * @param slots
     * @param frequency
     * @return The list of invocations.
     * @exception IllegalActionException
     */
    private ArrayList _getInvocations(String slots, int frequency)
            throws IllegalActionException {
        String slotSelection = slots + "\n";
        ArrayList invocations = new ArrayList();
        String number = "";
        int startSlot = 0, endSlot = 0;
        boolean prevSymbolWasAsterisk = false;
        for (int i = 0; i < slotSelection.length(); i++) {
            switch (slotSelection.charAt(i)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                number += slotSelection.charAt(i);
                if (_nextCharIsANumber(slotSelection, i)) {
                    number += slotSelection.charAt(++i);
                }
                int slotNumber = Integer.parseInt(number);
                if (startSlot == 0) {
                    startSlot = slotNumber;
                    if (prevSymbolWasAsterisk) {
                        int lastStart = (Integer) invocations.get(invocations
                                .size() - 2);
                        int lastEnd = (Integer) invocations.get(invocations
                                .size() - 1);
                        int lastDiff = lastEnd - lastStart;
                        while (lastEnd + 1 < startSlot) {
                            invocations.add(lastEnd);
                            invocations.add(lastEnd + lastDiff);
                            lastEnd += lastDiff;
                        }
                        prevSymbolWasAsterisk = false;
                    }
                } else {
                    endSlot = slotNumber + 1;
                }
                number = "";
                break;
            case '*':
                prevSymbolWasAsterisk = true;
                break;
            case '-':
            case '~': // optional
                break;
            case '|':
            case '\n':
                invocations.add(startSlot);
                invocations.add(endSlot);
                startSlot = endSlot = 0;
                if (prevSymbolWasAsterisk) {
                    int lastStart = (Integer) invocations.get(invocations
                            .size() - 2);
                    int lastEnd = (Integer) invocations
                            .get(invocations.size() - 1);
                    if (lastEnd == 0) {
                        lastEnd = lastStart + 1;
                    }
                    int lastDiff = lastEnd - lastStart;
                    int end = startSlot;
                    if (end == 0) {
                        end = frequency + 1;
                    }
                    while (lastEnd < end) {
                        invocations.add(lastEnd);
                        invocations.add(lastEnd + lastDiff);
                        lastEnd += lastDiff;
                    }
                    prevSymbolWasAsterisk = false;
                } else {
                    invocations.add(startSlot);
                    invocations.add(endSlot);
                }
                break;
            default:
                throw new IllegalActionException("'" + slots
                        + "' cannot be parsed");
            }
        }
        // update invocations so that 0-values which came from not initializing
        // secondSlot are filled
        for (int i = 0; i < invocations.size(); i += 2) {
            if ((Integer) invocations.get(i + 1) == 0) {
                invocations.add(i + 1, (Integer) invocations.get(i) + 1);
                invocations.remove(i + 2);
            }
        }
        return invocations;
    }

    /**
     * Return invocation of node for a port closest to a given time.
     *
     * @param port
     * @param upper
     * @return The node
     */
    private Node _getLastNodeBeforeTime(IOPort port, Time upper) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        Node lastNodeBeforeTime = null;
        Time time = Time.NEGATIVE_INFINITY;
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.object != null && gnode.object.equals(port)
                    && upper.compareTo(gnode.time) >= 0
                    && time.compareTo(gnode.time) < 0) {
                lastNodeBeforeTime = node;
                time = gnode.time;
            }
        }
        return lastNodeBeforeTime;
    }

    /**
     * Create subgraphs for all tasks, interconnect them with mode switches and
     * other tasks.
     *
     * @param refinement
     * @param modePeriod
     * @exception TDLModeSchedulerException
     * @exception IllegalActionException
     */
    private void _getTasks(State mode, Refinement refinement, Time modePeriod)
            throws IllegalActionException {
        // schedule tasks
        List<TDLTask> tasks = refinement.entityList();
        Time timeZero = new Time(_module.getDirector(), 0.0);

        for (TDLTask taskActor : tasks) {
            LetTask task = _analyzeSlotSelection(taskActor, modePeriod);
            List<ModalPort> sensors = taskActor.getSensorsReadFrom(
                    refinement.inputPortList(), _module.inputPortList());

            Node modeSwitchEnd = null, modeSwitchStart = null;
            if (_tmpModeSwitchStarts == null
                    || _tmpModeSwitchStarts.size() == 0) {
                modeSwitchStart = modeSwitchEnd = _createNode(timeZero,
                        TDLAction.AFTERMODESWITCH, mode);
                if (_tmpModeSwitchStarts != null) {
                    _tmpModeSwitchStarts.put(timeZero, modeSwitchStart);
                    _tmpModeSwitchEnds.put(timeZero, modeSwitchStart);
                } else {
                    new Exception("_tmpModeSwitchStarts was null?").printStackTrace();
                }
            } else {
                modeSwitchStart = _tmpModeSwitchStarts.get(timeZero);
                if (modeSwitchStart == null) {
                    modeSwitchStart = _tmpModeSwitchStarts.get(modePeriod);
                }
            }
            if (_tmpModeSwitchEnds != null && _tmpModeSwitchEnds.size() != 0) { // no mode switches, only one mode
                modeSwitchEnd = _tmpModeSwitchEnds.get(timeZero);
                if (modeSwitchEnd == null) {
                    modeSwitchEnd = _tmpModeSwitchEnds.get(modePeriod);
                }
            }

            List<Node> outputPortNodes = null;
            Node invocationEndNode = modeSwitchEnd;
            for (long i = task.getOffset(); i < modePeriod.getLongValue(); i += task
                    .getInvocationPeriod()) {
                Time invocationTime = new Time(_module.getDirector(), i);

                List<IOPort> notConnectedToSensors = new ArrayList();
                notConnectedToSensors.addAll(taskActor.inputPortList());
                List<Node> inputPortNodes = new ArrayList();
                for (ModalPort sensor : sensors) {
                    Node sensorNode = null, inputPortNode = null;
                    // if not already been read at this time instant
                    if (_tmpReadSensors.size() == 0
                            || _tmpReadSensors.get(invocationTime) == null
                            || !_tmpReadSensors.get(invocationTime).contains(
                                    sensor)) {
                        sensorNode = _createNode(invocationTime,
                                TDLAction.READSENSOR, sensor, invocationEndNode);
                    } else {
                        // if has already been read at this time instant, before a mode switch, connect
                        // task input ports to the mode switch and not to the sensor
                        sensorNode = getNode(invocationTime, sensor);
                        boolean sensorWasReadBeforeModeSwitch = false;
                        List<Edge> edges = (List<Edge>) _graph
                                .outputEdges(sensorNode);
                        if (edges.size() > 0) {
                            Node targetNode = edges.get(0).sink();
                            while (((TDLAction) targetNode.getWeight()).actionType == TDLAction.READSENSOR) {
                                edges = (List<Edge>) _graph
                                        .outputEdges(targetNode);
                                targetNode = edges.get(0).sink();
                            }
                            if (((TDLAction) targetNode.getWeight()).actionType == TDLAction.MODESWITCH) {
                                sensorWasReadBeforeModeSwitch = true;

                            }
                        }
                        if (sensorWasReadBeforeModeSwitch) {
                            sensorNode = modeSwitchEnd;
                        }
                    }
                    List<IOPort> inputPorts = taskActor.inputPortList();
                    for (IOPort inputPort : inputPorts) {
                        if (inputPort.isDeeplyConnected(sensor)) {
                            notConnectedToSensors.remove(inputPort);
                            inputPortNode = _createNode(invocationTime,
                                    TDLAction.READINPUT, inputPort, sensorNode);
                            inputPortNodes.add(inputPortNode);
                            _registerTaskInputPortReading(invocationTime,
                                    inputPort);
                            _connectToOtherTasksOutputPorts(inputPort);
                        } else {

                        }
                    }
                }

                Node taskExecutionNode = _createNode(invocationTime,
                        TDLAction.EXECUTETASK, taskActor);
                for (IOPort notConnectedToSensor : notConnectedToSensors) {
                    Node inputPortNode = _createNode(invocationTime,
                            TDLAction.READINPUT, notConnectedToSensor,
                            invocationEndNode);
                    inputPortNodes.add(inputPortNode);
                }
                if (inputPortNodes.size() == 0) {
                    Edge edge = new Edge(invocationEndNode, taskExecutionNode);
                    _graph.addEdge(edge);
                }
                for (Node inputPortNode : inputPortNodes) {
                    Edge edge = new Edge(inputPortNode, taskExecutionNode);
                    _graph.addEdge(edge);
                }

                outputPortNodes = new ArrayList();
                Node outputPortNode = null;
                List<IOPort> outputPorts = taskActor.outputPortList();
                for (IOPort outputPort : outputPorts) {
                    Time writePortTime = new Time(_module.getDirector(), i
                            + task.getLet());
                    Time nextInvocationTime = new Time(_module.getDirector(), i
                            + task.getLet() + task.getOffset());
                    if (nextInvocationTime.equals(modePeriod)) {
                        nextInvocationTime = timeZero;
                    }
                    outputPortNode = _createNode(writePortTime,
                            TDLAction.WRITEOUTPUT, outputPort,
                            taskExecutionNode);
                    outputPortNodes.add(outputPortNode);
                    _connectToIntermediateModeSwitch(
                            outputPortNode,
                            new Time(_module.getDirector(), i),
                            new Time(_module.getDirector(), task
                                    .getInvocationPeriod()));
                    //                    List<IOPort> ports = outputPort.connectedPortList();
                    //                    for (IOPort port : ports) {
                    //                        if (port.isInput() && port.getContainer().equals(taskActor)) {
                    //                            // output port is connected to input port of the same task
                    //                            Edge edge = new Edge(outputPortNode, getNode(nextInvocationTime, port));
                    //                            _graph.addEdge(edge);
                    //                        }
                    //                    }
                }
                // need a single invocation end node
                if (outputPortNodes.size() == 0) {
                    invocationEndNode = taskExecutionNode;
                } else if (outputPortNodes.size() == 1) {
                    invocationEndNode = outputPortNodes.get(0);
                } else if (outputPortNodes.size() > 1) {
                    invocationEndNode = getNode(new Time(_module.getDirector(),
                            0), null);
                    if (invocationEndNode == null) {
                        invocationEndNode = _createNode(
                                new Time(_module.getDirector(), i),
                                TDLAction.AFTERTASKOUTPUTS, null);
                    }
                }
            }

            // add an edge to the graphStarts elements
            for (Node outputPortNode : outputPortNodes) {
                Edge edge = new Edge(outputPortNode, modeSwitchStart,
                        modePeriod.subtract(((TDLAction) outputPortNode
                                .getWeight()).time));
                _graph.addEdge(edge);
            }
        }

        _addConnectionsBetweenTaskPorts();
    }

    /**
     * First read transitions and build subgraphs for the transitions.
     *
     * @param mode
     * @param refinement
     * @param modePeriod
     * @exception IllegalActionException
     * @exception TDLModeSchedulerException
     */
    private void _getTransitions(State mode, Refinement refinement,
            Time modePeriod) throws IllegalActionException {
        // sort transitions here - TODO: sort attribute, priority

        HashMap<Time, List<IOPort>> sensorsAndTransitions = new HashMap();

        List<TDLTransition> transitions = mode.nonpreemptiveTransitionList();
        for (TDLTransition transition : transitions) {
            int frequency = TDLModuleDirector.getFrequency(transition);
            long invocationPeriod = modePeriod.getLongValue() / frequency;
            Director director = _module.getDirector();
            for (int i = 0; i < frequency; i++) {
                List l = sensorsAndTransitions.get(new Time(director, i));
                if (l == null) {
                    l = new ArrayList();
                    sensorsAndTransitions.put(new Time(_module.getDirector(), i
                            * invocationPeriod), l);
                }
                List<IOPort> requiredSensors = transition.requiredSensors;
                for (IOPort requiredSensor : requiredSensors) {
                    l.add(requiredSensor);
                }
                l.add(transition);
            }
        }
        // now we have a list containing time stamp, sensors to be read and
        // transitions to be read

        // create graphs
        if (sensorsAndTransitions.size() != 0) {

            Set<Time> invocationTimes = sensorsAndTransitions.keySet();
            for (Time invocationTime : invocationTimes) {
                Node prev = null;
                Time modeSwitchEndTime = new Time(_module.getDirector(),
                        invocationTime.getLongValue()
                        % modePeriod.getLongValue());
                Node endNode = new Node(new TDLAction(modeSwitchEndTime,
                        TDLAction.AFTERMODESWITCH, mode));
                _tmpModeSwitchEnds.put(invocationTime, endNode);
                _graph.addNode(endNode);
                List<IOPort> l = sensorsAndTransitions.get(invocationTime);
                for (int i = 0; i < l.size(); i++) {
                    Node node = null;
                    Object sensorOrTransition = l.get(i);

                    if (sensorOrTransition instanceof ModalPort) {
                        node = new Node(new TDLAction(invocationTime,
                                TDLAction.READSENSOR, sensorOrTransition));
                        _graph.addNode(node);
                        _registerSensorReading(invocationTime,
                                (IOPort) sensorOrTransition);
                    } else {// transition
                        node = new Node(new TDLAction(invocationTime,
                                TDLAction.MODESWITCH, sensorOrTransition));
                        _graph.addNode(node);
                        _registerModeSwitch(mode, node, endNode);
                    }
                    if (prev != null) {
                        Edge edge = new Edge(prev, node, 0);
                        _graph.addEdge(edge);
                    } else {
                        _tmpModeSwitchStarts.put(modeSwitchEndTime, node);
                    }
                    prev = node;
                }

                Edge edge = new Edge(prev, endNode, 0);
                _graph.addEdge(edge);
                _tmpModeSwitchEnds.put(invocationTime, endNode);
            }
        }
        // now we have transitions and sensor readings in the graph; the
        // transitions for the same time are connected
        // graphStarts and graphEnds contain starts end ends of those subgraphs

    }

    /**
     * Return true if the character at position i in the string slotSelection is
     * a number.
     *
     * @param slotSelection
     * @param i
     * @return True if the character at position i in the string is a number.
     */
    private boolean _nextCharIsANumber(String slotSelection, int i) {
        return slotSelection.length() > i + 1
                && slotSelection.charAt(i + 1) > 47
                && slotSelection.charAt(i + 1) < 58;
    }

    /**
     * Store mode switches.
     *
     * @param state
     * @param transition
     * @param transitionNotTaken
     */
    private void _registerModeSwitch(State state, Node transition,
            Node transitionNotTaken) {
        List l = _modeSwitches.get(state);
        if (l == null) {
            l = new ArrayList();
            _modeSwitches.put(state, l);
        }
        l.add(new Object[] { transition, transitionNotTaken });
    }

    /**
     * Save that a sensor value was read at a certain point in time.
     * invocationTime: sensor1, sensor2, ...
     *
     * @param invocationTime
     *            Time the sensor is read.
     * @param sensor
     *            Sensor that is read.
     */
    private void _registerSensorReading(Time invocationTime, IOPort sensor) {
        List l = _tmpReadSensors.get(invocationTime);
        if (l == null) {
            l = new ArrayList();
            _tmpReadSensors.put(invocationTime, l);
        }
        l.add(sensor);
    }

    /**
     * Save the times a task input port was written.
     *
     * @param invocationTime
     *            Time the port is read.
     * @param port
     *            Port that is read.
     */
    private void _registerTaskInputPortReading(Time invocationTime, IOPort port) {
        List l = _tmpReadTaskInputPorts.get(port);
        if (l == null) {
            l = new ArrayList();
            _tmpReadTaskInputPorts.put(port, l);
        }
        l.add(invocationTime);
    }

    /**
     * Reset temporary variables used to create the graph for one mode.
     */
    private void _resetsTempVars() {
        _tmpReadSensors.clear();
        _tmpReadTaskInputPorts.clear();
        _tmpConnectedTaskPorts.clear();
        _tmpModeSwitchStarts.clear();
        _tmpModeSwitchEnds.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Controller of the TDL module.
     */
    private FSMActor _controller;

    /**
     * Graph containing all TDL actions.
     */
    private DirectedGraph _graph;

    /**
     * store all mode switch entry and exit points for all modes in the form:
     * mode1: modeSwitch1 : Object[](entry, exit) modeSwtich2 : Object[]entry,
     * exit) mode2: ...
     */
    private HashMap<State, List> _modeSwitches = new HashMap();

    /**
     * The TDL module.
     */
    private TDLModule _module;

    /**
     * Store the connection between output ports of one task with input ports of
     * another task.
     */
    private HashMap _tmpConnectedTaskPorts = new HashMap();

    /**
     * Store end of mode switch subgraphs. A mode switch subgraph consists of
     * sensors that are read and the mode switch.
     */
    private HashMap<Time, Node> _tmpModeSwitchEnds = new HashMap();

    /**
     * Store starts of mode switch subgraphs. A mode switch subgraph consists of
     * sensors that are read and the mode switch.
     */
    private HashMap<Time, Node> _tmpModeSwitchStarts = new HashMap();

    /**
     * Store the times sensors are read. This is used to only read a sensor once
     * at a time. If a sensor is read before a mode switch, it should not be
     * read again before executing tasks at the same time instant in order to
     * preserve determinism.
     */
    private HashMap<Time, List> _tmpReadSensors = new HashMap();

    /**
     * Store the times task input ports are read.
     */
    private HashMap<IOPort, List<Time>> _tmpReadTaskInputPorts = new HashMap();

}
