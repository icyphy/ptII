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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import ptolemy.backtrack.ast.Type;
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
    public void afterTraverse(TypeAnalyzer analyzer, CompilationUnit root) {
        PackageDeclaration declaration = root.getPackage();
        AST ast = root.getAST();
        if (_prefix != null && _prefix.length() > 0)
            // Add a prefix to each name node, if necessary.
            root.accept(new Renamer(analyzer.getState().getCrossAnalyzedTypes()));
    }

    /**
     *  @param analyzer
     *  @param root
     */
    public void beforeTraverse(TypeAnalyzer analyzer, CompilationUnit root) {
    }
    
    private Name _addPrefix(AST ast, Name name, String prefix) {
        Name newName = null;
        while (name != null && name instanceof QualifiedName &&
               ! (((QualifiedName)name).getQualifier() instanceof SimpleName))
               name = ((QualifiedName)name).getQualifier();
        int lastPosition = prefix.length() - 1;
        while (lastPosition >= 0) {
            int dotPosition = prefix.lastIndexOf('.', lastPosition);
            String part = dotPosition == -1 ?
                    prefix.substring(0, lastPosition + 1) :
                    prefix.substring(dotPosition + 1, lastPosition + 1);
            lastPosition = dotPosition - 1;
            if (name == null) {
                name = ast.newSimpleName(part);
                newName = name;
            } else if (name instanceof SimpleName) {
                name = ast.newQualifiedName(
                        ast.newSimpleName(part), 
                        ast.newSimpleName(
                                ((SimpleName)name).getIdentifier()));
                newName = name;
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
        return newName;
    }
    
    private class Renamer extends ASTVisitor {
        
        Renamer(Set crossAnalysis) {
            _crossAnalysisTypes = crossAnalysis;
            _crossAnalysisNames = new HashSet();
            Iterator crossAnalysisIter = crossAnalysis.iterator();
            while (crossAnalysisIter.hasNext()) {
                String className = (String)crossAnalysisIter.next();
                _crossAnalysisNames.add(className.replace('$', '.'));
            }
        }
        
        public void endVisit(SimpleName node) {
            _handleName(node);
        }
        
        public void endVisit(QualifiedName node) {
            _handleName(node);
        }
        
        private void _handleName(Name node) {
            if (node.getParent() != null &&
                    !(node.getParent() instanceof Name)) {
                String id = node.toString();
                boolean convert = false;
                Type type = Type.getType(node);

                if (type != null &&
                        _crossAnalysisTypes.contains(type.getName()) &&
                        _crossAnalysisNames.contains(id))
                    convert = true;
                else if (node.getParent() instanceof ImportDeclaration &&
                        _crossAnalysisNames.contains(id))
                    convert = true;
                else if (node.getParent() instanceof PackageDeclaration)
                    convert = true;
                    
                if (convert) {
                    Name newName = _addPrefix(node.getAST(), node, _prefix);
                    if (newName != null)
                        AbstractTransformer.replaceNode(node, newName);
                }
            }
        }
        
        private Set _crossAnalysisTypes;
        
        private Set _crossAnalysisNames;
    }

    private String _prefix;
}
