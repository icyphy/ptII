/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;

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
public interface MutableNodeModel extends NodeModel {

    /**
     * Set the graph parent of the given node.  Implementors of this method
     * are also responsible for insuring that it is set properly as
     * the child of the graph in the graph.
     */
    public void setParent(Object node, Object composite);
}



