/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;
import diva.util.SemanticObjectContainer;
import diva.util.PropertyContainer;
import java.util.Iterator;

/**
 * A graph is an object that contains nodes and
 * edges.  Edges are accessed through the nodes that
 * they connect.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface Graph extends SemanticObjectContainer, PropertyContainer {
    /**
     * Return an iterator over the nodes that this graph contains.
     */
    public Iterator nodes();

    /**
     * Return a count of the nodes this graph contains.
     */
    public int getNodeCount();
}

