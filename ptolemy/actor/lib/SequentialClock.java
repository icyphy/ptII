/* A clock source for sequence-capable domains.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SequentialClock

/**
 A clock source for sequence-capable domains.  This actor is considerably
 simpler than the Clock actor.  On each firing, it produces the next value
 from its <i>values</i> parameter, and schedules another firing at
 a future time determined by the <i>offsets</i> and <i>period</i> parameters.
 <p>
 This actor can be used in the DE domain
 to generate a sequence of events at regularly spaced
 intervals.  It cannot be used in CT, because CT will invoke it at times
 where it has not requested a firing, and it will inappropriately advance
 to the next output value.
 <p>
 At the beginning of each time interval of length given by <i>period</i>,
 it initiates a sequence of output events with values given by
 <i>values</i> and offset into the period given by <i>offsets</i>.
 These parameters contain arrays, which are required to have the same length.
 The <i>offsets</i> array must be nondecreasing and nonnegative,
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
 It assumes that all of its firings are in response to such
 requests.
 <p>
 The type of the output can be any token type. This type is inferred from the
 element type of the <i>values</i> parameter.
 <p>
 This actor is a timed source; the untimed version is Pulse.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (yuhong)
 @deprecated Use Clock instead.
 */
@Deprecated
public class SequentialClock extends TypedAtomicActor implements SequenceActor {
    // NOTE: This cannot extend Source, because it doesn't have a trigger
    // input.  This is too bad, since it results in a lot of duplicated
    // code with Clock.

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequentialClock(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);

        period = new Parameter(this, "period", new DoubleToken(2.0));
        period.setTypeEquals(BaseType.DOUBLE);

        offsets = new Parameter(this, "offsets");
        offsets.setExpression("{0.0, 1.0}");
        offsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // Call this so that we don't have to copy its code here...
        attributeChanged(offsets);

        // set the values parameter
        IntToken[] defaultValues = new IntToken[2];
        defaultValues[0] = new IntToken(1);
        defaultValues[1] = new IntToken(0);

        ArrayToken defaultValueToken = new ArrayToken(BaseType.INT,
                defaultValues);
        values = new Parameter(this, "values", defaultValueToken);

        // set type constraint
        output.setTypeAtLeast(ArrayType.elementType(values));

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.  The type of this port is determined by from
     *  the <i>values</i> parameter.
     */
    public TypedIOPort output = null;

    /** The offsets at which the specified values will be produced.
     *  This parameter must contain an array of doubles, and it defaults
     *  to {0.0, 1.0}.
     */
    public Parameter offsets;

    /** The period of the output waveform.
     *  This parameter must contain a DoubleToken, and defaults to 2.0.
     */
    public Parameter period;

    /** The values that will be produced at the specified offsets.
     *  This parameter must contain an ArrayToken, and defaults to {1, 0}.
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
     *   nondecreasing and nonnegative, or it is not a row vector.
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
        } else if (attribute == period) {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();

            if (periodValue <= 0.0) {
                throw new IllegalActionException(this,
                        "Period is required to be positive.  "
                                + "Period given: " + periodValue);
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SequentialClock newObject = (SequentialClock) super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.values));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Output the current value of the clock.
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>offsets</i> parameters do not have the same length, or if
     *   the value in the offsets parameter is encountered that is greater
     *   than the period, or if there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, _currentValue);
    }

    /** Schedule the first firing and initialize local variables.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the <i>values</i> parameter is not a row vector, or if the
     *   fireAt() method of the director throws it, or if the director does not
     *   agree to fire the actor at the specified time.
     */
    @Override
    public synchronized void initialize() throws IllegalActionException {
        super.initialize();

        _firstFiring = true;
        _phase = 0;

        // Note: SequentialClock getCurrentTime() calls don't have to rewritten
        // for DT because this actor is a pure source without any trigger.
        // All calls to getCurrentTime will return the global time of
        // the system.
        // Schedule the first firing.
        Time currentTime = getDirector().getModelTime();
        Time nextFiringTime = currentTime.add(_offsets[0]);

        // NOTE: This must be the last line, because it could result
        // in an immediate iteration.
        _fireAt(nextFiringTime);
    }

    /** Update the state of the actor and schedule the next firing,
     *  if appropriate.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing, or if the length of the values and
     *   offsets parameters don't match.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (!super.postfire()) {
            return false;
        }
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();

        // Set the cycle start time here rather than in initialize
        // so that we at least start out well aligned.
        if (_firstFiring) {
            _cycleStartTime = getDirector().getModelTime();
            _firstFiring = false;
        }

        // Increment to the next phase.
        _phase++;

        if (_phase >= _offsets.length) {
            _phase = 0;
            _cycleStartTime = _cycleStartTime.add(periodValue);
        }

        if (_offsets[_phase] >= periodValue) {
            throw new IllegalActionException(this, "Offset number " + _phase
                    + " with value " + _offsets[_phase]
                    + " must be less than the " + "period, which is "
                    + periodValue);
        }

        Time nextIterationTime = _cycleStartTime.add(_offsets[_phase]);
        _fireAt(nextIterationTime);

        return true;
    }

    /** Set the current value.
     *  @return True.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!super.prefire()) {
            return false;
        }
        ArrayToken val = (ArrayToken) values.getToken();

        if (val == null || val.length() <= _phase) {
            throw new IllegalActionException(this,
                    "Offsets and values parameters lengths do not match.");
        }

        _currentValue = val.getElement(_phase);
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The following are all transient because they need not be cloned.
    // Either the clone method or the initialize() method sets them.
    // The current value of the clock output.
    private transient Token _currentValue;

    // The most recent cycle start time.
    private transient Time _cycleStartTime;

    // Indicator of the first firing cycle.
    private boolean _firstFiring = true;

    // Cache of offsets array value.
    private transient double[] _offsets;

    // The phase of the next output.
    private transient int _phase;
}
