/*
@Copyright (c) 1998-2000 The Regents of the University of California.
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
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import ptolemy.domains.sdf.kernel.*;
import java.lang.Math;


//////////////////////////////////////////////////////////////////////////
//// HuffTree --- A tree class derived from BinaryTree class
//                containing methods that
//                will be used to construct Huffman Tree in Huffman coding.
/**
@author Michael Leung
@version $Id$
*/

public class HuffTree extends ptolemy.domains.sdf.lib.huffman.BinaryTree {

    /* Constructor for HuffTree*/
    public HuffTree() {
        super();
    }


    /* This method get the probability of a HuffTree Node
     * Probability of a HuffTree is equal to sum of the probability
     * of its branches.
     */

    public double getProb() {
        if (isLeaf())
            return _prob;
        else {
            if (getLeft() != null) {
                _prob = ((HuffTree) getLeft()).getProb();
            }
            if (getRight() != null) {
                _prob = _prob + ((HuffTree) getRight()).getProb();
            }
        }
        return _prob;
    }

    /* This method gets the depth of this HuffTree.*/
    public int getDepth() {
        int depth;
        if (isLeaf())
            return 0;
        depth = _max(((HuffTree) getLeft()).getDepth(),
                ((HuffTree) getRight()).getDepth()) + 1;
        return depth;
    }

    /* Override the addLeft method of BinaryTree.
     * Throw IllegalActionException when the child has a parent already.
     * Throw IllegalAcitonException when adding other tree type to the
     *       left of the HuffTree.
     */

    public void addLeft(BinaryTree child)
            throws IllegalActionException {
        if (!(child instanceof HuffTree)) {
            throw new IllegalActionException("HuffTree: "+
                    "Cannot add other tree type to the left "+
                    "of this huffman tree.");
        }
        if (child.getParent() != null)
            throw new IllegalActionException("HuffTree: " +
                    "Cannot add the tree " + child +
                    " to the left branch because " + child +
                    " has a parent already.");
        super.addLeft(child);
    }

    /* Override the addRight method of BinaryTree.
     * Throw IllegalActionException when the child has a parent already.
     * Throw IllegalAcitonException when adding other tree type to the
     * left of the HuffTree.
     */

    public void addRight(BinaryTree child)
            throws IllegalActionException {
        if (!(child instanceof HuffTree))
            throw new IllegalActionException("HuffTree: "+
                    "Cannot add other tree type to the right "+
                    "of this huffman tree.");
        if (child.getParent() != null)
            throw new IllegalActionException("HuffTree: " +
                    "Cannot add the tree " + child +
                    " to the right branch because " + child +
                    " has a parent already.");
        super.addRight(child);
    }

    /*Override the toString of Object class for debugging and testing
     *purposes.
     *This method will return the string representation of a HuffTree node.
     */

    public String toString() {
        String str = "tree:";
        str = str + getProb();
        return str;
    }


    /* Method that will print the tree in order*/
    public String printPreOrder() {
        String treeString="";
        treeString += this.toString();

        if (getLeft() != null) {
            treeString += "(";
            treeString += ((HuffTree) getLeft()).printPreOrder();
        } else {
            treeString += "(";
            treeString += "null";
        }

        treeString += ",";

        if (getRight() != null) {
            treeString += ((HuffTree) getRight()).printPreOrder();
            treeString += ")";
        } else {
            treeString += "null";
            treeString += ")";
        }
        return treeString;
    }

    // Helper functions:

    /* This method modified the HashMap with all the
     * HuffLeafs in this HuffTree.
     * Key for HashMap will be the data of HuffLeaf.
     * Value for a specific key will be the HuffLeaf position.
     */
    public void fill(HashMap map) {
        if (this == null)
            return;
        else if (this.isLeaf())
            map.put(((HuffLeaf) this ).getData(), this );
        else {
            ((HuffTree) this.getLeft()).fill(map);
            ((HuffTree) this.getRight()).fill(map);
        }
    }

    /* This method find and return the max of two int.
     */

    private int _max(int a, int b) {
        int temp = 0;
        if (a >= b)
            temp = a;
        else
            temp = b;
        return temp;
    }

    private double _prob;
}



