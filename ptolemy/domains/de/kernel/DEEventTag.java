/* Define the sort key for events in the DE domain.

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
*/

package ptolemy.domains.de.kernel;


//////////////////////////////////////////////////////////////////////////
//// DEEventTag
/** Events in the Ptolemy II DE domain are associated with tags. The tags
 *  define an ordering relation between events. This class implements the 
 *  tag associated with each event. A DE event tag is an aggregation of 
 *  time stamp and receiver depth.
 *  <p>
 *  A class that implements the DEEventQueue interface implements how tags
 *  are compared with each other and thus performs the sorting of events.
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see DECQComparator
 */
public class DEEventTag {

    /** Construct a DEEventTag object with the given time stamp and receiver
     *  depth. Time stamp is a double quantity indicating the time when
     *  the event takes place. Receiver depth is a long quantity
     *  indicating the 'topological' depth of the IOport containing the
     *  receiver of the event. Receiver depths are useful for scheduling
     *  simultaneous events.
     *
     * @param timeStamp The time when the event occurs.
     * @param receiverDepth The topological depth of the destination receiver.
     *
     */
    public DEEventTag(double timeStamp, long receiverDepth) {
        _timeStamp = timeStamp;
        _receiverDepth = receiverDepth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the time stamp field of this sort key.
     *
     * @return The time stamp field.
     */
    public double timeStamp() {
        return _timeStamp;
    }

    /** Return the receiver depth field of this sort key.
     *
     * @return The receiver depth field.
     */
    public long receiverDepth() {
        return _receiverDepth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    // _timeStamp The time stamp of the event.
    private double _timeStamp;
    // _receiverDepth The depth of the destination receiver.
    private long _receiverDepth;

}
