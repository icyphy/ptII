/* A calendar queue implementation of the DE event queue.

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

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.util.CQComparator;
import ptolemy.actor.util.CalendarQueue;
import ptolemy.actor.util.Time;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// DECQEventQueue

/**
 A calendar queue implementation of the DE event queue.
 This queue stores DE events in the order of their timestamps,
 microsteps, and then depths of their destination actors. See
 {@link DEEventQueue} for more explanation of the order of DE events.
 <P>
 Its complexity is theoretically O(1) for both enqueue and dequeue
 operations, assuming a reasonable distribution of timestamps. See
 {@link ptolemy.actor.util.CalendarQueue}.

 @author Lukito Muliadi, Edward A. Lee, Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class DECQEventQueue implements DEEventQueue {
    /** Construct an empty event queue.
     *  @param director The director that contains this event queue.
     */
    public DECQEventQueue(Director director) {
        _director = director;

        try {
            // Construct a calendar queue _cQueue with its default parameters:
            // minBinCount is 2, binCountFactor is 2, and isAdaptive is true.
            _cQueue = new CalendarQueue(new DECQComparator());
        } catch (IllegalActionException e) {
            // If the time resolution of the director is invalid,
            // it should have been caught before this.
            throw new InternalErrorException(e);
        }
    }

    /** Construct an empty event queue with the specified parameters.
     *  @param director The director that contains this event queue.
     *  @param minBinCount The minimum number of bins.
     *  @param binCountFactor The factor when changing the bin count.
     *  @param isAdaptive If the queue changes its number of bins at run time.
     */
    public DECQEventQueue(Director director, int minBinCount,
            int binCountFactor, boolean isAdaptive) {
        _director = director;

        try {
            // Construct a calendar queue _cQueue with the given parameters.
            _cQueue = new CalendarQueue(new DECQComparator(), minBinCount,
                    binCountFactor);
        } catch (IllegalActionException e) {
            // If the time resolution of the director is invalid,
            // it should have been caught before this.
            throw new InternalErrorException(e);
        }

        _cQueue.setAdaptive(isAdaptive);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  @param listener A listener to which to send debug messages.
     *  @see #removeDebugListener(DebugListener)
     */
    public void addDebugListener(DebugListener listener) {
        _cQueue.addDebugListener(listener);
    }

    /** Empty the event queue. This method is synchronized since there
     *  may be actors running under different threads in the DE domain.
     */
    public void clear() {
        _cQueue.clear();
    }

    /** Return the earliest DE event in the queue without removing it
     *  from the queue.
     *  @return The earliest DE event in the queue.
     *  @exception InvalidStateException If the queue is empty.
     */
    public final DEEvent get() {
        return (DEEvent) _cQueue.get();
    }

    /** Return true if this event queue is empty.
     *  @return True if there are no event in the queue.
     */
    public final boolean isEmpty() {
        return _cQueue.isEmpty();
    }

    /** Put an event into the event queue.
     *  If the given DE event is not in the event queue, enqueue it
     *  into the event queue and notify all threads
     *  that are stalled waiting for a DE event to be put in the queue.
     *  This method is synchronized since there
     *  may be actors running under different threads in the DE domain.
     *  @param event The event to enqueue.
     */
    public synchronized final void put(DEEvent event) {
        if (!_cQueue.includes(event)) {
            _cQueue.put(event);
            notifyAll();
        }
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     *  @see #addDebugListener(DebugListener)
     */
    public void removeDebugListener(DebugListener listener) {
        _cQueue.removeDebugListener(listener);
    }

    /** Return the size of the event queue.
     *  @return The size of the event queue.
     */
    public final int size() {
        return _cQueue.size();
    }

    /** Dequeue the earliest DE event in this event queue.
     *  @return The earliest DE event in the queue.
     *  @exception InvalidStateException If the queue is empty.
     */
    public final DEEvent take() {
        return (DEEvent) _cQueue.take();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////
    // An implementation of the CQComparator interface for use with
    // calendar queue that compares two DEEvents according to their
    // time stamps, microstep, and depth in that order.
    // One DE event is said to be earlier than another, if it has
    // a smaller time stamp, or when the time stamps are identical,
    // it has a smaller microstep, or when both time stamps and
    // microsteps are identical, it has a smaller depth.
    // The default binWidth is 1.0, and the default zeroReference is 0.0.
    private class DECQComparator implements CQComparator {
        /** Construct a new comparator.
         *  @exception IllegalActionException If the time resolution
         *  of the director is invalid.
         */
        public DECQComparator() throws IllegalActionException {
            // This constructor exists only to declare the thrown exception.
            super();
        }

        /** Compare two arguments for order. Return a negative integer,
         *  zero, or a positive integer if the first argument is less than,
         *  equal to, or greater than the second.
         *  Both arguments must be instances of DEEvent. Otherwise a
         *  ClassCastException will be thrown.  The compareTo() method
         *  of the first argument is used to do the comparison.
         *
         * @param object1 The first DE event.
         * @param object2 The second DE event.
         * @return A negative integer, zero, or a positive integer if the first
         *  argument is less than, equal to, or greater than the second.
         * @exception ClassCastException If any of the arguments is not
         *  an instance of DEEvent.
         */
        public final int compare(Object object1, Object object2) {
            return ((DEEvent) object1).compareTo((DEEvent) object2);
        }

        /** Given a DE event, return the virtual index of the bin that
         *  should contain this event. If the argument is not an instance
         *  of DEEvent, then a ClassCastException will be thrown.
         *  If the bin number is larger than what can be represented
         *  in a long, then the low-order 64 bits will be returned.
         *  Note that this could change the sign of the result, but
         *  the way this is used in the CalendarQueue class, this is OK.
         *  It is converted to a bin number by masking some number of
         *  low-order bits, so the result will be unaffected by the
         *  sign error.
         *  Note that this method cannot return a long.MAX_VALUE, which
         *  is used internally by CalendarQueue class.
         *  @param event The event.
         *  @return The index of the virtual bin containing the event.
         *  @exception ClassCastException If the argument is not
         *   an instance of DEEvent.
         */
        public final long getVirtualBinNumber(Object event) {
            // NOTE: The longValue() method will only
            // returns the low-order 64 bits of the result.
            // If it is larger than what can be represented
            // in 64 bits, then the returned result will be wrapped.
            return (((DEEvent) event).timeStamp().subtract(_zeroReference
                    .timeStamp())).divide(_binWidth.timeStamp());
        }

        /** Given an array of DE events, set an appropriate bin width.
         *  This method assumes that no two DE events have the same time stamp,
         *  microstep, and depth. This method also assumes that the events
         *  are sorted in a time-increasing order. {@link DEEventQueue}.
         *  Note that even the time stamps may not be increasing,
         *  the receiver depths, or the microsteps may be increasing.
         *  This method attempts to choose the bin width so that
         *  the average number of entries in a bin is one.
         *  If the argument is null or is an array with length less
         *  than two, set the bin width to the default, which is 1.0
         *  for this implementation.
         *
         *  @param entryArray An array of DE events.
         *  @exception ClassCastException If any entry in the array is not
         *  an instance of DEEvent.
         */
        public void setBinWidth(Object[] entryArray) {
            try {
                if ((entryArray == null) || (entryArray.length < 2)) {
                    _zeroReference = new DEEvent((Actor) null, new Time(
                            _director, 0.0), 0, 0);
                    return;
                }

                Time[] diff = new Time[entryArray.length - 1];
                Time average = (((DEEvent) entryArray[entryArray.length - 1])
                        .timeStamp().subtract(((DEEvent) entryArray[0])
                        .timeStamp())).divide((long) (entryArray.length - 1));
                Time zero = new Time(_director, 0.0);
                Time effectiveAverage = zero;
                int effectiveSamples = 0;

                if (average.isInfinite()) {
                    return;
                }

                for (int i = 0; i < (entryArray.length - 1); ++i) {
                    diff[i] = ((DEEvent) entryArray[i + 1]).timeStamp()
                            .subtract(((DEEvent) entryArray[i]).timeStamp());

                    if (diff[i].compareTo(average.add(average)) < 0) {
                        effectiveSamples++;
                        effectiveAverage = effectiveAverage.add(diff[i]);
                    }
                }

                if (effectiveAverage.equals(zero) || (effectiveSamples == 0)) {
                    // To avoid setting NaN or 0.0
                    // for the width, apparently due to simultaneous events,
                    // we leave it unchanged instead.
                    return;
                }

                effectiveAverage = effectiveAverage
                        .divide((long) effectiveSamples);
                _binWidth = new DEEvent((Actor) null, effectiveAverage
                        .multiply(3L), 0, 0);
            } catch (IllegalActionException e) {
                // If the time resolution of the director is invalid,
                // it should have been caught before this.
                throw new InternalErrorException(e);
            }
        }

        /** Set the zero reference, to be used in calculating the virtual
         *  bin number. The argument should be a DE event, otherwise a
         *  ClassCastException will be thrown.
         *  @exception ClassCastException If the argument is not an instance
         *   of DEEvent.
         */
        public void setZeroReference(Object zeroReference) {
            _zeroReference = (DEEvent) zeroReference;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private members                   ////
        // The bin width.
        private DEEvent _binWidth = new DEEvent((Actor) null, new Time(
                _director, 1.0), 0, 0);

        // The zero reference.
        private DEEvent _zeroReference = new DEEvent((Actor) null, new Time(
                _director, 0.0), 0, 0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // An instance of CalendarQueue used for sorting and storing events.
    private CalendarQueue _cQueue;

    // The director that contains this event queue.
    private Director _director;
}
