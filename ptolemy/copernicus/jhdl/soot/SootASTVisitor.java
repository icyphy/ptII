/* A Soot abstract syntax tree visitor.

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

package ptolemy.copernicus.jhdl.soot;

import java.util.*;

import ptolemy.copernicus.jhdl.util.*;

import soot.toolkits.graph.*;
import soot.*;
import soot.jimple.*;


//////////////////////////////////////////////////////////////////////////
//// SootASTVisitor
/**

This class traverses the syntax tree of a Soot Jimple Block. This
class will iterate over all Stmt Units within the Block and traverse
the syntax of each Stmt. A unique method is called to process each
syntatic unit within the syntax tree. Leaf methods return a null to
indicate that the corresponding syntax object has not been processed.
<b>
Custom visitors of this class that extend this base class should
extend the appropriate leaf methods and return a non-null object to
indicate that the syntax object has been properly processed.

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0 
@see SootASTException

*/

public class SootASTVisitor {

    public SootASTVisitor() {
    }

    public SootASTVisitor(Block block) throws SootASTException {
        processBlock(block);
    }

    public SootASTVisitor(Iterator i) throws SootASTException {
        processUnitIterator(i);
    }

    public void processBlock(Block block) throws SootASTException {
        processUnitIterator(block.iterator());
    }

    public void processBody(Body b) throws SootASTException {
        processPatchingChain(b.getUnits());
    }

    public void processPatchingChain(PatchingChain pc) throws SootASTException {
        processUnitIterator(pc.iterator());
    }

    public void processUnitIterator(Iterator units) throws SootASTException {
        for (; units.hasNext();) {
            // Process all Stmt units in this graph
            Stmt stmt = (Stmt)units.next();
            if (DEBUG) System.out.println("Statement Class="+
                    stmt.getClass().getName()+
                    "\n\t\""+stmt+"\"");
            processStmt(stmt);
        }
    }

    public Stmt processStmt(Stmt stmt) throws SootASTException {
        Stmt s = null;
        if (stmt instanceof DefinitionStmt)
            s = processDefinitionStmt((DefinitionStmt) stmt);
        if (stmt instanceof BreakpointStmt)
            s = processBreakpointStmt((BreakpointStmt) stmt);
        if (stmt instanceof MonitorStmt)
            s = processMonitorStmt((MonitorStmt) stmt);
        if (stmt instanceof GotoStmt)
            s = processGotoStmt((GotoStmt) stmt);
        if (stmt instanceof InvokeStmt)
            s = processInvokeStmt((InvokeStmt) stmt);
        if (stmt instanceof IfStmt)
            s = processIfStmt((IfStmt) stmt);
        if (stmt instanceof LookupSwitchStmt)
            s = processLookupSwitchStmt((LookupSwitchStmt) stmt);
        if (stmt instanceof NopStmt)
            s = processNopStmt((NopStmt) stmt);
        if (stmt instanceof RetStmt)
            s = processRetStmt((RetStmt) stmt);
        if (stmt instanceof ReturnStmt)
            s = processReturnStmt((ReturnStmt) stmt);
        if (stmt instanceof ReturnVoidStmt)
            s = processReturnVoidStmt((ReturnVoidStmt) stmt);
        if (stmt instanceof TableSwitchStmt)
            s = processTableSwitchStmt((TableSwitchStmt) stmt);
        if (stmt instanceof ThrowStmt)
            s = processThrowStmt((ThrowStmt) stmt);

        if (s == null)
            throw new SootASTException("Unsupported Statement:"+stmt+
                    " of type "+stmt.getClass().getName());

        return s;
    }

    public Stmt processDefinitionStmt(DefinitionStmt stmt)
            throws SootASTException {

        Value rightOp = processValue(stmt.getRightOp());
        Value leftOp = processValue(stmt.getLeftOp(),true);
        return processDefinitionStmt(stmt, rightOp, leftOp);
    }

    public Stmt processDefinitionStmt(DefinitionStmt stmt,
            Value rightOp, Value leftOp) {
        if (stmt instanceof AssignStmt)
            return processAssignStmt((AssignStmt) stmt, leftOp, rightOp);
        if (stmt instanceof IdentityStmt)
            return processIdentityStmt((IdentityStmt) stmt, leftOp, rightOp);
        return null;
    }

    public Stmt processAssignStmt(AssignStmt stmt, Value leftOp,
            Value rightOp) {
        return null;
    }

    public Stmt processIdentityStmt(IdentityStmt stmt, Value leftOp,
            Value rightOp) {
        return null;
    }

    public Stmt processBreakpointStmt(BreakpointStmt stmt) {
        return null;
    }

    public Stmt processMonitorStmt(MonitorStmt stmt)
            throws SootASTException {
        Value op = processValue(stmt.getOp());
        return processMonitorStmt(stmt, op);
    }

    public Stmt processMonitorStmt(MonitorStmt stmt, Value op) {
        if (stmt instanceof EnterMonitorStmt)
            return processEnterMonitorStmt((EnterMonitorStmt) stmt, op);
        else if (stmt instanceof ExitMonitorStmt)
            return processExitMonitorStmt((ExitMonitorStmt) stmt, op);

        return null;
    }

    public Stmt processEnterMonitorStmt(EnterMonitorStmt stmt, Value op) {
        return null;
    }

    public Stmt processExitMonitorStmt(ExitMonitorStmt stmt, Value op) {
        return null;
    }

    public Stmt processGotoStmt(GotoStmt stmt) {
        return null;
    }

    public Stmt processInvokeStmt(InvokeStmt stmt)
            throws SootASTException {
        InvokeExpr ie = (InvokeExpr) processValue(stmt.getInvokeExpr());
        return processInvokeStmt(stmt, ie);
    }

    public Stmt processInvokeStmt(InvokeStmt stmt, InvokeExpr ie)
            throws SootASTException {
        return null;
    }

    public Value processInvokeExpr(InvokeExpr ie)
            throws SootASTException {

        // Process all arguments to the invoke expression
        List args = ie.getArgs();
        Value vargs[] = new Value[args.size()];
        for (int i=0;i<args.size();i++) {
            vargs[i] = processValue((Value) args.get(i));
        }

        if (ie instanceof InstanceInvokeExpr)
            return processInstanceInvokeExpr((InstanceInvokeExpr)ie,vargs);
        // Grimp
        //          else if (ie instanceof NewInvokeExpr)
        //              return v.visitNewInvokeExpr((NewInvokeExpr) ie,vargs);
        else if (ie instanceof StaticInvokeExpr)
            return processStaticInvokeExpr((StaticInvokeExpr) ie,vargs);
        return null;
    }

    public Value processInstanceInvokeExpr(InstanceInvokeExpr ie,
            Value args[])
            throws SootASTException {

        Value base = processValue(ie.getBase());
        if (ie instanceof InterfaceInvokeExpr)
            return processInterfaceInvokeExpr((InterfaceInvokeExpr) ie,args,base);
        else if (ie instanceof SpecialInvokeExpr)
            return processSpecialInvokeExpr((SpecialInvokeExpr) ie,args,base);
        else if (ie instanceof VirtualInvokeExpr)
            return processVirtualInvokeExpr((VirtualInvokeExpr) ie,args,base);
        return null;
    }

    public Value processInterfaceInvokeExpr(InterfaceInvokeExpr ie,
            Value args[], Value base) {
        return null;
    }

    public Value processSpecialInvokeExpr(SpecialInvokeExpr ie,
            Value args[], Value base) {
        return null;
    }
    public Value processVirtualInvokeExpr(VirtualInvokeExpr ie,
            Value args[], Value base) {
        return null;
    }
    //    public Value processNewInvokeExpr(NewInvokeExpr ie, Value args[]);
    public Value processStaticInvokeExpr(StaticInvokeExpr ie, Value args[]) {
        return null;
    }

    public Stmt processIfStmt(IfStmt stmt)
            throws SootASTException {
        ConditionExpr condition = (ConditionExpr) stmt.getCondition();
        //processConditionExpr(condition);
        processValue(condition);
        return processIfStmt(stmt, condition);
    }

    public Stmt processIfStmt(IfStmt stmt, ConditionExpr condition) {
        return null;
    }

    public Value processConditionExpr(ConditionExpr ce)
            throws SootASTException {
        Value op1 = processValue(ce.getOp1());
        Value op2 = processValue(ce.getOp2());
        return processConditionExpr(ce, op1, op2);
    }

    public Value processConditionExpr(ConditionExpr ce, Value op1, Value op2) {
        if (ce instanceof EqExpr)
            return processEqExpr((EqExpr) ce, op1, op2);
        if (ce instanceof GeExpr)
            return processGeExpr((GeExpr) ce, op1, op2);
        if (ce instanceof GtExpr)
            return processGtExpr((GtExpr) ce, op1, op2);
        if (ce instanceof LeExpr)
            return processLeExpr((LeExpr) ce, op1, op2);
        if (ce instanceof LtExpr)
            return processLtExpr((LtExpr) ce, op1, op2);
        if (ce instanceof NeExpr)
            return processNeExpr((NeExpr) ce, op1, op2);
        return null;
    }

    public Value processEqExpr(EqExpr ce, Value op1, Value op2) {return null;}
    public Value processGeExpr(GeExpr ce, Value op1, Value op2) {return null;}
    public Value processGtExpr(GtExpr ce, Value op1, Value op2) {return null;}
    public Value processLeExpr(LeExpr ce, Value op1, Value op2) {return null;}
    public Value processLtExpr(LtExpr ce, Value op1, Value op2) {return null;}
    public Value processNeExpr(NeExpr ce, Value op1, Value op2) {return null;}

    public Stmt processLookupSwitchStmt(LookupSwitchStmt stmt)
            throws SootASTException {
        Value key = processValue(stmt.getKey());
        return processLookupSwitchStmt(stmt, key);
    }

    public Stmt processLookupSwitchStmt(LookupSwitchStmt stmt, Value key) {
        return null;
    }

    public Stmt processNopStmt(NopStmt stmt) {
        return null;
    }

    public Stmt processRetStmt(RetStmt stmt) {
        return null;
    }

    public Stmt processReturnStmt(ReturnStmt stmt)
            throws SootASTException {
        Value returnVal = processValue(stmt.getOp());
        return processReturnStmt(stmt, returnVal);
    }

    public Stmt processReturnStmt(ReturnStmt stmt, Value returnVal) {
        return null;
    }

    public Stmt processReturnVoidStmt(ReturnVoidStmt stmt) {
        return null;
    }

    public Stmt processTableSwitchStmt(TableSwitchStmt stmt)
            throws SootASTException {
        Value key = processValue(stmt.getKey());
        return processTableSwitchStmt(stmt, key);
    }

    public Stmt processTableSwitchStmt(TableSwitchStmt stmt, Value key) {
        return null;
    }

    public Stmt processThrowStmt(ThrowStmt stmt)
            throws SootASTException {
        Value op = processValue(stmt.getOp());
        return processThrowStmt(stmt, op);
    }

    public Stmt processThrowStmt(ThrowStmt stmt, Value op) {
        return null;
    }

    public Value processValue(Value val) throws SootASTException {
        return processValue(val,false);
    }

    public Value processValue(Value val, boolean left)
            throws SootASTException {

        Value r = null;

        if (DEBUG) System.out.println("\tValue="+val+" class="+
                val.getClass().getName()+" identity="+
                System.identityHashCode(val));

        // skip ConditionExpr & InvokeExpr
        if (val instanceof UnopExpr)
            r = processUnopExpr( (UnopExpr) val);
        if (val instanceof BinopExpr)
            r = processBinopExpr( (BinopExpr) val);
        if (val instanceof Local)
            r = processLocal( (Local) val, left);
        if (val instanceof CastExpr)
            r = processCastExpr((CastExpr) val);
        if (val instanceof Ref)
            r = processRef ((Ref) val, left);
        if (val instanceof InvokeExpr)
            r = processInvokeExpr((InvokeExpr) val);
        if (val instanceof Constant)
            r = processConstant((Constant) val);
        if (val instanceof InstanceOfExpr)
            r = processInstanceOfExpr((InstanceOfExpr) val);
        if (val instanceof NewArrayExpr)
            r = processNewArrayExpr((NewArrayExpr) val);
        if (val instanceof NewMultiArrayExpr)
            r = processNewMultiArrayExpr((NewMultiArrayExpr) val);
        if (val instanceof NewExpr)
            r = processNewExpr((NewExpr) val);

        if (r == null)
            throw new SootASTException("Unknown Value:"+val+" of type "+
                    val.getClass().getName());
        return r;
    }

    public Value processUnopExpr(UnopExpr expr)
            throws SootASTException {
        Value op = processValue(expr.getOp());
        return processUnopExpr(expr, op);
    }

    public Value processUnopExpr(UnopExpr expr, Value op) {
        if (expr instanceof NegExpr)
            return processNegExpr((NegExpr) expr, op);
        if (expr instanceof LengthExpr)
            return processLengthExpr((LengthExpr) expr, op);
        return null;
    }

    public Value processNegExpr(NegExpr expr, Value op) {
        return null;
    }

    public Value processLengthExpr(LengthExpr expr, Value op) {
        return null;
    }

    public Value processBinopExpr(BinopExpr expr)
            throws SootASTException {

        Value op1 = processValue(expr.getOp1());
        Value op2 = processValue(expr.getOp2());

        return processBinopExpr(expr, op1, op2);
    }

    public Value processBinopExpr(BinopExpr expr, Value op1, Value op2) {

        //          if (expr instanceof ConditionExpr)
        //              return processConditionExpr((ConditionExpr) expr, op1, op2);

        if (expr instanceof AddExpr)
            return processAddExpr((AddExpr) expr, op1, op2);
        if (expr instanceof AndExpr)
            return processAndExpr((AndExpr) expr, op1, op2);
        if (expr instanceof CmpExpr)
            return processCmpExpr((CmpExpr) expr, op1, op2);
        if (expr instanceof CmpgExpr)
            return processCmpgExpr((CmpgExpr) expr, op1, op2);
        if (expr instanceof CmplExpr)
            return processCmplExpr((CmplExpr) expr, op1, op2);
        if (expr instanceof DivExpr)
            return processDivExpr((DivExpr) expr, op1, op2);
        if (expr instanceof MulExpr)
            return processMulExpr((MulExpr) expr, op1, op2);
        if (expr instanceof OrExpr)
            return processOrExpr((OrExpr) expr, op1, op2);
        if (expr instanceof RemExpr)
            return processRemExpr((RemExpr) expr, op1, op2);
        if (expr instanceof ShlExpr)
            return processShlExpr((ShlExpr) expr, op1, op2);
        if (expr instanceof ShrExpr)
            return processShrExpr((ShrExpr) expr, op1, op2);
        if (expr instanceof SubExpr)
            return processSubExpr((SubExpr) expr, op1, op2);
        if (expr instanceof UshrExpr)
            return processUshrExpr((UshrExpr) expr, op1, op2);
        if (expr instanceof XorExpr)
            return processXorExpr((XorExpr) expr, op1, op2);
        return null;
    }

    public Value processAddExpr(AddExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processAndExpr(AndExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processCmpExpr(CmpExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processCmpgExpr(CmpgExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processCmplExpr(CmplExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processDivExpr(DivExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processMulExpr(MulExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processOrExpr(OrExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processRemExpr(RemExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processShlExpr(ShlExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processShrExpr(ShrExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processSubExpr(SubExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processUshrExpr(UshrExpr expr, Value op1, Value op2) {
        return null;
    }
    public Value processXorExpr(XorExpr expr, Value op1, Value op2) {
        return null;
    }

    public Value processLocal(Local l, boolean left) {
        return null;
    }

    public Value processCastExpr(CastExpr val) {
        Value op = val.getOp();
        return processCastExpr(val,op);
    }

    public Value processCastExpr(CastExpr val, Value op) {
        return null;
    }

    public Value processRef(Ref cr, boolean left) throws SootASTException {

        if (cr instanceof ConcreteRef)
            return processConcreteRef((ConcreteRef) cr, left);
        if (cr instanceof IdentityRef)
            return processIdentityRef((IdentityRef) cr);
        return null;
    }

    public Value processConcreteRef(ConcreteRef cr, boolean left)
            throws SootASTException {
        if (cr instanceof ArrayRef)
            return processArrayRef((ArrayRef) cr, left);
        if (cr instanceof FieldRef)
            return processFieldRef((FieldRef) cr, left);
        return null;
    }

    public Value processArrayRef(ArrayRef ifr, boolean left)
            throws SootASTException {
        Value base = processValue(ifr.getBase());
        Value index = processValue(ifr.getIndex());
        return processArrayRef(ifr, base, index, left);
    }

    public Value processArrayRef(ArrayRef ifr, Value base, Value index,
            boolean left) {
        return null;
    }

    public Value processFieldRef(FieldRef ifr, boolean left)
            throws SootASTException {
        if (ifr instanceof InstanceFieldRef)
            return processInstanceFieldRef((InstanceFieldRef) ifr, left);
        if (ifr instanceof StaticFieldRef)
            return processStaticFieldRef((StaticFieldRef) ifr, left);
        return null;
    }

    public Value processInstanceFieldRef(InstanceFieldRef ifr, boolean left)
            throws SootASTException {
        Value base = processValue(ifr.getBase());
        return processInstanceFieldRef(ifr,base,left);
    }

    public Value processInstanceFieldRef(InstanceFieldRef ifr, Value base,
            boolean left) {
        return null;
    }

    public Value processStaticFieldRef(StaticFieldRef ifr, boolean left) {
        return null;
    }

    public Value processIdentityRef(IdentityRef ifr) {
        if (ifr instanceof CaughtExceptionRef)
            return processCaughtExceptionRef((CaughtExceptionRef) ifr);
        if (ifr instanceof ThisRef)
            return processThisRef((ThisRef) ifr);
        if (ifr instanceof ParameterRef)
            return processParameterRef((ParameterRef) ifr);
        return null;
    }

    public Value processCaughtExceptionRef(CaughtExceptionRef ifr) {
        return null;
    }

    public Value processThisRef(ThisRef ifr) {
        return null;
    }

    public Value processParameterRef(ParameterRef ifr) {
        return null;
    }

    public Value processConstant(Constant c) {
        if (c instanceof NullConstant)
            return processNullConstant((NullConstant) c);
        if (c instanceof NumericConstant)
            return processNumericConstant((NumericConstant) c);
        if (c instanceof StringConstant)
            return processStringConstant((StringConstant) c);
        return null;
    }

    public Value processNullConstant(NullConstant c) {
        return null;
    }

    public Value processNumericConstant(NumericConstant c) {
        return null;
    }

    public Value processStringConstant(StringConstant c) {
        return null;
    }

    public Value processInstanceOfExpr(InstanceOfExpr ifr)
            throws SootASTException {
        Value op = processValue(ifr.getOp());
        return processInstanceOfExpr(ifr,op);
    }

    public Value processInstanceOfExpr(InstanceOfExpr ifr, Value op) {
        return null;
    }

    public Value processNewArrayExpr(NewArrayExpr ifr)
            throws SootASTException {
        Value size = processValue(ifr.getSize());
        return processNewArrayExpr(ifr,size);
    }

    public Value processNewArrayExpr(NewArrayExpr ifr, Value size) {
        return null;
    }

    public Value processNewMultiArrayExpr(NewMultiArrayExpr ifr)
            throws SootASTException {
        List sizes = ifr.getSizes();
        Value vsizes[] = new Value[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            vsizes[i] = processValue((Value) sizes.get(i));
        }
        return processNewMultiArrayExpr(ifr,vsizes);
    }

    public Value processNewMultiArrayExpr(NewMultiArrayExpr ifr,
            Value sizes[]) {
        return null;
    }

    public Value processNewExpr(NewExpr ifr) {
        return null;
    }

    public static boolean DEBUG = false;

    /**
     * This test main method is used to test the AST implemented
     * by this method. This method will process each basic block
     * of the given method through the AST. Since this base class
     * doesn't do anything, nothing will be printed to STDOUT unless
     * there is an error.
     **/
    public static void main(String args[]) {
        SootASTVisitor.DEBUG = true;
        Block blocks[] =
            ptolemy.copernicus.jhdl.test.Test.getMethodBlocks(args);
        for (int i = 0 ; i < blocks.length; i++) {
            try {
                SootASTVisitor s = new SootASTVisitor(blocks[i]);
            } catch (SootASTException e) {
                System.err.println(e);
                System.exit(1);
            }
        }
        //          for (int i = 0;i<graphs.length;i++)
        //              System.out.println("Block "+i+" Value Map=\n"+
        //                                 graphs[i].getValueMap());
    }

}

