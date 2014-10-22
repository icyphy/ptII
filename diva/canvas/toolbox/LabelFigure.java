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
 */
package diva.canvas.toolbox;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;

import javax.swing.SwingConstants;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasUtilities;
import diva.canvas.TransformContext;
import diva.util.java2d.ShapeUtilities;

/**
 * A figure which draws a string. If the string contains newlines,
 * then it will be broken up into multiple lines.
 * Strings can be "anchored" in the center or on one of
 * the edges or corners, so that when the font or text changes,
 * the label appears to stay in the right location.
 *
 * @author Michael Shilman
 * @author John Reekie
 * @version $Id$
 */
public class LabelFigure extends AbstractFigure {
    /** The anchor on the label. This must be one of the
     * constants defined in SwingConstants.
     */
    private int _anchor = SwingConstants.CENTER;

    /** The bounds in the internal coordinate system. This is
     * kept separately from the shape, in case the shape is empty
     * (as it will be for a blank string). In that case, a small
     * bounding box is set -- the default is a zero bounding box,
     * which will produce the wrong coordinates.
     */
    private Rectangle2D _bounds = null;

    /** The cached bounds, in the external coordinate system
     */
    private Rectangle2D _cachedBounds = null;

    /** The default font.
     */
    private static Font _defaultFont = new Font("Serif", Font.PLAIN, 16);

    /** The fill paint of the string.
     */
    private Paint _fillPaint = Color.black;

    /** The font.
     */
    private Font _font;

    /** The "padding" around the text
     */
    private double _padding = 4.0;

    /** The list of shapes used to draw the string, in the local
     * coordinate system
     */
    private Shape _shape;

    /** The string that gets painted.
     */
    private String _string;

    /** The transform context
     */
    private TransformContext _transformContext;

    /** The order of anchors used by the autoanchor method.
     */
    private static int[] _anchors = { SwingConstants.SOUTH,
            SwingConstants.NORTH, SwingConstants.WEST, SwingConstants.EAST,
            SwingConstants.SOUTH_WEST, SwingConstants.SOUTH_EAST,
            SwingConstants.NORTH_WEST, SwingConstants.NORTH_EAST };

    /**
     * Construct an empty label figure.
     */
    public LabelFigure() {
        this("", _defaultFont);
    }

    /**
     * Construct a label figure displaying the
     * given string, using the default font.
     */
    public LabelFigure(String s) {
        this(s, _defaultFont);
    }

    /**
     * Construct a label figure displaying the
     * given string in the given face, style, and size. A new
     * Font object representing the face, style, and size is
     * created for this label.
     */
    public LabelFigure(String s, String face, int style, int size) {
        this(s, new Font(face, style, size));
    }

    /**
     * Construct a label figure displaying the
     * given string in the given font. This is the best constructor
     * to use if you are creating a lot of labels in a font other
     * than the default, as a single instance of Font can then
     * be shared by many labels.
     */
    public LabelFigure(String s, Font f) {
        _string = s;
        _font = f;
        _transformContext = new TransformContext(this);
    }

    /**
     * Construct a label figure displaying the
     * given string in the given font, with the given padding and anchor.
     */
    public LabelFigure(String s, Font font, double padding, int anchor) {
        this(s, font);
        _padding = padding;
        _anchor = anchor;
    }

    /**
     * Construct a label figure displaying the
     * given string in the given font, with the given padding and anchor,
     * and the given color.
     */
    public LabelFigure(String s, Font font, double padding, int anchor,
            Color color) {
        this(s, font, padding, anchor);
        _fillPaint = color;
    }

    /** Choose an anchor point so as not to intersect a given
     * figure. The anchor point is cycled through until one is reached
     * such that the bounding box of the label does not intersect
     * the given shape.  If there is none,
     * the anchor is not changed. The order of preference is the
     * current anchor, the four edges, and the four corners.
     */
    public void autoAnchor(Shape s) {
        Rectangle2D.Double r = new Rectangle2D.Double();
        r.setRect(getBounds());

        // Try every anchor and if there's no overlap, use it
        Point2D location = getAnchorPoint();

        for (int _anchor2 : _anchors) {
            Point2D pt = CanvasUtilities.getLocation(r, _anchor2);
            CanvasUtilities.translate(pt, _padding, _anchor2);
            r.x += location.getX() - pt.getX();
            r.y += location.getY() - pt.getY();

            if (!s.intersects(r)) {
                //// System.out.println("Setting anchor to " + _anchors[i]);
                setAnchor(_anchor2);
                break;
            }
        }
    }

    /**
     * Get the point at which this figure is "anchored." This
     * will be one of the positioning constants defined in
     * javax.swing.SwingConstants.
     */
    public int getAnchor() {
        return _anchor;
    }

    /**
     * Get the location at which the anchor is currently located.
     * This method looks at the anchor and padding attributes to
     * figure out the point.
     */
    public Point2D getAnchorPoint() {
        Rectangle2D bounds = getBounds();
        Point2D pt = CanvasUtilities.getLocation(bounds, _anchor);

        if (_anchor != SwingConstants.CENTER) {
            CanvasUtilities.translate(pt, _padding, _anchor);
        }

        return pt;
    }

    /**
     * Get the bounds of this string
     */
    @Override
    public Rectangle2D getBounds() {
        if (_cachedBounds == null) {
            if (_shape == null) {
                _update();
            }

            AffineTransform at = _transformContext.getTransform();
            _cachedBounds = ShapeUtilities.transformBounds(_bounds, at);
        }

        return _cachedBounds;
    }

    /**
     * Get the font that this label is drawn in. To get the
     * font name, style, and size, call this method and then
     * call the appropriate methods on the Font object.
     */
    public Font getFont() {
        return _font;
    }

    /**
     * Get the fill paint for this label.
     */
    public Paint getFillPaint() {
        return _fillPaint;
    }

    /** Return the origin, which is the anchor point.
     *  @return The anchor point.
     */
    @Override
    public Point2D getOrigin() {
        return getAnchorPoint();
    }

    /**
     * Get the padding around the text.
     */
    public double getPadding() {
        return _padding;
    }

    /**
     * Get the shape of this label figure. This just returns
     * the bounds, since hit-testing on the actual filled
     * latter shapes is way slow (and not that useful, since
     * usually you want to treat the whole label as a single
     * object anyway, and not have to click on an actual
     * filled pixel).
     */
    @Override
    public Shape getShape() {
        return getBounds();
    }

    /**
     * Get the string.
     */
    public String getString() {
        return _string;
    }

    /**
     * Paint the figure.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        if (_cachedBounds == null) {
            getBounds();
        }

        if (_string != null) {
            // Push the context
            _transformContext.push(g);

            g.setPaint(_fillPaint);
            g.fill(_shape);

            // Pop the context
            _transformContext.pop(g);
        }
    }

    /**
     * Set the point at which this figure is "anchored." This
     * must be one of the positioning constants defined in
     * javax.swing.SwingConstants. The default is
     * SwingConstants.CENTER. Whenever the font or string is changed,
     * the label will be moved so that the anchor remains at
     * the same position on the screen. When this method is called,
     * the figure is adjusted so that the new anchor is at the
     * same position as the old anchor was. The actual position of
     * the text relative to the anchor point is shifted by the
     * padding attribute.
     */
    public void setAnchor(int anchor) {
        // Optimize if the figure is not yet painted
        if (_bounds == null) {
            _anchor = anchor;
        } else {
            Point2D oldpt = getAnchorPoint();
            _anchor = anchor;

            Point2D newpt = getAnchorPoint();

            repaint();
            translate(oldpt.getX() - newpt.getX(), oldpt.getY() - newpt.getY());
            _cachedBounds = null;
            repaint();
        }
    }

    /**
     * Set the fill paint that this shape
     * is drawn with.
     */
    public void setFillPaint(Paint p) {
        _fillPaint = p;
        repaint();
    }

    /**
     * Set the font.
     */
    public void setFont(Font f) {
        if (_cachedBounds == null) {
            _font = f;
        } else {
            // Remember the current anchor point
            Point2D pt = getAnchorPoint();
            _font = f;
            _update();

            // Move it back
            translateTo(pt);
        }
    }

    /**
     * Set the "padding" around the text. This is used
     * only if anchors are used -- when the label is positioned
     * relative to an anchor, it is also shifted by the padding
     * distance so that there is some space between the anchor
     * point and the text. The default padding is two, and the
     * padding must not be set to zero if automatic anchoring
     * is used.
     */
    public void setPadding(double padding) {
        _padding = padding;
        setAnchor(_anchor);
        _cachedBounds = null;
    }

    /**
     * Set the string.
     */
    public void setString(String s) {
        if (_cachedBounds == null) {
            _string = s;
        } else {
            if (!s.equals(_string)) {
                // repaint the string where it currently is
                repaint();

                // Remember the current anchor point
                Point2D pt = getAnchorPoint();

                // Modify the string
                _string = s;
                _update();

                // Recalculate and translate
                Point2D badpt = getAnchorPoint();
                translate(pt.getX() - badpt.getX(), pt.getY() - badpt.getY());

                // Repaint in new location
                repaint();
            }
        }
    }

    /**
     * Change the transform of this label. Note that the anchor
     * of the figure will appear to nmove -- use translateTo()
     * to move it back again if this method being called to
     * (for example) rotate the label.
     */

    //public void setTransform (AffineTransform at) {
    //      repaint();
    //      _transform = at;
    //      _bounds = null;
    //      repaint();
    //  }
    /**
     * Transform the label with the given transform.  Note that the anchor
     * of the figure will appear to move -- use translateTo()
     * to move it back again if this method being called to
     * (for example) rotate the label.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();
        _cachedBounds = null;
        _transformContext.preConcatenate(at);
        repaint();
    }

    /**
     * Translate the label so that the current anchor is located
     * at the given point. Use this if you apply a transform to
     * a label in order to rotate or scale it, but don't want
     * the label to actually go anywhere.
     */
    public void translateTo(double x, double y) {
        // FIXME: this might not work in the presence of
        // scaling. If not, modify to preconcatenate instead
        repaint();

        Point2D pt = getAnchorPoint();
        translate(x - pt.getX(), y - pt.getY());
        repaint();
    }

    /**
     * Translate the label so that the current anchor is located
     * at the given point. Use this if you apply a transform to
     * a label in order to rotate or scale it, but don't want
     * the label to actually go anywhere.
     */
    public void translateTo(Point2D pt) {
        translateTo(pt.getX(), pt.getY());
    }

    /** Update the shape used to draw the figure.
     */
    private void _update() {
        // Generate font render context with a unit transform.
        // Since we are generating a shape and drawing that, it makes
        // no difference what the values of the flags are
        FontRenderContext frc = new FontRenderContext(new AffineTransform(),
                false, false);

        // Only a single line
        if (_string.indexOf('\n') < 0) {
            // Get the shape
            GlyphVector gv = _font.createGlyphVector(frc, _string);
            _shape = gv.getOutline();

            // If the string is only whitespace, then the drawing stuff
            // won't work properly. So we set the bounding box to a special
            // value.
            if (_string.trim().equals("")) {
                _bounds = new Rectangle2D.Float(0.0f, 0.0f, 1.0f, 1.0f);
            } else {
                _bounds = _shape.getBounds2D();
            }
        } else {
            // Multiple lines, so generate a compound shape
            double dy = _font.getMaxCharBounds(frc).getHeight();
            StringTokenizer lines = new StringTokenizer(_string, "\n", true);
            _shape = null;

            int count = 0;

            while (lines.hasMoreTokens()) {
                String line = lines.nextToken();

                if (line.equals("\n")) {
                    // Note that leading or trailing newlines are ignored.
                    count++;
                } else if (!line.trim().equals("")) {
                    GlyphVector gv = _font.createGlyphVector(frc, line);
                    Shape s = gv.getOutline();

                    if (_shape == null) {
                        _shape = s;
                    } else {
                        // Translate each line and append to the previous shape
                        s = ShapeUtilities.translateModify(s, 0, count * dy);
                        ((GeneralPath) _shape).append(s, false);
                    }
                }
            }

            if (_shape == null) {
                // There was nothing in the text, so create a glyph
                // for a single blank space.
                GlyphVector gv = _font.createGlyphVector(frc, " ");
                _shape = gv.getOutline();
            }
            _bounds = _shape.getBounds2D();
        }

        _cachedBounds = null;
    }
}
