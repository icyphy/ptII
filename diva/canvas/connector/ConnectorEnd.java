/*
 Copyright (c) 1998-2005 The Regents of the University of California
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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** An interface for objects that can be attached to the end
 * of a connector. Implementations of this interface are used
 * to draw arrow-heads, circles, diamonds, and various other
 * kinds of decoration at the end of connectors.
 *
 * @version $Id$
 * @author  John Reekie
 */
public interface ConnectorEnd {
    /** Get the bounding box of the shape used to draw
     * this connector end.
     */
    public Rectangle2D getBounds();

    /** Get the connection point of the end. The given point is
     * modified with the location to which the connector should
     * be drawn.
     */
    public void getConnection(Point2D p);

    /** Get the origin of the line end. The given point is
     * modified.
     */
    public void getOrigin(Point2D p);

    /** Paint the connector end. This method assumes that
     * the graphics context is already set up with the correct
     * paint and stroke.
     */
    public void paint(Graphics2D g);

    /** Set the normal of the connector end. The argument is the
     * angle in radians away from the origin.
     */
    public void setNormal(double angle);

    /** Set the origin of the decoration.
     */
    public void setOrigin(double x, double y);

    /** Translate the connector end by the given amount.
     */
    public void translate(double x, double y);
}
