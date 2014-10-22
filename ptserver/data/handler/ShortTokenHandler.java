/*
 ShortTokenHandler converts ShortToken to/from byte stream

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

import ptolemy.data.ShortToken;

///////////////////////////////////////////////////////////////////
//// ShortTokenHandler
/** ShortTokenHandler converts ShortToken to/from byte stream.
 *
 *  @author ishwinde
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ishwinde)
 *  @Pt.AcceptedRating Red (ishwinde)
 */
public class ShortTokenHandler implements TokenHandler<ShortToken> {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert ShortToken to a byte stream using an algorithm defined in the DataOutputStream.
     *  @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     *
     *  @param token The token to be converted to the byte stream.
     *  @param outputStream The byte stream to write the token to.
     *  @exception IOException If cannot write to the stream.
     */
    @Override
    public void convertToBytes(ShortToken token, DataOutputStream outputStream)
            throws IOException {
        outputStream.writeShort(token.shortValue());
    }

    /** Reads a short from the inputStream and converts it to the ShortToken.
     *  @see ptserver.data.handler.TokenHandler#convertToToken(java.io.DataInputStream, Class)
     *
     *  @param inputStream The stream that contains the token.
     *  @param tokenType The type of the token. Should be ShortToken or it's derivatives.
     *  @return The token that arrived on the stream.
     *  @exception IOException If the stream cannot be read.
     */
    @Override
    public ShortToken convertToToken(DataInputStream inputStream,
            Class<? extends ShortToken> tokenType) throws IOException {
        return new ShortToken(inputStream.readShort());
    }
}
