/* A tree model for the Vergil library panel.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.tree;

// FIXME: trim this.
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.CompositeEntity;
import ptolemy.moml.MoMLParser;

import diva.gui.Application;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

//////////////////////////////////////////////////////////////////////////
//// LibraryTreeModel
/**

A tree model for the Vergil library panel.  This is a singleton
class that represents a list of top-level composite entities, each
of which is a library.
It is designed for use with JTree, which renders the hierarchy.
A list of top-level composite entities is used instead a single
top-level composite entity to get around the restrictions in Ptolemy II
on containment.  That is, a given composite entity can only contain
composite entities of a particular class.  Normally, you will not
want to display the root node, so call setRootVisible(false) on the
JTree.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class LibraryTreeModel extends EntityTreeModel {

    /** Create a new library tree model.  This is private because this
     *  is a singleton class.
     */
    private LibraryTreeModel() {
        // The root node is a list.
	super(new LinkedList());
        _directory = (List)_root;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a top-level library.
     *  @param library A top-level library.
     */
    public static void addLibrary(CompositeEntity library) {
        instance._directory.add(library);
        instance.valueForPathChanged(
                new TreePath(instance._root), instance._root);
        // Expand the newly added library.
        Object[] path = new Object[2];
        path[0] = instance._root;
        path[1] = library;
        if (instance._pTree != null) {
            instance._pTree.expandPath(new TreePath(path));
        }
    }

    /** Create a JTree object to display this library, and populate it
     *  with the master actor library.  If the JTree object has already
     *  been created, then simply return it.
     *  @param application The application, for error reporting, or null
     *   to send errors to the standard error stream.
     */
    public static JTree createTree(Application application) {
        if (instance._pTree != null) return instance._pTree;
        // If this fails, we don't really want to throw an exception
        // so that users can manually open libraries.
        try {
            URL entitylibURL = ClassLoader.getSystemClassLoader().getResource(
                    "ptolemy/vergil/lib/rootEntityLibrary.xml");
            if (entitylibURL == null) {
                throw new Exception(
                        "Cannot open master library: "
                        + "ptolemy/vergil/lib/rootEntityLibrary.xml");
            }
            MoMLParser parser = new MoMLParser();
            instance._masterLibrary = (CompositeEntity) parser.parse(
                    entitylibURL, entitylibURL.openStream());
            instance._directory.add(instance._masterLibrary);
        } catch (Exception ex) {
            if (application != null) {
                application.showError(
                        "Failed to open the master library: ", ex);
            } else {
                System.err.println("Failed to open the master library:\n" + ex);
            }
        }

        instance._pTree = new PTree(instance);
        // Do not show the root, since it simply aggregates libraries.
        instance._pTree.setRootVisible(false);
        // Expand the master library, if it exists.
        if (instance._masterLibrary != null) {
            Object[] path = new Object[2];
            path[0] = instance._root;
            path[1] = instance._masterLibrary;
            instance._pTree.expandPath(new TreePath(path));
        }
        return instance._pTree;
    }

    /** Get the child of the given parent at the given index.
     *  If the child does not exist, then return null.
     *  In this base class, a child is a contained entity.
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    public Object getChild(Object parent, int index) {
        if (parent == _directory) {
            try {
                return _directory.get(index);
            } catch (IndexOutOfBoundsException ex) {
                return null;
            }
        } else {
            return super.getChild(parent, index);
        }
    }

    /** Return the number of children of the given parent, which in
     *  this base class is the number of contained entities.
     *  @param parent A parent node.
     *  @return The number of contained entities.
     */
    public int getChildCount(Object parent) {
        if (parent == _root) {
            return _directory.size();
        } else {
            return super.getChildCount(parent);
        }
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {
        if (parent == _root) {
            return _directory.indexOf(child);
        } else {
            return super.getIndexOfChild(parent, child);
        }
    }

    /** Return true if the object is a leaf node.  In this base class,
     *  an object is a leaf node if it is not an instance of CompositeEntity.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        if (object == _root) {
            return false;
        } else {
            return super.isLeaf(object);
        }
    }

    /** Remove a top-level library.  If the specified library is not
     *  present, then do nothing.
     *  @param index The index of a top-level library.
     */
    public static void removeLibrary(int index) {
        try {
            instance._directory.remove(index);
            instance.valueForPathChanged(
                    new TreePath(instance._root), instance._root);
        } catch (IndexOutOfBoundsException ex) {}
    }

    /** Remove a top-level library.  If the specified library is not
     *  present, then do nothing.
     *  @param library A top-level library.
     */
    public static void removeLibrary(CompositeEntity library) {
        instance._directory.remove(library);
        instance.valueForPathChanged(
                new TreePath(instance._root), instance._root);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The single instance of this class. */
    public static final LibraryTreeModel instance = new LibraryTreeModel();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The root, as a list. */
    private List _directory;

    /** The master library of actors. */
    private CompositeEntity _masterLibrary;

    /** The JTree displaying the library. */
    private JTree _pTree;
}
