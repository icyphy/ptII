/*This class used to find the shortest path from a source to a destination
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
import ptolemy.kernel.util.IllegalActionException;
/**
 * This class used to find the shortest path from a source to a destination.
 * For this purpose each edge has a weight.
 * If the destination of this edge is a stormy track the weight is 6.
 * If it is an occupy track,
 * the weight is 5 and if has both conditions, weight is 11.
 * Else the weight of the edge is 1;
 * @author maryam
 *
 */

public class DijkstraAlgorithm {

  private  List<Vertex> nodes;
  private  List<Edge> edges;
  private Set<Vertex> settledNodes;
  private Set<Vertex> unSettledNodes;
  private Map<Vertex, Vertex> predecessors;
  private Map<Vertex, Integer> distance;

  public DijkstraAlgorithm() {
    this.nodes = new ArrayList<Vertex>();
    this.edges = new ArrayList<Edge>();
  }

  public void execute(Vertex source) {
    settledNodes = new HashSet<Vertex>();
    unSettledNodes = new HashSet<Vertex>();
    distance = new HashMap<Vertex, Integer>();
    predecessors = new HashMap<Vertex, Vertex>();
    distance.put(source, 0);
    unSettledNodes.add(source);
    while (unSettledNodes.size() > 0) {
      Vertex node = getMinimum(unSettledNodes);
      settledNodes.add(node);
      unSettledNodes.remove(node);
      findMinimalDistances(node);
    }
  }

  private void findMinimalDistances(Vertex node) {
    List<Vertex> adjacentNodes = getNeighbors(node);
    for (Vertex target : adjacentNodes) {
      if (getShortestDistance(target) > getShortestDistance(node)
          + getDistance(node, target)) {
        distance.put(target, getShortestDistance(node)
            + getDistance(node, target));
        predecessors.put(target, node);
        unSettledNodes.add(target);
      }
    }

  }

  private int getDistance(Vertex node, Vertex target) {
    for (Edge edge : edges) {
      if (edge.getSource().equals(node)
          && edge.getDestination().equals(target)) {
        return edge.getWeight();
      }
    }
    throw new RuntimeException("Should not happen");
  }

  private List<Vertex> getNeighbors(Vertex node) {
    List<Vertex> neighbors = new ArrayList<Vertex>();
    for (Edge edge : edges) {
      if (edge.getSource().equals(node)
          && !isSettled(edge.getDestination())) {
        neighbors.add(edge.getDestination());
      }
    }
    return neighbors;
  }

  private Vertex getMinimum(Set<Vertex> vertexes) {
    Vertex minimum = null;
    for (Vertex vertex : vertexes) {
      if (minimum == null) {
        minimum = vertex;
      } else {
        if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
          minimum = vertex;
        }
      }
    }
    return minimum;
  }

  private boolean isSettled(Vertex vertex) {
    return settledNodes.contains(vertex);
  }

  private int getShortestDistance(Vertex destination) {
    Integer d = distance.get(destination);
    if (d == null) {
      return Integer.MAX_VALUE;
    } else {
      return d;
    }
  }

  /*
   * This method returns the path from the source to the selected target and
   * NULL if no path exists
   */
  public LinkedList<Vertex> getPath(Vertex target) {
    LinkedList<Vertex> path = new LinkedList<Vertex>();
    Vertex step = target;
    // check if a path exists
    if (predecessors.get(step) == null) {
      return null;
    }
    path.add(step);
    while (predecessors.get(step) != null) {
      step = predecessors.get(step);
      path.add(step);
    }
    // Put it into the correct order
    Collections.reverse(path);
    return path;
  }
  
  public Token[] callDijkstra(Map<Integer, ArrayToken> neighbors, ArrayList<Integer> airportsId,
          int source , int destination, Map<Integer, Token> stormyTracks, Map<Integer, Boolean> inTransit) throws IllegalActionException {
     int k=0;
     for(Entry<Integer, ArrayToken> entry : neighbors.entrySet()){
         int node=entry.getKey();
         Vertex location=new Vertex(node);
         nodes.add(location);
         ArrayToken nodeNeighbors=entry.getValue();
          for(int i=0;i<nodeNeighbors.length();i++){
              int id=((IntToken)nodeNeighbors.getElement(i)).intValue();
              if(id!=-1){
                  int weight=1;
                  if(inTransit.containsKey(id))
                      if(inTransit.get(id)==true)
                          weight=5;
                  if(stormyTracks.containsKey(id))
                      if(((BooleanToken)stormyTracks.get(id)).booleanValue()==true)
                          weight+=5;
                  addLane(k++,node,id,weight);
              }
          }      
     }

     for (int i = 0; i < airportsId.size(); i++) {
       Vertex location = new Vertex(airportsId.get(i));
       nodes.add(location);
     }
  
     execute(new Vertex(source));
     LinkedList<Vertex> path = getPath(new Vertex(destination));
     if(path==null)
         return null;
     else
     {
         Token[] newFlightMap=new Token[path.size()];
         for(int i=0;i<path.size();i++){
             newFlightMap[i]=(Token) new IntToken(path.get(i).getId());
         }
        // ArrayToken flightMap=new ArrayToken(BaseType.INT, newFlightMap);
         return newFlightMap;
     }
 
}

private void addLane(int laneId, int sourceLocNo, int destLocNo, int duration) {
 Edge lane = new Edge(laneId,new Vertex(sourceLocNo), new Vertex(destLocNo), duration);
 edges.add(lane);
}


} 
