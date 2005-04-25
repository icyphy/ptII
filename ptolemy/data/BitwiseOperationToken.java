/* An interface for tokens that support bitwise operations.

Copyright (c) 2002-2005 The Regents of the University of California.
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
package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// BitwiseOperationToken

/**
   The operations that can be performed on tokens that have bitwise operations.

   @author Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Green (neuendor)
   @Pt.AcceptedRating Yellow (wbwu)
*/
public interface BitwiseOperationToken {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Returns a token representing the bitwise AND of this token and
     *  the given token.
     *  @param rightArgument  The token that is bitwise ANDed with this token.
     *  @return The bitwise AND.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseAnd(Token rightArgument)
            throws IllegalActionException;

    /** Returns a token representing the bitwise NOT of this token.
     *  @return The bitwise NOT of this token.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseNot() throws IllegalActionException;

    /** Returns a token representing the bitwise OR of this token and
     *  the given token.
     *  @param rightArgument  The token that is bitwise OR'd with this token.
     *  @return The bitwise OR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseOr(Token rightArgument)
            throws IllegalActionException;

    /** Returns a token representing the bitwise XOR of this token and
     *  the given token.
     *  @param rightArgument  The token that is bitwise XOR'd with this token.
     *  @return The bitwise XOR.
     *  @exception IllegalActionException If the given token is not
     *  compatible for this operation, or the operation does not make
     *  sense for this type.
     */
    public BitwiseOperationToken bitwiseXor(Token rightArgument)
            throws IllegalActionException;
}
