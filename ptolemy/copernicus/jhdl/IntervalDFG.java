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

import java.util.Collection;
import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import soot.Value;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.UnitBox;
import soot.ValueBox;

import soot.jimple.InstanceFieldRef;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.internal.JimpleLocal;

import soot.toolkits.graph.Block;

import ptolemy.copernicus.jhdl.util.JHDLUnsupportedException;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.kernel.util.IllegalActionException;

import ptolemy.graph.Node;
import ptolemy.graph.Edge;

/**
 * The graph that is manipuated is the graph associated with
 * the root.
 **/
public class IntervalDFG extends BlockDataFlowGraph {

    public IntervalDFG(IntervalChain ic) throws JHDLUnsupportedException {
	this(ic,null);
    }

    /**
     * This constructor will create a DFG from the given IntervalChain.
     * The constructor will also merge all of the children into a single
     * combined DFG.
     *
     * @param requiredDefinitions This is a Collection of Value objects
     * that must be defined for IntervalDFGs further in the tree. This
     * list of definitions is used to decide which assignment statements
     * (i.e. definitions) must be made available for subsequent blocks.
     **/
    public IntervalDFG(IntervalChain ic, Collection requiredDefinitions) 
	throws JHDLUnsupportedException {

	// Create DFG from the Block object associated with the root
	// Node of the given IntervalChain
	super((Block) ic.getRoot().weight());
	_ic = ic;
	
	// 1. Create graph for next interval
	// 2. Deterimine defintions for this Node
	// 3. Merge children of fork Nodes
	// 4. Update definitions of this Node
	// 5. Merge next interval into this graph

	// 1. Create graph for next inverval in chain if there is one.
	//    (This algorithm works bottom up so that all required
	//     definitions downstream are known before upstream
	//     DFGs are constructed).
	//    Any definitions required at this Node should be required
	//    for any Nodes further down the chain.
	IntervalChain nextChain = _ic.getNext();
	if (nextChain != null)
	    _next = new IntervalDFG(nextChain,requiredDefinitions);
	else
	    _next = null;

	// 2. Determine the required definitions for the children
	//    of this Node. 
	//    The required definitions for the children are initialized
	//    with the requiredDefinitions passed in AND by
	//    the definitions required by the next Interval in the 
	//    Chain.
	Collection childrenDefinitions = new UniqueVector();
	if (_next == null) 
	    childrenDefinitions.addAll(requiredDefinitions);
	else
	    childrenDefinitions.addAll(_next.getRequiredDefinitions());

	_requiredDefinitions = new UniqueVector();
	_requiredDefinitions.addAll(childrenDefinitions);

	// 3. Create DFGs for each of the children associated with a merge
	//    and merge the DFG with this DFG.

	// A simple Fork/Join construct. Create DFGs for children and
	// merge with parent.
	if (_ic.isSimpleMerge()) {

	    // - Create a DFG for each child associated with this fork.
	    // - Update the required definitions for this Node based on
	    //   definitions required by each child. 
	    // - Merge each DFG in a serial fashion (children DFGs are
	    //   not multiplexed here).
	    
	    // key=IntervalChain root Node, Value=IntervalChain
	    Map children = _ic.getChildren();
	    // key=Value, Value=Node
	    Map rootLvalues = getCurrentLValues();
	    // key=IntervalDFG, Value=child LValues (key=value,value=Node)
	    HashMap childrenLValues = new HashMap(children.size());

	    for (Iterator i = children.values().iterator();i.hasNext();) {
		IntervalChain childInterval = (IntervalChain) i.next();
		IntervalDFG childDFG = 
		    new IntervalDFG(childInterval,childrenDefinitions);
		Map childLValues = childDFG.getCurrentLValues();
		_requiredDefinitions.addAll(childDFG.getRequiredDefinitions());
		mergeSerial(childDFG,rootLvalues);
		childrenLValues.put(childDFG,childLValues);
	    }

	    if (children.values().size() == 1) {
		// Only one branch. Merge the root graph with
		// the branch graph.

		Iterator i = childrenLValues.keySet().iterator();
		IntervalDFG child = (IntervalDFG) i.next();
		Map childLValues = (Map) childrenLValues.get(child);

		// Iterate over each defined value and create a merge
		// node to resolve the between the root DFG and the 
		// child DFG.
		for (i=childLValues.keySet().iterator();i.hasNext();) {
		    Value origv = (Value) i.next();
		    if (isValueDefined(origv)) {
			Node childn = (Node) childLValues.get(origv);
  			_multiplexWithOneChild(child,childn);
		    }
		}
	    } else if (children.values().size() == 2) {
		// Two branches. Merge all of their outputs.		
		// Obtain the defintions defined by both children
		Iterator i = childrenLValues.keySet().iterator();
		IntervalDFG child1 = (IntervalDFG) i.next();
		IntervalDFG child2 = (IntervalDFG) i.next();
		Map child1LValues = (Map) childrenLValues.get(child1);
		Map child2LValues = (Map) childrenLValues.get(child2);
		// Iterate through all of child1Values
		for (i=child1LValues.keySet().iterator();i.hasNext();) {
		    Value origv = (Value) i.next();
		    if (isValueDefined(origv)) {
			Node child1n = (Node) child1LValues.get(origv);
			if (child2LValues.containsKey(origv)) {
			    // merge two children
			    Node child2n = (Node) child2LValues.get(origv);
			    _multiplexTwoChildren(child1,child2,
						  child1n,child2n);
			} else {
			    // merge child1 with root			
			    _multiplexWithOneChild(child1,child1n);
			}
		    }
		}
		// Iterate through all of child2Values
		for (i=child2LValues.keySet().iterator();i.hasNext();) {
		    Value origv = (Value) i.next();
		    if (isValueDefined(origv)) {
			Node child2n = (Node) child2LValues.get(origv);
			_multiplexWithOneChild(child2,child2n);
		    }
		}
 	    } else {
		// A switch.
		throw new JHDLUnsupportedException("Switches not yet supported");
	    }
	}
	if (_ic.isSpecialMerge()) {
	    throw new JHDLUnsupportedException("Special Nodes not yet supported");
	}	

	// add required definitions of this node
	Vector newDefs = new Vector();
	for (Iterator i=getLocalInputDefinitions().iterator();i.hasNext();)
	    newDefs.add( ((Node) i.next()).weight() );
	for (Iterator i=getInstanceFieldRefInputDefinitions().iterator();i.hasNext();)
	    newDefs.add( ((Node) i.next()).weight() );
	_requiredDefinitions.addAll(newDefs);

	System.out.print(_ic.toShortString()+" defs=");
	for (Iterator i=_requiredDefinitions.iterator();i.hasNext();) {
	    System.out.print(i.next()+" ");
	}
	System.out.println();

	// Connect previously created chain to this node
	if (_next != null)
	    mergeSerial(_next);
	
    }

    public IntervalChain getIntervalChain() { return _ic; }

    public boolean isValueDefined(Value v) {
	if (_requiredDefinitions.contains(v))
	    return true;	
	if (v instanceof InstanceFieldRef) {	    
	    InstanceFieldRef d = getMatchingIFR((InstanceFieldRef)v,
						_requiredDefinitions);
	    if (d != null)
		return true;
	}
	return false;
    }

    public Collection getRequiredDefinitions() {
	return _requiredDefinitions;
    }

    public String toBriefString() {
	return _ic.toShortString();
    }


    /**
     * This method will merge the provided DFG into the current DFG. The
     * inputs Nodes of the provided DFG will be driven by Nodes defined
     * in this DFG (i.e. this preceedes im).
     *
     * @return This method returns a HashMap that lists all new definitions
     * (i.e. lvalues) created by the child DFG. The key of the HashMap
     * is a Value object as found in the original list of Units (i.e. the
     * lvalue in the AssignStmt) and the value of the HashMap is the
     * new Value object that was created for this new definition. This
     * HashMap is used to determine which Nodes need to be multiplexed
     * for a join.
     **/
    protected HashMap _mergeSerial(IntervalDFG im) 
	throws JHDLUnsupportedException {

	// temporary hashmap between old nodes & new. 
	// Used when connecting edges
	HashMap newNodes = new HashMap(); 

	// key = Value object in top-level graph, 
	// Value = new Value object in lower graph.
	HashMap newDefinitions = new HashMap();

	// Iterate through all nodes and add to graph
	for (Iterator i = im.nodes().iterator(); i.hasNext();) {

	    Node node = (Node) i.next();	    
	    Node new_node = null;
	    Object weight = node.weight(); // all Nodes should have a weight

	    Collection inEdges = im.inputEdges(node);

	    if (inEdges.size() == 0) {
		// If the given Node has no input edges, it represents
		// a reference that must be defined by a preceeding
		// Node in the graph. If a Node with this Value exists
		// in the parent DFG (i.e. this), use the existing
		// Node. If not, create a new Node in the graph.
		new_node = _getNode(weight);
		newNodes.put(node,new_node);
	    } else {
		// This Node has at least one input Edge. This represents
		// one of two cases:
		// - an lvalue Node (i.e. the left side of an assignment
		//   statement)
		// - a regular operation (non lvalue).
		// Each case is treated differently.
		// 

		if (!im.isLValue(node)) {
		    // This is a regular operation Node that must be copied
		    // into this DFG. Create a new Node using the 
		    // same weight
		    new_node = _getNode(weight); //addNode(new Node(weight));
		    newNodes.put(node,new_node);
		} else {
		    		    
		    // LValue Node. There are two types of lvalue Nodes
		    // in the graph:
		    // - lvalues that are required by downstream DFGs
		    // - lvalues that are not required by downstream DFGs
		    // 
		    // If the lvalue is required by a downstream DFG,
		    // a copy of the child version must be created so
		    // that the correct value can be multiplexed.
		    if (_requiredDefinitions.contains(weight)) {
			// Copy Value
			Value newValue=_createNewValue(im,node);
			new_node = addNode(new Node(newValue));
			newNodes.put(node,new_node);
			// Creates a new definition of lvalue.
			newDefinitions.put(weight,newValue);
		    } else {
			// lvalue is not required downstream, create a new
			// Node using the origional Value object.
			new_node = addNode(new Node(weight));
			newNodes.put(node,new_node);
		    }
		}
	    } 
	}

	// Iterate through all edges and add to graph
	for (Iterator i = im.edges().iterator(); i.hasNext();) {
	    Edge e = (Edge) i.next();
	    Node src = e.source();
	    Node snk = e.sink();
	    Node new_src = (Node)newNodes.get(src);
	    Node new_snk = (Node)newNodes.get(snk);
	    if (e.hasWeight())
		addEdge(new_src,new_snk,e.weight());
	    else
		addEdge(new_src,new_snk);
	}
	return newDefinitions;
    }

    /**
     * This method will attempt to obtain the Node in the current
     * graph that has the same weight as the passed Value object. 
     * If no Node exists the the given weight, a new Node with that
     * weight is created and added to the graph.
     *
     *
     **/
    protected Node _getNode(Object v) {

	Node newNode=null;
//  	if (b instanceof InstanceFieldRef) {
//  	} else {
	    // If the Node does not exist, create one and return it.
	    if (!containsNodeWeight(v)) {
		newNode = addNode(new Node(v));
		return newNode;
	    }

	    // Obtain a collection of all Nodes with the given weight
	    
	    Collection nodes = nodes(v);
	    // If there is only one Node in the graph with the given weight,
	    // return it.
	    if (nodes.size() == 1) {
		newNode = (Node) nodes.iterator().next();
		return newNode;
	    }
	    // There is more than one Node in the graph with the given
	    // weight. Find the one that is preceeded by the others
	    // (they should all be connected).
	    for (Iterator i=nodes.iterator();i.hasNext();) {
		Node n = (Node) i.next();
		if (newNode == null)
		    newNode = n;	    
		else {
		    // determine all the nodes that are reachable from newNode
		    Collection rn = reachableNodes(newNode);
		    // if n is reachable from newNode, set n as newNode
		    if (rn.contains(n))
			newNode = n;
		}
	    }	    
//  	}

	return newNode;
    }

    /*
    protected Node _getOrigValueNode(Value origValue) {
	blah;
	Node origNode=null;

	if (!containsNodeWeight(origValue)) {
	    origNode = addNode(new Node(origValue));
	}
	else {
	    // Get Node (find one that is last in the list)
	    Collection nodes = nodes(origValue);
	    if (nodes.size() == 1)
		origNode = (Node) nodes.iterator().next();
	    else {
		for (Iterator i=nodes.iterator();i.hasNext();) {
		    Node n = (Node) i.next();
		    if (origNode == null)
			origNode = n;
		    else if (_sortedNodes.indexOf(n) > 
			     _sortedNodes.indexOf(origNode))
			origNode = n;
		}
	    }
	}
	return origNode;
    }
    */

    /**
     * This method will merge (i.e. multiplex) two Values for the case
     * when one Value is defined by "this" DFG and the other Value is
     * defined by one and only one child DFG.
     * Note that this method may need to create a new Node with
     * weight origValue in the root DFG if such a Node does not exist.
     *
     * @param child The DFG of the child
     * @param origValue The Value defined in the root or "this" DFG.
     * @param newValue The conflicting Value defined in a child DFG.
     * @return Returns the new multiplexer Node. 
     **/
    protected Node _multiplexWithOneChild(IntervalDFG child, Node childNode) {
	
	Value value = (Value) childNode.weight();
	Node parentNode = getOrCreateNode(value);

	// Get the edges associated with the original CFG.
	Node cNode = getConditionNode();

	Node trueNode = childNode;
	Node falseNode = parentNode;
	if (!isTrueNode(child)) {
	    trueNode = parentNode;
	    falseNode = childNode;
	}
	
	BinaryMuxNode bmn = new BinaryMuxNode(trueNode,falseNode,cNode,
					      value.toString());
	Node muxNode = addNodeWeight(bmn);

	addEdge(trueNode,muxNode,"true");
	addEdge(falseNode,muxNode,"false");
	addEdge(cNode,muxNode,"condition");

	Node newValueNode = addValueWeightedNode(value);
	addEdge(muxNode,newValueNode);
	return newValueNode;
    }

    protected Node _multiplexTwoChildren(IntervalDFG child1, 
					 IntervalDFG child2,
					 Node child1n,
					 Node child2n) {

	Value value = (Value) child1n.weight(); // both should be same
	
	// Get the edges associated with the original CFG.
	Node cNode = getConditionNode();

	Node trueNode = child1n;
	Node falseNode = child2n;
	if (!isTrueNode(child1)) {
	    trueNode = child2n;
	    falseNode = child1n;
	}

	BinaryMuxNode bmn = new BinaryMuxNode(trueNode,falseNode,cNode,
					      value.toString());
	Node muxNode = addNodeWeight(bmn);

	addEdge(trueNode,muxNode,"true");
	addEdge(falseNode,muxNode,"false");
	addEdge(cNode,muxNode,"condition");

	Node newValueNode = addValueWeightedNode(value);
	addEdge(muxNode,newValueNode);
	return newValueNode;

    }
    
    /** Return the condition Value object associated with a fork Node
     **/ 
    public Node getConditionNode() {
	IfStmt ifs = getIfStmt();
	Value v = ifs.getCondition();
	return node(v);
    }

    public IfStmt getIfStmt() {
	Node root = _ic.getRoot();
	Block b = (Block) root.weight();
	IfStmt ifs = (IfStmt) b.getTail();
	return ifs;
    }

    public boolean isTrueNode(IntervalDFG cidfg) {

	DirectedAcyclicCFG graph = _ic.getGraph();	
	IfStmt ifs = getIfStmt();

	Node childCFGNode = cidfg._ic.getRoot();

	Block dest = (Block) childCFGNode.weight();

//  	System.out.println("IFstmt="+ifs+" target="+
//  			   ifs.getTargetBox().getUnit()+" dest head="+
//  			   dest.getHead());


	if (ifs.getTargetBox().getUnit() == dest.getHead())
	    return true;
	else
	    return false;
    }

    /**
     * Creates a new Value object from the Value found within the weight
     * of Node n.
     **/
    protected Value _createNewValue(IntervalDFG id, Node n) 
	throws JHDLUnsupportedException {

	// Get original Value
	Value origValue = (Value) n.weight();

	// Copy Value
	Value newValue=null;
	if (origValue instanceof Local) {
	    Local l=(Local) origValue;
	    newValue = new JimpleLocal(l.getName()+"_"+
				       id.toBriefString(),
				       l.getType());
	} else {
	    // TODO: support fieldref values!
	    throw new JHDLUnsupportedException("node for class "+origValue.getClass().getName());
	    
	}
	return newValue;
    }

    public static IntervalDFG createInternalDFG(String args[]) 
	throws JHDLUnsupportedException {
	IntervalChain ic = IntervalChain.createIntervalChain(args,true);
	return new IntervalDFG(ic);
    }

    public static void main(String args[]) {
	IntervalDFG im = null;
	try {
	    im = createInternalDFG(args);	
	} catch (JHDLUnsupportedException e) {
	    e.printStackTrace();
	    System.exit(1);
	}

//  	System.out.println(im);
//  	BlockDataFlowGraph graphs[] = 
//  	    BlockDataFlowGraph.getBlockDataFlowGraphs(args);
//  	for (int i = 0;i<graphs.length;i++)
//  	    PtDirectedGraphToDotty.writeDotFile("bbgraph"+i,
//  						graphs[i]);
	PtDirectedGraphToDotty.writeDotFile("merge",im);
    }

    protected IntervalChain _ic;
    protected IntervalDFG _next;

    /**
     * This contains a Vector of Value objects. A definition for each
     * Value in this list is *required* further up the tree.
     **/
    protected Collection _requiredDefinitions;

}

class MuxNode {
}

class BinaryMuxNode extends MuxNode {
    public BinaryMuxNode(Node t, Node f, Node c, String name) {
	_trueNode = t;
	_falseNode = f;
	_conditionNode = c;
	_name = name;
    }
    public String toString() { return "mux_"+_name; }

    String _name;
    Node _trueNode;
    Node _falseNode;
    Node _conditionNode;
}

class UniqueVector extends Vector {

    public UniqueVector() { super(); }

    public boolean add(Object o) {
	if (!contains(o))
	    super.add(o);
	return true;
    }

    public boolean addAll(Collection c) {
	if (c!= null)
	    for (Iterator i=c.iterator();i.hasNext();)
		add(i.next());
	return true;
    }

}
