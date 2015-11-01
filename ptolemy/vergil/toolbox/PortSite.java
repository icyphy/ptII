/* The site for ports.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.vergil.toolbox;

import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.kernel.Port;
import diva.canvas.AbstractSite;
import diva.canvas.Figure;

/**
 A site that locates itself on the bounds of a figure's shape, designed
 for placing ports.

 @version $Id$
 @author Edward A. Lee
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class PortSite extends AbstractSite {
    /** Create a new site on the given figure with the given ID
     *  port type, and port number.
     *  @param figure The figure for the entity icon.
     *  @param port The port, which is ignored by this method
     *  @param number The number of the port within its kind, starting with 0.
     *  @param count The number of ports of its kind.
     *  @param direction One of SwingConstants.{WEST, NORTH, EAST, SOUTH}.
     */
    public PortSite(Figure figure, Port port, int number, int count,
            int direction) {
        _parentFigure = figure;
        // Ignored _port = port;
        _number = number;
        _count = count;
        _direction = direction;
        _normal = _getNormal();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the figure to which this site is attached.
     */
    @Override
    public Figure getFigure() {
        return _parentFigure;
    }

    /** Return the ID for this site, which is the number of the port.
     *  @return The number of the port.
     */
    @Override
    public int getID() {
        return _number;
    }

    /** Get the normal of the site.
     */
    @Override
    public double getNormal() {
        return _normal;
    }

    /** Get the x-coordinate of the site.
     */
    @Override
    public double getX() {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        if (_direction == SwingConstants.WEST) {
            // Port is on the left.
            return bounds.getX();
        } else if (_direction == SwingConstants.EAST) {
            // Port is on the right.
            return bounds.getX() + bounds.getWidth();
        } else {
            // Port is on the top or bottom side.
            int halfCount = _count / 2;
            double offset = bounds.getWidth() / 2.0 - halfCount * _snap;

            // If there are an even number of ports, skip the middle
            // position to get symmetry.
            boolean skipOne = _count / 2 * 2 == _count;

            if (skipOne && _number >= _count / 2) {
                offset += _snap;
            }

            return bounds.getX() + _snap * _number + offset;
        }
    }

    /** Get the y-coordinate of the site.
     */
    @Override
    public double getY() {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        if (_direction == SwingConstants.SOUTH) {
            // Port is on the bottom.
            return bounds.getY() + bounds.getHeight();
        } else if (_direction == SwingConstants.NORTH) {
            // Port is on the top.
            return bounds.getY();
        } else {
            // Port is on the left or right.
            int halfCount = _count / 2;
            double offset = bounds.getHeight() / 2.0 - halfCount * _snap;

            // If there are an even number of ports, skip the middle
            // position to get symmetry.
            boolean skipOne = _count / 2 * 2 == _count;

            if (skipOne && _number >= _count / 2) {
                offset += _snap;
            }
            return bounds.getY() + _snap * _number + offset;
        }
    }

    @Override
    public String toString() {
        return "BoundsSite[" + getX() + "," + getY() + "," + getNormal() + "]";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the normal of the site.
     */
    private double _getNormal() {
        if (_direction == SwingConstants.NORTH) {
            return -Math.PI / 2;
        } else if (_direction == SwingConstants.EAST) {
            return 0.0;
        } else if (_direction == SwingConstants.WEST) {
            return Math.PI;
        } else {
            return Math.PI / 2;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The number of ports of the kind as this one. */
    private int _count;

    /** The direction of the port. */
    private int _direction;

    /** The normal. */
    private double _normal;

    /** The number of this port within the ones of the same kind. */
    private int _number;

    /** The parent figure. */
    private Figure _parentFigure;

    /** The snap resolution.  FIXME: This should not be here. */
    private double _snap = 10.0;
}
