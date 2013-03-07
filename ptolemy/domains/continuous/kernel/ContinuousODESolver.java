/* The abstract base class of the ODE solvers.

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
package ptolemy.domains.continuous.kernel;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ContinuousODESolver

/**
 Abstract base class for ODE solvers. A complete integration
 consists of one or more rounds of executions. One round of execution requires
 the model time to be advanced and this advancement is handled by the
 Continuous director. Derived classes need to tell how many rounds
 of execution are necessary and how much time to advance for each round.
 <P>
 How many rounds are needed in one integration is solver dependent. For some
 solving algorithms, (i.e. the so called explicit methods) the number of
 rounds is fixed. For some others (i.e. implicit methods), the number of
 rounds can not be decided beforehand.
 <P>
 A round counter is a counter for the number of rounds in one integration.
 It helps the solvers to decide how to behave under different rounds.
 The round counter can be increased by the calling the _incrementRound()
 method. The _reset() method always resets the counter to 0. These methods are
 protected because they are only used by Continuous directors.
 <p>
 Conceptually, ODE solvers do not maintain simulation parameters,
 like step sizes and error tolerance. They get these parameters from the
 director. So the same set of parameters are shared by all the solvers
 in a simulation.

 @author Haiyang Zheng, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public abstract class ContinuousODESolver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of auxiliary variables that an integrator should
     *  provide when solving the ODE. Auxiliary variables are variables
     *  in integrators to store integrator-dependent intermediate results
     *  when solving an ODE.
     *  <br>
     *  For example, the fixed-step solvers need 0 auxiliary variable, but
     *  the RK23 solver needs 4 auxiliary variables to store the temporary
     *  derivatives at different time points during an integration.
     *  @return The number of auxiliary variables.
     */
    public abstract int getIntegratorAuxVariableCount();

    /** Perform one integration step. The fire() method of integrators
     *  delegates to this method. Derived classes need to implement
     *  the details. This method does not produce any outputs.
     *  @param integrator The integrator that calls this method.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public abstract void integratorIntegrate(ContinuousIntegrator integrator)
            throws IllegalActionException;

    /** Return true if the current integration step is accurate from the
     *  argument integrator's point of view. The isStepSizeAccurate() method
     *  of integrators delegates to this method.
     *  Derived classes need to implement the details.
     *  @param integrator The integrator that calls this method.
     *  @return True if the integrator finds the step accurate.
     */
    public abstract boolean integratorIsAccurate(ContinuousIntegrator integrator);

    /** The suggestedStepSize() method of the integrator delegates to this
     *  method. Derived classes need to implement the details.
     *  @param integrator The integrator that calls this method.
     *  @return The suggested next step size by the given integrator.
     */
    public abstract double integratorSuggestedStepSize(
            ContinuousIntegrator integrator);

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Report a debug message via the director.
     *  @param message The message.
     */
    protected final void _debug(String message) {
        _director._reportDebugMessage(message);
    }

    /** Get the current round factor. If the rounds are
     *  finished, then return 1.0.
     *  @return The current round factor.
     *  @see #_isStepFinished()
     */
    protected abstract double _getRoundTimeIncrement();

    /** Return the current round.
     *  @return The current round.
     */
    protected abstract int _getRound();

    /** Return true if debugging is turned on in the director.
     *  @return True if debugging is turned on.
     */
    protected final boolean _isDebugging() {
        return _director._isDebugging();
    }

    /** Return true if the current integration step is finished. For example,
     *  solvers with a fixed number of rounds in an integration step will
     *  return true when that number of rounds are complete. Solvers that
     *  iterate to a solution will return true when the solution is found.
     *  @return Return true if the solver has finished an integration step.
     */
    protected abstract boolean _isStepFinished();

    /** Make this solver the solver of the given Director. This method
     *  should only be called by CT directors, when they instantiate solvers
     *  according to the ODESolver parameters.
     *  @param director The CT director that contains this solver.
     */
    protected final void _makeSolverOf(ContinuousDirector director) {
        _director = director;
    }

    /** Reset the solver, indicating to it that we are starting an
     *  integration step. This method also sets a flag indicating that
     *  we have not converged to a solution if the ODE solver is implicit.
     */
    protected abstract void _reset();

    /** Set the round for the next integration step.
     *  This must be between zero and the value returned by
     *  getIntegratorAuxVariableCount().
     *  @param round The round for the next integration step.
     *  @see #getIntegratorAuxVariableCount()
     */
    protected abstract void _setRound(int round);

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The Continuous director that contains this solver. */
    protected ContinuousDirector _director = null;
}
