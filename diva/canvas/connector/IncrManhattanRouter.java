/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.connector;

import java.awt.Shape;

import diva.util.java2d.Polyline2D;

/**
 * A manhattan router which does only incremental routing
 * and delegates static routing to another manhattan router
 * implementation.
 *
 * @version $Revision$
 * @author  Michael Shilman (michaels@eecs.berkeley.edu)
 * @author  John Reekie (johnr@eecs.berkeley.edu)
 * @rating  Red
 */
public class IncrManhattanRouter implements ManhattanRouter {
    /**
     * The static router that handles calls to route().
     */
    private ManhattanRouter _staticRouter;

    /**
     * Construct a new incremental router which delegates static
     * routing to the given manhattan router, but does incremental
     * routing (the reroute*() methods) on its own.
     */
    public IncrManhattanRouter(ManhattanRouter staticRouter) {
        _staticRouter = staticRouter;
    }

    /**
     * Reroute the given Shape, given that the head site moved.
     */
    public void rerouteHead (Connector c, Shape s) {
        Polyline2D line = (Polyline2D) s;

        line.setX(1, c.getHeadSite().getX());
        line.setY(1, c.getHeadSite().getY());
    }

    /**
     * Reroute the given Shape, given that the tail site moved.
     */
    public void rerouteTail (Connector c, Shape s) {
        Polyline2D line = (Polyline2D) s;
        line.setX(0, c.getHeadSite().getX());
        line.setY(0, c.getHeadSite().getY());
    }

    /**
     * Reroute the given shape, given that both the head the tail
     * sites moved. The shape is modified by the router.
     */
    public void reroute (Connector c, Shape s) {
        rerouteHead(c, s);
        rerouteTail(c, s);
    }

    /**
     * Delegate the static routing to the static router.
     */
    public Shape route (Connector c) {
        return _staticRouter.routeManhattan((ManhattanConnector)c);
    }

    /**
     * Delegate the static routing to the static router.
     */
    public Polyline2D routeManhattan (ManhattanConnector c) {
        return _staticRouter.routeManhattan(c);
    }
}



