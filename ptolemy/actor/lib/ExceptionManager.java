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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import ptolemy.actor.AbstractInitializableAttribute;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Initializable;
import ptolemy.actor.Manager;
import ptolemy.actor.lib.gui.ExceptionManagerGUIFactory;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.ExceptionHandler;
import ptolemy.kernel.util.HierarchyListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLModelAttribute;

///////////////////////////////////////////////////////////////////
//// ExceptionManager

/**
 The ExceptionManager catches exceptions and attempts to handle them with the
 specified policy.  It notifies ExceptionSubscribers after an exception has
 occurred and again after the handling attempt.  If the exception cannot be
 handled, the model Manager is informed.

 @author Elizabeth Latronico
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (beth)
 @Pt.AcceptedRating Red (beth)
 */

public class ExceptionManager extends MoMLModelAttribute implements
        ExceptionHandler, ExecutionListener, Initializable, HierarchyListener {


    /** Create a model attribute with the specified container and name.
     *  @param container The specified container.
     *  @param name The specified name.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public ExceptionManager(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Provide a default model
        _model = new ExceptionManagerModel(this, workspace());

        // TODO:  Icon should list contained commands and status of each
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:14; font-family:SansSerif; fill:white;\">"
                + "Exception \nManager</text></svg>");

        new ExceptionManagerGUIFactory(this, "_ExceptionManagerGUIFactory");

        policy = new StringParameter(this, "policy");
        policy.setExpression(CatchExceptionAttribute.THROW);

        policy.addChoice(CatchExceptionAttribute.RESTART);
        policy.addChoice(CatchExceptionAttribute.STOP);
        policy.addChoice(CatchExceptionAttribute.THROW);

        exceptionMessage = new StringParameter(this, "exceptionMessage");
        exceptionMessage.setExpression("No exceptions encountered");
        exceptionMessage.setVisibility(Settable.NOT_EDITABLE);

        statusMessage = new StringParameter(this, "statusMessage");
        statusMessage.setExpression("No exceptions encountered");
        statusMessage.setVisibility(Settable.NOT_EDITABLE);

        modelURL.setVisibility(Settable.NONE);

        _resetMessages = true;
        _restartDesired = false;
        _subscribers = new ArrayList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The exception message from the caught exception. */
    public StringParameter exceptionMessage;

    /** The error handling policy to apply if an exception occurs.
     *  One of: restart, stop, throw.  See {@link CatchExceptionAttribute}
     */
    public StringParameter policy;

    /** The latest action, if any, taken by the CatchExceptionAttribute.
     *  For example, a notification that the model has restarted.
     *  It offers a way to provide feedback to the user.
     */
    public StringParameter statusMessage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the specified object to the set of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object.
     *  @param initializable The object whose methods should be invoked.
     *  @see #removeInitializable(Initializable)
     */
    @Override
    public void addInitializable(Initializable initializable) {
        if (_initializables == null) {
            _initializables = new LinkedHashSet<Initializable>();
        }
        _initializables.add(initializable);
    }


    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ExceptionManager newObject = (ExceptionManager) super.clone(workspace);

        newObject._initializables = new LinkedHashSet<Initializable>();
        newObject._initialized = false;
        newObject._resetMessages = false;
        newObject._restartDesired = false;
        newObject._subscribers = new ArrayList();

        return newObject;
    }

    /** Do nothing upon execution error.  Exceptions are passed to this
     *  attribute through handleException().  This method is required by
     *  the ExecutionListener interface.
     */
    @Override
    public void executionError(Manager manager, Throwable throwable) {

    }

    /** Restart here if restart is desired.  This method is called upon
     * successful completion.
     */
    @Override
    public void executionFinished(Manager manager) {
        if (_restartDesired) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            // Start a new execution in a new thread
            try {
                manager.startRun();
            } catch (IllegalActionException e) {
                statusMessage.setExpression("Cannot restart model.  "
                        + "Manager.startRun() failed.");
            }

            statusMessage.setExpression("Model restarted at "
                    + dateFormat.format(date));

            // Do NOT reset messages in the event of a restart
            // This way, user can see that model was restarted
            _resetMessages = false;
            _restartDesired = false;
        }
    }

    /** Notify this object that the containment hierarchy above it has
     *  changed. This method does nothing because instead we use
     *  {@link #preinitialize()} to handle re-establishing the connections.
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     *   @see AbstractInitializableAttribute
     */
    @Override
    public void hierarchyChanged() throws IllegalActionException {
        // Make sure we are registered as to be initialized
        // with the container.
        Initializable container = _getInitializableContainer();
        if (container != null) {
            container.addInitializable(this);
        }
    }

    /** Notify this object that the containment hierarchy above it will be
     *  changed.
     *  @exception IllegalActionException If unlinking to a published port fails.
     *  @see AbstractInitializableAttribute
     */
    @Override
    public void hierarchyWillChange() throws IllegalActionException {
        // Unregister to be initialized with the initializable container.
        // We will be re-registered when hierarchyChanged() is called.
        Initializable container = _getInitializableContainer();
        if (container != null) {
            container.removeInitializable(this);
        }
    }

    /** Find all of the ExceptionSubscribers in the model and save in a list.
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

        // Also, check for entities inside the model contained by this attribute
        iterator = _model.containedObjectsIterator();

        while (iterator.hasNext()) {
            obj = (NamedObj) iterator.next();
            if (obj instanceof ExceptionSubscriber) {
                _subscribers.add((ExceptionSubscriber) obj);
            }
        }

        // TODO:  Figure out why setting this through the constructor is not
        // working
        ((ExceptionManagerModel) _model).setModelContainer(this);
    }

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

        // Notify all subscribers, in the specified order, of the exception
        // Note at this stage it is not guaranteed that the exception can be
        // handled successfully

        for (ExceptionSubscriber subscriber : _subscribers) {
            subscriber.exceptionOccurred(policy.getValueAsString(), exception);
        }

        // Handle the exception according to the policy

         // Save the exception message.  Only informational at the moment.
         exceptionMessage.setExpression(exception.getMessage());

         Date date = new Date(System.currentTimeMillis());
         SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

         // Handle the exception according to the specified policy

         String policyValue = policy.stringValue();

         // Set initialized to false here, unless policy is to restart, in
         // which case set it after the current value is checked
         if (!policyValue.equals(CatchExceptionAttribute.RESTART)) {
             // Set _initialized here instead of in wrapup(), since
             // wrapup() is called prior to handleException()
             _initialized = false;
         }

          if (policyValue.equals(CatchExceptionAttribute.RESTART)) {
             // Restarts the model in a new thread

             // Check if the model made it through initialize().  If not, return
             // false (thereby leaving exception unhandled)
             if (!_initialized) {

                 // Return false if an exception is thrown, since this attribute
                 // did not resolve the exception.
                 statusMessage.setExpression("Cannot restart: Error before " +
                                 "or during intialize()");
                 for (ExceptionSubscriber subscriber : _subscribers) {
                     subscriber.exceptionHandled(false, policyValue);
                 }
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
                 statusMessage.setExpression("Cannot restart model since " +
                    "there is no model Manager.  Perhaps the model has no " +
                    "actors?");
                 for (ExceptionSubscriber subscriber : _subscribers) {
                     subscriber.exceptionHandled(false, policyValue);
                 }
                 return false;
             }

         } else if (policyValue.equals(CatchExceptionAttribute.STOP)) {
             statusMessage.setExpression("Model stopped at "
                     + dateFormat.format(date));

             // Call validate() to notify listeners of these changes
             exceptionMessage.validate();
             statusMessage.validate();

             // wrapup() is automatically called prior to handleException(),
             // so don't need to call it again
         } else if (policyValue.equals(CatchExceptionAttribute.THROW)) {
             statusMessage.setExpression("Exception thrown at "
                     + dateFormat.format(date));

             // Return false if an exception is thrown, since this attribute
             // did not resolve the exception.
             for (ExceptionSubscriber subscriber : _subscribers) {
                 subscriber.exceptionHandled(false, policyValue);
             }
             return false;

         } else {
             statusMessage.setExpression("Illegal policy encountered at: "
                     + dateFormat.format(date));

             for (ExceptionSubscriber subscriber : _subscribers) {
                 subscriber.exceptionHandled(false, policyValue);
             }
             // Throw an exception here instead of just returning false, since
             // this is a problem with CatchExceptionAttribute
             throw new IllegalActionException(this,
                     "Illegal exception handling policy.");
         }

         // Call validate() to notify listeners of these changes
         exceptionMessage.validate();
         statusMessage.validate();

         _resetMessages = false;

         for (ExceptionSubscriber subscriber : _subscribers) {
             subscriber.exceptionHandled(true, policyValue);
         }

        return true;
    }

    /** React to a change of state in the Manager.
     *
     * @param manager The model manager
     */

    @Override
    public void managerStateChanged(Manager manager) {

        if (manager.getState().equals(Manager.INITIALIZING)) {
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
                    // TODO:  What to do if parameters don't validate()?
                }
            }

            _resetMessages = true;
            _initialized = true;
        }
    }

    /** Remove the specified object from the list of objects whose
     *  preinitialize(), initialize(), and wrapup()
     *  methods should be invoked upon invocation of the corresponding
     *  methods of this object. If the specified object is not
     *  on the list, do nothing.
     *  @param initializable The object whose methods should no longer be invoked.
     *  @see #addInitializable(Initializable)
     */
    @Override
    public void removeInitializable(Initializable initializable) {
        if (_initializables != null) {
            _initializables.remove(initializable);
            if (_initializables.size() == 0) {
                _initializables = null;
            }
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

    /** Override the base class to register as an
     *  {@link Initializable}
     *  so that preinitialize() is invoked, and as a
     *  {@link HierarchyListener}, so that we are notified of
     *  changes in the hierarchy above.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     *   @see AbstractInitializableAttribute
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        Initializable previousInitializableContainer = _getInitializableContainer();
        NamedObj previousContainer = getContainer();
        super.setContainer(container);
        Initializable newInitializableContainer = _getInitializableContainer();
        if (previousInitializableContainer != newInitializableContainer) {
            if (previousInitializableContainer != null) {
                previousInitializableContainer.removeInitializable(this);
            }
            if (newInitializableContainer != null) {
                newInitializableContainer.addInitializable(this);
            }
        }
        if (previousContainer != container) {
            if (previousContainer != null) {
                previousContainer.removeHierarchyListener(this);
            }
            if (container != null) {
                container.addHierarchyListener(this);
            }
        }
    }

    /** Invoke wrapup() on registered initializables.
     *  @exception IllegalActionException If thrown by a subclass.
     *  @see AbstractInitializableAttribute
     */
    public void wrapup() throws IllegalActionException {
        // Invoke initializable methods.
        if (_initializables != null) {
            for (Initializable initializable : _initializables) {
                initializable.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the first Initializable encountered above this
     *  in the hierarchy that will be initialized (i.e., it is either
     *  an atomic actor or an opaque composite actor).
     *  @return The first Initializable above this in the hierarchy,
     *   or null if there is none.
     *   @see AbstractInitializableAttribute
     */
    protected Initializable _getInitializableContainer() {
        NamedObj container = getContainer();
        while (container != null) {
            if (container instanceof Initializable) {
                if (container instanceof CompositeActor) {
                    if (((CompositeActor) container).isOpaque()) {
                        return (Initializable) container;
                    }
                } else {
                    return (Initializable) container;
                }
            }
            container = container.getContainer();
        }
        return null;
    }

    // Commands
    // - Message displayer (separate or integrate?) (probably easier to just integrate)
    // - File logger
    // - Emailer

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of objects whose (pre)initialize() and wrapup() methods should be
     *  slaved to these.
     *  See {@link AbstractInitializableAttribute}
     */
    private transient Set<Initializable> _initializables;

    /** True if the model has been initialized but not yet wrapped up;
     *  false otherwise.  Some policies (e.g. restart) are desirable only
     *  for run-time exceptions.
     */
    private boolean _initialized;

    /** True if the model has been started externally (e.g. by a user);
     * false if the model has been restarted by this attribute.
     */
    private boolean _resetMessages;

    /** True if this attribute should invoke Manager.startRun() upon successful
     *  completion (i.e. when executionFinished() is invoked).
     */
    private boolean _restartDesired;

    /** A list of all ExceptionSusbcribers, to be notified when an exception is
     *  caught by this class.
     */
     private ArrayList<ExceptionSubscriber> _subscribers;
}
