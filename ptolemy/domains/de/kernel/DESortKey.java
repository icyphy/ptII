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
//// DESortKey
/** An event in Discrete Event domain is modeled as an instance of Token and
 *  an instance of DESortKey. DESortKey is an aggregation of time stamp 
 *  (double)and depth (long)
 *  <p>
 *  In a particular implementation of the global event queue, namely
 *  the CalendarQueue class, methods of the DECQComparator class are used to
 *  perform the sorting and arranging in the CalendarQueue.
 *  <p>
 *  FIXME: Support for other type of time stamp (e.g. long).

@author Lukito Muliadi
@version $Id$
@see DECQComparator
*/
public class DESortKey {

    /** Construct a DESortKey object with the given time stamp and receiver
     *  depth. Time stamp is a double quantity indicating the time when 
     *  the event takes place. Receiver depth is a long quantity 
     *  indicating the 'topological' depth of the IOport containing the 
     *  receiver of the event. Receiver depths are useful for scheduling 
     *  simultaneous events.
     * 
     * @param timeStamp the time when the event occurs.
     * @param receiverDepth 'topological' depth of the receiving receiver.
     * 
     */	
    public DESortKey(double timeStamp, long receiverDepth) {
        _timeStamp = timeStamp;
        _receiverDepth = receiverDepth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the time stamp field of the sort key.
     *
     * @return The time stamp field.
     */
    public double timeStamp() {
        return _timeStamp;
    }

    /** Return the receiver-depth field of the sort key.
     *
     * @return The receiver-depth field.
     */
    public long receiverDepth() {
        return _receiverDepth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    
    // FIXME: change double to Number ?
    // _timeStamp 
    private double _timeStamp;
    // _receiverDepth
    private long _receiverDepth;

}


