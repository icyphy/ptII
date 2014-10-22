/* A publisher that transparently tunnels messages to subscribers and saves its output for testing

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.parameters.SharedParameter;
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
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// PublisherNonStrictTest

/**
 This actor publishes input tokens on a named channel and compares
 the inputs against the value specified by the <i>correctValues</i> parameter.

 <p>This actor combines the {@link ptolemy.actor.lib.Publisher} actor
 and the {@link ptolemy.actor.lib.NonStrictTest} actor.  Thus, it has quite
 a bit of duplicated code from the NonStrictTest actor.

 <p> Note that in the superclass (Publisher), the input is a multiport,
 whereas in this class it is a regular non-multiport.  To use a multiport
 input, use {@link ptolemy.actor.lib.PublisherTest}

 @author Christopher Brooks, based on Test, which has Edward A. Lee and Jim Armstrong as authors
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PublisherNonStrictTest extends Publisher {

    /** Construct a publisher with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PublisherNonStrictTest(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        correctValues = new Parameter(this, "correctValues");
        correctValues.setExpression("{true}");
        correctValues.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);

        tolerance = new Parameter(this, "tolerance");
        tolerance.setExpression("1.0");
        tolerance.setTypeEquals(BaseType.DOUBLE);

        trainingMode = new SharedParameter(this, "trainingMode", getClass(),
                "false");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);

        // Note that in Publisher, the input is a multiport.
        input.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** A matrix specifying what the input should be.
     *  This defaults to a one-by-one array containing a boolean true.
     */
    public Parameter correctValues;

    /** A double specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a DoubleToken, with default
     *  value 10<sup>-9</sup>.  During training, if a correct value is
     *  greater than 10 orders of magnitude than the tolerance, then the
     *  tolerance is changed to a value 9 orders of magnitude less than
     *  the correct value.  This helps avoid comparisons beyond the
     *  precision of a Java double.
     */
    public Parameter tolerance;

    /** If true, then do not check inputs, but rather collect them into
     *  the <i>correctValues</i> array.  This parameter is a boolean,
     *  and it defaults to false. It is a shared parameter, meaning
     *  that changing it for any one instance in a model will change
     *  it for all instances in the model.
     */
    public SharedParameter trainingMode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is the channel, increment the workspace version
     *  to force cached receiver lists to be updated, and invalidate
     *  the schedule and resolved types of the director, if there is one.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == tolerance) {
            _tolerance = ((DoubleToken) tolerance.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read at most one input token from each
     *  input channel and send it to the subscribers,
     *  if any.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        // We don't call the super class because we want to
        // have token production occur in postfire.

        //super.fire();
        if (_debugging) {
            _debug("Called fire()");
        }
        _firedOnce = true;

        //int width = input.getWidth();

        //         // If we are in training mode, read the inputs and add to the
        //         // training data.
        //         boolean training = ((BooleanToken) trainingMode.getToken())
        //                 .booleanValue();

        //         if (training) {
        //             if (_trainingTokens == null) {
        //                 _trainingTokens = new ArrayList();
        //             }

        //             if (width == 1) {
        //                 if (input.hasToken(0)) {
        //                     Token token = input.get(0);
        //                     output.send(0, token);
        //                     if (token instanceof ArrayToken) {
        //                         Token[] innerArrayToken = new Token[1];
        //                         innerArrayToken[0] = token;
        //                         _trainingTokens.add(innerArrayToken);
        //                     } else {
        //                         _trainingTokens.add(token);
        //                     }
        //                 }
        //             } else {
        //                 ArrayList arrayList = new ArrayList();

        //                 for (int i = 0; i < width; i++) {
        //                     if (input.hasToken(i)) {
        //                         Token token = input.get(i);
        //                         arrayList.add(token);
        //                         output.send(i, token);
        //                     }
        //                 }

        //                 _trainingTokens.add(arrayList);
        //             }

        //             return;
        //         }

        //         for (int i = 0; i < width; i++) {
        //             if (input.hasToken(i)) {
        //                 Token token = input.get(i);
        //                 output.send(i, token);
        //             }
        //         }
    }

    /** Override the base class to set the iteration counter to zero.
     *  @exception IllegalActionException If the base class throws it or
     *  if we are running under the test suite and the trainingMode
     *  parameter is set to true.
     *  @see ptolemy.util.MessageHandler#isRunningNightlyBuild()
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _numberOfInputTokensSeen = 0;
        _iteration = 0;
        _trainingTokens = null;
        _firedOnce = false;
        _initialized = true;

        if (((BooleanToken) trainingMode.getToken()).booleanValue()) {
            if (MessageHandler.isRunningNightlyBuild()) {
                throw new IllegalActionException(this,
                        NonStrictTest.TRAINING_MODE_ERROR_MESSAGE);
            } else {
                System.err.println("Warning: '" + getFullName()
                        + "' is in training mode, set the trainingMode "
                        + "parameter to false before checking in");
            }
        }
    }

    /** Override the base class to ensure that links to subscribers
     *  have been updated.
     *  @exception IllegalActionException If there is already a publisher
     *   publishing on the same channel.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
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
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean superReturnValue = super.postfire();
        if (input.getWidth() != 1) {
            throw new IllegalActionException(this, "Width of input is "
                    + input.getWidth()
                    + " but PublisherNonStrictTest only supports a width of 1.");
        }

        boolean training = ((BooleanToken) trainingMode.getToken())
                .booleanValue();

        if (training) {
            if (_trainingTokens == null) {
                _trainingTokens = new ArrayList();
            }

            if (input.hasToken(0)) {
                Token token = input.get(0);
                _trainingTokens.add(token);
                output.send(0, token);
            }

            return true && superReturnValue;
        }

        //          if (_numberOfInputTokensSeen >= ((ArrayToken) (correctValues.getToken()))
        //                  .length()) {
        //              // Consume and discard input values.  We are beyond the end
        //              // of the correctValues array.
        //              //if (input.hasToken(0)) {
        //              //    input.get(0);

        //              //}
        //              return true;
        //          }

        Token referenceToken = ((ArrayToken) correctValues.getToken())
                .getElement(_numberOfInputTokensSeen);

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);

                //if (input.hasToken(0)) {
                //Token token = input.get(0);
                // output.send(0, token);
                _numberOfInputTokensSeen++;

                // FIXME: If we get a nil token on the input, what should we do?
                // Here, we require that the referenceToken also be nil.
                // If the token is an ArrayToken and two corresponding elements
                // are nil, then we consider them "close".
                if (token.isCloseTo(referenceToken, _tolerance).booleanValue() == false
                        && !referenceToken.isNil()
                        && !NonStrictTest._isCloseToIfNilArrayElement(token,
                                referenceToken, _tolerance)
                        && !NonStrictTest._isCloseToIfNilRecordElement(token,
                                referenceToken, _tolerance)) {
                    throw new IllegalActionException(this,
                            "Test fails in iteration " + _iteration + ".\n"
                                    + "Value was: " + token
                                    + ". Should have been: " + referenceToken);
                }
                output.send(i, token);
            }
        }

        _iteration++;
        return true && superReturnValue;
    }

    /** If <i>trainingMode</i> is <i>true</i>, then take the collected
     *  training tokens and store them as an array in <i>correctValues</i>.
     *  @exception IllegalActionException If initialized() was called
     *  and fire() was not called or if the number of inputs tokens seen
     *  is not greater than or equal to the number of elements in the
     *  <i>correctValues</i> array.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        boolean training = ((BooleanToken) trainingMode.getToken())
                .booleanValue();

        if (!training && _initialized) {
            if (!_firedOnce) {
                String errorMessage = "The fire() method of this actor was never called. "
                        + "Usually, this is an error indicating that "
                        + "starvation is occurring.";
                String fireCompatProperty = "ptolemy.actor.lib.NonStrictTest.fire.compat";

                if (StringUtilities.getProperty(fireCompatProperty).length() > 0) {
                    System.err.println("Warning: '" + getFullName() + "' "
                            + errorMessage
                            + "\nThis error is being ignored because " + "the "
                            + fireCompatProperty + "property was set.");
                } else {
                    throw new IllegalActionException(this, errorMessage);
                }
            }

            if (_numberOfInputTokensSeen < ((ArrayToken) correctValues
                    .getToken()).length()) {
                String errorMessage = "The test produced only "
                        + _numberOfInputTokensSeen
                        + " tokens, yet the correctValues parameter was "
                        + "expecting "
                        + ((ArrayToken) correctValues.getToken()).length()
                        + " tokens.";

                System.err.println("Warning: '" + getFullName() + "' "
                        + errorMessage);
            }
        }

        _initialized = false;

        // Note that wrapup() might get called by the manager before
        // we have any data...
        if (training && _trainingTokens != null && _trainingTokens.size() > 0) {
            Object[] newValues = _trainingTokens.toArray();

            // NOTE: Support input multiport for the benefit of derived classes.
            int width = input.getWidth();
            Token[] newTokens = new Token[newValues.length];

            if (width == 1) {
                for (int i = 0; i < newValues.length; i++) {
                    if (newValues[i] instanceof Token[]) {
                        // Handle width of 1, ArrayToken
                        newTokens[i] = new ArrayToken((Token[]) newValues[i]);
                        for (int j = 0; j < ((Token[]) newValues[i]).length; i++) {
                            _checkRangeOfTolerance(((Token[]) newValues[i])[j]);
                        }
                    } else {
                        newTokens[i] = (Token) newValues[i];
                        _checkRangeOfTolerance((Token) newValues[i]);
                    }
                }
            } else {
                for (int i = 0; i < newValues.length; i++) {
                    ArrayList entry = (ArrayList) newValues[i];

                    // Entry may be an empty array, in which case,
                    // we cannot do the update, so we return.
                    if (entry.size() < 1) {
                        System.err.println("Warning: '" + getFullName()
                                + "': Unable to train. "
                                + "Zero tokens received in iteration " + i);
                        return;
                    }

                    Object[] entries = entry.toArray();
                    Token[] newEntry = new Token[entries.length];

                    for (int j = 0; j < entries.length; j++) {
                        newEntry[j] = (Token) entries[j];
                        _checkRangeOfTolerance(newEntry[i]);
                    }

                    newTokens[i] = new ArrayToken(newEntry);
                }
            }

            correctValues.setToken(new ArrayToken(newTokens));
            correctValues.setPersistent(true);
        }

        if (training
                && (_trainingTokens == null || _trainingTokens.size() == 0)) {
            System.err.println("Warning: '" + getFullName()
                    + "' The test produced 0 tokens.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Set to true if fire() is called once.  If fire() is not called at
     *  least once, then throw an exception in wrapup().
     */
    protected boolean _firedOnce = false;

    /** Set to true when initialized() is called.
     */
    protected boolean _initialized = false;

    /** Count of iterations. */
    protected int _iteration;

    /** Number of input tokens seen by this actor in the fire method.*/
    protected int _numberOfInputTokensSeen = 0;

    /** A double that is read from the <i>tolerance</i> parameter
     *        specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a double, with default
     *  value 10<sup>-9</sup>.
     */
    protected double _tolerance;

    /** List to store tokens for training mode. */
    protected List _trainingTokens;

    /** Check that the difference in exponents between the
     *  input and the tolerance is not greater than the precision
     *  of a Double.  If the exponent of newValue parameter is
     *  different by from the exponent of the <i>tolerance</i>
     *  parameter by more than 10, then adjust the <i>tolerance</i>
     *  parameter.  This is useful for training large modesl
     *  that have many PublisherTests.
     *  @param newValue The token to be tested.  DoubleTokens
     *  are tested, other tokens are ignored.
     *  @exception IllegalActionException If thrown while reading the
     *  <i>tolerance</i> parameter.
     */
    private void _checkRangeOfTolerance(Token newValue)
            throws IllegalActionException {
        if (newValue instanceof DoubleToken) {
            Double value = ((DoubleToken) newValue).doubleValue();
            if (value == 0.0) {
                // The exponent of 0.0 is -Infinity, so skip it
                return;
            }
            double log = Math.log10(((DoubleToken) newValue).doubleValue());
            if (Math.abs(log - Math.log10(_tolerance)) > 10) {
                // Set the tolerance to something closer to the input so that
                // we don't set it many times.
                double newTolerance = Math.pow(10, log - 9);
                if (newTolerance > _tolerance) {
                    // Only set the tolerance if it is greater than the old tolerance.
                    tolerance.setToken(new DoubleToken(newTolerance));
                    tolerance.setPersistent(true);
                    attributeChanged(tolerance);
                    System.out
                            .println("PublisherNonStrictTest: "
                                    + getFullName()
                                    + ": exponent of "
                                    + newValue
                                    + " is "
                                    + log
                                    + ", which cannot be compared with the previous tolerance."
                                    + " The new tolerance is "
                                    + tolerance.getExpression() + ".");
                }
            }
        }
    }
}
