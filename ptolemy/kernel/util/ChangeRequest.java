/* Abstract base class for change requests.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (neuendor@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ChangeRequest
/**
Abstract base class for change requests.  A change request is any
modification to a model that might be performed during execution of the
model, but where there might only be certain phases of execution during
which it is safe to make the modification.  Such changes are also called
<i>mutations</i>.
<p>
A typical use of this class is to define an anonymous inner class that
implements the _execute() method to bring about the desired change.
The instance of that anonymous inner class is then queued with
an instance of NamedObj using its requestChange() method.
The execute() method must be called only once; attempting to call
it multiple times will trigger an exception.
<p>
Concrete derived classes can be defined to implement
mutations of a certain kind or in a certain way. Instances of these
classes should be queued with a NamedObj, just like an anonymous
inner class extending this class. MoMLChangeRequest is such a concrete
derived class, where the mutation is specified as MoML code.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@see ChangeListener
*/
public abstract class ChangeRequest {

    /** Construct a request with the specified source and description.
     *  The description is a string that is used to report the change,
     *  typically to the user in a debugging environment.
     *  The source is the object that requested this change request.
     *  A listener to changes will probably want to check the source
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  @param source The source of the change request.
     *  @param description A description of the change request.
     */
    public ChangeRequest(Object source, String description) {
        _source = source;
        _description = description;
        _errorReported = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new change listener to this request.  The listener will get
     *  notified when the change is executed, or the change fails.  This
     *  listener is notified first, and then any listeners that were
     *  given by setListeners.  This listener is also notified before
     *  other listeners that have been previously registered with this
     *  object.
     *  @param listener The listener to add.
     */
    public void addChangeListener(ChangeListener listener) {
        if (_localListeners == null) {
            _localListeners = new LinkedList();
        }
        // NOTE: We do not use weak references for these
        // listeners because an instance of ChangeRequest
        // is typically a transitory object.
        if (!_localListeners.contains(listener)) {
            _localListeners.add(0, listener);
        }
    }

    /** Execute the change.  This method invokes the protected method
     *  _execute(), takes care of reporting execution to any listeners
     *  and then wakes up any threads that might be waiting in a call to
     *  waitForCompletion().  Listeners that are attached directly to this
     *  object (using the addChangeListener() and removeChangeListener()
     *  methods) are notified first of the status of the request, followed
     *  by global listeners that were set using the setListeners() method.
     *  If the change failed because an exception was thrown, and the
     *  exception was not reported to any global listeners, then
     *  we throw an InternalErrorException because it is a bug to
     *  not have a listener in this case.
     *  <p>
     *  This method should be called exactly once, by the object that
     *  the change request was queued with.  Attempting to call this
     *  method more than once will throw an exception.
     */
    public final synchronized void execute() {
        if (!_pending) {
            throw new InternalErrorException(
                    "Attempted to execute a change request "
                    + "that had already been executed.");
        }
        _exception = null;
        // This flag is set if an exception is caught.  If the exception
        // is reported to any listeners set with setListeners, then
        // the flag is reset to false.  If we get to the end and the
        // flag is still true, then we write out to standard error.
        boolean needToReport = false;
        try {
            _execute();
        } catch (Exception ex) {
            needToReport = true;
            _exception = ex;
        }
        if (_localListeners != null) {
            Iterator listeners = _localListeners.iterator();
            while (listeners.hasNext()) {
                ChangeListener listener = (ChangeListener)listeners.next();
                if (_exception == null) {
                    listener.changeExecuted(this);
                } else {
                    // note that local listeners do not prevent an exception
                    // from being seen globally.  This is weird.
                    listener.changeFailed(this, _exception);
                }
            }
        }
        if (_listeners != null) {
            Iterator listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                Object listener = listeners.next();
                if (listener instanceof WeakReference) {
                    listener = ((WeakReference)listener).get();
                }
                if (listener instanceof ChangeListener) {
                    if (_exception == null) {
                        ((ChangeListener)listener).changeExecuted(this);
                    } else {
                        needToReport = false;
                        ((ChangeListener)listener).changeFailed(
                                this, _exception);
                    }
                }
            }
        }
        if (needToReport) {
            if (_exception != null) {
                // We used to print to stderr, but printing to stderr
                // is a bug if we have a UI, so we throw an InternalError.

                // If the _source is a Nameable, we use it in the Exception.
                Nameable object = null;
                if (_source instanceof Nameable) {
                    object = (Nameable)_source;
                }
                throw new InternalErrorException(object, _exception,
                        "ChangeRequest failed (NOTE: there is no "
                        + "ChangeListener):\n" + _description);
            }
        }
        _pending = false;
        notifyAll();
    }

    /** Get the description that was specified in the constructor.
     *  @return The description of the change.
     */
    public String getDescription() {
        return _description;
    }

    /** Get the source that was specified in the constructor.
     *  @return The source of the change.
     */
    public Object getSource() {
        return _source;
    }

    /** Return true if setErrorReported() has been called with a true
     *  argument.  This is used by listeners to avoid reporting an
     *  error repeatedly.  By convention, a listener that reports the
     *  error to the user, in a dialog box for example, should call
     *  this method to determine whether the error has already been
     *  reported.  If it reports the error, then it should call
     *  setErrorReported() with a true argument.
     *  @return True if an error has already been reported.
     */
    public boolean isErrorReported() {
        return _errorReported;
    }

    /** Return false if the change represented by this request has been
     *  asserted to be non-persistent by calling setPersistent(false),
     *  and return true otherwise.  That is, return false if the change
     *  has been asserted to not affect the
     *  MoML representation of the model. This might be used, for
     *  example, by a user interface, to determine whether to mark
     *  the model "modified," and hence, whether to prompt the user
     *  to save the model if a window is closed.
     *  <p>
     *  This method returns <i>true</i> unless setPersistent()
     *  has been called with an argument <i>false</i>.  It is up
     *  to the creator of the change request to call that method
     *  to ensure that the change is not persistent. There is
     *  no automatic detection of whether the change is persistent.
     *
     *  @see #setPersistent(boolean)
     *  @return True if the change represented by this request is
     *   persistent.
     */
    public boolean isPersistent() {
        return _persistent;
    }

    /** Remove the given change listener from this request.
     *  The listener will no longer be
     *  notified when the change is executed, or the change fails.
     */
    public void removeChangeListener(ChangeListener listener) {
        if (_localListeners != null) {
            _localListeners.remove(listener);
        }
    }

    /** Set the description.
     *  @param description The description.
     *  @since Ptolemy II 3.1
     */
    public void setDescription(String description) {
        _description = description;
    }

    /** Call with a true argument to indicate that an error has been
     *  reported to the user. This is used by listeners to avoid reporting an
     *  error repeatedly.  By convention, a listener that reports the
     *  error to the user, in a dialog box for example, should call
     *  this method after reporting an error.  It should call
     *  isErrorReported() to determine whether it is necessary to report
     *  the error.
     *  @param reported True if an error has been reported.
     */
    public void setErrorReported(boolean reported) {
        _errorReported = reported;
    }

    /** Specify a list of listeners to be notified when changes are
     *  successfully executed, or when an attempt to execute them results
     *  in an exception.  The next time that execute() is called, all
     *  listeners on the specified list will be notified.
     *  This class has this method, in addition to the usual
     *  addChangeListener() and removeChangeListener() because it is
     *  assumed that the primary list of listeners is being maintained
     *  in another class, specifically the top-level named object in the
     *  hierarchy.  The listeners set with this method are notified
     *  after the listeners that are attached directly to this object.
     *  <p>
     *  The class copies the given list, so that in the process of handling
     *  notifications from this class, more listeners can be added to
     *  the list of listeners in the top-level object.
     *  <p>
     *  Note that an alternative to using listeners is to call
     *  waitForCompletion(), although this may cause undesirable
     *  synchronization between the different threads.
     *
     *  @param listeners A list of instances of ChangeListener or
     *   instances of WeakReference referring to instances of
     *   ChangeListener.
     *  @see ChangeListener
     *  @see NamedObj
     */
    public void setListeners(List listeners) {
        if (listeners != null) {
            _listeners = new LinkedList(listeners);
        }
    }

    /** Assert whether the change represented by this request is
     *  persistent.  Call this method with argument <i>false</i> to
     *  assert that the change does not affect the MoML representation
     *  of the model. This might, for example, guide a user
     *  interface, to determine whether to mark the model "modified,"
     *  and hence, whether to prompt the user to save the model if a
     *  window is closed.
     *  <p>
     *  It is up to the creator of the change request to call this
     *  method to assure that the change is not persistent. Calling
     *  this method with a <i>false</i> argument does not make the
     *  change non-persistent. It merely asserts that it is. There is
     *  no automatic detection of whether the change is persistent.
     *  By default, the change is assumed to be persistent, so unless
     *  this is called with argument <i>false</i>, a UI will likely
     *  mark the model modified upon execution of the change request.
     *
     *  @see #isPersistent()
     *  @param persistent False to indicate that the change represented
     *   by this request is not persistent.
     */
    public void setPersistent(boolean persistent) {
        _persistent = persistent;
    }

    /** Wait for execution (or failure) of this change request.
     *  The calling thread is suspended until the execute() method
     *  completes.  If an exception occurs processing the request,
     *  then this method will throw that exception.
     *  <p>
     *  Note that using this method may cause the model to deadlock
     *  and not be able to proceed.  This is especially true if it
     *  is called from the Swing thread, and any actors in the
     *  model (such as plotters) wait for swing events.
     *  @exception Exception If the execution of the change request
     *   throws it.
     */
    public final synchronized void waitForCompletion() throws Exception {
        while (_pending) {
            wait();
        }
        if (_exception != null) {
            // Note the use of fillInStackTrace, so that the exception
            // appears to come from within the change request.
            throw (Exception)(_exception.fillInStackTrace());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the change.  Derived classes must implement this method.
     *  Any exception may be thrown if the change fails.
     *  @exception Exception If the change fails.
     */
    protected abstract void _execute() throws Exception;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A description of the change.
    private String _description;

    // A flag indicating whether the error has been reported.
    private boolean _errorReported;

    // The exception thrown by the most recent call to execute(), if any.
    private Exception _exception;

    // A list of listeners or weak references to listeners
    // that are given in setListeners().
    private List _listeners;

    // A list of listeners that are maintained locally.
    private List _localListeners;

    // A flag indicating that this request has not been executed yet.
    private boolean _pending = true;

    // A flag indicating that this change is persistent.
    private boolean _persistent = true;

    // The source of the change request.
    private Object _source;
}
