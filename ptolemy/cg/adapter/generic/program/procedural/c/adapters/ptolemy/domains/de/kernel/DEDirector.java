/* Code generator adapter class associated with the DEDirector class.

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.kernel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Time;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.PortDirector;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

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
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (wlc)
 *  @Pt.AcceptedRating red (wlc)
 */

public class DEDirector extends PortDirector {

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
    @Override
    final public Boolean allowDynamicMultiportReference()
            throws IllegalActionException {
        return false;
        //        return ((BooleanToken) ((Parameter) getComponent()
        //                .getDecoratorAttribute(getCodeGenerator(),
        //                        "allowDynamicMultiportReference")).getToken())
        //                .booleanValue();
    }

    /** Generate the constructor code for the specified director.
     * In this class we initialize the director with its internal
     * parameters and fields as well as with the depths of the actors.
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
        ptolemy.domains.de.kernel.DEDirector director = (ptolemy.domains.de.kernel.DEDirector) _director;

        result.append(_eol + getSanitizedDirectorName() + "->container = "
                + sanitizedContainerName + ";");
        result.append(_eol + _sanitizedDirectorName + "->_startTime = "
                + director.getModelStartTime() + ";");
        result.append(_eol + _sanitizedDirectorName + "->_stopTime = "
                + director.getModelStopTime() + ";");
        result.append(_eol + _sanitizedDirectorName + "->binCountFactor = "
                + ((IntToken) director.binCountFactor.getToken()).intValue()
                + ";");
        result.append(_eol
                + _sanitizedDirectorName
                + "->isCQAdaptive = "
                + ((BooleanToken) director.isCQAdaptive.getToken())
                .booleanValue() + ";");
        result.append(_eol + _sanitizedDirectorName + "->minBinCount = "
                + ((IntToken) director.minBinCount.getToken()).intValue() + ";");
        result.append(_eol
                + _sanitizedDirectorName
                + "->stopWhenQueueIsEmpty = "
                + ((BooleanToken) director.stopWhenQueueIsEmpty.getToken())
                .booleanValue() + ";");
        result.append(_eol + _sanitizedDirectorName
                + "->localClock->container = (struct Director*)"
                + _sanitizedDirectorName + ";");

        // Add the depth of the container actor
        result.append(_eol + "int* depth = malloc(sizeof(int));");
        CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
                .getCausalityInterface();
        int depth = causality.getDepthOfActor(container);
        result.append(_eol + "*depth = " + depth + ";");
        result.append(_eol + "pblMapAdd(" + _sanitizedDirectorName
                + "->actorsDepths, &" + sanitizedContainerName
                + ", sizeof(struct Actor*), depth, sizeof(int));");

        // Add the depths of the container's ports
        Iterator<?> ports = ((Actor) container).inputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            if (!port.isInsideConnected()) {
                continue;
            }
            depth = causality.getDepthOfPort(port);
            result.append(_eol + "*depth = " + depth + ";");
            result.append(_eol + "pblMapAdd(" + _sanitizedDirectorName
                    + "->portsDepths, &" + port.getName()
                    + ", sizeof(struct IOPort*), depth, sizeof(int));");
        }
        ports = ((Actor) container).outputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            if (!port.isInsideConnected()) {
                continue;
            }
            depth = causality.getDepthOfPort(port);
            result.append(_eol + "*depth = " + depth + ";");
            result.append(_eol + "pblMapAdd(" + _sanitizedDirectorName
                    + "->portsDepths, &" + port.getName()
                    + ", sizeof(struct IOPort*), depth, sizeof(int));");
        }

        List<?> containedActors = container.deepEntityList();
        Iterator<?> actors = containedActors.iterator();
        // First loop to create the struct IOPort
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName((NamedObj) actor);
            ports = actor.inputPortList().iterator();
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
            depth = causality.getDepthOfActor((Actor) actor);
            result.append(_eol + "*depth = " + depth + ";");
            result.append(_eol + "pblMapAdd(" + _sanitizedDirectorName
                    + "->actorsDepths, &" + sanitizedActorName
                    + ", sizeof(struct Actor*), depth, sizeof(int));");
            ports = ((Actor) actor).inputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isOutsideConnected()) {
                    continue;
                }
                depth = causality.getDepthOfPort(port);
                result.append(_eol + "*depth = " + depth + ";");
                result.append(_eol + "pblMapAdd(" + _sanitizedDirectorName
                        + "->portsDepths, &" + sanitizedActorName + "_"
                        + port.getName()
                        + ", sizeof(struct IOPort*), depth, sizeof(int));");
            }
            ports = ((Actor) actor).outputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isOutsideConnected()) {
                    continue;
                }
                depth = causality.getDepthOfPort(port);
                result.append(_eol + "*depth = " + depth + ";");
                result.append(_eol + "pblMapAdd(" + _sanitizedDirectorName
                        + "->portsDepths, &" + sanitizedActorName + "_"
                        + port.getName()
                        + ", sizeof(struct IOPort*), depth, sizeof(int));");

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
        ports = ((Actor) container).inputPortList().iterator();
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
        result.append(_eol + "free(depth);");

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

        code.append(_eol + "int " + _sanitizedDirectorName + "_fire();");
        code.append(_eol + "void " + _sanitizedDirectorName
                + "_fireAt(Actor * actor, Time time, int microstep);");
        code.append(_eol + "Actor * " + _sanitizedDirectorName
                + "_nextActorToFire();");

        code.append(_eol + "void " + _sanitizedDirectorName
                + "_Preinitialize();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Initialize();");
        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Prefire();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Fire();");
        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Postfire();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Wrapup();");

        return code.toString();
    }

    /** Generate The _fireAt function code.
     *  This method is the direct transposition of the _fireAt function of the director
     *  in C.
     *
     *  @return The _fireAt function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFireAtFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol + "Time result = time;");
        // We cannot schedule an event in the past !
        code.append(_eol + "if (result < " + _sanitizedDirectorName
                + ".currentModelTime)");
        code.append(_eol + "        result = " + _sanitizedDirectorName
                + ".currentModelTime;");

        code.append(_eol + "int depth = actor->depth;");
        code.append(_eol + "int priority = actor->priority;");
        code.append(_eol
                + "DEEvent * newEvent = newDEEventWithParam(actor, NULL, depth,");
        code.append(_eol + "                microstep, priority, result);");
        code.append(_eol + "CQueuePut(&(" + _sanitizedDirectorName
                + ".cqueue), newEvent);");

        code.append(_eol + "return;");

        return code.toString();
    }

    /** Generate The fire function code. This method calls fire() for in a loop
     *  This method is the direct transposition of the Fire function of the director
     *  in C.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // NOTE: This fire method does not call super.fire()
        // because this method is very different from that of the super class.
        // A BIG while loop that handles all events with the same tag.
        code.append(_eol + "while (true) {");
        code.append(_eol + "    int result = " + _sanitizedDirectorName
                + "_fire();");
        code.append(_eol + "    DEEvent * nextEvent = CQueueGet(&("
                + _sanitizedDirectorName + ".cqueue));");
        code.append(_eol + "    if (result == 1) {");
        code.append(_eol + "        continue;");
        code.append(_eol
                + "    } else if (result == -1 || nextEvent == NULL) {");
        code.append(_eol + "                " + _sanitizedDirectorName
                + ".noMoreActorToFire = true;");
        code.append(_eol + "        return;");
        code.append(_eol + "    } ");
        // else if 0, keep executing

        // if the next event is in the future break the loop !
        code.append(_eol + "    if (nextEvent->timestamp > "
                + _sanitizedDirectorName + ".currentModelTime ||"
                + "(nextEvent->timestamp == " + _sanitizedDirectorName
                + ".currentModelTime &&" + "nextEvent->microstep > "
                + _sanitizedDirectorName + ".currentMicrostep)) {");
        code.append(_eol + "        break;");
        code.append(_eol + "    } ");// else keep executing in the current iteration
        code.append(_eol + "} ");// Close the BIG while loop.

        return code.toString();
    }

    /** Generate The _fire function code.
     *  This method is the direct transposition of the _fire function of the director
     *  in C.
     *
     *  @return The _fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateFirePrivateFunctionCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol + "Actor * actorToFire = " + _sanitizedDirectorName
                + "_nextActorToFire();");
        code.append(_eol + "" + _sanitizedDirectorName
                + ".currentActor = actorToFire;");

        code.append(_eol + "if (actorToFire == NULL) {");
        code.append(_eol + "    " + _sanitizedDirectorName
                + ".noMoreActorToFire = true;");
        code.append(_eol + "    return -1;");
        code.append(_eol + "}");

        code.append(_eol + "if ((void*)actorToFire == (void*)"
                + _sanitizedDirectorName + ".containerActor) {");
        code.append(_eol + "    return 1;");
        code.append(_eol + "}");

        code.append(_eol + "boolean refire;");
        code.append(_eol + "do {");
        code.append(_eol + "    refire = false;");
        code.append(_eol
                + "    boolean prefire = (*(actorToFire->prefireFunction))();");
        code.append(_eol + "    if (!prefire) {");
        code.append(_eol + "        break;");
        code.append(_eol + "    }");
        code.append(_eol + "    (*(actorToFire->fireFunction))();");
        code.append(_eol + "    (*(actorToFire->postfireFunction))();");
        code.append(_eol + "} while (refire);");
        code.append(_eol + "return 0;");

        return code.toString();
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
        CompositeActor container = ((CompositeActor) _director.getContainer());
        List actorList = container.deepEntityList();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        code.append(_eol + _eol
                + codeGenerator.comment("Initialization of the director"));

        code.append(_eol + _sanitizedDirectorName
                + ".noMoreActorToFire = false;");
        code.append(_eol + _sanitizedDirectorName + ".currentMicrostep = 0;");

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
            code.append(_eol + sanitizedActorName + "_initialize();");
        }

        code.append(_eol + _sanitizedDirectorName + ".containerActor = &"
                + sanitizedContainerName + ";");

        code.append(_eol + _sanitizedDirectorName + ".currentModelTime = "
                + _sanitizedDirectorName + ".startTime;");
        Attribute stopWhenQueueIsEmpty = director
                .getAttribute("stopWhenQueueIsEmpty");
        boolean stopWhenQueueIsEmptyBool = ((BooleanToken) ((Variable) stopWhenQueueIsEmpty)
                .getToken()).booleanValue();
        code.append(_eol + _sanitizedDirectorName + ".stopWhenQueueIsEmpty = "
                + stopWhenQueueIsEmptyBool + ";");
        code.append(_eol + _sanitizedDirectorName + ".exceedStopTime = false;");

        // Register the stop time as an event such that the model is
        // guaranteed to stop at that time. This event also serves as
        // a guideline for an embedded Continuous model to know how much
        // further to integrate into future. But only do this if the
        // stop time is finite.
        if (!director.getModelStopTime().isPositiveInfinite()) {
            code.append(_eol + _sanitizedDirectorName + "_fireAt(&"
                    + _sanitizedDirectorName + ".containerActor->actor, " + ""
                    + _sanitizedDirectorName + ".stopTime, 1);");
        }

        code.append(_eol + _sanitizedDirectorName + ".isInitializing = false;");
        code.append(_eol
                + codeGenerator
                .comment("End of the Initialization of the director"));

        return code.toString();
    }

    //        Old way of initialization
    //
    //        code.append(_eol + _eol + "director.actors = calloc("
    //                + actorList.size() + ", sizeof(Actor*));");
    //        code.append(_eol + "if (director.actors == NULL)");
    //        code.append(_eol + "    perror(\"Allocation problem (global)\");");
    //
    //        code.append(_eol + _eol + "IOPort ** inPorts = NULL;");
    //        code.append(_eol + "IOPort ** outPorts = NULL;");
    //
    //        // Declare all the actors in the director structure
    //        int i = 0;
    //        Iterator<?> actors = actorList.iterator();
    //        while (actors.hasNext()) {
    //            Actor actor = (Actor) actors.next();
    //            List inputPorts = actor.inputPortList();
    //            int j = 0;
    //            code.append(_eol + "inPorts = NULL;");
    //            code.append(_eol + _eol + "inPorts = calloc(" + inputPorts.size()
    //                    + ", sizeof(IOPort*));");
    //            code.append(_eol + "if (inPorts == NULL)");
    //            code.append(_eol + "    perror(\"Allocation problem (global)\");");
    //            Iterator<?> inPorts = inputPorts.iterator();
    //            while (inPorts.hasNext()) {
    //                TypedIOPort inPort = (TypedIOPort) inPorts.next();
    //                code.append(_eol + "inPorts[" + j + "] = newIOPortWithParam(\""
    //                        + inPort.getName() + "\", \"" + inPort.getType()
    //                        + "\", " + inPort.isInput() + ", "
    //                        + inPort.isMultiport() + ", " + inPort.getWidth()
    //                        + ");");
    //                j++;
    //            }
    //
    //            List outputPorts = actor.outputPortList();
    //            j = 0;
    //            code.append(_eol + "outPorts = NULL;");
    //            code.append(_eol + "outPorts = calloc(" + outputPorts.size()
    //                    + ", sizeof(IOPort*));");
    //            code.append(_eol + "if (outPorts == NULL)");
    //            code.append(_eol + "    perror(\"Allocation problem (global)\");");
    //            Iterator<?> outPorts = outputPorts.iterator();
    //            while (outPorts.hasNext()) {
    //                TypedIOPort outPort = (TypedIOPort) outPorts.next();
    //                // TODO : add listeners later
    //                code.append(_eol + "outPorts[" + j
    //                        + "] = newIOPortWithParam(\"" + outPort.getName()
    //                        + "\", \"" + outPort.getType() + "\", "
    //                        + outPort.isInput() + ", " + outPort.isMultiport()
    //                        + ", " + outPort.getWidth() + ");");
    //                j++;
    //            }
    //
    //            code.append(_eol + "director.actors[" + i
    //                    + "] = newActorWithParam(\"" + actor.getName()
    //                    + "\", inPorts, outPorts);");
    //
    //            //code.append(_eol + "director.actors["+i+"]->preInitializeFunction = " + actor.getName() + "PreinitializeCode;");
    //            code.append(_eol + "director.actors[" + i
    //                    + "]->initializeFunction = " + actor.getName()
    //                    + "InitializeCode;");
    //            code.append(_eol + "director.actors[" + i + "]->prefireFunction = "
    //                    + actor.getName() + "PrefireCode;");
    //            code.append(_eol + "director.actors[" + i + "]->fireFunction = "
    //                    + modelName + "_" + actor.getName() + ";");
    //
    //            code.append(_eol + "director.actors[" + i
    //                    + "]->postfireFunction = " + actor.getName()
    //                    + "PostfireCode;");
    //            code.append(_eol
    //                    + codeGenerator.comment("initialization of the actor : "
    //                            + actor.getName()));
    //            code.append(_eol + actor.getName() + " = director.actors[" + i
    //                    + "];");
    //            CompositeActor container = (CompositeActor) director.getContainer();
    //            CausalityInterfaceForComposites causality = (CausalityInterfaceForComposites) container
    //                    .getCausalityInterface();
    //            int depth = causality.getDepthOfActor(actor);
    //            code.append(_eol + "director.actors[" + i + "]->depth = " + depth
    //                    + ";");
    //            code.append(_eol + "(*(director.actors[" + i
    //                    + "]->initializeFunction))();");
    //            i++;
    //        }

    /** Generate a main loop for an execution under the control of
     *  this DE director.
     *
     *  @return Code for the main loop of an execution.
     *  @exception IllegalActionException If something goes wrong.
     */
    @Override
    public String generateMainLoop() throws IllegalActionException {
        // Need a leading _eol here or else the execute decl. gets stripped out.
        StringBuffer code = new StringBuffer();
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);

        code.append("int " + _sanitizedDirectorName + "_fire() {" + _eol);
        code.append(generateFirePrivateFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("void " + _sanitizedDirectorName
                + "_fireAt(Actor * actor, Time time, int microstep) {" + _eol);
        code.append(generateFireAtFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("Actor * " + _sanitizedDirectorName
                + "_nextActorToFire() {" + _eol);
        code.append(generateNextActorToFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void " + _sanitizedDirectorName
                + "_Preinitialize() {" + _eol);
        code.append(generatePreinitializeMethodBodyCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Prefire() {"
                + _eol);
        code.append(generatePreFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("boolean " + _sanitizedDirectorName + "_Postfire() {"
                + _eol);
        code.append(generatePostFireFunctionCode());
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

    /** Generate The _NextActorToFire function code.
     *  This method is the direct transposition of the _NextActorToFire function of the director
     *  in C.
     *
     *  @return The _fireAt function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generateNextActorToFireFunctionCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol + "Actor * actorToFire = NULL;");
        code.append(_eol + "DEEvent * lastFoundEvent = NULL;");
        code.append(_eol + "DEEvent * nextEvent = NULL;");

        code.append(_eol + "while (true) {");
        code.append(_eol + "if (" + _sanitizedDirectorName
                + ".stopWhenQueueIsEmpty) {");
        code.append(_eol + "if (CQueueIsEmpty(&(" + _sanitizedDirectorName
                + ".cqueue))) {");
        code.append(_eol + "break;");
        code.append(_eol + "}");
        code.append(_eol + "}");

        code.append(_eol + "if (CQueueIsEmpty(&(" + _sanitizedDirectorName
                + ".cqueue))) {");
        code.append(_eol + "if (actorToFire != NULL");
        code.append(_eol + "|| " + _sanitizedDirectorName
                + ".currentModelTime >= " + _sanitizedDirectorName
                + ".stopTime) {");
        code.append(_eol + "break;");
        code.append(_eol + "}");
        code.append(_eol + "else");
        code.append(_eol + "return NULL;");
        code.append(_eol + "}");
        code.append(_eol + "nextEvent = CQueueGet(&(" + _sanitizedDirectorName
                + ".cqueue));");

        code.append(_eol + "if (actorToFire == NULL) {");
        code.append(_eol + "Time currentTime;");

        code.append(_eol + "lastFoundEvent = CQueueTake(&("
                + _sanitizedDirectorName + ".cqueue));");
        code.append(_eol + "currentTime = lastFoundEvent->timestamp;");
        code.append(_eol + "actorToFire = lastFoundEvent->actor;");

        code.append(_eol + "" + _sanitizedDirectorName
                + ".currentModelTime = currentTime;");

        code.append(_eol + "if (lastFoundEvent->microstep == 0)");
        code.append(_eol + "lastFoundEvent->microstep = 1;");
        code.append(_eol + "" + _sanitizedDirectorName
                + ".currentMicrostep = lastFoundEvent->microstep;");

        code.append(_eol + "if (" + _sanitizedDirectorName
                + ".currentModelTime > " + _sanitizedDirectorName
                + ".stopTime) {");
        code.append(_eol + "" + _sanitizedDirectorName
                + ".exceedStopTime = true;");
        code.append(_eol + "return NULL;");
        code.append(_eol + "}");
        code.append(_eol + "} else { ");
        code.append(_eol
                + "if (nextEvent->timestamp == lastFoundEvent->timestamp");
        code.append(_eol
                + "&& nextEvent->microstep == lastFoundEvent->microstep");
        code.append(_eol + "&& nextEvent->actor == actorToFire) {");
        code.append(_eol + " CQueueTake(&(" + _sanitizedDirectorName
                + ".cqueue));");
        code.append(_eol + "} else {");
        code.append(_eol + " break;");
        code.append(_eol + "}");
        code.append(_eol + "}");
        code.append(_eol + "}");

        code.append(_eol + "return actorToFire;");

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
    @Override
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
                ports.getBufferSize(port));
    }

    /** Generate The postfire function code.
     *  @return The postfire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generatePostFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol + "bool moreOutputsToTransfer = false;");

        // Reset the microstep to zero if the next event is
        // in the future.
        code.append(_eol + "if (!CQueueIsEmpty(&(" + _sanitizedDirectorName
                + ".cqueue)) && !moreOutputsToTransfer) {");
        code.append(_eol + "DEEvent * next = CQueueGet(&("
                + _sanitizedDirectorName + ".cqueue));");
        code.append(_eol + "if (next->timestamp > " + _sanitizedDirectorName
                + ".currentModelTime) {");
        code.append(_eol + "" + _sanitizedDirectorName
                + ".currentModelTime = next->timestamp;");
        code.append(_eol + "" + _sanitizedDirectorName
                + ".currentMicrostep = 0;");
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
        code.append(_eol + "bool stop = " + _sanitizedDirectorName
                + ".stopWhenQueueIsEmpty;");
        code.append(_eol + "if (moreOutputsToTransfer) {");
        code.append(_eol + "" + _sanitizedDirectorName + "_fireAt("
                + _sanitizedDirectorName + ".currentActor, "
                + _sanitizedDirectorName + ".currentModelTime, 0);");
        code.append(_eol + "} else if (" + _sanitizedDirectorName
                + ".noMoreActorToFire && (stop || " + _sanitizedDirectorName
                + ".currentModelTime >= " + _sanitizedDirectorName
                + ".stopTime)) {");
        code.append(_eol + "return false;");
        code.append(_eol + "}");
        code.append(_eol + "return true;" + _eol);

        return code.toString();
    }

    /** Generate The prefire function code.
     *  @return The prefire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generatePreFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        if (!_director.isEmbedded()) {
            // A top-level DE director is always ready to fire.
            code.append(_eol + "return true;");
            return code.toString();
        }

        // Update the time
        code.append(_eol
                + _sanitizedDirectorName
                + ".currentModelTime = "
                + _sanitizedDirectorName
                + ".containerActor->actor.container->director->currentModelTime;");
        code.append(_eol
                + _sanitizedDirectorName
                + ".currentMicrostep = "
                + _sanitizedDirectorName
                + ".containerActor->actor.container->director->currentMicrostep;");

        // If embedded, check the timestamp of the next event to decide
        // whether this director is ready to fire.
        code.append(_eol + "Time nextEventTime = Infinity;");

        code.append(_eol + "if (!CQueueIsEmpty(&(" + _sanitizedDirectorName
                + ".cqueue))) {");
        code.append(_eol + "    DEEvent * nextEvent = CQueueGet(&("
                + _sanitizedDirectorName + ".cqueue));");
        code.append(_eol + "    nextEventTime = nextEvent->timestamp;");
        code.append(_eol + "}");

        // If the model time is larger (later) than the first event
        // in the queue, then
        // catch up with the current model time by discarding
        // the old events. Do not, however, discard events whose
        // index but not time has passed.
        code.append(_eol + "while (" + _sanitizedDirectorName
                + ".currentModelTime > nextEventTime) {");
        code.append(_eol + "    DEEvent * skippedEvent = CQueueTake(&("
                + _sanitizedDirectorName + ".cqueue));");
        code.append(_eol + "    if (!CQueueIsEmpty(&(" + _sanitizedDirectorName
                + ".cqueue))) {");
        code.append(_eol + "        DEEvent * nextEvent = CQueueGet(&("
                + _sanitizedDirectorName + ".cqueue));");
        code.append(_eol + "        nextEventTime = nextEvent->timestamp;");
        code.append(_eol + "    } else {");
        code.append(_eol + "        nextEventTime = Infinity;");
        code.append(_eol + "    }");
        code.append(_eol + "}");

        code.append(_eol + "return true;");

        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePreinitializeCode());

        CompositeActor container = ((CompositeActor) _director.getContainer());
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);

        getSanitizedDirectorName();

        code.append(_eol + _eol + "" + _sanitizedDirectorName
                + ".cqueue = *(newCQueue());" + _eol);
        code.append(_eol + "" + _sanitizedDirectorName + ".startTime ="
                + _director.getModelStartTime() + ";");
        if (_director.getModelStopTime().compareTo(Time.POSITIVE_INFINITY) == 0) {
            code.append(_eol + "" + _sanitizedDirectorName
                    + ".stopTime = Infinity;");
        } else {
            code.append(_eol + "" + _sanitizedDirectorName + ".stopTime ="
                    + _director.getModelStopTime() + ";");
        }

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
        code.append(_eol + "" + _sanitizedDirectorName + ".fireAtFunction = "
                + _sanitizedDirectorName + "_fireAt;");
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
            code.append(_eol + sanitizedActorName + "_preinitialize();");
        }

        return code.toString();
    }

    /** Generate the variable declaration.
     *
     * <p> We override the super
     * method, because in DE the declaration of the variables are in
     * the actor's files.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);

        code.append(_eol + "Director " + _sanitizedDirectorName + ";");
        //code.append(_eol + super.generateVariableDeclaration());

        return code.toString();
    }

    /** Generate The wrapup function code.
     *  @return The wrapup function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        Iterator<?> actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            code.append(_eol + sanitizedActorName + "_wrapup();");
        }
        code.append(_eol + "return;" + _eol);

        return code.toString();
    }

    /** Get the files needed by the code generated from this adapter class.
     *  Basically here, we include the "standard" C-written declaration
     *  of the DECQEventQueue
     *  @return A set of strings that are header files needed by the code
     *  generated from this adapter class.
     *  @exception IllegalActionException If something goes wrong.
     */
    @Override
    public Set<String> getHeaderFiles() throws IllegalActionException {
        HashSet<String> result = new HashSet<String>(super.getHeaderFiles());
        CompositeActor container = ((CompositeActor) _director.getContainer());
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);

        result.add("\"" + sanitizedContainerName + ".h\"");
        result.add(processCode("\"$ModelName()_types.h\""));

        return result;
    }

    /** Return whether we need to pad buffers or not.
     *  @return True when we need to pad buffers.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    @Override
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

        //        // Generate variable declarations for input ports.
        //        String inputVariableDeclaration = _generateInputVariableDeclaration(target);
        //        if (inputVariableDeclaration.length() > 1) {
        //            code.append(_eol
        //                    + codeGenerator.comment(name
        //                            + "'s input variable declarations."));
        //            code.append(inputVariableDeclaration);
        //        }
        //
        //        // Generate variable declarations for output ports.
        //        String outputVariableDeclaration = _generateOutputVariableDeclaration(target);
        //        if (outputVariableDeclaration.length() > 1) {
        //            code.append(_eol
        //                    + codeGenerator.comment(name
        //                            + "'s output variable declarations."));
        //            code.append(outputVariableDeclaration);
        //        }
        //
        //        // Generate type convert variable declarations.
        //        String typeConvertVariableDeclaration = _generateTypeConvertVariableDeclaration(target);
        //        if (typeConvertVariableDeclaration.length() > 1) {
        //            code.append(_eol
        //                    + codeGenerator.comment(name
        //                            + "'s type convert variable declarations."));
        //            code.append(typeConvertVariableDeclaration);
        //        }

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
    @Override
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

    /** Generate input variable declarations.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return a String that declares input variables.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    //    private String _generateInputVariableDeclaration(
    //            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
    //        boolean dynamicReferencesAllowed = allowDynamicMultiportReference();
    //
    //        StringBuffer code = new StringBuffer();
    //
    //        Iterator<?> inputPorts = ((Actor) target.getComponent())
    //                .inputPortList().iterator();
    //        while (inputPorts.hasNext()) {
    //            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
    //
    //            if (!inputPort.isOutsideConnected()) {
    //                continue;
    //            }
    //            //targetType(inputPort.getType())
    //            code.append("DEReceiver "
    //                    + CodeGeneratorAdapter.generateName(inputPort));
    //
    //            int bufferSize = ports.getBufferSize(inputPort);
    //            if (inputPort.isMultiport()) {
    //                code.append("[" + inputPort.getWidth() + "]");
    //                if (bufferSize > 1 || dynamicReferencesAllowed) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //            } else {
    //                if (bufferSize > 1) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //            }
    //
    //            code.append(";" + _eol);
    //        }
    //
    //        return code.toString();
    //    }

    //    /** Generate output variable declarations.
    //     *  @return a String that declares output variables.
    //     *  @exception IllegalActionException If thrown while
    //     *  getting port information.
    //     */
    //    private String _generateOutputVariableDeclaration(
    //            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
    //        StringBuffer code = new StringBuffer();
    //
    //        Iterator<?> outputPorts = ((Actor) target.getComponent())
    //                .outputPortList().iterator();
    //
    //        while (outputPorts.hasNext()) {
    //            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
    //
    //            // If either the output port is a dangling port or
    //            // the output port has inside receivers.
    //            if (!outputPort.isOutsideConnected()
    //                    || outputPort.isInsideConnected()) {
    //                code.append("static " + targetType(outputPort.getType()) + " "
    //                        + CodeGeneratorAdapter.generateName(outputPort));
    //
    //                if (outputPort.isMultiport()) {
    //                    code.append("[" + outputPort.getWidthInside() + "]");
    //                }
    //
    //                int bufferSize = ports.getBufferSize(outputPort);
    //
    //                if (bufferSize > 1) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //                code.append(";" + _eol);
    //            }
    //        }
    //
    //        return code.toString();
    //    }

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
                    code.append(""
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

    //    /** Generate type convert variable declarations.
    //     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
    //     *  @return a String that declares type convert variables.
    //     *  @exception IllegalActionException If thrown while
    //     *  getting port information.
    //     */
    //    private String _generateTypeConvertVariableDeclaration(
    //            NamedProgramCodeGeneratorAdapter target)
    //            throws IllegalActionException {
    //        StringBuffer code = new StringBuffer();
    //
    //        Iterator<?> channels = target.getTypeConvertChannels().iterator();
    //        while (channels.hasNext()) {
    //            ProgramCodeGeneratorAdapter.Channel channel = (ProgramCodeGeneratorAdapter.Channel) channels
    //                    .next();
    //            Type portType = ((TypedIOPort) channel.port).getType();
    //
    //            if (getCodeGenerator().isPrimitive(portType)) {
    //
    //                code.append("static ");
    //                code.append(targetType(portType));
    //                code.append(" " + getTypeConvertReference(channel));
    //
    //                //int bufferSize = getBufferSize(channel.port);
    //                int bufferSize = Math.max(
    //                        DFUtilities.getTokenProductionRate(channel.port),
    //                        DFUtilities.getTokenConsumptionRate(channel.port));
    //
    //                if (bufferSize > 1) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //                code.append(";" + _eol);
    //            }
    //        }
    //        return code.toString();
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A hashmap that keeps track of parameters that are referenced for
     *  the associated actor.
     */
    protected HashMap<NamedProgramCodeGeneratorAdapter, HashSet<Parameter>> _referencedParameters = new HashMap<NamedProgramCodeGeneratorAdapter, HashSet<Parameter>>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    //    /** Compare two NamedObjs by full name.
    //     */
    //
    //    private static class FullNameComparator implements Comparator {
    //
    //        /** Compare two NamedObjs by fullName().
    //         *  @return -1 if object1 has fewer dots in its fullName(),
    //         *  1 if object1 has more dots in its fullName(),
    //         *  0 if the objects are the same.
    //         *  If the fullName()s of both NamedObjs have the
    //         *  same number of dots, then return the String compareTo()
    //         *  of the fullName()s.
    //         */
    //        public int compare(Object object1, Object object2) {
    //            String name1 = ((NamedObj) object1).getFullName();
    //            String name2 = ((NamedObj) object2).getFullName();
    //
    //            int index = 0;
    //            int dots1 = 0;
    //            while ((index = name1.indexOf(".", index)) != -1) {
    //                index++;
    //                dots1++;
    //            }
    //            int dots2 = 0;
    //            while ((index = name2.indexOf('.', index)) != -1) {
    //                index++;
    //                dots2++;
    //            }
    //            if (dots1 == dots2) {
    //                return 0;
    //            } else if (dots1 < dots2) {
    //                return -1;
    //            }
    //            return 1;
    //        }
    //    }
}
