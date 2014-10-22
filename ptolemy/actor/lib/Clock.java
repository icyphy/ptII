/* A clock source.

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

///////////////////////////////////////////////////////////////////
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
 short if the new period is shorter so that its time matches
 the new period. But it will only be cut short if current
 time has not passed the cycle start time plus the new period.
 Otherwise, the period in progress will run to completion.
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
     *   nondecreasing and nonnegative, or if the director will not
     *   respect the fireAt() call.
     */
    @Override
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
            if (_debugging) {
                _debug("Setting period to " + periodValue);
            }

            if (periodValue <= 0.0) {
                throw new IllegalActionException(this,
                        "Period is required to be positive.  "
                                + "Period given: " + periodValue);
            }
            // Schedule the next firing if we are running.
            if (_initialized) {
                // If this model has been dormant (e.g. in a ModalModel)
                // then it needs to catch up.
                _catchUp();
                // The _tentativeNextOutputTime may already
                // be in the future beyond the point where we want it
                // with the new period. Seems kind of tricky to get the
                // right value. Only if the _phase is zero is this an
                // issue, since in that case, the cycleStartTime has
                // been updated to the start of the new cycle, which
                // is too far in the future.
                if (_phase == 0 && _firstOutputProduced) {
                    Time potentialNextOutputTime = _tentativeCycleStartTime
                            .subtract(_previousPeriod).add(periodValue);
                    if (potentialNextOutputTime.compareTo(getDirector()
                            .getModelTime()) >= 0) {
                        _tentativeNextOutputTime = potentialNextOutputTime;
                        _tentativeCycleStartTime = potentialNextOutputTime;
                        // If this occurs outside fire(), e.g. in a modal
                        // model state transition, we also need to set the _cycleStartTime
                        // and _nextOutputTime.
                        if (!_tentative) {
                            _nextOutputTime = _tentativeNextOutputTime;
                            _cycleStartTime = _tentativeCycleStartTime;
                        }
                    }
                }
                _fireAt(_tentativeNextOutputTime);
            }
            _previousPeriod = periodValue;
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Clock newObject = (Clock) super.clone(workspace);

        try {
            ArrayToken offsetsValue = (ArrayToken) offsets.getToken();
            newObject._offsets = new double[offsetsValue.length()];
            System.arraycopy(_offsets, 0, newObject._offsets, 0,
                    _offsets.length);
        } catch (IllegalActionException ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }
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
    @Override
    public void fire() throws IllegalActionException {
        // Cannot call super.fire() because it consumes
        // trigger inputs.
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Called fire() at time " + currentTime);
        }

        // Use the strategy pattern here so that derived classes can
        // override how this is done.
        _updateTentativeValues();

        // This must be after the above update because it may trigger
        // a call to attributeChanged(), which uses the tentative values.
        // Moreover, we should set a flag so that if attributeChanged()
        // is called, then it is notified that the change is tentative.
        // It is tentative because the input may be tentative.
        // We should not commit any state changes in fire().
        try {
            _tentative = true;
            period.update();
        } finally {
            _tentative = false;
        }

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

        if (_enabled) {
            _catchUp();
            // Produce an output only if we exactly match a phase time
            // and, if the trigger input is connected, we have been triggered.
            // Also make sure that if the phase is the same as the previous phase,
            // then time has incremented.
            if (_isTimeForOutput()) {
                if (!triggerConnected || _tentativeTriggered) {
                    output.send(0, _getValue(_tentativePhase));
                }
                // Even if we skip the output because of the lack
                // of a trigger, we need to act as if we produced an
                // output for the purposes of scheduling the next event.
                _outputProduced = true;
            }
        }
    }

    /** Schedule the first firing and initialize local variables.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it, or if the director will not
     *   respect the fireAt() call.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Start cycles at the current time.
        // This is important in modal models that reinitialize the actor.
        _cycleStartTime = getDirector().getModelTime();
        _tentativeCycleStartTime = _cycleStartTime;
        _cycleCount = 0;
        _phase = 0;
        _tentativePhase = _phase;
        _nextOutputTime = _cycleStartTime.add(_offsets[_phase]);
        _tentativeNextOutputTime = _nextOutputTime;

        // Make sure the first output is enabled.
        _firstOutputProduced = false;
        _outputProduced = false;
        _enabled = true;
        _tentativeEnabled = _enabled;
        _previousPeriod = ((DoubleToken) period.getToken()).doubleValue();

        // Enable without a trigger input on the first firing.
        _triggered = true;
        _tentativeTriggered = _triggered;

        if (_debugging) {
            _debug("Requesting firing at time " + _nextOutputTime);
        }
        _fireAt(_nextOutputTime);

        _initialized = true;
    }

    /** Update the state of the actor and schedule the next firing,
     *  if appropriate.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_debugging) {
            _debug("Postfiring at " + getDirector().getModelTime());
        }
        _updateStates();

        if (_outputProduced) {
            _firstOutputProduced = true;
        }
        return super.postfire();
    }

    /** Check that the length of the <i>values</i> and
     *  <i>offsets</i> parameters are the same.
     *  @return True.
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>offsets</i> parameters do not have the same length.
     */
    @Override
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
        ArrayToken val = (ArrayToken) values.getToken();
        if (_offsets.length != val.length()) {
            throw new IllegalActionException(this,
                    "Values and offsets vectors do not have the same length.");
        }

        // Cannot call super.prefire() because it has different semantics
        // for the trigger input.
        return true;
    }

    /** Override the base class to indicate that the actor has not
     *  been initialized.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _initialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Catch up the tentative view
     *  of what the next output time should be.
     *  This sets _tentativeNextOutputTime to a value that
     *  is equal to or greater than current time, and it updates
     *  _tentativePhase and _tentativeCycleStartTime to correspond
     *  with this _tentativeNextOutputTime. If _tentativeNextOutputTime
     *  is already equal to or greater than current time, then do nothing.
     *  @exception IllegalActionException If the period is invalid.
     */
    protected void _catchUp() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        if (_tentativeNextOutputTime == null) {
            // Initialization hasn't happened yet. No catch up to do.
            return;
        }
        if (_tentativeNextOutputTime.compareTo(currentTime) >= 0) {
            return;
        }
        // Find the first cycle time and phase greater than the
        // current one that equals or exceeds current time.
        // It might not be the very next phase because we could
        // have been disabled in a modal model, or we could have
        // skipped cycles due to not being triggered.
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();
        Time phaseStartTime = _tentativeCycleStartTime
                .add(_offsets[_tentativePhase]);
        while (phaseStartTime.compareTo(currentTime) < 0) {
            _tentativePhase++;
            if (_tentativePhase >= _offsets.length) {
                _tentativePhase = 0;
                _tentativeCycleStartTime = _tentativeCycleStartTime
                        .add(periodValue);
            }
            phaseStartTime = _tentativeCycleStartTime
                    .add(_offsets[_tentativePhase]);
        }
        _tentativeNextOutputTime = phaseStartTime;
    }

    /** Get the specified output value, checking the form of the values
     *  parameter.
     *  @param index The index of the output values.
     *  @return A token that contains the output value.
     *  @exception IllegalActionException If the index is out of the range of
     *  the values parameter.
     */
    protected Token _getValue(int index) throws IllegalActionException {
        ArrayToken val = (ArrayToken) values.getToken();

        if (val == null || val.length() <= index) {
            throw new IllegalActionException(this,
                    "Index out of range of the values parameter.");
        }

        return val.getElement(index);
    }

    /** Return true if the current time is the right time for an output.
     *  @return True if the current time matches the _nextOutputTime.
     *  @exception IllegalActionException If the time is not right an
     *   a refiring cannot be requested.
     */
    protected boolean _isTimeForOutput() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        return _tentativeNextOutputTime.equals(currentTime);
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
        _outputProduced = false;
        _tentativeCycleStartTime = _cycleStartTime;
        _tentativeEnabled = _enabled;
        _tentativeNextOutputTime = _nextOutputTime;
        _tentativePhase = _phase;
        _tentativeTriggered = _triggered;
    }

    /** Update the states and request refiring if necessary.
     *  @exception IllegalActionException If the numberOfCycles parameter does
     *  not contain a valid parameter or can not request refiring, or if the director will not
     *   respect the fireAt() call..
     */
    protected void _updateStates() throws IllegalActionException {
        // Schedule another firing if we are enabled
        // and either an output was produced
        // or a trigger input was received.
        boolean triggerConnected = trigger.numberOfSources() > 0;
        boolean fireAtNeeded = _tentativeEnabled
                && (!triggerConnected && _outputProduced || triggerConnected
                        && _tentativeTriggered && !_outputProduced);
        _cycleStartTime = _tentativeCycleStartTime;
        _phase = _tentativePhase;
        if (_outputProduced) {
            _phase++;
            if (_phase == _offsets.length) {
                double periodValue = ((DoubleToken) period.getToken())
                        .doubleValue();
                _cycleStartTime = _cycleStartTime.add(periodValue);
                // Make the tentative value match, in case attributeChanged()
                // is called before the next firing.
                _tentativeCycleStartTime = _cycleStartTime;
                _cycleCount++;
                _phase = 0;
            }
            _tentativeTriggered = false;
        }
        _triggered = _tentativeTriggered;
        _enabled = _tentativeEnabled;
        _nextOutputTime = _cycleStartTime.add(_offsets[_phase]);

        if (fireAtNeeded) {
            if (_debugging) {
                _debug("Requesting firing at: " + _nextOutputTime + ".");
            }
            _fireAt(_nextOutputTime);
        }

        // This should be computed after the above so that a firing
        // gets requested for the tail end of the output pulses.
        int cycleLimit = ((IntToken) numberOfCycles.getToken()).intValue();
        _enabled = _enabled && (cycleLimit <= 0 || _cycleCount <= cycleLimit);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The count of cycles executed so far, or 0 before the start. */
    protected transient int _cycleCount;

    /** The most recent cycle start time. */
    protected transient Time _cycleStartTime;

    /** Indicator of whether the specified number of cycles have
     *  been completed. Also used in derived classes to turn on
     *  and off the clock.
     */
    protected transient boolean _enabled;

    /** Indicator of whether the first output has been produced. */
    protected transient boolean _firstOutputProduced = false;

    /** The time for the next output. */
    protected transient Time _nextOutputTime;

    /** Cache of offsets array value. */
    protected transient double[] _offsets;

    /** Indicator of whether an output was produced in this iteration. */
    protected transient boolean _outputProduced = false;

    /** The phase of the next output. */
    protected transient int _phase;

    /** The tentative time for the next output. */
    protected transient Time _tentativeNextOutputTime;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** True if the actor has been initialized. */
    private transient boolean _initialized;

    /** The previous value of the period. */
    private transient double _previousPeriod;

    // Following variables recall data from the fire to the postfire method.

    /** The tentative start time of the most recent cycle. */
    private transient Time _tentativeCycleStartTime;

    /** Flag indicating that an update to period is occurring
     *  in the fire() method.
     */
    private transient boolean _tentative = false;

    /** The indicator of whether the specified number of cycles
     *  have been completed. */
    private transient boolean _tentativeEnabled;

    /** The tentative phase of the next output. */
    private transient int _tentativePhase;

    /** Tentative indicator of triggered state. */
    private transient boolean _tentativeTriggered;

    /** Indicator of whether trigger inputs have arrived
     *  since the last output.
     */
    private transient boolean _triggered;
}
