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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// TNLManip
/** Static functions for manipulating lists of children of a TreeNode.
 *  In general, a list may contain TreeNodes or other lists.
 *
 *  @author Jeff Tsay
 */
public class TNLManip {

    private TNLManip() {}

    /** Make a deep clone of a child list. */
    public static final ArrayList cloneList(List list) {
        Iterator itr = list.iterator();
        ArrayList retval = new ArrayList(list.size());
        
        while (itr.hasNext()) {
           Object obj = itr.next();
           
           if (obj instanceof TreeNode) {
              retval.add(((TreeNode) obj).clone());
           } else if (obj instanceof List) {
              retval.add(cloneList((List) obj));
           } else {
              throw new RuntimeException("unknown object in list : " + obj.getClass());
           }       
        }
        return retval;
    }

    /** Have each member of the list accept the argument visitor. Each member 
     *  of the argument list should be a child list. Return a list
     *  of the return values from each visitation. If a node in the list  
     *  returns null, set the corresponding value in the return list to
     *  NullValue.instance.     
     */
    public static final ArrayList traverseList(IVisitor v, TreeNode parent,
     LinkedList args, List childList) {
       Object retval;
       ArrayList retList = new ArrayList(childList.size());

       Iterator itr = childList.iterator();

       while (itr.hasNext()) {
         Object obj = itr.next();

         if (obj instanceof TreeNode) {
            TreeNode node = (TreeNode) obj;

            retval = node.accept(v, args);

            if (retval == null) {
               retList.add(NullValue.instance);
            } else {
               retList.add(retval);
            }

         } else if (obj instanceof List) {
            retval = traverseList(v, null, args, (List) obj);

            retList.add(retval);
         } else {
            throw new RuntimeException("unknown object in list : " + obj.getClass());
         }
       }

       return retList;
    }
    
    /** Create a LinkedList with one element. If obj is null, the element in
     *  the LinkedList should be NullValue.instance. This method
     *  may be used to create a list from any object.
     */
    public static final LinkedList cons(Object obj) {
       return cons(obj, new LinkedList());        
    }
    
    public static final LinkedList cons(Object obj, LinkedList list) {
       if (obj == null) {
          list.addFirst(NullValue.instance);
       } else {
          list.addFirst(obj);
       }
       return list;
    }

    /** Convert an array of objects into a LinkedList with elements in the same 
     *  order as in the array. If an array element is null, the corresponding
     *  element in the LinkedList will be NullValue.instance. This method
     *  may be used for lists of any objects.
     */
    public static final LinkedList arrayToList(Object[] objArray) {
       LinkedList retval = new LinkedList();
       for (int i = 0; i < objArray.length; i++) {
           Object obj = objArray[i];
           if (obj == null) {
              retval.addLast(NullValue.instance);
           } else {
              retval.addLast(objArray[i]);
           }
       }
       return retval;
    } 

    /** Return a string representation of the list. This method simply calls
     *  toString(list, "")
     */ 
    public static final String toString(List list) {
       return toString(list, "");
    }

    /** Return a string representation of the list. The string representation
     *  is indented by the argument string.
     */ 
    public static final String toString(List list, String indent) {
       if (list.isEmpty()) {
          return "<empty list>\n";
       }
       
       String nextIndent = indent + " "; 

       StringBuffer sb = new StringBuffer();
             
       sb.append("list\n");

       Iterator itr = list.iterator();

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

       sb.append(indent); 
       
       sb.append("END list\n");

       return sb.toString();
    }
}
