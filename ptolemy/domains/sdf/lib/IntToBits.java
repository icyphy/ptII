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
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////
/// IntToBits
/**
This actor converts an IntToken into a sequence of 32 consecutive
BooleanTokens.  The most significant bit is the first boolean
token send out.  The least significant bit is the last boolean token send out.

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

        output_tokenProductionRate.setExpression("32");
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume a single IntToken on the input. Produce 32 consecutive
     *  BooleanTokens on the output port which is the bitwise
     *  representation of the input IntToken.
     *  The most significant bit is the first boolean
     *  token send out. The least significant bit is the last
     *  boolean token send out.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public final void fire() throws IllegalActionException  {
        super.fire();
        BooleanToken[] bits = new BooleanToken[32];
        IntToken token = (IntToken) (input.get(0));
        int integer = token.intValue();

        if (integer < 0) {
            bits[0] = new BooleanToken(true);
            integer = (int)(2147483648L + integer);
        } else {
            bits[0] = new BooleanToken(false);
        }


        for (int i = 31; i >= 1; i--) {
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
