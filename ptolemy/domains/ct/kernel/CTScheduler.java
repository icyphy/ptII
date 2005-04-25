/* The static scheduler for the continuous time domain.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.StateReceiver;
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
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// CTScheduler

/**
   The Static scheduler for the CT domain.
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
   discrete input and continuous output is called a <I>waveform generator</I>.
   <P>
   The interaction with some discrete domains requires that the
   CT simulation be able to remember its state and roll-back
   to the remembered state when needed. This in turn requires
   that all actors that have internal states to be able
   to remember and restore their states. These actors are called
   <I>stateful actors</I>.
   <P>
   In continuous-time simulation, time progresses in a discrete way.
   The distance between two consecutive time points is called the
   <I>integration step size</I> or step size, for short. Some actors
   may put constraints on the choice of the step size.
   These actors are called <I>step size control actors</I>. Examples of step
   size control actors include integrators, which control the
   accuracy and speed of numerical ODE solutions, and event generators,
   which produce discrete events.
   <P>
   To help with scheduling, the actors are partitioned into several clusters,
   including <I>continuous actors</I>, <I>discrete actors</I>,
   <I>arithmetic actors</I>, <I>stateTransition actors</I>,
   <I>dynamic actors</I>, <I>sink actors</I>,<I> event generators</I>,
   <I> waveform generators</I>, <I>CT subsystems </I>, and <I>non-CT
   subsystems</I>.
   This scheduler uses the cluster information and the system topology
   to provide the firing sequences for evaluating f() and g().
   It also provides a firing order for all the dynamic actors.
   The firing sequence for evaluating f() is
   called the <I>state transition schedule</I>; the firing
   sequence for evaluating g() is called the <I>output schedule</I>;
   and the firing sequence for dynamic actors is called the
   <I>dynamic actor schedule</I>.
   <P>
   The state transition schedule is the actors in the f() function sorted
   in topological order, such that, after the integrators emit their
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
   <p>
   This CTScheduler does not support mutation.

   @author Jie Liu, Haiyang Zheng, Ye Zhou
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
   @see ptolemy.actor.sched.Scheduler
*/
public class CTScheduler extends Scheduler {
    /** Construct a CT scheduler in the default workspace
     *  with an empty string as the name. There is no director
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

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** These are syntactic sugar for signal types, which are defined in
     *  CTReceiver.
     *  Signal type: CONTINUOUS.
     */
    public static final CTReceiver.SignalType CONTINUOUS = CTReceiver.CONTINUOUS;

    /**  Signal type: DISCRETE.
     */
    public static final CTReceiver.SignalType DISCRETE = CTReceiver.DISCRETE;

    /**  Signal type: UNKNOWN.
     */
    public static final CTReceiver.SignalType UNKNOWN = CTReceiver.UNKNOWN;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the given actor is in the continuous cluster of
     *  the model.
     *  @param actor The given actor.
     *  @return True if the actor is a continuous actor.
     *  @exception IllegalActionException If this method is called before
     *  initialization, where the schedule is computed.
     */
    public boolean isContinuous(Actor actor) throws IllegalActionException {
        if (_signalTypeMap == null) {
            throw new IllegalActionException(this,
                    " isContinuous() can only " + "be called after initialization.");
        }

        List continuousActors = _signalTypeMap.getContinuousActors();
        return continuousActors.contains(actor);
    }

    /** Return true if the given actor is in the discrete cluster of
     *  the model.
     *  @param actor The given actor.
     *  @return True if the actor is a discrete actor.
     *  @exception IllegalActionException If this method is called before
     *  initialization, where the schedule is computed.
     */
    public boolean isDiscrete(Actor actor) throws IllegalActionException {
        if (_signalTypeMap == null) {
            throw new IllegalActionException(this,
                    " isDiscrete() can only " + "be called after initialization.");
        }

        List discreteActors = _signalTypeMap.getDiscreteActors();
        return discreteActors.contains(actor);
    }

    /** Return the predecessors of the given actor in the same level of
     *  hierarchy. If the argument is null, return null. If the actor is
     *  a source, return an empty list.
     *  @param actor The given actor.
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
                IOPort outPort = (IOPort) outPorts.next();
                Actor pre = (Actor) outPort.getContainer();

                // NOTE: This could be done by using
                // NamedObj.depthInHierarchy() instead of comparing the
                // executive directors, but its tested this way, so we
                // leave it alone.
                if ((actor.getExecutiveDirector() == pre.getExecutiveDirector())
                        && !predecessors.contains(pre)) {
                    predecessors.addLast(pre);
                }
            }
        }

        return predecessors;
    }

    /** Return the SignalType as a String.
     *  @param signalType The type of signal.
     *  @return A string of the signal type.
     */
    public String signalTypeToString(CTReceiver.SignalType signalType) {
        if (signalType == CONTINUOUS) {
            return "CONTINUOUS";
        } else if (signalType == DISCRETE) {
            return "DISCRETE";
        } else if (signalType == UNKNOWN) {
            return "UNKNOWN";
        }

        return "An INVALID signaltype: " + signalType + ".";
    }

    /** Return the successive actors of the given actor in the same level of
     *  hierarchy. If the argument is null, return null. If the actor is a
     *  sink, return an empty list.
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
                IOPort inPort = (IOPort) inPorts.next();
                Actor post = (Actor) inPort.getContainer();

                // NOTE: This could be done by using
                // NamedObj.depthInHierarchy() instead of comparing the
                // executive directors, but its tested this way, so we
                // leave it alone.
                if ((actor.getExecutiveDirector() == post.getExecutiveDirector())
                        && !successors.contains(post)) {
                    successors.addLast(post);
                }
            }
        }

        return successors;
    }

    /** Return the full name of this scheduler.
     *  @return The full name of this scheduler.
     */
    public String toString() {
        return getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the CTSchedule. Caching of the schedule is done
     *  in the super class, so this method does not test
     *  for the validation of the schedule.
     */
    protected Schedule _getSchedule()
            throws NotSchedulableException, IllegalActionException {
        // NOTE: This implementation creates new Lists every time,
        // If this hurts performance a lot, consider reusing old lists.
        // This requires the Schedule class to implement clear().
        // NOTE: The current implementation focuses on the continuous
        // phase of execution, and only pays a little attantion to the
        // discrete phase of execution. In particular, the discrete phase
        // of execution is not as complicated as the DE domain by a SR fashion.
        CTSchedule ctSchedule = new CTSchedule();

        // Construct an empty map of the signal types of
        // all the ports of the container and the contained actors.
        _signalTypeMap = new SignalTypeMap();

        // Construct a list of lists to store actors that share the same
        // property. These lists are not mutually exclusive. For example, an
        // actor in the ctSubsystems list may also be contained in the list
        // of dynamic actors.
        LinkedList continuousActors = new LinkedList();
        LinkedList discreteActors = new LinkedList();
        LinkedList dynamicActors = new LinkedList();
        LinkedList stateTransitionActors = new LinkedList();
        LinkedList arithmeticActors = new LinkedList();
        LinkedList sinkActors = new LinkedList();
        LinkedList eventGenerators = new LinkedList();
        LinkedList waveformGenerators = new LinkedList();

        // all the opaque composite actors that have a CTEmbeddedDirector.
        LinkedList ctSubsystems = new LinkedList();

        // all the opaque composite actors that have a director other than
        // the CTEmbeddedDirector.
        LinkedList nonCTSubsystems = new LinkedList();

        // Construct a list of schedules, which will be accessible to
        // the CT directors.
        Schedule continuousActorSchedule = new Schedule();
        Schedule discreteActorSchedule = new Schedule();
        Schedule dynamicActorSchedule = new Schedule();
        Schedule eventGeneratorSchedule = new Schedule();
        Schedule outputSchedule = new Schedule();
        Schedule outputSSCActorSchedule = new Schedule();
        Schedule statefulActorSchedule = new Schedule();
        Schedule stateSSCActorSchedule = new Schedule();
        Schedule stateTransitionSchedule = new Schedule();
        Schedule waveformGeneratorSchedule = new Schedule();

        // Get the composite actor that contains the CT director,
        // which contains this scheduler.
        CompositeActor container = (CompositeActor) getContainer().getContainer();

        boolean isCTCompositeActor = container instanceof CTCompositeActor;

        // Examine and propagate the signal types of the container ports.
        // FIXME: the following implementation is not a good solution.
        // Signal types can also be derived in a similar way as the
        // function dependencies analysis.
        Iterator containerInPorts = container.inputPortList().iterator();

        while (containerInPorts.hasNext()) {
            IOPort inPort = (IOPort) containerInPorts.next();

            if (!isCTCompositeActor) {
                // If the container is not a CT composite actor,
                // this actor is not embedded inside a CT or HS
                // model. In most cases, the outside model is a discrete model.
                // For example, a DE model. However, there is a possibility that
                // the outside model is a Giotto or other domains that have
                // their receivers with a "state" semantics.
                // Solution: Set signal types according the receiver types.
                // If the receiver is a state receiver, the signal type is
                // set to "CONTINUOUS", otherwise, "DISCRETE".
                // NOTE: The implicit assumption is that all the receivers
                // belonging to the same IO port have the same signal type.
                Receiver[][] localReceivers = inPort.getReceivers();
                Receiver localReceiver = localReceivers[0][0];

                if (localReceiver instanceof StateReceiver) {
                    _signalTypeMap.setType(inPort, CONTINUOUS);
                } else {
                    _signalTypeMap.setType(inPort, DISCRETE);
                }
            } else {
                // If the container is a CT composite actor,
                // examine the "signalType" parameter specified manually
                // by model designers, or by the upper level in
                // hierarchy during its schedule construction.
                // If no such parameter exists, defalut the signal type of
                // the input ports to be "CONTINUOUS".
                Parameter signalType = (Parameter) inPort.getAttribute(
                        "signalType");

                if (signalType != null) {
                    String type = ((StringToken) signalType.getToken())
                        .stringValue();
                    type = type.trim().toUpperCase();

                    if (type.equals("CONTINUOUS")) {
                        _signalTypeMap.setType(inPort, CONTINUOUS);
                    } else if (type.equals("DISCRETE")) {
                        _signalTypeMap.setType(inPort, DISCRETE);
                    } else {
                        throw new IllegalActionException(inPort,
                                "Unrecognized signal type. "
                                + "It should be a string of "
                                + "either \"CONTINUOUS\" or \"DISCRETE\".");
                    }
                } else {
                    // The default signal type of the input ports of a
                    // CTCompositeActor is continuous.
                    // FIXME: This is not accurate. There is a possibility
                    // that one of the input ports receives a discrete input
                    // while another input port receives a continuous input.
                    // FIXME: We may need iterations to solve this problem.
                    // Because there are only three types: CONTINUOUS, DISCRETE,
                    // and UNKNONW, where the UNKNOWN is the bottom. Two
                    // iterations are sufficient to solve the signal type of
                    // all ports.
                    _signalTypeMap.setType(inPort, CONTINUOUS);
                }
            }

            // Propagate the signal types of the input ports of the contained
            // actors.
            _signalTypeMap.propagateTypeInside(inPort);
        }

        // Iterate all contained actors to classify each actor and
        // resolve the signal types of its ports.
        Iterator allActors = container.deepEntityList().iterator();

        while (allActors.hasNext()) {
            Actor a = (Actor) allActors.next();

            if (_debugging & _verbose) {
                _debug("Examine " + ((Nameable) a).getFullName()
                        + " for signal types.");
            }

            // Now classify actors by their implemented interfaces.
            if (a instanceof CompositeActor) {
                if (a instanceof CTCompositeActor) {
                    // the actor is a CT subsystem
                    dynamicActors.add(a);
                    arithmeticActors.add(a);
                    ctSubsystems.add(a);
                    statefulActorSchedule.add(new Firing(a)); //????
                    waveformGenerators.add(a);
                    eventGenerators.add(a);
                } else {
                    // The actor is a subsystem but not a CT one,
                    // it can only be an arithmetic actor or a sink actor.
                    // We will clarify this actor based on the completely
                    // resolved signal type of its input and output ports later.
                    // Right now, we simply save it for future processing.
                    nonCTSubsystems.add(a);
                }
            } else {
                // the actor is an atomic actor
                if (a instanceof CTStatefulActor) {
                    statefulActorSchedule.add(new Firing(a));
                }

                if (a instanceof CTWaveformGenerator) {
                    waveformGenerators.add(a);
                } else if (a instanceof CTEventGenerator) {
                    eventGenerators.add(a);
                } else if (a instanceof CTDynamicActor) {
                    dynamicActors.add(a);
                } else {
                    arithmeticActors.add(a);
                }
            }

            // Now resolve signal types to find the continuous and
            // discrete clusters of actors.
            if (a instanceof SequenceActor) {
                // Set all ports of a sequence actor with signal type "DISCRETE"
                if (predecessorList(a).isEmpty()) {
                    throw new NotSchedulableException(((Nameable) a).getName()
                            + " is a SequenceActor, which cannot be a"
                            + " source actor in the CT domain.");
                }

                Iterator ports = ((Entity) a).portList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    _signalTypeMap.setType(port, DISCRETE);

                    if (port.isOutput()) {
                        _signalTypeMap.propagateType(port);
                    }
                }
            } else if ((a instanceof CompositeActor)
                    && !(a instanceof CTCompositeActor)) {
                // This actor is an opaque composite actor but not a
                // CT Composite actor.
                // NOTE: For its output ports, we can tell the signal type
                // from their receiver types. However, the signal types of
                // its input ports have to be derived from the output ports
                // of some other actors that reside at the same hierarchical
                // level. So we only handle output ports here.
                Iterator ports = ((Entity) a).portList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();

                    if (port.isOutput()) {
                        Receiver[][] insideReceivers = port.getInsideReceivers();

                        // NOTE: The assumption is that all the receivers
                        // belonging to the same IO port have the same
                        // signal type.
                        Receiver insideReceiver = insideReceivers[0][0];

                        if (insideReceiver instanceof StateReceiver) {
                            _signalTypeMap.setType(port, CONTINUOUS);
                        } else {
                            _signalTypeMap.setType(port, DISCRETE);
                        }

                        _signalTypeMap.propagateType(port);
                    }
                }
            } else {
                // The signal types of the rest ports are obtained
                // from the "signalType" parameter.
                Iterator ports = ((Entity) a).portList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    Parameter signalType = (Parameter) port.getAttribute(
                            "signalType");

                    if (signalType != null) {
                        String type = ((StringToken) signalType.getToken())
                            .stringValue();
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
                        // Assume all the ports of a CTCompositeActor
                        // to be continuous unless otherwise specified.
                        // NOTE: this is a conservative approximation.
                        if (_signalTypeMap.getType(port) == UNKNOWN) {
                            _signalTypeMap.setType(port, CONTINUOUS);
                        }

                        if (port.isOutput()) {
                            _signalTypeMap.propagateType(port);
                        }
                    }
                }

                // NOTE: If it is a domain polymorphic source, unless its
                // outputs are declared as DISCRETE, assume their signal
                // types to CONTINUOUS. For example, the Const actor.
                // FIXME: Should the Sequence actor implement the SequenceActor
                // interface?
                if (predecessorList(a).isEmpty()) {
                    ports = ((Entity) a).portList().iterator();

                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();

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

        // Done with classification and port signal type assignment
        // of the sources, waveform generators, event generators,
        // sequence actors, and dynamic actors.
        // In the following, we first try to resolve the signal types of
        // the ports of the rest actors including nonCTSubsystems
        // by propagating known signal types.
        // First make sure that there is no causality loop of arithmetic
        // actors. This makes the graph reachability algorithms terminate.
        DirectedAcyclicGraph arithmeticGraph = _toGraph(arithmeticActors);

        if (!arithmeticGraph.isAcyclic()) {
            throw new NotSchedulableException(
                    "Arithmetic loops are not allowed in the CT domain.");
        }

        // We do not allow loops of dynamic actors, either.
        DirectedAcyclicGraph dynamicGraph = _toGraph(dynamicActors);

        // FIXME: Why is this disallowed? If we change this, change the class
        // comment (at the end) also.
        if (!dynamicGraph.isAcyclic()) {
            throw new NotSchedulableException(
                    "Loops of dynamic actors (e.g. integrators) "
                    + "are not allowed in the CT domain. You may insert a "
                    + "Scale actor with factor 1.");
        }

        // Now we propagate the signal types by topological sort.
        // Notice that signal types of the output ports of the dynamic actors,
        // source actors, waveform generators, event generators, and sequence
        // actors have already been propagated by one step.
        // So, we start with arithmetic actors.
        Object[] sortedArithmeticActors = arithmeticGraph.topologicalSort();

        for (int i = 0; i < sortedArithmeticActors.length; i++) {
            Actor actor = (Actor) sortedArithmeticActors[i];

            // Note that the signal type of the input ports should be set
            // already. If all input ports are CONTINUOUS, and the
            // output ports are UNKNOWN, then all output ports should be
            // CONTINUOUS. If all input ports are DISCRETE, and the
            // output ports are UNKNOWN, then all output ports should be
            // DISCRETE. If some input ports are continuous and some
            // input ports are discrete, then the output port type must
            // be set manually, which means they can not been resolved.
            Iterator inputPorts = actor.inputPortList().iterator();
            CTReceiver.SignalType knownInputType = UNKNOWN;
            boolean needManuallySetType = true;

            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();

                if (inputPort.getWidth() != 0) {
                    CTReceiver.SignalType inputType = _signalTypeMap.getType(inputPort);

                    if (inputType == UNKNOWN) {
                        throw new NotSchedulableException("Cannot resolve "
                                + "signal type for port " + inputPort.getFullName()
                                + ". If you are certain about the signal type"
                                + ", you can set them manually.\n"
                                + " To do this, you can add a parameter "
                                + "called \'signalType\' with value "
                                + "\'\"CONTINUOUS\"\' or \'\"DISCRETE\"\'"
                                + " to a port.");
                    } else if (knownInputType == UNKNOWN) {
                        knownInputType = inputType;
                        needManuallySetType = false;
                    } else if (knownInputType != inputType) {
                        needManuallySetType = true;
                        break;
                    }
                }
            }

            Iterator outputPorts = actor.outputPortList().iterator();

            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();

                if (outputPort.getWidth() != 0) {
                    CTReceiver.SignalType outputType = _signalTypeMap.getType(outputPort);

                    if (outputType == UNKNOWN) {
                        if (needManuallySetType) {
                            throw new NotSchedulableException("Cannot resolve "
                                    + "signal type for port "
                                    + outputPort.getFullName()
                                    + ".\n To set the signal type manually, "
                                    + "add a parameter with name \'signalType\'"
                                    + " and a string value \'\"CONTINUOUS\"\' "
                                    + "or \'\"DISCRETE\"\'.");
                        } else {
                            _signalTypeMap.setType(outputPort, knownInputType);
                        }
                    }

                    _signalTypeMap.propagateType(outputPort);
                }
            }
        }

        // Set the "signalType" parameters in the model
        // to display the signal types.
        _setPortSignalTypes(_signalTypeMap);

        // Output the signal type resolution result to the debugger.
        if (_debugging) {
            _debug("Resolved signal types: {\n" + _signalTypeMap.toString()
                    + "}");
        }

        // Now the signal types of all ports are resolved and stored in the
        // SignalTypes table. We classify continuous and discrete actors.
        // NOTE: An actor is continuous if it has continuous ports;
        // an actor is discrete if it has discrete ports. Under this
        // rule, the set of continuous actors and discrete actors may have
        // a non-empty intersection set.
        discreteActors = _signalTypeMap.getDiscreteActors();
        continuousActors = _signalTypeMap.getContinuousActors();

        // NOTE: There is a situation that the signal types of all the input
        // and output ports of a CT composite actor are derived as "DISCRETE".
        // In such case, we still need to include this actor into the set of
        // continuous actors, because there may be some continuous actors
        // hidden inside that CT composite actor.
        // To avoid introducing duplication:
        continuousActors.removeAll(ctSubsystems);
        continuousActors.addAll(ctSubsystems);

        // NOTE: There is a situation that the signal types of all the input
        // and output ports of a CT composite actor are derived as "CONTINUOUS".
        // In such case, we still need to include this actor into the set of
        // discrete actors, because there may be some discrete actors
        // hidden inside that CT composite actor.
        // To avoid introducing duplication:
        discreteActors.removeAll(ctSubsystems);
        discreteActors.addAll(ctSubsystems);

        // FIXME: the following statement does not make sense.
        // At this point, since the ports of all actors have their signal types
        // resolved, a nonCTSubsystem will be clarified based on
        // the signal types of its input and output ports.
        Iterator subsystems = nonCTSubsystems.iterator();

        while (subsystems.hasNext()) {
            CompositeActor subsystem = (CompositeActor) subsystems.next();

            if (discreteActors.contains(subsystem)
                    && continuousActors.contains(subsystem)) {
                // NOTE:
                // For a non-CT composite actor, it can not be a
                // waveform generator or an event generator.
                // Because the transformation of different type of signals
                // can only be made in CT models.
                waveformGenerators.add(subsystem);

                // remove the current subsystem from both the continuous
                // and discrete actor clusters.
                discreteActors.remove(subsystem);
                continuousActors.remove(subsystem);
            }
        }

        // Now, all actors are classified.
        // Notice that by now, we have all the discrete actors, but
        // they are not in the topological order. Sort them and
        // create the discrete schedule.
        DirectedAcyclicGraph discreteGraph = _toGraph(discreteActors);
        Object[] discreteSorted = discreteGraph.topologicalSort();

        for (int i = 0; i < discreteSorted.length; i++) {
            Actor actor = (Actor) discreteSorted[i];

            // We want to distinguish the waveform and event generators,
            // which have at least one input or output declared as CONTINUOUS
            // signal type, from purely discrete (continuous) actors, whose
            // ports are all DISCRETE (CONTINUOUS).
            if (continuousActors.contains(actor)) {
                if (actor instanceof CTCompositeActor) {
                    // We add CT composite actors into the list of discrete
                    // actors because a CTComposite actor can be anything.
                    discreteActorSchedule.add(new Firing(actor));
                } else {
                    // the following code removes event generators
                    // and waveform generators from the list of purely
                    // continuous actors.
                    // NOTE: we only remove actors that are declared to be
                    // event generators or waveform generators. Some actors,
                    // such as TriggeredContinuousClock actor, are still treated
                    // as purely continuous actors even though they have
                    // discrete inputs, because they produce continuous signals.
                    // TESTIT: Clock3, Clock5, and Clock6 in ct/lib/test/auto.
                    if ((actor instanceof CTEventGenerator)
                            || (actor instanceof CTWaveformGenerator)) {
                        continuousActors.remove(actor);
                    }
                }

                continue;
            }

            // We add purely discrete actors (discrete -> discrete) into list.
            discreteActorSchedule.add(new Firing(actor));
        }

        // Create the schedule for waveform generators.
        Iterator generators = waveformGenerators.iterator();

        while (generators.hasNext()) {
            Actor generator = (Actor) generators.next();
            waveformGeneratorSchedule.add(new Firing(generator));
        }

        // Schedule event generators so that they are executed topologically.
        // Treat them as sink actors from the point of view of a
        // continuous phase of execution.
        if (!eventGenerators.isEmpty()) {
            DirectedAcyclicGraph eventGraph = _toGraph(eventGenerators);
            Object[] eventSorted = eventGraph.topologicalSort();

            for (int i = 0; i < eventSorted.length; i++) {
                Actor actor = (Actor) eventSorted[i];

                // If this actor is both an event generator and
                // a CT step size control actor, we need to create two
                // firing objects for this actor for each schedule.
                eventGeneratorSchedule.add(new Firing(actor));

                if (actor instanceof CTStepSizeControlActor) {
                    // If this event generator is a step size control actor.
                    outputSSCActorSchedule.add(new Firing(actor));
                }
            }
        }

        // Actors remain in the continuousActors list are purely continuous
        // actor. The normal CT scheduling algorithm applies to them.
        // Add all continuous actors in the continuous actors schedule.
        Iterator continuousIterator = continuousActors.iterator();

        while (continuousIterator.hasNext()) {
            Actor actor = (Actor) continuousIterator.next();

            // only purely continuous actors (continuous -> continuous) and
            // CT composite actors are added into the continuousActorSchedule
            continuousActorSchedule.add(new Firing(actor));
        }

        // Continuous actors can be clarified into three kinds:
        // dynamic actors, state transition actors, and output actors
        // (a.k.a sink actors).
        // We first schedule dynamic actors and state transition actors.
        // Create a list of state related actors to record dynamic actors and
        // state transition actors. These actors are distinguished from output
        // actors.
        LinkedList stateRelatedActors = new LinkedList();

        // Manipulate on the arithmeticGraph and the dynamicGraph within
        // the continuous actors.
        arithmeticGraph = _toArithmeticGraph(continuousActors);

        if (!dynamicActors.isEmpty()) {
            Object[] dynamicArray = dynamicActors.toArray();

            // Dynamic actors are reverse ordered in the schedule.
            Object[] xSorted = dynamicGraph.topologicalSort(dynamicArray);

            for (int i = 0; i < xSorted.length; i++) {
                Actor dynamicActor = (Actor) xSorted[i];

                // Looping on add(0, a) will reverse the order.
                dynamicActorSchedule.add(0, new Firing(dynamicActor));
                stateRelatedActors.add(dynamicActor);

                if (dynamicActor instanceof CTStepSizeControlActor) {
                    stateSSCActorSchedule.add(new Firing(dynamicActor));
                }

                // find state transition actors
                Object[] fx;
                fx = arithmeticGraph.backwardReachableNodes(dynamicActor);

                Object[] fxSorted = arithmeticGraph.topologicalSort(fx);

                for (int fxi = 0; fxi < fxSorted.length; fxi++) {
                    Actor actor = (Actor) fxSorted[fxi];

                    if (stateTransitionActors.contains(actor)) {
                        continue;
                    }

                    stateTransitionActors.add(actor);
                    stateRelatedActors.add(actor);

                    if (actor instanceof CTStepSizeControlActor) {
                        stateSSCActorSchedule.add(new Firing(actor));
                    }
                }

                // A CTCompositeActor can also be served as a state transition
                // actor. To preserve topological order, append it to the
                // end of found state transition actors.
                // FIXME: is this necessary? get a two cascaded integrators
                // as a test.
                if ((dynamicActor instanceof CTCompositeActor)
                        && !stateTransitionActors.contains(dynamicActor)) {
                    stateTransitionActors.add(dynamicActor);
                }
            }
        }

        // Create StateTransitionActorSchedule.
        Iterator stActors = stateTransitionActors.iterator();

        while (stActors.hasNext()) {
            Actor stActor = (Actor) stActors.next();
            stateTransitionSchedule.add(new Firing(stActor));
        }

        // Construct a list of sink actors.
        sinkActors = (LinkedList) continuousActors.clone();
        sinkActors.removeAll(stateRelatedActors);

        // NOTE: Sink actors also include all the CT subsystems.
        sinkActors.addAll(ctSubsystems);

        // Create a schedule for the sink actors.
        if (!sinkActors.isEmpty()) {
            arithmeticGraph = _toArithmeticGraph(sinkActors);

            Object[] sinkArray = sinkActors.toArray();
            Object[] gxSorted = arithmeticGraph.topologicalSort(sinkArray);

            for (int i = 0; i < gxSorted.length; i++) {
                Actor a = (Actor) gxSorted[i];
                outputSchedule.add(new Firing(a));

                if (!eventGenerators.contains(a)
                        && a instanceof CTStepSizeControlActor) {
                    outputSSCActorSchedule.add(new Firing(a));
                }
            }
        }

        // Create the CTSchedule. Note it must be done in this order.
        ctSchedule.add(continuousActorSchedule);
        ctSchedule.add(discreteActorSchedule);
        ctSchedule.add(dynamicActorSchedule);
        ctSchedule.add(eventGeneratorSchedule);
        ctSchedule.add(outputSchedule);
        ctSchedule.add(outputSSCActorSchedule);
        ctSchedule.add(stateTransitionSchedule);
        ctSchedule.add(statefulActorSchedule);
        ctSchedule.add(stateSSCActorSchedule);
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
     *  @exception IllegalActionException If the parameter cannot
     *   contain the specified value.
     */
    private static void _setOrCreate(NamedObj container, String name,
            String value) throws IllegalActionException {
        Variable parameter = (Variable) container.getAttribute(name);

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
        final CompositeActor container = (CompositeActor) director.getContainer();

        ChangeRequest request = new ChangeRequest(this, "Record signal types") {
                protected void _execute() throws KernelException {
                    Iterator entities = container.deepEntityList().iterator();

                    while (entities.hasNext()) {
                        Entity entity = (Entity) entities.next();

                        for (Iterator ports = entity.portList().iterator();
                             ports.hasNext();) {
                            IOPort port = (IOPort) ports.next();
                            String typeString = typeMap.getType(port).toString();
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
            Actor actor = (Actor) actors.next();
            graph.addNodeWeight(actor);
        }

        // Create the edges.
        actors = list.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            // CTCompositeActor is basically everything,
            // it may be an event generator, or a state transition
            // actor.
            if ((actor instanceof CTCompositeActor)
                    || (!(actor instanceof CTDynamicActor)
                            && !(actor instanceof CTEventGenerator))) {
                // Find the successors of the actor
                Iterator successors = successorList(actor).iterator();

                while (successors.hasNext()) {
                    Actor successor = (Actor) successors.next();

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
            Actor a = (Actor) actors.next();
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

    // The signal types of all the ports of the container and the
    // contained actors.
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
                return (CTReceiver.SignalType) _map.get(port);
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
                             .getContainer())
                            && port.isInput())) {
                    Receiver[][] receivers = port.getReceivers();

                    for (int i = 0; i < receivers.length; i++) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            ((CTReceiver) receivers[i][j]).setSignalType(type);
                        }
                    }
                }

                if ((port.getContainer() == CTScheduler.this.getContainer()
                            .getContainer())
                        && port.isOutput()) {
                    Receiver[][] receivers = port.getInsideReceivers();

                    for (int i = 0; i < receivers.length; i++) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            ((CTReceiver) receivers[i][j]).setSignalType(type);
                        }
                    }
                }

                Entity actor = (Entity) port.getContainer();

                if ((type == CONTINUOUS)
                        && (actor != CTScheduler.this.getContainer()
                                .getContainer())
                        && !_continuousActors.contains(actor)) {
                    //System.out.println(actor.getName() + " is CONTINUOUS.");
                    _continuousActors.add(actor);
                }

                if ((type == DISCRETE)
                        && (actor != CTScheduler.this.getContainer()
                                .getContainer())
                        && !_discreteActors.contains(actor)) {
                    //System.out.println(actor.getName() + " is DISCRETE.");
                    _discreteActors.add(actor);
                }
            } else {
                CTReceiver.SignalType previousType = (CTReceiver.SignalType) _map
                    .get(port);

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
        public void propagateType(IOPort port) throws NotSchedulableException {
            if (!_map.containsKey(port)) {
                throw new InternalErrorException(port.getFullName()
                        + " type unknown.");
            }

            // Iterate over all ports that can receive data from this one.
            // This includes input ports lower in the hierarchy or output
            // ports higher in the hierarchy.
            Iterator connectedPorts = port.sinkPortList().iterator();

            while (connectedPorts.hasNext()) {
                IOPort nextPort = (IOPort) connectedPorts.next();

                if (!_map.containsKey(nextPort)) {
                    // check whether the nextPort has a parameter setting
                    // its signal type. compare it with the propagateType
                    // to see whether type conflict happens.
                    Parameter signalType = (Parameter) nextPort.getAttribute(
                            "signalType");
                    String configuredType;

                    if (signalType != null) {
                        try {
                            configuredType = ((StringToken) signalType.getToken())
                                .stringValue();
                            configuredType = configuredType.trim().toUpperCase();

                            String propagateType = signalTypeToString(getType(
                                                                              port));

                            if ((configuredType.compareToIgnoreCase("UNKNOWN") != 0)
                                    && (propagateType
                                            .compareToIgnoreCase(configuredType) != 0)) {
                                throw new NotSchedulableException(
                                        "Signal type conflict: "
                                        + port.getFullName() + " (of type "
                                        + configuredType + ") and "
                                        + nextPort.getFullName() + " (of type "
                                        + propagateType + ")"
                                        + "). Perhaps the connection has "
                                        + "sequence semantics?");
                            }
                        } catch (IllegalActionException e) {
                            throw new NotSchedulableException("The signal"
                                    + " type parameter does not contain a valid"
                                    + " value.");
                        }
                    }

                    setType(nextPort, getType(port));
                } else if (getType(port) != getType(nextPort)) {
                    throw new NotSchedulableException("Signal type conflict: "
                            + port.getFullName() + " (of type "
                            + signalTypeToString(getType(port)) + ") and "
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
                IOPort nextPort = (IOPort) connectedPorts.next();

                //System.out.println("Propagate type from port "
                //    + port.getFullName() + " to port "
                //    + nextPort.getFullName());
                if (!_map.containsKey(nextPort)) {
                    setType(nextPort, getType(port));
                } else if (getType(port) != getType(nextPort)) {
                    throw new NotSchedulableException("Signal type conflict: "
                            + port.getFullName() + " (of type "
                            + signalTypeToString(getType(port)) + ") and "
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
                    IOPort port = (IOPort) ports.next();
                    String type = signalTypeToString(getType(port));
                    buffer.append("  " + port.getFullName() + " :: " + type
                            + "\n");

                    if (type.equals("UNKNOWN")) {
                        throw new InternalErrorException(
                                "Found unsolved signal type at "
                                + port.getFullName() + " :: " + type);
                    }
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
