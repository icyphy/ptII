/* An interface that nodes in an abstract syntax tree implement.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

// FIXME: Seems that only TreeNode implements this.  Eliminate?

package ptolemy.lang;


import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ITreeNode
/**
An interface that nodes in an abstract syntax tree implement.

@author Jeff Tsay
@version $Id$
 */
public interface ITreeNode extends Cloneable {

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    /** Accept a visitor with no arguments. An implementation
     *  of this manages visits to the child nodes.
     *  Depending on the traversal method, the children of the node
     *  may be visited before or after this node is visited, or not at all.
     *  @param visitor The visitor to accept.
     *  @return The result of the visit.
     */
    public Object accept(IVisitor visitor);

    /** Accept a visitor with the specified arguments. An implementation
     *  of this manages visits to the child nodes.
     *  Depending on the traversal method, the children of the node
     *  may be visited before or after this node is visited, or not at all.
     *  @param visitor The visitor to accept.
     *  @param visitorArgs The arguments of the visit.
     *  @return The result of the visit.
     */
    public Object accept(IVisitor visitor, LinkedList visitorArgs);

    /** Return the list of children of this node.
     *  @return A list of children nodes.
     */
    public List children();

    /** Get the return value of the most recent
     *  visitor to the <i>index</i>-th child node.
     *  @param index The index of the child whose visit result is returned.
     *  @return The most recent result of visiting the specified child.
     */
    public Object childReturnValueAt(int index);

    /** Get the return value of the most recent
     *  visitor to the specified child node.
     *  @param child The child whose visit result is returned.
     *  @return The most recent result of visiting the specified child.
     */
    public Object childReturnValueFor(Object child);

    /** Return the class ID number, which is unique for each sub-type.
     *  @return A unique class ID number.
     */
    public int classID();

    /** Return a deep clone of this node, which contains clones of the
     *  children of the node.  If the node identifies itself as a singleton
     *  by returning true in its isSingleton() method, then do not clone it.
     *  @return A deep copy of this node.
     */
    public Object clone();

    /** Return the child at the specified index in the child list.
     *  If there is no such child, return null.
     *  @param index The index of the desired child.
     *  @return The child node, or null if there is none.
     */
    public Object getChild(int index);

    /** Return true if the class of this object is a singleton, i.e. there
     *  exists only one object of the subclass. This method needs to be
     *  overridden by  singleton classes.
     *  @return False.
     */
    public boolean isSingleton();

    /** Set the child at the specified index to the specified object,
     *  replacing any previous child at that index.
     *  @param index The index of the child.
     *  @param child The child to insert.
     */
    public void setChild(int index, Object child);

    /** Set the children of this node to the specified list.
     *  @param childList The list of children.
     */
    public void setChildren(ArrayList childList);

    /** Return a String representation of this node, prefixed by prefix,
     *  and all its children.
     *  @return A representation of this node.
     */
    public String toString(String indent);

    /** Visit all nodes or lists in in the argument list, and place
     *  the list of returned values in the CHILD_RETURN_VALUES_KEY
     *  property of the node.
     *  @param visitor The visitor to apply to the children.
     *  @param args The arguments to pass to the visitor.
     */
    public void traverseChildren(IVisitor visitor, LinkedList args);
}
