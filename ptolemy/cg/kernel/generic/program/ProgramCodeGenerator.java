/* Code generator for the Programming languages.

Copyright (c) 2009-2014 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
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
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.CodeGeneratorUtilities;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.lib.PointerToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.JVMBitWidth;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ProgramCodeGenerator

/** Generate a programming language version of a model.
 *
 *  <p>This base class contains parameters and methods common to
 *   all programming languages.</p>
 *
 *  @author Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (rodiers)
 *  @Pt.AcceptedRating red (rodiers)
 */
public class ProgramCodeGenerator extends GenericCodeGenerator {

    /** Create a new instance of the ProgramCodeGenerator.
     *  @param container The container.
     *  @param name The name of the ProgramCodeGenerator.
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

        runCommand = new StringParameter(this, "runCommand");
        // Set it to a default so that derived classes may override it.
        runCommand.setExpression(_runCommandDefault);

        useMake = new Parameter(this, "useMake");
        useMake.setTypeEquals(BaseType.BOOLEAN);
        useMake.setExpression("true");

        _substituteMap = CodeGeneratorUtilities.newMap(this);

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

    /** The command to use to run the generated code if the
     *  <i>useMake</i> parameter is false.  The initial default value
     *  is "make -f @modelName@.mk run".  Various '@' delimited
     *  key/value pairs will be automatically substituted.  In the
     *  default case @modelName@ will be replaced with a sanitized
     *  (Java-safe) version of the model name.
     *
     *  <p>If the string "@help:all@" appears, then all the key/value
     *  pairs are echoed at run time, though this may not result in a
     *  syntactically correct command.</p>
     *
     *  <p>If <i>useMake</i> is true, then the value of this parameter
     *  is ignored.</p>
     */
    public StringParameter runCommand;

    /** If true, then use the 'make' command to compile and run
     *  the generated code.  The default is true;
     */
    public Parameter useMake;

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

    /** Add a modified variable.
     *  @param variable The variable to add to the Set
     *  @exception IllegalActionException Not thrown in this base class.
     *  @see #getModifiedVariables()
     */
    final public void addModifiedVariables(Parameter variable)
            throws IllegalActionException {
        _modifiedVariables.add(variable);
    }

    /** If the attribute is the verbosity attribute, then if
     *  its value is 1, set a debug listener on the code generator.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    @Override
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

    /** Clone the attribute into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new attribute.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ProgramCodeGenerator newObject = (ProgramCodeGenerator) super
                .clone(workspace);

        try {
            newObject._substituteMap = CodeGeneratorUtilities.newMap(this);
        } catch (IllegalActionException ex) {
            throw new CloneNotSupportedException(ex.getMessage());
        }
        return newObject;
    }

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @see #ptolemyType(String)
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
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
            return "Scalar";
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
                                                                        // FIXME: Why do we have to use equals with BaseType.OBJECT?
                                                                        : ptType.equals(BaseType.OBJECT) ? "Object"
                                                                                //: ptType == BaseType.OBJECT ? "Object"
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
            } else if (ptType instanceof RecordType) {
                RecordType rType = (RecordType) ptType;
                StringBuffer arrayResult = new StringBuffer();
                for (String label : rType.labelSet()) {
                    Type t = null;
                    try {
                        t = (Type) rType.getTypeTerm(label).getValue();
                    } catch (IllegalActionException e) {
                    }
                    arrayResult.append(codeGenType(t) + ",");
                }
                result = arrayResult.toString().substring(0,
                        arrayResult.length() - 1);
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
    @Override
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

    /** Generate code that defines a constant.  In C, generate a
     *  #define, in Java, generate a static final.
     *  @param constant The name of the constant to be defined
     *  @param type A string representing the type.  In C, this
     *  parameter is ignored.
     *  @param value The value of the constant.
     *  @return A string that defines a constant.
     *  In this base class, a comment with the values of the
     *  arguments is returned.
     */
    public String generateConstantDefinition(String constant, String type,
            String value) {
        return comment(constant + " " + type + " " + value);
    }

    /** Generate The fire function code. This method is called when
     *  the firing code of each actor is not inlined. In the default,
     *  each actor's firing code is in a function with the name that
     *  is returned by
     *  {@link #generateFireFunctionMethodName(NamedObj)}.  Derived
     *  classes such as JavaCodeGenerator may put the fire functions
     *  in inner classes so as to reduce the Java file size.
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

    /** Generate the closing code for a group of fire functions common
     *  to a Composite Actor.  This method is called when the firing
     *  code of each actor is not inlined.
     *
     *  @return In this base class, return the empty string.  Derived
     *  classes, such as the JavaCodeGenerator could return the
     *  end of an inner class.
     */
    public String generateFireFunctionCompositeEnd() {
        return "";
    }

    /** Generate the initial code for a group of fire functions common
     *  to a Composite Actor.  This method is called when the firing
     *  code of each actor is not inlined.
     *
     *  @param className The name of the class to include in the
     *  initial code.
     *  @return In this base class, return the empty string.  Derived
     *  classes, such as the JavaCodeGenerator could return the
     *  start of an inner class.
     */
    public String generateFireFunctionCompositeStart(String className) {
        return "";
    }

    /** Generate the fire function method invocation. This method is called
     *  when the firing code of each actor is not inlined.  In this
     *  base class, each actor's firing code is in a function with the
     *  same name as that of the actor.
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return The name of the fire function invocation.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes should throw this exception if there are problems
     *  accessing the name or generating the name.
     */
    public String generateFireFunctionMethodInvocation(NamedObj namedObj)
            throws IllegalActionException {
        return generateFireFunctionMethodName(namedObj);
    }

    /** Generate the fire function method name. This method is called
     *  when the firing code of each actor is not inlined.  In this
     *  base class, each actor's firing code is in a function with the
     *  same name as that of the actor.
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return The name of the fire function method.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes should throw this exception if there are problems
     *  accessing the name or generating the name.
     */
    public String generateFireFunctionMethodName(NamedObj namedObj)
            throws IllegalActionException {
        return TemplateParser.escapeName(CodeGeneratorAdapter
                .generateName(namedObj));
    }

    /** Generate the fire function variable name and method
     *  name. This method is called when the firing code of each actor
     *  is not inlined.
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return An array of two elements.  In this base class, the
     *  first element is the empty string, the second element is the
     *  method name.  In derived classes, the first element is a
     *  String that contains the variable name, the second is the name
     *  of the method.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String[] generateFireFunctionVariableAndMethodName(NamedObj namedObj)
            throws IllegalActionException {
        String[] results = new String[2];
        results[0] = "";
        results[1] = CodeGeneratorAdapter.generateName(namedObj);
        return results;
    }

    /** Generate the fire function variable declaration. This method
     *  is called when the firing code of each actor is not inlined.
     *  In this base class, the empty string is returned.  Derived
     *  classes, such as JavaCodeGenerator, could return a variable
     *  declaration that instantiates an inner class.
     *
     *  <p>The purpose of this method is to allow derived generators
     *  to generate code in inner classes and thus allow the compilation
     *  of large models.</p>
     *
     *  @param namedObj The named object for which the name is generated.
     *  @return In this baseclass, return the empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     *  Derived classes should throw this exception if there are problems
     *  accessing the name or generating the name.
     */
    public String generateFireFunctionVariableDeclaration(NamedObj namedObj)
            throws IllegalActionException {
        return "";
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
        return code.replaceAll("/\\*[^*]*\\*/", "").replaceAll("[ \t\n\r]", "")
                .length() > 0;
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

    /**
     * Generate sanitized name for the given port.
     * If the {@link #variablesAsArrays} parameter is true, then
     * a reference into an array of the appropriate type (ports_int[],
     * ports_double[] etc.) is returned.  Otherwise, the name of
     * the port with any underscores converted to periods is returned.
     * See {@link ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.kernel.SDFDirector#generateInitializeCode()} for where the arrays are initialized.
     * @param port The port for which the name is generated.
     * @param portName The sanitized name of the port.
     * @param bufferSize The size of the port buffer.
     * @return The name of the port as an array element.
     */
    public String generatePortName(TypedIOPort port, String portName,
            int bufferSize) {

        try {
            if (!((BooleanToken) variablesAsArrays.getToken()).booleanValue()) {
                return portName.replace(".", "_");
            }
        } catch (IllegalActionException ex) {
            // Ignore
            ex.printStackTrace();
        }

        // Generate the port name as an element in array.
        // This is done to make the generate java file easier to compile.

        // There is similar code in JavaCodeGenerator.

        // The idea is that for each type, we have an array
        // that contain the variables for that type.
        // This means that we will have many less variables, which will
        // get around javac's "too many constants" message
        // (See http://marxsoftware.blogspot.com/2010/01/reproducing-too-many-constants-problem.html)

        // However, we don't want to search the arrays while
        // generating code, so we have a separate HashMap that
        // that is used at code generation time to map from
        // names to the index in the corresponding type array.

        String typeName = targetType(port.getType());

        // We will generate code that uses three different arrays.
        // The arrays differ in the number of dimensions.
        // Here, the arrays are represented as maps.
        // Determine which map to use.  We delay instantiation
        // until we need the map so that we don't generate unnecessary
        // code.

        HashMap<String, HashMap<String, Integer>> typeMap = null;
        HashMap<String, Integer> typeMaxIndex = null;
        String arrayName = null;

        if (!port.isMultiport() && bufferSize <= 1) {
            if (_portTypeMap == null) {
                // A map from String type name to a HashMap of port
                // name to array index.
                _portTypeMap = new HashMap<String, HashMap<String, Integer>>();
                _portTypeMaxIndex = new HashMap<String, Integer>();
            }
            arrayName = "ports_";
            typeMap = _portTypeMap;
            typeMaxIndex = _portTypeMaxIndex;
        } else if (port.isMultiport() && bufferSize <= 1 || !port.isMultiport()
                && bufferSize > 1) {
            // A 2D array is needed.
            if (_portTypeMap2 == null) {
                // A map from String type name to a HashMap of multiport name
                // or port with a buffersize greater than 1 to array index.
                _portTypeMap2 = new HashMap<String, HashMap<String, Integer>>();
                _portTypeMaxIndex2 = new HashMap<String, Integer>();
            }
            arrayName = "ports2_";
            typeMap = _portTypeMap2;
            typeMaxIndex = _portTypeMaxIndex2;
        } else if (port.isMultiport() && bufferSize > 1) {
            // A 3D array is needed.
            if (_portTypeMap3 == null) {
                // A map from String type name to a HashMap of multiport name
                // with a buffersize greater than 1 to array index.
                _portTypeMap3 = new HashMap<String, HashMap<String, Integer>>();
                _portTypeMaxIndex3 = new HashMap<String, Integer>();
            }
            arrayName = "ports3_";
            typeMap = _portTypeMap3;
            typeMaxIndex = _portTypeMaxIndex3;
        } else {
            throw new InternalErrorException(this, null,
                    "This should not be happening. " + "Port "
                            + port.getFullName() + " isMultiport(): "
                            + port.isMultiport() + " buffer size: "
                            + bufferSize);
        }

        // Look up the type in our HashTable of types.
        HashMap<String, Integer> portMap = null;
        if ((portMap = typeMap.get(typeName)) == null) {
            // A type that is not in our map of types.
            portMap = new HashMap<String, Integer>();
            typeMap.put(typeName, portMap);
            typeMaxIndex.put(typeName, 0);
        }

        // Look up the attribute by name in the HashTable.
        Integer portIndex = null;
        if ((portIndex = portMap.get(portName)) == null) {
            // FIXME: is there a better way to update an element in a HashMap?
            portIndex = typeMaxIndex.get(typeName);
            typeMaxIndex.put(typeName, portIndex + 1);
            portMap.put(portName, portIndex);
        }

        return arrayName + StringUtilities.sanitizeName(typeName) + "["
        + portIndex + "]";
    }

    /** Generate sanitized name for the given Ptolemy IOPort.
     * This method returns an array reference in to an array of Ptolemy
     * ports.  This method is used with AutoAdapter, most code
     * uses generatePortName() above.
     * This method is used when the {@link #variablesAsArrays}
     * parameter is true.
     * @param container The actor that contains the port, which may be null.
     * @param portName The sanitized name of the port
     * @return The name of the port as an array element.
     */
    public String generatePtIOPortName(NamedObj container, String portName) {
        // This method is used in AutoAdapter.
        Integer portIndex = null;
        String portNameSymbol = generateVariableName(container) + "_"
                + portName;
        if ((portIndex = _ioPortMap.get(portNameSymbol)) == null) {
            // FIXME: is there a better way to update an element in a HashMap?
            portIndex = Integer.valueOf(_ioPortMap.size());
            _ioPortMap.put(portNameSymbol, portIndex);
        }
        return "_ioPortMap[" + portIndex + "]";
    }

    /** Return the size of the ioPortMap.
     *  @return the size of the ioPortMap.
     */
    public int generatePtIOPortSize() {
        // This method is used in AutoAdapter.
        return _ioPortMap.size();
    }

    /** Generate sanitized name for the given TypedCompositeActor
     * This method is used when the {@link #variablesAsArrays}
     * parameter is true.
     * @param container The container of the actor.
     * @param actorName The sanitized name of the actor.
     * @return The name of the actor as an array element.
     *  @exception IllegalActionException If the variablesAsArrays parameter
     *  of the code generator cannot be read.
     */
    public String generatePtTypedCompositeActorName(NamedObj container,
            String actorName) throws IllegalActionException {
        if (!((BooleanToken) variablesAsArrays.getToken()).booleanValue()) {
            return generateVariableName(container);
        } else {
            // This method is used in AutoAdapter.
            Integer actorIndex = null;
            String actorNameSymbol = generateVariableName(container) + "_"
                    + actorName;
            if ((actorIndex = _typedCompositeActorMap.get(actorNameSymbol)) == null) {
                // FIXME: is there a better way to update an element in a HashMap?
                actorIndex = Integer.valueOf(_typedCompositeActorMap.size());
                _typedCompositeActorMap.put(actorNameSymbol, actorIndex);
            }
            return "_compositeMap[" + actorIndex + "]";
        }
    }

    /** Return the size of the TypedCompositeActor Map.
     *  @return the size of the TypedCompositeActor Map.
     */
    public int generatePtTypedCompositeActorSize() {
        // This method is used in AutoAdapter.
        return _typedCompositeActorMap.size();
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
        return CodeGeneratorAdapter.generateName(attribute) + "_";
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
     *  @see #setModifiedVariables(Set)
     */
    final public Set<Parameter> getModifiedVariables()
            throws IllegalActionException {
        return _modifiedVariables;
    }

    /**
     * Return The extension of the template files.
     * (for example c in case of C and j in case of Java)
     * @return The extension of the template files..
     */
    final public String getTemplateExtension() {
        return _templateExtension;
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
                                                        .equals("Object") ? BaseType.OBJECT : cgType
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

    /** Sets the set of modified variables.
     *  @param modifiedVariables The set of modified variables to be saved
     *  @exception IllegalActionException Not thrown in this base class.
     *  @see #getModifiedVariables()
     */
    final public void setModifiedVariables(Set<Parameter> modifiedVariables)
            throws IllegalActionException {
        _modifiedVariables = modifiedVariables;
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
     *  @param code The variable declarations to be split.
     *  @return A list of at least two elements.  If the code has less
     *  than the value of the <i>maximumNumberOfLinesPerBlock</i>
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
    public List<String> splitVariableDeclaration(int linesPerMethod,
            String prefix, String code) throws IOException {
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
                                                        // FIXME: Why do we have to use equals with BaseType.OBJECT?
                                                        : ptType.equals(BaseType.OBJECT) ? "Object"
                                                                : ptType == PointerToken.POINTER ? "void*"
                                                                        : "Token*";
    }

    /** Return an updated array of command line options.
     *  @return An array of updated command line options.
     */
    @Override
    public String[][] updateCommandOptions() {
        // This is a hack.

        // The command-line options that take arguments.
        String[][] options = {
                { "-generateComment", "   true|false (default: true)" },
                { "-inline", "            true|false (default: false)" },
                { "-maximumLinesPerBlock", "<an integer, default: 2500>" },
                { "-measureTime", "       true|false (default: false)" },
                { "-run", "               true|false (default: true)" },
                { "-runCommand",
                "        <a string, default: make -f @modelName@.mk run>" },
                { "-variablesAsArrays", " true|false (default:false)" },
                { "-verbosity",
                "         <an integer, try 1 or 10>, (default: 0)" } };

        String[][] parentOptions = super.updateCommandOptions();
        String[][] allOptions = new String[parentOptions.length
                                           + options.length][2];
        int i = 0;
        for (; i < parentOptions.length; i++) {
            allOptions[i][0] = parentOptions[i][0];
            allOptions[i][1] = parentOptions[i][1];
        }
        for (int j = 0; j < options.length; j++) {
            allOptions[i + j][0] = options[j][0];
            allOptions[i + j][1] = options[j][1];
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
                        .getClassName().replace('$', '.') + _eol;
            }
        } catch (IllegalActionException ex) {
            callingMethod = ex.toString();
        }
        // We escape the comment so that if there are $ in the comment
        // then we don't interpret them later.
        return "/* " + callingMethod + TemplateParser.escapeName(comment)
                + " */" + _eol;
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
            //Director directorAdapter = (Director) getAdapter(director);
            NamedProgramCodeGeneratorAdapter directorAdapter = (NamedProgramCodeGeneratorAdapter) getAdapter(director);
            code += directorAdapter.generateMainLoop(
            /* CodeGenerator.containsCode(_postfireCode)*/
            );

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
    @Override
    protected int _generateCode(StringBuffer code) throws KernelException {
        // Record the current time so that we can monitor performance of the
        // code generator by printing messages whenever any part of the code
        // generation process takes more than 10 seconds.
        long startTime = new Date().getTime();
        long overallStartTime = startTime;

        _reset();

        _sanitizedModelName = CodeGeneratorAdapter.generateName(_model);

        // Each time a .dll file is generated, we must use a different name
        // for it so that it can be loaded without restarting vergil.
        CompositeActor container = (CompositeActor) getContainer();
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

        // Perform any setup in the adapter.  EmbeddedCodeActor uses this.
        _setupAdapter();
        String preinitializeCode = _generatePreinitializeCode();

        // Typically, the preinitialize code consists of variable
        // declarations.  However, AutoAdapter generates method calls
        // that instantiate wrapper TypedCompositeActors, so we need
        // to invoke those method calls.
        String preinitializeMethodEntryCode = _generatePreinitializeMethodEntryCode();
        String preinitializeMethodBodyCode = _generatePreinitializeMethodBodyCode();
        String preinitializeMethodExitCode = _generatePreinitializeMethodExitCode();
        String preinitializeProcedureName = _generatePreinitializeMethodProcedureName();

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

        // Generate shared code.  Some adapter optionally add methods
        // to the shared code block, so we generate the shared code as
        // late as possible.  However, we have to generateSharedCode()
        // before generateTypeConvertCode() so that any polymorphic
        // codegen token methods used in the shared code are recorded.  See
        // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/arrayType18.xml
        String sharedCode = _generateSharedCode();

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
        //variableDeclareCode.set(0, null);

        // FIXME: Some user libraries may depend on our generated
        // code (i.e. definition of "boolean"). So, we need to append
        // these user libraries after the sharedCode. An easy to do
        // this is to separate the standard libraries from user library,
        // hinted by the angle bracket <> syntax in a #include statement.
        code.append(includeFiles);

        // Free up space as we go.
        includeFiles = null;

        // Get any include or import lines needed by the variable declarations.
        code.append(comment("end includeecode"));
        code.append(typeResolutionCode);
        typeResolutionCode = null;
        code.append(comment("end typeResolution code"));
        code.append(sharedCode);
        sharedCode = null;
        // Don't use **** in comments, it causes the nightly build to
        // report errors.
        code.append(comment("end shared code"));
        code.append(_writeVariableDeclarations(variableDeclareCode));
        code.append(comment("end variable declaration code"));
        code.append(preinitializeCode);
        preinitializeCode = null;
        code.append(comment("end preinitialize code"));
        code.append(comment("end preinitialize method code"));
        //code.append(globalCode);

        String[] splitPreinitializeMethodBodyCode = _splitBody(
                "_preinitializeMethod_", preinitializeMethodBodyCode);
        code.append(comment("Before appending splitPreinitializeMethodBodyCode[0]."));
        code.append(splitPreinitializeMethodBodyCode[0]);
        // Set this to null to free up space.
        splitPreinitializeMethodBodyCode[0] = null;
        code.append(comment("After appending splitPreinitializeMethodBodyCode[0]."));
        code.append(preinitializeMethodEntryCode);
        code.append(splitPreinitializeMethodBodyCode[1]);
        splitPreinitializeMethodBodyCode[1] = null;
        code.append(preinitializeMethodExitCode);

        if (!inlineValue) {

            code.append(comment("Before appending fireFunctionCode."));
            code.append(fireFunctionCode);
            fireFunctionCode = null;
            code.append(comment("After appending fireFunctionCode."));
        }

        //if (containsCode(variableInitCode)
        //        || containsCode(initializeCode)) {

        String[] splitVariableInitCode = _splitBody("_varinit_",
                variableInitCode);
        code.append(comment("Before appending splitVariableInitCode[0]."));
        code.append(splitVariableInitCode[0] + "\n");
        splitVariableInitCode[0] = null;
        code.append(comment("\nAfter appending splitVariableInitCode[0].\n"));

        String[] splitInitializeCode = _splitBody("_initialize_",
                initializeCode);
        code.append(comment("Before appending splitInitializeCode[0]."));
        code.append(splitInitializeCode[0]);
        splitInitializeCode[0] = null;
        code.append(comment("After appending splitInitializeCode[0]."));

        code.append(comment("Before appending initializeEntryCode"));
        code.append(initializeEntryCode);
        code.append(comment("After appending initializeEntryCode"));
        code.append(comment("Before appending splitVariableInitCode[1]."));
        code.append(splitVariableInitCode[1]);
        splitVariableInitCode[1] = null;
        code.append(comment("After appending splitVariableInitCode[1]."));
        code.append(comment("Before appending splitInitializeCode[1]."));
        code.append(splitInitializeCode[1]);
        splitInitializeCode[1] = null;
        code.append(comment("After appending splitInitializeCode[1]."));
        code.append(comment("Before appending initializeExitCode."));
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
            splitPostfireCode[0] = null;
            code.append(postfireEntryCode);
            code.append(splitPostfireCode[1]);
            splitPostfireCode[1] = null;
            code.append(postfireExitCode);
            //            }
        }
         */
        //if (containsCode(wrapupCode)) {
        String[] splitWrapupCode = _splitBody("_wrapup_", wrapupCode);
        code.append(splitWrapupCode[0]);
        splitWrapupCode[0] = null;
        code.append(wrapupEntryCode);
        code.append(splitWrapupCode[1]);
        splitWrapupCode[1] = null;
        //code.append(wrapupCode);
        code.append(wrapupExitCode);
        //}

        code.append(mainEntryCode);

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            if (containsCode(preinitializeMethodBodyCode)) {
                code.append(preinitializeProcedureName);
            }
            if (containsCode(variableInitCode) || containsCode(initializeCode)) {
                code.append(initializeProcedureName);
            }
        }

        code.append(bodyCode);
        // Findbugs warns that it is not necessary to set these fields
        // to null in JSSE1.6, but these strings are so huge that it
        // seems to help reduce the memory footprint.
        bodyCode = null;

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
        code = null;

        /*startTime =*/_printTimeAndMemory(startTime,
                "CodeGenerator: writing code consumed: ");

        // Create the needed directories
        String directory = codeDirectory.stringValue();
        if (!directory.endsWith("/")) {
            directory += "/";
        }

        // Writing the Makefile
        _writeMakefile(container, directory);

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
        
        getAdapter(getComponent());
        Director directorAdapter = (Director) getAdapter(
                ((ptolemy.actor.CompositeActor) getComponent()).getDirector());
        
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
    protected String _generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());

        try {
            // Delegate to the container to generate preinitialize code.
            code.append(adapter.generatePreinitializeMethodBodyCode());
        } catch (Throwable throwable) {
            throw new IllegalActionException(adapter.getComponent(), throwable,
                    "Failed to generate preinitialize method body code");
        }
        return code.toString();
    }

    /** Generate the preinitialization procedure entry point.
     *  @return a string for the preinitialization procedure entry point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected String _generatePreinitializeMethodEntryCode()
            throws IllegalActionException {
        return comment("preinitialization entry code");
    }

    /** Generate the preinitialization procedure exit point.
     *  @return a string for the preinitialization procedure exit point.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected String _generatePreinitializeMethodExitCode()
            throws IllegalActionException {
        return comment("preinitialization exit code");
    }

    /** Generate the preinitialization procedure name.
     *  @return a string for the preinitialization procedure name.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected String _generatePreinitializeMethodProcedureName()
            throws IllegalActionException {
        return "";
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
    @Override
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

    /** Return the prototype for fire functions.
     *  @return In this base class, return "()".
     *  Derived classes, such as the C code generator adapter
     *  might return "(void)".
     */
    protected String _getFireFunctionArguments() {
        return "()";
    }

    /** Generate the code for printing the execution time since
     *  the code generated by _recordStartTime() was called.
     *  This base class only generates a comment.
     *  @return Return the code for printing the total execution time.
     */
    protected String _printExecutionTime() {
        return comment("Print execution time.");
    }

    /** Generate the code for recording the current time.
     *  This base class only generates a comment.
     *  @return Return the code for recording the current time.
     */
    protected String _recordStartTime() {
        return comment("Record current time.");
    }

    /** Reset the code generator.
     *  @exception IllegalActionException Not thrown in this base class,
     *  thrown by the parent if the container of the model
     *  cannot be set to null.
     */
    @Override
    protected void _reset() throws IllegalActionException {
        super._reset();
        // Reset the indent to zero.
        _indent = 0;
        _newTypesUsed.clear();
        _tokenFuncUsed.clear();
        _typeFuncUsed.clear();
        if (_substituteMap != null) {
            _substituteMap.clear();
        }
    }

    /** Perform any setup or initialization of the adapter.
     *  Note that this is not the Ptolemy initialize() method,
     *  this method merely sets up any codegen-time variables
     *  in the adapters.
     *  @exception IllegalActionException If an error occurrs while
     *   initializing an adapter.
     */
    protected void _setupAdapter() throws IllegalActionException {
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getContainer());
        adapter.setupAdapter();
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
                    CodeGeneratorAdapter.generateName(getContainer()) + suffix,
                    code);
        } catch (Throwable throwable) {
            // Ignore
            System.out
            .println("Warning: Failed to split variable declaration: "
                    + throwable);
            throwable.printStackTrace();
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
     *  <dt><code>@CLASSPATHSEPARATOR@</code>
     *  <dd>The value of the path.separator property</dd>
     *  <dt><code>@PTJNI_NO_CYGWIN@</code>,
     *  <code>@PTJNI_SHAREDLIBRARY_CFLAG@</code>,
     *  <code>@PTJNI_SHAREDLIBRARY_LDFLAG@</code>,
     *  <code>@PTJNI_SHAREDLIBRARY_PREFIX@</code>
     *  <code>@PTJNI_SHAREDLIBRARY_SUFFIX@</code></dt>
     *  <dd>Platform dependent compiler variables</dd>
     *  <dt><code>@PTJavaCompiler@</code></dt>
     *  <dd><code>javac</code></dd>
     *  </dl>
     *  @param container The composite actor for which we generate the makefile
     *  @param currentDirectory The director in which the makefile is to be written.
     *  @exception IllegalActionException  If there is a problem reading
     *  a parameter, if there is a problem creating the codeDirectory directory
     *  or if there is a problem writing the code to a file.
     */
    protected void _writeMakefile(CompositeEntity container,
            String currentDirectory) throws IllegalActionException {
        File codeDirectoryFile = new File(currentDirectory);
        if (codeDirectoryFile.isFile()) {
            throw new IllegalActionException(this, "Error: "
                    + codeDirectory.stringValue() + " is a file, "
                    + " it should be a directory.");
        }

        if (!codeDirectoryFile.isDirectory() && !codeDirectoryFile.mkdirs()) {
            throw new IllegalActionException(this, "Failed to make the \""
                    + codeDirectory.stringValue() + "\" directory.");
        }

        // Add substitutions for all the parameter.
        // For example, @generatorPackage@ will be replaced with
        // the value of the generatorPackage.
        _substituteMap.put("@modelName@", _sanitizedModelName);

        _substituteMap.put("@CLASSPATHSEPARATOR@",
                StringUtilities.getProperty("path.separator"));

        // Define substitutions to be used in the makefile
        _substituteMap.put("@PTJNI_NO_CYGWIN@", "");
        _substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@", "");
        _substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@", "");
        _substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "");
        _substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "");
        _substituteMap.put("@PTJavaCompiler@", "javac");

        String osName = StringUtilities.getProperty("os.name");
        if (osName != null) {
            // Keep these alphabetical
            if (osName.startsWith("Linux")) {
                _substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-shared");
                // Need -fPIC for jni actors, see
                // codegen/c/actor/lib/jni/test/auto/Scale.xml
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@", "-fPIC");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@", "-fPIC");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "lib");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "so");
            } else if (osName.startsWith("Mac OS X")) {
                String widthFlag = "";
                if (!JVMBitWidth.is32Bit()) {
                    widthFlag = "-m64 ";
                }
                _substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", widthFlag
                        + "-dynamiclib");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "lib");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "dylib");
            } else if (osName.startsWith("SunOS")) {
                _substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-shared");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_CFLAG@", "-fPIC");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@", "-fPIC");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_PREFIX@", "lib");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "so");
            } else if (osName.startsWith("Windows")) {
                _substituteMap.put("@PTJNI_GCC_SHARED_FLAG@", "-shared");
                _substituteMap.put("@PTJNI_NO_CYGWIN@", "-mno-cygwin");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@",
                        "-Wl,--add-stdcall-alias");
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_SUFFIX@", "dll");
            } else {
                _substituteMap.put("@PTJNI_SHAREDLIBRARY_LDFLAG@",
                        "# Unknown java property os.name \"" + osName
                                + "\" please edit ptolemy/codegen/c/"
                                + "kernel/CCodeGenerator.java and "
                                + "ptolemy/actor/lib/jni/"
                                + "CompiledCompositeActor.java");
            }

        }

        List<String> templateList = new LinkedList<String>();

        // 1. Look for a .mk.in file with the same name as the model.
        URIAttribute uriAttribute = (URIAttribute) _model.getAttribute("_uri",
                URIAttribute.class);
        if (uriAttribute != null) {
            String uriString = uriAttribute.getURI().toString();
            templateList.add(uriString.substring(0,
                    uriString.lastIndexOf("/") + 1)
                    + _sanitizedModelName
                    + ".mk.in");
        }

        String generatorDirectory = generatorPackageList.stringValue().replace(
                '.', '/');

        if (container.getContainer() != null) {
            // We have a embedded code generator
            templateList.add("ptolemy/cg/kernel/" + generatorDirectory
                    + (_isTopLevel() ? "/makefile.in" : "/jnimakefile.in"));

        }

        // 2. If the target parameter is set, look for a makefile.

        // Look for generator specific make file
        templateList.add("ptolemy/cg/kernel/" + generatorDirectory
                + "/makefile.in");

        // Look for generator specific make file
        templateList.add("ptolemy/cg/adapter/" + generatorDirectory
                + "/makefile.in");

        // 3. Look for the generic makefile.in
        // Note this code is repeated in the catch below.

        templateList.add("ptolemy/cg/kernel/" + generatorDirectory
                + "/makefile.in");

        // If necessary, add a trailing / after codeDirectory.
        String makefileOutputName = codeDirectory.stringValue()
                + (!codeDirectory.stringValue().endsWith("/")
                        && !codeDirectory.stringValue().endsWith("\\") ? "/"
                                : "") + _sanitizedModelName + ".mk";

        BufferedReader makefileTemplateReader = null;

        StringBuffer errorMessage = new StringBuffer();
        String makefileTemplateName = null;
        boolean success = false;
        try {
            Iterator<?> templates = templateList.iterator();
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
                            _substituteMap, makefileOutputName);
                    success = true;
                    break;
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to read \"" + makefileTemplateName
                    + "\" or write \"" + makefileOutputName + "\"");
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
        System.out.println("Using " + makefileTemplateName);
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
                    prefix + CodeGeneratorAdapter.generateName(getContainer()),
                    code);
        } catch (Throwable throwable) {
            // Ignore
            System.out.println("Warning: Failed to split code: " + throwable);
            throwable.printStackTrace();
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
    protected String _writeVariableDeclarations(
            List<String> variableDeclarations) throws IllegalActionException {
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

    /** A map from String type name to a HashMap of port name to an
     *  array index.  Ports with a buffer size of 1 end up in this
     *  array.  The {@link #variablesAsArrays} parameter enables use of
     *  this map to reduce the number of variables generated.
     */
    protected HashMap<String, HashMap<String, Integer>> _portTypeMap;

    /** A map from String type name to a HashMap of port name to Array
     *  Index.  The {@link #variablesAsArrays} parameter enables use of
     *  this map to reduce the number of variables generated.
     */
    protected HashMap<String, Integer> _portTypeMaxIndex;

    /** A map from String type name to a HashMap of multiport or port
     *  to an array index.  Multiports with a buffersize of 1 or
     *  ports with a buffer size greater than 1 end up in this array.
     *  The {@link #variablesAsArrays} parameter enables use of
     *  this map to reduce the number of variables generated.
     */
    protected HashMap<String, HashMap<String, Integer>> _portTypeMap2;

    /** A map from String type name to a HashMap of multiport or port
     *  to the maximum number in the corresponding array.
     *  Multiports with a buffersize of 1 or ports with a buffer size
     *  greater than 1 end up in this array.
     *  The {@link #variablesAsArrays} parameter enables use of
     *  this map to reduce the number of variables generated.
     */
    protected HashMap<String, Integer> _portTypeMaxIndex2;

    /** A map from String type name to a HashMap of multiports to an
     *  array index.  Multiports with a buffer size greater than 1 end
     *  up in this array.
     *  The {@link #variablesAsArrays} parameter enables use of
     *  this map to reduce the number of variables generated.
     */
    protected HashMap<String, HashMap<String, Integer>> _portTypeMap3;

    /** A map from String type name to a HashMap of multiports to an
     *  to the maximum number in the corresponding array.
     *  Multiports with a buffer size greater than 1 end up in this
     *  array.
     *  The {@link #variablesAsArrays} parameter enables use of
     *  this map to reduce the number of variables generated.
     */
    protected HashMap<String, Integer> _portTypeMaxIndex3;

    /** A list of the primitive types supported by the code generator.
     */
    protected static List<String> _primitiveTypes = Arrays.asList(new String[] {
            "Int", "Double", "String", "Long", "Boolean", "UnsignedByte",
    "Pointer" });

    /** The initial default value of the <i>runCommand</i> parameter.
     *  The constructor of a derived class may compare the value of <i>runCommand</i>
     *  and this variable and decide to override the value of the <i>runCommand</i>
     *  parameter with a new value.
     */
    protected final static String _runCommandDefault = "make -f @modelName@.mk run";

    /** Map of '@' delimited keys to values.  Used to create
     *  the makefile from makefile.in.
     *  Use "@help:all@" to list all key/value pairs.
     */
    protected Map<String, String> _substituteMap;

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

    /** A map from an IOPort name to a table index.
     *  This is used when the variablesAsArrays parameter of the code generator
     *  is set to true.
     */
    private static HashMap<String, Integer> _ioPortMap = new HashMap<String, Integer>();

    /** A map from a TypedAtomicActor name to a table index.
     *  This is used when the variablesAsArrays parameter of the code generator
     *  is set to true.
     */
    private static HashMap<String, Integer> _typedCompositeActorMap = new HashMap<String, Integer>();

    /** The extension of the template files.
     *   (for example c in case of C and j in case of Java)
     */
    private String _templateExtension;
}
