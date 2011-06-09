/* Base class for Java code generator helper.

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

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.ParseTreeCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JavaCodeGeneratorHelper

/**
 Base class for C code generator helper.

 <p>Actor helpers extend this class and optionally define the
 generateFireCode(),
 generateInitializeCode(), generatePrefireCode(),
 generatePostfireCode(), generatePreinitializeCode(), and
 generateWrapupCode() methods.

 <p> In derived classes, these methods,
 if present, make actor specific changes to the corresponding code.
 If these methods are not present, then the parent class will automatically
 read the corresponding .c file and subsitute in the corresponding code
 block.  For example, generateInitializeCode() reads the
 <code>initBlock</code>, processes the macros and adds the resulting
 code block to the output.

 <p>For a complete list of methods to define, see
 {@link ptolemy.codegen.kernel.CodeGeneratorHelper}.

 <p>For further details, see <code>$PTII/ptolemy/codegen/README.html</code>

 @author Christopher Brooks, Edward Lee, Man-Kit Leung, Gang Zhou, Ye Zhou
 @version $Id$
 @since Ptolemy II 8.0
 o @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JavaCodeGeneratorHelper extends CodeGeneratorHelper {
    /**
     * Create a new instance of the C code generator helper.
     * @param component The actor object for this helper.
     */
    public JavaCodeGeneratorHelper(NamedObj component) {
        super(component);
        _parseTreeCodeGenerator = new JavaParseTreeCodeGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the translated new constructor invocation string. Keep the types
     *  referenced in the info table of this helper. The kernel will retrieve
     *  this information to determine the total number of referenced types in
     *  the model.
     *  @param constructorString The string within the $new() macro.
     *  @return The translated new constructor invocation string.
     *  @exception IllegalActionException The given constructor string is
     *   not well-formed.
     */
    public String getNewInvocation(String constructorString)
            throws IllegalActionException {
        addFunctionUsed("new");
        return super.getNewInvocation(constructorString);
    }

    /** Return a new parse tree code generator to use with expressions.
     *  @return the parse tree code generator to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        // FIXME: We need to create new ParseTreeCodeGenerator each time
        // here or else we get lots of test failures.  It would be better
        // if we could use the same JavaParseTreeCodeGenerator over and over.
        if (!(_parseTreeCodeGenerator instanceof JavaParseTreeCodeGenerator)) {
            _parseTreeCodeGenerator = new JavaParseTreeCodeGenerator();
        }
        return _parseTreeCodeGenerator;
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        String name = CodeGeneratorHelper.generateName(getComponent());
        // Generate variable declarations for referenced parameters.
        String referencedParameterDeclaration = _generateReferencedParameterDeclaration();
        if (referencedParameterDeclaration.length() > 1) {
            code.append(_eol
                    + _codeGenerator.comment(name
                            + "'s referenced parameter declarations."));
            code.append(referencedParameterDeclaration);
        }

        // Generate variable declarations for input ports.
        String inputVariableDeclaration = _generateInputVariableDeclaration();
        if (inputVariableDeclaration.length() > 1) {
            code.append(_eol
                    + _codeGenerator.comment(name
                            + "'s input variable declarations."));
            code.append(inputVariableDeclaration);
        }

        // Generate variable declarations for output ports.
        String outputVariableDeclaration = _generateOutputVariableDeclaration();
        if (outputVariableDeclaration.length() > 1) {
            code.append(_eol
                    + _codeGenerator.comment(name
                            + "'s output variable declarations."));
            code.append(outputVariableDeclaration);
        }

        // Generate type convert variable declarations.
        String typeConvertVariableDeclaration = _generateTypeConvertVariableDeclaration();
        if (typeConvertVariableDeclaration.length() > 1) {
            code.append(_eol
                    + _codeGenerator.comment(name
                            + "'s type convert variable declarations."));
            code.append(typeConvertVariableDeclaration);
        }

        return processCode(code.toString());
    }

    /** Get the code generator associated with this helper class.
     *  @return The code generator associated with this helper class.
     *  @see #setCodeGenerator(CodeGenerator)
     */
    public JavaCodeGenerator getCodeGenerator() {
        return (JavaCodeGenerator) _codeGenerator;
    }

    /** Return the translated token instance function invocation string.
     *  @param functionString The string within the $tokenFunc() macro.
     *  @param isStatic True if the method is static.
     *  @return The translated type function invocation string.
     *  @exception IllegalActionException The given function string is
     *   not well-formed.
     */
    public String getFunctionInvocation(String functionString, boolean isStatic)
            throws IllegalActionException {
        // Record the referenced type function in the infoTable.
        super.getFunctionInvocation(functionString, isStatic);

        // FIXME: lots of duplicated code from superclass here.
        functionString = processCode(functionString);

        // i.e. "$tokenFunc(token::add(arg1, arg2, ...))"
        // this transforms to ==>
        // "functionTable[token.type][FUNC_add] (token, arg1, arg2, ...)"
        // FIXME: we need to do some more smart parsing to find the following
        // indexes.
        int commaIndex = functionString.indexOf("::");
        int openFuncParenIndex = functionString.indexOf('(', commaIndex);
        int closeFuncParenIndex = functionString.lastIndexOf(')');

        // Syntax checking.
        if ((commaIndex == -1) || (openFuncParenIndex == -1)
                || (closeFuncParenIndex != (functionString.length() - 1))) {
            throw new IllegalActionException(
                    "Bad Syntax with the $tokenFunc / $typeFunc macro. "
                            + "[i.e. -- $tokenFunc(typeOrToken::func(arg1, ...))].  "
                            + "String was:\n:" + functionString);
        }

        String typeOrToken = functionString.substring(0, commaIndex).trim();
        String functionName = functionString.substring(commaIndex + 2,
                openFuncParenIndex).trim();

        String argumentList = functionString.substring(openFuncParenIndex + 1)
                .trim();

        if (isStatic) {

            if (argumentList.length() == 0) {
                throw new IllegalActionException(
                        "Static type function requires at least one argument(s).");
            }

            //return "functionTable[(int)" + typeOrToken + "][FUNC_"
            //+ functionName + "](" + argumentList;

            String methodType = typeOrToken
                    .substring(typeOrToken.indexOf('_') + 1);
            return methodType + "_" + functionName + "(" + argumentList;

        } else {

            // if it is more than just a closing paren
            if (argumentList.length() > 1) {
                argumentList = ", " + argumentList;
            }

            //return "functionTable[(int)" + typeOrToken + ".type][FUNC_"
            //+ functionName + "](" + typeOrToken + argumentList;
            //String methodType = typeOrToken.substring(typeOrToken.indexOf('_') + 1);
            getCodeGenerator().markFunctionCalled(
                    functionName + "_Token_Token", this);
            return functionName + "_Token_Token(" + typeOrToken + argumentList;
        }
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.addAll(_includeFiles);
        return files;
    }

    /** Get the header files needed to compile with the jvm library.
     *  @return A set of strings that are names of the header files
     *   needed by the code generated for jvm library
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getJVMHeaderFiles() throws IllegalActionException {
        String javaHome = StringUtilities.getProperty("java.home");

        ExecuteCommands executeCommands = getCodeGenerator()
                .getExecuteCommands();
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
        executeCommands.stdout(_eol + _eol
                + "JavaCodeGeneratorHelper: appended to path "
                + jreBinClientPath);

        executeCommands.appendToPath(jreBinClientPath);

        javaHome = javaHome.replace('\\', '/');
        if (javaHome.endsWith("/jre")) {
            javaHome = javaHome.substring(0, javaHome.length() - 4);
        }

        if (!(new File(javaHome + "/include").isDirectory())) {
            // It could be that we are running under WebStart
            // or otherwise in a JRE, so we should look for the JDK.
            File potentialJavaHomeParentFile = new File(javaHome)
                    .getParentFile();
            // Loop through twice, once with the parent, once with
            // C:/Program Files/Java.  This is lame, but easy
            for (int loop = 2; loop > 0; loop--) {
                // Get all the directories that have include/jni.h under them.
                File[] jdkFiles = potentialJavaHomeParentFile
                        .listFiles(new FileFilter() {
                            public boolean accept(File pathname) {
                                return new File(pathname, "/include/jni.h")
                                        .canRead();
                            }
                        });
                if (jdkFiles != null && jdkFiles.length >= 1) {
                    // Sort and get the last directory, which should
                    // be the most recent JDK.
                    java.util.Arrays.sort(jdkFiles);
                    javaHome = jdkFiles[jdkFiles.length - 1].toString();
                    break;
                } else {
                    // Not found, please try again.
                    potentialJavaHomeParentFile = new File(
                            "C:\\Program Files\\Java");
                }
            }
        }

        getCodeGenerator().addInclude("-I\"" + javaHome + "/include\"");

        String osName = StringUtilities.getProperty("os.name");
        String platform = "win32";
        if (osName.startsWith("Linux")) {
            platform = "linux";
        } else if (osName.startsWith("SunOS")) {
            platform = "solaris";
        } else if (osName.startsWith("Mac OS X")) {
            platform = "Mac OS X";
        }
        String jvmLoaderDirective = "-ljvm";
        String libjvmAbsoluteDirectory = "";
        if (platform.equals("win32")) {
            getCodeGenerator().addInclude(
                    "-I\"" + javaHome + "/include/" + platform + "\"");

            // The directive we use to find jvm.dll, which is usually in
            // something like c:/Program Files/Java/jre1.6.0_04/bin/client/jvm.dll
            jvmLoaderDirective = "-ljvm";

            String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir")
                    .replace('\\', '/');
            String libjvmRelativeDirectory = "ptolemy/codegen/c/lib/win";
            libjvmAbsoluteDirectory = ptIIDir + "/" + libjvmRelativeDirectory;
            String libjvmFileName = "libjvm.dll.a";
            String libjvmPath = libjvmAbsoluteDirectory + "/" + libjvmFileName;

            if (!(new File(libjvmPath).canRead())) {
                // If we are under WebStart or running from jar files, we
                // will need to copy libjvm.dll.a from the jar file
                // that gcc can find it.
                URL libjvmURL = Thread.currentThread().getContextClassLoader()
                        .getResource(
                                libjvmRelativeDirectory + "/" + libjvmFileName);
                if (libjvmURL != null) {
                    String libjvmAbsolutePath = null;
                    try {
                        // Look for libjvm.dll.a in the codegen directory
                        File libjvmFileCopy = new File(
                                getCodeGenerator().codeDirectory.asFile(),
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
                                    .substring(0, libjvmAbsolutePath
                                            .lastIndexOf("/"));

                            // Get rid of everything before the last /lib
                            // and the .dll.a
                            jvmLoaderDirective = "-l"
                                    + libjvmAbsolutePath.substring(
                                            libjvmAbsolutePath
                                                    .lastIndexOf("/lib") + 4,
                                            libjvmAbsolutePath.length() - 6);

                        }
                    } catch (Exception ex) {
                        throw new IllegalActionException(getComponent(), ex,
                                "Could not copy \"" + libjvmURL
                                        + "\" to the file system, path was: \""
                                        + libjvmAbsolutePath + "\"");
                    }
                }
            }
        } else if (platform.equals("Mac OS X")) {
            if (javaHome != null) {
                libjvmAbsoluteDirectory = javaHome + "/../Libraries";
            }
        } else {
            // Solaris, Linux etc.
            getCodeGenerator().addInclude(
                    "-I\"" + javaHome + "/include/" + platform + "\"");
        }
        getCodeGenerator().addLibrary("-L\"" + libjvmAbsoluteDirectory + "\"");
        getCodeGenerator().addLibrary(jvmLoaderDirective);

        Set files = new HashSet();
        files.add("<jni.h>");
        return files;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate input variable declarations.
     *  @return a String that declares input variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    protected String _generateInputVariableDeclaration()
            throws IllegalActionException {

        StringBuffer code = new StringBuffer();

        Iterator inputPorts = ((Actor) getComponent()).inputPortList()
                .iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            if (!inputPort.isOutsideConnected()) {
                continue;
            }

            _portVariableDeclaration(code, inputPort);
        }

        return code.toString();
    }

    /** Generate output variable declarations.
     *  @return a String that declares output variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    protected String _generateOutputVariableDeclaration()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator outputPorts = ((Actor) getComponent()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            if (!outputPort.isOutsideConnected()
                    || outputPort.isInsideConnected()) {
                _portVariableDeclaration(code, outputPort);
            }
        }

        return code.toString();
    }

    /** Generate referenced parameter declarations.
     *  @return a String that declares referenced parameters.
     *  @exception IllegalActionException If thrown while
     *  getting modified variable information.
     */
    protected String _generateReferencedParameterDeclaration()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        if (_referencedParameters != null) {
            Iterator parameters = _referencedParameters.iterator();

            while (parameters.hasNext()) {
                Parameter parameter = (Parameter) parameters.next();

                // avoid duplicate declaration.
                if (!_codeGenerator.getModifiedVariables().contains(parameter)) {
                    code.append("static " + targetType(parameter.getType())
                            + " " + generateVariableName(parameter) + ";"
                            + _eol);
                }
            }
        }

        return code.toString();
    }

    /** Generate type convert variable declarations.
     *  @return a String that declares type convert variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    protected String _generateTypeConvertVariableDeclaration()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator channels = _getTypeConvertChannels().iterator();
        while (channels.hasNext()) {
            Channel channel = (Channel) channels.next();
            Type portType = ((TypedIOPort) channel.port).getType();

            if (isPrimitive(portType)) {

                code.append("static ");
                code.append(targetType(portType));
                code.append(" " + _getTypeConvertReference(channel));

                //int bufferSize = getBufferSize(channel.port);
                int bufferSize = Math.max(DFUtilities
                        .getTokenProductionRate(channel.port), DFUtilities
                        .getTokenConsumptionRate(channel.port));

                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
        }
        return code.toString();
    }

    public String getReference(Attribute attribute, String[] channelAndOffset)
            throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        //FIXME: potential bug: if the attribute is not a parameter,
        //it will be referenced but not declared.
        if (attribute instanceof Parameter) {
            _referencedParameters.add(attribute);
        }

        result.append(_codeGenerator.generateVariableName(attribute));

        if (!channelAndOffset[0].equals("")) {
            throw new IllegalActionException(getComponent(),
                    "a parameter cannot have channel number.");
        }

        if (!channelAndOffset[1].equals("")) {
            Type elementType = ((ArrayType) ((Parameter) attribute).getType())
                    .getElementType();

            //result.append("[" + channelAndOffset[1] + "]");
            result.insert(0, "("
                    + codeGenType(elementType).replace("Array", "Token")
                            .replace("Matrix", "Token")
                    + ")(/*JCGH44*/Array_get(");
            if (isPrimitive(elementType)) {
                result.insert(0, "(");
            }

            result.append(" ," + channelAndOffset[1] + ")");

            if (isPrimitive(elementType)) {
                String cgType = codeGenType(elementType).toLowerCase();
                if (cgType.equals("integer")) {
                    cgType = "int";
                }
                String operator = "Value()";
                if (cgType.equals("string")) {
                    cgType = "";
                    operator = "toString()";
                }
                result.append(".payload/*jcgh2*/))." + cgType + operator);
            } else {
                result.append(")");
            }
        }
        return result.toString();
    }

    protected String _replaceMacro(String macro, String parameter)
            throws IllegalActionException {
        String result = super._replaceMacro(macro, parameter);

        if (result != null) {
            if (macro.equals("cgType")) {
                return result.replace("Int", "Integer").replace("Integereger",
                        "Integer");
            }
            return result;
        }

        if (macro.equals("include")) {
            _includeFiles.add(parameter);
            return "";
        } else if (macro.equals("refinePrimitiveType")) {
            TypedIOPort port = getPort(parameter);

            if (port == null) {
                throw new IllegalActionException(
                        parameter
                                + " is not a port. $refinePrimitiveType macro takes in a port.");
            }
            if (isPrimitive(port.getType())) {
                return ".payload/*jcgh*/." + codeGenType(port.getType());
            } else {
                return "";
            }
        } else if (macro.equals("lcCgType")) {
            String cgType = _replaceMacro("cgType", parameter);
            if (cgType.equals("Integer")) {
                return "int";
            }
            return cgType.toLowerCase();
        }

        // We will assume that it is a call to a polymorphic
        // functions.
        //String[] call = macro.split("_");
        getCodeGenerator().markFunctionCalled(macro, this);
        result = macro + "(" + parameter + ")";

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _portVariableDeclaration(StringBuffer code, TypedIOPort port)
            throws IllegalActionException {

        code.append("static " + targetType(port.getType()) + " "
                + generateName(port));

        int bufferSize = getBufferSize(port);

        if (port.isMultiport()) {
            code.append("[]");
            if (bufferSize > 1) {
                code.append("[]");
            }
            code.append(" = new " + targetType(port.getType()));
        } else {
            if (bufferSize > 1) {
                code.append("[]");
                code.append(" = new " + targetType(port.getType()));
            } else {
                //code.append(" = ");
            }
        }

        if (port.isMultiport()) {
            code.append("[" + port.getWidth() + "]");
        }

        if (bufferSize > 1) {
            code.append("[" + bufferSize + "]");
        } else {
            //code.append("0");
        }
        code.append(";" + _eol);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The set of header files that needed to be included. */
    private Set _includeFiles = new HashSet();

    /** True if we have printed the JVM warning. */
    private boolean _printedJVMWarning;
}
