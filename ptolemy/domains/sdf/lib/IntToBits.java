/* An actor that converts an IntToken to 32 consecutive BooleanTokens.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////
/// IntToBits
/**
This actor converts an IntToken into a sequence of Boolean tokens. 
The number of Boolean tokens is specified by the <i>numberOfBits</i>
parameter. It should be a positive integer not bigger than 32.
The most significant bit (the sign bit) is the first boolean
token send out. It is "false" if the input integer is non-negative,
otherwise it is "true". The least significant bit is the last boolean
token send out.

Let <i>k</i> denotes the value of the <i>numberOfBits</i> parameter.
An exception is thrown if the input integer is smaller than
-2<sup><i>k</i></sup> or greater 2<sup><i>k</i></sup> - 1.

@author Michael Leung
@version $Id$
@since Ptolemy II 0.4
*/

public class IntToBits extends SDFConverter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public IntToBits(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        input.setTypeEquals(BaseType.INT);
        
        numberOfBits = new Parameter(this, "numberOfBits");
        numberOfBits.setExpression("32");
 
        output_tokenProductionRate.setExpression("numberOfBits");
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of Boolean tokens that the input integer is coverted to.
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

    /** Consume a single IntToken on the input. Produce <i>numberOfBits</i>
     *  BooleanTokens on the output port which is the bitwise
     *  representation of the input IntToken.
     *  The most significant bit (the sign bit) is the first boolean
     *  token send out. The least significant bit is the last
     *  boolean token send out.
     *
     *  @exception IllegalActionException If there is no director.
     *  or if the input integer is out of range.
     */
    public final void fire() throws IllegalActionException  {
        super.fire();
        int rate = ((IntToken)numberOfBits.getToken()).intValue();
        BooleanToken[] bits = new BooleanToken[rate];
        IntToken token = (IntToken) (input.get(0));
        int integer = token.intValue();

        if (integer < 0) {
            if (integer < - (1 << (rate - 1)))
                throw new IllegalActionException(this,
                   "integer is out of range.");
            bits[0] = new BooleanToken(true);
            //integer = (int)(2147483648L + integer);
            integer = (int)((1 << (rate - 1)) + integer);
        } else {
            if (integer > (1 << (rate - 1)) - 1 )
                throw new IllegalActionException(this,
                    "integer is out of range.");
            bits[0] = new BooleanToken(false);
        }

        for (int i = rate - 1; i > 0; i--) {
            int remainder = integer % 2;
            integer = integer / 2;
            if (remainder == 0)
                bits[i] = new BooleanToken(false);
            else
                bits[i] = new BooleanToken(true);
        }

        output.send(0, bits, bits.length);
    }
}
