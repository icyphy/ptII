/* This director implements the Ptides programming model.

@Copyright (c) 2008-2011 The Regents of the University of California.
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

package ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CausalityMarker;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.ptides.lib.ActuatorSetup;
import ptolemy.domains.ptides.lib.ExecutionTimeListener;
import ptolemy.domains.ptides.lib.ExecutionTimeListener.ExecutionEventType;
import ptolemy.domains.ptides.lib.NetworkReceiver;
import ptolemy.domains.ptides.lib.NetworkTransmitter;
import ptolemy.domains.ptides.lib.SensorHandler;
import ptolemy.domains.ptides.lib.io.NetworkReceiverPort;
import ptolemy.domains.ptides.lib.io.NetworkTransmitterPort;
import ptolemy.domains.ptides.lib.io.PeriodicSamplingSensorPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;

/** This director implements the Ptides programming model,
 *  which is used for the design of distributed real-time systems.
 *
 *  <p> The Ptides director is based on the DE director. Like the DE
 *  director, this director maintains a totally ordered set of events,
 *  and processes these events in the order defined by their
 *  timestamps. These timestamps are also referred to as model
 *  time. In particular, if an event of timestamp tau is being
 *  processed by the director, then we say the current model time of
 *  the director is tau. Unlike most other Ptolemy directors, the
 *  Ptides directors are forced to be placed within a composite actor,
 *  with an enclosing director on the outside. Also unlike most other
 *  directors, where the local notion of
 *  model time is tightly coupled with that of the enclosing
 *  director, this director's notion of model time
 *  is decoupled from that of the enclosing director. This design
 *  allows the use of time in the enclosing
 *  director to simulate time in the physical world. Currently, only
 *  the DE director can be used as the enclosing director. One reason
 *  for using the DE director is that time cannot go backwards in DE,
 *  which is an important physical time property. More importantly,
 *  the fire method of this director changes the persistent state of
 *  actors, which means this director cannot be used inside of an
 *  actor that performs fix point iteration, which includes (currently),
 *  Continuous, CT and SR. For more details, please refer
 *  to Edward A. Lee, Haiyang Zheng. <a
 *  href="http://chess.eecs.berkeley.edu/pubs/430.html">Leveraging
 *  Synchronous Language Principles for Heterogeneous Modeling
 *  and Design of Embedded Systems</a>, Proceedings of the
 *  7th ACM & IEEE international conference on Embedded
 *  software, ACM, 114-123, 2007.</p>
 *
 *  <p> This director provides a set of features to address both
 *  the distributed and the real-time aspects of system design.
 *  To address the distributed aspect, each composite actor that
 *  has a Ptides director inside simulates a computation platform
 *  (e.g., a microprocessor), while the enclosing director simulates
 *  the physical world. Actors under the Ptides director then communicate
 *  to the outside via sensors, actuators, or networks. These
 *  components are simulated by input/output ports of the composite
 *  actors, as well as special actors that simulate network devices.
 *  </p>
 *
 *  <p> To address the real-time aspect, this director allows for the
 *  simulation of system timing behavior. This is achieved by allowing
 *  actors in the Ptides model to be annotated by parameters such as
 *  <i>WCET</i> and <i>executionTime</i>, (@see PtidesActorProperties),
 *  while requiring a Ptides director to have an enclosing DE director
 *  on the outside. The enclosing
 *  director's notion of time is used to simulate the passage of
 *  physical time based on the <i>WCET</i> and <i>executionTime</i>
 *  parameters of Ptides' actors. Notice, this simulated physical
 *  time is different from the actual time it takes to run the
 *  simulation. The actual time it takes to run the simulation
 *  (here on referred to as wall clock time) depends on the clock
 *  frequency of the computer that runs the simulation, while
 *  the simulated physical time depends on the clock frequency of
 *  the microprocessor that runs the Ptides implementation.</p>
 *
 *  <p> There are also three versions of simulated physical time within the
 *  Ptides environment: oracle time, platform time and execution time.
 *  These times help
 *  to capture the time synchronizations in a distributed environment.
 *  In such an environment, all platforms are assumed to be synchronized
 *  within a bounded error. Thus we assume there is a oracle that holds
 *  the "correct" time in the system, and this oracle time is the model
 *  time of the enclosing DE director. Each platform then has two local
 *  clocks that track this time. One is a execution clock, which simulates
 *  the main clock that triggers the CPU. It is used to simulate how long
 *  it takes for a particular actor to finish its execution. The other is
 *  the platform clock, which simulates the system timer. For example, when the sensor
 *  produces a timestamped event, the timestamp is retrieved using the platform
 *  clock. The execution clock and the platform clock simulate execution time
 *  and platform time, respectively.
 *
 *  <p> Each of the clocks is an instance of the
 *  {@link RealTimeClock} inner class. The clock keeps track of its time by
 *  saving a pair of Time values, the oracle time, and the execution or platform
 *  time, depending on the clock used. This pair of Time values correspond to the oracle time
 *  execution/platform time at the same wall clock time. In other words, since
 *  the execution/platform time drifts away from the oracle time, the saved
 *  pair indicates a starting point at which these values are "simultaneous". A clock
 *  drift parameter then tracks the drift of the execution/platform time with
 *  respect to the oracle time. If the times are perfectly synchronized, then
 *  the clock drift takes a value of 1.0. If the clock drift is bigger than
 *  1.0, then the execution/platform clock runs faster than the
 *  oracle time clock. If the clock drift is 0.0, that means the execution/platform
 *  time does not change as oracle time changes. The clock drift is not allowed
 *  to take a negative value. That is, as the oracle time increases, the
 *  execution/platform time cannot decrease. Every time the clock drift
 *  changes, the pair of saved execution/platform and oracle time needs to be
 *  updated. Currently, the execution/platform clocks can only be updated by
 *  changing the clock drift.
 *
 *  <p> While in some hardware platforms the platform clock and the execution
 *  clock are
 *  closely related (e.g., they may be tied to the same oscillator, but
 *  frequency divided by different factors), this is not true in general.
 *  Moreover, the above assumption does not hold in most Ptides systems that
 *  implement clock synchronization. There usually exists separate hardware
 *  that performs clock synchronization. Clock drifts are updated as time
 *  synchronization packets are received at the hardware. In the case platform
 *  and execution times are the same,
 *  a single {@link RealTimeClock} object can be created, and can be set to
 *  both the execution and platform clock.</p>
 *
 *  We assume all execution times are in terms of oracle time. That
 *  is, if an actor is annotated with <i>executionTime</i> equal to <i>t</i>,
 *  an event that triggers this actor will take exactly <i>t</i> amount of
 *  oracle time to process.</p>
 *
 *  <p> The platform time is used in the following
 *  situations: generating timestamps for sensor events, enforcing deadlines
 *  for actuation events, and to setup the wake-up time for timed interrupts.
 *  Also, the Ptides operational semantics assumes
 *  a bound in the time synchronization error. This error is captured in the
 *  parameter {@link #assumedPlatformTimeSynchronizationErrorBound}. If
 *  the actual error exceeds this bound, the safe-to-process analysis could
 *  produce an incorrect result. The demo PtidesNetworkLatencyTest illustrates
 *  this error.</p>
 *
 *  <p> The simulation of physical time makes it possible
 *  to simulate event preemption. However, in this basic version,
 *  the director does not simulate event preemption. Instead it's left
 *  for the subclass to implement preemptive scheduling behaviors.
 *  On the other hand, this scheduler simulates scheduling overhead,
 *  which is the amount of time for a scheduler to determine what is
 *  the next event to be executed. The parameter
 *  {@link #schedulerExecutionTimeBound} bounds the amount of physical
 *  time it takes for the Ptides scheduler to make its scheduling
 *  decision. This decision is made whenever a sensor or timed
 *  interrupt occurs, or when an event has finished processing. Notice
 *  in actual implementation, the actual scheduling overhead may be
 *  less than this bound, since the overhead might be dependent on the
 *  number of events in the system. However, the currently implementation
 *  takes a conservative approach and only simulates the overhead as if
 *  scheduling always takes maximum bounded time to execute.</p>
 *
 *  <p> The following paragraphs describe implementation details of this
 *  director. The implementation is based on the operational semantics
 *  of Ptides, as described in: Jia Zou, Slobodan Matic, Edward
 *  A. Lee, Thomas Huining Feng, Patricia Derler.  <a
 *  href="http://chess.eecs.berkeley.edu/pubs/529.html">Execution
 *  Strategies for Ptides, a Programming Model for Distributed
 *  Embedded Systems</a>, 15th IEEE Real-Time and Embedded Technology
 *  and Applications Symposium, 2009, IEEE Computer Society, 77-86,
 *  April, 2009.</p>
 *
 *  <p> Since sensors, actuators, and network devices are important
 *  in the context of Ptides, special actors such as {@link SensorHandler}
 *  {@link ActuatorSetup}, {@link NetworkReceiver},
 *  {@link NetworkTransmitter} are used in Ptides models. These actors
 *  must only be connected to input and output ports of the composite
 *  actor governed by the Ptides director. In addition, input
 *  ports to the Ptides director hold information about
 *  real time delays (see paper reference above)
 *  at sensors and network inputs. If an input port is connected to a
 *  SensorHandler, the port is considered a sensor port, and it
 *  could be annotated with parameter <i>deviceDelay</i>, which is of
 *  type double. If an input port is
 *  connected to a NetworkReceiver, the port is considered a network
 *  port, and could be annotated with <i>networkDelay</i> and
 *  <i>networkDriverDelay</i>
 *  parameters (the difference between these parameters are explained in
 *  the following paragraph). Both of these parameters are of type double.
 *  Note a port can be either a sensor, network input, network output, or
 *  actuator port, but it cannot be more than one of these all at once.
 *  If an input port is not annotated and
 *  if it is not connected to either a SensorHandler or a
 *  NetworkReceiver, the port is assumed to be a sensor port. We make
 *  this assumption because the SensorHandler is not necessarily
 *  needed to simulate the functionality of the Ptides model, while
 *  NetworkReceiver is. The same reasoning applies to actuator ports
 *  as well. However, if the {@link #schedulerExecutionTimeBound}
 *  parameter of the director is set to a non-zero value, sensor and actuator
 *  ports must be connected to SensorHandler and ActuationOutputDevice,
 *  respectively, in order
 *  to correctly simulate the scheduling overhead after each interrupt
 *  event.</p>
 *
 *  <p> While the <i>networkDelay</i> and <i>networkDriverDelay</i> parameters
 *  both characterize the platform physical time delay at the receiving end
 *  of a network interface, there are subtle differences between these two
 *  parameters. The <i>networkDelay</i> parameter is used to characterize the
 *  amount of simulated platform physical time
 *  delay experience by a packet, between when the packet first leaves the
 *  source platform, and when the packet arrives at the destination platform.
 *  This delay should be modeled in the enclosing
 *  DE director, while the maximum bound of this delay needs to be annotated
 *  as <i>networkDelay</i> at the input port of the destination platform. If it
 *  is not properly annotated, safe-to-process analysis of the Ptides platform
 *  could produce false positive results. On the other hand, the
 *  <i>networkDriverDelay</i>
 *  specifies the amount of physical time it takes for a packet to be
 *  consumed and an event produced in the destination platform. Note, the
 *  <i>d_o</i> parameter for network inputs is calculated by summing
 *  <i>networkDelay</i> and <i>networkDriverDelay</i>.</p>
 *
 *  <p> Like input ports, output ports of the composite actor governed by the
 *  Ptides director can also be annotated. By default, the director checks
 *  whether an output event's timestamp is smaller or equal to the simulated
 *  platform physical time of when this event is first produced. If the check
 *  fails, a deadline miss is implied, and
 *  the director throws an exception. If the check passes, the director
 *  transfers this event to the outside of the platform at physical time equal
 *  to the timestamp of the output event. However, if the output port is
 *  annotated with an <i>ignoreDeadline</i> parameter, then the director does
 *  not throw an exception. Instead, if the simulated physical time is smaller
 *  than the timestamp of the output event, the output event is transferred
 *  to the outside immediately. The output port could also be annotated with
 *  a parameter <i>transferImmediately</i>. If the parameter is true, then all
 *  events arriving at the output port will be transferred to the outside
 *  immediately, otherwise, the director will transfer these events to the
 *  outside when physical time equals the timestamp of the event.</p>
 *
 *  <p> The semantics of Ptides is based DE, which specifies
 *  all actors must process events in timestamp order. DE enforces
 *  this semantics by processing all events in the system in timestamp
 *  order, and by coupling the model time of the DE director with that
 *  of the enclosing director. The Ptides director however,
 *  decouples its time with that of the enclosing director, and
 *  instead enforces timestamp order processing by defining a
 *  safe-to-process analysis in its operational semantics.
 *  This analysis returns a boolean to indicate whether an event
 *  can be processed without violating Ptides
 *  semantics, based on information such as events currently in the
 *  event queue, their model time relationship with each other, as
 *  well as the current platform physical time. In this particular version of
 *  the Ptides scheduler, the director takes the earliest (smallest
 *  timestamp) event from the event queue, and compares its timestamp
 *  with the current platform physical time + a pre-computed offset (call this
 *  the delayOffset). If the physical time is larger, then this event
 *  is safe to process. Otherwise, we wait for physical time to pass
 *  until this event becomes safe, at which point it is processed. For
 *  more detailed information about how the delayOffset parameter is
 *  calculated, refer to the above paper reference. This is the most
 *  basic version of the Ptides scheduler (thus the name), however
 *  subclasses of this director may provide more sophisticated and
 *  concurrent scheduling algorithms.</p>
 *
 *  <p> Like in the DE domain, directed loops of IO ports with no
 *  model time delay will trigger an exception. For detailed
 *  explanation, @see DEDirector. The DE director uses {@link
 *  BooleanDependency} to define whether there is a model time
 *  delay between input and output ports of an actor. The Ptides
 *  director however, uses {@link SuperdenseDependency} to not only
 *  indicate whether model time delay exists, but also what the delay
 *  value is. It then uses this information to perform the safe-to-
 *  process analysis.</p>
 *
 *  <p> In the preinitialize method, the director calculates
 *  delayOffsets according to the model graph structure. The director
 *  uses superdense dependency between connected components to perform
 *  this calculation.</p>
 *
 *  <p> An event in the Ptides programming model is similar to an
 *  event in DE. A {@link ptolemy.domains.ptides.kernel.PtidesEvent}
 *  also consists of a timestamp, a value token, as well as fields
 *  such as microstep and depth, which are used to define the
 *  scheduling semantics. Because of these similarities, PtidesEvent
 *  is a subclass of DEEvent. While DEEvent only conceptually includes
 *  the value token (the actual token is sent directly to the receiver
 *  upon the creation of the new event),
 *  PtidesEvent actually stores the token as a part of its data structure,
 *  in order to enable out-of-order processing of events. The token is
 *  transmitted to the destination receiver when the Ptides director
 *  is ready to process the corresponding event.
 *  @see PtidesEvent
 *  FIXME: This class is too big, to refactor it, all the methods that deal
 *  with calculation of delayOffsets should be put in another class.
 *
 *  @author Patricia Derler, Edward A. Lee, Ben Lickly, Isaac Liu, Slobodan Matic, Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */
public class PtidesBasicDirector extends DEDirector {

    /** Construct a PtidesBasicDirector with the specified container and name.
     *  Parameters for this director are created and initialized.
     *  @param container The container
     *  @param name The name
     *  @exception IllegalActionException If the superclass throws it, or if
     *  creation or initialization of the director parameters failed.
     *  @exception NameDuplicationException If the superclass throws it, or
     *  if the container already contains an entity with the specified name.
     */
    public PtidesBasicDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        animateExecution = new Parameter(this, "animateExecution");
        animateExecution.setTypeEquals(BaseType.BOOLEAN);
        animateExecution.setExpression("false");

        assumedPlatformTimeSynchronizationErrorBound = new SharedParameter(
                this, "assumedPlatformTimeSynchronizationErrorBound");
        assumedPlatformTimeSynchronizationErrorBound
                .setTypeEquals(BaseType.DOUBLE);
        assumedPlatformTimeSynchronizationErrorBound.setExpression("0.0");

        forceActorsToProcessEventsInTimestampOrder = new Parameter(this,
                "forceActorsToProcessEventsInTimestampOrder");
        forceActorsToProcessEventsInTimestampOrder
                .setTypeEquals(BaseType.BOOLEAN);
        forceActorsToProcessEventsInTimestampOrder.setExpression("false");

        highlightModelTimeDelay = new Parameter(this, "highlightModelTimeDelay");
        highlightModelTimeDelay.setTypeEquals(BaseType.BOOLEAN);
        highlightModelTimeDelay.setExpression("false");

        initialExecutionClockTime = new Parameter(this,
                "initialExecutionSynchronizationError");
        initialExecutionClockTime.setTypeEquals(BaseType.DOUBLE);
        initialExecutionClockTime.setExpression("0.0");

        initialExecutionClockDrift = new Parameter(this, "executionClockDrift");
        initialExecutionClockDrift.setTypeEquals(BaseType.DOUBLE);
        initialExecutionClockDrift.setExpression("1.0");

        initialPlatformClockTime = new Parameter(this,
                "initialPlatformSynchronizationError");
        initialPlatformClockTime.setTypeEquals(BaseType.DOUBLE);
        initialPlatformClockTime.setExpression("0.0");

        initialPlatformClockDrift = new Parameter(this, "platformClockDrift");
        initialPlatformClockDrift.setTypeEquals(BaseType.DOUBLE);
        initialPlatformClockDrift.setExpression("1.0");

        safeToProcessTimerHandlingOverhead = new Parameter(this,
                "safeToProcessTimerHandlingOverhead");
        safeToProcessTimerHandlingOverhead.setTypeEquals(BaseType.DOUBLE);
        safeToProcessTimerHandlingOverhead.setExpression("0.0");

        schedulerExecutionTimeBound = new Parameter(this,
                "schedulerExecutionTime");
        schedulerExecutionTimeBound.setTypeEquals(BaseType.DOUBLE);
        schedulerExecutionTimeBound.setExpression("0.0");
        
        // Force isEmbedded() to always return false.
        setEmbedded(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, modify the color icon for actors in this director
     *  to indicate the state of execution as the model is running.
     *  This is a boolean that defaults to false.
     *  <p> Note this
     *  parameter is specially used for the Ptides directors, and
     *  is different from the animateExecution parameter in the
     *  Ptolemy debug menu. While the animateExecution parameter
     *  in the debug menu animates for the duration of an actor
     *  firing in wall clock time, this parameter causes the
     *  director to animate for the duration of simulated physical
     *  time, which is the time of the enclosing DE director.</p>
     *  <p> Also note this parameter must be set with conjunction
     *  of the <i>synchronizedToRealTime</i> parameter of the
     *  enclosing DE director.
     */
    public Parameter animateExecution;

    /** Store the estimated platform time synchronization error bound in the
     *  platform governed by
     *  this Ptides director. In a distributed Ptides environment,
     *  each distributed platform is modeled by a composite actor that
     *  is governed by a Ptides director (or its subclass). In
     *  reality, these platforms will have (physical) time
     *  synchronization errors between them. This error is captured
     *  within this parameter.
     */
    public SharedParameter assumedPlatformTimeSynchronizationErrorBound;

    /** The clock that keeps track of the execution time on this platform.
     */
    public RealTimeClock executionTimeClock;

    /** If true, force all actors to process events in timestamp
     *  order, even though some actors (in particular those without
     *  states) could process events out of timestamp order without
     *  affecting the deterministic behavior of the system. This
     *  could make the safe-to-process analysis simpler, while
     *  sacrificing concurrency of event execution in other parts
     *  of the model.
     */
    public Parameter forceActorsToProcessEventsInTimestampOrder;

    /** When set to true, highlight all deeply contained actors
     *  that have non-zero model time delays (including
     *  actors that only introduce microstep delays).
     *  When set to false, remove any such highlighting.
     *  This is a boolean that defaults to false.
     */
    public Parameter highlightModelTimeDelay;

    /** The initial clock drift rate of the execution clock. The type of this
     *  clock drift is a double, and the default value is 1.0, indicating the
     *  execution clock runs at the same rate as the oracle clock. The oracle
     *  clock is modeled by the model time of the enclosing DE director.
     */
    public Parameter initialExecutionClockDrift;

    /** The initial execution time clock time. The initial oracle clock time
     *  zero. If the initial execution clock time differs from that time, then
     *  that time is captured in this parameter.
     */
    public Parameter initialExecutionClockTime;

    /** The initial clock drift rate of the platform clock. The type of this
     *  clock drift is a double, and the default value is 1.0, indicating the
     *  platform clock runs at the same rate as the oracle clock. The oracle
     *  clock is modeled by the model time of the enclosing DE director.
     */
    public Parameter initialPlatformClockDrift;

    /** The initial platform time clock time. The initial oracle clock time
     *  zero. If the initial platform clock time differs from that time, then
     *  that time is captured in this parameter. Note this parameter is
     *  different from the assumedPlatformTimeSynchronizationErrorBound
     *  in that the other parameter is the estimated bound, while
     *  this parameter stores the initial difference.
     */
    public Parameter initialPlatformClockTime;

    /** The clock that keeps track of the platform time on this platform.
     */
    public RealTimeClock platformTimeClock;

    /** The amount of execution time it takes for the system to handle the
     *  safe-to-process timer. This value is used to simulate execution
     *  time.
     */
    public Parameter safeToProcessTimerHandlingOverhead;

    /** A Parameter representing the bound on the simulated scheduling
     *  overhead time. In real-time programs, it takes time for the scheduler
     *  to schedule a particular event processing. While simulating the
     *  passage of physical time, this Parameter
     *  is used to capture that scheduling overhead. Note, though this
     *  parameter gives the bound on scheduling overhead, in simulation,
     *  we use the value of this parameter to simulate the actual
     *  scheduling overhead.
     */
    public Parameter schedulerExecutionTimeBound;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the director parameters when the attribute has changed.
     *  If the parameter <i>highlightModelTimeDelays</i> has changed,
     *  then all actors in this director should be highlighted.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If model delays cannot be
     *  highlighted, delay dependencies cannot be declared, attribute
     *  cannot be changed to a particular value, or the super class
     *  throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == highlightModelTimeDelay) {
            for (Actor actor : (List<Actor>) ((CompositeEntity) getContainer())
                    .deepEntityList()) {
                if (actor instanceof AtomicActor) {
                    ((AtomicActor) actor).declareDelayDependency();
                }
            }
            if (((BooleanToken) highlightModelTimeDelay.getToken())
                    .booleanValue()) {
                _highlightModelDelays((CompositeActor) getContainer(), true);
                _timeDelayHighlighted = true;
            } else {
                if (_timeDelayHighlighted) {
                    _timeDelayHighlighted = false;
                    _highlightModelDelays((CompositeActor) getContainer(),
                            false);
                }
            }
        }
    }

    /**
     * Return the default dependency between input and output ports,
     * which for the Ptides domain is a {@link SuperdenseDependency}.
     *
     * @return The default dependency that describes a time delay of 0.0,
     *          and a index delay of 0 between ports.
     */
    public Dependency defaultDependency() {
        return SuperdenseDependency.OTIMES_IDENTITY;
    }

    /**
     * Return a {@link SuperdenseDependency} representing a model-time
     * delay of the specified amount.
     *
     * @param delay
     *            The real (timestamp) part of the delay.
     * @param index
     *            The integer (microstep) part of the delay
     * @return A Superdense dependency representing a delay.
     */
    public Dependency delayDependency(double delay, int index) {
        return SuperdenseDependency.valueOf(delay, index);
    }

    /** Return the assumed platform synchronization error bound of this platform.
     *  @see #assumedPlatformTimeSynchronizationErrorBound
     *  @return the assumed synchronization error bound.
     *  @exception IllegalActionException If assumedPlatformTimeSynchronizationErrorBound
     *   parameter does not contain a valid token.
     */
    public double getAssumedSynchronizationErrorBound()
            throws IllegalActionException {
        return ((DoubleToken) assumedPlatformTimeSynchronizationErrorBound
                .getToken()).doubleValue();
    }

    /** Return the current clock drift associated with the real
     *  time clock.
     *  @param realTimeClock The real time clock.
     *  @return the current clock drift associated with the clock ID.
     *  @exception IllegalActionException If clock ID is not recognized.
     */
    public double getClockDrift(RealTimeClock realTimeClock)
            throws IllegalActionException {
        return realTimeClock._clockDrift;
    }

    /** Return the current {@link ptolemy.domains.ptides.kernel.Tag}.
     *  @return timestamp and microstep (A microstep is an
     *  integer which represents the index of the sequence of execution
     *  phases when this director processes events with the same timestamp)
     *  of the current model time.
     */
    public Tag getModelTag() {
        return new Tag(getModelTime(), _microstep);
    }

    /** Get the simulated platform physical time of the environment, which is the oracle
     *  physical time offset by the clock synchronization error due to clock
     *  drift.
     *  @param realTimeClock The real time clock.
     *  @return the platform physical time.
     *  @exception IllegalActionException If director cannot get token for the
     *  parameter platformTimeSynchronizationError.
     *  @exception InternalErrorException If the platform physical time of the
     *  corresponding current oracle time cannot be retrieved.
     */
    public Tag getPlatformPhysicalTag(RealTimeClock realTimeClock)
            throws IllegalActionException {
        Tag tag = _getOraclePhysicalTag();
        HashMap<Time, Time> timeCachePair;
        if (realTimeClock == platformTimeClock) {
            timeCachePair = _oraclePlatformTimePair;
        } else if (realTimeClock == executionTimeClock) {
            timeCachePair = _oracleExecutionTimePair;
        } else {
            throw new InternalErrorException(
                    "The real time clock is not recognized");
        }
        tag.timestamp = _getPlatformPhysicalTimeForOraclePhysicalTime(
                tag.timestamp, realTimeClock, timeCachePair);
        if (tag.timestamp == null) {
            throw new InternalErrorException("The platform physical "
                    + "time at the current oracle time cannot be retrieved.");
        }
        return tag;
    }

    /** Print out debugging information if we are in debugging mode and call
     *  fire() of the super class.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("========= PtidesBasicDirector fires at " + getModelTime()
                    + "  with microstep as " + _microstep);
        }

        super.fire();

        if (_debugging) {
            _debug("PtidesBasicDirector fired!");
        }
    }

    /** Create new variables, initialize the actors and request a refiring
     *  at the current
     *  time of the executive director. This overrides the base class to
     *  throw an exception if there is no executive director.
     *  @exception IllegalActionException If the superclass throws
     *  it or if there is no executive director.
     */
    public void initialize() throws IllegalActionException {
        _currentlyExecutingStack = new Stack<ExecutionTimedEvent>();
        _realTimeInputEventQueue = new PriorityQueue<RealTimeEvent>();
        _realTimeOutputEventQueue = new PriorityQueue<RealTimeEvent>();
        _lastConsumedTag = new HashMap<NamedObj, Tag>();
        _pureEventSourcePorts = new HashMap<NamedObj, IOPort>();
        _physicalTimeExecutionStarted = null;
        _schedulerFinishTime = new Time(this, Double.NEGATIVE_INFINITY);
        _scheduleNewEvent = false;
        _timedInterruptTimes = new LinkedList<TimedEvent>();
        _eventsWithTimedInterrupt = new HashSet<PtidesEvent>();
        _futureExecutionFireAtTimes = new LinkedList<Time>();
        _ignoredExecutionFireAtTimes = new LinkedList<Time>();
        _futurePlatformFireAtTimes = new LinkedList<Time>();
        _ignoredPlatformFireAtTimes = new LinkedList<Time>();
        _lastExecutingActor = null;
        executionTimeClock = new RealTimeClock(
                ((DoubleToken) initialExecutionClockTime.getToken())
                        .doubleValue(),
                ((DoubleToken) initialExecutionClockDrift.getToken())
                        .doubleValue(), this);
        platformTimeClock = new RealTimeClock(
                ((DoubleToken) initialPlatformClockTime.getToken())
                        .doubleValue(),
                ((DoubleToken) initialPlatformClockDrift.getToken())
                        .doubleValue(), this);
        _oraclePlatformTimePair = new HashMap<Time, Time>();
        _oracleExecutionTimePair = new HashMap<Time, Time>();

        super.initialize();

        (((Actor) getContainer()).getExecutiveDirector())
                .fireAtCurrentTime((Actor) getContainer());

        _setIcon(_getIdleIcon(), true);

    }

    /** Return a new receiver of the type {@link PtidesBasicReceiver}.
     *  @return A new PtidesBasicReceiver.
     */
    public Receiver newReceiver() {
        if (_debugging && _verbose) {
            _debug("Creating a new Ptides basic receiver.");
        }

        return new PtidesBasicReceiver();
    }

    /** Return false if there are no more actors to be fired or the stop()
     *  method has been called.
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If the stopWhenQueueIsEmpty parameter
     *   does not contain a valid token, or refiring can not be requested.
     */
    public boolean postfire() throws IllegalActionException {
        // Do not call super.postfire() because that requests a
        // refiring at the next event time on the event queue.

        Boolean result = !_stopRequested && !_finishRequested;
        if (getModelTime().compareTo(getModelStopTime()) >= 0) {
            // If there is a still event on the event queue with time stamp
            // equal to the stop time, we want to process that event before
            // we declare that we are done.
            if (!_eventQueue.get().timeStamp().equals(getModelStopTime())) {
                result = false;
            }
        }
        return result;
    }

    /** Override the base class to not set model time to that of the
     *  enclosing director. This method always returns true, deferring the
     *  decision about whether to fire an actor to the fire() method.
     *  @return True.
     */
    public boolean prefire() {
        // Do not invoke the superclass prefire() because that
        // sets model time to match the enclosing director's time.
        if (_debugging) {
            _debug("Prefiring: Current time is: " + getModelTag());
        }
        return true;
    }

    /** Instantiate new model structures to get ready for a simulation run.
     *  Check if the enclosing director is a DEDirector. If not, throw
     *  an exception.
     *  Call the preinitialize() method in the super class. The superclass
     *  instantiates an event queue structure, however, here a
     *  PtidesListEventQueue structure is instantiated in its place.
     *  We do this because a Ptides scheduler not only needs to access the
     *  first event in the event queue, but all other events, in tag order.
     *  Also, the delayOffsets used in the
     *  safe-to-process analysis are calculated. This is followed by a check
     *  to see if sensors, actuators, and networks ports are annotated with
     *  the corresponding parameters, and whether they are connected to the
     *  corresponding sensor/actuator/network actors.
     *  Finally, the parameter stopWhenQueueIsEmpty is set to false.
     *  In general, Ptides
     *  models should never stop when the event queue is empty, because
     *  it can wait and react to future sensor input events.
     *  @see #_calculateDelayOffsets()
     *  @exception IllegalActionException If the enclosing director does
     *  not exist or is not a DEDirector, delayOffset cannot be calculated,
     *  sensor/actuator/network consistency cannot be checked, or if the
     *  super class throws it.
     */
    public void preinitialize() throws IllegalActionException {

        // Make sure an enclosing DE director exists.
        NamedObj container = getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(this,
                    "No container, or container is not an Actor.");
        }
        Director executiveDirector = ((Actor) container).getExecutiveDirector();
        if (executiveDirector == null
                || !(executiveDirector instanceof DEDirector)) {
            throw new IllegalActionException(this,
                    "All Ptides Directors must be used "
                            + "within a DE Director.");
        }

        // If animateExecution is set, make sure synchronizedToRealTime of the
        // enclosing DE director is also set.
        if (((BooleanToken) animateExecution.getToken()).booleanValue()) {
            Parameter parameter = (Parameter) ((NamedObj) executiveDirector)
                    .getAttribute("synchronizeToRealTime");
            if (parameter == null) {
                throw new IllegalActionException(executiveDirector, "The "
                        + "enclosing DE director is expected to have a "
                        + "synchronizedToRealTime parameter, but it "
                        + "could not be found.");
            }
            if (!((BooleanToken) parameter.getToken()).booleanValue()) {
                throw new IllegalActionException(executiveDirector, "The "
                        + "animateExecution parameter of the Ptides "
                        + "director is set. To get a realistic view "
                        + "of the actor execution, set the "
                        + "enclosing DE director's "
                        + "synchronizedToRealTime parameter to true.");
            }
        }

        super.preinitialize();
        // Initialize an event queue.
        _eventQueue = new PtidesListEventQueue();

        _checkSensorActuatorNetworkConsistency();
        
        _calculateDelayOffsets();

        // In Ptides, we should never stop when queue is empty.
        stopWhenQueueIsEmpty.setExpression("false");
    }


    
    
    /** Add a new execution time listener to the list of listeners.
     *  @param listener New listener.
     */
    public void registerExecutionTimeListener(ExecutionTimeListener listener) {
        if (_executionTimeListeners == null) {
            _executionTimeListeners = new ArrayList();
        }
        if(!_executionTimeListeners.contains(listener)) {
            _executionTimeListeners.add(listener);
        }
    }

    /** Set the microstep. A microstep is an
     *  integer which represents the index of the sequence of execution
     *  phases when this director processes events with the same timestamp.
     *  @see #getMicrostep()
     *  @param microstep An int specifying the microstep value.
     */
    public void setMicrostep(int microstep) {
        _microstep = microstep;
    }

    /** Set a new value to the current time of the model, where
     *  the new time <i>can</i> be smaller than the current model time,
     *  because Ptides allow events to be processed out of timestamp order.
     *  @param newTime The new current simulation time.
     *  @exception IllegalActionException If the new time is less than
     *   the current time returned by getCurrentTime().
     *  @see #getModelTime()
     *
     */
    public void setModelTime(Time newTime) throws IllegalActionException {
        int comparisonResult = getModelTime().compareTo(newTime);

        if (comparisonResult > 0) {
            if (_debugging) {
                _debug("==== Set current time backwards from " + getModelTime()
                        + " to: " + newTime);
            }
        } else if (comparisonResult < 0) {
            if (_debugging) {
                _debug("==== Set current time to: " + newTime);
            }
        } else {
            // the new time is equal to the current time, do nothing.
        }
        if (newTime.compareTo(Time.NEGATIVE_INFINITY) != 0) {
            localClock.setLocalTime(newTime);
        }
    }

    /** Set the timestamp and microstep of the current time.
     *  @param timestamp A Time object specifying the timestamp.
     *  @param microstep An integer specifying the microstep.
     *  @exception IllegalActionException If setting the model time is
     *  unsuccessful.
     *  @see #setModelTime(Time)
     */
    public void setTag(Time timestamp, int microstep)
            throws IllegalActionException {
        setModelTime(timestamp);
        setMicrostep(microstep);
    }

    /** Reset the icon idle if the animation for actor firing is
     *  turned on.
     *  @exception IllegalActionException If the wrapup() method of
     *  one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _setIcon(_getIdleIcon(), false);
        if (_lastExecutingActor != null) {
            _clearHighlight(_lastExecutingActor, false);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of currently executing events (including value and
     *  timestamp) and their remaining execution times.
     */
    protected Stack<ExecutionTimedEvent> _currentlyExecutingStack;
    
    /** List of listeners to be informed whenever an execution time
     *  event occurs.
     */
    protected ArrayList<ExecutionTimeListener> _executionTimeListeners;

    /** The last actor that was fired by this director. If null, then after
     *  actor firing, values saved in _pureEventDeadlines, _pureEventDelays, and
     *  _pureEventSourcePorts that are associated with this actor should be removed.
     */
    protected Actor _lastActorFired;
    
    /** Execution time of last actor fired. */
    protected Time _lastExecutionTime;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Perform bookkeeping after actor firing. This procedure consist of
     *  two actions:
     *  <ol>
     *  <li>An actor has just been fired. A token with destination to the
     *  outside of the Ptides platform could have been produced. If so,
     *  the corresponding event is taken out of event queue, and the token
     *  is placed at the actuator/network port, ready to be transferred
     *  to the outside.</li>
     *  <li> Bookkeeping structures that keep track of which actor
     *  has just fired are cleared.</li>
     *  </ol>
     *  @exception IllegalActionException If unable to get the next actuation event.
     */
    protected void _actorFired() throws IllegalActionException {

        if (_lastExecutionTime.compareTo(_zeroTime) == 0) {
            _sendExecutionTimeEvent(_lastActorFired, ExecutionEventType.START);
            _sendExecutionTimeEvent(_lastActorFired, ExecutionEventType.STOP);
        }

        _noActorToFire();

        // Now clear _pureEvent* since an actor has finished firing.
        if (_lastActorFired != null) {
            _pureEventSourcePorts.remove(_lastActorFired);
        }
    }

    /** Calculate the delayOffset for each input port in the model, and
     *  annotate the ports with these offsets.
     *  This causality analysis is called during the preinitialize phase.
     *  <p>
     *  Start from each input port that is connected to the outside of the platform
     *  (These input ports indicate sensors and network interfaces, call them startPorts),
     *  and traverse the graph until we reach the output port connected to the outside of
     *  the platform (actuators/network ports). For each input port in between,
     *  annotate it with
     *  a delayOffset parameter. This parameter is an array of doubles, where each double
     *  corresponds to the delay offset for a particular channel of that port.
     *  This delay offset is used for the safe to process analysis.
     *  <p>
     *  Note: for all transparent composite actors, the delayOffsets are not
     *  calculated for their input ports. Instead, the offsets are calculated and
     *  annotated for input ports that are inside these actors.
     *  @exception IllegalActionException If failed to clear or calculate delayOffset,
     *  cannot check whether the current port is a network port, cannot get the
     *  network delay of the current port, cannot get the real time delay of the
     *  current port, cannot get remote receivers, or cannot get the port channel
     *  for a particular receiver.
     */
    protected void _calculateDelayOffsets() throws IllegalActionException {

        // A set that keeps track of all the actors that have been traversed to.
        _visitedActors = new HashSet<Actor>();

        _clearDelayOffsets();

        // A map that saves a dependency for a port channel pair. This dependency is later used to
        // calculate delayOffset.
        _inputModelTimeDelays = new HashMap<IOPort, Map<Integer, SuperdenseDependency>>();

        // NOTE: In portDelays and localPortDelays, which saves the corresponding model time delays
        // for real-time ports to a particular port, we act as if each starting input port (which
        // is a real-time port) and each output
        // port in the graph to to have width 1, even though their width might be of some other
        // value. We do this because each output port and starting port do no need to be annotated
        // with the delayOffset parameter, and we assume all dependencies at each port is the same for
        // all channels. For regular input ports however, their width is correctly reflected in
        // portDelays and localPortDelays, because we need all model time delays to each channel
        // to calculate the delayOffset parameter.

        // FIXME: If there are composite actors within the top level composite actor,
        // does this algorithm work?
        // initialize all port model delays to infinity.
        _portDelays = new HashMap<IOPort, SuperdenseDependency>();
        if (getContainer() instanceof TypedCompositeActor) {
            for (Actor actor : (List<Actor>) (((TypedCompositeActor) getContainer())
                    .deepEntityList())) {
                for (TypedIOPort inputPort : (List<TypedIOPort>) (actor
                        .inputPortList())) {
                    _portDelays.put(inputPort,
                            SuperdenseDependency.OPLUS_IDENTITY);
                }
                for (TypedIOPort outputPort : (List<TypedIOPort>) (actor
                        .outputPortList())) {
                    _portDelays.put(outputPort,
                            SuperdenseDependency.OPLUS_IDENTITY);
                }
            }

            for (TypedIOPort inputPort : (List<TypedIOPort>) (((Actor) getContainer())
                    .inputPortList())) {
                SuperdenseDependency startDelay;
                // If the start port is a network port, the delay we start with is the
                // network delay + platform time synchronization error.
                // Otherwise the port is a sensor port, and the delay
                // we start with is the deviceDelay.
                Double start = null;
                if (_isNetworkInputPort(inputPort)) {
                    Double deviceDelayBound = _getDoubleParameterValue(inputPort, "deviceDelayBound");
                    Double networkDelayBound = _getDoubleParameterValue(inputPort, "networkDelayBound");
                    Double sourcePlatformDelayBound = _getDoubleParameterValue(inputPort, "sourcePlatformDelayBound");
                    
                    start = ((deviceDelayBound != null) ? deviceDelayBound : 0) + 
                            ((networkDelayBound != null) ? networkDelayBound : 0) + 
                            ((sourcePlatformDelayBound != null) ? sourcePlatformDelayBound : 0);
                    if (start != null) {
                        // FIXME: this is wrong, need to get the max between all
                        // differences in error bounds instead of just getting the
                        // error bound.
                        start += getAssumedSynchronizationErrorBound();
                    } else {
                        start = getAssumedSynchronizationErrorBound();
                    }
                } else {
                    start = _getDoubleParameterValue(inputPort, "deviceDelayBound");
                }
                if (start == null) {
                    start = 0.0;
                }
                startDelay = SuperdenseDependency.valueOf(-start, 0);
                _portDelays.put(inputPort, startDelay);
            }
            // Now start from each sensor (input port at the Ptides level), traverse through all
            // ports.
            for (TypedIOPort startPort : (List<TypedIOPort>) (((TypedCompositeActor) getContainer())
                    .inputPortList())) {
                _traverseToCalculateDelayOffset(startPort);
            }

            // For all unvisited actors, the sink port that's connected to its output ports
            // should be annotated with dependency SuperdenseDependency.OPLUS_IDENTITY,
            // because events arriving at these ports are always safe to process.
            for (Actor actor : (List<Actor>) ((CompositeActor) getContainer())
                    .deepEntityList()) {
                if (!_visitedActors.contains(actor)) {
                    for (IOPort port : (List<IOPort>) actor.outputPortList()) {
                        Receiver[][] remoteReceivers = port
                                .getRemoteReceivers();
                        for (int i = 0; i < remoteReceivers.length; i++) {
                            if (remoteReceivers[0] != null) {
                                for (int j = 0; j < remoteReceivers[i].length; j++) {
                                    Receiver receiver = remoteReceivers[i][j];
                                    IOPort receivePort = receiver
                                            .getContainer();
                                    int channel = receivePort
                                            .getChannelForReceiver(receiver);
                                    Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) _inputModelTimeDelays
                                            .get(receivePort);
                                    if (channelDependency == null) {
                                        channelDependency = new HashMap<Integer, SuperdenseDependency>();
                                    }
                                    channelDependency
                                            .put(Integer.valueOf(channel),
                                                    SuperdenseDependency.OPLUS_IDENTITY);
                                    _inputModelTimeDelays.put(receivePort,
                                            channelDependency);
                                }
                            }
                        }
                    }
                }
            }
        }

        // The inputModelTimeDelays hashset is the delays as calculated through
        // shortest path algorithm. Now we need to use these delays to calculate
        // the delayOffset, which is simply the minimum delay among all the
        // input model time delays.

        for (IOPort inputPort : (Set<IOPort>) _inputModelTimeDelays.keySet()) {
            Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) _inputModelTimeDelays
                    .get(inputPort);
            // Since the channel sizes may not start at 0 and grow by one monotonically, we need to traverse
            // the whole channelDependency and see what the maximum size is in order to prevent
            // a out of index error.
            int size = 1;
            for (Integer portChannelMinDelay : channelDependency.keySet()) {
                if (portChannelMinDelay.intValue() >= size) {
                    size = portChannelMinDelay.intValue() + 1;
                }
            }
            double[] delayOffsets = new double[size];
            for (Integer portChannelMinDelay : channelDependency.keySet()) {
                delayOffsets[portChannelMinDelay.intValue()] = _calculateDelayOffsetsForPortChannel(
                        inputPort, portChannelMinDelay);
            }
            _setDelayOffset(inputPort, delayOffsets);
        }
    }

    /** Check whether we should process more events in the same firing.
     *  If there are events in the event queue of the same timestamp,
     *  return true, otherwise return false.
     *  @return true if there are events of the same timestamp, otherwise
     *  return false.
     */
    protected boolean _checkForNextEvent() {
        // The following code enforces that a firing of a
        // DE director only handles events with the same tag.
        // If the earliest event in the event queue is in the future,
        // this code terminates the current iteration.
        synchronized (_eventQueue) {
            if (!_eventQueue.isEmpty()) {
                PtidesEvent next = (PtidesEvent) _eventQueue.get();

                if ((next.timeStamp().compareTo(getModelTime()) != 0)) {
                    // If the next event is in the future time,
                    // jump out of the big while loop and
                    // proceed to postfire().
                    // NOTE: we reset the microstep to 0 because it is
                    // the contract that if the event queue has some events
                    // at a time point, the first event must have the
                    // microstep as 0. See the
                    // _enqueueEvent(Actor actor, Time time) method.
                    _microstep = 1;
                    return false;
                } else if (next.microstep() != _microstep) {
                    // If the next event is has a different microstep,
                    // jump out of the big while loop and
                    // proceed to postfire().
                    return false;
                } else {
                    // The next event has the same tag as the current tag,
                    // indicating that at least one actor is going to be
                    // fired at the current iteration.
                    // Continue the current iteration.
                }
            }
        }
        return true;
    }

    /** Check the consistency of input/output ports. The following things are
     *  checked.
     *  <p>
     *  If an input port is a sensor port (no annotation), then it should not
     *  be connected to a NetworkReceiver. Also, it should not have a
     *  networkDelay attribute.
     *  </p><p>
     *  If an input port is a network port (annotated networkPort), then it
     *  should always be connected to a NetworkReceiver. Also, it should not
     *  have a deviceDelay attribute.
     *  </p>
     *  @exception IllegalActionException If sensor ports are connected to
     *  NetworkReceiver or have a networkDelay attribute; Or if a
     *  network port is not connected to a NetworkReceiver, or it has a
     *  deviceDelay attribute.
     */
    protected void _checkSensorActuatorNetworkConsistency()
            throws IllegalActionException {

        _networkInputPorts = new HashSet<IOPort>();
        _networkOutputPorts = new HashSet<IOPort>();
        
        // Find all network input ports and put them into a list.
        for (TypedIOPort port : (List<TypedIOPort>) (((TypedCompositeActor) getContainer())
                .inputPortList())) {  
            if (port instanceof NetworkReceiverPort) {
                _networkInputPorts.add(port);
            } else if (port instanceof PeriodicSamplingSensorPort) {
                
            } else {
                for (IOPort sinkPort : (List<IOPort>) port.deepInsidePortList()) {
                    if (sinkPort.isInput()) {
                        if (sinkPort.getContainer() instanceof NetworkReceiver) { 
                            _networkInputPorts.add(port);
                        }  
                    }
                } 
            }
            _checkDeviceDelay(port); 
        }
        
        // Find all network output ports and put them into a list.
        for (TypedIOPort port : (List<TypedIOPort>) (((TypedCompositeActor) getContainer())
                .outputPortList())) {  
            if (port instanceof NetworkTransmitterPort) {
                _networkOutputPorts.add(port);
            } 
            for (IOPort sourcePort : port.sourcePortList()) {
                if (sourcePort.getContainer() instanceof NetworkTransmitter) {
                    _networkOutputPorts.add(sourcePort);
                } 
            }
            _checkDeviceDelay(port); 
        } 
    }
 

    /** For each input port within the composite actor where this director
     *  resides. If the input port has a delayOffset parameter, set the value
     *  of that parameter to Infinity (meaning events arriving at this port
     *  will always be safe to process).
     *  @exception IllegalActionException If cannot evaluate the width of an
     *  input port, or if token of the parameter delayOffset cannot be evaluated.
     */
    protected void _clearDelayOffsets() throws IllegalActionException {
        if (getContainer() instanceof TypedCompositeActor) {
            for (Actor actor : (List<Actor>) (((TypedCompositeActor) getContainer())
                    .deepEntityList())) {
                for (TypedIOPort inputPort : (List<TypedIOPort>) (actor
                        .inputPortList())) {
                    Parameter parameter = (Parameter) (inputPort)
                            .getAttribute("delayOffset");
                    if (parameter != null) {
                        int channels = inputPort.getWidth();
                        if (channels > 0) {
                            Token[] tokens = new Token[channels];
                            for (int i = 0; i < channels; i++) {
                                tokens[i] = new DoubleToken(
                                        Double.POSITIVE_INFINITY);
                            }
                            parameter.setToken(new ArrayToken(tokens));
                        }
                    }
                }
            }
        }
    }

    /** Clear any highlights on the specified actor.
     *  @param actor The actor to clear.
     *  @param overwriteHighlight a boolean -- true if the current highlighting
     *  color is to be overridden.
     *  @exception IllegalActionException If the animateExecution
     *  parameter cannot be evaluated.
     */
    protected void _clearHighlight(Actor actor, boolean overwriteHighlight)
            throws IllegalActionException {
        if (((BooleanToken) animateExecution.getToken()).booleanValue()
                || overwriteHighlight) {
            String completeMoML = "<deleteProperty name=\"_highlightColor\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    (NamedObj) actor, completeMoML);
            Actor container = (Actor) getContainer();
            request.setPersistent(false);
            ((TypedCompositeActor) container).requestChange(request);
        }
    }

    /** Put a pure event into the event queue to schedule the given actor to
     *  fire at the specified timestamp.
     *  <p>
     *  The default microstep for the queued event is equal to one,
     *  unless the time is equal to the current time, where the microstep
     *  will be the current microstep plus one.
     *  </p><p>
     *  The depth for the queued event is the minimum of the depths of
     *  all the ports of the destination input port group. This depth
     *  is also stored in the actor.
     *  </p><p>
     *  If there is no event queue or the given actor is disabled, then
     *  this method does nothing.
     *  </p><p>
     *  The causal port as well as the absolute deadline for this event are
     *  also calculated.
     *  @param actor The actor to be fired.
     *  @param time The timestamp of the event.
     *  @param defaultMicrostep Ignored in this base class.
     *  @exception IllegalActionException If the time argument is less than
     *  the current model time, or the depth of the actor has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueEvent(Actor actor, Time time, int defaultMicrostep)
            throws IllegalActionException {
        if ((_eventQueue == null)
                || ((_disabledActors != null) && _disabledActors
                        .contains(actor))) {
            return;
        }
        // Reset the microstep to 1.
        int microstep = 1;

        if (time.compareTo(getModelTime()) == 0) {
            // If during initialization, do not increase the microstep.
            // This is based on the assumption that an actor only requests
            // one firing during initialization. In fact, if an actor requests
            // several firings at the same time,
            // only the first request will be granted.
            if (_isInitializing) {
                microstep = _microstep;
            } else {
                microstep = _microstep + 1;
            }
        } else if (time.compareTo(getModelTime()) < 0) {
            throw new IllegalActionException(actor,
                    "Attempt to queue an event in the past:"
                            + " Current time is " + getModelTime()
                            + " while event time is " + time);
        }

        int depth = _getDepthOfActor(actor);

        if (_debugging) {
            _debug("enqueue a pure event: ", ((NamedObj) actor).getName(),
                    "time = " + time + " microstep = " + microstep
                            + " depth = " + depth);
        }

        IOPort causalPort = _getCausalPortForThisPureEvent(actor);

        Time absoluteDeadline = _absoluteDeadlineForPureEvent(actor);

        PtidesEvent newEvent = new PtidesEvent(actor, causalPort, time,
                microstep, depth, absoluteDeadline);
        _eventQueue.put(newEvent);
    }

    /** Put a trigger event into the event queue.
     *  <p>
     *  The trigger event has the same timestamp as that of the director.
     *  The microstep of this event is always equal to the current microstep
     *  of this director. The depth for the queued event is the
     *  depth of the destination IO port. Finally, the token and the
     *  destination receiver are also stored in the event.
     *  </p><p>
     *  If the event queue is not ready or the actor that contains the
     *  destination port is disabled, do nothing.</p>
     *
     *  @param ioPort The destination IO port.
     *  @param token The token associated with this event.
     *  @param receiver The destination receiver.
     *  @exception IllegalActionException If the time argument is not the
     *  current time, or the depth of the given IO port has not be calculated,
     *  or the new event can not be enqueued.
     */
    protected void _enqueueTriggerEvent(IOPort ioPort, Token token,
            Receiver receiver) throws IllegalActionException {
        Actor actor = (Actor) ioPort.getContainer();

        if ((_eventQueue == null)
                || ((_disabledActors != null) && _disabledActors
                        .contains(actor))) {
            return;
        }
        int depth = _getDepthOfIOPort(ioPort);

        if (_debugging) {
            _debug("enqueue a trigger event for ",
                    ((NamedObj) actor).getName(), " time = " + getModelTime()
                            + " microstep = " + _microstep + " depth = "
                            + depth);
        }

        // Register this trigger event.
        PtidesEvent newEvent = new PtidesEvent(ioPort,
                ioPort.getChannelForReceiver(receiver), getModelTime(),
                _microstep, depth, token, receiver);
        _eventQueue.put(newEvent);
    }

    /** Return a collection of ports the given port is dependent on, within
     *  the same actor.
     *  This method delegates to the getDependency() method of the corresponding
     *  causality interface the actor is associated with. If getDependency()
     *  returns a dependency not equal to the oPlusIdentity, then the associated
     *  port is added to the Collection.
     *  The returned Collection has no duplicate entries.
     *  @param port The given port to find finite dependent ports.
     *  @return Collection of finite dependent ports.
     *  @exception IllegalActionException If Actor's getCausalityInterface()
     *  method, or CausalityInterface's getDependency() throws it.
     */
    protected static Collection<IOPort> _finiteDependentPorts(IOPort port)
            throws IllegalActionException {
        // FIXME: This does not support ports that are both input and output.
        // Should it?
        Collection<IOPort> result = new HashSet<IOPort>();
        Actor actor = (Actor) port.getContainer();
        CausalityInterface actorCausalityInterface = actor
                .getCausalityInterface();
        if (port.isInput()) {
            List<IOPort> outputs = ((Actor) port.getContainer())
                    .outputPortList();
            for (IOPort output : outputs) {
                Dependency dependency = actorCausalityInterface.getDependency(
                        port, output);
                if (dependency != null
                        && dependency.compareTo(dependency.oPlusIdentity()) != 0) {
                    result.add(output);
                }
            }
        } else { // port is output port.
            List<IOPort> inputs = ((Actor) port.getContainer()).inputPortList();
            for (IOPort input : inputs) {
                Dependency dependency = actorCausalityInterface.getDependency(
                        input, port);
                if (dependency != null
                        && dependency.compareTo(dependency.oPlusIdentity()) != 0) {
                    result.add(input);
                }
            }
        }
        return result;
    }

    /** Return a collection of ports that are in the same input port group.
     *  <p>
     *  A input port group is defined as follows.
     *  If there exists an output port, which both p....
     *  If input ports X and Y each have a dependency not equal to the
     *  default dependency's oPlusIdentity() on any common port
     *  or on the state of the associated actor, then they
     *  belong to the same input port group.
     *  The returned Collection has no duplicate entries.
     *  If the port is not an input port, an exception
     *  is thrown.
     *
     *  @param input The input port.
     *  @return Collection of ports belong to the same input port group.
     *  @exception IllegalActionException If failed to get finite dependent
     *  ports.
     */
    protected static Collection<IOPort> _portsBelongToTheSameInputGroup(
            IOPort input) throws IllegalActionException {
        Collection<IOPort> result = new HashSet<IOPort>();
        result.add(input);
        // First get all outputs that are dependent on this input.
        Collection<IOPort> outputs = _finiteDependentPorts(input);
        // Now for every input that is also dependent on the output, add
        // it to the list of ports that are returned.
        for (IOPort output : outputs) {
            result.addAll(_finiteDependentPorts(output));
        }
        return result;
    }

    /** Convert the platform to an oracle time based on the platform clock
     *  that is used. Call fireAt() of the executive director, which is in
     *  charge of keeping track of the simulated physical time.
     *  @param platformTime a Time object indicate the future platform time to
     *  fire.
     *  @param realTimeClock The real time clock that requested the fireAt.
     *  @exception IllegalActionException If cannot call fireAt of enclosing
     *  director, or if the oracle time is in the past, or cannot get the oracle
     *  time.
     */
    protected void _fireAtPlatformTime(Time platformTime,
            RealTimeClock realTimeClock) throws IllegalActionException {
        Actor container = (Actor) getContainer();
        Director executiveDirector = ((Actor) container).getExecutiveDirector();
        Time fireAtTime = _getOraclePhysicalTimeForPlatformPhysicalTime(
                platformTime, realTimeClock);
        if (fireAtTime == null) {
            throw new IllegalActionException(
                    this,
                    "Failed to get the oracle time of the corresponding "
                            + "platform time. This should not happen because the "
                            + "oracle time should be in the future.");
        }
        // Add this fireAt time to the list of future fireAt times to expect.
        if (realTimeClock == platformTimeClock) {
            if(!_futurePlatformFireAtTimes.contains(fireAtTime)) {
                _futurePlatformFireAtTimes.add(fireAtTime);
                Collections.sort(_futurePlatformFireAtTimes);
            }
        } else if (realTimeClock == executionTimeClock) {
            if (!_futureExecutionFireAtTimes.contains(fireAtTime)) {
                _futureExecutionFireAtTimes.add(fireAtTime);
                Collections.sort(_futureExecutionFireAtTimes);
            }
        }
        Time temp = executiveDirector.fireAt((Actor) container, fireAtTime);
        if (temp.compareTo(fireAtTime) != 0) {
            throw new IllegalActionException(this,
                    "The fireAt wanted to occur " + "at time: "
                            + fireAtTime.toString()
                            + ", however the actual time to fireAt is at: "
                            + temp.toString());
        }
    }

    /** Return the value of the delayOffset parameter. The delayOffset parameter
     *  used in the safe to process analysis of Ptides. In Ptides, an event of
     *  timestamp tau is safe to process at physical time t if
     *  t >= tau - delayOffset.
     *  For all non-pure(trigger) events, this delayOffset is stored at
     *  each channel of each input port.
     *  @param inPort The port with which this delayOffset parameter is
     *  associated.
     *  @param channel The channel with which this delayOffset parameter is
     *  associated.
     *  @param pureEvent a boolean -- true if the event is a pure event.
     *  @return delayOffset parameter.
     *  @exception IllegalActionException If the delayOffset parameter cannot
     *  be evaluated.
     */
    protected double _getDelayOffset(IOPort inPort, int channel,
            boolean pureEvent) throws IllegalActionException {
        // If the event is a pure event, and if actors receive events in
        // timestamp order, then the delayOffset is the smallest delay
        // offset among all the ports in the same port group as the inPort.
        // For the definition of the input port group, see
        // _portsBelongToTheSameInputGroup()
        // Note the checking of the parameter
        // forceActorsToProcessEventsInTimestampOrder here is an optimization.
        // If forceActorsToProcessEventsInTimestampOrder
        // is false, we could skip this if clause.
        if (pureEvent
                && ((BooleanToken) forceActorsToProcessEventsInTimestampOrder
                        .getToken()).booleanValue()) {
            double result = Double.POSITIVE_INFINITY;
            assert (inPort != null);
            Collection<IOPort> relatedPorts = _portsBelongToTheSameInputGroup(inPort);
            for (IOPort input : relatedPorts) {
                Parameter parameter = (Parameter) ((NamedObj) input)
                        .getAttribute("delayOffset");
                if (parameter != null) {
                    for (int i = 0; i < input.getWidth(); i++) {
                        DoubleToken token = ((DoubleToken) ((ArrayToken) parameter
                                .getToken()).arrayValue()[channel]);
                        if (token != null) {
                            if (token.doubleValue() < result) {
                                result = token.doubleValue();
                            }
                        } else {
                            throw new IllegalActionException(inPort,
                                    "delayOffset parameter is needed"
                                            + "for channel, "
                                            + "but it does not exist.");
                        }
                    }
                } else {
                    throw new IllegalActionException(inPort,
                            "delayOffset parameter is needed, "
                                    + "but it does not exist.");
                }
            }
            return result;
        }

        // If event is not a pure event, or, if the event is a pure event and
        // forceActorsToProcessEventsInTimestampOrder is false, then we can
        // simply get the delayOffset from the port.
        Parameter parameter = (Parameter) ((NamedObj) inPort)
                .getAttribute("delayOffset");
        if (parameter != null) {
            DoubleToken token = ((DoubleToken) ((ArrayToken) parameter
                    .getToken()).arrayValue()[channel]);
            if (token != null) {
                return token.doubleValue();
            } else {
                throw new IllegalActionException(inPort,
                        "delayOffset parameter is needed for channel "
                                + channel + ", but it does not exist.");
            }
        } else {
            throw new IllegalActionException(inPort,
                    "delayOffset parameter is needed, "
                            + "but it does not exist.");
        }
    }

    /** Get the dependency between an input and an output ports. If the
     *  ports do not belong to the same actor, an exception is thrown.
     *  Depending on the type of the actor (atomic or composite),
     *  the corresponding getDependency() method in
     *  the actor's causality interface is called.
     *  @param input The input port.
     *  @param output The output port.
     *  @return The dependency between the specified input port
     *  and the specified output port.
     *  @exception IllegalActionException If the ports do not belong to the
     *  same actor.
     */
    protected static Dependency _getDependency(IOPort input, IOPort output)
            throws IllegalActionException {
        Actor actor = (Actor) input.getContainer();
        // If the output is null, getDependency() of the causality interface
        // would still ensure the current dependency is returned.
        if (output != null) {
            Actor outputActor = (Actor) output.getContainer();
            if (actor != outputActor) {
                throw new IllegalActionException(
                        input,
                        output,
                        "Cannot get dependency from these two ports, "
                                + "because they do not belong to the same actor.");
            }
        }
        CausalityInterface causalityInterface = actor.getCausalityInterface();
        return causalityInterface.getDependency(input, output);
    }

    /** Return the value stored in a parameter associated with
     *  the input port.
     *  Used for deviceDelay, deviceDelayBound, networkDelayBound, 
     *  platformDelay and sourcePlatformDelay. 
     *  FIXME: specialized ports do contain the parameters, don't
     *  have to get the attribute with the string! For now leave it
     *  that way to support older models that do not use PtidesPorts.
     *  @param port The port the deviceDelay is associated with.
     *  @param parameterName The name of the parameter.
     *  @return the value of the deviceDelay parameter if the parameter is not
     *  null. Otherwise return null.
     *  @exception IllegalActionException If the token of the deviceDelay
     *  parameter cannot be evaluated.
     */
    protected static Double _getDoubleParameterValue(IOPort port, String parameterName)
            throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute(parameterName);
        if (parameter != null) {
            return Double.valueOf(((DoubleToken) parameter.getToken())
                    .doubleValue());
        }
        return null;
    } 
    
    /** Return a MoML string describing the icon appearance for a Ptides
     *  director that is currently executing the specified actor.
     *  The returned MoML can include a sequence of instances of
     *  VisibleAttribute or its subclasses. In this base class, this returns a
     *  red rectangle like the usual director rectangle.
     *  @see ptolemy.vergil.kernel.attributes.VisibleAttribute
     *  @param actorExecuting The actor that's executing.
     *  @return A MoML string.
     *  @exception IllegalActionException If the animateExecution parameter
     *  cannot be evaluated.
     */
    protected String _getExecutingIcon(Actor actorExecuting)
            throws IllegalActionException {
        _highlightActor(actorExecuting, "{0.0, 0.0, 1.0, 1.0}", false);
        return "  <property name=\"rectangle\" class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">"
                + "    <property name=\"height\" value=\"30\"/>"
                + "    <property name=\"fillColor\" value=\"{0.0, 0.0, 1.0, 1.0}\"/>"
                + "  </property>";
    }

    /** Return the value of the executionTime parameter associated with
     *  the port. If the port is null, then get the value of the parameter
     *  associated with the actor.
     *  @param port The port at which execution time is annotated.
     *  @param actor actor at which execution time is annotated.
     *  @return The value of the executionTime parameter.
     *  @exception IllegalActionException If execution time from
     *  PtidesActorProperties cannot be obtained.
     */
    protected static double _getExecutionTime(IOPort port, Actor actor)
            throws IllegalActionException {
        Double result = null;
        if (port != null) {
            result = PtidesActorProperties.getExecutionTime(port);
        }
        if (result != null) {
            return result.doubleValue();
        } else {
            return PtidesActorProperties.getExecutionTime(actor);
        }
    }

    /** Return a MoML string describing the icon appearance for an idle
     *  director. This can include a sequence of instances of VisibleAttribute
     *  or its subclasses. In this base class, this returns a red rectangle like
     *  the usual director rectangle used by default.
     *  @see ptolemy.vergil.kernel.attributes.VisibleAttribute
     *  @return A MoML string.
     */
    protected static String _getIdleIcon() {
        return "  <property name=\"rectangle\" class=\"ptolemy.vergil.kernel.attributes.RectangleAttribute\">"
                + "    <property name=\"height\" value=\"30\"/>"
                + "    <property name=\"fillColor\" value=\"{0.0, 1.0, 0.0, 1.0}\"/>"
                + "  </property>";
    }

    /** Return the actor to fire in this iteration, or null if no actor
     *  should be fired.
     *  This method performs the simulation of execution time, as described
     *  below. Execution times are assumed to be in oracle simulated physical
     *  time, not platform simulated physical time. The difference between
     *  these two times are described in the comment of this class.
     *  <p>
     *  In this base class, this method first checks whether the top event from
     *  the event queue has an actuator as its destination. If it does,
     *  then we check
     *  if physical time has reached the timestamp of the actuation event. If it
     *  has, then the actuator is fired. If not, then the actuator event is
     *  taken from the event queue and put it onto the _realTimeEventQueue, and
     *  fireAt() of the executive director is called. We then check if a
     *  real-time event should be processed by looking at the top event of the
     *  _realTimeEventQueue. If there is one that should be fired, that
     *  actor is returned for firing. If not, we go on and consider two
     *  cases, depending on whether there is an actor currently executing,
     *  as follows:
     *  <p>
     *  <b>Case 1</b>: If there is no actor currently
     *  executing, then this method checks the event queue and returns
     *  null if the queue is empty. If the queue is not empty, it checks the
     *  destination actor of the earliest event on the event queue. If the
     *  destination port or actor has a non-zero
     *  execution time, then that event is pushed onto the currently executing
     *  stack and this method returns null.
     *  Otherwise, if the execution time of the actor is
     *  zero, this method sets the current model time to the time stamp of
     *  that earliest event and returns that actor.
     *  <p>
     *  <b>Case 2</b>: If there is an actor currently executing, then this
     *  method checks whether that actor has a remaining execution time of zero.
     *  If so, then the currently executing actor is returned.
     *  If not, then we check if
     *  the earliest event on the event queue should
     *  preempt it (by invoking _preemptExecutingActor()).
     *  If so, this method checks the destination actor of that event
     *  and removes the event from the event queue. If that destination
     *  actor has an execution time of zero, then this method sets the current
     *  model time to the time stamp of that event, and returns that actor.
     *  Otherwise if the destination actor has an execution time of greater than
     *  zero, then fireAt() of the executive director is called, and the
     *  physical time at which the currently
     *  executing actor should finish executing is passed into fireAt() as an
     *  input. Finally this method returns null.
     *  <p>
     *  If there is no event on the event queue or that event should not preempt
     *  the currently executing actor, fireAt()
     *  of the enclosing director is called, with the time at which the
     *  currently executing actor to finish executing is passed in as input.
     *  Finally this method returns null.
     *  <p>
     *  Also, when an actor is fired, not only is the top event processed,
     *  all events in the event queue are also checked to see whether they
     *  have the same destination actor, and whether they have the same
     *  timestamp as the top event. Events that fits the above description are
     *  also taken out of the event queue and processed.
     *  <p>
     *  Finally, in any of the following situations: a sensor interrupt
     *  has occurred, a timed interrupt has occurred, or an actor has finished
     *  firing; the scheduler must run to decide whether the next event should
     *  be processed. Since the Ptides simulator simulates the passage of
     *  physical time, we also simulate the overhead for the scheduler to make
     *  its decision.
     *  The parameter: {@link #schedulerExecutionTimeBound} indicates this time.
     *  Note, when sensor and timed interrupts occur, the currently executing
     *  event will be preempted to perform the scheduling overhead.
     *  <p>
     *  Finally, note if a sensor interrupt occurs at the
     *  same time as a previous event finishes execution, then we always assume
     *  the sensor interrupt occurred first.
     *  @see #_preemptExecutingActor()
     *  @return The next actor to be fired, which can be null.
     *  @exception IllegalActionException If event queue is not ready, or
     *  an event is missed, or time is set backwards, or if the enclosing
     *  director does not respect the fireAt call.
     */
    protected Actor _getNextActorToFire() throws IllegalActionException {
        // FIXME: This method changes persistent state, yet it is called in
        //fire(). This means that this director cannot be used inside a director
        // that does a fixed point iteration, which includes (currently),
        // Continuous and CT and SR, but in the future may also include DE.
        Tag executionPhysicalTag = getPlatformPhysicalTag(executionTimeClock);
        Actor container = (Actor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();

        // If this fireAt time should be ignored, return null, so no actor
        // is fired at this time.
        if (_ignoreThisFireAtTime()) {
            return null;
        }

        // If we have received a timed interrupt, a sensor interrupt, or if an
        // event has finished processing, the scheduler must run to figure out
        // what is the next event to process. We simulate the passage of
        // physical time for the scheduler to make these decisions.
        // If the scheduler is making a scheduling decision, that action cannot
        // be preempted, and we wait until it finishes.
        if (_schedulerStillRunning()) {
            return null;
        }
        // When a timed interrupt occurs, the previously executing
        // event should be preempted.
        // Note the next three if statements should not be merged, because
        // each indicates the occurance of an external event, and we want to
        // simulate the scheduling overhead for each case.
        if (_timedInterruptOccurred()) {
            // Indicate that no other event is processing, only the scheduler is
            // running.
            // Unlike the other cases (input event or scheduling new event),
            // where the overhead of the scheduling is captured in parameter
            // schedulerExecutionTimeBound, here, the execution time also needs
            // to include an additional execution time for the system to handle
            // the safe-to-process interrupt. This time is captured in the
            // parameter safeToProcessTimerHandlingOverhead. The value of this
            // parameter is passed into _startScheduler and simulated as a part
            // of execution time.
            Parameter parameter = (Parameter) getAttribute("safeToProcessTimerHandlingOverhead");
            double additionalExecutionTime = 0.0;
            if (parameter != null) {
                additionalExecutionTime = ((DoubleToken) parameter.getToken())
                        .doubleValue();
            }
            if (_schedulerStarts(additionalExecutionTime)) {
                _resetExecutionTimeForPreemptedEvent();
                _physicalTimeExecutionStarted = null;
                return null;
            }
            // If scheduler overhead execution time is not simulated, then we
            // simply go on executing.
        }
        if (_scheduleNewEvent) {
            _scheduleNewEvent = false;
            if (_schedulerStarts(0.0)) {
                _resetExecutionTimeForPreemptedEvent();
                _physicalTimeExecutionStarted = null;
                return null;
            }
            // If scheduler overhead execution time is not simulated, then we
            // simply go on executing.
        }

        if (!_currentlyExecutingStack.isEmpty()) {
            // If we realize the previous execution was preempted by a system
            // scheduling or sensor interruption, then restart the event
            // execution at the current simulated physical time.
            // Note: even though preemption is not supported in this director,
            // its subclasses do need such support. Ideally this code should
            // be refactored and the code that deals with preemption should be
            // put in the subclass.
            if (_physicalTimeExecutionStarted == null) {
                _physicalTimeExecutionStarted = executionPhysicalTag.timestamp;
            }
            // We are currently executing an actor.
            ExecutionTimedEvent currentEventList = (ExecutionTimedEvent) _currentlyExecutingStack
                    .peek();
            // First check whether its remaining execution time is zero.
            Time remainingExecutionTime = currentEventList.remainingExecutionTime;
            Time finishTime = _physicalTimeExecutionStarted
                    .add(remainingExecutionTime);
            int comparison = finishTime
                    .compareTo(executionPhysicalTag.timestamp);
            if (comparison < 0) {
                throw new InternalErrorException(
                        "Physical time passed the finish time of the "
                                + "currently executing actor");
            } else if (comparison == 0) {
                // Currently executing actor finishes now, so we want to return
                // it. First set current model time.
                setTag(currentEventList.timeStamp, currentEventList.microstep);
                _currentlyExecutingStack.pop();
                // If there is now something on _currentlyExecutingStack,
                // then we are resuming its execution now.
                _physicalTimeExecutionStarted = executionPhysicalTag.timestamp;

                if (_debugging) {
                    _debug("Actor "
                            + _getActorFromEventList(
                                    (List<PtidesEvent>) currentEventList.contents)
                                    .getName(getContainer())
                            + " finishes executing at physical time "
                            + executionPhysicalTag.timestamp);
                }

                // Animate, if needed.
                _setIcon(_getIdleIcon(), false);
                _clearHighlight(
                        _getActorFromEventList((List<PtidesEvent>) currentEventList.contents),
                        false);
                _sendExecutionTimeEvent(
                        _getActorFromEventList((List<PtidesEvent>) currentEventList.contents), 
                        ExecutionEventType.STOP);
                _lastExecutingActor = null;

                // Request a refiring so we can process the next event
                // on the event queue at the current execution physical time.
                executiveDirector.fireAtCurrentTime((Actor) container);

                _lastActorFired = _getActorFromEventList((List<PtidesEvent>) currentEventList.contents);

                // Processing of an event has finished. In this case, the
                // scheduler
                // should run to figure out which is the next event to process.
                // Unless the event is a sensor event, in which case the
                // scheduling overhead was accounted for when the sensor
                // interrupt occurred.
                // If the event is a pure event, however, then no scheduling
                // overhead is simulated. This is based on the assumption that
                // PtidyOS does not have a separate fireAt() for pure events,
                // and thus an actor such as TimeDelay would produce an output
                // in the same firing at which the trigger event is consumed.
                if (!((List<PtidesEvent>) currentEventList.contents).get(0)
                        .isPureEvent()) {
                    _scheduleNewEvent = true;
                }
                return _lastActorFired;
            } else { // comparison > 0
                _fireAtPlatformTime(finishTime, executionTimeClock);

                // The currently executing actor needs more execution time.
                // Decide whether to preempt it.
                if (_eventQueue.isEmpty() || !_preemptExecutingActor()) {
                    // Either the event queue is empty or the
                    // currently executing actor does not get preempted
                    // and it has remaining execution time. We should just
                    // return because we previously called fireAt() with
                    // the expected completion time, so we will be fired
                    // again at that time. There is no need to change
                    // the remaining execution time on the stack nor
                    // the _physicalTimeExecutionStarted value because
                    // those will be checked when we are refired.
                    return null;
                }
                _sendExecutionTimeEvent(
                        _getActorFromEventList((List<PtidesEvent>) _currentlyExecutingStack
                                .peek().contents), ExecutionEventType.START);
            }
        }

        // If we get here, then we want to execute the actor destination
        // of the earliest event on the event queue, either because there
        // is no currently executing actor or the currently executing actor
        // got preempted.
        if (_eventQueue.isEmpty()) {
            // Nothing to fire.
            // Animate if appropriate.
            _setIcon(_getIdleIcon(), false);

            return null;
        }

        PtidesEvent eventFromQueue = _getNextSafeEvent();
        if (eventFromQueue == null) {
            return null;
        }
        Time timeStampOfEventFromQueue = eventFromQueue.timeStamp();
        int microstepOfEventFromQueue = eventFromQueue.microstep();

        // Every time safeToProcess analysis passes, and
        // a new actor is chosen to be start processing, we update
        // _actorLastConsumedTag to store the tag of the last event that was
        // consumed by this actor. This helps us to track if safeToProcess()
        // somehow failed to produce the correct results.
        _trackLastTagConsumedByActor(eventFromQueue);

        List<PtidesEvent> eventsToProcess = _takeAllSameTagEventsFromQueue(eventFromQueue);

        Actor actorToFire = _getActorFromEventList(eventsToProcess);

        // This inPort is used to get the execution time of an event, in case
        // that event's ioPort parameter is null.
        IOPort inPort = eventFromQueue.ioPort();
        // If the firing of this event triggered another pure event, we need to
        // calculate the deadline of the pure event through the use of the last
        // timestamp, the (smallest) deadline of the last event(s), and the
        // (smallest) delta of the causality delays, which we save here.
        // FIXME: is this correct?
        _saveEventInformation(eventsToProcess);

        Time executionTime = new Time(this, _getExecutionTime(inPort,
                actorToFire));
        _lastExecutionTime = executionTime;
        if (executionTime.compareTo(_zeroTime) == 0) {
            // If execution time is zero, return the actor.
            // It will be fired now.
            setTag(timeStampOfEventFromQueue, microstepOfEventFromQueue);

            // Request a refiring so we can process the next event
            // on the event queue at the current physical time.
            executiveDirector.fireAtCurrentTime((Actor) container);

            _lastActorFired = actorToFire;

            // An event has finished processing. In this case, the scheduler
            // should run to figure out what is the next event to process.
            // Unless the event is a sensor event, in which case the scheduling
            // overhead was accounted for when the sensor interrupt occurred.
            // If the event is a pure event, however, then no scheduling
            // overhead is simulated. This is based on the assumption that
            // PtidyOS does not have a separate fireAt() for pure events,
            // and thus an actor such as TimeDelay would produce an output
            // in the same firing at which the triggere event is consumed.
            if (!eventFromQueue.isPureEvent()) {
                _scheduleNewEvent = true;
            }
            return actorToFire;
        } else {

            // Execution time is not zero. Push the executing actor onto
            // the stack, call fireAt() on the enclosing director,
            // and return null.
            Time expectedCompletionTime = executionPhysicalTag.timestamp
                    .add(executionTime);
            _fireAtPlatformTime(expectedCompletionTime, executionTimeClock);

            // If we are preempting a current execution, then
            // update information of the preempted event.
            _resetExecutionTimeForPreemptedEvent();

            if (_currentlyExecutingStack.size() > 0) {
                _sendExecutionTimeEvent(
                        _getActorFromEventList((List<PtidesEvent>) _currentlyExecutingStack
                                .peek().contents), ExecutionEventType.PREEMPTED);
            }
            _currentlyExecutingStack.push(new ExecutionTimedEvent(
                    timeStampOfEventFromQueue, microstepOfEventFromQueue,
                    eventsToProcess, executionTime));

            _physicalTimeExecutionStarted = executionPhysicalTag.timestamp;
            if (_debugging) {
                _debug("Actor " + actorToFire.toString()
                        + " starts executing at physical time "
                        + executionPhysicalTag.timestamp);
            }

            // Animate if appropriate.
            _setIcon(_getExecutingIcon(actorToFire), false);
            _lastExecutingActor = actorToFire;
            _sendExecutionTimeEvent(_lastExecutingActor, ExecutionEventType.START);
            return null;
        }
    }

    /** Return the next event to process. Notice this event returned must
     *  be safe to process. If no safe event is found, null is returned.
     *  Any overriding method must ensure this is true (by
     *  calling some version of _safeToProcess()).
     *  <p>
     *  Notice if there are multiple
     *  events in the queue that are safe to process, this function can choose
     *  to return any one of these events, it can also choose to return null
     *  depending on the implementation.
     *  <p>
     *  Also notice this method should <i>not</i> take the event from the event
     *  queue.
     *  <p>
     *  In this baseline implementation, we only check whether
     *  the event at the top of the queue is safe to process. If it is not, then
     *  we return null. Otherwise we return the top event.
     *  @return Next safe event.
     *  @exception IllegalActionException If it cannot be determined if the
     *  event is safe to process.
     */
    protected PtidesEvent _getNextSafeEvent() throws IllegalActionException {
        PtidesEvent eventFromQueue = (PtidesEvent) _eventQueue.get();
        if (_safeToProcess(eventFromQueue)) {
            return eventFromQueue;
        } else {
            return null;
        }
    }

    /** Given a platform physical time, get the corresponding oracle physical
     *  time.
     *  This assumes there's a one-to-one mapping from the platform's time to
     *  the oracle time. We also assume the platform time to be
     *  continuous. If the platform time of interest is less than the last
     *  saved platform time of the corresponding clock, throw an exception.
     *  @param platformTime The platform time.
     *  @param realTimeClock The real time clock for which we are converting
     *  oracle time to.
     *  @return The oracle tag associated with the platform tag. Returns null
     *  if the platform time of interest is less than the last
     *  saved platform time of the corresponding clock.
     *  @exception IllegalActionException If the clock ID is not recognized.
     */
    protected Time _getOraclePhysicalTimeForPlatformPhysicalTime(
            Time platformTime, RealTimeClock realTimeClock)
            throws IllegalActionException {
        HashMap<Time, Time> timeCachePair = null;
        if (realTimeClock == platformTimeClock) {
            timeCachePair = _oraclePlatformTimePair;
        } else if (realTimeClock == executionTimeClock) {
            timeCachePair = _oracleExecutionTimePair;
        }
        Time timeDifference = platformTime
                .subtract(realTimeClock._lastPlatformTime);
        if (timeDifference.compareTo(_zeroTime) < 0) {
            throw new IllegalActionException("While getting the oracle time "
                    + "for a platform time, the last platform time saved "
                    + "was in the past, which makes it impossible to get "
                    + "the platform time.");
        }
        if (realTimeClock._clockDrift == 0.0) {
            // With a clock drift of zero, we cannot determine what oracle
            // time is given the platform time, because platform time is not
            // advancing as oracle time advances.
            throw new IllegalActionException(this,
                    "Cannot get oracle time from platform time if the clock "
                            + "drift is zero");
        }
        Time result = realTimeClock._lastOracleTime.add(timeDifference
                .getDoubleValue() / realTimeClock._clockDrift);
        timeCachePair.put(result, platformTime);
        return result;
    }

    /** Get the platform time associated with the reference oracle time.
     *  If the reference oracle time is stored in the time cache pair (which
     *  may be either an oracle-platform or an oracle-execution time
     *  pair), then simply return the platform time saved in that cache.
     *  Otherwise calculate the platform time based on the last saved oracle
     *  time, the new reference oracle time, old platform time, and the clock
     *  drift. Oracle time is the model time of the enclosing DE
     *  director.
     *  @param thisOracleTime The reference oracle time.
     *  @param realTimeClock The real time clock for which we are getting the
     *  platform physical time from.
     *  @param timeCachePair A map between the oracle time and platform time.
     *  @return the platform time associated with the reference oracle time.
     *  @exception IllegalActionException If the last oracle time is greater
     *  than the reference oracle time.
     */
    protected Time _getPlatformPhysicalTimeForOraclePhysicalTime(
            Time thisOracleTime, RealTimeClock realTimeClock,
            HashMap<Time, Time> timeCachePair) throws IllegalActionException {
        Time result = timeCachePair.get(thisOracleTime);
        if (result != null) {
            return result;
        } else {
            Time timeDifference = thisOracleTime
                    .subtract(realTimeClock._lastOracleTime);
            if (timeDifference.compareTo(_zeroTime) < 0) {
                throw new IllegalActionException(
                        "While getting the platform time "
                                + "for a oracle time, the last oracle time saved "
                                + "was in the past, which makes it impossible to get "
                                + "the new oracle time.");
            }
            return realTimeClock._lastPlatformTime.add(timeDifference
                    .getDoubleValue() * realTimeClock._clockDrift);
        }
    }

    /** Highlight the specified actor with the specified color.
     *  @param actor The actor to highlight.
     *  @param color The color, given as a string description in
     *  the form "{red, green, blue, alpha}", where each of these
     *  is a number between 0.0 and 1.0.
     *  @param overwriteHighlight  a boolean -- true if the current
     *  color is to be overwritten.
     *  @exception IllegalActionException If the animateExecution
     *  parameter cannot be evaluated.

     */
    protected void _highlightActor(Actor actor, String color,
            boolean overwriteHighlight) throws IllegalActionException {
        if (((BooleanToken) animateExecution.getToken()).booleanValue()
                || overwriteHighlight) {
            String completeMoML = "<property name=\"_highlightColor\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\""
                    + color + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    (NamedObj) actor, completeMoML);
            request.setPersistent(false);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor) container).requestChange(request);
        }
    }

    /** Among all events in the event queue, find the first event that
     *  is destined to an output port of the containing composite actor.
     *  This event is taken from the event queue, and the token is sent
     *  to the actuator/network output port.
     *  @exception IllegalActionException If the director cannot set the
     *  current tag of the director.
     */
    protected void _noActorToFire() throws IllegalActionException {
        int eventIndex = 0;
        synchronized (_eventQueue) {
            while (eventIndex < _eventQueue.size()) {
                PtidesEvent nextEvent = ((PtidesListEventQueue) _eventQueue)
                        .get(eventIndex);
                if (nextEvent.ioPort() != null &&
                //(nextEvent.ioPort().getContainer() == getContainer()) &&
                        nextEvent.ioPort().isOutput()) {
                    PtidesEvent ptidesEvent = ((PtidesListEventQueue) _eventQueue)
                            .take(eventIndex);
                    setTag(ptidesEvent.timeStamp(), ptidesEvent.microstep());
                } else {
                    // This event index should point to the next event if no
                    // event is taken from the event queue.
                    eventIndex++;
                }
            }
        }
    }

    /** Return whether we want to preempt the currently executing actor
     *  and instead execute another event.
     *  This base class returns false, indicating that the currently
     *  executing actor is never preempted.
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class. Derived
     *  classes may throw it if unable to preempt the currently executing actor.
     */
    protected boolean _preemptExecutingActor() throws IllegalActionException {
        return false;
    }

    /** If the event's destination port does not have a delayOffset (calculated
     *  through {@link #_calculateDelayOffsets}), or if there doesn't exist
     *  a destination port (in case of a pure event) then the event is
     *  always safe to process. Otherwise: If the current physical time has
     *  passed the timestamp of the event minus delayOffset of the port, then
     *  the event is safe to process. Otherwise the event is not safe to
     *  process, and we calculate the physical time when the event is safe to
     *  process and setup a timed interrupt. This method calls
     *  {@link #_fireAtPlatformTime(Time, RealTimeClock)}.
     *  at the oracle time of the corresponding safe-to-process time.
     *  @see #_getOraclePhysicalTimeForPlatformPhysicalTime(Time, RealTimeClock)
     *  @param event The event checked for safe to process
     *  @return True if the event is safe to process, otherwise return false.
     *  @exception IllegalActionException If port is null and event is not a
     *   pure event.
     */
    protected boolean _safeToProcess(PtidesEvent event)
            throws IllegalActionException {
        IOPort port = event.ioPort();
        if (port == null) {
            assert (event.isPureEvent());
            // The event's port could be null only if the event is a pure event,
            // and the pure event is not causally related to any input port,
            // thus the event is always safe to process.
            return true;
        }
        // This should actually never happen, since _getNextActuationEvent()
        // should move all actuation events to the outputs.
        assert (!port.isOutput());

        double delayOffset = _getDelayOffset(port,
                ((PtidesEvent) event).channel(), event.isPureEvent());
        Time waitUntilPhysicalTime = event.timeStamp().subtract(delayOffset);
        Tag platformPhysicalTag = getPlatformPhysicalTag(platformTimeClock);
        int compare = platformPhysicalTag.timestamp.subtract(
                waitUntilPhysicalTime).compareTo(_zeroTime);
        int microstep = platformPhysicalTag.microstep;
        if ((compare > 0) || compare == 0 && (microstep >= event.microstep())) {
            return true;
        } else {
            // For each future timed interrupt, save when it is supposed to
            // occur. This is used to simulate the amount of physical time
            // it takes for a scheduler to run when an interrupt occurs.
            if (!_eventsWithTimedInterrupt.contains(event)) {
                _eventsWithTimedInterrupt.add(event);
                TimedEvent timedEvent = new TimedEvent(waitUntilPhysicalTime,
                        event);
                _timedInterruptTimes.add(timedEvent);
                Collections.sort(_timedInterruptTimes);
                _fireAtPlatformTime(waitUntilPhysicalTime, platformTimeClock);
            }
            return false;
        }
    }

    /** Return whether the two input ports are in the same input port group.
     *  @param refEvent The reference event.
     *  @param event The event to be compared to the refEvent.
     *  @return whether the two input events share the same port group.
     *  @exception IllegalActionException If cannot decide whether ports
     *  belong to the same input port group.
     */
    protected boolean _sameInputPortGroup(PtidesEvent refEvent,
            PtidesEvent event) throws IllegalActionException {
        if (refEvent.ioPort() == null || event.ioPort() == null) {
            return false;
        }
        Collection<IOPort> inputPortGroup = _portsBelongToTheSameInputGroup(refEvent
                .ioPort());
        if (inputPortGroup.contains(event.ioPort())) {
            return true;
        }
        return false;
    }

    /** Save the information of the events ready to be processed. This includes
     *  the source port of the last firing event, the timestamp,
     *  and the minimum model time delay of the last executing event. This
     *  information
     *  is used to calculate the absolute deadline of the produced pure event.
     *  @param eventsToProcess The list of events to be processed.
     *  @exception IllegalActionException If absolute deadline of an event,
     *  finite dependent ports of a port, or dependency between two ports
     *  cannot be evaluated.
     */
    protected void _saveEventInformation(List<PtidesEvent> eventsToProcess)
            throws IllegalActionException {

        // The firing of this event is from this port. If the firing resulted in
        // the production of a pure event, then that pure event is causally
        // related to all events coming from
        // this input port (and those in its input port group).
        Actor actorToFire = eventsToProcess.get(0).actor();

        // If the firing of this event triggered another pure event, we need to
        // save the destination port of the previous event. That port is used
        // to determine the causality port for the pure event.
        _pureEventSourcePorts.put(actorToFire, eventsToProcess.get(0).ioPort());
    }

    /** Send an execution time event to all listeners.
     *  @param actor Actor that produced an execution time event.
     *  @param event Type of the event.
     *  @exception IllegalActionException If the physical time cannot be determined.
     */
    protected void _sendExecutionTimeEvent(Actor actor,
            ExecutionEventType event) throws IllegalActionException {
        double oracleTime = _getOraclePhysicalTag().timestamp.getDoubleValue(); 
        double physicalTime = getPlatformPhysicalTag(platformTimeClock).timestamp
        .getDoubleValue(); 
        double modelTime = getModelTime().getDoubleValue();
        if (_executionTimeListeners != null) {
            for (ExecutionTimeListener listener : _executionTimeListeners) {
                listener.event(actor, oracleTime, physicalTime, modelTime, event);
            }
        }
    }

    /** Set the icon for this director if the <i>animateExecution</i>
     *  parameter is set to true.
     *  @param moml A MoML string describing the contents of the icon.
     *  @param clearFirst If true, remove the previous icon before creating a
     *   new one.
     *  @exception IllegalActionException If the <i>animateExecution</i>
     *   parameter cannot be evaluated.
     */
    protected void _setIcon(String moml, boolean clearFirst)
            throws IllegalActionException {
        if (((BooleanToken) animateExecution.getToken()).booleanValue()) {
            String completeMoML = "<property name=\"_icon\" class=\"ptolemy.vergil.icon.EditorIcon\">"
                    + moml + "</property>";
            if (clearFirst && getAttribute("_icon") != null) {
                // If we are running under MoMLSimpleApplication, then the
                //_icon might not be present, so check before trying to
                // delete it.
                completeMoML = "<group><!-- PtidesBasicDirector --><deleteProperty name=\"_icon\"/>"
                        + completeMoML + "</group>";
            }
            MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                    completeMoML);
            request.setPersistent(false);
            Actor container = (Actor) getContainer();
            ((TypedCompositeActor) container).requestChange(request);
        }
    }

    /** Return all the events in the event queue that are of the same tag as the
     *  event passed in, AND have destination ports that are in the same input
     *  port group.
     *  These events should be removed from the event queue in the process.
     *  @param event The reference event.
     *  @return List of events of the same tag.
     *  @exception IllegalActionException If failed to get
     *   throws it.
     */
    protected List<PtidesEvent> _takeAllSameTagEventsFromQueue(PtidesEvent event)
            throws IllegalActionException {
        List<PtidesEvent> eventList = new ArrayList<PtidesEvent>();
        int eventIndex = 0;
        while (eventIndex < _eventQueue.size()) {
            PtidesEvent nextEvent = ((PtidesListEventQueue) _eventQueue)
                    .get(eventIndex);
            if (event == nextEvent) {
                eventList.add(((PtidesListEventQueue) _eventQueue)
                        .take(eventIndex));
            } else {
                if (nextEvent.hasTheSameTagAs(event)) {
                    if (_sameInputPortGroup(event, nextEvent)) {
                        eventList.add(((PtidesListEventQueue) _eventQueue)
                                .take(eventIndex));
                    } else {
                        eventIndex++;
                    }
                } else {
                    break;
                }
            }
        }
        return eventList;
    }

    /** Keep track of the last event an actor decides to process. This method
     *  is called immediately after _safeToProcess, thus it serves as a check to
     *  see if the processing of this event has violated DE semantics. If it
     *  has, then an exception is thrown, if it has not, then the current tag is
     *  saved for checks to see if future events are processed in timestamp
     *  order.
     *  @param event The event that has just been determined to be safe to
     *  process.
     *
     *  FIXME: this implementation is actually too conservative, in that it may
     *  result in false negatives. This method assumes each actor should process
     *  events in timestamp order, but in Ptides we only need each input port
     *  group to consume events in timestamp order. Thus this method is correct
     *  if the actor's input ports all reside within the same input port group.
     *  @exception IllegalActionException If a previous event processing has
     *   violated DE semantics.
     */
    protected void _trackLastTagConsumedByActor(PtidesEvent event)
            throws IllegalActionException {
        NamedObj obj = event.ioPort();
        if (event.isPureEvent()) {
            return;
        }

        // FIXME: This is a hack. We have this because the network interface may
        // receive many events at the same physical time, but then they will
        // decode the incoming token to produce events of (hopefully) different
        // timestamps. Thus here we do not need to check if safe to process was
        // correct if the actor is a NetworkReceiver.
        if (obj.getContainer() instanceof NetworkReceiver) {
            return;
        }

        Tag tag = new Tag(event.timeStamp(), event.microstep());
        Tag prevTag = _lastConsumedTag.get(obj);
        if (prevTag != null) {
            if (tag.compareTo(prevTag) <= 0) {
                if (obj instanceof Actor) {
                    throw new IllegalActionException(
                            obj,
                            "Event processed out of timestamp order. "
                                    + "The tag of the previous processed event is: "
                                    + "timestamp = prevTag.timestamp, microstep = "
                                    + prevTag.microstep
                                    + ". The tag of the current "
                                    + "event is: timestamp = " + tag.timestamp
                                    + ", microstep = " + tag.microstep + ". ");
                } else {
                    // If the event is destined to an actuation port, it doesn't
                    // have to be delivered to the port in timestamp order.
                    // Safe to process analysis is only defined for events
                    // destined for actors within the director, but not for
                    // events destined to the outside of this director.
                    if (obj.getContainer() != getContainer()) {
                        throw new IllegalActionException(
                                obj.getContainer(),
                                obj,
                                "Event processed out of timestamp order. "
                                        + "The tag of the previous processed event is: "
                                        + "timestamp = " + prevTag.timestamp
                                        + ", microstep = " + prevTag.microstep
                                        + ". The tag of the current event is: "
                                        + "timestamp = " + tag.timestamp
                                        + ", microstep = " + tag.microstep
                                        + ". ");
                    }
                }
            }
        }
        // The check was correct, now we replace the previous tag with the
        // current tag.
        _lastConsumedTag.put(obj, tag);
    }

    /** Transfer input events from outside of the Ptides platform to the inside.
     *  These events are assumed to be either sensor events or network events.
     *  For all events in the sensorEventQueue, transfer input events that are
     *  ready. For all events that are currently sitting at the port as
     *  indicated by the "port" parameter, if
     *  the deviceDelay is 0.0, then transfer them into the platform,
     *  otherwise move them into the sensorEventQueue and call fireAt() of the
     *  executive director. In either case, if the input port is a networkPort,
     *  we make sure the timestamp of the data token transmitted is set to the
     *  timestamp of the local event associated with this token.
     *  <p> As described in the comment for this class, there are two versions
     *  of simulated physical time: oracle simulated physical time, and
     *  platform simulated physical time. Timestamping of input events uses
     *  the platform simulated physical time, while the execution time delay
     *  (d_o) for sensors are modeled using the platform simulated physical
     *  time. </p>
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port, if the super class throws it, if physical tag cannot be
     *  evaluated, if token cannot be sent to the inside, or if there exists no
     *  token in the port, but hasToken() return true.
     */
    protected boolean _transferInputs(IOPort port)
            throws IllegalActionException {

        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque "
                            + "input port.");
        }

        boolean result = false;
        Tag platformPhysicalTag = getPlatformPhysicalTag(platformTimeClock);
        Tag executionPhysicalTag = getPlatformPhysicalTag(executionTimeClock);
        // First transfer all tokens that are already in the event queue for the
        // sensor.
        // Notice this is done NOT for the specific port
        // in question. Instead, we do it for ALL events that can be transferred
        // out of this platform.
        // FIXME: there is _NO_ guarantee from the priorityQueue that these
        // events are sent out in the order they arrive at the sensor. We can
        // only be sure that they are sent in the order of the timestamps, but
        // for two events of the same timestamp at a sensor, there's no
        // guarantee on the order of events sent to the inside.
        while (true) {
            if (_realTimeInputEventQueue.isEmpty()) {
                break;
            }

            RealTimeEvent realTimeEvent = (RealTimeEvent) _realTimeInputEventQueue
                    .peek();
            int compare = realTimeEvent.deliveryTag
                    .compareTo(executionPhysicalTag);

            if (compare > 0) {
                break;
            } else if (compare == 0) {
                Time lastModelTime = getModelTime();
                int lastMicrostep = _microstep;
                _realTimeInputEventQueue.poll();
//                if (_isNetworkInputPort(realTimeEvent.port)) {
//                    // If transferring a network input, make it always safe to
//                    // process.
//                    setTag(new Time(this, Double.NEGATIVE_INFINITY), 1);
//                } else {
                    setTag(realTimeEvent.timestampTag.timestamp,
                            realTimeEvent.timestampTag.microstep);
//                }
                realTimeEvent.port.sendInside(realTimeEvent.channel,
                        realTimeEvent.token);
                setTag(lastModelTime, lastMicrostep);
                if (_debugging) {
                    _debug(getName(), "transferring input from "
                            + realTimeEvent.port.getName());
                }
                result = true;
            } else {
                throw new IllegalActionException(realTimeEvent.port,
                        "missed transferring at the sensor. "
                                + "Should transfer input at oracle physical"
                                + "time = "
                                + realTimeEvent.deliveryTag.timestamp + "."
                                + realTimeEvent.deliveryTag.microstep
                                + ", and current oracle physical time = "
                                + executionPhysicalTag.timestamp + "."
                                + executionPhysicalTag.microstep);
            }
        }

        Double inputDelay = _getDoubleParameterValue(port, "deviceDelay"); 
        if (inputDelay == null) {
            inputDelay = 0.0;
        }
        if (inputDelay == 0.0) {
            Time lastModelTime = getModelTime();
            int lastMicrostep = _microstep;
            // If transferring a network input, make it always safe to process.
            if (_isNetworkInputPort(port)) {
                setTag(new Time(this, Double.NEGATIVE_INFINITY), 1);
            } else {
                // By default we assume the input is a sensor input, and
                // a sensor event would have timestamp equal to the platform's
                // physical time.
                setTag(platformPhysicalTag.timestamp,
                        platformPhysicalTag.microstep); 
            }
            result |= super._transferInputs(port);
            setTag(lastModelTime, lastMicrostep);
        } else {
            for (int i = 0; i < port.getWidth(); i++) {
                if (i < port.getWidthInside()) {
                    if (port.hasToken(i)) {
                        Token t = null;
                        try {
                            t = port.get(i);
                        } catch (NoTokenException ex) {
                            // This shouldn't happen.
                            throw new IllegalActionException(this, ex, null);
                        }
                        Time waitUntilTime = executionPhysicalTag.timestamp
                                .add(inputDelay);
                        // For the realTimeEvent, the delivery time to
                        // the platform is based on oraclePhysicalTag,
                        // while the timestamp of this event is based
                        // on platformPhysicalTime.
                        RealTimeEvent realTimeEvent = new RealTimeEvent(port,
                                i, t, new Tag(waitUntilTime, 1),
                                //executionPhysicalTag.microstep),
                                new Tag(platformPhysicalTag.timestamp,
                                // Even though it may seem like the sensor should
                                // always produce an output event with microstep 0,
                                // If two events are received at the same timestamp
                                // but different microsteps, we would get an
                                // exception because the downstream actor would have
                                // received two events with the same timestamp and
                                // microstep consecutively. This is not allowed by
                                // DE semantics. 
                                        platformPhysicalTag.microstep));
                        _realTimeInputEventQueue.add(realTimeEvent);
                        result = true;

                        // Wait until oracle physical time to transfer
                        // the token into the platform
                        // FIXME: this looks weird, should be deviceDelay
                        // be the # of clock cycles? What does it mean
                        // that the deviceDelay is in a notion of time?
                        // What does this time mean?
                        _fireAtPlatformTime(waitUntilTime, executionTimeClock);
                    }
                }
            }
        }
        return result;
    }

    /** Transfer output events from inside of the Ptides platform to the
     *  outside. These events are either actuator events or network events.
     *  These events are assumed to be either sensor events or network events.
     *  First, for tokens that are stored in the actuator event queue,
     *  send them to the outside of the platform if physical time has arrived.
     *  Second, compare current model time with simulated physical time.
     *  If physical time is smaller than current model time, then deadline
     *  has been missed. Throw an exception unless the port is annotated
     *  with ignoreDeadline. If the deadline has been missed and ignoreDeadline
     *  is true, or if the current model time is equal to the physical time,
     *  or if the port is annotated with transferImmediately, we send
     *  the tokens to the outside. If current model time has not arrived
     *  at the physical time, we put the token along with the destination
     *  port and channel into the actuator event queue, and call fireAt of
     *  the executive director so we could send it at a later physical time.
     *  <p> As described in the comment for this class, there are two versions
     *  of simulated physical time: oracle simulated physical time, and
     *  platform simulated physical time. The time at which an actuation event
     *  is sent to the output port uses
     *  the platform simulated physical time. </p>
     *  @param port The port to transfer tokens to.
     *  @return True if at least one data token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port, if the super class throws it, if physical tag cannot be
     *   evaluated, or if the token cannot be sent to the inside.
     */
    protected boolean _transferOutputs(IOPort port)
            throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque output port.");
        }

        // First check for current time, and transfer any tokens that are
        // already ready to output.
        boolean result = false;
        Tag platformPhysicalTag = getPlatformPhysicalTag(platformTimeClock);
        int compare = 0;
        // The following code does not transfer output tokens specifically for
        // "port", the input of this method. Instead, for all events in
        // _realTimeOutputEventQueue, those that has timestamp equal to the
        // current physical time are transferred to the outside of this
        // platform.
        // FIXME: there is _NO_ guarantee from the priorityQueue that these
        // events are sent out in the order they arrive at the actuator. We can
        // only be sure that they are sent in the order of the tags, but
        // for two events of the same tag at an actuator, there's no
        // guarantee on the order of events sent to the outside.
        while (true) {
            if (_realTimeOutputEventQueue.isEmpty()) {
                break;
            }
            RealTimeEvent tokenEvent = (RealTimeEvent) _realTimeOutputEventQueue
                    .peek();
            compare = tokenEvent.deliveryTag.compareTo(platformPhysicalTag);

            if (compare > 0) {
                // If the physical time has reached the deliveryTag's timestamp,
                // however microstep has not reached the delivery tag, we call
                // fireAt at the current physical time so microstep can advance.
                if (tokenEvent.deliveryTag.timestamp
                        .equals(platformPhysicalTag.timestamp)) {
                    (((Actor) getContainer()).getExecutiveDirector())
                            .fireAtCurrentTime((Actor) getContainer());
                }
                break;
            } else if (compare == 0) {
                _realTimeOutputEventQueue.poll();
                tokenEvent.port.send(tokenEvent.channel, tokenEvent.token);
                if (_debugging) {
                    _debug(getName(), "transferring output " + tokenEvent.token
                            + " from " + tokenEvent.port.getName());
                }
                result = true;
            } else if (compare < 0 && !_isNetworkOutputPort(port)) {
                throw new IllegalActionException(tokenEvent.port,
                        "missed deadline at the actuator. Deadline = "
                                + tokenEvent.deliveryTag.timestamp + "."
                                + tokenEvent.deliveryTag.microstep
                                + ", and current platform physical time = "
                                + platformPhysicalTag.timestamp
                                + ", and microstep = "
                                + platformPhysicalTag.microstep);
            }
        }

        
        // Deadline of an actuation event is the timestamp subtracted by the
        // deviceDelay (d_a) at the actuators.
        Double actuatorDeviceDelay = _getDoubleParameterValue(port, "deviceDelay");
        Time deadline = getModelTime();
        if (actuatorDeviceDelay != null) {
            deadline = deadline.subtract(actuatorDeviceDelay);
        }
        compare = deadline.compareTo(platformPhysicalTag.timestamp);

        // FIXME: since ports that are annotated with ignoreDeadline
        // are not checked for deadline violations, do they still count
        // as actuation ports? i.e., when calculating deadline, should we
        // start at ports that are annotated with transferImmediately?
        if (port instanceof NetworkTransmitterPort) {
            super._transferOutputs(port);
        } else if (compare < 0 && !_ignoreDeadline(port)) {
            for (int i = 0; i < port.getWidthInside(); i++) {
                if (port.hasTokenInside(i)) {
                    throw new IllegalActionException(port,
                            "missed deadline at the actuator. The deadline is "
                                    + deadline
                                    + ", and the platform physical time is "
                                    + platformPhysicalTag.timestamp);
                }
            }
        } else if (compare == 0 || _transferImmediately(port)
                || (compare < 0 && _ignoreDeadline(port))) {
            // If physical time has reached the timestamp of the last event,
            // or if _transferImmediately is true,
            // transmit data to the output now. Notice this does not guarantee
            // tokens are transmitted, simply because there might
            // not be any tokens to transmit.
            // If we transferred once to the network output, then return true,
            // and go through this once again.
            while (true) {
                if (!super._transferOutputs(port)) {
                    break;
                } else {
                    result = true;
                }
            }
        } else {
            // make sure this is the only case left.
            assert (compare > 0);
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        // A real time actuation event does not need to save the
                        // timestamp.
                        RealTimeEvent tokenEvent = new RealTimeEvent(port, i,
                                t, new Tag(getModelTime(), _microstep), null);
                        _realTimeOutputEventQueue.add(tokenEvent);
                        // Wait until platform physical time is equal to the
                        // timestamp of the event to transfer the output to the
                        // actuator. Notice even though the deadline of the
                        // event is timestamp - d_a, the time of actuation
                        // occurs at physical time equal to the timestamp.
                        _fireAtPlatformTime(getModelTime(), platformTimeClock);
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate the absolute deadline for the pure event. This class
     *  does not use deadline to perform scheduling, thus this method
     *  is not used. Instead it is a placeholder for subclasses to override.
     *  @param actor The destination actor of the pure event.
     *  @return null.
     */
    protected Time _absoluteDeadlineForPureEvent(Actor actor) {
        return null;
    }

    /** For a particular input port channel pair, calculate the delay offset.
     *  @param inputPort The input port to find delay offset.
     *  @param channel The channel at this input port.
     *  @return The delay offset associated with this port channel pair.
     *  @exception IllegalActionException If finite dependent ports cannot
     *  be evaluated, or token of actorsReceiveEventsInTimestampOrder
     *  parameter cannot be evaluated.
     */
    private double _calculateDelayOffsetsForPortChannel(IOPort inputPort,
            Integer channel) throws IllegalActionException {
        SuperdenseDependency smallestDependency = SuperdenseDependency.OPLUS_IDENTITY;
        for (IOPort port : (Collection<IOPort>) _portsBelongToTheSameInputGroup(inputPort)) {
            Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) _inputModelTimeDelays
                    .get(port);
            if (channelDependency != null) {
                for (Integer integer : channelDependency.keySet()) {
                    if (((BooleanToken) forceActorsToProcessEventsInTimestampOrder
                            .getToken()).booleanValue()) {
                        if (!(port == inputPort && integer.equals(channel))) {
                            SuperdenseDependency candidate = channelDependency
                                    .get(integer);
                            if (smallestDependency.compareTo(candidate) > 0) {
                                smallestDependency = candidate;
                            }
                        }
                    } else {
                        // Cannot assume events arrive in timestamp order.
                        SuperdenseDependency candidate = channelDependency
                                .get(integer);
                        if (smallestDependency.compareTo(candidate) > 0) {
                            smallestDependency = candidate;
                        }

                    }
                }
            }
        }
        return smallestDependency.timeValue();
    }
    
    /** Check that actual deviceDelay is not bigger than the bound.
     *  @param port The Port to be checked.
     *  @exception IllegalActionException If the parameters of the port cannot be read.
     */
    private void _checkDeviceDelay(TypedIOPort port) throws IllegalActionException {
        Double deviceDelay = _getDoubleParameterValue(port, "deviceDelay");
        Double deviceDelayBound = _getDoubleParameterValue(port, "deviceDelayBound");
        if (deviceDelay != null && deviceDelayBound != null &&
                deviceDelay > deviceDelayBound) {
            throw new IllegalActionException(port, 
                    "The deviceDelayBound must be >= deviceDelay.");
        }
    }

    /** Return the actor associated with the events in the list. All events
     *  within the list should share the same destination actor.
     *  @param currentEventList A list of events.
     *  @return Actor associated with events in the list.
     */
    private Actor _getActorFromEventList(List<PtidesEvent> currentEventList) {
        return currentEventList.get(0).actor();
    }

    /** This function is called when an pure event is produced. Given an actor,
     *  we need to return whether the pure event is causally related to a set
     *  of input ports. If it does, return one input port from that input port
     *  group; Otherwise, return null. If causality marker does not exist, or
     *  if the parameter forceActorsToProcessEventsInTimestampOrder is set,
     *  then we take the conservative approach, and say all inputs causally
     *  affect the pure event.
     *  @param actor The destination actor.
     *  @return the port that causally relates to the pure event. If there is
     *  no such port, return null.
     *  @exception IllegalActionException If the causality marker contains
     *  source port cannot be evaluated.
     */
    private IOPort _getCausalPortForThisPureEvent(Actor actor)
            throws IllegalActionException {
        CausalityMarker causalityMarker = (CausalityMarker) ((NamedObj) actor)
                .getAttribute("causalityMarker");
        // Get the last source port. However, we do not remove the last
        // source port from the hashmap, because more than one pure events can
        // be produced during one firing. In which case, the last source port
        // can be used again.
        IOPort lastSourcePort = (IOPort) _pureEventSourcePorts.get(actor);
        // If causality marker does not exist, or if the parameter
        // forceActorsToProcessEventsInTimestampOrder is set,
        // we take the conservative approach,
        // and say all inputs causally affect the pure event.
        if (causalityMarker == null
                || causalityMarker.containsPort(lastSourcePort)
                || ((BooleanToken) forceActorsToProcessEventsInTimestampOrder
                        .getToken()).booleanValue()) {
            return lastSourcePort;
        }
        return null;
    }

    /** Return the model time of the enclosing director, which is our model
     *  of time in the physical environment. Note this oracle physical time
     *  is different from the platform physical time, since platform time can
     *  drift away from the oracle time.
     *  @return the model time of the enclosing director, which is a model of
     *  time in the physical environment.
     */
    private Tag _getOraclePhysicalTag() {
        Tag tag = new Tag();
        Director executiveDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        tag.timestamp = executiveDirector.getModelTime();
        tag.microstep = ((DEDirector) executiveDirector).getMicrostep();
        return tag;
    }

    /** For all deeply contained actors, if annotateModelDelay is true, and if
     *  the actor has a dependency that is not equal to the OTimesIdentity or
     *  OPlusIdentity, then this actor is
     *  annotated with a certain color. This process is repeated recursively.
     *  If annotateModelDelay is false, then instead of highlighting actors,
     *  the highlighting is cleared.
     *  @param compositeActor actor to highlight model delays.
     *  @param highlightModelDelay Whether to highlight model delay.
     *  @exception IllegalActionException If causality interface cannot
     *  be evaluated, dependency cannot be evaluated, or finite dependent
     *  ports cannot be evaluated.
     */
    private void _highlightModelDelays(CompositeActor compositeActor,
            boolean highlightModelDelay) throws IllegalActionException {

        for (Actor actor : (List<Actor>) (compositeActor.deepEntityList())) {
            boolean annotateThisActor = false;
            CausalityInterface causalityInterface = actor
                    .getCausalityInterface();
            for (IOPort input : (List<IOPort>) actor.inputPortList()) {
                for (IOPort output : (Collection<IOPort>) _finiteDependentPorts(input)) {
                    Dependency dependency = causalityInterface.getDependency(
                            input, output);
                    // Annotate the actor if dependency is neither oPlus or
                    // oTimes identity. Notice this means if the actor is
                    // governed by a DEDirector, which uses BooleanDependency,
                    // then the actors will never be annotated.
                    if (!dependency.equals(dependency.oTimesIdentity())
                            && !dependency.equals(dependency.oPlusIdentity())) {
                        annotateThisActor = true;
                        break;
                    }
                }
                if (annotateThisActor) {
                    break;
                } else {
                    _clearHighlight(actor, true);
                }
            }
            if (annotateThisActor && highlightModelDelay) {
                _highlightActor(actor, "{0.0, 1.0, 1.0, 1.0}", true);
            }
            if (actor instanceof CompositeActor) {
                _highlightModelDelays((CompositeActor) actor,
                        highlightModelDelay);
            }
        }
    }

    /** Check if we should output to the enclosing director immediately.
     *  This method returns false by default.
     *  @param port Output port to transmit output event immediately.
     *  @return true If the output evnet should be transferred to the outside
     *  regardless of the deadline of the event.
     *  @exception IllegalActionException If token of the ignoreDeadline
     *  parameter cannot be evaluated.
     */
    private static boolean _ignoreDeadline(IOPort port)
            throws IllegalActionException {
        // FIXME: if the port is both an input or output port, what should we
        // do?
        if (port.isInput()) {
            IllegalActionException up = new IllegalActionException(
                    port,
                    "Trying to get the ignore deadline parameter of an "
                            + "input port. However, this ignoreDeadline parameter "
                            + "should only be annotated on an output port.");
            throw up;
        }
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("ignoreDeadline");
        if (parameter != null) {
            return ((BooleanToken) parameter.getToken()).booleanValue();
        }
        return false;
    }

    /** Return true if the current firing should be skipped.
     *  If the execution and platform ignore fireAt times contains the current
     *  oracle time, and if the execution and platform fireAt times does not
     *  contain the current oracle time, return true. Otherwise return false.
     *  Also, from each of the four lists, remove the current oracle time from
     *  that list, if it exists.
     *  @return true if the ignore lists contain the current oracle time, while
     *  the fireAt lists do not.
     *  @exception IllegalActionException If the director cannot get
     *   the current physical tag, or if the fireAt time to be ignored is
     *   in the past.
     */
    private boolean _ignoreThisFireAtTime() throws IllegalActionException {
        Tag oraclePhysicalTag = _getOraclePhysicalTag();
        Time executionFireAtTime = null;
        if (!_futureExecutionFireAtTimes.isEmpty()) {
            executionFireAtTime = _futureExecutionFireAtTimes.get(0);
        }
        Time platformFireAtTime = null;
        if (!_futurePlatformFireAtTimes.isEmpty()) {
            platformFireAtTime = _futurePlatformFireAtTimes.get(0);
        }
        Time executionIgnoreTime = null;
        if (!_ignoredExecutionFireAtTimes.isEmpty()) {
            executionIgnoreTime = _ignoredExecutionFireAtTimes.get(0);
        }
        Time platformIgnoreTime = null;
        if (!_ignoredPlatformFireAtTimes.isEmpty()) {
            platformIgnoreTime = _ignoredPlatformFireAtTimes.get(0);
        }
        int compareExecutionFireAtTime = -1;
        if (executionFireAtTime != null) {
            compareExecutionFireAtTime = oraclePhysicalTag.timestamp
                    .compareTo(executionFireAtTime);
            if (compareExecutionFireAtTime > 0) {
                throw new IllegalActionException(this, "A fireAt time "
                        + "that was to happen at: "
                        + executionFireAtTime.toString() + " is "
                        + "still in the list of fireAt times, while the "
                        + "current oracle time is: "
                        + oraclePhysicalTag.toString() + ".");
            } else if (compareExecutionFireAtTime == 0) {
                _futureExecutionFireAtTimes.remove(0);
            }
        }
        int comparePlatformFireAtTime = -1;
        if (platformFireAtTime != null) {
            comparePlatformFireAtTime = oraclePhysicalTag.timestamp
                    .compareTo(platformFireAtTime);
            if (comparePlatformFireAtTime > 0) {
                throw new IllegalActionException(this, "A fireAt time "
                        + "that was to happen at: "
                        + platformFireAtTime.toString() + " is "
                        + "still in the list of fireAt times, while the "
                        + "current oracle time is: "
                        + oraclePhysicalTag.toString() + ".");
            } else if (comparePlatformFireAtTime == 0) {
                _futurePlatformFireAtTimes.remove(0);
            }
        }
        int compareExecutionIgnoreTime = -1;
        if (executionIgnoreTime != null) {
            oraclePhysicalTag.timestamp.compareTo(executionIgnoreTime);
            if (compareExecutionIgnoreTime > 0) {
                throw new IllegalActionException(
                        this,
                        "A fireAt time to ignore "
                                + "happened in the past. The current oracle "
                                + "time is "
                                + oraclePhysicalTag.toString()
                                + ", and the fireAt "
                                + "time to be ignored is due to execution clock"
                                + executionIgnoreTime.toString() + ".");
            } else if (compareExecutionIgnoreTime == 0) {
                if (_debugging) {
                    _debug("The current oracle time is "
                            + oraclePhysicalTag.timestamp + "."
                            + oraclePhysicalTag.microstep + ", and we "
                            + "have skipped the current firing because "
                            + "there has been a change in clock drift in "
                            + "one of the system clocks.");
                }
                _ignoredExecutionFireAtTimes.remove(0);
            }
        }
        int comparePlatformIgnoreTime = -1;
        if (platformIgnoreTime != null) {
            oraclePhysicalTag.timestamp.compareTo(platformIgnoreTime);
            if (comparePlatformIgnoreTime > 0) {
                throw new IllegalActionException(
                        this,
                        "A fireAt time to ignore "
                                + "happened in the past. The current oracle time is "
                                + oraclePhysicalTag.toString()
                                + ", and the fireAt "
                                + "time to be ignored is due to platform clock "
                                + platformIgnoreTime.toString() + ".");
            } else if (comparePlatformIgnoreTime == 0) {
                if (_debugging) {
                    _debug("The current oracle time is "
                            + oraclePhysicalTag.timestamp + "."
                            + oraclePhysicalTag.microstep + ", and we "
                            + "have skipped the current firing because "
                            + "there has been a change in clock drift in "
                            + "one of the system clocks.");
                }
                _ignoredPlatformFireAtTimes.remove(0);
            }
        }
        if ((compareExecutionIgnoreTime == 0 || comparePlatformIgnoreTime == 0)
                && compareExecutionFireAtTime != 0
                && comparePlatformFireAtTime != 0) {
            return true;
        }
        return false;
    }

    /** Return whether the port is a networkPort. This method only checks
     *  for whether input ports are network ports. If port is an output
     *  port, then throw an exception.
     *  @exception IllegalActionException If port is an output port.
     */
    private boolean _isNetworkInputPort(IOPort port)
            throws IllegalActionException {
        if (port.isInput()) {
            return _networkInputPorts.contains(port);
        }
        IllegalActionException up = new IllegalActionException(
                port,
                "The port is an output port, we do not distinguish "
                        + "whether output ports are network ports or not, it "
                        + "only matters whether they support transferImmediately "
                        + "or ignoreDeadline");
        throw up;
    }
    
    /** Return whether the port is a networkPort. This method only checks
     *  for whether input ports are network ports. If port is an output
     *  port, then throw an exception.
     *  @exception IllegalActionException If port is an output port.
     */
    private boolean _isNetworkOutputPort(IOPort port)
            throws IllegalActionException {
        if (port.isOutput()) {
            return _networkOutputPorts.contains(port);
        }
        IllegalActionException up = new IllegalActionException(
                port,
                "The port is an output port, we do not distinguish "
                        + "whether output ports are network ports or not, it "
                        + "only matters whether they support transferImmediately "
                        + "or ignoreDeadline");
        throw up;
    }

    /** The previously executing event has been preempted by the scheduler. The
     *  remaining execution time of the
     *  previously executing event is updated.
     *  @exception IllegalActionException If the director failed to get physical
     *  time, or if the remaining execution is less than 0 for the preempted
     *  event.
     */
    private void _resetExecutionTimeForPreemptedEvent()
            throws IllegalActionException {
        // If we are preempting a current execution, then
        // update information of the preempted event.
        if (!_currentlyExecutingStack.isEmpty()) {
            // We are preempting a current execution.
            ExecutionTimedEvent currentEventList = _currentlyExecutingStack
                    .peek();
            Time elapsedTime = getPlatformPhysicalTag(executionTimeClock).timestamp
                    .subtract(_physicalTimeExecutionStarted);
            currentEventList.remainingExecutionTime = currentEventList.remainingExecutionTime
                    .subtract(elapsedTime);
            if (currentEventList.remainingExecutionTime.compareTo(_zeroTime) < 0) {
                // This should not occur.
                throw new IllegalActionException(
                        this,
                        _getActorFromEventList((List<PtidesEvent>) currentEventList.contents),
                        "Remaining execution time is negative!");
            }
            if (_debugging) {
                _debug("Preempting actor "
                        + _getActorFromEventList(
                                (List<PtidesEvent>) currentEventList.contents)
                                .getName((NamedObj) getContainer())
                        + " at physical time "
                        + getPlatformPhysicalTag(executionTimeClock).timestamp
                        + ", which has remaining execution time "
                        + currentEventList.remainingExecutionTime);
            }
        }
    }

    /** Check if the scheduler has finished running.
     *  @return true if the scheduler is still running.
     *  @exception IllegalActionException If unable to get the physical tag.
     */
    private boolean _schedulerStillRunning() throws IllegalActionException {
        // If the overhead time has not finished, return true.
        if (_schedulerFinishTime
                .compareTo(getPlatformPhysicalTag(executionTimeClock).timestamp) <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /** If schedulerExecutionTime exists and is non-zero, indicate the
     *  director is currently running scheduler by updating private
     *  variable {@link #_schedulerFinishTime}. When this occurs, the
     *  system cannot be preempted. Then set the enclosing director to fire
     *  this actor at the time when the scheduler finishes execution and
     *  return true. If schedulerExecutionTime doesn't exist, or is zero,
     *  then return false.
     *  @param additionalExecutionTime The additional execution time that needs
     *  to be added to the schedulerExecutionTime.
     *  @return true If simulation of scheduler execution started, else
     *  return false.
     *  @exception IllegalActionException If the director fails to get physical
     *  time or failed to get a token from the schedulerExecutionTime parameter.
     */
    private boolean _schedulerStarts(double additionalExecutionTime)
            throws IllegalActionException {
        _schedulerFinishTime = getPlatformPhysicalTag(executionTimeClock).timestamp;
        Parameter parameter = (Parameter) getAttribute("schedulerExecutionTime");
        if ((parameter == null || ((DoubleToken) parameter.getToken())
                .doubleValue() == 0.0) && additionalExecutionTime == 0.0) {
            return false;
        }
        _schedulerFinishTime = getPlatformPhysicalTag(executionTimeClock).timestamp
                .add(((DoubleToken) parameter.getToken()).doubleValue()).add(
                        additionalExecutionTime);
        _fireAtPlatformTime(_schedulerFinishTime, executionTimeClock);
        return true;
    }

    /** Set the delayOffset of a port to an array of delayOffset values.
     *  @param inputPort The input port to be annotated.
     *  @param delayOffsets The delayOffset values to annotate.
     *  @exception IllegalActionException If delayOffset parameter already
     *  exists, or the delayOffset parameter cannot be set.
     */
    private static void _setDelayOffset(IOPort inputPort, double[] delayOffsets)
            throws IllegalActionException {
        Parameter parameter = (Parameter) (inputPort)
                .getAttribute("delayOffset");
        if (parameter == null) {
            try {
                parameter = new Parameter(inputPort, "delayOffset");
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(
                        "A delayOffset parameter already exists");
            }
        }
        DoubleToken[] tokens = new DoubleToken[delayOffsets.length];
        for (int i = 0; i < delayOffsets.length; i++) {
            tokens[i] = new DoubleToken(delayOffsets[i]);
        }
        ArrayToken arrayToken = new ArrayToken(tokens);
        parameter.setToken(arrayToken);
    }

    /** Check if the safe-to-process timed interrupt has just occurred.
     *  @return true if a timed interrupt has occurred. Return false otherwise.
     *  @exception IllegalActionException If failed to get physical tag or if
     *  timed interrupt occurred in the past.
     */
    private boolean _timedInterruptOccurred() throws IllegalActionException {
        if (_timedInterruptTimes.isEmpty()) {
            return false;
        }
        TimedEvent timedEvent = _timedInterruptTimes.get(0);
        Time topTimedInterruptWakeUpTime = timedEvent.timeStamp;
        int result = topTimedInterruptWakeUpTime
                .compareTo(getPlatformPhysicalTag(platformTimeClock).timestamp);
        if (result < 0) {
            throw new IllegalActionException(
                    this,
                    "Timed interrupt should have occurred at time: "
                            + topTimedInterruptWakeUpTime.toString()
                            + ", but the current simulated physical time is already: "
                            + getPlatformPhysicalTag(platformTimeClock).timestamp
                            + ".");
        } else if (result == 0) {
            _timedInterruptTimes.remove(0);
            // Findbugs doesn't like the fact timedEvent.contents is an object,
            // but since we want timedEvent to be as general as possible, we
            // leave contents as an object.
            _eventsWithTimedInterrupt.remove(timedEvent.contents);
            return true;
        }
        return false;
    }

    /** Check if we should produce output events to the enclosing director
     *  immediately. This method returns false by default.
     *  @param port Output port to transmit output event immediately.
     *  @return true If the token is to be transferred immediately
     *  from the port.
     *  @exception IllegalActionException If token of this parameter
     *  cannot be evaluated.
     */
    private static boolean _transferImmediately(IOPort port)
            throws IllegalActionException {
        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("transferImmediately");
        if (parameter != null) {
            return ((BooleanToken) parameter.getToken()).booleanValue();
        }
        return false;
    }

    /** Starting from the startPort, traverse the graph to calculate the
     *  delayOffset offset.
     *  @param startPort the port at which to start traversing.
     *  @exception IllegalActionException If there are ports that are both input
     *  and output ports.
     */
    private void _traverseToCalculateDelayOffset(IOPort startPort)
            throws IllegalActionException {
        // Setup a local priority queue to store all reached ports
        HashMap localPortDelays = new HashMap<IOPort, SuperdenseDependency>(
                _portDelays);

        PriorityQueue distQueue = new PriorityQueue<PortDependency>();
        distQueue.add(new PortDependency(startPort,
                (SuperdenseDependency) localPortDelays.get(startPort)));

        // Dijkstra's shortest path algorithm to find all shortest time delays.
        while (!distQueue.isEmpty()) {
            PortDependency portDependency = (PortDependency) distQueue.remove();
            IOPort port = (IOPort) portDependency.port;
            SuperdenseDependency prevDependency = (SuperdenseDependency) portDependency.dependency;
            Actor actor = (Actor) port.getContainer();
            // If this actor has not been visited before, add it to the
            // visitedActor set.
            if (!_visitedActors.contains(actor)) {
                _visitedActors.add(actor);
            }
            if (port.isInput() && port.isOutput()) {
                throw new IllegalActionException(
                        "the causality analysis cannot deal with"
                                + "port that are both input and output");
            }
            // Do not want to traverse to the outside of the platform.
            if (actor != getContainer()) {
                if (port.isInput()) {
                    Collection<IOPort> outputs = _finiteDependentPorts(port);
                    for (IOPort outputPort : outputs) {
                        SuperdenseDependency minimumDelay = (SuperdenseDependency) _getDependency(
                                port, outputPort);
                        SuperdenseDependency modelTime = (SuperdenseDependency) prevDependency
                                .oTimes(minimumDelay);
                        if (((SuperdenseDependency) localPortDelays
                                .get(outputPort)).compareTo(modelTime) > 0) {
                            localPortDelays.put(outputPort, modelTime);
                            distQueue.add(new PortDependency(outputPort,
                                    modelTime));
                        }
                    }
                } else { // The port is an output port
                    // For each receiving port channel pair, add the dependency
                    // in inputModelTimeDelays. We do not need to check whether
                    // there already exists a dependency because if a dependency
                    // already exists for that pair, that dependency must have a
                    // greater value, meaning it should be replaced. This is
                    // because the output port that led to that pair would not
                    // be in distQueue if the dependency was smaller.
                    Receiver[][] remoteReceivers = port.getRemoteReceivers();
                    if (remoteReceivers != null) {
                        for (int i = 0; i < remoteReceivers.length; i++) {
                            if (remoteReceivers[0] != null) {
                                for (int j = 0; j < remoteReceivers[i].length; j++) {
                                    IOPort sinkPort = remoteReceivers[i][j]
                                            .getContainer();
                                    int channel = sinkPort
                                            .getChannelForReceiver(remoteReceivers[i][j]);
                                    // we do not want to traverse to the outside
                                    // of the platform.
                                    if (sinkPort.getContainer() != getContainer()) {
                                        // for this port channel pair, add the
                                        // dependency.
                                        Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) _inputModelTimeDelays
                                                .get(sinkPort);
                                        if (channelDependency == null) {
                                            channelDependency = new HashMap<Integer, SuperdenseDependency>();
                                        }
                                        channelDependency.put(
                                                Integer.valueOf(channel),
                                                prevDependency);
                                        _inputModelTimeDelays.put(sinkPort,
                                                channelDependency);
                                        // After updating dependencies, we need
                                        // to decide whether we should keep
                                        // traversing the graph.
                                        if (((SuperdenseDependency) localPortDelays
                                                .get(sinkPort))
                                                .compareTo(prevDependency) > 0) {
                                            localPortDelays.put(sinkPort,
                                                    prevDependency);
                                            distQueue.add(new PortDependency(
                                                    sinkPort, prevDependency));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (port == startPort) {
                // The (almost) same code (except for getting receivers) is used
                // if the port is a startPort or an output port.
                // This does not support input/output port, should it?
                Receiver[][] deepReceivers = port.deepGetReceivers();
                for (int i = 0; i < deepReceivers.length; i++) {
                    for (int j = 0; j < deepReceivers[i].length; j++) {
                        IOPort sinkPort = deepReceivers[i][j].getContainer();
                        int channel = sinkPort
                                .getChannelForReceiver(deepReceivers[i][j]);
                        // we do not want to traverse to the outside of the
                        // deepReceivers.
                        if (sinkPort.getContainer() != getContainer()) {
                            // for this port channel pair, add the dependency.
                            Map<Integer, SuperdenseDependency> channelDependency = (Map<Integer, SuperdenseDependency>) _inputModelTimeDelays
                                    .get(sinkPort);
                            if (channelDependency == null) {
                                channelDependency = new HashMap<Integer, SuperdenseDependency>();
                            }
                            channelDependency.put(Integer.valueOf(channel),
                                    prevDependency);
                            _inputModelTimeDelays.put(sinkPort,
                                    channelDependency);
                            // After updating dependencies, we need to decide
                            // whether we should keep traversing the graph.
                            if (((SuperdenseDependency) localPortDelays
                                    .get(sinkPort)).compareTo(prevDependency) > 0) {
                                localPortDelays.put(sinkPort, prevDependency);
                                distQueue.add(new PortDependency(sinkPort,
                                        prevDependency));
                            }
                        }
                    }
                }
            }
        }
        _portDelays = localPortDelays;
    }

    /** Update the future oracle times for this director to fire as
     *  a consequence of the change in clock drift.
     *  This director has requested the enclosing DE director to fire
     *  at some oracle times. However, since the clock drift is updated,
     *  the future oracle times at which this director should fire also
     *  needs to be updated.
     *  The updated future fireAt times are calculated as follows:
     *  At platform time p, oracle time o, the drift changes from c to c'.
     *  A previously requested fireAt is at oracle time o1 > o.
     *  i.e., without the change in drift, platform time p1 would
     *  have corresponded to oracle time o1 (so o1 is stored on the list).
     *  After the change in drift, p1 corresponds to oracle time o2.
     *  The equation to calculate o2 is:
     *  <code>o2 = o + (p1 - p) / c'</code>
     *  Also add the original future fire times into the list of
     *  ignored fireAt times if the input ignoreFireAtTimes is not null.
     *  Note, even if an oracle time is added to the ignore list,
     *  it does not guarantee that the director will not fire at that time.
     *  @see #_ignoredPlatformFireAtTimes
     *  Finally, since future oracle fireAt times have changed, the oracle
     *  and platform/execution time pairs are also updated.
     *  @param originalFireAtTimes The original fireAt (oracle) times.
     *  @param ignoreFireAtTimes The list of ignored fireAt (oracle) times.
     *  @param realTimeClock The real time clock whose clock drift has changed.
     *  @param timeCachePair The pair of oracle/platform times that were cached.
     *  @return The new list of fireAt times.
     *  @exception IllegalActionException If either the original or new fireAt
     *  time is in the past, or if the DE director cannot fire at the new oracle
     *  time.
     */
    private List<?> _updateFireAtTimes(List<?> originalFireAtTimes,
            List<Time> ignoreFireAtTimes, RealTimeClock realTimeClock,
            HashMap<Time, Time> timeCachePair) throws IllegalActionException {
        List<Time> newFireAtTimes = new LinkedList<Time>();
        Actor container = (Actor) getContainer();
        Director executiveDirector = ((Actor) container).getExecutiveDirector();
        HashMap<Time, Time> oldTimeCachePair = new HashMap<Time, Time>(
                timeCachePair);
        timeCachePair.clear();
        for (Object originalFireAt : originalFireAtTimes) {
            Time originalFireAtTime = null;
            if (originalFireAt instanceof Time) {
                originalFireAtTime = (Time) originalFireAt;
            } else if (originalFireAt instanceof TimedEvent) {
                originalFireAtTime = ((TimedEvent) originalFireAt).timeStamp;
            } else {
                throw new InternalErrorException(
                        "The input list originalFireAtTimes "
                                + "is expected to be a list of Time or TimedEvent, "
                                + "but it is neither.");
            }
            if (originalFireAtTime.compareTo(_getOraclePhysicalTag().timestamp) < 0) {
                throw new IllegalActionException(
                        this,
                        "The original fireAt time: "
                                + originalFireAtTime
                                + ", which is supposed to happen in the future, "
                                + "is actually in the past: "
                                + _getOraclePhysicalTag().timestamp);
            }
            // o2 = o + (p1 - p) / c'
            Time platformTimeDiff = getPlatformPhysicalTag(realTimeClock).timestamp
                    .subtract(realTimeClock._lastPlatformTime);
            double diff = platformTimeDiff.getDoubleValue()
                    / realTimeClock._clockDrift;
            Time newFireAtTime = realTimeClock._lastOracleTime.add(diff);
            if (newFireAtTime.compareTo(realTimeClock._lastOracleTime) < 0) {
                throw new InternalErrorException("The new fireAt time: "
                        + newFireAtTime
                        + ", which is supposed to happen in the future, "
                        + "is greater than the last oracle time of the "
                        + "clock: " + realTimeClock._lastOracleTime);
            }
            newFireAtTimes.add(newFireAtTime);
            Time temp = executiveDirector.fireAt((Actor) container,
                    newFireAtTime);
            if (temp.compareTo(newFireAtTime) != 0) {
                throw new IllegalActionException(this,
                        "The fireAt wanted to occur " + "at time: "
                                + newFireAtTime
                                + ", however the actual time to fireAt is at: "
                                + temp);
            }
            if (ignoreFireAtTimes != null) {
                ignoreFireAtTimes.add(originalFireAtTime);
            }
            // When the oracle time is updated, the cached oracle time/platform
            // time pair is no longer valid. A new oracle time now applies for
            // each cached platform time. This new pair is saved in
            // timeCachePair, so we throw away the old times, but for each new
            // oracle time, the platform time is saved.
            Time platformTime = oldTimeCachePair.get(originalFireAtTime);
            if (platformTime == null) {
                throw new InternalErrorException(
                        "The original fireAt time was not saved "
                                + "in the time cache pair");
            }
            timeCachePair.put(newFireAtTime, platformTime);
        }
        Collections.sort(newFireAtTimes);
        if (ignoreFireAtTimes != null) {
            Collections.sort(ignoreFireAtTimes);
        }
        return newFireAtTimes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Keep track of a set of Ptides events that already have its
     *  timed interrupt set.
     */
    private Set<PtidesEvent> _eventsWithTimedInterrupt;

    /** A list that keeps track of future fireAt oracle times for execution time
     *  simulation. When the clock drift
     *  changes, all times in this list should be updated to fire at a new
     *  oracle time, and this list should be updated.
     */
    private List<Time> _futureExecutionFireAtTimes;

    /** A list that keeps track of future fireAt oracle times for platform time
     *  simulation, i.e., fireAt times for safe-to-process analysis and
     *  actuator actuation time. When the clock drift
     *  changes, all times in this list should be updated to fire at a new
     *  oracle time, and this list should be updated.
     */
    private List<Time> _futurePlatformFireAtTimes;

    /** A list that keeps track of the oracle execution times at which fireAt
     *  of this
     *  actor should be skipped. When the clock drift changes, all future
     *  fireAt() calls should be updated to fire at a new oracle time, and
     *  the old fireAt times should be written into this list, so these
     *  fireAt times can be ignored.
     */
    private List<Time> _ignoredExecutionFireAtTimes;

    /** A list that keeps track of the oracle platform times at which fireAt
     *  of this actor should be skipped, i.e., fireAt times for safe-to-process
     *  analysis and actuator actuation time. When the clock drift changes, all
     *  future fireAt() calls should be updated to fire at a new oracle time,
     *  and the old fireAt times should be written into this list, so these
     *  fireAt times can be ignored.
     */
    private List<Time> _ignoredPlatformFireAtTimes;

    /** Map input ports to model time delays. These model time delays are then
     *  used to calculate the delayOffset parameter, which is used for safe to
     *  process analysis.
     */
    private Map _inputModelTimeDelays;

    /** Map actors or ports to tags. Each actor keeps track of the tag of the
     *  last event that was consumed when this actor (or the actor containing
     *  the port) fired. This helps to identify cases
     *  where safe-to-process analysis failed unexpectedly.
     */
    private HashMap<NamedObj, Tag> _lastConsumedTag;

    /** Keep track of the last actor with non-zero executing time that was
     *  executing.
     *  This helps to clear the highlighting of that actor when executing stops.
     */
    private Actor _lastExecutingActor;

    /** Keep track of a set of input ports to the composite actor governed by
     *  this director. These input ports are network input ports, which are
     *  input ports that are directly connected to NetworkReceivers.
     */
    private HashSet<IOPort> _networkInputPorts;
    
    /** Keep track of a set of input ports to the composite actor governed by
     *  this director. These input ports are network input ports, which are
     *  input ports that are directly connected to NetworkReceivers.
     */
    private HashSet<IOPort> _networkOutputPorts;

    /** Pairs of "simultaneous" future oracle and execution time. This map
     *  serves as a cache to help simplify the conversion from oracle
     *  time to execution time. Also, since the conversions between these
     *  times is not a bijection (i.e., converting from execution to oracle
     *  time and back would not necessarily give you back the original
     *  oracle time, this cache also makes sure this problem does not
     *  surface in the Ptides director.
     */
    private HashMap<Time, Time> _oracleExecutionTimePair;

    /** Pairs of "simultaneous" future oracle and platform time. This map
     *  serves as a cache to help simplify the conversion from oracle
     *  time and platform time. Also, since the conversions between these
     *  times is not a bijection (i.e., converting from platform to oracle
     *  time and back would not necessarily give you back the original
     *  oracle time, this cache also makes sure this problem does not
     *  surface in the Ptides director.
     */
    private HashMap<Time, Time> _oraclePlatformTimePair;

    /** The physical time at which the currently executing actor, if any,
     *  last resumed execution.
     */
    private Time _physicalTimeExecutionStarted;

    /** Maps ports to dependencies. Used to determine delayOffset parameter
     */
    private Map<IOPort, SuperdenseDependency> _portDelays;

    /** Store source port information for pure events that will be produced
     *  in the future.
     *  This variable maps the destination actor to be fired to the destination
     *  input port of the last event. This is used to determine the causality
     *  information of the pure event that is to be produced.
     */
    private Map _pureEventSourcePorts;

    /** A sorted queue of RealTimeEvents that stores events when they arrive at
     *  the input of the platform, but are not yet visible to the platform
     *  (because of real time delay d_o).
     */
    private PriorityQueue _realTimeInputEventQueue;

    /** A sorted queue of RealTimeEvents that buffers events before they are sent
     *  to the output.
     */
    private PriorityQueue _realTimeOutputEventQueue;

    /** The time at which the scheduler finishes processing.
     */
    private Time _schedulerFinishTime;

    /** Indicate whether an event has finished processing.
     */
    private boolean _scheduleNewEvent;

    /** Keep track of whether time delay actors have been highlighted.
     */
    private boolean _timeDelayHighlighted = false;

    /** A list of TimedEvents that saves Ptides events along with the times
     *  at which an interrupt is supposed to occur for these events. The time
     *  used is the platform time.
     */
    private List<TimedEvent> _timedInterruptTimes;

    /** Keep track of visited actors during delayOffset calculation.
     */
    private Set _visitedActors;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A TimedEvent extended with an additional field to represent
     *  the remaining execution time (in physical time) for processing
     *  the event.
     */
    protected static class ExecutionTimedEvent extends TimedEvent {

        /** Construct a new event with the specified time stamp,
         *  destination actor, and execution time.
         *  @param timeStamp The time stamp.
         *  @param microstep The microstep.
         *  @param executingEvents The events to execute.
         *  @param executionTime The execution time of the actor.
         */
        protected ExecutionTimedEvent(Time timeStamp, int microstep,
                Object executingEvents, Time executionTime) {
            super(timeStamp, executingEvents);
            this.microstep = microstep;
            remainingExecutionTime = executionTime;
        }

        /** Remaining execution time of the currently executing event. */
        protected Time remainingExecutionTime;

        /** Microstep of the executing event. */
        protected int microstep;

        /** Converts the executing event to a string. */
        public String toString() {
            return super.toString() + ", microstep = " + microstep
                    + ", remainingExecutionTime = " + remainingExecutionTime;
        }
    }

    /** Store a port and a dependency
     *  associated with that port. This structure is comparable, and
     *  it compares using the dependency information.
     */
    protected static class PortDependency implements Comparable {

        /** Construct a structure that holds a port and the associated
         *  dependency.
         *  @param port The port.
         *  @param dependency The Dependency.
         */
        protected PortDependency(IOPort port, Dependency dependency) {
            this.port = port;
            this.dependency = dependency;
        }

        /** The port. */
        protected IOPort port;

        /** The dependency. */
        protected Dependency dependency;

        /** Compares this PortDependency with another. Compares the
         *  dependencies of these two objects.
         *  @param arg0 The object comparing to.
         *  @return an int representing the compared value.
         */
        public int compareTo(Object arg0) {
            PortDependency portDependency = (PortDependency) arg0;
            if (this.dependency.compareTo(portDependency.dependency) > 0) {
                return 1;
            } else if (this.dependency.compareTo(portDependency.dependency) < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        /** Checks if this PortDependency is the same as another.
         *  @param arg0 The object checking against.
         *  @return true if the dependencies are equal.
         */
        public boolean equals(Object arg0) {
            if (!(arg0 instanceof PortDependency)) {
                return false;
            }
            return (compareTo(arg0) == 0);
        }

        /** Hashcode for this class.
         *  @return hashcode for this class.
         */
        public int hashCode() {
            return port.hashCode() >>> dependency.hashCode();
        }
    }

    /** A data structure to store two Time values that are interpreted
     *  as simultaneous. Specifically, one of these values is called the
     *  oracle time and one is called the platform time.
     *  When this class is constructed, an initial value of the platform
     *  time is given, and this is interpreted as being simultaneous with
     *  an oracle time of zero.
     *  @author jiazou
     */
    public class RealTimeClock {

        /** Construct a real time clock, with all the times and clock drifts
         *  set to default values: All clock drifts are initialized to Time 1.0,
         *  and the oracle time is initialized to Time 0.0. However the
         *  corresponding platform time is initialized to the value of the
         *  initialClockSynchronizationError parameter. The <i>clockDrift</i>
         *  parameter specifies how much platform time should be incremented for
         *  each unit increment of oracle time. A clock drift of 1.0 means that
         *  the clocks remain perfectly synchronized. A clock drift less than
         *  1.0 means that the platform time clock progresses more slowly
         *  than oracle time, and a clock drift greater than 1.0 means that it
         *  progresses more rapidly.
         *  @param platformTimeAtOracleTimeZero The platform Time value
         *   interpreted to be simultaneous with oracle time zero.
         *  @param clockDrift The relative rate of advance of platform time
         *   w.r.t. oracle time.
         */
        private RealTimeClock(double platformTimeAtOracleTimeZero,
                double clockDrift, Director ptidesDirector)
                throws IllegalActionException {
            _lastPlatformTime = new Time(PtidesBasicDirector.this,
                    platformTimeAtOracleTimeZero);
            _clockDrift = clockDrift;
            _lastOracleTime = new Time(PtidesBasicDirector.this);
        }

        /** Update the clock drift of the realTimeClock, specified by the
         *  clock ID. Check whether the new clock drift is less than zero.
         *  If so, throw an exception. If the new clock drift is different from
         *  the old one, update all future oracle fireAt times. Also keep track
         *  of the list of ignored future fireAt times, so that this director
         *  will not fire at these times.
         *  <p>
         *  See {@link #_updateFireAtTimes}
         *  </p>
         *  Also, upon updating the clock drift, the cached oracle time/platform
         *  time pair should also be updated. The old cache should be thrown
         *  away, while a new cache should be made for each pair.
         *  See {@link #_oracleExecutionTimePair},
         *  {@link #_oraclePlatformTimePair}
         *  All parameters of the realTimeClock are also updated in the process.
         *  @param newClockDrift a double that indicates the new drift of
         *  that particular clock.
         *  @exception IllegalActionException If either the original or updated
         *  fireAt time is in the past, or if the new clock drift is less than
         *  or equal to zero.
         */
        public void updateClockDrift(double newClockDrift)
                throws IllegalActionException {
            if (newClockDrift <= 0.0) {
                throw new IllegalActionException(
                        PtidesBasicDirector.this,
                        "The new clock drift is of value <= 0.0, this means "
                                + "for every cycle the clock advances in oracle time, "
                                + "the platform cycle decreases to stays put, i.e, "
                                + "time goes backwards or stays put. This is not a"
                                + "llowed.");
            }
            HashMap<Time, Time> timeCachePair = null;
            if (this == platformTimeClock) {
                timeCachePair = _oraclePlatformTimePair;
            } else if (this == executionTimeClock) {
                timeCachePair = _oracleExecutionTimePair;
            } else {
                throw new IllegalActionException(
                        PtidesBasicDirector.this,
                        "Trying to update a clock that is neither "
                                + "platform time clock or execution time clock.");
            }
            double thisTimeResolution = localClock.getTimeResolution();
            if (Math.abs(_clockDrift - newClockDrift) > thisTimeResolution) {
                // First update all the parameters in realTimeClock.
                Time newOracleTime = _getOraclePhysicalTag().timestamp;
                _lastPlatformTime = _getPlatformPhysicalTimeForOraclePhysicalTime(
                        newOracleTime, this, timeCachePair);
                _lastOracleTime = newOracleTime;
                _clockDrift = newClockDrift;
                // Based on the above information, update the future fireAt
                // times.
                if (this == executionTimeClock) {
                    _futureExecutionFireAtTimes = (List<Time>) _updateFireAtTimes(
                            _futureExecutionFireAtTimes,
                            _ignoredExecutionFireAtTimes, this, timeCachePair);
                } else if (this == platformTimeClock) {
                    _futurePlatformFireAtTimes = (List<Time>) _updateFireAtTimes(
                            _futurePlatformFireAtTimes,
                            _ignoredPlatformFireAtTimes, this, timeCachePair);
                    // If the clock is a platform clock, we also need to
                    // update the timed interrupt times. However,
                    // ignore fireAt time is not needed, since a timed
                    // interrupt time is part of a platform fireAt time.
                    _timedInterruptTimes = (List<TimedEvent>) _updateFireAtTimes(
                            _timedInterruptTimes, null, this, timeCachePair);
                } else {
                    throw new InternalErrorException(
                            PtidesBasicDirector.this,
                            null,
                            "The real time "
                                    + "clock that's trying to update fireAtTimes "
                                    + "is neither a execution time clock nor a "
                                    + "platform time clock. The Ptides director "
                                    + "doesn't know how to deal with it.");
                }
            }
        }

        /** The ratio between how much the platform clock changes each unit time
         *  as the oracle clock changes.
         */
        private double _clockDrift;

        /** The last saved platform time. This time is interpreted as being
         *  simultaneous with the value of _lastOracleTime.
         */
        private Time _lastPlatformTime;

        /** The last saved oracle time. This time is interpreted as being
         *  simultaneous with the value of _lastPlatformTime.
         */
        private Time _lastOracleTime;
    }

    /** A structure that holds a token with the port and channel it's
     *  connected to, as well as the timestamp associated with this
     *  token.  This object is used to hold sensor and actuation
     *  events.
     */
    protected static class RealTimeEvent implements Comparable {

        /** Construct a structure that holds a real-time event. This
         *  event saves the token to be transmitted, the port and
         *  channel this token should be delivered to, and the time
         *  this token should be delivered at.
         *  @param port The destination port.
         *  @param channel The destination channel.
         *  @param token The token to be delivered.
         *  @param deliveryTag The platform time of delivery of this token.
         *  @param timestampTag The timestamp tag of this token.
         */
        protected RealTimeEvent(IOPort port, int channel, Token token,
                Tag deliveryTag, Tag timestampTag) {
            this.port = port;
            this.channel = channel;
            this.token = token;
            this.deliveryTag = deliveryTag;
            this.timestampTag = timestampTag;
        }

        /** The channel. */
        private int channel;

        /** The time of delivery. */
        private Tag deliveryTag;

        /** The port. */
        private IOPort port;

        /** The tag of the input event. */
        private Tag timestampTag;

        /** The token. */
        private Token token;

        /** Compares this RealTimeEvent with another. Compares the delivery
         *  times of these two events.
         *  @param other The object comparing to.
         *  @return an int representing the comparison value.
         */
        public int compareTo(Object other) {
            return deliveryTag.compareTo(((RealTimeEvent) other).deliveryTag);
        }

        /** Checks if the other object is equal to this RealTimeEvent.
         */
        public boolean equals(Object other) {
            if (!(other instanceof RealTimeEvent)) {
                return false;
            }
            return deliveryTag.equals(((RealTimeEvent) other).deliveryTag);
        }

        /** Hashcode for this class.
         *  @return hashcode for this class.
         */
        public int hashCode() {
            int primitiveFieldHash = channel;
            int objectFieldHash = deliveryTag.hashCode() >>> port.hashCode() >>> timestampTag
                    .hashCode() >>> token.hashCode();
            return primitiveFieldHash >>> objectFieldHash;
        }
    }
}
