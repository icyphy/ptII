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

package ptolemy.copernicus.jhdl.circuit;

import byucc.jhdl.Logic.Logic;
import byucc.jhdl.Logic.Modules.arrayMult;
import byucc.jhdl.base.HWSystem;
import byucc.jhdl.base.Wire;
import byucc.jhdl.apps.Viewers.Schematic.SmartSchematicFrame;

import java.util.*;

import ptolemy.copernicus.java.ModelTransformer;

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.copernicus.jhdl.soot.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.graph.*;

import soot.jimple.*;
import soot.*;

//////////////////////////////////////////////////////////////////////////
//// 
/**
 * This class will take a PortDirectedGraph (created from a Ptolemy
 * Atomic Actor) and generate a JHDLCompositeEntity.
 *
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class ModelGraph2Entity {

    public ModelGraph2Entity(PortDirectedGraph graph, String name) throws IllegalActionException, NameDuplicationException {
	_graph = graph;
	_nodeEntityMap = new HashMap();
	_nodeRelationMap = new HashMap();
	_buildEntity(name);
    }

    protected void _buildEntity(String name) throws IllegalActionException, NameDuplicationException {

	// 1. Create empty entity
	_entity = new JHDLCompositeActor();
	_entity.setName(name);
	
	// 2. Add input ports to Entity & relation for input port
	for (Iterator i = _graph.getInputPortNodes().iterator();
	     i.hasNext();) {
	    Node inputNode = (Node) i.next();
	    Signal port = (Signal) inputNode.getWeight();
	    String portName = port.getName();
	    JHDLIOPort newPort = (JHDLIOPort) _entity.newPort(portName);
	    newPort.setInput(true);
	    newPort.setSignalWidth(port.getSignalWidth());
	    _nodeEntityMap.put(inputNode,newPort);
	    // Add input relation
	    ComponentRelation ir = _getOrAddOutputRelation(inputNode);
	    newPort.link(ir);
	}
	// 3. Add output ports to Entity
	for (Iterator i = _graph.getOutputPortNodes().iterator();
	     i.hasNext();) {
	    Node outputNode = (Node) i.next();
	    Signal port = (Signal) outputNode.getWeight();
	    String portName = port.getName();
	    JHDLIOPort newPort = (JHDLIOPort) _entity.newPort(portName);
	    newPort.setOutput(true);
	    newPort.setSignalWidth(port.getSignalWidth());
	    _nodeEntityMap.put(outputNode,newPort);
	    // Need to add link at end of processing
	}

	// 4. Iterate over each Node in the _graph
	// Skip Ports (need to remove these from Model graph)
	for (Iterator i = _graph.nodes().iterator();i.hasNext();) {
	    Node node = (Node) i.next();
	    Object weight = node.getWeight();
	    if (weight instanceof Signal)
		continue;
	    // See if an entity has been created
	    Entity nodeEntity = (Entity) _nodeEntityMap.get(node);
	    if (nodeEntity != null)
		continue; // object already created
	    _processNode(node);
	    //_nodeEntityMap.put(node,nodeEntity);
	}

	// 5. Link output Port to relation
	for (Iterator i = _graph.getOutputPortNodes().iterator();
	     i.hasNext();) {
	    Node outputNode = (Node) i.next();
	    JHDLIOPort outputPort = 
		(JHDLIOPort) _nodeEntityMap.get(outputNode);
	    // Get input edge to output Node
	    Collection c = _graph.inputEdges(outputNode);
	    if (c.size() != 1) {
		_error("Unexpected input edges for node " + outputNode);
	    }
	    Edge inputEdge = (Edge) c.iterator().next();
	    Node inputSource = inputEdge.source();
	    JHDLIORelation outputRelation = _getOutputRelation(inputSource);
	    outputPort.link(outputRelation);
	}

    }

    protected void _processNode(Node node) 
	throws IllegalActionException, NameDuplicationException, JHDLUnsupportedException {

        // Process Node
        Object nweight = node.getWeight();
        if (DEBUG) System.out.println("Node "+nweight+" ("+
                nweight.getClass().getName()+")");

	if (nweight instanceof Local) {
            _processLocal(node);
	} else if (nweight instanceof Constant) {
	    _processConstant(node);
  	} else if (nweight instanceof BinopExpr) {
  	    _processBinopExpr(node);
	} else if (nweight instanceof UnopExpr) {
	    _processUnopExpr(node);
//  	} else if (nweight instanceof BinaryMux) {
//  	    _processBinaryMux(node);
        } else {
            _unsupportedOperation(nweight);
        }
    }

    // No hardware (thus no port linking 
    protected void _processLocal(Node node) 
	throws JHDLUnsupportedException, IllegalActionException, 
	       NameDuplicationException {

        // should have one input and one output. Just update the _wireMap
        // TODO: I should use the local to provide some more naming
        // information for the wire.
        Collection c = _graph.inputEdges(node);
        if (c.size() != 1) {
            _error("Unexpected input edges for node " + node);
        }
	Edge inputEdge = (Edge) c.iterator().next();

	// There needs to be a relation for the input (driven by the source)
	// Since this node does not correspond to Hardware, there should
	// be no new relation. Delete relation for Nod
	JHDLIORelation inputRelation = _getOrAddOutputRelation(inputEdge.source());
	_deleteOutputRelation(node);
	_setOutputRelation(node,inputRelation);

    }

    protected void _processConstant(Node node) 
	throws IllegalActionException, NameDuplicationException, JHDLUnsupportedException {
        Object weight = node.getWeight();
        if (weight instanceof IntConstant) {
            int value = ((IntConstant) weight).value;
	    
	    // entity
	    JHDLConstantActor actor = 
		new JHDLConstantActor(_entity, value, 32);
	    _nodeEntityMap.put(node,actor);
	    
	    // relation
	    JHDLIORelation r = _getOrAddOutputRelation(node);
	    actor.output.link(r);

        } else
            _unsupportedOperation(weight);
    }

    protected void _processBinopExpr(Node node)
	throws IllegalActionException, NameDuplicationException, JHDLUnsupportedException {

        // get two Relations coming into this op
	JHDLIORelation r1 = null;
	JHDLIORelation r2 = null;
        for (Iterator ie=_graph.inputEdges(node).iterator();
             ie.hasNext();) {
            Edge e = (Edge) ie.next();
            if (!e.hasWeight())
                _error("Missing weight on Edge " + e);

	    JHDLIORelation o = _getOrAddEdgeSourceRelation(e);

            String op = (String) e.getWeight();
            if (op.equals("op1")) {
                r1 = o;
            } else if (op.equals("op2")) {
                r2 = o;
            } else
                _error("Bad edge weight on Edge " + e);
        }
        if (r1==null || r2==null)
            throw new JHDLUnsupportedException("Missing input wire");

        Object nweight = node.getWeight();
	JHDLBinOpActor actor = null;
        if (nweight instanceof AddExpr) {
	    actor = new JHDLBinOpActor(_entity,JHDLBinOpActor.ADD);
        } else if (nweight instanceof SubExpr) {
	    actor = new JHDLBinOpActor(_entity,JHDLBinOpActor.SUB);
        } else if (nweight instanceof AndExpr) {
	    actor = new JHDLBinOpActor(_entity,JHDLBinOpActor.AND);
        } else if (nweight instanceof OrExpr) {
	    actor = new JHDLBinOpActor(_entity,JHDLBinOpActor.OR);
        } else if (nweight instanceof XorExpr) {
	    actor = new JHDLBinOpActor(_entity,JHDLBinOpActor.XOR);
        } else if (nweight instanceof ShlExpr) {
            _unsupportedOperation(nweight);
        } else if (nweight instanceof ShrExpr) {
            _unsupportedOperation(nweight);
        } else if (nweight instanceof UshrExpr) {
            _unsupportedOperation(nweight);
        } else if (nweight instanceof RemExpr) {
            _unsupportedOperation(nweight);
        } else if (nweight instanceof DivExpr) {
            _unsupportedOperation(nweight);
        } else if (nweight instanceof MulExpr) {
	    actor = new JHDLBinOpActor(_entity,JHDLBinOpActor.MULT);
        } else if (nweight instanceof ConditionExpr) {
	    actor = new JHDLBinOpActor(_entity,JHDLBinOpActor.CONDITION);
        } else
            _unsupportedOperation(nweight);

	// resolve connections
	_nodeEntityMap.put(node,actor);
	actor.input1.link(r1);
	actor.input2.link(r2);
	// Output relation
	JHDLIORelation r = _getOrAddOutputRelation(node);
	actor.output.link(r);
    }

    protected void _processUnopExpr(Node node)
	throws IllegalActionException, NameDuplicationException, JHDLUnsupportedException {
        // get nodes coming into this op
        Collection inEdges = _graph.inputEdges(node);
        if (inEdges.size() != 1)
            _error("Illegal number of edges on unop node");
        Edge e = (Edge) inEdges.iterator().next();
	JHDLIORelation r1 = _getOrAddEdgeSourceRelation(e);

        Object nweight = node.getWeight();

	JHDLUnOpActor actor = null;
        if (nweight instanceof BooleanNotExpr) {
	    actor = new JHDLUnOpActor(_entity,JHDLUnOpActor.NOT);
        } else
            _unsupportedOperation(nweight);

	actor.input.link(r1);
	JHDLIORelation r = _getOrAddOutputRelation(node);
	actor.output.link(r);
    }

    protected void _processBinaryMux(Node node)
            throws JHDLUnsupportedException {

//          BinaryMux w = (BinaryMux) node.getWeight();

//          Wire condition = _getWire(w.getConditionNode(_sdfg,node));
//          Wire trueNode = _getWire(w.getTrueNode(_sdfg,node));
//          Wire falseNode = _getWire(w.getFalseNode(_sdfg,node));

//          Wire output = _cell.mux(falseNode,trueNode,condition);
//          _wireMap.put(node,output);

    }

    protected JHDLIORelation _getOutputRelation(Node n) {
	Object o = _nodeRelationMap.get(n);
	if (o == null)
	    return null;
	return (JHDLIORelation) o;
    }

    protected void _deleteOutputRelation(Node n) throws IllegalActionException, NameDuplicationException {
	JHDLIORelation r = _getOutputRelation(n);
	if (r == null)
	    return;
	// Delete relation
	r.setContainer(null);
    }

    // Find Relation associated with Node driving this edge
    protected JHDLIORelation _getOrAddEdgeSourceRelation(Edge e) throws JHDLUnsupportedException {
	Node source = e.source();
	return _getOrAddOutputRelation(source);
    }

    // Find Relation associated with this Node (i.e. Node drives relation)
    protected JHDLIORelation _getOrAddOutputRelation(Node n) throws JHDLUnsupportedException {
	JHDLIORelation cr  = _getOutputRelation(n);
        if (cr == null) {
	    // create new relation
	    try {
		cr = (JHDLIORelation) _entity.newRelation();
	    } catch (IllegalActionException ex) {
		 _error(ex);
	     }
	     // Set mapping
	     //_setNodeOutputEdgeRelation(source,cr);
	     _setOutputRelation(n,cr);
	 }
	 return cr;
     }

    protected void _setOutputRelation(Node n, JHDLIORelation r) {
	_nodeRelationMap.put(n,r);
    }

    protected static void _error(Exception e)
            throws JHDLUnsupportedException {
        throw new JHDLUnsupportedException(e.toString());
    }

    protected static void _error(String s)
            throws JHDLUnsupportedException {
        throw new JHDLUnsupportedException(s);
    }

    protected static void _unsupportedOperation(Object o)
            throws JHDLUnsupportedException {
        throw new JHDLUnsupportedException("Object "+
                o.getClass().getName()+
                " not supported");
    }

    public static ActorPortDirectedGraph createGraph(AtomicActor entity,
						Map options) {
	return new ActorPortDirectedGraph(entity,options);
    }


    public static void main(String args[]) {
        IntervalBlockDirectedGraph im = null;
        try {
            im = IntervalBlockDirectedGraph.createIntervalBlockDirectedGraph(args,true);
        } catch (JHDLUnsupportedException e) {
            e.printStackTrace();
            System.exit(1);
        }
	PortDirectedGraph g = new IntervalParameterPortDirectedGraph(im);
	PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
	dgToDotty.writeDotFile(".", "port",g);
	ModelGraph2Entity.DEBUG = true;

	ModelGraph2Entity process = null;
	JHDLActorTestbench testbench = null;
	int descriptionLevel = 
	    NamedObj.LINKS | 
	    NamedObj.FULLNAME |
	    //NamedObj.ATTRIBUTES |
	    NamedObj.CONTENTS |
	    NamedObj.DEEP;
	try {
	    process = new ModelGraph2Entity(g,"e0");
	    boolean resolve = process.getEntity().resolve();
	    JHDLCompositeActor _entity = process.getEntity();
	    System.out.println("Created Circuit");
	    //System.out.println(testbench.description(description));
	    System.out.println(_entity.description(descriptionLevel));
	    //System.out.println(_entity.toDot());
	    if (resolve)
		testbench = new JHDLActorTestbench(process.getEntity());
	    else {
		System.err.println("Failed to resolve");
		System.exit(1);
	    }
	} catch (IllegalActionException e) {
            e.printStackTrace();
            System.exit(1);
	} catch (NameDuplicationException e) {
            e.printStackTrace();
            System.exit(1);
	}
	    

	HWSystem hw = new HWSystem();
	testbench.build(hw);
	//hw.cycle(2);

	//new byucc.jhdl.apps.Viewers.JL.CLIJL(testbench.getTestbench());
      	java.awt.Frame f = new SmartSchematicFrame(testbench.getTestbench());
      	f.addWindowListener(new java.awt.event.WindowAdapter() {
      		public void windowClosing(java.awt.event.WindowEvent e) {
      		    System.exit(0);
      		}
      	    });
    }

    public JHDLCompositeActor getEntity() { return _entity; }

    public static boolean DEBUG = false;

    protected PortDirectedGraph _graph;
    // Mapping between a ModelGraph Node and an Entity
    protected Map _nodeEntityMap;
    // Mapping between a ModelGraph Edge and a Relation
    protected Map _nodeRelationMap;

    protected JHDLCompositeActor _entity;

}

class IntervalParameterPortDirectedGraph extends PortDirectedGraph {
    
    public IntervalParameterPortDirectedGraph(IntervalBlockDirectedGraph g) {
	super();
	addGraph(g);

	Map inputNodeSignalMap = new HashMap();
	Node returnNode = null;
	for (Iterator i = nodes().iterator(); i.hasNext();) {
	    Node n = (Node) i.next();
	    Object weight = n.getWeight();
	    if (weight == null)
		continue;

	    // Search for parameter Nodes
	    if (weight instanceof ParameterRef) {
		ParameterRef pr = (ParameterRef) n.getWeight();
		Type t = pr.getType();
		int bits=0;
		if (t instanceof IntType)
		    bits = 32;
		else if (t instanceof BooleanType)
		    bits = 1;
		else
		    bits = Signal.UNRESOLVED;
		int index = pr.getIndex();
		Signal s = new SimpleSignal("p"+index,bits);
		inputNodeSignalMap.put(n,s);
	    } else if (weight instanceof ReturnStmt) {
		returnNode = n;
	    }
	}
	// fix input nodes
	for (Iterator i = inputNodeSignalMap.keySet().iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    Node successor = (Node) successors(n).iterator().next(); 
	    removeNode(n);
	    Node port = addInputPortNode((Signal)inputNodeSignalMap.get(n));
	    addEdge(port,successor);
	}
	// fix output node
	Node pred = (Node) predecessors(returnNode).iterator().next();
	removeNode(returnNode);
	Node outport = addOutputPortNode(new SimpleSignal("output"));
	addEdge(pred,outport);
	
	System.out.println("return op="+ ((ReturnStmt)returnNode.getWeight()).getOp());

	// 
	removeUnreachable();
    }

}

