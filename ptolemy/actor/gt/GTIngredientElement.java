/* Superclass of any element that can be contained in a GTIngredient.

@Copyright (c) 2007-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt;

//////////////////////////////////////////////////////////////////////////
//// GTIngredientElement

/**
 Superclass of any element that can be contained in a GTIngredient.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTIngredientElement {

    /** Construct an element that can be added to a GTIngredient.
     *
     *  @param name The name of the element.
     *  @param canDisable Whether this element can be disabled.
     */
    public GTIngredientElement(String name, boolean canDisable) {
        _name = name;
        _canDisable = canDisable;
    }

    /** Return whether this element can be disabled.
     *
     *  @return true if this element can be disabled; false otherwise.
     */
    public boolean canDisable() {
        return _canDisable;
    }

    /** Return the name of this element.
     *
     *  @return The name.
     */
    public String getName() {
        return _name;
    }

    /** Whether this element can be disabled.
     */
    private boolean _canDisable;

    /** The name of this element.
     */
    private String _name;
}
