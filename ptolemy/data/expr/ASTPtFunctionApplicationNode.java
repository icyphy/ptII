/* ASTPtFunctionApplicationNode represents function nodes or array references in the parse tree

 Copyright (c) 1998-2003 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)

Created : May 1998
*/

package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionApplicationNode
/**
 This class represents an expression that is the application of a
function in the parse tree.  The first child of this node is the child
node that represents the function.  The function specification may any
node that evaluates to a FunctionToken, or a leaf node that refers to
the name of a function registered with the parser.  The remaining
children are node representing the arguments of the function.  For
information on the evaluation of functions, refer to {@link
ptolemy.data.expr.ParseTreeEvaluator#visitFunctionApplicationNode}.

@author Neil Smyth, Edward A. Lee, Steve Neuendorffer
@author Zoltan Kemenczy, Research in Motion Limited
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
@see ptolemy.data.expr.UtilityFunctions
@see java.lang.Math
*/
public class ASTPtFunctionApplicationNode extends ASTPtRootNode {

    public ASTPtFunctionApplicationNode(int id) {
        super(id);
    }

    public ASTPtFunctionApplicationNode(PtParser p, int id) {
        super(p, id);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String getFunctionName() {
        Node n = jjtGetChild(0);
        if (!(n instanceof ASTPtLeafNode))
            return null;
        else {
            ASTPtLeafNode leaf = (ASTPtLeafNode)n;
            if(leaf.isIdentifier()) {
                return leaf.getName();
            } else {
                return null;
            }
        }
    }

    public void jjtClose() {
        super.jjtClose();
        // We cannot assume that the result of a function call is
        // constant, even when the arguments to the function are.
        _isConstant = false;
    }

    /** Traverse this node with the given visitor.
     */
    public void visit(ParseTreeVisitor visitor)
            throws IllegalActionException {
        visitor.visitFunctionApplicationNode(this);
    }

}

