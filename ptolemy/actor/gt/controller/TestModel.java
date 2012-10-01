/* An event to compare the model with a known good run

 Copyright (c) 2010-2012 The Regents of the University of California.
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

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.gt.GTTools;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// Test

/**
 An event to compare the model in the model parameter with a known good result.
 <p>If the models differ only be the VersionAttribute, then they are
 considered to be equal.

 @see ptolemy.actor.lib.NonStrictTest

 @author Christopher Brooks, based on View by Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestModel extends GTEvent {
    // FIXME: This is a horrible copy and paste of ptolemy.actor.lib.NonStrictTest

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
        _firedOnce = true;
        RefiringData data = super.fire(arguments);

        CompositeEntity entity = getModelParameter().getModel();
        List filters = MoMLParser.getMoMLFilters();
        try {
            // The test suite calls MoMLSimpleApplication multiple times,
            // and the list of filters is static, so we reset it each time
            // so as to avoid adding filters every time we run an auto test.
            // We set the list of MoMLFilters to handle Backward Compatibility.
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

            // Filter out any graphical classes.
            RemoveGraphicalClasses removeGraphicalClasses = new RemoveGraphicalClasses();
            // Remove VersionAttributes.
            removeGraphicalClasses.put(
                    "ptolemy.kernel.attributes.VersionAttribute", null);
            MoMLParser.addMoMLFilter(removeGraphicalClasses);
            entity = (CompositeEntity) GTTools.cleanupModel(entity, _parser);
        } finally {
            MoMLParser.setMoMLFilters(filters);
            _parser.reset();
        }

        /* Read one token from each input channel and compare against
           the value specified in <i>correctValues</i>.  If the token
           count is larger than the length of <i>correctValues</i>,
           then return immediately, indicating that the inputs
           correctly matched the values in <i>correctValues</i> and
           that the test succeeded. */

        boolean training = ((BooleanToken) trainingMode.getToken())
                .booleanValue();

        if (training) {
            if (_trainingTokens == null) {
                _trainingTokens = new ArrayList();
            }

            Token moml;
            try {
                moml = new StringToken(entity.exportMoML());
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex, "Failed to parse "
                        + entity.exportMoML());

            }
            _trainingTokens.add(moml);

            return data;
        }

        if (_numberOfInputTokensSeen >= ((ArrayToken) (correctValues.getToken()))
                .length()) {
            // Consume and discard input values.  We are beyond the end
            // of the correctValues array.
            return data;
        }

        Token referenceToken = ((ArrayToken) (correctValues.getToken()))
                .getElement(_numberOfInputTokensSeen);

        Token token;
        try {
            token = new StringToken(entity.exportMoML());
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to parse "
                    + entity.exportMoML());
        }
        _numberOfInputTokensSeen++;

        if (!token.toString().equals(referenceToken.toString())) {
            String versionAttribute = "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"[^\"]\">";
            String replacement = "<!-- VersionAttribute -->";
            if (token
                    .toString()
                    .replaceAll(versionAttribute, replacement)
                    .equals(referenceToken.toString().replaceAll(
                            versionAttribute, replacement))) {
                throw new IllegalActionException(this,
                        "Test fails in iteration " + _iteration + ".\n"
                                + "Value was: " + token
                                + ".\nShould have been: " + referenceToken);
            } else {
                System.out.println("TestModel: results differed from known "
                        + "good results by only the VersionAttribute");
            }
        }

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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Number of input tokens seen by this actor in the fire method.*/
    protected int _numberOfInputTokensSeen = 0;

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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a parser for parsing models.
     */
    private void _init() {
        _workspace = new Workspace();
        _parser = new MoMLParser(_workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The parser.
     */
    private MoMLParser _parser;

    /** The workspace for the parser.
     */
    private Workspace _workspace;
}
