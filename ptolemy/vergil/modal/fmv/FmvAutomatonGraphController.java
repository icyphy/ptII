/* The graph controller for FmvAutomaton (FSM supporting verification using formal methods) models.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.modal.fmv;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.File;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import diva.canvas.Figure;
import diva.graph.GraphException;
import diva.graph.GraphPane;
import diva.graph.NodeRenderer;
import diva.gui.toolbox.FigureIcon;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.FSMGraphModel;
import ptolemy.vergil.modal.StateController;
import ptolemy.vergil.toolbox.FigureAction;

///////////////////////////////////////////////////////////////////
//// FmvAutomatonGraphController

/**
 * A Graph Controller for Fmv automata models. This controller adds the "Invoke NuSMV" menu item to the Graph menu.
 *
 * @author Chihhong Patrick Cheng Contributor: Edward A. Lee
 * @version $Id: FmvAutomatonGraphController.java,v 1.00 2007/04/12 03:59:41 cxh
 *          Exp $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red ()
 */
public class FmvAutomatonGraphController extends FSMGraphController {
    /**
     * Create a new controller with the specified directory of the current
     * model. The directory is for setting the current directory of the file
     * chooser invoked by the "Compose With" menu item.
     *
     * @param directory
     *        An instance of File that specifies the directory of the current
     *        model.
     */
    public FmvAutomatonGraphController(File directory) {
        super();
        // _directory = directory;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add commands to the specified menu and toolbar, as appropriate for this
     * controller. In this class, commands are added to create ports and
     * relations.
     *
     * @param menu
     *        The menu to add to, or null if none.
     * @param toolbar
     *        The toolbar to add to, or null if none.
     */
    @Override
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);

        // Add an item that adds new states.
        menu.addSeparator();
        diva.gui.GUIUtilities.addMenuItem(menu, _newFmvStateAction);
        diva.gui.GUIUtilities.addToolBarButton(toolbar, _newFmvStateAction);
    }

    /** Prototype state for rendering. */
    private static Location _prototypeFmvState;

    static {
        CompositeEntity container = new CompositeEntity();

        try {
            State state = new State(container, "Fmv");
            _prototypeFmvState = new Location(state, "_location");
        } catch (KernelException ex) {
            // This should not happen.
            throw new InternalErrorException(null, ex, null);
        }
    }

    /** The action for creating states. */
    private NewFmvStateAction _newFmvStateAction = new NewFmvStateAction();

    ///////////////////////////////////////////////////////////////////
    //// NewStateAction

    /** An action to create a new state. */
    @SuppressWarnings("serial")
    public class NewFmvStateAction extends FigureAction {
        /** Construct a new state. */
        public NewFmvStateAction() {
            super("New FmvState");
            putValue("tooltip", "New State");

            NodeRenderer renderer = new StateController.StateRenderer(
                    getGraphModel());
            Figure figure = renderer.render(_prototypeFmvState);

            // Standard toolbar icons are 25x25 pixels.
            FigureIcon icon = new FigureIcon(figure, 25, 25, 1, true);
            putValue(diva.gui.GUIUtilities.LARGE_ICON, icon);
            putValue("tooltip", "New FmvState");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_W));
        }

        /** Execute the action. */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            if (getSourceType() == TOOLBAR_TYPE
                    || getSourceType() == MENUBAR_TYPE) {
                // No location in the action, so put it in the middle.
                BasicGraphFrame frame = FmvAutomatonGraphController.this
                        .getFrame();
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

            String stateName = toplevel.uniqueName("FmvState");

            // Create the state.
            String moml = null;
            String locationName = "_location";

            // Try to get the class name for the state from the library,
            // so that the library and the toolbar are assured of creating
            // the same object.
            try {
                LibraryAttribute attribute = (LibraryAttribute) toplevel
                        .getAttribute("_library", LibraryAttribute.class);

                if (attribute != null) {
                    CompositeEntity library = attribute.getLibrary();
                    Entity prototype = library.getEntity("state");

                    if (prototype != null) {
                        moml = prototype.exportMoML(stateName);

                        // FIXME: Get location name from prototype.
                    }
                }
            } catch (Exception ex) {
                // Ignore and use the default.
                // Avoid a FindBugs warning about ignored exception.
                moml = null;
            }

            if (moml == null) {
                moml = "<entity name=\""
                        + stateName
                        + "\" class=\"ptolemy.domains.modal.kernel.fmv.FmvState\">\n"
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

}
