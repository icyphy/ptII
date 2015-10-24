/*For defining edges of a graph in DijkstraAlgorithm*/
package ptolemy.domains.atc.kernel.policy2;


public class Edge  {
    private final int id; 
    private final Vertex source;
    private final Vertex destination;
    private final int weight; 
    
    public Edge(int id, Vertex source, Vertex destination, int weight) {
      this.id = id;
      this.source = source;
      this.destination = destination;
      this.weight = weight;
    }
    
    public int getId() {
      return id;
    }
    public Vertex getDestination() {
      return destination;
    }

    public Vertex getSource() {
      return source;
    }
    public int getWeight() {
      return weight;
    }
    
    
  } 
