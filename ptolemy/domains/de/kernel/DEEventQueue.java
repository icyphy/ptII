/* The interface for DE domain event queue.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEEventQueue
//
/** This interface defines the global event queue used by the DE director
 *  to sort and manage events. Events are sorted according to their time
 *  stamps. In the case of tie, they are sorted according to their
 *  destination receiver depth. If both the time stamps and receiver depths
 *  are equal then events that were enqueued first are dequeued first.
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 *  @see DEDirector
 */
public interface DEEventQueue {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     */
    public void addDebugListener(DebugListener listener);

    /** Empty this event queue.
     */
    public void clear();

    /** Return the DEEventTag associated with the earliest event in this
     *  event queue. Note that the event is not dequeued.
     *  @return Return the DEEventTag associated with the earliest event
     *    in this queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public DEEventTag getNextTag() throws IllegalActionException;

    /** Return true if this event queue is empty.
     *  @return True if this queue is empty, false otherwise.
     */
    public boolean isEmpty();

    /** Enqueue an event into the event queue.
     *  @param event The event to be put into the queue.
     */
    public void put(DEEvent event);

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener);

    /** Dequeue the earliest event in this event queue.
     *  @return The DEEvent object associated with the earliest event in
     *    the queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public DEEvent take() throws IllegalActionException;
}
