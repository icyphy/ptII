/* The interface for DE domain event queue.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// DEEventQueue
//
/** This interface defines the global event queue used by DE directors
 *  to sort and manage events. Events are sorted according to their time
 *  stamps, microstep and the depth of the destination actor.
 *  One DEEvent is said to be earlier than another, if it has
 *  a smaller time stamp, or when the time stamps are identical,
 *  it has a smaller microstep, or when both time stamps and
 *  microsteps are identical, it has a smaller depth.
 *  If both three entries are are identical, the events are stored in
 *  a FIFO way.
 *
 *  @author Lukito Muliadi, Jie Liu
 *  @version $Id$
 *  @see DEReceiver
 *  @see ptolemy.actor.util.CalendarQueue
 *  @see DEDirector
 */
public interface DEEventQueue extends Debuggable{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Empty this event queue.
     */
    public void clear();

    /** Return the event associated with the earliest tag in this
     *  event queue. Note that the event is not dequeued.
     *  @return Return the earliest event.
     *  @exception IllegalActionException If the queue is empty.
     */
    public DEEvent get() throws IllegalActionException;

    /** Return true if this event queue is empty.
     *  @return True if this queue is empty, false otherwise.
     */
    public boolean isEmpty();

    /** Enqueue an event into the event queue.
     *  @param event The event to be put into the queue.
     *  @exception IllegalActionException If the event cannot be
     *      enqueued.
     */
    public void put(DEEvent event) throws IllegalActionException;

    /** Dequeue the earliest event in this event queue.
     *  @return The DEEvent object associated with the earliest event in
     *    the queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public DEEvent take() throws IllegalActionException;
}
