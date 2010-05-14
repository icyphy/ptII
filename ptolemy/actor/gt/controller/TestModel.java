/* An event to compare the model with a known good run

 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.net.URI;

import javax.swing.JFrame;

import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.XMLToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.lib.EventUtils;
import ptolemy.domains.ptera.lib.TableauParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.gt.GTFrameTools;

import java.util.ArrayList;
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
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// Test

/**
 An event to compare the model in the model parameter with a known good result.

 @author Christopher Brooks, based on View by Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestModel extends GTEvent {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public TestModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        correctValues = new Parameter(this, "correctValues");
        correctValues.setExpression("{true}");
        correctValues.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);

        requireAllCorrectValues = new SharedParameter(this,
                "requireAllCorrectValues", getClass(), "true");
        requireAllCorrectValues.setTypeEquals(BaseType.BOOLEAN);

        tolerance = new Parameter(this, "tolerance");
        tolerance.setExpression("1.0E-9");
        tolerance.setTypeEquals(BaseType.DOUBLE);

        trainingMode = new SharedParameter(this, "trainingMode", getClass(),
                "false");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);

        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A matrix specifying what the input should be.
     *  This defaults to a one-by-one array containing a boolean true.
     */
    public Parameter correctValues;

    /** If true, and the number of tokens seen in wrapup() is not
     *  equal to or greater than the number of elements in the
     *  <i>correctValues</i> array, then throw an exception.  The
     *  default value is true. This parameter is a shared parameter,
     *  meaning that changing it for any one instance in a model will
     *  change it for all instances in the model.
     */
    public Parameter requireAllCorrectValues;

    /** A double specifying how close the input has to be to the value
     *  given by <i>correctValues</i>.  This is a DoubleToken, with default
     *  value 10<sup>-9</sup>.
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

    /** Clone the event into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TestModel newObject = (TestModel) super.clone(workspace);
        newObject._init();
        newObject.correctValues.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        return newObject;
    }

    /** Process this event and show the model in the model parameter in the
     *  designated tableau.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the tableau cannot be used, or if
     *   thrown by the superclass.
     */
    public RefiringData fire(Token arguments) throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        Effigy effigy = EventUtils.findToplevelEffigy(this);
        if (effigy == null) {
            // The effigy may be null if the model is closed.
            return data;
        }

        CompositeEntity entity = getModelParameter().getModel();
        try {
            entity = (CompositeEntity) GTTools.cleanupModel(entity, _parser);
        } finally {
            _parser.reset();
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


//         if (input.getWidth() != 1) {
//             throw new IllegalActionException(this, "Width of input is "
//                     + input.getWidth()
//                     + " but NonStrictTest only supports a width of 1.");
//         }

        boolean training = ((BooleanToken) trainingMode.getToken())
                .booleanValue();

        if (training) {
            System.out.println("Training!");
            if (_trainingTokens == null) {
                _trainingTokens = new ArrayList();
            }

            //if (input.hasToken(0)) {
            XMLToken moml;
            try {
                moml = new XMLToken(entity.exportMoML());
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex, "Failed to parse "
                        + entity.exportMoML());

            }
            System.out.println("Training: " + moml);
            _trainingTokens.add(moml);
                //}

            return data;
        }

        if (_numberOfInputTokensSeen >= ((ArrayToken) (correctValues.getToken()))
                .length()) {
            System.out.println("Read all of our correct values");
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            //if (input.hasToken(0)) {
            //    input.get(0);
            //}

            return data;
        }

        XMLToken referenceToken = (XMLToken)((ArrayToken) (correctValues.getToken()))
                .getElement(_numberOfInputTokensSeen);

        //if (input.hasToken(0)) {
        //    Token token = input.get(0);
        XMLToken token;
        try {
            token = new XMLToken(entity.exportMoML());
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex, "Failed to parse "
                        + entity.exportMoML());

            }
            _numberOfInputTokensSeen++;

            // FIXME: If we get a nil token on the input, what should we do?
            // Here, we require that the referenceToken also be nil.
            // If the token is an ArrayToken and two corresponding elements
            // are nil, then we consider them "close".
    System.out.println("Comparison between " + referenceToken + "\n and \n"
            + token);
    if (token.toString().equals(referenceToken.toString()) == false
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
            //}

    System.out.println("_iteration: " + _iteration);
        _iteration++;
        return data;
    }

    /** Initialize this event.
     *
     *  @exception IllegalActionException If thrown by the superclass.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        super.initialize();
        _numberOfInputTokensSeen = 0;
        _iteration = 0;
        _trainingTokens = null;
        _firedOnce = false;
        _initialized = true;
        _workspace.removeAll();
    }


    /** If <i>trainingMode</i> is <i>true</i>, then take the collected
     *  training tokens and store them as an array in <i>correctValues</i>.
     *  @exception IllegalActionException If initialized() was called
     *  and fire() was not called or if the number of inputs tokens seen
     *  is not greater than or equal to the number of elements in the
     *  <i>correctValues</i> array.
     */
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

            if (_numberOfInputTokensSeen < ((ArrayToken) (correctValues
                    .getToken())).length()) {
                String errorMessage = "The test produced only "
                        + _numberOfInputTokensSeen
                        + " tokens, yet the correctValues parameter was "
                        + "expecting "
                        + ((ArrayToken) (correctValues.getToken())).length()
                        + " tokens.";
                if (((BooleanToken) requireAllCorrectValues.getToken())
                        .booleanValue()) {
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
        if (training && (_trainingTokens != null)
                && (_trainingTokens.size() > 0)) {
            Object[] newValues = _trainingTokens.toArray();

            // NOTE: Support input multiport for the benefit of derived classes.
            int width = 1;
            Token[] newTokens = new Token[newValues.length];

            if (width == 1) {
                for (int i = 0; i < newValues.length; i++) {
                    if (newValues[i] instanceof Token[]) {
                        // Handle width of 1, ArrayToken
                        newTokens[i] = new ArrayToken((Token[]) newValues[i]);
                    } else {
                        newTokens[i] = (Token) newValues[i];
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
                    }

                    newTokens[i] = new ArrayToken(newEntry);
                }
            }

            correctValues.setToken(new ArrayToken(newTokens));
            correctValues.setPersistent(true);
        }

        if (training
                && ((_trainingTokens == null) || (_trainingTokens.size() == 0))) {
            System.err.println("Warning: '" + getFullName()
                    + "' The test produced 0 tokens.");
            // If we get no data and we are training, set the expression
            // to the empty string.

            // Copernicus: Don't use setExpression() here, use setToken(NIL)
            //correctValues.setExpression("{}");
            correctValues.setToken(ArrayToken.NIL);
        }
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



    /** Create a parser for parsing models.
     */
    private void _init() {
        _workspace = new Workspace();
        _parser = new MoMLParser(_workspace);
    }

    /** The parser.
     */
    private MoMLParser _parser;

    /** The workspace for the parser.
     */
    private Workspace _workspace;
}
