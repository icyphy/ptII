/*
 
 Copyright (c) 2011 The Regents of the University of California.
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
package ptserver.data.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;

import ptolemy.data.DoubleToken;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.CommunicationToken;
import ptserver.data.TokenParser;

public class TokenParserTest {

    @Test
    public void testLongToken() throws IOException, IllegalActionException {
        LongToken token = new LongToken(12345566);
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testDoubleToken() throws IOException, IllegalActionException {
        DoubleToken token = new DoubleToken(123455.123);
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testCommunicationToken() throws IOException,
            IllegalActionException {
        CommunicationToken token = new CommunicationToken();
        token.setTargetActorName("targetActor");

        token.addPort("testPort1", 1);
        Token[] tokens = new Token[2];
        tokens[0] = new LongToken(1);
        tokens[1] = new LongToken(2);
        token.putTokens("testPort1", 0, tokens);

        token.addPort("testPort2", 1);
        tokens = new Token[3];
        tokens[0] = new DoubleToken(1.04);
        tokens[1] = new DoubleToken(2.04);
        tokens[2] = new DoubleToken(3.04);
        token.putTokens("testPort2", 0, tokens);

        token.addPort("testPort3", 2);
        token.putTokens("testPort3", 0, tokens);
        token.putTokens("testPort3", 1, tokens);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        Token convertedToken = TokenParser.getInstance().convertToToken(
                inputStream);
        assertEquals(token, convertedToken);

    }
}
