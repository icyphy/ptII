/* A class that compares two comparable objects.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.util.Comparator;

///////////////////////////////////////////////////////////////////
//// GeneralComparator

/**
 This class compares two comparable objects, object_1 and object_2,
 by calling <i>compare(object_1, object_2)</i>. A comparable object
 implements the {@link java.lang.Comparable} interface. This method returns
 -1, 0, or 1 if object_1 is less than, equal to, or bigger than object_2.

 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class GeneralComparator implements Comparator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return -1, 0, or 1 if the first object is less than, equal to, or
     *  bigger than the second object.
     *
     *  <p>If any of the argument is not a object of Comparable class, a
     *  ClassCastException will be thrown.
     *  @param first The first comparable object.
     *  @param second The second comparable object.
     *  @return The comparison result, -1, 0, or 1.
     */
    @Override
    public int compare(Object first, Object second) {
        return ((Comparable) first).compareTo(second);
    }
}
