/* A comparator for RTOS events.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;

import ptolemy.actor.util.CQComparator;

/** A comparator for RTOS events. All parameters are fixed.
 *  
 *  @author Jie Liu
 *  @version $Id$
 */

public class RTOSEventComparator implements CQComparator {

    /** Compare the two argument for order. Return a negative integer,
     *  zero, or a positive integer if the first argument is less than,
     *  equal to, or greater than the second.
     *  Both arguments must be instances of RTOSEvent or a
     *  ClassCastException will be thrown.  The compareTo() method
     *  of the first argument is used to do the comparison.
     *
     * @param object1 The first event.
     * @param object2 The second event.
     * @return A negative integer, zero, or a positive integer if the first
     *  argument is less than, equal to, or greater than the second.
     * @exception ClassCastException If one of the arguments is not
     *  an instance of DEEvent.
     */
    public final int compare(Object object1, Object object2) {
        return((RTOSEvent) object1).compareTo(object2);
    }

    /** Given an event, return the virtual index of
     *  the bin that should contain the event.
     *  If the argument is not an instance of DEEvent, then a
     *  ClassCastException will be thrown.  Only the priority
     *  of the arguments is used.  The quantity returned is the
     *  quantized piority, i.e. the
     *  difference between the priority of the event and that of
     *  the zero reference, divided by the priority of the bin width.
     *  @param event The event.
     *  @return The index of the virtual bin containing the event.
     *  @exception ClassCastException If the argument is not
     *   an instance of RTOSEvent.
     */
    public final long getVirtualBinNumber(Object event) {
        return (long)((RTOSEvent) event).priority();
    }

    
    /** Do nothing.
     *
     *  @param entryArray An array of RTOSEvent objects.
     * 
     */
    public void setBinWidth(Object[] entryArray) {
    }

    /** Do nothing.
     */
    public void setZeroReference(Object zeroReference) {
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
        
    // The bin width.
    private RTOSEvent _binWidth = new RTOSEvent(null, null, 1, 0.0);
    
    // The zero reference.
    private RTOSEvent _zeroReference = new RTOSEvent(null, null, 0, 0.0);
    
}

            
