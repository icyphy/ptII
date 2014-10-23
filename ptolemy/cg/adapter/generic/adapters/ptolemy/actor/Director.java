/* Code generator adapter class associated with the Director class.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.adapters.ptolemy.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ptides.kernel.PtidesPlatform;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// Director

/**
 Code generator adapter associated with the ptolemy.actor.Director class.
 This class is also associated with a code generator.

 @see GenericCodeGenerator
 @author Ye Zhou, Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (zhouye)
 @Pt.AcceptedRating Yellow (zhouye)
 */
public class Director extends NamedProgramCodeGeneratorAdapter {

    // FIXME: need documentation on how subclasses should extend this class.

    /** Construct the code generator adapter associated with the given director.
     *  Note before calling the generate*() methods, you must also call
     *  setCodeGenerator(GenericCodeGenerator).
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        super(director);
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                Public Methods                           ////

    /** Generate the send code for Port port.
     *  @param port The port for which to generate send code.
     *  @param channel The channel for which the send code is generated.
     *  @param dataToken The token to be sent
     *  @return The code that sends the dataToken on the channel.
     */
    public String generateCodeForSend(IOPort port, int channel, String dataToken) {
        return "";
    }

    /** Generate the get code for Port port.
     *  @param port The port for which to generate get code.
     *  @param channel The channel for which the get code is generated.
     *  @return The code that gets data from the channel.
     */
    public String generateCodeForGet(IOPort port, int channel) {
        return "";
    }

    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the adapters for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @return The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating fire code for the actor.
     */
    @Override
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment("The firing of the director."));

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            code.append(adapter.generateFireCode());
        }
        return code.toString();
    }

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. Each actor's firing code is in a
     *  function with the same name as that of the actor.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter actorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            code.append(actorAdapter.generateFireFunctionCode());
        }
        return code.toString();
    }

    /** Generate the constructor code for the specified director.
     * This is only used in the C Code generation.
     *
     * @return If the container is an instance of the PtidesPlatform,
     * then return constructor code.  Otherwise return the empty
     * string.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public String generateConstructorCode() throws IllegalActionException {
        // FIXME : This should be in a proper adapter but since the PtidesPlatformDirector class
        // is private, we did it here which works but is not nice ...
        if (getComponent().getContainer() instanceof PtidesPlatform) {
            StringBuffer result = new StringBuffer();
            CompositeActor container = (CompositeActor) _director
                    .getContainer();
            String sanitizedContainerName = CodeGeneratorAdapter
                    .generateName(container);
            String sanitizedDirectorName = CodeGeneratorAdapter
                    .generateName(_director);
            result.append(_eol + sanitizedDirectorName + "->container = "
                    + sanitizedContainerName + ";");
            result.append(_eol + sanitizedDirectorName + "->_startTime = "
                    + _director.getModelStartTime() + ";");
            result.append(_eol + sanitizedDirectorName + "->_stopTime = "
                    + _director.getModelStopTime() + ";");
            result.append(_eol + sanitizedDirectorName
                    + "->localClock->container = (struct Director*)"
                    + sanitizedDirectorName + ";");

            List<?> containedActors = container.deepEntityList();
            Iterator<?> actors = containedActors.iterator();
            // First loop to create the struct IOPort
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                String sanitizedActorName = CodeGeneratorAdapter
                        .generateName((NamedObj) actor);
                Iterator<?> ports = actor.inputPortList().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    if (!port.isOutsideConnected()) {
                        continue;
                    }
                    result.append(_eol + "struct IOPort* " + sanitizedActorName
                            + "_" + port.getName() + " = (struct IOPort*)"
                            + sanitizedActorName + "_get_" + port.getName()
                            + "();");
                }
                ports = actor.outputPortList().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    if (!port.isOutsideConnected()) {
                        continue;
                    }
                    result.append(_eol + "struct IOPort* " + sanitizedActorName
                            + "_" + port.getName() + " = (struct IOPort*)"
                            + sanitizedActorName + "_get_" + port.getName()
                            + "();");
                }
            }
            // Second loop to link the ports and put the depths
            actors = containedActors.iterator();
            while (actors.hasNext()) {
                NamedObj actor = (NamedObj) actors.next();
                String sanitizedActorName = CodeGeneratorAdapter
                        .generateName(actor);
                Iterator<?> ports = ((Actor) actor).outputPortList().iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    if (!port.isOutsideConnected()) {
                        continue;
                    }

                    int i = 0;
                    int j = 0;
                    Receiver[][] receiverss = port.getRemoteReceivers();
                    for (i = 0; i < receiverss.length; i++) {
                        if (receiverss[i] == null) {
                            continue;
                        }
                        for (j = 0; j < receiverss[i].length; j++) {
                            Receiver receiver = receiverss[i][j];
                            IOPort farPort = receiver.getContainer();
                            NamedObj farActor = farPort.getContainer();
                            String sanitizedFarActorName = CodeGeneratorAdapter
                                    .generateName(farActor);
                            String farPortName;
                            if (farActor == container) {
                                farPortName = farPort.getName()
                                        + "->_localInsideReceivers, ";
                            } else {
                                farPortName = sanitizedFarActorName + "_"
                                        + farPort.getName()
                                        + "->_localReceivers, ";
                            }

                            int foo = 0;
                            int bar = 0;
                            Receiver[][] farReceiverss;
                            if (farPort.isOutput() && farPort.isOpaque()) {
                                farReceiverss = farPort.getInsideReceivers();
                            } else {
                                farReceiverss = farPort.getReceivers();
                            }
                            loops: for (foo = 0; foo < farReceiverss.length; foo++) {
                                for (bar = 0; bar < farReceiverss[foo].length; bar++) {
                                    if (farReceiverss[foo][bar]
                                            .equals(receiver)) {
                                        break loops;
                                    }
                                }
                            }

                            if (foo == farReceiverss.length) {
                                throw new IllegalActionException(container,
                                        "Receiver not found in port : "
                                                + port.getFullName()
                                                + "in actor : "
                                                + sanitizedActorName);
                            }

                            result.append(_eol + "pblListAdd(pblListGet("
                                    + sanitizedActorName + "_" + port.getName()
                                    + "->_farReceivers, " + i + ")"
                                    + ", pblListGet(pblListGet(" + farPortName
                                    + foo + "), " + bar + "));");
                        }
                    }
                }
            }
            // In the case of a CompositeActor, we have to initialize the insideReceivers
            Iterator<?> ports = ((Actor) container).inputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isInsideConnected()) {
                    continue;
                }
                int i = 0;
                int j = 0;
                Receiver[][] receiverss = port.deepGetReceivers();
                for (i = 0; i < receiverss.length; i++) {
                    if (receiverss[i] == null) {
                        continue;
                    }
                    for (j = 0; j < receiverss[i].length; j++) {
                        Receiver receiver = receiverss[i][j];
                        IOPort farPort = receiver.getContainer();
                        NamedObj farActor = farPort.getContainer();
                        String sanitizedFarActorName = CodeGeneratorAdapter
                                .generateName(farActor);
                        String farPortName = sanitizedFarActorName + "_"
                                + farPort.getName() + "->_localReceivers, ";

                        int foo = 0;
                        int bar = 0;
                        Receiver[][] farReceiverss;
                        farReceiverss = farPort.getReceivers();
                        loops: for (foo = 0; foo < farReceiverss.length; foo++) {
                            for (bar = 0; bar < farReceiverss[foo].length; bar++) {
                                if (farReceiverss[foo][bar].equals(receiver)) {
                                    break loops;
                                }
                            }
                        }

                        if (foo == farReceiverss.length) {
                            throw new IllegalActionException(container,
                                    "Receiver not found in port : "
                                            + port.getFullName()
                                            + " in actor : "
                                            + sanitizedContainerName);
                        }

                        result.append(_eol + "pblListAdd(pblListGet("
                                + port.getName() + "->_insideReceivers, " + i
                                + ")" + ", pblListGet(pblListGet("
                                + farPortName + foo + "), " + bar + "));");
                    }
                }
            }

            return result.toString();
        }
        return "";
    }

    /** Generate the initialize code for this director.
     *  The initialize code for the director is generated by appending the
     *  initialize code for each actor.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment(1,
                "The initialization of the director."));

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            // Initialize code for the actor.
            code.append(adapterObject.generateInitializeCode());
        }
        return code.toString();
    }

    /** Generate the postfire code of the associated composite actor.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating postfire code for the actor
     *   or while creating buffer size and offset map.
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(getCodeGenerator().comment(0,
                "The postfire of the director."));

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            code.append(adapterObject.generatePostfireCode());
        }

        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        boolean addedDirectorComment = false;
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);

            // If a adapter generates preinitialization code, then
            // print a comment
            String adapterObjectPreinitializationCode = adapterObject
                    .generatePreinitializeCode();

            if (!addedDirectorComment
                    && ProgramCodeGenerator
                    .containsCode(adapterObjectPreinitializationCode)) {
                addedDirectorComment = true;
                code.append(getCodeGenerator().comment(0,
                        "The preinitialization of the director."));
            }
            code.append(adapterObjectPreinitializationCode);
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
     *  @return a string for the preinitialization method body.  In
     *  this base class, return the empty string.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        boolean addedDirectorComment = false;
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);

            // If a adapter generates preinitialization code, then
            // print a comment
            String adapterObjectPreinitializationCode = adapterObject
                    .generatePreinitializeMethodBodyCode();

            if (!addedDirectorComment
                    && ProgramCodeGenerator
                    .containsCode(adapterObjectPreinitializationCode)) {
                addedDirectorComment = true;
                code.append(getCodeGenerator().comment(0,
                        "The preinitialization method of the director."));
            }
            code.append(adapterObjectPreinitializationCode);
        }

        return code.toString();
    }

    /** Generate mode transition code. It delegates to the adapters of
     *  actors under the control of this director. The mode transition
     *  code generated in this method is executed after each global
     *  iteration, e.g., in HDF model.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If an actor adapter throws it
     *   while generating mode transition code.
     */
    @Override
    public void generateModeTransitionCode(StringBuffer code)
            throws IllegalActionException {
        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            adapterObject.generateModeTransitionCode(code);
        }
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.  The reference is looked for in the director
     *  not in the executive director (the director of the container).
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        generateTransferInputsCode(inputPort, code, false);
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @param executive If true, then look for the reference in the
     *  executive director (the director of the container).  The
     *  CaseDirector calls this with executive == true, most (all?)
     *  other Directors call this with executive == false.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code,
            boolean executive) throws IllegalActionException {

        code.append(CodeStream.indent(getCodeGenerator().comment(
                "Transfer tokens to the inside")));

        NamedProgramCodeGeneratorAdapter _compositeActorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(_director.getContainer());

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {
                String name = inputPort.getName();

                if (inputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                //FIXME: What should be the director? Executive or local?
                code.append(CodeStream.indent(_compositeActorAdapter
                        .getReference("@" + name, executive)));
                code.append(" = ");
                code.append(_compositeActorAdapter
                        .getReference(name, executive));
                code.append(";" + _eol);
            }
        }

        // Generate the type conversion code before fire code.
        code.append(_compositeActorAdapter.generateTypeConvertFireCode(true));
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.  The reference is looked for in the director
     *  not in the executive director (the director of the container).
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        generateTransferOutputsCode(outputPort, code, false);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @param executive If true, then look for the reference in the
     *  executive director (the director of the container).  The
     *  CaseDirector calls this with executive == true, most (all?)
     *  other Directors call this with executive == false.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort,
            StringBuffer code, boolean executive) throws IllegalActionException {
        code.append(getCodeGenerator().comment(
                "Director: Transfer tokens to the outside."));

        NamedProgramCodeGeneratorAdapter _compositeActorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(_director.getContainer());

        for (int i = 0; i < outputPort.getWidthInside(); i++) {
            if (i < outputPort.getWidth()) {
                String name = outputPort.getName();

                if (outputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                code.append(_compositeActorAdapter.getReference(name, false,
                        executive) + " = ");
                code.append(_compositeActorAdapter.getReference("@" + name,
                        false, executive));
                code.append(";" + _eol);
            }
        }
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor container = (CompositeActor) _director.getContainer();
        GenericCodeGenerator codeGenerator = getCodeGenerator();
        {
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(container);
            code.append(_generateVariableDeclaration(adapterObject));
        }

        Iterator<?> actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(actor);
            if (actor instanceof CompositeActor
                    && ((CompositeActor) actor).isOpaque()) {
                code.append(adapterObject.generateVariableDeclaration());
            } else {
                code.append(_generateVariableDeclaration(adapterObject));
            }
        }

        return code.toString();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  This method returns the declarations for the param actor only.
     *  @param adapter The adapter for which to generate variable declarations.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration(
            NamedProgramCodeGeneratorAdapter adapter)
                    throws IllegalActionException {
        return _generateVariableDeclaration(adapter);
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = (CompositeActor) _director.getContainer();
        GenericCodeGenerator codeGenerator = getCodeGenerator();
        {
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(container);
            code.append(_generateVariableInitialization(adapterObject));
        }

        Iterator<?> actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(actor);
            code.append(adapterObject.generateVariableInitialization());
            code.append(_generateVariableInitialization(adapterObject));
        }

        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  for the adapter given in argument
     *  @param adapter The adapter for which to generate variable initialization.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableInitialization(
            NamedProgramCodeGeneratorAdapter adapter)
                    throws IllegalActionException {
        return _generateVariableInitialization(adapter);
    }

    /** Generate the wrapup code of the director associated with this adapter
     *  class. For this base class, this method just generate the wrapup code
     *  for each actor.
     *  @return The generated wrapup code.
     *  @exception IllegalActionException If the adapter class for each actor
     *   cannot be found, or if an error occurs while the adapter generate the
     *   wrapup code.
     */
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(getCodeGenerator()
                .comment(1, "The wrapup of the director."));

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            code.append(adapterObject.generateWrapupCode());
        }

        return code.toString();
    }

    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *
     *  @param name The name of the parameter or port
     *  @param isWrite Whether to generate the write or read offset.
     *  @param target The ProgramCodeGeneratorAdapter for which code
     *  needs to be generated.
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    public String getReference(String name, boolean isWrite,
            NamedProgramCodeGeneratorAdapter target)
                    throws IllegalActionException {
        // FIXME: need documentation on the input string format.
        return "";
    }

    /** Return the director associated with this class.
     *  @return The director associated with this class.
     */
    @Override
    public NamedObj getComponent() {
        return _director;
    }

    /** Return an empty HashSet.
     *  @return An empty HashSet.
     *  @exception IllegalActionException Not thrown in this method.
     */
    @Override
    public Set<String> getIncludeDirectories() throws IllegalActionException {
        return new HashSet<String>();
    }

    /** Return an empty HashSet.
     *  @return An empty HashSet.
     *  @exception IllegalActionException Not thrown in this method.
     */
    @Override
    public Set<String> getLibraries() throws IllegalActionException {
        return new HashSet<String>();
    }

    /** Return an empty HashSet.
     *  @return An empty HashSet.
     *  @exception IllegalActionException Not thrown in this method.
     */
    @Override
    public Set<String> getLibraryDirectories() throws IllegalActionException {
        return new HashSet<String>();
    }

    /** Return a set of parameters that will be modified during the execution
     *  of the model. The director gets those variables if it implements
     *  ExplicitChangeContext interface.
     *
     *  @return a set of parameters that will be modified.
     *  @exception IllegalActionException If the adapter associated with an actor
     *   or director throws it while getting modified variables.
     */
    @Override
    public Set<Parameter> getModifiedVariables() throws IllegalActionException {
        Set<Parameter> set = new HashSet<Parameter>();

        if (_director instanceof ExplicitChangeContext) {
            set.addAll(((ExplicitChangeContext) _director)
                    .getModifiedVariables());
        }

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            set.addAll(adapterObject.getModifiedVariables());
        }

        return set;
    }

    /** Gets the parameter.
     *  @param target An adapter
     *  @param attribute The attribute
     *  @param channelAndOffset The given channel and offset.
     *  @return code for the parameter
     *  @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    public String getParameter(NamedProgramCodeGeneratorAdapter target,
            Attribute attribute, String[] channelAndOffset)
                    throws IllegalActionException {
        return _getParameter(target, attribute, channelAndOffset);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the minimum number of power of two that is greater than or
     *  equal to the given integer.
     *  @param value The given integer.
     *  @return the minimum number of power of two that is greater than or
     *   equal to the given integer.
     *  @exception IllegalActionException If the given integer is not positive.
     */
    protected int _ceilToPowerOfTwo(int value) throws IllegalActionException {
        if (value < 1) {
            throw new IllegalActionException(getComponent(),
                    "The given integer must be a positive integer.");
        }

        int powerOfTwo = 1;

        while (value > powerOfTwo) {
            powerOfTwo <<= 1;
        }

        return powerOfTwo;
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @param target The ProgramCodeGeneratorAdapter for which code
     *  needs to be generated.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    protected String _generateVariableDeclaration(
            NamedProgramCodeGeneratorAdapter target)
                    throws IllegalActionException {
        return "";
    }

    /** Generate variable initialization for the referenced parameters.
     *  @param target The ProgramCodeGeneratorAdapter for which code
     *  needs to be generated.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    protected String _generateVariableInitialization(
            NamedProgramCodeGeneratorAdapter target)
                    throws IllegalActionException {
        return "";
    }

    /**
     * Return an unique label for the given attribute referenced
     * by the given adapter. Subclass should override this method
     * to generate the desire label according to the given parameters.
     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     * @param attribute The given attribute.
     * @param channelAndOffset The given channel and offset.
     * @return an unique label for the given attribute.
     * @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    protected String _getParameter(NamedProgramCodeGeneratorAdapter target,
            Attribute attribute, String[] channelAndOffset)
                    throws IllegalActionException {
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The associated director.
     */
    protected ptolemy.actor.Director _director;
}
