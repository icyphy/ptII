package ptolemy.copernicus.jhdl.util;

import soot.jimple.*;
import ptolemy.graph.*;

public class MuxNode implements GraphNode {

    public MuxNode(GraphNode firstInput, GraphNode secondInput, Label label){
	_conditionBlock=label.getSuperBlock();
	_trueInput = label.branch() ? firstInput : secondInput;
	_falseInput = label.branch() ? secondInput : firstInput;
    }

    //A place holder.  We probably need another class for these "specific" muxes
    public MuxNode(Object trueInput, Object falseInput, Object condition, int dummy){
	//_trueInput = trueInput;
	//_falseInput = falseInput;
	//_conditionBlock = condition;  //WRONG
    }

    public Node createDataFlow(DirectedGraph graph, Object value){
	/*
	  This mux is really only useful if the value is written to
	  between here and the block that defines the condition.  For
	  now we can't detect this.  Need to implement it in the future.
	*/

	System.out.println("Getting from true input");
	Node trueResult = _trueInput.createDataFlow(graph, value);
	System.out.println("Getting from false input");
	Node falseResult = _falseInput.createDataFlow(graph, value);
	Object cond = ((IfStmt)_conditionBlock.getBlock().getTail()).getCondition();
	System.out.println("Getting from condition input");
	Node condResult = _conditionBlock.createDataFlow(graph, cond);
	System.out.println("Got 'em all");
	//Get the data flow for the values in the conditional expression

	System.out.println("true: "+trueResult);
	System.out.println("false: "+falseResult);
	System.out.println("cond: "+condResult);
	
	if (trueResult == null && falseResult == null) //&& condResult == null)
	    return null;

	if (trueResult == null)
	    return falseResult;

	if (falseResult == null)
	    return trueResult;
	
	MuxNode newMux = new MuxNode(trueResult, falseResult, condResult, 1);

	Node newMuxNode = graph.addNodeWeight(newMux);
	//if (trueResult == falseResult && trueResult == condResult){
	//    System.out.println("AH HA!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	//}
	SynthesisToDotty.writeDotFile("junk", graph);
	graph.addEdge(trueResult, newMuxNode);
	graph.addEdge(falseResult, newMuxNode);
	graph.addEdge(condResult, newMuxNode);
	
	return newMuxNode;
    }
    
    public String toString(){
	return "mux";
    }

    GraphNode _trueInput;
    GraphNode _falseInput;
    SuperBlock _conditionBlock;
}
