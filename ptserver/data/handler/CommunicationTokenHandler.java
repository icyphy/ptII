/* Serialize a CommunicationToken to and from binary format.

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
import java.util.ArrayList;
import java.util.Map.Entry;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.CommunicationToken;
import ptserver.data.TokenParser;

//////////////////////////////////////////////////////////////////////////
//// CommunicationTokenHandler

/** <p>Serialize a CommunicationToken to and from binary format.</p>
 *  <p>The stream has the following format:
 *
 *  CommunicationToken = Type(2), CommunicationTokenValueField <br />
 *  CommunicationTokenValueField = TargetActorName, PortCount(2), PortData...
 *  (PortData is repeated PortCount times)<br />
 *  PortData = PortName, ChannelCount(2), ChannelData... (ChannelData is repeated ChannelCount times)<br />
 *  ChannelData = ChannelTokenCount(2), Token... (Token is repeated ChannelTokenCount times;
 *  Its serialized using TokenHandler defined for its data type)</p>
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class CommunicationTokenHandler implements
        TokenHandler<CommunicationToken> {

    /** Serialize the communication token to the binary according to the format defined in {@link CommunicationTokenHandler}.
     *  @param token Token to be converted to bytes.
     *  @param outputStream The stream to write to.
     *  @exception IOException if there is a problem with the outputStream
     *  @exception IllegalActionException if there is the state becomes inconsistent
     *  @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     */
    @Override
    public void convertToBytes(CommunicationToken token,
            DataOutputStream outputStream) throws IOException,
            IllegalActionException {

        outputStream.writeUTF(token.getTargetActorName());
        outputStream.writeShort(token.getPortChannelTokenMap().size());

        for (Entry<String, ArrayList<Token[]>> entry : token
                .getPortChannelTokenMap().entrySet()) {
            String port = entry.getKey();
            ArrayList<Token[]> channelTokens = entry.getValue();

            outputStream.writeUTF(port);
            outputStream.writeShort(channelTokens.size());

            for (Token[] tokens : channelTokens) {
                outputStream.writeShort(tokens.length);

                for (Token innerToken : tokens) {
                    TokenParser.getInstance().convertToBytes(innerToken,
                            outputStream);
                }
            }
        }
    }

    /** Deserialize the token from the stream according to the format defined in {@link CommunicationTokenHandler}.
     *  @param inputStream The stream to read from.
     *  @param tokenType The type of token to be parsed.
     *  @return CommunicationToken deserialized from the inputStream.
     *  @exception IOException If the stream cannot be read.
     *  @exception IllegalActionException Not thrown in this class.
     *  @see ptserver.data.handler.TokenHandler#convertToToken(java.io.DataInputStream, Class)
     */
    @Override
    public CommunicationToken convertToToken(DataInputStream inputStream,
            Class<? extends CommunicationToken> tokenType) throws IOException,
            IllegalActionException {
        CommunicationToken token = new CommunicationToken();
        token.setTargetActorName(inputStream.readUTF());

        short portCount = inputStream.readShort();
        for (int portIndex = 0; portIndex < portCount; portIndex++) {
            String portName = inputStream.readUTF();
            short channelCount = inputStream.readShort();
            token.addPort(portName, channelCount);

            for (int channelIndex = 0; channelIndex < channelCount; channelIndex++) {
                short tokenCount = inputStream.readShort();
                Token[] tokens = new Token[tokenCount];

                for (int j = 0; j < tokenCount; j++) {
                    Token innerToken = TokenParser.getInstance()
                            .convertToToken(inputStream);
                    tokens[j] = innerToken;
                }

                token.putTokens(portName, channelIndex, tokens);
            }
        }

        return token;
    }
}
