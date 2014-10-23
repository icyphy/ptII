/*
 The widget visualizes only named objects that implement {@link PortablePlaceable} interface.
 It requests the named object to place itself into a provided container.
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

package ptolemy.homer.widgets;

import java.awt.Component;

import org.netbeans.api.visual.widget.Scene;

import ptolemy.actor.gui.AWTContainer;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.homer.kernel.PositionableElement;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PortablePlaceableWidget

/** The widget visualizes only named objects that implement {@link PortablePlaceable} interface.
 * It requests the named object to place itself into a provided container.  The container is then covered
 * with glass pane in order to prevent event propagation.
 *
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PortablePlaceableWidget extends GlassPaneWidget implements
MinSizeInterface {

    /** Create a new instance of the widget by requesting the element to place
     *  itself into the provided container.
     *  @param scene The scene.
     *  @param element The positionable element.
     */
    public PortablePlaceableWidget(Scene scene, PositionableElement element) {
        super(scene, element);

        NamedObj namedObject = element.getElement();
        if (!(namedObject instanceof PortablePlaceable)) {
            throw new IllegalArgumentException(
                    "NamedObject must be instance of PortablePlaceable");
        }

        ((PortablePlaceable) namedObject).place(new AWTContainer(
                _containerPanel) {
            @Override
            public void add(Object object) {
                _placeComponent((Component) object);
            }

        });

        // Workaround for actors that don't use PortableContainer's add method.
        if (_containerPanel.getComponentCount() > 0) {
            Component component = _containerPanel.getComponent(0);
            _containerPanel.remove(component);
            _placeComponent(component);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Place the component into container covered by a glasspane.
     *  @param component The object to place.
     */
    private void _placeComponent(Component component) {
        setGlassPaneSize(component.getPreferredSize());
        super.place(component);
    }

    /**
     * The minimal width for this width is _MIN_DIMENSION.
     */
    @Override
    public Integer getMinWidth() {
        return _MIN_DIMENSION;
    }

    /**
     * The minimal width for this height is _MIN_DIMENSION.
     */
    @Override
    public Integer getMinHeight() {
        return _MIN_DIMENSION;
    }

    /**
     * The minimal dimension a widget can have.
     */
    private static final int _MIN_DIMENSION = 20;
}
