/* 
Methods dealing with expressions. Most of the code and comments were taken from the 
Titanium project.

Copyright (c) 1998-1999 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.Iterator;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** Methods dealing with expressions. Most of the code and comments were taken from the 
 *  Titanium project. 
 * 
 *  @author Jeff Tsay
 */
public class ExprUtility implements JavaStaticSemanticConstants {

    /** Public constructor allows inheritence of methods although this class has no
     *  instance members.
     */
    public ExprUtility() {}

    /** Return a resolved node corresponding the object that the 
     *  FieldAccessNode accesses. The argument must be either an
     *  ObjectFieldAccessNode, a ThisFieldAccessNode, or a 
     *  SuperFieldAccessNode. Otherwise throw an IllegalArgumentException.
     *  In particular, if node is a SuperFieldAccessNode or a ThisFieldAccessNode, 
     *  return a new instance of ThisNode. If the node is a TypeFieldAccessNode,
     *  return null. If the argument node has been name resolved,
     *  return a node that is also resolved.
     */
    public static TreeNode accessedObject(FieldAccessNode node) {
        TreeNode retval;
        
        switch (node.classID()) {
          case OBJECTFIELDACCESSNODE_ID:
          return ((ObjectFieldAccessNode) node).getObject();
 
          case THISFIELDACCESSNODE_ID:
          case SUPERFIELDACCESSNODE_ID:
          retval = new ThisNode();
          TypeNameNode typeNameNode = (TypeNameNode) node.getProperty(THIS_CLASS_KEY);
          if (typeNameNode != null) {
             retval.setProperty(THIS_CLASS_KEY, typeNameNode);
          }          
          return retval;
          
          case TYPEFIELDACCESSNODE_ID:
          return null;          
        }
    
        ApplicationUtility.error("accessedObject() : node not supported " + node);             
        return null;
    }

    /** Return the integer value given by the IntLitNode. */
    public static int intValue(IntLitNode litNode) {
        String literal = litNode.getLiteral();
        
        return Integer.decode(literal).intValue();             
    } 

    public static boolean isIntConstant(ExprNode expr, int from, int to) {
        if (expr instanceof IntLitNode) {
           int value = intValue((IntLitNode) expr);
             
           return ((value >= from) && (value <= to));          
        } 
            
        return false;    
    }
    
    /** Return true iff the ExprNode is a statement expression, that is, it
     *  may appear as a legal statement if a semicolon is appended to the end.
     */
    public static boolean isStatementExpression(ExprNode expr) {
        switch (expr.classID()) {
          case ASSIGNNODE_ID:
          case MULTASSIGNNODE_ID:
          case DIVASSIGNNODE_ID:
          case REMASSIGNNODE_ID:          
          case PLUSASSIGNNODE_ID:
          case MINUSASSIGNNODE_ID:
          case LEFTSHIFTLOGASSIGNNODE_ID:
          case RIGHTSHIFTLOGASSIGNNODE_ID:
          case RIGHTSHIFTARITHASSIGNNODE_ID:
          case BITANDASSIGNNODE_ID:
          case BITXORASSIGNNODE_ID:
          case BITORASSIGNNODE_ID:
          case PREINCRNODE_ID:
          case PREDECRNODE_ID:
          case POSTINCRNODE_ID:
          case POSTDECRNODE_ID:
          case METHODCALLNODE_ID:
          case ALLOCATENODE_ID:
          case ALLOCATEANONYMOUSCLASSNODE_ID:
          return true;
          
          default:
          return false;
        }              
    }
    
}