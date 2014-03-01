package org.ptolemy.qss;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
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
@version $Id: When.java 68298 2014-02-05 17:00:30Z eal $
@since Ptolemy II 10.1
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
*/
public class QSSIntegrator extends TypedAtomicActor {

	/** Construct a new instance of this integrator.
	 *  @param container The container.
	 *  @param name The name.
	 *  @throws IllegalActionException If setting up ports and parameters fails.
	 *  @throws NameDuplicationException If the container already constains an object with this name.
	 */
	public QSSIntegrator(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		u = new TypedIOPort(this, "u", true, false);
		u.setTypeEquals(BaseType.DOUBLE);;
		q = new TypedIOPort(this, "q", false, true);
		q.setTypeEquals(BaseType.DOUBLE);;
		
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

	public Parameter xInit, quantum;
	
    /////////////////////////////////////////////////////////////////////
    ////                       public methods                        ////

    /** Declare that the output does not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(u, q, 0.0);
    }

    /** If it is time to produce a quantized output, produce it.
     *  Otherwise, indicate that the output is absent.
     *  @throws IllegalActionException If sending an output fails.
     */
	public void fire() throws IllegalActionException {
		super.fire();
		Time currentTime = getDirector().getModelTime();
		if (currentTime.equals(nextOutputTime) || nextOutputTime == null) {
			// It is time to send an output.
			q.send(0, new DoubleToken(nextOutputValue));
		} else {
			// For the continuous director, assert that the output is absent.
			q.sendClear(0);
		}
	}
	
	/** Initialize this actor to indicate that no input has yet been provided.
	 */
	public void initialize() throws IllegalActionException {
		super.initialize();
		nextOutputTime = previousStateUpdateTime = null;
		x = ((DoubleToken)xInit.getToken()).doubleValue();
		nextOutputValue = _quantize(((DoubleToken)xInit.getToken()).doubleValue());
		previousOutputValue = nextOutputValue;
		previousInput = null;
		// To make sure this actor fires at the start time, request a firing.
		getDirector().fireAtCurrentTime(this);
	}
	
	/** Return false, indicating that this actor can fire even if its
	 *  input is unknown.
	 *  @return False.
	 */
	public boolean isStrict() {
		return false;
	}
	
	/** Update the calculation of the next output time and request
	 *  a refiring at that time.
	 *  If there is a new input, read it and update the slope.
	 *  @return True if the base class returns true.
	 *  @throws IllegalActionException If reading inputs or parameters fails.
	 */
	public boolean postfire() throws IllegalActionException {
		Time currentTime = getDirector().getModelTime();
		double quantumValue = ((DoubleToken)quantum.getToken()).doubleValue();
		boolean inputReceived = false;

		// If an input is provided, we should override the nextOutputTime
		// and value calculated above, or if none was calculated above,
		// then modify the values set when the previous input arrived.
		if (u.hasToken(0)) {
			Token newInput = u.get(0);
			if (!newInput.equals(previousInput)) {
				// We have a new input value, different from the previous input.
				previousInput = newInput;
				inputReceived = true;
				// Update the slope.
				slope = _derivative(((DoubleToken)previousInput).doubleValue());
			}
		}
		if (currentTime.equals(nextOutputTime) || nextOutputTime == null) {
			// The fire method sent an output.
			// Update the state to match that output value.
		    if (nextOutputTime != null) {
		        x = nextOutputValue;
		        }
			previousOutputValue = nextOutputValue;

			// Calculate the time of the next output, which is the time
			// it will take at the current slope to rise or fall by the quantum.
			// The following will make sure that output
			// does not get sent again at the current time if there is
			// another firing. Note that DE needs this because
			// of the way it handles feedback loops. It may invoke fire and
			// postfire more than once in each iteration.
			nextOutputTime = _nextCrossingTime(slope, 0.0, 0.0, quantumValue, currentTime);
		} else {
			// The fire method did not send an output.
			// If we did not receive a new input, there is nothing to do.
			// But if we did, then we have a new slope, so we need to recompute
			// the time it will take to get to the next quantum.
			if (inputReceived) {
				// Update the current state.
				x += slope * (currentTime.subtract(previousStateUpdateTime)).getDoubleValue();
				// Update the time of the next output, which is the time it will take to
				// get from the current state to previous output value plus or minus the quantum
				// at the updated slope.
				nextOutputTime = _nextCrossingTime(slope, x, previousOutputValue, quantumValue, currentTime);
				// Reset the inputReceived flag
				inputReceived = false;
			}
		}
		// Calculate the next output value
		nextOutputValue = _nextOutputValue(slope, previousOutputValue, quantumValue);

		// Record time at which state is updated.
		previousStateUpdateTime = currentTime;

		// Request a refiring, unless the slope is small.
		if (nextOutputTime!=Time.POSITIVE_INFINITY){
		    getDirector().fireAt(this, nextOutputTime);  
		}
		return super.postfire();
	}
	
    /////////////////////////////////////////////////////////////////////
    ////                    protected methods                        ////
	
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
	protected Time _nextCrossingTime(
			double slope, double start, double reference, double quantum, Time currentTime) {
		if (slope > _SMALL) {
			// Slope is positive.
			double threshold = reference + quantum;
			if (start >= threshold) {
				return currentTime;
			} else {
				return currentTime.add((threshold - start)/slope);
			}
		} else if (slope < -_SMALL) {
			// Slope is negative.
			double threshold = reference - quantum;
			if (start <= threshold) {
				return currentTime;
			} else {
				return currentTime.add((threshold - start)/slope);
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
	protected double _nextOutputValue(double slope, double reference, double quantum) {
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
	 *  @throws IllegalActionException If the quantum parameter cannot
	 *   be evaluated.
	 */
	protected double _quantize(double x) throws IllegalActionException {
		double quantumValue = ((DoubleToken)quantum.getToken()).doubleValue();
		return (Math.floor(x/quantumValue))*quantumValue;
	}
	
	/////////////////////////////////////////////////////////////////////
	////                  protected variables                        ////

	// FIXME: Is "SMALL" adequate to check whether slope is zero?
	/** A small number, below which the slope is considered to be zero. */
	protected static final double _SMALL = 10E-9; 

	/////////////////////////////////////////////////////////////////////
	////                    private variables                        ////

	/** The time at which to produce the next output. */
	private Time nextOutputTime;
	
	/** The next output value to produce. */
	private double nextOutputValue;

	/** The most recently seen input. */
	private Token previousInput;

	/** The last output produced. */
	private double previousOutputValue;

	/** The time at which the state was last updated. */
	private Time previousStateUpdateTime;
	
	/** The slope calculated at the time of the most recent input. */
	private double slope;

	/** The current state. */
	private double x;
}
