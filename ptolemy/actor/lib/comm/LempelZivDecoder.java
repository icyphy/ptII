/* Lempel-Ziv decoder.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.comm;

import java.util.LinkedList;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Lempel-Ziv decoder.

/**
 Lempel-Ziv decoder.

 @see LempelZivCoder
 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class LempelZivDecoder extends Transformer {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LempelZivDecoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Declare port types.
        input.setTypeEquals(BaseType.INT);
        new Parameter(input, "tokenConsumptionRate", new IntToken(2));
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    /** Decode the Lempel-Ziv code while generating the decode book.
     *  The decode book should be same as the code book of the
     *  corresponding Lempel-Ziv encoder.
     *  @exception IllegalActionException if the input is not a decodable
     *  Lempel-Ziv code.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Token[] inputToken = input.get(0, 2);
        int oldPhase = ((IntToken) inputToken[0]).intValue();
        int bit = ((IntToken) inputToken[1]).intValue();
        String current = (String) _decodeBook.get(oldPhase);

        if (bit == 0) {
            current = current + "0";
        } else if (bit == 1) {
            current = current + "1";
        } else {
            throw new IllegalActionException(this,
                    "This is not a valid Lempel-Ziv code.");
        }

        _decodeBook.add(current);

        for (int i = 0; i < current.length(); i++) {
            if (current.charAt(i) == '0') {
                output.send(0, new BooleanToken(false));
            } else {
                output.send(0, new BooleanToken(true));
            }
        }
    }

    /** initialize the actor by creating a decode book that only
     *  contains one empty string "".
     *  @exception IllegalActionException If thrown by a super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _decodeBook = new LinkedList();
        _decodeBook.add("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The Lempel-Ziv decode book.
    private LinkedList _decodeBook;
}
