/* A polymorphic multiplexor.

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

 */
///////////////////////////////////////////////////////////////////
//// Multiplexor
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
 * This actor selects from the channels on the
 * <i>input</i> port, copying the input from one channel to the output,
 * based on the most recently received value on the <i>select</i> input.
 * If the selected channel has no token, then no output is produced.
 * The <i>select</i> input is required to be an integer between 0 and
 * <i>n</i>-1, where <i>n</i> is the width of the <i>input</i> port.
 * If no token has been received on the <i>select</i> port, then null
 * is sent to the output.  The <i>input</i> port may
 * receive Tokens of any type, but all channels must have the same type.
 * <p>
 * One token is consumed from each input channel that has a token.
 * Compare this with the Select actor, which only consumes a token on
 * the selected channel.
 * @author Jeff Tsay, Edward A. Lee, Stavros Tripakis
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Yellow (ctsay)
 * @Pt.AcceptedRating Yellow (cxh)
 * @see ptolemy.actor.lib.Select
 */
public class Multiplexor extends Transformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Input for the index of the port to select. The type is IntToken.
     */
    public TypedIOPort select;

    // Be sure to not use _channel if the select input
    // is not known. That would be non-monotonic.
    // Perform the in-range test here, where a new channel value is obtained:
    // Be sure to read all inputs that are present, even
    // if they aren't required in order to produce output.
    // Tokens need to be consumed in dataflow and DE domains.
    // Note that if the input is known to be absent,
    // then the following sends null. Dataflow receivers
    // interpret this as sending nothing (nothing is queued).
    // Fixed-point receivers (SR and Continuous) interpret
    // this as an assertion that the output is absent.
    // If no select value has been seen, then we can
    // assert that the output is empty. Note that this is only
    // safe if the select input is known.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The most recently read select input.
     */
    private IntToken _selectChannel;

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
    public Multiplexor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        input.setMultiport(true);
        select = new TypedIOPort(this, "select", true, false);
        select.setTypeEquals(BaseType.INT);
        new StringAttribute(select, "_cardinal").setExpression("SOUTH");
    }

    /**
     * Read a token from the <i>select</i> port and from each channel
     * of the <i>input</i> port, and output a token on the selected
     * channel.  This method will throw a NoTokenException if any
     * input channel does not have a token.
     * @exception IllegalActionException If there is no director, or if
     * the <i>select</i> input is out of range.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (select.isKnown(0)) {
            if (select.hasToken(0)) {
                $ASSIGN$_selectChannel((IntToken)select.get(0));
                int c = _selectChannel.intValue();
                if (c < 0 || c >= input.getWidth()) {
                    throw new IllegalActionException(this, "Select input is out of range: " + c+".");
                }
            }
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.isKnown(i)) {
                    Token token = null;
                    if (input.hasToken(i)) {
                        token = input.get(i);
                    }
                    if (_selectChannel != null && _selectChannel.intValue() == i) {
                        output.send(0, token);
                    }
                }
            }
            if (_selectChannel == null) {
                output.send(0, null);
            }
        }
    }

    /**
     * Initialize to the default, which is to use channel zero.
     * @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_selectChannel(null);
    }

    /**
     * Return false.
     * @return False.
     */
    public boolean isStrict() {
        return false;
    }

    private final IntToken $ASSIGN$_selectChannel(IntToken newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_selectChannel.add(null, _selectChannel, $CHECKPOINT.getTimestamp());
        }
        return _selectChannel = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _selectChannel = (IntToken)$RECORD$_selectChannel.restore(_selectChannel, timestamp, trim);
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

    private transient FieldRecord $RECORD$_selectChannel = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_selectChannel
        };

}

