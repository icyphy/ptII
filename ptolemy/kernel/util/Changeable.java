/* Interface for objects that support deferrable change requests.

Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// Changeable
/**
   This is an interface for objects that support change requests that can
   be deferred.  A change request is any
   modification to a model that might be performed during execution of the
   model, but where there might only be certain phases of execution during
   which it is safe to make the modification.  Such changes are also called
   <i>mutations</i>.
   <p>
   A change request is typically made by instantiating a subclass of
   ChangeRequest (possibly using an anonymous inner class) and then passing
   to the requestChange() method of an object implementing this interface.
   That object may delegate the request (for example, it might consolidate
   all such requests at the top level of the hierarchy by passing the
   request to its container).  If it does delegate, then it is expected
   to consistently delegate all commands to the same object.
   <p>
   When a change request is made, if it is safe to do so, then an
   implementor of this interface is free to immediately execute
   the request, unless setDeferringChangeRequests(true) has been called.
   It is never safe to execute a change request of the implementor
   is already in the middle of executing a change request (that
   execution may have triggered the request).

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.0
   @see ChangeRequest
*/

public interface Changeable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a change listener. Each listener
     *  will be notified of the execution (or failure) of each
     *  change request that is executed via the requestChange() method.
     *  Implementors are free to delegate both the request and the
     *  listener to other objects (e.g. the container), so the listener
     *  may be notified of more changes than those requested through
     *  the requestChange() method of this object.
     *  @param listener The listener to add.
     *  @see #removeChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     */
    public void addChangeListener(ChangeListener listener);

    /** Execute requested changes. An implementor is free to delegate
     *  this to another implementor of Changeable. An implementor
     *  is expected to execute all pending change requests (even
     *  if setDeferringChangeRequests() has been
     *  called with a true argument).  Listeners will be notified
     *  of success or failure.
     *  @see #addChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see #setDeferringChangeRequests(boolean)
     */
    public void executeChangeRequests();

    /** Return true if setDeferringChangeRequests() has been called
     *  to specify that change requests should be deferred.
     *  @return True if change requests are being deferred.
     *  @see #setDeferringChangeRequests(boolean)
     */
    public boolean isDeferringChangeRequests();

    /** Remove a change listener, if it is present.
     *  @param listener The listener to remove.
     *  @see #addChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener listener);

    /** Request that given change be executed. An implementor is free
     *  to delegate this request to another object (e.g. the container).
     *  It may also execute the request immediately,
     *  unless setDeferChangeRequests() has been called. If
     *  setDeferChangeRequests() has been called with a true argument,
     *  then an implementor is expected to queue the request until
     *  either setDeferChangeRequests() is called with a false
     *  argument or executeChangeRequests() is called.
     *  If an implementor is already in the middle of executing a change
     *  request, then the implementor is expected to finish that
     *  execution before executing this one.
     *  Change listeners will be notified of success (or failure) of the
     *  request when it is executed.
     *  @param change The requested change.
     *  @see #executeChangeRequests()
     *  @see #setDeferChangeRequests(boolean)
     */
    public void requestChange(ChangeRequest change);

    /** Specify whether change requests made by calls to requestChange()
     *  should be executed immediately. An implementor is free to delegate
     *  this to another object implementing Changeable (e.g. the container).
     *  If the argument is true, then an implementor is expected to
     *  queue requests until either this method is called again
     *  with argument false, or until executeChangeRequests() is called.
     *  If the argument is false, then execute any pending change requests
     *  and set a flag requesting that future requests be executed
     *  immediately.
     *  @param isDeferring If true, defer change requests.
     *  @see #addChangeListener(ChangeListener)
     *  @see #executeChangeRequests()
     *  @see #isDeferringRequests()
     *  @see #requestChange(ChangeRequest)
     */
    public void setDeferringChangeRequests(boolean isDeferring);
}
