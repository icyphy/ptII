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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtidesPreemptiveEDFDirector

/**
 Code generator adapter associated with the PtidesPreemptiveEDFDirector class. This class
 is also associated with a code generator.
 Also unlike the Ptolemy implementation, the execution does not depend on the WCET
 of actor.
 @author Jia Zou, Isaac Liu, Jeff C. Jensen
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating red (jiazou)
 @Pt.AcceptedRating red (jiazou)
 */
public class PtidesPreemptiveEDFDirector extends Director {

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
     *  Here we return an empty string, but the target specific adapter
     *  should overwrite it.
     *  @return The generated assembly file code.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public Map<String, String> generateAdditionalCodeFiles()
            throws IllegalActionException {
        Map<String, String> list = new HashMap();
        return list;
    }

    /** Generate the initialize code for the associated PtidesBasic director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (!(((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector)) {
            code.append(_templateParser.getCodeStream().getCodeBlock(
                    "initPIBlock"));
        }
        code.append(super.generateInitializeCode());

        return code.toString();
    }

    /** Generate a main loop for an execution under the control of
     *  this director.  In this base class, this simply delegates
     *  to generateFireCode() and generatePostfireCOde().
     *  @return Whatever generateFireCode() returns.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol
                + _templateParser.getCodeStream().getCodeBlock("mainLoopBlock"));
        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     *   NOTE: fire code for each function, as well as the scheduler, should all go here
     *   Take care of platform independent code.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());

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
        code.append(codestream.getCodeBlock("preinitPIBlock"));

        code.append(codestream.getCodeBlock("initPICodeBlock"));

        return code.toString();
    }

    /** Generate code for transferring all input tokens to the inside.
     *  In general, this is not allowed, because PtidesBasicDirector
     *  resides in an independent platform. Thus, it only connects to the outside
     *  through sensors, actuators, and network interfaces, and we use InputDevice
     *  and OutputDevice to generate sensor, actuator, and network interface
     *  code. The only place where this method is used is when a EmbeddedCodeActor
     *  is used inside of a Ptides director. In this case, the director for the
     *  EmbeddedCodeActor also has a PtidesBasicDirector, and that PtidesBasicDirector
     *  needs to transfer tokens inside of the EmbeddedCodeActor, which is done in
     *  this method. Note, since the inside EmbeddedCodeActor could _ONLY_ have a
     *  PTIDES director if the outside director is also a PTIDES director, we are
     *  sure the outside receiver much also have a PTIDES receiver.
     *
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "Transfer tokens to the inside")));

        // FIXME: Transfer token to inside actually only needs to run once...
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "FIXME: Transfer tokens to the inside actually only needs to run once, because"
                        + "we are only setting one pointer to equal another.")));
        for (int i = 0; i < inputPort.getWidth(); i++) {
            String sourcePortString = "Event_Head_"
                    + getAdapter(inputPort).getName() + "["
                    + Integer.toString(i) + "]";
            for (IOPort sinkPort : (List<IOPort>) inputPort
                    .deepInsidePortList()) {
                String sinkPortString = "Event_Head_"
                        + getAdapter(sinkPort).getName() + "["
                        + Integer.toString(i) + "]";
                code.append(sinkPortString + " = " + sourcePortString + ";"
                        + _eol);
            }
        }
    }

    /** Generate code for transferring all output tokens to the outside.
     *  In general, this is not allowed, because PtidesBasicDirector
     *  resides in an independent platform. Thus, it only connects to the outside
     *  through sensors, actuators, and network interfaces, and we use InputDevice
     *  and OutputDevice to generate sensor, actuator, and network interface
     *  code. The only place where this method is used is when a EmbeddedCodeActor
     *  is used inside of a Ptides director. In this case, the director for the
     *  EmbeddedCodeActor also has a PtidesBasicDirector, and that PtidesBasicDirector
     *  needs to transfer tokens outside of the EmbeddedCodeActor, which is done in
     *  this method. However, when an actor is trying to do "$put()$ on an output port
     *  that is connected to another output port, then the PtidesBasicReceiver would
     *  ensure a new event is created destined to the next actor on the outside, thus
     *  this method does not need to generate any code.
     *
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(getCodeGenerator()
                .comment("Transfer tokens to the outside"));
    }

    /**
     * Generate the type conversion fire code. Here we don't actually want to generate
     * any type conversion statement, the type conversion is done within the event creation.
     * @return The generated code.
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateTypeConvertFireCode() throws IllegalActionException {
        return "";
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Since we are using Events as holders for data values, we want to overwrite
     *  the method in Director to generate nothing. However, in case any of the actors
     *  are composite actors, we actually want to generate variable declaration for those
     *  actors. This is because the inside composite actors do not know data is stored
     *  within Events. Instead, it only has a reference to the input port. Thus, we need
     *  to generate a memory address for the input port to allow transfer of data first
     *  from the Events to these memory locations, and then from these memory locations
     *  to inside of the composite actors.
     *
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        for (Actor actor : (List<Actor>) ((CompositeActor) _director
                .getContainer()).deepEntityList()) {
            if (actor instanceof CompositeActor) {
                NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                        .getAdapter(actor);
                code.append(adapterObject.generateVariableDeclaration());
            }
        }
        return code.toString();
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

        Set sharedCode = new HashSet(super.getSharedCode());

        // if the outside is already a Ptides director (this could only happen if
        // we have a EmbeddedCodeActor inside of a Ptides director. This case
        // the EmbeddedCodeActor would also have a Ptides director (in order to
        // have Ptides receivers). But in this case no shared code needs to be
        // generated.
        if (((CompositeActor) getComponent().getContainer())
                .getExecutiveDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
            return sharedCode;
        }

        //PD too late, do in preeint _modelStaticAnalysis();

        _templateParser.getCodeStream().clear();

        // define the number of actuators in the system as a macro.
        _templateParser.getCodeStream().append(
                "#define numActuators " + actuators.size() + _eol);

        _templateParser.getCodeStream().appendCodeBlocks("StructDefBlock");
        _templateParser.getCodeStream().appendCodeBlocks("FuncProtoBlock");

        // prototypes for actor functions
        _templateParser.getCodeStream().append(_generateActorFuncProtoCode());

        // prototypes for actuator functions.
        _templateParser.getCodeStream().append(
                _generateActuatorActuationFuncArrayCode());

        _templateParser.getCodeStream().appendCodeBlocks("FuncBlock");

        if (!_templateParser.getCodeStream().isEmpty()) {
            sharedCode.add(processCode(_templateParser.getCodeStream()
                    .toString()));
        }

        return sharedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Map of Actors to actuator number. */
    public Map<Actor, Integer> actuators = new HashMap<Actor, Integer>();

    /** Map of Sensor to sensor number. */
    public Map<Actor, Integer> sensors = new HashMap<Actor, Integer>();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     *  Return the code for the actuatorActuations array.
     *  @return the code for the actuatorActuations array.
     */
    protected String _generateActuatorActuationFuncArrayCode() {
        StringBuffer code = new StringBuffer();

        if (actuators.size() > 0) {
            code.append("static void (*actuatorActuations[" + actuators.size()
                    + "])() = {");
            Iterator it = actuators.keySet().iterator();
            Actor actor = (Actor) it.next();
            code.append("Actuation_"
                    + CodeGeneratorAdapter.generateName((NamedObj) actor));
            while (it.hasNext()) {
                actor = (Actor) it.next();
                code.append(", Actuation_"
                        + CodeGeneratorAdapter.generateName((NamedObj) actor));
            }
            code.append("};" + _eol);
        }

        return code.toString();
    }

    /**
     *  Return the code for Actuation_*(void) function prototypes.
     *  @return the code for Actuations_*(void) function prototypes.
     */
    protected String _generateActuatorActuationFuncProtoCode() {
        StringBuffer code = new StringBuffer();

        //for (Actor actor : (List<Actor>) ((CompositeActor) _director
        //.getContainer()).deepEntityList()) {
        //            if (actor instanceof OutputDevice) {
        //                code.append("void Actuation_"
        //                        + CodeGeneratorAdapter.generateName((NamedObj) actor)
        //                        + "(void);" + _eol);
        //            }
        //}

        return code.toString();
    }

    /** Generate actor function prototypes.
     *  @return actor function prototype methods for each entity.
     *  @exception IllegalActionException If thrown while getting the
     *  adapter for the actor.
     */
    protected String _generateActorFuncProtoCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        for (Actor actor : (List<Actor>) ((CompositeActor) _director
                .getContainer()).deepEntityList()) {
            NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            String fireFunctionParameters = adapter.getFireFunctionParameters();
            if (fireFunctionParameters.equals("")) {
                fireFunctionParameters = "void";
            }
            code.append("void "
                    + CodeGeneratorAdapter.generateName((NamedObj) actor) + "("
                    + fireFunctionParameters + ");" + _eol);
        }

        code.append(_generateActuatorActuationFuncProtoCode());

        return code.toString();
    }

    /** Generate code for director header.
     *
     *  @return Code that declares the header for director
     */
    protected String _generateDirectorHeader() {
        return CodeGeneratorAdapter.generateName(_director) + "_controlBlock";
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
    @Override
    protected String _generateTypeConvertStatement(
            ProgramCodeGeneratorAdapter.Channel source,
            ProgramCodeGeneratorAdapter.Channel sink, int offset)
                    throws IllegalActionException {

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
        String sourcePortChannel = CodeGeneratorAdapter
                .generateName(source.port)
                + "#"
                + source.channelNumber
                + ", "
                + offset;
        String sourceRef = ((NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(source.port.getContainer())).getReference(
                        sourcePortChannel, false);

        String sinkPortChannel = CodeGeneratorAdapter.generateName(sink.port)
                + "#" + sink.channelNumber + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = ((NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(sink.port.getContainer())).getReference(
                        sinkPortChannel, true);

        // When the sink port is contained by a modal controller, it is
        // possible that the port is both input and output port. we need
        // to pay special attention. Directly calling getReference() will
        // treat it as output port and this is not correct.
        // FIXME: what about offset?
        if (sink.port.getContainer() instanceof ModalController) {
            sinkRef = CodeGeneratorAdapter.generateName(sink.port);
            if (sink.port.isMultiport()) {
                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
            }
        }

        String result = sourceRef;

        String sourceCodeGenType = getCodeGenerator().codeGenType(sourceType);
        String sinkCodeGenType = getCodeGenerator().codeGenType(sinkType);

        if (!sinkCodeGenType.equals(sourceCodeGenType)) {
            result = "$convert_" + sourceCodeGenType + "_" + sinkCodeGenType
                    + "(" + result + ")";
        }
        return sinkRef + " = " + result + ";" + _eol;
    }

    /** Traverse all the entities in the model and place them in the sensors
     *  and actuators variables.
     * @exception IllegalActionException Thrown in derived classes.
     */
    protected void _modelStaticAnalysis() throws IllegalActionException {

        //for (Actor actor : (List<Actor>) ((CompositeActor) _director
        //  .getContainer()).deepEntityList()) {
        // FIXME: should I be using Interrupt/ActuationDevice or just Input/OutputDevice?
        //            if (actor instanceof ActuatorSetup) {
        //                actuators.put(actor, Integer.valueOf(actuatorIndex));
        //                actuatorIndex++;
        //            }
        //
        //            if (actor instanceof SensorHandler) {
        //                sensors.put(actor, Integer.valueOf(sensorIndex));
        //                sensorIndex++;
        //            }
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Fire methods for each actor.
     * @return fire methods for each actor
     * @exception IllegalActionException If thrown when getting the port's adapter.
     */
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
            //
            //            if (actor instanceof SensorHandler) {
            //                code.append("void Sensing_"
            //                        + CodeGeneratorAdapter.generateName((NamedObj) actor)
            //                        + "() {" + _eol);
            //                code.append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.InputDevice) adapter)
            //                        .generateSensorSensingFuncCode());
            //                code.append("}" + _eol);
            //            }
            code.append("void "
                    + CodeGeneratorAdapter.generateName((NamedObj) actor)
                    + "() " + "{" + _eol);
            code.append(adapter.generateFireCode());

            // After each actor firing, the Event Head ptr needs to point to null
            code.append(_generateClearEventHeadCode(actor));
            code.append("}" + _eol);
        }

        return code.toString();
    }

    /** This code reset the Event_Head pointer for each channel to null.
     * @param actor The actor which the input channels reside, whose
     * pointers are pointed to null
     * @return The code that clears the event head.
     * @exception IllegalActionException If thrown while getting the
     * width of a port.
     */
    protected String _generateClearEventHeadCode(Actor actor)
            throws IllegalActionException {
        // FIXME: There exists actors that are both sensor, and also
        // receive input from the outside. For this reason, we allocate
        // pointers to events for all actors with input ports. This
        // means there exists declared pointers that are never used.
        // if the actor is an input device, the input is fake.
        /*if (actor instanceof InputDevice) {
            return "";
        }*/
        StringBuffer code = new StringBuffer();
        code.append("/* generate code for clearing Event Head buffer. */"
                + _eol);
        for (IOPort inputPort : (List<IOPort>) actor.inputPortList()) {
            for (int channel = 0; channel < inputPort.getWidth(); channel++) {
                code.append("Event_Head_"
                        + CodeGeneratorAdapter.generateName(inputPort) + "["
                        + channel + "] = NULL;" + _eol);
            }
        }
        return code.toString();
    }

    /** Generate a pointer to the event head.
     *  @return a pointer to the event head
     *  @exception IllegalActionException If thrown while getting the inputs
     *  or reading the width of the inputs.
     */
    protected String _generatePtrToEventHeadCodeInputs()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        for (Actor actor : (List<Actor>) ((CompositeActor) _director
                .getContainer()).deepEntityList()) {
            // if (!(actor instanceof InputDevice)) {
            // FIXME: There exists actors that are both sensor, and also
            // receive input from the outside. For this reason, we allocate
            // pointers to events for all actors with input ports. This
            // means there exists declared pointers that are never used.
            for (IOPort inputPort : (List<IOPort>) actor.inputPortList()) {
                if (inputPort.getWidth() > 0) {
                    code.append("Event* Event_Head_"
                            + CodeGeneratorAdapter.generateName(inputPort)
                            + "[" + inputPort.getWidth() + "] = {NULL");
                    for (int channel = 1; channel < inputPort.getWidth(); channel++) {
                        code.append(", NULL");
                    }
                    code.append("};" + _eol);
                }
            }
            //}
        }
        return code.toString();
    }
}
