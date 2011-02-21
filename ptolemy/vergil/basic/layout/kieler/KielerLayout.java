/* Layout interface between the Ptolemy Diva editor and the KIELER
 * layout library.*/
/*
 @Copyright (c) 2009-2010 The Regents of the University of California.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.graph.GraphInvalidStateException;
import ptolemy.gui.Top;
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
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.SnapConstraint;
import de.cau.cs.kieler.core.KielerException;
import de.cau.cs.kieler.core.alg.BasicProgressMonitor;
import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.LayoutDirection;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.options.PortType;
import de.cau.cs.kieler.kiml.util.BoxLayoutProvider;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klodd.hierarchical.HierarchicalDataflowLayoutProvider;
import diva.canvas.CanvasComponent;
import diva.canvas.CompositeFigure;
import diva.graph.GraphModel;
import diva.graph.layout.AbstractGlobalLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.modular.EdgeModel;

///////////////////////////////////////////////////////////////////
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
 * Setting bendpoints is not yet supported in Ptolemy. Ptolemy's built-in
 * connection routing does not consider obstacle avoidance, hence overlappings
 * with other nodes and connections might appear. This class tries to gap that
 * hole by the option to add new relations with vertices into the Ptolemy model.
 * The vertices are placed at the bend point positions such that a certain
 * routing is forced.
 * <p>
 * In order to avoid cluttering of the diagram with new clumsy vertices, there
 * are methods available to remove or just hide unnecessary relation vertices.
 * That are those vertices, just put in for routing and that have no semantic
 * relevance.
 * <p>
 * It uses location attributes of actors and attributes to place items and
 * corresponding for relation vertices.
 * 
 * @author Hauke Fuhrmann, <haf@informatik.uni-kiel.de>, Christian Motika ,
 *         <cmot@informatik.uni-kiel.de>
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class KielerLayout extends AbstractGlobalLayout {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Construct an instance taking a LayoutTarget for specifying some methods
     * for layout handling as given by the standard Ptolemy
     * AbstractGlobalLayout. The KielerLayout will need access to the top level
     * Ptolemy model, so either use corresponding constructor or call setModel()
     * prior to layout invocation.
     * 
     * @param target The LayoutTarget on which layout will be performed
     */
    public KielerLayout(LayoutTarget target) {
        super(target);
    }

    /**
     * Construct an instance setting the LayoutTarget as requested by the
     * AbstractGlobalLayout and the containing Ptolemy model. Preferred
     * constructor.
     * 
     * @param target The LayoutTarget on which layout will be performed
     * @param ptolemyContainer The composite actor that contains all elements to
     *            be layouted
     */
    public KielerLayout(LayoutTarget target, CompositeActor ptolemyContainer) {
        super(target);
        this.setModel(ptolemyContainer);
    }

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
     * @param composite the container of the diagram in terms of an GraphModel.
     */
    @Override
    public void layout(Object composite) {
        long overallTime = System.currentTimeMillis();
        String report;
        _ptolemyModelUtil = new PtolemyModelUtil();

        report = "Removing unnecessary relation vertices... ";
        _report(report);
        if (DEBUG) {
            _time = System.currentTimeMillis();
            System.out.print(report);
        }

        // TODO: Dont do this any more?
        // PtolemyModelUtil._removeUnnecessaryRelations(this._compositeActor);

        if (DEBUG) {
            System.out.println("done in "
                    + (System.currentTimeMillis() - _time) + "ms");
        }

        report = "Creating Kieler KGraph from Ptolemy model... ";
        _report(report);
        if (DEBUG) {
            _time = System.currentTimeMillis();
            System.out.print(report);
        }

        // create a Kieler Graph
        // create one node that will be used for the Hierarchical layout
        // algorithm. This will contain all items with connections
        KNode hierarchicalLayoutNode = KimlUtil.createInitializedNode();
        // create one node that will be layouted with a simple box layout
        // not considering connections. This will be used for all unconnected
        // nodes such as the director, text attributes and parameters
        KNode boxLayoutNode = KimlUtil.createInitializedNode();
        // make the hierarchical node a child of the box node so that
        // the unconnected nodes will be placed around the other ones
        hierarchicalLayoutNode.setParent(boxLayoutNode);
        KShapeLayout layout = hierarchicalLayoutNode
                .getData(KShapeLayout.class);
        layout.setProperty(LayoutOptions.LAYOUT_DIRECTION,
                LayoutDirection.RIGHT);
        layout.setProperty(LayoutOptions.OBJ_SPACING, MIN_SPACING);

        // now read ptolemy model and fill the two subgraphs with the model
        // infos
        _createGraph(composite, hierarchicalLayoutNode, boxLayoutNode);

        report = "Performing layout... ";
        _report(report);
        if (DEBUG) {
            System.out.println("done in "
                    + (System.currentTimeMillis() - _time) + "ms");
            _time = System.currentTimeMillis();
            System.out.print(report);
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
            KPoint offset = KielerGraphUtil
                    ._getUpperLeftCorner(hierarchicalLayoutNode);

            layout.setXpos(layout.getXpos() - offset.getX());
            layout.setYpos(layout.getYpos() - offset.getY());
            if (_doBoxLayout) {
                boxLayoutProvider.doLayout(boxLayoutNode,
                        progressMonitor.subTask(10));
            }

            report = "Applying layout to Ptolemy diagram... ";

            // write to XML file for debugging layout
            // writing to file requires XMI resource factory
            if (DEBUG) {
                System.out.println("done in "
                        + (System.currentTimeMillis() - _time) + "ms");
                KielerGraphUtil._writeToFile(boxLayoutNode);
                _time = System.currentTimeMillis();
                System.out.print(report);
            }

            // apply layout to ptolemy model. Will do so
            // recursively for all containing nodes (e.g. especially
            // for the hierarchical layout node and its contents)
            _applyLayout(hierarchicalLayoutNode);
            // _applyLayout(boxLayoutNode);

            if (DEBUG) {
                System.out.println("done in "
                        + (System.currentTimeMillis() - _time) + "ms");
            }

            report = "KIELER layout done in "
                    + (System.currentTimeMillis() - overallTime) + "ms.";
            _report(report);

        } catch (KielerException e) {
            // throw some Ptolemy runtime exception for a Kieler exception
            throw new GraphInvalidStateException(e,
                    "KIELER runtime exception: " + e.getMessage());
        } catch (IllegalActionException e) {
            // throw some Ptolemy runtime exception
            throw new GraphInvalidStateException(e,
                    "KIELER runtime exception: " + e.getMessage());
        }

    }

    /**
     * Configure whether the layout should only place nodes or additionally
     * route edges. Edge routing would be done by insertion of new relation
     * vertices which is a real manipulation of a model. Routing is only
     * supported for standard actor based frames with relations. Different arrow
     * styles like in Modal Models are not yet supported for routing.
     * 
     * @param flag True iff edge routing shall be applied by insertion of
     *            relation vertices.
     */
    public void setApplyEdgeLayout(boolean flag) {
        this._doApplyEdgeLayout = flag;
    }

    /**
     * Configure whether the layout should only place nodes or additionally
     * route edges. Edge routing would be done by annotating the relations with
     * bend point information of their connected links. Different arrow styles
     * like in Modal Models are not yet supported for routing.
     * 
     * @param flag True iff edge routing shall be applied by insertion of
     *            relation vertices.
     */
    public void setApplyEdgeLayoutBendPointAnnotation(boolean flag) {
        this._doApplyEdgeLayoutBendPointAnnotation = flag;
    }

    /**
     * Configure whether all unconnected nodes should also be placed by the
     * layouter with a simple box layout heuristic. Either only connected nodes
     * (e.g. actors connected by relations) get placed or all nodes including
     * unconnected nodes get placed. The rationale behind not placing
     * unconnected items is that the user might want to manually place text
     * annotations or parameters at some special locations, e.g. to document
     * specific parts of the model. In that case the bounding box of all
     * connected nodes will be located with its upper left corner at the same
     * position as before.
     * 
     * @param flag If true, apply layout to all nodes, including unconnected
     *            ones. Otherwise, apply layout only to connected nodes.
     */
    public void setBoxLayout(boolean flag) {
        this._doBoxLayout = flag;
    }

    /**
     * Set the Ptolemy Model that contains the graph that is to be layouted. The
     * layouter will require access to the Ptolemy model because the lower level
     * Diva abstraction does not consider certain properties required by the
     * Kieler layouter such as port positions.
     * 
     * @param model The parent composite actor which internal diagram shall be
     *            layouted.
     */
    public void setModel(CompositeActor model) {
        this._compositeActor = model;
    }

    /**
     * Set the Top window to enable status reports on the status bar.
     * 
     * @param top The Top window
     */
    public void setTop(Top top) {
        this._top = top;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Apply precomputed routing of edges to the Ptolemy model by insertion of
     * new relation vertices. Take a Kieler KEdge with layout information (bend
     * point positions) and create a new relation with a vertex for each bend
     * point and interconnect them. Then replace the original relation with the
     * new relation set. Return the original relation if it is safe to delete
     * it.
     * 
     * @param kEdge The Kieler KEdge that hold the precomupted layout
     *            information, i.e. bend point positions
     * @return The old Relation if it is safe to delete it.
     * @exception IllegalActionException Exception will be thrown if replacing
     *                of original relation is not possible, i.e. if unlink() or
     *                link() methods fail.
     */
    private Relation _applyEdgeLayout(KEdge kEdge)
            throws IllegalActionException {
        List<NamedObj> removedLinkTargets = new ArrayList<NamedObj>();
        int count = 0;
        String previousRelation = null;

        Relation oldRelation = (Relation) this.getLayoutTarget()
                .getGraphModel()
                .getSemanticObject(_kieler2PtolemyDivaEdges.get(kEdge));
        List<KPoint> bendPoints = kEdge.getData(KEdgeLayout.class)
                .getBendPoints();
        for (KPoint relativeKPoint : bendPoints) {
            KPoint kpoint = KielerGraphUtil._getAbsoluteKPoint(relativeKPoint,
                    KielerGraphUtil._getParent(kEdge));
            // calculate the snap-to-grid coordinates
            Point2D bendPoint = new Point2D.Double(kpoint.getX(), kpoint.getY());
            Point2D snapToGridBendPoint = SnapConstraint
                    .constrainPoint(bendPoint);
            // create new relation
            String relationName = _ptolemyModelUtil._getUniqueString(
                    _compositeActor, "relation");
            relationName = _ptolemyModelUtil._createRelationWithVertex(
                    relationName, snapToGridBendPoint.getX(),
                    snapToGridBendPoint.getY());

            // we process the first bendpoint
            if (count == 0) {
                KPort kSourcePort = kEdge.getSourcePort();
                KNode kNode = kEdge.getSource();
                NamedObj removedLink = _replaceRelation(kSourcePort, kNode,
                        relationName, oldRelation);
                if (removedLink != null) {
                    removedLinkTargets.add(removedLink);
                }
            }

            // process all other bendpoints
            else {
                if (previousRelation != null) {
                    _ptolemyModelUtil._link("relation1", previousRelation,
                            "relation2", relationName);
                }
            }
            previousRelation = relationName;
            count++;
        }

        // last, process target
        if (previousRelation != null) {
            KPort kTargetPort = kEdge.getTargetPort();
            KNode kNode = kEdge.getTarget();
            NamedObj removedLink = _replaceRelation(kTargetPort, kNode,
                    previousRelation, oldRelation);
            if (removedLink != null) {
                removedLinkTargets.add(removedLink);
            }
        }
        // remove old relation if it is no longer connected
        // here we cannot ask the Relation itself as all unlinks are buffered
        // and haven't been executed yet. So we must do our own bookkeeping.
        for (Object linkedObject : oldRelation.linkedObjectsList()) {
            if (!removedLinkTargets.contains(linkedObject)) {
                return null;
            }
        }
        return oldRelation;
    }

    /**
     * Apply the layout of an KEdge to its corresponding Diva Link. This is done
     * by adding a LayoutHint attribute to the corresponding Relation. Only
     * Relations are persistent objects in the abstract Ptolemy syntax and
     * therefore add the bend points for a link in its Relation. Hence, there
     * may be multiple link bend points stored in one Relation. The LayoutHint
     * attribute can carry the bend points for multiple Links and identifies the
     * link by the head and tail of the link.
     * 
     * @param kEdge The Kieler KEdge that hold the precomupted layout
     *            information, i.e. bend point positions
     * @exception IllegalActionException Exception will be thrown if replacing
     *                of original relation is not possible, i.e. if unlink() or
     *                link() methods fail.
     */
    private void _applyEdgeLayoutBendPointAnnotation(KEdge kEdge)
            throws IllegalActionException {
        //List<NamedObj> removedLinkTargets = new ArrayList<NamedObj>();
        //int count = 0;
        //String previousRelation = null;

        Object object = _kieler2PtolemyDivaEdges.get(kEdge);
        if (object instanceof Link) {
            Link link = (Link) object;

            Relation relation = (Relation) this.getLayoutTarget()
                    .getGraphModel().getSemanticObject(link);

            List<KPoint> bendPoints = kEdge.getData(KEdgeLayout.class)
                    .getBendPoints();

            // translate bend points into an array of doubles for the layout
            // hint attribute
            double[] layoutHintBendPoints = new double[bendPoints.size() * 2];
            int index = 0;
            for (KPoint relativeKPoint : bendPoints) {
                KPoint kpoint = KielerGraphUtil._getAbsoluteKPoint(
                        relativeKPoint, KielerGraphUtil._getParent(kEdge));

                // calculate the snap-to-grid coordinates
                Point2D bendPoint = new Point2D.Double(kpoint.getX(),
                        kpoint.getY());
                Point2D snapToGridBendPoint = SnapConstraint
                        .constrainPoint(bendPoint);

                layoutHintBendPoints[index] = snapToGridBendPoint.getX();
                layoutHintBendPoints[index + 1] = snapToGridBendPoint.getY();
                index += 2;
            }

            // now add a LayoutHint attribute to the relation
            // reuse any existing layout hint. There is only one per relation.
            try {
                Attribute layoutHint = relation.getAttribute("_layoutHint");
                if (layoutHint == null) {
                    layoutHint = new LayoutHint(relation, "_layoutHint");
                }
                if (layoutHint instanceof LayoutHint) {
                    NamedObj head = (NamedObj) link.getHead();
                    NamedObj tail = (NamedObj) link.getTail();
                    // determine correct direction of the edge
                    if (head == _divaEdgeSource.get(link)) {
                        ((LayoutHint) layoutHint).setLayoutHintItem(head, tail,
                                layoutHintBendPoints);
                    } else {
                        ((LayoutHint) layoutHint).setLayoutHintItem(tail, head,
                                layoutHintBendPoints);
                    }
                    // add this Attribute in a MoMLChangeRequest in order to get
                    // all
                    // change notifications right
                    _ptolemyModelUtil.addProperty(relation, layoutHint);
                }
            } catch (Exception e) {
                throw new IllegalActionException(
                        "Cannot set _layoutHint attribute for " + relation
                                + ": " + e.getMessage());
            }
        }
        return;
    }

    /**
     * Traverse a composite KNode containing corresponding Kieler nodes, ports
     * and edges for the Ptolemy model and apply all layout information
     * contained by it back to the Ptolemy model. Do most changes to the Ptolemy
     * model via MoMLChangeRequests. Set location attributes for all visible
     * Ptolemy nodes. So far Ptolemy does not support setting of connection
     * bendpoints explicitly. The Ptolemy connection router does not consider
     * obstruction avoidance so there are likely to be connection overlappings
     * in the diagram.
     * <p>
     * Optionally route edges explicitly by inserting new relation vertices for
     * each bend point.
     * 
     * @see #setApplyEdgeLayout(boolean)
     * 
     * @param kgraph The Kieler graph object containing all layout information
     *            to apply to the Ptolemy model
     * @exception IllegalActionException Exception can be thrown if routing of
     *                edges fails due to not allowed unlinking or linking of new
     *                relations.
     */
    private void _applyLayout(KNode kgraph) throws IllegalActionException {
        // long time = System.currentTimeMillis();

        // init required classes
        GraphModel graph = this.getLayoutTarget().getGraphModel();
        if (graph instanceof ActorGraphModel) {
            // apply node layout
            Collection<KNode> kNodes = kgraph.getChildren();
            if (_doBoxLayout) {
                kNodes = _kieler2ptolemyEntityNodes.keySet();
            }

            for (KNode knode : kNodes) {
                KShapeLayout absoluteLayout = KielerGraphUtil
                        ._getAbsoluteLayout(knode);
                NamedObj namedObj = _kieler2ptolemyEntityNodes.get(knode);
                // transform coordinate systems
                _kNode2Ptolemy(absoluteLayout, knode);

                // calculate the snap-to-grid coordinates
                Point2D bendPoint = new Point2D.Double(
                        absoluteLayout.getXpos(), absoluteLayout.getYpos());
                Point2D snapToGridBendPoint = SnapConstraint
                        .constrainPoint(bendPoint);

                if (namedObj instanceof Relation) {
                    Vertex vertex = (Vertex) _kieler2ptolemyDivaNodes
                            .get(knode);
                    _ptolemyModelUtil._setLocation(vertex, (Relation) namedObj,
                            snapToGridBendPoint.getX(),
                            snapToGridBendPoint.getY());
                } else {
                    _ptolemyModelUtil._setLocation(namedObj,
                            snapToGridBendPoint.getX(),
                            snapToGridBendPoint.getY());
                }
            }

            // System.out.println("Nodes apply: "+(System.currentTimeMillis()-time));
            // time = System.currentTimeMillis();

            // apply edge layout - extra vertices (haf)
            if (_doApplyEdgeLayout) {
                Set<Relation> relationsToDelete = new HashSet<Relation>();
                for (KEdge kedge : _kieler2PtolemyDivaEdges.keySet()) {
                    Relation oldRelation = _applyEdgeLayout(kedge);
                    if (oldRelation != null) {
                        relationsToDelete.add(oldRelation);
                        // System.out.println("Edge apply: "+(System.currentTimeMillis()-time));
                        // time = System.currentTimeMillis();
                    }
                }
                _ptolemyModelUtil._removeRelations(relationsToDelete);
            }
            // apply edge layout - bend points (cmot)
            if (_doApplyEdgeLayoutBendPointAnnotation) {
                for (KEdge kedge : _kieler2PtolemyDivaEdges.keySet()) {
                    _applyEdgeLayoutBendPointAnnotation(kedge);
                }
            }
        }
        // create change request and fire it
        _ptolemyModelUtil.performChangeRequest(_compositeActor);
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
     * @param composite The GraphModel composite object to retrieve the model
     *            information from
     * @param boxLayoutNode Kieler subgraph to receive all unconnected model
     *            elements
     * @param hierarchicalLayoutNode Kieler subgraph to receive all connected
     *            model elements
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

                // temporary variable for the new KNode corresponding to one
                // of the following cases depending on what the semantic object
                // is
                KNode knode = null;

                // handle actors, text and directors
                if (semanticNode instanceof Actor
                        || semanticNode instanceof Attribute) {

                    // for a ptolemy node create a Kieler KNode and
                    // put it in either the parents for hierarchichal
                    // or box layout depending on whether it is
                    // connected to something or not
                    knode = _createKNode(node, semanticNode);

                    // handle ports
                    if (semanticNode instanceof Actor
                            && semanticNode instanceof Entity) {
                        Actor actor = (Actor) semanticNode;
                        List<Port> inputs = actor.inputPortList();
                        List<Port> outputs = actor.outputPortList();

                        // create ports
                        _createKPorts(knode, inputs, PortType.INPUT);
                        _createKPorts(knode, outputs, PortType.OUTPUT);
                    }
                }

                // handle relation vertices
                else if (semanticNode instanceof Relation) {
                    // regard a relation vertex as a Kieler KNode
                    knode = _createKNodeForVertex((Vertex) node);
                }

                else if (semanticNode instanceof Port) {
                    knode = _createKNodeForPort(node, (Port) semanticNode);
                    // setup Kieler ports for the KNode of the internal Ptolemy
                    // port
                    // _createKPort(kPortNode, (Port)semanticNode);
                }

                // now do some common bookkeeping for all kinds of nodes
                if (knode != null) {
                    // store node in the correct composite node depending on
                    // whether it has connections or not
                    if (PtolemyModelUtil._isConnected((NamedObj) semanticNode)) {
                        knode.setParent(hierarchicalLayoutNode);
                        // get check bounds for global bounding box
                        KShapeLayout layout = knode.getData(KShapeLayout.class);
                        if (layout.getXpos() < globalX) {
                            globalX = layout.getXpos();
                        }
                        if (layout.getYpos() < globalY) {
                            globalY = layout.getYpos();
                        }
                    } else {
                        knode.setParent(boxLayoutNode);
                    }

                    // store node for later applying layout back
                    _ptolemy2KielerNodes.put(node, knode);
                    _kieler2ptolemyDivaNodes.put(knode, node);
                    _kieler2ptolemyEntityNodes.put(knode,
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
                } else if (semanticNode instanceof Port) { // internal ports
                    portList.add(node);
                    portIter = portList.iterator();
                }
                if (portIter != null) {
                    for (; portIter.hasNext();) {
                        Object divaPort = portIter.next();
                        // iterate all outgoing edges
                        Iterator edgeIterator = aGraph.outEdges(divaPort);
                        if (semanticNode instanceof Port) { // internal ports
                            edgeIterator = aGraph.getExternalPortModel()
                                    .outEdges(node);
                        }
                        for (; edgeIterator.hasNext();) {
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
        KShapeLayout layout = hierarchicalLayoutNode
                .getData(KShapeLayout.class);
        layout.setXpos(globalX);
        layout.setYpos(globalY);
    }

    // private void _createGraph(Object composite, KNode hierarchicalLayoutNode,
    // KNode boxLayoutNode) {
    // _ptolemy2KielerNodes = new HashMap<Object, KNode>();
    // _kieler2ptolemyDivaNodes = new HashMap<KNode, Object>();
    // _kieler2ptolemyEntityNodes = new HashMap<KNode, NamedObj>();
    // _ptolemyDiva2KielerEdges = new HashMap<Object, KEdge>();
    // _kieler2PtolemyDivaEdges = new HashMap<KEdge, Object>();
    // _ptolemy2KielerPorts = new HashMap<Port, List<KPort>>();
    // _kieler2PtolemyPorts = new HashMap<KPort, Port>();
    // _divaEdgeSource = new HashMap<Object, Object>();
    // _divaEdgeTarget = new HashMap<Object, Object>();
    //
    // // on-the-fly find upper left corner for bounding box of parent node
    // float globalX = Float.MAX_VALUE, globalY = Float.MAX_VALUE;
    //
    // // traverse ptolemy graph
    // LayoutTarget target = this.getLayoutTarget();
    // GraphModel graph = target.getGraphModel();
    // if (graph instanceof ActorGraphModel) {
    // ActorGraphModel aGraph = (ActorGraphModel) graph;
    //
    // // process nodes
    // for (Iterator iterator = aGraph.nodes(composite); iterator
    // .hasNext();) {
    // Object node = iterator.next();
    //
    // // here we get the corresponding Ptolemy object
    // // this breaks with Ptolemy/Diva abstraction
    // // for now we need the ptolemy Actor to get the ports and port
    // // positions
    // // and to distinguish Actors and Relation vertices
    // Object semanticNode = aGraph.getSemanticObject(node);
    //
    // // handle actors, text and directors
    // if (semanticNode instanceof Actor
    // || semanticNode instanceof Attribute) {
    //
    // // for a ptolemy node create a Kieler KNode and
    // // put it in either the parents for hierarchichal
    // // or box layout depending on whether it is
    // // connected to something or not
    // KNode knode = _createKNode(node, semanticNode);
    //
    // // store node in the correct composite node depending on
    // // whether it has connections or not
    // if (PtolemyModelUtil._isConnected((NamedObj) semanticNode)) {
    // knode.setParent(hierarchicalLayoutNode);
    // // get check bounds for global bounding box
    // KShapeLayout layout = KimlUtil
    // .getShapeLayout(knode);
    // if (layout.getXpos() < globalX) {
    // globalX = layout.getXpos();
    // }
    // if (layout.getYpos() < globalY) {
    // globalY = layout.getYpos();
    // }
    // } else {
    // knode.setParent(boxLayoutNode);
    // }
    //
    // // handle ports
    // if (semanticNode instanceof Actor
    // && semanticNode instanceof Entity) {
    // Actor actor = (Actor) semanticNode;
    // List<Port> inputs = actor.inputPortList();
    // List<Port> outputs = actor.outputPortList();
    //
    // // create ports
    // _createKPorts(knode, inputs, KPortType.INPUT);
    // _createKPorts(knode, outputs, KPortType.OUTPUT);
    // }
    // }
    //
    // // handle relation vertices
    // else if (semanticNode instanceof Relation) {
    // // regard a relation vertex as a Kieler KNode
    // KNode kVertexNode = _createKNodeForVertex((Vertex) node);
    // // add it to the graph
    // kVertexNode.setParent(hierarchicalLayoutNode);
    //
    // // get check bounds for global bounding box
    // KShapeLayout layout = KimlUtil
    // .getShapeLayout(kVertexNode);
    // if (layout.getXpos() < globalX) {
    // globalX = layout.getXpos();
    // }
    // if (layout.getYpos() < globalY) {
    // globalY = layout.getYpos();
    // }
    //
    // // store it in the maps
    // _ptolemy2KielerNodes.put(node, kVertexNode);
    // _kieler2ptolemyDivaNodes.put(kVertexNode, node);
    // _kieler2ptolemyEntityNodes.put(kVertexNode,
    // (NamedObj) semanticNode);
    // }
    //
    // else if (semanticNode instanceof Port) {
    // KNode kPortNode = _createKNodeForPort(node,
    // (Port) semanticNode);
    // // add it to the graph
    // kPortNode.setParent(hierarchicalLayoutNode);
    // // setup Kieler ports for the KNode of the internal Ptolemy
    // // port
    // // _createKPort(kPortNode, (Port)semanticNode);
    //
    // // get check bounds for global bounding box
    // KShapeLayout layout = KimlUtil
    // .getShapeLayout(kPortNode);
    // if (layout.getXpos() < globalX) {
    // globalX = layout.getXpos();
    // }
    // if (layout.getYpos() < globalY) {
    // globalY = layout.getYpos();
    // }
    //
    // // store it in the maps
    // _ptolemy2KielerNodes.put(node, kPortNode);
    // _kieler2ptolemyDivaNodes.put(kPortNode, node);
    // _kieler2ptolemyEntityNodes.put(kPortNode,
    // (NamedObj) semanticNode);
    // }
    //
    // // check if the node has ports
    // Iterator portIter = null;
    // List portList = new ArrayList();
    // if (semanticNode instanceof Relation) {
    // // if object is a relation vertex, it is itself kinda port
    // portList.add(node);
    // portIter = portList.iterator();
    // } else if (semanticNode instanceof Actor) {
    // portIter = aGraph.nodes(node);
    // } else if (semanticNode instanceof Port) { // internal ports
    // portList.add(node);
    // portIter = portList.iterator();
    // }
    // if (portIter != null) {
    // for (; portIter.hasNext();) {
    // Object divaPort = portIter.next();
    // // iterate all outgoing edges
    // Iterator edgeIterator = aGraph.outEdges(divaPort);
    // if (semanticNode instanceof Port) { // internal ports
    // edgeIterator = aGraph.getExternalPortModel()
    // .outEdges(node);
    // }
    // for (; edgeIterator.hasNext();) {
    // Object divaEdge = edgeIterator.next();
    // // store Diva edge in corresponding map with no
    // // KEdge that will be created later
    // _ptolemyDiva2KielerEdges.put(divaEdge, null);
    // }
    // }
    // }
    // }
    //
    // // create Kieler KEdges for Diva edges
    // _storeEndpoints();
    // for (Object divaEdge : _ptolemyDiva2KielerEdges.keySet()) {
    // _createKEdge(divaEdge);
    // }
    // }
    //
    // // set Bounding Box
    // KShapeLayout layout = KimlUtil
    // .getShapeLayout(hierarchicalLayoutNode);
    // layout.setXpos(globalX);
    // layout.setYpos(globalY);
    // }

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
     * @param divaEdge The Ptolemy diva edge object for which to create a new
     *            KEdge.
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

            KEdge kedge = KimlUtil.createInitializedEdge();

            Object source = _divaEdgeSource.get(divaEdge);
            Object target = _divaEdgeTarget.get(divaEdge);

            KPort kSourcePort = this._getPort(source, PortType.OUTPUT, rel);
            if (kSourcePort != null) {
                kedge.setSourcePort(kSourcePort);
                kSourcePort.getEdges().add(kedge);
                kedge.setSource(kSourcePort.getNode());
            } else {
                // edge is not connected to a port
                kedge.setSource(_ptolemy2KielerNodes.get(source));
            }
            KPort kTargetPort = this._getPort(target, PortType.INPUT, rel);
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
     * _kieler2ptolemyEntityNodes, such that the {@link #_applyLayout(KNode)}
     * method will be able to reapply the layout.
     * 
     * @param node The Diva node object.
     * @param semanticNode The corresponding Ptolemy semantic object, e.g. an
     *            Actor or TextAttribute
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
        KNode knode = KimlUtil.createInitializedNode();
        knode.getLabel().setText(name);
        // KLabel label = KimlUtil.createInitializedLabel(knode);
        // label.setText(name);
        KShapeLayout klayout = knode.getData(KShapeLayout.class);
        klayout.setHeight((float) bounds.getHeight());
        klayout.setWidth((float) bounds.getWidth());
        klayout.setXpos((float) bounds.getMinX());
        klayout.setYpos((float) bounds.getMinY());
        // transform coordinates
        // _ptolemy2KNode(klayout);
        klayout.setProperty(LayoutOptions.FIXED_SIZE, true);
        klayout.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                PortConstraints.FIXED_POS);

        // draw the director always as first element
        if (semanticNode instanceof Director) {
            klayout.setProperty(LayoutOptions.PRIORITY, 1);
        }

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
     * @param divaLocation Diva Representation of an inner port
     * @param port The Ptolemy inner port.
     * @return A new Kieler KNode corresponding to the Ptolemy inner port.
     */
    private KNode _createKNodeForPort(Object divaLocation, Port port) {
        KNode knode = KimlUtil.createInitializedNode();
        KShapeLayout layout = knode.getData(KShapeLayout.class);
        Rectangle2D bounds = this.getLayoutTarget().getBounds(divaLocation);
        // set alignment offset in order to set right height
        layout.setHeight((float) bounds.getHeight() + INNER_PORT_HEIGHT_OFFSET);
        layout.setWidth((float) bounds.getWidth());
        layout.setProperty(LayoutOptions.FIXED_SIZE, true);
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
     * @param vertex The Ptolemy vertex for which to create a KNode
     * @return An initialized KNode with one input and one output port
     */
    private KNode _createKNodeForVertex(Vertex vertex) {
        KNode kNode = KimlUtil.createInitializedNode();
        // simulate vertex by node with size 0
        KShapeLayout layout = kNode.getData(KShapeLayout.class);
        layout.setHeight(1);
        layout.setWidth(1);
        layout.setXpos((float) vertex.getLocation()[0]);
        layout.setYpos((float) vertex.getLocation()[1]);
        layout.setProperty(LayoutOptions.FIXED_SIZE, true);
        layout.setProperty(LayoutOptions.HYPERNODE, true);
        // as Kieler so far only suport nodes WITH port constraints,
        // add dummy ports
        /* TODO: The new klodd 0.4 version now supports nodes without ports.
         *       however, this could require more refactoring than only here.
         */
        KPort kInputPort = KimlUtil.createInitializedPort();
        KShapeLayout portLayout = kInputPort.getData(KShapeLayout.class);
        portLayout.setHeight(0);
        portLayout.setWidth(0);
        portLayout.setXpos(0);
        portLayout.setYpos(0);
        // kInputPort.setType(PortType.INPUT);
        kInputPort.setNode(kNode);
        KPort kOutputPort = KimlUtil.createInitializedPort();
        portLayout = kOutputPort.getData(KShapeLayout.class);
        portLayout.setHeight(0);
        portLayout.setWidth(0);
        portLayout.setXpos(0);
        portLayout.setYpos(0);
        // kOutputPort.setType(KPortType.OUTPUT);
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
     * @param knode The parent KNode of the new port
     * @param portType The port Type, either input or output
     * @param port The corresponding Ptolemy port (might be a multiport)
     * @param rank The rank of the new port which is an ordering index. If this
     *            is not set, Kieler will try to infer the ranks automatically
     *            from the port's position.
     * @param index Index of the KPort corresponding to a multiport
     * @param maxIndex Width of the multiport, i.e. the number of connected
     *            edges to that port.
     * @param size Custom size (same for width and height) for a port that will
     *            be used instead of the real Ptolemy port size. If this value
     *            is negative, the original Ptolemy sizes are used.
     */
    private void _createKPort(KNode knode, PortType portType, Port port,
            int rank, int index, int maxIndex, float size) {
        // create a new Kieler port
        KPort kport = KimlUtil.createInitializedPort();
        KShapeLayout kportlayout = kport.getData(KShapeLayout.class);
        // init port layout
        kportlayout.setXpos(0);
        kportlayout.setYpos(0);
        kportlayout.setHeight(5);
        kportlayout.setWidth(5);
        // add port to node and set type and options
        knode.getPorts().add(kport);
        // kport.setType(portType);
        // set a rank if valid
        if (rank != NO_RANK) {
            kportlayout.setProperty(LayoutOptions.PORT_RANK, rank);
        }

        // set port side and calc actual offset
        float[] offsets = _getMultiportOffsets(port, kportlayout, index,
                maxIndex);
        float offsetX = offsets[0], offsetY = offsets[1];

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
                Point2D.Double portLocation = new Point2D.Double(
                        portBounds.getMinX(), portBounds.getMinY());
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
                    int direction = IOPortController
                            .getDirection(IOPortController.getCardinality(port));
                    Point2D shrunkenLocation = KielerGraphUtil
                            ._shrinkCoordinates(newPortBounds,
                                    shrunkPortBounds, direction,
                                    MULTIPORT_BOTTOM);
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
     * @param knode The KNode to create Kieler ports for.
     * @param ports The Ptolemy ports counterparts for which to create Kieler
     *            ports.
     * @param portType Type of port, input or output. This is relevant for some
     *            Kieler layout algorithms.
     */
    private void _createKPorts(KNode knode, List<Port> ports, PortType portType) {
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
     * For a given Ptolemy port, its channel index in a multiport and the
     * maximum index in that multiport, calculate its offset in X and Y
     * coordinates. For example, the first channel on the east side has offset 0
     * and the next channel is moved below the first one and so on. On the north
     * side, the last channel has offset 0 and the first channel is at the most
     * left side.
     * 
     * @param port the Ptolemy port
     * @param kportlayout the corresponding KPort KShapeLayout
     * @param index index of the channel
     * @param maxIndex maximum available channel
     * @return float array with exactly two entries, x- and y-coordinate for the
     *         offset
     */
    private float[] _getMultiportOffsets(Port port, KShapeLayout kportlayout,
            int index, int maxIndex) {
        float offsetX = 0, offsetY = 0;
        int direction = IOPortController.getDirection(IOPortController
                .getCardinality(port));
        switch (direction) {
        case SwingConstants.NORTH:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.NORTH);
            // ports are extended to left with leftmost port index 0
            offsetX = -((maxIndex - index) * MULTIPORT_OFFSET);
            break;
        case SwingConstants.EAST:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.EAST);
            // ports are extended to bottom with top port index 0
            offsetY = index * MULTIPORT_OFFSET;
            break;
        case SwingConstants.SOUTH:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.SOUTH);
            offsetX = (index * MULTIPORT_OFFSET);
            break;
        default:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.WEST);
            // ports are extended to top beginning with top port index 0
            offsetY = -((maxIndex - index) * MULTIPORT_OFFSET);
            // if (maxIndex > 0 && index == maxIndex)
            // offsetY = 1;
            break;
        }
        float[] offsets = { offsetX, offsetY };
        return offsets;
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
     * @param ptolemyObject The corresponding Ptolemy object, either a Vertex or
     *            a Port
     * @param type The type of the port, incoming or outgoing
     * @param rel The relation that is connected to the Ptolemy multiport
     * @return The port found or null if no port was found.
     */
    private KPort _getPort(Object ptolemyObject, PortType type, Relation rel) {
        if (ptolemyObject instanceof Vertex) {
            KNode knode = _ptolemy2KielerNodes.get(ptolemyObject);
            for (KPort port : knode.getPorts()) {
                // if (port.getType().equals(type)) {
                return port;
                // }
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
     * @param kshapeLayout Layout of KNode kieler graphical node that contains
     *            bounds with location and size. This object will be altered to
     *            fit the new location.
     * @param referenceNode The parent reference node giving the bounds to
     *            calculate with.
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

    /**
     * Replace a relation connected to a port of a node with a new relation by
     * unlinking the old one and linking the new one. Properly handle the index
     * to what the new relation is inserted for multiports, i.e. the channel
     * index of the new relation should be the same as before for the old
     * relation.
     * 
     * @param kPort Kieler KPort the relation is connected to. Might be null or
     *            invalid if the port is an inner port within the composite
     *            actor.
     * @param kNode Kieler KNode the port belongs to or---if it is an inner
     *            port--- the inner port itself.
     * @param newRelationName The new relation that should be connected.
     * @param oldRelation The old relation that should be replaced. It does not
     *            get deleted at this point.
     * @return the NamedObj to which a link was removed, i.e. either a port or a
     *         relation
     * @exception IllegalActionException Exception may be thrown if unlinking or
     *                linking of a relation fails.
     */
    private NamedObj _replaceRelation(KPort kPort, KNode kNode,
            String newRelationName, Relation oldRelation)
            throws IllegalActionException {
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
            if (index == -1 && port instanceof ComponentPort) {
                linkedRelations = ((ComponentPort) port).insideRelationList();
                outsideLink = false;
                index = linkedRelations.indexOf(oldRelation);
            }
            if (outsideLink) {
                _ptolemyModelUtil._unlinkPort(port.getName(_compositeActor),
                        index);
                // _ptolemyModelUtil._performChangeRequest(_compositeActor);
                _ptolemyModelUtil._linkPort(port.getName(_compositeActor),
                        "relation", newRelationName, index);
            } else { // insideLink
                if (port instanceof ComponentPort) {
                    _ptolemyModelUtil._unlinkPortInside(
                            port.getName(_compositeActor), index);
                    _ptolemyModelUtil._linkPortInside(
                            port.getName(_compositeActor), "relation",
                            newRelationName, index);
                }
            }
            return port;
            // the connection is with an already existing relation
        } else if (sourceRelation != null) {
            if (!oldRelation.equals(sourceRelation)) {
                _ptolemyModelUtil._unlinkRelations(
                        oldRelation.getName(_compositeActor),
                        sourceRelation.getName(_compositeActor));
            }
            _ptolemyModelUtil._link("relation1", sourceRelation.getName(),
                    "relation2", newRelationName);
            return sourceRelation;
        }
        return null;
    }

    /**
     * Report a message to the top window status handler if it is available.
     * 
     * @param message The message to be reported.
     */
    private void _report(String message) {
        if (_top != null) {
            _top.report(message);
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
        if (DEBUG) {
            System.out.print("Store endpoints");
        }
        ActorGraphModel aGraph = (ActorGraphModel) this.getLayoutTarget()
                .getGraphModel();
        boolean allDirectionsSet = false;
        boolean progress = false;
        Set edges = _ptolemyDiva2KielerEdges.keySet();
        while (!allDirectionsSet) {
            allDirectionsSet = true;
            progress = false;
            if (DEBUG) {
                System.out.print(".");
            }
            for (Iterator edgeIter = edges.iterator(); edgeIter.hasNext();) {
                Object edge = edgeIter.next();
                EdgeModel edgeModel = aGraph.getEdgeModel(edge);

                Object simpleEndpoint1 = edgeModel.getHead(edge);
                Object simpleEndpoint2 = edgeModel.getTail(edge);
                Object endpoint1 = aGraph.getSemanticObject(simpleEndpoint1);
                Object endpoint2 = aGraph.getSemanticObject(simpleEndpoint2);

                // see if we have successfully looked at this edge before
                if (_divaEdgeTarget.containsKey(edge)
                        && _divaEdgeSource.containsKey(edge)) {
                    continue;
                }

                // check whether endpoints are source or target ports
                if (endpoint1 instanceof Port) {
                    boolean isInput1 = PtolemyModelUtil
                            ._isInput((Port) endpoint1);
                    // check if we look at inner or outer ports
                    if (simpleEndpoint1 instanceof Location) {
                        isInput1 = !isInput1; // inner input port is regarded as
                        // output
                    }
                    // set endpoints
                    if (isInput1) {
                        _divaEdgeTarget.put(edge, simpleEndpoint1);
                        _divaEdgeSource.put(edge, simpleEndpoint2);
                        progress = true;
                    } else {
                        _divaEdgeTarget.put(edge, simpleEndpoint2);
                        _divaEdgeSource.put(edge, simpleEndpoint1);
                        progress = true;
                    }
                } else if (endpoint2 instanceof Port) {
                    boolean isInput2 = PtolemyModelUtil
                            ._isInput((Port) endpoint2);
                    // check if we look at inner or outer ports
                    if (simpleEndpoint2 instanceof Location) {
                        isInput2 = !isInput2; // inner input port is regarded as
                        // output
                    }
                    // set endpoints
                    if (isInput2) {
                        _divaEdgeTarget.put(edge, simpleEndpoint2);
                        _divaEdgeSource.put(edge, simpleEndpoint1);
                        progress = true;
                    } else {
                        _divaEdgeTarget.put(edge, simpleEndpoint1);
                        _divaEdgeSource.put(edge, simpleEndpoint2);
                        progress = true;
                    }
                } else
                // see if one of the endpoints is source or target of other
                // edges
                if (_divaEdgeTarget.containsValue(simpleEndpoint1)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint2);
                    _divaEdgeSource.put(edge, simpleEndpoint1);
                    progress = true;
                } else if (_divaEdgeTarget.containsValue(simpleEndpoint2)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint1);
                    _divaEdgeSource.put(edge, simpleEndpoint2);
                    progress = true;
                } else if (_divaEdgeSource.containsValue(simpleEndpoint1)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint1);
                    _divaEdgeSource.put(edge, simpleEndpoint2);
                    progress = true;
                } else if (_divaEdgeSource.containsValue(simpleEndpoint2)) {
                    _divaEdgeTarget.put(edge, simpleEndpoint2);
                    _divaEdgeSource.put(edge, simpleEndpoint1);
                    progress = true;
                } else {
                    // now we can't deduce any information about this edge
                    allDirectionsSet = false;
                }
                if (DEBUG && progress) {
                    System.out.print("o");
                }
                // guarantee progress by just setting the direction if it
                // cannot be deduced
                if (!edgeIter.hasNext() && !progress) {
                    _divaEdgeTarget.put(edge, simpleEndpoint1);
                    _divaEdgeSource.put(edge, simpleEndpoint2);
                    if (DEBUG) {
                        System.out.print("O");
                    }
                }
            }
        }
        if (DEBUG) {
            System.out.println("done.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Debug flag that will trigger output of additional information during
     * layout run. With the flag set to true especially the Kieler graph
     * structure will be written to a file on harddisk in order to review the
     * graph later on.
     */
    private static final boolean DEBUG = false;

    /**
     * Default size of a port that will be used in Kieler layout if no explicit
     * size (e.g. copied from Ptolemy port) is given.
     */
    private static final float DEFAULT_PORT_SIZE = 5.0f;

    /**
     * The offset height used by Kieler for inner ports to correct connection
     * anchor.
     */
    private static final float INNER_PORT_HEIGHT_OFFSET = 11.0f;

    /**
     * Minimal distance between nodes. Changing this value will decrease the
     * overall horizontal space consumed by the diagram.
     */
    private static final float MIN_SPACING = 10.0f;

    /**
     * Offset between Kieler KPorts corresponding to a Ptolemy multiport. I.e.
     * the distance between multiple single KPorts.
     */
    private static final float MULTIPORT_OFFSET = 5.0f;

    /**
     * Offset between bottom of Multiport to the first Kieler KPort
     */
    private static final float MULTIPORT_BOTTOM = 4.5f;

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
     * Storage of actual sources of diva edges corresponding to data flow.
     */
    private Map<Object, Object> _divaEdgeSource;

    /**
     * Storage of actual targets of diva edges corresponding to data flow.
     */
    private Map<Object, Object> _divaEdgeTarget;

    /**
     * Flag to indicate whether edge routing shall be applied by insertion of
     * new relation vertices or not.
     */
    private boolean _doApplyEdgeLayout = false;

    /**
     * Flag to indicate whether edge routing shall be applied by annotating
     * relations with bend point information of their connected links.
     */
    private boolean _doApplyEdgeLayoutBendPointAnnotation = false;

    /**
     * Flag to indicate whether all nodes should be placed including unconnected
     * nodes such as attributes (e.g. director, text annotations,...).
     */
    private boolean _doBoxLayout = false;

    /**
     * Map Kieler KEdges to Ptolemy Diva Edges.
     */
    private Map<KEdge, Object> _kieler2PtolemyDivaEdges;

    /**
     * Map Kieler nodes to Diva Nodes. Opposite of _ptolemy2KielerNodes
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

    /**
     * Pointer to Top in order to report the current status.
     */
    private Top _top;

}
