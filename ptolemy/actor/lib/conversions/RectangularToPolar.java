/* An actor that converts numbers from rectangular form to polar form.

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

@ProposedRating Red (mikele@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.conversions;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;


///////////////////////////////////////////////////////////////
/// RectangularToPolar
/** This actor takes in two double tokens (xValue and yValue)
 *  from each input port, and outputs two new double tokens (magnitude and
    angle) to two different ports.
    The output is a polar form representation of the vector given at the
    inputs in rectangular form. The angle is in radians.

@author Michael Leung
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
    public RectangularToPolar(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        xInput = new TypedIOPort(this, "xInput", true, false);
        xInput.setTypeEquals(BaseType.DOUBLE);

        yInput = new TypedIOPort(this, "yInput", true, false);
        yInput.setTypeEquals(BaseType.DOUBLE);

        magnitudeOutput =
            new TypedIOPort(this, "magnitudeOutput", false, true);
        magnitudeOutput.setTypeEquals(BaseType.DOUBLE);

        angleOutput = new TypedIOPort(this, "angleOutput", false, true);
        angleOutput.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The xValue part. This has type DoubleToken. */
    public TypedIOPort xInput;
    /** The xValue part. This has type DoubleToken. */
    public TypedIOPort yInput;

    /** The magnitude part. This has type DoubleToken. */
    public TypedIOPort magnitudeOutput;
    /** The angle part. This has type DoubleToken. */
    public TypedIOPort angleOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        RectangularToPolar newobj = (RectangularToPolar)(super.clone(ws));
        newobj.xInput = (TypedIOPort)newobj.getPort("xInput");
        newobj.yInput = (TypedIOPort)newobj.getPort("yInput");
        newobj.magnitudeOutput =
            (TypedIOPort)newobj.getPort("magnitudeOutput");
        newobj.angleOutput = (TypedIOPort)newobj.getPort("angleOutput");
        return newobj;
    }

    /** Consume two double tokens (xValue and yvalue) from each input port,
     *  and output two new double tokens (magnitude and angle). The output is a
     *  polar form representation of the vector given at the inputs in
     *  rectangular form. The angle is in radians.
     *
     *  @exception IllegalActionException If there is no director.
     */

    public void fire() throws IllegalActionException {

        DoubleToken xValue = (DoubleToken) (xInput.get(0));
        double x = xValue.doubleValue();
        DoubleToken yValue = (DoubleToken) (yInput.get(0));
        double y = yValue.doubleValue();

        double magnitudeValue = Math.sqrt (x * x + y * y);
        double angleValue = Math.atan(y / x);

        DoubleToken magnitude = new DoubleToken (magnitudeValue);
        DoubleToken angle = new DoubleToken (angleValue);

        magnitudeOutput.broadcast(magnitude);
        angleOutput.broadcast(angle);
    }
}
