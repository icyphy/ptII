/* ASTPtFunctionalIfNode represents method calls on other Tokens and functional
   if-then else (?:) constructs.

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
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionalIfNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents functional if-then-else
nodes.
<p>
A functional if-then-else if of the form booleanToken ? token : token
The token returned depends on the value of the boolean.
<p>
@author Neil Smyth
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtFunctionalIfNode extends ASTPtRootNode {

    /** Evaluate the parse tree of a functional if-then-else expression.
     *  This expression has three sub-expressions. The first sub-expression
     *  should be of type boolean. If the first sub-expression evaluates
     *  to true, the value of the whole expression is given by the second
     *  sub-expression. Otherwise, the value is given by the third
     *  sub-expression. Depending on the value of the first sub-expression,
     *  only one of the second and third sub-expressions is evaluated.
     *  @exception IllegalActionException If an error occurs when
     *  trying to evaluate one of the sub-expressions.
     *  @return The token containing the value of the expression.
     */
    public ptolemy.data.Token evaluateParseTree()
            throws IllegalActionException {
        if (_isConstant && _ptToken != null) {
            return _ptToken;
        }

        int num = jjtGetNumChildren();
	if (num != 3) {
	    // A functional-if node MUST have three children in the parse
	    // tree.
	    throw new InternalErrorException(
		    "PtParser error: a functional-if node does not have "
		    + "three children in the parse tree.");
	}

	// evaluate the first sub-expression
	ASTPtRootNode child = (ASTPtRootNode)jjtGetChild(0);
        ptolemy.data.Token test = child.evaluateParseTree();
        if (!(test instanceof BooleanToken)) {
            throw new IllegalArgumentException(
                    "Functional-if must branch on a boolean: "
		    + test.toString());
        }

        boolean value = ((BooleanToken)test).booleanValue();
	// choose the correct sub-expression to evaluate
        if (value) {
	    child = (ASTPtRootNode)jjtGetChild(1);
        } else {
            child = (ASTPtRootNode)jjtGetChild(2);
        }
	_ptToken = child.evaluateParseTree();

	return _ptToken;
    }

    public ASTPtFunctionalIfNode(int id) {
        super(id);
    }

    public ASTPtFunctionalIfNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtFunctionalIfNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtFunctionalIfNode(p, id);
    }
}
