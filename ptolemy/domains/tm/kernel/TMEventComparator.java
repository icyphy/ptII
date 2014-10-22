/* A comparator for TM events.

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
package ptolemy.domains.tm.kernel;

import ptolemy.actor.util.CQComparator;

///////////////////////////////////////////////////////////////////
//// TMEventComparator

/**
 A comparator for TM events. This class extends CQComparator so that
 it can be used by CalendarQueue. This class ignores all the configuration
 parameters in CQComparator. Only the default parameters are used.

 @author Jie Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (liuj)
 @Pt.AcceptedRating Yellow (janneck)
 */
public class TMEventComparator implements CQComparator {
    /** Compare the two argument for order. Return -1, 0, or 1
     *  if the first argument is less than,
     *  equal to, or greater than the second.
     *  Both arguments must be instances of TMEvent or a
     *  ClassCastException will be thrown.  The compareTo() method
     *  of the first argument is used to do the comparison.
     *
     * @param object1 The first event.
     * @param object2 The second event.
     * @return -1, 0, or 1 if the first
     *  argument is less than, equal to, or greater than the second.
     * @exception ClassCastException If one of the arguments is not
     *  an instance of TMEvent.
     */
    @Override
    public final int compare(Object object1, Object object2) {
        return ((TMEvent) object1).compareTo(object2);
    }

    /** Given an event, return the virtual index of
     *  the bin that should contain the event.
     *  If the argument is not an instance of TMEvent, then a
     *  ClassCastException will be thrown.  Only the priority
     *  of the arguments is used.  The quantity returned is the
     *  quantized priority, i.e. the
     *  difference between the priority of the event and that of
     *  the zero reference, divided by the priority of the bin width.
     *  @param event The event.
     *  @return The index of the virtual bin containing the event.
     *
     */
    @Override
    public final long getVirtualBinNumber(Object event) {
        return ((TMEvent) event).priority();
    }

    /** Do nothing.
     *
     *  @param entryArray An array of TMEvent objects.
     *
     */
    @Override
    public void setBinWidth(Object[] entryArray) {
    }

    /** Do nothing.
     *  @param zeroReference The zero reference of the comparator.
     */
    @Override
    public void setZeroReference(Object zeroReference) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
}
