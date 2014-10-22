/* Abstract definition of elements.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

import ptolemy.actor.injection.PortableContainer;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// PositionableElement

/** Abstract definition of elements.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
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
    ////                         public methods                    ////

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

    /** Get the location of the element.
     *
     *  @return Return the location object of the element.
     *  @exception IllegalActionException If the location of the element
     *  is not defined.
     *  @see #setLocation(int, int, int, int)
     */
    public HomerLocation getLocation() throws IllegalActionException {
        HomerLocation location = (HomerLocation) getElement().getAttribute(
                HomerConstants.POSITION_NODE);
        // Check whether the location information is valid.
        location.validateLocation();
        return location;
    }

    /** Get the location of the element.
     *
     *  @return Return the location object of the element.
     *  @see #setTab(String)
     */
    public String getTab() {
        Settable tag = (Settable) _element
                .getAttribute(HomerConstants.TAB_NODE);

        // If the tab is not set, return default
        if (tag == null) {
            return HomerConstants.TAG;
        }

        return tag.getExpression();
    }

    /** Set the location of this element's representation.
     *
     *  @param x The x position of the top-left corner.
     *  @param y The y position of the top-left corner.
     *  @param width The width of the representation.
     *  @param height The height of the representation.
     *  @exception IllegalActionException If the location cannot be set.
     *  @see #getLocation()
     */
    public void setLocation(int x, int y, int width, int height)
            throws IllegalActionException {
        Attribute attribute = getElement().getAttribute(
                HomerConstants.POSITION_NODE);
        HomerLocation location = null;
        if (attribute instanceof HomerLocation) {
            location = (HomerLocation) attribute;
        } else {
            if (attribute != null) {
                getElement().removeAttribute(attribute);
            }
            try {
                location = new HomerLocation(getElement(),
                        HomerConstants.POSITION_NODE);
                location.setVisibility(Settable.NONE);
            } catch (NameDuplicationException e) {
                // This can't happen.
                throw new IllegalActionException(getElement(), e.getMessage());
            }
        }
        location.setLocation(x, y, width, height);
    }

    /** Set the tab attribute in the underlying named object wrapped by this element.
     *
     *  @param tag The tag identifier of the tab this element belongs to.
     *  @exception IllegalActionException If the tab cannot be set.
     *  @see #getTab()
     */
    public void setTab(String tag) throws IllegalActionException {
        Attribute attribute = getElement()
                .getAttribute(HomerConstants.TAB_NODE);

        StringAttribute tabAttribute;

        if (attribute instanceof StringAttribute) {
            tabAttribute = (StringAttribute) attribute;
        } else {
            if (attribute != null) {
                getElement().removeAttribute(attribute);
            }
            try {
                tabAttribute = new StringAttribute(getElement(),
                        HomerConstants.TAB_NODE);
                tabAttribute.setVisibility(Settable.NONE);
            } catch (NameDuplicationException e) {
                // This can't happen, because the attribute was removed first.
                throw new IllegalActionException(getElement(), e.getMessage());
            }
        }

        tabAttribute.setExpression(tag);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Ptolemy element.
     */
    private NamedObj _element;
}
