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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;

import ptolemy.actor.gui.AWTContainer;
import ptolemy.actor.gui.PortablePlaceable;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PortablePlaceableWidget
/**
 * TODO
 * @author Anar Huseynov
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PortablePlaceableWidget extends NamedObjectWidget implements
        NamedObjectWidgetInterface {

    /**
     * TODO
     * @param scene
     * @param namedObject
     */
    public PortablePlaceableWidget(final Scene scene, NamedObj namedObject) {
        super(scene, namedObject);
        if (!(namedObject instanceof PortablePlaceable)) {
            throw new IllegalArgumentException(
                    "NamedObject must be instance of PortablePlaceable");
        }
        final JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        final JPanel glassPane = new JPanel();

        glassPane.setOpaque(false);
        Dimension dimension = new Dimension(300, 200);
        glassPane.setSize(dimension);
        glassPane.setLocation(0, 0);
        layeredPane.setLayer(glassPane, JLayeredPane.DRAG_LAYER);
        layeredPane.add(glassPane);
        MouseAdapter mouseAdapter = new MouseAdapter() {
        };
        glassPane.addKeyListener(new KeyAdapter() {
        });
        glassPane.addMouseMotionListener(mouseAdapter);
        glassPane.addMouseListener(new WidgetMouseListener(this));
        glassPane.addMouseWheelListener(mouseAdapter);

        final JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout());
        containerPanel.setSize(dimension);
        containerPanel.setLocation(0, 0);
        layeredPane.setLayer(containerPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(containerPanel);
        _componentWidget = new ComponentWidget(scene, layeredPane);
        _componentWidget.setPreferredSize(dimension);

        setLayout(LayoutFactory.createOverlayLayout());
        addChild(_componentWidget);
        ((PortablePlaceable) namedObject)
                .place(new AWTContainer(containerPanel) {
                    @Override
                    public void add(Object object) {
                        Component component = (Component) object;
                        containerPanel.add(component, BorderLayout.CENTER);
                    }
                });
        addDependency(new Dependency() {

            public void revalidateDependency() {
                if (!isPreferredBoundsSet() || getPreferredBounds() == null) {
                    return;
                }
                glassPane.setSize(_componentWidget.getClientArea().getSize());
                containerPanel.setSize(_componentWidget.getClientArea()
                        .getSize());
            }
        });

    }

    /**
     * TODO
     */
    private ComponentWidget _componentWidget;
}
