/* An actor that converts polar coordinates to Cartesian coordinates.

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
package ptolemy.actor.lib.conversions;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
/// PolarToCartesian

/**

 This actor reads two double tokens (magnitude and angle) and outputs
 two new double tokens (x and y). The outputs are a Cartesian representation
 of the pair given at the inputs in polar form, where
 x = magnitude * cos(angle) and y = magnitude * sin(angle).
 Note that the angle input is assumed to be in radians. If either input
 is NaN or infinity, then the outputs are NaN or infinity.

 @author Michael Leung, Edward A. Lee, Paul Whitaker
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class PolarToCartesian extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PolarToCartesian(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        magnitude = new TypedIOPort(this, "magnitude", true, false);
        magnitude.setTypeEquals(BaseType.DOUBLE);

        angle = new TypedIOPort(this, "angle", true, false);
        angle.setTypeEquals(BaseType.DOUBLE);

        x = new TypedIOPort(this, "x", false, true);
        x.setTypeEquals(BaseType.DOUBLE);

        y = new TypedIOPort(this, "y", false, true);
        y.setTypeEquals(BaseType.DOUBLE);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port for the magnitude component, which has type
     DoubleToken. */
    public TypedIOPort magnitude;

    /** The input port for the angle component (in radians), which has type
     DoubleToken. */
    public TypedIOPort angle;

    /** The output port for the x coordinate, which has type DoubleToken. */
    public TypedIOPort x;

    /** The output port for the y coordinate, which has type DoubleToken. */
    public TypedIOPort y;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume a double token from each of the two input ports (magnitude
     *  and angle) and output a double token on each of the two output ports
     *  (x and y). The output is a Cartesian representation of the coordinates
     *  given at the inputs in polar form. The angle is in radians.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        DoubleToken magnitudeValue = (DoubleToken) magnitude.get(0);
        double angleValue = ((DoubleToken) angle.get(0)).doubleValue();

        // Perform multiplication using Token methods so as to preserve units of length.
        Token xValue = magnitudeValue.multiply(new DoubleToken(Math.cos(angleValue)));
        Token yValue = magnitudeValue.multiply(new DoubleToken(Math.sin(angleValue)));

        x.send(0, xValue);
        y.send(0, yValue);
    }

    /** Return false if either of the input ports has no token, otherwise
     *  return what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!magnitude.hasToken(0) || !angle.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
