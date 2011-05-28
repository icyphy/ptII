/*
 AbstractTokenHandler that implements getters and setters for the position 
 that is used as identifier of the token handler in the byte stream.
 
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

import ptolemy.data.Token;

//////////////////////////////////////////////////////////////////////////
//// AbstractTokenHandler

/**
 * <p>Implement getters and setters for the position that is used as identifier of the token in the byte stream.
 * The identifier is the first 2 bytes written within the byte stream before the byte stream produced by the token handler.
 * When the stream is parsed back to be converted to a token, the identifier helps locate correct token handler that could parse it.</p>
 * 
 * @param <T> Type of Token that the handler handles
 * @author ahuseyno
 * @version $Id$ 
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public abstract class AbstractTokenHandler<T extends Token> implements
        TokenHandler<T> {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return the position of the handler in the handler list of the TokenHandler.
     * Position field is used as the token type identifier in the byte stream
     * @return position The position in the handler list of the TokenHandler
     * @see TokenHandler 
     * @see #setPosition(short)
     */
    @Override
    public short getPosition() {
        return _position;
    }

    /**
     * Set the position of the handler in the token handler list.
     * Position field is used as the token type identifier in the byte stream
     * @param position
     * @see TokenHandler 
     * @see #getPosition()
     */
    @Override
    public void setPosition(short position) {
        this._position = position;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Position of the TokenHandler that identifies the token type in the byte stream.
     */
    private short _position;

}
