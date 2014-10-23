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

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// PublisherTest

/**
 This actor publishes input tokens on a named channel and compares
 the inputs against the value specified by the <i>correctValues</i> parameter.

 <p>This actor combines the {@link ptolemy.actor.lib.Publisher} actor
 and the {@link ptolemy.actor.lib.Test} actor.  Thus, it has quite
 a bit of duplicated code from the Test actor.

 @author Christopher Brooks, based on Test, which has Edward A. Lee and Jim Armstrong as authors
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class PublisherTest extends PublisherNonStrictTest {

    /** Construct a publisher with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PublisherTest(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Note that the parent class (PublisherNonStrictTest) does
        // not have a multiport input port.
        input.setMultiport(true);
    }

    /** Read at most one input token from each
     *  input channel and send it to the subscribers,
     *  if any.
     *  Read one token from each input channel and compare against
     *  the value specified in <i>correctValues</i>. If the value
     *  matches, then output false (to indicate that the test is not
     *  complete yet) and return.  Otherwise, throw an exception.
     *  If the iteration count is larger than the length of
     *  <i>correctValues</i>, then output <i>true</i> and return,
     *  indicating that the test is complete, i.e. that all
     *  values in <i>correctValues</i> have been matched.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int width = input.getWidth();

        // If we are in training mode, read the inputs and add to the
        // training data.
        boolean training = ((BooleanToken) trainingMode.getToken())
                .booleanValue();

        if (training) {
            if (_trainingTokens == null) {
                _trainingTokens = new ArrayList();
            }

            if (_debugging) {
                _debug("PublisherTest: width: " + width + " "
                        + input.hasToken(0));
            }
            if (width == 1) {
                if (input.hasToken(0)) {
                    Token token = input.get(0);
                    output.send(0, token);
                    if (token instanceof ArrayToken) {
                        Token[] innerArrayToken = new Token[1];
                        innerArrayToken[0] = token;
                        _trainingTokens.add(innerArrayToken);
                    } else {
                        _trainingTokens.add(token);
                    }
                }
            } else {
                ArrayList arrayList = new ArrayList();

                for (int i = 0; i < width; i++) {
                    if (input.hasToken(i)) {
                        Token token = input.get(i);
                        arrayList.add(token);
                        output.send(i, token);
                    }
                }

                _trainingTokens.add(arrayList);
            }

            return;
        }

        if (_numberOfInputTokensSeen >= ((ArrayToken) correctValues.getToken())
                .length()) {
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            for (int i = 0; i < width; i++) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);
                    output.send(i, token);
                }
            }

            // Indicate that the test has passed.
            output.send(0, new BooleanToken(true));
            return;
        }

        Token referenceToken = ((ArrayToken) correctValues.getToken())
                .getElement(_numberOfInputTokensSeen);
        Token[] reference;

        if (width == 1 && !(referenceToken instanceof ArrayToken)) {
            reference = new Token[1];
            reference[0] = referenceToken;
        } else {
            try {
                reference = ((ArrayToken) referenceToken).arrayValue();
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Width of input is " + width
                        + ", but correctValues parameter "
                        + "is not an array " + "of arrays.");
            }

            if (width != reference.length) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Width of input is " + width
                        + ", which does not match "
                        + "the  width of the "
                        + _numberOfInputTokensSeen + "-th element of"
                        + " correctValues, " + reference.length);
            }
        }

        for (int i = 0; i < width; i++) {
            if (!input.hasToken(i)) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Empty input on channel " + i);
            }

            Token token = input.get(i);

            boolean isClose;

            try {
                isClose = token.isCloseTo(reference[i], _tolerance)
                        .booleanValue()
                        || token.isNil()
                        && reference[i].isNil();
                // Additional guards makes things slightly easier for
                // Copernicus.
                if (token instanceof ArrayToken
                        && reference[i] instanceof ArrayToken) {
                    isClose |= NonStrictTest._isCloseToIfNilArrayElement(token,
                            reference[i], _tolerance);
                }
                if (token instanceof RecordToken
                        && reference[i] instanceof RecordToken) {
                    isClose |= NonStrictTest._isCloseToIfNilRecordElement(
                            token, reference[i], _tolerance);
                }

            } catch (IllegalActionException ex) {
                // Chain the exceptions together so we know which test
                // actor failed if there was more than one...
                throw new IllegalActionException(this, ex,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Value was: " + token
                        + ". Should have been: " + reference[i]);
            }

            if (!isClose) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Value was: " + token
                        + ". Should have been: " + reference[i]);
            }
            output.send(i, token);
        }

        _numberOfInputTokensSeen++;
    }

    /** Override the base class to do nothing and return true.
     *  @return True.
     */
    @Override
    public boolean postfire() {
        return true;
    }
}
