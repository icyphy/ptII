/* Super block

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
@ProposedRating Red (cxh)
@AcceptedRating Red (cxh)
*/
package ptolemy.copernicus.jhdl.util;

import soot.Unit;

import soot.jimple.*;

import soot.toolkits.graph.Block;

import ptolemy.graph.*;
import ptolemy.graph.DirectedGraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


//import soot.util.*;

/**
 * Control flow label
 */
public class SuperBlock implements GraphNode {
    /**
     * The head of a new tree
     */
    public SuperBlock(Block block, DirectedGraph graph) {
        _block = block;
        _graph = graph;

        _labels = null; //new HashMap();
    }

    public Vector primaryInputs() {
        return _primaryInputs;
    }

    public Vector primaryOutputs() {
        return _primaryOutputs;
    }

    public DirectedGraph getGraph() {
        return _graph;
    }

    public Block getBlock() {
        return _block;
    }

    //      public Vector getLabels() {
    //          return _labels;
    //      }
    public void addLabel(Label label, SuperBlock from) {
        if (from == null) {
            //Ummm.. this is a hack.  The source node in a graph will have a
            //label that doesn't come "from" anything.. but I want to
            //prevent a NullPointerException
            from = this;
        }

        if (_labels == null) {
            _labels = new HashMap();
        }

        Vector labelVector = (Vector) _labels.get(from);

        if (labelVector == null) {
            labelVector = new Vector();
            _labels.put(from, labelVector);
        }

        labelVector.add(label);
    }

    public void combineLabels(DirectedGraph graph) {
        _shrinkLabels(graph);
        _combineNonShrinkable(graph);
    }

    // propagate labels to a specific block. I know that I am further upstream
    // in the dag.
    public void propagateLabelsTo(SuperBlock succ) {
        Unit first = succ._block.getHead();
        Unit last = this._block.getTail();

        Collection c = _labels.values();

        if (c.size() != 1) { //DEBUG

            //By this point there should be only 1 vector of Labels left..
            throw new RuntimeException(
                    "_labels should contain exactly 1 vector.");
        }

        Vector labelVector = (Vector) c.toArray()[0];

        if (last instanceof IfStmt) {
            // The block ends in an if statement, so create a new label for the successor
            Unit target = ((IfStmt) last).getTarget();

            //ConditionExpr condition = (ConditionExpr)((IfStmt)last).getCondition();
            for (Iterator labels = labelVector.iterator(); labels.hasNext();) {
                Label label = (Label) labels.next();
                succ.addLabel(new Label(label, first == target, this), this);
            }
        } else {
            for (Iterator labels = labelVector.iterator(); labels.hasNext();) {
                Label label = (Label) labels.next();
                succ.addLabel(label, this);
            }
        } //if else (last instanceof IfStmt)
    }

    public Node createDataFlow(DirectedGraph graph, Object value) {
        System.out.println(">>>>>>>>>createDataFlow on " + this + " for "
                + value);
        System.out.println(">>>>>>>>>>>>> " + value.getClass());

        Collection pred = _labels.keySet();
        GraphNode predecessor;
        Node returnNode;

        if (pred.size() == 0) {
            predecessor = null;
        } else {
            predecessor = (GraphNode) pred.toArray()[0];
        }

        if (predecessor == this) {
            predecessor = null; //hack to fix a hack
        }

        //          if (!_graph.containsNodeWeight(value)) {
        //            System.out.println("---------This block doesn't have it");
        //              //This block doesn't define 'value', so pass the request to its
        //              //predecessor
        //              if (predecessor == null) return null; //No predecessor
        //              return predecessor.createDataFlow(graph, value);
        //          }
        Collection c = _graph.nodes();
        Set equalSet = new HashSet();

        for (Iterator i = c.iterator(); i.hasNext();) {
            Node n = (Node) i.next();

            if (_matches(n.getWeight(), value)) {
                equalSet.add(n);
            }
        }

        if (equalSet.size() == 0) {
            //This block doesn't define 'value', so pass the request to its
            //predecessor
            if (predecessor == null) {
                return null; //No predecessor
            }

            return predecessor.createDataFlow(graph, value);
        }

        Node[] nodes = new Node[equalSet.size()];
        System.arraycopy(equalSet.toArray(), 0, nodes, 0, equalSet.size());

        //When is the last time this block defines (i.e. write to) this value?
        Node lastDefinition = nodes[0];

        for (int i = 1; i < nodes.length; i++) {
            if (_graph.reachableNodes(lastDefinition).contains(nodes[i])) {
                //If nodes[i] is reachable from lastDefintion, then nodes[i]
                //is defined later and needs to be the new lastDefinition
                lastDefinition = nodes[i];
            } else if (!_graph.reachableNodes(nodes[i]).contains(lastDefinition)) { //DEBUG

                //Shouldn't happen.  This means that two references to 'value' are
                //not mutually reachable.  So we can't determine which was assigned
                //last in the code.  Soot optimizations should keep this from
                //happening.. but we'll check anyway
                throw new RuntimeException(
                        "Can't find the last definition; concurrent definition in two paths?");
            }
        }

        if (_graph.sourceNodes().contains(lastDefinition)) {
            //if lastDefinition is a source, then it really isn't defined here
            if (predecessor == null) {
                if (!graph.containsNode(lastDefinition)) {
                    graph.addNode(lastDefinition);
                }

                return lastDefinition;
            }

            Node gn = predecessor.createDataFlow(graph, value);

            if (gn == null) {
                //Nobody else wrote to it.. must be some kind of invariant or constant
                if (!graph.containsNode(lastDefinition)) {
                    graph.addNode(lastDefinition);
                }

                return lastDefinition;
            } else {
                return gn;
            }
        }

        Collection sources = _graph.sourceNodes();

        Vector currentBlockDefs = new Vector();
        Vector predecessorDefs = new Vector();

        currentBlockDefs.add(lastDefinition);

        for (int i = 0; i < currentBlockDefs.size(); i++) {
            Node currNode = (Node) currentBlockDefs.elementAt(i);
            System.out.println("currentBlockDefs: " + currNode);

            if (!graph.containsNode(currNode)) {
                graph.addNode(currNode);
            }

            for (Iterator j = _graph.predecessors(currNode).iterator();
                 j.hasNext();) {
                Node predNode = (Node) j.next();
                System.out.println("  " + predNode);

                if (sources.contains(predNode)) {
                    //If its a source, look for it later in the predecessor node
                    predecessorDefs.add(predNode);
                } else {
                    currentBlockDefs.add(predNode);
                }

                if (!graph.containsNode(predNode)) {
                    graph.addNode(predNode);
                }

                if (!graph.edgeExists(predNode, currNode)) {
                    graph.addEdge(predNode, currNode);
                }
            }
        }

        if (predecessor != null) {
            for (Iterator i = predecessorDefs.iterator(); i.hasNext();) {
                Node n = (Node) i.next();

                if (n.getWeight() instanceof Constant) {
                    continue;
                }

                System.out.println("going to " + predecessor + " to look for "
                        + n);

                Node result = predecessor.createDataFlow(graph, n.getWeight());

                if ((result != null) && !graph.edgeExists(result, n)) {
                    graph.addEdge(result, n);
                }
            }
        }

        return lastDefinition;
    }

    public String toString() {
        return _block.toShortString();
    }

    /**
     *  Check to see if the two objects match
     */
    protected boolean _matches(Object one, Object two) {
        if ((one == two) || (one.equals(two))) {
            return true;
        }

        if ((one instanceof FieldRef) && (two instanceof FieldRef)) {
            if (((FieldRef) one).equivTo(two)) {
                return true;
            }
        }

        return false;
    }

    /** Combine Labels that have same parent label. **/
    protected void _shrinkLabels(DirectedGraph graph) {
        if (_labels == null) {
            //If no labels were added to this node, it must be the head of the graph
            // Hashing a superblock to a vector of Labels
            _labels = new HashMap();

            Vector v = new Vector();
            v.add(new Label()); // add top-level for myself
            _labels.put(this, v);
            return;
        }

        boolean done = false;

        OUTER:
        while (!done) {
            //Iterate over all labels
            //  get all keys in hashMap
            for (Iterator blocks = _labels.keySet().iterator();
                 blocks.hasNext();) {
                GraphNode key = (GraphNode) blocks.next();
                Vector labelVector = (Vector) _labels.get(key);

                // get all Labels in vector associated within given key
                for (Iterator labels = labelVector.iterator();
                     labels.hasNext();) {
                    Label label = (Label) labels.next();

                    //Now we need to iterate over the labels again (compare all labels against each other)
                    for (Iterator blocks2 = _labels.keySet().iterator();
                         blocks2.hasNext();) {
                        GraphNode key2 = (GraphNode) blocks2.next();
                        Vector labelVector2 = (Vector) _labels.get(key2);

                        for (Iterator labels2 = labelVector2.iterator();
                             labels2.hasNext();) {
                            Label label2 = (Label) labels2.next();

                            //Do a pairwise comparison
                            if ((label != label2) && label.canCombine(label2)) {
                                //Combine labels
                                Label lowest;
                                GraphNode first;
                                GraphNode second;

                                if (label.level() <= label2.level()) {
                                    first = key;
                                    second = key2;
                                    lowest = label;
                                } else {
                                    first = key2;
                                    second = key;
                                    lowest = label2;
                                }

                                MuxNode mux = new MuxNode(first, second, lowest);

                                Vector v = new Vector();
                                v.add(lowest.getParent());

                                _labels.put(mux, v);
                                labelVector.remove(label);
                                labelVector2.remove(label2);

                                //Add appropriate arcs for new mux
                                graph.addNodeWeight(mux);
                                graph.addEdge(first, mux,
                                        new Boolean(lowest.branch()).toString());
                                graph.addEdge(second, mux,
                                        new Boolean(!lowest.branch()).toString());
                                graph.addEdge(lowest.getSuperBlock(), mux,
                                        ((IfStmt) lowest.getSuperBlock()._block
                                                .getTail()).getCondition().toString());
                                graph.addEdge(mux, this);

                                //If labelVector is empty, remove its entry from _labels and remove
                                //the arc from the graph
                                if (labelVector.isEmpty()) {
                                    _labels.remove(key);

                                    Node n1 = (Node) graph.node(key);
                                    Node n2 = (Node) graph.node(this);
                                    graph.removeEdge((Edge) graph.neighborEdges(
                                                             n1, n2).toArray()[0]);
                                }

                                //If labelVector2 is empty, remove its entry from _labels and remove
                                //the arc from the graph
                                if (labelVector2.isEmpty()) {
                                    _labels.remove(key2);

                                    Node n1 = (Node) graph.node(key2);
                                    Node n2 = (Node) graph.node(this);
                                    graph.removeEdge((Edge) graph.neighborEdges(
                                                             n1, n2).toArray()[0]);
                                }

                                continue OUTER; //Avoid a ConcurrentModificationException
                            } // if (label ...)
                        } // for (labels2 ...)
                    } // for (blocks2 ...)
                } // for (labels ...)
            } // for (blocks ...)

            done = true;
        } // while (!done)
    } //Method _shrinkLabels

    /** Put muxes on wires that cannot be shrunk. **/
    protected void _combineNonShrinkable(DirectedGraph graph) {
        while (true) {
            Collection c = _labels.keySet();

            if (c.size() < 2) {
                return;
            }

            GraphNode first = (GraphNode) c.toArray()[0];
            GraphNode second = (GraphNode) c.toArray()[1];
            Vector v1 = (Vector) _labels.get(first);
            Vector v2 = (Vector) _labels.get(second);

            Label low1 = (Label) v1.get(0);
            Label low2 = (Label) v2.get(0);

            for (Iterator i = v1.iterator(); i.hasNext();) {
                Label l = (Label) i.next();

                if (l.level() < low1.level()) {
                    low1 = l;
                }
            }

            for (Iterator i = v2.iterator(); i.hasNext();) {
                Label l = (Label) i.next();

                if (l.level() < low2.level()) {
                    low2 = l;
                }
            }

            if (low1.level() > low2.level()) { //Swap

                GraphNode temp = first;
                first = second;
                second = temp;
                low1 = low2;
            }

            //So now, low1 holds the label for the mux, and first is the corresponding GraphNode
            MuxNode mux = new MuxNode(first, second, low1);

            v1.addAll(v2);
            _labels.put(mux, v1);
            _labels.remove(first);
            _labels.remove(second);

            //Add appropriate arcs for new mux
            graph.addNodeWeight(mux);
            graph.addEdge(first, mux, new Boolean(low1.branch()).toString());
            graph.addEdge(second, mux, new Boolean(!low1.branch()).toString());
            graph.addEdge(low1.getSuperBlock(), mux,
                    ((IfStmt) low1.getSuperBlock()._block.getTail()).getCondition()
                    .toString());
            graph.addEdge(mux, this);

            Node n1 = (Node) graph.node(first);
            Node n2 = (Node) graph.node(this);
            graph.removeEdge((Edge) graph.neighborEdges(n1, n2).toArray()[0]);

            n1 = (Node) graph.node(second);
            n2 = (Node) graph.node(this);
            graph.removeEdge((Edge) graph.neighborEdges(n1, n2).toArray()[0]);
        }
    }

    protected Vector _primaryInputs;
    protected Vector _primaryOutputs;

    /**
     * A mapping from SuperBlocks to Vectors.  The keys indicate which SuperBlock the label comes
     * from.  The value is a Vector of Labels, which are all the labels coming from the key
     * SuperBlock.
     */
    protected Map _labels;
    protected DirectedGraph _graph;
    protected Block _block;
}
