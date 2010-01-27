/* Base class for named program code generator adapter.

 Copyright (c) 2005-2010 The Regents of the University of California.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
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
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////NamedProgramCodeGeneratorAdapter

//FIXME: Overhaul comments.

/**
* Base class for code generator adapter.
*
* <p>Subclasses should override generateFireCode(),
* generateInitializeCode() generatePostfireCode(),
* generatePreinitializeCode(), and generateWrapupCode() methods by
* appending a corresponding code block.
*
* <p>Subclasses should be sure to properly indent the code by
* either using the code block functionality in methods like
* _generateBlockCode(String) or by calling
* {@link ptolemy.codegen.kernel.CodeStream#indent(String)},
* for example:
* <pre>
*     StringBuffer code = new StringBuffer();
*     code.append(super.generateWrapupCode());
*     code.append("// Local wrapup code");
*     return processCode(CodeStream.indent(code.toString()));
* </pre>
*
* @author Ye Zhou, Gang Zhou, Edward A. Lee, Bert Rodiers Contributors: Christopher Brooks, Teale Fristoe
* @version $Id$
* @since Ptolemy II 8.0
* @Pt.ProposedRating Yellow (eal)
* @Pt.AcceptedRating Yellow (eal)
*/
public class NamedProgramCodeGeneratorAdapter extends ProgramCodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given component.
     *  @param component The associated component.
     */
    public NamedProgramCodeGeneratorAdapter(NamedObj component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Find out each output port that needs to be converted for the
     * actor associated with this adapter. Then, mark these ports along
     * with the sink ports (connection).
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void analyzeTypeConvert() throws IllegalActionException {
        // reset the previous type convert info.
        _portConversions.clear();

        Actor actor = (Actor) _component;

        ArrayList<TypedIOPort> sourcePorts = new ArrayList<TypedIOPort>();
        sourcePorts.addAll(actor.outputPortList());

        if (actor instanceof CompositeActor) {
            sourcePorts.addAll(actor.inputPortList());
        }

        Iterator<TypedIOPort> ports = sourcePorts.iterator();

        // for each output port.
        for (int i = 0; ports.hasNext(); i++) {
            TypedIOPort sourcePort = ports.next();

            // for each channel.
            for (int j = 0; j < sourcePort.getWidth(); j++) {
                Iterator<ProgramCodeGeneratorAdapter.Channel> sinks = getSinkChannels(sourcePort, j)
                        .iterator();

                // for each sink channel connected.
                while (sinks.hasNext()) {
                    ProgramCodeGeneratorAdapter.Channel sink = sinks.next();
                    TypedIOPort sinkPort = (TypedIOPort) sink.port;
                    if (!sourcePort.getType().equals(sinkPort.getType())) {
                        _markTypeConvert(new ProgramCodeGeneratorAdapter.Channel(sourcePort, j), sink);
                    }
                }
            }
        }
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
            ProgramCodeGenerator codeGenerator) throws IOException,
            IllegalActionException {

        // This is static so that ptolemy.actor.lib.jni.CompiledCompositeActor
        // will not depend on ptolemy.codegen.

        long lastModified = 0;

        CodeStream codeStream = null;

        codeStream = TemplateParser._getActualCodeStream(namedObj, codeGenerator);

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

    /** Get the component associated with this adapter.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return (NamedObj) super.getComponent();
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

        String composite = (getComponent() instanceof CompositeActor) ? "Composite Actor: "
                : "";

        code.append(_eol
                + getCodeGenerator().comment("Fire " + composite
                        + NamedProgramCodeGeneratorAdapter.generateName(getComponent())));

        if (getCodeGenerator().inline.getToken() == BooleanToken.TRUE) {
            code.append(_generateFireCode());
        } else if (getCodeGenerator().getContainer().getContainer() != null) {
            // Here we test whether the codegenerator is embedded in another actor or whether it
            // is at the toplevel. In it is embedded we don't need to generateTypeConvertFireCode.
            // Needed for jni and embeddedJava.
            code.append(_generateFireCode());
        } else {
            code.append(_generateFireInvocation(getComponent()) + ";" + _eol);
        }

        try {
            copyFilesToCodeDirectory(getComponent(), getCodeGenerator());
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
        code.append(_eol + "void " + NamedProgramCodeGeneratorAdapter.generateName(getComponent())
                + getCodeGenerator()._getFireFunctionArguments() + " {" + _eol);
        code.append(_generateFireCode());
        code.append("}" + _eol);
        return processCode(code.toString());
    }
    
    /**
     * Generate the type conversion fire code. This method is called by the
     * Director to append necessary fire code to handle type conversion.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateTypeConvertFireCode()
            throws IllegalActionException {
        return generateTypeConvertFireCode(false);
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
        //            ProgramCodeGeneratorAdapter portAdapter = getCodeGenerator().getAdapter(port);
        //            code.append(portAdapter.generatePostfireCode());
        //        }
        return processCode(code.toString());
    }

    /** Generate the prefire code of the associated composite actor.
    *  @return The prefire code of the associated composite actor.
    *  @exception IllegalActionException If illegal macro names are found.
    */
    public String generatePrefireCode() throws IllegalActionException {
        // FIXME: This is to be used in future re-structuring.
        StringBuffer code = new StringBuffer();
        //Actor actor = (Actor) getComponent();
        //for (IOPort port : (List<IOPort>) actor.inputPortList()) {
        //    ProgramCodeGeneratorAdapter portAdapter = getCodeGenerator().getAdapter(port);
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
        Iterator<ProgramCodeGeneratorAdapter.Channel> channels = getTypeConvertChannels().iterator();
        while (channels.hasNext()) {
            ProgramCodeGeneratorAdapter.Channel source = channels.next();

            if (!forComposite && source.port.isOutput() || forComposite
                    && source.port.isInput()) {

                Iterator<ProgramCodeGeneratorAdapter.Channel> sinkChannels = getTypeConvertSinkChannels(
                        source).iterator();

                while (sinkChannels.hasNext()) {
                    ProgramCodeGeneratorAdapter.Channel sink = sinkChannels.next();
                    code.append(_generateTypeConvertStatements(source, sink));
                }
            }
        }
        return code.toString();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        return "";
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        return "";
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
        CodeStream codeStream = _templateParser._getActualCodeStream();
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
        CodeStream codeStream = _templateParser._getActualCodeStream();
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
        CodeStream codeStream = _templateParser._getActualCodeStream();
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
    

    /** Return the name of the object.
     *  @return The name of the object.
     *  @see #setName(String)
     */
    public String getName() {
        return generateName(getComponent());
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
        if (_component instanceof ExplicitChangeContext) {
            set
                    .addAll(((ExplicitChangeContext) _component)
                            .getModifiedVariables());
        }

        Iterator<?> inputPorts = ((Actor) _component).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (inputPort instanceof ParameterPort
                    && inputPort.isOutsideConnected()) {
                set.add(((ParameterPort) inputPort).getParameter());
            }
        }
        return set;
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
        return _templateParser.getParameterValue(name, container);
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
        try {
            ptolemy.actor.Director director = ((Actor) _component).getDirector();
            Director directorAdapter = (Director) getAdapter(director);
            return directorAdapter.getReference(name, isWrite, this);
        } catch (Exception ex) {
            //If we can't find it with the local director, try the executive one. 
            ptolemy.actor.Director director = ((Actor) _component).getExecutiveDirector();
            Director directorAdapter = (Director) getAdapter(director);
            return directorAdapter.getReference(name, isWrite, this);            
        }
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
            sharedCode.add(_templateParser.processCode(codestream.toString()));
        }
        return sharedCode;

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
    public static List<ProgramCodeGeneratorAdapter.Channel> getSinkChannels(IOPort port, int channelNumber)
            throws IllegalActionException {
        List<ProgramCodeGeneratorAdapter.Channel> sinkChannels = new LinkedList<ProgramCodeGeneratorAdapter.Channel>();
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
                        ProgramCodeGeneratorAdapter.Channel sinkChannel = new ProgramCodeGeneratorAdapter.Channel(sinkPort, j);
                        sinkChannels.add(sinkChannel);
                        break;
                    }
                }
            }
        }

        return sinkChannels;
    }

    /**
     * Get the set of channels that need to be type converted.
     * @return Set of channels that need to be type converted.
     */
    public Set<ProgramCodeGeneratorAdapter.Channel> getTypeConvertChannels() {
        return _portConversions.keySet();
    }
    
    /**
     * Generate a variable reference for the given channel. This varaible
     * reference is needed for type conversion. The source adapter get this
     * reference instead of using the sink reference directly.
     * This method assumes the given channel is a source (output) channel.
     * @param channel The given source channel.
     * @return The variable reference for the given channel.
     */
    static public String getTypeConvertReference(ProgramCodeGeneratorAdapter.Channel channel) {
        return generateName(channel.port) + "_" + channel.channelNumber;
    }
    
    /**
     * Get the list of sink channels that the given source channel needs to
     * be type converted to.
     * @param source The given source channel.
     * @return List of sink channels that the given source channel needs to
     * be type converted to.
     */
    public List<ProgramCodeGeneratorAdapter.Channel> getTypeConvertSinkChannels(ProgramCodeGeneratorAdapter.Channel source) {
        if (_portConversions.containsKey(source)) {
            return _portConversions.get(source);
        }
        return new ArrayList<ProgramCodeGeneratorAdapter.Channel>();
    }

    /**
     * Get the corresponding type in the target language
     * from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The target language data type.
     */
    final public String targetType(Type ptType) {
        return getCodeGenerator().targetType(ptType);
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
    protected String _generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        // If the component name starts with a $, then convert "$" to "Dollar" and avoid problems
        // with macro substitution.  See codegen/c/actor/lib/test/auto/RampDollarNames.xml.
        codeStream.appendCodeBlock(_defaultBlocks[2], true); // fireBlock
        return codeStream.toString();
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
    protected String _generateTypeConvertStatement(ProgramCodeGeneratorAdapter.Channel source,
            ProgramCodeGeneratorAdapter.Channel sink, int offset) throws IllegalActionException {
        return _templateParser.generateTypeConvertStatement(source, sink, offset, null);
    }

    /**
     * Generate the invocation of the fire function of
     * the given component.
     * @param component The given component.
     * @return The generated code.
     */
    private static String _generateFireInvocation(NamedObj component) {
        return NamedProgramCodeGeneratorAdapter.generateName(component) + "()";
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
    private String _generateTypeConvertStatements(ProgramCodeGeneratorAdapter.Channel source, ProgramCodeGeneratorAdapter.Channel sink)
            throws IllegalActionException {

        StringBuffer statements = new StringBuffer();

        int rate = Math.max(DFUtilities.getTokenProductionRate(source.port),
                DFUtilities.getTokenConsumptionRate(source.port));

        for (int offset = 0; offset < rate || (offset == 0 && rate == 0); offset++) {
            statements.append(_generateTypeConvertStatement(source,
                    sink, offset));
        }
        return processCode(statements.toString());
    }
    
//    /** Given a port and channel number, create a Channel that sends
//     *  data to the specified port and channel number.
//     *  @param port The port.
//     *  @param channelNumber The channel number of the port.
//     *  @return the source channel.
//     *  @exception IllegalActionException If there is a problem getting
//     *  information about the receivers or constructing the new Channel.
//     *  FIXME: ONLY USED BY PN 
//     */
//    private static ProgramCodeGeneratorAdapter.Channel _getSourceChannel(IOPort port, int channelNumber)
//            throws IllegalActionException {
//        Receiver[][] receivers = null;
//    
//        if (port.isInput()) {
//            receivers = port.getReceivers();
//        } else if (port.isOutput()) {
//            if (port.getContainer() instanceof CompositeActor) {
//                receivers = port.getInsideReceivers();
//            } else {
//                // This port is the source port, so we only
//                // need to make a new Channel. We assume that
//                // the given channelNumber is valid.
//                return new ProgramCodeGeneratorAdapter.Channel(port, channelNumber);
//            }
//        } else {
//            assert false;
//        }
//    
//        List<IOPort> sourcePorts = port.sourcePortList();
//        sourcePorts.addAll(port.insideSourcePortList());
//    
//        for (IOPort sourcePort : sourcePorts) {
//            try {
//                ProgramCodeGeneratorAdapter.Channel source = new ProgramCodeGeneratorAdapter.Channel(sourcePort, sourcePort
//                        .getChannelForReceiver(receivers[channelNumber][0]));
//    
//                if (source != null) {
//                    return source;
//                }
//            } catch (IllegalActionException ex) {
//    
//            }
//        }
//        return null;
//    }
//    
    /**
     * Mark the given connection between the source and the sink channels
     * as type conversion required.
     * @param source The given source channel.
     * @param sink The given input channel.
     */
    private void _markTypeConvert(ProgramCodeGeneratorAdapter.Channel source, ProgramCodeGeneratorAdapter.Channel sink) {
        List<ProgramCodeGeneratorAdapter.Channel> sinks;
        if (_portConversions.containsKey(source)) {
            sinks = _portConversions.get(source);
        } else {
            sinks = new ArrayList<ProgramCodeGeneratorAdapter.Channel>();
            _portConversions.put(source, sinks);
        }
        sinks.add(sink);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A HashMap that contains mapping for ports and their conversion method.
     *  Ports that does not need to be converted do NOT have record in this
     *  map. The codegen kernel record this mapping during the first pass over
     *  the model. This map is used later in the code generation phase.
     */
    private Hashtable<ProgramCodeGeneratorAdapter.Channel, List<ProgramCodeGeneratorAdapter.Channel>> _portConversions = new Hashtable<ProgramCodeGeneratorAdapter.Channel, List<ProgramCodeGeneratorAdapter.Channel>>();

}
