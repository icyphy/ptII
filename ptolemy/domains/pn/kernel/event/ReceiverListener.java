/* A ReceiverListener is able to receive ReceiverEvents from the Processes
which notifies the various actions done on the receivers.

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

//////////////////////////////////////////////////////////////////////////
//// ReceiverListener
/**
A ReceiverListener is able to receive ReceiverEvents that are issued
during the execution of a process by a ProcessThread or director in PN.
In general, an object that implements this interface will probably be a front
end such as a execution visualization tool for
the Ptolemy II system, or an object that is communicating with a front end.

@author Mudit Goel
@version $Id$
*/

public interface ReceiverListener {

    /** Called to indicate that the capacity of the receiver has been
     *  increased.
     *  @param event An event that has a reference to the receiver. The event
     *  also has the new capacity of the receiver.
     */
    public void receiverCapacityIncreased(ReceiverActionEvent event);

    /** Called to report that all the tokens currently in the receiver have
     *  been removed.
     *  @param event An event that has a reference to the receiver being
     *  cleared.
     */
    public void receiverClear(ReceiverActionEvent event);

    /** Called to report that a request to finish any process trying to read
     *  from or write to this receiver has been made.
     *  @param event An event that has a reference to the receiver on which
     *  the request has been made.
     */
    public void receiverFinishRequested(ReceiverActionEvent event);

    /** Called to report that a process removed a token from the receiver.
     *  @param event An event that has a reference to the receiver.
     */
    public void receiverGet(ReceiverActionEvent event);

    /** Called to report that a request to pause any process trying to read
     *  from or write to this receiver has been made.
     *  @param event An event that has a reference ot the receiver.
     */
    public void receiverPauseRequested(ReceiverActionEvent event);

    /** Called to indicate that a token has been put into the receiver by
     *  a process.
     *  @param event An event that has a reference to the receiver.
     */
    public void receiverPut(ReceiverActionEvent event);

    /** Called to indicate that a process blocked on the receiver while
     *  trying to read from it.
     *  @param event An event that has a reference to the receiver.
     */
    public void receiverReadBlocked(ReceiverActionEvent event);

    /** Called to indicate that a process blocked while trying to read from
     *  this receiver can resume now.
     *  @param event An event that has a reference to the receiver.
     */
    public void receiverReadUnblocked(ReceiverActionEvent event);

    /** Called to indicate that a request to resume any process that might
     *  have paused earlier while trying to read from or write to this
     *  receiver has been made.
     *  @param event An event that has a reference to the receiver.
     */
    public void receiverResumeRequested(ReceiverActionEvent event);

    /** Called to indicate that a process blocked on the receiver while trying
     *  to read from the receiver.
     *  @param event An event that has a reference to the receiver.
     */
    public void receiverWriteBlocked(ReceiverActionEvent event);

    /** Called to indicate that a process that was blocked while trying to
     *  read from this receiver can resume now.
     *  @param event An event that has a reference to the receiver.
     */
    public void receiverWriteUnblocked(ReceiverActionEvent event);

}
