/* An actor that converts Cartesian coordinates to polar form.

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
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
/// CartesianToPolar

/**
 <p>Convert a Cartesian pair, which is represented by two double tokens (x and y),
 to a polar form, which is also represented by two double tokens (magnitude
 and angle).  The angle is in radians.
 </p><p>
 The implementation uses java.lang.Math.atan2(double, double).</p>
 @see java.lang.Math#atan2(double, double)

 @author Michael Leung, Edward A. Lee, Paul Whitaker
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class CartesianToPolar extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CartesianToPolar(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        x = new TypedIOPort(this, "x", true, false);
        x.setTypeEquals(BaseType.DOUBLE);

        y = new TypedIOPort(this, "y", true, false);
        y.setTypeEquals(BaseType.DOUBLE);

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

    /** The x coordinate of the input pair, which has type DoubleToken. */
    public TypedIOPort x;

    /** The y coordinate of the input pair, which has type DoubleToken. */
    public TypedIOPort y;

    /** The magnitude component of the output pair, which has type
     DoubleToken. */
    public TypedIOPort magnitude;

    /** The angle component of the output pair, which has type DoubleToken. */
    public TypedIOPort angle;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume one double token from each of the two input ports (x and y),
     *  and output one new double token on each of the two output ports
     *  (magnitude and angle). The output is a polar form representation of
     *  the Cartesian pair given at the inputs. The angle is in radians.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        double xValue = ((DoubleToken) x.get(0)).doubleValue();
        double yValue = ((DoubleToken) y.get(0)).doubleValue();

        double magnitudeValue = Math.sqrt(xValue * xValue + yValue * yValue);
        double angleValue = Math.atan2(yValue, xValue);

        magnitude.send(0, new DoubleToken(magnitudeValue));
        angle.send(0, new DoubleToken(angleValue));
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
