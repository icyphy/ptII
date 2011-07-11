/*
 * Code generator helper class associated with the PNDirector class.
 *
 * Copyright (c) 2008-2011 The Regents of the University of California. All
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
package ptolemy.codegen.c.domains.pn.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.actor.Director;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////PNDirector

/**
 * Code generator helper associated with the PNDirector class. This director
 * initializes all the actors, then starts a thread for each actor that invokes
 * the fire code for the actor in an infinite loop.
 *
 * FIXME: How to make it possible for executions to be finite?
 *
 * @author Edward A. Lee (based on SDFDirector helper class)
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (eal)
 */
public class PNDirector extends Director {

    /**
     * Construct the code generator helper associated with the given PNDirector.
     *
     * @param pnDirector The associated ptolemy.domains.pn.kernel.PNDirector
     */
    public PNDirector(ptolemy.domains.pn.kernel.PNDirector pnDirector) {
        super(pnDirector);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate the body code that lies between variable declaration and wrapup.
     *
     * @return The generated body code.
     * @exception IllegalActionException If the {@link #_generateFireCode()}
     * method throws the exceptions.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor compositeActor = (CompositeActor) _director
                .getContainer();

        code.append(_codeGenerator.comment("Create a thread for each actor."));
        code.append("pthread_attr_init(&pthread_custom_attr);" + _eol + _eol);

        List<Actor> actorList = compositeActor.deepEntityList();

        for (Actor actor : actorList) {
            // Generate the thread pointer.
            code.append("pthread_t thread_");
            code.append(_getActorThreadLabel(actor));
            code.append(";" + _eol);
        }

        for (Actor actor : actorList) {
            code.append("pthread_create(");
            code.append("&thread_" + _getActorThreadLabel(actor));
            code.append(", &pthread_custom_attr, ");
            code.append(_getActorThreadLabel(actor));
            code.append(", NULL);" + _eol);
        }

        for (Actor actor : actorList) {
            code.append("pthread_join(");
            code.append("thread_" + _getActorThreadLabel(actor));
            code.append(", NULL);" + _eol);
        }
        return code.toString();
    }

    /**
     * Get the files needed by the code generated from this helper class. This
     * base class returns an empty set.
     *
     * @return A set of strings that are header files needed by the code
     * generated from this helper class.
     * @exception IllegalActionException If something goes wrong.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("<pthread.h>");
        // files.add("<thread.h>");
        return files;
    }

    /**
     * Return the libraries specified in the "libraries" blocks in the templates
     * of the actors included in this CompositeActor.
     *
     * @return A Set of libraries.
     * @exception IllegalActionException If thrown when gathering libraries.
     */
    public Set getLibraries() throws IllegalActionException {
        Set libraries = new LinkedHashSet();
        libraries.add("pthread");
        // libraries.add("thread");
        return libraries;
    }

    /**
     * Return the code for getting data from the specific port channel.
     * @param port The specified port.
     * @param channel The specified channel.
     * @return the code for getting data from the specific port channel,
     * in this case, return the empty string.
     * @exception IllegalActionException Not thrown here.
     */
    public String generateCodeForGet(IOPort port, int channel)
            throws IllegalActionException {

        // getReadOffset()
        // incrementReadOffset()
        return "";
    }

    /**
     * Return the code for sending data from the specific port channel.
     * @param port The specified port.
     * @param channel The specified channel.
     * @return the code for sending data from the specific port channel,
     * in this case, return the empty string.
     * @exception IllegalActionException Not thrown here.
     */
    public String generateCodeForSend(IOPort port, int channel)
            throws IllegalActionException {
        return "";
    }

    /**
     * Return the generated label for the director header.
     * @return The generated label for the director header.
     */
    public String generateDirectorHeader() {
        return CodeGeneratorHelper.generateName(_director) + "_controlBlock";
    }

    /**
     * Generate the initialize code for the associated PN director.
     * @return The generated initialize code.
     * @exception IllegalActionException If the helper associated with the
     * director throws it while generating initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_codeGenerator
                .comment("Initialization code of the PNDirector."));

        // Don't generate the code to initialize all the actors.
        // code.append(super.generateInitializeCode());

        List args = new LinkedList();
        args.add(generateDirectorHeader());
        args.add(_generateActorNameFileForDebugging());

        // Initialize the director head variable.
        code.append(_codeStream.getCodeBlock("initBlock", args));

        args.remove(1);

        // Initialize each buffer variables.
        for (String bufferVariable : _buffers) {
            args.set(0, bufferVariable);
            code.append(_codeStream.getCodeBlock("initBuffer", args));
        }
        return code.toString();
    }

    /**
     * Return the main loop code for the associated PN director.
     * @return The generated main loop code.
     * @exception IllegalActionException If the helper associated with the
     * director throws it while generating main loop code.
     */
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(((CodeGeneratorHelper) _getHelper(_director.getContainer()))
                .generateFireCode());

        return code.toString();
    }

    /**
     * Return the generated header label for the specified port channel.
     * @param port The specified port.
     * @param i The specified channel number.
     * @return The generated header label for the specified port channel.
     */
    public static String generatePortHeader(IOPort port, int i) {
        return CodeGeneratorHelper.generateName(port) + "_" + i + "_pnHeader";
    }

    /**
     * Generate the postfire code for the associated PN director.
     * @return The generated postfire code.
     * @exception IllegalActionException If the helper associated with the
     * director throws it while generating postfire code.
     */
    public String generatePostfireCode() throws IllegalActionException {
        return "";
    }

    /**
     * Generate the preinitialize code for the associated PN director.
     *
     * @return The generated preinitialize code.
     * @exception IllegalActionException If the helper associated with an actor
     * throws it while generating preinitialize code for the actor.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer bufferCode = new StringBuffer();

        _buffers.clear();

        List actorList = ((CompositeEntity) _director.getContainer())
                .deepEntityList();

        Iterator actors = actorList.iterator();
        while (actors.hasNext()) {
            Entity actor = (Entity) actors.next();
            Iterator ports = actor.portList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                bufferCode.append(_createDynamicOffsetVariables(port));
            }
        }
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());

        List args = new LinkedList();
        args.add(generateDirectorHeader());

        args.add(((CompositeActor) _director.getContainer()).deepEntityList()
                .size());

        args.add(_buffers.size());
        args.add(_generateActorNameFileForDebugging());
        code.append(_codeStream.getCodeBlock("preinitBlock", args));

        code.append(bufferCode);

        _generateThreadFunctionCode(code);

        return code.toString();
    }

    /**
     * Generate the transfer output code for the specified port.
     * @param port The specified port.
     * @param code The given buffer to generate code to.
     * @exception IllegalActionException Not thrown here.
     */
    public void generateTransferOutputsCode(IOPort port, StringBuffer code)
            throws IllegalActionException {
        code.append(_codeGenerator.comment("PNDirector: "
                + "Transfer tokens to the outside."));
    }

    /**
     * Generate code for transferring enough tokens to complete an internal
     * iteration.
     *
     * @param inputPort The port to transfer tokens.
     * @param code The string buffer that the generated code is appended to.
     * @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(_codeGenerator.comment("PNDirector: "
                + "Transfer tokens to the inside."));

        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        ptolemy.codegen.c.actor.TypedCompositeActor compositeActorHelper = (ptolemy.codegen.c.actor.TypedCompositeActor) _getHelper(container);

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {
                String name = generateSimpleName(inputPort);

                if (inputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                for (int k = 0; k < rate; k++) {
                    code.append(compositeActorHelper.getReference("@" + name
                            + "," + k));
                    code.append(" = " + _eol);
                    code.append(compositeActorHelper.getReference(name + ","
                            + k));
                    code.append(";" + _eol);
                }
            }
        }
        // Generate the type conversion code before fire code.
        code.append(compositeActorHelper.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }

    /**
     * Generate variable initialization for the referenced parameters.
     *
     * @return code The generated code.
     * @exception IllegalActionException If the helper class for the model
     * director cannot be found.
     */
    public String generateVariableInitialization()
            throws IllegalActionException {
        return "";
    }

    /**
     * Generate the wrapup code for the associated PN director.
     *
     * @return The generated preinitialize code.
     * @exception IllegalActionException If the helper associated with an actor
     * throws it while generating preinitialize code for the actor.
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Note: We don't need to call the super class method nor
        // append the wrapup code for each of the containing actor.
        // Instead, the actor wrapup code resides in the actor
        // thread function code.

        List args = new LinkedList();
        args.add(generateDirectorHeader());
        args.add(_generateActorNameFileForDebugging());

        code.append(_codeStream.getCodeBlock("wrapupBlock", args));

        return code.toString();
    }

    /**
     * Return the size of the generated buffer for the specified port channel.
     * This returns the value of "initialQueueCapacity" parameter of the
     * PNDirector
     * @param port The specified port.
     * @param channelNumber The specified channel number.
     * @return The size of the generated buffer.
     * @exception IllegalActionException If an error occurs when getting the
     * value from the parameter.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        IntToken sizeToken = (IntToken) ((ptolemy.domains.pn.kernel.PNDirector) _director).initialQueueCapacity
                .getToken();

        // FIXME: Force buffer size to be at least 2.
        // We need to handle size 1 as special case.
        return Math.max(sizeToken.intValue(), 2);
    }

    /**
     * Generate the shared code for the associated PN director.
     * @return The generated shared code.
     * @exception IllegalActionException If the helper associated with the
     * director throws it while generating shared code.
     */
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();
        // sharedCode.add(_codeStream.getCodeBlock("sharedBlock"));
        return sharedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Create offset variables for the channels of the given port. The offset
     * variables are generated unconditionally.
     * @param port The port whose offset variables are generated.
     * @return Code that declares the read and write offset variables.
     * @exception IllegalActionException If getting the rate or reading
     * parameters throws it.
     */
    protected String _createDynamicOffsetVariables(IOPort port)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_eol
                + _codeGenerator
                        .comment("PN Director's offset variable declarations."));

        int width;
        if (port.isInput()) {
            width = port.getWidth();
        } else {
            width = 0;// port.getWidthInside();
        }

        if (width != 0) {

            // Declare the buffer header struct.
            List args = new LinkedList();
            args.add(""); // buffer header name
            args.add(""); // director header name
            args.add(""); // capacity
            args.add(""); // index

            // FIXME: Do some filtering, only generate needed buffer.
            for (int i = 0; i < width; i++) {
                args.set(0, generatePortHeader(port, i));
                args.set(1, generateDirectorHeader());
                args.set(2, getBufferSize(port, i));
                args.set(3, _buffers.size());

                code.append(_codeStream.getCodeBlock("declareBufferHeader",
                        args));

                // Record all the buffer instantiated.
                _buffers.add(generatePortHeader(port, i));
            }
        }
        return code.toString();
    }

    /**
     * Generate the notTerminate flag variable for the associated PN director.
     * Generating notTerminate instead of terminate saves the negation in
     * checking the flag (e.g. "while (!terminate) ...").
     *
     * @return The variable label of the notTerminate flag.
     */
    protected String _generateNotTerminateFlag() {
        return "true";// "director_controlBlock.controlBlock";
    }

    /**
     * @param code The given code buffer.
     * @exception IllegalActionException
     */
    private void _generateThreadFunctionCode(StringBuffer code)
            throws IllegalActionException {

        List actorList = ((CompositeActor) _director.getContainer())
                .deepEntityList();

        // Generate the function for each actor thread.
        for (Actor actor : (List<Actor>) actorList) {
            StringBuffer functionCode = new StringBuffer();

            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);

            // This is necessary for non-inline mode. The actor fire
            // function code generated after this, so we need to generate
            // a declaration for the function.
            if (_codeGenerator.inline.getToken() == BooleanToken.FALSE) {
                code.append(_eol + "void " + generateName((NamedObj) actor)
                        + "();");
            }

            code.append(_eol + "void* " + _getActorThreadLabel(actor)
                    + "(void* arg) {" + _eol);

            // Generate debug code for printing the thread ID and actor name.
            List args = new LinkedList();
            args.add(_generateActorNameFileForDebugging());
            args.add(actor.getDisplayName());
            code.append(_codeStream.getCodeBlock("printThreadName", args));

            // mainLoop

            // Check if the actor is an opague CompositeActor.
            // The actor is guaranteed to be opague from calling
            // deepEntityList(),
            // so all we need to check whether or not it is a CompositeActor.
            if (actor instanceof CompositeActor) {
                Director directorHelper = (Director) _getHelper(actor
                        .getDirector());

                // If so, it should contain a different Director.
                assert directorHelper != this;

                functionCode.append(directorHelper.generateMainLoop());

                functionCode.append("$incrementReadBlockingThreads(&"
                        + generateDirectorHeader() + ");" + _eol);
            } else {

                StringBuffer pnPostfireCode = new StringBuffer();

                String loopCode = "while (true) {" + _eol;

                // Generate a for loop if there is a firing count limit.
                if (actor instanceof LimitedFiringSource) {
                    int firingCount = ((IntToken) ((LimitedFiringSource) actor).firingCountLimit
                            .getToken()).intValue();

                    if (firingCount != 0) {
                        loopCode = "int i = 0;" + _eol + "for (; i < "
                                + firingCount + "; i++) {" + _eol;
                    }
                    pnPostfireCode.append(_eol);
                }

                functionCode.append(loopCode);

                functionCode.append(helper.generateFireCode());

                // FIXME: Doesn't look like the following comment is correct.
                // If not inline, generateFireCode() would be a call
                // to the fire function which already includes the
                // type conversion code.
                // if (inline) {
                //      functionCode.append(helper.generateTypeConvertFireCode());
                // }

                functionCode.append(helper.generatePostfireCode());

                boolean forComposite = actor instanceof CompositeActor;

                // Increment port offset.
                for (IOPort port : (List<IOPort>) ((Entity) actor).portList()) {
                    // Determine the amount to increment.
                    int rate = 0;
                    try {
                        rate = DFUtilities.getRate(port);
                    } catch (NullPointerException ex) {
                        // int i = 0;
                    }
                    PortCodeGenerator portHelper = (PortCodeGenerator) _getHelper(port);
                    CodeGeneratorHelper portCGHelper = (CodeGeneratorHelper) portHelper;

                    if (portCGHelper.checkRemote(forComposite, port)) {
                        pnPostfireCode.append(portHelper
                                .updateConnectedPortsOffset(rate, _director));
                    }
                    if (port.isInput()) {
                        pnPostfireCode.append(portHelper.updateOffset(rate,
                                _director));
                    }
                }

                // Code for incrementing buffer offsets.
                functionCode.append(pnPostfireCode.toString());

                functionCode.append("}" + _eol);
                functionCode.append("$incrementReadBlockingThreads(&"
                        + generateDirectorHeader() + ");" + _eol);
            }

            // wrapup
            functionCode.append(helper.generateWrapupCode());

            functionCode.append("return NULL;" + _eol);
            functionCode.append("}" + _eol);

            // init
            // This needs to be called last because all references
            // need to be collected before generating their initialization.
            String initializeCode = helper.generateInitializeCode();

            String variableInitializeCode = helper
                    .generateVariableInitialization();

            code.append(variableInitializeCode);
            code.append(initializeCode);

            code.append(functionCode);
        }
    }

    private String _generateActorNameFileForDebugging() {
        return CodeGeneratorHelper.generateName(_director) + "_nameMapFile";
    }

    /**
     * Generate the thread function name for a given actor.
     *
     * @param actor The given actor.
     * @return A unique label for the actor thread function.
     */
    private String _getActorThreadLabel(Actor actor) {
        return CodeGeneratorHelper.generateName((NamedObj) actor)
                + "_ThreadFunction";
    }

    private final HashSet<String> _buffers = new HashSet<String>();

}
