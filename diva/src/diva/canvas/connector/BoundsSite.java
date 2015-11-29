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

import javax.swing.SwingConstants;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;

/** A site that locates itself on the bounds of a figure's shape.
 * It has two fields that govern its position on the bounds.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class BoundsSite extends AbstractSite {
    /** The id
     */
    private int _id;

    /** The side to be located on: NORTH, SOUTH, EAST, WEST
     */
    private int _side;

    /** The distance to be located along that side, in percent
     */
    private double _offset;

    /** The parent figure
     */
    private Figure _parentFigure;

    /** Create a new site on the given figure with the given ID
     * and at the location given by the side and the offset.
     */
    public BoundsSite(Figure figure, int id, int side, double offset) {
        _parentFigure = figure;
        _id = id;
        _side = side;
        _offset = offset;
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

    /** Get the normal of the site.
     */
    @Override
    public double getNormal() {
        switch (_side) {
        case SwingConstants.NORTH:
            return -Math.PI / 2;

        case SwingConstants.SOUTH:
            return Math.PI / 2;

        case SwingConstants.WEST:
            return Math.PI;
        case SwingConstants.EAST:
        default:
            return 0.0;
        }
    }

    /** Get the distance to be located along the side, in percent.
     */
    public double getOffset() {
        return _offset;
    }

    /** Get the side to be located on: NORTH, SOUTH, EAST, WEST.
     */
    public int getSide() {
        return _side;
    }

    /** Get the x-coordinate of the site.
     */
    @Override
    public double getX() {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        double x = 0.0;

        switch (_side) {
        case SwingConstants.NORTH:
        case SwingConstants.SOUTH:
            x = bounds.getX() + _offset / 100.0 * bounds.getWidth();
            break;

        case SwingConstants.EAST:
            x = bounds.getX() + bounds.getWidth();
            break;

        case SwingConstants.WEST:
            x = bounds.getX();
            break;
        }

        return x;
    }

    /** Get the y-coordinate of the site.
     */
    @Override
    public double getY() {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        double y = 0.0;

        switch (_side) {
        case SwingConstants.EAST:
        case SwingConstants.WEST:
            y = bounds.getY() + _offset / 100.0 * bounds.getHeight();
            break;

        case SwingConstants.SOUTH:
            y = bounds.getY() + bounds.getHeight();
            break;

        case SwingConstants.NORTH:
            y = bounds.getY();
            break;
        }

        return y;
    }

    @Override
    public String toString() {
        return "BoundsSite[" + getX() + "," + getY() + "," + getNormal() + "]";
    }
}
