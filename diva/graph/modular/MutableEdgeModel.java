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
public interface MutableEdgeModel extends EdgeModel {
    /**
     * Return whether or not the given node is a valid
     * head of this edge.
     */
    public boolean acceptHead(Object edge, Object head);

    /**
     * Return whether or not the given node is a valid
     * tail of this edge.
     */
    public boolean acceptTail(Object edge, Object tail);

    /**
     * Set the node that this edge points to.  Implementors
     * of this method are also responsible for insuring
     * that it is set properly as an "incoming" edge of
     * the node, and that it is removed as an incoming
     * edge from its previous head node.
     */
    public void setHead(Object edge, Object head);

    /**
     * Set the node that this edge stems from.  Implementors
     * of this method are also responsible for insuring
     * that it is set properly as an "outgoing" edge of
     * the node, and that it is removed as an outgoing
     * edge from its previous tail node.
     */
    public void setTail(Object edge, Object tail);
}

