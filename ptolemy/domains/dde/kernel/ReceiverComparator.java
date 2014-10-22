/* A ReceiverComparator is used to sort receivers controlled by a time keeper.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.dde.kernel;

import java.util.Comparator;

///////////////////////////////////////////////////////////////////
//// ReceiverComparator

/**
 A ReceiverComparator is used to sort receivers controlled by a time keeper.
 Receivers are sorted according to ReceiverTime and priority using the
 compare() method. First receivers are sorted according to ReceiverTime.
 ReceiverTimes are partitioned into three categories: nonnegative times,
 PrioritizedTimedQueue.IGNORE times and PrioritizedTimedQueue.INACTIVE
 times. Nonnegative times precede IGNORE times precede INACTIVE times.
 Within the nonnegative time category, smaller times precede larger
 times. The IGNORE and INACTIVE categories each consist of a single
 value so within these categories comparison results in equality.

 <p>If it is determined that two receivers are equivalent according to
 ReceiverTime, then comparison is made according to the receiver priority.
 A larger receiver priority will precede a smaller receiver priority.
 If it is determined that two receivers are equivalent according to
 ReceiverTime and priority, then the compare() method returns 0.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (davisj)
 @Pt.AcceptedRating Green (kienhuis)
 @see ptolemy.domains.dde.kernel.DDEThread
 */
public class ReceiverComparator implements Comparator {
    /** Construct a ReceiverComparator.
     *  @param timeKeeper  The time keeper that controls the receivers.
     */
    public ReceiverComparator(TimeKeeper timeKeeper) {
        _timeKeeper = timeKeeper;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare two receivers according to ReceiverTime and priority.
     *  @param object1 The first object to be compared.
     *  @param object2 The second object to be compared.
     *  @return  +1, -1 or 0 if the first receiver argument will be
     *  ordered before, after or equivalent to the second receiver
     *  argument, respectively. Base the ordering first on the
     *  ReceiverTimes. If the ReceiverTimes are equal, then base the
     *  ordering on the receiver priority.
     *  @exception ClassCastException If object1 and obj2 are
     *   not instances of PrioritizedTimedQueue.
     */
    @Override
    public int compare(Object object1, Object object2) {
        PrioritizedTimedQueue receiver1 = null;
        PrioritizedTimedQueue receiver2 = null;

        if (object1 instanceof PrioritizedTimedQueue) {
            receiver1 = (PrioritizedTimedQueue) object1;

            if (object2 instanceof PrioritizedTimedQueue) {
                receiver2 = (PrioritizedTimedQueue) object2;
            } else {
                throw new ClassCastException(object2
                        + " is not a PrioritizedTimedQueue.");
            }
        } else {
            throw new ClassCastException(object1
                    + " is not a PrioritizedTimedQueue.");
        }

        //
        // Compare Receiver Time
        //
        double time1 = receiver1.getReceiverTime().getDoubleValue();
        double time2 = receiver2.getReceiverTime().getDoubleValue();

        if (time1 == PrioritizedTimedQueue.IGNORE
                || time2 == PrioritizedTimedQueue.IGNORE) {
            _timeKeeper._ignoredReceivers = true;
        } else {
            _timeKeeper._ignoredReceivers = false;
        }

        // Compare Nonnegative Time with Negative Time
        if (time1 >= 0 && time2 < 0) {
            return -1;
        } else if (time1 < 0 && time2 >= 0) {
            return 1;
        }

        // Compare Nonnegative Time with Nonnegative Time
        if (time1 >= 0 && time2 >= 0) {
            if (time1 < time2) {
                return -1;
            } else if (time1 > time2) {
                return 1;
            }
        }

        // Compare Negative Time with Negative Time
        if (time1 == PrioritizedTimedQueue.IGNORE
                && time2 == PrioritizedTimedQueue.INACTIVE) {
            return -1;
        } else if (time1 == PrioritizedTimedQueue.INACTIVE
                && time2 == PrioritizedTimedQueue.IGNORE) {
            return 1;
        }

        //
        // Compare Receiver Priority
        //
        int priority1 = receiver1._priority;
        int priority2 = receiver2._priority;

        if (priority1 > priority2) {
            return -1;
        } else if (priority1 < priority2) {
            return 1;
        }

        return 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private TimeKeeper _timeKeeper;
}
