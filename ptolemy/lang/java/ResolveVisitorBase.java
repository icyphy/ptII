/*
A base class for visitors that do resolution.

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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang.java;

import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// ResolveVisitorBase
/** ResolveVisitorBase attempts to collect the default operations for
 *  JavaVisitors that do resolution of some kind.
 *
 *  Nodes that are leaf nodes typically do nothing, and nodes that have
 *  children typically just pass on the same arguments to their children.
 *  However, nodes that only have NameNode's as their children do nothing.
 *
 *  @author ctsay@eecs.berkeley.edu
 */
public abstract class ResolveVisitorBase extends JavaVisitor
       implements JavaStaticSemanticConstants {
    public ResolveVisitorBase() {
        this(TM_CUSTOM);
    }

    public ResolveVisitorBase(int traversalMethod) {
        super(traversalMethod);
    }

    public Object visitNameNode(NameNode node, LinkedList args) {
        return null;
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return null;
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return null;
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return null;
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return null;
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return null;
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return null;
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return null;
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return null;
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return null;
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {
        return null;
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {
        return null;
    }

    // In general, classes cannot be resolved lazily because public classes
    // may inherit from them.

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return null;
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        return null;
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        return null;
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return null;
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return null;
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return null;
    }

    /** The default visit method. Traverse children with the same
     *  argument list.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        if (_traversalMethod == TM_CUSTOM) {
           TNLManip.traverseList(this, node, args, node.children());
        }
        return null;
    }
}
