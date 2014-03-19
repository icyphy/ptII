/* A nonstrict actor that performs a specified logic operation on the input.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.domains.sr.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BinaryNonStrictLogicGate

/**
 A nonstrict actor that performs a specified logic operation on two inputs.
 <p>On each firing, produce an output token with a value that is
 equal to the specified logic operator of the inputs if that output
 can be determined.
 The functions are:
 <ul>
 <li> <b>and</b>: The logical and operator.
 This is the default function for this actor.
 <li> <b>or</b>: The logical or operator.
 <li> <b>xor</b>: The logical xor operator.
 <li> <b>nand</b>: The logical nand operator.
 Equivalent to the negation of <i>and</i>.
 <li> <b>nor</b>: The logical nor operator.
 Equivalent to the negation of <i>or</i>.
 <li> <b>xnor</b>: The logical xnor operator.
 Equivalent to the negation of <i>xor</i>.
 </ul>
 <p>
 This actor is nonstrict.  That is, it does not require that each input
 have a token upon firing.  If the output can be determined from the
 known inputs, the output will be produced.  If the output can not be
 determined in the given firing, no output will be produced.  If all of the
 inputs are known and absent, the output will be made known and absent.
 At most one token is consumed on each input channel.

 @author Edward A. Lee
 @version $Id: NonStrictLogicGate.java 67784 2013-10-26 16:53:27Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (pwhitake)
 @Pt.AcceptedRating Red (pwhitake)
 */
public class BinaryNonStrictLogicGate extends NonStrictLogicGate {
    /** Construct an actor with the given container and name.  Set the
     *  logic function to the default ("and").  Set the types of the ports
     *  to boolean.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BinaryNonStrictLogicGate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        input.setMultiport(false);
        
        input2 = new TypedIOPort(this, "input2", true, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports                             ////
    
    /** The second input port. */
    public TypedIOPort input2;

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if all inputs are known.
     *  @return True if all inputs are known.
     *  @throws IllegalActionException If it fails.
     */
    protected boolean _allInputsKnown() throws IllegalActionException {
    	return input.isKnown() && input2.isKnown();
    }
    
	/** Read the inputs, and return the logic function applied to
	 *  all the are known and present.
	 *  @return The logic function applied to all available inputs.
	 *  @throws IllegalActionException If reading inputs fails.
	 */
	protected BooleanToken _readInputs()
			throws IllegalActionException {
		BooleanToken value = null;
		if (input.isKnown(0)) {
			if (input.hasToken(0)) {
				BooleanToken in = (BooleanToken) input.get(0);
				if (in != null) {
					value = _updateFunction(in, value);
				}
            }
        }
		if (input2.isKnown(0)) {
			if (input2.hasToken(0)) {
				BooleanToken in = (BooleanToken) input2.get(0);
				if (in != null) {
					value = _updateFunction(in, value);
				}
            }
        }
		return value;
	}
}
