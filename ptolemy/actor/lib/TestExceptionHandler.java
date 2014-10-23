/* Handle exceptions thrown in tests.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ExceptionHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// TestExceptionHandler

/**
 This actor tests for exceptions that are expected to occur when
 running a test model. When an exception is
 thrown by the model, this actor is invoked. It has two
 working modes, training mode and non-training mode. If in training mode,
 this actor handles an exception by recording the exception message. If
 not in training mode, this actor first compares the previously stored
 (assumed correct) message to the exception message and then throws an
 exception if the two messages are not the same.
 Also, if a test runs to completion without throwing an exception, this actor
 throws an exception in its wrapup() method. An exception is expected.

 @author Haiyang Zheng
 @deprecated Use {@link TestExceptionAttribute} instead.
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Yellow (hyzheng)
 */
@Deprecated
public class TestExceptionHandler extends TypedAtomicActor implements
ExceptionHandler {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TestExceptionHandler(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        correctExceptionMessage = new StringParameter(this,
                "correctExceptionMessage");
        correctExceptionMessage.setExpression("");
        trainingMode = new SharedParameter(this, "trainingMode", getClass(),
                "false");
        trainingMode.setTypeEquals(BaseType.BOOLEAN);
        _invoked = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** If true, then collect the exception message and set the
     *  correctExceptionMessage parameter with the content of the
     *  exception. This parameter is a boolean, and it defaults to false.
     *  It is a shared parameter, meaning
     *  that changing it for any one instance in a model will change
     *  it for all instances in the model.
     */
    public SharedParameter trainingMode;

    /** The correct exception message to be compared against. */
    public StringParameter correctExceptionMessage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize. */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _invoked = false;
    }

    /** Handle an exception thrown in a test. If in training
     *  mode, simply record the exception message. If not in training mode,
     *  first compare the stored good message against the exception message.
     *  If they are the same, do nothing. Otherwise, throw the exception again.
     *  @param context The object in which the error occurred.
     *  @param exception The exception to be handled.
     *  @return True if the exception message is the same as the saved message.
     *  @exception IllegalActionException If cannot get a valid token from
     *  the trainingMode parameter or the exception message is not the same as
     *  the stored message.
     */
    @Override
    public boolean handleException(NamedObj context, Throwable exception)
            throws IllegalActionException {
        _invoked = true;
        boolean training = ((BooleanToken) trainingMode.getToken())
                .booleanValue();
        if (training) {
            correctExceptionMessage.setExpression(exception.getMessage());
            correctExceptionMessage.setPersistent(true);
        } else {
            if (!exception.getMessage().equals(
                    correctExceptionMessage.stringValue())) {
                throw new IllegalActionException(this, exception, "Expected:\n"
                        + correctExceptionMessage.stringValue()
                        + "\nBut got:\n" + exception.getMessage());
            }
        }
        return true;
    }

    /** Call the super.wrapup() method. Check whether this actor has
     *  been invoked to handle exceptions. If not, throw an exception.
     *  Otherwise, do nothing.
     *  @exception IllegalActionException If this actor has not been
     *   invoked to handle exceptions.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
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
        if (!_invoked) {
            throw new IllegalActionException(this, " should "
                    + "have handled an exception but did not see any.");
        } else {
            _invoked = false;
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
    ////                         private variables                 ////

    private boolean _invoked = false;
}
