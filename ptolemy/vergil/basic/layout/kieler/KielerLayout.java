package ptolemy.vergil.basic.layout.kieler;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.graph.GraphInvalidStateException;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.PortTerminal;
import de.cau.cs.kieler.core.KielerException;
import de.cau.cs.kieler.core.alg.BasicProgressMonitor;
import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.kgraph.KPortType;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KLayoutDataFactory;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.layout.options.LayoutDirection;
import de.cau.cs.kieler.kiml.layout.options.LayoutOptions;
import de.cau.cs.kieler.kiml.layout.options.PortConstraints;
import de.cau.cs.kieler.kiml.layout.util.BoxLayoutProvider;
import de.cau.cs.kieler.kiml.layout.util.KimlLayoutUtil;
import de.cau.cs.kieler.klodd.hierarchical.HierarchicalDataflowLayoutProvider;
import diva.canvas.CanvasComponent;
import diva.canvas.CompositeFigure;
import diva.canvas.TransformContext;
import diva.graph.GraphModel;
import diva.graph.layout.AbstractGlobalLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.modular.EdgeModel;

/**
 * Ptolemy Layouter that uses the KIELER layout algorithm from an external
 * library to layout a given ptolemy model.
 * 
 * See http://www.informatik.uni-kiel.de/rtsys/kieler/ for more informatio about
 * KIELER.
 * 
 * KIELER - Kiel Integrated Environment for Layout for the Eclipse
 * RichClientPlatform
 * 
 * The KIELER project tries to enhance graphical modeling pragmatics. Next to
 * higher level solutions (meta layout, structure based editing...) developed
 * for Eclipse models, it also implements custom layout algorithms.
 * 
 * This class interfaces a standalone KIELER layout algorithm for actor oriented
 * port based graphical diagrams with a Ptolemy diagram.
 * 
 * While KIELER is mainly developed for an Eclipse environment, most algorithms
 * are also available standalone and can be used in a non Eclipse environment.
 * This class is a try to leverage this to apply KIELER algorithms with Ptolemy.
 * No Eclipse is required with that. Only one standalone external library.
 * 
 * Calling the layout() method will create a new Kieler graph datastructure, run
 * Kieler layout algorithms on it and augment it with resulting layout
 * information (locations and sizes of nodes, bendoints of connections). Then
 * this layout gets applied to the Ptolemy model. Moving of nodes in Ptolemy is
 * done via adding or changing location attributes. Setting bendpoints is not
 * yet supported in Ptolemy, hence this has to be emulated here. For each
 * bendpoint for an edge in a Kieler graph, a new Relation with a Vertex is
 * inserted in the Ptolemy model. They get interconnected and old Relations get
 * removed.
 * 
 * This transformation uses severe model structure transformations which are not
 * yet stable to be semantic preserving. So regard this only as a testing
 * version and proof of concept.
 * 
 * WARNING: Never run this layouter on important models where you don't have
 * backups! The model structure get changed and might alter the model semantics!
 * 
 * 
 * @author Hauke Fuhrmann, <haf@informatik.uni-kiel.de>
 */
public class KielerLayout extends AbstractGlobalLayout {

	/**
	 * Map Diva nodes to Kieler Nodes.
	 * 
	 * @see _kieler2ptolemyDivaNodes
	 */
	private Map<Object, KNode> _ptolemy2KielerNodes;

	/**
	 * Map Kieler nodes to Diva Nodes. Opposite of _ptolemy2KielerNodes
	 * 
	 * @see _ptolemy2KielerNodes
	 */
	private Map<KNode, Object> _kieler2ptolemyDivaNodes;

	/**
	 * Map Kieler Nodes to Ptolemy Nodes.
	 */
	private Map<KNode, NamedObj> _kieler2ptolemyEntityNodes;

	/**
	 * Map a set of Ptolemy relations to a set of Kieler edges.
	 */
	private Map<Set<Relation>, Set<KEdge>> _ptolemy2KielerEdges;

	/**
	 * Map Ptolemy ports to Kieler ports.
	 */
	private Map<Port, KPort> _ptolemy2KielerPorts;

	/**
	 * Map Kieler ports to Ptolemy ports.
	 */
	private Map<KPort, Port> _kieler2PtolemyPorts;

	/**
	 * Map a Ptolemy relation to a list of Diva nodes which are relation
	 * Vertices belonging to that relation. The keylist will contain all Ptolemy
	 * relations.
	 */
	private Map<Relation, List<Object>> _relations2EdgesVertices;

	/**
	 * Map Ptolemy links (= Edges in Diva) to Edges in Kieler
	 */
	private Map<Object, KEdge> _ptolemyDiva2KielerEdges;

	private Map<KEdge, Object> _kieler2PtolemyDivaEdges;

	private Map<Object, Object> _ptolemyDivaEdge2ptolemySourcePort;

	/**
	 * The top level Ptolemy composite actor that contains the diagram that is
	 * to be laid out.
	 */
	private CompositeActor _compositeActor;

	/**
	 * StringBuffer for Requests of Model changes. In Ptolemy the main
	 * infrastructure to do model changes is through XML change requests of the
	 * XML representation. This field is used to collect all changes in one
	 * String and then carry them out in only one operation whereas possible.
	 */
	private StringBuffer _momlChangeRequest;

	private boolean _debug = true;

	/**
	 * Constructor taking a LayoutTarget for specifying some methods for layout
	 * handling as given by the standard Ptolemy AbstractGlobalLayout. The
	 * KielerLayout will need access to the top level Ptolemy model, so either
	 * use corresponding constructor or call setModel() prior to layout
	 * invocation.
	 * 
	 * @param target
	 */
	public KielerLayout(LayoutTarget target) {
		super(target);
	}

	/**
	 * Preferred constructor setting the LayoutTarget as requested by the
	 * AbstractGlobalLayout and the containing Ptolemy model.
	 * 
	 * @param target
	 * @param ptolemyContainer
	 */
	public KielerLayout(LayoutTarget target, CompositeActor ptolemyContainer) {
		super(target);
		this.setModel(ptolemyContainer);
	}

	/**
	 * Layout the given composite. Main entry point for the layout action.
	 * Create an Kieler KGraph datastructure corresponding to the Ptolemy model,
	 * instanciate a Kieler layout algorithm (AbstractLayoutProvider) and run
	 * its doLayout() method on the KGraph. The KGraph gets augmented with
	 * layout information (position and sizes of objects and bendpoints for
	 * connections). This information is then reapplied to the ptolemy model by
	 * stating MoMLChangeRequests with location attributes for nodes. Connection
	 * bendpoints of Kieler Edges will be represented by Ptolemy Relations with
	 * a visible Vertex object per bendpoint to emulate "real" persistent
	 * bendpoints (as yet Ptolemy has no notion of bendpoints). Existing
	 * relations get replaced by these new relations.
	 * 
	 * @param composite
	 *            the container of the diagram in terms of an GraphModel.
	 */
	@Override
	public void layout(Object composite) {
		// create a Kieler Graph
		KNode hierarchicalLayoutNode = KimlLayoutUtil.createInitializedNode();
		KNode boxLayoutNode = KimlLayoutUtil.createInitializedNode();
		hierarchicalLayoutNode.setParent(boxLayoutNode);
		LayoutOptions.setLayoutDirection(KimlLayoutUtil
				.getShapeLayout(hierarchicalLayoutNode), LayoutDirection.HORIZONTAL);

		createGraph(composite, hierarchicalLayoutNode, boxLayoutNode);

		// create the layout providers which contains the actual layout algorithm
		HierarchicalDataflowLayoutProvider hierarchicalLayoutProvider = new HierarchicalDataflowLayoutProvider();
		BoxLayoutProvider boxLayoutProvider = new BoxLayoutProvider();

		// create a progress monitor
		IKielerProgressMonitor progressMonitor = new BasicProgressMonitor();

		try {
			// perform layout on the created graph
			hierarchicalLayoutProvider.doLayout(hierarchicalLayoutNode, progressMonitor.subTask(10));
			boxLayoutProvider.doLayout(boxLayoutNode, progressMonitor.subTask(10));

			// write to XML file for debugging layout
			// writing to file requires XMI resource factory
			if(_debug ) 
				writeToFile(boxLayoutNode);

			// apply layout to ptolemy model
			applyLayout(boxLayoutNode);
		} catch (KielerException e) {
			throw new GraphInvalidStateException(e,
					"KIELER runtime exception: " + e.getMessage());
		}

		// print some status information
		printStatus(boxLayoutNode, progressMonitor);

	}

	/**
	 * Creates a graph for the KIELER API from a ptolemy model. Will traverse
	 * the low level GraphModel given by the composite and record all found
	 * elements in the mapping fields of this object that keep a mapping between
	 * Ptolemy/Diva objects and Kieler objects. New Kieler objects (KEdge,
	 * KNode, KPort) get created for their respective Ptolemy counterparts and
	 * initialized with the initial sizes and positions and are put in a
	 * composite KNode (the graph Kieler will perform the layout on later) that
	 * is also returned. To obtain the right mappings, multiple abstraction
	 * levels of Ptolemy are considered here: Diva, as this was the intended
	 * original way to do automatic layout (e.g. by GlobalAbstractLayout) and
	 * Ptolemy, as Diva lacks certain concepts that are relevant for a proper
	 * layout, as for example exact port locations for considering port
	 * constraints in the model, supported by Kieler.
	 * 
	 * @param composite
	 *            the GraphModel composite object to retrieve the model
	 *            information from
	 * @param boxLayoutNode 
	 * @param hierarchicalLayoutNode 
	 * @return a composite KNode Kieler node object, that will contain the
	 *         corresponding graph structure
	 */
	private void createGraph(Object composite, KNode hierarchicalLayoutNode, KNode boxLayoutNode) {
		_ptolemy2KielerNodes = new HashMap<Object, KNode>();
		_kieler2ptolemyDivaNodes = new HashMap<KNode, Object>();
		_kieler2ptolemyEntityNodes = new HashMap<KNode, NamedObj>();
		_ptolemy2KielerEdges = new HashMap<Set<Relation>, Set<KEdge>>();
		_ptolemyDiva2KielerEdges = new HashMap<Object, KEdge>();
		_kieler2PtolemyDivaEdges = new HashMap<KEdge, Object>();
		_ptolemy2KielerPorts = new HashMap<Port, KPort>();
		_kieler2PtolemyPorts = new HashMap<KPort, Port>();
		_relations2EdgesVertices = new HashMap<Relation, List<Object>>();
		_ptolemyDivaEdge2ptolemySourcePort = new HashMap<Object, Object>();


		// traverse ptolemy graph
		LayoutTarget target = this.getLayoutTarget();
		GraphModel graph = target.getGraphModel();
		if (graph instanceof ActorGraphModel) {
			ActorGraphModel aGraph = (ActorGraphModel) graph;

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
					KShapeLayout klayout = KimlLayoutUtil.getShapeLayout(knode);
					klayout.setHeight((float) bounds.getHeight());
					klayout.setWidth((float) bounds.getWidth());
					klayout.setXpos((float) bounds.getX());
					klayout.setYpos((float) bounds.getY());
					// transform coordinates
					ptolemy2KNode(klayout);
					LayoutOptions.setFixedSize(klayout);

					// store node in the correct composite node depending on
					// whether it has connections or not
					if (hasConnections((NamedObj) semanticNode))
						knode.setParent(hierarchicalLayoutNode);
					else
						knode.setParent(boxLayoutNode);

					// store node for later applying layout back
					_ptolemy2KielerNodes.put(node, knode);
					_kieler2ptolemyDivaNodes.put(knode, node);
					_kieler2ptolemyEntityNodes.put(knode,
							(NamedObj) semanticNode);

					// handle ports
					if (semanticNode instanceof Actor && semanticNode instanceof Entity) {
						Actor actor = (Actor) semanticNode;
						Entity entity = (Entity) semanticNode;
						List<Port> inputs = actor.inputPortList();
						List<Port> outputs = actor.outputPortList();

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
						
//						// get incoming edges
//						for (Port inputPort : inputs) {
//							List<Relation> relations = inputPort
//									.linkedRelationList();
//							// store all linked relations in global map for later
//							for (Relation relation : relations) {
//								if (!_relations2EdgesVertices
//										.containsKey(relation)) {
//									ArrayList list = new ArrayList();
//									_relations2EdgesVertices
//											.put(relation, list);
//								}
//								System.out.println("Edge: " + relation);
//							}
//						}
						
						// create ports
						double[] location = this.getLocation(entity);
						createKPorts(knode, location, inputs, KPortType.INPUT);
						createKPorts(knode, location, outputs, KPortType.OUTPUT);
					}
				}

				// handle relation vertices
				else if (semanticNode instanceof Relation) {
					// Relation relation = (Relation) semanticNode;
					// // put relation in map
					// if (!_relations2EdgesVertices.containsKey(relation)) {
					// // make list to store all vertices for a relation
					// ArrayList list = new ArrayList();
					// list.add(node);
					// _relations2EdgesVertices.put(relation, list);
					// } else
					// _relations2EdgesVertices.get(relation).add(node);

					// regard a relation vertex as a Kieler KNode
					KNode kVertexNode = createKNodeForVertex((Vertex) node);
					// add it to the graph
					kVertexNode.setParent(hierarchicalLayoutNode);
					// store it in the maps
					_ptolemy2KielerNodes.put(node, kVertexNode);
					_kieler2ptolemyDivaNodes.put(kVertexNode, node);
					_kieler2ptolemyEntityNodes.put(kVertexNode,
							(NamedObj) semanticNode);
				}

				// check if the node has ports
				Iterator portIter = null;
				if (semanticNode instanceof Relation) {
					// if object is a relation vertex, it is itself kinda port
					List relations = new ArrayList();
					relations.add(node);
					portIter = relations.iterator();
				} else if (semanticNode instanceof Actor) {
					portIter = aGraph.nodes(node);
				}
				if (portIter != null) {
					for (; portIter.hasNext();) {
						Object divaPort = portIter.next();
						// iterate all outgoing edges
						for (Iterator iterator3 = aGraph.outEdges(divaPort); iterator3
								.hasNext();) {
							Object divaEdge = iterator3.next();
							// store Diva edge in corresponding map with no
							// KEdge that will be created later
							_ptolemyDiva2KielerEdges.put(divaEdge, null);

						}
					}
				}
			}

			// // find out what is connected to what, i.e. aggregate all
			// relations
			// // to relation groups
			// Set<List<Relation>> relationGroups =
			// getRelationGroups(_relations2EdgesVertices
			// .keySet());
			// for (List<Relation> relationGroup : relationGroups) {
			// // better work with a set to make no assumptions about the order
			// Set<Relation> relationGroupSet = new HashSet<Relation>();
			// relationGroupSet.addAll(relationGroup);
			// createKEdges(relationGroupSet);
			// }

			// create Kieler KEdges for Diva edges
			for (Object divaEdge : _ptolemyDiva2KielerEdges.keySet()) {
				EdgeModel edgeModel = aGraph.getEdgeModel(divaEdge);
				Object sourceNode = edgeModel.getHead(divaEdge);
				Object targetNode = edgeModel.getTail(divaEdge);
				// directions of Diva Edges actually might differ actual Ptolemy
				// model directions, so check those
				KPort kRealSourcePort = getSource(divaEdge, aGraph, null);
				KPort ksourcePort = this.getPort(sourceNode, KPortType.OUTPUT);
				
				if(kRealSourcePort != ksourcePort){
					// swap source and target
					Object temp = sourceNode;
					sourceNode = targetNode;
					targetNode = temp;
				}
				
				// create KEdge
				ksourcePort = this.getPort(sourceNode, KPortType.OUTPUT);
				KPort ktargetPort = this.getPort(targetNode, KPortType.INPUT);

				KEdge kedge = KimlLayoutUtil.createInitializedEdge();
				kedge.setSourcePort(ksourcePort);
				kedge.setTargetPort(ktargetPort);
				kedge.setSource(ksourcePort.getNode());
				kedge.setTarget(ktargetPort.getNode());

				// add KEdge to map
				_ptolemyDiva2KielerEdges.put(divaEdge, kedge);
				_kieler2PtolemyDivaEdges.put(kedge, divaEdge);
			}
		}
	}

	private KPort getSource(Object divaEdge, ActorGraphModel aGraph, Object ignoreNode) {
		EdgeModel edgeModel = aGraph.getEdgeModel(divaEdge);
		Object[] nodes = new Object[2];
		nodes[0] = edgeModel.getHead(divaEdge);
		nodes[1] = edgeModel.getTail(divaEdge);
		// which of the two is actually first in data flow direction?

		// see if you can find out anything about ports
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] instanceof Port) {
				Port port = (Port) nodes[i];
				NamedObj obj = port.getContainer();
				if (obj instanceof Actor) {
					Actor actor = (Actor) obj;
					if (actor.outputPortList().contains(port))
						return getPort(nodes[i], KPortType.OUTPUT);
					else if (actor.inputPortList().contains(port))
						;
					// then return the other one
					// FIXME: Vertices are no ports!
					return getPort(nodes[(i + 1)% nodes.length], KPortType.OUTPUT);
				}
			}
		}

		// things get more complex if we have two relations...
		// so here try to search recursively until you found any source

		for (int i = 0; i < nodes.length; i++) {
			// check only for vertices and if the node is not marked to be ignored (to avoid loops)
			if (nodes[i] instanceof Vertex && nodes[i] != ignoreNode) {
				// look at all edges to or from the vertex except our original
				// one
				Set<Object> edges = new HashSet<Object>();
				edges.addAll(getList(aGraph.outEdges(nodes[i])));
				edges.addAll(getList(aGraph.inEdges(nodes[i])));
				edges.remove(divaEdge);
				for (Object object : edges) {
					KPort foundSourcePort = getSource(object, aGraph, nodes[i]);
					if (foundSourcePort != null) {
						// yeah, somewhere along that edge we found a source
						return getPort(nodes[i], KPortType.OUTPUT);
					}
				}
			}
		}
		 
		// if nothing was found yet, we could not determine whether the edge is
		// connected to a source somewhere or not
		return null;

	}

	private static List getList(Iterator iter) {
		List list = new ArrayList();
		for (; iter.hasNext();) {
			Object o = iter.next();
			list.add(o);
		}
		return list;
	}

	/**
	 * For a set of relations get a set of relation groups, i.e. for each
	 * relation construct a list of relations that are all interconnected,
	 * either directly or indirectly.
	 * 
	 * @param relations
	 *            Set of relations
	 * @return a Set of relation groups as given by List<Relation> objects by
	 *         Ptolemy
	 */
	private Set<List<Relation>> getRelationGroups(Set<Relation> relations) {
		Set<List<Relation>> relationGroups = new HashSet<List<Relation>>();
		for (Relation relation : relations) {
			List<Relation> relationGroup = relation.relationGroupList();
			// check if we already have this relation group
			// TODO: verify whether relation groups are unique. Then you could
			// perform this check much more efficiently
			boolean found = false;
			for (List<Relation> listedRelationGroup : relationGroups) {
				if (listedRelationGroup.containsAll(relationGroup)) {
					found = true;
					break;
				}
			}
			if (!found) {
				relationGroups.add(relationGroup);
			}
		}
		return relationGroups;
	}

	/**
	 * Create Kieler edges (KEdge) corresponding to a given set of
	 * interconnected relations (a relation group as given by Ptolemy). The
	 * notion of edged in Ptolemy and Kieler somewhat differ: Kieler only has
	 * direct point-to-point KEdges from one port to another where the edge
	 * bendpoints will overlap as long as possible to reduce the amount of
	 * visible connections. However, Ptolemy only allows exactly one edge to
	 * link to a port (not regarding multiports). Connections to multiple ports
	 * are done via explicit relation vertices which may be connected to
	 * multiple ports and even to other relation vertices. Given the
	 * relationGroup, the corresponding new KEdge objects will be stored in the
	 * private mapping fields of this object.
	 * 
	 * @param relationGroup
	 *            the given relation group for which Kieler edges shall be
	 *            created. All relations in the Set must be interconnected
	 *            directly or indirectly.
	 */
	private void createKEdges(Set<Relation> relationGroup) {
		if (relationGroup != null && !relationGroup.isEmpty()) {
			// get any of the relations to work with
			Relation anyRelation = relationGroup.iterator().next();
			// find all ports connected to the relation group
			List<Port> linkedPorts = anyRelation.linkedPortList();
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
			// but unfortunately that is not true. there can be invalid Ptolemy
			// models. Kieler cannot handle that yet.
			if (source == null) {
				String portsString = "Involved ports: ";
				for (Port port : linkedPorts) {
					portsString += port.getName(_compositeActor) + ", ";
				}
				throw new GraphInvalidStateException(
						"A relation group is not connected to any source port. This is not supported by KIELER. "
								+ portsString);
			}
			Set<KEdge> kedges = new HashSet<KEdge>();
			_ptolemy2KielerEdges.put(relationGroup, kedges);
			// iterate all other ports and create a new KEdge to that
			for (Port port : linkedPorts) {
				if (!_ptolemy2KielerPorts.get(port).getType().equals(
						KPortType.OUTPUT)) {

					// create new edge in KIELER graph
					KEdge kedge = KimlLayoutUtil.createInitializedEdge();
					KPort kSourcePort = _ptolemy2KielerPorts.get(source);
					kedge.setSourcePort(kSourcePort);
					kedge.setSource(kSourcePort.getNode());
					KPort kTargetPort = _ptolemy2KielerPorts.get(port);
					kedge.setTargetPort(kTargetPort);
					kedge.setTarget(kTargetPort.getNode());
					// store edge mapping for later applying layout
					kedges.add(kedge);
				}
			}
		}
	}

	/**
	 * Create Kieler ports (KPort) for a Kieler node (KNode) given a list of
	 * Ptolemy Port objects and a port type (incoming, outgoing). The new KPorts
	 * get initialized in terms of size and position from the Ptolemy
	 * counterparts and attached to the corresponding KNode and registered in
	 * the mapping fields of this object.
	 * 
	 * @param knode
	 *            the KNode to create Kieler ports for
	 * @param referenceLocation 
	 * @param node 
	 * @param ports
	 *            the Ptolemy ports counterparts for which to create Kieler
	 *            ports
	 * @param portType
	 *            Type of port, input or output. This is relevant for some
	 *            Kieler layout algorithms. Additionally, a Kieler model might
	 *            get rejected if its regarded as incorrect (e.g. an edge should
	 *            have a outgoing source and an incoming target port).
	 */
	private void createKPorts(KNode knode, double[] referenceLocation, List<Port> ports, KPortType portType) {
		for (Iterator iterator2 = ports.iterator(); iterator2.hasNext();) {
			Port port = (Port) iterator2.next();
			
//			// handle multiports for inputs
//			if(portType.equals(KPortType.INPUT)){
//				List relations = port.linkedRelationList();
//				
//			}
			
			// create a new Kieler port
			KPort kport = KimlLayoutUtil.createInitializedPort();
			KShapeLayout kportlayout = KimlLayoutUtil.getShapeLayout(kport);
			// init port layout 
			kportlayout.setXpos(0);
			kportlayout.setYpos(0);
			kportlayout.setHeight(5);
			kportlayout.setWidth(5);
			// add port to node and set type and options
			knode.getPorts().add(kport);
			kport.setType(portType);
			LayoutOptions.setPortConstraints(kportlayout,
					PortConstraints.FIXED_POS);
			// try to set actual layout (size and position)
			Object portObject = this.getLayoutTarget().getVisualObject(port);
			if(portObject instanceof PortTerminal){
				// get visual Diva figure of port
				PortTerminal portFigure = (PortTerminal)portObject;
				// get bounds of the port figure 
				// (= relative to center of actor symbol here given by referenceLocation)
				Rectangle2D portBounds = portFigure.getBounds();
				// get the parent Diva figure which is the whole composite consisting of
				// the actor icon and its name which might be arbitrary large
				CanvasComponent parent = portFigure.getParent();
				if(parent instanceof CompositeFigure && referenceLocation.length == 2){
					CompositeFigure parentFigure = (CompositeFigure)parent;
					
					// port bounds are given in some specific transform context
					// to get the absolute port boundscall the corresponding transform
					TransformContext context = portFigure.getTransformContext();
					AffineTransform portTransform = context.getTransform();
					Point2D.Double portLocation = new Point2D.Double(portBounds.getMinX(), portBounds.getMinY());
					Point2D.Double transformedLocation = new Point2D.Double();
					portTransform.transform(portLocation, transformedLocation);
					// calculate coordinates relative to the Kieler nodes top left from absolutes
					double x = transformedLocation.getX() - parentFigure.getBounds().getMinX(); 
					double y = transformedLocation.getY() - parentFigure.getBounds().getMinY();
					kportlayout.setXpos((float)x);
					kportlayout.setYpos((float)y);
					kportlayout.setWidth((float)portBounds.getWidth());
					kportlayout.setHeight((float)portBounds.getHeight());
				}
				// transform koordinates to kieler coordinate system
				//ptolemy2KNode(knodelayout, kportlayout);
			}
			
			_ptolemy2KielerPorts.put(port, kport);
			_kieler2PtolemyPorts.put(kport, port);
		}
	}

	/**
	 * Traverses a composite KNode containing corresponding Kieler nodes, ports
	 * and edges for the Ptolemy model and applies all layout information
	 * contained by it back to the Ptolemy model. All changes to the Ptolemy
	 * model are done via MoMLChangeRequests. Location attributes for all
	 * visible Ptolemy nodes are set. For all edges, bendpoints get emulated in
	 * Ptolemy by adding new relation Vertex objects to the model and
	 * interconnect them, one for each bendpoint. This might add many new
	 * vertices. Old relations get deleted. Hence this method really changes the
	 * model structure to get a specific visual representation. The changes are
	 * not guaranteed to keep the model semantics.
	 * 
	 * @param kgraph
	 *            The Kieler graph object containing all layout information to
	 *            apply to the Ptolemy model
	 */
	private void applyLayout(KNode kgraph) {
		//System.out.println("GRAPH:\n" + toString(kgraph));
		_momlChangeRequest = new StringBuffer();
		GraphModel graph = this.getLayoutTarget().getGraphModel();
		if (graph instanceof ActorGraphModel) {
			// apply node layout
			for (KNode knode : _kieler2ptolemyEntityNodes.keySet()) {
				KShapeLayout absoluteLayout = getAbsoluteLayout(knode);

				NamedObj namedObj = _kieler2ptolemyEntityNodes.get(knode);

				// transform koordinate systems
				kNode2Ptolemy(absoluteLayout, knode);

				
				if (namedObj instanceof Relation) {
					Vertex vertex = (Vertex) _kieler2ptolemyDivaNodes
							.get(knode);
					setLocation(vertex, (Relation) namedObj, absoluteLayout
							.getXpos(), absoluteLayout.getYpos());
				} else{
					setLocation(namedObj, absoluteLayout.getXpos(),
							absoluteLayout.getYpos());
				}

			}
			// route edges
			// for (Set<Relation> relationGroup : _ptolemy2KielerEdges.keySet())
			// {
			// Set<KEdge> kedges = _ptolemy2KielerEdges.get(relationGroup);
			//
			// // remove old relation group
			// this.removeRelations(relationGroup);
			//
			// // create a new connection tree, that represents on what
			// // segments in
			// // the KIELER graph
			// // the edges are routed at the same place
			// for (KEdge edge : kedges) {
			// System.out.println(toString(edge));
			// }
			// HyperedgeConnectionTree connectionTree = new
			// HyperedgeConnectionTree();
			// connectionTree.addAll(kedges);
			// System.out.println("Edges: " + kedges.size() + " Tree: "
			// + connectionTree);
			// // dynamically add relations
			// String firstRelation = addRelationswithVertices(connectionTree);
			// // also link source port with first relation
			// if (firstRelation != null) {
			// KEdge edge = kedges.iterator().next();
			// Port port = _kieler2PtolemyPorts.get(edge.getSourcePort());
			// this.link("port", port.getName(_compositeActor),
			// "relation", firstRelation);
			// }
			// }
		}

		// create change request and fire it
		_momlChangeRequest.insert(0, "<group>");
		_momlChangeRequest.append("</group>");
		System.out.println("moml:" + _momlChangeRequest);
		MoMLChangeRequest request = new MoMLChangeRequest(this,
				_compositeActor, _momlChangeRequest.toString());
		request.setUndoable(true);
		_compositeActor.requestChange(request);
	}

	private KShapeLayout getAbsoluteLayout(KNode node) {
		KShapeLayout klayout = KimlLayoutUtil.getShapeLayout(node);
		KShapeLayout absoluteLayout = KLayoutDataFactory.eINSTANCE
				.createKShapeLayout();
		absoluteLayout.setHeight(klayout.getHeight());
		absoluteLayout.setWidth(klayout.getWidth());
		float offsetX = 0, offsetY = 0;
		KNode parent = node.getParent();
		while (parent != null) {
			KShapeLayout parentLayout = KimlLayoutUtil.getShapeLayout(parent);
			offsetX += parentLayout.getXpos();
			offsetY += parentLayout.getYpos();
			parent = parent.getParent();
		}
		absoluteLayout.setXpos(klayout.getXpos() + offsetX);
		absoluteLayout.setYpos(klayout.getYpos() + offsetY);
		return absoluteLayout;
	}

	/**
	 * Add new relations to a Ptolemy model corresponding to a connection tree
	 * represented by the given HyperedgeConnectionTree. The tree specifies the
	 * splitpoints where multiple edge split up to go on seperate ways while in
	 * upper levels they shared a piece of the same path (same bendpoints).
	 * Traverse the connection tree and create new relation creation request and
	 * link creation requests in this object's MoMLChangeRequest buffer. The
	 * changes are not applied until the change request is explicitly executed,
	 * resp. requested to be executed by the Ptolemy model. While linking to
	 * target ports is also done here, linking to the one source port must be
	 * done outside. For this the name of the first relation gets returned.
	 * 
	 * @param connectionTree
	 *            connection tree representing the connection structure of a
	 *            given set of common edges
	 * @return the name of the first relation created from the connection tree.
	 *         This relation name might be required to connect the whole new
	 *         reltion group to some output source port
	 */
	private String addRelationswithVertices(
			HyperedgeConnectionTree connectionTree) {
		List<KPoint> bendpoints = connectionTree.bendPointList();
		System.out.println("Tree: " + connectionTree);
		String firstRelationName = null;
		String relationName = null;
		// for all bendpoints
		for (KPoint point : bendpoints) {
			if (point != null) {
				// create new Relation
				String newRelationName = this.createRelationWithVertex(point
						.getX(), point.getY());
				if (relationName != null) {
					// and link all Relations
					this.link("relation1", relationName, "relation2",
							newRelationName);
				}
				relationName = newRelationName;
				if (firstRelationName == null) {
					firstRelationName = relationName;
				}
			} else {
				// this means this is a leaf node directly connected to a port
				KEdge edge = connectionTree.commonEdgeSet().iterator().next();
				Port targetPort = _kieler2PtolemyPorts
						.get(edge.getTargetPort());
				Port sourcePort = _kieler2PtolemyPorts
						.get(edge.getSourcePort());
				if (relationName != null) {
					// a relation was created before so connect with that
					this.link("relation", relationName, "port", targetPort
							.getName(_compositeActor));
				} else {
					// direct port to port connection
					// FIXME: why does this create a Vertex with location 0,0 ??
					String dummyRelationName = this.createRelation();
					this.link("port", sourcePort.getName(_compositeActor),
							"relation", dummyRelationName);
					this.link("relation", dummyRelationName, "port", targetPort
							.getName(_compositeActor));
				}
			}
		}
		// also add recursively
		for (HyperedgeConnectionTree subtree : connectionTree.subTreeList()) {
			String firstOfChildren = addRelationswithVertices(subtree);
			// also link last relation of this level with first relation of next
			// level
			this.link("relation1", relationName, "relation2", firstOfChildren);
		}

		// we are a leaf and need to connect to the target port
		if (connectionTree.subTreeList().isEmpty()) {
			KEdge edge = connectionTree.commonEdgeSet().iterator().next();
			Port targetPort = _kieler2PtolemyPorts.get(edge.getTargetPort());
			if (relationName != null) {
				// a relation was created before so connect with that
				this.link("relation", relationName, "port", targetPort
						.getName(_compositeActor));
			}
		}
		return firstRelationName;
	}

	/**
	 * Print some status information about the layout run. I.e. the runtime of
	 * the layout algorithm given by the progress monitor
	 * 
	 * @param kgraph
	 * @param progressMonitor
	 */
	private void printStatus(KNode kgraph,
			IKielerProgressMonitor progressMonitor) {
		System.out.println("KIELER Execution Time: "
				+ (progressMonitor.getExecutionTime() * 1000) + " ms");
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

	/**
	 * Transform a location from a Kieler node from Kieler coordinate system to
	 * ptolemy coordinate system. That is Kieler gives locations to be the upper
	 * left corner of an item and Ptolemy as the center point of the item.
	 * 
	 * @param kshapeLayout
	 *            LAyout of KNode kieler graphical node that contains bounds
	 *            with location and size
	 * @param referenceNode 
	 * @return KPoint in Ptolemy coordinate system
	 */
	private void kNode2Ptolemy(KShapeLayout kshapeLayout, KNode referenceNode) {
		
		NamedObj namedObj = _kieler2ptolemyEntityNodes.get(referenceNode);
		Object divaNode = _kieler2ptolemyDivaNodes.get(referenceNode);
		
		double[] location = this.getLocation(namedObj);
		
		if(namedObj != null && divaNode != null){
			Rectangle2D divaBounds = this.getLayoutTarget().getBounds(divaNode);
			double offsetX = 0, offsetY = 0;
			
			// check if we got a valid location inside the diva bounds
			// if not we might have something that has no location attribute 
			// (e.g. a relation vertex) where we don't need any offset
			if(location.length == 2 && divaBounds.contains(location[0], location[1])){
				offsetX = location[0] - divaBounds.getMinX();
				offsetY = location[1] - divaBounds.getMinY();
			}
			
			kshapeLayout
					.setXpos((float) (kshapeLayout.getXpos() + offsetX));
			kshapeLayout
					.setYpos((float) (kshapeLayout.getYpos() + offsetY));
		}
	}

	/**
	 * Transform a location from a Kieler node from Kieler coordinate system to
	 * ptolemy coordinate system. That is Kieler gives locations to be the upper
	 * left corner of an item and Ptolemy as the center point of the item.
	 * 
	 * @param kshapeLayout
	 *            LAyout of KNode kieler graphical node that contains bounds
	 *            with location and size
	 * @return KPoint in Ptolemy coordinate system
	 */
	private void ptolemy2KNode(KShapeLayout kshapeLayout) {
		kshapeLayout
				.setXpos((float) (kshapeLayout.getXpos() - 0.5 * kshapeLayout
						.getWidth()));
		kshapeLayout
				.setYpos((float) (kshapeLayout.getYpos() - 0.5 * kshapeLayout
						.getHeight()));
	}

	/**
	 * Create a new MoMLChangeRequest to add a new Relation with a Vertex at a
	 * given position. The MoML code is appended to the field MoMLChangeRequest
	 * buffer to buffer multiple such requests. Don't actually execute the
	 * request.
	 * 
	 * @param x
	 *            coordinate of new vertex
	 * @param y
	 *            coordinate of new vertex
	 * @return name of newly created relation
	 */
	private String createRelationWithVertex(double x, double y) {
		String relationName = _compositeActor.uniqueName("relation");
		relationName = this.getUniqueName(relationName);
		String vertexName = _compositeActor.uniqueName("vertex");
		vertexName = this.getUniqueName(vertexName);

		String moml = "<relation name=\"" + relationName
				+ "\" class=\"ptolemy.actor.TypedIORelation\">"
				+ "<vertex name=\"" + vertexName + "\" value=\"{" + x + " , "
				+ y + "}\"></vertex></relation>\n";

		_momlChangeRequest.append(moml);
		return relationName;
	}

	/**
	 * Create a new MoMLChangeRequest to add a new Relation. The MoML code is
	 * appended to the field MoMLChangeRequest buffer to buffer multiple such
	 * requests. Don't actually execute the request.
	 * 
	 * @return name of newly created relation
	 */
	private String createRelation() {
		String relationName = _compositeActor.uniqueName("relation");
		relationName = this.getUniqueName(relationName);

		String moml = "<relation name=\"" + relationName
				+ "\" class=\"ptolemy.actor.TypedIORelation\">"
				+ "</relation>\n";

		_momlChangeRequest.append(moml);
		return relationName;
	}

	/**
	 * Create a new MoMLChangeRequest to add a new link between arbitrary
	 * objects. The MoML code is appended to the field MoMLChangeRequest buffer
	 * to buffer multiple such requests. Don't actually execute the request.
	 * Supported types are given by MoML, e.g. "port", "relation". Connecting
	 * multiple relations requires to add a number, "relation1", "relation2" to
	 * the corresponding type.
	 * 
	 * @param type1
	 *            type of the first item to be linked, e.g. port, relation,
	 *            relation1, relation2
	 * @param name1
	 *            name of the first item to be linked
	 * @param type2
	 *            type of the second item to be linked, e.g. port, relation,
	 *            relation1, relation2
	 * @param name2
	 *            name of the second item to be linked
	 */
	private void link(String type1, String name1, String type2, String name2) {
		String moml = "<link " + type1 + "=\"" + name1 + "\" " + type2 + "=\""
				+ name2 + "\"/>\n";
		_momlChangeRequest.append(moml);
	}

	/**
	 * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it
	 * immediately. The request is addressed to a specific NamedObj in the
	 * Ptolemy model and hence does not get buffered because there is only
	 * exactly one move request per layout run per node.
	 * 
	 * @param obj
	 *            Ptolemy node to be moved
	 * @param x
	 *            new coordinate
	 * @param y
	 *            new coordinate
	 */
	private void setLocation(NamedObj obj, double x, double y) {
		String moml = "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{"
				+ x + "," + y + "}\"></property>\n";
		// need to request a MoML Change for a particular NamedObj and not the
		// top level element
		// so we need multiple requests here
		MoMLChangeRequest request = new MoMLChangeRequest(this, obj, moml
				.toString());
		request.setUndoable(true);
		obj.requestChange(request);
	}

	/**
	 * Create a MoMLChangeRequest to move a Ptolemy model object and schedule it
	 * immediately. The request is addressed to a specific NamedObj in the
	 * Ptolemy model and hence does not get buffered because there is only
	 * exactly one move request per layout run per node.
	 * 
	 * @param obj
	 *            Ptolemy node to be moved
	 * @param x
	 *            new coordinate
	 * @param y
	 *            new coordinate
	 */
	private void setLocation(Vertex vertex, Relation relation, double x,
			double y) {
		String moml = "<vertex name=\"" + vertex.getName() + "\" value=\"[" + x
				+ "," + y + "]\"></vertex>\n";
		// need to request a MoML Change for a particular Vertex
		// and not the
		// top level element
		// so we need multiple requests here
		MoMLChangeRequest request = new MoMLChangeRequest(this, relation, moml
				.toString());
		request.setUndoable(true);
		relation.requestChange(request);
	}

	/**
	 * Create a MoMLChangeRequest to remove a set of relations in a Ptolemy
	 * model object and schedule it immediately. As mixing creation of new
	 * relations and removing of old relation requests in the request buffer
	 * might cause name clashes, this request is not buffered.
	 * 
	 * @param relationSet
	 *            set of relation to be removed from the Ptolemy model
	 */
	private void removeRelations(Set<Relation> relationSet) {
		StringBuffer moml = new StringBuffer();
		for (Relation relation : relationSet) {
			// Delete the relation.
			moml.append("<deleteRelation name=\"" + relation.getName()
					+ "\"/>\n");
		}
		moml.insert(0, "<group>");
		moml.append("</group>");
		MoMLChangeRequest request = new MoMLChangeRequest(this,
				_compositeActor, moml.toString());
		request.setUndoable(true);
		_compositeActor.requestChange(request);
	}

	/**
	 * Set the Ptolemy Model that contains the graph that is to be layouted. The
	 * layouter will require access to the Ptolemy model because the lower level
	 * Diva abstraction does not consider certain properties required by the
	 * Kieler layouter such as port positions.
	 * 
	 * @param model
	 */
	public void setModel(CompositeActor model) {
		this._compositeActor = (CompositeActor) model;
	}

	int _nameSuffix = 0;

	/**
	 * Create a unique name in the model nameset prefixed by the given prefix.
	 * This method actually does not guarantee this yet but tries brute force by
	 * adding some counting String. Problem with the uniqueName() method of
	 * CompositeActors is, that they will return always the same name while the
	 * corresponding objects are not yet added to the model, e.g. by buffering
	 * multiple relation creations in one MoMLChangeRequest which gets executed
	 * later.
	 * 
	 * @param prefix
	 * @return Unique name in the model context
	 */
	private String getUniqueName(String prefix) {
		_nameSuffix++;
		return prefix + "_k_" + _nameSuffix;
	}

	/**
	 * Debug output a KEdge to a String, i.e. will represent all bendpoints in
	 * the String.
	 * 
	 * @param edge
	 * @return
	 */
	private String toString(KEdge edge) {
		StringBuffer string = new StringBuffer();
		string.append("[E:");
		string.append("Source:" + edge.getSource().hashCode());
		string.append(" Target:" + edge.getTarget().hashCode() + " Bends:");
		KEdgeLayout layout = KimlLayoutUtil.getEdgeLayout(edge);
		for (KPoint point : layout.getBendPoints()) {
			string.append(point.getX() + "," + point.getY() + " ");
		}

		string.append("]");
		return string.toString();
	}

	/**
	 * Check whether the given Ptolemy model object has any connections, i.e. is
	 * connected to any other components via some link.
	 * 
	 * @param namedObj
	 * @return
	 */
	private boolean hasConnections(NamedObj namedObj) {
		if (namedObj instanceof Attribute)
			return false;
		if (namedObj instanceof Actor) {
			Actor actor = (Actor) namedObj;
			List<Port> ports = new ArrayList<Port>();
			ports.addAll(actor.inputPortList());
			ports.addAll(actor.outputPortList());
			for (Port port : ports) {
				// if any port of an actor is conencted to any other
				// assume that there is also no visible connection
				if (!port.connectedPortList().isEmpty())
					return true;
			}
			return false;
		}
		// default to false
		return false;
	}

	private KNode createKNodeForVertex(Vertex vertex) {
		KNode knode = KimlLayoutUtil.createInitializedNode();
		// simulate vertex by node with size 0
		KShapeLayout layout = KimlLayoutUtil.getShapeLayout(knode);
		layout.setHeight(1);
		layout.setWidth(1);
		LayoutOptions.setFixedSize(layout);
		// as Kieler so far only suport nodes WITH port constraints,
		// add dummy ports
		KPort kinputport = KimlLayoutUtil.createInitializedPort();
		KShapeLayout portLayout = KimlLayoutUtil.getShapeLayout(knode);
		portLayout.setHeight(0);
		portLayout.setWidth(0);
		portLayout.setXpos(0);
		portLayout.setYpos(0);
		kinputport.setType(KPortType.INPUT);
		kinputport.setNode(knode);
		KPort koutputport = KimlLayoutUtil.createInitializedPort();
		portLayout = KimlLayoutUtil.getShapeLayout(knode);
		portLayout.setHeight(0);
		portLayout.setWidth(0);
		portLayout.setXpos(0);
		portLayout.setYpos(0);
		koutputport.setType(KPortType.OUTPUT);
		koutputport.setNode(knode);
		return knode;
	}

	private KPort getPort(Object ptolemyObject, KPortType type) {
		if (ptolemyObject instanceof Vertex) {
			KNode knode = _ptolemy2KielerNodes.get(ptolemyObject);
			for (KPort port : knode.getPorts()) {
				if (port.getType().equals(type))
					return port;
			}
		} else if (ptolemyObject instanceof Port) {
			return _ptolemy2KielerPorts.get(ptolemyObject);
		}
		return null;
	}

	private String toString(KNode knode) {
		return toString(knode, 0);
	}

	private String toString(KNode knode, int level) {
		StringBuffer buffer = new StringBuffer();
		KShapeLayout layout = KimlLayoutUtil.getShapeLayout(knode);
		buffer.append("Node: X" + layout.getXpos() + ",Y" + layout.getYpos()
				+ ",W" + layout.getWidth() + ",H" + layout.getHeight()
				+ " Hash:" + knode.hashCode() + "\n");
		List<KPort> ports = knode.getPorts();
		for (KPort port : ports) {
			buffer.append("      Port: " + port.getType() + port.hashCode()
					+ "\n");
		}
		List<KEdge> edges = knode.getOutgoingEdges();
		for (KEdge edge : edges) {
			buffer.append(toString(edge) + "\n");
		}
		List<KNode> children = knode.getChildren();
		for (KNode node : children) {
			buffer.append(+level + " " + toString(node, level + 1));
		}
		return buffer.toString();
	}
	
	private double[] getLocation(NamedObj namedObj){
		Attribute locationAttribute = namedObj.getAttribute("_location");
		double[] location = {0,0};
		if(locationAttribute != null && locationAttribute instanceof Location){
			location = ((Location)locationAttribute).getLocation();
		}
		return location;
	}
}
