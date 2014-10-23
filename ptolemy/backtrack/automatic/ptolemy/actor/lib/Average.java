/* An actor that outputs the average of the inputs so far.

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
//// Average
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

/**
 * <p>Output the average of the inputs after the last time a true token is
 * received at the reset port.
 * One output is produced each time the actor is fired.
 * The inputs and outputs can be any token type that
 * supports addition and division by an integer.  The output type is
 * constrained to be the same as the input type.
 * Note that if the input is an integer, then the output is an
 * integer, which may not be what you want. You may need to set
 * the input and output ports to double to force the result to be
 * a double.</p>
 * <p>
 * Note that the type system will fail to catch some errors. Static type
 * checking may result in a resolved type that does not support addition
 * and division.  In this case, a run-time error will occur.
 * </p>
 * @author Edward A. Lee, Jie Liu
 * @version $Id$
 * @since Ptolemy II 0.3
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (bilung)
 */
public class Average extends Transformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The reset port of type BooleanToken. If this input port
     * receives a True token, then the averaging process will be
     * reset.
     */
    public TypedIOPort reset;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Check whether to reset.
    // Being reset at this firing.
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private Token _sum;

    private Token _latestSum;

    private int _count = 0;

    private int _latestCount;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Average(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        reset = new TypedIOPort(this, "reset", true, false);
        reset.setTypeEquals(BaseType.BOOLEAN);
        new StringAttribute(reset, "_cardinal").setExpression("SOUTH");
        new SingletonParameter(reset, "_showName").setToken(BooleanToken.TRUE);
    }

    /**
     * Consume at most one token from the <i>input</i>
     * and compute the average of the input tokens so far. Send the
     * result to the output.  If there is no input token available,
     * no output will be produced.  If there is a true-valued token
     * on the <i>reset</i> input, then the average is reset, and
     * the output will be equal to the <i>input</i> token (if there
     * is one). If the fire method
     * is invoked multiple times in one iteration, then only the
     * input read on the last invocation in the iteration will affect
     * future averages.  Inputs that are read earlier in the iteration
     * are forgotten.
     * @exception IllegalActionException If addition or division by an
     * integer are not supported by the supplied tokens.
     */
    @Override public void fire() throws IllegalActionException  {
        super.fire();
        $ASSIGN$_latestSum(_sum);
        $ASSIGN$_latestCount(_count);
        for (int i = 0; i < reset.getWidth(); i++) {
            if (reset.hasToken(i)) {
                BooleanToken r = (BooleanToken)reset.get(i);
                if (r.booleanValue()) {
                    $ASSIGN$_latestSum(null);
                    $ASSIGN$_latestCount(0);
                }
            }
        }
        if (input.hasToken(0)) {
            Token in = input.get(0);
            $ASSIGN$SPECIAL$_latestCount(11, _latestCount);
            if (_latestSum == null) {
                $ASSIGN$_latestSum(in);
            } else {
                $ASSIGN$_latestSum(_latestSum.add(in));
            }
            Token out = _latestSum.divide(new IntToken(_latestCount));
            output.broadcast(out);
        }
    }

    /**
     * Reset the count of inputs.
     * @exception IllegalActionException If the parent class throws it.
     */
    @Override public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_count(0);
        $ASSIGN$_sum(null);
    }

    /**
     * Record the most recent input as part of the running average.
     * Do nothing if there is no input.
     * @exception IllegalActionException If the base class throws it.
     */
    @Override public boolean postfire() throws IllegalActionException  {
        $ASSIGN$_sum(_latestSum);
        $ASSIGN$_count(_latestCount);
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

    private final int $ASSIGN$_count(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_count.add(null, _count, $CHECKPOINT.getTimestamp());
        }
        return _count = newValue;
    }

    private final int $ASSIGN$_latestCount(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_latestCount.add(null, _latestCount, $CHECKPOINT.getTimestamp());
        }
        return _latestCount = newValue;
    }

    private final int $ASSIGN$SPECIAL$_latestCount(int operator, long newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_latestCount.add(null, _latestCount, $CHECKPOINT.getTimestamp());
        }
        switch (operator) {
            case 0:
                return _latestCount += newValue;
            case 1:
                return _latestCount -= newValue;
            case 2:
                return _latestCount *= newValue;
            case 3:
                return _latestCount /= newValue;
            case 4:
                return _latestCount &= newValue;
            case 5:
                return _latestCount |= newValue;
            case 6:
                return _latestCount ^= newValue;
            case 7:
                return _latestCount %= newValue;
            case 8:
                return _latestCount <<= newValue;
            case 9:
                return _latestCount >>= newValue;
            case 10:
                return _latestCount >>>= newValue;
            case 11:
                return _latestCount++;
            case 12:
                return _latestCount--;
            case 13:
                return ++_latestCount;
            case 14:
                return --_latestCount;
            default:
                return _latestCount;
        }
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _sum = (Token)$RECORD$_sum.restore(_sum, timestamp, trim);
        _latestSum = (Token)$RECORD$_latestSum.restore(_latestSum, timestamp, trim);
        _count = $RECORD$_count.restore(_count, timestamp, trim);
        _latestCount = $RECORD$_latestCount.restore(_latestCount, timestamp, trim);
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

    private transient FieldRecord $RECORD$_count = new FieldRecord(0);

    private transient FieldRecord $RECORD$_latestCount = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_sum,
            $RECORD$_latestSum,
            $RECORD$_count,
            $RECORD$_latestCount
        };

}

