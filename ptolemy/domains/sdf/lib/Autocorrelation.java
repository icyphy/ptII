/* A polymorphic autocorrelation function.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// Autocorrelation

/**
This actor calculates the autocorrelation of a sequence of input tokens.
<a name="autocorrelation"></a>
It is polymorphic, supporting any input data type that supports
multiplication, addition, and division by an integer.
Both biased and unbiased autocorrelation estimates are supported.
<p>
If the parameter <i>biased</i> is true, then
the autocorrelation estimate is
<a name="unbiased autocorrelation"></a>
<pre>
         N-1-k
       1  --- 
r(k) = -  \    x(n)x(n+k)
       N  /
          ---
          n=0
</pre>
for <i>k </i>=0<i>, ... , p</i>, where <i>N</i> is the number of
inputs to average (<i>numberOfInputs</i>) and <i>p</i> is the number of
lags to estimate (<i>numberOfLags</i>).
This estimate is biased because the outermost lags have fewer than <i>N</i>
<a name="biased autocorrelation"></a>
terms in the summation, and yet the summation is still normalized by <i>N</i>.
<p>
If the parameter <i>biased</i> is false (the default), then the estimate is
<pre>
           N-1-k
        1   ---
r(k) = ---  \    x(n)x(n+k)
       N-k  /
            ---
            n=0
</pre>
In this case, the estimate is unbiased.
However, note that the unbiased estimate does not guarantee
a positive definite sequence, so a power spectral estimate based on this
autocorrelation estimate may have negative components.
<a name="spectral estimation"></a>
<p>
If the parameter <i>symmetricOutput</i> is true, then the output
will be symmetric and contain a number of samples equal to twice
the number of lags requested plus one.  Otherwise, the output
will be twice the number of lags requested, which will be almost
symmetric (discard the last sample to get a perfectly symmetric output).

@author Edward A. Lee
@version $Id$
*/

public class Autocorrelation extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Autocorrelation(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

	numberOfInputs = new Parameter(this,
                "numberOfInputs", new IntToken(256));
	numberOfLags = new Parameter(this,
                "numberOfLags", new IntToken(64));
	biased = new Parameter(this,
                "biased", new BooleanToken(false));
	symmetricOutput = new Parameter(this,
                "symmetricOutput", new BooleanToken(false));
        attributeChanged(numberOfInputs);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Number of input samples to average.
     *  This is an integer with default value 256.
     */
    public Parameter numberOfInputs;

    /** Number of autocorrelation lags to output.
     *  This is an integer with default value 64.
     */
    public Parameter numberOfLags;

    /** If true, the estimate will be biased.
     *  This is a boolean with default value false.
     */
    public Parameter biased;

    /** If true, then the output from each firing
     *  will have 2*<i>numberOfLags</i> + 1
     *  samples (an odd number) whose values are symmetric about
     *  the midpoint. If false, then the output from each firing will
     *  have 2*<i>numberOfLags</i> samples (an even number)
     *  by omitting one of the endpoints (the last one).
     *  This is a boolean with default value false.
     */
    public Parameter symmetricOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>order</i> parameter, then
     *  set up the consumption and production constants, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numberOfInputs ||
                attribute == numberOfLags ||
                attribute == symmetricOutput) {
            _numberOfInputs = ((IntToken)numberOfInputs.getToken()).intValue();
            _numberOfLags = ((IntToken)numberOfLags.getToken()).intValue();
            _symmetricOutput
                    = ((BooleanToken)symmetricOutput.getToken()).booleanValue();

            if(_numberOfInputs <= 0) {
                throw new IllegalActionException(this,
                "Invalid numberOfInputs: " + _numberOfInputs);
            }

            if(_numberOfLags <= 0) {
                throw new IllegalActionException(this,
                "Invalid numberOfLags: " + _numberOfLags);
            }

            if (_symmetricOutput) {
                _numberOfOutputs = 2*_numberOfLags + 1;
            } else {
                _numberOfOutputs = 2*_numberOfLags;
            }
            input.setTokenConsumptionRate(_numberOfInputs);
            output.setTokenProductionRate(_numberOfOutputs);
            if (_outputs == null || _numberOfOutputs > _outputs.length) {
                _outputs = new Token[_numberOfOutputs];
            }

            Director dir = getDirector();
            if (dir != null) {
                dir.invalidateSchedule();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume the inputs and produce the outputs.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        boolean biasedValue = ((BooleanToken)biased.getToken()).booleanValue();
        Token[] inputValues = input.get(0, _numberOfInputs);
        int notsymmetric = _symmetricOutput ? 0 : 1;
        for(int i = _numberOfLags; i >= 0; i--) {
            Token sum = inputValues[0].zero();
            for(int j = 0; j < _numberOfInputs - i; j++) {
                sum = sum.add(inputValues[j].multiply(inputValues[j + i]));
            }
            if (biasedValue) {
                _outputs[i + _numberOfLags - notsymmetric]
                         = sum.divide(numberOfInputs.getToken());
            } else {
                _outputs[i + _numberOfLags - notsymmetric]
                         = sum.divide(new IntToken(_numberOfInputs - i));
	    }
        }
        // Now fill in the first half, which by symmetry is just
        // identical to what was just produced.
        for(int i = _numberOfLags - 1 - notsymmetric; i >= 0; i--) {
            _outputs[i] = _outputs[2 * (_numberOfLags - notsymmetric) - i];
        }
        output.send(0, _outputs, _numberOfOutputs);
    }

    /** If there are not sufficient inputs, then return false.
     *  Otherwise, return whatever the base class returns.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0, _numberOfInputs)) return false;
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _numberOfInputs;
    private int _numberOfLags;
    private int _numberOfOutputs;
    private boolean _symmetricOutput;
    private Token[] _outputs;
}
