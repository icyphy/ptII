/* Merge streams according to a boolean control signal.

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

 (This is similar to Select and BooleanMultiplexor and could be
 design/code reviewed at the same time.
 */
///////////////////////////////////////////////////////////////////
//// BooleanSelect
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 * Conditionally merge the streams at two input ports
 * depending on the value of the boolean control input.
 * In the first firing, this actor consumes a token from the
 * <i>control</i> input port.
 * The token at the <i>control</i> input specifies the
 * input port that should be read from in the next firing.
 * If the <i>control</i>
 * token is false, then the <i>falseInput</i> port is used,
 * otherwise the <i>trueInput</i> port is used. In the next
 * firing, tokens are consumed from the specified
 * port and sent to the <i>output</i> port.
 * <p>
 * The actor is able to fire if either it needs a new control
 * token and there is a token on the <i>control</i> port, or
 * it has read a control token and there is a token on every
 * channel of the specified input port.
 * <p>
 * If the input port that is read has width greater than an output port, then
 * some input tokens will be discarded (those on input channels for which
 * there is no corresponding output channel).
 * <p>
 * Because tokens are immutable, the same Token is sent
 * to the output, rather than a copy.  The <i>trueInput</i> and
 * <i>falseInput</i> port may receive Tokens of any type.
 * <p>
 * This actor is designed to be used with the DDF or PN director.
 * It should not be used with
 * SDF because the number of tokens it consumes is not fixed.
 * It probably also does not make sense to use it
 * with SR or DE, because it takes two firings to transfer
 * a token to the output. In those domains,{
@link BooleanMultiplexor}
 makes more sense.
 * Unlike BooleanMultiplexor actor, this actor
 * does not discard input tokens on the port that it does not read.
 * @author Steve Neuendorffer, Adam Cataldo, Edward A. Lee, Gang Zhou
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Green (neuendor)
 * @Pt.AcceptedRating Red (neuendor)
 */
public class BooleanSelect extends TypedAtomicActor implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // Put the control input on the bottom of the actor.
    // For the benefit of the DDF director, this actor sets
    // consumption rate values.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * Input for tokens on the true path.  The type can be anything.
     */
    public TypedIOPort trueInput;

    /**
     * Input for tokens on the false path.  The type can be anything.
     */
    public TypedIOPort falseInput;

    /**
     * Input that selects one of the other input ports.  The type is
     * BooleanToken.
     */
    public TypedIOPort control;

    /**
     * The output port.  The type is at least the type of
     * <i>trueInput</i> and <i>falseInput</i>
     */
    public TypedIOPort output;

    /**
     * This parameter provides token consumption rate for <i>trueInput</i>.
     * The type is int.
     */
    public Parameter trueInput_tokenConsumptionRate;

    /**
     * This parameter provides token consumption rate for <i>falseInput</i>.
     * The type is int.
     */
    public Parameter falseInput_tokenConsumptionRate;

    /**
     * This parameter provides token consumption rate for <i>control</i>.
     * The type is int.
     */
    public Parameter control_tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // If on this iteration the control token was used, set it to null.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The most recently read control token.
     */
    private BooleanToken _control = null;

    /**
     * Indicator that the control token was used in the fire method.
     */
    private boolean _controlUsed = false;

    /**
     * A final static IntToken with value 0.
     */
    private final static IntToken _zero = new IntToken(0);

    /**
     * A final static IntToken with value 1.
     */
    private final static IntToken _one = new IntToken(1);

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
    public BooleanSelect(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException  {
        super(container, name);
        trueInput = new TypedIOPort(this, "trueInput", true, false);
        trueInput.setMultiport(true);
        falseInput = new TypedIOPort(this, "falseInput", true, false);
        falseInput.setMultiport(true);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(trueInput);
        output.setTypeAtLeast(falseInput);
        output.setMultiport(true);
        output.setWidthEquals(trueInput, true);
        output.setWidthEquals(falseInput, true);
        StringAttribute controlCardinal = new StringAttribute(control, "_cardinal");
        controlCardinal.setExpression("SOUTH");
        trueInput_tokenConsumptionRate = new Parameter(trueInput, "tokenConsumptionRate");
        trueInput_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        trueInput_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        falseInput_tokenConsumptionRate = new Parameter(falseInput, "tokenConsumptionRate");
        falseInput_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        falseInput_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        control_tokenConsumptionRate = new Parameter(control, "tokenConsumptionRate");
        control_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        control_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"-20\" y=\"-20\" "+"width=\"40\" height=\"40\" "+"style=\"fill:white\"/>\n"+"<text x=\"-17\" y=\"-3\" "+"style=\"font-size:14\">\n"+"T \n"+"</text>\n"+"<text x=\"-17\" y=\"15\" "+"style=\"font-size:14\">\n"+"F \n"+"</text>\n"+"<text x=\"-5\" y=\"16\" "+"style=\"font-size:14\">\n"+"C \n"+"</text>\n"+"</svg>\n");
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
    @Override public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        BooleanSelect newObject = (BooleanSelect)super.clone(workspace);
        newObject.$ASSIGN$_control(null);
        newObject.$ASSIGN$_controlUsed(false);
        newObject.output.setTypeAtLeast(newObject.trueInput);
        newObject.output.setTypeAtLeast(newObject.falseInput);
        newObject.output.setWidthEquals(newObject.trueInput, true);
        newObject.output.setWidthEquals(newObject.falseInput, true);
        return newObject;
    }

    /**
     * Read a token from the control port or from the input designated
     * by the previously read input from the control port.  In the
     * latter case, send to the token read to the output. In the former
     * case, send nothing to the output.
     * @exception IllegalActionException If there is no director.
     */
    @Override public void fire() throws IllegalActionException  {
        super.fire();
        if (_control == null) {
            $ASSIGN$_control((BooleanToken)control.get(0));
            $ASSIGN$_controlUsed(false);
        } else {
            if (_control.booleanValue()) {
                for (int i = 0; i < trueInput.getWidth(); i++) {
                    if (output.getWidth() > i) {
                        output.send(i, trueInput.get(i));
                    }
                }
            } else {
                for (int i = 0; i < falseInput.getWidth(); i++) {
                    if (output.getWidth() > i) {
                        output.send(i, falseInput.get(i));
                    }
                }
            }
            $ASSIGN$_controlUsed(true);
        }
    }

    /**
     * Initialize this actor so that the <i>falseInput</i> is read
     * from until a token arrives on the <i>control</i> input.
     * @exception IllegalActionException If the parent class throws it.
     */
    @Override public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_control(null);
        $ASSIGN$_controlUsed(false);
        trueInput_tokenConsumptionRate.setToken(_zero);
        falseInput_tokenConsumptionRate.setToken(_zero);
        control_tokenConsumptionRate.setToken(_one);
    }

    /**
     * Return true, unless stop() has been called, in which case,
     * return false.
     * @return True if execution can continue into the next iteration.
     * @exception IllegalActionException If the base class throws it.
     */
    @Override public boolean postfire() throws IllegalActionException  {
        if (_controlUsed) {
            $ASSIGN$_control(null);
            trueInput_tokenConsumptionRate.setToken(_zero);
            falseInput_tokenConsumptionRate.setToken(_zero);
            control_tokenConsumptionRate.setToken(_one);
        } else {
            if (_control == null) {
                $ASSIGN$_control((BooleanToken)control.get(0));
            }
            if (_control.booleanValue()) {
                trueInput_tokenConsumptionRate.setToken(_one);
                falseInput_tokenConsumptionRate.setToken(_zero);
                control_tokenConsumptionRate.setToken(_zero);
            } else {
                trueInput_tokenConsumptionRate.setToken(_zero);
                falseInput_tokenConsumptionRate.setToken(_one);
                control_tokenConsumptionRate.setToken(_zero);
            }
        }
        return super.postfire();
    }

    /**
     * If the mode is to read a control token, then return true
     * if the <i>control</i> input has a token. Otherwise, return
     * true if every channel of the input port specified by the most
     * recently read control input has a token.
     * @return False if there are not enough tokens to fire.
     * @exception IllegalActionException If there is no director.
     */
    @Override public boolean prefire() throws IllegalActionException  {
        boolean result = super.prefire();
        if (_control == null) {
            return result && control.hasToken(0);
        } else {
            if (_control.booleanValue()) {
                for (int i = 0; i < trueInput.getWidth(); i++) {
                    if (!trueInput.hasToken(i)) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < falseInput.getWidth(); i++) {
                    if (!falseInput.hasToken(i)) {
                        return false;
                    }
                }
            }
        }
        return result;
    }

    private final BooleanToken $ASSIGN$_control(BooleanToken newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_control.add(null, _control, $CHECKPOINT.getTimestamp());
        }
        return _control = newValue;
    }

    private final boolean $ASSIGN$_controlUsed(boolean newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_controlUsed.add(null, _controlUsed, $CHECKPOINT.getTimestamp());
        }
        return _controlUsed = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _control = (BooleanToken)$RECORD$_control.restore(_control, timestamp, trim);
        _controlUsed = $RECORD$_controlUsed.restore(_controlUsed, timestamp, trim);
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

    private transient FieldRecord $RECORD$_controlUsed = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_control,
            $RECORD$_controlUsed
        };

}

