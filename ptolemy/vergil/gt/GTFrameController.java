/*

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.FSMMatcher;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.modal.FSMGraphModel;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.toolbox.JCanvasPanner;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTFrameController implements ChangeListener, KeyListener {

    public CompositeEntity getActiveModel() {
        AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) getGraphController()
                .getGraphModel();
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

    public List<JGraph> getJGraphs() {
        return _graphs;
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

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            e.consume();
            _frame.cancelFullScreen();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    @Override
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

    public static class GTActorGraphModel extends ActorGraphModel implements
    UpdateController {

        public GTActorGraphModel(NamedObj composite) {
            super(composite);
        }

        @Override
        public synchronized void startUpdate() {
            _updateStopped = false;
        }

        @Override
        public synchronized void stopUpdate() {
            _updateStopped = true;
        }

        @Override
        protected synchronized boolean _update() {
            if (!_updateStopped) {
                return super._update();
            } else {
                return true;
            }
        }

        private boolean _updateStopped = false;
    }

    public static class GTFSMGraphModel extends FSMGraphModel implements
    UpdateController {

        public GTFSMGraphModel(CompositeEntity composite) {
            super(composite);
        }

        @Override
        public synchronized void startUpdate() {
            _updateStopped = false;
        }

        @Override
        public synchronized void stopUpdate() {
            _updateStopped = true;
        }

        @Override
        protected synchronized boolean _update() {
            if (!_updateStopped) {
                return super._update();
            } else {
                return true;
            }
        }

        private boolean _updateStopped = false;
    }

    public interface UpdateController {

        public void startUpdate();

        public void stopUpdate();
    }

    protected GTFrameController(GTFrame frame) {
        _frame = frame;
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

    @SuppressWarnings("serial")
    protected JComponent _createRightComponent(NamedObj entity) {
        // entity must be SingleRuleTransformer or CompositeActorMatcher.

        if (_isFSM(entity)) {
            _graphController = _frame._createFSMGraphController();
        } else {
            _graphController = _frame._createActorGraphController();
        }
        _graphController.setConfiguration(getConfiguration());
        _graphController.setFrame(_frame);

        if (!(entity instanceof TransformationRule)
                || ((TransformationRule) entity).mode.isMatchOnly()) {
            return null;
        }

        _graphPanes = new LinkedList<GraphPane>();
        _graphs = new LinkedList<JGraph>();

        _tabbedPane = new JTabbedPane() {
            @Override
            public void setMinimumSize(Dimension minimumSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setMinimumSize(minimumSize);
                }
            }

            @Override
            public void setPreferredSize(Dimension preferredSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setPreferredSize(preferredSize);
                }
            }

            @Override
            public void setSize(int width, int height) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setSize(width, height);
                }
            }

        };

        _tabbedPane.addChangeListener(this);
        try {
            entity.workspace().getReadAccess();
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
        } finally {
            entity.workspace().doneReading();
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
                    AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) ((JGraph) tab)
                            .getGraphPane().getGraphModel();
                    graphModel.removeListeners();
                }
            }
        }
    }

    /** Add a tabbed pane for the specified matcher.
     *  @param matcher The matcher
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
            @Override
            public void mousePressed(LayerEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }
        });
        JGraph jgraph = new JGraph(pane);
        jgraph.addKeyListener(this);
        String name = matcher.getName();
        jgraph.setName(name);
        int index = _tabbedPane.getComponentCount();
        // Put before the default pane, unless this is the default.
        if (newPane) {
            index--;
        }
        _tabbedPane.add(jgraph, index);

        // Background color is parameterizable by preferences.
        Configuration configuration = getConfiguration();
        jgraph.setBackground(BasicGraphFrame.BACKGROUND_COLOR);
        if (configuration != null) {
            try {
                PtolemyPreferences preferences = PtolemyPreferences
                        .getPtolemyPreferencesWithinConfiguration(configuration);
                if (preferences != null) {
                    jgraph.setBackground(preferences.backgroundColor.asColor());
                }
            } catch (IllegalActionException e1) {
                // Ignore the exception and use the default color.
            }
        }

        // Create a drop target for the jgraph.
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    private boolean _isFSM(NamedObj entity) {
        return entity instanceof FSMMatcher || entity instanceof FSMActor;
    }

    private void _showTab(int tabIndex) {
        Component tab = _tabbedPane.getComponent(tabIndex);
        JCanvasPanner panner = _frame._getGraphPanner();
        if (tab instanceof JGraph) {
            _frame.setJGraph((JGraph) tab);
            tab.requestFocus();
            if (panner != null) {
                panner.setCanvas((JGraph) tab);
            }
        } else {
            if (panner != null) {
                panner.setCanvas(null);
            }
        }
    }

    private int _activeTabIndex = 0;

    private GTFrame _frame;

    private RunnableGraphController _graphController;

    private List<GraphPane> _graphPanes;

    private List<JGraph> _graphs;

    private JTabbedPane _tabbedPane;
}
