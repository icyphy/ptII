/* Class that encapsulates functionality for generating C code deom a
   SootMethod.

 Copyright (c) 2002-2003 The University of Maryland.
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

@ProposedRating Red (<your email address>)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.TableSwitchStmt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// MethodCodeGenerator
/** Class that encapsulates functionality for generating C code from a
    SootMethod.

@author Ankush Varma
@version $Id$
@since  Ptolemy II 2.0
*/
public class MethodCodeGenerator {

    public MethodCodeGenerator(Context context, HashMap requiredTypeMap) {
        _context = context;
        _requiredTypeMap = requiredTypeMap;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Generate code for a method.
     *  @param method The method.
     *  @return The code.
     */
    public String generate(SootMethod method) {
        byte indentLevel = 0;
        if (method.isConcrete() && !method.isNative() &&
                !OverriddenMethodGenerator.isOverridden(method)) {
            StringBuffer code = new StringBuffer();
            JimpleBody body = (JimpleBody)(method.retrieveActiveBody());
            CSwitch visitor = new CSwitch(_context);

            // For catching exceptions.
            ExceptionTracker tracker = new ExceptionTracker();
            tracker.init(body);

            // Set the visitor to this return type too.
            visitor.setReturnType(method.getReturnType());

            // Initialize the labels for jump targets.
            _initializeLabels(visitor, tracker, method);

            String thisLocalName = null;
            // The union of the set of parameters to the method and its local
            // variables.
            HashSet parameterAndThisLocals = new HashSet();

            // Tell the visitor whether traps exist.
            visitor.exceptionsExist = tracker.trapsExist();

            // Generate the method head.
            code.append(_generateMethodDeclaration(method
                    , parameterAndThisLocals, thisLocalName));

            // Generate declarations for variables used for
            // exception-catching.
            code.append(_declareExceptionVariables(tracker));
            // Generate declarations and initializations for local variables.
            code.append(_generateLocal(method, parameterAndThisLocals));

            code.append(_generateMethodBody(method, visitor, tracker
                    , thisLocalName));

            String description = "Function that implements Method "
                + method.getSignature();

            code.append("} ");
            code.append(_comment(description));
            return code.toString();
        } else {
            if (method.isNative() /*|| method.isAbstract()*/) {
                _updateRequiredTypes(method.getReturnType());
                return NativeMethodGenerator.getCode(method);
            }
            else if (OverriddenMethodGenerator.isOverridden(method)) {
                _updateRequiredTypes(method.getReturnType());
                return OverriddenMethodGenerator.getCode(method);
            }
            else {
                return "";
            }
        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Enclose a given string of text within appropriate delimiters to
     *  form a comment in the generated code.
     *  @param text The text to place in the generated comment.
     *  @return The generated comment.
     */
    protected final String _comment(String text) {
        return Utilities.comment(text);
    }


    /** Generate the declarations for variables used in exception-tracking.
        @param tracker The ExceptionTracker for the method for which the
        declarations are needed.
        @return A string containing these declarations.
    */
    protected String _declareExceptionVariables(ExceptionTracker tracker) {
        StringBuffer code = new StringBuffer();

        if (tracker.trapsExist()) {
            code.append(_indent(1)
                    + _comment("Variables used for catching exceptions.")
                    + "\n");
            code.append(_indent(1) + "extern jmp_buf env;\n");
            code.append(_indent(1) + "extern int epc;\n");
            code.append(_indent(1) + "jmp_buf caller_env;\n");
            code.append(_indent(1) + "int caller_epc;\n");
            if (tracker.trapsExist()) {

                code.append(_indent(1)
                        + "extern _EXCEPTION_INSTANCE exception_id;\n");
            }
        }

        return code.toString();

    }

    /** Generate the epilogue.
     *  The method below generates epilogue for the code in a method.
     *  @param tracker
     *  @param visitor
     *  @return The code.
     */
    protected String _generateEpilogue(ExceptionTracker tracker, CSwitch
            visitor) {

        StringBuffer code = new StringBuffer();

        //Epilogue
        if (tracker.trapsExist()) {
            code.append(_indent(1) + "}\n");

            code.append(_indent(1) + "else\n");
            code.append(_indent(1) + "{\n");

            //Code for mapping a trap name to an exception.
            code.append(_generateExceptionMap(tracker, visitor));
        }
        return code.toString();
    }


    /** Generate the exception map.
     *  The method below generates exception map for the code.
     *  @param tracker
     *  @param visitor
     *  @return The code.
     */
    protected String _generateExceptionMap(ExceptionTracker tracker
            , CSwitch visitor) {

        StringBuffer code = new StringBuffer();

        //Code for mapping an exception type to its handler.
        code.append("\n"+_indent(2)+
                "/* Map exception_id to handler */\n");
        code.append(_indent(2)+"switch (epc)\n");
        code.append(_indent(2)+"{\n");
        for (int i = 0;i<= (tracker.getEpc()-1);i++) {
            code.append(_indent(3)+"case "+(i)+":\n");
            if (tracker.getHandlerUnitList(i).size()>0) {
                Iterator j = tracker.getTrapsForEpc(i).listIterator();

                code.append(_indent(4));
                while (j.hasNext()) {
                    Trap currentTrap = (Trap)j.next();
                    code.append("if (PCCG_instanceof("
                            + "(PCCG_CLASS_INSTANCE*)exception_id, "
                            + CNames.hashNumberOf(currentTrap
                                    .getException())
                            + "))\n");
                    code.append(_indent(4) + "{\n");
                    code.append(_indent(5) + "goto " +
                            visitor.getLabel(currentTrap.getHandlerUnit())
                            + ";\n");
                    code.append(_indent(4) + "}\n");
                    code.append(_indent(4) + "else ");
                }

                // For the last else.
                code.append("\n"+_indent(4) + "{\n");
                code.append(_indent(5) +
                        "longjmp(caller_env, caller_epc);\n");
                code.append(_indent(5) +
                        "/* unhandled exception: " +
                        "return control to caller */\n");
                code.append(_indent(4) + "}\n");
            }
            else {
                code.append(_indent(4) +
                        "/* No active Traps for this epc. */\n");
            }

        }
        code.append(_indent(3) +
                "default: longjmp(caller_env, caller_epc);\n");

        code.append(_indent(2) + "}\n");

        code.append(_indent(1) + "}\n");
        return code.toString();
    }


    /** Generate code to declare and initialize local variables.
     *  The method below generates local declarations for the code, and
     *  initializes them to NULL pointers if they are RefTypes. If they
     *  are not Reftypes, they are initialized to 0.
     *  @param method The method for which declarations are
     *  needed.
     *  @param parameterAndThisLocals The parameters of this method, and
     *  variables that are local to the instance of the class to which this
     *  method belongs. These variables do <B>NOT</B> need to be declared
     *  or initialized by this method.
     *  @return The code.
     */
    protected String _generateLocal(SootMethod method
            , HashSet parameterAndThisLocals) {
        StringBuffer code = new StringBuffer();

        JimpleBody body = (JimpleBody)method.retrieveActiveBody();

        if (body.getLocals().size() > 0) {
            // Declare local variables.
            Iterator locals = body.getLocals().iterator();
            code.append(_indent(1)
                    + _comment("Declarations for local variables."));

            while (locals.hasNext()) {
                Local nextLocal = (Local)(locals.next());
                if (!parameterAndThisLocals.contains(nextLocal)) {
                    code.append(_indent(1));
                    Type localType = nextLocal.getType();
                    code.append(CNames.typeNameOf(localType));
                    code.append(" " + CNames.localNameOf(nextLocal) + ";\n");
                    _updateRequiredTypes(localType);
                }
            }
            code.append("\n");

            // Initialize local variables.
            code.append(_indent(1)
                    + _comment("Initializations for local variables."));

            locals = body.getLocals().iterator();
            while (locals.hasNext()) {
                Local nextLocal = (Local)(locals.next());
                if (!parameterAndThisLocals.contains(nextLocal)) {
                    code.append(_indent(1)
                            + CNames.localNameOf(nextLocal)
                            + " = ");

                    // Set RefTypes to NULL pointers, and all other variables
                    // to 0.
                    if (nextLocal.getType() instanceof RefType) {
                        code.append("NULL;\n");
                    }
                    else {
                        code.append("0;\n");
                    }
                }
            }
        }

        return code.toString();
    }


    /** Generate the code for the body of a method.
     *  @param method The method for which code is needed.
     *  @param visitor The visitor.
     *  @param tracker The ExceptionTracker.
     *  @param thisLocalName The local name.
     *  @return The code.
     */
    protected String _generateMethodBody(SootMethod method, CSwitch visitor
            , ExceptionTracker tracker, String thisLocalName) {

        JimpleBody body = (JimpleBody)method.retrieveActiveBody();
        StringBuffer code = new StringBuffer();
        visitor.indentLevel = 0;

        // Generate the method body.
        Iterator units = body.getUnits().iterator();
        if (thisLocalName != null) visitor.setThisLocalName(thisLocalName);
        units = body.getUnits().iterator();

        if (!Context.getSingleClassMode()) {
            code.append(_generateMethodPrologue(tracker, visitor));
        }
        else {
            visitor.indentLevel = 1;
        }

        code.append("\n" + _generateMethodUnitCode(tracker, visitor
                , method, visitor.indentLevel));

        code.append(_generateEpilogue(tracker, visitor));

        return code.toString();
    }

    /** Generate the method header for the code.
     *  The method below generates the method header.
     *  @param method The method.
     *  @param parameterAndThisLocals The set of parameters and local
     *  variables for this method.
     *  @param thisLocalName
     *  @return The code for the method's declaration(its head).
     */
    protected String _generateMethodDeclaration(SootMethod method, HashSet
            parameterAndThisLocals, String thisLocalName) {
        JimpleBody body = (JimpleBody)method.retrieveActiveBody();
        StringBuffer code = new StringBuffer();
        String description = "Function that implements Method " +
            method.getSignature();
        Type returnType = method.getReturnType();
        code.append(_comment(description));
        code.append(CNames.typeNameOf(returnType));
        _updateRequiredTypes(returnType);
        code.append(" ");
        code.append(CNames.functionNameOf(method));
        code.append("(");
        int parameterIndex;
        int parameterCount = 0;
        if (!method.isStatic()) {
            parameterAndThisLocals.add(body.getThisLocal());
            thisLocalName = CNames.localNameOf(body.getThisLocal());
            code.append(CNames.instanceNameOf(method.getDeclaringClass()) +
                    " " + thisLocalName);
            parameterCount++;
        }

        for (parameterIndex = 0;parameterIndex < method.getParameterCount();
             parameterIndex++) {
            if (parameterCount++ > 0) code.append(", ");
            Local local = body.getParameterLocal(parameterIndex);
            parameterAndThisLocals.add(local);
            Type parameterType = local.getType();
            code.append(CNames.typeNameOf(parameterType) + " "
                    + CNames.localNameOf(local));
            _updateRequiredTypes(parameterType);
        }
        code.append(")\n{\n");
        return code.toString();
    }


    /** Generate prologue code to be inserted in a method for
     *  exception-catching.
     *  @param tracker The ExceptionTracker that has the information for
     *  exceptions in this method.
     *  @param visitor The local CSwitch visitor object.
     *  @return The prologue code.
     */
    protected String _generateMethodPrologue(ExceptionTracker tracker
            , CSwitch visitor) {

        StringBuffer code = new StringBuffer();
        byte indentLevel = 1;

        code.append("\n");
        if (tracker.trapsExist()) {
            code.append(_indent(1) + "caller_epc = epc;\n");
            code.append(_indent(1)
                    + "memcpy(caller_env, env, sizeof(jmp_buf));\n");

            code.append("\n");

            code.append(_indent(1) + "epc = setjmp(env);\n");
            code.append(_indent(1) + "if (epc == 0)\n");
            code.append(_indent(1) + "{\n");

            indentLevel = 2;
        }

        visitor.indentLevel = indentLevel;

        return code.toString();
    }

    /** Generate the method unit code and code for handling exceptions.
     *
     *  @param tracker The ExceptionTracker object handing exceptions here.
     *  @param visitor The CSwitch visitor.
     *  @param method The method for which code is needed.
     *  @param indentLevel The level of indentation needed in each
     *  statement.
     *  @return The code.
     */
    protected String _generateMethodUnitCode(ExceptionTracker tracker,
            CSwitch visitor, SootMethod method, byte indentLevel) {
        JimpleBody body = (JimpleBody)method.retrieveActiveBody();
        StringBuffer code = new StringBuffer();

        //Exception-catching in the body.
        Iterator units = body.getUnits().iterator();
        boolean handle_exceptions = tracker.trapsExist()
            && (!Context.getSingleClassMode());

        while (units.hasNext()) {
            Unit unit = (Unit)(units.next());
            if (visitor.isTarget(unit)) {
                code.append(visitor.getLabel(unit) + ":\n");
            }

            //Code for begin Unit in exceptions.
            if (handle_exceptions && tracker.isBeginUnit(unit)) {
                tracker.beginUnitEncountered(unit);
                code.append(_indent(2) + "epc = " + tracker.getEpc()+";\n");
                code.append(_indent(2) + "/*Trap " + tracker.beginIndexOf(unit)
                        +" begins. */\n");
            }


            //Actual unit code.
            unit.apply(visitor);
            StringBuffer newCode = visitor.getCode();
            if (newCode.length() > 0) {
                code.append(_indent(indentLevel)).append(newCode + ";");
                code.append("/* "
                        + unit.toString().replace('/', '@') + " */");
                code.append("\n");
            }

            //Code for end unit in exceptions.
            if (handle_exceptions && tracker.isEndUnit(unit)) {
                code.append(_indent(2)+"/* That was end unit for trap "
                        + tracker.endIndexOf(unit) + " */\n");
                tracker.endUnitEncountered(unit);
                code.append(_indent(2)+"epc = " + tracker.getEpc() + ";\n");

            }

            //Code for handler unit in exceptions.
            if (handle_exceptions && tracker.isHandlerUnit(unit)) {
                code.append(_indent(2) + "/* Handler Unit for Trap " +
                        tracker.handlerIndexOf(unit) + " */\n");
            }

        }
        return code.toString();
    }


    /** Return a string that generates an indentation string (a sequence
     *  of spaces) for the given indentation level. Each indentation
     *  level unit is four characters wide.
     *  @param level The indentation level.
     *  @return The indentation string that corresponds to the given
     *  indentation level.
     *  Same as the corresponding method in CodeGenerator
     */
    protected String _indent(int level) {
        return Utilities.indent(level);
    }

    /** Initialize the labels for branch targets in the method.
     *  @param visitor The visitor design pattern.
     *  @param tracker The ExceptionTracker.
     *  @param method  The method for which labels need to be initialized.
     */
    protected void _initializeLabels(CSwitch visitor, ExceptionTracker
            tracker, SootMethod method) {
        JimpleBody body = (JimpleBody)method.retrieveActiveBody();
        Iterator units = body.getUnits().iterator();
        while (units.hasNext()) {
            Unit unit = (Unit)(units.next());
            Unit target = null;
            // Direct "goto".
            if (unit instanceof GotoStmt) {
                target = ((GotoStmt)unit).getTarget();
                if (target != null) {
                    visitor.addTarget(target);
                }
            }
            // Target of "if" statement.
            else if (unit instanceof IfStmt) {
                target = ((IfStmt)unit).getTarget();
                if (target != null) {
                    visitor.addTarget(target);
                }
            }
            // Handler for exceptions.
            else if (tracker.isHandlerUnit(unit)){
                target = unit;
                if (target != null) {
                    visitor.addTarget(target);
                }
            }
            // All targets for switch statements must be added.
            else if (unit instanceof TableSwitchStmt) {
                Iterator targets = ((TableSwitchStmt)unit).getTargets()
                    .iterator();

                while (targets.hasNext()) {
                    visitor.addTarget((Unit)targets.next());
                }

                visitor.addTarget(((TableSwitchStmt)unit).getDefaultTarget());
            }
            else if (unit instanceof LookupSwitchStmt) {
                Iterator targets = ((LookupSwitchStmt)unit).getTargets()
                    .iterator();

                while (targets.hasNext()) {
                    visitor.addTarget((Unit)targets.next());
                }

                visitor.addTarget(((LookupSwitchStmt)unit).getDefaultTarget());
            }
        }
    }

    /** Register a type as a type that must be imported into the generated code
     *  through an #include directive. The request is processed only if the
     *  argument
     *  is a RefType, or if it is an ArrayType with a RefType as the base type.
     *  All other requests are ignored. Duplicate requests are also ignored.
     *  @param type The type.
     *  Same as the corresponding method in CodeGenerator
     */
    protected void _updateRequiredTypes(Type type) {
        if (!_context.getDisableImports()) {
            SootClass source = null;
            if (type instanceof RefType) {
                source = ((RefType)type).getSootClass();
            } else if ((type instanceof ArrayType) &&
                    (((ArrayType)type).baseType instanceof RefType)) {
                source = ((RefType)(((ArrayType)type).baseType)).getSootClass();
            }
            if (source != null) {

                if (!_requiredTypeMap.containsKey(source)) {
                    _requiredTypeMap.put(source,
                            CNames.includeFileNameOf(source));
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    // Code generation context information.
    Context _context;

    // Mapping from classes that the current class depends on to their
    // include file names.
    HashMap _requiredTypeMap;
}
