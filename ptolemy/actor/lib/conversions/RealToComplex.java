/* Convert two real tokens into a single complex token.

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
import ptolemy.domains.sdf.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////
/// RealToComplex
/** 
This actor converts a pair of real tokens to a single complex token.
At each firing of the actor, it will consume at exactly one token
from each input port and convert them in to 
a complex token. If either port is empty, nothing is produced.

@author Michael Leung, Jie Liu, Edward A. Lee
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

        real = new TypedIOPort(this, "real", true, false);
        real.setTypeEquals(BaseType.DOUBLE);

        imag = new TypedIOPort(this, "imag", true, false);
        imag.setTypeEquals(BaseType.DOUBLE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The real part input. This has type DoubleToken. 
     */
    public TypedIOPort real;

    /** The imaginary part input. This has type DoubleToken. 
     */
    public TypedIOPort imag;

    /** The output port. This has type ComplexToken. 
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
        newobj.real = (TypedIOPort)newobj.getPort("real");
        newobj.imag = (TypedIOPort)newobj.getPort("imag");
        newobj.output = (TypedIOPort)newobj.getPort("output");
        return newobj;
    }

    /** Consume exactly one token from each input port and convert them to 
     *  a complex token. If either input port is empty, do nothing.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (real.hasToken(0) && imag.hasToken(0)) {
            double realValue = ((DoubleToken)real.get(0)).doubleValue();
            double imagValue = ((DoubleToken)imag.get(0)).doubleValue();
            ComplexToken token
                    = new ComplexToken (new Complex(realValue, imagValue));
            output.broadcast(token);
        }
    }
}
