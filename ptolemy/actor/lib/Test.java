/* Check the input streams against a parameter value and outputs
 a boolean true if result is correct.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Test

/**

 <p>This actor compares the inputs against the value specified by the
 <i>correctValues</i> parameter.  That parameter is an ArrayToken,
 where each element of the array should have the same type as the
 input.  The length of this array is the number of iterations of this
 actor that are tested.  Subsequent iterations always succeed, so the
 actor can be used as a "power-up" test for a model, checking the first
 few iterations against some known results.
 </p><p>
 The input is a multiport.  If there is more than one channel connected
 to it, then each element of <i>correctValues</i> must itself be an
 ArrayToken, with length matching the number of channels.
 Suppose for example that the width of the input is one,
 and the first three inputs should be 1, 2, and 3.  Then you can
 set <i>correctValues</i> to</p>
 <pre>
 {1, 2, 3}
 </pre>
 <p>Suppose instead that the input has width two, and the correct values
 in the first iteration are 1 on the first channel and 2 on the second.
 Then on the second iteration, the correct values are 3 on the first
 channel and 4 on the second.  Then you can set <i>correctValues</i> to</p>
 <pre>
 {{1, 2}, {3, 4}}
 </pre>
 <p> With this setting, no tests are performed after the first two iterations
 of this actor.
 </p><p>
 The input values are checked in the fire() method, which checks to
 make sure that each input channel has a token.  If an input value is
 missing or differs from what it should be, then fire() throws an
 exception. Thus, the test passes if no exception is thrown.
 If you need to check the input value in postfire() (say, after
 a fixed-point iteration has converged), then use NonStrictTest.
 </p><p>
 If the input is a DoubleToken or ComplexToken,
 then the comparison passes if the value is close to what it should
 be, within the specified <i>tolerance</i> (which defaults to
 10<sup>-9</sup>.  The input data type is undeclared, so it can
 resolve to anything.
 </p><p>
 On each firing, this actor produces the output <i>false</i> until
 it reaches the end of the <i>correctValues</i> array, at which point
 it outputs <i>true</i>.  This can be fed, for example, to an instance
 of the Stop actor to stop the test upon successfully matching the
 test data. In training mode, the output is always false.
 </p>
 @see NonStrictTest
 @author Edward A. Lee, Christopher Hylands, Jim Armstrong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class Test extends NonStrictTest {
    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Test(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Note that the parent class (NonStrictTest) does not have a multiport
        // input port.
        input.setMultiport(true);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.BOOLEAN);
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
     *  does not match, then throw an exception. If the value
     *  matches, then output false if additional inputs are
     *  expected (to indicate that the test is not
     *  complete yet), and output true if this is the last expected
     *  input.
     *  <p>If the iteration count is larger than the length of
     *  <i>correctValues</i>, then output <i>true</i> and return,
     *  indicating that the test is complete, i.e. that all
     *  values in <i>correctValues</i> have been matched.</p>
     *  <p>If the <i>requireOrderedValues</i> parameter is false,
     *  then the input can match any as yet unmatched value
     *  in <i>correctValues</i>.
     *
     *  @exception IllegalActionException If an input is missing,
     *   or if its value does not match the required value.
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
            if (_debugging) {
                _debug("Debug mode is on.");
            }
            if (_trainingTokens == null) {
                _trainingTokens = new ArrayList();
            }

            if (width == 1) {
                if (input.hasToken(0)) {
                    Token token = input.get(0);
                    if (_debugging) {
                        _debug("-- Read training input: " + token);
                    }
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
                    Token token = input.get(i);
                    if (_debugging) {
                        _debug("-- Read training inputs: " + token);
                    }
                    arrayList.add(token);
                }
                _trainingTokens.add(arrayList);
            }
            output.broadcast(BooleanToken.FALSE);
            return;
        }

        // If we are past the end of the expected inputs, then read
        // and discard all inputs and output true
        if (_numberOfInputTokensSeen >= ((ArrayToken) correctValues.getToken())
                .length()) {
            if (_debugging) {
                _debug("Past the end of training data. Read and discard all inputs.");
            }
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            for (int i = 0; i < width; i++) {
                if (input.hasToken(i)) {
                    input.get(i);
                }
            }

            // Indicate that the test has passed if the output is connected.
            output.broadcast(BooleanToken.TRUE);
            return;
        }

        Token[] reference = null;
        // _matchedValues[x] is true if the x'th element of
        // correctValues has been used.  _matchedValues is shared across
        // multiple firings.

        // possibleMatches is a list of indexes to possible
        // correctValues for this firing.  Each element of the list is
        // an index into the correctValues parameter that refers to a
        // possibly correct value that has matched all the inputs thus
        // far.
        List<Integer> possibleMatches = null;

        if (((BooleanToken) requireOrderedValues.getToken()).booleanValue()) {
            reference = _getReference(_numberOfInputTokensSeen, width);
        } else {
            // While handling multiport inputs, we use the possibleMatches
            // list to keep track of which of the elements in
            // correctValues are possible correct values for this
            // firing.

            possibleMatches = new LinkedList();
            for (int j = 0; j < _matchedValues.length; j++) {
                if (!_matchedValues[j]) {
                    possibleMatches.add(j);
                }
            }
        }

        for (int i = 0; i < width; i++) {
            if (!input.hasToken(i)) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _numberOfInputTokensSeen
                        + ".\n" + "Empty input on channel " + i);
            }

            Token token = input.get(i);

            if (((BooleanToken) requireOrderedValues.getToken()).booleanValue()) {
                if (_debugging) {
                    _debug("-- Read input: " + token
                            + ", which is expected to match: " + reference[i]);
                }

                if (!_isClose(token, reference[i], _tolerance)) {
                    throw new IllegalActionException(this,
                            "Test fails in iteration " + _numberOfInputTokensSeen
                            + ".\n" + "Value was: " + token
                            + ". Should have been: " + reference[i]);
                }
            } else {
                // requireCorrectOrder is false.

                // Loop through all the as yet unmatched correct values.
                //
                // If a channel does not match, then remove it from the
                // list of possible matches
                //
                // If the value on the channel does not match any of
                // the possible correct values, then throw an
                // exception.  If all the channels match an as yet
                // unmatched correct value, then mark that correct
                // value as matched.
                boolean sawMatch = false;
                Iterator<Integer> possibles = possibleMatches.iterator();
                while (possibles.hasNext()) {
                    int possibleIndex = possibles.next().intValue();
                    try {
                        reference = _getReference(possibleIndex, width);
                        if (_debugging) {
                            _debug("Test: token #: " + _numberOfInputTokensSeen
                                    + " channel: " + i
                                    + " possible: " + possibleIndex
                                    + " token: " + token
                                    + "ref[" + i + "]: " + reference[i]);
                        }

                        if (_isClose(token, reference[i], _tolerance)) {
                            if (!sawMatch) {
                                sawMatch = true;
                                _matchedValues[possibleIndex] = true;
                            }
                        } else {
                            if (_debugging) {
                                _debug("Test: removing " + possibleIndex);
                            }
                            possibles.remove();
                        }
                    } catch (IllegalActionException ex) {
                        // Chain the exceptions together so we know which test
                        // actor failed if there was more than one...
                        throw new IllegalActionException(this, ex,
                            "Test fails in iteration "
                                + _numberOfInputTokensSeen
                                + ".\n" + "Value was: " + token);

                    }
                }
                if (!sawMatch) {
                    throw new IllegalActionException(this,
                            "Test fails in iteration " + _iteration + ".\n"
                            + "Value was: " + token
                            + ". No matches were found in any of "
                            + "the as yet unmatched correct values.");
                }
            }
        }

        _numberOfInputTokensSeen++;

        if (output.numberOfSinks() > 0) {
            if (_numberOfInputTokensSeen >= ((ArrayToken) correctValues
                    .getToken()).length()) {
                // Seen all expected inputs.
                output.send(0, BooleanToken.TRUE);
            } else {
                // More inputs expected.
                output.send(0, BooleanToken.FALSE);
            }
        }
    }

    /** Override the base class to do nothing and return true.
     *  @return True.
     */
    @Override
    public boolean postfire() {
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the reference from the correctValues parameter.
     *  @param index The index of the element in correctValues to get.
     *  @param width The width of the input port
     *  @return an array of tokens.
     */
    private Token[] _getReference(int index, int width)
            throws IllegalActionException {
        Token[] reference;
        Token referenceToken = ((ArrayToken) correctValues.getToken())
            .getElement(index);

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
        return reference;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    // /** Set the input port greater than or equal to the type of elements
    //  *  in the correctValues array in case backward type inference is
    //  *  enabled and the input port has no type declared.
    //  *
    //  *  @return A set of inequalities.
    //  */
    // @Override
    // protected Set<Inequality> _customTypeConstraints() {
    //     HashSet<Inequality> result = new HashSet<Inequality>();
    //     if (isBackwardTypeInferenceEnabled()
    //             && input.getTypeTerm().isSettable()) {
    //         try {
    //             TypeConstant typeConstant = null;
    //             if (((BooleanToken) trainingMode.getToken())
    //                     .booleanValue()) {
    //                 typeConstant = new TypeConstant(BaseType.GENERAL);
    //             } else {
    //                 ArrayToken correctValuesToken = (ArrayToken) correctValues.getToken();
    //                 if (input.getWidth() == 1) {
    //                 // result.add(
    //                 //         new Inequality(
    //                 //                 new TypeConstant(
    //                 //                         ((ArrayToken) correctValues.getToken())
    //                 //                         .getElementType()),
    //                 //                 input.getTypeTerm()));

    //                 typeConstant = new TypeConstant(
    //                         correctValuesToken.getElementType());
    //                 } else {
    //                     typeConstant = new TypeConstant(
    //                             ((ArrayToken) correctValuesToken.getElement(0)).getElementType());
    //                 }
    //             }
    //             result.add( new Inequality(typeConstant,
    //                             input.getTypeTerm()));
    //         } catch (IllegalActionException ex) {
    //             throw new InternalErrorException(this, ex, "Failed to set the type constraint.");
    //         }
    //     }
    //     return result;
    // }

}
