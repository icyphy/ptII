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
public class LempelZivCoder extends Transformer {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LempelZivCoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Declare port types.
        input.setTypeEquals(BaseType.BOOLEAN);
        output.setTypeEquals(BaseType.INT);
    }

    /** Generate the Huffman codebook for the given <i>pmf</i>, and
     *  encode the input into booleans and send them to the output port.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            boolean inputValue 
                = ((BooleanToken)input.get(0)).booleanValue();
            _current = _current + (inputValue ? "1" : "0");
            //System.out.println(_current);
            int index = _codeBook.indexOf(_current);
            if (index == -1 || _firstInput) {
                _firstInput = false;
                output.send(0, new IntToken(_previousIndex));
                output.send(0, new IntToken(inputValue ? 1: 0));
                _codeBook.add(_current);
                _current = "";
                _previousIndex = 0;
            } else {
                _previousIndex = index;
            }
        } else if (_current != "") {
            // FIXME: This part of output can not be produced.
            int length = _current.length();
            _previousIndex =
                _codeBook.indexOf(_current.substring(0, length - 2));
            output.send(0, new IntToken(_previousIndex));
            if (_current.endsWith("1")) {
                output.send(0, new IntToken(1));
            } else {
                output.send(0, new IntToken(0));
            }
        }
    }
    
    public void initialize() {
        _codeBook = new LinkedList();
        _codeBook.add("");
        _current = "";
        _previousIndex = 0;
        _firstInput = true;
    }

    ////////////////////////////////////////////////////////////
    ////                   private variables                ////
    
    private int _previousIndex = 0;
    private LinkedList _codeBook;
    private String _current;
    private boolean _firstInput;
}
