/* 

 Copyright (c) 2001-2002 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.soot;

import java.util.*;

import ptolemy.graph.Node;

import soot.*;
import soot.jimple.*;
import soot.util.*;
import soot.jimple.internal.*;

public class BinaryMux implements Value {

    public BinaryMux(Node t, Node f, Node c, String name) {
	_trueNode = t;
	_falseNode = f;
	_conditionNode = c;
	_name = name;
    }

    public Node getConditionNode() { return _conditionNode; }
    public Node getTrueNode() { return _trueNode; }
    public Node getFalseNode() { return _falseNode; }

    public String toString() { 
	return "mux_"+_name; 
    }

    // Hack. Need to come up with a better way to represent Values
    public boolean equivTo(Object o) { return false; }
    public int equivHashCode() { return 0; }
    public void convertToBaf(JimpleToBafContext context, List l) {}
    public void apply(Switch sw) {}
    public List getUseBoxes() { return null; }
    public Type getType() {return null;}
    public Object clone() { return null;}

    String _name;
    Node _trueNode;
    Node _falseNode;
    Node _conditionNode;
}
