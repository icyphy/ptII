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
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
public class QSSIntegrator extends TypedAtomicActor {

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
        ;
        q = new TypedIOPort(this, "q", false, true);
        q.setTypeEquals(BaseType.DOUBLE);
        ;

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

    /** If it is time to produce a quantized output, produce it.
     *  Otherwise, indicate that the output is absent.
     *  @exception IllegalActionException If sending an output fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("Current time is: " + getDirector().getModelTime());
        }
        Time currentTime = getDirector().getModelTime();
        if (currentTime.equals(_nextOutputTime) || _nextOutputTime == null) {
            // It is time to send an output.
            q.send(0, new DoubleToken(_nextOutputValue));
            if (_debugging) {
                _debug("Send to output: " + _nextOutputValue);
            }
        } else {
            // For the continuous director, assert that the output is absent.
            q.sendClear(0);
            if (_debugging) {
                _debug("Output is absent.");
            }
        }
    }

    /** Initialize this actor to indicate that no input has yet been provided.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextOutputTime = _previousStateUpdateTime = null;
        _nextOutputValue = _quantize(((DoubleToken) xInit.getToken())
                .doubleValue());
        _previousInput = null;
        _previousOutputValue = _nextOutputValue;
        _previousStateUpdateTime = null;
        _currentSlope = 0.0;
        _previousSlope = 0.0;
        _x = ((DoubleToken) xInit.getToken()).doubleValue();
        _firstFiring = true;
        // To make sure this actor fires at the start time, request a firing.
        getDirector().fireAtCurrentTime(this);
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
        Time currentTime = getDirector().getModelTime();
        double quantumValue = ((DoubleToken) quantum.getToken()).doubleValue();
        boolean inputReceived = false;

        // If an input is provided, we should override the nextOutputTime
        // and value calculated above, or if none was calculated above,
        // then modify the values set when the previous input arrived.
        if (u.hasToken(0)) {
            Token newInput = u.get(0);
            if (_debugging) {
                _debug("Received input: " + newInput);
            }
            if (!newInput.equals(_previousInput)) {
                // Initialize the previousInput
                if (_firstFiring) {
                    _previousInput = newInput;
                    _firstFiring = false;
                }
                // Save the previous slope.
                _previousSlope = _derivative(((DoubleToken) _previousInput)
                        .doubleValue());
                // Compute the new slope.
                _currentSlope = _derivative(((DoubleToken) newInput).doubleValue());
                // Save the previous input
                _previousInput = newInput;
                // Set the received flag to true
                inputReceived = true;
            }
        }
        if (currentTime.equals(_nextOutputTime) || _nextOutputTime == null) {
            // The fire method sent an output.
            // Update the state to match that output value.
            if (_nextOutputTime != null) {
                _x = _nextOutputValue;
            }
            _previousOutputValue = _nextOutputValue;

            // Calculate the time of the next output, which is the time
            // it will take at the current slope to rise or fall by the quantum.
            // The following will make sure that output
            // does not get sent again at the current time if there is
            // another firing. Note that DE needs this because
            // of the way it handles feedback loops. It may invoke fire and
            // postfire more than once in each iteration.
            _nextOutputTime = _nextCrossingTime(_currentSlope, 0.0, 0.0, quantumValue,
                    currentTime);
            // Calculate the next output value
            _nextOutputValue = _nextOutputValue(_currentSlope, _previousOutputValue,
                    quantumValue);
        } else {
            // The fire method did not send an output.
            // If we did not receive a new input, there is nothing to do.
            // But if we did, then we have a new slope, so we need to recompute
            // the time it will take to get to the next quantum.
            if (inputReceived) {
                // Update the current state.
                _x += _previousSlope
                        * (currentTime.subtract(_previousStateUpdateTime))
                        .getDoubleValue();
                // Update the time of the next output, which is the time it will take to
                // get from the current state to previous output value plus or minus the quantum
                // at the updated slope.
                _nextOutputTime = _nextCrossingTime(_currentSlope, _x,
                        _previousOutputValue, quantumValue, currentTime);
                // Calculate the next output value
                _nextOutputValue = _nextOutputValue(_currentSlope,
                        _previousOutputValue, quantumValue);
            }
            else{
            // Calculate the next output value
            _nextOutputValue = _nextOutputValue(_previousSlope,
                    _previousOutputValue, quantumValue);
            }
        }

        // Record time at which state is updated.
        _previousStateUpdateTime = currentTime;

        // Request a refiring, unless the slope is small.
        if (_nextOutputTime != Time.POSITIVE_INFINITY) {
            getDirector().fireAt(this, _nextOutputTime);
            if (_debugging) {
                _debug("Requesting a refiring at: " + _nextOutputTime);
            }
        } else if (_debugging) {
                _debug("Next output time is infinite, so not calling fireAt().");
        }
        return super.postfire();
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

    /////////////////////////////////////////////////////////////////////
    ////                  protected variables                        ////

    // FIXME: Is "SMALL" adequate to check whether slope is zero?
    /** A small number, below which the slope is considered to be zero. */
    protected static final double _SMALL = 10E-9;

    /////////////////////////////////////////////////////////////////////
    ////                    private variables                        ////

    /** The time at which to produce the next output. */
    private Time _nextOutputTime;

    /** The next output value to produce. */
    private double _nextOutputValue;

    /** The most recently seen input. */
    private Token _previousInput;

    /** The last output produced. */
    private double _previousOutputValue;

    /** The time at which the state was last updated. */
    private Time _previousStateUpdateTime;

    /** The slope calculated at the time of the most recent input. */
    private double _currentSlope;

    /** The slope calculated at the time of the last recent input. */
    private double _previousSlope;

    /** The current state. */
    private double _x;

    /** The flag to indicate first firing. */
    private boolean _firstFiring;
}
