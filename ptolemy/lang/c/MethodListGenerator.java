/*
A class that extracts ordered lists of method declarations
with an ordering convention that is geared towards backed 
code generation via function pointers (e.g., for C code generation).

Copyright (c) 2001 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.lang.c;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.TreeNode;
import ptolemy.lang.java.*;
import ptolemy.lang.java.nodetypes.*;

/** A class that extracts ordered lists of method declarations
 *  with an ordering convention that is geared towards back-end 
 *  code generation via function pointers (e.g., for C code generation).
 *  Specifically, the class extracts the list of all new public 
 *  and protected methods that are defined
 *  for a given class (i.e., new method definitions, excluding definitions
 *  that override methods in super classes); the list of 
 *  inherited methods; and the list of 
 *  private methods.  For sub-classes, overridden methods in the 
 *  inherited methods list are replaced with the overriding definitions. 
 *  Thus, the inherited methods list is the list of 'active' definitions
 *  in the current scope whose declarations originated in superclasses.
 *
 *  For example, consider the follwing class/method combinations:
 *  class C1 (base class), public methods m1, m2, m3; private method p1
 *  class C2 (extends C1), public methods m4, m5 (overrides m2)
 *  class C3 (extends C2), public methods m6, m7; private methods p1, p2
 *
 *  Then the inherited methods list generated for C3 is
 *      m1, m5, m3, m4;
 *  the private list generated for C3 is
 *      p1, p2;
 *  the inherited methods list generated for C2 is
 *      m1, m5, m3;
 *  and the new methods list generated for C2 is
 *      m4.
 *
 *  (note that m5 and m2 have the same name and type signature, but
 *  different declarations).
 *
 *  If function pointers are declared according to these orderings
 *  in the translated type definitions associated with C1, C2, and C3,
 *  then virtual functions can be implemented correctly.
 *
 *  The lists constructed by this class are lists of method declarations.
 *  That is, each element is of type MethodDecl.
 * 
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 *
 */

public class MethodListGenerator {
 
    /* Instantiate a method list generator with an empty set of method
       lists. */
    public MethodListGenerator() {
    }

    /* Generate the method list for the specified class declaration. */ 
    public MethodListGenerator(ClassDeclNode node) {
        generate(node);
    }

    /** Return the list of inherited methods. */
    public LinkedList getInheritedMethods() {
        return _inheritedList;
    }

    /** Return the list of new methods. */
    public LinkedList getNewMethods() {
        return _newList;
    }

    /** Return the list of private methods. */
    public LinkedList getPrivateMethods() {
        return _privateList;
    }

    /** Given a class declaration node, construct the lists
     *  of inherited methods, new public and protected methods,
     *  and private methods. Recursively include methods from
     *  super classes in the inherited methods list.
     *  This visitation method is the main "entry point" for method
     *  list generation from a given class file.
     *  @param node The AST node of the Java class declaration. 
     */
    public void generate(ClassDeclNode node) {
        _newList = new LinkedList();
        _privateList = new LinkedList();
        ClassDecl classDeclaration = null; 
        JavaDecl declaration = JavaDecl.getDecl((TreeNode)node);
        if (!(declaration instanceof ClassDecl)) 
            CCodeGenerator.nodeVisitationError(node,
                    "Could not resolve class declaration");
        else {
            classDeclaration = (ClassDecl)declaration;
            ClassDecl superClassDeclaration = classDeclaration.getSuperClass();
            if (superClassDeclaration != null) {
                TreeNode superClassNode = 
                        superClassDeclaration.getSource();
            if (!(superClassNode instanceof ClassDeclNode)) 
                CCodeGenerator.nodeVisitationError(node,
                        "Could not obtain valid source of super class.");
                MethodListGenerator generator = 
                        new MethodListGenerator((ClassDeclNode)superClassNode);
                _inheritedList = generator.getInheritedMethods(); 
            }
            else _inheritedList = new LinkedList();

            // For each method declaration of the Java class, append the
            // the declared method to the appropriate method list. 
            List members = node.getMembers();
            if (members == null) return;
            Iterator membersIter = members.iterator();
            while (membersIter.hasNext()) {
                Object member = membersIter.next();
                if (member instanceof MethodDeclNode) 
                    _addMethod((MethodDeclNode)member);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private methods                          ////

    /** Given a method declaration node, append the associated
     *  declaration to the appropriate methods list ('new' or 'private') 
     *  if the method does not override another defined method.
     *  In case of an overriding method, replace the overrided
     *  method from the inherited methods list with this overriding
     *  method.
     *  @param node The AST node corresponding to a method declaration.
     *  @exception If the method is an overridding method but the
     *  declaration of the overridden method is not in the relevant
     *  methods list.
     */
    private void _addMethod(MethodDeclNode node) {
        MethodDecl methodDeclaration = null; 
        JavaDecl declaration = JavaDecl.getDecl((TreeNode)node);
        if (!(declaration instanceof MethodDecl)) 
            CCodeGenerator.nodeVisitationError(node,
                    "Could not resolve method call");
        else {
            methodDeclaration = (MethodDecl)declaration;
            MethodDecl overrides = methodDeclaration.getOverrides();
       
            // FIXME: this assumes the method is public or protected.
            if (overrides == null) _newList.add(methodDeclaration);
            else {
                int overrideIndex;
                if ((overrideIndex = _inheritedList.indexOf(overrides)) == -1)
                    CCodeGenerator.nodeVisitationError(node, 
                        "Unresolved method override. The "
                        + "method name is: " + methodDeclaration.fullName());
                else _inheritedList.set(overrideIndex, methodDeclaration);
            }
            
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                  private variables                        ////

    // The list of inherited method declarations for
    // the class processed for method list generation. Overridden in this list
    // are replaced with the overriding definitions. 
    private LinkedList _inheritedList = null;

    // The list of new and non-overriding public and protected method 
    // declarations. These are new methods and/or type signatures that
    // are defined by the given class. 
    private LinkedList _newList = null;

    // The list of private methods defined by the given class.
    private LinkedList _privateList = null;

}
