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
package ptolemy.copernicus.jhdl.util;

import soot.*;

import soot.jimple.*;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.copernicus.java.ModelTransformer;
import ptolemy.copernicus.jhdl.soot.*;
import ptolemy.graph.*;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
////

/**

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
@Pt.ProposedRating Red (cxh)
@Pt.AcceptedRating Red (cxh)
*/
public class ActorModelGraph extends ModelGraph {
    public ActorModelGraph(AtomicActor entity, Map options) {
        super(entity);

        // Get names & Soot objects
        String className = ModelTransformer.getInstanceClassName(entity, options);
        String entityClassName = entity.getClass().getName();
        System.out.println("Creating graph for class " + className
            + " (entity=" + entityClassName + ")");

        SootClass entityClass = Scene.v().loadClassAndSupport(className);
        SootMethod method = entityClass.getMethodByName("fire");

        // Get internal graph
        DirectedGraph fireGraph = null;

        try {
            fireGraph = new IntervalBlockDirectedGraph(method);
        } catch (IllegalActionException e) {
            System.err.println("Error " + e);
            e.printStackTrace();
            System.exit(1);
        }

        PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
        dgToDotty.writeDotFile(".", "entity", fireGraph);

        //
        Map portCallNodes = _getPortCallNodes(entity, fireGraph);
        dgToDotty = new PtDirectedGraphToDotty();
        dgToDotty.writeDotFile(".", "fixed", fireGraph);

        addGraph(fireGraph);

        // Add IOPort nodes
        for (Iterator i = portCallNodes.keySet().iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();
            Node topLevelPortNode = addIOPortNode(port);

            // get the list of entity Nodes associated with this Port
            List nodeList = (List) portCallNodes.get(port);

            // iterate over all nodes in the list
            for (Iterator j = nodeList.iterator(); j.hasNext();) {
                Node n = (Node) j.next();

                if (port.isInput()) {
                    addEdge(topLevelPortNode, n);
                } else {
                    addEdge(n, topLevelPortNode);
                }
            }
        }

        dgToDotty = new PtDirectedGraphToDotty();
        dgToDotty.writeDotFile(".", "fixedports", this);
    }

    // This method will identify all Nodes within the graph that
    // are associated with Port calls for each IOPort in the actor.
    // The key to the map is a IOPort and the Value is a List of
    // Nodes
    protected Map _getPortCallNodes(Actor actor, DirectedGraph entityGraph) {
        // Map from an IOPort to a node in the graph. The key in this
        // Map is a IOPort and the Value is a List of Nodes. (in most
        // cases, this List has only one entry).
        Map portNodeMap = new HashMap();

        // The following section of code will create a mapping between
        // the name of each IOPort and the IOPort itself. This will be
        // used later to match calls to the IOPort in the graph to
        // actual IOPort objects
        List inputPortList = actor.inputPortList();
        List outputPortList = actor.outputPortList();
        Map stringPortMap = new HashMap(inputPortList.size()
                + outputPortList.size());

        for (Iterator i = inputPortList.iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();
            String portName = port.getName();
            stringPortMap.put(portName, port);

            //System.out.println("Port="+portName+" is "+port);
        }

        for (Iterator i = outputPortList.iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();
            String portName = port.getName();
            stringPortMap.put(portName, port);

            //System.out.println("Port="+portName+" is "+port);
        }

        // This code will iterate over each Node in the graph and
        // search for VirtualInvokeExpr objects (i.e. searching
        // for get and send calls on the IOPort objects).
        // If a VirtualInvokeExpr object is found and the methodName
        // is "getInt" or "sendInt", then find the Nodes associated
        // with this port call.
        // Note that there may be more than one Node associated with
        // this port call (i.e. different control paths). All Nodes
        // associated with the IOPort call are Mapped to the portNodeMap.
        for (Iterator entityNodes = entityGraph.nodes().iterator();
                    entityNodes.hasNext();) {
            Node node = (Node) entityNodes.next();
            Object weight = node.getWeight();

            // See if weight is of type virtualinvoke
            if (!(weight instanceof VirtualInvokeExpr)) {
                continue;
            }

            VirtualInvokeExpr expr = (VirtualInvokeExpr) weight;

            // See if the invoke calls the appropriate method
            String methodName = expr.getMethod().getName();

            //System.out.println("   VirtualInvoke="+methodName);
            if (!methodName.equals("getInt") && !methodName.equals("sendInt")) {
                continue;
            }

            FieldRef ref = _getFieldRef(entityGraph, node);
            SootField field = ref.getField();
            String portName = field.getName();

            //                System.out.println("Found port node "+node+ " with methodname="+
            //                                   methodName + " portname="+portName);
            IOPort port = (IOPort) stringPortMap.get(portName);

            //              System.out.println("Found port call "+port);
            Node portNode = null;

            if (methodName.equals("getInt")) {
                // "getInt" method call
                // In this case, the successor to the getInt call is
                // identified as the Node associated with this port
                // call (i.e. input port)
                portNode = (Node) entityGraph.successors(node).iterator().next();
            } else {
                // "sendInt" method call
                // In this case, the predecessor to this sendInt call
                // is identified as the Node associated with this port
                // call (i.e. output port)
                for (Iterator i = entityGraph.predecessors(node).iterator();
                            i.hasNext();) {
                    Node n = (Node) i.next();
                    Edge e = (Edge) entityGraph.predecessorEdges(node, n)
                                                       .iterator().next();

                    if (e.hasWeight()) {
                        portNode = n;
                    }
                }
            }

            List nodeList = (List) portNodeMap.get(port);

            if (nodeList == null) {
                nodeList = new Vector();
                portNodeMap.put(port, nodeList);
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
        for (Iterator i = portNodeMap.keySet().iterator(); i.hasNext();) {
            IOPort port = (IOPort) i.next();

            //System.out.println("Port="+port);
            if (port.isInput()) {
                // inputs
                List l = (List) portNodeMap.get(port);

                for (Iterator j = l.iterator(); j.hasNext();) {
                    Node node = (Node) j.next();
                    Node predecessor = (Node) entityGraph.predecessors(node)
                                                                 .iterator()
                                                                 .next();
                    _deleteLeafBranch(entityGraph, predecessor);
                }
            } else {
                // outputs
                List l = (List) portNodeMap.get(port);

                for (Iterator j = l.iterator(); j.hasNext();) {
                    Node node = (Node) j.next();

                    // find successors
                    Node successor = (Node) entityGraph.successors(node)
                                                               .iterator().next();

                    // remove edge from node to sucessor
                    Edge e = (Edge) entityGraph.successorEdges(node, successor)
                                                       .iterator().next();
                    entityGraph.removeEdge(e);

                    // Remove branch
                    _deleteLeafBranch(entityGraph, successor);
                }
            }
        }

        PtDirectedGraphToDotty toDotty = new PtDirectedGraphToDotty();
        toDotty.writeDotFile(".", "fixedentity", entityGraph);

        return portNodeMap;
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

        for (Iterator i = graph.predecessors(node).iterator(); i.hasNext();) {
            Node n = (Node) i.next();

            //              System.out.println("Node="+node+" pred="+n);
            Edge e = (Edge) graph.predecessorEdges(node, n).iterator().next();

            if (!e.hasWeight()) {
                predecessor = n;
            }
        }

        Node fieldRefNode = (Node) graph.predecessors(predecessor).iterator()
                                                .next();

        return (FieldRef) fieldRefNode.getWeight();
    }

    protected void _deleteLeafBranch(DirectedGraph graph, Node node) {
        Collection predecssors = graph.predecessors(node);

        if (predecssors.size() == 1) {
            _deleteLeafBranch(graph, (Node) predecssors.iterator().next());
        }

        //System.out.println("Deleting node="+node);
        graph.removeNode(node);
    }

    public AtomicActor getAtomicActor() {
        return (AtomicActor) getEntity();
    }
}
