/* A continuous clock source.

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
package ptolemy.domains.hs.lib;

import ptolemy.actor.lib.Clock;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.hs.kernel.HSDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ContinuousClock

/**
 This is a clock source used in continuous-time (CT) domain.
 It extends the clock actor in ptolemy/actor/lib directory
 but overrides the fire, initialize, preinitialize and postfire method.
 This actor serves as a wave form generator in CT domain.
 <p>
 The actor uses the fireAt() method of the director to request
 firings at the beginning of each period plus each of the offsets,
 which are treated as breakpoints. At each breakpoint,
 the actor produces two outputs, one at t_minus phase and the other
 one at t_plus phase. The time does not advance at these two phases.
 For example, with the default settings, at time 1.0, the actor
 produces 0 at t_minus phase and 1 at t_plus phase. Note, at
 time t, which is between t_minus and t_plus, the possible output of
 this actor can be any value between 0 and 1.
 <p>
 The clock has a stopTime parameter and a numberOfCycles parameter.
 The whole model will stop execution when the
 stop time is reached. If the numberOfCycles is set to a positive integer,
 the clock will continue outputting the value of the defaultValue parameter
 after the number of cycles are reached if the stop time is not reached.
 <p>
 @see ptolemy.actor.lib.Clock
 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Red (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class ContinuousClock extends Clock implements CTEventGenerator {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ContinuousClock(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        defaultValue = new Parameter(this, "defaultValue");
        defaultValue.setExpression("0");

        // Override the signal type to be CONTINUOUS to indicate
        // that this actor produce outputs with state semantics.
        ((Parameter) output.getAttribute("signalType"))
                .setToken(new StringToken("CONTINUOUS"));

        // Override of the trigger signal type to CONTINUOUS.
        ((Parameter) trigger.getAttribute("signalType"))
                .setToken(new StringToken("CONTINUOUS"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The default value used after the clock stops. In the triggered
     *  continuous clock, which extends this class and has a start
     *  trigger, the default value is used as output before the clock starts.
     *  This parameter must contain a token, and it defaults to 0.
     */
    public Parameter defaultValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the state of the actor and schedule the next firing,
     *  if appropriate.Output the current value of the clock.
     *  @exception IllegalActionException If output can not send value.
     */
    public void fire() throws IllegalActionException {
        // Use the strategy pattern here so that derived classes can
        // override how this is done.
        _updateTentativeValues();

        // In initialize() method, we already ensured that the director is
        // a CTDirector.
        HSDirector director = (HSDirector) getDirector();

        if (director.isDiscretePhase()) {
            // Get the current time and period.
            Time currentTime = director.getModelTime();
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();

            // Use Time.NEGATIVE_INFINITY to indicate that no refire
            // event should be scheduled because we aren't at a phase boundary.
            _tentativeNextFiringTime = Time.NEGATIVE_INFINITY;

            // By default, the cycle count will not be incremented.
            _tentativeCycleCountIncrement = 0;

            // In case current time has reached or crossed a boundary between
            // periods, update it.  Note that normally the time will not
            // advance more than one period
            // (unless, perhaps, the entire domain has been dormant
            // for some time, as might happen for example in a hybrid system).
            // But do not do this if we are before the first iteration.
            // FIXME: why?
            if (_tentativeCycleCount > 0) {
                while (_tentativeCycleStartTime.add(periodValue).compareTo(
                        currentTime) <= 0) {
                    _tentativeCycleStartTime = _tentativeCycleStartTime
                            .add(periodValue);
                }
            }

            // Adjust the phase if the current time has moved beyond
            // the current phase time.
            Time currentPhaseTime = _tentativeCycleStartTime
                    .add(_offsets[_tentativePhase]);

            if (currentTime.compareTo(currentPhaseTime) == 0) {
                // Phase boundary.  Change the current value.
                _tentativeCurrentValue = _getValue(_tentativePhase);

                // If we are beyond the number of cycles requested, then
                // change the output value to zero.
                int cycleLimit = ((IntToken) numberOfCycles.getToken())
                        .intValue();

                // FIXME: performance suffers from this. cache the stop time.
                // NOTE: there are two stop time. One is based on the cycles
                // and period, and the other one is based on the stopTime
                // parameter.
                Time stopTime = _tentativeStartTime.add(cycleLimit
                        * periodValue);

                if (((cycleLimit > 0) && (currentTime.compareTo(stopTime) >= 0))
                        || _tentativeDone) {
                    _tentativeCurrentValue = defaultValue.getToken();
                }

                // Increment to the next phase.
                _tentativePhase++;

                if (_tentativePhase >= _offsets.length) {
                    _tentativePhase = 0;

                    // Schedule the first firing in the next period.
                    _tentativeCycleStartTime = _tentativeCycleStartTime
                            .add(periodValue);

                    // Indicate that the cycle count should increase.
                    _tentativeCycleCountIncrement++;
                }

                // Schedule the next firing in this period.
                // NOTE: In the TM domain, this may not occur if we have
                // missed a deadline.  As a consequence, the clock will stop.
                _tentativeNextFiringTime = _tentativeCycleStartTime
                        .add(_offsets[_tentativePhase]);

                if (_debugging) {
                    _debug("next firing is at " + _tentativeNextFiringTime);
                }


                if (_offsets[_tentativePhase] >= periodValue) {
                    throw new IllegalActionException(this, "Offset number "
                            + _tentativePhase + " with value "
                            + _offsets[_tentativePhase]
                            + " must be strictly less than the "
                            + "period, which is " + periodValue);
                }
            } else if (currentTime.compareTo(currentPhaseTime) < 0) {
                _tentativeNextFiringTime = currentPhaseTime;
            }

            _updateStates();
        }

        output.send(0, _currentValue);

        if (_debugging) {
            _debug("Output: " + _currentValue + " at "
                    + getDirector().getModelTime() + ".");
        }
    }

    /** Return true if this actor is scheduled to fire at the current time.
     *  @return True if this actor is scheduled to fire at the current time.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public boolean hasCurrentEvent() {
        Time currentPhaseTime = 
            _tentativeCycleStartTime.add(_offsets[_tentativePhase]);
        HSDirector director = (HSDirector) getDirector();
        boolean result = 
            director.getModelTime().compareTo(currentPhaseTime) == 0;
        return result;
    }

    /** Schedule the first firing and initialize local variables.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // FIXME: not always the default value. 
        // We should check whether the first offset is 0.0. If so,
        // we use the first value in the output value array.
        _currentValue = defaultValue.getToken();
        _startTime = Time.POSITIVE_INFINITY;
    }

    /** Make sure the continuous clock runs inside a CT domain.
     *  @exception IllegalActionException If the director is not
     *  a CTDirector or the parent class throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getDirector() instanceof HSDirector)) {
            throw new IllegalActionException("ContinuousClock can only"
                    + " be used inside CT domain.");
        }

        super.preinitialize();
    }

    /** Return false if the current model time exceeds the model stop time.
     *  Otherwise, return true.
     *  @return false if the current model time exceeds the model stop time.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing.
     */
    public boolean postfire() throws IllegalActionException {
        boolean postfireReturns = true;

        // NOTE: If the current time is bigger than the stop time,
        // the super class of clock, the TimedSource will return false
        // at its postfire() mathod. Therefore, this method does not
        // call that postfire() method.
        // Unlike what is defined in the super.postfire, continuous clock
        // requires that the current time passes the stop time.
        // That is, not only discrete phase execution, but also the continuous
        // phase execution is performed.
        if (getDirector().getModelTime().compareTo(getModelStopTime()) > 0) {
            postfireReturns = false;
        }

        if (_debugging) {
            _debug(" --- Postfire returns " + postfireReturns + ".");
        }

        return postfireReturns;
    }
}
