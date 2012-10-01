/*
 This token holds a byte array and was created for performance reasons only.

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

package ptserver.data;

import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// ByteArrayToken

/** The ByteArrayToken encapsulates a byte array. It was created primarily for
 *  performance reasons in order to avoid instantiation of large amount of ByteTokens.
 *  @author Anar Huseynov
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class ByteArrayToken extends Token {

    /**
     * Instantiate a ByteArrayToken with an empty byte array.
     */
    public ByteArrayToken() {
        _array = new byte[0];
    }

    /**
     * Instantiate a ByteArrayToken with the provided array.
     * @param array The array to wrap.
     */
    public ByteArrayToken(byte[] array) {
        assert array != null : "Can't pass null arrays";
        _array = array;
    }

    /**
     * Returns the byte array that this token wraps.
     * @return the array
     */
    public byte[] getArray() {
        return _array;
    }

    /**
     * The byte array the token wraps.
     */
    private final byte[] _array;

}
