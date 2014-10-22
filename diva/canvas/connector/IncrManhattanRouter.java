/*
 Copyright (c) 1998-2014 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.canvas.connector;

import java.awt.Shape;

import diva.util.java2d.Polyline2D;

/**
 * A manhattan router which does only incremental routing
 * and delegates static routing to another manhattan router
 * implementation.
 *
 * @version $Id$
 * @author  Michael Shilman
 * @author  John Reekie
 * @Pt.AcceptedRating  Red
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
    @Override
    public void rerouteHead(Connector c, Shape s) {
        Polyline2D line = (Polyline2D) s;

        line.setX(1, c.getHeadSite().getX());
        line.setY(1, c.getHeadSite().getY());
    }

    /**
     * Reroute the given Shape, given that the tail site moved.
     */
    @Override
    public void rerouteTail(Connector c, Shape s) {
        Polyline2D line = (Polyline2D) s;
        line.setX(0, c.getHeadSite().getX());
        line.setY(0, c.getHeadSite().getY());
    }

    /**
     * Reroute the given shape, given that both the head the tail
     * sites moved. The shape is modified by the router.
     */
    @Override
    public void reroute(Connector c, Shape s) {
        rerouteHead(c, s);
        rerouteTail(c, s);
    }

    /**
     * Delegate the static routing to the static router.
     */
    @Override
    public Shape route(Connector c) {
        return _staticRouter.routeManhattan((ManhattanConnector) c);
    }

    /**
     * Delegate the static routing to the static router.
     */
    @Override
    public Polyline2D routeManhattan(ManhattanConnector c) {
        return _staticRouter.routeManhattan(c);
    }
}
