/* A site for instances of PortTerminal. */

/*
 Copyright (c) 2006-2014 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 */

package ptolemy.vergil.actor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import diva.canvas.AbstractSite;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;

///////////////////////////////////////////////////////////////////
//// PortConnectSite

/**
 A site for instances of PortTerminal. For non-muiltiports, a PortTerminal
 will have exactly one of these sites, and it will be the connect site
 for the terminal.  Multiports, however, will generate distinct site for
 each connection to the multiport.
 <p>
 The normal for this site (the direction in which connections are made
 to it) is fixed when the site is constructed, and cannot be changed
 after that.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @see PortTerminal
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class PortConnectSite extends AbstractSite {
    /** Create a port connect site for the specified figure
     *  and id. An id of zero is the default site at the port
     *  icon. Larger ids are reserved for sites that are
     *  used to distinguish multiple connections to a multiport.
     *  @param figure The parent figure.
     *  @param terminal The port terminal.
     *  @param id The ID.
     *  @param normal The normal for this connect site.
     */
    public PortConnectSite(Figure figure, PortTerminal terminal, int id,
            double normal) {
        _parentFigure = figure;
        _terminal = terminal;
        _id = id;
        _hasNormal = true;
        _normal = CanvasUtilities.moduloAngle(normal);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the parent figure specified in the constructor.
     *  @return The parent figure.
     */
    @Override
    public Figure getFigure() {
        return _parentFigure;
    }

    /** Return the ID specified in the constructor.
     *  The default ID is zero. When additional instances of this
     *  site are associated with an instance of PortTerminal, they
     *  each get a unique ID starting with 1 and increasing by 1.
     *  The ID determines the position of the site relative to the
     *  port.
     *  @return The ID of the site.
     */
    @Override
    public int getID() {
        return _id;
    }

    /** Get the position of this site.
     *  @param normal The normal.
     *  @return The position of this site.
     */
    @Override
    public Point2D getPoint(double normal) {
        Rectangle2D bounds = _parentFigure.getShape().getBounds2D();

        double x = bounds.getX();
        double y = bounds.getY();
        double width = bounds.getWidth();
        double height = bounds.getHeight();

        double xout;
        double yout;

        if (normal < CanvasUtilities.NORTHWEST
                || normal > CanvasUtilities.SOUTHWEST) {
            // On the left edge.
            if (_id > 0) {
                int numberOfLinks = _terminal.getNumberOfLinks();
                xout = x - (numberOfLinks - _id)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
                yout = y + height / 2 - (numberOfLinks - _id)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
            } else {
                xout = x;
                yout = y + height / 2;
            }
        } else if (normal < CanvasUtilities.NORTHEAST) {
            // On the top edge.
            if (_id > 0) {
                int numberOfLinks = _terminal.getNumberOfLinks();
                yout = y - (numberOfLinks - _id)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
                xout = x + width / 2 - (numberOfLinks - _id)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
            } else {
                xout = x + width / 2;
                yout = y;
            }
        } else if (normal < CanvasUtilities.SOUTHEAST) {
            // On the right edge.
            if (_id > 0) {
                xout = x + width + (_id - 1)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
                yout = y + height / 2 + (_id - 1)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
            } else {
                xout = x + width;
                yout = y + height / 2;
            }
        } else {
            // On the bottom edge.
            if (_id > 0) {
                yout = y + height + (_id - 1)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
                xout = x + width / 2 + (_id - 1)
                        * IOPortController.MULTIPORT_CONNECTION_SPACING;
            } else {
                xout = x + width / 2;
                yout = y + height;
            }
        }

        return new Point2D.Double(xout, yout);
    }

    /** Get the terminal to which this site belongs.
     *  @return The terminal to which this site belongs.
     */
    public PortTerminal getTerminal() {
        return _terminal;
    }

    /** Get the horizontal position of this site with
     *  the normal that was set up by the constructor.
     *  @return The horizontal position of this site.
     */
    @Override
    public double getX() {
        return getPoint().getX();
    }

    /** Get the vertical position of this site with
     *  the normal that was set up by the constructor.
     *  @return The vertical position of this site.
     */
    @Override
    public double getY() {
        return getPoint().getY();
    }

    /** Do nothing. The normal is fixed at the time this is constructed.
     *  @param normal The normal.
     */
    @Override
    public void setNormal(double normal) {
        // Do nothing.
    }

    /** Return a string representation of this connect site.
     *  @return The name of the port and the ID.
     */
    @Override
    public String toString() {
        return "PortConnectSite for connection number " + _id + " of port "
                + _terminal.getPort().getFullName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The ID. */
    private int _id = 0;

    /** The parent figure. */
    private Figure _parentFigure;

    /** The port terminal. */
    private PortTerminal _terminal;
}
