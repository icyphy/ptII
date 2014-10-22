/*
 PingPongTokenHandler converts tokens from bytestream and back.

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

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.PingToken;
import ptserver.data.PongToken;

///////////////////////////////////////////////////////////////////
//// PingPongTokenHandler

/**
 * PingPongTokenHandler converts tokens from bytestream and back.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class PingPongTokenHandler implements TokenHandler<Token> {

    /**
     * Convert the Ping or Pong token to the by stream.
     * @param token the token to be converted
     * @param outputStream the outputStream holding stream of bytes
     * @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     * @exception IOException if there is a problem with the outputStream
     * @exception IllegalActionException if there is the state becomes inconsistent
     */
    @Override
    public void convertToBytes(Token token, DataOutputStream outputStream)
            throws IOException, IllegalActionException {
        if (token instanceof PingToken) {
            outputStream.writeLong(((PingToken) token).getTimestamp());
        } else if (token instanceof PongToken) {
            outputStream.writeLong(((PongToken) token).getTimestamp());
        } else {
            throw new IllegalStateException("Wrong class was passed in");
        }
    }

    /**
     * Return a Ping or Pong token depending on the tokenType by deserializing the inputStream.
     * @param inputStream The inputStream that contains serialized version
     * of a token
     * @param tokenType the type of the token to be read.
     * @return Token parsed from inputStream
     * @exception IOException if there is a problem with the outputStream
     * @exception IllegalActionException if there is the state becomes inconsistent
     */
    @Override
    public Token convertToToken(DataInputStream inputStream,
            Class<? extends Token> tokenType) throws IOException,
            IllegalActionException {
        if (tokenType == PingToken.class) {
            return new PingToken(inputStream.readLong());
        } else if (tokenType == PongToken.class) {
            return new PongToken(inputStream.readLong());
        } else {
            throw new IllegalStateException("Wrong class was passed in");
        }
    }

}
