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

import ptolemy.copernicus.jhdl.soot.*;

import byucc.jhdl.base.HWSystem;
import byucc.jhdl.base.Cell;
import byucc.jhdl.base.Wire;
import byucc.jhdl.base.Util;
import byucc.jhdl.Logic.Logic;
import byucc.jhdl.apps.Viewers.Schematic.SmartSchematicFrame;
import byucc.jhdl.Logic.Modules.arrayMult;

import java.util.*;

import ptolemy.copernicus.jhdl.util.*;

import ptolemy.copernicus.jhdl.soot.*;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.Node;
import ptolemy.graph.Edge;

import soot.jimple.*;
import soot.*;

//////////////////////////////////////////////////////////////////////////
//// CircuitCreator
/**

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class SDFGToJHDLCircuit {

    public SDFGToJHDLCircuit(SootBlockDirectedGraph sdfg,
			     JHDLTestbench parent)
	throws JHDLUnsupportedException {
	_cell = new Logic(parent,"newcell");
	_jtb = parent;
	_sdfg = sdfg;
	_processDFG();
    }

    protected void _processDFG() throws JHDLUnsupportedException {

	ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile("orig",_sdfg);
	Collection outputNodes = _determineOutputNodes();
	_trimNonReachableNodes(outputNodes);
	ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile("reachable",_sdfg);

	// Generate a topological order of nodes in this graph
	// (this is a single basic block, so it should be acyclic)
	Collection c;
	try {
	    c = _sdfg.topologicalSort(_sdfg.nodes());
	} catch (IllegalActionException e) {
	    throw new JHDLUnsupportedException(e.getMessage());
	}

	// create a hashmap between nodes (key) and wires (value)
	_wireMap = new HashMap(_sdfg.nodeCount());

	// sequence through the nodes in the graph in topological
	// order.
	for (Iterator i=c.iterator();i.hasNext();) {
	    _processNode((Node) i.next());
	}

    }

    /**
     * This method will return a Collection of Node objects that represent
     * output Wires.
     *
     * The default behavior is to find the Node associated with the
     * return statement.
     **/
    protected Collection _determineOutputNodes()
	throws JHDLUnsupportedException {
	Vector v = new Vector(1);
	// Find return statement node
	Node returnNode = null;
	for (Iterator i=_sdfg.nodes().iterator();
	     i.hasNext() && (returnNode == null);) {
	    Node n = (Node) i.next();
	    if (n.getWeight() instanceof ReturnStmt)
		returnNode = n;
	}
	v.add(returnNode);
	if (returnNode==null)
	    throw new JHDLUnsupportedException("No return statement in this basic block");
	return v;
    }

    protected void _trimNonReachableNodes(Collection outputNodes) {

	Collection keepers = new Vector(_sdfg.nodeCount());
	for (Iterator i=outputNodes.iterator();i.hasNext();) {
	    Node output = (Node) i.next();
	    Collection k=_sdfg.backwardReachableNodes(output);
	    keepers.addAll(k);
	    keepers.add(output);
	}
	Vector removeNodes=new Vector(_sdfg.nodeCount());
	for (Iterator i=_sdfg.nodes().iterator();i.hasNext();) {
	    Node n = (Node) i.next();
	    if (!keepers.contains(n))
		removeNodes.add(n);
	}
	for (Iterator i=removeNodes.iterator();i.hasNext();)
	    _sdfg.removeNode( (Node) i.next() );
    }

    /**
     * This method will generate hardware for the given Node.
     **/
    protected void _processNode(Node node) throws JHDLUnsupportedException {

	// Process Node
	Object nweight = node.getWeight();
	if (DEBUG) System.out.println("Node "+nweight+" ("+
				      nweight.getClass().getName()+")");

	if (nweight instanceof ParameterRef) {
	    _processParameterRef(node);
	} else if (nweight instanceof Local) {
	    _processLocal(node);
	} else if (nweight instanceof Constant) {
	    _processConstant(node);
	} else if (nweight instanceof BinopExpr) {
	    _processBinopExpr(node);
	} else if (nweight instanceof UnopExpr) {
	    _processUnopExpr(node);
	} else if (nweight instanceof ReturnStmt) {
	    _processReturnStmt(node);
	} else if (nweight instanceof BinaryMux) {
	    _processBinaryMux(node);
	} else {
	    _unsupportedOperation(nweight);
	}
    }

    protected void _processParameterRef(Node node)
	throws JHDLUnsupportedException {
	ParameterRef pr = (ParameterRef) node.getWeight();
	Type t = pr.getType();
	// Currently only support IntType
	int bits=0;
	if (t instanceof IntType)
	    bits = 32;
	else if (t instanceof BooleanType)
	    bits = 1;
	else
	    _unsupportedOperation(t);
	int index = pr.getIndex();
	// Create wire for parameter
	Wire param = createPrimaryInput(new String("i"+index),bits);
	_wireMap.put(node,param);
    }

    protected void _processLocal(Node node) throws JHDLUnsupportedException {

	// should have one input and one output. Just update the _wireMap
	// TODO: I should use the local to provide some more naming
	// information for the wire.
	Collection c = _sdfg.inputEdges(node);
	if (c.size() != 1) {
	    _error("Unexpected input edges for node " + node);
	}
	Edge e = (Edge) c.iterator().next();
	Wire w = _getWire(e.source());
	_wireMap.put(node,w);
    }

    protected void _processConstant(Node node) throws JHDLUnsupportedException {

	Object weight = node.getWeight();
	if (weight instanceof IntConstant) {
	    int value = ((IntConstant) weight).value;
	    Wire w = _cell.constant(32,value);
	    _wireMap.put(node,w);
	} else
	    _unsupportedOperation(weight);
    }

    protected void _processBinopExpr(Node node)
	throws JHDLUnsupportedException {

	// get two nodes coming into this op
	Wire wire1=null;
	Wire wire2=null;
	for (Iterator ie=_sdfg.inputEdges(node).iterator();
	     ie.hasNext();) {
	    Edge e = (Edge) ie.next();
	    if (!e.hasWeight())
		_error("Missing weight on Edge " + e);
	    Node source = e.source();
	    Wire o = _getWire(source);

	    String op = (String) e.getWeight();
	    if (op.equals("op1")) {
		wire1 = o;
	    } else if (op.equals("op2")) {
		wire2 = o;
	    } else
		_error("Bad edge weight on Edge " + e);
	}
	if (wire1==null || wire2==null)
	    throw new JHDLUnsupportedException("Missing input wire");

	Wire output=null;
	Object nweight = node.getWeight();
	if (nweight instanceof AddExpr) {
	    // create adder
	    output = _createAdder(wire1,wire2);
	} else if (nweight instanceof SubExpr) {
	    // create subtractor
	    output = _createSubtractor(wire1,wire2);
	} else if (nweight instanceof AndExpr) {
	    output = _createAnd(wire1,wire2);
	} else if (nweight instanceof OrExpr) {
	    output = _createOr(wire1,wire2);
	} else if (nweight instanceof XorExpr) {
	    output = _createXor(wire1,wire2);
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
	    output = _createMultiplier(wire1,wire2);
	} else if (nweight instanceof ConditionExpr) {
	    output = _processConditionExpr(node,wire1,wire2);
	} else
	    _unsupportedOperation(nweight);
	_wireMap.put(node,output);
	if (DEBUG)
	    System.out.println("Creating Wire "+output+" for Node "+node+
			       "("+System.identityHashCode(node)+")");

    }

    protected void _processUnopExpr(Node node)
	throws JHDLUnsupportedException {
	// get nodes coming into this op
	Wire wire1=null;
	Collection inEdges = _sdfg.inputEdges(node);
	if (inEdges.size() != 1)
	    _error("Illegal number of edges on unop node");
	Edge e = (Edge) inEdges.iterator().next();
	Node source = e.source();
	wire1 = _getWire(source);
	if (wire1 == null)
	    throw new JHDLUnsupportedException("Missing wire");

	Wire output=null;
	Object nweight = node.getWeight();

	if (nweight instanceof JHDLNotExpr) {
	    output = _createNot(wire1);
	} else
	    _unsupportedOperation(nweight);

	_wireMap.put(node,output);
    }

    protected Wire _processConditionExpr(Node node, Wire wire1,
					 Wire wire2)
	throws JHDLUnsupportedException {

	Object nweight = node.getWeight();
	if (nweight instanceof CompoundBooleanExpression)
	    return _processCompoundBooleanExpression(node,wire1,wire2);
	else
	    return _processSimpleConditionExpr(node,wire1,wire2);
    }

    protected Wire _processCompoundBooleanExpression(Node node, Wire wire1,
						     Wire wire2)
	throws JHDLUnsupportedException {

	Object nweight = node.getWeight();
	if (nweight instanceof CompoundAndExpression) {
	    return _createAnd(wire1,wire2);
	} else if (nweight instanceof CompoundOrExpression) {
	    return _createOr(wire1,wire2);
	} else
	    _unsupportedOperation(nweight);
	return null;
    }

    protected Wire _processSimpleConditionExpr(Node node, Wire wire1,
					       Wire wire2)
	throws JHDLUnsupportedException {

	// TODO: more agressive bitwidth analysis
	int wireSize = wire1.getWidth() > wire2.getWidth() ?
	    wire2.getWidth() : wire1.getWidth();

	Wire newWire1 = wire1.range(wireSize-1,0);
	Wire newWire2 = wire2.range(wireSize-1,0);

	Object weight = node.getWeight();
	Wire conditionTrue = _cell.wire(1);

	// LE, GE, GT, and LT are all performed with an adder
	if (weight instanceof LeExpr ||
	    weight instanceof GeExpr ||
	    weight instanceof GtExpr ||
	    weight instanceof LtExpr) {

	    Wire notB = _cell.not(newWire2);
	    Wire cin = _cell.wire(1);
	    Wire sum = _cell.wire(32);
	    Wire cout = _cell.wire(1);
	    _cell.add_o(newWire1,newWire2,cin,sum,cout);

	    if (weight instanceof LeExpr) {
		// cin = 0, true = not cout
		_cell.gnd_o(cin);
		_cell.not_o(cout,conditionTrue);
	    } else if (weight instanceof GeExpr) {
		// cin = 1, true = cout
		_cell.vcc_o(cin);
		_cell.buf_o(cout,conditionTrue);
	    } else if (weight instanceof LtExpr) {
		// cin = 1, true = not cout
		_cell.vcc_o(cin);
		_cell.not_o(cout,conditionTrue);
	    } else if (weight instanceof GtExpr) {
		// cin = 0, true = cout
		_cell.gnd_o(cin);
		_cell.buf_o(cout,conditionTrue);
	    }

	} else if (weight instanceof EqExpr ||
		   weight instanceof NeExpr) {
	    // Need to build a better EQ and NE module (carry chain?)
	    Wire equal = _cell.nor(_cell.xor(newWire1,newWire2));
	    if (weight instanceof EqExpr)
		_cell.buf_o(equal,conditionTrue);
	    else
		_cell.not_o(equal,conditionTrue);
	} else
	    _unsupportedOperation(weight);
	return conditionTrue;
    }

    protected Wire _createAdder(Wire wire1, Wire wire2 ) {
	return _cell.add(wire1,wire2);
    }

    protected Wire _createSubtractor(Wire wire1, Wire wire2 ) {
	return _cell.sub(wire1,wire2);
    }

    protected Wire _createMultiplier(Wire wire1, Wire wire2 ) {
	Wire allbits = _cell.wire(64);
	new arrayMult(_cell,       // parent
		      wire1,       // x
		      wire2,       // y
		      null,        // clk_en
		      allbits,     // pout
		      true,        // signed
		      0);          // pipedepth
	Wire output = allbits.range(31,0);
	return output;
    }

    protected Wire _createAnd(Wire wire1, Wire wire2 )
	throws JHDLUnsupportedException {
	if (wire1.getWidth() != wire2.getWidth())
	    _error("Mismatched Bits for Binary operation");
	return _cell.and(wire1,wire2);
    }

    protected Wire _createOr(Wire wire1, Wire wire2 )
	throws JHDLUnsupportedException {
	if (wire1.getWidth() != wire2.getWidth())
	    _error("Mismatched Bits for Binary operation");
	return _cell.or(wire1,wire2);
    }

    protected Wire _createXor(Wire wire1, Wire wire2 )
	throws JHDLUnsupportedException {
	if (wire1.getWidth() != wire2.getWidth())
	    _error("Mismatched Bits for Binary operation");
	return _cell.xor(wire1,wire2);
    }

    protected Wire _createNot(Wire wire ) {
	return _cell.not(wire);
    }

    protected void _processReturnStmt(Node node)
	throws JHDLUnsupportedException {
	// Get input wire
	Node predecessor=null;
	for (Iterator ie=_sdfg.inputEdges(node).iterator();
	     ie.hasNext();) {
	    Edge e = (Edge) ie.next();
	    if (predecessor != null)
		_error("More than one predecessor for Node "+node);
	    predecessor = e.source();
	}
	if (predecessor == null)
	    _error("No predecessor for Node "+node);
	Wire outputWire = (Wire) _wireMap.get(predecessor);
	// create a primary output
	createPrimaryOutput(outputWire);
    }

    protected void _processBinaryMux(Node node)
	throws JHDLUnsupportedException {

	BinaryMux w = (BinaryMux) node.getWeight();

	Wire condition = _getWire(w.getConditionNode(_sdfg,node));
	Wire trueNode = _getWire(w.getTrueNode(_sdfg,node));
	Wire falseNode = _getWire(w.getFalseNode(_sdfg,node));

	Wire output = _cell.mux(falseNode,trueNode,condition);
	_wireMap.put(node,output);
    }

    protected Wire _getWire(Node node) throws JHDLUnsupportedException {
	Object o = _wireMap.get(node);
	if (o == null)
	    _error("No wire for Node " + node + " ("+
		   System.identityHashCode(node)+")");
	return (Wire) o;
    }

    public Wire createPrimaryInput(String name, int width) {
	// 1. Create a new port
	_cell.addPort( Cell.in(name,width) );
	// 2. Create a new top-level testbench wire
	Wire input = _jtb.addPrimaryInputWire(name,width);
	// 3. Make connect call
	Wire newwire = _cell.connect(name,input);
	return newwire;
    }

    public void createPrimaryOutput(Wire w) {
	String wire_name = w.getLeafName();
	String w_name = Util.makeLegalJHDLIdentifier(wire_name);
	int w_width = w.getWidth();

	// 1. Create a new port
	_cell.addPort( Cell.out(w_name,w_width) );
	// 2. Create a new top-level testbench wire
	Wire output = _jtb.addPrimaryOutputWire(w_name,w_width);
	// 3. Make connect call
	Wire newwire = _cell.connect(w_name,output);
	// 4. buf_o
	_cell.buf_o(w,newwire);
    }

    public JHDLTestbench getTestbench() {
	return _jtb;
    }
    public Cell getCell() {
	return _cell;
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


    public static void main(String args[]) {


	// Print out graphs from original method
	SootBlockDirectedGraph graphs[] =
	    ControlSootDFGBuilder.createDataFlowGraphs(args,true);

	// Set debug flags
	//ValueMap.DEBUG = true;
	//IntervalBlockDirectedGraph.DEBUG = true;
	DEBUG = true;

	SootBlockDirectedGraph sbdg=null;
	try {
	    sbdg = IntervalBlockDirectedGraph.createIntervalBlockDirectedGraph(args,true);
	} catch (JHDLUnsupportedException e) {
	    e.printStackTrace();
	    System.err.println(e);
	    System.exit(1);
	}
	HWSystem hw = new HWSystem();
	JHDLTestbench jtb = new JHDLTestbench(hw);
	try {
	    SDFGToJHDLCircuit c = new SDFGToJHDLCircuit(sbdg,jtb);
	    java.awt.Frame f = new SmartSchematicFrame(c.getCell());
	    f.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent e) {
			System.exit(0);
		    }
		});
	} catch (JHDLUnsupportedException e) {
	    e.printStackTrace();
	    System.err.println(e);
	    System.exit(1);
	}

    }

    public static boolean DEBUG = false;

    protected Logic _cell;
    protected SootBlockDirectedGraph _sdfg;
    protected Map _wireMap;
    protected JHDLTestbench _jtb;
}
