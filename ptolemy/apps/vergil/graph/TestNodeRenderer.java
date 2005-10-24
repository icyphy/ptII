/*
 * $Id$
 *
 * Copyright (c) 1998-2005 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package ptolemy.apps.vergil.graph;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.NodeController;
import diva.graph.NodeRenderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;


/**
 * A factory which creates and returns a NodeFigure given a node input
 * to render.
 *
 * @author  Michael Shilman
 * @version $Revision$
 * @Pt.AcceptedRating  Red
 */
public class MetaNodeRenderer extends CompositeEntity implements NodeRenderer {
    /**
     * The shape for nodes.
     */
    private Shape _nodeShape = null;

    /**
     * The shape for composite nodes.
     */
    private Shape _compositeShape = null;

    /**
     * The scaling factor for composite nodes.
     *
     * @see #setCompositeScale(double)
     */
    private double _compositeScale = 0;

    /**
     * The fill paint for nodes.
     */
    private Paint _nodeFill = null;

    /**
     * The fill paint for composite nodes.
     */
    private Paint _compositeFill = null;

    /**
     * Create a renderer which renders nodes square and orange.
     */
    public MetaNodeRenderer(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        this(container, name, new Rectangle2D.Double(0.0, 0.0, 40.0, 40.0),
            new Rectangle2D.Double(0.0, 0.0, 600.0, 600.0), Color.orange,
            Color.red, .3);
    }

    /**
     * Create a renderer which renders nodes using the
     * given shape and fill paint.  The given shape must be
     * cloneable.
     */
    public MetaNodeRenderer(CompositeEntity container, String name,
        Shape nodeShape, Shape compositeShape, Paint nodeFill,
        Paint compositeFill, double compositeScale)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setNodeShape(nodeShape);
        setNodeFill(nodeFill);
        setCompositeShape(compositeShape);
        setCompositeFill(compositeFill);
        setCompositeScale(compositeScale);
    }

    /**
     * Return the fill that composites are painted with.
     */
    public Paint getCompositeFill() {
        return _compositeFill;
    }

    /**
     * Return the scaling factor for the composite nodes
     *
     * @see #setCompositeScale(double)
     */
    public double getCompositeScale() {
        return _compositeScale;
    }

    /**
     * Return the shape that composites are rendered in.
     */
    public Shape getCompositeShape() {
        return _compositeShape;
    }

    /**
     * Return the graph controller.
     */
    public GraphController getGraphController() {
        return ((NodeController) getContainer()).getController();
    }

    /**
     * Return the fill that nodes are painted with.
     */
    public Paint getNodeFill() {
        return _nodeFill;
    }

    /**
     * Return the shape that nodes are rendered in.
     */
    public Shape getNodeShape() {
        return _nodeShape;
    }

    /**
     * Return the rendered visual representation of this node.
     */
    public Figure render(Object node) {
        GraphModel model = getGraphController().getGraphModel();
        Shape shape = (model.isComposite(node)) ? _compositeShape : _nodeShape;

        if (shape instanceof RectangularShape) {
            RectangularShape r = (RectangularShape) shape;
            shape = (Shape) (r.clone());
        } else {
            shape = new GeneralPath(shape);
        }

        Paint fill = model.isComposite(node) ? _compositeFill : _nodeFill;

        BasicFigure bf = new BasicFigure(shape);
        bf.setFillPaint(fill);

        if (model.isComposite(node)) {
            CompositeFigure rep = new CompositeFigure(bf);
            double scale = getCompositeScale();
            rep.getTransformContext().getTransform().scale(scale, scale);
            return rep;
        }

        bf.setToolTipText(bf.toString());
        return bf;
    }

    /**
     * Set the fill to paint the composites with.
     */
    public void setCompositeFill(Paint p) {
        _compositeFill = p;
    }

    /**
     * Set the scaling factor for the composite nodes.
     * Given factor must be greater than 0 and less than
     * or equal to 1.
     *
     * (XXX document this).
     */
    public void setCompositeScale(double scale) {
        if ((scale <= 0) || (scale > 1)) {
            String err = "Scale must be between > 0 and <= 1.";
            throw new IllegalArgumentException(err);
        }

        _compositeScale = scale;
    }

    /**
     * Set the shape for composites to be rendered in.  The
     * shape must implement Cloneable.
     */
    public void setCompositeShape(Shape s) {
        _compositeShape = s;
    }

    /**
     * Set the fill to paint the nodes with.
     */
    public void setNodeFill(Paint p) {
        _nodeFill = p;
    }

    /**
     * Set the shape for nodes to be rendered in.  The
     * shape must implement Cloneable.
     */
    public void setNodeShape(Shape s) {
        _nodeShape = s;
    }
}
