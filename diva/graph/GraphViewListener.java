/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;

/**
 * A listener for changes in a graph's structure or contents,
 * which are communicated through GraphViewEvent objects.  GraphViewListeners
 * register themselves with a GraphViewModel object, and receive events
 * from Nodes and Edges contained by that model's root graph
 * or any of its subgraphs.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface GraphViewListener extends java.util.EventListener {

    /**
     * The figure representing a node was moved.
     */
    public void nodeMoved(GraphViewEvent e);

    /**
     * The connector representing an edge was just routed.
     */
    public void edgeRouted(GraphViewEvent e);

    /**
     * A figure representing a node was just drawn.
     */
    public void nodeDrawn(GraphViewEvent e);

    /**
     * A connector representing an edge was just drawn.
     */
    public void edgeDrawn(GraphViewEvent e);
}
