/* A Data Flow Graph generated from an IntervalChain

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

package ptolemy.copernicus.jhdl.soot;

import ptolemy.copernicus.jhdl.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import soot.SootField;
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

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.kernel.util.IllegalActionException;

import ptolemy.graph.Node;
import ptolemy.graph.Edge;

//////////////////////////////////////////////////////////////////////////
//// IntervalDFG
/**
 * The graph that is manipuated is the graph associated with
 * the root.
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
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
        super((Block) ic.getRoot().getWeight());
        _ic = ic;

        _processChain(requiredDefinitions);
    }

    protected void _processChain(Collection requiredDefinitions)
            throws JHDLUnsupportedException {

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
            IValueMap rootVM = new IValueMap(_locals,_instanceFieldRefs);

            // key=IntervalDFG, Value=child LValues (key=value,value=Node)
            HashMap childrenIValueMaps = new HashMap(children.size());

            for (Iterator i = children.values().iterator();i.hasNext();) {
                IntervalChain childInterval = (IntervalChain) i.next();
                IntervalDFG childDFG =
                    new IntervalDFG(childInterval,childrenDefinitions);
                _requiredDefinitions.addAll(childDFG.getRequiredDefinitions());

                _valueMap = (IValueMap) rootVM.clone();
                mergeSerial(childDFG);
                childrenIValueMaps.put(childDFG,_valueMap);
            }

            _valueMap = rootVM;

            if (children.values().size() == 1) {
                // Only one branch. Merge the root graph with
                // the branch graph.

                Iterator i = childrenIValueMaps.keySet().iterator();
                IntervalDFG childDFG = (IntervalDFG) i.next();
                IValueMap childMap = (IValueMap) childrenIValueMaps.get(childDFG);

                for (i=childMap.getDefs().keySet().iterator();i.hasNext();) {
                    Value origv = (Value) i.next();
                    Node n = (Node) childMap.getDefs().get(origv);
                    //                      System.out.println("New def="+n+" id="+
                    //                                         System.identityHashCode(n));
                    if (isRequired(origv)) {
                        //                          System.out.println("def needed");
                        Node childn = childMap.getLast(origv);
                        Node parentn = getOrCreateNode(origv);
                        _multiplexNodes(childDFG,childn,parentn);
                        //System.out.println("Multiplexing node "+childn);
                    }
                }
            } else if (children.values().size() == 2) {

                // Two branches. Merge all of their outputs.
                // Obtain the defintions defined by both children
                Iterator i = childrenIValueMaps.keySet().iterator();
                IntervalDFG child1DFG = (IntervalDFG) i.next();
                IntervalDFG child2DFG = (IntervalDFG) i.next();
                IValueMap child1Map = (IValueMap) childrenIValueMaps.get(child1DFG);
                IValueMap child2Map = (IValueMap) childrenIValueMaps.get(child2DFG);

                // Iterate through all of child1Values
                for (i=child1Map.getDefs().keySet().iterator();i.hasNext();) {
                    Value origv = (Value) i.next();
                    if (isRequired(origv)) {
                        Node child1n = (Node) child1Map.getDefs().get(origv);
                        if (child2Map.getDefs().containsKey(origv)) {
                            Node child2n = (Node) child2Map.getDefs().get(origv);
                            _multiplexNodes(child1DFG,child1n,child2n);
                        } else {
                            Node parentn = getOrCreateNode(origv);
                            _multiplexNodes(child1DFG,child1n,parentn);
                        }
                    }
                }
                // Iterate through all of child2Values
                for (i=child2Map.getDefs().keySet().iterator();i.hasNext();) {
                    Value origv = (Value) i.next();
                    if (isRequired(origv) &&
                            !child1Map.getDefs().containsKey(origv)) {
                        Node child2n = (Node) child2Map.getDefs().get(origv);
                        Node parentn = getOrCreateNode(origv);
                        _multiplexNodes(child2DFG,child2n,parentn);
                        //                          System.out.println("Multiplexing node from parent "+
                        //                                             child2n);
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
        // Get simple input definitions
        for (Iterator i=sourceNodes().iterator();i.hasNext();)
            newDefs.add( ((Node) i.next()).getWeight() );
        for (Iterator i=getInstanceFieldRefInputDefinitions().iterator();
             i.hasNext();)
            newDefs.add( ((Node) i.next()).getWeight() );
        _requiredDefinitions.addAll(newDefs);

        //          System.out.print(_ic.toShortString()+" defs=");
        for (Iterator i=_requiredDefinitions.iterator();i.hasNext();) {
            Object o=i.next();
            //              System.out.print(o+" ("+System.identityHashCode(o)+") ");
        }
        //System.out.println();

        // Connect previously created chain to this node
        if (_next != null) {
            //DEBUG=true; System.out.println("*** Serial Merge ***");
            mergeSerial(_next);
            //DEBUG=false;
        }
    }

    /** returns new lValues **/
    public void mergeSerial(IntervalDFG dfg) {

        // temporary hashmap between old nodes & new.
        // Used when connecting edges
        HashMap nodeMap = new HashMap();

        // Add all nodes to graph
        for (Iterator i = dfg.nodes().iterator(); i.hasNext();) {

            Node node = (Node) i.next();

            if (DEBUG) System.out.print("Adding node="+node);

            Node driver = findNodeDriver(dfg,node);
            if (driver != null)
                nodeMap.put(node,driver);
            else {
                if (node.getWeight() instanceof InstanceFieldRef) {
                    InstanceFieldRef ifr = (InstanceFieldRef) node.getWeight();
                    InstanceFieldRef nifr =
                        _getMatchingInstanceFieldRef((InstanceFieldRef) ifr);
                    if (nifr != null) {
                        Node newnode = addNodeWeight(nifr);
                        _valueMap.addInstanceFieldRef(nifr,newnode);
                        nodeMap.put(node,newnode);
                    } else
                        addNode(node);
                } else
                    addNode(node);
            }
        }

        // Iterate through all edges and add to graph
        for (Iterator i = dfg.edges().iterator(); i.hasNext();) {
            Edge e = (Edge) i.next();
            Node src = e.source();
            if (nodeMap.containsKey(src))
                src = (Node) nodeMap.get(src);
            Node snk = e.sink();
            if (nodeMap.containsKey(snk))
                snk = (Node) nodeMap.get(snk);

            if (successorEdges(src,snk).size() == 0) {
                // Add edge (avoid duplicates)
                if (e.hasWeight())
                    addEdge(src,snk,e.getWeight());
                else
                    addEdge(src,snk);
                Object snkWeight = snk.getWeight();
                if (snkWeight instanceof Local ||
                        snkWeight instanceof InstanceFieldRef)
                    _valueMap.addNewDef(snk);
            }
        }
    }

    public Node addNode(Node n) {

        super.addNode(n);
        Object weight = n.getWeight();
        if (weight instanceof Local) {
            _valueMap.addLocal((Local)weight,n);
        }
        if (weight instanceof InstanceFieldRef) {
            _valueMap.addInstanceFieldRef((InstanceFieldRef)weight,n);
        }
        return n;
    }

    protected Node findNodeDriver(BlockDataFlowGraph dfg, Node n) {

        Object weight = n.getWeight();
        if (weight instanceof Local) {
            if (dfg.inputEdges(n).size() == 0) {
                // return
                return _valueMap.getLastLocal(weight);
            } else
                return null; // Node is being driven
        }
        if (weight instanceof InstanceFieldRef) {
            InstanceFieldRef ifr = (InstanceFieldRef) weight;
            if (dfg.inputEdges(n).size() == 1) {
                return _valueMap.getMatchingInstanceFieldRefNode(ifr);
            }
        }
        return null;
    }

    public IntervalChain getIntervalChain() { return _ic; }

    /*
      public boolean isValueDefined(Value v) {
      if (_requiredDefinitions.contains(v))
      return true;
      if (v instanceof InstanceFieldRef) {
      InstanceFieldRef d =
      getMatchingInstanceFieldRef((InstanceFieldRef)v,
      _requiredDefinitions);
      if (d != null)
      return true;
      }
      return false;
      }
    */

    public Collection getRequiredDefinitions() {
        return _requiredDefinitions;
    }

    public String toBriefString() {
        return _ic.toShortString();
    }


    protected Node _multiplexNodes(IntervalDFG child1DFG,
            Node child1,
            Node child2) {

        Value value = (Value) child1.getWeight();

        // Get the edges associated with the original CFG.
        Node cNode = getConditionNode();

        Node trueNode = child1;
        Node falseNode = child2;
        if (!isTrueNode(child1DFG)) {
            trueNode = child2;
            falseNode = child1;
        }

        //          BinaryMux bmn = new BinaryMux(trueNode,falseNode,cNode,
        //                                                value.toString());
        BinaryMux bmn = new BinaryMux(value.toString());
        Node muxNode = addNodeWeight(bmn);

        addEdge(trueNode,muxNode,"true");
        addEdge(falseNode,muxNode,"false");
        addEdge(cNode,muxNode,"condition");

        Node newValueNode=null;
        try {
            newValueNode = _addLeftValue(value);
        } catch (JHDLUnsupportedException e) {
        }

        // _addSimpleNode, _addInstanceField
        addEdge(muxNode,newValueNode);
        return newValueNode;
    }

    /** move to higher level **/
    public Node getOrCreateNode(Value v) throws JHDLUnsupportedException {
        Node newNode = _valueMap.getLast(v);
        if (DEBUG)
            System.out.println("newNode = "+newNode+" value="+v+
                    " id="+System.identityHashCode(v));
        if (newNode == null) {
            if (v instanceof InstanceFieldRef) {
                InstanceFieldRef ifr = (InstanceFieldRef) v;
                //                  InstanceFieldRef ifr_p = _getMatchingInstanceFieldRef(ifr);
                //                  newNode = _valueMap.getLast(ifr_p);
                //                  if (newNode == null)
                newNode = _createInstanceFieldRef(ifr);
            } else if (v instanceof Local) {
                newNode = _createLocal((Local)v);
            }
        }
        return newNode;
    }

    public boolean isRequired(Value v) {
        if (_requiredDefinitions.contains(v))
            return true;
        if (v instanceof InstanceFieldRef) {
            InstanceFieldRef vifr = (InstanceFieldRef) v;
            for (Iterator i=_requiredDefinitions.iterator();i.hasNext();) {
                Object o = i.next();
                if (o instanceof InstanceFieldRef) {
                    InstanceFieldRef oifr = (InstanceFieldRef) o;
                    if (vifr.getBase().equals(oifr.getBase()) &&
                            vifr.getField().equals(oifr.getField()))
                        return true;
                }
            }
        }
        return false;
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
        Block b = (Block) root.getWeight();
        IfStmt ifs = (IfStmt) b.getTail();
        return ifs;
    }

    public boolean isTrueNode(IntervalDFG cidfg) {

        DirectedAcyclicCFG graph = _ic.getGraph();
        IfStmt ifs = getIfStmt();

        Node childCFGNode = cidfg._ic.getRoot();

        Block dest = (Block) childCFGNode.getWeight();

        //          System.out.println("IFstmt="+ifs+" target="+
        //                             ifs.getTargetBox().getUnit()+" dest head="+
        //                             dest.getHead());


        if (ifs.getTargetBox().getUnit() == dest.getHead())
            return true;
        else
            return false;
    }

    public Collection getInstanceFieldRefInputDefinitions() {
        ArrayList nodes = new ArrayList();
        // iterate over all nodes in the graph
        for (Iterator i = nodes().iterator(); i.hasNext();) {
            Node node = (Node) i.next();
            Object weight = node.getWeight();
            if (inputEdgeCount(node) == 1 &&
                    weight instanceof InstanceFieldRef) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    public static IntervalDFG createIntervalDFG(String args[])
            throws JHDLUnsupportedException {
        IntervalChain ic = IntervalChain.createIntervalChain(args,true);
        return new IntervalDFG(ic);
    }

    public static void main(String args[]) {
        //BlockDataFlowGraph.DEBUG=true;
        IntervalDFG im = null;
        try {
            im = createIntervalDFG(args);
        } catch (JHDLUnsupportedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        //          System.out.println(im);
        //          BlockDataFlowGraph graphs[] =
        //              BlockDataFlowGraph.getBlockDataFlowGraphs(args);
        //          for (int i = 0;i<graphs.length;i++)
        //              PtDirectedGraphToDotty.writeDotFile("bbgraph"+i,
        //                                                  graphs[i]);
        PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
        dgToDotty.writeDotFile(".", "merge", im);
    }

    protected IntervalChain _ic;
    protected IntervalDFG _next;

    /**
     * This contains a Vector of Value objects. A definition for each
     * Value in this list is *required* further up the tree.
     **/
    protected Collection _requiredDefinitions;

    protected IValueMap _valueMap;
}

class IValueMap {
    public IValueMap(MapList l, MapList ifr) {
        locals = l;
        instanceFieldRefs = ifr;
        newDefs = new HashMap();
    }
    public Object clone() {
        MapList l = (MapList) locals.clone();
        MapList ifrs = (MapList) instanceFieldRefs.clone();
        IValueMap vm = new IValueMap(l,ifrs);
        return vm;
    }
    public void addLocal(Local l,Node n) {
        locals.add(l,n);
        //newDefs.add(n);
    }
    public void addInstanceFieldRef(InstanceFieldRef ifr,Node n) {
        instanceFieldRefs.add(ifr,n);
        //newDefs.add(n);
    }
    public void addNewDef(Node n) {
        newDefs.put(n.getWeight(),n);
    }
    public Map getDefs() { return newDefs; }
    public Node getLast(Object v) {
        if (v instanceof Local)
            return getLastLocal(v);
        if (v instanceof InstanceFieldRef)
            return getLastInstanceFieldRef((InstanceFieldRef)v);
        return null;
    }
    public Node getLastLocal(Object v) {
        return (Node) locals.getLast(v);
    }
    public Node getLastInstanceFieldRef(InstanceFieldRef v) {
        return (Node) instanceFieldRefs.getLast(v);
    }
    public Node getMatchingInstanceFieldRefNode(InstanceFieldRef ifr) {
        SootField field = ifr.getField();
        Value baseValue = ifr.getBase();
        InstanceFieldRef previous=null;
        for (Iterator it = instanceFieldRefs.keySet().iterator();it.hasNext();) {
            InstanceFieldRef ifr_n = (InstanceFieldRef) it.next();
            if (ifr_n.getBase().equals(baseValue) &&
                    ifr_n.getField().equals(field)) {
                previous = ifr_n;
            }
        }
        return getLastInstanceFieldRef(previous);
    }

    public MapList locals;
    public MapList instanceFieldRefs;
    public Map newDefs;
}

/*

  - get snapshot of definitions before mergining.
  - use copy of snapshot before each iteration
  (multiple copies for each branch)
  - come up with own addNode method that updates these value maps

*/
