/* Class for modular code generators.

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

package ptolemy.cg.kernel.generic.program.procedural.java.modular;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.cg.lib.ModularCompiledSDFTypedCompositeActor;
import ptolemy.cg.lib.Profile;
import ptolemy.cg.lib.Profile.SimJunction;
import ptolemy.data.ArrayToken;
import ptolemy.domains.sdf.lib.SampleDelay;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringBufferExec;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////GenericCodeGenerator

/**
 * Class for modular code generator.
 * 
 * @author Dai Bui, Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating red (rodiers)
 * @Pt.AcceptedRating red (daib)
 */

public class ModularSDFCodeGenerator extends JavaCodeGenerator {

    /**
     * Create a new instance of the Modular java code generator.
     * 
     * @param container
     *            The container.
     * @param name
     *            The name of the Java code generator.
     * @exception IllegalActionException
     *                If the super class throws the exception or error occurs
     *                when setting the file path.
     * @exception NameDuplicationException
     *                If the super class throws the exception or an error occurs
     *                when setting the file path.
     */
    public ModularSDFCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generatorPackageList
                .setExpression("generic.program.procedural.java.modular");
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Create the profile for the model (at this level).
     * 
     * @exception IllegalActionException
     *                when the profile can't be generated.
     */
    public void createProfile() throws IllegalActionException {
        String modelName = NamedProgramCodeGeneratorAdapter
                .generateName(_model);
        String profileClassName = modelName + "_profile";

        StringBuffer profileCode = new StringBuffer();
        
        profileCode.append("import java.util.List;" + _eol);
        profileCode.append("import java.util.LinkedList;" + _eol);
        profileCode.append("import ptolemy.cg.lib.Profile;" + _eol);
        profileCode.append("import ptolemy.kernel.util.IllegalActionException;" + _eol);
        

        profileCode.append(_eol + "public class " + profileClassName + " extends Profile {"
                + _eol);
        profileCode.append(INDENT1
                + "public " + profileClassName + "() { }" + _eol);
        profileCode.append(createESDFGraph());
        
        
        profileCode.append(INDENT1
                + "public List<Profile.Port> ports() {" + _eol);
        profileCode.append(INDENT2 + "List<Profile.Port> ports = new LinkedList<Profile.Port>();" + _eol);
        ModularCompiledSDFTypedCompositeActor model = (ModularCompiledSDFTypedCompositeActor) _model;
        for (Object object : model.portList()) {
            
            IOPort port = (IOPort) object;
            Profile.Port profilePort = model.convertProfilePort(port);
            
            profileCode.append(INDENT2
                    + "ports.add(new Profile.Port(\"" + profilePort.name() + "\", " + profilePort.publisher() + ", "
                    + profilePort.subscriber() + ", " + profilePort.width() + ", "
                    + (port.isInput() ? DFUtilities.getTokenConsumptionRate(port):DFUtilities.getTokenProductionRate(port)) + ", "
                    + ptTypeToCodegenType(((TypedIOPort)port).getType()) + ", "
                    + port.isInput() + ", " + port.isOutput() + ", \"" + profilePort.getPubSubChannelName() + "\"));" + _eol);
        }
        
        profileCode.append(INDENT2 + "return ports;" + _eol);
        profileCode.append(INDENT1 + "}" + _eol);

        profileCode.append("}" + _eol);

        _writeCodeFileName(profileCode, profileClassName + ".java", true, true);
        
        List<String> commands = new LinkedList<String>();
        commands.add("javac -classpath \"."
                + StringUtilities.getProperty("path.separator")
                + StringUtilities.getProperty("ptolemy.ptII.dir") + "\"" + profileClassName + ".java");

        StringBufferExec executeCommands = new StringBufferExec(true);
        executeCommands.setWorkingDirectory(codeDirectory.asFile());
        executeCommands.setCommands(commands);
        executeCommands.start();
        
        int lastSubprocessReturnCode = executeCommands
        .getLastSubprocessReturnCode();
        if (lastSubprocessReturnCode != 0) {
            throw new IllegalActionException(this,
                "Execution of subcommands failed, last process returned "
                        + lastSubprocessReturnCode + ", which is not 0:\n"
                        + executeCommands.buffer.toString());
        }
    }
    
    /**
     * @throws IllegalActionException 
     * 
     */
    public StringBuffer createESDFGraph() throws IllegalActionException {
        
        CompositeActor container = (CompositeActor)getContainer();
        //solve the balance equation
        SDFModularScheduler scheduler = new SDFModularScheduler();
        Map firingVector = scheduler.getFiringVector(container, 1);
        
        Map port2Junction = new HashMap();
        Map<Profile.SimJunction, IOPort> junction2InputPort = new HashMap<Profile.SimJunction, IOPort>();
        Map<Profile.SimJunction, IOPort> junction2OutputPort = new HashMap<Profile.SimJunction, IOPort>();
        
        //construct a ESDF graph for deadlock analysis
        for(Iterator actors = container.deepEntityList().iterator(); actors.hasNext();) {
            Actor actor = (Actor)actors.next();
            if(! (actor instanceof SampleDelay))
            {
                for(Iterator ports = actor.outputPortList().iterator(); ports.hasNext();) {
                    IOPort outputPort = (IOPort) ports.next();
                    
                    if(outputPort instanceof ParameterPort) {
                        continue;   //FIXME
                    }
                    
                    for(Iterator connectedPorts = outputPort.connectedPortList().iterator();
                        connectedPorts.hasNext();) {
                        IOPort connectedPort = (IOPort)connectedPorts.next();
                        
                        if(!connectedPort.isOutput() &&
                                !(connectedPort.getContainer() instanceof SampleDelay)) { //only input ports, this exclude the output ports of the container
                            //each connection has one junction 
                            Profile.SimJunction junction = new Profile.SimJunction(connectedPort, outputPort, 0); //FIXME what is the number of initia tokens
                            
                            port2Junction.put((outputPort.hashCode() ^ connectedPort.hashCode()), junction);
//                            port2Junction.put(connectedPort, junction);
                            
                            junction2InputPort.put(junction, connectedPort);
                            junction2OutputPort.put(junction, outputPort);
                        }
                    }
                }
            } else {
                IOPort sampleDelayOutputPort = ((SampleDelay)actor).output;
                IOPort sampleDelayInputPort = ((SampleDelay)actor).input;
                IOPort outputPort = _getConnectedOutputPort(sampleDelayInputPort);
                if(outputPort == null)
                    continue;
                
                ArrayToken tokens = (ArrayToken)((SampleDelay)actor).initialOutputs.getToken();
                
                for(Iterator connectedPorts = sampleDelayOutputPort.connectedPortList().iterator();
                    connectedPorts.hasNext();) {
                    IOPort connectedPort = (IOPort)connectedPorts.next();
                    
                    if(!connectedPort.isOutput()) { //only input ports, this exclude the output ports of the container
                        
                        
                        //each connection has one junction 
                        Profile.SimJunction junction = new Profile.SimJunction(connectedPort, outputPort, 
                                tokens.length()); //FIXME what is the number of initia tokens
                        
                        port2Junction.put((outputPort.hashCode() ^ connectedPort.hashCode()), junction);
//                        port2Junction.put(connectedPort, junction);
                        
                        junction2InputPort.put(junction, connectedPort);
                        junction2OutputPort.put(junction, outputPort);
                    }
                }
            }
        }
        
        if(firingVector.containsKey(container))
            firingVector.remove(container);
        
        //deadlock analysis
        Map simFiringVector = new HashMap(firingVector);
        boolean deadlocked = true;
        
        while(simFiringVector.size() > 0) {
            
            boolean fireLastIteration = false;
            
            LinkedList actorSet = new LinkedList(simFiringVector.keySet());
            //pick an actor to fire
            for(Iterator actors = actorSet.iterator(); actors.hasNext();) {
                Actor actor = (Actor)actors.next();
                
                if(actor instanceof SampleDelay)
                {
                    simFiringVector.remove(actor);
                    continue;
                }
                
                boolean canFire = true;
                
                //check if we could fire this actor
                for(Iterator ports = actor.inputPortList().iterator(); ports.hasNext();) {
                    IOPort inputPort = (IOPort) ports.next();
                    
                    IOPort connectedPort = _getConnectedOutputPort(inputPort);
                    if(connectedPort == null)
                        continue;
                    
                    Profile.SimJunction junction = (SimJunction) port2Junction.get(inputPort.hashCode() ^ 
                            connectedPort.hashCode());
                    
                    //ports that are connected inside have junctions, we only check those ports
                    if(junction !=  null) {
                        if(junction.counter < DFUtilities.getTokenConsumptionRate(inputPort)) {
                            canFire = false;    //do not have enough tokens at some port to fire
                            break;
                        }
                    }
                }
                
                if(canFire) {
                    
                    //FIXME more complicated for other actors with different firing functions
                    _fireActor(actor, port2Junction);
                    
                    Integer numFirings = ((Integer)simFiringVector.get(actor)).intValue() - 1;  //decrease number of firings
                    
                    if(numFirings <= 0) {
                        simFiringVector.remove(actor);
                    } else
                        simFiringVector.put(actor, numFirings);
                    
                    fireLastIteration = true;   //mark that there is some actor that can fire, not deadlocked yet
                }
            }
            
            //check if deadlock happens
            if(simFiringVector.size() == 0) {
                deadlocked =  false;    //we can simulate
                break;
            } else if( !fireLastIteration ) {   //there are some actors left but none can fire -> deadlocked
                deadlocked = true;
                break;
            }
                
        }
        
        if(deadlocked) {
            return null;
        }
        
        //check if all junctions are in initial states after one firing iteration
        for(Iterator junctions = port2Junction.values().iterator(); junctions.hasNext();) {
            Profile.SimJunction junction = (Profile.SimJunction)junctions.next();
            if(!junction.isInInitalState()) {
                throw new IllegalActionException("Some junction is not in its initial state after one firing iteration");
            }
        }
        
        //unfolding the graph
        
        //find actors that produces output to fire
        List outputActors = new LinkedList();
        
        for(Iterator ports = container.outputPortList().iterator(); ports.hasNext();) {
            IOPort outputPort = (IOPort)ports.next();
            for(Iterator connectedPorts = outputPort.deepInsidePortList().iterator();
                connectedPorts.hasNext();) {
                IOPort connectedPort = (IOPort)connectedPorts.next();
                
                if(connectedPort.isOutput()) {
                    if(!outputActors.contains(connectedPort.getContainer())) {
                        outputActors.add(connectedPort.getContainer());
                    }
                }
            }
        }
        
      //find actors that produces output to fire
        List inputActors = new LinkedList();
        
        for(Iterator ports = container.inputPortList().iterator(); ports.hasNext();) {
            IOPort inputPort = (IOPort)ports.next();
            for(Iterator connectedPorts = inputPort.deepInsidePortList().iterator();
                connectedPorts.hasNext();) {
                IOPort connectedPort = (IOPort)connectedPorts.next();
                
                if(connectedPort.isInput()) {
                    if(!inputActors.contains(connectedPort.getContainer())) {
                        inputActors.add(connectedPort.getContainer());
                    }
                }
            }
        }
        
        Map actor2FiringVectors = new HashMap();
        
        for(Iterator actors = outputActors.iterator(); actors.hasNext();) {
            
            Map actorsFiringCount = new HashMap();
            
            Actor actor = (Actor)actors.next(); //pick an output actor to fire actors backward
            Integer numFirings = (Integer)firingVector.get(actor);

            Integer currentFiring = 0;
            
            Map firing2InputActorFiringVector = new HashMap();
            
            for(Iterator junctions = port2Junction.values().iterator(); junctions.hasNext();) {
                Profile.SimJunction junction = (Profile.SimJunction)junctions.next();
                junction.reset();
            }
            
            while(currentFiring < numFirings) {
                currentFiring++;
                
                _fireActor(actor, port2Junction);
            
                if(actorsFiringCount.get(actor) != null) {
                    actorsFiringCount.put(actor, currentFiring);
                } else {
                    actorsFiringCount.put(actor, new Integer(1));
                }
                
                List firedActors = new LinkedList();
                firedActors.add(actor);
                
                //fire actors connected to this actor
//FIXME                if(!inputActors.contains(actor))
                _fireActorsBackward(actor, firedActors, actorsFiringCount, 
                        port2Junction, junction2OutputPort, firingVector, inputActors);
                
                Map inputActorFiring = new HashMap();
                
                //get the vector of firings of input actors
                for(Iterator actors1 = inputActors.iterator(); actors1.hasNext();) {
                    Actor inputActor = (Actor) actors1.next();
                    if(actorsFiringCount.get(inputActor) != null) {
                        inputActorFiring.put(inputActor, actorsFiringCount.get(inputActor));
                    } else {
                        //FIXME: should we do this?
//                        inputActorFiring.put(inputActor, new Integer(0));
                    }
                }
                firing2InputActorFiringVector.put(currentFiring, inputActorFiring);
            }
            
            actor2FiringVectors.put(actor, firing2InputActorFiringVector);
        }
        
        //group output actors to the same dependence groups
        Map<Map, Map> actorInputDependentGroups = new HashMap<Map, Map>();
        for(Iterator actors = actor2FiringVectors.keySet().iterator(); actors.hasNext();) {
            Actor outputActor = (Actor)actors.next();
            Map inputActorFiringVector = (Map)(actor2FiringVectors.get(outputActor));
            for(Iterator firingIndice = inputActorFiringVector.keySet().iterator(); firingIndice.hasNext();) {
                Integer firingIndex = (Integer) firingIndice.next();
//                Firing firing = new Firing(outputActor, firingIndex);
                
                //get the input vector
                Map inputActorFiring = (Map)inputActorFiringVector.get(firingIndex);
                boolean existed = false;
                Map inputFiring = null;
                for(Iterator inputFirings = actorInputDependentGroups.keySet().iterator(); inputFirings.hasNext();) {
                    inputFiring = (Map)inputFirings.next();
                    if(inputActorFiring.size() == inputFiring.size())
                    {
                        boolean theSameSet = true; 
                            
                        for(Iterator inpActors = inputFiring.keySet().iterator(); inpActors.hasNext();) {
                            Actor inputActor = (Actor)inpActors.next();
                            if(!((Integer)(inputActorFiring.get(inputActor))).equals((Integer)(inputFiring.get(inputActor)))) {
                                theSameSet = false;
                                break;
                            }
                        }
                        
                        if(theSameSet)
                        {
                            existed = true;
                            break;
                        }
                    }
                }
                
                if(existed) {
                    actorInputDependentGroups.get(inputFiring).put(outputActor, firingIndex);
                } else {
                    Map actor2Index = new HashMap();
                    actor2Index.put(outputActor, firingIndex);
                    actorInputDependentGroups.put(inputActorFiring, actor2Index);
                }
            }
        }
        
        //firing forward to obtain all actors that depends on an input vector
        
        //create the partial order of input actor firings
        
        //for each node in the partial order, create a firing function
        
        //between successive firing functions, put a list of junctions which are 
        //connected actors
        
        StringBuffer esdf = new StringBuffer();
        
        esdf.append(INDENT1
                + "public List<Profile.ProfileActor> actors() throws IllegalActionException {" + _eol);
        esdf.append(INDENT2 + "List<Profile.ProfileActor> profiles = new LinkedList<Profile.ProfileActor>();" + _eol);
        ModularCompiledSDFTypedCompositeActor model = (ModularCompiledSDFTypedCompositeActor) _model;
        for (Object object : model.entityList()) {
            
            Actor actor = (Actor) object;
            String className = NamedProgramCodeGeneratorAdapter.generateName((NamedObj) actor);
            
            esdf.append(INDENT2
                    + "profiles.add(new Profile.ProfileActor(\"" + className + "\", " 
                    + (actor instanceof AtomicActor) + "));" + _eol);
        }
        
        esdf.append(INDENT2 + "return profiles;" + _eol);
        esdf.append(INDENT1 + "}" + _eol);
        
        return esdf;
    }

    /**
     * Generate code. This is the main entry point.
     * 
     * @param code
     *            The code buffer into which to generate the code.
     * @return The return value of the last subprocess that was executed. or -1
     *         if no commands were executed.
     * @exception KernelException
     *                If a type conflict occurs or the model is running.
     */
    public int generateCode(StringBuffer code) throws KernelException {

        int returnValue = -1;

        // If the container is in the top level, we are generating code
        // for the whole model. We have to make sure there is a manager,
        // and then preinitialize and resolve types.
        if (_isTopLevel()) {

            // If necessary, create a manager.
            Actor container = ((Actor) getContainer());
            Manager manager = container.getManager();

            if (manager == null) {
                CompositeActor toplevel = (CompositeActor) ((NamedObj) container)
                        .toplevel();
                manager = new Manager(toplevel.workspace(), "Manager");
                toplevel.setManager(manager);
            }

            // set director for transparent composite actors?
            try {
                // TODO: we should bypass preinitializeAndResolveTypes
                // Otherwise we give up the lazyness of the model
                manager.preinitializeAndResolveTypes();
                returnValue = _generateCode(code);
            } finally {
                // We call wrapup here so that the state gets set to idle.
                // This makes it difficult to test the Exit actor.
                try {
                    long startTime = (new Date()).getTime();
                    manager.wrapup();
                    _printTimeAndMemory(startTime, "CodeGenerator: "
                            + "wrapup consumed: ");
                } catch (RuntimeException ex) {
                    // The Exit actor causes Manager.wrapup() to throw this.
                    if (!manager.isExitingAfterWrapup()) {
                        throw ex;
                    }
                }
            }
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            returnValue = _generateCode(code);
        }
        return returnValue;
    }

    /**
     * Generate the main entry point.
     * 
     * @return Return the definition of the main entry point for a program. In
     *         C, this would be defining main().
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    public String generateMainEntryCode() throws IllegalActionException {

        StringBuffer mainEntryCode = new StringBuffer();

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            mainEntryCode
                    .append(_eol
                            + _eol
                            + "public static void main(String [] args) throws Exception {"
                            + _eol + _sanitizedModelName + " model = new "
                            + _sanitizedModelName + "();" + _eol
                            + "model.run();" + _eol + "}" + _eol
                            + "public void run() throws Exception {" + _eol);
        } else {
            mainEntryCode.append(_eol + _eol + "public Object[] fire (" + _eol);

            Iterator<?> inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            boolean addComma = false;
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

                String type = codeGenType2(inputPort.getType());
                if (!type.equals("Token") && !isPrimitive(codeGenType(inputPort.getType()))) {
                    type = "Token";
                }
                for (int i = 0; i < inputPort.getWidth(); i++) {
                    if (addComma) {
                        mainEntryCode.append(", ");
                    }
                    if (DFUtilities.getTokenConsumptionRate(inputPort) > 1) {
                        mainEntryCode.append(type + "[] " + inputPort.getName()
                                + "_" + i);
                    } else {
                        mainEntryCode.append(type + " " + inputPort.getName()
                                + "_" + i);
                    }
                    addComma = true;
                }
            }

            mainEntryCode.append(") {" + _eol);

        }

        return _processCode(mainEntryCode.toString());
    }

    /**
     * Generate the main exit point.
     * 
     * @return Return a string that declares the end of the main() function.
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    public String generateMainExitCode() throws IllegalActionException {

        if (_isTopLevel()) {
            return INDENT1 + "System.exit(0);" + _eol + "}" + _eol + "}" + _eol;
        } else {
            if (_model instanceof CompositeActor && ((CompositeActor) _model).outputPortList().isEmpty()) { 
                return INDENT1 + "return null;" + _eol + "}"
                + _eol + "}" + _eol;
            } else {
                return INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                        + _eol + "}" + _eol;
            }
        }
    }
    
    private void _fireActor(Actor actor, Map port2Junction) throws IllegalActionException {
        //check if we could fire this actor
        for(Iterator ports = actor.inputPortList().iterator(); ports.hasNext();) {
            IOPort inputPort = (IOPort) ports.next();
            IOPort connectedPort = _getConnectedOutputPort(inputPort);
            if(connectedPort == null)
                continue;
            Profile.SimJunction junction = (SimJunction) port2Junction.get(inputPort.hashCode() ^
                    connectedPort.hashCode());
            
            //ports that are connected inside have junctions, we only update those ports
            if(junction !=  null) {
                junction.counter -= DFUtilities.getTokenConsumptionRate(inputPort);
            }
        }
        
        
        for(Iterator ports = actor.outputPortList().iterator(); ports.hasNext();) {
            IOPort outputPort = (IOPort) ports.next();
            for(Iterator inputPorts = _getConnectedInputPorts(outputPort).iterator(); inputPorts.hasNext();)
            {
                IOPort connectedPort = (IOPort) inputPorts.next();
                if(connectedPort.isInput()) {
                    Profile.SimJunction junction = (SimJunction) port2Junction.get((outputPort.hashCode() ^ connectedPort.hashCode()));
                    
                    //ports that are connected inside have junctions, we only update those ports
                    if(junction !=  null) {
                        junction.counter += DFUtilities.getTokenProductionRate(outputPort);
                    }
                }
            }
        }
    }
    
    private void _fireActorsBackward(Actor actor, List firedActors, Map actorsFiringCount, 
            Map port2Junction, Map junction2OutputPort, 
            Map firingVector, List inputActors) throws IllegalActionException {
        
        for(Iterator ports = actor.inputPortList().iterator(); ports.hasNext();) {
            IOPort inputPort = (IOPort) ports.next();
            IOPort connectedPort = _getConnectedOutputPort(inputPort);
            if(connectedPort == null)
                continue;
            Profile.SimJunction junction = (SimJunction) port2Junction.get(inputPort.hashCode() ^ connectedPort.hashCode());
            
            //ports that are connected inside have junctions, we only update those ports
            if(junction !=  null) {
                connectedPort =   (IOPort)(junction2OutputPort.get(junction));
                Actor backwardActor = (Actor) connectedPort.getContainer();
                
                Integer numFiring;
                if( actorsFiringCount.get(backwardActor) != null)
                    numFiring = (Integer) actorsFiringCount.get(backwardActor);
                else
                    numFiring = 0;
                
                boolean actorFired = false;
                while(junction.counter < 0 &&
                        numFiring <= (Integer)firingVector.get(backwardActor)) {  //upstream actor needs to fire to increase the number of tokens so that it is not negative
                    _fireActor(backwardActor, port2Junction);   //FIXME: need to know firing which fire function
                    numFiring++;
                    actorFired = true;
                }
                
                if(actorFired && !firedActors.contains(backwardActor))
                    firedActors.add(backwardActor);
//                actorsFiringCount.put(backwardActor, numFiring);
                
                if(actorFired)
                {
                    actorsFiringCount.put(backwardActor, numFiring);
                    _fireActorsBackward(backwardActor, firedActors, actorsFiringCount, 
                        port2Junction, junction2OutputPort, firingVector, inputActors);
                }
            }
        }
    }
    
    private IOPort _getConnectedOutputPort(IOPort inputPort) {
        IOPort outputPort = null;
        for(Iterator inputPorts = inputPort.connectedPortList().iterator(); inputPorts.hasNext();) {
            IOPort port = (IOPort)inputPorts.next();
            
            if(port.isOutput()) {
                if(port.getContainer() instanceof SampleDelay) {
                    outputPort = _getConnectedOutputPort(((SampleDelay)port.getContainer()).input);
                }
                else
                    outputPort = port;
                break;
            }
        }
        
        return outputPort;
    }
    
    private List _getConnectedInputPorts(IOPort outputPort) {
        List connectedInputPorts = new LinkedList();
        for(Iterator inputPorts = outputPort.connectedPortList().iterator(); inputPorts.hasNext();)
        {
            IOPort connectedPort = (IOPort) inputPorts.next();
            if(connectedPort.isInput()) {
                if(connectedPort.getContainer() instanceof SampleDelay) {
                    connectedInputPorts.addAll(_getConnectedInputPorts(((SampleDelay)connectedPort.getContainer()).output));
                } else {
                    connectedInputPorts.add(connectedPort);
                }
            }
        }
        return connectedInputPorts;
    }
    
    private class Firing {
        public Firing(Actor firingActor, Integer index) {
            actor = firingActor;
            firingIndex = index;
        }
        
        public Actor actor;
        Integer firingIndex;
    }
}
