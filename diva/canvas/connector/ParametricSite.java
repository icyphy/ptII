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

import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;

/**
 * A site that locates itself in a figure at a location
 * in X and Y that is some percentage of the bounding box
 * of the figure.  So as the figure grows, the site relocates
 * itself proportionally.
 *
 * @version        $Id$
 * @author         Michael Shilman
 */
public class ParametricSite extends AbstractSite {
    /**
     * The id
     */
    private int _id;

    /**
     * The parent figure
     */
    private Figure _parentFigure;

    /**
     * The X parameter, denoting the percentage of
     * the width relative to the left side of the
     * figure.
     */
    private double _xt;

    /**
     * The Y parameter, denoting the percentage of
     * the height relative to the top side of the
     * figure.
     */
    private double _yt;

    /**
     * Create a new site on the given figure with the given id,
     * located at (xt, yt) percentage of the given figure's bounding
     * box, with (0,0) being the upper left-hand corner and (1,1)
     * being the lower right. The site will have the ID zero.
     */
    public ParametricSite(Figure figure, int id, double xt, double yt) {
        this._id = id;
        this._parentFigure = figure;
        _xt = xt;
        _yt = yt;
    }

    /**
     * Get the figure to which this site is attached.
     */
    @Override
    public Figure getFigure() {
        return _parentFigure;
    }

    /**
     * Get the ID of this site.
     */
    @Override
    public int getID() {
        return _id;
    }

    /**
     * Get the X-coordinate of the site. The site
     * is located at some percentage of the parent
     * figure's bounding width.
     */
    @Override
    public double getX() {
        Rectangle2D bounds = _parentFigure.getBounds();
        double x = bounds.getX() + _xt * bounds.getWidth();
        return x;
    }

    /**
     * Get the Y-coordinate of the site. The site
     * is located at some percentage of the parent
     * figure's bounding height.
     */
    @Override
    public double getY() {
        Rectangle2D bounds = _parentFigure.getBounds();
        double y = bounds.getY() + _yt * bounds.getHeight();
        return y;
    }
}
