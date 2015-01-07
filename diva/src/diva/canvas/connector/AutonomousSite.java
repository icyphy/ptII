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
import diva.canvas.CanvasLayer;
import diva.canvas.CanvasPane;
import diva.canvas.Figure;
import diva.canvas.TransformContext;

/** A site that is not attached to a figure. Autonomous sites are
 * useful in building editors in which connectors can be reshaped
 * or reconnected, as the connector can be attached to an autonomous
 * site and then the autonomous site moved. Because sites must be
 * located in a transform context, the constructor of autonomous
 * sites requires that a transform context be supplied. For convenience,
 * there are other constructors that accept a figures or pane, and
 * use the transform context of that object.
 *
 * @version        $Id$
 * @author         John Reekie
 */
public class AutonomousSite extends AbstractSite {
    /** The enclosing transform context
     */
    private TransformContext _context;

    /** The location of this site.
     */
    private double _x = 0.0;

    private double _y = 0.0;

    /** Create a new autonomous site in the given transform
     * context and at the given location within that context.
     */
    public AutonomousSite(TransformContext c, double x, double y) {
        setLocation(c, x, y);
    }

    /** Create a new autonomous site in the transform
     * context of the given pane and at the given location within that pane.
     */
    public AutonomousSite(CanvasPane p, double x, double y) {
        setLocation(p.getTransformContext(), x, y);
    }

    /** Create a new autonomous site in the transform
     * context of the given pane and at the given location within that layer.
     */
    public AutonomousSite(CanvasLayer l, double x, double y) {
        setLocation(l.getTransformContext(), x, y);
    }

    /** Create a new autonomous site in the transform context of
     * the given figure and at the given location within that figure.
     */
    public AutonomousSite(Figure f, double x, double y) {
        setLocation(f.getTransformContext(), x, y);
    }

    /** Return null. Autonomous sites are not attached to a figure.
     */
    @Override
    public Figure getFigure() {
        return null;
    }

    /** Return zero. Autonomous sites don't have a meaningful ID.
     */
    @Override
    public int getID() {
        return 0;
    }

    /** Get the enclosing transform context of this site. This
     * is the context given to the constructor or set in the
     * setLocation() method.
     */
    @Override
    public TransformContext getTransformContext() {
        return _context;
    }

    /** Get the x-coordinate of the site, in the enclosing
     * transform context.
     */
    @Override
    public double getX() {
        return _x;
    }

    /** Get the y-coordinate of the site, in the enclosing
     * transform context.
     */
    @Override
    public double getY() {
        return _y;
    }

    /** Set the transform context and the location within the new
     * transform context. This is typically used when dragging
     * an autonomous site across context boundaries.
     */
    public void setLocation(TransformContext c, double x, double y) {
        _context = c;
        _x = x;
        _y = y;
    }

    /** Translate the site by the indicated distance.
     */
    @Override
    public void translate(double x, double y) {
        _x += x;
        _y += y;
    }
}
