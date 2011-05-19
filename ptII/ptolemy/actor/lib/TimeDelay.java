/* An actor that delays the input by the specified amount.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
import java.util.Set;

import ptolemy.actor.CausalityMarker;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TimeDelay

/**
 This actor delays the input by a specified amount of time. It is designed
 to be used in timed domains such as DE. It can also be used
 in other domains, such as SR and SDF, but this will only be useful if the
 delay value is a multiple of the period of those directors. The amount
 of the time is required to be non-negative and has a default value 1.0.
 The input and output types are unconstrained, except that the output type
 must be the same as that of the input. It can be used in the Continuous
 domain, but it is really only useful to delay purely discrete signals.
 Because of the way variable-step-size ODE solvers work, the TimeDelay
 actor has the side effect of forcing the solver to use very small step
 sizes, which slows down a simulation.
 <p>
 This actor keeps a local FIFO queue to store all received but not processed
 inputs. The behavior of this actor on each firing is to read a token from
 the input, if there is one, store it into the local queue. It will
 also output any previously received token that is scheduled to be produced
 at the current time. If there is no previously received token scheduled
 to be produced, then the output will (implicitly) be absent.
 <p>
 If an input is read during the fire() method, then
 during the postfire() method, this actor schedules itself to fire again
 to produce the just received token on the corresponding output channel after
 the appropriate time delay. Note that if the value of delay is 0.0, the
 actor schedules itself to fire at the current model time.
 <p>
 This actor can be used to delay either discrete signals or continuous-time
 signals. However, in the latter case, some odd artifacts will inevitably
 appear if a variable step-size solver is being used. In particular, the
 output will be absent on any firing where there was no input at exactly
 time <i>t</i> - <i>d</i>, where <i>t</i> is the time of the firing
 and <i>d</i> is the value of the delay parameter.
 <p>
 Occasionally, this actor is useful with the
 delay parameter set to 0.0.  The time stamp of the output will
 equal that of the input, but there is a "microstep" delay.
 Several Ptolemy II domains use a "super dense" model
 of time, meaning that a signal from one actor to another can
 contain multiple events with the same time stamp. These events
 are "simultaneous," but nonetheless
 have a well-defined sequential ordering determined by the order
 in which they are produced.
 If \textit{delay} is 0.0, then the fire() method of this actor
 always produces on its output port the event consumed in the
 \textit{previous iteration} with the same time stamp, if there
 was one. If there wasn't such a previous iteration, then it
 produces no output.  Its postfire() method consumes and
 records the input for use in the next iteration, if there
 is such an input, and also requests a refiring at the current
 time.  This refire request triggers the next iteration (at
 the same time stamp), on which the output is produced.
 <p>
 A consequence of this strategy is that this actor is
 able to produce an output (or assert that there is no output) before the
 input with the same time is known.   Hence, it can be used to break
 causality loops in feedback systems. The Continuous director will leverage this when
 determining the fixed point behavior. It is sometimes useful to think
 of this zero-valued delay as an infinitesimal delay.

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

        delay = new Parameter(this, "delay");
        delay.setTypeEquals(BaseType.DOUBLE);
        delay.setExpression("1.0");
        _delay = 1.0;

        output.setTypeSameAs(input);

        // empty set of dependent ports.
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
    public Parameter delay;

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
        if (attribute == delay) {
            double newDelay = ((DoubleToken) (delay.getToken())).doubleValue();

            if (newDelay < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative delay: " + newDelay);
            } else {
                _delay = newDelay;
            }
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
        return newObject;
    }

    /** Declare that the output does not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(input, output, _delay);
    }

    /** Read one token from the input. Send out a token that is scheduled
     *  to produce at the current time to the output.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // produce output
        // NOTE: The amount of delay may be zero.
        // In this case, if there is already some token scheduled to
        // be produced at the current time before the current input
        // arrives, that token is produced. While the current input
        // is delayed to the next available firing at the current time.
        //
        // If we observe events in the queue that have expired,
        // discard them here.
        Time currentTime = getDirector().getModelTime();
        _currentOutput = null;

        if (_delayedOutputTokens.size() == 0) {
            output.send(0, null);
            return;
        }

        while (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            int comparison = eventTime.compareTo(currentTime);
            if (comparison == 0) {
                _currentOutput = (Token) earliestEvent.contents;
                output.send(0, _currentOutput);
                break;
            } else if (comparison > 0) {
                // It is not yet time to produce an output.
                output.send(0, null);
                break;
            }
            // If we get here, then we have passed the time of the delayed
            // output. We simply discard it and check the next one in the queue.
            _delayedOutputTokens.take();
        }
    }

    /** Initialize the states of this actor.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentOutput = null;
        _delayedOutputTokens = new CalendarQueue(
                new TimedEvent.TimeComparator());
    }

    /** Return false indicating that this actor can be fired even if
     *  the inputs are unknown.
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Process the current input if it has not been processed. Schedule
     *  a firing to produce the earliest output token.
     *  @exception IllegalActionException If scheduling to refire cannot
     *  be performed or the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time delayToTime = currentTime.add(_delay);

        // Remove the token that is sent at the current time.
        if (_delayedOutputTokens.size() > 0) {
            if (_currentOutput != null) {
                _delayedOutputTokens.take();
            }
        }

        // Handle the refiring of the multiple tokens
        // that are scheduled to be produced at the same time.
        if (_delayedOutputTokens.size() > 0) {
            TimedEvent earliestEvent = (TimedEvent) _delayedOutputTokens.get();
            Time eventTime = earliestEvent.timeStamp;

            if (eventTime.equals(currentTime)) {
                _fireAt(currentTime);
            }
        }

        // consume input
        if (input.hasToken(0)) {
            _delayedOutputTokens.put(new TimedEvent(delayToTime, input.get(0)));
            _fireAt(delayToTime);
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Current output. */
    protected Token _currentOutput;

    /** The amount of delay. */
    protected double _delay;

    /** A local event queue to store the delayed output tokens. */
    protected CalendarQueue _delayedOutputTokens;

    /** A causality marker to store information about how pure events are causally
     *  related to trigger events.
     */
    protected CausalityMarker _causalityMarker;

}
