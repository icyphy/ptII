/* The definition of attributes.

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

import ptolemy.actor.gui.style.ParameterEditorStyle;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.kernel.util.AbstractSettableAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;

///////////////////////////////////////////////////////////////////
//// AttributeElement

/** The definition of attributes.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class AttributeElement extends PositionableElement {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Parse the Ptolemy attribute element.
     *  @param attribute The Ptolemy attribute element to be parsed.
     *  @exception IllegalActionException If the attribute is not settable.
     */
    public AttributeElement(Attribute attribute) throws IllegalActionException {
        super(attribute);

        if (!(attribute instanceof Settable)) {
            throw new IllegalActionException("Attribute "
                    + attribute.getFullName() + " is not settable.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Attach a value listener to the underlying attribute.
     *
     *  @param listener The listener to attach.
     */
    public void addListener(ValueListener listener) {
        ((AbstractSettableAttribute) getElement()).addValueListener(listener);
    }

    /** Add the visual representation of the element to the provided
     *  container.
     *
     *  @param container The container for the element to be placed in.
     *  @exception IllegalActionException If the element cannot be added
     *  to the container.
     */
    @Override
    public void addToContainer(PortableContainer container)
            throws IllegalActionException {
        // Place the representation into the given container.
        _representation.placeWidget((Attribute) getElement(), container);
    }

    /** Change the style of the parameter to a new style.
     *
     *  Note: It will remove the style of the element even if there is something
     *  wrong with the new style.
     *
     *  @param style The new style to be used by the attribute.
     *  @exception IllegalActionException If this attribute is not of the
     *  expected class for the container, or it has no name, or the attribute
     *  and container are not in the same workspace, or the proposed container
     *  would result in recursive containment, or the proposed container is not
     *  an instance of Settable. Also, if the style name is not the expected
     *  name for the style.
     */
    public void changeStyle(ParameterEditorStyle style)
            throws IllegalActionException {

        try {
            getElement().workspace().getWriteAccess();
            style.setContainer(getElement());
        } catch (NameDuplicationException e) {
            // The name can still be wrong, in that case let's change this
            // to IllegalActionException saying the name is wrong.
            throw new IllegalActionException(style,
                    "Name for style attribute is not what's expected.");
        } finally {
            getElement().workspace().doneWriting();
        }
    }

    /** Remove a value listener from the underlying attribute.
     *
     *  @param listener The listener to remove.
     *  @see #addListener(ValueListener)
     */
    public void removeListener(ValueListener listener) {
        ((AbstractSettableAttribute) getElement())
                .removeValueListener(listener);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The representation of the attribute based on the style and the
     *  platform.
     */
    private AttributeRepresentation _representation = PtolemyInjector
            .getInjector().getInstance(AttributeRepresentation.class);
}
