/* ASTPtShiftNodes represent arthimetic and logical shift
 operators(<<, >>, >>>) nodes in the parse tree.

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

import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtShiftNode

/**
 The parse tree created from the expression string consists of a
 hierarchy of node objects. This class represents shift
 operators(&gt;&gt;, &lt;&lt;, &lt;&lt;&lt;) nodes in the parse tree.
 <p>
 Each node of this type has exactly two children. The resolved type
 is the type of the first child.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 */
public class ASTPtShiftNode extends ASTPtRootNode {
    public ASTPtShiftNode(int id) {
        super(id);
    }

    public ASTPtShiftNode(PtParser p, int id) {
        super(p, id);
    }

    /** Return the lexical token representing the operation of this node.
     *  @return the token that represents the operation of this node.
     */
    public Token getOperator() {
        return _lexicalToken;
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

        if (_lexicalToken.kind != ((ASTPtShiftNode) node)._lexicalToken.kind) {
            return false;
        }

        return true;
    }

    /** Traverse this node with the given visitor.
     */
    @Override
    public void visit(ParseTreeVisitor visitor) throws IllegalActionException {
        visitor.visitShiftNode(this);
    }

    protected Token _lexicalToken = null;
}
