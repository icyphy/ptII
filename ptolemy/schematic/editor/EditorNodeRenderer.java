/* The node renderer for ptolemy schematic objects.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.editor;

import diva.graph.*;
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.util.java2d.*;
import java.awt.*;
import java.awt.geom.*;
import ptolemy.schematic.util.*;

/**
 * A factory which creates figures for nodes in the schematic editor.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Id$
 */
public class EditorNodeRenderer implements NodeRenderer {
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
    public EditorNodeRenderer() {
        this(new Rectangle2D.Double(0.0,0.0,40.0,40.0), new Rectangle2D.Double(0.0,0.0,200.0,200.0), Color.red, Color.orange, .3);
    }

    /**
     * Create a renderer which renders nodes using the
     * given shape and fill paint.  The given shape must be
     * cloneable.
     */
    public EditorNodeRenderer(Shape nodeShape, Shape compositeShape, Paint nodeFill, Paint compositeFill, double compositeScale) {
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
     * Return a shape given a URL
     * FIXME hack for testing underlying stuff.
     */
    /*    public Figure getFigure(String url) {
        try {
            PTMLParser parser = new PTMLParser();
            XMLElement root = parser.parse(url);
            IconLibrary library = PTMLObjectFactory.createIconLibrary(root);
            Icon icon = (Icon) library.icons().nextElement();
            return icon.createFigure();
        }
        catch (Exception ex) {
            return null;
        }
	}*/
            
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
    public Figure render(Node n) {
        Shape shape = (n instanceof CompositeNode) ? _compositeShape : _nodeShape;
        if(shape instanceof RectangularShape) {
            RectangularShape r = (RectangularShape)shape;
            shape = (Shape)(r.clone());
        }
        else {
            shape = new GeneralPath(shape);
        }
        Paint fill = (n instanceof CompositeNode) ? _compositeFill : _nodeFill;

        BasicFigure bf = new BasicFigure(shape);
        bf.setFillPaint(fill);
        bf.setUserObject(n);
                
        if(n instanceof CompositeNode) {
            CompositeFigure rep = new CompositeFigure(bf);
            rep.setUserObject(n);
            double scale = getCompositeScale();
            rep.getTransformContext().getTransform().scale(scale, scale);

	    Icon icon = ((SchematicEntity)n).getIcon();	    
            Figure figure = icon.createFigure();
		//getFigure("file:/users/neuendor/ptII/ptolemy/" + 
		//		 "schematic/util/test/" + 
		//		 "exampleIconLibrary.ptml");

	    return figure;
        }

        //if it is not composite
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
        if((scale <= 0) || (scale > 1)) {
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
