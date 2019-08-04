/* Catch exceptions and handle them with the specified policy.

 Copyright (c) 2014 The Regents of the University of California.
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ExceptionHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CatchExceptionAttribute

/**
 This attribute catches exceptions and attempts to handle them with the
 specified policy.  If the exception cannot be handled, the attribute
 indicates this to the Manager.  Status messages may be logged to a file.

 @author Edward A. Lee, Elizabeth Latronico
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (beth)
 @Pt.AcceptedRating Red (beth)
 */
public class CatchExceptionAttribute extends AbstractInitializableAttribute
implements ExceptionHandler, ExecutionListener {

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
    public CatchExceptionAttribute(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        policy = new StringParameter(this, "policy");
        policy.setExpression(THROW);

        policy.addChoice(CONTINUE);
        policy.addChoice(THROW);
        policy.addChoice(RESTART);
        policy.addChoice(STOP);

        logFileName = new FileParameter(this, "logFile");
        logFileName.setExpression("");
        logFileName.setTypeEquals(BaseType.STRING);
        _writer = null;

        exceptionMessage = new StringParameter(this, "exceptionMessage");
        exceptionMessage.setExpression("No exceptions encountered");
        exceptionMessage.setVisibility(Settable.NOT_EDITABLE);

        statusMessage = new StringParameter(this, "statusMessage");
        statusMessage.setExpression("No exceptions encountered");
        statusMessage.setVisibility(Settable.NOT_EDITABLE);

        _initialized = false;
        _resetMessages = true;
        _restartDesired = false;
        _subscribers = new ArrayList();

    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** The exception message from the caught exception. */
    public StringParameter exceptionMessage;

    /** The file, if any, to log messages to. */
    public FileParameter logFileName;

    /** The error handling policy to apply if an exception occurs.
     *
     * One of:  Continue, Throw, Restart, Quit
     */
    public StringParameter policy;

    /** The latest action, if any, taken by the CatchExceptionAttribute.
     *  For example, a notification that the model has restarted.
     *  It offers a way to provide feedback to the user.
     */
    public StringParameter statusMessage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this base class,
     *  the method does nothing.  In derived classes, this method may
     *  throw an exception, indicating that the new attribute value
     *  is invalid.  It is up to the caller to restore the attribute
     *  to a valid value if an exception is thrown.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        // Open the log file attributeChanged so that exceptions occurring in
        // preinitialize() or initialize() may logged

        if (attribute == logFileName) {
            if (logFileName != null) {
                // Copied (with some edits) from FileWriter
                String filenameValue = logFileName.getExpression();

                if (filenameValue == null || filenameValue.equals("\"\"")) {
                    // See $PTII/ptolemy/domains/sdf/kernel/test/auto/zeroRate_delay5.xml, which sets
                    // the filename to a string that has two doublequotes. ""
                    // _setWriter(null) will close any existing writer
                    _setWriter(null);
                } else if (!filenameValue.equals(_previousFilename)) {
                    // New filename. Close the previous.
                    _previousFilename = filenameValue;
                    _setWriter(null);
                    if (!filenameValue.trim().equals("")) {
                        java.io.Writer writer = logFileName.openForWriting();
                        _setWriter(writer);
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the attribute into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CatchExceptionAttribute newObject = (CatchExceptionAttribute) super
                .clone(workspace);

        newObject._subscribers = new ArrayList();
        return newObject;
    }

    /** Do nothing upon execution error.  Exceptions are passed to this
     *  attribute through handleException().  This method is required by
     *  the ExecutionListener interface.
     *  @param manager Ignored.
     *  @param throwable Ignored.
     */
    @Override
    public void executionError(Manager manager, Throwable throwable) {

    }

    /** Restart here if restart is desired.  This method is called upon
     *  successful completion.
     *  @param manager The manager that starts the run.
     */
    @Override
    public void executionFinished(Manager manager) {
        if (_restartDesired) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            try {
                // Start a new execution in a new thread
                try {
                    manager.startRun();
                } catch (IllegalActionException e) {
                    _writeMessage("Cannot restart model.  "
                            + "Manager.startRun() failed.");
                }

                _writeMessage("Model restarted at " + dateFormat.format(date));
            } catch (IOException e) {
                statusMessage.setExpression("Error:  Cannot write to file.");
            }
            // Do NOT reset messages in the event of a restart
            // This way, user can see that model was restarted
            _resetMessages = false;
            _restartDesired = false;
        }
    }

    // TODO:  Figure out what makes sense for continue (if anything)

    /** Handle an exception according to the specified policy:
     *
     *  continue: Not implemented yet
     *   Consume the exception and return control to the director.
     *   Could be valuable for domains like DE or modal models when new
     *   events will arrive.  Probably not appropriate for domains like SDF
     *   where the director follows a predefined schedule based on data flow
     *   (since the actor throwing the exception no longer provides output to
     *   the next actor).
     *
     *  throw:  Do not catch the exception.
     *
     *  restart:  Stop and restart the model.  Does not apply to exceptions
     *   generated during initialize().
     *
     *  stop:  Stop the model.
     *
     *  @param context The object in which the error occurred.
     *  @param exception The exception to be handled.
     *  @return true if the exception is handled; false if this attribute
     *   did not handle it
     *  @exception IllegalActionException If thrown by the parent
     */
    @Override
    public boolean handleException(NamedObj context, Throwable exception)
            throws IllegalActionException {

        // Try / catch for IOException from file writer

        try {
            // Save the exception message.  Only informational at the moment.
            exceptionMessage.setExpression(exception.getMessage());
            if (_writer != null) {
                _writer.write("Exception: " + exception.getMessage() + "\n");
                _writer.flush();
            }

            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            // Handle the exception according to the specified policy
            // TODO:  Apply different policies depending on the type of exception.
            // How would the policy be specified then?

            String policyValue = policy.stringValue();

            // Set initialized to false here, unless policy is to restart, in
            // which case set it after the current value is checked
            if (!policyValue.equals(RESTART)) {
                // Set _initialized here instead of in wrapup(), since
                // wrapup() is called prior to handleException()
                _initialized = false;
            }

            if (policyValue.equals(CONTINUE)) {
                _writeMessage("Execution continued at "
                        + dateFormat.format(date));

                // FIXME:  Is continue possible?  Looks like wrapup() is called
                // automatically before handleException()
            } else if (policyValue.equals(THROW)) {
                _writeMessage("Exception thrown at " + dateFormat.format(date));

                // Return false if an exception is thrown, since this attribute
                // did not resolve the exception.
                return false;

            } else if (policyValue.equals(RESTART)) {
                // Restarts the model in a new thread

                // Check if the model made it through initialize().  If not, return
                // false (thereby leaving exception unhandled)
                if (!_initialized) {

                    // Return false if an exception is thrown, since this attribute
                    // did not resolve the exception.
                    _writeMessage("Cannot restart: Error before or during "
                            + "intialize()");
                    return false;
                }

                // Set _initialized here, instead of in wrapup(), since
                // wrapup() is called prior to handleException()
                _initialized = false;

                // Find an actor in the model; use the actor to get the manager.
                Manager manager = null;

                NamedObj toplevel = toplevel();
                if (toplevel != null) {
                    Iterator iterator = toplevel.containedObjectsIterator();
                    while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof Actor) {
                            manager = ((Actor) obj).getManager();
                        }
                    }
                }

                if (manager != null) {
                    // End execution
                    manager.finish();

                    // Wait until the manager notifies listeners of successful
                    // completion before restarting.  Manager will call
                    // _executionFinished().  Set a flag here indicating to restart
                    _restartDesired = true;

                } else {
                    _writeMessage("Cannot restart model since there is no model "
                            + "Manager.  Perhaps the model has no actors?");
                    return false;
                }

            } else if (policyValue.equals(STOP)) {
                _writeMessage("Model stopped at " + dateFormat.format(date));

                // Call validate() to notify listeners of these changes
                exceptionMessage.validate();
                statusMessage.validate();

                // wrapup() is automatically called prior to handleException(),
                // so don't need to call it again
            } else {
                _writeMessage("Illegal policy encountered at: "
                        + dateFormat.format(date));

                // Throw an exception here instead of just returning false, since
                // this is a problem with CatchExceptionAttribute
                throw new IllegalActionException(this,
                        "Illegal exception handling policy.");
            }

            // Call validate() to notify listeners of these changes
            exceptionMessage.validate();
            statusMessage.validate();

            _resetMessages = false;

            // Notify all ExceptionSubscribers
            for (ExceptionSubscriber subscriber : _subscribers) {
                subscriber.exceptionOccurred(policyValue, exception);
            }

            return true;
        } catch (IOException ioe) {
            statusMessage.setExpression("Error:  Cannot write to file.");
            return false;
        }
    }

    /** Find all of the ExceptionSubscribers in the model.
     *
     *  @exception IllegalActionException If thrown by parent
     */
    @Override
    public void initialize() throws IllegalActionException {

        // Use allAtomicEntityList here to include entities inside composite
        // actors. Could switch to containedObjectsIterator in the future if we
        // want to allow attributes to be ExceptionSubscribers.  (Will need to
        // implement a deep search.  containedObjectsIterator does not look
        // inside composite entities).
        Iterator iterator = ((CompositeActor) toplevel()).allAtomicEntityList()
                .iterator();
        _subscribers.clear();
        NamedObj obj;

        while (iterator.hasNext()) {
            obj = (NamedObj) iterator.next();
            if (obj instanceof ExceptionSubscriber) {
                _subscribers.add((ExceptionSubscriber) obj);
            }
        }
    }

    /** React to a change of state in the Manager.
     *
     * @param manager The model manager
     */
    @Override
    public void managerStateChanged(Manager manager) {
        if (manager.getState().equals(Manager.EXITING)) {
            // Close file writer, if any
            if (_writer != null) {
                try {
                    _writer.close();
                } catch (IOException e) {
                    // Can't really do anything about an exception here?
                }
            }
        } else if (manager.getState().equals(Manager.INITIALIZING)) {
            // Enable restart once all objects have been initialized
            //_initialized is set back to false at the end of _handleException()
            if (_resetMessages) {
                exceptionMessage.setExpression("No exceptions encountered");
                statusMessage.setExpression("No exceptions encountered");

                // Call validate() to notify listeners of these changes
                try {
                    exceptionMessage.validate();
                    statusMessage.validate();
                } catch (IllegalActionException e) {
                    try {
                        _writeMessage("Error initializing status message.");
                    } catch (IOException ioe) {
                        statusMessage.setExpression("Error writing to file");
                    }
                }
            }

            _resetMessages = true;
            _initialized = true;
        }
    }

    /** Register this attribute with the manager.  Done here instead of in the
     *  constructor since the director is found in order to get the manager.
     *  The constructor for this attribute might be called before the
     *  constructor for the director.
     *
     *  @exception IllegalActionException If the parent class throws it
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Find an actor in the model; use the actor to get the manager.
        Manager manager = null;

        NamedObj toplevel = toplevel();
        if (toplevel != null) {
            Iterator iterator = toplevel.containedObjectsIterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof Actor) {
                    manager = ((Actor) obj).getManager();
                }
            }
        }

        if (manager != null) {
            manager.addExecutionListener(this);
        } else {
            throw new IllegalActionException(this, "Manager cannot be found. "
                    + "Perhaps the model has no actors?");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write the given message to the statusMessage parameter and to the log
     * file, if open.
     * @param message The message to write
     * @exception IOException If there is a problem writing to the file
     */
    protected void _writeMessage(String message) throws IOException {
        statusMessage.setExpression(message);
        if (_writer != null) {
            _writer.write(message + " \n");
            _writer.flush();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the writer.  If there was a previous writer, close it.
     *  To set standard output, call this method with argument null.
     *  @param writer The writer to write to.
     *  @exception IllegalActionException If an IO error occurs.
     */
    private void _setWriter(java.io.Writer writer)
            throws IllegalActionException {
        // Copied (with some edits) from FileWriter.
        try {
            if (_writer != null && _writer != _stdOut) {
                _writer.close();

                // Since we have closed the writer, we also need to clear
                // _previousFilename, so that a new writer will be opened for
                // this filename if the model is executed again
                _previousFilename = null;
            }
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex, "setWriter(" + writer
                    + ") failed");
        }

        if (writer != null) {
            _writer = writer;
        } else {
            _writer = _stdOut;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** String value for the "continue" policy. */
    public static final String CONTINUE = "continue";

    /** String value for the "restart" policy. */
    public static final String RESTART = "restart";

    /** String value for the "throw" policy. */
    public static final String THROW = "throw";

    /** String value for the "stop" policy. */
    public static final String STOP = "stop";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** True if the model has been initialized but not yet wrapped up,
     *  false otherwise.  Some policies (e.g. restart) are desirable only
     *  for run-time exceptions.
     */
    private boolean _initialized;

    /** The previously used filename, or null if none has been previously used.
     */
    private String _previousFilename = null;

    /** True if the model has been started externally (e.g. by a user);
     * false if the model has been restarted by this attribute.
     */
    private boolean _resetMessages;

    /** True if this attribute should invoke Manager.startRun() upon successful
     *  completion (i.e. when executionFinished() is invoked).
     */
    private boolean _restartDesired;

    /** A list of all ExceptionSubscribers, to be notified when an exception is
     *  caught by this class.
     */
    private ArrayList<ExceptionSubscriber> _subscribers;

    /** Standard out as a writer. */
    private static java.io.Writer _stdOut = null;

    /** The writer to write to. */
    private java.io.Writer _writer = null;
}
