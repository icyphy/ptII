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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.Precision;

///////////////////////////////////////////////////////////////////
//// Multiplexor

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
public class Multiplexor extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Multiplexor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        A = new TypedIOPort(this, "A", true, false);
        A.setTypeEquals(BaseType.FIX);
        B = new TypedIOPort(this, "B", true, false);
        B.setTypeEquals(BaseType.FIX);

        select = new TypedIOPort(this, "select", true, false);
        select.setTypeEquals(BaseType.FIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for the first data token stream.  This is a port of
     *  fix point type.
     */
    public TypedIOPort A;

    /** Input for the second data token stream.  This is a port of
     *  fix point type.
     */
    public TypedIOPort B;

    /** Input for select one of the inputs.  This port has int type.
     */
    public TypedIOPort select;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** output a consecutive subset of the input bits.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (select.isKnown() && A.isKnown() && B.isKnown()) {
            if (select.hasToken(0)) {
                FixToken channel = (FixToken) select.get(0);

                _checkFixTokenWidth(channel, 1);

                _channel = channel.fixValue().getUnscaledValue().intValue();
            }

            Precision outputPrecision = new Precision(
                    ((Parameter) getAttribute("outputPrecision"))
                    .getExpression());

            FixToken tokenA = null;
            FixToken tokenB = null;

            if (A.hasToken(0)) {
                tokenA = (FixToken) A.get(0);
                if (tokenA.fixValue().getPrecision().getNumberOfBits() != outputPrecision
                        .getNumberOfBits()) {

                    throw new IllegalActionException(this,
                            "Input A has different width than the output port");
                }
            }
            if (B.hasToken(0)) {
                tokenB = (FixToken) B.get(0);
                if (tokenB.fixValue().getPrecision().getNumberOfBits() != outputPrecision
                        .getNumberOfBits()) {

                    throw new IllegalActionException(this,
                            "Input B has different width than the output port");
                }
            }

            if (_channel == 0) {
                sendOutput(output, 0, tokenA);
            } else {
                sendOutput(output, 0, tokenB);
            }
        } else {
            output.resend(0);
        }
    }

    /** Initialize to the default, which is to use channel zero. */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _channel = 0;
    }

    /** Override the base class to declare that the <i>A</i>, <i>B</i>
     *  and <i>select</i> ports do not depend on the <i>output</i> in
     *  a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(A, output);
        removeDependency(B, output);
        removeDependency(select, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The most recently read select input. */
    private int _channel = 0;

}
