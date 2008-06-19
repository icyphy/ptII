package ptolemy.domains.tt.tdl.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.domains.fsm.modal.RefinementPort;
import ptolemy.domains.tt.kernel.LetTask;
import ptolemy.graph.Edge;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

/**
 * Build a graph containing all TDL actions.
 * @author Patricia Derler
 *
 */
public class TDLActionsGraph {

    /**
     * Create a new TDLActionsGraph. TODO: better to derive from graph?
     * @param module
     * @param controller
     */
    public TDLActionsGraph(TDLModule module, FSMActor controller) {
        this.controller = controller;
        this.module = module;
    }
    
    private FSMActor controller;
    private TDLModule module;
    private HashMap _modeSwitchStarts = new HashMap();
    private HashMap _modeSwitchEnds = new HashMap();
    private HashMap _readSensors = new HashMap();
    private HashMap _writtenTaskOutputPorts = new HashMap();
    private HashMap _readTaskInputPorts = new HashMap();
    private HashMap _connectedTaskPorts = new HashMap();
    
    /**
     * Build the graph.
     * @throws IllegalActionException
     */
    public void buildGraph() throws IllegalActionException {
        for (Iterator stateIterator = controller.entityList().iterator(); stateIterator.hasNext(); ) {
            State state = (State) stateIterator.next();
            Refinement refinement = (Refinement) state.getRefinement()[0];
            graph = new Graph();
            try {
                long modePeriod = (long) (TDLModuleDirector.getModePeriod(state) / controller.getDirector().getTimeResolution());
                
                _getTransitions(state, refinement, modePeriod);
                _getTasks(refinement, modePeriod);
                _getActuators(refinement, modePeriod);
                
                int i = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    

    /**
     * First read transitions and build subgraphs for the transitions.
     * @param state
     * @param refinement
     * @param modePeriod
     * @throws IllegalActionException
     * @throws TDLModeSchedulerException
     */
    private void _getTransitions(State state, Refinement refinement, long modePeriod) throws IllegalActionException, TDLModeSchedulerException {
        // sort transitions here
        
        HashMap transitions = new HashMap();
        
        for (Iterator it = state.nonpreemptiveTransitionList().iterator(); it.hasNext(); ) {
            TDLTransition transition = (TDLTransition) it.next();
            int frequency = TDLModeScheduler.getFrequency((NamedObj) transition);
            long invocationPeriod = modePeriod / frequency;
            for (int i = 0; i < frequency; i++) {
                List l = (List) transitions.get(i);
                if (l == null) {
                    l = new ArrayList(); 
                    transitions.put(i * invocationPeriod, l);
                } 
                for (Iterator sensorIt = transition.requiredSensors.iterator(); sensorIt.hasNext(); ) {
                    l.add(sensorIt.next());
                }
                l.add(transition);
            }
        }
        // now we have a list containing time stamp, sensors to be read and transitions to be read
        
        // create graphs 
        for (Iterator transListIt = transitions.keySet().iterator(); transListIt.hasNext(); ) {
            Node node = null, prev = null;
            long invocationTime = (Long) transListIt.next();
            List l = (List) transitions.get(invocationTime);
            for (Iterator it = l.iterator(); it.hasNext(); ) {
                Object o = it.next();
                if (! (o instanceof TDLTask))
                    continue;
                Actor actor = (Actor) o; 
                if (actor instanceof IOPort) {
                    node = new Node(new TDLGraphNode(invocationTime, TDLGraphNode.READSENSOR, actor));
                    _registerSensorReading(invocationTime, (IOPort) actor);
                } else // transition
                    node = new Node(new TDLGraphNode(invocationTime, TDLGraphNode.MODESWITCH, actor));
                graph.addNode(node);
                if (prev != null) {
                    Edge edge = new Edge(prev, node, 0);
                    graph.addEdge(edge);
                } else {
                    _modeSwitchStarts.put(invocationTime, node);
                }
            }
            _modeSwitchEnds.put(invocationTime, node);
        }
        // now we have transitions and sensor readings in the graph; the transitions for the same time are connected
        // graphStarts and graphEnds contain starts end ends of those subgraphs
        
    }
    
    /**
     * Save that a sensor value was read at a certain point in time.
     * invocationTime: sensor1, sensor2, ...
     */
    private void _registerSensorReading(long invocationTime, IOPort sensor) {
        List l = (List) _readSensors.get(invocationTime);
        if (l == null) {
            l = new ArrayList();
            _readSensors.put(invocationTime, l);
        }
        l.add(sensor);
    }

    /**
     * Save that a task output was written at a vertain point in time.
     * @param invocationTime
     * @param port
     */
    private void _registerTaskOutputPortWriting(long invocationTime, IOPort port)  {
        List l = (List) _writtenTaskOutputPorts.get(invocationTime);
        if (l == null) {
            l = new ArrayList();
            _writtenTaskOutputPorts.put(invocationTime, l);
        }
        l.add(port);
    }
    
    /**
     * Save the times a task input port was written
     * port: invocationTime1, invocationTime2, ...
     * @param invocationTime
     * @param port
     */
    private void _registerTaskInputPortReading(long invocationTime, IOPort port)  {
        List l = (List) _readTaskInputPorts.get(port);
        if (l == null) {
            l = new ArrayList();
            _readTaskInputPorts.put(port, l);
        }
        l.add(invocationTime);
    }

    /**
     * Create subgraphs for all tasks, interconnect them with mode switches and other tasks.
     * @param refinement
     * @param modePeriod
     * @throws TDLModeSchedulerException
     */
    private void _getTasks(Refinement refinement, long modePeriod) throws TDLModeSchedulerException {
        // schedule tasks
        
        Iterator taskIterator = refinement.entityList().iterator();
        while (taskIterator.hasNext()) {
            Actor actor = (Actor) taskIterator.next();
            
            // get sensors used as inputs for the task
            List sensors = null;
            for (Iterator inputIt = actor.inputPortList().iterator(); inputIt.hasNext(); ) {
                IOPort port = (IOPort) inputIt.next();
                sensors = new ArrayList();
                sensors.addAll(port.connectedPortList());
                sensors.retainAll(module.inputPortList());
            }
            
            LetTask task = _analyzeSlotSelection(actor, modePeriod);
                
            Node prev = (Node) _modeSwitchEnds.get(0); 
            for (long i = task.getOffset(); i < modePeriod; i += task.getInvocationPeriod()) {

                for (Iterator it = sensors.iterator(); it.hasNext(); ) {
                    // if not already been read at this time instant
                    IOPort sensor = (IOPort) it.next();
                    if (!((List)_readSensors.get(i)).contains(sensor))
                        prev = _createNode(i, TDLGraphNode.READSENSOR, sensor, prev);
                }
                
                for (Iterator it = actor.inputPortList().iterator(); it.hasNext(); ) {
                    IOPort taskInputPort = (IOPort) it.next();
                    prev = _createNode(i, TDLGraphNode.READINPUT, taskInputPort, prev);
                    _registerTaskInputPortReading(i, (IOPort) taskInputPort);
                    _connectToOtherTasksOutputPorts(taskInputPort);
                }
                prev = _createNode(i, TDLGraphNode.EXECUTETASK, actor, prev);
                for (Iterator it = actor.outputPortList().iterator(); it.hasNext(); ) {
                    IOPort port = (IOPort) it.next();
                    prev = _createNode(i + task.getLet(), TDLGraphNode.WRITEOUTPUT, port, prev);
                    _registerTaskOutputPortWriting(i, (IOPort) port);
                }
                _connectToIntermediateModeSwitch(prev, i, task.getInvocationPeriod());
            }
            
            // add an edge to the graphStarts elements
            Node modeSwitch = (Node) _modeSwitchStarts.get(0);
            Edge edge = new Edge(prev, modeSwitch, modePeriod - ((TDLGraphNode)prev.getWeight()).time);
            graph.addEdge(edge);
        }
        
        _addConnectionsBetweenTaskPorts();
    }
    
    /**
     * Add actuator updates to the graph.
     * @param refinement
     * @param modePeriod
     */
    private void _getActuators(Refinement refinement, long modePeriod) {
        Iterator portIterator = refinement.outputPortList().iterator();
        while (portIterator.hasNext())  {
            IOPort actuator = (IOPort) portIterator.next();
            
            int frequency = TDLModeScheduler.getFrequency((NamedObj) actuator);
            long invocationPeriod = modePeriod / frequency;
            
            IOPort connectedPort = null;
            
            // get connected task output ports
            for (Iterator it = actuator.connectedPortList().iterator(); it.hasNext(); ) {
                IOPort port = (IOPort) it.next();
                if (port.getContainer() instanceof TDLTask) {
                    connectedPort = port; // only one task, not multiple tasks, can write to one actuator!!
                }
            }
            
            if (connectedPort != null) {
                for (int i = 0; i < frequency; i++) {
                    long invocationTime = i * invocationPeriod;
                    
                    Node node = _getNodeForPortWrittenBetweenTime(connectedPort, 0, invocationTime);
                    Node next = null;
                    Edge edge = null;
                    for (Iterator edgesIt = graph.neighbors(node).iterator(); edgesIt.hasNext(); ) {
                        edge = (Edge) edgesIt.next();
                        if (edge.source().equals(node)) {
                            next = edge.sink();
                        }
                    }
                    
                    // remove edge between node and next and insert actuator in between
                    graph.removeEdge(edge);
                    Node n = new Node(new TDLGraphNode(invocationTime, TDLGraphNode.WRITEACTUATOR, actuator));
                    graph.addNode(n);
                    graph.addEdge(new Edge(node, n, 0));
                    graph.addEdge(new Edge(n, next, 0));
                }
            }
        }
    }
    
    /**
     * Get a node which is used for writing an output port. This output port is written in a certain time frame defined
     * by the two parameters lower and upper.
     * @param port
     * @param lower
     * @param upper
     * @return
     */
    private Node _getNodeForPortWrittenBetweenTime(IOPort port, long lower,
            long upper) {
        for (Iterator msIt = _writtenTaskOutputPorts.keySet().iterator(); msIt.hasNext(); ) {
            long invocationTime = (Long) msIt.next();
            List outputPorts = (List) _writtenTaskOutputPorts.get(invocationTime);
            long diff = Long.MAX_VALUE;
            if (outputPorts.contains(port) && lower <= invocationTime && invocationTime <= upper) {
                if (upper - invocationTime < diff) {
                    diff = upper - invocationTime; 
                    Node outNode = getNode(invocationTime, (Actor) port);
                    return outNode;
                }
            }
        }
        return null;
    }

    /**
     * Add the connections between task output ports and other tasks input ports.
     */
    private void _addConnectionsBetweenTaskPorts() {
        for (Iterator it = _connectedTaskPorts.keySet().iterator(); it.hasNext(); ) {
            IOPort inputPort = (IOPort) it.next();
            List outputPortsReadFrom = (List) _connectedTaskPorts.get(inputPort);
            for (Iterator outIt = outputPortsReadFrom.iterator(); outIt.hasNext(); ) {
                IOPort outputPort = (IOPort) outIt.next();
                
                // get times when input port is read
                List l = (List) _readTaskInputPorts.get(inputPort);
                
                // get time when mode switch is done before that
                for (int i = 0; i < l.size(); i++) {
                    long readTime = (Long) l.get(i);
                    
                    long modeSwitchTimeBeforeRead = 0; 
                    for (Iterator msIt = _modeSwitchEnds.keySet().iterator(); msIt.hasNext(); ) {
                        long invocationTime = (Long) msIt.next();
                        if (invocationTime < readTime && modeSwitchTimeBeforeRead < invocationTime)
                            modeSwitchTimeBeforeRead = invocationTime;
                    }
                    
                    // get time of writing the task output; this time is >= modeSwitchTime and <= inputPortReadTime
                    Node outNode = _getNodeForPortWrittenBetweenTime(outputPort, modeSwitchTimeBeforeRead, readTime);
                    if (outNode != null) {
                        Node inNode = getNode(readTime, (Actor) inputPort);
                        Edge edge = new Edge (outNode, inNode, 0); // TODO: outnode.time - innode.time
                        graph.addEdge(edge);
                    }
                }   
            }
        }
    }
    
    /**
     * Return the node that is used for the execution of an actor at a certain time.
     * @param invocationTime
     * @param actor
     * @return
     */
    private Node getNode(long invocationTime, Actor actor) {
        for (Iterator it = graph.nodes().iterator(); it.hasNext(); ) {
            Node node = (Node) it.next();
            TDLGraphNode gnode = (TDLGraphNode) node.getWeight();
            if (gnode.time == invocationTime && gnode.object.equals(actor))
                return node;
        }
        return null;
    }
    
    /**
     * Connect an input port to the output port of another task that this input port reads from.
     * @param taskInputPort
     */
    private void _connectToOtherTasksOutputPorts(IOPort taskInputPort) {
        for (Iterator it = taskInputPort.connectedPortList().iterator(); it.hasNext(); ) {
            IOPort port = (IOPort) it.next();
            Actor actor = (Actor) port.getContainer(); 
            // if input port reads from another tasks output port
            if (actor instanceof TDLTask && !actor.equals(taskInputPort.getContainer())) { 
                // if task output port is written and there is no mode switch in between, add an edge
                // this information is not clear yet, these connections can only be added in the end
                // store the ports and add the conections later
                List l = (List) _connectedTaskPorts.get(taskInputPort);
                if (l == null) {
                    l = new ArrayList();
                    _readSensors.put(taskInputPort, l);
                }
                l.add(port);
            }
        }
    }
    
    /**
     * Connect a node to the next mode switch in time.
     * @param prev
     * @param taskInvocationTime
     * @param taskInvocationPeriod
     */
    private void _connectToIntermediateModeSwitch(Node prev, long taskInvocationTime, long taskInvocationPeriod) {
        // if there is a mode switch between now and the next invocation, add edge to graphStart and graphEnd
        long nextModeSwitchTime = 0, modeSwitchTimeBeforeNextTaskInvocation = 0;
        for (Iterator it = _modeSwitchStarts.keySet().iterator(); it.hasNext(); ) {
            long invocationTime = (Long) it.next();
            if (invocationTime > taskInvocationTime && 
                    invocationTime < taskInvocationTime + taskInvocationPeriod) {
                nextModeSwitchTime = invocationTime;
                modeSwitchTimeBeforeNextTaskInvocation = invocationTime;
            }
                
        }
        
        Node modeSwitch = (Node) _modeSwitchStarts.get(nextModeSwitchTime);
        if ( modeSwitch != null) {
            Edge edge = new Edge(prev, modeSwitch, 0);
            graph.addEdge(edge);
            prev = (Node) _modeSwitchEnds.get(modeSwitchTimeBeforeNextTaskInvocation);
        }
    }
   
    /**
     * Create a new node.
     * @param invocationTime
     * @param actionType
     * @param actor
     * @param previous
     * @return
     */
    private Node _createNode(long invocationTime, int actionType, Object actor, Node previous) {
        Node node = new Node(new TDLGraphNode(invocationTime, actionType, actor));
        graph.addNode(node);  
        if (previous == null) { // this can only happen if there are no mode switches
            _modeSwitchStarts.put(invocationTime, node);
            _modeSwitchEnds.put(invocationTime, node);
        } else {
            Edge edge = new Edge(previous, node, ((TDLGraphNode)node.getWeight()).time - ((TDLGraphNode)previous.getWeight()).time);
            graph.addEdge(edge); 
        }
        return node;
    }
    
    /**
     * Analyze the slot selection string.
     * @param actor
     * @param modePeriod
     * @return
     * @throws TDLModeSchedulerException
     */
    private LetTask _analyzeSlotSelection(Actor actor, long modePeriod)
            throws TDLModeSchedulerException {
        String slots = TDLModeScheduler.getSlots((NamedObj) actor);
        int frequency = TDLModeScheduler.getFrequency((NamedObj) actor);
        ArrayList invocations = _getInvocations(slots, frequency);
        // if task is periodic, it is a let task. otherwise schedule it as a special action
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
                        newInv = (Integer) invocations.get(0) + frequency;
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
            ArrayList list = new ArrayList();
            return new LetTask(actor, let, inv, offset);
        } else { // schedule single task as a set of tasks with different lets and invocation periods
            throw new TDLModeSchedulerException("Task is not periodic");
        }
    }
    
    /**
     * Analyze the slot seleciton string.
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
                if (nextCharIsANumber(slotSelection, i))
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
        // update invocations so that 0-values which came from not initializing secondSlot are filled
        for (int i = 0; i < invocations.size(); i += 2) {
            if ((Integer) invocations.get(i + 1) == 0) {
                invocations.add(i + 1, (Integer) invocations.get(i) + 1);
                invocations.remove(i + 2);
            }
        }
        return invocations;
    }
    
    /**
     * Return true if the character at position i in the string slotSeleciton is a number.
     * @param slotSelection
     * @param i
     * @return
     */
    private boolean nextCharIsANumber(String slotSelection, int i) {
        return slotSelection.length() > i + 1
            && slotSelection.charAt(i + 1) > 47
            && slotSelection.charAt(i + 1) < 58;
    }
    
    /**
     * Graph containing all TDL actions.
     */
    private Graph graph;
    
}
