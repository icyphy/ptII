/* Modular code generator adapter class associated with the SDFDirector class.

 Copyright (c) 2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////
////SDFDirector

/**
 Modular code generator adapter associated with the SDFDirector class. This class
 is also associated with a code generator.

 @author Dai Bui, Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */

package ptolemy.cg.adapter.generic.program.procedural.java.modular.adapters.ptolemy.domains.sdf.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.lib.ModularCodeGenTypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
////SDFDirector

/** Class for modular code generator.
*
*  @author Dai Bui, Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 8.0
*  @Pt.ProposedRating red (rodiers)
*  @Pt.AcceptedRating red (daib)
*/

public class SDFDirector
        extends
        ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.kernel.SDFDirector {

    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
        // TODO Auto-generated constructor stub
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    /** Generate the code for the firing of actors according to the SDF
     *  schedule.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the SDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be
     *   fired cannot find its associated adapter.
     */
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "The firing of the StaticSchedulingDirector")));
        boolean inline = ((BooleanToken) getCodeGenerator().inline.getToken())
                .booleanValue();

        // Generate code for one iteration.
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();
            
            // FIXME: Before looking for a adapter class, we should check to
            // see whether the actor contains a code generator attribute.
            // If it does, we should use that as the adapter.
            ProgramCodeGeneratorAdapter adapter = (ProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter((NamedObj) actor);

            if (actor instanceof ModularCodeGenTypedCompositeActor) {
                //call the internal code generator of the composite actor
                try {
                    ((ModularCodeGenTypedCompositeActor) actor).generateCode();
                } catch (KernelException e) {
                    throw new IllegalActionException(actor, e, "Can't generate code for " + actor.getName());
                }
                
                String className = ProgramCodeGeneratorAdapter.generateName((NamedObj) actor);
                String actorName = ModularCodeGenTypedCompositeActor.classToActorName(className);
                
                code.append(actorName + ".fire(");
                
                ProgramCodeGeneratorAdapter codegeneratorAdaptor = getAdapter((NamedObj)actor);
                
                Iterator<?> inputPorts = actor.inputPortList()
                .iterator();
                boolean addComma = false;
                while (inputPorts.hasNext()) {
                    TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                    if (addComma) {
                        code.append(", ");
                    }
                    
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        code.append(codegeneratorAdaptor.getReference( inputPort.getName() + "#" + i));
                    }
                    
                    addComma = true;
                }
        /*
                Iterator<?> outputPorts = actor.outputPortList()
                .iterator();
                while (outputPorts.hasNext()) {
                    TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
                    if (addComma) {
                        code.append(", ");
                    }
                    
                    for (int i = 0; i < outputPort.getWidth(); i++) {
                        code.append(codegeneratorAdaptor.getReference( outputPort.getName() + "#" + i));
                    }
                    
                    addComma = true;
                }
                */
                
                code.append(");"+ _eol);
                
                //transfer to external ports
                Iterator<?> outputPorts = actor.outputPortList()
                .iterator();
                while (outputPorts.hasNext()) {
                    TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
                    
                    for (int i = 0; i < outputPort.getWidth(); i++) {
                        code.append(codegeneratorAdaptor.getReference( outputPort.getName() + "#" + i) + 
                                " = " + actorName + "." + codegeneratorAdaptor.getReference( "@" + outputPort.getName() + "#" + i) + ";" + _eol);
                    }
                }
                
            } else {
                if (inline) {
                    for (int i = 0; i < firing.getIterationCount(); i++) {
    
                        // generate fire code for the actor
                        code.append(adapter.generateFireCode());
    
                        _generateUpdatePortOffsetCode(code, actor);
                    }
                } else {
    
                    int count = firing.getIterationCount();
                    if (count > 1) {
                        code.append("for (int i = 0; i < " + count + " ; i++) {"
                                + _eol);
                    }
    
                    code.append(generateName((NamedObj) actor)
                            + "();" + _eol);
    
                    _generateUpdatePortOffsetCode(code, actor);
    
                    if (count > 1) {
                        code.append("}" + _eol);
                    }
                }
            }
        }
        return code.toString();
    }
    
    
    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);


            // FindBugs wants this instanceof check.
            if (!(inputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(inputPort, null,
                        " is not an instance of TypedIOPort.");
            }

            String portName = inputPort.getName();

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {

                    String portNameWithChannelNumber = portName;
                    if (inputPort.isMultiport()) {
                        portNameWithChannelNumber = portName + '#' + i;
                    }
                    if (rate > 1) {
                        for (int k = 0; k < rate; k++) {
                            code.append(compositeActorAdapter.getReference("@"
                                    + portNameWithChannelNumber + "," + k));
                            code.append(" = " + portName + "_" + i + "[" + k
                                    + "];" + _eol);
                        }
                    } else {
                        code.append(compositeActorAdapter.getReference("@"
                                + portNameWithChannelNumber));
                        code.append(" = " + portName + "_" + i + ";" + _eol);
                    }
                    
                }
            }

        // Generate the type conversion code before fire code.
        code.append(compositeActorAdapter.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }
    
    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
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
        
        code.append(super.generateVariableDeclaration());
        
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();
            
            if (actor instanceof ModularCodeGenTypedCompositeActor) {
                String className = ProgramCodeGeneratorAdapter.generateName((NamedObj) actor);
                String actorName = ModularCodeGenTypedCompositeActor.classToActorName(className);
                
                code.append(className + " " + actorName + ";" + _eol);
            }
        }
        
        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableInitialization() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateVariableInitialization());
        
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();
            
            if (actor instanceof ModularCodeGenTypedCompositeActor) {
                //call the internal generated code of the composite actor
                
                String className = ProgramCodeGeneratorAdapter.generateName((NamedObj) actor);
                String actorName = ModularCodeGenTypedCompositeActor.classToActorName(className);
                
                code.append(actorName + " = new " + className + "();" + _eol);
                
                code.append(actorName + ".initialize();" + _eol);
            }
        }
        
        return code.toString();
    }
    
    //////////////////////////////////////////////////////////////////////
    ////                         private methods                      ////

}
