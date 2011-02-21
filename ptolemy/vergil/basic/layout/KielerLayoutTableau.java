/*  A top-level dialog window for controlling the Kieler graph layout algorithm.

 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.vergil.basic.layout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.layout.kieler.KielerLayout;
import ptolemy.vergil.basic.layout.kieler.PtolemyModelUtil;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;

/**
 A top-level dialog window for controlling the Kieler graph layout algorithm.

 @author Christopher Brooks, based on JVMTableau. Christian Motika <cmot@informatik.uni-kiel.de>
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class KielerLayoutTableau extends Tableau {
    /** Construct a frame to control layout of graphical elements
     *  using the Kieler algorithms for the specified Ptolemy II model.
     *
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public KielerLayoutTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        NamedObj model = container.getModel();
        
        _frame = new KielerLayoutFrame((CompositeEntity) model, this);
        setFrame(_frame);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of KielerLayoutTableau.
     */
    public class KielerLayoutFrame extends PtolemyFrame {
        /** Construct a frame to display Kieler layout controls.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public KielerLayoutFrame(final CompositeEntity model, Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
            super(model, tableau);
            
            setTitle("Layout of " + model.getName());

            // Caveats panel.
            JPanel caveatsPanel = new JPanel();
            caveatsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            caveatsPanel
                    .setLayout(new BoxLayout(caveatsPanel, BoxLayout.X_AXIS));

            JTextArea messageArea = new JTextArea(
                    "Use the buttons below to control the Kieler automatic layout algorithm",
                    2, 10);
            messageArea.setEditable(false);
            messageArea.setBorder(BorderFactory.createEtchedBorder());
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            caveatsPanel.add(messageArea);

            JButton moreInfoButton = new JButton("More Info");
            moreInfoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    String infoResource = "ptolemy/vergil/basic/layout/package.html";
                    try {
                        Configuration configuration = getConfiguration();

                        // Use Thread.currentThread() so that this code will
                        // work under WebStart.
                        URL infoURL = Thread.currentThread()
                                .getContextClassLoader()
                                .getResource(infoResource);
                        configuration.openModel(null, infoURL,
                                infoURL.toExternalForm());
                    } catch (Exception ex) {
                        throw new InternalErrorException(model, ex,
                                "Failed to open " + infoResource + ": ");
                    }
                }
            });
            caveatsPanel.add(moreInfoButton);
            JPanel upper = new JPanel();
            upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
            //caveatsPanel.setMaximumSize(new Dimension(500, 100));
            upper.add(caveatsPanel);

            // Panel for push buttons.
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 4));

            String[][] buttons = new String[][] {
                    { "Place All", "placeall.gif",
                            "Automatic layout. Places all items including attributes. No routing is done." },
                    { "Place", "place.gif",
                            "Automatic layout. Only places connected items. No routing is done." },
                    { "Place and Route", "placeandroute.gif",
                            "Place and Route! Inserts new Relation Vertices. (EXPERIMENTAL)" },
                    { "Remove Vertices", "removevertices.gif",
                            "Remove unnecessary relation vertices." },
                    { "Hide/Show Vertices", "hidevertices.gif",
                            "Toggle hide/show unnecessary relation vertices" },
                    { "Classic Layout", "classic.gif", "Older layout style" } };

            AbstractAction[] actions = new AbstractAction[] {
                    new PlaceAllAction(), new PlaceAction(),
                    new PlaceAndRouteAction(), new RemoveVerticesAction(),
                    new HideVerticesAction(), new PtolemyLayoutAction() };

            for (int i = 0; i < buttons.length; i++) {
                JButton button;
                URL url = getClass().getResource(
                        "/ptolemy/vergil/basic/layout/img/" + buttons[i][1]);
                if (url == null) {
                    button = new JButton(buttons[i][0]);
                } else {
                    button = new JButton(new ImageIcon(url));
                }
                button.setToolTipText(buttons[i][2]);
                buttonPanel.add(button);
                button.addActionListener(actions[i]);
            }

            //buttonPanel.setMaximumSize(new Dimension(500, 50));
            upper.add(buttonPanel);
            upper.setPreferredSize(new Dimension(200, 100));
            getContentPane().add(upper, BorderLayout.CENTER);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * Base class for layout actions.
     * Derived classes vary their constructor calls which allows the derived classes
     * to avoid duplicated code.
     */
    private class BaseLayoutAction extends AbstractAction {
        /** Construction an action for placing layout.
         *  @param applyEdgeLayout true if the edge layout should be applied.
         *  @param bendPointAnnotation true if the new layout hints should be
         *         annotated instead of adding dummy layout relations
         *  @param boxLayout true if the box layout should be applied.
         *  @param removeUnnecessaryRelations true if we are to remove
         *         unnecessary relation vertices.
         *  @param showUnnecessaryRelationsToggle true if we are to toggle between showing
         *  and hiding unnecessary relation vertices.
         */
        public BaseLayoutAction(boolean applyEdgeLayout, boolean bendPointAnnotation, boolean boxLayout,
                boolean removeUnnecessaryRelations,
                boolean showUnnecessaryRelationsToggle) {
            if (((applyEdgeLayout || boxLayout) && (removeUnnecessaryRelations || showUnnecessaryRelationsToggle))
                    || (removeUnnecessaryRelations && showUnnecessaryRelationsToggle)) {
                throw new InternalErrorException(
                        "If either applyEdgeLayout or boxLayout "
                                + "is true, then removeUnnecessaryRelations and showUnnecessaryRelationsToggle"
                                + "must be false.  Also, only one of "
                                + "removeUnnecessaryRelations and showUnnecessaryRelationsToggle can be true.");
            }
            _applyEdgeLayout = applyEdgeLayout && !bendPointAnnotation;
            _applyEdgeLayoutBendPointAnnotation = applyEdgeLayout && bendPointAnnotation;
            _boxLayout = boxLayout;
            _removeUnnecessaryRelations = removeUnnecessaryRelations;
            _showUnnecessaryRelationsToggle = showUnnecessaryRelationsToggle;
        }

        public void actionPerformed(ActionEvent e) {
            NamedObj model = null;
            try {
                // Get the frame and the current model here.
                model = _frame.getModel();
                actionPerformed(e, model);
            } catch (Exception ex) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error(
                        "Failed to layout \""
                                + (model == null ? "name not found" : (model
                                        .getFullName())) + "\"", ex);
            }
        }
        
        public void actionPerformed(ActionEvent e, NamedObj model) {
            try {
                if (!(model instanceof CompositeActor)) {
                    throw new InternalErrorException(
                            "For now only actor oriented graphs with ports are supported by KIELER layout. "
                                    + "The model \""
                                    + model.getFullName()
                                    + "\" was a "
                                    + model.getClass().getName()
                                    + " which is not an instance of CompositeActor.");
                }
                JFrame frame = null;
                int tableauxCount = 0;
                Iterator tableaux = Configuration.findEffigy(model)
                        .entityList(Tableau.class).iterator();
                while (tableaux.hasNext()) {
                    Tableau tableau = (Tableau) (tableaux.next());
                    tableauxCount++;
                    if (tableau.getFrame() instanceof ActorGraphFrame) {
                        frame = tableau.getFrame();
                    }
                }
                // Check for supported type of editor 
                if (!(frame instanceof ActorGraphFrame)) {
                    String message = "";
                    if (tableauxCount == 0) {
                        message = "findEffigy() found no Tableaux?  There should have been one "
                                + "ActorGraphFrame.";
                    } else {
                        JFrame firstFrame = ((Tableau) Configuration
                                .findEffigy(model).entityList(Tableau.class)
                                .get(0)).getFrame();
                        if (firstFrame instanceof KielerLayoutFrame) {
                            message = "Internal Error: findEffigy() returned a KielerLayoutGUI, "
                                    + "please save the model before running the layout mechanism.";
                        } else {
                            message = "The first frame of "
                                    + tableauxCount
                                    + " found by findEffigy() is a \""
                                    + firstFrame.getClass().getName()
                                    + "\", which is not an instance of ActorGraphFrame."
                                    + " None of the other frames were ActorGraphFrames either.";
                        }
                    }
                    throw new InternalErrorException(
                            model,
                            null,
                            "For now only actor oriented graphs with ports are supported by KIELER layout. "
                                    + message
                                    + (frame != null ? " Details about the frame: "
                                            + StringUtilities.ellipsis(
                                                    frame.toString(), 80)
                                            : ""));
                } else {
                    if (_removeUnnecessaryRelations) {
                        PtolemyModelUtil
                                .removeUnnecessaryRelations((CompositeActor) model);
                    } else if (_showUnnecessaryRelationsToggle) {
                        PtolemyModelUtil
                                .showUnnecessaryRelationsToggle((CompositeActor) model);
                    } else {
                        BasicGraphFrame graphFrame = (BasicGraphFrame) frame;

                        // fetch everything needed to build the LayoutTarget
                        GraphController graphController = graphFrame
                                .getJGraph().getGraphPane()
                                .getGraphController();
                        GraphModel graphModel = graphFrame.getJGraph()
                                .getGraphPane().getGraphController()
                                .getGraphModel();
                        BasicLayoutTarget layoutTarget = new BasicLayoutTarget(
                                graphController);

                        // create Kieler layouter for this layout target
                        KielerLayout layout = new KielerLayout(layoutTarget);
                        layout.setModel((CompositeActor) model);
                        layout.setApplyEdgeLayout(_applyEdgeLayout);
                        layout.setApplyEdgeLayoutBendPointAnnotation(_applyEdgeLayoutBendPointAnnotation);
                        layout.setBoxLayout(_boxLayout);
                        layout.setTop(graphFrame);

                        layout.layout(graphModel.getRoot());
                    }
                }
            } catch (Exception ex) {
                // If we do not catch exceptions here, then they
                // disappear to stdout, which is bad if we launched
                // where there is no stdout visible.
                MessageHandler.error(
                        "Failed to layout \""
                                + (model == null ? "name not found" : (model
                                        .getFullName())) + "\"", ex);
            }
        }

        public boolean _applyEdgeLayout;
        public boolean _applyEdgeLayoutBendPointAnnotation;
        public boolean _boxLayout;
        public boolean _removeUnnecessaryRelations;
        public boolean _showUnnecessaryRelationsToggle;
    }

    /** Use the older layout algorithm. */
    private class PtolemyLayoutAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            // Get the frame and the current model here.
            NamedObj model = _frame.getModel();
            List tableaux = Configuration.findEffigy(model).entityList(
                    Tableau.class);
            JFrame frame = ((Tableau) tableaux.get(0)).getFrame();
            BasicGraphFrame graphFrame = (BasicGraphFrame) frame;
            graphFrame.layoutGraphWithPtolemyLayout();
        }
    }
    
    /** New automatic layout option placing all connected nodes and annotating 
     * relations with bend point positions of connected links.
     */
    private class PlaceAndRouteAnnotationAction extends BaseLayoutAction {
        /** Construct a HideAndRouteAction.
         */
        public PlaceAndRouteAnnotationAction() {
            super(true, true, false, false, false);
        }
    }    

    /** Toggle between showing and hiding of unnecessary relation vertices.
     */
    private class HideVerticesAction extends BaseLayoutAction {
        /** Construct a HideAndRouteAction.
         */
        public HideVerticesAction() {
            super(false, false,false, false, true);
        }
    }

    /** Action to place all items, including attributes.  No routing is done. */
    private class PlaceAllAction extends BaseLayoutAction {
        /** Construct a PlaceAllAction that has 
         *  applyEdgeLayout set to false and
         *  boxLayout set to true.
         */
        public PlaceAllAction() {
            // applyEdgeLayout = false, boxLayout = true
            super(false, false,true, false, false);
        }
    }

    /** Action to do automatic layout. Only places connected items. No
     * routing is done.
     */
    private class PlaceAction extends BaseLayoutAction {
        /** Construct a PlaceAction that has 
         *  applyEdgeLayout set to false and
         *  boxLayout set to false.
         */
        public PlaceAction() {
            super(false, false,false, false, false);
        }
    }

    /** Place and Route! Inserts new Relation Vertices. (EXPERIMENTAL).
     */
    private class PlaceAndRouteAction extends BaseLayoutAction {
        /** Construct a PlaceAndRouteAction that has 
         *  applyEdgeLayout set to true and
         *  boxLayout set to false.
         */
        public PlaceAndRouteAction() {
            super(true, false,false, false, false);
        }
    }

    /**
     * An action to remove unnecessary relation vertices. Unnecessary
     * means they have been introduced just for manual routing of
     * edges and have only 0, 1 or 2 adjacent links.
     */
    private class RemoveVerticesAction extends BaseLayoutAction {
        /** Construct a PlaceAndRouteAction that has 
         *  applyEdgeLayout set to true and
         *  boxLayout set to false.
         */
        public RemoveVerticesAction() {
            super(false, false,false, true, false);
        }
    }

    /** The Kieler Layout Frame, needed so that we can call getModel(). */
    private KielerLayoutFrame _frame;
}
