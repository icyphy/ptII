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

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import ptolemy.backtrack.ast.Type;

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
    
    /** The name of the checkpoint object.
     */
    public static String CHECKPOINT_NAME = "$CHECKPOINT";
    
    /** The name of the method to set a checkpoint.
     */
    public static String SET_CHECKPOINT_NAME = "$SET$CHECKPOINT";
    
    /** Create an AST type node with a type string (possibly partitioned with
     *  "." and "[]").
     *  
     *  @param ast The {@link AST} object.
     *  @param type The type.
     *  @return The AST type node.
     */
    protected org.eclipse.jdt.core.dom.Type _createType(AST ast, String type) {
        String elementName = Type.getElementType(type);
        int dimensions = Type.dimensions(type);
        
        org.eclipse.jdt.core.dom.Type elementType;
        if (Type.isPrimitive(elementName))
            elementType = 
                ast.newPrimitiveType(PrimitiveType.toCode(elementName));
        else {
            Name element = _createName(ast, elementName);
            elementType = ast.newSimpleType(element);
        }
        
        org.eclipse.jdt.core.dom.Type returnType = elementType;
        for (int i = 1; i < dimensions; i++)
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
    protected Name _createName(AST ast, String name) {
        int pos = _indexOf(name, new char[]{'.', '$'}, 0);
        String subname = pos == -1 ? name : name.substring(0, pos);
        Name fullName = ast.newSimpleName(subname);
        while (pos != -1) {
            pos = _indexOf(name, new char[]{'.', '$'}, pos + 1);
            name = pos == -1 ? name : name.substring(0, pos);
            SimpleName simpleName = ast.newSimpleName(subname);
            fullName = ast.newQualifiedName(fullName, simpleName);
        }
        return fullName;
    }
    
    /** Find the first appearance of any of the given characters in a string.
     * 
     *  @param s The string.
     *  @param chars The array of characters.
     *  @param startPos The starting position from which the search begins.
     *  @return The index of the first appearance of any of the given
     *   characters in the string, or -1 if none of them is found.
     */
    protected int _indexOf(String s, char[] chars, int startPos) {
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
    protected boolean _isFieldDuplicated(Class c, String fieldName) {
        // Does NOT check fields inherited from interfaces.
        try {
            c.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
    
    /** Test if a method to be added already exists.
     * 
     *  @param c The current class.
     *  @param methodName The method name.
     *  @param parameters The types of parameters for the method.
     *  @return <tt>true</tt> if the method is already in the class.
     */
    protected boolean _isMethodDuplicated(Class c, String methodName, 
            Class[] parameters) {
        try {
            c.getMethod(methodName, parameters);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    /** Replace an AST node with another one by substituting the corresponding
     *  child of its parent.
     * 
     *  @param node The node to be replace.
     *  @param newNode The new node.
     */
    protected void _replaceNode(ASTNode node, ASTNode newNode) {
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
    
}