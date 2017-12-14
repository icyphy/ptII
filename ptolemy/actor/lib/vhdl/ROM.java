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
import ptolemy.data.ArrayToken;
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
public class ROM extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ROM(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set values parameter
        values = new Parameter(this, "values");
        values.setExpression("{1}");

        address = new TypedIOPort(this, "address", true, false);
        address.setTypeEquals(BaseType.FIX);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The values that will be produced on the output.
     *  This parameter is an array, with default value {1}.
     */
    public Parameter values;

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

        if (attribute == values) {

            _capacity = ((ArrayToken) values.getToken()).length();

            _addressWidth = (int) Math.floor(Math.log(_capacity) / Math.log(2));

        }
    }

    /** Output the fixpoint value of the concatenation of the input bits.
     *  If there is no inputs, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (address.isKnown(0)) {
            int addressValue;

            if (!address.hasToken(0)) {
                return;
            }

            FixToken addressToken = (FixToken) address.get(0);
            FixPoint addressFixValue = addressToken.fixValue();

            _checkFixTokenWidth(addressToken, _addressWidth);

            addressValue = addressFixValue.getUnscaledValue().intValue();

            if (addressValue >= _capacity) {
                throw new IllegalActionException(this,
                        "Address is out of range.");
            }

            ArrayToken valuesArray = (ArrayToken) values.getToken();
            FixPoint value = new FixPoint(
                    ((ScalarToken) valuesArray.getElement(addressValue))
                            .intValue());
            Token result = new FixToken(value);

            sendOutput(output, 0, result);
        } else {
            output.resend(0);
        }
    }

    /** Override the base class to declare that the <i>address</i>
     *  port does not depend on the <i>output</i> in a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(address, output);
    }

    private int _addressWidth;

    private int _capacity;
}
