AbstractCompoundExpression.java
	Extends CompoundBooleanExpression. Implements a number of 
	methods found in AbstractBinopExpr (almost a copy).
BooleanNotCompactor.java
	Converts complex Not (!) control-flow constructs into simple
	JHDLNotExpr Values.
CompoundAndExpression.java
	Represents the compound AND expression.
CompoundBooleanExpression.java
	Abstract class for representing a compound Boolean expression.
CompoundOrExpression.java
	Represents the compound OR expression.
ConditionalControlCompactor.java
	Contains the algorithm for converting successive IfStmt Values
	into a CompoundBooleanExpression.
ControlSootDFGBuilder.java
	This class extends SootDFGBuilder and generates a
	SootBlockDirectedGraph. The difference is that this class
	can create the dataflow associated with
	CompoundBooleanExpression Values and JHDLNotExpr Values	
DirectedAcyclicCFG.java
	This class takes a Soot Body and generates a DirectedGraph
	that represents the control-flow of the method. Nodes are
	Basic blocks and edges are control-flow paths.
DominatorCFG.java
	This class extends DirectedAcyclicCFG and supplements the
	data structure with appropriate dominator information
	(used for merging and control-flow analysis). It uses
	a DominatorHashMap object to represent this information.
DominatorHashMap.java
	A hashmap that represents dominator status between nodes
	in a DirectedAcyclicCFG.
HierarchicalControlGraph.java
	Extends DirectedGraph. Dummy class that can probably be removed.
IntervalBlockDirectedGraph.java
	Extends SootBlockDirectedGraph. Similar to IntervalDFG? Looks
	much simpler than IntervalDFG.
IntervalChain.java
	Represents an interval within a control-flow graph. Creation
	of this chain may add nodes to clarify the semantics of the
	graph. Generated from a DominatorCFG.
IntervalDFG.java
	This class is a BlockDataFlow graph that is a "merging" of
	multiple dataflow graphs associated with control-flow nodes
	in a method. Does the merging.
JHDLNotExpr.java
	A UnopExpr object that represents the NOT operator.
SootASTException.java
	An exception with the traversal of the Soot syntax. Generated
	by SootASTVisitor.
SootASTVisitor.java
	Traverses the syntax tree of a Soot Jimple Block.
SootBlockDirectedGraph.java
	A directed graph that contains a ValueMap.
SootDFGBuilder.java
	Extends SootASTVisitor. Generates a SootBlockDirectedGraph
	from a SootBlock.
ValueMap.java
	A HashListMap (a hashMap with Lists as values) that
	maps Values to Lists of Nodes.