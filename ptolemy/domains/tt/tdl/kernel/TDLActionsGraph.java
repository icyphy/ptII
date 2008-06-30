package ptolemy.domains.tt.tdl.kernel;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.modal.ModalPort;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.domains.tt.kernel.LetTask;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * Build a graph containing all TDL actions.
 * 
 * TODO: better to derive from graph?
 * 
 * @author Patricia Derler
 * 
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
     * Build the graph.
     * 
     * @throws IllegalActionException
     */
    public void buildGraph(State startmode) throws IllegalActionException {
        _graph = new DirectedGraph();
        for (Iterator stateIterator = _controller.entityList().iterator(); stateIterator
                .hasNext();) {
            State state = (State) stateIterator.next();
            Refinement refinement = (Refinement) state.getRefinement()[0]; 
            try {
                long modePeriod = (long) (TDLModuleDirector
                        .getModePeriod(state) / _controller.getDirector()
                        .getTimeResolution());
                _getTransitions(state, refinement, modePeriod);
                _getTasks(refinement, modePeriod);
                _getActuators(refinement, modePeriod);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // reset all temporary lists, only store mode switches. those are
            // the connection points between modes
            _resetsTempVars();
        }
        _connectModes();
        _printGraph();
        if (_modeSwitches.size() > 0) {
            _currentNode = (Node) ((Object[]) ((ArrayList)_modeSwitches.get(startmode)).get(0))[1];
        } else 
            _currentNode = null; //??
    }
    
    /**
     * 
     * @param nextMode Is null except for if a mode switch was taken.
     * @throws IllegalActionException
     */
    public void nextNodes(State nextMode) throws IllegalActionException {
        Collection sinkNodes = _graph.successors(_currentNode);
        if (sinkNodes.size() == 0) {
            throw new IllegalActionException("Graph Error: no sink nodes from node " + _currentNode);
        } else if (sinkNodes.size() == 1) {
            _nextNodes.add((Node) sinkNodes.iterator().next());
        } else {
            // current node is task output port that is connected to
            // another tasks input port
            if (((TDLAction)_currentNode.getWeight()).actionType == TDLAction.WRITEOUTPUT) {
                _nextNodes.addAll(sinkNodes);
            } 
            // current node is transition 
            else if (((TDLAction)_currentNode.getWeight()).actionType == TDLAction.AFTERMODESWITCH &&
                    nextMode != null) {
                _nextNodes.add((Node) ((Object[])_modeSwitches.get(nextMode))[1]);
            } else {
                throw new IllegalActionException("Graph Error: multiple sink nodes but not an output port or mode switch");
            } 
        }
    }

    private void _printGraph() {
        System.out.println(_graph.toString());
    }

    /**
     * Add the connections between task output ports and other tasks input
     * ports.
     */
    private void _addConnectionsBetweenTaskPorts() {
        for (Iterator it = _tmpConnectedTaskPorts.keySet().iterator(); it
                .hasNext();) {
            IOPort inputPort = (IOPort) it.next();
            List outputPortsReadFrom = (List) _tmpConnectedTaskPorts
                    .get(inputPort);
            for (Iterator outIt = outputPortsReadFrom.iterator(); outIt
                    .hasNext();) {
                IOPort outputPort = (IOPort) outIt.next();

                // get times when input port is read
                List l = (List) _tmpReadTaskInputPorts.get(inputPort);

                // get time when mode switch is done before that
                for (int i = 0; i < l.size(); i++) {
                    long readTime = (Long) l.get(i);

                    long modeSwitchTimeBeforeRead = 0;
                    for (Iterator msIt = _tmpModeSwitchEnds.keySet().iterator(); msIt
                            .hasNext();) {
                        long invocationTime = (Long) msIt.next();
                        if (invocationTime < readTime
                                && modeSwitchTimeBeforeRead < invocationTime)
                            modeSwitchTimeBeforeRead = invocationTime;
                    }

                    // get time of writing the task output; this time is >=
                    // modeSwitchTime and <= inputPortReadTime
                    Node outNode = _getNodeForPortWrittenBetweenTime(
                            outputPort, modeSwitchTimeBeforeRead, readTime);
                    if (outNode != null) {
                        Node inNode = _getNode(readTime, (Actor) inputPort);
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
     * @throws TDLModeSchedulerException
     */
    private LetTask _analyzeSlotSelection(TDLTask actor, long modePeriod)
            throws TDLModeSchedulerException {
        String slots = TDLModeScheduler.getSlots((NamedObj) actor);
        int frequency = TDLModeScheduler.getFrequency((NamedObj) actor);
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
            let = modePeriod / let;
            inv = modePeriod / inv;
            
            actor.let = let;
            actor.invocationPeriod = inv;
            actor.offset = offset;
            return new LetTask(actor, let, inv, offset); 
        } else { // schedule single task as a set of tasks with different
                    // lets and invocation periods
            throw new TDLModeSchedulerException("Task " + actor.getName()
                    + " is not periodic, slot selection string: " + slots);
        }
    }

    private void _connectModes() {
        // connect mode switches of all modes.
        for (Iterator modeIt = _modeSwitches.keySet().iterator(); modeIt
                .hasNext();) {
            State mode = (State) modeIt.next();

            for (Iterator nodePairsIt = ((List) _modeSwitches.get(mode))
                    .iterator(); nodePairsIt.hasNext();) {
                Object[] nodePair = (Object[]) nodePairsIt.next();

                Node sourceModeNode = (Node) nodePair[0];
                TDLTransition transition = (TDLTransition) ((TDLAction) sourceModeNode
                        .getWeight()).object;
                State targetMode = transition.destinationState();

                for (Iterator targEdgeIt = ((List) _modeSwitches
                        .get(targetMode)).iterator(); targEdgeIt.hasNext();) {
                    Object[] nodePair2 = (Object[]) targEdgeIt.next();
                    Node targetModeNode = (Node) nodePair2[1];
                    if (((TDLAction) targetModeNode.getWeight()).time == 0) {
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
            long taskInvocationTime, long taskInvocationPeriod) {
        // if there is a mode switch between now and the next invocation, add
        // edge to graphStart and graphEnd
        long nextModeSwitchTime = 0, modeSwitchTimeBeforeNextTaskInvocation = 0;
        for (Iterator it = _tmpModeSwitchStarts.keySet().iterator(); it
                .hasNext();) {
            long invocationTime = (Long) it.next();
            if (invocationTime > taskInvocationTime
                    && invocationTime < taskInvocationTime
                            + taskInvocationPeriod) {
                nextModeSwitchTime = invocationTime;
                modeSwitchTimeBeforeNextTaskInvocation = invocationTime;
            }

        }

        Node modeSwitch = (Node) _tmpModeSwitchStarts.get(nextModeSwitchTime);
        if (modeSwitch != null) {
            Edge edge = new Edge(prev, modeSwitch, 0);
            _graph.addEdge(edge);
            prev = (Node) _tmpModeSwitchEnds
                    .get(modeSwitchTimeBeforeNextTaskInvocation);
        }
    }

    /**
     * Connect an input port to the output port of another task that this input
     * port reads from.
     * 
     * @param taskInputPort
     */
    private void _connectToOtherTasksOutputPorts(IOPort taskInputPort) {
        for (Iterator it = taskInputPort.connectedPortList().iterator(); it
                .hasNext();) {
            IOPort port = (IOPort) it.next();
            Actor actor = (Actor) port.getContainer();
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
                    _tmpReadSensors.put(taskInputPort, l);
                }
                l.add(port);
            }
        }
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
    private Node _createNode(long invocationTime, int actionType, Object actor,
            Node previous) {
        Node node = new Node(
                new TDLAction(invocationTime, actionType, actor));
        _graph.addNode(node);
        if (previous == null) { // this can only happen if there are no mode
                                // switches
            _tmpModeSwitchStarts.put(invocationTime, node);
            _tmpModeSwitchEnds.put(invocationTime, node);
        } else {
            Edge edge = new Edge(previous, node, ((TDLAction) node
                    .getWeight()).time
                    - ((TDLAction) previous.getWeight()).time);
            _graph.addEdge(edge);
        }
        return node;
    }

    /**
     * Add actuator updates to the graph.
     * 
     * @param refinement
     * @param modePeriod
     */
    private void _getActuators(Refinement refinement, long modePeriod) {
        Iterator portIterator = refinement.outputPortList().iterator();
        while (portIterator.hasNext()) {
            IOPort actuator = (IOPort) portIterator.next();

            int frequency = TDLModeScheduler.getFrequency((NamedObj) actuator);
            long invocationPeriod = modePeriod / frequency;

            IOPort connectedPort = null;

            // get connected task output ports
            for (Iterator it = actuator.connectedPortList().iterator(); it
                    .hasNext();) {
                IOPort port = (IOPort) it.next();
                if (port.getContainer() instanceof TDLTask) {
                    connectedPort = port; // only one task, not multiple
                                            // tasks, can write to one
                                            // actuator!!
                }
            }

            if (connectedPort != null) {
                for (int i = 0; i < frequency; i++) {
                    long invocationTime = i * invocationPeriod;

                    Node node = _getNodeForPortWrittenBetweenTime(
                            connectedPort, 0, invocationTime);
                    Node next = null;
                    Edge edge = null;
                    for (Iterator edgesIt = _graph.neighbors(node).iterator(); edgesIt
                            .hasNext();) {
                        edge = (Edge) edgesIt.next();
                        if (edge.source().equals(node)) {
                            next = edge.sink();
                        }
                    }

                    // remove edge between node and next and insert actuator in
                    // between
                    _graph.removeEdge(edge);
                    Node n = new Node(new TDLAction(invocationTime,
                            TDLAction.WRITEACTUATOR, actuator));
                    _graph.addNode(n);
                    _graph.addEdge(new Edge(node, n, 0));
                    _graph.addEdge(new Edge(n, next, 0));
                }
            }
        }
    }

    /**
     * Analyze the slot seleciton string.
     * 
     * @param slots
     * @param frequency
     * @return
     * @throws TDLModeSchedulerException
     */
    private ArrayList _getInvocations(String slots, int frequency)
            throws TDLModeSchedulerException {
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
                throw new TDLModeSchedulerException("'" + slots
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
     * Return the node that is used for the execution of an actor at a certain
     * time.
     * 
     * @param invocationTime
     * @param actor
     * @return
     */
    private Node _getNode(long invocationTime, Object actor) {
        for (Iterator it = _graph.nodes().iterator(); it.hasNext();) {
            Node node = (Node) it.next();
            TDLAction gnode = (TDLAction) node.getWeight();
            if (gnode.time == invocationTime && gnode.object.equals(actor))
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
    private Node _getNodeForPortWrittenBetweenTime(IOPort port, long lower,
            long upper) {
        for (Iterator msIt = _tmpWrittenTaskOutputPorts.keySet().iterator(); msIt
                .hasNext();) {
            long invocationTime = (Long) msIt.next();
            List outputPorts = (List) _tmpWrittenTaskOutputPorts
                    .get(invocationTime);
            long diff = Long.MAX_VALUE;
            if (outputPorts.contains(port) && lower <= invocationTime
                    && invocationTime <= upper) {
                if (upper - invocationTime < diff) {
                    diff = upper - invocationTime;
                    Node outNode = _getNode(invocationTime, (Actor) port);
                    return outNode;
                }
            }
        }
        return null;
    }

    /**
     * Create subgraphs for all tasks, interconnect them with mode switches and
     * other tasks.
     * 
     * @param refinement
     * @param modePeriod
     * @throws TDLModeSchedulerException
     */
    private void _getTasks(Refinement refinement, long modePeriod)
            throws TDLModeSchedulerException {
        // schedule tasks

        Iterator taskIterator = refinement.entityList().iterator();
        while (taskIterator.hasNext()) {
            TDLTask actor = (TDLTask) taskIterator.next();
            LetTask task = _analyzeSlotSelection(actor, modePeriod); 
            Collection sensors = actor.getSensorsReadFrom(refinement.inputPortList(), 
                    _module.inputPortList());

            Node prev = (Node) _tmpModeSwitchEnds.get(0);
            for (long i = task.getOffset(); i < modePeriod; i += task
                    .getInvocationPeriod()) {

                for (Iterator it = sensors.iterator(); it.hasNext();) {
                    // if not already been read at this time instant
                    IOPort sensor = (IOPort) it.next();
                    if (!((List) _tmpReadSensors.get(i)).contains(sensor)) { 
                        prev = _createNode(i, TDLAction.READSENSOR, sensor,
                                prev);
                    } else {
                        Node node = _getNode(i, sensor);
                        if (!_graph.edgeExists(prev, node)) {
                            Edge edge = new Edge(prev, node);
                            _graph.addEdge(edge);
                        }
                        prev = node;
                    }
                }

                for (Iterator it = actor.inputPortList().iterator(); it
                        .hasNext();) {
                    IOPort taskInputPort = (IOPort) it.next();
                    prev = _createNode(i, TDLAction.READINPUT,
                            taskInputPort, prev);
                    _registerTaskInputPortReading(i, (IOPort) taskInputPort);
                    _connectToOtherTasksOutputPorts(taskInputPort);
                }
                prev = _createNode(i, TDLAction.EXECUTETASK, actor, prev);
                for (Iterator it = actor.outputPortList().iterator(); it
                        .hasNext();) {
                    IOPort port = (IOPort) it.next();
                    prev = _createNode(i + task.getLet(),
                            TDLAction.WRITEOUTPUT, port, prev);
                    _registerTaskOutputPortWriting(i, (IOPort) port);
                }
                _connectToIntermediateModeSwitch(prev, i, task
                        .getInvocationPeriod());
            }

            // add an edge to the graphStarts elements
            Node modeSwitch = (Node) _tmpModeSwitchStarts.get(new Long(0));
            Edge edge = new Edge(prev, modeSwitch, modePeriod
                    - ((TDLAction) prev.getWeight()).time);
            _graph.addEdge(edge);
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
    private void _getTransitions(State state, Refinement refinement,
            long modePeriod) throws IllegalActionException,
            TDLModeSchedulerException {
        // sort transitions here - TODO: sort attribute, priority

        HashMap sensorsAndTransitions = new HashMap();

        for (Iterator it = state.nonpreemptiveTransitionList().iterator(); it
                .hasNext();) {
            TDLTransition transition = (TDLTransition) it.next();
            int frequency = TDLModeScheduler
                    .getFrequency((NamedObj) transition);
            long invocationPeriod = modePeriod / frequency;
            transition.invocationPeriod = invocationPeriod;
            for (int i = 0; i < frequency; i++) {
                List l = (List) sensorsAndTransitions.get(i);
                if (l == null) {
                    l = new ArrayList();
                    sensorsAndTransitions.put(i * invocationPeriod, l);
                }
                for (Iterator sensorIt = transition.requiredSensors.iterator(); sensorIt
                        .hasNext();) {
                    l.add(sensorIt.next());
                }
                l.add(transition);
            }
        }
        // now we have a list containing time stamp, sensors to be read and
        // transitions to be read

        // create graphs
        for (Iterator transListIt = sensorsAndTransitions.keySet().iterator(); transListIt
                .hasNext();) {
            Node prev = null;
            long invocationTime = (Long) transListIt.next();
            Node endNode = new Node(new TDLAction(invocationTime,
                    TDLAction.AFTERMODESWITCH, null));
            _graph.addNode(endNode);
            List l = (List) sensorsAndTransitions.get(invocationTime);
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
                    _registerModeSwitch(state, node, endNode);
                }
                if (prev != null) {
                    Edge edge = new Edge(prev, node, 0);
                    _graph.addEdge(edge);
                } else {
                    _tmpModeSwitchStarts.put(invocationTime, node);
                }
                prev = node;
            }

            Edge edge = new Edge(prev, endNode, 0);
            _graph.addEdge(edge);
            // _modeSwitchEnds.put(invocationTime, node);
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
     * @param invocationTime Time the sensor is read.
     * @param sensor Sensor that is read.
     */
    private void _registerSensorReading(long invocationTime, IOPort sensor) {
        List l = (List) _tmpReadSensors.get(invocationTime);
        if (l == null) {
            l = new ArrayList();
            _tmpReadSensors.put(invocationTime, l);
        }
        l.add(sensor);
    }

    /**
     * Save the times a task input port was written.
     * 
     * @param invocationTime Time the port is read.
     * @param port Port that is read.
     */
    private void _registerTaskInputPortReading(long invocationTime, IOPort port) {
        List l = (List) _tmpReadTaskInputPorts.get(port);
        if (l == null) {
            l = new ArrayList();
            _tmpReadTaskInputPorts.put(port, l);
        }
        l.add(invocationTime);
    }

    /**
     * Save that a task output was written at a certain point in time.
     * 
     * @param invocationTime
     *            Time the port is written.
     * @param port
     *            Port that is written.
     */
    private void _registerTaskOutputPortWriting(long invocationTime, IOPort port) {
        List l = (List) _tmpWrittenTaskOutputPorts.get(invocationTime);
        if (l == null) {
            l = new ArrayList();
            _tmpWrittenTaskOutputPorts.put(invocationTime, l);
        }
        l.add(port);
    }

    /**
     * Reset temporary variables used to create the graph for one mode.
     */
    private void _resetsTempVars() {
        _tmpReadSensors.clear();
        _tmpWrittenTaskOutputPorts.clear();
        _tmpReadTaskInputPorts.clear();
        _tmpConnectedTaskPorts.clear();
        _tmpModeSwitchStarts.clear();
        _tmpModeSwitchEnds.clear();
    }
    
    /**
     * Node describing the next TDL action.
     */
    private Node _currentNode;
    
    private Collection _nextNodes;

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
     * mode1: modeSwitch1 : Object[](entry, exit) 
     *        modeSwtich2 : Object[]entry, exit) 
     * mode2: ...
     */
    private HashMap _modeSwitches = new HashMap();

    /**
     * The TDL module.
     */
    private TDLModule _module;
    
    /**
     * Store the connection between output ports of one task with input ports
     * of another task.
     */
    private HashMap _tmpConnectedTaskPorts = new HashMap();

    /**
     * Store end of mode switch subgraphs. A mode switch subgraph consists of 
     * sensors that are read and the mode switch.
     */
    private HashMap _tmpModeSwitchEnds = new HashMap();

    /**
     * Store starts of mode switch subgraphs. A mode switch subgraph consists of 
     * sensors that are read and the mode switch.
     */
    private HashMap _tmpModeSwitchStarts = new HashMap();
    
    /**
     * Store the times sensors are read. This is used to only read a sensor
     * once at a time. If a sensor is read before a mode switch, it should not
     * be read again before executing tasks at the same time instant in order
     * to preserve determinism.
     */
    private HashMap _tmpReadSensors = new HashMap();
    
    /**
     * Store the times task output ports are written.
     */
    private HashMap _tmpWrittenTaskOutputPorts = new HashMap();
    
    /**
     * Store the times task input ports are read.
     */
    private HashMap _tmpReadTaskInputPorts = new HashMap();
}
