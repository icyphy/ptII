/* A superdense time object consists of a time stamp and an index.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
//// SuperdenseTime

/**
 This class defines the structure of superdense time used in domains having
 time involved in computation. A superdense time object, s, consists of a time
 stamp and an index, denoted as s = (t, n).
 <p>
 Two superdense time objects can be compared to see which one happens first.
 The order is defined by the relationship between their time stamps and
 indexes. In particular, given s_1 = (t_1, n_1) and s_2 = (t_2, n_2), s_1
 happens earlier than s_2 (denoted as s_1 <= s_2), if t_1 < t_2 or (t_1 == t_2
 and n_1 <= n_2). The equality relation holds only if both t_1 == t_2 and
 n_1 == n_2 hold.

 @author Haiyang Zheng, Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class SuperdenseTime implements Comparable {

    /** Construct a superdense time object with the specified timestamp and
     *  index.
     *  @param timeStamp The time stamp.
     *  @param index The index.
     */
    public SuperdenseTime(Time timeStamp, int index) {
        _index = index;
        _timestamp = timeStamp;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare this superdense time object with the argument superdense
     *  time object for an order.
     *  The argument has to be a superdense time object.
     *  Otherwise, a ClassCastException will be thrown.
     *
     *  @param superdenseTime The superdense time object to compare against.
     *  @return -1, 0, or 1, depending on the order of the events.
     *  @exception ClassCastException If the argument is not a superdense
     *   time object.
     */
    public final int compareTo(Object superdenseTime) {
        return compareTo((SuperdenseTime) superdenseTime);
    }

    /** Compare this superdense time object with the argument superdense
     *  time object for an order. Return -1, 0, or 1 if this superdense
     *  time object happens earlier than, simultaneously with, or later than
     *  the argument superdense time object.
     *  <p>
     *  Their timestamps are compared first. If the two timestamps are not
     *  equal, their order defines the objects' order. Otherwise, the
     *  indexes are compared for the order, where the object with
     *  a smaller index happens earlier. If the two objects have the same
     *  timestamp and index, then they happen simultaneously.
     *
     *  @param superdenseTime The superdense time object to compare against.
     *  @return -1, 0, or 1, depends on the order.
     */
    public final int compareTo(SuperdenseTime superdenseTime) {
        if (_timestamp.compareTo(superdenseTime.timestamp()) > 0) {
            return 1;
        } else if (_timestamp.compareTo(superdenseTime.timestamp()) < 0) {
            return -1;
        } else if (_index > superdenseTime.index()) {
            return 1;
        } else if (_index < superdenseTime.index()) {
            return -1;
        } else {
            return 0;
        }
    }

    /** Return the index.
     *  @return The index.
     */
    public final int index() {
        return _index;
    }

    /** Return the timestamp.
     *  @return The timestamp.
     */
    public final Time timestamp() {
        return _timestamp;
    }

    /** Return a description of this superdense time object.
     *  @return A description of this superdense time object.
     */
    public final String toString() {
        return "Superdense Time: time stamp = " + _timestamp + " and index = "
                + _index + ".";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The index of this superdense time object.
    private int _index;

    // The timestamp of this superdense time object.
    private Time _timestamp;
}
