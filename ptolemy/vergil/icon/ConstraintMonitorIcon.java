/* An icon providing visual indication when constraints are violated.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.vergil.icon;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.ConstraintMonitor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.Figure;

///////////////////////////////////////////////////////////////////
//// ConstraintMonitorIcon

/**
An icon providing visual indication when constraints are violated.
This works specifically with {@link ConstraintMonitor}.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (celaine)
 */
public class ConstraintMonitorIcon extends BoxedValueIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ConstraintMonitorIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        okColor = new ColorAttribute(this, "okColor");
        okColor.setExpression("{0.3, 1.0, 0.3, 1.0}");

        closeColor = new ColorAttribute(this, "closeColor");
        closeColor.setExpression("{1.0, 1.0, 0.0, 1.0}");

        closeFraction = new Parameter(this, "closeFraction");
        closeFraction.setTypeEquals(BaseType.DOUBLE);
        closeFraction.setExpression("0.1");

        highColor = new ColorAttribute(this, "highColor");
        highColor.setExpression("{1.0, 0.3, 0.3, 1.0}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Color of the box to use when the constraint is close
     *  to the threshold.
     *  This defaults to yellow.
     */
    public ColorAttribute closeColor;

    /** Fraction of the threshold that is to be considered close
     *  to the threshold. This is a double that defaults to 0.1.
     */
    public Parameter closeFraction;

    /** Color of the box to use when the constraint is above
     *  the threshold.
     *  This defaults to pink.
     */
    public ColorAttribute highColor;

    /** Color of the box to use when the constraint is satisfied.
     *  This defaults to a light green.
     */
    public ColorAttribute okColor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to modify the background color, if appropriate.
     *  @return A new figure.
     */
    @Override
    public Figure createBackgroundFigure() {
        ConstraintMonitor container = (ConstraintMonitor) getContainer();
        try {
            double aggregateValue = ((DoubleToken) container.getToken())
                    .doubleValue();
            double threshold = ((DoubleToken) container.threshold.getToken())
                    .doubleValue();
            double close = ((DoubleToken) closeFraction.getToken())
                    .doubleValue();
            if (aggregateValue >= threshold) {
                boxColor.setToken(highColor.getToken());
            } else if ((threshold != Double.POSITIVE_INFINITY)
                    && (threshold - aggregateValue) <= close * threshold) {
                boxColor.setToken(closeColor.getToken());
            } else {
                boxColor.setToken(okColor.getToken());
            }
        } catch (IllegalActionException e) {
            // Ignore and use default color.
        }

        return super.createBackgroundFigure();
    }

    /** Override the base class to throw an exception if the container is
     *  not an instance of {@link ConstraintMonitor}.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        if (container != null && !(container instanceof ConstraintMonitor)) {
            throw new IllegalActionException(this, container,
                    "Container is required to be a ConstraintMonitor");
        }
        super.setContainer(container);
    }
}
