/* A tree model for the Vergil library panel.

 Copyright (c) 2000-2001 The Regents of the University of California.
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
import ptolemy.kernel.util.*;
import ptolemy.moml.EntityLibrary;
import ptolemy.vergil.icon.EditorIcon;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// VisibleTreeModel
/**

A tree model for the Vergil library panel.  This is a tree model that
shows entities, ports, relations, and visible attributes.  An attribute
is visible if it contains an attribute of class EditorIcon, or an
attribute named "_iconDescription" or "_iconSmallDescription".
Attributes that are not visible are not shown.
A composite entity that contains an attribute with name "_libraryMarker"
is treated as a sublibrary. A composite entity without such an attribute
is treated as an atomic entity.
This is designed for use with JTree, which renders the hierarchy.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class VisibleTreeModel extends EntityTreeModel {

    /** Create a new tree model with the specified root.
     *  @param root The root of the tree.
     */
    public VisibleTreeModel(CompositeEntity root) {
	super(root);
        _workspace = root.workspace();
        _workspaceVersion = _workspace.getVersion();
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
	if (index > getChildCount(parent) || index < 0) return null;
	if (!(parent instanceof NamedObj)) return null;
        NamedObj obj = (NamedObj)parent;

        List attributes = _visibleAttributes(obj);
        int numAttributes = attributes.size();
        if (index < numAttributes) {
            return attributes.get(index);
        }
        return super.getChild(parent, index - numAttributes);

        // FIXME: need to do ports and relations.
    }

    /** Return the number of children of the given parent.
     *  This is the number of visible attributes, ports, relations,
     *  and contained entities, filtered by the filter specified by
     *  setFilter(), if any has been specified.
     *  @param parent A parent node.
     *  @return The number of children.
     */
    public int getChildCount(Object parent) {
	if (!(parent instanceof NamedObj)) return 0;
        NamedObj obj = (NamedObj)parent;
        List attributes = _visibleAttributes(obj);
        int numAttributes = attributes.size();
        int result = numAttributes + super.getChildCount(parent);
        return result;
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {
        // FIXME: doing attributes only.
	if (!(parent instanceof NamedObj)) return -1;
        NamedObj obj = (NamedObj)parent;
        List attributes = _visibleAttributes(obj);
        int index = attributes.indexOf(child);
	if (index >= 0) return index;
        else return super.getIndexOfChild(parent, child);
    }

    /** Return true if the object is a leaf node.  An object is a leaf
     *  node if it has no children that are instances of one of the classes
     *  specified by setFilter(), if a filter has been specified.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        // NOTE: handle EntityLibrary specially to prevent evaluation
        // of the library prematurely.
        if (object instanceof EntityLibrary) return false;

        // If the object is not an instance of NamedObj,
        // then it is certainly atomic.
	if (!(object instanceof NamedObj)) return true;
        NamedObj obj = (NamedObj)object;

        // If the object is an instance of CompositeEntity, but does not
        // contain an attribute named "_libraryMarker", then treat it as an
        // atomic entity.
        if (obj instanceof CompositeEntity) {
            Attribute marker = obj.getAttribute("_libraryMarker");
            if (marker == null) {
                return true;
            }
        }

        // FIXME: Only doing attributes for now.
        List attributes = _visibleAttributes(obj);
        int numAttributes = attributes.size();
        if (numAttributes > 0) return false;
        else return super.isLeaf(object);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the list of visible attributes.
     */
    private List _visibleAttributes(NamedObj object) {
        // Use the cached list, if possible.
        long version = _workspace.getVersion();
        if (version == _workspaceVersion) {
            Object result = _attributeListCache.get(object);
            if (result != null) return (List)result;
        }
        // Cache is not valid.
        List result = new LinkedList();
        Iterator attributes = object.attributeList().iterator();
        while (attributes.hasNext()) {
            Attribute attribute = (Attribute)attributes.next();
            List iconList = attribute.attributeList(EditorIcon.class);
            if (iconList.size() > 0
                    || attribute.getAttribute("_iconDescription") != null
                    || attribute.getAttribute("_iconSmallDescription")!= null) {
                result.add(attribute);
            }
        }
        _attributeListCache.put(object, result);
        _workspaceVersion = _workspace.getVersion();
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Workspace and version information.
    private Workspace _workspace;
    private long _workspaceVersion;

    // Cache for visible attributes;
    private Map _attributeListCache = new HashMap();
}
