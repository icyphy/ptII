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

    protected void internalTransform(String phaseName, Map options) {
        System.out.println("CircuitTransformer.internalTransform("
			   + phaseName + ", " + options + ")");

        DirectedGraph combinedGraph = new DirectedGraph();
	
        // Loop over all the actor instance classes.
        for(Iterator i = _model.entityList().iterator();
            i.hasNext();) {
            Entity entity = (Entity)i.next();

	    combinedGraph.addNodeWeight(entity);
	    for (Iterator outPorts = ((TypedAtomicActor)entity).outputPortList().iterator();
		 outPorts.hasNext();){
		Object port=outPorts.next();
		combinedGraph.addNodeWeight(port);
		combinedGraph.addEdge(entity, port);
	    }
	    for (Iterator inPorts = ((TypedAtomicActor)entity).inputPortList().iterator();
		 inPorts.hasNext();){
		Object port=inPorts.next();
		combinedGraph.addNodeWeight(port);
		combinedGraph.addEdge(port, entity);
	    }

	    //              CircuitAnalysis analysis =
	    //                  new CircuitAnalysis(entity, entityClass);
	    //              HashMutableDirectedGraph operatorGraph =
	    //                  analysis.getOperatorGraph();
            
	    //              for(Iterator nodes = operatorGraph.getNodes().iterator();
	    //                  nodes.hasNext();) {
	    //                  Object node = nodes.next();
	    //                  combinedGraph.addNode(node);
	    //              }
	    //              for(Iterator nodes = operatorGraph.getNodes().iterator();
	    //                  nodes.hasNext();) {
	    //                  Object node = nodes.next();
	    //                  List succList = new LinkedList(operatorGraph.getSuccsOf(node));
	    //                  for(Iterator succs = succList.iterator();
	    //                      succs.hasNext();) {
	    //                      Object succ = succs.next();
	    //                      combinedGraph.addEdge(node, succ);
	    //                  }
	    //              }
        }
        
	//          Set removeSet = new HashSet();

        // Connect the combined graph.

	//Connect top-level ports
	for (Iterator inputPorts=_model.inputPortList().iterator();
	     inputPorts.hasNext();){
	    IOPort port = (IOPort)inputPorts.next();
	  
	    for(Iterator remoteports = port.connectedPortList().iterator();
		remoteports.hasNext();) {
		IOPort remotePort = (IOPort)remoteports.next();
		combinedGraph.addEdge(port, remotePort);
		//  	    removeSet.add(port);
		//  	    removeSet.add(remotePort);
	    }	  
	}
	//Connect ports of each actor
        for(Iterator entities = _model.entityList().iterator();
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
                  
	//          // remove the extra nodes for ports.
	//          for(Iterator nodes = removeSet.iterator();
	//              nodes.hasNext();) {
	//              Object node = nodes.next();
	//              // Then remove the node.
	//              for(Iterator preds = combinedGraph.getPredsOf(node).iterator();
	//                  preds.hasNext();) {
	//                  Object pred = preds.next();
	//                  for(Iterator succs = combinedGraph.getSuccsOf(node).iterator();
	//                      succs.hasNext();) {
	//                      Object succ = succs.next();
	//                      combinedGraph.addEdge(pred, succ);
	//                  }
	//              }
	//          }
            
        Set removeSet = new HashSet();

	//Flatten the actors
	for(Iterator cnodes=combinedGraph.nodes().iterator(); cnodes.hasNext();){
	    Node cnode=(Node)cnodes.next();

	    //Skip ports; only Entity's are expanded
	    if (!(cnode.weight() instanceof Entity)) continue;

	    removeSet.add(cnode);
  
	    String className =
		ActorTransformer.getInstanceClassName((Entity)cnode.weight(),
						      options);
	    SootClass entityClass = 
		Scene.v().loadClassAndSupport(className);

	    CircuitAnalysis analysis =
		new CircuitAnalysis((Entity)cnode.weight(), entityClass);
	    DirectedGraph operatorGraph = analysis.getOperatorGraph();

	    for(Iterator nodes = operatorGraph.nodes().iterator();
		nodes.hasNext();) {
		Node node = (Node)nodes.next();
		//Ports are already in combinedGraph, don't add them again
		if (!combinedGraph.containsNode(node))
		    combinedGraph.addNode(node);
		else if (!(node.weight() instanceof Port))
		    removeSet.remove(cnode);
	    }

	    for(Iterator nodes = operatorGraph.nodes().iterator();
		nodes.hasNext();) {
		Node node = (Node)nodes.next();
		List succList = new LinkedList(operatorGraph.successors(node));
		for(Iterator succs = succList.iterator();
		    succs.hasNext();) {
		    Node succ = (Node)succs.next();
		    combinedGraph.addEdge(node, succ);
		}
	    }
	  
	}
	
        // Remove all the nodes that were not required above.
        for(Iterator nodes = removeSet.iterator();
            nodes.hasNext();) {
	    Node node = (Node)nodes.next();
            List predList = new LinkedList(combinedGraph.predecessors(node));
            for(Iterator preds = predList.iterator();
                preds.hasNext();) {
                Node pred = (Node)preds.next();
                combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(pred, node).toArray()[0]);
            }
            List succList = new LinkedList(combinedGraph.successors(node));
            for(Iterator succs = succList.iterator();
                succs.hasNext();) {
                Node succ = (Node)succs.next();
                combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(node, succ).toArray()[0]);
            }
            combinedGraph.removeNode(node);
        }

	
	//  	System.out.println("Tails:");
	//  	for (Iterator i=combinedGraph.getTails().iterator(); i.hasNext();){
	//  	  System.out.println(i.next());
	//  	}
	//  	System.out.println("Heads:");
	//  	for (Iterator i=combinedGraph.getHeads().iterator(); i.hasNext();){
	//  	  System.out.println(i.next());
	//  	}

	
        // Remove all the loner nodes (not connected to any other node
	removeSet=new HashSet();
	Set loners=new HashSet();
	loners.addAll(combinedGraph.sinkNodes());
	loners.retainAll(combinedGraph.sourceNodes());
	removeSet.addAll(loners);
	
        for(Iterator nodes = removeSet.iterator();
            nodes.hasNext();) {
	    Node node = (Node)nodes.next();
            List predList = new LinkedList(combinedGraph.predecessors(node));
            for(Iterator preds = predList.iterator();
                preds.hasNext();) {
                Node pred = (Node)preds.next();
                combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(pred, node).toArray()[0]);
            }
            List succList = new LinkedList(combinedGraph.successors(node));
            for(Iterator succs = succList.iterator();
                succs.hasNext();) {
                Node succ = (Node)succs.next();
                combinedGraph.removeEdge((Edge)combinedGraph.successorEdges(node, succ).toArray()[0]);
            }
            combinedGraph.removeNode(node);
        }


	//  	System.out.println("Writing "+_model.getName()+".dot");
	//  	try {
	//  	    FileWriter dotFile=new FileWriter(_model.getName()+".dot");
	//  	    dotFile.write(DirectedGraphToDotty.convert(combinedGraph,_model.getName()));
	//  	    dotFile.close();
	//  	} catch (IOException e){
	//  	    System.out.println(e);
	//  	}

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
    }

    private CompositeActor _model;

}
