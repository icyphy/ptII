/* Static functions for manipulating lists of children of a TreeNode.

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
In general, a list may contain TreeNodes or other lists.

@author Jeff Tsay
@version $Id$
 */
public class TNLManip {

    // Make the constructor private since there are only static methods.
    private TNLManip() {}

    /** Return a LinkedList with the same elements in the same
     *  order as in the specified array. If an array element is null,
     *  then the corresponding element in the LinkedList will be
     *  NullValue.instance. This method may be used for lists of
     *  any objects, not just nodes of an AST.
     *  @param objArray The array of objects to convert to a list.
     *  @return A new list with the same elements as the array.
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

    /** Make a deep clone of a list.  That is, each element is cloned
     *  and a new list of the new element is returned.
     *  @param list The list to clone.
     *  @return A new list.
     */
    public static final ArrayList cloneList(List list) {
        // FIXME: Shouldn't the return value be a List?
        Iterator itr = list.iterator();
        ArrayList retval = new ArrayList(list.size());

        while (itr.hasNext()) {
            Object obj = itr.next();

            if (obj instanceof TreeNode) {
                retval.add(((TreeNode) obj).clone());
            } else if (obj instanceof List) {
                retval.add(cloneList((List) obj));
            } else {
                throw new RuntimeException("unknown object in list: " +
                        obj.getClass());
            }
        }
        return retval;
    }

    /** Create a new LinkedList with the specified object in it.
     *  If the object is null, then NullValue.instance is inserted instead.
     *  FIXME: This method is misnamed because it is not like a Lisp cons.
     *  @param obj The object to prepend.
     *  @return The new list.
     */
    public static final LinkedList cons(Object obj) {
        return cons(obj, new LinkedList());
    }

    /** Prepend the specified object to the front of the specified list.
     *  If the object to prepend is null, then NullValue.instance is
     *  prepended instead.
     *  FIXME: This method is misnamed because it is not like a Lisp cons.
     *  It is destructive.
     *  @param obj The object to prepend.
     *  @param list The list to modify.
     *  @return The modified list.
     */
    public static final LinkedList cons(Object obj, LinkedList list) {
        if (obj == null) {
            list.addFirst(NullValue.instance);
        } else {
            list.addFirst(obj);
        }
        return list;
    }

    /** Print the classnames of the specified node and any nodes
     *  it contains.  Do this recursively so that the entire hierarchy
     *  is seen.
     *  @param node The node to print, which should be either a TreeNode
     *   or a List.
     *  @param prefix The prefix to prepend to the printed lines.
     */
    public static final void showTree(Object node, String prefix) {
        System.out.println(prefix + node.getClass().getName());
        if (node instanceof TreeNode) {
            List children = ((TreeNode)node).children();
            Iterator itr = children.iterator();
            while (itr.hasNext()) {
                Object inside = itr.next();
                showTree(inside, prefix + "  ");
            }
        } else if (node instanceof List) {
            Iterator itr = ((List)node).iterator();
            while (itr.hasNext()) {
                Object inside = itr.next();
                showTree(inside, prefix + "  ");
            }
        }
    }

    /** Return a string representation of the list. This method simply calls
     *  toString(list, "").
     *  @param list The list of nodes to represent.
     *  @return A string representation.
     */
    public static final String toString(List list) {
        return toString(list, "");
    }

    /** Return a string representation of the list. The string representation
     *  is prefixed by the argument string.
     *  @param list The list of nodes to print.
     *  @param prefix The prefix for each line printed.
     *  @return A string representation.
     */
    public static final String toString(List list, String prefix) {
        if (list.isEmpty()) {
            return "<empty list>\n";
        }
        String nextprefix = prefix + " ";
        StringBuffer sb = new StringBuffer();
        sb.append("list\n");

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            sb.append(nextprefix);
            Object child = itr.next();

            if (child instanceof TreeNode) {
                TreeNode childNode = (TreeNode) child;
                sb.append(childNode.toString(nextprefix));
            } else if (child instanceof List) {
                sb.append(toString((List) child, nextprefix));
            } else {
                throw new RuntimeException("toString(" + list + ", \"" +
					   prefix + 
					   "\"): unknown object in list: " +
					   child.getClass());
            }
        }
        sb.append(prefix);
        sb.append("END list\n");
        return sb.toString();
    }

    /** Have each member of the specified <i>list</i> accept the
     *  specified visitor. The specified <i>args</i> are passed to the
     *  accept() method of each element of the <i>list</i>, if that element
     *  is an instance of TreeNode.  If the element is a list, then its
     *  elements are visited.  If it is neither a TreeNode nor a list,
     *  then an exception is thrown. The returned
     *  list has the same size as the specified <i>list</i>, and contains
     *  of the return values of each visitation. If an accept() method
     *  returns null, the corresponding value in the returned list is
     *  NullValue.instance.
     *  @param visitor The visitor to apply to the elements of the list.
     *  @param args The arguments to pass to the accept() method.
     *  @param list The list of objects to visit.
     *  @return A list of the results of the visits.
     */
    public static final ArrayList traverseList(IVisitor visitor,
            LinkedList args, List list) {
        Object retval;
        ArrayList retList = new ArrayList(list.size());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object obj = itr.next();

            if (obj instanceof TreeNode) {
                TreeNode node = (TreeNode) obj;

                retval = node.accept(visitor, args);

                if (retval == null) {
                    retList.add(NullValue.instance);
                } else {
                    retList.add(retval);
                }

            } else if (obj instanceof List) {
                retval = traverseList(visitor, args, (List)obj);

                retList.add(retval);
            } else {
                // FIXME: This should not be a runtime exception!
                throw new RuntimeException("unknown object in list: " +
                        obj.getClass());
            }
        }
        return retList;
    }
}
