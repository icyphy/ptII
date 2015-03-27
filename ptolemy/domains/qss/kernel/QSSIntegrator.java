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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.lib.conversions.SmoothToDouble;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// QSSIntegrator

/**
A quantized-state integrator.
This integrator is designed to integrate continuous-time signals under the
{@link QSSDirector}. The input events indicate significant changes in the input
signal, and output events indicate significant changes in the output signal.
The value of the input signal is the derivative of the output.
Here "significant" means that the signal has changed by more than the specified
quantum, as defined by the selected solver, explained in detail below.
<p>
Three types of solvers are provided:
<ol>
<li> <b>QSS1</b>: The input <i>u</i> is assumed to be piecewise constant.
<li> <b>QSS2</b>: The input <i>u</i> is assumed to be piecewise linear.
<li> <b>QSS3</b>: The input <i>u</i> is assumed to be piecewise quadratic.
</ol>
An input token can be instance of {@link DoubleToken} or {@link SmoothToken},
the latter of which potentially carries
not only a value at q time, but also zero or more derivatives
of the input signal at that time. To provide a piecewise linear input to a
QSS2 integrator, for example, you can specify an input with the expression
<tt>smoothToken(2.0, {1.0})</tt>, which specifies a value of 2.0 and a first
derivative of 1.0. All other derivatives are assumed to be zero.
A QSS1 integrator will ignore all derivatives on the input.
A QSS2 integrator will ignore all but the first derivative on the input.
A QSS3 integrator will ignore all but the first and second derivatives on the input.
If a {@link DoubleToken} is provided on the input, then all derivatives of the input
are assumed to be zero.
<p>
This integrator has two modes of operation, depending on the value of
<i>propagateInputDerivatives</i>. In both modes, the integrator will produce
an output whenever a <i>quantization event</i> occurs. For QSS1, a quantization
event occurs when the state of the integrator changes by the quantum (see below
for an explanation of the quantum). For example, if the input is a constant 1.0
and the quantum is 0.1, then an output will be produced every 0.1 seconds, because
the input specifies that the state has slope 1.0, so it will increase by the quantum
every 0.1 seconds. For QSS2, a quantization event occurs when
the derivative of the state changes by the quantum. For example, if the input
is piecewise linear with initial value 0.0 and first derivative 1.0 and the state
has initial value 0.0, then at the start, the state has value 0.0,
first derivative 0.0, and second derivative 1.0.
Because of the second derivative, as time elapses, the first derivative of the state
will increase. When it increases by the quantum, an output will be produced.
For QSS3, a quantization
event occurs when the second derivative changes by the quantum in a similar fashion.
<p>
Also, in both modes of operation, the integrator will produce an output whenever
it is initialized, and whenever it receives an <i>impulse</i> input event.
<p>
When an output is produced, its value will be the current state of the integrator.
In addition, depending on the solver, it may contain derivative information.
For QSS1, the input is semantically piecewise constant, so the output
is piecewise linear; hence each output event will be a SmoothToken
that is piecewise linear, with a first derivative equal to the most recently
received input value.
For QSS2, the output will have a first and second derivative obtained from the input.
For QSS3, the output will have first, second, and third derivatives.
<p>
We can now explain how the two modes of operation differ.
If <i>propagateInputDerivatives</i> is set to
true (the default), then this integrator will <i>also</i>
produce an output every time it receives an input.
Each output will include derivative
information from the input, to the extent that these are appropriate for
the solver.
In this case, the output has a direct dependence on the input, so a cycle
of instances of QSSIntegrator needs to contain at least one integrator that has
<i>propagateInputDerivatives</i> set to false, or else it has to contain
a {@link TimeDelay} actor. Otherwise, a causality loop blocks execution of the model.
<p>
If <i>propagateInputDerivatives</i> is set to
false, then output is not produced <i>only</i> when quantization events occur,
when the integrator is initialized, and when the <i>impulse</i> port receives an event.
In this case, there is no direct dependence between the <i>u</i> input and the output,
and hence there is no difficulty putting this integrator in a feedback loop.
The price paid, however, is that downstream actors do not get immediately informed
of changes in the derivatives of the output.
They will learn of these changes when the next quantization event occurs.
To see an example of the consequences, see the demo
<a href="$PTII/ptolemy/domains/qss/demo/HelloWorld/HelloWorld_Propagate.xml">$PTII/ptolemy/domains/qss/demo/HelloWorld/HelloWorld_Propagate.xml</a>.
<p>
The frequency with which the output <i>q</i> of this integrator
is produced depends on the <i>solver</i> choice and the 
<i>absoluteQuantum</i> and <i>relativeQuantum</i> parameter values.
These determine when a quantization event occurs, as explained above.
The <i>quantum</i> is equal to the larger of <i>absoluteQuantum</i>
and the product of <i>relativeQuantum</i> and the current state value.
The simplest case is
where the solver is QSS1 and <i>relativeQuantum</i> is zero. In this case, a
quantization event occurs whenever the integral of the input signal changes by
the <i>absoluateQuantum</i>. For QSS1, the input is assumed to be piecewise
constant. If the input is a SmoothToken, the derivatives of the input
are ignored.
<p>
On the first firing at initialization
time, the output value is given by <i>xInit</i>. That initial value can be a
SmoothToken (expressed as smoothToken(value, {array of derivatives}).
<p>
When an <i>impulse</i> input is received, the value of that event
is added to the current state of this integrator (any derivatives provided
on the <i>impulse</i> input are ignored).
Then an output event is produced and the integrator is reinitialized so
that the next output quantum is relative to the new state value.
<p>
Note that in most cases, this actor outputs a SmoothToken.
(The only exception is at initialization, where <i>xInit</i> is produced;
it may not be a SmoothToken.)
A SmoothToken has that property that any downstream
actor can read the signal at any time, and the value will
be extrapolated to the time of the read automatically, regardless of whether
the source of the SmoothToken has produced an output at that time.
Thus, the outputs of this integrator only need to occur
explicitly when something interesting has changed that would make such prediction invalid.
Even though the signal contains only infrequent events, a downstream actor can read
the values frequently, for example to generate more representative plots of
the signal.
If want downstream actors to see only the actual events produced by this
integrator, then you can feed the output into an instance of {@link SmoothToDouble}.
<p>
FIXME: To do:
- Make xInit a PortParameter.
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
        new SingletonParameter(u, "_showName").setToken(BooleanToken.TRUE);

        q = new TypedIOPort(this, "q", false, true);
        q.setTypeEquals(BaseType.DOUBLE);

        // FIXME: Make this a PortParameter
        xInit = new Parameter(this, "xInit");
        xInit.setTypeEquals(BaseType.DOUBLE);
        xInit.setExpression("0.0");

	solver = new StringParameter(this, "solver");
	QSSDirector.configureSolverParameter(solver, "");

        absoluteQuantum = new Parameter(this, "absoluteQuantum");
        absoluteQuantum.setTypeEquals(BaseType.DOUBLE);
        
        relativeQuantum = new Parameter(this, "relativeQuantum");
        relativeQuantum.setTypeEquals(BaseType.DOUBLE);
        
        impulse = new TypedIOPort(this, "impulse", true, false);
        impulse.setTypeEquals(BaseType.DOUBLE);
        StringAttribute cardinality = new StringAttribute(impulse, "_cardinal");
        cardinality.setExpression("SOUTH");
        new SingletonParameter(impulse, "_showName").setToken(BooleanToken.TRUE);
        
        propagateInputDerivatives = new Parameter(this, "propagateInputDerivatives");
        propagateInputDerivatives.setTypeEquals(BaseType.BOOLEAN);
        propagateInputDerivatives.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If specified, the minimum quantum for this integrator.
     *  This is a double, and by default is not given, which means
     *  that the quantum is specified by the director.
     */
    public Parameter absoluteQuantum;

    /** The impulse input port. This is a single port of type double.
     *  If any derivatives are provided on this port via a SmoothToken,
     *  they are ignored.
     */
    public TypedIOPort impulse;

    /** Output (the quantized state). */
    public TypedIOPort q;
    
    /** If true (the default), then derivative information from the input will be
     *  produced on the outputs, and an output will be produced whenever
     *  an input is received. If false, then no derivative information is provided
     *  on the output (the output is assumed to be piecewise constant), and outputs
     *  are produced only when the integral has changed enough to
     *  trigger a quantization event.
     */
    public Parameter propagateInputDerivatives;
    
    /** If specified, the relative quantum for this integrator.
     *  If the value here is greater than zero, then the quantum
     *  that this integrator uses will be the larger of the
     *  {@link #absoluteQuantum} and |x| * relativeQuantum,
     *  where x is the current value of the state.
     *  This is a double that defaults to be empty (nothing
     *  specified), which causes the relativeQuantum to be
     *  retrieved from the director.
     */
    public Parameter relativeQuantum;

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

    /** If <i>propagateInputDerivatives</i> is false, then declare that the output
     *  does not depend on the input <i>u</i> in a firing.
     *  @exception IllegalActionException If propagateInputDerivatives cannot be evaluated.
     *  @see #getCausalityInterface()
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
	if (!isStrict()) {
	    _declareDelayDependency(u, q, 0.0);
	}
    }

    /** Set the derivative equal to the input.
     *  @return Success (0 for success, else user-defined error code).
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

    /** If it is time to produce a quantized output (there is a quantization event), produce it.
     *  Otherwise, indicate that the output is absent. Also produce an output if an <i>impulse</i>
     *  event is received and if this is the first firing at initialization time.
     *  If <i>propagateInputDerivatives</i> is true, then this method will first read inputs
     *  and will produce an output whether there is a quantization event or not.
     *  The input value (and any derivatives it provides) will be provided on the output as
     *  the derivative (and any higher-order derivatives) of the output.
     *  @exception IllegalActionException If sending an output fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Firing at time: " + currentTime);
        }
        // Flag to produce output.
        boolean produceOutput = false;
        
        if (_qssSolver.getCurrentSimulationTime().compareTo(currentTime) < 0) {
            // The current simulation time is ahead of the solver's time.
            // Apparently, quantization events only occur when this is the case.
            try {
                // Catch up by integrating to current time.
        	// If advancing to time triggers a quantization event, then the returned list
        	// will be non-empty.
                List<Integer> events = _qssSolver.advanceToTime(currentTime);
                
                // Flag to produce output.
                produceOutput = !events.isEmpty();
                if (_debugging && produceOutput) {
        	    _debug("State has a quantization event.");
                }
            } catch (Exception ee) {
                throw new IllegalActionException(this, ee, ee.getMessage());
            }
        }
        if (_firstRound) {
            // Quantize the initial state and reinitialize the solver.
            _qssSolver.triggerQuantizationEvents(true);
            produceOutput = true;
            _firstRound = false;
        }

        // Read any impulse input that may be available.
        produceOutput = _handleImpulse() || produceOutput;
        
        // If propagateInputDerivatives is true, then reading an input
        // triggers an output.
        boolean hasInput = false;
        if (isStrict()) {
            // If an input is available, read it.
            hasInput = _handleInput();
            produceOutput = hasInput || produceOutput;
        }
        
        // Produce outputs only if there is a quantization event, an impulse,
        // or an input was read and propagateInputDerivatives is true.
        if (produceOutput) {
            // Note that the output will include derivative information
            // from reading the input above, if input was read. Otherwise,
            // it has derivative information from previous inputs.
            double currentValue = _qssSolver.getStateModel(0).coeffs[0];
            
            if (hasInput) {
        	// If there is an input, then we might be producing an output
        	// at a time other than the time of a quantization event.
        	// We really need to evaluate the continuous state.
        	currentValue = _qssSolver.evaluateStateModelContinuous(0, currentTime);
            }

            // The derivatives of the output are determined by the most recent
            // input, extrapolated to the present, up to the n-th derivative, for QSSn.
            double[] derivatives = null;
            if (_previousInput != null) {
        	SmoothToken extrapolatedInput = _previousInput.extrapolate(currentTime);
        	int order = SmoothToken.order(extrapolatedInput);
                derivatives = new double[order + 1];
                derivatives[0] = extrapolatedInput.doubleValue();
                for (int i = 1; i <= order; i++) {
                    derivatives[i] = SmoothToken.derivativeValue(extrapolatedInput, i);
        	}
            }
            Token token = new SmoothToken(currentValue, currentTime, derivatives);
            q.send(0, token);
            if (_debugging) {
                _debug("Send to output: " + token);
            }
        }
        // To ensure determinacy, read the input after producing the output.
        // Otherwise, the output produced could depend on the order in which
        // actors are created because this actor can be fired at any time.
        if (!isStrict()) {
            _handleInput();
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
        
        _previousInput = null;
        _previousInputTime = null;
        
        // Get director and check its type.
        QSSDirector director = (QSSDirector)getDirector();
        if (!(director instanceof QSSDirector)) {
            throw new IllegalActionException(
                    this,
                    String.format(
                            "Director %s cannot be used for QSS, which requires a QSSDirector.",
                            director.getName()));
        }
        final Time currentTime = director.getModelTime();

        // Create a new QSS solver and initialize it.
        // If no solver is specified, use the director's specification.
        String solverSpec = solver.stringValue().trim();
        if (solverSpec.equals("")) {
            _qssSolver = director.newQSSSolver();
        } else {
            _qssSolver = director.newQSSSolver(solverSpec);
        }
        
        // Find the quanta.
        double absoluteQuantumValue;
        DoubleToken quantum = (DoubleToken)absoluteQuantum.getToken();
        if (quantum == null) {
            // No quantum given for this integrator. Use the director's value.
            absoluteQuantumValue = director.getAbsoluteQuantum();
        } else {
            absoluteQuantumValue = quantum.doubleValue();
        }
        double relativeQuantumValue;
        quantum = (DoubleToken)relativeQuantum.getToken();
        if (quantum == null) {
            // No quantum given for this integrator. Use the director's value.
            relativeQuantumValue = director.getRelativeQuantum();
        } else {
            relativeQuantumValue = quantum.doubleValue();
        }

        // Determine the maximum order of the input variables.
        // This is equal to the maximum order of the state model.
        // Specifically, for QSS1, the state model order is 0, because the QSS1 solver
        // semantically produces a piecewise constant output. No derivatives are
        // provided. (Note that this actor will provide derivative information
        // on the output if {@link #propagateInputDerivatives} is set to true).
        // For QSS1, any derivatives of the input are ignored, so the input
        // model order should be zero as well.
        _maximumInputOrder = _qssSolver.getStateModelOrder();
        
        // Set up the solver to use this actor to specify the number of states (1)
        // and input variables (1), and to use this actor to calculate the derivative
        // of the states.
        _qssSolver.initialize(
        	this, 				// The derivative function implementer.
        	currentTime, 			// The simulation start time.
        	director.getModelStopTime(), 	// The maximum time to an event.
        	absoluteQuantumValue, 		// The absolute quantum.
        	relativeQuantumValue, 		// The relative quantum.
        	_maximumInputOrder);		// The order of the input variables.

        // Initialize the state variable to match the {@link #xInit} parameter.
        DoubleToken xInitValue = (DoubleToken)xInit.getToken();
        _qssSolver.setStateValue(0, xInitValue.doubleValue());
        if (xInitValue instanceof SmoothToken) {
            // The initial value also specifies derivatives.
            // Use these to set an initial input model.
            double[] derivatives = ((SmoothToken)xInitValue).derivativeValues();
            _setInputModel(derivatives, currentTime);
        }
        
        // To make sure this actor fires at the start time, request a firing.
        getDirector().fireAtCurrentTime(this);
        
        _firstRound = true;
        _lastFireAtTime = null;
    }

    /** Return the value of <i>propagateInputDerivatives</i>,
     *  because this actor is strict if that value is true, and is non-strict otherwise
     *  (it does not need to know its input to fire).
     *  @return The value of propagateInputDerivatives.
     *  @throws IllegalActionException  If propagateInputDerivatives cannot be evaluated.
     */
    @Override
    public boolean isStrict() throws IllegalActionException {
        return ((BooleanToken)propagateInputDerivatives.getToken()).booleanValue();
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
        
        // Predict the next quantization event time.
        final Time possibleFireAtTime = _qssSolver.predictQuantizationEventTimeEarliest();
        if (_debugging) {
            _debug("Request refiring at " + possibleFireAtTime);
        }

        // Cancel previous firing time if necessary.
        // FIXME: Following is confusing and probably redundant.
        final boolean possibleDiffersFromLast = (null == _lastFireAtTime || possibleFireAtTime
        	.compareTo(_lastFireAtTime) != 0);
        if (_lastFireAtTime != null // Made request before.
        	&& _lastFireAtTime.compareTo(currentTime) > 0
        	// Last request was not used.
        	// _lastFireAtTime > currentTime
        	&& possibleDiffersFromLast // Last request is no longer valid.
        	) {
            if (_debugging) {
        	_debug("Canceling previous fireAt request at " + _lastFireAtTime);
            }
            ((DEDirector) getDirector()).cancelFireAt(this, _lastFireAtTime);
        }

        // Request firing time if necessary.
        if (possibleDiffersFromLast && !possibleFireAtTime.isPositiveInfinite()) {
            getDirector().fireAt(this, possibleFireAtTime);
            if (_debugging) {
        	_debug("Requesting refiring at " + possibleFireAtTime);
            }
            _lastFireAtTime = possibleFireAtTime;
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If there is an input available on impulse port, handle it.
     *  @return If an input was seen on the impulse port.
     *  @throws IllegalActionException If reading the input fails.
     */
    private boolean _handleImpulse() throws IllegalActionException {
	// Be sure to use hasNewToken() here, or could repeatedly get impulses.
        if (impulse.getWidth() > 0 && impulse.hasNewToken(0)) {
            DoubleToken impulseToken = (DoubleToken) impulse.get(0);
            double impulseValue = impulseToken.doubleValue();
            if (_debugging) {
                _debug("-- impulse input received with value " + impulseValue);
            }
            if (impulseValue != 0.0) {
        	// Need to get the internal state model at the current time.
        	Time currentTime = getDirector().getModelTime();
        	double previousState = _qssSolver.evaluateStateModelContinuous(0, currentTime);
        	
        	// Update the state with the impulse value.
                double currentState = previousState + impulseValue;
                // Note that the following call sets flags indicating that new
                // rate event and quantization events are needed, just like setting the
                // initial value in initialize().
                // NOTE: could try using setStateValueOnly, but that method does not
                // do what it says. Instead, it sets all derivatives to zero.
                // _qssSolver.setStateValueOnly(0, currentState);
                // Hence, we reinitialize, as if xInit were being set.
                _qssSolver.setStateValue(0, currentState);
                // Apparently, the above loses any information about previously provided
                // derivative information. Restore that information here.
                if (_previousInput != null) {
                    _setInputModel(_previousInput, _previousInput.getTime());
                }

                _qssSolver.triggerQuantizationEvents(true);
                try {
		    _qssSolver.triggerRateEvent();
		} catch (Exception e) {
		    throw new IllegalActionException(this, e, e.getMessage());
		}

                if (_debugging) {
                    _debug("-- Due to impulse input, change state from "
                	    + previousState
                	    + " to "
                            + currentState);
                }
                return true;
            }
        }
        return false;
    }
    
    /** If an input is present, read it. If its value and time do not
     *  match that of the most recently seen input, then set the input
     *  variable model of the solver using the provided input data.
     *  @return True if a new unique input is seen.
     *  @throws IllegalActionException If the input cannot be read.
     */
    private boolean _handleInput()
	    throws IllegalActionException {
        // Be sure to use hasNewToken(). Don't want to react to just
        // extrapolated input.
        if (u.hasNewToken(0)) {
            DoubleToken inputToken = (DoubleToken)u.get(0);
            if (_debugging) {
        	_debug("Read input: " + inputToken);
            }
            Time currentTime = getDirector().getModelTime();
            if (currentTime.equals(_previousInputTime) && inputToken.equals(_previousInput)) {
        	// Input is identical to previous input. No new information.
        	if (_debugging) {
        	    _debug("Input is identical to previous input, so ignoring.");
        	}
        	return false;
            }
            _previousInputTime = currentTime;
            double inputValue = inputToken.doubleValue();
            if (inputToken instanceof SmoothToken) {
        	// Discard any derivatives higher than the requisite order.
        	double[] inputDerivatives = ((SmoothToken)inputToken).derivativeValues();
        	if (inputDerivatives == null || inputDerivatives.length <= _maximumInputOrder) {
        	    // Easy case. Input already has the right form.
        	    _previousInput = (SmoothToken)inputToken;
        	} else if (_maximumInputOrder == 0) {
        	    // No derivatives will be used.
        	    if (_debugging) {
        		_debug("##### Warning: Input derivatives will be ignored: " + inputDerivatives);
        	    }
        	    _previousInput = new SmoothToken(inputValue, currentTime, null);
        	} else {
        	    double[] derivatives = new double[_maximumInputOrder];
        	    for (int i = 0; i < _maximumInputOrder; i++) {
        		if (i < inputDerivatives.length) {
        		    derivatives[i] = inputDerivatives[i];
        		}
        	    }
        	    _previousInput = new SmoothToken(inputValue, currentTime, derivatives);
        	}
            } else {
        	_previousInput = new SmoothToken(inputValue, currentTime, null);
            }
            _setInputModel(_previousInput, currentTime);
            
            return true;
        }
        // Return false if there is no new input u and no new impulse input.
	return false;
    }
    
    /** Given a SmoothToken that specifies the derivatives being integrated,
     *  set the "input model."
     *  @param derivatives The input to this integrator, which represents the
     *   derivative of the state and possibly higher-order derivatives.
     *  @param currentTime The current time.
     *  @throws IllegalActionException If the solver throws an exception when
     *   triggering a rate event.
     */
    private void _setInputModel(SmoothToken derivatives, Time currentTime) throws IllegalActionException {
        ModelPolynomial inputModel = _qssSolver.getInputVariableModel(0);
        inputModel.coeffs[0] = derivatives.doubleValue();
        if (_maximumInputOrder > 0) {
            // Using QSS2 or QSS3, so derivatives of the input can be used.
            // Get derivative information from the input, if present.
            // Note that, confusingly, the input value is a derivative, but the derivative
            // signal may also have derivatives.
            double[] derivativesOfInput = derivatives.derivativeValues();
            int factorial = 1;
            for (int i = 1; i <= _maximumInputOrder; i++) {
        	if (derivativesOfInput == null || derivativesOfInput.length < i) {
        	    // Derivative not provided. Set it to zero.
        	    inputModel.coeffs[i] = 0.0;
        	} else {
        	    inputModel.coeffs[i] = derivativesOfInput[i-1]/factorial;
        	    factorial = factorial * i;
        	}
            }
        }
        inputModel.tMdl = currentTime;
        
        // Since there is a new input distinct from the previous, we
        // have a new rate. Trigger a rate event.
        try {
            _qssSolver.triggerRateEvent();
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }

    /** Given an array of derivatives that specifies an input value and its
     *  derivatives, set the "input model."
     *  @param derivatives The input to this integrator, which represents the
     *   derivative of the state and possibly higher-order derivatives.
     *  @param currentTime The current time.
     *  @throws IllegalActionException If the solver throws an exception when
     *   triggering a rate event.
     */
    private void _setInputModel(double[] derivatives, Time currentTime) throws IllegalActionException {
	// FIXME: This is duplicated from the previous method, almost.
	// Fix SmoothToken so such inefficiency is not needed by changing it to
	// a double[] internally for the value and its derivatives, despite
	// the redundancy with the field storing the value in DoubleToken.
        ModelPolynomial inputModel = _qssSolver.getInputVariableModel(0);
        inputModel.coeffs[0] = derivatives[0];
        if (_maximumInputOrder > 0) {
            // Using QSS2 or QSS3, so derivatives of the input can be used.
            int factorial = 1;
            for (int i = 1; i <= _maximumInputOrder; i++) {
        	if (derivatives.length <= i) {
        	    // Derivative not provided. Set it to zero.
        	    inputModel.coeffs[i] = 0.0;
        	} else {
        	    inputModel.coeffs[i] = derivatives[i]/factorial;
        	    factorial = factorial * i;
        	}
            }
        }
        inputModel.tMdl = currentTime;
        
        // Since there is a new input distinct from the previous, we
        // have a new rate. Trigger a rate event.
        try {
            _qssSolver.triggerRateEvent();
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag identifying the first round of iterations of fire() and postfire()
     *  after initialize().
     */
    private boolean _firstRound = true;

    /** Track requests for firing. */
    private Time _lastFireAtTime;
    
    /** Previous input token, if any. */
    private SmoothToken _previousInput;
    
    /** Time of previous input token, if any. */
    private Time _previousInputTime;

    /** The QSS solver for this actor.
     *  It is an instance of the class given by the
     *  <i>QSSSolver</i> parameter of the director.
     */
    private QSSBase _qssSolver = null;
    
    /** Maximum input order. */
    private Integer _maximumInputOrder;
}
