package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;

public class Graph implements Cloneable {

    public Graph() {

    }
    
    public Graph clone() throws CloneNotSupportedException {
        Graph newObject = (Graph) super.clone(); 
        newObject._nodeConnection = (ArrayList<ArrayList<Integer>>) _nodeConnection.clone(); 
        newObject._edge = (ArrayList<Pair<Integer, Integer>>) _edge.clone(); 
        return newObject; 
    }
    
    public void clear() {
        _nodeConnection.clear();
        _edge.clear(); 
    }

    public boolean contains(int id1, int id2) {
        if (id1 >= _nodeConnection.size() || id2 >= _nodeConnection.size()) {
            return false;
        }
        // System.out.println(id1+" "+id2); 
        for (Integer edgeId : _nodeConnection.get(id1)) {
            // System.out.println(_edge.get(edgeId).getFirst().intValue()+":"+_edge.get(edgeId).getSecond().intValue()); 
            if (_edge.get(edgeId).getFirst().intValue() == id2 || _edge.get(edgeId).getSecond().intValue() == id2) {
                return true;
            }
        }
        return false;
    }

    /** Add a mapping constraint (A, B).
    *
    * @param id1 Event A in the constraint
    * @param id2 Event B in the constraint
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

    public Iterable<Integer> getEdges(int nodeId) {
        return (Iterable<Integer>) _nodeConnection.get(nodeId);
    }

    public int nodeSize() {
        return _nodeConnection.size();
    }

    public int edgeSize() {
        return _edge.size();
    }

    public Pair<Integer, Integer> getEdge(int edgeId) {
        return _edge.get(edgeId);
    }

    /** The adjacency list that represents the mapping constraints
     *  (event pairs).
     */
    private ArrayList<ArrayList<Integer>> _nodeConnection = new ArrayList<ArrayList<Integer>>();

    private ArrayList<Pair<Integer, Integer>> _edge = new ArrayList<Pair<Integer, Integer>>();

}
