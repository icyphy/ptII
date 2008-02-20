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

package ptolemy.vergil.gt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.FSMMatcher;
import ptolemy.actor.gt.GTEntity;
import ptolemy.actor.gt.GTIngredientsAttribute;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.PortMatcher;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorInstanceController;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.fsm.FSMGraphModel;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;
import diva.gui.toolbox.JCanvasPanner;
import diva.gui.toolbox.JContextMenu;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTFrameController implements ChangeListener, KeyListener {

    public CompositeEntity getActiveModel() {
        AbstractBasicGraphModel graphModel =
            (AbstractBasicGraphModel) getGraphController().getGraphModel();
        return (CompositeEntity) graphModel.getPtolemyModel();
    }

    public int getActiveTabIndex() {
        return _activeTabIndex;
    }

    public Configuration getConfiguration() {
        NamedObj toplevel = _frame.getTableau().toplevel();
        if (toplevel instanceof Configuration) {
            return (Configuration) toplevel;
        } else {
            return null;
        }
    }

    public RunnableGraphController getGraphController() {
        return _graphController;
    }

    public List<GraphPane> getGraphPanes() {
        return _graphPanes;
    }

    public List<JGraph> getGraphs() {
        return _graphs;
    }

    /** Return the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @return the JGraph.
     *  @see ExtendedGraphFrame#getJGraph()
     *  @see ExtendedGraphFrame#setJGraph(JGraph)
     */
    public JGraph getJGraph() {
        if (hasTabs()) {
            Component selected = _tabbedPane.getSelectedComponent();
            if (selected instanceof JGraph) {
                return (JGraph) selected;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public JTabbedPane getTabbedPane() {
        return _tabbedPane;
    }

    public TransformationRule getTransformationRule() {
        CompositeEntity model = getActiveModel();
        NamedObj parent = model.getContainer();
        while (!(parent instanceof TransformationRule)) {
            parent = parent.getContainer();
        }
        return (TransformationRule) parent;
    }

    public boolean hasTabs() {
        return _tabbedPane != null;
    }

    public boolean isTableActive() {
        return hasTabs() && getActiveTabIndex() == 2;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                || e.getKeyCode() == (KeyEvent.VK_ALT | KeyEvent.VK_S)) {
            e.consume();
            _frame.cancelFullScreen();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    public void stateChanged(ChangeEvent event) {
        if (event.getSource() == _tabbedPane) {
            _activeTabIndex = _tabbedPane.getSelectedIndex();
            if (_activeTabIndex < _graphPanes.size()) {
                _graphController.getSelectionModel().clearSelection();
                GraphPane graphPane = _graphPanes.get(_activeTabIndex);
                _graphController.setGraphPane(graphPane);
                _graphController.setGraphModel(graphPane.getGraphModel());
            }
            _showTab(_activeTabIndex);
        }
    }

    protected GTFrameController(ExtendedGraphFrame frame,
            JCanvasPanner graphPanner) {
        _frame = frame;
        _graphPanner = graphPanner;
    }

    protected RunnableGraphController _createGraphController(NamedObj entity) {
        if (_frame instanceof MatchResultViewer) {
            if (_isFSM(entity)) {
                return ((MatchResultViewer) _frame).new
                        MatchResultFSMGraphController();
            } else {
                return ((MatchResultViewer) _frame).new
                        MatchResultActorGraphController();
            }
        } else {
            if (_isFSM(entity)) {
                return new GTFSMGraphController();
            } else {
                return new GTActorGraphController();
            }
        }
    }

    protected AbstractBasicGraphModel _createGraphModel(NamedObj entity) {
        if (_isFSM(entity)) {
            return new GTFSMGraphModel((CompositeEntity) entity);
        } else {
            return new GTActorGraphModel(entity);
        }
    }

    protected GraphPane _createGraphPane(NamedObj entity) {
        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        AbstractBasicGraphModel graphModel = _createGraphModel(entity);
        GraphPane graphPane = new GraphPane(_graphController, graphModel);
        if (_graphPanes != null) {
            _graphPanes.add(graphPane);
        }
        return graphPane;
    }

    protected JComponent _createRightComponent(NamedObj entity) {
        // entity must be SingleRuleTransformer or CompositeActorMatcher.

        _graphController = _createGraphController(entity);
        _graphController.setConfiguration(getConfiguration());
        _graphController.setFrame(_frame);

        if (!(entity instanceof TransformationRule)) {
            return null;
        }

        _graphPanes = new LinkedList<GraphPane>();
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

        };

        _tabbedPane.addChangeListener(this);
        Iterator<?> cases = ((TransformationRule) entity).entityList(
                CompositeActorMatcher.class).iterator();
        boolean first = true;
        while (cases.hasNext()) {
            CompositeActorMatcher matcher = (CompositeActorMatcher) cases
                    .next();
            JGraph jgraph = _addTabbedPane(matcher, false);
            // The first JGraph is the one with the focus.
            if (first) {
                first = false;
                _frame.setJGraph(jgraph);
            }
            _graphs.add(jgraph);
        }

        GraphPane graphPane = _graphPanes.get(0);
        _graphController.setGraphPane(graphPane);
        _graphController.setGraphModel(graphPane.getGraphModel());

        return _tabbedPane;
    }

    protected void _removeListeners() {
        if (hasTabs()) {
            for (Component tab : _tabbedPane.getComponents()) {
                if (tab instanceof JGraph) {
                    AbstractBasicGraphModel graphModel =
                        (AbstractBasicGraphModel) ((JGraph) tab).getGraphPane()
                        .getGraphModel();
                    graphModel.removeListeners();
                }
            }
        }
    }

    protected static class GTActorGraphModel extends ActorGraphModel {

        public synchronized void startUpdate() {
            Set<?> linkSet = _getLinkSet();
            Set<Link> linksToRemove = new HashSet<Link>();
            for (Object linkObject : linkSet) {
                Link link = (Link) linkObject;
                boolean headOK = GraphUtilities.isContainedNode(link.getHead(),
                        getRoot(), this);
                boolean tailOK = GraphUtilities.isContainedNode(link.getTail(),
                        getRoot(), this);
                if (!(headOK && tailOK)) {
                    linksToRemove.add(link);
                }
            }
            for (Link link : linksToRemove) {
                _removeLink(link);
            }
            _updateStopped = false;
            _update();
        }

        public synchronized void stopUpdate() {
            _updateStopped = true;
        }

        protected boolean _update() {
            if (!_updateStopped) {
                return super._update();
            } else {
                return true;
            }
        }

        GTActorGraphModel(NamedObj composite) {
            super(composite);
        }

        private boolean _updateStopped = false;
    }

    protected static class GTFSMGraphModel extends FSMGraphModel {

        public GTFSMGraphModel(CompositeEntity composite) {
            super(composite);
        }

    }

    /** Add a tabbed pane for the specified case.
     *  @param refinement The case.
     *  @param newPane True to add the pane prior to the last pane.
     *  @return The pane.
     */
    private JGraph _addTabbedPane(CompositeActorMatcher matcher, boolean newPane) {
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
        jgraph.setBackground(BasicGraphFrame.BACKGROUND_COLOR);
        // Create a drop target for the jgraph.
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    private boolean _isFSM(NamedObj entity) {
        return entity instanceof FSMMatcher || entity instanceof FSMActor;
    }

    private void _showTab(int tabIndex) {
        Component tab = _tabbedPane.getComponent(tabIndex);
        if (tab instanceof JGraph) {
            _frame.setJGraph((JGraph) tab);
            if (_graphPanner != null) {
                _graphPanner.setCanvas((JGraph) tab);
            }
        } else {
            if (_graphPanner != null) {
                _graphPanner.setCanvas(null);
            }
        }
    }

    private int _activeTabIndex = 0;

    private ExtendedGraphFrame _frame;

    private RunnableGraphController _graphController;

    private List<GraphPane> _graphPanes;

    private JCanvasPanner _graphPanner;

    private List<JGraph> _graphs;

    private JTabbedPane _tabbedPane;

    private static class ConfigureOperationsAction extends FigureAction {

        public void actionPerformed(ActionEvent event) {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(event);
            NamedObj target = getTarget();
            Frame frame = getFrame();
            if (target instanceof GTEntity) {
                List<?> attributeList = target
                        .attributeList(EditorFactory.class);
                if (attributeList.size() > 0) {
                    EditorFactory factory = (EditorFactory) attributeList
                            .get(0);
                    factory.createEditor(target, frame);
                } else {
                    new EditParametersDialog(frame, target);
                }
            } else {
                List<?> ingredientsAttributes = target
                        .attributeList(GTIngredientsAttribute.class);
                try {
                    if (ingredientsAttributes.isEmpty()) {
                        Attribute attribute = new GTIngredientsAttribute(
                                target, target.uniqueName("operations"));
                        attribute.setPersistent(false);
                    }

                    EditorFactory factory = new GTIngredientsEditor.Factory(
                            target, target
                                    .uniqueName("ingredientsEditorFactory"));
                    factory.setPersistent(false);
                    factory.createEditor(target, frame);
                    factory.setContainer(null);
                } catch (KernelException e) {
                    throw new InternalErrorException(e);
                }
            }
        }

        ConfigureOperationsAction(String name) {
            super(name);
        }
    }

    private class GTActorGraphController extends ActorEditorGraphController {

        public class NewRelationAction extends
                ActorEditorGraphController.NewRelationAction {

            public void actionPerformed(ActionEvent e) {
                if (isTableActive()) {
                    return;
                } else {
                    super.actionPerformed(e);
                }
            }

        }

        protected void _createControllers() {
            super._createControllers();
            _entityController = new EntityController();
            _portController = new PortController();
        }

        protected void initializeInteraction() {
            super.initializeInteraction();

            MenuActionFactory newFactory = new GTMenuActionFactory(
                    _configureMenuFactory, _configureAction);
            _replaceFactory(_menuFactory, _configureMenuFactory, newFactory);
            _configureMenuFactory = newFactory;
        }

        private void _replaceFactory(PtolemyMenuFactory menuFactory,
                MenuItemFactory replacedFactory, MenuItemFactory replacement) {
            List<?> factories = menuFactory.menuItemFactoryList();
            int size = factories.size();
            for (int i = 0; i < size; i++) {
                MenuItemFactory factory = (MenuItemFactory) factories.get(0);
                menuFactory.removeMenuItemFactory(factory);
                if (factory == replacedFactory) {
                    menuFactory.addMenuItemFactory(replacement);
                } else {
                    menuFactory.addMenuItemFactory(factory);
                }
            }
        }

        private class EntityController extends ActorInstanceController {
            EntityController() {
                super(GTActorGraphController.this);

                MenuActionFactory newFactory = new GTMenuActionFactory(
                        _configureMenuFactory, _configureAction);
                _replaceFactory(_menuFactory, _configureMenuFactory, newFactory);
                _configureMenuFactory = newFactory;

                FigureAction operationsAction = new ConfigureOperationsAction(
                        "Operations");
                _configureMenuFactory.addAction(operationsAction, "Customize");
            }
        }

        private class PortController extends ExternalIOPortController {

            public PortController() {
                super(GTActorGraphController.this, AttributeController.FULL);

                MenuActionFactory newFactory = new GTMenuActionFactory(
                        _configureMenuFactory, _configureAction);
                _replaceFactory(_menuFactory, _configureMenuFactory, newFactory);
                _configureMenuFactory = newFactory;
            }
        }
    }

    private class GTFSMGraphController extends FSMGraphController {

    }

    private static class GTMenuActionFactory extends MenuActionFactory {

        public void addAction(Action action, String label) {
            _oldFactory.addAction(action, label);
        }

        public void addActions(Action[] actions, String label) {
            _oldFactory.addActions(actions, label);
        }

        public JMenuItem create(JContextMenu menu, NamedObj object) {
            JMenuItem menuItem = _oldFactory.create(menu, object);
            if (menuItem instanceof JMenu) {
                JMenu subMenu = (JMenu) menuItem;
                if (subMenu.getText().equals("Customize")) {
                    Component[] menuItems = subMenu.getMenuComponents();
                    for (Component itemComponent : menuItems) {
                        JMenuItem item = (JMenuItem) itemComponent;
                        Action action = item.getAction();
                        if (object instanceof PortMatcher) {
                            // Disable all the items for a PortMatcher, which
                            // should be configured by double-clicking the
                            // containing CompositeActor.
                            item.setEnabled(false);
                        } else if (action instanceof PortDialogAction
                                && object instanceof GTEntity) {
                            // Disable the PortDialogAction from the context
                            // menu.
                            item.setEnabled(false);
                        } else if (action instanceof ConfigureOperationsAction
                                && (!(object instanceof Entity) || !GTTools
                                        .isInReplacement(object))) {
                            // Hide the ConfigureOperationsAction from the
                            // context menu.
                            item.setVisible(false);
                        }
                    }
                }
            }
            return menuItem;
        }

        GTMenuActionFactory(MenuActionFactory oldFactory, Action configureAction) {
            super(configureAction);

            _oldFactory = oldFactory;
        }

        private MenuActionFactory _oldFactory;
    }
}
