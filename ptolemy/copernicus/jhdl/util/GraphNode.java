package ptolemy.copernicus.jhdl.util;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;

public interface GraphNode{


    /**
     *
     */
    Node createDataFlow(DirectedGraph graph, Object value);

}
