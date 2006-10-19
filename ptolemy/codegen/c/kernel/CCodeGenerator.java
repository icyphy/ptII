package ptolemy.codegen.c.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.ActorCodeGenerator;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
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
         
            code.append("#define NUM_TYPE " + types.length + "\n");
            code.append("#define NUM_FUNC " + functions.length + "\n");
            code.append("Token (*functionTable[NUM_TYPE][NUM_FUNC])"
                    + "(Token, ...)= {\n");
    
            for (int i = 0; i < types.length; i++) {
                code.append("\t");
                for (int j = 0; j < functions.length; j++) {
                    code.append(types[i] + "_" + functions[j]);
                    if ((i != (types.length - 1))
                            || (j != (functions.length - 1))) {
                        code.append(", ");
                    }
                }
                code.append("\n");
            }
       
            code.append("};\n");
        }
        return code.toString();
    }
    
    /** Generate include files.
     *  @return The include files.
     *  @throws IllegalActionException If the helper class for some actor 
     *   cannot be found.
     */
    public String generateIncludeFiles() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ActorCodeGenerator compositeActorHelper = _getHelper(getContainer());
        Set includingFiles = compositeActorHelper.getHeaderFiles();

        if (!isTopLevel()) {
            includingFiles.add("\"Jni" + _sanitizedModelName + ".h\"");
            // FIXME: This is temporary. Only works on my machine.
            _includes.add("-I\"C:/Program Files/Java/jdk1.5.0_06/include\"");
            _includes.add("-I\"C:/Program Files/Java/jdk1.5.0_06/include/win32\"");
        }
        
        includingFiles.add("<stdarg.h>");
        includingFiles.add("<stdio.h>");
        includingFiles.add("<string.h>");
        Iterator files = includingFiles.iterator();

        while (files.hasNext()) {
            String file = (String) files.next();
        
            code.append("#include " + file + "\n");
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
            return "\n\ninitialize() {\n";
            
        // If the container is not in the top level, we are generating code 
        // for the Java and C co-simulation.
        } else {
            return "\n\nJNIEXPORT void JNICALL\n"
            + "Java_Jni" + _sanitizedModelName + "_initialize("
            + "JNIEnv *env, jobject obj) {\n";
        }
    }

    /** Generate the initialization procedure exit point.
     *  @return a string for the initialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeExitCode() throws IllegalActionException {
      
        return "}\n";
    }
    
    /** Generate the initialization procedure name.
     *  @return a string for the initialization procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeProcedureName() throws IllegalActionException {
      
        return _INDENT1 + "initialize();\n";
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
            mainEntryCode.append("\n\nmain(int argc, char *argv[]) {\n");
        
        // If the container is not in the top level, we are generating code 
        // for the Java and C co-simulation.
        } else {
            mainEntryCode.append("\n\nJNIEXPORT jobjectArray JNICALL\n"
                    + "Java_Jni" + _sanitizedModelName + "_fire (\n"
                    + "JNIEnv *env, jobject obj");
            
            Iterator inputPorts = ((Actor) getContainer()).inputPortList().iterator();
            while(inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                mainEntryCode.append(", jobjectArray " + inputPort.getName());
            }
            
            mainEntryCode.append("){\n");                               
        }
        return mainEntryCode.toString();
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {
   
        if (isTopLevel()) {
            return _INDENT1 + "exit(0);\n}\n";
        } else {
            return _INDENT1 + "return tokensToAllOutputPorts;\n}\n";
        }
    }
    
    /** Generate preinitialize code (if there is any).
     *  This method calls the generatePreinitializeCode() method
     *  of the code generator helper associated with the model director
     *  @return The preinitialize code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found, or if an error occurs when the director
     *   helper generates preinitialize code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        
        ptolemy.actor.Director director = ((CompositeActor) getContainer())
                .getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "The model "
                    + _model.getName() + " does not have a director.");
        }
       
        Attribute iterations = director.getAttribute("iterations");

        if (iterations != null) {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();

            if (iterationCount > 0) {
                code.append("static int iteration = 0;\n");
            }
        }

        return code.toString();
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
     * @throws IllegalActionException If an error occurrs when generating
     *  the type resolution code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the type resolution code.
     */
    public String generateTypeConvertCode() throws IllegalActionException {
     
        StringBuffer code = new StringBuffer();

        code.append(comment(0, "Generate type resolution code for "
                + getContainer().getFullName()));

        // Include the constantsBlock at the top so that sharedBlocks from
        // actors can use true and false etc.  StringMatches needs this.
        CodeStream sharedStream = new CodeStream(
                "$CLASSPATH/ptolemy/codegen/kernel/SharedCode.c");
        sharedStream.appendCodeBlock("constantsBlock");
        code.append(sharedStream.toString());

        // Determine the total number of referenced polymorphic functions.
        HashSet functions = new HashSet();
        functions.add("delete");
        //functions.add("toString");    // for debugging.
        functions.add("convert");
        functions.addAll(_typeFuncUsed);
        functions.addAll(_tokenFuncUsed);

        // Determine the total number of referenced types.
        HashSet types = new HashSet();
        if (functions.contains("equals")) {
            types.add("Boolean");
        }
        if (functions.contains("toString")) {
            types.add("String");
        }
        types.addAll(_newTypesUsed);

        Object[] typesArray = types.toArray();
        CodeStream[] typeStreams = new CodeStream[types.size()];

        // Generate type map.
        String typeMembers = "";
        code.append("#define TYPE_Token -1 \n");
        for (int i = 0; i < typesArray.length; i++) {
            // Open the .c file for each type.
            typeStreams[i] = new CodeStream(
                    "$CLASSPATH/ptolemy/codegen/kernel/type/" + typesArray[i]
                            + ".c");

            code.append("#define TYPE_" + typesArray[i] + " " + i + "\n");

            // Dynamically generate all the types within the union.
            typeMembers += "\t\t" + typesArray[i] + "Token " + typesArray[i]
                    + ";\n";
        }

        Object[] functionsArray = functions.toArray();

        // Generate function map.
        for (int i = 0; i < functionsArray.length; i++) {
   
            code.append("#define FUNC_" + functionsArray[i] + " " + i + "\n");
        }
      
        code.append("typedef struct token Token;");

        // Generate type and function definitions.
        for (int i = 0; i < typesArray.length; i++) {
            // The "declareBlock" contains all necessary declarations for the
            // type; thus, it is always read into the code stream when
            // accessing this particular type.
            typeStreams[i].appendCodeBlock("declareBlock");
            code.append(typeStreams[i].toString());
        }

        ArrayList args = new ArrayList();
        args.add("");
        // Token declareBlock.
        if (!typeMembers.equals("")) {
            args.set(0, typeMembers);
            sharedStream.clear();
            sharedStream.appendCodeBlock("tokenDeclareBlock", args);
        }

        // Append type-polymorphic functions included in the function table. 
        for (int i = 0; i < types.size(); i++) {
            // The "funcDeclareBlock" contains all function declarations for
            // the type.
            for (int j = 0; j < functionsArray.length; j++) {
                args.set(0, typesArray[i] + "_" + functionsArray[j]);
                sharedStream.appendCodeBlock("funcHeaderBlock", args);
            }
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
            typeStreams[i].appendCodeBlock("newBlock");

            for (int j = 0; j < functionsArray.length; j++) {
                // The code block declaration has to follow this convention:
                // /*** [function name]Block ***/ 
                //     .....
                // /**/
                try {
                    typeStreams[i].appendCodeBlock(functionsArray[j] + "Block");
                } catch (IllegalActionException ex) {
                    // We have to catch the exception if some code blocks are
                    // not found. We have to define the function label in the
                    // generated code because the function table makes
                    // reference to this label.
             
                    typeStreams[i].append("#define " + typesArray[i] + "_"
                            + functionsArray[j] + " MISSING \n");

                    // It is ok because this polymorphic function may not be
                    // supported by all types. 
                }
            }
            code.append(typeStreams[i].toString());
        }
        return code.toString();
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
        if (_modifiedVariables != null) {
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                Parameter parameter = (Parameter) modifiedVariables.next();

                code.append("static "
                        + CodeGeneratorHelper.cType(parameter.getType()) + " "
                        + CodeGeneratorHelper.generateVariableName(parameter)
                        + ";\n");
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
        if (_modifiedVariables != null) {
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                Parameter parameter = (Parameter) modifiedVariables.next();

                NamedObj container = parameter.getContainer();
                CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);
                code.append(CodeGeneratorHelper.generateVariableName(parameter)
                        + " = "
                        + containerHelper.getParameterValue(
                                parameter.getName(), parameter.getContainer())
                        + ";\n");
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
            return "\n\nwrapup() {\n";
            
        // If the container is not in the top level, we are generating code 
        // for the Java and C co-simulation.
        } else {
            return "\n\nJNIEXPORT void JNICALL\n"
            + "Java_Jni" + _sanitizedModelName + "_wrapup("
            + "JNIEnv *env, jobject obj) {\n";
        }
    }

    /** Generate the wrapup procedure exit point.
     *  @return a string for the wrapup procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupExitCode() throws IllegalActionException {
       
        return "}\n";
    }
    
    /** Generate the wrapup procedure name.
     *  @return a string for the wrapup procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupProcedureName() throws IllegalActionException {
     
        return _INDENT1 + "wrapup();\n";
    }
    
}
