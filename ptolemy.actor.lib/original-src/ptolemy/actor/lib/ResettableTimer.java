/* Produce an output after the time specified on the input has elapsed.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ResettableTimer

/**
 Produce an output after the time specified on the input has elapsed.
 If the input value is 0.0, then the output will be produced at the
 next superdense time index (i.e., on the next firing, but at the current
 time). If the input is negative, this actor will cancel the previously
 requested output, if it has not yet been produced by the time the
 negative input is received.
 The value of the output is specified by the <i>value</i> parameter.
 <p>
 If the <i>preemptive</i> parameter is true (the default), then if
 a new input arrives before the previous timer request has expired,
 then that timer request is canceled. If an input arrives at the same
 time that the previous timer request expires, an output is produced
 immediately. The timer request is not cancelled.
 <p>
 If the <i>preemptive</i> parameter is
 false, then the new input will cause the timer to start only after
 the currently pending timer (if any is pending) expires.
 <p>
 When the <i>preemptive</i> parameter is true,
 this actor resembles the VariableDelay actor in the DE domain, except that
 arrivals of new inputs before the delay has expired causes the
 previously scheduled output to be canceled. Also, the output value
 is given in this actor
 by the <i>value</i> parameter instead of by the input.
 <p>
 When the <i>preemptive</i> parameter is false,
 this actor resembles the Server actor in the DE domain, except that
 the time delay is specified by the single input.
 The Server actor, by contrast, has separate inputs for service time and
 payload, and the service time experienced by a payload depends
 on the most recently arrived service time input <i>at the time
 that the payload service begins</i>, not at the time the payload
 arrives.
 <p>
 If this actor is used in a modal model and is in a mode that is
 not active for some time, then no outputs will be produced for
 the times it is inactive. If it becomes active again before the
 scheduled time to produce an output, then it will produce that
 output. If it is not preemptive, then upon
 becoming active again, it will behave as if it had been active
 during the intervening time, calculating when the outputs should
 have been produced, and discarding them if the calculated time
 falls in the inactive period.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ResettableTimer extends Transformer {
    /** Construct an actor with the specified container and name.
     *  Declare that the input can only receive double tokens and the output
     *  has a data type the same as the value parameter.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ResettableTimer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        value = new Parameter(this, "value", new BooleanToken(true));
        preemptive = new Parameter(this, "preemptive", new BooleanToken(true));
        preemptive.setTypeEquals(BaseType.BOOLEAN);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeSameAs(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      ports and parameters                 ////

    /** Indicator of whether new inputs cancel previous requests.
     *  This is a boolean that defaults to true.
     */
    public Parameter preemptive;

    /** The value produced at the output.  This can have any type,
     *  and it defaults to a boolean token with value <i>true</i>.
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and links the type of the <i>value</i> parameter
     *  to the output.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ResettableTimer newObject = (ResettableTimer) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.value);
        return newObject;
    }

    /** Declare that the output does not immediately depend on the input.
     *  @exception IllegalActionException If causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(input, output, 0.0);
    }

    /** If an output is scheduled to be produced, then produce it.
     *  @exception IllegalActionException If there is no director, or can not
     *  send or get tokens from ports.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 0;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        if (_debugging) {
            _debug("Fire at time " + currentTime + ", microstep "
                    + currentMicrostep);
        }
        int comparison = currentTime.compareTo(_pendingOutputTime);
        if (comparison == 0 && currentMicrostep == _pendingOutputMicrostep) {
            // Current pending requests matches current time.
            if (_debugging) {
                _debug("Time matches. Sending output.");
            }
            output.send(0, value.getToken());
        } else if (_pendingOutputTime == Time.NEGATIVE_INFINITY) {
            // No pending requests.
            if (_debugging) {
                _debug("No pending requests.");
            }
            return;
        } else if (!((BooleanToken) preemptive.getToken()).booleanValue()) {
            // Non-preemptive behavior. May need to catch up.
            while (comparison > 0 || comparison == 0
                    && currentMicrostep > _pendingOutputMicrostep) {
                // Current time has passed the pending output time.
                if (_debugging) {
                    _debug("Time passed expected output time of "
                            + _pendingOutputTime + ", microstep "
                            + _pendingOutputMicrostep);
                }
                // May need to catch up.
                if (_pendingRequests == null || _pendingRequests.size() == 0) {
                    // No more pending requests.
                    if (_debugging) {
                        _debug("No more pending requests.");
                    }
                    _pendingOutputTime = Time.NEGATIVE_INFINITY;
                    _pendingOutputMicrostep = 1;
                    break;
                }
                // NOTE: The following changes the state of the actor, but this is
                // safe as long as time does not roll back upon re-activation in
                // a modal model.
                TimedEvent event = (TimedEvent) _pendingRequests.take();
                // Check for possible cancel event.
                if (_pendingRequests.size() > 0) {
                    TimedEvent possibleCancel = (TimedEvent) _pendingRequests
                            .get();
                    if (possibleCancel.contents == null) {
                        // Found a cancel event.
                        _pendingRequests.take();
                        // Skip this event and look to see whether there is another.
                        continue;
                    }
                }
                // The time stamp of the event is the time the input
                // arrived, and its value is the value of the input.
                // Calculate the time at which the first pending event should be produced.
                double delayValue = ((DoubleToken) event.contents)
                        .doubleValue();
                _pendingOutputTime = _pendingOutputTime.add(delayValue);
                if (delayValue > 0) {
                    _pendingOutputMicrostep = 1;
                } else {
                    _pendingOutputMicrostep = currentMicrostep + 1;
                }
                comparison = currentTime.compareTo(_pendingOutputTime);
                if (comparison == 0
                        && currentMicrostep == _pendingOutputMicrostep) {
                    // Next pending request matches current time.
                    if (_debugging) {
                        _debug("Time matches pending output. Sending output.");
                    }
                    output.send(0, value.getToken());
                    break;
                }
                // If the next pending request is still in the past,
                // repeat by looking at the next event.
            }
        }
    }

    /** Initialize the internal states of this actor.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _pendingOutputTime = Time.NEGATIVE_INFINITY;
        _pendingOutputMicrostep = 1;
        if (_pendingRequests != null) {
            _pendingRequests.clear();
        }
    }

    /** Read the input (if any) and schedule a future output.
     *  @exception IllegalActionException If reading the input,
     *   or requesting a refiring throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Token inputToken = null;
        double delayValue = -1;
        boolean isPreemptive = ((BooleanToken) preemptive.getToken())
                .booleanValue();
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentMicrostep = 0;
        if (director instanceof SuperdenseTimeDirector) {
            currentMicrostep = ((SuperdenseTimeDirector) director).getIndex();
        }
        if (_debugging) {
            _debug("Postfire at time " + currentTime + ", microstep "
                    + currentMicrostep);
        }
        // Since postfire concludes the iteration, discard pending data if it was produced
        // in fire().
        if (currentTime.equals(_pendingOutputTime)
                && currentMicrostep == _pendingOutputMicrostep) {
            _pendingOutputTime = Time.NEGATIVE_INFINITY;
            _pendingOutputMicrostep = 1;
        }
        if (input.hasToken(0)) {
            inputToken = input.get(0);
            delayValue = ((DoubleToken) inputToken).doubleValue();
            if (_debugging) {
                _debug("Read input " + delayValue);
            }
            if (delayValue < 0) {
                // Cancel the previous request.
                if (!isPreemptive && _pendingRequests != null
                        && _pendingRequests.size() > 0) {
                    // Append a cancel request to the event queue.
                    TimedEvent cancelEvent = new TimedEvent(currentTime, null);
                    _pendingRequests.put(cancelEvent);
                } else {
                    // Cancel the currently pending output value, if there one.
                    if (_pendingOutputTime.compareTo(currentTime) >= 0) {
                        _pendingOutputTime = Time.NEGATIVE_INFINITY;
                        _pendingOutputMicrostep = 1;
                    }
                }
                // Continue with the code below as if no new input has arrived.
                inputToken = null;
            }
        }
        if (isPreemptive) {
            // Preemptive behavior.
            if (inputToken != null) {
                // If there is an input, update the pending output time.
                _pendingOutputTime = currentTime.add(delayValue);
                if (delayValue != 0.0) {
                    _pendingOutputMicrostep = 1;
                } else {
                    _pendingOutputMicrostep = currentMicrostep + 1;
                }
                _fireAt(_pendingOutputTime);
                if (_debugging) {
                    _debug("Requesting refiring at " + _pendingOutputTime
                            + ", microstep " + _pendingOutputMicrostep);
                }
            } else {
                // There is no input. If the pending output matches the current time
                // but the current microstep is too small, request refiring at the current
                // time.
                if (currentTime.equals(_pendingOutputTime)
                        && currentMicrostep < _pendingOutputMicrostep) {
                    // The firing is in response to a previous request, but the microstep is too early.
                    // Note that this should not happen, but we are begin paranoid here.
                    if (_debugging) {
                        _debug("Microstep is too early. Refire at "
                                + currentTime + ", microstep "
                                + _pendingOutputMicrostep);
                    }
                    _fireAt(currentTime);
                }
            }
        } else {
            // Nonpreemptive behavior. If there is a new input,
            // stick on the queue.
            // First, make sure we have a queue.
            if (_pendingRequests == null) {
                // Non-preemptive behavior is the same if there is no
                // pending request in the future.
                _pendingRequests = new CalendarQueue(
                        new TimedEvent.TimeComparator());
            }
            if (inputToken != null) {
                if (_debugging) {
                    _debug("Deferring start of timer with value " + inputToken);
                }
                _pendingRequests.put(new TimedEvent(currentTime, inputToken));
            }
            while (_pendingRequests.size() > 0
                    && _pendingOutputTime == Time.NEGATIVE_INFINITY) {
                // Get the first pending request and schedule a future firing,
                // but only if there isn't already one pending.
                TimedEvent event = (TimedEvent) _pendingRequests.take();
                // Check for possible cancel event.
                if (_pendingRequests.size() > 0) {
                    TimedEvent possibleCancel = (TimedEvent) _pendingRequests
                            .get();
                    if (possibleCancel.contents == null) {
                        // Found a cancel event.
                        _pendingRequests.take();
                        // Skip this event and look to see whether there is another.
                        continue;
                    }
                }
                delayValue = ((DoubleToken) event.contents).doubleValue();
                _pendingOutputTime = currentTime.add(delayValue);
                if (delayValue != 0.0) {
                    _pendingOutputMicrostep = 1;
                } else {
                    _pendingOutputMicrostep = currentMicrostep + 1;
                }
                if (_debugging) {
                    _debug("Requesting refiring at time " + _pendingOutputTime
                            + ", microstep " + _pendingOutputMicrostep);
                }
                _fireAt(_pendingOutputTime);
                break;
            }
        }
        return super.postfire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Pending output time. */
    private Time _pendingOutputTime;

    /** Pending output microstep. */
    private int _pendingOutputMicrostep;

    /** A local queue to store the pending requests. */
    private CalendarQueue _pendingRequests;
}
