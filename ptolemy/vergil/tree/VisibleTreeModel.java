/* A tree model for the Vergil library panel.

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
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;
import ptolemy.vergil.icon.EditorIcon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// VisibleTreeModel
/**

A tree model for the Vergil library panel.  This is a tree model that
shows all entities and some ports, relations, and attributes.  The ports,
relations, and attributes that it shows are those that
contains an attribute of class EditorIcon, or that contain an
attribute named "_iconDescription" or "_smallIconDescription".
A composite entity that contains an attribute with name "_libraryMarker"
is treated as a sublibrary. A composite entity without such an attribute
is treated as an atomic entity.
This is designed for use with JTree, which renders the hierarchy.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class VisibleTreeModel extends FullTreeModel {

    /** Create a new tree model with the specified root.
     *  @param root The root of the tree.
     */
    public VisibleTreeModel(CompositeEntity root) {
        super(root);
        _workspace = root.workspace();
        _workspaceAttributeVersion = _workspace.getVersion();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the object is a leaf node.  An object is a leaf
     *  node if it has no children that are instances of one of the classes
     *  specified by setFilter(), if a filter has been specified.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        // NOTE: handle EntityLibrary specially to prevent evaluation
        // of the library prematurely.
        if (object instanceof EntityLibrary) return false;

        // If the object is an instance of CompositeEntity, but does not
        // contain an attribute named "_libraryMarker", then treat it as an
        // atomic entity.
        if (object instanceof CompositeEntity) {
            Attribute marker = ((CompositeEntity)object).getAttribute(
                    "_libraryMarker");
            if (marker == null) {
                return true;
            }
        }

        // Defer to the parent for the rest.
        return super.isLeaf(object);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the list of attributes, or an empty list if there are none.
     *  Override this method if you wish to show only a subset of the
     *  attributes.
     *  @return A list of attributes.
     */
    protected List _attributes(Object object) {
        if (!(object instanceof NamedObj)) {
            return Collections.EMPTY_LIST;
        }

        // Use the cached list, if possible.
        long version = _workspace.getVersion();
        if (version == _workspaceAttributeVersion) {
            Object result = _attributeListCache.get(object);
            if (result != null) return (List)result;
        }
        // Cache is not valid.
        List result = new LinkedList();
        Iterator attributes = ((NamedObj)object).attributeList().iterator();
        while (attributes.hasNext()) {
            NamedObj attribute = (NamedObj)attributes.next();
            if (_isVisible(attribute)) {
                result.add(attribute);
            }
        }
        _attributeListCache.put(object, result);
        _workspaceAttributeVersion = _workspace.getVersion();
        return result;
    }

    /** Return true if the object contains either an attribute of
     *  class EditorIcon or an attribute of any class named
     *  "_iconDescription" or "_smallIconDescription".  This
     *  will result in the object being rendered in the library.
     *  @return True if the object is to be rendered in the library.
     */
    protected boolean _isVisible(NamedObj object) {
        List iconList = object.attributeList(EditorIcon.class);
        if (iconList.size() > 0
                || object.getAttribute("_iconDescription") != null
                || object.getAttribute("_smallIconDescription")!= null) {
            return true;
        } else {
            return false;
        }
    }

    /** Return the list of ports, or an empty list if there are none.
     *  Override this method if you wish to show only a subset of the
     *  ports.
     *  @return A list of ports.
     */
    protected List _ports(Object object) {
        if (!(object instanceof Entity)) {
            return Collections.EMPTY_LIST;
        }

        // Use the cached list, if possible.
        long version = _workspace.getVersion();
        if (version == _workspacePortVersion) {
            Object result = _portListCache.get(object);
            if (result != null) return (List)result;
        }
        // Cache is not valid.
        List result = new LinkedList();
        Iterator ports = ((Entity)object).portList().iterator();
        while (ports.hasNext()) {
            NamedObj port = (NamedObj)ports.next();
            if (_isVisible(port)) {
                result.add(port);
            }
        }
        _portListCache.put(object, result);
        _workspacePortVersion = _workspace.getVersion();
        return result;
    }

    /** Return the list of relations, or an empty list if there are none.
     *  Override this method if you wish to show only a subset of the
     *  relations.
     *  @return A list of relations.
     */
    protected List _relations(Object object) {
        if (!(object instanceof CompositeEntity)) {
            return Collections.EMPTY_LIST;
        }

        // Use the cached list, if possible.
        long version = _workspace.getVersion();
        if (version == _workspaceRelationVersion) {
            Object result = _relationListCache.get(object);
            if (result != null) return (List)result;
        }
        // Cache is not valid.
        List result = new LinkedList();
        Iterator relations
            = ((CompositeEntity)object).relationList().iterator();
        while (relations.hasNext()) {
            NamedObj relation = (NamedObj)relations.next();
            if (_isVisible(relation)) {
                result.add(relation);
            }
        }
        _relationListCache.put(object, result);
        _workspaceRelationVersion = _workspace.getVersion();
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Workspace and version information.
    private Workspace _workspace;
    private long _workspaceAttributeVersion;
    private long _workspacePortVersion;
    private long _workspaceRelationVersion;

    // Cache for visible attributes.
    private Map _attributeListCache = new HashMap();

    // Cache for visible ports.
    private Map _portListCache = new HashMap();

    // Cache for visible relations.
    private Map _relationListCache = new HashMap();
}
