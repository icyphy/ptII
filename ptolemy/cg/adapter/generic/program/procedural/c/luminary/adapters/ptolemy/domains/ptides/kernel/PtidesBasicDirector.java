/* Code generator adapter class associated with the PtidesBasicDirector class.

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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.ptides.kernel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.domains.ptides.lib.luminary.LuminarySensorInputDevice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
//// PtidesBasicDirector

/**
 Code generator helper associated with the PtidesBasicDirector class.
 This helper generates Luminary specific code.

 This director starts a task for each actor. Each task has a specified
 name, stack size, priority and function code to execute. User can introduce
 annotations in an actor to specify these values. In particular, this
 helper class looks for the "_stackSize" and "_priority" parameters and
 use their values to create the tasks. If these parameters are not specified,
 the code generator uses the default value 80 for stack size, and 0 for
 priority.

 Each task executes a given function which consists of the actor initialization,
 fire and wrapup code.
 @author Jia Zou, Isaac Liu, Jeff C. Jensen
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating red (jiazou)
 @Pt.AcceptedRating red (jiazou)
 */
public class PtidesBasicDirector
        extends
        ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesBasicDirector {

    /** Construct the code generator adapter associated with the given
     *  PtidesBasicDirector.
     *  @param ptidesBasicDirector The associated director
     *  ptolemy.domains.ptides.kernel.PtidesBasicDirector
     */
    public PtidesBasicDirector(
            ptolemy.domains.ptides.kernel.PtidesBasicDirector ptidesBasicDirector) {
        super(ptidesBasicDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the assembly file associated for this PtidyOS program.
     *  Here we return an empty string, but the target specific adapter
     *  should overwrite it.
     *  @return The generated assembly file code.
     *  @exception IllegalActionException
     */
    public StringBuffer generateAsseblyFile() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // Get all actors that are interruptDevices. Then for each of these actors,
        // generate a name for it, and put the name along with this actor into a
        // HashMap.
        Map devices = new HashMap<Actor, String>();
        for (Actor actor : (List<Actor>) ((TypedCompositeActor) getComponent().getContainer())
                .deepEntityList()) {
            // If the input is a sensor device, then we need to use interrupts to trigger it.
            if (actor instanceof LuminarySensorInputDevice) {
                devices.put(actor, new String("Sensing_"
                        + NamedProgramCodeGeneratorAdapter
                                .generateName((NamedObj) actor)));
            }
        }

        // List of args used to get the template.
        List args = new LinkedList();

        // The first element in the args should be the externs. For each device in the set,
        // we need to add an external method.
        StringBuffer externs = new StringBuffer();
        for (Actor actor : (Set<Actor>) devices.keySet()) {
            externs.append("        EXTERN  " + devices.get(actor) + _eol);
        }
        args.add(externs.toString());

        // Now we create an array for each device. The length of the array should be the number of
        // supported configurations in this device. For each actor that fits a device and
        // a particular configuration, add it into the array associated with the device, 
        // and the index of this actor should equal to the index of the configuration in
        // supportedConfigurations().
        int configurationSize = LuminarySensorInputDevice.numSupportedInputDeviceConfigurations;
        String[] GPHandlers = new String[configurationSize];
        boolean foundConfig = false;
        for (LuminarySensorInputDevice actor : (Set<LuminarySensorInputDevice>) devices.keySet()) {
            for (int i = 0; i < actor.supportedConfigurations().size(); i++) {
                if (actor.configuration().compareTo(actor
                        .supportedConfigurations().get(i)) == 0) {
                    GPHandlers[i + actor.startingConfiguration()] = (String) devices.get(actor);
                    foundConfig = true;
                    break;
                }
            }
            if (foundConfig == false) {
                throw new IllegalActionException(actor, "Cannot found the configuration for this " +
                		"actor.");
            }
        }
        for (int i = 0; i < configurationSize; i++) {
            // If there is nothing in this array for this index, then we use IntDefaultHandler.
            if (GPHandlers[i] == null) {
                args.add("IntDefaultHandler");
            } else {
                args.add(GPHandlers[i]);
            }
        }

        // In the future if we add more devices, then it should be a derivation of the above code.
        code.append(_templateParser.getCodeStream().getCodeBlock(
                "assemblyFileBlock", args));

        return code;
    }

    /**
     * Generate the director fire code.
     * The code creates a new task for each actor according to
     * their specified parameters (e.g. stack depth, priority,
     * and etc.). The code also initiates the task scheduler.
     * @return The generated fire code.
     * @exception IllegalActionException Not thrown in this class.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code
                .append(getCodeGenerator().comment(
                        "Create a task for each actor."));

        code.append("while (true);" + _eol);
        return code.toString();
    }

    /**
     * Generate the fire function code.
     * The code contains the function code for each actor. It is a collection
     * of global functions, one for each actor that is visible to this
     * director helper. Creating each new task requires one of these
     * function as parameter. It is the code that the task executes.
     * When the inline parameter is checked, the task function code is
     * generated in {@link #generatePreinitializeCode()} which is
     * outside the main function.
     * @return The fire function code.
     * @exception IllegalActionException If there is an exception in
     *  generating the task function code.
     */
    public String generateFireFunctionCode() throws IllegalActionException {
        return "";
    }

    /**
     * Generate the initialize code.
     * This generates the hardware initialization code and creates
     * the queues for all referable port channels.
     * @return The generated initialize code.
     * @exception IllegalActionException If the helper associated with
     *  an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(getCodeGenerator().comment(
                "Initialization code of the PtidesDirector."));

        code.append(_templateParser.getCodeStream().getCodeBlock("initPDBlock"));
        code.append(super.generateInitializeCode());

        return code.toString();
    }

    /**
     * Generate the main loop code.
     * @return The main loop code.
     * @exception IllegalActionException If looking up the inline
     *  parameter or generating the fire code throws it.
     */
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(NamedProgramCodeGeneratorAdapter.generateName(_director
                .getContainer())
                + "();" + _eol);

        return code.toString();
    }

    /** Generate the preinitialize code for the associated Ptides director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating preinitialize code for the actor.
     *   FIXME: Take care of platform dependent code.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(super.generatePreinitializeCode());

        code.append(_templateParser.getCodeStream().getCodeBlock(
                "preinitPDBlock"));

        code.append(_templateParser.getCodeStream().getCodeBlock(
                "initPDCodeBlock"));

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
    public String generateVariableInitialization()
            throws IllegalActionException {
        return "";
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
        Set sharedCode = super.getSharedCode();
        return sharedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the offset variables for the given port.
     *  Since built-in queue structure is used, it eliminates the
     *  need for offset variables. Instead, this method generates
     *  the declaration of the queues if the specified port has any
     *  referable port channels. There is one referable port channel
     *  for each connection. All input port channels are considered
     *  referable.
     *  @param port The specified port.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the code block
     *   throws it.
     */
    protected String _createDynamicOffsetVariables(TypedIOPort port)
            throws IllegalActionException {
        /* FIXME: make it useful such that we know which event offset to write to
           StringBuffer code = new StringBuffer();
           code.append(_eol + _codeGenerator.comment(
           "PtidesDirector's queue declarations."));

           int width;
           if (port.isInput()) {
               width = port.getWidth();
           } else {
               width = 0;
           }

           // Declare a queue variable for each channel.
           if (width != 0) {

               List args = new LinkedList();
               args.add("");

               for (int i = 0; i < width; i++) {
                   args.set(0, _generateQueueReference(port, i));
                   code.append(_codeStream.getCodeBlock("declareBufferHeader", args));

                   // Keep track of the queues.
                   _queues.add(new Channel(port, i));
               }
           }
           return code.toString()*/
        return "";
    }

    /** Generate the initialization code for any hardware component that is used.
     *  @return code initialization code for hardware peripherals
     *  @throws IllegalActionException
     */
    protected String _generateInitializeHardwareCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // FIXME: output initialization always needs to happen before input initialization.
        code.append("void initializeHardware() {" + _eol);
        for (Actor actor : _actuators.keySet()) {
            code
                    .append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.OutputDevice) getAdapter(actor))
                            .generateHardwareInitializationCode());
        }
        for (Actor actor : _sensors.keySet()) {
            code
                    .append(((ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.lib.InputDevice) getAdapter(actor))
                            .generateHardwareInitializationCode());
        }

        code.append("}" + _eol);
        return code.toString();
    }

}
