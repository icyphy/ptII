/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;

/**
 * A static layout engine which layouts traverse the graph structure and
 * performs layout from scratch, ignoring the previous positions of the nodes
 * in the graphs.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface GlobalLayout {

    /** Return the layout target.
     */
    public LayoutTarget getLayoutTarget();

    /** Set the layout target.
     */
    public void setLayoutTarget(LayoutTarget target);

    /**
     * Layout the graph model in the viewport
     * specified by the layout target environment.
     */
    public void layout(Object composite);
}


