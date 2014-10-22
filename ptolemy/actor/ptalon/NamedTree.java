/* This class is used for generic trees, where each node has a name.

 Copyright (c) 2006-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

package ptolemy.actor.ptalon;

import java.util.LinkedList;
import java.util.List;

/**
 This class is used for generic trees, where each node
 has a name. Subclasses will typically should set TreeType to
 be the subclass itself.

 For instance, IfTree extends NamedTree &lt IfTree &gt
 *
 <p>
 @author Adam Cataldo, Elaine Cheong
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public abstract class NamedTree<TreeType extends NamedTree> {

    /** Create a new tree with the specified parent.  This
     *  is null if the tree to create is a root.
     *  @param parent The parent for this tree.
     *  @param name The name for this tree.
     */
    public NamedTree(TreeType parent, String name) {
        _name = name;
        _parent = parent;
        _children = new LinkedList<TreeType>();
    }

    /** Create a new child tree to this tree with the specified
     *  name and return it.
     *  @param name The name of the child.
     *  @return The child TreeType.
     */
    public abstract TreeType addChild(String name);

    /** Returns the child with the specified name.
     *  @param name The name for the desired child.
     *  @return A child with the specified name, if there
     *  is any, or null otherwise.
     */
    public TreeType getChild(String name) {
        for (TreeType child : _children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    /** Returns the children of this tree.
     *  @return The children of this tree.
     */
    public List<TreeType> getChildren() {
        return _children;
    }

    /** Returns the name associated with this tree.
     *  @return The name associated with this tree.
     */
    public String getName() {
        return _name;
    }

    /** Returns the parent of this tree.
     *  @return The parent of this tree.
     */
    public TreeType getParent() {
        return _parent;
    }

    /** Return the ancestors of this tree, not including this tree.
     *  @return The ancestors of this tree, not including
     *  this tree.
     */
    public List<TreeType> getProperAncestors() {
        LinkedList<TreeType> list = new LinkedList<TreeType>();
        TreeType next = _parent;
        while (next != null) {
            list.addFirst(next);
            next = (TreeType) next.getParent();
        }
        return list;
    }

    /** The parent of this tree.
     */
    protected TreeType _parent;

    /** The children of this tree.
     */
    protected List<TreeType> _children;

    /** The name of this tree.
     */
    protected String _name;
}
