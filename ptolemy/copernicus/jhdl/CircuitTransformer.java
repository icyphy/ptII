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

import byucc.jhdl.base.Cell;
import byucc.jhdl.base.HWSystem;
import byucc.jhdl.Logic.Logic;

import soot.*;
import soot.jimple.*;
//import soot.jimple.toolkits.invoke.MethodCallGraph;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
//import soot.jimple.toolkits.invoke.InvokeGraph;
//import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
//import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
//import soot.jimple.toolkits.invoke.VTATypeGraph;
//import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;

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
//import ptolemy.copernicus.kernel.MustAliasAnalysis;
import ptolemy.copernicus.java.ModelTransformer;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.copernicus.jhdl.soot.*;
import ptolemy.copernicus.jhdl.util.*;

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
        return "targetPackage outDir";
    }

    /**
     * 1. Create a DAG that matches topology of model
     * 2.
     **/
    protected void internalTransform(String phaseName, Map options) {
        System.out.println("CircuitTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        //_outDir = Options.getString(options, "outDir");
        _outDir = PhaseOptions.getString(options, "outDir");
        
        System.out.println("**************************************************");
        System.out.println("*** START JHDL");
        System.out.println("**************************************************");
        System.out.println("\nCircuitTransformer.internalTransform("
                + phaseName + ", " + options + ")");

//  	ModelGraph dg = new CompositeModelGraph(_model,options);
//  	PtDirectedGraphToDotty toDotty = new PtDirectedGraphToDotty();
//  	toDotty.writeDotFile(".", _model.getName(), dg);

	ptolemy.copernicus.jhdl.circuit.JHDLCompositeActor c = null;
	ptolemy.copernicus.jhdl.circuit.JHDLActorTestbench testbench = null;
	try {
	    System.out.println("here");
	    c = ptolemy.copernicus.jhdl.circuit.CompositeActor2JHDLCompositeActor.build(_model,options);
	    System.out.println("here");
	    boolean resolve = c.resolve();
	    System.out.println("here");
	    int descriptionLevel = 
		NamedObj.LINKS | 
		NamedObj.FULLNAME |
		//NamedObj.ATTRIBUTES |
		NamedObj.CONTENTS |
		NamedObj.DEEP;
	    System.out.println(c.description(descriptionLevel));
	    if (resolve) {
		testbench = 
		    new ptolemy.copernicus.jhdl.circuit.JHDLActorTestbench(c);
		HWSystem hw = new HWSystem();
		testbench.build(hw);
		new byucc.jhdl.apps.Viewers.JL.CLIJL(testbench.getTestbench());
	    }
	} catch (IllegalActionException e) {
            e.printStackTrace();
	} catch (NameDuplicationException e) {
            e.printStackTrace();
	}

	//HWSystem hw = new HWSystem();
	//JHDLTestbench jtb = new JHDLTestbench(hw);

	/*
	//////////////////////////////////////////////
	// Step 1. Create a DirectedGraph that matches
	//         the topology of the model
	//////////////////////////////////////////////
	DirectedGraph combinedGraph = _createModelGraph(_model);

	//////////////////////////////////////////////
	// Step 2. Create a DFG for each entity in the model.
	//////////////////////////////////////////////
	Map entityGraphMap = _createEntityGraphs(_model, options);

	//////////////////////////////////////////////
	// Step 3. Create top-level testbench
	//////////////////////////////////////////////
	HWSystem hw = new HWSystem();
	JHDLTestbench _jtb = new JHDLTestbench(hw);

	//	Cell _cell = _createTopLevelCell(_jtb,combinedGraph);

	//////////////////////////////////////////////
	// Step 3. Insert each DFG into the top-level graph
	//////////////////////////////////////////////
	_insertEntityGraphs(_model,combinedGraph,entityGraphMap);

	PtDirectedGraphToDotty toDotty = new PtDirectedGraphToDotty();
	toDotty.writeDotFile(".", _model.getName(), combinedGraph);
	*/

 	System.out.println("**************************************************");
	System.out.println("*** END JHDL");
	System.out.println("**************************************************");
    }

    /**
     * Create a DirectedGraph that matches the topology of the
     * origional model. This graph will have the following properties:
     * - All entities in the model will have a node in the graph
     * - All input/output ports associated with each entitty will have
     *   a node in the graph (with appropriate edges)
     * - All top-level input/output ports will have a node in the
     *   graph (with appropriate edges)
     **/
    protected DirectedGraph _createModelGraph(CompositeActor model) {

        System.out.println("1. Creating Graph of Model "+model.getName());

        DirectedGraph combinedGraph = new DirectedGraph();

        // 1. Loop over all the actors in the model. For each actor,
        //    add the following to the top-level graph:
        //    - a Node in the graph for the entity
        //    - a Node in the graph for each input and output
        //      port of the entity
        //    - appropriate edges between input/output ports and entity
        System.out.println("   Add actors");
        for (Iterator i = model.entityList().iterator(); i.hasNext();) {

            Entity entity = (Entity)i.next();
            System.out.println("    Adding entity "+entity);

            // add Node to graph corresponding to entity
            combinedGraph.addNodeWeight(entity);

            // iterate over all outPorts and add Node corresponding
            // to port. Also add edge between entity and port
            for (Iterator outPorts =
                     ((TypedAtomicActor)entity).outputPortList().iterator();
                 outPorts.hasNext();){
                Object port=outPorts.next();
                System.out.println("     Adding outport "+((Nameable)port).getName());
                combinedGraph.addNodeWeight(port);
                combinedGraph.addEdge(entity, port);
            }

            // iterate over all inPorts and add Node corresponding
            // to port. Also add edge between entity and port
            for (Iterator inPorts =
                     ((TypedAtomicActor)entity).inputPortList().iterator();
                 inPorts.hasNext();){
                Object port=inPorts.next();
                System.out.println("     Adding inport "+((Nameable)port).getName());
                combinedGraph.addNodeWeight(port);
                combinedGraph.addEdge(port, entity);
            }
        }

        // 2. Connect top-level inputPorts to the ports of the connected
        // actors
        System.out.println("   Add top-level input ports");
        for (Iterator inputPorts=model.inputPortList().iterator();
             inputPorts.hasNext();){
            IOPort port = (IOPort)inputPorts.next();
            System.out.println("    Input Port = " + port + " inside sinks="
                    + port.insideSinkPortList().size());
            combinedGraph.addNodeWeight(port);
            for (Iterator insideSinks = port.insideSinkPortList().iterator();
                 insideSinks.hasNext();) {
                IOPort insideSink = (IOPort)insideSinks.next();
                System.out.println("     remote port="+insideSink);
                combinedGraph.addEdge(port, insideSink);
            }
        }

        // 3. Iterate over all output ports and make connections in graph
        // representing topology of model
        System.out.println("   Add output connections");
        for (Iterator i = model.entityList().iterator(); i.hasNext();) {
            Entity entity = (Entity)i.next();
            for (Iterator outPorts = ((TypedAtomicActor)entity).outputPortList().iterator(); outPorts.hasNext();){
                IOPort port = (IOPort) outPorts.next();
                System.out.println("    Entity output Port="+port+
                        " sinks="+port.sinkPortList().size());
                // insideSinkPortList(), sinkPortList()
                for (Iterator sinkPorts = port.sinkPortList().iterator();
                     sinkPorts.hasNext();) {
                    IOPort sinkPort = (IOPort) sinkPorts.next();
                    System.out.println("     Sink Port="+sinkPort);

                    if (!combinedGraph.containsNodeWeight(sinkPort)) {
                        combinedGraph.addNodeWeight(sinkPort);
                    }
                    combinedGraph.addEdge(port,sinkPort);
                }
            }
        }

        // Write out model
        //PtDirectedGraphToDotty toDotty = new PtDirectedGraphToDotty();
        //toDotty.writeDotFile(_outDir, model.getName(), combinedGraph);
        return combinedGraph;
    }

    // This method will create a directed graph representing the
    // dataflow of each entity in the model. The method will return
    // a HashMap that associates an entity to a Directed Graph
    protected Map _createEntityGraphs(CompositeActor model, Map options) {
        List entityList = model.entityList();
        Map entityMap = new HashMap(entityList.size());

        for (Iterator i = entityList.iterator(); i.hasNext();) {

            Entity entity = (Entity) i.next();

            DirectedGraph entityGraph = _createEntityGraph(entity, options);

            entityMap.put(entity,entityGraph);
        }

        return entityMap;
    }

    // This method creates a directed graph for a single entity in the
    // model. It calls IntervalBlockDirectedGraph to create the graph.
    protected DirectedGraph _createEntityGraph(Entity entity, Map options) {

        System.out.println("2. Creating Graph of Entity ");

        // Get the names of the class and entity
        String className =
            ModelTransformer.getInstanceClassName(entity,options);
        String entityClassName = entity.getClass().getName();

        // skip some classes?
        //          if (entityClassName.equals("ptolemy.actor.lib.Const") ||
        //              entityClassName.equals("ptolemy.actor.lib.FileWriter"))
        //              return null;

        System.out.println("Creating graph for class "+className+
                " (entity="+entityClassName+")");
        SootClass entityClass = Scene.v().loadClassAndSupport(className);

        SootMethod method = entityClass.getMethodByName("fire");
        DirectedGraph fireGraph=null;
        try {
            fireGraph = new IntervalBlockDirectedGraph(method);
        } catch(IllegalActionException e) {
            System.err.println("Error "+e);
            e.printStackTrace();
            System.exit(1);
        }

        PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
        //        dgToDotty.writeDotFile(".", className,fireGraph);
        dgToDotty.writeDotFile(".", "entity", fireGraph);

        return fireGraph;

    }

    // This method will insert the directed graph for each actor into
    // the top-level model
    protected void _insertEntityGraphs(CompositeActor model,
            DirectedGraph topGraph,
            Map entityGraphMap) {

        for (Iterator i = model.entityList().iterator(); i.hasNext();) {
            Entity entity = (Entity) i.next();
            DirectedGraph entityGraph =
                (DirectedGraph) entityGraphMap.get(entity);
            _insertEntityGraph(topGraph,entity,entityGraph);
        }
    }

    /**
     * This method will insert the graph associated with the given
     * entity into the top-level model graph.
     **/
    protected void _insertEntityGraph(DirectedGraph graph, Entity entity,
            DirectedGraph entityGraph) {

        System.out.println("3. Inserting Entity Graph ");

        TypedAtomicActor actor = (TypedAtomicActor) entity;

        // Clean up graph and identify ports in graph
        Map portCallNodes = _getPortCallNodes(actor,entityGraph);

        // Insert actor graph into top-level graph
        graph.addGraph(entityGraph);

        // Remove Node in graph representing entity
        Node entityNode = graph.node(entity);
        graph.removeNode(entityNode);

        // Iterate through the ports of the entity and connect the
        // entity graph port Nodes to the top-level port nodes
        for (Iterator i=portCallNodes.keySet().iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();
            Node topLevelPortNode = graph.node(port);

            // get the list of entity Nodes associated with this Port
            List nodeList = (List) portCallNodes.get(port);

            // iterate over all nodes in the list
            for (Iterator j = nodeList.iterator(); j.hasNext();) {
                Node n = (Node) j.next();
                if (port.isInput()) {
                    graph.addEdge(topLevelPortNode,n);
                } else {
                    graph.addEdge(n,topLevelPortNode);
                }
            }
        }

        /*

          List inputPortList = actor.inputPortList();
          List outputPortList = actor.outputPortList();

          Map inputPortNodeMap = new HashMap(inputPortList.size());
          Map outputPortNodeMap = new HashMap(outputPortList.size());
          // Iterate over all actor input ports and obtain top-level
          // node associated with this port
          for (Iterator i=inputPortList.iterator();
          i.hasNext();) {
          IOPort port = (IOPort) i.next();
          // Identify the Node in the top-level graph associated
          // with this port
          Node portNode = graph.node(port);
          inputPortNodeMap.put(port,portNode);
          }
          // Iterate over all actor output ports and obtain top-level
          // node associated with this port
          for (Iterator i=outputPortList.iterator();
          i.hasNext();) {
          IOPort port = (IOPort) i.next();
          // Identify the Node in the top-level graph associated
          // with this port
          Node portNode = graph.node(port);
          outputPortNodeMap.put(port,portNode);
          }

          // Iterate over the input ports
          for (Iterator i=inputPortNodeMap.keySet().iterator(); i.hasNext();) {
          IOPort inputport = (IOPort) i.next();


          }

          Map inputPorts = new HashMap();
          Map outputPorts = new HashMap();

          // 1. Identify ports in entity graph
          // TODO: multiple calls to get in different control flow paths?
          // - Assume homogoneous (need to think about how to connect
          //   port calls for multi-rate ports)
          // - Assume non-hierarchical (need to recursively build graph)
          for (Iterator entityNodes = entityGraph.nodes().iterator();
          entityNodes.hasNext();) {
          Node node = (Node) entityNodes.next();
          Object weight = node.getWeight();

          // See if weight is of type virtualinvoke
          if (!(weight instanceof VirtualInvokeExpr))
          continue;
          VirtualInvokeExpr expr = (VirtualInvokeExpr) weight;

          // See if the invoke calls the appropriate method
          String methodName = expr.getMethod().getName();
          System.out.println("   VirtualInvoke="+methodName);
          if (!methodName.equals("getInt") &&
          !methodName.equals("sendInt"))
          continue;

          FieldRef ref = _getFieldRef(entityGraph,node);
          SootField field = ref.getField();
          String portName = field.getName();

          if (methodName.equals("getInt")) {
          inputPorts.put(portName,node);
          }
          if (methodName.equals("sendInt")) {
          outputPorts.put(portName,node);
          }
          }
        */

        // 2. Add "in-between"
        //

        // 3.
    }

    // This method will identify all Nodes within the graph that
    // are associated with Port calls for each IOPort in the actor.
    // The key to the map is a IOPort and the Value is a List of
    // Nodes
    protected Map _getPortCallNodes(TypedAtomicActor actor,
            DirectedGraph entityGraph) {

        Map portNodeMap = new HashMap();

        // Create a mapping between port name and IOPort object
        List inputPortList = actor.inputPortList();
        List outputPortList = actor.outputPortList();
        Map stringPortMap = new HashMap(inputPortList.size() +
                outputPortList.size());
        for (Iterator i=inputPortList.iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();
            String portName = port.getName();
            stringPortMap.put(portName,port);
            //System.out.println("Port="+portName+" is "+port);
        }
        for (Iterator i=outputPortList.iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();
            String portName = port.getName();
            stringPortMap.put(portName,port);
            //System.out.println("Port="+portName+" is "+port);
        }

        for (Iterator entityNodes = entityGraph.nodes().iterator();
             entityNodes.hasNext();) {
            Node node = (Node) entityNodes.next();
            Object weight = node.getWeight();

            // See if weight is of type virtualinvoke
            if (!(weight instanceof VirtualInvokeExpr))
                continue;
            VirtualInvokeExpr expr = (VirtualInvokeExpr) weight;

            // See if the invoke calls the appropriate method
            String methodName = expr.getMethod().getName();
            //System.out.println("   VirtualInvoke="+methodName);
            if (!methodName.equals("getInt") &&
                    !methodName.equals("sendInt"))
                continue;

            FieldRef ref = _getFieldRef(entityGraph,node);
            SootField field = ref.getField();
            String portName = field.getName();


            //                System.out.println("Found port node "+node+ " with methodname="+
            //                                   methodName + " portname="+portName);
            IOPort port = (IOPort) stringPortMap.get(portName);
            //              System.out.println("Found port call "+port);

            Node portNode=null;
            if (methodName.equals("getInt")) {
                // "getInt" method call
                portNode = (Node) entityGraph.successors(node).iterator().next();
            } else {
                // "sendInt" method call
                for (Iterator i = entityGraph.predecessors(node).iterator();
                     i.hasNext();) {
                    Node n = (Node) i.next();
                    Edge e = (Edge) entityGraph.predecessorEdges(node,n).iterator().next();
                    if (e.hasWeight())
                        portNode = n;
                }
            }

            List nodeList = (List) portNodeMap.get(port);
            if (nodeList == null) {
                nodeList = new Vector();
                portNodeMap.put(port,nodeList);
            }
            nodeList.add(portNode);
        }

        /*
          for (Iterator i=portNodeMap.keySet().iterator();i.hasNext();) {
          IOPort port = (IOPort) i.next();
          System.out.print("Port="+port);
          System.out.print(" nodes=");
          List l = (List) portNodeMap.get(port);
          for (Iterator j=l.iterator();j.hasNext();) {
          Node n = (Node) j.next();
          System.out.print(n+" ");
          }
          System.out.println();
          }
        */

        // Fix up graph (remove some garbage)
        for (Iterator i = portNodeMap.keySet().iterator();i.hasNext();) {
            IOPort port = (IOPort) i.next();
            //System.out.println("Port="+port);
            if (port.isInput()) {
                // inputs
                List l = (List) portNodeMap.get(port);
                for (Iterator j = l.iterator(); j.hasNext(); ) {
                    Node node = (Node) j.next();
                    Node predecessor = (Node) entityGraph.predecessors(node).iterator().next();
                    _deleteLeafBranch(entityGraph,predecessor);
                }
            } else {
                // outputs
                List l = (List) portNodeMap.get(port);
                for (Iterator j = l.iterator(); j.hasNext(); ) {
                    Node node = (Node) j.next();
                    // find successors
                    Node successor =
                        (Node) entityGraph.successors(node).iterator().next();
                    // remove edge from node to sucessor
                    Edge e = (Edge) entityGraph.successorEdges(node,successor).iterator().next();
                    entityGraph.removeEdge(e);
                    // Remove branch
                    _deleteLeafBranch(entityGraph,successor);
                }
            }
        }

        PtDirectedGraphToDotty toDotty = new PtDirectedGraphToDotty();
        toDotty.writeDotFile(".", "fixedentity", entityGraph);

        return portNodeMap;
    }

    protected void _deleteLeafBranch(DirectedGraph graph, Node node) {
        Collection predecssors = graph.predecessors(node);
        if (predecssors.size() == 1) {
            _deleteLeafBranch(graph,(Node) predecssors.iterator().next());
        }
        //System.out.println("Deleting node="+node);
        graph.removeNode(node);
    }

    /**
     * A method invocation node
     *
     **/
    // From a invokevirtual node with a "getInt" or "sendInt" method
    // name, find the predecessor node that refers to the Field from
    // which this node is called. (weak)
    protected FieldRef _getFieldRef(DirectedGraph graph, Node node) {

        // Find non-argument predecessor
        Node predecessor = null;
        for (Iterator i = graph.predecessors(node).iterator();
             i.hasNext();) {
            Node n = (Node) i.next();
            //              System.out.println("Node="+node+" pred="+n);
            Edge e = (Edge) graph.predecessorEdges(node,n).iterator().next();
            if (!e.hasWeight())
                predecessor = n;
        }

        Node fieldRefNode = (Node) graph.predecessors(predecessor).iterator().next();

        return (FieldRef) fieldRefNode.getWeight();
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
    private String _outDir;
}
