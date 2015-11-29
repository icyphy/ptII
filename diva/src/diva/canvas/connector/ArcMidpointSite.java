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

import diva.canvas.AbstractSite;
import diva.canvas.Figure;

/** A concrete implementation of Site that is located in the
 * center of the bounding box of a figure. This is a utility class
 * provided for convenience of figures that need to make their
 * center points connectible.
 *
 * @version        $Id$
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
    public ArcMidpointSite(ArcConnector connector, int id) {
        this._id = id;
        this._parentFigure = connector;
    }

    /** Get the figure to which this site is attached.
     */
    @Override
    public Figure getFigure() {
        return _parentFigure;
    }

    /** Get the ID of this site.
     */
    @Override
    public int getID() {
        return _id;
    }

    /** Get the x-coordinate of the site. The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    @Override
    public double getX() {
        return _parentFigure.getArcMidpoint().getX();
    }

    /** Get the y-coordinate of the site.  The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    @Override
    public double getY() {
        return _parentFigure.getArcMidpoint().getY();
    }
}
