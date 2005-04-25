/* Check the input streams against a parameter value and outputs
   a boolean true if result is correct.

   Copyright (c) 2004-2005 The Regents of the University of California.
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

   review output port.
*/
package ptolemy.copernicus.java.test;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Test;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.ArrayList;


//////////////////////////////////////////////////////////////////////////
//// CGTest

/**
   NonStrictTest actor suitable for use with Copernicus.

   This actor differs from actor.lib.NonStrictTest in that
   the trainingMode parameter is a Parameter, not a SharedParameter.

   @see Test
   @author Christopher Brooks
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Redw (cxh)
*/
public class CGTest extends CGNonStrictTest {
    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CGTest(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // These liens are copied from T
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);
        input.setMultiport(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports                             ////

    /** Boolean output that is false as long as there is data to
     *  compare against the input, but becomes true on the first
     *  firing after such data has been exhausted.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input channel and compare against
     *  the value specified in <i>correctValues</i>. If the value
     *  matches, then output false (to indicate that the test is not
     *  complete yet) and return.  Otherwise, throw an exception.
     *  If the iteration count is larger than the length of
     *  <i>correctValues</i>, then output <i>true</i> and return,
     *  indicating that the test is complete, i.e. that all
     *  values in <i>correctValues</i> have been matched.
     *
     *  @exception IllegalActionException If an input is missing,
     *   or if its value does not match the required value.
     */
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

            if (width == 1) {
                if (input.hasToken(0)) {
                    _trainingTokens.add(input.get(0));
                }
            } else {
                ArrayList arrayList = new ArrayList();
                _trainingTokens.add(arrayList);

                for (int i = 0; i < width; i++) {
                    arrayList.add(input.get(i));
                }
            }

            return;
        }

        if (_numberOfInputTokensSeen >= ((ArrayToken) (correctValues.getToken()))
                .length()) {
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            for (int i = 0; i < width; i++) {
                if (input.hasToken(i)) {
                    input.get(i);
                }
            }

            // Indicate that the test has passed.
            output.send(0, new BooleanToken(true));
            return;
        }

        output.send(0, new BooleanToken(false));

        Token referenceToken = ((ArrayToken) (correctValues.getToken()))
            .getElement(_numberOfInputTokensSeen);
        Token[] reference;

        if ((width == 1) && !(referenceToken instanceof ArrayToken)) {
            reference = new Token[1];
            reference[0] = referenceToken;
        } else {
            try {
                reference = ((ArrayToken) referenceToken).arrayValue();
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Width of input is " + width
                        + ", but correctValues parameter " + "is not an array "
                        + "of arrays.");
            }

            if (width != reference.length) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Width of input is " + width
                        + ", which does not match " + "the  width of the "
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
                    .booleanValue();
            } catch (IllegalActionException ex) {
                // Chain the exceptions together so we know which test
                // actor failed if there was more than one...
                throw new IllegalActionException(this, ex,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Value was: " + token + ". Should have been: "
                        + reference[i]);
            }

            if (!isClose) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Value was: " + token + ". Should have been: "
                        + reference[i]);
            }
        }

        _numberOfInputTokensSeen++;
    }

    /** Override the base class to do nothing and return true.
     *  @return True.
     */
    public boolean postfire() {
        return true;
    }
}
