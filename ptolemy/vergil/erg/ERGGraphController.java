/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.vergil.erg;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import ptolemy.domains.erg.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.fsm.FSMGraphModel;
import ptolemy.vergil.fsm.StateController;
import ptolemy.vergil.toolbox.FigureAction;
import diva.canvas.Figure;
import diva.graph.GraphException;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.toolbox.FigureIcon;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ERGGraphController extends FSMGraphController {

    /**
     *
     */
    public ERGGraphController() {
    }

    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);

        Component[] components = menu.getMenuComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component instanceof JMenuItem) {
                Action action = ((JMenuItem) component).getAction();
                if (action instanceof NewStateAction) {
                    menu.remove(i);
                    break;
                }
            }
        }

        components = toolbar.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component instanceof JButton) {
                Action action = ((JButton) component).getAction();
                if (action instanceof NewStateAction) {
                    toolbar.remove(i);
                    break;
                }
            }
        }

        diva.gui.GUIUtilities.addMenuItem(menu, _newEventAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar, _newEventAction);
    }

    /** An action to create a new state. */
    public class NewEventAction extends FigureAction {

        /** Construct a new state. */
        public NewEventAction() {
            super("New Event");
            putValue("tooltip", "New Event");

            NodeRenderer renderer = new StateController.StateRenderer(
                    getGraphModel());
            Figure figure = renderer.render(_prototypeEvent);

            // Standard toolbar icons are 25x25 pixels.
            FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
            putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            putValue("tooltip", "New Event");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY, Integer
                    .valueOf(KeyEvent.VK_W));
        }

        /** Execute the action. */
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            if ((getSourceType() == TOOLBAR_TYPE)
                    || (getSourceType() == MENUBAR_TYPE)) {
                // No location in the action, so put it in the middle.
                BasicGraphFrame frame = ERGGraphController.this.getFrame();
                Point2D center;

                if (frame != null) {
                    // Put in the middle of the visible part.
                    center = frame.getCenter();
                    x = center.getX();
                    y = center.getY();
                } else {
                    // Put in the middle of the pane.
                    GraphPane pane = getGraphPane();
                    center = pane.getSize();
                    x = center.getX() / 2;
                    y = center.getY() / 2;
                }
            } else {
                x = getX();
                y = getY();
            }

            FSMGraphModel graphModel = (FSMGraphModel) getGraphModel();
            NamedObj toplevel = graphModel.getPtolemyModel();

            String stateName = toplevel.uniqueName("Event");

            // Create the state.
            String moml = null;
            String locationName = "_location";

            if (moml == null) {
                moml = "<entity name=\"" + stateName
                        + "\" class=\"ptolemy.domains.erg.kernel.Event\">\n"
                        + "<property name=\"" + locationName
                        + "\" class=\"ptolemy.kernel.util.Location\""
                        + " value=\"[" + x + ", " + y + "]\"/>\n"
                        + "</entity>\n";
            }

            ChangeRequest request = new MoMLChangeRequest(this, toplevel, moml);
            toplevel.requestChange(request);

            try {
                request.waitForCompletion();
            } catch (Exception ex) {
                throw new GraphException(ex);
            }
        }
    }

    protected void _createControllers() {
        super._createControllers();
        _transitionController = new SchedulingRelationController(this);
    }

    /** The action for creating event. */
    private NewEventAction _newEventAction = new NewEventAction();

    /** Prototype state for rendering. */
    private static final Location _prototypeEvent;

    static {
        CompositeEntity container = new CompositeEntity();

        try {
            Event state = new Event(container, "E");
            _prototypeEvent = new Location(state, "_location");
        } catch (KernelException ex) {
            // This should not happen.
            throw new InternalErrorException(null, ex, null);
        }
    }
}
