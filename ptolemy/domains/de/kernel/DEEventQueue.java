/* The interface for DE domain event queues.

Copyright (c) 1998-2004 The Regents of the University of California.
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
   This interface defines the global event queue used by DE directors
   to sort and manage DE events. DE events are sorted according to their time
   stamps, microstep and the depth of the destination actor.
   One DE event is said to be earlier than another, if it has
   a smaller time stamp, or when the time stamps are identical,
   it has a smaller microstep, or when both time stamps and
   microsteps are identical, it has a smaller depth.
   If all three entries are identical, the DE events are stored in
   the order they are added into this event queue.

   @author Lukito Muliadi, Jie Liu
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (liuj)
   @Pt.AcceptedRating Green (cxh)
   @see DEEvent
*/
public interface DEEventQueue extends Debuggable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Empty this event queue.
     */
    public void clear();

    /** Return the earliest DE event in this event queue.  
     *  Note that the DE event is not dequeued. 
     *  @return The earliest DE event.
     *  @exception InvalidStateException If the event queue is empty.
     */
    public DEEvent get() throws InvalidStateException;

    /** Return true if this event queue is empty.
     *  @return True if this queue is empty, false otherwise.
     */
    public boolean isEmpty();

    /** Enqueue a DE event into the event queue.
     *  @param deEvent The DE event to be put into the queue.
     *  @exception IllegalActionException If the event cannot be enqueued.
     */
    public void put(DEEvent deEvent) throws IllegalActionException;

    /** Return the size of the event queue.  
     *  @return The size of the event queue.
     */
    public int size();
    
    /** Dequeue the earliest DE event in this event queue.
     *  @return The earliest DE event in the event queue.
     *  @exception InvalidStateException If the queue is empty.
     */
    public DEEvent take() throws InvalidStateException;
}
