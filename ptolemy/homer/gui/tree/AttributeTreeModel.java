/* A tree model for Ptolemy II Actors and Attributes, for use with JTree.

Copyright (c) 2000-2010 The Regents of the University of California.
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
package ptolemy.homer.gui.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.tree.TreePath;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.vergil.tree.ClassAndEntityTreeModel;

///////////////////////////////////////////////////////////////////
////AttributeTreeModel

/**
A tree model for Ptolemy II models.  Nodes in this tree contain
the following Actors and their Attributes as child elements.

The indexes of the attributes are 0 to a-1, where a is the
number of attributes. 

@author Steve Neuendorffer and Edward A. Lee Contributors: Ishwinder Singh  
@version $Id$
@since Ptolemy II 1.0
@Pt.ProposedRating Red (ishwinde)
@Pt.AcceptedRating Red (ishwinde)
*/
public class AttributeTreeModel extends ClassAndEntityTreeModel {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create a new tree model with the specified root.
     *  @param root The root of the tree.
     */
    public AttributeTreeModel(CompositeEntity root) {
        super(root);
        _filter = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public List getAttributes(Object parent) {
        if (!(parent instanceof NamedObj)) {
            return Collections.emptyList();
        }

        List children = new ArrayList();
        for (Object attribute : ((NamedObj) parent).attributeList()) {
            if (attribute instanceof Settable) {
                if (((Settable) attribute).getVisibility()
                        .equals(Settable.FULL)) {
                    if (attribute instanceof Nameable) {
                        if ((_filter != null) && (_filter.length() > 0)) {
                            if (((Nameable) attribute).getFullName()
                                    .toLowerCase()
                                    .contains(_filter.toLowerCase())) {
                                children.add(attribute);
                            }
                        } else {
                            children.add(attribute);
                        }
                    }
                }
            }
        }

        return children;
    }

    /** Get the child of the given parent at the given index.
     *  If the child does not exist, then return null.
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    public Object getChild(Object parent, int index) {
        List attributes = getAttributes(parent);
        if (index > attributes.size() - 1) {
            return super.getChild(parent, index - attributes.size());
        } else if (index >= 0) {
            return attributes.get(index);
        } else {
            return null;
        }
    }

    /** Return the number of children of the given parent.
     *  @param parent A parent node.
     *  @return The number of children.
     */
    public int getChildCount(Object parent) {
        return getAttributes(parent).size() + super.getChildCount(parent);
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @param parent The parent.
     *  @param child The child.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {
        return getAttributes(parent).indexOf(child);
    }

    /** Return true if the object is a leaf node.  An object is a leaf
     *  node if it has no children that are instances of one of the classes
     *  specified by setFilter(), if a filter has been specified.
     *  @param object The object.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        if (object == null) {
            return true;
        } else if (getAttributes(object).size() > 0) {
            return false;
        } else {
            return super.isLeaf(object);
        }
    }

    /**  Set the filter applied to the underlying model.
     *  @param filter The filter to apply to all Nameables.
     */
    public void applyFilter(String filter) {
        _filter = filter;
        valueForPathChanged(new TreePath(getRoot()), null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The string filter being applied to tree nodes.
     */
    private String _filter;
}
