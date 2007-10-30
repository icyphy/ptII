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
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.JGraph;

public abstract class AbstractGTFrame extends ExtendedGraphFrame
implements ChangeListener, KeyListener {

    /** Construct a frame associated with the specified Ptolemy II model.
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
    public AbstractGTFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
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
    public AbstractGTFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
    }

    public CompositeEntity getActiveModel() {
        ActorGraphModel graphModel =
            (ActorGraphModel) _getGraphController().getGraphModel();
        return (CompositeEntity) graphModel.getPtolemyModel();
    }

    /** Return the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @return the JGraph.
     *  @see #setJGraph(JGraph)
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
            return super.getJGraph();
        }
    }

    public int getSelectedIndex() {
        return _selectedIndex;
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

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode()
                == (KeyEvent.VK_ALT | KeyEvent.VK_S)) {
            e.consume();
            cancelFullScreen();
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
            _selectedIndex = _tabbedPane.getSelectedIndex();
            if (_selectedIndex < _graphPanes.size()) {
                _controller.getSelectionModel().clearSelection();
                GraphPane graphPane = _graphPanes.get(_selectedIndex);
                _controller.setGraphPane(graphPane);
                _controller.setGraphModel(graphPane.getGraphModel());
            }
            _showTab(_selectedIndex);
        }
    }

    protected boolean _close() {
        boolean result = super._close();
        if (result && hasTabs()) {
            for (Component tab : _tabbedPane.getComponents()) {
                if (tab instanceof JGraph) {
                    AbstractBasicGraphModel graphModel =
                        (AbstractBasicGraphModel) ((JGraph) tab).getGraphPane()
                                .getGraphModel();
                    graphModel.removeListeners();
                }
            }
        }
        return result;
    }

    protected abstract ActorEditorGraphController _createController();

    protected GraphPane _createGraphPane(NamedObj entity) {
        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        ActorGraphModel graphModel = new ActorGraphModel(entity);
        GraphPane graphPane = new GraphPane(_controller, graphModel);
        if (_graphPanes != null) {
            _graphPanes.add(graphPane);
        }
        return graphPane;
    }

    protected JComponent _createRightComponent(NamedObj entity) {
        // entity must be SingleRuleTransformer or CompositeActorMatcher.

        _controller = _createController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        if (!(entity instanceof TransformationRule)) {
            JComponent component = super._createRightComponent(entity);
            return component;
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

            /** Serial ID */
            private static final long serialVersionUID = -4998226270980176175L;
        };

        _tabbedPane.addChangeListener(this);
        Iterator<?> cases = ((TransformationRule) entity).entityList(
                CompositeActorMatcher.class).iterator();
        boolean first = true;
        while (cases.hasNext()) {
            CompositeActorMatcher matcher =
                (CompositeActorMatcher) cases.next();
            JGraph jgraph = _addTabbedPane(matcher, false);
            // The first JGraph is the one with the focus.
            if (first) {
                first = false;
                setJGraph(jgraph);
            }
            _graphs.add(jgraph);
        }

        GraphPane graphPane = _graphPanes.get(0);
        _controller.setGraphPane(graphPane);
        _controller.setGraphModel(graphPane.getGraphModel());

        return _tabbedPane;
    }

    protected GraphController _getGraphController() {
        return _controller;
    }

    protected JTabbedPane _getTabbedPane() {
        return _tabbedPane;
    }

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
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    private void _showTab(int tabIndex) {
        Component tab = _tabbedPane.getComponent(tabIndex);
        if (tab instanceof JGraph) {
            setJGraph((JGraph) tab);
            if (_graphPanner != null) {
                _graphPanner.setCanvas((JGraph) tab);
            }
        } else {
            if (_graphPanner != null) {
                _graphPanner.setCanvas(null);
            }
        }
    }

    private ActorEditorGraphController _controller;

    private List<GraphPane> _graphPanes;

    private List<JGraph> _graphs;

    private int _selectedIndex = 0;

    private JTabbedPane _tabbedPane;

    private static final long serialVersionUID = -7057287804004272089L;
}
