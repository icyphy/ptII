/*
@Copyright (c) 1998-1999 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY

@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.domains.sdf.lib.huffman;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.util.NamedObj;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;



//////////////////////////////////////////////////////////////////////////
//// BinaryTree --- A class containing methods
//                  for creating and manipulating a binary tree.
/////
/**
@author Michael Leung
@version $Id$
*/


public class BinaryTree extends java.lang.Object {

    // Class constructor for BinaryTree.
    public void BinaryTree() {
        _parent = null;
        _left = null;
        _right = null;
    }

    // Returns the parent of this tree.
    public BinaryTree getParent() {
        return _parent;
    }

    // Returns the left branch of this tree.
    public BinaryTree getLeft() {
        return _left;
    }

    // Returns the right branch of this tree.
    public BinaryTree getRight() {
        return _right;
    }

    // Returns the root of this tree.
    public BinaryTree getRoot() {
        BinaryTree temp;
        temp = this;
        while (temp._parent !=null) {
            temp = this._parent;
        }
        return temp;
    }

    /* Add an new left child.
     * Throw an IllegalActionException if want to add a child to the left and
     * the child already has a parent.
     */

    public void addLeft(BinaryTree child)
            throws IllegalActionException {
        if (child._parent != null)
            throw new IllegalActionException("BinaryTree: " +
                    "Cannot add the tree " + child +
                    " to the left branch because " + child +
                    " has a parent already.");
        _left = child;
        child._parent = this;
    }

    /* Add an new right child.
     * Throw an IllegalActionException if want to add an children to the right
     * and the child already has a parent.
     */

    public void addRight(BinaryTree child)
            throws IllegalActionException {
        if (child._parent != null)
                throw new IllegalActionException("BinaryTree: " +
                        "Cannot add the tree " + child +
                        " to the right branch because " + child +
                        " has a parent already.");
            _right = child;
            child._parent = this;
    }

    /* Set the parent of this obj to be myParent.
     * Throw an IllegalActionException if we set the parent of this
     * when this already has a parent.
     */
    // FIXME: need a better documentation and do what the real function needs.

    public void setParent(BinaryTree myParent)
            throws IllegalActionException {
        if ((myParent.getLeft() != null) && (myParent.getRight() != null)) {
            throw new IllegalActionException("BinaryTree: "+
                    "Cannot set the parent of this because the parent has "+
                    "two children already.");
        }
        _parent = myParent;
    }

    // Remove left branch.
    public void removeLeft() {
        _left._parent = null;
        _left = null;
    }

    // Remove right branch.
    public void removeRight() {
        _right._parent = null;
        _right = null;
    }

    // Check if it is the end of tree nodes.
    public boolean isLeaf() {
        boolean boo;
        boo = ((_left == null) && (_right == null));
        return boo;
    }

    /* Check if it is the root of tree by checking if the parent
     * is null or not.
     */
    public boolean isRoot() {
        boolean boo;
        boo = (_parent == null);
        return boo;
    }

    /**
     * Merging two BinaryTree to form a new tree.
     * First argument will be the left branch, and second argument will
     * be the right branch.
     * Throws an IllegalActionException if either right tree or left tree
     * has a parent already.
     */

    public void merge (BinaryTree leftTree, BinaryTree rightTree)
        throws IllegalActionException {
        if ((leftTree._parent != null) || (rightTree._parent != null)) {
            throw new IllegalActionException("BinaryTree: "+
                    "Cannot megre because either left or right" +
                    "has a parent already.");
        }
        addLeft(leftTree);
        addRight(rightTree);
    }

    /* Override toString method for the java.lang.Object class. The overrided
     * version should recursively return the name of the node
     * of a tree class and will return null either their left or right is
     * null.
     *
     * The return sequence should be :
     * first return itself;
     * then return the left branch;
     * then return the right branch.
     */

    public String toString() {
        String treeString="";
        treeString += "tree";

        if (getLeft() != null) {
            treeString += "(";
            treeString += _left.toString();
        } else {
            treeString += "(";
            treeString += "null";
        }

        treeString += ",";

        if (getRight() != null) {
            treeString += _right.toString();
            treeString += ")";
        } else {
            treeString += "null";
            treeString += ")";
        }
        return treeString;
    }



    private BinaryTree _parent;
    private BinaryTree _left;
    private BinaryTree _right;

}







