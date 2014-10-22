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
package ptolemy.actor.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PhaseUnwrap

/**

 This actor unwraps a phase plot, removing discontinuities of
 magnitude 2*PI. The input is assumed to be a sequence of phases in
 radians in the range [-PI, PI].  The input and output types
 are double.  This actor assumes that the phase never
 changes by more than PI in one sample period. This is not a very
 sophisticated phase unwrapper, but for many applications, it does
 the job.

 @author Joe Buck and Edward A. Lee and Elaine Cheong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (celaine)
 */
public class PhaseUnwrap extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PhaseUnwrap(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one input token and output a value that
     *  represents the same angle, but differs from the previous output
     *  (or from 0.0, if this is the first output), by less than 2*PI.
     *  If there is no input token, then no output is produced.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            double newPhase = ((DoubleToken) input.get(0)).doubleValue();

            // compute the phase change and check for wraparound
            double phaseChange = newPhase - _previousPhaseInput;

            if (phaseChange < -Math.PI) {
                phaseChange += 2 * Math.PI;
            }

            if (phaseChange > Math.PI) {
                phaseChange -= 2 * Math.PI;
            }

            _tempPreviousPhaseOutput = _previousPhaseOutput + phaseChange;
            _tempPreviousPhaseInput = newPhase;
            output.send(0, new DoubleToken(_tempPreviousPhaseOutput));
        }
    }

    /** Reset the state of the actor to assume the most recently seen
     *  phase is zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _previousPhaseInput = 0.0;
        _previousPhaseOutput = 0.0;
        _tempPreviousPhaseInput = 0.0;
        _tempPreviousPhaseOutput = 0.0;
    }

    /** Record the final value of the most recent value of the input,
     *  for use in the next phase.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _previousPhaseInput = _tempPreviousPhaseInput;
        _previousPhaseOutput = _tempPreviousPhaseOutput;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The value of the input in the previous phase.  Needed to work
    // in the CT domain.
    private double _previousPhaseInput = 0.0;

    // The value of the input in the previous phase.
    private double _tempPreviousPhaseInput = 0.0;

    // The value of the output in the previous phase.  Needed to work
    // in the CT domain.
    private double _previousPhaseOutput = 0.0;

    // The value of the output in the previous phase.
    private double _tempPreviousPhaseOutput = 0.0;
}
