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
package diva.canvas.interactor;

import java.awt.geom.Rectangle2D;

import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;

/** An interactor that drags its target only within a given
 * rectangular region. An instance of this class sets itself up so
 * that the dragged figures or items always remain within the given
 * region. To do so, it creates an instance of BoundsConstraint that
 * it attaches to itself, and overrides the setup() method to
 * initialize the constraint according to the size of the target.
 *
 * <p> This interactor is intended more as an example of how to
 * produce a customized drag-interactor than anything else.
 *
 * @version $Id$
 * @author John Reekie
 */
public class BoundedDragInteractor extends DragInteractor {
    /** The bounds
     */
    private Rectangle2D _bounds;

    /** The bounds constraint
     */
    private BoundsConstraint _constraint;

    /**
     * Create an instance that keeps figures inside the given regio
     */
    public BoundedDragInteractor(Rectangle2D bounds) {
        super();
        _bounds = bounds;
        _constraint = new BoundsConstraint(_bounds);
        appendConstraint(_constraint);
    }

    /** Adjust the bounds so that the bounding-box of the target stays
     * within the region.
     */
    @Override
    public void setup(LayerEvent e) {
        // Get the size of the figure and calculate bounds
        // FIXME: how to parameterize for figure sets?
        Figure f = e.getFigureSource();
        double ex = e.getLayerX();
        double ey = e.getLayerY();
        Rectangle2D b = f.getBounds();

        double x = _bounds.getX() + ex - b.getX();
        double y = _bounds.getY() + ey - b.getY();

        double w = _bounds.getX() + _bounds.getWidth() + ex
                - (b.getX() + b.getWidth()) - x;
        double h = _bounds.getY() + _bounds.getHeight() + ey
                - (b.getY() + b.getHeight()) - y;

        // Finally (!), set the bounds constraint
        _constraint.setBounds(new Rectangle2D.Double(x, y, w, h));
    }
}
