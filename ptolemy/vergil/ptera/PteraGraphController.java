/*  A controller for a Ptera model.

 Copyright (c) 2009-2016 The Regents of the University of California.
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

package ptolemy.vergil.ptera;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import diva.canvas.Figure;
import diva.graph.GraphException;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.FigureIcon;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.domains.ptera.kernel.EventDebugEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.FSMGraphModel;
import ptolemy.vergil.modal.StateController;
import ptolemy.vergil.toolbox.FigureAction;

/**
 A controller for a Ptera model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PteraGraphController extends FSMGraphController {

    /** Create a PteraGraphController. */
    public PteraGraphController() {
    }

    /** React to an event by highlighting the new state.
     *  @param debugEvent The debug event.
     */
    @Override
    public void event(DebugEvent debugEvent) {
        if (debugEvent instanceof EventDebugEvent) {
            Event event = ((EventDebugEvent) debugEvent).getEvent();
            boolean isProcessed = ((EventDebugEvent) debugEvent).isProcessed();

            if (event != null) {
                Object location = event.getAttribute("_location");

                if (location != null) {
                    Figure figure = getFigure(location);

                    if (figure != null) {
                        if (_animationRenderer == null) {
                            _animationRenderer = new AnimationRenderer();
                        }

                        if (isProcessed) {
                            long animationDelay = getAnimationDelay();
                            if (animationDelay > 0 && isProcessed) {
                                try {
                                    Thread.sleep(animationDelay);
                                } catch (InterruptedException ex) {
                                }
                            }

                            _animationRenderer.renderDeselected(figure);
                        } else {
                            _animationRenderer.renderSelected(figure);
                        }
                    }
                }
            }
        }
    }

    @Override
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

        // Only include the port actions if there is an actor library.
        // The ptinyViewer configuration uses this.
        if (getConfiguration().getEntity("actor library") != null) {
            GUIUtilities.addMenuItem(menu, _newEventAction);
            GUIUtilities.addToolBarButton(toolbar, _newEventAction);
        }
    }

    /** An action to create a new event. */
    @SuppressWarnings("serial")
    public class NewEventAction extends FigureAction {

        /** Construct a new state. */
        public NewEventAction() {
            super("New Event");

            NodeRenderer renderer = new StateController.StateRenderer(
                    getGraphModel());
            Figure figure = renderer.render(_prototypeEvent);

            // Standard toolbar icons are 25x25 pixels.
            FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
            putValue(GUIUtilities.LARGE_ICON, icon);
            putValue("tooltip", "New Event");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_W));
        }

        /** Execute the action.
         *  @param e The action event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            PteraGraphFrame frame = (PteraGraphFrame) PteraGraphController.this
                    .getFrame();

            final double x;
            final double y;
            if (getSourceType() == TOOLBAR_TYPE
                    || getSourceType() == MENUBAR_TYPE) {
                // No location in the action, so put it in the middle.
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

            if (frame == null) {
                // Findbugs points out NP: Null pointer dereference
                throw new NullPointerException(
                        PteraGraphController.this + ": frame is null?");
            } else {
                // Create the state.
                String moml = "<group name=\"auto\">"
                        + frame._getDefaultEventMoML() + "</group>";

                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        toplevel, moml) {

                    @Override
                    protected void _postParse(MoMLParser parser) {
                        List<NamedObj> topObjects = parser.topObjectsCreated();
                        if (topObjects == null) {
                            return;
                        }
                        for (NamedObj object : topObjects) {
                            Location location = (Location) object
                                    .getAttribute("_location");
                            if (location == null) {
                                try {
                                    location = new Location(object,
                                            "_location");
                                } catch (KernelException e) {
                                    // Ignore.
                                }
                            }
                            if (location != null) {
                                try {
                                    location.setLocation(new double[] { x, y });
                                } catch (IllegalActionException e) {
                                    // Ignore.
                                }
                            }
                        }
                        parser.clearTopObjectsList();
                        super._postParse(parser);
                    }

                    @Override
                    protected void _preParse(MoMLParser parser) {
                        super._preParse(parser);
                        parser.clearTopObjectsList();
                    }
                };
                toplevel.requestChange(request);

                try {
                    request.waitForCompletion();
                } catch (Exception ex) {
                    throw new GraphException(ex);
                }
            }
        }
    }

    @Override
    protected void _createControllers() {
        super._createControllers();
        _transitionController = new SchedulingRelationController(this);
        _modalTransitionController = _transitionController;
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
