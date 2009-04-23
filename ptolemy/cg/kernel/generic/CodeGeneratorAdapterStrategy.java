/* Base class for code generator adapter.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic;

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
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
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
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////////
//// CodeGeneratorAdapterStrategy

/**
 * FIXME: Overhaul comments.
 * The strategy that determines how code should be generated for a certain CodeGeneratorAdapter.
 *
 * <p>Subclasses should override generateFireCode(),
 * generateInitializeCode() generatePostfireCode(),
 * generatePreinitializeCode(), and generateWrapupCode() methods by
 * appending a corresponding code block.
 *
 * <p>Subclasses should be sure to properly indent the code by
 * either using the code block functionality in methods like
 * {@link #_generateBlockCode(String)} or by calling
 * {@link ptolemy.codegen.kernel.CodeStream#indent(String)},
 * for example:
 * <pre>
 *     StringBuffer code = new StringBuffer();
 *     code.append(super.generateWrapupCode());
 *     code.append("// Local wrapup code");
 *     return processCode(CodeStream.indent(code.toString()));
 * </pre>
 *
 * @author Ye Zhou, Gang Zhou, Edward A. Lee, Contributors: Christopher Brooks, Teale Fristoe
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (eal)
 */
// FIXME: Why extend NamedObj? Extend Attribute and store in the actor being adapted?
public class CodeGeneratorAdapterStrategy extends NamedObj {

    /** Construct the code generator adapter strategy.
     */
    public CodeGeneratorAdapterStrategy() {
    }

    /** Set the component for which we are generating code.
     *  @param object The associated component.
     */
    public void setComponent(NamedObj object) {
        // FIXME: Why is this a namedObj when the analyzeActor()
        // method requires an Actor?
        _object = object;

        _parseTreeCodeGenerator = new ParseTreeCodeGenerator() {
            /** Given a string, escape special characters as necessary for the
             *  target language.
             *  @param string The string to escape.
             *  @return A new string with special characters replaced.
             */
            public String escapeForTargetLanguage(String string) {
                return string;
            }

            /** Evaluate the parse tree with the specified root node using
             *  the specified scope to resolve the values of variables.
             *  @param node The root of the parse tree.
             *  @param scope The scope for evaluation.
             *  @return The result of evaluation.
             *  @exception IllegalActionException If an error occurs during
             *   evaluation.
             */
            public ptolemy.data.Token evaluateParseTree(ASTPtRootNode node,
                    ParserScope scope) {
                return new Token();
            }

            /** Generate code that corresponds with the fire() method.
             *  @return The generated code.
             */
            public String generateFireCode() {
                return "/* ParseTreeCodeGenerator.generateFireCode() "
                + "not implemented in codegen.kernel.GenericCodeGenerator */";
            }
        };
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a type to the Set of types used thus far.
     *  @param typeName A string naming the type, for example "Boolean"
     *  or "String".
     */
    public void addNewTypeUsed(String typeName) {
        _codeGenerator._newTypesUsed.add(typeName);
    }

    public void addFunctionUsed(String functionName) {
        _codeGenerator._typeFuncUsed.add(functionName);
    }

    /**
     * Find out each output port that needs to be converted for the
     * actor associated with this adapter. Then, mark these ports along
     * with the sink ports (connection).
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
                Iterator<Channel> sinks = getSinkChannels(sourcePort, j).iterator();

                // for each sink channel connected.
                while (sinks.hasNext()) {
                    Channel sink = sinks.next();
                    TypedIOPort sinkPort = (TypedIOPort) sink.port;
                    if (!sourcePort.getType().equals(sinkPort.getType())) {
                        _markTypeConvert(new Channel(sourcePort, j), sink);
                    }
                }
            }
        }
    }

    // FIXME rodiers: this only used by the PNDirector
    static public boolean checkLocal(boolean forComposite, IOPort port) throws IllegalActionException {
        return (port.isInput() && !forComposite && port.isOutsideConnected())
        || (port.isOutput() && forComposite);
    }

 // FIXME rodiers: this only used by the PNDirector
    static public boolean checkRemote(boolean forComposite, IOPort port) {
        return (port.isOutput() && !forComposite)
        || (port.isInput() && forComposite);
    }

    /**
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
     * @exception IllegalActionException Thrown if the given ptolemy cannot
     *  be resolved.
     */
    final public String codeGenType(Type ptType) {
        return _codeGenerator.codeGenType(ptType);
    }

    /** Return the code stream.
     * @return The code stream.
     */
    // TODO rodiers: do we want to have this public?
    // BTW is this really necessary? (the code stream is used to set
    // correct in the adapter embedded code actor. However
    final public CodeStream getCodeStream() {
        return _codeStream;
    }

    /**
     * Generate the fire code. In this base class, add the name of the
     * associated component in the comment. It checks the inline parameter
     * of the code generator. If the value is true, it generates the actor
     * fire code and the necessary type conversion code. Otherwise, it
     * generate an invocation to the actor function that is generated by
     * generateFireFunctionCode.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        String composite = (getComponent() instanceof CompositeActor) ?
                "Composite Actor: " : "";

        code.append(_eol + _codeGenerator.comment("Fire "
                + composite + generateName(getComponent())));

        if (_codeGenerator.inline.getToken() == BooleanToken.TRUE) {
            code.append(_adapter._generateFireCode());
            code.append(generateTypeConvertFireCode());
        } else if (getComponent().getContainer() != null) {
            // Needed for jni and embeddedJava
            code.append(_adapter._generateFireCode());
        } else {
            code.append(_generateFireInvocation(
                    getComponent()) + ";" + _eol);
        }

        try {
            copyFilesToCodeDirectory(getComponent(), _codeGenerator);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
            "Problem copying files from the necessaryFiles parameter.");
        }
        return processCode(code.toString());
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_eol + "void " + generateName(getComponent())
                + _getFireFunctionArguments() + " {"
                + _eol);
        code.append(_adapter._generateFireCode());
        code.append(generateTypeConvertFireCode());
        code.append("}" + _eol);
        return processCode(code.toString());
    }

    /**
     * Generate the initialize code. In this base class, return empty
     * string. Subclasses may extend this method to generate initialize
     * code of the associated component and append the code to the
     * given string buffer.
     * @return The initialize code of the containing composite actor.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return _generateBlockByName(_defaultBlocks[1]);
    }

    public String generateIterationCode(String countExpression)
        throws IllegalActionException {
    // FIXME: This is to be used in future re-structuring.
    return "";
    }

    /** Generate mode transition code. The mode transition code
     *  generated in this method is executed after each global
     *  iteration, e.g., in HDF model.  Do nothing in this base class.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void generateModeTransitionCode(StringBuffer code)
    throws IllegalActionException {
    }

    /**
     * Generate the postfire code. In this base class, do nothing. Subclasses
     * may extend this method to generate the postfire code of the associated
     * component and append the code to the given string buffer.
     *
     * @return The generated postfire code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_generateBlockByName(_defaultBlocks[3]));

//        Actor actor = (Actor) getComponent();
//        for (IOPort port : (List<IOPort>) actor.outputPortList()) {
//            CodeGeneratorAdapter portAdapter = getCodeGenerator().getAdapter(port);
//            code.append(portAdapter.generatePostfireCode());
//        }
        return processCode(code.toString());
    }

    public String generatePrefireCode() throws IllegalActionException {
        // FIXME: This is to be used in future re-structuring.
        StringBuffer code = new StringBuffer();
        //Actor actor = (Actor) getComponent();
        //for (IOPort port : (List<IOPort>) actor.inputPortList()) {
        //    CodeGeneratorAdapter portAdapter = getCodeGenerator().getAdapter(port);
        //    code.append(portAdapter.generatePrefireCode());
        //}
        return processCode(code.toString());
    }

    /**
     * Generate the preinitialize code. In this base class, return an empty
     * string. This method generally does not generate any execution code
     * and returns an empty string. Subclasses may generate code for variable
     * declaration, defining constants, etc.
     * @return A string of the preinitialize code for the adapter.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        return _generateBlockByName(_defaultBlocks[0]);
    }

    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    final public String generateTypeConvertFireCode() throws IllegalActionException {
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
        Iterator<Channel> channels = getTypeConvertChannels().iterator();
        while (channels.hasNext()) {
            Channel source = channels.next();

            if (!forComposite && source.port.isOutput() || forComposite
                    && source.port.isInput()) {

                Iterator<Channel> sinkChannels = _getTypeConvertSinkChannels(source)
                .iterator();

                while (sinkChannels.hasNext()) {
                    Channel sink = sinkChannels.next();
                    code.append(_generateTypeConvertStatements(source, sink));
                }
            }
        }
        return code.toString();
    }

    /** Generate a variable name for the NamedObj.
     *  @param namedObj The NamedObj to generate variable name for.
     *  @see ptolemy.cg.kernel.generic.GenericCodeGenerator#generateVariableName(NamedObj)
     *  @return The variable name for the NamedObj.
     */
    public String generateVariableName(NamedObj namedObj) {
        return _codeGenerator.generateVariableName(namedObj);
    }


    /**
     * Generate the wrapup code. In this base class, do nothing. Subclasses
     * may extend this method to generate the wrapup code of the associated
     * component and append the code to the given string buffer.
     *
     * @return The generated wrapup code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    public String generateWrapupCode() throws IllegalActionException {
        return _generateBlockByName(_defaultBlocks[4]);
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    public GenericCodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    /** Get the component associated with this adapter.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return (NamedObj) _object;
    }

    public ptolemy.actor.Director getDirector() {
        ptolemy.actor.Director director =
            ((Actor) _object).getExecutiveDirector();

        if (director == null) {
            // getComponent() is at the top level. Use it's local director.
            director = ((Actor) _object).getDirector();
        }
        return director;
    }

    public Director getDirectorAdapter() throws IllegalActionException {
        return (Director) _getAdapter(getDirector());
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

    /** Get the files needed by the code generated from this adapter class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set<String> getHeaderFiles() throws IllegalActionException {
        Set<String> files = new HashSet<String>();

        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("includeFiles", true);
        String includeFilesString = codeStream.toString();

        if (includeFilesString.length() > 0) {
            LinkedList<String> includeFilesList = null;
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

    /** Return a set of directories to include for the generated code.
     *  @return A Set containing the contents of the actor's
     *   "includeDirectories" block in its template.
     *  @exception IllegalActionException If thrown when getting or reading
     *   the CodeStream.
     */
    public Set<String> getIncludeDirectories() throws IllegalActionException {
        Set<String> includeDirectories = new HashSet<String>();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("includeDirectories", true);
        String includeDirectoriesString = codeStream.toString();

        if (includeDirectoriesString.length() > 0) {
            LinkedList<String> includeDirectoriesList = null;
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

    /** Return a set of libraries to link in the generated code.
     *  @return A Set containing the libraries in the actor's
     *   "libraries" block in its template.
     *  @exception IllegalActionException If thrown when getting or reading
     *   the CodeStream.
     */
    public Set<String> getLibraries() throws IllegalActionException {
        Set<String> libraries = new HashSet<String>();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("libraries", true);
        String librariesString = codeStream.toString();

        if (librariesString.length() > 0) {
            LinkedList<String> librariesList = null;
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

    /** Return a set of directories to find libraries in.
     *  @return A Set containing the directories in the actor's
     *   "libraryDirectories" block in its template.
     *  @exception IllegalActionException If thrown when getting or reading
     *   the CodeStream.
     */
    public Set<String> getLibraryDirectories() throws IllegalActionException {
        Set<String> libraryDirectories = new HashSet<String>();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("libraryDirectories", true);
        String libraryDirectoriesString = codeStream.toString();

        if (libraryDirectoriesString.length() > 0) {
            LinkedList<String> libraryDirectoryList = null;
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

    /** Return a set of parameters that will be modified during the execution
     *  of the model. The actor gets those variables if it implements
     *  ExplicitChangeContext interface or it contains PortParameters.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If an actor throws it while getting
     *   modified variables.
     */
    public Set<Parameter> getModifiedVariables() throws IllegalActionException {
        Set<Parameter> set = new HashSet<Parameter>();
        if (_object instanceof ExplicitChangeContext) {
            set.addAll(((ExplicitChangeContext) _object)
                    .getModifiedVariables());
        }

        Iterator<?> inputPorts = ((Actor) _object).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort && inputPort.isOutsideConnected()) {
                set.add(((ParameterPort) inputPort).getParameter());
            }
        }
        return set;
    }

    /** Return the translated new constructor invocation string. Keep the types
     *  referenced in the info table of this adapter. The kernel will retrieve
     *  this information to determine the total number of referenced types in
     *  the model.
     *  @param constructorString The string within the $new() macro.
     *  @return The translated new constructor invocation string.
     *  @exception IllegalActionException The given constructor string is
     *   not well-formed.
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
        if ((openFuncParenIndex == -1)
                || (closeFuncParenIndex != (constructorString.length() - 1))) {
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

    /** Get the object associated with this adapter.
     *  @return The associated object.
     */
    public NamedObj getObject() {
        return (NamedObj) _object;
    }

    /** Return the value or an expression in the target language for
     *  the specified parameter of the associated actor.  If the
     *  parameter is specified by an expression, then the expression
     *  will be parsed. If any parameter referenced in that expression
     *  is specified by another expression, the parsing continues
     *  recursively until either a parameter is directly specified by
     *  a constant or a parameter can be directly modified during
     *  execution in which case a reference to the parameter is
     *  generated.
     *
     *  @param name The name of the parameter.
     *  @param container The container to search upwards from.
     *  @return The value or expression as a string.
     *  @exception IllegalActionException If the parameter does not exist or
     *   does not have a value.
     */
    final public String getParameterValue(String name, NamedObj container)
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
            throw new IllegalActionException(getComponent(), "Invalid cast type: "
                    + attributeName);
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
                 if (_codeGenerator._modifiedVariables.contains(variable)) {
                 return generateVariableName(variable);
                 } else if (variable.isStringMode()) {
                 return "\"" + variable.getExpression() + "\"";
                 }
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
                    Iterator<?> allScopedVariableNames = ModelScope.getAllScopedVariableNames(variable,container).iterator();
                    while (allScopedVariableNames.hasNext()) {
                        results.append(allScopedVariableNames.next().toString() + "\n");
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

            } else /* if (attribute instanceof Settable)*/{
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
                        parseTree = parser.generateParseTree(element.toString());
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

                    return _generateTypeConvertMethod(elementCode,
                            castType, codeGenType(element.getType()));
                }

                throw new IllegalActionException(getComponent(), attributeName
                        + " does not contain an ArrayToken.");
            }

            throw new IllegalActionException(getComponent(), attributeName
                    + " is not a parameter.");
        }
    }

    /** Return the parse tree to use with expressions.
     *  @return the parse tree to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        return _parseTreeCodeGenerator;
    }

    /**
     * Get the port that has the given name.
     * @param refName The given name.
     * @return The port that has the given name.
     */
    final public TypedIOPort getPort(String refName) {
        Actor actor = (Actor) _object;
        Iterator<?> inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (inputPort.getName().equals(refName)) {
                return inputPort;
            }
        }

        Iterator<?> outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (outputPort.getName().equals(refName)) {
                return outputPort;
            }
        }

        return null;
    }

    /** Return the associated actor's rates for all configurations of
     *  this actor.  In this base class, return null.
     *  @return null
     */
    public int[][] getRates() {
        return null;
    }

    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *
     *  FIXME: need documentation on the input string format.
     *
     *  @param name The name of the parameter or port
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    final public String getReference(String name) throws IllegalActionException {
        boolean isWrite = false;
        return getReference(name, isWrite);
    }

    // FIXME: documentation
    public String getReference(String name, boolean isWrite)
    throws IllegalActionException {
        ptolemy.actor.Director director = getDirector();
        Director directorAdapter = (Director) _getAdapter(director);
        return directorAdapter.getReference(name, isWrite, _adapter);
    }


    /**
     * Generate the shared code. This is the first generate method invoked out
     * of all, so any initialization of variables of this adapter should be done
     * in this method. In this base class, return an empty set. Subclasses may
     * generate code for variable declaration, defining constants, etc.
     * @return An empty set in this base class.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public Set<String> getSharedCode() throws IllegalActionException {
        Set<String> sharedCode = new HashSet<String>();
        _codeStream.clear();
        _codeStream.appendCodeBlocks(".*shared.*");
        if (!_codeStream.isEmpty()) {
            sharedCode.add(processCode(_codeStream.toString()));
        }
        return sharedCode;
    }

    /** Get the size of a parameter. The size of a parameter
     *  is the length of its array if the parameter's type is array,
     *  and 1 otherwise.
     *  @param name The name of the parameter.
     *  @return The expression that represents the size of a parameter or port.
     *  @exception IllegalActionException If no port or parameter of
     *   the given name is found.
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
                            return getReference(name) + ".payload." + codeGenType(type) + "->size";
                        }
                    }
                }
            }
        }

        throw new IllegalActionException(getComponent(), "Attribute not found: "
                + name);
    }

    /** Get the write offset in the buffer of a given channel to which a token
     *  should be put. The channel is given by its containing port and
     *  the channel number in that port.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel to which a token
     *   should be put.
     *  @exception IllegalActionException Thrown if the adapter class cannot
     *   be found.
     *  @see #setWriteOffset(IOPort, int, Object)
     */
//    public Object getWriteOffset(IOPort port, int channelNumber)
//    throws IllegalActionException {
//
//        return ((PortCodeGenerator) _getAdapter(port))
//        .getWriteOffset(channelNumber);
//    }

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

    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    final public String processCode(String code) throws IllegalActionException {
        StringBuffer result = new StringBuffer();
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

            if ((currentPos > 0) && (code.charAt(currentPos - 1) == '\\')) {
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

            //if (!_codeGenerator.getMacros().contains(macro)) {
            //    result.append(subcode.substring(0, 1));
            //    result.append(processCode(subcode.substring(1)));
            //} else {
            String name = code.substring(openParenIndex + 1, closeParenIndex);

            name = processCode(name.trim());

            //List arguments = parseArgumentList(name);

            try {
                result.append(_replaceMacro(macro, name));
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Failed to replace the parameter \"" + name
                        + "\" in the macro \"" + macro
                        + "\".\nInitial code was:\n" + code);
            }

            String string = code.substring(closeParenIndex + 1, nextPos);
            result.append(string);
            //}
            currentPos = nextPos;
        }

        return result.toString();
    }

    /** Set the adapter.
     *  @param adapter The given adapter.
     */
    final public void setAdapter(CodeGeneratorAdapter adapter) {
        _adapter = adapter;
        _codeStream = new CodeStream(_adapter);
    }

    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    final public void setCodeGenerator(GenericCodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    /** Set the read offset in a buffer of a given channel from which a token
     *  should be read.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @param readOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException Thrown if the adapter class cannot
     *   be found.
     *  @see #getReadOffset(IOPort, int)
     */
//    final public void setReadOffset(IOPort port, int channelNumber,
//            Object readOffset) throws IllegalActionException {
//        ((PortCodeGenerator) _getAdapter(port))
//        .setReadOffset(channelNumber, readOffset);
//    }

    /** Set the write offset in a buffer of a given channel to which a token
     *  should be put.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @param writeOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException If
     *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     *  @see #getWriteOffset(IOPort, int)
     */
//    final public void setWriteOffset(IOPort port, int channelNumber,
//            Object writeOffset) throws IllegalActionException {
//        ((PortCodeGenerator) _getAdapter(port))
//            .setWriteOffset(channelNumber, writeOffset);
//    }

    /**
     * Get the corresponding type in C from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The C data type.
     */
    public /*static*/ String targetType(Type ptType) {
        return _codeGenerator.targetType(ptType);
    }

    public String toString() {
        return getComponent().toString() + "'s Adapter";
    }

    /////////////////////////////////////////////////////////////////////
    ////                      public inner classes                   ////

    /**
     * Return the index of a specific character in the string starting
     * from the given index. It find the first occurence of the character
     * that is not embedded inside parentheses "()".
     * @param ch The character to search for.
     * @param string The given string to search from.
     * @param fromIndex The index to start the search.
     * @return The first occurence of the character in the string that
     *  is not embedded in parentheses.
     */
    public static int indexOf(String ch, String string, int fromIndex) {

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

    public static List<String> parseList(String parameters) {
        List<String> result = new ArrayList<String>();
        int previousCommaIndex = 0;
        int commaIndex = indexOf(",", parameters, 0);

        while (commaIndex >= 0) {
            String item = parameters.substring(
                    previousCommaIndex, commaIndex).trim();

            result.add(item);

            previousCommaIndex = commaIndex + 1;
            commaIndex = indexOf(",", parameters, previousCommaIndex);
        }

        String item = parameters.substring(
                previousCommaIndex, parameters.length()).trim();

        if (item.trim().length() > 0) {
            result.add(item);
        }

        return result;
    }

    /** Copy files to the code directory.  The optional
     *  <code>fileDependencies</code> codeBlock consists of one or
     *  more lines where each line names a file that should be copied
     *  to the directory named by the <i>codeDirectory</i> parameter
     *  of the code generator. The file is only copied if a file by
     *  that name does not exist in <i>codeDirectory</i> or if the
     *  source file was more recently modified than the destination
     *  file.
     *  <p>Using the <code>fileDependencies</code> code block allows
     *  actor writers to refer to code defined in other files.
     *
     *  @param namedObj If this argument is an instance of
     *  ptolemy.actor.lib.jni.EmbeddedCActor, then the code blocks
     *  from EmbeddedCActor's <i>embeddedCCode</i> parameter are used.
     *  @param codeGenerator The code generator from which the
     *  <i>codeDirectory</i> parameter is read.
     *  @return The modification time of the most recent file.
     *  @exception IOException If there is a problem reading the
     *  <i>codeDirectory</i> parameter.
     *  @exception IllegalActionException If there is a problem reading the
     *  <i>codeDirectory</i> parameter.
     */
    public static long copyFilesToCodeDirectory(NamedObj namedObj,
            GenericCodeGenerator codeGenerator) throws IOException,
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
            LinkedList<String> fileDependenciesList = StringUtilities
            .readLines(fileDependencies);
            File codeDirectoryFile = codeGenerator._codeDirectoryAsFile();
            String necessaryFileName = null;
            Iterator<String> iterator = fileDependenciesList.iterator();
            while (iterator.hasNext()) {
                necessaryFileName = iterator.next();

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
                        || (necessaryFileSource.exists() && necessaryFileSource
                                .lastModified() > necessaryFileDestination
                                .lastModified())) {
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
     *  channel of a multiport.
     * @param port The referenced port.
     * @param isWrite Whether to generate the write or read offset.
     * @param channelString The string that will determine the channel.
     * @return The expression that represents the offset for a channel determined
     *  dynamically in the generated code.
     */
    public static String generateChannelOffset(IOPort port, boolean isWrite,
            String channelString) {
        // By default, return the channel offset for the first channel.
        if (channelString.equals("")) {
            channelString = "0";
        }

        String channelOffset = CodeGeneratorAdapterStrategy.generateName(port);
        channelOffset += (isWrite) ? "_writeOffset" : "_readOffset";
        channelOffset += "[" + channelString + "]";

        return channelOffset;
    }

    /** Generate sanitized name for the given named object. Remove all
     *  underscores to avoid conflicts with systems functions.
     *  @param namedObj The named object for which the name is generated.
     *  @return The sanitized name.
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

    public static String generatePortReference(IOPort port,
            String[] channelAndOffset, boolean isWrite) {

        StringBuffer result = new StringBuffer();
        String channelOffset;
        if (channelAndOffset[1].equals("")) {
            channelOffset = CodeGeneratorAdapterStrategy
            .generateChannelOffset(port, isWrite,
                    channelAndOffset[0]);
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
     * Return an array of strings that are regular expressions of all the
     * code blocks that are appended automatically by default. Since the
     * content of the array are regex, users should use matches() instead
     * of equals() to compare their strings.
     * @return Array of string regular expressions of names of code blocks
     * that are appended by default.
     */
    public static String[] getDefaultBlocks() {
        return _defaultBlocks;
    }


    /** Return a list of channel objects that are the sink input ports given
     *  a port and channel. Note the returned channels are newly
     *  created objects and therefore not associated with the adapter class.
     *  @param port The given output port.
     *  @param channelNumber The given channel number.
     *  @return The list of channel objects that are the sink channels
     *   of the given output channel.
     * @exception IllegalActionException
     */
    public static List<Channel> getSinkChannels(IOPort port, int channelNumber) throws IllegalActionException {
        List<Channel> sinkChannels = new LinkedList<Channel>();
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
             // FIXME: Is this an important warning? The reference to
             // printedNullPortWarnings prevents us from making this
             // a static method.
            if (!printedNullPortWarnings) {
                printedNullPortWarnings = true;
                System.out.println("Warning: Channel " + channelNumber
                        + " of Port \"" + port
                        + "\" was null! Total number of channels: "
                        + remoteReceivers.length);
            }
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

    /** Given a port and channel number, create a Channel that sends
     *  data to the specified port and channel number.
     *  @param port The port.
     *  @param channelNumber The channel number of the port.
     *  @return the source channel.
     *  @exception IllegalActionException If there is a problem getting
     *  information about the receivers or constructing the new Channel.
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

        List<IOPort> sourcePorts = port.sourcePortList();
        sourcePorts.addAll(port.insideSourcePortList());

        for (IOPort sourcePort : sourcePorts) {
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
     * Get the set of channels that need to be type converted.
     * @return Set of channels that need to be type converted.
     */
    public Set<Channel> getTypeConvertChannels() {
        return _portConversions.keySet();
    }
    
    public static void main(String[] args) {
        selfTest();
    }


    public static void selfTest() {
        System.out.println(parseList("(a, b, abc)"));
        System.out.println(indexOf(",", "(a, b, abc,), (a , b, abc,)", 0));
        System.out.println(indexOf(",", "a, b, abc,, (a , b, abc,)", 0));
        System.out.println(indexOf(",", ", (b), abc,, (a , b, abc,)", 0));
        System.out.println(indexOf(",", "a, (b, abc,), (a , b, abc,)", 0));
        System.out.println(indexOf(",", "(((a), b,) a),bc,, (a , b, abc,)", 0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////



    /** Given a block name, generate code for that block.
     *  This method is called by actors adapters that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the adapter .c file.
     */
    protected String _generateBlockCode(String blockName)
    throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        return _generateBlockCode(blockName, new ArrayList<String>());
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors adapters that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @param args The arguments for the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the adapter .c file.
     */
    protected String _generateBlockCode(String blockName, List<String> args)
    throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName, args);
        return processCode(_codeStream.toString());
    }

    /**
     * Generate the fire code. This method is intended to be
     * overwritten by sub-classes to generate actor-specific code.
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
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @param offset The given offset.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
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

        // The references are associated with their own adapter, so we need
        // to find the associated adapter.
        String sourcePortChannel = source.port.getName() + "#"
        + source.channelNumber + ", " + offset;
        String sourceRef = ((CodeGeneratorAdapter) _getAdapter(source.port
                .getContainer())).getReference(sourcePortChannel);

        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
        + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = ((CodeGeneratorAdapter) _getAdapter(sink.port
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
                        result = "$new(" + codeGenType(sinkType) + "(1, 1, " + result + ", TYPE_"
                        + codeGenType(sourceType) + "))";
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

    /** Return the prototype for fire functions.
     *  @return In this base class, return "()".
     *  Derived classes, such as the C code generator adapter
     *  might return "(void)".
     */
    protected String _getFireFunctionArguments() {
        return "()";
    }

    /** Get the code generator adapter associated with the given component.
     *  @param component The given component.
     *  @return The code generator adapter.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    protected CodeGeneratorAdapter _getAdapter(NamedObj component)
    throws IllegalActionException {
        return _codeGenerator.getAdapter(component);
    }

    /**
     * Generate a variable reference for the given channel. This varaible
     * reference is needed for type conversion. The source adapter get this
     * reference instead of using the sink reference directly.
     * This method assumes the given channel is a source (output) channel.
     * @param channel The given source channel.
     * @return The variable reference for the given channel.
     */
    static public String getTypeConvertReference(Channel channel) {
        return generateName(channel.port) + "_" + channel.channelNumber;
    }

    /** Return the replacement string of the given macro. Subclass
     * of GenericCodeGenerator may overriding this method to extend or support
     * a different set of macros.
     * @param macro The given macro.
     * @param parameter The given parameter to the macro.
     * @return The replacement string of the given macro.
     * @exception IllegalActionException Thrown if the given macro or
     *  parameter is not valid.
     */
    protected String _replaceMacro(String macro, String parameter)
    throws IllegalActionException {

        // $$def(abc)
        // ==> abc$def(abc)
        int indexOfDollarSign = macro.indexOf('$');
        if (indexOfDollarSign >= 0) {
            String result = "$" + macro.substring(0, indexOfDollarSign);
            String innerMacro = macro.substring(indexOfDollarSign + 1, macro.length());
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

            TypedIOPort port = getPort(parameter);
            if (port != null) {
                return targetType(port.getType());
            }

            Variable variable = _getVariable(parameter);
            if (variable != null) {
                return targetType(variable.getType());
            }
            throw new IllegalActionException(parameter
                    + " is not a port. $type macro takes in a port.");

        } else if (macro.equals("type") || macro.equals("cgType")) {

            String type = "";
            if (macro.equals("type")) {
                type = "TYPE_";
            }

            TypedIOPort port = getPort(parameter);
            if (port != null) {
                return type + codeGenType(port.getType());
            }

            Variable variable = _getVariable(parameter);
            if (variable != null) {
                return type + codeGenType(variable.getType());
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
            // Try calling a method defined in the adapter first.
            try {
                Method method = getClass().getMethod(macro, new Class[0]);
                return (String) method.invoke(this, new Object[0]);
            } catch (Exception ex) {
                // Don't print out error, since this is probably not an user macro.
            }

            // Try to treat this as an user macro class.
            Method handler = null;
            Method checker = null;
            Class<?> userMacro = null;
            try {
                userMacro = Class.forName(
                        "ptolemy.codegen.kernel.userMacro." + macro);

                handler = userMacro.getMethod("handleMacro", new Class[] { List.class });
                checker = userMacro.getMethod("checkArguments", new Class[] { List.class });
            } catch (Exception ex) {
                // Don't print out error, since this is probably not an user macro.
                return null;
            }

            try {
                checker.invoke(userMacro, new Object[] { parseList(parameter) });
                return (String) handler.invoke(userMacro,
                        new Object[] { parseList(parameter) });
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to invoke user macro ($" + macro + ").");
            }
        }
    }

    /** Find the paired close parenthesis given a string and an index
     *  which is the position of an open parenthesis. Return -1 if no
     *  paired close parenthesis is found.
     *  @param string The given string.
     *  @param pos The given index.
     *  @return The index which indicates the position of the paired
     *   close parenthesis of the string.
     *  @exception IllegalActionException If the character at the
     *   given position of the string is not an open parenthesis or
     *   if the index is less than 0 or past the end of the string.
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
    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The CodeGeneratorAdapter.*/
    protected CodeGeneratorAdapter _adapter;
    

    /** The code generator that contains this adapter class.
     */
    protected GenericCodeGenerator _codeGenerator;

    /**
     * The code stream associated with this adapter.
     */
    protected CodeStream _codeStream = null;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    /** The parse tree to use with expressions. */
    protected ParseTreeCodeGenerator _parseTreeCodeGenerator;

    /** Indent string for indent level 1.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected final static String _INDENT1 = StringUtilities.getIndentPrefix(1);

    /** Indent string for indent level 2.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected final static String _INDENT2 = StringUtilities.getIndentPrefix(2);

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Generate code for a given block.  The comment includes
     *  the portion of the blockName parameter up until the string
     *  "Block".
     *  @param blockName The name of the block, which usually ends
     *  with the string "Block".
     *  @return The generated wrapup code.
     *  @exception IllegalActionException If thrown while appending to the
     *  the block or processing the macros.
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
                            + getComponent().getName()));
        }
        return processCode(_codeStream.toString());

    }

    /**
     * Generate the invocation of the fire function of
     * the given component.
     * @param component The given component.
     * @return The generated code.
     */
    private static String _generateFireInvocation(
            NamedObj component) {
        return generateName(component) + "()";
    }

    /**
     * Generate expression that evaluates to a result of equivalent
     * value with the cast type.
     * @param ref The given variable expression.
     * @param castType The given cast type.
     * @param refType The given type of the variable.
     * @return The variable expression that evaluates to a result of
     *  equivalent value with the cast type.
     * @exception IllegalActionException
     */
    public String _generateTypeConvertMethod(String ref, String castType,
            String refType) throws IllegalActionException {

        if (castType == null || refType == null || castType.equals(refType)) {
            return ref;
        }

        if (isPrimitive(castType)) {
            ref = refType + "to" + castType + "(" + ref + ")";
        } else if (isPrimitive(refType)) {
            ref = "$new(" + refType + "(" + ref + "))";
        }

        if (!castType.equals("Token") && !isPrimitive(castType)) {
            ref = "$typeFunc(TYPE_" + castType + "::convert(" + ref + "))";
        }

        return processCode(ref);
    }

    /**
     * Generate the type conversion statements for the two given channels.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     * FIXME: SDF specific
     */
    private String _generateTypeConvertStatements(Channel source, Channel sink)
    throws IllegalActionException {

        StringBuffer statements = new StringBuffer();

        int rate = Math.max(DFUtilities.getTokenProductionRate(source.port),
                DFUtilities.getTokenConsumptionRate(source.port));

        for (int offset = 0; offset < rate || (offset == 0 && rate == 0); offset++) {
            statements.append(_generateTypeConvertStatement(
                    source, sink, offset));
        }
        return processCode(statements.toString());
    }

    /** Return the actual CodeStream for this Adapter.
     * @return The actual CodeStream.
     * @exception IllegalActionException If thrown by a called method.
     */
    private CodeStream _getActualCodeStream() throws IllegalActionException {
        return _getActualCodeStream(getComponent(), _codeGenerator);
    }

    /**
     * Return the position of the first occurence of the "&" sign in
     * the given code string, starting from the given from position.
     * If the "&" sign found is escaped by "\\", it will be ignored.
     * @param code The given code string.
     * @param from The given position to start searching from.
     * @return The next position of the "&" sign.
     */
    static private int _getMacroStartIndex(String code, int from) {
        int position = from - 1;

        do {
            position = code.indexOf("$", position + 1);

        } while (position > 0 && code.charAt(position - 1) == '\\');

        return position;
    }

    /**
     * Get the list of sink channels that the given source channel needs to
     * be type converted to.
     * @param source The given source channel.
     * @return List of sink channels that the given source channel needs to
     * be type converted to.
     */
    public List<Channel> _getTypeConvertSinkChannels(Channel source) {
        if (_portConversions.containsKey(source)) {
            return _portConversions.get(source);
        }
        return new ArrayList<Channel>();
    }

    /***
     * Return a variable that matches the given label.
     * Null is returned, if no such variable cannot found.
     * @param refName The given label.
     * @return A variable that matches the given label, or
     *  null if no such variable is found.
     */
    private Variable _getVariable(String refName) {
        NamedObj actor = (NamedObj) _object;

        for (Object attribute : actor.attributeList()) {

            if (attribute instanceof Variable) {

                if (((Variable)attribute).getName().equals(refName)) {
                    return (Variable) attribute;
                }
            }
        }
        return null;
    }

    /**
     * Mark the given connection between the source and the sink channels
     * as type conversion required.
     * @param source The given source channel.
     * @param sink The given input channel.
     */
    private void _markTypeConvert(Channel source, Channel sink) {
        List<Channel> sinks;
        if (_portConversions.containsKey(source)) {
            sinks = _portConversions.get(source);
        } else {
            sinks = new ArrayList<Channel>();
            _portConversions.put(source, sinks);
        }
        sinks.add(sink);
    }

    private String _replaceGetMacro(String parameter) throws IllegalActionException {
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
                    + "The $get macro takes in as arguments " +
            "a channelNumber, and a port (e.g. $get(0, output).");
        }

        PortCodeGenerator portAdapter = (PortCodeGenerator) _getAdapter(port);

        return portAdapter.generateCodeForGet(channel);
    }

    private String _replaceSendMacro(String parameter) throws IllegalActionException {
        // e.g. $send(input, 0, token);
        List<String> parameters = parseList(parameter);

        TypedIOPort port = null;
        String channel = "";
        String dataToken = "";

        port = getPort(parameters.get(0));
        channel = parameters.get(1);

        if (port == null || channel.length() == 0) {
            throw new IllegalActionException(parameter
                    + " is not acceptable by $send(). "
                    + "The $send macro takes in as arguments " +
            "a channelNumber, port, and data (e.g. $send(0, output, 45).");
        }

        if (parameters.size() == 2) {
            dataToken = processCode("$ref(" + port.getName() + "#" + channel + ")");

        } else if (parameters.size() == 3) {
            dataToken = parameters.get(2);
        }

        PortCodeGenerator portAdapter = (PortCodeGenerator) _getAdapter(port);

        return portAdapter.generateCodeForSend(channel, dataToken);
    }

    /** Return the actual CodeStream associated with the given Actor and
     *  GenericCodeGenerator.  Generally, this will come from the Actor's template
     *  file, but EmbeddedCActors get their code from the embeddedCCode parameter.
     * @param namedObj The actor whose code to return.
     * @param codeGenerator The actor's GenericCodeGenerator.
     * @return The actor's actual CodeStream.
     * @exception IllegalActionException If thrown when getting the actor's adapter.
     */
    private static CodeStream _getActualCodeStream(NamedObj namedObj,
            GenericCodeGenerator codeGenerator) throws IllegalActionException {

        CodeGeneratorAdapter adapter = null;
        CodeStream codeStream = null;
        if (namedObj != null
                && namedObj instanceof ptolemy.actor.lib.jni.EmbeddedCActor) {
            adapter =  codeGenerator.getAdapter(codeGenerator.getContainer());
            codeStream = new CodeStream(adapter);
            // We have an EmbeddedCActor, read the codeBlocks from
            // the embeddedCCode parameter.
            codeStream
            .setCodeBlocks(((ptolemy.actor.lib.jni.EmbeddedCActor) namedObj).embeddedCCode
                    .getExpression());
        } else {
            adapter = codeGenerator.getAdapter(namedObj);
            codeStream = new CodeStream(adapter);
        }

        return codeStream;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The code block table that stores the code block body (StringBuffer)
     * with the code block name (String) as key.
     */
    private static final String[] _defaultBlocks = { "preinitBlock",
        "initBlock", "fireBlock", "postfireBlock", "wrapupBlock" };

    /** The associated object. */
    private Object _object;

    /** A HashMap that contains mapping for ports and their conversion method.
     *  Ports that does not need to be converted do NOT have record in this
     *  map. The codegen kernel record this mapping during the first pass over
     *  the model. This map is used later in the code generation phase.
     */
    private Hashtable<Channel, List<Channel>> _portConversions = new Hashtable<Channel, List<Channel>>();

    /////////////////////////////////////////////////////////////////////
    ////                      inner classes                   ////


    /** A class that defines a channel object. A channel object is
     *  specified by its port and its channel index in that port.
     */
    public static class Channel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct the channel with the given port and channel number.
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
         *  object, otherwise false;
         */
        public boolean equals(Object object) {
            return object instanceof Channel
            && port.equals(((Channel) object).port)
            && channelNumber == ((Channel) object).channelNumber;
        }

        /**
         * Return the hash code for this channel. Implementing this method
         * is required for comparing the equality of channels.
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
            return port.getName() + "_" + channelNumber;
        }

        /** The port that contains this channel.
         */
        public IOPort port;

        /** The channel number of this channel.
         */
        public int channelNumber;
    }

    /** This class implements a scope, which is used to generate the
     *  parsed expressions in target language.
     */
    private class VariableScope extends ModelScope {
        /** Construct a scope consisting of the variables of the containing
         *  actor and its containers and their scope-extending attributes.
         */
        public VariableScope() {
            _variable = null;
        }

        /** Construct a scope consisting of the variables of the container
         *  of the given instance of Variable and its containers and their
         *  scope-extending attributes.
         *  @param variable The variable whose expression is under code
         *   generation using this scope.
         */
        public VariableScope(Variable variable) {
            _variable = variable;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Look up and return the macro or expression in the target language
         *  corresponding to the specified name in the scope.
         *  @param name The given name string.
         *  @return The macro or expression with the specified name in the scope.
         *  @exception IllegalActionException If thrown while getting buffer
         *   sizes or creating ObjectToken.
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
                    String parameterValue = getParameterValue(name, result.getContainer());
                    try {
                        ASTPtRootNode parseTree =
                            parser.generateParseTree(parameterValue);

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

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The name of the attribute to look up.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
        throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().getType(name);
            }
            return null;
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the type term to look up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
        throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().getTypeTerm(name);
            }
            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of variable names within the scope.
         *  @exception IllegalActionException If there is a problem
         *  getting the identifier set from the variable.
         */
        public Set identifierSet() throws IllegalActionException {
            if (_variable != null) {
                return _variable.getParserScope().identifierSet();
            }
            return null;
        }

        public String toString() {
            return super.toString() + " variable: " + _variable + " variable.parserScope: "
                + (_variable == null ? "N/A, _variable is null" : _variable.getParserScope());
        }
        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** If _variable is not null, then the adapter scope created is
         *  for parsing the expression specified for this variable and
         *  generating the corresponding code in target language.
         */
        private Variable _variable = null;
    }

}
