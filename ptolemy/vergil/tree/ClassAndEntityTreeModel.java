/* A tree model for Ptolemy II objects, for use with JTree.

 Copyright (c) 2000-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.tree;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ClassAndEntityTreeModel
/**
A tree model for Ptolemy II models that includes class definitions and
entities. The indexes of the class definitions are 0 to a-1, where a is the
number of class definitions.  The indexes of the entities are a to a+p-1,
where p is the number of entities.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class ClassAndEntityTreeModel extends EntityTreeModel {

    /** Create a new tree model with the specified root.
     *  @param root The root of the tree.
     */
    public ClassAndEntityTreeModel(NamedObj root) {
        super(root);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the child of the given parent at the given index.
     *  If the child does not exist, then return null.
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    public Object getChild(Object parent, int index) {
        List classes = _classes(parent);
        int numClasses = classes.size();

        if (index >= numClasses) {
            return super.getChild(parent,
                    index - numClasses);
        } else if (index >= 0) {
            return classes.get(index);
        } else {
            return null;
        }
    }

    /** Return the number of children of the given parent.
     *  This is the number classes and contained
     *  entities, filtered by the filter specified by setFilter(),
     *  if any has been specified.
     *  @param parent A parent node.
     *  @return The number of children.
     */
    public int getChildCount(Object parent) {
        List classes = _classes(parent);
        int numClasses = classes.size();

        return numClasses + super.getChildCount(parent);
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {

        List classes = _classes(parent);

        int index = classes.indexOf(child);
        if (index >= 0) {
            return index;
        } else {
            // Object is not a class definition. Defer to the base class.
            int numClasses = classes.size();
            index = super.getIndexOfChild(parent, child);
            if (index >= 0) {
                return index + numClasses;
            }
        }
        return -1;
    }

    /** Return true if the object is a leaf node.  An object is a leaf
     *  node if it has no children that are instances of one of the classes
     *  specified by setFilter(), if a filter has been specified.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        // FIXME: Ignoring setFilter for now.

        if (_classes(object).size() > 0) return false;

        return super.isLeaf(object);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the list of classes, or an empty list if there are none.
     *  Override this method if you wish to show only a subset of the
     *  classes.
     *  @return A list of classes.
     */
    protected List _classes(Object object) {
        if (!(object instanceof CompositeEntity)) {
            return Collections.EMPTY_LIST;
        }
        return ((CompositeEntity)object).classDefinitionList();
    }
}
