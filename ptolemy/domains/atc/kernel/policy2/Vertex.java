/* For defining nodes of a graph in DijkstraAlgorithm*/
package ptolemy.domains.atc.kernel.policy2;

public class Vertex {
    final private int id;
    
    
    public Vertex(int id) {
      this.id = id;
    }
    public int getId() {
      return id;
    }

    
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == -1) ? 0 :1);
      return result;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Vertex other = (Vertex) obj;
      if (id == -1) {
        if (other.id != -1)
          return false;
      } else if (id!=other.id)
        return false;
      return true;
    }
    
  } 
