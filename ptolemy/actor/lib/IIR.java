/* An IIR filter actor that uses a direct form II implementation.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// IIR
/**
This actor implements an infinite impulse response (IIR) filter. A
direct form II implementation is used.
<p>
This filter has a transfer function given by:
<pre>
       b<sub>0</sub> + b<sub>1</sub>z<sup>-1</sup> + ... + b<sub>M</sub>z<sup>-M</sup>
      ------------------------
       1 + a<sub>1</sub>z<sup>-1</sup> + ... + a<sub>N</sub>z<sup>-N</sup>
</pre>
The constant terms of the numerator polynomial are specified by the <i>numerator</i> parameter and the constant terms of the denominator polynomial are specified by the <i>denominator</i> parameter.
<p>
The <i>numerator</i> parameter represents the numerator coefficients as a row vector of a token of type DoubleMatrixToken. The format is {{b<sub>0</sub>, b<sub>1</sub>, ..., b<sub>M</sub>}}. The default value of this parameter is {{1.0}}.
<p>
The <i>denominator</i> parameter represents the denominator coefficients as a row vector of a token of type DoubleMatrixToken. The format is {{a<sub>0</sub>, a<sub>1</sub>, ..., a<sub>N</sub>}}. Note that the value of a<sub>0</sub> is constrained to be 1. This implementation will silently ignore whatever value the user enters for as the first element of <i>denominator</i>, and will use 1.0 instead.  The default value of this parameter is {{1.0}}.
<p>
The default values therefore correspond to a filter with a transfer function of 1.
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
    public IIR(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // parameters
	double numeratorTaps[][] = {{1.0}};
	double denominatorTaps[][] = {{1.0}};
	numerator =
	    new Parameter(this, "numerator", new DoubleMatrixToken(numeratorTaps));
	attributeChanged(numerator);
	denominator = 
	    new Parameter(this, "denominator", new DoubleMatrixToken(denominatorTaps));
	attributeChanged(denominator);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This parameter represents the numerator coefficients as a row 
     *  vector of a token of type DoubleMatrixToken. The format is 
     *  {{b<sub>0</sub>, b<sub>1</sub>, ..., b<sub>M</sub>}}. The default 
     *  value of this parameter is {{1.0}}.
     */
    public Parameter numerator;

    /** This  parameter represents the denominator coefficients as a row 
     *  vector of a token of type DoubleMatrixToken. The format is 
     *  {{a<sub>0</sub>, a<sub>1</sub>, ..., a<sub>N</sub>}}. Note that 
     *  the value of a<sub>0</sub> is constrained to be 1. This 
     *  implementation will silently ignore whatever value the user 
     *  enters for as the first element of <i>denominator</i>, and will 
     *  use 1.0 instead.  The default value of this parameter is {{1.0}}.
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
	//System.out.println("attributeChanged(): invoked on " + 
	//   attribute.getName());
        if (attribute == numerator) {
	    _numerator =  (((DoubleMatrixToken)numerator.getToken()).doubleMatrix())[0];
	} else if (attribute == denominator) {
	    _denominator = (((DoubleMatrixToken)denominator.getToken()).doubleMatrix())[0];
	    // Note: a<sub>0</sub> must always be 1, so ignore whatever the user
	    // entered and just use 1.
	    _denominator[0] = 1.0;
	} else {
	    throw new IllegalActionException(this,
                "Unrecognized parameter: " + attribute.getName());
        }
	// Initialize filter state.
	if ((_numerator != null) && (_denominator != null)) {
	    int stateSize = 
		(int)java.lang.Math.max(_numerator.length, _denominator.length);
	    _stateVector = new double[stateSize];
	}
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        IIR newobj = (IIR)super.clone(ws);
        newobj.numerator = (Parameter)newobj.getAttribute("numerator");
        newobj.denominator = (Parameter)newobj.getAttribute("denominator");
        return newobj;
    }

    /** Consume an input token and compute a single output token.
     *  @exception IllegalActionException Not thrown
     */
    public void fire() throws IllegalActionException {
	// NOTE: iterate() should never return STOP_ITERATING, so 
	// this should be safe.
 	iterate(1);
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
	_curTap = 0;
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
	//System.out.println("iterate() invoked........");
	// Check if we need to reallocate the output token array.
	if (count > _resultArray.length) {
	    _resultArray = new DoubleToken[count];
	}
	// The current input sample.
	double xCur;
	// The current output sample.
	double yCur;
	double wn;
        if (input.hasToken(0, count)) {
	    // NOTE: inArray.length may be > count, in which case
	    // only the first count tokens are valid.
            Token[] inArray = input.get(0, count);
	    for (int i = 0; i < count; i++) {
		xCur = ((DoubleToken)(inArray[i])).doubleValue();

		wn = xCur;
		//System.out.println("_curTap = " + _curTap);
		//System.out.println(" _denominator.length = " +  _denominator.length);
		for (int j = 1; j < _denominator.length; j++) {
		    wn += 
			_denominator[j]*_stateVector[(_curTap + j) % 
						     _stateVector.length];
		    // System.out.println("j = " + j + ", _stateVector val = " + 
		    //_stateVector[(_curTap + j+1) %  _stateVector.length] +
		    //" index = " + (_curTap + j+1) %  _stateVector.length); 
		}
		_stateVector[_curTap] = wn;
		yCur = 0;
		//System.out.println("_numerator.length = " +  _numerator.length);
		for (int k = 0; k < _numerator.length; k++) {
		    yCur += 
			_numerator[k]*_stateVector[(_curTap +k) % 
						   _stateVector.length];
		    // System.out.println("k = " + k + ",  _stateVector val = " +
		    //_stateVector[(_curTap +k) %   _stateVector.length] +
		    //" index = " + (_curTap +k) %   _stateVector.length);
		}
		_resultArray[i] = new DoubleToken(yCur);
		// Update the state vector pointer.
		if (--_curTap < 0) _curTap = _stateVector.length - 1;
	    }
	    //System.out.println("output = " + _resultArray[0]);
            output.send(0, _resultArray, count);
            return COMPLETED;
        } else {
            return NOT_READY;
        }
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
    private int _curTap;
}
