/* An actor that outputs the difference between successive inputs.

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
//// Differential
///////////////////////////////////////////////////////////////////
////                         public methods                    ////
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Output the current input minus the previous input, or if there
 * has been no previous input, the current input itself.
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Differential extends Transformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private Token _currentInput;

    private Token _lastInput;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public Differential(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    /**
     * Consume at most one token from the <i>input</i> port and output
     * its value minus the value of the input read in the previous
     * iteration.  If there has been no previous iteration, then
     * output the current input.  If there is no input, then produce
     * no output.
     * @exception IllegalActionException If subtraction is not
     * supported by the supplied tokens.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        if (input.hasToken(0)) {
            $ASSIGN$_currentInput(input.get(0));
            if (_lastInput != null) {
                output.broadcast(_currentInput.subtract(_lastInput));
            } else {
                output.broadcast(_currentInput);
            }
        }
    }

    /**
     * Reset to indicate that no input has yet been seen.
     * @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_lastInput(null);
    }

    /**
     * Record the most recent input as the latest input.
     * @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException  {
        $ASSIGN$_lastInput(_currentInput);
        return super.postfire();
    }

    private final Token $ASSIGN$_currentInput(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_currentInput.add(null, _currentInput, $CHECKPOINT.getTimestamp());
        }
        return _currentInput = newValue;
    }

    private final Token $ASSIGN$_lastInput(Token newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_lastInput.add(null, _lastInput, $CHECKPOINT.getTimestamp());
        }
        return _lastInput = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _currentInput = (Token)$RECORD$_currentInput.restore(_currentInput, timestamp, trim);
        _lastInput = (Token)$RECORD$_lastInput.restore(_lastInput, timestamp, trim);
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

    private transient FieldRecord $RECORD$_currentInput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_lastInput = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_currentInput,
            $RECORD$_lastInput
        };

}

