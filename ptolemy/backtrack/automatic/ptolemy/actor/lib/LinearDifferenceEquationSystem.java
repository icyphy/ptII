/* Linear Difference Equation System.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
//// LinearDifferenceEquationSystem
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Linear Difference Equation System.
 * <p>The linear state-space model implements a system whose behavior
 * is defined by:
 * <pre>
 * x(k+1) = Ax(k) + Bu(k)
 * y(k) = Cx(k) + Du(k)
 * x(0) = x0
 * </pre>
 * where x is the state vector, u is the input vector, and y is the
 * output vector. (Note that in Ptolemy II, vectors are double matrices
 * with one column or one row.) The matrix coefficients must have the
 * following characteristics:
 * <pre>
 * A must be an n-by-n matrix, where n is the number of states.
 * B must be an n-by-m matrix, where m is the number of inputs.
 * C must be an r-by-n matrix, where r is the number of outputs.
 * D must be an r-by-m matrix.
 * </pre>
 * For each firing, the actor accepts one input DoubleMatrixToken of
 * dimension <i>m</i> x 1, and generates one output DoubleMatrixToken of
 * dimension <i>r</i> x 1.
 * <P>
 * In addition to producing the output <i>y</i> through port
 * <i>output</i>, the actor also produce the state values <i>x</i>
 * through port <i>state</i>.
 * @author Jie Liu and Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Yellow (celaine)
 * @Pt.AcceptedRating Yellow (celaine)
 */
public class LinearDifferenceEquationSystem extends Transformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // icon
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * Output port that produces DoubleMatrixToken of dimension
     * <i>r</i> x 1 (see class comment).
     */
    public TypedIOPort state;

    /**
     * The A matrix in the state-space representation. It must be a
     * square matrix.
     * The default value is [[1.0]].
     */
    public Parameter A;

    /**
     * The B matrix in the state-space representation. The number of
     * rows must be equal to the number of rows of the A matrix. The
     * number of columns must be equal to the number of rows in the
     * input token.  The default value is [[1.0]].
     */
    public Parameter B;

    /**
     * The C matrix in the state-space representation. The number of
     * columns must be equal to the number of columns of the A
     * matrix.  The number of rows must be equal to the number of
     * columns in the output token. The default value is [[0.0]].
     */
    public Parameter C;

    /**
     * The D matrix in the state-space representation. The number of
     * columns must be equal to the number of rows in the input token
     * (a DoubleMatrixToken of dimension <i>m</i> x 1.  The number of
     * rows must be equal to the number of columns in the output
     * token (a DoubleMatrixToken of dimension <i>r</i> x 1.  The
     * default value is [[0.0]].
     */
    public Parameter D;

    /**
     * The initial condition for the state variables. This must be a
     * column vector (double matrix with only one column) whose
     * length is equal to the number of state variables.  The default
     * value is [0.0].
     * NOTE: Changes to this parameter will be * applied at the next
     * time when fire() is called.
     */
    public Parameter initialStates;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Check that it is a square matrix.
    // Check that B is a matrix.
    // Check that C is a matrix.
    // The initialStates parameter should be a row vector.
    // reset initial state.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The internal state.
    // The next state.
    // Indicate whether the initial state has beed set.
    // Indicate whether the output is a scalar.
    // Indicate whether the state variable is a scalar;
    private Token _x;

    private Token _xPrime;

    private boolean _initialStateChanged;

    private boolean _singleOutput;

    private boolean _singleState;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public LinearDifferenceEquationSystem(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        input.setMultiport(false);
        output.setMultiport(false);
        state = new TypedIOPort(this, "state", false, true);
        A = new Parameter(this, "A");
        A.setExpression("[1.0]");
        A.setTypeEquals(BaseType.DOUBLE_MATRIX);
        B = new Parameter(this, "B");
        B.setExpression("[1.0]");
        B.setTypeEquals(BaseType.DOUBLE_MATRIX);
        C = new Parameter(this, "C");
        C.setExpression("[1.0]");
        C.setTypeEquals(BaseType.DOUBLE_MATRIX);
        D = new Parameter(this, "D");
        D.setExpression("[0.0]");
        D.setTypeEquals(BaseType.DOUBLE_MATRIX);
        initialStates = new Parameter(this, "initialStates");
        initialStates.setExpression("[0.0]");
        initialStates.setTypeEquals(BaseType.DOUBLE_MATRIX);
        double[][] zero =  {
             {
                0.0
            }
        };
        $ASSIGN$_x(new DoubleMatrixToken(zero));
        $ASSIGN$_initialStateChanged(true);
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-75\" y=\"-30\" "+"width=\"150\" height=\"60\" "+"style=\"fill:white\"/>\n"+"<text x=\"-70\" y=\"-10\" "+"style=\"font-size:14\">\n"+"x(k+1) = Ax(k) + Bu(k) "+"</text>\n"+"<text x=\"-70\" y=\"10\" "+"style=\"font-size:14\">\n"+"    y(k) = Cx(k) + Du(k)"+"</text>\n"+"</svg>\n");
    }

    /**
     * If the argument is <i>A, B, C, D</i> or <i>initialStates</i>
     * parameters, check that they are indeed matrices and vectors,
     * and request initialization from the director if there is one.
     * Other sanity checks like the dimensions of the matrices will
     * be done in the preinitialize() method.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If the numerator and the
     * denominator matrix is not a row vector.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == A) {
            DoubleMatrixToken token = (DoubleMatrixToken)A.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0 || token.getRowCount() != token.getColumnCount()) {
                throw new IllegalActionException(this, "The A matrix must be a nonempty square matrix.");
            }
        } else if (attribute == B) {
            DoubleMatrixToken token = (DoubleMatrixToken)B.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this, "The B matrix must be a nonempty matrix.");
            }
        } else if (attribute == C) {
            DoubleMatrixToken token = (DoubleMatrixToken)C.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this, "The C matrix must be a nonempty matrix.");
            }
        } else if (attribute == D) {
            DoubleMatrixToken token = (DoubleMatrixToken)D.getToken();
            if (token.getRowCount() == 0 || token.getColumnCount() == 0) {
                throw new IllegalActionException(this, "The D matrix must be a nonempty matrix.");
            }
        } else if (attribute == initialStates) {
            DoubleMatrixToken token = (DoubleMatrixToken)initialStates.getToken();
            if (token.getColumnCount() != 1 || token.getRowCount() < 1) {
                throw new IllegalActionException(this, "The initialStates must be a column vector.");
            }
            $ASSIGN$_initialStateChanged(true);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Consume the input token, compute the system response, and
     * produce outputs. Notice that the state is updated in
     * postfire. That is, if fire() is called multiple times before
     * postfire() is called, this actor will use the same internal
     * state to compute the outputs.
     * @exception IllegalActionException If the get() or send() methods
     * of the ports throw this exception.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (input.hasToken(0)) {
            Token u = input.get(0);
            Token y = C.getToken().multiply(_x).add(D.getToken().multiply(u));
            $ASSIGN$_xPrime(A.getToken().multiply(_x).add(B.getToken().multiply(u)));
            if (_singleOutput) {
                output.send(0, ((DoubleMatrixToken)y).getElementAsToken(0, 0));
            } else {
                output.send(0, y);
            }
            if (_singleState) {
                state.send(0, ((DoubleMatrixToken)_x).getElementAsToken(0, 0));
            } else {
                state.send(0, _x);
            }
        }
    }

    /**
     * Update the internal state.
     * @exception IllegalActionException If thrown by the super class.
     */
    public boolean postfire() throws IllegalActionException  {
        if (super.postfire()) {
            $ASSIGN$_x(_xPrime);
            return true;
        } else {
            return false;
        }
    }

    /**
     * If the parameter <i>initialStates</i> has changed, then update
     * the internal state of this actor to be the value of the
     * <i>initialStates</i> parameter.
     * @exception IllegalActionException If <i>initialStates</i>
     * parameter is invalid, or if the base class throws it.
     */
    public boolean prefire() throws IllegalActionException  {
        super.prefire();
        if (_initialStateChanged) {
            $ASSIGN$_x(initialStates.getToken());
            $ASSIGN$_initialStateChanged(false);
        }
        if (input.hasToken(0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check the dimension of all parameters. If the system needs
     * multiple inputs, then set the type of the <i>input</i> port to
     * be DoubleMatrix; otherwise set the type to Double.  Similarly,
     * for the output ports <i>output</i> and <i>state</i>, if the
     * system needs multiple outputs, then set the type of the port
     * to be DoubleMatrix; otherwise set the type to Double.
     * @exception IllegalActionException If the dimensions do not
     * match.
     */
    public void preinitialize() throws IllegalActionException  {
        super.preinitialize();
        DoubleMatrixToken a = (DoubleMatrixToken)A.getToken();
        int n = a.getRowCount();
        DoubleMatrixToken b = (DoubleMatrixToken)B.getToken();
        if (b.getRowCount() != n) {
            throw new IllegalActionException(this, "The number of rows of the B matrix should equal to " + "the number of rows of the A matrix.");
        }
        if (n == 1) {
            $ASSIGN$_singleState(true);
            state.setTypeEquals(BaseType.DOUBLE);
        } else {
            $ASSIGN$_singleState(false);
            state.setTypeEquals(BaseType.DOUBLE_MATRIX);
        }
        int m = b.getColumnCount();
        if (m == 1) {
            input.setTypeEquals(BaseType.DOUBLE);
        } else {
            input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        }
        DoubleMatrixToken c = (DoubleMatrixToken)C.getToken();
        if (c.getColumnCount() != n) {
            throw new IllegalActionException(this, "The number of columns of the C matrix should equal to " + "the number of rows of the A matrix.");
        }
        int r = c.getRowCount();
        if (r == 1) {
            $ASSIGN$_singleOutput(true);
            output.setTypeEquals(BaseType.DOUBLE);
        } else {
            $ASSIGN$_singleOutput(false);
            output.setTypeEquals(BaseType.DOUBLE_MATRIX);
        }
        DoubleMatrixToken d = (DoubleMatrixToken)D.getToken();
        if (c.getRowCount() != d.getRowCount()) {
            throw new IllegalActionException(this, "The number of rows of the D matrix should equal to " + "the number of rows of the C matrix.");
        }
        DoubleMatrixToken x0 = (DoubleMatrixToken)initialStates.getToken();
        if (x0.getRowCount() != n) {
            throw new IllegalActionException(this, "The number of initial states should equal to " + "the number of columns of the A matrix.");
        }
        $ASSIGN$_initialStateChanged(true);
    }

    private final Token $ASSIGN$_x(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_x.add(null, _x, $CHECKPOINT.getTimestamp());
        }
        return _x = newValue;
    }

    private final Token $ASSIGN$_xPrime(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_xPrime.add(null, _xPrime, $CHECKPOINT.getTimestamp());
        }
        return _xPrime = newValue;
    }

    private final boolean $ASSIGN$_initialStateChanged(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_initialStateChanged.add(null, _initialStateChanged, $CHECKPOINT.getTimestamp());
        }
        return _initialStateChanged = newValue;
    }

    private final boolean $ASSIGN$_singleOutput(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_singleOutput.add(null, _singleOutput, $CHECKPOINT.getTimestamp());
        }
        return _singleOutput = newValue;
    }

    private final boolean $ASSIGN$_singleState(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_singleState.add(null, _singleState, $CHECKPOINT.getTimestamp());
        }
        return _singleState = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _x = (Token)$RECORD$_x.restore(_x, timestamp, trim);
        _xPrime = (Token)$RECORD$_xPrime.restore(_xPrime, timestamp, trim);
        _initialStateChanged = $RECORD$_initialStateChanged.restore(_initialStateChanged, timestamp, trim);
        _singleOutput = $RECORD$_singleOutput.restore(_singleOutput, timestamp, trim);
        _singleState = $RECORD$_singleState.restore(_singleState, timestamp, trim);
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

    private transient FieldRecord $RECORD$_x = new FieldRecord(0);

    private transient FieldRecord $RECORD$_xPrime = new FieldRecord(0);

    private transient FieldRecord $RECORD$_initialStateChanged = new FieldRecord(0);

    private transient FieldRecord $RECORD$_singleOutput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_singleState = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_x,
            $RECORD$_xPrime,
            $RECORD$_initialStateChanged,
            $RECORD$_singleOutput,
            $RECORD$_singleState
        };

}

