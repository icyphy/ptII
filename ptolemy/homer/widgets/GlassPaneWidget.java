/*
 Glasspane covers underlying component and blocks any events from propagating to them.
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;

import ptolemy.homer.kernel.PositionableElement;

///////////////////////////////////////////////////////////////////
//// GlassPaneWidget

/** Glasspane covers underlying awt component and blocks any events from propagating to them.
 *  Instead, it forward all events it receives to the scene's view.  This is done to ensure
 *  that all widget actions work as intended.  This class is not intended to be used as is
 *  but rather needs to be extended for widgets where glasspane is required.
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class GlassPaneWidget extends NamedObjectWidget {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create new instance of the glasspane for the given positionable element.
     *  @param scene The scene containing the widget.
     *  @param element The element to visualize.
     */
    public GlassPaneWidget(final Scene scene, PositionableElement element) {
        super(scene, element);

        _layeredPane = new JLayeredPane();
        _layeredPane.setLayout(null);
        _glassPane = new JPanel();
        _glassPane.setOpaque(false);
        _glassPane.setLocation(0, 0);
        _layeredPane.setLayer(_glassPane, JLayeredPane.DRAG_LAYER);
        _layeredPane.add(_glassPane);

        // Capture mouse events and forward them to the scene.
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                _redispatchEvent(scene, e);
            }

        };
        _glassPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                _redispatchEvent(scene, e);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                _redispatchEvent(scene, e);
            }
        });
        _glassPane.addMouseMotionListener(mouseAdapter);
        _glassPane.addMouseListener(mouseAdapter);
        _glassPane.addMouseWheelListener(mouseAdapter);

        _containerPanel = new JPanel();
        _containerPanel.setLayout(new BorderLayout());
        _containerPanel.setLocation(0, 0);
        _layeredPane.setLayer(_containerPanel, JLayeredPane.DEFAULT_LAYER);
        _layeredPane.add(_containerPanel);
        _componentWidget = new ComponentWidget(scene, _layeredPane);

        setLayout(LayoutFactory.createOverlayLayout());
        addChild(_componentWidget);
        addDependency(new Dependency() {

            @Override
            public void revalidateDependency() {
                if (!isPreferredBoundsSet() || getPreferredBounds() == null
                        || getClientArea() == null) {
                    return;
                }
                if (getClientArea().getSize().equals(_glassPane.getSize())) {
                    return;
                }
                _glassPane.setSize(_componentWidget.getClientArea().getSize());
                _containerPanel.setSize(_componentWidget.getClientArea()
                        .getSize());
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the dimension of the glass pane and components its covering.
     *  This method must be called once to set sizes for all components.  After
     *  that the widget would automatically resize.
     *  @param dimension the dimension of the glasspane.
     */
    public void setGlassPaneSize(Dimension dimension) {
        _componentWidget.setPreferredSize(dimension);
        _glassPane.setSize(dimension);
        _containerPanel.setSize(dimension);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Place the component under the glasspane.
     *  @param component The component to placed underneath the glasspane.
     */
    protected void place(Component component) {
        _containerPanel.add(component, BorderLayout.CENTER);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The container panel holding the underlying component.
     */
    protected JPanel _containerPanel;

    /** The layered pane holding both the glasspane and the container panel.
     */
    protected JLayeredPane _layeredPane;

    /** The component widget that wraps the layerer pane.
     */
    protected ComponentWidget _componentWidget;

    /** The glass pane covering the container panel.
     */
    protected JPanel _glassPane;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Redispatch the awt event to the scene's view.
     *  @param scene The scene where the event needs to be dispatched.
     *  @param awtEvent The event received by the glasspane.
     */
    private void _redispatchEvent(final Scene scene, AWTEvent awtEvent) {
        Component component = scene.getView();
        AWTEvent newEvent;
        if (awtEvent instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) awtEvent;
            Point componentPoint = SwingUtilities.convertPoint(_glassPane,
                    mouseEvent.getPoint(), component);
            if (mouseEvent instanceof MouseWheelEvent) {
                MouseWheelEvent mouseWheelEvent = (MouseWheelEvent) mouseEvent;
                newEvent = new MouseWheelEvent(component, mouseEvent.getID(),
                        mouseEvent.getWhen(), mouseEvent.getModifiers(),
                        componentPoint.x, componentPoint.y,
                        mouseEvent.getClickCount(),
                        mouseEvent.isPopupTrigger(),
                        mouseWheelEvent.getScrollType(),
                        mouseWheelEvent.getScrollAmount(),
                        mouseWheelEvent.getWheelRotation());
            } else {
                newEvent = new MouseEvent(component, mouseEvent.getID(),
                        mouseEvent.getWhen(), mouseEvent.getModifiers(),
                        componentPoint.x, componentPoint.y,
                        mouseEvent.getClickCount(),
                        mouseEvent.isPopupTrigger(), mouseEvent.getButton());
            }
        } else if (awtEvent instanceof KeyEvent) {
            newEvent = awtEvent;
            awtEvent.setSource(component);
        } else {
            throw new IllegalStateException("Unhandled event type");
        }
        scene.getView().dispatchEvent(newEvent);
    }
}
