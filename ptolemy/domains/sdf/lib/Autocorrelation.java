/* A polymorphic autocorrelation function.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Autocorrelation

/**
This actor calculates the autocorrelation of a sequence of input tokens.
<a name="autocorrelation"></a>
It is polymorphic, supporting any input data type that supports
multiplication, addition, and division by an integer.
However, since integer division will lose the fractional portion of the
result, type resolution will resolve the input type to double or double
matrix if the input port is connected to an integer or integer matrix source,
respectively.
<p>
Both biased and unbiased autocorrelation estimates are supported.
If the parameter <i>biased</i> is true, then
the autocorrelation estimate is
<a name="unbiased autocorrelation"></a>
<pre>
         N-1-k
       1  ---
r(k) = -  \    x<sup>*</sup>(n)x(n+k)
       N  /
          ---
          n = 0
</pre>
for <i>k </i>= 0<i>, ... , p</i>, where <i>N</i> is the number of
inputs to average (<i>numberOfInputs</i>), <i>p</i> is the number of
lags to estimate (<i>numberOfLags</i>), and x<sup>*</sup> is the
conjugate of the input (if it is complex).
This estimate is biased because the outermost lags have fewer than <i>N</i>
<a name="biased autocorrelation"></a>
terms in the summation, and yet the summation is still normalized by <i>N</i>.
<p>
If the parameter <i>biased</i> is false (the default), then the estimate is
<pre>
           N-1-k
        1   ---
r(k) = ---  \    x<sup>*</sup>(n)x(n+k)
       N-k  /
            ---
            n = 0
</pre>
In this case, the estimate is unbiased.
However, note that the unbiased estimate does not guarantee
a positive definite sequence, so a power spectral estimate based on this
autocorrelation estimate may have negative components.
<a name="spectral estimation"></a>
<p>
The output will be an array of tokens whose type is at least that
of the input. If the parameter <i>symmetricOutput</i> is true,
then the output will be symmetric and have length equal to twice
the number of lags requested plus one.  Otherwise, the output
will have length equal to twice the number of lags requested,
which will be almost symmetric (insert the last
sample into the first position to get the symmetric output that you
would get with the <i>symmetricOutput</i> being true).

@author Edward A. Lee and Yuhong Xiong
@version $Id$
@since Ptolemy II 1.0
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
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input_tokenConsumptionRate.setExpression("numberOfInputs");

        numberOfInputs =
            new Parameter(this, "numberOfInputs", new IntToken(256));
        numberOfInputs.setTypeEquals(BaseType.INT);

        numberOfLags = new Parameter(this, "numberOfLags", new IntToken(64));
        numberOfLags.setTypeEquals(BaseType.INT);

        biased = new Parameter(this, "biased", new BooleanToken(false));
        biased.setTypeEquals(BaseType.BOOLEAN);

        symmetricOutput =
            new Parameter(this, "symmetricOutput", new BooleanToken(false));
        symmetricOutput.setTypeEquals(BaseType.BOOLEAN);

        input.setTypeAtLeast(new FunctionTerm(input));

        // Set the output type to be an ArrayType.
        // This is refined further by the typeConstraintList method.
        output.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        attributeChanged(numberOfInputs);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, the estimate will be biased.
     *  This is a boolean with default value false.
     */
    public Parameter biased;

     /** Number of input samples to average.
     *  This is an integer with default value 256.
     */
    public Parameter numberOfInputs;

    /** Number of autocorrelation lags to output.
     *  This is an integer with default value 64.
     */
    public Parameter numberOfLags;

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
        if (attribute == numberOfInputs
            || attribute == numberOfLags
            || attribute == symmetricOutput) {
            _numberOfInputs = ((IntToken) numberOfInputs.getToken()).intValue();
            _numberOfLags = ((IntToken) numberOfLags.getToken()).intValue();
            _symmetricOutput =
                ((BooleanToken) symmetricOutput.getToken()).booleanValue();

            if (_numberOfInputs <= 0) {
                throw new IllegalActionException(
                    this,
                    "Invalid numberOfInputs: " + _numberOfInputs);
            }

            if (_numberOfLags <= 0) {
                throw new IllegalActionException(
                    this,
                    "Invalid numberOfLags: " + _numberOfLags);
            }

            if (_symmetricOutput) {
                _lengthOfOutput = 2 * _numberOfLags + 1;
            } else {
                _lengthOfOutput = 2 * _numberOfLags;
            }
     
            if (_outputs == null || _lengthOfOutput != _outputs.length) {
                _outputs = new Token[_lengthOfOutput];
            }

        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
        throws CloneNotSupportedException {
        Autocorrelation newObject = (Autocorrelation) super.clone(workspace);
        newObject.input.setTypeAtLeast(new FunctionTerm(newObject.input));
        return newObject;
    }

    /** Consume tokens from the input and produce a token on the output
     *  that contains an array token that represents an autocorrelation
     *  estimate of the consumed tokens.  The estimate is consistent with
     *  the parameters of the object, as described in the class comment.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        boolean biasedValue = ((BooleanToken) biased.getToken()).booleanValue();
        Token[] inputValues = input.get(0, _numberOfInputs);
        int notSymmetric = _symmetricOutput ? 0 : 1;
        // NOTE: Is there a better way to determine whether the input
        // is complex?
        boolean complex = inputValues[0] instanceof ComplexToken;
        for (int i = _numberOfLags; i >= 0; i--) {
            Token sum = inputValues[0].zero();
            for (int j = 0; j < _numberOfInputs - i; j++) {
                if (complex) {
                    ComplexToken conjugate =
                        new ComplexToken(
                            ((ComplexToken) inputValues[j])
                                .complexValue()
                                .conjugate());
                    sum = sum.add(conjugate.multiply(inputValues[j + i]));
                } else {
                    sum = sum.add(inputValues[j].multiply(inputValues[j + i]));
                }
            }
            if (biasedValue) {
                _outputs[i + _numberOfLags - notSymmetric] =
                    sum.divide(numberOfInputs.getToken());
            } else {
                _outputs[i + _numberOfLags - notSymmetric] =
                    sum.divide(new IntToken(_numberOfInputs - i));
            }
        }
        // Now fill in the first half, which by symmetry is just
        // identical to what was just produced, or its conjugate if
        // the input is complex.
        for (int i = _numberOfLags - 1 - notSymmetric; i >= 0; i--) {
            if (complex) {
                ComplexToken candidate =
                    (ComplexToken) _outputs[2 * (_numberOfLags - notSymmetric)
                        - i];
                _outputs[i] =
                    new ComplexToken(candidate.complexValue().conjugate());
            } else {
                _outputs[i] = _outputs[2 * (_numberOfLags - notSymmetric) - i];
            }
        }
        output.broadcast(new ArrayToken(_outputs));
    }

    /** If there are not sufficient inputs, then return false.
     *  Otherwise, return whatever the base class returns.
     *  @exception IllegalActionException If the base class throws it.
     *  @return True if it is ok to continue.
     */
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0, _numberOfInputs)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        } else {
            return super.prefire();
        }
    }

    /** Return the type constraint that the type of the elements of the
     *  output array is no less than the type of the input port.
     *  @return A list of inequalities.
     */
    public List typeConstraintList() {
        List result = super.typeConstraintList();
        if (result == null) {
            result = new LinkedList();
        }
        ArrayType outArrType = (ArrayType) output.getType();
        InequalityTerm elementTerm = outArrType.getElementTypeTerm();
        Inequality ineq = new Inequality(input.getTypeTerm(), elementTerm);

        result.add(ineq);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _numberOfInputs;
    private int _numberOfLags;
    private int _lengthOfOutput;
    private boolean _symmetricOutput;
    private Token[] _outputs;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Int or IntMatrix; otherwise, the result is Double
    // or DoubleMatrix, respectively.
    private class FunctionTerm extends MonotonicFunction {

        // The constructor takes a port argument so that the clone()
        // method can construct an instance of this class for the
        // input port on the clone.
        private FunctionTerm(TypedIOPort port) {
            _port = port;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() {
            Type inputType = _port.getType();
            if (inputType == BaseType.INT) {
                return BaseType.DOUBLE;
            } else if (inputType == BaseType.INT_MATRIX) {
                return BaseType.DOUBLE_MATRIX;
            } else {
                return inputType;
            }
        }

        /** Return an one element array containing the InequalityTerm
         *  representing the type of the input port.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            InequalityTerm[] variable = new InequalityTerm[1];
            variable[0] = _port.getTypeTerm();
            return variable;
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

        private TypedIOPort _port;
    }
}
