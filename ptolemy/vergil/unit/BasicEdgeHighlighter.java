/*
 Copyright (c) 2003-2014 The Regents of the University of California
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

 PT_COPYRIGHT_VERSION_3
 COPYRIGHTENDKEY
 *
 */
package ptolemy.vergil.unit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import diva.canvas.DamageRegion;
import diva.canvas.Figure;
import diva.canvas.FigureDecorator;

///////////////////////////////////////////////////////////////////
//// BasicEdgeHighlighter

/**
 A decorator figure that displays a highlight behind an edge. This capability
 was planned for BasicHighlighter but there doesn't seem to be an easy way to
 determine that an edge is being painted. This class is a stripped down
 version of BasicHighlighter with the paint method modified so that it
 assumes that an edge is being highlighted.
 @version        $Id$
 @author         Rowland R Johnson
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
public class BasicEdgeHighlighter extends FigureDecorator {
    /** Create a new highlighter with a default paint, "halo", and stroke.
     */
    public BasicEdgeHighlighter() {
        this._paint = new Color(255, 255, 0, 200);
        this._halo = 4.0f;
        _stroke = new BasicStroke(2 * _halo);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return false. This method always returns false, as it
     * is meaningless (and dangerous!) to be able to hit a highlight.
     */
    @Override
    public boolean hit(Rectangle2D region) {
        return false;
    }

    /** Create a new instance of this highlighter.
     */
    @Override
    public FigureDecorator newInstance(Figure f) {
        return new BasicEdgeHighlighter();
    }

    /** Paint the edge. This method first paints the highlight over
     * the contained edge by drawing a line width determined by halo. It
     * then paints the contained edge.
     */
    @Override
    public void paint(Graphics2D g) {
        g.setPaint(_paint);

        Shape shape = getChild().getShape();

        if (_stroke != null) {
            g.setStroke(_stroke);
        }

        g.draw(shape);

        // Draw the child
        getChild().paint(g);
    }

    /** Receive repaint notification. This method generates another
     * repaint() call, with a larger region, in order to ensure
     * that the highlight is repainted.
     */
    @Override
    public void repaint(DamageRegion d) {
        repaint();
    }

    /** Request a repaint of the figure and highlight.
     * This method reads the bounding box of the highlighted
     * figure, and requests a repaint of that box stretched
     * in each direction by the halo.
     */
    @Override
    public void repaint() {
        Rectangle2D bounds = getChild().getBounds();
        double x = bounds.getX() - _halo;
        double y = bounds.getY() - _halo;
        double w = bounds.getWidth() + 2 * _halo;
        double h = bounds.getHeight() + 2 * _halo;

        getParent().repaint(
                DamageRegion.createDamageRegion(getTransformContext(), x, y, w,
                        h));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The "halo" size
     */
    private float _halo;

    /* The highlight paint, or null if none.
     */
    private Paint _paint;

    /* The highlight stroke, or null if none.
     */
    private Stroke _stroke;
}
