package ptolemy.copernicus.jhdl.util;

import ptolemy.graph.DirectedGraph;

public interface GraphNode{


    /**
     *
     */
    GraphNode createDataFlow(DirectedGraph graph, Object value);

}
