/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * itself proportinally.
 *
 * @version        $Revision$
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
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
    public Figure getFigure () {
        return _parentFigure;
    }

    /**
     * Get the ID of this site.
     */
    public int getID () {
        return _id;
    }

    /**
     * Get the X-coordinate of the site. The site
     * is located at some percentage of the parent
     * figure's bounding width.
     */
    public double getX () {
        Rectangle2D bounds = _parentFigure.getBounds();
        double x = bounds.getX() + _xt*bounds.getWidth();
        return x;
    }

    /**
     * Get the Y-coordinate of the site. The site
     * is located at some percentage of the parent
     * figure's bounding height.
     */
    public double getY () {
        Rectangle2D bounds = _parentFigure.getBounds();
        double y = bounds.getY() + _yt*bounds.getHeight();
        return y;
    }
}


