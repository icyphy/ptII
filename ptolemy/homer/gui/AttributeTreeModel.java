/* A tree model for Ptolemy II Actors and Attributes, for use with JTree.

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
package ptolemy.homer.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
@since Ptolemy II 10.0
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

    /** Set the filter applied to the underlying model.
     *  @param filter The filter to apply to all Nameables.
     */
    public void applyFilter(String filter) {
        _filter = filter;
        valueForPathChanged(new TreePath(getRoot()), null);
    }

    /** Get the child of the given parent at the given index.
     *  If the child does not exist, then return null.
     *  @param parent A node in the tree.
     *  @param index The index of the desired child.
     *  @return A node, or null if there is no such child.
     */
    @Override
    public Object getChild(Object parent, int index) {
        List attributes = _getChildren(parent);
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
    @Override
    public int getChildCount(Object parent) {
        return _getChildren(parent).size() + super.getChildCount(parent);
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @param parent The parent.
     *  @param child The child.
     *  @return The index of the specified child.
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return _getChildren(parent).indexOf(child);
    }

    /** Return true if the object is a leaf node.  An object is a leaf
     *  node if it has no children that are instances of one of the classes
     *  specified by setFilter(), if a filter has been specified.
     *  @param object The object.
     *  @return True if the node has no children.
     */
    @Override
    public boolean isLeaf(Object object) {
        if (object == null) {
            return true;
        } else if (_getChildren(object).size() > 0) {
            return false;
        } else {
            return super.isLeaf(object);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a filtered listing of attributes within the object based on the filtering criteria.
     *  @param parent The parent object of the attributes.
     *  @return Return filtered list of attributes.
     */
    private List _getChildren(Object parent) {
        if (!(parent instanceof NamedObj)) {
            return Collections.emptyList();
        }

        // Load attributes if any exist at this level.
        List children = new ArrayList();
        for (Object attribute : ((NamedObj) parent)
                .attributeList(Settable.class)) {
            if (((Settable) attribute).getVisibility().equals(Settable.FULL)) {
                if (_filter != null && _filter.length() > 0) {
                    if (((Nameable) attribute).getFullName()
                            .toLowerCase(Locale.getDefault())
                            .contains(_filter.toLowerCase(Locale.getDefault()))) {
                        children.add(attribute);
                    }
                } else {
                    children.add(attribute);
                }
            }
        }

        return children;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The string filter being applied to tree nodes.
     */
    private String _filter;
}
