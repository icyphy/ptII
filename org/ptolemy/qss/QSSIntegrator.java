package org.ptolemy.qss;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.BooleanSwitch;
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

@author David Broman, Edward A. Lee, Thierry Nouidui Michael Wetter
@version $Id: When.java 68298 2014-02-05 17:00:30Z eal $
@since Ptolemy II 10.1
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
*/
public class QSSIntegrator extends TypedAtomicActor {

	public QSSIntegrator(CompositeEntity container, String name)
			throws IllegalActionException, NameDuplicationException {
		super(container, name);
		u = new TypedIOPort(this, "u", true, false);
		u.setTypeEquals(BaseType.DOUBLE);;
		q = new TypedIOPort(this, "q", false, true);
		q.setTypeEquals(BaseType.DOUBLE);;
		
		qInit = new Parameter(this, "qInit");
		qInit.setTypeEquals(BaseType.DOUBLE);
		qInit.setExpression("0.0");
		
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

	public Parameter qInit, quantum;
	
    /** Declare that the output does not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void declareDelayDependency() throws IllegalActionException {
        _declareDelayDependency(u, q, 0.0);
    }

	public void fire() throws IllegalActionException {
		super.fire();
		Time currentTime = getDirector().getModelTime();
		if (currentTime.equals(nextOutputTime) || nextOutputTime == null) {
			q.send(0, new DoubleToken(nextOutputValue));
			previousOutputValue = nextOutputValue;
		} else {
			// For the continuous director, assert that the output is absent.
			q.sendClear(0);
		}
	}
	public void initialize() throws IllegalActionException {
		super.initialize();
		nextOutputTime = previousStateUpdateTime = null;
		// FIXME: Quantizing the initial value of the state.
		// Is this the right thing to do?
		state = _quantize(((DoubleToken)qInit.getToken()).doubleValue());
		nextOutputValue = state;
		previousOutputValue = state;
		uPrevious = null;
		
		// To make sure this actor fires at the start time, request a firing.
		getDirector().fireAtCurrentTime(this);
	}
	public boolean isStrict() {
		return false;
	}
	
	public boolean postfire() throws IllegalActionException {
		Time currentTime = getDirector().getModelTime();
		double quantumValue = ((DoubleToken)quantum.getToken()).doubleValue();
		if (u.hasToken(0)) {
			Token newInput = u.get(0);
			if (!newInput.equals(uPrevious)) {
				// We have a new input value.
				double newInputValue = ((DoubleToken)newInput).doubleValue();
				uPrevious = newInput;
				double slope = _derivative(newInputValue);
				// Is the current time a time at which we produce an output?
				if (currentTime.equals(nextOutputTime) || nextOutputTime == null) {
					state = nextOutputValue;
					previousStateUpdateTime = currentTime;
					double timeIncrement = quantumValue/Math.abs(slope);
					// FIXME: Need a more rational approach here. Is divide by zero OK?
					if (timeIncrement < 10E10) {
						nextOutputTime = currentTime.add(timeIncrement);
						if (slope > 0.0) {
							nextOutputValue += quantumValue;
						} else {
							// Note: If slope == 0.0, time increment is infinite, so this
							// value will never be used.
							nextOutputValue -= quantumValue;
						}
						getDirector().fireAt(this, nextOutputTime);
					} else {
						nextOutputTime = Time.POSITIVE_INFINITY;
					}
				} else {
					// We have a new input, but it's not a time to produce a new output.
					// Update the current state.
					state += previousSlope * (currentTime.subtract(previousStateUpdateTime)).getDoubleValue();
					previousStateUpdateTime = currentTime;
					if (slope > 0.0) {
						nextOutputValue = previousOutputValue + quantumValue;
					} else {
						nextOutputValue = previousOutputValue - quantumValue;
					}
					double timeIncrement = (nextOutputValue - state)/slope;
					nextOutputTime = currentTime.add(timeIncrement);
					getDirector().fireAt(this, nextOutputTime);
				}
				previousSlope = slope;
			}
		} else if (currentTime.equals(nextOutputTime)) {
			// No input is provided, but we did produce an output.
			state = nextOutputValue;
			// Calculate the time to the next output.
			double slope = _derivative(((DoubleToken)uPrevious).doubleValue());
			double timeIncrement = quantumValue/Math.abs(slope);
			// FIXME: Need a more rational approach here.
			if (timeIncrement < 10E10) {
				nextOutputTime = currentTime.add(timeIncrement);
				if (slope > 0.0) {
					nextOutputValue += quantumValue;
				} else {
					// Note: If slope == 0.0, time increment is infinite, so this
					// value will never be used.
					nextOutputValue -= quantumValue;
				}
				getDirector().fireAt(this, nextOutputTime);
			} else {
				nextOutputTime = Time.POSITIVE_INFINITY;
			}
		} else {
			if (nextOutputTime == null) {
				// First firing, and no input is now provided.
				throw new IllegalActionException(this, "No input provided");
			}
		}
		return super.postfire();
	}
	
	protected double _derivative(double newInputValue) {
		// FIXME: Invoke FMU to get derivatives.
		return newInputValue;
	}
	
	protected double _quantize(double x) throws IllegalActionException {
		double quantumValue = ((DoubleToken)quantum.getToken()).doubleValue();
		return (Math.floor(x/quantumValue))*quantumValue;
	}
	private Time nextOutputTime, previousStateUpdateTime;
	private double nextOutputValue, previousOutputValue;
	private Token uPrevious;
	private double state, previousSlope;
}
