/* Base class for code generators.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.codegen.c.actor.TypedCompositeActor;
import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.copernicus.kernel.Copernicus;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// CodeGenerator

/** Base class for code generator.
 *  
 *  @author Edward A. Lee, Gang Zhou, Ye Zhou, Contributors: Christopher Brooks
 *  @version $Id$ CodeGenerator.java,v 1.51 2005/07/13 14:07:20 cxh Exp $
 *  @since Ptolemy II 5.1
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGenerator extends Attribute implements ComponentCodeGenerator {
    /** Create a new instance of the code generator.
     *  @param container The container.
     *  @param name The name of the code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public CodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        codeDirectory = new FileParameter(this, "codeDirectory");

        codeDirectory.setExpression("$HOME/codegen/");

        // FIXME: This should not be necessary, but if we don't
        // do it, then getBaseDirectory() thinks we are in the current dir.
        codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());
        new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

        compile = new Parameter(this, "compile");
        compile.setTypeEquals(BaseType.BOOLEAN);
        compile.setExpression("true");

        generatorPackage = new StringParameter(this, "generatorPackage");
        generatorPackage.setExpression("ptolemy.codegen.c");

        inline = new Parameter(this, "inline");
        inline.setTypeEquals(BaseType.BOOLEAN);
        inline.setExpression("true");

        overwriteFiles = new Parameter(this, "overwriteFiles");
        overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
        overwriteFiles.setExpression("true");

        run = new Parameter(this, "run");
        run.setTypeEquals(BaseType.BOOLEAN);
        run.setExpression("true");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        _model = (CompositeEntity) getContainer();
        _sanitizedModelName = StringUtilities.sanitizeName(_model.getName());

        // FIXME: We may not want this GUI dependency here...
        // This attribute could be put in the MoML in the library instead
        // of here in the Java code.
        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/codegen.
     */
    public FileParameter codeDirectory;

    /** If true, then compile the generated code. The default   
     *  value is a parameter with the value true.
     */
    public Parameter compile;

    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;

    /** If true, generate file with no functions.  If false, generate
     *  file with functions. The default value is a parameter with the 
     *  value true.
     */
    public Parameter inline;

    /** If true, overwrite preexisting files.  The default
     *  value is a parameter with the value true.
     */
    public Parameter overwriteFiles;

    /** If true, then run the generated code. The default   
     *  value is a parameter with the value true.
     */
    public Parameter run;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a formatted comment containing the
     *  specified string. In this base class, the
     *  comments is a C-style comment, which begins with
     *  "\/*" and ends with "*\/". Subclasses may override this
     *  produce comments that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
        return "/* " + comment + " */\n";
    }

    /** Generate the body code that lies between initialize and wrapup.
     *  In this base class, nothing is generated.
     *  @return The empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateBodyCode() throws IllegalActionException {
        return "";
    }

    /** Generate code and write it to the file specified by the
     *  <i>codeDirectory</i> parameter.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public void generateCode() throws KernelException {
        generateCode(new StringBuffer());
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
     *  @param code The given string buffer.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public int generateCode(StringBuffer code) throws KernelException {

        boolean inline = ((BooleanToken) this.inline.getToken()).booleanValue();

        // perform port type conversion. This has to be before generation
        // of other codes.
        _checkPortTypeConversion();

        // We separate the generation and the appending into 2 phases.
        // This would be convenience for making addition passes, and
        // for adding additional code into different sections.
        String sharedCode = generateSharedCode();
        String includeFiles = generateIncludeFiles();
        String preinitializeCode = generatePreinitializeCode();      
        String initializeCode = generateInitializeCode();
        String bodyCode = generateBodyCode();
        String fireFunctionCode = null;
        if (!inline) {
            fireFunctionCode = generateFireFunctionCode();
        }
        String wrapupCode = generateWrapupCode();

        String variableDeclareCode = generateVariableDeclaration();
        String variableInitCode = generateVariableInitialization();
        // generate type resolution code has to be after 
        // fire(), wrapup(), preinit(), init()...
        String typeResolutionCode = generateTypeResolutionCode();

        // The appending phase.
        code.append(includeFiles);
        code.append(sharedCode);
        code.append(typeResolutionCode);      
        code.append(variableDeclareCode);
        code.append(preinitializeCode);
        if (!inline) {
            code.append(fireFunctionCode);
        }
        code.append("\n\nmain(int argc, char *argv[]) {\n");
        code.append(variableInitCode);
        code.append(initializeCode);
        code.append(bodyCode);
        code.append(wrapupCode);
        code.append("exit(0);\n");
        code.append("}\n");

        if (_executeCommands == null) {
            _executeCommands = new StreamExec();
        }

        _writeCode(code);
        _writeMakefile();
        return _executeCommands();
    }

    /** Generate code for a model.
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @return The return value of the last subprocess that was run
     *  to compile or run the model.  Return -1 if called  with no arguments.
     *  Return -2 if no CodeGenerator was created.
     *  @exception Exception If any error occurs.
     */
    public static int generateCode(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.codegen.kernel.CodeGenerator model.xml "
                    + "[model.xml . . .]\n"
                    + "  The arguments name MoML files containing models");
            return -1;
        }

        CodeGenerator codeGenerator = null;

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        parser.setMoMLFilters(BackwardCompatibility.allFilters());
        parser.addMoMLFilter(new RemoveGraphicalClasses());

        for (int i = 0; i < args.length; i++) {
            // Note: the code below uses explicit try catch blocks
            // so we can provide very clear error messages about what
            // failed to the end user.  The alternative is to wrap the
            // entire body in one try/catch block and say
            // "Code generation failed for foo", which is not clear.
            URL modelURL;

            try {
                modelURL = new File(args[i]).toURL();
            } catch (Exception ex) {
                throw new Exception("Could not open \"" + args[i] + "\"", ex);
            }

            CompositeActor toplevel = null;

            try {
                try {
                    toplevel = (CompositeActor) parser.parse(null, modelURL);
                } catch (Exception ex) {
                    throw new Exception("Failed to parse \"" + args[i] + "\"",
                            ex);
                }

                // Get all instances of this class contained in the model
                List codeGenerators = toplevel
                        .attributeList(CodeGenerator.class);

                if (codeGenerators.size() == 0) {
                    // Add a codeGenerator
                    codeGenerator = new StaticSchedulingCodeGenerator(toplevel,
                            "CodeGenerator_AutoAdded");
                } else {
                    // Get the last CodeGenerator in the list, maybe
                    // it was added last?
                    codeGenerator = (CodeGenerator) codeGenerators
                            .get(codeGenerators.size() - 1);
                }

                try {
                    codeGenerator.generateCode();
                } catch (KernelException ex) {
                    throw new Exception("Failed to generate code for \""
                            + args[i] + "\"", ex);
                }
            } finally {
                // Destroy the top level so that we avoid
                // problems with running the model after generating code
                if (toplevel != null) {
                    toplevel.setContainer(null);
                }
            }
        }
        if (codeGenerator != null) {
            return codeGenerator.getExecuteCommands()
                    .getLastSubprocessReturnCode();
        }
        return -2;
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a 
     *  function with the same name as that of the actor.
     * 
     *  @return The fire function code of the containing composite actor.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());
        code.append(compositeActorHelper.generateFireFunctionCode());
        return code.toString();
    }

    /** Generate include files.
     *  @return The include files.
     *  @throws IllegalActionException If the helper class for some actor 
     *   cannot be found.
     */
    public String generateIncludeFiles() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());
        Set includingFiles = compositeActorHelper.getHeaderFiles();

        Iterator files = includingFiles.iterator();

        while (files.hasNext()) {
            String file = (String) files.next();
            code.append("#include " + file + "\n");
        }

        return code.toString();
    }

    /**
     * Return the code associated with initialization of the containing
     * composite actor. This method calls the generateInitializeCode()
     * method of the code generator helper associated with the model director.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If the helper class for the model
     *  director cannot be found or if an error occurs when the director
     *  helper generates initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("\nInitialize " + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());
        code.append(compositeActorHelper.generateInitializeCode());
        return code.toString();
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
        ptolemy.actor.Director director = ((CompositeActor) getContainer())
                .getDirector();

        if (director == null) {
            throw new IllegalActionException(this, "The model "
                    + _model.getName() + " does not have a director.");
        }

        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());

        _modifiedVariables = compositeActorHelper.getModifiedVariables();

        code.append(compositeActorHelper.generatePreinitializeCode());

        code.append(compositeActorHelper.createOffsetVariablesIfNeeded());

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
     * Generate code shared by helper actors, including globally defined
     * data struct types and static methods or variables shared by multiple
     * instances of the same helper actor type.
     * @return The shared code of the containing composite actor.
     * @throws IllegalActionException If an error ocurrs when generating
     *  the globally shared code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the shared code.
     */
    public String generateSharedCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Generate shared code for "
                + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());

        Set sharedCodeBlocks = compositeActorHelper.generateSharedCode();

        Iterator blocks = sharedCodeBlocks.iterator();

        while (blocks.hasNext()) {
            String block = (String) blocks.next();
            code.append(block);
        }

        code.append(comment("Finished generate shared code for "
                + getContainer().getFullName()));

        return code.toString();
    }

    /**
     * Generate type resolution code.
     * Determine the proper code put into the source to support dynamic type
     * resolution. First, find out the different types used in the model.
     * Second, find out the different polymorphic functions used. (note: types
     * and functions are independent of each other). Third, append code blocks
     * according to the functions used, and read from files according to the
     * types referenced. Fourth, generate type resolution code, which consists
     * of constants (MAX_NUM_TYPE, MAX_NUM_FUNC), the type map, the function
     * map, function definitions read from the files, and function table.
     * @return The type resolution code.
     * @throws IllegalActionException If an error ocurrs when generating
     *  the type resolution code, or if the helper class for the model
     *  director cannot be found, or if an error occurs when the helper
     *  actor generates the type resolution code.
     */
    public String generateTypeResolutionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Generate type resolution code for "
                + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());

        //Iterator actors = ((ptolemy.actor.CompositeActor) compositeActorHelper
        //        .getComponent()).deepEntityList().iterator();

        CodeStream sharedStream = new CodeStream(
                "$CLASSPATH/ptolemy/codegen/kernel/SharedCode.c");
        sharedStream.appendCodeBlock("constantsBlock");
        code.append(sharedStream.toString());

        // Determine the total number of referenced types.
        // Determine the total number of referenced polymorphic functions.
        HashSet functions = new HashSet();
        HashSet types = new HashSet();
        types.addAll(_primitiveTypes);

        /*
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            CodeGeneratorHelper helperObject = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            functions.addAll(helperObject._typeFuncUsed);
            types.addAll(helperObject._newTypesUsed);
        }
        */
        
        functions.addAll(_typeFuncUsed);
        types.addAll(_newTypesUsed);
        // The constructor of Array requires calling the convert function.  
        if (types.contains("Array")) {
            functions.add("convert");
        }

        Object[] typesArray = types.toArray();
        CodeStream[] typeStreams = new CodeStream[types.size()];

        // Generate type map.
        String typeMembers = new String();
        for (int i = 0; i < types.size(); i++) {
            // Open the .c file for each type.
            typeStreams[i] = new CodeStream(
                    "$CLASSPATH/ptolemy/codegen/kernel/type/" + typesArray[i]
                            + ".c");
            // FIXME: we need to compute the [partial] order of the hierarchy. 
            code.append("#define TYPE_" + typesArray[i] + " " + i + "\n");

            // Dynamically generate all the types within the union.
            typeMembers += "\t\t" + typesArray[i] + "Token " + typesArray[i]
                    + ";\n";
        }

        Object[] functionsArray = functions.toArray();

        // Generate function map.
        for (int i = 0; i < functions.size(); i++) {
            code.append("#define FUNC_" + functionsArray[i] + " " + i + "\n");
        }
        code.append("typedef struct token Token;");

        // Generate type and function definitions.
        for (int i = 0; i < types.size(); i++) {
            // The "declareBlock" contains all necessary declarations for the
            // type; thus, it is always read into the code stream when
            // accessing this particular type.
            typeStreams[i].appendCodeBlock("declareBlock");
            code.append(typeStreams[i].toString());
        }

        // Token declareBlock.
        if (!typeMembers.equals("")) {
            ArrayList args = new ArrayList();
            args.add(typeMembers);
            sharedStream.clear();
            sharedStream.appendCodeBlock("tokenDeclareBlock", args);
            code.append(sharedStream.toString());
        }

        for (int i = 0; i < types.size(); i++) {
            // The "funcDeclareBlock" contains all function declarations for
            // the type.
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

        // Generate function table.
        if (functions.size() > 0 && types.size() > 0) {
            code.append("#define NUM_TYPE " + types.size() + "\n");
            code.append("#define NUM_FUNC " + functions.size() + "\n");
            code.append("Token (*functionTable"
                    + "[NUM_TYPE][NUM_FUNC])(Token)= {\n");
            for (int i = 0; i < types.size(); i++) {
                code.append("\t");
                for (int j = 0; j < functions.size(); j++) {
                    code.append(typesArray[i] + "_" + functionsArray[j]);
                    if ((i != (types.size() - 1))
                            || (j != (functions.size() - 1))) {
                        code.append(", ");
                    }
                }
                code.append("\n");
            }
            code.append("};\n");
        }

        for (int i = 0; i < types.size(); i++) {
            typeStreams[i].clear();
            typeStreams[i].appendCodeBlock("newBlock");

            for (int j = 0; j < functions.size(); j++) {
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
        code.append("\n\n");
        code.append(comment("Variable Declarations "
                + getContainer().getFullName()));
        
        // Generate variable declarations for modified variables.
        if (_modifiedVariables != null) {
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                Parameter parameter = (Parameter) modifiedVariables.next();

                code.append("static " 
                        + CodeGeneratorHelper._generateType(parameter.getType())
                        + " " + CodeGeneratorHelper.generateVariableName(parameter) 
                        + ";\n");
            }
        }
        
        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());

        code.append(compositeActorHelper.generateVariableDeclaration());
        return code.toString();
    }
    
    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append("\n\n");
        code.append(comment("Variable initialization "
                + getContainer().getFullName()));
        
        // Generate variable initialization for modified variables.
        if (_modifiedVariables != null) {
            Iterator modifiedVariables = _modifiedVariables.iterator();
            while (modifiedVariables.hasNext()) {
                Parameter parameter = (Parameter) modifiedVariables.next();

                NamedObj container = parameter.getContainer();
                CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);
                code.append(CodeGeneratorHelper.generateVariableName(parameter)
                        + " = " 
                        + containerHelper.getParameterValue(parameter.getName(), parameter.getContainer())
                        + ";\n");
            }
        }

        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());

        code.append(compositeActorHelper.generateVariableInitialization());
        return code.toString();
    }

    /** Generate into the specified code stream the code associated with
     *  wrapping up the container composite actor. This method calls the
     *  generateWrapupCode() method of the code generator helper associated
     *  with the director of this container.
     *  @return The wrapup code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(comment("Wrapup " + getContainer().getFullName()));

        TypedCompositeActor compositeActorHelper = (TypedCompositeActor) _getHelper(getContainer());
        code.append(compositeActorHelper.generateWrapupCode());
        return code.toString();
    }

    /** Return the associated component, which is always the container.
     *  @return The helper to generate code.
     */
    public NamedObj getComponent() {
        return getContainer();
    }

    /** Get the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @return executeCommands The subprocess command executor.
     *  @see #setExecuteCommands(ExecuteCommands)
     */
    public ExecuteCommands getExecuteCommands() {
        return _executeCommands;
    }

    /** Generate code for a model.
     *  <p>For example:
     *  <pre>
     *  java -classpath $PTII ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *  or
     *  <pre>
     *  $PTII/bin/ptinvoke ptolemy.codegen.kernel.CodeGenerator $PTII/ptolemy/codegen/c/actor/lib/test/auto/Ramp.xml
     *  </pre>
     *  @param args An array of Strings, each element names a MoML file
     *  containing a model.
     *  @exception Exception If any error occurs.
     */
    public static void main(String[] args) throws Exception {
        generateCode(args);
    }

    /** This method is used to set the code generator for a helper class.
     *  Since this is not a helper class for a component, this method does
     *  nothing.
     *  @param codeGenerator The given code generator. 
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
    }

    /** Set the command executor, which can be either non-graphical
     *  or graphical.  The initial default is non-graphical, which
     *  means that stderr and stdout from subcommands is written
     *  to the console.
     *  @param executeCommands The subprocess command executor.
     *  @see #getExecuteCommands()
     */
    public void setExecuteCommands(ExecuteCommands executeCommands) {
        _executeCommands = executeCommands;
    }

    /** Set the container of this object to be the given container.
     *  @param container The given container.
     *  @exception IllegalActionException If the given container
     *   is not null and not an instance of CompositeEntity.
     *  @exception NameDuplicationException If there already exists a
     *   container with the same name.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if ((container != null) && !(container instanceof CompositeEntity)) {
            throw new IllegalActionException(this, container,
                    "CodeGenerator can only be contained"
                            + " by CompositeEntity");
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Check for port type conversion between actors and record the conversion
     * needed in the helper's info table. The helper would knows how to convert
     * its input ports from the recorded information. If source ports and the
     * helper's input ports are of different types, then conversion is needed.
     * (i.e. if a source port is a primitive type and the input port is a 
     * general type, we need to upgrade the source type to a Token type.) 
     * @exception IllegalActionException Thrown if helper is not found, or the conversion not
     *  supported.
     */
    protected void _checkPortTypeConversion() throws IllegalActionException {

        // FIXME: This method only applies to flat model (one hierarchy) . 
        Iterator actors = ((ptolemy.actor.CompositeActor) _getHelper(
                getContainer()).getComponent()).deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            //CodeGeneratorHelper helper = 
            //    (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            for (int i = 0; i < actor.inputPortList().size(); i++) {
                TypedIOPort inputPort = ((TypedIOPort) actor.inputPortList()
                        .get(i));

                // j is the source port channel index in the input port.
                for (int j = 0; j < inputPort.sourcePortList().size(); j++) {
                    TypedIOPort sourcePort = ((TypedIOPort) inputPort
                            .sourcePortList().get(j));

                    if (!inputPort.getType().equals(sourcePort.getType())) {
                        String sourceType = CodeGeneratorHelper
                                ._getCodeGenTypeFromPtolemyType(sourcePort
                                        .getType());
                        String targetType = CodeGeneratorHelper
                                ._getCodeGenTypeFromPtolemyType(inputPort
                                        .getType());

                        CodeGeneratorHelper sourceHelper = (CodeGeneratorHelper) _getHelper(sourcePort
                                .getContainer());

                        // Record the needed inter-actor type conversions.
                        // **Source port reference name is uniquely identified
                        // by "[NAME]#[INDEX]", its name and its channel#.
                        String refName = sourcePort.getName();
                        // FIXME: 1. inputPort.sourcePortList() returns a list 
                        // of source ports. The API does not say the 1st source
                        // port in the list connects to the 1st channel of input 
                        // port, the 2nd to the 2nd, etc. _getChannelIndex(inputPort, j,
                        // sourcePort) uses this assumption which is not guaranteed.
                        // 2. It does not consider the case that the same channel
                        // of the input port may be connected to more than one source
                        // port, e.g., in modal model.
                        refName += "#"
                                + sourceHelper._getChannelIndex(inputPort, j,
                                        sourcePort);

                        if (targetType.equals("Token")) {
                            // Record the referenced type in the infoTable.
                            _newTypesUsed.add(sourceType);
                            sourceHelper._portConversions.put(refName,
                                    sourceType + "_new");
                            sourceHelper._portDeclareTypes
                                    .put(refName, "Token");
                        } else if (targetType.equals("String")) {
                            if (sourceType.equals("Int")) {
                                sourceHelper._portConversions.put(refName,
                                        "itoa");
                            } else if (sourceType.equals("Long")) {
                                sourceHelper._portConversions.put(refName,
                                        "ltoa");
                            } else if (sourceType.equals("Double")) {
                                sourceHelper._portConversions.put(refName,
                                        "ftoa");
                            } else if (sourceType.equals("Boolean")) {
                                sourceHelper._portConversions.put(refName,
                                        "btoa");
                            } else {
                                throw new IllegalActionException(
                                        "Port type conversion not handled "
                                                + "-- from " + sourceType
                                                + " to " + targetType + ".\n");
                            }
                            sourceHelper._portDeclareTypes
                                    .put(refName, "char*");
                        } else if (targetType.equals("Double")) {
                            if (sourceType.equals("Int")) {
                                // C would take care of converting
                                // from int to double.
                            } else {
                                throw new IllegalActionException(
                                        "Port type conversion not handled "
                                                + "-- from " + sourceType
                                                + " to " + targetType + ".\n");
                            }
                        } else if (targetType.equals("Array")) {

                        } else {

                            // FIXME: we may have to handle other port
                            // conversion types.

                            //throw new IllegalActionException(
                            //    "Port type conversion not handled -- from "
                            //    + sourceType + " to " + targetType + ".\n");
                            throw new IllegalActionException(
                                    "Port type conversion not handled -- from "
                                            + sourceType + " to " + targetType
                                            + ".\n");
                        }
                    }
                }
            }
        }
    }

    /** Get the code generator helper associated with the given component.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        if (_helperStore.containsKey(component)) {
            return (ComponentCodeGenerator) _helperStore.get(component);
        }

        String packageName = generatorPackage.stringValue();

        String componentClassName = component.getClass().getName();
        String helperClassName = componentClassName.replaceFirst("ptolemy",
                packageName);

        Class helperClass = null;

        try {
            helperClass = Class.forName(helperClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, e,
                    "Cannot find helper class " + helperClassName);
        }

        Constructor constructor = null;

        try {
            constructor = helperClass.getConstructor(new Class[] { component
                    .getClass() });
        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(this, e,
                    "There is no constructor in " + helperClassName
                            + " which accepts an instance of "
                            + componentClassName + " as the argument.");
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { component });
        } catch (Exception e) {
            throw new IllegalActionException(component, e,
                    "Failed to create helper class code generator.");
        }

        if (!(helperObject instanceof ComponentCodeGenerator)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this component: " + component
                            + ". Its helper class does not"
                            + " implement componentCodeGenerator.");
        }

        ComponentCodeGenerator castHelperObject = (ComponentCodeGenerator) helperObject;

        castHelperObject.setCodeGenerator(this);

        _helperStore.put(component, helperObject);

        return castHelperObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A set that contains all variables in the model whose values can be 
     *  changed during execution.
     */
    protected Set _modifiedVariables;
    
    /** A HashSet that contains all codegen types referenced in the model.
     * When the codegen kernel processes a $new() macro, it would add the
     * codegen type to this set. Codegen types are supported by the code
     * generator package. (e.g. Int, Double, Array, and etc.)
     */
    protected HashSet _newTypesUsed = new HashSet();
    
    /** 
     * A static list of all primitive types supported by the code generator. 
     */
    protected static List _primitiveTypes = Arrays.asList(new String[] { "Int",
            "Double", "String", "Boolean" });

    /** A HashSet that contains all type functions referenced in the model.
     *  When the codegen kernel processes a $typeFunc() macro, it would add
     *  the type function to this set. 
     */
    protected HashSet _typeFuncUsed = new HashSet();
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory.
     *  @return The return value of the last subprocess that was executed
     *  or -1 if no commands were executed.
     */
    private int _executeCommands() throws IllegalActionException {

        List commands = new LinkedList();
        if (((BooleanToken) compile.getToken()).booleanValue()) {
            commands.add("make -f " + _sanitizedModelName + ".mk");
        }
        
        if (((BooleanToken) compile.getToken()).booleanValue()) {
            String command = codeDirectory.stringValue()
                + ((!codeDirectory.stringValue().endsWith("/") && !codeDirectory
                           .stringValue().endsWith("\\")) ? "/" : "")
                + _sanitizedModelName;

            commands.add("\"" + command.replace('\\', '/') + "\"");
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
                errorMessage.append((String) allCommands.next() + "\n");
            }
            throw new IllegalActionException("Problem executing the "
                    + "commands:\n" + errorMessage);
        }
        return _executeCommands.getLastSubprocessReturnCode();
    }

    /** Write the code to a directory named by the codeDirectory
     *  parameter, with a file name that is a sanitized version of the
     *  model name, and an extension that is the last package of
     *  the generatorPackage.
     *  @param code The StringBuffer containing the code.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    private void _writeCode(StringBuffer code) throws IllegalActionException {
        // This method is private so that the body of the caller shorter.

        String extension = generatorPackage.stringValue().substring(
                generatorPackage.stringValue().lastIndexOf("."));

        String codeFileName = _sanitizedModelName + extension;

        // Write the code to a file with the same name as the model into
        // the directory named by the codeDirectory parameter.
        try {
            File codeDirectoryFile = codeDirectory.asFile();
            if (codeDirectoryFile.isFile()) {
                throw new IOException("Error: " + codeDirectory.stringValue()
                        + " is a file, " + "it should be a directory.");
            }
            if (!codeDirectoryFile.isDirectory() && !codeDirectoryFile.mkdirs()) {
                throw new IOException("Failed to make the \""
                        + codeDirectory.stringValue() + "\" directory.");
            }

            // FIXME: Note that we need to make the directory before calling
            // getBaseDirectory()
            codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());

            _executeCommands.stdout("Writing " + codeFileName + " in "
                    + codeDirectory.getBaseDirectory());

            // Check if needs to overwrite.
            if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
                    && codeDirectory.asFile().exists()) {
                // FIXME: It is totally bogus to ask a yes/no question
                // like this, since it makes it impossible to call
                // this method from a script.  If the question is
                // asked, the build will hang.
                if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                        + " exists. OK to overwrite?")) {
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                }
            }

            Writer writer = null;
            try {
                writer = FileUtilities.openForWriting(codeFileName,
                        codeDirectory.getBaseDirectory(), false);
                writer.write(code.toString());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "Failed to write \""
                    + codeFileName + "\" in "
                    + codeDirectory.getBaseDirectory());
        }
    }

    /** Read in a template makefile, substitute variables and write
     *  the resulting makefile.
     *
     *  <p>The makefile template can be found by looking up a resource
     *  name makefile.in in the package named by the 
     *  <i>generatorPackage</i> parameter.  Thus, if the 
     *  <i>generatorPackage</i> has the value "ptolemy.codegen.c",
     *  then we look for the resouce "ptolemy.codegen.c.makefile.in"
     *  <p>
     *  <p>The makefile is written to a directory named by the codeDirectory
     *  parameter, with a file name that is a sanitized version of the
     *  model name, and a ".mk" extension.  Thus, for a model named "Foo",
     *  we might generate a makefile in "$HOME/codegen/Foo.mk".

     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    private void _writeMakefile() throws IllegalActionException {

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
                throw new IllegalActionException(this,
                        "Please select another file name.");
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
            substituteMap = Copernicus.newMap(this);
            substituteMap.put("@modelName@", _sanitizedModelName);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Problem generating substitution map from " + _model);
        }

        BufferedReader makefileTemplateReader = null;

        String makefileTemplateName = generatorPackage.stringValue().replace(
                '.', '/')
                + "/makefile.in";

        // If necessary, add a trailing / after codeDirectory.
        String makefileOutputName = codeDirectory.stringValue()
                + ((!codeDirectory.stringValue().endsWith("/") && !codeDirectory
                        .stringValue().endsWith("\\")) ? "/" : "")
                + _sanitizedModelName + ".mk";

        try {
            try {
                makefileTemplateReader = Copernicus
                        .openAsFileOrURL(makefileTemplateName);
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex, "Failed to open \""
                        + makefileTemplateName + "\" for reading.");
            }

            _executeCommands.stdout("Reading \"" + makefileTemplateName
                    + "\", writing \"" + makefileOutputName + "\"");
            Copernicus.substitute(makefileTemplateReader, substituteMap,
                    makefileOutputName);
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
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A hash map that stores the code generator helpers associated
     *  with the actors.
     */
    private HashMap _helperStore = new HashMap();

    private ExecuteCommands _executeCommands;

    /** The model we for which we are generating code. */
    CompositeEntity _model;

    /** The sanitized model name. */
    String _sanitizedModelName;

}
