/* Compute the remainder after dividing the input by the divisor.

 Copyright (c) 1990-2014 The Regents of the University of California.
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

 */
package ptolemy.actor.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Remainder

/**
 <p>Compute the remainder after dividing the input by the divisor.
 The input and output data types are both double.
 This is implemented using the IEEEremainder() method of the java Math
 class, which computes the remainder as prescribed by the IEEE 754
 standard. The method documentation states:</p>
 <blockquote>
 "The remainder value is mathematically equal to f1 - f2 ? n, where n
 is the mathematical integer closest to the exact mathematical value
 of the quotient f1/f2, and if two mathematical integers are equally
 close to f1/f2, then n is the integer that is even. If the
 remainder is zero, its sign is the same as the sign of the first
 argument. Special cases:
 <ul>
 <li> If either argument is NaN, or the first argument is infinite,
 or the second argument is positive zero or negative zero,
 then the result is NaN.</li>
 <li> If the first argument is finite and the second argument is
 infinite, then the result is the same as the first argument."</li>
 </ul>
 </blockquote>

 <p>Note: The divisor parameter is available as an input port in
 the MathFunction.Modulo() method. If you need to change the divisor
 during run-time, the MathFunction actor may be the a better choice.</p>

 @author Edward A. Lee
 @version $Id$
 @see UnaryMathFunction
 @since Ptolemy II 1.0.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class Remainder extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Remainder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        divisor = new Parameter(this, "divisor", new DoubleToken(1.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The divisor for calculating the remainder.
     *  This is a double with default value 1.0.
     */
    public Parameter divisor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one input token and output the remainder after
     *  dividing the input by the divisor.
     *  If there is no input token, then no output is produced.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            double in = ((DoubleToken) input.get(0)).doubleValue();
            double divisorValue = ((DoubleToken) divisor.getToken())
                    .doubleValue();
            output.send(0,
                    new DoubleToken(Math.IEEEremainder(in, divisorValue)));
        }
    }
}
