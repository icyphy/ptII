/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;

/**
 * An edge is an object that is contained by a graph and connects
 * nodes.  An edge has a "head" and a "tail" as if it was directed,
 * but also has a method isDirected() that says whether or not the
 * edge should be treated as directed (e.g. should there be an arrow
 * drawn on the head).  An edge has a semantic object that is its
 * semantic equivalent in the application and may have a visual object
 * which is its syntactic representation in the user interface.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface EdgeModel {
    /**
     * Return the node at the head of this edge.
     */
    public Object getHead(Object edge);

    /**
     * Return the node at the tail of this edge.
     */
    public Object getTail(Object edge);

    /**
     * Return whether or not this edge is directed.
     */
    public boolean isDirected(Object edge);
}

