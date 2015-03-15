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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input signal. This has type double and is normally a SmoothToken. */
    public TypedIOPort input;

    /** Output event with value 0.0 when the zero crossing occurs. */
    public TypedIOPort output;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        Time currentTime = getDirector().getModelTime();
        if (_debugging) {
            _debug("Firing at time: " + currentTime);
        }
        if (input.hasNewToken(0)) {
            // If there is an input, then cancel any previous fireAt() request.
            if (_lastFireAtTime != null) {
                if (_debugging) {
                    _debug("Canceling previous fireAt request at " + _lastFireAtTime);
                }
                // Director class is checked in initialize().
                ((DEDirector) getDirector()).cancelFireAt(this, _lastFireAtTime);
            }

            DoubleToken inputToken = (DoubleToken)input.get(0);
            double inputValue = inputToken.doubleValue();
            if (_debugging) {
        	_debug("Read input: " + inputToken);
            }
            if (_previousInput != null) {
        	double previousValue = _previousInput.doubleValue();
        	// If either the input is zero or the sign of the input is opposite of
        	// the previous input, then produce an output.
        	// Note that normally, we do not compare doubles, but in this case,
        	// an exactly zero input discrete event is treated as a zero crossing.
        	if (inputValue == 0.0 || (inputValue * previousValue < 0.0)) {
        	    output.send(0, DoubleToken.ZERO);
        	}
            } else if (inputValue == 0.0) {
        	// If the input is zero, produce an output even if there is no previous input.
        	output.send(0, DoubleToken.ZERO);
            }
            _previousInput = inputToken;
            
            // If the input is a SmoothToken, the predict the time of a future zero
            // crossing.
            if (inputToken instanceof SmoothToken) {
        	// FIXME: Find a general solution here.
        	// FIXME: Move this code to SmoothToken.
        	double[] derivatives = ((SmoothToken)inputToken).derivativeValues();
        	// Handle linear case first.
        	// FIXME: Discarding all higher order derivatives.
        	if (derivatives.length == 1
        		|| (derivatives.length > 1 && derivatives[1] == 0.0)) {
        	    // There is a predictable zero crossing only if the derivative
        	    // and value have opposite signs.
        	    if (inputValue * derivatives[0] < 0) {
        		Time future = currentTime.add(- inputValue / derivatives[0]);
        		getDirector().fireAt(this, future);
        		_lastFireAtTime = future;
        	    }
        	} else if (derivatives.length >= 2) {
        	    // FIXME: Discarding all higher order derivatives.
        	    // Suppose the current value at the input is x.
        	    // Then the time of the next zero crossing, if it exists, is t
        	    // that solves the following quadratic:
        	    //  0 = x + d1*t + d2*t^2,
        	    // where d1 is the first derivative and d2 is the second.
        	    // The quadratic formula says
        	    //  t = (-d1 +- sqrt(d1^2 - 2*x*d2))/d2
        	    // This has a real-valued solution iff d1^2 >= 2*x*d2 and d2 != 0.
        	    // Already checked the second condition.
        	    double d1squared = derivatives[0] * derivatives[0];
        	    if (d1squared >= 2 * inputValue * derivatives[1]) {
        		double sqrt = Math.sqrt(d1squared - 2 * inputValue * derivatives[1]);
        		double first = (-derivatives[0] + sqrt) / derivatives[1];
        		double second = (-derivatives[0] - sqrt) / derivatives[1];
        		Time future = null;
        		if (first < second && first > 0.0) {
        		    future = currentTime.add(first);
        		} else if (second > 0.0) {
        		    future = currentTime.add(second);
        		}
        		if (future != null) {
        		    getDirector().fireAt(this, future);
        		    _lastFireAtTime = future;
        		}
        	    }
        	}
            }
        } else if (currentTime.equals(_lastFireAtTime)) {
            // There is no input, and time matches the last requested firing time.
            output.send(0, DoubleToken.ZERO);
            _lastFireAtTime = null;
        }
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
    ////                         private variables                 ////

    /** Track requests for firing. */
    private Time _lastFireAtTime;
    
    /** Previous input token, if any. */
    private DoubleToken _previousInput;
}
