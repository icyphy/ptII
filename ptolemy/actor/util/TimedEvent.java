/* An event that can be inserted in a CalendarQueue using a double as
   a sort key.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

//////////////////////////////////////////////////////////////////////////
//// TimedEvent
/**
This class aggregates a double and an Object, and provides a CQComparator
as an inner class.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
@see CQComparator
*/

public class TimedEvent {

    /** Construct an event with the specified time stamp and contents.
     *  @param time The time stamp.
     *  @param obj The contents
     */
    public TimedEvent(double time, Object obj) {
        timeStamp = time;
        contents = obj;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The time stamp. */
    public double timeStamp;

    /** The event object. */
    public Object contents;


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

            if ( a.timeStamp < b.timeStamp )  {
                return -1;
            } else if ( a.timeStamp > b.timeStamp ) {
                return 1;
            } else {
                return 0;
            }
        }

        /** Given an entry, return a virtual bin number for the entry.
         *  The calculation performed is:
         *  <p>
         *  <i>(entry.timeStamp - zeroReference) / binWidth</i>,
         *  </p>
         *  with the result cast to long.
         *  If the arguments are not instances of TimedEvent, then a
         *  ClassCastException will be thrown.
         *  @param entry The entry.
         *  @return The virtual bin number for the entry, according to the
         *   current zero reference and the bin width.
         *  @exception ClassCastException If the arguments are not instances of
         *   TimedEvent.
         */
        public long getVirtualBinNumber(Object entry) {
            return (long)((((TimedEvent)entry).timeStamp
                    - _zeroReference.timeStamp)/
                    _binWidth.timeStamp);
        }

        /** Given an array of TimedEvent objects, find the appropriate bin
         *  width. By 'appropriate', we mean that
         *  the bin width is chosen such that on average
         *  the number of entries in all non-empty bins is equal to one.
         *  If the argument is null, return the default bin width,
         *  which is 1.0 for this implementation.  If the argument
         *  is a length one array, then the single element in the array
         *  (an instance of TimedEvent) is made the bin width.
         *  Otherwise, the statistics of the elements of the array
         *  are analyzed to determine a reasonable bin width.
         *
         *  @param entryArray An array of TimedEvent objects.
         *  @exception ClassCastException If one of the array elements is not
         *   an instance of TimedEvent.
         */
        public void setBinWidth(Object[] entryArray) {
            if ( entryArray == null ) {
                // Reset to default.
                _binWidth = new TimedEvent(1.0, null);
                return;
            }
            if ( entryArray.length == 1) {
                _binWidth = (TimedEvent)entryArray[0];
                return;
            }

            double[] diff = new double[entryArray.length - 1];

            double average = 0;
            for (int i = 1; i < entryArray.length; ++i) {
                diff[i-1] = ((TimedEvent)entryArray[i]).timeStamp -
                    ((TimedEvent)entryArray[i-1]).timeStamp;
                average = average + diff[i-1];
            }
            average = average / diff.length;
            double effAverage = 0;
            int nEffSamples = 0;
            for (int i = 1; i < entryArray.length; ++i) {
                if ( diff[i-1] < 2*average ) {
                    nEffSamples++;
                    effAverage = effAverage + diff[i-1];
                }
            }
            // To avoid returning NaN or 0.0 for the width, if this is
            // the result, leave the bin width unchanged.
            if (effAverage == 0.0 || nEffSamples == 0) {
                return;
            }
            effAverage = effAverage / nEffSamples;
            _binWidth = new TimedEvent(3.0 * effAverage, null);
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

        // The bin width.
        private TimedEvent _binWidth = new TimedEvent(1.0, null);

        // The zero reference.
        private TimedEvent _zeroReference = new TimedEvent(0.0, null);
    }
}
