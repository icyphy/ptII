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

@author Jeff Tsay
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

    public final boolean hasScope() { return true; }

    public final Scope getScope() {
        if (!wasVisitedBy(ResolveClassVisitor.visitorClass())) {
            //System.out.println("getScope() for " + _name + ": building scope");
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

    /** Ensure that the source code for this ClassDecl is loaded.
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
                //System.out.println("ClassDecl: Reading in user type : " +
                //        fileName + " by loadClassName");
                StaticResolution.loadClassName(fileName, 0);
                if (_source == AbsentTreeNode.instance) {
                    throw new RuntimeException("Could not load " +
                            fullName());
                }
            } else {

                // openSource() might throw IOException if we can't
                // get the canonical name of fileName.
                File file = _pickLibrary(_container).openSource(fileName);

                //System.out.println("ClassDecl: Reading in user type : " +
                //        fullName() + " from " + fileName);

                StaticResolution.loadFile(file, 0); // should set the source

                if (_source == AbsentTreeNode.instance) {
                    throw new RuntimeException("file " + fileName +
                            " doesn't contain class or interface " +
                            fullName());
                }
            }
            //System.out.println(">Done reading class " + fullName());
        }
    }

    public ClassDecl getSuperClass() {
        return _superClass;
    }

    public void setSuperClass(ClassDecl superClass) {
        _superClass = superClass;
    }

    /** @exception IOException If we can't get the canonical
     *  name of a source file.
     *  @exception FileNotFoundException If we fail to find the source
     *  file.
     */
    protected void _buildScope() throws IOException {
	//System.out.println("ClassDecl._buildScope(): Building scope " +
        //				 "for class " + fullName());
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
