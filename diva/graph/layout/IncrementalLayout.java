/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;

/**
 * A layout engine which operates incrementally, based on the arrival
 * of new node information.  An incremental layout is simply a kind
 * of graph listener that updates the layout with every change in
 * the graph.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface IncrementalLayout extends GlobalLayout {

    /** Called in response to the given edge being given a figure.
     */
    public void edgeDrawn(Object edge);

    /** Called in response to the connector representing the given edge being
     *  rereouted.
     */
    public void edgeRouted(Object edge);

    /** Called in response to the given node being given a figure.
     */
    public void nodeDrawn(Object node);

    /** Called in response to the figure representing the
     *  given node being moved.
     */
    public void nodeMoved(Object node);
}

