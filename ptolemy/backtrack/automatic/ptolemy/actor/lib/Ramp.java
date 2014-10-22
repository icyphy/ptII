/* An actor that outputs a sequence with a given step in values.

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
//// Ramp
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.Manager;
import ptolemy.actor.lib.SequenceSource;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * Produce an output token on each firing with a value that is
 * incremented by the specified step each iteration. The
 * first output is given by the <i>init</i> parameter, and the
 * increment may be given either by the <i>step</i> parameter or by
 * the associated <i>step</i> port. Note that the increment will show
 * up in the output only on the next iteration. If you need it to show
 * up on the current iteration, use the{
@link ptolemy.actor.lib.Accumulator Accumulator}
 actor.
 * The type of the output is determined by the constraint that it must
 * be greater than or equal to the types of the parameter (and/or the
 * <i>step</i> port, if it is connected).
 * Thus, this actor is
 * polymorphic in the sense that its output data type can be that
 * of any token type that supports addition.
 * @see Accumulator
 * @author Yuhong Xiong, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 0.2
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (bilung)
 */
public class Ramp extends SequenceSource implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // set the type constraints.
    // Show the firingCountLimit parameter last.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The value produced by the ramp on its first iteration.
     * If this value is changed during execution, then the new
     * value will be the output on the next iteration.
     * The default value of this parameter is the integer 0.
     */
    public PortParameter init;

    /**
     * The amount by which the ramp output is incremented on each iteration.
     * The default value of this parameter is the integer 1.
     */
    public PortParameter step;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // If type resolution has happened (the model is running),
    // then update the current state.
    // set the type constraints.
    // Check whether we need to reallocate the output token array.
    // Consume any trigger inputs.
    // NOTE: It might seem that using trigger.numberOfSources() is
    // correct here, but it is not. It is possible for channels
    // to be connected, for example, to other output ports or
    // even back to this same trigger port, in which case higher
    // numbered channels will not have their inputs read.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Token _stateToken = null;

    private Token[] _resultArray;

    /**
     * Construct an actor with the given container and name.
     * In addition to invoking the base class constructors, construct
     * the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     * port. Initialize <i>init</i>
     * to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Ramp(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        init = new PortParameter(this, "init");
        init.setExpression("0");
        new Parameter(init.getPort(), "_showName", BooleanToken.TRUE);
        step = new PortParameter(this, "step");
        step.setExpression("1");
        new Parameter(step.getPort(), "_showName", BooleanToken.TRUE);
        output.setTypeAtLeast(init);
        output.setTypeAtLeast(step);
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-30\" y=\"-20\" "+"width=\"60\" height=\"40\" "+"style=\"fill:white\"/>\n"+"<polygon points=\"-20,10 20,-10 20,10\" "+"style=\"fill:grey\"/>\n"+"</svg>\n");
        $ASSIGN$_resultArray(new Token[1]);
        firingCountLimit.moveToLast();
    }

    /**
     * If the argument is the <i>init</i> parameter, then reset the
     * state to the specified value.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If <i>init<i> cannot be evaluated
     * or cannot be converted to the output type, or if the superclass
     * throws it.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute == init) {
            Manager manager = getManager();
            if (manager != null) {
                Manager.State state = manager.getState();
                if (state == Manager.ITERATING || state == Manager.PAUSED || state == Manager.PAUSED_ON_BREAKPOINT) {
                    $ASSIGN$_stateToken(output.getType().convert(init.getToken()));
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Clone the actor into the specified workspace. This calls the
     * base class and then sets the <code>init</code> and <code>step</code>
     * public members to the parameters of the new actor.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        Ramp newObject = (Ramp)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.init);
        newObject.output.setTypeAtLeast(newObject.step);
        $ASSIGN$_resultArray(new Token[1]);
        return newObject;
    }

    /**
     * Send the current value of the state of this actor to the output.
     * @exception IllegalActionException If calling send() or super.fire()
     * throws it.
     */
    public void fire() throws IllegalActionException  {
        init.update();
        super.fire();
        output.send(0, _stateToken);
    }

    /**
     * Set the state to equal the value of the <i>init</i> parameter.
     * The state is incremented by the value of the <i>step</i>
     * parameter on each iteration (in the postfire() method).
     * @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_stateToken(output.getType().convert(init.getToken()));
    }

    /**
     * Invoke a specified number of iterations of this actor. Each
     * iteration updates the state of the actor by adding the
     * value of the <i>step</i> parameter to the state and sending
     * the value of the state to the output. The iteration count
     * is also incremented by the value of <i>count</i>, and if
     * the result is greater than or equal to <i>firingCountLimit</i>
     * then return STOP_ITERATING.
     * <p>
     * This method should be called instead of the usual prefire(),
     * fire(), postfire() methods when this actor is used in a
     * domain that supports vectorized actors.  This leads to more
     * efficient execution.
     * @param count The number of iterations to perform.
     * @return COMPLETED if the actor was successfully iterated the
     * specified number of times. Otherwise, if the maximum
     * iteration count has been reached, return STOP_ITERATING.
     * @exception IllegalActionException If iterating cannot be
     * performed.
     */
    public int iterate(int count) throws IllegalActionException  {
        if (count > _resultArray.length) {
            $ASSIGN$_resultArray(new Token[count]);
        }
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i, count)) {
                trigger.get(i, count);
            }
        }
        for (int i = 0; i < count; i++) {
            $ASSIGN$_resultArray(i, _stateToken);
            try {
                step.update();
                init.update();
                $ASSIGN$_stateToken(_stateToken.add(step.getToken()));
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(this, ex, "Should not be thrown because we have already " + "verified that the tokens can be added");
            }
        }
        output.send(0, $BACKUP$_resultArray(), count);
        if (_firingCountLimit != 0) {
            _iterationCount += count;
            if (_iterationCount >= _firingCountLimit) {
                return STOP_ITERATING;
            }
        }
        return COMPLETED;
    }

    /**
     * Update the state of the actor by adding the value of the
     * <i>step</i> parameter to the state.  Also, increment the
     * iteration count, and if the result is equal to
     * <i>firingCountLimit</i>, then
     * return false.
     * @return False if the number of iterations matches the number requested.
     * @exception IllegalActionException If the firingCountLimit parameter
     * has an invalid expression.
     */
    public boolean postfire() throws IllegalActionException  {
        step.update();
        $ASSIGN$_stateToken(_stateToken.add(step.getToken()));
        return super.postfire();
    }

    private final Token $ASSIGN$_stateToken(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_stateToken.add(null, _stateToken, $CHECKPOINT.getTimestamp());
        }
        return _stateToken = newValue;
    }

    private final Token[] $ASSIGN$_resultArray(Token[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_resultArray.add(null, _resultArray, $CHECKPOINT.getTimestamp());
        }
        return _resultArray = newValue;
    }

    private final Token $ASSIGN$_resultArray(int index0, Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_resultArray.add(new int[] {
                    index0
                }, _resultArray[index0], $CHECKPOINT.getTimestamp());
        }
        return _resultArray[index0] = newValue;
    }

    private final Token[] $BACKUP$_resultArray() {
        $RECORD$_resultArray.backup(null, _resultArray, $CHECKPOINT.getTimestamp());
        return _resultArray;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _stateToken = (Token)$RECORD$_stateToken.restore(_stateToken, timestamp, trim);
        _resultArray = (Token[])$RECORD$_resultArray.restore(_resultArray, timestamp, trim);
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

    private transient FieldRecord $RECORD$_stateToken = new FieldRecord(0);

    private transient FieldRecord $RECORD$_resultArray = new FieldRecord(1);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_stateToken,
            $RECORD$_resultArray
        };

}

