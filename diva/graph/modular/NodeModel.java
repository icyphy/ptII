/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;
import java.util.Iterator;

/**
 * A node is an object that is contained by a graph
 * and is connected to other nodes by edges.  A node
 * has a semantic object that is its semantic equivalent
 * in the application and may have a visual object which
 * is its syntactic representation in the user interface.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface NodeModel {
    /**
     * Return an iterator over the edges coming into the given node.
     */
    public Iterator inEdges(Object node);

    /**
     * Return an iterator over the edges coming out of the given node.
     */
    public Iterator outEdges(Object node);

    /**
     * Return the graph parent of the given node.
     */
    public Object getParent(Object node);
}



