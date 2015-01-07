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

import diva.graph.GraphModel;

/**
 * A static random layout engine.  This class tries to be smart by
 * not placing nodes on top of one another if possible, but doesn't
 * guarantee anything about the layout except that it will fall
 * into the required viewport.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class RandomLayout extends AbstractGlobalLayout {
    /**
     * The number of iterations that it will try
     * to place a node not on top of other nodes
     * before it gives up.
     */
    private static final int NUM_ITER = 10;

    /**
     * Simple constructor.
     */
    public RandomLayout(LayoutTarget target) {
        super(target);
    }

    /**
     * Layout the graph model and viewport specified by the given
     * target environment.  Tries to be smart by not placing nodes on
     * top of one another if possible, but doesn't guarantee anything
     * about the layout except that it will fall into the required
     * viewport.
     */
    @Override
    public void layout(Object composite) {
        LayoutTarget target = getLayoutTarget();
        GraphModel model = target.getGraphModel();

        for (Iterator ns = model.nodes(composite); ns.hasNext();) {
            Object node = ns.next();

            if (target.isNodeVisible(node)) {
                Rectangle2D vp = target.getViewport(composite);
                Rectangle2D bounds = target.getBounds(node);

                for (int i = 0; i < NUM_ITER; i++) {
                    double x = vp.getX() + Math.abs(Math.random())
                            * vp.getWidth();
                    double y = vp.getY() + Math.abs(Math.random())
                            * vp.getHeight();
                    LayoutUtilities.place(target, node, x, y);
                    bounds = target.getBounds(node);

                    boolean overlap = false;
                    Iterator j = target.intersectingNodes(bounds);

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

        LayoutUtilities.routeVisibleEdges(composite, target);
    }
}
