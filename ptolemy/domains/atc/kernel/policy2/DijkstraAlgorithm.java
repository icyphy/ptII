/* Find the shortest path from a source to a destination.

   Copyright (c) 2015-2016 The Regents of the University of California.
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

package ptolemy.domains.atc.kernel.policy2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;

/**
 * Find the shortest path from a source to a destination.
 * For this purpose each edge has a weight.
 * If the destination of this edge is a stormy track the weight is 6.
 * If it is an occupy track,
 * the weight is 5 and if has both conditions, weight is 11.
 * Else the weight of the edge is 1;
 *
 *  @author Maryam Bagheri
 *  @version $Id$
 *  @since Ptolemy II 11.0
 */
public class DijkstraAlgorithm {

    /** Instantiate. */
    public DijkstraAlgorithm() {
        _nodes = new ArrayList<Vertex>();
        _edges = new ArrayList<Edge>();
    }

    /** Execute the Dijkstra algorithm.
     *  @param source The source id.
     */
    public void execute(Vertex source) {
        _settledNodes = new HashSet<Vertex>();
        _unsettledNodes = new HashSet<Vertex>();
        _distance = new HashMap<Vertex, Integer>();
        _predecessors = new HashMap<Vertex, Vertex>();
        _distance.put(source, 0);
        _unsettledNodes.add(source);
        while (_unsettledNodes.size() > 0) {
            Vertex node = _getMinimum(_unsettledNodes);
            _settledNodes.add(node);
            _unsettledNodes.remove(node);
            _findMinimalDistances(node);
        }
    }

    /** Call the Dijkstra algorithm.
     *  @param neighbors The map of neighbors.
     *  @param airportsId The airports
     *  @param source The source
     *  @param destination The destination
     *  @param stormyTracks The map of storm tracks.
     *  @param inTransit the map of in transit objects.
     *  @return an array of tokens
     */
    public Token[] callDijkstra(Map<Integer, ArrayToken> neighbors, ArrayList<Integer> airportsId,
            int source, int destination, Map<Integer, Token> stormyTracks, Map<Integer, Boolean> inTransit) {
        int k=0;
        for (Entry<Integer, ArrayToken> entry : neighbors.entrySet()) {
            int node=entry.getKey();
            Vertex location=new Vertex(node);
            _nodes.add(location);
            ArrayToken nodeNeighbors=entry.getValue();
            for (int i=0;i<nodeNeighbors.length();i++) {
                int id=((IntToken)nodeNeighbors.getElement(i)).intValue();
                if (id!=-1) {
                    int weight=1;
                    if (inTransit.containsKey(id))
                        if (inTransit.get(id)==true)
                            weight=5;
                    if (stormyTracks.containsKey(id))
                        if (((BooleanToken)stormyTracks.get(id)).booleanValue()==true)
                            weight+=5;
                    _addLane(k++,node,id,weight);
                }
            }
        }

        for (int i = 0; i < airportsId.size(); i++) {
            Vertex location = new Vertex(airportsId.get(i));
            _nodes.add(location);
        }

        execute(new Vertex(source));
        LinkedList<Vertex> path = getPath(new Vertex(destination));
        if (path==null) {
            return null;
        } else {
            Token[] newFlightMap=new Token[path.size()];
            for (int i=0;i<path.size();i++) {
                newFlightMap[i]=(Token) new IntToken(path.get(i).getId());
            }
            // ArrayToken flightMap=new ArrayToken(BaseType.INT, newFlightMap);
            return newFlightMap;
        }

    }

    /** Return the path from the source to the selected target.  Return null
     *  if no path exists.
     *  @param target The target.
     *  @return the path to the target or null.
     */
    public LinkedList<Vertex> getPath(Vertex target) {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;
        // check if a path exists
        if (_predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (_predecessors.get(step) != null) {
            step = _predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }


    ///////////////////////////////////////////////////////////////////
    ////                     private method                        ////

    private void _addLane(int laneId, int sourceLocNo, int destLocNo, int duration) {
        Edge lane = new Edge(laneId,new Vertex(sourceLocNo), new Vertex(destLocNo), duration);
        _edges.add(lane);
    }

    private void _findMinimalDistances(Vertex node) {
        List<Vertex> adjacentNodes = _getNeighbors(node);
        for (Vertex target : adjacentNodes) {
            if (_getShortestDistance(target) > _getShortestDistance(node)
                    + _getDistance(node, target)) {
                _distance.put(target, _getShortestDistance(node)
                        + _getDistance(node, target));
                _predecessors.put(target, node);
                _unsettledNodes.add(target);
            }
        }

    }

    private int _getDistance(Vertex node, Vertex target) {
        for (Edge edge : _edges) {
            if (edge.getSource().equals(node)
                    && edge.getDestination().equals(target)) {
                return edge.getWeight();
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private Vertex _getMinimum(Set<Vertex> vertexes) {
        Vertex minimum = null;
        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (_getShortestDistance(vertex) < _getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private List<Vertex> _getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<Vertex>();
        for (Edge edge : _edges) {
            if (edge.getSource().equals(node)
                    && !_isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private int _getShortestDistance(Vertex destination) {
        Integer d = _distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    private boolean _isSettled(Vertex vertex) {
        return _settledNodes.contains(vertex);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    private  List<Vertex> _nodes;
    private  List<Edge> _edges;
    private Set<Vertex> _settledNodes;
    private Set<Vertex> _unsettledNodes;
    private Map<Vertex, Vertex> _predecessors;
    private Map<Vertex, Integer> _distance;
}
