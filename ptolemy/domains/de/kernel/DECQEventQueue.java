/* A calendar queue implementation of the DE event queue.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.actor.util.CQComparator;
import ptolemy.actor.util.CalendarQueue;

//////////////////////////////////////////////////////////////////////////
//// DECQEventQueue
//
/** A calendar queue implementation of the DE event queue. It store DE
 *  events in the order of their time stamps, microstep and the depth
 *  of the destination actor. One DEEvent is said to be earlier than
 *  another, if it has
 *  a smaller time stamp, or when the time stamps are identical,
 *  it has a smaller microstep, or when both time stamps and
 *  microsteps are identical, it has a smaller depth.
 *  <P>
 *  Its complexity is
 *  theoretically O(1) for both enqueue and dequeue operations, assuming
 *  a reasonable distribution of time stamps.
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 *  @see DEDirector
 */
public class DECQEventQueue implements DEEventQueue {

    /** Construct an empty event queue.  The calender queue takes its
     *  default parameter, i.e. minBinCount is 2, binCoundFactor is 2,
     *  and isAdaptive is true.
     */
    public DECQEventQueue() {
        _cQueue = new CalendarQueue(new DECQComparator());
    }

    /** Construct an empty event queue with a minimum bin number
     *  a bin count factor, and a boolean indicating whether the
     *  queue is adaptive.
     *  @param minBinCount The minimum number of bins.
     *  @param binCountFactor The factor when changing the bin count.
     *  @param isAdaptive If the queue changes its number of bins
     *     at run time.
     */
    public DECQEventQueue(int minBinCount, int binCountFactor,
            boolean isAdaptive) {
        _cQueue = new CalendarQueue(new DECQComparator(),
                minBinCount, binCountFactor);
        _cQueue.setAdaptive(isAdaptive);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do nothing.
     *  @param listener A listener to which to send debug messages.
     */
    public void addDebugListener(DebugListener listener) {
        _cQueue.addDebugListener(listener);
    }

    /** Empty the event queue. This method is synchronized since there
     *  may be actors running under different threads in the DE domain.
     */
    public synchronized void clear() {
        _cQueue.clear();
    }

    /** Return the smallest event in the queue without dequeueing it.
     *  This method is synchronized since there
     *  may be actors running under different threads in the DE domain.
     *  @return The smallest event in the queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public synchronized final DEEvent get() throws IllegalActionException {
        return (DEEvent)_cQueue.get();
    }

    /** Return true if this event queue is empty.
     *  @return True if the queue is empty.
     */
    public synchronized final boolean isEmpty() {
        return _cQueue.isEmpty();
    }

    /** Enqueue an event into the event queue and notify all threads
     *  that are stalled waiting for an event to be put in the queue.
     *  This method is synchronized since there
     *  may be actors running under different threads in the DE domain.
     *  @param event The event to enqueue.
     */
    public synchronized final void put(DEEvent event) {
        _cQueue.put(event);
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
     *  This method is synchronized since there
     *  may be actors running under different threads in the DE domain.
     *  @return The earliest event in the queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public synchronized final DEEvent take() throws IllegalActionException {
        return (DEEvent)_cQueue.take();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////

    // An implementation of the CQComparator interface for use with
    // calendar queue that compares two DEEvents according to there
    // time stamps, microstep, and depth in that order.
    // One DEEvent is said to be earlier than another, if it has
    // a smaller time stamp, or when the time stamps are identical,
    // it has a smaller microstep, or when both time stamps and
    // microsteps are identical, it has a smaller depth.
    private class DECQComparator implements CQComparator {

	/** Compare the two argument for order. Return a negative integer,
	 *  zero, or a positive integer if the first argument is less than,
	 *  equal to, or greater than the second.
	 *  Both arguments must be instances of DEEvent or a
	 *  ClassCastException will be thrown.  The compareTo() method
         *  of the first argument is used to do the comparison.
         *
	 * @param object1 The first event.
	 * @param object2 The second event.
	 * @return A negative integer, zero, or a positive integer if the first
	 *  argument is less than, equal to, or greater than the second.
	 * @exception ClassCastException If one of the arguments is not
         *  an instance of DEEvent.
	 */
	public final int compare(Object object1, Object object2) {
	    return((DEEvent) object1).compareTo(object2);
        }

	/** Given an event, return the virtual index of
	 *  the bin that should contain the event.
	 *  If the argument is not an instance of DEEvent, then a
	 *  ClassCastException will be thrown.  Only the time stamp
         *  of the arguments is used.  The quantity returned is the
         *  difference between the time stamp of the key and that of
         *  the zero reference divided by the time stamp of the bin width.
	 *  @param event The event.
	 *  @return The index of the virtual bin containing the key.
	 *  @exception ClassCastException If the argument is not
         *   an instance of DEEvent.
	 */
	public final long getVirtualBinNumber(Object event) {
	    return (long)((((DEEvent) event).timeStamp()
                    - _zeroReference.timeStamp())/_binWidth.timeStamp());
	}

	/** Given an array of DEEvent objects, set an appropriate bin
	 *  width. This method assumes that the
         *  entries provided are all different, and are in increasing order.
         *  Note, however, that the time stamps may not be increasing.
         *  It may instead be the receiver depth that is increasing.
         *  This method attempts to choose the bin width so that
	 *  the average number of entries in a bin is one.
	 *  If the argument is null or is an array with length less
         *  than two, return the default bin width, which is 1.0
	 *  for this implementation.
	 *
	 *  @param entryArray An array of DEEvent objects.
	 *  @exception ClassCastException If an entry in the array is not
         *   an instance of DEEvent.
	 */
	public void setBinWidth(Object[] entryArray) {

	    if ( entryArray == null || entryArray.length < 2) {
		_zeroReference = new DEEvent(null, 0.0, 0, 0);
                return;
	    }

	    double[] diff = new double[entryArray.length - 1];

	    double average = 0.0;
	    for (int i = 0; i < entryArray.length - 1; ++i) {
		diff[i] = ((DEEvent)entryArray[i+1]).timeStamp() -
		    ((DEEvent)entryArray[i]).timeStamp();
		average += diff[i];
	    }
	    average /= diff.length;
	    double effAverage = 0.0;
	    int nEffSamples = 0;
	    for (int i = 0; i < entryArray.length - 1; ++i) {
		if (diff[i] < 2*average) {
		    nEffSamples++;
		    effAverage = effAverage + diff[i];
		}
	    }
            // To avoid returning NaN or 0.0
            // for the width, apparently due to simultaneous events,
            // we leave it unchanged instead.
            if (effAverage == 0.0 || nEffSamples == 0) {
                return;
            }
	    effAverage = effAverage / nEffSamples;
            _binWidth = new DEEvent(null, 3.0 * effAverage, 0, 0);
	}

        /** Set the zero reference, to be used in calculating the virtual
         *  bin number.
         *  @exception ClassCastException If the argument is not an instance
         *   of DEEvent.
         */
        public void setZeroReference(Object zeroReference) {
            _zeroReference = (DEEvent) zeroReference;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private members                   ////

        // The bin width.
        private DEEvent _binWidth = new DEEvent(null, 1.0, 0, 0);

        // The zero reference.
        private DEEvent _zeroReference = new DEEvent(null, 0.0, 0, 0);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The instance of CalendarQueue used for sorting.
    private CalendarQueue _cQueue = new CalendarQueue(new DECQComparator());
}
