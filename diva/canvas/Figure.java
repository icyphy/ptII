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

import diva.canvas.interactor.Interactor;
import diva.util.UserObjectContainer;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


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
     * given is in the enclosing transform context.
     */
    public boolean contains(Point2D p);

    /** Get the bounding box of this figure. The result rectangle is
     *  given in the enclosing transform context.  The returned rectangle
     *  may be an internally cached shape, and should not be modified.
     */
    public Rectangle2D getBounds();

    /** Return the interactor of this figure. Return
     *  null if there isn't one.
     */
    public Interactor getInteractor();

    /** Get the most immediate layer containing this figure.
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
     */
    public CanvasComponent getParent();

    /** Get the outline shape of this figure. The outline shape is
     *  used for things like highlighting. The result shape is given
     *  in the enclosing transform context.
     */
    public Shape getShape();

    /** Return the tooltip string for this figure, or null if the figure
     *  does not have a tooltip.
     */
    public String getToolTipText();

    /** Test if this figure is "hit" by the given rectangle. This is the
     *  same as intersects if the interior of the figure is not
     *  transparent. The rectangle is given in the enclosing transform context.
     *  If the figure is not visible, it must return false.
     *  The default implementation is the same as <b>intersects</b>
     *  if the figure is visible.
     *
     * <p>(This method would be better named <b>hits</b>, but
     * the name <b>hit</b> is consistent with java.awt.Graphics2D.)
     */
    public boolean hit(Rectangle2D r);

    /** Test if this figure intersects the given rectangle. The
     *  rectangle is given in the enclosing transform context.
     */
    public boolean intersects(Rectangle2D r);

    /** Set the interactor of this figure. Once a figure has an
     *  interactor given to it, it will respond to events
     *  on the canvas, in the ways determined by the interactor.
     */
    public void setInteractor(Interactor interactor);

    /** Set the parent of this figure.  A null argument means that the
     * figure is being removed from its parent. No checks are performed
     * to see if the figure already has a parent -- it is the
     * responsibility of the caller to do this. This method is not intended
     * for public use, and should never be called by client code.
     */
    public void setParent(CanvasComponent fc);

    /** Set the tooltip string for this figure.  If the string is null, then
     *  the figure will not have a tooltip.
     */
    public void setToolTipText(String s);

    /** Transform the figure with the supplied transform. This can
     *  be used to perform arbitrary translation, scaling, shearing, and
     *  rotation operations.
     */
    public void transform(AffineTransform at);

    /** Move the figure the indicated distance.
     */
    public void translate(double x, double y);
}
