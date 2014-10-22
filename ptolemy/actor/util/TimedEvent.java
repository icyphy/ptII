/* An event that can be inserted in a CalendarQueue using an instance of Time
 as a sort key.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
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
public class TimedEvent implements Comparable<TimedEvent> {
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
    @Override
    public String toString() {
        return "timeStamp: " + timeStamp + ", contents: " + contents;
    }

    /** Return true if this TimedEvent object has the same
     *  timeStamp and eventObject  as the given TimedEvent object.
     *  @param timedEvent The TimedEvent object that this
     *  TimedEvent object is compared to.
     *  @return True if the two TimedEvent objects have the same time
     *  stamp and event object.
     */
    @Override
    public boolean equals(Object timedEvent) {
        // See http://www.technofundo.com/tech/java/equalhash.html

        /* FindBugs says that TimedEvent "defined
         * compareTo(Object) and uses Object.equals()"
         * http://findbugs.sourceforge.net/bugDescriptions.html#EQ_COMPARETO_USE_OBJECT_EQUALS
         * says: "This class defines a compareTo(...) method but
         * inherits its equals() method from
         * java.lang.Object. Generally, the value of compareTo should
         * return zero if and only if equals returns true. If this is
         * violated, weird and unpredictable failures will occur in
         * classes such as PriorityQueue. In Java 5 the
         * PriorityQueue.remove method uses the compareTo method,
         * while in Java 6 it uses the equals method.
         *
         *  From the JavaDoc for the compareTo method in the
         *  Comparable interface:
         *
         * It is strongly recommended, but not strictly required that
         * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking,
         * any class that implements the Comparable interface and
         * violates this condition should clearly indicate this
         * fact. The recommended language is "Note: this class has a
         * natural ordering that is inconsistent with equals." "
         */
        if (timedEvent == this) {
            return true;
        }
        if (timedEvent == null || timedEvent.getClass() != getClass()) {
            return false;
        } else {
            TimedEvent event = (TimedEvent) timedEvent;
            if (compareTo(event) == 0 && contents.equals(event.contents)) {
                return true;
            }
        }
        return false;
    }

    /** Compare two TimedEvents by comparing their timestamps.
     *  @param timedEvent The event to compare against.
     *  @return The integer -1, 0, or 1 if this is less than, equal to, or
     *   greater than the argument.
     */
    @Override
    public int compareTo(TimedEvent timedEvent) {
        return timeStamp.compareTo(timedEvent.timeStamp);
    }

    /** Return the hash code for the TimedEvent object. If two
     *  TimedEvent objects contains the same timestamp,
     *  and event object, then they will have the same hashCode.
     *  @return The hash code for this TimedEvent object.
     */
    @Override
    public int hashCode() {
        int hashCode = 21;
        if (timeStamp != null) {
            hashCode = 31 * hashCode + timeStamp.hashCode();
        }
        if (contents != null) {
            hashCode = 31 * hashCode + contents.hashCode();
        }
        return hashCode;
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
        /** Construct a TimeComparator object.
         */
        public TimeComparator() {
            _binWidth = 1;
            _zeroReference = 0.0;
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
        @Override
        public int compare(Object object1, Object object2) {
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
         */
        @Override
        public long getVirtualBinNumber(Object entry) {
            // Note: The longValue() method will only
            // returns the low-order 64 bits of the result.
            // If it is larger than what can be represented
            // in 64 bits, then the returned result will be wrapped.
            long value = (long) (((TimedEvent) entry).timeStamp.subtract(
                    _zeroReference).getLongValue() / _binWidth);
            if (value != Long.MAX_VALUE) {
                return value;
            } else {
                return Long.MAX_VALUE - 1;
            }
        }

        /** Given an array of TimedEvent objects, find the appropriate bin
         *  width. By 'appropriate', we mean that
         *  the bin width is chosen such that on average
         *  the number of entries in all non-empty bins is equal to one.
         *  If the argument is null, return the default bin width,
         *  which is 1.0 for this implementation.
         *  Otherwise, the statistics of the elements of the array
         *  are analyzed to determine a reasonable bin width.
         *
         *  @param entryArray An array of TimedEvent objects.
         *  @exception ClassCastException If one of the array elements is not
         *   an instance of TimedEvent.
         */
        @Override
        public void setBinWidth(Object[] entryArray) {
            if (entryArray == null) {
                // Reset to default.
                _binWidth = 1;
                _zeroReference = 0.0;
                return;
            }

            double[] diff = new double[entryArray.length - 1];

            Time firstEntryTime = ((TimedEvent) entryArray[0]).timeStamp;
            Time lastEntryTime = ((TimedEvent) entryArray[entryArray.length - 1]).timeStamp;

            if (firstEntryTime.isInfinite()
                    && firstEntryTime.equals(lastEntryTime)) {
                // To avoid setting NaN or 0.0
                // for the width, apparently due to simultaneous events,
                // we leave it unchanged instead.
                return;
            }

            double average = lastEntryTime.subtract(firstEntryTime)
                    .getDoubleValue();
            average = average / (entryArray.length - 1);

            double effAverage = 0.0;
            int nEffSamples = 0;

            for (int i = 1; i < entryArray.length; ++i) {
                if (diff[i - 1] < 2 * average) {
                    nEffSamples++;
                    effAverage = effAverage + diff[i - 1];
                }
            }

            // To avoid returning NaN or 0.0 for the width, if this is
            // the result, leave the bin width unchanged.
            if (effAverage == 0 || nEffSamples == 0) {
                return;
            }

            effAverage = effAverage / nEffSamples;
            _binWidth = effAverage * 3;
        }

        /** Set the zero reference, to be used in calculating the virtual
         *  bin number.
         *  @exception ClassCastException If the argument is not an instance
         *   of TimedEvent.
         */
        @Override
        public void setZeroReference(Object zeroReference) {
            _zeroReference = ((TimedEvent) zeroReference).timeStamp
                    .getDoubleValue();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private members                   ////

        // The bin width.
        private double _binWidth;

        // The zero reference.
        private double _zeroReference;
    }

}
