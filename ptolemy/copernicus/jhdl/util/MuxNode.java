package ptolemy.copernicus.jhdl.util;

import soot.jimple.*;
import ptolemy.graph.*;

public class MuxNode implements GraphNode {

    public MuxNode(GraphNode firstInput, GraphNode secondInput, Label label){
	_conditionBlock=label.getSuperBlock();
	_trueInput = label.branch() ? firstInput : secondInput;
	_falseInput = label.branch() ? secondInput : firstInput;
    }

    public MuxNode(Object trueInput, Object falseInput, Object condition, int dummy){
	//_trueInput = trueInput;
	//_falseInput = falseInput;
	//_conditionBlock = condition;  //WRONG
    }

    public Object createDataFlow(DirectedGraph graph, Object value){
	/*
	  This mux is really only useful if the value is written to
	  between here and the block that defines the condition.  For
	  now we can't detect this.  Need to implement it in the future.
	*/

	Object trueResult = _trueInput.createDataFlow(graph, value);
	Object falseResult = _falseInput.createDataFlow(graph, value);
	ConditionExpr cond = (ConditionExpr)_conditionBlock.getBlock().getTail();
	Object condResult = _conditionBlock.createDataFlow(graph, cond);
	//Get the data flow for the values in the conditional expression
	MuxNode newMux = new MuxNode(trueResult, falseResult, condResult, 1);

	graph.addNodeWeight(newMux);
	graph.addEdge(trueResult, newMux);
	graph.addEdge(falseResult, newMux);
	graph.addEdge(condResult, newMux);
	
	return newMux;
    }
    
    public String toString(){
	return "mux";
    }

    GraphNode _trueInput;
    GraphNode _falseInput;
    SuperBlock _conditionBlock;
}
