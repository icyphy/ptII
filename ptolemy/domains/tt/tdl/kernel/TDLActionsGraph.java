package ptolemy.domains.tt.tdl.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.modal.ModalPort;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.domains.tt.kernel.LetTask;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * The TDLActionsGraph helps building a graph containing all TDL actions of a
 * TDLModule thus representing their causal connections and timing information.
 * A node in the graph is a TDLAction with a time stamp. Edges show causal
 * connections between the nodes.
 * 
 * @author Patricia Derler
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

    /**
     * Build the graph by iterating through all the modes of the TDL module.
     * Create nodes for transitions, TDL tasks and actuators and connect them.
     * 
     * @param startmode
     *            This is the initial mode of the TDL module.
     * @throws IllegalActionException
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
                e.printStackTrace();
            }

            _resetsTempVars();
        }
        _connectModes();
        System.out.println(_graph.nodeCount());
        Node startNode = getNode(new TDLAction(new Time(_module.getDirector(),
                0.0), TDLAction.AFTERMODESWITCH, startmode));
        if (startNode == null)
            startNode = getNode(new TDLAction(((TDLModuleDirector) _module
                    .getDirector()).getModePeriod(startmode),
                    TDLAction.AFTERMODESWITCH, startmode));
        _printGraph(startNode, new ArrayList(), "");
    }

    /**
     * Return the node that is used for the execution of an actor at a certain
     * time.
     * 
     * @param invocationTime
     * @param actor
     * @return
     */
    public Node getNode(Time invocationTime, Object actor) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.time.equals(invocationTime)
                    && ((gnode.object != null && gnode.object.equals(actor)) || (gnode.object == null && actor == null)))
                return node;
        }
        return null;
    }

    public Node getNode(TDLAction action) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.equals(action))
                return node;
        }
        return null;
    }

    /**
     * Get a node which is used for writing an output port. This output port is
     * written in a certain time frame defined by the two parameters lower and
     * upper.
     * 
     * @param port
     * @param lower
     * @param upper
     * @return
     */
    public Node getNode(Object port, Time lower, Time upper) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.object.equals(port) && lower.compareTo(gnode.time) <= 0
                    && upper.compareTo(gnode.time) > 0)
                return node;
        }
        return null;
    }

    /**
     * @param nextMode
     *            Is null except for if a mode switch was taken.
     * @throws IllegalActionException
     */
    public void nextNodes(State nextMode) throws IllegalActionException {
        Collection sinkNodes = _graph.successors(_currentNode);
        if (sinkNodes.size() == 0) {
            throw new IllegalActionException(
                    "Graph Error: no sink nodes from node " + _currentNode);
        } else if (sinkNodes.size() == 1) {
            _nextNodes.add((Node) sinkNodes.iterator().next());
        } else {
            // current node is task output port that is connected to
            // another tasks input port
            if (((TDLAction) _currentNode.getWeight()).actionType == TDLAction.WRITEOUTPUT) {
                _nextNodes.addAll(sinkNodes);
            }
            // current node is transition 
            else if (((TDLAction) _currentNode.getWeight()).actionType == TDLAction.AFTERMODESWITCH
                    && nextMode != null) {
                _nextNodes.add((Node) (_modeSwitches.get(nextMode)).get(1));
            } else {
                throw new IllegalActionException(
                        "Graph Error: multiple sink nodes but not an output port or mode switch");
            }
        }
    }

    private void _printGraph(Node node, List<Node> visited, String s) {
        if (visited.contains(node)) {
            return;
        } else {
            visited.add(node);
            Collection<Edge> edges = _graph.outputEdges(node);
            for (Edge edge : edges) {
                System.out.println(s + edge);
                _printGraph(edge.sink(), visited, s + "    ");
            }
        }
    }

    /**
     * Add the connections between task output ports and other tasks input
     * ports.
     */
    private void _addConnectionsBetweenTaskPorts() {
        Set<IOPort> inputPorts = (Set<IOPort>) _tmpConnectedTaskPorts.keySet();
        for (IOPort inputPort : inputPorts) {

            List<IOPort> outputPorts = (List<IOPort>) _tmpConnectedTaskPorts
                    .get(inputPort);
            for (IOPort outputPort : outputPorts) {
                // get times when input port is read
                List<Time> readTimes = _tmpReadTaskInputPorts.get(inputPort);

                // get time when mode switch is done before that
                for (Time readTime : readTimes) {

                    Time modeSwitchTimeBeforeRead = new Time(_module
                            .getDirector(), 0);

                    Set<Time> invocationTimes = _tmpModeSwitchEnds.keySet();
                    for (Time invocationTime : invocationTimes) {
                        if (invocationTime.compareTo(readTime) < 0
                                && modeSwitchTimeBeforeRead
                                        .compareTo(invocationTime) < 0)
                            modeSwitchTimeBeforeRead = invocationTime;
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
     * @param modePeriod
     * @return
     * @throws IllegalActionException 
     * @throws TDLModeSchedulerException
     */
    private LetTask _analyzeSlotSelection(TDLTask actor, Time modePeriodTime) throws IllegalActionException
           {
        long modePeriod = modePeriodTime.getLongValue();
        String slots = TDLModuleDirector.getSlots((NamedObj) actor);
        int frequency = TDLModuleDirector.getFrequency((NamedObj) actor);
        ArrayList invocations = _getInvocations(slots, frequency);
        // if task is periodic, it is a let task. otherwise schedule it as a
        // special action
        Long offset, let = new Long(0), inv = new Long(0);
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
                if (let == 0)
                    let = newlet;
                else if (newlet != let)
                    periodic = false;
            }
            // invocation periods must be the same
            if (invocations.size() > 2) {
                for (int i = 0; i < invocations.size(); i += 2) {
                    if (invocations.size() > i + 2) {
                        newInv = (Integer) invocations.get(i + 2)
                                - (Integer) invocations.get(i);
                    } else
                        newInv = ((Integer) invocations.get(0) + frequency)
                                % frequency;
                    if (inv == 0)
                        inv = newInv;
                    else if (newInv != inv)
                        periodic = false;
                }
            } else {
                inv = new Long(1);
            }
        }
        if (periodic) {
            offset = (modePeriod / frequency)
                    * ((Integer) invocations.get(0) - 1);
            let = (modePeriod / let) / frequency;
            inv = (modePeriod / inv) / frequency;

            actor.let = let;
            actor.invocationPeriod = inv;
            actor.offset = offset;
            return new LetTask(actor, let, inv, offset);
        } else { // schedule single task as a set of tasks with different
            // lets and invocation periods
            throw new IllegalActionException("Task " + actor.getName()
                    + " is not periodic, slot selection string: " + slots);
        }
    }

    private void _connectModes() throws IllegalActionException {
        // connect mode switches of all modes.
        Set<State> states = (Set<State>) _modeSwitches.keySet();
        for (State state : states) {
            List<Object[]> nodePairs = _modeSwitches.get(state);
            for (Object[] nodePair : nodePairs) {
                Node sourceModeNode = (Node) nodePair[0];
                TDLTransition transition = (TDLTransition) ((TDLAction) sourceModeNode
                        .getWeight()).object;
                State targetMode = transition.destinationState();

                List<Object[]> targetNodePairs = ((List) _modeSwitches
                        .get(targetMode));
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
     * Connect a node to the next mode switch in time.
     * 
     * @param prev
     * @param taskInvocationTime
     * @param taskInvocationPeriod
     */
    private void _connectToIntermediateModeSwitch(Node prev,
            Time taskInvocationTime, Time taskInvocationPeriod) {
        // if there is a mode switch between now and the next invocation, add
        // edge to graphStart and graphEnd
        Time nextModeSwitchTime = new Time(_module.getDirector(), 0);
        Time modeSwitchTimeBeforeNextTaskInvocation = new Time(_module
                .getDirector(), 0);

        if (_tmpModeSwitchStarts.size() == 0)
            return;
        Set<Time> invocationTimes = _tmpModeSwitchStarts.keySet();
        for (Time invocationTime : invocationTimes) {
            if (invocationTime.compareTo(taskInvocationTime) > 0
                    && invocationTime.compareTo(taskInvocationTime
                            .add(taskInvocationPeriod)) < 0) {
                nextModeSwitchTime = invocationTime;
                modeSwitchTimeBeforeNextTaskInvocation = invocationTime;
            }

        }
        if (modeSwitchTimeBeforeNextTaskInvocation.equals(new Time(_module
                .getDirector(), 0)))
            return;

        Node modeSwitch = (Node) _tmpModeSwitchStarts.get(nextModeSwitchTime);
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
     * Create a new node with an edge to the previous node
     * 
     * @param invocationTime
     * @param actionType
     * @param actor
     * @param previous
     * @return
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
     * Create a new node.
     * 
     * @param invocationTime
     * @param actionType
     * @param actor
     * @param previous
     * @return
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
     * @param modePeriod
     */
    private void _getActuators(Refinement refinement, Time modePeriod) {
        List<IOPort> outputPorts = refinement.outputPortList();
        for (IOPort outputPort : outputPorts) {

            int frequency = TDLModuleDirector
                    .getFrequency((NamedObj) outputPort);

            IOPort connectedPort = null;

            // get connected task output ports
            List<IOPort> taskOutputPorts = outputPort.deepInsidePortList();
            for (IOPort taskOuputPort : taskOutputPorts) {
                if (taskOuputPort.getContainer() instanceof TDLTask) {
                    connectedPort = taskOuputPort; // only one task, not multiple
                    // tasks, can write to one
                    // actuator!!
                }
            }

            if (connectedPort != null) {
                for (int i = 1; i <= frequency; i++) {
                    Time invocationEndTime = new Time(_module.getDirector(),
                            modePeriod.getLongValue() / frequency * (i));

                    Node node = getLastNodeBeforeTime(connectedPort,
                            invocationEndTime);
                    Node next = null;
                    Edge edge = null;
                    Collection<Edge> edges = (Collection<Edge>) _graph
                            .outputEdges(node);
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

    private Node getLastNodeBeforeTime(IOPort port, Time upper) {
        List<Node> nodes = (List<Node>) _graph.nodes();
        Node lastNodeBeforeTime = null;
        Time time = Time.NEGATIVE_INFINITY;
        for (Node node : nodes) {
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.object.equals(port) && upper.compareTo(gnode.time) >= 0
                    && time.compareTo(gnode.time) < 0) {
                lastNodeBeforeTime = node;
                time = gnode.time;
            }
        }
        return lastNodeBeforeTime;
    }

    /**
     * Analyze the slot seleciton string.
     * 
     * @param slots
     * @param frequency
     * @return
     * @throws IllegalActionException  
     */
    private ArrayList _getInvocations(String slots, int frequency) throws IllegalActionException
             {
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
                if (_nextCharIsANumber(slotSelection, i))
                    number += slotSelection.charAt(++i);
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
                } else
                    endSlot = slotNumber + 1;
                number = "";
                break;
            case '*':
                prevSymbolWasAsterisk = true;
                break;
            case '-':
                break;
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
                    if (lastEnd == 0)
                        lastEnd = lastStart + 1;
                    int lastDiff = lastEnd - lastStart;
                    int end = startSlot;
                    if (end == 0)
                        end = frequency + 1;
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
     * Create subgraphs for all tasks, interconnect them with mode switches and
     * other tasks.
     * 
     * @param refinement
     * @param modePeriod
     * @throws TDLModeSchedulerException
     * @throws IllegalActionException
     */
    private void _getTasks(State mode, Refinement refinement, Time modePeriod)
            throws IllegalActionException {
        // schedule tasks
        List<TDLTask> tasks = (List<TDLTask>) refinement.entityList();

        for (TDLTask taskActor : tasks) {
            LetTask task = _analyzeSlotSelection(taskActor, modePeriod);
            List<ModalPort> sensors = taskActor.getSensorsReadFrom(refinement
                    .inputPortList(), _module.inputPortList());

            Node modeSwitchEnd = null, modeSwitchStart = null;
            if (_tmpModeSwitchStarts == null
                    || _tmpModeSwitchStarts.size() == 0)
                modeSwitchStart = modeSwitchEnd = _createNode(new Time(_module
                        .getDirector(), 0), TDLAction.AFTERMODESWITCH, mode);
            else {
                modeSwitchStart = _tmpModeSwitchStarts.get(new Time(_module
                        .getDirector(), 0.0));
                if (modeSwitchStart == null)
                    modeSwitchStart = (Node) _tmpModeSwitchStarts
                            .get(modePeriod);
            }
            if (_tmpModeSwitchEnds != null && _tmpModeSwitchEnds.size() != 0) { // no mode switches, only one mode
                modeSwitchEnd = (Node) _tmpModeSwitchEnds.get(new Time(_module
                        .getDirector(), 0.0));
                if (modeSwitchEnd == null)
                    modeSwitchEnd = (Node) _tmpModeSwitchEnds.get(modePeriod);
            }

            List<Node> outputPortNodes = null;
            Node invocationEndNode = modeSwitchEnd;
            for (long i = task.getOffset(); i < modePeriod.getLongValue(); i += task
                    .getInvocationPeriod()) {

                List<IOPort> notConnectedToSensors = new ArrayList();
                notConnectedToSensors.addAll(taskActor.inputPortList());
                List<Node> inputPortNodes = new ArrayList();
                for (ModalPort sensor : sensors) {
                    Node sensorNode = null, inputPortNode = null;
                    // if not already been read at this time instant 
                    if ((_tmpReadSensors.size() == 0)
                            || !((List) _tmpReadSensors.get(i))
                                    .contains(sensor)) {
                        sensorNode = _createNode(new Time(
                                _module.getDirector(), i),
                                TDLAction.READSENSOR, sensor, invocationEndNode);
                    } else {
                        sensorNode = getNode(
                                new Time(_module.getDirector(), i), sensor);
                    }
                    List<IOPort> inputPorts = taskActor.inputPortList();
                    for (IOPort inputPort : inputPorts) {
                        if (inputPort.isDeeplyConnected(sensor)) {
                            notConnectedToSensors.remove(inputPort);
                            inputPortNode = _createNode(new Time(_module
                                    .getDirector(), i), TDLAction.READINPUT,
                                    inputPort, sensorNode);
                            inputPortNodes.add(inputPortNode);
                            _registerTaskInputPortReading(new Time(_module
                                    .getDirector(), i), (IOPort) inputPort);
                            _connectToOtherTasksOutputPorts(inputPort);
                        }
                    }
                }

                Node taskExecutionNode = _createNode(new Time(_module
                        .getDirector(), i), TDLAction.EXECUTETASK, taskActor);
                for (IOPort notConnectedToSensor : notConnectedToSensors) {
                    Node inputPortNode = _createNode(new Time(_module
                            .getDirector(), i), TDLAction.READINPUT,
                            notConnectedToSensor, invocationEndNode);
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
                    outputPortNode = _createNode(writePortTime,
                            TDLAction.WRITEOUTPUT, outputPort,
                            taskExecutionNode);
                    outputPortNodes.add(outputPortNode);
                    _connectToIntermediateModeSwitch(outputPortNode, new Time(
                            _module.getDirector(), i), new Time(_module
                            .getDirector(), task.getInvocationPeriod()));
                }
                // need a single invocation end node
                if (outputPortNodes.size() == 0) {
                    invocationEndNode = taskExecutionNode;
                } else if (outputPortNodes.size() == 1) {
                    invocationEndNode = outputPortNodes.get(0);
                } else if (outputPortNodes.size() > 0) {
                    invocationEndNode = getNode(new Time(_module.getDirector(),
                            0), null);
                    if (invocationEndNode == null)
                        invocationEndNode = _createNode(new Time(_module
                                .getDirector(), i), TDLAction.AFTERTASKOUTPUTS,
                                null);
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
     * @param state
     * @param refinement
     * @param modePeriod
     * @throws IllegalActionException
     * @throws TDLModeSchedulerException
     */
    private void _getTransitions(State mode, Refinement refinement,
            Time modePeriod) {
        // sort transitions here - TODO: sort attribute, priority

        HashMap<Time, List<IOPort>> sensorsAndTransitions = new HashMap();

        List<TDLTransition> transitions = mode.nonpreemptiveTransitionList();
        for (TDLTransition transition : transitions) {
            int frequency = TDLModuleDirector
                    .getFrequency((NamedObj) transition);
            long invocationPeriod = modePeriod.getLongValue() / frequency;
            transition.invocationPeriod = invocationPeriod;
            for (int i = 1; i <= frequency; i++) {
                List l = (List) sensorsAndTransitions.get(i);
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
     * Return true if the character at position i in the string slotSeleciton is
     * a number.
     * 
     * @param slotSelection
     * @param i
     * @return
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
        List l = (List) _modeSwitches.get(state);
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

    /**
     * Node describing the next TDL action.
     */
    private Node _currentNode;

    private Collection<Node> _nextNodes;

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

    /**
     * Recursively compute the list of forward reachable nodes that only
     * depend on one previous node and are scheduled for the same 
     * time as the current node.
     * @param node Node from which the reachable nodes are computed from.
     * @param visited Already visited nodes to avoid loops.
     * @return List of forward reachable nodes.
     */
    private List<Node> getForwardReachableIndependentNodes(Node node,
            List<Node> visited) {
        if (visited.contains(node))
            return new ArrayList();
        else
            visited.add(node);
        List<Node> events = new ArrayList();
        List<Edge> edges = (List<Edge>) _graph.outputEdges(node);
        for (Edge edge : edges) {
            Node targetNode = edge.sink();
            if (_graph.inputEdgeCount(targetNode) == 1) {
                events.add(targetNode);
                if (((TDLAction) targetNode.getWeight()).actionType != TDLAction.READSENSOR
                        && ((TDLAction) targetNode.getWeight()).actionType != TDLAction.MODESWITCH
                        && ((TDLAction) targetNode.getWeight()).actionType != TDLAction.EXECUTETASK
                        && ((TDLAction) targetNode.getWeight()).actionType != TDLAction.AFTERMODESWITCH)
                    events.addAll(getForwardReachableIndependentNodes(
                            targetNode, visited));
            }
        }
        return events;
    }

    /**
     * Recursively compute the set of nodes reachable from a given node that depend
     * on more than one node or are scheduled to happen at a future time. Examples for
     * the latter are nodes describing the writing of an output port 
     * @param justExecuted Node that was just executed.
     * @param node Node from which the reachable Nodes are computed.
     * @param visited Already visited nodes, used to avoid loops.
     * @return Set of reachable nodes taht depend on more than one input port or 
     * contain actions that should happen later than the given node.
     */
    public HashMap<Node, List<TDLAction>> getNextJoinNodes(Node justExecuted,
            Node node, List<Node> visited) {
        if (visited.contains(node))
            return new HashMap();
        else
            visited.add(node);
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
                    if (!justExecuted.equals(backwardNode.source()))
                        actions.add((TDLAction) backwardNode.source()
                                .getWeight());
                }
                if (actions.size() > 0)
                    events.put(targetNode, actions);
                else
                    events.putAll(getNextJoinNodes(justExecuted, targetNode,
                            visited));
            } else {
                events.putAll(getNextJoinNodes(justExecuted, targetNode,
                        visited));
            }

        }
        return events;
    }

    /**
     * Returns all forward reachable nodes in the graph that are connected to the given node.
     * Those forward reachable nodes do not depend on other nodes and the actions defined in the
     * nodes are scheduled to happen at the same time as the action in the given node.
     * @param node Given node.
     * @return List of nodes that are forward reachable from the given node.
     */
    public List<Node> getEventsFollowingAction(Node node) {
        return getForwardReachableIndependentNodes(node, new ArrayList());
    }

}
