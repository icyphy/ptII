/* Dataflow representation of a Soot Block

 Copyright (c) 2001-2003 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl;

import soot.SootField;
import soot.Value;
import soot.Local;

import soot.jimple.DefinitionStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.LookupSwitchStmt;

import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.CastExpr;

import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;

import soot.jimple.UnopExpr;
import soot.jimple.NegExpr;
import soot.jimple.BinopExpr;
import soot.jimple.FieldRef;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.IfStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.Ref;
import soot.jimple.IdentityRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ArrayRef;
import soot.jimple.StaticFieldRef;

import soot.jimple.internal.JAndExpr;
import soot.jimple.internal.JOrExpr;
import soot.jimple.internal.JimpleLocal;

//import soot.jimple.toolkits.invoke.MethodCallGraph;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
//import soot.jimple.toolkits.invoke.InvokeGraph;
//import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
//import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
//import soot.jimple.toolkits.invoke.VTATypeGraph;
//import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;

import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.graph.Edge;

import ptolemy.copernicus.jhdl.util.HashListMap;
import ptolemy.copernicus.jhdl.util.PtDirectedGraphToDotty;
import ptolemy.copernicus.jhdl.util.BlockGraphToDotty;
import ptolemy.copernicus.jhdl.util.JHDLUnsupportedException;

import ptolemy.copernicus.jhdl.soot.JHDLNotExpr;
import ptolemy.copernicus.jhdl.soot.CompoundBooleanExpression;
import ptolemy.copernicus.jhdl.soot.ConditionalControlCompactor;

//////////////////////////////////////////////////////////////////////////
//// BlockDataFlowGraph
/**
 * This class will take a Soot block and create a ptolemy
 * DirectedGraph that represents the data dependancies between Soot Values.
 * Since a single block (i.e. Basic Block) is acyclic,
 * this graph will also be acyclic.<p>
 *
 * The weights of the ptolemy.graph.Node objects created for this
 * graph are the semantic objects that the Node represents. In most
 * cases, these objects are of type soot.Value. The exception to this
 * includes the Nodes associated with ReturnStmt objects - the weight
 * of these Nodes are Stmt objects, not Value objects. <p>
 *
 * Note: Following are useful methods
 * DirectedGraph.nodes()
 * DirectedGraph.sourceNodes()
 * DirectedGraph.sinkNodes()
 *
@author Mike Wirthlin and Matthew Koecher
@version $Id$
@since Ptolemy II 2.0
*/

public class BlockDataFlowGraph extends DirectedGraph {

    /**
     * Constructor iterates through the statements found within the
     * Block to create a dataflow graph of the block behavior.
     *
     * @param block Basic block that will used to create a dataflow graph
     * @see ptolemy.copernicus.jhdl.BlockDataFlowGraph#_processAssignStmt(AssignStmt)
     * @see BlockDataFlowGraph#_processIdentityStmt(IdentityStmt)
     * @see BlockDataFlowGraph#_processInvokeStmt(InvokeStmt)
     * @see BlockDataFlowGraph#_processReturnStmt(ReturnStmt)
     *
     **/
    public BlockDataFlowGraph(Block block) throws JHDLUnsupportedException {

        super();
        _block = block;

        _locals = new HashListMap();
        _instanceFieldRefs = new HashListMap();

        // Iterate over all units within block
        _processStmtUnits();
    }

    //////////////////////////////////////////////////

    /**
     * Loop through all the Stmt Units in the Body and create the
     * corresponding DataFlow graph. This method will identify the
     * appropriate statement type and call a method more specific
     * to the statement of interest. This method is called in the
     * constructor of this class.
     *
     * @see BlockDataFlowGraph#_processAssignStmt(AssignStmt)
     * @see BlockDataFlowGraph#_processIdentityStmt(IdentityStmt)
     * @see BlockDataFlowGraph#_processInvokeStmt(InvokeStmt)
     * @see BlockDataFlowGraph#_processReturnStmt(ReturnStmt)
     * @see BlockDataFlowGraph#_processIfStmt(IfStmt)
     * @exception The JHDLUnsupportedException will be thrown if an
     * unsupported (or unknown) Stmt object is encountered.
     *
     **/
    protected void _processStmtUnits()
            throws JHDLUnsupportedException {
        for (Iterator units = _block.iterator(); units.hasNext();) {

            // Process all Stmt units in this graph
            Stmt stmt = (Stmt)units.next();
            if (DEBUG) System.out.println("Statement Class="+
                    stmt.getClass().getName()+
                    "\n\t\""+stmt+"\"");

            // Each statement is treated differently. Search for the
            // appropriate statement type and process it according
            // to its semantics.
            if (stmt instanceof DefinitionStmt) {
                _processDefinitionStmt((DefinitionStmt) stmt);
            } else if (stmt instanceof InvokeStmt) {
                _processInvokeStmt((InvokeStmt) stmt);
            } else if (stmt instanceof ReturnStmt) {
                _processReturnStmt((ReturnStmt) stmt);
            } else if (stmt instanceof ReturnVoidStmt) {
                // a return void statement does not affect dataflow.
            } else if (stmt instanceof IfStmt) {
                // if statements shoudl be last statement in basic block.
                // This IfStmt generates dataflow constructs
                _processIfStmt((IfStmt) stmt);
            } else if (stmt instanceof TableSwitchStmt) {
                // No data flow is added at this point - control flow
                // analysis may look at this statement at a later time.
            } else if (stmt instanceof LookupSwitchStmt) {
                // No data flow is added at this point - control flow
                // analysis may look at this statement at a later time.
            } else if (stmt instanceof GotoStmt) {
                // Goto statements shoudl be last statement in basic block.
                // No data flow is added at this point - control flow
                // analysis may look at this statement at a later time.
            } else {
                throw new JHDLUnsupportedException("Unsupported statement="+
                        stmt.getClass().getName());
            }
        }
    }

    /**
     * This method will evaluate DefinitionStmt statements
     * (AssignStmt and IdentityStmt). This method will create a new
     * Node that represents the Value being assigned. It will also
     * create or find a Node representing the right operation.
     *
     * This method will also create a dependency edge between the
     * Node representing the "rightOp" and the Node representing the
     * "leftOp".
     *
     * This method is called by _processStmtUnits()
     *
     * @see BlockDataFlowGraph#_processUnopExpr(UnopExpr)
     * @see BlockDataFlowGraph#_processBinopExpr(BinopExpr)
     * @see BlockDataFlowGraph#_processLocal(Local)
     * @see BlockDataFlowGraph#_getOrCreateInstanceFieldRef(InstanceFieldRef)
     * @see BlockDataFlowGraph#_processInstanceInvokeExpr(InstanceInvokeExpr)
     * @see BlockDataFlowGraph#_processConstant(Constant)
     * @see BlockDataFlowGraph#_processStaticFieldRef(StaticFieldRef)
     **/
    protected void _processDefinitionStmt(DefinitionStmt stmt)
            throws JHDLUnsupportedException {

        // 1. Create Node for RightOp first
        // 2. Create LeftOp Node
        // 3. Add edge from RightOp to LeftOp

        // Create Node for RightOp
        Value rightOp = stmt.getRightOp();
        Value leftOp = stmt.getLeftOp();

        Node rightOpNode = _processValue(rightOp);
        Node leftOpNode = _addLeftValue(leftOp);

        // Add edge
        addEdge(rightOpNode,leftOpNode);
    }

    /**
     * This method will take a Value and create or find a Node that
     * represents this Value. This method uses several type-specific
     * methods to obtain this Node.
     *
     * This method is called by many other methods that must obtain
     * a Node for a given Value.
     *
     * @return Returns the Node associated with the given Value
     **/
    protected Node _processValue(Value v)
            throws JHDLUnsupportedException {

        if (DEBUG) System.out.println("\tValue="+v+" class="+
                v.getClass().getName()+" identity="+
                System.identityHashCode(v));
        Node valueNode = null;
        if (v instanceof UnopExpr){
            valueNode = _processUnopExpr( (UnopExpr) v);
        } else if (v instanceof BinopExpr){
            valueNode = _processBinopExpr( (BinopExpr) v);
        } else if (v instanceof Local) {
            valueNode = _processLocal((Local)v);
        } else if (v instanceof CastExpr) {
            valueNode = _processCastExpr((CastExpr) v);
        } else if (v instanceof Ref) {
            valueNode = _processRef((Ref) v);
        } else if (v instanceof InvokeExpr) {
            valueNode = _processInvokeExpr((InvokeExpr) v);
        } else if (v instanceof Constant){
            valueNode = _processConstant((Constant) v);
        } else {
            // soot.jimple.NewExpr
            throw new JHDLUnsupportedException("Unsupported Value="+
                    v.getClass().getName());
        }
        return valueNode;
    }

    /**
     * Process a UnopExpr and create the corresponding dataflow graph.
     * Return a Node in the dataflow graph that represents the result.
     * Only the NegExpr operation is supported.
     *
     * This method is called by the _processValue() method.
     **/
    protected Node _processUnopExpr(UnopExpr expr)
            throws JHDLUnsupportedException {
        if (expr instanceof NegExpr ||
                expr instanceof JHDLNotExpr){

            Node n = addNodeWeight(expr);
            Value rightValue=expr.getOp();
            Node rv = _processValue(rightValue);
            addEdge(rv,n);
            return n;
        } else {
            throw new JHDLUnsupportedException("Unsupported Unary Operator="+
                    expr.getClass().getName());
        }
    }


    /**
     * Create a new Node for a binary operation expression.
     *
     * 1. Obtain Nodes associated with op1 and op2
     * 2. Create a new Node for the binary operation
     * 3. Add edges between op1/op2 and the new Node.
     *
     * This method is called by the _processValue() method.
     *
     * @return Returns the new Node created for the binary operation.
     **/
    protected Node _processBinopExpr(BinopExpr expr)
            throws JHDLUnsupportedException {
        Value rightValue1=expr.getOp1();
        Value rightValue2=expr.getOp2();
        Node n = addNodeWeight(expr);
        Node r1n = _processValue(rightValue1);
        Node r2n = _processValue(rightValue2);
        addEdge(r1n,n,"op1");
        addEdge(r2n,n,"op2");
        return n;
    }


    /**
     * This method will search to see if the given
     * Local exists in the graph. If it exists, the last definition Node of
     * the Local is returned. If it doesn't exist, a new Local is created.
     *
     * This method is called by the _processValue() method.
     *
     **/
    protected Node _processLocal(Local l) {
        Node n=null;
        if (!_locals.containsKey(l)) {
            return _createLocal(l);
        } else {
            return (Node) _locals.getLast(l);
        }
    }

    /**
     * Creates a new Node with the given Local as the weight. It also
     * adds the new Node to the _locals MapList.
     *
     * This method is called by _processLocal(), _addLeftValue()
     **/
    protected Node _createLocal(Local l) {
        Node n = addNodeWeight(l);
        _locals.add(l,n);
        return n;
    }

    /**
     * This method ignores the Cast and simply returns a Node associated
     * with the Value object that is casted.
     *
     * This method is called by the _processValue() method.
     **/
    protected Node _processCastExpr(CastExpr ce)
            throws JHDLUnsupportedException {
        Value op = ce.getOp();
        return _processValue(op);
    }

    /**
     * This method processes Reference Values. This method will
     * call type-specific methods to process each type of Ref.
     *
     * This method is called by the _processValue() method.
     **/
    protected Node _processRef(Ref cr)
            throws JHDLUnsupportedException {
        Node new_node = null;
        if (cr instanceof ArrayRef) {
            new_node = _processArrayRef((ArrayRef) cr);
        } else if (cr instanceof IdentityRef) {
            new_node = _processIdentityRef((IdentityRef) cr);
        } else if (cr instanceof InstanceFieldRef) {
            new_node = _processInstanceFieldRef((InstanceFieldRef) cr);
        } else if (cr instanceof StaticFieldRef) {
            new_node = _processStaticFieldRef((StaticFieldRef) cr);
        } else {
            throw new JHDLUnsupportedException("Unsupported Ref="+
                    cr.getClass().getName());
        }
        return new_node;
    }

    /**
     * Includes ThisRef and ParameterRef objects.
     **/
    protected Node _processIdentityRef(IdentityRef tr) {
        return _getOrCreateNode(tr);
    }

    protected Node _processArrayRef(ArrayRef ifr)
            throws JHDLUnsupportedException {
        throw new JHDLUnsupportedException("No support for arrays");
    }

    protected Node _processInstanceFieldRef(InstanceFieldRef ifr)
            throws JHDLUnsupportedException {

        InstanceFieldRef ifr_p = _getMatchingInstanceFieldRef(ifr);
        if (ifr_p == null) {
            return _createInstanceFieldRef(ifr);
        } else {
            return (Node) _instanceFieldRefs.getLast(ifr_p);
        }
    }

    protected InstanceFieldRef _getMatchingInstanceFieldRef(InstanceFieldRef ifr) {
        SootField field = ifr.getField();
        Value baseValue = ifr.getBase();
        InstanceFieldRef previous=null;
        for (Iterator it = _instanceFieldRefs.keySet().iterator();it.hasNext();) {
            InstanceFieldRef ifr_n = (InstanceFieldRef) it.next();
            if (ifr_n.getBase().equals(baseValue) &&
                    ifr_n.getField().equals(field)) {
                previous = ifr_n;
            }
        }
        return previous;
    }

    /**
     * Called by _processInstanceFieldRef and _addLeftValue
     **/
    protected Node _createInstanceFieldRef(InstanceFieldRef ifr)
            throws JHDLUnsupportedException {

        InstanceFieldRef new_ifr = _getMatchingInstanceFieldRef(ifr);
        if (new_ifr == null)
            new_ifr = ifr;

        if (DEBUG) System.out.println("\tNew InstanceFieldRef Node using id="+
                System.identityHashCode(new_ifr));

        Node base = _processValue(new_ifr.getBase());
        Node n = addNodeWeight(new_ifr);
        _instanceFieldRefs.add(new_ifr,n);

        // add edge between base and ifr
        //addBaseEdge(base,n);  MRK hack
        return n;
    }

    protected Node _processStaticFieldRef(StaticFieldRef sfr)
            throws JHDLUnsupportedException {
        //        throw new JHDLUnsupportedException("Static field references currently not supported"+sfr);
        return _getOrCreateNode(sfr);
    }

    protected Node _processInvokeExpr(InvokeExpr ie)
            throws JHDLUnsupportedException {
        Node invokeNode = null;
        if (ie instanceof InstanceInvokeExpr)
            invokeNode = _createInstanceInvokeExpr((InstanceInvokeExpr)ie);
        else if (ie instanceof InterfaceInvokeExpr ||
                ie instanceof SpecialInvokeExpr ||
                ie instanceof StaticInvokeExpr ||
                ie instanceof VirtualInvokeExpr)
            throw new JHDLUnsupportedException("Unsupported InvokeExpr="+
                    ie.getClass().getName());
        else
            throw new JHDLUnsupportedException("Unsupported InvokeExpr="+
                    ie.getClass().getName());

        // add argument links
        int argCount=0;
        for (Iterator arguments = ie.getArgs().iterator();
             arguments.hasNext();) {
            Value argument = (Value)arguments.next();
            Node a_n = _processValue(argument);
            addEdge(a_n,invokeNode,new Integer(argCount++));
        }
        return invokeNode;
    }

    protected Node _createInstanceInvokeExpr(InstanceInvokeExpr iie)
            throws JHDLUnsupportedException {
        Node base = _processValue(iie.getBase());
        Node n = addNodeWeight(iie);
        addBaseEdge(base,n);
        return n;
    }

    /**
     * Process the Left Value of DefinitionStmt. This method
     * is called by the _processDefinitionStmt method. There are two types
     * of left Values: Locals and InstanceFieldRef. Locals are added like
     * regular values (i.e. a single Node is added for the Value). If
     * a Local is overwritten, there will be more than one Node associated
     * with the Value - the last Value in the _valueMap is the last instance
     * of the Value.
     *
     * InstanceFieldRef Values are treated special and are added with
     * a call to the method _addInstanceFieldRefNode.
     *
     **/
    protected Node _addLeftValue(Value lv)
            throws JHDLUnsupportedException {
        Node leftOpNode = null;
        if (DEBUG) System.out.println("\tLeft Value="+lv+" class="+
                lv.getClass().getName()+
                " identity="+
                System.identityHashCode(lv));
        if (lv instanceof Local) {
            leftOpNode = _createLocal((Local) lv);
        } else if (lv instanceof InstanceFieldRef) {
            leftOpNode = _createInstanceFieldRef((InstanceFieldRef)lv);
        } else {
            throw new JHDLUnsupportedException("Unsupported Left AssignOp=" +
                    lv.getClass().getName());
        }
        //_lValues.add(lv);
        return leftOpNode;
    }

    /**
     * Process a Constant Value object. If a Node exists with the given
     * constant, return the Node. Otherwise create a new Node with the
     * constant weight.
     **/
    protected Node _processConstant(Constant c) {
        return _getOrCreateNode(c);
    }


    /////////////////////////////

    protected void _processInvokeStmt(InvokeStmt stmt)
            throws JHDLUnsupportedException {
        InvokeExpr ie = (InvokeExpr) stmt.getInvokeExpr();
        _processInvokeExpr(ie);
    }

    /**
     * This method will add a Node for the ReturnStmt and add an edge
     * from the Value being returned and this new ReturnStmt Node.
     * The weight of the new Node is the ReturnStmt.
     **/
    protected Node _processReturnStmt(ReturnStmt stmt)
            throws JHDLUnsupportedException {
        Value returnedValue = stmt.getOp();
        Node returnedNode = _processValue(returnedValue);
        // NOTE: The weight of this Node is a ReturnStmt Object,
        // not a Value!!
        Node newNode = addNodeWeight(stmt);
        addEdge(returnedNode,newNode);
        return newNode;
    }

    /**
     **/
    protected Node _processIfStmt(IfStmt stmt)
            throws JHDLUnsupportedException {

        Value condition = stmt.getCondition();
        if (!(condition instanceof ConditionExpr))
            throw new JHDLUnsupportedException("Unsupported Condition="+
                    condition.getClass().getName());

        return _processConditionExpr((ConditionExpr) condition);

    }

    protected Node _processConditionExpr(ConditionExpr condition)
            throws JHDLUnsupportedException {

        Node n = null;
        Value op1 = condition.getOp1();
        Value op2 = condition.getOp2();
        Node op1n;
        Node op2n;

        if (condition instanceof EqExpr ||
                condition instanceof GeExpr ||
                condition instanceof GtExpr ||
                condition instanceof LeExpr ||
                condition instanceof LtExpr ||
                condition instanceof NeExpr) {
            op1n = _processValue(op1);
            op2n = _processValue(op2);
            n = addNodeWeight(condition);
        } else if (condition instanceof CompoundBooleanExpression) {
            op1n = _processConditionExpr((ConditionExpr) op1);
            op2n = _processConditionExpr((ConditionExpr) op2);
            n = addNodeWeight(condition);
        } else
            throw new JHDLUnsupportedException("Unknown ConditionExpr "+
                    condition.getClass());
        addEdge(op1n,n,"op1");
        addEdge(op2n,n,"op2");

        return n;
    }

    /**
     * This method will search the graph to see if the given Value
     * exists as a weight of a Node in the graph.  If it is, the
     * *LAST* Node corresponding to the Value is returned.
     * If not, a new Node is created
     * in the graph with the given Value as the Node weight. Further,
     * the _valueMap is updated to Map the Value to the Node.
     **/
    protected Node _getOrCreateNode(Value value) {
        Node n=null;
        if (!containsNodeWeight(value)) {
            n = addNodeWeight(value);
            return n;
        } else {
            return node(value);
        }
    }

    protected Edge addBaseEdge(Node base, Node ref) {
        return addEdge(base,ref,"base");
    }


    //////////////////////////////////////////////////


    /**
     * This method returns an array of BlockDataFlowGraph objects for
     * a given Method from a class. The method/class name are speciefied
     * as command line arguments.
     **/
    public static BlockDataFlowGraph[] getBlockDataFlowGraphs(String args[]) {
        soot.SootMethod testMethod =
            ptolemy.copernicus.jhdl.test.Test.getSootMethod(args);
        soot.Body body = testMethod.retrieveActiveBody();

        BriefBlockGraph bbgraph = new BriefBlockGraph(body);
        BlockGraphToDotty toDotty = new BlockGraphToDotty();
        toDotty.writeDotFile(".","cfg", bbgraph);
        List blockList=bbgraph.getBlocks();
        BlockDataFlowGraph graphs[] = new BlockDataFlowGraph[blockList.size()];
        PtDirectedGraphToDotty dgToDotty = new PtDirectedGraphToDotty();
        for (int blockNum=0; blockNum < blockList.size(); blockNum++){
            Block block=(Block)blockList.get(blockNum);
            BlockDataFlowGraph dataFlowGraph = null;
            try {
                dataFlowGraph = new BlockDataFlowGraph(block);
                graphs[blockNum] = dataFlowGraph;
            } catch (JHDLUnsupportedException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            dgToDotty.writeDotFile(".","bbgraph" + blockNum,
                    dataFlowGraph);
        }
        return graphs;
    }

    public static void main(String args[]) {
        BlockDataFlowGraph.DEBUG = true;
        BlockDataFlowGraph[] graphs = getBlockDataFlowGraphs(args);
        //          for (int i = 0;i<graphs.length;i++)
        //              System.out.println("Block "+i+" Value Map=\n"+
        //                                 graphs[i].getValueMap());
    }

    /** The original Soot block used to create this graph **/
    protected Block _block;

    /**
     * Maps Soot "Values" (leftOp and rightOps) to Nodes. The key for
     * this Map is a soot.Value and the value of the Map is a
     * List. Members of the List are Nodes that are mapped to the given
     * Value. Ordering of the List is important as Nodes at the
     * beginning of the list were created before Nodes at the end
     * of the list. Each Value maps to one List and all Nodes are
     * contained in the Lists.
     *
     * This mapping allows you to get a number of Nodes from a Value.
     * Usually, the first or last Node is desired.
     * Note that the reverse mapping is built into the graph - Nodes
     * in the graph contain a "Value" for their weight.
     *
     **/
    //protected HashListMap _valueMap;

    protected HashListMap _locals;
    protected HashListMap _instanceFieldRefs;

    public static boolean DEBUG = false;

}
