/* Huffman Decoder.

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

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// HuffmanCoder

/**
 Given a probability distribution and the corresponding alphabet,
 decode the input using Huffman code and send the result to the output
 port. Its base class HuffmanBasic generates the code book.
 The decoder simply decode the input according to this code book.

 @see HuffmanBasic
 @see HuffmanCoder
 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class HuffmanDecoder extends HuffmanBasic {
    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HuffmanDecoder(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Declare port types.
        output.setTypeAtLeast(ArrayType.elementType(alphabet));
        input.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HuffmanDecoder newObject = (HuffmanDecoder) super.clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.alphabet));
        } catch (IllegalActionException e) {
            // Should have been caught before.
            throw new InternalErrorException(e);
        }
        return newObject;
    }

    /** Generate the Huffman codebook for the given <i>pmf</i>, and
     *  encode the input into booleans and send them to the output port.
     *  @exception IllegalActionException If the input is not a decodable code.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ArrayToken alphabetArrayToken = (ArrayToken) alphabet.getToken();
        Token[] alphabetTokens = new Token[_pmf.length];

        for (int i = 0; i < _pmf.length; i++) {
            alphabetTokens[i] = alphabetArrayToken.getElement(i);
        }

        // Get the input token. Ready for output.
        if (!input.hasToken(0) && !_code.equals("")) {
            throw new IllegalActionException(this,
                    "This is not a decodable code.");
        }

        BooleanToken inputToken = (BooleanToken) input.get(0);

        if (inputToken.booleanValue()) {
            _code = _code + "1";
        } else {
            _code = _code + "0";
        }

        // Find the codeword in the code book.
        for (int i = 0; i < _pmf.length; i++) {
            if (_code.equals(_codeBook[i])) {
                output.send(0, alphabetTokens[i]);
                _code = "";
                break;
            }
        }
    }

    /** Initialize the actor. set the current input string to be empty.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _code = "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The current input string, concatenated by input booleans.
    private String _code = "";
}
