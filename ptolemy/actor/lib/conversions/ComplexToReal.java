/* An actor that converse the complex to its real and imaginary part

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
/// ComplexToReal
/** This actor takes in a complex token and output the real and the imaginary
    part of the complex token to two different port.

@author Michael Leung
@version $Id$
*/

public class ComplexToReal extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ComplexToReal(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(ComplexToken.class);

        realOutput = new TypedIOPort(this, "realOutput", false, true);
        realOutput.setTypeEquals(DoubleToken.class);

        imagOutput = new TypedIOPort(this, "imagOutput", false, true);
        imagOutput.setTypeEquals(DoubleToken.class);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public TypedIOPort input;

    /** The output ports. */
    public TypedIOPort realOutput;
    public TypedIOPort imagOutput;

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
            ComplexToReal newobj = (ComplexToReal)(super.clone(ws));
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.realOutput = (TypedIOPort)newobj.getPort("realOutput");
            newobj.imagOutput = (TypedIOPort)newobj.getPort("imagOutput");
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

    /** Consume the inputs and produce the outputs of the ComplexToReal actor.
     *  
     *  realPart = real part of the complex token.
     *  imagPart = imaginary part of the complex token.
     *
     *  @exception IllegalActionException Not Thrown.
     */
    public void fire() throws IllegalActionException {

        ComplexToken complex = (ComplexToken) (input.get(0));    
        Complex comp = complex.complexValue();

        DoubleToken realPart = new DoubleToken (comp.real);
        DoubleToken imagPart = new DoubleToken (comp.imag);
       
        realOutput.broadcast(realPart);
        imagOutput.broadcast(imagPart);
    }
}



