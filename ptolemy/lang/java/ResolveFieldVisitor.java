/* 
Resolve fields, overloading, and do other random semantic checks.
Code adopted from st-field.cc from the Titanium project.

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

/** A visitor that does field and method resolution.
 *  
 *  Code and comments taken from the Titanium project.
 * 
 *  @author ctsay@eecs.berkeley.edu
 */
public class ResolveFieldVisitor extends ReplacementJavaVisitor {
    public ResolveFieldVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitVarDeclNode(VarDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitThisConstructorCallNode(ThisConstructorCallNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitInstanceInitNode(InstanceInitNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitThisFieldAccessNode(ThisFieldAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitTypeClassAccessNode(TypeClassAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitOuterThisAccessNode(OuterThisAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitCastNode(CastNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    /* The default visit method comes from ReplacementJavaVisitor. */

    protected static class FieldContext {
        public FieldContext() {}

        public FieldContext(FieldContext ctx) {
            currentClass = ctx.currentClass;
            inStatic = ctx.inStatic;
        }

        public TypeNameNode _currentClass;

        /** A flag indicating that we are in static code. */
        public boolean inStatic;
    }


    /** The current package. */
    protected PackageDecl _currentPackage = null;
}    
