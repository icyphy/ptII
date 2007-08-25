package ptolemy.vergil.gt;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.SingleRuleTransformer;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.JGraph;

public abstract class AbstractGTFrame extends ExtendedGraphFrame
implements ChangeListener {

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

    /** Set the center location of the visible part of the pane.
     *  This will cause the panner to center on the specified location
     *  with the current zoom factor.
     *  @param center The center of the visible part.
     *  @see #getCenter()
     */
    public void setCenter(Point2D center) {
        if (_graphs == null) {
            super.setCenter(center);
        } else {
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
            }
        }
    }

    protected abstract ActorEditorGraphController _createController();

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = _createController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new GraphPane(_controller, graphModel);
    }

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

    protected GraphController _getGraphController() {
        if (_tabbedPane == null) {
            return _controller;
        } else {
            int index = _tabbedPane.getSelectedIndex();
            return _graphs.get(index).getGraphPane().getGraphController();
        }
    }

    protected List<JGraph> _getGraphs() {
        return _graphs;
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
        // FIXME: Should override _setDropIntoEnabled to modify all the drop
        //        targets created.
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    /** The graph controller.  This is created in _createGraphPane(). */
    private ActorEditorGraphController _controller;

    private List<JGraph> _graphs;

    private JTabbedPane _tabbedPane;

    private static final long serialVersionUID = -7057287804004272089L;
}
