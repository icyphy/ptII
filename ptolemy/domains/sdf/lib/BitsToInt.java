/* An actor that converts 32 consecutive BooleanTokens to an IntToken

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////
/// BitsToInt
/**
This actor converts a sequence of BooleanTokens into a single IntToken.
The number of Boolean tokens is specified by the <i>numberOfBits</i>
parameter and should be a positive integer not larger than 32. Let <i>k</i>
denotes the value of the <i>numberOfBits</i> parameter. The output
integer is ranged from -2<sup><i>k</i></sup> to 2<sup><i>k</i></sup> - 1.

The first boolean token received indicates the sign of the integer. If
it is "false", the output integer is a non-negative number. If it is "true",
the output integer is a negative number. The least significant bit is
the last boolean token received.

@author Michael Leung
@version $Id$
@since Ptolemy II 0.4
*/

public class BitsToInt extends SDFConverter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BitsToInt(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);
        
        numberOfBits = new Parameter(this, "numberOfBits");
        numberOfBits.setExpression("32");

        input_tokenConsumptionRate.setExpression("numberOfBits");

        input.setTypeEquals(BaseType.BOOLEAN);

        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of bits that is converted to the output integer.
     *  It should be a positive integer no more than 32.
     */
    public Parameter numberOfBits;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>numberOfBits</i> parameter, then
     *  set the production rate of the output port.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameter is out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numberOfBits) {
            int rate = ((IntToken)numberOfBits.getToken()).intValue();
            if (rate < 1 || rate > 32) {
                throw new IllegalActionException(this,
                        "Invalid number of bits: " + rate);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume <i>numberOfBits</i> BooleanTokens on the input.
     *  Output a single IntToken which is representing by the
     *  BooleanTokens.
     *  The first token consumed is the most significant bit (The sign bit).
     *  The last token consumed is the least significant bit
     *  @exception IllegalActionException If there is no director.
     */
    public final void fire() throws IllegalActionException  {
        super.fire();
        int rate = ((IntToken)numberOfBits.getToken()).intValue();
        Token[] bits = new BooleanToken[rate];
        bits = input.get(0, rate);

        int integer = 0;
        for (int i = 1; i < rate; i++) {
            integer = integer << 1;
            if (((BooleanToken)bits[i]).booleanValue())
                integer += 1;
        }
        if (((BooleanToken)bits[0]).booleanValue()) {
            //convert integer to negative value.
            integer = integer - (1 << (rate - 1));
        }

        IntToken value = new IntToken(integer);
        output.send(0, value);
    }
}
