/* A concrete scalar token.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (wbwu@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

//////////////////////////////////////////////////////////////////////////
//// ConcreteScalarToken
/**
A token that represents a scalar of any type.
This is instantiated by the Constants class with name "scalar".

@author Edward A. Lee
@version $Id$
*/
public class ConcreteScalarToken extends ScalarToken {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return this token.
     *  @return This token.
     */
    public ScalarToken absolute() {
        return this;
    }

    /** Return false.
     *  @param arg A token to compare against.
     *  @return A BooleanToken with value false.
     */
    public BooleanToken isLessThan(ScalarToken arg) {
        return BooleanToken.FALSE;
    }

    /** Return the type of this token.
     *  @return BaseType.SCALAR.
     */
    public Type getType() {
        return BaseType.SCALAR;
    }
}
