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
import ptolemy.domains.sdf.kernel.*;
import java.lang.Math;


//////////////////////////////////////////////////////////////////////////
//// HuffLeaf --- A tree class derived from HuffTree class which represents
//                the leaf class of huffman tree.
/**
   @author Michael Leung
   @version $Id$
*/

public class HuffLeaf extends ptolemy.domains.sdf.lib.huffman.HuffTree {

    // Constructor for Huffman leaf.
    public HuffLeaf(String d, double p) {
        super();
        _prob = p;
        _data = d;
    }

    // Returns the StringValue of the data contains in this Huffman leaf.
    public String getData() {
        return _data;
    }

    // Returns the probability contains in this Huffman leaf.
    public double getProb() {
        return _prob;
    }

    // Set the value of probability
    public void setProb(double p) {
        _prob = p;
    }

    // set the data
    public void setData(String d) {
        _data = d;
    }

    /*Override the toString of Object class for debugging and testing
     *purposes.
     *This method will return the string representation of a HuffLeaf.
     */

    public String toString() {
        String str = "Leaf:";
        str = str + getProb() + " " + "data:" + getData();
        return str;
    }

    private double _prob;
    private String _data;
}
