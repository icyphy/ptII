/* Lempel-Ziv encoder.

Copyright (c) 2004 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// HuffmanCoder
/** 
   Lempel-Ziv encoder.

   @author Rachel Zhou
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
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Declare port types.
        input.setTypeEquals(BaseType.INT);
        _inputRate = new Parameter(input, "tokenConsumptionRate",
                       new IntToken(1));
        output.setTypeEquals(BaseType.BOOLEAN);
    }

    /** Generate the Huffman codebook for the given <i>pmf</i>, and
     *  encode the input into booleans and send them to the output port.
     */
    public void fire() throws IllegalActionException {
        Token[] inputToken = (Token[])input.get(0, 2);
        int oldPhase = ((IntToken)inputToken[0]).intValue();
        int bit = ((IntToken)inputToken[1]).intValue();
        String current = (String)_decodeBook.get(oldPhase);
        if (bit == 1) {
            current = current + "1";
        } else {
            current = current + "0";
        }
        _decodeBook.add(current);
        for (int i = 0; i < current.length(); i ++) {
            if (current.charAt(i) == '1') {
                output.send(0, new BooleanToken(true));
            } else {
                output.send(0, new BooleanToken(false));
            }
        }
            
    }
    
    public void initialize() {
        _decodeBook = new LinkedList();
        _decodeBook.add("");
    }

    ////////////////////////////////////////////////////////////
    ////                   private variables                ////
    
    //private int _previousIndex = 0;
    private LinkedList _decodeBook;
    //private String _current;
    //private boolean _firstInput;
    private Parameter _inputRate; 
}
