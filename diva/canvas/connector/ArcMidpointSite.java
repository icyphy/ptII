/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;

/** A concrete implementation of Site that is located in the
 * center of the bounding box of a figure. This is a utility class
 * provided for convenience of figures that need to make their
 * center points connectible.
 *
 * @version        $Revision$
 * @author         John Reekie
 */
public class ArcMidpointSite extends AbstractSite {

    /** The id
     */
    private int _id;

    /** The parent figure
     */
    private ArcConnector _parentFigure;

    /** Create a new site on the given arc connector with the given ID.
     *  @param connector The arc connector.
     *  @param id The id.
     */
    public ArcMidpointSite (ArcConnector connector, int id) {
        this._id = id;
        this._parentFigure = connector;
    }

    /** Get the figure to which this site is attached.
     */
    public Figure getFigure () {
        return _parentFigure;
    }

    /** Get the ID of this site.
     */
    public int getID () {
        return _id;
    }

    /** Get the x-coordinate of the site. The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    public double getX () {
        return _parentFigure.getArcMidpoint().getX();
    }

    /** Get the y-coordinate of the site.  The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    public double getY () {
        return _parentFigure.getArcMidpoint().getY();
    }
}
