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
package diva.canvas;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.interactor.Interactor;
import diva.util.UserObjectContainer;

/** A Figure is a persistent object drawn on the screen.
 * This interface roots a small tree of interfaces that define
 * various roles that different kinds of figures play. It is also
 * implemented by the AbstractFigure class, which roots the tree
 * of concrete figure classes.
 *
 * @version $Id$
 * @author John Reekie
 * @Pt.AcceptedRating Yellow
 */
public interface Figure extends VisibleComponent, UserObjectContainer {
    /** Test whether this figure contains the point given. The point
     *  given is in the enclosing transform context.
     *  @param point The given point
     *  @return true if the point is contained by this figure.
     */
    public boolean contains(Point2D point);

    /** Get the bounding box of this figure. The result rectangle is
     *  given in the enclosing transform context.  The returned rectangle
     *  may be an internally cached shape, and should not be modified.
     *  @return the bounding box of this figure.
     */
    public Rectangle2D getBounds();

    /** Return the interactor of this figure. Return
     *  null if there isn't one.
     *  @return the interactor of this figure or null.
     *  @see #setInteractor(Interactor)
     */
    public Interactor getInteractor();

    /** Get the most immediate layer containing this figure.
     *  @return the most immediate layer containing this figure.
     */
    public CanvasLayer getLayer();

    /** Return the origin of the figure in the enclosing transform
     *  context.  This might be, for example, the center of the figure,
     *  the upper left corner, or some other point with respect to which
     *  the pieces of the figure are defined.
     *  @return The origin of the figure.
     */
    public Point2D getOrigin();

    /** Return the parent of this figure. Return null if the figure
     *  does not have a parent.  (Note that a figure with no parent
     *  can exist, but it will not be displayed, as it must be in a
     *  layer for the figure canvas to ever call its paint method.)
     *  @return the parent of this figure.
     *  @see #setParent(CanvasComponent)
     */
    @Override
    public CanvasComponent getParent();

    /** Get the outline shape of this figure. The outline shape is
     *  used for things like highlighting. The result shape is given
     *  in the enclosing transform context.
     *  @return the outline shape of this figure
     */
    public Shape getShape();

    /** Return the tooltip string for this figure, or null if the figure
     *  does not have a tooltip.
     *  @return the tooltip string for this figure.
     *  @see #setToolTipText(String)
     */
    public String getToolTipText();

    /** Test if this figure is "hit" by the given rectangle. This is the
     *  same as intersects if the interior of the figure is not
     *  transparent. The rectangle is given in the enclosing transform context.
     *  If the figure is not visible, it must return false.
     *  The default implementation is the same as <b>intersects</b>
     *  if the figure is visible.
     *
     *  <p>(This method would be better named <b>hits</b>, but
     *  the name <b>hit</b> is consistent with java.awt.Graphics2D.)
     *  @param rectangle The rectangle to be checked.
     *  @return true if the figure is hit by the given rectangle.
     */
    public boolean hit(Rectangle2D rectangle);

    /** Test if this figure intersects the given rectangle. The
     *  rectangle is given in the enclosing transform context.
     *  @param rectangle The rectangle to be checked.
     *  @return truen if the figure intersects the given rectangle.
     */
    public boolean intersects(Rectangle2D rectangle);

    /** Set the interactor of this figure. Once a figure has an
     *  interactor given to it, it will respond to events
     *  on the canvas, in the ways determined by the interactor.
     *  @param interactor The interactor.
     *  @see #getInteractor()
     */
    public void setInteractor(Interactor interactor);

    /** Set the parent of this figure.  A null argument means that the
     *  figure is being removed from its parent. No checks are performed
     *  to see if the figure already has a parent -- it is the
     *  responsibility of the caller to do this. This method is not intended
     *  for public use, and should never be called by client code.
     *  @param canvasComponent The parent of the figure.
     *  @see #getParent()
     */
    public void setParent(CanvasComponent canvasComponent);

    /** Set the tooltip string for this figure.  If the string is null, then
     *  the figure will not have a tooltip.
     *  @param toolTipText The tool tip text.
     *  @see #getToolTipText()
     */
    public void setToolTipText(String toolTipText);

    /** Transform the figure with the supplied transform. This can
     *  be used to perform arbitrary translation, scaling, shearing, and
     *  rotation operations.
     *  @param affineTransform The transform to be used.
     */
    public void transform(AffineTransform affineTransform);

    /** Move the figure the indicated distance.
     *  @param x The x value to be moved.
     *  @param y The y value to be moved.
     */
    public void translate(double x, double y);
}
