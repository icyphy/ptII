/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
package ptolemy.domains.qss.kernel;

import org.ptolemy.qss.solver.QSSBase;
import org.ptolemy.qss.util.DerivativeFunction;
import org.ptolemy.qss.util.ModelPolynomial;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// QSSIntegrator

/**
A quantized-state integrator.

@author David Broman, Edward A. Lee, Thierry Nouidui, Michael Wetter
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

        quantum = new Parameter(this, "quantum");
        quantum.setTypeEquals(BaseType.DOUBLE);
        quantum.setExpression("0.01");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input (the derivative). */
    public TypedIOPort u;

    /** Output (the quantized state). */
    public TypedIOPort q;

    /** Initial value of the state. */
    public Parameter xInit;

    /** Quantum. */
    public Parameter quantum;

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
        
        // Assume do not need a quantization-event.
        assert (_qssSolver.needQuantizationEventIndex() == -1);

        // Step time if it will advance the integrator.
        if (_qssSolver.getCurrentSimulationTime().compareTo(currentTime) == -1) {
            try {
                _qssSolver.stepToTime(currentTime);
            } catch (Exception ee) {
                throw new IllegalActionException(this, ee, ee.getMessage());
            }
            // Requantize if necessary.
            if (_qssSolver.needQuantizationEventIndex() >= 0) {
                _qssSolver.triggerQuantizationEvent(0);
                if (q.getWidth() > 0) {
                    double[] model = _qssSolver.getStateModel(0).coeffs;
                    // FIXME: Seems like if the input is available,
                    // then we could read it now and send out a QSSToken with
                    // the value and its derivative. How to do that?
                    q.send(0, new QSSToken(model[0]));
                }
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
        _qssSolver = _director.newQSSSolver();
        _qssSolver.initializeDerivativeFunction(this);
        _qssSolver.initializeSimulationTime(currentTime);
        _qssSolver.setQuantizationEventTimeMaximum(_director.getModelStopTime());

        // Initialize the state in the solver.
        double xInitValue = ((DoubleToken)xInit.getToken()).doubleValue();
        _qssSolver.setStateValue(0, xInitValue);

        // Set the error tolerance.
        // FIXME: The director is specifying the tolerance here, but this actor should use
        // that specification only as a default.
        double tolerance = _director.getErrorTolerance();
        // FIXME: Should the relative tolerance here be different from the absolute tolerance?
        _qssSolver.setQuantizationTolerance(0, tolerance, tolerance);
        
        // To make sure this actor fires at the start time, request a firing.
        getDirector().fireAtCurrentTime(this);
        
        _firstRound = true;
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
            inputValue = ((DoubleToken)u.get(0)).doubleValue();
            newInput = true;
        }

        if (_firstRound) {
            _inputModel = new ModelPolynomial(0);
            // FIXME: What does the following mean?? The docs say nothing.
            _inputModel.claimWriteAccess();
            // Give model to the integrator.
            _qssSolver.addInputVariableModel(0, _inputModel);
            _inputModel.coeffs[0] = inputValue;
            _inputModel.tMdl = currentTime;

            // Validate the integrator. FIXME: What does that mean?
            final String failMsg = _qssSolver.validate();
            if (null != failMsg) {
                throw new IllegalActionException(this, failMsg);
            }

            _qssSolver.triggerQuantizationEvent(0);

            try {
                _qssSolver.triggerRateEvent();
            } catch (Exception ee) {
                // Rethrow as an IllegalActionException.
                throw new IllegalActionException(this, ee, "Triggering rate event failed.");
            }

            _firstRound = false;
        } else if (newInput) {
            // New input value provided.
            // FIXME: If the input is a QSSToken, set the higher order fields of the input model.
            _inputModel.coeffs[0] = inputValue;
            _inputModel.tMdl = currentTime;
            
            try {
                _qssSolver.triggerRateEvent();
            } catch (Exception ee) {
                throw new IllegalActionException(this, ee, "Triggering rate event failed.");
            }
        }
        // Find the next firing time, assuming nothing else in simulation changes.
        final Time possibleFireAtTime = _qssSolver.predictQuantizationEventTimeEarliest();
        if (_debugging) {
            _debug("Request refiring at " + possibleFireAtTime);
        }

        // Cancel previous firing time if necessary.
        // FIXME: Following is confusing and probably redundant.
        final boolean possibleDiffersFromLast = (null == _lastFireAtTime || possibleFireAtTime
                .compareTo(_lastFireAtTime) != 0);
        if (null != _lastFireAtTime // Made request before.
                && _lastFireAtTime.compareTo(currentTime) == 1 // Last request was
                                                            // not used.
                                                            // _lastFireAtTime >
                                                            // currentTime
                && possibleDiffersFromLast // Last request is no longer valid.
        ) {
            if (_debugging) {
                _debug("Cancelling previous fireAt request at " + _lastFireAtTime);
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
    ////                         protected methods                 ////

    /** Return the derivative (slope) at the current time with the given
     *  input value. In this base class, the input is assumed to be the
     *  derivative, so this method just returns its argument. Derived
     *  classes may provide some other function to provide a derivative.
     *  @param input The input value.
     *  @return The current derivative.
     */
    protected double _derivative(double input) {
        // NOTE: Invoke FMU to get derivatives in a derived class.
        return input;
    }

    /** Return the next time at which a line with the given slope
     *  will rise or fall from the specified starting point to the
     *  specified reference plus or minus the specified quantum.
     *  If the specified slope is smaller than the value of
     *  {@link #_SMALL}, then return Time.POSITIVE_INFINITY.
     *  If the starting point has already hit or crossed
     *  the specified reference plus or minus the specified
     *  quantum, then return the current time.
     *  @param slope The derivative.
     *  @param start The starting point.
     *  @param reference The reference point.
     *  @param quantum The quantum.
     *  @param currentTime The current time.
     *  @return The next event time.
     */
    protected Time _nextCrossingTime(double slope, double start,
            double reference, double quantum, Time currentTime) {
        if (slope > _SMALL) {
            // Slope is positive.
            double threshold = reference + quantum;
            if (start >= threshold) {
                return currentTime;
            } else {
                return currentTime.add((threshold - start) / slope);
            }
        } else if (slope < -_SMALL) {
            // Slope is negative.
            double threshold = reference - quantum;
            if (start <= threshold) {
                return currentTime;
            } else {
                return currentTime.add((threshold - start) / slope);
            }
        } else {
            // Slope is small.
            return Time.POSITIVE_INFINITY;
        }
    }

    /** Return the next output value, which is the reference plus the quantum
     *  if the slope is positive, and the reference minus the quantum otherwise.
     *  @param slope The slope.
     *  @param reference The reference.
     *  @param quantum The quantum.
     *  @return The reference plus or minus the quantum.
     */
    protected double _nextOutputValue(double slope, double reference,
            double quantum) {
        if (slope > 0.0) {
            return reference + quantum;
        } else {
            return reference - quantum;
        }
    }

    /** Return the argument quantized to a multiple of quantum given by
     *  the {@link #quantum} parameter.
     *  @param x The value to quantize.
     *  @return A quantized value.
     *  @exception IllegalActionException If the quantum parameter cannot
     *   be evaluated.
     */
    protected double _quantize(double x) throws IllegalActionException {
        double quantumValue = ((DoubleToken) quantum.getToken()).doubleValue();
        return (Math.floor(x / quantumValue)) * quantumValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // FIXME: Is "SMALL" adequate to check whether slope is zero?
    /** A small number, below which the slope is considered to be zero. */
    protected static final double _SMALL = 10E-9;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** QSS director. */
    private QSSDirector _director;

    /** Flag identifying the first round of iterations of fire() and postfire()
     *  after initialize().
     */
    private boolean _firstRound = true;

    /** Input variable model. */
    ModelPolynomial _inputModel;
    
    /** Track requests for firing. */
    private Time _lastFireAtTime;

    /** The QSS solver for this actor.
     *  It is an instance of the class given by the
     *  <i>QSSSolver</i> parameter of the director.
     */
    private QSSBase _qssSolver = null;
}
