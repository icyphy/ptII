/* Controller for modal models.

   Copyright (c) 1999-2013 The Regents of the University of California.
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
package ptolemy.domains.modal.modal;

import java.util.Comparator;

import ptolemy.kernel.Entity;

///////////////////////////////////////////////////////////////////
//// ClassComparator


/**
   A comparator to compare classes, which is used to sort the map returned by
   the _getRefinementClasses() method in ModalRefinement and ModalController.

   @author Thomas Huining Feng
   @version $Id: ModalController.java 66458 2013-05-31 00:23:14Z cxh $
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
 class ClassComparator implements Comparator<Class<? extends Entity>> {

    /** Compare class1 and class2, and return -1 if class1 is a subclass of
     *  class2, 1 if class2 is a subclass of class1, and otherwise, the
     *  result of comparing the names of class1 and class2.
     *
     *  @param class1 The first class.
     *  @param class2 The second class.
     *  @return -1, 0, or 1 representing whether class1 is less than, equal
     *   to, or greater than class2.
     */
    public int compare(Class<? extends Entity> class1,
            Class<? extends Entity> class2) {
        if (!class1.equals(class2)) {
            if (class1.isAssignableFrom(class2)) {
                return 1;
            } else if (class2.isAssignableFrom(class1)) {
                return -1;
            }
        }
        return class1.getName().compareTo(class2.getName());
    }
}
