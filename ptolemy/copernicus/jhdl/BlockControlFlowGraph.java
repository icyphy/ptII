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

package ptolemy.copernicus.jhdl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;

import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

//////////////////////////////////////////////////////////////////////////
//// BlockControlFlowGraph
/**
 * This class will take a Soot block and create a DirectedGraph.
 *

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class BlockControlFlowGraph extends DirectedGraph {

    BlockControlFlowGraph(BriefBlockGraph bbgraph) {
	super();
	_bbgraph = bbgraph;
	_createGraph();
    }

    /**
     * create the topology of the graph.
     **/
    protected void _createGraph() {
	List blockList=_bbgraph.getBlocks();

	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    addNodeWeight(block);
	}

	//Connect the graph so it has the same structure as bbgraph
	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    Node nb = node(block);
	    //Get successors to this block and add an edge to graph for each one
	    for (Iterator succs=block.getSuccs().iterator(); succs.hasNext();){
		Block succ=(Block)succs.next();
		addEdge(nb,node(succ));
	    }
	}
    }

    protected Map _blockMap;

    protected BriefBlockGraph _bbgraph;

}
