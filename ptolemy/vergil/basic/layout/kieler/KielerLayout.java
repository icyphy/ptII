/* Layout interface between the Ptolemy Diva editor and the KIELER
 * layout library.*/
/*
 @Copyright (c) 2009-2014 The Regents of the University of California.
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

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.gui.Top;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.RelativeLocation;
import ptolemy.moml.Vertex;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorGraphModel.ExternalPortModel;
import ptolemy.vergil.actor.IOPortController;
import ptolemy.vergil.actor.KielerLayoutConnector;
import ptolemy.vergil.actor.PortTerminal;
import ptolemy.vergil.basic.RelativeLocatable;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.kernel.RelativeLinkFigure;
import ptolemy.vergil.modal.FSMGraphModel;
import ptolemy.vergil.toolbox.SnapConstraint;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.core.alg.BasicProgressMonitor;
import de.cau.cs.kieler.core.alg.DefaultFactory;
import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.alg.InstancePool;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KLabel;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.core.util.Pair;
import de.cau.cs.kieler.kiml.AbstractLayoutProvider;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.options.EdgeLabelPlacement;
import de.cau.cs.kieler.kiml.options.LayoutOptions;
import de.cau.cs.kieler.kiml.options.PortConstraints;
import de.cau.cs.kieler.kiml.options.PortSide;
import de.cau.cs.kieler.kiml.options.SizeConstraint;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klay.layered.LayeredLayoutProvider;
import diva.canvas.CanvasComponent;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.connector.AbstractConnector;
import diva.canvas.toolbox.LabelFigure;
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
 * </p>
 * <p><b>
 * KIELER - Kiel Integrated Environment for Layout for the Eclipse
 * RichClientPlatform
 * </b></p>
 * <p>
 * The KIELER project tries to enhance graphical modeling pragmatics. Next to
 * higher level solutions (meta layout, view management, structure-based editing,
 * etc.) developed for Eclipse models, it also implements custom layout algorithms.
 * </p>
 * <p>
 * This class interfaces a standalone KIELER layout algorithm for actor oriented
 * port based graphical diagrams with a Ptolemy diagram.
 * </p>
 * <p>
 * While KIELER is mainly developed for an Eclipse environment, most algorithms
 * are also available standalone and can be used in a non-Eclipse environment.
 * This class is an approach to leverage this by employing the algorithms within Ptolemy.
 * Two standalone external libraries are required, one containing KIELER classes
 * and a small subset of the Eclipse Modeling Framework (EMF), the other
 * containing the Google Guava library, which is used as utility.
 * </p>
 * <p>
 * Calling the layout() method will create a new KIELER graph data structure, run
 * KIELER layout algorithms on it and augment it with resulting layout
 * information (locations and sizes of nodes, bend points of connections). Then
 * this layout is applied to the Ptolemy model. Moving of nodes in Ptolemy is
 * done via adding or changing location attributes.
 * </p>
 * <p>
 * Setting bend points was not supported in Ptolemy. Ptolemy's built-in
 * connection routing does not consider obstacle avoidance, hence overlaps
 * with other nodes and connections might appear. In order to gap this problem,
 * the actual drawing of the connections is performed by {@link KielerLayoutConnector}
 * instead of the standard Manhattan connector.
 * This KielerLayout stores bend points of connections
 * persistently in a Ptolemy model via {@link LayoutHint} attributes
 * attached to relations. The KielerLayoutConnector then reads these
 * attributes and routes the edges accordingly. If the bend points are not
 * valid anymore, the attribute is removed and layout has to be performed again.
 * </p>
 * @author Hauke Fuhrmann (<a href="mailto:haf@informatik.uni-kiel.de">haf</a>),
 *         Christian Motika (<a href="mailto:cmot@informatik.uni-kiel.de">cmot</a>),
 *         Miro Sp&ouml;nemann (<a href="mailto:msp@informatik.uni-kiel.de">msp</a>) ,
 *         Christoph Daniel Schulze (<a href="mailto:cds@informatik.uni-kiel.de">cds</a>)
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class KielerLayout extends AbstractGlobalLayout {

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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Layout the given composite. Main entry point for the layout action.
     * Create a KIELER KGraph data structure corresponding to the Ptolemy model,
     * instantiate a KIELER layout algorithm (AbstractLayoutProvider) and run
     * its doLayout() method on the KGraph. The KGraph is augmented with
     * layout information (position and sizes of objects and bend points for
     * connections). This information is then reapplied to the ptolemy model by
     * stating MoMLChangeRequests with location attributes for nodes.
     * Connection bend points are applied using {@link LayoutHint}s.
     *
     * @param composite the container of the diagram in terms of a GraphModel.
     */
    @Override
    public void layout(Object composite) {
        KielerLayoutConnector.setLayoutInProgress(true);

        // some variables for time statistics
        long overallTime = System.currentTimeMillis();

        _report("Performing KIELER layout... ");
        long graphOverhead = overallTime;

        // Create a KGraph for the KIELER layout algorithm.
        KNode parentNode = KimlUtil.createInitializedNode();
        KShapeLayout parentLayout = parentNode.getData(KShapeLayout.class);
        if (_top != null) {
            Dimension contentSize = _top.getContentSize();
            parentLayout.setWidth(contentSize.width);
            parentLayout.setHeight(contentSize.height);
        }

        try {
            // Configure the layout algorithm by annotating the graph.
            Parameters parameters = new Parameters(_compositeEntity);
            parameters.configureLayout(parentLayout, getLayoutTarget()
                    .getGraphModel());

            // Now read Ptolemy model and fill the KGraph with the model data.
            _createGraph(composite, parentNode);
            graphOverhead = System.currentTimeMillis() - graphOverhead;

            // Create the layout provider which performs the actual layout algorithm.
            InstancePool<AbstractLayoutProvider> layouterPool = _getLayouterPool();
            AbstractLayoutProvider layoutProvider = layouterPool.fetch();

            // Create a progress monitor for execution time measurement.
            IKielerProgressMonitor progressMonitor = new BasicProgressMonitor();

            // Perform layout on the created graph.
            layoutProvider.doLayout(parentNode, progressMonitor);

            // Write to XML file for debugging layout (requires XMI resource factory).
            if (DEBUG) {
                KielerGraphUtil._writeToFile(parentNode);
            }

            // Set initial position as the bounding box of the hierarchical node.
            KVector offset = KielerGraphUtil._getUpperLeftCorner(parentNode);
            parentLayout.setXpos(parentLayout.getXpos() - (float) offset.x);
            parentLayout.setYpos(parentLayout.getYpos() - (float) offset.y);

            long momlRequestOverhead = System.currentTimeMillis();

            // Apply layout to ptolemy model.
            _applyLayout(parentNode);

            momlRequestOverhead = System.currentTimeMillis()
                    - momlRequestOverhead;
            overallTime = System.currentTimeMillis() - overallTime;
            _report("KIELER layout done in " + overallTime
                    + "ms (Graph conversion " + graphOverhead
                    + "ms, Algorithm "
                    + Math.round(progressMonitor.getExecutionTime() * 1000)
                    + "ms, MoMLChanges " + momlRequestOverhead + "ms).");

            // Release the layout provider back to the instance pool for later reuse.
            layouterPool.release(layoutProvider);

        } catch (IllegalActionException exception) {
            // Throw some Ptolemy runtime exception.
            throw new InternalErrorException(exception);
        }

        KielerLayoutConnector.setLayoutInProgress(false);
    }

    /**
     * Set the Ptolemy Model that contains the graph that is to be layouted. The
     * layouter will require access to the Ptolemy model because the lower level
     * Diva abstraction does not consider certain properties required by the
     * KIELER layouter such as port positions.
     *
     * @param model The parent composite entity which internal diagram shall be layouted.
     */
    public void setModel(CompositeEntity model) {
        this._compositeEntity = model;
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
    ////                         protected methods                 ////

    /**
     * Return a pool for layout provider instances that can be reused in subsequent layout
     * runs. New instances can be fetched from the pool and should be released back to
     * the pool after use.
     *
     * @return a layout provider pool
     */
    protected static synchronized InstancePool<AbstractLayoutProvider> _getLayouterPool() {
        if (_layoutProviderPool == null) {
            _layoutProviderPool = new InstancePool<AbstractLayoutProvider>(
                    new DefaultFactory(LayeredLayoutProvider.class));
        }
        return _layoutProviderPool;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Traverse a composite KNode containing corresponding KIELER nodes, ports
     * and edges for the Ptolemy model and apply all layout information
     * contained by it back to the Ptolemy model. Do most changes to the Ptolemy
     * model via MoMLChangeRequests. Set location attributes for all visible
     * Ptolemy nodes.
     * Optionally route edges by inserting {@link LayoutHint} attributes.
     *
     * @param parentNode The KIELER graph object containing all layout information
     *            to apply to the Ptolemy model
     * @exception IllegalActionException if routing of edges fails.
     */
    private void _applyLayout(KNode parentNode) throws IllegalActionException {
        // Create a special change request to apply the computed layout to the model.
        ApplyLayoutRequest layoutRequest = new ApplyLayoutRequest(
                _compositeEntity);

        // Apply node layout.
        for (KNode knode : parentNode.getChildren()) {
            KShapeLayout nodeLayout = knode.getData(KShapeLayout.class);
            KVector nodePos = nodeLayout.createVector();
            Object divaNode = _kieler2ptolemyDivaNodes.get(knode);
            if (divaNode instanceof Location) {
                Locatable location = (Location) divaNode;

                // Transform coordinate systems.
                KimlUtil.toAbsolute(nodePos, parentNode);
                _kNode2Ptolemy(nodePos, divaNode, location);

                // Calculate the snap-to-grid coordinates.
                double[] snapToGridNodePoint = SnapConstraint.constrainPoint(
                        nodePos.x, nodePos.y);

                // Include the new location in the request.
                layoutRequest.addLocation(location, snapToGridNodePoint[0],
                        snapToGridNodePoint[1]);
            }
        }

        GraphModel graphModel = getLayoutTarget().getGraphModel();
        if (graphModel instanceof ActorGraphModel) {
            // apply edge layout - bend points with layout hints
            for (Pair<KEdge, Link> entry : _edgeList) {
                _applyEdgeLayoutBendPointAnnotation(entry.getFirst(),
                        entry.getSecond(), layoutRequest);
            }
        } else if (graphModel instanceof FSMGraphModel) {
            // apply edge layout - one single point for specifying a curve
            for (Pair<KEdge, Link> entry : _edgeList) {
                _applyEdgeLayoutCurve(entry.getFirst(), entry.getSecond(),
                        layoutRequest);
            }
        }

        // Let the composite actor execute the actual changes.
        _compositeEntity.requestChange(layoutRequest);
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
     * @param kedge The KIELER edge that holds the precomputed layout
     *            information, i.e. bend point positions
     * @exception IllegalActionException Exception will be thrown if replacing
     *                of original relation is not possible, i.e. if unlink() or
     *                link() methods fail.
     */
    private void _applyEdgeLayoutBendPointAnnotation(KEdge kedge, Link link,
            ApplyLayoutRequest layoutRequest) throws IllegalActionException {
        List<KPoint> bendPoints = kedge.getData(KEdgeLayout.class)
                .getBendPoints();

        // Translate bend points into an array of doubles for the layout hint attribute.
        double[] layoutHintBendPoints = new double[bendPoints.size() * 2];
        int index = 0;
        KNode parentNode = KielerGraphUtil._getParent(kedge);
        for (KPoint relativeKPoint : bendPoints) {
            KVector kpoint = relativeKPoint.createVector();
            KimlUtil.toAbsolute(kpoint, parentNode);

            // calculate the snap-to-grid coordinates
            double[] snapToGridBendPoint = SnapConstraint.constrainPoint(
                    kpoint.x, kpoint.y);

            layoutHintBendPoints[index] = snapToGridBendPoint[0];
            layoutHintBendPoints[index + 1] = snapToGridBendPoint[1];
            index += 2;
        }

        Relation relation = link.getRelation();
        NamedObj head = (NamedObj) link.getHead();
        NamedObj tail = (NamedObj) link.getTail();
        // Determine correct direction of the edge.
        if (head != _divaEdgeSource.get(link)) {
            layoutRequest.addConnection(relation, tail, head,
                    layoutHintBendPoints);
        } else {
            layoutRequest.addConnection(relation, head, tail,
                    layoutHintBendPoints);
        }
    }

    private void _applyEdgeLayoutCurve(KEdge kedge, Link link,
            ApplyLayoutRequest layoutRequest) {
        Relation relation = link.getRelation();
        // Don't process any self-loops, since that would cause headaches.
        if (relation instanceof Transition && link.getHead() != link.getTail()) {
            KEdgeLayout edgeLayout = kedge.getData(KEdgeLayout.class);
            List<KPoint> bendPoints = edgeLayout.getBendPoints();

            KShapeLayout sourceLayout = kedge.getSource().getData(
                    KShapeLayout.class);
            double sourcex = sourceLayout.getXpos() + sourceLayout.getWidth()
                    / 2;
            double sourcey = sourceLayout.getYpos() + sourceLayout.getHeight()
                    / 2;
            KShapeLayout targetLayout = kedge.getTarget().getData(
                    KShapeLayout.class);
            double targetx = targetLayout.getXpos() + targetLayout.getWidth()
                    / 2;
            double targety = targetLayout.getYpos() + targetLayout.getHeight()
                    / 2;

            // Determine a reference point for drawing the curve.
            double exitAngle = 0;
            double refx = 0, refy = 0;
            if (bendPoints.isEmpty()) {
                refx = (edgeLayout.getSourcePoint().getX() + edgeLayout
                        .getTargetPoint().getX()) / 2;
                refy = (edgeLayout.getSourcePoint().getY() + edgeLayout
                        .getTargetPoint().getY()) / 2;
            } else {
                refx = bendPoints.get(0).getX();
                refy = bendPoints.get(0).getY();
            }

            // Take the angular difference between the reference point and
            // the target point as seen from the source.
            double targetth = Math.atan2(targety - sourcey, targetx - sourcex);
            double meanth = Math.atan2(refy - sourcey, refx - sourcex);
            exitAngle = meanth - targetth;

            // Fit the angle into the bounds of [-pi, pi]
            if (exitAngle > Math.PI) {
                exitAngle -= 2 * Math.PI;
            } else if (exitAngle < -Math.PI) {
                exitAngle += 2 * Math.PI;
            }

            layoutRequest.addCurve((Transition) relation, exitAngle);
        }
    }

    /**
     * Creates a graph for the KIELER API from a Ptolemy model. Will traverse
     * the low level GraphModel given by the composite and record all found
     * elements in the mapping fields of this object that keep a mapping between
     * Ptolemy/Diva objects and KIELER objects. New KIELER objects (KEdge,
     * KNode, KPort) are created for their respective Ptolemy counterparts and
     * initialized with the initial sizes and positions and are put in a
     * composite KNode (the graph KIELER will perform the layout on later). To
     * obtain the right mappings, multiple abstraction levels of Ptolemy are
     * considered here: Diva, as this was the intended original way to do
     * automatic layout (e.g. by GlobalAbstractLayout) and Ptolemy, since Diva
     * lacks certain concepts that are relevant for a proper layout, such
     * as exact port locations for considering port constraints in the
     * model, which are supported by KIELER.
     *
     * @param composite The GraphModel composite object to retrieve the model
     *            information from
     * @param parentNode KIELER subgraph to receive all connected
     *            model elements
     */
    private void _createGraph(Object composite, KNode parentNode) {
        _kieler2ptolemyDivaNodes = HashBiMap.create();
        _ptolemy2KielerPorts = LinkedListMultimap.create();
        _divaEdgeSource = Maps.newHashMap();
        _divaEdgeTarget = Maps.newHashMap();
        _edgeList = Lists.newLinkedList();
        KShapeLayout parentLayout = parentNode.getData(KShapeLayout.class);

        // Determine whether to include unconnected nodes.
        boolean doBoxLayout = parentLayout.getProperty(Parameters.DECORATIONS);

        // On-the-fly find upper left corner for bounding box of parent node.
        float globalX = Float.MAX_VALUE, globalY = Float.MAX_VALUE;

        // Traverse the ptolemy graph.
        GraphModel graphModel = getLayoutTarget().getGraphModel();
        ExternalPortModel externalPortModel = null;
        if (graphModel instanceof ActorGraphModel) {
            externalPortModel = ((ActorGraphModel) graphModel)
                    .getExternalPortModel();
        }
        List<Link> unprocessedEdges = new LinkedList<Link>();
        List<NamedObj> unprocessedRelatives = new LinkedList<NamedObj>();

        // Process nodes.
        for (Iterator iterator = graphModel.nodes(composite); iterator
                .hasNext();) {
            Object node = iterator.next();
            if (!(node instanceof Locatable)) {
                continue;
            }
            Iterator portIter = null;

            // Here we get the corresponding Ptolemy object.
            // This breaks with Ptolemy/Diva abstraction; for now we need
            // the ptolemy actor to get the ports and port positions
            // and to distinguish actors and relation vertices.
            NamedObj semanticNode = (NamedObj) graphModel
                    .getSemanticObject(node);

            if (doBoxLayout || PtolemyModelUtil._isConnected(semanticNode)) {
                // Temporary variable for the new KNode corresponding to one of
                // the following cases depending on what the semantic object is.
                KNode knode = null;

                // Handle actors, text, and directors.
                if (semanticNode instanceof Actor
                        || semanticNode instanceof Attribute) {

                    // Create a KIELER node for a ptolemy node.
                    knode = _createKNode(node, semanticNode);

                    // Handle the ports of this node.
                    if (semanticNode instanceof Actor
                            && semanticNode instanceof Entity) {
                        Actor actor = (Actor) semanticNode;
                        List<Port> inputs = actor.inputPortList();
                        List<Port> outputs = actor.outputPortList();

                        // create ports
                        _createKPorts(knode, inputs);
                        _createKPorts(knode, outputs);
                        portIter = graphModel.nodes(node);
                    } else if (semanticNode instanceof RelativeLocatable) {
                        unprocessedRelatives.add(semanticNode);
                    }
                }

                // Handle relation vertices.
                else if (semanticNode instanceof Relation) {
                    // Regard a relation vertex as a KIELER KNode.
                    knode = _createKNodeForVertex((Vertex) node);
                    portIter = Iterators.singletonIterator(node);
                }

                // Handle internal ports.
                else if (semanticNode instanceof ComponentPort) {
                    knode = _createKNodeForPort(node,
                            (ComponentPort) semanticNode);
                    portIter = Iterators.singletonIterator(node);
                }

                // Handle modal model states.
                else if (semanticNode instanceof State) {
                    knode = _createKNodeForState(node, (State) semanticNode);
                    portIter = Iterators.singletonIterator(node);
                }

                // Now do some common bookkeeping for all kinds of nodes.
                if (knode != null) {
                    knode.setParent(parentNode);
                    // Get check bounds for global bounding box.
                    KShapeLayout layout = knode.getData(KShapeLayout.class);
                    if (layout.getXpos() < globalX) {
                        globalX = layout.getXpos();
                    }
                    if (layout.getYpos() < globalY) {
                        globalY = layout.getYpos();
                    }

                    // Store node for later applying layout back.
                    _kieler2ptolemyDivaNodes.put(knode, (Locatable) node);
                }
            }

            if (portIter != null) {
                while (portIter.hasNext()) {
                    Object divaPort = portIter.next();
                    // Iterate all outgoing edges.
                    Iterator edgeIterator;
                    if (semanticNode instanceof Port
                            && externalPortModel != null) { // internal ports
                        edgeIterator = externalPortModel.outEdges(divaPort);
                    } else {
                        edgeIterator = graphModel.outEdges(divaPort);
                    }
                    while (edgeIterator.hasNext()) {
                        Object next = edgeIterator.next();
                        if (next instanceof Link) {
                            unprocessedEdges.add((Link) next);
                        }
                    }
                }
            }
        }

        // Create KIELER edges for Diva edges.
        if (graphModel instanceof ActorGraphModel) {
            _storeEndpoints(unprocessedEdges);
        }
        for (Link divaEdge : unprocessedEdges) {
            _createKEdge(divaEdge);
        }

        // Create edges for associations of relative locatables to their reference objects.
        for (NamedObj relativeObj : unprocessedRelatives) {
            _createKEdgeForAttribute(relativeObj);
        }

        // Set graph offset.
        parentLayout.setXpos(globalX);
        parentLayout.setYpos(globalY);
    }

    /**
     * Create a KIELER edge for a Ptolemy Diva edge object. The KEdge will be
     * setup between either two ports or relation vertices or mixed. Hence the
     * KEdge corresponds more likely to a Ptolemy link than a relation. Diva edges
     * have no direction related to the flow of data in Ptolemy. However, KIELER
     * uses a directed graph to perform layout and so a meaningful direction
     * should be set in the KEdge. This direction will be approximated by doing
     * a tree search beginning on both endpoints of the Diva edge. Whenever
     * either of the endpoints is connected to a source port, this will be the
     * source of the KEdge and determine its direction.
     *
     * The newly created edge is stored with the corresponding Diva edge in the
     * global maps _ptolemyDiva2KielerEdges, _kieler2PtolemyDivaEdges, such that
     * the {@link #_applyLayout(KNode)} method will be able to reapply the
     * layout.
     *
     * @param divaEdge The Ptolemy diva edge object for which to create a new KEdge.
     */
    private void _createKEdge(Link divaEdge) {
        KEdge kedge = KimlUtil.createInitializedEdge();

        Object source = _divaEdgeSource.get(divaEdge);
        if (source == null) {
            source = divaEdge.getTail();
        }
        Object target = _divaEdgeTarget.get(divaEdge);
        if (target == null) {
            target = divaEdge.getHead();
        }

        KPort kSourcePort = _getPort(source, divaEdge.getRelation());
        if (kSourcePort != null) {
            kedge.setSourcePort(kSourcePort);
            kSourcePort.getEdges().add(kedge);
            kedge.setSource(kSourcePort.getNode());
        } else {
            // Edge is not connected to a port.
            kedge.setSource(_kieler2ptolemyDivaNodes.inverse().get(source));
        }
        KPort kTargetPort = _getPort(target, divaEdge.getRelation());
        if (kTargetPort != null) {
            kedge.setTargetPort(kTargetPort);
            kTargetPort.getEdges().add(kedge);
            kedge.setTarget(kTargetPort.getNode());
        } else {
            // Edge is not connected to a port.
            kedge.setTarget(_kieler2ptolemyDivaNodes.inverse().get(target));
        }

        // Set source and target point so they are not (0, 0).
        KEdgeLayout edgeLayout = kedge.getData(KEdgeLayout.class);
        if (source instanceof Locatable) {
            double[] pos = ((Locatable) source).getLocation();
            edgeLayout.getSourcePoint().setX((float) pos[0]);
            edgeLayout.getSourcePoint().setY((float) pos[1]);
        }
        if (target instanceof Locatable) {
            double[] pos = ((Locatable) target).getLocation();
            edgeLayout.getTargetPoint().setX((float) pos[0]);
            edgeLayout.getTargetPoint().setY((float) pos[1]);
        }

        // Add the edge to the list.
        _edgeList.add(new Pair<KEdge, Link>(kedge, divaEdge));

        // Create a label for the edge.
        Object figure = getLayoutTarget().getVisualObject(divaEdge);
        if (figure instanceof AbstractConnector) {
            LabelFigure labelFigure = ((AbstractConnector) figure)
                    .getLabelFigure();
            if (labelFigure != null) {
                KLabel label = KimlUtil.createInitializedLabel(kedge);
                label.setText(labelFigure.getString());
                KShapeLayout labelLayout = label.getData(KShapeLayout.class);
                labelLayout.setProperty(LayoutOptions.EDGE_LABEL_PLACEMENT,
                        EdgeLabelPlacement.CENTER);
                Rectangle2D bounds = labelFigure.getBounds();
                labelLayout.setWidth((float) bounds.getWidth());
                labelLayout.setHeight((float) bounds.getHeight());
                labelLayout
                        .setXpos((edgeLayout.getSourcePoint().getX() + edgeLayout
                                .getTargetPoint().getX()) / 2);
                labelLayout
                        .setYpos((edgeLayout.getSourcePoint().getY() + edgeLayout
                                .getTargetPoint().getY()) / 2);
                kedge.getLabels().add(label);
            }
        }
    }

    /**
     * Create a dummy edge for an attribute that is relative locatable. The edge
     * will only be used to indicate the association between the attribute and
     * its reference object.
     *
     * @param attribute the attribute for which to create a dummy edge
     */
    private void _createKEdgeForAttribute(NamedObj attribute) {
        Locatable source = PtolemyModelUtil._getLocation(attribute);
        if (source instanceof RelativeLocation) {
            NamedObj referenceObj = PtolemyModelUtil
                    ._getReferencedObj((RelativeLocation) source);
            if (referenceObj != null) {
                Locatable target = PtolemyModelUtil._getLocation(referenceObj);
                KNode sourceNode = _kieler2ptolemyDivaNodes.inverse().get(
                        source);
                KNode targetNode = _kieler2ptolemyDivaNodes.inverse().get(
                        target);
                if (sourceNode != null && targetNode != null) {
                    // Create a dummy edge to connect the comment box to its reference.
                    KEdge newEdge = KimlUtil.createInitializedEdge();
                    newEdge.setSource(sourceNode);
                    newEdge.setTarget(targetNode);

                    KEdgeLayout edgeLayout = newEdge.getData(KEdgeLayout.class);
                    double[] sourcePos = source.getLocation();
                    edgeLayout.getSourcePoint().setX((float) sourcePos[0]);
                    edgeLayout.getSourcePoint().setY((float) sourcePos[1]);
                    double[] targetPos = target.getLocation();
                    edgeLayout.getTargetPoint().setX((float) targetPos[0]);
                    edgeLayout.getTargetPoint().setY((float) targetPos[1]);
                }
            }
        }
    }

    /**
     * Create a new KIELER KNode corresponding to a Ptolemy diva node and its
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
     * @return The initialized KIELER KNode
     */
    private KNode _createKNode(Object node, NamedObj semanticNode) {
        Rectangle2D bounds;
        if (semanticNode instanceof RelativeLocatable) {
            // RelativeLocatables may have a dashed line to show which actor
            // they are attached to. This line must not be part of the size
            // calculation, so we calculate the size of the object manually
            // without taking the line into account
            Figure figure = (Figure) getLayoutTarget().getVisualObject(node);

            // The figure should be a composite figure, but let's be sure
            if (figure instanceof CompositeFigure) {
                CompositeFigure compFigure = (CompositeFigure) figure;

                bounds = new Rectangle2D.Double();
                bounds.add(compFigure.getBackgroundFigure().getBounds());

                // Iterate over the composite figure's component, adding the
                // bounds of each to arrive at the final bounds without dashed
                // line (RelativeLinkFigure)
                Iterator parts = compFigure.figures();
                while (parts.hasNext()) {
                    Figure part = (Figure) parts.next();

                    if (!(part instanceof RelativeLinkFigure)) {
                        bounds.add(part.getBounds());
                    }
                }
            } else {
                // It's not a composite figure, so use figure's bounds
                bounds = getLayoutTarget().getBounds(node);
            }
        } else {
            bounds = getLayoutTarget().getBounds(node);
        }

        // Create new node in KIELER graph and apply the initial size and position
        KNode knode = KimlUtil.createInitializedNode();
        KShapeLayout nodeLayout = knode.getData(KShapeLayout.class);
        nodeLayout.setWidth((float) bounds.getWidth());
        nodeLayout.setHeight((float) bounds.getHeight());
        nodeLayout.setXpos((float) bounds.getMinX());
        nodeLayout.setYpos((float) bounds.getMinY());
        nodeLayout.setProperty(LayoutOptions.SIZE_CONSTRAINT,
                SizeConstraint.FIXED);
        if (semanticNode instanceof Attribute) {
            nodeLayout.setProperty(LayoutOptions.COMMENT_BOX, true);
        } else {
            nodeLayout.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                    PortConstraints.FIXED_POS);
        }

        // set the node label
        KLabel label = KimlUtil.createInitializedLabel(knode);
        label.setText(semanticNode.getDisplayName());
        KShapeLayout labelLayout = label.getData(KShapeLayout.class);
        labelLayout.setWidth((float) bounds.getWidth() - 2);
        labelLayout.setHeight(10);
        labelLayout.setXpos(1);
        labelLayout.setYpos(1);
        knode.getLabels().add(label);

        // Draw the director always as first element.
        if (semanticNode instanceof Director) {
            nodeLayout.setProperty(LayoutOptions.PRIORITY, 1);
        }

        return knode;
    }

    /**
     * Create a KIELER KNode for a Ptolemy inner port. That is the graphical
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
     * @return A new KIELER node corresponding to the Ptolemy inner port.
     */
    private KNode _createKNodeForPort(Object divaLocation, ComponentPort port) {
        KNode knode = KimlUtil.createInitializedNode();
        KShapeLayout layout = knode.getData(KShapeLayout.class);
        layout.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                PortConstraints.FIXED_POS);

        Rectangle2D figureBounds = getLayoutTarget().getBounds(divaLocation);
        Rectangle2D shapeBounds = figureBounds;
        // Try to find more specific bounds of the shape that do not include the name label.
        Object obj = getLayoutTarget().getVisualObject(divaLocation);
        if (obj instanceof PortTerminal) {
            PortTerminal portVisual = (PortTerminal) obj;
            shapeBounds = portVisual.getShape().getBounds();
        }
        // Set alignment offset in order to set right height.
        layout.setHeight((float) figureBounds.getHeight()
                + INNER_PORT_HEIGHT_OFFSET);
        layout.setWidth((float) figureBounds.getWidth());
        layout.setProperty(LayoutOptions.SIZE_CONSTRAINT, SizeConstraint.FIXED);
        layout.setXpos((float) figureBounds.getMinX());
        layout.setYpos((float) figureBounds.getMinY());

        // Create KIELER ports to specify anchor points for the layouter.
        // These constants are black magic, gotten by trial and error.
        KVector portBase = new KVector(MULTIPORT_INNER_OFFSET);
        portBase.y += figureBounds.getHeight() - shapeBounds.getHeight();
        int direction = PtolemyModelUtil._getExternalPortDirection(port);
        switch (direction) {
        case SwingConstants.NORTH:
            portBase.x += shapeBounds.getWidth() / 2;
            break;
        case SwingConstants.SOUTH:
            portBase.x += shapeBounds.getWidth() / 2;
            portBase.y += shapeBounds.getHeight();
            break;
        case SwingConstants.EAST:
            portBase.x += shapeBounds.getWidth();
            portBase.y += shapeBounds.getHeight() / 2;
            break;
        default:
            portBase.y += shapeBounds.getHeight() / 2;
        }

        // Create a port for each incoming relation and set an order.
        List relations = port.insideRelationList();
        int maxIndex = relations.size() - 1;
        for (int index = 0; index < relations.size(); index++) {
            KPort kPort = KimlUtil.createInitializedPort();
            KShapeLayout portLayout = kPort.getData(KShapeLayout.class);
            portLayout.setHeight(DEFAULT_PORT_SIZE);
            portLayout.setWidth(DEFAULT_PORT_SIZE);
            KVector offset = _getMultiportOffsets(port, portLayout, index,
                    maxIndex, false);
            portLayout.setXpos((float) (portBase.x + offset.x));
            portLayout.setYpos((float) (portBase.y + offset.y)
                    - portLayout.getHeight() / 2);
            kPort.setNode(knode);
            _ptolemy2KielerPorts.put(port, kPort);
        }
        return knode;
    }

    /**
     * Create a KIELER node for a Ptolemy Vertex. Vertices of Ptolemy can be
     * handles as usual KNodes in KIELER (an alternative would be to handle them
     * as connection bend points).
     *
     * @param vertex The Ptolemy vertex for which to create a KNode
     * @return An initialized KNode
     */
    private KNode _createKNodeForVertex(Vertex vertex) {
        Rectangle2D bounds = getLayoutTarget().getBounds(vertex);
        KNode knode = KimlUtil.createInitializedNode();
        KShapeLayout nodeLayout = knode.getData(KShapeLayout.class);
        nodeLayout.setHeight((float) bounds.getHeight());
        nodeLayout.setWidth((float) bounds.getWidth());
        nodeLayout.setXpos((float) bounds.getMinX());
        nodeLayout.setYpos((float) bounds.getMinY());
        nodeLayout.setProperty(LayoutOptions.SIZE_CONSTRAINT,
                SizeConstraint.FIXED);
        nodeLayout.setProperty(LayoutOptions.HYPERNODE, true);
        return knode;
    }

    /**
     * Create a KIELER node for a Ptolemy State.
     *
     * @param node The Diva node object
     * @param state The Ptolemy state for which to create a KNode
     * @return An initialized KNode
     */
    private KNode _createKNodeForState(Object node, State state) {
        Rectangle2D bounds = getLayoutTarget().getBounds(node);
        KNode knode = KimlUtil.createInitializedNode();
        KShapeLayout nodeLayout = knode.getData(KShapeLayout.class);
        nodeLayout.setHeight((float) bounds.getHeight());
        nodeLayout.setWidth((float) bounds.getWidth());
        nodeLayout.setXpos((float) bounds.getMinX());
        nodeLayout.setYpos((float) bounds.getMinY());
        nodeLayout.setProperty(LayoutOptions.SIZE_CONSTRAINT,
                SizeConstraint.FIXED);
        nodeLayout.setProperty(LayoutOptions.PORT_CONSTRAINTS,
                PortConstraints.FREE);

        KLabel label = KimlUtil.createInitializedLabel(knode);
        label.setText(state.getDisplayName());
        KShapeLayout labelLayout = label.getData(KShapeLayout.class);
        labelLayout.setWidth(nodeLayout.getWidth() - 2);
        labelLayout.setHeight(nodeLayout.getHeight() - 2);
        labelLayout.setXpos(1);
        labelLayout.setYpos(1);
        knode.getLabels().add(label);
        return knode;
    }

    /**
     * Create a KIELER KPort corresponding to a Ptolemy Port. Set the size and
     * position (relative to parent) and the direction of the port in the KPort
     * layout information. Since KIELER does not explicitly support multiports as
     * Ptolemy, this is emulated by creating multiple distinct ports with a
     * little offset each. Create only one node. For multiports call this method
     * multiple times with changed parameters.
     *
     * The newly created port is stored with the corresponding ptolemy port in
     * the global maps _kieler2PtolemyPorts, _ptolemy2KielerPorts, such that the
     * {@link #_applyLayout(KNode)} method will be able to reapply the layout.
     *
     * @param knode The parent KNode of the new port
     * @param port The corresponding Ptolemy port (might be a multiport)
     * @param index Index of the KPort corresponding to a multiport
     * @param maxIndex Width of the multiport, i.e. the number of connected
     *            edges to that port.
     * @param size Custom size (same for width and height) for a port that will
     *            be used instead of the real Ptolemy port size. If this value
     *            is negative, the original Ptolemy sizes are used.
     */
    private void _createKPort(KNode knode, Port port, int index, int maxIndex,
            float size) {
        // Create a new KIELER port and initialize its layout.
        KPort kport = KimlUtil.createInitializedPort();
        knode.getPorts().add(kport);
        KShapeLayout kportlayout = kport.getData(KShapeLayout.class);
        kportlayout.setHeight(DEFAULT_PORT_SIZE);
        kportlayout.setWidth(DEFAULT_PORT_SIZE);

        // Set port side and calculate actual offset.
        KVector offset = _getMultiportOffsets(port, kportlayout, index,
                maxIndex, true);

        // Try to set actual layout (size and position)
        Object portObject = getLayoutTarget().getVisualObject(port);
        if (portObject instanceof PortTerminal) {
            // Get visual Diva figure of port.
            PortTerminal portFigure = (PortTerminal) portObject;
            // Get bounds of the port figure (= relative to center of actor
            // symbol here given by referenceLocation).
            Rectangle2D portBounds = portFigure.getBounds();
            // Get the parent Diva figure which is the whole composite consisting of
            // the actor icon and its name, which might be arbitrary large.
            CanvasComponent parent = portFigure.getParent();
            if (parent instanceof CompositeFigure) {
                CompositeFigure parentFigure = (CompositeFigure) parent;

                AffineTransform parentTransform = parentFigure
                        .getTransformContext().getTransform();
                Point2D.Double portLocation = new Point2D.Double(
                        portBounds.getMinX(), portBounds.getMinY());
                Point2D.Double transformedLocation = new Point2D.Double();
                parentTransform.transform(portLocation, transformedLocation);
                // Calculate coordinates relative to the KIELER nodes top left from absolutes.
                double width = portBounds.getWidth();
                double height = portBounds.getHeight();
                double x = transformedLocation.getX()
                        - parentFigure.getBounds().getMinX() + offset.x;
                double y = transformedLocation.getY()
                        - parentFigure.getBounds().getMinY() + offset.y;
                kportlayout.setXpos((float) x);
                kportlayout.setYpos((float) y);
                // No valid size given -> use diagram port size
                if (size < 0) {
                    kportlayout.setWidth((float) width);
                    kportlayout.setHeight((float) height);
                } else {
                    // If we want to use some custom port size, the new coordinates
                    // need to be adapted to the new size.
                    Rectangle2D newPortBounds = new Rectangle2D.Double();
                    newPortBounds.setRect(x, y, width, height);
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
        // Put ports in global maps for later use.
        _ptolemy2KielerPorts.put(port, kport);
    }

    /**
     * Create KIELER ports (KPort) for a KIELER node (KNode) given a list of
     * Ptolemy Port objects and a port type (incoming, outgoing). The new KPorts
     * are initialized in terms of size and position from the Ptolemy
     * counterparts and attached to the corresponding KNode and registered in
     * the mapping fields of this object.
     *
     * For Ptolemy multiports with multiple connections, multiple KIELER KPorts
     * are created with slightly offset location. Hence the layouter can also
     * consider the location for connection crossing minimization. Hence
     * one Ptolemy port may correspond to multiple KIELER KPorts.
     *
     * @param knode The KNode to create KIELER ports for.
     * @param ports The Ptolemy ports counterparts for which to create KIELER
     *            ports.
     */
    private void _createKPorts(KNode knode, List<Port> ports) {
        for (Port port : ports) {
            // Handle multiports for inputs.
            if (port.linkedRelationList().size() > 1) {
                // Create a port for each incoming relation and set an order.
                List relations = port.linkedRelationList();
                int maxIndex = relations.size() - 1;
                for (int index = 0; index < relations.size(); index++) {
                    _createKPort(knode, port, index, maxIndex,
                            DEFAULT_PORT_SIZE);
                }
            } else {
                // If not a multiport, just create one port.
                _createKPort(knode, port, 0, 0, -1);
            }
        }
    }

    /**
     * Get a KIELER KPort for a corresponding Ptolemy object, i.e. a Port or a
     * relation Vertex. If the input is a Vertex, it is determined which of the
     * two KPorts of the corresponding KNode is returned (since in KIELER a Vertex
     * is represented by one node with one input and one output port).
     *
     * If the input object is a Ptolemy Port, the KPort counterpart is searched
     * in the global maps. If additionally the Port is a multiport with multiple
     * connection, the given relation is used to determine which KPort
     * corresponds to the Port/Relation combination. In KIELER multiports are
     * represented by multiple KPorts with slightly offset locations.
     *
     * @param ptolemyObject The corresponding Ptolemy object, either a Vertex or
     *            a Port
     * @param relation The relation that is connected to the Ptolemy multiport
     * @return The found port, or null if no port was found.
     */
    private KPort _getPort(Object ptolemyObject, Relation relation) {
        if (ptolemyObject instanceof Vertex) {
            KNode knode = _kieler2ptolemyDivaNodes.inverse().get(ptolemyObject);
            for (KPort port : knode.getPorts()) {
                return port;
            }
        }
        List<Relation> relations = null;
        if (ptolemyObject instanceof Location) {
            // Handle an inner port represented by a Location.
            // The real port object is its container.
            ptolemyObject = ((Location) ptolemyObject).getContainer();
            if (ptolemyObject instanceof ComponentPort) {
                relations = ((ComponentPort) ptolemyObject)
                        .insideRelationList();
            }
        }
        if (ptolemyObject instanceof Port && relation != null) {
            // Special case for multiports: For a particular relation, get its
            // index in the relation list of the port. Then get the KIELER port with
            // the same index (as we mapped one Ptolemy multiport to a list of multiple
            // KIELER ports). For a simple port, just give the first corresponding port.
            if (relations == null) {
                // In this case: outer port
                relations = ((Port) ptolemyObject).linkedRelationList();
            }
            int index = relations.indexOf(relation);
            List<KPort> kports = _ptolemy2KielerPorts.get((Port) ptolemyObject);
            if (index >= 0 && index < kports.size()) {
                return kports.get(index);
            }
            return kports.get(0);
        }
        return null;
    }

    /**
     * Transform a location from a KIELER node from KIELER coordinate system to
     * ptolemy coordinate system. That is KIELER gives locations to be the upper
     * left corner of an item and Ptolemy as the center point of the item.
     *
     * If the original location is not within the bounds of the referenceNode at
     * all, the location updated differently. This is an important distinction between
     * nodes and vertices.
     *
     * @param pos Position of KIELER graphical node. This object will be altered to
     *            fit the new location.
     * @param divaNode the graphical representation in Diva
     * @param locatable the location object of the node
     */
    private void _kNode2Ptolemy(KVector pos, Object divaNode,
            Locatable locatable) {
        Point2D location = PtolemyModelUtil._getLocationPoint(locatable);
        if (divaNode != null) {
            Rectangle2D divaBounds;
            if (locatable instanceof RelativeLocation) {
                // Consider only the background figure of a composite figure
                // if the node is relative locatable, otherwise the link would also
                // be included in the bounds.
                Figure figure = (Figure) getLayoutTarget().getVisualObject(
                        divaNode);
                divaBounds = figure.getShape().getBounds2D();
            } else {
                divaBounds = getLayoutTarget().getBounds(divaNode);
            }
            double offsetX = 0, offsetY = 0;

            // Check whether the location could be determined.
            // If not, we might have something that has no location attribute,
            // such as a relation vertex, where we use the center as offset.
            if (location != null) {
                offsetX = location.getX() - divaBounds.getMinX();
                offsetY = location.getY() - divaBounds.getMinY();
            } else {
                offsetX = divaBounds.getWidth() / 2;
                offsetY = divaBounds.getHeight() / 2;
            }

            pos.x += offsetX;
            pos.y += offsetY;
        }
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
     * and then propagate this information to the adjacent edges.
     *
     * @param unprocessedEdges The list of edges that need processing.
     */
    private void _storeEndpoints(List<Link> unprocessedEdges) {
        ActorGraphModel aGraph = (ActorGraphModel) getLayoutTarget()
                .getGraphModel();
        boolean allDirectionsSet = false;
        boolean progress = false;
        while (!allDirectionsSet) {
            allDirectionsSet = true;
            progress = false;
            for (Iterator<Link> edgeIter = unprocessedEdges.iterator(); edgeIter
                    .hasNext();) {
                Link edge = edgeIter.next();
                EdgeModel edgeModel = aGraph.getEdgeModel(edge);

                Object simpleEndpoint1 = edgeModel.getHead(edge);
                Object simpleEndpoint2 = edgeModel.getTail(edge);
                Object endpoint1 = aGraph.getSemanticObject(simpleEndpoint1);
                Object endpoint2 = aGraph.getSemanticObject(simpleEndpoint2);

                // See if we have successfully looked at this edge before.
                if (_divaEdgeTarget.containsKey(edge)
                        && _divaEdgeSource.containsKey(edge)) {
                    continue;
                }

                // Check whether endpoints are source or target ports.
                if (endpoint1 instanceof Port) {
                    boolean isInput1 = PtolemyModelUtil
                            ._isInput((Port) endpoint1);
                    // Check if we look at inner or outer ports.
                    if (simpleEndpoint1 instanceof Location) {
                        // Inner input port is regarded as output.
                        isInput1 = !isInput1;
                    }
                    // Set endpoints.
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
                    // Check if we look at inner or outer ports.
                    if (simpleEndpoint2 instanceof Location) {
                        // Inner input port is regarded as output.
                        isInput2 = !isInput2;
                    }
                    // Set endpoints.
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
                // See if one of the endpoints is source or target of other edges.
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
                    // Now we can't deduce any information about this edge.
                    allDirectionsSet = false;
                }

                // Guarantee progress by just setting the direction if it cannot be deduced.
                if (!edgeIter.hasNext() && !progress) {
                    _divaEdgeTarget.put(edge, simpleEndpoint1);
                    _divaEdgeSource.put(edge, simpleEndpoint2);
                }
            }
        }
    }

    /**
     * For a given Ptolemy port, its channel index in a multiport, and the
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
     * @param outer True if the direction of the ports is obtained by calling
     * IOPortController.getDirection().  Otherwise, the direction is obtained
     * by calling PtolemyModelUtil._getExternalPortDirection().
     * @return offset vector
     */
    protected static KVector _getMultiportOffsets(Port port,
            KShapeLayout kportlayout, int index, int maxIndex, boolean outer) {
        KVector offset = new KVector();
        int direction = 0;
        if (outer) {
            direction = IOPortController.getDirection(IOPortController
                    .getCardinality(port));
        } else {
            direction = PtolemyModelUtil._getExternalPortDirection(port);
        }
        switch (direction) {
        case SwingConstants.NORTH:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.NORTH);
            // Ports are extended to left with leftmost port index 0.
            offset.x = -((maxIndex - index) * MULTIPORT_OFFSET);
            break;
        case SwingConstants.EAST:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.EAST);
            // Ports are extended to bottom with top port index 0.
            offset.y = index * MULTIPORT_OFFSET;
            break;
        case SwingConstants.SOUTH:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.SOUTH);
            offset.x = index * MULTIPORT_OFFSET;
            break;
        default:
            kportlayout.setProperty(LayoutOptions.PORT_SIDE, PortSide.WEST);
            // Ports are extended to top beginning with top port index 0.
            offset.y = -((maxIndex - index) * MULTIPORT_OFFSET);
            break;
        }
        return offset;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Offset between KIELER KPorts corresponding to a Ptolemy multiport. I.e.
     * the distance between multiple single KPorts.
     */
    private static final float MULTIPORT_OFFSET = 5.0f;

    /**
     * Debug flag that will trigger output of additional information during
     * layout run. With the flag set to true especially the KIELER graph
     * structure will be written to a file on hard disk in order to review the
     * graph later on.
     */
    private static final boolean DEBUG = false;

    /**
     * Default size of a port that will be used in KIELER layout if no explicit
     * size (e.g. copied from Ptolemy port) is given.
     */
    private static final float DEFAULT_PORT_SIZE = 5.0f;

    /**
     * The offset height used by KIELER for inner ports to correct connection anchor.
     */
    private static final float INNER_PORT_HEIGHT_OFFSET = 11.0f;

    /**
     * Offset at the x and y-coordinate of the shape of a Multiport to its figure.
     */
    private static final KVector MULTIPORT_INNER_OFFSET = new KVector(3f, -3f);

    /**
     * Offset between bottom of Multiport to the first KIELER KPort
     */
    private static final float MULTIPORT_BOTTOM = 4.5f;

    /**
     * A pool of layout provider instances, which are used to perform the actual
     * layout. The pool is accessed statically, so that its instances can be reused
     * in subsequent layout runs.
     */
    private static InstancePool<AbstractLayoutProvider> _layoutProviderPool;

    /**
     * The top level Ptolemy composite entity that contains the diagram that is
     * to be laid out.
     */
    private CompositeEntity _compositeEntity;

    /**
     * Storage of actual sources of diva edges corresponding to data flow.
     */
    private Map<Link, Object> _divaEdgeSource;

    /**
     * Storage of actual targets of diva edges corresponding to data flow.
     */
    private Map<Link, Object> _divaEdgeTarget;

    /**
     * List of KIELER edges and corresponding Diva links.
     */
    private List<Pair<KEdge, Link>> _edgeList;

    /**
     * Map KIELER nodes to Diva Nodes and back.
     */
    private BiMap<KNode, Locatable> _kieler2ptolemyDivaNodes;

    /**
     * Map Ptolemy ports to KIELER ports. A Ptolemy multiport could correspond
     * to multiple KIELER ports. Hence it's a mapping to a List of KPorts.
     */
    private ListMultimap<Port, KPort> _ptolemy2KielerPorts;

    /**
     * Pointer to Top in order to report the current status.
     */
    private Top _top;

}
