/** A clock source that only generates an output when triggered.The number of
times the specified cycle repeats is controlled  by numberOfCycles parameter.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;
import ptolemy.actor.Director;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;


//////////////////////////////////////////////////////////////////////////
//// TriggeredClock
/**
When triggered by an external trigger,
this actor produces a periodic signal, a generalized square wave
that sequences through <i>N</i> output values with arbitrary duty cycles
and period.  It has various uses.  Its simplest use in the DE domain
is to generate a sequence of events at regularly spaced
intervals.  In CT, it can be used to generate a square wave.
In both domains, however, it can also generate more intricate
waveforms that cycle through a set of values.
<p>
At the beginning of each time interval of length given by <i>period</i>,
it initiates a sequence of output events with values given by
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

The <i>numberOfCycles</i> parameter controls the number of
times the specified cycle repeats.
<p>
The actor uses the fireAt() method of the director to request
firing at the beginning of each period plus each of the offsets.

Some directors, such as those in CT, may also fire the actor at
other times, without requiring a trigger input.  This is because
that CT may compute the behavior of a system at any time.
Again, the actor simply repeats the previous output.
Thus, the output can be viewed as samples of the clock waveform,
where the time of each sample is the time of the firing that
produced it.  If the actor fires before the first offset has
been reached, then a zero token of the same type as those in
the <i>values</i> array is produced.
<p>
The clock waveform is a square wave (in the sense that transitions
between levels are discrete and the signal is piecewise constant),
with <i>N</i> levels, where <i>N</i> is the length of the <i>values</i>
parameter.  Changes between levels occur at times
<i>nP</i> + <i>o<sub>i </sub></i> where <i>n</i> is any nonnegative integer,
<i>P</i> is the period, and <i>o<sub>i </sub></i> is an entry
in the <i>offsets</i> array.
<p>
The type of the output can be any token type. This type is inferred from the
element type of the <i>values</i> parameter.
<p>
This actor is a timed source, the untimed version is Pulse.
This actor is based on the Clock actor. It differs from that actor in
two respects: 1)TriggeredClock is not automatically triggered at the start of simulation 2)As described above the action of the trigger input is completely different.

@author J.R. Armstrong
@version $Id$
*/

public class TriggeredClock extends TimedSource {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TriggeredClock(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // set up  parameter values
        period = new Parameter(this, "period", new DoubleToken(2.0));
        period.setTypeEquals(BaseType.DOUBLE);

        numberOfCycles = new Parameter(this,"numberOfCycles");
        numberOfCycles.setExpression("1");

        offsets = new Parameter(this, "offsets");
        offsets.setExpression("{0.0, 1.0}");
        offsets.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        // Call this so that we don't have to copy its code here...
        attributeChanged(offsets);

        IntToken[] defaultValues = new IntToken[2];
        defaultValues[0] = new IntToken(1);
        defaultValues[1] = new IntToken(0);
        ArrayToken defaultValueToken = new ArrayToken(defaultValues);
        values = new Parameter(this, "values", defaultValueToken);
        values.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        // set output type
        ArrayType valuesArrayType = (ArrayType)values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm);

        //set the trigger port to be a multiport
        trigger.setMultiport(false);
        trigger.setTypeEquals(BaseType.BOOLEAN);

        // Call this so that we don't have to copy its code here...
        attributeChanged(values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

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

    /** The number of cycles that the actor will produce
     */
    public Parameter numberOfCycles;


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
            ArrayToken offsetsValue = (ArrayToken)offsets.getToken();
            _offsets = new double[offsetsValue.length()];
            double previous = 0.0;
            for (int i = 0; i < offsetsValue.length(); i++) {
                _offsets[i] = ((DoubleToken)offsetsValue.getElement(i))
                    .doubleValue();
                // Check nondecreasing property.
                if (_offsets[i] < previous) {
                    throw new IllegalActionException(this,
                                                     "Value of offsets is not nondecreasing " +
                                                     "and nonnegative.");
                }
                previous = _offsets[i];
            }
        } else if (attribute == period) {
            double periodValue =
                ((DoubleToken)period.getToken()).doubleValue();
            if (periodValue <= 0.0) {
                throw new IllegalActionException(this,
                                                 "Period is required to be positive.  " +
                                                 "Period given: " + periodValue);
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
    public Object clone(Workspace workspace)
        throws CloneNotSupportedException {
        TriggeredClock newObject = (TriggeredClock)super.clone(workspace);
        ArrayType valuesArrayType = (ArrayType)newObject.values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm);

        return newObject;
    }

    /** Output the current value of the clock.
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>offsets</i> parameters do not have the same length, or if
     *   the value in the offsets parameter is encountered that is greater
     *   than the period, or if there is no director.
     */
    public void fire() throws IllegalActionException {

        // Get the current time and period.

        double periodValue = ((DoubleToken)period.getToken()).doubleValue();
        double currentTime = getDirector().getCurrentTime();

        if(!_trigger){
            if(trigger.hasToken(0)){
                _trigger  = ((BooleanToken)trigger.get(0)).booleanValue();
                if (_trigger){
                    _tentativeCycleStartTime = currentTime;
                    _tentativePhase = 0;
                    _tentativeCurrentValue = _getValue(_phase);
                    _trigger = true;
                }
            }
        } else {
            // In case time has gone backwards since the last call to fire()
            // (something that can occur within an iteration), reinitialize
            // these from the last known good state.
            System.out.println("current time"+ currentTime);
            _tentativeCycleStartTime = _cycleStartTime;
            _tentativePhase = _phase;
            _tentativeCurrentValue = _currentValue;
        }

        // In case current time has reached or crossed a boundary between
        // periods, update it.  Note that normally it will not
        // have advanced by more than one period
        // (unless, perhaps, the entire domain has been dormant
        // for some time, as might happen for example in a hybrid system).
        while (_tentativeCycleStartTime + periodValue <= currentTime) {
            _tentativeCycleStartTime += periodValue;
        }
        // Use Double.NEGATIVE_INFINITY to indicate that no refire
        // event should be scheduled because we aren't at a phase boundary.
        _tentativeNextFiringTime = Double.NEGATIVE_INFINITY;

        ArrayToken valuesVariable = (ArrayToken)(values.getToken());
        if (_offsets.length != valuesVariable.length()) {
            throw new IllegalActionException(this,
                                             "Values and offsets vectors do not have the same length.");
        }
        // Phase boundary.  Change the current value.
        _tentativeCurrentValue = _getValue(_tentativePhase);
        // Increment to the next phase.
        _tentativePhase++;
        if (_tentativePhase >= _offsets.length) {
            _tentativePhase = 0;
            // Schedule the first firing in the next period.
            _tentativeCycleStartTime += periodValue;
        }
        if(_offsets[_tentativePhase] >= periodValue) {
            throw new IllegalActionException(this,
                                             "Offset number " + _tentativePhase + " with value "
                                             + _offsets[_tentativePhase] + " must be less than the "
                                             + "period, which is " + periodValue);
        }
        // Schedule the next firing in this period.
        _tentativeNextFiringTime
            = _tentativeCycleStartTime + _offsets[_tentativePhase];
        output.send(0, _tentativeCurrentValue);
    }

    /** Initialize
     */
    public void initialize() throws IllegalActionException {
        _trigger = false;
        _cycleCount = 0;
        super.initialize();
    }


    /** Update the state of the actor and schedule the next firing,
     *  if appropriate.
     *  @exception IllegalActionException If the director throws it when
     *   scheduling the next firing.
     */
    public boolean postfire() throws IllegalActionException {
        _cycleStartTime = _tentativeCycleStartTime;
        _currentValue = _tentativeCurrentValue;
        _phase = _tentativePhase;

        // Used to use any negative number here to indicate
        // that no future firing should be scheduled.
        // Now, we leave it up to the director, unless the value
        // explicitly indicates no firing with Double.NEGATIVE_INFINITY.
        _cycleCount++;
        int cycleLimit  = ((IntToken)numberOfCycles.getToken()).intValue();
        if (_cycleCount <= cycleLimit){
            if (_tentativeNextFiringTime != Double.NEGATIVE_INFINITY) {
                getDirector().fireAt(this, _tentativeNextFiringTime);
            }
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Get the specified value, checking the form of the values parameter.
     */
    private Token _getValue(int index) throws IllegalActionException {
        ArrayToken value = (ArrayToken)(values.getToken());
        if (value == null || value.length() <= index) {
            throw new IllegalActionException(this,
                                             "Index out of range of the values parameter.");
        }
        return value.getElement(index);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The following are all transient because they need not be cloned.
    // Either the clone method or the initialize() method sets them.
    // The counter which counts the number of clock cycles.
    private transient int  _cycleCount;

    private boolean _oldTrigger;
    // The current value of the clock output.
    private transient Token _currentValue;

    // The most recent cycle start time.
    private transient double _cycleStartTime;

    // Cache of offsets array value.
    private transient double[] _offsets;

    // The phase of the next output.
    private transient int _phase;

    // The value of the trigger input
    private transient boolean  _trigger;

    // Following variables recall data from the fire to the postfire method.
    private transient Token _tentativeCurrentValue;
    private transient double _tentativeCycleStartTime;
    private transient double _tentativeNextFiringTime;
    private transient int _tentativePhase;
}
