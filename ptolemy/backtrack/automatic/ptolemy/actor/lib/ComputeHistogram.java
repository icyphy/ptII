/* Compute a histogram of input data.

 @Copyright (c) 2003-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY

 */
///////////////////////////////////////////////////////////////////
//// ComputeHistogram
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
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
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 * Compute a histogram.
 * <p>
 * The output array consists of a set of vertical bars, each representing
 * a histogram bin.  The height of the bar is the count of the number
 * of inputs that have been observed that fall within that bin.
 * The <i>n</i>-th bin represents values in the range
 * (<i>x</i> - <i>w</i>/2 + <i>o</i>, <i>x</i> + <i>w</i>/2 + <i>o</i>),
 * where <i>w</i> is the value of the <i>binWidth</i> parameter,
 * and <i>o</i> is the value of the <i>binOffset</i> parameter.
 * So for example, if <i>o = w/2</i>,
 * then each bin represents values from <i>nw</i> to
 * (<i>n</i> + 1)<i>w</i> for some integer <i>n</i>.
 * The default offset is 0.5, half the default bin width, which is 1.0.
 * <p>
 * This actor has a <i>legend</i> parameter,
 * which gives a comma-separated list of labels to attach to
 * each dataset.  Normally, the number of elements in this list
 * should equal the number of input channels, although this
 * is not enforced.
 * @see ptolemy.plot.Histogram
 * @author Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 4.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ComputeHistogram extends TypedAtomicActor implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The lowest value that will be recorded in the histogram.
     * This parameter has type double, with default value 0.0.
     */
    public Parameter minimumValue;

    /**
     * The highest value that will be recorded in the histogram.
     * This parameter has type double, with default value 1.0.
     */
    public Parameter maximumValue;

    /**
     * The number of bins.
     * This parameter has type int, with default value 10.
     */
    public Parameter numberOfBins;

    /**
     * The number of tokens to compute the histogram for.
     */
    public PortParameter inputCount;

    /**
     * The parameter that determines the consumption rate of the input.
     */
    public Parameter input_tokenConsumptionRate;

    /**
     * The input port of type double.
     */
    public TypedIOPort input;

    /**
     * The input port of type array of integer.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Send the output array.
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Calculate the bin number.
    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    private int[] _bins;

    private double _minimumValue;

    private double _maximumValue;

    private double _binWidth;

    private int _numberOfBins;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public ComputeHistogram(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.INT));
        minimumValue = new Parameter(this, "minimumValue");
        minimumValue.setExpression("0.0");
        minimumValue.setTypeEquals(BaseType.DOUBLE);
        maximumValue = new Parameter(this, "maximumValue");
        maximumValue.setExpression("1.0");
        maximumValue.setTypeEquals(BaseType.DOUBLE);
        numberOfBins = new Parameter(this, "numberOfBins");
        numberOfBins.setExpression("10");
        numberOfBins.setTypeEquals(BaseType.INT);
        inputCount = new PortParameter(this, "inputCount");
        inputCount.setExpression("10");
        inputCount.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate = new Parameter(input, "tokenConsumptionRate");
        input_tokenConsumptionRate.setExpression("inputCount");
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        input_tokenConsumptionRate.setPersistent(false);
    }

    /**
     * If the parameter is <i>binWidth</i> or <i>binOffset</i>, then
     * configure the histogram with the specified bin width or offset.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If the bin width is not positive.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == minimumValue || attribute == maximumValue || attribute == numberOfBins) {
            $ASSIGN$_minimumValue(((DoubleToken)minimumValue.getToken()).doubleValue());
            $ASSIGN$_maximumValue(((DoubleToken)maximumValue.getToken()).doubleValue());
            $ASSIGN$_numberOfBins(((IntToken)numberOfBins.getToken()).intValue());
            double width = (_maximumValue - _minimumValue) / _numberOfBins;
            if (width <= 0.0) {
                throw new IllegalActionException(this, "Invalid bin width (must be positive): " + width);
            }
            $ASSIGN$_binWidth(width);
            $ASSIGN$_bins(new int[_numberOfBins]);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Clone the actor into the specified workspace.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        ComputeHistogram newObject = (ComputeHistogram)super.clone(workspace);
        newObject.$ASSIGN$_bins(new int[_numberOfBins]);
        if (_bins != null) {
            System.arraycopy($BACKUP$_bins(), 0, newObject.$BACKUP$_bins(), 0, _bins.length);
        }
        return newObject;
    }

    /**
     * Read at most one input token from each input channel
     * and update the histogram.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        $ASSIGN$_bins(new int[_numberOfBins]);
        inputCount.update();
        int count = ((IntToken)inputCount.getToken()).intValue();
        for (int i = 0; i < count; i++) {
            if (input.hasToken(0)) {
                DoubleToken curToken = (DoubleToken)input.get(0);
                double curValue = curToken.doubleValue();
                _addPoint(curValue);
            }
        }
        Token[] values = new Token[_bins.length];
        for (int i = 0; i < _bins.length; i++) {
            values[i] = new IntToken(_bins[i]);
        }
        output.send(0, new ArrayToken(BaseType.INT, values));
    }

    /**
     * Return false if the input does not have enough tokens to fire.
     * Otherwise, return true.
     * @return False if the number of input tokens available is not at least
     * equal to the <i>decimation</i> parameter multiplied by the
     * <i>blockSize</i> parameter.
     * @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException  {
        int count = ((IntToken)inputCount.getToken()).intValue();
        return input.hasToken(0, count) && super.prefire();
    }

    private void _addPoint(double value) {
        int bin = (int)Math.round((value - (_minimumValue + _binWidth * 0.5)) / _binWidth);
        if (bin >= 0 && bin < _numberOfBins) {
            $ASSIGN$SPECIAL$_bins(11, bin, _bins[bin]);
        }
    }

    private final int[] $ASSIGN$_bins(int[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_bins.add(null, _bins, $CHECKPOINT.getTimestamp());
        }
        return _bins = newValue;
    }

    private final int $ASSIGN$SPECIAL$_bins(int operator, int index0, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_bins.add(new int[] {
                    index0
                }, _bins[index0], $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _bins[index0] += newValue;
            case 1:
                return _bins[index0] -= newValue;
            case 2:
                return _bins[index0] *= newValue;
            case 3:
                return _bins[index0] /= newValue;
            case 4:
                return _bins[index0] &= newValue;
            case 5:
                return _bins[index0] |= newValue;
            case 6:
                return _bins[index0] ^= newValue;
            case 7:
                return _bins[index0] %= newValue;
            case 8:
                return _bins[index0] <<= newValue;
            case 9:
                return _bins[index0] >>= newValue;
            case 10:
                return _bins[index0] >>>= newValue;
            case 11:
                return _bins[index0]++;
            case 12:
                return _bins[index0]--;
            case 13:
                return ++_bins[index0];
            case 14:
                return --_bins[index0];
            default:
                return _bins[index0];
        }
    }

    private final int[] $BACKUP$_bins() {
        $RECORD$_bins.backup(null, _bins, $CHECKPOINT.getTimestamp());
        return _bins;
    }

    private final double $ASSIGN$_minimumValue(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_minimumValue.add(null, _minimumValue, $CHECKPOINT.getTimestamp());
        }
        return _minimumValue = newValue;
    }

    private final double $ASSIGN$_maximumValue(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_maximumValue.add(null, _maximumValue, $CHECKPOINT.getTimestamp());
        }
        return _maximumValue = newValue;
    }

    private final double $ASSIGN$_binWidth(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_binWidth.add(null, _binWidth, $CHECKPOINT.getTimestamp());
        }
        return _binWidth = newValue;
    }

    private final int $ASSIGN$_numberOfBins(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_numberOfBins.add(null, _numberOfBins, $CHECKPOINT.getTimestamp());
        }
        return _numberOfBins = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _bins = (int[])$RECORD$_bins.restore(_bins, timestamp, trim);
        _minimumValue = $RECORD$_minimumValue.restore(_minimumValue, timestamp, trim);
        _maximumValue = $RECORD$_maximumValue.restore(_maximumValue, timestamp, trim);
        _binWidth = $RECORD$_binWidth.restore(_binWidth, timestamp, trim);
        _numberOfBins = $RECORD$_numberOfBins.restore(_numberOfBins, timestamp, trim);
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

    private transient FieldRecord $RECORD$_bins = new FieldRecord(1);

    private transient FieldRecord $RECORD$_minimumValue = new FieldRecord(0);

    private transient FieldRecord $RECORD$_maximumValue = new FieldRecord(0);

    private transient FieldRecord $RECORD$_binWidth = new FieldRecord(0);

    private transient FieldRecord $RECORD$_numberOfBins = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_bins,
            $RECORD$_minimumValue,
            $RECORD$_maximumValue,
            $RECORD$_binWidth,
            $RECORD$_numberOfBins
        };

}

