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

import java.util.Collections;
import java.util.List;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// FullTreeModel
/**
A tree model for Ptolemy II models.  Nodes in this tree contain
the following child elements, in this order:
<ul>
<li> attributes
<li> ports
<li> relations
<li> class definitions
<li> contained entities
</ul>
The indexes of the attributes are 0 to a-1, where a is the
number of attributes.  The indexes of the ports are a to a+p-1,
where p is the number of ports, and so on.
Subclasses may return a subset of the attributes, ports, and
relations by overriding the protected methods that list these
contained objects.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class FullTreeModel extends ClassAndEntityTreeModel {

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
        List attributes = _attributes(parent);
        int numAttributes = attributes.size();

        List ports = _ports(parent);
        int numPorts = ports.size();

        List relations = _relations(parent);
        int numRelations = relations.size();

        if (index >= numAttributes + numPorts + numRelations) {
            return super.getChild(parent,
                    index - numAttributes - numPorts - numRelations);
        } else if (index >= numAttributes + numPorts) {
            return relations.get(index - numAttributes - numPorts);
        } else if (index >= numAttributes) {
            return ports.get(index - numAttributes);
        } else if (index >= 0) {
            return attributes.get(index);
        } else {
            return null;
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
        List attributes = _attributes(parent);
        int numAttributes = attributes.size();

        List ports = _ports(parent);
        int numPorts = ports.size();

        List relations = _relations(parent);
        int numRelations = relations.size();

        return numAttributes + numPorts + numRelations
            + super.getChildCount(parent);
    }

    /** Return the index of the given child within the given parent.
     *  If the parent is not contained in the child, return -1.
     *  @return The index of the specified child.
     */
    public int getIndexOfChild(Object parent, Object child) {

        List attributes = _attributes(parent);

        int index = attributes.indexOf(child);
        if (index >= 0) {
            return index;
        } else {
            // Object is not an attribute.  See whether it's a port.
            List ports = _ports(parent);

            index = ports.indexOf(child);
            int numAttributes = attributes.size();
            if (index >= 0) {
                return index + numAttributes;
            } else {
                // Not an attribute or port. Try relation.
                List relations = _relations(parent);

                index = relations.indexOf(child);
                int numPorts = ports.size();
                if (index >= 0) {
                    return index + numAttributes + numPorts;
                } else {
                    // Not an attribute, port, or relation. Defer to base
                    // class.
                    index = super.getIndexOfChild(parent, child);
                    if (index >= 0) {
                        int numRelations = relations.size();
                        return index + numAttributes + numPorts + numRelations;
                    }
                }
            }
        }
        return -1;
    }

    /** Return true if the object is a leaf node.  An object is a leaf
     *  node if it has no children that are instances of one of the classes
     *  specified by setFilter(), if a filter has been specified.
     *  @return True if the node has no children.
     */
    public boolean isLeaf(Object object) {
        // FIXME: Ignoring setFilter for now.

        if (_attributes(object).size() > 0) return false;
        if (_ports(object).size() > 0) return false;
        if (_relations(object).size() > 0) return false;

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
        return ((NamedObj)object).attributeList();
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
        return ((Entity)object).portList();
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
        return ((CompositeEntity)object).relationList();
    }
}
