/* A clock source.

 Copyright (c) 1998-2006 The Regents of the University of California.
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

import ptolemy.actor.Manager;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Clock

/**
 This actor produces a periodic signal, a sequence of events at
 regularly spaced intervals.
 At the beginning of each time interval of length given by <i>period</i>,
 starting from the time at which initialize() is invoked,
 this actor initiates a sequence of output events with values given by
 <i>values</i> and offset into the period given by <i>offsets</i>.
 These parameters contain arrays, which are required to have the same length.
 The <i>offsets</i> array contains doubles, which
 must be nondecreasing and nonnegative,
 or an exception will be thrown when it is set.
 If any entry is greater than or equal to the <i>period</i>
 then the corresponding output will never be produced.
 <p>
 The <i>values</i> parameter by default
 contains an array of IntTokens with values 1 and 0.  The default
 <i>offsets</i> array is {0.0, 1.0}.  Thus, the default output will be
 alternating 1 and 0 with 50% duty cycle.  The default period
 is 2.0.
 <p>
 The type of the output can be any token type. This type is inferred
 from the element type of the <i>values</i> parameter.
 <p>
 If the <i>period</i> is changed at any time, either by
 providing an input or by changing the parameter, then the
 new period will take effect as soon as possible. That is,
 if there is already a period in progress, it may be cut
 short if the new period is shorter.
 <p>
 This actor can generate finite sequences by specifying
 a finite <i>numberOfCycles</i>. The numberOfCycles has a default value
 UNBOUNDED, indicating infinite length of executions. If numberOfCycles is
 a positive number, once the specified number of cycles has been completed,
 then this actor returns false from the postfire() method, which indicates
 to the director that the actor should not be fired again.
 (A cycle is "completed" each time the last event in the <i>values</i>
 array is produced).
 <p>
 The actor can also generate a finite sequence by giving a finite
 value to the <i>stopTime</i> parameter. This gives a time rather than
 a number of cycles, and thus can be used to stop the clock in the middle
 of a cycle, unlike <i>numberOfCycles</i>.  Just like <i>numberOfCycles</i>,
 when the stop time is reached, the actor's postfire() method returns
 false.
 <p>
 If the <i>trigger</i> input is connected, then an output will only
 be produced if a input has been received since the last output.
 The trigger input has no effect on the first output. After the
 first output event, no further output event will be produced
 until a time greater than or equal to the time at which a trigger
 input is received. At that time, the output produced will have
 whatever value would have been produced if the trigger input
 was not connected. Note that this trigger is typically useful
 in a feedback situation, where the output of the clock
 eventually results in a trigger input. If the time-stamp
 of that trigger input is less than the time between clock
 events, then the clock will behave as if there were no
 trigger input. Otherwise, it will "skip beats."
 <p>
 This actor can be a bit tricky to use inside a ModalModel.
 In particular, if the actor is in a state refinement, then
 it may "skip a beat" because of the state not being the current
 state at the time of the beat. If this occurs, the clock will
 simply stop firing, and will produce no further outputs.
 To prevent this, the clock may be reinitialized
 (by setting the <i>reset</i> flag
 of a modal model transition). Alternatively, you can assign
 a value to the
 the <i>period</i> of the Clock in the <i>setActions</i>
 of the transition. This will also have the effect of
 waking up the clock, but with a subtle difference.
 If you use a <i>reset</i> transition, the clock starts
 over upon entering the destination state. If you set
 the <i>period</i> parameter instead, then the clock
 behaves as if it had been running all along (except
 that its period may get changed). Thus, in the first
 case, the output events are aligned with the time
 of the transition, while in the second case, they
 are aligned with the start time of the model execution.
 <p>
 This actor is a timed source; the untimed version is Pulse.

 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (yuhong)
 */
public class Clock extends TimedSource {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Clock(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        period = new PortParameter(this, "period");
        period.setExpression("2.0");
        period.setTypeEquals(BaseType.DOUBLE);

        offsets = new Parameter(this, "offsets");
        offsets.setExpression("{0.0, 1.0}");
        offsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // Call this so that we don't have to copy its code here...
        attributeChanged(offsets);

        // Set the values parameter.
        values = new Parameter(this, "values");
        values.setExpression("{1, 0}");

        // Set type constraint on the output.
        output.setTypeAtLeast(ArrayType.elementType(values));

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);

        // Set the numberOfCycles parameter.
        // Create a symbolic name for the default.
        Parameter unbounded = new Parameter(this, "UNBOUNDED");
        unbounded.setPersistent(false);
        unbounded.setExpression("-1");
        unbounded.setVisibility(Settable.EXPERT);
        numberOfCycles = new Parameter(this, "numberOfCycles");
        numberOfCycles.setTypeEquals(BaseType.INT);
        numberOfCycles.setExpression("UNBOUNDED");

        // Set the output signal type as DISCRETE to indicate
        // that the outputs of this actor are discrete events.
        // NOTE: ContinuousClock, a subclass of this class overrides
        // the signal type to CONTINUOUS.
        new Parameter(output, "signalType", new StringToken("DISCRETE"));

        // Set the trigger signal type as DISCRETE.
        new Parameter(trigger, "signalType", new StringToken("DISCRETE"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of cycles to produce, or UNBOUNDED to specify no limit.
     *  This is an integer with default UNBOUNDED.
     */
    public Parameter numberOfCycles;

    /** The offsets at which the specified values will be produced.
     *  This parameter must contain an array of doubles, and it defaults
     *  to {0.0, 1.0}.
     */
    public Parameter offsets;

    /** The period of the output waveform.
     *  This is a double that defaults to 2.0.
     */
    public PortParameter period;

    /** The values that will be produced at the specified offsets.
     *  This parameter must contain an ArrayToken, and it defaults to
     *  {1, 0}
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>offsets</i> parameter, check that the
     *  array is nondecreasing and has the right dimension; if the
     *  argument is <i>period</i>, check that it is positive. Other
     *  sanity checks with <i>period</i> and <i>values</i> are done in
     *  the fire() method.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the offsets array is not
     *   nondecreasing and nonnegative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == offsets) {
            ArrayToken offsetsValue = (ArrayToken) offsets.getToken();
            _offsets = new double[offsetsValue.length()];

            double previous = 0.0;

            for (int i = 0; i < offsetsValue.length(); i++) {
                _offsets[i] = ((DoubleToken) offsetsValue.getElement(i))
                        .doubleValue();

                // Check nondecreasing property.
                if (_offsets[i] < previous) {
                    throw new IllegalActionException(this,
                            "Value of offsets is not nondecreasing "
                                    + "and nonnegative.");
                }

                previous = _offsets[i];
            }
            // Re-initialize the actor if we are running.
            if (getManager() != null) {
                Manager.State state = getManager().getState();
                if (state == Manager.ITERATING || state == Manager.PAUSED) {
                    // Reinitialize.
                    initialize();
                }
            }
        } else if (attribute == period) {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();

            if (periodValue <= 0.0) {
                throw new IllegalActionException(this,
                        "Period is required to be positive.  "
                                + "Period given: " + periodValue);
            }
            // Schedule the next firing if we are running.
            if (getManager() != null) {
                Manager.State state = getManager().getState();
                if (state == Manager.ITERATING || state == Manager.PAUSED) {
                    // Need to update the cycle start time in case the
                    // clock has been dormant (e.g. in a modal model).
                    // Use the old period to do this.
                    Time currentTime = getDirector().getModelTime();
                    if (_previousPeriodValue == 0.0) {
                        _previousPeriodValue = periodValue;
                    }
                    while (_cycleStartTime.add(_previousPeriodValue).compareTo(
                            currentTime) <= 0) {
                        _cycleStartTime = _cycleStartTime.add(_previousPeriodValue);
                    }
                    // Need to also update the phase.
                    // NOTE: If two successive offsets are identical,
                    // then this will always select the first matching
                    // phase. Is this the right thing to do?
                    _phase = 0;
                    Time nextFiringTime = _cycleStartTime.add(_offsets[0]);
                    while (nextFiringTime.compareTo(currentTime) < 0 
                            && _phase < _offsets.length - 1) {
                        _phase++;
                        nextFiringTime = _cycleStartTime.add(_offsets[_phase]);
                    }
                    // May still have to add one period, if no offset is
                    // enough to get past current time.
                    if (nextFiringTime.compareTo(currentTime) < 0) {
                        nextFiringTime = _cycleStartTime.add(_previousPeriodValue);
                    }
                    getDirector().fireAt(this, nextFiringTime);
                }
            }
            _previousPeriodValue = periodValue;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Clock newObject = (Clock) super.clone(workspace);

        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.values));
        } catch (IllegalActionException e) {
            // Should have been caught before.
            throw new InternalErrorException(e);
        }

        return newObject;
    }

    /** Output the current value of the clock if the time is right
     *  and, if connected, a trigger has been received.
     *  @exception IllegalActionException If
     *   the value in the offsets parameter is encountered that is greater
     *   than the period, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        // Cannot call super.fire() because it consumes
        // trigger inputs.
        
        // Get the current time.
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Called fire() at time " + currentTime);
        }
        // Update the period.
        period.update();
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();

        // Use the strategy pattern here so that derived classes can
        // override how this is done.
        _updateTentativeValues();

        // Use Time.NEGATIVE_INFINITY to indicate that no refire
        // event should be scheduled because we aren't at a phase boundary.
        // This could happen, for example, if we get a trigger input.
        _tentativeNextFiringTime = Time.NEGATIVE_INFINITY;

        // By default, the cycle count will not be incremented.
        _tentativeCycleCountIncrement = 0;
        
        // Check the trigger input, if it is connected.
        boolean triggerConnected = false;
        if (trigger.numberOfSources() > 0) {
            triggerConnected = true;
            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.isKnown(i) && trigger.hasToken(i)) {
                    trigger.get(i);
                    _tentativeTriggered = true;
                }
            }
        }

        // A cycle count of 0 is used to indicate that we are done firing,
        // so if it's zero, do nothing.
        if (_tentativeCycleCount > 0) {
            // In case current time has reached or crossed a boundary between
            // periods, update it.  Note that normally it will not
            // have advanced by more than one period
            // (unless, perhaps, the entire domain has been dormant
            // for some time, as might happen for example in a modal model).
            // But do not do this if we are before the first iteration.
            // NOTE: This used to increment only if <, not if ==.
            // But this isn't quite right. If we are right at the end
            // of a cycle, then we are also at the beginning of the next
            // cycle (cycleStartTime + period == currentTime). So we should
            // change things so that we are at the start of the next cycle.
            // EAL 7/20/07.
            // NOTE: This is just a modulo operation. Better way to do it?
            while (_tentativeCycleStartTime.add(periodValue).compareTo(
                    currentTime) <= 0) {
                _tentativeCycleStartTime = _tentativeCycleStartTime
                        .add(periodValue);
            }

            // Next figure out what phase we are in.
            // NOTE: This could be optimized to remember the previous
            // phase and start from there, but it's probably not worth
            // the extra complexity.
            _tentativePhase = 0;
            Time phaseStartTime = _tentativeCycleStartTime.add(_offsets[0]);
            while (phaseStartTime.compareTo(currentTime) < 0 
                    && _tentativePhase < _offsets.length - 1) {
                _tentativePhase++;
                phaseStartTime = _tentativeCycleStartTime.add(_offsets[_tentativePhase]);
            }
            // The above finds the first phase that matches the current time.
            // But if successive offsets are identical, then we need to increase
            // the phase.
            if (_tentativePhase <= _phase
                    && _offsets.length > _phase + 1
                    && _offsets[_tentativePhase] == _offsets[_phase + 1]) {
                _tentativePhase = _phase + 1;
            }
            // Produce an output only if we exactly match a phase time
            // and, if the trigger input is connected, we have been triggered.
            // Also make sure that if the phase is the same as the previous phase,
            // then time has incremented.
            if (phaseStartTime.equals(currentTime)
                    && _tentativeTriggered
                    && (_tentativePhase != _phase || !phaseStartTime.equals(_previousOutputTime))) {
                output.send(0, _getValue(_tentativePhase));
                // If the phase is the last, then increment the cycle count.
                if (_tentativePhase == _offsets.length - 1) {
                    _tentativeCycleCountIncrement = 1;
                }
                if (triggerConnected) {
                    _tentativeTriggered = false;
                }
            }
            // Schedule the next firing. This must be done even if we
            // have not been triggered. The time of the next firing
            // is the cycle start time incremented by either the period
            // or the next phase offset.
            double increment = periodValue + _offsets[0];
            int phase = _tentativePhase + 1;
            if (phase < _offsets.length) {
                increment = _offsets[phase];
            }
            _tentativeNextFiringTime = _tentativeCycleStartTime.add(increment);
        }
    }

    /** Schedule the first firing and initialize local variables.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (_debugging) {
            _debug("Initializing " + getFullName() + ".");
        }

        // Start cycles at the current time.
        // This is important in modal models that reinitialize the actor.
        _cycleStartTime = getDirector().getModelTime();
        _tentativeNextFiringTime = _cycleStartTime.add(_offsets[0]);
        _previousOutputTime = Time.NEGATIVE_INFINITY;
        _previousPeriodValue = 0.0;
        
        // Make sure the first output is enabled.
        _triggered = true;
        
        // Indicate that no previous phase has been output.
        _phase = -1;

        // Initialize the _done flag and the cycle count.
        // We use the strategy pattern so that derived classes
        // can do something different here.
        _initializeCycleCount();

        // Schedule the first firing to start the clock.
        // Subclasseses may disable starting by setting _done to true
        // in their _initializeCycleCount() method.
        if (!_done) {
            if (_debugging) {
                _debug("Requesting firing at time " + _tentativeNextFiringTime);
            }

            // This should be the last line, because in threaded domains,
            // it could execute immediately.
            getDirector().fireAt(this, _tentativeNextFiringTime);
        }
    }

    /** Update the state of the actor and schedule the next firing,
     *  if appropriate.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing.
     */
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Postfiring at " + getDirector().getModelTime());
        }
        _updateStates();
        return super.postfire();
    }

    /** Check that the length of the <i>values</i> and
     *  <i>offsets</i> parameters are the same.
     *  @return True.
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>offsets</i> parameters do not have the same length.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire()");
        }
        // Check the length of the values and offsets arrays.
        // This is done here because it cannot be done in
        // attributeChanged(), since the two parameters are set
        // separately, and checking in initialize() is not really
        // sufficient, since the values of these parameters can
        // change at run time.
        ArrayToken val = (ArrayToken) (values.getToken());
        if (_offsets.length != val.length()) {
            throw new IllegalActionException(this,
                    "Values and offsets vectors do not have the same length.");
        }

        // Cannot call super.prefire() because it has different semantics
        // for the trigger input.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the specified output value, checking the form of the values
     *  parameter.
     *  @param index The index of the output values.
     *  @return A token that contains the output value.
     *  @exception IllegalActionException If the index is out of the range of
     *  the values parameter.
     */
    protected Token _getValue(int index) throws IllegalActionException {
        ArrayToken val = (ArrayToken) (values.getToken());

        if ((val == null) || (val.length() <= index)) {
            throw new IllegalActionException(this,
                    "Index out of range of the values parameter.");
        }

        return val.getElement(index);
    }

    /** Initialize the cycle count and done flag.  These are done in a
     *  protected method so that derived classes can do something different
     *  here.
     */
    protected void _initializeCycleCount() {
        _done = false;
        _cycleCount = 1;
    }

    /** Copy values committed in initialize() or in the last postfire()
     *  into the corresponding tentative variables. In effect, this loads
     *  the last known good value for these variables, which is particularly
     *  important if time has gone backwards. This is done in a
     *  protected method because derived classes may want to override
     *  it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _updateTentativeValues() throws IllegalActionException {
        _tentativeCycleCount = _cycleCount;
        _tentativeCycleStartTime = _cycleStartTime;
        _tentativeDone = _done;
        _tentativeTriggered = _triggered;
    }

    /** Update the states and request refiring if necessary.
     *  @exception IllegalActionException If the numberOfCycles parameter does
     *  not contain a valid parameter or can not request refiring.
     */
    protected void _updateStates() throws IllegalActionException {
        _cycleStartTime = _tentativeCycleStartTime;
        _triggered = _tentativeTriggered;
        _cycleCount = _tentativeCycleCount;
        _done = _tentativeDone;
        _cycleCount += _tentativeCycleCountIncrement;
        _phase = _tentativePhase;
        _previousOutputTime = getDirector().getModelTime();

        int cycleLimit = ((IntToken) numberOfCycles.getToken()).intValue();

        // Used to use any negative number here to indicate
        // that no future firing should be scheduled.
        // Now, we leave it up to the director, unless the value
        // explicitly indicates no firing with Double.NEGATIVE_INFINITY.
        if (!_done
                && (_tentativeNextFiringTime.compareTo(Time.NEGATIVE_INFINITY) != 0)) {
            getDirector().fireAt(this, _tentativeNextFiringTime);

            if (_debugging) {
                _debug("Requesting firing at: " + _tentativeNextFiringTime
                        + ".");
            }
        }

        // This should be computed after the above so that a firing
        // gets requested for the tail end of the output pulses.
        _done = _done
                || ((cycleLimit > 0) && (_cycleCount > cycleLimit));

        if (_done) {
            _cycleCount = 0;

            if (_debugging) {
                _debug("Done with requested number of cycles.");
            }
        }

        if (_debugging) {
            _debug("Cycle count for next iteration: " + _cycleCount + ".");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The count of cycles executed so far, or 0 before the start. */
    protected transient int _cycleCount;

    /** The most recent cycle start time. */
    protected transient Time _cycleStartTime;

    /** Indicator of whether the specified number of cycles have
     *  been completed.
     */
    protected transient boolean _done;

    /** Cache of offsets array value. */
    protected transient double[] _offsets;
    
    /** The phase of the next output. */
    protected transient int _phase;
    
    /** A record of the previous output time. */
    protected transient Time _previousOutputTime;
    
    /** A record of the previous value of the period. */
    protected transient double _previousPeriodValue;

    /** Indicator of whether a trigger input has arrived. */
    protected transient boolean _triggered = true;

    // Following variables recall data from the fire to the postfire method.

    /** The tentative count of cycles executed so far. */
    protected transient int _tentativeCycleCount;

    /** The tentative increment for cycle count increment. */
    protected transient int _tentativeCycleCountIncrement;

    /** The tentative start time of the most recent cycle. */
    protected transient Time _tentativeCycleStartTime;

    /** The indicator of whether the specified number of cycles
     *  have been completed. */
    protected transient boolean _tentativeDone;

    /** The tentative time for next firing. */
    protected transient Time _tentativeNextFiringTime;
    
    /** The tentative phase of the next output. */
    protected transient int _tentativePhase;

    /** The tentative flag indicating whether we've been triggered. */
    protected transient boolean _tentativeTriggered;
}
