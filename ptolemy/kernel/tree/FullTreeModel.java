/* A tree model for Ptolemy II objects, for use with JTree.

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

package ptolemy.kernel.tree;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.CompositeEntity;

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
A tree model for Ptolemy II models.  Nodes in this tree contain
the following child elements, in this order:
<ul>
<li> attributes
<li> ports
<li> relations
<li> contained entities
</ul>

FIXME: More information.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class FullTreeModel extends EntityTreeModel {

    /** Create a new tree model with the specified root.
     *  @param root The root of the tree.
     */
    public FullTreeModel(CompositeEntity root) {
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
        // FIXME: doing attributes only.
	if(!(parent instanceof NamedObj)) return null;
        NamedObj obj = (NamedObj)parent;
        List attributes = obj.attributeList();
        int numAttributes = attributes.size();
	if(index >= numAttributes) {
            return super.getChild(parent, index - numAttributes);
        } else if(index < 0) {
            return null;
        } else {
            return attributes.get(index);
        }
    }

    /** Return the number of children of the given parent.
     *  This is the number attributes, ports, relations, and contained
     *  entities, filtered by the filter specified by setFilter(),
     *  if any has been specified.
     *  @param parent A parent node.
     *  @return The number of children.
     */
    public int getChildCount(Object parent) {
        // FIXME: Only doing attributes for now.
	if (!(parent instanceof NamedObj)) return 0;
        NamedObj obj = (NamedObj)parent;
        List attributes = obj.attributeList();
        int numAttributes = attributes.size();
        return numAttributes + super.getChildCount(parent);
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {
        // FIXME: doing attributes only.
	if (!(parent instanceof NamedObj)) return -1;
        NamedObj obj = (NamedObj)parent;
        List attributes = obj.attributeList();
        int index = attributes.indexOf(child);
	if(index >= 0) return index;
        else return super.getIndexOfChild(parent, child);
    }

    /** Return true if the object is a leaf node.  An object is a leaf
     *  node if it has no children that are instances of one of the classes
     *  specified by setFilter(), if a filter has been specified.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        // FIXME: Only doing attributes for now.
	if (!(object instanceof NamedObj)) return true;
        NamedObj obj = (NamedObj)object;
        List attributes = obj.attributeList();
        int numAttributes = attributes.size();
        if (numAttributes > 0) return false;
        else return super.isLeaf(object);
    }
}
