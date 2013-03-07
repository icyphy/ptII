/* Tokenizer reads out tokens from the byte array.

 Copyright (c) 2011-2013 The Regents of the University of California.
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

package ptserver.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Tokenizer

/** Tokenizer reads out tokens from the byte array.
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class Tokenizer {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create a new instance of the tokenizer with the specified payload.
     *  @param payload the byte payload received from the MQTT broker.
     */
    public Tokenizer(byte[] payload) {
        // There is no need to close the ByteArrayInputStream since it just wraps a byte array.
        _inputStream = new DataInputStream(new ByteArrayInputStream(payload));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the next token in the byte stream or null if there is none left.
     *  @return the next token or null if there is none left
     *  @exception IOException if there is a problem reading the byte stream
     *  @exception IllegalActionException if there is a problem loading a token handler
     */
    public Token getNextToken() throws IOException, IllegalActionException {
        if (_inputStream.available() > 0) {
            return TokenParser.getInstance().convertToToken(_inputStream);
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The input stream that wrapped the byte array.
     */
    private final DataInputStream _inputStream;
}
