/* An actor that converse rectangular to polar

 Copyright (c) 1998-1999 The Regents of the University of California.
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
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.actor.lib.*;
import ptolemy.math.Complex;
import java.lang.Math.*;

///////////////////////////////////////////////////////////////
/// RectangularToPolar
/** This actor takes in two double tokens xValue and yValue (rectangular)
 *  from two different ports and gives back two double tokens radius and
    angle (polar) to two different ports.

@author Michael Leung
@version $Id$
*/

public class RectangularToPolar extends Transformer {

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
        xInput.setTypeEquals(DoubleToken.class);

        yInput = new TypedIOPort(this, "yInput", true, false);
        yInput.setTypeEquals(DoubleToken.class);

        magnitudeOutput = new TypedIOPort(this, "magnitudeOutput", false, true);
        magnitudeOutput.setTypeEquals(DoubleToken.class);

        angleOutput = new TypedIOPort(this, "angleOutput", false, true);
        angleOutput.setTypeEquals(DoubleToken.class);

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
    public Object clone(Workspace ws) {
        try {
            RectangularToPolar newobj = (RectangularToPolar)(super.clone(ws));
            newobj.xInput = (TypedIOPort)newobj.getPort("xInput");
            newobj.yInput = (TypedIOPort)newobj.getPort("yInput");
            newobj.magnitudeOutput = (TypedIOPort)newobj.getPort("magnitudeOutput");
            newobj.angleOutput = (TypedIOPort)newobj.getPort("angleOutput");
                return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Consume two double token representing the rectangular coordinate
     *  from two separate input ports and output two new double tokens
     *  of polar representation of the input tokens to two separate output
     *  ports.
     *
     *  @exception IllegalActionException will be thrown if attempt to
     *  fire this actor when there is no director.
     */

    public void fire() throws IllegalActionException {

        DoubleToken xValue = (DoubleToken) (xInput.get(0));
        double x = xValue.doubleValue();
        DoubleToken yValue = (DoubleToken) (yInput.get(0));
        double y = yValue.doubleValue();

        double magnitudeValue = Math.sqrt(x*x + y*y);
        double angleValue = Math.atan(y/x);

        DoubleToken magnitude = new DoubleToken (magnitudeValue);
        DoubleToken angle = new DoubleToken (angleValue);

        magnitudeOutput.broadcast(magnitude);
        angleOutput.broadcast(angle);
    }
}



