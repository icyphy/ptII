/* A Java AST visitor that does renaming.

 Copyright (c) 2000 The Regents of the University of California.
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
import java.util.Map;

import ptolemy.lang.TreeNode;
import ptolemy.lang.java.nodetypes.*;

//////////////////////////////////////////////////////////////////////////
//// RenameJavaVisitor
/** A Java AST visitor that does renaming. The argument to each visitation 
 *  method should be a LinkedList containing a Map of old name strings to
 *  new name strings. The renaming is done in place, i.e. no new nodes
 *  are allocated. Renaming is only done on NameNodes that are unqualified.
 *
 *  @author Jeff Tsay
 */
public class RenameJavaVisitor extends ResolveVisitorBase {

    public RenameJavaVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitNameNode(NameNode node, LinkedList args) {
        TreeNode qualifier = node.getQualifier();
        
        qualifier.accept(this, args);              
                
        if (qualifier == AbsentTreeNode.instance) {        
           String ident = node.getIdent();  
        
           Map nameToNewNameMap = (Map) args.get(0);
          
           String newName = (String) nameToNewNameMap.get(ident);    
        
           if (newName != null) {
              node.setIdent(newName);
           }
        }
            
        return null;
    }    

    // make sure we override the following methods because they may (deeply) 
    // contain NameNodes.

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }
            
    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return _defaultVisit(node, args);
    }    
}