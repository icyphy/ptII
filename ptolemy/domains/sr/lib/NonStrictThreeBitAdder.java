/* A nonstrict three-bit adder.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// NonStrictThreeBitAdder
/**
A nonstrict three-bit adder.  This adder has one input port, which is a
multiport, and two output ports, which are single ports.  All of the ports
are of type int, and inputs are outputs are single bits.  An exception is
thrown if a number other than 0 or 1 is received as an input, or if there
are not exactly three three channels connected to the input port.  This actor
adds the three input bits, and outputs the result to the lowBit and highBit
ports.  Only two of the inputs must be known for highBit to be determined.
All inputs are necessary for lowBit to be determined.  An absence of a token
is considered to have no contribution to the sum (same as value the zero).
If no input tokens are available at all, then no output is produced.

@author Paul Whitaker
@version $Id$
@since Ptolemy II 2.0
*/

public class NonStrictThreeBitAdder extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public NonStrictThreeBitAdder(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Attribute(this, "_nonStrictMarker");
        inputBits = new TypedIOPort(this, "inputBits", true, false);
        inputBits.setMultiport(true);
        highBit = new TypedIOPort(this, "highBit", false, true);
        lowBit = new TypedIOPort(this, "lowBit", false, true);
        inputBits.setTypeEquals(BaseType.INT);
        highBit.setTypeEquals(BaseType.INT);
        lowBit.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Inputs for bits to be added.  The types are integer, and only
     *  ones and zeros are expected.
     */
    public TypedIOPort inputBits;

    /** Output for the high bit.  The type is integer, and only ones and
     *  zeros will be sent.
     */
    public TypedIOPort highBit;

    /** Output for the low bit.  The type is integer, and only ones and
     *  zeros will be sent.
     */
    public TypedIOPort lowBit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the bits from the input port and output the high bit and low
     *  bit if they can be determined.
     *  @exception IllegalActionException If there is no director,
     *   if there are not exactly three channels connected to the input,
     *   or if invalid inputs are received.
     */
    public void fire() throws IllegalActionException {
        int numKnown = 0;
        int numOnes = 0;

        int width = inputBits.getWidth();
        if (width != 3) throw new IllegalActionException(this,
                "inputBits must have exactly 3 connected channels.");

        for (int i = 0; i < width; i++) {
            if (inputBits.isKnown(i)) {
                numKnown++;
                if (inputBits.hasToken(i)) {
                    int value = ((IntToken)inputBits.get(i)).intValue();
                    if ((value != 0) && (value != 1))
                        throw new IllegalActionException(this,
                                "Inputs can only be 0 or 1.");
                    if (value == 1) numOnes++;
                }
            }
        }

        IntToken high = null;
        IntToken low = null;

        if (numKnown == 3) {
            if ((numOnes == 0) || (numOnes == 2)) {
                low = new IntToken(0);
            } else {
                low = new IntToken(1);
            }
        }

        // numZeros = numKnown - numOnes
        if (numKnown - numOnes >= 2) high = new IntToken(0);
        if (numOnes >= 2) high = new IntToken(1);

        if (high != null) highBit.send(0, high);
        if (low != null) lowBit.send(0, low);
    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        removeDependency(inputBits, highBit);
        removeDependency(inputBits, lowBit);
    }
}






