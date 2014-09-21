/* ASTPtUnionConstructNode represents record construction in the parse tree.

 Copyright (c) 2006-2013 The Regents of the University of California.
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


 Created : June 2005

 */
package ptolemy.data.expr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////ASTPtUnionConstructNode

/**
 The parse tree created from the expression string consists of a
 hierarchy of node objects. This class represents union construction using
 the following syntax: <code>{|foo = "abc"|}</code>. The result of
 parsing and evaluating this expression is a union token with the label
 <i>foo</i>, and the corresponding value is a StringToken of value "abc".

 When using an expression to specify a union type, multiple labels
 are allowed: <code>{|foo = string, bar = int|}</code>, which is a union
 type with two alternatives: one labeled <i>foo</i> with type string, one
 labeled <i>bar</i> with type integer.

 FIXME: ambiguity

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 */
public class ASTPtUnionConstructNode extends ASTPtRootNode {
    public ASTPtUnionConstructNode(int id) {
        super(id);
    }

    public ASTPtUnionConstructNode(PtParser p, int id) {
        super(p, id);
    }

    /** Return the list of label names for this union construct.
     *  The order of the list is not meaningful.
     *  @return The list of label names.
     */
    public List getLabelNames() {
        return _labelNames;
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
        // Note: we don't call super.isCongruent(), which checks for ordered
        // congruence of the children.
        // Check to see that they are the same kind of node.
        if (node._id != _id) {
            return false;
        }

        ASTPtRecordConstructNode recordNode = (ASTPtRecordConstructNode) node;

        // Empty records are allowed (Are they?)
        if (recordNode._fieldNames == null && _labelNames == null) {
            return true;
        }

        // But both must be empty
        if (recordNode._fieldNames == null || _labelNames == null) {
            return false;
        }

        // Check that they have the same number of fields.
        if (recordNode._fieldNames.size() != _labelNames.size()) {
            return false;
        }

        // The field names must be the same.
        // Not use set for unordered comparison.
        // Note that field names are not renamed!
        Set nameSet = new HashSet(_labelNames);
        Set nodeNameSet = new HashSet(
                ((ASTPtRecordConstructNode) node)._fieldNames);

        if (!nameSet.equals(nodeNameSet)) {
            return false;
        }

        // Check that their children are congruent, under renaming.
        Iterator fieldNames = _labelNames.iterator();
        Iterator children = _children.iterator();

        while (fieldNames.hasNext()) {
            String fieldName = (String) fieldNames.next();
            ASTPtRootNode child = (ASTPtRootNode) children.next();
            int nodeIndex = recordNode._fieldNames.indexOf(fieldName);
            ASTPtRootNode nodeChild = (ASTPtRootNode) recordNode._children
                    .get(nodeIndex);

            if (!child.isCongruent(nodeChild, renaming)) {
                return false;
            }
        }

        return true;
    }

    /** Traverse this node with the given visitor.
     */
    @Override
    public void visit(ParseTreeVisitor visitor) throws IllegalActionException {
        visitor.visitUnionConstructNode(this);
    }

    /** The list of field names for the record.
     */
    protected LinkedList _labelNames = new LinkedList();
}
