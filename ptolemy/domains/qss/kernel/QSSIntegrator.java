/* A quantized-state integrator.
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2014-2015 The Regents of the University of California.
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
package ptolemy.domains.qss.kernel;

import java.util.List;

import org.ptolemy.qss.solver.QSSBase;
import org.ptolemy.qss.util.DerivativeFunction;
import org.ptolemy.qss.util.ModelPolynomial;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// QSSIntegrator

/**
A quantized-state integrator.
This integrator is designed to integrate continuous-time signals under the
{@link QSSDirector}. The input events indicate significant changes in the input
signal, and output events indicate significant changes in the output signal.
The input signal is the derivative of the output.
Here "significant" means that the signal has changed by more than the specified
quantum. 

FIXME: To do:
- Document this better.
- Add an Impulse port, and make xInit a PortParameter.
- Add an output that bundles the input to generate a SmoothToken.
- Make a vector version.

@see QSSDirector
@author Edward A. Lee, Thierry Nouidui, Michael Wetter
@version $Id$
@since Ptolemy II 11.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
public class QSSIntegrator extends TypedAtomicActor implements DerivativeFunction {

    /** Construct a new instance of this integrator.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If setting up ports and parameters fails.
     *  @exception NameDuplicationException If the container already contains an object with this name.
     */
    public QSSIntegrator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        u = new TypedIOPort(this, "u", true, false);
        u.setTypeEquals(BaseType.DOUBLE);

        q = new TypedIOPort(this, "q", false, true);
        q.setTypeEquals(BaseType.DOUBLE);

        xInit = new Parameter(this, "xInit");
        xInit.setTypeEquals(BaseType.DOUBLE);
        xInit.setExpression("0.0");

	solver = new StringParameter(this, "solver");
	QSSDirector.configureSolverParameter(solver, "");

        absoluteTolerance = new Parameter(this, "absoluteTolerance");
        absoluteTolerance.setTypeEquals(BaseType.DOUBLE);
        
        relativeTolerance = new Parameter(this, "relativeTolerance");
        relativeTolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If specified, the minimum quantum for this integrator.
     *  This is a double, and by default is not given, which means
     *  that the quantum is specified by the director.
     */
    public Parameter absoluteTolerance;

    /** Output (the quantized state). */
    public TypedIOPort q;
    
    /** If specified, the relative quantum for this integrator.
     *  If the value here is greater than zero, then the quantum
     *  that this integrator uses will be the larger of the
     *  {@link #absoluteTolerance} and |x| * relativeTolerance,
     *  where x is the current value of the state.
     *  This is a double that defaults to be empty (nothing
     *  specified), which causes the relativeTolerance to be
     *  retrieved from the director.
     */
    public Parameter relativeTolerance;

    /** The class name of the QSS solver used for integration.  This
     *  is a string that defaults to the empty string, which delegates
     *  the choice to the director.
     */
    public StringParameter solver;

    /** Input (the derivative). */
    public TypedIOPort u;

    /** Initial value of the state. */
    public Parameter xInit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Declare that the output does not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(u, q, 0.0);
    }

    /** Set the derivative equal to the input.
     *  @return FIXME: What does the return value mean???
     */
    @Override
    public int evaluateDerivatives(Time time, double[] xx, double[] uu,
	    double[] xdot) throws IllegalActionException {
	
        // Check assumptions.
        assert (xx.length == 1);
        assert (xdot.length == 1);

        // The derivative is equal to the input.
        xdot[0] = uu[0];
        
        return 0;
    }

    /** Return 0. This actor does not provide directional derivatives.
     *  @return 0.
     */
    @Override
    public double evaluateDirectionalDerivatives(int idx, double[] xx_dot,
	    double[] uu_dot) throws IllegalActionException {
	return 0;
    }

    /** If it is time to produce a quantized output, produce it.
     *  Otherwise, indicate that the output is absent.
     *  @exception IllegalActionException If sending an output fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Current time is: " + currentTime);
        }
        
        if (_qssSolver.getCurrentSimulationTime().compareTo(currentTime) < 0) {
            // The current simulation time is ahead of the solver's time.
            // Catch up by integrating to current time.
            try {
                List<Integer> events = _qssSolver.advanceToTime(currentTime);
                int outputWidth = q.getWidth();
                for (Integer event : events) {
                    if (event < outputWidth) {
                        double[] model = _qssSolver.getStateModel(0).coeffs;
                        // FIXME: Seems like if the input is available,
                        // then we could read it now and send out a SmoothToken with
                        // the value and its derivative. How to do that?
                        // Also, we should be sending the whole model.
                        Token token = new SmoothToken(model, currentTime);
                        q.send(0, token);
                        if (_debugging) {
                            _debug("Send to output: " + token);
                        }
                    }
                }
            } catch (Exception ee) {
                throw new IllegalActionException(this, ee, ee.getMessage());
            }
        } else if (_firstRound) {
            // If this is the first firing, then produce an output even
            // if there has not been any change by a quantum.
            double[] model = _qssSolver.getStateModel(0).coeffs;
            // FIXME: Seems like if the input is available,
            // then we could read it now and send out a SmoothToken with
            // the value and its derivative. How to do that?
            // Also, we should be sending the whole model.
            Token token = new SmoothToken(model, currentTime);
            q.send(0, token);
            if (_debugging) {
                _debug("Send to output: " + token);
            }
        }
    }

    /** Return 1, because this actor always has one input variable,
     *  which specifies that value of the derivative.
     *  @return 1.
     */
    @Override
    public int getInputVariableCount() {
	return 1;
    }

    /** Return false, as this actor does not provide directional derivatives.
     *  FIXME: What is a directional derivative?
     *  @return False.
     */
    @Override
    public boolean getProvidesDirectionalDerivatives() {
	return false;
    }

    /** Return 1, as there is one state variable.
     *  @return 1.
     */
    @Override
    public int getStateCount() {
	return 1;
    }

    /** Initialize this actor to indicate that no input has yet been provided.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        // Get director and check its type.
        Director director = getDirector();
        if (!(director instanceof QSSDirector)) {
            throw new IllegalActionException(
                    this,
                    String.format(
                            "Director %s cannot be used for QSS, which requires a QSSDirector.",
                            director.getName()));
        }
        _director = (QSSDirector) director;
        final Time currentTime = _director.getModelTime();

        // Create a new QSS solver and initialize it.
        // FIXME: The director is specifying the solver method here, but this actor should use
        // that specification only as a default.
        String solverSpec = solver.stringValue().trim();
        if (solverSpec.equals("")) {
            _qssSolver = _director.newQSSSolver();
        } else {
            _qssSolver = _director.newQSSSolver(solverSpec);
        }
        
        // Find the error tolerance.
        double absoluteToleranceValue;
        DoubleToken tolerance = (DoubleToken)absoluteTolerance.getToken();
        if (tolerance == null) {
            absoluteToleranceValue = _director.getErrorTolerance();
        } else {
            absoluteToleranceValue = tolerance.doubleValue();
        }
        double relativeToleranceValue;
        tolerance = (DoubleToken)relativeTolerance.getToken();
        if (tolerance == null) {
            // FIXME: When the director acquires a relativeTolerance, use that.
            relativeToleranceValue = _director.getErrorTolerance();
        } else {
            relativeToleranceValue = tolerance.doubleValue();
        }

        // Determine the maximum order of the input variables.
        // This is the maximum order of the state model.
        _maximumInputOrder = _qssSolver.getStateModelOrder();
        
        // Set up the solver to use this actor to specify the number of states (1)
        // and input variables (1), and to use this actor to calculate the derivative
        // of the states.
        _qssSolver.initialize(
        	this, 				// The derivative function implementer.
        	currentTime, 			// The simulation start time.
        	_director.getModelStopTime(), 	// The maximum time to an event.
        	absoluteToleranceValue, 	// The absolute quantum tolerance.
        	relativeToleranceValue, 	// The relative quantum tolerance.
        	_maximumInputOrder);		// The order of the input variables.

        // Initialize the state variable to match the {@link #xInit} parameter.
        double xInitValue = ((DoubleToken)xInit.getToken()).doubleValue();
        _qssSolver.setStateValue(0, xInitValue);
        
        // To make sure this actor fires at the start time, request a firing.
        getDirector().fireAtCurrentTime(this);
        
        _firstRound = true;
        _lastFireAtTime = null;
    }

    /** Return false, indicating that this actor can fire even if its
     *  input is unknown.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    /** Update the calculation of the next output time and request
     *  a refiring at that time.
     *  If there is a new input, read it and update the slope.
     *  @return True if the base class returns true.
     *  @exception IllegalActionException If reading inputs or parameters fails.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
	boolean result = super.postfire();
        Time currentTime = getDirector().getModelTime();
        
        // Look for an input token.
        // If none is provided, then assume an initial derivative of zero.
        double inputValue = 0.0;
        boolean newInput = false;
        if (u.hasToken(0)) {
            DoubleToken inputToken = (DoubleToken)u.get(0);
            inputValue = inputToken.doubleValue();
            newInput = true; 
            ModelPolynomial inputModel = _qssSolver.getInputVariableModel(0);
            inputModel.coeffs[0] = inputValue;
            // Get derivative information from the input, if present.
            if (inputToken instanceof SmoothToken) {
        	double[] derivatives = ((SmoothToken)inputToken).derivativeValues();     	
        	int factorial = 1;
        	for (int i = 1; i < _maximumInputOrder + 1; i++) {
        	    if (derivatives == null || derivatives.length < i) {
        	    	inputModel.coeffs[i] = 0.0;
        	    } else {
        	    	inputModel.coeffs[i] = derivatives[i-1]/factorial;
        	    	factorial = factorial * i;
        	    }
        	}      	
            }     
            inputModel.tMdl = currentTime;
        }

        if (_firstRound) {
            // FIXME: Why is this needed on the first round?
            _qssSolver.triggerQuantizationEvents(true);
            _firstRound = false;
        }
        // If input values have changed, trigger a rate event.
        if (newInput) {
            try {
        	_qssSolver.triggerRateEvent();
            } catch (Exception ee) {
        	throw new IllegalActionException(this, ee, "Triggering rate event failed.");
            }
        }
        // Find the next firing time, assuming nothing else in simulation changes
        // (no further inputs before that time).
        final Time possibleFireAtTime = _qssSolver.predictQuantizationEventTimeEarliest();
        if (_debugging) {
            _debug("Request refiring at " + possibleFireAtTime);
        }

        // Cancel previous firing time if necessary.
        // FIXME: Following is confusing and probably redundant.
        final boolean possibleDiffersFromLast = (null == _lastFireAtTime || possibleFireAtTime
                .compareTo(_lastFireAtTime) != 0);
        if (null != _lastFireAtTime // Made request before.
                && _lastFireAtTime.compareTo(currentTime) > 0 // Last request was
                                                            // not used.
                                                            // _lastFireAtTime >
                                                            // currentTime
                && possibleDiffersFromLast // Last request is no longer valid.
        ) {
            if (_debugging) {
                _debug("Canceling previous fireAt request at " + _lastFireAtTime);
            }
            _director.cancelFireAt(this, _lastFireAtTime);
        }

        // Request firing time if necessary.
        if (possibleDiffersFromLast && !possibleFireAtTime.isPositiveInfinite()) {
            getDirector().fireAt(this, possibleFireAtTime);
            _lastFireAtTime = possibleFireAtTime;
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** QSS director. */
    private QSSDirector _director;

    /** Flag identifying the first round of iterations of fire() and postfire()
     *  after initialize().
     */
    private boolean _firstRound = true;

    /** Track requests for firing. */
    private Time _lastFireAtTime;

    /** The QSS solver for this actor.
     *  It is an instance of the class given by the
     *  <i>QSSSolver</i> parameter of the director.
     */
    private QSSBase _qssSolver = null;
    
    /** Maximum input order. */
    private Integer _maximumInputOrder;
}
