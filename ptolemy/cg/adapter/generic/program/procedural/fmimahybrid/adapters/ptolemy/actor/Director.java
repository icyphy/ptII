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
package ptolemy.cg.adapter.generic.program.procedural.fmimahybrid.adapters.ptolemy.actor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ptolemy.fmi.FMIScalarVariable;
import org.ptolemy.fmi.FMIScalarVariable.Causality;
import org.ptolemy.fmi.type.FMIBooleanType;
import org.ptolemy.fmi.type.FMIIntegerType;
import org.ptolemy.fmi.type.FMIRealType;
import org.ptolemy.fmi.type.FMIStringType;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.fmima.FMIMACodeGeneratorAdapter;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Node;
import ptolemy.kernel.util.IllegalActionException;

////Director

/**
 * Code generator adapter for generating FMIMA code for Director.
 * 
 * @see GenericCodeGenerator
 * @author Christopher Brooks
 * @version $Id: Director.java 71665 2015-02-27 22:13:20Z f.cremona $
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Director extends FMIMACodeGeneratorAdapter {

	/**
	 * Construct the code generator adapter associated with the given director.
	 * Note before calling the generate*() methods, you must also call
	 * setCodeGenerator(GenericCodeGenerator).
	 * 
	 * @param director
	 *            The associated director.
	 */
	public Director(ptolemy.actor.Director director) {
		super(director);
	}

	// /////////////////////////////////////////////////////////////////
	// // Public Methods ////

	/**
	 * Generate the code for the firing of actors. In this base class, it is
	 * attempted to fire all the actors once. In subclasses such as the adapters
	 * for SDF and Giotto directors, the firings of actors observe the
	 * associated schedule. In addition, some special handling is needed, e.g.,
	 * the iteration limit in SDF and time advancement in Giotto.
	 * 
	 * @return The generated code.
	 * @exception IllegalActionException
	 *                If the adapter associated with an actor throws it while
	 *                generating fire code for the actor.
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
		// FIXME: we should generate the start of the main() method from some
		// other cg method.
		CodeStream codeStream = _templateParser.getCodeStream();
		codeStream.clear();

		codeStream.appendCodeBlock("mainStartBlock");
		code.append(processCode(codeStream.toString()));

		actors = ((CompositeActor) adapter.getComponent().getContainer())
				.deepEntityList().iterator();
	
		while (actors.hasNext()) {

			ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors
					.next();
			// Add code for loading and initialize FMUs
			code.append("printf(\"Loading FMU " + actor.getName()
					+ "...\\n\");\n");
                        // Replace \ with /.
			code.append("loadFMU(&fmus[" + actor.getName() + "], \""
                                + actor.fmuFile.asFile().toString().replace("\\","/") + "\");\n");
                        code.append("fmuFileNames[" + actor.getName() + "] = strdup(\""
                                + actor.fmuFile.asFile().toString().replace("\\","/") + "\");\n");
			code.append("printf(\"Initializing FMU " + actor.getName()
					+ "...\\n\");\n");
			code.append("fmus[" + actor.getName()
					+ "].component = initializeFMU(&fmus[" + actor.getName()
					+ "], visible, loggingOn, nCategories, categories);\n");
		}
		
		HashMap<Node, FMIScalarVariable> node2Scalar = new HashMap<Node, FMIScalarVariable>();
		HashMap<Node, TypedIOPort> node2Port = new HashMap<Node, TypedIOPort>();
		HashMap<TypedIOPort, FMIScalarVariable> port2Scalar = new HashMap<TypedIOPort, FMIScalarVariable>();
		HashMap<TypedIOPort, Node> port2Node = new HashMap<TypedIOPort, Node>(); 
		
		DirectedGraph graph = new DirectedGraph();
		
		actors = ((CompositeActor) adapter.getComponent().getContainer())
				.deepEntityList().iterator();
		
		// Add all the nodes to the graph
		while (actors.hasNext()) {

			ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors
					.next();
			for (TypedIOPort port : actor.portList()) {
				for (FMIScalarVariable scalar : actor.getScalarVariables()) {
					if ((scalar.causality == Causality.input ||  scalar.causality == Causality.output) 
							&& scalar.name.equals(port.getName())) {
						Node node = new Node(port);
						port2Scalar.put(port, scalar);
						node2Scalar.put(node, scalar);
						port2Node.put(port, node);
						node2Port.put(node, port);
						graph.addNode(node);
					}				
				}
			}
		}
		
		actors = ((CompositeActor) adapter.getComponent().getContainer())
				.deepEntityList().iterator();

		// Add edges to the graph
				
		// Iterate through the actors and generate connection list.
		while (actors.hasNext()) {

			// FIXME: Check to see if the actor is something other than a
			// FMUImport.

			ptolemy.actor.lib.fmi.FMUImport actor = (ptolemy.actor.lib.fmi.FMUImport) actors
					.next();

			for (TypedIOPort output : actor.outputPortList()) {
				FMIScalarVariable scalar = port2Scalar.get(output);
				List<FMIScalarVariable> inputDependencies = actor.getInputDependencyList(scalar);
				if (inputDependencies != null) {
					for (FMIScalarVariable inputScalar : inputDependencies) {
						Node source = null;
						Node sink = port2Node.get(output);
						for (TypedIOPort port : actor.inputPortList()) {
							if (port.getName().equals(inputScalar.name))
								source = port2Node.get(port);
						}
						graph.addEdge(source, sink);
					}
				}
			}

			// Add all the edges due to topological dependencies
			for (TypedIOPort input : actor.inputPortList()) {
				Node sink = port2Node.get(input);				
				List<TypedIOPort> connected_ports = input.connectedPortList();
				for (TypedIOPort output : connected_ports) {					
					Node source = port2Node.get(output);					
					graph.addEdge(source, sink);
				}
			}
		}
		
		actors = ((CompositeActor) adapter.getComponent().getContainer())
				.deepEntityList().iterator();
		
		Collection<Node> nodeCollection = graph.nodes();
                
        // It is ok to have an empty graph for testing purposes, try
        //   cd ptolemy/cg/kernel/generic/program/procedural/fmima/test
        //   $PTII/bin/ptjacl FMIMACodeGenerator.tcl 
		//if (nodeCollection.size() == 0) {
		//	throw new IllegalActionException(adapter.getComponent(), "The GRAPH is empty");
		//}
		
		

		// If diagram contains cycles, throw an exception
		if (graph.isAcyclic() == false) {
                    throw new IllegalActionException(adapter.getComponent(),
					"The model contains cycles. Unable to determine a port updates order.");
		}

		// Sort the graph in order to determine the correct get()/set() sequence
		// to update the node values
		List<Node> sortedGraph = graph.topologicalSort(nodeCollection);
		// Add the sorted connections
		int connectionIndex = 0;
				
		for (Node sourceNode : sortedGraph) {
			if (node2Port.get(sourceNode).isOutput()) {
				Collection<Node> successors = graph.successors(sourceNode);
				for (Node sinkNode : successors) {

					FMIScalarVariable sourceScalar = node2Scalar.get(sourceNode);
					FMIScalarVariable sinkScalar = node2Scalar.get(sinkNode);

					ptolemy.actor.lib.fmi.FMUImport sourceActor = (ptolemy.actor.lib.fmi.FMUImport)
							node2Port.get(sourceNode).getContainer();
					ptolemy.actor.lib.fmi.FMUImport sinkActor = (ptolemy.actor.lib.fmi.FMUImport)
							node2Port.get(sinkNode).getContainer();

					String fmuSourceName = sourceActor.getName();
					String fmuSinkName = sinkActor.getName();

					String sourceType = "";
					String sinkType = "";

					if (sourceScalar.type instanceof FMIBooleanType) {
						sourceType = "fmi2_Boolean";
					} else if (sourceScalar.type instanceof FMIIntegerType) {
						sourceType = "fmi2_Integer";
					} else if (sourceScalar.type instanceof FMIRealType) {
						sourceType = "fmi2_Real";
					} else if (sourceScalar.type instanceof FMIStringType) {
						sourceType = "fmi2_String";
					}

					if (sinkScalar.type instanceof FMIBooleanType) {
						sinkType = "fmi2_Boolean";
					} else if (sinkScalar.type instanceof FMIIntegerType) {
						sinkType = "fmi2_Integer";
					} else if (sinkScalar.type instanceof FMIRealType) {
						sinkType = "fmi2_Real";
					} else if (sinkScalar.type instanceof FMIStringType) {
						sinkType = "fmi2_String";
					}

					code.append("connections["
							+ connectionIndex
							+ "].sourceFMU = &fmus["
							+ fmuSourceName
							+ "];\n"
							+ "connections["
							+ connectionIndex
							+ "].sourcePort = getValueReference(getScalarVariable(fmus["
							+ fmuSourceName + "].modelDescription, "
							+ sourceScalar.valueReference + "));\n"
							+ "connections[" + connectionIndex
							+ "].sourceType = " + sourceType + ";\n"
							+ "connections[" + connectionIndex
							+ "].sinkFMU = &fmus[" + fmuSinkName + "];\n");

					code.append("connections["
							+ connectionIndex
							+ "].sinkPort = getValueReference(getScalarVariable(fmus["
							+ fmuSinkName + "].modelDescription, "
							+ sinkScalar.valueReference + "));\n");

					code.append("connections[" + connectionIndex
							+ "].sinkType = " + sinkType + ";\n");
					connectionIndex++;
				}
			}
		}
			
			


		// Generate the end of the main() method.
		// FIXME: we should generate the start of the main() method from some
		// other cg method.
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
	 * Generate the preinitialize code. We do not call the super method, because
	 * we have arguments to add here This code contains the variable
	 * declarations
	 *
	 * @return The generated preinitialize code.
	 * @exception IllegalActionException
	 *                If thrown while appending to the the block or processing
	 *                the macros.
	 */
	// @Override
	// public String generatePreinitializeCode() throws IllegalActionException {

	// }
}
