/* Check the input streams against a parameter value, ignoring absent values.

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

 */
package ptolemy.actor.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// NonStrictTest

/**

 <p>This actor compares the inputs against the value specified by the
 <i>correctValues</i> parameter.  That parameter is an ArrayToken,
 where each element of the array is of the same type as the input.
 On each firing where the input is present, the value of the input
 is compared against the next token in the <i>correctValues</i>
 parameter.  If it matches, the firing succeeds. If it doesn't
 match, then an exception is thrown. After matching each of
 the value in the <i>correctValues</i> parameter, subsequent iterations
 always succeed, so the actor can be used as a "power-up" test for a model,
 checking the first few iterations against some known results.</p>
 <p>
 Unlike the Test actor, NonStrictTest does not support a multiport
 input, only single port inputs are supported.  This also differs
 from Test in that it ignores absent inputs, and it checks the inputs
 in the postfire() method rather than the fire() method.</p>
 <p>
 This actor accepts any type of data on its input port, therefore it
 doesn't declare a type, but lets the type resolution algorithm find
 the least fixed point. If backward type inference is enabled, and
 no input type has been declared, the input is constrained to be
 equal to <code>BaseType.GENERAL</code>. This will result in upstream
 ports resolving to the most general type rather than the most specific.
 </p><p>
  If the input is a DoubleToken or ComplexToken, then the comparison
 passes if the value is close to what it should be, within the
 specified <i>tolerance</i> (which defaults to 10<sup>-9</sup>).
 During training, if a correct value is
 greater than 10 orders of magnitude than the tolerance, then the
 tolerance is changed to a value 9 orders of magnitude less than
 the correct value.  This helps avoid comparisons beyond the
 precision of a Java double.</p>
 <p>
 If the parameter <i>trainingMode</i> is <i>true</i>, then instead
 of performing the test, this actor collects the inputs into the
 <i>correctValues</i> parameter.  Thus, to use this actor, you can
 place it in a model, set <i>trainingMode</i> to <i>true</i> to
 collect the reference data, then set <i>trainingMode</i> to
 <i>false</i>.  Any subsequent run of the actor will throw an
 exception if the input data does not match the training data.
 The value of the reference token is set in the wrapup() method.
 The <i>trainingMode</i> parameter is a shared parameter,
 meaning that if you change it for any one instance of this
 actor in the model, then it will be changed for all instances.</p>

 @see Test
 @author Paul Whitaker, Christopher Hylands, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
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
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        correctValues = new Parameter(this, "correctValues");
        correctValues.setExpression("{true}");
        correctValues.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);

        tolerance = new Parameter(this, "tolerance");
        tolerance.setExpression("1.0E-9");
        tolerance.setTypeEquals(BaseType.DOUBLE);

        requireAllCorrectValues = new SharedParameter(this,
                "requireAllCorrectValues", getClass(), "true");
        requireAllCorrectValues.setTypeEquals(BaseType.BOOLEAN);

        trainingMode = new SharedParameter(this, "trainingMode", getClass(),
                "false");
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
     *  value 10<sup>-9</sup>.  During training, if a correct value is
     *  greater than 10 orders of magnitude than the tolerance, then the
     *  tolerance is changed to a value 9 orders of magnitude less than
     *  the correct value.  This helps avoid comparisons beyond the
     *  precision of a Java double.
     */
    public Parameter tolerance;

    /** If true, and the number of tokens seen in wrapup() is not
     *  equal to or greater than the number of elements in the
     *  <i>correctValues</i> array, then throw an exception.  The
     *  default value is true. This parameter is a shared parameter,
     *  meaning that changing it for any one instance in a model will
     *  change it for all instances in the model.
     */
    public Parameter requireAllCorrectValues;

    /** If true, then do not check inputs, but rather collect them into
     *  the <i>correctValues</i> array.  This parameter is a boolean,
     *  and it defaults to false. It is a shared parameter, meaning
     *  that changing it for any one instance in a model will change
     *  it for all instances in the model.
     */
    public SharedParameter trainingMode;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ArrayAverage.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        NonStrictTest newObject = (NonStrictTest) super.clone(workspace);
        newObject.correctValues.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        return newObject;
    }

    /** If the attribute being changed is <i>tolerance</i>, then check
     *  that it is increasing and nonnegative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the indexes vector is not
     *  increasing and nonnegative, or the indexes is not a row vector.
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

    /** Call super.fire() and set _firedOnce to true.
     *  Derived classes should either call this fire() method
     *  or else set _firedOnce to true.
     *  @see #_firedOnce
     *  @exception IllegalActionException If thrown by the baseclass.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _firedOnce = true;
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
                        TRAINING_MODE_ERROR_MESSAGE);
            } else {
                System.err.println("Warning: '" + getFullName()
                        + "' is in training mode, set the trainingMode "
                        + "parameter to false before checking in");
            }
        }
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
        if (!super.postfire()) {
            return false;
        }
        if (input.getWidth() != 1) {
            throw new IllegalActionException(this, "Width of input is "
                    + input.getWidth()
                    + " but NonStrictTest only supports a width of 1.");
        }

        boolean training = ((BooleanToken) trainingMode.getToken())
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

        if (_numberOfInputTokensSeen >= ((ArrayToken) correctValues.getToken())
                .length()) {
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            if (input.hasToken(0)) {
                input.get(0);
            }

            return true;
        }

        Token referenceToken = ((ArrayToken) correctValues.getToken())
                .getElement(_numberOfInputTokensSeen);

        if (input.hasToken(0)) {
            Token token = input.get(0);
            _numberOfInputTokensSeen++;

            // FIXME: If we get a nil token on the input, what should we do?
            // Here, we require that the referenceToken also be nil.
            // If the token is an ArrayToken and two corresponding elements
            // are nil, then we consider them "close".
            if (token.isCloseTo(referenceToken, _tolerance).booleanValue() == false
                    && !referenceToken.isNil()
                    && !_isCloseToIfNilArrayElement(token, referenceToken,
                            _tolerance)
                            && !_isCloseToIfNilRecordElement(token, referenceToken,
                                    _tolerance)) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _iteration + ".\n"
                                + "Value was: " + token
                                + ". Should have been: " + referenceToken);
            }
        }

        _iteration++;
        return true;
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
                    _initialized = false;
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
                if (((BooleanToken) requireAllCorrectValues.getToken())
                        .booleanValue()) {
                    _initialized = false;
                    // FIXME: this produce a dialog for each failed test.
                    throw new IllegalActionException(this, errorMessage);
                }
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
                        for (int j = 0; j < ((Token[]) newValues[i]).length; j++) {
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
                        _checkRangeOfTolerance(newEntry[j]);
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
            // If we get no data and we are training, set the expression
            // to the empty string.

            // Copernicus: Don't use setExpression() here, use setToken(NIL)
            //correctValues.setExpression("{}");
            correctValues.setToken(ArrayToken.NIL);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Exception message that is used if we are running under
     *  the nightly build and the trainingMode parameter is true.
     */
    public static final String TRAINING_MODE_ERROR_MESSAGE = "Training Mode set for test actor and isRunningNightlyBuild()\n"
            + "  returned true, indicating that the\n"
            + "  ptolemy.ptII.isRunningNightlyBuild property is set.\n"
            + "  The trainingMode parameter should not be set in files\n"
            + "  that are checked into the nightly build!"
            + "  To run the tests in nightly build mode, use"
            + "     make nightly";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
    protected void _checkRangeOfTolerance(Token newValue)
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
                    .println("NonStrictTest: "
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

    /** Set the input port to be greater than or equal to
     *  <code>BaseType.GENERAL</code> in case backward type inference is
     *  enabled and the input port has no type declared.
     *
     *  @return A set of inequalities.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        HashSet<Inequality> result = new HashSet<Inequality>();
        if (isBackwardTypeInferenceEnabled()
                && input.getTypeTerm().isSettable()) {
            result.add(new Inequality(new TypeConstant(BaseType.GENERAL), input
                    .getTypeTerm()));
        }
        return result;
    }

    /** Test whether the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. It is assumed that the argument is
     *  an ArrayToken, and the isCloseTo() method of the array elements
     *  is used.
     *  This method differs from
     *  {@link ptolemy.data.ArrayToken#_isCloseTo(Token, double)}
     *  in that if corresponding elements are both nil tokens, then
     *  those two elements are considered "close", see
     *  {@link ptolemy.data.Token#NIL}.
     *  @param token1 The first array token to compare.
     *  @param token2 The second array token to compare.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @exception IllegalActionException If the elements do not support
     *   this comparison.
     *  @return True if the first argument is close
     *  to this token.  False if the arguments are not ArrayTokens
     */
    protected static boolean _isCloseToIfNilArrayElement(Token token1,
            Token token2, double epsilon) throws IllegalActionException {
        if (!(token1 instanceof ArrayToken) || !(token2 instanceof ArrayToken)) {
            return false;
        }

        ArrayToken array1 = (ArrayToken) token1;
        ArrayToken array2 = (ArrayToken) token2;
        if (array1.length() != array2.length()) {
            return false;
        }

        for (int i = 0; i < array1.length(); i++) {
            // Here is where isCloseTo() differs from isEqualTo().
            // Note that we return false the first time we hit an
            // element token that is not close to our current element token.
            BooleanToken result = array1.getElement(i).isCloseTo(
                    array2.getElement(i), epsilon);

            // If the tokens are not close and array1[i] and is not nil, then
            // the arrays really aren't close.
            if (result.booleanValue() == false) {
                if (array1.getElement(i).isNil()
                        && array2.getElement(i).isNil()) {
                    // They are not close, but both are nil, so for
                    // our purposes, the are close.
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /** Test whether the value of this token is close to the first argument,
     *  where "close" means that the distance between them is less than
     *  or equal to the second argument.  This method only makes sense
     *  for tokens where the distance between them is reasonably
     *  represented as a double. It is assumed that the argument is
     *  a Record, and the isCloseTo() method of the record elements
     *  is used.
     *  This method differs from
     *  {@link ptolemy.data.RecordToken#_isCloseTo(Token, double)}
     *  in that if corresponding elements are both nil tokens, then
     *  those two elements are considered "close", see
     *  {@link ptolemy.data.Token#NIL}.
     *  @param token1 The first array token to compare.
     *  @param token2 The second array token to compare.
     *  @param epsilon The value that we use to determine whether two
     *   tokens are close.
     *  @exception IllegalActionException If the elements do not support
     *   this comparison.
     *  @return True if the first argument is close
     *  to this token.  False if the arguments are not ArrayTokens
     */
    protected static boolean _isCloseToIfNilRecordElement(Token token1,
            Token token2, double epsilon) throws IllegalActionException {
        if (!(token1 instanceof RecordToken)
                || !(token2 instanceof RecordToken)) {
            return false;
        }
        RecordToken record1 = (RecordToken) token1;
        RecordToken record2 = (RecordToken) token2;

        Set myLabelSet = record1.labelSet();
        Set argLabelSet = record2.labelSet();

        if (!myLabelSet.equals(argLabelSet)) {
            return false;
        }

        // Loop through all of the fields, checking each one for closeness.
        Iterator iterator = myLabelSet.iterator();

        while (iterator.hasNext()) {
            String label = (String) iterator.next();
            Token innerToken1 = record1.get(label);
            Token innerToken2 = record2.get(label);
            boolean result = false;
            if (innerToken1 instanceof ArrayToken) {
                result = _isCloseToIfNilArrayElement(innerToken1, innerToken2,
                        epsilon);
            } else if (innerToken1 instanceof RecordToken) {
                result = _isCloseToIfNilRecordElement(innerToken1, innerToken2,
                        epsilon);
            } else {
                result = innerToken1.isCloseTo(innerToken2, epsilon)
                        .booleanValue();
            }

            if (!result) {
                if (innerToken1.isNil() && innerToken2.isNil()) {
                    // They are not close, but both are nil, so for
                    // our purposes, the are close.
                } else {
                    return false;
                }
            }
        }
        return true;
    }

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

    /** Set to true if fire() is called once.  If fire() is not called at
     *  least once, then throw an exception in wrapup().
     */
    protected boolean _firedOnce = false;

    /** Set to true when initialized() is called.
     */
    protected boolean _initialized = false;
}
