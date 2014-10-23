/** An actor that slices the input bits and output a consecutive subset
 of the input bits.

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
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
//// Slice

/**
 Produce an output token on each firing with a FixPoint value that is
 equal to the slicing of the bits of the input token value. The bit width of
 the output token value is determined by taking the difference of parameters
 start and end. The width parameter specifies the bit width of the input
 value. The output FixPoint value is unsigned, and all its bits are integer
 bits. The input can have any scalar type.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class Slice extends FixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Slice(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.FIX);

        start = new Parameter(this, "start");
        end = new Parameter(this, "end");
        lsb = new StringParameter(this, "lsb");
        lsb.setExpression("LSB");
        lsb.addChoice("LSB");
        lsb.addChoice("MSB");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * The input port.
     */
    public TypedIOPort input;

    /**
     * The start index for the portion of the bits to be sliced.
     */
    public Parameter start;

    /**
     * The end index for the portion of the bits to be sliced.
     */
    public Parameter end;

    /**
     * Whether start and end index assumes LSB or MSB representation.
     */
    public Parameter lsb;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output a consecutive subset of the input bits.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            FixToken in = (FixToken) input.get(0);
            int widthValue = in.fixValue().getPrecision().getNumberOfBits();
            int startValue = ((IntToken) start.getToken()).intValue();
            int endValue = ((IntToken) end.getToken()).intValue() + 1;
            boolean lsbValue = ((StringToken) lsb.getToken()).stringValue()
                    .equals("LSB");

            int newStartValue = lsbValue ? widthValue - endValue : startValue;
            int newEndValue = lsbValue ? widthValue - startValue : endValue;
            int shiftBits = lsbValue ? startValue : widthValue - endValue;

            char[] mask = new char[widthValue];
            Arrays.fill(mask, '0');
            Arrays.fill(mask, newStartValue, newEndValue, '1');

            BigDecimal value = new BigDecimal(in.fixValue().getUnscaledValue()
                    .and(new BigInteger(new String(mask), 2))
                    .shiftRight(shiftBits));
            Precision precision = new Precision(
                    ((Parameter) getAttribute("outputPrecision"))
                    .getExpression());
            if (newEndValue - newStartValue != precision.getNumberOfBits()) {
                throw new IllegalActionException(this, "Bit width of "
                        + (newEndValue - newStartValue)
                        + " is not equal to precision " + precision);
            }

            Overflow overflow = Overflow
                    .getName(((Parameter) getAttribute("outputOverflow"))
                            .getExpression().toLowerCase(Locale.getDefault()));

            Rounding rounding = Rounding
                    .getName(((Parameter) getAttribute("outputRounding"))
                            .getExpression().toLowerCase(Locale.getDefault()));

            FixPoint result = new FixPoint(value, new FixPointQuantization(
                    precision, overflow, rounding));

            sendOutput(output, 0, new FixToken(result));
        }
    }
}
