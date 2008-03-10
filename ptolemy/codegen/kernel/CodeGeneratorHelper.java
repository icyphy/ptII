/* Base class for code generator helper.

 Copyright (c) 2005-2007 The Regents of the University of California.
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
import ptolemy.actor.lib.jni.PointerToken;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.ExplicitChangeContext;
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
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////CodeGeneratorHelper

/**
 * Base class for code generator helper.
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
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (eal)
 */
public class CodeGeneratorHelper extends NamedObj implements ActorCodeGenerator {
    /** Construct a code generator helper.
     */
    public CodeGeneratorHelper() {
        this(null);
    }

    /** Construct the code generator helper associated
     *  with the given component.
     *  @param component The associated component.
     */
    public CodeGeneratorHelper(NamedObj component) {
        // FIXME: Why is this a namedObj when the analyzeActor()
        // method requires an Actor?
        _component = component;

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
                        + "not implemented in codegen.kernel.CodeGenerator */";
            }
        };
    }

    /** Construct the code generator helper associated
     *  with the given component.
     *  @param component The associated component.
     *  @param name The name of helper.  All periods are
     *  replaced with underscores.
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a type to the Set of types used thus far.
     *  @param typeName A string naming the type, for example "Boolean"
     *  or "String".
     */
    public void addNewTypeUsed(String typeName) {
        _codeGenerator._newTypesUsed.add(typeName);
    }

    /**
     * Find out each output port that needs to be converted for the
     * actor associated with this helper. Then, mark these ports along
     * with the sink ports (connection).
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void analyzeTypeConvert() throws IllegalActionException {
        // reset the previous type convert info.
        _portConversions.clear();

        Actor actor = (Actor) _component;

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
     * Get the corresponding type in code generation from the given Ptolemy
     * type.
     * @param ptType The given Ptolemy type.
     * @return The code generation type.
     * @exception IllegalActionException Thrown if the given ptolemy cannot
     *  be resolved.
     */
    public static String codeGenType(Type ptType) {
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
                //result = codeGenType(((ArrayType) ptType).getElementType()) + "Array";
                result = "Array";
            } else if (ptType instanceof MatrixType) {
                //result = ptType.getClass().getSimpleName().replace("Type", "");
                result = "Matrix";
            }
        }

        //if (result.length() == 0) {
        //    throw new IllegalActionException(
        //            "Cannot resolved codegen type from Ptolemy type: " + ptType);
        //}
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

    /** Generate code for declaring read and write offset variables if needed.
     *  Return empty string in this base class.
     *
     *  @return The empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        return "";
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

    /**
     * Get the corresponding type in C from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The C data type.
     */
    public static String targetType(Type ptType) {
        // FIXME: we may need to add more primitive types.
        return ptType == BaseType.INT ? "int"
                : ptType == BaseType.STRING ? "char*"
                        : ptType == BaseType.DOUBLE ? "double"
                                : ptType == BaseType.BOOLEAN ? "boolean"
                                        : ptType == BaseType.LONG ? "long"
                                                : ptType == BaseType.UNSIGNED_BYTE ? "unsigned char"
                                                        : ptType == PointerToken.POINTER ? "void*"
                                                                : "Token";
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

        String channelOffset = CodeGeneratorHelper.generateName(port);
        channelOffset += (isWrite) ? "_writeOffset" : "_readOffset";
        channelOffset += "[" + channelString + "]";

        return channelOffset;
    }

    /**
     * Generate the fire code. In this base class, add the name of the
     * associated component in the comment. Subclasses may extend this
     * method to generate the fire code of the associated component.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateFireCode() throws IllegalActionException {
        _codeStream.clear();

        String composite = (getComponent() instanceof CompositeActor) ? "Composite Actor: "
                : "";

        _codeStream.append(_eol
                + CodeStream.indent(_codeGenerator.comment("Fire " + composite
                        + getComponent().getName())));

        _codeStream.appendCodeBlock(_defaultBlocks[2], true); // fireBlock

        try {
            copyFilesToCodeDirectory(getComponent(), _codeGenerator);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem copying files from the necessaryFiles parameter.");
        }
        return processCode(_codeStream.toString());
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
        // FIXME: This is C specific, it should be abstracted.
        // We use (void) so as to avoid the avr-gcc 3.4.6 warning:
        // "function declaration isn't a prototype"
        code.append(_eol + "void " + generateName(getComponent()) + "(void) {"
                + _eol);
        code.append(generateFireCode());
        code.append(generateTypeConvertFireCode());
        code.append("}" + _eol);
        return code.toString();
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

    /** Generate the main entry point.
     *  @return In this base class, return a comment.  Subclasses
     *  should return the definition of the main entry point for a program.
     *  In C, this would be defining main().
     *  @exception IllegalActionException Not thrown in this base class.
     */

    //public String generateMainEntryCode() throws IllegalActionException {
    //    return _codeGenerator.comment("main entry code");
    //}
    /** Generate the main entry point.
     *  @return In this base class, return a comment.  Subclasses
     *  should return the a string that closes optionally calls exit
     *  and closes the main() method
     *  @exception IllegalActionException Not thrown in this base class.
     */
    //public String generateMainExitCode() throws IllegalActionException {
    //    return _codeGenerator.comment("main exit code");
    //}
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

    /** Generate sanitized name for the given named object. Remove all
     *  underscores to avoid conflicts with systems functions.
     *  @param namedObj The named object for which the name is generated.
     *  @return The sanitized name.
     */
    public static String generateName(NamedObj namedObj) {
        String name = StringUtilities.sanitizeName(namedObj.getFullName());
        if (name.startsWith("_")) {
            name = name.substring(1, name.length());
        }
        return name;
    }

    /**
     * Generate the expression that represents the offset in the generated
     * code.
     * @param offsetString The specified offset from the user.
     * @param port The referenced port.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @return The expression that represents the offset in the generated code.
     * @exception IllegalActionException If there is problems getting the port
     *  buffer size or the offset in the channel and offset map.
     */
    public String generateOffset(String offsetString, IOPort port, int channel,
            boolean isWrite) throws IllegalActionException {
        ptolemy.actor.Director director = 
            ((Actor) _component).getExecutiveDirector();
        
        if (director == null) {
            // _component is at the top level. Use it's local director.
            director = ((Actor) _component).getDirector();
        }
        Director directorHelper = (Director) _getHelper(director);
        Receiver[][] r = port.getReceivers();
        // FIXME: use receivers to generate the offset code.
        return processCode(directorHelper.generateOffset(
                offsetString, port, channel, isWrite, this));        
    }

    /**
     * Generate the expression that represents the offset in the generated
     * code.
     * @param offsetString The specified offset from the user.
     * @param port The referenced port.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @return The expression that represents the offset in the generated code.
     * @exception IllegalActionException If there is problems getting the port
     *  buffer size or the offset in the channel and offset map.
     */
    protected String _generateOffset(String offsetString, IOPort port, int channel, boolean isWrite) throws IllegalActionException {
        boolean dynamicReferencesAllowed = ((BooleanToken) _codeGenerator.allowDynamicMultiportReference
                .getToken()).booleanValue();
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        // When dynamic references are allowed, any input ports require
        // offsets.
        if (dynamicReferencesAllowed && port.isInput()) {
            if (!(port.isMultiport() || getBufferSize(port) > 1)) {
                return "";
            }
        } else {
            if (!(getBufferSize(port) > 1)) {
                return "";
            }
        }

        String result = null;
        Object offsetObject;

        // Get the offset index.
        if (isWrite) {
            offsetObject = getWriteOffset(port, channel);
        } else {
            offsetObject = getReadOffset(port, channel);
        }

        if (!offsetString.equals("")) {
            // Specified offset.

            String temp;
            if (offsetObject instanceof Integer && _isInteger(offsetString)) {

                int offset = ((Integer) offsetObject).intValue()
                        + (Integer.valueOf(offsetString)).intValue();

                offset %= getBufferSize(port, channel);
                temp = Integer.toString(offset);
                /*
                 int divisor = getBufferSize(sinkPort,
                 sinkChannelNumber);
                 temp = "("
                 + getWriteOffset(sinkPort,
                 sinkChannelNumber) + " + "
                 + channelAndOffset[1] + ")%" + divisor;
                 */

            } else {
                // FIXME: We haven't check if modulo is 0. But this
                // should never happen. For offsets that need to be
                // represented by string expression,
                // getBufferSize(port, channelNumber) will always
                // return a value at least 2.

                if (padBuffers) {
                    int modulo = getBufferSize(port, channel) - 1;
                    temp = "(" + offsetObject.toString() + " + " + offsetString
                            + ")&" + modulo;
                } else {
                    int modulo = getBufferSize(port, channel);
                    temp = "(" + offsetObject.toString() + " + " + offsetString
                            + ")%" + modulo;
                }
            }

            result = "[" + temp + "]";

        } else {
            // Did not specify offset, so the receiver buffer
            // size is 1. This is multiple firing.

            if (offsetObject instanceof Integer) {
                int offset = ((Integer) offsetObject).intValue();

                offset %= getBufferSize(port, channel);

                result = "[" + offset + "]";
            } else {
                if (padBuffers) {
                    int modulo = getBufferSize(port, channel) - 1;
                    result = "[" + offsetObject + "&" + modulo + "]";
                } else {
                    result = "[" + offsetObject + "%"
                            + getBufferSize(port, channel) + "]";
                }
            }
        }
        return result;
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
        return _generateBlockByName(_defaultBlocks[3]);
    }

    /**
     * Generate the preinitialize code. In this base class, return an empty
     * string. This method generally does not generate any execution code
     * and returns an empty string. Subclasses may generate code for variable
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

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        return "";
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the helper class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        //  Generate variable initialization for referenced parameters.
        if (!_referencedParameters.isEmpty()) {
            code.append(_eol
                    + _codeGenerator.comment(1, _component.getName()
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
                                + getParameterValue(parameter.getName(),
                                        _component) + ";" + _eol);
                    }
                } catch (Throwable throwable) {
                    throw new IllegalActionException(_component, throwable,
                            "Failed to generate variable initialization for \""
                                    + parameter + "\"");
                }
            }
        }
        return code.toString();
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

    /**
     * Return the buffer size of a given port, which is the maximum of
     * the bufferSizes of all channels of the given port.
     * @param port The given port.
     * @return The buffer size of the given port.
     * @exception IllegalActionException If the
     * {@link #getBufferSize(IOPort, int)} method throws it.
     * @see #setBufferSize(IOPort, int, int)
     */
    public int getBufferSize(IOPort port) throws IllegalActionException {
        int bufferSize = 1;

        if (port.getContainer() == _component) {
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

    /** Get the buffer size of the given port of this actor.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @return The buffer size of the given port and channel.
     *  @exception IllegalActionException If the getBufferSize()
     *   method of the actor helper class throws it.
     * @see #setBufferSize(IOPort, int, int)
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        if (port.getContainer() == _component) {
            if (_bufferSizes == null) {
                throw new InternalErrorException(this, null,
                        "_bufferSizes is null?");
            }
            if (_bufferSizes.get(port) == null) {
                StringBuffer buffers = new StringBuffer();
                Iterator ports = _bufferSizes.keySet().iterator();
                while (ports.hasNext()) {
                    IOPort aPort = (IOPort) ports.next();
                    if (buffers.length() > 0) {
                        buffers.append(", ");
                    }
                    buffers.append(aPort.getName());
                }
                throw new InternalErrorException(this, null,
                        "_bufferSizes.get(" + port
                                + ") is null?, _bufferSizes.size(): "
                                + _bufferSizes.size() + " ports: "
                                + buffers.toString());
            }
            return ((int[]) _bufferSizes.get(port))[channelNumber];
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(port
                    .getContainer());
            return actorHelper.getBufferSize(port, channelNumber);
        }
    }

    /** Get the code generator associated with this helper class.
     *  @return The code generator associated with this helper class.
     *  @see #setCodeGenerator(CodeGenerator)
     */
    public CodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    /** Get the component associated with this helper.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return _component;
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

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this helper class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();

        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("includeFiles", true);
        String includeFilesString = codeStream.toString();

        if (includeFilesString.length() > 0) {
            LinkedList includeFilesList = new LinkedList();
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
    public Set getIncludeDirectories() throws IllegalActionException {
        Set includeDirectories = new HashSet();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("includeDirectories", true);
        String includeDirectoriesString = codeStream.toString();

        if (includeDirectoriesString.length() > 0) {
            LinkedList includeDirectoriesList = new LinkedList();
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
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new HashSet();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("libraries", true);
        String librariesString = codeStream.toString();

        if (librariesString.length() > 0) {
            LinkedList librariesList = new LinkedList();
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
    public Set getLibraryDirectories() throws IllegalActionException {
        Set libraryDirectories = new HashSet();
        CodeStream codeStream = _getActualCodeStream();
        codeStream.appendCodeBlock("libraryDirectories", true);
        String libraryDirectoriesString = codeStream.toString();

        if (libraryDirectoriesString.length() > 0) {
            LinkedList libraryDirectoryList = new LinkedList();
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
    public Set getModifiedVariables() throws IllegalActionException {
        Set set = new HashSet();
        if (_component instanceof ExplicitChangeContext) {
            set.addAll(((ExplicitChangeContext) _component)
                    .getModifiedVariables());
        }

        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort && inputPort.getWidth() > 0) {
                set.add(((ParameterPort) inputPort).getParameter());
            }
        }
        return set;
    }

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
                            + "[i.e. -- $new(Array(8, 8, arg1, arg2, ...))]");
        }

        String typeName = constructorString.substring(0, openFuncParenIndex)
                .trim();

        // Record the referenced type function in the infoTable.
        _codeGenerator._newTypesUsed.add(typeName);

        return typeName + "_new"
                + constructorString.substring(openFuncParenIndex);
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
    public String getParameterValue(String name, NamedObj container)
            throws IllegalActionException {
        name = processCode(name);

        StringTokenizer tokenizer = new StringTokenizer(name, ",");

        String attributeName = tokenizer.nextToken().trim();
        String offset = null;
        String castType = null;

        if (tokenizer.hasMoreTokens()) {
            offset = tokenizer.nextToken().trim();

            if (tokenizer.hasMoreTokens()) {
                throw new IllegalActionException(_component, name
                        + " does not have the correct format for"
                        + " accessing the parameter value.");
            }
        }

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(attributeName, "()",
                false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(_component, "Invalid cast type: "
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
                parseTreeCodeGenerator.evaluateParseTree(parseTree,
                        new VariableScope(variable));

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
            //throw new IllegalActionException(_component,
            //        "Attribute does not have a value: " + name);
        } else {
            // FIXME: if offset != null, for now we assume the value of
            // the parameter is fixed during execution.
            if (attribute instanceof Parameter) {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof ArrayToken) {
                    Token element = ((ArrayToken) token).getElement(Integer
                            .valueOf(offset).intValue());

                    return _generateTypeConvertMethod(element.toString(),
                            castType, codeGenType(element.getType()));
                }

                throw new IllegalActionException(_component, attributeName
                        + " does not contain an ArrayToken.");
            }

            throw new IllegalActionException(_component, attributeName
                    + " is not a parameter.");
        }
    }

    /** Return the parse tree to use with expressions.
     *  @return the parse tree to use with expressions.
     */
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        return _parseTreeCodeGenerator;
    }

    /** Return the associated actor's rates for all configurations of
     *  this actor.  In this base class, return null.
     *  @return null
     */
    public int[][] getRates() {
        return null;
    }

    /** Get the read offset in the buffer of a given channel from which a token
     *  should be read. The channel is given by its containing port and
     *  the channel number in that port.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel from which a token
     *   should be read.
     *  @exception IllegalActionException Thrown if the helper class cannot
     *   be found.
     *  @see #setReadOffset(IOPort, int, Object)
     */
    public Object getReadOffset(IOPort inputPort, int channelNumber)
            throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            return ((Object[]) _readOffsets.get(inputPort))[channelNumber];
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            return actorHelper.getReadOffset(inputPort, channelNumber);
        }
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
    public String getReference(String name) throws IllegalActionException {
        boolean isWrite = false;
        return getReference(name, isWrite);
    }

    private String getReference(String name, boolean isWrite) 
            throws IllegalActionException {
        ptolemy.actor.Director director = 
            ((Actor) _component).getExecutiveDirector();
        
        if (director == null) {
            // _component is at the top level. Use it's local director.
            director = ((Actor) _component).getDirector();
        }
        Director directorHelper = (Director) _getHelper(director);
        
        return directorHelper.getReference(name, isWrite, this);
    }

    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *  @param name The name of the parameter or port
     *  @param isWrite Whether to generate the write or read offset.
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    protected String _getReference(String name, boolean isWrite)
            throws IllegalActionException {
        boolean dynamicReferencesAllowed = ((BooleanToken) _codeGenerator.allowDynamicMultiportReference
                .getToken()).booleanValue();

        name = processCode(name);

        String castType = null;
        String refType = null;

        StringBuffer result = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if ((tokenizer.countTokens() != 1) && (tokenizer.countTokens() != 3)
                && (tokenizer.countTokens() != 5)) {
            throw new IllegalActionException(_component,
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(_component, "Invalid cast type: "
                    + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            castType = tokenizer2.nextToken().trim();
            refName = tokenizer2.nextToken().trim();
        }

        boolean forComposite = false;

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
        if (refName.charAt(0) == '@') {
            forComposite = true;
            refName = refName.substring(1);
        }

        TypedIOPort port = getPort(refName);

        String[] channelAndOffset = _getChannelAndOffset(name);

        if (port != null) {
            refType = codeGenType(port.getType());

            int channelNumber = 0;
            boolean isChannelNumberInt = true;
            if (!channelAndOffset[0].equals("")) {
                // If dynamic multiport references are allowed, catch errors
                // when the channel specification is not an integer.
                if (dynamicReferencesAllowed) {
                    try {
                        channelNumber = 
                            (Integer.valueOf(channelAndOffset[0])).intValue();
                    } catch (Exception ex) {
                        isChannelNumberInt = false;
                    }
                } else {
                    channelNumber = 
                        (Integer.valueOf(channelAndOffset[0])).intValue();
                }
            }

            if (!isChannelNumberInt) { // variable channel reference.
                if (port.isOutput()) {
                    throw new IllegalActionException(
                            "Variable channel reference not supported"
                                    + "for output ports");
                } else {

                    String channelOffset;
                    if (channelAndOffset[1] == "") {
                        channelOffset = CodeGeneratorHelper
                                .generateChannelOffset(port, isWrite,
                                        channelAndOffset[0]);
                    } else {
                        channelOffset = channelAndOffset[1];
                    }

                    result.append(generateName(_component));
                    result.append("_");
                    result.append(port.getName());
                    if (port.isMultiport()) {
                        result.append("[" + channelAndOffset[0] + "]");
                    }
                    result.append("[" + channelOffset + "]");

                    return result.toString();
                }
            }

            // To support modal model, we need to check the following condition
            // first because an output port of a modal controller should be
            // mainly treated as an output port. However, during choice action,
            // an output port of a modal controller will receive the tokens sent
            // from the same port.  During commit action, an output port of a modal
            // controller will NOT receive the tokens sent from the same port.
            if ((port.isOutput() && !forComposite)
                    || (port.isInput() && forComposite)) {
                Receiver[][] remoteReceivers;

                // For the same reason as above, we cannot do: if (port.isInput())...
                if (port.isOutput()) {
                    remoteReceivers = port.getRemoteReceivers();
                } else {
                    remoteReceivers = port.deepGetReceivers();
                }

                if (remoteReceivers.length == 0) {
                    // The channel of this output port doesn't have any sink.
                    result.append(generateName(_component));
                    result.append("_");
                    result.append(port.getName());
                    return _generateTypeConvertMethod(result.toString(),
                            castType, refType);
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
                    if (typeConvertSinks.contains(channel) && 
                            isPrimitive(((TypedIOPort) sourceChannel.port).getType())) {

                        if (!hasTypeConvertReference) {
                            if (i != 0) {
                                result.append(" = ");
                            }
                            result.append(_getTypeConvertReference(sourceChannel));

                            if (dynamicReferencesAllowed && port.isInput()) {
                                if (channelAndOffset[1].trim().length() > 0) {
                                    result.append("[" + channelAndOffset[1].trim() + "]");
                                } else {
                                    result.append("[" + 
                                        CodeGeneratorHelper.generateChannelOffset(
                                        port, isWrite, channelAndOffset[0]) + "]");
                                }
                            } else {
                                int rate = Math.max(
                                        DFUtilities.getTokenProductionRate(sourceChannel.port),
                                        DFUtilities.getTokenConsumptionRate(sourceChannel.port));
                                if (rate > 1
                                        && channelAndOffset[1].trim().length() > 0) {
                                    result.append("["
                                            + channelAndOffset[1].trim() + "]");
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
                        result.append(generateOffset(channelAndOffset[1],
                                sinkPort, sinkChannelNumber, true));
                    }
                }

                return _generateTypeConvertMethod(result.toString(), castType,
                        refType);
            }

            // Note that if the width is 0, then we have no connection to
            // the port but the port might be a PortParameter, in which
            // case we want the Parameter.
            // codegen/c/actor/lib/string/test/auto/StringCompare3.xml
            // tests this.

            if ((port.isInput() && !forComposite && port.getWidth() > 0)
                    || (port.isOutput() && forComposite)) {

                result.append(generateName(port));

                //if (!channelAndOffset[0].equals("")) {
                if (port.isMultiport()) {
                    // Channel number specified. This must be a multiport.
                    result.append("[" + channelAndOffset[0] + "]");
                }

                result.append(generateOffset(channelAndOffset[1], port,
                        channelNumber, isWrite));

                return _generateTypeConvertMethod(result.toString(), castType,
                        refType);
            }
        }

        // Try if the name is a parameter.
        Attribute attribute = _component.getAttribute(refName);

        if (attribute != null) {
            //FIXME: potential bug: if the attribute is not a parameter,
            //it will be referenced but not declared.
            if (attribute instanceof Parameter) {
                _referencedParameters.add(attribute);
                refType = codeGenType(((Parameter) attribute).getType());
            }

            result.append(_codeGenerator.generateVariableName(attribute));

            if (!channelAndOffset[0].equals("")) {
                throw new IllegalActionException(_component,
                        "a parameter cannot have channel number.");
            }

            if (!channelAndOffset[1].equals("")) {
                //result.append("[" + channelAndOffset[1] + "]");
                result.insert(0, "Array_get(");
                result.append(" ," + channelAndOffset[1] + ")");

                Type elementType = ((ArrayType) ((Parameter) attribute)
                        .getType()).getElementType();

                if (isPrimitive(elementType)) {
                    result.append(".payload." + codeGenType(elementType));
                }
            }
            return _generateTypeConvertMethod(result.toString(), castType,
                    refType);
        }

        throw new IllegalActionException(_component, "Reference not found: "
                + name);
    }

    /**
     * Get the port that has the given name.
     * @param refName The given name.
     * @return The port that has the given name.
     */
    public TypedIOPort getPort(String refName) {
        Actor actor = (Actor) _component;

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (inputPort.getName().equals(refName)) {
                return inputPort;
            }
        }

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // The channel is specified as $ref(port#channelNumber).
            if (outputPort.getName().equals(refName)) {
                return outputPort;
            }
        }

        return null;
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
        sharedCode.add(processCode(_codeStream.toString()));
        return sharedCode;
    }

    /** Return a list of channel objects that are the sink input ports given
     *  a port and channel. Note the returned channels are newly
     *  created objects and therefore not associated with the helper class.
     *  @param port The given output port.
     *  @param channelNumber The given channel number.
     *  @return The list of channel objects that are the sink channels
     *   of the given output channel.
     */
    public static List getSinkChannels(IOPort port, int channelNumber) {
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
        Attribute attribute = ModelScope.getScopedVariable(null, _component,
                name);

        if (attribute != null) {
            // FIXME:  Could it be something other than variable?
            if (attribute instanceof Variable) {
                Token token = ((Variable) attribute).getToken();

                if (token instanceof ArrayToken) {
                    return String.valueOf(((ArrayToken) token).length());
                }

                return "1";
            }
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
                            return getReference(name) + ".payload.Array->size";
                        }
                    }
                }
            }
        }

        throw new IllegalActionException(_component, "Attribute not found: "
                + name);
    }

    /** Given a port and channel number, create a Channel that sends
     *  data to the specified port and channel number.
     *  @param port The port.
     *  @param channelNumber The channel number of the port.
     *  @return the source channel.
     *  @exception IllegalActionException If there is a problem getting
     *  information about the receivers or constructing the new Channel.
     */
    public Channel getSourceChannel(IOPort port, int channelNumber)
            throws IllegalActionException {
        Receiver[][] receivers;

        if (port.isInput()) {
            receivers = port.getReceivers();
        } else {
            receivers = port.getRemoteReceivers();
        }

        TypedIOPort sourcePort = ((TypedIOPort) port.sourcePortList().get(0));

        Channel source = new Channel(sourcePort, sourcePort
                .getChannelForReceiver(receivers[0][0]));

        return source;
    }

    /** Get the write offset in the buffer of a given channel to which a token
     *  should be put. The channel is given by its containing port and
     *  the channel number in that port.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel to which a token
     *   should be put.
     *  @exception IllegalActionException Thrown if the helper class cannot
     *   be found.
     *  @see #setWriteOffset(IOPort, int, Object)
     */
    public Object getWriteOffset(IOPort inputPort, int channelNumber)
            throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            return ((Object[]) _writeOffsets.get(inputPort))[channelNumber];
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            return actorHelper.getWriteOffset(inputPort, channelNumber);
        }
    }

    /**
     * Determine if the given type is primitive.
     * @param ptType The given ptolemy type.
     * @return true if the given type is primitive, otherwise false.
     * @exception IllegalActionException Thrown if there is no
     *  corresponding codegen type.
     */
    public static boolean isPrimitive(Type ptType)
            throws IllegalActionException {
        return CodeGenerator._primitiveTypes.contains(codeGenType(ptType));
    }

    /**
     * Determine if the given type is primitive.
     * @param cgType The given codegen type.
     * @return true if the given type is primitive, otherwise false.
     */
    public static boolean isPrimitive(String cgType) {
        return CodeGenerator._primitiveTypes.contains(cgType);
    }

    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    public String processCode(String code) throws IllegalActionException {
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
                throw new IllegalActionException(_component,
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
            } catch (IllegalActionException ex) {
                throw new IllegalActionException(this, ex,
                        "Failed to replace the parameter \"" + name
                                + "\" in the macro \"" + macro + "\"");
            }

            String string = code.substring(closeParenIndex + 1, nextPos);
            result.append(string);
            //}
            currentPos = nextPos;
        }

        return result.toString();
    }

    /**
     * Return the position of the first occurence of the "&" sign in
     * the given code string, starting from the given from position.
     * If the "&" sign found is escaped by "\\", it will be ignored.
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

    /** Reset the offsets of all channels of all input ports of the
     *  associated actor to the default value of 0.
     *
     *  @return The reset code of the associated actor.
     *  @exception IllegalActionException If thrown while getting or
     *   setting the offset.
     */
    public String resetInputPortsOffset() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            for (int i = 0; i < port.getWidth(); i++) {
                Object readOffset = getReadOffset(port, i);
                if (readOffset instanceof Integer) {
                    setReadOffset(port, i, Integer.valueOf(0));
                } else {
                    code.append(CodeStream.indent(((String) readOffset)
                            + " = 0;" + _eol));
                }
                Object writeOffset = getWriteOffset(port, i);
                if (writeOffset instanceof Integer) {
                    setWriteOffset(port, i, Integer.valueOf(0));
                } else {
                    code.append(CodeStream.indent(((String) writeOffset)
                            + " = 0;" + _eol));
                }
            }
        }
        return code.toString();
    }

    /** Set the buffer size of a given port.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @param bufferSize The buffer size to be set to that port and channel.
     *  @see #getBufferSize(IOPort)
     */
    public void setBufferSize(IOPort port, int channelNumber, int bufferSize) {
        int[] bufferSizes = (int[]) _bufferSizes.get(port);
        bufferSizes[channelNumber] = bufferSize;
    }

    /** Set the code generator associated with this helper class.
     *  @param codeGenerator The code generator associated with this
     *   helper class.
     *  @see #getCodeGenerator()
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    /** Set the read offset in a buffer of a given channel from which a token
     *  should be read.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel.
     *  @param readOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException Thrown if the helper class cannot
     *   be found.
     *  @see #getReadOffset(IOPort, int)
     */
    public void setReadOffset(IOPort inputPort, int channelNumber,
            Object readOffset) throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            Object[] readOffsets = (Object[]) _readOffsets.get(inputPort);
            readOffsets[channelNumber] = readOffset;
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            actorHelper.setReadOffset(inputPort, channelNumber, readOffset);
        }
    }

    /** Set the write offset in a buffer of a given channel to which a token
     *  should be put.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel.
     *  @param writeOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException If
     *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     *  @see #getWriteOffset(IOPort, int)
     */
    public void setWriteOffset(IOPort inputPort, int channelNumber,
            Object writeOffset) throws IllegalActionException {
        if (inputPort.getContainer() == _component) {
            Object[] writeOffsets = (Object[]) _writeOffsets.get(inputPort);
            writeOffsets[channelNumber] = writeOffset;
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper(inputPort
                    .getContainer());
            actorHelper.setWriteOffset(inputPort, channelNumber, writeOffset);
        }
    }

    /////////////////////////////////////////////////////////////////////
    ////                      public inner classes                   ////

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
         * Return the string representation of this channel.
         * @return The string representation of this channel.
         */
        public String toString() {
            return port.getName() + "_" + channelNumber;
        }

        /**
         * Return the hash code for this channel. Implementing this method
         * is required for comparing the equality of channels.
         * @return Hash code for this channel.
         */
        public int hashCode() {
            return port.hashCode() + channelNumber;
        }

        /** The port that contains this channel.
         */
        public IOPort port;

        /** The channel number of this channel.
         */
        public int channelNumber;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** This class implements a scope, which is used to generate the
     *  parsed expressions in target language.
     */
    protected class VariableScope extends ModelScope {
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

            NamedObj container = _component;
            if (_variable != null) {
                container = _variable.getContainer();
            }

            Variable result = getScopedVariable(_variable, container, name);

            if (result != null) {
                // If the variable found is a modified variable, which means
                // its vaule can be directly changed during execution
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
                    ASTPtRootNode parseTree = parser.generateParseTree(
                            getParameterValue(name, result.getContainer()));
                    ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
                    return evaluator.evaluateParseTree(parseTree, this);                    
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

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** If _variable is not null, then the helper scope created is
         *  for parsing the expression specified for this variable and
         *  generating the corresponding code in target language.
         */
        private Variable _variable = null;
    }

    /** Create the buffer size and offset maps for each input port, which is
     *  associated with this helper object. A key of the map is an IOPort
     *  of the actor. The corresponding value is an array of channel objects.
     *  The i-th channel object corresponds to the i-th channel of that IOPort.
     *  This method is used to maintain a internal HashMap of channels of the
     *  actor. The channel objects in the map are used to keep track of the
     *  buffer sizes or offsets in their buffer.
     *  @exception IllegalActionException If the director helper or executive
     *   director is not found, or if
     *   {@link #setReadOffset(IOPort, int, Object)} method throws it, or if
     *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     *
     */
    protected void _createBufferSizeAndOffsetMap()
            throws IllegalActionException {

        _createInputBufferSizeAndOffsetMap();

    }

    /** Create the input buffer and offset map.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    protected void _createInputBufferSizeAndOffsetMap()
            throws IllegalActionException {

        //We only care about input ports where data are actually stored
        //except when an output port is not connected to any input port.
        //In that case the variable corresponding to the unconnected output
        //port always has size 1 and the assignment to this variable is
        //performed just for the side effect.
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            int length = port.getWidth();

            //if (length == 0) {
            //    length = 1;
            //}
            int[] bufferSizes = new int[length];
            _bufferSizes.put(port, bufferSizes);

            ptolemy.actor.Director director = ((Actor) _component)
                    .getExecutiveDirector();
            if (director == null) {
                // _component is at the top level. Use it's local director.
                director = ((Actor) _component).getDirector();
            }
            Director directorHelper = (Director) _getHelper(director);

            for (int i = 0; i < port.getWidth(); i++) {
                int bufferSize = directorHelper.getBufferSize(port, i);
                setBufferSize(port, i, bufferSize);
            }

            Object[] readOffsets = new Object[length];
            _readOffsets.put(port, readOffsets);

            Object[] writeOffsets = new Object[length];
            _writeOffsets.put(port, writeOffsets);

            for (int i = 0; i < length; i++) {
                setReadOffset(port, i, Integer.valueOf(0));
                setWriteOffset(port, i, Integer.valueOf(0));
            }
        }
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors helpers that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the helper .c file.
     */
    protected String _generateBlockCode(String blockName)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        return _generateBlockCode(blockName, new ArrayList());
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors helpers that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @param args The arguments for the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the helper .c file.
     */
    protected String _generateBlockCode(String blockName, List args)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        _codeStream.clear();
        _codeStream.appendCodeBlock(blockName, args);
        return processCode(_codeStream.toString());
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
    protected String _generateTypeConvertMethod(String ref, String castType,
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
     * helpers for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatements(Channel source, Channel sink)
            throws IllegalActionException {

        StringBuffer statements = new StringBuffer();

        int rate = Math.max(DFUtilities.getTokenProductionRate(source.port),
                DFUtilities.getTokenConsumptionRate(source.port));

        for (int offset = 0; offset < rate || (offset == 0 && rate == 0); offset++) {
            statements.append(CodeStream.indent(_generateTypeConvertStatement(
                    source, sink, offset)));
        }
        return processCode(statements.toString());
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
        String sourcePortChannel = source.port.getName() + "#"
                + source.channelNumber + ", " + offset;
        String sourceRef = ((CodeGeneratorHelper) _getHelper(source.port
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
                        result = "$new(Array(1, 1, " + result + ", TYPE_"
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
                                + ", TYPE_"
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

    /** Return the channel and offset given in a string.
     *  The result is an string array of length 2. The first element
     *  indicates the channel index, and the second the offset. If either
     *  element is an empty string, it means that channel/offset is not
     *  specified.
     * @param name The given string.
     * @return An string array of length 2, containing expressions of the
     *  channel index and offset.
     * @exception IllegalActionException If the channel index or offset
     *  specified in the given string is illegal.
     */
    protected String[] _getChannelAndOffset(String name)
            throws IllegalActionException {

        String[] result = { "", "" };

        // Given expression of forms:
        //     "port"
        //     "port, offset", or
        //     "port#channel, offset".

        int poundIndex = _indexOf('#', name, 0);
        int commaIndex = _indexOf(',', name, 0);

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
     * Return the index of a specific character in the string starting
     * from the given index. It find the first occurence of the character
     * that is not embedded inside parentheses "()".
     * @param ch The character to search for.
     * @param string The given string to search from.
     * @param fromIndex The index to start the search.
     * @return The first occurence of the character in the string that
     *  is not embedded in parentheses.
     * @throws IllegalActionException Thrown if the given string does not
     *  contain the same number of open and closed parentheses.
     */
    private static int _indexOf(char ch, String string, int fromIndex)
            throws IllegalActionException {

        int parenIndex = fromIndex;
        int result = -1;

        do {
            int closedParenIndex = parenIndex;

            result = string.indexOf(ch, closedParenIndex);

            parenIndex = string.indexOf('(', closedParenIndex);

            if (parenIndex >= 0) {
                closedParenIndex = _findClosedParen(string, parenIndex);
            }
        } while (parenIndex > 0 && result > parenIndex);

        return result;
    }

    /** Get the code generator helper associated with the given component.
     *  @param component The given component.
     *  @return The code generator helper.
     *  @exception IllegalActionException If the helper class cannot be found.
     */
    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        return _codeGenerator._getHelper(component);
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
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
     * reference instead of using the sink reference directly.
     * This method assumes the given channel is a source (output) channel.
     * @param channel The given source channel.
     * @return The variable reference for the given channel.
     */
    protected String _getTypeConvertReference(Channel channel) {
        return generateName(channel.port) + "_" + channel.channelNumber;
    }

    /** Return the replacement string of the given macro. Subclass
     * of CodeGenerator may overriding this method to extend or support
     * a different set of macros.
     * @param macro The given macro.
     * @param parameter The given parameter to the macro.
     * @return The replacement string of the given macro.
     * @exception IllegalActionException Thrown if the given macro or
     *  parameter is not valid.
     */
    protected String _replaceMacro(String macro, String parameter)
            throws IllegalActionException {
        if (macro.equals("ref")) {
            return getReference(parameter);
        } else if (macro.equals("targetType")) {
            TypedIOPort port = getPort(parameter);
            if (port == null) {
                throw new IllegalActionException(parameter
                        + " is not a port. $type macro takes in a port.");
            }
            return targetType(port.getType());

        } else if (macro.equals("type") || macro.equals("cgType")) {

            TypedIOPort port = getPort(parameter);

            if (port == null) {
                throw new IllegalActionException(parameter
                        + " is not a port. $type macro takes in a port.");
            }
            String type = "";
            if (macro.equals("type")) {
                type = "TYPE_";
            }
            return type + codeGenType(port.getType());

        } else if (macro.equals("val")) {
            return getParameterValue(parameter, _component);

        } else if (macro.equals("size")) {
            return "" + getSize(parameter);

        } else if (macro.equals("actorSymbol")) {
            if (parameter.trim().length() == 0) {
                return generateVariableName(_component);
            } else {
                return generateVariableName(_component) + "_"
                        + processCode(parameter);
            }
        } else if (macro.equals("actorClass")) {
            return _component.getClassName().replace('.', '_') + "_"
                    + processCode(parameter);

            // Handle type function macros.
        } else if (macro.equals("new")) {
            return getNewInvocation(parameter);

        } else if (macro.equals("tokenFunc")) {
            return getFunctionInvocation(parameter, false);

        } else if (macro.equals("typeFunc")) {
            return getFunctionInvocation(parameter, true);

        } else {
            return null;
            // This macro is not handled.
            //throw new IllegalActionException("Macro is not handled.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Generate a variable name for the NamedObj.
     *  @param namedObj The NamedObj to generate variable name for.
     *  @see ptolemy.codegen.kernel.CodeGenerator#generateVariableName(NamedObj)
     *  @return The variable name for the NamedObj.
     */
    public String generateVariableName(NamedObj namedObj) {
        return _codeGenerator.generateVariableName(namedObj);
    }

    /** A hashmap that keeps track of the bufferSizes of each channel
     *  of the actor.
     */
    protected HashMap _bufferSizes = new HashMap();

    /** The code generator that contains this helper class.
     */
    protected CodeGenerator _codeGenerator;

    /**
     * The code stream associated with this helper.
     */
    protected CodeStream _codeStream = new CodeStream(this);

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line charactor so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;
    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    /** The parse tree to use with expressions. */
    protected ParseTreeCodeGenerator _parseTreeCodeGenerator;

    /** A HashMap that contains mapping for ports and their conversion method.
     *  Ports that does not need to be converted do NOT have record in this
     *  map. The codegen kernel record this mapping during the first pass over
     *  the model. This map is used later in the code generation phase.
     */
    protected Hashtable _portConversions = new Hashtable();

    /** A hashmap that keeps track of the read offsets of each input channel of
     *  the actor.
     */
    protected HashMap _readOffsets = new HashMap();

    /** A hashset that keeps track of parameters that are referenced for
     *  the associated actor.
     */
    protected HashSet _referencedParameters = new HashSet();

    /** A hashmap that keeps track of the write offsets of each input channel of
     *  the actor.
     */
    protected HashMap _writeOffsets = new HashMap();

    /** Indent string for indent level 1.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected final static String _INDENT1 = StringUtilities.getIndentPrefix(1);

    /** Indent string for indent level 2.
     *  @see ptolemy.util.StringUtilities#getIndentPrefix(int)
     */
    protected final static String _INDENT2 = StringUtilities.getIndentPrefix(2);

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
    private static int _findClosedParen(String string, int pos)
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

    /** Generate code for a given block.  The comment includes
     *  the portion of the blockName parameter up until the string
     *  "Block".
     *  @param blockName The name of the block, which usually ends
     *  with the string "Block".
     *  @return The generated wrapup code.
     *  @exception IllegalActionException If thrown while appending to the
     *  the block or processing the macros.
     */
    public String _generateBlockByName(String blockName)
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
                    + CodeStream.indent(_codeGenerator.comment(shortBlockName
                            + getComponent().getName())));
        }
        return processCode(_codeStream.toString());

    }

    /** Return the actual CodeStream for this Helper.
     * @return The actual CodeStream.
     * @exception IllegalActionException If thrown by a called method.
     */
    private CodeStream _getActualCodeStream() throws IllegalActionException {
        return _getActualCodeStream(getComponent(), _codeGenerator);
    }

    /** Return the actual CodeStream associated with the given Actor and
     *  CodeGenerator.  Generally, this will come from the Actor's template
     *  file, but EmbeddedCActors get their code from the embeddedCCode parameter.
     * @param namedObj The actor whose code to return.
     * @param codeGenerator The actor's CodeGenerator.
     * @return The actor's actual CodeStream.
     * @exception IllegalActionException If thrown when getting the actor's helper.
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

    /**
     * Get the list of sink channels that the given source channel needs to
     * be type converted to.
     * @param source The given source channel.
     * @return List of sink channels that the given source channel needs to
     * be type converted to.
     */
    private List _getTypeConvertSinkChannels(Channel source) {
        if (_portConversions.containsKey(source)) {
            return ((List) _portConversions.get(source));
        }
        return new ArrayList();
    }

    /**
     * Return true if the given string can be parse as an integer; otherwise,
     * return false.
     * @param numberString The given number string.
     * @return True if the given string can be parse as an integer; otherwise,
     *  return false.
     */
    private boolean _isInteger(String numberString) {
        try {
            Integer.parseInt(numberString);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Mark the given connection between the source and the sink channels
     * as type conversion required.
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated component. */
    private NamedObj _component;

    /**
     * The code block table that stores the code block body (StringBuffer)
     * with the code block name (String) as key.
     */
    private static final String[] _defaultBlocks = { "preinitBlock",
            "initBlock", "fireBlock", "postfireBlock", "wrapupBlock" };

    private boolean printedNullPortWarnings = false;
}
