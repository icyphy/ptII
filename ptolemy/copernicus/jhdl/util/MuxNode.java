package ptolemy.copernicus.jhdl.util;

import soot.jimple.*;
import ptolemy.graph.*;

public class MuxNode implements GraphNode {

    public MuxNode(GraphNode firstInput, GraphNode secondInput, Label label){
	_conditionBlock=label.getSuperBlock();
	_trueInput = label.branch() ? firstInput : secondInput;
	_falseInput = label.branch() ? secondInput : firstInput;
    }

    public GraphNode createDataFlow(DirectedGraph graph, Object value){
	return this;
    }
    
    public String toString(){
	return "mux";
    }

    GraphNode _trueInput;
    GraphNode _falseInput;
    SuperBlock _conditionBlock;
}
