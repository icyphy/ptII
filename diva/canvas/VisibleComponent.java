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
package diva.canvas;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/** A visible component is a canvas component that is painted onto a
 * graphics context. This interface contains a small set of methods
 * that are required by any such object.  It is also used as a tagging
 * interface so that paint routines can tell whether certain objects
 * want to be painted or not.
 *
 * @version        $Id$
 * @author         John Reekie
 * @Pt.AcceptedRating Yellow
 */
public interface VisibleComponent extends CanvasComponent {
    /** Test the visibility flag of this object. Note that this flag
     *  does not indicate whether the object is actually visible on
     *  the screen, as one of its ancestors may not be visible.
     */
    public boolean isVisible();

    /** Paint this object onto a 2D graphics object. Implementors
     * should first test if the visibility flag is set, and
     * paint the object if it is.
     */
    public void paint(Graphics2D g2d);

    /** Paint this object onto a 2D graphics object, within the given
     * region.  Implementors should first test if the visibility flag is
     * set, and paint the object if it is. The provided region can be
     * used to optimize the paint, but implementors can assume that the
     * clip region is correctly set beforehand.
     */
    public void paint(Graphics2D g, Rectangle2D region);

    /** Set the visibility flag of this object. If the flag is false,
     * then the object will not be painted on the screen.
     */
    public void setVisible(boolean flag);
}
