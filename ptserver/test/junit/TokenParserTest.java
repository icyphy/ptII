/*

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
package ptserver.test.junit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.UnionToken;
import ptolemy.data.UnsignedByteToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptserver.data.AttributeChangeToken;
import ptserver.data.CommunicationToken;
import ptserver.data.TokenParser;

/**
 * TokenParserTest class.
 *
 * @author ahuseyno
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
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
    public void testBooleanToken() throws IOException, IllegalActionException {
        BooleanToken token = new BooleanToken(false);
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testFloatToken() throws IOException, IllegalActionException {
        FloatToken token = new FloatToken(123);
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
    public void testShortToken() throws IOException, IllegalActionException {
        ShortToken token = new ShortToken(1234);
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testStringToken() throws IOException, IllegalActionException {
        StringToken token = new StringToken("testing");
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testUnsignedByteToken() throws IOException,
    IllegalActionException {
        UnsignedByteToken token = new UnsignedByteToken(220);
        //System.out.println(token.byteValue());
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        UnsignedByteToken token1 = TokenParser.getInstance().convertToToken(
                inputStream);
        assertEquals(token, token1);
        //System.out.println(token1.byteValue());
    }

    @Test
    public void testRecordToken() throws IOException, IllegalActionException {
        Token[] tokens = new Token[2];
        String[] labels = new String[2];

        labels[0] = "float";
        tokens[0] = new FloatToken(123);

        labels[1] = "long";
        tokens[1] = new DoubleToken(123.234);

        RecordToken token = new RecordToken(labels, tokens);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testArrayToken() throws IOException, IllegalActionException {
        Token[] tokens = new Token[2];

        tokens[0] = new FloatToken(123);

        tokens[1] = new StringToken("Strinttesting");

        ArrayToken token = new ArrayToken(tokens);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testIntMatrixToken() throws IOException, IllegalActionException {
        int[][] matrix = new int[3][4];

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                matrix[row][column] = row * column;

            }
        }
        IntMatrixToken token = new IntMatrixToken(matrix);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testLongMatrixToken() throws IOException,
    IllegalActionException {
        long[][] matrix = new long[3][4];

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                matrix[row][column] = row * column * 12232233L;

            }
        }
        LongMatrixToken token = new LongMatrixToken(matrix);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testDoubleMatrixToken() throws IOException,
    IllegalActionException {
        double[][] matrix = new double[3][4];

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                matrix[row][column] = row * column * 2.34;

            }
        }
        DoubleMatrixToken token = new DoubleMatrixToken(matrix);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testComplexMatrixToken() throws IOException,
    IllegalActionException {
        Complex[][] matrix = new Complex[3][4];

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                matrix[row][column] = new Complex(row * column * 2.34, row
                        * column * 12.5234);
            }
        }
        ComplexMatrixToken token = new ComplexMatrixToken(matrix);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testComplexToken() throws IOException, IllegalActionException {

        Complex value = new Complex(234.34, 3432.324);
        ComplexToken token = new ComplexToken(value);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        assertEquals(token,
                TokenParser.getInstance().convertToToken(inputStream));
    }

    @Test
    public void testUnionToken() throws IOException, IllegalActionException {

        String label = "unionlabel";
        Complex complex = new Complex(234.34, 3432.324);
        ComplexToken value = new ComplexToken(complex);
        UnionToken token = new UnionToken(label, value);

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

        token.addPort("testPort4", 1);
        tokens = new Token[2];
        tokens[0] = new BooleanToken(true);
        tokens[1] = new BooleanToken(false);
        token.putTokens("testPort4", 0, tokens);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        Token convertedToken = TokenParser.getInstance().convertToToken(
                inputStream);
        assertEquals(token, convertedToken);

    }

    @Test
    public void testCommunicationToken2() throws IOException,
    IllegalActionException {
        CommunicationToken token = new CommunicationToken();
        token.setTargetActorName("targetActor");
        token.addPort("testPort1", 1);
        Token[] tokens = new Token[1];
        tokens[0] = new DoubleToken(0.0);
        token.putTokens("testPort1", 0, tokens);
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        CommunicationToken convertedToken = TokenParser.getInstance()
                .convertToToken(inputStream);
        assertEquals(token, convertedToken);
        assertEquals(token.getPortChannelTokenMap().values().iterator().next()
                .get(0)[0], convertedToken.getPortChannelTokenMap().values()
                .iterator().next().get(0)[0]);
    }

    @Test
    public void testAttributeChangeToken() throws IOException,
    IllegalActionException {
        AttributeChangeToken token = new AttributeChangeToken();
        token.setTargetSettableName("targetSettable");
        token.setExpression("testing");

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream(outputStream);
        TokenParser.getInstance().convertToBytes(token, outputStream);
        Token convertedToken = TokenParser.getInstance().convertToToken(
                inputStream);
        assertEquals(token, convertedToken);
    }
}
