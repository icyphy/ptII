/* Extension of Comparator interface for CalendarQueue

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

import java.util.Comparator;

//////////////////////////////////////////////////////////////////////////
//// CQComparator
/**
This interface extends the java.util.Comparator interface, which
defines the compare() method. The extension defines additional methods
that specifically support the CalendarQueue class.  That class needs
to associate an entry in the queue with a virtual bin number, and
needs to be able to periodically recompute the width of its bins.
Thus, merely being able to compare entries is not sufficient.

@author Lukito Muliadi, Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.actor.util.CalendarQueue
@see java.util.Comparator
*/

public interface CQComparator extends Comparator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given an entry, return a virtual bin number for the entry.
     *  The virtual bin number is a quantized version of whatever
     *  key is being used to compare entries.
     *  The calculation performed should be something like:
     *  <p>
     *  <i>(entry - zeroReference) / binWidth</i>,
     *  </p>
     *  with the result cast to long.
     *
     *  <p> Classes that implement this interface will in general need
     *  to perform a downcast on the arguments (of type Object) to the
     *  appropriate user defined classes. If the arguments are not of
     *  appropriate type, the implementation should throw a
     *  ClassCastException.  Note that this method cannot return
     *  long.MAX_VALUE, which is used internally by the CalendarQueue.
     *
     *  @param entry An object that can be inserted in a calendar queue.
     *  @return The index of the bin.
     */
    public long getVirtualBinNumber(Object entry);

    /** Given an array of entries, set an appropriate bin width for a
     *  calendar queue to hold these entries.  This method assumes that the
     *  entries provided are all different, and are in increasing order.
     *  Ideally, the bin width is chosen so that
     *  the average number of entries in non-empty bins is equal to one.
     *  If the argument is null set the default bin width.
     *  @param entryArray An array of entries.
     */
    public void setBinWidth(Object[] entryArray);

    /** Set the zero reference, to be used in calculating the virtual
     *  bin number.
     *  @param zeroReference The starting point for bins.
     */
    public void setZeroReference(Object zeroReference);
}
