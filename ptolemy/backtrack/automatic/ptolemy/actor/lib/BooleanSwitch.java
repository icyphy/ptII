/* Split a stream into two according to a boolean control signal.

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

 This is similar to Switch and could be design/code reviewed at the same time.
 */
///////////////////////////////////////////////////////////////////
//// BooleanSwitch
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 * Split an input stream onto two output ports depending on a
 * boolean control input.  In an
 * iteration, if an input token is available at the <i>control</i> input,
 * that token is read, and its value is noted.  Its value specifies the
 * output port that should be written to in this and subsequent iterations,
 * until another <i>control</i> input is provided. If no <i>control</i>
 * input is provided, then the inputs are routed to the <i>falseOutput</i> port.
 * In each iteration, at most one token on each channel of the <i>input</i> port
 * is read and sent to the corresponding channel of the
 * <i>trueOutput</i> port or the <i>falseOutput</i> port, depending on the
 * most recently received <i>control</i> input.
 * If the input has width greater than an output port, then
 * some input tokens will be discarded (those on input channels for which
 * there is no corresponding output channel).
 * Because tokens are
 * immutable, the same Token is sent to the output, rather than a copy.
 * The <i>input</i> port may receive Tokens of any type.
 * <p>Note that the this actor may be used in Synchronous Dataflow (SDF)
 * models, but only under certain circumstances. Specifically, downstream
 * actors will be fired whether a token is sent to them or not.
 * This will only work if the downstream actors specifically check to
 * see whether input tokens are available.
 * @author Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Green (neuendor)
 * @Pt.AcceptedRating Red (neuendor)
 */
public class BooleanSwitch extends TypedAtomicActor implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Put the control input on the bottom of the actor.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * Input that selects one of the other input ports.  The type is
     * boolean.
     */
    public TypedIOPort control;

    /**
     * The input port.  The type can be anything. This is a multiport,
     * and input tokens on all channels are routed to corresponding
     * channels on the output port, if there are such channels.
     */
    public TypedIOPort input;

    /**
     * Output for tokens on the true path.  The type is at least the
     * type of the input.
     */
    public TypedIOPort trueOutput;

    /**
     * Output for tokens on the false path.  The type is at least the
     * type of the input.
     */
    public TypedIOPort falseOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The most recently read control token.
    private boolean _control = false;

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
    public BooleanSwitch(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);
        trueOutput = new TypedIOPort(this, "trueOutput", false, true);
        falseOutput = new TypedIOPort(this, "falseOutput", false, true);
        trueOutput.setTypeAtLeast(input);
        falseOutput.setTypeAtLeast(input);
        trueOutput.setMultiport(true);
        falseOutput.setMultiport(true);
        trueOutput.setWidthEquals(input, true);
        falseOutput.setWidthEquals(input, true);
        StringAttribute controlCardinal = new StringAttribute(control, "_cardinal");
        controlCardinal.setExpression("SOUTH");
    }

    /**
     * Clone this actor into the specified workspace. The new actor is
     * <i>not</i> added to the directory of that workspace (you must do this
     * yourself if you want it there).
     * The result is a new actor with the same ports as the original, but
     * no connections and no container.  A container must be set before
     * much can be done with this actor.
     * @param workspace The workspace for the cloned object.
     * @exception CloneNotSupportedException If cloned ports cannot have
     * as their container the cloned entity (this should not occur), or
     * if one of the attributes cannot be cloned.
     * @return A new ComponentEntity.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        BooleanSwitch newObject = (BooleanSwitch)super.clone(workspace);
        newObject.trueOutput.setTypeAtLeast(newObject.input);
        newObject.falseOutput.setTypeAtLeast(newObject.input);
        newObject.trueOutput.setWidthEquals(newObject.input, true);
        newObject.falseOutput.setWidthEquals(newObject.input, true);
        return newObject;
    }

    /**
     * Read a token from each input port.  If the token from the
     * <i>control</i> input is true, then output the token consumed from the
     * <i>input</i> port on the <i>trueOutput</i> port,
     * otherwise output the token on the <i>falseOutput</i> port.
     * @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (control.hasToken(0)) {
            $ASSIGN$_control(((BooleanToken)control.get(0)).booleanValue());
        }
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                if (_control) {
                    if (i < trueOutput.getWidth()) {
                        trueOutput.send(i, token);
                    }
                } else {
                    if (i < falseOutput.getWidth()) {
                        falseOutput.send(i, token);
                    }
                }
            }
        }
    }

    /**
     * Initialize this actor so that the <i>falseOutput</i> is written
     * to until a token arrives on the <i>control</i> input.
     * @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_control(false);
    }

    private final boolean $ASSIGN$_control(boolean newValue) {
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

