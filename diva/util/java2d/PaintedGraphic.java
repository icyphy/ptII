/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.java2d;

import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

/** The interface for a set of utility classes that paint shapes
 * or other kinds of graphical objects. The purpose of these classes
 * is to provide a simple interface for basic graphical drawing
 * operations. Generally, they combine a number of different
 * objects from the Java2D API in the most commonly useful way.
 *
 * @author  Nick Zamora
 * @version $Revision$
 * @deprecated Will be removed in Diva 0.4. Use diva.compat.canvas if needed.
 */
public interface PaintedGraphic extends PaintedObject {

    /** Get the line width.
     */
    public float getLineWidth ();

    /** Get the stroke.
     */
    public Stroke getStroke ();

    /** Test if this shape is hit by the given rectangle.  Any transparent
     * parts of the graphic are generally intersected, but not hit.
     */
    public boolean hit (Rectangle2D r);

    /** Test if this shape intersects the given rectangle.
     */
    public boolean intersects (Rectangle2D r);

    /** Set the line width.
     */
    public void setLineWidth (float lineWidth);
}






