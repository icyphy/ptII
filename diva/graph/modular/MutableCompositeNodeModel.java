/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;

/**
 * A graph is an object that contains nodes and
 * edges.  Edges are accessed through the nodes that
 * they connect.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface MutableCompositeNodeModel extends MutableNodeModel,
                                                   CompositeModel {
}

