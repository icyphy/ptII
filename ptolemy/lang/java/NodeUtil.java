/* Miscellaneous utilities for working with AST Nodes.

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


@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)

*/

package ptolemy.lang.java;

import ptolemy.lang.TreeNode;
import ptolemy.lang.java.nodetypes.*;
import ptolemy.lang.Scope;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// NodeUtil.
/** Miscellaneous utilities for working with abstract syntax tree (AST) Nodes.
@version $Id$
@author Shuvra S. Bhattacharyya
 */
public final class NodeUtil implements JavaStaticSemanticConstants {

    /* FIXME: Ideally, the functionality in this class should be
     *  contained within appropriate AST nodes.
     */

    // private constructor prevent instantiation of this class
    private NodeUtil() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given an AST compile unit node, return the corresponding user
     *  type type definition node.
     *  @param compileUnit The compile unit node.
     *  @return The corresponding user type definition node. A null
     *  object is returned if the given compileUnit is null, or has a
     *  null type definition list.
     */
    public static UserTypeDeclNode getDefinedType(CompileUnitNode compileUnit) {
        List definedTypes;
        if (compileUnit == null) {
            return null;
        } else if ((definedTypes = compileUnit.getDefTypes()) == null) {
            return null;
        } else if (definedTypes.size() > 1) {
            // FIXME: what does it mean when there are multiple
            // type definitions?

	    StringBuffer buffer =
		new StringBuffer("NodeUtil.getDefinedType() "
                        + "has encountered a CompileUnitNode with "
                        + "multiple type definitions."
                        + ". \nA dump of the offending AST follows.\n"
                        + compileUnit.toString());
	    for(int i = 0; i< definedTypes.size(); i++){
		buffer.append("\nDefined Type #" + i + definedTypes.get(0));
	    }

	    //throw new RuntimeException(buffer.toString());
            System.err.println("Warning: " + buffer.toString());
	}
        return (UserTypeDeclNode)(definedTypes.get(0));
    }

    /** Return the scope associated with an abstract syntax tree (AST) node.
     *  Return null if the AST node does not have a defined scope.
     *  @param node The AST node.
     *  @return The scope of the AST node.
     */
    public static Scope getScope(TreeNode node) {
        return (Scope)(node.getProperty(SCOPE_KEY));
    }

    /** Given an AST TypedDecl, return the class or interface
     *  declaration (ClassDecl) associated with the declared entity's
     *  type, if one exists.  For example, if this method is called on
     *  a field declaration of the form 'myClass myField;', it will
     *  return the declaration of 'myClass'.
     *  @param typedNode The AST TypedNode.
     *  @return The declaration associated with the type of the AST TypedNode.
     *  Return null if we cannot resolve the desired declaration.
     */
     public static ClassDecl typedDeclToClassDecl(TypedDecl typedDecl) {
         if (typedDecl == null) {
             return null;
         }
         TypeNode type = ((TypedDecl)typedDecl).getType();
         if (!(type instanceof TypeNameNode)) {
             return null;
         }
         JavaDecl declaration = JavaDecl.getDecl((TreeNode)type);
         if (!(declaration instanceof ClassDecl)) {
             return null;
         }
         return (ClassDecl)declaration;
     }
}
