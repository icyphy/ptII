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

import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// BinaryNonStrictLogicGate

/**
 A nonstrict actor that performs an and operation on two inputs.
 <p>On each firing, produce an output token with a value with the
 value of the and if that output
 can be determined.

 @author Patricia Derler, Edward A. Lee
 @version $Id: NonStrictLogicGate.java 67784 2013-10-26 16:53:27Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (pwhitake)
 @Pt.AcceptedRating Red (pwhitake)
 */
public class NonStrictAndGate extends BinaryNonStrictLogicGate {
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
    public NonStrictAndGate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _function = _AND;
        input.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if all inputs are known.
     *  @return True if all inputs are known.
     *  @throws IllegalActionException If it fails.
     */
    protected boolean _allInputsKnown() throws IllegalActionException {
    	return input.isKnown() && input2.isKnown();
    }
    
	/** Read the inputs, and return the and function applied to
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
