/* An IIR filter actor that uses a direct form II implementation.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

///////////////////////////////////////////////////////////////////
//// IIR
/**
This actor is an implementation of an infinite impulse response IIR filter.
A direct form II [1] implementation is used. The input and output types
are DoubleToken.
<p>
This filter has a transfer function given by:
<pre>
       b<sub>0</sub> + b<sub>1</sub>z<sup>-1</sup> + ... + b<sub>M</sub>z<sup>-M</sup>
      ------------------------
       1 + a<sub>1</sub>z<sup>-1</sup> + ... + a<sub>N</sub>z<sup>-N</sup>
</pre>
The constant terms of the numerator polynomial are specified by the
<i>numerator</i> parameter and the constant terms of the denominator
polynomial are specified by the <i>denominator</i> parameter.
Both of these are given as arrays of doubles, using the syntax
<pre>
   {b<sub>0</sub>, b<sub>1</sub>, ..., b<sub>M</sub>}
</pre>
The default numerator and denominator are both {1.0}, resulting in
a filter with unity transfer function.
<p>
The first coefficient of the
<i>denominator</i> parameter is required to be 1.0. This
implementation will issue a warning if the user enters something
other than 1.0, and will use 1.0 instead.  The value is shown explicitly
nonetheless so that the numerator and denominator polynomials are easy
to read.
<p>
<b>References</b>
<p>
[1]
A. V. Oppenheim, R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
Prentice Hall, 1989.

@author Brian K. Vogel
@version $Id$
*/
public class IIR extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public IIR(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // Parameters
	numerator = new Parameter(this, "numerator");
        numerator.setExpression("{1.0}");
	attributeChanged(numerator);
	denominator = new Parameter(this, "denominator");
        denominator.setExpression("{1.0}");
	attributeChanged(denominator);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This parameter represents the numerator coefficients as an array
     *  of a tokens of type double. The format is
     *  {b<sub>0</sub>, b<sub>1</sub>, ..., b<sub>M</sub>}. The default
     *  value of this parameter is {1.0}.
     */
    public Parameter numerator;

    /** This  parameter represents the denominator coefficients as an
     *  array of a tokens of type double. The format is
     *  {a<sub>0</sub>, a<sub>1</sub>, ..., a<sub>N</sub>}. Note that
     *  the value of a<sub>0</sub> is constrained to be 1.0. This
     *  implementation will issue a warning if it is not.
     *  The default value of this parameter is {1.0}.
     */
    public Parameter denominator;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle parameter change events on the
     *  <i>numerator</i> and <i>denominator</i> parameters. The
     *  filter state vector is reinitialized to zero state.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If this method is invoked
     *   with an unrecognized parameter.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numerator) {
            ArrayToken numeratorValue = (ArrayToken)numerator.getToken();
            _numerator = new double[numeratorValue.length()];
            for (int i = 0; i < numeratorValue.length(); i++) {
                _numerator[i] = ((DoubleToken)numeratorValue.getElement(i))
                    .doubleValue();
            }
	} else if (attribute == denominator) {
            ArrayToken denominatorValue = (ArrayToken)denominator.getToken();
            _denominator = new double[denominatorValue.length()];
            for (int i = 0; i < denominatorValue.length(); i++) {
                _denominator[i] = ((DoubleToken)denominatorValue.getElement(i))
                    .doubleValue();
            }

	    // Note: a<sub>0</sub> must always be 1.
            // Issue a warning if it isn't.
            if (_denominator[0] != 1.0) {
                try {
                    MessageHandler.warning(
                            "First denominator value is required to be 1.0. "
                            + "Using 1.0.");
                } catch (CancelException ex) {
                    throw new IllegalActionException(this,
                            "Canceled parameter change.");
                }
                // Override the user and just use 1.
                _denominator[0] = 1.0;
            }
	} else {
            super.attributeChanged(attribute);
            return;
        }
	// Initialize filter state.
	if ((_numerator != null) && (_denominator != null)) {
	    int stateSize = (int)java.lang.Math.max(_numerator.length,
                    _denominator.length);
	    _stateVector = new double[stateSize];
	}
    }

    /** If at least one input token is available, consume a single
     *  input token, apply the filter to that input token, and
     *  compute a single output token. If this method is invoked
     *  multiple times in one iteration, then only the input read
     *  on the last invocation in the iteration will affect the
     *  filter state.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
	// The current input sample.
	double xCurrent;
	// The current output sample.
	double yCurrent;
	double window;
        if (input.hasToken(0)) {
            DoubleToken in = (DoubleToken)input.get(0);
	    xCurrent = (in).doubleValue();
	    window = xCurrent;
	    for (int j = 1; j < _denominator.length; j++) {
		window += -_denominator[j]*_stateVector[(_currentTap + j) %
                        _stateVector.length];
	    }
	    // Shadowed state. used in postfire().
	    _latestWindow = window;
	    // Save state vector value.
	    double savedState = _stateVector[_currentTap];
	    _stateVector[_currentTap] = window;
	    yCurrent = 0;
	    for (int k = 0; k < _numerator.length; k++) {
		yCurrent += _numerator[k]*_stateVector[(_currentTap +k) %
                        _stateVector.length];
	    }
	    // Restore state vector to previous state.
	    _stateVector[_currentTap] = savedState;
	    DoubleToken out = new DoubleToken(yCurrent);
            output.send(0, out);
	}
    }

    /**  Initialize the filter state vector with zero state.
     *   @exception IllegalActionException If the base class throws
     *    it.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
	// Initialize filter state.
	int stateSize =
	    (int)java.lang.Math.max(_numerator.length, _denominator.length);
	_stateVector = new double[stateSize];
	_currentTap = 0;
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration causes the filter to consume an input token and
     *  compute a single output token. An invocation
     *  of this method therefore applies the filter to <i>count</i>
     *  successive input tokens.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY, and do
     *   not consume any input tokens.
     *  @exception IllegalActionException If iterating cannot be
     *  performed.
     */
    public int iterate(int count) throws IllegalActionException {
	// Check if we need to reallocate the output token array.
	if (count > _resultArray.length) {
	    _resultArray = new DoubleToken[count];
	}
	// The current input sample.
	double xCurrent;
	// The current output sample.
	double yCurrent;
	double window;
        if (input.hasToken(0, count)) {
	    // NOTE: inArray.length may be > count, in which case
	    // only the first count tokens are valid.
            Token[] inArray = input.get(0, count);
	    for (int i = 0; i < count; i++) {
		xCurrent = ((DoubleToken)(inArray[i])).doubleValue();

		window = xCurrent;
		for (int j = 1; j < _denominator.length; j++) {
		    window += -_denominator[j]*_stateVector[(_currentTap + j) %
                            _stateVector.length];
		}
		_stateVector[_currentTap] = window;
		yCurrent = 0;
		for (int k = 0; k < _numerator.length; k++) {
		    yCurrent +=	_numerator[k]*_stateVector[(_currentTap +k) %
                            _stateVector.length];
		}
		_resultArray[i] = new DoubleToken(yCurrent);
		// Update the state vector pointer.
		if (--_currentTap < 0) _currentTap = _stateVector.length - 1;
	    }
            output.send(0, _resultArray, count);
            return COMPLETED;
        } else {
            return NOT_READY;
        }
    }

    /** Update the filter state.
     *
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
	_stateVector[_currentTap] = _latestWindow;
	// Update the state vector pointer.
	if (--_currentTap < 0) _currentTap = _stateVector.length - 1;
	return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private DoubleToken[] _resultArray = new DoubleToken[1];

    // Filter parameters
    private double[] _numerator;
    private double[] _denominator;

    // Filter state vector
    private double[] _stateVector;

    // State vector pointer
    private int _currentTap;

    // Shadow state.
    private double _latestWindow;
}
