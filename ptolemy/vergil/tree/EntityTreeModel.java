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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

//////////////////////////////////////////////////////////////////////////
//// EntityTreeModel
/**

A tree model for Ptolemy II objects. This class makes it easy to
view ptolemy models in a JTree, which renders the hierarchy.
This base class handles only composite entities and their contained
entities.  It does not include entities that are class definitions.
Derived classes represent more (or less) of the Ptolemy II model.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class EntityTreeModel implements TreeModel {

    /** Create a new tree model with the specified root.
     *  Normally the root is an instance of CompositeEntity, but other
     *  root objects might be used by derived classes.
     *  @param root The root of the tree.
     */
    public EntityTreeModel(NamedObj root) {
        setRoot(root);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to this model.
     *  @param listener The listener to add.
     */
    public void addTreeModelListener(TreeModelListener listener) {
        _listenerList.add(listener);
    }

    /** Get the child of the given parent at the given index.
     *  If the child does not exist, then return null.
     *  In this base class, a child is a contained entity.
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    public Object getChild(Object parent, int index) {
        if (index > getChildCount(parent)) return null;
        CompositeEntity entity = (CompositeEntity)parent;
        return entity.entityList().get(index);
    }

    /** Return the number of children of the given parent, which in
     *  this base class is the number of contained entities.
     *  @param parent A parent node.
     *  @return The number of contained entities.
     */
    public int getChildCount(Object parent) {
        if (!(parent instanceof CompositeEntity)) return 0;
        CompositeEntity entity = (CompositeEntity)parent;
        return entity.numberOfEntities();
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {
        if (!(parent instanceof CompositeEntity)) return -1;
        CompositeEntity entity = (CompositeEntity)parent;
        return entity.entityList().indexOf(child);
    }

    /** Get the root of this tree model.
     *  @return An Entity.
     */
    public Object getRoot() {
        return _root;
    }

    /** Return true if the object is a leaf node.  In this base class,
     *  an object is a leaf node if it is not an instance of CompositeEntity.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        if (!(object instanceof CompositeEntity)) return true;
        // NOTE: The following is probably not a good idea because it
        // will force evaluation of the contents of a Library prematurely.
        // if (((CompositeEntity)object).numEntities() == 0) return true;
        return false;
    }

    /** Set the object that this treemodel looks at.
     */
    public void setRoot(NamedObj root) {
        if (_root != null) {
            _root.removeChangeListener(_rootListener);
        }
        _root = root;
        if (_root != null) {
            _root.addChangeListener(_rootListener);
        }
    }

    /** Remove the specified listener.
     *  @param listener The listener to remove.
     */
    public void removeTreeModelListener(TreeModelListener listener) {
        _listenerList.remove(listener);
    }

    /** Notify listeners that the object at the given path has changed.
     *  @param path The path of the node that has changed.
     *  @param newValue The new value of the node.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        Iterator listeners = _listenerList.iterator();
        TreeModelEvent event = new TreeModelEvent(this, path);
        while (listeners.hasNext()) {
            TreeModelListener listener = (TreeModelListener)listeners.next();
            listener.treeStructureChanged(event);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public class TreeUpdateListener implements ChangeListener {
        /** Trigger an update of the entire tree.
         */
        public void changeExecuted(ChangeRequest change) {
            // FIXME it would be nice if there was more information in
            // the change about the context of the change.
            valueForPathChanged(new TreePath(getRoot()), getRoot());
        }

        /** Trigger an update of the entire tree.
         */
        public void changeFailed(ChangeRequest change, Exception exception) {
            valueForPathChanged(new TreePath(getRoot()), getRoot());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The root of the tree.
    protected NamedObj _root = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of listeners.
    private List _listenerList = new LinkedList();

    // The model listener.
    private ChangeListener _rootListener = new TreeUpdateListener();
}
