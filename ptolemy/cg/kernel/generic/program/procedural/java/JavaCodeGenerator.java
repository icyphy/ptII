/* Code generator for the Java language.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

 */
package ptolemy.cg.kernel.generic.program.procedural.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.cg.lib.PointerToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JavaCodeGenerator

/** Base class for Java code generator.
 *
 *  @author Gang Zhou, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (zgang)
 *  @Pt.AcceptedRating red (zgang)
 */
public class JavaCodeGenerator extends ProceduralCodeGenerator {

    /** Create a new instance of the Java code generator.
     *  @param container The container.
     *  @param name The name of the Java code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public JavaCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, "java", "j");

        // compileCommand is only used if useMake is false.
        if (compileCommand.getExpression().equals(_compileCommandDefault)) {
            compileCommand
            .setExpression("javac -classpath \"@PTCGLibraries@\" -J-Xmx1500M @modelName@.java");
        }

        // runCommand is only used if useMake is false.
        if (runCommand.getExpression().equals(_runCommandDefault)) {
            runCommand
            .setExpression("java -classpath \"@PTCGLibraries@\" -Xmx1500M @MODELCLASS@");
        }

        generatorPackageList.setExpression("generic.program.procedural.java");
        // A list of the primitive types supported by the code generator.
        // FIXME: we should not have to set these each time, but
        // JavaCodeGenerator uses Integer, and CCodeGenerator uses Int
        _primitiveTypes = Arrays.asList(new String[] { "Integer", "Double",
                "String", "Long", "Boolean", "UnsignedByte",
                /*"Complex",*/"Pointer", "Object" });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param type The given Ptolemy type.
     * @return The code generation type.
     */
    @Override
    public String codeGenType(Type type) {
        //String ptolemyType = super.codeGenType(type);
        String result = type == BaseType.INT ? "Int"
                : type == BaseType.LONG ? "Long"
                        : type == BaseType.STRING ? "String"
                                : type == BaseType.DOUBLE ? "Double"
                                        : type == BaseType.BOOLEAN ? "Boolean"
                                                : type == BaseType.UNSIGNED_BYTE ? "UnsignedByte"
                                                        : type == PointerToken.POINTER ? "Pointer"
                                                                : type == BaseType.COMPLEX ? "Complex"
                                                                        // FIXME: Why do we have to use equals with BaseType.OBJECT?
                                                                        : type.equals(BaseType.OBJECT) ? "Object"
                                                                                //: type == BaseType.OBJECT ? "Object"
                                                                                : null;

        if (result == null) {
            if (type instanceof ArrayType) {
                result = "Array";

            } else if (type instanceof MatrixType) {
                result = "Matrix";
            }
        }
        if (result == null || result.length() == 0) {
            //             if (type instanceof ptolemy.data.type.ObjectType) {
            //                 System.out.println("ObjectType: " + type + " " + BaseType.OBJECT);
            //                 ptolemy.data.type.ObjectType objectType = (ptolemy.data.type.ObjectType)type;
            //                 Class clazz = objectType.getTokenClass();
            //                 System.out.println("ObjectType class: " + (clazz == null ? "null!" : clazz.getName()));
            //                 Class clazz2 = BaseType.OBJECT.getTokenClass();
            //                 System.out.println("BaseType.ObjectType class: " + (clazz2 == null ? "null!" : clazz2.getName()));
            //                 boolean flag = (clazz == clazz2);
            //                 System.out.println("clazz == clazz2: " + flag);
            //                 flag = (type == BaseType.OBJECT);
            //                 System.out.println("type == BaseType.OBJECT: " + flag);
            //                 flag = (type.equals(BaseType.OBJECT));
            //                 System.out.println("type.equals(BaseType.OBJECT): " + flag);
            //                 System.out.println("type.hashCode(): " + type.hashCode() + " BaseType.OBJECT.hashCode(): " + BaseType.OBJECT.hashCode());
            //                 System.out.println("System.identityHashCode(type): " + System.identityHashCode(type)
            //                         + " System.identityHashCode(BaseType.OBJECT) " + System.identityHashCode(BaseType.OBJECT));
            //             }

            // It is not an error to resolve to general.  See
            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/Display.xml
            System.out
            .println("JavaCodeGenerator.codeGenType(): Cannot resolve codegen type from Ptolemy type: "
                    + type
                    + ".  Maybe the type of a port needs to be set from the UI or backward type inference disabled?");
        }
        if (result == null) {
            return null;
        }
        return result.replace("Int", "Integer")
                .replace("Integerger", "Integer");
        //return ptolemyType.replace("Int", "Integer").replace("Integerger", "Integer").replace("Array", "Token");
    }

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param type The given Ptolemy type.
     * @return The code generation type in primitive Java types.
     */
    public static String codeGenType2(Type type) {
        // FIXME: why is this necessary?  It seems to return C types (int vs Int, long vs. Long)
        // It is used in ./ptolemy/cg/kernel/generic/program/procedural/java/modular/ModularCodeGenerator.java
        // It is used in ./ptolemy/cg/kernel/generic/program/procedural/java/modular/ModularSDFCodeGenerator.java
        String result = type == BaseType.INT ? "int"
                : type == BaseType.LONG ? "long"
                        : type == BaseType.STRING ? "String"
                                : type == BaseType.DOUBLE ? "double"
                                        : type == BaseType.BOOLEAN ? "boolean"
                                                : type == BaseType.UNSIGNED_BYTE ? "unsigned byte"
                                                        : type == PointerToken.POINTER ? "Pointer"
                                                                : type == BaseType.COMPLEX ? "Complex"
                                                                        // FIXME: Why do we have to use equals with Object
                                                                        : type.equals(BaseType.OBJECT) ? "Object"
                                                                                //: type == BaseType.OBJECT ? "Object"
                                                                                : null;

        if (result == null) {
            if (type instanceof ArrayType) {
                result = "Array";

            } else if (type instanceof MatrixType) {
                result = "Matrix";
            }
        }
        if (result == null || result.length() == 0) {
            System.out
            .println("JavaCodeGenerator.codeGenType2: Cannot resolve codegen type from Ptolemy type: "
                    + type);
        }
        if (result == null) {
            return null;
        }
        return result;
    }

    /**
     * Return the index of the type in the typesArray in the generated code.
     * @param type The given codegen type.
     * @return The index of the type in the typesArray
     * @exception IllegalActionException If the type is unsupported.
     */
    static public Short codeGenTypeValue(String type)
            throws IllegalActionException {
        // FIXME: the typesArray should only include types used
        // by the model.
        // FIXME: why does this return a Short, but codeGenTypeToPtType()
        // takes an int?
        Short typeReturn;
        if (type.equals("Token")) {
            typeReturn = -1;
        } else if (type.equals("String")) {
            typeReturn = 0;
        } else if (type.equals("Array")) {
            typeReturn = 1;
        } else if (type.equals("Integer")) {
            typeReturn = 2;
        } else if (type.equals("Long")) {
            typeReturn = 3;
        } else if (type.equals("Double")) {
            typeReturn = 4;
        } else if (type.equals("Boolean")) {
            typeReturn = 5;
        } else if (type.equals("UnsignedByte")) {
            typeReturn = 6;
        } else if (type.equals("Pointer")) {
            typeReturn = 7;
        } else if (type.equals("Matrix")) {
            typeReturn = 8;
        } else if (type.equals("Complex")) {
            typeReturn = 9;
        } else if (type.equals("Object")) {
            typeReturn = 10;
        } else {
            throw new IllegalActionException("Unsupported type: " + type);
        }

        return typeReturn;
    }

    /**
     * Return the type that corresponds with an index in the typesArray in
     * in the generated type.
     * @param codeGenType The index of the codegen type.
     * @return The Ptolemy type that corresponds with the index.
     * @exception IllegalActionException If the type is unsupported.
     * @see #ptTypeToCodegenType(Type)
     */
    static public Type codeGenTypeToPtType(int codeGenType)
            throws IllegalActionException {
        Type returnType;

        // FIXME: Add more types.
        // FIXME: the typesArray should only include types used
        // by the model.
        switch (codeGenType) {
        case 0:
            returnType = BaseType.STRING;
            break;
        case 2:
            returnType = BaseType.INT;
            break;
        case 3:
            returnType = BaseType.LONG;
            break;
        case 4:
            returnType = BaseType.DOUBLE;
            break;
        case 5:
            returnType = BaseType.BOOLEAN;
            break;
        case 6:
            returnType = BaseType.UNSIGNED_BYTE;
            break;
        case 7:
            returnType = PointerToken.POINTER;
            break;
            // FIXME: case 8 is Matrix
        case 9:
            returnType = BaseType.COMPLEX;
            break;
        case 10:
            returnType = BaseType.OBJECT;
            break;
        default:
            throw new IllegalActionException("Unsuported type");
        }
        return returnType;
    }

    /**
     * Return the index of the type in the typesArray in the generated code.
     * @param type The given Ptolemy type.
     * @return The index of the type in the typesArray
     * @exception IllegalActionException If the type is unsupported.
     * @see #codeGenTypeToPtType(int)
     */
    static public int ptTypeToCodegenType(Type type)
            throws IllegalActionException {
        // FIXME: the typesArray should only include types used
        // by the model.
        int result = type == BaseType.INT ? 2
                : type == BaseType.LONG ? 3
                        : type == BaseType.STRING ? 0
                                : type == BaseType.DOUBLE ? 4
                                        : type == BaseType.BOOLEAN ? 5
                                                : type == BaseType.UNSIGNED_BYTE ? 6
                                                        : type == PointerToken.POINTER ? 7
                                                                : type == BaseType.COMPLEX ? 9
                                                                        // FIXME: Why do we have to use equals with BaseType.OBJECT?
                                                                        : type.equals(BaseType.OBJECT) ? 10
                                                                                //: type == BaseType.OBJECT ? 10
                                                                                : -10;

        if (result == -10) {
            if (type instanceof ArrayType) {
                result = 0;

            } else if (type instanceof MatrixType) {
                result = 8;
            }
        }

        if (result == -10) {
            throw new IllegalActionException("Unsuported type: " + type);
        }

        return result;
    }

    /** Generate code that defines a constant.
     *  In Java, generate a static final.
     *  @param constant The name of the constant to be defined
     *  @param type A string representing the type.  In C, this
     *  parameter is ignored.
     *  @param value The value of the constant.
     *  @return A static final that defines the constant.
     */
    @Override
    public String generateConstantDefinition(String constant, String type,
            String value) {
        // Maybe we should keep track of these in a Set?
        return "static final " + type + " " + constant + " = " + value + ";"
        + _eol;
    }

    /** Generate the closing code for a group of fire functions common
     *  to a Composite Actor.  This method is called when the firing
     *  code of each actor is not inlined.
     *
     *  @return a curly bracket and _eol.
     */
    @Override
    public String generateFireFunctionCompositeEnd() {
        return "}" + _eol;
    }

    /** Generate the initial code for a group of fire functions common
     *  to a Composite Actor.  This method is called when the firing
     *  code of each actor is not inlined.
     *
     *  @param className The name of the class to include in the
     *  initial code.
     *  @return A string that defines an inner class.
     */
    @Override
    public String generateFireFunctionCompositeStart(String className) {
        return "class " + className + "{" + _eol;
    }

    /** Generate the fire function method invocation. This method is called
     *  when the firing code of each actor is not inlined.
     *
     *  <p>So as to reduce the size of classes to be compiled, this
     *  code generator generates the fire function methods for the top
     *  two levels of composites in separate inner classes.  This
     *  method returns a String that contains variable that refers to
     *  an instance of the inner class followed by the name of the
     *  method to be invoked.</p>
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return The name of the fire function method to be invoked.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireFunctionMethodInvocation(NamedObj namedObj)
            throws IllegalActionException {
        String[] results = generateFireFunctionVariableAndMethodName(namedObj);
        String result = "_inner" + results[0] + "." + results[1];
        //System.out.println("JCG.generateFireFunctionMethodInvocation(): " + namedObj.getFullName() + " " + result);
        return result;
    }

    /** Generate the fire function method name. This method is called
     *  when the firing code of each actor is not inlined.
     *
     *  <p>So as to reduce the size of classes to be compiled, this
     *  code generator generates the fire function methods for the top
     *  two levels of composites in separate inner classes.  This
     *  method returns a String that contains the name of the method
     *  to be invoked.</p>
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return The name of the fire function method.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireFunctionMethodName(NamedObj namedObj)
            throws IllegalActionException {
        String[] results = generateFireFunctionVariableAndMethodName(namedObj);
        //System.out.println("JCG.generateFireFunctionMethodName(): " + namedObj.getFullName() + " " + results[1]);
        return results[1];
    }

    /** Generate the fire function variable name and method
     *  name. This method is called when the firing code of each actor
     *  is not inlined.
     *
     *  <p>So as to reduce the size of classes to be compiled, this
     *  code generator generates the fire function methods for the top
     *  two levels of composites in separate inner classes.</p>
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return An array of two elements.  The first element is a String
     *  that contains the variable name, the second is the name of the method.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String[] generateFireFunctionVariableAndMethodName(NamedObj namedObj)
            throws IllegalActionException {
        // Get the toplevel name and the name of the composite under the
        // top level.
        // If we have Foo.Ramp, return _inner_Foo.Ramp
        // If we have Foo.Bar.Ramp, return _inner_Foo_Bar.Ramp
        // If we have Foo.Bar.Biz.Ramp, return _inner_Foo_Bar.Biz_Ramp
        NamedObj container = namedObj.getContainer();
        String[] results = new String[2];
        if (container == null) {
            results[0] = "";
            results[1] = CodeGeneratorAdapter.generateName(namedObj);
            results[1] = TemplateParser.escapeName(results[1]);
            return results;
        }
        String fullName = namedObj.getFullName();

        int firstDot = fullName.indexOf('.');
        if (firstDot == -1) {
            throw new InternalErrorException(namedObj, null,
                    "Could not find '.' in " + fullName);
        }
        if (firstDot == 0) {
            firstDot = fullName.indexOf('.', firstDot + 1);
        }
        int secondDot = fullName.indexOf('.', firstDot + 1);
        if (firstDot == -1) {
            results[0] = "";
            results[1] = _javaKeywordSanitize(CodeGeneratorAdapter
                    .generateName(namedObj));
            results[1] = TemplateParser.escapeName(results[1]);
            return results;
        }

        if (secondDot == -1) {

            results[0] = _javaKeywordSanitize(StringUtilities
                    .sanitizeName(fullName.substring(0, firstDot)));
            results[1] = _javaKeywordSanitize(StringUtilities
                    .sanitizeName(fullName.substring(firstDot + 1,
                            fullName.length())));
            if (namedObj instanceof ptolemy.actor.TypedCompositeActor) {
                // A Hack for inline code generation.  The problem is
                // that when we generate inline code, we want the top
                // few composites to be generated inside inner classes
                // so that we can reduce the code size for
                // compilation.  Unfortunately, when SDFCodeGenerator
                // finds a TypedComposite, it generates code for the
                // TypedComposite and the inside of the TypedComposite
                // at the same time.  Thus, the code needs to be
                // inside the same inner class.  Thus, the name of the
                // TypedComposite has the name of the NamedObj twice.
                // This is a hack.
                results[0] += "_" + results[1];
            }
        } else {
            results[0] = StringUtilities.sanitizeName(fullName.substring(0,
                    firstDot)
                    + "_"
                    + fullName.substring(firstDot + 1, secondDot));
            results[1] = _javaKeywordSanitize(StringUtilities
                    .sanitizeName(fullName.substring(secondDot + 1,
                            fullName.length())));
        }

        //System.out.println("JCG: genVarAndMethName: " + firstDot + " " + secondDot + " " + fullName + " variableName: " + results[0] + " methodName: " + results[1]);

        // If an actor name has a $ in it . . .
        // $PTII/bin/ptcg -language java -inline false ~/ptII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ActorWithDollarSignInName.xml
        results[0] = TemplateParser.escapeName(results[0]);
        results[1] = TemplateParser.escapeName(results[1]);

        return results;
    }

    /** Generate the fire function variable declaration. This method
     *  is called when the firing code of each actor is not inlined.
     *
     *  <p>So as to reduce the size of classes the Java Code
     *  Generator, the fire function methods for the top two levels of
     *  composites are placed in a separate inner class.</p>
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return If the namedObj is in a containment hierarchy that
     *  also contains a GenericCodeGenerator, then the declaration is
     *  returned.  Otherwise, the empty string is returned.
     *  @exception IllegalActionException If there are problems
     *  accessing the name of the namedObj or generating the variable
     *  declaration.
     */
    @Override
    public String generateFireFunctionVariableDeclaration(NamedObj namedObj)
            throws IllegalActionException {
        // Go up the containment chain, looking for a GenericCodeGenerator.
        // If there is one, return the declaration.  If there is not one,
        // return the empty string.  This is needed for Composite Codegen.
        NamedObj container = namedObj.getContainer();
        while (container != null) {
            List<GenericCodeGenerator> codeGenerators = container
                    .attributeList(GenericCodeGenerator.class);
            if (codeGenerators.size() > 0) {
                String[] results = generateFireFunctionVariableAndMethodName(namedObj);
                return results[0] + " _inner" + results[0] + " = new "
                + results[0] + "();" + _eol;
            }
            container = container.getContainer();
        }
        return "";
    }

    /** Generate the function table.
     *
     *  @param types An array of types.
     *  @param functions An array of functions.
     *  @return The code that declares functions.
     */
    public Object generateFunctionTable(String[] types, String[] functions) {
        // FIXME: consider making this private?
        StringBuffer code = new StringBuffer();

        if (functions.length > 0 && types.length > 0) {

            code.append("private static final int NUM_TYPE = " + types.length
                    + ";" + _eol);
            code.append("private static final int NUM_FUNC = "
                    + functions.length + ";" + _eol);
            code.append("//Token (*functionTable[NUM_TYPE][NUM_FUNC])"
                    + "(Token, ...) = {" + _eol);

            for (int i = 0; i < types.length; i++) {
                code.append("//\t{");
                for (int j = 0; j < functions.length; j++) {
                    if (functions[j].equals("isCloseTo")
                            && (types[i].equals("Boolean")
                                    || types[i].equals("String") || types[i]
                                            .equals("Object"))) {
                        // Boolean_isCloseTo and String_isCloseTo
                        // are the same as their corresponding *_equals
                        code.append(types[i] + "_equals");
                    } else {
                        // Check to see if the type/function combo is supported.
                        String typeFunctionName = types[i] + "_" + functions[j];
                        if (_unsupportedTypeFunctions
                                .contains(typeFunctionName)) {
                            code.append("unsupportedTypeFunction");
                        } else {
                            if (_scalarDeleteTypes.contains(types[i])
                                    && functions[j].equals("delete")) {
                                code.append("scalarDelete");
                            } else {
                                code.append(typeFunctionName);
                            }
                        }
                    }
                    if (j != functions.length - 1) {
                        code.append(", ");
                    }
                }
                if (i != types.length - 1) {
                    code.append("},");
                } else {
                    code.append("}");
                }
                code.append(_eol);
            }

            code.append("//};" + _eol);
        }
        return code.toString();
    }

    /** Return the closing entry code, if any.
     *  @return the closing entry code.
     */
    @Override
    public String generateClosingEntryCode() {
        return "public void doWrapup() throws Exception { " + _eol;
    }

    /** Return the closing exit code, if any.
     *  @return the closing exit code.
     */
    @Override
    public String generateClosingExitCode() {
        return "}" + _eol;
    }

    /** Generate the initialization procedure entry point.
     *  @return a string for the initialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class..
     */
    @Override
    public String generateInitializeEntryCode() throws IllegalActionException {
        return _eol + _eol + "public void initialize() throws Exception {"
                + _eol;
    }

    /** Generate the initialization procedure exit point.
     *  @return a string for the initialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class..
     */
    @Override
    public String generateInitializeExitCode() throws IllegalActionException {
        return "}" + _eol;
    }

    /** Generate the initialization procedure name.
     *  @return a string for the initialization procedure name.
     *  @exception IllegalActionException Not thrown in this baseclass.
     */
    @Override
    public String generateInitializeProcedureName()
            throws IllegalActionException {
        //return _INDENT1 + "initialize();" + _eol;
        return "// Don't call initialize() here, it is called in main.";
    }

    /** Generate line number information.  In this class, lines
     *  of the format
     *  <pre>
     *  #line <i>lineNumber</i> "<i>filename</i>"
     *  </pre>
     *  are generated for use by the C preprocessor.
     *  @param lineNumber The line number of the source file or
     *  file containing code blocks.
     *  @param filename The name of the source file or file containing
     *  code blocks.
     *  @return text that is suitable for the C preprocessor.
     */
    @Override
    public String generateLineInfo(int lineNumber, String filename) {
        return "#line " + lineNumber + " \"" + filename + "\"\n";
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a program.
     *   In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateMainEntryCode() throws IllegalActionException {

        StringBuffer mainEntryCode = new StringBuffer();

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            //          mainEntryCode
            //          .append(_eol
            //                  + _eol
            //                  + "public static void main(String [] args) throws Exception {"
            //                  + _eol + _sanitizedModelName + " model = new "
            //                  + _sanitizedModelName + "();" + _eol
            //                  + "model.run();" + _eol + "}" + _eol
            //                  + "public void run() throws Exception {" + _eol);

            String recordStartTimeCode = "";
            String printExecutionTimeCode = "";
            if (((BooleanToken) measureTime.getToken()).booleanValue()) {
                recordStartTimeCode = _recordStartTime();
                printExecutionTimeCode = _printExecutionTime();
            }
            mainEntryCode
            .append(_eol
                    + _eol
                    + "public static void main(String [] args) throws Exception {"
                    + _eol + _sanitizedModelName + " model = new "
                    + _sanitizedModelName + "();" + _eol
                    + recordStartTimeCode + _eol
                    + "model.preinitialize();" + _eol
                    + "model.initialize();" + _eol + "model.execute();"
                    + _eol + "model.doWrapup();" + _eol
                    + printExecutionTimeCode + _eol + "System.exit(0);"
                    + _eol + "}" + _eol);

        } else {
            mainEntryCode.append(_eol + _eol + "public Object[] " + _eol
                    + "fire (" + _eol);

            Iterator<?> inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            boolean addComma = false;
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                if (addComma) {
                    mainEntryCode.append(", ");
                }
                mainEntryCode.append("Object[]" + inputPort.getName());
                addComma = true;
            }

            mainEntryCode.append(") throws Exception {" + _eol);

        }

        return mainEntryCode.toString();
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateMainExitCode() throws IllegalActionException {

        if (_isTopLevel()) {
            return "}";
        } else {
            if (_model instanceof CompositeActor
                    && ((CompositeActor) _model).outputPortList().isEmpty()) {
                return INDENT1 + "return null;" + _eol + "}" + _eol + "}"
                        + _eol;
            } else {
                return INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                        + _eol + "}" + _eol;
            }
        }
    }

    /** Generate the package statement, if any.
     *  @return If the <i>generateInSubdirectory</i> parameter of
     *  the grandparent is true, then generate a package statement with
     *  the name of the package being the sanitized model name
     *  @exception IllegalActionException Thrown if <i>generateInSubdirectory</i>
     *  cannot be read.
     */
    @Override
    public String generatePackageStatement() throws IllegalActionException {
        if (_generateInSubdirectory) {
            return "package " + _sanitizedModelName + ";" + _eol;
        }
        return "";
    }

    /** Generate the postfire procedure entry point.
     *  @return a string for the postfire procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generatePostfireEntryCode() throws IllegalActionException {
        return _eol + _eol + "public boolean postfire() {" + _eol;

    }

    /** Generate the postfire procedure exit point.
     *  @return a string for the postfire procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generatePostfireExitCode() throws IllegalActionException {
        return INDENT1 + "return true;" + _eol + "}" + _eol;
    }

    /** Generate the postfire procedure name.
     *  @return a string for the postfire procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generatePostfireProcedureName() throws IllegalActionException {

        return INDENT1 + "postfire();" + _eol;
    }

    /**
     * Generate type conversion code.
     * Determine the proper code put into the source to support dynamic type
     * resolution. First, find out the different types used in the model.
     * Second, find out the different polymorphic functions used. (note: types
     * and functions are independent of each other). Third, append code blocks
     * according to the functions used, and read from files according to the
     * types referenced. Fourth, generate type resolution code, which consists
     * of constants (MAX_NUM_TYPE, MAX_NUM_FUNC), the type map, the function
     * map, function definitions read from the files, and function table.
     * @return The type resolution code.
     * @exception IllegalActionException If an error occurs when generating
     *  the type resolution code, or if the adapter class for the model
     *  director cannot be found, or if an error occurs when the adapter
     *  actor generates the type resolution code.
     */
    @Override
    public String generateTypeConvertCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();

        code.append(_eol
                + comment("Generate type resolution code for "
                        + getContainer().getFullName()));

        // Include the constantsBlock at the top so that sharedBlocks from
        // actors can use true and false etc.  StringMatches needs this.

        CodeStream sharedStream = new CodeStream(
                "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/java/SharedCode.j",
                this);
        sharedStream.appendCodeBlock("constantsBlock");
        code.append(sharedStream.toString());

        HashSet<String> functions = _getReferencedFunctions();
        HashSet<String> types = _getReferencedTypes(functions);

        String[] typesArray = new String[types.size()];
        types.toArray(typesArray);

        CodeStream[] typeStreams = new CodeStream[types.size()];

        // Generate type map.
        StringBuffer typeMembers = new StringBuffer();
        code.append("private static final short TYPE_Token = -1;" + _eol);
        for (int i = 0; i < typesArray.length; i++) {
            // Open the .j file for each type.
            typeStreams[i] = new CodeStream(
                    "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/java/type/"
                            + typesArray[i] + ".j", this);

            //FIXME: temporarily statically assign types, is there any better way to do that?
            //            code.append("#define PTCG_TYPE_" + typesArray[i] + " " + i + _eol);
            code.append("#define PTCG_TYPE_" + typesArray[i] + " "
                    + codeGenTypeValue(typesArray[i]) + _eol);

            //            code.append("private static final short TYPE_" + typesArray[i] + " = " + i
            code.append("private static final short TYPE_" + typesArray[i]
                    + " = " + codeGenTypeValue(typesArray[i]) + ";" + _eol);

            // Dynamically generate all the types within the union.
            if (i > 0) {
                typeMembers.append(INDENT2);
            }
            typeMembers.append(typesArray[i] + "Token " + typesArray[i] + ";");
            if (i < typesArray.length - 1) {
                typeMembers.append(_eol);
            }
        }

        String[] functionsArray = new String[functions.size()];
        functions.toArray(functionsArray);

        // True if we have a delete function that needs to return a Token
        boolean defineEmptyToken = false;

        for (int i = 0; i < functionsArray.length; i++) {
            code.append("#define FUNC_" + functionsArray[i] + " " + i + _eol);
            if (functionsArray[i].equals("delete")) {
                defineEmptyToken = true;
            }
        }

        // Generate type and function definitions.
        for (int i = 0; i < typesArray.length; i++) {
            // The "declareBlock" contains all necessary declarations for the
            // type; thus, it is always read into the code stream when
            // accessing this particular type.

            StringBuffer declareBlock = new StringBuffer();
            declareBlock.append(typeStreams[i].getCodeBlock("declareBlock"));
            if (declareBlock.length() > 0) {
                if (_generateInSubdirectory) {
                    declareBlock.insert(0, generatePackageStatement());
                }
                String typeName = _typeNameCG(typesArray[i]);
                _writeCodeFileName(declareBlock, typeName + ".java", false,
                        true);
            }
        }

        // Token declareBlock.
        if (typeMembers.length() != 0) {

            sharedStream.clear();
            StringBuffer declareTokenBlock = new StringBuffer();
            if (_generateInSubdirectory) {
                declareTokenBlock.append("package " + _sanitizedModelName + ";"
                        + _eol);
            }
            declareTokenBlock.append(sharedStream
                    .getCodeBlock("tokenDeclareBlock"));

            if (defineEmptyToken) {
                sharedStream.append("public static Token emptyToken; "
                        + comment("Used by *_delete() and others.") + _eol);
            }

            _writeCodeFileName(declareTokenBlock, "Token.java", false, true);
        }

        // Set to true if we need the unsupportedFunction() method.
        boolean defineUnsupportedTypeFunctionMethod = false;

        // Set to true if we need to scalarDelete() method.
        boolean defineScalarDeleteMethod = false;

        // Append type-polymorphic functions included in the function table.
        for (int i = 0; i < types.size(); i++) {
            // The "funcDeclareBlock" contains all function declarations for
            // the type.
            for (int j = 0; j < functionsArray.length; j++) {
                String typeFunctionName = typesArray[i] + "_"
                        + functionsArray[j];
                if (_unsupportedTypeFunctions.contains(typeFunctionName)) {
                    defineUnsupportedTypeFunctionMethod = true;
                }
                if (_scalarDeleteTypes.contains(typesArray[i])
                        && functionsArray[j].equals("delete")) {
                    defineScalarDeleteMethod = true;
                }
                if (functionsArray[j].equals("isCloseTo")
                        && (typesArray[i].equals("Boolean") || typesArray[i]
                                .equals("String"))) {
                    boolean foundEquals = false;
                    for (String element : functionsArray) {
                        if (element.equals("equals")) {
                            foundEquals = true;
                        }
                    }
                    if (!foundEquals) {
                        // Boolean_isCloseTo and String_isCloseTo
                        // use Boolean_equals and String_equals.
                        ArrayList<String> args = new ArrayList<String>();
                        args.add(typesArray[i] + "_equals");
                        sharedStream.appendCodeBlock("funcHeaderBlock", args);
                    }
                }
                if (!_scalarDeleteTypes.contains(typesArray[i])
                        || !functionsArray[j].equals("delete")) {
                    // Skip Boolean_delete etc.
                    ArrayList<String> args = new ArrayList<String>();
                    args.add(typeFunctionName);
                    sharedStream.appendCodeBlock("funcHeaderBlock", args);
                }
            }
        }

        if (defineUnsupportedTypeFunctionMethod) {
            // Some type/function combos are not supported, so we
            // save space by defining only one method.
            sharedStream.appendCodeBlock("unsupportedTypeFunction");
        }

        if (defineScalarDeleteMethod) {
            // Types that share the scalarDelete() method, which does nothing.
            // We use one method so as to reduce code size.
            sharedStream.appendCodeBlock("scalarDeleteFunction");
        }

        code.append(sharedStream.toString());

        // Append functions that are specified used by this type (without
        // going through the function table).
        for (int i = 0; i < typesArray.length; i++) {
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("funcDeclareBlock");
            code.append(typeStreams[i].toString());
        }

        // FIXME: in the future we need to load the convertPrimitivesBlock
        // dynamically, and maybe break it into multiple blocks to minimize
        // code size.
        sharedStream.clear();
        sharedStream.appendCodeBlock("convertPrimitivesBlock");
        code.append(sharedStream.toString());

        // Generate function type and token table.
        code.append(generateFunctionTable(typesArray, functionsArray));

        for (int i = 0; i < typesArray.length; i++) {
            typeStreams[i].clear();
            //typeStreams[i].appendCodeBlock(typesArray[i] + "_new");

            for (String element : functionsArray) {

                // The code block declaration has to follow this convention:
                // /*** [function name]Block ***/
                //     .....
                // /**/
                try {
                    // Boolean_isCloseTo and String_isCloseTo map to
                    // Boolean_equals and String_equals.
                    if (element.equals("isCloseTo")
                            && (typesArray[i].equals("Boolean")
                                    || typesArray[i].equals("String") || typesArray[i]
                                            .equals("Object"))) {

                        if (!functions.contains("equals")) {
                            //typeStreams[i].appendCodeBlock(typesArray[i]
                            //        + "_equals");
                            markFunctionCalled(typesArray[i] + "_equals", null);
                        }
                    } else {
                        String functionName = typesArray[i] + "_" + element;

                        if (!_unsupportedTypeFunctions.contains(functionName)
                                && !_overloadedFunctionSet
                                .contains(functionName)) {

                            //typeStreams[i].appendCodeBlock(typesArray[i] + "_"
                            //+ functionsArray[j]);
                            markFunctionCalled(functionName, null);
                        }
                    }
                } catch (IllegalActionException ex) {
                    // We have to catch the exception if some code blocks are
                    // not found. We have to define the function label in the
                    // generated code because the function table makes
                    // reference to this label.

                    System.out.println("Warning, failed to find java "
                            + typesArray[i] + "_" + element);
                    //                     typeStreams[i].append("#define " + typesArray[i] + "_"
                    //                             + functionsArray[j] + " MISSING " + _eol);

                    // It is ok because this polymorphic function may not be
                    // supported by all types.
                }
            }
            code.append(_processCode(typeStreams[i].toString()));
        }

        code.append(_overloadedFunctions.toString());

        if (_generateInSubdirectory) {
            code.insert(0, "/*" + generatePackageStatement()
                    + (_typeDeclarations != null ? _typeDeclarations : "")
                    + _eol + "public class TypeResolution {" + _eol + "*/");
            code.append("// }" + _eol);
        }

        if (!((BooleanToken) inline.getToken()).booleanValue()) {
            // Variable declarations that refer to instances of inner classes.
            // Used only if inline == false.
            Set<String> variableDeclarations = new TreeSet<String>();

            // This seems expensive.
            // Could be Actors or States.  For States, see:
            // $PTII/bin/ptcg -language java  -inline false $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/modal/test/auto/FSMActor.xml
            Iterator<?> namedObjs = ((CompositeActor) getComponent().toplevel())
                    .allAtomicEntityList().iterator();
            while (namedObjs.hasNext()) {
                NamedObj namedObj = (NamedObj) namedObjs.next();
                if (namedObj instanceof Actor) {
                    variableDeclarations
                    .add(generateFireFunctionVariableDeclaration(namedObj));
                }
            }

            // Collect all the variable declarations into a StringBuffer.
            StringBuffer variables = new StringBuffer();
            for (String variableDeclaration : variableDeclarations) {
                variables.append(variableDeclaration);
            }
            if (variables.length() > 0) {
                variables
                .insert(0,
                        comment("inline: true, Variables that refer to inner classes."));
            }
            code.append(variables);

        }

        if (_variablesAsArrays) {
            // If variablesAsArrays is true, then use arrays of variables instead
            // of individual variables and save space.

            // Generate the declarations for the arrays that contain variables.
            // See also generateInitializeCode() in
            // $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/kernel/SDFDirector.java

            code.append(comment(1, "Arrays that contain variables."));
            if (_variableTypeMaxIndex != null) {
                for (Map.Entry<String, Integer> entry : _variableTypeMaxIndex
                        .entrySet()) {
                    String typeName = entry.getKey();
                    code.append(typeName + " variables_"
                            + StringUtilities.sanitizeName(typeName)
                            + "[] = new " + typeName + "[" + entry.getValue()
                            + "];" + _eol);
                }
            }

            if (_portTypeMaxIndex != null) {
                // See ProgramCodeGenerator.generatePortName() for where we set up the
                // maps.
                code.append(comment(1, "Arrays that contain ports."));
                for (Map.Entry<String, Integer> entry : _portTypeMaxIndex
                        .entrySet()) {
                    String typeName = entry.getKey();
                    code.append(typeName + " ports_"
                            + StringUtilities.sanitizeName(typeName)
                            + "[] = new " + typeName + "[" + entry.getValue()
                            + "];" + _eol);
                }
            }

            if (_portTypeMaxIndex2 != null) {
                code.append(comment(1,
                        "Arrays that contain multiports w/ buffer == 1 or ports with buffers > 1."));
                for (Map.Entry<String, Integer> entry : _portTypeMaxIndex2
                        .entrySet()) {
                    String typeName = entry.getKey();
                    code.append(typeName + " ports2_"
                            + StringUtilities.sanitizeName(typeName)
                            + "[][] = new " + typeName + "[" + entry.getValue()
                            + "][];" + _eol);
                }
            }

            if (_portTypeMaxIndex3 != null) {
                code.append(comment(1,
                        "Arrays that contain multiports with buffers > 1."));
                for (Map.Entry<String, Integer> entry : _portTypeMaxIndex3
                        .entrySet()) {
                    String typeName = entry.getKey();
                    code.append(typeName + " ports3_"
                            + StringUtilities.sanitizeName(typeName)
                            + "[][][] = new " + typeName + "["
                            + entry.getValue() + "][][];" + _eol);
                }
            }

        }
        return code.toString();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateVariableDeclaration());

        // Generate variable declarations for modified variables.
        if (_modifiedVariables != null && !_modifiedVariables.isEmpty()) {
            code.append(comment("Generate variable declarations for "
                    + "modified parameters"));
            Iterator<?> modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                // SetVariable needs this to be a Variable, not a Parameter.
                Variable variable = (Variable) modifiedVariables.next();

                NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(variable
                        .getContainer());
                if (!_variablesAsArrays) {
                    code.append("public static "
                            + adapter.targetType(variable.getType()) + " "
                            + generateVariableName(variable) + ";" + _eol);
                }
            }
        }

        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateVariableInitialization());

        // if (_generateInSubdirectory) {
        //     code.append(comment(1, "Arrays that contain variables."));
        //     for (Map.Entry<String, Integer> entry : _variableTypeMaxIndex.entrySet()) {
        //         String typeName = entry.getKey();
        //         code.append("variables_"
        //                 + StringUtilities.sanitizeName(typeName)
        //                 + " = new " + typeName
        //                 + "[" + (((Integer)(entry.getValue())).intValue()) + "];" + _eol);
        //     }
        // }

        // Generate variable initialization for modified variables.
        if (_modifiedVariables != null && !_modifiedVariables.isEmpty()) {
            code.append(comment(1, "Generate variable initialization for "
                    + "modified parameters"));

            Iterator<?> modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                // SetVariable needs this to be a Variable, not a Parameter.
                Variable variable = (Variable) modifiedVariables.next();

                NamedObj container = variable.getContainer();
                NamedProgramCodeGeneratorAdapter containerAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(container);
                //String parameterValue = "";
                try {
                    /*parameterValue = */containerAdapter.getParameterValue(
                            variable.getName(), variable.getContainer());
                } catch (Throwable throwable) {
                    throw new IllegalActionException(container, throwable,
                            "Failed to get the value of \""
                                    + variable.getName()
                                    + "\", the container Adapter was: "
                                    + containerAdapter + " which is a "
                                    + containerAdapter.getClass().getName());
                }
                code.append(generateVariableName(variable)
                        + " = "
                        + containerAdapter.getParameterValue(
                                variable.getName(), variable.getContainer())
                                + ";" + _eol);
            }
        }
        return code.toString();
    }

    /** Generate variable name for the given attribute. The reason to append
     *  underscore is to avoid conflict with the names of other objects. For
     *  example, the paired PortParameter and ParameterPort have the same name.
     *  @param attribute The attribute to for which to generate a variable name.
     *  @return The generated variable name.
     */
    @Override
    public String generateVariableName(NamedObj attribute) {
        if (!_variablesAsArrays || !(attribute instanceof Variable)) {
            return super.generateVariableName(attribute);
        }

        Variable variable = (Variable) attribute;
        // The idea is that for each type, we have an array
        // that contain the variables for that type.
        // This means that we will have many less variables, which will
        // get around javac's "too many constants" message
        // (See http://marxsoftware.blogspot.com/2010/01/reproducing-too-many-constants-problem.html)

        // However, we don't want to search the arrays while
        // generating code, so we have a separate HashMap that
        // that is used at code generation time to map from
        // names to the index in the corresponding type array.

        if (_variableTypeMap == null) {
            // A map from String type name to a HashMap of variable name to Array Index.
            _variableTypeMap = new HashMap<String, HashMap<String, Integer>>();
            _variableTypeMaxIndex = new HashMap<String, Integer>();
        }

        // Get the type.
        NamedProgramCodeGeneratorAdapter adapter = null;
        try {
            adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(variable
                    .getContainer());
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(variable, ex,
                    "Failed to get the adapter of " + variable);
        }
        String typeName = adapter.targetType(variable.getType());

        // Look up the type in our HashTable of types.
        HashMap<String, Integer> variableMap = null;
        if ((variableMap = _variableTypeMap.get(typeName)) == null) {
            // A type that is not in our map of types.
            variableMap = new HashMap<String, Integer>();
            _variableTypeMap.put(typeName, variableMap);
            _variableTypeMaxIndex.put(typeName, 0);
        }

        // Look up the attribute by name in the HashTable.
        String variableName = super.generateVariableName(attribute);
        Integer variableIndex = null;
        if ((variableIndex = variableMap.get(variableName)) == null) {
            // FIXME: is there a better way to update an element in a HashMap?
            variableIndex = _variableTypeMaxIndex.get(typeName);
            _variableTypeMaxIndex.put(typeName, variableIndex + 1);
            variableMap.put(variableName, variableIndex);
        }

        return "variables_" + StringUtilities.sanitizeName(typeName) + "["
        + variableIndex + "]";

    }

    /** Generate the wrapup procedure entry point.
     *  @return a string for the wrapup procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateWrapupEntryCode() throws IllegalActionException {
        return _eol + _eol + "public void wrapup() throws Exception {" + _eol;
    }

    /** Generate the wrapup procedure exit point.
     *  @return a string for the wrapup procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateWrapupExitCode() throws IllegalActionException {

        return "}" + _eol;
    }

    /** Generate the wrapup procedure name.
     *  @return a string for the wrapup procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateWrapupProcedureName() throws IllegalActionException {

        return "wrapup();" + _eol;
    }

    /**
     * Return the return signature for run() and execute().
     * @return The visibility signature.
     */
    @Override
    public String getMethodVisibilityString() {
        return "public ";
    }

    /**
     * Return the exception signature (for Java).
     * @return The exception signature.
     */
    @Override
    public String getMethodExceptionString() {
        return " throws Exception";
    }

    /** Add called functions to the set of overloaded functions for
     *  later use.
     *  If the function starts with "Array_", add everything after the
     *  "Array_" is added to the set of token functions used.
     *  @param name The name of the function, for example "Double_equals"
     *  @param javaTemplateParser The corresponding parser that contains the
     *  codeBlock.
     *  @exception IllegalActionException If there is a problem adding
     *  a function to the set of overloaded functions.
     */
    public void markFunctionCalled(String name,
            JavaTemplateParser javaTemplateParser)
                    throws IllegalActionException {

        try {
            if (_overloadedFunctions == null) {
                throw new NullPointerException(
                        "Call _analyzeTypeConversions() "
                                + "by calling _generateCode() or generateCode() "
                                + "before calling markFunctionCalled().  Otherwise the "
                                + "CodeStream of overloaded functions will not be initialized");
            }
            String functionCode = _overloadedFunctions.getCodeBlock(name);

            if (!_overloadedFunctionSet.contains(name)) {
                _overloadedFunctionSet.add(name);

                String code = javaTemplateParser == null ? _processCode(functionCode)
                        : javaTemplateParser.processCode(functionCode);

                _overloadedFunctions.append(code);

            }
            if (name.startsWith("Array_")) {
                // Array_xxx might need to have xxx added.
                // See c/actor/lib/test/auto/MultiplyDivide5.xml

                // FIXME: this will add any function, which means that
                // if the user has Array_foo, foo is added.  Is this right?
                _tokenFuncUsed.add(name.substring(6));
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to mark function called for \"" + name + "\" in \""
                            + getComponent().getFullName() + "\"");
        }

    }

    /** Split a long function body into multiple inner classes.
     *  @param linesPerMethod The number of lines that should go into
     *  each method.
     *  @param prefix The prefix to use when naming inner classes that
     *  are created.
     *  @param code The method body to be split.
     *  @return An array of two Strings, where the first element
     *  is the new definitions (if any), and the second element
     *  is the new body.  If the number of lines in the code parameter
     *  is less than linesPerMethod, then the first element will be
     *  the empty string and the second element will be the value of
     *  the code parameter.
     *  @exception IOException If thrown while reading the code.
     */
    @Override
    public String[] splitLongBody(int linesPerMethod, String prefix, String code)
            throws IOException {
        BufferedReader bufferedReader = null;
        StringBuffer bodies = new StringBuffer("class " + prefix + " {" + _eol);
        // One method calls all the other methods, thus reducing the
        // size of the top level caller.
        String callAllBodyMethodName = "callAll" + prefix;
        StringBuffer callAllBody = new StringBuffer("void "
                + callAllBodyMethodName + "() throws Exception {" + _eol
                + prefix + " " + prefix + " = new " + prefix + "();" + _eol);

        StringBuffer masterBody = new StringBuffer(prefix + " " + prefix
                + " = new " + prefix + "();" + _eol);

        try {
            bufferedReader = new BufferedReader(new StringReader(code));
            String line;
            int methodNumber = 0;

            // lineNumber keeps track of the number of lines seen
            // so that we know whether we've reached the linesPerMethod
            // Note that we don't reset lineNumer in the while loop.
            int lineNumber = 0;
            StringBuffer body = new StringBuffer();
            // Read lines until we reach the linesPerMethod.
            // If we reach the linesPerMethod line and we are inside
            // a try/catch, if or {} block, then keep reading and appending
            // until the block ends
            while ((line = bufferedReader.readLine()) != null) {
                //String methodName = prefix + "_" + methodNumber++;
                // Don't create really long names or javac exits with "(File name too long)"
                String methodName = "_" + prefix.substring(0, 2) + "_sL_"
                        + methodNumber++;
                lineNumber++;
                body = new StringBuffer(line + _eol);
                int commentCount = 0;
                int ifCount = 0;
                int openBracketCount = 0;
                int switchCount = 0;
                int tryCount = 0;
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("/*")) {
                    commentCount++;
                }
                if (trimmedLine.endsWith("*/")) {
                    commentCount--;
                }

                // Look for curly braces in non-commented lines
                if (trimmedLine.endsWith("{")) {
                    if (ifCount > 0) {
                        ifCount--;
                    } else if (switchCount > 0) {
                        switchCount--;
                    }
                    openBracketCount++;
                }
                // Lines can both start and end with braces.
                if (trimmedLine.startsWith("}")) {
                    if (ifCount > 0) {
                        ifCount--;
                    } else if (switchCount > 0) {
                        switchCount--;
                    }
                    openBracketCount--;
                    // Don't break up try catch blocks
                    if (trimmedLine.startsWith("} catch")
                            || trimmedLine.startsWith("} catch")) {
                        tryCount--;
                    }
                } else if (trimmedLine.startsWith("try")) {
                    tryCount++;
                } else if (line.trim().startsWith("if")) {
                    ifCount++;
                } else if (line.trim().startsWith("switch")) {
                    switchCount++;
                }

                //System.out.println(ifCount + " " + openBracketCount + " " + commentCount + " " + tryCount + " a: " + line);
                // Keep appending lines until we are do linesPerMethod lines
                // or the if, {}, comment or try/catch block ends.
                for (int i = 0; i + 1 < linesPerMethod && line != null
                        || ifCount > 0 || openBracketCount > 0
                        || commentCount > 0 || tryCount > 0 || switchCount > 0; i++) {
                    lineNumber++;
                    line = bufferedReader.readLine();
                    //System.out.println(ifCount + " " + openBracketCount + " " + commentCount + " " + tryCount + " b:" + line);

                    if (i > 100000000) {
                        throw new InternalErrorException(
                                "Internal Error: looped more than 10000000 lines?"
                                        + " This can happen if curly brackets are not on lines"
                                        + " by themselves or if there /* */ comments that are not"
                                        + " on lines by themselves."
                                        + " ifCount: " + ifCount
                                        + " openBracketCount: "
                                        + openBracketCount + " commentCount: "
                                        + commentCount + " switchCount: "
                                        + switchCount + " tryCount: "
                                        + tryCount + " line:\n" + line
                                        + " code:\n" + code);
                    }
                    if (line != null) {
                        body.append(line + _eol);
                        trimmedLine = line.trim();
                        if (trimmedLine.startsWith("/*")) {
                            commentCount++;
                        }
                        if (trimmedLine.endsWith("*/")) {
                            commentCount--;
                        }

                        if (!trimmedLine.startsWith("//")
                                && !trimmedLine.startsWith("/*")
                                && !trimmedLine.startsWith("*")) {
                            // FIXME: this looks like duplicated code
                            // Look for curly braces in non-commented lines
                            if (trimmedLine.endsWith("{")) {
                                if (ifCount > 0) {
                                    ifCount--;
                                } else if (switchCount > 0) {
                                    switchCount--;
                                }
                                openBracketCount++;
                            }
                            // Lines can both start and end with braces.
                            if (trimmedLine.startsWith("}")) {
                                if (ifCount > 0) {
                                    ifCount--;
                                } else if (switchCount > 0) {
                                    switchCount--;
                                }
                                openBracketCount--;
                                // Don't break up try catch blocks
                                if (trimmedLine.startsWith("} catch")
                                        || trimmedLine.startsWith("} catch")) {
                                    tryCount--;
                                }
                            } else if (trimmedLine.startsWith("try")) {
                                tryCount++;
                            } else if (line.trim().startsWith("if")) {
                                ifCount++;
                            } else if (line.trim().startsWith("switch")) {
                                switchCount++;
                            }
                        }
                    }
                }

                //callAllBody.append(prefix + "." + methodName + "();" + _eol);
                callAllBody.append("new " + methodName + "();" + _eol);
                //                 bodies.append("void " + methodName + "() {" + _eol
                //                         + body.toString()
                //                         + "}" + _eol);

                bodies.append("class " + methodName + " {" + _eol + methodName
                        + "() throws Exception {" + _eol + body.toString()
                        + "}" + _eol + "}" + _eol);
            }
            if (lineNumber <= linesPerMethod) {
                // We must have less than linesPerMethod lines in the body
                bodies = new StringBuffer();
                callAllBody = new StringBuffer();
                masterBody = new StringBuffer(body);
            }
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }

        if (bodies.length() != 0) {
            bodies.append(_eol + callAllBody);
            bodies.append("}" + _eol);
            bodies.append("}" + _eol);
            masterBody.append(prefix + "." + "callAll" + prefix + "();" + _eol);
        }

        String[] results = { bodies.toString(), masterBody.toString() };
        return results;
    }

    /** Split a long variable declaration body into multiple blocks or
     *  files.
     *
     *  <p>For Java, if the <i>code</i> consists of
     *  <i>linesPerMethod</i> or fewer lines, then the the first
     *  element will be the empty string and the second element will
     *  consist of the value of the code argument.  If the <i>code</i>
     *  consists of more than <i>linesPerMethod</i> then the first
     *  element will consist of one or more "import static" statements
     *  and the second and possibly successive element will consist of
     *  Java classes that should be written out by
     *  {@link #_writeVariableDeclarations(List)}.
     *
     *  @param linesPerMethod The number of lines that should go into
     *  each method.
     *  @param prefix The prefix to use when naming functions that
     *  are created
     *  @param code The variable declarations to be split.
     *  @return A list of at least two elements.  If the code has less than
     *  _LINES_PER_METHOD lines, then the first element is empty, the
     *  second element contains the contents of the code parameter.  If
     *  the code has more lines than _LINES_PER_METHOD, then the first
     *  element contains the declarations necessary for the include files
     *  section and the second element and successive elements contain the
     *  declarations.  Each declaration should be placed into a file
     *  that corresponds with the include or import listed in the first
     *  element.
     *  @exception IOException If thrown while reading the code.
     */
    @Override
    public List<String> splitVariableDeclaration(int linesPerMethod,
            String prefix, String code) throws IOException {
        LinkedList<String> results = new LinkedList<String>();
        // The first element of the list is the declarations, if any.
        // We add an empty string so that the second and possibly
        // successive elements will be in the proper location.
        results.add("");
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new StringReader(code));
            String line;
            int methodNumber = 0;
            // lineNumber keeps track of the number of lines seen
            // so that we know whether we've reached the linesPerMethod
            // Note that we don't reset lineNumer in the while loop.
            int lineNumber = 0;
            String topPackageName = prefix + ".";
            if (_generateInSubdirectory) {
                topPackageName = _sanitizedModelName + ".";
            }
            _typeDeclarations = new StringBuffer("import " + topPackageName
                    + "Token;" + _eol);
            HashSet<String> functions = _getReferencedFunctions();

            HashSet<String> types = _getReferencedTypes(functions);
            String[] typesArray = new String[types.size()];
            types.toArray(typesArray);
            // Add imports for non-empty declareBlocks (usually just Array)
            for (String element : typesArray) {
                String typesTemplate = "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/java/type/"
                        + element + ".j";
                CodeStream codeStream = new CodeStream(typesTemplate, this);
                try {
                    if (codeStream.getCodeBlock("declareBlock").length() > 0) {
                        String typeName = _typeNameCG(element);
                        _typeDeclarations.append("import " + topPackageName
                                + typeName + ";" + _eol);
                    }
                } catch (IllegalActionException ex) {
                    IOException exception = new IOException(
                            "Failed to get the declare block for " + element
                            + " from " + typesTemplate);
                    exception.initCause(ex);
                    throw exception;
                }
            }

            StringBuffer body;

            // imports for the classes that define the variables.
            StringBuffer declarations = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                String className = prefix + ".class" + methodNumber++;
                lineNumber++;
                body = new StringBuffer(line + _eol);
                for (int i = 0; i + 1 < linesPerMethod && line != null; i++) {
                    lineNumber++;
                    line = bufferedReader.readLine();
                    if (line != null) {
                        body.append(line + _eol);
                    }
                }

                // If we have less than linesPerMethod lines, we don't
                // use body
                String packageName = className.substring(0,
                        className.lastIndexOf('.'));
                if (_generateInSubdirectory) {
                    packageName = _sanitizedModelName + "." + packageName;
                    className = _sanitizedModelName + "." + className;
                }
                declarations
                .append("import static " + className + ".*;" + _eol);

                String shortClassName = className.substring(className
                        .lastIndexOf('.') + 1);
                body.insert(0, "package " + packageName + ";" + _eol
                        + _typeDeclarations.toString() + _eol + "public class "
                        + shortClassName + " { " + _eol);
                body.append("}" + _eol);
                results.add(body.toString());
            }
            results.set(0,
                    _typeDeclarations.toString() + declarations.toString());

            if (lineNumber <= linesPerMethod) {
                // We must have less than linesPerMethod lines in the body
                results = new LinkedList<String>();
                results.add("");
                results.add(code);
            }
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }
        return results;
    }

    /**
     * Get the corresponding type in Java from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The Java data type.
     */
    @Override
    public String targetType(Type ptType) {
        // FIXME: this is duplicated code from ProgramCodeGeneratorAdapter.targetType()
        // FIXME: we may need to add more primitive types.
        return ptType == BaseType.INT ? "int"
                : ptType == BaseType.STRING ? "String"
                        : ptType == BaseType.DOUBLE ? "double"
                                : ptType == BaseType.BOOLEAN ? "boolean"
                                        : ptType == BaseType.LONG ? "long"
                                                : ptType == BaseType.UNSIGNED_BYTE ? "byte"
                                                        // FIXME: Why do we have to use equals with BaseType.OBJECT?
                                                        // Object and Complex types are not primitive types.
                                                        // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/ObjectToken1.xml
                                                        //: ptType.equals(BaseType.OBJECT) ? "Object"
                                                        //: ptType == BaseType.OBJECT ? "Object"

                                                        //: ptType == PointerToken.POINTER ? "void*"
                                                        : "Token";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add include directories specified by the actors in this model.
     *  @exception IllegalActionException If thrown when getting an actor's
     *   include directories.
     */
    @Override
    protected void _addActorIncludeDirectories() throws IllegalActionException {
        // NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());

        // Set actorIncludeDirectories = adapter.getIncludeDirectories();
        // Iterator includeIterator = actorIncludeDirectories.iterator();
        // while (includeIterator.hasNext()) {
        //     String includeDirectory = (String) includeIterator.next();
        //     System.out.println("JCG._addActorIncludeDirectories(): " + includeDirectory);
        //     addInclude(includeDirectory);
        // }
    }

    /** Add libraries specified by the actors in this model.
     *  Libraries are specified in the "libraryDirectories" block of the template.
     *  FIXME: this might only be getting libraries from the TypedAtomicActor.
     *  @see ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator#addLibrary(String)
     *  @see ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator#addLibraryIfNecessary(String)
     *  @exception IllegalActionException If thrown when getting an actor's
     *   libraries.
     */
    @Override
    protected void _addActorLibraries() throws IllegalActionException {
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());

        Set actorLibraryDirectories = adapter.getLibraryDirectories();
        Iterator libraryDirectoryIterator = actorLibraryDirectories.iterator();
        while (libraryDirectoryIterator.hasNext()) {
            String libraryDirectory = (String) libraryDirectoryIterator.next();
            addLibrary(libraryDirectory);
        }

        // Set actorLibraries = adapter.getLibraries();
        // Iterator librariesIterator = actorLibraries.iterator();
        // while (librariesIterator.hasNext()) {
        //     addLibrary("-l\"" + ((String) librariesIterator.next()) + "\"");
        // }
    }

    /** Add the directories and files from the classpath to
     *  the list of libraries.
     *  This method is used to add the JavaScope.zip file used by code
     *  coverage so that we can use Ptolemy classes in the nightly build.
     */
    protected void _addClassPathLibraries() {
        String javaClassPath = StringUtilities.getProperty("java.class.path");
        StringTokenizer tokenizer = new StringTokenizer(javaClassPath,
                File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            addLibraryIfNecessary(tokenizer.nextToken());
        }
    }

    /** Analyze the model to find out what connections need to be type
     *  converted. This should be called before all the generate methods.
     *  @exception IllegalActionException If the adapter of the
     *   top composite actor is unavailable.
     */
    @Override
    protected void _analyzeTypeConversions() throws IllegalActionException {
        super._analyzeTypeConversions();

        // FIXME: Refer to cg instead.
        String cCodegenPath = "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/java/";
        String typeDir = cCodegenPath + "type/";
        String functionDir = typeDir + "polymorphic/";

        _overloadedFunctions = new CodeStream(functionDir + "add.j", this);
        _overloadedFunctions.parse(functionDir + "equals.j");
        _overloadedFunctions.parse(functionDir + "modulo.j");
        _overloadedFunctions.parse(functionDir + "multiply.j");
        _overloadedFunctions.parse(functionDir + "divide.j");
        _overloadedFunctions.parse(functionDir + "subtract.j");
        _overloadedFunctions.parse(functionDir + "toString.j");
        _overloadedFunctions.parse(functionDir + "convert.j");
        _overloadedFunctions.parse(functionDir + "print.j");
        _overloadedFunctions.parse(functionDir + "negate.j");
        _overloadedFunctions.parse(functionDir + "zero.j");
        _overloadedFunctions.parse(functionDir + "one.j");
        _overloadedFunctions.parse(typeDir + "Array.j");
        _overloadedFunctions.parse(typeDir + "Boolean.j");
        _overloadedFunctions.parse(typeDir + "Complex.j");
        _overloadedFunctions.parse(typeDir + "Double.j");
        _overloadedFunctions.parse(typeDir + "Integer.j");
        _overloadedFunctions.parse(typeDir + "Matrix.j");
        _overloadedFunctions.parse(typeDir + "Object.j");
        _overloadedFunctions.parse(typeDir + "String.j");

        //        String directorFunctionDir = cCodegenPath + "parameterized/directorFunctions/";
        //        _overloadedFunctions.parse(directorFunctionDir + "PNDirector.java");
        //        _overloadedFunctions.parse(directorFunctionDir + "OpenRtosPNDirector.java");
        //        _overloadedFunctions.parse(directorFunctionDir + "MpiPNDirector.c");

        _overloadedFunctionSet = new HashSet<String>();

    }

    /** Given a Collection of Strings, return a string where each element of the
     *  Set is separated by $.
     *  @param collection The Collection of Strings.
     *  @return A String that contains each element of the Set separated by
     *  a space.
     */
    protected String _concatenateClasspath(Collection<String> collection) {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (buffer.length() > 0) {
                //buffer.append("$(CLASSPATHSEPARATOR)");
                buffer.append(File.pathSeparator);
            }
            buffer.append(iterator.next());
        }
        if (buffer.length() > 0) {
            //buffer.append("$(CLASSPATHSEPARATOR)");
            buffer.append(File.pathSeparator);
        }
        return buffer.toString();
    }

    /** Make a final pass over the generated code. Subclass may extend
     * this method to do extra processing to format the output code. If
     * sourceLineBinding is set to true, it will check and insert the
     * appropriate #line macro for each line in the given code. Blank lines
     * are discarded if #line macros are inserted.
     * @param code The given code to be processed.
     * @return The processed code.
     * @exception IllegalActionException If #getOutputFilename() throws it.
     */
    @Override
    protected StringBuffer _finalPassOverCode(StringBuffer code)
            throws IllegalActionException {

        // Simple cpp like preprocessor
        // #define foo
        // #ifdef foo
        // #endif
        // Note that foo does not have a value.
        // Nested ifdefs are not supported.

        StringTokenizer tokenizer = new StringTokenizer(code.toString(), _eol
                + "\n");

        code = new StringBuffer();

        boolean okToPrint = true;
        HashSet<String> defines = new HashSet<String>();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            if (line.indexOf("#") == -1) {
                if (!okToPrint) {
                    code.append("//" + line + _eol);
                } else {
                    // Use // style comments in case there is a /* .. */ comment.
                    code.append(line + _eol);
                }
            } else {
                line = line.trim();
                int defineIndex = line.indexOf("#define");
                if (defineIndex > -1) {
                    String define = line.substring(defineIndex + 8);
                    if (define.indexOf(" ") != -1) {
                        define = define.substring(0, define.indexOf(" "));
                    }
                    defines.add(define);
                }
                int ifIndex = line.indexOf("#ifdef");
                if (ifIndex > -1) {
                    String define = line.substring(ifIndex + 7);
                    if (define.indexOf(" ") != -1) {
                        define = define.substring(0, define.indexOf(" "));
                    }
                    if (defines.contains(define)) {
                        okToPrint = true;
                    } else {
                        okToPrint = false;
                    }
                } else {
                    if (line.startsWith("#endif")) {
                        okToPrint = true;
                    }
                }
                code.append("// " + line + _eol);
            }
        }

        // Run the pass over the code after pseudo preprocessing
        code = super._finalPassOverCode(code);

        return code;
    }

    /** Generate include files. FIXME: State what is included.
     *  @return The #include statements, surrounded by #ifndef to ensure
     *   that the files are included only once.
     *  @exception IllegalActionException If the adapter class for some actor
     *   cannot be found.
     */
    @Override
    protected String _generateIncludeFiles() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        NamedProgramCodeGeneratorAdapter compositeActorAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        Set<String> includingFiles = compositeActorAdapter.getHeaderFiles();

        for (String file : includingFiles) {
            if (!file.equals("<math.h>") && !file.equals("<stdio.h>")) {
                code.append("import " + file + _eol);
            }
        }
        code.append("public class " + _sanitizedModelName + " {" + _eol);
        return code.toString();
    }

    /** Generate the preinitialization method body.
     *
     *  <p>Typically, the preinitialize code consists of variable
     *   declarations.  However, AutoAdapter generates method calls
     *   that instantiate wrapper TypedCompositeActors, so we need
     *   to invoke those method calls.</p>
     *
     *  @return a string for the preinitialization method body.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected String _generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer(
                super._generatePreinitializeMethodBodyCode() + _eol);
        if (containsCode(code.toString())) {
            code.append("if (_toplevel != null) {" + _eol
                    + "    _toplevel.preinitialize();" + _eol + "}" + _eol);
        }
        return comment("JCG preintialization body code") + _eol
                + code.toString();
    }

    /** Generate the preinitialization procedure entry point.
     *  @return a string for the preinitialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected String _generatePreinitializeMethodEntryCode()
            throws IllegalActionException {
        return _eol + _eol + "public void preinitialize() throws Exception {"
                + _eol;
    }

    /** Generate the preinitialization procedure exit point.
     *  @return a string for the preinitialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected String _generatePreinitializeMethodExitCode()
            throws IllegalActionException {
        return "}" + _eol;
    }

    /** Generate the preinitialization procedure name.
     *  @return a string for the preinitialization procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected String _generatePreinitializeMethodProcedureName()
            throws IllegalActionException {
        return "// Don't call preinitialize() here, it is called in main.";
    }

    /**
     * Attempt to automatically generate an adapter.
     * @param codeGenerator The code generator with which to associate the adapter.
     * @param object The given object.
     * @return The code generator adapter or null if no adapter can be
     * automatically generated.
     */
    @Override
    protected CodeGeneratorAdapter _getAutoGeneratedAdapter(
            GenericCodeGenerator codeGenerator, Object object) {
        // This method is called by GenericCodeGenerator.getAdapter().

        // See
        // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=342

        return AutoAdapter.getAutoAdapter(codeGenerator, object);
    }

    /** Return the prototype for fire functions.
     * @return The string " throws Exception" is
     * appended to the value returned by the superclass.
     */
    @Override
    protected String _getFireFunctionArguments() {
        return super._getFireFunctionArguments() + " throws Exception";
    }

    /** Generate the code for printing the execution time since
     *  the code generated by _recordStartTime() was called.
     *  @return Return the code for printing the total execution time.
     */
    @Override
    protected String _printExecutionTime() {
        StringBuffer endCode = new StringBuffer();
        endCode.append(super._printExecutionTime());

        endCode.append("Runtime runtime = Runtime.getRuntime();\n"
                + "long totalMemory = runtime.totalMemory() / 1024;\n"
                + "long freeMemory = runtime.freeMemory() / 1024;\n"
                + "System.out.println(System.currentTimeMillis() - startTime + \""
                + " ms. Memory: \" + totalMemory + \"K Free: \""
                + " + freeMemory + \"K (\" + "
                + "Math.round((((double) freeMemory) / ((double) totalMemory)) * 100.0)"
                + " + \"%\");\n");
        return endCode.toString();
    }

    /** Process the specified code for the adapter associated with the
     *  container.  Replace macros with their values.
     *  @param code The code to process.
     *  @return The processed code.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    protected String _processCode(String code) throws IllegalActionException {
        ProgramCodeGeneratorAdapter adapter = (ProgramCodeGeneratorAdapter) getAdapter(getContainer());
        return adapter.processCode(code);
    }

    /** Generate the code for recording the current time.
     *  This writes current time into a timespec struct called "start".
     *  @return Return the code for recording the current time.
     */
    @Override
    protected String _recordStartTime() {
        StringBuffer startCode = new StringBuffer();
        startCode.append("long startTime = System.currentTimeMillis();");
        return startCode.toString();
    }

    /** Reset the code generator.
     *  After calling _reset(), you must call _analyzeTypeConversions()
     *  before calling the generate methods again.
     *  @exception IllegalActionException Not thrown in this base class,
     *  thrown by the parent if the container of the model
     *  cannot be set to null.
     */
    @Override
    protected void _reset() throws IllegalActionException {
        super._reset();
        _overloadedFunctions = null;
        if (_overloadedFunctionSet != null) {
            _overloadedFunctionSet.clear();
        }
        _typeDeclarations = null;
        if (_variableTypeMap != null) {
            _variableTypeMap.clear();
        }
        if (_variableTypeMaxIndex != null) {
            _variableTypeMaxIndex.clear();
        }
        //         if (_substituteMap != null) {
        //             _substituteMap.clear();
        //         }
    }

    /** Return the class of the templateParser class. In cse
     *  there isn't one return null.
     *  @return The base class for templateParser.
     */
    @Override
    protected Class<? extends TemplateParser> _templateParserClass() {
        return JavaTemplateParser.class;
    }

    /** Read in a template makefile, substitute variables and write
     *  the resulting makefile.
     *
     *  <p>If a <code>.mk.in</code> file with the name of the sanitized model
     *  name, then that file is used as a template.  For example, if the
     *  model name is <code>Foo</code> and the file <code>Foo.mk.in</code>
     *  exists, then the file <code>Foo.mk.in</code> is used as a makefile
     *  template.
     *
     *  <p>If no <code>.mk.in</code> file is found, then the makefile
     *  template can be found by looking up a resource name
     *  makefile.in in the package named by the
     *  <i>generatorPackage</i> parameter.  Thus, if the
     *  <i>generatorPackage</i> has the value "ptolemy.codegen.c",
     *  then we look for the resource "ptolemy.codegen.c.makefile.in", which
     *  is usually found as <code>$PTII/ptolemy/codegen/c/makefile.in</code>.
     *
     *  <p>The makefile is written to a directory named by the
     *  <i>codeDirectory</i> parameter, with a file name that is a
     *  sanitized version of the model name, and a ".mk" extension.
     *  Thus, for a model named "Foo", we might generate a makefile in
     *  "$HOME/codegen/Foo.mk".

     *  <p>Under Java under Windows, your <code>$HOME</code> variable
     *  is set to the value of the <code>user.home</code>System property,
     *  which is usually something like
     *  <code>C:\Documents and Settings\<i>yourlogin</i></code>, thus
     *  for user <code>mrptolemy</code> the makefile would be
     *  <code>C:\Documents and Settings\mrptolemy\codegen\Foo.mk</code>.
     *
     *  <p>The following variables are substituted
     *  <dl>
     *  <dt><code>@modelName@</code>
     *  <dd>The sanitized model name, created by invoking
     *  {@link ptolemy.util.StringUtilities#sanitizeName(String)}
     *  on the model name.
     *  <dt><code>@PTCGIncludes@</code>
     *  <dd>The elements of the set of include command arguments that
     *  were added by calling {@link #addInclude(String)}, where each
     *  element is separated by a space.
     *  <dt><code>@PTCGLibraries@</code>
     *  <dd>The elements of the set of library command arguments that
     *  were added by calling {@link #addLibrary(String)}, where each
     *  element is separated by a space.
     *  </dl>
     *  @param container The composite actor for which we generate the makefile
     *  @param currentDirectory The director in which the makefile is to be written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    @Override
    protected void _writeMakefile(CompositeEntity container,
            String currentDirectory) throws IllegalActionException {

        if (!_libraries.isEmpty()) {
            // Loop through the path elements in java.class.path and add
            // them as libraries.  We need this so that we can find the
            // JavaScope.zip code coverage file in the nightly build
            _addClassPathLibraries();
        }

        String root = ".";
        String modelClass = _sanitizedModelName;
        if (((BooleanToken) generateInSubdirectory.getToken()).booleanValue()) {
            root = "..";
            modelClass = _sanitizedModelName + "." + _sanitizedModelName;
            addLibraryIfNecessary("..");
        }

        _substituteMap.put("@ROOT@", root);

        if (_libraries.size() == 0) {
            addLibraryIfNecessary(".");
        }

        String ptcgLibraries = _concatenateClasspath(_libraries);
        // Remove any trailing : and avoid having ".::.." in the makefile.
        if (ptcgLibraries.endsWith(":")) {
            ptcgLibraries = ptcgLibraries.substring(0,
                    ptcgLibraries.length() - 1);
        }
        _substituteMap.put("@PTCGLibraries@", ptcgLibraries);

        _substituteMap.put("@MODELCLASS@", modelClass);

        super._writeMakefile(container, currentDirectory);
    }

    /** Write the variable declaration code.
     *  <p>If the first element is the empty String, then the second
     *  element contains all of the variable declarations and this method
     *  does not create files and the value of the second element is returned.
     *  If the first element is not the empty string, then the second
     *  and successive elements are written to a file whos name is
     *  created by parsing the first element.  The first element must
     *  consist of strings like "import static variables.foo;", the file name
     *  will be "variables/foo.java".
     *  @param variableDeclarations A List of two or more elements.
     *  first element is not empty, then it contains the language
     *  specific declarations for the variable declarations.  In Java,
     *  the first element may consist of one or more "import"
     *  statements.  The second and successive elements contain the
     *  code to be written to separate files or to be returned as one
     *  String.
     *  @return If the first element is empty, then return the value
     *  of the second element.  If the first element is not empty, then
     *  files are created and the empty string is returned.
     *  @exception IllegalActionException Thrown if there is a problem
     *  writing the files.
     */
    @Override
    protected String _writeVariableDeclarations(
            List<String> variableDeclarations) throws IllegalActionException {
        if (variableDeclarations.size() < 2) {
            throw new IllegalActionException(getComponent(),
                    "_writeVariableDeclarations called "
                            + "with an list of less than two elements.");
        }
        if (variableDeclarations.size() == 2) {
            if (variableDeclarations.get(0).length() > 0) {
                throw new IllegalActionException(
                        getComponent(),
                        "_writeVariableDeclarations called "
                                + "with a list of two elements but the first element is "
                                + "non-empty.");
            } else {
                return variableDeclarations.get(1);
            }
        }

        // Write out the second and successive elements.

        boolean sawTokenImport = false;
        File codeDirectoryFile = null;
        try {
            codeDirectoryFile = _codeDirectoryAsFile();
        } catch (IOException ex) {
            throw new IllegalActionException(this, getComponent(),
                    "Failed to get the codeDirectory as a parameter");
        }
        String directoryName = "";

        // For use with Matrix.java, Array.java etc.
        HashSet<String> functions = _getReferencedFunctions();
        HashSet<String> types = _getReferencedTypes(functions);
        types.add("Token");
        String[] typesArray = new String[types.size()];
        types.toArray(typesArray);

        BufferedReader importReader = null;
        StringBuffer result = new StringBuffer();
        int blockNumber = 0;
        for (String block : variableDeclarations) {
            if (++blockNumber == 1) {
                // The first element contains the import directives
                importReader = new BufferedReader(new StringReader(block));
            } else {
                String importLine = null;
                try {
                    importLine = importReader.readLine();
                } catch (IOException ex) {
                    throw new IllegalActionException(getComponent(), ex,
                            "Failed to read a line from the imports in\n"
                                    + block);
                }
                if (importLine != null) {
                    // The first import is of the form "import RepeatVariables.Token;"
                    // FIXME: This is weak, what happens if the format changes.
                    int i = 0;
                    for (; i < typesArray.length && importLine != null; i++) {
                        if (importLine.endsWith(".Token;")) {
                            sawTokenImport = true;
                        }
                        String typeName = _typeNameCG(typesArray[i]);
                        if (importLine.endsWith(typeName + ";")) {
                            try {
                                // Reset the loop of types to the
                                // first element so that we may search
                                // the new import line.
                                i = -1;
                                importLine = importReader.readLine();
                            } catch (IOException ex) {
                                throw new IllegalActionException(
                                        getComponent(), ex,
                                        "Failed to read line after \"import ..."
                                                + typeName + ";\""
                                                + "from the imports in\n"
                                                + block);
                            }
                        }
                    }
                    // Second and successive lines are of
                    // the form "import static RepeatVariables.class0.*;".

                    if (importLine == null) {
                        throw new InternalErrorException(
                                "Last import line was null? Read " + i
                                + " lines of " + typesArray.length
                                + " lines.");
                    }

                    // Get rid of the ".*;".
                    if (importLine.lastIndexOf(".*;") == -1) {
                        throw new InternalErrorException("Import line: "
                                + importLine + " does not have .*;?");
                    }
                    String shortImportLine = importLine.substring(0,
                            importLine.lastIndexOf(".*;"));
                    // Find the last space, then get the text up to the last char.
                    String filepath = shortImportLine.substring(
                            shortImportLine.lastIndexOf(' ') + 1,
                            shortImportLine.length()).replace('.', '/');
                    // Create the directory, if necessary.
                    directoryName = filepath.replace('.', '/').substring(0,
                            filepath.lastIndexOf('/'));

                    if (((BooleanToken) generateInSubdirectory.getToken())
                            .booleanValue()) {
                        if (directoryName.indexOf('/') >= 0) {
                            directoryName = directoryName
                                    .substring(directoryName.indexOf('/'));
                        } else {
                            System.out
                                    .println("Warning: JavaCodeGenerator: directoryName \""
                                            + directoryName
                                            + "\" has no slashes?");
                        }
                    }
                    File directory = new File(codeDirectoryFile, directoryName);
                    if (!directory.isDirectory()) {
                        if (!directory.mkdirs()) {
                            throw new IllegalActionException(getComponent(),
                                    "Failed to create directory \""
                                            + directoryName + "\"");
                        }
                    }

                    File file = new File(directory, filepath.substring(filepath
                            .lastIndexOf("/") + 1) + ".java");

                    FileWriter writer = null;
                    try {
                        try {
                            writer = new FileWriter(file);
                        } catch (IOException ex) {
                            throw new IllegalActionException(getComponent(),
                                    ex, "Failed to open \"" + file + "\"");
                        }
                        try {
                            writer.write(block);
                            if (block
                                    .indexOf(StaticSchedulingDirector.CURRENTTIME_DECLARATION) != -1) {

                                // Output the currentTime declaration, which is
                                // not static because _currentTime might? vary
                                // between directors?
                                result.append(StaticSchedulingDirector.CURRENTTIME_DECLARATION
                                        + _eol);
                            }
                        } catch (IOException ex) {
                            throw new IllegalActionException(getComponent(),
                                    ex, "Failed to write " + "block "
                                            + blockNumber + " to \"" + file
                                            + "\"");
                        }
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException ex) {
                                throw new IllegalActionException(
                                        getComponent(), ex,
                                        "Failed to close \"" + file + "\"");
                            }
                        }
                    }
                }
            }
        }

        HashSet<String> typesAndToken = _getReferencedTypes(functions);
        //        CodeStream[] typeStreams = new CodeStream[typesAndToken.size()];
        if (sawTokenImport) {
            // Add Token to the set of types so that we only need one loop
            typesAndToken.add("Token");
            //            typeStreams = new CodeStream[typesAndToken.size()];
        }

        if (!((BooleanToken) generateInSubdirectory.getToken()).booleanValue()) {
            Object[] typesAndTokenArray = typesAndToken.toArray();
            for (int i = 0; i < typesAndTokenArray.length; i++) {
                // Create Array, Matrix or Token.java in the variable directory.
                StringBuffer declareTypeOrTokenBlock = new StringBuffer();

                CodeStream codeStream = null;
                if (!typesAndTokenArray[i].equals("Token")) {
                    // Array or Matrix
                    codeStream = new CodeStream(
                            "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/java/type/"
                                    + typesAndTokenArray[i] + ".j", this);
                    declareTypeOrTokenBlock.append(codeStream
                            .getCodeBlock("declareBlock"));
                } else {
                    // Token
                    codeStream = new CodeStream(
                            "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/java/SharedCode.j",
                            this);
                    declareTypeOrTokenBlock.append(codeStream
                            .getCodeBlock("tokenDeclareBlock"));
                    // FIXME: what about empty tokens
                    //if (defineEmptyToken) {
                    //declareTypeOrTokenBlock.append("public static Token emptyToken; "
                    //        + comment("Used by *_delete() and others.") + _eol);
                    //}
                }
                if (declareTypeOrTokenBlock.length() > 0) {
                    declareTypeOrTokenBlock.insert(0, "package "
                            + directoryName + ";" + _eol);
                    String typeName = typesAndTokenArray[i].toString();
                    typeName = _typeNameCG(typeName);
                    _writeCodeFileName(declareTypeOrTokenBlock,
                            codeDirectoryFile.toString() + "/" + directoryName
                            + "/" + typeName + ".java", false, true);
                    // FIXME: we should not be deleting the .java and .class file
                    // in the top level, instead, we should be writing our code
                    // into a subdirectory.
                    File topTokenFile = new File(codeDirectoryFile, typeName
                            + ".java");
                    if (!topTokenFile.delete()) {
                        throw new IllegalActionException("Failed to delete "
                                + topTokenFile);
                    }
                    File topTokenClass = new File(codeDirectoryFile, typeName
                            + ".class");
                    if (!topTokenClass.delete()) {
                        System.out.println("Warning: Failed to delete "
                                + topTokenClass);
                    }
                }
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the set of referenced functions.
     * @return The set of referenced functions.
     */
    private HashSet<String> _getReferencedFunctions() {
        // Determine the total number of referenced polymorphic functions.
        HashSet<String> functions = new HashSet<String>();
        functions.add("new");
        functions.add("delete");
        //functions.add("toString");    // for debugging.
        functions.add("convert");
        functions.add("isCloseTo");
        functions.addAll(_typeFuncUsed);
        functions.addAll(_tokenFuncUsed);
        //         System.out.println("JavaCodeGenerator: all type functions: " + _typeFuncUsed);
        //         System.out.println("JavaCodeGenerator: all token functions: " + _tokenFuncUsed);
        //         System.out.println("JavaCodeGenerator: all referenced functions: " + functions);
        return functions;
    }

    /** Return the new types used by the given set of functions.
     * @param functions The set of functions used, such as "equals",
     * "isCloseTo", and "toString".
     * @return The new types used.
     */
    private HashSet<String> _getReferencedTypes(HashSet<String> functions) {
        // FIXME: Why is this not called _getNewTypesUsed() like what is in
        // ptolemy/cg/kernel/generic/program/procedural/c/CCodeGenerator.java
        // Determine the total number of referenced types.
        HashSet<String> types = new HashSet<String>();
        if (functions.contains("equals") || functions.contains("isCloseTo")) {
            types.add("Boolean");
        }

        if (functions.contains("toString")) {
            types.add("String");
        }

        if (functions.contains("isCloseTo")
                && _newTypesUsed.contains("Integer")
                && !_newTypesUsed.contains("Double")) {
            // FIXME: we should not need Double for Int_isCloseTo()
            types.add("Double");
        }

        types.addAll(_newTypesUsed);
        //System.out.println("JavaCodeGenerator: all referenced types: " + types);
        return types;
    }

    /** If the word argument is a Java keyword, return a sanitized version.
     *  This method is used when inline is false so that if we have
     *  an actor with the same name as a Java keyword, we don't end
     *  up trying to create a method with with that name.
     *  @param word The string to be checked.
     *  @return the sanitized version.
     */
    private String _javaKeywordSanitize(String word) {
        // This is needed by $PTII/bin/ptcg -language java -variablesAsArrays true -inline false $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/hoc/test/auto/CaseOpaque.xml
        if (word.equals("default")) {
            return "xdefault";
        } else if (word.equals("true")) {
            return "xtrue";
        }
        return word;
    }

    /** Return the base name of the file that defines methods for the type
     *
     *  @param typeName The name of the type to be checked.
     *  @return If the typeName is Complex or Object, then return ComplexCG or ObjectCG,
     *  otherwise, return the typeName;
     */
    private static String _typeNameCG(String typeName) {
        if (typeName.equals("Complex")) {
            typeName = "ComplexCG";
        } else if (typeName.equals("Object")) {
            typeName = "ObjectCG";
        }
        return typeName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private CodeStream _overloadedFunctions;

    private Set<String> _overloadedFunctionSet;

    /** Java import statements for Token, Array, etc.
     */
    private StringBuffer _typeDeclarations;

    /** A map from String type name to a HashMap of variable name to
     * Array Index.  Used for large models to reduce the number
     * of variables
     */
    private HashMap<String, HashMap<String, Integer>> _variableTypeMap;

    /** A map from String type name to a HashMap of variable name to
     * Array Index.  Used for large models to reduce the number
     * of variables
     */
    private HashMap<String, Integer> _variableTypeMaxIndex;

    /** Set of type/function combinations that are not supported.
     *  We use one method so as to reduce code size.
     */
    private static Set<String> _unsupportedTypeFunctions;

    /** Types that share the scalarDelete() method, which does nothing.
     *  We use one method so as to reduce code size.
     */
    private static Set<String> _scalarDeleteTypes;

    static {
        _unsupportedTypeFunctions = new HashSet<String>();
        _unsupportedTypeFunctions.add("String_divide");
        _unsupportedTypeFunctions.add("String_multiply");
        _unsupportedTypeFunctions.add("String_negate");
        _unsupportedTypeFunctions.add("String_one");
        _unsupportedTypeFunctions.add("String_subtract");

        _unsupportedTypeFunctions.add("Boolean_divide");
        _unsupportedTypeFunctions.add("Boolean_multiply");
        _unsupportedTypeFunctions.add("Boolean_subtract");

        _scalarDeleteTypes = new HashSet<String>();
        _scalarDeleteTypes.add("Boolean");
        _scalarDeleteTypes.add("Double");
        _scalarDeleteTypes.add("Integer");
        _scalarDeleteTypes.add("Long");
    }
}
