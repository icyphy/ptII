/* A clock source.

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
package ptolemy.actor.lib;

import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// Clock

/**
   This actor produces a periodic signal, a sequence of events at
   regularly spaced intervals. It can generate finite pulses by specifying
   a finite <i>numberOfCycles</i>. The numberOfCycles has a default value
   as -1, indicating infinite length of executions. If numberOfCycles is
   a positive number, once the specified number of cycles has been completed,
   then this actor returns false from the postfire method. FIXME: is this
   the desired behavior?
   <p>
   At the beginning of each time interval of length given by <i>period</i>,
   this actor initiates a sequence of output events with values given by
   <i>values</i> and offset into the period given by <i>offsets</i>.
   These parameters contain arrays, which are required to have the same length.
   The <i>offsets</i> array contains doubles, which
   must be nondecreasing and nonnegative,
   or an exception will be thrown when it is set.
   Moreover, its largest entry must be smaller than <i>period</i>
   or an exception will be thrown by the fire() method.
   <p>
   The <i>values</i> parameter by default
   contains an array of IntTokens with values 1 and 0.  The default
   <i>offsets</i> array is {0.0, 1.0}.  Thus, the default output will be
   alternating 1 and 0 with 50% duty cycle.  The default period
   is 2.0.
   <p>
   The actor uses the fireAt() method of the director to request
   firing at the beginning of each period plus each of the offsets.
   <p>
   The type of the output can be any token type. This type is inferred
   from the element type of the <i>values</i> parameter.
   <p>
   This actor is a timed source; the untimed version is Pulse.
   <p>
   There is another kind of clock called ContinuousClock, which produces
   a square wave instead of a sequence of events. The ContinuousClock
   is a special actor for continuous-time domain. One of their key differences
   is that a Clock outputs a DISCRETE signal while a ContinuousClock outputs
   a CONTINUOUS signal.

   @see ptolemy.domains.ct.lib.ContinuousClock
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

        period = new Parameter(this, "period");
        period.setExpression("2.0");
        period.setTypeEquals(BaseType.DOUBLE);

        offsets = new Parameter(this, "offsets");
        offsets.setExpression("{0.0, 1.0}");
        offsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // Call this so that we don't have to copy its code here...
        attributeChanged(offsets);

        // Set the values parameter.
        values = new Parameter(this, "values");
        values.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        values.setExpression("{1, 0}");

        // set type constraint
        ArrayType valuesArrayType = (ArrayType) values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm);

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);

        // Set the numberOfCycles parameter.
        numberOfCycles = new Parameter(this, "numberOfCycles");
        numberOfCycles.setTypeEquals(BaseType.INT);
        numberOfCycles.setExpression("-1");

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

    /** The number of cycles to produce, or -1 to specify no limit.
     *  This is an integer with default -1.
     */
    public Parameter numberOfCycles;

    /** The offsets at which the specified values will be produced.
     *  This parameter must contain an array of doubles, and it defaults
     *  to {0.0, 1.0}.
     */
    public Parameter offsets;

    /** The period of the output waveform.
     *  This parameter must contain a DoubleToken, and it defaults to 2.0.
     */
    public Parameter period;

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
        } else if (attribute == period) {
            double periodValue = ((DoubleToken) period.getToken()).doubleValue();

            if (periodValue <= 0.0) {
                throw new IllegalActionException(this,
                        "Period is required to be positive.  " + "Period given: "
                        + periodValue);
            }
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
        ArrayType valuesArrayType = (ArrayType) newObject.values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm);

        return newObject;
    }

    /** Output the current value of the clock.
     *  @exception IllegalActionException If
     *   the value in the offsets parameter is encountered that is greater
     *   than the period, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Get the current time and period.
        Time currentTime = getDirector().getModelTime();
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();

        if (_debugging) {
            _debug("--- Firing at time " + currentTime + ".");
        }

        // Use the strategy pattern here so that derived classes can
        // override how this is done.
        _updateTentativeValues();

        // Use Time.NEGATIVE_INFINITY to indicate that no refire
        // event should be scheduled because we aren't at a phase boundary.
        _tentativeNextFiringTime = Time.NEGATIVE_INFINITY;

        // By default, the cycle count will not be incremented.
        _tentativeCycleCountIncrement = 0;

        // In case current time has reached or crossed a boundary between
        // periods, update it.  Note that normally it will not
        // have advanced by more than one period
        // (unless, perhaps, the entire domain has been dormant
        // for some time, as might happen for example in a hybrid system).
        // But do not do this if we are before the first iteration.
        if (_tentativeCycleCount > 0) {
            while (_tentativeCycleStartTime.add(periodValue).compareTo(currentTime) < 0) {
                _tentativeCycleStartTime = _tentativeCycleStartTime.add(periodValue);
            }

            Time currentPhaseTime = _tentativeCycleStartTime.add(_offsets[_tentativePhase]);

            // Adjust the phase if time has moved beyond the current phase.
            if (currentTime.compareTo(currentPhaseTime) == 0) {
                // Phase boundary.  Change the current value.
                _tentativeCurrentValue = _getValue(_tentativePhase);

                // Increment to the next phase.
                _tentativePhase++;

                if (_tentativePhase >= _offsets.length) {
                    _tentativePhase = 0;

                    // Schedule the first firing in the next period.
                    _tentativeCycleStartTime = _tentativeCycleStartTime.add(periodValue);

                    // Indicate that the cycle count should increase.
                    _tentativeCycleCountIncrement++;
                }

                if (_offsets[_tentativePhase] >= periodValue) {
                    throw new IllegalActionException(this,
                            "Offset number " + _tentativePhase + " with value "
                            + _offsets[_tentativePhase]
                            + " must be strictly less than the "
                            + "period, which is " + periodValue);
                }

                // Schedule the next firing in this period.
                // NOTE: In the TM domain, this may not occur if we have
                // missed a deadline.  As a consequence, the clock will stop.
                _tentativeNextFiringTime = _tentativeCycleStartTime.add(_offsets[_tentativePhase]);

                if (_debugging) {
                    _debug("next firing is at " + _tentativeNextFiringTime);
                }

                // If we are beyond the number of cycles requested, then
                // change the output value to zero.
                // FIXME: is this a desired behavior? why not simply stop the firing?
                // NOTE: This actor is intended to be used in DE domain. If this actor
                // serves as a source, when the cycle limit is reached, it should stop
                // producing more events and consequently the model stops.
                int cycleLimit = ((IntToken) numberOfCycles.getToken())
                    .intValue();

                if (cycleLimit > 0) {
                    Time stopTime = _tentativeStartTime.add(cycleLimit * periodValue);

                    if (currentTime.compareTo(stopTime) >= 0) {
                        _tentativeCurrentValue = _tentativeCurrentValue.zero();
                    }
                }

                // Used to use any negative number here to indicate
                // that no future firing should be scheduled.
                // Now, we leave it up to the director, unless the value
                // explicitly indicates no firing with Double.NEGATIVE_INFINITY.
                output.send(0, _tentativeCurrentValue);

                if (_debugging) {
                    _debug("Output: " + _tentativeCurrentValue + ".");
                }
            }
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

        Time timeToStart = getDirector().getModelTime();
        _cycleStartTime = timeToStart;
        _startTime = timeToStart.add(_offsets[0]);
        _currentValue = _getValue(0).zero();
        _phase = 0;
        _tentativeNextFiringTime = _startTime;

        // As in fire(), we use the strategy pattern so that derived classes
        // can do something different here.
        _initializeCycleCount();

        // Subclasseses may disable starting by setting _done to true.
        if (!_done) {
            if (_debugging) {
                _debug("Requesting firing at time " + _startTime);
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
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>offsets</i> parameters do not have the same length.
     */
    public boolean prefire() throws IllegalActionException {
        // FIXME: why put the check here???
        ArrayToken val = (ArrayToken) (values.getToken());

        if (_offsets.length != val.length()) {
            throw new IllegalActionException(this,
                    "Values and offsets vectors do not have the same length.");
        }

        return super.prefire();
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
        _tentativeCurrentValue = _currentValue;
        _tentativeCycleCount = _cycleCount;
        _tentativeCycleStartTime = _cycleStartTime;
        _tentativeDone = _done;
        _tentativePhase = _phase;
        _tentativeStartTime = _startTime;
    }

    /** Update the states and request refiring if necessary.
     *  @exception IllegalActionException If the numberOfCycles parameter does
     *  not contain a valid parameter or can not request refiring.
     */
    protected void _updateStates() throws IllegalActionException {
        _cycleStartTime = _tentativeCycleStartTime;
        _currentValue = _tentativeCurrentValue;
        _phase = _tentativePhase;
        _cycleCount = _tentativeCycleCount;
        _startTime = _tentativeStartTime;
        _done = _tentativeDone;

        _cycleCount += _tentativeCycleCountIncrement;

        if (_debugging) {
            _debug("Phase for next iteration: " + _phase);
        }

        int cycleLimit = ((IntToken) numberOfCycles.getToken()).intValue();

        // Used to use any negative number here to indicate
        // that no future firing should be scheduled.
        // Now, we leave it up to the director, unless the value
        // explicitly indicates no firing with Double.NEGATIVE_INFINITY.
        if (!_done
                && (_tentativeNextFiringTime.compareTo(
                            Time.NEGATIVE_INFINITY) != 0)) {
            getDirector().fireAt(this, _tentativeNextFiringTime);

            if (_debugging) {
                _debug("Requesting firing at: " + _tentativeNextFiringTime
                        + ".");
            }
        }

        // This should be computed after the above so that a firing
        // gets requested for the tail end of the output pulses.
        _done = _done
            || ((cycleLimit > 0) && (_cycleCount > cycleLimit)
                    && (_phase == 0));

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

    /** The current value of the clock output. */
    protected transient Token _currentValue;

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

    /** The time at which output starts. */
    protected transient Time _startTime;

    // Following variables recall data from the fire to the postfire method.

    /** The tentative current value of the clock output. */
    protected transient Token _tentativeCurrentValue;

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

    /** The tentative phase of the next otuput. */
    protected transient int _tentativePhase;

    /** The tentative start time for the clock to produce output. */
    protected transient Time _tentativeStartTime;
}
