/* A polymorphic switch, which routes inputs to specified output channels.

 Copyright (c) 1997-2013 The Regents of the University of California.
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
//// Switch
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

/**
 * <p>A polymorphic switch, which routes inputs to specified output channels.
 * This actor has two input ports, the <i>input</i> port for data,
 * and the <i>control</i> port to select which output channel to use.
 * When it fires, if an input token is available at the <i>control</i>
 * input, that token is read, and its value is noted.  If an input
 * token is available on the <i>input</i> port, then that token is
 * read, sent to the output channel specified by the most recently
 * received value on the <i>control</i> port.  If no token has been
 * received on the <i>control</i> port, then the token is sent to
 * channel zero.  If the value of the most recently received token
 * on the <i>control</i> port is out of range (less than zero,
 * or greater than or equal to the width of the output), then no
 * output is produced, and the token is lost.
 * </p><p>
 * Note that it may be tempting to call an instance of this
 * class "switch", but recall that "switch" is a Java keyword, and
 * thus it cannot be the name of an object.</p>
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (cxh)
 */
public class Switch extends Transformer implements Rollbackable {

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
    public Switch(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        output.setMultiport(true);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);
        StringAttribute controlCardinal = new StringAttribute(control, "_cardinal");
        controlCardinal.setExpression("SOUTH");
    }

    /**
     * Read a control token, if there is one, and transfer an input
     * token, if there is one, to the output channel specified by
     * the most recent control token, if it is in range.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (control.hasToken(0)) {
            $ASSIGN$_control(((IntToken)control.get(0)).intValue());
        }
        if (input.hasToken(0)) {
            Token token = input.get(0);
            if (_control >= 0 && _control < output.getWidth()) {
                output.send(_control, token);
            }
        }
    }

    /**
     * Initialize this actor so that channel zero of <i>input</i> is read
     * from until a token arrives on the <i>control</i> input.
     * @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_control(0);
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

