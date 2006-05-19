/* Base abstract class for explicit variable step size ODE solver.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.continuous.kernel.solver;

import ptolemy.domains.continuous.kernel.ContinuousODESolver;

//////////////////////////////////////////////////////////////////////////
//// ExplicitODESolver

/**
 This abstract class is the base class for explicit ODE solvers.
 This base class handles the round counts.

 @author  Haiyang Zheng, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public abstract class ExplicitODESolver extends ContinuousODESolver {

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Increment the round and return the time increment associated
     *  with the round.
     *  @return The time increment associated with the next round.
     */
    protected final double _incrementRound() { 
        double result = _timeInc[_roundCount];
        _roundCount++;
        return result;
    }
    
    /** Return true if the current integration step is finished. For example,
     *  solvers with a fixed number of rounds in an integration step will
     *  return true when that number of rounds are complete. Solvers that
     *  iterate to a solution will return true when the solution is found.
     *  @return Return true if the solver has finished an integration step.
     */
    protected final boolean _isStepFinished() {
        return _roundCount == _timeInc.length;
    }

    /** Reset the solver, indicating to it that we are starting an
     *  integration step. This method resets the round counter.
     */
    protected final void _reset() {
        _roundCount = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ////

    /** The ratio of time increments within one integration step. */
    protected double[] _timeInc;

    /** The round counter. */
    protected int _roundCount = 0;
}
