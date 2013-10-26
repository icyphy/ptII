/* A polymorphic autocorrelation function.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
//// Autocorrelation
package ptolemy.backtrack.automatic.ptolemy.domains.sdf.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * This actor calculates the autocorrelation of a sequence of input tokens.
 * <a name="autocorrelation"></a>
 * It is polymorphic, supporting any input data type that supports
 * multiplication, addition, and division by an integer.
 * However, since integer division will lose the fractional portion of the
 * result, type resolution will resolve the input type to double or double
 * matrix if the input port is connected to an integer or integer matrix source,
 * respectively.
 * <p>
 * Both biased and unbiased autocorrelation estimates are supported.
 * If the parameter <i>biased</i> is true, then
 * the autocorrelation estimate is
 * <a name="unbiased autocorrelation"></a>
 * <pre>
 * N-1-k
 * 1  ---
 * r(k) = -  \    x(n)x(n+k)
 * N  /
 * ---
 * n=0
 * </pre>
 * for <i>k </i>= 0<i>, ... , p</i>, where <i>N</i> is the number of
 * inputs to average (<i>numberOfInputs</i>), <i>p</i> is the number of
 * lags to estimate (<i>numberOfLags</i>), and x<sup>*</sup> is the
 * conjugate of the input (if it is complex).
 * This estimate is biased because the outermost lags have fewer than <i>N</i>
 * <a name="biased autocorrelation"></a>
 * terms in the summation, and yet the summation is still normalized by <i>N</i>.
 * <p>
 * If the parameter <i>biased</i> is false (the default), then the estimate is
 * <pre>
 * N-1-k
 * 1   ---
 * r(k) = ---  \    x(n)x(n+k)
 * N-k  /
 * ---
 * n=0
 * </pre>
 * In this case, the estimate is unbiased.
 * However, note that the unbiased estimate does not guarantee
 * a positive definite sequence, so a power spectral estimate based on this
 * autocorrelation estimate may have negative components.
 * <a name="spectral estimation"></a>
 * <p>
 * The output will be an array of tokens whose type is at least that
 * of the input. If the parameter <i>symmetricOutput</i> is true,
 * then the output will be symmetric and have length equal to twice
 * the number of lags requested plus one.  Otherwise, the output
 * will have length equal to twice the number of lags requested,
 * which will be almost symmetric (insert the last
 * sample into the first position to get the symmetric output that you
 * would get with the <i>symmetricOutput</i> being true).
 * @author Edward A. Lee and Yuhong Xiong
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Yellow (neuendor)
 */
public class Autocorrelation extends SDFTransformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Set the output type to be an ArrayType.
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    /**
     * If true, the estimate will be biased.
     * This is a boolean with default value false.
     */
    public Parameter biased;

    /**
     * Number of input samples to average.
     * This is an integer with default value 256.
     */
    public Parameter numberOfInputs;

    /**
     * Number of autocorrelation lags to output.
     * This is an integer with default value 64.
     */
    public Parameter numberOfLags;

    /**
     * If true, then the output from each firing
     * will have 2*<i>numberOfLags</i> + 1
     * samples (an odd number) whose values are symmetric about
     * the midpoint. If false, then the output from each firing will
     * have 2*<i>numberOfLags</i> samples (an even number)
     * by omitting one of the endpoints (the last one).
     * This is a boolean with default value false.
     */
    public Parameter symmetricOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // NOTE: Is there a better way to determine whether the input
    // is complex?
    // Now fill in the first half, which by symmetry is just
    // identical to what was just produced, or its conjugate if
    // the input is complex.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Int or IntMatrix; otherwise, the result is Double
    // or DoubleMatrix, respectively.
    // The constructor takes a port argument so that the clone()
    // method can construct an instance of this class for the
    // input port on the clone.
    ///////////////////////////////////////////////////////////////
    ////                       public inner methods            ////
    private int _numberOfInputs;

    private int _numberOfLags;

    private int _lengthOfOutput;

    private boolean _symmetricOutput;

    private Token[] _outputs;

    private static class FunctionTerm extends MonotonicFunction implements Rollbackable {

        protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////
        private TypedIOPort _port;

        private FunctionTerm(TypedIOPort port) {
            $ASSIGN$_port(port);
        }

        /**
         * Return the function result.
         * @return A Type.
         */
        public Object getValue() {
            Type inputType = _port.getType();
            if (inputType == BaseType.INT) {
                return BaseType.DOUBLE;
            } else if (inputType == BaseType.INT_MATRIX) {
                return BaseType.DOUBLE_MATRIX;
            } else {
                return inputType;
            }
        }

        /**
         * Return an one element array containing the InequalityTerm
         * representing the type of the input port.
         * @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            InequalityTerm[] variable = new InequalityTerm[1];
            variable[0] = _port.getTypeTerm();
            return variable;
        }

        private final TypedIOPort $ASSIGN$_port(TypedIOPort newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$_port.add(null, _port, $CHECKPOINT.getTimestamp());
            }
            return _port = newValue;
        }

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            _port = (TypedIOPort)$RECORD$_port.restore(_port, timestamp, trim);
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

        private transient FieldRecord $RECORD$_port = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$_port
            };

    }

    /**
     * Monotonic function that determines the type of the output port.
     */
    private class OutputTypeTerm extends MonotonicFunction implements Rollbackable {

        protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////
        ///////////////////////////////////////////////////////////////
        ////                   private methods                     ////
        ///////////////////////////////////////////////////////////////
        ////                   private members                     ////
        /**
         * The array type with element types matching the typeable.
         */
        private ArrayType _arrayType;

        /**
         * Return an array type with element types given by the
         * associated typeable.
         * @return An ArrayType.
         * @exception IllegalActionException If the type of the
         * associated typeable cannot be determined.
         */
        public Object getValue() throws IllegalActionException  {
            ConstVariableModelAnalysis analysis = ConstVariableModelAnalysis.getAnalysis(symmetricOutput);
            if (analysis.isConstant(symmetricOutput) && analysis.isConstant(numberOfLags)) {
                Token symmetricOutputToken = analysis.getConstantValue(symmetricOutput);
                Token numberOfLagsToken = analysis.getConstantValue(numberOfLags);
                int lags = ((IntToken)numberOfLagsToken).intValue();
                if (((BooleanToken)symmetricOutputToken).booleanValue()) {
                    return _getArrayTypeRaw(2 * lags + 1);
                } else {
                    return _getArrayTypeRaw(2 * lags);
                }
            }
            return _getArrayTypeRaw();
        }

        /**
         * Return an array containing the type term for the actor's
         * input port.
         * @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            InequalityTerm[] array = new InequalityTerm[1];
            array[0] = input.getTypeTerm();
            return array;
        }

        /**
         * Get an array type with element type matching the type
         * of the associated typeable.
         * @return An array type for the associated typeable.
         * @exception IllegalActionException If the type of the typeable
         * cannot be determined.
         */
        private ArrayType _getArrayTypeRaw() throws IllegalActionException  {
            Type type = input.getType();
            if (_arrayType == null || !_arrayType.getElementType().equals(type)) {
                $ASSIGN$_arrayType(new ArrayType(type));
            }
            return _arrayType;
        }

        /**
         * Get an array type with element type matching the type
         * of the associated typeable.
         * @return An array type for the associated typeable.
         * @exception IllegalActionException If the type of the typeable
         * cannot be determined.
         */
        private ArrayType _getArrayTypeRaw(int length) throws IllegalActionException  {
            Type type = input.getType();
            if (_arrayType == null || !_arrayType.getElementType().equals(type)) {
                $ASSIGN$_arrayType(new ArrayType(type, length));
            }
            return _arrayType;
        }

        private final ArrayType $ASSIGN$_arrayType(ArrayType newValue) {
            if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
                $RECORD$_arrayType.add(null, _arrayType, $CHECKPOINT.getTimestamp());
            }
            return _arrayType = newValue;
        }

        public void $COMMIT(long timestamp) {
            FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
            $RECORD$$CHECKPOINT.commit(timestamp);
        }

        public void $RESTORE(long timestamp, boolean trim) {
            _arrayType = (ArrayType)$RECORD$_arrayType.restore(_arrayType, timestamp, trim);
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

        private transient FieldRecord $RECORD$_arrayType = new FieldRecord(0);

        private transient FieldRecord[] $RECORDS = new FieldRecord[] {
                $RECORD$_arrayType
            };

    }

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Autocorrelation(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input_tokenConsumptionRate.setExpression("numberOfInputs");
        numberOfInputs = new Parameter(this, "numberOfInputs", new IntToken(256));
        numberOfInputs.setTypeEquals(BaseType.INT);
        numberOfLags = new Parameter(this, "numberOfLags", new IntToken(64));
        numberOfLags.setTypeEquals(BaseType.INT);
        biased = new Parameter(this, "biased", new BooleanToken(false));
        biased.setTypeEquals(BaseType.BOOLEAN);
        symmetricOutput = new Parameter(this, "symmetricOutput", new BooleanToken(false));
        symmetricOutput.setTypeEquals(BaseType.BOOLEAN);
        input.setTypeAtLeast(new FunctionTerm(input));
        output.setTypeAtLeast(new OutputTypeTerm());
        attributeChanged(numberOfInputs);
    }

    /**
     * Check to see that the numberOfInputs parameter is positive,
     * and that the numberOfLags parameter is positive.  Based on the
     * new values, recompute the size of the output array.
     * @param attribute The attribute that has changed.
     * @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == numberOfInputs || attribute == numberOfLags || attribute == symmetricOutput) {
            $ASSIGN$_numberOfInputs(((IntToken)numberOfInputs.getToken()).intValue());
            $ASSIGN$_numberOfLags(((IntToken)numberOfLags.getToken()).intValue());
            $ASSIGN$_symmetricOutput(((BooleanToken)symmetricOutput.getToken()).booleanValue());
            if (_numberOfInputs <= 0) {
                throw new IllegalActionException(this, "Invalid numberOfInputs: " + _numberOfInputs);
            }
            if (_numberOfLags <= 0) {
                throw new IllegalActionException(this, "Invalid numberOfLags: " + _numberOfLags);
            }
            if (_symmetricOutput) {
                $ASSIGN$_lengthOfOutput(2 * _numberOfLags + 1);
            } else {
                $ASSIGN$_lengthOfOutput(2 * _numberOfLags);
            }
            if (_outputs == null || _lengthOfOutput != _outputs.length) {
                $ASSIGN$_outputs(new Token[_lengthOfOutput]);
            }
        } else {
            super.attributeChanged(attribute);
        }
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
        Autocorrelation newObject = (Autocorrelation)super.clone(workspace);
        newObject.input.setTypeAtLeast(new FunctionTerm(newObject.input));
        newObject.output.setTypeAtLeast(newObject.new OutputTypeTerm());
        newObject.$ASSIGN$_outputs(new Token[newObject._lengthOfOutput]);
        System.arraycopy($BACKUP$_outputs(), 0, newObject.$BACKUP$_outputs(), 0, _outputs.length);
        return newObject;
    }

    /**
     * Consume tokens from the input and produce a token on the output
     * that contains an array token that represents an autocorrelation
     * estimate of the consumed tokens.  The estimate is consistent with
     * the parameters of the object, as described in the class comment.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        boolean biasedValue = ((BooleanToken)biased.getToken()).booleanValue();
        Token[] inputValues = input.get(0, _numberOfInputs);
        int notSymmetric = _symmetricOutput?0:1;
        boolean complex = inputValues[0] instanceof ComplexToken;
        for (int i = _numberOfLags; i >= 0; i--) {
            Token sum = inputValues[0].zero();
            for (int j = 0; j < _numberOfInputs - i; j++) {
                if (complex) {
                    ComplexToken conjugate = new ComplexToken(((ComplexToken)inputValues[j]).complexValue().conjugate());
                    sum = sum.add(conjugate.multiply(inputValues[j + i]));
                } else {
                    sum = sum.add(inputValues[j].multiply(inputValues[j + i]));
                }
            }
            if (biasedValue) {
                $ASSIGN$_outputs(i + _numberOfLags - notSymmetric, sum.divide(numberOfInputs.getToken()));
            } else {
                $ASSIGN$_outputs(i + _numberOfLags - notSymmetric, sum.divide(new IntToken(_numberOfInputs - i)));
            }
        }
        for (int i = _numberOfLags - 1-notSymmetric; i >= 0; i--) {
            if (complex) {
                ComplexToken candidate = (ComplexToken)_outputs[2 * (_numberOfLags - notSymmetric) - i];
                $ASSIGN$_outputs(i, new ComplexToken(candidate.complexValue().conjugate()));
            } else {
                $ASSIGN$_outputs(i, _outputs[2 * (_numberOfLags - notSymmetric) - i]);
            }
        }
        output.broadcast(new ArrayToken($BACKUP$_outputs()));
    }

    /**
     * If there are not sufficient inputs, then return false.
     * Otherwise, return whatever the base class returns.
     * @exception IllegalActionException If the base class throws it.
     * @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException  {
        if (!input.hasToken(0, _numberOfInputs)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        } else {
            return super.prefire();
        }
    }

    private final int $ASSIGN$_numberOfInputs(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_numberOfInputs.add(null, _numberOfInputs, $CHECKPOINT.getTimestamp());
        }
        return _numberOfInputs = newValue;
    }

    private final int $ASSIGN$_numberOfLags(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_numberOfLags.add(null, _numberOfLags, $CHECKPOINT.getTimestamp());
        }
        return _numberOfLags = newValue;
    }

    private final int $ASSIGN$_lengthOfOutput(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_lengthOfOutput.add(null, _lengthOfOutput, $CHECKPOINT.getTimestamp());
        }
        return _lengthOfOutput = newValue;
    }

    private final boolean $ASSIGN$_symmetricOutput(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_symmetricOutput.add(null, _symmetricOutput, $CHECKPOINT.getTimestamp());
        }
        return _symmetricOutput = newValue;
    }

    private final Token[] $ASSIGN$_outputs(Token[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_outputs.add(null, _outputs, $CHECKPOINT.getTimestamp());
        }
        return _outputs = newValue;
    }

    private final Token $ASSIGN$_outputs(int index0, Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_outputs.add(new int[] {
                    index0
                }, _outputs[index0], $CHECKPOINT.getTimestamp());
        }
        return _outputs[index0] = newValue;
    }

    private final Token[] $BACKUP$_outputs() {
        $RECORD$_outputs.backup(null, _outputs, $CHECKPOINT.getTimestamp());
        return _outputs;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _numberOfInputs = $RECORD$_numberOfInputs.restore(_numberOfInputs, timestamp, trim);
        _numberOfLags = $RECORD$_numberOfLags.restore(_numberOfLags, timestamp, trim);
        _lengthOfOutput = $RECORD$_lengthOfOutput.restore(_lengthOfOutput, timestamp, trim);
        _symmetricOutput = $RECORD$_symmetricOutput.restore(_symmetricOutput, timestamp, trim);
        _outputs = (Token[])$RECORD$_outputs.restore(_outputs, timestamp, trim);
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

    private transient FieldRecord $RECORD$_numberOfInputs = new FieldRecord(0);

    private transient FieldRecord $RECORD$_numberOfLags = new FieldRecord(0);

    private transient FieldRecord $RECORD$_lengthOfOutput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_symmetricOutput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_outputs = new FieldRecord(1);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_numberOfInputs,
            $RECORD$_numberOfLags,
            $RECORD$_lengthOfOutput,
            $RECORD$_symmetricOutput,
            $RECORD$_outputs
        };

}

