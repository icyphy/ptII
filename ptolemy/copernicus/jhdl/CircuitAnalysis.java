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
import ptolemy.graph.*;
import ptolemy.copernicus.kernel.*;
import ptolemy.copernicus.java.*;
import ptolemy.copernicus.jhdl.util.*;

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
    public CircuitAnalysis(Entity entity, SootClass theClass) {
	DirectedGraph graph = new DirectedGraph();
        _graph = graph;
       
        //System.out.println("className = " + entity.getClass().getName());

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

	//  	try {
	//  	  byucc.util.flowgraph.JavaControlFlowGraph jcfg =
	//  	    new byucc.util.flowgraph.JavaControlFlowGraph(theClass.toString(),"fire");

	//  	  Iterator it = jcfg.getAllNodes().iterator();
	//  	  while (it.hasNext()) {
	//  	    ((byucc.util.newgraph.Node) it.next()).setDotLabelFromToString();
	//  	  }

	//  	  jcfg.writeDotFile(theClass.toString()+".dot");
	//  	  jcfg.writeDataFlowDotFile(theClass.toString()+"_df.dot");
	//  	}
	//  	catch (IOException e){
	//  	  System.out.println(e);
	//  	}

	DirectedGraph graph1 = new DirectedGraph();
	DirectedGraph graph2 = new DirectedGraph();
	DirectedGraph graph3 = new DirectedGraph();
	
        if(theClass.declaresMethodByName("prefire")) {
            _analyze(graph1, theClass.getMethodByName("prefire"));
        }
        if(theClass.declaresMethodByName("fire")) {
            _analyze(graph2, theClass.getMethodByName("fire"));
        }
        if(theClass.declaresMethodByName("postfire")) {
            _analyze(graph3, theClass.getMethodByName("postfire"));
        }

	_appendGraph(graph, graph1);
	_appendGraph(graph, graph2);
	_appendGraph(graph, graph3);
	
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

        //System.out.println("Original graph:\r\n" + graph + "\r\n");
	System.out.println(SynthesisToDotty.convert(graph)); //, entity.getName()));

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
  
    protected void _analyze(DirectedGraph graph, SootMethod method) {
        Body body = method.retrieveActiveBody();
        CompleteUnitGraph unitGraph = new CompleteUnitGraph(body);
        // this will help us figure out where locals are defined.
        SimpleLocalDefs localDefs = new SimpleLocalDefs(unitGraph);
        SimpleLocalUses localUses = new SimpleLocalUses(unitGraph, localDefs);

	//Inline methods
	Inliner inliner = getInliner();
	inliner.inline(body);
	
	BriefBlockGraph bbgraph=new BriefBlockGraph(body);
	//System.out.println(BlockGraphToDotty.convert(bbgraph, "bbgraph"));
	List blockList=bbgraph.getBlocks();
	DirectedGraph dataFlowGraph; //[] = new DirectedGraph[blockList.size()];
	Map blockToSuperBlockMap = new HashMap();
	
	for (int blockNum=0; blockNum < blockList.size(); blockNum++){
	    Block block=(Block)blockList.get(blockNum);
	    Set requiredNodeSet = new HashSet();
	    dataFlowGraph=new DirectedGraph();

	    _blockDataFlow(block, dataFlowGraph, method, localDefs, localUses, requiredNodeSet);

	    SuperBlock sb=new SuperBlock(block, dataFlowGraph);
	    blockToSuperBlockMap.put(block, sb);

	    for (Iterator i=requiredNodeSet.iterator(); i.hasNext();){
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

	_controlFlowAnalysis(graph);

	System.out.println(PtDirectedGraphToDotty.convert(graph));	

	_extractDataFlow(graph);
    } // Method _analyze

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
    
    protected void _blockDataFlow(Block block, DirectedGraph localGraph,
				  SootMethod method,
				  SimpleLocalDefs localDefs,
				  SimpleLocalUses localUses,
				  Set requiredNodeSet){

	//Maps values to Nodes, so that we can grab the most recent
	//Node to add edges to
	Map nodeMap=new HashMap();
	
	for(Iterator units = block.iterator(); units.hasNext();) {
	    Stmt stmt = (Stmt)units.next();
	    if(stmt instanceof AssignStmt) {
		Object leftOp = ((AssignStmt)stmt).getLeftOp();
		
		if(leftOp instanceof FieldRef) {
		    SootField field = ((FieldRef)leftOp).getField();
		    // Then treat as a local.
		    leftOp = field;
		}

		//if(!localGraph.containsNodeWeight(leftOp)) {
		Node leftOpNode=localGraph.addNodeWeight(leftOp);
		    //} 
                                
		Value rightOp = ((AssignStmt)stmt).getRightOp();
		//System.err.println(rightOp.getClass().toString());
		if(rightOp instanceof FieldRef) {
		    SootField field = ((FieldRef)rightOp).getField();
		    ValueTag tag = (ValueTag)field.getTag("_CGValue");
		    if(tag == null || !(tag.getObject() instanceof Token)) {
			//This represents some state that is being read
			// Then treat as a local.
			if(!localGraph.containsNodeWeight(field)) {
			    nodeMap.put(field, localGraph.addNodeWeight(field));
			}
			localGraph.addEdge((Node)nodeMap.get(field),
					   leftOpNode);
		    } else {
			//This is a token that has been initialized to some
			//value
			// Get the constant value of the token.
			Token valueToken=(Token)tag.getObject();
			//requiredNodeSet.add(valueToken);  //Is this node really required?
			if(!localGraph.containsNodeWeight(valueToken)) {
			    nodeMap.put(valueToken, localGraph.addNodeWeight(valueToken));
			}
			localGraph.addEdge((Node)nodeMap.get(valueToken),
					   leftOpNode);
		    }   
		} else if(rightOp instanceof Local) {
		    if(!localGraph.containsNodeWeight(rightOp)) {
			nodeMap.put(rightOp, localGraph.addNodeWeight(rightOp));
		    }
		    localGraph.addEdge((Node)nodeMap.get(rightOp),
				       leftOpNode);
		} else if(rightOp instanceof InvokeExpr) {
		    InvokeExpr invokeExpr = (InvokeExpr)rightOp;
		    SootMethod invokedMethod = invokeExpr.getMethod();
		    boolean connectArguments=true;

		    if(rightOp instanceof InstanceInvokeExpr) {
			Value base = ((InstanceInvokeExpr)invokeExpr).getBase();
			
			if(invokedMethod.getName().equals("get")) {
			    //FIXME:  Make sure this is really a port.get() call, not just any get()
			    Port port = InlinePortTransformer.getPortValue(
									   method, (Local)base, stmt, localDefs, 
									   localUses);
			    if(!localGraph.containsNodeWeight(port)) {
				nodeMap.put(port, localGraph.addNodeWeight(port));
			    }
			    //requiredNodeSet.add(port); //Is this node really required?
			    localGraph.addEdge((Node)nodeMap.get(port),
					       leftOpNode);

			    connectArguments=false;//Don't create nodes for the arguments below
			} else {

			    //This is for all methods that have not been
			    //inlined yet (and aren't "get"s).  Must handle
			    //these eventually
			    nodeMap.put(invokedMethod,
					localGraph.addNodeWeight(invokedMethod));
			    if (!localGraph.containsNodeWeight(base))
				nodeMap.put(base, localGraph.addNodeWeight(base));
			    Node baseNode=(Node)nodeMap.get(base);

			    //Fix situations such as r=r.toString();, so that r is in the localGraph
			    //(added as the leftOp) but not in nodeMap yet.  So just use leftOp.
			    if (baseNode == null) baseNode=leftOpNode;
			    
			    localGraph.addEdge(baseNode,
					       (Node)nodeMap.get(invokedMethod),
					       "base");
			}
		    } else {
			//StaticInvokeExpr
			nodeMap.put(invokedMethod, localGraph.addNodeWeight(invokedMethod));
		    }

		    if (connectArguments){
			//This is skipped for a "get" method call

			int argCount=0;
			for(Iterator arguments = 
				((InvokeExpr)rightOp).getArgs().iterator();
			    arguments.hasNext();) {			    
			    Value argument = (Value)arguments.next();
			    if(!localGraph.containsNodeWeight(argument)) {
				nodeMap.put(argument, localGraph.addNodeWeight(argument));
			    }
			    localGraph.addEdge((Node)nodeMap.get(argument),
					       (Node)nodeMap.get(invokedMethod),
					       new Integer(argCount++));
			}
			localGraph.addEdge((Node)nodeMap.get(invokedMethod),
					   leftOpNode);
		    }
		    
		} else if (rightOp instanceof BinopExpr){
		    Value op1=((BinopExpr)rightOp).getOp1();
		    Value op2=((BinopExpr)rightOp).getOp2();
		  
		    nodeMap.put(rightOp, localGraph.addNodeWeight(rightOp));
		    
		    if (!localGraph.containsNodeWeight(op1)){
			nodeMap.put(op1, localGraph.addNodeWeight(op1));
		    }
		    if (!localGraph.containsNodeWeight(op2)){
			nodeMap.put(op2, localGraph.addNodeWeight(op2));
		    }

		    localGraph.addEdge((Node)nodeMap.get(op1),
				       (Node)nodeMap.get(rightOp), "op1");
		    localGraph.addEdge((Node)nodeMap.get(op2),
				       (Node)nodeMap.get(rightOp), "op2");
		    localGraph.addEdge((Node)nodeMap.get(rightOp),
				       leftOpNode);
		} else if (rightOp instanceof UnopExpr){
		    if (rightOp instanceof NegExpr){
			Value op=((UnopExpr)rightOp).getOp();
		    
			nodeMap.put(rightOp, localGraph.addNodeWeight(rightOp));
			if (!localGraph.containsNodeWeight(op)){
			    nodeMap.put(op, localGraph.addNodeWeight(op));
			}
			localGraph.addEdge((Node)nodeMap.get(op),
					   (Node)nodeMap.get(rightOp));
			localGraph.addEdge((Node)nodeMap.get(rightOp),
					   leftOpNode);
		    }
		} else {
		    //If its nothing above, just add a node for it with
		    //an edge to leftOp.  Constants will be caught here.

		    if (!localGraph.containsNodeWeight(rightOp)){
			nodeMap.put(rightOp, localGraph.addNodeWeight(rightOp));
		    }
		    localGraph.addEdge((Node)nodeMap.get(rightOp),
				       leftOpNode);
		}

		nodeMap.put(leftOp, leftOpNode);
		// end of AssignStmt 'if'
	    } else if(stmt instanceof InvokeStmt) {
		Object op = ((InvokeStmt)stmt).getInvokeExpr();
		if(op instanceof VirtualInvokeExpr) {
		    VirtualInvokeExpr invokeExpr = 
			(VirtualInvokeExpr)op;
		    SootMethod invokedMethod = invokeExpr.getMethod();
		    Value base = invokeExpr.getBase();
		    if(invokedMethod.getName().equals("send")) {
			Port port = InlinePortTransformer.getPortValue(
								       method, (Local)base, stmt, localDefs, 
								       localUses);
			// String portName = port.getName();
			if(!localGraph.containsNodeWeight(port)) {
			    nodeMap.put(port, localGraph.addNodeWeight(port));
			}
			requiredNodeSet.add(port);
                           
			Value tokenValue = invokeExpr.getArg(1);
			if(!localGraph.containsNodeWeight(tokenValue)) {
			    nodeMap.put(tokenValue, localGraph.addNodeWeight(tokenValue));
			}
			localGraph.addEdge((Node)nodeMap.get(tokenValue),
					   (Node)nodeMap.get(port));
		    }
		} // if(op instanceof VirturalInvokeExpr)
	    } // else if(stmt instanceof InvokeStmt)
	} // for(Iterator units = block.iterator(); units.hasNext();) 
    }

    protected void _appendGraph(DirectedGraph graph, DirectedGraph append){

	Collection sinks = graph.sinkNodes();
	Collection sources = append.sourceNodes();
	
	for (Iterator i=append.nodes().iterator(); i.hasNext();){
	    Node node=(Node)i.next();
	    graph.addNode(node);
	}

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
	    t.equals(BaseType.LONG) || t.equals(BaseType.UNSIGNED_BYTE) ||
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
