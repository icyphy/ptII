/* An actor that converts polar coordinates to a complex token.

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
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////////
/// PolarToComplex

/**

 This actor reads two double tokens (magnitude and angle) and outputs
 a single complex token. The output is a complex token representation of
 the coordinates given at the inputs in polar form. The complex token
 has two parts: the first part correspond to the real part, which is
 magnitude * cos(angle, and the second part is the imaginary part, which is
 magnitude * sin(angle). Note that the angle input is
 assumed to be in radians. If either input is NaN or infinity,
 then the output is NaN or infinity.

 @author Michael Leung, Edward A. Lee, Paul Whitaker
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class PolarToComplex extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PolarToComplex(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        magnitude = new TypedIOPort(this, "magnitude", true, false);
        magnitude.setTypeEquals(BaseType.DOUBLE);

        angle = new TypedIOPort(this, "angle", true, false);
        angle.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port for the magnitude component, which has type
     DoubleToken. */
    public TypedIOPort magnitude;

    /** The input port for the angle component (in radians), which has
     type DoubleToken. */
    public TypedIOPort angle;

    /** The port for the output, which has type ComplexToken. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume a double token from each input port (magnitude and angle)
     *  and output a new complex token on the output port. The output is
     *  a complex representation of the coordinates given at the inputs in
     *  polar form. The input angle is assumed to be in radians. If either
     *  input has no token, then do nothing.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        double magnitudeValue = ((DoubleToken) magnitude.get(0)).doubleValue();
        double angleValue = ((DoubleToken) angle.get(0)).doubleValue();

        double xValue = magnitudeValue * Math.cos(angleValue);
        double yValue = magnitudeValue * Math.sin(angleValue);

        output.send(0, new ComplexToken(new Complex(xValue, yValue)));
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
