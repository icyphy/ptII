/** A token that contains an unsized Array number.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCL5AIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.data;

import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;

///////////////////////////////////////////////////////////////////
//// UnsizedArrayToken

/**
 A token that represents an array.  This token type exists solely so
 that types can be declared through the parameter mechanism using a
 token value, since we don't represent types distinctly from tokens.
 Generally speaking actors should process ArrayTokens, which properly
 report their length.

 @author Steve Neuendorffer
 @see ptolemy.data.ArrayToken
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class UnsizedArrayToken extends ArrayToken {

    /** Construct an empty array token with the given element type.
     *  @param elementType A token type.
     */
    public UnsizedArrayToken(Type elementType) {
        super(elementType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the type of this token.
     *  @return an unsized array type, having the correct element type.
     */
    @Override
    public Type getType() {
        return new ArrayType(getElementType());
    }

    /** Return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  @return A string beginning with "{" that contains expressions
     *  for every element in the array separated by commas, ending with "}".
     */
    @Override
    public String toString() {
        return getType().toString();
    }
}
