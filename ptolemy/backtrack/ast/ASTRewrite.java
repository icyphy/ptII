/* A set of AST rewriting functions.

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

import java.util.List;

import net.sourceforge.jrefactory.ast.ASTCompilationUnit;
import net.sourceforge.jrefactory.ast.ASTImportDeclaration;
import net.sourceforge.jrefactory.ast.ASTName;
import net.sourceforge.jrefactory.ast.ASTPackageDeclaration;
import net.sourceforge.jrefactory.ast.SimpleNode;
import net.sourceforge.jrefactory.factory.NameFactory;
import net.sourceforge.jrefactory.parser.JavaParserTreeConstants;
import net.sourceforge.jrefactory.parser.Token;

//////////////////////////////////////////////////////////////////////////
//// ASTRewrite
/**
   An AST (Abstract Syntax Tree) represents the information abstracted from
   a source program. This class provides a set of general methods to rewrite
   (or transform) ASTs. These methods are all static and stateless. They can
   be applied to any AST obtained from the JRefactory parser.
   <p>
   AST rewriting refers to modifying the AST by means of cutting and
   grafting. The modification preserves correctness of the AST, but changes
   the behavior of the Java program the AST represents.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (tfeng)
*/
public class ASTRewrite {
    
    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Add an import statement to the head of the Java program represented
     *  by the AST. The position of the import statement among the others
     *  is arbitrary, because Java semantics does not require any
     *  well-defined order among import statements.
     *  <p>
     *  The order of importations can be made explicit in the pretty-printer
     *  that transforms the AST back to Java source code.
     *  <p>
     *  When <tt>className</tt> is <tt>null</tt>, the importation looks like
     *  "<tt>packageName.*</tt>", otherwise, the importation is a
     *  single-class importation like "<tt>packageName.className</tt>".
     *  
     *  @param cu The compilation unit to be transformed. A compilation unit
     *   is the root of an AST, which represents all the information
     *   obtained from a single Java file (with <tt>.java</tt> postfix).
     *  @param packageName The name of the package to be imported.
     *  @param className The simple name of the class to be imported.
     */
    public static void addImport(ASTCompilationUnit cu, String packageName, String className) {
        ASTName importName = NameFactory.getName(packageName, className);

        ASTImportDeclaration importNode =
            new ASTImportDeclaration(JavaParserTreeConstants.JJTIMPORTDECLARATION);
        importNode.addSpecial("import", new Token());
        importNode.jjtAddFirstChild(importName);
        
        cu.jjtInsertChild(importNode, 0);
    }
    
    public static SimpleNode minimizeAST(SimpleNode node) {
        if (node.jjtGetNumChildren() == 1) {
            if ((node.getClass().getName().contains("Expression") ||
                    node.getClass().getName().endsWith("Prefix") ||
                    node.getClass().getName().endsWith("Suffix")) &&
                    node.specials == null &&
                    node.getName().length() == 0 &&
                    node.getImage().length() == 0) {

                SimpleNode child = (SimpleNode)node.jjtGetFirstChild();
                SimpleNode parent = (SimpleNode)node.jjtGetParent();
                if (parent == null)
                    return minimizeAST(child);
                else {
                    for (int i=0; i<parent.jjtGetNumChildren(); i++)
                        if (parent.jjtGetChild(i) == node) {
                            parent.jjtDeleteChild(i);
                            child = minimizeAST(child);
                            parent.jjtInsertChild(child, i);
                            break;
                        }
                    return child;
                }
            }
        }
        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode)node.jjtGetChild(i);
            SimpleNode cchild = minimizeAST(child);
            if (child != cchild) {
                node.jjtDeleteChild(i);
                node.jjtInsertChild(cchild, i);
            }
        }
        return node;
    }

    /** Modify the package declaration of the AST. This rewrite rule
     *  is used to move the class(es) in a Java file from one package
     *  to another.
     *  
     *  @param cu The compilation unit to be transformed. A compilation unit
     *   is the root of an AST, which represents all the information
     *   obtained from a single Java file (with <tt>.java</tt> postfix).
     *  @param packageName The name of the package to be moved to.
     */
    public static void modifyPackage(ASTCompilationUnit cu, String packageName) {
        List packages = cu.findChildrenOfType(ASTPackageDeclaration.class);
        ASTPackageDeclaration pkg;
        if (packages.isEmpty()) {
            pkg = new ASTPackageDeclaration(JavaParserTreeConstants.JJTPACKAGEDECLARATION);
            ASTName pkgName = NameFactory.getName(packageName, null);
            pkg.jjtAddFirstChild(pkgName);
            cu.jjtAddFirstChild(pkg);
        } else {
            pkg = (ASTPackageDeclaration)packages.get(0);
            ASTName name = (ASTName)pkg.jjtGetFirstChild();
            name.setImage(packageName);
        }
    }
}
