/* Computes the required definitions of a SootBlockDirectedGraph

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

import soot.toolkits.graph.Block;
import soot.*;
import soot.jimple.*;

import ptolemy.graph.*;
import ptolemy.graph.analysis.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// 
/**
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class SootDFGRequiredDefinitionsAnalysis extends Analysis {

    public SootDFGRequiredDefinitionsAnalysis(SootBlockDirectedGraph graph) {
	super(graph);
	_sbdfGraph = graph;
    }

    protected Object _compute() {
	Vector requiredNodes = new Vector();
	Iterator nodes = _sbdfGraph.nodes().iterator();
	while (nodes.hasNext()) {
	    Node n = (Node) nodes.next();
	    Object nweight = n.getWeight();
	    if (nweight instanceof Local) {
		if (_sbdfGraph.predecessors(n).size() == 0)
		    requiredNodes.add(n);
	    } else if (nweight instanceof InstanceFieldRef) {		
		if (_sbdfGraph.predecessors(n).size() == 1)
		    requiredNodes.add(n);
	    }
	}
	return requiredNodes;
    }

    SootBlockDirectedGraph _sbdfGraph;
}
