/* An IIR filter actor that uses a direct form II implementation.

 Copyright (c) 2003-2013 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
///////////////////////////////////////////////////////////////////
//// GradientAdaptiveLattice
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Lattice;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * An adaptive FIR filter with a lattice structure.  This class extends
 * the base class to dynamically adapt the reflection coefficients to
 * minimize the power of the output sequence.  The output reflection
 * coefficients are guaranteed to lie between -1.0 and 1.0, ensuring that the
 * resulting filter is a minimum phase linear predictor.  The
 * reflectionCoefficients parameter is interpreted as the initial
 * coefficients.
 * @author Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 4.0
 * @Pt.ProposedRating Red (vogel)
 * @Pt.AcceptedRating Red (cxh)
 */
public class GradientAdaptiveLattice extends Lattice implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Parameters
    // The currently adapted reflection coefficients
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The output port that produces the current reflection
     * coefficients.  The port is of type array of double.
     */
    public TypedIOPort adaptedReflectionCoefficients;

    /**
     * The time constant of the filter, which determines how fast the
     * filter adapts.
     * The default value of this parameter is 1.0.
     */
    public Parameter timeConstant;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // FIXME: there is a bug in either the variable naming or the
    // two lines below.
    // Reinitialize the reflection coefficients from the parameter value.
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    // Compute the filter, updating the caches, based on the current
    // values.  Extend the base class to adapt the reflection coefficients
    // NOTE: The following code is ported from Ptolemy Classic.
    // Update forward errors.
    // Backward: Compute the weights for the next round Note:
    // strictly speaking, _backwardCache[_order] is not necessary
    // for computing the output.  It is computed for the use of
    // subclasses which adapt the reflection coefficients.
    // Reallocate the internal arrays. Extend the base class to
    // reallocate the power estimation array.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The error power in the output signal.  The length is _order.
    // Cache of the error power.  The length is _order.
    // Cache of the reflection coefficients.  The length is _order;
    private double _alpha = 0.0;

    private double _oneMinusAlpha = 1.0;

    private double[] _estimatedErrorPower;

    private double[] _estimatedErrorPowerCache;

    private double[] _reflectionCoefficientsCache;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public GradientAdaptiveLattice(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        timeConstant = new Parameter(this, "timeConstant");
        timeConstant.setExpression("1.0");
        timeConstant.setTypeEquals(BaseType.DOUBLE);
        timeConstant.validate();
        adaptedReflectionCoefficients = new TypedIOPort(this, "adaptedReflectionCoefficients", false, true);
        adaptedReflectionCoefficients.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        output.setTypeAtLeast(input);
    }

    /**
     * Handle parameter change events on the
     * <i>order</i> and <i>timeConstant</i> parameters. The
     * filter state vector is reinitialized to zero state.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If this method is invoked
     * with an unrecognized parameter.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == timeConstant) {
            double timeConstantValue = ((DoubleToken)timeConstant.getToken()).doubleValue();
            $ASSIGN$_oneMinusAlpha((timeConstantValue - 1.0) / (timeConstantValue + 1.0));
            $ASSIGN$_alpha(1.0 - _oneMinusAlpha);
        }
        super.attributeChanged(attribute);
    }

    /**
     * Clone the actor into the specified workspace. This calls the
     * base class and then sets the type constraints.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class has
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        GradientAdaptiveLattice newObject = (GradientAdaptiveLattice)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.$ASSIGN$_estimatedErrorPower(new double[newObject._order + 1]);
        System.arraycopy(newObject.$BACKUP$_estimatedErrorPower(), 0, $BACKUP$_estimatedErrorPower(), 0, newObject._order + 1);
        newObject.$ASSIGN$_estimatedErrorPowerCache(new double[newObject._order + 1]);
        System.arraycopy(newObject.$BACKUP$_estimatedErrorPowerCache(), 0, $BACKUP$_estimatedErrorPowerCache(), 0, newObject._order + 1);
        newObject.$ASSIGN$_reflectionCoefficientsCache(new double[newObject._order]);
        System.arraycopy(newObject.$BACKUP$_reflectionCoefficientsCache(), 0, $BACKUP$_reflectionCoefficientsCache(), 0, newObject._order);
        return newObject;
    }

    /**
     * Initialize the state of the filter.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        for (int i = 0; i <= _order; i++) {
            $ASSIGN$_estimatedErrorPower(i, 0.0);
            $ASSIGN$_estimatedErrorPowerCache(i, 0.0);
            if (i < _order) {
                $ASSIGN$_reflectionCoefficientsCache(i, 0.0);
            }
        }
        ArrayToken value = (ArrayToken)reflectionCoefficients.getToken();
        for (int i = 0; i < _order; i++) {
            _reflectionCoefficients[i] = ((DoubleToken)value.getElement(i)).doubleValue();
        }
    }

    /**
     * Update the filter state.
     * @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException  {
        System.arraycopy($BACKUP$_estimatedErrorPowerCache(), 0, $BACKUP$_estimatedErrorPower(), 0, _order + 1);
        System.arraycopy($BACKUP$_reflectionCoefficientsCache(), 0, _reflectionCoefficients, 0, _order);
        return super.postfire();
    }

    protected void _doFilter() throws IllegalActionException  {
        double k;
        for (int i = 0; i < _order; i++) {
            k = _reflectionCoefficients[i];
            _forwardCache[i + 1] = -k * _backwardCache[i] + _forwardCache[i];
        }
        DoubleToken[] outputArray = new DoubleToken[_order];
        for (int i = _order; i > 0; i--) {
            k = _reflectionCoefficients[i - 1];
            _backwardCache[i] = -k * _forwardCache[i - 1] + _backwardCache[i - 1];
            double fe_i = _forwardCache[i];
            double be_i = _backwardCache[i];
            double fe_ip = _forwardCache[i - 1];
            double be_ip = _backwardCache[i - 1];
            double newError = _estimatedErrorPower[i] * _oneMinusAlpha + _alpha * (fe_ip * fe_ip + be_ip * be_ip);
            double newCoefficient = _reflectionCoefficients[i - 1];
            if (newError != 0.0) {
                newCoefficient += _alpha * (fe_i * be_ip + be_i * fe_ip) / newError;
                if (newCoefficient > 1.0) {
                    newCoefficient = 1.0;
                } else if (newCoefficient < -1.0) {
                    newCoefficient = -1.0;
                }
            }
            outputArray[i - 1] = new DoubleToken(newCoefficient);
            $ASSIGN$_reflectionCoefficientsCache(i - 1, newCoefficient);
            $ASSIGN$_estimatedErrorPowerCache(i, newError);
        }
        adaptedReflectionCoefficients.send(0, new ArrayToken(BaseType.DOUBLE, outputArray));
    }

    protected void _reallocate() {
        super._reallocate();
        $ASSIGN$_estimatedErrorPower(new double[_order + 1]);
        $ASSIGN$_estimatedErrorPowerCache(new double[_order + 1]);
        $ASSIGN$_reflectionCoefficientsCache(new double[_order]);
    }

    private final double $ASSIGN$_alpha(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_alpha.add(null, _alpha, $CHECKPOINT.getTimestamp());
        }
        return _alpha = newValue;
    }

    private final double $ASSIGN$_oneMinusAlpha(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_oneMinusAlpha.add(null, _oneMinusAlpha, $CHECKPOINT.getTimestamp());
        }
        return _oneMinusAlpha = newValue;
    }

    private final double[] $ASSIGN$_estimatedErrorPower(double[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_estimatedErrorPower.add(null, _estimatedErrorPower, $CHECKPOINT.getTimestamp());
        }
        return _estimatedErrorPower = newValue;
    }

    private final double $ASSIGN$_estimatedErrorPower(int index0, double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_estimatedErrorPower.add(new int[] {
                    index0
                }, _estimatedErrorPower[index0], $CHECKPOINT.getTimestamp());
        }
        return _estimatedErrorPower[index0] = newValue;
    }

    private final double[] $BACKUP$_estimatedErrorPower() {
        $RECORD$_estimatedErrorPower.backup(null, _estimatedErrorPower, $CHECKPOINT.getTimestamp());
        return _estimatedErrorPower;
    }

    private final double[] $ASSIGN$_estimatedErrorPowerCache(double[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_estimatedErrorPowerCache.add(null, _estimatedErrorPowerCache, $CHECKPOINT.getTimestamp());
        }
        return _estimatedErrorPowerCache = newValue;
    }

    private final double $ASSIGN$_estimatedErrorPowerCache(int index0, double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_estimatedErrorPowerCache.add(new int[] {
                    index0
                }, _estimatedErrorPowerCache[index0], $CHECKPOINT.getTimestamp());
        }
        return _estimatedErrorPowerCache[index0] = newValue;
    }

    private final double[] $BACKUP$_estimatedErrorPowerCache() {
        $RECORD$_estimatedErrorPowerCache.backup(null, _estimatedErrorPowerCache, $CHECKPOINT.getTimestamp());
        return _estimatedErrorPowerCache;
    }

    private final double[] $ASSIGN$_reflectionCoefficientsCache(double[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_reflectionCoefficientsCache.add(null, _reflectionCoefficientsCache, $CHECKPOINT.getTimestamp());
        }
        return _reflectionCoefficientsCache = newValue;
    }

    private final double $ASSIGN$_reflectionCoefficientsCache(int index0, double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_reflectionCoefficientsCache.add(new int[] {
                    index0
                }, _reflectionCoefficientsCache[index0], $CHECKPOINT.getTimestamp());
        }
        return _reflectionCoefficientsCache[index0] = newValue;
    }

    private final double[] $BACKUP$_reflectionCoefficientsCache() {
        $RECORD$_reflectionCoefficientsCache.backup(null, _reflectionCoefficientsCache, $CHECKPOINT.getTimestamp());
        return _reflectionCoefficientsCache;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _alpha = $RECORD$_alpha.restore(_alpha, timestamp, trim);
        _oneMinusAlpha = $RECORD$_oneMinusAlpha.restore(_oneMinusAlpha, timestamp, trim);
        _estimatedErrorPower = (double[])$RECORD$_estimatedErrorPower.restore(_estimatedErrorPower, timestamp, trim);
        _estimatedErrorPowerCache = (double[])$RECORD$_estimatedErrorPowerCache.restore(_estimatedErrorPowerCache, timestamp, trim);
        _reflectionCoefficientsCache = (double[])$RECORD$_reflectionCoefficientsCache.restore(_reflectionCoefficientsCache, timestamp, trim);
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

    private transient FieldRecord $RECORD$_alpha = new FieldRecord(0);

    private transient FieldRecord $RECORD$_oneMinusAlpha = new FieldRecord(0);

    private transient FieldRecord $RECORD$_estimatedErrorPower = new FieldRecord(1);

    private transient FieldRecord $RECORD$_estimatedErrorPowerCache = new FieldRecord(1);

    private transient FieldRecord $RECORD$_reflectionCoefficientsCache = new FieldRecord(1);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_alpha,
            $RECORD$_oneMinusAlpha,
            $RECORD$_estimatedErrorPower,
            $RECORD$_estimatedErrorPowerCache,
            $RECORD$_reflectionCoefficientsCache
        };

}

