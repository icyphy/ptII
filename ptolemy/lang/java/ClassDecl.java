/* A declaration of a class or interface in Java.

Copyright (c) 1998-2001 The Regents of the University of California.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// ClassDecl
/** A declaration of a class or interface in Java.
<p>
Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.

@author Jeff Tsay, Shuvra S. Bhattacharyya 
@version $Id$
 */
public class ClassDecl extends TypeDecl implements JavaStaticSemanticConstants {

    public ClassDecl(String name, JavaDecl container) {
        this(name, CG_CLASS, null, 0, null, container);

        _defType = _invalidType(this, name);
    }

    public ClassDecl(String name, int category0, TypeNameNode defType,
            int modifiers, TreeNode source, JavaDecl container) {
        super(name, category0, defType);
        _modifiers = modifiers;
        _source = source;
        _container = container;
    }

    /** Invalidate the class so that its scope and source are re-read
     *  next time they are needed.
     */
    public void invalidate() {
        removeVisitor(ResolveClassVisitor.visitorClass());
        removeVisitor(ResolveInheritanceVisitor.visitorClass());

        _scope = null;

        _source = null;
    }

    /** Invalidate the scope so that it is re-built the next time it is needed */
    public void invalidateScope() {
        removeVisitor(ResolveClassVisitor.visitorClass());
        _scope = null; 
    }

    public final boolean hasScope() { return true; }

    public final Scope getScope() {
        if (!wasVisitedBy(ResolveClassVisitor.visitorClass())) {
            if (StaticResolution.traceLoading)
                System.out.println("getScope() for " + _name + ": building scope");
            try {
                _buildScope();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build a scope: e");
            }
        }
        //System.out.println("getScope() for " + _name + ": scope already in place");
        return _scope;
    }

    public final Scope getTypeScope() {
        return _scope;
    }

    public final void setScope(Scope scope) {
        _scope = scope;
    }

    public final boolean hasModifiers() {
        return true;
    }

    public final int getModifiers() {
        return _modifiers;
    }

    public final void setModifiers(int modifiers) {
        _modifiers = modifiers;
    }

    public final boolean hasSource() {
        return true;
    }

    public final TreeNode getSource() {
        return _source;
    }

    public final void setSource(TreeNode source) {
        _source = source;
    }

    public final boolean hasContainer() {
        return true;
    }

    public final JavaDecl getContainer() {
        return _container;
    }

    public final void setContainer(JavaDecl container) {
        _container = container;
    }

    public List getInterfaces() {
        return _interfaces;
    }

    public void setInterfaces(List interfaces) {
        _interfaces = interfaces;
    }

    /** Ensure that the source code for this ClassDecl is loaded, and pass 0
     *  resolution is completed on it.
     *  If the source already has been loaded, do nothing.  Otherwise, if the
     *  class is a JVM system class, or a Ptolemy class, or we can load 
     *  the class file using a class loader, then attempt to perform shallow loading
     *  of the AST (we must first ensure that the class file can be loaded
     *  in case we need to perform shallow-to-deep conversion later).
     *  If we cannot load the class file, parse the Java source to obtain a full AST.
     *  @exception IOException If we can't get the canonical
     *  name of the test library.
     *  @exception FileNotFoundException If we can't find the source file.
     */
    public void loadSource() throws IOException {
        if (_source == null) {
            _source = AbsentTreeNode.instance;

            String fileName = fullName('.');

            if (SearchPath.systemClassSet.contains(fileName)
                    || SearchPath.ptolemyCoreClassSet.contains(fileName)){
                // The class is either a JVM system class or Ptolemy core class
                if (StaticResolution.traceLoading) {
                    System.out.println("ClassDecl.loadSource: Reading in Java/Ptolemy"
                            + " type : '" + fileName + "' by loadClassName");
                }
                StaticResolution.loadClassName(fileName, 0);
                if (_source == AbsentTreeNode.instance) {
                    throw new RuntimeException("Could not load " + fullName());
                }
            } else {
                // We are dealing with a user-defined class. First, see if we
                // can load it in shallowly. If not, load in a full AST
                // by parsing the Java source.
 
                if (StaticResolution.traceLoading) 
                    System.out.println("ClassDecl.loadSource: Reading in user type : '" 
                            + fullName()); 
                Class loadedClass = null;

                if (StaticResolution.shallowLoadingEnabled() &&
                        StaticResolution.enableDeepUserASTs) {
                    // First, make sure that the class is already loaded or that
                    // we can load the class. Otherwise,
                    // we will have problems if we later have to convert to a deep AST.
                    try {
                        loadedClass = Class.forName(fullName());
                    } catch (Exception exception) {}
                    if (loadedClass == null) {
                        // Try to load the class.
                        JavaClassLoader classLoader = new JavaClassLoader();
                        if (StaticResolution.traceLoading) 
                            System.out.println("loadSource: "
                                    + "trying to load class '" + fullName() + "'"
                                    + " with a JavaClassLoader");
                        try {
                            loadedClass = classLoader.loadClass(fullName());
                        } catch (ClassNotFoundException exception) {
                            System.out.println("loadSource: could not load class");
                        }
                    }
                    if (loadedClass != null)  {
                        // Generate a shallow AST
                        CompileUnitNode loadedAST = 
                                ASTReflect.ASTCompileUnitNode(loadedClass);
                        _source = NodeUtil.getDefinedType(loadedAST);
                        if (loadedAST == null)
                            throw new NullPointerException("ClassDecl.loadSource: "
                                    + "loaded AST for " + fullName() + " is null even " 
                                    + "though the corresponding class (" 
                                    + loadedClass.getClass().getName() 
                                    + ")was successfully loaded.");
                        else {
                            // Register the loaded class, and perform pass 0 resolution.
                            _source = NodeUtil.getDefinedType(loadedAST);
                            loadedAST.setProperty(IDENT_KEY, fullName());
                            JavaParserManip.allParsedMap.put(fullName(), loadedAST); 
                            StaticResolution.loadCompileUnit(loadedAST, 0);
                        }
                    }
                }  // If shallow loading is enabled.
                if (loadedClass == null) {
                    // We cannot hope to use reflection, so parse the Java source.
                    if (StaticResolution.traceLoading) 
                    System.out.println("loadSource: calling loadFile "
                                + "to perform a full AST load of " + fullName());
                    // openSource() might throw IOException if we can't
                    // get the canonical name of fileName.
                    File file = _pickLibrary(_container).openSource(fileName);

                    // The following should set the source
                    StaticResolution.loadFile(file, 0, fullName()); 
                }
                if ((_source == null) || (_source == AbsentTreeNode.instance)) {
                    throw new RuntimeException("file " + fileName +
                            " doesn't contain class or interface " +
                            fullName());
                }
            } // dealing with a user-defined class
            if (StaticResolution.traceLoading)
                System.out.println("ClassDecl.loadSource: done reading in class " 
                        + fullName());
        } // if (_source == null)
    }

    public ClassDecl getSuperClass() {
        return _superClass;
    }

    public void setSuperClass(ClassDecl superClass) {
        _superClass = superClass;
    }

    /** Get a string representation of the scope of this class and recursively, 
     *  of all super classes. The string representation includes (in parentheses)
     *  the loading mode (shallow, full, or deep) of each class that is examined.
     *  This is useful for diagnostic purposes.
     *  @return The string representation of scope information.
     */
    public String getAllScopes() {
        StringBuffer scopeString = new StringBuffer();
        scopeString.append("Scope for Class " + fullName() + " ("
                + ASTReflect.getLoadingMode((UserTypeDeclNode)getSource()) + "):\n");
        scopeString.append(getScope().toString());
        ClassDecl superClass = getSuperClass();
        if (superClass != null) scopeString.append(superClass.getAllScopes());
        return scopeString.toString(); 
    }

    /** @exception IOException If we can't get the canonical
     *  name of a source file.
     *  @exception FileNotFoundException If we fail to find the source
     *  file.
     */
    protected void _buildScope() throws IOException {
        if (StaticResolution.traceLoading)
	        System.out.println("ClassDecl._buildScope(): Building scope " +
                    "(and calling loadSource) for class " + fullName());

        loadSource();

        // builds scopes for all recently loaded classes, including
	    // this one
        StaticResolution.buildScopes();

        // If class didn't load, give it a dummy scope, etc
        if (_scope == null) {
            System.err.println("Warning: class " + _name + " did not load, " +
                    "using dummy scope.");

            _scope =
		new Scope(StaticResolution.SYSTEM_PACKAGE.getScope());

            setSuperClass(StaticResolution.OBJECT_DECL);

            addVisitor(ResolveClassVisitor.visitorClass());
        }
    }

    protected static TypeNameNode _invalidType(ClassDecl self, String name) {
        NameNode nameNode = new NameNode(AbsentTreeNode.instance, name);
        nameNode.setProperty(DECL_KEY, self);

        return new TypeNameNode(nameNode);
    }

    protected int  _modifiers;
    protected TreeNode  _source;
    protected JavaDecl  _container;
    protected Scope   _scope = null;
    protected List _interfaces = new LinkedList();
    protected ClassDecl _superClass;
}
