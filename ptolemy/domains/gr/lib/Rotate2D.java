/* Rotate a two-dimensional figure based on the angle and anchor
 point provided by the user.

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

import java.awt.geom.AffineTransform;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import diva.canvas.Figure;

///////////////////////////////////////////////////////////////////
//// Rotate2D

/**
 Rotate a two-dimensional figure based on the angle, and anchor point
 provided by the user.  The angle, step, and anchor points can either
 be preset in the parameter edit window, or updated dynamically through
 the actor's ports.  The angle can be specified in radians or degrees
 by selecting the angle type in the parameter edit window.  Angles
 increase clockwise beginning at the positive X-axis in a Cartesian
 plane.  If the <i>accumulate</i> parameter defined in the base class
 is set to true, any changes to the angle of rotation will be relative
 to the figure's current orientation.  Otherwise, the angle specified
 will be relative to the positive X-axis.

 @author Ismael M. Sarmiento, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (ismael)
 @Pt.AcceptedRating Yellow (chf)
 */
public class Rotate2D extends GRTransform2D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Rotate2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        angleType = new StringAttribute(this, "angleType");
        angleType.setExpression("radians");

        initialTheta = new Parameter(this, "initialTheta", new DoubleToken(0.0));
        initialTheta.setTypeEquals(BaseType.DOUBLE);

        initialAnchorX = new Parameter(this, "initialAnchorX", new DoubleToken(
                0.0));
        initialAnchorX.setTypeEquals(BaseType.DOUBLE);

        initialAnchorY = new Parameter(this, "initialAnchorY", new DoubleToken(
                0.0));
        initialAnchorY.setTypeEquals(BaseType.DOUBLE);

        theta = new TypedIOPort(this, "theta", true, false);
        theta.setTypeEquals(BaseType.DOUBLE);

        anchorX = new TypedIOPort(this, "anchorX", true, false);
        anchorX.setTypeEquals(BaseType.DOUBLE);

        anchorY = new TypedIOPort(this, "anchorY", true, false);
        anchorY.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The angle the figure is to be rotated by, measured clockwise from
     *  the positive X-axis on a Cartesian plane.  The default double value
     *  is 0.0.
     */
    public TypedIOPort theta;

    /** The X coordinate of the point the figure is to be rotated about.
     *  The default double value is 0.0.
     */
    public TypedIOPort anchorX;

    /** The Y coordinate of the point the figure is to be rotated about.
     *  The default double value is 0.0.
     */
    public TypedIOPort anchorY;

    /** The initial angle of rotation.  The default double value is 0.0.
     */
    public Parameter initialTheta;

    /** The initial x-coordinate of the anchor point.
     *  The default double value is 0.0. */
    public Parameter initialAnchorX;

    /** The initial y-coordinate of the anchor point.
     *  The default double value is 0.0. */
    public Parameter initialAnchorY;

    /** How the angle is specified.  Can be <B>degrees</B> or
     *  <B>radians</B> (default).
     */
    public StringAttribute angleType;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Apply the initial rotation to the figure.
     *  @param figure The figure the transformation is to be applied to.
     *  @exception IllegalActionException If the getToken() method
     *  throws such an exception.
     */
    @Override
    protected void _applyInitialTransform(Figure figure)
            throws IllegalActionException {
        _oldAngle = ((DoubleToken) initialTheta.getToken()).doubleValue();
        _oldAnchorX = ((DoubleToken) initialAnchorX.getToken()).doubleValue();
        _oldAnchorY = ((DoubleToken) initialAnchorY.getToken()).doubleValue();

        if (angleType.getExpression().equals("degrees")) {
            _oldAngle = Math.toRadians(_oldAngle);
        }

        figure.transform(AffineTransform.getRotateInstance(_oldAngle,
                _oldAnchorX, _oldAnchorY));
    }

    /** Apply the current rotation transformation to the figure.
     *  @param figure The figure the transformation is to be applied to.
     *  @exception IllegalActionException If the getToken() method throws
     *  such an exception.
     */
    @Override
    protected void _applyTransform(final Figure figure)
            throws IllegalActionException {
        double angle = _oldAngle;
        double anchorXValue = _oldAnchorX;
        double anchorYValue = _oldAnchorY;

        boolean needsTransform = false;

        if (theta.isOutsideConnected() && theta.hasToken(0)) {
            angle = ((DoubleToken) theta.get(0)).doubleValue();
            needsTransform = true;

            if (angleType.getExpression().equals("degrees")) {
                angle = Math.toRadians(angle);
            }
        }

        if (anchorX.isOutsideConnected() && anchorX.hasToken(0)) {
            anchorXValue = ((DoubleToken) anchorX.get(0)).doubleValue();
            needsTransform = true;
        }

        if (anchorY.isOutsideConnected() && anchorY.hasToken(0)) {
            anchorYValue = -((DoubleToken) anchorY.get(0)).doubleValue();
            needsTransform = true;
        }

        if (needsTransform) {
            final AffineTransform inputTransform = AffineTransform
                    .getRotateInstance(angle, anchorXValue, anchorYValue);

            if (!figure.getTransformContext().getTransform()
                    .equals(inputTransform)) {
                if (!_isAccumulating()) {
                    inputTransform.concatenate(figure.getTransformContext()
                            .getInverseTransform());
                }

                figure.transform(inputTransform);
            }
        }
    }

    private double _oldAngle;

    private double _oldAnchorX;

    private double _oldAnchorY;
}
