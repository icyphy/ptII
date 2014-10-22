/* An actor that converts a complex token to polar coordinates.

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
/// ComplexToPolar

/**
 <p>Convert a complex token to polar coordinates, which are represented by two
 double tokens (magnitude and angle).  The output angle is in radians.</p>
 <p>
 The implementation uses java.lang.Math.atan2(double, double).
 </p>

 @see java.lang.Math#atan2(double, double)

 @author Michael Leung, Edward A. Lee, and Paul Whitaker
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (cxh)
 */
public class ComplexToPolar extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ComplexToPolar(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.COMPLEX);

        magnitude = new TypedIOPort(this, "magnitude", false, true);
        magnitude.setTypeEquals(BaseType.DOUBLE);

        angle = new TypedIOPort(this, "angle", false, true);
        angle.setTypeEquals(BaseType.DOUBLE);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The port for the input, which has type ComplexToken. */
    public TypedIOPort input;

    /** The output port for the magnitude component, which has type
     DoubleToken. */
    public TypedIOPort magnitude;

    /** The output port for the angle component, which has type DoubleToken. */
    public TypedIOPort angle;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume one complex token on the input port and output a new double
     *  token on each of the two output ports (magnitude and angle). The
     *  outputs are a polar form representation of the complex input. The
     *  output angle is in radians.
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Complex inputValue = ((ComplexToken) input.get(0)).complexValue();

        double magnitudeValue = Math.sqrt(inputValue.real * inputValue.real
                + inputValue.imag * inputValue.imag);

        double angleValue = Math.atan2(inputValue.imag, inputValue.real);

        magnitude.send(0, new DoubleToken(magnitudeValue));
        angle.send(0, new DoubleToken(angleValue));
    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
