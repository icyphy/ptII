package ptolemy.copernicus.jhdl.util;

import ptolemy.graph.DirectedGraph;

public interface GraphNode{


    /**
     *
     */
    Object createDataFlow(DirectedGraph graph, Object value);

}
