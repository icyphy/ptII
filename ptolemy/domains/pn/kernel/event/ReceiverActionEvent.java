/* An event representing the various actions being taken on the receiver in PN.

Copyright (c) 1997-2000 The Regents of the University of California.
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

package ptolemy.domains.pn.kernel.event;
import ptolemy.domains.pn.kernel.*;


//////////////////////////////////////////////////////////////////////////
//// ReceiverActionEvent
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

public class ReceiverActionEvent {

    /** Create a new event and assign the variable corresponding to the
     *  capacity of the receiver to the current capacity of the receiver.
     *  @param The Receiver.
     */
    public ReceiverActionEvent(PNQueueReceiver r) {
        _receiver = r;
	_capacity = r.getCapacity();
    }

    /** Create a new event and set the receiver and its capacity parameters
     *  as specified.
     *  @param r The receiver corresponding to which this event is generated.
     *  @param c Current capacity of the receiver.
     */
    public ReceiverActionEvent(PNQueueReceiver r, int c) {
        _receiver = r;
        _capacity = c;
    }

    //////////////////////////////////////////////////////////////
    ////                    public methods                   /////

    /** Return the actor corresponding to the process that generated the event.
     */
    public PNQueueReceiver getReceiver() {
        return _receiver;
    }

    /** Return the capacity of the receiver.
     */
    public int getCapacity() {
        return _capacity;
    }

    //////////////////////////////////////////////////////////////
    ////                   private variables                 /////

    private PNQueueReceiver _receiver;
    private int _capacity;
}
