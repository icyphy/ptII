/*
A C code generator for generating "code files" (.c files) that implement
Java classes.

Copyright (c) 2001-2002 The University of Maryland.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.SootClass;
import soot.SootMethod;
import soot.Scene;
import soot.Type;
import soot.Local;
import soot.Unit;
import soot.Trap;

import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.JimpleBody;

/** A C code generator for generating "code files" (.c files) that implement
    Java classes.

    @author Shuvra S. Bhattacharyya, Ankush Varma
    @version $Id$
    @since Ptolemy II 2.0
*/

// FIXME: Handle (ignore?) phantom methods and fields.

public class CodeFileGenerator extends CodeGenerator {

    /** Construct a code file generator.
     */
    public CodeFileGenerator() {
        super();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate C code for a class, including code (function declarations)
     *  for all of its methods, and for its initialization function.
     *  @param source The class.
     *  @return The code.
     */
    public String generate(SootClass source) {
        StringBuffer bodyCode = new StringBuffer();
        StringBuffer headerCode = new StringBuffer();
        _context.clear();

        source.setApplicationClass();
        _updateRequiredTypes(source.getType());

        // Add runtime include files.
        _context.addIncludeFile("<setjmp.h>");
        _context.addIncludeFile("<stdlib.h>");
        _context.addIncludeFile("\"pccg_runtime.h\"");

        if (!_context.getSingleClassMode()) {
            _context.addIncludeFile("\"strings.h\"");
        }

        // Generate function prototypes for all private methods.
        int count = 0;
        Iterator methods = source.getMethods().iterator();
        while (methods.hasNext()) {
            SootMethod method = (SootMethod)(methods.next());
            if (method.isPrivate()) {
                if (count++ == 0) {
                    bodyCode.append(_comment("Prototypes for functions that "
                            + "implement private methods"));
                }
                if (method.isNative()) {
                    bodyCode.append("extern ");
                }
                bodyCode.append(_generateMethodHeader(method) + ";\n");
            }
        }
        bodyCode.append("\n");


        // Generate the code for all of the methods.
        methods = source.getMethods().iterator();
        while (methods.hasNext()) {
            SootMethod thisMethod = (SootMethod)methods.next();
            String methodCode = _generateMethod(thisMethod);
            // Only generate required methods.
            if (RequiredFileGenerator.isRequiredMethod(thisMethod)) {
                bodyCode.append(methodCode);
            }
            if (methodCode.length() != 0) {
                bodyCode.append("\n");
            }
        }

        // Declare the run-time structure that is to contain class information.
        bodyCode.append(_comment("Structure that contains class information"));
        bodyCode.append("struct " + CNames.classNameOf(source) + " "
                + CNames.classStructureNameOf(source) + ";\n\n");

        // Generate code for the function that initializes the class.
        bodyCode.append(_generateClassInitialization(source));

        headerCode.append(_generateIncludeDirectives() + "\n");

        headerCode.append(_declareConstants() + "\n");

        // Generate the typedefs for the array instances.
        headerCode.append(_generateArrayInstanceDeclarations());

        return (headerCode.append(bodyCode)).toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate declarations for constants in the generated code.
     *  @return The generated declarations for constants.
     */
    protected StringBuffer _declareConstants() {
        StringBuffer code = new StringBuffer();
        String typeName = CNames.instanceNameOf(Scene.v().
                getSootClass("java.lang.String"));
        Iterator stringConstants = _context.getStringConstants();

        if (stringConstants.hasNext()) {
            code.append(_comment("Pointers to string constants"));
            while (stringConstants.hasNext()) {
                code.append("static " + typeName + " "
                        + _context.getIdentifier((String)(stringConstants.
                                next()))
                        + ";\n");
            }
        }
        return code;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Generate initialization code for structures that implement classes.
     *  Code for a C function is returned. This function implements
     *  initialization required for the class-specific structure
     *  that is pointed to by its argument.
     *  @param source The class.
     *  @return Initialization code for the class.
     */
    private String _generateClassInitialization(SootClass source) {
        StringBuffer code = new StringBuffer();
        final String argumentName = "class";
        final String argumentReference = argumentName + "->";
        code.append(_comment("Function that initializes structure for Class "
                + source.getName()));
        code.append("void " + CNames.initializerNameOf(source) + "("
                + CNames.classNameOf(source) + " " + argumentName
                + ") {\n");
        if (!_context.getSingleClassMode()) {
            code.append(_generateMethodPointerInitialization(
                    MethodListGenerator.getInheritedMethods(source),
                    argumentReference));
        }
        code.append(_generateMethodPointerInitialization(
                MethodListGenerator.getNewMethods(source),
                argumentReference));
        code.append(_generateMethodPointerInitialization(
                MethodListGenerator.getConstructors(source),
                argumentReference));
        code.append(_generateMethodPointerInitialization(
                MethodListGenerator.getPrivateMethods(source),
                argumentReference));

        Iterator stringConstants = _context.getStringConstants();

        if (stringConstants.hasNext()) {
            SootClass stringClass = Scene.v().getSootClass("java.lang.String");
            String stringType = CNames.instanceNameOf(stringClass);
            String stringStructure = CNames.classStructureNameOf(stringClass);
            String stringInitializer = CNames.methodNameOf(
                    stringClass.getMethod("void <init>(char[])"));
            code.append("\n" + _indent(1) +
                    _comment("Initialization of string constants"));
            while (stringConstants.hasNext()) {
                StringBuffer value = new StringBuffer(
                                        stringConstants.next().toString());
                // Replace all incidences of " with \" to prevent bad characters
                // between quotes.
                for(int i = 0; i<value.length(); i++) {
                    if (value.charAt(i) == '"') {
                        value.insert(i,"\\");
                        i++;
                    }
                }

                String identifier = _context.getIdentifier(value.toString());
                code.append(_indent(1) + identifier + " = (" + stringType +
                        ")(malloc(sizeof (struct " + stringType + ")));\n");

                code.append(_indent(1) + stringStructure + ".methods."
                        + stringInitializer
                        + "(" + identifier + ", \"" + value + "\");\n");
            }
        }

        // Invoke the static initializer method for the class if it exists.
        SootMethod initializer;
        if ((initializer = MethodListGenerator.getClassInitializer(source))
                != null) {
            code.append("\n" + _indent(1)
                    + _comment("Static initializer method"));
            code.append(_indent(1) +
                            CNames.functionNameOf(initializer) + "();\n");
        }

        // Set up the superclass pointer.
        code.append("\n" + _indent(1) + argumentReference +
                CNames.superclassPointerName() + " = ");
        if (!_context.getSingleClassMode() && source.hasSuperclass()) {
            code.append("&" + CNames.classStructureNameOf(
                    source.getSuperclass()));
            _updateRequiredTypes(source.getSuperclass().getType());

        } else {
            code.append("NULL");
            _context.addIncludeFile("<stdio.h>");
        }
        code.append(";\n");

        code.append("}\n");
        return code.toString();
    }



    /** Generate code for a method.
     *  @param method The method.
     *  @return The code.
     */
    private String _generateMethod(SootMethod method) {

        byte indentLevel;
        if (method.isConcrete() && !(method.isNative())) {
            StringBuffer code = new StringBuffer();
            String description = "Function that implements Method " +
                    method.getSubSignature();
            code.append(_comment(description));
            JimpleBody body = (JimpleBody)(method.retrieveActiveBody());
            CSwitch visitor = new CSwitch(_context);

            // Generate the method header.
            Type returnType = method.getReturnType();

            // Set the visitor to this return type too.
            visitor.returnType = returnType;

            code.append(CNames.typeNameOf(returnType));
            _updateRequiredTypes(returnType);
            code.append(" ");
            code.append(CNames.functionNameOf(method));
            code.append("(");
            int parameterIndex;
            int parameterCount = 0;
            String thisLocalName = null;
            HashSet parameterAndThisLocals = new HashSet();
            if (!method.isStatic()) {
                parameterAndThisLocals.add(body.getThisLocal());
                thisLocalName = CNames.localNameOf(body.getThisLocal());
                code.append(CNames.instanceNameOf(method.getDeclaringClass()) +
                        " " + thisLocalName);
                parameterCount++;
            }

            for(parameterIndex = 0;parameterIndex < method.getParameterCount();
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

            // Generate local declarations.
            Iterator locals = body.getLocals().iterator();
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

            // For catching exceptions.
            ExceptionTracker tracker = new ExceptionTracker();
            tracker.init(body);

            // Construct labels for branch targets
            Iterator units = body.getUnits().iterator();
            while (units.hasNext()) {
                Unit unit = (Unit)(units.next());
                Unit target = null;
                if (unit instanceof GotoStmt) {
                    target = ((GotoStmt)unit).getTarget();
                } else if (unit instanceof IfStmt) {
                    target = ((IfStmt)unit).getTarget();
                } else if (tracker.isHandlerUnit(unit)){
                    target = unit;
                }
                if (target != null) {
                    visitor.addTarget(target);
                }
            }

            // Generate the method body.
            if (thisLocalName != null) visitor.setThisLocalName(thisLocalName);
            units = body.getUnits().iterator();

            if (!_context.getSingleClassMode()) {
                // Prologue.
                code.append(_indent(1) + "extern jmp_buf env;\n");
                code.append(_indent(1) + "extern int epc;\n");
                code.append(_indent(1) + "jmp_buf caller_env;\n");
                code.append(_indent(1) + "extern char *exception_type;\n");
                code.append(_indent(1) + "extern int exception_id;\n");
                code.append(_indent(1) + "int caller_epc = epc;\n\n");

                //FIXME: this is a dummy to suppress warnings
                code.append(_indent(1)+ "exception_id = 13;   /*dummy*/\n");
                code.append(_indent(1)
                        + "strcpy(exception_type, \"13\"); /*dummy*/\n");

                code.append("\n"+_indent(1)
                            +"memcpy(caller_env, env, sizeof(jmp_buf));\n");

                if (tracker.trapsExist()) {
                    code.append(_indent(1)+"epc = setjmp(env);\n");
                    code.append(_indent(1)+"if(epc == 0)\n");
                    code.append(_indent(1)+"{\n");
                }

                if (tracker.trapsExist()) {
                    indentLevel = 2;
                }
                else {
                    indentLevel = 1;
                }
            }
            else {
                indentLevel = 1;
            }

            visitor.indentLevel = indentLevel;

            boolean handle_exceptions = tracker.trapsExist()
                    && (!_context.getSingleClassMode());

            //Exception-catching in the body.
            while (units.hasNext()) {
                Unit unit = (Unit)(units.next());
                if (visitor.isTarget(unit)) {
                    code.append(visitor.getLabel(unit) + ":\n");
                }

                //Code for begin Unit in exceptions.
                if (handle_exceptions && tracker.isBeginUnit(unit)) {
                    tracker.beginUnitEncountered(unit);
                    code.append(_indent(2)+"epc = "+ tracker.getEpc()+";\n");
                    code.append(_indent(2)+"/*Trap " +tracker.beginIndexOf(unit)
                                +" begins. */\n");
                }


                //Actual unit code.
                unit.apply(visitor);
                StringBuffer newCode = visitor.getCode();
                if (newCode.length() > 0) {
                    code.append(_indent(indentLevel)).append(newCode).
                            append(";\n");
                }

                //Code for end unit in exceptions.
                if(handle_exceptions && tracker.isEndUnit(unit)) {
                    code.append(_indent(2)+"/* That was end unit for trap "+
                                tracker.endIndexOf(unit)+" */\n");
                    tracker.endUnitEncountered(unit);
                    code.append(_indent(2)+"epc = "+tracker.getEpc()+";\n");

                }

                //Code for handler unit in exceptions.
                if(handle_exceptions && tracker.isHandlerUnit(unit)) {
                    code.append(_indent(2)+"/* Handler Unit for Trap "+
                                tracker.handlerIndexOf(unit)+" */\n");
                }

            }

            //Epilogue
            if(tracker.trapsExist()) {
                code.append(_indent(1)+"}\n");

                code.append(_indent(1)+"else\n");
                code.append(_indent(1)+"{\n");

                //Code for mapping a trap name to an exception.
                code.append(_indent(2)+
                            "/* Map exception_id to exception_type */\n");
                code.append(_indent(2)+
                            "strcpy(exception_type, (char *) exception_id);\n");
                //FIXME: This is not the correct mapping.

                //Code for mapping an exception type to its handler.
                code.append("\n"+_indent(2)+
                            "/* Map exception_type to handler */\n");
                code.append(_indent(2)+"switch (epc)\n");
                code.append(_indent(2)+"{\n");
                for(int i = 0;i<= (tracker.getEpc()-1); i++) {
                    code.append(_indent(3)+"case "+(i)+":\n");
                    if (tracker.getHandlerUnitList(i).size()>0) {
                        Iterator j = tracker.getTrapsForEpc(i).listIterator();

                        code.append(_indent(4));
                        while (j.hasNext()) {
                                Trap currentTrap = (Trap)j.next();
                                code.append("if (strcmp(exception_type, \""+
                                        currentTrap.getException()+"\"))\n");
                                code.append(_indent(4)+"{\n");
                                code.append(_indent(5)+"goto " +
                                  visitor.getLabel(currentTrap.getHandlerUnit())
                                    +";\n");
                                code.append(_indent(4)+"}\n");
                                code.append(_indent(4)+"else ");
                            }

                        // For the last else.
                        code.append("\n"+_indent(4)+"{\n");
                        code.append(_indent(5)+
                            "longjmp(caller_env, caller_epc);\n");
                        code.append(_indent(5)+
                                    "/* unhandled exception: "+
                                    "return control to caller */\n");
                        code.append(_indent(4)+"}\n");
                    }
                    else {
                        code.append(_indent(4)+
                            "/* No active Traps for this epc. */\n");

                    }

                }
                code.append(_indent(3)+
                            "default: longjmp(caller_env, caller_epc);\n");

                code.append(_indent(2)+"}\n");


                code.append(_indent(1)+"}\n");


            }

            // Trailer code
            code.append("} ");
            code.append(_comment(description));
            return code.toString();
        } else {
            return "";
        }
    }

    /** Generate code to initialize method pointers (in the method table)
        in a structure that implements a class.
     */
    private String _generateMethodPointerInitialization(List methodList,
            String argumentReference) {
        StringBuffer code = new StringBuffer();
        Iterator methods = methodList.iterator();
        while (methods.hasNext()) {
            SootMethod method = (SootMethod)(methods.next());
            if (!method.isStatic()
                    && RequiredFileGenerator.isRequiredMethod(method)) {
                code.append(_indent(1) + argumentReference
                        + "methods." + CNames.methodNameOf(method) + " = "
                        + CNames.functionNameOf(method) + ";\n");
                _updateRequiredTypes(method.getDeclaringClass().getType());
            }
        }
        return code.toString();
    }
}
