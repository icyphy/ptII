/* Code generator for the Java language.

 Copyright (c) 2008-2010 The Regents of the University of California.
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
package ptolemy.codegen.java.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.jni.PointerToken;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeGeneratorUtilities;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////CodeGenerator

/** Base class for Java code generator.
 *
 *  @author Gang Zhou
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating red (zgang)
 *  @Pt.AcceptedRating red (zgang)
 */

public class JavaCodeGenerator extends CodeGenerator {

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
        super(container, name);

        generatorPackage.setExpression("ptolemy.codegen.java");
        // A list of the primitive types supported by the code generator.
        // FIXME: we should not have to set these each time, but
        // JavaCodeGenerator uses Integer, and CCodeGenerator uses Int
        _primitiveTypes = Arrays.asList(new String[] { "Integer", "Double",
                "String", "Long", "Boolean", "UnsignedByte", "Pointer" });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String codeGenType(Type type) {
        //String ptolemyType = super.codeGenType(type);
        String result = type == BaseType.INT ? "Int"
                : type == BaseType.LONG ? "Long"
                        : type == BaseType.STRING ? "String"
                                : type == BaseType.DOUBLE ? "Double"
                                        : type == BaseType.BOOLEAN ? "Boolean"
                                                : type == BaseType.UNSIGNED_BYTE ? "UnsignedByte"
                                                        : type == PointerToken.POINTER ? "Pointer"
                                                                : null;

        if (result == null) {
            if (type instanceof ArrayType) {
                result = "Array";

            } else if (type instanceof MatrixType) {
                result = "Matrix";
            }
        }
        if (result == null || result.length() == 0) {
            new Exception("Cannot resolve codegen type from Ptolemy type: "
                    + type).printStackTrace();
        }
        if (result == null) {
            return null;
        }
        return result.replace("Int", "Integer")
                .replace("Integerger", "Integer");
        //return ptolemyType.replace("Int", "Integer").replace("Integerger", "Integer").replace("Array", "Token");
    }

    /** Generate the function table.  In this base class return
     *  the empty string.
     *  @param types An array of types.
     *  @param functions An array of functions.
     *  @return The code that declares functions.
     */
    public Object generateFunctionTable(Object[] types, Object[] functions) {
        StringBuffer code = new StringBuffer();

        if (functions.length > 0 && types.length > 0) {

            code.append("private final int NUM_TYPE = " + types.length + ";"
                    + _eol);
            code.append("private final int NUM_FUNC = " + functions.length
                    + ";" + _eol);
            code.append("//Token (*functionTable[NUM_TYPE][NUM_FUNC])"
                    + "(Token, ...)= {" + _eol);

            for (int i = 0; i < types.length; i++) {
                code.append("//\t{");
                for (int j = 0; j < functions.length; j++) {
                    if (functions[j].equals("isCloseTo")
                            && (types[i].equals("Boolean") || types[i]
                                    .equals("String"))) {
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
                    if (j != (functions.length - 1)) {
                        code.append(", ");
                    }
                }
                if (i != (types.length - 1)) {
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
    public String generateClosingEntryCode() {
        return "public void doWrapup() throws Exception { " + _eol;
    }

    /** Return the closing exit code, if any.
     *  @return the closing exit code.
     */
    public String generateClosingExitCode() {
        return "}" + _eol;
    }

    /** Generate the initialization procedure entry point.
     *  @return a string for the initialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeEntryCode() throws IllegalActionException {
        return _eol + _eol + "public void initialize() {" + _eol;
    }

    /** Generate the initialization procedure exit point.
     *  @return a string for the initialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeExitCode() throws IllegalActionException {

        return "}" + _eol;
    }

    /** Generate the initialization procedure name.
     *  @return a string for the initialization procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
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
    public String generateLineInfo(int lineNumber, String filename) {
        return "#line " + lineNumber + " \"" + filename + "\"\n";
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a program.
     *   In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {

        StringBuffer mainEntryCode = new StringBuffer();

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (isTopLevel()) {
            //             mainEntryCode
            //                     .append(_eol
            //                             + _eol
            //                             + "public static void main(String [] args) throws Exception {"
            //                             + _eol + _sanitizedModelName + " model = new "
            //                             + _sanitizedModelName + "();" + _eol
            //                             + "model.run();" + _eol + "}" + _eol
            //                             + "public void run() throws Exception {" + _eol);

            mainEntryCode
                    .append(_eol
                            + _eol
                            + "public static void main(String [] args) throws Exception {"
                            + _eol + _sanitizedModelName + " model = new "
                            + _sanitizedModelName + "();" + _eol
                            + "model.initialize();" + _eol + "model.execute();"
                            + _eol + "model.doWrapup();" + _eol
                            + "System.exit(0);" + _eol + "}" + _eol);

            String targetValue = target.getExpression();
            if (!targetValue.equals(_DEFAULT_TARGET)) {
                mainEntryCode.append("//FIXME: JavaCodeGenerator hack" + _eol
                        + "init();" + _eol);
            }

        } else {
            mainEntryCode.append(_eol + _eol + "public Object[] " + _eol
                    + "fire (" + _eol);

            Iterator inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            boolean addComma = false;
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                if (addComma) {
                    mainEntryCode.append(", ");
                }
                mainEntryCode.append("Object[]"
                        + CodeGeneratorHelper.generateSimpleName(inputPort));
                addComma = true;
            }

            mainEntryCode.append("){" + _eol);

        }

        return mainEntryCode.toString();
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {

        if (isTopLevel()) {
            return "}";
        } else {
            return _INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                    + _eol + "}" + _eol;
        }
    }

    /** Generate the postfire procedure entry point.
     *  @return a string for the postfire procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireEntryCode() throws IllegalActionException {
        return _eol + _eol + "public boolean postfire() {" + _eol;

    }

    /** Generate the postfire procedure exit point.
     *  @return a string for the postfire procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireExitCode() throws IllegalActionException {
        return _INDENT1 + "return true;" + _eol + "}" + _eol;
    }

    /** Generate the postfire procedure name.
     *  @return a string for the postfire procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireProcedureName() throws IllegalActionException {

        return _INDENT1 + "postfire();" + _eol;
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
     * @exception IllegalActionException If an error occurrs when generating
     *  the type resolution code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the type resolution code.
     */
    public String generateTypeConvertCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();

        code.append(_eol
                + comment("Generate type resolution code for "
                        + getContainer().getFullName()));

        // Include the constantsBlock at the top so that sharedBlocks from
        // actors can use true and false etc.  StringMatches needs this.
        CodeStream sharedStream = new CodeStream(
                "$CLASSPATH/ptolemy/codegen/java/kernel/SharedCode.j", this);
        sharedStream.appendCodeBlock("constantsBlock");
        code.append(sharedStream.toString());

        HashSet functions = _getReferencedFunctions();

        HashSet types = _getReferencedTypes(functions);

        Object[] typesArray = types.toArray();
        CodeStream[] typeStreams = new CodeStream[types.size()];

        // Generate type map.
        StringBuffer typeMembers = new StringBuffer();
        code.append("private final short TYPE_Token = -1;" + _eol);
        for (int i = 0; i < typesArray.length; i++) {
            // Open the .j file for each type.
            typeStreams[i] = new CodeStream(
                    "$CLASSPATH/ptolemy/codegen/java/kernel/type/"
                            + typesArray[i] + ".j", this);

            code.append("#define PTCG_TYPE_" + typesArray[i] + " " + i + _eol);
            code.append("private final short TYPE_" + typesArray[i] + " = " + i
                    + ";" + _eol);

            // Dynamically generate all the types within the union.
            if (i > 0) {
                typeMembers.append(_INDENT2);
            }
            typeMembers.append(typesArray[i] + "Token " + typesArray[i] + ";");
            if (i < typesArray.length - 1) {
                typeMembers.append(_eol);
            }
        }

        Object[] functionsArray = functions.toArray();

        // True if we have a delete function that needs to return a Token
        boolean defineEmptyToken = false;

        for (int i = 0; i < functionsArray.length; i++) {
            code.append("#define FUNC_" + functionsArray[i] + " " + i + _eol);
            if (functionsArray[i].equals("delete")) {
                defineEmptyToken = true;
            }
        }

        //code.append("typedef struct token Token;" + _eol);

        // Generate type and function definitions.
        for (int i = 0; i < typesArray.length; i++) {
            // The "declareBlock" contains all necessary declarations for the
            // type; thus, it is always read into the code stream when
            // accessing this particular type.
            typeStreams[i].appendCodeBlock("declareBlock");
            code.append(typeStreams[i].toString());
        }

        ArrayList args = new ArrayList();
        // Token declareBlock.
        if (typeMembers.length() != 0) {
            args.add(typeMembers.toString());
            sharedStream.clear();
            sharedStream.appendCodeBlock("tokenDeclareBlock", args);

            if (defineEmptyToken) {
                sharedStream.append("Token emptyToken; "
                        + comment("Used by *_delete() and others.") + _eol);
            }
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
                    for (int k = 0; k < functionsArray.length; k++) {
                        if (functionsArray[k].equals("equals")) {
                            foundEquals = true;
                        }
                    }
                    if (!foundEquals) {
                        // Boolean_isCloseTo and String_isCloseTo
                        // use Boolean_equals and String_equals.
                        args.clear();
                        args.add(typesArray[i] + "_equals");
                        sharedStream.appendCodeBlock("funcHeaderBlock", args);
                    }
                }
                if (!_scalarDeleteTypes.contains(typesArray[i])
                        || !functionsArray[j].equals("delete")) {
                    // Skip Boolean_delete etc.
                    args.clear();
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

            for (int j = 0; j < functionsArray.length; j++) {

                // The code block declaration has to follow this convention:
                // /*** [function name]Block ***/
                //     .....
                // /**/
                try {
                    // Boolean_isCloseTo and String_isCloseTo map to
                    // Boolean_equals and String_equals.
                    if (functionsArray[j].equals("isCloseTo")
                            && (typesArray[i].equals("Boolean") || typesArray[i]
                                    .equals("String"))) {

                        if (!functions.contains("equals")) {
                            //typeStreams[i].appendCodeBlock(typesArray[i]
                            //        + "_equals");
                            markFunctionCalled(typesArray[i] + "_equals", null);
                        }
                    } else {
                        String functionName = typesArray[i] + "_"
                                + functionsArray[j];

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

                    System.out.println("Warning, failed to find "
                            + typesArray[i] + "_" + functionsArray[j]);
                    //                     typeStreams[i].append("#define " + typesArray[i] + "_"
                    //                             + functionsArray[j] + " MISSING " + _eol);

                    // It is ok because this polymorphic function may not be
                    // supported by all types.
                }
            }
            code.append(processCode(typeStreams[i].toString()));
        }

        code.append(_overloadedFunctions.toString());

        return code.toString();
    }

    /** Process the specified code for the helper associated with the
     *  container.  Replace macros with their values.
     *  @param code The code to process.
     *  @return The processed code.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    public String processCode(String code) throws IllegalActionException {
        JavaCodeGeneratorHelper helper = (JavaCodeGeneratorHelper) _getHelper(getContainer());
        return helper.processCode(code);
    }

    /** Return the set of referenced functions.
     * @return The set of referenced functions.
     */
    private HashSet _getReferencedFunctions() {
        // Determine the total number of referenced polymorphic functions.
        HashSet functions = new HashSet();
        functions.add("new");
        functions.add("delete");
        //functions.add("toString");    // for debugging.
        functions.add("convert");
        functions.add("isCloseTo");
        functions.addAll(_typeFuncUsed);
        functions.addAll(_tokenFuncUsed);

        return functions;
    }

    /** Return the new types used by the given set of functions.
     * @param functions The set of functions used, such as "equals",
     * "isCloseTo", and "toString".
     * @return The new types used.
     */
    private HashSet _getReferencedTypes(HashSet functions) {
        // Determine the total number of referenced types.
        HashSet types = new HashSet();
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
        return types;
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateVariableDeclaration());

        // Generate variable declarations for modified variables.
        if (_modifiedVariables != null && !(_modifiedVariables.isEmpty())) {
            code.append(comment("Generate variable declarations for "
                    + "modified parameters"));
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                // SetVariable needs this to be a Variable, not a Parameter.
                Variable variable = (Variable) modifiedVariables.next();

                CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(variable
                        .getContainer());
                code.append("static " + helper.targetType(variable.getType())
                        + " " + generateVariableName(variable) + ";" + _eol);
            }
        }

        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateVariableInitialization());

        // Generate variable initialization for modified variables.
        if (_modifiedVariables != null && !(_modifiedVariables.isEmpty())) {
            code.append(comment(1, "Generate variable initialization for "
                    + "modified parameters"));
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                // SetVariable needs this to be a Variable, not a Parameter.
                Variable variable = (Variable) modifiedVariables.next();

                NamedObj container = variable.getContainer();
                CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);
                code.append(_INDENT1
                        + generateVariableName(variable)
                        + " = "
                        + containerHelper.getParameterValue(CodeGeneratorHelper
                                .generateSimpleName(variable), variable
                                .getContainer()) + ";" + _eol);
            }
        }

        return code.toString();
    }

    /** Generate the wrapup procedure entry point.
     *  @return a string for the wrapup procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupEntryCode() throws IllegalActionException {
        return _eol + _eol + "public void wrapup() {" + _eol;
    }

    /** Generate the wrapup procedure exit point.
     *  @return a string for the wrapup procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupExitCode() throws IllegalActionException {

        return "}" + _eol;
    }

    /** Generate the wrapup procedure name.
     *  @return a string for the wrapup procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupProcedureName() throws IllegalActionException {

        String commentWrapup = "";
        if (_iterations() <= 0) {
            commentWrapup = "//";
        }
        return commentWrapup + "wrapup();" + _eol;
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
                int openBracketCount = 0;
                int commentCount = 0;
                for (int i = 0; ((i + 1) < linesPerMethod && line != null)
                        || openBracketCount > 0 || commentCount > 0; i++) {
                    lineNumber++;
                    line = bufferedReader.readLine();
                    if (line != null) {
                        body.append(line + _eol);
                        String trimmedLine = line.trim();
                        if (trimmedLine.startsWith("/*")) {
                            commentCount++;
                        }
                        if (trimmedLine.endsWith("*/")) {
                            commentCount--;
                        }

                        if (!trimmedLine.startsWith("//")
                                && !trimmedLine.startsWith("/*")
                                && !trimmedLine.startsWith("*")) {
                            // Look for curly braces in non-commented lines
                            // This code could be buggy . . .
                            if (line.trim().endsWith("{")) {
                                openBracketCount++;
                            }
                            // Lines can both start and end with braces.
                            if (line.trim().startsWith("}")) {
                                openBracketCount--;
                            }
                        }
                    }
                }

                bodies.append("void " + methodName + "() {" + _eol
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

    /**
     * Get the corresponding type in Java from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The Java data type.
     */
    public String targetType(Type ptType) {
        // FIXME: this is duplicated code from CodeGeneratorHelper.targetType()
        // FIXME: we may need to add more primitive types.
        return ptType == BaseType.INT ? "int"
                : ptType == BaseType.STRING ? "String"
                        : ptType == BaseType.DOUBLE ? "double"
                                : ptType == BaseType.BOOLEAN ? "boolean"
                                        : ptType == BaseType.LONG ? "long"
                                                : ptType == BaseType.UNSIGNED_BYTE ? "byte"
                                                        //: ptType == PointerToken.POINTER ? "void*"
                                                        : "Token";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add include directories specified by the actors in this model.
     *  @exception IllegalActionException If thrown when getting an actor's
     *   include directories.
     */
    protected void _addActorIncludeDirectories() throws IllegalActionException {
        //         ActorCodeGenerator helper = _getHelper(getContainer());

        //         Set actorIncludeDirectories = helper.getIncludeDirectories();
        //         Iterator includeIterator = actorIncludeDirectories.iterator();
        //         while (includeIterator.hasNext()) {
        //             addInclude("-I\"" + ((String) includeIterator.next()) + "\"");
        //         }
    }

    /** Add libraries specified by the actors in this model.
     *  @exception IllegalActionException If thrown when getting an actor's
     *   libraries.
     */
    protected void _addActorLibraries() throws IllegalActionException {
        //         ActorCodeGenerator helper = _getHelper(getContainer());

        //         Set actorLibraryDirectories = helper.getLibraryDirectories();
        //         Iterator libraryDirectoryIterator = actorLibraryDirectories.iterator();
        //         while (libraryDirectoryIterator.hasNext()) {
        //             addLibrary("-L\"" + ((String) libraryDirectoryIterator.next())
        //                     + "\"");
        //         }

        //         Set actorLibraries = helper.getLibraries();
        //         Iterator librariesIterator = actorLibraries.iterator();
        //         while (librariesIterator.hasNext()) {
        //             addLibrary("-l\"" + ((String) librariesIterator.next()) + "\"");
        //         }
    }

    /** Analyze the model to find out what connections need to be type
     *  converted. This should be called before all the generate methods.
     *  @exception IllegalActionException If the helper of the
     *   top composite actor is unavailable.
     */
    protected void _analyzeTypeConversions() throws IllegalActionException {
        super._analyzeTypeConversions();

        String cCodegenPath = "$CLASSPATH/ptolemy/codegen/java/kernel/";
        String typeDir = cCodegenPath + "type/";
        String functionDir = typeDir + "polymorphic/";

        _overloadedFunctions = new CodeStream(functionDir + "add.j", this);
        _overloadedFunctions.parse(functionDir + "equals.j");
        _overloadedFunctions.parse(functionDir + "multiply.j");
        _overloadedFunctions.parse(functionDir + "divide.j");
        _overloadedFunctions.parse(functionDir + "subtract.j");
        _overloadedFunctions.parse(functionDir + "toString.j");
        _overloadedFunctions.parse(functionDir + "convert.j");
        _overloadedFunctions.parse(functionDir + "print.j");
        _overloadedFunctions.parse(functionDir + "negate.j");
        _overloadedFunctions.parse(typeDir + "Array.j");
        _overloadedFunctions.parse(typeDir + "Boolean.j");
        _overloadedFunctions.parse(typeDir + "Double.j");
        _overloadedFunctions.parse(typeDir + "Integer.j");
        _overloadedFunctions.parse(typeDir + "Matrix.j");
        _overloadedFunctions.parse(typeDir + "String.j");

        //        String directorFunctionDir = cCodegenPath + "parameterized/directorFunctions/";
        //        _overloadedFunctions.parse(directorFunctionDir + "PNDirector.java");
        //        _overloadedFunctions.parse(directorFunctionDir + "OpenRtosPNDirector.java");
        //        _overloadedFunctions.parse(directorFunctionDir + "MpiPNDirector.c");

        _overloadedFunctionSet = new HashSet<String>();

    }

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory.
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     *  @exception IllegalActionException If there are problems reading
     *  parameters or executing the commands.
     */
    protected int _executeCommands() throws IllegalActionException {

        List commands = new LinkedList();
        if (((BooleanToken) compile.getToken()).booleanValue()) {
            commands.add("make -f " + _sanitizedModelName + ".mk "
                    + compileTarget.stringValue());
        }

        if (isTopLevel()) {
            if (((BooleanToken) run.getToken()).booleanValue()) {
                commands.add("make -f " + _sanitizedModelName + ".mk run");
            }
        }

        if (commands.size() == 0) {
            return -1;
        }

        _executeCommands.setCommands(commands);
        _executeCommands.setWorkingDirectory(codeDirectory.asFile());

        try {
            // FIXME: need to put this output in to the UI, if any.
            _executeCommands.start();
        } catch (Exception ex) {
            StringBuffer errorMessage = new StringBuffer();
            Iterator allCommands = commands.iterator();
            while (allCommands.hasNext()) {
                errorMessage.append((String) allCommands.next() + _eol);
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:" + _eol + errorMessage);
        }
        return _executeCommands.getLastSubprocessReturnCode();
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
        HashSet defines = new HashSet<String>();
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

        if (((BooleanToken) sourceLineBinding.getToken()).booleanValue()) {

            String filename = getOutputFilename();
            //filename = new java.io.File(filename).getAbsolutePath().replace('\\', '/');

            tokenizer = new StringTokenizer(code.toString(), _eol);

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

        return code;
    }

    /** Generate include files. FIXME: State what is included.
     *  @return The #include statements, surrounded by #ifndef to ensure
     *   that the files are included only once.
     *  @exception IllegalActionException If the helper class for some actor
     *   cannot be found.
     */
    protected String _generateIncludeFiles() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
        Set includingFiles = compositeActorHelper.getHeaderFiles();

        //includingFiles.add("<stdlib.h>"); // Sun requires stdlib.h for malloc

        //if (isTopLevel()
        //        && ((BooleanToken) measureTime.getToken()).booleanValue()) {
        //    includingFiles.add("<sys/time.h>");
        //}

        //if (!isTopLevel()) {
        //    includingFiles.add("\"" + _sanitizedModelName + ".h\"");
        //
        //    includingFiles.addAll(((JavaCodeGeneratorHelper)compositeActorHelper).getJVMHeaderFiles());
        //}

        //includingFiles.add("<stdarg.h>");
        //includingFiles.add("<stdio.h>");
        //includingFiles.add("<string.h>");

        for (String file : (Set<String>) includingFiles) {
            if (!file.equals("<math.h>") && !file.equals("<stdio.h>")) {
                code.append("import " + file + _eol);
            }
        }
        code.append("public class " + _sanitizedModelName + " {" + _eol);
        return code.toString();
    }

    /** Generate the code for printing the execution time since
     *  the code generated by _recordStartTime() was called.
     *  @return Return the code for printing the total execution time.
     */
    protected String _printExecutionTime() {
        StringBuffer endCode = new StringBuffer();
        endCode.append(super._printExecutionTime());

        endCode
                .append("Runtime runtime = Runtime.getRuntime();\n"
                        + "long totalMemory = runtime.totalMemory() / 1024;\n"
                        + "long freeMemory = runtime.freeMemory() / 1024;\n"
                        + "System.out.println(System.currentTimeMillis() - startTime + \""
                        + " ms. Memory: \" + totalMemory + \"K Free: \""
                        + " + freeMemory + \"K (\" + "
                        + "Math.round((((double) freeMemory) / ((double) totalMemory)) * 100.0)"
                        + " + \"%\");\n");
        return endCode.toString();
    }

    /** Generate the code for recording the current time.
     *  This writes current time into a timespec struct called "start".
     *  @return Return the code for recording the current time.
     */
    protected String _recordStartTime() {
        StringBuffer startCode = new StringBuffer();
        startCode.append("long startTime = System.currentTimeMillis();");
        return startCode.toString();
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
     *  then we look for the resouce "ptolemy.codegen.c.makefile.in", which
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

     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    protected void _writeMakefile() throws IllegalActionException {

        // Write the code to a file with the same name as the model into
        // the directory named by the codeDirectory parameter.
        //try {
        // Check if needs to overwrite.
        if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
                && codeDirectory.asFile().exists()) {
            // FIXME: It is totally bogus to ask a yes/no question
            // like this, since it makes it impossible to call
            // this method from a script.  If the question is
            // asked, the build will hang.
            if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                    + " exists. OK to overwrite?")) {
                return;
                /*
                throw new IllegalActionException(this,
                        "Please select another file name.");
                        */
            }
        }

        File codeDirectoryFile = codeDirectory.asFile();
        if (codeDirectoryFile.isFile()) {
            throw new IllegalActionException(this, "Error: "
                    + codeDirectory.stringValue() + " is a file, "
                    + " it should be a directory.");
        }

        if (!codeDirectoryFile.isDirectory() && !codeDirectoryFile.mkdirs()) {
            throw new IllegalActionException(this, "Failed to make the \""
                    + codeDirectory.stringValue() + "\" directory.");
        }

        Map substituteMap;
        try {
            // Add substitutions for all the parameter.
            // For example, @generatorPackage@ will be replaced with
            // the value of the generatorPackage.
            substituteMap = CodeGeneratorUtilities.newMap(this);
            substituteMap.put("@modelName@", _sanitizedModelName);
            substituteMap.put("@CLASSPATHSEPARATOR@", StringUtilities
                    .getProperty("path.separator"));
            substituteMap
                    .put("@PTCGIncludes@", _concatenateElements(_includes));
            substituteMap.put("@PTCGLibraries@",
                    _concatenateClasspath(_libraries));

            // Define substitutions to be used in the makefile
            substituteMap.put("@PTJNI_NO_CYGWIN@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "");
            if (((BooleanToken) generateCpp.getToken()).booleanValue()) {
                substituteMap.put("@PTJavaCompiler@", "g++");
            } else {
                substituteMap.put("@PTJavaCompiler@", "javac");
            }

            String osName = StringUtilities.getProperty("os.name");
            if (osName != null) {
                // Keep these alphabetical
                if (osName.startsWith("Linux")) {
                    substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-shared");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "lib");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "so");
                } else if (osName.startsWith("Mac OS X")) {
                    substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-dynamiclib");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "lib");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "dylib");
                } else if (osName.startsWith("SunOS")) {
                    substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-shared");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@", "-fPIC");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@", "-fPIC");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "lib");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "so");
                } else if (osName.startsWith("Windows")) {
                    substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-shared");
                    substituteMap.put("@PTJNI_NO_CYGWIN@", "-mno-cygwin");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@",
                            "-Wl,--add-stdcall-alias");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "dll");
                } else {
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@",
                            "# Unknown java property os.name \"" + osName
                                    + "\" please edit ptolemy/codegen/c/"
                                    + "kernel/JavaCodeGenerator.java and "
                                    + "ptolemy/actor/lib/jni/"
                                    + "CompiledCompositeActor.java");
                }

            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Problem generating substitution map from " + _model);
        }

        List templateList = new LinkedList();

        // 1. Look for a .mk.in file with the same name as the model.
        URIAttribute uriAttribute = (URIAttribute) _model.getAttribute("_uri",
                URIAttribute.class);
        if (uriAttribute != null) {
            String uriString = uriAttribute.getURI().toString();
            templateList.add(uriString.substring(0,
                    uriString.lastIndexOf("/") + 1)
                    + _sanitizedModelName + ".mk.in");
        }
        // 2. If the target parameter is set, look for a makefile.
        String generatorDirectory = generatorPackage.stringValue().replace('.',
                '/');
        String targetValue = target.getExpression();
        templateList.add(generatorDirectory + "/targets/" + targetValue
                + "/makefile.in");

        // 3. Look for the generic makefile.in
        // Note this code is repeated in the catch below.
        templateList.add(generatorDirectory + "/makefile.in");

        // If necessary, add a trailing / after codeDirectory.
        String makefileOutputName = codeDirectory.stringValue()
                + ((!codeDirectory.stringValue().endsWith("/") && !codeDirectory
                        .stringValue().endsWith("\\")) ? "/" : "")
                + _sanitizedModelName + ".mk";

        BufferedReader makefileTemplateReader = null;

        StringBuffer errorMessage = new StringBuffer();
        String makefileTemplateName = null;
        boolean success = false;
        try {
            Iterator templates = templateList.iterator();
            while (templates.hasNext()) {
                makefileTemplateName = (String) templates.next();
                try {
                    makefileTemplateReader = CodeGeneratorUtilities
                            .openAsFileOrURL(makefileTemplateName);
                } catch (IOException ex) {
                    errorMessage.append("Failed to open \""
                            + makefileTemplateName + "\". ");
                }
                if (makefileTemplateReader != null) {
                    _executeCommands.stdout("Reading \"" + makefileTemplateName
                            + "\"," + _eol + "    writing \""
                            + makefileOutputName + "\"");
                    CodeGeneratorUtilities.substitute(makefileTemplateReader,
                            substituteMap, makefileOutputName);
                    success = true;
                    break;
                }
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to read \""
                    + makefileTemplateName + "\" or write \""
                    + makefileOutputName + "\"");
        } finally {
            if (makefileTemplateReader != null) {
                try {
                    makefileTemplateReader.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to close \"" + makefileTemplateName + "\"");
                }
            }
        }
        if (!success) {
            throw new IllegalActionException(this, errorMessage.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a Collection of Strings, return a string where each element of the
     *  Set is separated by $.
     *  @param collection The collection of elements.
     *  @return A String that contains each element of the Set separated by
     *  a space.
     */
    private static String _concatenateClasspath(Collection collection) {
        StringBuffer buffer = new StringBuffer();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (buffer.length() > 0) {
                buffer.append("$(CLASSPATHSEPARATOR)");
            }
            buffer.append((String) iterator.next());
        }
        if (buffer.length() > 0) {
            buffer.append("$(CLASSPATHSEPARATOR)");
        }
        return buffer.toString();
    }

    /** Given a Collection of Strings, return a string where each element of the
     *  Set is separated by a space.
     *  @param collection The collection of elements.
     *  @return A String that contains each element of the Set separated by
     *  a space.
     */
    private static String _concatenateElements(Collection collection) {
        StringBuffer buffer = new StringBuffer();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (buffer.length() > 0) {
                buffer.append(" ");
            }
            buffer.append((String) iterator.next());
        }
        return buffer.toString();
    }

    /** Return the value of the iterations parameter of the director, if any. */
    private int _iterations() throws IllegalActionException {
        Director director = ((ptolemy.actor.CompositeActor) getComponent())
                .getDirector();
        if (director != null) {
            Attribute iterations = director.getAttribute("iterations");
            if (iterations != null) {
                return ((IntToken) ((Variable) iterations).getToken())
                        .intValue();
            }
        }
        return -1;
    }

    /** Add called functions to the set of overloaded functions for
     *  later use.
     *  If the function starts with "Array_", add everything after the
     *  "Array_" is added to the set of token functions used.
     *  @param name The name of the function, for example "Double_equals"
     *  @param helper The corresponding helper that contains the
     *  codeBlock.
     *  @exception IllegalActionException If there is a problem adding
     *  a function to the set of overloaded functions.
     */
    public void markFunctionCalled(String name, JavaCodeGeneratorHelper helper)
            throws IllegalActionException {

        try {
            String functionCode = _overloadedFunctions.getCodeBlock(name);

            if (!_overloadedFunctionSet.contains(name)) {
                _overloadedFunctionSet.add(name);

                String code = (helper == null) ? processCode(functionCode)
                        : helper.processCode(functionCode);

                _overloadedFunctions.append(code);

            }
            if (name.startsWith("Array_")) {
                // Array_xxx might need to have xxx added.
                // See c/actor/lib/test/auto/MultiplyDivide5.xml

                // FIXME: this will add any function, which means that
                // if the user has Array_foo, foo is added.  Is this right?
                _tokenFuncUsed.add(name.substring(6));
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to mark function called for \"" + name + "\" in \""
                            + getComponent().getFullName() + "\"");
        }

    }

    private CodeStream _overloadedFunctions;

    private Set<String> _overloadedFunctionSet;

    /** Set of type/function combinations that are not supported.
     *  We use one method so as to reduce code size.
     */
    private static Set _unsupportedTypeFunctions;

    /** Types that share the scalarDelete() method, which does nothing.
     *  We use one method so as to reduce code size.
     */
    private static Set _scalarDeleteTypes;

    static {
        _unsupportedTypeFunctions = new HashSet();
        _unsupportedTypeFunctions.add("String_divide");
        _unsupportedTypeFunctions.add("String_multiply");
        _unsupportedTypeFunctions.add("String_negate");
        _unsupportedTypeFunctions.add("String_one");
        _unsupportedTypeFunctions.add("String_subtract");

        _unsupportedTypeFunctions.add("Boolean_divide");
        _unsupportedTypeFunctions.add("Boolean_multiply");
        _unsupportedTypeFunctions.add("Boolean_subtract");

        _scalarDeleteTypes = new HashSet();
        _scalarDeleteTypes.add("Boolean");
        _scalarDeleteTypes.add("Double");
        _scalarDeleteTypes.add("Integer");
        _scalarDeleteTypes.add("Long");
    }
}
