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

package ptolemy.copernicus.jhdl.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.copernicus.jhdl.RequiredBlockDataFlowGraph;

import soot.Body;
import soot.SootMethod;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

//////////////////////////////////////////////////////////////////////////
//// MergedControlFlowGraph
/**
 * This class will take a Soot block and create a DirectedGraph.
 *

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

public class MergedControlFlowGraph extends DirectedGraph {

    public MergedControlFlowGraph(SootMethod method) throws IllegalActionException {
        this(method.retrieveActiveBody());
    }

    public MergedControlFlowGraph(Body body) throws IllegalActionException {
        super();

        _bbgraph = new BriefBlockGraph(body);
        _createGraph();
        _controlFlowAnalysis();
    }

//      public DirectedGraph createDataFlowGraph() {
//          // 1. Create dataflow graph of each basic block
//          // 2. Determine hierarchical super block boundaries
//          // 3. Determine signal multiplexing
//          // 4. Combine dataflow graphs

//          // Create dataflow of each basic block
//          List blockList = _bbgraph.getBlocks();
//          for (int blockNum=0;blockNum<blockList.size();blockNum++) {
//              Block block=(Block)blockList.get(blockNum);
//              BlockDataFlowGraph dataFlowGraph=null;
//              try {
//                  dataFlowGraph = new BlockDataFlowGraph(block);
//              } catch(IllegalActionException e) {
//                  System.err.println(e);
//              }
//          }
//          return null;
//      }

    /**
     * create the topology of the graph.
     **/
    protected void _createGraph() throws IllegalActionException {
        Map blockToSuperBlockMap = new HashMap();
        List blockList=_bbgraph.getBlocks();

        // Create nodes
        for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
            Block block=(Block)blocks.next();
            RequiredBlockDataFlowGraph dfg = new RequiredBlockDataFlowGraph(block);
            SuperBlock sb = new SuperBlock(block,dfg);
            blockToSuperBlockMap.put(block, sb);

            //addNodeWeight(block);
            //addNodeWeight(dfg);
            addNodeWeight(sb);
        }

        // Create edges
        for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
            Block block=(Block)blocks.next();
            //Node nb = node(block);
            Node nb = node(blockToSuperBlockMap.get(block));

            //Get successors to this block and add an edge
            //to graph for each one
            for (Iterator succs=block.getSuccs().iterator();
                 succs.hasNext();){
                Block succ=(Block)succs.next();
                Node sb = node(blockToSuperBlockMap.get(succ));
                addEdge(nb,sb);
            }
        }
         if (!isAcyclic()) {
             System.err.println(_toDotty.convert(this, "this"));
            System.out.println("Warning! Graph has feedback");
            //    throw new IllegalActionException("Feedback currently not supported");
         }
    }

    protected void _controlFlowAnalysis() throws IllegalActionException {
        //Get a topological sort
        Map nodeToLabel = new HashMap();
        SuperBlock sorted[]=null;

        Object []temp=topologicalSort(nodes()).toArray();
        sorted=new SuperBlock[temp.length];
        for (int i=0; i < temp.length; i++){
            sorted[i]=(SuperBlock)((Node)temp[i]).getWeight();
        }

        for (int i=0; i < sorted.length; i++){
            sorted[i].combineLabels(this);
            for (Iterator succs = successors(node(sorted[i])).iterator();
                 succs.hasNext();){
                Node succ = (Node)succs.next();

                sorted[i].propagateLabelsTo((SuperBlock)succ.getWeight());
            }
        }

    }

    public static void main(String args[]) {
        String classname =
            ptolemy.copernicus.jhdl.test.Test.DEFAULT_TESTCLASS;
        String methodname =
            ptolemy.copernicus.jhdl.test.Test.DEFAULT_TESTMETHOD;
        if (args.length > 0)
            classname = args[0];
        if (args.length > 1)
            methodname = args[1];

        soot.SootClass testClass =
            ptolemy.copernicus.jhdl.test.Test.getApplicationClass(classname);
        if (testClass == null) {
            System.err.println("Class "+classname+" not found");
            System.exit(1);
        }
        System.out.println("Loading class "+classname+" method "+methodname);
        if (!testClass.declaresMethodByName(methodname)) {
            System.err.println("Method "+methodname+" not found");
            System.exit(1);
        }
        soot.SootMethod testMethod = testClass.getMethodByName(methodname);
        soot.Body body = testMethod.retrieveActiveBody();

        BriefBlockGraph bbgraph = new BriefBlockGraph(body);
        _toDotty.writeDotFile(".", "bbgraph", bbgraph);
        try {
            MergedControlFlowGraph bcfg = new MergedControlFlowGraph(body);
        } catch (IllegalActionException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    private static ptolemy.copernicus.jhdl.util.BlockGraphToDotty _toDotty =
    new ptolemy.copernicus.jhdl.util.BlockGraphToDotty();
    protected BriefBlockGraph _bbgraph;
}
