/* Convert from rectangular form to polar form.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.conversions;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;


///////////////////////////////////////////////////////////////
/// RectangularToPolar

/**
This actor reads two double tokens (x and y)
and outputs two new double tokens (magnitude and angle).
The output is a polar form representation of the vector given at the
inputs in rectangular form. The angle is in radians.
<p>
The implementation uses java.lang.Math.atan2(), which gives
the following documentation:
<p>
"This method computes the phase <i>theta</i> by computing an arc tangent
 of <code>y/x</code> in the range of -<i>pi</i> to <i>pi</i>. Special
 cases:
 <ul>
 <li>If either argument is NaN, then the result is NaN.
 <li>If the first argument is positive zero and the second argument
 is positive, or the first argument is positive and finite and the
 second argument is positive infinity, then the result is positive
 zero.
 <li>If the first argument is negative zero and the second argument
 is positive, or the first argument is negative and finite and the
 second argument is positive infinity, then the result is negative zero.
 <li>If the first argument is positive zero and the second argument
 is negative, or the first argument is positive and finite and the
 second argument is negative infinity, then the result is the
 <code>double</code> value closest to pi.
 <li>If the first argument is negative zero and the second argument
 is negative, or the first argument is negative and finite and the
 second argument is negative infinity, then the result is the
 <code>double</code> value closest to -pi.
 <li>If the first argument is positive and the second argument is
 positive zero or negative zero, or the first argument is positive
 infinity and the second argument is finite, then the result is the
 <code>double</code> value closest to pi/2.
 <li>If the first argument is negative and the second argument is
 positive zero or negative zero, or the first argument is negative
 infinity and the second argument is finite, then the result is the
 <code>double</code> value closest to -pi/2.
 <li>If both arguments are positive infinity, then the result is the
 <code>double</code> value closest to pi/4.
 <li>If the first argument is positive infinity and the second argument
 is negative infinity, then the result is the <code>double</code>
 value closest to 3*pi/4.
 <li>If the first argument is negative infinity and the second argument
 is positive infinity, then the result is the <code>double</code> value
 closest to -pi/4.
 <li>If both arguments are negative infinity, then the result is the
 <code>double</code> value closest to -3*pi/4.</ul>"
 </ul>

@author Michael Leung and Edward A. Lee
@version $Id$
*/

public class RectangularToPolar extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RectangularToPolar(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        x = new TypedIOPort(this, "x", true, false);
        x.setTypeEquals(BaseType.DOUBLE);

        y = new TypedIOPort(this, "y", true, false);
        y.setTypeEquals(BaseType.DOUBLE);

        magnitude = new TypedIOPort(this, "magnitude", false, true);
        magnitude.setTypeEquals(BaseType.DOUBLE);

        angle = new TypedIOPort(this, "angle", false, true);
        angle.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The x part. This has type DoubleToken. */
    public TypedIOPort x;

    /** The y part. This has type DoubleToken. */
    public TypedIOPort y;

    /** The magnitude part. This has type DoubleToken. */
    public TypedIOPort magnitude;

    /** The angle part. This has type DoubleToken. */
    public TypedIOPort angle;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume two double tokens (x and y) from each input port,
     *  and output two new double tokens (magnitude and angle). The output is a
     *  polar form representation of the vector given at the inputs in
     *  rectangular form. The angle is in radians.
     *  If either input has no token, then do nothing.
     *
     *  @exception IllegalActionException If there is no director.
     */

    public void fire() throws IllegalActionException {
        if (x.hasToken(0) && y.hasToken(0)) {
            double xValue = ((DoubleToken) (x.get(0))).doubleValue();
            double yValue = ((DoubleToken) (y.get(0))).doubleValue();

            double magnitudeValue
                = Math.sqrt (xValue * xValue + yValue * yValue);
            double angleValue
                = Math.atan2(yValue, xValue);

            magnitude.broadcast(new DoubleToken (magnitudeValue));
            angle.broadcast(new DoubleToken (angleValue));
        }
    }
}
