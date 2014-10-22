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
package diva.graph.layout;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * Randomly place newly-added nodes within the target viewport.  This
 * class tries to be smart by not placing nodes on top of one another
 * if possible, but doesn't guarantee anything about the layout except
 * that it will fall into the required viewport.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
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
    //private void debug(String s) {
    //    System.out.println("RandomIncrLayout: " + s);
    //}
    /** Called in response to the given edge being drawn.
     */
    @Override
    public void edgeDrawn(Object edge) {
        // do nothing
    }

    /** Called in response to the connector representing the given edge being
     *  rereouted.
     */
    @Override
    public void edgeRouted(Object edge) {
        // do nothing
    }

    /**
     * Lay out the given node randomly, trying not
     * to overlap it with existing nodes.
     */
    @Override
    public void nodeDrawn(Object node) {
        if (getLayoutTarget().isNodeVisible(node)) {
            Rectangle2D vp = getLayoutTarget().getViewport(
                    getLayoutTarget().getGraphModel().getParent(node));
            Rectangle2D bounds = getLayoutTarget().getBounds(node);

            for (int i = 0; i < NUM_ITER; i++) {
                double x = vp.getX() + Math.abs(Math.random()) * vp.getWidth();
                double y = vp.getY() + Math.abs(Math.random()) * vp.getHeight();

                LayoutUtilities.place(getLayoutTarget(), node, x, y);
                bounds = getLayoutTarget().getBounds(node);

                boolean overlap = false;
                Iterator j = getLayoutTarget().intersectingNodes(bounds);

                while (j.hasNext()) {
                    Object n2 = j.next();

                    if (node != n2) {
                        overlap = false;
                    }
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
    @Override
    public void nodeMoved(Object node) {
        // do nothing
    }

    /** Return the layout target.
     */
    @Override
    public LayoutTarget getLayoutTarget() {
        return _global.getLayoutTarget();
    }

    /** Set the layout target.
     */
    @Override
    public void setLayoutTarget(LayoutTarget target) {
        _global.setLayoutTarget(target);
    }

    /**
     * Layout the graph model in the viewport
     * specified by the layout target environment.
     */
    @Override
    public void layout(Object composite) {
        _global.layout(composite);
    }
}
