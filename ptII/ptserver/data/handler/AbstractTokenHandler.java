/*
 AbstractTokenHandler that implements getters and setters for the position
 
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
 * AbstractTokenHandler that implements getters and setters for the position.
 * Position field is used as a token identifier in the byte stream. 
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
    /* (non-Javadoc)
     * @see ptserver.data.handler.TokenHandler#getPosition()
     */
    @Override
    public short getPosition() {
        return position;
    }

    /* (non-Javadoc)
     * @see ptserver.data.handler.TokenHandler#setPosition(short)
     */
    @Override
    public void setPosition(short position) {
        this.position = position;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * Position of the TokenHandler
     */
    private short position;

}
