/* PtRootNode is the root node of the parse tree. It is also the base
   class for all Ptolemy II nodes as each node "isA" root node for the
   tree below it.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

import java.util.Vector;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
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
The tree is resolved in a top down manner, calling evaluateTree() on the
children of each node before resolving the type of the current node.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtRootNode implements Node {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called to recursively evaluate the parse tree
     *  of nodes returned from the parser. Starting at the top, it resolves
     *  the ptolemy.data.Token stored in each Node in a depth first manner.
     *  When all the children of a node have returned (type & value resolved),
     *  the type & value of the current node may be resolved by a call to
     *  _resolveNode() method.
     *  @exception IllegalActionException Thrown when an error occurs
     *  trying to evaluate the PtToken type and/or value to be stored in
     *  node in the tree.
     *  @return The token contained by the root node for the parse tree.
     */
    public ptolemy.data.Token evaluateParseTree()
            throws IllegalActionException {
        if (_isConstant && _ptToken != null) {
            return _ptToken;
        }
        int numChildren = jjtGetNumChildren();
        if (numChildren == 0) {
            // leaf node, should not be here
            throw new InternalErrorException(
                    "Encountered a node with no children that is " +
                    "not a leaf node, check PtParser.");
        } else {
            _childTokens = new ptolemy.data.Token[numChildren];
            for (int i = 0; i < numChildren; i++) {
                ASTPtRootNode child = (ASTPtRootNode)jjtGetChild(i);
                _childTokens[i] = child.evaluateParseTree();
            }
            _ptToken = _resolveNode();
            return _ptToken;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public ASTPtRootNode(int i) {
        _id = i;
    }

    public ASTPtRootNode(PtParser p, int i) {
        this(i);
        _parser = p;
    }

    public static Node jjtCreate(int id) {
        return new ASTPtRootNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtRootNode(p, id);
    }

    public void jjtOpen() {
    }

    public void jjtClose() {
        if (_children != null) {
            _children.trimToSize();
            _isConstant = true;
            for (int i = 0; i < _children.size(); ++i) {
                ASTPtRootNode ch = (ASTPtRootNode)jjtGetChild(i);
                if (!ch._isConstant) {
                    _isConstant = false;
                    break;
                }
            }
        }
    }

    public void jjtSetParent(Node n) { _parent = n; }
    public Node jjtGetParent() { return _parent; }

    public void jjtAddChild(Node n, int i) {
        if (_children == null) {
            _children = new Vector();
        }
        if (i >= _children.size()) {
            while (_children.size() <= i) {
                _children.add(null);
            }
        }
        _children.setElementAt(n, i);
    }

    public Node jjtGetChild(int i) {
        return (Node)_children.elementAt(i);
    }

    public int jjtGetNumChildren() {
        return (_children == null) ? 0 : _children.size();
    }

    /* You can override these two methods in subclasses of RootNode to
       customize the way the node appears when the tree is dumped.  If
       your output uses more than one line you should override
       toString(String), otherwise overriding toString() is probably all
       you need to do. */

    public String toString() { return PtParserTreeConstants.jjtNodeName[_id]; }
    public String toString(String prefix) { return prefix + toString() ; }

    /* Override this method if you want to customize how the node dumps
       out its children. - overridden Neil Smyth*/

    public void displayParseTree(String prefix) {
        if (_ptToken != null) {
            String str = toString(prefix) + ", Token type: ";
            str = str + _ptToken.getClass().getName() + ", Value: ";
            System.out.println( str + _ptToken.toString());
        } else {
            System.out.println( toString(prefix) + "  _ptToken is null");
        }
        if (_children != null) {
            for (int i = 0; i < _children.size(); ++i) {
                ASTPtRootNode n = (ASTPtRootNode)_children.elementAt(i);
                if (n != null) {
                    n.displayParseTree(prefix + " ");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    /** Resolves the Token to be stored in the node. When this
     *  method is called by evaluateParseTree(), the tokens in each of the
     *  children have been resolved. Thus this method is concerned with
     *  evaluating both the value and type of the ptToken to be stored.
     *  This method should be overridden in all subclasses which have children.
     *  @exception IllegalActionException Thrown when an error occurs
     *   trying to evaluate the PtToken type and/or value to be stored in
     *   the current node.
     *  @return The ptolemy.data.Token stored in this node.
     */
    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {
        int num = jjtGetNumChildren();
        if (num > 1) {
            throw new IllegalActionException(
                    "Node has several children, this method " +
                    "should be overridden!");
        } else if (num == 0) {
            throw new IllegalActionException(
                    "Node has no children, this method " +
                    "should be overridden!");
        }
        return _childTokens[0];
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected Node _parent;
    protected Vector _children;
    protected int _id;
    protected PtParser _parser;

    ///////////////////////////////////////////////////////////////////
    /// from here until next line of dashes is code for PtParser

    /** Each node stores its type and state information in this variable.
     */
    protected ptolemy.data.Token _ptToken;

    /** In nodes with more than one child, the operators are stored in this
     *  LinkedList. Note that here token refers to tokens returned by the
     *  lexical analyzer.
     */
    protected LinkedList _lexicalTokens = new LinkedList();

    /** Stores the ptolemy.data.Tokens of each of the children nodes */
    protected ptolemy.data.Token[] _childTokens;

    /** Flags whether the parse tree under this root evaluates to a constant.
     */
    protected boolean _isConstant = false;

}
