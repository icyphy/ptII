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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import soot.SootMethod;

import soot.jimple.InstanceFieldRef;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.internal.JimpleLocal;

import soot.toolkits.graph.Block;

import ptolemy.copernicus.jhdl.*;
import ptolemy.copernicus.jhdl.util.*;
import ptolemy.copernicus.jhdl.soot.*;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.graph.Node;
import ptolemy.graph.Edge;
import ptolemy.graph.DirectedGraph;

public class IntervalBlockDirectedGraph extends SootBlockDirectedGraph {

    /**
     * This constructor will create an IntervalBlockDirectedGraph
     * object from an IntervalChain object.
     **/
    public IntervalBlockDirectedGraph(IntervalChain ic)
            throws JHDLUnsupportedException, SootASTException {

        // Create DFG from the Block object associated with the root
        // Node of the given IntervalChain
        super(ic.getRootBlock());
        new ControlSootDFGBuilder(this);

        _ic = ic;

        if (DEBUG)
            System.out.println(toShortString()+":Creating IDFG");

        _processChain();

        if (DEBUG)
            System.out.println(toShortString()+":Completed IDFG");

        if (DEBUG) {
            String filename = new String(toShortString()+"graph");
            PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
            dgToDotty.writeDotFile(".",filename,this);
        }

    }

    /**
     * This constructor will create an IntervalBlockDirectedGraph
     * object from a SootMethod.
     **/
    public IntervalBlockDirectedGraph(SootMethod method)
            throws JHDLUnsupportedException, SootASTException,
            IllegalActionException {
        this(new IntervalChain(method));
    }

    protected void _processChain()
            throws JHDLUnsupportedException, SootASTException {

        // 1. Create graph for next inverval in chain if there is one.
        //    (This algorithm works bottom up so that all required
        //     definitions downstream are known before upstream
        //     DFGs are constructed).
        IntervalChain nextChain = _ic.getNext();
        if (nextChain != null) {
            _next = new IntervalBlockDirectedGraph(nextChain);
        } else {
            _next = null;
        }

        // 2. Merge Children Nodes
        if (_ic.isSimpleMerge())
            _simpleMerge();
        else if (_ic.isSpecialMerge()) {
            _specialMerge();
        }
        // else it is a chain that doesn't need merging


        // 3. Connect previously created chain to this node
        if (_next != null) {
            _valueMap.mergeSerial(_next._valueMap);
        }

    }

    /**
     * This method will perform a merge on a standard fork/join
     * construct.
     **/
    protected void _simpleMerge()
            throws JHDLUnsupportedException, SootASTException {

        if (DEBUG)
            System.out.println(toShortString()+":Merge children");

        // key=IntervalChain root Node, Value=IntervalChain
        Map children = _ic.getChildren();

        // Iterate over the children:
        // - merge the child
        // - save the ValueMap for the control path
        // key=IntervalBlockDirectedGraph, Value=ValueMap
        HashMap childrenValueMaps = new HashMap(children.size());
        Iterator i = children.values().iterator();i.hasNext();
        ValueMap initialValueMap = (ValueMap) _valueMap.clone();
        while (i.hasNext()) {
            IntervalChain childInterval = (IntervalChain) i.next();
            // Create a new dfg for child
            IntervalBlockDirectedGraph childDFG =
                new IntervalBlockDirectedGraph(childInterval);
            ValueMap valueMapCopy = (ValueMap) _valueMap.clone();
            //                System.out.println("_valueMap=\n"+_valueMap);
            //                System.out.println("valueMapCopy=\n"+valueMapCopy);
            valueMapCopy.mergeSerial(childDFG._valueMap);
            //                System.out.println("_valueMap=\n"+_valueMap);
            //                System.out.println("valueMapCopy=\n"+valueMapCopy);
            childrenValueMaps.put(childDFG,valueMapCopy);
        }

        Iterator childMapIterator = childrenValueMaps.values().iterator();
        Iterator childDFGs = childrenValueMaps.keySet().iterator();
        int numChildren = children.values().size();

        if (numChildren == 1) {
            // merge root w/child
            ValueMap childMap = (ValueMap) childMapIterator.next();
            IntervalBlockDirectedGraph childDFG =
                (IntervalBlockDirectedGraph) childDFGs.next();
            joinOneChild(childDFG,childMap);
        } else if (numChildren == 2) {
            // merge two children
            ValueMap childMap1 = (ValueMap) childMapIterator.next();
            ValueMap childMap2 = (ValueMap) childMapIterator.next();
            IntervalBlockDirectedGraph child1DFG =
                (IntervalBlockDirectedGraph) childDFGs.next();
            IntervalBlockDirectedGraph child2DFG =
                (IntervalBlockDirectedGraph) childDFGs.next();
            joinTwoChildren(childMap1,child1DFG,
                    childMap2,child2DFG);
        } else {
            // A switch.
            throw new JHDLUnsupportedException("Switches not yet supported");
            // merge switch targets
        }
    }

    public void _specialMerge() throws JHDLUnsupportedException {
        throw new JHDLUnsupportedException("Special Nodes not yet supported");
    }

    public IntervalChain getIntervalChain() { return _ic; }

    public String toBriefString() {
        return _ic.toShortString();
    }

    /**
     * This method will join one control-flow branch with a root
     * control-flow branch. This method must look at all values
     * that are assigned in both the child branch and root branch.
     * Assignment values must be multiplexed in either of the two
     * conditions:
     * 1. If the root and child both define the same value
     * 2. If the child defines a value that is required by a
     *    parent of the root (need the defined and not defined path)
     *
     * Three valueMaps:
     * _valueMap: map of graph *before* merege
     *   - used to determine which Values have already been assigned
     *   - used to associate a Value being merged with a parent Node
     * childDFG._valueMap:  map of child graph (not associated with
     *                      this graph)
     *   - used to determine which Nodes are assigned in the origional
     *     child graph
     * childMap: map of merged parent & child
     *   - used to find the Node that needs to be merged (can't
     *     be childDFG._valueMap since it isn't in graph)
     **/
    public void joinOneChild(IntervalBlockDirectedGraph childDFG,
            ValueMap childMap) {

        if (DEBUG) System.out.println("Merge single fork "+
                childDFG.toShortString()+
                " with parent "+toShortString());
        // Determine which branch is true
        boolean childTrue;
        if (childDFG._ic.isTrueBranch())
            childTrue = true;
        else
            childTrue = false;

        // Determine Values assigned in parent
        Collection parentAssignedValues = _valueMap.getAssignedValues();
        if (DEBUG) {
            System.out.print("\tParent assigned values=");
            printAssignedValues(parentAssignedValues);
        }

        // Determine Values assigned in child
        Collection childAssignedNodes = childDFG._valueMap.getAssignedNodes();
        if (DEBUG) {
            System.out.print("\tChildren assigned values=");
            printAssignedValues(childDFG._valueMap.getAssignedValues());
            System.out.print("\tChildren assigned Nodes=");
            for (Iterator i=childAssignedNodes.iterator();i.hasNext();) {
                Node n = (Node) i.next();
                System.out.print(n+" ");
            }
            System.out.println();
        }

        // Iterate over all Nodes assigned in child.
        // If the value is also assigned in the parent,
        // multiplex the two nodes
        for (Iterator i=childAssignedNodes.iterator();i.hasNext();) {
            Node childNode = (Node) i.next();
            Value nodeValue = (Value) childNode.getWeight();
            if (parentAssignedValues.contains(nodeValue)) {
                Node rootNode = _valueMap.getValueNode(nodeValue);
                Node childMergeNode = childMap.getValueNode(nodeValue);
                if (childTrue)
                    _multiplexTwoNodes(childMergeNode,rootNode,childMap);
                else
                    _multiplexTwoNodes(rootNode,childMergeNode,childMap);
            }
        }
        _valueMap = childMap;
    }

    public void joinTwoChildren(ValueMap child1Map,
            IntervalBlockDirectedGraph child1DFG,
            ValueMap child2Map,
            IntervalBlockDirectedGraph child2DFG) {

        if (DEBUG) System.out.println("Merge dual forks "+
                child1DFG.toShortString()+" and "+
                child2DFG.toShortString()+
                " with parent "+toShortString());


        // Determine which branch is true
        boolean child1True;
        if (child1DFG._ic.isTrueBranch())
            child1True = true;
        else
            child1True = false;

        // Determine Values assigned in parent
        Collection parentAssignedValues = _valueMap.getAssignedValues();
        if (DEBUG) {
            System.out.print("Parent assigned values=");
            printAssignedValues(parentAssignedValues);
        }

        // Determine Values assigned in each child
        // (not including those assigned in parent)
        Collection child1AssignedValues =
            child1DFG._valueMap.getAssignedValues();
        Collection child2AssignedValues =
            child2DFG._valueMap.getAssignedValues();

        // Iterate over all assignment Nodes in child1
        for (Iterator i=child1DFG._valueMap.getAssignedNodes().iterator();
             i.hasNext();) {
            Node childNode = (Node) i.next();
            Value nodeValue = (Value) childNode.getWeight();
            if (child2AssignedValues.contains(nodeValue)) {
                Node child2Node = child2Map.getValueNode(nodeValue);
                Node child1MergeNode = child1Map.getValueNode(nodeValue);
                if (child1True)
                    _multiplexTwoNodes(child1MergeNode,child2Node,child1Map);
                else
                    _multiplexTwoNodes(child2Node,child1MergeNode,child1Map);
            } else if (parentAssignedValues.contains(nodeValue)) {
                Node rootNode = _valueMap.getValueNode(nodeValue);
                Node child1MergeNode = child1Map.getValueNode(nodeValue);
                if (child1True)
                    _multiplexTwoNodes(child1MergeNode,rootNode,child1Map);
                else
                    _multiplexTwoNodes(rootNode,child1MergeNode,child1Map);
            }
        }

        // Iterate over all assignment Nodes in child2
        for (Iterator i=child2DFG._valueMap.getAssignedNodes().iterator();
             i.hasNext();) {
            Node childNode = (Node) i.next();
            Value nodeValue = (Value) childNode.getWeight();

            // If the value also exists in child1, it has been
            // processed. Continue.

            if (child1AssignedValues.contains(nodeValue))
                continue;

            // This is the case when the value is defined in child2
            // and in the root, but not in child1
            if (parentAssignedValues.contains(nodeValue)) {
                Node rootNode = _valueMap.getValueNode(nodeValue);
                Node child2MergeNode = child2Map.getValueNode(nodeValue);

                if (!child1True)
                    _multiplexTwoNodes(child2MergeNode,rootNode,child2Map);
                else
                    _multiplexTwoNodes(rootNode,child2MergeNode,child2Map);
            }
        }

        // Merge Value Maps!
        _valueMap.updateMap();

    }

    protected void printAssignedValues(Collection values) {
        for (Iterator i=values.iterator();i.hasNext();) {
            Value v = (Value) i.next();
            System.out.print(v+"("+System.identityHashCode(v)+") ");
        }
        System.out.println();
    }

    protected Node _multiplexTwoNodes(Node trueNode,
            Node falseNode,
            ValueMap map) {

        if (DEBUG)
            System.out.println("\tMultiplex: True="+
                    trueNode+"("+System.identityHashCode(trueNode)
                    +")"+
                    " False="+falseNode+"("+
                    System.identityHashCode(trueNode)+")");

        // Get the edges associated with the original CFG.
        Node cNode = getConditionNode();

        Value value = (Value) trueNode.getWeight();
        BinaryMux bmn = new BinaryMux(value.toString());

        Node muxNode = addNodeWeight(bmn);

        addEdge(trueNode,muxNode,BinaryMux.TRUE_LABEL);
        addEdge(falseNode,muxNode,BinaryMux.FALSE_LABEL);
        addEdge(cNode,muxNode,BinaryMux.CONDITION_LABEL);

        Node newNode = map.addValueNode(value);
        addEdge(muxNode,newNode);

        return newNode;
    }



    /**
     * Return the Node associated with the Condition of the IfStmt.
     **/
    public Node getConditionNode() {
        IfStmt ifs = getIfStmt();
        Value v = ifs.getCondition();
        return _valueMap.getValueNode(v);
    }

    /**
     * This method will return the IfStmt associated with the
     * last Unit in the Block associated with this graph.
     * If the last Unit is not an IfStmt, this method will return a
     * null.
     **/
    public IfStmt getIfStmt() {
        Node root = _ic.getRoot();
        Block b = (Block) root.getWeight();
        Unit u = b.getTail();
        if (u instanceof IfStmt)
            return (IfStmt) u;
        else
            return null;
    }

    public String toShortString() {
        return _ic.toShortString();
    }

    /**
     * This method will greate an IntervalBlockDirectedGraph from the
     * Class and Method specified in the String arguments. This
     * method creates the graph from an IntervalChain.
     *
     * @param args Specifies the Classname (args[0]) and the
     * Methodname (args[1]).
     * @param writeGraphs If set true, this method will create
     * ".dot" file graphs for intermediate results. Specifically,
     * this method will create a file called "merge.dot"
     * that represents the merged DFG.
     *
     * @see IntervalChain#createIntervalChain(String[],boolean)
     * TODO: modify this method to use the SootMethod constructor
     **/
    public static IntervalBlockDirectedGraph createIntervalBlockDirectedGraph(String args[],boolean writeGraphs)
            throws JHDLUnsupportedException {
        IntervalChain ic = IntervalChain.createIntervalChain(args,writeGraphs);
        IntervalBlockDirectedGraph ibdg=null;
        try {
            ibdg = new IntervalBlockDirectedGraph(ic);
        } catch (SootASTException e) {
            System.err.println(e);
            System.exit(1);
        }
        if (writeGraphs) {
            PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
            dgToDotty.writeDotFile(".", "merge",ibdg);
        }
        return ibdg;
    }

    public static boolean DEBUG=false;

    public static void main(String args[]) {

        SootBlockDirectedGraph graphs[] =
            ControlSootDFGBuilder.createDataFlowGraphs(args,true);
        /*
          for (int i = 0; i<graphs.length;i++) {
          System.out.print("Assigned Nodes for Graph "+i);
          for (Iterator j =
          graphs[i].getValueMap().assignedNodes().iterator();
          j.hasNext();) {
          Object o = j.next();
          System.out.print(" "+o);
          }
          System.out.println();
          }
        */

        IntervalBlockDirectedGraph im = null;
        try {
            im = createIntervalBlockDirectedGraph(args,true);
        } catch (JHDLUnsupportedException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    protected IntervalChain _ic;
    protected IntervalBlockDirectedGraph _next;


}

