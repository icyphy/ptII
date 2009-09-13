/* Generate discrete events at prespecified time instants.

 Copyright (c) 1998-2009 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DiscreteClock

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
 If any entry is greater than the <i>period</i>
 then the corresponding output will never be produced.
 To get a finite sequence of events that is not periodic,
 just set <i>period</i> to Infinity.
  <p>
 The <i>values</i> parameter by default
 contains the array {1}.  The default
 <i>offsets</i> array is {0.0}.
 The default period is 1.0.
 <p>
 The type of the output can be any token type. This type is inferred
 from the element type of the <i>values</i> parameter.
 <p>
 For example, if <i>values</i> = {1, 2, 3},
 <i>offsets</i> = {0.1, 0.2, 0.3},
 <i>period</i> = 1.0,
 and the actor is initialized at time 0.0, then
 it will produce outputs with value 1 at all times
 <i>n</i> + 0.1, outputs with value 2 at all times
 <i>n</i> + 0.2, and outputs with value 3 at all times
 <i>n</i> + 0.3.
 <p>
 If the actor becomes inactive due to being in a modal model,
 then it resumes producing the sequence as early
 as it can upon becoming active again. Specifically,
 if the actor is not fired at a time it expects to produce
an output event, then it will produce that output event at the
earliest possible time after the expected time.
That earliest possible time is the current model time
 when its fireAtSkipped() method is called by the director.
 For example, if with the above <i>values</i> and
 <i>offsets</i> the actor is inactive from time 0.15 to time 0.25,
 it will have missed the output with value 2 that should have been produced
 at time 0.2. It will produce that output value 2 at time 0.25, and resume
 as if nothing had happened (except that time will be shifted).
 Specifically, it will produce the output 3 at time 0.35, and
 will begin the next period at time 1.05. The time between events
 remains exactly as specified by <i>values</i> and
 <i>offsets</i>, except when the actor is inactive at a time
 it expected to produce an output.
 <p>
 If the director that this is used with supports superdense time
 (like DE, Continuous), then the outputs are produced at index zero normally,
 unless two or more offsets have the same value. In that case, each output
 at the same time is produced at superdense time index one greater
 than the previous output. Also, the very first output may be produced
 at a superdense index greater than zero if the director's index is
 greater than zero when this actor is initialized. This can happen,
 for example, if this clock is in a refinement in a modal model,
 and the modal model enters that mode with a reset transition.
 <p>
 If the <i>period</i> is changed at any time, either by
 providing an input or by changing the parameter, then the
 new period will take effect immediately if the new period
 is provided at the same time (including the
 microstep) that the current cycle starts,
 or after the current cycle completes otherwise.
 <p>
 This actor can generate finite sequences by specifying
 a finite <i>stopTime</i>. Upon reaching that stop time,
 postfire() returns false, and the actor does not expect to
 be invoked again.
 <p>
 Alternatively, a token at the <i>start</i> input will start the clock. A token
 at the <i>stop</i> input will stop the clock, if it is still running.
 If both <i>start</i> and <i>stop</i> are received simultaneously, then
 the clock will be stopped.
 <p>
 If the <i>trigger</i> input is connected, then an output will only
 be produced if a trigger input has been received since the last output
 or if the trigger input coincides with the time when an output should
 be produced. If a trigger input has not been received, then the
 output will be skipped, moving on the the next phase.
 The only exception is the first output, which is produced
 whether a trigger is provided or not. This is because the
 trigger input is typically useful
 in a feedback situation, where the output of the clock
 eventually results in a trigger input. If the time-stamp
 of that trigger input is less than the time between clock
 events, then the clock will behave as if there were no
 trigger input. Otherwise, it will "skip beats."
 <p>
 This actor is a timed source; the untimed version is Pulse.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class DiscreteClock extends TimedSource {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If name coincides with
     *   an entity already in the container.
     */
    public DiscreteClock(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        period = new PortParameter(this, "period");
        period.setExpression("1.0");
        period.setTypeEquals(BaseType.DOUBLE);

        offsets = new Parameter(this, "offsets");
        offsets.setExpression("{0.0}");
        offsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // Call this so that we don't have to copy its code here...
        attributeChanged(offsets);

        // Set the values parameter.
        values = new Parameter(this, "values");
        values.setExpression("{1}");

        // Set type constraint on the output.
        output.setTypeAtLeast(ArrayType.elementType(values));

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);

        start = new TypedIOPort(this, "start");
        start.setInput(true);
        new StringAttribute(start, "_cardinal").setExpression("SOUTH");
        new Parameter(start, "_showName").setExpression("true");

        stop = new TypedIOPort(this, "stop");
        stop.setInput(true);
        new StringAttribute(stop, "_cardinal").setExpression("SOUTH");
        new Parameter(stop, "_showName").setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The offsets at which the specified values will be produced.
     *  This parameter must contain an array of doubles, and it defaults
     *  to {0.0}.
     */
    public Parameter offsets;

    /** The period of the output waveform.
     *  This is a double that defaults to 1.0.
     */
    public PortParameter period;

    /** A port that, if connected, is used to specify when the clock
     *  starts. This port accepts any type. The arrival of an event
     *  is what starts the clock. Upon arrival of such an event,
     *  the clock starts as if just initialized. The clock will not
     *  start until such an event is provided, unless the port is
     *  left unconnected, in which case the actor starts immediately.
     */
    public TypedIOPort start;

    /** A port that, if connected, is used to specify when the clock
     *  stops. This port accepts any type. The arrival of an event
     *  is what stops the clock.
     */
    public TypedIOPort stop;

    /** The values that will be produced at the specified offsets.
     *  This parameter must contain an ArrayToken, and it defaults to
     *  {1}
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////

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
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DiscreteClock newObject = (DiscreteClock) super.clone(workspace);
        try {
            ArrayToken offsetsValue = (ArrayToken) offsets.getToken();
            newObject._offsets = new double[offsetsValue.length()];
            System.arraycopy(_offsets, 0, newObject._offsets, 0,
                    _offsets.length);
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.values));
        } catch (IllegalActionException ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }
        return newObject;
    }

    /** Output the current value of the clock if the clock is currently
     *  enabled and, if the trigger input is connected, a trigger has been
     *  received. This method is expected to be called only at the right
     *  time to produce the next output, since otherwise prefire() will
     *  return false.
     *  @exception IllegalActionException If
     *   the value in the offsets parameter is encountered that is greater
     *   than the period, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        // Cannot call super.fire() because it consumes
        // trigger inputs.
        if (_debugging) {
            Director director = getDirector();
            Time currentTime = director.getModelTime();
            int currentIndex = 0;
            if (director instanceof SuperdenseTimeDirector) {
                currentIndex = ((SuperdenseTimeDirector) director).getIndex();
            }
            _debug("Called fire() at time (" + currentTime + ", "
                    + currentIndex + ")");
            if (_enabled && _triggered) {
                _debug("Sending output data: " + _getValue(_phase));
            }
        }

        if (_enabled && _triggered) {
            output.send(0, _getValue(_phase));
        }
    }

    /** Notify this actor that a {@link Director#fireAt(Actor,Time)}
     *  request was skipped, and that current time has passed the
     *  requested time. A director calls this method when in a modal
     *  model it was inactive at the time of the request, and it
     *  became active again after the time of the request had
     *  expired. This method extends the currently active period
     *  by the amount of time that this actor was inactive, which
     *  is the current time minus the argument time, and requests
     *  an immediate firing to produce the next output.
     *  @param time The time of the request that was skipped.
     *  @exception IllegalActionException If skipping the request
     *   is not acceptable to the actor.
     */
    public void fireAtSkipped(Time time) throws IllegalActionException {
        // If the time skipped does not match our expected next firing time,
        // then this report is probably not intended for us. (The Continuous
        // director broadcasts these reports to all actors since it doesn't
        // keep track of which actors registered a breakpoint).
        if (!time.equals(_nextOutputTime)) {
            return;
        }
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        if (_debugging) {
            _debug("At time "
                    + currentTime
                    + " director reports skipping a requested firing at time "
                    + time);
        }
        int comparison = currentTime.compareTo(time);
        if (comparison < 0) {
            throw new IllegalActionException(this, director,
                    "Director indicates that a fireAt() request at time "
                    + time
                    + " was skipped, but current time has only advanced to "
                    + currentTime);
        } else if (comparison > 0) {
            // Current time is ahead of the skipped time.
            // If current time is also ahead of the _nextOutputTime,
            // then we need to adjust the _nextOutputTime.
            // It may not be ahead because fireAtSkipped() may
            // get called multiple times in one inactive interval.
            if (currentTime.compareTo(_nextOutputTime) > 0) {
                Time difference = currentTime.subtract(time);
                _nextOutputTime = currentTime;
                _cycleStartTime = _cycleStartTime.add(difference);
                director.fireAtCurrentTime(this);
            }
        }
        // If current time is equal to the time skipped, then
        // we can safely ignore this report from the director.
        // The director is reporting that it has passed our requested
        // superdense time index, but in such cases we go ahead
        // and produce an output, so there is no problem.
        // All we have to do is request a refiring at the current time.
        director.fireAtCurrentTime(this);
   }

    /** Override the base class to initialize the index.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it, or if the director does not
     *   agree to fire the actor at the specified time.
     */
    public synchronized void initialize() throws IllegalActionException {
        super.initialize();

        // Start cycles at the current time.
        // This is important in modal models that reinitialize the actor.
        Time currentTime = getDirector().getModelTime();
        _cycleStartTime = currentTime;
        _cycleCount = 0;
        _phase = 0;
        _nextOutputTime = _cycleStartTime.add(_offsets[_phase]);
        _nextOutputIndex = 0;
        _enabled = true;

        // Enable without a trigger input on the first firing.
        _triggered = true;

        if (_debugging) {
            _debug("In intialize, requesting firing at time " + _nextOutputTime);
        }
        // The enclosing superdense time index may have already passed zero,
        // in which case a requested firing at the current time is already in the past.
        // Adjust the _nextOutputIndex to ensure the first output is produced.
        Director director = getDirector();
        if (director instanceof SuperdenseTimeDirector) {
            if (_offsets[_phase] == 0.0) {
                _nextOutputIndex = ((SuperdenseTimeDirector) director)
                        .getIndex();
            }
        }

        if (_debugging) {
            _debug("Requesting a refiring at " + _nextOutputTime
                    + ", with index " + _nextOutputIndex);
        }
        _fireAt(_nextOutputTime);

        // If the start port is connected, then start disabled.
        if (start.isOutsideConnected()) {
            _enabled = false;
        }
    }

    /** Update the time and index of the next expected output.
     *  @return False if the specified number of cycles has been reached,
     *   and true otherwise.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing, or if an offset value exceeds the period.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        _skipToNextPhase();

        if (_debugging) {
            _debug("Postfiring. Requesting refiring at (" + _nextOutputTime
                    + ", " + _nextOutputIndex + ")");
        }

        if (trigger.numberOfSources() > 0) {
            _triggered = false;
        }
        return result;
    }

    /** Check that the length of the <i>values</i> and
     *  <i>offsets</i> parameters are the same and return true
     *  if it is time to produce an output.
     *  @return True.
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>offsets</i> parameters do not have the same length.
     */
    public boolean prefire() throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        int currentIndex = 0;
        if (director instanceof SuperdenseTimeDirector) {
            currentIndex = ((SuperdenseTimeDirector) director).getIndex();
        }
        if (_debugging) {
            _debug("Called prefire() at time (" + currentTime + ", "
                    + currentIndex + ")");
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

        // Check the start input, to see whether everything needs to
        // be reset. This must be done in prefire() because if we need
        // to reinitialize, this will affect what prefire() returns.
        if (start.isOutsideConnected()) {
            if (start.hasToken(0)) {
                if (_debugging) {
                    _debug("Received a start input.");
                }
                start.get(0);
                // Restart everything.
                initialize();
                _enabled = true;
            }
        }
        // Check stop
        if (stop.isOutsideConnected()) {
            if (stop.hasToken(0)) {
                if (_debugging) {
                    _debug("Received a stop input.");
                }
                stop.get(0);
                _enabled = false;
            }
        }

        // Update the period from the port parameter, if appropriate.
        period.update();

        // Check the trigger input, if it is connected.
        // Make sure to read all channels from the trigger input.
        if (trigger.numberOfSources() > 0) {
            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.isKnown(i) && trigger.hasToken(i)) {
                    trigger.get(i);
                    _triggered = true;
                }
            }
        }

        // See whether it is time to produce an output.
        int comparison = _nextOutputTime.compareTo(currentTime);
        if (comparison > 0) {
            // If it is too early to produce an output, return false.
            // This is safe because we have made a fireAt() call for
            // the next output time.
            if (_debugging) {
                _debug("Prefire returns false. Too early to produce output.");
            }
            return false;
        } else if (comparison == 0) {
            // It is the right time to produce an output. Check
            // the index.
            if (director instanceof SuperdenseTimeDirector) {
                if (_nextOutputIndex > currentIndex) {
                    // We have not yet reached the requisite index.
                    // Request another firing at the current time.
                    _fireAt(currentTime);
                    if (_debugging) {
                        _debug("Requesting firing at the current time. "
                                + "Prefire returns false. Index is "
                                + "too early to produce output.");
                    }
                    return false;
                }
            }
            // At this point, the time matches the next output, and either
            // the index either matches or exceeds the index for the next output,
            // or the director does not support superdense time.
            // It could exceed it if we are in a modal
            // model and our mode was not active when that time came.
            // We agree to fire, since the semantics is to produce
            // the next output as soon as possible.
            if (!_triggered) {
                if (_debugging) {
                    _debug("Prefire returns false. No trigger yet. Skipping phase.");
                }
                _skipToNextPhase();
                return false;
            }
            // Ready to fire.
            if (_debugging) {
                _debug("Prefire returns true. Time and index match next output.");
            }
            return true;
        }
        // If we get here, then current time has passed our
        // expected next firing time.  This can occur if we were
        // in a modal model.
        throw new IllegalActionException(this, getDirector(),
                "Director failed to fire this actor at the requested time "
                + _nextOutputTime
                + " and also failed to call fireAtSkipped()."
                + " Perhaps the director is incompatible with DiscreteClock?");
        // If we get here, then current time matches the next firing time,
        // but we have passed the superdense index at which we expected to fire.
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

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

    /** The index of when the output should be emitted. */
    protected transient int _nextOutputIndex;

    /** Cache of offsets array value. */
    protected transient double[] _offsets;

    /** The phase of the next output. */
    protected transient int _phase;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Skip the current firing phase and request a refiring at the
     *  time of the next one.
     *  @throws IllegalActionException If the period cannot be evaluated.
     */
    private void _skipToNextPhase() throws IllegalActionException {
        _phase++;
        if (_phase >= _offsets.length) {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();
            _phase = 0;
            _cycleStartTime = _cycleStartTime.add(periodValue);
        }
        Time nextOutputTime = _cycleStartTime.add(_offsets[_phase]);
        if (_nextOutputTime.equals(nextOutputTime)) {
            _nextOutputIndex++;
        } else {
            _nextOutputTime = nextOutputTime;
        }
        _fireAt(_nextOutputTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator of whether trigger inputs have arrived
     *  since the last output.
     */
    private transient boolean _triggered;
}
