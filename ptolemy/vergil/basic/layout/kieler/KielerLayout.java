package ptolemy.vergil.basic.layout.kieler;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import ptolemy.actor.Actor;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.vergil.actor.ActorGraphModel;
import de.cau.cs.kieler.core.KielerException;
import de.cau.cs.kieler.core.alg.BasicProgressMonitor;
import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KGraphPackage;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.kgraph.KPortType;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.layout.options.LayoutDirection;
import de.cau.cs.kieler.kiml.layout.options.LayoutOptions;
import de.cau.cs.kieler.kiml.layout.options.PortConstraints;
import de.cau.cs.kieler.kiml.layout.util.KimlLayoutUtil;
import de.cau.cs.kieler.klodd.hierarchical.HierarchicalDataflowLayoutProvider;
import diva.graph.GraphModel;
import diva.graph.layout.AbstractGlobalLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.layout.LayoutUtilities;

/**
 * Ptolemy Layouter that uses the KIELER layout algorithm from an external
 * library to layout a given ptolemy model.
 * 
 * @author Hauke Fuhrmann
 */
public class KielerLayout extends AbstractGlobalLayout {

	// Maps ptolemy nodes to kieler nodes and edges
	private Map<Object, KNode> _ptolemy2KielerNodes;
	private Map<KNode, Object> _kieler2ptolemyNodes;
	private Map<Relation, List<KEdge>> _ptolemy2KielerEdges;
	private Map<Port, KPort> _ptolemy2KielerPorts;

	private Map<Relation, List<Object>> _relations2EdgesVertices;

	public KielerLayout(LayoutTarget target) {
		super(target);
	}

	@Override
	public void layout(Object composite) {
		// create a Kieler Graph
		KNode kgraph = createGraph(composite);

		// create the layout provider which contains the actual layout algorithm
		HierarchicalDataflowLayoutProvider dataflowLayoutProvider = new HierarchicalDataflowLayoutProvider();

		// create a progress monitor
		IKielerProgressMonitor progressMonitor = new BasicProgressMonitor();

		try {
			// perform layout on the created graph
			dataflowLayoutProvider.doLayout(kgraph, progressMonitor);

			// apply layout to ptolemy model
			applyLayout(kgraph);
		} catch (KielerException exception) {
			MessageHandler.error("Failed executing automatic KIELER layout: "
					+ exception.getMessage(), exception);
		}

		// print some status information
		printStatus(kgraph, progressMonitor);

		// write to XML file for debugging layout
		writeToFile(kgraph);
	}

	/**
	 * Creates a graph for the KIELER API from a ptolemy model.
	 * 
	 * @param composite
	 * @return
	 */
	private KNode createGraph(Object composite) {
		_ptolemy2KielerNodes = new HashMap<Object, KNode>();
		_kieler2ptolemyNodes = new HashMap<KNode, Object>();
		_ptolemy2KielerEdges = new HashMap<Relation, List<KEdge>>();
		_ptolemy2KielerPorts = new HashMap<Port, KPort>();
		_relations2EdgesVertices = new HashMap<Relation, List<Object>>();

		// create graph in KIELER API
		KNode kgraph = KimlLayoutUtil.createInitializedNode();

		// set some global layout options
		LayoutOptions.setLayoutDirection(KimlLayoutUtil.getShapeLayout(kgraph),
				LayoutDirection.HORIZONTAL);

		// traverse ptolemy graph
		LayoutTarget target = this.getLayoutTarget();
		GraphModel graph = target.getGraphModel();
		if (graph instanceof ActorGraphModel) {
			ActorGraphModel aGraph = (ActorGraphModel) graph;

			Set relationSet = new HashSet<Relation>();

			// process nodes
			for (Iterator iterator = aGraph.nodes(composite); iterator
					.hasNext();) {
				Object node = iterator.next();
				Rectangle2D bounds = target.getBounds(node);
				System.out.println("Node with bounds: "
						+ aGraph.getSemanticObject(node) + " " + bounds);

				// here we get the corresponding Ptolemy object
				// FIXME: this breaks with Ptolemy/Diva abstraction
				// for now we need the ptolemy Actor to get the ports and port
				// positions
				// and to distinguish Actors and Relation vertices
				Object semanticNode = aGraph.getSemanticObject(node);

				// handle actors, text and directors
				if (semanticNode instanceof Actor
						|| semanticNode instanceof Attribute
						|| semanticNode instanceof Port) {
					// create new node in KIELER graph and apply the initial
					// size
					// and position
					KNode knode = KimlLayoutUtil.createInitializedNode();
					knode.setParent(kgraph);
					KShapeLayout klayout = KimlLayoutUtil.getShapeLayout(knode);
					klayout.setHeight((float) bounds.getHeight());
					klayout.setWidth((float) bounds.getWidth());
					klayout.setXpos((float) bounds.getX());
					klayout.setYpos((float) bounds.getY());
					// store node for later applying layout back
					_ptolemy2KielerNodes.put(node, knode);
					_kieler2ptolemyNodes.put(knode, node);

					// handle ports
					if (semanticNode instanceof Actor) {
						Actor actor = (Actor) semanticNode;
						List<Port> inputs = actor.inputPortList();
						List<Port> outputs = actor.outputPortList();

						createKPorts(knode, inputs, KPortType.INPUT);
						createKPorts(knode, outputs, KPortType.OUTPUT);

						// get outgoing edges
						for (Port outputPort : outputs) {
							List<Relation> relations = outputPort
									.linkedRelationList();
							for (Relation relation : relations) {
								if (!_relations2EdgesVertices
										.containsKey(relation)) {
									ArrayList list = new ArrayList();
									_relations2EdgesVertices
											.put(relation, list);
								}
								System.out.println("Edge: " + relation);
							}
						}
					}
				}

				// handle relation vertices
				if (semanticNode instanceof Relation) {
					Relation relation = (Relation) semanticNode;
					// put relation in map
					if (!_relations2EdgesVertices.containsKey(relation)) {
						// make list to store all vertices for a relation
						ArrayList list = new ArrayList();
						list.add(node);
						_relations2EdgesVertices.put(relation, list);
					} else
						_relations2EdgesVertices.get(relation).add(node);
				}

				// iterate outgoing edges
				for (Iterator iterator2 = aGraph.outEdges(node); iterator2
						.hasNext();) {
					Object edge = iterator2.next();
					Relation relation = (Relation) aGraph
							.getSemanticObject(edge);
					// put relation in map if it's not in there yet
					if (!_relations2EdgesVertices.containsKey(relation)) {
						ArrayList list = new ArrayList();
						list.add(node);
						_relations2EdgesVertices.put(relation, list);
					}
					System.out.println("Edge: " + relation);
				}
			}

			// iterate all found relations and create KEdges (possibly multiple
			// per relation)
			for (Relation relation : _relations2EdgesVertices.keySet()) {
				List<Port> linkedPorts = relation.linkedPortList();
				Port source = null;
				for (Port port : linkedPorts) {
					if (_ptolemy2KielerPorts.get(port).getType().equals(
							KPortType.OUTPUT)) {
						// take the first found output to be the one and only
						// source
						// could there be relations with more than one source??
						source = port;
						break;
					}
				}
				// there should always be a source port for a relation...
				assert source != null;
				List<KEdge> kedges = new ArrayList<KEdge>();
				_ptolemy2KielerEdges.put(relation, kedges);
				// iterate all other ports and create a new KEdge to that
				for (Port port : linkedPorts) {
					if (!_ptolemy2KielerPorts.get(port).getType().equals(
							KPortType.OUTPUT)) {

						// create new edge in KIELER graph
						KEdge kedge = KimlLayoutUtil.createInitializedEdge();
						KPort kSourcePort = _ptolemy2KielerPorts.get(source);
						kedge.setSourcePort(kSourcePort);
						kedge.setSource(kSourcePort.getNode()); // FIXME: set
																// port
																// correctly
						KPort kTargetPort = _ptolemy2KielerPorts.get(port);
						kedge.setTargetPort(kTargetPort); // FIXME: set port
															// correctly
						kedge.setTarget(kTargetPort.getNode()); // FIXME: set
																// target
																// correctly
						// store edge mapping for later applying layout
						kedges.add(kedge);
					}
				}
			}
		}
		return kgraph;
	}

	private void createKPorts(KNode knode, List<Port> ports, KPortType portType) {
		for (Iterator iterator2 = ports.iterator(); iterator2.hasNext();) {
			Port port = (Port) iterator2.next();
			KPort kport = KimlLayoutUtil.createInitializedPort();
			KShapeLayout kportlayout = KimlLayoutUtil.getShapeLayout(kport);
			// FIXME: set port positions
			kportlayout.setXpos(0);
			kportlayout.setYpos(0);
			kportlayout.setHeight(5);
			kportlayout.setWidth(5);
			knode.getPorts().add(kport);
			kport.setType(portType);
			LayoutOptions.setPortConstraints(kportlayout,
					PortConstraints.FIXED_POS);
			_ptolemy2KielerPorts.put(port, kport);
		}
	}

	/**
	 * Traverses a KNode (supposed to be the graph) and applies all layout
	 * information to the ptolemy model.
	 * 
	 * @param kgraph
	 */
	private void applyLayout(KNode kgraph) {
		// apply node layout
		for (KNode knode : kgraph.getChildren()) {
			Object node = _kieler2ptolemyNodes.get(knode);
			KShapeLayout klayout = KimlLayoutUtil.getShapeLayout(knode);
			LayoutUtilities.place(getLayoutTarget(), node, klayout.getXpos(),
					klayout.getYpos());
		}
		// place relation vertices
		for (Relation relation : _relations2EdgesVertices.keySet()) {
			if (!_relations2EdgesVertices.get(relation).isEmpty()) {
				List<Object> vertices = _relations2EdgesVertices.get(relation);
				List<KEdge> kedges = _ptolemy2KielerEdges.get(relation);
				for (int i = 0; i < kedges.size() - 1; i++) {
					KEdge kedge1 = kedges.get(i);
					KEdge kedge2 = kedges.get(i + 1);
					KPoint splitpoint = findSplitPoint(kedge1, kedge2);
					if (vertices.size() > i) {
						LayoutUtilities.place(this.getLayoutTarget(), vertices
								.get(i), splitpoint.getX(), splitpoint.getY());
					}
				}
			}
		}
	}

	private void printStatus(KNode kgraph,
			IKielerProgressMonitor progressMonitor) {
		System.out.println("KIELER Execution Time: "
				+ (progressMonitor.getExecutionTime() * 1000) + " ms");
	}

	private KPoint findSplitPoint(KEdge edge1, KEdge edge2) {
		EList bendpoints1 = KimlLayoutUtil.getEdgeLayout(edge1).getBendPoints();
		EList bendpoints2 = KimlLayoutUtil.getEdgeLayout(edge2).getBendPoints();
		// initialize point with first available bendpoint
		KPoint point = (KPoint) bendpoints1.get(0);
		assert point != null;
		for (int i = 0; i < bendpoints1.size(); i++) {
			if (bendpoints2.size() > i) {
				KPoint p1 = (KPoint) bendpoints1.get(i);
				KPoint p2 = (KPoint) bendpoints2.get(i);
				if (p1.getX() == p2.getX() && p1.getY() == p2.getY()) {
					point = p1;
				}
			}
		}
		return point;
	}

	/**
	 * Write a KGraph (Kieler graph datastructure) to a file in its XMI
	 * representation. Can be used for debugging (manually look at it) or
	 * loading it elsewhere, e.g. a KIELER Graph viewer.
	 * 
	 * @param kgraph
	 */
	private void writeToFile(KNode kgraph) {
		// Create a resource set.
		ResourceSet resourceSet = new ResourceSetImpl();

		// Register the default resource factory -- only needed for stand-alone!
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
						new XMIResourceFactoryImpl());

		try {
			// Get the URI of the model file.
			File file = new File("kgraph.xmi");
			URI fileURI = URI.createFileURI(file.getAbsolutePath());

			// Demand load the resource for this file.
			Resource resource = resourceSet.createResource(fileURI);

			resource.getContents().add(kgraph);
			
			// Print the contents of the resource to System.out.
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
		}
	}
}
