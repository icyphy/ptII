/* Class for modular code generators.

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
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.cg.lib.ModularCompiledSDFTypedCompositeActor;
import ptolemy.cg.lib.Profile;
import ptolemy.cg.lib.Profile.FiringFunction;
import ptolemy.cg.lib.Profile.FiringFunctionPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.domains.sdf.lib.SampleDelay;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringBufferExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////GenericCodeGenerator

/**
 * Class for modular code generator.
 *
 * @author Dai Bui, Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 10.0
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create the profile for the model (at this level).
     *
     * @exception IllegalActionException
     *                when the profile can't be generated.
     */
    public void createProfile() throws IllegalActionException {
        String modelName = CodeGeneratorAdapter.generateName(_model);
        String profileClassName = modelName + "_profile";

        StringBuffer profileCode = new StringBuffer();

        profileCode.append("import java.util.List;" + _eol);
        profileCode.append("import java.util.LinkedList;" + _eol);
        profileCode.append("import ptolemy.cg.lib.Profile;" + _eol);
        profileCode.append("import ptolemy.kernel.util.IllegalActionException;"
                + _eol);

        profileCode.append(_eol + "public class " + profileClassName
                + " extends Profile {" + _eol);
        profileCode.append(INDENT1 + "public " + profileClassName + "() { }"
                + _eol);

        profileCode.append(createGraph());

        profileCode.append(INDENT1 + "public List<Profile.Port> ports() {"
                + _eol);
        profileCode.append(INDENT2
                + "List<Profile.Port> ports = new LinkedList<Profile.Port>();"
                + _eol);
        ModularCompiledSDFTypedCompositeActor model = (ModularCompiledSDFTypedCompositeActor) _model;
        for (Object object : model.portList()) {

            if (object instanceof TypedIOPort) {
                TypedIOPort port = (TypedIOPort) object;
                Profile.Port profilePort = model.convertProfilePort(port);

                profileCode.append(INDENT2
                        + "ports.add(new Profile.Port(\""
                        + profilePort.name()
                        + "\", "
                        + profilePort.publisher()
                        + ", "
                        + profilePort.subscriber()
                        + ", "
                        + profilePort.width()
                        + ", "
                        + (port.isInput() ? DFUtilities
                                .getTokenConsumptionRate(port) : DFUtilities
                                .getTokenProductionRate(port)) + ", "
                                + ptTypeToCodegenType(port.getType()) + ", "
                        + port.isInput() + ", " + port.isOutput() + ", \""
                                + profilePort.getPubSubChannelName() + "\"));" + _eol);
            } else {
                throw new InternalErrorException(_model, null, "Port " + object
                        + " is not a TypedIOPort?");
            }
        }

        profileCode.append(INDENT2 + "return ports;" + _eol);
        profileCode.append(INDENT1 + "}" + _eol);

        profileCode.append("}" + _eol);

        _writeCodeFileName(profileCode, profileClassName + ".java", true, true);

        List<String> commands = new LinkedList<String>();
        String topDirectory = ".";
        if (((BooleanToken) generateInSubdirectory.getToken()).booleanValue()) {
            topDirectory = "..";
        }
        commands.add("javac -classpath \"" + topDirectory
                + StringUtilities.getProperty("path.separator")
                + StringUtilities.getProperty("ptolemy.ptII.dir") + "\""
                + profileClassName + ".java");

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

    /** Create the deterministic SDF with sharing buffers graph for a composite actor.
     * @return The generated profile of the composite actor.
     * @exception IllegalActionException If there are problems performing the analysis.
     */
    public StringBuffer createGraph() throws IllegalActionException {

        CompositeActor container = (CompositeActor) getContainer();
        //solve the balance equation
        SDFModularScheduler scheduler = new SDFModularScheduler();
        Map firingVector = scheduler.getFiringVector(container, 1);

        Map port2Junction = new HashMap();
        Map<SimulationJunction, IOPort> junction2InputPort = new HashMap<SimulationJunction, IOPort>();
        Map<SimulationJunction, IOPort> junction2OutputPort = new HashMap<SimulationJunction, IOPort>();

        _createExpandedGraph(container, port2Junction, junction2InputPort,
                junction2OutputPort);

        if (firingVector.containsKey(container)) {
            firingVector.remove(container);
        }

        if (!_deadlockAnalysis(firingVector, port2Junction)) {
            return null;
        }

        //unfolding the graph
        Set actorFirings = new HashSet();

        _createDependencyGraph(container, firingVector, port2Junction,
                actorFirings);

        _deriveFiringFunctionDependency(container, firingVector, actorFirings);

        _printGraph(actorFirings);

        List<SimulationFiringFunction> outputFiringFunctions = new LinkedList();
        List<SimulationFiringFunction> inputFiringFunctions = new LinkedList();

        _findInputOutputFirings(container, outputFiringFunctions,
                inputFiringFunctions);

        //clustering outputs
        Map clusteredOutputs = new HashMap();
        Map outputInputDependence = new HashMap();

        _clusteringOutputFirings(actorFirings, outputFiringFunctions,
                inputFiringFunctions, clusteredOutputs, outputInputDependence);

        Set<IOPort> inInputConnectedPorts = new HashSet();
        Set<IOPort> inOutputConnectedPorts = new HashSet();

        for (Object inputPort : container.inputPortList()) {
            inInputConnectedPorts.addAll(((IOPort) inputPort)
                    .deepInsidePortList());
        }

        for (Object outputPort : container.outputPortList()) {
            inOutputConnectedPorts.addAll(((IOPort) outputPort)
                    .deepInsidePortList());
        }

        List<FiringCluster> firingClusters = new LinkedList();

        Set clusters = new HashSet();

        _clusterActorFirings(outputFiringFunctions, inputFiringFunctions,
                clusteredOutputs, outputInputDependence, inInputConnectedPorts,
                inOutputConnectedPorts, firingClusters, clusters, actorFirings);

        _deriveClusterDependency(firingClusters);

        //sort clusters
        if (firingClusters.size() > 0) {
            //            Map visitedCluster = new HashMap();
            _computeClusterDepth(firingClusters.get(0), firingClusters);

            ClusterComparator clusterComparator = new ClusterComparator();
            Collections.sort(firingClusters, clusterComparator);
            //            for (Object cluster:firingClusters) {
            //                System.out.print(" " + ((FiringCluster)cluster).index);
            //            }
        }

        //generate profile

        StringBuffer esdf = new StringBuffer();

        _generateProfile(container, actorFirings, firingClusters, esdf);

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
    @Override
    public int generateCode(StringBuffer code) throws KernelException {

        int returnValue = -1;

        // If the container is in the top level, we are generating code
        // for the whole model. We have to make sure there is a manager,
        // and then preinitialize and resolve types.
        if (_isTopLevel()) {

            // If necessary, create a manager.
            Actor container = (Actor) getContainer();
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
                    long startTime = new Date().getTime();
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
    @Override
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
                if (!type.equals("Token")
                        && !isPrimitive(codeGenType(inputPort.getType()))) {
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
    @Override
    public String generateMainExitCode() throws IllegalActionException {

        if (_isTopLevel()) {
            return INDENT1 + "System.exit(0);" + _eol + "}" + _eol + "}" + _eol;
        } else {
            if (_model instanceof CompositeActor
                    && ((CompositeActor) _model).outputPortList().isEmpty()) {
                return INDENT1 + "return null;" + _eol + "}" + _eol + "}"
                        + _eol;
            } else {
                return INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                        + _eol + "}" + _eol;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ///                             private methods                 /////

    /** Create clusters of actor firing based on the dependencies on input firings.
     *
     * @param outputFiringFunctions The list of firings that produces tokens to external actors.
     * @param inputFiringFunctions The list of firings that consumes external tokens.
     * @param clusteredOutputs The map from groups of input firings to the group of output firings
     * that depend on the input firings.
     * @param outputInputDependence The map from each each output firing to the set of input firings
     * that it depends on.
     * @param inInputConnectedPorts The input connected ports
     * @param inOutputConnectedPorts The output connected ports
     * @param firingClusters The cluster of firings.
     * @param clusters The clusters
     * @param actorFirings The actor firings.
     * @exception IllegalActionException If thrown while getting the
     * firings from the profile.
     */
    private void _clusterActorFirings(
            List<SimulationFiringFunction> outputFiringFunctions,
            List<SimulationFiringFunction> inputFiringFunctions,
            Map clusteredOutputs, Map outputInputDependence,
            Set<IOPort> inInputConnectedPorts,
            Set<IOPort> inOutputConnectedPorts,
            List<FiringCluster> firingClusters, Set clusters, Set actorFirings)
                    throws IllegalActionException {

        Set<Firing> nonClustered = new HashSet(actorFirings);

        //create clusters of firings of actors
        for (Iterator firings = clusteredOutputs.keySet().iterator(); firings
                .hasNext();) {
            Set inputFirings = (Set) firings.next();
            Set clusteredFirings = new HashSet();
            FiringCluster firingCluster = new FiringCluster();

            Set searchedFirings = new HashSet();

            Set outputFirings = (Set) clusteredOutputs.get(inputFirings);

            //            _printGraph(inputFirings);
            for (Object outputFiring : outputFirings) {
                _clusterFirings((Firing) outputFiring, clusteredFirings,
                        searchedFirings, inputFirings, inputFiringFunctions,
                        outputFiringFunctions, outputInputDependence);
            }

            //sort actors in a firing cluster
            List sortedFirings = new LinkedList(clusteredFirings);
            //            if (sortedFirings.size() > 0) {
            //                Set visitedFirings = new HashSet();
            //                _computeFiringDepth((Firing) sortedFirings.get(0), visitedFirings);
            //                FiringComparator comparator = new FiringComparator();
            //                Collections.sort(sortedFirings, comparator);
            //            }

            //            System.out.println("New cluster");

            for (Object f : sortedFirings) {
                firingCluster.actorFirings.add((Firing) f);
                ((Firing) f).cluster = firingCluster;
                Actor actor = ((Firing) f).actor;

                if (actor instanceof ModularCompiledSDFTypedCompositeActor) {
                    if (_getFiringFunction(inputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set<IOPort> inputPorts = new HashSet(
                                actor.inputPortList());

                        inputPorts.retainAll(inInputConnectedPorts);

                        List<FiringFunctionPort> inputFiringPorts = ((ModularCompiledSDFTypedCompositeActor) actor)
                                .getProfile().firings()
                                .get(((Firing) f).firingFunction).ports;
                        for (IOPort inputPort : inputPorts) {
                            for (FiringFunctionPort firingPort : inputFiringPorts) {
                                if (firingPort.isInput
                                        && firingPort.externalPortName
                                        .equals(inputPort.getName())) {
                                    firingCluster.inputPorts.add(inputPort);
                                    break;
                                }
                            }
                        }
                    }

                    if (_getFiringFunction(outputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set<IOPort> outputPorts = new HashSet(
                                actor.outputPortList());
                        outputPorts.retainAll(inOutputConnectedPorts);

                        List<FiringFunctionPort> outputFiringPorts = ((ModularCompiledSDFTypedCompositeActor) actor)
                                .getProfile().firings()
                                .get(((Firing) f).firingFunction).ports;
                        for (IOPort outputPort : outputPorts) {
                            for (FiringFunctionPort firingPort : outputFiringPorts) {
                                if (!firingPort.isInput
                                        && firingPort.externalPortName
                                        .equals(outputPort.getName())) {
                                    firingCluster.outputPorts.add(outputPort);
                                    break;
                                }
                            }
                        }
                        firingCluster.outputPorts.addAll(outputPorts);
                    }
                } else {
                    if (_getFiringFunction(inputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set inputPorts = new HashSet(actor.inputPortList());

                        inputPorts.retainAll(inInputConnectedPorts);
                        firingCluster.inputPorts.addAll(inputPorts);
                    }

                    if (_getFiringFunction(outputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set outputPorts = new HashSet(actor.outputPortList());
                        outputPorts.retainAll(inOutputConnectedPorts);
                        firingCluster.outputPorts.addAll(outputPorts);
                    }
                }

                //System.out.println("Clustered firing: " + actor.getFullName() + ", firing Index: " + ((Firing)f).firingIndex + " function: " + ((Firing)f).firingFunction);
            }

            firingClusters.add(firingCluster);

            clusters.add(sortedFirings);
            nonClustered.removeAll(sortedFirings);
        }

        //firings that do not belong to any cluster
        if (nonClustered.size() > 0) {
            FiringCluster firingCluster = new FiringCluster();
            firingClusters.add(firingCluster);

            clusters.add(nonClustered);

            for (Object f : nonClustered) {
                firingCluster.actorFirings.add((Firing) f);
                ((Firing) f).cluster = firingCluster;
                Actor actor = ((Firing) f).actor;

                if (actor instanceof ModularCompiledSDFTypedCompositeActor) {
                    if (_getFiringFunction(inputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set<IOPort> inputPorts = new HashSet(
                                actor.inputPortList());

                        inputPorts.retainAll(inInputConnectedPorts);

                        List<FiringFunctionPort> inputFiringPorts = ((ModularCompiledSDFTypedCompositeActor) actor)
                                .getProfile().firings()
                                .get(((Firing) f).firingFunction).ports;
                        for (IOPort inputPort : inputPorts) {
                            for (FiringFunctionPort firingPort : inputFiringPorts) {
                                if (firingPort.isInput
                                        && firingPort.externalPortName
                                        .equals(inputPort.getName())) {
                                    firingCluster.inputPorts.add(inputPort);
                                    break;
                                }
                            }
                        }
                    }

                    if (_getFiringFunction(outputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set<IOPort> outputPorts = new HashSet(
                                actor.outputPortList());
                        outputPorts.retainAll(inOutputConnectedPorts);

                        List<FiringFunctionPort> outputFiringPorts = ((ModularCompiledSDFTypedCompositeActor) actor)
                                .getProfile().firings()
                                .get(((Firing) f).firingFunction).ports;
                        for (IOPort outputPort : outputPorts) {
                            for (FiringFunctionPort firingPort : outputFiringPorts) {
                                if (!firingPort.isInput
                                        && firingPort.externalPortName
                                        .equals(outputPort.getName())) {
                                    firingCluster.outputPorts.add(outputPort);
                                    break;
                                }
                            }
                        }
                        firingCluster.outputPorts.addAll(outputPorts);
                    }
                } else {
                    if (_getFiringFunction(inputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set inputPorts = new HashSet(actor.inputPortList());

                        inputPorts.retainAll(inInputConnectedPorts);
                        firingCluster.inputPorts.addAll(inputPorts);
                    }

                    if (_getFiringFunction(outputFiringFunctions, actor,
                            ((Firing) f).firingFunction) != null) {

                        Set outputPorts = new HashSet(actor.outputPortList());
                        outputPorts.retainAll(inOutputConnectedPorts);
                        firingCluster.outputPorts.addAll(outputPorts);
                    }
                }

            }
        }
    }

    /** Cluster firings together so that there is no false dependency.
     * @param currentFiring Pointer to current node in the firing graph.
     * @param clusteredFirings The output set of clustered firings.
     * @param searchedFirings The set of searched firing used for searching.
     * @param inputFirings The set of output firings (produce external tokens) of the cluster.
     * @param inputActors The set of actors that consume external tokens.
     * @param outputActors The set of actors that produce external tokens.
     * @param outputInputDependence The map form each output firing to a set of input firings
     * that its depend on.
     */
    private void _clusterFirings(Firing currentFiring, Set clusteredFirings,
            Set searchedFirings, Set inputFirings, List inputActors,
            List outputActors, Map outputInputDependence) {

        searchedFirings.add(currentFiring);

        Set currentlySearchedFirings = new HashSet();
        Set outputFirings = new HashSet();

        _getDependentForwardFiring(currentFiring, outputFirings,
                currentlySearchedFirings, inputActors, outputActors);

        boolean validFiring = true;
        for (Object outputFiring : outputFirings) {
            Set inputDependentFirings = (Set) outputInputDependence
                    .get(outputFiring);
            Set tmpInputFiring = new HashSet(inputFirings);
            tmpInputFiring.removeAll(inputDependentFirings);

            if (!tmpInputFiring.isEmpty()) { //there are some inputs of the cluster that the current firing does not depend on
                validFiring = false;
                break;
            }
        }

        if (validFiring) {
            clusteredFirings.add(currentFiring);
            for (Object element : currentFiring.previousActorFirings) {
                Firing previousFiring = (Firing) element;

                if (!searchedFirings.contains(previousFiring)) {
                    _clusterFirings(previousFiring, clusteredFirings,
                            searchedFirings, inputFirings, inputActors,
                            outputActors, outputInputDependence);
                }
            }
        }
    }

    /** Create dependency graph between firings of internal actors inside a composite actor
     * @param container The composite actor.
     * @param firingVector The firing vector of the actors inside the composite actor obtained
     * by solving the balance equation.
     * @param port2Junction The map from a pair of connected ports to the junction between them.
     * @param actorFirings The list of firings of the internal actors inside the composite actor.
     * @exception IllegalActionException If thrown while getting the firings froim the profile,
     * the token production rate or the token consumption rate.
     */
    private void _createDependencyGraph(CompositeActor container,
            Map firingVector, Map port2Junction, Set actorFirings)
                    throws IllegalActionException {
        //create the dependency graph
        for (Iterator actors = container.deepEntityList().iterator(); actors
                .hasNext();) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof SampleDelay) {
                continue;
            }

            Integer numFirings = (Integer) firingVector.get(actor);
            for (Iterator ports = actor.outputPortList().iterator(); ports
                    .hasNext();) {
                IOPort outputPort = (IOPort) ports.next();

                for (Iterator inputPorts = _getConnectedInputPorts(outputPort)
                        .iterator(); inputPorts.hasNext();) {
                    IOPort connectedPort = (IOPort) inputPorts.next();

                    SimulationJunction junction = (SimulationJunction) port2Junction
                            .get(outputPort.hashCode()
                                    ^ connectedPort.hashCode());

                    if (junction != null) {
                        junction.reset();
                        Actor nextActor = (Actor) connectedPort.getContainer();
                        int nextActorNumFirings = (Integer) firingVector
                                .get(nextActor);

                        List<FiringFunction> currentActorFirings = null;
                        List<FiringFunction> nextActorFirings = null;

                        int numFireFunctions;
                        int nextActorFireFunctions;

                        if (actor instanceof ModularCompiledSDFTypedCompositeActor) {
                            currentActorFirings = ((ModularCompiledSDFTypedCompositeActor) actor)
                                    .getProfile().firings();
                            numFireFunctions = currentActorFirings.size();
                        } else {
                            numFireFunctions = 1;
                        }

                        if (nextActor instanceof ModularCompiledSDFTypedCompositeActor) {
                            nextActorFirings = ((ModularCompiledSDFTypedCompositeActor) nextActor)
                                    .getProfile().firings();
                            nextActorFireFunctions = nextActorFirings.size();
                        } else {
                            nextActorFireFunctions = 1;
                        }

                        int iterationCount = 1;
                        int firingFunctionCount = 0;
                        boolean firstConsumption = true;

                        for (int i = 0; i <= numFirings; i++) {
                            nextFiring: for (int j = 0; j < numFireFunctions; j++) {
                                boolean fired = false;
                                if (i > 0) {
                                    if (actor instanceof ModularCompiledSDFTypedCompositeActor) {
                                        for (FiringFunctionPort port : currentActorFirings
                                                .get(j).ports) {
                                            if (port.externalPortName
                                                    .equals(outputPort
                                                            .getName())) {
                                                junction.counter += port.rate;
                                                fired = true;
                                                break;
                                            }
                                        }

                                    } else {
                                        //normal actor
                                        junction.counter += DFUtilities
                                                .getTokenProductionRate(outputPort);
                                        fired = true;
                                    }
                                } else {

                                    //check the backward dependency
                                    if (junction.counter > 0
                                            && firstConsumption) {
                                        //get the last fire function that put tokens into the junction
                                        int lastFireFunction = numFireFunctions - 1;
                                        if (actor instanceof ModularCompiledSDFTypedCompositeActor) {
                                            FOUND_FUNCTION: for (int k = numFireFunctions - 1; k >= 0; k--) {
                                                for (FiringFunctionPort port : currentActorFirings
                                                        .get(k).ports) {
                                                    if (port.externalPortName
                                                            .equals(outputPort
                                                                    .getName())) {
                                                        lastFireFunction = k;
                                                        break FOUND_FUNCTION;
                                                    }
                                                }
                                            }
                                        }

                                        int firstFireFunction = 0;

                                        if (nextActor instanceof ModularCompiledSDFTypedCompositeActor) {
                                            FOUND_NEXT_ACTOR_FUNCTION: for (int n = 0; n < numFireFunctions; n++) {
                                                for (FiringFunctionPort port : nextActorFirings
                                                        .get(n).ports) {
                                                    if (port.externalPortName
                                                            .equals(outputPort
                                                                    .getName())) {
                                                        firstFireFunction = n;
                                                        break FOUND_NEXT_ACTOR_FUNCTION;
                                                    }
                                                }
                                            }
                                        }

                                        Firing previousIterationFiring = _getFiring(
                                                actor, numFirings,
                                                lastFireFunction, actorFirings);
                                        if (previousIterationFiring == null) {
                                            previousIterationFiring = new Firing(
                                                    actor, numFirings,
                                                    lastFireFunction);
                                            actorFirings
                                            .add(previousIterationFiring);
                                        }

                                        Firing nextIterationFiring = _getFiring(
                                                nextActor, 1,
                                                firstFireFunction, actorFirings);
                                        if (nextIterationFiring == null) {
                                            nextIterationFiring = new Firing(
                                                    nextActor, 1,
                                                    firstFireFunction);
                                            actorFirings
                                            .add(nextIterationFiring);
                                        }

                                        previousIterationFiring.nextIterationFirings
                                        .add(nextIterationFiring);
                                        nextIterationFiring.previousIterationFirings
                                        .add(previousIterationFiring);

                                        firstConsumption = false;
                                    }

                                    //backward dependency to the firing of the previous iteration
                                    Firing previousIterationFiring = _getFiring(
                                            actor, numFirings, j, actorFirings);
                                    if (previousIterationFiring == null) {
                                        previousIterationFiring = new Firing(
                                                actor, numFirings, j);
                                        actorFirings
                                        .add(previousIterationFiring);
                                    }

                                    Firing nextIterationFiring = _getFiring(
                                            actor, 1, j, actorFirings);
                                    if (nextIterationFiring == null) {
                                        nextIterationFiring = new Firing(actor,
                                                1, j);
                                        actorFirings.add(nextIterationFiring);
                                    }

                                    previousIterationFiring.nextIterationFirings
                                    .add(nextIterationFiring);
                                    nextIterationFiring.previousIterationFirings
                                    .add(previousIterationFiring);

                                    fired = true;
                                }

                                if (fired) {

                                    Firing currentFiring = null;

                                    if (i > 0) {
                                        currentFiring = _getFiring(actor, i, j,
                                                actorFirings);
                                        if (currentFiring == null) {
                                            currentFiring = new Firing(actor,
                                                    i, j);
                                            actorFirings.add(currentFiring);
                                        }
                                    }

                                    //next actor consumes tokens
                                    while (iterationCount <= nextActorNumFirings) {
                                        while (firingFunctionCount < nextActorFireFunctions) {
                                            boolean nextActorFired = false;
                                            //try to consume some tokens
                                            if (nextActor instanceof ModularCompiledSDFTypedCompositeActor) {
                                                for (FiringFunctionPort port : nextActorFirings
                                                        .get(firingFunctionCount).ports) {
                                                    if (port.externalPortName
                                                            .equals(connectedPort
                                                                    .getName())) {

                                                        if (junction.counter >= port.rate) {
                                                            junction.counter -= port.rate;
                                                            nextActorFired = true;
                                                        } else {
                                                            continue nextFiring;
                                                        }
                                                        break;
                                                    }
                                                }
                                            } else {
                                                if (junction.counter >= DFUtilities
                                                        .getTokenConsumptionRate(connectedPort)) {
                                                    junction.counter -= DFUtilities
                                                            .getTokenConsumptionRate(connectedPort);
                                                    nextActorFired = true;
                                                } else {
                                                    continue nextFiring;
                                                }
                                            }

                                            if (nextActorFired
                                                    && currentFiring != null) {
                                                Firing nextFiring = _getFiring(
                                                        nextActor,
                                                        iterationCount,
                                                        firingFunctionCount,
                                                        actorFirings);
                                                if (nextFiring == null) {
                                                    nextFiring = new Firing(
                                                            nextActor,
                                                            iterationCount,
                                                            firingFunctionCount);
                                                    actorFirings
                                                    .add(nextFiring);
                                                }

                                                currentFiring.nextActorFirings
                                                .add(nextFiring);
                                                nextFiring.previousActorFirings
                                                .add(currentFiring);
                                            }

                                            firingFunctionCount++;
                                        }

                                        if (firingFunctionCount >= nextActorFireFunctions) {
                                            firingFunctionCount = 0;
                                        }

                                        iterationCount++;
                                    }

                                    if (iterationCount > nextActorNumFirings) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Create "expanded" graph for a composite actors. Each firing of
     * an internal actors will be a node in the the expanded graph. Each relation between
     * a pair of ports will be denoted by a "junction".
     * @param container The composite actor
     * @param port2Junction The map from pairs for ports to their connected junction.
     * A junction is a counter denoting the number of tokens in a relation between two ports.
     * @param junction2InputPort The map from junctions to respective input ports
     * @param junction2OutputPort The map from junctions to respective output port
     * @exception IllegalActionException If thrown while getting the token.
     */
    private void _createExpandedGraph(CompositeActor container,
            Map port2Junction, Map junction2InputPort, Map junction2OutputPort)
                    throws IllegalActionException {
        //construct a extended graph for deadlock analysis
        for (Iterator actors = container.deepEntityList().iterator(); actors
                .hasNext();) {
            Actor actor = (Actor) actors.next();
            if (!(actor instanceof SampleDelay)) {
                for (Iterator ports = actor.outputPortList().iterator(); ports
                        .hasNext();) {
                    IOPort outputPort = (IOPort) ports.next();

                    if (outputPort instanceof ParameterPort) {
                        continue; //FIXME
                    }

                    for (Iterator connectedPorts = outputPort
                            .connectedPortList().iterator(); connectedPorts
                            .hasNext();) {
                        IOPort connectedPort = (IOPort) connectedPorts.next();

                        if (!connectedPort.isOutput()
                                && !(connectedPort.getContainer() instanceof SampleDelay)) { //only input ports, this exclude the output ports of the container
                            //each connection has one junction
                            SimulationJunction junction = new SimulationJunction(
                                    connectedPort, outputPort, 0); //FIXME what is the number of initial tokens

                            port2Junction.put(outputPort.hashCode()
                                    ^ connectedPort.hashCode(), junction);
                            //                            port2Junction.put(connectedPort, junction);

                            junction2InputPort.put(junction, connectedPort);
                            junction2OutputPort.put(junction, outputPort);
                        }
                    }
                }
            } else {
                IOPort sampleDelayOutputPort = ((SampleDelay) actor).output;
                IOPort sampleDelayInputPort = ((SampleDelay) actor).input;
                for (IOPort outputPort : _getConnectedOutputPorts(sampleDelayInputPort)) {
                    //                    if (outputPort == null)
                    //                        continue;

                    ArrayToken tokens = (ArrayToken) ((SampleDelay) actor).initialOutputs
                            .getToken();

                    for (Iterator connectedPorts = sampleDelayOutputPort
                            .connectedPortList().iterator(); connectedPorts
                            .hasNext();) {
                        IOPort connectedPort = (IOPort) connectedPorts.next();

                        if (!connectedPort.isOutput()) { //only input ports, this exclude the output ports of the container

                            //each connection has one junction
                            SimulationJunction junction = new SimulationJunction(
                                    connectedPort, outputPort, tokens.length()); //FIXME what is the number of initial tokens

                            port2Junction.put(outputPort.hashCode()
                                    ^ connectedPort.hashCode(), junction);

                            junction2InputPort.put(junction, connectedPort);
                            junction2OutputPort.put(junction, outputPort);
                        }
                    }
                }
            }
        }
    }

    /** Clustering output firings (firings that produces tokens to output port of a composite
     * actor) according to dependency on the input firings
     *
     * @param actorFirings The list of firings of internal actors inside the composite actor.
     * @param outputFiringFunctions The list of output firings.
     * @param inputFiringFunctions The list of input firings.
     * @param clusteredOutputs
     * @param outputInputDependence
     */

    private void _clusteringOutputFirings(Set actorFirings,
            List<SimulationFiringFunction> outputFiringFunctions,
            List<SimulationFiringFunction> inputFiringFunctions,
            Map clusteredOutputs, Map outputInputDependence) {
        for (Iterator firings = actorFirings.iterator(); firings.hasNext();) {

            Firing firing = (Firing) firings.next();
            if (_getFiringFunction(outputFiringFunctions, firing.actor,
                    firing.firingFunction) != null) {
                Set inputFirings = new HashSet();
                Set searchedFirings = new HashSet();

                _getDependentBackwardFiring(firing, inputFirings,
                        searchedFirings, inputFiringFunctions,
                        outputFiringFunctions);
                outputInputDependence.put(firing, inputFirings);

                System.out.println("Out put firing: "
                        + firing.actor.getFullName() + ", firing Index: "
                        + firing.firingIndex + " function: "
                        + firing.firingFunction);
                _printGraph(inputFirings);
            }
        }

        //cluster output firings
        for (Iterator outputFirings = outputInputDependence.keySet().iterator(); outputFirings
                .hasNext();) {
            Firing outputFiring = (Firing) outputFirings.next();
            Set inputFirings = (Set) outputInputDependence.get(outputFiring);

            boolean existed = false;
            for (Iterator inputFiringsIter = clusteredOutputs.keySet()
                    .iterator(); inputFiringsIter.hasNext();) {
                Set firings = (Set) inputFiringsIter.next();
                if (inputFirings.equals(firings)) {
                    Set clusteredOutputFirings = (Set) clusteredOutputs
                            .get(firings);
                    clusteredOutputFirings.add(outputFiring);
                    existed = true;
                    break;
                }
            }

            if (!existed) {
                Set clusteredOutputFirings = new HashSet();
                clusteredOutputFirings.add(outputFiring);
                Set firings = new HashSet(inputFirings);
                clusteredOutputs.put(firings, clusteredOutputFirings);
            }
        }
    }

    /** Do simulation based on firing vector to determine if deadlock happens.
     * @param firingVector
     * @param port2Junction The map from pairs of ports to their respective junction.
     * @return If deadlock happens, return true, otherwise, return false.
     * @exception IllegalActionException
     */
    private boolean _deadlockAnalysis(Map firingVector, Map port2Junction)
            throws IllegalActionException {

        //deadlock analysis
        Map simFiringVector = new HashMap(firingVector);
        boolean deadlocked = true;

        while (simFiringVector.size() > 0) {

            boolean fireLastIteration = false;

            LinkedList actorSet = new LinkedList(simFiringVector.keySet());
            //pick an actor to fire
            for (Iterator actors = actorSet.iterator(); actors.hasNext();) {
                Actor actor = (Actor) actors.next();

                if (actor instanceof SampleDelay) {
                    simFiringVector.remove(actor);
                    continue;
                }

                if (!(actor instanceof ModularCompiledSDFTypedCompositeActor)) {
                    boolean canFire = true;

                    //check if we could fire this actor
                    for (Iterator ports = actor.inputPortList().iterator(); ports
                            .hasNext();) {
                        IOPort inputPort = (IOPort) ports.next();

                        for (IOPort connectedPort : _getConnectedOutputPorts(inputPort)) {

                            SimulationJunction junction = (SimulationJunction) port2Junction
                                    .get(inputPort.hashCode()
                                            ^ connectedPort.hashCode());

                            //ports that are connected inside have junctions, we only check those ports
                            if (junction != null) {
                                if (junction.counter < DFUtilities
                                        .getTokenConsumptionRate(inputPort)) {
                                    canFire = false; //do not have enough tokens at some port to fire
                                    break;
                                }
                            }
                        }
                    }

                    if (canFire) {

                        //FIXME more complicated for other actors with different firing functions
                        _fireActor(actor, port2Junction);

                        Integer numFirings = ((Integer) simFiringVector
                                .get(actor)).intValue() - 1; //decrease number of firings

                        if (numFirings <= 0) {
                            simFiringVector.remove(actor);
                        } else {
                            simFiringVector.put(actor, numFirings);
                        }

                        fireLastIteration = true; //mark that there is some actor that can fire, not deadlocked yet
                    }
                } else {

                    //FIXME
                    boolean canFire = true;

                    //check if we could fire this actor
                    for (Iterator ports = actor.inputPortList().iterator(); ports
                            .hasNext();) {
                        IOPort inputPort = (IOPort) ports.next();

                        for (IOPort connectedPort : _getConnectedOutputPorts(inputPort)) {
                            if (connectedPort == null) {
                                continue;
                            }

                            SimulationJunction junction = (SimulationJunction) port2Junction
                                    .get(inputPort.hashCode()
                                            ^ connectedPort.hashCode());

                            //ports that are connected inside have junctions, we only check those ports
                            if (junction != null) {
                                if (junction.counter < DFUtilities
                                        .getTokenConsumptionRate(inputPort)) {
                                    canFire = false; //do not have enough tokens at some port to fire
                                    break;
                                }
                            }
                        }
                    }

                    if (canFire) {

                        //FIXME more complicated for other actors with different firing functions
                        _fireActor(actor, port2Junction);

                        Integer numFirings = ((Integer) simFiringVector
                                .get(actor)).intValue() - 1; //decrease number of firings

                        if (numFirings <= 0) {
                            simFiringVector.remove(actor);
                        } else {
                            simFiringVector.put(actor, numFirings);
                        }

                        fireLastIteration = true; //mark that there is some actor that can fire, not deadlocked yet
                    }
                }
            }

            //check if deadlock happens
            if (simFiringVector.size() == 0) {
                deadlocked = false; //we can simulate
                break;
            } else if (!fireLastIteration) { //there are some actors left but none can fire -> deadlocked
                deadlocked = true;
                break;
            }

        }

        if (deadlocked) {
            return false;
        }

        //check if all junctions are in initial states after one firing iteration
        for (Iterator junctions = port2Junction.values().iterator(); junctions
                .hasNext();) {
            SimulationJunction junction = (SimulationJunction) junctions.next();
            if (!junction.isInInitialState()) {
                throw new IllegalActionException(
                        "Some junction is not in its initial state after one firing iteration");
            }
        }
        return true;
    }

    /** Derive the dependencies between firing clusters from the dependency between firing in the clusters
     * @param firingClusters The list of firing clusters
     */
    private void _deriveClusterDependency(List<FiringCluster> firingClusters) {
        //create dependencies between clusters
        for (FiringCluster cluster : firingClusters) {
            for (Firing f : cluster.actorFirings) {
                for (Firing nextFiring : f.nextActorFirings) {
                    if (!cluster.nextClusters.contains(nextFiring.cluster)) {
                        cluster.nextClusters.add(nextFiring.cluster);
                    }
                }

                cluster.nextClusters.remove(cluster);

                for (Firing previousFiring : f.previousActorFirings) {
                    if (!cluster.previousClusters
                            .contains(previousFiring.cluster)) {
                        cluster.previousClusters.add(previousFiring.cluster);
                    }

                }

                cluster.previousClusters.remove(cluster);

                for (Firing nextIterationFiring : f.nextIterationFirings) {
                    if (!cluster.nextIterationClusters
                            .contains(nextIterationFiring.cluster)) {
                        cluster.nextIterationClusters
                        .add(nextIterationFiring.cluster);
                    }
                }

                cluster.nextIterationClusters.remove(cluster);

                for (Firing previousIterationFiring : f.previousIterationFirings) {
                    if (!cluster.previousIterationClusters
                            .contains(previousIterationFiring.cluster)) {
                        cluster.previousIterationClusters
                        .add(previousIterationFiring.cluster);
                    }
                }

                cluster.previousIterationClusters.remove(cluster);

            }
        }
    }

    /** Derive the dependency between firing functions of internal actors inside a container
     *
     * @param container The container SDF actor
     * @param firingVector The firing vector of actors which contains the number of firing of each actor
     * in one iteration of the external composite actor
     * @param actorFirings The list of firings of actors in the one firing iteration of the composite actor
     * @exception IllegalActionException
     */

    private void _deriveFiringFunctionDependency(CompositeActor container,
            Map firingVector, Set actorFirings) throws IllegalActionException {
        //dependency between firing of each actor
        for (Iterator actors = container.deepEntityList().iterator(); actors
                .hasNext();) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof SampleDelay) {
                continue;
            }

            Integer numFiring = (Integer) firingVector.get(actor);
            for (int i = 1; i <= numFiring; i++) {
                if (actor instanceof ModularCompiledSDFTypedCompositeActor) {
                    List<FiringFunction> actorFiringFunctions = ((ModularCompiledSDFTypedCompositeActor) actor)
                            .getProfile().firings();
                    int numFireFunctions = actorFiringFunctions.size();

                    for (int j = 0; j < numFireFunctions; j++) {
                        Firing firing = _getFiring(actor, i, j, actorFirings);
                        if (firing == null) {
                            firing = new Firing(actor, i, j);
                            actorFirings.add(firing);
                        }

                        for (Integer index : actorFiringFunctions.get(j).previousFiringFunctions) {
                            Firing previousFiring = _getFiring(actor, i, index,
                                    actorFirings);

                            if (previousFiring == null) {
                                previousFiring = new Firing(actor, i, index);
                                actorFirings.add(previousFiring);
                            }
                            firing.previousActorFirings.add(previousFiring);
                            previousFiring.nextActorFirings.add(firing);
                        }

                        for (Integer index : actorFiringFunctions.get(j).nextFiringFunctions) {
                            Firing nextFiring = _getFiring(actor, i, index,
                                    actorFirings);

                            if (nextFiring == null) {
                                nextFiring = new Firing(actor, i, index);
                                actorFirings.add(nextFiring);
                            }

                            firing.nextActorFirings.add(nextFiring);
                            nextFiring.previousActorFirings.add(firing);
                        }

                        if (i > 1) {
                            for (Integer index : actorFiringFunctions
                                    .get(firing.firingFunction).previousIterationFirings) {
                                Firing previousFiring = _getFiring(actor,
                                        i - 1, index, actorFirings);

                                if (previousFiring == null) {
                                    previousFiring = new Firing(actor, i - 1,
                                            index);
                                    actorFirings.add(previousFiring);
                                }

                                firing.previousActorFirings.add(previousFiring);
                                previousFiring.nextActorFirings.add(firing);
                            }
                        }

                        //dependency between firings of one function
                        if (i > 1) {
                            Firing previousFiring = _getFiring(actor, i - 1, j,
                                    actorFirings);
                            if (previousFiring == null) {
                                previousFiring = new Firing(actor, i - 1, j);
                                actorFirings.add(previousFiring);
                            }

                            firing.previousActorFirings.add(previousFiring);
                            previousFiring.nextActorFirings.add(firing);
                        }
                    }
                } else {
                    if (i > 1) {
                        Firing firing = _getFiring(actor, i, 0, actorFirings);

                        if (firing == null) {
                            firing = new Firing(actor, i, 0);
                            actorFirings.add(firing);
                        }

                        Firing previousFiring = _getFiring(actor, i - 1, 0,
                                actorFirings);
                        if (previousFiring == null) {
                            previousFiring = new Firing(actor, i - 1, 0);
                            actorFirings.add(previousFiring);
                        }

                        firing.previousActorFirings.add(previousFiring);
                        previousFiring.nextActorFirings.add(firing);
                    }
                }
            }

        }
    }

    /** Find input/output firings (firings that consume/produce tokens from external actors)
     * @param container The composite actor
     * @param outputFiringFunctions The list of output firings
     * @param inputFiringFunctions The list of input firings
     * @exception IllegalActionException
     */
    private void _findInputOutputFirings(CompositeActor container,
            List<SimulationFiringFunction> outputFiringFunctions,
            List<SimulationFiringFunction> inputFiringFunctions)
                    throws IllegalActionException {
        //find output actors (produce external tokens)

        for (Iterator ports = container.outputPortList().iterator(); ports
                .hasNext();) {
            IOPort outputPort = (IOPort) ports.next();
            for (Iterator connectedPorts = outputPort.deepInsidePortList()
                    .iterator(); connectedPorts.hasNext();) {
                IOPort connectedPort = (IOPort) connectedPorts.next();

                if (connectedPort.isOutput()) {
                    Actor connectedActor = (Actor) connectedPort.getContainer();
                    if (connectedActor instanceof ModularCompiledSDFTypedCompositeActor) {
                        List<FiringFunction> connectedActorFuntions = ((ModularCompiledSDFTypedCompositeActor) connectedActor)
                                .getProfile().firings();
                        int numFunctions = connectedActorFuntions.size();

                        for (int j = 0; j < numFunctions; j++) {
                            for (FiringFunctionPort port : connectedActorFuntions
                                    .get(j).ports) {
                                if (!port.isInput
                                        && port.externalPortName
                                        .equals(connectedPort.getName())) {

                                    SimulationFiringFunction function = _getFiringFunction(
                                            outputFiringFunctions,
                                            connectedActor, j);
                                    if (function == null) {
                                        function = new SimulationFiringFunction(
                                                connectedActor, j);
                                        outputFiringFunctions.add(function);
                                    }
                                }
                            }
                        }
                    } else {
                        SimulationFiringFunction function = _getFiringFunction(
                                outputFiringFunctions, connectedActor, 0);

                        if (function == null) {
                            function = new SimulationFiringFunction(
                                    connectedActor, 0);
                            outputFiringFunctions.add(function);
                        }
                    }
                }
            }
        }

        //find input actors (consume external tokens)

        for (Iterator ports = container.inputPortList().iterator(); ports
                .hasNext();) {
            IOPort inputPort = (IOPort) ports.next();
            for (Iterator connectedPorts = inputPort.deepInsidePortList()
                    .iterator(); connectedPorts.hasNext();) {
                IOPort connectedPort = (IOPort) connectedPorts.next();

                if (connectedPort.isInput()) {
                    Actor connectedActor = (Actor) connectedPort.getContainer();
                    if (connectedActor instanceof ModularCompiledSDFTypedCompositeActor) {
                        List<FiringFunction> connectedActorFuntions = ((ModularCompiledSDFTypedCompositeActor) connectedActor)
                                .getProfile().firings();
                        int numFunctions = connectedActorFuntions.size();
                        for (int j = 0; j < numFunctions; j++) {
                            for (FiringFunctionPort port : connectedActorFuntions
                                    .get(j).ports) {
                                if (port.isInput
                                        && port.externalPortName
                                        .equals(connectedPort.getName())) {
                                    SimulationFiringFunction function = _getFiringFunction(
                                            inputFiringFunctions,
                                            connectedActor, j);
                                    if (function == null) {
                                        function = new SimulationFiringFunction(
                                                connectedActor, j);
                                        inputFiringFunctions.add(function);
                                    }
                                }
                            }
                        }
                    } else {
                        SimulationFiringFunction function = _getFiringFunction(
                                inputFiringFunctions, connectedActor, 0);

                        if (function == null) {
                            function = new SimulationFiringFunction(
                                    connectedActor, 0);
                            inputFiringFunctions.add(function);
                        }
                    }
                }
            }
        }
    }

    /** Simulating firing of an actor
     *
     * @param actor The actor to fire
     * @param port2Junction The map from ports to junction
     * @exception IllegalActionException
     */
    private void _fireActor(Actor actor, Map port2Junction)
            throws IllegalActionException {
        //check if we could fire this actor
        for (Iterator ports = actor.inputPortList().iterator(); ports.hasNext();) {
            IOPort inputPort = (IOPort) ports.next();
            for (IOPort connectedPort : _getConnectedOutputPorts(inputPort)) {
                SimulationJunction junction = (SimulationJunction) port2Junction
                        .get(inputPort.hashCode() ^ connectedPort.hashCode());

                //ports that are connected inside have junctions, we only update those ports
                if (junction != null) {
                    junction.counter -= DFUtilities
                            .getTokenConsumptionRate(inputPort);
                }
            }
        }

        for (Iterator ports = actor.outputPortList().iterator(); ports
                .hasNext();) {
            IOPort outputPort = (IOPort) ports.next();
            for (Iterator inputPorts = _getConnectedInputPorts(outputPort)
                    .iterator(); inputPorts.hasNext();) {
                IOPort connectedPort = (IOPort) inputPorts.next();
                if (connectedPort.isInput()) {
                    SimulationJunction junction = (SimulationJunction) port2Junction
                            .get(outputPort.hashCode()
                                    ^ connectedPort.hashCode());

                    //ports that are connected inside have junctions, we only update those ports
                    if (junction != null) {
                        junction.counter += DFUtilities
                                .getTokenProductionRate(outputPort);
                    }
                }
            }
        }
    }

    /** Writing out the profile for the composite actor and print out the dot file.
     *
     * @param container The composite actor whose profile to be generated.
     * @param actorFirings
     * @param firingClusters
     * @param esdf
     * @exception IllegalActionException
     */
    private void _generateProfile(CompositeActor container, Set actorFirings,
            List<FiringCluster> firingClusters, StringBuffer esdf)
                    throws IllegalActionException {
        esdf.append(INDENT1
                + "public List<FiringFunction> firings() throws IllegalActionException {"
                + _eol);
        esdf.append(INDENT2
                + "List<FiringFunction> firingFunctions = new LinkedList<FiringFunction>();"
                + _eol);
        esdf.append(INDENT2 + "FiringFunction firingFunction;" + _eol + _eol);
        for (FiringCluster cluster : firingClusters) {

            int index = firingClusters.indexOf(cluster);

            esdf.append(INDENT2 + "firingFunction = new FiringFunction("
                    + index + ");" + _eol);

            String externalPortName;

            //add ports name and rate
            for (IOPort inputPort : cluster.inputPorts) {
                externalPortName = "";
                for (Object connectedPort : inputPort.connectedPortList()) {
                    if (container.portList().contains(connectedPort)) {
                        externalPortName = ((IOPort) connectedPort).getName();
                        break;
                    }
                }

                if (!externalPortName.equals("")) {
                    esdf.append(INDENT2
                            + "firingFunction.ports.add(new FiringFunctionPort(\""
                            + inputPort.getName() + "\",\"" + externalPortName
                            + "\","
                            + DFUtilities.getTokenConsumptionRate(inputPort)
                            + "," + inputPort.isInput() + "));" + _eol);
                }
            }

            for (IOPort outputPort : cluster.outputPorts) {

                externalPortName = "";
                for (Object connectedPort : outputPort.connectedPortList()) {
                    if (container.portList().contains(connectedPort)) {
                        externalPortName = ((IOPort) connectedPort).getName();
                        break;
                    }
                }

                if (!externalPortName.equals("")) {
                    esdf.append(INDENT2
                            + "firingFunction.ports.add(new FiringFunctionPort(\""
                            + outputPort.getName() + "\",\"" + externalPortName
                            + "\","
                            + DFUtilities.getTokenProductionRate(outputPort)
                            + "," + outputPort.isInput() + "));" + _eol);
                }
            }

            for (FiringCluster nextCluster : cluster.nextClusters) {
                esdf.append(INDENT2 + "firingFunction.nextFiringFunctions.add("
                        + firingClusters.indexOf(nextCluster) + ");" + _eol);

            }

            for (FiringCluster previousCluster : cluster.previousClusters) {
                esdf.append(INDENT2
                        + "firingFunction.previousFiringFunctions.add("
                        + firingClusters.indexOf(previousCluster) + ");" + _eol);

            }

            for (FiringCluster nextCluster : cluster.nextIterationClusters) {
                esdf.append(INDENT2
                        + "firingFunction.nextIterationFirings.add("
                        + firingClusters.indexOf(nextCluster) + ");" + _eol);

            }

            for (FiringCluster previousCluster : cluster.previousIterationClusters) {
                esdf.append(INDENT2
                        + "firingFunction.previousIterationFirings.add("
                        + firingClusters.indexOf(previousCluster) + ");" + _eol);

            }

            esdf.append(INDENT2 + "firingFunctions.add(firingFunction);" + _eol
                    + _eol);
        }

        //draw cluster graphs
        StringBuffer graph = new StringBuffer();

        System.out.println("Size of the expaned graph " + actorFirings.size());

        graph.append("digraph G {" + _eol + "\tsize=\"8,8\"" + _eol
                + "\tcompound=true;" + _eol);

        for (Iterator firings = actorFirings.iterator(); firings.hasNext();) {
            Firing firing = (Firing) firings.next();

            graph.append("\t" + firing.actor.getName() + "_"
                    + firing.firingIndex + "_" + firing.firingFunction + ";"
                    + _eol);

            for (Object element : firing.nextActorFirings) {
                Firing nextFiring = (Firing) element;
                graph.append("\t" + firing.actor.getName() + "_"
                        + firing.firingIndex + "_" + firing.firingFunction
                        + " -> " + nextFiring.actor.getName() + "_"
                        + nextFiring.firingIndex + "_"
                        + nextFiring.firingFunction + ";" + _eol);
            }
        }

        for (FiringCluster cluster : firingClusters) {
            graph.append("\t" + "subgraph cluster"
                    + firingClusters.indexOf(cluster) + "{" + _eol
                    + "\t\tlabel=\"Cluster_" + firingClusters.indexOf(cluster)
                    + "\";" + _eol);
            for (Firing firing : cluster.actorFirings) {
                graph.append("\t\t" + firing.actor.getName() + "_"
                        + firing.firingIndex + "_" + firing.firingFunction
                        + ";" + _eol);
            }
            graph.append("\t}" + _eol);
        }

        StringBuffer clustersGraph = new StringBuffer();

        clustersGraph.append("digraph clusteredG {" + _eol + "\tsize=\"8,8\""
                + _eol);

        for (FiringCluster cluster : firingClusters) {

            int index = firingClusters.indexOf(cluster);
            clustersGraph.append("\t" + "Cluster_" + index + ";" + _eol);

            for (FiringCluster nextCluster : cluster.nextClusters) {
                //                Firing nextFiring = nextCluster.actorFirings.get(0);
                //                graph.append("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction
                //                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_"
                //                        + nextFiring.firingFunction
                //                        + " [ltail=cluster" + index + ", lhead=cluster" + firingClusters.indexOf(nextCluster) + "];" + _eol);
                clustersGraph.append("\t" + "Cluster_" + index + " -> "
                        + "Cluster_" + firingClusters.indexOf(nextCluster)
                        + ";" + _eol);
            }

            for (FiringCluster nextCluster : cluster.nextIterationClusters) {
                //                Firing nextFiring = nextCluster.actorFirings.get(0);
                //                graph.append("\t" + firing.actor.getName() + "_" + firing.firingIndex + "_" + firing.firingFunction
                //                        + " -> "+ nextFiring.actor.getName() + "_" + nextFiring.firingIndex + "_"
                //                        + nextFiring.firingFunction
                //                        + " [style=dotted, ltail=cluster" + index + ", lhead=cluster" + firingClusters.indexOf(nextCluster) + "];" + _eol);
                clustersGraph.append("\t" + "Cluster_" + index + " -> "
                        + "Cluster_" + firingClusters.indexOf(nextCluster)
                        + "[style=dotted];" + _eol);
            }
        }

        graph.append("}" + _eol);
        clustersGraph.append("}");

        String modelName = CodeGeneratorAdapter.generateName(_model);

        _writeCodeFileName(graph, modelName + "_clusterDependency.dot", true,
                true);
        _writeCodeFileName(clustersGraph, modelName + "_clusteredGraph.dot",
                true, true);

        //System.out.println(graph);
        //System.out.println(clustersGraph);

        esdf.append(INDENT2 + "return firingFunctions;" + _eol);
        esdf.append(INDENT1 + "}" + _eol);
    }

    /** Return the output port (source port) that connects to an input port.
     *
     * @param inputPort The input port.
     * @return Return the output port (source port) that connects to
     * an input port.
     */
    private Set<IOPort> _getConnectedOutputPorts(IOPort inputPort) {
        Set<IOPort> outputPorts = new HashSet();
        for (Object element : inputPort.sourcePortList()) {
            IOPort port = (IOPort) element;

            if (port.isOutput()) {
                if (port.getContainer() instanceof SampleDelay) {
                    outputPorts
                    .addAll(_getConnectedOutputPorts(((SampleDelay) port
                            .getContainer()).input));
                } else {
                    outputPorts.add(port);
                }
            }
        }

        return outputPorts;
    }

    /** Get the list of all input ports that receive tokens from one output port.
     * @param outputPort The output port.
     * @return The list of input port.
     */
    private List _getConnectedInputPorts(IOPort outputPort) {
        List connectedInputPorts = new LinkedList();
        for (Iterator inputPorts = outputPort.connectedPortList().iterator(); inputPorts
                .hasNext();) {
            IOPort connectedPort = (IOPort) inputPorts.next();
            if (connectedPort.isInput()) {
                if (connectedPort.getContainer() instanceof SampleDelay) {
                    connectedInputPorts
                    .addAll(_getConnectedInputPorts(((SampleDelay) connectedPort
                            .getContainer()).output));
                } else {
                    connectedInputPorts.add(connectedPort);
                }
            }
        }
        return connectedInputPorts;
    }

    /** Find input firings that a firing of an internal actor depends on.
     * @param firing The firing of the internal actor.
     * @param inputFirings The derived input firings that the firing of the actor depends on.
     * @param searchedFirings The list of visited firings.
     * @param inputFiringFunctions The list of input firings (firings that consume external tokens).
     * @param outputFiringFunctions The list of output firings (firings that produced external tokens).
     */
    private void _getDependentBackwardFiring(Firing firing, Set inputFirings,
            Set searchedFirings, List inputFiringFunctions,
            List outputFiringFunctions) {

        if (_getFiringFunction(inputFiringFunctions, firing.actor,
                firing.firingFunction) != null
                && !inputFirings.contains(firing)) {
            inputFirings.add(firing);
        }

        for (Object element : firing.previousActorFirings) {
            Firing previousFiring = (Firing) element;
            if (!searchedFirings.contains(previousFiring)) {
                searchedFirings.add(previousFiring);
                _getDependentBackwardFiring(previousFiring, inputFirings,
                        searchedFirings, inputFiringFunctions,
                        outputFiringFunctions);
            }

        }
    }

    /** Find all output firings that depend on a firing of an internal actor.
     * @param firing The firing that we want to find all output firings that depend on it.
     * @param outputFirings The derived output firings that depend on the firing.
     * @param searchedFirings The visited firings used in the search procedure.
     * @param inputFiringFunctions The list of input firings (firings that consume external tokens).
     * @param outputFiringFunctions The list of output firings (firings that produced external tokens).
     */
    private void _getDependentForwardFiring(Firing firing, Set outputFirings,
            Set searchedFirings, List inputFiringFunctions,
            List outputFiringFunctions) {

        if (_getFiringFunction(outputFiringFunctions, firing.actor,
                firing.firingFunction) != null
                && !outputFirings.contains(firing)) {
            outputFirings.add(firing);
        }

        for (Object element : firing.nextActorFirings) {
            Firing nextFiring = (Firing) element;
            if (!searchedFirings.contains(nextFiring)) {
                searchedFirings.add(nextFiring);
                _getDependentForwardFiring(nextFiring, outputFirings,
                        searchedFirings, inputFiringFunctions,
                        outputFiringFunctions);
            }

        }
    }

    /** Get the respective firing function object of an actor and the index of the firing function.
     *
     * @param firingFunctionList The list of firing function object.
     * @param actor The actor that fire.
     * @param index The index of the firing function.
     * @return The found object if the object existed, otherwise return null.
     */
    private SimulationFiringFunction _getFiringFunction(
            List<SimulationFiringFunction> firingFunctionList, Actor actor,
            int index) {
        for (SimulationFiringFunction function : firingFunctionList) {
            if (function.actor == actor && function.functionIndex == index) {
                return function;
            }
        }
        return null;
    }

    /** Get the firing instance of an actor based on firing iteration and firing function index.
     * @param actor The actor that fires.
     * @param index The firing iteration of the actor.
     * @param firingFunction The firing function.
     * @param firingList List of all firings.
     * @return The firing object if the object existed, otherwise return null.
     */

    private Firing _getFiring(Actor actor, int index, int firingFunction,
            Set firingList) {
        Firing ret = null;
        for (Iterator firings = firingList.iterator(); firings.hasNext();) {
            Firing firing = (Firing) firings.next();
            if (firing.actor == actor && firing.firingIndex == index
                    && firing.firingFunction == firingFunction) {
                ret = firing;
                break;
            }
        }

        return ret;
    }

    /** Print out the firing dependency graph for debugging both textually and in dot format.
     * @param inputFirings The list of all firings.
     */
    private void _printGraph(Set inputFirings) {
        //        for (Iterator firings = firingList.iterator(); firings.hasNext();) {
        //            Firing firing = (Firing) firings.next();
        //
        //            System.out.println(firing.actor.getFullName() + ", firing Index: " + firing.firingIndex + " function: " + firing.firingFunction);
        //            for (Iterator nextFirings = firing.nextActorFirings.iterator(); nextFirings.hasNext();) {
        //                Firing nextFiring = (Firing) nextFirings.next();
        //                System.out.println("\t next " + nextFiring.actor.getFullName() + ", firing Index: " + nextFiring.firingIndex + " function: " + nextFiring.firingFunction);
        //            }
        //
        //            for (Iterator previousFirings = firing.previousActorFirings.iterator(); previousFirings.hasNext();) {
        //                Firing previousFiring = (Firing) previousFirings.next();
        //                System.out.println("\t previous " + previousFiring.actor.getFullName() + ", firing Index: " + previousFiring.firingIndex  + " function: " + previousFiring.firingFunction);
        //            }
        //        }

        System.out.println("digraph G1 {" + _eol + "\tsize=\"8,8\"");
        for (Iterator firings = inputFirings.iterator(); firings.hasNext();) {
            Firing firing = (Firing) firings.next();

            for (Object element : firing.nextActorFirings) {
                Firing nextFiring = (Firing) element;
                System.out.println("\t" + firing.actor.getName() + "_"
                        + firing.firingIndex + "_" + firing.firingFunction
                        + " -> " + nextFiring.actor.getName() + "_"
                        + nextFiring.firingIndex + "_"
                        + nextFiring.firingFunction + ";");
            }

            for (Object element : firing.nextIterationFirings) {
                Firing nextFiring = (Firing) element;
                System.out.println("\t" + firing.actor.getName() + "_"
                        + firing.firingIndex + "_" + firing.firingFunction
                        + " -> " + nextFiring.actor.getName() + "_"
                        + nextFiring.firingIndex + "_"
                        + nextFiring.firingFunction + "[style=dotted];");
            }
        }
        System.out.println("}");

    }

    /** Compute the depth of firings to obtain firing order.
     *
     * @param firing
     * @param visitedFirings
     */
    //    private void _computeFiringDepth(Firing firing, Set<Firing> visitedFirings) {
    //        if (visitedFirings.contains(firing)) {
    //            return;
    //        } else {
    //            visitedFirings.add(firing);
    //            for (Firing nextFiring : firing.nextActorFirings) {
    //                if (nextFiring.index <= firing.index) {
    //                    nextFiring.index = firing.index + 1;
    //                }
    //                _computeFiringDepth(nextFiring, visitedFirings);
    //            }
    //
    //            for (Firing previousFiring : firing.previousActorFirings) {
    //                if (previousFiring.index >= firing.index) {
    //                    previousFiring.index = firing.index - 1;
    //                }
    //                _computeFiringDepth(previousFiring, visitedFirings);
    //            }
    //
    //        }
    //    }

    /** Compute the depths of clusters to obtain cluster firing order.
     *
     * @param cluster The cluster whose depth is computed.
     * @param firingClusters the list of visited clusters.
     */
    private void _computeClusterDepth(FiringCluster cluster,
            List<FiringCluster> firingClusters) {

        //Dijkstra algorithm for computing the actor depth
        for (FiringCluster fc : firingClusters) {
            if (cluster.nextClusters.contains(fc)) {
                fc.index = 1;
            } else if (cluster.previousClusters.contains(fc)) {
                fc.index = -1;
            } else {
                fc.index = Integer.MIN_VALUE;
            }
        }
        cluster.index = 0;

        Set<FiringCluster> Q = new HashSet(firingClusters);
        while (Q.size() > 0) {
            FiringCluster closestFC = null;
            Integer maxDistance = Integer.MIN_VALUE;
            for (FiringCluster fc : Q) {
                if (fc.index > maxDistance) {
                    closestFC = fc;
                    maxDistance = fc.index;
                }
            }

            if (closestFC == null) {
                break;
            }

            Q.remove(closestFC);

            for (FiringCluster next : closestFC.nextClusters) {
                next.index = Math.max(next.index, closestFC.index + 1);
            }

            for (FiringCluster previous : closestFC.previousClusters) {
                previous.index = Math.max(previous.index, closestFC.index - 1);
            }
        }
    }

    /** Junction for simulation.
     *
     * @author dai
     *
     */
    static public class SimulationJunction {
        /** Constructor for simulation junction.
         * @param inputPort The input port of the junction.
         * @param outputPort The output port of the junction.
         * @param numInitialTokens The number of initial tokens in the junction.
         */
        public SimulationJunction(IOPort inputPort, IOPort outputPort,
                int numInitialTokens) {
            _inputPort = inputPort;
            _outputPort = outputPort;
            counter = _numInitialTokens = numInitialTokens;
        }

        /** Return the number of initial tokens in a junction.
         * @return The number of initial tokens.
         */
        public int getNumInitialTokens() {
            return _numInitialTokens;
        }

        /**
         * @return The output port of the junction.
         */
        public IOPort getInputPort() {
            return _inputPort;
        }

        /**
         * @return The input port of the junction.
         */
        public IOPort getOutputPort() {
            return _outputPort;
        }

        /** test if the junction has the same number of tokens as its initial state.
         * @return true if currently the junction has the same number of tokens as its initial state.
         */
        public boolean isInInitialState() {
            return _numInitialTokens == counter;
        }

        /** Reset the number of tokens to the junction's initial state.
         */
        public void reset() {
            counter = _numInitialTokens;
        }

        /** The current number of tokens in a junction.
         */
        public int counter;

        /** The number of initial tokens in a junction.
         */
        private int _numInitialTokens;

        /** The input port of the junction.
         */
        private IOPort _inputPort;

        /** The output port of the junction.
         */
        private IOPort _outputPort;
    }

    /** A firing function for simulating (e.g. deadlock simulation)
     * @author dai
     *
     */
    public static class SimulationFiringFunction {
        /** Constructor.
         * @param actor The actor that fires.
         * @param index The index of the firing function of the actor.
         */
        SimulationFiringFunction(Actor actor, int index) {
            this.actor = actor;
            functionIndex = index;
        }

        /** The actor that fires.
         */
        public Actor actor;

        /** The index of the firing function of the actor.
         */
        public int functionIndex;
    }

    /** A firing instance of an actor.
     *
     * @author dai
     *
     */
    public static class Firing {
        /**
         * @param firingActor The actor that fires
         * @param index The index of firing iterations.
         * @param function The firing function of the actor.
         */
        public Firing(Actor firingActor, int index, int function) {
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

    /** A cluster of firings of actors.
     *
     * @author dai
     */
    static public class FiringCluster {
        public FiringCluster() {
            actorFirings = new LinkedList();
            nextClusters = new LinkedList();
            previousClusters = new LinkedList();

            nextIterationClusters = new LinkedList();
            previousIterationClusters = new LinkedList();

            inputPorts = new HashSet();
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
    //    private class FiringComparator implements Comparator<Firing> {
    //        public int compare(Firing f1, Firing f2) {
    //            return (f2.index - f1.index);
    //        }
    //    }

    /** Comparator used to sort the actors. */
    private static class ClusterComparator implements Comparator<FiringCluster> {
        // FindBugs indicates that this should be a static class.

        /** Compare the depths of two actors.
         *  NOTE: This method assumes and does not check that the
         *  depth cache is up to date and contains both specified actors.
         */
        @Override
        public int compare(FiringCluster c1, FiringCluster c2) {
            return c1.index - c2.index;
        }
    }
}
