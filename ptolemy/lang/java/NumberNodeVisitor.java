/* 
Number the nodes in the AST uniquely, numbering ancestors before their 
children, and numbering children from left to right.

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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import ptolemy.lang.*;

/** A visitor that numbers nodes in the AST uniquely, numbering ancestors before 
 *  their children, and numbering children from left to right.
 * 
 *  @author Jeff Tsay
 */
public class NumberNodeVisitor extends JavaVisitor {
    public NumberNodeVisitor() {
        this(new HashMap());
    }

    public NumberNodeVisitor(Map nodeMap) {
        super(TM_SELF_FIRST);
        
        _nodeMap = nodeMap;
    }

    /** The default visit method. */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        if (!node.hasProperty(PropertyMap.NUMBER_KEY)) {
           Integer keyValue = new Integer(_nodeMap.size());
           node.setProperty(PropertyMap.NUMBER_KEY, keyValue);        
           _nodeMap.put(keyValue, node);
        }
        
        return null;
    }
    
    /** Given a list of TreeNode's, number all TreeNode's in the list uniquely,
     *  including descendents of individual TreeNode's. 
     *  Return the Set of all nodes.
     */
    public static Map numberNodes(Collection nodes) {
        HashMap map = new HashMap();
        NumberNodeVisitor v = new NumberNodeVisitor(map);
        
        Iterator itr = nodes.iterator();
        
        while (itr.hasNext()) {
            TreeNode node = (TreeNode) itr.next();
            node.accept(v, null); // modifies map
        } 
        
        return map;                
    }
             
    protected final Map _nodeMap;
}
