/*
The interface that all nodes in an abstract syntax tree implement.

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


package ptolemy.lang;


import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ITreeNode
/**
 *  The interface that all nodes in an abstract syntax tree implement.
 *
 *  @author Jeff Tsay
 */
public interface ITreeNode extends Cloneable {

    /** Accept a visitor, giving the visitor a list of zero arguments. */
    public Object accept(IVisitor v);

    /** Accept a visitor, giving the visitor a list of arguments. Depending on
     *  the traversal method, the children of the node may be visited before or
     *  after this node is visited, or not at all.
     */
    public Object accept(IVisitor v, LinkedList visitorArgs);
    
    /** Return the list of all direct children of this node. */
    public ArrayList children(); 

    /** Syntactic sugar to get the return value of the most recent
      * visitor to the i-th child node, where the argument is i.
      */
    public Object childReturnValueAt(int index);

    /** Syntactic sugar to get the return value of the most recent
      * visitor to the specified child node.
      */
    public Object childReturnValueFor(Object child);

    /** Return the class ID number (the node ID), which is unique for
     *  each sub-type.
     */    
    public int classID();

    /** Return a clone of this node, cloning all children of the node. */
    public Object clone();

    /** Return the child at the specified index in the child list. */
    public Object getChild(int index);

    public boolean isSingleton();

    /** Set the child at the specified index in the child list. */
    public void setChild(int index, Object child);

    /** Visit all nodes or lists in in the argument list, and place the list of 
     *  return values in the CHILD_RETURN_VALUES_KEY property of the node.
     */
    public void traverseChildren(IVisitor v, LinkedList args);
    
    public void setChildren(ArrayList childList);

    /** Return a String representation of this node.
     *  Call the toString() method of all child nodes.
     */  
    public String toString();

    /** Return a String representation of this node, indented by ident. 
     *  Call the toString() method of all child nodes.
     */
    public String toString(String indent);    
}
