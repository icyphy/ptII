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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.CausalityInterface;
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
import ptolemy.math.DoubleArrayMath;
import ptolemy.math.DoubleMatrixMath;

///////////////////////////////////////////////////////////////////
//// AlgebraicLoopDirector

/**
 A director for solving algebraic loops. This director initializes
 all inputs that have a defaultValue parameter, and then executes the model
 according to the specified solver method until all inputs change by less
 than a specified errorTolerance. The methods implemented are:
 <ol>
 <li> SuccessiveSubstitution: This simple strategy executes the model
 until all inputs have converge.
 <li> NewtonRaphson: The Newton-Raphson iteration.
 <li> Homotopy: A homotopy method that uses Newton-Raphson iteration and Euler
      updates that search along a curve on which a linear approximation to the
      problem is successively replaced with the actual function.
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
 For the SuccessiveSubstitution, we simply evaluate <i>g</i> repeatedly
 until either all signals do not change by more than the errorTolerance,
 or until there have been <i>maxIterations</i>. Note that it is possible
 to reach a fixed point where some or all signals are infinite.
 <p>
 For NewtonRaphson, we solve for g(x)=x by solving f(x)=0,
 where f(x) = x-g(x). This is done by iterating as follows:
 <pre>
   x_n+1 = x_n - f(x_n)/f'(x_n) = x_n - (x_n - g(x_n))/(1 - g'(x_n)) .
 </pre>
 To estimate g'(x_n), we do
 <pre>
   g'(x_n) = (g(x_n + d) - g(x_n))/d
 </pre>
 where <i>d</i> is the <i>delta</i> parameter.
 <p>
 For Homotopy, we solve f(x) = x - g(x).
 The problem is reformulated as H(s, lambda, x0) = s - lambda (g(s+x0)-x0),
 where lambda has an initial value of 0 and is successively increased to 1,
 s is a coordinate transformation defined so that x = s+x0, where
 x0 is the initial iterate.
 <br>
 The implementation is equal to Program 3 of
 Eugene L. Allgower and Kurt Georg,
 Introduction to Numerical Continuation Methods,
 Classics in Applied Mathematics, Vol. 45, SIAM, 2003.
 However, the implementation by Allgower and Georg assumes an initial iterate of 0,
 which is the reason for the above coordinate transformation.
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
      FIXME: This is addressed in the Homotopy algorithm which uses Broyden updates
      to estimate the Jacobian. A similar mechanism could be added to the Newton
      algorithm in future work.
 <li> This code is not at all optimized and may be quite inefficient.
 </ul>

 @author Edward A. Lee and Michael Wetter
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (pwhitake)
 */
public class AlgebraicLoopDirector extends StaticSchedulingDirector {

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
        method.addChoice("Homotopy");
        method.addChoice("NewtonRaphson");
        method.addChoice("SuccessiveSubstitution");
        method.setExpression("Homotopy");

        maxIterations = new Parameter(this, "maxIterations");
        maxIterations.setTypeEquals(BaseType.INT);
        maxIterations.setExpression("1000");

        errorTolerance = new Parameter(this, "errorTolerance");
        errorTolerance.setTypeEquals(BaseType.DOUBLE);
        errorTolerance.setExpression("1E-4");

        AlgebraicLoopScheduler scheduler = new AlgebraicLoopScheduler(
                this, uniqueName("Scheduler"));
        setScheduler(scheduler);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The default tolerance for determining when convergence has occurred.
     *  When the current value of an input port differs from the previous
     *  value by less than the <i>errorTolerance</i>, then we declare it to
     *  hfave converged. This parameter gives a default value that will be used
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
     *  that is one of "Homotopy" (the default), "NewtonRaphson" or "SuccessiveSubstitution".
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AlgebraicLoopDirector newObject = (AlgebraicLoopDirector) super
                .clone(workspace);
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
    @Override
    public void fire() throws IllegalActionException {
        // Do not call super.fire(). It doesn't do the right thing.
        if (_debugging) {
            _debug("AlgebraicLoopDirector: invoking fire().");
        }

        int i = 0;
        for (AlgebraicLoopReceiver receiver : _breakVariables) {
            // Get the updated value from the previous iteration.
            Token t = receiver._getUpdatedValue();
            if (t instanceof DoubleToken) {
                _x_n[i] = ((DoubleToken) t).doubleValue();
                i++;
            } else {
                IOPort port = receiver.getContainer();
                throw new IllegalActionException(
                        "Break variable is required to be a double. Got " + t
                        + " on port " + port.getName(getContainer()));
            }
        }
        // Now, _x_n contains all values for the receivers

        // Call the solver.
        _solver.solve(_x_n);
        if (_debugging) {
            _debug(this.getFullName() + ": Fixed point found after "
                    + _solver.getIterationCount() + " iterations.");
        }
    }

    /** Evaluate the loop function for x and save the result in g.
     *
     *  This function is called by the solver to evaluate the loop function.
     *  @param x Input to the loop function.
     *  @param g Double vector of the same size as x. The result will be stored in this function.
     *
     *  @exception IllegalActionException If the prefire() method
     *   returns false having previously returned true in the same
     *   iteration, or if the prefire() or fire() method of the actor
     *   throws it, or if evaluating the function yields a value that
     *   is not a double.
     */
    protected void _evaluateLoopFunction(final double[] x, double[] g)
            throws IllegalActionException {
        // Set the argument to the receivers
        int iRec = 0;
        for (AlgebraicLoopReceiver receiver : _breakVariables) {
            DoubleToken t = new DoubleToken(x[iRec]);
            // Set the initial value of the receiver.
            receiver._setInitialValue(t);
            if (_debugging) {
                IOPort port = receiver.getContainer();
                _debug("Setting input to loop function for '"
                        + port.getName(getContainer()) + "' to " + x[iRec]);
            }
            iRec++;
        }

        // Execute the schedule, which is the current
        // evaluation of the feedback function.
        // That is, x_n is replaced with g(x_n), where g()
        // is the feedback function.
        Schedule schedule = getScheduler().getSchedule();
        Iterator firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext() && !_stopRequested) {
            Actor actor = ((Firing) firingIterator.next()).getActor();
            // If the actor has previously returned false in postfire(),
            // do not fire it.
            if (!_actorsFinishedExecution.contains(actor)) {
                _fireActor(actor);
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
                _clearAllDestinationReceivers(actor);
            }
        }
        // Get the values from the receivers, and return them in g
        int i = 0;
        for (AlgebraicLoopReceiver receiver : _breakVariables) {
            // Store g(x_n)
            Token t = receiver._getUpdatedValue();
            if (t instanceof DoubleToken) {
                g[i] = ((DoubleToken) (t)).doubleValue();
                if (_debugging) {
                    IOPort port = receiver.getContainer();
                    _debug("Output of loop function at '"
                            + port.getName(getContainer()) + "' = " + g[i]);
                }
                i++;
            } else {
                IOPort port = receiver.getContainer();
                throw new IllegalActionException(
                        "Break variable is required to be a double. Got " + t
                        + " on port " + port.getName(getContainer()));
            }
        }
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method, then identify all the break variables
     *  (where an input port has a defaultValue set) and adjust the I/O
     *  dependencies.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Set up a list of all receivers that contain the values where the
        // iterate x needs to be stored.
        // These are the input ports associated with the break variables.
        // These are all receivers that have a default value and that are an input.
        _breakVariables = new LinkedList<AlgebraicLoopReceiver>();
        CompositeEntity container = (CompositeEntity) getContainer();
        @SuppressWarnings("unchecked")
        List<Actor> actors = container.deepEntityList();
        _nVars = 0;
        for (Actor actor : actors) {
            @SuppressWarnings("unchecked")
            List<IOPort> inputPorts = actor.inputPortList();
            for (IOPort port : inputPorts) {
                // If the port has a default value, then all its receivers are break variables.
                Token initialValue = port.defaultValue.getToken();
                if (initialValue != null) {
                    // Break any causality relation between this input and all outputs.
                    CausalityInterface causality = actor
                            .getCausalityInterface();
                    @SuppressWarnings("unchecked")
                    List<IOPort> outputPorts = actor.outputPortList();
                    for (IOPort output : outputPorts) {
                        causality.removeDependency(port, output);
                    }
                    Receiver[][] receivers = port.getReceivers();
                    for (Receiver[] receivers2 : receivers) {
                        for (Receiver receiver : receivers2) {
                            _breakVariables.add((AlgebraicLoopReceiver) receiver);
                            // Set both the initial value and the updated value of the receiver.
                            ((AlgebraicLoopReceiver) receiver)._setInitialValue(initialValue);
                            ((AlgebraicLoopReceiver) receiver).put(initialValue);
                        }
                    }
                    if (_debugging) {
                        _debug("Break variable: "
                                + port.getName(getContainer()));
                    }
                }
            }
        }
        _nVars = _breakVariables.size();
        _x_n = new double[_nVars];
        _g_n = new double[_nVars];
        _tolerance = new double[_nVars];

        // Instantiate the numerical solver
        // Get the maximum number of iterations
        final int maxIterationsValue = ((IntToken) (maxIterations.getToken()))
                .intValue();

        // Get the variable names and the tolerance.
        // FIXME: If the port a multiport, then the names will not be unique. Is this a problem?
        final String[] variableNames = new String[_nVars];
        int i = 0;
        for (AlgebraicLoopReceiver receiver : _breakVariables) {
            IOPort port = receiver.getContainer();
            variableNames[i] = port.getName(getContainer());
            _tolerance[i] = _getErrorTolerance(port);
            i++;
        }

        // Instantiate the solver.
        if (method.stringValue().equals("Homotopy")) {
            _solver = new Homotopy(variableNames, _tolerance,
                    maxIterationsValue);
        } else if (method.stringValue().equals("NewtonRaphson")) {
            _solver = new NewtonRaphson(variableNames, _tolerance,
                    maxIterationsValue);
        } else if (method.stringValue().equals("SuccessiveSubstitution")) {
            _solver = new SuccessiveSubstitution(variableNames, _tolerance,
                    maxIterationsValue);
        } else {
            throw new IllegalActionException("Solver '" + method.stringValue()
                    + "' is not a valid keyword.");
        }
    }

    /** Return a new FixedPointReceiver. If a subclass overrides this
     *  method, the receiver it creates must be a subclass of FixedPointReceiver,
     *  and it must add the receiver to the _receivers list (a protected
     *  member of this class).
     *  @return A new FixedPointReceiver.
     */
    @Override
    public Receiver newReceiver() {
        AlgebraicLoopReceiver receiver = new AlgebraicLoopReceiver(this);
        return receiver;
    }
    
    /** Postfire all contained actors.
     * @throws IllegalActionException If the superclass throws it, or
     *  if any of the contained actors throw it.
     */
    public boolean postfire() throws IllegalActionException {
            CompositeEntity container = (CompositeEntity)getContainer();
            @SuppressWarnings("unchecked")
                List<Actor> actors = container.deepEntityList();
            for (Actor actor : actors) {
                    actor.postfire();
            }
            return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Call the send(index, null) method of each output port of the specified actor.
     *  @param actor The actor.
     *  @exception IllegalActionException If thrown while getting
     *   the width of a port, determining if a port is known
     *   or while sending data.
     */
    protected void _clearAllDestinationReceivers(Actor actor)
            throws IllegalActionException {
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int j = 0; j < outputPort.getWidth(); j++) {
                if (!outputPort.isKnown(j)) {
                    if (_debugging) {
                        _debug("Set output " + outputPort.getFullName()
                                + " to absent.");
                    }
                    outputPort.send(j, null);
                }
            }
        }
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
        // FIXME: Should we call _clearAllDestinationReceivers(actor) before firing?
        // If the actor fails to produce an output, should that change the state of
        // the destinations to absent?
        if (prefireReturns) {
            if (_debugging) {
                _debug("Firing: " + ((Nameable) actor).getName());
            }
            actor.fire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Return the error tolerance of the specified port.
     *  @param port The port.
     *  @return The port's error tolerance.
     *  @exception IllegalActionException If the errorTolerance parameter of the port
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
            Parameter tolerance = (Parameter) port.getAttribute(
                    "errorTolerance", Parameter.class);
            if (tolerance != null) {
                Token value = tolerance.getToken();
                if (value instanceof DoubleToken) {
                    // Port has a custom tolerance.
                    double epsilon = ((DoubleToken) value).doubleValue();
                    // Cache the value.
                    if (_errorTolerances == null) {
                        _errorTolerances = new HashMap<IOPort, Double>();
                        _errorTolerances.put(port, epsilon);
                    }
                    return epsilon;
                }
            }
        }
        // Return the default value.
        return ((DoubleToken) errorTolerance.getToken()).doubleValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of receivers for all break variables. */
    protected List<AlgebraicLoopReceiver> _breakVariables;

    /** Current value of the loop function g(x_n). */
    protected double[] _g_n;

    /** Number of break variables. */
    protected int _nVars;

    /** Algebraic loop solver. */
    AlgebraicLoopSolver _solver;

    /** Tolerance for each iteration variable. */
    protected double[] _tolerance;

    /** Current value of the iteration variables x_n. */
    protected double[] _x_n;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A table of error tolerances for ports that specify them. */
    private Map<IOPort, Double> _errorTolerances;

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
     For the SuccessiveSubstitution, we simply evaluate <i>g</i> repeatedly
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
     For Homotopy, we solve f(x) = x - g(x) = 0.
     The problem is reformulated as H(x, lambda) = x - lambda g(x),
     where lambda has an initial value of 0 and is successively increased to 1.
     The implementation is equal to Program 3 of
     Eugene L. Allgower and Kurt Georg,
     Introduction to Numerical Continuation Methods,
     Classics in Applied Mathematics, Vol. 45, SIAM, 2003.

     @author Michael Wetter
     */
    abstract class AlgebraicLoopSolver {

        /** Construct an algebraic loop solver.
         *  @param variableNames Names of each break variable.
         *  @param tolerance Tolerance for each variable.
         *  @param maxIterations Maximum number of iterations.
         */
        public AlgebraicLoopSolver(String[] variableNames, double[] tolerance,
                int maxIterations) throws IllegalActionException {
            _variableNames = variableNames;
            _tolerance = tolerance;
            _maxIterations = maxIterations;
        }

        /** Return true if the solver converged, false otherwise.
         *  @return true if the solver converged, false otherwise.
         */
        public boolean converged() {
            return _converged;
        }

        ///////////////////////////////////////////////////////////////////
        ////             public methods                                ////
        /**  This method solves the fixed point iteration.
         *
         *  @param xIni Array with the initial values of the variables, to be replaced
         *   with the solution by this method.
         * @exception IllegalActionException If the prefire() method
         *  returns false having previously returned true in the same
         *  iteration, or if the prefire() or fire() method of the actor
         *  throws it, or if evaluating the function yields a value that
         *  is not a double, or if the solver fails to find a solution.
         */
        abstract public void solve(double[] xIni) throws IllegalActionException;

        /** Return the number of iterations done in the last call to the method solve(double[]).
         *  @return The number of iterations
         */
        public int getIterationCount() {
            return _iterationCount;
        }

        /** Return true if each element of f is within the solver tolerance.
         * 
         * @param f Value of residual function to be tested.
         * @return true if convergence is achieved, false otherwise.
         */
        protected boolean _didConverge(final double[] f){
            for (int i = 0; i < f.length; i++) {
                final double diff = Math.abs(f[i]);
                if (diff > Math.max(_tolerance[i], diff * _tolerance[i])) {
                    return false;
                }
            }
            return true;
        }

        ///////////////////////////////////////////////////////////////////
        ////             protected variables                           ////
        /** Flag that indicates whether the solver converged */
        protected boolean _converged;

        /** Number of iterations in the last call to the function solve(double[]) */
        protected int _iterationCount;

        /** Maximum number of iterations */
        protected int _maxIterations;

        /** Local view of the tolerance vector. */
        protected double[] _tolerance;

        /** Variable names, used for error reporting */
        protected String[] _variableNames;
    }

    /**
     * Class for solving algebraic loops using the Newton-Raphson method.
     *
     * @author Michael Wetter
     */
    class NewtonRaphson extends AlgebraicLoopSolver {

        /** Construct an algebraic loop solver.
         *
         *  @param variableNames Names of each break variable.
         *  @param tolerance Tolerance for each variable.
         *  @param maxIterations Maximum number of iterations.
         */
        public NewtonRaphson(String[] variableNames, double[] tolerance,
                int maxIterations) throws IllegalActionException {
            super(variableNames, tolerance, maxIterations);
            // Temporary variable used to store the result of f(x)=g(x)-x.
            _f = new double[_nVars];

            // Initialize step size for Jacobian calculation
            _deltaX = new double[tolerance.length];
            for (int i = 0; i < tolerance.length; i++) {
                // FIXME: _deltaX should take into account the scaling of the variable.
                //        For FMUs, this can be obtained from the nominal attribute.
                //        Maybe this should be an attribute of a port of a Ptolemy actor?
                _deltaX[i] = 1E-5;
            }

        }

        /** Solve the algebraic loop using the specified array as the initial
         *  guess for the variables being solved for and replace the contents
         *  of the specified array with the solution that is found.
         *  <p>
         *  This method iterates until a solution is found. If it does not
         *  converge within the maximum number of iterations, it throws
         *  an IllegalActionException. A method that calls solve(double[] xIni)
         *  can then call converged() to check whether the exception is thrown because
         *  of lack of convergence.
         *
         *  @param xIni Array with the initial values of the variables, which will be replaced
         *              by this method with the solution.
         *  @exception IllegalActionException If the prefire() method
         *             returns false having previously returned true in the same
         *             iteration, or if the prefire() or fire() method of the actor
         *             throws it, or if evaluating the function yields a value that
         *             is not a double, or if the solver fails to find a solution.
         */
        @Override
        public void solve(double[] xIni) throws IllegalActionException {
            _iterationCount = 0;

            // Evaluate the loop function to compute x_{n+1} = g(x_n).
            // This calls the loop function of the outer class.
            _residual(xIni, _f);
            double[] xNew = new double[xIni.length];

            // Main iteration loop.
            do {
                xNew = _newtonStep(xIni, _f);

                // Evaluate the loop function and store the residual in _f.
                _residual(xNew, _f);
                if(_debugging){
                    _debug("Newton obtained residual " + DoubleArrayMath.toString(_f));
                }
                // Check for convergence.
                _converged = _didConverge(_f);

                // Update iterate.
                System.arraycopy(xNew, 0, xIni, 0, xIni.length);

                // Check for maximum number of iterations in case we did not yet converge.
                if (!_converged && _iterationCount > _maxIterations) {
                    throw new IllegalActionException(
                            "Failed to converge after " + _maxIterations
                            + " iterations.");
                }
            } while (!_converged && !_stopRequested);

            if (_debugging && _converged) {
                _debug("Iteration converged after " + _iterationCount
                        + " iterations.");
            }
        }

        /** Return the new iterate of a Newton step.
         *
         * @param x The best known iterate.
         * @param f The function value f(x)=g(x)-x.
         * @return The new guess for the solution f(x) = 0.
         *
         * @exception IllegalActionException If the solver fails to find a solution.
         */
        protected double[] _newtonStep(final double[] x, final double[] f)
                throws IllegalActionException {
            final int n = x.length;

            double[] xNew = new double[n];
            double[] fNew = new double[n];
            System.arraycopy(x, 0, xNew, 0, n);
            // Jacobian of f(.)
            double[][] J = new double[n][n];
            // Loop over each independent variable, and fill the Jacobian.
            // The loop function is g(x), and we attempt to solve f(x) = g(x)-x.
            // Hence, the Jacobian can be approximated by
            // J[i][k] = (f_new[k] - f[k])/dX[i]
            for (int i = 0; i < n; i++) {
                final double xOri = xNew[i];
                xNew[i] += _deltaX[i];
                _residual(xNew, fNew);
                for (int k = 0; k < n; k++) {
                    J[i][k] = (fNew[k] - f[k]) / _deltaX[i];
                }
                // Reset the coordinate to its old value
                xNew[i] = xOri;
            }
            
            // Check whether Jacobian is invertible.
            // 
            // For now, we reject the problem. An improvement will be to try to recover from this,
            // for example by switching the solver, trying a different start value, adding
            // a perturbation, increasing the precision of the Jacobian approximation, 
            // adding relaxation, and/or some other means.
            final double det = DoubleMatrixMath.determinant(J);
            if (Math.abs(det) < 1E-5) {
                StringBuffer message = new StringBuffer();
                message.append("Singular Jacobian in Newton step. Reformulate equation or try different start values.\n");
                message.append("Break variables:\n");
                for (String name : _variableNames) {
                    message.append("    ");
                    message.append(name);
                    message.append("\n");
                }
                message.append("Jacobian = ");
                message.append(DoubleMatrixMath.toString(J));
                message.append("\n");
                message.append("Determinant = ");
                message.append(det);
                throw new IllegalActionException(message.toString());
            }

            // Solve J * d = -f(x_n) for d = x_{n+1}-x{n}
            // to get the Newton step.
            if (n == 1) {
                final double d = -f[0] / J[0][0];
                xNew[0] = x[0] + d;
            } else {
                final double[] d = _gaussElimination(J, f);
                xNew = DoubleArrayMath.subtract(x, d);
            }
            return xNew;
        }

        /** Return vector x that solves A*x=f by a Gauss elimination
         *  with normalization and interchange of rows.
         *
         * @param A A square matrix
         * @param f Array with solution of A*x=f
         * @return x Array x = A^-1 * f
         */
        protected double[] _gaussElimination(final double[][] A, final double[] f) {
            int i, j, k, piv, iMax, jMax;
            final int dim = f.length;
            final int dimP1 = dim + 1;
            double[] r = new double[dim];
            double[][] B = new double[dim][dimP1];
            double[] tempRow = new double[dimP1];
            double a, pivotElement;
            double aMax = -1;

            for (i = 0; i < dim; i++) {
                for (j = 0; j < dim; j++) {
                    B[i][j] = A[i][j];
                }
                B[i][dim] = f[i];
            }

            for (piv = 0; piv < dim; piv++) {
                //interchange rows if necessary
                iMax = 0;
                jMax = 0;
                for (i = 0; i < dim; i++) {
                    for (j = dim - 1; j >= 0; j--) {
                        if (Math.abs(B[i][j]) > aMax) {
                            aMax = Math.abs(B[i][j]);
                            iMax = i;
                            jMax = j;
                        }
                    }
                }

                if (iMax != jMax) {
                    for (i = 0; i < dimP1; i++) {
                        tempRow[i] = B[iMax][i];
                        B[iMax][i] = B[jMax][i];
                        B[jMax][i] = tempRow[i];
                    }
                }

                pivotElement = B[piv][piv];

                // Normalization of pivot row.
                for (j = 0; j < dimP1; j++) {
                    B[piv][j] = B[piv][j] / pivotElement;
                }

                // Elimination.
                for (k = 0; k < dim; k++) {
                    if (piv != k) {
                        a = B[k][piv];
                        for (j = 0; j < dimP1; j++) // set new row
                        {
                            B[k][j] = B[k][j] - a * B[piv][j];
                        }
                    }
                }
            }

            for (i = 0; i < dim; i++) {
                r[i] = B[i][dim];
            }

            return r;
        }

        /** Evaluate the residual function f(x) = x-g(x).
         *
         *  @param x Input to the loop function g(x).
         *  @param f Double vector of the same size as x. The result will be stored in this function.
         *
         *  @exception IllegalActionException If the prefire() method
         *   returns false having previously returned true in the same
         *   iteration, or if the prefire() or fire() method of the actor
         *   throws it, or if evaluating the function yields a value that
         *   is not a double.
         */
        protected void _residual(final double[] x, double[] f)
                throws IllegalActionException {
            double[] g = new double[_nVars];
            _evaluateLoopFunction(x, g);
            _iterationCount++;
            for (int i = 0; i < _nVars; i++) {
                f[i] = x[i] - g[i];
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////             protected variables                           ////

        /** Step size for finite difference approximation */
        protected double[] _deltaX;

        /** Temporary variable used to store the result of f(x) = x-g(x) */
        protected double[] _f;

    }

    /**
     * Class for solving algebraic loops using a homotopy method.
     *
     * @author Michael Wetter
     */
    class Homotopy extends AlgebraicLoopSolver {

        /** Construct an algebraic loop solver.
         *
         *
         *  @param variableNames Names of each break variable.
         *  @param tolerance Tolerance for each variable.
         *  @param maxIterations Maximum number of iterations.
         */
        public Homotopy(String[] variableNames, double[] tolerance,
                int maxIterations) throws IllegalActionException {
            super(variableNames, tolerance, maxIterations);
            _converged = false;
            _n1 = _nVars+1;

            // Initialize step size for Jacobian calculation.
            _deltaX = new double[_nVars];
            for (int i = 0; i < _nVars; i++) {
                // FIXME: _deltaX should take into account the scaling of the variable.
                _deltaX[i] = 1E-5;
            }
            /* Current guess of solution */
            _y = new double[_nVars];
            /* Matrices _b and _q are used in the Newton algorithm. */
            _b = new double[_nVars + 1][_nVars];
            _q = new double[_nVars + 1][_nVars + 1];
            _t = new double[_nVars + 1];
            _r = new double[_nVars];

            // Allocate storage for initial value
            _xIni = new double[_nVars];
            
            // Solver parameters
            _ctmax = 0.8;
            _dmax = 0.2;
            _dmin = 0.001;
            _hmax = 1.28;
            _hmin = 0.000001;
            _hmn = 0.00001;
            _h = 0.32;
            _cdmax = 1000.0;
            _angmax = Math.PI / 3.0;
            _acfac = 2.0;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** This method solves u - lambda F(u) = 0
         *  with initial values u=0 and lambda=0.
         *
         * The solution, for lambda=1, is a fixed point of F : Re^n -> Re^n.
         * 
         * To allow a non-zero initial guess for the loop function, the problem 
         * is reformulated as H(s, lambda, x0) = s - lambda (g(s+x0)-x0),
         * where lambda has an initial value of 0 and is successively increased to 1,
         * s is a coordinate transformation defined so that x = s+x0, where
         * x0 is the initial iterate.
         * 
         * The Jacobian is approximated at the start of the solution using finite differences,
         * and then updated using Broyden's method.
         *
         * The method starts with computing the Jacobian H'(x)=A, where
         * x=(s, lambda). 
         * Next, it computes the tangent vector t = t(A).
         * It then conducts a predictor Euler step u = x+h*t.
         * After computing a perturbation vector pv, it corrects the iterates using
         * v = u - A^+ (H(u)-pv), where A^+ is the Moore-Penrose inverse of A.
         *
         *  @param xIni Array with the initial values of the variables, which will be replaced
         *              by this method with the obtained solution.
         * @exception IllegalActionException If the prefire() method
         *              returns false having previously returned true in the same
         *              iteration, or if the prefire() or fire() method of the actor
         *              throws it, or if evaluating the function yields a value that
         *              is not a double, or if the solver fails to find a solution.
         */
        @Override
        public void solve(double[] xIni) throws IllegalActionException {

            _iterationCount = 0;
            // Store initial guess
            System.arraycopy(xIni, 0, _xIni, 0, _nVars);

            final int _n1 = _nVars + 1;

            // Solution vector x1 is x in the first n elements,
            // and the n+1-th element is the homotopy factor.
            double[] x1 = new double[_n1];
            // Predictor.
            double[] u = new double[_n1];
            // Corrector.
            double[] v = new double[_n1];

            boolean switchToNewton = false;

            // Set x1 to zero. The initial value xIni will be taken into account
            // when the loop function is evaluated.
            for(int i = 0; i < _n1; i++){
               x1[i] = 0.0;
            }

            boolean _doNewtonStep = false;
            // Compute transpose of the Jacobian at x.
            // This method also assigns _y.
            _b = _jac(x1, _h);

            // Compute _b and _q, the orthogonal decompositions of _b.
            double cond = _decomp();
            
            // Check condition number of initial point.
            if (cond > _cdmax) {
                throw new IllegalActionException("Bad condition number '"
                        + cond
                        + "' of initial point. Select different initial point.");
            }
            
            // Save the tangent vector
            for (int k = 0; k < _n1; k++) {
                _t[k] = _q[_nVars][k];
            }
            
            // Set the orientation for search.
            final double or = _getOrientation();

            // Main iteration loop.
            double[] w = new double[_nVars];
            while (!_stopRequested) {

                while (!_stopRequested && !switchToNewton) {
                    if (Math.abs(_h) < _hmin) {
                        StringBuffer message = new StringBuffer();
                        message.append("Failure at minimum step size after "
                                + _iterationCount + " function evaluations.\n");
                        message.append("Last solution vector was "
                                + DoubleArrayMath.toString( DoubleArrayMath.add(w, _xIni) ) + "\n");
                        message.append("with homotopy factor lambda = "
                                + x1[_nVars] + "\n");
                        message.append("(lambda should be 1 at solution.)\n");
                        throw new IllegalActionException(message.toString());
                    }
                    if (_iterationCount > _maxIterations) {
                        throw new IllegalActionException(
                                "Maximum number of function evaluations exceeded.");
                    }
                    // Save tangent vector.
                    for (int k = 0; k < _n1; k++) {
                        _t[k] = _q[_nVars][k];
                    }
                    // Do a predictor step.
                    for (int k = 0; k < _n1; k++) {
                        u[k] = x1[k] + _h * or * _t[k];
                    }
                    // Evaluate the function for the value of the predictor step.
                    w = _map(u);

                    // Update predictor.
                    // This sets _test=true if a call to Newton should be done.
                    _updateQB(w, _angmax);

                    if (_test) {
                        // Newton corrector and update.
                        // If the step is a success, this call
                        // assigns _test = true and updates _r.
                        _newton(u, v, w);
                        if (_test) {
                            // Residual and contraction test are positive.
                            // Get out of the predictor corrector loop.
                            switchToNewton = true;
                        } else {
                            // Residual or contraction test is negative.
                            // Try a smaller step.
                            _h /= _acfac;
                        }
                    } else {
                        // PC step not accepted.
                        // Try a smaller step.
                        _h /= _acfac;
                    }
                }
                if (!_stopRequested) {
                    // Reset flag of the main iteration loop.
                    switchToNewton = false;

                    boolean succ = false;
                    // Switch to Newton step length.
                    if (v[_nVars] >= 1) {
                        _doNewtonStep = true;
                    }
                    if (_doNewtonStep) {
                        _h = -(v[_nVars] - 1.0) / _q[_nVars][_nVars];
                        if (Math.abs(_h) < _hmn) {
                            // Obtained minimum step length.
                            succ = true;
                        }
                    } else {
                        _h = Math.min(Math.abs(_h) * _acfac, _hmax);
                    }
                    // Assign new point on curve.
                    System.arraycopy(v, 0, x1, 0, _n1);

                    // Assign y = H(x).
                    System.arraycopy(_r, 0, _y, 0, _nVars);

                    if (succ) {
                        // Copy the solution vector to the function argument.
                        for(int i = 0; i < _nVars; i++){
                            xIni[i] = x1[i]+_xIni[i];
                        }
                        // Stop the curve tracing.
                        return;
                    }
                }
            }
        }

        /** Evaluate the transpose of the Jacobian of H(x, lambda) = x - lambda F(x)
         *  by using forward differences.
         *
         * @param x Point at which the Jacobian is approximated.
         * @param h Step size.
         * @return The transpose of the Jacobian.
         * @exception IllegalActionException If the prefire() method
         *  returns false having previously returned true in the same
         *  iteration, or if the prefire() or fire() method of the actor
         *  throws it, or if evaluating the function yields a value that
         *  is not a double.
         */
        double[][] _jac(double[] x, double h) throws IllegalActionException {
            double[][] b = new double[_n1][_nVars];
            // Note that the original implementation of Allgower uses h for the step
            // size in all n1 coordinates. Our implementation uses h*_deltaX[i] for i = 0, ..., n1-2,
            // and h for i = n1-1. This is done to take the scaling of the variable into account.
            final double[] hNewton = new double[_n1];
            for (int i = 0; i < _nVars; i++){
                hNewton[i] = h*_deltaX[i];
            }
            hNewton[_nVars] = h;
            for (int i = 0; i < _n1; i++) {
                x[i] = x[i] + hNewton[i];
                // Here, we use _y as a temporary storage, as it will be reset below.
                _y = _map(x);
                x[i] = x[i] - hNewton[i];
                for (int k = 0; k < _nVars; k++) {
                    b[i][k] = _y[k];
                }
            }
            // Store the current function value in _y.
            _y = _map(x);
            for (int i = 0; i < _n1; i++) {
                for (int k = 0; k < _nVars; k++) {
                    b[i][k] = (b[i][k] - _y[k]) / hNewton[i];
                }
            }
            return b;
        }

        /** Compute y = H(x), where H(x)=0 is curve to be traced.
         *
         * @param x Independent variable where the last element is the homotopy factor.
         * @return y = H(x).
         * @exception IllegalActionException If the prefire() method
         *  returns false having previously returned true in the same
         *  iteration, or if the prefire() or fire() method of the actor
         *  throws it, or if evaluating the function yields a value that
         *  is not a double.
         */
        double[] _map(final double[] x) throws IllegalActionException {
            
            double[] g = new double[_nVars];
            double[] y = new double[_nVars];

            // Add transformation of x that takes into account the initial value,
            // as the implementaiton of Allgower and Georg uses _xIni=0. 
            for(int i = 0; i < _nVars; i++){
                y[i] = x[i]+_xIni[i];
            }
            _evaluateLoopFunction(y, g);
            _iterationCount++;
            System.arraycopy(x, 0, y, 0, _nVars);
            for (int i = 0; i < _nVars; i++) {
                // Subtact _xIni from the loop function due to the above transformation,
                // and multiply the difference with the homotopy factor that is stored in x[_nVars]. 
                y[i] -= x[_nVars] * (g[i]-_xIni[i]);
            }
            if (_debugging){
               _debug("Obtained y = " + DoubleArrayMath.toString(y) + "\n" +
                      "with lambda = " + x[_nVars]);
            }
            return y;
        }

        /** Return the direction in which the curve will be traversed.
         *
         * @return Direction in which the curve will be traversed.
         */
        protected double _getOrientation() {
            return (_t[_nVars] > 0) ? 1.0 : -1.0;
        }

        /** Perform a Givens rotation.
         *
         * This method performs a Givens rotation on _b and _q.
         * Prior to calling this method, _c1 and _c2 need to be set.
         * The method then uses _c1 and _c2, and sets them to new values.
         * A method that calls _givens then need to use _c1 and _c2.
         * This was needed as Java passes double by value and not be reference.
         *
         * @param l1 Coordinate to be acted upon.
         * @param l2 Coordinate to be acted upon.
         * @param l3 Coordinate to be acted upon.
         */
        void _givens(int l1, int l2, int l3) {
            if (Math.abs(_c1) + Math.abs(_c2) == 0.0) {
                return;
            }
            double sn;
            if (Math.abs(_c2) >= Math.abs(_c1)) {
                sn = Math.sqrt(1. + Math.pow(_c1 / _c2, 2.0)) * Math.abs(_c2);
            } else {
                sn = Math.sqrt(1. + Math.pow(_c2 / _c1, 2.0)) * Math.abs(_c1);
            }
            final double s1 = _c1 / sn;
            final double s2 = _c2 / sn;
            for (int k = 0; k < _nVars + 1; k++) {
                final double sv1 = _q[l1][k];
                final double sv2 = _q[l2][k];
                _q[l1][k] = s1 * sv1 + s2 * sv2;
                _q[l2][k] = -s2 * sv1 + s1 * sv2;
            }
            for (int k = l3; k < _nVars; k++) {
                final double sv1 = _b[l1][k];
                final double sv2 = _b[l2][k];
                _b[l1][k] = s1 * sv1 + s2 * sv2;
                _b[l2][k] = -s2 * sv1 + s1 * sv2;
            }
            _c1 = sn;
            _c2 = 0.0;
            return;
        }

        /** Conduct a QR decomposition.
         *
         *  A QR decomposition for _b is stored in _q and _b by
         *  using Givens rotation on _b an _q until
         *  _b is upper triangular.
         *  A very coarse condition estimate is returned.
         *
         *  @return A very coarse condition estimate.
         */
        double _decomp() {

            for (int k = 0; k < _n1; k++) {
                for (int m = 0; m < _n1; m++) {
                    _q[k][m] = 0.0;
                }
                _q[k][k] = 1.0;
            }
            // Successive Givens transformation.
            for (int m = 0; m < _nVars; m++) {
                for (int k = m + 1; k < _n1; k++) {
                    // Here we set _c1 and _c2, as these are class members.
                    // The original code uses these as input arguments to
                    // _givens(...), but we use them as class members
                    // as Java can only return single values.
                    _c1 = _b[m][m];
                    _c2 = _b[k][m];
                    _givens(m, k, m + 1);
                    _b[m][m] = _c1;
                    _b[k][m] = _c2;
                }
            }

            // Compute a very coarse condition estimate.
            double cond = 0.0;
            for (int i = 1; i < _nVars; i++) {
                for (int k = 0; k < i; k++) {
                    cond = Math.max(cond, Math.abs(_b[k][i] / _b[i][i]));
                }
            }
            return cond;
        }

        /** Conduct a Newton step.
         *
         * Conduct a Newton step v = u - A^+ where A is approximated by H'.
         * The matrix A^+ is the Moore-Penrose inverse of A.
         * This method uses perturbations to stabilize the method and
         * performs tests on the residuals and the contractions.
         *
         * @param u
         * @param w This argument is changed by this function.
         * @exception IllegalActionException If the prefire() method
         *  returns false having previously returned true in the same
         *  iteration, or if the prefire() or fire() method of the actor
         *  throws it, or if evaluating the function yields a value that
         *  is not a double.
         */
        protected void _newton(double[] u, double[] v, double[] w)
                throws IllegalActionException {

            double[] pv = new double[_nVars];
            double[] p = new double[_nVars];
            if (_debugging){
               _debug("Entered _newton.");
            }
            _test = true;
            // Perturb w
            for (int k = 0; k < _nVars; k++) {
                if (Math.abs(w[k]) > _deltaX[k]) {
                    pv[k] = 0.0;
                } else if (w[k] > 0.0) {
                    pv[k] = w[k] - _deltaX[k];
                } else {
                    pv[k] = w[k] + _deltaX[k];
                }

                w[k] = w[k] - pv[k];
            }
            final double d1 = _l2norm(w);
            if (d1 > _dmax) {
                if (_debugging){
                    _debug("Failed test on d1: " + d1 + " > " + _dmax);
                }
                _test = false;
                return;
            }
            for (int k = 0; k < _nVars; k++) {
                for (int m = 0; m < k; m++) {
                    w[k] = w[k] - _b[m][k] * w[m];
                }
                w[k] = w[k] / _b[k][k];
            }
            final double d2 = _l2norm(w);
            for (int k = 0; k < _n1; k++) {
                double s = 0.0;
                for (int m = 0; m < _nVars; m++) {
                    s += _q[m][k] * w[m];
                }
                v[k] = u[k] - s;
            }

            _r = _map(v);

            for (int k = 0; k < _nVars; k++) {
                p[k] = _r[k] - pv[k];
            }
            final double d3 = _l2norm(p);

            // Compute contraction
            final double contr = d3 / (d1 + _dmin);
            if (contr > _ctmax) {
                if (_debugging){
                    _debug("Failed contraction test 'contr > ctmax' as " + contr + " > " + _ctmax);
                }
                _test = false;
            }
            for (int k = _nVars - 2; k >= 0; k--) {
                _c1 = w[k];
                _c2 = w[k + 1];
                _givens(k, k + 1, k);
                w[k] = _c1;
                w[k + 1] = _c2;

            }
            for (int k = 0; k < _nVars; k++) {
                _b[0][k] -= p[k] / d2;
            }
            for (int k = 0; k < _nVars - 1; k++) {
                _c1 = _b[k][k];
                _c2 = _b[k + 1][k];
                _givens(k, k + 1, k);
                _b[k][k] = _c1;
                _b[k + 1][k] = _c2;

            }
            if (_b[_nVars - 1][_nVars - 1] < 0.0) {
                _test = false;
                _b[_nVars - 1][_nVars - 1] = -_b[_nVars - 1][_nVars - 1];
                for (int k = 0; k < _n1; k++) {
                    _q[_nVars - 1][k] = -_q[_nVars - 1][k];
                    _q[_nVars][k] = -_q[_nVars][k];
                }
            }
            // Perturb upper triangular matrix
            for (int i = 1; i < _nVars; i++) {
                for (int k = 0; k < i; k++) {
                    if (Math.abs(_b[k][i]) > _cdmax * Math.abs(_b[i][i])) {
                        if (_b[i][i] > 0) {
                            _b[i][i] = Math.abs(_b[k][i]) / _cdmax;
                        } else {
                            _b[i][i] = -Math.abs(_b[k][i]) / _cdmax;
                        }
                    }
                }
            }
            for (int k = 0; k < _nVars - 1; k++) {
                _b[k + 1][k] = 0.0;
            }
            return;
        }

        /** Update _q and _b arrays.
         *
         *  This method updates the _q and _b arrays using QR decomposition.
         *
         */
        protected void _updateQB(final double[] w, double angmax) {
            _test = true;
            // Update q and b.
            for (int k = 0; k < _nVars; k++) {
                _b[_nVars][k] = (w[k] - _y[k]) / _h;
            }
            for (int k = 0; k < _nVars; k++) {
                _c1 = _b[k][k];
                _c2 = _b[_nVars][k];
                _givens(k, _nVars, k);
                _b[k][k] = _c1;
                _b[_nVars][k] = _c2;

            }
            // Compute angle.
            double ang = 0.0;
            for (int k = 0; k < _n1; k++) {
                ang += _t[k] * _q[_nVars][k];
            }
            if (ang > 1.0) {
                ang = 1.0;
            }
            if (ang < -1.0) {
                ang = -1.0;
            }
            ang = Math.acos(ang);
            if (ang > angmax) {
                _test = false;
            }
            return;
        }

        /** Return the L2 norm.
         *
         * @param x Argument for which norm is returned.
         * @return the L2 norm.
         */
        protected double _l2norm(double[] x) {
            double r = 0;
            for (double ele : x) {
                r += ele * ele;
            }
            return Math.sqrt(r);
        }

        ///////////////////////////////////////////////////////////////////
        ////             protected variables                           ////
        /** Maximum contraction rate in corrector step, 0 < ctmax < 1. */
        protected double _ctmax; // See also algorithm 7.2.13 in Allgower and Georg 

        /** Maximal norm for H */
        protected double _dmax;

        /** Minimal norm for H */
        protected double _dmin;

        /** Maximal step size */
        protected double _hmax;

        /** Minimal step size */
        protected double _hmin;

        /** Minimal Newton step size */
        protected double _hmn;

        /** Initial step size */
        protected double _h;
        
        /** Number of independent variables including the homotopy factor */
        protected int _n1;

        /** Maximum for condition estimate */
        protected double _cdmax;

        /** Maximal angle */
        protected double _angmax;

        /** Acceleration factor for step length control */
        protected double _acfac;

        /** Matrix b used in Newton algorithm. */
        protected double[][] _b;
        /** Matrix q used in Newton algorithm. */
        protected double[][] _q;

        /** Tangent vector to homotopy curve. */
        protected double[] _t;

        /** Result of Newton step */
        protected double[] _r;

        /** Initial guess */
        protected double[] _xIni;
        
        /** Current guess of solution */
        protected double[] _y;

        /** Test for step length in Newton algorithm */
        protected boolean _test;

        /** Value c1 used in Newton algorithm. */
        protected double _c1;
        /** Value c2 used in Newton algorithm. */
        protected double _c2;

        /** Step size for finite difference approximation */
        protected double[] _deltaX;

    }

    /**
     * Class for solving algebraic loops using the su method.
     *
     * @author Michael Wetter
     */
    class SuccessiveSubstitution extends AlgebraicLoopSolver {

        /** Construct an algebraic loop solver.
         *
         *  @param variableNames Names of each break variable.
         *  @param tolerance Tolerance for each variable.
         *  @param maxIterations Maximum number of iterations.
         */
        public SuccessiveSubstitution(String[] variableNames,
                double[] tolerance, int maxIterations)
                        throws IllegalActionException {
            super(variableNames, tolerance, maxIterations);
        }

        /** Solve the algebraic loop using the specified array as the initial
         *  guess for the variables being solved for and replace the contents
         *  of the specified array with the solution that is found.
         *  <p>
         *  This method iterates until a solution is found. If it does not
         *  converge within the maximum number of iterations, it throws
         *  an IllegalActionException. A method that calls solve(double[] xInitial)
         *  can then call converged() to check whether the exception is thrown because
         *  of lack of convergence.
         *
         *  @param xIni Array with the initial values of the variables, to be replaced
         *   with the solution by this method.
         * @exception IllegalActionException If the prefire() method
         *  returns false having previously returned true in the same
         *  iteration, or if the prefire() or fire() method of the actor
         *  throws it, or if evaluating the function yields a value that
         *  is not a double, or if the solver fails to find a solution.
         */
        @Override
        public void solve(double[] xIni) throws IllegalActionException {
            _iterationCount = 0;
            final double[] xNew = new double[_nVars];
            do {
                // Evaluate the loop function to compute x_{n+1} = g(x_n).
                // This calls the loop function of the outer class.
                _evaluateLoopFunction(xIni, xNew);
                _iterationCount++;

                // Check for convergence
                _converged = true;

                for (int i = 0; i < xIni.length; i++) {
                    final double diff = Math.abs(xIni[i] - xNew[i]);
                    if (diff > Math.max(_tolerance[i], diff * _tolerance[i])) {
                        _converged = false;
                        break;
                    }
                }

                // Update iterate
                System.arraycopy(xNew, 0, xIni, 0, xIni.length);

                // Check for maximum number of iterations in case we did not yet converge.
                if (!_converged && _iterationCount > _maxIterations) {
                    throw new IllegalActionException(
                            "Failed to converge after " + _maxIterations
                            + " iterations.");
                }
            } while (!_converged && !_stopRequested);

            if (_debugging && _converged) {
                _debug("Iteration converged after " + _iterationCount
                        + " iterations.");
            }
        }
    }

}
