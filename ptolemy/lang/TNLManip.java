/* Static functions for manipulating lists of children of a TreeNode.

Copyright (c) 1998-2001 The Regents of the University of California.
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

@author Jeff Tsay and Shuvra S. Bhattacharyya 
@version $Id$
 */
public class TNLManip {

    // Make the constructor private since there are only static methods.
    private TNLManip() {}

    /** Create a new LinkedList with the specified object in it.
     *  If the object is null, then NullValue.instance is inserted instead.
     *  @param object The object to prepend.
     *  @return The new list.
     */
    public static final LinkedList addFirst(Object object) {
        return addFirst(object, new LinkedList());
    }

    /** Prepend the specified object to the front of the specified list.
     *  If the object to prepend is null, then NullValue.instance is
     *  prepended instead.
     *  @param object The object to prepend.
     *  @param list The list to modify.
     *  @return The modified list.
     */
    public static final LinkedList addFirst(Object object, LinkedList list) {
        // This used to be called cons, but it is not like the Lisp cons,
        // it is destructive.
        if (object == null) {
            list.addFirst(NullValue.instance);
        } else {
            list.addFirst(object);
        }
        return list;
    }

    /** Return a LinkedList with the same elements in the same
     *  order as in the specified array. If an array element is null,
     *  then the corresponding element in the LinkedList will be
     *  NullValue.instance. This method may be used for lists of
     *  any objects, not just nodes of an AST.
     *  @param objectArray The array of objects to convert to a list.
     *  @return A new list with the same elements as the array.
     */
    public static final LinkedList arrayToList(Object[] objectArray) {
        LinkedList returnValue = new LinkedList();
        for (int i = 0; i < objectArray.length; i++) {
            Object object = objectArray[i];
            if (object == null) {
                returnValue.addLast(NullValue.instance);
            } else {
                returnValue.addLast(objectArray[i]);
            }
        }
        return returnValue;
    }

    /** Make a deep clone of a list.  That is, each element is cloned
     *  and a new list of the new element is returned.
     *  @param list The list to clone.
     *  @return A new list.
     */
    public static final List cloneList(List list) {
        Iterator iterator = list.iterator();
        ArrayList returnValue = new ArrayList(list.size());

        while (iterator.hasNext()) {
            Object object = iterator.next();

            if (object instanceof TreeNode) {
                returnValue.add(((TreeNode) object).clone());
            } else if (object instanceof List) {
                returnValue.add(cloneList((List) object));
            } else {
                throw new RuntimeException("unknown object in list: " +
                        object.getClass());
            }
        }
        return returnValue;
    }

    /** Print the class names of the specified node and any nodes
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
            Iterator iterator = children.iterator();
            while (iterator.hasNext()) {
                Object inside = iterator.next();
                showTree(inside, prefix + "  ");
            }
        } else if (node instanceof List) {
            Iterator iterator = ((List)node).iterator();
            while (iterator.hasNext()) {
                Object inside = iterator.next();
                showTree(inside, prefix + "  ");
            }
        }
    }

    /** Return a string representation of the list.
     *  @param list The list of nodes to print.
     *  @return A string representation.
     */
    public static final String toString(List list) {
	return TNLManip.toString(list, "");
    }

    /** Return a string representation of the list. The string representation
     *  is prefixed by the argument string.
     *  @param list The list of nodes to print.
     *  @param prefix The prefix for each line printed.
     *  @return A string representation.
     */
    public static final String toString(List list, String prefix) {
        if (list.isEmpty()) {
            return " {}";
        }
        String nextPrefix = prefix + " ";
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("{ \n");

        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            stringBuffer.append(nextPrefix);
            Object child = iterator.next();

            if (child instanceof TreeNode) {
                TreeNode childNode = (TreeNode) child;
                stringBuffer.append(childNode.toString(nextPrefix));
            } else if (child instanceof List) {
                stringBuffer.append(toString((List) child, nextPrefix));
            } else {
                throw new RuntimeException("toString(" + list + ", \"" +
                        prefix +
                        "\"): unknown object in list: " +
                        child.getClass());
            }
        }
        stringBuffer.append("}");
        return stringBuffer.toString();
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
     *  @param setChildReturnValues Indicates whether or not the
     *  list of returned values should be placed in the CHILD_RETURN_VALUES_KEY
     *  property of the node.
     *  @return A list of the results of the visits.
     */
    public static final ArrayList traverseList(IVisitor visitor,
            LinkedList args, List list, boolean setChildReturnValues) {
        Object returnValue;
        ArrayList retList = new ArrayList(list.size());

        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();

            if (object instanceof TreeNode) {
                TreeNode node = (TreeNode) object;

                returnValue = node.accept(visitor, args, setChildReturnValues);

                if (returnValue == null) {
                    retList.add(NullValue.instance);
                } else {
                    retList.add(returnValue);
                }

                // FIXME: In the case of a null return value, should
                // NullValue.instance be set as the property value?             
                node.setProperty(node.RETURN_VALUE_AS_ELEMENT_KEY, returnValue);

            } else if (object instanceof List) {
                returnValue = traverseList(visitor, args, (List)object,
                        setChildReturnValues);

                retList.add(returnValue);
            } else {
                throw new RuntimeException("TNLManip.traverseList(): " +
                        "unknown object in list: " + object.getClass());
            }
        }
        return retList;
    }

    /** Have each member of the specified <i>list</i> accept the
     *  specified visitor, and set child return value properties
     *  throughout visitation. Equivalent to:
     *          traverseList(visitor, args, list, true).
     *  @param visitor The visitor to apply to the elements of the list.
     *  @param args The arguments to pass to the accept() method.
     *  @param list The list of objects to visit.
     *  @return A list of the results of the visits.
     */    
    public static final ArrayList traverseList(IVisitor visitor,
            LinkedList args, List list) {
        return traverseList(visitor, args, list, true);
    }
}
