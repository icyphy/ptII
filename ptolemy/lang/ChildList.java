
/* A class for implementing a list of child nodes within the data structure
   for an abstract syntax tree node.

Copyright (c) 2001 The University of Maryland   
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu) 
@AcceptedRating Red (ssb@eng.umd.edu)
*/


package ptolemy.lang;
import java.util.ArrayList;
import java.util.Collection;

//////////////////////////////////////////////////////////////////////////
//// ChildList
/**
A class for implementing a list of child nodes in the data structure
for an abstract syntax tree node. This is a simple extension of
the ArrayList class that overrides certain methods to appropriately
set the parent field of each child that is added to a child list.

FIXME: Probably, the TNLManip class should be removed, and its functionality 
merged into this class.

@author Shuvra S. Bhattacharyya 
@version $Id
@see ptolemy.lang.TNLManip

*/
public class ChildList extends ArrayList {

    /** Allocate a child list, given a parent tree node.
     *  @param parent The parent for all children that are inserted into
     *  the child list.
     */
    public ChildList(TreeNode parent) {
        super();
        _parent = parent;
    }

    /** Allocate a child list for a specified parent and number of children.
     *  @param parent The parent for all children that are inserted into
     *  the child list.
     *  @param numberOfChildren The number of children to allocate the child
     *  list for.
     */
    public ChildList(TreeNode parent, int numberOfChildren) {
        super(numberOfChildren);
        _parent = parent;
    }

    /**  Set the child at the specified index to the specified object,
     *  replacing any previous child at that index.
     *  @param index The index of the child.
     *  @param child The child to insert.
     *  @return The child that was inserted.
     */
    public Object set(int index, Object child) {
        super.set(index, child);
        _parent.setAsParent(child);
        return child;
    }

    /**
     * Inserts the specified child at the specified position in this 
     * child list.
     * @param child The child to insert.
     * @param index The index at which to insert the child.
     */
    public void add(int index, Object child)  {
        super.add(index, child); 
        _parent.setAsParent(child);
    }

    /** Appends the specified child to the end of this child list. 
     *  @param child The child to insert.
     *  @return <tt>true</tt> (as per the general contract of Collection.add).\n     */
    public boolean add(Object child) {
        super.add(child);
        _parent.setAsParent(child);
        return true;
    }


    /** Appends all of the elements in the specified Collection to 
     *  the end of this list, in the order that they are returned by 
     *  the specified Collection's Iterator.
     *  @param c The elements to be inserted into this list.
     *  @return Return true iff one or more elements were added to
     *  the list as a result of this call.
     */
    public boolean addAll(Collection c) {
        boolean returnValue = super.addAll(c);
        _parent.setAsParent(c);
        return returnValue;
    }


    /** Inserts all of the elements in the specified Collection into this list,
     *  starting at the specified position.
     *  @param index The index at which to insert the first element
     *  from the specified collection.
     *  @param c The elements to be inserted into this list.
     *  @return Return true iff one or more elements were added to
     *  the list as a result of this call.
     */
    public boolean addAll(int index, Collection c) {
        boolean returnValue = super.addAll(index, c);
        _parent.setAsParent(c);
        return returnValue;
    }
   
 
    /** Set the parent attributes of the child list, and all children within it,
     *  to the given tree node
     *  @param parent The tree node to be set as the parent of the child list.
     */  
    public void setParent(TreeNode parent) {
        _parent = parent;
        parent.setAsParent(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The parent of the children in the child list */
    private TreeNode _parent = null;
}
