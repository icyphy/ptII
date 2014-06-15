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

 @author Edward A. Lee and Michael Wetter
 @version $Id: AlgebraicLoopDirector.java 65763 2013-03-07 01:54:37Z cxh $
 @since Ptolemy II 2.0
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
        method.addChoice("NewtonRaphson");
        method.addChoice("FixedPointIteration");
        method.setExpression("NewtonRaphson");

        maxIterations = new Parameter(this, "maxIterations");
        maxIterations.setTypeEquals(BaseType.INT);
        maxIterations.setExpression("1000");

        errorTolerance = new Parameter(this, "errorTolerance");
        errorTolerance.setTypeEquals(BaseType.DOUBLE);
        errorTolerance.setExpression("1E-4");
        
        AlgebraicLoopScheduler scheduler = new AlgebraicLoopScheduler(this,
                uniqueName("Scheduler"));
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

        int i = 0;
        for (IOPort port : _breakVariables) {
        	Receiver[][] receivers = port.getReceivers();
        	for (Receiver[] receivers2 : receivers) {
            	for (Receiver receiver : receivers2) {
                    Token t = receiver.get();
                    if (t instanceof DoubleToken){
                    	_x_n[i] = ((DoubleToken)t).doubleValue();
                    	i++;
                    } else {
            			throw new IllegalActionException("Break variable is required to be a double. Got "
            					+ t + " on port " + port.getName(getContainer()));
                    }
            	}
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

    /** Return g(x).
     *  This function is called by the solver to evaluate the loop function.
     *  @param x Input to the loop function.
     *  @return Result of the loop function.
     *  @exception IllegalActionException If the prefire() method
     *   returns false having previously returned true in the same
     *   iteration, or if the prefire() or fire() method of the actor
     *   throws it, or if evaluating the function yields a value that
     *   is not a double.
     */
    protected double[] _evaluateLoopFunction(double[] x)
            throws IllegalActionException{
        // Set the argument to the receivers
        int iRec=0;
        for (IOPort port : _breakVariables) {
        	Receiver[][] receivers = port.getReceivers();
        	for (Receiver[] receivers2 : receivers) {
            	for (Receiver receiver : receivers2) {
            		DoubleToken t = new DoubleToken(x[iRec]);
            		receiver.put(t);
            		if (_debugging) {
            			_debug("Setting input to loop function for '" + receiver.getContainer().getName() + "' to " + x[iRec]);
            		}
            		iRec++;
            	}
        	}
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
        // Get the values from the receivers, and return them
        double[] g = new double[_nVars];

        int i = 0;
        for (IOPort port : _breakVariables) {
        	Receiver[][] receivers = port.getReceivers();
        	for (Receiver[] receivers2 : receivers) {
            	for (Receiver receiver : receivers2) {
            		// Store g(x_n)
            		Token t = receiver.get();
            		if (t instanceof DoubleToken){
            			g[i] = ((DoubleToken)(t)).doubleValue();
            			if (_debugging){
            				_debug("Output of loop function at '" + port.getName(getContainer()) + "' = " + g[i]);
            			}
            			i++;
            		} else {
            			throw new IllegalActionException("Break variable is required to be a double. Got "
            					+ t + " on port " + port.getName(getContainer()));
            		}
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

        _isNewtonRaphson = method.stringValue().equals("NewtonRaphson");

        // Set up a list of all receivers that contain the values where the
        // iterate x needs to be stored. 
        // These are the input ports associated with the break variables.
        // These are all receivers that have a default value and that are an input.
        _breakVariables = new LinkedList<IOPort>();
        CompositeEntity container = (CompositeEntity)getContainer();
        @SuppressWarnings("unchecked")
		List<Actor> actors = container.deepEntityList();
        _nVars = 0;
        for (Actor actor : actors) {
        	@SuppressWarnings("unchecked")
			List<IOPort> inputPorts = actor.inputPortList();
        	for (IOPort port : inputPorts) {
        		// If the port has a default value, then all its receivers are break variables.
                if (port.defaultValue.getToken() != null){
                	// Break any causality relation between this input and all outputs.
                	CausalityInterface causality = actor.getCausalityInterface();
                	List<IOPort> outputPorts = actor.outputPortList();
                	for (IOPort output : outputPorts) {
                		causality.removeDependency(port, output);
                	}
                	_breakVariables.add(port);
                	// Count the number of receivers it has.
                	int numberOfReceivers = 0;
                	Receiver[][] receivers = port.getReceivers();
                	for (Receiver[] receivers2 : receivers) {
                		numberOfReceivers += receivers2.length;
                	}
                	if (_debugging) {
                		_debug("Break variable: " + port.getName(getContainer())
                				+ ", which has " + numberOfReceivers + " values.");
                	}
                	_nVars += numberOfReceivers;
                }
        	}
        }        
        _x_n = new double[_nVars];
        _g_n = new double[_nVars];
        _tolerance = new double[_nVars];
        
        // Instantiate the numerical solver
        // Get the maximum number of iterations
        final int maxIterationsValue = ((IntToken)(maxIterations.getToken())).intValue();

        // Get the variable names and the tolerance.
        final String[] variableNames = new String[_nVars];
        int i = 0;
        for (IOPort port : _breakVariables) {
        	Receiver[][] receivers = port.getReceivers();
        	for (Receiver[] receivers2 : receivers) {
            	for (Receiver receiver : receivers2) {
                    Token t = receiver.get();
                    final String name = port.getName(getContainer());
                    if (t instanceof DoubleToken){
                    	variableNames[i] = name;                    	
                    	_tolerance[i] =_getErrorTolerance( port );
                    	i++;
                    } else {
            			throw new IllegalActionException("Break variable is required to be a double. Got "
            					+ t + " on port " + name);
                    }
            	}
        	}
        }
        // Instantiate the solver.
        _solver = new AlgebraicLoopSolver(variableNames, _tolerance, maxIterationsValue);
        
    }

    /** Return a new FixedPointReceiver. If a subclass overrides this
     *  method, the receiver it creates must be a subclass of FixedPointReceiver,
     *  and it must add the receiver to the _receivers list (a protected
     *  member of this class).
     *  @return A new FixedPointReceiver.
     */
    public Receiver newReceiver() {
    	AlgebraicLoopReceiver receiver = new AlgebraicLoopReceiver(this);
        return receiver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

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
                        _debug("Set output "
                                + outputPort.getFullName() + " to absent.");
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

    ///////////////////////////////////////////////////////////////////
    ////                   protected variables                     ////
        
    /** The list of ports for all break variables */
    protected List<IOPort> _breakVariables;

    /** Current value of the loop function g(x_n) */
    protected double[] _g_n;
        
    /** Flag to indicate that it is the NewtonRaphson method */
    protected boolean _isNewtonRaphson;
    
    /** Number of break variables */
    protected int _nVars;

    /** Algebraic loop solver */
    AlgebraicLoopSolver _solver;
    
    /** Tolerance for each iteration variable */
    protected double[] _tolerance;

    /** Current value of the iteration variables x_n */
    protected double[] _x_n;

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

        /** Construct an algebraic loop solver.
         *  @param variableNames Names of each break variable.
         *  @param tolerance Tolerance for each variable.
         *  @param maxIterations Maximum number of iterations.
         */
        public AlgebraicLoopSolver(
        		String[] variableNames,
                double[] tolerance,
                int maxIterations){
        	_variableNames = variableNames;
            _tolerance = tolerance;
            _maxIterations = maxIterations;
            _converged = false;
            _iterationCount = 0;
            // Set the step size for the Newton method.
            // FIXME: This should take the scaling of the variable into account.
            if (_isNewtonRaphson){
                _deltaX = new double[tolerance.length];
                for (int i = 0; i < tolerance.length; i++){
                    _tolerance[i] = 1E-5;
                    // FIXME: _deltaX should take into account the scaling of the variable.
                    //        It should also be adaptive.
                    _deltaX[i] = 1E-5;
                }
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////
        
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
         *  @param x Array with the initial values of the variables, to be replaced
         *   with the solution by this method.
         *  @exception IllegalActionException If the prefire() or fire() method
         *   of an actor throws it.
         *  @exception IllegalActionException If the solver fails to find a solution.
         */
        public void solve(double[] x)
        		throws IllegalActionException{
            _iterationCount = 0;
            double[] g = new double[_nVars];
            do {
            	double[] xNew;
                // Evaluate the loop function to compute x_{n+1} = g(x_n).
                // This calls the loop function of the outer class.
                if (_isNewtonRaphson) {
                	if (_iterationCount == 0){
                        g = _evaluateLoopFunction(x);
                	}
                    xNew = _newtonStep(x, g);
                } else {
                    // Successive substitution
                    xNew = _evaluateLoopFunction(x);
                }
                _iterationCount++;

                // Check for convergence
                _converged = true;

                // For the NewtonRaphson, we do not compare x and xNew, but rather xNew and g(xNew).
                // Otherwise, the test may indicate convergence if the Newton step is small.
            	if (_isNewtonRaphson){
            		g = _evaluateLoopFunction(xNew);
            	}
                for(int i = 0; i < x.length; i++){
                    final double diff = _isNewtonRaphson ? Math.abs(xNew[i]-g[i]) : Math.abs(x[i] - xNew[i]);
                    if (diff > Math.max(_tolerance[i], diff*_tolerance[i])){
                        _converged = false;
                        break;
                    }
                }

                // Update iterate
                System.arraycopy(xNew, 0, x, 0, x.length);
                
                // Check for maximum number of iterations in case we did not yet converge.
                if (!_converged && _iterationCount > _maxIterations) {
                    throw new IllegalActionException("Failed to converge after " + _maxIterations + " iterations.");
                }
            } while (!_converged && !_stopRequested);
            if (_debugging && _converged){
            	_debug("Iteration converged after " + _iterationCount + " iterations.");
            }
        }

        /** Return the new iterate of a Newton step. 
         * 
         * @param x The best known iterate.
         * @param g The function value g(x).
         * @return The new guess for the solution.
         * @exception IllegalActionException If the solver fails to find a solution.
         */
        protected double[] _newtonStep(final double[] x, final double[] g)
        		throws IllegalActionException {
            final int n = x.length;

            double[] xNew = new double[n];
            System.arraycopy(x, 0, xNew, 0, n);
            // Jacobian
            double[][] J = new double[n][n];
            // Loop over each independent variable, and fill the Jacobian
            for (int i = 0; i < n; i++){
                final double xOri = xNew[i];
                xNew[i] += _deltaX[i];
                final double [] gNew = _evaluateLoopFunction(xNew);
                for(int k = 0; k < n; k++){
                    J[i][k] = (gNew[k]-g[k])/_deltaX[i];
                }
                // Reset the coordinate to its old value
                xNew[i] = xOri;
            }
            // Check whether Jacobian is invertible
            // FIXME: For now, we reject the problem. An improvement will be to try to recover from this,
            //        for example by switching the solver, trying a different start value, increasing the 
            //        precision of the Jacobian approximation, adding relaxation, and/or some other means.
            final double det = DoubleMatrixMath.determinant(J);
            if (Math.abs(det) < 1E-5){
            	final String LS = System.getProperty("line.separator");
                String em = "Singular Jacobian in Newton step. Reformulate equation or try different start values."
                             + LS
                             + "Break variables: " + LS;
                for(String name : _variableNames){
                	em += "    " + name + LS;
                }
                em += "Jacobian = " + DoubleMatrixMath.toString(J)
                      + LS
                      + "Determinant = " + det;
                throw new IllegalActionException(em);
            }
            
            // Solve J * d = -g(x_n) for d = x_{n+1}-x{n} 
            // to get the Newton step.
            if (n == 1){
                final double d = -g[0]/J[0][0];
                xNew[0] = x[0] + d;
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
        public double[] gaussElimination(double[][] A, double[] f) {
        	int i, j, k, piv, iMax, jMax;
        	int dim = f.length;
        	int dimP1 = dim + 1;
        	double[]   r = new double[dim];
        	double[][] B = new double[dim][dimP1];
        	double[]   tempRow = new double[dimP1];
        	double a, pivotElement;
        	double aMax = -1;

        	for (i = 0; i < dim; i++) {
        		for (j = 0; j < dim; j++)
        			B[i][j] = A[i][j];
        		B[i][dim] = f[i];
        	}

        	for (piv = 0; piv < dim; piv++) {
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
         *  @return true if the solver converged, false otherwise.
         */
        public boolean converged() {
            return _converged;
        }

        /** Return the number of iterations done in the last call to the method solve(double[]).
         *  @return The number of iterations
         */
         public int getIterationCount() {
             return _iterationCount;
         }

        ///////////////////////////////////////////////////////////////////
        ////             protected variables                           ////

         /** Flag that indicates whether the solver converged */
         protected boolean _converged;

        /** Step size for finite difference approximation */
        protected double[] _deltaX;
        
        /** Number of iterations in the last call to the function solve(double[]) */
        protected int _iterationCount;
        
        /** Maximum number of iterations */
        protected int _maxIterations;
        
        /** Local view of the tolerance vector. */
        protected double[] _tolerance;

        /** Variable names, used for error reporting */
        protected String[] _variableNames;
        
    }
}
