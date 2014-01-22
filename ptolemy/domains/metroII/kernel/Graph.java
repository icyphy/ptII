/* Graph is an adjacency list data structure for representing mapping constraints.

 Copyright (c) 2012-2013 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////////
//// Graph

/**
 * 
 * Graph is an adjacency list data structure for representing mapping
 * constraints.
 * 
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 * 
 */
public class Graph implements Cloneable {

    /**
     * Constructs a linked list.
     */
    public Graph() {
        _nodeConnection = new ArrayList<ArrayList<Integer>>();
        _edge = new ArrayList<Pair<Integer, Integer>>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Clones the linked list.
     * 
     * @exception CloneNotSupportedException
     *                the object's class does not implement the Cloneable
     *                interface.
     */
    @Override
    public Graph clone() throws CloneNotSupportedException {
        Graph newObject = (Graph) super.clone();
        newObject._nodeConnection = (ArrayList<ArrayList<Integer>>) _nodeConnection
                .clone();
        newObject._edge = (ArrayList<Pair<Integer, Integer>>) _edge.clone();
        return newObject;
    }

    /**
     * Clears the linked list to the initial state. Remove all the mapping
     * constraints.
     */
    public void clear() {
        _nodeConnection.clear();
        _edge.clear();
    }

    /**
     * Checks if there is a mapping constraint constraining event id1 and event
     * id2.
     * 
     * @param id1
     *            ID of the first event.
     * @param id2
     *            ID of the second event.
     * @return true if there is a mapping constraint.
     */
    public boolean contains(int id1, int id2) {
        if (id1 >= _nodeConnection.size() || id2 >= _nodeConnection.size()) {
            return false;
        }
        // System.out.println(id1+" "+id2);
        for (Integer edgeId : _nodeConnection.get(id1)) {
            // System.out.println(_edge.get(edgeId).getFirst().intValue()+":"+_edge.get(edgeId).getSecond().intValue());
            if (_edge.get(edgeId).getFirst().intValue() == id2
                    || _edge.get(edgeId).getSecond().intValue() == id2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a mapping constraint (A, B). Event A and B are given by id1 and id2.
     * 
     * @param id1
     *            ID of event A in the constraint
     * @param id2
     *            ID of event B in the constraint
     */
    public void add(int id1, int id2) {
        if (contains(id1, id2)) {
            return;
        }
        // System.out.println("adding "+id1+" "+id2);
        _edge.add(new Pair(id1, id2));
        int largerId = id1;
        if (id2 > id1) {
            largerId = id2;
        }
        while (_nodeConnection.size() - 1 < largerId) {
            _nodeConnection.add(new ArrayList<Integer>());
        }
        _nodeConnection.get(id1).add(_edge.size() - 1);
        _nodeConnection.get(id2).add(_edge.size() - 1);
    }

    /**
     * Gets the iterator of the edges associated with nodeId.
     * 
     * @param nodeId
     *            ID of the node
     * @return An iterator of the edges connected to nodeId.
     */
    public Iterable<Integer> getEdges(int nodeId) {
        return _nodeConnection.get(nodeId);
    }

    /**
     * Return the size of nodes.
     * 
     * @return the size of nodes.
     */
    public int nodeSize() {
        return _nodeConnection.size();
    }

    /**
     * Returns the size of edges.
     * 
     * @return the size of edges.
     */
    public int edgeSize() {
        return _edge.size();
    }

    /**
     * Gets the edge with the given edge ID.
     * 
     * @param edgeId
     *            the edge Id.
     * @return the edge.
     */
    public Pair<Integer, Integer> getEdge(int edgeId) {
        return _edge.get(edgeId);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /**
     * The adjacency list that represents the mapping constraints (event pairs).
     * _nodeConnection.get(id) gives a list of edges connected with node ID id.
     */
    private ArrayList<ArrayList<Integer>> _nodeConnection;

    /**
     * The list of edges. _edge.get(id) gives an edge with ID id.
     */
    private ArrayList<Pair<Integer, Integer>> _edge;

}
