/* A directed acyclic graph representing the control flow of a method.

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

package ptolemy.copernicus.jhdl.soot;

import ptolemy.copernicus.jhdl.*;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ptolemy.graph.Edge;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

import soot.Body;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.AssignStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.SootMethod;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

import ptolemy.copernicus.jhdl.util.BlockGraphToDotty;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;

//////////////////////////////////////////////////////////////////////////
//// DirectedAcyclicCFG
/**
 * This class will take a Soot Body and create a DirectedAcyclicGraph
 * of the corresponding control-flow graph. The Nodes of this graph
 * are all weighted with a Soot Block.
 *
 * This class will also keep track of the single source Node (i.e. the
 * entry Block of the Soot Body) and will identify the exit sink
 * Node. A single sink Node will be created if there are multiple
 * exit points for this CFG.
 *
 *
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class DirectedAcyclicCFG extends DirectedGraph {

    DirectedAcyclicCFG(SootMethod method) throws IllegalActionException {
        this(method.retrieveActiveBody());
    }

    DirectedAcyclicCFG(Body body) throws IllegalActionException {
        this(new BriefBlockGraph(body));
    }

    DirectedAcyclicCFG(BriefBlockGraph bbg) throws IllegalActionException {
        super();
        _createGraph(bbg);
    }

    /**
     * Return the source Node of the CFG.
     **/
    public Node source() {
        return _source;
    }

    /**
     * Return the sink Node of the CFG.
     **/
    public Node sink() {
        return _sink;
    }

    /**
     * create the topology of the graph.
     **/
    protected void _createGraph(BriefBlockGraph bbg)
            throws IllegalActionException {

        // Save Body
        _bbgraph = bbg;

        // create copy of graph
        List blockList=_bbgraph.getBlocks();

        // Add one Node for each Block in the graph. The
        // weight of the Node is the corresponding Block.
        for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
            Block block=(Block)blocks.next();
            addNodeWeight(block);
        }

        // Copy edges. Iterate through each Node in the graph and
        // copy edges to its successors.
        //
        for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
            Block block=(Block)blocks.next();
            Node nb = node(block);

            List succs = block.getSuccs();
            //Get successors to this block and add an edge to graph for
            //each one.
            for (Iterator successors=succs.iterator(); successors.hasNext();){
                Block succ=(Block)successors.next();
                addEdge(nb,node(succ));
            }
        }

        // Identify single source and single sink
        Collection sources = sourceNodes();
        if (sources.size() == 0)
            throw new IllegalActionException("There is no source Node");
        if (sources.size() > 1)
            throw new IllegalActionException("There are more than one source nodes");
        _source = (Node) sources.iterator().next();

        // Identify sink
        Collection sinks = sinkNodes();
        if (sinks.size() == 0)
            throw new IllegalActionException("There are no sinks");
        if (sinks.size() == 1) {
            _sink = (Node) sinks.iterator().next();
        } else {
            _sink = addNodeWeight("sink");
            for (Iterator i=sinks.iterator();i.hasNext();) {
                Node n = (Node) i.next();
                addEdge(n,_sink);
            }
        }
    }

    public String nodeString(Node n) {
        if (n.hasWeight()) {
            Object o = n.getWeight();
            if (o instanceof Block) {
                return "B" + ((Block) o).getIndexInMethod();
            } else if (o instanceof String) {
                return (String) o;
            }
            return o.getClass().getName() + " " +o.toString();
        }
        return "";
    }

    public static DirectedAcyclicCFG _main(String args[]) {
        soot.SootMethod testMethod =
            ptolemy.copernicus.jhdl.test.Test.getSootMethod(args);
        DirectedAcyclicCFG _cfg=null;
        try {
            ConditionalControlCompactor.compact(testMethod);
            soot.Body body = testMethod.retrieveActiveBody();
            BriefBlockGraph bbgraph = new BriefBlockGraph(body);
            BlockGraphToDotty toDotty = new BlockGraphToDotty();
            //toDotty.writeDotFile(".", "bbgraph", bbgraph);
            _cfg = new DirectedAcyclicCFG(bbgraph);
            PtDirectedGraphToDotty dgToDotty =
                new PtDirectedGraphToDotty();
            dgToDotty.writeDotFile(".", testMethod.getName(), _cfg);
        } catch (IllegalActionException e) {
            System.err.println(e);
            System.exit(1);
        }
        return _cfg;
    }

    public static void main(String args[]) {
        _main(args);
    }

    /**
     * The BriefBlockGraph that was used as the template for this Graph.
     **/
    protected BriefBlockGraph _bbgraph;

    /**
     * The single entry Node of the graph.
     **/
    protected Node _source;

    /**
     * The single exit Node of the graph.
     **/
    protected Node _sink;

}
