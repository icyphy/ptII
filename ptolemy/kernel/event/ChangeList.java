/* A list of change requests to execute all at once.

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

import java.util.Enumeration;
import collections.LinkedList;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// ChangeList
/**
A list of change requests to execute all at once.  To ensure that a
set of changes is executed in one step, between two iterations,
collect the requested changes in a ChangeList and register that
list with the manager or director.

@author  Edward A. Lee
@version $Id$
*/
public class ChangeList extends ChangeRequest {

    /** Construct a request with the specified originator and description.
     *  The change list is initially empty.
     *  @param originator The source of the change request.
     *  @param description A description of the batch of changes.
     */
    public ChangeList(Nameable originator, String description) {
        super(originator, description);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a change to the list.
     */
    public void add(ChangeRequest change) {
        if (_changes == null) {
            _changes = new LinkedList();
        }
        _changes.insertLast(change);
    }

    /** Execute each of the changes in the change list.
     *  If there are none, do nothing. If any of the changes fails
     *  with an exception, then only those changes up to the exception
     *  will have been executed.
     *  @exception ChangeFailedException If any of the change requests
     *   in the list throw it.
     */
    public void execute() throws ChangeFailedException {
        if (_changes != null) {
            Enumeration changes = _changes.elements();
            while (changes.hasMoreElements()) {
                ChangeRequest change = (ChangeRequest)changes.nextElement();
                change.execute();
            }
        }
    }

    /** Notify the specified listener of the changes in the list.
     *  I.e., call the changeExecuted() method of the listener once
     *  for each change in the list.
     */
    public void notify(ChangeListener listener) {
        if (_changes != null) {
            Enumeration changes = _changes.elements();
            while (changes.hasMoreElements()) {
                ChangeRequest change = (ChangeRequest)changes.nextElement();
                listener.changeExecuted(change);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of change requests.
    private LinkedList _changes;
}
