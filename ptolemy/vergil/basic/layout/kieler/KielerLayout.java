/* Layout interface between the Ptolemy Diva editor and the KIELER 
 * layout library.*/
/*
@Copyright (c) 2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.vergil.basic.layout.kieler;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.graph.GraphInvalidStateException;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.IOPortController;
import ptolemy.vergil.actor.PortTerminal;
import de.cau.cs.kieler.core.KielerException;
import de.cau.cs.kieler.core.alg.BasicProgressMonitor;
import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.kgraph.KPortType;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.layout.options.LayoutDirection;
import de.cau.cs.kieler.kiml.layout.options.LayoutOptions;
import de.cau.cs.kieler.kiml.layout.options.PortConstraints;
import de.cau.cs.kieler.kiml.layout.options.PortSide;
import de.cau.cs.kieler.kiml.layout.util.BoxLayoutProvider;
import de.cau.cs.kieler.kiml.layout.util.KimlLayoutUtil;
import de.cau.cs.kieler.klodd.hierarchical.HierarchicalDataflowLayoutProvider;
import diva.canvas.CanvasComponent;
import diva.canvas.CompositeFigure;
import diva.graph.GraphModel;
import diva.graph.layout.AbstractGlobalLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.modular.EdgeModel;

//////////////////////////////////////////////////////////////////////////
////KielerLayout
/**
 * Ptolemy Layouter that uses the KIELER layout algorithm from an external
 * library to layout a given ptolemy model.
 * <p>
 * See http://www.informatik.uni-kiel.de/rtsys/kieler/ for more information
 * about KIELER.
 * <p>
 * KIELER - Kiel Integrated Environment for Layout for the Eclipse
 * RichClientPlatform
 * <p>
 * The KIELER project tries to enhance graphical modeling pragmatics. Next to
 * higher level solutions (meta layout, structure based editing...) developed
 * for Eclipse models, it also implements custom layout algorithms.
 * <p>
 * This class interfaces a standalone KIELER layout algorithm for actor oriented
 * port based graphical diagrams with a Ptolemy diagram.
 * <p>
 * While KIELER is mainly developed for an Eclipse environment, most algorithms
 * are also available standalone and can be used in a non Eclipse environment.
 * This class is a try to leverage this to apply KIELER algorithms with Ptolemy.
 * No Eclipse is required with that. Only one standalone external library.
 * <p>
 * Calling the layout() method will create a new Kieler graph datastructure, run
 * Kieler layout algorithms on it and augment it with resulting layout
 * information (locations and sizes of nodes, bendoints of connections). Then
 * this layout gets applied to the Ptolemy model. Moving of nodes in Ptolemy is
 * done via adding or changing location attributes.
 * <p>
 * Setting bendpoints is not yet supported in Ptolemy. Hence the resulting
 * layout in Ptolemy might show some improvable issues. Ptolemy's built-in
 * connection routing does not consider obstacle avoidance, hence overlappings
 * with other nodes and connections might appear. However the KIELER layouter
 * calculates collision free bendpoints which cannot yet be applied to the
 * diagram. This is a point for future improvements.
 * <p>
 * It uses location attributes of actors and attributes to place items and
 * corresponding for relation vertices.
 * 
 * @author Hauke Fuhrmann, <haf@informatik.uni-kiel.de>
 */
public class KielerLayout extends AbstractGlobalLayout {

    /**
     * Construct an instance taking a LayoutTarget for specifying some methods
     * for layout handling as given by the standard Ptolemy
     * AbstractGlobalLayout. The KielerLayout will need access to the top level
     * Ptolemy model, so either use corresponding constructor or call setModel()
     * prior to layout invocation.
     * 
     * @param target
     *            the LayoutTarget on which layout will be performed
     */
    public KielerLayout(LayoutTarget target) {
        super(target);
    }

    /**
     * Construct an instance setting the LayoutTarget as requested by the
     * AbstractGlobalLayout and the containing Ptolemy model. Preferred
     * constructor.
     * 
     * @param target
     * @param ptolemyContainer
     */
    public KielerLayout(LayoutTarget target, CompositeActor ptolemyContainer) {
        super(target);
        this.setModel(ptolemyContainer);
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Layout the given composite. Main entry point for the layout action.
     * Create a Kieler KGraph datastructure corresponding to the Ptolemy model,
     * instanciate a Kieler layout algorithm (AbstractLayoutProvider) and run
     * its doLayout() method on the KGraph. The KGraph gets augmented with
     * layout information (position and sizes of objects and bendpoints for
     * connections). This information is then reapplied to the ptolemy model by
     * stating MoMLChangeRequests with location attributes for nodes. So far
     * setting connection bendpoints in Ptolemy is not supported. Hence the
     * bendpoint information of KIELER is discarded, which may result in
     * suboptimal results as the Ptolemy connection router does not consider
     * obstacle avoidance.
     * 
     * @param composite
     *            the container of the diagram in terms of an GraphModel.
     */
    @Override
    public void layout(Object composite) {
        _ptolemyModelUtil = new PtolemyModelUtil();

        if (_doApplyEdgeLayout) {
            if (_debug) {
                _time = System.currentTimeMillis();
                System.out.print("Removing unnecessary relation vertices... ");
            }

            PtolemyModelUtil._removeUnnecessaryRelations(this._compositeActor);

            if (_debug) {
                System.out.println("done in "
                        + (System.currentTimeMillis() - _time) + "ms");
            }
        }
        
        if(_debug){
            _time = System.currentTimeMillis();
            System.out.print("Creating Kieler KGraph from Ptolemy model... ");
        }
        
        // create a Kieler Graph
        // create one node that will be used for the Hierarchical layout
        // algorithm. This will contain all items with connections
        KNode hierarchicalLayoutNode = KimlLayoutUtil.createInitializedNode();
        // create one node that will be layouted with a simple box layout
        // not considering connections. This will be used for all unconnected
        // nodes such as the director, text attributes and parameters
        KNode boxLayoutNode = KimlLayoutUtil.createInitializedNode();
        // make the hierarchichal node a child of the box node so that
        // the unconnected nodes will be placed around the other ones
        hierarchicalLayoutNode.setParent(boxLayoutNode);
        KShapeLayout layout = KimlLayoutUtil
                .getShapeLayout(hierarchicalLayoutNode);
        LayoutOptions.setLayoutDirection(layout, LayoutDirection.HORIZONTAL);
        LayoutOptions.setMinSpacing(layout, MIN_SPACING);

        // now read ptolemy model and fill the two subgraphs with the model
        // infos
        _createGraph(composite, hierarchicalLayoutNode, boxLayoutNode);

        if (_debug) {
            System.out.println("done in "
                    + (System.currentTimeMillis() - _time) + "ms");
            _time = System.currentTimeMillis();
            System.out.print("Performing layout... ");
        }

        // create the layout providers which contains the actual layout
        // algorithm
        HierarchicalDataflowLayoutProvider hierarchicalLayoutProvider = new HierarchicalDataflowLayoutProvider();
        BoxLayoutProvider boxLayoutProvider = new BoxLayoutProvider();

        // create a progress monitor
        IKielerProgressMonitor progressMonitor = new BasicProgressMonitor();

        try {
            // perform layout on the created graph
            hierarchicalLayoutProvider.doLayout(hierarchicalLayoutNode,
                    progressMonitor.subTask(10));
            // set initial position as the bounding box of the hierarchical node
            KPoint offset = KielerGraphUtil._getUpperLeftCorner(hierarchicalLayoutNode);

            if(_debug){
                System.out.println("Bounding Box coordinates: "+layout.getXpos()+" "+layout.getYpos());
                System.out.println("Hierarchical offset:      "+offset.getX()+" "+offset.getY());
            }
            
            layout.setXpos(layout.getXpos() - offset.getX());
            layout.setYpos(layout.getYpos() - offset.getY());
            if (_doBoxLayout) {
                boxLayoutProvider.doLayout(boxLayoutNode, progressMonitor
                        .subTask(10));
            }

            // write to XML file for debugging layout
            // writing to file requires XMI resource factory
            if (_debug) {
                System.out.println("done in "
                        + (System.currentTimeMillis() - _time) + "ms");
                KielerGraphUtil._writeToFile(boxLayoutNode);
                _time = System.currentTimeMillis();
                System.out.print("Applying layout to Ptolemy diagram... ");
            }

            // apply layout to ptolemy model. Will do so
            // recursively for all containing nodes (e.g. especially
            // for the hierarchical layout node and its contents)
            _applyLayout(hierarchicalLayoutNode);
            // _applyLayout(boxLayoutNode);

            if (_debug) {
                System.out.println("done in "
                        + (System.currentTimeMillis() - _time) + "ms");
            }

            // PtolemyModelUtil._showUnnecessaryRelations(this._compositeActor,
            // true);

        } catch (KielerException e) {
            // throw some Ptolemy runtime exception for a Kieler exception
            throw new GraphInvalidStateException(e,
                    "KIELER runtime exception: " + e.getMessage());
        } catch (IllegalActionException e){
            // throw some Ptolemy runtime exception
            throw new GraphInvalidStateException(e,
                    "KIELER runtime exception: " + e.getMessage());
        }

    }

    public void setApplyEdgeLayout(boolean flag) {
        this._doApplyEdgeLayout = flag;
    }
    
    public void setBoxLayout(boolean flag) {
        this._doBoxLayout = flag;
    }

    /**
     * Set the Ptolemy Model that contains the graph that is to be layouted. The
     * layouter will require access to the Ptolemy model because the lower level
     * Diva abstraction does not consider certain properties required by the
     * Kieler layouter such as port positions.
     * 
     * @param model
     *            The parent composite actor which internal diagram shall be
     *            layouted.
     */
    public void setModel(CompositeActor model) {
        this._compositeActor = model;
    }

    private void _applyEdgeLayout(KEdge kedge) throws IllegalActionException {
        int count = 0;
        String previousRelation = null;
        Relation oldRelation = (Relation) this.getLayoutTarget()
                .getGraphModel().getSemanticObject(
                        _kieler2PtolemyDivaEdges.get(kedge));
        List<KPoint> bendPoints = KimlLayoutUtil.getEdgeLayout(kedge)
                .getBendPoints();
        for (KPoint relativeKPoint : bendPoints) {
            KPoint kpoint = KielerGraphUtil._getAbsoluteKPoint(relativeKPoint,
                    KielerGraphUtil._getParent(kedge));
            
            // create new relation
            String relationName = _ptolemyModelUtil._createRelationWithVertex(
                    _compositeActor.uniqueName("relation"), kpoint.getX(),
                    kpoint.getY());
            _ptolemyModelUtil._performChangeRequest(_compositeActor);
            Relation newRelation = _compositeActor.getRelation(relationName);
            
            // we process the first bendpoint
            if (count == 0) { 
                KPort kSourcePort = kedge.getSourcePort();
                KNode kNode = kedge.getSource();

                _replaceRelation(kSourcePort, kNode, newRelation, oldRelation);
                _ptolemyModelUtil._performChangeRequest(_compositeActor);
            }
            
            // process all other bendpoints
            else {
                if (previousRelation != null)
                    _ptolemyModelUtil._link("relation1", previousRelation,
                            "relation2", relationName);
                _ptolemyModelUtil._performChangeRequest(_compositeActor);
            }
            previousRelation = relationName;
            count++;
        }
        
        // last, process target
        if (previousRelation != null) {
            KPort kTargetPort = kedge.getTargetPort();
            KNode kNode = kedge.getTarget();

            Relation previousRelationRelation = _compositeActor.getRelation(previousRelation);
            _replaceRelation(kTargetPort, kNode, previousRelationRelation, oldRelation);
            _ptolemyModelUtil._performChangeRequest(_compositeActor);
        }
        // remove old relation if it is no longer connected
        if (oldRelation.linkedObjectsList().isEmpty()) {
            _ptolemyModelUtil._removeRelation(oldRelation, _compositeActor);
        }
    }

    /**
     * Traverses a composite KNode containing corresponding Kieler nodes, ports
     * and edges for the Ptolemy model and applies all layout information
     * contained by it back to the Ptolemy model. All changes to the Ptolemy
     * model are done via MoMLChangeRequests. Location attributes for all
     * visible Ptolemy nodes are set. So far Ptolemy does not support setting of
     * connection bendpoints explicitly. So this information is discarded which
     * might result in suboptimal appearance. The Ptolemy connection router does
     * not consider obstruction avoidance so there are likely to be connection
     * overlappings in the diagram.
     * 
     * @param kgraph
     *            The Kieler graph object containing all layout information to
     *            apply to the Ptolemy model
     * @throws IllegalActionException 
     */
    private void _applyLayout(KNode kgraph) throws IllegalActionException {
        //long time = System.currentTimeMillis();
        
        // init required classes
        GraphModel graph = this.getLayoutTarget().getGraphModel();
        if (graph instanceof ActorGraphModel) {
            // apply node layout
            Collection<KNode> kNodes = kgraph.getChildren();
            if (_doBoxLayout)
                kNodes = _kieler2ptolemyEntityNodes.keySet();

            for (KNode knode : kNodes) {
                KShapeLayout absoluteLayout = KielerGraphUtil
                        ._getAbsoluteLayout(knode);
                NamedObj namedObj = _kieler2ptolemyEntityNodes.get(knode);
                // transform koordinate systems
                _kNode2Ptolemy(absoluteLayout, knode);
                if (namedObj instanceof Relation) {
                    Vertex vertex = (Vertex) _kieler2ptolemyDivaNodes
                            .get(knode);
                    PtolemyModelUtil._setLocation(vertex, (Relation) namedObj,
                            absoluteLayout.getXpos(), absoluteLayout.getYpos());
                } else {
                    PtolemyModelUtil._setLocation(namedObj, absoluteLayout
                            .getXpos(), absoluteLayout.getYpos());
                }
            }
            
            //System.out.println("Nodes apply: "+(System.currentTimeMillis()-time));
            //time = System.currentTimeMillis();
            
            // apply edge layout
            if (_doApplyEdgeLayout) {
                for (KEdge kedge : _kieler2PtolemyDivaEdges.keySet()) {
                    _applyEdgeLayout(kedge);
                    //System.out.println("Edge apply: "+(System.currentTimeMillis()-time));
                    //time = System.currentTimeMillis();
                }
            }
        }
        // create change request and fire it
        _ptolemyModelUtil._performChangeRequest(_compositeActor);
    }

    /**
     * Creates a graph for the KIELER API from a ptolemy model. Will traverse
     * the low level GraphModel given by the composite and record all found
     * elements in the mapping fields of this object that keep a mapping between
     * Ptolemy/Diva objects and Kieler objects. New Kieler objects (KEdge,
     * KNode, KPort) get created for their respective Ptolemy counterparts and
     * initialized with the initial sizes and positions and are put in a
     * composite KNode (the graph Kieler will perform the layout on later). To
     * obtain the right mappings, multiple abstraction levels of Ptolemy are
     * considered here: Diva, as this was the intended original way to do
     * automatic layout (e.g. by GlobalAbstractLayout) and Ptolemy, as Diva
     * lacks certain concepts that are relevant for a proper layout, as for
     * example exact port locations for considering port constraints in the
     * model, supported by Kieler.
     * 
     * @param composite
     *            the GraphModel composite object to retrieve the model
     *            information from
     * @param boxLayoutNode
     *            Kieler subgraph to receive all unconnected model elements
     * @param hierarchicalLayoutNode
     *            Kieler subgraph to receive all connected model elements
     */
    private void _createGraph(Object composite, KNode hierarchicalLayoutNode,
            KNode boxLayoutNode) {
        _ptolemy2KielerNodes = new HashMap<Object, KNode>();
        _kieler2ptolemyDivaNodes = new HashMap<KNode, Object>();
        _kieler2ptolemyEntityNodes = new HashMap<KNode, NamedObj>();
        _ptolemyDiva2KielerEdges = new HashMap<Object, KEdge>();
        _kieler2PtolemyDivaEdges = new HashMap<KEdge, Object>();
        _ptolemy2KielerPorts = new HashMap<Port, List<KPort>>();
        _kieler2PtolemyPorts = new HashMap<KPort, Port>();
        _divaEdgeSource = new HashMap<Object, Object>();
        _divaEdgeTarget = new HashMap<Object, Object>();

        // on-the-fly find upper left corner for bounding box of parent node
        float globalX = Float.MAX_VALUE, globalY = Float.MAX_VALUE;

        // traverse ptolemy graph
        LayoutTarget target = this.getLayoutTarget();
        GraphModel graph = target.getGraphModel();
        if (graph instanceof ActorGraphModel) {
            ActorGraphModel aGraph = (ActorGraphModel) graph;

            // process nodes
            for (Iterator iterator = aGraph.nodes(composite); iterator
                    .hasNext();) {
                Object node = iterator.next();

                // here we get the corresponding Ptolemy object
                // this breaks with Ptolemy/Diva abstraction
                // for now we need the ptolemy Actor to get the ports and port
                // positions
                // and to distinguish Actors and Relation vertices
                Object semanticNode = aGraph.getSemanticObject(node);

                // handle actors, text and directors
                if (semanticNode instanceof Actor
                        || semanticNode instanceof Attribute) {

                    // for a ptolemy node create a Kieler KNode and
                    // put it in either the parents for hierarchichal
                    // or box layout depending on whether it is
                    // connected to something or not
                    KNode knode = _createKNode(node, semanticNode);

                    // store node in the correct composite node depending on
                    // whether it has connections or not
                    if (PtolemyModelUtil._isConnected((NamedObj) semanticNode)) {
                        knode.setParent(hierarchicalLayoutNode);
                        // get check bounds for global bounding box
                        KShapeLayout layout = KimlLayoutUtil
                                .getShapeLayout(knode);
                        if (layout.getXpos() < globalX)
                            globalX = layout.getXpos();
                        if (layout.getYpos() < globalY)
                            globalY = layout.getYpos();
                    } else {
                        knode.setParent(boxLayoutNode);
                    }

                    // handle ports
                    if (semanticNode instanceof Actor
                            && semanticNode instanceof Entity) {
                        Actor actor = (Actor) semanticNode;
                        List<Port> inputs = actor.inputPortList();
                        List<Port> outputs = actor.outputPortList();

                        // create ports
                        _createKPorts(knode, inputs, KPortType.INPUT);
                        _createKPorts(knode, outputs, KPortType.OUTPUT);
                    }
                }

                // handle relation vertices
                else if (semanticNode instanceof Relation) {
                    // regard a relation vertex as a Kieler KNode
                    KNode kVertexNode = _createKNodeForVertex((Vertex) node);
                    // add it to the graph
                    kVertexNode.setParent(hierarchicalLayoutNode);
                    
                    // get check bounds for global bounding box
                    KShapeLayout layout = KimlLayoutUtil
                            .getShapeLayout(kVertexNode);
                    if (layout.getXpos() < globalX)
                        globalX = layout.getXpos();
                    if (layout.getYpos() < globalY)
                        globalY = layout.getYpos();

                    
                    // store it in the maps
                    _ptolemy2KielerNodes.put(node, kVertexNode);
                    _kieler2ptolemyDivaNodes.put(kVertexNode, node);
                    _kieler2ptolemyEntityNodes.put(kVertexNode,
                            (NamedObj) semanticNode);
                }

                else if (semanticNode instanceof Port) {
                    KNode kPortNode = _createKNodeForPort((Port) semanticNode);
                    // add it to the graph
                    kPortNode.setParent(hierarchicalLayoutNode);
                    // setup Kieler ports for the KNode of the internal Ptolemy
                    // port
                    // _createKPort(kPortNode, (Port)semanticNode);
                    
                 // get check bounds for global bounding box
                    KShapeLayout layout = KimlLayoutUtil
                            .getShapeLayout(kPortNode);
                    if (layout.getXpos() < globalX)
                        globalX = layout.getXpos();
                    if (layout.getYpos() < globalY)
                        globalY = layout.getYpos();
                    
                    // store it in the maps
                    _ptolemy2KielerNodes.put(node, kPortNode);
                    _kieler2ptolemyDivaNodes.put(kPortNode, node);
                    _kieler2ptolemyEntityNodes.put(kPortNode,
                            (NamedObj) semanticNode);
                }

                // check if the node has ports
                Iterator portIter = null;
                List portList = new ArrayList();
                if (semanticNode instanceof Relation) {
                    // if object is a relation vertex, it is itself kinda port
                    portList.add(node);
                    portIter = portList.iterator();
                } else if (semanticNode instanceof Actor) {
                    portIter = aGraph.nodes(node);
                } else if(semanticNode instanceof Port){ // internal ports
                    portList.add(node);
                    portIter = portList.iterator();
                }
                if (portIter != null) {
                    for (; portIter.hasNext();) {
                        Object divaPort = portIter.next();
                        // iterate all outgoing edges
                        Iterator edgeIterator = aGraph.outEdges(divaPort);
                        if(semanticNode instanceof Port){ // internal ports
                            edgeIterator = aGraph.getExternalPortModel().outEdges(node);
                        }
                        for ( ;edgeIterator.hasNext();) {
                            Object divaEdge = edgeIterator.next();
                            // store Diva edge in corresponding map with no
                            // KEdge that will be created later
                            _ptolemyDiva2KielerEdges.put(divaEdge, null);
                        }
                    }
                }
            }

            // create Kieler KEdges for Diva edges
            _storeEndpoints();
            for (Object divaEdge : _ptolemyDiva2KielerEdges.keySet()) {
                _createKEdge(divaEdge);
            }
        }

        // set Bounding Box
        KShapeLayout layout = KimlLayoutUtil
                .getShapeLayout(hierarchicalLayoutNode);
        layout.setXpos(globalX);
        layout.setYpos(globalY);
    }

    /**
     * Create a Kieler KEdge for a Ptolemy Diva edge object. The KEdge will be
     * setup between either two ports or relation vertices or mixed. Hence the
     * KEdge corresponds more likely a Ptolemy link than a relation. Diva edges
     * have no direction related to the flow of data in Ptolemy. However, Kieler
     * uses a directed graph to perform layout and so a meaningful direction
     * should be set in the KEdge. This direction will be approximated by doing
     * a tree search beginning on both end points of the diva edge. Whenever
     * either of the endpoints is connected to a source port, this will be the
     * source of the KEdge and determine its direction.
     * 
     * The newly created edge is stored with the corresponding diva edge in the
     * global maps _ptolemyDiva2KielerEdges, _kieler2PtolemyDivaEdges, such that
     * the {@link #_applyLayout(KNode)} method will be able to reapply the
     * layout.
     * 
     * @param divaEdge
     *            The Ptolemy diva edge object for which to create a new KEdge.
     */
    private void _createKEdge(Object divaEdge) {
        GraphModel model = this.getLayoutTarget().getGraphModel();
        if (model instanceof ActorGraphModel) {
            ActorGraphModel aGraph = (ActorGraphModel) model;

            Object semObj = aGraph.getSemanticObject(divaEdge);
            Relation rel = null;
            if (semObj instanceof Relation) {
                rel = (Relation) semObj;
            }

            KEdge kedge = KimlLayoutUtil.createInitializedEdge();

            Object source = _divaEdgeSource.get(divaEdge);
            Object target = _divaEdgeTarget.get(divaEdge);

            KPort kSourcePort = this._getPort(source, KPortType.OUTPUT, rel);
            if (kSourcePort != null) {
                kedge.setSourcePort(kSourcePort);
                kSourcePort.getEdges().add(kedge);
                kedge.setSource(kSourcePort.getNode());
            } else {
                // edge is not connected to a port
                kedge.setSource(_ptolemy2KielerNodes.get(source));
            }
            KPort kTargetPort = this._getPort(target, KPortType.INPUT, rel);
            if (kTargetPort != null) {
                kedge.setTargetPort(kTargetPort);
                kTargetPort.getEdges().add(kedge);
                kedge.setTarget(kTargetPort.getNode());
            } else {
                // edge is not connected to a port
                kedge.setTarget(_ptolemy2KielerNodes.get(target));
            }

            // add KEdge to map
            _ptolemyDiva2KielerEdges.put(divaEdge, kedge);
            _kieler2PtolemyDivaEdges.put(kedge, divaEdge);
        }
    }

    /**
     * Create a new Kieler KNode corresponding to a Ptolemy diva node and its
     * Ptolemy semantic object (e.g. an Actor).
     * 
     * The newly created node is stored with the corresponding diva and ptolemy
     * nodes in the global maps _ptolemy2KielerNodes, _kieler2ptolemyDivaNodes,
     * _kieler2ptolemyEntityNodes, such that * the {@link #_applyLayout(KNode)}
     * method will be able to reapply the layout.
     * 
     * @param node
     *            The Diva node object.
     * @param semanticNode
     *            The corresponding Ptolemy semantic object, e.g. an Actor or
     *            TextAttribute
     * @return The initialized Kieler KNode
     */
    private KNode _createKNode(Object node, Object semanticNode) {

        String name = "";
        if (semanticNode instanceof NamedObj) {
            name = ((NamedObj) semanticNode).getDisplayName();
        }

        // create new node in KIELER graph and apply the initial
        // size and position
        Rectangle2D bounds = this.getLayoutTarget().getBounds(node);
        KNode knode = KimlLayoutUtil.createInitializedNode();
        knode.getLabel().setText(name);
        // KLabel label = KimlLayoutUtil.createInitializedLabel(knode);
        // label.setText(name);
        KShapeLayout klayout = KimlLayoutUtil.getShapeLayout(knode);
        klayout.setHeight((float) bounds.getHeight());
        klayout.setWidth((float) bounds.getWidth());
        klayout.setXpos((float) bounds.getMinX());
        klayout.setYpos((float) bounds.getMinY());
        // transform coordinates
        //_ptolemy2KNode(klayout);
        LayoutOptions.setFixedSize(klayout, true);
        LayoutOptions.setPortConstraints(klayout, PortConstraints.FIXED_POS);

        // draw the director always as first element
        if (semanticNode instanceof Director) {
            BoxLayoutProvider.setPriority(klayout, 1);
        }

        // store node for later applying layout back
        _ptolemy2KielerNodes.put(node, knode);
        _kieler2ptolemyDivaNodes.put(knode, node);
        _kieler2ptolemyEntityNodes.put(knode, (NamedObj) semanticNode);
        return knode;
    }

    /**
     * Create a Kieler KNode for a Ptolemy inner port. That is the graphical
     * representation for a port of a CompositeActor if you see the contents of
     * this CompositeActor. It is represented by a node where the connection may
     * touch the node corresponding to its type (input, output, both) on the
     * right, left or top.
     * 
     * For now this results a crude approximation of the node, because the
     * figure of the original Ptolemy port cannot be obtained by the layout
     * target. Hence we cannot ask the port for its original bounds.
     * 
     * @param node
     *            the Ptolemy inner port.
     * @return A new Kieler KNode corresponding to the Ptolemy inner port.
     */
    private KNode _createKNodeForPort(Port node) {
        KNode knode = KimlLayoutUtil.createInitializedNode();
        KShapeLayout layout = KimlLayoutUtil.getShapeLayout(knode);
        // Rectangle2D bounds = this.getLayoutTarget().getBounds(node);
        // Object object = this.getLayoutTarget().getVisualObject(node);
        // FIXME: do not set the size to a constant! Set it to the actual size
        // of the port! Problem: Obtaining the inner port's figure from
        // the layout target alway results null. Hence we cannot ask
        // the port figure for its bounds.
        layout.setHeight(DEFAULT_INNER_PORT_HEIGHT);
        layout.setWidth(DEFAULT_INNER_PORT_WIDTH);
        LayoutOptions.setFixedSize(layout, true);
        return knode;
    }

    /**
     * Create a Kieler KNode for a Ptolemy Vertex. Vertices of Ptolemy can be
     * handles as usual KNodes in Kieler (an alternative would be to handle them
     * as connection bendpoints). As Kieler does not support KNodes without port
     * constraints (as in usual graphs without ports), the corresponding KNode
     * will contain one input port and one output port. Size of the node and
     * positions of the ports are all set to zero.
     * 
     * @param vertex
     *            The Ptolemy vertex for which to create a KNode
     * @return An initialized KNode with one input and one output port
     */
    private KNode _createKNodeForVertex(Vertex vertex) {
        KNode kNode = KimlLayoutUtil.createInitializedNode();
        // simulate vertex by node with size 0
        KShapeLayout layout = KimlLayoutUtil.getShapeLayout(kNode);
        layout.setHeight(1);
        layout.setWidth(1);
        layout.setXpos((float)vertex.getLocation()[0]);
        layout.setYpos((float)vertex.getLocation()[1]);
        LayoutOptions.setFixedSize(layout, true);
        // as Kieler so far only suport nodes WITH port constraints,
        // add dummy ports
        KPort kInputPort = KimlLayoutUtil.createInitializedPort();
        KShapeLayout portLayout = KimlLayoutUtil.getShapeLayout(kInputPort);
        portLayout.setHeight(0);
        portLayout.setWidth(0);
        portLayout.setXpos(0);
        portLayout.setYpos(0);
        kInputPort.setType(KPortType.INPUT);
        kInputPort.setNode(kNode);
        KPort kOutputPort = KimlLayoutUtil.createInitializedPort();
        portLayout = KimlLayoutUtil.getShapeLayout(kOutputPort);
        portLayout.setHeight(0);
        portLayout.setWidth(0);
        portLayout.setXpos(0);
        portLayout.setYpos(0);
        kOutputPort.setType(KPortType.OUTPUT);
        kOutputPort.setNode(kNode);
        return kNode;
    }

    /**
     * Create a Kieler KPort corresponding to a Ptolemy Port. Set the size and
     * position (relative to parent) and the direction of the port in the KPort
     * layout information. As Kieler does not explicitly support multiports as
     * Ptolemy, this gets emulated by creating multiple distinct ports with a
     * little offset each. Create only one node. For multiports call this method
     * multiple times with changed parameters.
     * 
     * The newly created port is stored with the corresponding ptolemy port in
     * the global maps _kieler2PtolemyPorts, _ptolemy2KielerPorts, such that the
     * {@link #_applyLayout(KNode)} method will be able to reapply the layout.
     * 
     * @param knode
     *            The parent KNode of the new port
     * @param portType
     *            The port Type, either input or output
     * @param port
     *            The corresponding Ptolemy port (might be a multiport)
     * @param rank
     *            The rank of the new port which is an ordering index. If this
     *            is not set, Kieler will try to infer the ranks automatically
     *            from the port's position.
     * @param index
     *            Index of the KPort corresponding to a multiport
     * @param maxIndex
     *            Width of the multiport, i.e. the number of connected edges to
     *            that port.
     * @param size
     *            Custom size (same for width and height) for a port that will
     *            be used instead of the real Ptolemy port size. If this value
     *            is negative, the original Ptolemy sizes are used.
     */
    private void _createKPort(KNode knode, KPortType portType, Port port,
            int rank, int index, int maxIndex, float size) {
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
        // set a rank if valid
        if (rank != NO_RANK) {
            LayoutOptions.setPortRank(kportlayout, rank);
        }

        // set port side and calc actual offset
        float offsetX = 0, offsetY = 0;
        int direction = IOPortController.getDirection(IOPortController
                .getCardinality(port));
        switch (direction) {
        case SwingConstants.NORTH:
            LayoutOptions.setPortSide(kportlayout, PortSide.NORTH);
            // ports are extended to left with leftmost port index 0
            offsetX = -((maxIndex - index) * MULTIPORT_OFFSET);
            break;
        case SwingConstants.EAST:
            LayoutOptions.setPortSide(kportlayout, PortSide.EAST);
            // ports are extended to bottom with top port index 0
            offsetY = index * MULTIPORT_OFFSET;
            break;
        case SwingConstants.SOUTH:
            LayoutOptions.setPortSide(kportlayout, PortSide.SOUTH);
            offsetX = -(index * MULTIPORT_OFFSET);
            break;
        default:
            LayoutOptions.setPortSide(kportlayout, PortSide.WEST);
            // ports are extended to top beginning with top port index 0
            offsetY = -((maxIndex - index) * MULTIPORT_OFFSET);
            if (maxIndex > 0 && index == maxIndex)
                offsetY = 1;
            break;
        }

        // try to set actual layout (size and position)
        Object portObject = this.getLayoutTarget().getVisualObject(port);
        if (portObject instanceof PortTerminal) {
            // get visual Diva figure of port
            PortTerminal portFigure = (PortTerminal) portObject;
            // get bounds of the port figure
            // (= relative to center of actor symbol here given by
            // referenceLocation)
            Rectangle2D portBounds = portFigure.getBounds();
            // get the parent Diva figure which is the whole composite
            // consisting of
            // the actor icon and its name which might be arbitrary large
            CanvasComponent parent = portFigure.getParent();
            if (parent instanceof CompositeFigure) {
                CompositeFigure parentFigure = (CompositeFigure) parent;

                AffineTransform parentTransform = parentFigure
                        .getTransformContext().getTransform();
                Point2D.Double portLocation = new Point2D.Double(portBounds
                        .getMinX(), portBounds.getMinY());
                Point2D.Double transformedLocation = new Point2D.Double();
                // portTransform.transform(portLocation, transformedLocation);
                parentTransform.transform(portLocation, transformedLocation);
                // calculate coordinates relative to the Kieler nodes top left
                // from absolutes
                double w = portBounds.getWidth();
                double h = portBounds.getHeight();
                double x = transformedLocation.getX()
                        - parentFigure.getBounds().getMinX() + offsetX;
                double y = transformedLocation.getY()
                        - parentFigure.getBounds().getMinY() + offsetY;
                kportlayout.setXpos((float) x);
                kportlayout.setYpos((float) y);
                // no valid size given -> use diagram port size
                if (size < 0) {
                    kportlayout.setWidth((float) w);
                    kportlayout.setHeight((float) h);
                } else {
                    // if we want to use some custom port size, the new
                    // coordinates
                    // need to be adapted to the new size
                    Rectangle2D newPortBounds = new Rectangle2D.Double();
                    newPortBounds.setRect(x, y, w, h);
                    Rectangle2D shrunkPortBounds = new Rectangle2D.Double();
                    shrunkPortBounds.setRect(x, y, size, size);
                    Point2D shrunkenLocation = KielerGraphUtil
                            ._shrinkCoordinates(newPortBounds,
                                    shrunkPortBounds, direction);
                    kportlayout.setXpos((float) shrunkenLocation.getX());
                    kportlayout.setYpos((float) shrunkenLocation.getY());
                    kportlayout.setWidth(size);
                    kportlayout.setHeight(size);
                }
            }
        }
        // put ports in global maps for later use
        _kieler2PtolemyPorts.put(kport, port);
        List<KPort> kports = _ptolemy2KielerPorts.get(port);
        if (kports == null) {
            kports = new ArrayList<KPort>();
            _ptolemy2KielerPorts.put(port, kports);
        }
        kports.add(kport);
    }

    /**
     * Create Kieler ports (KPort) for a Kieler node (KNode) given a list of
     * Ptolemy Port objects and a port type (incoming, outgoing). The new KPorts
     * get initialized in terms of size and position from the Ptolemy
     * counterparts and attached to the corresponding KNode and registered in
     * the mapping fields of this object.
     * 
     * For Ptolemy multiports with multiple connections, multiple Kieler KPorts
     * are created with slightly offsetted location. Hence the layouter can also
     * here consider the location for connection crossing minimization. Hence
     * one Ptolemy port may correspond to multiple Kieler KPorts.
     * 
     * @param knode
     *            The KNode to create Kieler ports for.
     * @param ports
     *            The Ptolemy ports counterparts for which to create Kieler
     *            ports.
     * @param portType
     *            Type of port, input or output. This is relevant for some
     *            Kieler layout algorithms.
     */
    private void _createKPorts(KNode knode, List<Port> ports, KPortType portType) {
        for (Iterator iterator2 = ports.iterator(); iterator2.hasNext();) {
            Port port = (Port) iterator2.next();

            // handle multiports for inputs
            if (port.linkedRelationList().size() > 1) {
                // create a port for each incoming relation and set an order
                List relations = port.linkedRelationList();
                int maxIndex = relations.size() - 1;
                for (int index = 0; index < relations.size(); index++) {
                    _createKPort(knode, portType, port, NO_RANK, index,
                            maxIndex, DEFAULT_PORT_SIZE);
                }
            } else {
                // if not a multiport, just create one port
                _createKPort(knode, portType, port, NO_RANK, 0, 0, -1);
            }
        }
    }

    
    /**
     * Get a Kieler KPort for a corresponding Ptolemy object, i.e. a Port or a
     * relation Vertex. If the input is a Vertex, it is determined which of the
     * two KPorts of the corresponding KNode is returned (as in Kieler a Vertex
     * is represented by one node with one input and one output port).
     * 
     * If the input object is a Ptolemy Port, the KPort counterpart is searched
     * in the global maps. If additionally the Port is a multiport with multiple
     * connection, the given relation is used to determine which KPort
     * corresponds to the Port/Relation combination. In Kieler multiports are
     * represented by multiple KPorts with slightly offsetted locations.
     * 
     * @param ptolemyObject
     *            The corresponding Ptolemy object, either a Vertex or a Port
     * @param type
     *            The type of the port, incoming or outgoing
     * @param rel
     *            The relation that is connected to the Ptolemy multiport
     * @return
     */
    private KPort _getPort(Object ptolemyObject, KPortType type, Relation rel) {
        if (ptolemyObject instanceof Vertex) {
            KNode knode = _ptolemy2KielerNodes.get(ptolemyObject);
            for (KPort port : knode.getPorts()) {
                if (port.getType().equals(type)) {
                    return port;
                }
            }
        } else if (ptolemyObject instanceof Port) {
            // Special case for multiports: For a particular relation, get its
            // index
            // in the relation list of the port. Then get the Kieler port with
            // the same
            // index (as we mapped one Ptolemy multiport to a list of multiple
            // Kieler ports). For a simple port, just give the first
            // corresponding
            // Kieler port.
            List<Relation> relations = ((Port) ptolemyObject)
                    .linkedRelationList();
            int index = relations.indexOf(rel);
            List<KPort> kports = _ptolemy2KielerPorts.get(ptolemyObject);
            if (kports != null) {
                if (index >= 0 && index < kports.size()) {
                    return kports.get(index);
                }
                return kports.get(0);
            }
        }
        return null;
    }

    /**
     * Transform a location from a Kieler node from Kieler coordinate system to
     * ptolemy coordinate system. That is Kieler gives locations to be the upper
     * left corner of an item and Ptolemy as the center point of the item.
     * 
     * If the original location is not within the bounds of the referenceNode at
     * all, the location is not updated. (e.g. important distinction between
     * nodes and vertices).
     * 
     * @param kshapeLayout
     *            Layout of KNode kieler graphical node that contains bounds
     *            with location and size. This object will be altered to fit the
     *            new location.
     * @param referenceNode
     *            The parent reference node giving the bounds to calculate with.
     */
    private void _kNode2Ptolemy(KShapeLayout kshapeLayout, KNode referenceNode) {

        NamedObj namedObj = _kieler2ptolemyEntityNodes.get(referenceNode);
        Object divaNode = _kieler2ptolemyDivaNodes.get(referenceNode);

        double[] location = PtolemyModelUtil._getLocation(namedObj);
        
        if (namedObj != null && divaNode != null) {
            Rectangle2D divaBounds = this.getLayoutTarget().getBounds(divaNode);
            double offsetX = 0, offsetY = 0;
                       
            // check if we got a valid location inside the diva bounds
            // if not we might have something that has no location attribute
            // (e.g. a relation vertex) where we don't need any offset
            if (location.length == 2
                    && divaBounds.contains(location[0], location[1])) {
                offsetX = location[0] - divaBounds.getMinX();
                offsetY = location[1] - divaBounds.getMinY();
            }

            kshapeLayout.setXpos((float) (kshapeLayout.getXpos() + offsetX));
            kshapeLayout.setYpos((float) (kshapeLayout.getYpos() + offsetY));
        }
    }

    private void _replaceRelation(KPort kPort, KNode kNode, Relation newRelation,
            Relation oldRelation) throws IllegalActionException{
        Port port = null;
        Relation sourceRelation = null;
        if (kPort != null) {
            port = _kieler2PtolemyPorts.get(kPort);
        }
        if (port == null) { // we might have an inner input port as source
            NamedObj namedObj = _kieler2ptolemyEntityNodes.get(kNode);
            if (namedObj instanceof Port) {
                port = (Port) namedObj;
            } else if (namedObj instanceof Relation) {
                sourceRelation = (Relation) namedObj;
            }
        }
        if (port != null) { // now we are safe to proceed
            boolean outsideLink = true;
            List<Relation> linkedRelations = port.linkedRelationList();
            int index = linkedRelations.indexOf(oldRelation);
            if(index == -1 && port instanceof ComponentPort){
                linkedRelations = ((ComponentPort)port).insideRelationList();
                outsideLink = false;
                index = linkedRelations.indexOf(oldRelation);
            }
            if(outsideLink){
                _ptolemyModelUtil._unlinkPort(port.getName(_compositeActor), index);
                _ptolemyModelUtil._performChangeRequest(_compositeActor);
                _ptolemyModelUtil._linkPort(port.getName(_compositeActor),
                        "relation", newRelation.getName(_compositeActor), index);
            }
            else{ // insideLink
                if(port instanceof ComponentPort){
                    // FIXME: is this also doable in MoMLChangeRequests?
                    ((ComponentPort) port).unlinkInside(index);
                    ((ComponentPort) port).insertInsideLink(index, newRelation);
                }
            }
            
        } else if (sourceRelation != null) {
            _ptolemyModelUtil._link("relation1", sourceRelation.getName(),
                    "relation2", newRelation.getName(_compositeActor));
        }
    }

    /**
     * Determine the direction of dataflow of all edges and store it in the
     * local maps. Iterate all edges and try to deduce the type of each edge's
     * endpoints, i.e. whether it is an source or target. Do this in multiple
     * iterations by first getting clear information from input and output ports
     * and then propagate this information to the adjacent edges. Work only on
     * the local maps, i.e. get the list of all edges of the
     * _ptolemyDiva2KielerEdges map and store the source and target information
     * in _divaEdgeSource resp. _divaEdgeTarget.
     */
    private void _storeEndpoints() {
        ActorGraphModel aGraph = (ActorGraphModel) this.getLayoutTarget()
                .getGraphModel();
        boolean allDirectionsSet = false;
        Set edges = _ptolemyDiva2KielerEdges.keySet();
        while (!allDirectionsSet) {
            allDirectionsSet = true;
            for (Iterator edgeIter = edges.iterator(); edgeIter.hasNext();) {
                Object edge = (Object) edgeIter.next();
                EdgeModel edgeModel = aGraph.getEdgeModel(edge);
                
                Object simpleEndpoint1 = edgeModel.getHead(edge);
                Object simpleEndpoint2 = edgeModel.getTail(edge);
                Object endpoint1 = aGraph.getSemanticObject(simpleEndpoint1);
                Object endpoint2 = aGraph.getSemanticObject(simpleEndpoint2);
                
                // see if we have successfully looked at this edge before
                if (_divaEdgeTarget.containsKey(edge)
                        && _divaEdgeSource.containsKey(edge))
                    continue;
                
                // check whether endpoints are source or target ports
                if (endpoint1 instanceof Port) {
                    boolean isInput1 = PtolemyModelUtil._isInput((Port) endpoint1);
                    // check if we look at inner or outer ports
                    if(simpleEndpoint1 instanceof Location)
                        isInput1 = !isInput1;  // inner input port is regarded as output
                    // set endpoints
                    if (isInput1) {
                        _divaEdgeTarget.put(edge, simpleEndpoint1);
                        _divaEdgeSource.put(edge, simpleEndpoint2);
                    } else {
                        _divaEdgeTarget.put(edge, simpleEndpoint2);
                        _divaEdgeSource.put(edge, simpleEndpoint1);
                    }
                } else if (endpoint2 instanceof Port) {
                    boolean isInput2 = PtolemyModelUtil._isInput((Port) endpoint2);
                    // check if we look at inner or outer ports
                    if(simpleEndpoint2 instanceof Location)
                        isInput2 = !isInput2;  // inner input port is regarded as output
                    // set endpoints
                    if (isInput2) {
                        _divaEdgeTarget.put(edge, simpleEndpoint2);
                        _divaEdgeSource.put(edge, simpleEndpoint1);
                    } else {
                        _divaEdgeTarget.put(edge, simpleEndpoint1);
                        _divaEdgeSource.put(edge, simpleEndpoint2);
                    }
                } else
                // see if one of the endpoints is source or target of other
                // edges
                if (_divaEdgeTarget.containsValue(endpoint1)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint2);
                    _divaEdgeSource.put(edge, simpleEndpoint1);
                } else if (_divaEdgeTarget.containsValue(endpoint2)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint1);
                    _divaEdgeSource.put(edge, simpleEndpoint2);
                } else if (_divaEdgeSource.containsValue(endpoint1)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint1);
                    _divaEdgeSource.put(edge, simpleEndpoint2);
                } else if (_divaEdgeSource.containsValue(endpoint2)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint2);
                    _divaEdgeSource.put(edge, simpleEndpoint1);
                } else {
                    // now we can't deduce any information about this edge
                    allDirectionsSet = false;
                }
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * The default width used by Kieler for inner ports.
     */
    private static final float DEFAULT_INNER_PORT_WIDTH = 30.0f;
    
    /**
     * The default height used by Kieler for inner ports.
     */
    private static final float DEFAULT_INNER_PORT_HEIGHT = 46.0f;

    /**
     * Default size of a port that will be used in Kieler layout if no explicit
     * size (e.g. copied from Ptolemy port) is given.
     */
    private static final float DEFAULT_PORT_SIZE = 5.0f;

    /**
     * Minimal distance between nodes. Changing this value will decrease the
     * overall horizontal space consumed by the diagram.
     */
    private static final float MIN_SPACING = 10.0f;

    /**
     * Offset between Kieler KPorts corresponding to a Ptolemy multiport. I.e.
     * the distance between multiple single KPorts.
     */
    private static final float MULTIPORT_OFFSET = 4.0f;

    /**
     * Identify that no rank is given to a port.
     */
    private static final int NO_RANK = -1;

    /**
     * The top level Ptolemy composite actor that contains the diagram that is
     * to be laid out.
     */
    private CompositeActor _compositeActor;

    /**
     * Debug flag that will trigger output of additional information during
     * layout run. With the flag set to true especially the Kieler graph
     * structure will be written to a file on harddisk in order to review the
     * graph later on.
     */
    private boolean _debug = true;

    /**
     * Storage of actual sources of diva edges corresponding to data flow.
     */
    private Map<Object, Object> _divaEdgeSource;

    /**
     * Storage of actual targets of diva edges corresponding to data flow.
     */
    private Map<Object, Object> _divaEdgeTarget;

    /**
     * Map Kieler KEdges to Ptolemy Diva Edges.
     */
    private Map<KEdge, Object> _kieler2PtolemyDivaEdges;

    private boolean _doApplyEdgeLayout = false;
    private boolean _doBoxLayout = false;

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
     * Map Kieler ports to Ptolemy ports.
     */
    private Map<KPort, Port> _kieler2PtolemyPorts;

    /**
     * Map Diva nodes to Kieler Nodes.
     * 
     * @see _kieler2ptolemyDivaNodes
     */
    private Map<Object, KNode> _ptolemy2KielerNodes;

    /**
     * Map Ptolemy ports to Kieler ports. A Ptolemy multiport could correspond
     * to multiple single Kieler ports. Hence it's a mapping to a List of
     * KPorts.
     */
    private Map<Port, List<KPort>> _ptolemy2KielerPorts;

    /**
     * Map Ptolemy links (= Edges in Diva) to Edges in Kieler
     */
    private Map<Object, KEdge> _ptolemyDiva2KielerEdges;

    /**
     * Helper class to manipulate Ptolemy models. Especially for sending
     * MoMLChangeRequests. Instance is required to buffer multiple requests for
     * better performance.
     */
    private PtolemyModelUtil _ptolemyModelUtil;

    /**
     * Variable to store time for statistics.
     */
    private long _time;

}
