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
package diva.canvas.toolbox;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import diva.canvas.AbstractFigure;
import diva.canvas.Figure;
import diva.canvas.connector.BoundsSite;
import diva.canvas.connector.Terminal;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.ShapeUtilities;

/** An IconFigure is a figure that contains a main background figure,
 * a PaintedObject to decorate that figure, a label, and an arbitrary
 * number of attached Terminal objects.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class IconFigure extends AbstractFigure {
    /** The composite of this icon and all its stuff
     */
    private Composite _composite;

    /** The background figure
     */
    private Figure _background;

    /** The transform that is applied to the background
     * and the graphic
     */
    private AffineTransform _transform = new AffineTransform();

    /** The "graphic" object
     */
    private PaintedObject _graphic;

    /** The terminals of this icon
     */
    private ArrayList _terminals = new ArrayList();

    /** The sites to which the terminals attach
     */
    private ArrayList _sites = new ArrayList();

    /** The icon's label
     */
    private LabelFigure _label;

    /** Create a new icon figure using the given figure as the "background"
     * figure.
     */
    public IconFigure(Figure f) {
        _background = f;
        _background.setParent(this);
    }

    /** Create a new icon figure using the given figure as the
     * "background" figure and the given Painted object as its "graphic."
     */
    public IconFigure(Figure f, PaintedObject g) {
        _background = f;
        _background.setParent(this);
        _graphic = g;
    }

    /** Create a new icon figure using the given figure as the
     * "background" figure and with the given label.
     */
    public IconFigure(Figure f, String label) {
        _background = f;
        _background.setParent(this);
        setLabel(label);
    }

    /** Create a new icon figure using the given figure as the
     * "background" figure, the given Painted object as its "graphic,"
     * and the given string as a displayed label.
     */
    public IconFigure(Figure f, PaintedObject g, String label) {
        _background = f;
        _background.setParent(this);
        _graphic = g;
        setLabel(label);
    }

    /** Add the given terminal, on the given side and with the given
     * offset, to the icon. A BoundsSite will be created and the
     * terminal attached to it. See the BoundsSite class for a
     * description if the <i>side</i> and <i>offset</i> arguments.
     */
    public void addTerminal(Terminal t, int side, double offset) {
        BoundsSite site = new BoundsSite(this, _sites.size(), side, offset);
        t.setAttachSite(site);

        _sites.add(site);
        _terminals.add(t);

        repaint();
    }

    /** Get the background figure of the icon.
     */
    public Figure getBackground() {
        return _background;
    }

    /** Get the bounding box of this icon, including the terminals.
     * The bounds of the painted objects on top of the icon is not
     * included in the calculation, as its expensive to compute and
     * whoever created the icon is assumed to check that the painted
     * objects are within bounds at creation time.
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D bounds = (Rectangle2D) _background.getBounds().clone();
        bounds = ShapeUtilities.transformBounds(bounds, _transform);

        for (Iterator i = _terminals.iterator(); i.hasNext();) {
            Rectangle2D.union(bounds, ((Figure) i.next()).getBounds(), bounds);
        }

        return bounds;
    }

    /** Get the painted object that is drawn over the top
     * of the icon as its "graphic."
     */
    public PaintedObject getGraphic() {
        return _graphic;
    }

    /** Get the composite of this icon, or null if it doesn't
     * have one.
     */
    public Composite getComposite() {
        return _composite;
    }

    /** Get the shape of this figure. This is the shape of the background
     * figure.
     */
    @Override
    public Shape getShape() {
        Shape s = ShapeUtilities.cloneShape(_background.getShape());
        return ShapeUtilities.transformModify(s, _transform);
    }

    /** Get the terminal at the given index
     */
    public Terminal getTerminal(int index) {
        return (Terminal) _terminals.get(index);
    }

    /** Test if this figure intersects the given rectangle.
     */
    @Override
    public boolean intersects(Rectangle2D r) {
        boolean result = getShape().intersects(r);
        Iterator i = _terminals.iterator();

        while (!result && i.hasNext()) {
            result = result || ((Figure) i.next()).intersects(r);
        }

        return result;
    }

    /** Paint the icon.
     */
    @Override
    public void paint(Graphics2D g) {
        if (!isVisible()) {
            return;
        }

        if (_composite != null) {
            g.setComposite(_composite);
        }

        for (Iterator i = _terminals.iterator(); i.hasNext();) {
            ((Figure) i.next()).paint(g);
        }

        AffineTransform savedTransform = g.getTransform();
        g.transform(_transform);
        _background.paint(g);

        if (_graphic != null) {
            _graphic.paint(g);
        }

        g.setTransform(savedTransform);
    }

    /** Set the color composition operator of this figure. If the
     * composite is set to null, then the composite will not be
     * changed when the figure is painted -- provided that all
     * other objects in the system are well-behaved, this means
     * that icons will be opaque. The default composite value
     * is null.
     */
    public void setComposite(Composite c) {
        _composite = c;
        repaint();
    }

    /** Set the label of this figure.
     */
    public void setLabel(String s) {
        if (_label == null) {
            _label = new LabelFigure(s);
        }

        // FIXME
        repaint();
    }

    /** Get an iterator over the terminals of this figure.
     */
    public Iterator terminals() {
        return _terminals.iterator();
    }

    /** Transform the figure with the supplied transform. This method
     * transforms the background figure and the graphic, but not
     * the label or the terminals.
     */
    @Override
    public void transform(AffineTransform at) {
        repaint();
        _transform.preConcatenate(at);

        for (Iterator i = _terminals.iterator(); i.hasNext();) {
            ((Terminal) i.next()).relocate();
        }

        repaint();
    }

    /** Translate the figure the given distance.
     */
    @Override
    public void translate(double x, double y) {
        repaint();
        _transform.translate(x / _transform.getScaleX(),
                y / _transform.getScaleY());

        for (Iterator i = _terminals.iterator(); i.hasNext();) {
            ((Terminal) i.next()).translate(x, y);
        }

        repaint();
    }
}
