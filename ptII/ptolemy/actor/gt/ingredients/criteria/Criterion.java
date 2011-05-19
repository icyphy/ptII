/* A criterion to constrain an object in the host model.

 Copyright (c) 1997-2009 The Regents of the University of California.
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

package ptolemy.actor.gt.ingredients.criteria;

import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Criterion

/**
 A criterion to constrain an object in the host model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class Criterion extends GTIngredient {

    /** Construct a criterion within the given list as its owner containing a
     *  given number of elements. All elements are enabled at the beginning.
     *
     *  @param owner The list as the owner of the constructed GTIngredientList.
     *  @param elementCount The number of elements that the GTIngredient has.
     */
    public Criterion(GTIngredientList owner, int elementCount) {
        super(owner, elementCount);
    }

    /** Return whether this criterion can check the given object.
     *
     *  @param object The object.
     *  @return true if the object can be checked.
     */
    public boolean canCheck(NamedObj object) {
        return isApplicable(object);
    }

    /** Test whether the given object in the host model matches the object in
     *  the pattern that has this criterion.
     *
     *  @param object The object.
     *  @return true if the object matches.
     */
    public abstract boolean match(NamedObj object);
}
