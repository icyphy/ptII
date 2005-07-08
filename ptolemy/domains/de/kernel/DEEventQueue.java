/* The interface for DE domain event queues.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

 */
package ptolemy.domains.de.kernel;

import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// DEEventQueue

/**
 This interface defines an event queue used by DE directors to sort and
 manage DE events.
 <p>
 DE events are sorted according to their timestamps, microsteps, and then the
 depths of the destination actors. One DE event is said to be earlier than
 another, if it has a smaller timestamp, or when the timestamps are
 identical, it has a smaller microstep, or when both time stamps and
 microsteps are identical, it has a smaller depth. If all three entries are
 identical, then these two DE events are called identical.
 <p>
 This interface defines a few methods to manage the event queue, including
 adding a new event into the queue, getting the earliest event of the queue.
 A correct implementation of this interface should not allow identical
 events. In particular, when adding a new event, the event is not added if
 the event is already in the queue. Also note that calling the get() method
 does not delete events from event queue but calling the take() method does.

 @author Lukito Muliadi, Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 @see DEEvent
 */
public interface DEEventQueue extends Debuggable {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Empty this event queue.
     */
    public void clear();

    /** Return the earliest DE event in this event queue.
     *  Note that the DE event is not deleted.
     *  @return The earliest DE event.
     *  @exception InvalidStateException If the event queue is empty.
     */
    public DEEvent get() throws InvalidStateException;

    /** Return true if this event queue is empty.
     *  @return True if this queue is empty, false otherwise.
     */
    public boolean isEmpty();

    /** Enqueue a DE event into the event queue. If the event is already
     *  contained in the queue, this method does nothing.
     *  @param event The DE event to be put into the queue.
     *  @exception IllegalActionException If the event cannot be enqueued.
     */
    public void put(DEEvent event) throws IllegalActionException;

    /** Return the size of the event queue.
     *  @return The size of the event queue.
     */
    public int size();

    /** Return the earliest DE event in this event queue. The returned event
     *  is deleted from the event queue.
     *  @return The earliest DE event in the event queue.
     *  @exception InvalidStateException If the queue is empty.
     */
    public DEEvent take() throws InvalidStateException;
}
