/* An actor that converts Cartesian coordinates to a single complex token.

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
/// CartesianToComplex

/**
 Convert a Cartesian pair (represented as two double tokens) to a single
 complex token. At each firing of the actor, it will consume exactly one
 token from each of the two input ports and produce a complex token on the
 output port. The x input becomes the real output and the y input becomes the
 imaginary output. If either input port is empty, nothing is produced.

 @author Michael Leung, Jie Liu, Edward A. Lee, Paul Whitaker
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class CartesianToComplex extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CartesianToComplex(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        x = new TypedIOPort(this, "x", true, false);
        x.setTypeEquals(BaseType.DOUBLE);

        y = new TypedIOPort(this, "y", true, false);
        y.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port for the x coordinate of the Cartesian pair, which
     has type DoubleToken.
     */
    public TypedIOPort x;

    /** The input port for the y coordinate of the Cartesian pair, which
     has type DoubleToken.
     */
    public TypedIOPort y;

    /** The port for the output, which has type ComplexToken.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume exactly one token from each input port and output the
     *  converted complex token on the output port.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        double xValue = ((DoubleToken) x.get(0)).doubleValue();
        double yValue = ((DoubleToken) y.get(0)).doubleValue();
        ComplexToken token = new ComplexToken(new Complex(xValue, yValue));
        output.send(0, token);
    }

    /** Return false if either of the input ports has no token, otherwise
     *  return what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!x.hasToken(0) || !y.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }
}
