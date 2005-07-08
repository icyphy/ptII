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
 * @version $Id$
 * @deprecated Will be removed in Diva 0.4. Use diva.compat.canvas if needed.
 */
public interface PaintedGraphic extends PaintedObject {
    /** Get the line width.
     */
    public float getLineWidth();

    /** Get the stroke.
     */
    public Stroke getStroke();

    /** Test if this shape is hit by the given rectangle.  Any transparent
     * parts of the graphic are generally intersected, but not hit.
     */
    public boolean hit(Rectangle2D r);

    /** Test if this shape intersects the given rectangle.
     */
    public boolean intersects(Rectangle2D r);

    /** Set the line width.
     */
    public void setLineWidth(float lineWidth);
}
