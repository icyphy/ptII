/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;
import diva.canvas.Site;
import diva.canvas.connector.Connector;

/**
 * A factory which creates a visual representation (EdgeFigure)
 * given an edge input.  The factory is not held responsible
 * for routing the edge, but simply for providing the
 * basic visual properties of the edge (color, line width,
 * dashes, etc.).  The client will set up the endpoints
 * of the edge, and then tell the edge to route itself.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface EdgeRenderer {
    /**
     * Render a visual representation of the given edge.
     */
    public Connector render(Object edge, Site tailSite, Site headSite);
}


