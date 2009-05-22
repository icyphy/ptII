/* Code generator helper class associated with the PidesDirector class.

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
package ptolemy.codegen.c.targets.luminary.domains.ptides.kernel;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////
//// PtidesEmbeddedDirector

/**
 Code generator helper associated with the PtidesDirector class.
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

 @author Jia Zou, Isaac Liu
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (jiazou)
 @Pt.AcceptedRating
 */
public class PtidesBasicDirector extends ptolemy.codegen.c.domains.ptides.kernel.PtidesBasicDirector {

    /**
     * Construct the code generator helper associated with the given
     * PtidesDirector.
     * @param ptidesEmbeddedDirector The associated
     *  ptolemy.domains.ptides.kernel.PtidesEmbeddedDirector
     */
    public PtidesBasicDirector(ptolemy.domains.ptides.kernel.PtidesBasicDirector ptidesBasicDirector) {
        super(ptidesBasicDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

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
        //CompositeActor compositeActor =
        //    (CompositeActor) _director.getContainer();

        code.append(_codeGenerator.comment("Create a task for each actor."));

        //List<Actor> actorList = compositeActor.deepEntityList();

        /* The xTaskCreate() function takes as parameters a task function
         * pointer, the task name, a stack size value, a pointer to
         * the task parameters, a value for task priority, and the task
         * handle which would be assigned upon the return of the function.
         * The vTaskStartScheduler() is generated after the xTaskCreate()
         * calls. It starts the task scheduler once every task is created.
         */
        /*
        for (Actor actor : actorList) {
            code.append("xTaskCreate(");
            code.append(_getActorTaskLabel(actor));         // task function.
            code.append(", \"" + actor.getDisplayName());   // task string label.
            code.append("\", " + _getStackSize(actor));     // stack depth.
            code.append(", NULL");                          // pointer to task parameters
            code.append(", " + _getPriority(actor));        // priority.
            code.append(", task_" +                        // task handle.
                    _getActorTaskLabel(actor) + ");" + _eol);
        }

        code.append("vTaskStartScheduler();" + _eol);
         */
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

        code.append(_codeGenerator
                .comment("Initialization code of the PtidesDirector."));

        code.append(_codeStream.getCodeBlock("initPDBlock"));
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

        code.append(CodeGeneratorHelper.generateName(_director
                .getContainer()) + "();" + _eol);

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

        List args = new LinkedList();
        args.add(_generateDirectorHeader());

        args.add(((CompositeActor)
                _director.getContainer()).deepEntityList().size());

        code.append(super.generatePreinitializeCode());

        code.append(_codeStream.getCodeBlock("preinitPDBlock", args));
        
        code.append(_codeStream.getCodeBlock("initPDCodeBlock"));
        
        code.append(_generateInitializeHardwareCode());

        return code.toString();
    }

    /**
     * Generate code for transferring tokens into a composite.
     *

     * @param inputPort The port to transfer tokens.
     * @param code The string buffer that the generated code is appended to.
     * @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
    throws IllegalActionException {
        /* FIXME: So far, the only possible composition using PN
         * is PN inside PN. In this case, we should generate one cross-level
         * queue rather than two. This method override the base
         * class method and generate no extra code.
         */
        return;
    }

    /**
     * Generate code for transferring tokens outside of a composite.
     *
     * @param port The specified port.
     * @param code The given code buffer.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void generateTransferOutputsCode(IOPort port, StringBuffer code)
    throws IllegalActionException {
        /* FIXME: So far, the only possible composition using PN
         * is PN inside PN. In this case, we should generate one cross-level
         * queue rather than two. This method override the base
         * class method and generate no extra code.
         */
        return;
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
     * Return the buffer size to generate the variable for the
     * specified port channel.
     * This number dictates the size of the array generated for a variable
     * associated with the port channel. This returns 1, since queuing
     * is done using a separate structure.
     * @param port The specified port
     * @param channelNumber The specified channel number.
     * @return The buffer size to generate the variable for the
     *  specified port channel. In this case, it's 1.
     * @exception IllegalActionException Not thrown in this class.
     */
    public int getBufferSize(IOPort port, int channelNumber)
    throws IllegalActionException {
        // FIXME: Reference with offset larger than 1 will not work
        // (e.g. $ref(port, 3)).
        return 1;
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
    protected String _generateInitializeHardwareCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // FIXME: output initialization always needs to happen before input initialization.
        code.append("void initializeHardware() {" + _eol);
        for (Actor actor : _actuators.keySet()) {
            code.append(((ptolemy.codegen.c.domains.ptides.lib.OutputDevice)_getHelper(actor)).generateHardwareInitializationCode());
        }
        for (Actor actor : _sensors.keySet()) {
            code.append(((ptolemy.codegen.c.domains.ptides.lib.InputDevice)_getHelper(actor)).generateHardwareInitializationCode());
        }
        
        code.append("}" + _eol);
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
