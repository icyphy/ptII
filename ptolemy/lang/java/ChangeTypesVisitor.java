/* A Java AST visitor that changes the types of some variables.

 Copyright (c) 2000 The Regents of the University of California.
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
import java.util.Map;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A Java AST visitor that changes the types of some variables.
 *
 *  @author Jeff Tsay
 */
//////////////////////////////////////////////////////////////////////////
//// ChangeTypesVisitor
public class ChangeTypesVisitor extends ReplacementJavaVisitor {

    public ChangeTypesVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _declToTypeMap = (Map) args.get(0);
        _declsLeft = _declToTypeMap.size();

        return _defaultVisit(node, null);
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) node);

        TypeNameNode typeNode = (TypeNameNode) _declToTypeMap.get(typedDecl);

        if (typeNode != null) {
            node.setDefType(typeNode);
            // typedDecl.setType(typeNode);
            _declsLeft--;
        }

        return node;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }


    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node);
    }

    protected Object _visitVarInitDeclNode(VarInitDeclNode node) {
        TypedDecl typedDecl = (TypedDecl) JavaDecl.getDecl((NamedNode) node);

        TypeNameNode typeNode = (TypeNameNode) _declToTypeMap.get(typedDecl);

        if (typeNode != null) {
            node.setDefType(typeNode);
            // typedDecl.setType(typeNode);
            _declsLeft--;
        }

        if (_declsLeft > 0) {
            node.setInitExpr((TreeNode) node.getInitExpr().accept(this, null));
        }

        return node;
    }

    /** The default visit method. Replace all children with their return
     *  values, using the same arguments, and return the node. If there
     *  are no more declarations left to change, do not visit the children.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        if (_declsLeft > 0) {
            node.setChildren(TNLManip.traverseList(this, node, null, node.children()));
        }
        return node;
    }


    protected Map _declToTypeMap = null;
    protected int _declsLeft;
}
