/*
A class that computes names of various entities to use for C code generation.

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

import ptolemy.lang.java.JavaDecl;
import ptolemy.lang.java.nodetypes.*;
import ptolemy.lang.java.JavaVisitor;
import ptolemy.lang.java.ASTReflect;
import ptolemy.lang.TreeNode;

import java.util.LinkedList;
import java.util.List;


/** A class that determines names of various entities to use for C code 
 *  generation. Methods in this class use the C_NAME_KEY property of
 *  the abstract syntax tree (AST) nodes that are referenced.
 *  When this property is not defined for a referenced AST node,
 *  the appropriate name is computed (according to the appropriate
 *  naming convention), and the C_NAME_KEY property is set to the
 *  computed name. This class helps to implement lazy evaluation of
 *  names for C code generation, as opposed to pre-processing all referenced
 *  abstract syntax trees for name generation.
 *  
 *  This class also sets AST node properties that are relevant
 *  to the generation of #include directives. Specifically, the 
 *  C_INCLUDE_FILE_KEY property for a ClassDeclNode is set to the
 *  corresponding header (.h) file name. 
 *
 *  FIXME: the following describes functionality that should
 *  be transfered elsewhere (one way to do this would be by revising
 *  HeaderFileGenerator to do self-first traversal, another way
 *  would be to implement a separate pass for computing the .h file
 *  include list): ... and 
 *  the C_IMPORT_INCLUDE_FILES_KEY
 *  properties are set for TypeNameNodes that are not contained in subtrees of
 *  BlockNodes, since the associated header files need to be referenced
 *  when generating a header file for the associated compilation unit.
 *
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 *
 */


public final class CNameGenerator implements CCodeGeneratorConstants {


    /** Get the name assigned to an abstract syntax tree (AST) node for 
     *  C code generation purposes. A null string is returned for a null
     *  node argument.
     *  @param node The AST node.
     *  @return The name assigned to the node.
     */
    public static String getCNameOf(TreeNode node) {
        if (node == null) return "";
        else {
            Object nameProperty = node.getProperty(C_NAME_KEY) ;
            if ((nameProperty == null) || !(nameProperty instanceof String)) {
                if (node instanceof TypeNode) 
                    return _getCTypeOf((TypeNode)node);
                else if (node instanceof ClassDeclNode)
                    return _getCClassNameOf((ClassDeclNode)node);
                else if (node instanceof InterfaceDeclNode)
                    _nameRequestError(node, "Interfaces are not supported.");
                else _nameRequestError(node, "Invalid request for C name.");
            }
            else return (String)nameProperty;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Derive a unique name for a class that is to be used as the
     *  name of the user-defined C type that implements the class. This is
     *  needed to resolve overloaded class names (i.e., classes with 
     *  the same names in different packages). The unique name is set as 
     *  the value of the C_NAME_KEY property of the given abstract syntax 
     *  tree node. An include file is also associated with the class
     *  through the C_INCLUDE_FILE_KEY;
     *
     *  @param node The abstract syntax tree node for the given class 
     *  declaration. 
     *  @param args Visitor arguments (unused).
     *  @return A null object is returned.
     *  
     */
    private static String _getCClassNameOf(ClassDeclNode node) {
        TreeNode parent = node.getParent();
        if (!(parent instanceof CompileUnitNode))  {
            _nameRequestError(node, "Class declaration node has parent "
                    + "that is not of type CompileUnitNode.");
        }
        String compileUnitName = ASTReflect.getFullyQualifiedName(
                (CompileUnitNode)parent);
        String className = node.getName().getIdent();
        String CClassName = (compileUnitName + "." + className).hashCode() + 
                "_" + className;
        node.setProperty(C_NAME_KEY, CClassName);
        // FIXME: Handle use of "\" in directory names (windows compatibility).
        String includeFileName = compileUnitName.replace('.', '/') 
                + node.getName().getIdent() + ".h";
        node.setProperty(C_INCLUDE_FILE_KEY, includeFileName);
        return CClassName;
    }


    /** Determine the C name associated with a Java PrimitiveTypeNode, 
     *  and set the C_NAME_KEY property of the TypeNode to be the 
     *  determined name.
     *  @param node The AST PrimitiveTypeNode node.
     *  @return The C name assigned to the node.
     */
    private static String _getCPrimitiveTypeNameOf(TypeNode node) {
        String name = null;
        if (node instanceof BoolTypeNode) name = "int"; 
        else if (node instanceof ByteTypeNode) name = "char"; 
        else if (node instanceof CharTypeNode) name = "char"; 
        else if (node instanceof DoubleTypeNode) name = "double"; 
        else if (node instanceof FloatTypeNode) name = "float"; 
        else if (node instanceof IntTypeNode) name = "int"; 
        else if (node instanceof LongTypeNode) name = "long"; 
        else if (node instanceof ShortTypeNode) name = "short"; 
        else _nameRequestError(node, "Unsupported primitive type '"
                + node.getClass().getName() + "'");
        node.setProperty(C_NAME_KEY, name);
        return name;
    }


    /** Determine the C name associated with a Java TypeNameNode, and set the 
     *  C_NAME_KEY property of the TypeNameNode to be the determined name.
     *  @param node The AST TypeNameNode node.
     *  @return The C name assigned to the node.
     */
    private static String _getCTypeNameOf(TypeNameNode node) {
        JavaDecl declaration = JavaDecl.getDecl((TreeNode)node);
        if (declaration == null) 
            _nameRequestError(node, "Unresolved type declaration."); 
        else {
            TreeNode source = declaration.getSource();
            
            /* FIXME: handle importing of include files ...
            if (source.hasProperty(C_INCLUDE_FILE_KEY) &&
                    (node.hasProperty(C_IMPORT_INCLUDE_FILE_KEY) ||
                    _importForAllTypeNames))
                _addIncludeFile((String)(source.getProperty(C_INCLUDE_FILE_KEY)));
            */

            String sourceName = getCNameOf(source);
            node.setProperty(C_NAME_KEY, sourceName);
            return sourceName;
        }
        return null; 
    }


    /** Determine the C name associated with a Java TypeNode, and set the 
     *  C_NAME_KEY property of the TypeNode to be the determined name.
     *  @param node The AST TypeNode node.
     *  @return The C name assigned to the node.
     */
    private static String _getCTypeOf(TypeNode node) {
        String returnString = null;
        if (node instanceof PrimitiveTypeNode) 
            returnString = _getCPrimitiveTypeNameOf(node); 
        else if (node instanceof TypeNameNode) {
            returnString = _getCTypeNameOf((TypeNameNode)node); 
        }
        else _nameRequestError(node, "Unsupported Java type '"
                + node.getClass().getName() + "'");
        return returnString;
    }


    /** Format a diagnostic message for name requests in which serious
     *  errors are detected, and throw a RuntimeException that 
     *  contains the message.
     *  @param node The node whose name request triggered the error.
     *  @param message A description of the error that occured.
     *  @exception A run-time exception is thrown unconditionally.
     */
    public static void _nameRequestError(TreeNode node, String message)
            throws RuntimeException {
        String exceptionMessage = new String(message + "\n");
        if (node instanceof TreeNode) {
            exceptionMessage += "A dump of the offending node follows.\n\n"
                    + node.toString() + "\nEnd of offending node dump.\n\n";
        }
        else if (node == null) 
            exceptionMessage += "The 'offending node' is NULL.\n";
        else 
            exceptionMessage += "The offending node is an instance of '" +
                    node.getClass().getName() + "'\n";
        throw new RuntimeException(exceptionMessage);
    }


    ///////////////////////////////////////////////////////////////////
    /// Old methods that need to be converted

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

        String methodName = _uniqueName(node.getName().getIdent());
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
        String methodName = _uniqueName(node.getName().getIdent());
        node.setProperty(C_NAME_KEY, methodName);
        return null;
    }

    /** Disable visitation of children for a visited block node.
     *  @param node The BlockNode.
     *  @param args Visitor arguments (unused).
     *  @return A null object is returned.
     */
    public Object visitBlockNode(BlockNode node, LinkedList args) {
        node.ignoreChildren();
        return null;  
    }

    /** Mark a visited TypeNameNode to indicate that the associated
     *  declarations should be imported when generating a header (.h) file for
     *  the current compilation unit.
     *  @param node The TypeNameNode. 
     *  @param args Visitor arguments (unused).
     *  @return A null object is returned.
     */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        node.setProperty(C_IMPORT_INCLUDE_FILE_KEY, new Boolean(true));
        return null;  
    }


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
    private String _uniqueName(String methodName) {
        // FIXME --- extract the real class name and unique name index
        String _className = "xxx";
        int _uniqueNameIndex = 0;
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

}
