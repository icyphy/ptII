/* Check the input streams against a parameter value.

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
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.math.Complex;

//////////////////////////////////////////////////////////////////////////
//// Test
/**
This actor compares the inputs against the value specified by
the <i>correctValues</i> parameter.  That parameter is an ArrayToken,
which is an array of tokens.  The length of this array is the number
of iterations of this actor that are tested.  Subsequent iterations
always succeed, so the actor can be used as a "power-up" test for
a model, checking the first few iterations against some known results.
<p>
The input is a multiport.  If there is more than one channel connected
to it, then each element of <i>correctValues</i> must itself be an
ArrayToken, with length matching the number of channels.
Suppose for example that the width of the input is one
and the first three inputs should be 1, 2, and 3.  Then you can
set <i>correctValues</i> to
<pre>
    [1, 2, 3]
</pre>
or
<pre>
    [1; 2; 3]
</pre>
Either syntax is acceptable.
Suppose instead that the input has width two, and the correct values
in the first iteration are 1 on the first channel and 2 on the second.
Then on the second iteration, the correct values are 3 on the first
channel and 4 on the second.  Then you can set <i>correctValues</i> to
<pre>
    [1, 2; 3, 4]
</pre>
With this setting, no tests are performed after the first two iterations
of this actor.
<p>
The input values are checked in the postfire() method, which checks to
make sure that each input channel has a token.  If an input value is
missing or differs from what it should be, then postfire() throws an
exception. Thus, the test passes if no exception is thrown.
<p>
If the input is a DoubleToken or ComplexToken,
then the comparison passes if the value is close to what it should
be, within the specified <i>tolerance</i> (which defaults to
10<sup>-9</sup>.  The input data type is undeclared, so it can
resolve to anything.

@author Edward A. Lee
@version $Id$
*/

public class Test extends Sink {

    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Test(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        Token[] defaultEntries = new Token[1];
        defaultEntries[0] = new BooleanToken(true);
        ArrayToken defaultArray = new ArrayToken(defaultEntries);
        correctValues = new Parameter(this, "correctValues", defaultArray);
	correctValues.setTypeEquals(new ArrayType(BaseType.NAT));

        tolerance = new Parameter(this, "tolerance", new DoubleToken(1e-9));
        tolerance.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A matrix specifying what the input should be. */
    public Parameter correctValues;

    /** A double specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.
     */
    public Parameter tolerance;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to allow type changes to the
     *  <i>correctValues</i> parameter.
     *  @exception IllegalActionException If the new parameter value
     *   is not an array.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == correctValues) {
            if (!(correctValues.getToken() instanceof ArrayToken)) {
                throw new IllegalActionException(this,
                "correctValues parameter is required to have an array value.");
            }
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Test newobj = (Test)super.clone(ws);
        newobj.correctValues = (Parameter)newobj.getAttribute("correctValues");
        newobj.tolerance = (Parameter)newobj.getAttribute("tolerance");
        return newobj;
    }

    /** Override the base class to set the iteration counter to zero.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = 0;
    }

    /** Read one token from each input channel and compare against
     *  the value specified in <i>correctValues</i>.  If the interation count
     *  is larger than the length of <i>correctValues</i>, then return
     *  immediately, declaring success on the test.
     *  @exception IllegalActionException If an input is missing,
     *   or if its value does not match the required value.
     */
    public boolean postfire() throws IllegalActionException {
        if (_count >= ((ArrayToken)(correctValues.getToken())).length()) {
            return true;
        }
        Token referenceToken
                = ((ArrayToken)(correctValues.getToken())).getElement(_count);
        Token[] reference;
        int width = input.getWidth();
        if (width == 1 && !(referenceToken instanceof ArrayToken)) {
            reference = new Token[1];
            reference[0] = referenceToken;
        } else {
            try {
                reference = ((ArrayToken)referenceToken).arrayValue();
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this,
                "Test fails in iteration " + _count + ".\n"
                + "Width of input is " + width
                + ", but correctValues parameter is not an array of arrays.");
            }
            if (width != reference.length) {
                throw new IllegalActionException(this,
                "Test fails in iteration " + _count + ".\n"
                + "Width of input is " + width
                + ", which does not match the width of the " + _count
                + "-th element of correctValues, "
                + reference.length);
            }
        }
        for (int i = 0; i < width; i++) {
            if (!input.hasToken(i)) {
                throw new IllegalActionException(this,
                "Test fails in iteration " + _count + ".\n"
                + "Empty input on channel " + i);
            }
            Token token = input.get(i);
            if (token instanceof DoubleToken) {
                // Check using tolerance.
                Token correctValue = reference[i];
                try {
                    double correct = ((DoubleToken)correctValue).doubleValue();
                    double seen = ((DoubleToken)token).doubleValue();
                    double ok
                        = ((DoubleToken)(tolerance.getToken())).doubleValue();
                    if (Math.abs(correct - seen) > ok) {
                        throw new IllegalActionException(this,
                        "Test fails in iteration " + _count + ".\n"
                        + "Value was: " + seen
                        + ". Should have been: " + correct);
                    }
                } catch (ClassCastException ex) {
                    throw new IllegalActionException(this,
                    "Test fails in iteration " + _count + ".\n"
                    + "Input is a double but correct value is not: "
                    + correctValue.toString());
                }
            } else if (token instanceof ComplexToken) {
                // Check using tolerance.
                Token correctValue = reference[i];
                try {
                    Complex correct
                        = ((ComplexToken)correctValue).complexValue();
                    Complex seen
                        = ((ComplexToken)token).complexValue();
                    double ok
                        = ((DoubleToken)(tolerance.getToken())).doubleValue();
                    if (Math.abs(correct.real - seen.real) > ok ||
                            Math.abs(correct.imag - seen.imag) > ok) {
                        throw new IllegalActionException(this,
                        "Test fails in iteration " + _count + ".\n"
                        + "Value was: " + seen
                        + ". Should have been: " + correct);
                    }
                } catch (ClassCastException ex) {
                    throw new IllegalActionException(this,
                    "Test fails in iteration " + _count + ".\n"
                    + "Input is complex but correct value is not: "
                    + correctValue.toString());
                }
            } else {
                Token correctValue = reference[i];
                BooleanToken result = token.isEqualTo(correctValue);
                if (!result.booleanValue()) {
                    throw new IllegalActionException(this,
                    "Test fails in iteration " + _count + ".\n"
                    + "Value was: " + token
                    + ". Should have been: " + correctValue);
                }
            }
        }
        _count++;
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Count of iterations.
    private int _count = 0;
}
