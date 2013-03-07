/* A Delay Line with ArrayToken output.

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
//// DelayLine
package ptolemy.backtrack.automatic.ptolemy.domains.sdf.lib;

import java.lang.Object;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * This actor reads tokens from its input port, and for each token read
 * outputs an array that contains the current token as the first token,
 * followed by some number of previously read tokens.  The length of the
 * output array is the same as that of the array in the <i>initialValues</i>
 * parameter.  That parameter also provides the initial values when there
 * are no previously read tokens.
 * <p>
 * Note that this actor is not a simple sample delay.
 * Use the SampleDelay actor to achieve a simple sample delay.
 * @see ptolemy.domains.sdf.lib.SampleDelay
 * @author Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Yellow (yuhong)
 * @Pt.AcceptedRating Yellow (neuendor)
 */
public class DelayLine extends SDFTransformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    // set the output type to be an ArrayType, and input type to
    // be the corresponding token type.
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /**
     * The initial values of the delay line.
     * This parameter must contain an ArrayToken.
     * The default value is an array that contains 4 integer tokens.
     * Changes to this parameter after initialize() has been invoked
     * are ignored until the next execution of the model.
     */
    public Parameter initialValues;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // Set flag to invalidate cached type constraints
    // Shift down.
    // Read the next input.
    // output the output token.
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The delay line.
     */
    private Token[] _delayLine = null;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public DelayLine(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        initialValues = new Parameter(this, "initialValues");
        initialValues.setExpression("{0, 0, 0, 0}");
        output.setTypeAtLeast(ArrayType.arrayOf(input));
        output.setTypeAtLeast(initialValues);
    }

    /**
     * Override the base class to allow type changes on
     * <i>initialValues</i>.
     * @exception IllegalActionException If type changes are not
     * allowed on the specified attribute.
     */
    public void attributeTypeChanged(Attribute attribute) throws IllegalActionException  {
        if (attribute != initialValues) {
            super.attributeTypeChanged(attribute);
        } else {
            _typesValid = false;
        }
    }

    /**
     * Clone the actor into the specified workspace. This overrides the
     * base class to handle type constraints.
     * @param workspace The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException If a derived class contains
     * an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException  {
        DelayLine newObject = (DelayLine)super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType.arrayOf(newObject.input));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        newObject.output.setTypeAtLeast(newObject.initialValues);
        return newObject;
    }

    /**
     * Consume a token from the input, push it onto the delay line
     * and produce the output ArrayToken containing the current state of
     * the delay line.
     * @exception IllegalActionException If not enough tokens are available.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        System.arraycopy($BACKUP$_delayLine(), 0, $BACKUP$_delayLine(), 1, _delayLine.length - 1);
        $ASSIGN$_delayLine(0, input.get(0));
        output.send(0, new ArrayToken($BACKUP$_delayLine()));
    }

    /**
     * Initialize this actor by reading the value of <i>initialValues</i>.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_delayLine(((ArrayToken)initialValues.getToken()).arrayValue());
    }

    private final Token $ASSIGN$_delayLine(int index0, Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_delayLine.add(new int[] {
                    index0
                }, _delayLine[index0], $CHECKPOINT.getTimestamp());
        }
        return _delayLine[index0] = newValue;
    }

    private final Token[] $ASSIGN$_delayLine(Token[] newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_delayLine.add(null, _delayLine, $CHECKPOINT.getTimestamp());
        }
        return _delayLine = newValue;
    }

    private final Token[] $BACKUP$_delayLine() {
        $RECORD$_delayLine.backup(null, _delayLine, $CHECKPOINT.getTimestamp());
        return _delayLine;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _delayLine = (Token[])$RECORD$_delayLine.restore(_delayLine, timestamp, trim);
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

    private transient FieldRecord $RECORD$_delayLine = new FieldRecord(1);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_delayLine
        };

}

