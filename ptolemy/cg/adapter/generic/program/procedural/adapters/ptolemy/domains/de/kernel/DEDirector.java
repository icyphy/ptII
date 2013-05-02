/* Code generator adapter class associated with the DEDirector class.

 Copyright (c) 2009-2013 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.de.kernel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.DFUtilities;
import ptolemy.actor.util.Time;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;

///////////////////////////////////////////////////////////////////
////DEDirector

/**
 * Code generator adapter associated with the DEDirector class. 
 * This adapter is highly experimental since it changes a lot 
 * of behaviors of the code generation process.
 * This class is also associated with a code generator.
 *
 *  @author William Lucas based on SDFDirector.java by Ye Zhou, Gang Zhou
 *  @version $Id$
 *  @since Ptolemy II 9.1
 *  @Pt.ProposedRating red (wlc)
 *  @Pt.AcceptedRating red (wlc)
*/

public class DEDirector extends Director {

    /** Construct the code generator adapter associated with the given
     *  DEDirector.
     *  @param deDirector The associated
     *  ptolemy.domains.de.kernel.DEDirector
     */
    public DEDirector(ptolemy.domains.de.kernel.DEDirector deDirector) {
        super(deDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return whether the channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  @return True when the channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    final public Boolean allowDynamicMultiportReference()
            throws IllegalActionException {
        return false;
        // FIXME : temporary, we never allow dynamic multiport reference
        //return ((BooleanToken) ((Parameter) getComponent()
        //        .getDecoratorAttribute(getCodeGenerator(),
        //                "allowDynamicMultiportReference")).getToken())
        //        .booleanValue();
    }

    /** Generate a the C and H code for all the actors.
     * It returns a table with the names of the actors, their C code, 
     * and their H code.
     *  
     *  @return Code for the actors.
     *  @exception IllegalActionException If something goes wrong while reading
     *  actors Templates.
     */
    public String[] generateActorCode() throws IllegalActionException {
        List actorList = ((CompositeActor) _director.getContainer())
                .deepEntityList();
        int size = actorList.size();
        String codeTable[] = new String[3*size];
        
        // Sort by name so that we retrieve the actors from the list
        // by composite.
    
        Collections.sort(actorList, new FullNameComparator());
    
        ProgramCodeGenerator codeGenerator = getCodeGenerator();
        //HashMap<String, StringBuffer> innerClasses = new HashMap<String, StringBuffer>();
        Iterator<?> actors = actorList.iterator();
        int i = 0;
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter actorAdapter = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(actor);
            
            StringBuffer codeActor = new StringBuffer();
            StringBuffer codeActorH = new StringBuffer();
    
            codeActor.append("#include \"" + actor.getName() + ".h\"");
            
            codeActorH.append("#ifndef DEFINE_" + actor.getName().toUpperCase() + "_H");
            codeActorH.append(_eol + "#define DEFINE_" + actor.getName().toUpperCase() + "_H");
            codeActorH.append(_eol + "#include \"main.h\"");
            codeActorH.append(_eol + "#include \"types.h\"");
            codeActorH.append(_eol + "#include <stdbool.h>");
            
            //code.append(_eol + "void "+ actor.getName() + "PreinitializeCode() {");
            codeActorH.append(_eol + actorAdapter.generatePreinitializeCode());
            //code.append(_eol + "}" + _eol);
    
            codeActor.append(_eol + "bool " + actor.getName() + "PrefireCode() {");
            codeActor.append(_eol + actorAdapter.generatePrefireCode());
            codeActor.append(_eol + "return true;" + _eol);
            codeActor.append(_eol + "}" + _eol);
            codeActorH.append(_eol + "bool " + actor.getName() + "PrefireCode();");
    
            codeActor.append(_eol + "void " + actor.getName() + "FireCode() {");
            codeActor.append(_eol + actorAdapter.generateFireCode());
            codeActor.append(_eol + "}" + _eol);
            codeActorH.append(_eol + "void " + actor.getName() + "FireCode();");
    
            codeActor.append(_eol + "void " + actor.getName() + "PostfireCode() {");
            codeActor.append(_eol + actorAdapter.generatePostfireCode());
            codeActor.append(_eol + "}" + _eol);
            codeActorH.append(_eol + "void " + actor.getName() + "PostfireCode();");
            
            codeActor.append(_eol + "void " + actor.getName() + "InitializeCode() {");
            codeActor.append(_eol + actorAdapter.generateInitializeCode());
            codeActor.append(_eol + _generateVariableInitialization(actorAdapter));
            codeActor.append(_eol + "}" + _eol);
            codeActorH.append(_eol + "void " + actor.getName() + "InitializeCode();");
            
            codeActorH.append(_eol + _generateVariableDeclaration(actorAdapter));
            
            codeActorH.append(_eol + "#endif");
            
            codeTable[i] = actor.getName();
            codeTable[i+1] = codeActor.toString();
            codeTable[i+2] = codeActorH.toString();
            
            i += 3;
        }
        
        return codeTable;
    }

    /** Generate The fire loop function code. This method calls fire() for in a loop
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireLoopFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        code.append(_eol + "int i = 1;");
        code.append(_eol + "while (i==1) {");
        code.append(_eol + "    int result = DEDirectorFire(&director);");
        code.append(_eol
                + "    DEEvent * nextEvent = CQueueGet(&(director.cqueue));");
        code.append(_eol + "    if (result == 1) {");
        code.append(_eol + "        continue;");
        code.append(_eol
                + "    } else if (result == -1 || nextEvent == NULL) {");
        code.append(_eol + "		director.noMoreActorToFire = true;");
        code.append(_eol + "        return;");
        code.append(_eol + "    } ");
        // else if 0, keep executing

        // if the next event is in the future break the loop !
        code.append(_eol
                + "    if (nextEvent->timestamp > director.currentModelTime ||"
                + "(nextEvent->timestamp == director.currentModelTime &&"
                + "nextEvent->microstep > director.currentMicrostep)) {");
        code.append(_eol + "        break;");
        code.append(_eol + "    } ");// else keep executing in the current iteration
        code.append(_eol + "} ");// Close the BIG while loop.

        return code.toString();
    }

    /** Generate the call to the initialize function of the DE director
     *  @return The initialize function code.
     *  @exception IllegalActionException If thrown while generating initialize code.
     */
    public String generateInitializeCode() throws IllegalActionException {
        return "DEDirectorInitialize();";
    }

    /** Generate the initialize code for the associated DE director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeFunctionCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ptolemy.actor.Director director = (ptolemy.actor.Director) getComponent();
        List actorList = ((CompositeActor) _director.getContainer())
                .deepEntityList();
        // Sort by name so that we retrieve the actors from the list
        // by composite.

        Collections.sort(actorList, new FullNameComparator());

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        // Adding the handle in order to find function pointers with their names
        //code.append(_eol + "void * handle = dlopen(NULL, RTLD_LAZY);");

        //HashMap<String, StringBuffer> innerClasses = new HashMap<String, StringBuffer>();
        code.append(_eol + _eol
                + codeGenerator.comment("Initialization of the director"));
        code.append(_eol + "director.startTime ="
                + director.getModelStartTime() + ";");
        if (director.getModelStopTime().compareTo(Time.POSITIVE_INFINITY) == 0) {
            code.append(_eol + "director.stopTime = Infinity;");
        }
        else
            code.append(_eol + "director.stopTime =" + director.getModelStopTime() + ";");
        code.append(_eol + _eol + "director.cqueue = *(newCQueue());" + _eol);
        code.append(_eol + _eol + "director.actors = calloc("
                + actorList.size() + ", sizeof(Actor*));");
        code.append(_eol + "if (director.actors == NULL)");
        code.append(_eol + "    perror(\"Allocation problem (global)\");");

        code.append(_eol + _eol + "IOPort ** inPorts = NULL;");
        code.append(_eol + "IOPort ** outPorts = NULL;");

        // Declare all the actors in the director structure
        int i = 0;
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            List inputPorts = actor.inputPortList();
            int j = 0;
            code.append(_eol + "inPorts = NULL;");
            code.append(_eol + _eol + "inPorts = calloc(" + inputPorts.size()
                    + ", sizeof(IOPort*));");
            code.append(_eol + "if (inPorts == NULL)");
            code.append(_eol + "    perror(\"Allocation problem (global)\");");
            Iterator<?> inPorts = inputPorts.iterator();
            while (inPorts.hasNext()) {
                TypedIOPort inPort = (TypedIOPort) inPorts.next();
                code.append(_eol + "inPorts[" + j + "] = newIOPortWithParam(\""
                        + inPort.getName() + "\", \"" + inPort.getType()
                        + "\", " + inPort.isInput() + ", "
                        + inPort.isMultiport() + ", " + inPort.getWidth()
                        + ");");
                j++;
            }

            List outputPorts = actor.outputPortList();
            j = 0;
            code.append(_eol + "outPorts = NULL;");
            code.append(_eol + "outPorts = calloc(" + outputPorts.size()
                    + ", sizeof(IOPort*));");
            code.append(_eol + "if (outPorts == NULL)");
            code.append(_eol + "    perror(\"Allocation problem (global)\");");
            Iterator<?> outPorts = outputPorts.iterator();
            while (outPorts.hasNext()) {
                TypedIOPort outPort = (TypedIOPort) outPorts.next();
                // TODO : add listeners later
                code.append(_eol + "outPorts[" + j
                        + "] = newIOPortWithParam(\"" + outPort.getName()
                        + "\", \"" + outPort.getType() + "\", "
                        + outPort.isInput() + ", " + outPort.isMultiport()
                        + ", " + outPort.getWidth() + ");");
                j++;
            }

            code.append(_eol + "director.actors[" + i
                    + "] = newActorWithParam(\"" + actor.getName()
                    + "\", inPorts, outPorts);");

            //code.append(_eol + "director.actors["+i+"]->preInitializeFunction = " + actor.getName() + "PreinitializeCode;");
            code.append(_eol + "director.actors[" + i
                    + "]->initializeFunction = " + actor.getName()
                    + "InitializeCode;");
            code.append(_eol + "director.actors[" + i + "]->prefireFunction = "
                    + actor.getName() + "PrefireCode;");
            code.append(_eol + "director.actors[" + i + "]->fireFunction = "
                    + actor.getName() + "FireCode;");
            code.append(_eol + "director.actors[" + i
                    + "]->postfireFunction = " + actor.getName()
                    + "PostfireCode;");
            code.append(_eol
                    + codeGenerator.comment("initialization of the actor : "
                            + actor.getName()));
            code.append(_eol + actor.getName() + " = director.actors[" + i
                    + "];");
            CompositeActor container = (CompositeActor) director.getContainer();
            CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                    .getCausalityInterface();
            int depth = causality.getDepthOfActor(actor);
            code.append(_eol + "director.actors[" + i + "]->depth = " + depth
                    + ";");
            code.append(_eol + "(*(director.actors[" + i
                    + "]->initializeFunction))();");
            i++;
        }

        // TODO : for now, the container actor is Top level we can correct it easily
        code.append(_eol + _eol
                + "director.containerActor = newActorWithParam(\""
                + director.getContainer().getName() + "\", NULL, NULL);");

        code.append(_eol + "director.currentModelTime = director.startTime;");
        code.append(_eol + "director.currentMicrostep = 0;");
        code.append(_eol + "director.noMoreActorToFire = false;");
        Attribute stopWhenQueueIsEmpty = director.getAttribute("stopWhenQueueIsEmpty");
        boolean stopWhenQueueIsEmptyBool = ((BooleanToken) ((Variable) stopWhenQueueIsEmpty).getToken()).booleanValue();
        code.append(_eol + "director.stopWhenQueueIsEmpty = "+ stopWhenQueueIsEmptyBool +";");
        code.append(_eol + "director.exceedStopTime = false;");

        code.append(_eol + "container = director.containerActor;");

        //code.append(super.generateInitializeCode());

        // Register the stop time as an event such that the model is
        // guaranteed to stop at that time. This event also serves as
        // a guideline for an embedded Continuous model to know how much
        // further to integrate into future. But only do this if the
        // stop time is finite.
        if (!director.getModelStopTime().isPositiveInfinite()) {
            code.append(_eol + "DEDirectorFireAt(&director, container, "
                    + "director.stopTime, 1);");
        }

        code.append(_eol + "director.isInitializing = false;");
        code.append(_eol
                + codeGenerator
                        .comment("End of the Initialization of the director"));
        return code.toString();
    }

    /** Generate a main loop for an execution under the control of
     *  this DE director. 
     *  
     *  @return Code for the main loop of an execution.
     *  @exception IllegalActionException If something goes wrong.
     */
    public String generateMainLoop() throws IllegalActionException {
        // Need a leading _eol here or else the execute decl. gets stripped out.
        StringBuffer code = new StringBuffer();

        code.append(_eol + "bool DEDirectorPrefire() {" + _eol);
        code.append(generatePreFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("bool DEDirectorPostfire() {" + _eol);
        code.append(generatePostFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("void DEDirectorFireLoop() {" + _eol);
        code.append(generateFireLoopFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void DEDirectorInitialize() {" + _eol);
        code.append(generateInitializeFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + getCodeGenerator().getMethodVisibiliyString()
                + " void execute() "
                + getCodeGenerator().getMethodExceptionString() + " {" + _eol);

        // TODO : Type resolving should have been made before. 
        //resolveTypes();

        //code.append(_eol + "DEDirectorInitialize();");

        code.append(_eol + "bool result = false;");
        code.append(_eol + "int iterationCount = 0;");
        code.append(_eol + "while (!result) {");
        code.append(_eol + "    iterationCount++;");
        code.append(_eol + "    if (DEDirectorPrefire()) {");
        code.append(_eol + "        DEDirectorFireLoop();");
        code.append(_eol + "        result = DEDirectorPostfire();");
        code.append(_eol + "    }");
        code.append(_eol + "}");

        code.append(_eol + "return;");
        code.append(_eol + "}" + _eol);
        
        return code.toString();
    }
    
    /**
     * Generate sanitized name for the given named object. Remove all
     * underscores to avoid conflicts with systems functions.
     * @param port The port for which the name is generated.
     * @return The sanitized name.
     * @exception IllegalActionException If the variablesAsArrays parameter
     * cannot be read or if the buffer size of the port cannot be read.
     */
    public String generatePortName(TypedIOPort port)
            throws IllegalActionException {

        // FIXME: note that if we have a port that has a character that
        // is santized away, then we will run into problems if we try to
        // refer to the port by the sanitized name.
        String portName = StringUtilities.sanitizeName(port.getFullName());

        // FIXME: Assume that all objects share the same top level. In this case,
        // having the top level in the generated name does not help to
        // expand the name space but merely lengthen the name string.
        //        NamedObj parent = namedObj.toplevel();
        //        if (namedObj.toplevel() == namedObj) {
        //            return "_toplevel_";
        //        }
        //        String name = StringUtilities.sanitizeName(namedObj.getName(parent));
        if (portName.startsWith("_")) {
            portName = portName.substring(1, portName.length());
        }
        portName = TemplateParser.escapePortName(portName);

        if (!((BooleanToken) getCodeGenerator().variablesAsArrays.getToken())
                .booleanValue()) {
            return portName;
        }

        // Get the name of the port that refers to the array of all ports.
        return getCodeGenerator().generatePortName(port, portName,
                _ports.getBufferSize(port));
    }

    /** Generate The postfire function code. 
     *  @return The postfire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generatePostFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // If any output ports still have tokens to transfer,
        // request a refiring at the current time.
        // TODO : not sure if necessary
        //code.append(_eol + "IOPort ** outputPorts = director.currentActor->outputPorts;");
        code.append(_eol + "bool moreOutputsToTransfer = false;");
        /*code.append(_eol + "for (int i = 0 ; i < sizeof(outputPorts)/sizeof(IOPort) ; i++)");
        code.append(_eol + "if (outputPorts[i]->eventsToSend != NULL) {");
        code.append(_eol + "moreOutputsToTransfer = true;");
        code.append(_eol + "break;" + _eol + "}");*/

        // Reset the microstep to zero if the next event is
        // in the future.
        code.append(_eol
                + "if (!CQueueIsEmpty(&(director.cqueue)) && !moreOutputsToTransfer) {");
        code.append(_eol + "DEEvent * next = CQueueGet(&(director.cqueue));");
        code.append(_eol + "if (next->timestamp > director.currentModelTime) {");
        code.append(_eol + "director.currentModelTime = next->timestamp;");
        code.append(_eol + "director.currentMicrostep = 0;");
        code.append(_eol + "}");
        code.append(_eol + "}");

        // Request refiring and/or stop the model.
        // There are two conditions to stop the model.
        // 1. There are no more actors to be fired (i.e. event queue is
        // empty), and either of the following conditions is satisfied:
        //     a. the stopWhenQueueIsEmpty parameter is set to true.
        //     b. the current model time equals the model stop time.
        // 2. The event queue is not empty, but the current time exceeds
        // the stop time.
        code.append(_eol + "bool stop = director.stopWhenQueueIsEmpty;");
        code.append(_eol + "if (moreOutputsToTransfer) {");
        code.append(_eol
                + "DEDirectorFireAt(&director, director.currentActor, director.currentModelTime, 0);");
        code.append(_eol
                + "} else if (director.noMoreActorToFire && (stop || director.currentModelTime >= director.stopTime)) {");
        code.append(_eol + "return true;");
        code.append(_eol + "}");
        code.append(_eol + "return false;" + _eol);

        return code.toString();
    }

    /** Generate The prefire function code. 
     *  @return The prefire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generatePreFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        // TODO : update the local time

        // TODO : define isTopLevel for the case of Composite Actor
        //code.append(_eol + "if (isTopLevel()) {" + _eol + "return true;" + _eol + "}");
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
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol + "DEDirector director;");

        code.append(_eol + "Actor * container;");

        List actorList = ((CompositeActor) _director.getContainer())
                .deepEntityList();

        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            code.append(_eol + "Actor * " + actor.getName() + ";");
        }
        
        // Don't call the superclass because the Preinitialize codes are within the actor's files
        //code.append(super.generatePreinitializeCode());

        // TODO : here deal with the case of a CompositeActor

        /*_createInputBufferSizeAndOffsetMap();

        // For the inside receivers of the output ports.
        _createOutputBufferSizeAndOffsetMap();

        code.append(_createOffsetVariablesIfNeeded());
        */
        return code.toString();
    }
    
    /** We override the super method, because in DE the declaration
     * of the variables are in the actor's files
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        return "";
    }

    /** Get the files needed by the code generated from this adapter class.
     *  Basically here, we include the "standard" C-written declaration
     *  of the DECQEventQueue
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException If something goes wrong.
     */
    public Set<String> getHeaderFiles() throws IllegalActionException {
        HashSet<String> result = new HashSet<String>();
        List actorList = ((CompositeActor) _director.getContainer())
                .deepEntityList();
        // Sort by name so that we retrieve the actors from the list
        // by composite.
    
        Collections.sort(actorList, new FullNameComparator());
    
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            result.add("\"" + actor.getName()+ ".h\"");
        }

        
        return result;
    }

    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *
     * <p>Usually given the name of an input port, getReference(String
     * name) returns a target language variable name representing the
     * input port. Given the name of an output port,
     * getReference(String name) returns variable names representing
     * the input ports connected to the output port.  However, if the
     * name of an input port starts with '@', getReference(String
     * name) returns variable names representing the input ports
     * connected to the given input port on the inside.  If the name
     * of an output port starts with '@', getReference(String name)
     * returns variable name representing the the given output port
     * which has inside receivers.  The special use of '@' is for
     * composite actor when tokens are transferred into or out of the
     * composite actor.</p>
     *
     *  @param name The name of the parameter or port
     *  @param isWrite Whether to generate the write or read offset.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    public String getReference(String name, boolean isWrite,
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        name = processCode(name);
        String castType = _getCastType(name);
        String refName = _getRefName(name);
        String[] channelAndOffset = _getChannelAndOffset(name);

        boolean forComposite = false;
        if (refName.charAt(0) == '@') {
            forComposite = true;
            refName = refName.substring(1);
        }

        TypedIOPort port = target.getTemplateParser().getPort(refName);
        if (port != null) {

            if (port instanceof ParameterPort && port.numLinks() <= 0) {

                // Then use the parameter (attribute) instead.
            } else {
                String result = getReference(port, channelAndOffset,
                        forComposite, isWrite, target);

                String refType = getCodeGenerator().codeGenType(port.getType());

                String returnValue = _templateParser.generateTypeConvertMethod(
                        result, castType, refType);
                return returnValue;
            }
        }

        // Try if the name is a parameter.
        Attribute attribute = target.getComponent().getAttribute(refName);
        if (attribute != null) {
            String refType = _getRefType(attribute);

            String result = _getParameter(target, attribute, channelAndOffset);

            result = _templateParser.generateTypeConvertMethod(result,
                    castType, refType);

            return result;
        }

        throw new IllegalActionException(target.getComponent(),
                "Reference not found: " + name);
    }

    /**
     * Return an unique label for the given port channel referenced
     * by the given adapter. By default, this delegates to the adapter to
     * generate the reference. Subclass may override this method
     * to generate the desire label according to the given parameters.
     * @param port The given port.
     * @param channelAndOffset The given channel and offset.
     * @param forComposite Whether the given adapter is associated with
     *  a CompositeActor
     * @param isWrite The type of the reference. True if this is
     *  a write reference; otherwise, this is a read reference.
     * @param target The ProgramCodeGeneratorAdapter for which code
     * needs to be generated.
     * @return an unique reference label for the given port channel.
     * @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite,
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        boolean dynamicReferencesAllowed = allowDynamicMultiportReference();

        int channelNumber = 0;
        boolean isChannelNumberInt = true;
        if (!channelAndOffset[0].equals("")) {
            // If dynamic multiport references are allowed, catch errors
            // when the channel specification is not an integer.
            if (dynamicReferencesAllowed) {
                try {
                    channelNumber = Integer.valueOf(channelAndOffset[0])
                            .intValue();
                } catch (NumberFormatException ex) {
                    isChannelNumberInt = false;
                }
            } else {
                channelNumber = Integer.valueOf(channelAndOffset[0]).intValue();
            }
        }
        if (!isChannelNumberInt) { // variable channel reference.
            if (port.isOutput()) {
                throw new IllegalActionException(
                        "Variable channel reference not supported"
                                + " for output ports");
            } else {
                String returnValue = _generatePortReference(port,
                        channelAndOffset, isWrite);
                return returnValue;
            }
        }

        StringBuffer result = new StringBuffer();

        // To support modal model, we need to check the following condition
        // first because an output port of a modal controller should be
        // mainly treated as an output port. However, during choice action,
        // an output port of a modal controller will receive the tokens sent
        // from the same port.  During commit action, an output port of a modal
        // controller will NOT receive the tokens sent from the same port.
        if (_checkRemote(forComposite, port)) {
            Receiver[][] remoteReceivers;

            // For the same reason as above, we cannot do: if (port.isInput())...
            if (port.isOutput()) {
                remoteReceivers = port.getRemoteReceivers();
            } else {
                remoteReceivers = port.deepGetReceivers();
            }

            if (remoteReceivers.length == 0) {
                // The channel of this output port doesn't have any
                // sink or is a PortParameter.
                // Needed by $PTII/bin/ptcg -language java -variablesAsArrays true $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortParameterOpaque.xml
                String returnValue = generatePortName(port);
                return returnValue;
            }

            ProgramCodeGeneratorAdapter.Channel sourceChannel = new ProgramCodeGeneratorAdapter.Channel(
                    port, channelNumber);

            List<ProgramCodeGeneratorAdapter.Channel> typeConvertSinks = target
                    .getTypeConvertSinkChannels(sourceChannel);

            List<ProgramCodeGeneratorAdapter.Channel> sinkChannels = getSinkChannels(
                    port, channelNumber);

            boolean hasTypeConvertReference = false;
            for (int i = 0; i < sinkChannels.size(); i++) {
                ProgramCodeGeneratorAdapter.Channel channel = sinkChannels
                        .get(i);
                TypedIOPort sinkPort = (TypedIOPort) channel.port;
                int sinkChannelNumber = channel.channelNumber;

                // Type convert.
                if (typeConvertSinks.contains(channel)
                        && getCodeGenerator().isPrimitive(
                                ((TypedIOPort) sourceChannel.port).getType())) {

                    if (!hasTypeConvertReference) {
                        if (i != 0) {
                            result.append(" = ");
                        }
                        result.append(getTypeConvertReference(sourceChannel));

                        if (dynamicReferencesAllowed && port.isInput()) {
                            if (channelAndOffset[1].trim().length() > 0) {
                                result.append("[" + channelAndOffset[1].trim()
                                        + "]");
                            } else {
                                result.append("["
                                        + _generateChannelOffset(port, isWrite,
                                                channelAndOffset[0]) + "]");
                            }
                        } else {
                            int rate = Math
                                    .max(DFUtilities
                                            .getTokenProductionRate(sourceChannel.port),
                                            DFUtilities
                                                    .getTokenConsumptionRate(sourceChannel.port));
                            if (rate > 1
                                    && channelAndOffset[1].trim().length() > 0) {
                                result.append("[" + channelAndOffset[1].trim()
                                        + "]");
                            }
                        }
                        hasTypeConvertReference = true;
                    } else {
                        // We already generated reference for this sink.
                        continue;
                    }
                } else {
                    if (i != 0) {
                        result.append(" = ");
                    }
                    result.append(generatePortName(sinkPort));

                    if (sinkPort.isMultiport()) {
                        result.append("[" + sinkChannelNumber + "]");
                    }
                    if (channelAndOffset[1].equals("")) {
                        channelAndOffset[1] = "0";
                    }
                    result.append(_ports.generateOffset(sinkPort,
                            channelAndOffset[1], sinkChannelNumber, true));
                }
            }
            return result.toString();
        }

        // Note that if the width is 0, then we have no connection to
        // the port but the port might be a PortParameter, in which
        // case we want the Parameter.
        // codegen/c/actor/lib/string/test/auto/StringCompare3.xml
        // tests this.

        if (_checkLocal(forComposite, port)) {

            result.append(/*NamedProgramCodeGeneratorAdapter.*/generatePortName(port));

            //if (!channelAndOffset[0].equals("")) {
            if (port.isMultiport()) {
                // Channel number specified. This must be a multiport.
                result.append("[" + channelAndOffset[0] + "]");
            }

            result.append(_ports.generateOffset(port, channelAndOffset[1],
                    channelNumber, isWrite));
            return result.toString();
        }

        // FIXME: when does this happen?
        return "";
    }

    /** Return whether we need to pad buffers or not.
     *  @return True when we need to pad buffers.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    final public Boolean padBuffers() throws IllegalActionException {
        return false;
        // FIXME : for now never do it (test)
        //return ((BooleanToken) ((Parameter) getComponent()
        //        .getDecoratorAttribute(getCodeGenerator(), "padBuffers"))
        //        .getToken()).booleanValue();
    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    protected String _generateVariableDeclaration(
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
    
        ProgramCodeGenerator codeGenerator = getCodeGenerator();
    
        String name = CodeGeneratorAdapter.generateName(getComponent());
        // Generate variable declarations for referenced parameters.
        String referencedParameterDeclaration = _generateReferencedParameterDeclaration(target);
        if (referencedParameterDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s referenced parameter declarations."));
            code.append(referencedParameterDeclaration);
        }
    
        // Generate variable declarations for input ports.
        String inputVariableDeclaration = _generateInputVariableDeclaration(target);
        if (inputVariableDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s input variable declarations."));
            code.append(inputVariableDeclaration);
        }
    
        // Generate variable declarations for output ports.
        String outputVariableDeclaration = _generateOutputVariableDeclaration(target);
        if (outputVariableDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s output variable declarations."));
            code.append(outputVariableDeclaration);
        }
    
        // Generate type convert variable declarations.
        String typeConvertVariableDeclaration = _generateTypeConvertVariableDeclaration(target);
        if (typeConvertVariableDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s type convert variable declarations."));
            code.append(typeConvertVariableDeclaration);
        }
    
        return processCode(code.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate variable initialization for the referenced parameters.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    protected String _generateVariableInitialization(
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        //  Generate variable initialization for referenced parameters.
        if (!_referencedParameters.isEmpty()
                && _referencedParameters.containsKey(target)) {
            code.append(_eol
                    + codeGenerator.comment(1, target.getComponent().getName()
                            + "'s parameter initialization"));

            for (Parameter parameter : _referencedParameters.get(target)) {
                try {
                    // avoid duplication.
                    if (!codeGenerator.getModifiedVariables().contains(
                            parameter)) {
                        code.append(GenericCodeGenerator.INDENT1
                                + codeGenerator.generateVariableName(parameter)
                                + " = "
                                + target.getParameterValue(parameter.getName(),
                                        target.getComponent()) + ";" + _eol);
                    }
                } catch (Throwable throwable) {
                    throw new IllegalActionException(target.getComponent(),
                            throwable,
                            "Failed to generate variable initialization for \""
                                    + parameter + "\"");
                }
            }
        }
        return code.toString();
    }

    /** Return an unique label for the given attribute referenced
     * by the given adapter.
     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     * @param attribute The given attribute.
     * @param channelAndOffset The given channel and offset.
     * @return an unique label for the given attribute.
     * @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    @Override
    protected String _getParameter(NamedProgramCodeGeneratorAdapter target,
            Attribute attribute, String[] channelAndOffset)
            throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        //FIXME: potential bug: if the attribute is not a parameter,
        //it will be referenced but not declared.
        if (attribute instanceof Parameter) {
            if (!_referencedParameters.containsKey(target)) {
                _referencedParameters.put(target, new HashSet<Parameter>());
            }
            _referencedParameters.get(target).add((Parameter) attribute);
        }
    
        result.append(getCodeGenerator().generateVariableName(attribute));
    
        if (!channelAndOffset[0].equals("")) {
            throw new IllegalActionException(getComponent(),
                    "a parameter cannot have channel number.");
        }
    
        if (!channelAndOffset[1].equals("")) {
            //result.append("[" + channelAndOffset[1] + "]");
    
            // FIXME Findbugs: [M D BC] Unchecked/unconfirmed cast [BC_UNCONFIRMED_CAST]
            // We are not certain that attribute is parameter.
            if (!(attribute instanceof Parameter)) {
                throw new InternalErrorException(attribute, null,
                        "The attribute " + attribute.getFullName()
                                + " is not a Parameter.");
            } else {
                Type elementType = ((ArrayType) ((Parameter) attribute)
                        .getType()).getElementType();
    
                result.insert(0, "Array_get(");
                if (getCodeGenerator().isPrimitive(elementType)) {
                    // Generate type specific Array_get(). e.g. IntArray_get().
                    result.insert(0, "/*CGH77*/"
                            + getCodeGenerator().codeGenType(elementType));
                }
                result.insert(0, "/*CGH77*/");
    
                result.append(" ," + channelAndOffset[1] + ")");
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** This very simple function just tells if a port is a local port.
     *
     *  @return A boolean true when the port is local
     *  @exception IllegalActionException 
     */
    static private boolean _checkLocal(boolean forComposite, IOPort port)
            throws IllegalActionException {
        return port.isInput() && !forComposite && port.isOutsideConnected()
                || port.isOutput() && forComposite;
    }

    /** This very simple function just tells if a port is a remote port.
    *
    *  @return A boolean true when the port is remote
    *  @exception IllegalActionException 
    */
    static private boolean _checkRemote(boolean forComposite, IOPort port) {
        return port.isOutput() && !forComposite || port.isInput()
                && forComposite;
    }

    /**
     * Generate a string that represents the offset for a dynamically determined
     *  channel of a multiport.
     * @param port The referenced port.
     * @param isWrite Whether to generate the write or read offset.
     * @param channelString The string that will determine the channel.
     * @return The expression that represents the offset for a channel determined
     *  dynamically in the generated code.
     */
    private String _generateChannelOffset(TypedIOPort port, boolean isWrite,
            String channelString) throws IllegalActionException {
        // By default, return the channel offset for the first channel.
        if (channelString.equals("")) {
            channelString = "0";
        }

        String channelOffset = generatePortName(port)
                + (isWrite ? "_writeOffset" : "_readOffset") + "["
                + channelString + "]";

        return channelOffset;
    }

    /** Generate input variable declarations.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return a String that declares input variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateInputVariableDeclaration(
            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
        boolean dynamicReferencesAllowed = allowDynamicMultiportReference();

        StringBuffer code = new StringBuffer();

        Iterator<?> inputPorts = ((Actor) target.getComponent())
                .inputPortList().iterator();
        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            if (!inputPort.isOutsideConnected()) {
                continue;
            }
            //targetType(inputPort.getType())
            code.append("DEReceiver "
                    + CodeGeneratorAdapter.generateName(inputPort));

            int bufferSize = _ports.getBufferSize(inputPort);
            if (inputPort.isMultiport()) {
                code.append("[" + inputPort.getWidth() + "]");
                if (bufferSize > 1 || dynamicReferencesAllowed) {
                    code.append("[" + bufferSize + "]");
                }
            } else {
                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
            }

            code.append(";" + _eol);
        }

        return code.toString();
    }

    /** Generate output variable declarations.
     *  @return a String that declares output variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateOutputVariableDeclaration(
            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> outputPorts = ((Actor) target.getComponent())
                .outputPortList().iterator();

        while (outputPorts.hasNext()) {
            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();

            // If either the output port is a dangling port or
            // the output port has inside receivers.
            if (!outputPort.isOutsideConnected()
                    || outputPort.isInsideConnected()) {
                code.append("static " + targetType(outputPort.getType()) + " "
                        + CodeGeneratorAdapter.generateName(outputPort));

                if (outputPort.isMultiport()) {
                    code.append("[" + outputPort.getWidthInside() + "]");
                }

                int bufferSize = _ports.getBufferSize(outputPort);

                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
        }

        return code.toString();
    }

    /**
     * Generate a string that represents the reference for an IOPort
     * @param port The port to get the reference.
     * @param isWrite Whether to generate the write or read offset.
     * @param channelAndOffset The string[] that will determine the channel and the offset.
     * @return The expression that represents the reference for the port
     */
    private String _generatePortReference(TypedIOPort port,
            String[] channelAndOffset, boolean isWrite)
            throws IllegalActionException {

        StringBuffer result = new StringBuffer();
        String channelOffset;
        if (channelAndOffset[1].equals("")) {
            channelOffset = _generateChannelOffset(port, isWrite,
                    channelAndOffset[0]);
        } else {
            channelOffset = channelAndOffset[1];
        }

        result.append(generatePortName(port));

        if (port.isMultiport()) {
            result.append("[" + channelAndOffset[0] + "]");
        }
        result.append("[" + channelOffset + "]");

        return result.toString();
    }

    /** Generate referenced parameter declarations.
     *  @return a String that declares referenced parameters.
     *  @exception IllegalActionException If thrown while
     *  getting modified variable information.
     */
    private String _generateReferencedParameterDeclaration(
            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        if (_referencedParameters.containsKey(target)) {
            for (Parameter parameter : _referencedParameters.get(target)) {
                // avoid duplicate declaration.
                if (!getCodeGenerator().getModifiedVariables().contains(
                        parameter)) {
                    code.append("static "
                            + targetType(parameter.getType())
                            + " "
                            + getCodeGenerator()
                                    .generateVariableName(parameter) + ";"
                            + _eol);
                }
            }
        }

        return code.toString();
    }

    /** Generate type convert variable declarations.
     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return a String that declares type convert variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    private String _generateTypeConvertVariableDeclaration(
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Iterator<?> channels = target.getTypeConvertChannels().iterator();
        while (channels.hasNext()) {
            ProgramCodeGeneratorAdapter.Channel channel = (ProgramCodeGeneratorAdapter.Channel) channels
                    .next();
            Type portType = ((TypedIOPort) channel.port).getType();

            if (getCodeGenerator().isPrimitive(portType)) {

                code.append("static ");
                code.append(targetType(portType));
                code.append(" " + getTypeConvertReference(channel));

                //int bufferSize = getBufferSize(channel.port);
                int bufferSize = Math.max(
                        DFUtilities.getTokenProductionRate(channel.port),
                        DFUtilities.getTokenConsumptionRate(channel.port));

                if (bufferSize > 1) {
                    code.append("[" + bufferSize + "]");
                }
                code.append(";" + _eol);
            }
        }
        return code.toString();
    }

    /**
     * Generate a string that represents the cast type of a parameter or port
     * named "name"
     * @param name The name.
     * @return The string which represents the correct cast
     */
    private String _getCastType(String name) throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(),
                    "Invalid cast type: " + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            String type = tokenizer2.nextToken().trim();
            return type.length() > 0 ? type : null;
        }
        return null;
    }

    /** Return the channel and offset given in a string.
     *  The result is an string array of length 2. The first element
     *  indicates the channel index, and the second the offset. If either
     *  element is an empty string, it means that channel/offset is not
     *  specified.
     * @param name The given string.
     * @return An string array of length 2, containing expressions of the
     *  channel index and offset.
     * @exception IllegalActionException If the channel index or offset
     *  specified in the given string is illegal.
     */
    private String[] _getChannelAndOffset(String name)
            throws IllegalActionException {

        String[] result = { "", "" };

        // Given expression of forms:
        //     "port"
        //     "port, offset", or
        //     "port#channel, offset".

        int poundIndex = TemplateParser.indexOf("#", name, 0);
        int commaIndex = TemplateParser.indexOf(",", name, 0);

        if (commaIndex < 0) {
            commaIndex = name.length();
        }
        if (poundIndex < 0) {
            poundIndex = commaIndex;
        }

        if (poundIndex < commaIndex) {
            result[0] = name.substring(poundIndex + 1, commaIndex);
        }

        if (commaIndex < name.length()) {
            result[1] = name.substring(commaIndex + 1);
        }
        return result;
    }

    /**
     * Generate a string that represents the reference to a parameter or a port
     * named "name"
     * @param name The name.
     * @return The string which represents the reference
     */
    private String _getRefName(String name) throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if (tokenizer.countTokens() != 1 && tokenizer.countTokens() != 3
                && tokenizer.countTokens() != 5) {
            throw new IllegalActionException(getComponent(),
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(),
                    "Invalid cast type: " + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            // castType
            tokenizer2.nextToken();
        }

        return tokenizer2.nextToken().trim();
    }

    /**
     * Generate a string that represents the type of an attribute (only if it
     * is a parameter).
     * @param attribute The attribute to deal with.
     * @return The string which represents the type
     */
    private String _getRefType(Attribute attribute) {
        if (attribute instanceof Parameter) {
            return getCodeGenerator().codeGenType(
                    ((Parameter) attribute).getType());
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A hashmap that keeps track of parameters that are referenced for
     *  the associated actor.
     */
    protected HashMap<NamedProgramCodeGeneratorAdapter, HashSet<Parameter>> _referencedParameters = new HashMap<NamedProgramCodeGeneratorAdapter, HashSet<Parameter>>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Compare two NamedObjs by full name.
     */

    private static class FullNameComparator implements Comparator {

        /** Compare two NamedObjs by fullName().
         *  @return -1 if object1 has fewer dots in its fullName(),
         *  1 if object1 has more dots in its fullName(),
         *  0 if the objects are the same.
         *  If the fullName()s of both NamedObjs have the
         *  same number of dots, then return the String compareTo()
         *  of the fullName()s.
         */
        public int compare(Object object1, Object object2) {
            String name1 = ((NamedObj) object1).getFullName();
            String name2 = ((NamedObj) object2).getFullName();

            int index = 0;
            int dots1 = 0;
            while ((index = name1.indexOf(".", index)) != -1) {
                index++;
                dots1++;
            }
            int dots2 = 0;
            while ((index = name2.indexOf('.', index)) != -1) {
                index++;
                dots2++;
            }
            if (dots1 == dots2) {
                return 0;
            } else if (dots1 < dots2) {
                return -1;
            }
            return 1;
        }
    }

    /** A class that keeps track of information necessary to
     *  generate communication code at ports inside a StaticScheduled model.
     */
    protected class PortInfo {

        /** Create a PortInfo instance.
         *  @param port The port for which we are doing
         *  extra bookkeeping to generate code.
         */
        public PortInfo(IOPort port) {
            _port = port;
        }

        /** Get the buffer size of channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @return return The size of the buffer.
         *  @see #setBufferSize(int, int)
         *  @exception IllegalActionException If thrown while getting
         *  the channel or rate.
         */
        public int getBufferSize(int channelNumber)
                throws IllegalActionException {
            ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
            if (_bufferSizes.get(channel) == null) {
                // This should be a special case for doing
                // codegen for a sub-part of a model.
                //FIXME Why is the buffer size of a port its width? Should it be the rate of the port?
                return DFUtilities.getRate(channel.port);
                //return channel.port.getWidth();
            }
            return _bufferSizes.get(channel);
        }

        /**
         * Return the buffer size of the port, which is the maximum of
         * the bufferSizes of all channels the port.
         * @return The buffer size of the port.
         * @exception IllegalActionException If the
         * {@link #getBufferSize(int)} method throws it.
         * @see #setBufferSize(int, int)
         */
        public int getBufferSize() throws IllegalActionException {
            int bufferSize = 1;

            int length = 0;

            if (_port.isInput()) {
                length = _port.getWidth();
            } else {
                length = _port.getWidthInside();
            }

            for (int i = 0; i < length; i++) {
                int channelBufferSize = 1;
                try {
                    channelBufferSize = getBufferSize(i);
                } catch (ptolemy.actor.sched.NotSchedulableException ex) {
                    // Ignore.  Probably a modal model.
                    // $PTII/bin/ptcg -inline true -language java /Users/cxh/ptII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/modal/test/auto/Simple01.xml
                }
                if (channelBufferSize > bufferSize) {
                    bufferSize = channelBufferSize;
                }
            }
            return bufferSize;
        }

        /**Generate the expression that represents the offset in the generated
         * code.
         * @param offset The specified offset from the user.
         * @param channel The referenced port channel.
         * @param isWrite Whether to generate the write or read offset.
         * @return The expression that represents the offset in the generated code.
         * @exception IllegalActionException If there is problems getting the port
         *  buffer size or the offset in the channel and offset map.
         */
        public String generateOffset(String offset, int channel, boolean isWrite)
                throws IllegalActionException {
            return _generateOffset(offset, channel, isWrite);
        }

        /** Get the read offset of a channel of the port.
         *  @param channelNumber The number of the channel.
         *  @return The read offset.
         *  @exception IllegalActionException If thrown while getting the channel.
         *  @see #setReadOffset(int, Object)
         */
        public Object getReadOffset(int channelNumber)
                throws IllegalActionException {
            ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
            if (_readOffsets.get(channel) == null) {
                throw new IllegalActionException(
                        "Could not find the specified channel in this director");
            }
            return _readOffsets.get(channel);

        }

        /** Get the write offset of a channel of the port.
         *  @param channelNumber The number of the channel.
         *  @return The write offset.
         *  @exception IllegalActionException If thrown while getting the channel.
         *  @see #setWriteOffset(int, Object)
         */
        public Object getWriteOffset(int channelNumber)
                throws IllegalActionException {
            ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
            if (_writeOffsets.get(channel) == null) {
                throw new IllegalActionException(_port,
                        "Could not write offset for channel " + channelNumber
                                + " in port " + _port.getFullName()
                                + ", there were " + _writeOffsets.size()
                                + " writeOffsets for this port.");
            }
            return _writeOffsets.get(channel);

        }

        /** Initialize the offsets.
         *  @return The code to initialize the offsets.
         *  @exception IllegalActionException Thrown if offsets can't be initialized.
         */
        public String initializeOffsets() throws IllegalActionException {

            /* FIXME: move pthread specific code out-of-here...
            if (_isPthread()) {
                return "";
            }
            */

            StringBuffer code = new StringBuffer();

            for (int i = 0; i < _port.getWidth(); i++) {
                Object readOffset = _ports.getReadOffset(_port, i);
                if (readOffset instanceof Integer) {
                    _ports.setReadOffset(_port, i, Integer.valueOf(0));
                } else {
                    code.append((String) readOffset + " = 0;" + _eol);
                }
                Object writeOffset = _ports.getWriteOffset(_port, i);
                if (writeOffset instanceof Integer) {
                    _ports.setWriteOffset(_port, i, Integer.valueOf(0));
                } else {
                    code.append((String) writeOffset + " = 0;" + _eol);
                }
            }
            return code.toString();
        }

        /** Set the buffer size of channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param bufferSize The size of the buffer.
         *  @see #getBufferSize(int)
         */
        public void setBufferSize(int channelNumber, int bufferSize) {
            ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
            _bufferSizes.put(channel, bufferSize);
        }

        /** Set the read offset of a channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param readOffset The offset.
         *  @see #getReadOffset(int)
         */
        public void setReadOffset(int channelNumber, Object readOffset) {
            ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
            _readOffsets.put(channel, readOffset);
        }

        /** Set the write offset of a channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param writeOffset The offset.
         *  @see #getWriteOffset(int)
         */
        public void setWriteOffset(int channelNumber, Object writeOffset) {
            ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
            _writeOffsets.put(channel, writeOffset);
        }

        /** Update the offsets of the buffers associated with the ports connected
         *  with the given port in its downstream.
         *
         *  @return The generated code.
         *  @param rate The rate, which must be greater than or equal to 0.
         *  @exception IllegalActionException If thrown while reading or writing
         *   offsets, or getting the buffer size, or if the rate is less than 0.
         */
        public String updateConnectedPortsOffset(int rate)
                throws IllegalActionException {
            boolean padBuffers = padBuffers();

            StringBuffer code = new StringBuffer();
            code.append(getCodeGenerator()
                    .comment(
                            "Begin updateConnectedPortsOffset "
                                    + /*NamedProgramCodeGeneratorAdapter.*/generatePortName((TypedIOPort) _port)));

            if (rate == 0) {
                return "";
            } else if (rate < 0) {
                throw new IllegalActionException(_port, "the rate: " + rate
                        + " is negative.");
            }

            int length = 0;
            if (_port.isInput()) {
                length = _port.getWidthInside();
            } else {
                length = _port.getWidth();
            }

            for (int j = 0; j < length; j++) {
                List<ProgramCodeGeneratorAdapter.Channel> sinkChannels = getSinkChannels(
                        _port, j);

                for (int k = 0; k < sinkChannels.size(); k++) {
                    ProgramCodeGeneratorAdapter.Channel channel = sinkChannels
                            .get(k);
                    ptolemy.actor.IOPort sinkPort = channel.port;
                    int sinkChannelNumber = channel.channelNumber;

                    Object offsetObject = _ports.getWriteOffset(sinkPort,
                            sinkChannelNumber);

                    if (offsetObject instanceof Integer) {
                        int offset = ((Integer) offsetObject).intValue();
                        int bufferSize = _ports.getBufferSize(sinkPort,
                                sinkChannelNumber);
                        if (bufferSize != 0) {
                            offset = (offset + rate) % bufferSize;
                        }
                        _ports.setWriteOffset(sinkPort, sinkChannelNumber,
                                Integer.valueOf(offset));
                    } else { // If offset is a variable.
                        String offsetVariable = (String) _ports.getWriteOffset(
                                sinkPort, sinkChannelNumber);

                        if (padBuffers) {
                            int modulo = _ports.getBufferSize(sinkPort,
                                    sinkChannelNumber) - 1;
                            code.append(offsetVariable + " = ("
                                    + offsetVariable + " + " + rate + ")&"
                                    + modulo + ";" + _eol);
                        } else {
                            code.append(offsetVariable
                                    + " = ("
                                    + offsetVariable
                                    + " + "
                                    + rate
                                    + ") % "
                                    + _ports.getBufferSize(sinkPort,
                                            sinkChannelNumber) + ";" + _eol);

                        }
                    }
                }
            }
            code.append(getCodeGenerator()
                    .comment(
                            "End updateConnectedPortsOffset "
                                    + /*NamedProgramCodeGeneratorAdapter.*/generatePortName((TypedIOPort) _port)));
            return code.toString();
        }

        /** Update the read offset.
         *  @param rate  The rate of the channels.
         *  @return The offset.
         *  @exception IllegalActionException If thrown while getting a token,
         *  adapter, read offset or buffer size.
         */
        public String updateOffset(int rate) throws IllegalActionException {

            //Receiver receiver = _getReceiver(null, 0, _port);

            StringBuffer code = new StringBuffer(
                    getCodeGenerator()
                            .comment(
                                    "Begin updateOffset "
                                            + /*NamedProgramCodeGeneratorAdapter.*/generatePortName((TypedIOPort) _port)));

            for (int i = 0; i < _port.getWidth(); i++) {
                code.append(_updateOffset(i, rate)
                        + _eol
                        + getCodeGenerator()
                                .comment(
                                        "End updateOffset "
                                                + /*NamedProgramCodeGeneratorAdapter.*/generatePortName((TypedIOPort) _port)));
            }
            return code.toString();
        }

        private ProgramCodeGeneratorAdapter.Channel _getChannel(
                int channelNumber) {
            return new ProgramCodeGeneratorAdapter.Channel(_port, channelNumber);
        }

        /**
         * Generate the expression that represents the offset in the generated
         * code.
         * @param offsetString The specified offset from the user.
         * @param channel The referenced port channel.
         * @param isWrite Whether to generate the write or read offset.
         * @return The expression that represents the offset in the generated code.
         * @exception IllegalActionException If there is problems getting the port
         *  buffer size or the offset in the channel and offset map.
         */
        private String _generateOffset(String offsetString, int channel,
                boolean isWrite) throws IllegalActionException {
            boolean dynamicReferencesAllowed = allowDynamicMultiportReference();
            boolean padBuffers = padBuffers();

            //if (MpiPNDirector.isLocalBuffer(port, channel)) {
            //    int i = 1;
            //}

            // When dynamic references are allowed, any input ports require
            // offsets.
            if (dynamicReferencesAllowed && _port.isInput()) {
                if (!(_port.isMultiport() || getBufferSize() > 1)) {
                    return "";
                }
            } else {
                int bufferSize = getBufferSize();
                if (!(bufferSize > 1)) {
                    return "";
                }
            }

            String result = "";
            Object offsetObject;

            // Get the offset index.
            if (isWrite) {
                offsetObject = getWriteOffset(channel);
            } else {
                offsetObject = getReadOffset(channel);
            }
            if (!offsetString.equals("")) {
                // Specified offset.

                String temp = "";
                if (offsetObject instanceof Integer && _isInteger(offsetString)) {

                    int offset = ((Integer) offsetObject).intValue()
                            + Integer.valueOf(offsetString).intValue();

                    offset %= getBufferSize(channel);
                    temp = Integer.toString(offset);
                    /*
                     int divisor = getBufferSize(sinkPort,
                     sinkChannelNumber);
                     temp = "("
                     + getWriteOffset(sinkPort,
                     sinkChannelNumber) + " + "
                     + channelAndOffset[1] + ")%" + divisor;
                     */
                    result = "[" + temp + "]";
                } else {
                    // FIXME: We haven't check if modulo is 0. But this
                    // should never happen. For offsets that need to be
                    // represented by string expression,
                    // getBufferSize(_port, channelNumber) will always
                    // return a value at least 2.

                    //              if (MpiPNDirector.isLocalBuffer(_port, channel)) {
                    //              temp = offsetObject.toString();
                    //              temp = MpiPNDirector.generateFreeSlots(_port, channel) +
                    //              "[" + MpiPNDirector.generatePortHeader(_port, channel) + ".current]";
                    //              } else
                    if (offsetObject == null) {
                        result = getCodeGenerator()
                                .comment(
                                        _port.getFullName()
                                                + " Getting offset for channel "
                                                + channel
                                                + " returned null?"
                                                + "This can happen if there are problems with Profile.firing().");

                    } else {
                        if (padBuffers) {
                            int modulo = getBufferSize(channel) - 1;
                            temp = "(" + offsetObject.toString() + " + "
                                    + offsetString + ")&" + modulo;
                        } else {
                            int modulo = getBufferSize(channel);
                            temp = "(" + offsetObject.toString() + " + "
                                    + offsetString + ")%" + modulo;
                        }
                        result = "[" + temp + "]";
                    }
                }

            } else {
                // Did not specify offset, so the receiver buffer
                // size is 1. This is multiple firing.

                if (offsetObject instanceof Integer) {
                    int offset = ((Integer) offsetObject).intValue();

                    offset %= getBufferSize(channel);

                    result = "[" + offset + "]";
                } else if (offsetObject != null) {

                    //              if (MpiPNDirector.isLocalBuffer(_port, channel)) {
                    //              result = offsetObject.toString();
                    //              result = MpiPNDirector.generateFreeSlots(_port, channel) +
                    //              "[" + MpiPNDirector.generatePortHeader(_port, channel) + ".current]";
                    //              } else
                    if (padBuffers) {
                        int modulo = getBufferSize(channel) - 1;
                        result = "[" + offsetObject + "&" + modulo + "]";
                    } else {
                        result = "[" + offsetObject + "%"
                                + getBufferSize(channel) + "]";
                    }
                }
            }
            return result;
        }

        /**
         * Return true if the given string can be parse as an integer; otherwise,
         * return false.
         * @param numberString The given number string.
         * @return True if the given string can be parse as an integer; otherwise,
         *  return false.
         */
        private boolean _isInteger(String numberString) {
            try {
                Integer.parseInt(numberString);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        /** Update the offset of the channel.
         *  @param channel The channel number of the channel to be offset.
         *  @param rate The firing rate of the port.
         *  @return The code that represents the offset to the channel,
         *  @exception IllegalActionException If thrown while getting a token,
         *  adapter, read offset or buffer size.
         */
        private String _updateOffset(int channel, int rate)
                throws IllegalActionException {
            StringBuffer code = new StringBuffer();
            boolean padBuffers = padBuffers();

            // Update the offset for each channel.
            if (getReadOffset(channel) instanceof Integer) {
                int offset = ((Integer) getReadOffset(channel)).intValue();
                if (getBufferSize(channel) != 0) {
                    offset = (offset + rate) % getBufferSize(channel);
                }
                setReadOffset(channel, Integer.valueOf(offset));
            } else { // If offset is a variable.
                String offsetVariable = (String) getReadOffset(channel);
                if (padBuffers) {
                    int modulo = getBufferSize(channel) - 1;
                    code.append(offsetVariable + " = (" + offsetVariable
                            + " + " + rate + ")&" + modulo + ";" + _eol);
                } else {
                    code.append(offsetVariable + " = (" + offsetVariable
                            + " + " + rate + ") % " + getBufferSize(channel)
                            + ";" + _eol);
                }
            }
            return code.toString();

        }

        /** A HashMap that keeps track of the bufferSizes of each channel
         *  of the actor.
         */
        private HashMap<ProgramCodeGeneratorAdapter.Channel, Integer> _bufferSizes = new HashMap<ProgramCodeGeneratorAdapter.Channel, Integer>();

        /** The port for which we are doing extra bookkeeping to generate code.
         */
        private IOPort _port;

        /** A HashMap that keeps track of the read offsets of each input channel of
         *  the actor.
         */
        private HashMap<ProgramCodeGeneratorAdapter.Channel, Object> _readOffsets = new HashMap<ProgramCodeGeneratorAdapter.Channel, Object>();

        /** A HashMap that keeps track of the write offsets of each input channel of
         *  the actor.
         */
        private HashMap<ProgramCodeGeneratorAdapter.Channel, Object> _writeOffsets = new HashMap<ProgramCodeGeneratorAdapter.Channel, Object>();

    }

    /** A adapter class that allows generating code for ports.*/
    public class Ports {

        /**Generate the expression that represents the offset in the generated
         * code.
         *  @param port The given port.
         * @param offset The specified offset from the user.
         * @param channel The referenced port channel.
         * @param isWrite Whether to generate the write or read offset.
         * @return The expression that represents the offset in the generated code.
         * @exception IllegalActionException If there is problems getting the port
         *  buffer size or the offset in the channel and offset map.
         */
        public String generateOffset(IOPort port, String offset, int channel,
                boolean isWrite) throws IllegalActionException {
            return _getPortInfo(port).generateOffset(offset, channel, isWrite);
        }

        /** Get the buffer size of channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param port The given port.
         *  @return return The size of the buffer.
         * @exception IllegalActionException
         *  @see #setBufferSize(IOPort, int, int)
         */
        public int getBufferSize(IOPort port, int channelNumber)
                throws IllegalActionException {
            return _getPortInfo(port).getBufferSize(channelNumber);
        }

        /**
         * Return the buffer size of a given port, which is the maximum of
         * the bufferSizes of all channels of the given port.
         * @param port The given port.
         * @return The buffer size of the given port.
         * @exception IllegalActionException If the
         * {@link #getBufferSize(IOPort, int)} method throws it.
         * @see #setBufferSize(IOPort, int, int)
         */
        public int getBufferSize(IOPort port) throws IllegalActionException {
            return _getPortInfo(port).getBufferSize();
        }

        /** Get the read offset in the buffer of a given channel from which a token
         *  should be read. The channel is given by its containing port and
         *  the channel number in that port.
         *  @param inputPort The given port.
         *  @param channelNumber The given channel number.
         *  @return The offset in the buffer of a given channel from which a token
         *   should be read.
         *  @exception IllegalActionException Thrown if the adapter class cannot
         *   be found.
         *  @see #setReadOffset(IOPort, int, Object)
         */
        public Object getReadOffset(IOPort inputPort, int channelNumber)
                throws IllegalActionException {
            return _getPortInfo(inputPort).getReadOffset(channelNumber);
        }

        /** Get the write offset in the buffer of a given channel to which a token
         *  should be put. The channel is given by its containing port and
         *  the channel number in that port.
         *  @param port The given port.
         *  @param channelNumber The given channel number.
         *  @return The offset in the buffer of a given channel to which a token
         *   should be put.
         *  @exception IllegalActionException Thrown if the adapter class cannot
         *   be found.
         *  @see #setWriteOffset(IOPort, int, Object)
         */
        public Object getWriteOffset(IOPort port, int channelNumber)
                throws IllegalActionException {
            return _getPortInfo(port).getWriteOffset(channelNumber);
        }

        /** Initialize the offsets.
         *  @param port The given port.
         *  @return The code to initialize the offsets.
         *  @exception IllegalActionException Thrown if offsets can't be initialized.
         */
        public String initializeOffsets(IOPort port)
                throws IllegalActionException {
            return _getPortInfo(port).initializeOffsets();
        }

        /** Set the buffer size of channel of the port.
         *  @param port The given port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param bufferSize The size of the buffer.
         *  @see #getBufferSize(IOPort, int)
         *  @exception IllegalActionException If thrown while getting the port
         *  information or while setting the buffer size.
         */
        public void setBufferSize(IOPort port, int channelNumber, int bufferSize)
                throws IllegalActionException {
            _getPortInfo(port).setBufferSize(channelNumber, bufferSize);
        }

        /** Set the read offset in a buffer of a given channel from which a token
         *  should be read.
         *  @param port The given port.
         *  @param channelNumber The given channel.
         *  @param readOffset The offset to be set to the buffer of that channel.
         *  @exception IllegalActionException Thrown if the adapter class cannot
         *   be found.
         *  @see #getReadOffset(IOPort, int)
         */
        public void setReadOffset(IOPort port, int channelNumber,
                Object readOffset) throws IllegalActionException {
            _getPortInfo(port).setReadOffset(channelNumber, readOffset);
        }

        /** Set the write offset in a buffer of a given channel to which a token
         *  should be put.
         *  @param port The given port.
         *  @param channelNumber The given channel.
         *  @param writeOffset The offset to be set to the buffer of that channel.
         *  @exception IllegalActionException If
         *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
         *  @see #getWriteOffset(IOPort, int)
         */
        public void setWriteOffset(IOPort port, int channelNumber,
                Object writeOffset) throws IllegalActionException {
            _getPortInfo(port).setWriteOffset(channelNumber, writeOffset);
        }

        /** Update the offsets of the buffers associated with the ports connected
         *  with the given port in its downstream.
         *
         *  @param port The port whose directly connected downstream actors update
         *   their write offsets.
         *  @return The generated code.
         *  @param rate The rate, which must be greater than or equal to 0.
         *  @exception IllegalActionException If thrown while reading or writing
         *   offsets, or getting the buffer size, or if the rate is less than 0.
         */
        public String updateConnectedPortsOffset(IOPort port, int rate)
                throws IllegalActionException {
            return _getPortInfo(port).updateConnectedPortsOffset(rate);
        }

        /** Update the read offset.
         *  @param port The given port.
         *  @param rate  The rate of the channels.
         *  @return The offset.
         *  @exception IllegalActionException If thrown while getting a token,
         *  adapter, read offset or buffer size.
         */
        public String updateOffset(IOPort port, int rate)
                throws IllegalActionException {
            return _getPortInfo(port).updateOffset(rate);
        }

        /** Return the information necessary to generate
         *  communication code at the given port.
         * @param port The given port for which we want to
         *      retrieve information to generate code.
         */
        private PortInfo _getPortInfo(IOPort port)
                throws IllegalActionException {
            PortInfo info = null;
            if (!_portInfo.containsKey(port)) {
                NamedObj container = getComponent().getContainer()
                        .getContainer();
                // If we don't have portInfo for the port, then go up the hierarchy and look
                // for portInfo elsewhere.  This is very convoluted, but necessary for
                // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/lib/test/auto/SampleDelay5.xml
                if (container != null
                        && getComponent().getContainer() != port.getContainer()
                                .getContainer()
                        && getComponent().getContainer() != port.getContainer()
                        && getComponent()
                                .getContainer()
                                .getFullName()
                                .startsWith(
                                        port.getContainer().getContainer()
                                                .getFullName())) {
                    while (container != null) {
                        if (container instanceof CompositeEntity) {
                            List entities = ((CompositeEntity) container)
                                    .attributeList(ptolemy.actor.Director.class);
                            if (entities.size() > 0) {
                                Director entity = (Director) getCodeGenerator()
                                        .getAdapter(
                                                entities.get(entities.size() - 1));
                                if (entity instanceof DEDirector) {
                                    DEDirector parent = (DEDirector) entity;
                                    if (parent._ports._portInfo
                                            .containsKey(port)) {
                                        info = parent._ports._portInfo
                                                .get(port);
                                    }
                                    break;
                                }
                            }
                        }
                        container = container.getContainer();
                    }
                }
                if (info == null) {
                    info = new PortInfo(port);
                }
                _portInfo.put(port, info);
            } else {
                info = _portInfo.get(port);
            }
            return info;
        }

        /** A map from IOPort to PortInfo. */
        protected Map<IOPort, PortInfo> _portInfo = new HashMap<IOPort, PortInfo>();
    }

    /** The meta information about the ports in the container. */
    public Ports _ports = new Ports();
}
