/* PtLeafNode represents leaf nodes in the parse tree.

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

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtLeafNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents the leaf nodes of the
tree.

@author Neil Smyth
@version$Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtLeafNode extends ASTPtRootNode {

    ///////////////////////////////////////////////////////////////////
    /// from here until next line of dashes is code for PtParser

/** When the input String refers to another parameter, we store the
 *  referred parameter in the leaf node. Thus when the value of a
 *  parameter changes, by reevaluating the parse tree we get the
 *  correct result.
 */
protected Variable _var;

    /** If this leaf node represents a reference to a parameter, return the
     *  PtToken contained in that parameter. Otherwise return the PtToken
     *  object stored in this node.
     *  @return The PtToken stored/referenced by this node
     *  @exception IllegalActionException If an error occurs
     *  trying to evaluate the PtToken type and/or value to be stored in
     *  node in the tree.
     */
    public ptolemy.data.Token evaluateParseTree()
            throws IllegalActionException {
        if (_var != null) {
            _ptToken = _var.getToken();
        } else if (_ptToken == null) {
            throw new IllegalActionException(
                    "In a leaf node, either _ptToken or _param " +
                    "must be non-null");
        }
        return _ptToken;
    }

    ///////////////////////////////////////////////////////////////////
    public ASTPtLeafNode(int id) {
        super(id);
    }

    public ASTPtLeafNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtLeafNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtLeafNode(p, id);
    }
}
