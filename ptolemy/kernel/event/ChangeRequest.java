/* Abstract base class for change requests.

 Copyright (c) 1998 The Regents of the University of California.
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
which it is safe to make the modification.  Instances of ChangeRequest
can be collected into a ChangeList and queued with the director
for later execution.  Use of a ChangeList ensures that all the changes
in the list are executed at the same time.

@author  Edward A. Lee
@version $Id$
@see ChangeList
*/
public abstract class ChangeRequest {

    /** Construct a request with the specified originator and description.
     *  @param originator The source of the change request.
     *  @param description A description of the change request.
     */	
    protected ChangeRequest(Nameable originator, String description) {
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

    /** Get the description
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
     *  In this base class, just call the changeExecuted() method of
     *  the listener.
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
