/* Define the sort key for events in the DE domain.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import java.lang.Comparable;

//////////////////////////////////////////////////////////////////////////
//// DEEventTag
/** Events in the Ptolemy II DE domain are associated with tags. The tags
 *  define an ordering relation between events. This class implements the
 *  tag associated with each event. It has a time stamp, a microstep,
 *  and a receiver depth.  These are compared in order by the compareTo()
 *  method.
 *
 *  @author Lukito Muliadi, Edward A. Lee
 *  @version $Id$
 *  @see DECQEventQueue
 */
public class DEEventTag implements Comparable {

    /** Construct a DEEventTag object with the given time stamp, microstep,
     *  and receiver depth. The time stamp is a double quantity indicating
     *  the time at which the event takes place. The microstep is an
     *  integer indicating phase of execution within a fixed time.
     *  The receiver depth is an integer
     *  indicating the topological depth of the IOport containing the
     *  receiver of the event (larger depth implies lower priority when
     *  processing events). Microsteps and receiver depths are used
     *  to process simultaneous events in a deterministic way.
     *
     * @param timeStamp The time when the event occurs.
     * @param microstep The phase of execution within a fixed time.
     * @param receiverDepth The topological depth of the destination receiver.
     *
     */
    public DEEventTag(double timeStamp, int microstep, int receiverDepth) {
        _timeStamp = timeStamp;
        _microstep = microstep;
        _receiverDepth = receiverDepth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare this tag with the specified tag for order.
     *  Return a negative integer, zero, or a positive integer if this
     *  tag is less than, equal to, or greater than the specified object.
     *  The time stamp is checked first.  If the two time stamps are
     *  identical, then the microstep is checked.  If those are identical,
     *  then the receiver depth is checked.
     *  The argument has to be an instance of DEEventTag or a
     *  ClassCastException will be thrown.
     *
     * @param tag The tag to compare against.
     * @exception ClassCastException If the argument is not an instance
     *  of DEEventTag
     */
     public final int compareTo(Object tag) {

         DEEventTag castTag = (DEEventTag) tag;

         if ( _timeStamp < tag._timeStamp)  {
             return -1;
         } else if ( _timeStamp > tag._timeStamp) {
             return 1;
         } else if ( _microstep < tag._microstep) {
             return -1;
         } else if ( _microstep > tag._microstep) {
             return 1;
         } else if ( _receiverDepth < tag._receiverDepth) {
             return -1;
         } else if ( _receiverDepth > tag._receiverDepth) {
             return 1;
         } else {
             return 0;
         }
     }

    /** Return the microstep field of this sort key.
     *  @return The microstep field.
     */
    public final int microstep() {
        return _microstep;
    }

    /** Return the receiver depth field of this sort key.
     *  @return The receiver depth field.
     */
    public final int receiverDepth() {
        return _receiverDepth;
    }

    /** Return the time stamp field of this sort key.
     *  @return The time stamp field.
     */
    public final double timeStamp() {
        return _timeStamp;
    }

    /** Return information about the tag, including both the event
     *  time stamp and the depth.
     *  @return A string describing the tag.
     */
    public String toString() {
        return "DEEventTag(" + _timeStamp + ", " + _receiverDepth + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The microstep.
    private int _microstep;

    // The depth of the destination receiver.
    private int _receiverDepth;

    // The time stamp of the event.
    private double _timeStamp;
}
