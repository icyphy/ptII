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

import ptolemy.copernicus.jhdl.util.*;

import ptolemy.graph.*;
import soot.toolkits.graph.Block;
import soot.*;
import soot.jimple.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// 
/**
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class SootBlockDirectedGraph extends DirectedGraph {
    
    public SootBlockDirectedGraph(Block block) {
	super();
	_block = block;
	_locals = new HashListMap();
    }

    public Node getValueNode(Value v) {
	if (_locals.containsKey(v))
	    return (Node) _locals.getLast(v);
	return null;
    }

    public Node getOrAddValueNode(Value v) {
	Node n = getValueNode(v);
	if (n == null)
	    return addValueNode(v);
	else
	    return n;
    }

    public Node addValueNode(Value v) {
	Node n = addNodeWeight(v);
	_locals.add(v,n);
	return n;
    }

    // origValue will map to the list associated with newValue
    public void replaceValueNode(Value origValue, Value newValue) {
	List l = _locals.getList(newValue);
	_locals.setList(origValue,l);
	//return getValueNode(newValue);
    }

    public static boolean equal(InstanceFieldRef ifr1, InstanceFieldRef ifr2) {
	return ifr1.getBase().equals(ifr2.getBase()) &&
	    ifr1.getField().equals(ifr2.getField());
    }

    /*
      - Determine which references are the same (equivelance class)
      - 
     */
    public void mergeInstanceFieldRefs() {

	// Iterate through all units
	

	// 1. Get all IFRs
	List ifrNodes = new Vector();
	for (Iterator i = nodes().iterator(); i.hasNext();){
	    Node n = (Node) i.next();
  	    if (n.getWeight() instanceof InstanceFieldRef) {
		ifrNodes.add(n);
	    }
	}

	// 2. Search through ifrValues and determine which ones 
	//    are the same.
	List ifrConfirmed = new Vector();
	List ifrRemoved = new Vector();
	for (Iterator i = ifrNodes.iterator();i.hasNext();) {
	    Node i_node = (Node) i.next();
	    InstanceFieldRef i_ifr = (InstanceFieldRef) i_node.getWeight();
	    for (Iterator j = ifrConfirmed.iterator();j.hasNext();) {
		Node j_node = (Node) j.next();
		InstanceFieldRef j_ifr = (InstanceFieldRef) j_node.getWeight();
		//System.out.println("Comparing "+i_node+" with "+j_node);

		if (j_ifr.getBase().equals(i_ifr.getBase()) &&
		    j_ifr.getField().equals(i_ifr.getField())) {
		    // Found a match. i_ifr = j_ifr.
		    //System.out.println("Same!");

		    Vector removeEdges = new Vector();
		    // Update Input Edges
		    for (Iterator i_input_edges=inputEdges(i_node).iterator();
			 i_input_edges.hasNext();) {
			Edge i_in_edge = (Edge) i_input_edges.next();
			removeEdges.add(i_in_edge);
			if (i_in_edge.hasWeight() && 
			    i_in_edge.getWeight().equals(BASE_WEIGHT)) {
			    // delete edge
			    //removeEdge(i_in_edge);
			} else {
			    // add new edge from source to j_node
			    addEdge(i_in_edge.source(),j_node);
			    //removeEdge(i_in_edge);
			}
		    }

		    // Update Output Edges
		    for (Iterator i_output_edges=outputEdges(i_node).iterator();
			 i_output_edges.hasNext();) {
			Edge i_out_edge = (Edge) i_output_edges.next();
			// add new edge from source to j_node
			addEdge(j_node,i_out_edge.sink());
			//removeEdge(i_out_edge);
			removeEdges.add(i_out_edge);
		    }
		    
		    for (Iterator r = removeEdges.iterator(); r.hasNext();)
			removeEdge((Edge) r.next());
		    // Add to ifrRemoved
		    ifrRemoved.add(i_node);
		} 
	    }
	    // update confirmed
	    if (!ifrRemoved.contains(i_node))
		ifrConfirmed.add(i_node);
	}
	for (Iterator r = ifrRemoved.iterator(); r.hasNext();)
	    removeNode((Node) r.next());
    }

    public static String BASE_WEIGHT = "base";
    protected HashListMap _locals;
    protected Block _block;

    public static void main(String args[]) {
	SootBlockDirectedGraph graphs[] = 
	    ControlSootDFGBuilder.getGraphs(args);
	for (int i = 0;i<graphs.length;i++) {
	    PtDirectedGraphToDotty.writeDotFile("bgraph"+i,graphs[i]);
	    graphs[i].mergeInstanceFieldRefs();
	    PtDirectedGraphToDotty.writeDotFile("mgraph"+i,graphs[i]);
	}
    }

}

