/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;
import diva.graph.GraphModel;
import diva.graph.GraphUtilities;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Miscellaneous utility routines used in layout.
 *
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @version        $Revision$
 * @rating Red
 */
public final class LayoutUtilities {
    /**
     * Place the given node at the given position and
     * reroute its edges.
     */
    public static final void place(LayoutTarget target, Object node,
            double x, double y) {
        GraphModel model = target.getGraphModel();
        placeNoReroute(target, node, x, y);

        for(Iterator i = model.inEdges(node); i.hasNext(); ) {
            Object edge = i.next();
            if(target.isEdgeVisible(edge)) {
                target.route(edge); //XXX reroute
            }
        }
        for(Iterator i = model.outEdges(node); i.hasNext(); ) {
            Object edge = i.next();
            if(target.isEdgeVisible(edge)) {
                target.route(edge); //XXX reroute
            }
        }
        //XXX reroute children if it's a composite node!
    }

    /**
     * Place the given node at the given position but do
     * not reroute its edges.
     */
    public static final void placeNoReroute(LayoutTarget target,
            Object node, double x, double y) {
        Rectangle2D bounds = target.getBounds(node);
        target.translate(node, x-bounds.getWidth()/2-bounds.getX(),
                y-bounds.getHeight()/2-bounds.getY());
    }


    /**
     * Check consistency of the graph in terms of topology and
     * layout.
     */
    public static final boolean checkConsistency(Object composite,
            GraphModel model) {
        if(!GraphUtilities.checkConsistency(composite, model)) {
            return false;
        }
        return checkCommon(composite, model);
    }

    /**
     * Check consistency and containment in terms of topology and
     * layout.
     */
    public static final boolean checkContainment(Object composite,
            GraphModel model) {
        if(!GraphUtilities.checkContainment(composite, model)) {
            return false;
        }
        return checkCommon(composite, model);
    }


    /**
     * A common layout consistency check shared by checkConsistency()
     * and checkContainment().
     */
    private static final boolean checkCommon(Object composite,
            GraphModel model) {
        /*
          for(Iterator i = g.nodes(); i.hasNext(); ) {
          Node n = (Node)i.next();
          if(target.getLayoutNode(n) == null) {
          return false;
          }

          for(Iterator j = n.outEdges(); j.hasNext(); ) {
          Edge e = (Edge)j.next();
          if((target.getLayoutEdge(e) == null) ||
          (target.getLayoutNode(e.getHead()) == null)) {
          return false;
          }
          }
          for(Iterator j = n.inEdges(); j.hasNext(); ) {
          Edge e = (Edge)j.next();
          if((target.getLayoutEdge(e) == null) ||
          (target.getLayoutNode(e.getTail()) == null)) {
          return false;
          }
          }
          }
          return true;
        */
        //XXX
        return true;
    }

    /**
     * Iterate over all the visible edges in the given graph and reroute
     * them.
     */
    public static final void routeVisibleEdges(Object composite,
            LayoutTarget target) {
        for(Iterator i = GraphUtilities.localEdges(composite,
                target.getGraphModel()); i.hasNext();) {
            Object edge = i.next();
            if(target.isEdgeVisible(edge)) {
                target.route(edge);
            }
        }
    }
}



