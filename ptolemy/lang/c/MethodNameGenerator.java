/*
A JavaVisitor that generates unique method names to be used
when translating Java method declarations into C function
declarations.

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

import ptolemy.lang.java.nodetypes.*;
import ptolemy.lang.java.JavaVisitor;
import ptolemy.lang.java.ASTReflect;
import ptolemy.lang.TreeNode;

import java.util.LinkedList;
import java.util.List;


/** A JavaVisitor that generates unique method names to be used
 *  when translating Java method declarations into C function
 *  declarations. This visitor does not generate code; it
 *  is an intermediate pass. It requires that static resolution has been
 *  performed beforehand. It sets the C_NAME_KEY property of
 *  MethodDeclNodes to the respective unique names that are assigned
 *  to them. 
 *
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 *
 */


public class MethodNameGenerator extends JavaVisitor implements CCodeGeneratorConstants {
    public MethodNameGenerator() {
        super(TM_SELF_FIRST);
    }

    /** Reset an internal counter that is used to generate unique names.
     *  This insures that a given class file will have the same resulting unique
     *  method names every time it is processed by this visitor.
     *  Also, save the fully qualified class name to use as a prefix
     *  for unique names that are generated through this visitation.
     *  The generated names are unique within the scope of the given
     *  compilation unit. There is no guarantee that they won't collide
     *  names defined in other compilation units, although, attempts
     *  are made to minimize the probability of collisions.
     *
     *  @param node The abstract syntax tree node for a compilation unit
     *  for which unique method names must be generated.
     *  @param args Visitor arguments (unused).
     *  @return A null object is returned.
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _uniqueNameIndex = 0;
        _className = ASTReflect.getFullyQualifiedName(node);

        // Sanitize for C compilation.
        _className = _className.replace('.', '_');  
        return null;
    }

    /** Derive a unique name for a method that is to be used as the
     *  name of the C function that implements the method. This is
     *  needed to resolve overloaded method names in Java into unique function
     *  names in C. The unique name is set as the value of
     *  the C_NAME_KEY property of the given abstract syntax tree node.
     *
     *  @param node The abstract syntax tree node for the given method 
     *  declaration. 
     *  @param args Visitor arguments (unused).
     *  @return A null object is returned.
     *  
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {

        String methodName = _uniqueMethodName(node.getName().getIdent());
        node.setProperty(C_NAME_KEY, methodName);
        return null;
    }
    
    /** Derive a unique name for a constructor that is to be used as the
     *  name of the C function that implements the constructor. This is
     *  needed to resolve overloaded constructor names in Java 
     *  into unique function names in C. The unique name is set as the value of
     *  the C_NAME_KEY property of the given abstract syntax tree node.
     *
     *  @param node The abstract syntax tree node for the given constructor 
     *  declaration. 
     *  @param args Visitor arguments (unused).
     *  @return A null object is returned.
     *  
     */
    public Object visitConstructorDeclNode(
            ConstructorDeclNode node, LinkedList args) {
        String methodName = _uniqueMethodName(node.getName().getIdent());
        node.setProperty(C_NAME_KEY, methodName);
        return null;
    }

    /** The default visitation method for this visitor does nothing,
     *  and returns a null object.
     * 
     *  @param node The abstract syntax tree node being visited. 
     *  @param args Visitor arguments (unused).
     *  @return A null object is returned.
     */
    public Object _defaultVisit(TreeNode node, LinkedList args) {
        return null; 
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a unique method name based on a given method name. 
     *  The unique name is derived from the fully qualified class name,
     *  the method name, and a unique integer identifier. If the
     *  resulting string is too long, then a hash code is used to
     *  to compress the unique name, and the method name, by itself,
     *  is appended to improve readability.
     *  @param methodName The method name that is to be converted into a 
     *  unique method name. 
     *  @return The unique method name.
     */
    private String _uniqueMethodName(String methodName) {
        String temp = _className + '_' + methodName + "_" + _uniqueNameIndex++;

        // FIXME: Make sure the result string fits within the number
        // distinguishable characters in C identifier.
        if (temp.length() > MAX_IDENTIFIER_CHARACTERS) {

            // Compress the unique identifier using a hash function.
            // Place the hash code in the beginning of the identifier,
            // and the method name at the end. That way, if the name
            // gets truncated (e.g., due to limits on the number of 
            // distinguished characters by the linker), the unique
            // part will be preserved as much as possible. Also, prepend the
            // the first character of the package name so we get a
            // valid C identifier (C identifers cannot start with numbers).
            Integer prefixCode = new Integer(temp.hashCode());
            temp = temp.charAt(0) + prefixCode.toString().replace('-', '0')
                    + "_" + methodName;
        }

        return temp;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The fully-qualified name of the class for which unique method
     *  names are being generated.
     */  
    private String _className = "";

    /** An index that is incremented to expedite the search for a unique
     *  name by the uniqueMethodName() method.
     */
    private int _uniqueNameIndex = 0;

}
