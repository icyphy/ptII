/*
Static functions for manipulating lists of children of a TreeNode.

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

package ptolemy.lang;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

public class TNLManip {

    private TNLManip() {}

    public static final LinkedList traverseList(IVisitor v, TreeNode parent,
     LinkedList args, List childList) {
       Object retval;
       LinkedList retList = new LinkedList();

       Iterator itr = childList.listIterator();

       while (itr.hasNext()) {
         Object obj = itr.next();

         if (obj instanceof TreeNode) {
            TreeNode node = (TreeNode) obj;

            retval = node.accept(v, args);

            if (retval == null) {
               retList.addLast(NullValue.instance);
            } else {
               retList.addLast(retval);
            }

         } else if (obj instanceof List) {
            retval = (Object) traverseList(v, null, args, (List) obj);

            retList.addLast(retval);
         } else {
            throw new RuntimeException("unknown object in list : " + obj.getClass());
         }
       }

       return retList;
    }
    
    public static final LinkedList cons(Object obj) {
       return cons(obj, new LinkedList());        
    }
    
    public static final LinkedList cons(Object obj, LinkedList list) {
       list.addFirst(obj);
       return list;
    }
  
    public static final String toString(List list) {
       return toString(list, "");
    }

    public static final String toString(List list, String indent) {
       if (list.isEmpty()) {
          return "<empty list>\n";
       }
       
       String nextIndent = indent + " "; 

       StringBuffer sb = new StringBuffer();
             
       sb.append("list\n");

       Iterator itr = list.listIterator();

       while (itr.hasNext()) {
          
          sb.append(nextIndent); 
          
          Object child = itr.next();

          if (child instanceof TreeNode) {
             TreeNode childNode = (TreeNode) child;
             sb.append(childNode.toString(nextIndent));
          } else if (child instanceof List) {
             sb.append(toString((List) child, nextIndent));
          } else {
             throw new RuntimeException("unknown object in list : " + child.getClass());
          }
       }

       sb.append(indent); // FIXME
       
       sb.append("END list\n");

       return sb.toString();
    }
}
