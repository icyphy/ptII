/* An actor that converse real to complex

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

///////////////////////////////////////////////////////////////
/// RealToComplex
/** This actor takes in a real part and an imaginary part
    and output a complex token.

@author Michael Leung
@version $Id$
*/

public class RealToComplex extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RealToComplex(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        realInput = new TypedIOPort(this, "realInput", true, false);
        realInput.setTypeEquals(DoubleToken.class);

        imagInput = new TypedIOPort(this, "imagInput", true, false);
        imagInput.setTypeEquals(DoubleToken.class);


        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(ComplexToken.class);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public TypedIOPort realInput;
    public TypedIOPort imagInput;

    /** The output ports. */
    public TypedIOPort output;

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
            RealToComplex newobj = (RealToComplex)(super.clone(ws));
            newobj.realInput = (TypedIOPort)newobj.getPort("realInput");
            newobj.imagInput = (TypedIOPort)newobj.getPort("imagInput");
            newobj.output = (TypedIOPort)newobj.getPort("output");
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

    /** Consume the inputs and produce the outputs of the RealToComplex actor.
     *
     *  Construct an Complex variable by using the constructor for Complex.
     *  Complex comp = new Complex(realPart, imagPart);  
     *
     *  @exception IllegalActionException Not Thrown.
     */
    public void fire() throws IllegalActionException {

        DoubleToken real = (DoubleToken) (realInput.get(0)); 
        DoubleToken imag = (DoubleToken) (imagInput.get(0)); 
 
        double realPart = real.doubleValue();
        double imagPart = imag.doubleValue();

        Complex comp = new Complex(realPart, imagPart);  
        ComplexToken token = new ComplexToken (comp);
       
        output.broadcast(token);
       
    }
}



