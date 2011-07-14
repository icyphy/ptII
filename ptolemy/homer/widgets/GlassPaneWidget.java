/*
 TODO
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

import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// GlassPaneWidget
/**
 * TODO
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class GlassPaneWidget extends NamedObjectWidget implements
        NamedObjectWidgetInterface {

    /**
     * TODO
     * @param scene
     * @param namedObject
     */
    public GlassPaneWidget(final Scene scene, NamedObj namedObject) {
        super(scene, namedObject);
        _layeredPane = new JLayeredPane();
        _layeredPane.setLayout(null);
        _glassPane = new JPanel();

        _glassPane.setOpaque(false);
        _glassPane.setLocation(0, 0);
        _layeredPane.setLayer(_glassPane, JLayeredPane.DRAG_LAYER);
        _layeredPane.add(_glassPane);
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

        };
        _glassPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                redispatchEvent(scene, e, _glassPane);
            }
        });
        _glassPane.addMouseMotionListener(mouseAdapter);
        _glassPane.addMouseListener(mouseAdapter);
        _glassPane.addMouseWheelListener(mouseAdapter);

        _containerPanel = new JPanel();
        _containerPanel.setLayout(new BorderLayout());
        //        _containerPanel.setSize(dimension);
        _containerPanel.setLocation(0, 0);
        _layeredPane.setLayer(_containerPanel, JLayeredPane.DEFAULT_LAYER);
        _layeredPane.add(_containerPanel);
        _componentWidget = new ComponentWidget(scene, _layeredPane);

        setLayout(LayoutFactory.createOverlayLayout());
        addChild(_componentWidget);
        addDependency(new Dependency() {

            public void revalidateDependency() {
                if (!isPreferredBoundsSet() || getPreferredBounds() == null) {
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

    /**
     * Set the dimension of the glass pane and components its covering.
     * This method must be called once to set sizes for all components.  After
     * that the widget would automatically resize.
     * @param dimension the dimension of the glasspane.
     */
    public void setGlassPaneSize(Dimension dimension) {
        _componentWidget.setPreferredSize(dimension);
        _glassPane.setSize(dimension);
        _containerPanel.setSize(dimension);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     * TODO
     * @param scene
     * @param awtEvent
     * @param glassPane
     */
    private void redispatchEvent(final Scene scene, AWTEvent awtEvent,
            Component glassPane) {
        Component component = scene.getView();
        AWTEvent newEvent;
        if (awtEvent instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) awtEvent;
            Point componentPoint = SwingUtilities.convertPoint(glassPane,
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

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                 ////
    /**
     * 
     */
    protected JPanel _containerPanel;
    /**
     * 
     */
    protected JLayeredPane _layeredPane;
    /**
     * 
     */
    protected ComponentWidget _componentWidget;
    /**
     * 
     */
    protected JPanel _glassPane;
}
