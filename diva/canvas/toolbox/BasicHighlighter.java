/*
 Copyright (c) 1998-2001 The Regents of the University of California
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

package diva.canvas.toolbox;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import diva.canvas.DamageRegion;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;

/** A decorator figure that displays a highlight behind the
 * figure. The highlighter has several options to control
 * the color, transparency, and "halo." Currently, this
 * just displays a rectangle on the figure's bounding box,
 * but this may be changed to take notice of the figure's
 * shape in the future.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class BasicHighlighter extends FigureDecorator {

    /* The compositing operation.
     */
    private Composite _composite = null;

    /* The "halo" size
     */
    private float _halo;

    /* The highlight paint, or null if none.
     */
    private Paint _paint;

    /* The highlight stroke, or null if none.
     */
    private Stroke _stroke;

    /** Create a new highlighter with a default paint and "halo"
     */
    public BasicHighlighter () {
        this._paint = Color.yellow;
        this._halo = 4.0f;
    }

    /** Create a new highlighter with the given paint and "halo"
     */
    public BasicHighlighter (Paint paint, float halo) {
        this._paint = paint;
        this._halo = halo;
    }

    /** Create a new highlighter with the given paint, "halo,"
     * and compositing operation.
     */
    public BasicHighlighter (Paint paint, float halo, Composite composite) {
        this._paint = paint;
        this._halo = halo;
        this._composite = composite;
    }

    /** Create a new highlighter with the given paint, "halo,"
     *  compositing operation, and stroke.  This highlighter
     *  draws an outline only and does not fill it.
     */
    public BasicHighlighter (Paint paint, float halo, Composite composite, Stroke stroke) {
        this._paint = paint;
        this._halo = halo;
        this._composite = composite;
        this._stroke = stroke;
    }

    /** Get the composite.
     */
    public Composite getComposite () {
        return _composite;
    }

    /** Get the bounds. This is the child's bounding box stretched
     * by the "halo."
     */
    public Rectangle2D getBounds () {
        Rectangle2D b = getChild().getBounds();
        Rectangle2D bounds = new Rectangle2D.Double(
                b.getX() - _halo,
                b.getY() - _halo,
                b.getWidth() + 2 * _halo,
                b.getHeight() + 2 * _halo);

        return bounds;
    }

    /** Get the halo.
     */
    public float getHalo () {
        return _halo;
    }

    /** Get the paint.
     */
    public Paint getPaint () {
        return _paint;
    }

    /** Get the stroke.
     */
    public Stroke getStroke() {
        return _stroke;
    }

    /** Return false. This method always returns false, as it
     * is meaningless (and dangerous!) to be able to hit a highlight.
     */
    public boolean hit (Rectangle2D region) {
        return false;
    }

    /** Create a new instance of this highlighter. The new
     * instance will have the same paint, halo, and composite
     * as this one.
     */
    public FigureDecorator newInstance (Figure f) {
        return new BasicHighlighter(_paint, _halo, _composite, _stroke);
    }

    /** Paint the figure. This method first paints the highlight over
     * the contained figure's bounding box stretched by the halo. It
     * then paints the contained figure.
     */
    public void paint (Graphics2D g) {
        if (_composite != null) {
            g.setComposite(_composite);
        }
        g.setPaint(_paint);

        // Draw the highlight
        // Rectangle2D bounds = getChild().getBounds();
        // FIXME: Can we draw non-rectangular highlights???
        Rectangle2D bounds = getChild().getShape().getBounds2D();
        double x = bounds.getX() - _halo;
        double y = bounds.getY() - _halo;
        double w = bounds.getWidth() + 2 * _halo;
        double h = bounds.getHeight() + 2 * _halo;

        if (_stroke == null) {
            g.fill(new Rectangle2D.Double(x,y,w,h));
        } else {
            g.setStroke(_stroke);
            g.draw(new Rectangle2D.Double(x,y,w,h));
        }

        // Draw the child
        getChild().paint(g);
    }

    /** Receive repaint notification. This method generates another
     * repaint() call, with a larger region, in order to ensure
     * that the highlight is repainted.
     */
    public void repaint (DamageRegion d) {
        repaint();
    }

    /** Request a repaint of the figure and highlight.
     * This method reads the bounding box of the highlighted
     * figure, and requests a repaint of that box stretched
     * in each direction by the halo.
     */
    public void repaint () {
        Rectangle2D bounds = getChild().getBounds();
        double x = bounds.getX() - _halo;
        double y = bounds.getY() - _halo;
        double w = bounds.getWidth() + 2 * _halo;
        double h = bounds.getHeight() + 2 * _halo;

        getParent().repaint(DamageRegion.createDamageRegion(
                getTransformContext(),
                x,y,w,h));
    }
}


