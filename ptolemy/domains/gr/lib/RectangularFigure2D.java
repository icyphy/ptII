/* Create a rectangle, rounded rectangle, or ellipse with the size and
 position specified by the user.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.domains.gr.lib;

import java.awt.Point;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import diva.canvas.toolbox.BasicFigure;

///////////////////////////////////////////////////////////////////
//// RectangularFigure2D

/**
 A base class for actors that create figures based on rectangular
 bounding boxes, such as rectangles, rounded rectangles, and ellipses.
 The initial size, position, and type of figure are specified in the
 parameter edit window and can be changed after the figure has been
 displayed.

 @author Ismael M. Sarmiento, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (ismael)
 @Pt.AcceptedRating Red (chf)
 */
public abstract class RectangularFigure2D extends GRShape2D {
    /** Construct an actor with the given container and name.
     *  Initialize the position and size of the figure.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RectangularFigure2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xPosition = new Parameter(this, "xPosition", new DoubleToken(0.0));
        yPosition = new Parameter(this, "yPosition", new DoubleToken(0.0));
        width = new Parameter(this, "width", new DoubleToken(50.0));
        height = new Parameter(this, "height", new DoubleToken(50.0));

        xPosition.setTypeEquals(BaseType.DOUBLE);
        yPosition.setTypeEquals(BaseType.DOUBLE);
        width.setTypeEquals(BaseType.DOUBLE);
        height.setTypeEquals(BaseType.DOUBLE);
    }

    /** Update the position and location of the figure on the screen when
     *  the user changes the parameters.
     *  @param attribute The attribute which changed.
     *  @exception IllegalActionException If thrown while updating the
     *  figure or by the superclass.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if ((attribute == xPosition || attribute == yPosition
                || attribute == width || attribute == height)
                && _viewScreen != null) {
            _updateFigure();
        }

        super.attributeChanged(attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The initial x position (of type double) of the figure, before
     * additional transformations.  The default value is 0.0.
     */
    public Parameter xPosition;

    /** The initial y position (of type double) of the figure, before
     * additional transformations.  The default value is 0.0.
     */
    public Parameter yPosition;

    /** The initial width of the bounding rectangle (of type double)
     * of the figure, before additional transformations.  The default
     * value is 50.0.
     */
    public Parameter width;

    /** The initial height of the bounding rectangle (of type double)
     *  of the figure, before additional transformations.  The default
     *  value is 50.0.
     */
    public Parameter height;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the figure for this actor.  This method should be implemented
     *  by derived classes to create a figure of the appropriate type.
     *  @return A new Figure.
     *  @exception IllegalActionException If a parameter is not valid.
     */
    @Override
    protected abstract BasicFigure _createFigure()
            throws IllegalActionException;

    /** Return the offset in a single axis by which to move the rectangle
     *  so that the figure is centered at the cartesian origin.
     * @param dimension The size of the rectangle along a single axis.
     * @return A DoubleToken containing the offset needed to center the
     * figure.
     * @exception IllegalActionException If getToken() method throws such
     * an exception.
     */
    protected double _getCenterOffset(Parameter dimension)
            throws IllegalActionException {
        return ((DoubleToken) dimension.getToken()).doubleValue() / -2.0;
    }

    /** Return the center point of the rectangle as a Point.Double.
     * @return The rectangle's center point as a Point.Double.
     * @exception IllegalActionException If getToken() method throws such
     * an exception.
     */
    protected Point.Double _getCenterPoint() throws IllegalActionException {
        return new Point.Double(
                ((DoubleToken) xPosition.getToken()).doubleValue(),
                ((DoubleToken) yPosition.getToken()).doubleValue());
    }

    /** Calculate the lower left point of the rectangle as a Point.Double.
     * @return The lower left point of the rectangle as a Point.Double.
     * @exception IllegalActionException If getToken() method throws such
     * an exception.
     */
    protected Point.Double _getCornerPoint() throws IllegalActionException {
        Point.Double center = _getCenterPoint();
        return new Point.Double(center.getX()
                + ((DoubleToken) width.getToken()).doubleValue() / 2.0,
                center.getY() + ((DoubleToken) height.getToken()).doubleValue()
                / 2.0);
    }

    /** Update the figure's position and size when the user changes
     *  the appropriate parameters.
     * @exception IllegalActionException If getToken() generates
     * IllegalActionException.
     */
    protected abstract void _updateFigure() throws IllegalActionException;
}
