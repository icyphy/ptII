/* A transformer that removes unnecessary fields from classes.

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

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.MethodCallGraph;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
import soot.jimple.toolkits.invoke.VTATypeGraph;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;

import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.BaseType;

import ptolemy.data.expr.Variable;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.graph.Edge;
import ptolemy.copernicus.kernel.*;
import ptolemy.copernicus.java.*;
import ptolemy.copernicus.jhdl.util.GraphToDotty;
import ptolemy.copernicus.jhdl.util.SuperBlock;
import ptolemy.copernicus.jhdl.util.GraphNode;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.copernicus.jhdl.util.SynthesisToDotty;
import ptolemy.copernicus.jhdl.util.BlockGraphToDotty;
import ptolemy.copernicus.jhdl.util.MergedControlFlowGraph;

//////////////////////////////////////////////////////////////////////////
//// CircuitAnalysis
/**

@author Steve Neuendorffer and Ben Warlick
@version $Id$
@since Ptolemy II 2.0
*/
public class CircuitAnalysis {
    /** Construct a new analysis
     */
    public CircuitAnalysis(Entity entity, SootClass theClass) 
    throws IllegalActionException {
	DirectedGraph graph = new DirectedGraph();
        _graph = graph;
       
        System.out.println("className = " + entity.getClass().getName());

	//Handle cases that have been predefined and don't need analyzing
	//  	if (_pd.isDefined(entity)){
	//  	  try {
	//  	    _pd.convertEntityToGraph(entity, graph);
	//  	  } catch (IllegalActionException e){
	//  	    System.out.println("Error in CircuitAnalysis: "+e);
	//  	  }
	//  	  return;
	//  	}
	
        // Analyze the bodies of the appropriate methods for things that
        // are not sample delays.
        _requiredNodeMap = new HashMap();

	DirectedGraph prefire_graph=null;
	DirectedGraph fire_graph=null;
	DirectedGraph postfire_graph=null;
	
        if(theClass.declaresMethodByName("prefire")) {
            prefire_graph = 
		_analyzeMethod(theClass.getMethodByName("prefire"));
//              _analyze(prefire_graph, theClass.getMethodByName("prefire"));
        }
        if(theClass.declaresMethodByName("fire")) {
	    fire_graph = 
		_analyzeMethod(theClass.getMethodByName("fire"));
//              _analyze(fire_graph, theClass.getMethodByName("fire"));
        }
        if(theClass.declaresMethodByName("postfire")) {
	    postfire_graph = 
		_analyzeMethod(theClass.getMethodByName("postfire"));
//              _analyze(postfire_graph, theClass.getMethodByName("postfire"));
        }

//  	_appendGraph(graph, prefire_graph);
//  	_appendGraph(graph, fire_graph);
//  	_appendGraph(graph, postfire_graph);

        // get rid of non-essential nodes of 
//          boolean changed = true;
//          while(changed) {
//              changed = false;
//              for(Iterator nodes = graph.nodes().iterator();
//                  nodes.hasNext();) {
//                  Node node = (Node)nodes.next();
//                  if(requiredNodeSet.contains(node)) {
//                      continue;
//                  }
//                  HashSet set = new HashSet(graph.successors(node));
//                  set.retainAll(requiredNodeSet);
//                  if(set.isEmpty()) {
//                      continue;
//                  }
//                  requiredNodeSet.add(node);
//                  changed = true;
//              }
//          }

	SynthesisToDotty.writeDotFile(GraphToDotty.validFileName(entity.getName()),
				      fire_graph);

	/* This isn't ready yet.. this should act on graphs at each node,
	   not the top-level control flow graph
        // Go though and eliminate unnecessary nodes.  These are nodes
        // that are not the names of output ports and have no targets,
        // or locals
        Set removeSet = new HashSet();

	// find removable nodes and add new edges between removed
	// nodes predecessors and successors
        for(Iterator nodes = graph.nodes().iterator();
            nodes.hasNext();) {
            Node node = (Node)nodes.next();
            if(node.weight() instanceof Local ||
	       node.weight() instanceof SootField ||
	       !requiredNodeSet.contains(node)) {
                // Then remove the node.
                for(Iterator preds = graph.predecessors(node).iterator();
                    preds.hasNext();) {
                    Node pred = (Node)preds.next();
                    for(Iterator succs = graph.successors(node).iterator();
                        succs.hasNext();) {
                        Node succ = (Node)succs.next();
                        graph.addEdge(pred, succ);
                    }
                }
                removeSet.add(node);
            }
        }
       
        // Remove all the edges & nodes
        for(Iterator nodes = removeSet.iterator();
            nodes.hasNext();) {
            Node node = (Node)nodes.next();
            List predList = new LinkedList(graph.predecessors(node));
            for(Iterator preds = predList.iterator();
                preds.hasNext();) {
                Node pred = (Node)preds.next();
                graph.removeEdge((Edge)graph.successorEdges(pred, node).toArray()[0]);
            }
            List succList = new LinkedList(graph.successors(node));
            for(Iterator succs = succList.iterator();
                succs.hasNext();) {
                Node succ = (Node)succs.next();
                graph.removeEdge((Edge)graph.successorEdges(node, succ).toArray()[0]);
            }
            graph.removeNode(node);
        }
	*/
        //System.out.println("Filtered graph:\r\n" + graph + "\r\n");
    }

    public DirectedGraph getOperatorGraph() {
        return _graph;
    }

    public Inliner getInliner(){
	return new Inliner() {
		protected boolean shouldInline(SootMethod sootMethod){
		    String name=sootMethod.getName();

		    return false;
	  
		    //  	  //Don't inline get() or send() methods on ports.  They are
		    //  	  //handled separately
		    //  	  //if (name.equals("get") || name.equals("send")){
		    //  	    //FIXME - this code should check to make sure this
		    //  	    //method is declared by a Port class
		    //  	    if (isClass("ptolemy.kernel.Port", sootMethod.getDeclaringClass())){
		    //  	      return false;
		    //  	    }
		    //  	    //}
		    //  	  //Don't inline Token's methods, as they are mostly just
		    //  	  //arithmetic methods, such as add(), multiply(), etc.
		    //  	  if (isClass("ptolemy.data.Token", sootMethod.getDeclaringClass())){
		    //  	    return false;
		    //  	  }
		    //  	  return true;
		}

		protected boolean isClass(String name, SootClass sc){

		    if (name.equals(sc.getName()))
			return true;
	  
		    while (sc.hasSuperclass()){
			sc=sc.getSuperclass();
			if (name.equals(sc.getName()))
			    return true;
		    }
	  
		    return false;
		}
	    };
    }
  

    public static void main(String args[]) {
	TypedCompositeActor system = new TypedCompositeActor();
	try {
	  ptolemy.domains.sdf.kernel.SDFDirector sdfd = new
	    ptolemy.domains.sdf.kernel.SDFDirector(system,"director");	
	  ptolemy.copernicus.jhdl.demo.FIR2.FIR f = 
	    new ptolemy.copernicus.jhdl.demo.FIR2.FIR(system,"fir");
	  SootClass entityClass = 
	    Scene.v().loadClassAndSupport("ptolemy.copernicus.jhdl.demo.FIR2.FIR");
	  entityClass.setApplicationClass();
	  if (entityClass == null) {
	    System.err.println("Err - cannot find class");
	    System.exit(1);
	  } else
	    new CircuitAnalysis(f,entityClass);
	} catch (ptolemy.kernel.util.KernelException e) {
	  System.err.println(e);
	}
    }

    /**
     **/
    protected DirectedGraph _analyzeMethod(SootMethod method) 
	throws IllegalActionException {
        Body body = method.retrieveActiveBody();
  	MergedControlFlowGraph mcfg = new MergedControlFlowGraph(body);
	return mcfg;
    }

    /**
     * This is the main method of the class. This method will
     * take a SootMethod and created a new directed graph for
     * the method of interest.
     **/
    protected void _analyze(DirectedGraph graph, SootMethod method) {
        Body body = method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, 
							localDefs);

	// Inline methods? (Use Nathan's inliner?)
	Inliner inliner = getInliner();
	inliner.inline(body);
	
	BriefBlockGraph bbgraph=new BriefBlockGraph(body);
//  	System.out.println("//BlockGraph for Method:"+ method.getName()+"\n"+
//  			   BlockGraphToDotty.convert(bbgraph, "bbgraph"));
	List blockList=bbgraph.getBlocks();
	BlockDataFlowGraph dataFlowGraph=null;
	Map blockToSuperBlockMap = new HashMap();
	
	// Iterate over all basic blocks provided by Soot
	for (int blockNum=0; blockNum < blockList.size(); blockNum++){
	    Block block=(Block)blockList.get(blockNum);
	    //Set requiredNodeSet = new HashSet();
	    // Create a dataflow graph for each basic block
	    try {
		dataFlowGraph=new BlockDataFlowGraph(block);
	    } catch (IllegalActionException e) {
		System.err.println(e);
	    }
	    /*
  	    System.out.println("//Dataflow graph for block "+
  			       block.getIndexInMethod()+"\n"+
  			       PtDirectedGraphToDotty.convert(dataFlowGraph));
	    */

	    SuperBlock sb=new SuperBlock(block, dataFlowGraph);
	    blockToSuperBlockMap.put(block, sb);

	    for (Iterator i=dataFlowGraph.getRequiredNodeSet().iterator(); 
		 i.hasNext();){
		_requiredNodeMap.put(i.next(), sb);
	    }
	    
	} // for (int blockNum=0; blockNum < blockList.size(); blockNum++)

	//Add all dataflow graphs for each block to the main graph
	graph.addNodeWeights(blockToSuperBlockMap.values());

	//graph.addNodeWeights(blockList);

	//Connect the graph so it has the same structure as bbgraph
	for (Iterator blocks=blockList.iterator(); blocks.hasNext();){
	    Block block=(Block)blocks.next();
	    //Get successors to this block and add an edge to graph for each one
	    for (Iterator succs=block.getSuccs().iterator(); succs.hasNext();){
		Block succ=(Block)succs.next();
		graph.addEdge(blockToSuperBlockMap.get(block),
			      blockToSuperBlockMap.get(succ));
		//graph.addEdge(block, succ);
	    }
	}

	if (!graph.isAcyclic()){  //Loops not supported yet
	    _graph=null;
	    throw new RuntimeException("Loops currently not supported");
	}

	// Merge control flow nodes (insert muxes)
	_controlFlowAnalysis(graph);

	//System.out.println(PtDirectedGraphToDotty.convert(graph));	

	// flattening of control flow graph into a single dataflow graph
	//_extractDataFlow(graph);
    } 
    // Method _analyze

    protected void _extractDataFlow(DirectedGraph graph){
	Set keys=_requiredNodeMap.keySet();
	for (Iterator i=keys.iterator(); i.hasNext(); ){
	    Port port=(Port)i.next();
	    GraphNode gn=(GraphNode)_requiredNodeMap.get(port);
	    DirectedGraph dg=new DirectedGraph();
	    gn.createDataFlow(dg, port);
	}
	
    }
    
    protected void _controlFlowAnalysis(DirectedGraph graph){
	//Get a topological sort

	Map nodeToLabel = new HashMap();
	SuperBlock sorted[]=null;
	
	try {
	    Object []temp=graph.attemptTopologicalSort(graph.nodes()).toArray();
	    sorted=new SuperBlock[temp.length];   
	    for (int i=0; i < temp.length; i++){
		sorted[i]=(SuperBlock)((Node)temp[i]).weight();
	    }
	} catch (IllegalActionException e){
	    throw new RuntimeException(e.toString());
	}

	//sorted[0].addLabel(new Label(), null);
	
	for (int i=0; i < sorted.length; i++){
	    sorted[i].combineLabels(graph);
	    for (Iterator succs = graph.successors(graph.node(sorted[i])).iterator();
		 succs.hasNext();){
		Node succ = (Node)succs.next();
		
		sorted[i].propagateLabelsTo((SuperBlock)succ.weight());
	    }
	}
	
    }
    

    /**
     * This method will add a graph (disconnected?) to the origional
     * graph. This is done by adding all nodes and edges from the
     * secondary graph to the original graph. This method will also
     * add edges from all sink Nodes in the original graph to all of the
     * source Nodes in the added graph.
     *
     * TODO: This method should probably go somewhere else
     * (graph package?)
     **/
    protected void _appendGraph(DirectedGraph graph, DirectedGraph append){

	Collection sinks = graph.sinkNodes();
	Collection sources = append.sourceNodes();
	
	// add nodes to new graph
	for (Iterator i=append.nodes().iterator(); i.hasNext();){
	    Node node=(Node)i.next();
	    graph.addNode(node);
	}

	// add edges to new graph
	for (Iterator i=append.edges().iterator(); i.hasNext();){
	    graph.addEdge((Edge)i.next());
	}

	for (Iterator i=sinks.iterator(); i.hasNext(); ){
	    Node first=(Node)i.next();
	    for (Iterator j=sources.iterator(); j.hasNext(); ){
		Node second=(Node)j.next();
		graph.addEdge(first, second);
	    }
	}
	
    }
    
    protected ptolemy.data.type.Type _getPortType(Port port) 
	throws RuntimeException {
	ptolemy.data.type.Type t=null;
	try {
	    TypedIOPort tport=(TypedIOPort)port;
	    t=tport.getType();
	} catch (ClassCastException e){
	    throw new RuntimeException("Must have ports that are TypedIOPorts");
	}
	if (t.equals(BaseType.FIX) || t.equals(BaseType.INT) ||
	    t.equals(BaseType.LONG) /*|| t.equals(BaseType.UNSIGNED_BYTE)*/ ||
	    t.equals(BaseType.BOOLEAN) ) {
	    return t;
	} else {
	    throw new RuntimeException("Unsupported port type "+t+" in port "+port);
	}
    }

    private DirectedGraph _graph;
    private Map _requiredNodeMap;
    private int count = 0;
    private static Predefined _predefined=new Predefined();
}
