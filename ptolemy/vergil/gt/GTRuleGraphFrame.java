/* A graph editor frame for ptolemy graph transformation models.
 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.vergil.gt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.RecursiveGraphMatcher;
import ptolemy.actor.gt.SingleRuleTransformer;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.FileParameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.kernel.AnimationRenderer;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.canvas.Figure;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// GTRuleGraphFrame

/**
 A graph editor frame for ptolemy graph transformation models.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see ptolemy.vergil.actor.ActorGraphFrame
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTRuleGraphFrame extends ExtendedGraphFrame
implements ChangeListener {

    /** Construct a frame associated with the specified case actor.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public GTRuleGraphFrame(SingleRuleTransformer entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified case actor.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public GTRuleGraphFrame(SingleRuleTransformer entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        // FIXME
        // helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    /** Set the center location of the visible part of the pane.
     *  This will cause the panner to center on the specified location
     *  with the current zoom factor.
     *  @param center The center of the visible part.
     *  @see #getCenter()
     */
    public void setCenter(Point2D center) {
        if (_graphs != null) {
            Rectangle2D visibleRect = getVisibleCanvasRectangle();
            AffineTransform newTransform = getJGraph().getCanvasPane()
                    .getTransformContext().getTransform();

            newTransform.translate(visibleRect.getCenterX() - center.getX(),
                    visibleRect.getCenterY() - center.getY());

            Iterator<JGraph> graphsIterator = _graphs.iterator();
            while (graphsIterator.hasNext()) {
                graphsIterator.next().getCanvasPane().setTransform(
                        newTransform);
            }
        }
    }

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();
        if (source instanceof JTabbedPane) {
            Component selected = ((JTabbedPane) source).getSelectedComponent();
            if (selected instanceof JGraph) {
                setJGraph((JGraph) selected);
            }
            if (_graphPanner != null) {
                _graphPanner.setCanvas((JGraph) selected);
                _graphPanner.repaint();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        _ruleMenu = new JMenu("Rule");
        _ruleMenu.setMnemonic(KeyEvent.VK_R);
        _menubar.add(_ruleMenu);

        MatchAction matchAction = new MatchAction();
        GUIUtilities.addMenuItem(_ruleMenu, matchAction);

        _ruleMenu.addSeparator();

        CreateHierarchyAction createHierarchyAction =
            new CreateHierarchyAction();
        GUIUtilities.addMenuItem(_ruleMenu, createHierarchyAction);

        LayoutAction layoutAction = new LayoutAction();
        GUIUtilities.addMenuItem(_ruleMenu, layoutAction);

        NewRelationAction newRelationAction = new NewRelationAction();
        GUIUtilities.addMenuItem(_ruleMenu, newRelationAction);

        GUIUtilities.addToolBarButton(_toolbar, newRelationAction);
        GUIUtilities.addToolBarButton(_toolbar, matchAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new ActorEditorGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new GraphPane(_controller, graphModel);
    }

    /** Create the component that goes to the right of the library.
     *  NOTE: This is called in the base class constructor, before
     *  things have been initialized. Hence, it cannot reference
     *  local variables.
     *  @param entity The entity to display in the component.
     *  @return The component that goes to the right of the library.
     */
    protected JComponent _createRightComponent(NamedObj entity) {
        if (!(entity instanceof SingleRuleTransformer)) {
            return super._createRightComponent(entity);
        }

        _graphs = new LinkedList<JGraph>();

        _tabbedPane = new JTabbedPane() {
            public void setMinimumSize(Dimension minimumSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setMinimumSize(minimumSize);
                }
            }

            public void setPreferredSize(Dimension preferredSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setPreferredSize(preferredSize);
                }
            }

            public void setSize(int width, int height) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setSize(width, height);
                }
            }

            /** Serial ID */
            private static final long serialVersionUID = -4998226270980176175L;
        };
        _tabbedPane.addChangeListener(this);
        Iterator<?> cases = ((SingleRuleTransformer) entity).entityList(
                CompositeActorMatcher.class).iterator();
        boolean first = true;
        while (cases.hasNext()) {
            CompositeActorMatcher matcher = (CompositeActorMatcher) cases.next();
            JGraph jgraph = _addTabbedPane(matcher, false);
            // The first JGraph is the one with the focus.
            if (first) {
                first = false;
                setJGraph(jgraph);
            }
            _graphs.add(jgraph);
        }
        return _tabbedPane;
    }

    /** The graph controller.  This is created in _createGraphPane(). */
    protected ActorEditorGraphController _controller;

    /** The case menu. */
    protected JMenu _ruleMenu;

    /** Add a tabbed pane for the specified case.
     *  @param refinement The case.
     *  @param newPane True to add the pane prior to the last pane.
     *  @return The pane.
     */
    private JGraph _addTabbedPane(CompositeActorMatcher matcher,
            boolean newPane) {
        GraphPane pane = _createGraphPane(matcher);
        pane.getForegroundLayer().setPickHalo(2);
        pane.getForegroundEventLayer().setConsuming(false);
        pane.getForegroundEventLayer().setEnabled(true);
        pane.getForegroundEventLayer().addLayerListener(new LayerAdapter() {
            /** Invoked when the mouse is pressed on a layer
             * or figure.
             */
            public void mousePressed(LayerEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }
        });
        JGraph jgraph = new JGraph(pane);
        String name = matcher.getName();
        jgraph.setName(name);
        int index = _tabbedPane.getComponentCount();
        // Put before the default pane, unless this is the default.
        if (newPane) {
            index--;
        }
        _tabbedPane.add(jgraph, index);
        jgraph.setBackground(BACKGROUND_COLOR);
        // Create a drop target for the jgraph.
        // FIXME: Should override _setDropIntoEnabled to modify all the drop
        //        targets created.
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    private SingleRuleTransformer _getTransformer() {
        ActorGraphModel graphModel =
            (ActorGraphModel) _controller.getGraphModel();
        CompositeActorMatcher matcher =
            (CompositeActorMatcher) graphModel.getPtolemyModel();
        SingleRuleTransformer transformer =
            (SingleRuleTransformer) matcher.getContainer();
        return transformer;
    }

    private List<JGraph> _graphs;

    /** The tabbed pane for cases. */
    private JTabbedPane _tabbedPane;

    /** Serial ID */
    private static final long serialVersionUID = 5919681658644668772L;



    /** Create a hierarchy, which is semantically flattened when the matching or
        transformation is performed.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 6.1
     @see ActorGraphFrame.CreateHierarchyAction
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class CreateHierarchyAction extends AbstractAction {

        /**  Create a new action to introduce a level of hierarchy.
         */
        public CreateHierarchyAction() {
            super("Create Hierarchy");
            putValue("tooltip",
                    "Create a TypedCompositeActor that contains the"
                            + " selected actors.");
        }

        public void actionPerformed(ActionEvent e) {
            createHierarchy();
        }

        private static final long serialVersionUID = -4212552714361210815L;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Action to automatically layout the graph.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 6.1
     @see ActorGraphFrame.LayoutAction
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class LayoutAction extends AbstractAction {

        /** Create a new action to automatically lay out the graph. */
        public LayoutAction() {
            super("Automatic Layout");
            putValue("tooltip", "Layout the Graph (Ctrl+T)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_T, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        /** Lay out the graph. */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraph();
            } catch (Exception ex) {
                MessageHandler.error("Layout failed", ex);
            }
        }

        private static final long serialVersionUID = -31790471585661407L;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private class MatchAction extends FigureAction {

        public MatchAction() {
            super("Match Model");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/match.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/match_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/match_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/match_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match a Ptolemy model in an external file");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));

            _attribute = new Attribute((Workspace) null);

            try {
                _inputModel = new FileParameter(_attribute, "inputModel");
                _inputModel.setDisplayName("Input model");
            } catch (KernelException e) {
                throw new KernelRuntimeException(e, "Unable to create action " +
                        "instance.");
            }
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            MatchFileChooser filesChooser =
                new MatchFileChooser(getFrame(), _attribute);
            if (filesChooser.buttonPressed().equals(
                    MatchFileChooser._moreButtons[0])) {
                File input = null;
                try {
                    input = _inputModel.asFile();
                } catch (IllegalActionException ex) {
                }
                if (input == null) {
                    MessageHandler.message("Input model must not be empty.");
                    return;
                }
                if (!input.exists()) {
                    MessageHandler.message("Unable to read input model " +
                            _inputModel.getExpression() + ".");
                    return;
                }

                try {
                    NamedObj toplevel =
                        GTRuleGraphFrame.this.getTableau().toplevel();
                    if (!(toplevel instanceof Configuration)) {
                        throw new InternalErrorException(
                                "Expected top-level to be a Configuration: "
                                        + toplevel.getFullName());
                    }

                    _parser.reset();
                    CompositeEntity model = (CompositeEntity) _parser.parse(
                            null, input.toURL().openStream());

                    SingleRuleTransformer transformerActor = _getTransformer();
                    CompositeActorMatcher matcher =
                        transformerActor.getLeftHandSide();
                    if (_matcher.match(matcher, model)) {
                        MatchResultViewer._setTableauFactory(this, model);

                        Configuration configuration = (Configuration) toplevel;
                        Tableau tableau = configuration.openModel(model);
                        MatchResultViewer viewer =
                            (MatchResultViewer) tableau.getFrame();

                        MatchResult result = _matcher.getMatchResult();
                        viewer.setMatchResult(result);
                    }
                } catch (MalformedURLException ex) {
                    MessageHandler.message("Unable to obtain URL from the " +
                            "input file name.");
                    return;
                } catch (Exception ex) {
                    throw new InternalErrorException(ex);
                }
            }
        }

        public void highlightObject(MatchResultViewer frame, NamedObj object) {
            Object location =
                object.getAttribute("_location");

            ActorEditorGraphController controller =
                (ActorEditorGraphController)
                frame.getJGraph().getGraphPane()
                .getGraphController();
            Figure figure = controller.getFigure(location);
            new AnimationRenderer().renderSelected(figure);
        }

        private Attribute _attribute;

        private FileParameter _inputModel;

        private RecursiveGraphMatcher _matcher = new RecursiveGraphMatcher();

        private MoMLParser _parser = new MoMLParser();

        private static final long serialVersionUID = -696919249330217870L;
    }

    private static class MatchFileChooser extends ComponentDialog {

        public MatchFileChooser(Frame owner, NamedObj target) {
            super(owner, "Choose Input File", new Configurer(target),
                    _moreButtons);
        }

        private static final String[] _moreButtons = new String[] {
            "Match", "Cancel"
        };

        private static final long serialVersionUID = 2369054217750135740L;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    ///////////////////////////////////////////////////////////////////
    //// NewRelationAction
    /** An action to create a new relation. */
    private class NewRelationAction extends FigureAction {

        /** Create an action that creates a new relation.
         */
        public NewRelationAction() {
            super("New Relation");

            String[][] iconRoles = new String[][] {
                    { "/ptolemy/vergil/actor/img/relation.gif",
                        GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/relation_o.gif",
                        GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/relation_ov.gif",
                        GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/relation_on.gif",
                        GUIUtilities.SELECTED_ICON } };
            GUIUtilities.addIcons(this, iconRoles);

            putValue("tooltip", "Control-click to create a new relation");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY, Integer.valueOf(
                    KeyEvent.VK_R));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            double x;
            double y;

            JGraph graph = (JGraph) _tabbedPane.getSelectedComponent();
            Dimension size = graph.getSize();
            x = size.getWidth() / 2;
            y = size.getHeight() / 2;
            double[] point = SnapConstraint.constrainPoint(x, y);

            SingleRuleTransformer transformerActor = _getTransformer();
            List<?> entityList =
                transformerActor.entityList(CompositeActorMatcher.class);
            Iterator<?> iterator = entityList.iterator();
            for (int i = 0; i < _tabbedPane.getSelectedIndex(); i++) {
                iterator.next();
            }

            NamedObj namedObj = (NamedObj) iterator.next();

            final String relationName = namedObj.uniqueName("relation");
            final String vertexName = "vertex1";

            // Create the relation.
            StringBuffer moml = new StringBuffer();
            moml.append("<relation name=\"" + relationName + "\">\n");
            moml.append("<vertex name=\"" + vertexName + "\" value=\"{");
            moml.append(point[0] + ", " + point[1]);
            moml.append("}\"/>\n");
            moml.append("</relation>");

            MoMLChangeRequest request = new MoMLChangeRequest(this, namedObj,
                    moml.toString());
            request.setUndoable(true);
            namedObj.requestChange(request);
        }

        private static final long serialVersionUID = 2208151447002268749L;
    }

    /*private class TransformAction extends FigureAction {

        public TransformAction() {
            super("Perform Transformation");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/run.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/run_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/run_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/run_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Perform Transformation (Ctrl+R)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_R, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));

            _attribute = new Attribute((Workspace) null);

            try {
                _inputModel = new FileParameter(_attribute, "inputModel");
                _inputModel.setDisplayName("Input model");

                _outputModel = new FileParameter(_attribute, "outputModel");
                _outputModel.setDisplayName("Output model");
            } catch (KernelException e) {
                throw new KernelRuntimeException(e, "Unable to create action " +
                        "instance.");
            }
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            TransformationFilesChooser filesChooser =
                new TransformationFilesChooser(getFrame(), _attribute);
            if (filesChooser.buttonPressed().equals(
                    TransformationFilesChooser._moreButtons[0])) {
                File input = null;
                try {
                    input = _inputModel.asFile();
                } catch (IllegalActionException ex) {
                }
                if (input == null) {
                    MessageHandler.message("Input model must not be empty.");
                    return;
                }
                if (!input.exists()) {
                    MessageHandler.message("Unable to read input model " +
                            _inputModel.getExpression() + ".");
                    return;
                }

                File output = null;
                try {
                    output = _outputModel.asFile();
                } catch (IllegalActionException ex) {
                }
                if (output == null) {
                    MessageHandler.message("Output model must not be empty.");
                    return;
                }
                if (output.exists()) {
                    if (!MessageHandler.yesNoQuestion(
                            "Overwrite output model " +
                            _outputModel.getExpression() + "?")) {
                        return;
                    }
                }

                _parser.reset();
                NamedObj model;
                try {
                    model = _parser.parse(null, input.toURL());
                } catch (Exception ex) {
                    MessageHandler.message("Unable to parse input model.");
                    return;
                }

                // TODO
                throw new KernelRuntimeException(
                        "Model transformation to be implemented.");
            }
        }

        private Attribute _attribute;

        private FileParameter _inputModel;

        private FileParameter _outputModel;

        private MoMLParser _parser = new MoMLParser();

        private static final long serialVersionUID = -3272455226789715544L;

    }*/

    /*private static class TransformationFilesChooser extends ComponentDialog {

        public TransformationFilesChooser(Frame owner, NamedObj target) {
            super(owner, "Choose Input/Output Files", new Configurer(target),
                    _moreButtons);
        }

        private static final String[] _moreButtons = new String[] {
            "Transform", "Cancel"
        };

        private static final long serialVersionUID = -8150952956122992910L;
    }*/
}
