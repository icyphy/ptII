/*
An implementation of the visitor design pattern that generates C code
from Jimple statements.

Copyright (c) 2001-2003 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import soot.ArrayType;
import soot.PrimType;
import soot.ByteType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.RefType;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.Scene;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.BreakpointStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.EqExpr;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Expr;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GotoStmt;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.JimpleValueSwitch;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

//////////////////////////////////////////////////////////////////////////
//// CSwitch
/** An implementation of the visitor design pattern that generates C code
    from Jimple statements. Code generated in this class is placed in an
    internal code stack (see {@link #_push(StringBuffer)}, and {@link
    #_pop()}).

   @author Shuvra S. Bhattacharyya, Ankush Varma
   @version $Id$
   @since Ptolemy II 2.0
*/
public class CSwitch implements JimpleValueSwitch, StmtSwitch {

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** Current Indentation level: Used by other classes if they need to know or
     *  change  the degree of indentation in the currently generated code.
     */
    public byte indentLevel;

    /** Set to true if this statement is in the body of a method that
     * requires exception-handling.
     */
    public boolean exceptionsExist;

    /** Construct a new CSwitch with an empty context. */
    public CSwitch() {
        super();
        _code = new Stack();
        _context = new Context();
        _targetMap = new HashMap();
        _targetCount = 0;
        indentLevel = 0;
    }

    /** Construct a new CSwitch with a given context. */
    public CSwitch(Context context) {
        this();
        _context = context;
    }

    /** Register a unit as a target of a branch from some other statement.
     *  @param target The unit.
     */
    public void addTarget(Unit target) {
        _targetMap.put(target, "label" + _targetCount++);
    }


    public void caseAddExpr(AddExpr v) {
        _generateBinaryOperation(v, "+");
    }

    public void caseAndExpr(AndExpr v) {
        // Bitwise AND operator.
        _generateBinaryOperation(v, "&");
    }


    /** Generate code for an array reference. This is done by
     *  generating a call to a macro from the run time library
     *  with the given base (array) and index expression.
     *  @param v The array reference whose code is to be generated.
     */
    public void caseArrayRef(ArrayRef v) {
        v.getBase().apply(this);
        StringBuffer baseCode = _pop();
        v.getIndex().apply(this);
        StringBuffer indexCode = _pop();

        String baseCast = "(" + CNames.typeNameOf(v.getBase().getType())
            + ")";
        String elem_type = CNames.typeNameOf(v.getType());

        _push(CNames.arrayReferenceFunction + "("
                + baseCast + baseCode
                + ", " + elem_type
                + ", (long)" + indexCode + ")");
    }


    /** Generate code for an assignment statement.
     *  @param stmt The assignment statement.
     */
    public void caseAssignStmt(AssignStmt stmt) {
        stmt.getRightOp().apply(this);
        stmt.getLeftOp().apply(this);
        StringBuffer code = new StringBuffer();
        String indent = new String();

        if (indentLevel == 1) {
            indent = "    ";
        }
        else if (indentLevel == 2) {
            indent = "        ";
        }


        String castType = CNames.typeNameOf(stmt.getLeftOp().getType());
        // If the RHS is a InterfaceInvokeExpr, and LHS is of type "short",
        // cast it as "long", so that no warnings are thrown on an
        // interface lookup.
        if (stmt.getRightOp() instanceof InterfaceInvokeExpr) {
            if (castType.equals("short")) {
                castType = new String("long");
            }
        }


        if (stmt.getLeftOp().getType() instanceof ArrayType) {
            _context.addArrayInstance(
                    CNames.typeNameOf(stmt.getLeftOp().getType()));
        }

        if (stmt.getRightOp().getType() instanceof ArrayType) {
            _context.addArrayInstance(
                    CNames.typeNameOf(stmt.getRightOp().getType()));
        }

        // castType makes sure values on left and right are compatible
        // also prevents gcc warnings.
        code = _pop().append(" = (" + castType + ")").append(_pop());

        // If the right hand side is a newExpr, assign the correct class to
        // the variable on the left-hand-side.
        if (stmt.getRightOp() instanceof NewExpr) {
            stmt.getLeftOp().apply(this);
            // The &V is so that we get the name of the class structure.
            code.append(";\n" + indent + _pop() + "->class = &V"
                    + CNames.typeNameOf(stmt.getLeftOp().getType()));
        }

        _push(code);

    }

    /* Generate code for a breakpoint statement
     * @param stmt The statement.
     */
    public void caseBreakpointStmt(BreakpointStmt stmt) {
        defaultCase(stmt);
    }

    /* Generate the code for a cast expression.
     * @param v The expression.
     */
    public void caseCastExpr(CastExpr v) {
        //FIXME: Does not handle null cast.
        if (!v.getOp().toString().equals("null")) {
            _push("("+CNames.typeNameOf(v.getCastType())+")"
                    +CNames.localNameOf((Local)v.getOp()));
        }
        else {
            /*
              System.err.println("CSwitch.caseCastExpression does not"
              +"handle null.");
            */

            defaultCase(v);
        }
    }

    /* Generate the code for a caught exception reference.
     * @param v The caught expression reference
     */
    public void caseCaughtExceptionRef(CaughtExceptionRef v) {
        _push("exception_id");
    }


    /** Generate code for a Compare expression.
     *  @param v The expression.
     */
    public void caseCmpExpr(CmpExpr v) {
        _generateCompare(v);
    }

    /** Generate code for a Cmpg(compare greater than) expression. Presently
     * this is equivalent to generating code for a Cmp expression since NaN
     * is not supported at present.
     * @param v The expression.
     */
    public void caseCmpgExpr(CmpgExpr v) {
        _generateCompare(v);
    }

    /** Generate code for a Cmpl(compare less than) expression. Presently
     * this is equivalent to generating code for a Cmp expression since NaN
     * is not supported at present.
     * @param v The expression.
     */
    public void caseCmplExpr(CmplExpr v) {
        _generateCompare(v);
    }

    /* Generate code for a division.
     * @param v The division expression.
     */
    public void caseDivExpr(DivExpr v) {
        _generateBinaryOperation(v, "/");
    }


    /* Generate code for a double constant.
     * This makes approximations for +/- infinity by using large numbers
     * instead.
     * @param v The double constant.
     */
    public void caseDoubleConstant(DoubleConstant v) {
        String constant = v.toString();

        if ((constant.compareTo("#Infinity") == 0)
                ||(constant.compareTo("#NaN")) == 0){
            constant = new String("_MAX_DOUBLE");
            //as close to +infinity as we can get
        }
        else if (constant.compareTo("#-Infinity") == 0) {
            constant = new String("-_MAX_DOUBLE");
            // as close to -inf as we can get
        }

        _push("((double)" + constant +")");
    }

    /* Generate the code to enter a monitor for synchronization. This is
     * currently disabled.
     * @param v The enter monitor statement.
     */
    public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
        defaultCase(stmt);
    }

    /* Generate the code to check for equality.
     * @param v The equality expression.
     */
    public void caseEqExpr(EqExpr v) {
        _generateBinaryOperation(v, "==");
    }

    /* Generate the code to exit from a monitor (for synchronization).
     * This is currently disabled.
     * @param v The exit monitor statement.
     */
    public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
        defaultCase(stmt);
    }

    /* Generate the code for a floating point constant. NaN and infinities
     * are handled as large numbers.
     * @param v The floating point constant.
     */
    public void caseFloatConstant(FloatConstant v) {
        String constant = v.toString();

        if ( (constant.compareTo("#InfinityF") == 0)
                ||(constant.compareTo("#NaNF")      == 0)) {
            constant = new String("_MAX_FLOAT");
            //as close to +infinity as we can get
        }
        else if (constant.compareTo("#-InfinityF") == 0) {
            constant = new String("-_MAX_FLOAT");
            // as close to -inf as we can get
        }

        _push("((float) " + constant +")");
    }

    /* Generate the code for a "greater than or equal to" expression.
     * @param v The expression.
     */
    public void caseGeExpr(GeExpr v) {
        _generateBinaryOperation(v, ">=");
    }

    /* Generate the code for an unconditional goto statement for jumping to
     * a label.
     * @param stmt The statement.
     */
    public void caseGotoStmt(GotoStmt stmt) {
        _push("goto " + getLabel(stmt.getTarget()));
    }

    /* Generate the code for a "greater than" expression.
     * @param v The expression.
     */
    public void caseGtExpr(GtExpr v) {
        _generateBinaryOperation(v, ">");
    }

    /* Generate the code for an identity statement.
     * This is typically of the a = b type.
     * @param stmt The statement.
     */
    public void caseIdentityStmt(IdentityStmt stmt) {
        Value rightOp = stmt.getRightOp();
        if ((rightOp instanceof ParameterRef) || (rightOp instanceof ThisRef))
            return;
        else {
            rightOp.apply(this);
            stmt.getLeftOp().apply(this);

            String cast = new String ("("
                    + CNames.typeNameOf(stmt.getLeftOp().getType())
                    + ")");

            _push(_pop().append(" = " + cast ).append(_pop()));
        }
    }

    /** Generate code for an if statement.
     *  @param stmt The if statement.
     */
    public void caseIfStmt(IfStmt stmt) {
        stmt.getCondition().apply(this);
        _push(new StringBuffer("if (" + _pop() + ") "
                + "goto " + getLabel(stmt.getTarget())));
    }

    /** Generate code for a reference to a non-static field of an object.
     *  A non-static field belongs to an instance(object), not a class.
     *  @param v The instance field reference.
     */
    public void caseInstanceFieldRef(InstanceFieldRef v) {
        v.getBase().apply(this);
        _push(_pop().append("->").append(CNames.fieldNameOf(v.getField())));
    }

    /** Generate code for an instanceof expression.
     *  @param v The instanceof expression.
     */
    public void caseInstanceOfExpr(InstanceOfExpr v) {
        // instanceof is needed only for RefTypes.
        Type type = v.getCheckType();

        if ((type instanceof RefType)) {
            v.getOp().apply(this);
            _push(CNames.instanceOfFunction + "("
                    +"(PCCG_CLASS_INSTANCE*)"
                    + _pop() + ", "
                    + CNames.hashNumberOf(((RefType)type).getSootClass())
                    + ")");

            // Add the required include file.
            _context.addIncludeFile("\""
                    + CNames.includeFileNameOf(((RefType)type).getSootClass())
                    + "\"");
        }

        else {
            _unexpectedCase(v,
                    "Only RefTypes are supported for 'instanceof'");
        }
    }

    /** Push the value of an integer constant onto the code stack.
     *  @param v The integer constant.
     */
    public void caseIntConstant(IntConstant v) {
        int constant = v.value;
        int maxInt = 32767;

        if (constant > maxInt) {
            constant = maxInt;
        }
        else if (constant < -maxInt) {
            constant = -maxInt;
        }

        _push(Integer.toString(constant));
    }

    /** Generate code for calling a method that was declared in an
     * interface.
     * @param v The interface invoke expression.
     */
    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
        //_generateInstanceInvokeExpression(v);
        //defaultCase(v);
        SootMethod method = v.getMethod();

        v.getBase().apply(this);
        String instanceName = _pop().toString();
        String cast;

        String returnType = CNames.typeNameOf(method.getReturnType());

        if (!method.isStatic()) {
             cast = new String("(" + returnType + " (*) (void*, ...))");
        }
        else {
            cast = new String("(" + returnType
                    + " (*) "
                    + CNames.typeNameOf(method.getParameterType(0))
                    + ", ...))");
        }


        _push("("
                + cast + "(" +instanceName + "->class->lookup("
                + CNames.hashNumberOf(method)
                + ")))"
                + "( " + instanceName
                + _generateArguments(v, 1)
                + ")");
    }

    /** Generate code for an invoke statement.
     *  @param stmt The invoke statement.
     */
    public void caseInvokeStmt(InvokeStmt stmt) {
        stmt.getInvokeExpr().apply(this);
    }

    /** Generate code for a "less than or equal to" expression.
     *  @param v The expression.
     */
    public void caseLeExpr(LeExpr v) {
        _generateBinaryOperation(v, "<=");
    }

    /** Generate code for an array length expression. This
     *  is performed by inserting a call to a length computation
     *  macro from the pccg run-time library.
     *  @param v The length expression.
     */
    public void caseLengthExpr(LengthExpr v) {
        v.getOp().apply(this);
        _push(CNames.arrayLengthFunction + "(" + _pop() + ")");
    }


    /** Push the name of a local onto the code stack.
     *  @param l The local.
     */
    public void caseLocal(Local l) {
        _push(CNames.localNameOf(l));
    }

    /** Generate the code for a long constant.
     * @param v The long constant.
     */
    public void caseLongConstant(LongConstant v) {
        long constant = v.value;
        long maxLong = 2147483647;

        if (constant > maxLong) {
            constant = maxLong;
        }
        else if (constant < -maxLong) {
            constant = -maxLong;
        }

        _push(Long.toString(constant));
    }

    /* Generate the code for a lookup switch statement. There is also a
     * table switch statement that is slightly different.
     * @param stmt The statement.
     */
    public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
        StringBuffer code = new StringBuffer();
        int numberOfTargets = stmt.getTargetCount();
        int maxLong = 2147483647;

        code.append("switch ("
                + CNames.localNameOf((Local)stmt.getKey())
                + ") {\n");

        indentLevel++;

        for (int i = 0; i < numberOfTargets; i++) {
            int dummyLookupValue = stmt.getLookupValue(i);

            //FIXME: Integer Compatibility issue.
            if (dummyLookupValue > maxLong) {
                dummyLookupValue = maxLong;
                code.append(Utilities.comment(
                        "Warning: index out of range of long: "
                        + "truncated by CSWitch.caseLookupSwitchStmt()"));
            }
            else if(dummyLookupValue < -maxLong) {
                dummyLookupValue = -maxLong;
                code.append(Utilities.comment(
                        "Warning: index out of range of long: "
                        + "truncated by CSWitch.caseLookupSwitchStmt()"));
            }

            code.append(_indent() + "case " + dummyLookupValue + ": goto "
                    + getLabel(stmt.getTarget(i)) + ";\n");
        }

        code.append(_indent() + "default: goto "
                + getLabel(stmt.getDefaultTarget()) + ";\n");

        indentLevel--;
        code.append(_indent() + "}\n");
        _push(code);
    }

    /** Generate the code for a "less than" expression.
     *  @param v The expression.
     */
    public void caseLtExpr(LtExpr v) {
        _generateBinaryOperation(v, "<");
    }

    /** Generate the code for a multiply expression.
     *  @param v The expression.
     */
    public void caseMulExpr(MulExpr v) {
        _generateBinaryOperation(v, "*");
    }

    /** Generate the code for an inequality expression.
     *  @param v The expression.
     */
    public void caseNeExpr(NeExpr v) {
        _generateBinaryOperation(v, "!=");
    }

    /** Generate code for a "negative of" expression.
     *  @param v The expression.
     */
    public void caseNegExpr(NegExpr v) {
        _push("-" + CNames.localNameOf((Local)v.getOp()));
    }

    /* Generate code allocating space for a new array.
     * @param v The new array expression.
     */
    public void caseNewArrayExpr(NewArrayExpr v) {

        v.getSize().apply(this);
        String sizeCode = _pop().toString();
        _push(_generateArrayAllocation(v.getBaseType(), 1, sizeCode));
    }

    /** Generate code for a "new object" expression.
     *  @param v The expression.
     */
    public void caseNewExpr(NewExpr v) {
        Type type = v.getType();
        if (_debug) {
            System.out.println("new expr types: "
                    + v.getBaseType().getClass().getName()
                    + ", " + v.getType().getClass().getName() + ", "
                    + CNames.typeNameOf(v.getBaseType()) + ", "
                    + CNames.typeNameOf(v.getType()));
        }

        String name = CNames.typeNameOf(v.getType());
        // Put a #include for the appropriate type.
        if (type instanceof RefType) {
                SootClass sootClass = ((RefType)type).getSootClass();
                _context.addIncludeFile("\""
                    + CNames.includeFileNameOf(sootClass) + "\"");
        }
        _push("( malloc(sizeof(struct " + name + ")))");
    }

    /** Generate code for defining a new multidimensional array.
     *  @param v The "new multidimensional array" expression.
     */
    public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
        if (_debug) {
            System.out.println("NewMultiArrayExpr: " + v.getSizeCount() +
                    "/"
                    + v.getSizes().size()
                    + v.getBaseType().getClass().getName());
        }
        String sizeCode = new String();
        Iterator sizes = v.getSizes().iterator();
        while (sizes.hasNext()) {
            ((Value)sizes.next()).apply(this);
            sizeCode += _pop();
            if (sizes.hasNext()) sizeCode += ", ";
        }

        _push(_generateArrayAllocation(v.getBaseType(), v.getSizeCount(),
                sizeCode));
    }

    /* FIXME : Is this replaced by something else?
       public void caseNextNextStmtRef(NextNextStmtRef v) {
       defaultCase(v);
       }
    */

    /** Generate code for a Nop(No operation) statement.
     *  @param stmt The statement.
     */
    public void caseNopStmt(NopStmt stmt) {
        //do nothing
        //defaultCase(stmt);
    }

    /** Generate code for a null constant.
     *  @param v The constant.
     */
    public void caseNullConstant(NullConstant v) {
        _push("NULL");
    }

    /** Generate code for an "bitwise or" expression.
     *  @param v The expression.
     */
    public void caseOrExpr(OrExpr v) {
        _generateBinaryOperation(v,"|");
    }

    /** Generate code for a parameter reference. Not currently implemented.
     *  @param v The parameter reference.
     */
    public void caseParameterRef(ParameterRef v) {
        defaultCase(v);
    }

    /** Generate code for a remainder, or "mod", expression.
     *  @param v The expression.
     */
    public void caseRemExpr(RemExpr v) {
        _generateBinaryOperation(v, "%");
    }


    /** Generate code for a Ret statement. This is currently not supported.
     *  @param stmt The statement.
     */
    public void caseRetStmt(RetStmt stmt) {
        defaultCase(stmt);
    }

    /** Generate code for a "return" statement.
     *  @param stmt The statement.
     */
    public void caseReturnStmt(ReturnStmt stmt) {
        stmt.getOp().apply(this);

        String indent = new String();

        if (indentLevel == 1) {
            indent = "    ";
        }
        else if (indentLevel == 2) {
            indent = "        ";
        }

        // Do not do exception-management in single-class mode.
        if ((!Context.getSingleClassMode()) && exceptionsExist) {
            _push("\n"
                    + indent + "memcpy(env, caller_env, sizeof(jmp_buf));\n"
                    + indent + "epc = caller_epc;\n"
                    + indent + "return "
                    + "(" + CNames.typeNameOf(_returnType) + ")"
                    + _pop());
        }
        else {
            _push("\n" + indent + "return "
            + "(" + CNames.typeNameOf(_returnType) + ")"
            + _pop());
        }
    }

    /** Generate code for a "return void" statement.
     *  @param stmt The statement.
     */
    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
        String indent = new String();

        if (indentLevel == 1) {
            indent = "    ";
        }
        else if (indentLevel == 2) {
            indent = "        ";
        }

        // Do not do exception-management in single-class mode.
        if ((!Context.getSingleClassMode()) && exceptionsExist) {
            _push("\n"
                    + indent + "memcpy(env, caller_env, sizeof(jmp_buf));\n"
                    + indent + "epc = caller_epc;\n"
                    + indent + "return ");
        }
        else {
            _push("\n" +indent + "return ");
        }
    }

    /** Generate code for a shift-left expression.
     * @param v The expression.
     */
    public void caseShlExpr(ShlExpr v) {
        v.getOp2().apply(this);
        v.getOp1().apply(this);
        String number = _pop().toString();
        String shiftIndex = _pop().toString();

        int dataWidth = 32;
        // Number of bits in "int" for C. Java has 64bits, but we can't
        // implement that in C yet.
        Type dataType = v.getOp1().getType();

        if (dataType instanceof PrimType) {
            if (dataType instanceof ByteType) {
                dataWidth = 8;
            }
            else if (dataType instanceof IntType) {
                dataWidth = 32;
            }
            else if (dataType instanceof LongType) {
                dataWidth = 32;
                // FIXME: Wrong number of bits for "long".
                // But ANSI C supports only 32.
            }
            else if (dataType instanceof ShortType) {
                dataWidth = 16;
            }
        }

        dataWidth--; // Because it saturates at N-1 bits.

        _push("(" + shiftIndex + " <= " + dataWidth + ") ? "
                + "(" + number + " << "
                + "(" + shiftIndex + "%" + dataWidth + ")"
                + "):0");
    }

    /** Generate code for a Shift Right expression. The upper bits should
     * be filled with the sign.
     * @param v The expression.
     */
    public void caseShrExpr(ShrExpr v) {
        v.getOp2().apply(this);
        v.getOp1().apply(this);
        String number = _pop().toString();
        String shiftIndex = _pop().toString();

        int dataWidth = 32;
        // Number of bits in "int" for C. Java has 64bits, but we can't
        // implement that in C yet.
        Type dataType = v.getOp1().getType();

        if (dataType instanceof PrimType) {
            if (dataType instanceof ByteType) {
                dataWidth = 8;
            }
            else if (dataType instanceof IntType) {
                dataWidth = 32;
            }
            else if (dataType instanceof LongType) {
                dataWidth = 32;
                // FIXME: Wrong number of bits for "long". But ANSI C
                // supports only 32.
            }
            else if (dataType instanceof ShortType) {
                dataWidth = 16;
            }
        }

        dataWidth--; // Because it saturates at N-1 bits.

        // Note that we can simplify this further and generate more
        // streamlined code when number or shiftIndex are statically known
        // constants.
        // We use the % operator to suppress warnings.
        _push("(" + shiftIndex + " <= " + dataWidth + ") ? "
                + "("
                + "(" + number + " > 0)? "
                + "("
                + number + " >> "
                + "(" + shiftIndex + " % " + dataWidth + ")"
                + "): "
                + "( "
                + "( -"
                + "("
                + "(-"
                + "(" + number +")"
                + ") >> "
                + "("
                +"(" + shiftIndex + " % " + dataWidth + ")"
                + ")"
                + ")"
                + ")"
                + " - 1"
                + ") "
                + "): 0");
    }

    /** Generate code for a special invoke expression.
     *  @param v The expression.
     *  @return The code.
     */
    public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
        // Presently, we consider one case: a call to a method
        // that is declared by the superclass of the base. This
        // occurs when the base class constructor is invoked automatically
        // from with a given class constructor.
        SootMethod method = v.getMethod();

        // Generate cast for first argument of method.
        String cast = new String();

        SootClass declaringClass = method.getDeclaringClass();

        if (!declaringClass.isInterface()) {
            cast = "("
                + CNames.instanceNameOf(declaringClass)
                + "/* actual cast */)";
        }


        Iterator inheritedMethods = MethodListGenerator
            .getInheritedMethods(declaringClass)
            .iterator();

        while (inheritedMethods.hasNext()) {
            SootMethod inheritedMethod = (SootMethod)inheritedMethods
                .next();


            if (inheritedMethod.getSubSignature()
                    .equals(method.getSubSignature())) {
                cast = "("
                    + CNames.instanceNameOf(inheritedMethod
                            .getDeclaringClass())
                    + "/* inherited cast */)";
                break;
            }
        }

        if (!(v.getBase().getType() instanceof RefType)) {
            _unexpectedCase(v, "RefType base type expected.");
        } else {
            SootClass baseClass = ((RefType)(v.getBase().getType())).
                getSootClass();
            if (baseClass == declaringClass) {
                if (method.isStatic()) {
                    _unexpectedCase(v, "Non-static method expected.");
                }
                else {
                    _generateInstanceInvokeExpression(v);
                }
            }
            else if ((!baseClass.hasSuperclass()) ||
                    (baseClass.getSuperclass() != declaringClass)) {
                _unexpectedCase(v,
                        "Expected method class to be superclass of base");
                // If we are generating code in single class mode, then
                // we are not supporting inheritance, so ignore invocation
                // of the superclass constructor.
            } else if (Context.getSingleClassMode()) {
                return;
            } else {
                v.getBase().apply(this);
                StringBuffer baseCode = _pop();
                _push(CNames.classStructureNameOf(declaringClass)
                        + ".methods."
                        + CNames.methodNameOf(method)
                        + "("
                        + cast
                        + baseCode
                        + _generateArguments(v, 1)
                        + ")");
            }
        }
    }

    /** Generate code for a reference to a static field.
     *  @param v The static field reference.
     */
    public void caseStaticFieldRef(StaticFieldRef v) {
        SootField field = v.getField();
        SootClass className = field.getDeclaringClass();
        _push(CNames.classStructureNameOf(className)
                + ".classvars."
                + CNames.fieldNameOf(field));
        _context.addIncludeFile("\""
                + CNames.includeFileNameOf(className)
                + "\"");

    }

    /** Generate code for invoking a static method.
     *  @param v The static invoke expression.
     */
    public void caseStaticInvokeExpr(StaticInvokeExpr v) {
        if (!Context.getSingleClassMode()) {
            String includeFileName =
                v.toString().substring(v.toString().indexOf('<')+1,
                        v.toString().indexOf(':'));

            includeFileName = "\""
                + CNames.sanitize(includeFileName).replace('.', '/')
                + ".h\"";

            _context.addIncludeFile(includeFileName);
        }

        // The method that was invoked.
        SootMethod method = v.getMethod();

        // Handling native methods here.
        _push(CNames.functionNameOf(method) + "("
                + _generateArguments(v, 0) + ")");
    }

    /** Generate code for a string constant.
     *  @param v The string constant.
     */
    public void caseStringConstant(StringConstant v) {
        StringBuffer stringConst = new StringBuffer(v.value);

        // Convert \ to \\.
        for (int i = 0; i < stringConst.length(); i++) {
            if (stringConst.charAt(i) == '\\') {
                stringConst.insert(i, '\\');
                i++;
            }
        }


        // Convert " to \".
        for (int i = 0; i < stringConst.length(); i++) {
            if (stringConst.charAt(i) == '"') {
                stringConst.insert(i, '\\');
                i++;
            }
        }


        // Format newlines properly.
        for (int i = 0; i < stringConst.length(); i++) {
            if (stringConst.charAt(i) == '\n') {
                stringConst.replace(i,i+1, "\\n");
                i++;
            }
        }


        // Format \0 properly, avoid gcc warnings like:
        // 'warning: null character(s) preserved in literal'
        for (int i = 0; i < stringConst.length(); i++) {
            if (stringConst.charAt(i) == '\0') {
                stringConst.replace(i,i+1, "\\0");
                i++;
            }
        }

        // Format Control-M properly, avoid gcc warnings like:
        // 'warning: multi-line string literals are deprecated'
        for (int i = 0; i < stringConst.length(); i++) {
            if (stringConst.charAt(i) == '\r') {
                stringConst.replace(i,i+1, "\\r");
                i++;
            }
        }

        _push("charArrayToString(\"" + stringConst + "\")" );
    }

    /** Generate code for a subtract expression.
     *  @param v The expression.
     */
    public void caseSubExpr(SubExpr v) {
        _generateBinaryOperation(v, "-");
    }

    /** Generate code for a Table switch statement. There is also a
     *  related statement called a lookup switch statement.
     *  @param stmt The statement.
     */
    public void caseTableSwitchStmt(TableSwitchStmt stmt) {
        StringBuffer code = new StringBuffer();

        int min = stmt.getLowIndex();
        int max = stmt.getHighIndex();

        code.append("switch ("
                + CNames.localNameOf((Local)stmt.getKey())
                + ") {\n");

        indentLevel++;

        for (int i = min; i <= max; i++) {
            code.append(_indent() + "case " + i + ": goto "
                    + getLabel(stmt.getTarget(i-min)) + ";\n");
        }

        code.append(_indent() + "default: goto "
                + getLabel(stmt.getDefaultTarget()) + ";\n");

        indentLevel--;
        code.append(_indent() + "}\n");
        _push(code);
    }

    /** Generate code for a "this" reference. Currently not supported.
     *  @param v The reference.
     */
    public void caseThisRef(ThisRef v) {
        defaultCase(v);
    }

    /** Generate code for a throw statement.
     *  @param stmt The statement.
     */
    public void caseThrowStmt(ThrowStmt stmt) {

        String indent = new String();

        if (indentLevel == 1) {
            indent = "    ";
        }
        else if (indentLevel == 2) {
            indent = "        ";
        }

        _push("/* Throw exception of type " + stmt.getOp().getType().toString()
                + " */\n"
                + indent + "exception_id = (_EXCEPTION_INSTANCE)"
                + CNames.localNameOf((Local)stmt.getOp())+";\n"
                + indent + "longjmp(env, epc)");

    }

    /** Generate code for an "unsigned shift right" expression. This causes
     *  the empty upper bits to be filled with zeros.
     *  @param v The expression.
     */
    public void caseUshrExpr(UshrExpr v) {
        v.getOp2().apply(this);
        v.getOp1().apply(this);
        String number = _pop().toString();
        String shiftIndex = _pop().toString();

        int dataWidth = 32;
        // Number of bits in "int" for C. Java has 64bits, but we can't
        // implement that in C yet.
        Type dataType = v.getOp1().getType();

        // The "ifs" are in this order so that only the shortest data type
        // is chosen.
        if (dataType instanceof PrimType) {
            if (dataType instanceof ByteType) {
                dataWidth = 8;
            }
            else if (dataType instanceof ShortType) {
                dataWidth = 16;
            }
            else if (dataType instanceof IntType) {
                dataWidth = 32;
            }
            else if (dataType instanceof LongType) {
                dataWidth = 32;
                // FIXME: Wrong number of bits for "long". But ANSI C
                // supports only 32.
            }
        }

        long max = (long)Math.pow(2, dataWidth - 1) - 1;

        // Because we can only shift by width - 1
        dataWidth--;

        if (v.getOp2() instanceof IntConstant) {
            if (((IntConstant)v.getOp2()).value == 0) {
                _push(number);
                return; // Do not proceed to complex expression.
            }
        }

        // We can trust >> only for positive numbers.
        _push("(" + shiftIndex + " <= " + dataWidth + ")? "
                + "("
                + "(" + number + " > 0)? "
                + "(" + number + " >> "
                + "(" + shiftIndex + " % " + dataWidth
                + ")"
                + "): "
                + "("
                + "("
                + "("
                + "( - (" + number + "))"
                + " >> "
                + "("
                + "(" + shiftIndex + " -1 "
                + ")" + "%" + dataWidth
                + ")"
                + ")"
                + " | "
                + "("
                + "((unsigned long) " + max + ")"
                + ">>"
                + "(" + shiftIndex + "- 1)"
                + ")"
                + ")"
                + " - "
                + "("
                + "(-(" + number + ")) >> " + shiftIndex
                + ")"
                + ")"
                + "): 0");
    }

    /** Generate code for a virtual invoke expression.
     *  @param v The expression.
     */
    public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
        _generateInstanceInvokeExpression(v);
    }


    /** Generate code for an Xor expression.
     *  @param v The expression.
     */
    public void caseXorExpr(XorExpr v) {
        _generateBinaryOperation(v,"^");
    }

    /** Clear the set of branch targets encountered in the current method.
     */
    public void clearTargets() {
        _targetMap.clear();
        _targetCount = 0;
    }


    /** Generate code for unhandled expressions or statements.
     *  @param obj The object(expression or statement) to be handled.
     */
    public void defaultCase(Object obj) {
        if (obj instanceof Stmt) {
            _push("/*UNHANDLED STATEMENT: " + obj.getClass().getName() + "*/");
        }
        else if (obj instanceof Expr) {
            _push("epc++; epc-- /*UNHANDLED EXPRESSION HERE:"
                    +obj.getClass().getName() +"*/");
        }
        else {
            //neither statement nor expression
            _push("< UNHANDLED: "+obj.getClass().getName()+">");
        }

    }

    /** Retrieve the code generated by the switch since the last time
     *  code was retrieved from it.
     */
    public StringBuffer getCode() {
        StringBuffer result = new StringBuffer();
        Iterator fragments = _code.iterator();
        while (fragments.hasNext()) {
            result.append((StringBuffer)(fragments.next()));
        }
        _code.clear();
        return result;
    }

    /** Return the unique label associated with a unit that has been registered
     *  as the target of a branch from another statement.
     *  Uniqueness is with respect to the current method.
     *  @param unit The unit.
     *  @return The label.
     *  @exception RuntimeException If the unit has not yet been registered as
     *  the target of a branch.
     */
    public String getLabel(Unit unit) {
        String label;
        if ((label = ((String)_targetMap.get(unit))) == null) {
            throw new RuntimeException("Unit is not a branch target.\n"
                    + "The offending unit is: " + unit.toString() + "\n");
        }
        else return label;
    }

    /** Return the name of the local in the current method that represents
     *  the current class.
     *  @return The name.
     */
    public String getThisLocalName() {
        return _thisLocalName;
    }


    /** Return true if the given unit has been registered as the target of
     *  a branch from another statement.
     *  @param unit The unit.
     */
    public boolean isTarget(Unit unit) {
        return _targetMap.containsKey(unit);
    }

    /** Set the type to be cast for return statements
     *  @param type The type to be cast for a return statement.
     */
    public void setReturnType(Type type) {
        _returnType = type;
    }

    /** Set the name of the local in the current method that represents
     *  the current class.
     *  @param name The name.
     */
    public void setThisLocalName(String name) {
        _thisLocalName = name;
    }


    ///////////////////////////////////////////////////////////////////
    ////                    protected methods                      ////

    /** Generate code for a list of arguments from an invoke expression.
     *  @param expression The invoke expression.
     *  @param previousArguments The number of arguments that have already
     *  been generated for this invoke expression.
     *  @return The code.
     */
    protected String _generateArguments(InvokeExpr expression,
            int previousArguments) {
        StringBuffer code = new StringBuffer();
        Iterator args = expression.getArgs().iterator();
        SootMethod method = expression.getMethod();

        int count = previousArguments;
        while (args.hasNext()) {
            Type expectedParamType = method
                .getParameterType(count - previousArguments);

            if (count++ > 0) {
                code.append(", ");
            }

            ((Value)args.next()).apply(this);

            String cast = new String ("("
                    + CNames.typeNameOf(expectedParamType) + ") ");

            code.append( cast +  _pop() );

        }

        return code.toString();
    }

    /** Allocate memory for a given array.
     */
    protected String _generateArrayAllocation(Type elementType,
        int dimensionsToFill, String sizeCode) {
        int dimensions;
        // Determine the name of the run-time variable that
        // represents the array element class.
        String elementClass;
        String elementSizeType = "void*";

        // The number of dimensions is always 1, unless the element is
        // an array.
        dimensions = 1;
        if (elementType instanceof ArrayType) {
            dimensions = ((ArrayType)elementType).numDimensions;
            elementType = ((ArrayType)elementType).baseType;
        }
        if (elementType instanceof RefType) {
            elementClass = CNames.typeNameOf(elementType);
        }
        else {
            // If its a primitive type.
            elementClass = CNames.arrayClassPrefix +
                CNames.typeNameOf(elementType) + "_elem";
            elementSizeType = CNames.typeNameOf(elementType);
        }

        // Generate code for a call to a run-time function that
        // will allocate an array. This code should be completed
        // by the calling method with the appropriate dimension
        // and size.
        return CNames.arrayAllocateFunction + "((PCCG_CLASS_PTR)"
            + " malloc(sizeof(" + elementClass + "))"
            + ", sizeof(" + elementSizeType + "), "
            + dimensions + ", " + dimensionsToFill
            + ", " + sizeCode
            + ")";
    }

    /** Generate code for a binary operation expression.
     *  @param expression The expression.
     *  @param operator The string representation of the binary operator.
     */
    protected void _generateBinaryOperation(BinopExpr expression,
            String operator) {
        expression.getOp2().apply(this);
        expression.getOp1().apply(this);

        String cast = new String();

        cast = "("
            + CNames.typeNameOf(expression.getOp1().getType())
            + ")";

        _push(_pop().append(" " + operator + " " + cast).append(_pop()));
    }

    /** Generate code for a compare expression.
     * The following semantics of a compare expression is assumed: <BR>
     * (op1 > op2)  ==> return 1 <BR>
     * (op1 < op2)  ==> return -1 <BR>
     * (op1 == op2) ==> return 0 <BR>
     *  @param v The expression.
     */
    protected void _generateCompare(BinopExpr v) {
        v.getOp2().apply(this);
        v.getOp1().apply(this);
        String op1 = _pop().toString();
        String op2 = _pop().toString();
        _push("((" + op1 + " > " + op2 + ") ?  1 : (" +
                "(" + op1 + " < " + op2 + ") ?  -1 : 0))");

    }

    /** Generate code for an instance invoke expression.
     *  @param expression The instance invoke expression.
     */
    protected void _generateInstanceInvokeExpression(
            InstanceInvokeExpr expression) {

        SootMethod method = expression.getMethod();
        SootClass declaringClass = method.getDeclaringClass();

        // If the declaring class is an interface extending a method in
        // Object, then the first argument to non-static methods will be
        // the interface, not Object.
        if ((expression instanceof VirtualInvokeExpr)
                && (Scene.v().getSootClass("java.lang.Object")
                        .declaresMethod(method.getSubSignature()))
            ){
            Type baseType = expression.getBase().getType();
            if (baseType instanceof RefType) {
                declaringClass = ((RefType)baseType).getSootClass();
            }
        }

        StringBuffer code = new StringBuffer();

        expression.getBase().apply(this);
        StringBuffer instanceName = _pop();


        // We're using the class pointer only for abstract methods.
        // We don't do this if the instance is an array.
        code = new StringBuffer(
                instanceName
                + "->class->methods."
                + CNames.methodNameOf(method));

        String cast = new String();

        // Default cast is used only if the declaring class does not seem
        // to inherit this method.
        cast = "("
            + CNames.instanceNameOf(declaringClass)
            + "/* default cast */)";

        Iterator inheritedMethods = MethodListGenerator
            .getInheritedMethods(declaringClass)
            .iterator();

        while (inheritedMethods.hasNext()) {
            SootMethod inheritedMethod = (SootMethod)inheritedMethods
                .next();


            if (inheritedMethod.getSubSignature()
                    .equals(method.getSubSignature())) {
                cast = "("
                    + CNames.instanceNameOf(inheritedMethod
                            .getDeclaringClass())
                    + "/* inherited cast */)";
                break;
            }
        }


        code.append("("
                + cast
                + instanceName
                + _generateArguments(expression, 1)
                + ")");

        _push(code);
    }

    /** Returns the appropriate indentation based on the value of the
     *  "indentLevel" variable.
     *
     *  @return A String containing 4 spaces for every level of indentation.
     */
    protected String _indent() {
        return Utilities.indent(indentLevel);
    }

    /** Retrieve and remove the code at the top of the code stack.
     *  Code that exists on entry to a visitation method should not
     *  be removed (popped) by the method.
     *  @return The code at the top of the code stack.
     */
    protected StringBuffer _pop() {
        return (StringBuffer)(_code.pop());
    }

    /** Push a string buffer onto the code stack. On exit from a visitation
     *  method, any code that results from the method should be
     *  placed (pushed) on the stack in a single string buffer (or
     *  through a single call to {@link #_push(String)}). Any other code
     *  (intermediate strings that do not represent code generated by the
     *  method) should be removed from the stack before exiting the
     *  visitation method.
     *  @param codeString The string buffer.
     */
    protected void _push(StringBuffer codeString) {
        _code.push(codeString);
    }

    /** Push a string onto the code stack. See {@link #_push(StringBuffer)}.
     *  @param codeString The string.
     */
    protected void _push(String codeString) {
        _code.push(new StringBuffer(codeString));
    }

    /** Report an error, with an associated object and a descriptive message,
     *  for an unexpected code generation situation. The resulting message
     *  is pushed onto the top of the code stack.
     *  @param object The associated object.
     *  @param message The descriptive message.
     */
    protected void _unexpectedCase(Object object, String message) {
        _push("epc++ /* UNEXPECTED CASE "
                + object.getClass().getName()
                + " :" + message + " "
                + "*/; epc--");

        /*
          System.err.println("Unexpected code conversion case in CSwitch:\n"
          + "        " + message + "\n        Case object is of class "
          + object.getClass().getName());
        */
    }


    ///////////////////////////////////////////////////////////////////
    ////                    private variables                      ////

    // A stack of StringBuffers corresponding to code that has been generated
    // by this switch. The most recently-generated code is at the top of
    // the stack, and in general, older code resides lower in the stack.
    private Stack _code;

    // Code generation context.
    private Context _context;

    // Control local debugging output
    private static boolean _debug = false;

    // The number of branch targets encountered so far in the current method.
    private int _targetCount;

    // Map from units in a method into labels (for goto statements in the
    // generated code).
    private HashMap _targetMap;

    // The name of the local in the current method that represents the current
    // class.
    // FIXME: do we really need this field, and the associated methods?
    private String _thisLocalName;

    /** Type to be cast for return statements */
    private Type _returnType;

}
