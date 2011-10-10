/* Code generator for the C language.

 Copyright (c) 2005-2011 The Regents of the University of California.
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
package ptolemy.codegen.c.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Placeable;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeGeneratorUtilities;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.JVMBitWidth;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////CodeGenerator

/** Base class for C code generator.
 *
 *  @author Gang Zhou
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating red (zgang)
 *  @Pt.AcceptedRating red (zgang)
 */

public class CCodeGenerator extends CodeGenerator {

    /** Create a new instance of the C code generator.
     *  @param container The container.
     *  @param name The name of the C code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public CCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generatorPackage.setExpression("ptolemy.codegen.c");

        // FIXME: we should not have to set these each time, but
        // JavaCodeGenerator uses Integer, and CCodeGenerator uses Int
        _primitiveTypes = Arrays.asList(new String[] { "Int", "Double",
                "String", "Long", "Boolean", "UnsignedByte", "Pointer" });
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the function table.  In this base class return
     *  the empty string.
     *  @param types An array of types.
     *  @param functions An array of functions.
     *  @return The code that declares functions.
     */
    public Object generateFunctionTable(Object[] types, Object[] functions) {
        StringBuffer code = new StringBuffer();

        if (functions.length > 0 && types.length > 0) {

            code.append("#define NUM_TYPE " + types.length + _eol);
            code.append("#define NUM_FUNC " + functions.length + _eol);
            code.append("Token (*functionTable[NUM_TYPE][NUM_FUNC])"
                    + "(Token, ...)= {" + _eol);

            for (int i = 0; i < types.length; i++) {
                code.append("\t{");
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

            code.append("};" + _eol);
        }
        return code.toString();
    }

    /** Generate the initialization procedure entry point.
     *  @return a string for the initialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeEntryCode() throws IllegalActionException {
        // If the container is in the top level, we are generating code
        // for the whole model.
        if (isTopLevel()) {
            // We use (void) so as to avoid the avr-gcc 3.4.6 warning:
            // "function declaration isn't a prototype
            if (!_hasPlaceable()) {
                return _eol + _eol + "void initialize(void) {" + _eol;
            } else if (_hasPlaceable()
                    && target.getExpression().equals("posix")) {
                return _eol + _eol + "#ifdef __MAC_OS_X_VERSION_10_0" + _eol
                        + "void initialize(void * options) {" + _eol + "#else"
                        + _eol + "void initialize(void) {" + _eol + "#endif"
                        + _eol;

            } else {
                return _eol + _eol + "void initialize(void) {" + _eol;

            }
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
                    + _sanitizedModelName + "_initialize("
                    + "JNIEnv *env, jobject obj) {" + _eol;
        }
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
        if (_hasPlaceable() && target.getExpression().equals("posix")) {
            return "    initialize(options);" + _eol + "#else" + _eol
                    + "     initialize();" + _eol + "#endif";
        } else {
            return "    initialize();" + _eol;
        }
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
        return "#line " + lineNumber + " \"" + filename + "\"" + _eol;
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
            mainEntryCode.append(_eol + _eol
                    + "int main(int argc, char *argv[]) {" + _eol);

            if (_hasPlaceable() && target.getExpression().equals("posix")) {
                mainEntryCode
                        .append(_eol
                                + _eol
                                + "#ifdef __MAC_OS_X_VERSION_10_0"
                                + _eol
                                + "    CFRunLoopSourceContext sourceContext;"
                                + _eol
                                + ""
                                + _eol
                                + "    /* Start the thread that runs the VM. */"
                                + _eol
                                + "    pthread_t vmthread;"
                                + _eol
                                + "    "
                                + _eol
                                + "    const char * vmArgv[2];"
                                + _eol
                                + "    vmArgv[0] = argv[0];"
                                + _eol
                                + "    /* Parse the args */"
                                + _eol
                                + "    if (access(\"/Users/cxh/ptII/ptolemy/plot/Plot.class\", R_OK) == 0) {"
                                + _eol
                                + "        vmArgv[1] = strdup(\"-Djava.class.path=/Users/cxh/ptII\");"
                                + _eol
                                + "    } else {"
                                + _eol
                                + "        // Use ptsupport here in case we use SliderSource or some other actor"
                                + _eol
                                + "        vmArgv[1] = strdup(\"-Djava.class.path=/Users/cxh/ptII/ptolemy/ptsupport.jar\");"
                                + _eol
                                + "        //options[0].optionString = \"-Djava.class.path=/Users/cxh/ptII/ptolemy/plot/plotapplication.jar\";"
                                + _eol
                                + "    }"
                                + _eol
                                + "    VMLaunchOptions * launchOptions = NewVMLaunchOptions(2, vmArgv);"
                                + _eol
                                + ""
                                + _eol
                                + "    /* Set our name for the Application Menu to our Main class */"
                                + _eol
                                + "    //setAppName(launchOptions->mainClass);"
                                + _eol
                                + "    setAppName(\""
                                + _sanitizedModelName
                                + "\");"
                                + _eol
                                + "    "
                                + _eol
                                + "    /* create a new pthread copying the stack size of the primordial pthread */ "
                                + _eol
                                + "    struct rlimit limit;"
                                + _eol
                                + "    size_t stack_size = 0;"
                                + _eol
                                + "    int rc = getrlimit(RLIMIT_STACK, &limit);"
                                + _eol
                                + "    if (rc == 0) {"
                                + _eol
                                + "        if (limit.rlim_cur != 0LL) {"
                                + _eol
                                + "            stack_size = (size_t)limit.rlim_cur;"
                                + _eol
                                + "        }"
                                + _eol
                                + "    }"
                                + _eol
                                + "    "
                                + _eol
                                + "    pthread_attr_t thread_attr;"
                                + _eol
                                + "    pthread_attr_init(&thread_attr);"
                                + _eol
                                + "    pthread_attr_setscope(&thread_attr, PTHREAD_SCOPE_SYSTEM);"
                                + _eol
                                + "    pthread_attr_setdetachstate(&thread_attr, PTHREAD_CREATE_DETACHED);"
                                + _eol
                                + "    if (stack_size > 0) {"
                                + _eol
                                + "        pthread_attr_setstacksize(&thread_attr, stack_size);"
                                + _eol
                                + "    }"
                                + _eol
                                + ""
                                + _eol
                                + "    /* Start the thread that we will start the JVM on. */"
                                + _eol
                                + "    pthread_create(&vmthread, &thread_attr, startupJava, launchOptions);"
                                + _eol
                                + "    pthread_attr_destroy(&thread_attr);"
                                + _eol
                                + ""
                                + _eol
                                + "    /* Create a a sourceContext to be used by our source that makes */"
                                + _eol
                                + "    /* sure the CFRunLoop doesn't exit right away */"
                                + _eol
                                + "    sourceContext.version = 0;"
                                + _eol
                                + "    sourceContext.info = NULL;"
                                + _eol
                                + "    sourceContext.retain = NULL;"
                                + _eol
                                + "    sourceContext.release = NULL;"
                                + _eol
                                + "    sourceContext.copyDescription = NULL;"
                                + _eol
                                + "    sourceContext.equal = NULL;"
                                + _eol
                                + "    sourceContext.hash = NULL;"
                                + _eol
                                + "    sourceContext.schedule = NULL;"
                                + _eol
                                + "    sourceContext.cancel = NULL;"
                                + _eol
                                + "    sourceContext.perform = &sourceCallBack;"
                                + _eol
                                + "    "
                                + _eol
                                + "    /* Create the Source from the sourceContext */"
                                + _eol
                                + "    CFRunLoopSourceRef sourceRef = CFRunLoopSourceCreate (NULL, 0, &sourceContext);"
                                + _eol
                                + "    "
                                + _eol
                                + "    /* Use the constant kCFRunLoopCommonModes to add the source to the set of objects */ "
                                + _eol
                                + "     /* monitored by all the common modes */"
                                + _eol
                                + "    CFRunLoopAddSource (CFRunLoopGetCurrent(),sourceRef,kCFRunLoopCommonModes); "
                                + _eol
                                + ""
                                + _eol
                                + "    /* Park this thread in the runloop */"
                                + _eol
                                + "    CFRunLoopRun();"
                                + _eol
                                + "    "
                                + _eol
                                + "    return 0;"
                                + _eol
                                + "} "
                                + _eol
                                + "static void* startupJava(void *options) {    "
                                + _eol);
            }
        } else {
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
            mainEntryCode.append(_eol + _eol + "JNIEXPORT jobjectArray JNICALL"
                    + _eol + "Java_" + _sanitizedModelName + "_fire (" + _eol
                    + "JNIEnv *env, jobject obj");

            Iterator inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                mainEntryCode.append(", jobjectArray "
                        + CodeGeneratorHelper.generateSimpleName(inputPort));
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
            return _INDENT1 + "exit(0);" + _eol + "}" + _eol;
        } else {
            return _INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                    + _eol;
        }
    }

    /** Generate the postfire procedure entry point.
     *  @return a string for the postfire procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireEntryCode() throws IllegalActionException {
        // If the container is in the top level, we are generating code
        // for the whole model.
        if (isTopLevel()) {
            return _eol + _eol + "boolean postfire(void) {" + _eol;

            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
                    + _sanitizedModelName + "_postfire("
                    + "JNIEnv *env, jobject obj) {" + _eol;
        }
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

        return _INDENT1 + "postfire(void);" + _eol;
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
                "$CLASSPATH/ptolemy/codegen/c/kernel/SharedCode.c", this);
        sharedStream.appendCodeBlock("constantsBlock");
        code.append(sharedStream.toString());

        HashSet functions = _getReferencedFunctions();

        HashSet types = _getTypeIDToUsed(_getNewTypesUsed(functions));

        Object[] typesArray = types.toArray();
        CodeStream[] typeStreams = new CodeStream[types.size()];

        // Generate type map.
        StringBuffer typeMembers = new StringBuffer();
        code.append("#define TYPE_Token -1 " + _eol);

        for (int i = 0; i < typesArray.length; i++) {
            // Open the .c file for each type.
            typeStreams[i] = new CodeStream(
                    "$CLASSPATH/ptolemy/codegen/c/kernel/type/" + typesArray[i]
                            + ".c", this);

            code.append("#define TYPE_" + typesArray[i] + " " + i + _eol);

            // Dynamically generate all the types within the union.
            typeMembers.append(typesArray[i] + "Token " + typesArray[i] + ";");
            if (i < typesArray.length - 1) {
                typeMembers.append(_eol);
            }
        }

        Object[] functionsArray = functions.toArray();

        // True if we have a delete function that needs to return a Token
        boolean defineEmptyToken = false;

        // Generate function map.
        for (int i = 0; i < functionsArray.length; i++) {

            code.append("#define FUNC_" + functionsArray[i] + " " + i + _eol);
            if (functionsArray[i].equals("delete")) {
                defineEmptyToken = true;
            }
        }

        code.append("typedef struct token Token;" + _eol);

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
            sharedStream.clear();
            args.add(typeMembers.toString());
            sharedStream.appendCodeBlock("tokenDeclareBlock", args);

            if (defineEmptyToken) {
                sharedStream.append("Token emptyToken; "
                        + comment("Used by *_delete() and others.") + _eol);
            }
            code.append(sharedStream.toString());
        }

        // Set to true if we need the unsupportedFunction() method.
        boolean defineUnsupportedTypeFunctionMethod = false;

        // Set to true if we need to scalarDelete() method.
        boolean defineScalarDeleteMethod = false;

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
        sharedStream.clear();

        StringBuffer typeFunctionCode = new StringBuffer();
        for (int i = 0; i < typesArray.length; i++) {
            typeStreams[i].clear();
            //typeStreams[i].appendCodeBlock(typesArray[i] + "_new");

            for (int j = 0; j < functionsArray.length; j++) {
                // The code block declaration has to follow this convention:
                // /*** [function name]Block ***/
                //     .....
                // /**/
                String functionName = typesArray[i] + "_" + functionsArray[j];

                try {
                    // Boolean_isCloseTo and String_isCloseTo map to
                    // Boolean_equals and String_equals.
                    if (functionsArray[j].equals("isCloseTo")
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
            typeFunctionCode.append(typeStreams[i].toString());
        }

        // Append type-polymorphic functions included in the function table.
        for (int i = 0; i < types.size(); i++) {
            // The "funcDeclareBlock" contains all function declarations for
            // the type.
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
                    for (int k = 0; k < functionsArray.length; k++) {
                        if (functionsArray[k].equals("equals")) {
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
                            sharedStream.appendCodeBlock("funcHeaderBlock",
                                    args);
                        }
                    }
                }
                if (!_scalarDeleteTypes.contains(typesArray[i])
                        || !functionsArray[j].equals("delete")) {
                    // Skip Boolean_delete etc.
                    args.clear();
                    if (!_unsupportedTypeFunctions.contains(functionName)) {
                        args.add(functionName);
                        sharedStream.append("// functionHeader: " + _eol);
                        sharedStream.appendCodeBlock("funcHeaderBlock", args);
                    }
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

        // sharedStream should the global code (e.g. token declaration,
        // constants, and etc.)
        code.append(sharedStream.toString());

        // Generate function type and token table.
        code.append(generateFunctionTable(typesArray, functionsArray));

        // _overloadedFunctions contains the set of functions:
        // add_Type1_Type2, negate_Type, and etc.
        code.append(_overloadedFunctions.toString());

        // typeFunction contains the set of function:
        // Type_new(), Type_delete(), and etc.
        code.append(typeFunctionCode);

        return processCode(code.toString());
    }

    private HashSet<String> _getTypeIDToUsed(HashSet<String> types) {
        HashSet result = new HashSet();
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

    /** Process the specified code for the helper associated with the
     *  container.  Replace macros with their values.
     *  @param code The code to process.
     *  @return The processed code.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    public String processCode(String code) throws IllegalActionException {
        CCodeGeneratorHelper helper = (CCodeGeneratorHelper) _getHelper(getContainer());
        return helper.processCode(code);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the set of referenced functions.
     * @return The set of referenced functions.
     */
    private HashSet _getReferencedFunctions() {
        // Determine the total number of referenced polymorphic functions.
        HashSet functions = new HashSet();
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
    private HashSet _getNewTypesUsed(HashSet functions) {
        // Determine the total number of referenced types.
        HashSet types = new HashSet();
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
                code.append(comment(
                        1,
                        "Variable: "
                                + variable
                                + " simpleName: "
                                + CodeGeneratorHelper
                                        .generateSimpleName(variable)
                                + " value: "
                                + containerHelper.getParameterValue(
                                        CodeGeneratorHelper
                                                .generateSimpleName(variable),
                                        variable.getContainer())
                                + " variable Type: "
                                + containerHelper.targetType(variable.getType())));

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

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (isTopLevel()) {
            return _eol + _eol + "void wrapup(void) {" + _eol;

            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            return _eol + _eol + "JNIEXPORT void JNICALL" + _eol + "Java_"
                    + _sanitizedModelName + "_wrapup("
                    + "JNIEnv *env, jobject obj) {" + _eol;
        }
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

        return _INDENT1 + "wrapup();" + _eol;
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
                for (int i = 0; (i + 1) < linesPerMethod && line != null; i++) {
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
     *  @exception IllegalActionException If thrown when getting an actor's
     *   include directories.
     */
    protected void _addActorIncludeDirectories() throws IllegalActionException {
        ActorCodeGenerator helper = _getHelper(getContainer());

        Set actorIncludeDirectories = helper.getIncludeDirectories();
        Iterator includeIterator = actorIncludeDirectories.iterator();
        while (includeIterator.hasNext()) {
            addInclude("-I\"" + ((String) includeIterator.next()) + "\"");
        }
    }

    /** Add libraries specified by the actors in this model.
     *  @exception IllegalActionException If thrown when getting an actor's
     *   libraries.
     */
    protected void _addActorLibraries() throws IllegalActionException {
        ActorCodeGenerator helper = _getHelper(getContainer());

        Set actorLibraryDirectories = helper.getLibraryDirectories();
        Iterator libraryDirectoryIterator = actorLibraryDirectories.iterator();
        while (libraryDirectoryIterator.hasNext()) {
            addLibrary("-L\"" + ((String) libraryDirectoryIterator.next())
                    + "\"");
        }

        Set actorLibraries = helper.getLibraries();
        Iterator librariesIterator = actorLibraries.iterator();
        while (librariesIterator.hasNext()) {
            addLibrary("-l\"" + ((String) librariesIterator.next()) + "\"");
        }
    }

    /** Analyze the model to find out what connections need to be type
     *  converted. This should be called before all the generate methods.
     *  @exception IllegalActionException If the helper of the
     *   top composite actor is unavailable.
     */
    protected void _analyzeTypeConversions() throws IllegalActionException {
        super._analyzeTypeConversions();
        _overloadedFunctionSet = new LinkedHashSet<String>();

        String cCodegenPath = "$CLASSPATH/ptolemy/codegen/c/";
        String typeDir = cCodegenPath + "kernel/type/";
        String functionDir = typeDir + "polymorphic/";

        _overloadedFunctions = new CodeStream(functionDir + "add.c", this);
        _overloadedFunctions.parse(functionDir + "clone.c");
        _overloadedFunctions.parse(functionDir + "convert.c");
        _overloadedFunctions.parse(functionDir + "delete.c");
        _overloadedFunctions.parse(functionDir + "divide.c");
        _overloadedFunctions.parse(functionDir + "equals.c");
        _overloadedFunctions.parse(functionDir + "multiply.c");
        _overloadedFunctions.parse(functionDir + "negate.c");
        _overloadedFunctions.parse(functionDir + "print.c");
        _overloadedFunctions.parse(functionDir + "subtract.c");
        _overloadedFunctions.parse(functionDir + "toString.c");
        _overloadedFunctions.parse(functionDir + "zero.c");

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
        _overloadedFunctions.parse(typeDir + "String.c");
        _overloadedFunctions.parse(typeDir + "StringArray.c");
        _overloadedFunctions.parse(typeDir + "UnsignedByte.c");

        // Useful for debugging
        //Iterator codeBlockNames = _overloadedFunctions.getAllCodeBlockNames().iterator();
        //while (codeBlockNames.hasNext()) {
        //    System.out.println("code block: " + codeBlockNames.next());
        //}

        // Parse other function files.
        String directorFunctionDir = cCodegenPath
                + "kernel/parameterized/directorFunctions/";
        _overloadedFunctions.parse(directorFunctionDir + "PNDirector.c");

        // ------------ Parse target-specific functions. --------------------
        if (target.getExpression().equals("default")) {
            return;
        }

        try {
            String targetDir = cCodegenPath + "targets/"
                    + target.getExpression() + "/";
            directorFunctionDir = targetDir
                    + "kernel/parameterized/directorFunctions/";
            _overloadedFunctions.parse(directorFunctionDir + "PNDirector.c",
                    true);
        } catch (IllegalActionException ex) {
            // Some API's may not have these files.
        }
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
                if (target.getExpression().equals("mpi")) {
                    // FIXME: Under the mpi target, we run "make run".
                    // This should be generalized.
                    commands.add("make -f " + _sanitizedModelName + ".mk run");
                } else {
                    String command = codeDirectory.stringValue()
                            + ((!codeDirectory.stringValue().endsWith("/") && !codeDirectory
                                    .stringValue().endsWith("\\")) ? "/" : "")
                            + _sanitizedModelName;

                    commands.add("\"" + command.replace('\\', '/') + "\"");
                }
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

        code = super._finalPassOverCode(code);

        if (((BooleanToken) sourceLineBinding.getToken()).booleanValue()) {

            String filename = getOutputFilename();
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

        includingFiles.add("<stdlib.h>"); // Sun requires stdlib.h for malloc

        if (isTopLevel()
                && ((BooleanToken) measureTime.getToken()).booleanValue()) {
            includingFiles.add("<sys/time.h>");
        }

        if (!isTopLevel()) {
            includingFiles.add("\"" + _sanitizedModelName + ".h\"");

            includingFiles.addAll(((CCodeGeneratorHelper) compositeActorHelper)
                    .getJVMHeaderFiles());
        }

        includingFiles.add("<stdarg.h>");
        includingFiles.add("<stdio.h>");
        includingFiles.add("<string.h>");

        for (String file : (Set<String>) includingFiles) {
            // Not all embedded platforms have all .h files.
            // For example, the AVR does not have time.h
            // FIXME: Surely we can control whether the files are
            // included more than once rather than relying on #ifndef!
            code.append("#ifndef PT_NO_"
                    + file.substring(1, file.length() - 3).replace('/', '_')
                            .toUpperCase() + "_H" + _eol + "#include " + file
                    + _eol + "#endif" + _eol);
        }

        return code.toString();
    }

    /** Generate the code for printing the execution time since
     *  the code generated by _recordStartTime() was called.
     *  @return Return the code for printing the total execution time.
     */
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
    protected String _recordStartTime() {
        StringBuffer startCode = new StringBuffer();
        startCode.append(super._recordStartTime());
        startCode.append("struct timespec start, end;\n" + "double dT = 0.0;\n"
                + "clock_gettime(CLOCK_REALTIME, &start);\n\n");
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
            substituteMap
                    .put("@PTCGIncludes@", _concatenateElements(_includes));
            String osName = StringUtilities.getProperty("os.name");
            substituteMap.put("@PTCGLibraries@",
                    _concatenateElements(_libraries));

            // Define substitutions to be used in the makefile
            substituteMap.put("@PTJNI_NO_CYGWIN@", "");
            substituteMap.put("@PTJNI_PLATFORM_LDFLAG@", " ");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "");
            substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "");
            if (((BooleanToken) generateCpp.getToken()).booleanValue()) {
                substituteMap.put("@PTCGCompiler@", "g++");
            } else {
                substituteMap.put("@PTCGCompiler@", "gcc");
            }

            if (osName != null) {
                // Keep these alphabetical
                if (osName.startsWith("Linux")) {
                    substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-shared");
                    // Need -fPIC for jni actors, see
                    // codegen/c/actor/lib/jni/test/auto/Scale.xml
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@", "-fPIC");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@", "-fPIC");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "lib");
                    substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "so");
                } else if (osName.startsWith("Mac OS X")) {
                    String widthFlag = "";
                    if (!JVMBitWidth.is32Bit()) {
                        widthFlag = "-m64 ";
                    }
                    substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-dynamiclib");
                    // Need when we call the plotter from generated C code.
                    substituteMap.put("@PTJNI_PLATFORM_LDFLAG@", widthFlag
                            + "-framework JavaVM -framework CoreFoundation");
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
                                    + "kernel/CCodeGenerator.java and "
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
        //URIAttribute uriAttribute = (URIAttribute) _model.getAttribute("_uri",
        //        URIAttribute.class);
        //if (uriAttribute != null) {
        URI uri = URIAttribute.getModelURI(_model);
        if (uri != null) {
            //uriAttribute.getURI().toString();
            String uriString = uri.toString();
            templateList.add(uriString.substring(0,
                    uriString.lastIndexOf("/") + 1)
                    + _sanitizedModelName
                    + ".mk.in");
        }
        // 2. If the target parameter is set, look for a makefile.
        String generatorDirectory = generatorPackage.stringValue().replace('.',
                '/');
        String targetValue = target.getExpression();
        templateList.add(generatorDirectory + "/targets/" + targetValue
                + "/makefile.in");

        // 3. Look for the generic C makefile.in
        // Note this code is repeated in the catch below.
        templateList.add(generatorDirectory
                + (isTopLevel() ? "/makefile.in" : "/jnimakefile.in"));

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
                System.out.println("Checking for " + makefileTemplateName);
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
     *  Set is separated by a space.
     *  @param collection  The collection of elements.
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

    /** Return true if the model contains a Placeable.
     *  @return true if the model contains a Placeable;
     */
    public boolean _hasPlaceable() {
        boolean hasPlaceable = false;
        Iterator atomicEntities = _model.allAtomicEntityList().iterator();

        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();

            if (object instanceof Placeable
                    && !(object instanceof ptolemy.actor.lib.gui.Display)) {
                hasPlaceable = true;
                break;
            }
        }
        return hasPlaceable;
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
    public void markFunctionCalled(String name, CCodeGeneratorHelper helper)
            throws IllegalActionException {

        try {
            String functionCode = _overloadedFunctions.getCodeBlock(name);

            if (!_overloadedFunctionSet.contains(name)) {

                String code = helper == null ? processCode(functionCode)
                        : helper.processCode(functionCode);

                _overloadedFunctions.append(code);

                _overloadedFunctionSet.add(name);
            }
            //            if (name.startsWith("Array_")) {
            //                // Array_xxx might need to have xxx added.
            //                // See c/actor/lib/test/auto/MultiplyDivide5.xml
            //
            //                // FIXME: this will add any function, which means that
            //                // if the user has Array_foo, foo is added.  Is this right?
            //                _tokenFuncUsed.add(name.substring(6));
            //            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to mark function called for \"" + name + "\" in \""
                            + getComponent().getFullName() + "\"");
        }

    }

    private CodeStream _overloadedFunctions;

    /** An ordered set of function code */
    LinkedHashSet<String> _overloadedFunctionSet;

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
        _scalarDeleteTypes.add("Int");
        _scalarDeleteTypes.add("Long");
    }
}
