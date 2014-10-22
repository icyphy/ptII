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
 *
 */
package diva.canvas.connector;

import java.awt.geom.Point2D;

import diva.canvas.Figure;
import diva.canvas.Site;

/** A connector target that returns sites on the perimeter of a figure.
 *
 * @version $Id$
 * @author John Reekie
 * @author Michael Shilman
 */
public class PerimeterTarget extends AbstractConnectorTarget {
    /** Return the nearest site on the figure if the figure
     * is not a connector
     */
    @Override
    public Site getHeadSite(Figure f, final double x, final double y) {
        // Removed the test if (!(f instanceof Connector)) {
        // It is now also possible to connect with other links.
        // If this existing link has a vertex as head or tail,
        // we will connect with the vertex, otherwise we will
        // remove the old link, create a new vertex, link the
        // head and tail of the existing link with the
        // vertex and link the new link with the vertex.

        // FIXME: Need to generate unique ID per figure
        // FIXME: Need to actually return a useful site!
        if (!(f instanceof Connector)) {
            return new PerimeterSite(f, 0);
        } else {
            // In case we are snapping a connector with a
            // connector we want to snap at the mouse location,
            // not the "start" of the figure.
            // It would be even better to snap to the actual figure, but this
            // does not seem to be that easy (without iterating through the
            // complete shape.
            return new PerimeterSite(f, 0) {
                @Override
                public Point2D getPoint(double normal) {
                    return new Point2D.Double(x, y);
                }
            };
        }
    }
}
