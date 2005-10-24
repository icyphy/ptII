/*

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

import soot.*;

import soot.jimple.*;

import soot.toolkits.graph.Block;

import ptolemy.copernicus.jhdl.util.*;
import ptolemy.graph.*;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// ValueMap

/**
 *
 * This is a HashMap that maps Soot Values found within a
 * SootBlockDirectedGraph to lists of Nodes. The purpose of a ValueMap
 * is to determine which "Values" within a graph are assigned or
 * active. Note that the Nodes contained within these Lists are not
 * necessarily representative of the underlying graph - the Values and
 * Nodes represent a subset of the Values/Nodes within a graph.
 *
 * Any changes in the underlying graph will not be reflected in
 * the ValueMap. For example, adding or removing Nodes in the
 * underlying graph will have no impact on the ValueMap.
 * However, methods within the ValueMap may be used to modify the
 * underlying graph. For example, there are several methods for
 * adding Value-weighted Nodes within the graph while simultaneously
 * updating the mapping between Values and Nodes.

 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class ValueMap extends HashListMap {
    /**
     * This constructor will initialize an empty HashListMap. This
     * will <em>not</em> add any Nodes or Values to the
     * list. Nodes/Values must be added to the list with the following
     * methods:
     * <ul>
     * <li>addValueNode(Node)
     * <li>addValueNode(Value)
     * <li>getOrAddValueNode(Value)
     * <li>updateMap
     * <li>mergeSerial
     * </ul>
     *
     **/
    public ValueMap(SootBlockDirectedGraph graph) {
        super();
        _graph = graph;
    }

    /**
     * This constructor will create a new ValueMap that is identical
     * to the ValueMap argument. This constructor will call the
     * copy constructor of the super HashListMap. This ValueMap copy
     * will have a different HashMap than that of the argument. This
     * will insure that changes to the copy are <em>not</em> reflected
     * in the origional.
     **/
    public ValueMap(ValueMap vm) {
        super(vm);
        _graph = vm._graph;
    }

    /**
     * This method will create a copy of the ValueMap object. This
     * copy is created by calling the copy constructor.
     **/
    public Object clone() {
        return new ValueMap(this);
    }

    /**
     * Returns the underlying graph used by this ValueMap.
     **/
    public DirectedGraph getGraph() {
        return _graph;
    }

    /**
     * This method will return a Collection that includes all of the
     * Values found within this ValueMap. Note that this may not
     * include all Values contained within the graph.
     *
     * This method calls the keySet() method of the super HashListMap
     * class.
     **/
    public Collection getValues() {
        return keySet();
    }

    /**
     * This method returns a Collection of all Nodes found within this
     * ValueMap. Note that this method will <em>not</em> return all
     * the Nodes in the graph. This method will iterate through the
     * Nodes found within the HashListMap data structure and collect
     * them into a single Collection.
     **/
    public Collection getNodes() {
        Vector nodes = new Vector(_graph.nodeCount());
        Collection values = getValues();

        for (Iterator i = values.iterator(); i.hasNext();) {
            Value v = (Value) i.next();
            nodes.addAll(getNodes(v));
        }

        return nodes;
    }

    /**
     * This method will return all of the Nodes associated within a
     * given Value. This method calls the super HashListMap class
     * getList(Object) method to obtain this Collection.
     * Note that this method will not return all Nodes within the
     * graph that have the given Value as a weight - only internal
     * HashListMap structures are used to collect the appropriate
     * Nodes.
     **/
    public Collection getNodes(Value v) {
        return getList(v);
    }

    /**
     * This method will add a new Value weighted Node to the
     * underlying graph. The weight of the Node is the Value
     * argument.
     *
     * In addition, this method will add a mapping in the
     * HashListMap between the Value and the new Node. If the
     * Value argument does not exist as a key in the HashListMap,
     * it is added to the HashListMap as a key and the Node as the
     * value. If the Value argument does exist, the new Node is added
     * to the end of the List mapped to the Value arguement. This
     * means that the most recent Node associated with this Value is
     * the Node that was recently created.<p>
     *
     * Value arguments of type InstanceFieldRef (referred to as IFR)
     * are treated slightly differently than other Value
     * arguments. Unlike Local Values, InstanceFieldRef objects are
     * unique for each instance within a Soot Block. In many cases,
     * differing InstanceFieldRef objects represent the same Value
     * (i.e. two different IFRs have the same base). When
     * InstanceFieldRef objects are passed in, this method will search
     * through the Lists for InstanceFieldRef objects that may match
     * the given argument. If an IFR is found that matches an
     * argument, the new Node is created with the matching IFR rather
     * than the IFR argument.
     *
     * This is the primary way of adding Value weighted Nodes to the
     * graph. Several other methods in the class use this method to add new
     * Value weighted Nodes to the graph.
     *
     **/
    public Node addValueNode(Value v) {
        Value nodeValue = v;

        if (v instanceof InstanceFieldRef) {
            InstanceFieldRef ifr = getMatchingInstanceFieldRef((InstanceFieldRef) v);

            if (ifr != null) {
                nodeValue = ifr;
            }
        }

        Node n = _graph.addNodeWeight(nodeValue);
        add(nodeValue, n);

        if (DEBUG) {
            System.out.print("ValueMap:Adding node=" + n + " with weight "
                + nodeValue + " of type " + nodeValue.getClass().getName()
                + "\n");
        }

        return n;
    }

    /**
     * This method will add a new Value weighted Node to the graph
     * using the weight of the Node passed in as an argument. If the
     * given Node does not have a weight or the weight is not of
     * type Value, this method will not add a Node and return a null.
     **/
    public Node addValueNode(Node n) {
        if (n.hasWeight()) {
            Object o = n.getWeight();

            if (o instanceof Value) {
                return addValueNode((Value) n.getWeight());
            }
        }

        return null;
    }

    /**
     * This method will search all Nodes within the ValueMap (not the
     * graph) for a matching InstanceFieldRef object. If a match is
     * found, it is returned. If no match is found, this method will
     * return null.
     *
     * This method searches the Collection of Nodes obtained from the
     * getNodes() method. This method will <em>not</em> search the
     * entire graph.
     *
     * The method equalIFR is used to determine a match between two
     * InstanceFieldRef objects.
     *
     * @see equalIFR(InstanceFieldRef,InstanceFieldRef)
     **/
    public InstanceFieldRef getMatchingInstanceFieldRef(InstanceFieldRef ifr) {
        // Iterate through all nodes in the graph and see if there
        // is a matching InstanceFieldRef (i.e. same base and
        // same field).
        InstanceFieldRef dupIfr = null;

        //        for (Iterator i = _graph.nodes().iterator();i.hasNext();) {
        for (Iterator i = getNodes().iterator(); i.hasNext();) {
            Node n = (Node) i.next();

            if (n.getWeight() instanceof InstanceFieldRef) {
                InstanceFieldRef t_ifr = (InstanceFieldRef) n.getWeight();

                if (equalIFR(t_ifr, ifr) && (ifr != t_ifr)) {
                    dupIfr = t_ifr;
                }
            }
        }

        return dupIfr;
    }

    /**
     * This method will determine whether two InstanceFieldRef (IFR)
     * objects are equal. Two IFR objects are considered equal if the
     * base (as returned by the InstanceFieldRef.getBase() method) of
     * each object is the same (i.e. the base object associated with
     * each IFR is the same).
     *
     * @see getMatchingInstanceFieldRef(InstanceFieldRef)
     **/

    // TODO: This method may need to go in a different class (more
    // general than this class)
    public static boolean equalIFR(InstanceFieldRef ifr1, InstanceFieldRef ifr2) {
        return ifr1.getBase().equals(ifr2.getBase())
                && ifr1.getField().equals(ifr2.getField());
    }

    /**
     * This method will return the Node in the graph most recently
     * associated with the given Value. Since a Value object may be
     * mapped to multiple Nodes, only the last Node added to the list
     * will be returned. If no Nodes are associated with the given
     * Value, null is returned.
     *
     * If the Value is an InstanceFieldRef (IFR), this method will
     * search the HashListMap for a matching IFR. The Node associated
     * with the matching IFR will be returned if it exists.
     *
     * @see getMatchingInstanceFieldRef(InstanceFieldRef)
     **/
    public Node getValueNode(Value v) {
        if (containsKey(v)) {
            return (Node) getLast(v);
        }

        if (v instanceof InstanceFieldRef) {
            InstanceFieldRef ifr = getMatchingInstanceFieldRef((InstanceFieldRef) v);
            Node ifrNode = (Node) getLast(ifr);

            //add(v,n);
            return ifrNode;
        }

        return null;
    }

    /**
     * This method will search the HashListMap using the getValueNode
     * method and return the Node that is found. If no Node is found,
     * a new Node is created using the addValueNode method.
     *
     * @see getValueNode(Value)
     * @see addValueNode(Value)
     **/
    public Node getOrAddValueNode(Value v) {
        Node n = getValueNode(v);

        if (n == null) {
            return addValueNode(v);
        } else {
            return n;
        }
    }

    /**
     * This method will return true of the Value is of type
     * Local or InstanceFieldRef.
     **/

    // TODO: Put this in a more generic Soot class?
    public boolean isVariableValue(Value v) {
        if (v instanceof Local || v instanceof InstanceFieldRef) {
            return true;
        }

        return false;
    }

    /**
     * This method will return a Collection of Values in this ValueMap
     * that are variables (Value of type Local and InstanceFieldRef).
     *
     * @see isVariableValue(Value)
     **/
    public Collection getVariableValues() {
        Collection values = getValues();
        Vector variableValues = new Vector(values.size());

        for (Iterator i = values.iterator(); i.hasNext();) {
            Value v = (Value) i.next();

            if (isVariableValue(v)) {
                variableValues.add(v);
            }
        }

        return variableValues;
    }

    /**
     * This method will return true if the given Node is assigned
     * within this ValueMap. A Node is assigned if the following
     * conditions are true:
     * <ul>
     * <li> The Node has a weight of type Value
     * <li> The Value argument exists in the ValueMap (Note that Node
     * may be assigned in the graph, but it may not be assigned in the
     * given ValueMap).
     * <li> The Value is a variable Value (Local or InstanceFieldRef)
     * <li> The Local weighted Node has one input edge (source) or the
     * InstanceFieldRef weighted Node has two edges (base and source)
     * </ul>
     **/
    public boolean isAssigned(Node n) {
        if (!n.hasWeight()) {
            return false;
        }

        Object o = n.getWeight();

        if (!(o instanceof Value)) {
            return false;
        }

        //Value v = (Value) n.getWeight();
        Value v = (Value) o;

        // See if the Value associated with the given Node
        if (!containsKey(v)) {
            return false;
        }

        if (!isVariableValue(v)) {
            return false;
        }

        if (v instanceof Local && (_graph.inputEdgeCount(n) == 1)) {
            return true;
        }

        if (v instanceof InstanceFieldRef && (_graph.inputEdgeCount(n) == 2)) {
            return true;
        }

        return false;
    }

    /**
     * This method will return a Collection of Nodes that are assigned
     * within this ValueMap. This method will only provide the last
     * Nodes assigned by the method and not include upstream
     * assignments in the dataflow graph.
     **/

    // TODO: rename method? getLastAssignedNodes?
    public Collection getAssignedNodes() {
        Collection c = getAssignedValues();
        Collection an = new Vector(c.size());

        for (Iterator i = c.iterator(); i.hasNext();) {
            Value v = (Value) i.next();
            Node n = getValueNode(v);
            an.add(n);
        }

        /*
        // This code will get all assigned nodes and not just
        // the most recently assigned nodes
        Collection c = getNodes();
        Collection an = new Vector(c.size());
        for (Iterator i = c.iterator();i.hasNext();) {
        Node n = (Node) i.next();
        if (isAssigned(n))
        an.add(n);
        }
        */
        return an;
    }

    public Collection getAssignedValues() {
        Collection vs = getVariableValues();
        Collection av = new UniqueVector(vs.size());

        for (Iterator i = vs.iterator(); i.hasNext();) {
            Value v = (Value) i.next();

            for (Iterator j = getNodes(v).iterator(); j.hasNext();) {
                Node n = (Node) j.next();

                if (isAssigned(n)) {
                    av.add(v);
                }
            }
        }

        return av;
    }

    /**
     * This method will clear all mappings within the ValueMap and
     * rebuild the mappings based on the topology of the underlying
     * graph.
     *
     * This method will iterate through all graph Nodes in a
     * topological order. A new mapping will be added for each Value
     * weighted Node in the graph. Nodes with non-Value weights will
     * not have a mapping.
     *
     **/
    public void updateMap() {
        // Clear all mappings
        clear();

        // Iterate over all Nodes within the graph (in topological order)
        List topologicalOrder = null;

        try {
            topologicalOrder = _graph.topologicalSort(_graph.nodes());
        } catch (GraphActionException e) {
        }

        for (Iterator i = topologicalOrder.iterator(); i.hasNext();) {
            Node n = (Node) i.next();

            if (n.hasWeight()) {
                Object o = n.getWeight();

                if (o instanceof Value) {
                    add(o, n);
                }
            }
        }
    }

    /**
     * This method will merge a data flow graph as a successor of
     * the current graph. Rather than passing the graph itself, the
     * ValueMap of the graph is passed in. The ValueMap is used
     * to decide which nodes in the successor graph are driven by
     * nodes in this graph.
     **/
    public void mergeSerial(ValueMap successor) {
        DirectedGraph succeedingGraph = successor.getGraph();

        // temporary hashmap between old nodes & new.
        // Used when connecting edges.
        HashMap nodeMap = new HashMap();

        // Add all nodes from dfg to graph
        for (Iterator i = succeedingGraph.nodes().iterator(); i.hasNext();) {
            Node node = (Node) i.next();

            if (DEBUG) {
                System.out.print("ValueMap:Merging node=" + node
                    + " with weight " + node.getWeight() + " of type "
                    + node.getWeight().getClass().getName() + "\n");
            }

            Object nodeWeight = node.getWeight();
            Node newNode = null;

            if (nodeWeight instanceof Value) {
                // Some nodes don't have a Value weight (binary mux node)
                Value nodeValue = (Value) nodeWeight;

                if (!successor.isAssigned(node)) {
                    newNode = this.getValueNode(nodeValue);
                }

                if (newNode == null) {
                    newNode = this.addValueNode(nodeValue);
                }
            } else {
                // create a new non-Value weighted Node
                newNode = _graph.addNodeWeight(nodeWeight);
            }

            nodeMap.put(node, newNode);
        }

        // Iterate through all edges and add to graph
        for (Iterator i = succeedingGraph.edges().iterator(); i.hasNext();) {
            Edge e = (Edge) i.next();
            Node src = e.source();

            if (nodeMap.containsKey(src)) {
                src = (Node) nodeMap.get(src);
            }

            Node snk = e.sink();

            if (nodeMap.containsKey(snk)) {
                snk = (Node) nodeMap.get(snk);
            }

            // Check and see if the current graph already has an
            // edge between the two nodes. If not, add the edge.
            if (_graph.successorEdges(src, snk).size() == 0) {
                if (e.hasWeight()) {
                    _graph.addEdge(src, snk, e.getWeight());
                } else {
                    _graph.addEdge(src, snk);
                }
            }
        }
    }

    /**
     * A debugging flag. This can be set to true to enable a number of
     * verbose debugging messages to standard output.
     **/
    public static boolean DEBUG = false;

    /**
     * This is the underlying graph for which the Nodes and Values are
     * found. Note that the Nodes/Values held within this HashMap do
     * not necessarily include all the Nodes in the graph.
     **/
    protected SootBlockDirectedGraph _graph;
}
