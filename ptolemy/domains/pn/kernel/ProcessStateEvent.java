/* An event representing the various changes in the state of a process in PN.

Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Yellow (mudit@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.domains.pn.kernel;
import ptolemy.kernel.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// ProcessStateEvent
/**
An event passed from a process executing under the PN semantics to a 
ProcessStateListener. This is used to
represent an event that happened during the execution of a topology.
This event contains two pieces of information:  the actor under the control
of the process and an exception that might be thrown. 
The exception might not be a valid reference.

@author Mudit Goel
@version $Id$
*/

public class ProcessStateEvent {

    /** Create a new event 
     *  @param The actor 
     */
    public ProcessStateEvent(Actor a) {
        _actor = a;
        _exception = null;
    }

    /** Create a new event that corresponds to an exception
     *  caught by the process.
     */
    public ProcessStateEvent(Actor a, Exception e) {
        _actor = a;
        _exception = e;
    }

    //////////////////////////////////////////////////////////////
    ////                    public methods                   /////

    /** Return the actor corresponding to the process that generated the event.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Return the exception associated with the event.
     */
    public Exception getException() {
        return _exception;
    }

    //////////////////////////////////////////////////////////////
    ////                   private variables                 /////

    private Actor _actor;
    private Exception _exception;
}










