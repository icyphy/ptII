/* The site for ports.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import diva.canvas.AbstractSite;
import diva.canvas.Figure;
import ptolemy.actor.IOPort;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
A site that locates itself on the bounds of a figure's shape, designed
for placing ports.

@version	$Id$
@since Ptolemy II 2.0
@author 	Edward A. Lee
*/
public class PortSite extends AbstractSite {

    /** Create a new site on the given figure with the given ID
     *  port type, and port number.
     *  @param figure The figure for the entity icon.
     *  @param port The port.
     *  @param number The number of the port within its kind, starting with 0.
     *  @param count The number of ports of its kind.
     */
    public PortSite(Figure figure, IOPort port, int number, int count) {
        _parentFigure = figure;
        _port = port;
        _number = number;
        _count = count;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the figure to which this site is attached.
     */
    public Figure getFigure() {
        return _parentFigure;
    }

    /** Return the ID for this site, which is the number of the port.
     *  @return The number of the port.
     */
    public int getID() {
        return _number;
    }

    /** Get the normal of the site.
     */
    public double getNormal() {
        if (_port.isInput()) {
            if (_port.isOutput()) {
                // Port is both an input and an output.
                return Math.PI/2;
            } else {
                // Port is an input only.
                return Math.PI;
            }
        }
        // Port is an output only, or neither.
        return 0.0;
    }

    /** Get the x-coordinate of the site.
     */
    public double getX() {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        double x = 0.0;
        if (_port.isInput() && _port.isOutput()) {
            // Port is both an input and an output.
            double offset = bounds.getWidth()/2.0 - (_count/2)*_snap;
            // If there are an even number of ports, skip the middle
            // position to get symmetry.
            boolean skipOne = (_count/2)*2 == _count;
            if (skipOne && _number >= _count/2) offset += _snap;

            x = bounds.getX() + _snap * _number + offset;
        } else if (_port.isInput()) {
            // Port is an input only.
            x = bounds.getX();
        } else if (_port.isOutput()) {
            // Port is an output only.
            x = bounds.getX() + bounds.getWidth();
        }
        return x;
    }

    /** Get the y-coordinate of the site.
     */
    public double getY() {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        double y = 0.0;
        if (_port.isInput() && _port.isOutput()) {
            // Port is both an input and an output.
            y = bounds.getY() + bounds.getHeight();
        } else if (_port.isInput() || _port.isOutput()) {
            // Port is an input or output only.
            double offset = bounds.getHeight()/2.0 - (_count/2)*_snap;
            // If there are an even number of ports, skip the middle
            // position to get symmetry.
            boolean skipOne = (_count/2)*2 == _count;
            if (skipOne && _number >= _count/2) offset += _snap;

            y = bounds.getY() + _snap * _number + offset;
        }
        return y;
    }

    public String toString() {
	return "BoundsSite[" + getX() + "," + getY() + "," + getNormal() + "]";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The number of ports of the kind as this one. */
    private int _count;

    /** The ID. */
    private int _id;

    /** The normal. */
    private double _normal;

    /** The number of this port within the ones of the same kind. */
    private int _number;

    /** The parent figure. */
    private Figure _parentFigure;

    /** The port. */
    private IOPort _port;

    /** The snap resolution.  FIXME: This should not be here. */
    private double _snap = 10.0;
}
