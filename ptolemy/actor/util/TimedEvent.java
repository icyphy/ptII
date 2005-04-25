/* An event that can be inserted in a CalendarQueue using an instance of Time
   as a sort key.

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
package ptolemy.actor.util;

import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;


//////////////////////////////////////////////////////////////////////////
//// TimedEvent

/**
   This class aggregates an instance of Time and an Object, and provides a CQComparator
   as an inner class.

   @author Edward A. Lee and Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.4
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (liuj)
   @see CQComparator
   @see Time
*/
public class TimedEvent {
    /** Construct an event with the specified time stamp and contents.
     *  @param time The time stamp.
     *  @param obj The contents.
     */
    public TimedEvent(Time time, Object obj) {
        timeStamp = time;
        contents = obj;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The time stamp. */
    public Time timeStamp;

    /** The event object. */
    public Object contents;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Display timeStamp and contents. */
    public String toString() {
        return "timeStamp: " + timeStamp + ", contents: " + contents;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// TimeComparator

    /**
     * This class implements the CQComparator interface. It compares instances
     * of TimedEvent. Therefore, all arguments passed to its methods have
     * to be of type TimedEvent (or TimedEvent[] for the getBinWidth() method).
     * If this is violated, ClassCastException will be thrown.
     */
    public static class TimeComparator implements CQComparator {
        /** Construct a TimeComparator object for the given director.
         *  @param director The director this comparator is associated with.
         *  @exception IllegalActionException If the specified director has
         *   an invalid time precision.
         */
        public TimeComparator(Director director) throws IllegalActionException {
            _director = director;
            _binWidth = new Time(_director, 1.0);
            _zeroReference = new TimedEvent(new Time(_director, 0.0), null);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Compare the two arguments. Return a negative integer,
         *  zero, or a positive integer depending on whether
         *  the first argument is less than,
         *  equal to, or greater than the second.
         *  Both arguments have to be instances of TimedEvent, otherwise a
         *  ClassCastException will be thrown.
         *  @param object1 The first event.
         *  @param object2 The second event.
         *  @return -1, 0, or +1 depending on whether the first
         *   argument is less than, equal to, or greater than the second.
         *  @exception ClassCastException If either argument is not an instance
         *   of TimedEvent.
         */
        public final int compare(Object object1, Object object2) {
            TimedEvent a = (TimedEvent) object1;
            TimedEvent b = (TimedEvent) object2;
            return a.timeStamp.compareTo(b.timeStamp);
        }

        /** Given an entry, return a virtual bin number for the entry.
         *  The calculation performed is:
         *  <p>
         *  <i>(entry.timeStamp - zeroReference) / binWidth</i>,
         *  </p>
         *  with the result cast to long.
         *  If the arguments are not instances of TimedEvent, then a
         *  ClassCastException will be thrown.
         *  If the bin number is larger than what can be represented
         *  in a long, then the low-order 64 bits will be returned.
         *  Note that this could change the sign of the result, but
         *  the way this is used in the CalendarQueue class, this is OK.
         *  It is converted to a bin number by masking some number of
         *  low-order bits, so the result will be unaffected by the
         *  sign error.
         *  @param entry The entry.
         *  @return The virtual bin number for the entry, according to the
         *   current zero reference and the bin width.
         *  @exception ClassCastException If the arguments are not instances of
         *   TimedEvent.
         *  @exception InternalErrorException If the specified director has
         *   an invalid time precision.
         */
        public long getVirtualBinNumber(Object entry) {
            // NOTE: The longValue() method will only
            // returns the low-order 64 bits of the result.
            // If it is larger than what can be represented
            // in 64 bits, then the returned result will be wrapped.
            long value = (((TimedEvent) entry).timeStamp.subtract(_zeroReference.timeStamp))
                            .divide(_binWidth);
            return value;

            // What used to be here:

            /*
                        return (long) (((TimedEvent) entry).timeStamp.subtract(_zeroReference.timeStamp)
                                .getDoubleValue() / _binWidth);
            */
        }

        /** Given an array of TimedEvent objects, find the appropriate bin
         *  width. By 'appropriate', we mean that
         *  the bin width is chosen such that on average
         *  the number of entries in all non-empty bins is equal to one.
         *  If the argument is null, return the default bin width,
         *  which is 1.0 for this implementation.  If the argument
         *  is a length-one array, then the single element in the array
         *  (an instance of TimedEvent) is made the bin width.
         *  Otherwise, the statistics of the elements of the array
         *  are analyzed to determine a reasonable bin width.
         *
         *  @param entryArray An array of TimedEvent objects.
         *  @exception ClassCastException If one of the array elements is not
         *   an instance of TimedEvent.
         *  @exception InternalErrorException If the specified director has
         *   an invalid time precision.
         */
        public void setBinWidth(Object[] entryArray) {
            try {
                if (entryArray == null) {
                    // Reset to default.
                    _binWidth = new Time(_director, 1.0);
                    return;
                }

                if (entryArray.length == 1) {
                    _binWidth = ((TimedEvent) entryArray[0]).timeStamp;
                    return;
                }

                Time[] diff = new Time[entryArray.length - 1];

                Time zero = new Time(_director, 0.0);
                Time average = zero;

                for (int i = 1; i < entryArray.length; ++i) {
                    diff[i - 1] = ((TimedEvent) entryArray[i]).timeStamp
                                    .subtract(((TimedEvent) entryArray[i - 1]).timeStamp);
                    average = average.add(diff[i - 1]);
                }

                average = average.divide((long) diff.length);

                Time effAverage = zero;
                int nEffSamples = 0;

                for (int i = 1; i < entryArray.length; ++i) {
                    if (diff[i - 1].compareTo(average.add(average)) < 0) {
                        nEffSamples++;
                        effAverage = effAverage.add(diff[i - 1]);
                    }
                }

                // To avoid returning NaN or 0.0 for the width, if this is
                // the result, leave the bin width unchanged.
                if ((effAverage.equals(zero)) || (nEffSamples == 0)) {
                    return;
                }

                effAverage = effAverage.divide((long) nEffSamples);
                _binWidth = effAverage.multiply(3L);
            } catch (IllegalActionException e) {
                // Malformed time resolution would have been caught by now.
                throw new InternalErrorException(e);
            }
        }

        /** Set the zero reference, to be used in calculating the virtual
         *  bin number.
         *  @exception ClassCastException If the argument is not an instance
         *   of TimedEvent.
         */
        public void setZeroReference(Object zeroReference) {
            _zeroReference = (TimedEvent) zeroReference;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private members                   ////
        // The director that contains this comparator.
        private Director _director;

        // The bin width.
        private Time _binWidth;

        // The zero reference.
        private TimedEvent _zeroReference;
    }
}
