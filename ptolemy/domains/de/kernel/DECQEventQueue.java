/* A calendar queue implementation of the DE event queue.

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

@ProposedRating red (lmuliadi@eecs.berkeley.edu)
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
//// DECQEventQueue
//
/** A calendar queue implementation of the DE event queue. Its complexity is
 *  O(1) for both enqueue and dequeue operations.
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Empty the event queue.
     */
    public synchronized void clear() {
        _cQueue.clear();
    }


    /** Return the DEEventTag associated with the earliest event in this
     *  event queue. Note that the event is not dequeued.
     *  @return The DEEventTag associated with the earliest event in this
     *  event queue.
     *  @exception IllegalAccessException.java If the queue is empty.
     */
    public DEEventTag getNextTag() throws IllegalAccessException {
        return (DEEventTag)_cQueue.getNextKey();
    }

    /** Enqueue an event into the event queue.
     */
    public synchronized void put(DEEvent event) {
        _cQueue.put(event.getEventTag(), event);
    }

    /** Dequeue the earliest event in this event queue.
     *  @return The DEEvent object associated with the earliest event in
     *    the queue.
     *  @exception IllegalAccessException.java If the queue is empty.
     */
    public synchronized DEEvent take() throws IllegalAccessException {
        return (DEEvent)_cQueue.take();
    }

    /** Return true if this event queue is empty.
     */
    public boolean isEmpty() {
        return _cQueue.isEmpty();
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
	public int compare(Object object1, Object object2) {

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
	public long getBinIndex(Object key,
                Object zeroReference, Object binWidth) {
	    DEEventTag a = (DEEventTag) key;
	    DEEventTag w = (DEEventTag) binWidth;
	    DEEventTag zero = (DEEventTag) zeroReference;

	    return (long)((a.timeStamp() - zero.timeStamp())/w.timeStamp());
	}


	/** Given an array of DEEventTag objects, find the appropriate bin
	 *  width. By 'appropriate', the bin width is chosen such that
         *  on average
	 *  the number of entry in all non-empty bins is equal to one.
	 *  If the argument is null, return the default bin width which is 1.0
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

	    if ( keyArray == null ) {
		return new DEEventTag(1.0, 0);
	    }

	    double[] diff = new double[keyArray.length - 1];

	    double average = 0;
	    for (int i = 1; i < keyArray.length; ++i) {
		diff[i-1] = ((DEEventTag)keyArray[i]).timeStamp() -
		    ((DEEventTag)keyArray[i-1]).timeStamp();
		average = average + diff[i-1];
	    }
	    average = average / diff.length;
	    double effAverage = 0;
	    int nEffSamples = 0;
	    for (int i = 1; i < keyArray.length; ++i) {
		if ( diff[i-1] < 2*average ) {
		    nEffSamples++;
		    effAverage = effAverage + diff[i-1];
		}
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






