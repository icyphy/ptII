/* Abstract base class for change requests.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ChangeRequest
/**
Abstract base class for change requests.  A change request is any
modification to a model that might be performed during execution of the
model, but where there might only be certain phases of execution during
which it is safe to make the modification.  Such changes are called
<i>mutations</i>.
<p>
A typical use of this class is to define an anonymous inner class that
implements the _execute() method to bring about the desired change.
The instance of that anonymous inner class is then queued with a
a an instance of NamedObj using its requestChange() method.
<p>
Concrete derived classes can be defined to implement
mutations of a certain kind or in a certain way. Instances of these
classes should be queued with a NamedObj, just like an anonymous
inner class extending this class. MoMLChangeRequest is such a concrete
derived class, where the mutation is specified as MoML code.

@author  Edward A. Lee
@version $Id$
@see ChangeListener
*/
public abstract class ChangeRequest {

    /** Construct a request with the specified originator and description.
     *  The description is a string that is used to report the change,
     *  typically to the user in a debugging environment.
     *  The originator is the source of the change request.
     *  A listener to changes will probably want to check the originator
     *  so that when it is notified of errors or successful completion
     *  of changes, it can tell whether the change is one it requested.
     *  @param originator The originator of the change request.
     *  @param description A description of the change request.
     */
    public ChangeRequest(Object originator, String description) {
        _originator = originator;
        _description = description;
        _errorReported = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change.  This method invokes the protected method
     *  _execute(), reports to listeners (if any) that have been
     *  specified using the setListeners() method, and wakes up any
     *  threads that might be waiting in a call to waitForCompletion().
     */
    public synchronized void execute() {
        _exception = null;
        boolean needToReport = false;
        try {
            _execute();
        } catch (Exception ex) {
            needToReport = true;
            _exception = ex;
        }
        if (_listeners != null) {
            Iterator listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                ChangeListener listener = (ChangeListener)listeners.next();
                if (_exception == null) {
                    listener.changeExecuted(this);
                } else {
                    needToReport = false;
                    listener.changeFailed(this, _exception);
                }
            }
        }
        if (needToReport) {
            // There are no listeners, so if there was an exception,
            // we print it to standard error.
            if (_exception != null) {
                System.err.println(
                        "Exception occurred executing change request:");
                _exception.printStackTrace();
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

    /** Get the originator that was specified in the constructor.
     *  @return The originator of the change.
     */
    public Object getOriginator() {
        return _originator;
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
     *  This class has this single method, rather than the usual
     *  addChangeListener() and removeChangeListener() because it is
     *  assumed that the list of listeners is being maintained in another
     *  class, specifically the top-level named object in the hierarchy.
     *  The class copies the list, so that in the process of handling
     *  notifications from this class, more listeners can be added to
     *  the list of listeners in the top-level object.
     *  <p>
     *  Note that an alternative to using listeners is to call
     *  waitForCompletion().
     *
     *  @param listeners A list of instances of ChangeListener.
     *  @see ChangeListener
     *  @see NamedObj
     */
    public void setListeners(List listeners) {
        _listeners = new LinkedList(listeners);
    }

    /** Wait for execution (or failure) of this change request.
     *  The calling thread is suspended until the execute() method
     *  completes.  If an exception occurs processing the request,
     *  then this method will throw that exception.
     *  @exception Exception If the execution of the change request
     *   throws it.
     */
    public synchronized void waitForCompletion() throws Exception {
        while (_pending) {
            wait();
        }
        if (_exception != null) {
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

    // The exception thrown by the most recent call to execute(), if any.
    private Exception _exception;

    // A list of listeners.
    private List _listeners;

    // A flag indicating whether the error has been reported.
    private boolean _errorReported;

    // The originator of the change request.
    private Object _originator;

    // A flag indicating that a request is pending.
    private boolean _pending = true;
}
