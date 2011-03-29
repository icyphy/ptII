/* Code generator for the Programming languages.

Copyright (c) 2009-2010 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.lib.jni.PointerToken;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////ProgramCodeGenerator

/** Base class for Program code generator.
*
*  @author Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating red (rodiers)
*  @Pt.AcceptedRating red (rodiers)
*/
public class ProgramCodeGenerator extends GenericCodeGenerator {

    /** Create a new instance of the ProceduralCodeGenerator.
     *  @param container The container.
     *  @param name The name of the ProceduralCodeGenerator.
     *  @param outputFileExtension The extension of the output file.
     *   (for example c in case of C and java in case of Java)
     *  @param templateExtension The extension of the template files.
     *   (for example c in case of C and j in case of Java).
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public ProgramCodeGenerator(NamedObj container, String name,
            String outputFileExtension, String templateExtension)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, outputFileExtension);

        _templateExtension = templateExtension;

        generateComment = new Parameter(this, "generateComment");
        generateComment.setTypeEquals(BaseType.BOOLEAN);
        generateComment.setExpression("true");

        inline = new Parameter(this, "inline");
        inline.setTypeEquals(BaseType.BOOLEAN);
        inline.setExpression("true");

        maximumLinesPerBlock = new Parameter(this, "maximumLinesPerBlock");
        maximumLinesPerBlock.setTypeEquals(BaseType.INT);
        maximumLinesPerBlock.setExpression("2500");

        measureTime = new Parameter(this, "measureTime");
        measureTime.setTypeEquals(BaseType.BOOLEAN);
        measureTime.setExpression("false");

        run = new Parameter(this, "run");
        run.setTypeEquals(BaseType.BOOLEAN);
        run.setExpression("true");

        variablesAsArrays = new Parameter(this, "variablesAsArrays");
        variablesAsArrays.setTypeEquals(BaseType.BOOLEAN);
        variablesAsArrays.setExpression("false");

        verbosity = new Parameter(this, "verbosity");
        verbosity.setTypeEquals(BaseType.INT);
        verbosity.setExpression("0");

        generatorPackageList.setExpression("generic.program");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** If true, generate comments in the output code; otherwise,
     *  no comments is generated. The default value is a parameter
     *  with the value true.
     */
    public Parameter generateComment;

    /** If true, generate file with no functions.  If false, generate
     *  file with functions. The default value is a parameter with the
     *  value false.
     */
    public Parameter inline;


    /** The maximum number of lines per block.  Maximum number of
     *  lines in initialize(), postfire() and wrapup() methods. This
     *  parameter is used to make smaller methods so that compilers
     *  take less time to compile.  Most target languages have limits
     *  to the size of a block or method.  If a block or method has
     *  more lines than this value, then some code generators may try
     *  to split of the code.  Note that this is very experimental.
     *  The default value is an integer with value 2500.
     */
    public Parameter maximumLinesPerBlock;

    /** If true, generate code to measure the execution time.
     *  The default value is a parameter with the value false.
     */
    public Parameter measureTime;

    /** If true, then run the generated code. The default
     *  value is a parameter with the value true.
     */
    public Parameter run;

    /** If true, then generate code that puts variables into arrays;
     *  otherwise, use standalone variables.  This parameter is used
     *  for very large models that would otherwise generate code that
     *  cannot be compiled by the Java compiler.  If this is the case,
     *  then javac will produce an error message like "too many
     *  constants". The default value is a parameter with the value
     *  false.
     */
    public Parameter variablesAsArrays;

    /** Level of verbosity in comments and other output.  Levels
     *  greater than 0 will cause the code generator to generate more
     *  detailed information about the operation of the code
     *  generator.  If the value of the <i>verbosity</i> parameter is
     *  greater than 9, then the comment is prepended with the name of
     *  the method that called the method that called this method.
     *  This is useful for debugging.  The default is an integer with
     *  the value 0, which indicates that the lowest level of
     *  verbosity.
     */
    public Parameter verbosity;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is the verbosity attribute, then if
     *  its value is 1, set a debug listener on the code generator.   
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == variablesAsArrays) {
            if (((BooleanToken) variablesAsArrays.getToken()).booleanValue()) {
                _variablesAsArrays = true;
            } else {
                _variablesAsArrays = false;
            }
        } else if (attribute == verbosity) {
            int verbosityLevel = ((IntToken) verbosity.getToken()).intValue();
            if (verbosityLevel == 1) {
                addDebugListener(new StreamListener());
            }
        }
        super.attributeChanged(attribute);
    }

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @see #ptolemyType(String)
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
            return "";
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
                                                                 : ptType == BaseType.COMPLEX ? "Complex"
                                                                : null;

        if (result == null) {
            if (ptType instanceof ArrayType) {

                // This change breaks $PTII/bin/ptcg $PTII/ptolemy/codegen/c/actor/lib/colt/test/auto/BinomialSelectorTest.xml
                if (isPrimitive(((ArrayType) ptType).getElementType())) {
                    result = codeGenType(((ArrayType) ptType).getElementType())
                            + "Array";
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
                    .println("Cannot resolve codegen type from Ptolemy type: "
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
                        + _formatComment(comment);
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
                return _formatComment(comment);
            }
        } catch (IllegalActionException e) {
            // do nothing.
        }
        return "";
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
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        code.append(adapter.generateFireFunctionCode());
        return code.toString();
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

    /**
     * Return the code associated with initialization of the containing
     * composite actor. This method calls the generateInitializeCode()
     * method of the code generator adapter associated with the model director.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If the adapter class for the model
     *  director cannot be found or if an error occurs when the director
     *  adapter generates initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(comment("Initialize " + getContainer().getFullName()));

        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        code.append(adapter.generateInitializeCode());
        return code.toString();
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

    /** Generate the initialization procedure entry point.
     *  @return a string for the initialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generateInitializeEntryCode() throws IllegalActionException {

        return comment("initialization entry code");
    }

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

    /** Generate the package statement, if any.
     *  Derived classes, such as the Java code generator, might generate
     *  a package statement here.
     *  @return In this base class, return the empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePackageStatement() throws IllegalActionException {
        return "";
    }

    /** Generate into the specified code stream the code associated with
     *  postfiring up the container composite actor. This method calls the
     *  generatePostfireCode() method of the code generator adapter associated
     *  with the director of this container.
     *  @return The postfire code of the containing composite actor.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        code.append(adapter.generatePostfireCode());
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
     *   the type conversion code, or if the adapter class for the model
     *   director cannot be found, or if an error occurs when the adapter
     *   actor generates the type conversion code.
     */
    public String generateTypeConvertCode() throws IllegalActionException {
        return "";
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        return adapter.generateVariableDeclaration();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(_eol + _eol);
        //code.append(comment(1, "Variable initialization "
        //       + getContainer().getFullName()));

        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());

        code.append(adapter.generateVariableInitialization());
        return code.toString();
    }

    /** Generate variable name for the given attribute. The reason to append
     *  underscore is to avoid conflict with the names of other objects. For
     *  example, the paired PortParameter and ParameterPort have the same name.
     *  @param attribute The attribute to for which to generate a variable name.
     *  @return The generated variable name.
     */
    public String generateVariableName(NamedObj attribute) {
        return NamedProgramCodeGeneratorAdapter.generateName(attribute) + "_";
    }

    /** Generate into the specified code stream the code associated with
     *  wrapping up the container composite actor. This method calls the
     *  generateWrapupCode() method of the code generator adapter associated
     *  with the director of this container.
     *  @return The wrapup code of the containing composite actor.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(comment(1, "Wrapup " + getContainer().getFullName()));

        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        code.append(adapter.generateWrapupCode());
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

    /** Return the set of modified variables.
     *  @return The set of modified variables.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    final public Set<Parameter> getModifiedVariables()
            throws IllegalActionException {
        return _modifiedVariables;
    }

    /**
     * Return The extention of the template files.
     * (for example c in case of C and j in case of Java)
     * @return The extention of the template files..
     */
    final public String getTemplateExtension() {
        return _templateExtension;
    }

    /** Instantiate the given code generator adapter.
     *  @param component The given component.
     *  @param componentClass The class of the component to be instantiated.
     *  The constructor for class named by the adapterClassName argument
     *  must take an argument of the class componentClass.
     *  @param adapterClassName The dot separated name of the adapter.
     *  @return The code generator adapter.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    protected CodeGeneratorAdapter _instantiateAdapter(Object component,
            Class<?> componentClass, String adapterClassName)
            throws IllegalActionException {
        ProgramCodeGeneratorAdapter adapter = (ProgramCodeGeneratorAdapter) super
                ._instantiateAdapter(component, componentClass,
                        adapterClassName);
        try {
            Class<?> templateParserClass = _templateParserClass();
            if (templateParserClass != null) {
                adapter.setTemplateParser((TemplateParser) templateParserClass
                        .newInstance());
            }
        } catch (InstantiationException e) {
            throw new InternalErrorException(e);
        } catch (IllegalAccessException e) {
            throw new InternalErrorException(e);
        }

        return adapter;
    }

    /**
     * Determine if the given type is primitive.
     * @param cgType The given codegen type.
     * @return true if the given type is primitive, otherwise false.
     */
    final public boolean isPrimitive(String cgType) {
        return _primitiveTypes.contains(cgType);
    }

    /**
     * Determine if the given type is primitive.
     * @param ptType The given ptolemy type.
     * @return true if the given type is primitive, otherwise false.
     */
    final public boolean isPrimitive(Type ptType) {
        // This method cannot be static as it calls
        // codeGenType(), which is not static
        return _primitiveTypes.contains(codeGenType(ptType));
    }

    /** Return the Ptolemy type that corresponds to the type named by
     *  the argument.
     *  @see #codeGenType(Type)
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

    /** Split a long function body into multiple functions.
     *   
     *  <p>In this base class, since we don't know what the target
     *  language will be, the first element is the empty string, the
     *  second element is the code argument.</p>
     *
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
     *  @exception IOException If thrown while reading the code.
     */
    public String[] splitLongBody(int linesPerMethod, String prefix, String code)
            throws IOException {
        String[] results = { "", code };
        return results;
    }

    /** Split a long variable declaration body into multiple blocks
     *  or files. 
     *  <p>In this base class, since we don't know what the target
     *  language will be, the first element is the empty string, the
     *  second element is the code argument.</p>
     *
     *  @param linesPerMethod The number of lines that should go into
     *  each method.
     *  @param prefix The prefix to use when naming functions that
     *  are created
     *  @param code The variable declarationsto be split.
     *  @return A list of at least two elements.  If the code has less
     *  than the value of the <i>maximumNumberOfLinesPerBLock</i>
     *  parameter lines, then the first element is empty, the second
     *  element contains the contents of the code parameter.  If the
     *  code has more lines than <i>maximumLinesPerBlock</i>,
     *  then the first element contains the declarations necessary for
     *  the include files section and the second element and
     *  successive elements contain the declarations.  Each
     *  declaration should be placed into a file that corresponds with
     *  the include or import listed in the first element.
     *  @exception IOException If thrown while reading the code.
     */
    public List<String> splitVariableDeclaration(int linesPerMethod, String prefix, String code)
            throws IOException {
        List<String> results = new LinkedList<String>();
        results.add("");
        results.add(code);
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

    /** Return an updated array of command line options.
     *  @return An array of updated command line options.
     */
    public String[][] updateCommandOptions() {
        // This is a hack.  

        // The command-line options that take arguments. 
        String[][] options = {
            { "-generateComment", "   true|false (default: true)" },
            { "-inline", "            true|false (default: false)" },
            { "-maximumLinesPerBlock", "<an integer, default: 2500)"}, 
            { "-measureTime", "       true|false (default: false)" },
            { "-run", "               true|false (default: true)" },
            { "-variablesAsArrays", " true|false (default:false)"},
            { "-verbosity", "         <an integer, try 1 or 10>, (default: 0)"}};

        String[][] parentOptions = super.updateCommandOptions();
        String[][] allOptions = new String[parentOptions.length + options.length][2];
        int i = 0;
        for(; i < parentOptions.length; i++) {
            allOptions[i][0] = parentOptions[i][0];
            allOptions[i][1] = parentOptions[i][1];
        }
        for(int j = 0; j < options.length; j++) {
            allOptions[i + j ][0] = options[j][0];
            allOptions[i + j ][1] = options[j][1];
        }
        return allOptions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
     *  @exception IllegalActionException If the adapter of the
     *   top composite actor is unavailable.
     */
    protected void _analyzeTypeConversions() throws IllegalActionException {
        ((NamedProgramCodeGeneratorAdapter) getAdapter(getContainer()))
                .analyzeTypeConvert();
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
        // It is package protected so we can read it in ProgramCodeGeneratorAdapter
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

    /** Return a formatted comment containing the specified string. In
     *  this base class, the comments is a C-style comment, which
     *  begins with "\/*" and ends with "*\/" followed by the platform
     *  dependent end of line character(s): under Unix: "\n", under
     *  Windows: "\n\r". Subclasses may override this produce comments
     *  that match the code generation language.
     *  If the value of the <i>verbosity</i> parameter is greater than 9,
     *  then the comment is prepended with the name of the method that
     *  called the method that called this method.  This is useful  
     *  for debugging.
     *  @param comment The string to put in the comment.
     *  @return A formatted comment.
     */
    protected String _formatComment(String comment) {
        String callingMethod = "";
        try {
            if (((IntToken) verbosity.getToken()).intValue() > 9) {
                callingMethod = new Throwable().getStackTrace()[2]
                        .getClassName().replace('$', '.')
                        + _eol;
            }
        } catch (IllegalActionException ex) {
            callingMethod = ex.toString();
        }
        return "/* " + callingMethod + comment + " */" + _eol;
    }

    /** Generate the body code that lies between variable declaration
     *  and wrapup. This method delegates to the director adapter
     *  to generate a main loop.
     *  @return The generated body code.
     *  @exception IllegalActionException If there is no director.
     */
    protected String _generateBodyCode() throws IllegalActionException {

        String code = "";

        CompositeEntity model = (CompositeEntity) getContainer();

        // NOTE: The cast is safe because setContainer ensures
        // the container is an Actor.
        ptolemy.actor.Director director = ((Actor) model).getDirector();

        if (director == null) {
            throw new IllegalActionException(model, "Does not have a director.");
        }
        Director directorAdapter = (Director) getAdapter(director);

        if (_isTopLevel()) {
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
            code += directorAdapter.generateMainLoop(
            /*CodeGenerator.containsCode(_postfireCode)*/);

        } else {
            // Generate embedded code.
            NamedProgramCodeGeneratorAdapter compositeAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(model);
            code += compositeAdapter.generateFireCode();
        }

        return code;

    }

    /** Generate include files. This base class just returns an empty string.
     *  @return The include files.
     *  @exception IllegalActionException If the adapter class for some actor
     *   cannot be found.
     */
    protected String _generateIncludeFiles() throws IllegalActionException {
        return "";
    }

    /** Generate code shared by actors, including globally defined
     *  data struct types and static methods or variables shared by multiple
     *  instances of the same actor type.
     *  @return The shared code of the containing composite actor.
     *  @exception IllegalActionException If an error occurrs when generating
     *   the globally shared code, or if the adapter class for the model
     *   director cannot be found, or if an error occurs when the adapter
     *   actor generates the shared code.
     */
    protected String _generateSharedCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        Set<String> sharedCodeBlocks = adapter.getSharedCode();
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
    protected int _generateCode(StringBuffer code) throws KernelException {
        // Record the current time so that we can monitor performance of the
        // code generator by printing messages whenever any part of the code
        // generation process takes more than 10 seconds.
        long startTime = (new Date()).getTime();
        long overallStartTime = startTime;

        _reset();

        _sanitizedModelName = NamedProgramCodeGeneratorAdapter
                .generateName(_model);

        // Each time a .dll file is generated, we must use a different name
        // for it so that it can be loaded without restarting vergil.
        NamedObj container = getContainer();
        if (container instanceof ptolemy.cg.lib.CompiledCompositeActor) {
            _sanitizedModelName = ((ptolemy.cg.lib.CompiledCompositeActor) container)
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

        String variableInitCode = generateVariableInitialization();
        // generate type resolution code has to be after
        // fire(), wrapup(), preinit(), init()...
        String typeResolutionCode = generateTypeConvertCode();
        // Generating variable declarations needs to happen after buffer
        // sizes are set(?).  Also, we want to generate the type convert code
        // so that we know if we need to import Array etc.
        List<String> variableDeclareCode = _splitVariableDeclaration(
                "Variables", generateVariableDeclaration());

        //String globalCode = generateGlobalCode();

        // Include files depends the generated code, so it
        // has to be generated after everything.
        String includeFiles = _generateIncludeFiles();

        startTime = _printTimeAndMemory(startTime,
                "CodeGenerator: generating code consumed: ");

        // The appending phase.
        code.append(generateCopyright());

        code.append(generatePackageStatement());

        code.append(variableDeclareCode.get(0));
        // FIXME: Some user libraries may depend on our generated
        // code (i.e. definition of "boolean"). So, we need to append
        // these user libraries after the sharedCode. An easy to do
        // this is to separate the standard libraries from user library,
        // hinted by the angle bracket <> syntax in a #include statement.
        code.append(includeFiles);
        // Get any include or import lines needed by the variable declarations.
        code.append(comment("end includeecode"));
        code.append(typeResolutionCode);
        code.append(comment("end typeResolution code"));
        code.append(sharedCode);
        // Don't use **** in comments, it causes the nightly build to
        // report errors.
        code.append(comment("end shared code"));
        code.append(_writeVariableDeclarations(variableDeclareCode));
        code.append(comment("end variable declaration code"));
        code.append(preinitializeCode);
        code.append(comment("end preinitialize code"));
        //code.append(globalCode);

        if (!inlineValue) {

            code.append(comment("Before appending fireFunctionCode."));
            code.append(fireFunctionCode);
            code.append(comment("After appending fireFunctionCode."));
        }

        //if (containsCode(variableInitCode)
        //        || containsCode(initializeCode)) {

        String[] splitVariableInitCode = _splitBody("_varinit_",
                variableInitCode);
        code.append(comment("Before appending splitVariableInitCode[0]."));
        code.append(splitVariableInitCode[0]);
        code.append(comment("After appending splitVariableInitCode[0]."));
        String[] splitInitializeCode = _splitBody("_initialize_",
                initializeCode);
        code.append(comment("Before appending splitInitializeCode[0]."));
        code.append(splitInitializeCode[0]);
        code.append(comment("After appending splitInitializeCode[0]."));

        code.append(comment("Before appending initializeEntryCode"));
        code.append(initializeEntryCode);
        code.append(comment("Before appending splitVariableInitCode[0]."));
        code.append(splitVariableInitCode[1]);
        code.append(comment("Before appending splitInitializeCode[1]."));
        code.append(splitInitializeCode[1]);
        code.append(initializeExitCode);

        /* FIXME: Postfire code should be invisible to the code generator.
         *  Postfire code should be generated by the Director adapter.
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
        if (_isTopLevel()) {
            if (containsCode(variableInitCode) || containsCode(initializeCode)) {
                code.append(initializeProcedureName);
            }
        }

        code.append(bodyCode);

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            if (containsCode(closingEntryCode)) {
                code.append(closingEntryCode);
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

        super._generateCode(code);

        /*startTime =*/_printTimeAndMemory(startTime,
                "CodeGenerator: writing code consumed: ");

        _writeMakefile();

        _printTimeAndMemory(overallStartTime,
                "CodeGenerator: All phases above consumed: ");

        return _executeCommands();
    }

    /** Generate preinitialize code (if there is any).
     *  This method calls the generatePreinitializeCode() method
     *  of the code generator adapter associated with the enclosing
     *  composite actor.
     *  @return The preinitialize code of the containing composite actor.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found, or if an error occurs when the director
     *   adapter generates preinitialize code.
     */
    protected String _generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());

        try {
            // Determine which variables in the model can change
            // value during execution.
            _modifiedVariables = adapter.getModifiedVariables();

            // Delegate to the container to generate preinitialize code.
            code.append(adapter.generatePreinitializeCode());

        } catch (Throwable throwable) {
            throw new IllegalActionException(adapter.getComponent(), throwable,
                    "Failed to generate preinitialize code");
        }
        return code.toString();
    }

    /** Generate the code for printing the execution time since
     *  the code generated by _recordStartTime() was called.
     *  This base class only generates a comment.
     *  @return Return the code for printing the total execution time.
     */
    protected String _printExecutionTime() {
        return comment("Print execution time.");
    }

    /** Return the prototype for fire functions.
     *  @return In this base class, return "()".
     *  Derived classes, such as the C code generator adapter
     *  might return "(void)".
     */
    protected String _getFireFunctionArguments() {
        return "()";
    }

    /** Generate the code for recording the current time.
     *  This base class only generates a comment.
     *  @return Return the code for recording the current time.
     */
    protected String _recordStartTime() {
        return comment("Record current time.");
    }

    /** Reset the code generator.
     */
    protected void _reset() {
        super._reset();
        // Reset the indent to zero.
        _indent = 0;
        _newTypesUsed.clear();
        _tokenFuncUsed.clear();
        _typeFuncUsed.clear();
    }

    /** Split the variable declaration into possibly two sections.
     *  @param suffix The suffix to use when naming functions that
     *  are created.
     *  @param code The variable declarations to be split.
     *  @return A list of at least two elements.  If the code has less
     *  than <i>maximumLinesPerBlock</i> lines, then the first
     *  element is empty, the second element contains the contents of
     *  the code parameter.  If the code has more lines than
     *  <i>maximumLinesPerBlock</i>, then the first element contains the
     *  declarations necessary for the include files section and the
     *  second element and successive elements contain the
     *  declarations.  Each declaration should be placed into a file
     *  that corresponds with the include or import listed in the
     *  first element.
     */
    protected List<String> _splitVariableDeclaration(String suffix, String code) {
        // Split the initialize body into multiple methods
        // so that the compiler has an easier time.
        List<String> results = new LinkedList<String>();
        try {
            results = splitVariableDeclaration(
                    ((IntToken) maximumLinesPerBlock.getToken()).intValue(),
                    NamedProgramCodeGeneratorAdapter
                    .generateName(getContainer()) + suffix,
                    code);
        } catch (Exception ex) {
            // Ignore
            System.out.println("Warning: Failed to split variable declaration: "
                    + ex);
            ex.printStackTrace();
            results.add("");
            results.add(code);
        }
        return results;
    }

    /** Return the class of the templateParser class. In cse
     *  there isn't one return null.
     *  @return The base class for templateParser.  
     */
    protected Class<? extends TemplateParser> _templateParserClass() {
        return TemplateParser.class;

    }

    /** Create a make file to compile the generated code file(s).
     *  In this base class, it does nothing.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _writeMakefile() throws IllegalActionException {
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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

    /** Split the code.
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
     */
    public String[] _splitBody(String prefix, String code) {
        // Split the initialize body into multiple methods
        // so that the compiler has an easier time.
        String[] results = null;
        try {
            results = splitLongBody(
                    ((IntToken) maximumLinesPerBlock.getToken()).intValue(),
                    prefix + NamedProgramCodeGeneratorAdapter
                            .generateName(getContainer()), code);
        } catch (Exception ex) {
            // Ignore
            System.out.println("Warning: Failed to split code: " + ex);
            ex.printStackTrace();
            results = new String[] { "", code };
        }
        return results;
    }

    /** Write the variable declaration code.
     *  @param variableDeclarations A List of two or more elements.  If
     *  the first element is the empty String, then the second element
     *  contains all of the variable declarations.  If the first element
     *  is not empty, then it contains the language specific declarations
     *  for the variable declarations.  For example, in C, the first element
     *  would consist of one or more "#include" statements.  In Java, the
     *  first element would consist of one or more "import" statements.  The
     *  second and successive elements contain the code to be written
     *  to separate files or to be returned as one String.
     *  @return The variable declarations or the empty string.  In this
     *  base class, the variable declarations are returned. Derived classes
     *  may write each element to a separate file and return the empty string.
     *  @exception IllegalActionException Not thrown in this base class.  Derived
     *  classes should throw this if there is a problem writing the file(s).
     */
    protected String _writeVariableDeclarations(List<String> variableDeclarations) 
            throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        int lineNumber = 0;
        for (String blocks : variableDeclarations) {
            // Skip the first element because it includes
            // the #include or import directives
            if (lineNumber++ > 0) {
                result.append(blocks);
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A set that contains all variables in the model whose values can be
     *  changed during execution.
     */
    protected Set<Parameter> _modifiedVariables = new HashSet<Parameter>();

    /** A HashSet that contains all codegen types referenced in the model.
     * When the codegen kernel processes a $new() macro, it would add the
     * codegen type to this set. Codegen types are supported by the code
     * generator package. (e.g. Int, Double, Array, and etc.)
     */
    protected HashSet<String> _newTypesUsed = new HashSet<String>();

    /** A list of the primitive types supported by the code generator.
     */
    protected static List<String> _primitiveTypes = Arrays.asList(new String[] {
            "Int", "Double", "String", "Long", "Boolean", "UnsignedByte",
            "Pointer"});

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

    /** The value of the variableAsArrays parameter. */
    protected boolean _variablesAsArrays;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    /** The current indent level when pretty printing code. */
    private int _indent;

    /** The extension of the template files.
     *   (for example c in case of C and j in case of Java)
     */
    private String _templateExtension;

}
