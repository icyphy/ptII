/* Chop an input sequence and construct from it a new output sequence.

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
//// Chop
package ptolemy.backtrack.automatic.ptolemy.domains.sdf.lib;

import java.lang.Object;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This actor reads a sequence of input tokens of any type, and writes a
 * sequence of tokens constructed from the input sequence (possibly
 * supplemented with zeros).  The number of input tokens consumed
 * is given by <i>numberToRead</i>, and the number of output tokens
 * produced is given by <i>numberToWrite</i>.
 * The <i>offset</i> parameter (default 0) specifies where in the output
 * block the first (oldest) input that is read should go.
 * If <i>offset</i> is positive and <i>usePastInputs</i> is true,
 * then the first few outputs will come from values read in previous iterations.
 * <p>
 * A simple use of this actor is to pad a block of inputs with zeros.
 * Set <i>offset</i> to zero and use <i>numberToWrite &gt; numberToRead</i>.
 * <a name="zero padding"></a>
 * <a name="padding"></a></p>
 * <p>
 * Another simple use is to obtain overlapping windows from
 * an input stream.
 * Set <i>usePastInputs</i> to true, use <i>numberToWrite &gt; numberToRead</i>,
 * and set <i>offset</i> equal to <i>numberToWrite - numberToRead</i>.
 * <a name="overlapping windows"></a>
 * <a name="windowing"></a></p>
 * <p>
 * The general operation is illustrated with the following examples.
 * If <i>offset</i> is positive,
 * there two possible scenarios, illustrated by the following examples:</p>
 * <p>
 * <pre>
 * iiiiii                  numberToRead = 6
 * \    \                 offset = 2
 * ppiiiiii00              numberToWrite = 10
 * iiiiii                  numberToRead = 6
 * \ \  \                 offset = 2
 * ppiii                   numberToWrite = 5
 * </pre></p>
 * <p>
 * The symbol "i" refers to any input token. The leftmost symbol
 * refers to the oldest input token of the ones consumed in a given
 * firing. The symbol "p" refers to a token that is either zero
 * (if <i>usePastInputs</i> is false) or is equal to a previously
 * consumed input token (if <i>usePastInputs</i> is true).
 * The symbol "0" refers to a zero-valued token.
 * In the first of the above examples, the entire input block is
 * copied to the output, and then filled out with zeros.
 * In the second example, only a portion of the input block fits.
 * The remaining input tokens are discarded, although they might
 * be used in subsequent firings if <i>usePastInputs</i> is true.</p>
 * <p>
 * When the <i>offset</i> is negative, this indicates that the
 * first <i>offset</i> input tokens that are read should be
 * discarded.  The corresponding scenarios are shown below:</p>
 * <p>
 * <pre>
 * iiiiii                  numberToRead = 6
 * / /  /                   offset = -2
 * iiii000000              numberToWrite = 10
 * iiiiii                  numberToRead = 6
 * / / //                   offset = -2
 * iii                     numberToWrite = 3
 * </pre>
 * </p>
 * <p>
 * In the first of these examples, the first two input tokens are
 * discarded.  In the second example, the first two and the last input
 * token are discarded.</p>
 * <p>
 * The zero-valued tokens are constructed using the zero() method of
 * the first input token that is read in the firing.  This returns
 * a zero-valued token with the same type as the input.</p>
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Yellow (neuendor)
 */
public class Chop extends SDFTransformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The number of input tokens to read.
     * This is an integer, with default 128.
     */
    public Parameter numberToRead;

    /**
     * The number of tokens to write to the output.
     * This is an integer, with default 64.
     */
    public Parameter numberToWrite;

    /**
     * Start of output block relative to start of input block.
     * This is an integer, with default 0.
     */
    public Parameter offset;

    /**
     * If offset is greater than 0, specify whether to use previously
     * read inputs (otherwise use zeros).
     * This is a boolean, with default true.
     */
    public Parameter usePastInputs;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Note: it is important that none of these sections depend on
    // each other.
    // NOTE: The following computation gets repeated when each of
    // these gets set, but it's a simple calculation, so we live
    // with it.
    // The variables _highLimit and _lowLimit indicate the range of
    // output indexes that come directly from the input block
    // that is read.
    // Fill past buffer with zeros.
    // FIXME: This will access past samples...
    // Copy input buffer into past buffer.  Have to be careful
    // here because the buffer might be longer than the
    // input window.
    // Shift older data.
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private int _highLimit;

    private int _inputIndex;

    private int _lowLimit;

    private int _numberToRead;

    private int _numberToWrite;

    private int _offsetValue;

    private Token[] _buffer;

    private Token[] _pastBuffer;

    private boolean _usePast;

    private boolean _pastNeedsInitializing;

    /**
     * Construct an actor in the specified container with the specified
     * name.
     * @param container The container.
     * @param name The name.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the name coincides with
     * an actor already in the container.
     */
    public Chop(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        numberToRead = new Parameter(this, "numberToRead");
        numberToRead.setExpression("128");
        numberToRead.setTypeEquals(BaseType.INT);
        numberToWrite = new Parameter(this, "numberToWrite");
        numberToWrite.setExpression("64");
        numberToWrite.setTypeEquals(BaseType.INT);
        offset = new Parameter(this, "offset");
        offset.setExpression("0");
        offset.setTypeEquals(BaseType.INT);
        usePastInputs = new Parameter(this, "usePastInputs");
        usePastInputs.setExpression("true");
        usePastInputs.setTypeEquals(BaseType.BOOLEAN);
        input_tokenConsumptionRate.setExpression("numberToRead");
        output_tokenProductionRate.setExpression("numberToWrite");
    }

    /**
     * Check the validity of parameter values and using the new
     * values, recompute the size of the internal buffers.
     * @param attribute The attribute that has changed.
     * @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == numberToRead) {
            $ASSIGN$_numberToRead(((IntToken)numberToRead.getToken()).intValue());
            if (_numberToRead <= 0) {
                throw new IllegalActionException(this, "Invalid numberToRead: " + _numberToRead);
            }
        } else if (attribute == numberToWrite) {
            $ASSIGN$_numberToWrite(((IntToken)numberToWrite.getToken()).intValue());
            if (_numberToWrite <= 0) {
                throw new IllegalActionException(this, "Invalid numberToWrite: " + _numberToRead);
            }
            $ASSIGN$_buffer(new Token[_numberToWrite]);
        } else if (attribute == offset) {
            $ASSIGN$_offsetValue(((IntToken)offset.getToken()).intValue());
        } else if (attribute == usePastInputs) {
            $ASSIGN$_usePast(((BooleanToken)usePastInputs.getToken()).booleanValue());
        }
        if (attribute == offset || attribute == usePastInputs) {
            if (_offsetValue > 0) {
                $ASSIGN$_pastBuffer(new Token[_offsetValue]);
                $ASSIGN$_pastNeedsInitializing(true);
            }
        }
        if (attribute == numberToRead || attribute == numberToWrite || attribute == offset || attribute == usePastInputs) {
            $ASSIGN$_highLimit(_offsetValue + _numberToRead - 1);
            if (_highLimit >= _numberToWrite) {
                $ASSIGN$_highLimit(_numberToWrite - 1);
            }
            if (_offsetValue >= 0) {
                $ASSIGN$_lowLimit(_offsetValue);
                $ASSIGN$_inputIndex(0);
            } else {
                $ASSIGN$_lowLimit(0);
                $ASSIGN$_inputIndex(-_offsetValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Consume the specified number of input tokens, and produce
     * the specified number of output tokens.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        int inputIndex = _inputIndex;
        int pastBufferIndex = 0;
        Token[] inBuffer = input.get(0, _numberToRead);
        Token zero = inBuffer[0].zero();
        for (int i = 0; i < _numberToWrite; i++) {
            if (i > _highLimit) {
                $ASSIGN$_buffer(i, zero);
            } else if (i < _lowLimit) {
                if (_usePast) {
                    if (_pastNeedsInitializing) {
                        for (int j = 0; j < _pastBuffer.length; j++) {
                            $ASSIGN$_pastBuffer(j, zero);
                        }
                        $ASSIGN$_pastNeedsInitializing(false);
                    }
                    $ASSIGN$_buffer(i, _pastBuffer[pastBufferIndex++]);
                } else {
                    $ASSIGN$_buffer(i, zero);
                }
            } else {
                $ASSIGN$_buffer(i, inBuffer[inputIndex]);
                inputIndex++;
            }
        }
        if (_usePast && _offsetValue > 0) {
            int startCopy = _numberToRead - _offsetValue;
            int length = _pastBuffer.length;
            int destination = 0;
            if (startCopy < 0) {
                destination = _pastBuffer.length - _numberToRead;
                System.arraycopy($BACKUP$_pastBuffer(), _numberToRead, $BACKUP$_pastBuffer(), 0, destination);
                startCopy = 0;
                length = _numberToRead;
            }
            System.arraycopy(inBuffer, startCopy, $BACKUP$_pastBuffer(), destination, length);
        }
        output.send(0, $BACKUP$_buffer(), _numberToWrite);
    }

    /**
     * Override the base class to ensure that the past buffer
     * gets initialized.
     * @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_pastNeedsInitializing(true);
    }

    private final int $ASSIGN$_highLimit(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_highLimit.add(null, _highLimit, $CHECKPOINT.getTimestamp());
        }
        return _highLimit = newValue;
    }

    private final int $ASSIGN$_inputIndex(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_inputIndex.add(null, _inputIndex, $CHECKPOINT.getTimestamp());
        }
        return _inputIndex = newValue;
    }

    private final int $ASSIGN$_lowLimit(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_lowLimit.add(null, _lowLimit, $CHECKPOINT.getTimestamp());
        }
        return _lowLimit = newValue;
    }

    private final int $ASSIGN$_numberToRead(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_numberToRead.add(null, _numberToRead, $CHECKPOINT.getTimestamp());
        }
        return _numberToRead = newValue;
    }

    private final int $ASSIGN$_numberToWrite(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_numberToWrite.add(null, _numberToWrite, $CHECKPOINT.getTimestamp());
        }
        return _numberToWrite = newValue;
    }

    private final int $ASSIGN$_offsetValue(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_offsetValue.add(null, _offsetValue, $CHECKPOINT.getTimestamp());
        }
        return _offsetValue = newValue;
    }

    private final Token[] $ASSIGN$_buffer(Token[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_buffer.add(null, _buffer, $CHECKPOINT.getTimestamp());
        }
        return _buffer = newValue;
    }

    private final Token $ASSIGN$_buffer(int index0, Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_buffer.add(new int[] {
                    index0
                }, _buffer[index0], $CHECKPOINT.getTimestamp());
        }
        return _buffer[index0] = newValue;
    }

    private final Token[] $BACKUP$_buffer() {
        $RECORD$_buffer.backup(null, _buffer, $CHECKPOINT.getTimestamp());
        return _buffer;
    }

    private final Token[] $ASSIGN$_pastBuffer(Token[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_pastBuffer.add(null, _pastBuffer, $CHECKPOINT.getTimestamp());
        }
        return _pastBuffer = newValue;
    }

    private final Token $ASSIGN$_pastBuffer(int index0, Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_pastBuffer.add(new int[] {
                    index0
                }, _pastBuffer[index0], $CHECKPOINT.getTimestamp());
        }
        return _pastBuffer[index0] = newValue;
    }

    private final Token[] $BACKUP$_pastBuffer() {
        $RECORD$_pastBuffer.backup(null, _pastBuffer, $CHECKPOINT.getTimestamp());
        return _pastBuffer;
    }

    private final boolean $ASSIGN$_usePast(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_usePast.add(null, _usePast, $CHECKPOINT.getTimestamp());
        }
        return _usePast = newValue;
    }

    private final boolean $ASSIGN$_pastNeedsInitializing(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_pastNeedsInitializing.add(null, _pastNeedsInitializing, $CHECKPOINT.getTimestamp());
        }
        return _pastNeedsInitializing = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _highLimit = $RECORD$_highLimit.restore(_highLimit, timestamp, trim);
        _inputIndex = $RECORD$_inputIndex.restore(_inputIndex, timestamp, trim);
        _lowLimit = $RECORD$_lowLimit.restore(_lowLimit, timestamp, trim);
        _numberToRead = $RECORD$_numberToRead.restore(_numberToRead, timestamp, trim);
        _numberToWrite = $RECORD$_numberToWrite.restore(_numberToWrite, timestamp, trim);
        _offsetValue = $RECORD$_offsetValue.restore(_offsetValue, timestamp, trim);
        _buffer = (Token[])$RECORD$_buffer.restore(_buffer, timestamp, trim);
        _pastBuffer = (Token[])$RECORD$_pastBuffer.restore(_pastBuffer, timestamp, trim);
        _usePast = $RECORD$_usePast.restore(_usePast, timestamp, trim);
        _pastNeedsInitializing = $RECORD$_pastNeedsInitializing.restore(_pastNeedsInitializing, timestamp, trim);
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

    private transient FieldRecord $RECORD$_highLimit = new FieldRecord(0);

    private transient FieldRecord $RECORD$_inputIndex = new FieldRecord(0);

    private transient FieldRecord $RECORD$_lowLimit = new FieldRecord(0);

    private transient FieldRecord $RECORD$_numberToRead = new FieldRecord(0);

    private transient FieldRecord $RECORD$_numberToWrite = new FieldRecord(0);

    private transient FieldRecord $RECORD$_offsetValue = new FieldRecord(0);

    private transient FieldRecord $RECORD$_buffer = new FieldRecord(1);

    private transient FieldRecord $RECORD$_pastBuffer = new FieldRecord(1);

    private transient FieldRecord $RECORD$_usePast = new FieldRecord(0);

    private transient FieldRecord $RECORD$_pastNeedsInitializing = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_highLimit,
            $RECORD$_inputIndex,
            $RECORD$_lowLimit,
            $RECORD$_numberToRead,
            $RECORD$_numberToWrite,
            $RECORD$_offsetValue,
            $RECORD$_buffer,
            $RECORD$_pastBuffer,
            $RECORD$_usePast,
            $RECORD$_pastNeedsInitializing
        };

}

