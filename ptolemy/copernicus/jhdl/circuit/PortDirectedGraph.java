/*

 Copyright (c) 2001-2003 The Regents of the University of California.
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

package ptolemy.copernicus.jhdl.circuit;

import java.util.*;

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.copernicus.jhdl.soot.*;

import ptolemy.actor.*;
import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import soot.jimple.*;
import soot.*;

//////////////////////////////////////////////////////////////////////////
//// 
/**
 * This class represents a DirectedGraph that has distinct Nodes
 * that are considered "ports". The class also provides a method
 * for trimming nodes that are not reachable from input or outputs.

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class PortDirectedGraph extends DirectedGraph {
    
    public PortDirectedGraph() {
	_inputPortNodes = new Vector();
	_outputPortNodes = new Vector();
    }

    public Node addInputPortNode(Signal weight) {
	Node n = addNodeWeight(weight);
	_inputPortNodes.add(n);
	return n;
    }

    public Node addOutputPortNode(Signal weight) {
	Node n = addNodeWeight(weight);
	_outputPortNodes.add(n);
	return n;
    }

    // TODO: determine if there is a path between each input port and
    // the outputs. If there is no path, remove the input nodes.
    public void removeUnreachable() {

	// Determine all nodes that are considered "accetable" input Nodes
	//  - Input ports
	//  - Constants
	Collection forwardreachable = new HashSet();
	forwardreachable.addAll(getInputPortNodes());
	for (Iterator i = nodes().iterator(); i.hasNext();) {
	    Node n = (Node) i.next();
	    if (n.getWeight() instanceof Constant)
		forwardreachable.add(n);
	}
	// Determine all nodes reachable from inputs & constants
	for (Iterator i = getInputPortNodes().iterator();i.hasNext();) {
	    Node inport = (Node) i.next();
	    Collection reach = reachableNodes(inport);
	    forwardreachable.addAll(reach);
	}

	// Determine backward reachables
	Collection backwardreachable = new Vector();
	backwardreachable.addAll(getOutputPortNodes());
	for (Iterator i = getOutputPortNodes().iterator();i.hasNext();) {
	    Node output = (Node) i.next();
	    Collection reach = backwardReachableNodes(output);
	    backwardreachable.addAll(reach);
	}
	//System.out.println(forwardreachable + "n" + backwardreachable);
	Collection remove = new Vector();
	for (Iterator i = nodes().iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (!forwardreachable.contains(n) ||
		!backwardreachable.contains(n))
		remove.add(n);
	}
	for (Iterator i = remove.iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    removeNode(n);
	}
    }
    public Collection getInputPortNodes() { return _inputPortNodes; }
    public Collection getOutputPortNodes() { return _outputPortNodes; }

    protected Collection _inputPortNodes;
    protected Collection _outputPortNodes;

}
