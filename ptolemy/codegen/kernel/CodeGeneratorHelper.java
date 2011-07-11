/*
 * Base class for code generator helper.
 *
 * Copyright (c) 2005-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 */
package ptolemy.codegen.kernel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.codegen.actor.Director;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

////CodeGeneratorHelper

/**
 * Base class for code generator helper.
 *
 * <p>
 * Subclasses should override generateFireCode(), generateInitializeCode()
 * generatePostfireCode(), generatePreinitializeCode(), and generateWrapupCode()
 * methods by appending a corresponding code block.
 *
 * <p>
 * Subclasses should be sure to properly indent the code by either using the
 * code block functionality in methods like {@link #_generateBlockCode(String)}
 * or by calling {@link ptolemy.codegen.kernel.CodeStream#indent(String)}, for
 * example:
 *
 * <pre>
 * StringBuffer code = new StringBuffer();
 * code.append(super.generateWrapupCode());
 * code.append(&quot;// Local wrapup code&quot;);
 * return processCode(CodeStream.indent(code.toString()));
 * </pre>
 *
 * @author Ye Zhou, Gang Zhou, Edward A. Lee, Contributors: Christopher Brooks,
 * Teale Fristoe
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGeneratorHelper extends NamedObj implements ActorCodeGenerator {

    /**
     * Construct a code generator helper.
     */
    public CodeGeneratorHelper() {
        this(null);
    }

    /**
     * Construct the code generator helper associated with the given component.
     * @param component The associated component.
     */
    public CodeGeneratorHelper(NamedObj component) {
        this((Object) component);
    }

    /**
     * Construct the code generator helper associated with the given component.
     * @param component The associated component.
     * @param name The name of helper. All periods are replaced with
     * underscores.
     */
    public CodeGeneratorHelper(NamedObj component, String name) {
        this(component);

        try {
            setName(name.replaceAll("\\.", "_") + " helper");
        } catch (IllegalActionException ex) {
            // This should not occur.
        } catch (NameDuplicationException ex) {
            // FIXME: May not be important to handle.
        }
    }

    /**
     * Construct a code generator helper.
     * @param object The object for which the code generator should be constructed.
     */
    public CodeGeneratorHelper(Object object) {
        // FIXME: Why is this a namedObj when the analyzeActor()
        // method requires an Actor?
        _object = object;

        _parseTreeCodeGenerator = new ParseTreeCodeGenerator() {
            /**
             * Given a string, escape special characters as necessary for the
             * target language.
             * @param string The string to escape.
             * @return A new string with special characters replaced.
             */
            public String escapeForTargetLanguage(String string) {
                return string;
            }

            /**
             * Evaluate the parse tree with the specified root node using the
             * specified scope to resolve the values of variables.
             * @param node The root of the parse tree.
             * @param scope The scope for evaluation.
             * @return The result of evaluation.
             * @exception IllegalActionException If an error occurs during
             * evaluation.
             */
            public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
                    ParserScope scope) {
                return new Token();
            }

            /**
             * Generate code that corresponds with the fire() method.
             * @return The generated code.
             */
            public String generateFireCode() {
                return "/* ParseTreeCodeGenerator.generateFireCode() "
                        + "not implemented in codegen.kernel.CodeGenerator */";
            }
        };
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a type to the Set of types used thus far.
     * @param typeName A string naming the type, for example "Boolean" or
     * "String".
     */
    public void addNewTypeUsed(String typeName) {
        _codeGenerator._newTypesUsed.add(typeName);
    }

    /**
     * Add a functiom to the Set of functions used thus far.
     * @param functionName A string naming a function, for example "new".
     */
    public void addFunctionUsed(String functionName) {
        _codeGenerator._typeFuncUsed.add(functionName);
    }

    /**
     * Find out each output port that needs to be converted for the actor
     * associated with this helper. Then, mark these ports along with the sink
     * ports (connection).
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void analyzeTypeConvert() throws IllegalActionException {
        // reset the previous type convert info.
        _portConversions.clear();

        Actor actor = (Actor) _object;

        ArrayList sourcePorts = new ArrayList();
        sourcePorts.addAll(actor.outputPortList());

        if (actor instanceof CompositeActor) {
            sourcePorts.addAll(actor.inputPortList());
        }

        Iterator ports = sourcePorts.iterator();

        // for each output port.
        for (int i = 0; ports.hasNext(); i++) {
            TypedIOPort sourcePort = (TypedIOPort) ports.next();

            // for each channel.
            for (int j = 0; j < sourcePort.getWidth(); j++) {
                Iterator sinks = getSinkChannels(sourcePort, j).iterator();

                // for each sink channel connected.
                while (sinks.hasNext()) {
                    Channel sink = (Channel) sinks.next();
                    TypedIOPort sinkPort = (TypedIOPort) sink.port;
                    if (!sourcePort.getType().equals(sinkPort.getType())) {
                        _markTypeConvert(new Channel(sourcePort, j), sink);
                    }
                }
            }
        }
    }

    /**
     * Return true if the port is a local port.
     * @param forComposite True if this check is for a composite
     * @param port The port to be checked.
     * @return true if the port is a local port.
     * @exception IllegalActionException If thrown while queryint the port.
     */
    public boolean checkLocal(boolean forComposite, IOPort port)
            throws IllegalActionException {
        return port.isInput() && !forComposite && port.isOutsideConnected()
                || port.isOutput() && forComposite;
    }

    /**
     * Return true if the port is a remote port.
     * @param forComposite True if this check is for a composite
     * @param port The port to be checked.
     * @return true if the port is a remote port.
     */
    public boolean checkRemote(boolean forComposite, IOPort port) {
        return port.isOutput() && !forComposite || port.isInput()
                && forComposite;
    }

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
     * @exception IllegalActionException Thrown if the given ptolemy cannot be
     * resolved.
     */
    public String codeGenType(Type ptType) {
        return _codeGenerator.codeGenType(ptType);
    }

    /**
     * Generate code for declaring read and write offset variables if needed.
     * Return empty string in this base class.
     *
     * @return The empty string.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        return "";
    }

    /**
     * Generate the fire code. In this base class, add the name of the
     * associated component in the comment. It checks the inline parameter of
     * the code generator. If the value is true, it generates the actor fire
     * code and the necessary type conversion code. Otherwise, it generate an
     * invocation to the actor function that is generated by
     * generateFireFunctionCode.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        String composite = getComponent() instanceof CompositeActor ? "Composite Actor: "
                : "";

        code.append(_eol
                + _codeGenerator.comment("Fire " + composite
                        + generateName(getComponent())));

        // Generate, process and store the fire code now because
        // we don't want to call _generateFireCode() in other places.
        // We should process the fire code here so the code generator can
        // infer information from the code blocks.
        _fireCode = processCode(_generateFireCode());

        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE) {
            code.append(_fireCode);
            code.append(generateTypeConvertFireCode());
        } else if (getCodeGenerator().getContainer().getContainer() != null) {
            // Here we test whether the codegenerator is embedded in another actor or whether it
            // is at the toplevel. In it is embedded we don't need to generateTypeConvertFireCode.
            // Needed for jni and embeddedJava.
            code.append(_fireCode);
        } else {
            code.append(_generateFireInvocation(getComponent()) + ";" + _eol);
        }

        try {
            copyFilesToCodeDirectory(getComponent(), _codeGenerator);
        } catch (IOException ex) {
            throw new IllegalActionException(getComponent(), ex,
                    "Problem copying files from the necessaryFiles parameter.");
        }
        return processCode(code.toString());
    }

    /**
     * Generate The fire function code. This method is called when the firing
     * code of each actor is not inlined. Each actor's firing code is in a
     * function with the same name as that of the actor.
     *
     * @return The fire function code.
     * @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_eol + "void " + generateName(getComponent())
                + _getFireFunctionArguments()
                /*+ _codeGenerator.comment(getClass().getName() + ".generateFireFunctionCode()")*/
                + " {" + _eol);

        code.append(_generateFireCode());
        code.append(generateTypeConvertFireCode());
        code.append("}" + _eol);

        try {
            copyFilesToCodeDirectory(getComponent(), _codeGenerator);
        } catch (IOException ex) {
            throw new IllegalActionException(getComponent(), ex,
                    "Problem copying files from the necessaryFiles parameter.");
        }

        return processCode(code.toString());
    }

    /**
     * Generate The fire function code. This method is called when generating
     * code for a Giotto director. Produce the fire code but do not transfer
     * generate any type conversion code that sends this actors output to
     * another actor's input. The typeConversion and moving from an output to an
     * input should be taken care of in a driver method not the fire method.
     * This works under that assumption that _generateFireCode() also generates
     * the code for postfire. If later we generate postfire code in a different
     * method please add that method call to this method
     * @return The fire function code.
     * @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode2() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_generateFireCode());
        return processCode(code.toString());
    }

    /**
     * Generate the initialize code. In this base class, return empty string.
     * Subclasses may extend this method to generate initialize code of the
     * associated component and append the code to the given string buffer.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If thrown while appending to the the
     * block or processing the macros.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return _generateBlockByName(_defaultBlocks[1]);
    }

    /**
     * Generate mode transition code. The mode transition code generated in this
     * method is executed after each global iteration, e.g., in HDF model. Do
     * nothing in this base class.
     *
     * @param code The string buffer that the generated code is appended to.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
    }

    /**
     * Generate the expression that represents the offset in the generated code.
     * @param offsetString The specified offset from the user.
     * @param port The referenced port.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @return The expression that represents the offset in the generated code.
     * @exception IllegalActionException If there is problems getting the port
     * buffer size or the offset in the channel and offset map.
     */
    public String generateOffset(String offsetString, IOPort port, int channel,
            boolean isWrite) throws IllegalActionException {

        ptolemy.actor.Director director = getDirector();

        PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);

        return processCode(portHelper.generateOffset(offsetString, channel,
                isWrite, director));
    }

    /**
     * Generate the postfire code. In this base class, do nothing. Subclasses
     * may extend this method to generate the postfire code of the associated
     * component and append the code to the given string buffer.
     *
     * @return The generated postfire code.
     * @exception IllegalActionException If thrown while appending to the the
     * block or processing the macros.
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_generateBlockByName(_defaultBlocks[3]));

        //        Actor actor = (Actor) getComponent();
        //        for (IOPort port : (List<IOPort>) actor.outputPortList()) {
        //            CodeGeneratorHelper portHelper = (CodeGeneratorHelper) _getHelper(port);
        //            code.append(portHelper.generatePostfireCode());
        //        }
        return processCode(code.toString());
    }

    public String generatePrefireCode() throws IllegalActionException {
        // FIXME: This is to be used in future re-structuring.
        StringBuffer code = new StringBuffer();
        //Actor actor = (Actor) getComponent();
        //for (IOPort port : (List<IOPort>) actor.inputPortList()) {
        //    CodeGeneratorHelper portHelper = (CodeGeneratorHelper) _getHelper(port);
        //    code.append(portHelper.generatePrefireCode());
        //}
        return processCode(code.toString());
    }

    /**
     * Generate the preinitialize code. In this base class, return an empty
     * string. This method generally does not generate any execution code and
     * returns an empty string. Subclasses may generate code for variable
     * declaration, defining constants, etc.
     * @return A string of the preinitialize code for the helper.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        _createBufferSizeAndOffsetMap();

        return _generateBlockByName(_defaultBlocks[0]);
    }

    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateTypeConvertFireCode() throws IllegalActionException {
        return generateTypeConvertFireCode(false);
    }

    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * @param forComposite True if we are generating code for a composite.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateTypeConvertFireCode(boolean forComposite)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Type conversion code for inter-actor port conversion.
        Iterator channels = _getTypeConvertChannels().iterator();
        while (channels.hasNext()) {
            Channel source = (Channel) channels.next();

            if (!forComposite && source.port.isOutput() || forComposite
                    && source.port.isInput()) {

                Iterator sinkChannels = _getTypeConvertSinkChannels(source)
                        .iterator();

                while (sinkChannels.hasNext()) {
                    Channel sink = (Channel) sinkChannels.next();
                    code.append(_generateTypeConvertStatements(source, sink));
                }
            }
        }
        return code.toString();
    }

    /**
     * Generate variable declarations for inputs and outputs and parameters.
     * Append the declarations to the given string buffer.
     * @return code The generated code.
     * @exception IllegalActionException If the helper class for the model
     * director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        return "";
    }

    /**
     * Generate variable initialization for the referenced parameters.
     * @return code The generated code.
     * @exception IllegalActionException If the helper class for the model
     * director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        //  Generate variable initialization for referenced parameters.
        if (!_referencedParameters.isEmpty()) {
            code.append(_eol
                    + _codeGenerator.comment(1,
                            generateSimpleName(getComponent())
                                    + "'s parameter initialization"));

            Iterator parameters = _referencedParameters.iterator();

            while (parameters.hasNext()) {
                Parameter parameter = (Parameter) parameters.next();
                try {
                    // avoid duplication.
                    if (!_codeGenerator._modifiedVariables.contains(parameter)) {
                        code.append(_INDENT1
                                + _codeGenerator
                                        .generateVariableName(parameter)
                                + " = "
                                + getParameterValue(
                                        generateSimpleName(parameter),
                                        getComponent()) + ";" + _eol);
                    }
                } catch (Throwable throwable) {
                    throw new IllegalActionException(getComponent(), throwable,
                            "Failed to generate variable initialization for \""
                                    + parameter + "\"");
                }
            }
        }
        return code.toString();
    }

    /**
     * Generate a variable name for the NamedObj.
     * @param namedObj The NamedObj to generate variable name for.
     * @see ptolemy.codegen.kernel.CodeGenerator#generateVariableName(NamedObj)
     * @return The variable name for the NamedObj.
     */
    public String generateVariableName(NamedObj namedObj) {
        return _codeGenerator.generateVariableName(namedObj);
    }

    /**
     * Generate the wrapup code. In this base class, do nothing. Subclasses may
     * extend this method to generate the wrapup code of the associated
     * component and append the code to the given string buffer.
     *
     * @return The generated wrapup code.
     * @exception IllegalActionException If thrown while appending to the the
     * block or processing the macros.
     */
    public String generateWrapupCode() throws IllegalActionException {
        return _generateBlockByName(_defaultBlocks[4]);
    }

    /**
     * Return the buffer size of a given port, which is the maximum of the
     * bufferSizes of all channels of the given port.
     * @param port The given port.
     * @return The buffer size of the given port.
     * @exception IllegalActionException If the
     * {@link #getBufferSize(IOPort, int)} method throws it.
     * @see #setBufferSize(IOPort, int, int)
     */
    public int getBufferSize(IOPort port) throws IllegalActionException {
        int bufferSize = 1;

        if (port.getContainer() == getComponent()) {
            int length = 0;

            if (port.isInput()) {
                length = port.getWidth();
            } else {
                length = port.getWidthInside();
            }

            for (int i = 0; i < length; i++) {
                int channelBufferSize = getBufferSize(port, i);

                if (channelBufferSize > bufferSize) {
                    bufferSize = channelBufferSize;
                }
            }
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(port
                    .getContainer());
            bufferSize = actorHelper.getBufferSize(port);
        }

        return bufferSize;
    }

    /**
     * Get the buffer size of the given port of this actor.
     * @param port The given port.
     * @param channelNumber The given channel.
     * @return The buffer size of the given port and channel.
     * @exception IllegalActionException If the getBufferSize() method of the
     * actor helper class throws it.
     * @see #setBufferSize(IOPort, int, int)
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        return ((PortCodeGenerator) _getHelper(port))
                .getBufferSize(channelNumber);
    }

    /**
     * Get the code generator associated with this helper class.
     * @return The code generator associated with this helper class.
     * @see #setCodeGenerator(CodeGenerator)
     */
    public CodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    /**
     * Get the component associated with this helper.
     * @return The associated component.
     */
    public NamedObj getComponent() {
        return (NamedObj) _object;
    }

    /**
     *  Return the executive director.  If there is no executive
     *  director, then return the director associated with the
     *  object passed in to the constructor.
     *  @return the executive director or the director of the actor.
     */
    public ptolemy.actor.Director getDirector() {
        ptolemy.actor.Director director = ((Actor) _object)
                .getExecutiveDirector();

        if (director == null) {
            // getComponent() is at the top level. Use its local director.
            director = ((Actor) _object).getDirector();
        }
        return director;
    }

    /**
     * Return the helper of the director.
     * @return Return the helper of the director.
     * @exception IllegalActionException If thrown while getting the helper
     * of the director.
     */
    public Director getDirectorHelper() throws IllegalActionException {
        return (Director) _getHelper(getDirector());
    }

    /**
     * Return the translated token instance function invocation string.
     * @param functionString The string within the $tokenFunc() macro.
     * @param isStatic True if the method is static.
     * @return The translated type function invocation string.
     * @exception IllegalActionException The given function string is not
     * well-formed.
     */
    public String getFunctionInvocation(String functionString, boolean isStatic)
            throws IllegalActionException {
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
        if (commaIndex == -1 || openFuncParenIndex == -1
                || closeFuncParenIndex != functionString.length() - 1) {
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
            // Record the referenced type function in the infoTable.
            _codeGenerator._typeFuncUsed.add(functionName);

            if (argumentList.length() == 0) {
                throw new IllegalActionException(
                        "Static type function requires at least one argument(s).");
            }

            return "functionTable[(int)" + typeOrToken + "][FUNC_"
                    + functionName + "](" + argumentList;

        } else {
            // Record the referenced type function in the infoTable.
            _codeGenerator._tokenFuncUsed.add(functionName);

            // if it is more than just a closing paren
            if (argumentList.length() > 1) {
                argumentList = ", " + argumentList;
            }

            return "functionTable[(int)" + typeOrToken + ".type][FUNC_"
                    + functionName + "](" + typeOrToken + argumentList;
        }
    }

    /**
     * Get the files needed by the code generated from this helper class. This
     * base class returns an empty set.
     * @return A set of strings that are header files needed by the code
     * generated from this helper class.
     * @exception IllegalActionException Not Thrown in this base class.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();

        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("includeFiles", true);
        String includeFilesString = codeStream.toString();

        if (includeFilesString.length() > 0) {
            LinkedList includeFilesList = null;
            try {
                includeFilesList = StringUtilities
                        .readLines(includeFilesString);
            } catch (IOException e) {
                throw new IllegalActionException(
                        "Unable to read include files for " + getName());
            }
            files.addAll(includeFilesList);
        }

        return files;
    }

    /**
     * Return a set of directories to include for the generated code.
     * @return A Set containing the contents of the actor's "includeDirectories"
     * block in its template.
     * @exception IllegalActionException If thrown when getting or reading the
     * CodeStream.
     */
    public Set getIncludeDirectories() throws IllegalActionException {
        Set includeDirectories = new HashSet();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("includeDirectories", true);
        String includeDirectoriesString = codeStream.toString();

        if (includeDirectoriesString.length() > 0) {
            LinkedList includeDirectoriesList = null;
            try {
                includeDirectoriesList = StringUtilities
                        .readLines(includeDirectoriesString);
            } catch (IOException e) {
                throw new IllegalActionException(
                        "Unable to read include directories for " + getName());
            }
            includeDirectories.addAll(includeDirectoriesList);
        }

        return includeDirectories;
    }

    /**
     * Return a set of libraries to link in the generated code.
     * @return A Set containing the libraries in the actor's "libraries" block
     * in its template.
     * @exception IllegalActionException If thrown when getting or reading the
     * CodeStream.
     */
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new HashSet();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("libraries", true);
        String librariesString = codeStream.toString();

        if (librariesString.length() > 0) {
            LinkedList librariesList = null;
            try {
                librariesList = StringUtilities.readLines(librariesString);
            } catch (IOException e) {
                throw new IllegalActionException(
                        "Unable to read libraries for " + getName());
            }
            libraries.addAll(librariesList);
        }

        return libraries;
    }

    /**
     * Return a set of directories to find libraries in.
     * @return A Set containing the directories in the actor's
     * "libraryDirectories" block in its template.
     * @exception IllegalActionException If thrown when getting or reading the
     * CodeStream.
     */
    public Set getLibraryDirectories() throws IllegalActionException {
        Set libraryDirectories = new HashSet();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("libraryDirectories", true);
        String libraryDirectoriesString = codeStream.toString();

        if (libraryDirectoriesString.length() > 0) {
            LinkedList libraryDirectoryList = null;
            try {
                libraryDirectoryList = StringUtilities
                        .readLines(libraryDirectoriesString);
            } catch (IOException e) {
                throw new IllegalActionException(
                        "Unable to read library directories for " + getName());
            }
            libraryDirectories.addAll(libraryDirectoryList);
        }

        return libraryDirectories;
    }

    /**
     * Return a set of parameters that will be modified during the execution of
     * the model. The actor gets those variables if it implements
     * ExplicitChangeContext interface or it contains PortParameters.
     *
     * @return a set of parameters that will be modified.
     * @exception IllegalActionException If an actor throws it while getting
     * modified variables.
     */
    public Set getModifiedVariables() throws IllegalActionException {
        Set set = new HashSet();
        if (_object instanceof ExplicitChangeContext) {
            set
                    .addAll(((ExplicitChangeContext) _object)
                            .getModifiedVariables());
        }

        Iterator inputPorts = ((Actor) _object).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort
                    && inputPort.isOutsideConnected()) {
                set.add(((ParameterPort) inputPort).getParameter());
            }
        }
        return set;
    }

    /**
     * Return the translated new constructor invocation string. Keep the types
     * referenced in the info table of this helper. The kernel will retrieve
     * this information to determine the total number of referenced types in the
     * model.
     * @param constructorString The string within the $new() macro.
     * @return The translated new constructor invocation string.
     * @exception IllegalActionException The given constructor string is not
     * well-formed.
     */
    public String getNewInvocation(String constructorString)
            throws IllegalActionException {
        constructorString = processCode(constructorString);

        // i.e. "$new(Array(8, 8, arg1, arg2, ...))"
        // this transforms to ==>
        // "Array_new(8, arg1, arg2, ...)"
        int openFuncParenIndex = constructorString.indexOf('(');
        int closeFuncParenIndex = constructorString.lastIndexOf(')');

        // Syntax checking.
        if (openFuncParenIndex == -1
                || closeFuncParenIndex != constructorString.length() - 1) {
            throw new IllegalActionException(
                    "Bad Syntax with the $new() macro. "
                            + "[i.e. -- $new([elementType]Array(8, 8, arg1, arg2, ...))]");
        }

        String typeName = constructorString.substring(0, openFuncParenIndex)
                .trim();

        // Record the referenced type function in the infoTable.
        _codeGenerator._newTypesUsed.add(typeName);

        // new() should not be a polymorphic function.
        //_codeGenerator._typeFuncUsed.add("new");

        // Transform this to a function call that needs
        // implementation code.
        return processCode("$" + typeName + "_new"
                + constructorString.substring(openFuncParenIndex));
    }

    /**
     * Get the object associated with this helper.
     * @return The associated object.
     */
    public NamedObj getObject() {
        return (NamedObj) _object;
    }

    /**
     * Return the value or an expression in the target language for the
     * specified parameter of the associated actor. If the parameter is
     * specified by an expression, then the expression will be parsed. If any
     * parameter referenced in that expression is specified by another
     * expression, the parsing continues recursively until either a parameter is
     * directly specified by a constant or a parameter can be directly modified
     * during execution in which case a reference to the parameter is generated.
     *
     * @param name The name of the parameter.
     * @param container The container to search upwards from.
     * @return The value or expression as a string.
     * @exception IllegalActionException If the parameter does not exist or does
     * not have a value.
     */
    public String getParameterValue(String name, NamedObj container)
            throws IllegalActionException {
        if (name.contains("$")) {
            name = processCode(name);
        }

        StringTokenizer tokenizer = new StringTokenizer(name, ",");

        String attributeName = tokenizer.nextToken().trim();
        String offset = null;
        String castType = null;

        if (tokenizer.hasMoreTokens()) {
            offset = tokenizer.nextToken().trim();

            if (tokenizer.hasMoreTokens()) {
                throw new IllegalActionException(getComponent(), name
                        + " does not have the correct format for"
                        + " accessing the parameter value.");
            }
        }

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(attributeName, "()",
                false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(),
                    "Invalid cast type: " + attributeName);
        }

        if (tokenizer2.countTokens() == 2) {
            castType = tokenizer2.nextToken().trim();
            attributeName = tokenizer2.nextToken().trim();
        }

        Attribute attribute = ModelScope.getScopedVariable(null, container,
                attributeName);

        if (attribute == null) {
            attribute = container.getAttribute(attributeName);
            if (attribute == null) {
                throw new IllegalActionException(container,
                        "No attribute named: " + name);
            }
        }

        if (offset == null) {
            if (attribute instanceof Variable) {
                // FIXME: need to ensure that the returned string
                // is correct syntax for the target language.
                Variable variable = (Variable) attribute;

                /*
                 * if (_codeGenerator._modifiedVariables.contains(variable)) {
                 * return generateVariableName(variable); } else if
                 * (variable.isStringMode()) { return "\"" +
                 * variable.getExpression() + "\""; }
                 */

                ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
                if (variable.isStringMode()) {
                    return _generateTypeConvertMethod("\""
                            + parseTreeCodeGenerator
                                    .escapeForTargetLanguage(variable
                                            .getExpression()) + "\"", castType,
                            "String");
                }

                PtParser parser = new PtParser();
                ASTPtRootNode parseTree = null;
                try {
                    parseTree = parser.generateParseTree(variable
                            .getExpression());
                } catch (Throwable throwable) {
                    throw new IllegalActionException(variable, throwable,
                            "Failed to generate parse tree for \"" + name
                                    + "\". in \"" + container + "\"");
                }
                try {
                    parseTreeCodeGenerator.evaluateParseTree(parseTree,
                            new VariableScope(variable));
                } catch (Exception ex) {
                    StringBuffer results = new StringBuffer();
                    Iterator allScopedVariableNames = ModelScope
                            .getAllScopedVariableNames(variable, container)
                            .iterator();
                    while (allScopedVariableNames.hasNext()) {
                        results.append(allScopedVariableNames.next().toString()
                                + "\n");
                    }
                    throw new IllegalActionException(getComponent(), ex,
                            "Failed to find " + variable.getFullName() + "\n"
                                    + results.toString());

                }

                String fireCode = processCode(parseTreeCodeGenerator
                        .generateFireCode());

                //if (castType == null && codeGenType(variable.getType()).equals("Array")) {
                // FIXME: this is a gross hack necessary for Case.
                // The problem is that if the refinement is named "{0}", then
                // we get into trouble because {0} is "false"?  sigh.
                //    return "Array_new(1, 1, " + fireCode + ");";
                //}
                return _generateTypeConvertMethod(fireCode, castType,
                        codeGenType(variable.getType()));

            } else /* if (attribute instanceof Settable) */{
                return ((Settable) attribute).getExpression();
            }

            // FIXME: Are there any other values that a
            // parameter might have?
            //throw new IllegalActionException(getComponent(),
            //        "Attribute does not have a value: " + name);
        } else {
            // FIXME: if offset != null, for now we assume the value of
            // the parameter is fixed during execution.
            if (attribute instanceof Parameter) {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof ArrayToken) {
                    Token element = ((ArrayToken) token).getElement(Integer
                            .valueOf(offset).intValue());

                    /////////////////////////////////////////////////////
                    ParseTreeCodeGenerator parseTreeCodeGenerator = getParseTreeCodeGenerator();
                    PtParser parser = new PtParser();
                    ASTPtRootNode parseTree = null;
                    try {
                        parseTree = parser
                                .generateParseTree(element.toString());
                    } catch (Throwable throwable) {
                        throw new IllegalActionException(attribute, throwable,
                                "Failed to generate parse tree for \"" + name
                                        + "\". in \"" + container + "\"");
                    }
                    parseTreeCodeGenerator.evaluateParseTree(parseTree,
                            new VariableScope((Parameter) attribute));

                    String elementCode = processCode(parseTreeCodeGenerator
                            .generateFireCode());
                    /////////////////////////////////////////////////////

                    return _generateTypeConvertMethod(elementCode, castType,
                            codeGenType(element.getType()));
                }

                throw new IllegalActionException(getComponent(), attributeName
                        + " does not contain an ArrayToken.");
            }

            throw new IllegalActionException(getComponent(), attributeName
                    + " is not a parameter.");
        }
    }

    /**
     * Return the parse tree to use with expressions.
     * @return the parse tree to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        return _parseTreeCodeGenerator;
    }

    /**
     * Get the port that has the given name.
     * @param refName The given name.
     * @return The port that has the given name.
     */
    public TypedIOPort getPort(String refName) {
        Actor actor = (Actor) _object;
        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (generateSimpleName(inputPort).equals(refName)) {
                return inputPort;
            }
        }

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (generateSimpleName(outputPort).equals(refName)) {
                return outputPort;
            }
        }

        return null;
    }

    /**
     * Return the associated actor's rates for all configurations of this actor.
     * In this base class, return null.
     * @return null
     */
    public int[][] getRates() {
        return null;
    }

    /**
     * Get the read offset in the buffer of a given channel from which a token
     * should be read. The channel is given by its containing port and the
     * channel number in that port.
     * @param inputPort The given port.
     * @param channelNumber The given channel number.
     * @return The offset in the buffer of a given channel from which a token
     * should be read.
     * @exception IllegalActionException Thrown if the helper class cannot be
     * found.
     * @see #setReadOffset(IOPort, int, Object)
     */
    public Object getReadOffset(IOPort inputPort, int channelNumber)
            throws IllegalActionException {

        return ((PortCodeGenerator) _getHelper(inputPort))
                .getReadOffset(channelNumber);
    }

    /**
     * Return the reference to the specified parameter or port of the associated
     * actor. For a parameter, the returned string is in the form
     * "fullName_parameterName". For a port, the returned string is in the form
     * "fullName_portName[channelNumber][offset]", if any channel number or
     * offset is given.
     *
     * FIXME: need documentation on the input string format.
     *
     * @param name The name of the parameter or port
     * @return The reference to that parameter or port (a variable name, for
     * example).
     * @exception IllegalActionException If the parameter or port does not exist
     * or does not have a value.
     */
    public String getReference(String name) throws IllegalActionException {
        boolean isWrite = false;
        return getReference(name, isWrite);
    }

    /**
     * Generate the shared code. This is the first generate method invoked out
     * of all, so any initialization of variables of this helper should be done
     * in this method. In this base class, return an empty set. Subclasses may
     * generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        _codeStream.clear();
        _codeStream.appendCodeBlocks(".*shared.*");
        if (!_codeStream.isEmpty()) {
            sharedCode.add(processCode(_codeStream.toString()));
        }
        return sharedCode;
    }

    /**
     * Get the size of a parameter. The size of a parameter is the length of its
     * array if the parameter's type is array, and 1 otherwise.
     * @param name The name of the parameter.
     * @return The expression that represents the size of a parameter or port.
     * @exception IllegalActionException If no port or parameter of the given
     * name is found.
     */
    public String getSize(String name) throws IllegalActionException {

        // Try if the name is a parameter.
        Variable attribute = ModelScope.getScopedVariable(null, getComponent(),
                name);

        if (attribute != null) {
            Token token = attribute.getToken();

            if (token instanceof ArrayToken) {
                return String.valueOf(((ArrayToken) token).length());
            }

            return "1";
        } else {
            TypedIOPort port = getPort(name);
            if (port != null) {
                if (port.isMultiport()) {
                    return String.valueOf(port.getWidth());
                } else {
                    Type type = port.getType();
                    if (type instanceof ArrayType) {
                        if (((ArrayType) type).hasKnownLength()) {
                            return String.valueOf(((ArrayType) type).length());
                        } else {
                            return getReference(name) + ".payload."
                                    + codeGenType(type) + "->size";
                        }
                    }
                }
            }
        }

        throw new IllegalActionException(getComponent(),
                "Attribute not found: " + name);
    }

    /**
     * Return the worst case execution time (WCET) seen by this
     * component.
     * @return The Worst Case Execution Time (WCET), in this base class,
     * the default value of 500.0 is returned.
     * @exception IllegalActionException If there is a problem determining
     * the WCET or a problem accessing the model.  Not thrown in this
     * base class.
     */
    public double getWCET() throws IllegalActionException {
        return 500.0;
    }

    /**
     * Get the write offset in the buffer of a given channel to which a token
     * should be put. The channel is given by its containing port and the
     * channel number in that port.
     * @param port The given port.
     * @param channelNumber The given channel number.
     * @return The offset in the buffer of a given channel to which a token
     * should be put.
     * @exception IllegalActionException Thrown if the helper class cannot be
     * found.
     * @see #setWriteOffset(IOPort, int, Object)
     */
    public Object getWriteOffset(IOPort port, int channelNumber)
            throws IllegalActionException {

        return ((PortCodeGenerator) _getHelper(port))
                .getWriteOffset(channelNumber);
    }

    /**
     * Determine if the given type is primitive.
     * @param cgType The given codegen type.
     * @return true if the given type is primitive, otherwise false.
     */
    public boolean isPrimitive(String cgType) {
        return _codeGenerator.isPrimitive(cgType);
    }

    /**
     * Determine if the given type is primitive.
     * @param ptType The given ptolemy type.
     * @return true if the given type is primitive, otherwise false.
     */
    public boolean isPrimitive(Type ptType) {
        return _codeGenerator.isPrimitive(ptType);
    }

    /**
     * Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    public String processCode(String code) throws IllegalActionException {
        StringBuffer result = new StringBuffer();

        boolean processAgain = false;

        int currentPos = _getMacroStartIndex(code, 0);

        if (currentPos < 0) {
            // No "$" in the string
            return code;
        }

        result.append(code.substring(0, currentPos));

        while (currentPos < code.length()) {
            int openParenIndex = code.indexOf("(", currentPos + 1);
            if (openParenIndex == -1) {
                throw new IllegalActionException(getComponent(),
                        "Failed to find open paren in \"" + code + "\".");
            }
            int closeParenIndex = _findClosedParen(code, openParenIndex);

            if (closeParenIndex < 0) {
                // No matching close parenthesis is found.
                result.append(code.substring(currentPos));
                return result.toString();
            }

            int nextPos = _getMacroStartIndex(code, closeParenIndex + 1);

            if (nextPos < 0) {
                //currentPos is the last "$"
                nextPos = code.length();
            }

            String subcode = code.substring(currentPos, nextPos);

            if (currentPos > 0 && code.charAt(currentPos - 1) == '\\') {
                // found "\$", do not make replacement.
                // FIXME: This is wrong. subcode may contain other macros
                // to be processed.
                // Should be result.append(processCode(subcode.substring(1)));
                result.append(subcode);
                currentPos = nextPos;
                continue;
            }

            String macro = code.substring(currentPos + 1, openParenIndex);
            macro = macro.trim();

            String name = code.substring(openParenIndex + 1, closeParenIndex);

            name = processCode(name.trim());

            try {
                String replaceString = _replaceMacro(macro, name);

                // If the replaceString contains '$' sign,
                // then we have to re-process the whole code string.
                if (_getMacroStartIndex(replaceString, 0) >= 0) {
                    processAgain = true;
                }
                result.append(replaceString);

            } catch (Throwable throwable) {
                throw new IllegalActionException(getComponent(), throwable,
                        "Failed to replace the parameter \"" + name
                                + "\" in the macro \"" + macro
                                + "\".\nInitial code was:\n" + code);
            }
            result.append(code.substring(closeParenIndex + 1, nextPos));

            currentPos = nextPos;
        }

        if (processAgain) {
            return processCode(result.toString());
        }

        return result.toString();
    }

    /**
     * Reset the offsets of all channels of all input ports of the associated
     * actor to the default value of 0.
     *
     * @return The reset code of the associated actor.
     * @exception IllegalActionException If thrown while getting or setting the
     * offset.
     */
    public String resetInputPortsOffset() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator inputPorts = ((Actor) _object).inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            code.append(((PortCodeGenerator) _getHelper(port))
                    .initializeOffsets());
        }
        return code.toString();
    }

    /**
     * Set the buffer size of a given port.
     * @param port The given port.
     * @param channelNumber The given channel.
     * @param bufferSize The buffer size to be set to that port and channel.
     * @see #getBufferSize(IOPort)
     * @exception IllegalActionException
     */
    public void setBufferSize(IOPort port, int channelNumber, int bufferSize)
            throws IllegalActionException {
        ((PortCodeGenerator) _getHelper(port)).setBufferSize(channelNumber,
                bufferSize);
    }

    /**
     * Set the code generator associated with this helper class.
     * @param codeGenerator The code generator associated with this helper
     * class.
     * @see #getCodeGenerator()
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    /**
     * Set the read offset in a buffer of a given channel from which a token
     * should be read.
     * @param port The given port.
     * @param channelNumber The given channel.
     * @param readOffset The offset to be set to the buffer of that channel.
     * @exception IllegalActionException Thrown if the helper class cannot be
     * found.
     * @see #getReadOffset(IOPort, int)
     */
    public void setReadOffset(IOPort port, int channelNumber, Object readOffset)
            throws IllegalActionException {
        ((PortCodeGenerator) _getHelper(port)).setReadOffset(channelNumber,
                readOffset);
    }

    /**
     * Set the write offset in a buffer of a given channel to which a token
     * should be put.
     * @param port The given port.
     * @param channelNumber The given channel.
     * @param writeOffset The offset to be set to the buffer of that channel.
     * @exception IllegalActionException If
     * {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     * @see #getWriteOffset(IOPort, int)
     */
    public void setWriteOffset(IOPort port, int channelNumber,
            Object writeOffset) throws IllegalActionException {
        ((PortCodeGenerator) _getHelper(port)).setWriteOffset(channelNumber,
                writeOffset);
    }

    /**
     * Get the corresponding type in C from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The C data type.
     */
    public/* static */String targetType(Type ptType) {
        return _codeGenerator.targetType(ptType);
    }

    public String toString() {
        return getComponent().toString() + "'s Helper";
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public inner classes                   ////

    /**
     * Return the index of a specific character in the string starting from the
     * given index. It find the first occurence of the character that is not
     * embedded inside parentheses "()".
     * @param ch The character to search for.
     * @param string The given string to search from.
     * @param fromIndex The index to start the search.
     * @return The first occurence of the character in the string that is not
     * embedded in parentheses.
     */
    public static int _indexOf(String ch, String string, int fromIndex) {
        // FIXME: this method name should *not* have a leading underscore.
        int parenIndex = fromIndex;
        int result = -1;
        int closedParenIndex = parenIndex;

        do {
            result = string.indexOf(ch, closedParenIndex);

            parenIndex = string.indexOf('(', closedParenIndex);

            if (parenIndex >= 0) {
                try {
                    closedParenIndex = _findClosedParen(string, parenIndex);
                } catch (IllegalActionException e) {
                    closedParenIndex = -1;
                }
            }
        } while (result > parenIndex && result < closedParenIndex);

        return result;
    }

    /** Parse the list of comma separted parameters.
     *  @param parameters A comma separate list of parameters.
     *  @return A list of parameters.
     */
    public static List<String> parseList(String parameters) {
        List<String> result = new ArrayList<String>();
        int previousCommaIndex = 0;
        int commaIndex = _indexOf(",", parameters, 0);

        while (commaIndex >= 0) {
            String item = parameters.substring(previousCommaIndex, commaIndex)
                    .trim();

            result.add(item);

            previousCommaIndex = commaIndex + 1;
            commaIndex = _indexOf(",", parameters, previousCommaIndex);
        }

        String item = parameters.substring(previousCommaIndex,
                parameters.length()).trim();

        if (item.trim().length() > 0) {
            result.add(item);
        }

        return result;
    }

    /**
     * Copy files to the code directory. The optional
     * <code>fileDependencies</code> codeBlock consists of one or more lines
     * where each line names a file that should be copied to the directory named
     * by the <i>codeDirectory</i> parameter of the code generator. The file is
     * only copied if a file by that name does not exist in <i>codeDirectory</i>
     * or if the source file was more recently modified than the destination
     * file.
     * <p>
     * Using the <code>fileDependencies</code> code block allows actor writers
     * to refer to code defined in other files.
     *
     * @param namedObj If this argument is an instance of
     * ptolemy.actor.lib.jni.EmbeddedCActor, then the code blocks from
     * EmbeddedCActor's <i>embeddedCCode</i> parameter are used.
     * @param codeGenerator The code generator from which the <i>codeDirectory</i>
     * parameter is read.
     * @return The modification time of the most recent file.
     * @exception IOException If there is a problem reading the <i>codeDirectory</i>
     * parameter.
     * @exception IllegalActionException If there is a problem reading the
     * <i>codeDirectory</i> parameter.
     */
    public static long copyFilesToCodeDirectory(NamedObj namedObj,
            CodeGenerator codeGenerator) throws IOException,
            IllegalActionException {

        // This is static so that ptolemy.actor.lib.jni.CompiledCompositeActor
        // will not depend on ptolemy.codegen.

        long lastModified = 0;

        CodeStream codeStream = null;

        codeStream = _getActualCodeStream(namedObj, codeGenerator);

        // Read in the optional fileDependencies code block.
        codeStream.appendCodeBlock("fileDependencies", true);
        String fileDependencies = codeStream.toString();

        if (fileDependencies.length() > 0) {
            LinkedList fileDependenciesList = StringUtilities
                    .readLines(fileDependencies);
            File codeDirectoryFile = codeGenerator._codeDirectoryAsFile();
            String necessaryFileName = null;
            Iterator iterator = fileDependenciesList.iterator();
            while (iterator.hasNext()) {
                necessaryFileName = (String) iterator.next();

                // Look up the file as a resource.  We do this so we can possibly
                // get it from a jar file in the release.
                URL necessaryURL = null;
                try {
                    necessaryURL = FileUtilities.nameToURL(necessaryFileName,
                            null, null);
                } catch (IOException ex) {
                    // If the filename has no slashes, try prepending file:./
                    if (necessaryFileName.indexOf("/") == -1
                            || necessaryFileName.indexOf("\\") == -1) {
                        try {
                            necessaryURL = FileUtilities.nameToURL("file:./"
                                    + necessaryFileName, null, null);
                        } catch (IOException ex2) {
                            // Throw the original exception
                            throw ex;
                        }
                    } else {
                        // Throw the original exception
                        throw ex;
                    }
                }
                // Get the base filename (text after last /)
                String necessaryFileShortName = necessaryURL.getPath();
                if (necessaryURL.getPath().lastIndexOf("/") > -1) {
                    necessaryFileShortName = necessaryFileShortName
                            .substring(necessaryFileShortName.lastIndexOf("/"));
                }

                File necessaryFileDestination = new File(codeDirectoryFile,
                        necessaryFileShortName);
                File necessaryFileSource = new File(necessaryFileName);
                if (!necessaryFileDestination.exists()
                        || necessaryFileSource.exists()
                        && necessaryFileSource.lastModified() > necessaryFileDestination
                                .lastModified()) {
                    // If the dest file does not exist or is older than the
                    // source file, we do the copy
                    System.out.println("Copying " + necessaryFileSource
                            + " to " + necessaryFileDestination);

                    try {
                        FileUtilities.binaryCopyURLToFile(necessaryURL,
                                necessaryFileDestination);
                    } catch (IOException ex) {
                        String directory = "unknown";
                        if (!StringUtilities.getProperty("user.dir").equals("")) {
                            directory = "\""
                                    + StringUtilities.getProperty("user.dir")
                                    + "\"";
                        }
                        throw new IllegalActionException(namedObj, ex,
                                "Failed to copy \"" + necessaryURL + "\" to \""
                                        + necessaryFileDestination
                                        + "\". Current directory is "
                                        + directory);
                    }
                }
                // Reopen the destination file and get its time for
                // comparison
                File necessaryFileDestination2 = new File(codeDirectoryFile,
                        necessaryFileShortName);
                if (necessaryFileDestination2.lastModified() > lastModified) {
                    lastModified = necessaryFileDestination2.lastModified();
                }
            }
        }
        return lastModified;
    }

    /**
     * Generate a string that represents the offset for a dynamically determined
     * channel of a multiport.
     * @param port The referenced port.
     * @param isWrite Whether to generate the write or read offset.
     * @param channelString The string that will determine the channel.
     * @return The expression that represents the offset for a channel
     * determined dynamically in the generated code.
     */
    public static String generateChannelOffset(IOPort port, boolean isWrite,
            String channelString) {
        // By default, return the channel offset for the first channel.
        if (channelString.equals("")) {
            channelString = "0";
        }

        String channelOffset = CodeGeneratorHelper.generateName(port);
        channelOffset += isWrite ? "_writeOffset" : "_readOffset";
        channelOffset += "[" + channelString + "]";

        return channelOffset;
    }

    /**
     * Generate sanitized name for the given named object. Remove all
     * underscores to avoid conflicts with systems functions.
     * @param namedObj The named object for which the name is generated.
     * @return The sanitized name.
     */
    public static String generateName(NamedObj namedObj) {
        String name = StringUtilities.sanitizeName(namedObj.getFullName());

        // FIXME: Assume that all objects share the same top level. In this case,
        // having the top level in the generated name does not help to
        // expand the name space but merely lengthen the name string.
        //        NamedObj parent = namedObj.toplevel();
        //        if (namedObj.toplevel() == namedObj) {
        //            return "_toplevel_";
        //        }
        //        String name = StringUtilities.sanitizeName(namedObj.getName(parent));
        if (name.startsWith("_")) {
            name = name.substring(1, name.length());
        }
        return name.replaceAll("\\$", "Dollar");
    }

    /**
     * Generate sanitized name for the given named object. Remove all
     * underscores to avoid conflicts with systems functions.
     * @param namedObj The named object for which the name is generated.
     * @return The sanitized name.
     */
    public static String generateSimpleName(NamedObj namedObj) {
        String name = StringUtilities.sanitizeName(namedObj.getName());
        return name.replaceAll("\\$", "Dollar");
    }

    /**
     *  Return a reference to the port.
     *  @param port The port.
     *  @param channelAndOffset The given channel and offset.
     *  @param isWrite True if the port is to be written to.
     *  @return The reference to the port.
     *  @see #getReference(TypedIOPort, String[], boolean, boolean)
     */
    public static String generatePortReference(IOPort port,
            String[] channelAndOffset, boolean isWrite) {

        StringBuffer result = new StringBuffer();
        String channelOffset;
        if (channelAndOffset[1].equals("")) {
            channelOffset = CodeGeneratorHelper.generateChannelOffset(port,
                    isWrite, channelAndOffset[0]);
        } else {
            channelOffset = channelAndOffset[1];
        }

        result.append(generateName(port));

        if (port.isMultiport()) {
            result.append("[" + channelAndOffset[0] + "]");
        }
        result.append("[" + channelOffset + "]");

        return result.toString();
    }

    /**
     * Return an array of strings that are regular expressions of all the code
     * blocks that are appended automatically by default. Since the content of
     * the array are regex, users should use matches() instead of equals() to
     * compare their strings.
     * @return Array of string regular expressions of names of code blocks that
     * are appended by default.
     */
    public static String[] getDefaultBlocks() {
        return _defaultBlocks;
    }

    /**
     *  Return a reference to the attribute.
     *  @param attribute The attribute.
     *  @param channelAndOffset The given channel and offset.
     *  @return The reference.
     *  @exception IllegalActionException If the attribute does not
     *   exist or does not have a value.
     */
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
            //result.append("[" + channelAndOffset[1] + "]");
            Type elementType = ((ArrayType) ((Parameter) attribute).getType())
                    .getElementType();

            result.insert(0, "Array_get(");
            if (isPrimitive(elementType)) {
                // Generate type specific Array_get(). e.g. IntArray_get().
                result.insert(0, "/*CGH77*/" + codeGenType(elementType));
            }
            result.insert(0, "/*CGH77*/");

            result.append(" ," + channelAndOffset[1] + ")");
        }
        return result.toString();
    }

    /**
     *  Return a reference.
     *  @param name The reference to be parsed.  The format is
     *  assumed to be "xxx#(yyy)", where "yyy" will be returned
     *  @param isWrite True if the reference is to be written to.
     *  @return The reference.
     *  @exception IllegalActionException If the attribute does not
     *   exist or does not have a value.
     */
    public String getReference(String name, boolean isWrite)
            throws IllegalActionException {
        ptolemy.actor.Director director = getDirector();
        Director directorHelper = (Director) _getHelper(director);

        name = processCode(name);
        String castType = _getCastType(name);
        String refName = _getRefName(name);
        String[] channelAndOffset = _getChannelAndOffset(name);

        // Usually given the name of an input port, getReference(String name)
        // returns variable name representing the input port. Given the name
        // of an output port, getReference(String name) returns variable names
        // representing the input ports connected to the output port.
        // However, if the name of an input port starts with '@',
        // getReference(String name) returns variable names representing the
        // input ports connected to the given input port on the inside.
        // If the name of an output port starts with '@',
        // getReference(String name) returns variable name representing the
        // the given output port which has inside receivers.
        // The special use of '@' is for composite actor when
        // tokens are transferred into or out of the composite actor.
        boolean forComposite = false;
        if (refName.charAt(0) == '@') {
            forComposite = true;
            refName = refName.substring(1);
        }

        TypedIOPort port = getPort(refName);
        if (port != null) {

            if (port instanceof ParameterPort && port.numLinks() <= 0) {

                // Then use the parameter (attribute) instead.
            } else {
                String result = directorHelper.getReference(port,
                        channelAndOffset, forComposite, isWrite, this);

                String refType = codeGenType(port.getType());

                return _generateTypeConvertMethod(result, castType, refType);
            }
        }

        // Try if the name is a parameter.
        Attribute attribute = getComponent().getAttribute(refName);

        if (attribute != null) {
            String refType = _getRefType(attribute);

            String result = directorHelper.getReference(attribute,
                    channelAndOffset, this);

            return _generateTypeConvertMethod(result, castType, refType);
        }

        throw new IllegalActionException(getComponent(),
                "Reference not found: " + name);
    }

    /**
     *  Return a reference to the port.
     *  @param port The port.
     *  @param channelAndOffset The given channel and offset.
     *  @param forComposite True if this check is for a composite
     *  @param isWrite True if the reference is to be written to.
     *  @return the reference.
     *  @exception IllegalActionException If the attribute does not
     *   exist or does not have a value.
     *  @see #generatePortReference(IOPort, String[], boolean)
     */
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite)
            throws IllegalActionException {

        StringBuffer result = new StringBuffer();
        boolean dynamicReferencesAllowed = ((BooleanToken) _codeGenerator.allowDynamicMultiportReference
                .getToken()).booleanValue();

        int channelNumber = 0;
        boolean isChannelNumberInt = true;
        if (!channelAndOffset[0].equals("")) {
            // If dynamic multiport references are allowed, catch errors
            // when the channel specification is not an integer.
            if (dynamicReferencesAllowed) {
                try {
                    channelNumber = Integer.valueOf(channelAndOffset[0])
                            .intValue();
                } catch (Exception ex) {
                    isChannelNumberInt = false;
                }
            } else {
                channelNumber = Integer.valueOf(channelAndOffset[0]).intValue();
            }
        }

        if (!isChannelNumberInt) { // variable channel reference.
            if (port.isOutput()) {
                throw new IllegalActionException(
                        "Variable channel reference not supported"
                                + " for output ports");
            } else {

                return generatePortReference(port, channelAndOffset, isWrite);
            }
        }

        // To support modal model, we need to check the following condition
        // first because an output port of a modal controller should be
        // mainly treated as an output port. However, during choice action,
        // an output port of a modal controller will receive the tokens sent
        // from the same port.  During commit action, an output port of a modal
        // controller will NOT receive the tokens sent from the same port.
        if (checkRemote(forComposite, port)) {
            Receiver[][] remoteReceivers;

            // For the same reason as above, we cannot do: if (port.isInput())...
            if (port.isOutput()) {
                remoteReceivers = port.getRemoteReceivers();
            } else {
                remoteReceivers = port.deepGetReceivers();
            }

            if (remoteReceivers.length == 0) {
                // The channel of this output port doesn't have any sink.
                result.append(generateName(getComponent()));
                result.append("_");
                result.append(generateSimpleName(port));
                return result.toString();
            }

            Channel sourceChannel = new Channel(port, channelNumber);

            List typeConvertSinks = _getTypeConvertSinkChannels(sourceChannel);

            List sinkChannels = getSinkChannels(port, channelNumber);

            boolean hasTypeConvertReference = false;

            for (int i = 0; i < sinkChannels.size(); i++) {
                Channel channel = (Channel) sinkChannels.get(i);
                IOPort sinkPort = channel.port;
                int sinkChannelNumber = channel.channelNumber;

                // Type convert.
                if (typeConvertSinks.contains(channel)
                        && isPrimitive(((TypedIOPort) sourceChannel.port)
                                .getType())) {

                    if (!hasTypeConvertReference) {
                        if (i != 0) {
                            result.append(" = ");
                        }
                        result.append(_getTypeConvertReference(sourceChannel));

                        if (dynamicReferencesAllowed && port.isInput()) {
                            if (channelAndOffset[1].trim().length() > 0) {
                                result.append("[" + channelAndOffset[1].trim()
                                        + "]");
                            } else {
                                result.append("["
                                        + CodeGeneratorHelper
                                                .generateChannelOffset(port,
                                                        isWrite,
                                                        channelAndOffset[0])
                                        + "]");
                            }
                        } else {
                            int rate = Math
                                    .max(
                                            DFUtilities
                                                    .getTokenProductionRate(sourceChannel.port),
                                            DFUtilities
                                                    .getTokenConsumptionRate(sourceChannel.port));
                            if (rate > 1
                                    && channelAndOffset[1].trim().length() > 0) {
                                result.append("[" + channelAndOffset[1].trim()
                                        + "]");
                            }
                        }
                        hasTypeConvertReference = true;
                    } else {
                        // We already generated reference for this sink.
                        continue;
                    }
                } else {
                    if (i != 0) {
                        result.append(" = ");
                    }
                    result.append(generateName(sinkPort));

                    if (sinkPort.isMultiport()) {
                        result.append("[" + sinkChannelNumber + "]");
                    }
                    if (channelAndOffset[1].equals("")) {
                        channelAndOffset[1] = "0";
                    }
                    result.append(generateOffset(channelAndOffset[1], sinkPort,
                            sinkChannelNumber, true));
                }
            }

            return result.toString();
        }

        // Note that if the width is 0, then we have no connection to
        // the port but the port might be a PortParameter, in which
        // case we want the Parameter.
        // codegen/c/actor/lib/string/test/auto/StringCompare3.xml
        // tests this.

        if (checkLocal(forComposite, port)) {

            result.append(generateName(port));

            //if (!channelAndOffset[0].equals("")) {
            if (port.isMultiport()) {
                // Channel number specified. This must be a multiport.
                result.append("[" + channelAndOffset[0] + "]");
            }

            result.append(generateOffset(channelAndOffset[1], port,
                    channelNumber, isWrite));

            return result.toString();
        }

        // FIXME: when does this happen?
        return "";
    }

    /**
     * Return a list of channel objects that are the sink input ports given a
     * port and channel. Note the returned channels are newly created objects
     * and therefore not associated with the helper class.
     * @param port The given output port.
     * @param channelNumber The given channel number.
     * @return The list of channel objects that are the sink channels of the
     * given output channel.
     * @exception IllegalActionException
     */
    public static List<Channel> getSinkChannels(IOPort port, int channelNumber)
            throws IllegalActionException {
        List sinkChannels = new LinkedList();
        Receiver[][] remoteReceivers;

        // due to reason stated in getReference(String),
        // we cannot do: if (port.isInput())...
        if (port.isOutput()) {
            remoteReceivers = port.getRemoteReceivers();
        } else {
            remoteReceivers = port.deepGetReceivers();
        }

        if (remoteReceivers.length <= channelNumber || channelNumber < 0) {
            // This is an escape method. This class will not call this
            // method if the output port does not have a remote receiver.
            return sinkChannels;
        }

        if (remoteReceivers[channelNumber] == null) {
            /*
             * // FIXME: Is this an important warning? The reference to //
             * printedNullPortWarnings prevents us from making this // a static
             * method. if (!printedNullPortWarnings) { printedNullPortWarnings =
             * true; System.out.println("Warning: Channel " + channelNumber + "
             * of Port \"" + port + "\" was null! Total number of channels: " +
             * remoteReceivers.length); }
             */
            return sinkChannels;
        }

        for (int i = 0; i < remoteReceivers[channelNumber].length; i++) {
            IOPort sinkPort = remoteReceivers[channelNumber][i].getContainer();
            Receiver[][] portReceivers;

            if (sinkPort.isInput()) {
                portReceivers = sinkPort.getReceivers();
            } else {
                portReceivers = sinkPort.getInsideReceivers();
            }

            for (int j = 0; j < portReceivers.length; j++) {
                for (int k = 0; k < portReceivers[j].length; k++) {
                    if (remoteReceivers[channelNumber][i] == portReceivers[j][k]) {
                        Channel sinkChannel = new Channel(sinkPort, j);
                        sinkChannels.add(sinkChannel);
                        break;
                    }
                }
            }
        }

        return sinkChannels;
    }

    //    private List<String> processListOfCode(List<String> list) throws IllegalActionException {
    //        ArrayList<String> newList = new ArrayList<String>();
    //        for (String code : list) {
    //            newList.add(processCode(code));
    //        }
    //        return newList;
    //    }

    /**
     * Given a port and channel number, create a Channel that sends data to the
     * specified port and channel number.
     * @param port The port.
     * @param channelNumber The channel number of the port.
     * @return the source channel.
     * @exception IllegalActionException If there is a problem getting
     * information about the receivers or constructing the new Channel.
     */
    public static Channel getSourceChannel(IOPort port, int channelNumber)
            throws IllegalActionException {
        Receiver[][] receivers = null;

        if (port.isInput()) {
            receivers = port.getReceivers();
        } else if (port.isOutput()) {
            if (port.getContainer() instanceof CompositeActor) {
                receivers = port.getInsideReceivers();
            } else {
                // This port is the source port, so we only
                // need to make a new Channel. We assume that
                // the given channelNumber is valid.
                return new Channel(port, channelNumber);
            }
        } else {
            assert false;
        }

        List sourcePorts = port.sourcePortList();
        sourcePorts.addAll(port.insideSourcePortList());

        for (TypedIOPort sourcePort : (List<TypedIOPort>) sourcePorts) {
            try {
                Channel source = new Channel(sourcePort, sourcePort
                        .getChannelForReceiver(receivers[channelNumber][0]));

                if (source != null) {
                    return source;
                }
            } catch (IllegalActionException ex) {

            }
        }
        return null;
    }

    /**
     * A class that defines a channel object. A channel object is specified by
     * its port and its channel index in that port.
     */
    public static class Channel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /**
         * Construct the channel with the given port and channel number.
         * @param portObject The given port.
         * @param channel The channel number of this object in the given port.
         */
        public Channel(IOPort portObject, int channel) {
            port = portObject;
            channelNumber = channel;
        }

        /**
         * Whether this channel is the same as the given object.
         * @param object The given object.
         * @return True if this channel is the same reference as the given
         * object, otherwise false;
         */
        public boolean equals(Object object) {
            return object instanceof Channel
                    && port.equals(((Channel) object).port)
                    && channelNumber == ((Channel) object).channelNumber;
        }

        /**
         * Return the hash code for this channel. Implementing this method is
         * required for comparing the equality of channels.
         * @return Hash code for this channel.
         */
        public int hashCode() {
            return port.hashCode() + channelNumber;
        }

        /**
         * Return the string representation of this channel.
         * @return The string representation of this channel.
         */
        public String toString() {
            return generateSimpleName(port) + "_" + channelNumber;
        }

        /**
         * The port that contains this channel.
         */
        public IOPort port;

        /**
         * The channel number of this channel.
         */
        public int channelNumber;
    }

    /**
     * This class implements a scope, which is used to generate the parsed
     * expressions in target language.
     */
    protected class VariableScope extends ModelScope {
        /**
         * Construct a scope consisting of the variables of the containing actor
         * and its containers and their scope-extending attributes.
         */
        public VariableScope() {
            _variable = null;
        }

        /**
         * Construct a scope consisting of the variables of the container of the
         * given instance of Variable and its containers and their
         * scope-extending attributes.
         * @param variable The variable whose expression is under code
         * generation using this scope.
         */
        public VariableScope(Variable variable) {
            _variable = variable;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         * Look up and return the macro or expression in the target language
         * corresponding to the specified name in the scope.
         * @param name The given name string.
         * @return The macro or expression with the specified name in the scope.
         * @exception IllegalActionException If thrown while getting buffer
         * sizes or creating ObjectToken.
         */
        public Token get(String name) throws IllegalActionException {

            NamedObj container = getComponent();
            if (_variable != null) {
                container = _variable.getContainer();
            }

            Variable result = getScopedVariable(_variable, container, name);

            if (result != null) {
                // If the variable found is a modified variable, which means
                // its value can be directly changed during execution
                // (e.g., in commit action of a modal controller), then this
                // variable is declared in the target language and should be
                // referenced by the name anywhere it is used.
                if (_codeGenerator._modifiedVariables.contains(result)) {
                    return new ObjectToken(_codeGenerator
                            .generateVariableName(result));
                } else {
                    // This will lead to recursive call until a variable found
                    // is either directly specified by a constant or it is a
                    // modified variable.
                    PtParser parser = new PtParser();
                    String parameterValue = getParameterValue(name, result
                            .getContainer());
                    try {
                        ASTPtRootNode parseTree = parser
                                .generateParseTree(parameterValue);

                        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();

                        return evaluator.evaluateParseTree(parseTree, this);
                    } catch (IllegalActionException ex) {
                        // Could not evaluate the expression. This means that
                        // the parameter value contains a variable expression.
                        // So, we'll won't try to evaluate it.
                        return new ObjectToken(parameterValue);
                    }
                }
            } else {
                return null;
            }
        }

        /**
         * Look up and return the type of the attribute with the specified name
         * in the scope. Return null if such an attribute does not exist.
         * @param name The name of the attribute to look up.
         * @return The attribute with the specified name in the scope.
         * @exception IllegalActionException If a value in the scope exists with
         * the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().getType(name);
            }
            return null;
        }

        /**
         * Look up and return the type term for the specified name in the scope.
         * Return null if the name is not defined in this scope, or is a
         * constant type.
         * @param name The name of the type term to look up.
         * @return The InequalityTerm associated with the given name in the
         * scope.
         * @exception IllegalActionException If a value in the scope exists with
         * the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().getTypeTerm(name);
            }
            return null;
        }

        /**
         * Return the list of identifiers within the scope.
         * @return The list of variable names within the scope.
         * @exception IllegalActionException If there is a problem getting the
         * identifier set from the variable.
         */
        public Set identifierSet() throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().identifierSet();
            }
            return null;
        }

        public String toString() {
            return super.toString()
                    + " variable: "
                    + _variable
                    + " variable.parserScope: "
                    + (_variable == null ? "N/A, _variable is null" : _variable
                            .getParserScope());
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /**
         * If _variable is not null, then the helper scope created is for
         * parsing the expression specified for this variable and generating the
         * corresponding code in target language.
         */
        private Variable _variable = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /**
     * Generate the fire code. This method is intended to be overwritten by
     * sub-classes to generate actor-specific code.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected String _generateFireCode() throws IllegalActionException {
        _codeStream.clear();

        // If the component name starts with a $, then convert "$" to "Dollar" and avoid problems
        // with macro substitution.  See codegen/c/actor/lib/test/auto/RampDollarNames.xml.
        _codeStream.appendCodeBlock(_defaultBlocks[2], true); // fireBlock
        return _codeStream.toString();
    }

    /**
     * Create the buffer size and offset maps for each input port, which is
     * associated with this helper object. A key of the map is an IOPort of the
     * actor. The corresponding value is an array of channel objects. The i-th
     * channel object corresponds to the i-th channel of that IOPort. This
     * method is used to maintain a internal HashMap of channels of the actor.
     * The channel objects in the map are used to keep track of the buffer sizes
     * or offsets in their buffer.
     * @exception IllegalActionException If the director helper or executive
     * director is not found, or if {@link #setReadOffset(IOPort, int, Object)}
     * method throws it, or if {@link #setWriteOffset(IOPort, int, Object)}
     * method throws it.
     *
     */
    protected void _createBufferSizeAndOffsetMap()
            throws IllegalActionException {

        _createInputBufferSizeAndOffsetMap();

    }

    /**
     * Create the input buffer and offset map.
     * @exception IllegalActionException If thrown while getting port
     * information.
     */
    protected void _createInputBufferSizeAndOffsetMap()
            throws IllegalActionException {
        //We only care about input ports where data are actually stored
        //except when an output port is not connected to any input port.
        //In that case the variable corresponding to the unconnected output
        //port always has size 1 and the assignment to this variable is
        //performed just for the side effect.
        Iterator inputPorts = ((Actor) _object).inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            int length = port.getWidth();

            ptolemy.actor.Director director = getDirector();
            Director directorHelper = (Director) _getHelper(director);

            for (int i = 0; i < port.getWidth(); i++) {
                int bufferSize = directorHelper.getBufferSize(port, i);
                setBufferSize(port, i, bufferSize);
            }

            for (int i = 0; i < length; i++) {
                setReadOffset(port, i, Integer.valueOf(0));
                setWriteOffset(port, i, Integer.valueOf(0));
            }
        }
    }

    /**
     * Given a block name, generate code for that block. This method is called
     * by actors helpers that have simple blocks that do not take parameters or
     * have widths.
     * @param blockName The name of the block.
     * @return The code for the given block.
     * @exception IllegalActionException If illegal macro names are found, or if
     * there is a problem parsing the code block from the helper .c file.
     */
    protected String _generateBlockCode(String blockName)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        return _generateBlockCode(blockName, new ArrayList());
    }

    /**
     * Given a block name, generate code for that block. This method is called
     * by actors helpers that have simple blocks that do not take parameters or
     * have widths.
     * @param blockName The name of the block.
     * @param args The arguments for the block.
     * @return The code for the given block.
     * @exception IllegalActionException If illegal macro names are found, or if
     * there is a problem parsing the code block from the helper .c file.
     */
    protected String _generateBlockCode(String blockName, List args)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName, args);
        return processCode(_codeStream.toString());
    }

    /**
     * Generate expression that evaluates to a result of equivalent value with
     * the cast type.
     * @param expression The given variable expression.
     * @param castType The given cast type.
     * @param refType The given type of the variable.
     * @return The variable expression that evaluates to a result of equivalent
     * value with the cast type.
     * @exception IllegalActionException
     */
    protected String _generateTypeConvertMethod(String expression,
            String castType, String refType) throws IllegalActionException {

        if (castType == null || refType == null || castType.equals(refType)) {
            if (castType == null
                    && (refType != null && refType.equals("Token"))
                    && (expression.equals("true") || expression.equals("false"))) {
                // FIXME: is this right?  It is needed by the Case actor in
                // $PTII/bin/ptcg $PTII/ptolemy/codegen/c/actor/lib/hoc/test/auto/Case5.xml
                return ("Boolean_new(" + expression + ")");
            } else {
                return expression;
            }
        }

        if (castType.length() == 0) {
            throw new IllegalActionException("_generateTypeConvertMethod(\""
                    + expression + "\", \"" + castType + "\", \"" + refType
                    + "\") called with castType (the 2nd arg) "
                    + "having length 0.");
        }

        if (refType.length() == 0) {
            throw new IllegalActionException("_generateTypeConvertMethod(\""
                    + expression + "\", \"" + castType + "\", \"" + refType
                    + "\") called with refType (the 3rd arg) "
                    + "having length 0.");
        }

        expression = "$convert_" + refType + "_" + castType + "(" + expression
                + ")";

        return processCode(expression);
    }

    /**
     * Generate the type conversion statement for the particular offset of the
     * two given channels. This assumes that the offset is the same for both
     * channel. Advancing the offset of one has to advance the offset of the
     * other.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @param offset The given offset.
     * @return The type convert statement for assigning the converted source
     * variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * helpers for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatement(Channel source,
            Channel sink, int offset) throws IllegalActionException {

        Type sourceType = ((TypedIOPort) source.port).getType();
        Type sinkType = ((TypedIOPort) sink.port).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }

        // The references are associated with their own helper, so we need
        // to find the associated helper.
        String sourcePortChannel = generateSimpleName(source.port) + "#"
                + source.channelNumber + ", " + offset;
        String sourceRef = ((CodeGeneratorHelper) _getHelper(source.port
                .getContainer())).getReference(sourcePortChannel);

        String sinkPortChannel = generateSimpleName(sink.port) + "#"
                + sink.channelNumber + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = ((CodeGeneratorHelper) _getHelper(sink.port
                .getContainer())).getReference(sinkPortChannel, true);

        // When the sink port is contained by a modal controller, it is
        // possible that the port is both input and output port. we need
        // to pay special attention. Directly calling getReference() will
        // treat it as output port and this is not correct.
        // FIXME: what about offset?
        if (sink.port.getContainer() instanceof ModalController) {
            sinkRef = generateName(sink.port);
            if (sink.port.isMultiport()) {
                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
            }
        }

        String result = sourceRef;

        if (!sinkType.equals(sourceType)) {
            if (isPrimitive(sinkType)) {
                result = codeGenType(sourceType) + "to" + codeGenType(sinkType)
                        + "(" + result + ")";

            } else if (isPrimitive(sourceType)) {
                result = "$new(" + codeGenType(sourceType) + "(" + result
                        + "))";
            }

            if (sinkType != BaseType.SCALAR && sinkType != BaseType.GENERAL
                    && !isPrimitive(sinkType)) {
                if (sinkType instanceof ArrayType) {
                    if (isPrimitive(sourceType)) {
                        result = "$new(" + codeGenType(sinkType) + "(1, 1, "
                                + result + ", TYPE_" + codeGenType(sourceType)
                                + "))";
                    }

                    // Deep converting for ArrayType.
                    Type elementType = ((ArrayType) sinkType).getElementType();
                    while (elementType instanceof ArrayType) {
                        elementType = ((ArrayType) elementType)
                                .getElementType();
                    }

                    if (elementType != BaseType.SCALAR
                            && elementType != BaseType.GENERAL) {
                        result = "$typeFunc(TYPE_"
                                + codeGenType(sinkType)
                                + "::convert("
                                + result
                                + ", /*CGH*/ TYPE_"
                                + codeGenType(((ArrayType) sinkType)
                                        .getElementType()) + "))";
                    }

                } else {
                    result = "$typeFunc(TYPE_" + codeGenType(sinkType)
                            + "::convert(" + result + "))";
                }
            }
        }
        return sinkRef + " = " + result + ";" + _eol;
    }

    /**
     * Generate the type conversion statements for the two given channels.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @return The type convert statement for assigning the converted source
     * variable to the sink variable.
     * @exception IllegalActionException If there is a problem getting the
     * helpers for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatements(Channel source, Channel sink)
            throws IllegalActionException {

        StringBuffer statements = new StringBuffer();

        int rate = Math.max(DFUtilities.getTokenProductionRate(source.port),
                DFUtilities.getTokenConsumptionRate(source.port));

        for (int offset = 0; offset < rate || offset == 0 && rate == 0; offset++) {
            statements.append(_generateTypeConvertStatement(source, sink,
                    offset));
        }
        return processCode(statements.toString());
    }

    /**
     * Get the cast type of a name.
     * @param name The given name.  The name must have a "#" in it.
     * @return The string after the "#" and before the () is returned
     * @exception IllegalActionException If the cast type is invalid.
     */
    protected String _getCastType(String name) throws IllegalActionException {
        // FIXME: This code is not called by any of the tests.
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(),
                    "Invalid cast type: " + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            String type = tokenizer2.nextToken().trim();
            return type.length() > 0 ? type : null;
        }
        return null;
    }

    /**
     * Return the channel and offset given in a string. The result is an string
     * array of length 2. The first element indicates the channel index, and the
     * second the offset. If either element is an empty string, it means that
     * channel/offset is not specified.
     * @param name The given string.
     * @return An string array of length 2, containing expressions of the
     * channel index and offset.
     * @exception IllegalActionException If the channel index or offset
     * specified in the given string is illegal.
     */
    protected String[] _getChannelAndOffset(String name)
            throws IllegalActionException {

        String[] result = { "", "" };

        // Given expression of forms:
        //     "port"
        //     "port, offset", or
        //     "port#channel, offset".

        int poundIndex = _indexOf("#", name, 0);
        int commaIndex = _indexOf(",", name, 0);

        if (commaIndex < 0) {
            commaIndex = name.length();
        }
        if (poundIndex < 0) {
            poundIndex = commaIndex;
        }

        if (poundIndex < commaIndex) {
            result[0] = name.substring(poundIndex + 1, commaIndex);
        }

        if (commaIndex < name.length()) {
            result[1] = name.substring(commaIndex + 1);
        }
        return result;
    }

    /**
     * Return the prototype for fire functions.
     * @return In this base class, return "()". Derived classes, such as the C
     * code generator helper might return "(void)".
     */
    protected String _getFireFunctionArguments() {
        return "()";
    }

    /**
     * Get the code generator helper associated with the given component.
     * @param component The given component.
     * @return The code generator helper.
     * @exception IllegalActionException If the helper class cannot be found.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        return _codeGenerator._getHelper(component);
    }

    /**
     * Get the code generator helper associated with the given object.
     * @param object The given object.
     * @return The code generator helper.
     * @exception IllegalActionException If the helper class cannot be found.
     */
    protected Object _getHelper(Object object) throws IllegalActionException {
        return _codeGenerator._getHelper(object);
    }

    /**
     * Return the reference to the specified parameter or port of the associated
     * actor. For a parameter, the returned string is in the form
     * "fullName_parameterName". For a port, the returned string is in the form
     * "fullName_portName[channelNumber][offset]", if any channel number or
     * offset is given.
     * @param name The name of the parameter or port
     * @param isWrite Whether to generate the write or read offset.
     * @return The reference to that parameter or port (a variable name, for
     * example).
     * @exception IllegalActionException If the parameter or port does not exist
     * or does not have a value.
     */
    protected String _getReference(String name, boolean isWrite)
            throws IllegalActionException {
        return "";
    }

    /**
     * Return the list of corresponding reference channel. This is because a
     * channel may map to multiple reference channels in the generated code. At
     * the same time, multiple channels may map to the same reference channel
     * (e.g. the input-output port pair of a connection).
     * @param port The specified port.
     * @param channelNumber The specified channel number.
     * @param isWrite Whether this is a write or read access.
     * @return The list of reference channel.
     * @exception IllegalActionException
     */
    protected List<Channel> _getReferenceChannels(TypedIOPort port,
            int channelNumber, boolean isWrite) throws IllegalActionException {
        if (isWrite) {
            return getSinkChannels(port, channelNumber);

        } else {
            ArrayList<Channel> channels = new ArrayList<Channel>();
            channels.add(new Channel(port, channelNumber));
            return channels;
        }
    }

    /**
     * Get the set of channels that need to be type converted.
     * @return Set of channels that need to be type converted.
     */
    protected Set _getTypeConvertChannels() {
        return _portConversions.keySet();
    }

    /**
     * Generate a variable reference for the given channel. This varaible
     * reference is needed for type conversion. The source helper get this
     * reference instead of using the sink reference directly. This method
     * assumes the given channel is a source (output) channel.
     * @param channel The given source channel.
     * @return The variable reference for the given channel.
     */
    protected String _getTypeConvertReference(Channel channel) {
        return generateName(channel.port) + "_" + channel.channelNumber;
    }

    /**
     * Return the replacement string of the given macro. Subclass of
     * CodeGenerator may overriding this method to extend or support a different
     * set of macros.
     * @param macro The given macro.
     * @param parameter The given parameter to the macro.
     * @return The replacement string of the given macro.
     * @exception IllegalActionException Thrown if the given macro or parameter
     * is not valid.
     */
    protected String _replaceMacro(String macro, String parameter)
            throws IllegalActionException {

        // $$def(abc)
        // ==> abc$def(abc)
        int indexOfDollarSign = macro.indexOf('$');
        if (indexOfDollarSign >= 0) {
            String result = "$" + macro.substring(0, indexOfDollarSign);
            String innerMacro = macro.substring(indexOfDollarSign + 1, macro
                    .length());
            result += _replaceMacro(innerMacro, parameter);
            return result;
        }
        if (macro.equals("get")) {
            return _replaceGetMacro(parameter);

        } else if (macro.equals("send")) {
            return _replaceSendMacro(parameter);

        } else if (macro.equals("ref")) {
            return getReference(parameter);

        } else if (macro.equals("targetType")) {
            Typeable typeable = _getTypeable(parameter);
            if (typeable != null) {
                return targetType(typeable.getType());
            }

            throw new IllegalActionException(parameter
                    + " is not a typeable object. The $targetType() "
                    + "macro takes in a Typeable object.");

        } else if (macro.equals("elementType")
                || macro.equals("elementTargetType")) {
            Typeable typeable = _getTypeable(parameter);
            if (typeable != null && typeable.getType() instanceof ArrayType) {

                if (macro.equals("elementType")) {
                    return codeGenType(((ArrayType) typeable.getType())
                            .getElementType());
                } else {
                    return targetType(((ArrayType) typeable.getType())
                            .getElementType());
                }
            }
            throw new IllegalActionException(parameter
                    + " is not of ArrayType. The $elementType() "
                    + "macro takes in a ArrayType object.");

        } else if (macro.equals("type") || macro.equals("cgType")) {

            String type = "";
            if (macro.equals("type")) {
                type = "TYPE_";
            }
            Typeable typeable = _getTypeable(parameter);
            if (typeable != null) {
                return type + codeGenType(typeable.getType());
            }
            throw new IllegalActionException(parameter
                    + " is not a port. $type macro takes in a port.");

        } else if (macro.equals("val")) {
            return getParameterValue(parameter, getComponent());

        } else if (macro.equals("size")) {
            return "" + getSize(parameter);

        } else if (macro.equals("actorSymbol")) {
            if (parameter.trim().length() == 0) {
                return generateVariableName(getComponent());
            } else {
                return generateVariableName(getComponent()) + "_"
                        + processCode(parameter);
            }
        } else if (macro.equals("actorClass")) {
            return getComponent().getClassName().replace('.', '_') + "_"
                    + processCode(parameter);

            // Handle type function macros.
        } else if (macro.equals("new")) {
            return getNewInvocation(parameter);

        } else if (macro.equals("tokenFunc")) {
            return getFunctionInvocation(parameter, false);

        } else if (macro.equals("typeFunc")) {
            return getFunctionInvocation(parameter, true);

        } else {
            // Try calling a method defined in the helper first.
            try {
                Method method = getClass().getMethod(macro, new Class[0]);
                return (String) method.invoke(this, new Object[0]);
            } catch (Exception ex) {
                // Don't print out error, since this is probably not an user macro.
            }

            // Try to treat this as an user macro class.
            Method handler = null;
            Method checker = null;
            Class userMacro = null;
            try {
                userMacro = Class.forName("ptolemy.codegen.kernel.userMacro."
                        + macro);

                handler = userMacro.getMethod("handleMacro",
                        new Class[] { List.class });
                checker = userMacro.getMethod("checkArguments",
                        new Class[] { List.class });
            } catch (Exception ex) {
                // Don't print out error, since this is probably not an user macro.
                return null;
            }

            try {
                checker
                        .invoke(userMacro,
                                new Object[] { parseList(parameter) });
                return (String) handler.invoke(userMacro,
                        new Object[] { parseList(parameter) });
            } catch (Exception ex) {
                throw new IllegalActionException(getComponent(), ex,
                        "Failed to invoke user macro ($" + macro + ").");
            }
        }
    }

    /**
     * Find the paired close parenthesis given a string and an index which is
     * the position of an open parenthesis. Return -1 if no paired close
     * parenthesis is found.
     * @param string The given string.
     * @param pos The given index.
     * @return The index which indicates the position of the paired close
     * parenthesis of the string.
     * @exception IllegalActionException If the character at the given position
     * of the string is not an open parenthesis or if the index is less than 0
     * or past the end of the string.
     */
    protected static int _findClosedParen(String string, int pos)
            throws IllegalActionException {
        if (pos < 0 || pos >= string.length()) {
            throw new IllegalActionException("The character index " + pos
                    + " is past the end of string \"" + string
                    + "\", which has a length of " + string.length() + ".");
        }

        if (string.charAt(pos) != '(') {
            throw new IllegalActionException("The character at index " + pos
                    + " of string: " + string + " is not a open parenthesis.");
        }

        int nextOpenParen = string.indexOf("(", pos + 1);

        if (nextOpenParen < 0) {
            nextOpenParen = string.length();
        }

        int nextCloseParen = string.indexOf(")", pos);

        if (nextCloseParen < 0) {
            return -1;
        }

        int count = 1;
        int beginIndex = pos + 1;

        while (beginIndex > 0) {
            if (nextCloseParen < nextOpenParen) {
                count--;

                if (count == 0) {
                    return nextCloseParen;
                }

                beginIndex = nextCloseParen + 1;
                nextCloseParen = string.indexOf(")", beginIndex);

                if (nextCloseParen < 0) {
                    return -1;
                }
            }

            if (nextOpenParen < nextCloseParen) {
                count++;
                beginIndex = nextOpenParen + 1;
                nextOpenParen = string.indexOf("(", beginIndex);

                if (nextOpenParen < 0) {
                    nextOpenParen = string.length();
                }
            }
        }

        return -1;
    }

    /**
     * Return a number of spaces that is proportional to the argument. If the
     * argument is negative or zero, return an empty string.
     * @param level The level of indenting represented by the spaces.
     * @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }

    /**
     * The code generator that contains this helper class.
     */
    protected CodeGenerator _codeGenerator;

    /**
     * The code stream associated with this helper.
     */
    protected CodeStream _codeStream = new CodeStream(this);

    /**
     * End of line character. Under Unix: "\n", under Windows: "\n\r". We use a
     * end of line charactor so that the files we generate have the proper end
     * of line character for use by other native tools.
     */
    protected final static String _eol;

    /** The parse tree to use with expressions. */
    protected ParseTreeCodeGenerator _parseTreeCodeGenerator;

    /**
     * A HashMap that contains mapping for ports and their conversion method.
     * Ports that does not need to be converted do NOT have record in this map.
     * The codegen kernel record this mapping during the first pass over the
     * model. This map is used later in the code generation phase.
     */
    protected Hashtable _portConversions = new Hashtable();

    /**
     * A hashset that keeps track of parameters that are referenced for the
     * associated actor.
     */
    protected HashSet _referencedParameters = new HashSet();

    /**
     * Indent string for indent level 1.
     * @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected final static String _INDENT1 = StringUtilities.getIndentPrefix(1);

    /**
     * Indent string for indent level 2.
     * @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected final static String _INDENT2 = StringUtilities.getIndentPrefix(2);

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated object. */
    private final Object _object;

    /**
     * The code block table that stores the code block body (StringBuffer) with
     * the code block name (String) as key.
     */
    private static final String[] _defaultBlocks = { "preinitBlock",
            "initBlock", "fireBlock", "postfireBlock", "wrapupBlock" };

    //private boolean printedNullPortWarnings = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Generate code for a given block. The comment includes the portion of the
     * blockName parameter up until the string "Block".
     * @param blockName The name of the block, which usually ends with the
     * string "Block".
     * @return The generated wrapup code.
     * @exception IllegalActionException If thrown while appending to the the
     * block or processing the macros.
     */
    private String _generateBlockByName(String blockName)
            throws IllegalActionException {
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName, true);
        // There is no need to generate comment for empty code block.
        if (!_codeStream.isEmpty()) {
            // Don't die if the blockName ends not in "Block".
            String shortBlockName = null;
            int index = blockName.lastIndexOf("Block");
            if (index != -1) {
                shortBlockName = blockName.substring(0, index);
            } else {
                shortBlockName = blockName;
            }
            _codeStream.insert(0, _eol
                    + _codeGenerator.comment(shortBlockName
                            + generateSimpleName(getComponent())));
        }
        return processCode(_codeStream.toString());

    }

    /**
     * Generate the invocation of the fire function of the given component.
     * @param component The given component.
     * @return The generated code.
     */
    private static String _generateFireInvocation(NamedObj component) {
        return generateName(component) + "()";
    }

    /**
     * Return the actual CodeStream for this Helper.
     * @return The actual CodeStream.
     * @exception IllegalActionException If thrown by a called method.
     */
    private CodeStream _getActualCodeStream() throws IllegalActionException {
        return _getActualCodeStream(getComponent(), _codeGenerator);
    }

    /**
     * Return the position of the first occurence of the "&" sign in the given
     * code string, starting from the given from position. If the "&" sign found
     * is escaped by "\\", it will be ignored.
     * @param code The given code string.
     * @param from The given position to start searching from.
     * @return The next position of the "&" sign.
     */
    private int _getMacroStartIndex(String code, int from) {
        int position = from - 1;

        do {
            position = code.indexOf("$", position + 1);
        } while (position > 0 && code.charAt(position - 1) == '\\');

        return position;
    }

    private String _getRefName(String name) throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if (tokenizer.countTokens() != 1 && tokenizer.countTokens() != 3
                && tokenizer.countTokens() != 5) {
            throw new IllegalActionException(getComponent(),
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(),
                    "Invalid cast type: " + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            // castType
            tokenizer2.nextToken();
        }

        return tokenizer2.nextToken().trim();
    }

    private String _getRefType(Attribute attribute) {
        if (attribute instanceof Parameter) {
            return codeGenType(((Parameter) attribute).getType());
        }
        return null;
    }

    /**
     * If the object name is a Port or Variable, return its Port
     * or Variable, otherwise return null.
     * @param objectName  The object name
     * @return Either the Port, Variable or null.
     */
    private Typeable _getTypeable(String objectName) {
        TypedIOPort port = getPort(objectName);
        if (port != null) {
            return port;
        }

        Variable variable = _getVariable(objectName);
        if (variable != null) {
            return variable;
        }
        return null;
    }

    /**
     * Get the list of sink channels that the given source channel needs to be
     * type converted to.
     * @param source The given source channel.
     * @return List of sink channels that the given source channel needs to be
     * type converted to.
     */
    private List _getTypeConvertSinkChannels(Channel source) {
        if (_portConversions.containsKey(source)) {
            return (List) _portConversions.get(source);
        }
        return new ArrayList();
    }

    /***************************************************************************
     * Return a variable that matches the given label. Null is returned, if no
     * such variable cannot found.
     * @param refName The given label.
     * @return A variable that matches the given label, or null if no such
     * variable is found.
     */
    private Variable _getVariable(String refName) {
        NamedObj actor = (NamedObj) _object;

        for (Object attribute : actor.attributeList()) {

            if (attribute instanceof Variable) {

                if (generateSimpleName(((Variable) attribute)).equals(refName)) {
                    return (Variable) attribute;
                }
            }
        }
        return null;
    }

    /**
     * Mark the given connection between the source and the sink channels as
     * type conversion required.
     * @param source The given source channel.
     * @param sink The given input channel.
     */
    private void _markTypeConvert(Channel source, Channel sink) {
        List sinks;
        if (_portConversions.containsKey(source)) {
            sinks = (List) _portConversions.get(source);
        } else {
            sinks = new ArrayList();
            _portConversions.put(source, sinks);
        }
        sinks.add(sink);
    }

    private String _replaceGetMacro(String parameter)
            throws IllegalActionException {
        // e.g. $get(0, input);
        List<String> parameters = parseList(parameter);

        TypedIOPort port = null;
        String channel = "";
        if (parameters.size() == 2) {
            port = getPort(parameters.get(0));
            channel = parameters.get(1);
        }

        if (port == null || channel.length() == 0) {
            throw new IllegalActionException(parameter
                    + " is not acceptable by $get(). "
                    + "The $get macro takes in as arguments "
                    + "a channelNumber, and a port (e.g. $get(0, output).");
        }

        PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);

        return portHelper.generateCodeForGet(channel);
    }

    private String _replaceSendMacro(String parameter)
            throws IllegalActionException {
        // e.g. $send(input, 0, token);
        List<String> parameters = parseList(parameter);

        TypedIOPort port = null;
        String channel = "";
        String dataToken = "";

        port = getPort(parameters.get(0));
        channel = parameters.get(1);

        if (port == null || channel.length() == 0) {
            throw new IllegalActionException(
                    parameter
                            + " is not acceptable by $send(). "
                            + "The $send macro takes in as arguments "
                            + "a channelNumber, port, and data (e.g. $send(0, output, 45).");
        }

        if (parameters.size() == 2) {
            dataToken = processCode("$ref(" + generateSimpleName(port) + "#"
                    + channel + ")");

        } else if (parameters.size() == 3) {
            dataToken = parameters.get(2);
        }

        PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);

        return portHelper.generateCodeForSend(channel, dataToken);
    }

    /**
     * Return the actual CodeStream associated with the given Actor and
     * CodeGenerator. Generally, this will come from the Actor's template file,
     * but EmbeddedCActors get their code from the embeddedCCode parameter.
     * @param namedObj The actor whose code to return.
     * @param codeGenerator The actor's CodeGenerator.
     * @return The actor's actual CodeStream.
     * @exception IllegalActionException If thrown when getting the actor's
     * helper.
     */
    private static CodeStream _getActualCodeStream(NamedObj namedObj,
            CodeGenerator codeGenerator) throws IllegalActionException {

        CodeGeneratorHelper helper = null;
        CodeStream codeStream = null;
        if (namedObj != null
                && namedObj instanceof ptolemy.actor.lib.jni.EmbeddedCActor) {
            helper = (CodeGeneratorHelper) codeGenerator
                    ._getHelper(codeGenerator.getContainer());
            codeStream = new CodeStream(helper);
            // We have an EmbeddedCActor, read the codeBlocks from
            // the embeddedCCode parameter.
            codeStream
                    .setCodeBlocks(((ptolemy.actor.lib.jni.EmbeddedCActor) namedObj).embeddedCCode
                            .getExpression());
        } else {
            helper = (CodeGeneratorHelper) codeGenerator._getHelper(namedObj);
            codeStream = new CodeStream(helper);
        }

        return codeStream;
    }

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    private String _fireCode = "";
}
