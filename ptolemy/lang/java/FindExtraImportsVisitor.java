/* A visitor that reports or eliminates unnecessary import statements.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** A visitor that reports or eliminates unnecessary import statements.
 *  This visitor may not remove all unnecessary import statements, namely
 *  if fully qualified type names are used in the code, but it should not
 *  remove import statements that are necessary.
 *
 *  @author Jeff Tsay
 */
public class FindExtraImportsVisitor extends ReplacementJavaVisitor
    implements JavaStaticSemanticConstants {

    /** Creates a FindExtraImportsVisitor. If remove is true, extra imports
     *  will be discarded from the AST. Extra imports will be reported to by
     *  writing to System.out.
     */
    public FindExtraImportsVisitor(boolean remove) {
        this(remove, System.out);
    }

    /** Creates a FindExtraImportsVisitor. If remove is true, extra imports
     *  will be discarded from the AST. If out is not null, extra imports
     *  will be reported to by writing to out.
     */
    public FindExtraImportsVisitor(boolean remove, PrintStream out) {
        _remove = remove;
        _out = out;
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _filename = (String) node.getDefinedProperty(IDENT_KEY);

        // figure what types are being used
        TNLManip.traverseList(this, null, node.getDefTypes());

        // report extra imports
        List importList =
            TNLManip.traverseList(this, null, node.getImports());

        // remove extra imports if desired
        if (_remove) {
            LinkedList neededImports = new LinkedList();

            Iterator importItr = importList.iterator();

            while (importItr.hasNext()) {
                Object obj = importItr.next();

                if (obj != NullValue.instance) {
                    // import was needed, and returned
                    neededImports.addLast(obj);
                }

                node.setImports(neededImports);
            }
        }

        return node;
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {
        NameNode name = node.getName();
        ClassDecl classDecl = (ClassDecl) JavaDecl.getDecl(name);

        // check if we used the type
        if (!_usedUserTypesSet.contains(classDecl)) {
            if (_out != null) {
                _out.println("In file " + _filename +
                        ", found unnecessary class/interface import : " +
                        StaticResolution.nameString(name));
            }

            return NullValue.instance;
        }

        return node;
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node, LinkedList args) {
        NameNode name = node.getName();
        PackageDecl pkgDecl = (PackageDecl) JavaDecl.getDecl(name);

        if (pkgDecl == StaticResolution.JAVA_LANG_PACKAGE) {
            // do not remove the java.lang package
            return node;
        }

        // check if we used the package
        if (!_usedPackagesSet.contains(pkgDecl)) {
            if (_out != null) {
                _out.println("In file " + _filename +
                        ", found unnecessary package import : " +
                        StaticResolution.nameString(name));
            }

            return NullValue.instance;
        }

        return node;
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {

        NameNode name = node.getName();

        // find the declaration of the type. We cannot remove
        // statements importing the specific type

        ClassDecl classDecl = (ClassDecl) JavaDecl.getDecl(name);

        _usedUserTypesSet.add(classDecl);

        // find the package that the type is in. We cannot remove statements
        // importing the specific package

        PackageDecl pkgDecl = (PackageDecl) _packageOfType(classDecl);

        _usedPackagesSet.add(pkgDecl);

        return node;
    }

    /** Given the ClassDecl, return the package to which the type belongs. */
    protected PackageDecl _packageOfType(ClassDecl classDecl) {
        JavaDecl container = classDecl.getContainer();

        if (container == null) {
            ApplicationUtility.error("user type " + classDecl.getName() +
                    " is has no container.");
        }

        switch (container.category) {
        case CG_PACKAGE:
            return (PackageDecl) container;

        case CG_CLASS:
        case CG_INTERFACE:
            // the class is an inner class, return the package of the outer class
            return  _packageOfType((ClassDecl) container);
        }

        ApplicationUtility.error("container of class " + classDecl.getName() +
                " is not a package nor a user type");

        return null;
    }

    protected boolean _remove;
    protected PrintStream _out;
    protected String _filename;

    protected HashSet _usedUserTypesSet = new HashSet();
    protected HashSet _usedPackagesSet = new HashSet();
}
