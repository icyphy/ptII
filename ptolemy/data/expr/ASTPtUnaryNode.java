/* ASTPtUnaryNode represent the unary operator(!, -) nodes in the parse tree

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtUnaryNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents unary operator(!, -, ~)
nodes in the parse tree.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtUnaryNode extends ASTPtRootNode {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public ASTPtUnaryNode(int id) {
        super(id);
    }

    public ASTPtUnaryNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtUnaryNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtUnaryNode(p, id);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {
        if (jjtGetNumChildren() != 1) {
            throw new InternalErrorException(
                    "More than one child of a Unary node");
        }
        ptolemy.data.Token result = _childTokens[0];
        if (_isMinus == true) {
            // Need to chose the type at the bottom of the hierarch
            // so as to not do any upcasting. For now IntToken will do.
            result = result.multiply(new ptolemy.data.IntToken(-1));
        } else if (_isNot == true) {
            if (!(result instanceof BooleanToken)) {
                throw new IllegalActionException(
                        "Not operator not support for non-boolean token: " +
                        result.toString());
            }
            result = ((BooleanToken)result).not();
        } else if (_isBitwiseNot == true) {
            if (result instanceof IntToken) {
                int tmp = ~(((IntToken)result).intValue());
                return new IntToken(tmp);
            } else if (result instanceof LongToken) {
                long tmp = ~(((LongToken)result).longValue());
                return new LongToken(tmp);
            } else {
                throw new IllegalActionException(
                        "Cannot apply bitwise NOT \"~\" to  " +
                        "non-integer type: " + result.toString());
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected boolean _isMinus = false;
    protected boolean _isNot = false;
    protected boolean _isBitwiseNot = false;
}
