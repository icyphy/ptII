/* A tree model for Ptolemy II objects, for use with JTree.

 Copyright (c) 2000-2014 The Regents of the University of California.
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

 */
package ptolemy.vergil.tree;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// EntityTreeModel

/**

 A tree model for Ptolemy II objects. This class makes it easy to
 view ptolemy models in a JTree, which renders the hierarchy.
 This base class handles only composite entities and their contained
 entities.  It does not include entities that are class definitions.
 Derived classes represent more (or less) of the Ptolemy II model.

 @author Steve Neuendorffer and Edward A. Lee, Contributor: Jianwu Wang
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
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
     *  @see #removeTreeModelListener(TreeModelListener)
     */
    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        // In http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4801,
        // Jianwu Wang found that the _listenerList attribute in
        // ptolemy.vergil.tree.EntityTreeModel was leaking memory.
        // This attribute keeps holding some references, so that
        // instances of EntityTreeModel and its sub-classes can not be
        // GCed when a window is open and closed.
        //
        // When it and its sub-classes are used in Kepler,
        // addTreeModelListener() and removeTreeModelListener()
        // functions of EntityTreeModel class are called implicitly.
        // Jianwu did not find a good way to clean up after a window
        // is closed.  Changing the _listenerList attribute using
        // WeakReference fixe the leak.
        _listenerList.add(new WeakReference(listener));
    }

    /** Get the child of the given parent at the given index.
     *  If the child does not exist, then return null.
     *  In this base class, a child is a contained entity.
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    @Override
    public Object getChild(Object parent, int index) {
        if (index > getChildCount(parent)) {
            return null;
        }

        CompositeEntity entity = (CompositeEntity) parent;
        return entity.entityList().get(index);
    }

    /** Return the number of children of the given parent, which in
     *  this base class is the number of contained entities.
     *  @param parent A parent node.
     *  @return The number of contained entities.
     */
    @Override
    public int getChildCount(Object parent) {
        if (!(parent instanceof CompositeEntity)) {
            return 0;
        }

        CompositeEntity entity = (CompositeEntity) parent;
        return entity.numberOfEntities();
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child or is not an instance
     *  of CompositeEntity return -1.
     *  @param parent The parent, which is usually a CompositeEntity.
     *  @param child The child.
     *  @return The index of the specified child.
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (!(parent instanceof CompositeEntity)) {
            return -1;
        }

        CompositeEntity entity = (CompositeEntity) parent;
        return entity.entityList().indexOf(child);
    }

    /** Get the root of this tree model.
     *  @return A NamedObj, usually an Entity.
     *  @see #setRoot(NamedObj)
     */
    @Override
    public Object getRoot() {
        return _root;
    }

    /** Return true if the object is a leaf node.  In this base class,
     *  an object is a leaf node if it is not an instance of CompositeEntity.
     *  @param object The object in question.
     *  @return True if the node has no children.
     */
    @Override
    public boolean isLeaf(Object object) {
        if (!(object instanceof CompositeEntity)) {
            return true;
        }

        // NOTE: The following is probably not a good idea because it
        // will force evaluation of the contents of a Library prematurely.
        // if (((CompositeEntity)object).numEntities() == 0) return true;
        return false;
    }

    /** Set the object that this treemodel looks at.
     *  @param root The root NamedObj.  BasicGraphFrame.dispose() calls
     *  this method and passes null as the value of root.
     *  @see #getRoot()
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
     *  @see #addTreeModelListener(TreeModelListener)
     */
    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        int i = 0;
        int size = _listenerList.size();
        while (i < size) {
            Object _listener = ((WeakReference) _listenerList.get(i)).get();
            if (_listener == null) {
                _listenerList.remove(i);
                size--;
            } else {
                if (_listener == listener) {
                    _listenerList.remove(i);
                    size--;
                }
                i++;
            }
        }
    }

    /** Notify listeners that the object at the given path has changed.
     *  @param path The path of the node that has changed.
     *  @param newValue The new value of the node.
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        Iterator listeners = _listenerList.iterator();
        TreeModelEvent event = new TreeModelEvent(this, path);

        while (listeners.hasNext()) {
            Object listener = ((WeakReference) listeners.next()).get();
            if (listener != null) {
                ((TreeModelListener) listener).treeStructureChanged(event);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A ChangeListener that updates the Tree. */
    public class TreeUpdateListener implements ChangeListener {
        /** Trigger an update of the tree.  If the change
         *  request indicates that it is localized, then only
         *  the relevant portion of the tree is updated.
         *  Otherwise, the entire tree is modified.
         */
        @Override
        public void changeExecuted(final ChangeRequest change) {
            // If the change is not structural, say for example SetVariable setting its variable,
            // then ignore the change because it will not modify the tree.
            if (!change.isStructuralChange()) {
                return;
            }
            //System.out.println("change = " + change + change.getDescription());
            // Note that this should be in the swing thread.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ArrayList path = new ArrayList();
                    Object root = getRoot();

                    // If the change request is local, then it
                    // should return non-null to this method.
                    NamedObj locality = change.getLocality();

                    if (locality == null) {
                        if (root != null) {
                            path.add(0, root);
                        } else {
                            // BasicGraphFrame.dispose() calls setRoot(null),
                            // so root might be null.

                            // Exporting gt models to html in a
                            // headless environment with Xvfb results
                            // in root being null.  However, this is
                            // not always reproducible.

                            // To replicate on sisyphus (RHEL 6) as
                            // the hudson user:

                            // Xvfb :2 -screen 0 1024x768x24 &
                            // export DISPLAY=localhost:2.0
                            // ant test.single -Dtest.name=ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest -Djunit.formatter=plain
                            // The error is
                            //    java.lang.IllegalArgumentException: Last path component must be non-null
                            //    at javax.swing.tree.TreePath.<init>(TreePath.java:105)

                            // Just return, our work here is done.
                            return;
                        }
                    } else {
                        // The change has a declared locality.
                        // Construct a path to that locality.
                        NamedObj container = locality;

                        while (container != root) {
                            if (container == null) {
                                // This should not occur, but if it
                                // does, we revert to just using the
                                // root.
                                path = new ArrayList();
                                path.add(0, root);
                                break;
                            }

                            path.add(0, container);
                            container = container.getContainer();
                        }
                    }

                    try {
                        valueForPathChanged(new TreePath(path.toArray()),
                                locality);
                    } catch (IllegalArgumentException ex) {
                        throw new RuntimeException(
                                "Failed to instantiate a TreePath, path was "
                                        + Arrays.toString(path.toArray())
                                        + " locality was " + locality
                                        + " root was: " + root
                                        + " changeRequest was: " + change
                                        + " changeRequest description: "
                                        + change.getDescription()
                                        + " changeRequest source: "
                                        + change.getSource()
                                        + " changeRequest class: "
                                        + change.getClass().getName(), ex);
                    }
                }
            });
        }

        /** Trigger an update of the tree.  If the change
         *  request indicates that it is localized, then only
         *  the relevant portion of the tree is updated.
         *  Otherwise, the entire tree is modified.
         */
        @Override
        public void changeFailed(ChangeRequest change, Exception exception) {
            // We do the same thing whether the change succeeded or failed.
            changeExecuted(change);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The root of the tree. */
    protected NamedObj _root = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of listeners. */
    private List _listenerList = new LinkedList<WeakReference>();

    /** The model listener. */
    private ChangeListener _rootListener = new TreeUpdateListener();
}
