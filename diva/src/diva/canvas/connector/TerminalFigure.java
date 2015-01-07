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

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasComponent;
import diva.canvas.CanvasLayer;
import diva.canvas.DamageRegion;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.interactor.Interactor;

/** A TerminalFigure decorates a figure to be a terminal as well.  Using this
 * class, any Figure can be made a terminal.  As per the decorator pattern,
 * the Figure should be not be accessed externally to this class.
 *
 * @version $Id$
 * @author Steve Neuendorffer
 */
public class TerminalFigure extends AbstractFigure implements Terminal {
    Site _attachSite;

    protected Site _connectSite;

    Figure _figure;

    /**
     * Create a new TerminalFigure for the given figure, with the given
     * site for connections.
     */
    public TerminalFigure(Figure figure, Site connectSite) {
        _figure = figure;
        _connectSite = connectSite;
    }

    /**
     * Create a new TerminalFigure for the given figure. This
     * is intended for use by derived classes that will provide
     * their own connect site or sites.
     */
    protected TerminalFigure(Figure figure) {
        _figure = figure;
    }

    /** Test whether this figure contains the point given. The point
     * given is in the enclosing transform context.
     */
    @Override
    public boolean contains(Point2D p) {
        return _figure.contains(p);
    }

    /** Get the bounding box of this figure. The result rectangle is
     *  given in the enclosing transform context.
     */
    @Override
    public Rectangle2D getBounds() {
        return _figure.getBounds();
    }

    /** Get the site that the terminal is attached to.
     */
    @Override
    public Site getAttachSite() {
        return _attachSite;
    }

    /** Get the site that a connector can connect to.
     */
    @Override
    public Site getConnectSite() {
        return _connectSite;
    }

    /** Get the figure that this terminal figure is wrapping.  Note
     *  usage of this figure must be considered extremely carefully to
     *  avoid breaking the decorator pattern.
     */
    public Figure getFigure() {
        return _figure;
    }

    /** Return the interactor of this figure. Return
     *  null if there isn't one.
     */
    @Override
    public Interactor getInteractor() {
        return _figure.getInteractor();
    }

    /** Get the most immediate layer containing this figure.
     */
    @Override
    public CanvasLayer getLayer() {
        return _figure.getLayer();
    }

    /** Return the origin of the wrapped figure in the enclosing
     *  transform context.
     *  @return The origin of the background figure.
     */
    @Override
    public Point2D getOrigin() {
        return _figure.getOrigin();
    }

    /** Return the parent of this component. Return null if the component
     *  does not have a parent.
     */
    @Override
    public CanvasComponent getParent() {
        return _figure.getParent();
    }

    /** Return the transform context of the component. If the component
     * has its own transform context, this method should return it,
     * otherwise it should return the transform context of its parent.
     */
    @Override
    public TransformContext getTransformContext() {
        return _figure.getTransformContext();
    }

    /** Get the outline shape of this figure. The outline shape is
     *  used for things like highlighting. The result shape is given
     *  in the enclosing transform context.
     */
    @Override
    public Shape getShape() {
        return _figure.getShape();
    }

    /** Return the tooltip for this figure.
     */
    @Override
    public String getToolTipText() {
        return _figure.getToolTipText();
    }

    /**
     * Return the user object.
     */
    @Override
    public Object getUserObject() {
        return _figure.getUserObject();
    }

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
    @Override
    public boolean hit(Rectangle2D r) {
        return _figure.hit(r);
    }

    /** Test if this figure intersects the given rectangle. The
     *  rectangle is given in the enclosing transform context.
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        return _figure.intersects(r);
    }

    /** Test the visibility flag of this object. Note that this flag
     *  does not indicate whether the object is actually visible on
     *  the screen, as one of its ancestors may not be visible.
     */
    @Override
    public boolean isVisible() {
        return _figure.isVisible();
    }

    /** Paint the figure.
     */
    @Override
    public void paint(Graphics2D g) {
        _figure.paint(g);
    }

    /** Paint this object onto a 2D graphics object, within the given
     * region.  Implementors should first test if the visibility flag is
     * set, and paint the object if it is. The provided region can be
     * used to optimize the paint, but implementors can assume that the
     * clip region is correctly set beforehand.
     */
    @Override
    public void paint(Graphics2D g, Rectangle2D region) {
        _figure.paint(g, region);
    }

    /** Tell the terminal to relocate itself because the
     * attachment site (or the figure that owns it) has moved.
     */
    @Override
    public void relocate() {
        // FIXME implement
    }

    /** Schedule a repaint of the component. This should be called after
     *  performing modifications on the component.
     */
    @Override
    public void repaint() {
        _figure.repaint();
    }

    /** Accept notification that a repaint has occurred somewhere
     * in the tree below this component. The component must
     * clear any cached data that depends on its children and
     * forward the notification upwards.
     */
    @Override
    public void repaint(DamageRegion d) {
        _figure.repaint(d);
    }

    /** Set the site that the terminal is attached to.
     */
    @Override
    public void setAttachSite(Site s) {
        _attachSite = s;
    }

    /** Set the interactor of this figure. Once a figure has an
     *  interactor given to it, it will respond to events
     *  on the canvas, in the ways determined by the interactor.
     */
    @Override
    public void setInteractor(Interactor interactor) {
        _figure.setInteractor(interactor);
    }

    /** Set the parent of this figure.  A null argument means that the
     * figure is being removed from its parent. No checks are performed
     * to see if the figure already has a parent -- it is the
     * responsibility of the caller to do this. This method is not intended
     * for public use, and should never be called by client code.
     */
    @Override
    public void setParent(CanvasComponent fc) {
        _figure.setParent(fc);
    }

    /** Set the tool tip for this figure.
     */
    @Override
    public void setToolTipText(String tip) {
        _figure.setToolTipText(tip);
    }

    /** Transform the figure with the supplied transform. This can
     *  be used to perform arbitrary translation, scaling, shearing, and
     *  rotation operations.
     */
    @Override
    public void transform(AffineTransform at) {
        _figure.transform(at);
    }

    /** Move the figure the indicated distance.
     */
    @Override
    public void translate(double x, double y) {
        _figure.translate(x, y);
    }

    /**
     * Set the user object.
     */
    @Override
    public void setUserObject(Object o) {
        _figure.setUserObject(o);
    }

    /** Set the visibility flag of this object. If the flag is false,
     * then the object will not be painted on the screen.
     */
    @Override
    public void setVisible(boolean flag) {
        _figure.setVisible(flag);
    }
}
