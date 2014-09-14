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
package ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.domains.ptides.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtidesPreemptiveEDFDirector

/**
 Code generator adapter associated with the PtidesPreemptiveEDFDirector class.
 This adapter generates XMOS specific code.

 FIXME: add renesas code
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
     *  PtidesDirector.
     *  @param ptidesPreemptiveEDFDirector The associated director
     *  ptolemy.domains.ptides.kernel.PtidesDirector
     */
    public PtidesPreemptiveEDFDirector(
            ptolemy.domains.ptides.kernel.PtidesDirector ptidesPreemptiveEDFDirector) {
        super(ptidesPreemptiveEDFDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the assembly file associated for this PtidyOS program.
     *  @return The generated assembly file code.
     *  @exception IllegalActionException If thrown while generating the XC file.
     */
    @Override
    public Map<String, String> generateAdditionalCodeFiles()
            throws IllegalActionException {
        Map<String, String> list = new HashMap<String, String>();
        list.put("xc", _generateXCFile());
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
        StringBuffer code = new StringBuffer();

        code.append(super.generateInitializeCode());
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

        //code.append(_templateParser.getCodeStream().getCodeBlock("initPDBlock"));

        return code.toString();
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

        code.append(_generateActorFireCode());

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

        //        List args = new ArrayList();
        //
        //        _templateParser.getCodeStream().append(
        //                _templateParser.getCodeStream().getCodeBlock("initPDCodeBlock",
        //                        args));

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

    Map<Integer, String> _actuatorPins = new HashMap();

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

        _templateParser.getCodeStream().appendCodeBlocks(
                "CommonTypeDefinitions");
        _templateParser.getCodeStream().appendCodeBlocks("StructDefBlock");
        _templateParser.getCodeStream().appendCodeBlocks("FuncProtoBlock");
        _templateParser.getCodeStream().appendCodeBlocks("SchedulerBlock");

        List args = new LinkedList();

        // prototypes for actor functions
        _templateParser.getCodeStream().append(_generateActorFuncProtoCode());

        // prototypes for actuator functions.
        _templateParser.getCodeStream().append(
                _generateActuatorActuationFuncArrayCode());

        StringBuffer actuatorIds = new StringBuffer();
        //        for (Actor actor : (List<Actor>) ((CompositeActor) _director
        //                .getContainer()).deepEntityList()) {
        //            if (actor instanceof ActuatorSetup) {
        //                actuators.put(actor, Integer.valueOf(actuatorIndex));
        //                actuatorIndex++;
        //
        //                devicePortId = ((StringToken) ((Parameter) ((ActuatorSetup) actor)
        //                        .getAttribute("devicePortId")).getToken())
        //                        .stringValue();
        //                _devicePortIds.put(actor, devicePortId);
        //
        //                deviceId = ((StringToken) ((Parameter) ((ActuatorSetup) actor)
        //                        .getAttribute("deviceId")).getToken()).stringValue();
        //                _deviceIds.put(actor, deviceId);
        //            }
        //
        //            if (actor instanceof SensorHandler) {
        //                sensors.put(actor, Integer.valueOf(sensorIndex));
        //                sensorIndex++;
        //
        //                devicePortId = ((StringToken) ((Parameter) ((SensorHandler) actor)
        //                        .getAttribute("devicePortId")).getToken())
        //                        .stringValue();
        //                _devicePortIds.put(actor, devicePortId);
        //
        //                deviceId = ((StringToken) ((Parameter) ((SensorHandler) actor)
        //                        .getAttribute("deviceId")).getToken()).stringValue();
        //                _deviceIds.put(actor, deviceId);
        //            }
        //        }

        //        for (int i = 0; i < maxNumSensorInputs - sensors.size(); i++) {
        //            args.add("");
        //        }
        //        _templateParser.getCodeStream()
        //                .append(_templateParser.getCodeStream().getCodeBlock(
        //                        "FuncBlock", args));

        args.clear();

        StringBuffer switchstatement = new StringBuffer("switch(type) {\n");
        for (Actor actuator : actuators.keySet()) {
            String deviceName = CodeGeneratorAdapter
                    .generateName((NamedObj) actuator);
            switchstatement
                    .append("case " + _deviceIds.get(actuator) + ":\n"
                            + "    newEvent->fire = " + deviceName + ";\n"
                            + "break;\n");
            if (actuatorIds.length() > 0) {
                actuatorIds.append(", ");
            }
            actuatorIds.append(_deviceIds.get(actuator));
        }
        switchstatement.append("}");
        args.add(actuatorIds.toString());
        args.add(switchstatement.toString());

        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream().getCodeBlock("ActuationBlock",
                        args));

        if (!_templateParser.getCodeStream().isEmpty()) {
            sharedCode.add(processCode(_templateParser.getCodeStream()
                    .toString()));
        }

        return sharedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Fire methods for each actor.
     * @return fire methods for each actor
     * @exception IllegalActionException If thrown when getting the port's adapter.
     */
    @Override
    public String _generateActorFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);

            //            if (actor instanceof ActuationDevice) {
            //                code.append("void Actuation_"
            //                        + CodeGeneratorAdapter.generateName((NamedObj) actor)
            //                        + "() {" + _eol);
            //                code.append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.OutputDevice) adapter)
            //                        .generateActuatorActuationFuncCode());
            //                code.append("}" + _eol);
            //            }

            String fireFunctionParameters = adapter.getFireFunctionParameters();
            code.append("void "
                    + CodeGeneratorAdapter.generateName((NamedObj) actor) + "("
                    + fireFunctionParameters + ") " + "{" + _eol);
            code.append(adapter.generateFireCode());

            // After each actor firing, the Event Head ptr needs to point to null
            code.append(_generateClearEventHeadCode(actor));
            code.append("}" + _eol);
        }

        return code.toString();
    }

    /** Generate the initialization code for any hardware component that is used.
     *  @return code initialization code for hardware peripherals
     *  @exception IllegalActionException If thrown by the super class.
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

    /** Generate the function prototype.
     *  @return The function prototype.
     */
    protected String _generateSensorFuncProtoCode() {
        StringBuffer code = new StringBuffer();

        //for (Actor actor : (List<Actor>) ((CompositeActor) _director
        //     .getContainer()).deepEntityList()) {
        //            if (actor instanceof SensorHandler) {
        //                code.append("void "
        //                        + CodeGeneratorAdapter.generateName((NamedObj) actor)
        //                        + "(streaming chanend schedulerChannel, const Time &timestamp);"
        //                        + _eol);
        //            }
        //}

        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private String _generateXCFile() throws IllegalActionException {
        List<String> args = new ArrayList<String>();
        _templateParser.getCodeStream().clear();

        StringBuffer sensorDefinition = new StringBuffer();
        StringBuffer sensorReadyFlags = new StringBuffer();
        StringBuffer sensorSwitch = new StringBuffer(
                "while (1) {\n    select {\n");
        for (Actor sensor : sensors.keySet()) {
            String deviceName = CodeGeneratorAdapter
                    .generateName((NamedObj) sensor) + "_device";

            sensorDefinition.append("on stdcore[1]: in port " + deviceName
                    + " = " + _devicePortIds.get(sensor) + ";\n");

            sensorReadyFlags.append("uint8 " + deviceName + "Ready = TRUE;\n");

            sensorSwitch.append("case " + deviceName + " when pinseq("
                    + deviceName + "Ready) :> void:\n" + "if (" + deviceName
                    + "Ready) {\n"
                    + "getTimestamp(timestamp, platformClockChannel);\n"
                    + CodeGeneratorAdapter.generateName((NamedObj) sensor)
                    + "(schedulerChannel, timestamp);\n" + deviceName
                    + "Ready = FALSE;\n" + "} else {\n" + deviceName
                    + "Ready = TRUE;\n" + "}\n break; \n");
        }

        sensorSwitch.append("}\n}\n");

        StringBuffer actuatorDefinition = new StringBuffer();
        StringBuffer doActuation = new StringBuffer();
        StringBuffer initActuatorString = new StringBuffer();
        for (Actor actuator : actuators.keySet()) {
            String deviceName = CodeGeneratorAdapter
                    .generateName((NamedObj) actuator);
            actuatorDefinition.append("on stdcore[1]: out port " + deviceName
                    + " = " + _devicePortIds.get(actuator) + ";\n");

            doActuation
                    .append("void "
                            + deviceName
                            + "_Actuation() {\n"
                            + "timer time;\n uint32 count;\n"
                            + deviceName
                            + " <: 1;\n time :> count;\ntime when timerafter(count + 5000) :> void;"
                            + deviceName + " <: 0;\n}\n");

            initActuatorString.append(deviceName + " <: 0;\n");
        }

        String sensorProtoCode = _generateSensorFuncProtoCode();
        args.add(sensorDefinition.toString());
        args.add(sensorProtoCode);
        args.add(actuatorDefinition.toString());
        args.add(sensorReadyFlags.toString());
        args.add(sensorSwitch.toString());
        args.add(doActuation.toString());
        args.add(initActuatorString.toString());

        _templateParser.getCodeStream().append(
                _templateParser.getCodeStream().getCodeBlock("XCCodeBlock",
                        args));

        return processCode(_templateParser.getCodeStream().toString());
    }

    private Map<Actor, String> _devicePortIds = new HashMap();
    private Map<Actor, String> _deviceIds = new HashMap();

}
