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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPoint;
import ptolemy.math.Precision;

///////////////////////////////////////////////////////////////////
//// Memory

/**
 Produce an output token on each firing with a FixPoint value that is
 equal to the concatenation of the input bits from each input channel.
 The ordering of channels determines the order of the concatenation; inputs
 from later channels are appended to the end. The input can have any scalar
 type.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class Memory extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Memory(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        capacity = new Parameter(this, "capacity");
        capacity.setExpression("1");

        writeEnable = new TypedIOPort(this, "writeEnable", true, false);
        writeEnable.setTypeEquals(BaseType.FIX);

        address = new TypedIOPort(this, "address", true, false);
        address.setTypeEquals(BaseType.FIX);

        dataIn = new TypedIOPort(this, "dataIn", true, false);
        dataIn.setTypeEquals(BaseType.FIX);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The range of address from 0 to capacity.
     */
    public Parameter capacity;

    /** The input port for writing data .
     */
    public TypedIOPort dataIn;

    /** The control port for signaling write.
     */
    public TypedIOPort writeEnable;

    /** The address port for fetching data.
     */
    public TypedIOPort address;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     * @exception IllegalActionException
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == capacity) {
            _capacity = ((ScalarToken) capacity.getToken()).intValue();

            _addressWidth = (int) Math.floor(Math.log(_capacity) / Math.log(2));

        } else if (attribute.getName().equals("outputPrecision")) {
            _dataWidth = new Precision(getPortPrecision(output))
                    .getNumberOfBits();
        }
    }

    /** Output the fixpoint value of the concatenation of the input bits.
     *  If there is no inputs, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (address.isKnown(0) && writeEnable.isKnown(0) && dataIn.isKnown(0)) {
            int addressValue;

            if (!address.hasToken(0) || !writeEnable.hasToken(0)
                    || !dataIn.hasToken(0)) {
                return;
            }

            // Consume tokens from all input ports.
            FixToken in = (FixToken) dataIn.get(0);

            FixToken addressToken = (FixToken) address.get(0);
            FixPoint addressFixValue = addressToken.fixValue();

            FixToken writeEnableToken = (FixToken) writeEnable.get(0);
            FixPoint writeEnableValue = writeEnableToken.fixValue();

            _checkFixTokenWidth(writeEnableToken, 1);
            _checkFixTokenWidth(addressToken, _addressWidth);
            _checkFixTokenWidth(in, _dataWidth);

            addressValue = addressFixValue.getUnscaledValue().intValue();

            if (addressValue >= _capacity) {
                throw new IllegalActionException(this,
                        "Address is out of range.");
            }

            if (writeEnableValue.toBitString().equals("1")) {
                _storage[addressValue] = in;
            }

            Token result = _storage[addressValue];
            if (result == null) {
                result = Token.NIL;
            }

            sendOutput(output, 0, result);
        } else {
            output.resend(0);
        }
    }

    /**
     *
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _storage = new FixToken[_capacity];
    }

    /** Override the base class to declare that the <i>address</i>,
     *        <i>dataIn</i> and <i>writeEnable</i> ports do not depend on
     *        the <i>output</i> in a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(address, output);
        removeDependency(dataIn, output);
        removeDependency(writeEnable, output);
    }

    private int _addressWidth;

    private int _capacity;

    private int _dataWidth;

    private FixToken[] _storage;

}
