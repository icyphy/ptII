/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.util.java2d.Polyline2D;

/** A ManhattanRouter is an object that routes Polylines as a series
 * of perpendicular edges. This interface extends the parent interface
 * with a couple of methods specific to Polylines.
 *
 * @version $Revision$
 * @author  Michael Shilman (michaels@eecs.berkeley.edu)
 * @author  John Reekie (johnr@eecs.berkeley.edu)
 */
public interface ManhattanRouter extends Router {

    /** Route the given connector, returning a Polyline2D. This
     * method is the same as route(), except that the return
     * type is tighter.
     */
    public Polyline2D routeManhattan (ManhattanConnector c);
}


