/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;

/**
 * A listener for changes in a graph's structure or contents,
 * which are communicated through GraphEvent objects.  GraphListeners
 * register themselves with a GraphModel object, and receive events
 * from Nodes and Edges contained by that model's root graph
 * or any of its subgraphs.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Yellow
 */
public interface GraphListener extends java.util.EventListener {

    /**
     * An edge's head has been changed in a registered
     * graph or one of its subgraphs.  The added edge
     * is the "source" of the event.  The previous head
     * is accessible via e.getOldValue().
     */
    public void edgeHeadChanged(GraphEvent e);

    /**
     * An edge's tail has been changed in a registered
     * graph or one of its subgraphs.  The added edge
     * is the "source" of the event.  The previous tail
     * is accessible via e.getOldValue().
     */
    public void edgeTailChanged(GraphEvent e);

     /**
     * A node has been been added to the registered
     * graph or one of its subgraphs.  The added node
     * is the "source" of the event.
     */
    public void nodeAdded(GraphEvent e);

    /**
     * A node has been been deleted from the registered
     * graphs or one of its subgraphs.  The deleted node
     * is the "source" of the event.  The previous parent
     * graph is accessible via e.getOldValue().
     */
    public void nodeRemoved(GraphEvent e);

    /**
     * The structure of the event's "source" graph has
     * been drastically changed in some way, and this
     * event signals the listener to refresh its view
     * of that graph from model.
     */
    public void structureChanged(GraphEvent e);
}


