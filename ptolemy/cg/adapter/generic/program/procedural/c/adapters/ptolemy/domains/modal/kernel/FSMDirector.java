/* Code generator adapter class associated with the FSMDirector class.

 Copyright (c)2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////FSMDirector

/**
Code generator adapter associated with the FSMDirector class. This class
is also associated with a code generator.

@author William Lucas
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (sssf)
@Pt.AcceptedRating Red (sssf)
 */
public class FSMDirector
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel.FSMDirector {

    /** Construct the code generator helper associated
     *  with the given modal controller.
     *  @param component The associated component.
     */
    public FSMDirector(ptolemy.domains.modal.kernel.FSMDirector component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Public methods                    ////

    /** Generate the constructor code for the specified director.
     * In this class we initialize the director with its internal
     * parameters and fields as well as with the depths of the actors
     *
     * @return The generated constructor code
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateConstructorCode() throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        CompositeActor container = (CompositeActor) _director.getContainer();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);
        ptolemy.domains.modal.kernel.FSMDirector director = (ptolemy.domains.modal.kernel.FSMDirector) _director;

        result.append(_eol + getSanitizedDirectorName() + "->container = "
                + sanitizedContainerName + ";");
        result.append(_eol + _sanitizedDirectorName + "->_startTime = "
                + director.getModelStartTime() + ";");
        result.append(_eol + _sanitizedDirectorName + "->_stopTime = "
                + director.getModelStopTime() + ";");
        result.append(_eol + _sanitizedDirectorName
                + "->localClock->container = (struct Director*)"
                + _sanitizedDirectorName + ";");
        result.append(_eol + _sanitizedDirectorName + "->makeTransitions = "
                + sanitizedContainerName + "_makeTransitions;");
        result.append(_eol + _sanitizedDirectorName
                + "->transferModalInputs = " + sanitizedContainerName
                + "_transferModalInputs;");
        result.append(_eol + _sanitizedDirectorName
                + "->transferModalOutputs = " + sanitizedContainerName
                + "_transferModalOutputs;");

        List<?> containedActors = container.deepEntityList();
        Iterator<?> actors = containedActors.iterator();
        // First loop to create the struct IOPort
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName((NamedObj) actor);
            if (sanitizedActorName.contains("Controller")) {
                continue;
            }
            Iterator<?> ports = actor.inputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isOutsideConnected()) {
                    continue;
                }
                result.append(_eol + "struct IOPort* " + sanitizedActorName
                        + "_" + port.getName() + " = (struct IOPort*)"
                        + sanitizedActorName + "_get_" + port.getName() + "();");
            }
            ports = actor.outputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isOutsideConnected()) {
                    continue;
                }
                result.append(_eol + "struct IOPort* " + sanitizedActorName
                        + "_" + port.getName() + " = (struct IOPort*)"
                        + sanitizedActorName + "_get_" + port.getName() + "();");
            }
        }
        // Second loop to link the ports and put the depths
        actors = containedActors.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            if (sanitizedActorName.contains("Controller")) {
                continue;
            }
            Iterator<?> ports = ((Actor) actor).inputPortList().iterator();
            ports = ((Actor) actor).outputPortList().iterator();
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
                        if (receiver.toString().contains("_Controller")) {
                            continue;
                        }
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
                                    + farPort.getName() + "->_localReceivers, ";
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
                                if (farReceiverss[foo][bar].equals(receiver)) {
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
                    if (sanitizedFarActorName.contains("Controller")) {
                        continue;
                    }
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
                                        + port.getFullName() + " in actor : "
                                        + sanitizedContainerName);
                    }

                    result.append(_eol + "pblListAdd(pblListGet("
                            + port.getName() + "->_insideReceivers, " + i + ")"
                            + ", pblListGet(pblListGet(" + farPortName + foo
                            + "), " + bar + "));");
                }
            }
        }

        return result.toString();
    }

    /** Generate The functions' declaration code for this director.
     *
     *  @return The functions' declaration function code.
     *  @exception IllegalActionException If thrown while generating code.
     */
    public String generateFunctionsDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);

        code.append(_eol + "void " + _sanitizedDirectorName
                + "_Preinitialize();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Initialize();");
        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Prefire();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Fire();");
        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Postfire();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Wrapup();");

        return code.toString();
    }

    /** Generate the initialize function code for the associated FSM director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeFunctionCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        List actorList = container.deepEntityList();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        code.append(_eol + _eol
                + codeGenerator.comment("Initialization of the director"));

        if (_director.isEmbedded()) {
            ptolemy.actor.Director executiveDirector = container
                    .getExecutiveDirector();
            // Some composites, such as RunCompositeActor want to be treated
            // as if they are at the top level even though they have an executive
            // director, so be sure to check _isTopLevel().
            if (executiveDirector instanceof SuperdenseTimeDirector) {
                code.append(_eol
                        + _sanitizedDirectorName
                        + ".currentMicrostep = "
                        + ((SuperdenseTimeDirector) executiveDirector)
                        .getIndex() + ";");
            }
        }

        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            if (!actor.getFullName().contains("_Controller")) {
                code.append(_eol + sanitizedActorName + "_initialize();");
            }
        }

        code.append(_eol + _sanitizedDirectorName + ".containerActor = &"
                + sanitizedContainerName + ";");

        code.append(_eol + _sanitizedDirectorName + ".currentModelTime = "
                + _sanitizedDirectorName + ".startTime;");
        code.append(_eol + _sanitizedDirectorName + ".exceedStopTime = false;");

        code.append(_eol + _sanitizedDirectorName + ".isInitializing = false;");
        code.append(_eol
                + codeGenerator
                .comment("End of the Initialization of the director"));

        return code.toString();
    }

    /** Generate a main loop for an execution under the control of
     *  this FSM director.
     *
     *  @return Code for the main loop of an execution.
     *  @exception IllegalActionException If something goes wrong.
     */
    @Override
    public String generateMainLoop() throws IllegalActionException {
        // Need a leading _eol here or else the execute decl. gets stripped out.
        StringBuffer code = new StringBuffer();
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);

        code.append(_eol + "void " + _sanitizedDirectorName
                + "_Preinitialize() {" + _eol);
        code.append(generatePreinitializeMethodBodyCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Prefire() {"
                + _eol);
        code.append(generatePrefireCode());
        code.append(_eol + "}" + _eol);

        code.append("boolean " + _sanitizedDirectorName + "_Postfire() {"
                + _eol);
        code.append(generatePostfireCode());
        code.append(_eol + "}" + _eol);

        code.append("void " + _sanitizedDirectorName + "_Fire() {" + _eol);
        code.append(generateFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void " + _sanitizedDirectorName + "_Initialize() {"
                + _eol);
        code.append(generateInitializeFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void " + _sanitizedDirectorName + "_Wrapup() {"
                + _eol);
        code.append(generateWrapupCode());
        code.append(_eol + "}" + _eol);

        return code.toString();
    }

    /** Generate the code for the firing of this director.
     *
     *  @return The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating fire code for the actor.
     */
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment("The firing of the director."));

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor.getFullName().contains("_Controller")) {
                NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                        .getAdapter(actor);
                code.append(adapter.generateFireCode());
            }
        }
        return code.toString();
    }

    /** Generate the postfire code of the associated composite actor.
     *
     *  @return The postfire code of the associated composite actor.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating postfire code for the actor
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol
                + getCodeGenerator()
                .comment(0, "The postfire of the director."));

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor.getFullName().contains("_Controller")) {
                NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                        .getAdapter(actor);
                code.append(adapter.generatePostfireCode());
            }
        }

        return code.toString();
    }

    /** Generate the prefire code of the associated composite actor.
     *
     *  @return The prefire code of the associated composite actor.
     *  @exception IllegalActionException It should never happen
     */
    @Override
    public String generatePrefireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol + "return true;");

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
        StringBuffer code = new StringBuffer();
        //code.append(super.generatePreinitializeCode());
        // We do execute this method without using its result because we need to initialize the offsets
        super.generatePreinitializeCode();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);

        getSanitizedDirectorName();

        code.append(_eol + "" + _sanitizedDirectorName
                + ".preinitializeFunction = " + _sanitizedDirectorName
                + "_Preinitialize;");
        code.append(_eol + "" + _sanitizedDirectorName
                + ".initializeFunction = " + _sanitizedDirectorName
                + "_Initialize;");
        code.append(_eol + "" + _sanitizedDirectorName + ".prefireFunction = "
                + _sanitizedDirectorName + "_Prefire;");
        code.append(_eol + "" + _sanitizedDirectorName + ".postfireFunction = "
                + _sanitizedDirectorName + "_Postfire;");
        code.append(_eol + "" + _sanitizedDirectorName + ".fireFunction = "
                + _sanitizedDirectorName + "_Fire;");
        code.append(_eol + "" + _sanitizedDirectorName + ".wrapupFunction = "
                + _sanitizedDirectorName + "_Wrapup;");
        code.append(_eol + "" + _sanitizedDirectorName + ".containerActor = &"
                + sanitizedContainerName + ";");

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
    public String generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        List actorList = container.deepEntityList();
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            if (!actor.getFullName().contains("_Controller")) {
                code.append(_eol + sanitizedActorName + "_preinitialize();");
            }
        }

        return code.toString();
    }

    /** Generate the code for the transfer of input values inside the modal model.
     *
     *  @return The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating code for the actor.
     */
    public String generateTransferInputCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        String containerName = generateName(container);

        List<TypedIOPort> inputPorts = container.inputPortList();
        List<TypedIOPort> outputPorts = container.outputPortList();
        TypedIOPort inputPort;
        for (int i = 0; i < inputPorts.size(); i++) {

            inputPort = inputPorts.get(i);
            if (!outputPorts.contains(inputPort)) {
                code.append(_eol + containerName + "__Controller_"
                        + inputPort.getName() + "_isPresent = false;" + _eol);
            }
        }
        Iterator<?> ports = container.inputPortList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            if (!port.getFullName().contains("_Controller")) {
                code.append(_eol + port.getName() + " = (struct IOPort*)"
                        + containerName + "_get_" + port.getName() + "();");
                int width = port.getWidth();
                for (int i = 0; i < width; i++) {
                    code.append(_eol + "if (pblMapContainsKey(mapTokensIn, &"
                            + port.getName() + ", sizeof(struct IOPort*))) {"
                            + _eol);
                    code.append(_eol
                            + "Token temp = *((Token*)pblMapGet(mapTokensIn, &"
                            + port.getName()
                            + ", sizeof(struct IOPort*), NULL));");
                    code.append(_eol + containerName + "__Controller_"
                            + port.getName());
                    code.append(" = temp");
                    code.append(".payload."
                            + getCodeGenerator().codeGenType(port.getType())
                            + ";" + _eol);
                    code.append(_eol + containerName + "__Controller_"
                            + port.getName() + "_isPresent = true;" + _eol);
                    code.append(_eol + "}");
                }

            }
        }
        ports = container.outputPortList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            if (!port.getFullName().contains("_Controller")) {
                code.append(_eol + port.getName() + " = (struct IOPort*)"
                        + containerName + "_get_" + port.getName() + "();");
            }
        }
        return processCode(code.toString());
    }

    /** Generate the code for the transfer of output values inside the modal model.
     *
     *  @return The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating code for the actor.
     */
    public String generateTransferOutputCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        String containerName = generateName(container);

        Iterator<?> ports = container.outputPortList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            if (!port.getFullName().contains("_Controller")) {
                code.append(_eol + port.getName() + " = (struct IOPort*)"
                        + containerName + "_get_" + port.getName() + "();");
                int width = port.getWidth();
                String widthDeclaration;
                for (int i = 0; i < width; i++) {
                    if (width > 1) {
                        widthDeclaration = "[" + i + "]";
                    } else {
                        widthDeclaration = "";
                    }
                    code.append(_eol + "if (pblMapContainsKey(mapTokensOut, &"
                            + port.getName() + ", sizeof(struct IOPort*))) {"
                            + _eol);
                    code.append(_eol
                            + "Token temp = *((Token*)pblMapGet(mapTokensOut, &"
                            + port.getName()
                            + ", sizeof(struct IOPort*), NULL));");
                    code.append(_eol + containerName + "__Controller_"
                            + port.getName() + widthDeclaration);
                    code.append(" = temp");
                    code.append(".payload."
                            + getCodeGenerator().codeGenType(port.getType())
                            + ";" + _eol);
                    code.append(_eol + "}");
                }

            }
        }
        ports = container.outputPortList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            if (!port.getFullName().contains("_Controller")) {
                code.append(_eol + port.getName() + " = (struct IOPort*)"
                        + containerName + "_get_" + port.getName() + "();");
            }
        }
        return processCode(code.toString());
    }

    /** We override the super method, because the declaration
     * of the variables are in the actor's files.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);

        CompositeActor container = ((CompositeActor) _director.getContainer());
        //code.append(_eol + "Director " + _sanitizedDirectorName + ";");
        Iterator<?> ports = container.inputPortList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            if (!port.getFullName().contains("_Controller")) {
                code.append(_eol + "static struct IOPort* " + port.getName()
                        + ";");
            }
        }
        ports = container.outputPortList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            if (!port.getFullName().contains("_Controller")) {
                code.append(_eol + "static struct IOPort* " + port.getName()
                        + ";");
            }
        }

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor.getFullName().contains("_Controller")) {
                NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                        .getAdapter(actor);
                code.append(adapter.generateVariableDeclaration());
            }
        }

        return code.toString();
    }

    /** Returns the sanitized name of this director adapter.
     *
     * @return The sanitized name of the director
     */
    public String getSanitizedDirectorName() {
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);
        return _sanitizedDirectorName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The sanitized name of the director.
     */
    protected String _sanitizedDirectorName;
    // FIXME: move the declaration of _sanitizedDirectorName up the class hierarchy.
}
