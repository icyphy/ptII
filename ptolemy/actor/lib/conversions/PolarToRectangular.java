/* An actor that converts numbers from polar form to rectangular form.

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
/// PolarToRectangular
/** This actor takes in two double tokens (magnitude and angle)
    one from each input port and output two new double tokens (xValue
    and yValue). The output is a rectangular form representation of the vector
    given at the inputs in polar form. The angle is in radians. 

@author Michael Leung
@version $Id$
*/

public class PolarToRectangular extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PolarToRectangular(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        magnitudeInput = new TypedIOPort(this, "magnitudeInput", true, false);
        magnitudeInput.setTypeEquals(BaseType.DOUBLE);

        angleInput = new TypedIOPort(this, "angleInput", true, false);
        angleInput.setTypeEquals(BaseType.DOUBLE);

        xOutput = new TypedIOPort(this, "xOutput", false, true);
        xOutput.setTypeEquals(BaseType.DOUBLE);

        yOutput = new TypedIOPort(this, "yOutput", false, true);
        yOutput.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The magnitude part. This has type DoubleToken. */
    public TypedIOPort magnitudeInput;
    /** The angle part. This has type DoubleToken. Angle in radian */
    public TypedIOPort angleInput;

    /** The xValue part . This has type DoubleToken. */
    public TypedIOPort xOutput;
    /** The yValue part. This has type DoubleToken. */
    public TypedIOPort yOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        PolarToRectangular newobj = (PolarToRectangular)(super.clone(ws));
        newobj.magnitudeInput =
            (TypedIOPort)newobj.getPort("magnitudeInput");
        newobj.angleInput = (TypedIOPort)newobj.getPort("angleInput");
        newobj.xOutput = (TypedIOPort)newobj.getPort("xOutput");
        newobj.yOutput = (TypedIOPort)newobj.getPort("yOutput");
        return newobj;
    }

    /** Consume two double token (magnitude and angle) from each
     *  input port and output two new double token (xValue and yValue).
     *  The output is a rectangular form representation of the vector given
     *  at the inputs in polar form. The angle is in radians.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {

        DoubleToken magnitudeValue = (DoubleToken) (magnitudeInput.get(0));
        double  magnitude = magnitudeValue.doubleValue();
        DoubleToken angleValue = (DoubleToken) (angleInput.get(0));
        double angle = angleValue.doubleValue();

        double xValue = magnitude * Math.cos(angle);
        double yValue = magnitude * Math.sin(angle);

        DoubleToken x = new DoubleToken (xValue);
        DoubleToken y = new DoubleToken (yValue);

        xOutput.broadcast(x);
        yOutput.broadcast(y);
    }
}




