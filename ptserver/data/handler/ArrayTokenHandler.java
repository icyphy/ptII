/* ArrayTokenHandler converts ArrayToken to/from byte stream.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
package ptserver.data.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.TokenParser;

///////////////////////////////////////////////////////////////////
//// ArrayTokenHandler

/** ArrayTokenHandler converts ArrayToken to/from byte stream.
 *  @author ishwinde
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ishwinde)
 *  @Pt.AcceptedRating Red (ishwinde)
 */
public class ArrayTokenHandler implements TokenHandler<ArrayToken> {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Write the ArrayToken to a byte array.
     *  @param token Token to be converted to bytes.
     *  @param outputStream The stream to write to.
     *  @exception IOException If the stream cannot be written.
     *  @exception IllegalActionException Not thrown in this class.
     *  @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     */
    @Override
    public void convertToBytes(ArrayToken token, DataOutputStream outputStream)
            throws IOException, IllegalActionException {

        int size = token.length();
        outputStream.writeInt(size);

        for (int index = 0; index < size; index++) {
            Token elementToken = token.getElement(index);
            TokenParser.getInstance()
                    .convertToBytes(elementToken, outputStream);
        }
    }

    /** Read an ArrayToken from the input stream.
     *  @param inputStream The stream to read from.
     *  @param tokenType The type of token to be parsed.
     *  @return The populated ArrayToken object.
     *  @exception IOException If the stream cannot be read.
     *  @exception IllegalActionException Not thrown in this class.
     *  @see ptserver.data.handler.TokenHandler#convertToToken(java.io.DataInputStream, Class)
     */
    @Override
    public ArrayToken convertToToken(DataInputStream inputStream,
            Class<? extends ArrayToken> tokenType) throws IOException,
            IllegalActionException {

        int size = inputStream.readInt();
        Token[] tokens = new Token[size];

        for (int index = 0; index < size; index++) {
            tokens[index] = TokenParser.getInstance().convertToToken(
                    inputStream);
        }

        return new ArrayToken(tokens);
    }
}
