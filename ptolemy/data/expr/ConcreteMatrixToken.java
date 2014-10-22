/* Matrix token with no element type.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.data.expr;

import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

///////////////////////////////////////////////////////////////////
//// ConcreteMatrixToken

/**
 A token that represents an empty matrix, with no element type.
 This is instantiated by the Constants class with name "matrix".

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (wbwu)
 @see Constants
 */
public class ConcreteMatrixToken extends MatrixToken {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the number of columns of the contained matrix.
     *  @return Zero.
     */
    @Override
    public int getColumnCount() {
        return 0;
    }

    /** Throw an ArrayIndexOutOfBoundsException.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return An exception.
     *  @exception ArrayIndexOutOfBoundsException Always thrown.
     */
    @Override
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
        throw new ArrayIndexOutOfBoundsException("Empty matrix.");
    }

    /** Return the Type of the tokens contained in this matrix token.
     *  @return A Type.
     */
    @Override
    public Type getElementType() {
        throw new ArrayIndexOutOfBoundsException("Empty matrix.");
    }

    /** Return the number of rows of the contained matrix.
     *  @return Zero.
     */
    @Override
    public int getRowCount() {
        return 0;
    }

    /** Return the type of this token.
     *  @return BaseType.MATRIX.
     */
    @Override
    public Type getType() {
        return BaseType.MATRIX;
    }
}
