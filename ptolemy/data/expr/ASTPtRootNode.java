/* PtRootNode is the root node of the parse tree. It is also the base
 class for all Ptolemy II nodes as each node is a root node for the
 tree below it.

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
import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ASTPtRootNode

/**
 The parse tree created from the expression string consists of a
 hierarchy of node objects, each of which is an instance of a class
 derived from this class. This is because each node is a root node for
 the portion of the parse tree below it.
 <p>
 Each node in the parse tree stores its type and state information
 in a ptolemy.data.Token variable. A parent node uses the type and value of
 the ptolemy.data.Tokens contained in its child nodes to evaluate the type
 and value of the ptolemy.data.Token it should contain.
 <P>
 When a node has more than one child nodes, the lexical tokens relating
 the child nodes are stored in the parent node. Thus if we parsed a string
 such as "2+4-9", the child nodes would be leaf nodes containing
 ptolemy.data.Token's with values 2, 4 and 9, and the parent node would
 store the lexical tokens representing the "+" and the "-".
 <p>
 The tree is evaluated in a top down manner, calling evaluateParseTree() on the
 children of each node before resolving the type of the current node.

 @author Neil Smyth
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 */
public class ASTPtRootNode implements Node, Cloneable {
    public ASTPtRootNode(int i) {
        _id = i;
    }

    public ASTPtRootNode(PtParser p, int i) {
        this(i);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the parse tree node by invoking the clone() method of
     *  the base class (java.lang.Object).
     *  @return A new parse tree node.
     *  @exception CloneNotSupportedException If the superclass clone()
     *   method throws it.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ASTPtRootNode node = (ASTPtRootNode) super.clone();

        if (_children != null) {
            node._children = new ArrayList(_children.size());

            // Deeply clone all the children.
            for (Iterator i = _children.iterator(); i.hasNext();) {
                ASTPtRootNode child = (ASTPtRootNode) i.next();
                ASTPtRootNode clone = (ASTPtRootNode) child.clone();
                node._children.add(clone);
                clone._parent = node;
            }
        }

        return node;
    }

    /** Override this method if you want to customize how the node dumps
     *  out its children.
     */
    public void displayParseTree(String prefix) {
        if (_ptToken != null) {
            String str = toString(prefix) + ", Token type: ";
            str = str + _ptToken.getClass().getName() + ", Value: ";
            System.out.println(str + _ptToken.toString());
        } else {
            System.out.println(toString(prefix) + "  _ptToken is null");
        }

        if (_children != null) {
            for (int i = 0; i < _children.size(); ++i) {
                ASTPtRootNode n = (ASTPtRootNode) _children.get(i);

                if (n != null) {
                    n.displayParseTree(prefix + " ");
                }
            }
        }
    }

    /** Evaluate the parse tree.
     *  @exception IllegalActionException If an error occurs
     *  trying to evaluate the PtToken type and/or value to be stored in
     *  node in the tree.
     *  @return The token contained by the root node for the parse tree.
     *  @deprecated Use a ParseTreeEvaluator instead.
     */
    @Deprecated
    public ptolemy.data.Token evaluateParseTree() throws IllegalActionException {
        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        return evaluator.evaluateParseTree(this);
    }

    /** Return the evaluated token value of this node.  This value may be
     *  set during parsing, if this represents a constant value, or may be
     *  set during parse tree evaluation.
     */
    public ptolemy.data.Token getToken() {
        return _ptToken;
    }

    /** Return the type of this node.  This value may be set during
     *  parsing, if this represents a constant value, or may be set
     *  during parse tree evaluation.
     */
    public ptolemy.data.type.Type getType() {
        return _ptType;
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
        // Check to see that they are the same kind of node.
        if (node._id != _id) {
            return false;
        }

        // Empty children are allowed
        if (node._children == null && _children == null) {
            return true;
        }

        // But both must be empty
        if (node._children == null || _children == null) {
            return false;
        }

        // Check that they have the same number of children.
        if (node._children.size() != _children.size()) {
            return false;
        }

        // Check that their children are congruent.
        Iterator children = _children.iterator();
        Iterator nodeChildren = node._children.iterator();

        while (children.hasNext()) {
            ASTPtRootNode child = (ASTPtRootNode) children.next();
            ASTPtRootNode nodeChild = (ASTPtRootNode) nodeChildren.next();

            if (!child.isCongruent(nodeChild, renaming)) {
                return false;
            }
        }

        return true;
    }

    /** Return true if this node represents a constant value.  This will
     *  be set to true if the node is a constant leaf node (either it is a
     *  constant leaf node, or a pure function with constant children.)
     */
    public boolean isConstant() {
        return _isConstant;
    }

    /** Return true if this node has had its token value set to something
     *  other than null.
     */
    public boolean isEvaluated() {
        return _ptToken != null;
    }

    @Override
    public void jjtAddChild(Node n, int i) {
        if (_children == null) {
            _children = new ArrayList();
        }

        if (i >= _children.size()) {
            while (_children.size() <= i) {
                _children.add(null);
            }
        }

        _children.set(i, n);
    }

    @Override
    public void jjtClose() {
        if (_children != null) {
            // Trim the list of children, to reduce memory usage.
            _children.trimToSize();

            // Check to see if this node is constant, i.e. it has
            // only constant children.
            _isConstant = true;

            for (int i = 0; i < _children.size(); ++i) {
                ASTPtRootNode ch = (ASTPtRootNode) jjtGetChild(i);

                if (!ch._isConstant) {
                    _isConstant = false;
                    break;
                }
            }
        }
    }

    @Override
    public Node jjtGetChild(int i) {
        return (Node) _children.get(i);
    }

    @Override
    public int jjtGetNumChildren() {
        return _children == null ? 0 : _children.size();
    }

    @Override
    public Node jjtGetParent() {
        return _parent;
    }

    @Override
    public void jjtOpen() {
    }

    @Override
    public void jjtSetParent(Node n) {
        _parent = n;
    }

    /** Set whether this node is a constant.  In almost all cases this
     *  is statically determined when the parse tree is first parsed,
     *  and this method is not normally called.  However, it can be
     *  useful to transform a parse tree, which can make parts of the
     *  parse tree constant which were not before.  This method is
     *  provided for those transformations.
     */
    public void setConstant(boolean flag) {
        _isConstant = flag;
    }

    /** Set the value of this node.  This may be set during parsing,
     *  if the node is a constant node, or during evaluation of the
     *  expression.
     */
    public void setToken(ptolemy.data.Token token) {
        _ptToken = token;
    }

    /** Set the value of this node.  This may be set during parsing,
     *  if the node is a constant node, or during evaluation of the
     *  expression.
     */
    public void setType(ptolemy.data.type.Type type) {
        _ptType = type;
    }

    /** You can override these two methods in subclasses of RootNode
     * to customize the way the node appears when the tree is dumped.
     * If your output uses more than one line you should override
     * toString(String), otherwise overriding toString() is probably
     * all you need to do.
     */
    @Override
    public String toString() {
        return PtParserTreeConstants.jjtNodeName[_id] + ":" + _isConstant + ":"
                + _ptType + ":" + _ptToken;
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    /** Traverse this node with the given visitor.
     *  subclasses should override this method to invoke the appropriate
     *  method in the visitor.
     */
    public void visit(ParseTreeVisitor visitor) throws IllegalActionException {
        throw new IllegalActionException("The visit() method is not "
                + " implemented for nodes of type " + getClass().getName()
                + ".");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    protected Node _parent;

    protected ArrayList _children;

    protected int _id;

    /** Each node stores its type and state information in this variable.
     */
    protected ptolemy.data.Token _ptToken;

    /** The type of this node.
     */
    protected ptolemy.data.type.Type _ptType;

    /** Flags whether the parse tree under this root evaluates to a constant.
     */
    protected boolean _isConstant = false;
}
