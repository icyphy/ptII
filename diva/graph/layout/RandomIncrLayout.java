/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Randomly place newly-added nodes within the target viewport.  This
 * class tries to be smart by not placing nodes on top of one another
 * if possible, but doesn't guarantee anything about the layout except
 * that it will fall into the required viewport.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class RandomIncrLayout implements IncrementalLayout {
    private RandomLayout _global;
    private static final int NUM_ITER = 10;

    /**
     * Construct a random layout that lays out in
     * the context of the given layout target.
     */
    public RandomIncrLayout(LayoutTarget target) {
        _global = new RandomLayout(target);
    }

    /**
     * Debugging output to standard out.
     */
    private void debug(String s) {
        System.out.println("RandomIncrLayout: " + s);
    }

    /** Called in response to the given edge being drawn.
     */
    public void edgeDrawn(Object edge) {
        // do nothing
    }

    /** Called in response to the connector representing the given edge being
     *  rereouted.
     */
    public void edgeRouted(Object edge) {
        // do nothing
    }

    /**
     * Lay out the given node randomly, trying not
     * to overlap it with existing nodes.
     */
    public void nodeDrawn(Object node) {
        if (getLayoutTarget().isNodeVisible(node)) {
            Rectangle2D vp = getLayoutTarget().getViewport(getLayoutTarget().getGraphModel().getParent(node));
            Rectangle2D bounds = getLayoutTarget().getBounds(node);
            for (int i = 0; i < NUM_ITER; i++) {
                double x = vp.getX() +
                    Math.abs(Math.random()) * vp.getWidth();
                double y = vp.getY() +
                    Math.abs(Math.random()) * vp.getHeight();

                LayoutUtilities.place(getLayoutTarget(), node, x, y);
                bounds = getLayoutTarget().getBounds(node);
                boolean overlap = false;
                Iterator j = getLayoutTarget().intersectingNodes(bounds);
                while (j.hasNext()) {
                    Object n2 = (Object)j.next();
                    if (node != n2) { overlap = false; }
                }
                if (!overlap) {
                    break;
                }
            }
        }
    }

    /** Called in response to the figure representing the
     *  given node being moved.
     */
    public void nodeMoved(Object node) {
        // do nothing
    }

    /** Return the layout target.
     */
    public LayoutTarget getLayoutTarget() {
        return _global.getLayoutTarget();
    }

    /** Set the layout target.
     */
    public void setLayoutTarget(LayoutTarget target) {
        _global.setLayoutTarget(target);
    }

    /**
     * Layout the graph model in the viewport
     * specified by the layout target environment.
     */
    public void layout(Object composite) {
        _global.layout(composite);
    }
}



