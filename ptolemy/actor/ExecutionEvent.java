/* An ExecutionEvent represents an event that happens during a Manager's
execution

Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// ExecutionEvent
/**
An ExecutionEvent is passed from a Manager to an ExecutionListener to
represent an event that happened during the execution of a topology.
This event contains three pieces of information:  the manager that created
the event, the toplevel iteration number during which the event occurred and
an exception.   Each object may or may not be a valid reference.

@author Steve Neuendorffer
@version $Id$
*/

public class ExecutionEvent {

    /** Create a new execution event originating from the specified manager.
     */
    public ExecutionEvent(Manager m) {
        _manager = m;
        _iteration = 0;
        _exception = null;
    }

    /** Create a new event that occurs during the specified toplevel
     *  iteration of the specified Manager's execution.
     */
    public ExecutionEvent(Manager m, int iteration) {
        _manager = m;
        _iteration = iteration;
        _exception = null;
    }

    /** Create a new execution event that corresponds to an exception
     *  caught by the manager during the specified iteration.
     */
    public ExecutionEvent(Manager m, int iteration, Exception e) {
        _manager = m;
        _iteration = iteration;
        _exception = e;
    }

    //////////////////////////////////////////////////////////////
    ////                    public methods                   /////

    /** Return the Manager that generated the event.
     */
    public Manager getManager() {
        return _manager;
    }

    /** Return the number of the toplevel iteration during which the event
     *  was generated.
     */
    public int getIteration() {
        return _iteration;
    }

    /** Return the exception associated with the event.
     */
    public Exception getException() {
        return _exception;
    }

    //////////////////////////////////////////////////////////////
    ////                   private variables                 /////

    private Manager _manager;
    private int _iteration;
    private Exception _exception;
}




