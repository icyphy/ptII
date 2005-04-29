/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package diva.graph.basic;

import java.util.ArrayList;
import java.util.Iterator;

import diva.graph.modular.BasicModularGraphModel;
import diva.graph.modular.CompositeNode;
import diva.graph.modular.Edge;
import diva.graph.modular.Graph;
import diva.graph.modular.Node;
import diva.util.ArrayIterator;
import diva.util.BasicPropertyContainer;
import diva.util.SemanticObjectContainer;


/**
 * A basic implementation of a mutable graph model that stores its
 * graph structure as a collection of nodes and edges (as opposed to
 * an adjacency matrix).  This class is a good reference class for how
 * to use the diva.graph.modular classes and will suffice as a data
 * structure for simple, throw-away applications (a heavy-weight
 * application like a schematic editor will probably want to implement
 * things differently.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class BasicGraphModel extends BasicModularGraphModel {
    /**
     * Construct an empty graph model.
     */
    public BasicGraphModel() {
        super(new BasicCompositeNode(null));
    }

    /**
     * Make a new composite node object.
     */
    public CompositeNode createComposite(Object semanticObject) {
        return new BasicCompositeNode(semanticObject);
    }

    /**
     * Make a new edge object.
     */
    public Edge createEdge(Object semanticObject) {
        return new BasicEdge(semanticObject);
    }

    /**
     * Make a new node object.
     */
    public Node createNode(Object semanticObject) {
        return new BasicNode(semanticObject);
    }

    /**
     * A class that represents an intermediary
     * between a semantic object and a visual
     * representation.
     */
    private static abstract class Intermediate extends BasicPropertyContainer
        implements SemanticObjectContainer {
        /**
         * The semantic object of this intermediate.
         */
        private Object _semanticObject = null;

        /**
         * Set the semantic object of this object, i.e.  the
         * application object that is semantically equivalent to this
         * node.
         */
        public Object getSemanticObject() {
            return _semanticObject;
        }

        /**
         * Set the semantic object of this object, i.e.  the
         * application object that is semantically equivalent to this
         * node.
         */
        public void setSemanticObject(Object o) {
            _semanticObject = o;
        }
    }

    /**
     * A simple node implementation.
     */
    private static class BasicNode extends Intermediate implements Node {
        /**
         * The edges <b>into</b> this node.
         */
        private ArrayList _in = new ArrayList();

        /**
         * The edges <b>out of</b> this node.
         */
        private ArrayList _out = new ArrayList();

        /**
         * The parent of this node.
         */
        private CompositeNode _parent = null;

        /**
         * Create a node containing some user data.
         */
        public BasicNode(Object userObject) {
            this.setSemanticObject(userObject);
        }

        public void addInEdge(Edge e) {
            _in.add(e);
        }

        public void addOutEdge(Edge e) {
            _out.add(e);
        }

        public Graph getParent() {
            return _parent;
        }

        /**
         * Return an iterator over the <i>in</i> edges of this
         * node. This iterator does not support removal operations.
         */
        public Iterator inEdges() {
            return new ArrayIterator(_in.toArray());
        }

        /**
         * Return an iterator over the source, or "in", nodes of this
         * node. This iterator does not support removal operations.
         * This method provides a convenient way of traversing a
         * graph.
         */
        public Iterator inNodes() {
            return new diva.util.IteratorAdapter() {
                    Iterator edges = inEdges();

                    public boolean hasNext() {
                        return edges.hasNext();
                    }

                    public Object next() {
                        return ((Edge) edges.next()).getTail();
                    }
                };
        }

        /**
         * Return an iterator over the <i>out</i> edges of this
         * node.  This iterator does not support removal operations.
         */
        public Iterator outEdges() {
            return new ArrayIterator(_out.toArray());
        }

        /**
         * Return an iterator over the sink, or "out", nodes of this
         * node. This iterator does not support removal operations.
         * This method provides a convenient way of traversing a
         * graph.
         */
        public Iterator outNodes() {
            return new diva.util.IteratorAdapter() {
                    Iterator edges = outEdges();

                    public boolean hasNext() {
                        return edges.hasNext();
                    }

                    public Object next() {
                        return ((Edge) edges.next()).getHead();
                    }
                };
        }

        public void removeInEdge(Edge e) {
            _in.remove(e);
        }

        public void removeOutEdge(Edge e) {
            _out.remove(e);
        }

        public void setParent(Graph parent) {
            if (_parent != null) {
                ((BasicCompositeNode) _parent).remove(this);
            }

            _parent = (BasicCompositeNode) parent;

            if (_parent != null) {
                ((BasicCompositeNode) _parent).add(this);
            }
        }

        public String toString() {
            Object o = this.getSemanticObject();
            return "BasicNode[" + o + "]";
        }
    }

    /**
     * A simple composite node implementation.
     */
    private static class BasicCompositeNode extends BasicNode
        implements CompositeNode {
        /**
         * The nodes that this composite node
         * contains.
         */
        private ArrayList _nodes = new ArrayList();

        /**
         * Construct an empty composite nodes with
         * the given semantic object.
         */
        public BasicCompositeNode(Object userObject) {
            super(userObject);
        }

        public void add(Node n) {
            _nodes.add(n);
        }

        public boolean contains(Node n) {
            return _nodes.contains(n);
        }

        public int getNodeCount() {
            return _nodes.size();
        }

        public Node getNode(int i) {
            return (Node) _nodes.get(i);
        }

        public int getIndex(Node n) {
            return _nodes.indexOf(n);
        }

        public Iterator nodes() {
            return _nodes.iterator();
        }

        public void remove(Node n) {
            _nodes.remove(n);
        }

        public String toString() {
            Object o = this.getSemanticObject();
            return "CompositeNode[" + o + "]";
        }
    }

    /**
     * A basic implementation of the Edge interface.
     */
    private static class BasicEdge extends Intermediate implements Edge {
        /**
         * Whether or not this edge is directed.
         */
        private boolean _directed = true;

        /**
         * The head of the edge.
         */
        private Node _head = null;

        /**
         * The tail of the edge.
         */
        private Node _tail = null;

        /**
         * The weight on this edge.
         */
        private double _weight = 1.0;

        /**
         * Create a new edge with the specified user object
         * but no tail or head.
         */
        public BasicEdge(Object userObject) {
            this(userObject, null, null);
        }

        /**
         * Create a new edge with the specified user object
         * and tail/head
         */
        public BasicEdge(Object userObject, Node tail, Node head) {
            this.setSemanticObject(userObject);
            setTail(tail);
            setHead(head);
        }

        public boolean acceptHead(Node head) {
            return true;
        }

        public boolean acceptTail(Node tail) {
            return true;
        }

        public Node getHead() {
            return _head;
        }

        public Node getTail() {
            return _tail;
        }

        public double getWeight() {
            return _weight;
        }

        public boolean isDirected() {
            return _directed;
        }

        public void setDirected(boolean val) {
            _directed = val;
        }

        public void setHead(Node n) {
            if (_head != null) {
                ((BasicNode) _head).removeInEdge(this);
            }

            _head = n;

            if (_head != null) {
                ((BasicNode) _head).addInEdge(this);
            }
        }

        public void setTail(Node n) {
            if (_tail != null) {
                ((BasicNode) _tail).removeOutEdge(this);
            }

            _tail = n;

            if (_tail != null) {
                ((BasicNode) _tail).addOutEdge(this);
            }
        }

        public void setWeight(double weight) {
            _weight = weight;
        }

        /** Print a readable description of this edge
         */
        public String toString() {
            Object o = this.getSemanticObject();
            return "Edge[" + o + "]";
        }
    }
}
