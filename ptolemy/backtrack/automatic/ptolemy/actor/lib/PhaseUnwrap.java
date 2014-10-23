/* A simple phase unwrapper.

 Copyright (c) 1990-2014 The Regents of the University of California.
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
//// PhaseUnwrap
package ptolemy.backtrack.automatic.ptolemy.actor.lib;

import java.lang.Object;
import ptolemy.actor.lib.Transformer;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.backtrack.util.CheckpointRecord;
import ptolemy.backtrack.util.FieldRecord;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This actor unwraps a phase plot, removing discontinuities of
 * magnitude 2*PI. The input is assumed to be a sequence of phases in
 * radians in the range [-PI, PI].  The input and output types
 * are double.  This actor assumes that the phase never
 * changes by more than PI in one sample period. This is not a very
 * sophisticated phase unwrapper, but for many applications, it does
 * the job.
 * @author Joe Buck and Edward A. Lee and Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating Yellow (celaine)
 * @Pt.AcceptedRating Yellow (celaine)
 */
public class PhaseUnwrap extends Transformer implements Rollbackable {

    protected transient Checkpoint $CHECKPOINT = new Checkpoint(this);

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    // compute the phase change and check for wraparound
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The value of the input in the previous phase.  Needed to work
    // in the CT domain.
    // The value of the input in the previous phase.
    // The value of the output in the previous phase.  Needed to work
    // in the CT domain.
    // The value of the output in the previous phase.
    private double _previousPhaseInput = 0.0;

    private double _tempPreviousPhaseInput = 0.0;

    private double _previousPhaseOutput = 0.0;

    private double _tempPreviousPhaseOutput = 0.0;

    /**
     * Construct an actor with the given container and name.
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the actor cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    public PhaseUnwrap(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    /**
     * Consume at most one input token and output a value that
     * represents the same angle, but differs from the previous output
     * (or from 0.0, if this is the first output), by less than 2*PI.
     * If there is no input token, then no output is produced.
     * @exception IllegalActionException If there is no director.
     */
    @Override public void fire() throws IllegalActionException  {
        super.fire();
        if (input.hasToken(0)) {
            double newPhase = ((DoubleToken)input.get(0)).doubleValue();
            double phaseChange = newPhase - _previousPhaseInput;
            if (phaseChange < -Math.PI) {
                phaseChange += 2 * Math.PI;
            }
            if (phaseChange > Math.PI) {
                phaseChange -= 2 * Math.PI;
            }
            $ASSIGN$_tempPreviousPhaseOutput(_previousPhaseOutput + phaseChange);
            $ASSIGN$_tempPreviousPhaseInput(newPhase);
            output.send(0, new DoubleToken(_tempPreviousPhaseOutput));
        }
    }

    /**
     * Reset the state of the actor to assume the most recently seen
     * phase is zero.
     * @exception IllegalActionException If the parent class throws it.
     */
    @Override public void initialize() throws IllegalActionException  {
        super.initialize();
        $ASSIGN$_previousPhaseInput(0.0);
        $ASSIGN$_previousPhaseOutput(0.0);
        $ASSIGN$_tempPreviousPhaseInput(0.0);
        $ASSIGN$_tempPreviousPhaseOutput(0.0);
    }

    /**
     * Record the final value of the most recent value of the input,
     * for use in the next phase.
     * @exception IllegalActionException If the base class throws it.
     */
    @Override public boolean postfire() throws IllegalActionException  {
        $ASSIGN$_previousPhaseInput(_tempPreviousPhaseInput);
        $ASSIGN$_previousPhaseOutput(_tempPreviousPhaseOutput);
        return super.postfire();
    }

    private final double $ASSIGN$_previousPhaseInput(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_previousPhaseInput.add(null, _previousPhaseInput, $CHECKPOINT.getTimestamp());
        }
        return _previousPhaseInput = newValue;
    }

    private final double $ASSIGN$_tempPreviousPhaseInput(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_tempPreviousPhaseInput.add(null, _tempPreviousPhaseInput, $CHECKPOINT.getTimestamp());
        }
        return _tempPreviousPhaseInput = newValue;
    }

    private final double $ASSIGN$_previousPhaseOutput(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_previousPhaseOutput.add(null, _previousPhaseOutput, $CHECKPOINT.getTimestamp());
        }
        return _previousPhaseOutput = newValue;
    }

    private final double $ASSIGN$_tempPreviousPhaseOutput(double newValue) {
        if ($CHECKPOINT != null && $CHECKPOINT.getTimestamp() > 0) {
            $RECORD$_tempPreviousPhaseOutput.add(null, _tempPreviousPhaseOutput, $CHECKPOINT.getTimestamp());
        }
        return _tempPreviousPhaseOutput = newValue;
    }

    public void $COMMIT(long timestamp) {
        FieldRecord.commit($RECORDS, timestamp, $RECORD$$CHECKPOINT.getTopTimestamp());
        $RECORD$$CHECKPOINT.commit(timestamp);
    }

    public void $RESTORE(long timestamp, boolean trim) {
        _previousPhaseInput = $RECORD$_previousPhaseInput.restore(_previousPhaseInput, timestamp, trim);
        _tempPreviousPhaseInput = $RECORD$_tempPreviousPhaseInput.restore(_tempPreviousPhaseInput, timestamp, trim);
        _previousPhaseOutput = $RECORD$_previousPhaseOutput.restore(_previousPhaseOutput, timestamp, trim);
        _tempPreviousPhaseOutput = $RECORD$_tempPreviousPhaseOutput.restore(_tempPreviousPhaseOutput, timestamp, trim);
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

    private transient FieldRecord $RECORD$_previousPhaseInput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_tempPreviousPhaseInput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_previousPhaseOutput = new FieldRecord(0);

    private transient FieldRecord $RECORD$_tempPreviousPhaseOutput = new FieldRecord(0);

    private transient FieldRecord[] $RECORDS = new FieldRecord[] {
            $RECORD$_previousPhaseInput,
            $RECORD$_tempPreviousPhaseInput,
            $RECORD$_previousPhaseOutput,
            $RECORD$_tempPreviousPhaseOutput
        };

}

