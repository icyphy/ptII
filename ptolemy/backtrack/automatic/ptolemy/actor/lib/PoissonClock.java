/* A Poisson process clock source.

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
///////////////////////////////////////////////////////////////////
//// PoissonClock
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TimedActor;
import ptolemy.actor.util.Time;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
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

/**
 * This actor produces discrete events according to a Poisson process.
 * The time between events is given by independent and identically
 * distributed exponential random variables. The values produced
 * rotate sequentially through those given in the <i>values</i> parameter,
 * which is an array of anything and defaults to {1, 0}.
 * The type of the output can be any token type.  This type is inferred from
 * the element type of the <i>values</i> parameter.
 * The mean time between events is given by the <i>meanTime</i> parameter,
 * which defaults to 1.0.
 * <p>
 * In the initialize() method and postfire() methods,
 * the actor uses the fireAt() method of the director to request
 * the next firing.  The first firing is always at the start time, unless
 * the parameter <i>fireAtStart</i> is changed to <i>false</i>.
 * <p>
 * If the trigger input is connected, then any event on it will
 * cause the Poisson process to immediately produce the next
 * event, as if the time for that event had arrived.
 * <p>
 * If this actor is inactive at the time at which it would have
 * otherwise produced an output, then it will stop producing outputs.
 * This should not happen.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (yuhong)
 */
public class PoissonClock extends RandomSource implements TimedActor, Rollbackable {

    // Set the values parameter
    // Call this so that we don't have to copy its code here...
    // Note that this class copies much of TimedSource into here
    // because it can't subclass both TimedSource and RandomSource.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * If true, then this actor will request a firing at the start time.
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
     * The time at which postfire() should return false. This is a
     * double that defaults to Infinity, which means that postfire()
     * never returns false (or at least, doesn't do so due to stopTime
     * having been exceeded).
     */
    public Parameter stopTime;

    /**
     * The values that will be produced at the output.
     * This parameter can contain any ArrayToken, and it defaults to {1, 0}.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // NOTE: Do not throw an exception if the director ignores this
    // stop time request or returns some other value of time.
    // postfire() will return false on the next firing after time
    // equals the stop time or exceeds it.
    // If there is a trigger input, then it is time for an output.
    // It is time to produce an output if the current time equals
    // or exceeds the next firing time (it should never exceed).
    // It is too early.
    // The time matches, but the microstep is too early.
    // If this is the first call to fire() in an iteration,
    // the the superclass will generate a new random number
    // to be used in postfire().
    // The superclass will ensure that the next call to fire() generates
    // a new random number. That random number will be first used
    // in the first postfire() call.
    // NOTE: Do not throw an exception if the director ignores this
    // stop time request or returns some other value of time.
    // postfire() will return false on the next firing after time
    // equals the stop time or exceeds it.
    // Have to explicitly generate the first random number
    // becuase the superclass doesn't do it until the first
    // call to fire().
    // An output was produced in this iteration.
    // The following is not needed because the first call
    // to fire() in the superclass generated a new random
    // number.
    // _generateRandomNumber();
    // Output was not produced, but time matches, which
    // means the microstep was too early. Request a refiring.
    // Do not call super.prefire() because that returns false if
    // there are no trigger inputs.
    // If any trigger input has a token, then return true.
    // NOTE: It might seem that using trigger.numberOfSources() is
    // correct here, but it is not. It is possible for channels
    // to be connected, for example, to other output ports or
    // even back to this same trigger port, in which case higher
    // numbered channels will not have their inputs read.
    // No need to continue. fire() will
    // consume the tokens (in the superclass).
    // Also return true if there are no trigger inputs
    // but it is time for the next output.
    // Otherwise, return false.
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /* Get the specified value, checking the form of the values parameter.
     */
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Most recently generated exponential random number.
    // Flag indicating that the model is running.
    // The following are all transient to silence a javadoc bug
    // about the @serialize tag.
    // The transient qualifier should probably be removed if this
    // class is made serializable.
    // The length of the values parameter vector.
    // The next firing time requested of the director.
    // The index of the next output.
    // Indicator that an output was produced in this iteration.
    // stop time.
    private double _current;

    private boolean _executing = false;

    private transient int _length;

    private transient Time _nextFiringTime;

    private transient int _nextOutputIndex;

    private boolean _outputProduced;

    private Time _stopTime;

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
        output.setTypeAtLeast(ArrayType.elementType(values));
        attributeChanged(values);
        fireAtStart = new Parameter(this, "fireAtStart");
        fireAtStart.setExpression("true");
        fireAtStart.setTypeEquals(BaseType.BOOLEAN);
        stopTime = new Parameter(this, "stopTime");
        stopTime.setExpression("Infinity");
        stopTime.setTypeEquals(BaseType.DOUBLE);
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" "+"width=\"40\" height=\"40\" "+"style=\"fill:lightGrey\"/>\n"+"<circle cx=\"0\" cy=\"0\" r=\"17\""+"style=\"fill:white\"/>\n"+"<line x1=\"0\" y1=\"-15\" x2=\"0\" y2=\"-13\"/>\n"+"<line x1=\"0\" y1=\"14\" x2=\"0\" y2=\"16\"/>\n"+"<line x1=\"-15\" y1=\"0\" x2=\"-13\" y2=\"0\"/>\n"+"<line x1=\"14\" y1=\"0\" x2=\"16\" y2=\"0\"/>\n"+"<line x1=\"0\" y1=\"-8\" x2=\"0\" y2=\"0\"/>\n"+"<line x1=\"0\" y1=\"0\" x2=\"11.26\" y2=\"-6.5\"/>\n"+"</svg>\n");
    }

    /**
     * If the argument is the meanTime parameter, check that it is
     * positive.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If the meanTime value is
     * not positive.
     */
    @Override public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == meanTime) {
            double mean = ((DoubleToken)meanTime.getToken()).doubleValue();
            if (mean <= 0.0) {
                throw new IllegalActionException(this, "meanTime is required to be positive.  meanTime given: " + mean);
            }
        } else if (attribute == values) {
            ArrayToken val = (ArrayToken)values.getToken();
            $ASSIGN$_length(val.length());
        } else if (attribute == stopTime) {
            double newStopTimeValue = ((DoubleToken)stopTime.getToken()).doubleValue();
            if (_executing) {
                Time newStopTime = new Time(getDirector(), newStopTimeValue);
                Director director = getDirector();
                if (director != null) {
                    Time currentTime = director.getModelTime();
                    if (newStopTime.compareTo(currentTime) > 0) {
                        director.fireAt(this, newStopTime);
                    } else {
                        throw new IllegalActionException(this, "The stop time " + "is earlier than the current time.");
                    }
                }
                $ASSIGN$_stopTime(newStopTime);
            }
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
    @Override public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        PoissonClock newObject = (PoissonClock)super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType.elementType(newObject.values));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /**
     * Output the current value.
     * @exception IllegalActionException If there is no director.
     */
    @Override public void fire() throws IllegalActionException  {
        boolean triggerInputPresent = false;
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.isKnown() && trigger.hasToken(i)) {
                triggerInputPresent = true;
            }
        }
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        boolean timeForOutput = currentTime.compareTo(_nextFiringTime) >= 0;
        if (!timeForOutput && !triggerInputPresent) {
            return;
        }
        if (director instanceof SuperdenseTimeDirector) {
            int currentMicrostep = ((SuperdenseTimeDirector)director).getIndex();
            if (currentMicrostep < 1 && !triggerInputPresent) {
                return;
            }
        }
        super.fire();
        output.send(0, _getValue(_nextOutputIndex));
        $ASSIGN$_outputProduced(true);
    }

    /**
     * Get the stop time.
     * @return The stop time.
     * @deprecated As of Ptolemy II 4.1, replaced by{
@link #getModelStopTime    }

     */
    @Deprecated public double getStopTime() {
        return getModelStopTime().getDoubleValue();
    }

    /**
     * Get the stop time.
     * @return The stop time.
     */
    public Time getModelStopTime() {
        return _stopTime;
    }

    /**
     * Request the first firing either at the start time
     * or at a random time, depending on <i>fireAtStart</i>.
     * @exception IllegalActionException If the fireAt() method of the
     * director throws it, or if the director does not
     * agree to fire the actor at the specified time.
     */
    @Override public void initialize() throws IllegalActionException  {
        super.initialize();
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }
        double stopTimeValue = ((DoubleToken)stopTime.getToken()).doubleValue();
        $ASSIGN$_stopTime(new Time(getDirector(), stopTimeValue));
        Time currentTime = director.getModelTime();
        if (!_stopTime.isInfinite() && _stopTime.compareTo(currentTime) > 0) {
            director.fireAt(this, _stopTime);
            $ASSIGN$_executing(true);
        }
        $ASSIGN$_nextOutputIndex(0);
        $ASSIGN$_nextFiringTime(currentTime);
        $ASSIGN$_outputProduced(false);
        if (((BooleanToken)fireAtStart.getToken()).booleanValue()) {
            _fireAt(currentTime);
        } else {
            _generateRandomNumber();
            $ASSIGN$_nextFiringTime(director.getModelTime().add(_current));
            _fireAt(_nextFiringTime);
        }
    }

    /**
     * Generate an exponential random number and schedule the next firing.
     * @exception IllegalActionException If the director throws it when
     * scheduling the next firing, or if the director does not
     * agree to fire the actor at the specified time.
     */
    @Override public boolean postfire() throws IllegalActionException  {
        boolean result = super.postfire();
        Time currentTime = getDirector().getModelTime();
        if (_outputProduced) {
            $ASSIGN$_outputProduced(false);
            $ASSIGN$SPECIAL$_nextOutputIndex(11, _nextOutputIndex);
            if (_nextOutputIndex >= _length) {
                $ASSIGN$_nextOutputIndex(0);
            }
            $ASSIGN$_nextFiringTime(currentTime.add(_current));
            _fireAt(_nextFiringTime);
        } else if (currentTime.compareTo(_nextFiringTime) >= 0) {
            _fireAt(currentTime);
        }
        if (currentTime.compareTo(_stopTime) >= 0) {
            return false;
        }
        return result;
    }

    /**
     * If the current time matches the expected time for the next
     * output, then return true. Also return true if the
     * trigger input is connected and has events.
     * Otherwise, return false.
     * @exception IllegalActionException If there is no director.
     */
    @Override public boolean prefire() throws IllegalActionException  {
        if (_debugging) {
            _debug("Called prefire()");
        }
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.isKnown() && trigger.hasToken(i)) {
                return true;
            }
        }
        Time currentTime = getDirector().getModelTime();
        if (currentTime.compareTo(_nextFiringTime) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Override the base class to reset a flag that indicates that the
     * model is executing. This method is invoked exactly once per execution
     * of an application.  None of the other action methods should be
     * be invoked after it.
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override public void wrapup() throws IllegalActionException  {
        super.wrapup();
        $ASSIGN$_executing(false);
    }

    /**
     * Generate a new random number.
     * @exception IllegalActionException If parameter values are incorrect.
     */
    @Override protected void _generateRandomNumber() throws IllegalActionException  {
        double meanTimeValue = ((DoubleToken)meanTime.getToken()).doubleValue();
        double test = _random.nextDouble();
        $ASSIGN$_current(-Math.log(1 - test) * meanTimeValue);
    }

    private Token _getValue(int index) throws IllegalActionException  {
        ArrayToken val = (ArrayToken)values.getToken();
        if (val == null || index >= _length) {
            throw new IllegalActionException(this, "Index out of range of the values parameter.");
        }
        return val.getElement(index);
    }

    private final double $ASSIGN$_current(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_current.add(null, _current, $CHECKPOINT.getTimestamp());
        }
        return _current = newValue;
    }

    private final boolean $ASSIGN$_executing(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_executing.add(null, _executing, $CHECKPOINT.getTimestamp());
        }
        return _executing = newValue;
    }

    private final int $ASSIGN$_length(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_length.add(null, _length, $CHECKPOINT.getTimestamp());
        }
        return _length = newValue;
    }

    private final Time $ASSIGN$_nextFiringTime(Time newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextFiringTime.add(null, _nextFiringTime, $CHECKPOINT.getTimestamp());
        }
        return _nextFiringTime = newValue;
    }

    private final int $ASSIGN$_nextOutputIndex(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextOutputIndex.add(null, _nextOutputIndex, $CHECKPOINT.getTimestamp());
        }
        return _nextOutputIndex = newValue;
    }

    private final int $ASSIGN$SPECIAL$_nextOutputIndex(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextOutputIndex.add(null, _nextOutputIndex, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _nextOutputIndex += newValue;
            case 1:
                return _nextOutputIndex -= newValue;
            case 2:
                return _nextOutputIndex *= newValue;
            case 3:
                return _nextOutputIndex /= newValue;
            case 4:
                return _nextOutputIndex &= newValue;
            case 5:
                return _nextOutputIndex |= newValue;
            case 6:
                return _nextOutputIndex ^= newValue;
            case 7:
                return _nextOutputIndex %= newValue;
            case 8:
                return _nextOutputIndex <<= newValue;
            case 9:
                return _nextOutputIndex >>= newValue;
            case 10:
                return _nextOutputIndex >>>= newValue;
            case 11:
                return _nextOutputIndex++;
            case 12:
                return _nextOutputIndex--;
            case 13:
                return ++_nextOutputIndex;
            case 14:
                return --_nextOutputIndex;
            default:
                return _nextOutputIndex;
        }
    }

    private final boolean $ASSIGN$_outputProduced(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_outputProduced.add(null, _outputProduced, $CHECKPOINT.getTimestamp());
        }
        return _outputProduced = newValue;
    }

    private final Time $ASSIGN$_stopTime(Time newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_stopTime.add(null, _stopTime, $CHECKPOINT.getTimestamp());
        }
        return _stopTime = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        super.$COMMIT(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _current = $RECORD$_current.restore(_current, timestamp, trim);
        _executing = $RECORD$_executing.restore(_executing, timestamp, trim);
        _length = $RECORD$_length.restore(_length, timestamp, trim);
        _nextFiringTime = (Time)$RECORD$_nextFiringTime.restore(_nextFiringTime, timestamp, trim);
        _nextOutputIndex = $RECORD$_nextOutputIndex.restore(_nextOutputIndex, timestamp, trim);
        _outputProduced = $RECORD$_outputProduced.restore(_outputProduced, timestamp, trim);
        _stopTime = (Time)$RECORD$_stopTime.restore(_stopTime, timestamp, trim);
        super.$RESTORE(timestamp, trim);
    }

    private transient FieldRecord $RECORD$_current = new FieldRecord(0);

    private transient FieldRecord $RECORD$_executing = new FieldRecord(0);

    private transient FieldRecord $RECORD$_length = new FieldRecord(0);

    private transient FieldRecord $RECORD$_nextFiringTime = new FieldRecord(0);

    private transient FieldRecord $RECORD$_nextOutputIndex = new FieldRecord(0);

    private transient FieldRecord $RECORD$_outputProduced = new FieldRecord(0);

    private transient FieldRecord $RECORD$_stopTime = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_current,
            $RECORD$_executing,
            $RECORD$_length,
            $RECORD$_nextFiringTime,
            $RECORD$_nextOutputIndex,
            $RECORD$_outputProduced,
            $RECORD$_stopTime
        };

}

