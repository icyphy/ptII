/* An actor that converse polar to rectangular

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
/// PolarToRectangular
/** This actor takes in two double tokens radius and angle (polar)
    from two different ports and gives back two double tokens xValue 
    and yValue (rectangular) to two different ports.

@author Michael Leung
@version $Id$
*/

public class PolarToRectangular extends Transformer {

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

        magInput = new TypedIOPort(this, "magInput", true, false);
        magInput.setTypeEquals(DoubleToken.class);

        angInput = new TypedIOPort(this, "angInput", true, false);
        angInput.setTypeEquals(DoubleToken.class);

        xOutput = new TypedIOPort(this, "xOutput", false, true);
        xOutput.setTypeEquals(DoubleToken.class);

        yOutput = new TypedIOPort(this, "yOutput", false, true);
        yOutput.setTypeEquals(DoubleToken.class);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public TypedIOPort magInput;
    public TypedIOPort angInput;

    /** The output ports. */
    public TypedIOPort xOutput;
    public TypedIOPort yOutput;

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
            PolarToRectangular newobj = (PolarToRectangular)(super.clone(ws));
            newobj.magInput = (TypedIOPort)newobj.getPort("magInput");
            newobj.angInput = (TypedIOPort)newobj.getPort("angInput");
            newobj.xOutput = (TypedIOPort)newobj.getPort("xOutput");
            newobj.yOutput = (TypedIOPort)newobj.getPort("yOutput");
                return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** initialization
     */

    public void initialize() throws IllegalActionException {
        super.initialize();
    
    }  

    /** Consume the inputs and produce the outputs of the PolarToRectangular
     *  actor. Magnitude is double and Angle (in radian)is double also.
     *
     *  xValue = magnitude * cos(angle)
     *  yValue = magnitude * sin(angle)
     *  (calculations of angle are in radian domain)
     * 
     *  @exception IllegalActionException Not Thrown.
     */
    public void fire() throws IllegalActionException {

        DoubleToken mag = (DoubleToken) (magInput.get(0));    
        double  magnitude = mag.doubleValue();
        DoubleToken ang = (DoubleToken) (angInput.get(0));    
        double angle = ang.doubleValue();

        double xValue = magnitude * Math.cos(angle);
        double yValue = magnitude * Math.sin(angle);

        DoubleToken x = new DoubleToken (xValue);
        DoubleToken y = new DoubleToken (yValue);
       
        xOutput.broadcast(x);
        yOutput.broadcast(y);
    }
}



