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
package ptolemy.vergil.kernel;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import diva.canvas.DamageRegion;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.toolbox.BasicHighlighter;

/** A decorator figure that displays a shadow behind the
 *  figure. The highlighter has several options to control
 *  the color, transparency, and size. Currently, this
 *
 * @version        $Id$
 * @author         Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
 */
public class ShadowHighlighter extends BasicHighlighter {

    /** Create a new shadow with a default paint and size.
     */
    public ShadowHighlighter() {
        super(Color.gray, 6.0f);
    }

    /** Create a new shadow with the given paint and halo.
     *  @param paint The paint.
     *  @param halo The halo, which is the size of the shadow.
     */
    public ShadowHighlighter(Paint paint, float halo) {
        super(paint, halo);
    }

    /** Create a new shadow with the given paint, size.
     *  and compositing operation.
     *  @param paint The paint.
     *  @param halo The halo, which is the size of the shadow.
     *  @param composite The compositing operation.
     */
    public ShadowHighlighter(Paint paint, float halo, Composite composite) {
        super(paint, halo, composite);
    }

    /** Create a new shadow with the given paint, size,
     *  compositing operation, and stroke.  This shadow
     *  draws an outline only and does not fill it.
     *  @param paint The paint.
     *  @param halo The halo, which is the size of the shadow.
     *  @param composite The compositing operation.
     *  @param stroke The stroke
     */
    public ShadowHighlighter(Paint paint, float halo, Composite composite,
            Stroke stroke) {
        super(paint, halo, composite, stroke);
    }

    /** Get the bounds. This is the child's bounding box stretched
     *  by the "halo."
     *  @return The bounds.
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D b = getChild().getBounds();
        Rectangle2D bounds = new Rectangle2D.Double(b.getX(), b.getY()
                + getHalo(), b.getWidth() + getHalo(), b.getHeight()
                + getHalo());
        return bounds;
    }

    /** Create a new instance of this shadower. The new
     *  instance will have the same paint, size, and composite
     *  as this one.
     *  @param figure The figure, ignored in this method.
     *  @return A new instance of the ShadowHighlighter class.
     */
    @Override
    public FigureDecorator newInstance(Figure figure) {
        return new ShadowHighlighter(getPaint(), getHalo(), getComposite(),
                getStroke());
    }

    /** Paint the figure. This method first paints the shadow over
     *  the contained figure's bounding box stretched by the size. It
     *  then paints the contained figure.
     *  @param g The Graphics2D context.
     */
    @Override
    public void paint(Graphics2D g) {
        Composite composite = getComposite();
        if (composite != null) {
            g.setComposite(composite);
        }

        g.setPaint(getPaint());

        // Draw the shadow
        // Rectangle2D bounds = getChild().getBounds();
        // FIXME: Can we draw non-rectangular shadows???
        Rectangle2D bounds = getChild().getShape().getBounds2D();
        double x = bounds.getX() + getHalo();
        double y = bounds.getY() + getHalo();
        double w = bounds.getWidth();
        double h = bounds.getHeight();

        if (getStroke() == null) {
            g.fill(new Rectangle2D.Double(x, y, w, h));
        } else {
            g.setStroke(getStroke());
            g.draw(new Rectangle2D.Double(x, y, w, h));
        }

        // Draw the child
        getChild().paint(g);
    }

    /** Request a repaint of the figure and shadow.
     * This method reads the bounding box of the shadowed
     * figure, and requests a repaint of that box stretched
     * in each direction by the size.
     */
    @Override
    public void repaint() {
        Rectangle2D bounds = getChild().getBounds();
        double x = bounds.getX();
        double y = bounds.getY();
        double w = bounds.getWidth() + getHalo();
        double h = bounds.getHeight() + getHalo();

        getParent().repaint(
                DamageRegion.createDamageRegion(getTransformContext(), x, y, w,
                        h));
    }
}
