/* A static scheduler for the continuous time domain.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTScheduler
/**
Static scheduler for the CT domain.
A CT (sub)system can be mathematically represented as:<Br>
<pre>
<pre>    dx/dt = f(x, u, t)<Br>
<pre>    y = g(x, u, t)<BR>
</pre></pre></pre>
where x is the state of the system, u is the input, y is the output,
f() is the state transition map and g() is the output map.
<P>
The system is built using actors. That is, all the functions, f() and g(),
are built up by chains of actors.  For higher order systems,
x is a vector, built using more than one integrator.
In general, actors that have the functionality of integration
from their inputs to their outputs are called <I>dynamic actors</I>.
Other actors are called <I>arithmetic actors</I>.
<P>
In order to interact with discrete domains, some actors in the
CT domain are able to convert continuous waveforms to discrete events,
and vice versa. An actor that has continuous input and discrete
output is call an <I>event generator</I>; an actor that has
discrete input and continuous output is called a
<I>waveform generator</I>.
<P>
The interaction with some discrete domains requires that the
CT simulation be able to remember its state and roll-back
to the remembered state when needed. This in turn requires
that all actors which have internal states should be able
to remember and restore their states. These actors are called
<I>stateful actors</I>.
<P>
In the continuous time simulation, time progresses in a discrete way.
The distance between consecutive simulation time points is called the
<I>integration step size</I> or step size, for short. Some actors
need to be able to control the step sizes in the simulation. These actors
are called <I>step size control actors</I>. Examples of step size
control actors include integrators, which control the
accuracy and speed of numerical ODE solutions, and some event
generators, which detect events.
<P>
To help with scheduling, the actors are partitioned into several clusters:
the <I>arithmetic actors</I>, the <I>dynamic actors</I>,
the <I>step size control actors</I>, the <I>sink actors</I>,
the <I>stateful actors</I>, the <I> event generators</I>,
and the <I> waveform generators</I>.
This scheduler uses the clustered information and the system topology,
to provide the firing sequences for evaluating f() and g().
It also provides a firing order for all the dynamic actors.
The firing sequence for evaluating f() is
called the <I> state transition schedule</I>; the firing
sequence for evaluating g() is called the <I>output schedule</I>;
and the firing sequence for dynamic actors is called the
<I>dynamic actor schedule</I>.
<P>
The state transition schedule is the actors in the f() function sorted
in the topological order, such that, after the integrators emit their
state x, a chain of firings according to the schedule evaluates the
f() function and returns tokens corresponding to dx/dt to the
integrators.
<P>
The output schedule is the actors in the g() function sorted in
their topological order.
<P>
The dynamic actor schedule is a list of dynamic actors in their reverse
topological order.
<P>
If there are loops of arithmetic actors or loops of integrators,
then the (sub)system are not schedulable, and a NotSchedulableException
will be thrown if schedules are requested.

@author Jie Liu
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.actor.sched.Scheduler
*/

public class CTScheduler extends Scheduler {

    /** Construct a CT scheduler in the default workspace
     *  with the default name "CTScheduler". There is no director
     *  containing this scheduler. To attach this scheduler to a
     *  CTDirector, call setScheduler() on the CTDirector.
     */
    public CTScheduler() {
        this(null);
    }

    /** Construct a CT scheduler in the given workspace
     *  with the name "CTScheduler". There is no director
     *  containing this scheduler. To attach this scheduler to a
     *  CTDirector, call setScheduler() on the CTDirector.
     *
     *  @param workspace The workspace.
     */
    public CTScheduler(Workspace workspace) {
        super(workspace);
        try {
            setName(_STATIC_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    "Internal error when setting name to a CTScheduler");
        }
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CTScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** These are syntactic sugar for signal types, which are defined in
     *  CTReceiver.
     *  Signal type: CONTINUOUS.
     */
    public static final CTReceiver.SignalType CONTINUOUS =
    CTReceiver.CONTINUOUS;

    /**  Signal type: DISCRETE.
     */
    public static final CTReceiver.SignalType DISCRETE =
    CTReceiver.DISCRETE;

    /**  Signal type: UNKNOWN.
     */
    public static final CTReceiver.SignalType UNKNOWN =
    CTReceiver.UNKNOWN;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the specified actor is in the continuous cluster of
     *  the model.
     *  @param actor The specified actor.
     *  @return True if the actor is a continuous actor.
     *  @exception IllegalActionException If this method is called before
     *  initialization, where the schedule is computed.
     */
    public boolean isContinuous(Actor actor) throws IllegalActionException {
        if (_signalTypeMap == null) {
            throw new IllegalActionException(this, " isContinuous() can only "
                    + "be called after initialization.");
        }
        List continuousActors = _signalTypeMap.getContinuousActors();
        return continuousActors.contains(actor);
    }


    /** Return true if the specified actor is in the discrete cluster of
     *  the model.
     *  @param actor The specified actor.
     *  @return True if the actor is a discrete actor.
     *  @exception IllegalActionException If this method is called before
     *  initialization, where the schedule is computed.
     */
    public boolean isDiscrete(Actor actor) throws IllegalActionException {
        if (_signalTypeMap == null) {
            throw new IllegalActionException(this, " isDiscrete() can only "
                    + "be called after initialization.");
        }
        List discreteActors = _signalTypeMap.getDiscreteActors();
        return discreteActors.contains(actor);
    }

    /** Return the predecessors of the given actor in the topology within
     *  this opaque composite actor.
     *  If the argument is null, returns null.
     *  If the actor is a source, returns an empty list.
     *  @param actor The specified actor.
     *  @return The list of predecessors, unordered.
     */
    public List predecessorList(Actor actor) {
        if (actor == null) {
            return null;
        }
        LinkedList predecessors = new LinkedList();
        Iterator inPorts = actor.inputPortList().iterator();
        while (inPorts.hasNext()) {
            IOPort port = (IOPort) inPorts.next();
            Iterator outPorts = port.deepConnectedOutPortList().iterator();
            while (outPorts.hasNext()) {
                IOPort outPort = (IOPort)outPorts.next();
                Actor pre = (Actor)outPort.getContainer();
                // NOTE: This could be done by using
                // NamedObj.depthInHierarchy() instead of comparing the
                // executive directors, but its tested this way, so we
                // leave it alone.
                if ((actor.getExecutiveDirector()
                        == pre.getExecutiveDirector()) &&
                        !predecessors.contains(pre)) {
                    predecessors.addLast(pre);
                }
            }
        }
        return predecessors;
    }

    /** Return the SignalType as a String */
    public String signalTypeToString(CTReceiver.SignalType signalType) {
        if (signalType == CONTINUOUS) {
            return "CONTINUOUS";
        } else if (signalType == DISCRETE) {
            return "DISCRETE";
        } else if (signalType == UNKNOWN) {
            return "UNKNOWN";
        }
        return "INVALID:" + signalType + " is invalid";
    }


    /** Return the successive actors of the given actor in the topology.
     *  If the argument is null, returns null.
     *  If the actor is a sink, returns an empty list.
     *  @param actor The specified actor. If the actor is null, returns null.
     *  @return The enumerations of predecessors.
     */
    public List successorList(Actor actor) {
        if (actor == null) {
            return null;
        }
        LinkedList successors = new LinkedList();
        Iterator outports = actor.outputPortList().iterator();
        while (outports.hasNext()) {
            IOPort outPort = (IOPort) outports.next();
            Iterator inPorts = outPort.deepConnectedInPortList().iterator();
            while (inPorts.hasNext()) {
                IOPort inPort = (IOPort)inPorts.next();
                Actor post = (Actor)inPort.getContainer();
                // NOTE: This could be done by using
                // NamedObj.depthInHierarchy() instead of comparing the
                // executive directors, but its tested this way, so we
                // leave it alone.
                if ((actor.getExecutiveDirector()
                        == post.getExecutiveDirector()) &&
                        !successors.contains(post)) {
                    successors.addLast(post);
                }
            }
        }
        return successors;
    }

    /** Return all the scheduling information in a String.
     *  @return All the schedules.
     */
    public String toString() {
        return getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the CTSchedule. Caching of the schedule is done
     *  in the director class, so this method does not test
     *  for the validation of the schedule.
     */
    protected Schedule _getSchedule() throws NotSchedulableException,
            IllegalActionException {
        // This implementation creates new Lists every time,
        // If this hurts performance a lot, consider reuse old lists.
        // That requires Schedule class to implement clear().
        CTSchedule ctSchedule = new CTSchedule();

        _signalTypeMap = new SignalTypeMap();

        LinkedList sinkActors = new LinkedList();
        LinkedList dynamicActors = new LinkedList();
        LinkedList discreteActors = new LinkedList();
        LinkedList arithmeticActors = new LinkedList();
        LinkedList eventGenerators = new LinkedList();
        LinkedList waveformGenerators = new LinkedList();
        LinkedList continuousActors = new LinkedList();

        Schedule discreteActorSchedule = new Schedule();
        Schedule continuousActorSchedule = new Schedule();
        Schedule dynamicActorSchedule = new Schedule();
        Schedule eventGeneratorSchedule = new Schedule();
        Schedule outputSchedule = new Schedule();
        Schedule outputSSCActors = new Schedule();
        Schedule stateTransitionSchedule = new Schedule();
        Schedule statefulActors = new Schedule();
        Schedule stateSSCActors = new Schedule();
        Schedule waveformGeneratorSchedule = new Schedule();

        // classify actors and fill in unordered schedules.
        CompositeActor container =
            (CompositeActor)getContainer().getContainer();

        // Examin and propagate composite port signal types.
        boolean isCTComposite = false;
        if (container instanceof CTStepSizeControlActor) {
            isCTComposite = true;
        }
        Iterator containerInPorts = container.inputPortList().iterator();
        while (containerInPorts.hasNext()) {
            IOPort inPort = (IOPort) containerInPorts.next();
            //System.out.println("Examine and propagate type from port" +
            //    inPort.getFullName());
            if (!isCTComposite) {
                _signalTypeMap.setType(inPort, DISCRETE);
            } else {
                // examine the parameters.
                Parameter signalType =
                    (Parameter)inPort.getAttribute("signalType");
                if (signalType != null) {
                    String type = ((StringToken)signalType.getToken()).
                        stringValue();
                    type = type.trim().toUpperCase();
                    if (type.equals("CONTINUOUS")) {
                        _signalTypeMap.setType(inPort, CONTINUOUS);
                    } else if (type.equals("DISCRETE")) {
                        _signalTypeMap.setType(inPort, DISCRETE);
                    } else {
                        throw new IllegalActionException(inPort,
                                "Unrecognized signal type. "
                                + "It should be a String of "
                                + "either \"CONTINUOUS\" or \"DISCRETE\".");
                    }
                } else {
                    // The default of CTCompositeActor is continuous.
                    _signalTypeMap.setType(inPort, CONTINUOUS);
                }
            }
            _signalTypeMap.propagateTypeInside(inPort);
        }


        // We clone the list of all actors and will remove discrete actors
        // from them.
        continuousActors = (LinkedList)
            ((LinkedList)container.deepEntityList()).clone();

        Iterator allActors = container.deepEntityList().iterator();
        while (allActors.hasNext()) {

            Actor a = (Actor) allActors.next();
            //System.out.println("examine " + ((Nameable)a).getFullName());

            // Now classify actors by their interfaces.
            // Event generators are treated as sinks, and
            // waveform generators are treated as sources.
            // Note that this breaks some causality loops.

            if (a instanceof CTStatefulActor) {
                statefulActors.add(new Firing(a));
            }
            if (a instanceof CTWaveformGenerator) {
                waveformGenerators.add(a);
            }
            if (a instanceof CTEventGenerator) {
                eventGenerators.add(a);
            }

            if (a instanceof CTDynamicActor) {
                dynamicActors.addLast(a);
            }else if (!(a instanceof CTWaveformGenerator)) {
                arithmeticActors.add(a);
            }

            // Now resolve signal types to find the continuous and
            // discrete cluster.
            if (a instanceof SequenceActor) {
                if (predecessorList(a).isEmpty()) {
                    throw new NotSchedulableException(((Nameable)a).getName()
                            + " is a SequenceActor, which cannot be a"
                            + " source actor in the CT domain.");
                }
                Iterator ports = ((Entity)a).portList().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    _signalTypeMap.setType(port, DISCRETE);
                    if (port.isOutput()) {
                        _signalTypeMap.propagateType(port);
                    }
                }
            } else if ((a instanceof TypedCompositeActor) &&
                    !(a instanceof CTStepSizeControlActor)) {
                // Opaque composite actors that are not CTComposite actors
                // are treated as DISCRETE actors.
                Iterator ports = ((Entity)a).portList().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    _signalTypeMap.setType(port, DISCRETE);
                    if (port.isOutput()) {
                        _signalTypeMap.propagateType(port);
                    }
                }
            } else {
                // Other signal types are obtained from parameters on ports.
                Iterator ports = ((Entity)a).portList().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    Parameter signalType =
                        (Parameter)port.getAttribute("signalType");
                    if (signalType != null) {
                        String type = ((StringToken)signalType.getToken()).
                            stringValue();
                        type = type.trim().toUpperCase();
                        if (type.equals("CONTINUOUS")) {
                            _signalTypeMap.setType(port, CONTINUOUS);
                            if (port.isOutput()) {
                                _signalTypeMap.propagateType(port);
                            }
                        } else if (type.equals("DISCRETE")) {
                            _signalTypeMap.setType(port, DISCRETE);
                            if (port.isOutput()) {
                                _signalTypeMap.propagateType(port);
                            }
                        } else {
                            throw new InvalidStateException(port,
                                    " signalType not understandable.");
                        }
                    } else if (a instanceof CTCompositeActor) {
                        // Assume all it ports to be continuous unless
                        // otherwise specified.
                        _signalTypeMap.setType(port, CONTINUOUS);
                        if (port.isOutput()) {
                            _signalTypeMap.propagateType(port);
                        }
                    }
                }
                // If it is a domain polymorphic source, then treat
                // it outputs as CONTINUOUS, unless otherwise specified.
                if (predecessorList(a).isEmpty()) {
                    ports = ((Entity)a).portList().iterator();
                    while (ports.hasNext()) {
                        IOPort port = (IOPort)ports.next();
                        if (_signalTypeMap.getType(port) == UNKNOWN) {
                            _signalTypeMap.setType(port, CONTINUOUS);
                            if (port.isOutput()) {
                                _signalTypeMap.propagateType(port);
                            }
                        }
                    }
                }
            }
        }

        // Save the resolved signal types in each port.


        // Done with all actor classification and known port signal
        // type assignment.

        // Now we propagate the signal types by topological sort.
        // First make sure that there is no causality loop of arithmetic
        // actor. This makes other graph reachability algorithms terminate.
        DirectedAcyclicGraph arithmeticGraph = _toGraph(arithmeticActors);
        if (!arithmeticGraph.isAcyclic()) {
            throw new NotSchedulableException(
                    "Arithmetic loops are not allowed in the CT domain.");
        }
        DirectedAcyclicGraph dynamicGraph = _toGraph(dynamicActors);
        if (!dynamicGraph.isAcyclic()) {
            throw new NotSchedulableException(
                    "Loops of dynamic actors (e.g. integrators) " +
                    "are not allowed in the CT domain. You may insert a " +
                    "Scale actor with factor 1.");
        }

        // Find the continuous and discrete clusters by propagating signal
        // type constrains.
        // Notice that signal types of dynamic actors and source actors
        // and waveform generators has already propagated by one step.
        // So, we can start with the arithmetic actors only.

        Object[] sortedArithmeticActors = arithmeticGraph.topologicalSort();
        // Examine and propagate signal types.
        for (int i = 0; i < sortedArithmeticActors.length; i++ ) {
            Actor actor = (Actor)sortedArithmeticActors[i];
            // Note that the signal type of the input ports should be set
            // already. If all input ports are CONTINUOUS, and the
            // output ports are UNKNOWN, then all output ports should be
            // CONTINUOUS. If all input ports are DISCRETE, and the
            // output ports are UNKNOWN, then all output ports should be
            // DISCRETE. If some input ports are continuous and some
            // input ports are discrete, then the output port type must
            // be set manually, which mean they have been resolved by now.
            Iterator inputPorts = actor.inputPortList().iterator();
            CTReceiver.SignalType knownType = UNKNOWN;
            boolean needManuallySetType = true;
            while (inputPorts.hasNext()) {
                IOPort port = (IOPort)inputPorts.next();
                if (port.getWidth() != 0) {
                    CTReceiver.SignalType type = _signalTypeMap.getType(port);
                    if (type == UNKNOWN) {
                        throw new NotSchedulableException("Cannot resolve "
                                + "signal type for port "
                                + port.getFullName()
                                + ". If you are certain about the signal type"
                                + ", you can set them manually.\n"
                                + " To do this, you can add a parameter "
                                + "called \'signalType\' with value "
                                + "\'\"CONTINUOUS\"\' or \'\"DISCRETE\"\'"
                                + " to a port.");
                    } else if (knownType == UNKNOWN) {
                        knownType = type;
                        needManuallySetType = false;
                    } else if (knownType != type) {
                        needManuallySetType = true;
                        break;
                    }
                }
            }
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                CTReceiver.SignalType type = _signalTypeMap.getType(port);
                if (type == UNKNOWN) {
                    if (needManuallySetType) {
                        throw new NotSchedulableException("Cannot resolve "
                                + "signal type for port " + port.getFullName()
                                + ".\n To set the signal type manually, "
                                + "add a parameter with name \'signalType\' "
                                + "and a string value \'\"CONTINUOUS\"\' "
                                + "or \'\"DISCRETE\"\'.");
                    } else {
                        _signalTypeMap.setType(port, knownType);
                    }
                }
                // If there's any inconsistency in the signal type,
                // then this method will throw exception.
                _signalTypeMap.propagateType(port);
            }
        }

        // Set attributes in the model to display the signal types.
        _setPortSignalTypes(_signalTypeMap);

        // Output the signal type resolution result to the debugger.
        if (_debugging) _debug("Resolved signal types: {\n"
                + _signalTypeMap.toString() + "}");

        // Now all ports are in the SignalTypes table. We classify
        // continuous and discrete actors.
        // Note that an actor is continuous if it has continuous ports;
        // an actor is discrete if it has discrete ports. So under this
        // rule, continuous actor set and discrete actor set may overlap.
        discreteActors = _signalTypeMap.getDiscreteActors();
        continuousActors = _signalTypeMap.getContinuousActors();

        // Notice that by now, we have all the discrete actors, but
        // they are not  in the topological order.
        // Now create the discrete schedule.
        DirectedAcyclicGraph discreteGraph = _toGraph(discreteActors);
        Object[] discreteSorted = discreteGraph.topologicalSort();
        for (int i = 0; i < discreteSorted.length; i++) {
            Actor actor = (Actor) discreteSorted[i];
                
            discreteActorSchedule.add(new Firing(actor));

            // FIXME: For a CTCompositeActor, if it only has outputs
            // and the outputs are "DISCRETE", it is treated as a
            // discrete actor. Consequently, it is not included in 
            // the sinkActors, not in the outputSSCActors. 
            // The following code adds it into the continuousActor list.
            if ((actor instanceof CompositeActor) &&
                (actor instanceof CTStepSizeControlActor)) {
                    if (!continuousActors.contains(actor)) {
                        continuousActors.add(actor);
                    }
                }            
        }

        // Actors remain in the continuousActors list are real continuous
        // actor. The normal (CT) scheduling should only apply to them.

        // Now we check whether there are any sequence actors or discrete
        // composite actors in the continuous part of the system.
        // Add all continuous actors in the continuous actors schedule.
        Iterator continuousIterator = continuousActors.iterator();
        while (continuousIterator.hasNext()) {
            Actor actor = (Actor)continuousIterator.next();
            if ((actor instanceof SequenceActor) ||
                    ((actor instanceof CompositeActor) &&
                            (!(actor instanceof CTStepSizeControlActor)))) {
                throw new NotSchedulableException((Nameable)actor,
                        " is in the continuous cluster, but it is a "
                        + "sequence or discrete actor.");
            }
            continuousActorSchedule.add(new Firing(actor));
            // We test for sinks in the continuous cluster. These actors
            // are used to generate the output schedule. Event generators
            // in the continuous cluster are treated as sink actors. Note
            // dynamic actors themselves are not sink actors.
            if (actor instanceof CTEventGenerator) {
                sinkActors.add(actor);
            } else if (!(actor instanceof CTDynamicActor)) {
                List successorList = successorList(actor);
                if (successorList.isEmpty()) {
                    sinkActors.add(actor);
                } else {
                    Iterator successors = successorList.iterator();
                    boolean isSink = true;
                    while (successors.hasNext()) {
                        Actor successor = (Actor)successors.next();
                        if (continuousActors.contains(successor)) {
                            isSink = false;
                            break;
                        }
                    }
                    if (isSink) {
                        sinkActors.add(actor);
                    }
                }
            }

        }

        // Create waveformGeneratorSchedule.
        Iterator generators = waveformGenerators.iterator();
        while (generators.hasNext()) {
            Actor generator = (Actor)generators.next();
            waveformGeneratorSchedule.add(new Firing(generator));
        }

        // Schedule event generators so that they are executed topologically.
        // And treat them as sinks.
        if (!eventGenerators.isEmpty()) {
            DirectedAcyclicGraph eventGraph = _toGraph(eventGenerators);
            Object[] eventSorted = eventGraph.topologicalSort();
            for (int i = 0; i < eventSorted.length; i++) {
                eventGeneratorSchedule.add(new Firing((Actor)eventSorted[i]));
                //if (continuousActors.contains(eventSorted[i])) {
                // Also add them to the sink actor list in
                // topological order.
                //  sinkActors.addLast(eventSorted[i]);
                //}
            }
        }

        // Now schedule dynamic actors and state transition actors.
        // Manipulate on the arithmeticGraph and the dynamicGraph within
        // the continuous actors.

        arithmeticGraph = _toArithmeticGraph(continuousActors);
        if (!dynamicActors.isEmpty()) {
            Object[] dynamicArray = dynamicActors.toArray();
            // Dynamic actors are reverse ordered in the schedule.
            Object[] xSorted = dynamicGraph.topologicalSort(dynamicArray);
            for (int i = 0; i < xSorted.length; i++) {
                Actor a = (Actor)xSorted[i];
                // Looping on add(0, a) will reverse the order.
                dynamicActorSchedule.add(0, new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    // Note: they are not ordered, but addFirst() is
                    // considered more efficient.
                    stateSSCActors.add(new Firing(a));
                }
            }

            // State transition schedule
            Object[] fx = arithmeticGraph.backwardReachableNodes(dynamicArray);
            Object[] fxSorted = arithmeticGraph.topologicalSort(fx);
            for (int i = 0; i < fxSorted.length; i++) {
                Actor a = (Actor)fxSorted[i];
                stateTransitionSchedule.add(new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    // Note: they are not ordered, but we try to keep
                    // a topological order anyway.
                    stateSSCActors.add(new Firing(a));
                }
            }
        }

        // The assumption that the CTEventGenerators do not 
        // appear in an integration path and they
        // bridge the continous actors and discrete actors
        // only applies to the ATOMIC actors, like a 
        // LevelCrossingDetector.  
        // For a ModalModel or CT subSystem (CTCompositeActor),
        // which may generate discrete events and have to implement
        // the CTEventGenerator interface, the assumption 
        // is not true any more. 
        // There is a possibility that they are added into the 
        // outputSchedule multiple times. 
        
        // The situation happens when an actor in the sinkActors
        // list happens to be in the backward reachable nodes of 
        // another actor in the sinkActors list. (See comment below
        // for details.)

        if (!sinkActors.isEmpty()) {
            // Construct an array of sink actors.
            Object[] sinkArray = sinkActors.toArray();
            // Output map.
            Object[] gx = arithmeticGraph.backwardReachableNodes(sinkArray);
            Object[] gxSorted = arithmeticGraph.topologicalSort(gx);
            for (int i = 0; i < gxSorted.length; i++) {
                Actor a = (Actor)gxSorted[i];

                if (sinkActors.contains(a)) {
                    // If Actor a is already in the sinkActors list, 
                    // we add it to the outputSchedule here because
                    // we want to keep the ordering of the event 
                    // passing. 
                    // We remove the actor from the sinkActors list
                    // such that it is not added twice. 
                    sinkActors.remove(a);
                }

                outputSchedule.add(new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    outputSSCActors.add(new Firing(a));
                }
            }
            // Add sinks to the output schedule. Note the ordering among
            // sink actors since we allow chains of event generators.
            Iterator sinks = sinkActors.iterator();
            while (sinks.hasNext()) {
                Actor a = (Actor)sinks.next();
                outputSchedule.add(new Firing(a));
                if (a instanceof CTStepSizeControlActor) {
                    outputSSCActors.add(new Firing(a));
                }
            }

        }
        
        // Create the CTSchedule. Note it must be done in this order.
        ctSchedule.add(discreteActorSchedule);
        ctSchedule.add(continuousActorSchedule);
        ctSchedule.add(dynamicActorSchedule);
        ctSchedule.add(eventGeneratorSchedule);
        ctSchedule.add(outputSchedule);
        ctSchedule.add(outputSSCActors);
        ctSchedule.add(stateTransitionSchedule);
        ctSchedule.add(statefulActors);
        ctSchedule.add(stateSSCActors);
        ctSchedule.add(waveformGeneratorSchedule);

        setValid(true);
        return ctSchedule;
    }

    /** Set or create a not-visible, not-persistent parameter
     *  with the specified name in the specified container with
     *  the specified value.
     *  @param container The container for the parameter.
     *  @param name The name for the parameter.
     *  @param value The value for the parameter.
     *  @throws IllegalActionException If the parameter cannot
     *   contain the specified value.
     */
    private static void _setOrCreate(
            NamedObj container, String name, String value)
            throws IllegalActionException {
        Variable parameter = (Variable)container.getAttribute(name);
        if (parameter == null) {
            // Parameter does not exist, so create it.
            try {
                parameter = new Variable(container, name);
                parameter.setVisibility(Settable.NOT_EDITABLE);
                parameter.setPersistent(false);
            } catch (KernelException ex) {
                // Should not occur.
                throw new InternalErrorException(ex.toString());
            }
        }
        parameter.setToken(new StringToken(value));
    }

    /** Create and set a parameter in each port according
     *  to the resolved. continuous/discrete nature of the port.
     *  @param typeMap A map from ports to
     */
    private void _setPortSignalTypes(final SignalTypeMap typeMap) {
        Director director = (Director) getContainer();
        final CompositeActor container =
            (CompositeActor)director.getContainer();
            
        ChangeRequest request = new ChangeRequest(this, "Record signal types") {
            protected void _execute() throws KernelException {
                Iterator entities = container.deepEntityList().iterator();
                while (entities.hasNext()) {
                    Entity entity = (Entity)entities.next();
                    for (Iterator ports = entity.portList().iterator();
                         ports.hasNext();) {
                        IOPort port = (IOPort)ports.next();
                        String typeString =
                                typeMap.getType(port).toString();
                        _setOrCreate(port, "resolvedSignalType", typeString);
                    }
                }
            }
        };
        // Indicate that the change is non-persistent, so that
        // the UI doesn't prompt to save.
        request.setPersistent(false);
        container.requestChange(request);
    }

    /** Convert the given list of actors to a directed acyclic graph.
     *  CTDynamicActors are treated as sinks to break closed loops.
     *  Each actor in the argument is a node in the graph,
     *  each link between a pair of actors, except the output links
     *  from dynamic actors, is a edge between the
     *  corresponding nodes.
     *  The existence of the director and containers is not checked
     *  in this method, so the caller should check.
     *  @param list The list of actors to be scheduled.
     *  @return A graph representation of the actors.
     */
    private DirectedAcyclicGraph _toArithmeticGraph(List list) {
        DirectedAcyclicGraph graph = new DirectedAcyclicGraph();
        // Create the nodes.
        Iterator actors = list.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            graph.addNodeWeight(actor);
        }

        // Create the edges.
        actors = list.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            // CTCompositeActor is basically everything,
            // it may be an event generator, or a state transition
            // actor.
            if ((actor instanceof CTCompositeActor) || 
                (!(actor instanceof CTDynamicActor) &&
                    !(actor instanceof CTEventGenerator))
                     ) {
                // Find the successors of the actor
                Iterator successors = successorList(actor).iterator();
                while (successors.hasNext()) {
                    Actor successor = (Actor)successors.next();
                    if (list.contains(successor)) {
                        graph.addEdge(actor, successor);
                    }
                }
            }
        }
        return graph;
    }

    /** Convert the given actors to a directed acyclic graph.
     *  CTDynamicActors are NOT treated as sinks. This method
     *  is used to construct the dynamic actor schedule.
     *  Each actor in the argument is a node in the graph,
     *  and each link between a pair of actors is a edge between the
     *  corresponding nodes.
     *  @param list The list of actors to be converted to a graph.
     *  @return A graph representation of the actors.
     */
    private DirectedAcyclicGraph _toGraph(List list) {

        DirectedAcyclicGraph g = new DirectedAcyclicGraph();
        // Create the nodes.
        Iterator actors = list.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor)actors.next();
            g.addNodeWeight(a);
        }
        // Create the edges.
        actors = list.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor) actors.next();
            // Find the successors of a
            Iterator successors = successorList(a).iterator();
            while (successors.hasNext()) {
                Actor s = (Actor) successors.next();
                if (list.contains(s)) {
                    g.addEdge(a, s);
                }
            }
        }
        return g;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The static name of the scheduler.
    private static final String _STATIC_NAME = "CTScheduler";

    // The signal types.
    private SignalTypeMap _signalTypeMap;


    // Static Enumerations of signal types.



    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // Inner class for signal type table. This wraps a HashMap, but
    // the put() method will check for conflicts. That is, if there
    // exist a map port --> CONTINUOUS, but another map port --> DISCRETE
    // is trying to be inserted, then a NotSchedulableException will be
    // thrown.

    private class SignalTypeMap {

        public SignalTypeMap() {
            _map = new HashMap();
            _continuousActors = new LinkedList();
            _discreteActors = new LinkedList();
        }

        /////////////////////////////////////////////////////////////////
        ////                     public methods                      ////

        // Return the list of actors with continuos ports.
        public LinkedList getContinuousActors() {
            return _continuousActors;
        }

        // Return the list of actors with discrete ports.
        public LinkedList getDiscreteActors() {
            return _discreteActors;
        }


        // Return the signal type of the specified port.
        // @return CONTINUOUS, DISCRETE, or UNKNOWN

        public CTReceiver.SignalType getType(IOPort port)
                throws NotSchedulableException {
            if (!_map.containsKey(port)) {
                return UNKNOWN;
            } else {
                return (CTReceiver.SignalType)_map.get(port);
            }
        }

        // Check for consistency and set a port to the specific type.
        // the map.
        public void setType(IOPort port, CTReceiver.SignalType type)
                throws NotSchedulableException {
            //System.out.println("set type: " + port.getFullName() + " " +
            //   signalTypeToString(type));
            if (!_map.containsKey(port)) {
                _map.put(port, type);
                // If it is an input port,
                // set the signal type to all the receivers in the port.
                if (((port.getContainer() != CTScheduler.this.getContainer()
                        .getContainer()) && port.isInput())) {
                    Receiver[][] receivers = port.getReceivers();
                    for (int i = 0; i < receivers.length; i++) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            ((CTReceiver)receivers[i][j])
                                .setSignalType(type);
                        }
                    }
                }
                if ((port.getContainer() == CTScheduler.this.getContainer()
                        .getContainer()) &&  port.isOutput()) {
                    Receiver[][] receivers = port.getInsideReceivers();
                    for (int i = 0; i < receivers.length; i++) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            ((CTReceiver)receivers[i][j])
                                .setSignalType(type);
                        }
                    }
                }
                Entity actor = (Entity)port.getContainer();
                if ((type == CONTINUOUS) &&
                        actor != CTScheduler.this.getContainer().getContainer()
                        && !_continuousActors.contains(actor)) {
                    //System.out.println(actor.getName() + " is CONTINUOUS.");
                    _continuousActors.add(actor);
                }
                if ((type == DISCRETE) &&
                        actor != CTScheduler.this.getContainer().getContainer()
                        && !_discreteActors.contains(actor)) {
                    //System.out.println(actor.getName() + " is DISCRETE.");
                    _discreteActors.add(actor);
                }
            } else {
                CTReceiver.SignalType previousType =
                    (CTReceiver.SignalType) _map.get(port);
                if (previousType != type) {
                    throw new NotSchedulableException(port.getFullName()
                            + " has a signal type conflict: \n"
                            + "Its signal type was set/resolved to "
                            + signalTypeToString(previousType)
                            + ", but is going to be set to "
                            + signalTypeToString(type) + " now.");
                }
            }
        }

        // Set the type of all the connected input ports equal to the
        // type of the specified port. The caller must make sure that
        // the type of argument has already been set. Otherwise an
        // InternalErrorException will be thrown.
        // If any connected port already has a type and
        // it is not the same as the type to be set, then throw
        // a NonSchedulableException.
        public void propagateType(IOPort port)
                throws NotSchedulableException {

            if (!_map.containsKey(port)) {
                throw new InternalErrorException(port.getFullName()
                        + " type unknown.");
            }
            // Iterate over all ports that can receive data from this one.
            // This includes input ports lower in the hierarchy or output
            // ports higher in the hierarchy.
            Iterator connectedPorts = port.sinkPortList().iterator();
            while (connectedPorts.hasNext()) {
                IOPort nextPort = (IOPort)connectedPorts.next();

                //System.out.println("Propagate type from port "
                //    + port.getFullName() + " to port "
                //    + nextPort.getFullName());
                if (!_map.containsKey(nextPort)) {
                    setType(nextPort, getType(port));
                } else if (getType(port) != getType(nextPort)) {
                    throw new NotSchedulableException(
                            "Signal type conflict: "
                            + port.getFullName() + " (of type "
                            + signalTypeToString(getType(port))
                            + ") and "
                            + nextPort.getFullName() + " (of type "
                            + signalTypeToString(getType(nextPort)) + ")"
                            + "). Perhaps the connection has "
                            + "sequence semantics?");
                }
            }
        }

        // Set the type of all the connected ports on the inside equal to the
        // type of the specified port. The caller must make sure that
        // the type of argument has already been set. Otherwise an
        // InternalErrorException will be thrown.
        // If any connected port already has a type and
        // it is not the same as the type to be set, then throw
        // a NonSchedulableException.
        public void propagateTypeInside(IOPort port)
                throws NotSchedulableException {

            if (!_map.containsKey(port)) {
                throw new InternalErrorException(port.getFullName()
                        + " type unknown.");
            }
            // Iterate over all ports that can receive data from this one.
            // This includes input ports lower in the hierarchy or output
            // ports higher in the hierarchy.
            Iterator connectedPorts = port.insideSinkPortList().iterator();
            while (connectedPorts.hasNext()) {
                IOPort nextPort = (IOPort)connectedPorts.next();

                //System.out.println("Propagate type from port "
                //    + port.getFullName() + " to port "
                //    + nextPort.getFullName());
                if (!_map.containsKey(nextPort)) {
                    setType(nextPort, getType(port));
                } else if (getType(port) != getType(nextPort)) {
                    throw new NotSchedulableException(
                            "Signal type conflict: "
                            + port.getFullName() + " (of type "
                            + signalTypeToString(getType(port))
                            + ") and "
                            + nextPort.getFullName() + " (of type "
                            + signalTypeToString(getType(nextPort)) + ")"
                            + "). Perhaps the connections has "
                            + "sequence semantics instead of the continuous "
                            + "signal semantics that CT requires?  This "
                            + "would happen if one of the actors was an "
                            + "SDF actor.");
                }
            }
        }

        /** Return a string representation for the signal types of all ports.
         *  It is in the format like:
         *  portFullName::signalType.
         *  @return The string representation of the signal types.
         *   If the map of the signal types is empty, then return an empty
         *   string.
         */
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            if (_map != null) {
                Iterator ports = _map.keySet().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort)ports.next();
                    String type = signalTypeToString(getType(port));
                    buffer.append("  "
                            + port.getFullName()
                            + " :: "
                            + type
                            + "\n");
                }
            }
            return buffer.toString();
        }

        /////////////////////////////////////////////////////////////////
        ////                     private variables                   ////
        // The HashMap.
        private HashMap _map;

        private LinkedList _continuousActors;

        private LinkedList _discreteActors;
    }
}
