/* A pulse source.

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
//// Pulse
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.lib.SequenceSource;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
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

/**
 * Produce a pulse with a shape specified by the parameters.
 * The <i>values</i> parameter contains an ArrayToken, which specifies
 * the sequence of values to produce at the output.  The <i>indexes</i>
 * parameter contains an array of integers, which specifies when those values
 * should be produced.  The array in the <i>indexes</i> parameter
 * must have the same length as that in the
 * <i>values</i> parameter or an exception will be thrown by the fire() method.
 * Also, the <i>indexes</i> array must be increasing and non-negative,
 * or an exception will be thrown when it is set.
 * <p>
 * Eventually, this actor will support various kinds of interpolation.
 * For now, it outputs a zero (of the same type as the values) whenever
 * the iteration count does not match an index in <i>indexes</i>.
 * <p>
 * The default for the <i>values</i> parameter is
 * an integer vector of form {1, 0}.
 * The default indexes array is {0, 1}.
 * Thus, the default output sequence will be 1, 0, 0, ...
 * <p>
 * However, the Pulse actor has a <I>repeat</i> parameter. When set to
 * true, the defined sequence is repeated indefinitely. Otherwise, the
 * default sequence of zero values result.
 * <p>
 * The type of the output can be any token type. This type is inferred
 * from the element type of the <i>values</i> parameter.
 * <p>The Ptolemy Expression language has several constructs that are
 * useful for creating arrays for use as values or indexes:
 * <dl>
 * <dt><code>[0:1:100].toArray()</code>
 * <dd>Matlab style array construction that creates an array of 100 elements,
 * 0 through 99.
 * <dt><code>repeat(100, {1}(0))</code>
 * <dd>Creat a sequence of one hundred 1's.
 * </dl>
 * <p>
 * NOTE: A reset input for this actor would be useful.  This would reset
 * the iterations count, to cause the pulse to emerge again.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 0.2
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (cxh)
 */
public class Pulse extends SequenceSource implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Call this so that we don't have to copy its code here...
    // set values parameter
    // Set the Repeat Flag.
    // set type constraint
    // Call this so that we don't have to copy its code here...
    // Show the firingCountLimit parameter last.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The indexes at which the specified values will be produced.
     * This parameter is an array of integers, with default value {0, 1}.
     */
    public Parameter indexes;

    /**
     * The flag that indicates whether the pulse sequence needs to be
     * repeated. This is a boolean, and defaults to false.
     */
    public Parameter repeat;

    /**
     * The values that will be produced at the specified indexes.
     * This parameter is an array, with default value {1, 0}.
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Check nondecreasing property.
    // Got a match with an index.
    // Repeat the pulse sequence again.
    // We stop incrementing after reaching the top of the indexes
    // vector to avoid possibility of overflow.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Count of the iterations.  This stops incrementing when
    // we exceed the top of the indexes vector.
    // Index of the next output in the values array.
    // Cache of indexes array value.
    // Zero token of the same type as in the values array.
    // Indicator of whether the iterations count matches one of the indexes.
    // Flag to indicate whether or not to repeat the sequence.
    private int _iterationCount = 0;

    private int _indexColCount = 0;

    private transient int[] _indexes;

    private Token _zero;

    private boolean _match = false;

    private boolean _repeatFlag;

    /**
     * Construct an actor with the specified container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Pulse(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        indexes = new Parameter(this, "indexes");
        indexes.setExpression("{0, 1}");
        indexes.setTypeEquals(new ArrayType(BaseType.INT));
        attributeChanged(indexes);
        values = new Parameter(this, "values");
        values.setExpression("{1, 0}");
        repeat = new Parameter(this, "repeat", new BooleanToken(false));
        repeat.setTypeEquals(BaseType.BOOLEAN);
        attributeChanged(repeat);
        output.setTypeAtLeast(ArrayType.elementType(values));
        attributeChanged(values);
        firingCountLimit.moveToLast();
    }

    /**
     * If the attribute being changed is <i>indexes</i>, then check
     * that it is increasing and nonnegative.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If the indexes vector is not
     * increasing and nonnegative, or the indexes is not a row vector.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == indexes) {
            ArrayToken indexesValue = (ArrayToken)indexes.getToken();
            $ASSIGN$_indexes(new int[indexesValue.length()]);
            int previous = 0;
            for (int i = 0; i < indexesValue.length(); i++) {
                $ASSIGN$_indexes(i, ((IntToken)indexesValue.getElement(i)).intValue());
                if (_indexes[i] < previous) {
                    throw new IllegalActionException(this, "Value of indexes is not nondecreasing " + "and nonnegative.");
                }
                previous = _indexes[i];
            }
        } else if (attribute == values) {
            try {
                ArrayToken valuesArray = (ArrayToken)values.getToken();
                Token prototype = valuesArray.getElement(0);
                $ASSIGN$_zero(prototype.zero());
            } catch (ArrayIndexOutOfBoundsException ex) {
                throw new IllegalActionException(this, "Cannot set values to an empty array.");
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this, "Cannot set values to something that is not an array: " + values.getToken());
            }
        } else if (attribute == repeat) {
            $ASSIGN$_repeatFlag(((BooleanToken)repeat.getToken()).booleanValue());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Clone the actor into the specified workspace. This overrides the
     * base class to handle type constraints.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        Pulse newObject = (Pulse)super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType.elementType(newObject.values));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        newObject.$ASSIGN$_indexes(new int[_indexes.length]);
        System.arraycopy($BACKUP$_indexes(), 0, newObject.$BACKUP$_indexes(), 0, _indexes.length);
        try {
            ArrayToken valuesArray = (ArrayToken)newObject.values.getToken();
            Token prototype = valuesArray.getElement(0);
            newObject.$ASSIGN$_zero(prototype.zero());
        } catch (Exception ex) {
            throw new InternalErrorException(ex);
        }
        return newObject;
    }

    /**
     * Output a value if the count of iterations matches one of the entries
     * in the indexes array.
     * Otherwise output a zero token with the same type as the values in
     * the value array.
     * @exception IllegalActionException If the values and indexes parameters
     * do not have the same length, or if there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        int currentIndex = 0;
        ArrayToken val = (ArrayToken)values.getToken();
        if (_indexColCount < _indexes.length) {
            if (val.length() != _indexes.length) {
                throw new IllegalActionException(this, "Parameters values and indexes have " + "different lengths.  Length of values = " + val.length()+". Length of indexes = "+_indexes.length+".");
            }
            currentIndex = _indexes[_indexColCount];
            if (_iterationCount == currentIndex) {
                output.send(0, val.getElement(_indexColCount));
                $ASSIGN$_match(true);
                return;
            }
        } else {
            if (_repeatFlag) {
                $ASSIGN$_iterationCount(0);
                $ASSIGN$_indexColCount(0);
                currentIndex = _indexes[_indexColCount];
                if (_iterationCount == currentIndex) {
                    output.send(0, val.getElement(_indexColCount));
                    $ASSIGN$_match(true);
                }
                return;
            }
        }
        output.send(0, _zero);
        $ASSIGN$_match(false);
    }

    /**
     * Set the iteration count to zero.
     * @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_iterationCount(0);
        $ASSIGN$_indexColCount(0);
    }

    /**
     * Update the iteration counters until they exceed the values
     * in the indexes array.
     * @exception IllegalActionException If the expression of indexes
     * is not valid.
     */
    public boolean postfire() throws IllegalActionException  {
        if (_iterationCount <= _indexes[_indexes.length - 1]) {
            $ASSIGN$SPECIAL$_iterationCount(13, _iterationCount);
        }
        if (_match) {
            $ASSIGN$SPECIAL$_indexColCount(13, _indexColCount);
        }
        return super.postfire();
    }

    /**
     * Start an iteration.
     * @exception IllegalActionException If the base class throws it.
     */
    public boolean prefire() throws IllegalActionException  {
        $ASSIGN$_match(false);
        return super.prefire();
    }

    private final int $ASSIGN$_iterationCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_iterationCount.add(null, _iterationCount, $CHECKPOINT.getTimestamp());
        }
        return _iterationCount = newValue;
    }

    private final int $ASSIGN$SPECIAL$_iterationCount(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_iterationCount.add(null, _iterationCount, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _iterationCount += newValue;
            case 1:
                return _iterationCount -= newValue;
            case 2:
                return _iterationCount *= newValue;
            case 3:
                return _iterationCount /= newValue;
            case 4:
                return _iterationCount &= newValue;
            case 5:
                return _iterationCount |= newValue;
            case 6:
                return _iterationCount ^= newValue;
            case 7:
                return _iterationCount %= newValue;
            case 8:
                return _iterationCount <<= newValue;
            case 9:
                return _iterationCount >>= newValue;
            case 10:
                return _iterationCount >>>= newValue;
            case 11:
                return _iterationCount++;
            case 12:
                return _iterationCount--;
            case 13:
                return ++_iterationCount;
            case 14:
                return --_iterationCount;
            default:
                return _iterationCount;
        }
    }

    private final int $ASSIGN$_indexColCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_indexColCount.add(null, _indexColCount, $CHECKPOINT.getTimestamp());
        }
        return _indexColCount = newValue;
    }

    private final int $ASSIGN$SPECIAL$_indexColCount(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_indexColCount.add(null, _indexColCount, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _indexColCount += newValue;
            case 1:
                return _indexColCount -= newValue;
            case 2:
                return _indexColCount *= newValue;
            case 3:
                return _indexColCount /= newValue;
            case 4:
                return _indexColCount &= newValue;
            case 5:
                return _indexColCount |= newValue;
            case 6:
                return _indexColCount ^= newValue;
            case 7:
                return _indexColCount %= newValue;
            case 8:
                return _indexColCount <<= newValue;
            case 9:
                return _indexColCount >>= newValue;
            case 10:
                return _indexColCount >>>= newValue;
            case 11:
                return _indexColCount++;
            case 12:
                return _indexColCount--;
            case 13:
                return ++_indexColCount;
            case 14:
                return --_indexColCount;
            default:
                return _indexColCount;
        }
    }

    private final int[] $ASSIGN$_indexes(int[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_indexes.add(null, _indexes, $CHECKPOINT.getTimestamp());
        }
        return _indexes = newValue;
    }

    private final int $ASSIGN$_indexes(int index0, int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_indexes.add(new int[] {
                    index0
                }, _indexes[index0], $CHECKPOINT.getTimestamp());
        }
        return _indexes[index0] = newValue;
    }

    private final int[] $BACKUP$_indexes() {
        $RECORD$_indexes.backup(null, _indexes, $CHECKPOINT.getTimestamp());
        return _indexes;
    }

    private final Token $ASSIGN$_zero(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_zero.add(null, _zero, $CHECKPOINT.getTimestamp());
        }
        return _zero = newValue;
    }

    private final boolean $ASSIGN$_match(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_match.add(null, _match, $CHECKPOINT.getTimestamp());
        }
        return _match = newValue;
    }

    private final boolean $ASSIGN$_repeatFlag(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_repeatFlag.add(null, _repeatFlag, $CHECKPOINT.getTimestamp());
        }
        return _repeatFlag = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _iterationCount = $RECORD$_iterationCount.restore(_iterationCount, timestamp, trim);
        _indexColCount = $RECORD$_indexColCount.restore(_indexColCount, timestamp, trim);
        _indexes = (int[])$RECORD$_indexes.restore(_indexes, timestamp, trim);
        _zero = (Token)$RECORD$_zero.restore(_zero, timestamp, trim);
        _match = $RECORD$_match.restore(_match, timestamp, trim);
        _repeatFlag = $RECORD$_repeatFlag.restore(_repeatFlag, timestamp, trim);
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

    protected transient CheckpointRecord $RECORD$$CHECKPOINT = new CheckpointRecord();

    private transient FieldRecord $RECORD$_iterationCount = new FieldRecord(0);

    private transient FieldRecord $RECORD$_indexColCount = new FieldRecord(0);

    private transient FieldRecord $RECORD$_indexes = new FieldRecord(1);

    private transient FieldRecord $RECORD$_zero = new FieldRecord(0);

    private transient FieldRecord $RECORD$_match = new FieldRecord(0);

    private transient FieldRecord $RECORD$_repeatFlag = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_iterationCount,
            $RECORD$_indexColCount,
            $RECORD$_indexes,
            $RECORD$_zero,
            $RECORD$_match,
            $RECORD$_repeatFlag
        };

}

