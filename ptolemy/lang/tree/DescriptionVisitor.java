/* A visitor that constructs a short description of the node it visits.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.lang.tree;

import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.IVisitor;
import ptolemy.lang.java.JavaVisitor;
import ptolemy.lang.StringManip;
import ptolemy.lang.TNLManip;
import ptolemy.lang.TreeNode;
import ptolemy.lang.java.nodetypes.*;

/**
A visitor that constructs a short description of the node it visits.

@author Edward A. Lee
@version $Id$
*/
public class DescriptionVisitor extends JavaVisitor {

    /** Create a visitor with a custom traversal method.
     */
    public DescriptionVisitor() {
        super(IVisitor.TM_CUSTOM);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Object visitNameNode(NameNode node, LinkedList args) {
        return _nameVisit(node, args);
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return _literalVisit(node, args);
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return _literalVisit(node, args);
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return _literalVisit(node, args);
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return _literalVisit(node, args);
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return _literalVisit(node, args);
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return _literalVisit(node, args);
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return _literalVisit(node, args);
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return _namedVisit(node, args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The default visit method, which returns the (unqualified)
     *  class name of the node.
     *  @param node The node to visit.
     *  @param args The arguments to the visitor (ignored).
     *  @return A description of the node.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return _getClassName(node);
    }

    /** Return a string with the classname followed by the literal in
     *  parentheses.
     *  @param node The node to visit.
     *  @param args The arguments to the visitor (ignored).
     *  @return A description of the node.
     */
    protected Object _literalVisit(LiteralNode node, LinkedList args) {
        return (_getClassName(node) + " (" + node.getLiteral() + ")");
    }

    /** Return a string with the classname followed by the name in
     *  parentheses.
     *  @param node The node to visit.
     *  @param args The arguments to the visitor (ignored).
     *  @return A description of the node.
     */
    protected Object _nameVisit(NameNode node, LinkedList args) {
        return (_getClassName(node) + " (" + node.getIdent() + ")");
    }

    /** Return a string with the classname followed by the name in
     *  parentheses.
     *  @param node The node to visit.
     *  @param args The arguments to the visitor (ignored).
     *  @return A description of the node.
     */
    protected Object _namedVisit(NamedNode node, LinkedList args) {
        return (_getClassName(node) + " (" + node.getName().getIdent() + ")");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the classname of the node.
     *  @param The node.
     *  @return A String giving the (unqualified) class name.
     */
    private Object _getClassName(Object node) {
        return StringManip.unqualifiedPart(node.getClass().getName());
    }
}
