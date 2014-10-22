/* An actor that outputs the sum of the inputs so far.

 Copyright (c) 2002-2013 The Regents of the University of California.
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
//// Accumulator
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 * Output the initial value plus the sum of all the inputs since
 * the last time a true token was received at the reset port.
 * One output is produced each time the actor is fired. The
 * inputs and outputs can be any token type that supports addition.
 * The output type is constrained to be greater than or
 * equal to the input type and the type of the <i>init</i> parameter.
 * <p>
 * If the input and <i>init</i> data type are scalars, then you can
 * also set the <i>lowerBound</i> and <i>upperBound</i> parameters to
 * limit the range of the accumulated value.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Yellow (neuendor)
 */
public class Accumulator extends Transformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // set the type constraints.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The lower bound. If this is set, then its type must be the
     * same as that of the <i>init</i> parameter, and the output
     * will be constrained to never drop below the lower bound.
     * By default, this is not set, so there is no lower bound.
     */
    public Parameter lowerBound;

    /**
     * The value produced by the actor on its first iteration.
     * The default value of this parameter is the integer 0.
     */
    public Parameter init;

    /**
     * If this port receives a True token on any channel, then the
     * accumulator state will be reset to the initial value.
     * This is a multiport and has type boolean.
     */
    public TypedIOPort reset;

    /**
     * The upper bound. If this is set, then its type must be the
     * same as that of the <i>init</i> parameter, and the output
     * will be constrained to never rise above the upper bound.
     * By default, this is not set, so there is no upper bound.
     */
    public Parameter upperBound;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // set the type constraints.
    // Check whether to reset.
    // Being reset at this firing.
    // Check the bounds.
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /**
     * The running sum.
     */
    private Token _sum;

    /**
     * The latest sum, prior to a state commit.
     */
    private Token _latestSum;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Accumulator(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setMultiport(true);
        reset = new TypedIOPort(this, "reset", true, false);
        reset.setTypeEquals(BaseType.BOOLEAN);
        reset.setMultiport(true);
        new StringAttribute(reset, "_cardinal").setExpression("SOUTH");
        new SingletonParameter(reset, "_showName").setToken(BooleanToken.TRUE);
        init = new Parameter(this, "init");
        init.setExpression("0");
        lowerBound = new Parameter(this, "lowerBound");
        lowerBound.setTypeSameAs(init);
        upperBound = new Parameter(this, "upperBound");
        upperBound.setTypeSameAs(init);
        output.setTypeAtLeast(init);
        output.setTypeAtLeast(input);
    }

    /**
     * Clone the actor into the specified workspace. This calls the
     * base class and then sets up the type constraints.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        Accumulator newObject = (Accumulator)super.clone(workspace);
        newObject.lowerBound.setTypeSameAs(newObject.init);
        newObject.upperBound.setTypeSameAs(newObject.init);
        newObject.output.setTypeAtLeast(newObject.init);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /**
     * Consume at most one token from each channel of the <i>input</i>
     * port, add it to the running sum, and produce the result at the
     * <i>output</i> port.  If there is no input token available,
     * the current value of the running sum is produced at the output.
     * If there is a true-valued token on the <i>reset</i> input,
     * then the running sum is reset to the initial value before
     * adding the input.
     * @exception IllegalActionException If addition is not
     * supported by the supplied tokens.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        $ASSIGN$_latestSum(_sum);
        for (int i = 0; i < reset.getWidth(); i++) {
            if (reset.hasToken(i)) {
                BooleanToken r = (BooleanToken)reset.get(i);
                if (r.booleanValue()) {
                    $ASSIGN$_latestSum(output.getType().convert(init.getToken()));
                }
            }
        }
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token in = input.get(i);
                $ASSIGN$_latestSum(_latestSum.add(in));
            }
        }
        Token lowerBoundValue = lowerBound.getToken();
        if (lowerBoundValue != null) {
            if (lowerBoundValue instanceof ScalarToken) {
                if (((ScalarToken)lowerBoundValue).isGreaterThan((ScalarToken)_latestSum).booleanValue()) {
                    $ASSIGN$_latestSum(lowerBoundValue);
                }
            } else {
                throw new IllegalActionException(this, "lowerBound parameter only works with scalar values. Value given was: " + lowerBoundValue);
            }
        }
        Token upperBoundValue = upperBound.getToken();
        if (upperBoundValue != null) {
            if (upperBoundValue instanceof ScalarToken) {
                if (((ScalarToken)upperBoundValue).isLessThan((ScalarToken)_latestSum).booleanValue()) {
                    $ASSIGN$_latestSum(upperBoundValue);
                }
            } else {
                throw new IllegalActionException(this, "upperBound parameter only works with scalar values. Value given was: " + upperBoundValue);
            }
        }
        output.broadcast(_latestSum);
    }

    /**
     * Reset the running sum to equal the value of <i>init</i>.
     * @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_latestSum($ASSIGN$_sum(output.getType().convert(init.getToken())));
    }

    /**
     * Record the most recent input as part of the running average.
     * Do nothing if there is no input.
     * @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException  {
        $ASSIGN$_sum(_latestSum);
        return super.postfire();
    }

    private final Token $ASSIGN$_sum(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_sum.add(null, _sum, $CHECKPOINT.getTimestamp());
        }
        return _sum = newValue;
    }

    private final Token $ASSIGN$_latestSum(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_latestSum.add(null, _latestSum, $CHECKPOINT.getTimestamp());
        }
        return _latestSum = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _sum = (Token)$RECORD$_sum.restore(_sum, timestamp, trim);
        _latestSum = (Token)$RECORD$_latestSum.restore(_latestSum, timestamp, trim);
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

    private transient FieldRecord $RECORD$_sum = new FieldRecord(0);

    private transient FieldRecord $RECORD$_latestSum = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_sum,
            $RECORD$_latestSum
        };

}

