/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
 */
package diva.graph.basic;

import diva.graph.layout.LayoutTarget;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.canvas.Figure;
import diva.canvas.CompositeFigure;
import diva.canvas.GraphicsPane;
import diva.canvas.ZList;
import diva.canvas.connector.Connector;
import diva.util.FilteredIterator;
import diva.util.ProxyIterator;
import diva.util.Filter;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

/**
 * The basic set of information necessary to layout a graph: a mapping
 * the graph data structure to aspects of its visual representation, a
 * viewport to layout in, and some manipulation routines including
 * pick, place, and route.  This is an implementation for the Diva
 * canvas, other layout target implementations can "port" the layout
 * algorithms to other display implementations.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Id$
 * @rating Red
 */
public class BasicLayoutTarget implements LayoutTarget {
    /**
     * The graph controller that this is performing
     * layout on.
     */
    private GraphController _controller;

    /**
     * The percentage of the screen that should be used
     * for layout; default value is .8.
     */
    private double _layoutPercentage = .8;

    /**
     * Construct a new layout target that operates
     * in the given pane.
     */
    public BasicLayoutTarget(GraphController controller) {
        _controller = controller;
    }

    /**
     * Return the bounds of the figure associated with the given node
     * in the target's view.
     */
    public Rectangle2D getBounds(Object node) {
        Figure f = (Figure)_controller.getFigure(node);
        return f.getBounds();
    }

    /**
     * Return the graph controller.
     */
    public GraphController getController() {
        return _controller;
    }

    /**
     * Return the graph model that we are operating on.
     */
    public GraphModel getGraphModel() {
        return _controller.getGraphModel();
    }

    /**
     * Return the percentage of the pane that is used for layout.
     *
     * @see #setLayoutPercentage(double)
     */
    public double getLayoutPercentage() {
        return _layoutPercentage;
    }

    /**
     * Return the pane of this display.  This is a template
     * method for lazy subclassers.
     */
    protected GraphicsPane getGraphicsPane() {
        return _controller.getGraphPane();
    }

    /**
     * Return the root graph of this display.  This is a template
     * method for lazy subclassers.
     */
    protected Object getRootGraph() {
        return _controller.getGraphModel().getRoot();
    }


    /**
     * Return the viewport of the given graph as a rectangle
     * in logical coordinates.
     */
    public Rectangle2D getViewport(Object composite) {
        GraphModel model = _controller.getGraphModel();
        if (composite == getRootGraph()) {
            Point2D p = getGraphicsPane().getSize();

            double borderPercentage = (1-getLayoutPercentage())/2;
            double x = borderPercentage*p.getX();
            double y = borderPercentage*p.getY();
            double w = getLayoutPercentage()*p.getX();
            double h = getLayoutPercentage()*p.getY();
            return new Rectangle2D.Double(x, y, w, h);
        }
        else if (model.isComposite(composite)) {
            CompositeFigure cf = (CompositeFigure)_controller.getFigure(composite);
            if (cf != null) {
                return cf.getShape().getBounds2D();
            }
        }
        String err = "Unknown graph.  Cannot determine viewport.";
        throw new IllegalArgumentException(err);
    }

    /**
     * Return the visual object of the given graph object.  Note that the
     * purpose of a layout target is to abstract away the visual object and
     * using this method breaks that abstraction.
     */
    public Object getVisualObject(Object object) {
        return _controller.getFigure(object);
    }

    /**
     * Return whether or not the given node is actually
     * visible in the view.
     */
    public boolean isNodeVisible(Object node) {
        Figure nf = (Figure)_controller.getFigure(node);
        return (nf != null && nf.isVisible() && nf.getParent() != null);
    }

    /**
     * Return whether or not the given edge is actually
     * visible in the view.
     */
    public boolean isEdgeVisible(Object edge) {
        Connector ef = (Connector)_controller.getFigure(edge);
        return (ef != null && ef.isVisible() && ef.getParent() != null);
    }

    /**
     * Return an iterator over the nodes which intersect the given
     * rectangle in the top-level graph.
     */
    public Iterator intersectingNodes(Rectangle2D r) {
        final GraphModel model = _controller.getGraphModel();
        ZList zlist = getGraphicsPane().getForegroundLayer().getFigures();
        Iterator i = zlist.getIntersectedFigures(r).figuresFromFront();
        Iterator j = new FilteredIterator(i, new Filter() {
                public boolean accept(Object o) {
                    Figure f = (Figure)o;
                    return (model.isNode(f.getUserObject()));
                }
            });

        return new ProxyIterator(j) {
                public Object next() {
                    Figure nf = (Figure)super.next();
                    return nf.getUserObject();
                }
            };
    }

    /**
     * Return an iterator over the node or edge figures which
     * intersect the given rectangle.
     */
    public Iterator intersectingEdges(Rectangle2D r) {
        ZList zlist = getGraphicsPane().getForegroundLayer().getFigures();
        Iterator i = zlist.getIntersectedFigures(r).figuresFromFront();
        Iterator j = new FilteredIterator(i, new Filter() {
                public boolean accept(Object o) {
                    return (o instanceof Connector);
                }
            });
        return new ProxyIterator(j) {
                public Object next() {
                    Connector ef = (Connector)super.next();
                    return ef.getUserObject();
                }
            };
    }

    /**
     * Route absolutely the figure associated with the given edge in
     * the target's view.
     */
    public void route(Object edge) {
        Connector ef = (Connector)_controller.getFigure(edge);
        // FIXME this should just call route(), but the way connectors
        // handle rerouting is kindof broken.  see fixme in
        // abstractconnector.reroute();
        ef.reroute();
    }

    /**
     * Set the percentage of the pane that should be used for layout.
     * This method defines a frame in which layout is executed that
     * is sized layoutPercentage*bounds, where "bounds" is the rectangular
     * bounding box of the visual representation of a particular graph.
     *
     * @see #getViewport(Object)
     */
    public void setLayoutPercentage(double d) {
        if ((d <= 0) || d > 1) {
            String err = "Layout percentage must be between 0 and 1";
            throw new IllegalArgumentException(err);
        }
        else {
            _layoutPercentage = d;
        }
    }

    /**
     * Translate the figure associated with the given node in the
     * target's view by the given delta.
     */
    public void translate(Object node, double dx, double dy) {
        Figure f = (Figure)_controller.getFigure(node);
        f.translate(dx,dy);
    }
}


