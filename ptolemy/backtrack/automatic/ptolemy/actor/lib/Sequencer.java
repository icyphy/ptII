/* An actor to put tokens in order.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
//// Sequencer
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.backtrack.util.java.util.TreeMap;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * This actor takes a sequence of inputs tagged with a sequence number
 * and produces them on the output port in the order given by the
 * sequence number.  The sequence numbers are integers starting
 * with zero.  On each firing, this actor consumes one token
 * from the <i>input</i> port and one token from the
 * <i>sequenceNumber</i> port. If the sequence number is the
 * next one in the sequence, then the token read from the <i>input</i>
 * port is produced on the <i>output</i> port.  Otherwise,
 * it is saved until its sequence number is the next one
 * in the sequence.  If an output is produced, then it may
 * be immediately followed by tokens that were previously
 * saved, if their sequence numbers are next.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (ctsay)
 */
public class Sequencer extends Transformer implements SequenceActor, Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * Input for the sequence number. The type is int.
     */
    public TypedIOPort sequenceNumber;

    /**
     * The first number of the sequence.  This is an int that
     * defaults to 0.
     */
    public Parameter startingSequenceNumber;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Indicator that an output was produced by the fire() method.
    // Indicator of the next sequence number for the output.
    // Token consumed by fire() to be recorded in postfire().
    // The sorted pending data.
    // The sequence number of the data read in the fire() method.
    private boolean _fireProducedOutput = false;

    private int _nextSequenceNumber;

    private Token _nextToken;

    private TreeMap _pending = (TreeMap)new TreeMap().$SET$CHECKPOINT($CHECKPOINT);

    private int _sequenceNumberOfInput;

    /**
     * Construct an actor in the specified container with the specified
     * name.
     * @param container The container.
     * @param name The name of this actor within the container.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the name coincides with
     * an actor already in the container.
     */
    public Sequencer(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        sequenceNumber = new TypedIOPort(this, "sequenceNumber", true, false);
        sequenceNumber.setTypeEquals(BaseType.INT);
        startingSequenceNumber = new Parameter(this, "startingSequenceNumber");
        startingSequenceNumber.setExpression("0");
    }

    /**
     * Clone the actor into the specified workspace.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    @Override public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        Sequencer newObject = (Sequencer)super.clone(workspace);
        newObject.$ASSIGN$_pending(new TreeMap());
        return newObject;
    }

    /**
     * Read a token from the <i>sequenceNumber</i> port and from
     * the <i>input</i> port, and output the next token(s) in the
     * sequence, or none if the next token in the sequence has not
     * yet been seen.  This method will throw a NoTokenException if
     * <i>sequenceNumber</i> or <i>input</i> does not have a token.
     * @exception IllegalActionException If there is no director.
     */
    @Override public void fire() throws IllegalActionException  {
        super.fire();
        $ASSIGN$_sequenceNumberOfInput(((IntToken)sequenceNumber.get(0)).intValue());
        $ASSIGN$_nextToken(input.get(0));
        if (_sequenceNumberOfInput == _nextSequenceNumber) {
            output.send(0, _nextToken);
            $ASSIGN$_fireProducedOutput(true);
        }
    }

    /**
     * Reset current sequence number to the value given by the
     * <i>startingSequenceNumber</i> parameter.
     * @exception IllegalActionException If accessing the
     * <i>startingSequenceNumber</i> parameter causes an exception.
     */
    @Override public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_fireProducedOutput(false);
        $ASSIGN$_nextSequenceNumber(((IntToken)startingSequenceNumber.getToken()).intValue());
        _pending.clear();
    }

    /**
     * If the fire() method produced the input token then check to
     * whether any pending tokens have subsequent sequence numbers.
     * @exception IllegalActionException If there is no director.
     */
    @Override public boolean postfire() throws IllegalActionException  {
        if (_fireProducedOutput) {
            $ASSIGN$SPECIAL$_nextSequenceNumber(11, _nextSequenceNumber);
            if (_pending.size() > 0) {
                Integer nextKey = (Integer)_pending.firstKey();
                int next = nextKey.intValue();
                while (next == _nextSequenceNumber) {
                    $ASSIGN$SPECIAL$_nextSequenceNumber(11, _nextSequenceNumber);
                    Token token = (Token)_pending.remove(nextKey);
                    output.send(0, token);
                    if (_pending.size() == 0) {
                        break;
                    }
                    nextKey = (Integer)_pending.firstKey();
                    next = nextKey.intValue();
                }
            }
            $ASSIGN$_fireProducedOutput(false);
        } else {
            _pending.put(Integer.valueOf(_sequenceNumberOfInput), _nextToken);
        }
        return super.postfire();
    }

    /**
     * Return false if either the <i>input</i> port or the
     * <i>sequenceNumber</i> port lacks an input token.
     * Otherwise, return whatever the superclass returns.
     * @return False if there are not enough tokens to fire.
     * @exception IllegalActionException If there is no director.
     */
    @Override public boolean prefire() throws IllegalActionException  {
        $ASSIGN$_fireProducedOutput(false);
        if (!sequenceNumber.hasToken(0)) {
            return false;
        }
        if (!input.hasToken(0)) {
            return false;
        }
        return super.prefire();
    }

    private final boolean $ASSIGN$_fireProducedOutput(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_fireProducedOutput.add(null, _fireProducedOutput, $CHECKPOINT.getTimestamp());
        }
        return _fireProducedOutput = newValue;
    }

    private final int $ASSIGN$_nextSequenceNumber(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextSequenceNumber.add(null, _nextSequenceNumber, $CHECKPOINT.getTimestamp());
        }
        return _nextSequenceNumber = newValue;
    }

    private final int $ASSIGN$SPECIAL$_nextSequenceNumber(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextSequenceNumber.add(null, _nextSequenceNumber, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _nextSequenceNumber += newValue;
            case 1:
                return _nextSequenceNumber -= newValue;
            case 2:
                return _nextSequenceNumber *= newValue;
            case 3:
                return _nextSequenceNumber /= newValue;
            case 4:
                return _nextSequenceNumber &= newValue;
            case 5:
                return _nextSequenceNumber |= newValue;
            case 6:
                return _nextSequenceNumber ^= newValue;
            case 7:
                return _nextSequenceNumber %= newValue;
            case 8:
                return _nextSequenceNumber <<= newValue;
            case 9:
                return _nextSequenceNumber >>= newValue;
            case 10:
                return _nextSequenceNumber >>>= newValue;
            case 11:
                return _nextSequenceNumber++;
            case 12:
                return _nextSequenceNumber--;
            case 13:
                return ++_nextSequenceNumber;
            case 14:
                return --_nextSequenceNumber;
            default:
                return _nextSequenceNumber;
        }
    }

    private final Token $ASSIGN$_nextToken(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_nextToken.add(null, _nextToken, $CHECKPOINT.getTimestamp());
        }
        return _nextToken = newValue;
    }

    private final TreeMap $ASSIGN$_pending(TreeMap newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_pending.add(null, _pending, $CHECKPOINT.getTimestamp());
        }
        if (newValue != null && $CHECKPOINT != newValue.$GET$CHECKPOINT()) {
            newValue.$SET$CHECKPOINT($CHECKPOINT);
        }
        return _pending = newValue;
    }

    private final int $ASSIGN$_sequenceNumberOfInput(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_sequenceNumberOfInput.add(null, _sequenceNumberOfInput, $CHECKPOINT.getTimestamp());
        }
        return _sequenceNumberOfInput = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _fireProducedOutput = $RECORD$_fireProducedOutput.restore(_fireProducedOutput, timestamp, trim);
        _nextSequenceNumber = $RECORD$_nextSequenceNumber.restore(_nextSequenceNumber, timestamp, trim);
        _nextToken = (Token)$RECORD$_nextToken.restore(_nextToken, timestamp, trim);
        _pending = (TreeMap)$RECORD$_pending.restore(_pending, timestamp, trim);
        _sequenceNumberOfInput = $RECORD$_sequenceNumberOfInput.restore(_sequenceNumberOfInput, timestamp, trim);
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

    private transient FieldRecord $RECORD$_fireProducedOutput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_nextSequenceNumber = new FieldRecord(0);

    private transient FieldRecord $RECORD$_nextToken = new FieldRecord(0);

    private transient FieldRecord $RECORD$_pending = new FieldRecord(0);

    private transient FieldRecord $RECORD$_sequenceNumberOfInput = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_fireProducedOutput,
            $RECORD$_nextSequenceNumber,
            $RECORD$_nextToken,
            $RECORD$_pending,
            $RECORD$_sequenceNumberOfInput
        };

}

