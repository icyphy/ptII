/* Base class for code generators.

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
package ptolemy.codegen.kernel;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.lib.jni.PointerToken;
import ptolemy.codegen.actor.Director;
import ptolemy.codegen.gui.CodeGeneratorGUIFactory;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.UndefinedConstantOrIdentifierException;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CodeGenerator

/** Base class for code generator.
 *
 *  @author Edward A. Lee, Gang Zhou, Ye Zhou, Contributors: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 6.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGenerator extends Attribute implements ComponentCodeGenerator {
    /** A FilenameFilter that looks for directories other than CVS.
     */
    private static class TargetFilter implements FilenameFilter {
        /** Tests if the specified file in a directory is a directory
         *  other than "CVS".
         *  @param dir The directory in which the file is contained.
         *  @param name The name of the file in question.
         *  @return true if the file is a directory with a name other
         *  than <code>CVS</code>
         */
        public boolean accept(File dir, String name) {
            if (name.equals("CVS")) {
                return false;
            }
            File file = new File(dir, name);
            return file.isDirectory();
        }
    }

    // Note: If you add publicly settable parameters, update
    // _commandFlags or _commandOptions.

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

        // Note: If you add publicly settable parameters, update
        // _commandFlags or _commandOptions.

        allowDynamicMultiportReference = new Parameter(this,
                "allowDynamicMultiportReference");
        allowDynamicMultiportReference.setTypeEquals(BaseType.BOOLEAN);
        allowDynamicMultiportReference.setExpression("false");

        codeDirectory = new FileParameter(this, "codeDirectory");
        codeDirectory.setExpression("$HOME/codegen/");

        // FIXME: This should not be necessary, but if we don't
        // do it, then getBaseDirectory() thinks we are in the current dir.
        try {
            codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());
        } catch (IllegalActionException ex) {
            _sandboxVariableCheck(ex);
        }

        new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

        compile = new Parameter(this, "compile");
        compile.setTypeEquals(BaseType.BOOLEAN);
        compile.setExpression("true");

        compileTarget = new StringParameter(this, "compileTarget");

        generatorPackage = new StringParameter(this, "generatorPackage");

        generateComment = new Parameter(this, "generateComment");
        generateComment.setTypeEquals(BaseType.BOOLEAN);
        generateComment.setExpression("true");

        generateCpp = new Parameter(this, "generateCpp");
        generateCpp.setTypeEquals(BaseType.BOOLEAN);
        generateCpp.setExpression("false");

        generateEmbeddedCode = new Parameter(this, "generateEmbeddedCode");
        generateEmbeddedCode.setTypeEquals(BaseType.BOOLEAN);
        generateEmbeddedCode.setExpression("true");
        // Hide the embeddedCode parameter from the user.
        generateEmbeddedCode.setVisibility(Settable.EXPERT);

        inline = new Parameter(this, "inline");
        inline.setTypeEquals(BaseType.BOOLEAN);
        inline.setExpression("false");

        measureTime = new Parameter(this, "measureTime");
        measureTime.setTypeEquals(BaseType.BOOLEAN);
        measureTime.setExpression("false");

        overwriteFiles = new Parameter(this, "overwriteFiles");
        overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
        overwriteFiles.setExpression("true");

        padBuffers = new Parameter(this, "padBuffers");
        padBuffers.setTypeEquals(BaseType.BOOLEAN);
        padBuffers.setExpression("true");

        run = new Parameter(this, "run");
        run.setTypeEquals(BaseType.BOOLEAN);
        run.setExpression("true");

        sourceLineBinding = new Parameter(this, "sourceLineBinding");
        sourceLineBinding.setTypeEquals(BaseType.BOOLEAN);
        sourceLineBinding.setExpression("false");

        target = new StringParameter(this, "target");
        target.setExpression("default");
        _updateTarget();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");

        _model = (CompositeEntity) getContainer();

        // FIXME: We may not want this GUI dependency here...
        // This attribute could be put in the MoML in the library instead
        // of here in the Java code.
        new CodeGeneratorGUIFactory(this, "_codeGeneratorGUIFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, then channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  TODO: This parameter is SDF specific.
     */
    public Parameter allowDynamicMultiportReference;

    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/codegen.
     */
    public FileParameter codeDirectory;

    /** If true, then compile the generated code. The default
     *  value is a parameter with the value true.
     */
    public Parameter compile;

    /** The name of compile target to be run if the <i>compile</i> parameter
     *  is true.  This is a string with a default value of the empty string,
     *  which means the first target in the makefile would be run.
     */
    public StringParameter compileTarget;

    /** If true, generate comments in the output code; otherwise,
     *  no comments is generated. The default value is a parameter
     *  with the value true.
     */
    public Parameter generateComment;

    /** If true, the generated code will be C++ instead of C.
     * FIXME: This is a temporary fix.  In the long run, C++ should
     * be its own target language for code generation.  In the short
     * run, this parameter will allow experimentation with C++ code
     * generation, and should identify changes needed for correctly
     * implemented C++ code generation.
     */
    public Parameter generateCpp;

    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;

    /** If true, then generate code for that uses the reflection for Java
     *  and JNI for C and is embedded within the model
     *  The default value is false and this parameter is not usually
     *  editable by the user.  This parameter is set to true when
     *  CompiledCompositeActor is run in an interpreted Ptolemy model.
     *  This parameter
     *  is set to false when a model contains one or more
     *  CompiledCompositeActors and C or Java code is being generated for the
     *  model.
     */
    public Parameter generateEmbeddedCode;

    /** If true, generate file with no functions.  If false, generate
     *  file with functions. The default value is a parameter with the
     *  value false.
     */
    public Parameter inline;

    /** If true, generate code to meausre the execution time.
     *  The default value is a parameter with the value false.
     */
    public Parameter measureTime;

    /** If true, overwrite preexisting files.  The default
     *  value is a parameter with the value true.
     */
    public Parameter overwriteFiles;

    /** If true, then buffers are padded to powers of two.
     *  TODO: This parameter is SDF specific.
     */
    public Parameter padBuffers;

    /** If true, then run the generated code. The default
     *  value is a parameter with the value true.
     */
    public Parameter run;

    /** If true, then the generated source is bound to the line
     *  number and file of the (helper) templates. Otherwise, the
     *  source is bound only to the output file. This is a boolean
     *  parameter with default value false.
     */
    public Parameter sourceLineBinding;

    /** The hardware target for code generation.  The list of possible choices
     *  is generated from the list of directories in the <code>targets</code>
     *  subdirectory of the directory named by the <i>generatorPackage</i>
     *  parameter.  For example, if <i>generatorPackage</i> is
     *  <code>ptolemy.codegen.c</code>, and if
     *  <code>$PTII/ptolemy/codegen/c/targets/</code> exists and contains
     *  an <code>iRobot/</code> directory, and this parameter is set
     *  to <code>iRobot</code>, then files from
     *  <code>$PTII/ptolemy/codegen/c/targets/iRobot</code> will be used
     *  to generate code.  The default value is the string "default"
     *  which means that the default target for the language is used.
     */
    public StringParameter target;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
    private int _generateCode(StringBuffer code) throws KernelException {
        // Record the current time so that we can monitor performance of the
        // code generator by printing messages whenever any part of the code
        // generation process takes more than 10 seconds.
        long startTime = (new Date()).getTime();
        long overallStartTime = startTime;

        reset();

        _sanitizedModelName = CodeGeneratorHelper.generateName(_model);

        // Each time a .dll file is generated, we must use a different name
        // for it so that it can be loaded without restarting vergil.
        NamedObj container = getContainer();
        if (container instanceof ptolemy.actor.lib.jni.CompiledCompositeActor) {
            _sanitizedModelName = ((ptolemy.actor.lib.jni.CompiledCompositeActor) container)
                    .getSanitizedName();
        } else if (container instanceof ptolemy.actor.lib.embeddedJava.CompiledCompositeActor) {
            _sanitizedModelName = ((ptolemy.actor.lib.embeddedJava.CompiledCompositeActor) container)
                    .getSanitizedName();
        }

        boolean inlineValue = ((BooleanToken) inline.getToken()).booleanValue();

        // Analyze type conversions that may be needed.
        // This must be called before any code is generated.
        _analyzeTypeConversions();

        // Report time consumed if appropriate.
        startTime = _printTimeAndMemory(startTime,
                "CodeGenerator.analyzeTypeConvert() consumed: ");

        // Add include directories and libraries specified by actors.
        _addActorIncludeDirectories();
        _addActorLibraries();

        // Generate code.
        // We use the strategy pattern here, calling methods that
        // can be overridden in derived classes. We mostly invoke
        // these methods in the order that the code will be
        // executed, except for some exceptions as noted.
        String sharedCode = _generateSharedCode();
        String preinitializeCode = _generatePreinitializeCode();

        // FIXME: The rest of these methods should be made protected
        // like the ones called above. The derived classes also need
        // to be fixed.
        String initializeCode = generateInitializeCode();

        // The StaticSchedulingCodeGenerator._generateBodyCode() reads
        // _postfireCode to see if we should include a call to postfire or
        // not, so we need to call generatePostfireCode() before
        // call _generateBodyCode().
        //_postfireCode = generatePostfireCode();

        String bodyCode = _generateBodyCode();
        String mainEntryCode = generateMainEntryCode();
        String mainExitCode = generateMainExitCode();
        String initializeEntryCode = generateInitializeEntryCode();
        String initializeExitCode = generateInitializeExitCode();
        String initializeProcedureName = generateInitializeProcedureName();
        //String postfireEntryCode = generatePostfireEntryCode();
        //String postfireExitCode = generatePostfireExitCode();
        ///*String postfireProcedureName =*/generatePostfireProcedureName();
        String wrapupEntryCode = generateWrapupEntryCode();
        String wrapupExitCode = generateWrapupExitCode();
        String wrapupProcedureName = generateWrapupProcedureName();

        String fireFunctionCode = null;
        if (!inlineValue) {
            fireFunctionCode = generateFireFunctionCode();
        }
        String wrapupCode = generateWrapupCode();
        String closingEntryCode = generateClosingEntryCode();
        String closingExitCode = generateClosingExitCode();

        // Generating variable declarations needs to happen after buffer
        // sizes are set(?).
        String variableDeclareCode = generateVariableDeclaration();
        String variableInitCode = generateVariableInitialization();
        // generate type resolution code has to be after
        // fire(), wrapup(), preinit(), init()...
        String typeResolutionCode = generateTypeConvertCode();
        //String globalCode = generateGlobalCode();

        // Include files depends the generated code, so it
        // has to be generated after everything.
        String includeFiles = _generateIncludeFiles();

        startTime = _printTimeAndMemory(startTime,
                "CodeGenerator: generating code consumed: ");

        // The appending phase.
        code.append(generateCopyright());

        // FIXME: Some user libraries may depend on our generated
        // code (i.e. definition of "boolean"). So, we need to append
        // these user libraries after the sharedCode. An easy to do
        // this is to separate the standard libraries from user librar,
        // hinted by the angle bracket <> syntax in a #include statement.
        code.append(includeFiles);
        code.append(typeResolutionCode);
        code.append(sharedCode);
        // Don't use **** in comments, it causes the nightly build to
        // report errors.
        code.append(comment("end shared code"));
        code.append(variableDeclareCode);
        code.append(preinitializeCode);
        code.append(comment("end preinitialize code"));
        //code.append(globalCode);

        if (!inlineValue) {

            code.append(comment("before appending fireFunctionCode"));
            code.append(fireFunctionCode);
            code.append(comment("after appending fireFunctionCode"));
        }

        //if (containsCode(variableInitCode)
        //        || containsCode(initializeCode)) {

        String[] splitVariableInitCode = _splitBody("_varinit_",
                variableInitCode);
        code.append(splitVariableInitCode[0]);
        String[] splitInitializeCode = _splitBody("_initialize_",
                initializeCode);
        code.append(splitInitializeCode[0]);

        code.append(initializeEntryCode);
        code.append(splitVariableInitCode[1]);
        code.append(splitInitializeCode[1]);
        code.append(initializeExitCode);

        /* FIXME: Postfire code should be invisible to the code generator.
         *  Postfire code should be generated by the Director helper.
         *
        if (containsCode(_postfireCode)) {
            // if (isTopLevel()) {
            //                          code.append(postfireProcedureName);
            //            } else {
            String [] splitPostfireCode = _splitBody("_postfire_",
                    _postfireCode);
            code.append(splitPostfireCode[0]);
            code.append(postfireEntryCode);
            code.append(splitPostfireCode[1]);
            code.append(postfireExitCode);
            //            }
        }
         */
        //if (containsCode(wrapupCode)) {
        // FIXME: The wrapup code can span multiple lines, so
        // our first attempt will not work.
        //String [] splitWrapupCode = _splitBody("_wrapup_", wrapupCode);
        //code.append(splitWrapupCode[0]);
        code.append(wrapupEntryCode);
        //code.append(splitWrapupCode[1]);
        code.append(wrapupCode);
        code.append(wrapupExitCode);
        //}

        code.append(mainEntryCode);

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (isTopLevel()) {
            if (((BooleanToken) measureTime.getToken()).booleanValue()) {
                code.append(_recordStartTime());
            }

            if (containsCode(variableInitCode) || containsCode(initializeCode)) {
                code.append(initializeProcedureName);
            }
        }

        code.append(bodyCode);

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (isTopLevel()) {
            if (containsCode(closingEntryCode)) {
                code.append(closingEntryCode);
            }
            if (((BooleanToken) measureTime.getToken()).booleanValue()) {
                code.append(_printExecutionTime());
            }
            if (containsCode(wrapupCode)) {
                code.append(wrapupProcedureName);
            }
            //if (containsCode(closingExitCode)) {
            code.append(closingExitCode);
            //}
        }

        code.append(mainExitCode);

        if (_executeCommands == null) {
            _executeCommands = new StreamExec();
        }

        startTime = _printTimeAndMemory(startTime,
                "CodeGenerator: appending code consumed: ");

        code = _finalPassOverCode(code);
        startTime = _printTimeAndMemory(startTime,
                "CodeGenerator: final pass consumed: ");

        _codeFileName = _writeCode(code);

        /*startTime =*/_printTimeAndMemory(startTime,
                "CodeGenerator: writing code consumed: ");

        _writeMakefile();

        _printTimeAndMemory(overallStartTime,
                "CodeGenerator: All phases above consumed: ");

        return _executeCommands();
    }


    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CodeGenerator newObject = (CodeGenerator) super.clone(workspace);
        newObject._helperStore = null;
        newObject._includes = null;
        newObject._libraries = null;
        newObject._model = null;
        newObject._modifiedVariables = null;
        newObject._newTypesUsed = null;
        newObject._tokenFuncUsed = null;
        newObject._typeFuncUsed = null;

        return newObject;
    }

    /** Add an include command line argument the compile command.
     *  @param includeCommand  The include command, for example
     *  "-I/usr/local/include".
     */
    public void addInclude(String includeCommand) {
        _includes.add(includeCommand);
    }

    /** Add a library command line argument the compile command.
     *  @param libraryCommand  The library command, for example
     *  "-L/usr/local/lib".
     *  @see #addLibraryIfNecessary(String)
     */
    public void addLibrary(String libraryCommand) {
        _libraries.add(libraryCommand);
    }

    /** If the compile command does not yet containe a library,
     *         add a library command line argument the compile command.
     *
     *  @param libraryCommand  The library command, for example
     *  "-L/usr/local/lib".
     *  @see #addLibrary(String)
     */
    public void addLibraryIfNecessary(String libraryCommand) {
        if (!_libraries.contains(libraryCommand)) {
            _libraries.add(libraryCommand);
        }
    }

    /** If the attribute is the codeDirectory parameter, then set the
     *  base directory of the codeDirectory parameter.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == codeDirectory) {
            // FIXME: This should not be necessary, but if we don't
            // do it, then getBaseDirectory() thinks we are in the current dir.
            try {
                codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());
            } catch (IllegalActionException ex) {
                _sandboxVariableCheck(ex);
            }
        } else if (attribute == generatorPackage) {
            super.attributeChanged(attribute);
            _updateTarget();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
     * @exception IllegalActionException Thrown if the given ptolemy cannot
     *  be resolved.
     */
    public String codeGenType(Type ptType) {
        // Do not make this static as Java Codegen requires that it be
        // non static.
        // If this is static, then this command will fail:
        // $PTII/bin/ptcg -generatorPackage ptolemy.codegen.java $PTII/ptolemy/codegen/java/actor/lib/colt/test/auto/ColtBinomialSelector.xml
        if (ptType == BaseType.GENERAL) {
            return "Token";
        }

        // FIXME: this may be the case for unconnected ports.
        if (ptType == BaseType.UNKNOWN) {
            return "Token";
        }

        if (ptType == BaseType.SCALAR) {
            // FIXME: do we need a codegen type for scalar?
            // This is needed by the Case actor in
            // ptolemy/codegen/c/actor/lib/hoc/test/auto/Case5.xml
            return "Token";
        }

        // FIXME: We may need to add more types.
        // FIXME: We have to create separate type for different matrix types.
        String result = ptType == BaseType.INT ? "Int"
                : ptType == BaseType.LONG ? "Long"
                        : ptType == BaseType.STRING ? "String"
                                : ptType == BaseType.DOUBLE ? "Double"
                                        : ptType == BaseType.BOOLEAN ? "Boolean"
                                                : ptType == BaseType.UNSIGNED_BYTE ? "UnsignedByte"
                                                        : ptType == PointerToken.POINTER ? "Pointer"
                                                                : null;

        if (result == null) {
            if (ptType instanceof ArrayType) {

                Type elementType = ((ArrayType) ptType).getElementType();
                // This change breaks $PTII/bin/ptcg $PTII/ptolemy/codegen/c/actor/lib/colt/test/auto/BinomialSelectorTest.xml
                if (isPrimitive(elementType)) {
                    result = codeGenType(elementType) + "Array";
                } else {
                    result = "Array";
                }

            } else if (ptType instanceof MatrixType) {
                //result = ptType.getClass().getSimpleName().replace("Type", "");
                result = "Matrix";
            }
        }
        if (result == null || result.length() == 0) {
            System.out
                    .println("Cannot resolved codegen type from Ptolemy type: "
                            + ptType);
        }
        return result;
    }

    /** Return a formatted comment containing the
     *  specified string with a specified indent level.
     *  @param comment The string to put in the comment.
     *  @param indentLevel The indentation level.
     *  @return A formatted comment.
     */
    public String comment(int indentLevel, String comment) {
        try {
            if (generateComment.getToken() == BooleanToken.TRUE) {
                return StringUtilities.getIndentPrefix(indentLevel)
                        + formatComment(comment);
            }
        } catch (IllegalActionException e) {
            // do nothing.
        }
        return "";
    }

    /** Return a formatted comment containing the
     *  specified string. In this base class, the
     *  comments is a C-style comment, which begins with
     *  "\/*" and ends with "*\/".
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String comment(String comment) {
        try {
            if (generateComment.getToken() == BooleanToken.TRUE) {
                return formatComment(comment);
            }
        } catch (IllegalActionException e) {
            // do nothing.
        }
        return "";
    }

    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a C-style comment, which
     *  begins with "\/*" and ends with "*\/" followed by the platform
     *  dependent end of line character(s): under Unix: "\n", under
     *  Windows: "\n\r". Subclasses may override this produce comments
     *  that match the code generation language.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    public String formatComment(String comment) {
        return "/* " + comment + " */" + _eol;
    }

    /** Generate code and write it to the file specified by the
     *  <i>codeDirectory</i> parameter.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If the target file cannot be overwritten
     *   or write-to-file throw any exception.
     */
    public int generateCode() throws KernelException {
        // FIXME: This doesn't make any sense.
        // It writes to a string buffer and then discards the
        // reference to that string buffer... (???)
        return generateCode(new StringBuffer());
    }

    /** Generate code.  This is the main entry point.
     *  @param code The code buffer into which to generate the code.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    public int generateCode(StringBuffer code) throws KernelException {

        int returnValue = -1;

        // If the container is in the top level, we are generating code
        // for the whole model. We have to make sure there is a manager,
        // and then preinitialize and resolve types.
        if (isTopLevel()) {

            // If necessary, create a manager.
            Actor container = ((Actor) getContainer());
            Manager manager = container.getManager();

            TypedCompositeActor toplevel = (TypedCompositeActor) ((NamedObj) container)
                .toplevel();

            if (manager == null) {
                manager = new Manager(toplevel.workspace(), "Manager");
                toplevel.setManager(manager);
            }

            try {
                manager.preinitializeAndResolveTypes();
                returnValue = _generateCode(code);
            } finally {
                // We call wrapup here so that the state gets set to idle.
                // This makes it difficult to test the Exit actor.
                try {
                    long startTime = (new Date()).getTime();
                    manager.wrapup();
                    _printTimeAndMemory(startTime, "CodeGenerator: "
                            + "wrapup consumed: ");
                } catch (RuntimeException ex) {
                    // The Exit actor causes Manager.wrapup() to throw this.
                    if (!manager.isExitingAfterWrapup()) {
                        throw ex;
                    }
                }
            }
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            returnValue = _generateCode(code);
        }
        return returnValue;
    }

    /** Return the closing entry code, if any.
     *  @return the closing entry code.
     */
    public String generateClosingEntryCode() {
        return comment("closing entry code");
    }

    /** Return the closing exit code, if any.
     *  @return the closing exit code.
     */
    public String generateClosingExitCode() {
        return comment("closing exit code");
    }

    /** Return the copyright for this code.
     *  @return The copyright.
     */
    public String generateCopyright() {
        // FIXME: Why isn't this method static?
        // Why isn't it in CodegenUtilities?
        return comment("Generated by Ptolemy II (http://ptolemy.eecs.berkeley.edu)"
                + _eol
                + _eol
                + "Copyright (c) 2005-2011 The Regents of the University of California."
                + _eol
                + "All rights reserved."
                + _eol
                + "Permission is hereby granted, without written agreement and without"
                + _eol
                + "license or royalty fees, to use, copy, modify, and distribute this"
                + _eol
                + "software and its documentation for any purpose, provided that the above"
                + _eol
                + "copyright notice and the following two paragraphs appear in all copies"
                + _eol
                + "of this software."
                + _eol
                + ""
                + _eol
                + "IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY"
                + _eol
                + "FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES"
                + _eol
                + "ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF"
                + _eol
                + "THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF"
                + _eol
                + "SUCH DAMAGE."
                + _eol
                + ""
                + _eol
                + "THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,"
                + _eol
                + "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF"
                + _eol
                + "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE"
                + _eol
                + "PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF"
                + _eol
                + "CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,"
                + _eol + "ENHANCEMENTS, OR MODIFICATIONS." + _eol);
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
        ActorCodeGenerator helper = _getHelper(getContainer());
        code.append(helper.generateFireFunctionCode());
        return code.toString();
    }

    /** Generate the function table.  In this base class return
     *  the empty string.
     *  @param types An array of types.
     *  @param functions An array of functions.
     *  @return The code that declares functions.
     */
    public Object generateFunctionTable(Object[] types, Object[] functions) {

        return "";
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
        //code.append(comment("Initialize " + getContainer().getFullName()));

        ActorCodeGenerator helper = _getHelper(getContainer());
        code.append(helper.generateInitializeCode());
        return code.toString();
    }

    /** Generate the initialization procedure entry point.
     *  @return a string for the initialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeEntryCode() throws IllegalActionException {

        return comment("initialization entry code");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the initialization procedure exit point.
     *  @return a string for the initialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeExitCode() throws IllegalActionException {

        return comment("initialization exit code");
    }

    /** Generate the initialization procedure name.
     *  @return a string for the initialization procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeProcedureName()
            throws IllegalActionException {

        return "";
    }

    /** Generate line number and file name information.
     *  @param lineNumber The line number of the source file or
     *  file containing code blocks.
     *  @param filename The name of the source file or file containing
     *  code blocks.
     *  @return In this base class, return the empty string.
     */
    public String generateLineInfo(int lineNumber, String filename) {
        return "";
    }

    /** Generate the main entry point.
     *  @return Return the definition of the main entry point for a program.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {

        return comment("main entry code");
    }

    /** Generate the main exit point.
     *  @return Return a string that declares the end of the main() function.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {

        return comment("main exit code");
    }

    /** Generate into the specified code stream the code associated with
     *  postfiring up the container composite actor. This method calls the
     *  generatePostfireCode() method of the code generator helper associated
     *  with the director of this container.
     *  @return The postfire code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ActorCodeGenerator helper = _getHelper(getContainer());
        code.append(helper.generatePostfireCode());
        return code.toString();
    }

    /** Generate the postfire procedure entry point.
     *  @return a string for the postfire procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireEntryCode() throws IllegalActionException {

        return comment("postfire entry code");
    }

    /** Generate the postfire procedure exit point.
     *  @return a string for the postfire procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireExitCode() throws IllegalActionException {

        return comment("postfire exit code");
    }

    /** Generate the postfire procedure name.
     *  @return a string for the postfire procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePostfireProcedureName() throws IllegalActionException {

        return "";
    }

    /** Generate type conversion code.
     *
     *  @return The type conversion code.
     *  @exception IllegalActionException If an error occurrs when generating
     *   the type conversion code, or if the helper class for the model
     *   director cannot be found, or if an error occurs when the helper
     *   actor generates the type conversion code.
     */
    public String generateTypeConvertCode() throws IllegalActionException {

        return "";
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        ActorCodeGenerator helper = _getHelper(getContainer());
        return helper.generateVariableDeclaration();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(_eol + _eol);
        //code.append(comment(1, "Variable initialization "
        //       + getContainer().getFullName()));

        ActorCodeGenerator helper = _getHelper(getContainer());

        code.append(helper.generateVariableInitialization());
        return code.toString();
    }

    /** Generate variable name for the given attribute. The reason to append
     *  underscore is to avoid conflict with the names of other objects. For
     *  example, the paired PortParameter and ParameterPort have the same name.
     *  @param attribute The attribute to generate variable name for.
     *  @return The generated variable name.
     */
    public String generateVariableName(NamedObj attribute) {
        return CodeGeneratorHelper.generateName(attribute) + "_";
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
        //code.append(comment(1, "Wrapup " + getContainer().getFullName()));

        ActorCodeGenerator helper = _getHelper(getContainer());
        code.append(helper.generateWrapupCode());
        return code.toString();
    }

    /** Generate the wrapup procedure entry point.
     *  @return a string for the wrapup procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupEntryCode() throws IllegalActionException {

        return comment("wrapup entry code");
    }

    /** Generate the wrapup procedure exit point.
     *  @return a string for the wrapup procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupExitCode() throws IllegalActionException {

        return comment("wrapup exit code");
    }

    /** Generate the wrapup procedure name.
     *  @return a string for the wrapup procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateWrapupProcedureName() throws IllegalActionException {

        return "";
    }

    /** Return the name of the code file that was written, if any.
     *  If no file was written, then return null.
     *  @return The name of the file that was written.
     */
    public String getCodeFileName() {
        return _codeFileName;
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

    /** Return a list of macros this code generator supports.
     *  @return Returns the _macros.
     */
    public List getMacros() {
        return _macros;
    }

    /** Return the set of modified variables.
     *  @return The set of modified variables.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public Set getModifiedVariables() throws IllegalActionException {
        return _modifiedVariables;
    }

    /**
     * Return the name of the output file.
     * @return The output file name.
     * @exception IllegalActionException If there is problem resolving
     *  the string value of the generatorPackage parameter.
     */
    public String getOutputFilename() throws IllegalActionException {
        String packageValue = generatorPackage.stringValue();

        String extension = packageValue
                .substring(packageValue.lastIndexOf("."));

        return _sanitizedModelName + extension;
    }

    /**
     * Determine if the given type is primitive.
     * @param cgType The given codegen type.
     * @return true if the given type is primitive, otherwise false.
     */
    public boolean isPrimitive(String cgType) {
        return _primitiveTypes.contains(cgType);
    }

    /**
     * Determine if the given type is primitive.
     * @param ptType The given ptolemy type.
     * @return true if the given type is primitive, otherwise false.
     */
    public boolean isPrimitive(Type ptType) {
        // This method cannot be static as it calls
        // codeGenType(), which is not static
        return _primitiveTypes.contains(codeGenType(ptType));
    }

    /** Test if the containing actor is in the top level.
     *  @return true if the containing actor is in the top level.
     */
    public boolean isTopLevel() {
        return getContainer().getContainer() == null;
    }

    /** Return the Ptolemy type that corresponds to the type named by
     *  the argument.
     *  @param cgType A String naming a type.
     *  @return null if there is not corresponding Ptolemy type.
     */
    public static Type ptolemyType(String cgType) {
        Type result = cgType.equals("Int") ? BaseType.INT : cgType
                .equals("Long") ? BaseType.LONG
                : cgType.equals("String") ? BaseType.STRING : cgType
                        .equals("Boolean") ? BaseType.BOOLEAN : cgType
                        .equals("Double") ? BaseType.DOUBLE : cgType
                        .equals("Complex") ? BaseType.COMPLEX : cgType
                        .equals("Pointer") ? PointerToken.POINTER : null;

        if (cgType.endsWith("Array")) {
            String elementType = cgType.replace("Array", "");
            result = new ArrayType(ptolemyType(elementType));

        } else if (cgType.endsWith("Matrix")) {
            String elementType = cgType.replace("Matrix", "");
            result = elementType.equals("Int") ? BaseType.INT_MATRIX
                    : elementType.equals("Complex") ? BaseType.COMPLEX_MATRIX
                            : elementType.equals("Double") ? BaseType.DOUBLE_MATRIX
                                    : elementType.equals("Boolean") ? BaseType.BOOLEAN_MATRIX
                                            : elementType.equals("Fix") ? BaseType.FIX_MATRIX
                                                    : elementType
                                                            .equals("Long") ? BaseType.LONG_MATRIX
                                                            : null;

        }
        return result;
    }

    /** Reset the code generator.
     */
    public void reset() {
        // Reset the indent to zero.
        _indent = 0;

        // Reset the code file name so that getCodeFileName()
        // accurately reports whether code was generated.
        _codeFileName = null;

        _newTypesUsed.clear();
        _tokenFuncUsed.clear();
        _typeFuncUsed.clear();
        _libraries.clear();
        _includes.clear();
        _helperStore.clear();
    }

    /** This method is used to set the code generator for a helper class.
     *  Since this is not a helper class for a component, this method does
     *  nothing.
     *  @param codeGenerator The given code generator.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
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

    /** Split a long function body into multiple functions.

     *  <p>In this base class, since we don't know what the target
     *  language will be, the first element is the empty string, the
     *  second element is the code argument.

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
     *  the code parameter.  In this base class, the first element
     *  is always the empty string and the second element is the value
     *  of the code parameter.
     *  @exception IOException If thrown will reading the code.
     */
    public String[] splitLongBody(int linesPerMethod, String prefix, String code)
            throws IOException {
        String[] results = { "", code };
        return results;
    }

    /**
     * Get the corresponding type in C from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The C data type.
     */
    public/*static*/String targetType(Type ptType) {
        // FIXME: we may need to add more primitive types.
        return ptType == BaseType.INT ? "int"
                : ptType == BaseType.STRING ? "char*"
                        : ptType == BaseType.DOUBLE ? "double"
                                : ptType == BaseType.BOOLEAN ? "boolean"
                                        : ptType == BaseType.LONG ? "long long "
                                                : ptType == BaseType.UNSIGNED_BYTE ? "unsigned char"
                                                        : ptType == PointerToken.POINTER ? "void*"
                                                                : "Token";
    }

    /** Add include directories specified by the actors in this model.
     *  @exception IllegalActionException Never in this base class.
     */
    protected void _addActorIncludeDirectories() throws IllegalActionException {
    }

    /** Add libraries specified by the actors in this model.
     *  @exception IllegalActionException Never in this base class.
     */
    protected void _addActorLibraries() throws IllegalActionException {
    }

    /** Analyze the model to find out what connections need to be type
     *  converted. This should be called before all the generate methods.
     *  @exception IllegalActionException If the helper of the
     *   top composite actor is unavailable.
     */
    protected void _analyzeTypeConversions() throws IllegalActionException {
        ((CodeGeneratorHelper) _getHelper(getContainer())).analyzeTypeConvert();
    }

    /** Return the value of the codeDirectory parameter.
     *  @return The value of the {@link #codeDirectory} parameter.
     *  @exception IOException If the <i>codeDirectory</i> parameter
     *  names a file or a directory cannot be created.
     *  @exception IllegalActionException If thrown while reading the
     *  codeDirectory parameter.
     */
    protected File _codeDirectoryAsFile() throws IOException,
            IllegalActionException {
        // This method is here to avoid code duplication.
        // It is package protected so we can read it in CodeGeneratorHelper
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
        return codeDirectoryFile;
    }

    /** Execute the compile and run commands in the
     *  <i>codeDirectory</i> directory. In this base class, 0 is
     *  returned by default.
     *  @return The result of the execution.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected int _executeCommands() throws IllegalActionException {
        return 0;
    }

    /** Make a final pass over the generated code. Subclass may extend
     * this method to do extra processing to format the output code.
     * @param code The given code to be processed.
     * @return The processed code.
     * @exception IllegalActionException If #getOutputFilename() throws it.
     */
    protected StringBuffer _finalPassOverCode(StringBuffer code)
            throws IllegalActionException {

        StringTokenizer tokenizer = new StringTokenizer(code.toString(), _eol
                + "\n");

        code = new StringBuffer();

        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            line = _prettyPrint(line, "{", "}");
            code.append(line + _eol);
        }

        return code;
    }

    /** Generate the body code that lies between variable declaration
     *  and wrapup. This method delegates to the director helper
     *  to generate a main loop.
     *  @return The generated body code.
     *  @exception IllegalActionException If there is no director.
     */
    protected String _generateBodyCode() throws IllegalActionException {
        CompositeEntity model = (CompositeEntity) getContainer();

        // NOTE: The cast is safe because setContainer ensures
        // the container is an Actor.
        ptolemy.actor.Director director = ((Actor) model).getDirector();

        if (director == null) {
            throw new IllegalActionException(model, "Does not have a director.");
        }
        Director directorHelper = (Director) _getHelper(director);

        if (isTopLevel()) {
            /*
            if (_postfireCode == null) {
                throw new InternalErrorException(
                        getContainer(),
                        null,
                        "generatePostfireCode() should be called before "
                                + "_generateBodyCode() because we need to know "
                                + "if there is a C postfire() method "
                                + "to be called.");
            }
             */
            return directorHelper.generateMainLoop(
            /*CodeGenerator.containsCode(_postfireCode)*/);

        } else {
            // Generate embedded code.
            CodeGeneratorHelper compositeHelper = (CodeGeneratorHelper) _getHelper(model);
            return compositeHelper.generateFireCode();
        }
    }

    /** Generate include files. This base class just returns an empty string.
     *  @return The include files.
     *  @exception IllegalActionException If the helper class for some actor
     *   cannot be found.
     */
    protected String _generateIncludeFiles() throws IllegalActionException {
        return "";
    }

    /** Generate preinitialize code (if there is any).
     *  This method calls the generatePreinitializeCode() method
     *  of the code generator helper associated with the enclosing
     *  composite actor.
     *  @return The preinitialize code of the containing composite actor.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found, or if an error occurs when the director
     *   helper generates preinitialize code.
     */
    protected String _generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ActorCodeGenerator helper = _getHelper(getContainer());

        try {
            // Determine which variables in the model can change
            // value during execution.
            _modifiedVariables = helper.getModifiedVariables();

            // Delegate to the container to generate preinitialize code.
            code.append(helper.generatePreinitializeCode());

            // Create variables for buffer offset indexing.
            // FIXME: This does not belong here. It is SDF-specific.
            code.append(helper.createOffsetVariablesIfNeeded());
        } catch (Throwable throwable) {
            throw new IllegalActionException(helper.getComponent(), throwable,
                    "Failed to generate preinitialize code");
        }
        return code.toString();
    }

    /** Generate code shared by actors, including globally defined
     *  data struct types and static methods or variables shared by multiple
     *  instances of the same actor type.
     *  @return The shared code of the containing composite actor.
     *  @exception IllegalActionException If an error occurrs when generating
     *   the globally shared code, or if the helper class for the model
     *   director cannot be found, or if an error occurs when the helper
     *   actor generates the shared code.
     */
    protected String _generateSharedCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ActorCodeGenerator targetHelper = _getTargetHelper(getContainer());
        Set sharedCodeBlocks;
        Iterator blocks;
        if (targetHelper != null) {
            sharedCodeBlocks = targetHelper.getSharedCode();
            blocks = sharedCodeBlocks.iterator();
            while (blocks.hasNext()) {
                String block = (String) blocks.next();
                code.append(block);
            }
        }

        ActorCodeGenerator helper = _getHelper(getContainer());
        sharedCodeBlocks = helper.getSharedCode();
        blocks = sharedCodeBlocks.iterator();
        while (blocks.hasNext()) {
            String block = (String) blocks.next();
            code.append(block);
        }

        if (code.length() > 0) {
            code.insert(
                    0,
                    _eol
                            + comment("Generate shared code for "
                                    + CodeGeneratorHelper
                                            .generateSimpleName(getContainer())));
            code.append(comment("Finished generating shared code for "
                    + CodeGeneratorHelper.generateSimpleName(getContainer())));
        }

        return code.toString();
    }

    /** Get the code generator helper associated with the given component.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    protected ActorCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        return (ActorCodeGenerator) _getHelper((Object) component);
    }

    /**
     * Get the code generator helper associated with the given object.
     * @param object The given object.
     * @return The code generator helper.
     * @exception IllegalActionException If the helper class cannot be found.
     */
    protected Object _getHelper(Object object) throws IllegalActionException {

        if (_helperStore.containsKey(object)) {
            return _helperStore.get(object);
        }

        String packageName = generatorPackage.stringValue();

        Class componentClass = object.getClass();
        String className = componentClass.getName();

        ActorCodeGenerator helperObject = null;

        // e.g. "ptolemy.*" ==> "ptolemy.codegen.c.*"
        String helperClassName = className.replaceFirst("ptolemy", packageName);

        String targetValue = target.getExpression();
        if (!targetValue.equals(_DEFAULT_TARGET)) {
            // Look in the target-specific directory.
            String targetSubDirectory = ".targets." + targetValue;

            try {
                helperClassName = className.replaceFirst("ptolemy", packageName
                        + targetSubDirectory);
                helperObject = _instantiateHelper(object, helperClassName);
                _helperStore.put(object, helperObject);
                return helperObject;

            } catch (Exception ex) {
                // There may not be a target-specific implementation.

                // FIXME: should we warn the users about this?
            }
        }

        // Now, we look in the default directory.
        if (packageName.trim().length() == 0) {
            packageName = "ptolemy.codegen";
        }

        StringBuffer classesTried = new StringBuffer();
        // Traverse the inheritance hierarchy of the componentClass
        // and look for a helper.
        do {
            className = componentClass.getName();

            // FIXME: Is this the right error message?
            if (!className.contains("ptolemy")) {
                throw new IllegalActionException("The classname \"" + className
                        + "\" does not contain \"ptolemy\"."
                        + "The classes tried were:\n" + classesTried.toString()
                        + "\nThus, there is no " + "codegen adaptor for "
                        + object.getClass());
            }

            // It could be that the className does not begin with
            // ptolemy, so try a simple substitution.
            try {
                helperClassName = packageName + "."
                        + className.replaceFirst("ptolemy.", "");
                // Save the classes tried for the error message
                if (classesTried.length() > 0) {
                    classesTried.append(", ");
                }
                classesTried.append(helperClassName);

                helperObject = _instantiateHelper(object, componentClass,
                        helperClassName);

            } catch (IllegalActionException ex) {
                // If helper class cannot be found, search the helper class
                // for parent class instead.
                componentClass = componentClass.getSuperclass();
            }

        } while (helperObject == null);

        _helperStore.put(object, helperObject);
        return helperObject;
        //}

        //        try {
        //            helperClassName = className.replaceFirst("ptolemy", packageName);
        //            helperObject = _instantiateHelper(object, helperClassName);
        //        } catch (Exception ex) {
        //            // try the implementation in the middle layer.
        //            helperClassName = className.replaceFirst("ptolemy", "ptolemy.codegen");
        //            helperObject = _instantiateHelper(object, helperClassName);
        //        }
        //_helperStore.put(object, helperObject);
        //return helperObject;

    }

    /** Generate the code for printing the execution time since
     *  the code generated by _recordStartTime() was called.
     *  This base class only generates a comment.
     *  @return Return the code for printing the total execution time.
     */
    protected String _printExecutionTime() {
        return comment("Print execution time.");
    }

    /** Print the elapsed time since the specified startTime if
     *  the elapsed time is greater than 10 seconds. Otherwise,
     *  do nothing.
     *  @param startTime The start time.  Usually set to the value
     *   of <code>(new Date()).getTime()</code>.
     *  @param message A prefix to the printed message.
     *  @return The current time.
     */
    protected long _printTimeAndMemory(long startTime, String message) {
        long currentTime = (new Date()).getTime();
        if (currentTime - startTime > 10000) {
            System.out.println(message + Manager.timeAndMemory(startTime));
        }
        return currentTime;
    }

    /** Generate the code for recording the current time.
     *  This base class only generates a comment.
     *  @return Return the code for recording the current time.
     */
    protected String _recordStartTime() {
        return comment("Record current time.");
    }

    /** Write the code to a directory named by the codeDirectory
     *  parameter, with a file name that is a sanitized version of the
     *  model name, and an extension that is the last package of
     *  the generatorPackage.
     *  @param code The StringBuffer containing the code.
     *  @return The name of the file that was written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    protected String _writeCode(StringBuffer code)
            throws IllegalActionException {

        // This method is private so that the body of the caller shorter.

        String codeFileName = getOutputFilename();

        // Write the code to a file with the same name as the model into
        // the directory named by the codeDirectory parameter.
        try {
            _executeCommands.stdout("Writing " + codeFileName + " in "
                    + codeDirectory.getBaseDirectory() + " (" + code.length()
                    + " characters)");

            // Check if needs to overwrite.
            if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
                    && codeDirectory.asFile().exists()) {
                // FIXME: It is totally bogus to ask a yes/no question
                // like this, since it makes it impossible to call
                // this method from a script.  If the question is
                // asked, the build will hang.
                if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                        + " exists. OK to overwrite?")) {
                    /*
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                     */
                    return FileUtilities.nameToFile(codeFileName,
                            codeDirectory.getBaseDirectory())
                            .getCanonicalPath();
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

            // The base directory has been set, this might have happened before
            // the directory was created. At that moment java assumes it is a file
            // when you call asFile().toURI(), and java removes the last "/".
            // When you afterwards call FileUtilities.openForWriting, this will call
            // nameToFile, which will call URI.resolve. In the end java than parses
            // the base directory and since it was recorded without a slash, it assumes
            // it is a file and removes this from the path.
            // To fix this we explicitly call setBaseDirectory again.
            codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());

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
            return FileUtilities.nameToFile(codeFileName,
                    codeDirectory.getBaseDirectory()).getCanonicalPath();
        } catch (Throwable ex) {
            throw new IllegalActionException(this, ex, "Failed to write \""
                    + codeFileName + "\" in "
                    + codeDirectory.getBaseDirectory());
        }
    }

    /** Create a make file to compile the generated code file(s).
     *  In this base class, it does nothing.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _writeMakefile() throws IllegalActionException {
    }

    /** Get the code generator helper associated with target, if any.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    private ActorCodeGenerator _getTargetHelper(NamedObj component)
            throws IllegalActionException {
        // FIXME: Do we want to cache the targetHelper?
        //if (_helperStore.containsKey(component)) {
        //    return (ActorCodeGenerator) _helperStore.get(component);
        //}

        String targetValue = target.getExpression();
        if (targetValue.equals(_DEFAULT_TARGET)) {
            return null;
        }
        String packageName = generatorPackage.stringValue();

        String upcaseTarget = targetValue.substring(0, 1).toUpperCase()
                + targetValue.substring(1);
        String helperClassName = packageName + ".targets." + targetValue + "."
                + upcaseTarget + "Target";

        return _instantiateHelper(component, helperClassName);
    }

    private ActorCodeGenerator _instantiateHelper(Object component,
            String helperClassName) throws IllegalActionException {
        return _instantiateHelper(component, component.getClass(),
                helperClassName);
    }

    /** Instantiate the given code generator helper.
     *  @param component The given component.
     *  @param helperClassName The dot separated name of the helper.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    private ActorCodeGenerator _instantiateHelper(Object component,
            Class componentClass, String helperClassName)
            throws IllegalActionException {

        Class helperClass = null;

        try {
            helperClass = Class.forName(helperClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalActionException(this, e,
                    "Cannot find helper class " + helperClassName);
        }

        Constructor constructor = null;

        try {
            constructor = helperClass
                    .getConstructor(new Class[] { componentClass });

        } catch (NoSuchMethodException e) {
            throw new IllegalActionException(this, e,
                    "There is no constructor in " + helperClassName
                            + " which accepts an instance of "
                            + component.getClass().getName()
                            + " as the argument.");
        }

        Object helperObject = null;

        try {
            helperObject = constructor.newInstance(new Object[] { component });
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to create helper class code generator for "
                            + helperClassName + ".");
        }

        if (!(helperObject instanceof ActorCodeGenerator)) {
            throw new IllegalActionException(this,
                    "Cannot generate code for this component: " + component
                            + ". Its helper class does not"
                            + " implement ActorCodeGenerator.");
        }

        ActorCodeGenerator castHelperObject = (ActorCodeGenerator) helperObject;

        castHelperObject.setCodeGenerator(this);

        return castHelperObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Pretty print the given line by indenting the line with the
     *  current indent level. If a block begin symbol is found, the
     *  indent level is incremented. Similarly, the indent level is
     *  decremented if a block end symbol is found.
     *  @param line The given line of code.
     *  @param blockBegin The given block begin symbol to match.
     *  @param blockEnd The given block end symbol to match.
     *  @return The pretty-printed version of the given code line.
     */
    private String _prettyPrint(String line, String blockBegin, String blockEnd) {

        line = line.trim();
        int begin = line.contains(blockBegin) ? 1 : 0;
        int end = line.contains(blockEnd) ? -1 : 0;

        String result = CodeStream.indent(_indent + end, line);

        _indent += begin + end;

        return result;
    }

    /** Split the code. */
    private String[] _splitBody(String prefix, String code) {
        // Split the initialize body into multiple methods
        // so that the compiler has an easier time.
        String[] results = null;
        try {
            results = splitLongBody(_LINES_PER_METHOD, prefix
                    + CodeGeneratorHelper.generateName(getContainer()), code);
        } catch (IOException ex) {
            // Ignore
            System.out.println("Warning: Failed to split code: " + ex);
            ex.printStackTrace();
            results = new String[] { "", code };
        }
        return results;
    }

    /** Set the parameters in the model stored in _parameterNames
     *  to the values given by _parameterValues. Those lists are
     *  populated by command line arguments.
     *  @param model The model in which to update parameters.
     */
    private void _updateParameters(NamedObj model) {
        // Check saved options to see whether any is setting an attribute.
        Iterator names = _parameterNames.iterator();
        Iterator values = _parameterValues.iterator();

        while (names.hasNext() && values.hasNext()) {
            String name = (String) names.next();
            String value = (String) values.next();

            Attribute attribute = model.getAttribute(name);
            if (attribute instanceof Settable) {
                // Use a MoMLChangeRequest so that visual rendition (if
                // any) is updated and listeners are notified.
                String moml = "<property name=\"" + name + "\" value=\""
                        + value + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, model,
                        moml);
                model.requestChange(request);
            } else {
                attribute = getAttribute(name);

                if (attribute instanceof Settable) {
                    // Use a MoMLChangeRequest so that visual rendition (if
                    // any) is updated and listeners are notified.
                    String moml = "<property name=\"" + name + "\" value=\""
                            + value + "\"/>";
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            this, moml);
                    model.requestChange(request);
                }
                attribute = getAttribute(name);
            }

            if (model instanceof CompositeActor) {
                ptolemy.actor.Director director = ((CompositeActor) model)
                        .getDirector();

                if (director != null) {
                    attribute = director.getAttribute(name);

                    if (attribute instanceof Settable) {

                        // Use a MoMLChangeRequest so that
                        // visual rendition (if any) is
                        // updated and listeners are notified.
                        String moml = "<property name=\"" + name
                                + "\" value=\"" + value + "\"/>";
                        MoMLChangeRequest request = new MoMLChangeRequest(this,
                                director, moml);
                        director.requestChange(request);
                    }
                }
            }
        }
    }

    /** Update the target parameter choices from the directory named by
     *  the generatorPackage parameter.  All the choices are removed
     *  the choice "default" is added and then the names of all directories
     *  in the directory named by <i>generatorPackage</i>/targets (except
     *  the <code>CVS</code> directory are added as choices.
     *  @exception IllegalActionException If the <i>generatorPackage</i>
     *  attribute cannot be read.
     */
    private void _updateTarget() throws IllegalActionException {
        String generatorPackageValue = generatorPackage.stringValue();
        String generatorPackageDirectoryName = StringUtilities.substitute(
                generatorPackageValue, ".", "/");

        target.removeAllChoices();
        target.addChoice(_DEFAULT_TARGET);

        File generatorPackageFile = null;
        try {
            // This will likely fail if ptolemy/codegen/c/targets is
            // in a jar file We use a URI here so that we can call
            // File(URI).

            URL generatorPackageURL = FileUtilities.nameToURL("$CLASSPATH/"
                    + generatorPackageDirectoryName, null, getClass()
                    .getClassLoader());
            URI generatorPackageURI = new URI(generatorPackageURL
                    .toExternalForm().replaceAll(" ", "%20"));
            generatorPackageFile = new File(generatorPackageURI);
        } catch (Throwable throwable) {
            // Ignore, we can't find the directory.
            // We return from here to quiet a FindBugs warning.
            return;
        }

        File targetDirectory = new File(generatorPackageFile, "targets");
        if (targetDirectory.exists()) {
            String[] targets = targetDirectory.list(new TargetFilter());
            for (int i = 0; i < targets.length; i++) {
                target.addChoice(targets[i]);
            }
        }
    }

    /** Return true if the input contains code.
     *  In this context, code is considered to be anything other
     *  than comments and whitespace.
     *  @param code The string to check for code.
     *  @return True if the string contains anything other than
     *  white space or comments
     */
    public static boolean containsCode(String code) {
        if (code == null) {
            return false;
        }
        return (code.replaceAll("/\\*[^*]*\\*/", "")
                .replaceAll("[ \t\n\r]", "").length() > 0);
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
        try {
            if (args.length == 0) {
                System.err
                        .println("Usage: java -classpath $PTII "
                                + "ptolemy.codegen.kernel.CodeGenerator model.xml "
                                + "[model.xml . . .]"
                                + _eol
                                + "  The arguments name MoML files containing models."
                                + "  Use -help to get a full list of command line arguments.");
                return -1;
            }

            CodeGenerator codeGenerator = null;

            // See MoMLSimpleApplication for similar code
            MoMLParser parser = new MoMLParser();
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
            // Don't remove graphical classes here, it means
            // we can't generate code for plotters etc using $PTII/bin/ptcg
            //MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

            // Reset the list each time we parse a parameter set.
            // Otherwise two calls to this method will share params!
            _parameterNames = new LinkedList<String>();
            _parameterValues = new LinkedList<String>();
            for (int i = 0; i < args.length; i++) {
                if (parseArg(args[i])) {
                    continue;
                }
                if (args[i].trim().startsWith("-")) {
                    if (i >= (args.length - 1)) {
                        throw new IllegalActionException("t set "
                                + "parameter " + args[i] + " when no value is "
                                + "given.");
                    }

                    // Save in case this is a parameter name and value.
                    _parameterNames.add(args[i].substring(1));
                    _parameterValues.add(args[i + 1]);
                    i++;
                    continue;
                }
                // Note: the code below uses explicit try catch blocks
                // so we can provide very clear error messages about what
                // failed to the end user.  The alternative is to wrap the
                // entire body in one try/catch block and say
                // "Code generation failed for foo", which is not clear.

                URL modelURL = null;
                try {
                    modelURL = new File(args[i]).toURI().toURL();
                } catch (Exception ex) {
                    throw new Exception("Could not open \"" + args[i] + "\"",
                            ex);
                }

                CompositeActor toplevel = null;

                try {
                    try {
                        // Reset the parser and reload so that if
                        // we run the model and then generate code,
                        // we get the same results when generating code.
                        // If we don't do this, then the nightly tests
                        // fail because the results don't match.
                        parser.reset();
                        MoMLParser.purgeModelRecord(modelURL);
                        toplevel = (CompositeActor) parser
                                .parse(null, modelURL);
                    } catch (Exception ex) {
                        throw new Exception("Failed to parse \"" + args[i]
                                + "\"", ex);
                    }

                    // Get all instances of this class contained in the model
                    List codeGenerators = toplevel
                            .attributeList(CodeGenerator.class);

                    // If the user called this with -generatorPackage ptolemy.codegen.java,
                    // the process that argument.  This is a bit hacky, but works.
                    String generatorPackageValue = "ptolemy.codegen.c";
                    int parameterIndex = -1;
                    if ((parameterIndex = _parameterNames
                            .indexOf("generatorPackage")) != -1) {
                        generatorPackageValue = _parameterValues
                                .get(parameterIndex);
                    }
                    Class generatorClass = _getCodeGeneratorClass(generatorPackageValue);

                    if (codeGenerators.size() != 0) {
                        // Get the last CodeGenerator in the list, maybe
                        // it was added last?
                        for (Object object : (List<CodeGenerator>) codeGenerators) {
                            //if (object instanceof CCodeGenerator) {
                            if (generatorClass.isInstance(object)) {
                                codeGenerator = (CodeGenerator) object;
                                break;
                            }
                        }
                    }

                    if (codeGenerators.size() == 0 || codeGenerator == null) {
                        // Add a codeGenerator
                        Constructor codeGeneratorConstructor = generatorClass
                                .getConstructor(new Class[] { NamedObj.class,
                                        String.class });
                        codeGenerator = (CodeGenerator) codeGeneratorConstructor
                                .newInstance(new Object[] { toplevel,
                                        "CodeGenerator_AutoAdded" });
                    }

                    codeGenerator._updateParameters(toplevel);
                    codeGenerator.generateEmbeddedCode.setExpression("false");

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
                    if (modelURL != null) {
                        // It turns out that after r55530, we need to
                        // call purgeModelRecord after setting the
                        // container to null.  Effigy.setContainer()
                        // does something similar.
                        parser.reset();
                        MoMLParser.purgeModelRecord(modelURL);
                    }
                }
            }
            if (codeGenerator != null) {
                return codeGenerator.getExecuteCommands()
                        .getLastSubprocessReturnCode();
            }
            return -2;
        } catch (Throwable ex) {
            MoMLApplication.throwArgsException(ex, args);
        }
        return -1;
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

    /** Parse a command-line argument. This method recognized -help
     *  and -version command-line arguments, and prints usage or
     *  version information. No other command-line arguments are
     *  recognized.
     *  @param arg The command-line argument to be parsed.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    public static boolean parseArg(String arg) throws Exception {
        if (arg.equals("-help")) {
            System.out.println(_usage());

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        } else if (arg.equals("-version")) {
            System.out
                    .println("Version "
                            + VersionAttribute.CURRENT_VERSION.getExpression()
                            + ", Build $Id$");

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        }
        // Argument not recognized.
        return false;
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected static String _usage() {
        // Call the static method that generates the usage strings.
        return StringUtilities.usageString(_commandTemplate, _commandOptions,
                _commandFlags);
    }

    /** Get the code generator associated with the generatePackage parameter.
     *  @param generatorPackageValue  The value of the generatorPackage parameter.
     *  @return The CodeGenerator class that corresponds with the generatorPackage parameter.
     *  For example, if generatorPackage is "ptolemy.codegen.c", then the class
     *  "ptolemy.codegen.c.kernel.CCodeGenerator" is searched for.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    private static Class _getCodeGeneratorClass(String generatorPackageValue)
            throws IllegalActionException {
        String language = generatorPackageValue.substring(generatorPackageValue
                .lastIndexOf("."));
        String capitalizedLanguage = language.substring(1, 2).toUpperCase()
                + language.substring(2);
        String codeGeneratorClassName = generatorPackageValue + ".kernel."
                + capitalizedLanguage + "CodeGenerator";
        Class result = null;
        try {
            result = Class.forName(codeGeneratorClassName);
        } catch (Throwable throwable) {
            throw new IllegalActionException("Failed to find \""
                    + codeGeneratorClassName
                    + "\", generatorPackage parameter was \""
                    + generatorPackageValue + "\".");

        }
        return result;
    }

    private void _sandboxVariableCheck(IllegalActionException ex)
            throws IllegalActionException {
        if (ex.getCause() instanceof UndefinedConstantOrIdentifierException
                && ((UndefinedConstantOrIdentifierException) (ex.getCause()))
                        .nodeName().equals("HOME")) {
            System.out
                    .println("Warning: Tried to set base directory of codeDirectory parameter to \""
                            + codeDirectory.getExpression()
                            + "\", but $HOME is not defined.  "
                            + "(-sandbox always causes this)");
        } else {
            throw ex;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the file that was written.
     *  If no file was written, then the value is null.
     */
    protected String _codeFileName = null;

    /** The command-line options that are either present or not. */
    protected static String[] _commandFlags = { "-help", "-version", };

    /** The command-line options that take arguments. */
    protected static String[][] _commandOptions = {
            { "-allowDynamicMultiportReferences",
                    "        true|false (default: false)" },
            {
                    "-codeDirectory",
                    "<directory in which to put code (default: $HOME/codegen. Other values: $CWD, $HOME, $PTII, $TMPDIR)>" },
            { "-compile", "           true|false (default: true)" },
            { "-compileTarget",
                    "     <target to be run, defaults to empty string>" },
            { "-generateComment", "   true|false (default: true)" },
            { "-generatorPackage",
                    "  <Java package of code generator, defaults to ptolemy.codegen.c>" },
            { "-inline", "            true|false (default: false)" },
            { "-measureTime", "       true|false (default: false)" },
            { "-overwriteFiles", "    true|false (default: true)" },
            { "-padBuffers", "        true|false (default: true)" },
            { "-run", "               true|false (default: true)" },
            { "-sourceLineBinding", " true|false (default: false)" },
            { "-target", "            <target name, defaults to false>" },
            { "-<parameter name>", "  <parameter value>" } };

    /** The form of the command line. */
    protected static final String _commandTemplate = "ptcg [ options ] [file ...]";

    /** The default target name. */
    protected static final String _DEFAULT_TARGET = "default";

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected static final String _eol;

    /** Execute commands to run the generated code.
     */
    protected ExecuteCommands _executeCommands;

    /** List of library command line arguments where each element is
     *  a string, for example "-L/usr/local/lib".
     *  This variable is a list so as to preserve the order that the
     *  library commands were added to the list of libraries matters,
     *  see the manual page for the -L option of the ld command.
     */
    protected List<String> _libraries = new LinkedList<String>();

    /** Indent string for indent level 1.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected static final String _INDENT1 = StringUtilities.getIndentPrefix(1);

    /** Indent string for indent level 2.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected static final String _INDENT2 = StringUtilities.getIndentPrefix(2);

    /** Indent string for indent level 3.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected static final String _INDENT3 = StringUtilities.getIndentPrefix(3);

    /** Set of include command line arguments where each element is
     *  a string, for example "-I/usr/local/include".
     */
    protected Set<String> _includes = new HashSet<String>();

    /** The model we for which we are generating code. */
    protected CompositeEntity _model;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** A set that contains all variables in the model whose values can be
     *  changed during execution.
     */
    protected Set _modifiedVariables = new HashSet();

    /** A HashSet that contains all codegen types referenced in the model.
     * When the codegen kernel processes a $new() macro, it would add the
     * codegen type to this set. Codegen types are supported by the code
     * generator package. (e.g. Int, Double, Array, and etc.)
     */
    protected HashSet<String> _newTypesUsed = new HashSet<String>();

    /**
     * A static list of all macros supported by the code generator.
     */
    protected static List<String> _macros = new ArrayList<String>(
            Arrays.asList(new String[] { "ref", "val", "size", "type",
                    "targetType", "cgType", "tokenFunc", "typeFunc",
                    "actorSymbol", "actorClass", "new" }));

    /** A list of the primitive types supported by the code generator.
     */
    protected static List _primitiveTypes = Arrays.asList(new String[] { "Int",
            "Double", "String", "Long", "Boolean", "UnsignedByte", "Pointer" });

    /** The postfire code. */
    protected String _postfireCode = null;

    /** The sanitized model name. */
    protected String _sanitizedModelName;

    /** A set that contains all token functions referenced in the model.
     *  When the codegen kernel processes a $tokenFunc() macro, it must add
     *  the token function to this set.
     */
    protected Set<String> _tokenFuncUsed = new HashSet<String>();

    /** A set that contains all type-specific functions referenced in the model.
     *  When the codegen kernel processes a $typeFunc() macro, it must add
     *  the type function to this set. Only those functions that are added
     *  to this set will be included in the generated code.
     */
    protected Set<String> _typeFuncUsed = new HashSet<String>();

    /** A map giving the code generator helpers for each actor. */
    private Map _helperStore = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The current indent level when pretty printing code. */
    private int _indent;

    /** List of parameter names seen on the command line. */
    private static List<String> _parameterNames;

    /** List of parameter values seen on the command line. */
    private static List<String> _parameterValues;

    /** Maximum number of lines in initialize(), postfire() and wrapup()
     *  methodS. This variable is used to make smaller methods so that
     *  compilers take less time.*/
    private static int _LINES_PER_METHOD = 10000;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    ///////////////////////////////////////////////////////////////////
    //// Code
    //     protected static class Code implements Comparable {
    //         public Code(String string, int order) {
    //             this.string = string;

    //             // Reverse the sign so that higher order value
    //             // will be first in the sorted list.
    //             this.order = -order;
    //         }

    //         public int compareTo(Object object) {
    //             if (object instanceof Code) {
    //                 Code code2 = (Code) object;

    //                 return (order > code2.order) ? 1 : (order < code2.order) ? -1
    //                         : 0;
    //             }
    //             return 0;
    //         }

    //         public final String string;

    //         private int order;

    //     }
}
