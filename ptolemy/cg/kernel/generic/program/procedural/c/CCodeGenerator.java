/* Code generator for the C language.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program.procedural.c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralTemplateParser;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.BaseType.StringType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.FSMDirector;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.domains.modal.modal.Refinement;
import ptolemy.domains.ptides.kernel.PtidesDirector;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CCodeGenerator

/** Base class for C code generator.
 *
 *  @author Gang Zhou, William Lucas
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (wlc)
 *  @Pt.AcceptedRating red (wlc)
 */

public class CCodeGenerator extends ProceduralCodeGenerator {

    /** Create a new instance of the C code generator.
     * This class is the core of the C Code generation
     * For instance, the _generateCode method is key to the
     * code generation.
     *  @param container The container.
     *  @param name The name of the C code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public CCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, "c", "c");

        generateCpp = new Parameter(this, "generateCpp");
        generateCpp.setTypeEquals(BaseType.BOOLEAN);
        generateCpp.setExpression("false");

        sourceLineBinding = new Parameter(this, "sourceLineBinding");
        sourceLineBinding.setTypeEquals(BaseType.BOOLEAN);
        sourceLineBinding.setExpression("false");

        generatorPackageList.setExpression("generic.program.procedural.c");

        // FIXME: we should not have to set these each time, but
        // JavaCodeGenerator uses Integer, and CCodeGenerator uses Int
        _primitiveTypes = Arrays.asList(new String[] { "Int", "Double",
                "String", "Long", "Boolean", "UnsignedByte", "Pointer" });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, the generated code will be C++ instead of C.
     * FIXME: This is a temporary fix.  In the long run, C++ should
     * be its own target language for code generation.  In the short
     * run, this parameter will allow experimentation with C++ code
     * generation, and should identify changes needed for correctly
     * implemented C++ code generation.
     */
    public Parameter generateCpp;

    /** If true, then the generated source is bound to the line
     *  number and file of the (adapter) templates. Otherwise, the
     *  source is bound only to the output file. This is a boolean
     *  parameter with default value false.
     */
    public Parameter sourceLineBinding;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CCodeGenerator newObject = (CCodeGenerator) super.clone(workspace);

        newObject._definesToAdd = new LinkedList<String>();
        return newObject;
    }

    /** Generate code that defines a constant.  In C, generate a
     *  #define
     *  @param constant The name of the constant to be defined
     *  @param type A string representing the type.  In C, this
     *  parameter is ignored.
     *  @param value The value of the constant.
     *  @return A #define that defines the constant.
     */
    @Override
    public String generateConstantDefinition(String constant, String type,
            String value) {
        // Maybe we should keep track of these in a Set?
        return "#ifndef " + constant + _eol + "#define " + constant + " "
        + value + _eol + "#endif" + _eol;
    }

    /** Generate the fire function method name. This method is called
     *  when the firing code of each actor is not inlined.  In this
     *  class, each actor's firing code is in a function with the
     *  same name as that of the actor, with the "fire" id
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return The name of the fire function method.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes should throw this exception if there are problems
     *  accessing the name or generating the name.
     */
    @Override
    public String generateFireFunctionMethodName(NamedObj namedObj)
            throws IllegalActionException {
        return TemplateParser.escapeName(CodeGeneratorAdapter
                .generateName(namedObj)) + "_fire";
    }

    /** Generate the function table.  In this base class return
     *  the empty string.
     *  @param types An array of types.
     *  @param functions An array of functions.
     *  @return The code that declares functions.
     */
    public String generateFunctionTable(String[] types, String[] functions) {
        // FIXME: make this private?
        StringBuffer code = new StringBuffer();

        if (functions.length > 0 && types.length > 0) {

            code.append("Token* (*functionTable[NUM_TYPE][NUM_FUNC])"
                    + "(Token*, ...)= {" + _eol);

            for (int i = 0; i < types.length; i++) {
                if (types[i].endsWith("Structure")) {
                    continue;
                }
                code.append("\t{");
                for (int j = 0; j < functions.length; j++) {
                    // Do not add the *_new() methods because they
                    // take a primitive type, not a Token.
                    if (!functions[j].equals("new")) {
                        if (functions[j].equals("isCloseTo")
                                && (types[i].equals("Boolean") || types[i]
                                        .equals("String"))) {
                            // Boolean_isCloseTo and String_isCloseTo
                            // are the same as their corresponding *_equals
                            code.append(types[i] + "_equals");
                        } else {
                            // Check to see if the type/function combo is supported.
                            String typeFunctionName = types[i] + "_"
                                    + functions[j];
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
                }
                if (i != types.length - 1) {
                    code.append("},");
                } else {
                    code.append("}");
                }
                code.append(_eol);
            }

            code.append("};" + _eol);
        }
        return code.toString();
    }

    /** Generate the initialization procedure entry point.
     *  @return a string for the initialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateInitializeEntryCode() throws IllegalActionException {
        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            // We use (void) so as to avoid the avr-gcc 3.4.6 warning:
            // "function declaration isn't a prototype
            return _eol + _eol + "void initialize(void) {" + _eol;

            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            String throwExceptionCode = _eol
                    + _eol
                    + "void throwInternalErrorException()"
                    + _eol
                    + "{"
                    + _eol
                    + "   jclass exceptionClass;"
                    + _eol
                    + "   char *className = \"java/lang/Exception\";"
                    + _eol
                    + "   exceptionClass = (*env)->FindClass(env,className);"
                    + _eol
                    + "   fprintf(stderr, \"%s: %d: throwing %s, class %p.\\n\", __FILE__, __LINE__, className, exceptionClass);"
                    + _eol

                    + "   fprintf(stderr, \"throwing returned %d\\n\", (*env)->ThrowNew(env, exceptionClass, \"Check stdout\"));"
                    + "}" + _eol;
            String escapeName = _sanitizedModelName.replaceAll("_", "_1");
            return throwExceptionCode + _eol + _eol + "JNIEXPORT void JNICALL"
                    + _eol + "Java_" + escapeName + "_initialize("
                    + "JNIEnv *env_glob, jobject obj_glob) {" + _eol
                    + "env = env_glob;" + _eol + "obj = obj_glob;";

        }
    }

    /** Return the closing entry code, if any.
     *  @return the closing entry code.
     */
    @Override
    public String generateClosingEntryCode() {
        return "void doWrapup() { " + _eol;
    }

    /** Return the closing exit code, if any.
     *  @return the closing exit code.
     */
    @Override
    public String generateClosingExitCode() {
        return "}" + _eol;
    }

    /** Generate the initialization procedure exit point.
     *  @return a string for the initialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateInitializeExitCode() throws IllegalActionException {
        return "}" + _eol;
    }

    /** Generate the initialization procedure name.
     *  @return a string for the initialization procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateInitializeProcedureName()
            throws IllegalActionException {
        if (_isTopLevel()) {
            // We use (void) so as to avoid the avr-gcc 3.4.6 warning:
            // "function declaration isn't a prototype
            return _eol + _eol + "void initialize(void);" + _eol;

            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            String escapeName = _sanitizedModelName.replaceAll("_", "_1");
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
            + escapeName + "_initialize("
            + "JNIEnv *env_glob, jobject obj_glob);" + _eol;
        }
        //return "// Don't call initialize() here, it is called in main.";
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
        return "#line " + lineNumber + " \"" + filename + "\"" + _eol;
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
            // add some function declarations
            //            mainEntryCode.append("boolean run();" + _eol + "void execute();"
            //                    + _eol + "void doWrapup();" + _eol);

            mainEntryCode.append(_eol + _eol
                    + "int main(int argc, char *argv[]) {" + _eol);

            if (((BooleanToken) measureTime.getToken()).booleanValue()) {
                mainEntryCode.append(_eol + "struct timeval start, end;");
                mainEntryCode.append(_eol + "long mtime, secs, usecs;");
                mainEntryCode.append(_eol + "gettimeofday(&start, NULL);");
            }

            mainEntryCode.append(_eol
                    + "//boolean completedSuccessfully = false;");
            mainEntryCode.append(_eol + _eol + "initialize();");

            mainEntryCode.append(_eol + "while (true) {");
            mainEntryCode.append(_eol + "if (!iterate()) {");
            mainEntryCode.append(_eol + "break;");
            mainEntryCode.append(_eol + "}");
            mainEntryCode.append(_eol + "//completedSuccessfully = true;");

            mainEntryCode.append(_eol + "}");
            mainEntryCode.append(_eol + "wrapup();");

            if (((BooleanToken) measureTime.getToken()).booleanValue()) {
                mainEntryCode.append(_eol + "gettimeofday(&end, NULL);");
                mainEntryCode.append(_eol
                        + "secs  = end.tv_sec  - start.tv_sec;");
                mainEntryCode.append(_eol
                        + "usecs = end.tv_usec - start.tv_usec;");
                mainEntryCode.append(_eol
                        + "mtime = ((secs) * 1000 + usecs/1000.0) + 0.5;");
                mainEntryCode.append(_eol
                        + "printf(\"Elapsed time: %ld millisecs\\n\", mtime);");
            }
            mainEntryCode.append(_eol + "exit(0);");

        } else {
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.

            String escapeName = _sanitizedModelName.replaceAll("_", "_1");
            mainEntryCode.append(_eol + _eol + "JNIEXPORT jobjectArray JNICALL"
                    + _eol + "Java_" + escapeName + "_fire (" + _eol
                    + "JNIEnv *env, jobject obj");

            Iterator<?> inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                mainEntryCode.append(", jobjectArray " + inputPort.getName()
                        + "_glob");
            }

            mainEntryCode.append("){" + _eol);
            inputPorts = ((Actor) getContainer()).inputPortList().iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                mainEntryCode.append(_eol + inputPort.getName() + " = "
                        + inputPort.getName() + "_glob;");
            }

            mainEntryCode.append(_eol + "(*(" + _sanitizedModelName
                    + "->fire))(" + _sanitizedModelName + ");");
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
            return _eol + "}" + _eol;
            //return _eol;
        } else {
            return INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                    + _eol;
            //return "}" + _eol;
        }
    }

    /** Generate the postfire procedure entry point.
     *  @return a string for the postfire procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generatePostfireEntryCode() throws IllegalActionException {
        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            return _eol + _eol + "boolean postfire(void) {" + _eol;

            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            String escapeName = _sanitizedModelName.replaceAll("_", "_1");
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
            + escapeName + "_postfire(" + "JNIEnv *env, jobject obj) {"
            + _eol;
        }
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

        return INDENT1 + "postfire(void);" + _eol;
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
     * Note that this method is really different from the super one because
     * it splits the code in a .c part and a .h part.
     * @return The type resolution code.
     * @exception IllegalActionException If an error occurs when generating
     *  the type resolution code, or if the adapter class for the model
     *  director cannot be found, or if an error occurs when the adapter
     *  actor generates the type resolution code.
     */
    public String[] generateTypeConvertCodeCandH()
            throws IllegalActionException {

        StringBuffer codeH = new StringBuffer();
        StringBuffer codeC = new StringBuffer();
        codeC.append("#include \"_ptTypes.h\"");

        codeH.append(_eol + "#include <stdio.h>" + _eol
                + "#include <stdlib.h>" + _eol
                + "/* Define _BSD_SOURCE so that string.h includes a declaration for strdup under RHEL. */" + _eol
                + "#define _BSD_SOURCE" + _eol
                + "#include <string.h>" + _eol
                + "#include <math.h>" + _eol
                + "#include <errno.h>" + _eol
                + "#include <float.h>" + _eol
                + "#include <limits.h>" + _eol
                + "#include <stdarg.h>" + _eol
                + "#include \"pbl.h\"" + _eol);

        // Add the extra-define
        for (String extraDefine : _definesToAdd) {
            codeH.append(_eol + "#define " + extraDefine);
        }

        codeH.append(_eol
                + comment("Generate type resolution code for "
                        + getContainer().getFullName()));
        codeC.append(_eol
                + comment("Generate type resolution code for "
                        + getContainer().getFullName()));

        // Include the constantsBlock at the top so that sharedBlocks from
        // actors can use true and false etc.  StringMatches needs this.
        CodeStream sharedStream = new CodeStream(
                "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/c/SharedCode.c",
                this);
        sharedStream.appendCodeBlock("constantsBlock");
        codeH.append(sharedStream.toString());

        HashSet<String> functions = _getReferencedFunctions();

        HashSet<String> types = _getTypeIDToUsed(_getNewTypesUsed(functions));

        String[] typesArray = new String[types.size()];
        types.toArray(typesArray);

        CodeStream[] typeStreams = new CodeStream[types.size()];

        codeH.append(_eol + "typedef struct Actor Actor;");
        codeH.append(_eol + "typedef struct CompositeActor CompositeActor;");
        codeH.append(_eol + "typedef struct IOPort IOPort;");
        codeH.append(_eol + "typedef struct Receiver Receiver;");
        codeH.append(_eol + "typedef struct Director Director;");
        codeH.append(_eol + "typedef double Time;" + _eol);

        // Generate type map.
        StringBuffer typeMembers = new StringBuffer();
        codeH.append("#define TYPE_Token -1 " + _eol);
        codeH.append("#define TYPE_Unknown -1 " + _eol);
        if (!types.contains("Scalar")) {
            codeH.append("#define TYPE_Scalar -1 " + _eol);
        }

        for (int i = 0; i < typesArray.length; i++) {

            // We have to define a new structure
            if (typesArray[i].endsWith("Structure")) {
                // Open the .c file for each structure.
                typeStreams[i] = new CodeStream(
                        "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/c/structures/"
                                + typesArray[i].substring(0,
                                        typesArray[i].indexOf("Structure"))
                                        + ".c", this);
            } else {
                // Open the .c file for each type.
                typeStreams[i] = new CodeStream(
                        "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/c/type/"
                                + typesArray[i] + ".c", this);

                codeH.append("#define TYPE_" + typesArray[i] + " " + i + _eol);

                // Dynamically generate all the types within the union.
                typeMembers.append(typesArray[i] + "Token " + typesArray[i]
                        + ";");
                if (i < typesArray.length - 1) {
                    typeMembers.append(_eol);
                }
            }
        }

        String[] functionsArray = new String[functions.size()];
        functions.toArray(functionsArray);

        // True if we have a delete function that needs to return a Token
        boolean defineEmptyToken = false;

        // Generate function map.
        // We don't generate functions for the new functions. because new takes
        // different arguments than the other functions
        int offset = 0; // set to 1 when we see the new function
        for (int i = 0; i < functionsArray.length; i++) {
            if (functionsArray[i].equals("new")) {
                offset = 1;
            } else {
                codeH.append("#define FUNC_" + functionsArray[i] + " "
                        + (i - offset) + _eol);
                if (functionsArray[i].equals("delete")) {
                    defineEmptyToken = true;
                }
            }
        }

        codeH.append("typedef struct token Token;" + _eol);

        // Generate type and function definitions.
        for (int i = 0; i < typesArray.length; i++) {
            // The "declareBlock" contains all necessary declarations for the
            // type; thus, it is always read into the code stream when
            // accessing this particular type.
            typeStreams[i].appendCodeBlock("declareBlock");
            codeH.append(typeStreams[i].toString());
        }

        ArrayList<String> args = new ArrayList<String>();
        ArrayList<String> functionsDeclared = new ArrayList<String>();
        // Token declareBlock.
        if (typeMembers.length() != 0) {
            sharedStream.clear();
            args.add(typeMembers.toString());
            sharedStream.appendCodeBlock("tokenDeclareBlock", args);

            if (defineEmptyToken) {
                sharedStream.append("Token emptyToken; "
                        + comment("Used by *_delete() and others.") + _eol);
            }
            codeH.append(sharedStream.toString());
        }

        // Set to true if we need the unsupportedFunction() method.
        boolean defineUnsupportedTypeFunctionMethod = false;

        // Set to true if we need to scalarDelete() method.
        boolean defineScalarDeleteMethod = false;

        // Define the generic convert method
        StringBuffer convertImplementation = new StringBuffer(_eol
                + "Token* convert(Token* t, char type) {" + _eol);
        convertImplementation.append("if (t->type == type)" + _eol
                + "return t;" + _eol);
        String convertDeclaration = _eol
                + "Token* convert(Token* t, char type);" + _eol;
        codeH.append(convertDeclaration);

        // Append functions that are specified used by this type (without
        // going through the function table).
        for (int i = 0; i < typesArray.length; i++) {
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("funcDeclareBlock");
            codeH.append(typeStreams[i].toString());
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("funcImplementationBlock", true);
            codeC.append(typeStreams[i].toString());
            convertImplementation.append(_eol + "if (type == TYPE_"
                    + typesArray[i] + ") {");
            convertImplementation.append(_eol + "return " + typesArray[i]
                    + "_convert(t);" + _eol + "}");
        }
        convertImplementation.append(_eol + "return NULL;" + _eol + "}" + _eol);
        codeC.append(convertImplementation.toString());

        // FIXME: in the future we need to load the convertPrimitivesBlock
        // dynamically, and maybe break it into multiple blocks to minimize
        // code size.
        sharedStream.clear();
        sharedStream.appendCodeBlock("convertPrimitivesBlockDeclaration");
        codeH.append(sharedStream.toString());
        sharedStream.clear();
        sharedStream.appendCodeBlock("convertPrimitivesBlockImplementation");
        codeC.append(sharedStream.toString());

        sharedStream.clear();

        StringBuffer typeFunctionCode = new StringBuffer();
        for (int i = 0; i < typesArray.length; i++) {
            typeStreams[i].clear();
            //typeStreams[i].appendCodeBlock(typesArray[i] + "_new");

            // Appends the Structure code in case of a structure declaration
            if (typesArray[i].endsWith("Structure")) {
                //typeStreams[i].appendCodeBlock("funcImplementationBlock");
                //codeC.append(typeStreams[i].toString());
                continue;
            }

            for (String element : functionsArray) {
                // The code block declaration has to follow this convention:
                // /*** [function name]Block ***/
                //     .....
                // /**/
                String functionName = typesArray[i] + "_" + element;

                try {
                    // Boolean_isCloseTo and String_isCloseTo map to
                    // Boolean_equals and String_equals.
                    if (element.equals("isCloseTo")
                            && (typesArray[i].equals("Boolean") || typesArray[i]
                                    .equals("String"))) {

                        if (!functions.contains("equals")) {
                            markFunctionCalled(typesArray[i] + "_equals", null);
                        }
                    } else {
                        if (!_unsupportedTypeFunctions.contains(functionName)
                                && !_overloadedFunctionSet
                                .contains(functionName)) {

                            markFunctionCalled(functionName, null);
                        }
                    }
                } catch (IllegalActionException ex) {
                    // We have to catch the exception if some code blocks are
                    // not found. We have to define the function label in the
                    // generated code because the function table makes
                    // reference to this label.

                    typeStreams[i].append("#define " + functionName
                            + " MISSING " + _eol);
                    _unsupportedTypeFunctions.add(functionName);

                    System.out
                    .println("Warning -- missing function defintion: "
                            + functionName + "()");

                    // It is ok because this polymorphic function may not be
                    // supported by all types.
                }
            }
            typeFunctionCode.append(processCode(typeStreams[i].toString()));
        }

        // Append type-polymorphic functions included in the function table.
        for (int i = 0; i < types.size(); i++) {
            // The "funcDeclareBlock" contains all function declarations for
            // the type.
            if (typesArray[i].endsWith("Structure")) {
                continue;
            }
            for (int j = 0; j < functionsArray.length; j++) {
                String functionName = typesArray[i] + "_" + functionsArray[j];

                if (_unsupportedTypeFunctions.contains(functionName)) {
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
                        args.clear();
                        functionName = typesArray[i] + "_equals";
                        if (!_unsupportedTypeFunctions.contains(functionName)) {
                            args.add(functionName);
                            functionsDeclared.add(functionName);
                            sharedStream.appendCodeBlock("funcHeaderBlock",
                                    args);
                        }
                    }
                }
                if (!_scalarDeleteTypes.contains(typesArray[i])
                        || !functionsArray[j].equals("delete")) {
                    // Skip Boolean_delete etc.
                    args.clear();
                    if (!_unsupportedTypeFunctions.contains(functionName)
                            && !functionName.endsWith("_new")) {
                        args.add(functionName);
                        functionsDeclared.add(functionName);
                        sharedStream.append("// functionHeader: " + _eol);
                        sharedStream.appendCodeBlock("funcHeaderBlock", args);
                    }
                }
            }
        }

        // We have to declare all the functions which have not been declared before.
        /*for (String functionName : _overloadedFunctionSet) {
                if (!functionsDeclared.contains(functionName)
                                && !functionName.endsWith("_new")
                                && !functionName.endsWith("_delete")) {
                        args.clear();
                        //args.add(functionName);
                        _overloadedFunctionsDeclaration =
                        sharedStream.append("// functionHeader: " + _eol);
                        sharedStream.appendCodeBlock(functionName, args);
                }
        }*/

        // sharedStream should the global code (e.g. token declaration,
        // constants, and etc.)
        codeH.append(sharedStream.toString());

        sharedStream.clear();
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
        codeC.append(sharedStream.toString());
        sharedStream.clear();
        if (defineUnsupportedTypeFunctionMethod) {
            // Some type/function combos are not supported, so we
            // save space by defining only one method.
            sharedStream.appendCodeBlock("unsupportedTypeFunctionDeclaration");
        }

        if (defineScalarDeleteMethod) {
            // Types that share the scalarDelete() method, which does nothing.
            // We use one method so as to reduce code size.
            sharedStream.appendCodeBlock("scalarDeleteFunctionDeclaration");
        }
        codeH.append(sharedStream.toString());

        // _overloadedFunctions contains the set of functions:
        // add_Type1_Type2, negate_Type, and etc.
        codeC.append(_overloadedFunctions.toString());
        codeH.append(_overloadedFunctionsDeclaration.toString());

        // Generate function type and token table.
        codeC.append(generateFunctionTable(typesArray, functionsArray));

        codeH.append("#define NUM_TYPE " + typesArray.length + _eol);
        codeH.append("#define NUM_FUNC " + functionsArray.length + _eol);

        codeH.append("extern Token* (*functionTable[NUM_TYPE][NUM_FUNC])"
                + "(Token*, ...);" + _eol);

        // typeFunction contains the set of function:
        // Type_new(), Type_delete(), and etc.
        codeH.append(typeFunctionCode);

        String[] result = new String[2];
        result[0] = processCode(codeH.toString());
        result[1] = processCode(codeC.toString());
        return result;
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
            Iterator<Parameter> modifiedVariables = _modifiedVariables
                    .iterator();
            while (modifiedVariables.hasNext()) {
                // SetVariable needs this to be a Variable, not a Parameter.
                Variable variable = modifiedVariables.next();

                NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(variable
                        .getContainer());
                code.append("" + adapter.targetType(variable.getType()) + " "
                        + generateVariableName(variable) + ";" + _eol);
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

        // Generate variable initialization for modified variables.
        if (_modifiedVariables != null && !_modifiedVariables.isEmpty()) {
            code.append(comment(1, "Generate variable initialization for "
                    + "modified parameters"));
            Iterator<Parameter> modifiedVariables = _modifiedVariables
                    .iterator();
            while (modifiedVariables.hasNext()) {
                // SetVariable needs this to be a Variable, not a Parameter.
                Variable variable = modifiedVariables.next();

                //NamedObj container = variable.getContainer();
                //NamedProgramCodeGeneratorAdapter containerAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(container);
                String variableName = variable.getValueAsString();
                if (variable.getType() instanceof StringType) {
                    variableName = "\"" + variableName + "\"";
                }
                code.append(INDENT1 + generateVariableName(variable) + " = "
                        + variableName//containerAdapter.getParameterValue(
                        //variable.getName(), variable.getContainer())
                        + ";" + _eol);
            }
        }

        return code.toString();
    }

    /** Generate the wrapup procedure entry point.
     *  @return a string for the wrapup procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateWrapupEntryCode() throws IllegalActionException {

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            return _eol + _eol + "void wrapup(void) {" + _eol;

            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            String escapeName = _sanitizedModelName.replaceAll("_", "_1");
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
            + escapeName + "_wrapup(" + "JNIEnv *env, jobject obj) {"
            + _eol;
        }
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

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            return _eol + "void wrapup(void);" + _eol;

            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            String escapeName = _sanitizedModelName.replaceAll("_", "_1");
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
            + escapeName + "_wrapup(" + "JNIEnv *env, jobject obj);"
            + _eol;
        }
    }

    /** Generate the model name.
     *  @return a string for the model name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String getModelName() throws IllegalActionException {
        return _sanitizedModelName;
    }

    /** Add called functions to the set of overloaded functions for
     *  later use.
     *  If the function starts with "Array_", add everything after the
     *  "Array_" is added to the set of token functions used.
     *  @param name The name of the function, for example "Double_equals"
     *  @param templateParser The corresponding templateParser that contains the
     *  codeBlock.
     *  @exception IllegalActionException If there is a problem adding
     *  a function to the set of overloaded functions.
     */
    @Override
    public void markFunctionCalled(String name,
            ProceduralTemplateParser templateParser)
                    throws IllegalActionException {

        try {
            String functionCode = _overloadedFunctions.getCodeBlock(name);
            HashSet<String> functions = _getReferencedFunctions();

            boolean macro = false;
            int indexEndDeclaration = functionCode.indexOf('{');
            String declarationFunctionCode = "";
            if (indexEndDeclaration > 0) {
                declarationFunctionCode = functionCode.substring(0,
                        indexEndDeclaration) + ";" + _eol;
            } else if (functionCode.startsWith("#define")) {
                // int this case the function is a macro, we have to define it in the types.h !
                declarationFunctionCode = functionCode;
                macro = true;
            }
            if (!_overloadedFunctionSet.contains(name)) {

                String code = templateParser == null ? processCode(functionCode)
                        : templateParser.processCode(functionCode);
                String declarationCode = templateParser == null ? processCode(declarationFunctionCode)
                        : templateParser.processCode(declarationFunctionCode);

                if (!macro) {
                    _overloadedFunctions.append(code);
                }

                boolean ok = true;
                for (String partName : functions) {
                    if (name.endsWith(partName) || name.endsWith("_new")
                            || name.endsWith("_equals")) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    _overloadedFunctionsDeclaration.append(declarationCode);
                }

                _overloadedFunctionSet.add(name);
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

    /** Split a long function body into multiple functions.
     *  @param linesPerMethod The number of lines that should go into
     *  each method.
     *  @param prefix The prefix to use when naming functions that
     *  are created
     *  @param code The method body to be split.
     *  @return An array of two Strings, where the first element
     *  is the new definitions (if any), and the second element
     *  is the new body.  If the number of lines in the code parameter
     *  is less than linesPerMethod, then the first element will be
     *  the empty string and the second element will be the value of
     *  the code parameter.
     *  @exception IOException If thrown will reading the code.
     */
    @Override
    public String[] splitLongBody(int linesPerMethod, String prefix, String code)
            throws IOException {
        BufferedReader bufferedReader = null;
        StringBuffer bodies = new StringBuffer();
        StringBuffer masterBody = new StringBuffer();

        try {
            bufferedReader = new BufferedReader(new StringReader(code));
            String line;
            int methodNumber = 0;
            int lineNumber = 0;
            StringBuffer body = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                String methodName = prefix + "_" + methodNumber++;
                body = new StringBuffer(line + _eol);
                for (int i = 0; i + 1 < linesPerMethod && line != null; i++) {
                    lineNumber++;
                    line = bufferedReader.readLine();
                    if (line != null) {
                        body.append(line + _eol);
                    }
                }

                bodies.append("void " + methodName + "(void) {" + _eol
                        + body.toString() + "}" + _eol);
                masterBody.append(methodName + "();" + _eol);
            }
            if (lineNumber < linesPerMethod) {
                // We must have less than linesPerMethod lines in the body
                bodies = new StringBuffer();
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

        String[] results = { bodies.toString(), masterBody.toString() };
        return results;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add include directories specified by the actors in this model.
     *  Each element in the Set of include directories has "-I" prepended
     *  and then {@link #addInclude(String)} is called.
     *  @param adapter The adapter that has the include directories.
     *  @exception IllegalActionException If thrown when getting an actor's
     *   include directories.
     *  @see ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter#getIncludeDirectories()
     */
    protected void _addActorIncludeDirectories(
            NamedProgramCodeGeneratorAdapter adapter)
                    throws IllegalActionException {

        Set<String> actorIncludeDirectories = adapter.getIncludeDirectories();
        Iterator<String> includeIterator = actorIncludeDirectories.iterator();
        while (includeIterator.hasNext()) {
            addInclude("-I\"" + includeIterator.next() + "\"");
        }
    }

    /** Add libraries specified by the actors in this model.
     *  @param adapter The adapter that has library directories.
     *  @exception IllegalActionException If thrown when getting an actor's
     *   libraries.
     */
    protected void _addActorLibraries(NamedProgramCodeGeneratorAdapter adapter)
            throws IllegalActionException {
        Set<String> actorLibraryDirectories = adapter.getLibraryDirectories();
        Iterator<String> libraryDirectoryIterator = actorLibraryDirectories
                .iterator();
        while (libraryDirectoryIterator.hasNext()) {
            addLibrary("-L\"" + libraryDirectoryIterator.next() + "\"");
        }

        Set<String> actorLibraries = adapter.getLibraries();
        Iterator<String> librariesIterator = actorLibraries.iterator();
        while (librariesIterator.hasNext()) {
            addLibrary("-l\"" + librariesIterator.next() + "\"");
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
        _overloadedFunctionSet = new LinkedHashSet<String>();

        String cCodegenPath = "$CLASSPATH/ptolemy/cg/kernel/generic/program/procedural/c/";
        String typeDir = cCodegenPath + "type/";
        String functionDir = typeDir + "polymorphic/";

        _overloadedFunctionsDeclaration = new CodeStream(functionDir, this);

        _overloadedFunctions = new CodeStream(functionDir + "add.c", this);
        _overloadedFunctions.parse(functionDir + "clone.c");
        _overloadedFunctions.parse(functionDir + "convert.c");
        _overloadedFunctions.parse(functionDir + "delete.c");
        _overloadedFunctions.parse(functionDir + "divide.c");
        _overloadedFunctions.parse(functionDir + "equals.c");
        _overloadedFunctions.parse(functionDir + "modulo.c");
        _overloadedFunctions.parse(functionDir + "multiply.c");
        _overloadedFunctions.parse(functionDir + "negate.c");
        _overloadedFunctions.parse(functionDir + "print.c");
        _overloadedFunctions.parse(functionDir + "subtract.c");
        _overloadedFunctions.parse(functionDir + "toString.c");
        _overloadedFunctions.parse(functionDir + "zero.c");
        _overloadedFunctions.parse(functionDir + "one.c");

        _overloadedFunctions.parse(typeDir + "Array.c");
        _overloadedFunctions.parse(typeDir + "Boolean.c");
        _overloadedFunctions.parse(typeDir + "BooleanArray.c");
        _overloadedFunctions.parse(typeDir + "Complex.c");
        _overloadedFunctions.parse(typeDir + "Double.c");
        _overloadedFunctions.parse(typeDir + "DoubleArray.c");
        _overloadedFunctions.parse(typeDir + "Int.c");
        _overloadedFunctions.parse(typeDir + "IntArray.c");
        _overloadedFunctions.parse(typeDir + "Long.c");
        _overloadedFunctions.parse(typeDir + "Matrix.c");
        _overloadedFunctions.parse(typeDir + "Pointer.c");
        _overloadedFunctions.parse(typeDir + "Record.c");
        _overloadedFunctions.parse(typeDir + "Scalar.c");
        _overloadedFunctions.parse(typeDir + "String.c");
        _overloadedFunctions.parse(typeDir + "StringArray.c");
        _overloadedFunctions.parse(typeDir + "UnsignedByte.c");

        // Useful for debugging
        //         Iterator codeBlockNames = _overloadedFunctions.getAllCodeBlockNames().iterator();
        //         while (codeBlockNames.hasNext()) {
        //            System.out.println("code block: " + codeBlockNames.next());
        //         }

        // Parse other function files.
        //        String directorFunctionDir = cCodegenPath
        //                + "kernel/parameterized/directorFunctions/";
        //        _overloadedFunctions.parse(directorFunctionDir + "PNDirector.c");
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

        code = super._finalPassOverCode(code);

        if (((BooleanToken) sourceLineBinding.getToken()).booleanValue()) {

            String filename = _getOutputFilename();
            //filename = new java.io.File(filename).getAbsolutePath().replace('\\', '/');

            // Make sure all #line macros are at the start of a line.
            String codeString = code.toString().replaceAll("#line",
                    _eol + "#line");

            StringTokenizer tokenizer = new StringTokenizer(codeString, _eol);

            code = new StringBuffer();

            String lastLine = null;
            if (tokenizer.hasMoreTokens()) {
                lastLine = tokenizer.nextToken();
            }

            for (int i = 2; tokenizer.hasMoreTokens();) {
                String line = tokenizer.nextToken();
                if (lastLine.trim().length() == 0) {
                    lastLine = line;
                } else if (line.trim().length() == 0) {
                    // Get another line.
                } else {
                    if (lastLine.trim().startsWith("#line")) {
                        if (!line.trim().startsWith("#line")) {
                            code.append(lastLine + _eol);
                            i++;
                        }
                    } else {
                        code.append(lastLine + _eol);
                        i++;

                        if (!line.trim().startsWith("#line")) {
                            code.append("#line " + i++ + " \"" + filename
                                    + "\"" + _eol);
                        }
                    }
                    lastLine = line;
                }
            }

            if (lastLine != null && lastLine.trim().length() != 0) {
                code.append(lastLine + _eol);
            }
        }

        return new StringBuffer(TemplateParser.unescapeName(code.toString()));
    }

    /** Generate code and append it to the given string buffer.
     *  Write the code to the directory specified by the codeDirectory
     *  parameter.  The file name is a sanitized version of the model
     *  name with a suffix that is based on last package name of the
     *  <i>generatorPackage</i> parameter.  Thus if the
     *  <i>codeDirectory</i> is <code>$HOME</code>, the name of the
     *  model is <code>Foo</code> and the <i>generatorPackage</i>
     *  is <code>ptolemy.codegen.c</code>, then the file that is
     *  written will be <code>$HOME/Foo.c</code>
     *  This method is the main entry point.
     *  We do not call the super method because it is too different from it.
     *  For instance, there is not only one file generated, but a few.
     *
     *  The code generation algorithm works as followed :
     *  We generate a file with the name of the model (+ .c) and its header
     *  file. In this file we have the implementation of a ptolemy manager in
     *  C ({@link ptolemy.actor.Manager}.
     *
     *  Also for, each Composite Actor (including the top level container)
     *  We generate the files implementing the behavior of the director and
     *  of all the actors. (sources files are in the src directory and header
     *  files in includes directory)
     *  Moreover, for each folder a makefile is generated.
     *
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    @Override
    protected int _generateCode(StringBuffer code) throws KernelException {
        // Record the current time so that we can monitor performance of the
        // code generator by printing messages whenever any part of the code
        // generation process takes more than 10 seconds.
        long startTime = new Date().getTime();
        long overallStartTime = startTime;

        StringBuffer codeMainH = new StringBuffer();
        StringBuffer codeTypesH = new StringBuffer();
        StringBuffer codeTypesC = new StringBuffer();

        _reset();

        _sanitizedModelName = CodeGeneratorAdapter.generateName(_model);

        // Analyze type conversions that may be needed.
        // This must be called before any code is generated.
        _analyzeTypeConversions();

        // Report time consumed if appropriate.
        startTime = _printTimeAndMemory(startTime,
                "CCodeGenerator.analyzeTypeConvert() consumed: ");

        // Create the needed directories
        String directory = codeDirectory.stringValue();
        if (!directory.endsWith("/")) {
            directory += "/";
        }

        String directoryCommons = directory + "commons/";

        if ((!_isTopLevel() && ((BooleanToken) generateEmbeddedCode.getToken())
                .booleanValue())) {
            System.out.println("Not deleting " + directory
                    + ", there might be .class files that we just generated.");
        } else {
            System.out.println("CCodeGenerator._generateCode(): Deleting "
                    + directory);
            if (!FileUtilities.deleteDirectory(directory)) {
                throw new IllegalActionException(this, "Failed to delete \""
                        + directory + "\"");
            }
        }

        // add the includes to the makefile
        if (!_includes.contains("-I " + directoryCommons)) {
            _includes.add("-I " + directoryCommons);
        }

        if (new File(directoryCommons).mkdirs()) {
            // Findbugs wants that
            directory += "";
        }

        /////////////////////////////////////////////////////////////////////

        CompositeActor container = (CompositeActor) getContainer();
        String sanitizedNameContainer = CodeGeneratorAdapter
                .generateName(container);
        _generateAndWriteCompositeActorCode(container, "");

        /////////////////////////////////////////////////////////////////////

        // Generate shared code.  Some adapter optionally add methods
        // to the shared code block, so we generate the shared code as
        // late as possible.  However, we have to generateSharedCode()
        // before generateTypeConvertCode() so that any polymorphic
        // codegen token methods used in the shared code are recorded.  See
        // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/arrayType18.xml
        String sharedCode = _generateSharedCode();
        String declareSharedCode = _generateDeclareSharedCode();

        // generate type resolution code has to be after
        // fire(), wrapup(), preinit(), init()...
        String[] typeResolutionCode = generateTypeConvertCodeCandH();

        startTime = _printTimeAndMemory(startTime,
                "CCodeGenerator: generating code consumed: ");

        // The appending phase.
        code.append(generateCopyright());
        codeMainH.append(generateCopyright());
        codeTypesH.append(generateCopyright());
        codeTypesC.append(generateCopyright());

        // appending the .h multi-inclusions protection macro
        codeMainH.append("#ifndef NO_"
                + _sanitizedModelName.toUpperCase(Locale.getDefault())
                + "_MAIN_H" + _eol + "#define NO_"
                + _sanitizedModelName.toUpperCase(Locale.getDefault())
                + "_MAIN_H" + _eol);

        //////////////////////////////////////////////////////////////
        // Writing the Manager (main) file                          //
        //////////////////////////////////////////////////////////////

        // Header file declaration

        codeMainH
        .append(_eol + "#include \"" + sanitizedNameContainer + ".h\"");
        code.append(_eol + "#include \"" + _sanitizedModelName + "_Main.h\"");
        if (((BooleanToken) measureTime.getToken()).booleanValue()) {
            code.append(_eol + "#include <sys/time.h>");
            code.append(_eol + "#include <unistd.h>");
        }

        // Main entry point function
        code.append(generateMainEntryCode());

        code.append(generateMainExitCode());

        // If the container is not in the top level, we are generating code
        // for the Java and C co-simulation.
        if (!_isTopLevel()) {
            codeMainH.append(_eol + "static jobjectArray input;" + _eol
                    + "static jobjectArray tokensToAllOutputPorts;" + _eol
                    + "static JNIEnv* env;" + _eol + "static jobject obj;"
                    + _eol);
        }

        // Preinitialize function
        codeMainH.append(_eol + "static int iterationCount;");
        codeMainH.append(_eol + "void preinitialize();");
        code.append(_eol + "void preinitialize() {");
        code.append(_eol + "emptyToken.type = -1;");
        code.append(_eol + "iterationCount = 0;");
        code.append(_eol + sanitizedNameContainer + "_New();");
        code.append(_eol + "(*(" + sanitizedNameContainer
                + "->preinitialize))(" + sanitizedNameContainer + ");");
        code.append(_eol + "}");

        // Initialize function
        codeMainH.append(generateInitializeProcedureName());
        code.append(generateInitializeEntryCode());
        code.append(_eol + "preinitialize();");
        code.append(_eol + "(*(" + sanitizedNameContainer + "->initialize))("
                + sanitizedNameContainer + ");");
        code.append(generateInitializeExitCode());

        // Iterate function
        codeMainH.append(_eol + "boolean iterate();");
        code.append(_eol + "boolean iterate() {");
        code.append(_eol + "boolean result = true;");
        code.append(_eol + "iterationCount++;");
        code.append(_eol + "if ((*(" + sanitizedNameContainer + "->prefire))("
                + sanitizedNameContainer + ")) {");
        code.append(_eol + "(*(" + sanitizedNameContainer + "->fire))("
                + sanitizedNameContainer + ");");
        code.append(_eol + "result = (*(" + sanitizedNameContainer
                + "->postfire))(" + sanitizedNameContainer + ");");
        code.append(_eol + "}");
        code.append(_eol + "return result;");
        code.append(_eol + "}");

        // wrapup function
        codeMainH.append(generateWrapupProcedureName());
        code.append(generateWrapupEntryCode());
        code.append(_eol + "(*(" + sanitizedNameContainer + "->wrapup))("
                + sanitizedNameContainer + ");");
        code.append(generateWrapupExitCode());

        // Closing the ifndef in the main header
        codeMainH.append(_eol + "#endif");

        //////////////////////////////////////////////////
        // Appending the code for the types files       //
        //////////////////////////////////////////////////

        // appending the .h multi-inclusions protection macro
        codeTypesH.append("#ifndef NO_"
                + _sanitizedModelName.toUpperCase(Locale.getDefault())
                + "_TYPES_H" + _eol + "#define NO_"
                + _sanitizedModelName.toUpperCase(Locale.getDefault())
                + "_TYPES_H" + _eol);

        // Get any include or import lines needed by the variable declarations.
        codeTypesH.append(_eol + typeResolutionCode[0]);
        typeResolutionCode[0] = null;

        codeTypesH.append(comment("end typeResolution code"));
        codeTypesC.append(typeResolutionCode[1]);
        typeResolutionCode[1] = null;
        codeTypesC.append(comment("end typeResolution code"));
        codeTypesC.append(sharedCode);
        sharedCode = null;
        codeTypesH.append(declareSharedCode);
        declareSharedCode = null;
        // Don't use **** in comments, it causes the nightly build to
        // report errors.
        codeTypesH.append(comment("end shared code"));
        codeTypesH.append("#endif");

        startTime = _printTimeAndMemory(startTime,
                "CCodeGenerator: appending code consumed: ");

        //////////////////////////////////////////////////
        // final pass                                   //
        //////////////////////////////////////////////////

        code = _finalPassOverCode(code);
        codeMainH = _finalPassOverCode(codeMainH);
        codeTypesH = _finalPassOverCode(codeTypesH);
        codeTypesC = _finalPassOverCode(codeTypesC);
        startTime = _printTimeAndMemory(startTime,
                "CCodeGenerator: final pass consumed: ");

        //////////////////////////////////////////////////
        // Writing files                                //
        //////////////////////////////////////////////////

        _writeCodeFileName(code, _sanitizedModelName + "_Main.c", true, false);
        _writeCodeFileName(codeMainH, _sanitizedModelName + "_Main.h", true,
                false);
        _writeCodeFileName(codeTypesH, directoryCommons + "_ptTypes.h", true,
                false);
        _writeCodeFileName(codeTypesC, directoryCommons + "_ptTypes.c", true,
                false);

        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_AtomicActor.h");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_AtomicActor.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_Actor.h");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_Actor.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_CompositeActor.h");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_CompositeActor.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_IOPort.h");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_IOPort.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_TypedIOPort.h");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_TypedIOPort.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_Receiver.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_Receiver.h");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_Director.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_Director.h");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_LocalClock.c");
        _copyCFileTosrc(
                "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/",
                directoryCommons, "_LocalClock.h");

        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pbl.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pbl.h");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblCollection.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblhash.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblHeap.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblIterator.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblList.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblMap.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblPriorityQueue.c");
        _copyCFileTosrc(
                "ptolemy/cg/kernel/generic/program/procedural/c/structures/",
                directoryCommons, "pblSet.c");

        code = null;
        codeMainH = null;
        codeTypesH = null;
        codeTypesC = null;

        if (_executeCommands == null) {
            _executeCommands = new StreamExec();
        }

        // Writing the Makefile
        _writeMakefile(container, directory);

        /*startTime =*/_printTimeAndMemory(startTime,
                "CCodeGenerator: writing code consumed: ");

        //////////////////////////////////////////
        // Executing commands                   //
        //////////////////////////////////////////

        _printTimeAndMemory(overallStartTime,
                "CCodeGenerator: All phases above consumed: ");

        return _executeCommands();
    }

    /** Generate the code for a constructor.
     *  @param actor The actor for which constructor code shall be generated.
     *  @return The constructor code.
     *  @exception IllegalActionException If there is a problem
     *  generating the code.
     */
    protected String _generateConstructorCode(Actor actor)
            throws IllegalActionException {
        StringBuffer result = new StringBuffer(_eol);
        String sanitizedActorName = CodeGeneratorAdapter
                .generateName((NamedObj) actor);
        result.append("struct AtomicActor* " + sanitizedActorName
                + " = AtomicActor_New();" + _eol);
        CompositeActor container = (CompositeActor) actor.getContainer();
        while (!container.isOpaque()) {
            container = (CompositeActor) container.getContainer();
        }

        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);
        result.append(sanitizedActorName + "->container = "
                + sanitizedContainerName + ";" + _eol);
        result.append(sanitizedActorName + "->preinitialize = "
                + sanitizedActorName + "_preinitialize;" + _eol);
        result.append(sanitizedActorName + "->initialize = "
                + sanitizedActorName + "_initialize;" + _eol);
        result.append(sanitizedActorName + "->prefire = " + sanitizedActorName
                + "_prefire;" + _eol);
        result.append(sanitizedActorName + "->fire = " + sanitizedActorName
                + "_fire;" + _eol);
        result.append(sanitizedActorName + "->postfire = " + sanitizedActorName
                + "_postfire;" + _eol);
        result.append(sanitizedActorName + "->wrapup = " + sanitizedActorName
                + "_wrapup;" + _eol);

        Iterator<?> inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort iPort = (TypedIOPort) inputPorts.next();
            ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort portAdapter = (ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort) getAdapter(iPort);
            result.append(portAdapter.generatePortDeclaration() + _eol);
        }

        Iterator<?> outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            TypedIOPort oPort = (TypedIOPort) outputPorts.next();
            ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort portAdapter = (ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort) getAdapter(oPort);
            result.append(portAdapter.generatePortDeclaration() + _eol);
        }

        result.append("return " + sanitizedActorName + ";" + _eol);

        return result.toString();
    }

    /** Generate the code for a constructor.
     *  @param actor The Composite actor for which constructor code
     *  shall be generated.
     *  @return The constructor code.
     *  @exception IllegalActionException If there is a problem
     *  generating the code.
     */
    protected String _generateConstructorCode(CompositeActor actor)
            throws IllegalActionException {
        StringBuffer result = new StringBuffer(_eol);
        String sanitizedActorName = CodeGeneratorAdapter.generateName(actor);
        result.append(_eol + sanitizedActorName + " = CompositeActor_New();"
                + _eol);
        // Only set the container if the container of the actor is non null
        // and we are not generating embedded code.

        System.out
                .println("CCodeGenerator set the container? : actor.getContainer(): "
                        + actor.getContainer()
                        + " _isTopLevel: "
                        + _isTopLevel()
                        + " generateEmbeddedCode: "
                        + ((BooleanToken) generateEmbeddedCode.getToken())
                                .booleanValue());

        // Checking for being at the top level caused problems with:
        // $PTII/bin/ptcg -language c -generateInSubdirectory false -inline false -maximumLinesPerBlock 2500 -variablesAsArrays false ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/lib/test/auto/Microstep.xml

        if (actor.getContainer() != null
        // && (!_isTopLevel() &&  !((BooleanToken) generateEmbeddedCode.getToken()).booleanValue())
                && !((BooleanToken) generateEmbeddedCode.getToken())
                        .booleanValue()) {
            CompositeActor container = (CompositeActor) actor.getContainer();
            while (!container.isOpaque()) {
                container = (CompositeActor) container.getContainer();
            }
            result.append(_eol + sanitizedActorName + "->container = "
                    + CodeGeneratorAdapter.generateName(actor.getContainer())
                    + ";" + _eol);
        }

        result.append(_eol + comment("Creation of the director") + _eol);
        String typeDirector = actor.getDirector().getClass().getSimpleName();
        String directorName = CodeGeneratorAdapter.generateName(actor
                .getDirector());
        result.append("struct " + typeDirector + "* " + directorName + " = "
                + typeDirector + "_New();" + _eol);
        result.append(sanitizedActorName + "->_director = (struct Director*)"
                + directorName + ";" + _eol);

        result.append(_eol + comment("Actor Initializations") + _eol);
        List<?> actorList = actor.deepEntityList();
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            NamedObj act = (NamedObj) actors.next();
            if (CodeGeneratorAdapter.generateName(act).contains("Controller")) {
                continue;
            }
            if (act instanceof AtomicActor) {
                result.append(CodeGeneratorAdapter.generateName(act) + " = "
                        + CodeGeneratorAdapter.generateName(act) + "_New();"
                        + _eol);
            } else if (act instanceof CompositeEntity) {
                result.append(CodeGeneratorAdapter.generateName(act) + " = "
                        + CodeGeneratorAdapter.generateName(act) + "_New();"
                        + _eol);
            } else {
                throw new IllegalActionException(actor,
                        "Unsupported type of Actor : " + act.getFullName());
            }
            result.append("pblListAdd(" + sanitizedActorName
                    + "->_containedEntities, "
                    + CodeGeneratorAdapter.generateName(act) + ");" + _eol);
        }

        result.append(_eol + comment("Creation of the ports of the container")
                + _eol);
        Iterator<?> inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort iPort = (TypedIOPort) inputPorts.next();
            ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort portAdapter = (ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort) getAdapter(iPort);
            result.append(portAdapter.generatePortDeclaration() + _eol);
        }

        Iterator<?> outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            TypedIOPort oPort = (TypedIOPort) outputPorts.next();
            ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort portAdapter = (ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.IOPort) getAdapter(oPort);
            result.append(portAdapter.generatePortDeclaration() + _eol);
        }

        /* Initialization of the director */
        ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter = (ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) getAdapter(actor
                .getDirector());
        result.append(directorAdapter.generateConstructorCode());

        result.append(_eol + "return " + sanitizedActorName + ";" + _eol);

        return result.toString();
    }

    /** Generate the code to access ports for an actor.
     *  @param actor The actor for which code shall be generated.
     *  @return The port accessor code.
     */
    protected String _generatePortsAccessorsCode(Actor actor) {
        String sanitizedActorName = CodeGeneratorAdapter
                .generateName((NamedObj) actor);
        StringBuffer result = new StringBuffer();

        List<?> portList = actor.inputPortList();
        portList.addAll(actor.outputPortList());
        for (Object o : portList) {
            IOPort port = (IOPort) o;
            if (!port.isOutsideConnected()) {
                continue;
            }
            result.append("struct IOPort* " + sanitizedActorName + "_get_"
                    + port.getName() + "() {" + _eol);
            result.append("return (struct IOPort*) " + port.getName() + ";"
                    + _eol);
            result.append("}" + _eol);
        }

        return result.toString();
    }

    /** Generate the code to access ports for a composite actor.
     *  @param actor The actor for which code shall be generated.
     *  @return The port accessor code.
     */
    protected String _generatePortsAccessorsCode(CompositeActor actor) {
        String sanitizedActorName = CodeGeneratorAdapter.generateName(actor);
        StringBuffer result = new StringBuffer();

        List<?> portList = actor.portList();
        for (Object o : portList) {
            IOPort port = (IOPort) o;
            result.append("struct IOPort* " + sanitizedActorName + "_get_"
                    + port.getName() + "() {" + _eol);
            result.append("return (struct IOPort*) " + port.getName() + ";"
                    + _eol);
            result.append("}" + _eol);
        }

        return result.toString();
    }

    /** Generate the code to declare the accessors to ports.
     *  @param actor The actor for which code shall be generated.
     *  @return The port accessor declaration code.
     */
    protected String _generatePortsAccessorsDeclaration(Actor actor) {
        String sanitizedActorName = CodeGeneratorAdapter
                .generateName((NamedObj) actor);
        StringBuffer result = new StringBuffer();

        List<?> portList = actor.inputPortList();
        portList.addAll(actor.outputPortList());
        for (Object o : portList) {
            IOPort port = (IOPort) o;
            if (!port.isOutsideConnected()) {
                continue;
            }
            result.append("struct IOPort* " + sanitizedActorName + "_get_"
                    + port.getName() + "();" + _eol);
        }

        return result.toString();
    }

    /** Generate the code to declare the accessors to ports.
     *  @param actor The composite actor for which code shall be generated.
     *  @return The port accessor declaration code.
     */
    protected String _generatePortsAccessorsDeclaration(CompositeActor actor) {
        String sanitizedActorName = CodeGeneratorAdapter.generateName(actor);
        StringBuffer result = new StringBuffer();

        List<?> portList = actor.portList();
        for (Object o : portList) {
            IOPort port = (IOPort) o;
            result.append("struct IOPort* " + sanitizedActorName + "_get_"
                    + port.getName() + "();" + _eol);
        }

        return result.toString();
    }

    /** Generate and write the code for an actor.
     *  This method is called by the container actor
     *
     *  There are 2 phases in this method :
     *  First we generate the code for the actor
     *  Then we write the code
     *
     *  @param actor The actor that needs to be generated
     *  @param directorAdapter The adapter of the director.  This
     *  adapter is used to generate variable declarations for the
     *  director.
     *  @param container Not used by this method.
     *  @param directory The directory path of the sources files
     *  @exception IllegalActionException If anything goes wrong during the generation.
     */
    protected void _generateAndWriteActorCode(NamedObj actor,
            NamedProgramCodeGeneratorAdapter directorAdapter,
            CompositeEntity container, String directory)
                    throws IllegalActionException {
        /////////////////////////////////////////////
        // Initialization of the actor             //
        /////////////////////////////////////////////

        NamedProgramCodeGeneratorAdapter actorAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(actor);
        String sanitizedActorName = CodeGeneratorAdapter.generateName(actor);

        // Add include directories and libraries specified by actors.
        // TODO : modify those methods to take the actor into account
        _addActorIncludeDirectories(actorAdapter);
        _addActorLibraries(actorAdapter);

        actorAdapter.setupAdapter();

        /////////////////////////////////////////////
        // Generation of the code of the actor     //
        /////////////////////////////////////////////

        StringBuffer actorDefinition = new StringBuffer(_eol);

        if (actor instanceof ptolemy.cg.lib.CompiledCompositeActor) {
            actorDefinition
            .append(_eol + "JNIEnv *env;" + _eol + "jobject obj");
            Iterator<?> inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                actorDefinition.append(";" + _eol + "jobjectArray "
                        + inputPort.getName() + "");
            }
            actorDefinition.append(";" + _eol);
            actorDefinition.append("jobjectArray tokensToAllOutputPorts;"
                    + _eol);
        }

        // Generate the declaration of the ports
        StringBuffer portsDefinition = new StringBuffer(_eol);
        Iterator<?> inputPorts = ((Actor) actor).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            if (!inputPort.isOutsideConnected()) {
                continue;
            }
            portsDefinition.append(_eol + "static struct TypedIOPort* "
                    + inputPort.getName() + ";");
        }
        Iterator<?> outputPorts = ((Actor) actor).outputPortList().iterator();
        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
            if (!outputPort.isOutsideConnected()) {
                continue;
            }
            portsDefinition.append(_eol + "static struct TypedIOPort* "
                    + outputPort.getName() + ";");
        }

        _modifiedVariables = actorAdapter.getModifiedVariables();

        // Generate the contructor of the atomic actor
        String constructorEntryCode = _eol + "struct AtomicActor* "
                + sanitizedActorName + "_New() {" + _eol;
        String constructorCode = _generateConstructorCode((Actor) actor);
        String constructorExitCode = _eol + "}" + _eol;
        String constructorDeclarationCode = _eol + "struct AtomicActor* "
                + sanitizedActorName + "_New();";

        // Generate the accessors to the ports
        String portAccessors = _generatePortsAccessorsCode((Actor) actor);
        String portAccessorsDeclaration = _generatePortsAccessorsDeclaration((Actor) actor);

        // Generate the preinitialization of the actor actor
        String preinitializeMethodEntryCode = _eol
                + "void "
                + sanitizedActorName
                + "_preinitialize(struct AtomicActor* actor) {"
                + _eol
                + comment("Preinitalization of the actor : "
                        + sanitizedActorName);
        String preinitializeMethodBodyCode = actorAdapter
                .generatePreinitializeMethodBodyCode();
        String preinitializeMethodExitCode = _eol
                + "}"
                + comment("End of the preinitalization of the actor : "
                        + sanitizedActorName);
        String preinitializeDeclarationName = _eol + "void "
                + sanitizedActorName
                + "_preinitialize(struct AtomicActor* actor);";

        // Generate the variable declaration for this actor
        String variableDeclarationCode = actorAdapter
                .generatePreinitializeCode();

        // Generate the initialization of the actor actor
        String initializeMethodEntryCode = _eol + "void " + sanitizedActorName
                + "_initialize(struct AtomicActor* actor) {" + _eol
                + comment("Initalization of the actor : " + sanitizedActorName);
        String initializeMethodBodyCode = actorAdapter.generateInitializeCode();
        String initializeMethodExitCode = _eol
                + "}"
                + comment("End of the initalization of the actor : "
                        + sanitizedActorName);
        String initializeDeclarationName = _eol + "void " + sanitizedActorName
                + "_initialize(struct AtomicActor* actor);";

        // Generate the prefire code of the actor actor
        String prefireMethodEntryCode = _eol + "bool " + sanitizedActorName
                + "_prefire(struct AtomicActor* actor) {" + _eol
                + comment("prefire of the actor : " + sanitizedActorName);
        String prefireMethodBodyCode = actorAdapter.generatePrefireCode();
        prefireMethodBodyCode += _eol + "return true;" + _eol;
        String prefireMethodExitCode = _eol
                + "}"
                + comment("End of the prefire of the actor : "
                        + sanitizedActorName);
        String prefireDeclarationName = _eol + "bool " + sanitizedActorName
                + "_prefire(struct AtomicActor* actor);";

        // Generate the fire code of the actor actor
        String fireMethodBodyCode = actorAdapter.generateFireFunctionCode();
        String fireDeclarationName = _eol + "void " + sanitizedActorName
                + "_fire(struct AtomicActor* actor);";

        // Generate the postfire code of the actor actor
        String postfireMethodEntryCode = _eol + "bool " + sanitizedActorName
                + "_postfire(struct AtomicActor* actor) {" + _eol
                + comment("Postfire of the actor : " + sanitizedActorName);
        String postfireMethodBodyCode = actorAdapter.generatePostfireCode();
        postfireMethodBodyCode += _eol + "return true;" + _eol;
        String postfireMethodExitCode = _eol
                + "}"
                + comment("End of the postfire of the actor : "
                        + sanitizedActorName);
        String postfireDeclarationName = _eol + "bool " + sanitizedActorName
                + "_postfire(struct AtomicActor* actor);";

        // Generate the wrapup code of the actor actor
        String wrapupMethodEntryCode = _eol + "void " + sanitizedActorName
                + "_wrapup(struct AtomicActor* actor) {" + _eol
                + comment("Wrapup of the actor : " + sanitizedActorName);
        String wrapupMethodBodyCode = actorAdapter.generateWrapupCode();
        String wrapupMethodExitCode = _eol
                + "}"
                + comment("End of the wrapup of the actor : "
                        + sanitizedActorName);
        String wrapupDeclarationName = _eol + "void " + sanitizedActorName
                + "_wrapup(struct AtomicActor* actor);";

        // Generate the variable declaration for this actor
        if (directorAdapter instanceof ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) {
            variableDeclarationCode += _eol
                    + ((ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) directorAdapter)
                    .generateVariableDeclaration(actorAdapter);
            variableDeclarationCode += _eol + generateVariableDeclaration();
            preinitializeMethodBodyCode += _eol
                    + ((ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) directorAdapter)
                    .generateVariableInitialization(actorAdapter);
            preinitializeMethodBodyCode += _eol
                    + generateVariableInitialization();
        }

        String includeFiles = _generateIncludeFiles(actorAdapter);

        ///////////////////////////////////////
        // Writing the actor files       //
        ///////////////////////////////////////

        StringBuffer codeContainerC = new StringBuffer();
        StringBuffer codeContainerH = new StringBuffer();

        // The appending phase.

        // appending the .h multi-inclusions protection macro
        codeContainerH.append("#ifndef NO_"
                + sanitizedActorName.toUpperCase(Locale.getDefault()) + "_H"
                + _eol + "#define NO_"
                + sanitizedActorName.toUpperCase(Locale.getDefault()) + "_H"
                + _eol);
        codeContainerH.append(generateCopyright());
        codeContainerC.append(generateCopyright());

        codeContainerC.append(_eol + "#ifdef __cplusplus" + _eol
                + "extern \"C\" {" + _eol + "#endif" + _eol + _eol);

        // Appending the includes
        codeContainerC.append("#include \"" + sanitizedActorName + ".h\""
                + _eol);

        codeContainerC.append(_eol + "#ifdef __cplusplus" + _eol + "}" + _eol
                + "#endif" + _eol + _eol);

        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);
        codeContainerH.append("#include \"_ptTypes.h\"" + _eol);
        codeContainerH.append("#include \"_AtomicActor.h\"" + _eol);
        codeContainerH.append("#include \"_TypedIOPort.h\"" + _eol);
        // FIXME : not a good way to do it
        if (((CompositeActor) container).getDirector() instanceof DEDirector) {
            codeContainerH.append("#include \"_DEReceiver.h\"" + _eol);
        } else if (((CompositeActor) container).getDirector() instanceof SDFDirector) {
            codeContainerH.append("#include \"_SDFReceiver.h\"" + _eol);
        }
        codeContainerH.append("#include \"" + sanitizedContainerName + ".h\""
                + _eol);
        codeContainerH.append(includeFiles);

        // Free up space as we go.
        includeFiles = null;

        codeContainerH.append(_eol + "#ifdef __cplusplus" + _eol
                + "extern \"C\" {" + _eol + "#endif" + _eol + _eol);

        // Appending the name of the actor
        codeContainerH.append(comment("Actor declaration"));
        codeContainerH.append(constructorDeclarationCode);
        codeContainerH.append(portAccessorsDeclaration);
        codeContainerH.append(actorDefinition.toString());
        codeContainerH.append(comment("end actor declaration"));

        // Appending the variable declaration
        codeContainerC.append(comment("Variable declaration code"));
        codeContainerC.append(variableDeclarationCode);
        codeContainerC.append(comment("end variable declaration code"));

        codeContainerC.append(_eol + "#ifdef __cplusplus" + _eol
                + "extern \"C\" {" + _eol + "#endif" + _eol + _eol);

        variableDeclarationCode = null;

        // Appending the ports declaration
        codeContainerC.append(portsDefinition);
        portsDefinition = null;

        codeContainerC.append(preinitializeDeclarationName);
        codeContainerC.append(initializeDeclarationName);
        codeContainerC.append(prefireDeclarationName);
        codeContainerC.append(fireDeclarationName);
        codeContainerC.append(postfireDeclarationName);
        codeContainerC.append(wrapupDeclarationName);

        // Appending the constructor code
        codeContainerC.append(constructorEntryCode);
        codeContainerC.append(constructorCode);
        codeContainerC.append(constructorExitCode);
        constructorEntryCode = null;
        constructorCode = null;
        constructorExitCode = null;

        // Appending the accessors code
        codeContainerC.append(portAccessors);
        portAccessors = null;

        // Appending the preinitialization code
        codeContainerC.append(preinitializeMethodEntryCode);
        codeContainerC.append(preinitializeMethodBodyCode);
        codeContainerC.append(preinitializeMethodExitCode);
        preinitializeMethodEntryCode = null;
        preinitializeMethodBodyCode = null;
        preinitializeMethodExitCode = null;

        // Appending the initialization code
        codeContainerC.append(initializeMethodEntryCode);
        codeContainerC.append(initializeMethodBodyCode);
        codeContainerC.append(initializeMethodExitCode);
        initializeMethodEntryCode = null;
        initializeMethodBodyCode = null;
        initializeMethodExitCode = null;

        // Appending the prefire code
        codeContainerC.append(prefireMethodEntryCode);
        codeContainerC.append(prefireMethodBodyCode);
        codeContainerC.append(prefireMethodExitCode);
        prefireMethodEntryCode = null;
        prefireMethodBodyCode = null;
        prefireMethodExitCode = null;

        codeContainerC.append(_eol + "#ifdef __cplusplus" + _eol + "}" + _eol
                + "#endif" + _eol + _eol);

        // Appending the fire code
        codeContainerC.append(fireMethodBodyCode);
        fireMethodBodyCode = null;

        codeContainerC.append(_eol + "#ifdef __cplusplus" + _eol
                + "extern \"C\" {" + _eol + "#endif" + _eol + _eol);

        // Appending the postfire code
        codeContainerC.append(postfireMethodEntryCode);
        codeContainerC.append(postfireMethodBodyCode);
        codeContainerC.append(postfireMethodExitCode);
        postfireMethodEntryCode = null;
        postfireMethodBodyCode = null;
        postfireMethodExitCode = null;

        // Appending the wrapup code
        codeContainerC.append(wrapupMethodEntryCode);
        codeContainerC.append(wrapupMethodBodyCode);
        codeContainerC.append(wrapupMethodExitCode);
        wrapupMethodEntryCode = null;
        wrapupMethodBodyCode = null;
        wrapupMethodExitCode = null;

        codeContainerC.append(_eol + "#ifdef __cplusplus" + _eol + "}" + _eol
                + "#endif" + _eol + _eol);

        codeContainerH.append(_eol + "#ifdef __cplusplus" + _eol + "}" + _eol
                + "#endif" + _eol + _eol);

        // Closing the ifdef
        codeContainerH
                .append(_eol + "#endif /* "
                        + sanitizedActorName.toUpperCase(Locale.getDefault())
                        + "_H */");

        // Final pass on the code
        codeContainerH = _finalPassOverCode(codeContainerH);
        codeContainerC = _finalPassOverCode(codeContainerC);

        // Writing the code in the files
        _writeCodeFileName(codeContainerH, directory + "/" + sanitizedActorName
                + ".h", true, false);
        _writeCodeFileName(codeContainerC, directory + "/" + sanitizedActorName
                + ".c", true, false);

        // freeing memory
        codeContainerH = null;
        codeContainerC = null;
    }

    /** Generate and write the code for a composite actor.
     *  This method is called recursively, for any composite actor present
     *  in the model.
     *  There are 5 phases in this method :
     *  First we initialize the parameters for the container
     *  Then we generate and write the code for the container
     *  Then we call the generation for each contained actor
     *  Then we call the generation for the director
     *  Finally we generate the makefile corresponding
     *
     *  @param container The actor that needs to be generated
     *  @param containerDirectory A string describing where to write the files
     *  @exception IllegalActionException If anything goes wrong during the generation.
     */
    protected void _generateAndWriteCompositeActorCode(
            CompositeEntity container, String containerDirectory)
                    throws IllegalActionException {

        /////////////////////////////////////////////
        // Initialization of the container         //
        /////////////////////////////////////////////

        Director director = null;
        if (container instanceof CompositeActor) {
            director = ((CompositeActor) container).getDirector();
        } else {
            throw new IllegalActionException(container,
                    "Unsupported type of Actor : " + container.getFullName());
        }

        // This attribute should be false, but not in the case of a compiled composite actor.
        Attribute generateEmbeddedCode = getAttribute("generateEmbeddedCode");
        if (generateEmbeddedCode instanceof Parameter) {
            ((Parameter) generateEmbeddedCode).setExpression("false");
        }
        if (container instanceof ptolemy.cg.lib.CompiledCompositeActor) {
            //            ((ptolemy.cg.lib.CompiledCompositeActor) container).initialize();
            //            _sanitizedModelName = ((ptolemy.cg.lib.CompiledCompositeActor) container)
            //                    .getSanitizedName();
            if (generateEmbeddedCode instanceof Parameter) {
                ((Parameter) generateEmbeddedCode).setExpression("true");
            }
        }

        // Check the inline value
        boolean inlineValue = ((BooleanToken) inline.getToken()).booleanValue();
        if (inlineValue && director != null && director instanceof DEDirector) {
            inline.setExpression("false");
            System.out
                    .println("inline was set to true, which is not relevant to a DE model, "
                            + "so it is being reset to false.");
        }

        // Create the needed directories
        String directory = codeDirectory.stringValue();

        // FIXME : maybe escape some chararcters here
        String sanitizedContainerName = container.getName();
        containerDirectory += "/_" + sanitizedContainerName; //container.getFullName();
        sanitizedContainerName = CodeGeneratorAdapter.generateName(container);

        directory = directory + containerDirectory;

        if (new File(directory).mkdirs()) {
            //Findbugs wants that
            directory += "";
        }

        // add the includes to the makefile
        if (!_includes.contains("-I " + directory)) {
            _includes.add("-I " + directory);
        }

        //////////////////////////////////////////////////
        // Generation and writing of the container code //
        //////////////////////////////////////////////////

        // Generate the declaration of the ports
        StringBuffer portsDefinition = new StringBuffer(_eol);
        Iterator<?> inputPorts = ((Actor) container).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
            portsDefinition.append(_eol + "static struct TypedIOPort* "
                    + inputPort.getName() + ";");
        }
        Iterator<?> outputPorts = ((Actor) container).outputPortList()
                .iterator();
        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
            portsDefinition.append(_eol + "static struct TypedIOPort* "
                    + outputPort.getName() + ";");
        }

        StringBuffer CCode = new StringBuffer(generateCopyright() + _eol);
        CCode.append("#include \"" + sanitizedContainerName + ".h\"" + _eol);

        CCode.append(portsDefinition.toString());

        // FindBugs says that we don't have to check the instance here
        // because we do it earlier.
        String actorType = "CompositeActor";
        //String actorType;
        //if (container instanceof CompositeActor) {
        //actorType = "CompositeActor";
        //} else {
        //throw new IllegalActionException(container,
        //           "CompositeEntity non supported yet :"
        //+ container.getClassName());
        //}

        CCode.append("struct " + actorType + "* " + sanitizedContainerName
                + ";" + _eol);

        CCode.append(portsDefinition.toString());
        CCode.append(_eol + "struct " + actorType + "* "
                + sanitizedContainerName + "_New() {" + _eol);

        CCode.append(_generateConstructorCode((CompositeActor) container)
                + _eol);

        CCode.append("}" + _eol);

        StringBuffer HCode = new StringBuffer(generateCopyright() + _eol);
        HCode.append("#ifndef _"
                + sanitizedContainerName.toUpperCase(Locale.getDefault())
                + "_H_" + _eol + "#define _"
                + sanitizedContainerName.toUpperCase(Locale.getDefault())
                + "_H_" + _eol);

        HCode.append("#include \"_" + actorType + ".h\"" + _eol);
        HCode.append("#include \"_AtomicActor.h\"" + _eol);
        if (director == null) {
            throw new IllegalActionException(container, "The container \""
                    + container.getFullName() + "\" does not have a director.");
        }
        String directorType = director.getClass().getSimpleName();
        HCode.append("#include \"_" + directorType + ".h\"" + _eol);
        if (((CompositeActor) container).getExecutiveDirector() != null) {
            directorType = ((CompositeActor) container).getExecutiveDirector()
                    .getClass().getSimpleName();
            HCode.append("#include \"_" + directorType + ".h\"" + _eol);
        }

        // Only include a .h file for the container if we are not at
        // the top level and we are not generating embedded code.

        System.out
                .println("CCodeGenerator include the container .h? : container.getContainer(): "
                        + container.getContainer()
                        + " _isTopLevel: "
                        + _isTopLevel()
                        + " generateEmbeddedCode: "
                        + ((BooleanToken) ((Parameter) generateEmbeddedCode)
                                .getToken()).booleanValue());

        // Checking for being at the top level caused problems with:
        // $PTII/bin/ptcg -language c -generateInSubdirectory false -inline false -maximumLinesPerBlock 2500 -variablesAsArrays false ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/lib/test/auto/Microstep.xml

        if (container.getContainer() != null
        //&& !_isTopLevel() &&  !((BooleanToken) ((Parameter)generateEmbeddedCode).getToken()).booleanValue()
                && !((BooleanToken) ((Parameter) generateEmbeddedCode)
                        .getToken()).booleanValue()) {
            HCode.append("#include \""
                    + CodeGeneratorAdapter.generateName(container
                            .getContainer()) + ".h\"" + _eol);
        }

        for (Object containedActorO : container.deepEntityList()) {
            NamedObj containedActor = (NamedObj) containedActorO;
            if (CodeGeneratorAdapter.generateName(containedActor).contains(
                    "Controller")) {
                continue;
            }
            HCode.append("#include \""
                    + CodeGeneratorAdapter.generateName(containedActor)
                    + ".h\"" + _eol);
        }

        HCode.append(_eol + "#ifdef __cplusplus" + _eol + "extern \"C\" {"
                + _eol + "#endif" + _eol + _eol);

        HCode.append("extern struct " + actorType + "* "
                + sanitizedContainerName + ";" + _eol);
        HCode.append("extern struct " + actorType + "* "
                + sanitizedContainerName + "_New();" + _eol);
        if (((CompositeActor) container).getDirector() instanceof SDFDirector) {
            HCode.append(_eol + "void " + sanitizedContainerName
                    + "_Schedule_iterate();" + _eol);
        } else if (((CompositeActor) container).getDirector() instanceof FSMDirector) {
            HCode.append(_eol + "void " + sanitizedContainerName
                    + "_transferModalInputs(PblMap* mapTokensIn);" + _eol);
            HCode.append(_eol + "void " + sanitizedContainerName
                    + "_transferModalOutputs(PblMap* mapTokensOut);" + _eol);
            HCode.append(_eol + "void " + sanitizedContainerName
                    + "_makeTransitions(struct FSMDirector* director);" + _eol);
        }

        for (Object containedActorO : container.deepEntityList()) {
            NamedObj containedActor = (NamedObj) containedActorO;
            if (CodeGeneratorAdapter.generateName(containedActor).contains(
                    "Controller")) {
                continue;
            }
            if (containedActor instanceof AtomicActor
                    || containedActor instanceof FSMActor) {
                HCode.append("struct AtomicActor* "
                        + CodeGeneratorAdapter.generateName(containedActor)
                        + ";" + _eol);
            } else if (containedActor instanceof CompositeActor) {
                HCode.append("struct CompositeActor* "
                        + CodeGeneratorAdapter.generateName(containedActor)
                        + ";" + _eol);
            }
        }

        // Generate the accessors to the ports
        String portAccessors = _generatePortsAccessorsCode((CompositeActor) container);
        String portAccessorsDeclaration = _generatePortsAccessorsDeclaration((CompositeActor) container);
        HCode.append(portAccessorsDeclaration);
        CCode.append(portAccessors);

        HCode.append(_eol + "#ifdef __cplusplus" + _eol + "}" + _eol + "#endif"
                + _eol + _eol);

        HCode.append("#endif /* "
                + sanitizedContainerName.toUpperCase(Locale.getDefault())
                + " */" + _eol);

        // Final pass on the code
        HCode = _finalPassOverCode(HCode);
        CCode = _finalPassOverCode(CCode);

        // Writing the code in the files
        _writeCodeFileName(HCode, directory + "/" + sanitizedContainerName
                + ".h", true, false);
        _writeCodeFileName(CCode, directory + "/" + sanitizedContainerName
                + ".c", true, false);

        // freeing memory
        HCode = null;
        CCode = null;

        if (((CompositeActor) container).getDirector() instanceof SDFDirector) {
            CCode = new StringBuffer();
            ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel.SDFDirector directorAdapter = (ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel.SDFDirector) getAdapter(((CompositeActor) container)
                    .getDirector());

            CCode.append("#include \"" + sanitizedContainerName + ".h\"");

            CCode.append(_eol + "void " + sanitizedContainerName
                    + "_Schedule_iterate() {");
            CCode.append(_eol + directorAdapter.generateSchedule());
            CCode.append(_eol + "}");

            _writeCodeFileName(CCode, directory + "/" + sanitizedContainerName
                    + "_SDFSchedule.c", true, false);

            CCode = null;
        } else if (((CompositeActor) container).getDirector() instanceof FSMDirector) {
            CCode = new StringBuffer();
            ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.kernel.FSMDirector directorAdapter = (ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.kernel.FSMDirector) getAdapter(((CompositeActor) container)
                    .getDirector());

            CCode.append("#include \"" + sanitizedContainerName + ".h\"");

            // Variables declaration
            CCode.append(_eol + directorAdapter.generateVariableDeclaration());

            // Transfer inputs to modal model function
            CCode.append(_eol + "void " + sanitizedContainerName
                    + "_transferModalInputs(PblMap* mapTokensIn) {");
            CCode.append(_eol + directorAdapter.generateTransferInputCode());
            CCode.append(_eol + "}");

            // Transfer outputs to modal model function
            CCode.append(_eol + "void " + sanitizedContainerName
                    + "_transferModalOutputs(PblMap* mapTokensOut) {");
            CCode.append(_eol + directorAdapter.generateTransferOutputCode());
            CCode.append(_eol + "}");

            // Transition code for the modal model
            CCode.append(_eol + "void " + sanitizedContainerName
                    + "_makeTransitions(struct FSMDirector* director) {");
            CCode.append(_eol + directorAdapter.generateFireFunctionCode());
            CCode.append(_eol + "}");

            _writeCodeFileName(CCode, directory + "/" + sanitizedContainerName
                    + "_FSMTransitionCode.c", true, false);

            CCode = null;
        }

        ////////////////////////////////////
        // End of the writing of the code //
        ////////////////////////////////////

        String directoryC = codeDirectory.stringValue();
        if (!directoryC.endsWith("/")) {
            directoryC += "/";
        }
        String directoryCommons = directoryC + "commons/";

        if (((CompositeActor) container).getDirector() instanceof PtidesDirector) {
            // in order to mark the Record_new function
            processCode("$new(Record(0.0,0,0));");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesReceiver.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesReceiver.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesDirector.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesDirector.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesPlatformDirector.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesPlatformDirector.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesEvent.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesEvent.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesPort.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesPort.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesMirrorPort.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/",
                    directoryCommons, "_PtidesMirrorPort.c");
            _definesToAdd.add("PTIDESDIRECTOR 11");
            _definesToAdd.add("PTIDESPLATFORMDIRECTOR 3");

        } else if (((CompositeActor) container).getDirector() instanceof DEDirector) {
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_CalendarQueue.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_CalendarQueue.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_DEEvent.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_DEEvent.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_DEReceiver.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_DEReceiver.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_DEDirector.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/de/kernel/",
                    directoryCommons, "_DEDirector.c");
            _definesToAdd.add("DEDIRECTOR 1");
        } else if (((CompositeActor) container).getDirector() instanceof SDFDirector) {
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/sdf/kernel/",
                    directoryCommons, "_SDFReceiver.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/sdf/kernel/",
                    directoryCommons, "_SDFReceiver.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/sdf/kernel/",
                    directoryCommons, "_SDFDirector.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/sdf/kernel/",
                    directoryCommons, "_SDFDirector.c");
            _definesToAdd.add("SDFDIRECTOR 2");
        } else if (((CompositeActor) container).getDirector() instanceof FSMDirector) {
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal/kernel/",
                    directoryCommons, "_FSMReceiver.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal/kernel/",
                    directoryCommons, "_FSMReceiver.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal/kernel/",
                    directoryCommons, "_FSMDirector.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal/kernel/",
                    directoryCommons, "_FSMDirector.c");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal/kernel/",
                    directoryCommons, "_FSMActor.h");
            _copyCFileTosrc(
                    "ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal/kernel/",
                    directoryCommons, "_FSMActor.c");
            _definesToAdd.add("FSMDIRECTOR 4");
        }
        // Generation and writing of all the contained actors
        // This function calls itself when the actor is composite
        List actorList = container.deepEntityList();
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();

            // The recursive call
            if (actor instanceof ModalController) {
                continue;
            } else if (actor instanceof AtomicActor
                    || actor instanceof FSMActor) {
                _generateAndWriteActorCode(
                        actor,
                        (NamedProgramCodeGeneratorAdapter) getAdapter(director),
                        container, directory);
            } else if (actor instanceof CompositeEntity) {
                _generateAndWriteCompositeActorCode((CompositeEntity) actor,
                        containerDirectory);
            } else if (actor instanceof State) {
                // For modal model support
                for (TypedActor act : ((State) actor).getRefinement()) {
                    Refinement r = (Refinement) act;
                    _generateAndWriteCompositeActorCode(r, containerDirectory);
                }
            } else {
                throw new IllegalActionException(container,
                        "Unsupported type of Actor : " + actor.getFullName());
            }
        }

        // Generate the director code
        //        _generateAndWriteDirectorCode(director, directoryIncludes, directorySrc);

        //        // Writing the Makefile
        //        if (_isTopLevel())
        //            _writeMakefile(container, directory);
        //        _writeMakefile(container, directorySrc);
    }

    //    /** Generate and write the code for a director within
    //     *  a container.
    //     *  This method is called by the container actor
    //     *
    //     *  @param director The director that needs to be generated.
    //     *  @param includesDirectory The directory path of the includes files
    //     *  @param srcDirectory The directory path of the sources files
    //     *  @exception IllegalActionException If anything goes wrong during the generation.
    //     */
    //    protected void _generateAndWriteDirectorCode(Director director, String includesDirectory, String srcDirectory)
    //            throws IllegalActionException {
    //        // This is an error case
    //        // Transparent actors should be treated at upper level
    //        if (director == null)
    //            throw new IllegalActionException(getComponent(),
    //                    "Trying to generate code for a null director!");
    //
    //        ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter =
    //                (ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) getAdapter(director);
    //
    //        String sanitizedDirectorName = CodeGeneratorAdapter.generateName(director);
    //
    //        StringBuffer codeDirectorC = new StringBuffer();
    //        StringBuffer codeDirectorH = new StringBuffer();
    //
    //        codeDirectorH.append(generateCopyright());
    //        codeDirectorC.append(generateCopyright());
    //
    //        codeDirectorH.append("#ifndef NO_"
    //                + sanitizedDirectorName.toUpperCase(Locale.getDefault()) + "_H" + _eol
    //                + "#define NO_" + sanitizedDirectorName.toUpperCase(Locale.getDefault()) + "_H"
    //                + _eol);
    //
    //        codeDirectorH.append(_generateIncludeFiles(directorAdapter));
    //        codeDirectorH.append("#include \"" + _sanitizedModelName + "__Director.h\"" + _eol);
    //        codeDirectorH.append("#include \"" + _sanitizedModelName + ".h\"" + _eol);
    //        codeDirectorH.append("#include <stdbool.h>" + _eol);
    //        codeDirectorC.append("#include \"" + sanitizedDirectorName + ".h\"" + _eol);
    //        codeDirectorC.append(_eol + directorAdapter.generateMainLoop());
    //
    //        codeDirectorH.append(_eol + directorAdapter.generateVariableDeclaration());
    //
    //        // FIXME : DE director dependent implement it in the father
    //        if (director instanceof DEDirector)
    //            codeDirectorH.append(_eol +
    //                    ((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.kernel.DEDirector)directorAdapter)
    //                    .generateFunctionsDeclaration());
    //        else if (director instanceof SDFDirector)
    //            codeDirectorH.append(_eol +
    //                    ((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel.SDFDirector)directorAdapter)
    //                    .generateFunctionsDeclaration());
    //        else if (director instanceof FSMDirector)
    //            codeDirectorH.append(_eol +
    //                    ((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.kernel.FSMDirector)directorAdapter)
    //                    .generateFunctionsDeclaration());
    //        codeDirectorH.append(_eol + "#endif");
    //
    //        // Final pass on the code
    //        codeDirectorC = _finalPassOverCode(codeDirectorC);
    //        codeDirectorH = _finalPassOverCode(codeDirectorH);
    //
    //        // Writing the code in the files
    //        _writeCodeFileName(codeDirectorH, includesDirectory + "/" + sanitizedDirectorName + ".h", true,
    //                false);
    //        _writeCodeFileName(codeDirectorC, srcDirectory + "/" + sanitizedDirectorName + ".c", true,
    //                false);
    //
    //        // freeing memory
    //        codeDirectorH = null;
    //        codeDirectorC = null;
    //    }

    /** Generate the body code that lies between variable declaration
     *  and wrapup. This method delegates to the director adapter
     *  to generate a main loop.
     *  Note : We do not call the super method, because this one is too different
     *  @return The generated body code.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    protected String _generateBodyCode() throws IllegalActionException {

        String code = "";

        CompositeEntity model = (CompositeEntity) getContainer();

        // NOTE: The cast is safe because setContainer ensures
        // the container is an Actor.
        ptolemy.actor.Director director = ((Actor) model).getDirector();

        if (director == null) {
            throw new IllegalActionException(model, "Does not have a director.");
        }
        ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director directorAdapter = (ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director) getAdapter(director);

        if (_isTopLevel()) {
            code += directorAdapter.generateMainLoop();
        } else {
            // Generate embedded code.
            NamedProgramCodeGeneratorAdapter compositeAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(model);
            code += compositeAdapter.generateFireCode();
        }

        return code;
    }

    /** Generate the declarations of the code shared by actors,
     *  including globally defined
     *  data struct types and static methods or variables shared by multiple
     *  instances of the same actor type.
     *  @return The shared code of the containing composite actor.
     *  @exception IllegalActionException If an error occurs when generating
     *   the globally shared code, or if the adapter class for the model
     *   director cannot be found, or if an error occurs when the adapter
     *   actor generates the shared code.
     */
    protected String _generateDeclareSharedCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        Set<String> sharedCodeBlocks = adapter.getDeclareSharedCode();
        Iterator<String> blocks = sharedCodeBlocks.iterator();
        while (blocks.hasNext()) {
            String block = blocks.next();
            code.append(block);
        }

        if (code.length() > 0) {
            code.insert(0, _eol
                    + comment("Generate shared code for "
                            + getContainer().getName()));
            code.append(comment("Finished generating shared code for "
                    + getContainer().getName()));
        }

        return code.toString();
    }

    /** Generate include files. FIXME: State what is included.
     *  @param actorAdapter The adapter that has the header files.
     *  @return The #include statements, surrounded by #ifndef to ensure
     *   that the files are included only once.
     *  @exception IllegalActionException If the adapter class for some actor
     *   cannot be found.
     */
    protected String _generateIncludeFiles(
            NamedProgramCodeGeneratorAdapter actorAdapter)
                    throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Set<String> includingFiles = actorAdapter.getHeaderFiles();

        includingFiles.add("<stdlib.h>"); // Sun requires stdlib.h for malloc

        // Only include the jvmHeader files if we are not at the top
        // level and generateEmbeddedCode is true.   Otherwise
        // $PTII/bin/ptcg $PTII/ptolemy/cg/lib/test/auto/ScaleC.xml
        // includes jni.h, which will fail under targets like Arduino.
        if (!_isTopLevel()
                && ((BooleanToken) generateEmbeddedCode.getToken())
                        .booleanValue()) {
            includingFiles.addAll(_getJVMHeaderFiles());
        }

        for (String file : includingFiles) {
            code.append("#include " + file + _eol);
        }

        return code.toString();
    }

    /** Return the prototype for fire functions.
     * @return The string"(void)" so as to avoid the avr-gcc 3.4.6
     * warning: "function declaration isn't a prototype"
     */
    @Override
    protected String _getFireFunctionArguments() {
        return "(struct AtomicActor* actor)";
    }

    /** Generate the code for printing the execution time since
     *  the code generated by _recordStartTime() was called.
     *  @return Return the code for printing the total execution time.
     */
    @Override
    protected String _printExecutionTime() {
        StringBuffer endCode = new StringBuffer();
        endCode.append(super._printExecutionTime());
        endCode.append("clock_gettime(CLOCK_REALTIME, &end);\n"
                + "dT = end.tv_sec - start.tv_sec + (end.tv_nsec - start.tv_nsec) * 1.0e-9;\n"
                + "printf(\"execution time: %g seconds\\n\", dT);\n\n");
        return endCode.toString();
    }

    /** Generate the code for recording the current time.
     *  This writes current time into a timespec struct called "start".
     *  @return Return the code for recording the current time.
     */
    @Override
    protected String _recordStartTime() {
        StringBuffer startCode = new StringBuffer();
        startCode.append(super._recordStartTime());
        startCode.append("struct timespec start, end;\n" + "double dT = 0.0;\n"
                + "clock_gettime(CLOCK_REALTIME, &start);\n\n");
        return startCode.toString();
    }

    /** Return the class of the templateParser class. In cse
     *  there isn't one return null.
     *  @return The base class for templateParser.
     */
    @Override
    protected Class<? extends TemplateParser> _templateParserClass() {
        return CTemplateParser.class;
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
     *  <p>See the parent class
     *  {@link ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator#_writeMakefile(CompositeEntity, String)}
     *  for variable that are substituted by the parent class.</p>
     *
     *  <p>The following variables are substituted:
     *  <code>PTCG_CFILES</code>, <code>PTCG_OFILES</code>,
     *  <code>PTCGCompiler</code>.</p>
     *
     *  @param container The composite actor for which we generate the makefile
     *  @param currentDirectory The director in which the makefile is to be written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    @Override
    protected void _writeMakefile(CompositeEntity container,
            String currentDirectory) throws IllegalActionException {

        if (generatorPackageList.stringValue().contains("arduino")) {
            // FIXME: Temporary hack to set up platform dependent -I
            // directive for the Arduino, see
            // $PTII/ptolemy/cg/adapter/generic/program/procedural/c/arduino/makefile.in
            _substituteMap.put("@ARDUINO_INCLUDES@", _arduinoIncludes());
            _substituteMap.put("@AVR_BASE@", _avrBase());
        } else if (generatorPackageList.stringValue().contains("mbed")) {
            // FIXME: Temporary hack to set up platform dependent -I
            // directive for the mbed, see
            // $PTII/ptolemy/cg/adapter/generic/program/procedural/c/mbed/makefile.in
            _substituteMap.put("@MBED_INCLUDES@", _mbedIncludes());
            _substituteMap.put("@MBED_BASE@", _mbedBase());
            // FIXME: We need a way to include target specific functions.
            _mbedCopy();
        }

        // Adds the .c and .o needed files
        Director director = null;
        if (container instanceof CompositeActor) {
            director = ((CompositeActor) container).getDirector();
        } else if (container instanceof FSMActor) {
            director = ((FSMActor) container).getDirector();
        } else {
            throw new IllegalActionException(container,
                    "Unsupported type of Actor : " + container.getFullName());
        }

        if (director != null && director instanceof DEDirector) {
            StringBuffer ptcgC = new StringBuffer(
                    "$(shell find . -type f -name '*.c')");//"src/types.c src/Actor.c src/CalendarQueue.c src/DEDirector.c src/DEEvent.c src/DEReceiver.c src/IOPort.c";
            StringBuffer ptcgO = new StringBuffer();//"build/types.o build/Actor.o build/CalendarQueue.o build/DEDirector.o build/DEEvent.o build/DEReceiver.o build/IOPort.o";
            if (_actorsToInclude != null) {
                Iterator<String> actors = _actorsToInclude.iterator();
                while (actors.hasNext()) {
                    String actor = actors.next();
                    ptcgC.append(actor + ".c");
                    ptcgO.append(actor + ".o");
                }
            }
            _substituteMap.put("@PTCG_CFILES@", ptcgC.toString());
            _substituteMap.put("@PTCG_OFILES@", ptcgO.toString());
        } else {
            _substituteMap.put("@PTCG_CFILES@",
                    "$(shell find . -type f -name '*.c')");
            _substituteMap.put("@PTCG_OFILES@", "");
        }

        if (((BooleanToken) generateCpp.getToken()).booleanValue()) {
            _substituteMap.put("@PTCGCompiler@", "g++");
        } else {
            _substituteMap.put("@PTCGCompiler@", "gcc");
        }

        _substituteMap.put("@PTCGLibraries@", _concatenateElements(_libraries));

        super._writeMakefile(container, currentDirectory);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the platform dependent -I directive for the Arduino.
     *  See $PTII/ptolemy/cg/adapter/generic/program/procedural/c/arduino/makefile.in
     *  @return the -I directive for the Arduino.
     */
    private String _arduinoIncludes() {
        // FIXME: Temporary hack to set up platform dependent -I directive.
        // The real fix would be to execute "avr-gcc -print-libgcc-file-name"
        // and then search up the path.
        String environmentVariableName = "ARDUINO_INCLUDES_DIRECTORY";
        String arduinoIncludeDirectoryEnvironmentVariable = System
                .getenv(environmentVariableName);
        String[] includeSearchPath = new String[] {
                arduinoIncludeDirectoryEnvironmentVariable,
                "/usr/local/arduino/hardware/arduino/sam/cores/arduino",
                "/usr/local/arduino-1.5.8/hardware/arduino/sam/cores/arduino",
                "/Applications/Arduino.app/Contents/Resources/Java/hardware/arduino/avr/cores/arduino" };
        // If the env is not set, then we don't want to print a null.
        StringBuffer directories = new StringBuffer();
        for (int i = 0; i < includeSearchPath.length; i++) {
            if (includeSearchPath != null
                    && new File(includeSearchPath[i], "Arduino.h").isFile()) {
                if (includeSearchPath[i]
                        .equals(arduinoIncludeDirectoryEnvironmentVariable)) {
                    System.out.println("Using the value defined by "
                            + environmentVariableName + ": "
                            + arduinoIncludeDirectoryEnvironmentVariable);
                }
                return "-I" + includeSearchPath[i];
            }
            directories.append(includeSearchPath[i] + ", ");
        }
        System.err.println("Could not find Arduino.h. Checked "
                + directories.toString().substring(0, directories.length() - 2)
                + ". Try setting the " + environmentVariableName
                + " environment variable and re-running Ptolemy.");
        return "-I/SetThe" + environmentVariableName + "EnvironmentVariable";
    }

    private String _avrBase() {
        String osName = StringUtilities.getProperty("os.name");
        if (osName.startsWith("Mac OS X")) {
            return "/Applications/Arduino.app/Contents/Resources/Java/hardware";
        }
        return "/usr/local/arduino/hardware";
    }

    /** Get the header files needed to compile with the jvm library.
     *  @return A set of strings that are names of the header files
     *   needed by the code generated for jvm library
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    private Set<String> _getJVMHeaderFiles() throws IllegalActionException {
        String javaHome = StringUtilities.getProperty("java.home");

        ExecuteCommands executeCommands = getExecuteCommands();
        if (executeCommands == null) {
            executeCommands = new StreamExec();
        }

        if (!_printedJVMWarning) {
            // We only print this once.
            _printedJVMWarning = true;

            executeCommands.stdout(_eol + _eol
                    + "WARNING: This model uses an actor that "
                    + "links with the jvm library." + _eol
                    + "  To properly run the executable, you must have jvm.dll"
                    + " in your path." + _eol
                    + "  If you do not, then when you run the executable, "
                    + "it will immediately exit" + _eol + "  with no message!"
                    + _eol + "  For example, place " + javaHome
                    + "\\bin\\client" + _eol
                    + "  in your path.  If you are running Vergil from the "
                    + "command line as " + _eol + "  $PTII/bin/ptinvoke, "
                    + "then this has been handled for you." + _eol
                    + "  If you are running via Eclipse, then you must update "
                    + "your path by hand." + _eol + _eol + _eol);
        }

        String jreBinClientPath = javaHome + File.separator + "bin"
                + File.separator + "client";
        executeCommands
        .stdout(_eol + _eol
                + "CCodeGeneratorAdapter: appended to path "
                + jreBinClientPath);

        executeCommands.appendToPath(jreBinClientPath);

        javaHome = javaHome.replace('\\', '/');
        if (javaHome.endsWith("/jre")) {
            javaHome = javaHome.substring(0, javaHome.length() - 4);
        }

        if (!new File(javaHome + "/include").isDirectory()) {
            // It could be that we are running under WebStart
            // or otherwise in a JRE, so we should look for the JDK.
            File potentialJavaHomeParentFile = new File(javaHome)
            .getParentFile();
            // Loop through twice, once with the parent, once with
            // C:/Program Files/Java.  This is lame, but easy
            for (int loop = 2; loop > 0; loop--) {
                // Get all the directories that have include/jni.h under them.
                File[] jdkFiles = potentialJavaHomeParentFile
                        .listFiles(new _JniFileFilter());
                if (jdkFiles != null && jdkFiles.length >= 1) {
                    // Sort and get the last directory, which should
                    // be the most recent JDK.
                    java.util.Arrays.sort(jdkFiles);
                    javaHome = jdkFiles[jdkFiles.length - 1].toString();
                    break;
                } else {
                    // Not found, please try again.
                    // Findbugs warns about this explicit path.
                    potentialJavaHomeParentFile = new File(
                            "C:\\Program Files\\Java");
                }
            }
        }
        if (new File(javaHome + "/include").isDirectory()) {
            addInclude("-I\"" + javaHome + "/include\"");
        } else {
            // Perhaps this is Mac OS X 10.7, where Java is laid out differently?
            File headerDirectory = new File(new File(javaHome).getParentFile(),
                    "Headers");
            if (new File(headerDirectory, "jni.h").exists()) {
                addInclude("-I\"" + headerDirectory + "\"");
            }
        }

        String osName = StringUtilities.getProperty("os.name");
        String platform = "win32";
        if (osName.startsWith("Linux")) {
            platform = "linux";
        } else if (osName.startsWith("SunOS")) {
            platform = "solaris";
        } else if (osName.startsWith("Mac OS X")) {
            platform = "Mac OS X";
        }

        if (platform.equals("Mac OS X")) {
            addInclude("-I\"" + javaHome + "/include/darwin\"");
        }

        String jvmLoaderDirective = "-ljvm";
        String libjvmAbsoluteDirectory = "";
        if (platform.equals("win32")) {
            addInclude("-I\"" + javaHome + "/include/" + platform + "\"");

            // The directive we use to find jvm.dll, which is usually in
            // something like c:/Program Files/Java/jre1.6.0_04/bin/client/jvm.dll
            jvmLoaderDirective = "-ljvm";

            String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir")
                    .replace('\\', '/');
            String libjvmRelativeDirectory = "ptolemy/codegen/c/lib/win";
            libjvmAbsoluteDirectory = ptIIDir + "/" + libjvmRelativeDirectory;
            String libjvmFileName = "libjvm.dll.a";
            String libjvmPath = libjvmAbsoluteDirectory + "/" + libjvmFileName;

            if (!new File(libjvmPath).canRead()) {
                // If we are under WebStart or running from jar files, we
                // will need to copy libjvm.dll.a from the jar file
                // that gcc can find it.
                URL libjvmURL = Thread
                        .currentThread()
                        .getContextClassLoader()
                        .getResource(
                                libjvmRelativeDirectory + "/" + libjvmFileName);
                if (libjvmURL != null) {
                    String libjvmAbsolutePath = null;
                    try {
                        // Look for libjvm.dll.a in the codegen directory
                        File libjvmFileCopy = new File(codeDirectory.asFile(),
                                "libjvm.dll.a");

                        if (!libjvmFileCopy.canRead()) {
                            // Create libjvm.dll.a in the codegen directory
                            FileUtilities.binaryCopyURLToFile(libjvmURL,
                                    libjvmFileCopy);
                        }

                        libjvmAbsolutePath = libjvmFileCopy.getAbsolutePath();
                        if (libjvmFileCopy.canRead()) {
                            libjvmAbsolutePath = libjvmAbsolutePath.replace(
                                    '\\', '/');
                            libjvmAbsoluteDirectory = libjvmAbsolutePath
                                    .substring(0,
                                            libjvmAbsolutePath.lastIndexOf("/"));

                            // Get rid of everything before the last /lib
                            // and the .dll.a
                            jvmLoaderDirective = "-l"
                                    + libjvmAbsolutePath.substring(
                                            libjvmAbsolutePath
                                            .lastIndexOf("/lib") + 4,
                                            libjvmAbsolutePath.length() - 6);

                        }
                    } catch (Throwable throwable) {
                        throw new IllegalActionException(getComponent(),
                                throwable, "Could not copy \"" + libjvmURL
                                + "\" to the file system, path was: \""
                                + libjvmAbsolutePath + "\"");
                    }
                }
            }
        } else if (platform.equals("Mac OS X")) {
            if (javaHome != null) {
                if (new File(javaHome + "/../Libraries").isDirectory()) {
                    // Apple Java, directory layout is sadly different from all other jvms.  (Be Different).
                    libjvmAbsoluteDirectory = javaHome + "/../Libraries";

                    // Why do people who work at Apple insist on changing the
                    // names of things?  Why is jvmlinkage necessary?

                    // We also need to adjust the rpath here so that this works:
                    // $PTII/bin/ptcg -language c $PTII/ptolemy/cg/lib/test/auto/ScaleC.xml
                    jvmLoaderDirective = "-Wl,-rpath,"
                            + libjvmAbsoluteDirectory + " -ljvmlinkage";
                } else {
                    // Oracle Java 1.7
                    addInclude("-I\"" + javaHome + "/include/darwin\"");
                    libjvmAbsoluteDirectory = javaHome + "/jre/lib/server";

                    // We also need to adjust the rpath here so that this works:
                    // $PTII/bin/ptcg -language c $PTII/ptolemy/cg/lib/test/auto/ScaleC.xml
                    jvmLoaderDirective = "-Wl,-rpath,"
                            + libjvmAbsoluteDirectory;
                }
            }

        } else {
            // Solaris, Linux etc.
            addInclude("-I\"" + javaHome + "/include/" + platform + "\"");
        }
        addLibrary("-L\"" + libjvmAbsoluteDirectory + "\"");
        addLibrary(jvmLoaderDirective);

        Set<String> files = new HashSet<String>();
        files.add("<jni.h>");
        return files;
    }

    private HashSet<String> _getTypeIDToUsed(HashSet<String> types) {
        HashSet<String> result = new HashSet<String>();
        result.addAll(types);

        for (String type : types) {
            if (type.endsWith("Array")) {
                String elementType = type.replace("Array", "");
                if (elementType.length() > 0) {
                    result.add(elementType);
                }
            }
        }
        return result;
    }

    private String _mbedBase() {
        return "/usr/local/mbed/gcc-arm-none-eabi";
    }

    /** Copy object files from
     *  $PTII/ptolemy/cg/adapter/generic/program/procedural/c/mbed/lib.
     */
    private void _mbedCopy() throws IllegalActionException {
        // FIXME: We need a way to include target specific functions.

        File cgLibDirectory = new File(codeDirectory.asFile(), "/lib/");
        String mbedLibraryDirectory = "ptolemy/cg/adapter/generic/program/procedural/c/mbed/lib/";
        try {

            if (!cgLibDirectory.mkdirs()) {
                throw new java.io.FileNotFoundException("Could not create \"" + cgLibDirectory + "\"");
            }
            // Copy the .o files for mbed.
            String [] objectFiles = new String [] {
                "MKL25Z4.ld",
                "board.o",
                "cmsis_nvic.o",
                "libmbed.a",
                "mbed_overrides.o",
                "retarget.o",
                "startup_MKL25Z4.o",
                "system_MKL25Z4.o"
            };

            for (String objectFile : objectFiles) {
                _copyCFileTosrc(
                        mbedLibraryDirectory,
                        cgLibDirectory.getCanonicalPath() + "/", objectFile);
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(getComponent(), throwable,
                    "Failed to copy mbed .o files from \""
                    + mbedLibraryDirectory + "\" to \"" + cgLibDirectory + "\".");
        }
    }

    /** Return the platform dependent -I directive for the Mbed.
     *  See $PTII/ptolemy/cg/adapter/generic/program/procedural/c/mbed/makefile.in
     *  @return the -I directive for the Mbed.
     */
    private String _mbedIncludes() {
        // FIXME: Temporary hack to set up platform dependent -I directive.
        // The real fix would be to execute "avr-gcc -print-libgcc-file-name"
        // and then search up the path.
        String environmentVariableName = "MBED_INCLUDES_DIRECTORY";
        String mbedIncludeDirectoryEnvironmentVariable = System
                .getenv(environmentVariableName);
        String[] includeSearchPath = new String[] {
                mbedIncludeDirectoryEnvironmentVariable,
                "/usr/local/mbed/hardware/mbed/sam/cores/mbed",
                "/usr/local/mbed-1.5.8/hardware/mbed/sam/cores/mbed",
                "/Applications/Mbed.app/Contents/Resources/Java/hardware/mbed/avr/cores/mbed" };
        // If the env is not set, then we don't want to print a null.
        StringBuffer directories = new StringBuffer();
        for (int i = 0; i < includeSearchPath.length; i++) {
            if (includeSearchPath != null
                    && new File(includeSearchPath[i], "Mbed.h").isFile()) {
                if (includeSearchPath[i]
                        .equals(mbedIncludeDirectoryEnvironmentVariable)) {
                    System.out.println("Using the value defined by "
                            + environmentVariableName + ": "
                            + mbedIncludeDirectoryEnvironmentVariable);
                }
                return "-I" + includeSearchPath[i];
            }
            directories.append(includeSearchPath[i] + ", ");
        }
        System.err.println("Could not find Mbed.h. Checked "
                + directories.toString().substring(0, directories.length() - 2)
                + ". Try setting the " + environmentVariableName
                + " environment variable and re-running Ptolemy.");
        return "-I/SetThe" + environmentVariableName + "EnvironmentVariable";
    }

    /** Process the specified code for the adapter associated with the
     *  container.  Replace macros with their values.
     *  @param code The code to process.
     *  @return The processed code.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    private String processCode(String code) throws IllegalActionException {
        ProgramCodeGeneratorAdapter adapter = (ProgramCodeGeneratorAdapter) getAdapter(getContainer());
        return adapter.processCode(code);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the set of referenced functions.
     * @return The set of referenced functions.
     */
    private HashSet<String> _getReferencedFunctions() {
        // Determine the total number of referenced polymorphic functions.
        HashSet<String> functions = new HashSet<String>();
        functions.add("delete");
        //functions.add("toString");    // for debugging.
        functions.add("convert");
        functions.add("isCloseTo");
        functions.addAll(_typeFuncUsed);
        functions.addAll(_tokenFuncUsed);

        // If the code generator is invoked with "-verbosity N" and N is greater than 0,
        // then debugging is enabled.
        if (_debugging) {
            System.out.println("CCodeGenerator: all referenced functions: "
                    + functions);
        }
        return functions;
    }

    /** Return the new types used by the given set of functions.
     * @param functions The set of functions used, such as "equals",
     * "isCloseTo", and "toString".
     * @return The new types used.
     */
    private HashSet<String> _getNewTypesUsed(HashSet<String> functions) {
        // Determine the total number of referenced types.
        HashSet<String> types = new HashSet<String>();
        if (functions.contains("equals") || functions.contains("isCloseTo")) {
            types.add("Boolean");
        }

        if (functions.contains("toString")) {
            types.add("String");
        }

        if (functions.contains("isCloseTo")
                //&& _newTypesUsed.contains("Int")
                //&& !_newTypesUsed.contains("Double")
                ) {
            // FIXME: we should not need Double for Int_isCloseTo()
            types.add("Double");
        }

        types.addAll(_newTypesUsed);

        // If the code generator is invoked with "-verbosity N" and N is greater than 0,
        // then debugging is enabled.
        if (_debugging) {
            _debug("CCodeGenerator: all referenced types: " + types);
        }

        return types;
    }

    /** Return true if include/jni.h is found. */
    private static class _JniFileFilter implements FileFilter {
        // FindBugs suggested "Could be refactored into a named static
        // inner class (1)"
        /** Return true if include/jni.h is found.
         *  @return true if include/jni.h is found.
         */
        @Override
        public boolean accept(File pathname) {
            return new File(pathname, "/include/jni.h").canRead();
        }
    }

    /** A list of actors present to include in the makefile*/
    private List<String> _actorsToInclude;

    /** A list of the defines to add in the _types.h file */
    private List<String> _definesToAdd = new LinkedList<String>();

    private CodeStream _overloadedFunctionsDeclaration;
    private CodeStream _overloadedFunctions;

    /** An ordered set of function code */
    private LinkedHashSet<String> _overloadedFunctionSet;

    /** True if we have printed the JVM warning. */
    private boolean _printedJVMWarning = true;

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
        _scalarDeleteTypes.add("Int");
        _scalarDeleteTypes.add("Long");
    }
}
