/* An actor that converts two real tokens into a complex token.

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
import ptolemy.domains.sdf.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////
/// RealToComplex
/** 
An actor that converts a pair of real tokens to a complex token.
At each firing of the actor, it will consume at exactly one token
from each input port, if there is any, and convert them in to 
a complex token. If any of the port is empty, a zero will be used
in the corresponding part. 

@author Michael Leung, Jie Liu
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
        realInput.setTypeEquals(BaseType.DOUBLE);

        imagInput = new TypedIOPort(this, "imagInput", true, false);
        imagInput.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The real part. This has type DoubleToken. 
     */
    public TypedIOPort realInput;

    /** The imaginary part. This has type DoubleToken. 
     */
    public TypedIOPort imagInput;

    /** The output ports. This has type ComplexToken. 
     */
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
        RealToComplex newobj = (RealToComplex)(super.clone(ws));
        newobj.realInput = (TypedIOPort)newobj.getPort("realInput");
        newobj.imagInput = (TypedIOPort)newobj.getPort("imagInput");
        newobj.output = (TypedIOPort)newobj.getPort("output");
        return newobj;
    }

    /** Consume at exactly one token from each input port, 
     *  if there is any, and convert them in to 
     *  a complex token. If any of the port is empty, a zero will be used
     *  in the corresponding part. 
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        double real = 0.0;
        double imag = 0.0;
        if (realInput.hasToken(0)) {
            real = ((DoubleToken)realInput.get(0)).doubleValue();
        }
        if (imagInput.hasToken(0)) {
            imag = ((DoubleToken)imagInput.get(0)).doubleValue();
        }
        
        Complex complexNumber = new Complex(real, imag);
        ComplexToken token = new ComplexToken (complexNumber);

        output.broadcast(token);
    }
}
