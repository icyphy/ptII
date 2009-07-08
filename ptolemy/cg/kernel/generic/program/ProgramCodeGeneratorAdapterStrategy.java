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
package ptolemy.cg.kernel.generic.program;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////////
//// ProgramCodeGeneratorAdapterStrategy

/**
 * The strategy that determines how code should be generated for a certain ProgramCodeGeneratorAdapter.
 * @author Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (rodiers)
 * @Pt.AcceptedRating Red (rodiers)
 */
// FIXME: Why extend NamedObj? Extend Attribute and store in the actor being adapted?
public class ProgramCodeGeneratorAdapterStrategy extends NamedObj {

    /** Construct the code generator adapter strategy.
     */
    public ProgramCodeGeneratorAdapterStrategy() {
    }

    /** Set the component for which we are generating code.
     *  @param object The associated component.
     *  @see #getComponent
     */
    public void setComponent(NamedObj object) {
        // FIXME: Why is this a namedObj when the analyzeActor()
        // method requires an Actor?
        _object = object;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    // FIXME rodiers: this only used by the PNDirector
    static public boolean checkLocal(boolean forComposite, IOPort port)
            throws IllegalActionException {
        return (port.isInput() && !forComposite && port.isOutsideConnected())
                || (port.isOutput() && forComposite);
    }

    // FIXME rodiers: this only used by the PNDirector
    static public boolean checkRemote(boolean forComposite, IOPort port) {
        return (port.isOutput() && !forComposite)
                || (port.isInput() && forComposite);
    }

    /** Return the code stream.
     * @return The code stream.
     */
    // TODO rodiers: do we want to have this public?
    // BTW is this really necessary? (the code stream is used to set
    // correct in the adapter embedded code actor. However
    final public CodeStream getCodeStream() {
        return _templateParser.getCodeStream();
    }

    /** Generate sanitized name for the given named object. Remove all
     *  underscores to avoid conflicts with systems functions.
     *  @param namedObj The named object for which the name is generated.
     *  @return The sanitized name.
     */
    final public static String generateSimpleName(NamedObj namedObj) {
        String name = StringUtilities.sanitizeName(namedObj.getName());
        return name.replaceAll("\\$", "Dollar");
    }
    
    /**
     * Generate expression that evaluates to a result of equivalent
     * value with the cast type.
     * @param expression The given variable expression.
     * @param castType The given cast type.
     * @param refType The given type of the variable.
     * @return The variable expression that evaluates to a result of
     *  equivalent value with the cast type.
     * @exception IllegalActionException
     */
    public String generateTypeConvertMethod(String expression, String castType,
            String refType) throws IllegalActionException {
        return _templateParser.generateTypeConvertMethod(expression, castType, refType);
    }


    /** Generate a variable name for the NamedObj.
     *  @param namedObj The NamedObj to generate variable name for.
     *  @see ProgramCodeGenerator#generateVariableName(NamedObj)
     *  @return The variable name for the NamedObj.
     */
    public String generateVariableName(NamedObj namedObj) {
        return _codeGenerator.generateVariableName(namedObj);
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(ProgramCodeGenerator)
     */
    public ProgramCodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    /** Get the component associated with this adapter.
     *  @return The associated component.
     *  @see #setComponent
     */
    public NamedObj getComponent() {
        return (NamedObj) _object;
    }

    /** Get the files needed by the code generated from this adapter class.
     *  This base class returns an empty set.
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException Not Thrown in this base class.
     */
    public Set<String> getHeaderFiles() throws IllegalActionException {
        return _templateParser.getHeaderFiles();
    }

    /** Return a set of directories to include for the generated code.
     *  @return A Set containing the contents of the actor's
     *   "includeDirectories" block in its template.
     *  @exception IllegalActionException If thrown when getting or reading
     *   the CodeStream.
     */
    public Set<String> getIncludeDirectories() throws IllegalActionException {
        Set<String> includeDirectories = new HashSet<String>();
        CodeStream codeStream = getTemplateParser()._getActualCodeStream();
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
        CodeStream codeStream = getTemplateParser()._getActualCodeStream();
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
        CodeStream codeStream = getTemplateParser()._getActualCodeStream();
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
            set
                    .addAll(((ExplicitChangeContext) _object)
                            .getModifiedVariables());
        }

        Iterator<?> inputPorts = ((Actor) _object).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort
                    && inputPort.isOutsideConnected()) {
                set.add(((ParameterPort) inputPort).getParameter());
            }
        }
        return set;
    }

    /** Get the object associated with this adapter.
     *  @return The associated object.
     */
    public NamedObj getObject() {
        return (NamedObj) _object;
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
        return _adapter.getReference(name, isWrite);
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
     *  @param isWrite Whether to generate the write or read offset.
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    public String getReference(String name, boolean isWrite)
            throws IllegalActionException {
        ptolemy.actor.Director director = ((Actor) _object).getDirector();
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
        CodeStream codestream = _templateParser.getCodeStream();
        codestream.clear();
        codestream.appendCodeBlocks(".*shared.*");
        if (!codestream.isEmpty()) {
            sharedCode.add(processCode(codestream.toString()));
        }
        return sharedCode;
    }

    /** Given a block name, generate code for that block.
     *  This method is called by actors adapters that have simple blocks
     *  that do not take parameters or have widths.
     *  @param blockName The name of the block.
     *  @return The code for the given block.
     *  @exception IllegalActionException If illegal macro names are
     *  found, or if there is a problem parsing the code block from
     *  the adapter .c file.
     */
    public String generateBlockCode(String blockName)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        return generateBlockCode(blockName, new ArrayList<String>());
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
    public String generateBlockCode(String blockName, List<String> args)
            throws IllegalActionException {
        // We use this method to reduce code duplication for simple blocks.
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        codeStream.appendCodeBlock(blockName, args);
        return processCode(codeStream.toString());
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

        String channelOffset = ProgramCodeGeneratorAdapterStrategy
                .generateName(port);
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
            channelOffset = ProgramCodeGeneratorAdapterStrategy
                    .generateChannelOffset(port, isWrite, channelAndOffset[0]);
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
    
    /** Get the template parser associated with this strategy.
     *  @return The associated template parser.
     */
    final public TemplateParser getTemplateParser() {
        return _templateParser;
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


    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException If illegal macro names are found.
     */
    final public String processCode(String code) throws IllegalActionException {
        return _templateParser.processCode(code);
    }

    /** Set the adapter.
     *  @param adapter The given adapter.
     */
    final public void setAdapter(ProgramCodeGeneratorAdapter adapter) {
        _adapter = adapter;
        _createParser();        
    }

    /** Set the associated code generator.
     *  @param codeGenerator The code generator associated with this class.
     *  @see #getCodeGenerator()
     */
    final public void setCodeGenerator(ProgramCodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
        if (_templateParser != null) {
            _templateParser.setCodeGenerator(codeGenerator);
        }
    }

    public String toString() {
        return getComponent().toString() + "'s Adapter";
    }


    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Create the template parser.
     */
    protected void _createParser() { 
        _templateParser = new TemplateParser(_object, _adapter);
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
        String sourceRef = ((ProgramCodeGeneratorAdapter) _getAdapter(source.port
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
        String sinkRef = ((ProgramCodeGeneratorAdapter) _getAdapter(sink.port
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
            if (_codeGenerator.isPrimitive(sinkType)) {
                result = _codeGenerator.codeGenType(sourceType) + "to" + _codeGenerator.codeGenType(sinkType)
                        + "(" + result + ")";

            } else if (_codeGenerator.isPrimitive(sourceType)) {
                result = "$new(" + _codeGenerator.codeGenType(sourceType) + "(" + result
                        + "))";
            }

            if (sinkType != BaseType.SCALAR && sinkType != BaseType.GENERAL
                    && !_codeGenerator.isPrimitive(sinkType)) {
                if (sinkType instanceof ArrayType) {
                    if (_codeGenerator.isPrimitive(sourceType)) {
                        result = "$new(" + _codeGenerator.codeGenType(sinkType) + "(1, 1, "
                                + result + ", TYPE_" + _codeGenerator.codeGenType(sourceType)
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
                                + _codeGenerator.codeGenType(sinkType)
                                + "::convert("
                                + result
                                + ", /*CGH*/ TYPE_"
                                + _codeGenerator.codeGenType(((ArrayType) sinkType)
                                        .getElementType()) + "))";
                    }

                } else {
                    result = "$typeFunc(TYPE_" + _codeGenerator.codeGenType(sinkType)
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
    protected ProgramCodeGeneratorAdapter _getAdapter(NamedObj component)
            throws IllegalActionException {
        return (ProgramCodeGeneratorAdapter) _codeGenerator
                .getAdapter(component);
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

    /** The ProgramCodeGeneratorAdapter.*/
    protected ProgramCodeGeneratorAdapter _adapter;

    /** The code generator that contains this adapter class.
     */
    protected ProgramCodeGenerator _codeGenerator;

    protected TemplateParser _templateParser;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated object. */
    private Object _object;


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
}
