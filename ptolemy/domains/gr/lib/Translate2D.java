/* An actor that translates the input 2D shape

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

import java.awt.geom.Point2D;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import diva.canvas.Figure;

///////////////////////////////////////////////////////////////////
//// Translate2D

/**
 This actor represents a translation of a two-dimensional GR scene.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (chf)
 @Pt.AcceptedRating Red (chf)
 */
public class Translate2D extends GRTransform2D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Translate2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xTranslate = new TypedIOPort(this, "xTranslate", true, false);
        xTranslate.setTypeEquals(BaseType.DOUBLE);

        yTranslate = new TypedIOPort(this, "yTranslate", true, false);
        yTranslate.setTypeEquals(BaseType.DOUBLE);

        initialXTranslation = new Parameter(this, "initialXTranslation",
                new DoubleToken(0.0));
        initialXTranslation.setTypeEquals(BaseType.DOUBLE);

        initialYTranslation = new Parameter(this, "initialYTranslation",
                new DoubleToken(0.0));
        initialYTranslation.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount of translation in the x-axis during firing. If this
     *  transform is in accumulate mode, the translation value is
     *  accumulated for each firing.
     */
    public TypedIOPort xTranslate;

    /** The amount of translation in the y-axis during firing. If this
     *  transform is in accumulate mode, the translation value is
     *  accumulated for each firing.
     */
    public TypedIOPort yTranslate;

    /** The initial translation in the x-axis
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is 0.0.
     */
    public Parameter initialXTranslation;

    /** The initial translation in the y-axis
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is 0.0.
     */
    public Parameter initialYTranslation;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the initial transform of the given figure.  This method is
     * invoked by this base class during the initialize() method.
     * Derived classes should implement it to provide class-specific
     * behavior.
     *  @exception IllegalActionException If the value of some
     *  parameters can't be obtained.
     */
    @Override
    protected void _applyInitialTransform(Figure figure)
            throws IllegalActionException {
        double initialX = ((DoubleToken) initialXTranslation.getToken())
                .doubleValue();
        double initialY = ((DoubleToken) initialYTranslation.getToken())
                .doubleValue();

        // Translate to?
        figure.translate(initialX, initialY);
    }

    /** Consume input tokens, and transform the given figure according
     * to the current state of the transform.  This method is invoked
     * by this base classes during the fire() method.
     *  @exception IllegalActionException If the value of some
     *  parameters can't be obtained.
     */
    @Override
    protected void _applyTransform(Figure figure) throws IllegalActionException {
        boolean applyTransform = false;
        double xOffset = 0.0;
        double yOffset = 0.0;
        double initialX = ((DoubleToken) initialXTranslation.getToken())
                .doubleValue();
        double initialY = ((DoubleToken) initialYTranslation.getToken())
                .doubleValue();
        boolean isAccumulating = _isAccumulating();
        Point2D origin = figure.getOrigin();

        if (xTranslate.isOutsideConnected()) {
            if (xTranslate.hasToken(0)) {
                double in = ((DoubleToken) xTranslate.get(0)).doubleValue();
                applyTransform = true;
                xOffset = in;

                if (!isAccumulating) {
                    // Subtract the current xOrigin.
                    xOffset = xOffset - origin.getX() + initialX;
                }
            }
        }

        if (yTranslate.isOutsideConnected()) {
            if (yTranslate.hasToken(0)) {
                double in = ((DoubleToken) yTranslate.get(0)).doubleValue();
                applyTransform = true;
                yOffset = in;

                if (!isAccumulating) {
                    // Subtract the current yOrigin.
                    yOffset = yOffset - origin.getY() + initialY;
                }
            }
        }

        if (applyTransform) {
            figure.translate(xOffset, yOffset);
        }
    }
}
