/* A polymorphic select, which routes specified input channels to the output.

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

 Code review issue: This actor reads data in prefire.
 */
///////////////////////////////////////////////////////////////////
//// Select
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

/**
 * <p>A polymorphic select, which routes specified input channels to the
 * output.  This actor has two input ports, the <i>input</i> port for
 * data, and the <i>control</i> port to select which input channel to
 * read.  In an iteration, if an input token is available at the
 * <i>control</i> input, that token is read, and its value is noted.  Its
 * value specifies the input channel that should be read next. If an
 * input token is available on the specified channel of the <i>input</i>
 * port, then that token is read and sent to the output.
 * </p><p> The actor indicates a willingness to fire in its prefire() method
 * if there is an input available on the channel specified by the most
 * recently seen token on the <i>control</i> port.  If no token has ever
 * been received on the <i>control</i> port, then channel zero is assumed
 * to be the one to read.  If the value of the most recently received
 * token on the <i>control</i> port is out of range (less than zero, or
 * greater than or equal to the width of the input), then the actor will
 * not fire() (although it will continue to consume tokens on the
 * <i>control</i> port in its prefire() method).
 * </p><p> This actor is similar to the {
@link Multiplexor}
 actor, except that it
 * never discards input tokens.  Tokens on channels that are not selected
 * are not consumed.
 * </p><p> Note that in the DE domain, where this actor is commonly used, if
 * a new value is given to the <i>control</i> port, then all previously
 * unread input tokens on the specified input channel will be read at the
 * same firing time, in the order in which they arrived.</p>
 * <p>Note further that this actor is subtly different from the{
@link BooleanSelect}
 actor. In addition to the obvious difference
 * that the latter accepts only two input streams, the latter also
 * requires two firings to produce an output. The BooleanSelect actor
 * is designed to work with DDF, but because of the multiple firings,
 * will not work with DE or SR. This actor will, because it consumes
 * the control input and the data input in the same firing.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Green (neuendor)
 * @Pt.AcceptedRating Yellow (liuj)
 */
public class Select extends Transformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Put the control input on the bottom of the actor.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Input port for control tokens, which specify the output channel
     * to produce data on.  The type is int.
     */
    public TypedIOPort control;

    // Redo this check in case the control has changed since prefire().
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The most recently read control token.
    private int _control = 0;

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
    public Select(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        input.setMultiport(true);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);
        StringAttribute controlCardinal = new StringAttribute(control, "_cardinal");
        controlCardinal.setExpression("SOUTH");
    }

    /**
     * Read an input token from the specified input channel and produce
     * it on the output.
     * @exception IllegalActionException If there is no director.
     */
    @Override public void fire() throws IllegalActionException  {
        super.fire();
        if (input.hasToken(_control)) {
            output.send(0, input.get(_control));
        }
    }

    /**
     * Initialize this actor so that channel zero of <i>input</i> is read
     * from until a token arrives on the <i>control</i> input.
     * @exception IllegalActionException If the parent class throws it.
     */
    @Override public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_control(0);
    }

    /**
     * Read a control token, if there is one, and check to see
     * whether an input is available on the input channel specified by
     * the most recent control token, if it is in range.
     * Return false if there is no input token to read.
     * Otherwise, return whatever the superclass returns.
     * @return True if the actor is ready to fire.
     * @exception IllegalActionException If there is no director.
     */
    @Override public boolean prefire() throws IllegalActionException  {
        if (control.hasToken(0)) {
            $ASSIGN$_control(((IntToken)control.get(0)).intValue());
        }
        if (_control < 0 || _control > input.getWidth() || !input.hasToken(_control)) {
            return false;
        }
        return super.prefire();
    }

    private final int $ASSIGN$_control(int newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_control.add(null, _control, $CHECKPOINT.getTimestamp());
        }
        return _control = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _control = $RECORD$_control.restore(_control, timestamp, trim);
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

    private transient FieldRecord $RECORD$_control = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_control
        };

}

