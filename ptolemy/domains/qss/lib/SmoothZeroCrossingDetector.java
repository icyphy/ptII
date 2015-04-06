/* A quantized-state zero-crossing detector.
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
package ptolemy.domains.qss.lib;

import org.ptolemy.qss.util.PolynomialRoot;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.qss.kernel.QSSDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SmoothZeroCrossingDetector

/**
A zero-crossing detector designed for use with quantized-state systems.
The input is of type double, and it is assumed to represent a piecewise
smooth signal. When presented with an input, if the has either crossed or
hit zero since the last seen input, then an output will be produced.
If derivatives are available on the input, then they are
used to predict the time of the next zero crossing (or touching),
and this actor will request a refiring at that time. If it refires
at that time, and no other input has arrived in the intervening interval,
then it will produce an output in that firing.
<p>
<b>NOTE:</b> This actor currently discards all derivatives of the input
higher than the second derivative. Hence, it could miss a zero crossing
by a substantial margin if there are higher-order derivatives.

@see QSSDirector
@author Edward A. Lee
@version $Id$
@since Ptolemy II 11.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
public class SmoothZeroCrossingDetector extends TypedAtomicActor {

    /** Construct a new instance.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If setting up ports and parameters fails.
     *  @exception NameDuplicationException If the container already contains an object with this name.
     */
    public SmoothZeroCrossingDetector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
        
        level = new Parameter(this, "level", new DoubleToken(0.0));
        level.setTypeEquals(BaseType.DOUBLE);

        value = new Parameter(this, "value");
        value.setExpression("level");

        // By default, this director detects both directions of level crossings.
        direction = new StringParameter(this, "direction");
        direction.setExpression("both");
        _detectRisingCrossing = true;
        _detectFallingCrossing = true;

        direction.addChoice("both");
        direction.addChoice("falling");
        direction.addChoice("rising");

        output.setTypeAtLeast(value);
        
        _errorTolerance = 1e-4;
        errorTolerance = new Parameter(this, "errorTolerance", new DoubleToken(
                _errorTolerance));
        errorTolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A parameter that can be used to limit the detected level crossings
     *  to rising or falling. There are three choices: "falling", "rising", and
     *  "both". The default value is "both".
     */
    public StringParameter direction;
    
    /** The error tolerance specifying how close the time needs to be to
     *  the zero crossing to produce the output event.
     *  This is a double with default 1e-4.
     */
    public Parameter errorTolerance;

    /** Input signal. This has type double and is normally a SmoothToken. */
    public TypedIOPort input;

    /** The parameter that specifies the level threshold. By default, it
     *  contains a double with value 0.0. Note, a change of this
     *  parameter at run time will not be applied until the next
     *  iteration.
     */
    public Parameter level;

    /** Output event with value 0.0 when the zero crossing occurs. */
    public TypedIOPort output;
    
    /** The output value to produce when a level-crossing is detected.
     *  This can be any data type. It defaults to the same value
     *  as the <i>level</i> parameter.
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the attribute if it has been changed. If the attribute
     *  is <i>direction</i> or <i>level</i>, then update the local cache.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the attribute change failed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == errorTolerance) {
            double tolerance = ((DoubleToken) errorTolerance.getToken())
                    .doubleValue();

            if (tolerance <= 0.0) {
                throw new IllegalActionException(this,
                        "Error tolerance must be greater than 0.");
            }

            _errorTolerance = tolerance;
        } else if (attribute == direction) {
            String crossingDirections = direction.stringValue();

            if (crossingDirections.equalsIgnoreCase("falling")) {
                _detectFallingCrossing = true;
                _detectRisingCrossing = false;
            } else if (crossingDirections.equalsIgnoreCase("rising")) {
                _detectFallingCrossing = false;
                _detectRisingCrossing = true;
            } else if (crossingDirections.equalsIgnoreCase("both")) {
                _detectFallingCrossing = true;
                _detectRisingCrossing = true;
            } else {
                throw new IllegalActionException("Unknown direction: "
                        + crossingDirections);
            }
        } else if (attribute == level) {
            _level = ((DoubleToken) level.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** If an input is available and either it equals zero or it has crossed
     *  zero from the most recently seen input, then output 0.0; otherwise, if
     *  an input available and that input is a {@link SmoothToken} with non-zero
     *  derivatives, then predict the time at which a zero crossing will occur
     *  and request a refiring at that time; Otherwise, if no input is available
     *  and current time matches the time of a previous refiring request, then
     *  produce the output 0.0.
     *  @exception IllegalActionException If sending an output fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        
        // Current time.
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Firing at time: " + currentTime);
        }
        
        // Current input.
        DoubleToken inputToken = null;
        if (input.hasNewToken(0)) {
            inputToken = (DoubleToken)input.get(0);
            if (_debugging) {
        	_debug("Read input: " + inputToken);
            }
        }
        
        // If there is no input, then this firing must be occurring
        // in reaction to a projection for a crossing.
        if (inputToken == null && currentTime.equals(_lastFireAtTime)) {
            if (_previousInput != null) {
        	// There is no new token, so we just update the most recently seen token.
        	SmoothToken projectedToNow = ((SmoothToken)_previousInput).extrapolate(currentTime);
        	// Create a virual new input with value 0.0.
        	inputToken = new SmoothToken(0.0, currentTime, projectedToNow.derivativeValues());
                if (_debugging) {
                    _debug("Projected zero crossing has occurred. Assumed input value: " + inputToken);
                }
            }
        }
        
        if (inputToken instanceof SmoothToken) {
            // At this point, either a new token has arrived, or a projected crossing
            // has occurred (in which case, the value of inputToken is identically 0.0.
            double inputValue = inputToken.doubleValue();

            // Check whether we should produce an output.
            if (_previousInput != null) {
        	// If either the input is zero or the sign of the input is opposite of
        	// the previous input, then produce an output if the direction matches.
        	double previousValue = _previousInput.doubleValue();
        	boolean inputIsRising = inputValue >= 0.0 && previousValue < 0.0;
        	boolean inputIsFalling = inputValue <= 0.0 && previousValue > 0.0;
        	if (_detectFallingCrossing && inputIsFalling
        		|| _detectRisingCrossing && inputIsRising) {
        	    output.send(0, value.getToken());
                    if (_debugging) {
                	_debug("Crossing detected. Sending output.");
                    }
        	}
            }
            
            // Predict the time of a future zero crossing.
            Time future = null;

            double[] derivatives = ((SmoothToken)inputToken).derivativeValues();
            // Handle linear case first.
            if (derivatives.length == 1
        	    || (derivatives.length == 2 && derivatives[1] == 0.0)
        	    || (derivatives.length > 2 && derivatives[1] == 0.0 && derivatives[2] == 0.0)) {
        	// There is a predictable zero crossing only if the derivative
        	// and value have opposite signs.
        	if (_detectRisingCrossing && inputValue < 0.0 && derivatives[0] > 0.0
        		|| _detectFallingCrossing && inputValue > 0.0 && derivatives[0] < 0.0) {
        	    future = currentTime.add(- inputValue / derivatives[0]);
        	}
            } else if (derivatives.length == 2
        	    || (derivatives.length > 2 && derivatives[2] == 0.0)) {
        	// Suppose the current value at the input is x.
        	// Then the time of the next zero crossing, if it exists, is t
        	// that solves the following quadratic (from the Taylor series expansion):
        	//   0 = (d2*t^2)/2 + d1*t + x,
        	// where d1 is the first derivative and d2 is the second.
        	double delta = PolynomialRoot.findMinimumPositiveRoot2(derivatives[1]/2.0, derivatives[0], inputValue);
        	if (delta >= _errorTolerance && delta != Double.POSITIVE_INFINITY) {
        	    future = currentTime.add(delta);
        	}
            } else if (derivatives.length >= 3) {
        	// Suppose the current value at the input is x.
        	// Then the time of the next zero crossing, if it exists, is t
        	// that solves the following cubic (from the Taylor series expansion):
        	//   0 = (d3*t^3)/6 + (d2*t^2)/2 + d1*t + x,
        	// where d1 is the first derivative, d2 is the second, and d3 is the third.
        	double delta = PolynomialRoot.findMinimumPositiveRoot3(
        		derivatives[2]/6.0, derivatives[1]/2.0, derivatives[0], inputValue, _errorTolerance, 0.0);
        	if (delta >= _errorTolerance && delta != Double.POSITIVE_INFINITY) {
        	    future = currentTime.add(delta);
        	}
            }
            if (future != null) {
        	// If the new zero crossing prediction differs from the old by the error
        	// tolerance or more from the old prediction, cancel the old prediction and
        	// replace it with the new.
        	if (_lastFireAtTime == null || Math.abs(future.subtractToDouble(_lastFireAtTime)) >= _errorTolerance) {
        	    // First cancel any previous fireAt() request.
        	    if (_lastFireAtTime != null) {
        		if (_debugging) {
        		    _debug("Cancelling previous fireAt request at " + _lastFireAtTime);
        		}
        		// Director class is checked in initialize().
        		((DEDirector) getDirector()).cancelFireAt(this, _lastFireAtTime);
        	    }

        	    getDirector().fireAt(this, future);
        	    _lastFireAtTime = future;
        	}
            } else {
        	// No future prediction.
        	// If there is a previous prediction, cancel it.
        	if (_lastFireAtTime != null) {
        	    if (_debugging) {
        		_debug("Cancelling previous fireAt request at " + _lastFireAtTime);
        	    }
        	    // Director class is checked in initialize().
        	    ((DEDirector) getDirector()).cancelFireAt(this, _lastFireAtTime);
        	    _lastFireAtTime = null;
        	}
            }
        } else {
            // An input token has arrived that is not a SmoothToken. Cancel any
            // previous projection, since we assume all derivatives are zero.
            if (_lastFireAtTime != null) {
        	if (_debugging) {
        	    _debug("Input has zero derivatives. Cancelling previous fireAt request at " + _lastFireAtTime);
        	}
        	// Director class is checked in initialize().
        	((DEDirector) getDirector()).cancelFireAt(this, _lastFireAtTime);
            }
            _lastFireAtTime = null;
        }
        _previousInput = (SmoothToken) inputToken;
    }

    /** Initialize this actor to indicate that no input has yet been provided.
     *  @throws IllegalActionException If the director is not a DEDirector.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _previousInput = null;
        _lastFireAtTime = null;
        
        if (!(getDirector() instanceof DEDirector)) {
            throw new IllegalActionException(this,
        	    "SmoothZeroCrossingDetector will only work with a DE or QSS director.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The level threshold this actor detects. */
    protected double _level;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag indicating whether this actor detects the level crossing
     *  when the input value is rising.
     */
    private boolean _detectRisingCrossing;

    /** Flag indicating whether this actor detects the level crossing
     *  when the input value is falling.
     */
    private boolean _detectFallingCrossing;
    
    /** Cache of the value of errorTolerance. */
    private double _errorTolerance;

    /** Track requests for firing. */
    private Time _lastFireAtTime;
    
    /** Previous input token, if any. */
    private SmoothToken _previousInput;
}
