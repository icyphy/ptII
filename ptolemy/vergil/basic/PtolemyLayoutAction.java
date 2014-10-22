/* The default Ptolemy layout with place and route.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
 2
 */
package ptolemy.vergil.basic;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphFrame;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphUtilities;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LayoutTarget;
import diva.graph.layout.LevelLayout;

///////////////////////////////////////////////////////////////////
//// PtolemyLayoutAction

/**
Trigger the Ptolemy place and route automatic dataflow layout algorithm
from withing the Vergil GUI. Operate on the current model, hence the
model needs to be an input in the doAction() method.

<p>The Ptolemy layout mechanism produces layouts that are not as
good as the Kieler layout mechanism, so use the @see KielerLayoutMechanism.</p>

@author  Christopher Brooks, based on KielerLayoutAction by Christian Motika and BasicGraphFrame by Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cmot)
@Pt.AcceptedRating Red (cmot)
 */
public class PtolemyLayoutAction extends Object implements IGuiAction {

    /**
     * Layout the graph if the model is a CompositeActor. Otherwise throw an
     * exception. The frame type must be ActorGraphFrame. The Ptolemy layouter.
     * is called with placing and routing.
     *
     * @param model the model
     */
    @Override
    public void doAction(NamedObj model) {
        try {
            JFrame frame = null;
            Iterator tableaux = Configuration.findEffigy(model)
                    .entityList(Tableau.class).iterator();
            while (tableaux.hasNext()) {
                Tableau tableau = (Tableau) tableaux.next();
                if (tableau.getFrame() instanceof ActorGraphFrame) {
                    frame = tableau.getFrame();
                }
            }
            _graphFrame = (BasicGraphFrame) frame;

            // fetch everything needed to build the LayoutTarget
            GraphController graphController = _graphFrame.getJGraph()
                    .getGraphPane().getGraphController();
            AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) _graphFrame
                    .getJGraph().getGraphPane().getGraphController()
                    .getGraphModel();
            BasicLayoutTarget layoutTarget = new PtolemyLayoutTarget(
                    graphController);

            // create Ptolemy layouter for this layout target
            PtolemyLayout layout = new PtolemyLayout(layoutTarget);
            // layout.setModel((CompositeActor) model);
            // layout.setApplyEdgeLayout(false);
            // layout.setApplyEdgeLayoutBendPointAnnotation(true);
            // layout.setBoxLayout(false);
            // layout.setTop(graphFrame);

            layout.setOrientation(LevelLayout.HORIZONTAL);
            layout.setRandomizedPlacement(false);

            // Before doing the layout, need to take a copy of all the current
            // node locations  which can be used to undo the effects of the move.
            try {
                NamedObj composite = graphModel.getPtolemyModel();

                StringBuffer moml = new StringBuffer();
                moml.append("<group>\n");

                // NOTE: this gives at iteration over locations.
                Iterator<Location> nodes = graphModel.nodes(composite);

                while (nodes.hasNext()) {
                    Location location = nodes.next();

                    // Get the containing element.
                    NamedObj element = location.getContainer();

                    // Give default values in case the previous locations value
                    // has not yet been set.
                    String expression = location.getExpression();

                    if (expression == null) {
                        expression = "0, 0";
                    }

                    // Create the MoML, wrapping the location attribute
                    // in an element referring to the container.
                    String containingElementName = element.getElementName();
                    moml.append("<" + containingElementName + " name=\""
                            + element.getName() + "\" >\n");

                    // NOTE: use the moml info element name here in case the
                    // location is a vertex.
                    moml.append("<" + location.getElementName() + " name=\""
                            + location.getName() + "\" value=\"" + expression
                            + "\" />\n");
                    moml.append("</" + containingElementName + ">\n");
                }

                moml.append("</group>\n");

                // Push the undo entry onto the stack.
                MoMLUndoEntry undoEntry = new MoMLUndoEntry(composite,
                        moml.toString());
                UndoStackAttribute undoInfo = UndoStackAttribute
                        .getUndoInfo(composite);
                undoInfo.push(undoEntry);
            } catch (Throwable throwable) {
                // Operation not undoable.
            }

            layout.layout(graphModel.getRoot());
        } catch (Exception ex) {
            // If we do not catch exceptions here, then they
            // disappear to stdout, which is bad if we launched
            // where there is no stdout visible.
            MessageHandler.error("Failed to layout \""
                    + (model == null ? "name not found" : model.getFullName())
                    + "\"", ex);
        }
    }

    private BasicGraphFrame _graphFrame;

    ///////////////////////////////////////////////////////////////////
    //// PtolemyLayout

    /** A layout algorithm for laying out ptolemy graphs.  Since our edges
     *  are undirected, this layout algorithm turns them into directed edges
     *  aimed consistently. i.e. An edge should always be "out" of an
     *  internal output port and always be "in" of an internal input port.
     *  Conversely, an edge is "out" of an external input port, and "in" of
     *  an external output port.  The copying operation also flattens
     *  the graph, because the level layout algorithm doesn't understand
     *  how to layout hierarchical nodes.
     */
    private static class PtolemyLayout extends LevelLayout {
        // FIXME: input ports should be on left, and output ports on right.

        /** Construct a new levelizing layout with a vertical orientation. */
        public PtolemyLayout(LayoutTarget target) {
            super(target);
        }

        /** Copy the given graph and make the nodes/edges in the copied
         *  graph point to the nodes/edges in the original.
         */
        @Override
        protected Object copyComposite(Object origComposite) {
            LayoutTarget target = getLayoutTarget();
            GraphModel model = target.getGraphModel();
            diva.graph.basic.BasicGraphModel local = getLocalGraphModel();
            Object copyComposite = local.createComposite(null);
            HashMap<Object, Object> map = new HashMap<Object, Object>();

            // Copy all the nodes for the graph.
            for (Iterator<?> i = model.nodes(origComposite); i.hasNext();) {
                Object origNode = i.next();

                if (target.isNodeVisible(origNode)) {
                    Rectangle2D r = target.getBounds(origNode);
                    LevelInfo inf = new LevelInfo();
                    inf.origNode = origNode;
                    inf.x = r.getX();
                    inf.y = r.getY();
                    inf.width = r.getWidth();
                    inf.height = r.getHeight();

                    Object copyNode = local.createNode(inf);
                    local.addNode(this, copyNode, copyComposite);
                    map.put(origNode, copyNode);
                }
            }

            // Add all the edges.
            Iterator<?> i = GraphUtilities.partiallyContainedEdges(
                    origComposite, model);

            while (i.hasNext()) {
                Object origEdge = i.next();
                Object origTail = model.getTail(origEdge);
                Object origHead = model.getHead(origEdge);

                if (origHead != null && origTail != null) {
                    Figure tailFigure = (Figure) target
                            .getVisualObject(origTail);
                    Figure headFigure = (Figure) target
                            .getVisualObject(origHead);

                    // Swap the head and the tail if it will improve the
                    // layout, since LevelLayout only uses directed edges.
                    if (tailFigure instanceof Terminal) {
                        Terminal terminal = (Terminal) tailFigure;
                        Site site = terminal.getConnectSite();

                        if (site instanceof FixedNormalSite) {
                            double normal = site.getNormal();
                            int direction = CanvasUtilities
                                    .getDirection(normal);

                            if (direction == SwingConstants.WEST) {
                                Object temp = origTail;
                                origTail = origHead;
                                origHead = temp;
                            }
                        }
                    } else if (headFigure instanceof Terminal) {
                        Terminal terminal = (Terminal) headFigure;
                        Site site = terminal.getConnectSite();

                        if (site instanceof FixedNormalSite) {
                            double normal = site.getNormal();
                            int direction = CanvasUtilities
                                    .getDirection(normal);

                            if (direction == SwingConstants.EAST) {
                                Object temp = origTail;
                                origTail = origHead;
                                origHead = temp;
                            }
                        }
                    }

                    origTail = _getParentInGraph(model, origComposite, origTail);
                    origHead = _getParentInGraph(model, origComposite, origHead);

                    Object copyTail = map.get(origTail);
                    Object copyHead = map.get(origHead);

                    if (copyHead != null && copyTail != null) {
                        Object copyEdge = local.createEdge(origEdge);
                        local.setEdgeTail(this, copyEdge, copyTail);
                        local.setEdgeHead(this, copyEdge, copyHead);
                    }
                }
            }

            return copyComposite;
        }

        // Unfortunately, the head and/or tail of the edge may not
        // be directly contained in the graph.  In this case, we need to
        // figure out which of their parents IS in the graph
        // and calculate the cost of that instead.
        private Object _getParentInGraph(GraphModel model, Object graph,
                Object node) {
            while (node != null && !model.containsNode(graph, node)) {
                Object parent = model.getParent(node);

                if (model.isNode(parent)) {
                    node = parent;
                } else {
                    node = null;
                }
            }

            return node;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PtolemyLayoutTarget

    /** A layout target that translates locatable nodes. */
    private/*static*/class PtolemyLayoutTarget extends BasicLayoutTarget {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        // However, we call getVisibleCanvasRectangle(), which cannot
        // be static.

        /** Construct a new layout target that operates
         *  in the given pane.
         */
        public PtolemyLayoutTarget(GraphController controller) {
            super(controller);
        }

        /** Return the viewport of the given graph as a rectangle
         *  in logical coordinates.
         */
        @Override
        public Rectangle2D getViewport(Object composite) {
            //GraphModel model = getController().getGraphModel();

            if (composite == getRootGraph()) {
                // Take into account the current zoom and pan.
                Rectangle2D bounds = _graphFrame.getVisibleCanvasRectangle();

                double width = bounds.getWidth();
                double height = bounds.getHeight();

                double borderPercentage = (1 - getLayoutPercentage()) / 2;
                double x = borderPercentage * width + bounds.getX();
                double y = borderPercentage * height + bounds.getY();
                double w = getLayoutPercentage() * width;
                double h = getLayoutPercentage() * height;
                return new Rectangle2D.Double(x, y, w, h);
            } else {
                return super.getViewport(composite);
            }
        }

        /** Translate the figure associated with the given node in the
         *  target's view by the given delta.
         */
        @Override
        public void translate(Object node, double dx, double dy) {
            super.translate(node, dx, dy);

            if (node instanceof Locatable) {
                double[] location = ((Locatable) node).getLocation();

                if (location == null) {
                    location = new double[2];

                    Figure figure = getController().getFigure(node);
                    location[0] = figure.getBounds().getCenterX();
                    location[1] = figure.getBounds().getCenterY();
                } else {
                    location[0] += dx;
                    location[1] += dy;
                }

                try {
                    ((Locatable) node).setLocation(location);
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.getMessage());
                }
            }
        }
    }
}
