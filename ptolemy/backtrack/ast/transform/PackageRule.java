/* 

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.ast.transform;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import ptolemy.backtrack.ast.TypeAnalyzer;

//////////////////////////////////////////////////////////////////////////
//// PackageRule
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PackageRule extends TransformRule {
    
    public String getPrefix() {
        return _prefix;
    }
    
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }

    /**
     *  @param root
     */
    public void afterTraverse(CompilationUnit root) {
        PackageDeclaration declaration = root.getPackage();
        AST ast = root.getAST();
        if (_prefix != null) {
            Name name = declaration.getName();
            while (name != null && name instanceof QualifiedName &&
                   ! (((QualifiedName)name).getQualifier() instanceof SimpleName))
                   name = ((QualifiedName)name).getQualifier();
            int lastPosition = _prefix.length() - 1;
            while (lastPosition >= 0) {
                int dotPosition = _prefix.lastIndexOf('.', lastPosition);
                String part = dotPosition == -1 ?
                        _prefix.substring(0, lastPosition + 1) :
                        _prefix.substring(dotPosition + 1, lastPosition + 1);
                lastPosition = dotPosition - 1;
                if (name == null) {
                    name = ast.newSimpleName(part);
                    declaration.setName(name);
                } else if (name instanceof SimpleName) {
                    name = ast.newQualifiedName(
                            ast.newSimpleName(part), 
                            ast.newSimpleName(
                                    ((SimpleName)name).getIdentifier()));
                    declaration.setName(name);
                } else {
                    QualifiedName qualifiedName = (QualifiedName)name;
                    SimpleName leftPart = 
                        (SimpleName)qualifiedName.getQualifier();
                    qualifiedName.setQualifier(
                            ast.newQualifiedName(
                                    ast.newSimpleName(part), 
                                    ast.newSimpleName(
                                            leftPart.getIdentifier())));
                    name = qualifiedName.getQualifier();
                }
            }
        }
    }

    /**
     *  @param analyzer
     *  @param root
     */
    public void beforeTraverse(TypeAnalyzer analyzer, CompilationUnit root) {
    }

    private String _prefix;
}
