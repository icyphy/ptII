/* ASTPtLogicalNode represent logical operator(&&, ||) nodes in the parse tree

 Copyright (c) 1998-2002 The Regents of the University of California.
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

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.BooleanToken;

//////////////////////////////////////////////////////////////////////////
//// ASTPtLogicalNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents logical operator(&&, ||)
nodes in the parse tree.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtLogicalNode extends ASTPtRootNode {

    /** Evaluate the parse tree of a conditional-and or conditional-or
     *  expression.
     *  @exception IllegalActionException If an error occurs when trying to
     *  evaluate a sub-expression.
     *  @return The token containing the result of the expression.
     */
    public ptolemy.data.Token evaluateParseTree()
            throws IllegalActionException {
        if (_isConstant && _ptToken != null) {
            return _ptToken;
        }

        int num = jjtGetNumChildren();
	int numOperators = _lexicalTokens.size();
	if (num <= 1 || numOperators != num - 1) {
	    throw new InternalErrorException(
	            "PtParser error: the parse tree for a conditional-and "
		    + "or conditional-or expression does not have the correct "
		    + "number of children or operators.");
	}

	Token operator = (Token)_lexicalTokens.get(0);
	boolean isAnd = false;
	if (operator.image.equalsIgnoreCase("&&")) {
	    isAnd = true;
	}

        for (int i = 0; i < num; i++) {
	    ASTPtRootNode child = (ASTPtRootNode)jjtGetChild(i);
	    ptolemy.data.Token value = child.evaluateParseTree();
            if (!(value instanceof BooleanToken)) {
                throw new IllegalActionException("Cannot perform logical "
                        + "operation on " + value.getClass());
            }
	    if (((BooleanToken)value).booleanValue() != isAnd) {
		_ptToken = new ptolemy.data.BooleanToken(!isAnd);
		return _ptToken;
	    }
	}
	_ptToken = new ptolemy.data.BooleanToken(isAnd);
	return _ptToken;
    }

    public ASTPtLogicalNode(int id) {
        super(id);
    }

    public ASTPtLogicalNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtLogicalNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtLogicalNode(p, id);
    }
}
