/* Code generator adapter for generating FMIMA code for Director.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.fmima.adapters.ptolemy.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.sched.Scheduler;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

////Director

/**
 Code generator adapter for generating FMIMA code for Director.

 @see GenericCodeGenerator
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)

 */
public class Director extends FMIMACodeGeneratorAdapter {

    /** Construct the code generator adapter associated with the given director.
     *  Note before calling the generate*() methods, you must also call
     *  setCodeGenerator(GenericCodeGenerator).
     *  @param director The associated director.
     */
    public Director(ptolemy.actor.Director director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                Public Methods                           ////

    /** Generate the code for the firing of actors.
     *  In this base class, it is attempted to fire all the actors once.
     *  In subclasses such as the adapters for SDF and Giotto directors, the
     *  firings of actors observe the associated schedule. In addition,
     *  some special handling is needed, e.g., the iteration limit in SDF
     *  and time advancement in Giotto.
     *  @return The generated code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating fire code for the actor.
     */
    @Override
    public String generateFMIMA() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
                
        NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getAdapter(getComponent());
        Iterator<?> actors = ((CompositeActor) adapter.getComponent()
                .getContainer()).deepEntityList().iterator();
                
        code.append(getCodeGenerator()
                .comment(
                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/Director.java start"
                                + _eol
                                + "   "
                                + adapter.getComponent().getName()));

        // Generate the start of the main() method.
        // FIXME: we should generate the start of the main() method from some other cg method.
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        codeStream.appendCodeBlock("mainStartBlock");
        code.append(processCode(codeStream.toString()));
        
        HashMap<TypedIOPort, Node> actorNodeMap = new HashMap<TypedIOPort, Node>();
        HashMap<Node, TypedIOPort> actorPortMap = new HashMap<Node, TypedIOPort>();
        HashMap<String, Node> actorNodeNamesMap = new HashMap<String, Node>();
        
        DirectedGraph graph = new DirectedGraph();
        
        actors = ((CompositeActor) adapter.getComponent()
                .getContainer()).deepEntityList().iterator();
        // Iterate through the actors and generate connection list.
        while (actors.hasNext()) {

            // FIXME: Check to see if the actor is something other than a FMUImport.

        	ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors.next();

        	// Add all the nodes to the graph (input nodes)
        	for (TypedIOPort input : actor.inputPortList()) { 
        		Node node = new Node(input);
                actorNodeMap.put(input, node);
                actorPortMap.put(node, input);
                actorNodeNamesMap.put(input.getFullName(), node);
                graph.addNode(node);
        	}
        	// Add all the nodes to the graph (output nodes)
        	for (TypedIOPort output : actor.outputPortList()) { 
        		Node node = new Node(output);
                actorNodeMap.put(output, node);
                actorPortMap.put(node, output);
                actorNodeNamesMap.put(output.getFullName(), node);
                graph.addNode(node);
        	}
        	
        }
        
        actors = ((CompositeActor) adapter.getComponent()
                .getContainer()).deepEntityList().iterator();
        
        while (actors.hasNext()) {
        	        	
        	ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors.next();
        	
        	// Add all the topological edges
        	for (TypedIOPort input : actor.inputPortList()) { 
        		Node sinkNode = (Node) actorNodeMap.get(input);
        		List<TypedIOPort> connected_ports = input.connectedPortList();
        		
        		for (int port_idx = 0; port_idx < connected_ports.size(); port_idx++) {
        			TypedIOPort output = (TypedIOPort) connected_ports.get(port_idx);
        			Node sourceNode = (Node) actorNodeMap.get(output);
        			graph.addEdge(sourceNode, sinkNode);
        		}
        	}

        	// Add all the causality relation edges
        	for (TypedIOPort output : actor.outputPortList()) {         		
        		Node sinkNode = (Node) actorNodeMap.get(output);        		
         		Set<String> inputPorts = actor.getInputDependencyList(output.getName());
         		
         		if (inputPorts != null) {         			
 	        		Iterator<String> inputIterator = inputPorts.iterator();
 	        		while(inputIterator.hasNext()) {
 	        			String input = (String) inputIterator.next(); 	        		 	        		
 	        			Node sourceNode = (Node) actorNodeNamesMap.get(input);
 	        			
 	        			System.out.println("---> Causality relation: " + input + " -> " + output.getName());
 	        			
 	        			graph.addEdge(sourceNode, sinkNode);
 	        		}
         		}
        	}
        	
        	code.append("printf(\"Loading FMU " + actor.getName() + "...\\n\");\n");
        	code.append("loadFMU(&fmus[" + actor.getName() + "], \"" + actor.fmuFile.asFile() + "\");\n");
        	code.append("printf(\"Initializing FMU " + actor.getName() + "...\\n\");\n");
        	code.append("fmus[" + actor.getName() + "].component = initializeFMU(&fmus[" + actor.getName() + "], visible, loggingOn, nCategories, categories);\n");
        }
        
        Collection<Node> nodeCollection = graph.nodes();
        
        if (nodeCollection.size() == 0) {
        	throw new IllegalActionException("The GRAPH is empty");
        }
        
        if (graph.isAcyclic() == false) {
        	throw new IllegalActionException("The GRAPH contains cycles. Unable to determine a port updates order.");
        }
                
        List sortedGraph = graph.topologicalSort(nodeCollection);
                
        int connectionIndex = 0;
        while (sortedGraph.size() > 0) {
        	
        	Node portSourceNode = (Node) sortedGraph.get(0);        	
        	Node portSinkNode = (Node) ((List) (graph.reachableNodes(portSourceNode))).get(0);
        	
        	sortedGraph.remove(portSourceNode);
        	sortedGraph.remove(portSinkNode);
        	
        	TypedIOPort outputPort = actorPortMap.get(portSourceNode);
        	TypedIOPort inputPort = actorPortMap.get(portSinkNode);     	
        	
        	ptolemy.actor.lib.fmi.FMUImport sourceActor = (ptolemy.actor.lib.fmi.FMUImport) actorPortMap.get(portSourceNode).getContainer();
        	ptolemy.actor.lib.fmi.FMUImport sinkActor = (ptolemy.actor.lib.fmi.FMUImport) actorPortMap.get(portSinkNode).getContainer();
        	
        	String fmuSourceName = sourceActor.getName();
        	String fmuSinkName = sinkActor.getName();        	
        	
        	code.append("connections[" + connectionIndex + "].sourceFMU = &fmus[" + fmuSourceName + "];\n"
        			+ "connections[" + connectionIndex + "].sourcePort = getValueReference(getScalarVariable(fmus[" + fmuSourceName + "].modelDescription, " + sourceActor.getValueReference(outputPort.getName()) + "));\n"
        			+ "connections[" + connectionIndex + "].sourceType = " + sourceActor.getTypeOfPort(outputPort.getName()) + ";\n"
        			+ "connections[" + connectionIndex + "].sinkFMU = &fmus[" + fmuSinkName + "];\n"
                    + "connections[" + connectionIndex + "].sinkPort = getValueReference(getScalarVariable(fmus[" + fmuSinkName + "].modelDescription, " + sinkActor.getValueReference(inputPort.getName()) + "));\n"
                    + "connections[" + connectionIndex + "].sinkType = " + sinkActor.getTypeOfPort(inputPort.getName()) + ";\n");
        	connectionIndex++;
        }

        // Generate the end of the main() method.
        // FIXME: we should generate the start of the main() method from some other cg method.
        codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        
        codeStream.appendCodeBlock("mainEndBlock");
        code.append(processCode(codeStream.toString()));

        code.append(getCodeGenerator()
                .comment(
                        "ptolemy/cg/adapter/generic/program/procedural/fmima/adapters/ptolemy/actor/Director.java end"));
        return code.toString();
    }
    
    /**
     * Generate the preinitialize code. We do not call the super
     * method, because we have arguments to add here
     * This code contains the variable declarations
     *
     * @return The generated preinitialize code.
     * @exception IllegalActionException If thrown while appending to the
     * the block or processing the macros.
     */
    //    @Override
    //public String generatePreinitializeCode() throws IllegalActionException {

    //    }
}
