/* A RcvrComparator is used to sort receivers controlled by a time keeper.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Green (davisj@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;

import java.util.Comparator;

//////////////////////////////////////////////////////////////////////////
//// RcvrComparator
/**
A RcvrComparator is used to sort receivers controlled by a time keeper.
Receivers are sorted according to rcvrTime and priority using the
compare() method. First receivers are sorted according to rcvrTime.
RcvrTimes are partitioned into three categories: nonnegative times,
PrioritizedTimedQueue.IGNORE times and PrioritizedTimedQueue.INACTIVE
times. Nonnegative times precede IGNORE times precede INACTIVE times.
Within the nonnegative time category, smaller times precede larger
times. The IGNORE and INACTIVE categories each consist of a single
value so within these categories comparison results in equality.

If it is determined that two receivers are equivalent according to
rcvrTime, then comparison is made according to the receiver priority.
A larger receiver priority will precede a smaller receiver priority.
If it is determined that two receivers are equivalent according to
rcvrTime and priority, then the compare() method returns 0.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.DDEThread
*/
public class RcvrComparator implements Comparator {

    /**
     */
    public RcvrComparator(TimeKeeper cntlr) {
	_timeKeeper = cntlr;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare two receivers according to rcvrTime and priority.
     *  Return +1, -1 or 0 if the first receiver argument will be
     *  ordered before, after or equivalent to the second receiver
     *  argument, respectively. Base the ordering first on the
     *  rcvrTimes. If the rcvrTimes are equal, then base the
     *  ordering on the receiver priority.
     *  @exception ClassCastException If obj1 and obj2 are
     *   not instances of PrioritizedTimedQueue.
     */
    public int compare(Object obj1, Object obj2) {
	PrioritizedTimedQueue rcvr1 = null;
	PrioritizedTimedQueue rcvr2 = null;
        if( obj1 instanceof PrioritizedTimedQueue ) {
	    rcvr1 = (PrioritizedTimedQueue)obj1;
        }
        if( obj2 instanceof PrioritizedTimedQueue ) {
	    rcvr2 = (PrioritizedTimedQueue)obj2;
        }

	//
	// Compare Receiver Time
	//
	double time1 = rcvr1.getRcvrTime();
	double time2 = rcvr2.getRcvrTime();
	if( time1 == PrioritizedTimedQueue.IGNORE ||
		time2 == PrioritizedTimedQueue.IGNORE ) {
	    _timeKeeper._ignoredRcvrs = true;
	} else {
	    _timeKeeper._ignoredRcvrs = false;
	}

	// Compare Nonnegative Time with Negative Time
	if( time1 >= 0 && time2 < 0 ) {
	    return -1;
	} else if( time1 < 0 && time2 >= 0 ) {
	    return 1;
	}

	// Compare Nonnegative Time with Nonnegative Time
	if( time1 >= 0 && time2 >= 0 ) {
	    if( time1 < time2 ) {
		return -1;
	    } else if( time1 > time2 ) {
		return 1;
	    }
	}

	// Compare Negative Time with Negative Time
	if( time1 == PrioritizedTimedQueue.IGNORE &&
		time2 == PrioritizedTimedQueue.INACTIVE ) {
	    return -1;
	} else if( time1 == PrioritizedTimedQueue.INACTIVE &&
		time2 == PrioritizedTimedQueue.IGNORE ) {
	    return 1;
	}

	//
	// Compare Receiver Priority
	//
	int priority1 = rcvr1._priority;
	int priority2 = rcvr2._priority;

	if( priority1 > priority2 ) {
	    return -1;
	} else if( priority1 < priority2 ) {
	    return 1;
	}

	return 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods		   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    TimeKeeper _timeKeeper;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

}
