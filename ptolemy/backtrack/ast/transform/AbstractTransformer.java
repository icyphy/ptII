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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import ptolemy.backtrack.ast.LocalClassLoader;
import ptolemy.backtrack.ast.Type;
import ptolemy.backtrack.ast.TypeAnalyzerState;
import ptolemy.backtrack.ast.LocalClassLoader.ClassImport;

//////////////////////////////////////////////////////////////////////////
//// AbstractTransformer
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class AbstractTransformer {

    /** Given a table of lists, add a value to the list associated with a key.
     *  If the list does not exist, it is created and put into the table.
     *
     *  @param lists The table of lists.
     *  @param key The key.
     *  @param value The value to be added.
     */
    public static void addToLists(Hashtable lists, Object key, Object value) {
        List list = (List)lists.get(key);
        if (list == null) {
            list = new LinkedList();
            lists.put(key, list);
        }
        list.add(value);
    }

    /** Create an AST type node with a type string (possibly partitioned with
     *  "." and "[]").
     *
     *  @param ast The {@link AST} object.
     *  @param type The type.
     *  @return The AST type node.
     */
    public static org.eclipse.jdt.core.dom.Type createType(AST ast, String type) {
        String elementName = Type.getElementType(type);

        org.eclipse.jdt.core.dom.Type elementType;
        if (Type.isPrimitive(elementName))
            elementType =
                ast.newPrimitiveType(PrimitiveType.toCode(elementName));
        else {
            Name element = createName(ast, elementName);
            elementType = ast.newSimpleType(element);
        }

        org.eclipse.jdt.core.dom.Type returnType = elementType;
        for (int i = 0; i < Type.dimensions(type); i++)
            returnType = ast.newArrayType(returnType);

        return returnType;
    }

    /** Create an AST name node with a name string (possibly partitioned with
     *  ".").
     *
     *  @param ast The {@link AST} object.
     *  @param name The name.
     *  @return The AST name node.
     */
    public static Name createName(AST ast, String name) {
        int oldPos = 0;
        Name fullName = null;
        while (oldPos != -1) {
            int pos = indexOf(name, new char[]{'.', '$'}, oldPos);
            String subname = pos == -1 ? name.substring(oldPos) : name.substring(oldPos, pos);
            char c = subname.charAt(0);
            while (c >= '0' && c <= '9') {
                subname = subname.substring(1);
                c = subname.charAt(0);
            }
            if (fullName == null)
                fullName = ast.newSimpleName(subname);
            else
                fullName = ast.newQualifiedName(fullName, ast.newSimpleName(subname));
            if (pos == -1)
                oldPos = -1;
            else
                oldPos = pos + 1;
        }
        return fullName;
    }

    /** Get the shortest possible name of the a class. If there is no conflict,
     *  the class is first imported, and only the simple class is returned;
     *  otherwise, the its full name is returned.
     *
     *  @param c The class.
     *  @param loader The class loader used to test importation conflicts.
     *  @param root The root of the AST. If there is no conflict and the class
     *   has not been imported yet, a new {@link ImportDeclaration} is added to
     *   it.
     *  @return The shortest possible class name.
     */
    public static String getClassName(Class c, TypeAnalyzerState state,
            CompilationUnit root) {
        return getClassName(c.getName(), state, root);
    }

    /** Get the shortest possible name of the a class. If there is no conflict,
     *  the class is first imported, and only the simple class is returned;
     *  otherwise, the its full name is returned.
     *
     *  @param name The full class name.
     *  @param loader The class loader used to test importation conflicts.
     *  @param root The root of the AST. If there is no conflict and the class
     *   has not been imported yet, a new {@link ImportDeclaration} is added to
     *   it.
     *  @return The shortest possible class name.
     */
    public static String getClassName(String name, TypeAnalyzerState state,
            CompilationUnit root) {
        int dimensions = Type.dimensions(name);
        if (dimensions > 0)
            name = Type.getElementType(name);
        name = _getNonarrayClassName(name, state, root);
        if (dimensions > 0) {
            Type type = Type.createType(name);
            for (int i = 0; i < dimensions; i++)
                type = type.addOneDimension();
            name = type.getName();
        }
        return name;
    }

    /** Find the first appearance of any of the given characters in a string.
     *
     *  @param s The string.
     *  @param chars The array of characters.
     *  @param startPos The starting position from which the search begins.
     *  @return The index of the first appearance of any of the given
     *   characters in the string, or -1 if none of them is found.
     */
    public static int indexOf(String s, char[] chars, int startPos) {
        int pos = -1;
        for (int i = 0; i < chars.length; i++) {
            int newPos = s.indexOf(chars[i], startPos);
            if (pos == -1 || newPos < pos)
                pos = newPos;
        }
        return pos;
    }

    /** Test if a field to be added already exists.
     *
     *  @param c The current class.
     *  @param fieldName The field name.
     *  @return <tt>true</tt> if the field is already in the class.
     */
    public static boolean isFieldDuplicated(Class c, String fieldName) {
        // Does NOT check fields inherited from interfaces.
        try {
            c.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /** Test if a method exists in a class or its superclasses. This is the
     *  same as <tt>_hasMethod(c, methodName, parameters, false)</tt>.
     *
     *  @param c The current class.
     *  @param methodName The method name.
     *  @param parameters The types of parameters for the method.
     *  @return <tt>true</tt> if the method is already in the class.
     */
    public static boolean hasMethod(Class c, String methodName,
            Class[] parameters) {
        return hasMethod(c, methodName, parameters, false);
    }

    /** Test if a method exists in a class.
     *
     *  @param c The current class.
     *  @param methodName The method name.
     *  @param thisClassOnly Whether to test the given class only (but not
     *   test its superclasses).
     *  @param parameters The types of parameters for the method.
     *  @return <tt>true</tt> if the method is already in the class.
     */
    public static boolean hasMethod(Class c, String methodName,
            Class[] parameters, boolean thisClassOnly) {
        try {
            if (thisClassOnly)
                c.getMethod(methodName, parameters);
            else
                c.getDeclaredMethod(methodName, parameters);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /** Find the last appearance of any of the given characters in a string.
     *
     *  @param s The string.
     *  @param chars The array of characters.
     *  @param startPos The starting position from which the search begins.
     *  @return The index of the last appearance of any of the given
     *   characters in the string, or -1 if none of them is found.
     */
    public static int lastIndexOf(String s, char[] chars) {
        int pos = -1;
        for (int i = 0; i < chars.length; i++) {
            int newPos = s.lastIndexOf(chars[i]);
            if (pos == -1 || newPos > pos)
                pos = newPos;
        }
        return pos;
    }

    /** Remove an AST node from the its parent.
     *
     *  @param node The node to be removed.
     */
    public static void removeNode(ASTNode node) {
        ASTNode parent = node.getParent();
        StructuralPropertyDescriptor location = node.getLocationInParent();
        if (location.isChildProperty())
            parent.setStructuralProperty(location, null);
        else {
            List properties =
                (List)parent.getStructuralProperty(location);
            int position = properties.indexOf(node);
            properties.remove(position);
        }
    }

    /** Replace an AST node with another one by substituting the corresponding
     *  child of its parent.
     *
     *  @param node The node to be replace.
     *  @param newNode The new node.
     */
    public static void replaceNode(ASTNode node, ASTNode newNode) {
        ASTNode parent = node.getParent();
        StructuralPropertyDescriptor location = node.getLocationInParent();
        if (location.isChildProperty())
            parent.setStructuralProperty(location, newNode);
        else {
            List properties =
                (List)parent.getStructuralProperty(location);
            int position = properties.indexOf(node);
            properties.set(position, newNode);
        }
    }

    /** The prefix of assignment methods.
     */
    public static String ASSIGN_PREFIX = "$ASSIGN$";

	/** The prefix of backup functions.
     */
    public static String BACKUP_PREFIX = "$BACKUP$";

    /** The name of the checkpoint object.
     */
    public static String CHECKPOINT_NAME = "$CHECKPOINT";

    /** The name of the checkpoint record.
     */
    public static String CHECKPOINT_RECORD_NAME = "$RECORD$$CHECKPOINT";

    /** The name of the method to get the checkpoint.
     */
    public static String GET_CHECKPOINT_NAME = "$GET$CHECKPOINT";

    /** The name of the method to set a checkpoint.
     */
    public static String SET_CHECKPOINT_NAME = "$SET$CHECKPOINT";

    /** Get the name of the assignment method.
     *
     *  @param fieldName The field name.
     *  @param special Whether the method handles special assign operators.
     *  @return The name of the assignment method.
     */
    protected String _getAssignMethodName(String fieldName, boolean special) {
        return ASSIGN_PREFIX + (special ? "SPECIAL$" : "") + fieldName;
    }

	/** Get the name of the backup method.
	 *
	 *  @param fieldName The field name.
	 *  @return The name of the backup method.
	 */
	protected String _getBackupMethodName(String fieldName) {
		return BACKUP_PREFIX + fieldName;
	}

    /** Get the shortest possible name of the a class. If there is no conflict,
     *  the class is first imported, and only the simple class is returned;
     *  otherwise, the its full name is returned.
     *
     *  @param name The full class name. It must not be the name of an array
     *   type.
     *  @param loader The class loader used to test importation conflicts.
     *  @param root The root of the AST. If there is no conflict and the class
     *   has not been imported yet, a new {@link ImportDeclaration} is added to
     *   it.
     *  @return The shortest possible class name.
     */
    private static String _getNonarrayClassName(String name,
            TypeAnalyzerState state, CompilationUnit root) {
        LocalClassLoader loader = state.getClassLoader();
        int lastDot = name.lastIndexOf('.');
        String packageName = lastDot == -1 ? "" : name.substring(0, lastDot);
        String className = name.substring(lastDot + 1);
        String simpleName;
        int lastSeparator = lastIndexOf(name, new char[]{'.', '$'});
        if (lastSeparator == -1)
            return name;
        else
            simpleName = name.substring(lastSeparator + 1);

        String currentClassName = state.getCurrentClass().getName();
        if (name.equals(currentClassName))
            return simpleName;
        else {
            int dollarPos = currentClassName.length();
            while (dollarPos >= 0) {
                String baseName = currentClassName.substring(0, dollarPos) + "$";
                if (name.startsWith(baseName))
                    return name.substring(baseName.length());
                dollarPos = currentClassName.lastIndexOf('$', dollarPos - 1);
            }
        }
        // FIXME

        Iterator importedClasses = loader.getImportedClasses().iterator();
        while (importedClasses.hasNext()) {
            ClassImport importedClass = (ClassImport)importedClasses.next();
            if (importedClass.getPackageName().equals(packageName) &&
                    importedClass.getClassName().equals(className))
                // Already imported.
                return simpleName;
            else {
                String importedName = importedClass.getClassName();
                int lastDollar = importedName.lastIndexOf('$');
                if (lastDollar == -1 && importedName.equals(simpleName))
                    return name;
                else if (lastDollar >= 0 &&
                        importedName.substring(lastDollar + 1)
                            .equals(simpleName))
                    return name;
            }
        }

        Iterator importedPackages = loader.getImportedPackages().iterator();
        while (importedPackages.hasNext()) {
            String importedPackage = (String)importedPackages.next();
            if (importedPackage.equals(packageName))    // Already imported.
                return simpleName;
            else {
                try {
                    // Test if a class with the same name exists in the
                    // package.
                    loader.loadClass(importedPackage + "." + simpleName);
                    // If exists, conflict.
                    return name;
                } catch (ClassNotFoundException e) {
                }
            }
        }

        AST ast = root.getAST();
        ImportDeclaration declaration = ast.newImportDeclaration();
        declaration.setName(createName(ast, name));
        root.imports().add(declaration);
        loader.importClass(name);
        return simpleName;
    }
}