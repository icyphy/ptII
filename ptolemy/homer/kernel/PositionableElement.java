/* Abstract definition of elements. 
 
 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.homer.kernel;

import ptolemy.actor.gui.PortableContainer;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PositionableElement

/** Abstract definition of elements. 
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public abstract class PositionableElement {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Store the element that should have its location defined.
     * 
     *  @param element The object with the location defined.
     */
    public PositionableElement(NamedObj element) {
        _element = element;
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Get the location of the element.
     * 
     *  @return Return the location object of the element.
     *  @exception IllegalActionException If the location of the element
     *  is not defined.
     */
    public HomerLocation getLocation() throws IllegalActionException {
        HomerLocation location = (HomerLocation) getElement().getAttribute(
                HomerConstants.POSITION_NODE);
        // Check whether the location information is valid.
        location.validateLocation();
        return location;
    }

    /**
     * TODO
     * @param x
     * @param y
     * @param width
     * @param height
     * @throws IllegalActionException 
     */
    public void setLocation(int x, int y, int width, int height)
            throws IllegalActionException {
        Attribute attribute = getElement().getAttribute(
                HomerConstants.POSITION_NODE);
        HomerLocation location;
        if (attribute instanceof HomerLocation) {
            location = (HomerLocation) attribute;
        } else {
            getElement().removeAttribute(attribute);
            try {
                location = new HomerLocation(getElement(),
                        HomerConstants.POSITION_NODE);
            } catch (NameDuplicationException e) {
                // this can't happen.
                location = null;
            }
        }
        location.setLocation(x, y, width, height);
    }

    /** Get the location of the element.
     *  @return Return the location object of the element.
     */
    public String getTab() {
        Parameter tag = ((Parameter) _element
                .getAttribute(HomerConstants.TAB_NODE));

        // If the tab is not set, return default
        if (tag == null) {
            return HomerConstants.TAG;
        }

        return tag.getExpression();
    }

    /** Add the visual representation of the element to the provided
     *  container.
     * 
     *  @param container The container to place the representation in.
     *  @exception IllegalActionException If the element cannot be added
     *  to the container. 
     */
    public abstract void addToContainer(PortableContainer container)
            throws IllegalActionException;

    /** Get the underlying element.
     * 
     *  @return The Ptolemy element with Android specific location information.
     */
    public NamedObj getElement() {
        return _element;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The Ptolemy element.
     */
    private NamedObj _element;
}
