/* A tree model for ptolemy models

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

package ptolemy.actor.gui.util;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import java.util.*;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import java.awt.geom.*;
import javax.swing.SwingConstants;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

//////////////////////////////////////////////////////////////////////////
//// PtolemyTreeModel
/**
A tree model for ptolemy models.  This class makes it easy to view ptolemy
models in a JTree.  Entities in the model become nodes in the tree, 
and composite entities are internal nodes.

@see #PtolemyTreeCellRenderer
@author Steve Neuendorffer
@version $Id$
*/
public class PtolemyTreeModel implements TreeModel {

    /**
     * Create a new tree model, whose root is the given entity.
     */
    public PtolemyTreeModel(Entity entity) {
	_root = entity;
    }

    /**
     * Add a listener to this model.
     */
    public void addTreeModelListener(TreeModelListener l) {
	_listenerList.add(l);
    }

    /**
     * Get the child at the given index of the given parent.  The parent is
     * assumed to be contained within this model.
     */
    public Object getChild(Object parent, int index) {
	if(index > getChildCount(parent)) return null;
	CompositeEntity entity = (CompositeEntity)parent;
	return entity.entityList().get(index);
    }

    /**
     * Return the number of children of the given object.  The parent is
     * assumed to be contained within this model.
     */
    public int getChildCount(Object parent) {
	if(!(parent instanceof CompositeEntity)) return 0;
	CompositeEntity entity = (CompositeEntity)parent;
	return entity.entityList().size();
    }

    /**
     * Return the index of the given child within the given parent.  If the
     * parent is not contained in the child, or the parent is not a composite
     * entity, return -1.  The parent is
     * assumed to be contained within this model.
     */
    public int getIndexOfChild(Object parent, Object child) {
	if(!(parent instanceof CompositeEntity)) return -1;
	CompositeEntity entity = (CompositeEntity)parent;
	return entity.entityList().indexOf(child);
    }

    /**
     * Get the root of this tree model.
     */
    public Object getRoot() {
	return _root;
    }

    /**
     * Return true if the object is a leaf.
     * @return true if the object is not a composite entity.
     */
    public boolean isLeaf(Object object) {
	return !(object instanceof CompositeEntity);
    }

    /**
     * Remove the given listener.
     */
    public void removeTreeModelListener(TreeModelListener l) {
	_listenerList.remove(l);
    }

    /**
     * Notify listeners that the object at the given path has changed.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
	Iterator listeners = _listenerList.iterator();
	TreeModelEvent event = new TreeModelEvent(this, path);
	while(listeners.hasNext()) {
	    TreeModelListener listener = (TreeModelListener)listeners.next();
	    listener.treeStructureChanged(event);
	}
    }

    // The list of listeners.
    private List _listenerList = new LinkedList();
    // The root of the tree.
    private Entity _root;
}
