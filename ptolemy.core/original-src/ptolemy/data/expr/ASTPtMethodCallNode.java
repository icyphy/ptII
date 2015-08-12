/* ASTPtMethodCallNode represents method calls on other Tokens and functional
 if-then else (?:) constructs.

 Copyright (c) 1998-2014 The Regents of the University of California and
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


 Created : May 1998

 */
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtMethodCallNode

/**
 The parse tree created from the expression string consists of a
 hierarchy of node objects. This class represents method call nodes
 in the parse tree.
 <p>
 To allow extension of the parser capabilities without modifying
 the kernel code, method calls on Tokens are supported with the following
 syntax  (token).methodName(comma separated arguments).
 <p>
 Method arguments are processed as described in {@link
 ASTPtFunctionApplicationNode}.  However, to allow element-by-element
 method calls on ArrayTokens, the following sequence is followed here
 to find a method to execute:
 <ul>
 <li>Look for a method with tokens as supplied by PtParser.</li>
 <li>If that fails, convert all instances of ArrayToken to Token[] and
 look again, element-by-element.</li>
 <li>If that fails, convert all method arguments to their underlying java
 types and try again.</li>
 <li>Finally, if the above fails, convert the method object Token to
 its underlying java type and try again.</li>
 </ul>
 <p>

 @author Neil Smyth, University of California;
 @author Zoltan Kemenczy, Research in Motion Limited
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 */
public class ASTPtMethodCallNode extends ASTPtRootNode {
    public ASTPtMethodCallNode(int id) {
        super(id);
    }

    public ASTPtMethodCallNode(PtParser p, int id) {
        super(p, id);
    }

    /** Return the name of the method invoked by this node.
     *  @return the name of the method invoked by this node.
     */
    public String getMethodName() {
        return _methodName;
    }

    @Override
    public void jjtClose() {
        super.jjtClose();

        // We cannot assume anything about a method call.
        _isConstant = false;
    }

    /** Traverse this node with the given visitor.
     */
    @Override
    public void visit(ParseTreeVisitor visitor) throws IllegalActionException {
        visitor.visitMethodCallNode(this);
    }

    /** Need to store the method name of the method call.
     */
    protected String _methodName;
}
