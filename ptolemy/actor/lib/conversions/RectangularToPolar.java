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

        magOutput = new TypedIOPort(this, "magOutput", false, true);
        magOutput.setTypeEquals(DoubleToken.class);

        angOutput = new TypedIOPort(this, "angOutput", false, true);
        angOutput.setTypeEquals(DoubleToken.class);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public TypedIOPort xInput;
    public TypedIOPort yInput;

    /** The output ports. */
    public TypedIOPort magOutput;
    public TypedIOPort angOutput;

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
            newobj.magOutput = (TypedIOPort)newobj.getPort("magOutput");
            newobj.angOutput = (TypedIOPort)newobj.getPort("angOutput");
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

    /** Consume the inputs and produce the outputs of the RectangularToPolar
     *  actor.
     *
     *  magnitude = square root of x*x and y*y.
     *  angle = arctan(y/x).
     *  (where x and y are the rectangular inputs and calculations of
     *   angle are in radian domain)  
     *
     *  @exception IllegalActionException Not Thrown.
     */
    public void fire() throws IllegalActionException {

        DoubleToken xValue = (DoubleToken) (xInput.get(0));    
        double x = xValue.doubleValue();
        DoubleToken yValue = (DoubleToken) (yInput.get(0));    
        double y = yValue.doubleValue();

        double mag = Math.sqrt(x*x + y*y);
        double ang = Math.atan(y/x);

        DoubleToken magnitude = new DoubleToken (mag);
        DoubleToken angle = new DoubleToken (ang);
       
        magOutput.broadcast(magnitude);
        angOutput.broadcast(angle);
    }
}



