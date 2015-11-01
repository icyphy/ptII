/* Scale a two-dimensional figure based on the size provided by the user.

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
import diva.canvas.Figure;

///////////////////////////////////////////////////////////////////
//// Scale2D

/**
 Scale a two-dimensional figure by the x and y factor provided by the
 user.  If <i>accumulate</i> is set to true, any changes to the scale
 will be be relative to the figure's current size.  Otherwise, the
 scaling factor specified will be relative to the original size of the
 figure.

 @author Ismael M. Sarmiento, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (ismael)
 @Pt.AcceptedRating Yellow (chf)
 */
public class Scale2D extends GRTransform2D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Scale2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        initialScaleFactorX = new Parameter(this, "initialScaleFactorX",
                new DoubleToken(1.0));
        initialScaleFactorX.setTypeEquals(BaseType.DOUBLE);

        initialScaleFactorY = new Parameter(this, "initialScaleFactorY",
                new DoubleToken(1.0));
        initialScaleFactorY.setTypeEquals(BaseType.DOUBLE);

        scaleFactorX = new TypedIOPort(this, "scaleFactorX", true, false);
        scaleFactorX.setTypeEquals(BaseType.DOUBLE);

        scaleFactorY = new TypedIOPort(this, "scaleFactorY", true, false);
        scaleFactorY.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The initial scale factor of the figure in the x direction. */
    Parameter initialScaleFactorX;

    /** The initial scale factor of the figure in the y direction. */
    Parameter initialScaleFactorY;

    /** The factor by which to increase the figure size on the x-axis. */
    TypedIOPort scaleFactorX;

    /** The factor by which to increase the figure size on the y-axis. */
    TypedIOPort scaleFactorY;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the initial scaling transformation to the figure.
     *  @param figure The figure the transformation is to be applied to.
     *  @exception IllegalActionException If the getToken() method
     *  throws such an exception.
     */
    @Override
    protected void _applyInitialTransform(Figure figure)
            throws IllegalActionException {
        _oldScaleFactorX = ((DoubleToken) initialScaleFactorX.getToken())
                .doubleValue();
        _oldScaleFactorY = ((DoubleToken) initialScaleFactorY.getToken())
                .doubleValue();

        figure.transform(AffineTransform.getScaleInstance(_oldScaleFactorX,
                _oldScaleFactorY));
    }

    /** Apply the current scaling transformation to the figure.
     *  @param figure The figure the transformation is to be applied to.
     *  @exception IllegalActionException If the getToken() method throws
     *  such an exception.
     */
    @Override
    protected void _applyTransform(Figure figure) throws IllegalActionException {
        double scaleFactorXValue = 1.0;
        double scaleFactorYValue = 1.0;

        boolean needsTransform = false;

        if (scaleFactorX.isOutsideConnected() && scaleFactorX.hasToken(0)) {
            scaleFactorXValue = ((DoubleToken) scaleFactorX.get(0))
                    .doubleValue();
            needsTransform = true;
        }

        if (scaleFactorY.isOutsideConnected() && scaleFactorY.hasToken(0)) {
            scaleFactorYValue = ((DoubleToken) scaleFactorY.get(0))
                    .doubleValue();
            needsTransform = true;
        }

        if (needsTransform) {
            if (_isAccumulating()) {
                scaleFactorXValue *= _oldScaleFactorX;
                scaleFactorYValue *= _oldScaleFactorY;
            }

            _oldScaleFactorX = scaleFactorXValue;
            _oldScaleFactorY = scaleFactorYValue;

            AffineTransform inputTransform = AffineTransform.getScaleInstance(
                    scaleFactorXValue, scaleFactorYValue);

            figure.transform(inputTransform);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The previous scale factor applied to the figure across its x-axis.
    private double _oldScaleFactorX;

    // The previous scale factor applied to the figure across its y-axis.
    private double _oldScaleFactorY;
}
