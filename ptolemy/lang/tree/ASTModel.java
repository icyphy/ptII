/* A tree model for abstract syntax trees, for use with JTree.

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

package ptolemy.lang.tree;

import ptolemy.lang.TreeNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

//////////////////////////////////////////////////////////////////////////
//// ASTModel
/**
A tree model for abstract syntax trees.
This class makes it easy to view abstract syntax trees in a JTree.
To use this class, do
<pre>
JTree tree = new JTree(new ASTModel(rootNode));
tree.setCellRenderer(new ASTNodeRenderer());
</pre>

@see #ASTNodeRenderer
@author Edward A. Lee
@version $Id$
*/
public class ASTModel implements TreeModel {

    /** Create a new tree model, with the specified root.
     *  @param root The root of the tree.
     */
    public ASTModel(TreeNode root) {
	_root = root;
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
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    public Object getChild(Object parent, int index) {
        // FIXME: There should be a common base class for nodes
        // so that this nonsense is not necessary.
        if (parent instanceof TreeNode) {
            return ((TreeNode)parent).getChild(index);
        } else if (parent instanceof List) {
            return ((List)parent).get(index);
        } else {
            return null;
        }
    }

    /** Return the number of children of the given parent.
     *  @param parent A parent node.
     *  @return The number of children.
     */
    public int getChildCount(Object parent) {
        // FIXME: There should be a common base class for nodes
        // so that this nonsense is not necessary.
        if (parent instanceof TreeNode) {
            List childList = ((TreeNode)parent).children();
            return childList.size();
        } else if (parent instanceof List) {
            return ((List)parent).size();
        } else {
            return 0;
        }
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {
        // FIXME: There should be a common base class for nodes
        // so that this nonsense is not necessary.
        if (parent instanceof TreeNode) {
            List childList = ((TreeNode)parent).children();
            return childList.indexOf(child);
        } else if (parent instanceof List) {
            return ((List)parent).indexOf(child);
        } else {
            return -1;
        }
    }

    /** Get the root of this tree model.
     *  @return A TreeNode.
     */
    public Object getRoot() {
	return _root;
    }

    /** Return true if the object is a leaf node.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        // FIXME: There should be a common base class for nodes
        // so that this nonsense is not necessary.
        if (object instanceof TreeNode) {
            List childList = ((TreeNode)object).children();
            return (childList == null || childList.size() == 0);
        } else if (object instanceof List) {
            return (((List)object).size() == 0);
        } else {
            return false;
        }
    }

    /** Remove the specified listener.
     *  @param listener The listener to remove.
     */
    public void removeTreeModelListener(TreeModelListener listener) {
	_listenerList.remove(listener);
    }

    /** Notify listeners that the object at the given path has changed.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
	Iterator listeners = _listenerList.iterator();
	TreeModelEvent event = new TreeModelEvent(this, path);
	while(listeners.hasNext()) {
	    TreeModelListener listener = (TreeModelListener)listeners.next();
	    listener.treeStructureChanged(event);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The list of listeners.
    private List _listenerList = new LinkedList();
    // The root of the tree.
    private TreeNode _root;
}
