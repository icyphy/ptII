/* An actor that delays the input by the specified amount.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import ptolemy.actor.CausalityMarker;
import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TimeDelay

/**
 This actor delays the input by a specified amount of time given by
 the <i>delay</i> port or parameter, which defaults to 1.0. It is designed
 to be used in timed domains, particularly DE. It can also be used
 in other domains, such as SR and SDF, but this will only be useful if the
 delay value is a multiple of the period of those directors. The value
 of <i>delay</i> is required to be nonnegative. In addition, if the
 <i>delay</i> port is connected (and hence the delay will be variable
 at run time), then the values provided at the port are required to be
 greater than or equal <i>minimumDelay</i>,
 which defaults to the value of <i>delay</i>.
 The input and output types are unconstrained, except that the output type
 must be the same as that of the input.
 <p>
 Note that in Ptides the SuperdenseDependency is used for computing
 offsets and deadlines. The dependency between the input and the output
 of this actor is the <i>minimumDelay</i>. A <i>minimumDelay</i> of
 values greater than 0.0 allows for more efficient execution of Ptides models. If
 this actor is used as a fixed delay actor, i.e. the delay value is not
 changed during the execution, the <i>minimumDelay</i> should be set to
 the actual delay, which is the default.
 <p>
 For directors that implement {@link SuperdenseTimeDirector}, such as
 DE, the output microstep of an event will match the input microstep,
 unless the time delay is 0.0, in which case, the output microstep will
 be one greater than the input microstep.
 A time delay of 0.0 is sometimes useful to break
 causality loops in feedback systems. It is sometimes useful to think
 of this zero-valued delay as an infinitesimal delay.
 <p>
 This actor keeps a local FIFO queue to store all received but not produced
 inputs. The behavior of this actor on each firing is to
 output any previously received token that is scheduled to be produced
 at the current time (and microstep).
 If there is no previously received token scheduled
 to be produced, then the output will be absent.
 <p>
 Inputs are read only during the postfire() method.
 If an input is present, then this actor schedules itself to fire again
 to produce the just received token on the corresponding output channel after
 the appropriate time delay. Note that if the value of delay is 0.0, the
 actor schedules itself to fire at the current model time, resulting in
 an output with an incremented microstep.
 <p>
 This actor can also be used in the Continuous
 domain, but it is only useful to delay purely discrete signals.
 As a consequence, for directors that implement {@link SuperdenseTimeDirector},
 this actor insists that input events have microstep 1 or greater.
 It will throw an exception if it receives an input with microstep 0,
 which in the Continuous domain, implies a continuous signal.
 There are two reasons for rejecting continuous inputs.
 First, because of the way variable-step-size ODE solvers work, the TimeDelay
 actor has the side effect of forcing the solver to use very small step
 sizes, which slows down a simulation.
 Second, and more important, some odd artifacts will
 appear if a variable step-size solver is being used. In particular, the
 output will be absent on any firing where there was no input at exactly
 time <i>t</i> - <i>d</i>, where <i>t</i> is the time of the firing
 and <i>d</i> is the value of the delay parameter. Thus, a continuous
 signal input will have gaps on the output, and will fail to be
bR piecewise continuous.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class TimeDelay extends Transformer {
    /** Construct an actor with the specified container and name.
     *  Constrain that the output type to be the same as the input type.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimeDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        delay = new PortParameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("1.0");
        _delay = 1.0;

        minimumDelay = new Parameter(this, "minimumDelay");
        minimumDelay.setTypeEquals(BaseType.DOUBLE);
        minimumDelay.setExpression("delay");

        // Put the delay input on the bottom of the actor.
        StringAttribute controlCardinal = new StringAttribute(delay.getPort(),
                "_cardinal");
        controlCardinal.setExpression("SOUTH");

        output.setTypeSameAs(input);

        // Empty set of dependent ports.
        // This declaration is done this way for the benefit of Ptides.
        // It is interpreted by Ptides to indicate that out-of-order
        // execution is allowed.
        Set<Port> dependentPorts = new HashSet<Port>();
        _causalityMarker = new CausalityMarker(this, "causalityMarker");
        _causalityMarker.addDependentPortSet(dependentPorts);

    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The amount of delay. The default for this parameter is 1.0.
     *  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public PortParameter delay;

    /** Minimum delay to impose if the <i>delay</i>
     *  port is connected. This is a double that defaults to the value of the delay.
     */
    public Parameter minimumDelay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>delay</i>, then ensure that the value
     *  is non-negative.
     *  <p>NOTE: the newDelay may be 0.0, which may change the causality
     *  property of the model. We leave the model designers to decide
     *  whether the zero delay is really what they want.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == delay || attribute == minimumDelay) {
            double newDelay = ((DoubleToken) (delay.getToken())).doubleValue();
            double newMinimumDelay = ((DoubleToken) (minimumDelay.getToken()))
                    .doubleValue();
            if (newMinimumDelay > newDelay) {
                throw new IllegalActionException(this,
                        "Cannot have minimumDelay > delay "
                                + (newMinimumDelay > newDelay)
                                + ". Modify the delay value.");
            }
            _minimumDelay = newMinimumDelay;
            _delay = newDelay;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. Set a type
     *  constraint that the output type is the same as the that of input.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TimeDelay newObject = (TimeDelay) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.input);
        newObject._causalityMarker = (CausalityMarker) newObject
                .getAttribute("causalityMarker");
        newObject._pendingOutputs = null;
        return newObject;
    }

    /** Declare that the output does not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(delay.getPort(), output, _minimumDelay);
        _declareDelayDependency(input, output, _minimumDelay);
    }

    /** Read one token from the input. Send out a token that is scheduled
     *  to be produced at the current time.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_isTime()) {
            // Time to produce the output.
            PendingEvent event = _pendingOutputs.getLast();
            output.send(0, event.token);
            if (_debugging) {
                _debug("Sending output. Value = " + event.token + ", time = "
                        + event.timeStamp + ", microstep = " + event.microstep);
            }
        } else {
            // Nothing to send. Assert the output to be absent.
            output.send(0, null);
            if (_debugging) {
                _debug("Nothing to send. Asserting absent output at time "
                        + getDirector().getModelTime());
            }
        }
    }

    /** Initialize the states of this actor.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_pendingOutputs != null) {
            _pendingOutputs.clear();
        } else {
            _pendingOutputs = new LinkedList<PendingEvent>();
        }
    }

    /** Return false indicating that this actor can be fired even if
     *  the inputs are unknown.
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Read the input, if there is one, and request refiring.
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed or the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        delay.update();

        // No point in using the isTime() method here, since we need
        // all the intermediate values.
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int microstep = 1;
        if (director instanceof SuperdenseTimeDirector) {
            microstep = ((SuperdenseTimeDirector) director).getIndex();
        }

        if (_pendingOutputs.size() > 0) {
            PendingEvent event = _pendingOutputs.getLast();
            int comparison = currentTime.compareTo(event.timeStamp);
            if (comparison == 0 && microstep >= event.microstep) {
                // Remove the oldest event in the event queue, since
                // this will have been produced in fire().
                _pendingOutputs.removeLast();
            }
        }

        // Check whether the next oldest event has the same time.
        if (_pendingOutputs.size() > 0) {
            // The current time stamp of the next event
            // may match, but not the microstep.
            // In this case, we have to request a refiring.
            PendingEvent nextEvent = _pendingOutputs.getLast();
            if (currentTime.equals(nextEvent.timeStamp)) {
                _fireAt(currentTime);
            }
            if (_debugging) {
                _debug("Deferring output to a later microstep. Value = "
                        + nextEvent.token + ", time = " + nextEvent.timeStamp
                        + ", microstep = " + nextEvent.microstep
                        + ". Current microstep is " + microstep);
            }
        }

        if (input.hasToken(0)) {
            Token token = input.get(0);
            PendingEvent newEvent = new PendingEvent();
            newEvent.token = token;
            newEvent.timeStamp = currentTime.add(_delay);
            newEvent.microstep = microstep;
            if (_delay == 0.0) {
                newEvent.microstep++;
            }
            _fireAt(newEvent.timeStamp);
            _addEvent(newEvent);
            if (_debugging) {
                _debug("Queueing event for later output. Value = "
                        + newEvent.token + ", time = " + newEvent.timeStamp
                        + ", microstep = " + newEvent.microstep);
            }
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Insert a new event into the queue of pending events.
     *  This method ensures that events in the queue are in time-stamp
     *  and microstep order, and that when time stamps and microsteps match,
     *  that the order is FIFO. The latest time stamp and largest microstep
     *  are at the beginning of the list.
     *  @param newEvent The new event to be inserted into the queue
     *  of pending events.
     */
    protected void _addEvent(PendingEvent newEvent) {
        if (_pendingOutputs.size() == 0) {
            // List is empty. This is easy.
            _pendingOutputs.add(newEvent);
            return;
        }
        // Optimize for the common case, which is that insertions
        // go at the beginning.
        PendingEvent newestEvent = _pendingOutputs.getFirst();
        int comparison = newEvent.timeStamp.compareTo(newestEvent.timeStamp);
        if (comparison > 0) {
            // New event has higher time stamp than all in the queue.
            _pendingOutputs.addFirst(newEvent);
        } else if (comparison == 0
                && newEvent.microstep >= newestEvent.microstep) {
            // New event has the same time stamp as the newest
            // in the queue, but microstep is greater or equal.
            _pendingOutputs.addFirst(newEvent);
        } else {
            // Event has to be inserted into the queue.
            // Here we do a linear search, which is a poor choice if
            // the delay is highly variable. But that case is rare.
            ListIterator<PendingEvent> iterator = _pendingOutputs
                    .listIterator();
            while (iterator.hasNext()) {
                PendingEvent nextNewestEvent = iterator.next();
                comparison = newEvent.timeStamp
                        .compareTo(nextNewestEvent.timeStamp);
                if (comparison > 0
                        || (comparison == 0 && newEvent.microstep >= newestEvent.microstep)) {
                    // New event is later than or equal to current one.
                    // First replace the current element, then add the current element back in.
                    iterator.set(newEvent);
                    iterator.add(nextNewestEvent);
                    return;
                }
            }
            // Got to the end of the list without finding an event
            // that is older than the new event. Put at the end.
            _pendingOutputs.addLast(newEvent);
        }
    }

    /** Return true if it is time to produce an output.
     *  @return Return true if it is time to produce an output.
     *  @exception IllegalActionException If current time exceeds the time of
     *   of the next pending event.
     */
    protected boolean _isTime() throws IllegalActionException {
        if (_pendingOutputs.size() == 0) {
            // No pending events.
            return false;
        }
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int microstep = 1;
        if (director instanceof SuperdenseTimeDirector) {
            microstep = ((SuperdenseTimeDirector) director).getIndex();
        }

        PendingEvent event = _pendingOutputs.getLast();
        int comparison = currentTime.compareTo(event.timeStamp);
        if (comparison > 0) {
            // Current time exceeds the event time. This should not happen.
            throw new IllegalActionException(
                    this,
                    "Failed to output event with time stamp "
                            + event.timeStamp
                            + " and value "
                            + event.token
                            + ". Perhaps the director is incompatible with TimeDelay?");
        }
        // If the time is right and the microstep matches or exceeds
        // the desired microstep, then it is time.
        return (comparison == 0 && microstep >= event.microstep);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The amount of delay. */
    protected double _delay;

    /** The amount of minimumDelay. */
    protected double _minimumDelay = 0.0;

    /** A local queue to store the delayed output tokens. */
    protected LinkedList<PendingEvent> _pendingOutputs;

    /** A causality marker to store information about how pure events are causally
     *  related to trigger events.
     */
    protected CausalityMarker _causalityMarker;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Data structure to store pending events. */
    public static class PendingEvent {
        // FindBugs indicates that this should be a static class.
        /** The time stamp for the pending event. */
        public Time timeStamp;
        /** The token associated with the event. */
        public Token token;
        /** The microstep associated with the pending event. */
        public int microstep;
    }
}
