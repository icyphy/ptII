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

import byucc.jhdl.base.HWSystem;
import byucc.jhdl.base.Cell;
import byucc.jhdl.base.Wire;
import byucc.jhdl.base.Util;
import byucc.jhdl.Logic.Logic;
import byucc.jhdl.apps.Viewers.Schematic.SmartSchematicFrame;
import byucc.jhdl.Logic.Modules.arrayMult;

import java.util.Collection;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

import ptolemy.copernicus.jhdl.util.JHDLUnsupportedException;
import ptolemy.copernicus.jhdl.util.JHDLTestbench;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.Node;
import ptolemy.graph.Edge;

import soot.jimple.ReturnStmt;
import soot.jimple.ParameterRef;
import soot.jimple.BinopExpr;
import soot.jimple.AddExpr;
import soot.jimple.SubExpr;
import soot.jimple.AndExpr;
import soot.jimple.OrExpr;
import soot.jimple.XorExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.UshrExpr;
import soot.jimple.RemExpr;
import soot.jimple.DivExpr;
import soot.jimple.MulExpr;
import soot.toolkits.graph.Block;
import soot.Value;
import soot.Type;
import soot.IntType;

//////////////////////////////////////////////////////////////////////////
//// CircuitCreator
/**

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class CircuitGenerator {

    public CircuitGenerator() {

	// Create a new hardware system for this circuit
	_hw = new HWSystem();

	// Create a new testbench for this circuit
	_jtb = new JHDLTestbench(_hw);

	// Create a new cell
	_cell = new Logic(_jtb,"newcell");

    }

    public void firstBasicBlockHardware(Block block) 
	throws JHDLUnsupportedException {
	
	BlockDataFlowGraph bdfg = new BlockDataFlowGraph(block);
	ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile("orig",bdfg);

	// remove all local nodes
  	bdfg.removeLocalNodes();
	ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile("trimmed",bdfg);
	
	// Find return statement node
	Node returnNode = null; 
	for (Iterator i=bdfg.nodes().iterator();
	     i.hasNext() && (returnNode == null);) {
	    Node n = (Node) i.next();
	    if (n.weight() instanceof ReturnStmt)
		returnNode = n;
	}
	if (returnNode==null)
	    throw new JHDLUnsupportedException("No return statement in this basic block");

	// remove all nodes that don't lead to return statement
	Collection d=bdfg.backwardReachableNodes(returnNode);
	d.add(returnNode);
	ArrayList removeNodes=new ArrayList(d.size());
	for (Iterator i=bdfg.nodes().iterator();
	     i.hasNext();) {
	    Node n = (Node) i.next();
	    if (!d.contains(n))
		removeNodes.add(n);
	}
	for (Iterator i=removeNodes.iterator();i.hasNext();)
	    bdfg.removeNode( (Node) i.next() );
	ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty.writeDotFile("reachable",bdfg);

	// Generate a topological order of nodes in this graph
	// (this is a single basic block, so it should be acyclic)
	Collection c;
	try {
	    c = bdfg.attemptTopologicalSort(bdfg.nodes());
	} catch (IllegalActionException e) {
	    throw new JHDLUnsupportedException(e.getMessage());
	}
	
	// create a hashmap between nodes (key) and wires (value)
	HashMap wires = new HashMap(bdfg.nodeCount());
	
	// sequence through the nodes in the graph in topological
	// order.
	for (Iterator i=c.iterator();i.hasNext();) {
	    // current node
	    Node node = (Node) i.next();
	    // obtain input edges for the given node
	    Collection inEdges = bdfg.inputEdges(node);

	    // Process Node
	    Object nweight = node.weight();
	    System.out.println("Node="+nweight.getClass().getName());

	    if (nweight instanceof ParameterRef) {
		ParameterRef pr = (ParameterRef) nweight;
		Type t = pr.getType();
		// Currently only support IntType
		if (!(t instanceof IntType))
		    _unsupportedOperation(t);
		int index = pr.getIndex();
		// Create wire for parameter
		Wire param = createPrimaryInput(new String("i"+index),32);
		wires.put(node,param);
	    } else if (nweight instanceof BinopExpr) {
		// get two nodes coming into this op
		Wire wire1=null;
		Wire wire2=null;
		for (Iterator ie=bdfg.inputEdges(node).iterator();
		     ie.hasNext();) {
		    Edge e = (Edge) ie.next();		    
		    if (!e.hasWeight())
			_error("Missing weight on Edge " + e);
		    Node source = e.source();
		    Object o = wires.get(source);
		    if (o == null)
			_error("No wire for Edge " + e + " corresponding to "+
			       source);
		    
		    String op = (String) e.weight();
		    if (op.equals("op1")) {
			wire1 = (Wire) o;
		    } else if (op.equals("op2")) {
			wire2 = (Wire) o;
		    } else
			_error("Bad edge weight on Edge " + e);
		}
		if (wire1==null || wire2==null)
		    throw new JHDLUnsupportedException("Missing input wire");
 
		Wire output=null;
		if (nweight instanceof AddExpr) {
		    // create adder
		    output = _cell.add(wire1,wire2);
		} else if (nweight instanceof SubExpr) {
		    // create subtractor
		    output = _cell.sub(wire1,wire2);
		} else if (nweight instanceof AndExpr) {
		    _unsupportedOperation(nweight);
		} else if (nweight instanceof OrExpr) {
		    _unsupportedOperation(nweight);
		} else if (nweight instanceof XorExpr) {
		    _unsupportedOperation(nweight);

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
		    Wire allbits = _cell.wire(64);
		    new arrayMult(_cell,       // parent
				  wire1,       // x
				  wire2,       // y
				  null,        // clk_en
				  allbits,     // pout
				  true,        // signed
				  0);          // pipedepth
		    output = allbits.range(31,0);
		} else
		    _unsupportedOperation(nweight);
		wires.put(node,output);
	    } else if (nweight instanceof ReturnStmt) {
		// Get input wire
		Node predecessor=null;
		for (Iterator ie=bdfg.inputEdges(node).iterator();
		     ie.hasNext();) {
		    Edge e = (Edge) ie.next();
		    if (predecessor != null)
			_error("More than one predecessor for Node "+node);
		    predecessor = e.source();
		}
		if (predecessor == null)
		    _error("No predecessor for Node "+node);
		Wire outputWire = (Wire) wires.get(predecessor);
		// create a primary output
		createPrimaryOutput(outputWire);
	    } else {
		_unsupportedOperation(nweight);
	    }
	}

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
	if (args.length < 2) {
	    System.err.println("<classname> <methodname>");
	    System.exit(1);
	}
	String classname = args[0];
	String methodname = args[1];
	
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
	soot.toolkits.graph.BriefBlockGraph bbgraph = 
	    new soot.toolkits.graph.BriefBlockGraph(body);
	Block firstBlock = (Block) bbgraph.getBlocks().get(0);
	System.out.println(firstBlock);

	CircuitGenerator cg = new CircuitGenerator();
	try {
	    cg.firstBasicBlockHardware(firstBlock);
	} catch (JHDLUnsupportedException e) {
	    System.err.println(e);
	    System.exit(1);
	}

	JHDLTestbench c = cg.getTestbench();
    	new SmartSchematicFrame(cg.getCell());

   }
    
    protected HWSystem _hw;
    protected JHDLTestbench _jtb;
    protected Logic _cell;

}
