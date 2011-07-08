/*
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

import javax.swing.JPanel;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.Scene;

import ptolemy.actor.gui.AWTContainer;
import ptolemy.actor.gui.PortablePlaceable;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PortablePlaceableWidget

public class PortablePlaceableWidget extends NamedObjectWidget implements
        NamedObjectWidgetInterface {

    public PortablePlaceableWidget(final Scene scene, NamedObj namedObject) {
        super(scene, namedObject);
        if (!(namedObject instanceof PortablePlaceable)) {
            throw new IllegalArgumentException(
                    "NamedObject must be instance of PortablePlaceable");
        }
        final JPanel panel = new JPanel();
        _componentWidget = new ComponentWidget(scene, panel);
        ((PortablePlaceable) namedObject).place(new AWTContainer(panel) {
            @Override
            public void add(Object object) {
                Component component = (Component) object;
                getPlatformContainer().add(component, BorderLayout.CENTER);
            }
        });
        setLayout(LayoutFactory.createOverlayLayout());
        addChild(_componentWidget);
        addDependency(new Dependency() {

            public void revalidateDependency() {
                //
                if (!isPreferredBoundsSet() || getPreferredBounds() == null) {
                    return;
                }
                //                if (_componentWidget.isComponentVisible()) {
                //                    _componentWidget.setComponentVisible(false);
                //                }
            }
        });
    }

    private ComponentWidget _componentWidget;
    private JPanel panel;
    private JPanel innerPanel;
}
