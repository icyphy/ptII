/** An actor that outputs the fixpoint value of the concatenation of
 the input bits.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.vhdl;

import java.math.BigInteger;
import java.util.Locale;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPoint;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Rounding;

///////////////////////////////////////////////////////////////////
//// LogicFunction

/**
 Produce an output token on each firing with a FixPoint value that is
 equal to the sum of all the inputs at the plus port minus the inputs at the
 minus port.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class LogicFunction extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LogicFunction(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        A = new TypedIOPort(this, "A", true, false);
        A.setTypeEquals(BaseType.FIX);

        B = new TypedIOPort(this, "B", true, false);
        B.setTypeEquals(BaseType.FIX);

        operation = new StringParameter(this, "operation");
        operation.setExpression("AND");
        operation.addChoice("AND");
        operation.addChoice("OR");
        operation.addChoice("NAND");
        operation.addChoice("NOR");
        operation.addChoice("XOR");
        operation.addChoice("XNOR");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens to be added.  This is a multiport of fix point
     *  type
     */
    public TypedIOPort A;

    /** Input for tokens to be subtracted.  This is a multiport of fix
     *  point type.
     */
    public TypedIOPort B;

    /** Indicate whether addition or subtraction needs to be performed.
     */
    public Parameter operation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output the fixpoint value of the sum of the input bits.
     *  If there is no inputs, then produce null.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (A.isKnown() && B.isKnown()) {
            BigInteger intResult = null;
            Precision precision = new Precision(
                    ((Parameter) getAttribute("outputPrecision"))
                    .getExpression());
            if (A.hasToken(0) && B.hasToken(0)) {
                FixPoint valueA = ((FixToken) A.get(0)).fixValue();
                FixPoint valueB = ((FixToken) B.get(0)).fixValue();
                if (valueA.getPrecision().getNumberOfBits() != precision
                        .getNumberOfBits()) {
                    throw new IllegalActionException(this,
                            "Input A has different width than the output port");
                }
                if (valueB.getPrecision().getNumberOfBits() != precision
                        .getNumberOfBits()) {
                    throw new IllegalActionException(this,
                            "Input B has different width than the output port");
                }
                BigInteger bigIntA = valueA.getUnscaledValue();
                BigInteger bigIntB = valueB.getUnscaledValue();
                if (operation.getExpression().equals("AND")) {
                    intResult = bigIntA.and(bigIntB);
                } else if (operation.getExpression().equals("OR")) {
                    intResult = bigIntA.or(bigIntB);
                } else if (operation.getExpression().equals("NAND")) {
                    intResult = bigIntA.and(bigIntB).not();
                } else if (operation.getExpression().equals("NOR")) {
                    intResult = bigIntA.or(bigIntB).not();
                } else if (operation.getExpression().equals("XOR")) {
                    intResult = bigIntA.xor(bigIntB);
                } else if (operation.getExpression().equals("XNOR")) {
                    intResult = bigIntA.xor(bigIntB).not();
                }
            }

            if (intResult != null) {
                Overflow overflow = Overflow
                        .getName(((Parameter) getAttribute("outputOverflow"))
                                .getExpression().toLowerCase(
                                        Locale.getDefault()));

                Rounding rounding = Rounding
                        .getName(((Parameter) getAttribute("outputRounding"))
                                .getExpression().toLowerCase(
                                        Locale.getDefault()));
                FixPoint result = new FixPoint(intResult.doubleValue(),
                        new FixPointQuantization(precision, overflow, rounding));
                sendOutput(output, 0, new FixToken(result));
            }
        } else {
            output.resend(0);
        }
    }

    /** Override the base class to declare that the <i>A</i> and
     *  <i>B</i> ports do not depend on the <i>output</i> in a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(A, output);
        removeDependency(B, output);
    }
}
