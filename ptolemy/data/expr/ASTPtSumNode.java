/* ASTPtSumNode represent sum(+, -) nodes in the parse tree

 Copyright (c) 1998-2014 The Regents of the University of California.
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


 Created : May 1998

 */
package ptolemy.data.expr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtSumNode

/**
 The parse tree created from the expression string consists of a
 hierarchy of node objects. This class represents sum(+, -) nodes in
 the parse tree.

 @author Neil Smyth, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 */
public class ASTPtSumNode extends ASTPtRootNode {
    public ASTPtSumNode(int id) {
        super(id);
    }

    public ASTPtSumNode(PtParser p, int id) {
        super(p, id);
    }

    /** Clone the parse tree node by invoking the clone() method of
     *  the base class. The new node copies the list of operators (+, -)
     *  represented by this node.
     *  @return A new parse tree node.
     *  @exception CloneNotSupportedException If the superclass clone()
     *   method throws it.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ASTPtSumNode newNode = (ASTPtSumNode) super.clone();
        newNode._lexicalTokens = (ArrayList<Token>) _lexicalTokens.clone();
        return newNode;
    }

    /** Return the list of lexical tokens that were used to make this node.
     *  @return The list of lexical tokens that were used to make this node.
     */
    public List<Token> getLexicalTokenList() {
        return _lexicalTokens;
    }

    /** Return true if this node is (hierarchically) congruent to the
     *  given node, under the given renaming of bound identifiers.
     *  Derived classes should extend this method to add additional
     *  necessary congruency checks.
     *  @param node The node to compare to.
     *  @param renaming A map from String to String that gives a
     *  renaming from identifiers in this node to identifiers in the
     *  given node.
     */
    @Override
    public boolean isCongruent(ASTPtRootNode node, Map renaming) {
        if (!super.isCongruent(node, renaming)) {
            return false;
        }

        // The operators must be the same.
        Iterator<Token> nodeTokens = ((ASTPtSumNode) node)._lexicalTokens
                .iterator();

        for (Token token : _lexicalTokens) {
            Token nodeToken = nodeTokens.next();

            if (token.kind != nodeToken.kind) {
                return false;
            }

            if (!token.image.equals(nodeToken.image)) {
                return false;
            }
        }

        return true;
    }

    /** Close this node.
     */
    @Override
    public void jjtClose() {
        super.jjtClose();
        _lexicalTokens.trimToSize();
    }

    /** Traverse this node with the given visitor.
     */
    @Override
    public void visit(ParseTreeVisitor visitor) throws IllegalActionException {
        visitor.visitSumNode(this);
    }

    protected ArrayList<Token> _lexicalTokens = new ArrayList<Token>();
}
