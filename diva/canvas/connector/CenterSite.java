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

package diva.canvas.connector;

import java.awt.geom.Rectangle2D;

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
public class CenterSite extends AbstractSite {

    /** The id
     */
    private int _id;

    /** The parent figure
     */
    private Figure _parentFigure;

    /** Create a new site on the given figure. The site will have
     * the ID zero.
     *
     * FIXME: This should be deprecated? Use the constructor that takes an ID.
     */
    public CenterSite (Figure figure) {
        this(figure,0);
    }

    /** Create a new site on the given figure and with the given ID
     */
    public CenterSite (Figure figure, int id) {
        this._id = id;
        this._parentFigure = figure;
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
        Rectangle2D bounds = _parentFigure.getBounds();
        return bounds.getX() + bounds.getWidth()/2;
    }

    /** Get the y-coordinate of the site.  The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    public double getY () {
        Rectangle2D bounds = _parentFigure.getBounds();
        return bounds.getY() + bounds.getHeight()/2;
    }
}


