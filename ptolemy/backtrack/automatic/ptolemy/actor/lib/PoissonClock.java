/* A Poisson process clock source.

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
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.Director;
import ptolemy.actor.lib.TimedSource;
import ptolemy.actor.util.Time;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
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
//// PoissonClock

/** 
 * This actor produces a signal that is piecewise constant, with transitions
 * between levels taken at times given by a Poisson process.
 * It has various uses.  Its simplest use in the DE domain
 * is to generate a sequence of events at intervals that are spaced
 * randomly, according to an exponential distribution.
 * In CT, it can be used to generate a piecewise constant waveform
 * with randomly spaced transition times.
 * In both domains, the output value can cycle through a set of values.
 * <p>
 * The mean time between events is given by the <i>meanTime</i> parameter.
 * An <i>event</i> is defined to be the transition to a new output value.
 * The default mean time is 1.0.
 * <p>
 * The <i>values</i> parameter must contain an ArrayToken, or an
 * exception will be thrown when it is set.
 * By default the elements of the array are IntTokens with values 1 and 0,
 * Thus, the default output value is always 1 or 0.
 * <p>
 * In the initialize() method and in each invocation of the fire() method,
 * the actor uses the fireAt() method of the director to request
 * the next firing.  The first firing is always at the start time, unless
 * the parameter <i>fireAtStart</i> is changed to <i>false</i>.
 * It may in addition fire at any time in response to a trigger
 * input.  On such firings, it simply repeats the most recent output
 * (or generates a new output if the time is suitable.)
 * Thus, the trigger, in effect, asks the actor what its current
 * output value is. Some directors, such as those in CT, may also fire the
 * actor at other times, without requiring a trigger input.  Again, the actor
 * simply repeats the previous output.
 * Thus, the output can be viewed as samples of the piecewise
 * constant waveform,
 * where the time of each sample is the time of the firing that
 * produced it.
 * <p>
 * The type of the output can be any token type.  This type is inferred from
 * the element type of the <i>values</i> parameter.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (yuhong)
 */
public class PoissonClock extends TimedSource implements Rollbackable {

    protected Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Set the values parameter
    // set type constraint
    /**         // Call this so that we don't have to copy its code here...

     *     ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
If true, then this actor will request a firing at the start time.
     * Otherwise, the first firing will be requested at the first random
     * time. This is a boolean-valued parameter that defaults to <i>true</i>.
     */
    public Parameter fireAtStart;

    /**     
     * The mean time between events, where the output value transitions.
     * This parameter must contain a DoubleToken.
     */
    public Parameter meanTime;

    /**     
     * The values that will be produced at the output.
     * This parameter can contain any ArrayToken, and it defaults to {1, 0}.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Get the current time and period.
    private transient     // Indicator whether we've reached the next event.
int    // In case current time has reached or crossed a boundary to the
    // next output, update it.
     ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /* Get the specified value, checking the form of the values parameter.
     */
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The following are all transient to silence a javadoc bug
    // about the @serialize tag.
    // The transient qualifier should probably be removed if this
    // class is made serializable.
    // The length of the values parameter vector.
_length;

    // The index of the current output.
    private transient int _tentativeCurrentOutputIndex;

    private transient int _currentOutputIndex;

    // The next firing time requested of the director.
    private transient Time _nextFiringTime;

    // An indicator of whether a boundary is crossed in the fire() method.
    private transient boolean _boundaryCrossed;

    /**     
     * Construct an actor with the specified container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public PoissonClock(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        meanTime = new Parameter(this, "meanTime");
        meanTime.setExpression("1.0");
        meanTime.setTypeEquals(BaseType.DOUBLE);
        values = new Parameter(this, "values");
        values.setExpression("{1, 0}");
        values.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        ArrayType valuesArrayType = (ArrayType)values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm);
        attributeChanged(values);
        fireAtStart = new Parameter(this, "fireAtStart");
        fireAtStart.setExpression("true");
        fireAtStart.setTypeEquals(BaseType.BOOLEAN);
    }

    /**     
     * If the argument is the meanTime parameter, check that it is
     * positive.
     * @exception IllegalActionException If the meanTime value is
     * not positive.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == meanTime) {
            double mean = ((DoubleToken)meanTime.getToken()).doubleValue();
            if (mean <= 0.0) {
                throw new IllegalActionException(this, "meanTime is required to be positive.  meanTime given: " + mean);
            }
        } else if (attribute == values) {
            ArrayToken val = (ArrayToken)(values.getToken());
            $ASSIGN$_length(val.length());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**     
     * Clone the actor into the specified workspace. This calls the
     * base class and then sets the parameter public members to refer
     * to the parameters of the new actor.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        PoissonClock newObject = (PoissonClock)super.clone(workspace);
        ArrayType valuesArrayType = (ArrayType)newObject.values.getType();
        InequalityTerm elementTerm = valuesArrayType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm);
        return newObject;
    }

    /**     
     * Output the current value.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        $ASSIGN$_boundaryCrossed(false);
        $ASSIGN$_tentativeCurrentOutputIndex(_currentOutputIndex);
        output.send(0, _getValue(_tentativeCurrentOutputIndex));
        if (currentTime.compareTo(_nextFiringTime) == 0) {
            $ASSIGN$SPECIAL$_tentativeCurrentOutputIndex(11, _tentativeCurrentOutputIndex);
            if (_tentativeCurrentOutputIndex >= _length) {
                $ASSIGN$_tentativeCurrentOutputIndex(0);
            }
            $ASSIGN$_boundaryCrossed(true);
        }
    }

    /**     
     * Schedule the first firing at time zero and initialize local variables.
     * @exception IllegalActionException If the fireAt() method of the
     * director throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_tentativeCurrentOutputIndex(0);
        $ASSIGN$_currentOutputIndex(0);
        Time currentTime = getDirector().getModelTime();
        $ASSIGN$_nextFiringTime(currentTime);
        if (((BooleanToken)fireAtStart.getToken()).booleanValue()) {
            getDirector().fireAt(this, currentTime);
        } else {
            double meanTimeValue = ((DoubleToken)meanTime.getToken()).doubleValue();
            double exp = -Math.log((1 - Math.random())) * meanTimeValue;
            Director director = getDirector();
            $ASSIGN$_nextFiringTime(director.getModelTime().add(exp));
            director.fireAt(this, _nextFiringTime);
        }
    }

    /**     
     * Update the state of the actor and schedule the next firing,
     * if appropriate.
     * @exception IllegalActionException If the director throws it when
     * scheduling the next firing.
     */
    public boolean postfire() throws IllegalActionException  {
        $ASSIGN$_currentOutputIndex(_tentativeCurrentOutputIndex);
        if (_boundaryCrossed) {
            double meanTimeValue = ((DoubleToken)meanTime.getToken()).doubleValue();
            double exp = -Math.log((1 - Math.random())) * meanTimeValue;
            Director director = getDirector();
            $ASSIGN$_nextFiringTime(director.getModelTime().add(exp));
            director.fireAt(this, _nextFiringTime);
        }
        return super.postfire();
    }

    private Token _getValue(int index) throws IllegalActionException  {
        ArrayToken val = (ArrayToken)(values.getToken());
        if ((val == null) || (index >= _length)) {
            throw new IllegalActionException(this, "Index out of range of the values parameter.");
        }
        return val.getElement(index);
    }

    private final int $ASSIGN$_length(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_length.add(null, _length, $CHECKPOINT.getTimestamp());
        }
        return _length = newValue;
    }

    private final int $ASSIGN$_tentativeCurrentOutputIndex(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_tentativeCurrentOutputIndex.add(null, _tentativeCurrentOutputIndex, $CHECKPOINT.getTimestamp());
        }
        return _tentativeCurrentOutputIndex = newValue;
    }

    private final int $ASSIGN$SPECIAL$_tentativeCurrentOutputIndex(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_tentativeCurrentOutputIndex.add(null, _tentativeCurrentOutputIndex, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _tentativeCurrentOutputIndex += newValue;
            case 1:
                return _tentativeCurrentOutputIndex -= newValue;
            case 2:
                return _tentativeCurrentOutputIndex *= newValue;
            case 3:
                return _tentativeCurrentOutputIndex /= newValue;
            case 4:
                return _tentativeCurrentOutputIndex &= newValue;
            case 5:
                return _tentativeCurrentOutputIndex |= newValue;
            case 6:
                return _tentativeCurrentOutputIndex ^= newValue;
            case 7:
                return _tentativeCurrentOutputIndex %= newValue;
            case 8:
                return _tentativeCurrentOutputIndex <<= newValue;
            case 9:
                return _tentativeCurrentOutputIndex >>= newValue;
            case 10:
                return _tentativeCurrentOutputIndex >>>= newValue;
            case 11:
                return _tentativeCurrentOutputIndex++;
            case 12:
                return _tentativeCurrentOutputIndex--;
            case 13:
                return ++_tentativeCurrentOutputIndex;
            case 14:
                return --_tentativeCurrentOutputIndex;
            default:
                return _tentativeCurrentOutputIndex;
        }
    }

    private final int $ASSIGN$_currentOutputIndex(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_currentOutputIndex.add(null, _currentOutputIndex, $CHECKPOINT.getTimestamp());
        }
        return _currentOutputIndex = newValue;
    }

    private final Time $ASSIGN$_nextFiringTime(Time newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextFiringTime.add(null, _nextFiringTime, $CHECKPOINT.getTimestamp());
        }
        return _nextFiringTime = newValue;
    }

    private final boolean $ASSIGN$_boundaryCrossed(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_boundaryCrossed.add(null, _boundaryCrossed, $CHECKPOINT.getTimestamp());
        }
        return _boundaryCrossed = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _length = $RECORD$_length.restore(_length, timestamp, trim);
        _tentativeCurrentOutputIndex = $RECORD$_tentativeCurrentOutputIndex.restore(_tentativeCurrentOutputIndex, timestamp, trim);
        _currentOutputIndex = $RECORD$_currentOutputIndex.restore(_currentOutputIndex, timestamp, trim);
        _nextFiringTime = (Time)$RECORD$_nextFiringTime.restore(_nextFiringTime, timestamp, trim);
        _boundaryCrossed = $RECORD$_boundaryCrossed.restore(_boundaryCrossed, timestamp, trim);
        if (timestamp <= $RECORD$$CHECKPOINT.getTopTimestamp()) {
            $CHECKPOINT = $RECORD$$CHECKPOINT.restore($CHECKPOINT, this, timestamp, trim);
            FieldRecord.popState($RECORDS);
            $RESTORE(timestamp, trim);
        }
    }

    public final Checkpoint $GET$CHECKPOINT() {
        return $CHECKPOINT;
    }

    public final Object $SET$CHECKPOINT(Checkpoint checkpoint) {
        if ($CHECKPOINT != checkpoint) {
            Checkpoint oldCheckpoint = $CHECKPOINT;
            if (checkpoint != null) {
                $RECORD$$CHECKPOINT.add($CHECKPOINT, checkpoint.getTimestamp());
                FieldRecord.pushState($RECORDS);
            }
            $CHECKPOINT = checkpoint;
            oldCheckpoint.setCheckpoint(checkpoint);
            checkpoint.addObject(this);
        }
        return this;
    }

    protected CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private FieldRecord $RECORD$_length = new FieldRecord(0);

    private FieldRecord $RECORD$_tentativeCurrentOutputIndex = new FieldRecord(0);

    private FieldRecord $RECORD$_currentOutputIndex = new FieldRecord(0);

    private FieldRecord $RECORD$_nextFiringTime = new FieldRecord(0);

    private FieldRecord $RECORD$_boundaryCrossed = new FieldRecord(0);

    private FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_length,
            $RECORD$_tentativeCurrentOutputIndex,
            $RECORD$_currentOutputIndex,
            $RECORD$_nextFiringTime,
            $RECORD$_boundaryCrossed
        };

}

