/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.util.java2d;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

/** The interface for a SMALL set of utility classes that paint shapes
 * or other kinds of graphical objects. The purpose of these classes
 * is to provide a simple interface for some of the more complicated
 * things in Java2D, like strings and Images.
 *
 * @version        $Revision$
 * @author         John Reekie
 * @deprecated Will be removed in Diva 0.4. Use diva.compat.canvas if needed.
 */
public interface PaintedObject {

    /** Get the bounding box of the object when painted. Implementations
     * of this method should take account of the thickness of the
     * stroke, if there is one.
     */
    public Rectangle2D getBounds ();

    /** Paint the shape. Implementations are expected to redraw
     * the entire object. Whether or not the paint overwrites
     * fields in the graphics context such as the current
     * paint, stroke, and composite, depends on the implementing class.
     */
    public void paint (Graphics2D g);
}


