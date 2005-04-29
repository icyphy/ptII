/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
package diva.graph.layout;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import diva.graph.GraphModel;


/**
 * The basic set of information necessary to layout a graph: a mapping
 * the graph data structure to aspects of its visual representation,
 * a viewport to layout in, and some manipulation routines including
 * pick, place, and route.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public interface LayoutTarget {
    /**
     * Return the bounds of the figure associated with the given node
     * in the target's view.
     */
    public Rectangle2D getBounds(Object node);

    /**
     * Return the graph model that provides a traversal
     * interface to the graph I'm trying to layout.
     */
    public GraphModel getGraphModel();

    /**
     * Return the visual object of the given graph object.  Note that the
     * purpose of a layout target is to abstract away the visual object and
     * using this method breaks that abstraction.
     */
    public Object getVisualObject(Object object);

    /**
     * Return the viewport of the given graph as a rectangle
     * in logical coordinates.
     */
    public Rectangle2D getViewport(Object composite);

    /**
     * Return whether or not the given node is actually
     * visible in the view.
     */
    public boolean isNodeVisible(Object node);

    /**
     * Return whether or not the given edge is actually
     * visible in the view.
     */
    public boolean isEdgeVisible(Object edge);

    /**
     * Return an iterator over edges in the view which intersect
     * the given rectangle.
     */
    public Iterator intersectingNodes(Rectangle2D r);

    /**
     * Return an iterator over nodes in the view which intersect
     * the given rectangle.
     */
    public Iterator intersectingEdges(Rectangle2D r);

    /**
     * Route absolutely the figure associated with the given edge in
     * the target's view.
     */
    public void route(Object edge);

    /**
     * Reroute the figure associated with the given edge in the
     * target's view given an incremental change in one of its
     * endpoints.
     */

    //    public void reroute(Edge e);

    /**
     * Translate the figure associated with the given node in the
     * target's view by the given delta.
     */
    public void translate(Object node, double dx, double dy);
}
