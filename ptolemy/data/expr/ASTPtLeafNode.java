/* PtLeafNode represents leaf nodes in the parse tree.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// ASTPtLeafNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents the leaf nodes of the
tree.

@author Neil Smyth
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtLeafNode extends ASTPtRootNode {

    public ASTPtLeafNode(int id) {
        super(id);
    }

    public ASTPtLeafNode(PtParser p, int id) {
        super(p, id);
    }

    /** Return the name that this node refers to.  This may be a
     *  literal value, such as "5", or a reference to another object,
     *  such as the name of a variable in scope.
     */
    public String getName() {
        return _name;
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
    public boolean isCongruent(ASTPtRootNode node, Map renaming) {
        if (!super.isCongruent(node, renaming)) {
            return false;
        }
        // Both must be constant or not.
        if (isConstant() != node.isConstant()) {
            return false;
        }
        if (isConstant()) {
            // If constant, then check the value
            return getToken().equals(node.getToken());
        } else {
            // Else, check the name.
            String checkName = (String)renaming.get(getName());
            if (checkName == null) {
                checkName = getName();
            }
            if (!checkName.equals(((ASTPtLeafNode)node).getName())) {
                return false;
            } else {
                return true;
            }
        }
    }
    
    /** Return true if the leaf is an identifier that must be
     * evaluated in scope.
     */
    public boolean isIdentifier() {
        return !isConstant();
    }

    /** Return a string representation
     */
    public String toString() {
        return super.toString() + ":" + _name;
    }

    /** Traverse this node with the given visitor.
     */
    public void visit(ParseTreeVisitor visitor)
            throws IllegalActionException {
        visitor.visitLeafNode(this);
    }

    /** The identifier that this leaf node refers to.
     */
    protected String _name;
}
