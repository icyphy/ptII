/* A transformer that removes unnecessary fields from classes.

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
//import soot.toolkits.graph.*;
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

import ptolemy.data.expr.Variable;
import ptolemy.graph.*;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;
import ptolemy.copernicus.kernel.MustAliasAnalysis;
import ptolemy.copernicus.java.ActorTransformer;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;

//////////////////////////////////////////////////////////////////////////
//// CircuitTransformer
/**
A transformer that removes unnecessary fields from classes.
@author Steve Neuendorffer and Ben Warlick
@version $Id$
@since Ptolemy II 2.0
*/
public class CircuitTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private CircuitTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static CircuitTransformer v(CompositeActor model) {
        return new CircuitTransformer(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage";
    }

    /**
     * 1. Create a DAG that matches topology of model
     * 2.
     **/
    protected void internalTransform(String phaseName, Map options) {

	System.out.println("**************************************************");
	System.out.println("*** START JHDL");
	System.out.println("**************************************************");
        System.out.println("\nCircuitTransformer.internalTransform("
			   + phaseName + ", " + options + ")");

	//////////////////////////////////////////////
	// Step 1. Create a DirectedGraph that matches
	//         the topology of the model
	//////////////////////////////////////////////
        DirectedGraph combinedGraph = _createModelGraph(_model);

	//////////////////////////////////////////////
	// Step 2. Call 'CircuitAnalysis' on each actor
	// in the model. CircuitAnalysis will create a DAG
	// for each node - hash this graph with the node.
	//////////////////////////////////////////////
        Set removeSet = new HashSet();
	Map replaceMap = new HashMap();

	for(Iterator cnodes=combinedGraph.nodes().iterator();
	    cnodes.hasNext();) {

	    Node cnode=(Node)cnodes.next();

	    //Skip ports; only Entity's are expanded
	    if (!(cnode.getWeight() instanceof Entity)) continue;

	    // Get directed graph of entity (if null, don't replace)
	    DirectedGraph operatorGraph =
		_createEntityGraph((Entity) cnode.getWeight(), options);
	    if (operatorGraph != null)
		replaceMap.put(cnode,operatorGraph);

	}


	//////////////////////////////////////////////
	// Step 3. Replace node in DAG with the DAG
	// that was created by CircuitAnalysis
	//////////////////////////////////////////////
	for (Iterator removeNodes = replaceMap.keySet().iterator();
	     removeNodes.hasNext();){

	    Node removeNode = (Node)removeNodes.next();

	    combinedGraph.removeNode(removeNode);

	    DirectedGraph operatorGraph = (DirectedGraph)replaceMap.get(removeNode);

	    for(Iterator nodes = operatorGraph.nodes().iterator(); nodes.hasNext();) {
		Node node = (Node)nodes.next();
		combinedGraph.addNode(node);
	    }

	    for(Iterator edges = operatorGraph.edges().iterator(); edges.hasNext();) {
		combinedGraph.addEdge((Edge)edges.next());
	    }

	}

        // Remove all the nodes that were not required above.
//          for(Iterator nodes = removeSet.iterator();
//              nodes.hasNext();) {
//  	    Node node = (Node)nodes.next();
//              List predList = new LinkedList(combinedGraph.predecessors(node));
//              for(Iterator preds = predList.iterator();
//                  preds.hasNext();) {
//                  Node pred = (Node)preds.next();
//                  combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(pred, node).toArray()[0]);
//              }
//              List succList = new LinkedList(combinedGraph.successors(node));
//              for(Iterator succs = succList.iterator();
//                  succs.hasNext();) {
//                  Node succ = (Node)succs.next();
//                  combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(node, succ).toArray()[0]);
//              }
//              combinedGraph.removeNode(node);
//          }


	//  	System.out.println("Tails:");
	//  	for (Iterator i=combinedGraph.getTails().iterator(); i.hasNext();){
	//  	  System.out.println(i.next());
	//  	}
	//  	System.out.println("Heads:");
	//  	for (Iterator i=combinedGraph.getHeads().iterator(); i.hasNext();){
	//  	  System.out.println(i.next());
	//  	}


        // Remove all the loner nodes (not connected to any other node
//  	removeSet=new HashSet();
//  	Set loners=new HashSet();
//  	loners.addAll(combinedGraph.sinkNodes());
//  	loners.retainAll(combinedGraph.sourceNodes());
//  	removeSet.addAll(loners);

//          for(Iterator nodes = removeSet.iterator();
//              nodes.hasNext();) {
//  	    Node node = (Node)nodes.next();
//              List predList = new LinkedList(combinedGraph.predecessors(node));
//              for(Iterator preds = predList.iterator();
//                  preds.hasNext();) {
//                  Node pred = (Node)preds.next();
//                  combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(pred, node).toArray()[0]);
//              }
//              List succList = new LinkedList(combinedGraph.successors(node));
//              for(Iterator succs = succList.iterator();
//                  succs.hasNext();) {
//                  Node succ = (Node)succs.next();
//                  combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(node, succ).toArray()[0]);
//              }
//              combinedGraph.removeNode(node);
//          }


	//////////////////////////////////////////////
	// Step 4. Spit out the graph
	//////////////////////////////////////////////
	//PtDirectedGraphToDotty.writeDotFile(_model.getName(),combinedGraph);

	//          // Write as a circuit.
	//          try {
	//              String targetPackage = Options.getString(options, "targetPackage");
	//              String outDir = Options.getString(options, "outDir");
	//              CircuitCreator.create(combinedGraph,
	//                      outDir, targetPackage,
	//                      "JHDL" + StringUtilities.sanitizeName(
	//                              _model.getName()));
	//          } catch (Exception ex) {
	//              ex.printStackTrace();
	//          }
	System.out.println("**************************************************");
	System.out.println("*** END JHDL");
	System.out.println("**************************************************");
    }

    protected DirectedGraph _createEntityGraph(Entity entity, Map options) {
	String className =
	    ActorTransformer.getInstanceClassName(entity,options);

	String entityClassName = entity.getClass().getName();
	// skip some classes
	if (entityClassName.equals("ptolemy.actor.lib.Const") ||
	    entityClassName.equals("ptolemy.actor.lib.FileWriter"))
	    return null;

	System.out.println("Creating graph for class "+className+
			   " (entity="+entityClassName+")");
	SootClass entityClass = Scene.v().loadClassAndSupport(className);

	CircuitAnalysis analysis=null;
	try {
	    analysis =
		new CircuitAnalysis(entity, entityClass);
	} catch(IllegalActionException e) {
            e.printStackTrace();
	    throw new RuntimeException(e.toString());
	}
	DirectedGraph operatorGraph = analysis.getOperatorGraph();
	return operatorGraph;
    }

    /**
     * Create a DirectedGraph that matches
     * the topology of the model
     **/
    protected DirectedGraph _createModelGraph(CompositeActor model) {
        DirectedGraph combinedGraph = new DirectedGraph();
        // Loop over all the actors in the model
        for(Iterator i = model.entityList().iterator(); i.hasNext();) {
            Entity entity = (Entity)i.next();

	    // add Node to graph corresponding to entity
	    combinedGraph.addNodeWeight(entity);

	    // iterate over all outPorts and add Node corresponding
	    // to port. Also add edge between entity and port
	    for (Iterator outPorts =
		   ((TypedAtomicActor)entity).outputPortList().iterator();
		 outPorts.hasNext();){
		Object port=outPorts.next();
		combinedGraph.addNodeWeight(port);
		combinedGraph.addEdge(entity, port);
	    }

	    // iterate over all inPorts and add Node corresponding
	    // to port. Also add edge between entity and port
	    for (Iterator inPorts =
		   ((TypedAtomicActor)entity).inputPortList().iterator();
		 inPorts.hasNext();){
		Object port=inPorts.next();
		combinedGraph.addNodeWeight(port);
		combinedGraph.addEdge(port, entity);
	    }
        }


	// Connect top-level inputPorts to the ports of the connected
	// actors
	for (Iterator inputPorts=model.inputPortList().iterator();
	     inputPorts.hasNext();){
	    IOPort port = (IOPort)inputPorts.next();

	    for(Iterator remoteports = port.connectedPortList().iterator();
		remoteports.hasNext();) {
		IOPort remotePort = (IOPort)remoteports.next();
		// TODO: this looks like a bug - port has
		// not been added to the graph?
		combinedGraph.addEdge(port, remotePort);
		//  	    removeSet.add(port);
		//  	    removeSet.add(remotePort);
	    }
	}

	// Add edges to the DAG to match the topology of the
	// connections between individual actors
        for(Iterator entities = model.entityList().iterator();
            entities.hasNext();) {
            TypedAtomicActor actor = (TypedAtomicActor)entities.next();

            for(Iterator ports = actor.outputPortList().iterator();
                ports.hasNext();) {
                IOPort port = (IOPort)ports.next();

                for(Iterator remoteports = port.connectedPortList().iterator();
                    remoteports.hasNext();) {
                    IOPort remotePort = (IOPort)remoteports.next();
                    combinedGraph.addEdge(port, remotePort);
		    //                      removeSet.add(port);
		    //                      removeSet.add(remotePort);
                }
            }
        }

	// Write out model
	PtDirectedGraphToDotty.writeDotFile("model",combinedGraph);
	return combinedGraph;
    }

    protected void _insertActorDAG(DirectedGraph graph) {
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
	    }
	} catch (ptolemy.kernel.util.KernelException e) {
	    System.err.println(e);
	}
    }

    private CompositeActor _model;

}
