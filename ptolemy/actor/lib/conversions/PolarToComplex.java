/* Convert from polar form to a complex token.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;


///////////////////////////////////////////////////////////////
/// PolarToComplex
/**

This actor reads two double tokens (magnitude and angle)
and outputs two new double tokens (x and y).
The output is a complex token representation of the vector
given at the inputs in polar form. The angle input is
assumed to be in radians. If either input is NaN or infinity,
then the output is NaN or infinity.

@author Michael Leung and Edward A. Lee
@version $Id$
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
    public PolarToComplex(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        magnitude = new TypedIOPort(this, "magnitude", true, false);
        magnitude.setTypeEquals(BaseType.DOUBLE);

        angle = new TypedIOPort(this, "angle", true, false);
        angle.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The magnitude part. This has type DoubleToken. */
    public TypedIOPort magnitude;

    /** The angle part. This has type DoubleToken. Angle in radian */
    public TypedIOPort angle;

    /** The complex output. This has type ComplexToken. */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        PolarToComplex newobj = (PolarToComplex)(super.clone(ws));
        newobj.magnitude = (TypedIOPort)newobj.getPort("magnitude");
        newobj.angle = (TypedIOPort)newobj.getPort("angle");
        newobj.output = (TypedIOPort)newobj.getPort("output");
        return newobj;
    }

    /** Consume two double token (magnitude and angle) from each
     *  input port and output a new complex token.
     *  The output is a complex representation of the vector given
     *  at the inputs in polar form. The input angle is assumed to be
     *  in radians. If either input has no token, then do nothing.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (magnitude.hasToken(0) && angle.hasToken(0)) {
            double magnitudeValue
                    = ((DoubleToken)(magnitude.get(0))).doubleValue();
            double angleValue
                    = ((DoubleToken) (angle.get(0))).doubleValue();

            double xValue = magnitudeValue * Math.cos(angleValue);
            double yValue = magnitudeValue * Math.sin(angleValue);

            output.broadcast(new ComplexToken (new Complex(xValue, yValue)));
        }
    }
}
