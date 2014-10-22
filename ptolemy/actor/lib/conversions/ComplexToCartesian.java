/* An actor that converts a complex token to Cartesian coordinates.

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
/// ComplexToCartesian

/**
 Read a complex token and output double tokens that represent the real and
 imaginary parts to two different output ports.

 @author Michael Leung, Edward A. Lee, Paul Whitaker
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class ComplexToCartesian extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ComplexToCartesian(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.COMPLEX);

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

    /** The port for the input, which has type ComplexToken. */
    public TypedIOPort input;

    /** The output port for x coordinate, which has type DoubleToken. */
    public TypedIOPort x;

    /** The output port for the imaginary coordinate, which has type
     DoubleToken. */
    public TypedIOPort y;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume a complex token from the input port and output a new double
     *  token on each of the two output ports (the real and imaginary parts
     *  of the input complex token).
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Complex complexNumber = ((ComplexToken) input.get(0)).complexValue();

        x.send(0, new DoubleToken(complexNumber.real));
        y.send(0, new DoubleToken(complexNumber.imag));
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
