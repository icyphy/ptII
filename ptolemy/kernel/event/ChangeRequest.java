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

package ptolemy.kernel.event;

import ptolemy.kernel.util.*;

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
implements the execute() method to bring about the desired change.
The instance of that anonymous inner class is then queued with the
director or manager using the requestChange() method.
<p>
Alternatively, a set of concrete derived classes are provided that
implement a few of the more common sorts of mutations. These derived
classes can be instantiated and queued with the director or manager.
To ensure that a group of such changes is executed together,
use ChangeList to collect them, and queue the instance of
ChangeList with the director or manager.

@author  Edward A. Lee
@version $Id$
@see ChangeList
*/
public abstract class ChangeRequest {

    /** Construct a request with the specified originator and description.
     *  The description is a string that is used to report the change,
     *  typically to the user in a debugging environment.
     *  @param originator The source of the change request.
     *  @param description A description of the change request.
     */
    public ChangeRequest(Nameable originator, String description) {
        _description = description;
        _originator = originator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the change.  This method is defined in derived classes,
     *  which are specialized to particular changes.
     *  @exception ChangeFailedException If the change fails.
     */
    public abstract void execute() throws ChangeFailedException;

    /** Get the description.
     *  @return The description of the change.
     */
    public String getDescription() {
        return _description;
    }

    /** Get the source of the change request.
     *  @return The source of the change request.
     */
    public Nameable getOriginator() {
        return _originator;
    }

    /** Notify the specified listener of this change.
     *  In this base class, this just calls the changeExecuted() method of
     *  the listener.  This is defined so that derived classes can alter
     *  the way they notify listeners.  In particular, ChangeList will
     *  notify listeners of all changes that succeeded.
     *  @param listener The listener to notify.
     *  @see ChangeList
     */
    public void notify(ChangeListener listener) {
        listener.changeExecuted(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A description of the change.
    private String _description;

    // The source of the change request.
    private Nameable _originator;
}
