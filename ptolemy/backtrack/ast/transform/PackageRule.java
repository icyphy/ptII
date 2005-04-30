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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import ptolemy.backtrack.ast.LocalClassLoader;
import ptolemy.backtrack.ast.Type;
import ptolemy.backtrack.ast.TypeAnalyzer;
import ptolemy.backtrack.ast.TypeAnalyzerState;

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
        if (_prefix != null && _prefix.length() > 0) {
            // Add a prefix to each name node, if necessary.
            root.accept(new Renamer(analyzer.getState()));
            
            PackageDeclaration packageDeclaration = root.getPackage();
            AST ast = root.getAST();
            if (packageDeclaration == null) {
                packageDeclaration = ast.newPackageDeclaration();
                packageDeclaration.setName(
                        AbstractTransformer.createName(ast, _prefix));
                root.setPackage(packageDeclaration);
            }
        }
    }

    /**
     *  @param analyzer
     *  @param root
     */
    public void beforeTraverse(TypeAnalyzer analyzer, CompilationUnit root) {
    }

    private void _addImport(LocalClassLoader loader, Name name,
            String oldPackageName) {
        CompilationUnit root = (CompilationUnit)name.getRoot();
        AST ast = name.getAST();
        String className = name.toString();
        String fullName = oldPackageName + "." + className;

        boolean transform = true;

        // Try to load the class within the package.
        try {
            loader.loadClass(fullName);
        } catch (ClassNotFoundException e) {
            transform = false;
        } catch (NoClassDefFoundError e) {
            transform = false;
        }

        // Check conflict.
        if (transform) {
            Iterator classesIter = loader.getImportedClasses().iterator();
            while (classesIter.hasNext()) {
                LocalClassLoader.ClassImport classImport =
                    (LocalClassLoader.ClassImport)classesIter.next();
                if (classImport.getClassName().equals(className)) {
                    transform = false;
                    break;
                }
            }

            if (transform) {
                ImportDeclaration importDeclaration =
                    ast.newImportDeclaration();
                importDeclaration.setName(AbstractTransformer.createName(ast, fullName));
                root.imports().add(importDeclaration);
                loader.importClass(fullName);
            }
        }
    }

    private Name _addPrefix(Name name, String prefix) {
        AST ast = name.getAST();
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

        Renamer(TypeAnalyzerState state) {
            _state = state;
            _crossAnalysisTypes = state.getCrossAnalyzedTypes();
            _crossAnalysisNames = new HashSet();
            Iterator crossAnalysisIter = _crossAnalysisTypes.iterator();
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
            if (node.getParent() != null) {
                String id = node.toString();
                if (id.equals("ptolemy.backtrack.util.java.util.Random")) {
                    int i = 10;
                }

                Type type = Type.getType(node);
                Type owner = Type.getOwner(node);
                String fullName;

                boolean convert = false;
                boolean addImport = false;

                if (type != null && owner == null &&
                        _crossAnalysisTypes.contains(type.getName()) &&
                        _crossAnalysisNames.contains(id) &&
                        type.getName().length() == id.length()) {
                    fullName = type.getName();
                    convert = true;
                } else if (node.getParent() instanceof ImportDeclaration &&
                        _crossAnalysisNames.contains(id)) {
                    fullName = id;
                    convert = true;
                } else if (node.getParent() instanceof PackageDeclaration) {
                    fullName = id;
                    convert = true;
                    _oldPackageName = node.toString();
                } else
                    fullName = null;

                if (type != null && owner == null &&
                        node instanceof SimpleName &&
                        !_crossAnalysisTypes.contains(type.getName()) &&
                        _oldPackageName != null)
                    addImport = true;

                if (convert) {
                    Name newName;
                    if (ConstructorTransformer.SPECIAL_TYPE_MAPPING.
                            containsKey(fullName))
                        newName = AbstractTransformer.createName(node.getAST(),
                                (String)ConstructorTransformer.
                                SPECIAL_TYPE_MAPPING.get(fullName));
                    else
                        newName = _addPrefix(node, _prefix);
                    if (newName != null) {
                        AbstractTransformer.replaceNode(node, newName);
                        _specialTypes.add(newName.toString());
                    }
                }

                if (addImport)
                    _addImport(_state.getClassLoader(), node, _oldPackageName);

                if (!convert && !(node.getParent() instanceof Name)) {
                    Iterator specialTypesIter = _specialTypes.iterator();
                    while (specialTypesIter.hasNext()) {
                        String specialType = (String)specialTypesIter.next();
                        String simpleType =
                            specialType.substring(specialType.lastIndexOf(".") + 1);
                        String newId = null;
                        if (id.startsWith(specialType + "."))
                            newId = id.substring(specialType.length() + 1 - simpleType.length());
                        else if (id.equals(specialType))
                            newId = id.substring(specialType.length() - simpleType.length());
                        if (newId != null)
                            AbstractTransformer.replaceNode(node,
                                    AbstractTransformer.createName(
                                            node.getAST(), newId));
                    }
                }
            }
        }

        private Set _crossAnalysisTypes;

        private Set _crossAnalysisNames;

        private String _oldPackageName;

        private TypeAnalyzerState _state;

        private List _specialTypes = new LinkedList();
    }

    private String _prefix;
}
