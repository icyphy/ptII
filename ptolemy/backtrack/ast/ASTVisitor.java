/* Superclass of any AST visitor.

Copyright (c) 1998-2004 The Regents of the University of California.
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

*/

package ptolemy.backtrack.ast;

import net.sourceforge.jrefactory.ast.*;
import net.sourceforge.jrefactory.parser.JavaParserVisitor;

//////////////////////////////////////////////////////////////////////////
//// ASTVisitor
/**
   Superclass of any AST visitor. It implements the default behavior of an
   AST visitor by recursively visiting every node in the AST in a depth-first
   manner. Different <tt>visit</tt> functions are defined for different types
   of nodes. Subclasses may override these functions to add behavior to the
   traversal on some or all types of nodes.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (tfeng)
*/
public class ASTVisitor implements JavaParserVisitor {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.SimpleNode, java.lang.Object)
     */
    public Object visit(SimpleNode node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTCompilationUnit, java.lang.Object)
     */
    public Object visit(ASTCompilationUnit node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPackageDeclaration, java.lang.Object)
     */
    public Object visit(ASTPackageDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTImportDeclaration, java.lang.Object)
     */
    public Object visit(ASTImportDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTTypeDeclaration, java.lang.Object)
     */
    public Object visit(ASTTypeDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTClassDeclaration, java.lang.Object)
     */
    public Object visit(ASTClassDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTUnmodifiedClassDeclaration, java.lang.Object)
     */
    public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAnnotation, java.lang.Object)
     */
    public Object visit(ASTAnnotation node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTMemberValuePairs, java.lang.Object)
     */
    public Object visit(ASTMemberValuePairs node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTMemberValuePair, java.lang.Object)
     */
    public Object visit(ASTMemberValuePair node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTMemberValue, java.lang.Object)
     */
    public Object visit(ASTMemberValue node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTMemberValueArrayInitializer, java.lang.Object)
     */
    public Object visit(ASTMemberValueArrayInitializer node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTTypeParameters, java.lang.Object)
     */
    public Object visit(ASTTypeParameters node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTTypeParameterList, java.lang.Object)
     */
    public Object visit(ASTTypeParameterList node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTTypeParameter, java.lang.Object)
     */
    public Object visit(ASTTypeParameter node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTReferenceTypeList, java.lang.Object)
     */
    public Object visit(ASTReferenceTypeList node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTActualTypeArgument, java.lang.Object)
     */
    public Object visit(ASTActualTypeArgument node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTClassBody, java.lang.Object)
     */
    public Object visit(ASTClassBody node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTNestedClassDeclaration, java.lang.Object)
     */
    public Object visit(ASTNestedClassDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTJSPBody, java.lang.Object)
     */
    public Object visit(ASTJSPBody node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTClassBodyDeclaration, java.lang.Object)
     */
    public Object visit(ASTClassBodyDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTEnumDeclaration, java.lang.Object)
     */
    public Object visit(ASTEnumDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTIdentifier, java.lang.Object)
     */
    public Object visit(ASTIdentifier node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTEnumElement, java.lang.Object)
     */
    public Object visit(ASTEnumElement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAnnotationTypeDeclaration, java.lang.Object)
     */
    public Object visit(ASTAnnotationTypeDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAnnotationTypeMemberDeclaration, java.lang.Object)
     */
    public Object visit(ASTAnnotationTypeMemberDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAnnotationMethodDeclaration, java.lang.Object)
     */
    public Object visit(ASTAnnotationMethodDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTConstantDeclaration, java.lang.Object)
     */
    public Object visit(ASTConstantDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTInterfaceDeclaration, java.lang.Object)
     */
    public Object visit(ASTInterfaceDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTNestedInterfaceDeclaration, java.lang.Object)
     */
    public Object visit(ASTNestedInterfaceDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTUnmodifiedInterfaceDeclaration, java.lang.Object)
     */
    public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTInterfaceBody, java.lang.Object)
     */
    public Object visit(ASTInterfaceBody node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTInterfaceMemberDeclaration, java.lang.Object)
     */
    public Object visit(ASTInterfaceMemberDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTFieldDeclaration, java.lang.Object)
     */
    public Object visit(ASTFieldDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTVariableDeclarator, java.lang.Object)
     */
    public Object visit(ASTVariableDeclarator node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTVariableDeclaratorId, java.lang.Object)
     */
    public Object visit(ASTVariableDeclaratorId node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTVariableInitializer, java.lang.Object)
     */
    public Object visit(ASTVariableInitializer node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTArrayInitializer, java.lang.Object)
     */
    public Object visit(ASTArrayInitializer node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTMethodDeclaration, java.lang.Object)
     */
    public Object visit(ASTMethodDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTMethodDeclarator, java.lang.Object)
     */
    public Object visit(ASTMethodDeclarator node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTFormalParameters, java.lang.Object)
     */
    public Object visit(ASTFormalParameters node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTFormalParameter, java.lang.Object)
     */
    public Object visit(ASTFormalParameter node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTConstructorDeclaration, java.lang.Object)
     */
    public Object visit(ASTConstructorDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTExplicitConstructorInvocation, java.lang.Object)
     */
    public Object visit(ASTExplicitConstructorInvocation node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTInitializer, java.lang.Object)
     */
    public Object visit(ASTInitializer node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTType, java.lang.Object)
     */
    public Object visit(ASTType node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTReferenceType, java.lang.Object)
     */
    public Object visit(ASTReferenceType node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTTypeArguments, java.lang.Object)
     */
    public Object visit(ASTTypeArguments node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPrimitiveType, java.lang.Object)
     */
    public Object visit(ASTPrimitiveType node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTResultType, java.lang.Object)
     */
    public Object visit(ASTResultType node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTName, java.lang.Object)
     */
    public Object visit(ASTName node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTClassOrInterfaceType, java.lang.Object)
     */
    public Object visit(ASTClassOrInterfaceType node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTNameList, java.lang.Object)
     */
    public Object visit(ASTNameList node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTGenericNameList, java.lang.Object)
     */
    public Object visit(ASTGenericNameList node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTExpression, java.lang.Object)
     */
    public Object visit(ASTExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAssignmentOperator, java.lang.Object)
     */
    public Object visit(ASTAssignmentOperator node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTConditionalExpression, java.lang.Object)
     */
    public Object visit(ASTConditionalExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTConditionalOrExpression, java.lang.Object)
     */
    public Object visit(ASTConditionalOrExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTConditionalAndExpression, java.lang.Object)
     */
    public Object visit(ASTConditionalAndExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTInclusiveOrExpression, java.lang.Object)
     */
    public Object visit(ASTInclusiveOrExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTExclusiveOrExpression, java.lang.Object)
     */
    public Object visit(ASTExclusiveOrExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAndExpression, java.lang.Object)
     */
    public Object visit(ASTAndExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTEqualityExpression, java.lang.Object)
     */
    public Object visit(ASTEqualityExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTInstanceOfExpression, java.lang.Object)
     */
    public Object visit(ASTInstanceOfExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTRelationalExpression, java.lang.Object)
     */
    public Object visit(ASTRelationalExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTShiftExpression, java.lang.Object)
     */
    public Object visit(ASTShiftExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAdditiveExpression, java.lang.Object)
     */
    public Object visit(ASTAdditiveExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTMultiplicativeExpression, java.lang.Object)
     */
    public Object visit(ASTMultiplicativeExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTUnaryExpression, java.lang.Object)
     */
    public Object visit(ASTUnaryExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPreIncrementExpression, java.lang.Object)
     */
    public Object visit(ASTPreIncrementExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPreDecrementExpression, java.lang.Object)
     */
    public Object visit(ASTPreDecrementExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTUnaryExpressionNotPlusMinus, java.lang.Object)
     */
    public Object visit(ASTUnaryExpressionNotPlusMinus node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPostfixExpression, java.lang.Object)
     */
    public Object visit(ASTPostfixExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTCastExpression, java.lang.Object)
     */
    public Object visit(ASTCastExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPrimaryExpression, java.lang.Object)
     */
    public Object visit(ASTPrimaryExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPrimaryPrefix, java.lang.Object)
     */
    public Object visit(ASTPrimaryPrefix node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTPrimarySuffix, java.lang.Object)
     */
    public Object visit(ASTPrimarySuffix node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTLiteral, java.lang.Object)
     */
    public Object visit(ASTLiteral node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTBooleanLiteral, java.lang.Object)
     */
    public Object visit(ASTBooleanLiteral node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTNullLiteral, java.lang.Object)
     */
    public Object visit(ASTNullLiteral node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTArguments, java.lang.Object)
     */
    public Object visit(ASTArguments node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTArgumentList, java.lang.Object)
     */
    public Object visit(ASTArgumentList node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAllocationExpression, java.lang.Object)
     */
    public Object visit(ASTAllocationExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTArrayDimsAndInits, java.lang.Object)
     */
    public Object visit(ASTArrayDimsAndInits node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTStatement, java.lang.Object)
     */
    public Object visit(ASTStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTLabeledStatement, java.lang.Object)
     */
    public Object visit(ASTLabeledStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTBlock, java.lang.Object)
     */
    public Object visit(ASTBlock node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTBlockStatement, java.lang.Object)
     */
    public Object visit(ASTBlockStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTLocalVariableDeclaration, java.lang.Object)
     */
    public Object visit(ASTLocalVariableDeclaration node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTEmptyStatement, java.lang.Object)
     */
    public Object visit(ASTEmptyStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTStatementExpression, java.lang.Object)
     */
    public Object visit(ASTStatementExpression node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTSwitchStatement, java.lang.Object)
     */
    public Object visit(ASTSwitchStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTSwitchLabel, java.lang.Object)
     */
    public Object visit(ASTSwitchLabel node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTIfStatement, java.lang.Object)
     */
    public Object visit(ASTIfStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTWhileStatement, java.lang.Object)
     */
    public Object visit(ASTWhileStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTForStatement, java.lang.Object)
     */
    public Object visit(ASTForStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTForInit, java.lang.Object)
     */
    public Object visit(ASTForInit node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTDoStatement, java.lang.Object)
     */
    public Object visit(ASTDoStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTStatementExpressionList, java.lang.Object)
     */
    public Object visit(ASTStatementExpressionList node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTForUpdate, java.lang.Object)
     */
    public Object visit(ASTForUpdate node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTBreakStatement, java.lang.Object)
     */
    public Object visit(ASTBreakStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTContinueStatement, java.lang.Object)
     */
    public Object visit(ASTContinueStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTReturnStatement, java.lang.Object)
     */
    public Object visit(ASTReturnStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTThrowStatement, java.lang.Object)
     */
    public Object visit(ASTThrowStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTSynchronizedStatement, java.lang.Object)
     */
    public Object visit(ASTSynchronizedStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTTryStatement, java.lang.Object)
     */
    public Object visit(ASTTryStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

    /* (non-Javadoc)
     * @see net.sourceforge.jrefactory.parser.JavaParserVisitor#visit(net.sourceforge.jrefactory.ast.ASTAssertionStatement, java.lang.Object)
     */
    public Object visit(ASTAssertionStatement node, Object data) {
        // TODO Auto-generated method stub
        return node.childrenAccept(this, data);
    }

}
