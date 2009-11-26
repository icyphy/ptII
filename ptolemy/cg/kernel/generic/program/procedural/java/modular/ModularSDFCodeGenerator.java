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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
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
import ptolemy.cg.lib.Profile.FiringFunction;
import ptolemy.cg.lib.Profile.FiringFunctionPort;
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
        profileCode.append(createGraph());
        
        
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
    public StringBuffer createGraph() throws IllegalActionException {
        
        CompositeActor container = (CompositeActor)getContainer();
        //solve the balance equation
        SDFModularScheduler scheduler = new SDFModularScheduler();
        Map firingVector = scheduler.getFiringVector(container, 1);
        
        Map port2Junction = new HashMap();
        Map<SimJunction, IOPort> junction2InputPort = new HashMap<SimJunction, IOPort>();
        Map<SimJunction, IOPort> junction2OutputPort = new HashMap<SimJunction, IOPort>();
        
        _createExpandedGraph(container, port2Junction, junction2InputPort, junction2OutputPort);
        
        if(firingVector.containsKey(container))
            firingVector.remove(container);
        
        if(!_deadlockAnalysis(firingVector, port2Junction))
            return null;
        
        //unfolding the graph
        List actorFirings = new LinkedList();
        
        _createDependencyGraph(container, firingVector, port2Junction, actorFirings);
        
        _deriveFiringFunctionDependency(container, firingVector, actorFirings);
                
        _printGraph(actorFirings);

        //find output actors (produce external tokens)
        List<SimFiringFunction> outputFiringFunctions = new LinkedList();
        
        for(Iterator ports = container.outputPortList().iterator(); ports.hasNext();) {
            IOPort outputPort = (IOPort)ports.next();
            for(Iterator connectedPorts = outputPort.deepInsidePortList().iterator();
                connectedPorts.hasNext();) {
                IOPort connectedPort = (IOPort)connectedPorts.next();
                
                if(connectedPort.isOutput()) {
                    Actor connectedActor = (Actor) connectedPort.getContainer();
                    if(connectedActor instanceof ModularCompiledSDFTypedCompositeActor) {
                        List<FiringFunction> connectedActorFuntions = ((ModularCompiledSDFTypedCompositeActor)connectedActor).getProfile().firings();
                        int numFunctions = connectedActorFuntions.size();
                        
                        for(int j = 0; j < numFunctions; j++) {
                            for(FiringFunctionPort port : connectedActorFuntions.get(j).ports) {
                                if(!port.isInput && port.externalPortName.equals(connectedPort.getName())) {
                                    
                                    SimFiringFunction function =  _getFiringFunction(outputFiringFunctions, connectedActor, j);
                                    if(function == null) {
                                        function =  new SimFiringFunction(connectedActor, j);
                                        outputFiringFunctions.add(function);
                                    }
                                }
                            }
                        }
                    } else {
                        SimFiringFunction function =  _getFiringFunction(outputFiringFunctions, connectedActor, 0);
                        
                        if(function == null) {
                            function =  new SimFiringFunction(connectedActor, 0);
                            outputFiringFunctions.add(function);
                        }
                    }
                }
            }
        }
        
        //find input actors (consume external tokens)
        
        List<SimFiringFunction> inputFiringFunctions = new LinkedList();
        
        for(Iterator ports = container.inputPortList().iterator(); ports.hasNext();) {
            IOPort inputPort = (IOPort)ports.next();
            for(Iterator connectedPorts = inputPort.deepInsidePortList().iterator();
                connectedPorts.hasNext();) {
                IOPort connectedPort = (IOPort)connectedPorts.next();
                
                if(connectedPort.isInput()) {
                    Actor connectedActor = (Actor) connectedPort.getContainer();
                    if(connectedActor instanceof ModularCompiledSDFTypedCompositeActor) {
                        List<FiringFunction> connectedActorFuntions = ((ModularCompiledSDFTypedCompositeActor)connectedActor).getProfile().firings();
                        int numFunctions = connectedActorFuntions.size();
                        for(int j = 0; j < numFunctions; j++) {
                            for(FiringFunctionPort port : connectedActorFuntions.get(j).ports) {
                                if(port.isInput && port.externalPortName.equals(connectedPort.getName())) {
                                    SimFiringFunction function =  _getFiringFunction(inputFiringFunctions, connectedActor, j);
                                    if(function == null) {
                                        function =  new SimFiringFunction(connectedActor, j);
                                        inputFiringFunctions.add(function);
                                    }
                                }
                            }
                        }
                    } else {
                        SimFiringFunction function =  _getFiringFunction(inputFiringFunctions, connectedActor, 0);
                        
                        if(function == null) {
                            function =  new SimFiringFunction(connectedActor, 0);
                            inputFiringFunctions.add(function);
                        }
                    }
                }
            }
        }
        
        //clustering
        Map outputInputDependence = new HashMap();
        for(Iterator firings = actorFirings.iterator(); firings.hasNext();) {
            
            Firing firing = (Firing) firings.next();
            if( _getFiringFunction(outputFiringFunctions, firing.actor, firing.firingFunction) != null) {
                Set inputFirings =  (Set) new HashSet();
                Set searchedFirings = (Set) new HashSet();
                
                _getDependentBackwardFiring(firing, inputFirings, searchedFirings,
                        inputFiringFunctions, outputFiringFunctions);
                outputInputDependence.put(firing, inputFirings);
                
                System.out.println("Out put firing: " + firing.actor.getFullName() + ", firing Index: " + firing.firingIndex + " function: " + firing.firingFunction);
                //_printGraph(inputFirings);
            }
        }
        
        Map clusteredOutputs = new HashMap();
        
        
        //cluster output firings
        for(Iterator outputFirings = outputInputDependence.keySet().iterator(); outputFirings.hasNext();) {
            Firing outputFiring = (Firing) outputFirings.next();
            Set inputFirings = (Set) outputInputDependence.get(outputFiring);
            boolean existed = false;
            for(Iterator inputFiringsIter = clusteredOutputs.keySet().iterator(); inputFiringsIter.hasNext();) {
                Set firings = (Set) inputFiringsIter.next();
                if(inputFirings.equals(firings)) {
                    Set clusteredOutputFirings = (Set) clusteredOutputs.get(firings);
                    clusteredOutputFirings.add(outputFiring);
                    existed = true;
                    break;
                }
            }
            
            if(!existed) {
                Set clusteredOutputFirings = new HashSet();
                clusteredOutputFirings.add(outputFiring);
                Set firings = (Set) new HashSet(inputFirings);
                clusteredOutputs.put(firings, clusteredOutputFirings);
            }
        }
        
        
        Set<IOPort> inInputConnectedPorts = new HashSet();
        Set<IOPort> inOutputConnectedPorts = new HashSet();

        for(Object inputPort: container.inputPortList()) {
            inInputConnectedPorts.addAll(((IOPort)inputPort).deepInsidePortList());
        }
        
        for(Object outputPort: container.outputPortList()) {
            inOutputConnectedPorts.addAll(((IOPort)outputPort).deepInsidePortList());
        }
        
        List<FiringCluster> firingClusters = new LinkedList();
        
        Set clusters = new HashSet();
        
        //create clusters of actors
        for(Iterator firings = clusteredOutputs.keySet().iterator(); firings.hasNext();) {
            Set inputFirings = (Set) firings.next();
            Set clusteredFirings = new HashSet();
            FiringCluster firingCluster = new FiringCluster();
            
            Set searchedFirings = new HashSet();
            
            Set ouputFirings = (Set) clusteredOutputs.get(inputFirings);
            
            for(Object ouputFiring:ouputFirings) {
                _clusterFirings((Firing)ouputFiring, clusteredFirings, searchedFirings, inputFirings,
                        inputFiringFunctions, outputFiringFunctions, outputInputDependence);
            }
            
            //sort actors in a firing cluster
            List sortedFirings = new LinkedList(clusteredFirings);
            if(sortedFirings.size() > 0) {
                Set visitedFirings = new HashSet();
                _computeFiringDepth((Firing) sortedFirings.get(0), visitedFirings);
                FiringComparator comparator = new FiringComparator();
                Collections.sort(sortedFirings, comparator);
            }
            
            System.out.println("New cluster");
            for(Object f:sortedFirings) {
                firingCluster.actorFirings.add((Firing)f);
                ((Firing)f).cluster = firingCluster;
                Actor actor = ((Firing)f).actor;
                
                if(actor instanceof ModularCompiledSDFTypedCompositeActor) {
                    if(_getFiringFunction(inputFiringFunctions, actor, ((Firing)f).firingFunction) != null) {
                        
                        Set<IOPort> inputPorts = new HashSet(actor.inputPortList());
                        
                        inputPorts.retainAll(inInputConnectedPorts);
                        
                        List<FiringFunctionPort> inputFiringPorts = ((ModularCompiledSDFTypedCompositeActor)actor).getProfile().firings().get(((Firing)f).firingFunction).ports;
                        for(IOPort inputPort: inputPorts) {
                            for(FiringFunctionPort firingPort : inputFiringPorts) {
                                if(firingPort.isInput && firingPort.externalPortName.equals(inputPort.getName())) {
                                    firingCluster.inputPorts.add(inputPort);
                                    break;
                                }
                            }
                        }
                    }
                    
                    if(_getFiringFunction(outputFiringFunctions, actor, ((Firing)f).firingFunction) != null) {
                        
                        Set<IOPort> outputPorts = new HashSet(actor.outputPortList());
                        outputPorts.retainAll(inOutputConnectedPorts);
                        
                        List<FiringFunctionPort> outputFiringPorts = ((ModularCompiledSDFTypedCompositeActor)actor).getProfile().firings().get(((Firing)f).firingFunction).ports;
                        for(IOPort outputPort: outputPorts) {
                            for(FiringFunctionPort firingPort : outputFiringPorts) {
                                if(!firingPort.isInput && firingPort.externalPortName.equals(outputPort.getName())) {
                                    firingCluster.outputPorts.add(outputPort);
                                    break;
                                }
                            }
                        }
                        firingCluster.outputPorts.addAll(outputPorts);
                    }
                } else {
                    if(_getFiringFunction(inputFiringFunctions, actor, ((Firing)f).firingFunction) != null) {
                        
                        Set inputPorts = new HashSet(actor.inputPortList());
                        
                        inputPorts.retainAll(inInputConnectedPorts);
                        firingCluster.inputPorts.addAll(inputPorts);
                    }
                    
                    if(_getFiringFunction(outputFiringFunctions, actor, ((Firing)f).firingFunction) != null) {

                        Set outputPorts = new HashSet(actor.outputPortList());
                        outputPorts.retainAll(inOutputConnectedPorts);
                        firingCluster.outputPorts.addAll(outputPorts);
                    }
                }
                
                System.out.println("Clustered firing: " + actor.getFullName() + ", firing Index: " + ((Firing)f).firingIndex + " function: " + ((Firing)f).firingFunction);
            }
            
            firingClusters.add(firingCluster);
            
            clusters.add(sortedFirings);
        }
        
        //create clusters dependency
        for(FiringCluster cluster:firingClusters) { 
            for(Firing f:cluster.actorFirings) {
                for(Firing nextFiring:(f.nextActorFirings)) {
                    if(!cluster.nextClusters.contains(nextFiring.cluster)) { 
                        cluster.nextClusters.add(nextFiring.cluster);
                    }
                }
                
                cluster.nextClusters.remove(cluster);
                
                for(Firing previousFiring:(f.previousActorFirings)) {
                    if(!cluster.previousClusters.contains(previousFiring.cluster)) { 
                        cluster.previousClusters.add(previousFiring.cluster);
                    }
                    
                }
                
                cluster.previousClusters.remove(cluster);
                
                for(Firing nextIterationFiring: (f.nextIterationFirings)) {
                    if(!cluster.nextIterationClusters.contains(nextIterationFiring.cluster)) {
                        cluster.nextIterationClusters.add(nextIterationFiring.cluster);
                    }
                }
                
                cluster.nextIterationClusters.remove(cluster);
                
                for(Firing previousIterationFiring: (f.previousIterationFirings)) {
                    if(!cluster.previousIterationClusters.contains(previousIterationFiring.cluster)) {
                        cluster.previousIterationClusters.add(previousIterationFiring.cluster);
                    }
                }
                
                cluster.previousIterationClusters.remove(cluster);
              
            }
        }
        
        //sort clusters
        if(firingClusters.size() > 0) {
            Map visitedCluster = new HashMap();
            _computeClusterDepth(firingClusters.get(0), visitedCluster);
            
            ClusterComparator clusterComparator = new ClusterComparator();
            Collections.sort(firingClusters, clusterComparator);
        }
        
        
        //generate profile
        
        StringBuffer esdf = new StringBuffer();
        
        esdf.append(INDENT1
                + "public List<FiringFunction> firings() throws IllegalActionException {" + _eol);
        esdf.append(INDENT2 + "List<FiringFunction> firingFunctions = new LinkedList<FiringFunction>();" + _eol);
        esdf.append(INDENT2
                + "FiringFunction firingFunction;" + _eol + _eol);
        for(FiringCluster cluster:firingClusters) { 
           
            int index = firingClusters.indexOf(cluster);
            
            esdf.append(INDENT2
                    + "firingFunction = new FiringFunction(" + index + ");" + _eol);
            
            String externalPortName;
            
            //add ports name and rate
            for(IOPort inputPort:cluster.inputPorts) {
                externalPortName = "";
                for(Object connectedPort: inputPort.connectedPortList()) {
                    if(container.portList().contains(connectedPort)) {
                        externalPortName = ((IOPort)connectedPort).getName();
                        break;
                    }
                }
                
                if(!externalPortName.equals("")) {
                    esdf.append(INDENT2
                        + "firingFunction.ports.add(new FiringFunctionPort(\"" + inputPort.getName() 
                        + "\",\"" + externalPortName
                        + "\"," + DFUtilities.getTokenConsumptionRate(inputPort) + "," + inputPort.isInput() + "));" + _eol);
                }
            }
            
            for(IOPort outputPort:cluster.outputPorts) {
                
                externalPortName = "";
                for(Object connectedPort: outputPort.connectedPortList()) {
                    if(container.portList().contains(connectedPort)) {
                        externalPortName = ((IOPort)connectedPort).getName();
                        break;
                    }
                }
                
                if(!externalPortName.equals(""))
                    esdf.append(INDENT2
                        + "firingFunction.ports.add(new FiringFunctionPort(\"" + outputPort.getName()
                        + "\",\"" + externalPortName
                        + "\"," + DFUtilities.getTokenProductionRate(outputPort) + "," + outputPort.isInput() + "));" + _eol);
            }
            
            for(FiringCluster nextCluster:cluster.nextClusters) {
                esdf.append(INDENT2
                        + "firingFunction.nextFiringFunctions.add(" + firingClusters.indexOf(nextCluster) + ");" + _eol);
                
            }
            
            for(FiringCluster previousCluster:cluster.previousClusters) {
                esdf.append(INDENT2
                        + "firingFunction.previousFiringFunctions.add(" + firingClusters.indexOf(previousCluster) + ");" + _eol);
                
            }
            
            for(FiringCluster nextCluster:cluster.nextIterationClusters) {
                esdf.append(INDENT2
                        + "firingFunction.nextIterationFirings.add(" + firingClusters.indexOf(nextCluster) + ");" + _eol);
                
            }
            
            for(FiringCluster previousCluster:cluster.previousIterationClusters) {
                esdf.append(INDENT2
                        + "firingFunction.previousIterationFirings.add(" + firingClusters.indexOf(previousCluster) + ");" + _eol);
                
            }
            
            esdf.append(INDENT2
                    + "firingFunctions.add(firingFunction);" + _eol + _eol);
        }
        
        
        //draw cluster graphs
        StringBuffer graph = new StringBuffer();
        
        graph.append("digraph G {" + _eol + "\tcompound=true;" + _eol);
        
        for(Iterator firings = actorFirings.iterator(); firings.hasNext();) {
            Firing firing = (Firing) firings.next();
            
            for(Iterator nextFirings = firing.nextActorFirings.iterator(); nextFirings.hasNext();) {
                Firing nextFiring = (Firing) nextFirings.next();
                graph.append("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction 
                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_" 
                        + nextFiring.firingFunction + ";" + _eol);
            }
            
//            for(Iterator nextFirings = firing.nextIterationFirings.iterator(); nextFirings.hasNext();) {
//                Firing nextFiring = (Firing) nextFirings.next();
//                graph.append("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction 
//                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_" 
//                        + nextFiring.firingFunction + "[style=dotted];" + _eol);
//            }
        }
        
        for(FiringCluster cluster:firingClusters) {
            graph.append("\t" + "subgraph cluster" + firingClusters.indexOf(cluster) + "{" + _eol
                    + "\t\tlabel=\"Cluster_" + firingClusters.indexOf(cluster) + "\";" + _eol);
            for(Firing firing: cluster.actorFirings) {
                graph.append("\t\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction + ";" + _eol);
            }
            graph.append("\t}" + _eol);
        }
        
        StringBuffer clustersGraph = new StringBuffer();
        
        clustersGraph.append("digraph clusteredG {" + _eol);
        
        for(FiringCluster cluster:firingClusters) { 
            
            int index = firingClusters.indexOf(cluster);
//            Firing firing = cluster.actorFirings.get(0);
            
            for(FiringCluster nextCluster:cluster.nextClusters) {
//                Firing nextFiring = nextCluster.actorFirings.get(0);
//                graph.append("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction 
//                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_" 
//                        + nextFiring.firingFunction 
//                        + " [ltail=cluster" + index + ", lhead=cluster" + firingClusters.indexOf(nextCluster) + "];" + _eol);
                clustersGraph.append("\t" + "Cluster_" + index + " -> "  + "Cluster_" 
                        + firingClusters.indexOf(nextCluster) + ";" + _eol);
            }
            
            for(FiringCluster nextCluster:cluster.nextIterationClusters) {
//                Firing nextFiring = nextCluster.actorFirings.get(0);
//                graph.append("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction 
//                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_" 
//                        + nextFiring.firingFunction 
//                        + " [style=dotted, ltail=cluster" + index + ", lhead=cluster" + firingClusters.indexOf(nextCluster) + "];" + _eol);
                clustersGraph.append("\t" + "Cluster_" + index + " -> "  + "Cluster_" + firingClusters.indexOf(nextCluster) + "[style=dotted];" + _eol);
            }
        }
        
        graph.append("}" + _eol);
        clustersGraph.append("}");
        
        System.out.println(graph);
        System.out.println(clustersGraph);
        
        esdf.append(INDENT2 + "return firingFunctions;" + _eol);
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
    
    //////////////////////////////////////////////////////////////////////
    /// private methods
    private void _createDependencyGraph(CompositeActor container, Map firingVector, 
            Map port2Junction, List actorFirings) throws IllegalActionException {
      //create the dependency graph
        for(Iterator actors = container.deepEntityList().iterator(); actors.hasNext();) {
            Actor actor = (Actor) actors.next();
            
            if(actor instanceof SampleDelay)
                continue;
            
            Integer numFirings = (Integer) firingVector.get(actor);
            for(Iterator ports = actor.outputPortList().iterator(); ports.hasNext();) {
                IOPort outputPort = (IOPort) ports.next();
                
                for(Iterator inputPorts = _getConnectedInputPorts(outputPort).iterator(); inputPorts.hasNext();) {
                    IOPort connectedPort = (IOPort) inputPorts.next();
                    
                    SimJunction junction = (SimJunction) port2Junction.get(outputPort.hashCode() ^ connectedPort.hashCode());
                    
                    if(junction !=  null) {
                        junction.reset();
                        Actor nextActor = (Actor) connectedPort.getContainer();
                        int nextActorNumFirings = (Integer) firingVector.get(nextActor);
                        
                        List<FiringFunction> currentActorFirings = null;
                        List<FiringFunction> nextActorFirings = null;
                        
                        int numFireFunctions;
                        int nextActorFireFunctions;
                        
                        if(actor instanceof ModularCompiledSDFTypedCompositeActor) {
                            currentActorFirings = ((ModularCompiledSDFTypedCompositeActor)actor).getProfile().firings();
                            numFireFunctions = currentActorFirings.size();
                        } else
                            numFireFunctions = 1;
                        
                        
                        if(nextActor instanceof ModularCompiledSDFTypedCompositeActor) {
                            nextActorFirings = ((ModularCompiledSDFTypedCompositeActor)nextActor).getProfile().firings();
                            nextActorFireFunctions = nextActorFirings.size();
                        } else {
                            nextActorFireFunctions = 1;
                        }
                        
                        int iterationCount = 1;
                        int firingFunctionCount = 0;
                        boolean firstConsumption = true;
                        
                        for(int i = 0; i <= numFirings; i++) {
                            nextFiring:
                            for(int j = 0; j < numFireFunctions; j++) {
                                boolean fired = false;
                                if(i > 0) {
                                    if(actor instanceof ModularCompiledSDFTypedCompositeActor) {
                                        for(FiringFunctionPort port:currentActorFirings.get(j).ports) {
                                            if(port.externalPortName.equals(outputPort.getName())) {
                                                junction.counter += port.rate;
                                                fired = true;
                                                break;
                                            }
                                        }
                                            
                                    } else {
                                        //normal actor
                                        junction.counter += DFUtilities.getTokenProductionRate(outputPort);
                                        fired = true;
                                    }
                                } else {
                                    
                                    //check the backward dependency
                                    if(junction.counter > 0 && firstConsumption) {
                                        //get the last fire function that put tokens into the junction
                                        int lastFireFunction = numFireFunctions-1;
                                        if(actor instanceof ModularCompiledSDFTypedCompositeActor) {
                                            FOUND_FUNCTION:
                                            for( int k = numFireFunctions-1; k >= 0 ; k--) {
                                                for(FiringFunctionPort port:currentActorFirings.get(k).ports) {
                                                    if(port.externalPortName.equals(outputPort.getName())) {
                                                        lastFireFunction = k;
                                                        break FOUND_FUNCTION;
                                                    }
                                                }
                                            }
                                        }
                                        
                                        int firstFireFunction = 0;
                                        
                                        if(nextActor instanceof ModularCompiledSDFTypedCompositeActor) {
                                            FOUND_NEXT_ACTOR_FUNCTION:
                                            for(int n = 0; n < numFireFunctions; n++) {
                                                for(FiringFunctionPort port:nextActorFirings.get(n).ports) {
                                                    if(port.externalPortName.equals(outputPort.getName())) {
                                                        firstFireFunction = n;
                                                        break FOUND_NEXT_ACTOR_FUNCTION;
                                                    }
                                                }
                                            }
                                        }
                                        
                                        Firing previousIterationFiring = _getFiring(actor, numFirings, lastFireFunction, actorFirings);
                                        if(previousIterationFiring == null)
                                        {
                                            previousIterationFiring = new Firing(actor, numFirings, lastFireFunction);
                                            actorFirings.add(previousIterationFiring);
                                        }
                                        
                                        Firing nextIterationFiring = _getFiring(nextActor, 1, firstFireFunction, actorFirings);
                                        if(nextIterationFiring == null)
                                        {
                                            nextIterationFiring = new Firing(nextActor, 1, firstFireFunction);
                                            actorFirings.add(nextIterationFiring);
                                        }
                                        
                                        previousIterationFiring.nextIterationFirings.add(nextIterationFiring);
                                        nextIterationFiring.previousIterationFirings.add(previousIterationFiring);
                                        
                                        firstConsumption = false;
                                    }
                                    
                                    //backward dependency to the firing of the previous iteration
                                    Firing previousIterationFiring = _getFiring(actor, numFirings, j, actorFirings);
                                    if(previousIterationFiring == null)
                                    {
                                        previousIterationFiring = new Firing(actor, numFirings, j);
                                        actorFirings.add(previousIterationFiring);
                                    }
                                    
                                    Firing nextIterationFiring = _getFiring(actor, 1, j, actorFirings);
                                    if(nextIterationFiring == null)
                                    {
                                        nextIterationFiring = new Firing(actor, 1, j);
                                        actorFirings.add(nextIterationFiring);
                                    }
                                    
                                    previousIterationFiring.nextIterationFirings.add(nextIterationFiring);
                                    nextIterationFiring.previousIterationFirings.add(previousIterationFiring);
                                    
                                    fired = true;
                                }
                                
                                if(fired) {
                                    
                                    Firing currentFiring = null;
                                    
                                    if(i > 0) {
                                        currentFiring = _getFiring(actor, i, j, actorFirings);
                                        if(currentFiring == null)
                                        {
                                            currentFiring = new Firing(actor, i, j);
                                            actorFirings.add(currentFiring);
                                        }
                                    }
                                    
                                    //next actor consumes tokens
                                    while(iterationCount <= nextActorNumFirings) {
                                        while(firingFunctionCount < nextActorFireFunctions) {
                                            boolean nextActorFired = false;
                                            //try to consume some tokens
                                            if(nextActor instanceof ModularCompiledSDFTypedCompositeActor) {
                                                for(FiringFunctionPort port:nextActorFirings.get(firingFunctionCount).ports) {
                                                    if(port.externalPortName.equals(connectedPort.getName())) {
                                                        
                                                        if(junction.counter >= port.rate) {
                                                            junction.counter -= port.rate;
                                                            nextActorFired = true;
                                                        } else {
                                                            continue nextFiring;
                                                        }
                                                        break;
                                                    }
                                                }
                                            } else {
                                                if(junction.counter >= DFUtilities.getTokenConsumptionRate(connectedPort)) {
                                                    junction.counter -= DFUtilities.getTokenConsumptionRate(connectedPort);
                                                    nextActorFired = true;
                                                } else{
                                                    continue nextFiring;
                                                }
                                            }
                                            
                                            if(nextActorFired && currentFiring != null) {
                                                Firing nextFiring = _getFiring(nextActor, iterationCount, firingFunctionCount, actorFirings);
                                                if(nextFiring == null)
                                                {
                                                    nextFiring = new Firing(nextActor, iterationCount, firingFunctionCount);
                                                    actorFirings.add(nextFiring);
                                                }
                                                
                                                currentFiring.nextActorFirings.add(nextFiring);
                                                nextFiring.previousActorFirings.add(currentFiring);
                                            }
                                            
                                            firingFunctionCount++;
                                        }
                                        
                                        if(firingFunctionCount >= nextActorFireFunctions)
                                            firingFunctionCount = 0;
                                        
                                        iterationCount++;
                                    }
                                    
                                    if(iterationCount >nextActorNumFirings)
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    private void _createExpandedGraph(CompositeActor container, Map port2Junction, 
            Map junction2InputPort, Map junction2OutputPort) throws IllegalActionException {
      //construct a extended graph for deadlock analysis
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
                            SimJunction junction = new SimJunction(connectedPort, outputPort, 0); //FIXME what is the number of initial tokens
                            
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
                        SimJunction junction = new SimJunction(connectedPort, outputPort, 
                                tokens.length()); //FIXME what is the number of initial tokens
                        
                        port2Junction.put((outputPort.hashCode() ^ connectedPort.hashCode()), junction);
                        
                        junction2InputPort.put(junction, connectedPort);
                        junction2OutputPort.put(junction, outputPort);
                    }
                }
            }
        }
    }
    
    private boolean _deadlockAnalysis(Map firingVector, Map port2Junction ) throws IllegalActionException {
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
                
                if(! (actor instanceof ModularCompiledSDFTypedCompositeActor)) {
                    boolean canFire = true;
                    
                    //check if we could fire this actor
                    for(Iterator ports = actor.inputPortList().iterator(); ports.hasNext();) {
                        IOPort inputPort = (IOPort) ports.next();
                        
                        IOPort connectedPort = _getConnectedOutputPort(inputPort);
                        if(connectedPort == null)
                            continue;
                        
                        SimJunction junction = (SimJunction) port2Junction.get(inputPort.hashCode() ^ 
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
                } else {

                    //FIXME
                    boolean canFire = true;
                    
                    //check if we could fire this actor
                    for(Iterator ports = actor.inputPortList().iterator(); ports.hasNext();) {
                        IOPort inputPort = (IOPort) ports.next();
                        
                        IOPort connectedPort = _getConnectedOutputPort(inputPort);
                        if(connectedPort == null)
                            continue;
                        
                        SimJunction junction = (SimJunction) port2Junction.get(inputPort.hashCode() ^ 
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
            return false;
        }
        
        //check if all junctions are in initial states after one firing iteration
        for(Iterator junctions = port2Junction.values().iterator(); junctions.hasNext();) {
            SimJunction junction = (SimJunction)junctions.next();
            if(!junction.isInInitalState()) {
                throw new IllegalActionException("Some junction is not in its initial state after one firing iteration");
            }
        }
        return true;
    }
    
    private void _deriveFiringFunctionDependency(CompositeActor container, Map firingVector,
            List actorFirings) throws IllegalActionException {
      //dependency between firing of each actor
        for(Iterator actors = container.deepEntityList().iterator(); actors.hasNext();) {
             Actor actor = (Actor) actors.next();
             
             if(actor instanceof SampleDelay)
                 continue;
             
             Integer numFiring = (Integer) firingVector.get(actor);
             for(int i = 1; i <= numFiring; i++) {
                 if(actor instanceof ModularCompiledSDFTypedCompositeActor) {
                     List<FiringFunction> actorFiringFunctions = ((ModularCompiledSDFTypedCompositeActor)actor).getProfile().firings();
                     int numFireFunctions = actorFiringFunctions.size();
                     
                     for(int j = 0; j < numFireFunctions; j++) {
                         Firing firing = _getFiring(actor, i, j, actorFirings);
                         if(firing == null)
                         {
                             firing = new Firing(actor, i, j);
                             actorFirings.add(firing);
                         }
                         
                         for(Integer index :actorFiringFunctions.get(j).previousFiringFunctions) {
                             Firing previousFiring = _getFiring(actor, i, index, actorFirings);
                             
                             if(previousFiring == null)
                             {
                                 previousFiring = new Firing(actor, i, index);
                                 actorFirings.add(previousFiring);
                             }
                             firing.previousActorFirings.add(previousFiring);
                             previousFiring.nextActorFirings.add(firing);
                         }
                         
                         for(Integer index :actorFiringFunctions.get(j).nextFiringFunctions) {
                             Firing nextFiring = _getFiring(actor, i, index, actorFirings);
                             
                             if(nextFiring == null)
                             {
                                 nextFiring = new Firing(actor, i, index);
                                 actorFirings.add(nextFiring);
                             }
                             
                             firing.nextActorFirings.add(nextFiring);
                             nextFiring.previousActorFirings.add(firing);
                         }
                         
                         if( i > 1) {
                             for(Integer index : actorFiringFunctions.get(firing.firingFunction).previousIterationFirings) {
                                 Firing previousFiring = _getFiring(actor, i-1, index, actorFirings);
                                 
                                 if(previousFiring == null)
                                 {
                                     previousFiring = new Firing(actor, i-1, index);
                                     actorFirings.add(previousFiring);
                                 }
                                 
                                 firing.previousActorFirings.add(previousFiring);
                                 previousFiring.nextActorFirings.add(firing);
                             }
                         }
                         
                         //dependency between firings of one function
                         if(i > 1) {
                             Firing previousFiring = _getFiring(actor, i-1, j, actorFirings);
                             if(previousFiring == null)
                             {
                                 previousFiring = new Firing(actor, i-1, j);
                                 actorFirings.add(previousFiring);
                             }
                             
                             firing.previousActorFirings.add(previousFiring);
                             previousFiring.nextActorFirings.add(firing);
                         }
                     }
                 } else {
                     if( i > 1)
                     {
                         Firing firing = _getFiring(actor, i, 0, actorFirings);
                         
                         if(firing == null)
                         {
                             firing = new Firing(actor, i, 0);
                             actorFirings.add(firing);
                         }
                         
                         Firing previousFiring = _getFiring(actor, i-1, 0, actorFirings);
                         if(previousFiring == null)
                         {
                             previousFiring = new Firing(actor, i-1, 0);
                             actorFirings.add(previousFiring);
                         }
                         
                         firing.previousActorFirings.add(previousFiring);
                         previousFiring.nextActorFirings.add(firing);
                     }
                 }
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
            SimJunction junction = (SimJunction) port2Junction.get(inputPort.hashCode() ^
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
                    SimJunction junction = (SimJunction) port2Junction.get((outputPort.hashCode() ^ connectedPort.hashCode()));
                    
                    //ports that are connected inside have junctions, we only update those ports
                    if(junction !=  null) {
                        junction.counter += DFUtilities.getTokenProductionRate(outputPort);
                    }
                }
            }
        }
    }
    
//    private void _fireActorsBackward(Actor actor, List firedActors, Map actorsFiringCount, 
//            Map port2Junction, Map junction2OutputPort, 
//            Map firingVector, List inputActors) throws IllegalActionException {
//        
//        for(Iterator ports = actor.inputPortList().iterator(); ports.hasNext();) {
//            IOPort inputPort = (IOPort) ports.next();
//            IOPort connectedPort = _getConnectedOutputPort(inputPort);
//            if(connectedPort == null)
//                continue;
//            SimJunction junction = (SimJunction) port2Junction.get(inputPort.hashCode() ^ connectedPort.hashCode());
//            
//            //ports that are connected inside have junctions, we only update those ports
//            if(junction !=  null) {
//                connectedPort =   (IOPort)(junction2OutputPort.get(junction));
//                Actor backwardActor = (Actor) connectedPort.getContainer();
//                
//                Integer numFiring;
//                if( actorsFiringCount.get(backwardActor) != null)
//                    numFiring = (Integer) actorsFiringCount.get(backwardActor);
//                else
//                    numFiring = 0;
//                
//                boolean actorFired = false;
//                while(junction.counter < 0 &&
//                        numFiring <= (Integer)firingVector.get(backwardActor)) {  //upstream actor needs to fire to increase the number of tokens so that it is not negative
//                    _fireActor(backwardActor, port2Junction);   //FIXME: need to know firing which fire function
//                    numFiring++;
//                    actorFired = true;
//                }
//                
//                if(actorFired && !firedActors.contains(backwardActor))
//                    firedActors.add(backwardActor);
////                actorsFiringCount.put(backwardActor, numFiring);
//                
//                if(actorFired)
//                {
//                    actorsFiringCount.put(backwardActor, numFiring);
//                    _fireActorsBackward(backwardActor, firedActors, actorsFiringCount, 
//                        port2Junction, junction2OutputPort, firingVector, inputActors);
//                }
//            }
//        }
//    }
    
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
    
    private void _clusterFirings(Firing currentFiring, Set  clusteredFirings, Set searchedFirings, Set inputFirings,
            List inputActors, List outputActors, Map outputInputDependence) {
        
        searchedFirings.add(currentFiring);
        
        Set currentSearchedFirings = new HashSet();
        Set outputFirings = new HashSet();

        _getDependentForwardFiring(currentFiring, outputFirings, currentSearchedFirings,
                inputActors, outputActors);
        
        boolean validFiring = true;
        for(Object outputFiring: outputFirings) {
            Set inputDependentFirings = (Set)outputInputDependence.get(outputFiring);
            Set tmpInputFiring = new HashSet(inputFirings);
            tmpInputFiring.removeAll(inputDependentFirings);
            
            if(!tmpInputFiring.isEmpty()) { //there are some inputs that the current firing does not depend on
                validFiring = false;
                break;
            } 
        }
       
        if(validFiring) {
            clusteredFirings.add(currentFiring);
            for(Iterator previousFirings = currentFiring.previousActorFirings.iterator(); previousFirings.hasNext();) {
                Firing previousFiring = (Firing) previousFirings.next();
                
                if(!searchedFirings.contains(previousFiring)) {
                    _clusterFirings(previousFiring, clusteredFirings, searchedFirings, inputFirings,
                            inputActors, outputActors, outputInputDependence);
                }
            }
        }
    }
    private void _getDependentBackwardFiring(Firing firing, Set inputFirings, Set searchedFirings,
            List inputFiringFunctions, List outputFiringFunctions) {
        
        if(_getFiringFunction(inputFiringFunctions, firing.actor, firing.firingFunction) != null && 
                !inputFirings.contains(firing)) {
            inputFirings.add(firing);
        }
        
        for(Iterator previousFirings = firing.previousActorFirings.iterator(); previousFirings.hasNext();) {
            Firing previousFiring = (Firing) previousFirings.next();
            if(!searchedFirings.contains(previousFiring)) {
                searchedFirings.add(previousFiring);
                _getDependentBackwardFiring(previousFiring, inputFirings, searchedFirings,
                        inputFiringFunctions, outputFiringFunctions);
            }
            
        }
    }
    
    private void _getDependentForwardFiring(Firing firing, Set outputFirings, Set searchedFirings,
            List inputFiringFunctions, List outputFiringFunctions) {
        
        if(_getFiringFunction(outputFiringFunctions, firing.actor, firing.firingFunction) != null && 
                !outputFirings.contains(firing)) {
            outputFirings.add(firing);
        }
        
        for(Iterator nextFirings = firing.nextActorFirings.iterator(); nextFirings.hasNext();) {
            Firing nextFiring = (Firing) nextFirings.next();
            if(!searchedFirings.contains(nextFiring)) {
                searchedFirings.add(nextFiring);
//                if(!((nextFiring.actor == firing.actor) && (nextFiring.firingFunction == firing.firingFunction)))    //FIXME: do we need this?
                    _getDependentForwardFiring(nextFiring, outputFirings, searchedFirings,
                            inputFiringFunctions, outputFiringFunctions);
            }
            
        }
    }
    
    private SimFiringFunction _getFiringFunction(List<SimFiringFunction> firingFunctionList, Actor actor, int index) {
        for(SimFiringFunction function: firingFunctionList) {
            if(function.actor == actor && function.functionIndex == index) { 
                return function;
            }
        }
        return null;
    }
    
    private Firing _getFiring(Actor actor, int index, int firingFunction, List firingList) {
        Firing ret = null;
        for(Iterator firings = firingList.iterator(); firings.hasNext();) {
            Firing firing = (Firing) firings.next();
            if(firing.actor == actor && firing.firingIndex == index && firing.firingFunction == firingFunction) {
                ret = firing;
                break;
            }
        }
        
        return ret;
    }
    
    private void _printGraph(List firingList) {
        for(Iterator firings = firingList.iterator(); firings.hasNext();) {
            Firing firing = (Firing) firings.next();
            
            System.out.println(firing.actor.getFullName() + ", firing Index: " + firing.firingIndex + " function: " + firing.firingFunction);
            for(Iterator nextFirings = firing.nextActorFirings.iterator(); nextFirings.hasNext();) {
                Firing nextFiring = (Firing) nextFirings.next();
                System.out.println("\t next " + nextFiring.actor.getFullName() + ", firing Index: " + nextFiring.firingIndex + " function: " + nextFiring.firingFunction);
            }
            
            for(Iterator previousFirings = firing.previousActorFirings.iterator(); previousFirings.hasNext();) {
                Firing previousFiring = (Firing) previousFirings.next();
                System.out.println("\t previous " + previousFiring.actor.getFullName() + ", firing Index: " + previousFiring.firingIndex  + " function: " + previousFiring.firingFunction);
            }
        }
        
        System.out.println("digraph G1 {");
        for(Iterator firings = firingList.iterator(); firings.hasNext();) {
            Firing firing = (Firing) firings.next();
            
            for(Iterator nextFirings = firing.nextActorFirings.iterator(); nextFirings.hasNext();) {
                Firing nextFiring = (Firing) nextFirings.next();
                System.out.println("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction 
                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_" + nextFiring.firingFunction + ";");
            }
            
            for(Iterator nextFirings = firing.nextIterationFirings.iterator(); nextFirings.hasNext();) {
                Firing nextFiring = (Firing) nextFirings.next();
                System.out.println("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction 
                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_" + nextFiring.firingFunction + "[style=dotted];");
            }
        }
        System.out.println("}");

    }
    
    private void _computeFiringDepth(Firing firing, Set<Firing> visitedFirings) {
        if(visitedFirings.contains(firing))
            return;
        else {
            visitedFirings.add(firing);
            for(Firing nextFiring:firing.nextActorFirings) {
                if(nextFiring.index <= firing.index) {
                    nextFiring.index = firing.index + 1;
                }
                _computeFiringDepth(nextFiring, visitedFirings);
            }
            
            for(Firing previousFiring: firing.previousActorFirings) {
                if(previousFiring.index >= firing.index) {
                    previousFiring.index = firing.index - 1;
                }
                _computeFiringDepth(previousFiring, visitedFirings);
            }
            
        }
    }
    
    private void _computeClusterDepth(FiringCluster cluster, Map visitedClusters) {
       
        for(FiringCluster nextCluster:cluster.nextClusters) {
            if(nextCluster.index <= cluster.index) {
                nextCluster.index = cluster.index + 1;
            }
            if( visitedClusters.get(cluster.hashCode() ^ nextCluster.hashCode()) == null) {
                Integer value = 1;
                visitedClusters.put(cluster.hashCode() ^ nextCluster.hashCode(), value);
                _computeClusterDepth(nextCluster, visitedClusters);
            }
        }
        
        for(FiringCluster previousCluster: cluster.previousClusters) {
            if(previousCluster.index >= cluster.index) {
                previousCluster.index = cluster.index - 1;
            }
            if( visitedClusters.get(cluster.hashCode() ^ previousCluster.hashCode()) == null) {
                Integer value = 1;
                visitedClusters.put(cluster.hashCode() ^ previousCluster.hashCode(), value);
                _computeClusterDepth(previousCluster, visitedClusters);
            }
        }
            
    }
    
    /**
     * Simulation junction
     */
    static public class SimJunction {
        public SimJunction(IOPort inputPort, IOPort outputPort, int numInitialTokens) {
            _inputPort = inputPort;
            _outputPort = outputPort;
            counter = _numInitialTokens = numInitialTokens;
        }
        
        public int getNumInitialTokens() {
            return _numInitialTokens;
        }
        
        public IOPort getInputPort() {
            return _inputPort;
        }
        
        public IOPort getOutputPort() {
            return _outputPort;
        }
        
        public boolean isInInitalState() {
            return _numInitialTokens == counter;
        }
        
        public void reset() {
            counter = _numInitialTokens;
        }
        
        public int counter;
        private int _numInitialTokens;
        private IOPort _inputPort;
        private IOPort _outputPort;
    }
    /**
     * 
     */
    public static class SimFiringFunction {
        SimFiringFunction(Actor actor, int index ) {
            this.actor = actor;
            functionIndex = index;
        }
        
        public Actor actor;
        public int functionIndex;
    }
    
    /**
     * 
     * @author dai
     *
     */
    public static class Firing {
        public Firing(Actor firingActor, int index,  int function) {
            actor = firingActor;
            firingIndex = index;
            firingFunction = function;
            nextActorFirings = new HashSet();
            previousActorFirings = new HashSet();
            
            previousIterationFirings = new HashSet();
            nextIterationFirings = new HashSet();
            
            this.index = 0;
        }
        
        public Actor actor;
        public int firingIndex;
        public int firingFunction;
        public Set<Firing> nextActorFirings;
        public Set<Firing> previousActorFirings;
        public Set<Firing> previousIterationFirings;
        public Set<Firing> nextIterationFirings;
        public FiringCluster cluster;
        
        public int index;
    }
    
    /**
     * 
     * @author dai
     *
     */
    static public class FiringCluster {
        public FiringCluster() {
            actorFirings = new LinkedList();
            nextClusters = new LinkedList();
            previousClusters = new LinkedList();
            
            nextIterationClusters = new LinkedList();
            previousIterationClusters = new LinkedList();
            
            inputPorts =  new HashSet();
            outputPorts = new HashSet();
            
            this.index = 0;
        }
        public List<Firing> actorFirings;
        
        public List<FiringCluster> nextClusters;
        public List<FiringCluster> previousClusters;
        
        public List<FiringCluster> nextIterationClusters;
        public List<FiringCluster> previousIterationClusters;
        
        public Set<IOPort> inputPorts;
        public Set<IOPort> outputPorts;
        
        public int index;
    }
    
    
    /** Comparator used to sort the actors. */
    private class FiringComparator implements Comparator<Firing> {
        public int compare(Firing f1, Firing f2) {
            return (f2.index - f1.index);
        }
    }
    
    /** Comparator used to sort the actors. */
    private class ClusterComparator implements Comparator<FiringCluster> {
        /** Compare the depths of two actors.
         *  NOTE: This method assumes and does not check that the
         *  depth cache is up to date and contains both specified actors.
         */
        public int compare(FiringCluster c1, FiringCluster c2) {
            return c1.index - c2.index;
        }
    }
}
