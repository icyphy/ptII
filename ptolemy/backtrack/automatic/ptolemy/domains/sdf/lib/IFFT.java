/* An IFFT.

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
//// IFFT
package ptolemy.backtrack.automatic.ptolemy.domains.sdf.lib;

import java.lang.Object;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.ComplexToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.Complex;
import ptolemy.math.SignalProcessing;

/**
 * This actor calculates the inverse FFT of a complex input array.
 * The order of the IFFT determines the number of tokens that
 * will be consumed and produced on each firing. The order is
 * the base-2 logarithm of the size. The default order is 8,
 * which means that 2<sup>8</sup> = 256 tokens are read and 2<sup>8</sup>
 * = 256 tokens are produced.
 * The result of the IFFT is a new array of Complex tokens.
 * @author Bart Kienhuis, Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Yellow (neuendor)
 * @Pt.AcceptedRating Yellow (eal)
 * @see ptolemy.math.SignalProcessing#IFFTComplexOut
 */
public class IFFT extends SDFTransformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    /**
     * The order of the IFFT.  The type is IntToken, and the value should
     * be greater than zero.  The default value is an IntToken with value 8.
     */
    public Parameter order;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Get the size of the FFT transform
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _transformSize;

    private int _orderValue;

    private ComplexToken[] _outTokenArray;

    private Complex[] _inComplexArray;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public IFFT(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.COMPLEX);
        output.setTypeEquals(BaseType.COMPLEX);
        order = new Parameter(this, "order");
        order.setExpression("8");
        order.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setExpression("2^order");
        output_tokenProductionRate.setExpression("2^order");
    }

    /**
     * Ensure that the order parameter is positive and recompute the
     * size of internal buffers.
     * @param attribute The attribute that has changed.
     * @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == order) {
            $ASSIGN$_orderValue(((IntToken)order.getToken()).intValue());
            if (_orderValue <= 0) {
                throw new IllegalActionException(this, "Order was " + _orderValue+" but must be greater than zero.");
            }
            $ASSIGN$_transformSize((int)Math.pow(2, _orderValue));
            $ASSIGN$_inComplexArray(new Complex[_transformSize]);
            $ASSIGN$_outTokenArray(new ComplexToken[_transformSize]);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Consume the inputs and produce the outputs of the IFFT filter.
     * @exception IllegalActionException If a runtime type error occurs.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        Token[] inTokenArray = input.get(0, _transformSize);
        for (int i = 0; i < _transformSize; i++) {
            $ASSIGN$_inComplexArray(i, ((ComplexToken)inTokenArray[i]).complexValue());
        }
        Complex[] outComplexArray = SignalProcessing.IFFTComplexOut($BACKUP$_inComplexArray(), _orderValue);
        for (int i = 0; i < _transformSize; i++) {
            $ASSIGN$_outTokenArray(i, new ComplexToken(outComplexArray[i]));
        }
        output.send(0, $BACKUP$_outTokenArray(), _transformSize);
    }

    private final int $ASSIGN$_transformSize(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_transformSize.add(null, _transformSize, $CHECKPOINT.getTimestamp());
        }
        return _transformSize = newValue;
    }

    private final int $ASSIGN$_orderValue(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_orderValue.add(null, _orderValue, $CHECKPOINT.getTimestamp());
        }
        return _orderValue = newValue;
    }

    private final ComplexToken[] $ASSIGN$_outTokenArray(ComplexToken[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_outTokenArray.add(null, _outTokenArray, $CHECKPOINT.getTimestamp());
        }
        return _outTokenArray = newValue;
    }

    private final ComplexToken $ASSIGN$_outTokenArray(int index0, ComplexToken newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_outTokenArray.add(new int[] {
                    index0
                }, _outTokenArray[index0], $CHECKPOINT.getTimestamp());
        }
        return _outTokenArray[index0] = newValue;
    }

    private final ComplexToken[] $BACKUP$_outTokenArray() {
        $RECORD$_outTokenArray.backup(null, _outTokenArray, $CHECKPOINT.getTimestamp());
        return _outTokenArray;
    }

    private final Complex[] $ASSIGN$_inComplexArray(Complex[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_inComplexArray.add(null, _inComplexArray, $CHECKPOINT.getTimestamp());
        }
        return _inComplexArray = newValue;
    }

    private final Complex $ASSIGN$_inComplexArray(int index0, Complex newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_inComplexArray.add(new int[] {
                    index0
                }, _inComplexArray[index0], $CHECKPOINT.getTimestamp());
        }
        return _inComplexArray[index0] = newValue;
    }

    private final Complex[] $BACKUP$_inComplexArray() {
        $RECORD$_inComplexArray.backup(null, _inComplexArray, $CHECKPOINT.getTimestamp());
        return _inComplexArray;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _transformSize = $RECORD$_transformSize.restore(_transformSize, timestamp, trim);
        _orderValue = $RECORD$_orderValue.restore(_orderValue, timestamp, trim);
        _outTokenArray = (ComplexToken[])$RECORD$_outTokenArray.restore(_outTokenArray, timestamp, trim);
        _inComplexArray = (Complex[])$RECORD$_inComplexArray.restore(_inComplexArray, timestamp, trim);
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

    private transient FieldRecord $RECORD$_transformSize = new FieldRecord(0);

    private transient FieldRecord $RECORD$_orderValue = new FieldRecord(0);

    private transient FieldRecord $RECORD$_outTokenArray = new FieldRecord(1);

    private transient FieldRecord $RECORD$_inComplexArray = new FieldRecord(1);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_transformSize,
            $RECORD$_orderValue,
            $RECORD$_outTokenArray,
            $RECORD$_inComplexArray
        };

}

