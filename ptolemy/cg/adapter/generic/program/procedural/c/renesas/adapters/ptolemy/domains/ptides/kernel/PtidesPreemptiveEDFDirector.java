/* Code generator adapter class associated with the PtidesBasicDirector class.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.renesas.adapters.ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ptides.lib.ActuatorSetup;
import ptolemy.domains.ptides.lib.SensorHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtidesPreemptiveEDFDirector

/**
 Code generator adapter associated with the PtidesPreemptiveEDFDirector class.
 This adapter generates Renesas specific code.

 @author Patricia Derler
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating red (derler)
 @Pt.AcceptedRating red (derler)
 */
public class PtidesPreemptiveEDFDirector
extends
ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesPreemptiveEDFDirector {

    /** Construct the code generator adapter associated with the given
     *  PtidesBasicDirector.
     *  @param ptidesPreemptiveEDFDirector The associated director
     *  ptolemy.domains.ptides.kernel.PtidesBasicDirector
     */
    public PtidesPreemptiveEDFDirector(
            ptolemy.domains.ptides.kernel.PtidesDirector ptidesPreemptiveEDFDirector) {
        super(ptidesPreemptiveEDFDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the assembly file associated for this PtidyOS program.
     *  @return The generated assembly file code.
     *  @exception IllegalActionException If thrown by generateInterruptVectorTableCode().
     */
    @Override
    public Map<String, String> generateAdditionalCodeFiles()
            throws IllegalActionException {
        Map<String, String> list = new HashMap();
        list.put("InterruptVectorTable.c", generateInterruptVectorTableCode());
        return list;
    }

    /**
     * Generate the director fire code.
     * The code creates a new task for each actor according to
     * their specified parameters (e.g. stack depth, priority,
     * and etc.). The code also initiates the task scheduler.
     * @return The generated fire code.
     * @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(getCodeGenerator().comment("Create a task for each actor."));

        for (Actor actor : (List<Actor>) ((CompositeActor) getComponent()
                .getContainer()).deepEntityList()) {
            NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(actor);
            code.append(adapter.generateFireCode());
        }

        return code.toString();
    }

    /** Generate the interrupt code.
     *  @exception IllegalActionException If there is a problem
     *  accessing the model.
     */
    public void generateInterruptCode() throws IllegalActionException {
        List args = new ArrayList();
        for (int key : _interruptHandlerNames.keySet()) {
            args.clear();
            String function = _interruptHandlerNames.get(key);
            String letter = RenesasUtilities.interruptHandlerLetters.get(key)
                    + "";
            if (function.endsWith("_Handler")) {
                args.add(function);
                args.add(letter);
                _templateParser.getCodeStream().append(
                        _templateParser.getCodeStream().getCodeBlock(
                                "actuationBlock", args));
            }
        }

        // Interrupts code has to be at the end.
        args.clear();
        StringBuffer emptyfunctions = new StringBuffer();
        for (int key : _interruptHandlerNames.keySet()) {
            String function = _interruptHandlerNames.get(key);
            if (function.equals("")) {
                emptyfunctions.append("void EmptyInterruptHandler_" + key
                        + "() {}\n");
                function = "EmptyInterruptHandler_" + key;
            }
        }
        args.add(emptyfunctions.toString());

        StringBuffer systick1 = new StringBuffer();
        StringBuffer systick2 = new StringBuffer();
        for (Actor actuator : actuators.keySet()) {
            Character letter = RenesasUtilities.interruptHandlerLetters
                    .get(actuators.get(actuator));
            systick1.append("if (actNs" + letter
                    + "[dummy] < ((4*divideByValue/2) << 16)) {\n" + "    actS"
                    + letter + "[dummy] = actS" + letter + "[dummy]-1;\n"
                    + "    actNs" + letter + "[dummy] = 1000000000+actNs"
                    + letter + "[dummy]-((4*divideByValue/2) << 16);\n"
                    + "} else {\n" + "    actNs" + letter + "[dummy] = actNs"
                    + letter + "[dummy] - ((4*divideByValue/2) << 16);\n"
                    + "}\n");

            systick2.append("if ((MTU20.TIOR.BIT.IO" + letter
                    + " == 0) && (actWr" + letter + " != actRd" + letter
                    + ") && (actNs" + letter + "[actRd" + letter
                    + "] < ((4*divideByValue/2)*(65536 + intDel)))) {\n"
                    + "    MTU20.TGR" + letter + " = actNs" + letter + "[actRd"
                    + letter + "]/(4*divideByValue/2);\n"
                    + "    MTU20.TSR.BIT.TGF" + letter + " = 0;\n"
                    + "    MTU20.TIER.BIT.TGIE" + letter + " = 1;\n"
                    + "    if (actSt" + letter + " == 0)\n"
                    + "            MTU20.TIOR.BIT.IO" + letter + " =2;\n"
                    + "    else\n" + "             MTU20.TIOR.BIT.IO" + letter
                    + " = 5;\n" + "}");
        }

        args.add(systick1);
        args.add(systick2);

        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream().getCodeBlock("InterruptBlock",
                        args));

    }

    /** Generate the interrupt vector table.
     *  @return The interrupt vector table.
     *  @exception IllegalActionException If there is a problem
     *  accessing the model.
     */
    public String generateInterruptVectorTableCode()
            throws IllegalActionException {
        _templateParser.getCodeStream().clear();
        List args = new ArrayList();
        StringBuffer externDeclarations = new StringBuffer();
        for (int key : _interruptHandlerNames.keySet()) {
            String function = _interruptHandlerNames.get(key);
            if (function.equals("")) {
                function = "EmptyInterruptHandler_" + key;
            }
            args.add(function);
            externDeclarations.append("extern void " + function + "(void);\n");
        }
        args.add(externDeclarations.toString());
        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream().getCodeBlock(
                        "InterruptVectorTable", args));

        return processCode(_templateParser.getCodeStream().toString());
    }

    /**
     * Generate the fire function code.
     * The code contains the function code for each actor. It is a collection
     * of global functions, one for each actor that is visible to this
     * director adapter. Creating each new task requires one of these
     * function as parameter. It is the code that the task executes.
     * When the inline parameter is checked, the task function code is
     * generated in {@link #generatePreinitializeCode()} which is
     * outside the main function.
     * @return The fire function code.
     * @exception IllegalActionException If there is an exception in
     *  generating the task function code.
     */
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        return "";
    }

    /**
     * Generate the initialize code.
     * This generates the hardware initialization code and creates
     * the queues for all referrable port channels.
     * @return The generated initialize code.
     * @exception IllegalActionException If the adapter associated with
     *  an actor throws it while generating initialize code for the actor.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generateInitializeCode());

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
            return code.toString();
        }

        code.append(getCodeGenerator()
                .comment(
                        "Platform dependent initializatoin code of the PtidesDirector."));

        List args = new ArrayList();
        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream().getCodeBlock("initPDBlock",
                        args));

        return _templateParser.getCodeStream().toString();
    }

    /** Generate the preinitialize code for the associated Ptides director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating preinitialize code for the actor.
     *   FIXME: Take care of platform dependent code.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        _modelStaticAnalysis();

        code.append(_generatePtrToEventHeadCodeInputs());

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no preinit code needs to be
        // generated.
        // Notice here we append code from _generatePtrToEventHeadCodeInputs()
        // because ports inside of the EmbeddedCodeActor needs to have pointers
        // to event heads declared, which is done in the previous method.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
            return code.toString();
        }

        code.append(super._generateActorFireCode());

        CodeStream codestream = _templateParser.getCodeStream();

        codestream.clear();

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
            return code.toString();
        }

        code.append(_templateParser.getCodeStream().getCodeBlock(
                "preinitPDBlock"));

        List args = new ArrayList();
        StringBuffer initIHs = new StringBuffer();
        for (Actor actuator : actuators.keySet()) {
            Character letter = RenesasUtilities.interruptHandlerLetters
                    .get(actuators.get(actuator));
            int timerNumber = RenesasUtilities.timerNumbers.get(actuators
                    .get(actuator));
            initIHs.append("MTU2" + timerNumber + ".TIOR.BIT.IO" + letter
                    + " = 0;\n" + "MTU2" + timerNumber + ".TIER.BIT.TCIE"
                    + letter + " = 1;\n");
        }
        for (Actor sensor : sensors.keySet()) {
            Character letter = RenesasUtilities.interruptHandlerLetters
                    .get(sensors.get(sensor));
            int timerNumber = RenesasUtilities.timerNumbers.get(sensors
                    .get(sensor));
            initIHs.append("MTU2" + timerNumber + ".TIOR.BIT.IO" + letter
                    + " = 8;\n" + "MTU2" + timerNumber + ".TIER.BIT.TCIE"
                    + letter + " = 1;\n");
        }

        args.add(initIHs.toString());
        code.append(_templateParser.getCodeStream().getCodeBlock(
                "initPDCodeBlock", args));

        code.append(_generateInitializeHardwareCode());

        return code.toString();
    }

    /**
     * Generate variable initialization for the referenced parameters.
     * This overrides the super class method and returns an empty
     * string. It avoids generating any offset variables.
     * @return code The empty string.
     * @exception IllegalActionException Not thrown in this class.
     */
    @Override
    public String generateVariableInitialization()
            throws IllegalActionException {
        return "";
    }

    private Map<Integer, String> _interruptHandlerNames;

    /** Get sensors and actuators.
     *  @exception IllegalActionException Thrown if parameters of sensors or actuators cannot be read.
     */
    @Override
    protected void _modelStaticAnalysis() throws IllegalActionException {
        _interruptHandlerNames = new HashMap();
        for (int key : RenesasUtilities.interruptHandlerLetters.keySet()) {
            _interruptHandlerNames.put(key, "");
        }

        int id = -1;
        for (Actor actor : (List<Actor>) ((CompositeActor) _director
                .getContainer()).deepEntityList()) {
            if (actor instanceof ActuatorSetup) {
                id = ((IntToken) ((Parameter) ((ActuatorSetup) actor)
                        .getAttribute("InterruptHandlerID")).getToken())
                        .intValue();
                actuators.put(actor, id);
                if (_interruptHandlerNames.get(id) == null) {
                    throw new IllegalActionException(actor,
                            "The interrupt handler" + " with id " + id
                            + " cannot be used.");
                }
                _interruptHandlerNames.put(id,
                        CodeGeneratorAdapter.generateName((NamedObj) actor)
                        + "_Handler");
            }

            if (actor instanceof SensorHandler) {
                id = ((IntToken) ((Parameter) ((SensorHandler) actor)
                        .getAttribute("InterruptHandlerID")).getToken())
                        .intValue();
                sensors.put(actor, id);
                if (_interruptHandlerNames.get(id) == null) {
                    throw new IllegalActionException(actor,
                            "The interrupt handler" + " with id " + id
                            + " cannot be used.");
                }
                _interruptHandlerNames.put(id,
                        CodeGeneratorAdapter.generateName((NamedObj) actor));
            }
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
    @Override
    public Set getSharedCode() throws IllegalActionException {
        Set sharedCode = new HashSet();

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
            return sharedCode;
        }

        _modelStaticAnalysis();

        _templateParser.getCodeStream().clear();

        // define the number of actuators in the system as a macro.
        _templateParser.getCodeStream().append(
                "#define numActuators " + actuators.size() + _eol);

        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream().getCodeBlock("StructDefBlock"));

        _templateParser.getCodeStream().appendCodeBlocks(
                "CommonTypeDefinitions");

        StringBuffer actuatorPublicVariables = new StringBuffer();
        for (Actor actuator : actuators.keySet()) {
            Character letter = RenesasUtilities.interruptHandlerLetters
                    .get(actuators.get(actuator));
            actuatorPublicVariables.append("uint32 actS" + letter
                    + "[10], actNs" + letter + "[10];\n" + "uint32 actRd"
                    + letter + " = 0, actWr" + letter + " = 0, actSt" + letter
                    + " = 0;\n");
        }
        List<String> args = new ArrayList();
        args.add(actuatorPublicVariables.toString());
        StringBuffer interruptPragmas = new StringBuffer(
                "#pragma interrupt SysTickHandler(resbank)\n"
                        + "#pragma interrupt SafeToProcessInterruptHandler(resbank)\n");
        for (Integer id : _interruptHandlerNames.keySet()) {
            String function = _interruptHandlerNames.get(id);
            if (function.equals("")) {
                function = "EmptyInterruptHandler_" + id;
            }
            interruptPragmas.append("#pragma interrupt" + function
                    + "(resbank)\n");
        }
        args.add(interruptPragmas.toString());
        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream()
                .getCodeBlock("globalVars", args));

        _templateParser.getCodeStream().append(_generateActorFuncProtoCode());

        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream().getCodeBlock("FuncBlock"));

        generateInterruptCode();

        if (!_templateParser.getCodeStream().isEmpty()) {
            sharedCode.add(processCode(_templateParser.getCodeStream()
                    .toString()));
        }

        return sharedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate the initialization code for any hardware component that is used.
     *  @return code initialization code for hardware peripherals
     *  @exception IllegalActionException If thrown while getting the
     *  adapter or generating the hardward initialization code for the 
     *  components.
     */
    protected String _generateInitializeHardwareCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
            return code.toString();
        }

        // FIXME: output initialization always needs to happen before input initialization.
        code.append("void initializeHardware() {" + _eol);
        for (Actor actor : actuators.keySet()) {
            code.append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.OutputDevice) getAdapter(actor))
                    .generateHardwareInitializationCode());
        }
        for (Actor actor : sensors.keySet()) {
            code.append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.InputDevice) getAdapter(actor))
                    .generateHardwareInitializationCode());
        }

        code.append("}" + _eol);
        return code.toString();
    }
}
