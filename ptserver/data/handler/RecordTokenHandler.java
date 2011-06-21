/*
 RecordTokenHandler converts RecordToken to/from byte stream

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
package ptserver.data.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.TokenParser;

///////////////////////////////////////////////////////////////////
//// RecordTokenHandler
/**
 * RecordTokenHandler converts RecordToken to/from byte stream
 *
 * @author ishwinde
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ishwinde)
 * @Pt.AcceptedRating Red (ishwinde)
 *
 */
public class RecordTokenHandler implements TokenHandler<RecordToken> {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Convert RecordToken to a byte stream using an algorithm defined in the DataOutputStream.
     * @exception IllegalActionException
     * @see ptserver.data.handler.TokenHandler#convertToBytes(ptolemy.data.Token, java.io.DataOutputStream)
     */
    public void convertToBytes(RecordToken token, DataOutputStream outputStream)
            throws IOException, IllegalActionException {
        int size = token.length();

        Set labelSet = token.labelSet();
        outputStream.writeInt(size);

        Iterator iterator = labelSet.iterator();

        while (iterator.hasNext()) {
            String label = (String) iterator.next();
            Token valueToken = token.get(label);
            outputStream.writeUTF(label);

            TokenParser.getInstance().convertToBytes(valueToken, outputStream);

        }

    }

    /**
     * Read from the inputStream and converts it to the RecordToken.
     * @exception IllegalActionException
     * @see ptserver.data.handler.TokenHandler#convertToToken(java.io.DataInputStream, Class)
     */
    public RecordToken convertToToken(DataInputStream inputStream,
            Class<? extends RecordToken> tokenType)
            throws IOException, IllegalActionException {

        int size = inputStream.readInt();
        String[] labels = new String[size];
        Token[] tokens = new Token[size];

        for (int index = 0; index < size; index++) {

            labels[index] = inputStream.readUTF();
            tokens[index] = TokenParser.getInstance().convertToToken(
                    inputStream);

        }
        return new RecordToken(labels, tokens);
    }
}
