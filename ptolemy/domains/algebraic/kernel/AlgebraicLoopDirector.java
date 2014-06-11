/* Director for solving algebraic loops.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.domains.algebraic.kernel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.sched.Schedule;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.DoubleMatrixMath;
///////////////////////////////////////////////////////////////////
//// AlgebraicLoopDirector

/**
 A director for solving algebraic loops. This director initializes
 all inputs that have a defaultValue parameter, and then executes the model
 according to the specified solver method until all inputs change by less
 than a specified errorTolerance. The methods implemented are:
 <ol>
 <li> FixedPointIteration: This simple strategy simply executes the model
 until all inputs have converge.
 <li> NewtonRaphson: FIXME: Not implemented yet.
 </ol>
 In all cases, the number of iterations is limited to <i>maxIterations</i>,
 and an exception will be thrown if convergence hasn't occurred by then.
 <p>
 The errorTolerance may be given as a parameter to an individual port
 (just add a parameter named "errorTolerance" to the port). For any
 port for which there is no such parameter, the errorTolerance parameter
 of this director will be used. Note that if the errorTolerance of a port
 is changed during a run, the new value is ignored until the next run.
 <p>
 In all cases, the problem being solved has the form:
 <pre>
     -----------
     |  -----  |
     -->| g |---
     x  -----
 </pre>
 where <i>x</i> is initially the vector of values corresponding to input
 ports that have a non-null defaultValue parameter, and <i>g</i> is the
 network of actors connecting these ports.
 <p>
 For the FixedPointIteration, we simply evaluate <i>g</i> repeatedly
 until either all signals do not change by more than the errorTolerance,
 or until there have been <i>maxIterations</i>. Note that it is possible
 to reach a fixed point where some or all signals are infinite.
 <p>
 For NewtonRaphson, we solve for g(x)=x by solving f(x)=0,
 where f(x) = g(x)-x. This is done by iterating as follows:
 <pre>
   x_n+1 = x_n - f(x_n)/f'(x_n) = x_n - (g(x_n) - x_n)/(g'(x_n) - 1) .
 </pre>
 To estimate g'(x_n), we do
 <pre>
   g'(x_n) = (g(x_n + d) - g(x_n))/d
 </pre>
 where <i>d</i> is the <i>delta</i> parameter.
 <p>
 FIXME: Questions:
 <ul>
 <li> This implementation checks for convergence of <i>all</i> input
      ports, not just those with defaultValue parameters (i.e., not just
      <i>x</i>). Is this what we want?
 <li> This implementation uses the defaultValue on every firing.
      Should the default value instead be replaced by the previous
      solution on all but the first iteration?
      FIXME: Yes, this will need to be done as it increases robustness and efficiency.
 <li> Should delta be able to be different for each variable?
      I.e., should each input port that has a defaultValue parameter
      also be able to have a delta parameter?
      FIXME: Yes, the magnitudes of variables can be a few orders of magnitude different,
      e.g., if one port as a control signal, and another an air pressure in Pascal.
 <li> Instead of estimating g'(x_n), we want to be able to optionally
      identify g'(x_n). But this is really tricky when x_n is a vector
      (i.e. where there are multiple input ports with defaultValue).
 <li> This code is not at all optimized and may be quite inefficient.
 </ul>

 @author Edward A. Lee
 @version $Id: AlgebraicLoopDirector.java 65763 2013-03-07 01:54:37Z cxh $
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (pwhitake)
 */
public class AlgebraicLoopDirector extends FixedPointDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public AlgebraicLoopDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Put the stopTime at the end parameters, since it rarely makes sense to use.
        stopTime.moveToLast();

        method = new StringParameter(this, "method");
        method.addChoice("NewtonRaphson");
        method.addChoice("FixedPointIteration");
        method.setExpression("NewtonRaphson");

        maxIterations = new Parameter(this, "maxIterations");
        maxIterations.setTypeEquals(BaseType.INT);
        maxIterations.setExpression("1000");

        errorTolerance = new Parameter(this, "errorTolerance");
        errorTolerance.setTypeEquals(BaseType.DOUBLE);
        errorTolerance.setExpression("1E-4");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The default tolerance for determining when convergence has occurred.
     *  When the current value of an input port differs from the previous
     *  value by less than the <i>errorTolerance</i>, then we declare it to
     *  have converged. This parameter gives a default value that will be used
     *  if the port has no parameter named "errorTolerance". This is a
     *  double with default value 1E-4.
     */
    public Parameter errorTolerance;

    /** The maximum number of allowed iterations before the director
     *  will declare a failure to converge. This is an integer that defaults
     *  to 1000.
     */
    public Parameter maxIterations;

    /** The method to be used to solve algebraic loops. This is a string
     *  that is one of "NewtonRaphson" (the default) or "FixedPointIteration".
     */
    public StringParameter method;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AlgebraicLoopDirector newObject = (AlgebraicLoopDirector) super.clone(workspace);
        // FIXME: populate.
        return newObject;
    }

    /** Prefire and fire actors in the order given by the scheduler
     *  until the iteration converges.
     *  An iteration converges when a pass through the schedule does
     *  not change the status of any receiver.
     *  @exception IllegalActionException If an actor violates the
     *   monotonicity constraints, or the prefire() or fire() method
     *   of the actor throws it.
     *   
     *
     */
    public void fire() throws IllegalActionException {
        // Do not call super.fire(). It doesn't do the right thing.
        if (_debugging) {
            _debug("AlgebraicLoopDirector: invoking fire().");
        }
        int iterationCount = 0;

        List<AlgebraicLoopReceiver> receivers = _receivers;
        int i = 0;
        for (AlgebraicLoopReceiver receiver : receivers) {
            if ( (receiver.getContainer().defaultValue != null) ){
                Token t = receiver._getCurrentToken();
                if (t != null && t.getType() == BaseType.DOUBLE){
                    _x_n[i] = ((DoubleToken)t).doubleValue();
                    _tol[i] =_getErrorTolerance( receiver.getContainer() );
                    i++;
                }
                else{
                    // FIXME:
                    // The Token is null. I think this is a bug in Ptolemy, as test/auto/Newton1.xml
                    // should have the same values for all receivers.
                    // Hence, for now, I just set it to some value in order to
                    // fill the vector of doubles
                    _x_n[i] = 99999;
                    _tol[i] = ((DoubleToken)errorTolerance.getToken()).doubleValue();
                    i++;
                }
            }
        }
        // Now, _x_n contains all values for the receivers
        int maxIterationsValue = ((IntToken)(maxIterations.getToken())).intValue();

        // Instantiate the numerical solver
        AlgebraicLoopSolver solver = new AlgebraicLoopSolver(_tol, maxIterationsValue);
        // Call the solver.
        try{
            solver.solve(_x_n);
        }
        catch(IllegalArgumentException e){
            throw new IllegalActionException(this, e.getMessage());
        }
        catch(Exception e){
            if (!solver.converged())
                throw new IllegalActionException(this, "Failed to converge after " + maxIterationsValue + " iterations.");
        }
        if (_debugging) {
            _debug(this.getFullName() + ": Fixed point found after "
            + solver.getIterationCount() + " iterations.");
        }
    }

    /** Return g(x).
     *
     *  This function is called by the solver to evaluate the loop function.
     *
     * @param x Input to the loop function.
     * @return Result of the loop function.
     * @exception IllegalActionException If the prefire() method
     *   returns false having previously returned true in the same
     *   iteration, or if the prefire() or fire() method of the actor
     *   throws it.
     */
    protected double[] _evaluateLoopFunction(double[] x)
            throws IllegalActionException{
        // Set the argument to the receivers
        int iRec=0;
        List<AlgebraicLoopReceiver> receivers = _receivers;
        for (AlgebraicLoopReceiver receiver : receivers) {
            // FIXME: We should do this only if the receiver is a DoubleToken
            DoubleToken t = new DoubleToken(x[iRec]);
            receiver.put(t);
            iRec++;
        }

        // Calculate the superclass fixed point, which is the current
        // evaluation of the feedback function.
        // That is, x_n is replaced with g(x_n), where g()
        // is the feedback function.
        Schedule schedule = getScheduler().getSchedule();
        do {
                Iterator firingIterator = schedule.firingIterator();
                while (firingIterator.hasNext() && !_stopRequested) {
                    Actor actor = ((Firing) firingIterator.next()).getActor();
                    // If the actor has previously returned false in postfire(),
                    // do not fire it.
                    if (!_actorsFinishedExecution.contains(actor)) {
                        // Check whether the actor is ready to fire.
                        if (_isReadyToFire(actor)) {
                            _fireActor(actor);
                            _actorsFired.add(actor);
                        } else {
                            if (_debugging) {
                                if (!_actorsFinishedFiring.contains(actor)
                                        && actor.isStrict()) {
                                    _debug("Strict actor has unknown inputs: "
                                            + actor.getFullName());
                                }
                            }
                        }
                    } else {
                        // The postfire() method of this actor returned false in
                        // some previous iteration, so here, for the benefit of
                        // connected actors, we need to explicitly call the
                        // send(index, null) method of all of its output ports,
                        // which indicates that a signal is known to be absent.
                        if (_debugging) {
                            _debug("FixedPointDirector: no longer enabled (return false in postfire): "
                                    + actor.getFullName());
                        }
                        _sendAbsentToAllUnknownOutputsOf(actor);
                    }
                }
        } while (!_hasIterationConverged() && !_stopRequested);

        // Get the values from the receivers, and return them
        double[] g = new double[x.length];

        int i = 0;
        for (AlgebraicLoopReceiver receiver : receivers) {
            if (receiver.getContainer().defaultValue != null){
                // Record g(x_n)
                Token t = receiver._getCurrentToken();
                if (t.getType() == BaseType.DOUBLE){
                    g[i] = ((DoubleToken)(t)).doubleValue();
                    i++;
                }
            }
        }
        return g;
    }


    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _isNewtonRaphson = method.stringValue().equals(new String("NewtonRaphson"));

        // Count the number of receivers that have a default value.
        int nRec = 0;
        List<AlgebraicLoopReceiver> receivers = _receivers;
        for (AlgebraicLoopReceiver receiver : receivers) {
            if (receiver.getContainer().defaultValue != null)
                nRec++;
        }
        _x_n = new double[nRec];
        _g_n = new double[nRec];
        _tol = new double[nRec];
        // FIXME: populate.
    }

    /** Return a new FixedPointReceiver. If a subclass overrides this
     *  method, the receiver it creates must be a subclass of FixedPointReceiver,
     *  and it must add the receiver to the _receivers list (a protected
     *  member of this class).
     *  @return A new FixedPointReceiver.
     */
    public Receiver newReceiver() {
        Receiver receiver = new AlgebraicLoopReceiver(this);
        _receivers.add(receiver);
        return receiver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if this iteration has converged.  If the superclass has not
     *  converged, then we have not yet established whether all receivers are known,
     *  so this returns false. Otherwise, it returns true if the values of all receivers
     *  are close to what they were in the previous iteration.
     *  @param iterationCount The count of the iteration.
     *  @return true if this iteration has converged.
     *  @throws IllegalActionException If the number of iterations exceeds the value
     *   of maxIterations.
     */
    protected boolean _hasIterationConverged(int iterationCount)
            throws IllegalActionException {
        int maxIterationsValue = ((IntToken)maxIterations.getToken()).intValue();
        if (iterationCount > maxIterationsValue) {
            throw new IllegalActionException(this, "Failed to converge after " + maxIterationsValue + " iterations.");
        }
        if (!super._hasIterationConverged()) {
            return false;
        }
        if (_debugging) {
            _debug("Checking whether new values are close to previous values");
        }
        @SuppressWarnings("unchecked")
        List<AlgebraicLoopReceiver> receivers = _receivers;
        for (AlgebraicLoopReceiver receiver : receivers) {
            Token currentToken = receiver._getCurrentToken();
            Token previousToken = receiver._getPreviousToken();
            if (currentToken != previousToken) {
                // Either might be null.
                if (currentToken == null || previousToken == null) {
                    return false;
                }
                IOPort container = receiver.getContainer();
                double epsilon = _getErrorTolerance(container);
                if (!(currentToken.isCloseTo(previousToken, epsilon)).booleanValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Fire an actor. Call its prefire() method, and
     *  if that returns true, call its fire() method.
     *  @param actor The actor to be fired.
     *  @exception IllegalActionException If the prefire() method
     *   returns false having previously returned true in the same
     *   iteration, or if the prefire() or fire() method of the actor
     *   throws it.
     */
    protected void _fireActor(Actor actor) throws IllegalActionException {
        // Prefire the actor.
        boolean prefireReturns = actor.prefire();
        if (_debugging) {
            _debug("AlgebraicLoopDirector: Prefiring: "
                    + ((Nameable) actor).getFullName() + ", which returns "
                    + prefireReturns);
        }
        if (prefireReturns) {
            _actorsAllowedToFire.add(actor);

            // Whether all inputs are known must be checked before
            // firing to handle cases with self-loops, because the
            // current firing may change the status of some input
            // receivers from unknown to known.
            boolean allInputsKnownBeforeFiring = _areAllInputsKnown(actor);

            if (_debugging) {
                if (allInputsKnownBeforeFiring) {
                    _debug("Firing: " + ((Nameable) actor).getName()
                            + ", which has all inputs known.");
                } else {
                    _debug("Firing: " + ((Nameable) actor).getName()
                            + ", which has some inputs unknown.");
                }
            }

            actor.fire();
            // If all of the inputs of this actor were known before firing, then the
            // outputs can be asserted to be absent.
            if (allInputsKnownBeforeFiring) {
                _sendAbsentToAllUnknownOutputsOf(actor);
            }
        } else {
            // prefire() returned false. The actor declines
            // to fire. This could be because some inputs are
            // not known.  If all inputs are known, then we
            // interpret this to mean that all outputs should be absent.
            // Note that prefire() is executed only after all the inputs are
            // known if the actor is strict.
            if (actor.isStrict() || _areAllInputsKnown(actor)) {
                _sendAbsentToAllUnknownOutputsOf(actor);
            }
        }
    }

    /** React to the change in receiver status by incrementing the count of
     *  known receivers.
     */
    protected void _receiverChanged() {
        super._receiverChanged();
    }

    /** Return the list of receivers that this director is in charge of.
     *  @return A list of receivers.
     */
    protected List<AlgebraicLoopReceiver> _receivers() {
        return _receivers;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Return the error tolerance of the specified port.
     *  @param port The port.
     *  @return The port's error tolerance.
     *  @throws IllegalActionException If the errorTolerance parameter of the port
     *   cannot be evaluated.
     */
    private double _getErrorTolerance(IOPort port)
            throws IllegalActionException {
        // If it's in the cache, use that value.
        if (_errorTolerances != null) {
            Double result = _errorTolerances.get(port);
            if (result != null) {
                return result;
            }
        }
        if (port != null) {
            Parameter tolerance = (Parameter)port.getAttribute("errorTolerance", Parameter.class);
            if (tolerance != null) {
                Token value = tolerance.getToken();
                if (value instanceof DoubleToken) {
                    // Port has a custom tolerance.
                    double epsilon = ((DoubleToken)value).doubleValue();
                    // Cache the value.
                    if (_errorTolerances == null) {
                        _errorTolerances = new HashMap<IOPort,Double>();
                        _errorTolerances.put(port, epsilon);
                    }
                    return epsilon;
                }
            }
        }
        // Return the default value.
        return ((DoubleToken)errorTolerance.getToken()).doubleValue();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////
    /** Current value of the iteration variables x_n */
    protected double[] _x_n;
    /** Current value of the loop function g(x_n) */
    protected double[] _g_n;
    /** Tolerance for each iteration variable */
    protected double[] _tol;

    /** Flag to indicate that it is the NewtonRaphson method */
    protected boolean _isNewtonRaphson;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A table of error tolerances for ports that specify them. */
    private Map<IOPort,Double> _errorTolerances;

    ///////////////////////////////////////////////////////////////////
    ////   Inner class for numerical solver                        ////

    /**
     A class for solving algebraic loops.

     This class solves an algebraic loop of the form x=g(x)
     <pre>
         -----------
         |  -----  |
         -->| g |---
         x  -----
     </pre>
     where <i>x</i> is initially the vector of values corresponding to input
     ports that have a non-null defaultValue parameter, and <i>g</i> is the
     network of actors connecting these ports.
     <p>
     For the FixedPointIteration, we simply evaluate <i>g</i> repeatedly
     until either all signals do not change by more than the errorTolerance,
     or until there have been <i>maxIterations</i>. Note that it is possible
     to reach a fixed point where some or all signals are infinite.
     <p>
     For NewtonRaphson, we solve for g(x)=x by solving f(x)=0,
     where f(x) = g(x)-x. This is done by iterating as follows:
     <pre>
       x_n+1 = x_n - f(x_n)/f'(x_n) = x_n - (g(x_n) - x_n)/(g'(x_n) - 1) .
     </pre>
     To estimate g'(x_n), we do
     <pre>
       g'(x_n) = (g(x_n + d) - g(x_n))/d
     </pre>
     where <i>d</i> is the <i>delta</i> parameter.
     <p>

     @author Michael Wetter
     */
    class AlgebraicLoopSolver {

        /** Constructs an algebraic loop solver.

         *  @param tolerance Tolerance for each variable.
         *  @param maxIterations Maximum number of iterations.
         */
        public AlgebraicLoopSolver(
                double[] tolerance,
                int maxIterations){
            _tol = new double[tolerance.length];
            _x = new double[tolerance.length];

            System.arraycopy(tolerance, 0, _tol, 0, tolerance.length);
            _maxIterations = maxIterations;

            _converged = false;
            _iterationCount = 0;
            // Set the step size for the Newton method.
            // FIXME: This should take the scaling of the variable into account.
            if (_isNewtonRaphson){
                _deltaX = new double[tolerance.length];
                for (int i = 0; i < tolerance.length; i++){
                    _tol[i] = 1E-5;
                    // FIXME: _deltaX should take into account the scaling of the variable.
                    //        It should also be adaptive.
                    _deltaX[i] = 1E-5;
                }
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////
        /** Solve the algebraic loop.
         *
         *  This method iterates until a solution is found. If it does not
         *  converge within the maximum number of iterations, it throws
         *  an IllegalActionException. A method that calls solve(double[] xInitial)
         *  can then call converged() to check whether the exception is thrown because
         *  of lack of convergence.
         *
         * @param xInitial Initial iterate.
         * @exception IllegalActionException If an actor violates the
         *            monotonicity constraints, or the prefire() or fire() method
         *            of the actor throws it.
         * @exception IllegalArgumentException If the solver fails to find a solution.
         */
        public void solve(double[] xInitial)
            throws IllegalActionException{
            _x = xInitial;
            _iterationCount = 0;
            do {
                // Evaluate the loop function to compute x_{n+1} = g(x_n).
                // This calls the loop function of the outer class.
                double[] g = new double[xInitial.length];
                if (_isNewtonRaphson){
                    _xNew = _newtonStep(_x);
                }
                else{
                    // Successive substitution
                    _xNew = _evaluateLoopFunction(_x);
                }
                _iterationCount++;

                // Check for convergence
                _converged = true;
                for(int i = 0; i < _x.length; i++){
                    double diff = _x[i] - _xNew[i];
                    // FIXME: this needs to be replaced with a test for relative and absolute convergence.
                    if (diff > _tol[i] || -diff > _tol[i]){
                        _converged = false;
                        break;
                    }
                }
                // Update iterate
                System.arraycopy(_xNew, 0, _x, 0, _x.length);
                
                // Check for maximum number of iterations in case we did not yet converge.
                if (!_converged && _iterationCount > _maxIterations)
                    throw new RuntimeException("Failed to converge after " + _maxIterations + " iterations.");
            } while (!_converged && !_stopRequested);
        }

        /** Return the new iterate of a Newton step. 
         * 
         * @param x The best known iterate.
         * @return The new guess for the solution.
         * @exception IllegalActionException If an actor violates the
         *            monotonicity constraints, or the prefire() or fire() method
         *            of the actor throws it.
         * @exception IllegalArgumentException If the solver fails to find a solution.
         *
         */
        protected double[] _newtonStep(
                    double[] x)
        throws IllegalActionException {
            final int n = x.length;

            double[] xNew = new double[n];
            System.arraycopy(x, 0, xNew, 0, n);
            // Jacobian
            double[][] J = new double[n][n];
            // Loop over each independent variable, and fill the Jacobian
            final double[] g = _evaluateLoopFunction(x);
            for (int i = 0; i < n; i++){
                final double xOri = xNew[i];
                xNew[i] += _deltaX[i];
                final double [] gNew = _evaluateLoopFunction(xNew);
                for(int k = 0; k < n; k++)
                    J[i][k] = (gNew[k]-g[k])/_deltaX[i];
                // Reset the coordinate to its old value
                xNew[i] = xOri;
            }
            // Check whether Jacobian is invertible
            final double det = DoubleMatrixMath.determinant(J);
            if (Math.abs(det) < 1E-5){
                String em = "Singular Jacobian in Newton step. Reformulate equation or try different start value"
                             + System.lineSeparator()
                             + "Jacobian = " + DoubleMatrixMath.toString(J)
                             + System.lineSeparator()
                             + "Determinant = " + det;
                throw new IllegalArgumentException(em);
            }
            
            // Solve J * d = -g(x_n) for d = x_{n+1}-x{n} 
            // to get the Newton step.
            if (n == 1){
                final double d = -g[1]/J[1][1];
                xNew[1] = x[1] + d;
            }
            else{
                final double[] d = gaussElimination(J, g);
                for (int i = 0; i < n; i++)
                    xNew[i] = x[i] - d[i];
            }
            return xNew;
        }

        
    /** Return vector x that solves A*x=f by a Gauss elimination
     *  with normalization and interchange of rows.
     * 
     *  A is an NxN matrix
     * Method solves the equation A*x=f for x.
     *
     * @param A Matrix
     * @param f Array with solution of A*x=f
     * @return x Array x = A**(-1) * f
      */
    public double[] gaussElimination(double[][] A, double[] f)
    {
        int i, j, k, piv, iMax, jMax;
        int dim = f.length;
        int dimP1 = dim + 1;
        double[]   r = new double[dim];
        double[][] B = new double[dim][dimP1];
        double[]   tempRow = new double[dimP1];
        double a, pivotElement;
        double aMax = -1;

        for (i = 0; i < dim; i++)
        {
            for (j = 0; j < dim; j++)
                B[i][j] = A[i][j];
            B[i][dim] = f[i];
        }

        for (piv = 0; piv < dim; piv++)
        {
        //interchange rows if necessary
            iMax = 0;
            jMax = 0;
            for (i = 0; i < dim; i++)
            {
                for(j = dim-1; j >= 0; j--)
                {
                    if(Math.abs(B[i][j]) > aMax)
                    {
                        aMax = Math.abs(B[i][j]);
                        iMax = i;
                        jMax = j;
                    }
                }
            }

            if ( iMax != jMax)
            {
                for (i = 0; i < dimP1; i++)
                {
                    tempRow[i] = B[iMax][i];
                    B[iMax][i] = B[jMax][i];
                    B[jMax][i] = tempRow[i];
                }
            }


            pivotElement = B[piv][piv];

        // normalization of pivot row
            for (j = 0; j < dimP1; j++)
                B[piv][j] = B[piv][j]/pivotElement;

        // elimination
            for(k = 0; k < dim; k++)
            {
                if(piv!=k)
                {
                    a = B[k][piv];
                    for(j = 0 ; j < dimP1; j++) // set new row
                    {
                        B[k][j] =  B[k][j] - a * B[piv][j];
                    }
                }
            }
        }


    for (i = 0; i < dim; i++)
            r[i] = B[i][dim];

    return r;
    }
       
        
        /** Return true if the solver converged, false otherwise.
         *
         * @return true if the solver converged, false otherwise.
         */
        public boolean converged() {
            return _converged;
        }

        /** Return the number of iterations done in the last call to the method solve(double[]).
         *
         * @return The number of iterations
         */
         public int getIterationCount() {
             return _iterationCount;
         }

        ///////////////////////////////////////////////////////////////////
        //// protected variables                                       ////
         /** Current value of the iteration variables x */
         protected double[] _x;
         /** Updated value of the iteration variables x */
         protected double[] _xNew;
        /** Tolerance for each iteration variable */
        protected double[] _tol;
        /** Step size for finite difference approximation */
        protected double[] _deltaX;
        /** Number of iterations in the last call to the function solve(double[]) */
        protected int _iterationCount;
        /** Maximum number of iterations */
        protected int _maxIterations;
        /** Flag that indicates whether the solver converged */
        protected boolean _converged;
    }
}
