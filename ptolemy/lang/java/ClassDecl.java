/* A declaration of a class or interface in Java.

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

import java.io.File;
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

    /** Invalidate the class so that its environment and source are re-read
     *  next time they are needed.
     */
    public void invalidate() {
        removeVisitor(ResolveClassVisitor.visitorClass());        
        removeVisitor(ResolveInheritanceVisitor.visitorClass());        
        
        _environ = null;
         
        _source = null;        
    }

    public final boolean hasEnviron() { return true; }

    public final Environ getEnviron() {
        if (!wasVisitedBy(ResolveClassVisitor.visitorClass())) {
           // ApplicationUtility.trace("getEnviron() for " + _name + ": building environment"); 
           _buildEnviron();
        }
        // ApplicationUtility.trace("getEnviron() for " + _name + ": environment already in place");         
        return _environ;
    }
    
    public final Environ getTypeEnviron() {
        return _environ;
    }

    public final void setEnviron(Environ environ) {
        _environ = environ;
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

    /** Ensure that the source code for this ClassDecl is loaded. */
    public void loadSource() {
        if (_source == null) {
           _source = AbsentTreeNode.instance;

           String fileName = fullName(File.separatorChar);
           File file = _pickLibrary(_container).openSource(fileName);

           ApplicationUtility.trace(">Reading in user type : " + fullName() +
            " from " + fileName);
           
           StaticResolution.load(file, 0); // should set the source
         
           if (_source == AbsentTreeNode.instance) {
	          ApplicationUtility.error("file " + fileName +
               " doesn't contain class or interface " + fullName());
           }
           
           ApplicationUtility.trace(">Done reading class " + fullName());
        }
    }

    public ClassDecl getSuperClass() {
        return _superClass;
    }

    public void setSuperClass(ClassDecl superClass) {
        _superClass = superClass;
    }

    protected void _buildEnviron() {
        ApplicationUtility.trace(">Building env for class " + fullName());

        loadSource();

        // builds environments for all recently loaded classes, including this one
        StaticResolution.buildEnvironments();

        // If class didn't load, give it a dummy environment, etc
        if (_environ == null) {
           ApplicationUtility.error("class " + _name + " did not load, " +
            "using dummy environment.");
                   
           _environ = new Environ(StaticResolution.SYSTEM_PACKAGE.getEnviron());

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
    protected Environ   _environ = null;
    protected List _interfaces = new LinkedList();
    protected ClassDecl _superClass;
}
