/* A calendar queue implementation of the DE event queue.

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
import ptolemy.actor.util.CalendarQueue; // For javadoc
import ptolemy.data.*;
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DECQEventQueue
//
/** A calendar queue implementation of the DE event queue. Its complexity is
 *  O(1) for both enqueue and dequeue operations, assuming a reasonable
 *  distribution of time stamps.
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 *  @see DEDirector
 */
public class DECQEventQueue implements DEEventQueue {

    /** Construct a director with empty string as name in the
     *  default workspace.
     */
    public DECQEventQueue() {
        _cQueue = new CalendarQueue(new DECQComparator());
        // Uncomment this to disable reallocation of bins when
        // queue size changes.
        // _cQueue.setAdaptive(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     */
    public void addDebugListener(DebugListener listener) {
        _cQueue.addDebugListener(listener);
    }

    /** Empty the event queue.
     */
    public synchronized void clear() {
        _cQueue.clear();
    }

    /** Return the DEEventTag associated with the earliest event in this
     *  event queue. Note that the event is not dequeued.
     *  @return The DEEventTag associated with the earliest event in this
     *  event queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public synchronized final DEEventTag getNextTag()
            throws IllegalActionException {
        return (DEEventTag)_cQueue.getNextKey();
    }

    /** Return true if this event queue is empty.
     */
    public synchronized final boolean isEmpty() {
        return _cQueue.isEmpty();
    }

    /** Enqueue an event into the event queue.
     */
    public final synchronized void put(DEEvent event) {
        _cQueue.put(event.getEventTag(), event);
        notifyAll();
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener) {
        _cQueue.removeDebugListener(listener);
    }

    /** Dequeue the earliest event in this event queue.
     *  @return The DEEvent object associated with the earliest event in
     *    the queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public final synchronized DEEvent take() throws IllegalActionException {
        return (DEEvent)_cQueue.take();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////

    // An implementation of the CQComparator interface for use with
    // calendar queue with DEEventTag as the sort key.
    private class DECQComparator implements CQComparator {

	/** Compare its two argument for order. Return a negative integer,
	 *  zero, or a positive integer as the first argument is less than,
	 *  equal to, or greater than the second.
	 *  <p>
	 *  Both arguments have to be instances of DEEventTag, otherwise a
	 *  ClassCastException will be thrown.
	 *  <p>
	 *  The comparison is done based on their time stamps, and in case the
	 *  time stamps are equal, then their receiverDepth values is used.
	 * @param object1 the first DEEventTag argument
	 * @param object2 the second DEEventTag argument
	 * @return a negative integer, zero, or a positive integer as the first
	 *         argument is less than, equal to, or greater than the second.
	 * @exception ClassCastException object1 and object2 have to be
         *         instances
	 *            of DEEventTag
	 */
	public final int compare(Object object1, Object object2) {

	    DEEventTag a = (DEEventTag) object1;
	    DEEventTag b = (DEEventTag) object2;

	    if ( a.timeStamp() < b.timeStamp() )  {
		return -1;
	    } else if ( a.timeStamp() > b.timeStamp() ) {
		return 1;
	    } else if ( a.receiverDepth() < b.receiverDepth() ) {
		return -1;
	    } else if ( a.receiverDepth() > b.receiverDepth() ) {
		return 1;
	    } else {
		return 0;
	    }
        }

	/** Given a key, a zero reference, and a bin width, return the index of
	 *  the bin containing the key.
	 *  <p>
	 *  If the arguments are not instances of DEEventTag, then a
	 *  ClassCastException will be thrown.
	 *  @param key the key
	 *  @param zeroReference the zero reference.
	 *  @param binWidth the width of the bin
	 *  @return The index of the bin containing the key, according to the
	 *          zero reference, and the bin width.
	 *  @exception ClassCastException Arguments need to be instances of
	 *          DEEventTag.
	 */
	public final long getBinIndex(Object key,
                Object zeroReference, Object binWidth) {
	    DEEventTag a = (DEEventTag) key;
	    DEEventTag w = (DEEventTag) binWidth;
	    DEEventTag zero = (DEEventTag) zeroReference;

	    return (long)((a.timeStamp() - zero.timeStamp())/w.timeStamp());
	}

	/** Given an array of DEEventTag objects, return an appropriate bin
	 *  width. This method assumes that the
         *  keys provided are all different, and are in increasing order.
         *  Note, however, that the time stamps may not be increasing.
         *  It may instead be the receiver depth that is increasing.
         *  Ideally, the bin width is chosen so that
	 *  the average number of entries in bins is equal to one.
	 *  If the argument is null or is an array with length less
         *  than two, return the default bin width, which is 1.0
	 *  for this implementation.
	 *  <p>
	 *  If the argument is not an instance of DEEventTag[], then a
	 *  ClassCastException will be thrown.
	 *
	 *  @param keyArray an array of DEEventTag objects.
	 *  @return The bin width.
	 *  @exception ClassCastException keyArray need to be an array of
	 *          DEEventTag.
	 *
	 */
	public Object getBinWidth(Object[] keyArray) {

	    if ( keyArray == null || keyArray.length < 2) {
		return new DEEventTag(1.0, 0);
	    }

	    double[] diff = new double[keyArray.length - 1];

	    double average = 0.0;
	    for (int i = 0; i < keyArray.length - 1; ++i) {
		diff[i] = ((DEEventTag)keyArray[i+1]).timeStamp() -
		    ((DEEventTag)keyArray[i]).timeStamp();
		average += diff[i];
	    }
	    average /= diff.length;
	    double effAverage = 0.0;
	    int nEffSamples = 0;
	    for (int i = 0; i < keyArray.length - 1; ++i) {
		if (diff[i] < 2*average) {
		    nEffSamples++;
		    effAverage = effAverage + diff[i];
		}
	    }
            // To avoid returning NaN or 0.0
            // for the width, apparently due to simultaneous events,
            // we return the default instead.  This is probably not really 
            // right... instead the bin width should be left unchanged.
            if (effAverage == 0.0 || nEffSamples == 0) {
                return new DEEventTag(1.0, 0);
            }
	    effAverage = effAverage / nEffSamples;
	    return new DEEventTag(3.0 * effAverage, 0);
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //_cQueue: an instance of CalendarQueue is used for sorting.
    private CalendarQueue _cQueue = new CalendarQueue(new DECQComparator());

}
