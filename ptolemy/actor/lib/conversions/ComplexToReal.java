/* An actor that converts a complex token into two real tokens.

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
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////
/// ComplexToReal
/** This actor takes in a complex token and outputs the real and the imaginary
    part to two different ports.

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
        input.setTypeEquals(BaseType.COMPLEX);

        realOutput = new TypedIOPort(this, "realOutput", false, true);
        realOutput.setTypeEquals(BaseType.DOUBLE);

        imagOutput = new TypedIOPort(this, "imagOutput", false, true);
        imagOutput.setTypeEquals(BaseType.DOUBLE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. This has type ComplexToken. */
    public TypedIOPort input;

    /** The real part. This has type DoubleToken. */
    public TypedIOPort realOutput;
    /** The imaginary part. This has type DoubleToken. */
    public TypedIOPort imagOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        ComplexToReal newobj = (ComplexToReal)(super.clone(ws));
        newobj.input = (TypedIOPort)newobj.getPort("input");
        newobj.realOutput = (TypedIOPort)newobj.getPort("realOutput");
        newobj.imagOutput = (TypedIOPort)newobj.getPort("imagOutput");
        return newobj;
     }

    /** Consume a complex token from input port and
     *  output two new double tokens (the real and imaginary parts
     *  of the input complex token).
     *
     *  @exception IllegalActionException If there is no director.
     */

    public final void fire() throws IllegalActionException  {

        ComplexToken complex = (ComplexToken) (input.get(0));
        Complex complexNumber = complex.complexValue();

        DoubleToken realPart = new DoubleToken (complexNumber.real);
        DoubleToken imagPart = new DoubleToken (complexNumber.imag);

        realOutput.send(0, realPart);
        imagOutput.send(0, imagPart);
    }
}
