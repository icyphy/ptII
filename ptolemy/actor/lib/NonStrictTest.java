/* Check the input streams against a parameter value, ignoring absent values.

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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.StringUtilities;

import java.util.ArrayList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// NonStrictTest
/**

This actor compares the inputs against the value specified by the
<i>correctValues</i> parameter.  That parameter is an ArrayToken,
where each element of the array is of the same type as the input.
On each firing where the input is present, the value of the input
is compared against the next token in the <i>correctValues</i>
parameter.  If it matches, the firing succeeds. If it doesn't
match, then an exception is thrown. After matching each of
the value in the <i>correctValues</i> parameter, subsequent iterations
always succeed, so the actor can be used as a "power-up" test for a model,
checking the first few iterations against some known results.
<p>
Unlike the Test actor, NonStrictTest does not support a multiport
input, only single port inputs are supported.  This also differs
from Test in that it ignores absent inputs, and it checks the inputs
in the postfire() method rather than the fire() method.
<p>
If the input is a DoubleToken or ComplexToken, then the comparison
passes if the value is close to what it should be, within the
specified <i>tolerance</i> (which defaults to 10<sup>-9</sup>.  The
input data type is undeclared, so it can resolve to anything.
<p>
If the parameter <i>trainingMode</i> is <i>true</i>, then instead
of performing the test, this actor collects the inputs into the
<i>correctValues</i> parameter.  Thus, to use this actor, you can
place it in a model, set <i>trainingMode</i> to <i>true</i> to
collect the reference data, then set <i>trainingMode</i> to
<i>false</i>.  Any subsequent run of the actor will throw an
exception if the input data does not match the training data.
The value of the reference token is set in the wrapup() method.

@see Test
@author Paul Whitaker, Christopher Hylands, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/

public class NonStrictTest extends Sink {

    // The Test actor could be extended so that Strictness was a parameter,
    // but that would require some slightly tricky code to handle
    // multiports in a non-strict fashion.  The problem is that if
    // we have more than one input channel, and we want to handle
    // non-strict inputs, then we need to keep track of number of
    // tokens we have seen on each channel. Also, this actor does
    // not read inputs until postfire(), which is too late to produce
    // an output, as done by Test.

    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonStrictTest(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        correctValues = new Parameter(this, "correctValues");
        correctValues.setExpression("[true]");
        correctValues.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        tolerance = new Parameter(this, "tolerance");
        tolerance.setExpression("1.0E-9");
        tolerance.setTypeEquals(BaseType.DOUBLE);

        trainingMode = new Parameter(this, "trainingMode");
        trainingMode.setExpression("false");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        
        input.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A matrix specifying what the input should be.
     *  This defaults to a one-by-one array containing a boolean true.
     */
    public Parameter correctValues;

    /** A double specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a DoubleToken, with default
     *  value 10<sup>-9</sup>.
     */
    public Parameter tolerance;

    /** If true, then do not check inputs, but rather collect them into
     *  the <i>correctValues</i> array.  This parameter is a boolean,
     *  and it defaults to false.
     */
    public Parameter trainingMode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>tolerance</i>, then check
     *  that it is increasing and nonnegative.
     *  @exception IllegalActionException If the indexes vector is not
     *  increasing and nonnegative, or the indexes is not a row vector.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == tolerance) {
            _tolerance = ((DoubleToken)(tolerance.getToken())).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to set the iteration counter to zero.
     *  @exception IllegalActionException If the base class throws it or
     *  if we are running under the test suite and the trainingMode
     *  parameter is set to true.
     *  @see #isRunningNightlyBuild()
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _numberOfInputTokensSeen = 0;
        _iteration = 0;
        _trainingTokens = null;
        if (((BooleanToken)trainingMode.getToken()).booleanValue()) {
            if (isRunningNightlyBuild()) {
                throw new IllegalActionException(this,
                        TRAINING_MODE_ERROR_MESSAGE);
            } else {
                System.err.println("Warning: '" + this.getFullName()
                        + "' is in training mode, set the trainingMode "
                        + "parameter to false before checking in");
            }
        }
    }

    /** If the trainingMode parameter is true and the
     *  model is being run as part of the test suite, then throw an exception.
     *  This method merely checks to see if
     *  "ptolemy.ptII.isRunningNightlyBuild" property exists and is not empty.
     *  To run the test suite in the Nightly Build mode, use
     *  <pre>
     *  make nightly
     *  </pre>
     */
    public static boolean isRunningNightlyBuild() {
        if (StringUtilities
                .getProperty("ptolemy.ptII.isRunningNightlyBuild")
                .length() > 0) {
            return true;
        }
        return false;
    }

    /** Read one token from each input channel and compare against
     *  the value specified in <i>correctValues</i>.  If the token count
     *  is larger than the length of <i>correctValues</i>, then return
     *  immediately, indicating that the inputs correctly matched
     *  the values in <i>correctValues</i> and that the test succeeded.
     *
     *  @exception IllegalActionException If an input does not match
     *   the required value or if the width of the input is not 1.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.getWidth() != 1) {
            throw new IllegalActionException(this,
                    "Width of input is " + input.getWidth()
                    + "but NonStrictTest only supports a width of 1.");
        }
        boolean training = ((BooleanToken)trainingMode.getToken())
            .booleanValue();
        if (training) {
            if (_trainingTokens == null) {
                _trainingTokens = new ArrayList();
            }
            if (input.hasToken(0)) {
                _trainingTokens.add(input.get(0));
            }
            return true;
        }
        if (_numberOfInputTokensSeen
                >= ((ArrayToken)(correctValues.getToken())).length()) {
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            if (input.hasToken(0)) {
                input.get(0);
            }
            return true;
        }

        Token referenceToken = ((ArrayToken)(correctValues.getToken()))
            .getElement(_numberOfInputTokensSeen);
        if (input.hasToken(0)) {
            Token token = input.get(0);
            _numberOfInputTokensSeen++;
            if (token.isCloseTo(referenceToken, _tolerance).booleanValue()
                    == false)
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _iteration
                        + ".\n"
                        + "Value was: " + token
                        + ". Should have been: " + referenceToken);

        }
        _iteration++;
        return true;
    }

    /** If <i>trainingMode</i> is <i>true</i>, then take the collected
     *  training tokens and store them as an array in <i>correctValues</i>.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        boolean training = ((BooleanToken)trainingMode.getToken())
            .booleanValue();
        // Note that wrapup() might get called by the manager before
        // we have any data...
        if (training && _trainingTokens != null &&
                _trainingTokens.size() > 0) {
            Object[] newValues = _trainingTokens.toArray();
            
            // NOTE: Support input multiport for the benefit of derived classes.
            int width = input.getWidth();
            Token[] newTokens = new Token[newValues.length];
            if (width == 1) {
                for (int i = 0; i < newValues.length; i++) {
                    newTokens[i] = (Token)newValues[i];
                }
            } else {
                for (int i = 0; i < newValues.length; i++) {
                    ArrayList entry = (ArrayList)newValues[i];
                    Object[] entries = entry.toArray();
                    Token[] newEntry = new Token[entries.length];
                    for (int j = 0; j < entries.length; j++) {
                        newEntry[j] = (Token)entries[j];
                    }
                    newTokens[i] = new ArrayToken(newEntry);
                }
            }
            correctValues.setToken(new ArrayToken(newTokens));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Exception message that is used if we are running under
     *  the nightly build and the trainingMode parameter is true.
     */
    public static String TRAINING_MODE_ERROR_MESSAGE =
    "Training Mode set for test actor and isRunningNightlyBuild()\n"
    + "  returned true, indicating that the\n"
    + "  ptolemy.ptII.isRunningNightlyBuild property is set.\n"
    + "  The trainingMode parameter should not be set in files\n"
    + "  that are checked into the nightly build!"
    + "  To run the tests in nightly build mode, use"
    + "     make nightly";
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Number of input tokens seen by this actor in the fire method.*/
    protected int _numberOfInputTokensSeen = 0;

    /** A double that is read from the <i>tolerance</i> parameter
     *        specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a double, with default
     *  value 10<sup>-9</sup>.
     */
    protected double _tolerance;

    /** Count of iterations. */
    protected int _iteration;

    /** List to store tokens for training mode. */
    protected List _trainingTokens;
}
