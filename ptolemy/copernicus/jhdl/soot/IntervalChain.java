/* An interval within a control flow graph.

Copyright (c) 2001-2005 The Regents of the University of California.
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
*/
package ptolemy.copernicus.jhdl.soot;

import soot.SootMethod;
import soot.Unit;

import soot.jimple.IfStmt;

import soot.toolkits.graph.Block;

import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


//////////////////////////////////////////////////////////////////////////
//// IntervalChain

/**

This class represents an interval within a control-flow graph. While
there are several definitions for an Interval, this class will define
an Interval as a directed, acyclic control flow graph with one
entry point (root) and one exit point (sink). Since control flow
graphs are not naturally organized as Intervals, the control flow
graph may be modified by adding nodes where appropriate to identify and
separate such Intervals.

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
@Pt.ProposedRating Red (cxh)
@Pt.AcceptedRating Red (cxh)
*/
public class IntervalChain {
    public IntervalChain(SootMethod method) throws IllegalActionException {
        this(new DominatorCFG(method));
    }

    /**
     * This constructor is called to create an IntervalChain for a top-level
     * DominatorCFG control flow graph.
     **/
    public IntervalChain(DominatorCFG graph) throws IllegalActionException {
        _parent = null;
        _graph = graph;
        _root = _graph.source();
        _init();
    }

    /**
     * This constructor is called to create an IntervalChain for
     * a Node in a DominatorCFG graph. This constructor is called
     * recursively as Chains are built from a top-level graph.
     **/
    public IntervalChain(IntervalChain parent, Node root)
        throws IllegalActionException {
        _parent = parent;
        _graph = getGraph();
        _root = root;
        _init();
    }

    public DominatorCFG getGraph() {
        if (_parent == null) {
            return _graph;
        } else {
            return _parent.getGraph();
        }
    }

    public Block getRootBlock() {
        return (Block) getRoot().getWeight();
    }

    /** Return the sink node of the interval **/
    public Node getSink() {
        return _sink;
    }

    /** Return the sink node in the chain of intervals. If this
     * interval is the end of the chain, return the sink of this
     * interval. If not, recursively call getChainSinkNode until
     * the end of the chain is found.
     **/
    public Node getChainSinkNode() {
        if (_next == null) {
            return _sink;
        } else {
            return _next.getChainSinkNode();
        }
    }

    /** Return the last IntervalChain in the Chain. **/
    public IntervalChain getChainSinkInterval() {
        if (_next == null) {
            return this;
        } else {
            return _next.getChainSinkInterval();
        }
    }

    /** Return the sink node of the interval **/
    public Node getChainSinkTarget() {
        IntervalChain n = getChainSinkInterval();
        return n.getSinkTarget();
    }

    public Node getRoot() {
        return _root;
    }

    public IntervalChain getNext() {
        return _next;
    }

    public Map getChildren() {
        return _children;
    }

    public boolean isSingleNode() {
        if ((_children == null) && (_specialChildren == null)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSimpleMerge() {
        if (isSingleNode()) {
            return false;
        }

        if ((_children != null) && (_specialChildren == null)) {
            return true;
        }

        return false;
    }

    public boolean isSpecialMerge() {
        if (isSingleNode()) {
            return false;
        }

        if ((_children == null) && (_specialChildren != null)) {
            return true;
        }

        return false;
    }

    public boolean isValid() {
        return _valid;
    }

    /**
     * This method will determine whether the IntervalChain is a
     * "true" branch in an IfStmt. Will return false if it is not a
     * branch in an IfStmt or it is the "false" branch in an IfStmt.
     **/
    public boolean isTrueBranch() {
        // Get the root Node to the parent IntervalChain
        if (_parent == null) {
            return false;
        }

        Node parentRoot = _parent.getRoot();

        // Get the last unit in the Block associated with the Parent
        Block b = (Block) parentRoot.getWeight();
        Unit u = b.getTail();

        // See if the last statement is an IfStmt
        IfStmt ifstmt = null;

        if (u instanceof IfStmt) {
            ifstmt = (IfStmt) u;
        } else {
            return false;
        }

        // Get the block associated with the current IntervalChain
        Block curBlock = (Block) _root.getWeight();

        // Check to see if target of IfStmt is the same as the
        // curBlock
        if (ifstmt.getTargetBox().getUnit() == curBlock.getHead()) {
            return true;
        }

        return false;
    }

    /** The sink Node <i>must</i> have at most one outgoing edge. This
     * method will return the target of this edge. **/
    public Node getSinkTarget() {
        Collection targets = _graph.successors(_sink);

        if (targets.size() == 0) {
            return null;
        }

        return (Node) targets.iterator().next();
    }

    public String getRootShortString() {
        return _graph.nodeString(_root);
    }

    public String toShortString() {
        return "I" + getRootShortString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Interval " + toShortString());

        if (_parent == null) {
            sb.append(" no parent");
        } else {
            sb.append(" parent=" + is(_parent));
        }

        if (_children != null) {
            sb.append(" children=");

            for (Iterator i = _children.values().iterator(); i.hasNext();) {
                IntervalChain r = (IntervalChain) i.next();
                sb.append(r.toShortString() + " ");
            }
        } else {
            if (_specialChildren != null) {
                sb.append(" special=");

                for (Iterator i = _specialChildren.iterator(); i.hasNext();) {
                    Node n = (Node) i.next();
                    sb.append(ns(n) + " ");
                }
            } else {
                sb.append(" no children");
            }
        }

        sb.append(" sink=");

        if (_sink == null) {
            sb.append("none");
        } else {
            sb.append(ns(_sink));
        }

        if (_next != null) {
            sb.append(" next=" + is(_next));
        }

        sb.append("\n");

        // Print all children
        if (_children != null) {
            for (Iterator i = _children.values().iterator(); i.hasNext();) {
                IntervalChain r = (IntervalChain) i.next();
                sb.append(r);
            }
        }

        // Print next
        if (_next != null) {
            sb.append(_next);
        }

        return sb.toString();
    }

    protected String ns(Node n) {
        if (n != null) {
            return _graph.nodeString(n);
        } else {
            return "null";
        }
    }

    protected String is(IntervalChain i) {
        Node n = i._root;
        return _graph.nodeString(n);
    }

    /**
     * This method will search the topology of the graph and create
     * the appropriate IntervalChain.
     *
     * _root and _graph should be set before this method is called.
     *
     **/
    protected void _init() throws IllegalActionException {
        _valid = true;

        // Switch on the number of succesors to the current Node.
        // There are three cases:
        // 0 successors: it is a sink Node
        // 1 successor:  It is a single Node (one Node interval)
        // >1 successors: It is a fork Node. This case is handled
        //                Differently
        //switch(numSuccessors) {
        switch (_graph.successors(_root).size()) {
        case 0:

            // No successor Nodes to _root (this is a leaf node).
            // Set the _sinkNode to _root and end the chain.
            _sink = _root;
            _next = null;

            if (DEBUG) {
                System.out.print("Leaf Node=" + ns(_root));
            }

            break;

        case 1:

            // _root has only one successor. This Interval is
            // a single node - set _sinkNode to _root.
            Node successor = (Node) _graph.successors(_root).iterator().next();
            _sink = _root;

            // If the _root dominates the child, then the child
            // is the next Node in a chain. Continue the chain.
            // If the _root does not dominate the child, the chain
            // ends here (a dominator will create an Interval for
            // the child).
            if (_graph.dominates(_root, successor)) {
                // root dominate the one child
                _next = new IntervalChain(this, successor);
            } else {
                // root does not dominate the one child
                _next = null;
            }

            if (DEBUG) {
                System.out.print("Sequential Node=" + ns(_root));

                if (_next != null) {
                    System.out.print(" next=" + is(_next));
                } else {
                    System.out.print(" no next");
                }
            }

            break;

        default:
            _processForkNode();
            break;
        }

        if (DEBUG) {
            System.out.println();
        }

        //System.out.println("True branch="+isTrueBranch()+" "+getRootShortString());
    }

    /**
     * This method will analyze fork Nodes and create the proper
     * Interval based on the topology.
     * There are three types of Intervals that occur with
     * fork Nodes:
     * 1. Its children are all dominated and the Immediate
     *    post dominator is dominated by fork (i.e. this is
     *    a standard fork/join interval). This is the
     *    "normal" fork interval. In this case, an interval is
     *    created to include the root, all children and the associated
     *    join node.
     * 2. The node forks, but its children are not dominated by the
     *    root.  This is an "invalid" interval. (i.e. multiple
     *    exits). In this case, the IntervalChain is marked as
     *    "invalid" and will subsequently be ignored.
     *    Nodes within such invalid intervals will be part of
     *    "special" intervals.
     * 3. The node forks, but it has children that are
     *    "invalid". This node will include a sub-tree
     *    of nodes and will terminate at the immediate
     *    postDominator of the root. This is called a "special"
     *    interval.
     **/
    protected void _processForkNode() throws IllegalActionException {
        Node child;
        Collection rootSuccessors = _graph.successors(_root);
        int numSuccessors = rootSuccessors.size();

        // iterate over direct children. Children are either dominated
        // by the root or not. If they are dominated, create a new
        // interval and store.
        // If not, save nonDominated children in a list.
        HashMap domChildren = new HashMap(numSuccessors);
        Vector nonDomChildren = new Vector(numSuccessors);
        boolean specialInterval = false;

        for (Iterator i = rootSuccessors.iterator(); i.hasNext();) {
            child = (Node) i.next();

            if (_graph.dominates(_root, child)) {
                // root dominates current child.
                IntervalChain childInterval = new IntervalChain(this, child);
                domChildren.put(child, childInterval);

                if (!childInterval.isValid()) {
                    specialInterval = true;
                }
            } else {
                // root does not dominate current child.
                nonDomChildren.add(child);
            }
        }

        // Determine the immediate post-dominator (ipd) of the current
        // node.
        Node immediatePostDominator = _graph.getImmediatePostDominator(_root);

        if (DEBUG) {
            System.out.print("Fork " + ns(_root) + " ipd="
                + ns(immediatePostDominator));
        }

        // If the ipd is one of the dominated children, remove it from
        // the list of dominated children.
        // (it will be outside of the interval since a custom
        //  join node will be made in front of it)
        domChildren.remove(immediatePostDominator);

        ///////////////////////////////
        // Process nonSpecial intervals
        ///////////////////////////////
        if (!specialInterval) {
            // Determine whether there is a common target
            // for children (i.e. is this a Normal interval).
            boolean canMerge = true;

            if (DEBUG) {
                System.out.print(" domChildren=");
            }

            for (Iterator i = domChildren.values().iterator();
                        i.hasNext() && canMerge;) {
                IntervalChain childInterval = (IntervalChain) i.next();

                if (DEBUG) {
                    System.out.print(is(childInterval) + " ");
                }

                // All of the edges that leave _root must converge to
                // the ipd in order for this node to be mergable. There
                // are several cases in which this might occur:
                // 1. The target of the child interval must point
                //    to the ipd.
                // 2. The root of the child interval must be the ipd.
                //
                // Note that a merge Node for this interval has
                // not yet been created (ipd may be a child interval)
                if ((childInterval.getChainSinkTarget() != immediatePostDominator)
                            && (childInterval.getRoot() != immediatePostDominator)) {
                    canMerge = false;

                    if (DEBUG) {
                        System.out.print(" childInterval " + is(childInterval)
                            + "!=ipd " + ns(immediatePostDominator)
                            + "(target="
                            + ns(childInterval.getChainSinkTarget()) + " "
                            + is(childInterval.getChainSinkInterval()) + ")");
                    }
                }
            }

            for (Iterator i = nonDomChildren.iterator();
                        i.hasNext() && canMerge;) {
                child = (Node) i.next();

                if (child != immediatePostDominator) {
                    canMerge = false;

                    if (DEBUG) {
                        System.out.print(" child " + ns(child) + "!= ipd "
                            + ns(immediatePostDominator));
                    }
                }
            }

            // If Interval cannot be merged, it is invalid
            if (!canMerge) {
                _valid = false;

                if (DEBUG) {
                    System.out.println(" invalid");
                }

                return;
            } else {
                if (DEBUG) {
                    System.out.print(" normal");
                }
            }

            _children = domChildren;

            // Create a new node as the target for the final
            // merge (instead of the ipd). Make the ipd
            // the next and continue processing.
            _sink = addSimpleJoinNode(immediatePostDominator);

            if (DEBUG) {
                System.out.print(" sink=" + ns(_sink));
            }

            if (DEBUG) {
                System.out.println();
            }

            if (_graph.dominates(_root, immediatePostDominator)) {
                _next = new IntervalChain(this, immediatePostDominator);
            } else {
                _next = null;
            }
        } else {
            ///////////////////////////////
            // Process Special intervals
            ///////////////////////////////
            if (DEBUG) {
                System.out.print(" special");
            }

            Vector nodes = new Vector();
            boolean addMode = false;

            //                  Object sort[] = _graph.topologicalSort();
            //                  for (int i=0;i<sort.length;i++) {
            //                      Node n = (Node) sort[i];
            Collection sort = _graph.topologicalSort(_graph.nodes());

            for (Iterator i = sort.iterator(); i.hasNext();) {
                Node n = (Node) i.next();

                if (n == immediatePostDominator) {
                    // don't add ipd
                    addMode = false;
                }

                if (addMode) {
                    nodes.add(n);

                    // add join node
                    addSpecialJoinNode(n);
                }

                if (n == _root) {
                    // don't add root
                    addMode = true;
                }
            }

            _specialChildren = nodes;

            // Continue chain (new interval)
            // Create a new node as the target for the final
            // merge (instead of the ipd). Make the ipd
            // the next and continue processing.
            _sink = addSpecialJoinNode(immediatePostDominator);

            if (DEBUG) {
                System.out.print(" sink=" + ns(_sink));
            }

            if (DEBUG) {
                System.out.println();
            }

            if (_graph.dominates(_root, immediatePostDominator)) {
                _next = new IntervalChain(this, immediatePostDominator);
            } else {
                _next = null;
            }
        }
    }

    protected Node addSimpleJoinNode(Node ipd) throws IllegalActionException {
        // temporary string name for new join node
        String newNodeWeight = "join_";

        // Vector for edges that will need to be removed (may
        // not be the full size of ipdInEdges)
        Vector remove = new Vector(_graph.inputEdges(ipd).size());

        // Iterate through dominated intervals and change the edge
        // leaving the sink node to the newNode
        for (Iterator i = _children.values().iterator(); i.hasNext();) {
            IntervalChain ic = (IntervalChain) i.next();
            Node sink = ic.getChainSinkNode();
            Edge e = (Edge) _graph.outputEdges(sink).iterator().next();
            remove.add(e);
            newNodeWeight += ns(sink);
        }

        // Get all edges from _root to commonTarget and move them
        // to newNode
        for (Iterator i = _graph.outputEdges(_root).iterator(); i.hasNext();) {
            Edge e = (Edge) i.next();

            if (e.sink() == ipd) {
                remove.add(e);
                newNodeWeight += (ns(e.source()));
            }
        }

        // Create new node
        Node newNode = _graph.addNodeWeight(newNodeWeight);

        // iterate over edges to remove and edges to add
        for (Iterator i = remove.iterator(); i.hasNext();) {
            Edge e = (Edge) i.next();
            _graph.removeEdge(e);
            _graph.addEdge(e.source(), newNode);
        }

        _graph.addEdge(newNode, ipd);

        //System.out.println(_graph);
        // _graph.update();
        //System.out.println(_graph);
        return newNode;
    }

    protected Node addSpecialJoinNode(Node join) {
        Collection joinInEdges = _graph.inputEdges(join);

        if (joinInEdges.size() > 1) {
            //              System.out.print(" spjoin="+ns(join)+" edges="+
            //                               joinInEdges.size());
            String newNodeWeight = "spjoin__";
            Vector remove = new Vector(joinInEdges.size());

            // Figure out the weight and collect edges
            for (Iterator i = joinInEdges.iterator(); i.hasNext();) {
                Edge e = (Edge) i.next();
                newNodeWeight += ns(e.source());
                remove.add(e);
            }

            Node newNode = _graph.addNodeWeight(newNodeWeight);

            for (Iterator i = remove.iterator(); i.hasNext();) {
                Edge e = (Edge) i.next();
                _graph.removeEdge(e);
                _graph.addEdge(e.source(), newNode);
            }

            _graph.addEdge(newNode, join);
            return newNode;
        }

        return null;
    }

    /**
     * This method will create an IntervalChain object for the
     * Class and Method specified by the String arguments.
     * This method creates the IntervalChain from a DominatorCFG.
     *
     * @param args Specifies the Classname (args[0]) and the
     * Methodname (args[1]).
     * @param writeGraphs If set true, this method will create
     * ".dot" file graphs for intermediate results. Specifically,
     * this method will create a file called "interval.dot"
     * that represents the Intervals in the CFG.
     *
     * @see DominatorCFG#createDominatorCFG(String[],boolean)
     **/
    public static IntervalChain createIntervalChain(String[] args,
        boolean writeGraphs) {
        IntervalChain ic = null;

        try {
            DominatorCFG _cfg = DominatorCFG.createDominatorCFG(args,
                    writeGraphs);
            ic = new IntervalChain(_cfg);

            if (writeGraphs) {
                PtDirectedGraphToDotty toDotty = new PtDirectedGraphToDotty();
                toDotty.writeDotFile(".", "interval", _cfg);
            }
        } catch (IllegalActionException e) {
            System.err.println(e);
            System.exit(1);
        }

        return ic;
    }

    public static void main(String[] args) {
        IntervalChain ic = createIntervalChain(args, true);
        System.out.println("IntervalChain=\n" + ic);
    }

    /** The top-level DAG associated with this IntervalChain **/
    protected DominatorCFG _graph;

    /** The Root Node of the interval (i.e. the entry Node of the
     * Interval). **/
    protected Node _root;

    /** This Node is the sink for the current Interval. By definition, each
     * IntervalChain must have one entry point (_root) and one exit point. The
     * _sink is the exit point of the Interval.
     **/
    protected Node _sink;

    /** The parent Interval of this Interval. If _parent is null, this
     * interval is the top-level interval **/
    IntervalChain _parent;

    /** An Interval is a linked list of sequentially executing intervals.
     * _next is the next Interval in the list. If _next is null, this
     * is the end of the interval chain.
     **/
    protected IntervalChain _next;
    protected boolean _valid;

    /**
     * key=root Node, value=corresponding IntervalChain
     **/
    HashMap _children;
    Vector _specialChildren;
    public static boolean DEBUG = false;
}
