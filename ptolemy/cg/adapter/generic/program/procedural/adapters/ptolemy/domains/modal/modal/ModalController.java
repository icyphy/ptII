/* Code generator helper for modal controller.

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.modal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel.FSMActor.TransitionRetriever;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ModalController

/**
 Code generator helper for modal controller.

 @author Shanna-Shaye Forbes, based on Gang Zhou's ptolemy/codegen work.  Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating red (sssf)
 @Pt.AcceptedRating red (sssf)
 */
public class ModalController extends NamedProgramCodeGeneratorAdapter {

    /** Construct the code generator helper associated
     *  with the given modal controller.
     *  @param component The associated modal controller component.
     */
    public ModalController(ptolemy.domains.modal.modal.ModalController component) {
        super(component);
        _myController = component;
    }

    /**
     * Generate the preinitialization code for the director.
     * @return string containing the preinitializaton code
     * @exception IllegalActionException If thrown by the superclass or thrown
     * while generating code for the director.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        _createControllerVariables(code);

        code.append(_generateActorCode());
        return code.toString();
    }

    @Override
    public String generateFireCode() throws IllegalActionException {
        NamedProgramCodeGeneratorAdapter controller = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(_myController);
        FSMActor controllerHelper;
        try {
            controllerHelper = new FSMActor(_myController);
            controllerHelper.setCodeGenerator(getCodeGenerator());
            controllerHelper.setTemplateParser(getTemplateParser());
        } catch (NameDuplicationException ndx) {
            throw new IllegalActionException(ndx.toString());
        }
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment(
                "1. Start transfer tokens to the outside."));

        List<IOPort> inputPorts = _myController.inputPortList();
        for (int i = 0; i < inputPorts.size(); i++) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.get(i);
            if (!inputPort.isOutput()) {
                generateTransferInputsCode(inputPort, code);
            }
        }

        // Generate code for preemptive transition.
        code.append(_eol
                + getCodeGenerator().comment("2. Preemptive Transition"));

        controllerHelper.generateTransitionCode(code,
                new PreemptiveTransitions());

        code.append(_eol);

        // Check to see if a preemptive transition is taken.
        code.append("if ("
                + controller.processCode("$actorSymbol(transitionFlag)")
                + " == 0) {" + _eol);

        // Generate code for refinements.
        _generateRefinementCode(code);

        // Generate code for non-preemptive transition
        code.append(getCodeGenerator().comment("3. Nonpreemptive Transition"));
        // generateTransitionCode(code);
        controllerHelper.generateTransitionCode(code,
                new NonPreemptiveTransitions());
        code.append("}" + _eol);
        code.append(getCodeGenerator().comment("4. Start transfer outputs."));
        List<IOPort> outputPorts = _myController.outputPortList();
        for (int i = 0; i < outputPorts.size(); i++) {
            IOPort outputPort = outputPorts.get(i);

            generateTransferOutputsCode((TypedIOPort) outputPort, code);

        }
        code.append(getCodeGenerator().comment("5. End transfer outputs."));
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
        Actor actor;
        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) getComponent()
                .getContainer();
        getCodeGenerator().getAdapter(container);

        // Reset the offset for all of the contained actors' input ports.
        Iterator<?> actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {

            actor = (Actor) actors.next();
            getAdapter(actor);
            //if (actor.getDisplayName().contains("_Controller")) {
            //actorHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
            //      .getAdapter(actor);
            //code.append(actorHelper.generateFireFunctionCode());
            //}

        }
        return code.toString();
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(TypedIOPort inputPort,
            StringBuffer code) throws IllegalActionException {

        //FIXME Figure out how to deal with multiports
        List<IOPort> connectedPorts = inputPort.sinkPortList();
        for (int i = 0; i < connectedPorts.size(); i++) {
            TypedIOPort t = (TypedIOPort) connectedPorts.get(i);
            if (t.isInput()) {
                // FIXME: Defaulting to buffer size 1.
                code.append(getCodeGenerator().generatePortName(
                        t,
                        StringUtilities.sanitizeName(t.getFullName())
                        .substring(1), 1)
                        + " = ");
            }
        }
        String name = inputPort.getFullName();
        int i = name.indexOf("_Controller");
        name = name.substring(0, i) + name.substring(i + 12);
        code.append( // FIXME: Defaulting to buffer size 1.
                getCodeGenerator().generatePortName(
                        inputPort,
                        StringUtilities.sanitizeName(inputPort.getFullName())
                        .substring(1), 1)
                        + " = "
                        // FIXME: Defaulting to buffer size 1.
                        + getCodeGenerator().generatePortName(inputPort,
                                StringUtilities.sanitizeName(name).substring(1), 1)
                                + "; ");

    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(TypedIOPort outputPort,
            StringBuffer code) throws IllegalActionException {

        NamedProgramCodeGeneratorAdapter _compositeActorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(
                        ((ptolemy.domains.modal.modal.ModalController) getComponent())
                        .getDirector().getContainer());

        //executive If true, then look for the reference in the
        // executive director (the director of the container).  The
        // CaseDirector calls this with executive == true, most (all?)
        // other Directors call this with executive == false.
        boolean executive = true;
        for (int i = 0; i < outputPort.getWidthInside(); i++) {
            if (i < outputPort.getWidth()) {
                String name = outputPort.getName();

                if (outputPort.isMultiport()) {
                    name = name + '#' + i;
                }

                //code.append(name + " = ");
                //code.append("@" + name);
                //code.append(getReference(name) + " = ");
                //code.append(getReference("@" + name));
                code.append(_compositeActorAdapter.getReference(name, false,
                        executive)
                        + " = "
                        + _compositeActorAdapter.getReference("@" + name,
                                false, executive) + ";");
            }
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, 1);
    }

    /** Generate code for the firing of refinements.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @return The refinemebt code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    protected boolean _generateRefinementCode(StringBuffer code)
            throws IllegalActionException {

        ProgramCodeGeneratorAdapter controllerHelper = (ProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(_myController);

        boolean refined = false;
        int depth = 1;
        code.append(_getIndentPrefix(depth));
        code.append("switch ("
                + controllerHelper.processCode("$actorSymbol(currentState)")
                + ") {" + _eol);

        Iterator states = _myController.entityList().iterator();
        int stateCount = 0;
        depth++;

        while (states.hasNext()) {
            code.append(_getIndentPrefix(depth));
            code.append("case " + stateCount + ":" + _eol);
            stateCount++;

            depth++;

            State state = (State) states.next();
            Actor[] actors = state.getRefinement();

            if (actors != null) {
                refined = true;
                for (Actor actor : actors) {
                    NamedProgramCodeGeneratorAdapter actorHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                            .getAdapter(actor);

                    // fire the actor

                    code.append(actorHelper.generateFireCode());

                    List<IOPort> outputPorts = actor.outputPortList();
                    for (IOPort outputPort : outputPorts) {

                        String source = outputPort.getFullName().substring(1)
                                .replace(".", "_");
                        int k = source.lastIndexOf("_");
                        int l = source.substring(0, k).lastIndexOf("_");
                        String destination = source.substring(0, l)
                                + "__Controller" + source.substring(k);
                        //update controller outputs
                        code.append(_eol
                                // FIXME: Defaulting to buffer size 1.
                                + getCodeGenerator().generatePortName(
                                        (TypedIOPort) outputPort, destination,
                                        1)
                                        + " = "
                                        // FIXME: Defaulting to buffer size 1.
                                        + getCodeGenerator().generatePortName(
                                                (TypedIOPort) outputPort, source, 1)
                                                + ";" + _eol);
                    }

                }
            }

            code.append(_getIndentPrefix(depth));

            code.append("break;" + _eol); //end of case statement
            depth--;
        }
        depth--;
        code.append(_getIndentPrefix(depth));
        code.append("}" + _eol); //end of switch statement

        return refined;
    }

    /** Update the read offsets of the buffer associated with the given port.
     *
     *  @param port The port whose read offset is to be updated.
     *  @param code The string buffer that the generated code is appended to.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    protected void _updatePortOffset(TypedIOPort port, StringBuffer code,
            int rate) throws IllegalActionException {
        if (_debugging) {
            _debug("_updatePortOffset in Modal controller called");
        }
        if (rate == 0) {
            return;
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }
        //NamedProgramCodeGeneratorAdapter portHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
        //        .getAdapter(port);
        String str;
        Receiver rec[][] = port.getRemoteReceivers();

        for (Receiver[] element : rec) {
            for (int j = 0; j < element.length; j++) {
                //str = rec[i][j].toString();
                //str = str.substring(str.indexOf("{") + 2, str.lastIndexOf("."));
                //str = str.replace('.', '_');
                code.append(getCodeGenerator().comment(
                        "MC: updatePortOffsets: "
                                + element[j].getContainer().getFullName()));
                String portName = StringUtilities.sanitizeName(element[j]
                        .getContainer().getFullName());
                // FIXME: Defaulting to buffer size 1.
                str = getCodeGenerator().generatePortName(
                        (TypedIOPort) element[j].getContainer(),
                        portName.substring(1), 1);
                code.append(str + " = ");
            }
        }

        //code.append(portHelper.getDisplayName() + ";" + _eol);
        // FIXME: Defaulting to buffer size 1.
        code.append(getCodeGenerator().generatePortName(port,
                StringUtilities.sanitizeName(port.getFullName()).substring(1),
                1)
                + ";" + _eol);
    }

    /** Update the offsets of the buffers associated with the ports connected
     *  with the given port in its downstream.
     *
     *  @param port The port whose directly connected downstream actors update
     *   their write offsets.
     *  @param code The string buffer that the generated code is appended to.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    protected void _updateConnectedPortsOffset(IOPort port, StringBuffer code,
            int rate) throws IllegalActionException {

        if (rate == 0) {
            return;
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }
        NamedProgramCodeGeneratorAdapter portHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(port);
        String str;
        Receiver rec[][] = port.getRemoteReceivers();

        for (Receiver[] element : rec) {
            for (int j = 0; j < element.length; j++) {
                str = element[j].toString();
                str = str.substring(str.indexOf("{") + 2, str.lastIndexOf("."));
                str = str.replace('.', '_');

                code.append(str + " = ");
            }
        }

        code.append(portHelper.getDisplayName() + ";" + _eol);
    }

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Generate code that creates the controller variables.
     * @param code The StringBuffer to which to add code.
     * @exception IllegalActionException If thrown while getting the
     * width of an input port.
     */
    private void _createControllerVariables(StringBuffer code)
            throws IllegalActionException {
        List<TypedIOPort> inputPorts = _myController.inputPortList();
        List<TypedIOPort> outputPorts = _myController.outputPortList();
        String name = _myController.getFullName().substring(1);
        String modalName = name.replace("._Controller", "");
        name = name.replace('.', '_');
        modalName = modalName.replace('.', '_');
        TypedIOPort inputPort;
        TypedIOPort outputPort;
        code.append(getCodeGenerator().comment(
                "Beginning of create controller variables."));
        for (int i = 0; i < inputPorts.size(); i++) {

            inputPort = inputPorts.get(i);
            if (!outputPorts.contains(inputPort)) {
                int width = inputPort.getWidth();
                // FIXME "static" is language-dependent, but we need it
                // generating code in small blocks, see:
                // $PTII/bin/ptcg -language java -generateInSubdirectory true -inline false -maximumLinesPerBlock 1 -variablesAsArrays true $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/modal/test/auto/Simple01.xml
                code.append("static " + inputPort.getType() + " " + name + "_"
                        + inputPort.getName());
                if (width > 1) {
                    code.append("[" + width + "]");
                }
                code.append(";" + _eol);
                // FIXME "static" is language-dependent, but we need it
                // generating code in small blocks.
                code.append("static " + inputPort.getType() + " " + modalName
                        + "_" + inputPort.getName());
                if (width > 1) {
                    code.append("[" + width + "]");
                }
                code.append(";" + _eol);
            }
        }

        for (int i = 0; i < outputPorts.size(); i++) {
            outputPort = outputPorts.get(i);
            int width = outputPort.getWidth();
            // FIXME "static" is language-dependent, but we need it
            // generating code in small blocks.
            code.append("static " + outputPort.getType() + " " + name + "_"
                    + outputPort.getName());
            if (width > 1) {
                code.append("[" + width + "]");
            }
            code.append(";" + _eol);

            // FIXME "static" is language-dependent, but we need it
            // generating code in small blocks.
            code.append("static " + outputPort.getType() + " " + modalName
                    + "_" + outputPort.getName());
            if (width > 1) {
                code.append("[" + width + "]");
            }
            code.append(";" + _eol);
        }

        //code.append("int " + name + "__currentState;" + _eol);
        // code.append("int " + name + "__transitionFlag;" + _eol);
        //add in inputs for all the states

        ProgramCodeGenerator programCodeGenerator = getCodeGenerator();
        Iterator states = _myController.entityList().iterator();
        int j = 0;
        while (states.hasNext()) {

            State state = (State) states.next();
            code.append(programCodeGenerator.generateConstantDefinition("STATE"
                    + state.getFullName().replace(".", "_"), "int",
                    Integer.toString(j))
                    + _eol);
            j++;

        }

        code.append(getCodeGenerator().comment(
                "End of create controller variables"));
    }

    /**
     * Generate code for all the actors associated with the given FSMDirector.
     * @return String containing the actor code.
     * @exception IllegalActionException If throw while accessing the model.
     */
    private String _generateActorCode() throws IllegalActionException {
        if (_debugging) {
            _debug("_generateActorCode in Modal controller called");
        }
        StringBuffer code = new StringBuffer();

        //int depth = 1;
        Iterator states = _myController.deepEntityList().iterator();
        //int stateCount = 0;
        //depth++;

        while (states.hasNext()) {
            // code.append(_getIndentPrefix(depth));
            //code.append("case " + stateCount + ":" + _eol);
            //stateCount++;

            //depth++;

            State state = (State) states.next();
            Actor[] actors = state.getRefinement();
            Set<Actor> actorsSet = new HashSet();
            ;
            if (actors != null) {
                for (Actor actor : actors) {
                    actorsSet.add(actor);
                }
            }

            if (actors != null) {
                //for (int i = 0; i < actors.length; i++) {
                Iterator actorIterator = actorsSet.iterator();
                Actor actor;
                while (actorIterator.hasNext()) {
                    actor = (Actor) actorIterator.next();
                    NamedProgramCodeGeneratorAdapter actorHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                            .getAdapter(actor);

                    //_getHelper((NamedObj) actors2);

                    if (actor.getDirector().getFullName().contains("Giotto") == false) {
                        //code.append("void "+_getActorName(actors2)+"(){");
                        //                       code.append(actorHelper.generateFireFunctionCode()); // this was there initially and it works with SDF

                        code.append(actorHelper.generateTypeConvertFireCode());
                        //code.append(_eol+"}"+_eol);
                    } else {
                        code.append(getCodeGenerator().comment(
                                "modal model contains giotto director"));
                    }
                }
            }
        }
        return code.toString();
    }

    /** Retrieve the nonpreemtive transitions. */
    private static class NonPreemptiveTransitions implements
    TransitionRetriever {
        // Findbugs wants this to be static.
        /** Retrieve the nonpreemtive transitions.
         *  @param state The state
         *  @return An iterator that refers to the nonpreemptive transitions.
         */
        @Override
        public Iterator retrieveTransitions(State state) {
            try {
                return state.nonpreemptiveTransitionList().iterator();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(state, e,
                        "Error evaluating transition parameters.");
            }
        }
    }

    /** Retrieve the nonpreemtive transitions. */
    private static class PreemptiveTransitions implements TransitionRetriever {
        // Findbugs wants this to be static.
        /** Retrieve the preemptive transitions.
         *  @param state The state
         *  @return An iterator that refers to the preemptive transitions.
         */
        @Override
        public Iterator retrieveTransitions(State state) {
            try {
                return state.preemptiveTransitionList().iterator();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(state, e,
                        "Error evaluating transition parameters.");
            }
        }
    }

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The associated modal controller component. */
    protected ptolemy.domains.modal.modal.ModalController _myController;

}
