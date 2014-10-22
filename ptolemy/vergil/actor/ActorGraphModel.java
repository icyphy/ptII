/* A graph model for basic ptolemy models.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.NamedObjNodeModel;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.SnapConstraint;
import diva.graph.GraphEvent;
import diva.graph.GraphUtilities;
import diva.graph.modular.CompositeModel;
import diva.graph.modular.CompositeNodeModel;
import diva.graph.modular.EdgeModel;
import diva.graph.modular.MutableEdgeModel;
import diva.graph.modular.NodeModel;
import diva.util.NullIterator;

// NOTE: The inner classes here should be factored out as independent
// classes, and the resulting NodeModel hierarchy should be carefully
// thought out.  This work has been started, with the NamedObjNodeModel
// base class and the AttributeNodeModel derived class.  The remaining
// node models here and in the FSMGraphModel remain to be done.  EAL.
///////////////////////////////////////////////////////////////////
//// ActorGraphModel

/**
 This class represents one level of hierarchy of a Ptolemy II model.
 The graph model represents attributes, ports, entities and relations
 as nodes.  Entities and attributes are represented in the model by the
 icon that is used to visually depict them.  Relations are represented
 in the model by its vertices (which are visual elements that generally
 exist in multiple places in a visual rendition).  Ports represent
 themselves in the model.
 <p>
 In the terminology of diva, the graph elements are "nodes" (icons,
 vertices, and ports), and the "edges" link them.  Edges
 are represented in the model by instances of the Link class.
 Edges may link a port and a vertex, or a port and another port.
 For visual simplicity, both types of edges are represented by
 an instance of the Link class.  If an edge is placed between a port
 and a vertex then the Link represents a Ptolemy II link between
 the port and the vertex's Relation.  However, if an edge is placed between
 two ports, then it represents a relation (with no vertex) and links from
 the relation to each port (in Ptolemy II, this is called a "connection").
 <p>
 This model uses a ptolemy change listener to detect changes to the model
 that do not originate from this model.  These changes are propagated
 as structure changed graph events to all graphListeners registered with this
 model.  This mechanism allows a graph visualization of a ptolemy model to
 remain synchronized with the state of a mutating model.

 @author Steve Neuendorffer, Contributor: Edward A. Lee, Bert Rodiers
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
public class ActorGraphModel extends AbstractBasicGraphModel {
    /** Construct a new graph model whose root is the given composite entity.
     *  @param composite The top-level composite entity for the model.
     */
    public ActorGraphModel(NamedObj composite) {
        super(composite);
        _linkSet = new HashSet<Link>();
        _update();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This implementation will delegate to the implementation in the parent
     *  class and will additionally update the model in case it is necessary.
     *  This is typically the case when a link is created to another link.
     *  If this existing link has a vertex as head or tail,
     *  we will connect with the vertex, otherwise we will
     *  remove the old link, create a new vertex, link the
     *  head and tail of the existing link with the
     *  vertex and link the new link with the vertex.
     *  It is possible to link with an existing link.
     *  If this existing link has a vertex as head or tail,
     *  we will connect with the vertex, otherwise we will
     *  remove the old link, create a new vertex, link the
     *  head and tail of the existing link with the
     *  vertex and link the new link with the vertex.
     *  In the latter case the parent class won't call _update
     *  as a result of an optimization, and hence we do it here.
     *
     *  @param change The change that has been executed.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
        super.changeExecuted(change);

        // update the graph model if necessary.
        if (_forceUpdate && _update()) {
            _forceUpdate = false;
            // Notify any graph listeners
            // that the graph might have
            // completely changed.
            dispatchGraphEvent(new GraphEvent(this,
                    GraphEvent.STRUCTURE_CHANGED, getRoot()));
        }
    }

    /** Disconnect an edge from its two endpoints and notify graph
     *  listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     *  event whose source is the given source.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @param edge The edge.
     */
    @Override
    public void disconnectEdge(Object eventSource, Object edge) {
        if (!(getEdgeModel(edge) instanceof MutableEdgeModel)) {
            return;
        }

        MutableEdgeModel model = (MutableEdgeModel) getEdgeModel(edge);
        Object head = model.getHead(edge);
        Object tail = model.getTail(edge);
        model.setTail(edge, null);
        model.setHead(edge, null);

        if (head != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_HEAD_CHANGED, edge, head);
            dispatchGraphEvent(e);
        }

        if (tail != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_TAIL_CHANGED, edge, tail);
            dispatchGraphEvent(e);
        }
    }

    /** Return the model for the given composite object.
     *  In this class, return an instance of CompositePtolemyModel
     *  if the object is the root object of this graph model, and return
     *  an instance of IconModel if the object is a location contained
     *  by an entity.  Otherwise return null.
     *  @param composite A composite object.
     *  @return A model of a composite node.
     */
    @Override
    public CompositeModel getCompositeModel(Object composite) {
        CompositeModel result = super.getCompositeModel(composite);

        if (result == null && composite instanceof Locatable
                && ((Locatable) composite).getContainer() instanceof Entity) {
            return _iconModel;
        }

        return result;
    }

    /** Return a MoML String that will delete the given edge from the
     *  Ptolemy model.
     *  @param edge The edge.
     *  @return A valid MoML string.
     */
    @Override
    public String getDeleteEdgeMoML(Object edge) {
        // Note: the abstraction here is rather broken.  Ideally this
        // should look like getDeleteNodeMoML()
        if (!(getEdgeModel(edge) instanceof LinkModel)) {
            return "";
        }

        LinkModel model = (LinkModel) getEdgeModel(edge);
        return model.getDeleteEdgeMoML(edge);
    }

    /** Return a MoML String that will delete the given node from the
     *  Ptolemy model.
     *  @param node The node.
     *  @return A valid MoML string.
     */
    @Override
    public String getDeleteNodeMoML(Object node) {
        if (!(getNodeModel(node) instanceof NamedObjNodeModel)) {
            return "";
        }

        NamedObjNodeModel model = (NamedObjNodeModel) getNodeModel(node);
        return model.getDeleteNodeMoML(node);
    }

    /** Return the model for the given edge object.  If the object is not
     *  an edge, then return null.
     *  @param edge An object which is assumed to be in this graph model.
     *  @return An instance of LinkModel if the object is a Link.
     *   Otherwise return null.
     */
    @Override
    public EdgeModel getEdgeModel(Object edge) {
        if (edge instanceof Link) {
            return _linkModel;
        } else {
            return null;
        }
    }

    /** Return the model for edge objects that are instance of Link.
     *  This will return the same object as getEdgeModel() when the
     *  argument is a link.
     *  @return The model for links.
     */
    public LinkModel getLinkModel() {
        // FIXME: This design makes it impossible to have different
        // models for different types of links.  This method should
        // be removed, and getEdgeModel() should be used instead.
        return _linkModel;
    }

    /** Return the node model for the given object.  If the object is not
     *  a node, then return null.  The argument should be either an instance
     *  of Port or Vertex, or it implements Locatable.
     *  @param node An object which is assumed to be in this graph model.
     *  @return The node model for the specified node, or null if there
     *   is none.
     */
    @Override
    public NodeModel getNodeModel(Object node) {
        if (node instanceof Port) {
            return _portModel;
        } else if (node instanceof Vertex) {
            return _vertexModel;
        } else if (node instanceof Locatable) {
            Object container = ((Locatable) node).getContainer();

            if (container instanceof Port) {
                return _externalPortModel;
            } else if (container instanceof Entity) {
                return _iconModel;
            }
        }

        return super.getNodeModel(node);
    }

    /** Return the semantic object corresponding to the given node, edge,
     *  or composite.  A "semantic object" is an object associated with
     *  a node in the graph.  In this case, if the node is icon, the
     *  semantic object is an entity.  If it is a vertex or a link, the
     *  semantic object is a relation.  If it is a port, then the
     *  semantic object is the port itself.
     *  @param element A graph element.
     *  @return The semantic object associated with this element, or null
     *   if the object is not recognized.
     */
    @Override
    public Object getSemanticObject(Object element) {
        if (element instanceof Vertex) {
            return ((Vertex) element).getContainer();
        } else if (element instanceof Link) {
            return ((Link) element).getRelation();
        }

        return super.getSemanticObject(element);
    }

    /** Delete a node from its parent graph and notify
     *  graph listeners with a NODE_REMOVED event.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @param node The node.
     */
    @Override
    public void removeNode(Object eventSource, Object node) {
        if (!(getNodeModel(node) instanceof NamedObjNodeModel)) {
            return;
        }

        NamedObjNodeModel model = (NamedObjNodeModel) getNodeModel(node);

        model.removeNode(eventSource, node);
    }

    // FIXME: The following methods are probably inappropriate.
    // They make it impossible to have customized models for
    // particular links or icons. getLinkModel() and
    // getNodeModel() should be sufficient.
    // Big changes needed, however to make this work.
    // The huge inner classes below should be factored out as
    // separate classes.  EAL
    /** Get the icon model.
     *  @return The icon model.
     */
    public IconModel getIconModel() {
        return _iconModel;
    }

    /** Get the port model.
     *  @return The port model.
     */
    public PortModel getPortModel() {
        return _portModel;
    }

    /** Get the external port model.
     *  @return The external port model.
     */
    public ExternalPortModel getExternalPortModel() {
        return _externalPortModel;
    }

    /** Get the vertex model.
     *  @return The vertex model.
     */
    public VertexModel getVertexModel() {
        return _vertexModel;
    }

    // End of FIXME.
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get an unmodifiable copy of the link set.
     *
     *  @return The link set.
     */
    protected Set<?> _getLinkSet() {
        return Collections.unmodifiableSet(_linkSet);
    }

    /** Remove a link from the link set. This function is not made synchronized.
     *  Concurrent modification on the link set should be avoided.
     *
     *  @param link The link to be removed.
     */
    protected void _removeLink(Link link) {
        _linkSet.remove(link);
    }

    /** Update the graph model.  This is called whenever a change request is
     *  executed.  In this class the internal set of link objects is created
     *  to represent each of the links in the graph, and to remove any
     *  link objects that are incorrect (e.g., do not have both ends
     *  in the model).
     *  This method is usually called just before
     *  issuing a graph event.  If this method returns false, then the graph
     *  event should not be issued, because further changes are necessary.
     *  @return True if the model was successfully updated, or false if
     *  further change requests were queued.
     */
    @Override
    protected boolean _update() {
        // Go through all the links that currently exist, and remove
        // any that don't have both ends in the model.

        Iterator<Link> links = _linkSet.iterator();

        while (links.hasNext()) {
            Link link = links.next();
            Relation relation = link.getRelation();

            // Undo needs this: Check that the relation hasn't been removed
            if (relation == null || relation.getContainer() == null
                    || _isHidden(relation)) {
                // NOTE: We used to not do the next three lines when
                // relation == null, but this seems better.
                // EAL 6/26/05.
                link.setHead(null);
                link.setTail(null);
                links.remove();
                continue;
            }

            boolean headOK = GraphUtilities.isContainedNode(link.getHead(),
                    getRoot(), this);
            boolean tailOK = GraphUtilities.isContainedNode(link.getTail(),
                    getRoot(), this);

            // If the head or tail has been removed, then remove this link.
            if (!(headOK && tailOK)) {
                Object headObj = getSemanticObject(link.getHead());
                Object tailObj = getSemanticObject(link.getTail());
                link.setHead(null);
                link.setTail(null);
                links.remove();

                if (headObj instanceof Port && tailObj instanceof Port
                        && relation.getContainer() != null
                        && relation.linkedPortList().size() < 2) {
                    NamedObj container = getPtolemyModel();

                    // remove the relation  This should trigger removing the
                    // other link.  This avoids turning a direct connection
                    // into a half connection with a diamond.
                    // Note that the source is NOT the graphmodel, so this
                    // will trigger the changerequest listener to
                    // redraw the graph again.
                    MoMLChangeRequest request = new MoMLChangeRequest(
                            container, container, "<deleteRelation name=\""
                                    + relation.getName(container) + "\"/>\n");
                    request.setUndoable(true);

                    // Need to merge the undo for this request in with one that
                    // triggered it
                    request.setMergeWithPreviousUndo(true);
                    container.requestChange(request);

                    // If updating requires further updates to the model
                    // i.e. the above change request, then return false.
                    // this is so that rerendering doesn't happen until the
                    // graph model has reached a stable point.
                    // Note that there is a bit of a performance tradeoff
                    // here as to whether we queue a bunch of mutations in
                    // parallel (which may be redundant) or queue them
                    // serially (which may be slow).
                    return false;
                }
            }
        }

        // Now create Links for links that may be new
        NamedObj ptolemyModel = getPtolemyModel();

        if (ptolemyModel instanceof CompositeEntity) {
            Iterator<?> relations = ((CompositeEntity) ptolemyModel)
                    .relationList().iterator();

            while (relations.hasNext()) {
                _updateLinks((ComponentRelation) relations.next());
            }
        }

        return super._update();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the relation has a _hide attribute indicating
     *  that it is hidden.
     *  @return True if the relation is hidden.
     */
    private boolean _isHidden(Relation relation) {
        Attribute hide = relation.getAttribute("_hide");
        if (hide != null) {
            if (hide instanceof Parameter) {
                Token token;
                try {
                    token = ((Parameter) hide).getToken();
                    if (token instanceof BooleanToken) {
                        if (((BooleanToken) token).booleanValue()) {
                            return true;
                        }
                    }
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
            } else {
                // The mere presence of the attribute will hide
                // the relation.
                return true;
            }
        }
        return false;
    }

    /** Make sure that there is a Link object representing every
     *  link connected to the given relation.  Create links if necessary.
     */
    private void _updateLinks(ComponentRelation relation) {
        // If the relation is hidden, then skip it.
        if (_isHidden(relation)) {
            return;
        }
        // FIXME: This method is expensive for large graphs.
        // It is called for each relation, it creates a new list
        // of links for each relation, it then goes through the full
        // list of all existing links, looking only at the ones
        // associated with this relation.  Ugh.
        // Create a list of linked objects.
        // We will remove objects from this list as we discover
        // existing links to them, and then create links to any
        // remaining objects in the list.

        List<?> linkedObjects = relation.linkedObjectsList();
        int linkedObjectsCount = linkedObjects.size();

        for (Link link : new LinkedList<Link>(_linkSet)) {

            // If this link matches a link in the linkedObjects list,
            // then we remove that link from that list, since we don't
            // have to manufacture that link.
            Object tail = link.getTail();
            Object tailObj = getSemanticObject(tail);
            Object head = link.getHead();
            Object headObj = getSemanticObject(head);

            if (tailObj != relation && headObj != relation
                    && link.getRelation() != relation) {
                // The link does not involve this relation. Skip it.
                // NOTE: Used to skip it if the relation field of the link
                // didn't match this relation. But we need to ignore
                // that field for links between relations, since that
                // field will be arbitrarily one of the relations,
                // and we'll end up creating two links where there
                // should be one.
                // EAL 6/26/05
                continue;
            }

            if (tailObj != relation && headObj != relation
                    && linkedObjectsCount > 2) {
                // When the link is a direct link between two ports but the
                // relation has more than 2 ends, the link is corrupted and
                // should be deleted. This could happen as a result of model
                // transformation of the model in the frame.
                // tfeng (03/10/2009)
                link.setHead(null);
                link.setTail(null);
                _linkSet.remove(link);
                continue;
            }

            if (tailObj != null && linkedObjects.contains(tailObj)) {
                // The tail is an object in the list.
                linkedObjects.remove(tailObj);
            } else if (tailObj != relation) {
                // Unless the tail object is this relation, the link
                // must be spurious. Remove the link.
                link.setHead(null);
                link.setTail(null);
                _linkSet.remove(link);
            }

            if (headObj != null && linkedObjects.contains(headObj)) {
                // The head is an object in the list.
                linkedObjects.remove(headObj);
            } else if (headObj != relation) {
                // Unless the head object is this relation, the link
                // must be spurious. Remove the link.
                link.setHead(null);
                link.setTail(null);
                _linkSet.remove(link);
            }
        }

        // Count the remaining linked objects, which are those
        // for which there is no Link object.
        int unlinkedPortCount = linkedObjects.size();

        // If there are no links left to create, then just return.
        if (unlinkedPortCount == 0) {
            return;
        }

        // Get the Root vertex.  This is where we will manufacture links.
        // The root vertex is the one with no linked vertices.
        Vertex rootVertex = null;
        Iterator<?> vertexes = relation.attributeList(Vertex.class).iterator();

        while (vertexes.hasNext()) {
            Vertex v = (Vertex) vertexes.next();

            if (v.getLinkedVertex() == null) {
                rootVertex = v;
            }
        }

        // If there are no verticies, and the relation has exactly
        // two connections, neither of which has been made yet, then
        // create a link without a vertex for the relation.
        if (rootVertex == null && linkedObjectsCount == 2
                && unlinkedPortCount == 2
                && linkedObjects.get(0) instanceof Port
                && linkedObjects.get(1) instanceof Port) {
            Port port1 = (Port) linkedObjects.get(0);
            Port port2 = (Port) linkedObjects.get(1);
            Object head = null;
            Object tail = null;

            if (port1.getContainer().equals(getRoot())) {
                head = _getLocation(port1);
            } else {
                head = port1;
            }

            if (port2.getContainer().equals(getRoot())) {
                tail = _getLocation(port2);
            } else {
                tail = port2;
            }

            Link link;

            try {
                link = new Link();
                _linkSet.add(link);
            } catch (Exception e) {
                throw new InternalErrorException("Failed to create "
                        + "new link, even though one does not "
                        + "already exist:" + e.getMessage());
            }

            link.setRelation(relation);
            link.setHead(head);
            link.setTail(tail);
        } else {
            // A regular relation with a diamond.
            // Create a vertex if one is not found.
            if (rootVertex == null) {
                try {
                    String name = relation.uniqueName("vertex");
                    rootVertex = new Vertex(relation, name);

                    // Have to manually handle propagation, since
                    // the MoML parser is not involved.
                    // FIXME: This could cause a name collision!
                    // (Unlikely though since auto naming will take
                    // into account subclasses).
                    rootVertex.propagateExistence();
                } catch (Throwable throwable) {
                    throw new InternalErrorException(null, throwable,
                            "Failed to create "
                                    + "new vertex, even though one does not "
                                    + "already exist:" + throwable.getMessage());
                }
            }

            // Create any required links for this relation.
            Iterator<?> linkedObjectsIterator = linkedObjects.iterator();

            while (linkedObjectsIterator.hasNext()) {
                Object portOrRelation = linkedObjectsIterator.next();

                // Set the head to the port or relation. More precisely:
                //   If it is a port belonging to the composite, then
                //   set the head to a Location contained by the port.
                //   If is a port belonging to an actor, then set
                //   the head to the port.
                //   If it is a relation, then set the head to the
                //   root vertex of the relation.
                Object head = null;

                if (portOrRelation instanceof Port) {
                    Port port = (Port) portOrRelation;

                    if (port.getContainer().equals(getRoot())) {
                        head = _getLocation(port);
                    } else {
                        head = port;
                    }
                } else {
                    // Get the Root vertex of the other relation.
                    // The root vertex is the one with no linked vertices.
                    vertexes = ((Relation) portOrRelation).attributeList(
                            Vertex.class).iterator();

                    while (vertexes.hasNext()) {
                        Vertex v = (Vertex) vertexes.next();

                        if (v.getLinkedVertex() == null) {
                            head = v;
                        }
                    }
                }

                Link link;

                try {
                    link = new Link();
                    _linkSet.add(link);
                } catch (Exception e) {
                    throw new InternalErrorException("Failed to create "
                            + "new link, even though one does not "
                            + "already exist:" + e.getMessage());
                }

                link.setRelation(relation);
                link.setHead(head);
                link.setTail(rootVertex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The models of the different types of nodes and edges. */
    private ExternalPortModel _externalPortModel = new ExternalPortModel();

    /** A flag to force calling update when a ChangeRequest has been executed. */
    private boolean _forceUpdate = false;

    private IconModel _iconModel = new IconModel();

    private LinkModel _linkModel = new LinkModel();

    /** The set of all links in the model. */
    private Set<Link> _linkSet;

    private PortModel _portModel = new PortModel();

    private VertexModel _vertexModel = new VertexModel();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The model for ports that make external connections to this graph.
     *  These ports are always contained by the root of this graph model.
     */
    public class ExternalPortModel extends NamedObjNodeModel {
        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model. The MoML assumes a context that is the container
         *  of the port.
         *  @param node The node.
         *  @return A valid MoML string.
         */
        @Override
        public String getDeleteNodeMoML(Object node) {
            Locatable location = (Locatable) node;
            ComponentPort port = (ComponentPort) location.getContainer();
            StringBuffer moml = new StringBuffer();
            moml.append("<deletePort name=\"" + port.getName() + "\"/>\n");
            return moml.toString();
        }

        /** Return the graph parent of the given node.
         *  @param node The node, which is assumed to be a port contained in
         *   the root of this graph model.
         *  @return The root of this graph model.
         */
        @Override
        public Object getParent(Object node) {
            return ((Locatable) node).getContainer().getContainer();
        }

        /** Return an iterator over the edges coming into the given node.
         *  This method first ensures that there is a link
         *  object for every link.
         *  Then the iterator is constructed by
         *  removing any links that do not have the given node as head.
         *  @param node The node, which is assumed to be a port contained in
         *   the root of this graph model.
         *  @return An iterator of Link objects, all of which have
         *   the given node as their head.
         */
        @Override
        public Iterator inEdges(Object node) {
            Locatable location = (Locatable) node;
            //ComponentPort port = (ComponentPort) location.getContainer();

            // make sure that the links to relations that we are connected to
            // are up to date.
            // Go through all the links, creating a list of
            // those we are connected to.
            List<Link> portLinkList = new LinkedList<Link>();

            for (Link link : _linkSet) {
                Object head = link.getHead();

                if (head != null && head.equals(location)) {
                    portLinkList.add(link);
                }
            }

            return portLinkList.iterator();
        }

        /** Return an iterator over the edges coming out of the given node.
         *  This iterator is constructed by looping over all the relations
         *  that the port is connected to, and ensuring that there is a link
         *  object for every link.  Then the iterator is constructed by
         *  removing any links that do not have the given node as tail.
         *  @param node The node, which is assumed to be a port contained in
         *   the root of this graph model.
         *  @return An iterator of Link objects, all of which have their
         *   tail as the given node.
         */
        @Override
        public Iterator outEdges(Object node) {
            Locatable location = (Locatable) node;
            //ComponentPort port = (ComponentPort) location.getContainer();

            // make sure that the links to relations that we are connected to
            // are up to date.
            // Go through all the links, creating a list of
            // those we are connected to.
            List<Link> portLinkList = new LinkedList<Link>();

            for (Link link : _linkSet) {
                Object tail = link.getTail();

                if (tail != null && tail.equals(location)) {
                    portLinkList.add(link);
                }
            }

            return portLinkList.iterator();
        }

        /** Remove the given edge from the model.
         *  @param eventSource The source of the event that will be dispatched,
         *   e.g. the view that made this call.
         *  @param node The node.
         */
        @Override
        public void removeNode(final Object eventSource, Object node) {
            Locatable location = (Locatable) node;
            ComponentPort port = (ComponentPort) location.getContainer();
            NamedObj container = port.getContainer();
            ;

            StringBuffer moml = new StringBuffer();
            moml.append("<deletePort name=\"" + port.getName(container)
                    + "\"/>\n");

            // Note: The source is NOT the graph model.
            MoMLChangeRequest request = new MoMLChangeRequest(this, container,
                    moml.toString());
            request.setUndoable(true);
            container.requestChange(request);
        }
    }

    /** The model for an icon that contains ports.
     */
    public static class IconModel extends NamedObjNodeModel implements
            CompositeNodeModel {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model. The returned string assumes that the context is
         *  the container of the object with an icon.
         *  @param node The node.
         *  @return A valid MoML string.
         */
        @Override
        public String getDeleteNodeMoML(Object node) {
            NamedObj deleteObj = ((Locatable) node).getContainer();
            String moml = "<deleteEntity name=\"" + deleteObj.getName()
                    + "\"/>\n";
            return moml;
        }

        /** Return the number of nodes contained in
         *  this graph or composite node.
         *  @param composite The composite, which is assumed to be an icon.
         *  @return The number of ports contained in the container of the icon.
         */
        @Override
        public int getNodeCount(Object composite) {
            Locatable location = (Locatable) composite;
            return ((ComponentEntity) location.getContainer()).portList()
                    .size();
        }

        /** Return the graph parent of the given node.
         *  @param node The node, which is assumed to be an icon.
         *  @return The container of the Icon's container, which should be
         *   the root of the graph.
         */
        @Override
        public Object getParent(Object node) {
            return ((Locatable) node).getContainer().getContainer();
        }

        /** Return an iterator over the edges coming into the given node.
         *  @param node The node, which is assumed to be an icon.
         *  @return A NullIterator, since no edges are attached to icons.
         */
        @Override
        public Iterator inEdges(Object node) {
            return new NullIterator();
        }

        /** Provide an iterator over the nodes in the
         *  given graph or composite node. The nodes are ports, so if the
         *  container of the node is not an entity, then an empty iterator
         *  is returned.  This iterator
         *  does not necessarily support removal operations.
         *  @param composite The composite, which is assumed to be an icon.
         *  @return An iterator over the ports contained in the container
         *   of the icon.
         */
        @Override
        public Iterator nodes(Object composite) {
            Locatable location = (Locatable) composite;
            Nameable container = location.getContainer();

            if (container instanceof ComponentEntity) {
                ComponentEntity entity = (ComponentEntity) container;
                return entity.portList().iterator();
            } else {
                return new NullIterator();
            }
        }

        /**
         * Provide an iterator over the nodes that should
         * be rendered prior to the edges. This iterator
         * does not necessarily support removal operations.
         * In this base class, this returns the same iterator
         * as the nodes(Object) method.
         * @param composite The composite, which is assumed to be an icon.
         * @return An iterator of nodes that should be rendered before
         * the edges.
         */
        @Override
        public Iterator nodesBeforeEdges(Object composite) {
            return nodes(composite);
        }

        /**
         * Provide an iterator over the nodes that should
         * be rendered after to the edges. This iterator
         * does not necessarily support removal operations.
         * In this base class, this returns an iterator over
         * nothing.
         * @param composite The composite, which is assumed to be an icon.
         * @return An iterator of nodes that should be rendered after
         * the edges.
         */
        @Override
        public Iterator nodesAfterEdges(Object composite) {
            return new NullIterator();
        }

        /** Return an iterator over the edges coming out of the given node.
         *  @param node The node, which is assumed to be an icon.
         *  @return A NullIterator, since no edges are attached to icons.
         */
        @Override
        public Iterator outEdges(Object node) {
            return new NullIterator();
        }

        /** Remove the given node from the model.  The node is assumed
         *  to be an icon.
         *  @param eventSource The source of the event that will be dispatched,
         *   e.g. the view that made this call.
         *  @param node The node.
         */
        @Override
        public void removeNode(final Object eventSource, Object node) {
            NamedObj deleteObj = ((Locatable) node).getContainer();

            if (!(deleteObj instanceof ComponentEntity)) {
                throw new InternalErrorException(
                        "Attempt to remove a node that is not an Entity. "
                                + "node = " + node);
            }

            // Make the request in the context of the container.
            NamedObj container = deleteObj.getContainer();
            ;

            String moml = "<deleteEntity name=\"" + deleteObj.getName()
                    + "\"/>\n";

            // Note: The source is NOT the graph model.
            MoMLChangeRequest request = new MoMLChangeRequest(this, container,
                    moml);
            request.setUndoable(true);
            container.requestChange(request);
        }
    }

    /** The model for links that connect two ports, or a port and a vertex.
     */
    public class LinkModel implements MutableEdgeModel {
        /** Return true if the head of the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be a link.
         *  @param node The node to attach to.
         *  @return True if the node is a port or a vertex, or a location
         *  representing a port.
         */
        @Override
        public boolean acceptHead(Object edge, Object node) {
            if (node instanceof Port || node instanceof Vertex
                    || node instanceof Locatable
                    && ((Locatable) node).getContainer() instanceof Port) {
                return true;
            } else {
                return false;
            }
        }

        /** Return true if the tail of the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be a link.
         *  @param node The node to attach to.
         *  @return True if the node is a port or a vertex, or a location
         *  representing a port.
         */
        @Override
        public boolean acceptTail(Object edge, Object node) {
            if (node instanceof Port || node instanceof Vertex
                    || node instanceof Locatable
                    && ((Locatable) node).getContainer() instanceof Port) {
                return true;
            } else {
                return false;
            }
        }

        /** Generate the moml to add a vertex to an exist link.
         *  @param moml The moml to add the vertex to the link.
         *  @param failmoml The moml to undo these changed when
         *                      something goes wrong.
         *  @param container The container.
         *  @param oldLink The link that will be replace by two new once
         *      and a vertex in between.
         *  @param newRelationName The name of the new relation.
         *  @param x The x coordinate of the location of the vertex.
         *  @param y The y coordinate of the location of the vertex.
         */
        public void addNewVertexToLink(final StringBuffer moml,
                final StringBuffer failmoml, final CompositeEntity container,
                Link oldLink, String newRelationName, double x, double y) {

            final String vertexName = "vertex1";
            ComponentRelation relation = oldLink.getRelation();
            int width = IORelation.WIDTH_TO_INFER;
            if (relation instanceof IORelation) {
                Parameter widthPar = ((IORelation) relation).width;
                try {
                    IntToken t = (IntToken) widthPar.getToken();
                    if (t != null) {
                        width = t.intValue();
                    }
                } catch (IllegalActionException e) {
                    // ignore the exception. If we can't request the
                    // width, we'll use WIDTH_TO_INFER
                }
            }

            // Create the relation.
            moml.append("<relation name=\"" + newRelationName + "\">\n");
            moml.append("<property name=\"width\" class=\"ptolemy.data.expr.Parameter\""
                    + " value=\"" + width + "\"></property>");
            moml.append("<vertex name=\"" + vertexName + "\" value=\"{");
            moml.append(x + ", " + y);
            moml.append("}\"/>\n");

            moml.append("</relation>");

            // We will remove the existing link, but before doing that
            // we need to retrieve the index to reconnect at the correct index

            boolean headIsActorPort = oldLink.getHead() instanceof IOPort;
            boolean tailIsActorPort = oldLink.getTail() instanceof IOPort;

            NamedObj oldHead = (NamedObj) oldLink.getHead();
            NamedObj oldTail = (NamedObj) oldLink.getTail();

            _unlinkMoML(container, moml, oldHead, oldTail, relation);

            NamedObj oldHeadSemantic = (NamedObj) getSemanticObject(oldHead);

            if (oldHeadSemantic != null) {
                int headRelationIndex = oldHeadSemantic instanceof IOPort ? IOPort
                        .getRelationIndex((IOPort) oldHeadSemantic, relation,
                                headIsActorPort) : -1;
                _linkWithRelation(moml, failmoml, container, oldHeadSemantic,
                        headRelationIndex, newRelationName);
            }

            NamedObj oldTailSemantic = (NamedObj) getSemanticObject(oldTail);

            if (oldTailSemantic != null) {
                int tailRelationIndex = oldTailSemantic instanceof IOPort ? IOPort
                        .getRelationIndex((IOPort) oldTailSemantic, relation,
                                tailIsActorPort) : -1;

                _linkWithRelation(moml, failmoml, container, oldTailSemantic,
                        tailRelationIndex, newRelationName);
            }
        }

        /** Return a MoML String that will delete the given edge from the
         *  Ptolemy model.
         *  @param edge The edge.
         *  @return A valid MoML string.
         */
        public String getDeleteEdgeMoML(Object edge) {
            final Link link = (Link) edge;
            NamedObj linkHead = (NamedObj) link.getHead();
            NamedObj linkTail = (NamedObj) link.getTail();
            Relation linkRelation = link.getRelation();

            // This moml is parsed to execute the change
            StringBuffer moml = new StringBuffer();

            // Make the request in the context of the container.
            // JDK1.2.2 fails to compile the next line.
            NamedObj container = getPtolemyModel();

            // create moml to unlink any existing.
            try {
                _unlinkMoML(container, moml, linkHead, linkTail, linkRelation);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }

            return moml.toString();
        }

        /** Return the head node of the given edge.
         *  @param edge The edge, which is assumed to be a link.
         *  @return The node that is the head of the specified edge.
         *  @see #setHead(Object, Object)
         */
        @Override
        public Object getHead(Object edge) {
            return ((Link) edge).getHead();
        }

        /** Return the tail node of the specified edge.
         *  @param edge The edge, which is assumed to be a link.
         *  @return The node that is the tail of the specified edge.
         *  @see #setTail(Object, Object)
         */
        @Override
        public Object getTail(Object edge) {
            return ((Link) edge).getTail();
        }

        /** Return true if this edge is directed.
         *  In this model, none of edges
         *  are directed, so this always returns false.
         *  @param edge The edge, which is assumed to be a link.
         *  @return False.
         */
        @Override
        public boolean isDirected(Object edge) {
            return false;
        }

        /** Connect the given edge to the given head node.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be a link.
         *  @param newLinkHead The new head for the edge, which is assumed to
         *  be a location representing a port, a port or a vertex.
         *  @see #getHead(Object)
         */
        @Override
        public void setHead(final Object edge, final Object newLinkHead) {
            _setHeadOrTail(edge, newLinkHead, true);
        }

        /** Connect the given edge to the given tail node.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be a link.
         *  @param newLinkTail The new tail for the edge, which is
         *  assumed to be a location representing a port, a port or a
         *  vertex.
         *  @see #getTail(Object)
         */
        @Override
        public void setTail(final Object edge, final Object newLinkTail) {
            _setHeadOrTail(edge, newLinkTail, false);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Get a location for a new relations between the ports denoted by
         * semanticHead and semanticTail.
         * @param semanticHead The head for the new relation.
         * @param semanticTail The tail for the new relation.
         * @param headIsActorPort A flag that specifies whether this is a
         *      actor port of a actor or a stand-alone port.
         * @param tailIsActorPort A flag that specifies whether this is a
         *      actor port of a actor or a stand-alone port.
         * @return The new location.
         */
        private double[] _getNewLocation(NamedObj semanticHead,
                NamedObj semanticTail, boolean headIsActorPort,
                boolean tailIsActorPort) {
            double[] headLocation = _getLocation(
                    headIsActorPort ? semanticHead.getContainer()
                            : semanticHead).getLocation();
            double[] tailLocation = _getLocation(
                    tailIsActorPort ? semanticTail.getContainer()
                            : semanticTail).getLocation();
            double[] newLocation = new double[2];
            newLocation[0] = (headLocation[0] + tailLocation[0]) / 2.0;
            newLocation[1] = (headLocation[1] + tailLocation[1]) / 2.0;
            newLocation = SnapConstraint.constrainPoint(newLocation);
            return newLocation;
        }

        /** Append moml to the given buffer that connects a link with the
         *  given head and tail.  Names in the moml that is written will be
         *  relative to the given container.  This may require adding a
         *  vertex to the ptolemy model.
         *  If no vertex is added, then return null.
         *  @param container The container composite actor.
         *  @param moml The string buffer to write the MoML to.
         *  @param failmoml The string buffer to write alternative
         *   MoML to, to be used if the first MoML fails.
         *  @param linkHead The head vertex or port.
         *  @param linkTail The tail vertex or port.
         *  @return The MoML to establish a link, or null if no vertex is added.
         */
        private String _linkMoML(NamedObj container, StringBuffer moml,
                StringBuffer failmoml, NamedObj linkHead, NamedObj linkTail)
                throws Exception {
            if (linkHead != null && linkTail != null) {
                NamedObj head = (NamedObj) getSemanticObject(linkHead);
                NamedObj tail = (NamedObj) getSemanticObject(linkTail);

                if (head instanceof ComponentPort
                        && tail instanceof ComponentPort) {
                    ComponentPort headPort = (ComponentPort) head;
                    ComponentPort tailPort = (ComponentPort) tail;
                    NamedObj ptolemyModel = getPtolemyModel();

                    // Linking two ports with a new relation.
                    String relationName = ptolemyModel.uniqueName("relation");

                    // If the context is not the entity that we're editing,
                    // then we need to set the context correctly.
                    if (ptolemyModel != container) {
                        String contextString = "<entity name=\""
                                + ptolemyModel.getName(container) + "\">\n";
                        moml.append(contextString);
                        failmoml.append(contextString);
                    }

                    // Note that we use no class so that we use the container's
                    // factory method when this gets parsed
                    moml.append("<relation name=\"" + relationName + "\"/>\n");
                    moml.append("<link port=\""
                            + headPort.getName(ptolemyModel) + "\" relation=\""
                            + relationName + "\"/>\n");
                    moml.append("<link port=\""
                            + tailPort.getName(ptolemyModel) + "\" relation=\""
                            + relationName + "\"/>\n");

                    // Record moml so that we can blow away these
                    // links in case we can't create them
                    failmoml.append("<unlink port=\""
                            + headPort.getName(ptolemyModel) + "\" relation=\""
                            + relationName + "\"/>\n");
                    failmoml.append("<unlink port=\""
                            + tailPort.getName(ptolemyModel) + "\" relation=\""
                            + relationName + "\"/>\n");
                    failmoml.append("<deleteRelation name=\"" + relationName
                            + "\"/>\n");

                    // close the context
                    if (ptolemyModel != container) {
                        moml.append("</entity>");
                        failmoml.append("</entity>");
                    }

                    // Ugh this is ugly.
                    if (ptolemyModel != container) {
                        return ptolemyModel.getName(container) + "."
                                + relationName;
                    } else {
                        return relationName;
                    }
                } else if (head instanceof ComponentPort
                        && linkTail instanceof Vertex) {
                    // Linking a port to an existing relation.
                    moml.append("<link port=\"" + head.getName(container)
                            + "\" relation=\"" + tail.getName(container)
                            + "\"/>\n");
                    return tail.getName(container);
                } else if (tail instanceof ComponentPort
                        && linkHead instanceof Vertex) {
                    // Linking a port to an existing relation.
                    moml.append("<link port=\"" + tail.getName(container)
                            + "\" relation=\"" + head.getName(container)
                            + "\"/>\n");
                    return head.getName(container);
                } else if (linkHead instanceof Vertex
                        && linkTail instanceof Vertex) {
                    moml.append("<link relation1=\"" + tail.getName(container)
                            + "\" relation2=\"" + head.getName(container)
                            + "\"/>\n");
                    return head.getName(container);
                } else {
                    throw new RuntimeException("Link failed: " + "Head = "
                            + head + ", Tail = " + tail);
                }
            } else {
                // No Linking to do.
                return null;
            }
        }

        /** Append moml to the given buffer that connects a relation with the
         *  given semanticObject. If relationIndex equals -1 it will
         *  be ignored, otherwise the relation will be connected at index relationIndex at
         *  the semanticObject, in case it represents a port.
         *  @param moml The string buffer to write the MoML to.
         *  @param failmoml The string buffer to write alternative
         *   MoML to, to be used if the first MoML fails.
         *  @param container The container composite actor.
         *  @param semanticObject The semantic object (relation or port).
         *  @param relationIndex The index of the relation at the port.
         *  @param relationName The name of the relation.
         */
        private void _linkWithRelation(final StringBuffer moml,
                final StringBuffer failmoml, final CompositeEntity container,
                NamedObj semanticObject, int relationIndex, String relationName) {

            if (semanticObject instanceof ComponentPort) {
                moml.append("<link port=\"" + semanticObject.getName(container)
                        + "\" relation=\"" + relationName);
                if (relationIndex != -1) {
                    moml.append("\" insertAt=\"" + relationIndex);
                }
                moml.append("\"/>\n");

                // Record moml so that we can blow away these
                // links in case we can't create them
                failmoml.append("<unlink port=\""
                        + semanticObject.getName(container) + "\" relation=\""
                        + relationName + "\"/>\n");
            } else if (semanticObject instanceof Relation) {
                moml.append("<link relation1=\""
                        + semanticObject.getName(container) + "\" relation2=\""
                        + relationName + "\"/>\n");
                failmoml.append("<unlink relation1=\""
                        + semanticObject.getName(container) + "\" relation2=\""
                        + relationName + "\"/>\n");
            } else {
                throw new RuntimeException("Link failed: " + "Object = "
                        + semanticObject + ", Relation = " + relationName);
            }
        }

        /** Connect the given edge to the given head or tail node.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be a link.
         *  @param newLinkHeadOrTail The new head or tail for the edge,
         *  which is assumed to be a location representing a port,
         *  a port or a vertex.
         *  @param isHead True when newLinkHeadOrTail represents the head
         *  @see #setHead(Object, Object)
         *  @see #setTail(Object, Object)
         */
        private void _setHeadOrTail(final Object edge,
                final Object newLinkHeadOrTail, final boolean isHead) {
            final Link link = (Link) edge;
            final NamedObj linkHead = (NamedObj) link.getHead();
            final NamedObj linkTail = (NamedObj) link.getTail();
            Relation linkRelation = link.getRelation();

            // This moml is parsed to execute the change
            final StringBuffer moml = new StringBuffer();

            // This moml is parsed in case the change fails.
            final StringBuffer failmoml = new StringBuffer();
            moml.append("<group>\n");
            failmoml.append("<group>\n");

            // Make the request in the context of the container.
            final CompositeEntity container = (CompositeEntity) getPtolemyModel();

            String relationName = "";

            // Flag specifying whether we have actually created any MoML.
            boolean appendedMoML = false;

            try {
                // create moml to unlink any existing.
                appendedMoML = _unlinkMoML(container, moml, linkHead, linkTail,
                        linkRelation);

                // It is possible to link with an existing link.
                // If this existing link has a vertex as head or tail,
                // we will connect with the vertex, otherwise we will
                // remove the old link, create a new vertex, link the
                // head and tail of the existing link with the
                // vertex and link the new link with the vertex.
                if (newLinkHeadOrTail instanceof Link) {

                    Link oldLink = (Link) newLinkHeadOrTail;

                    NamedObj oldHead = (NamedObj) oldLink.getHead();
                    NamedObj oldTail = (NamedObj) oldLink.getTail();

                    if (oldHead instanceof Vertex) {
                        // Link the new link with oldHead
                        // create moml to make the new links.
                        if (isHead) {
                            relationName = _linkMoML(container, moml, failmoml,
                                    oldHead, linkTail);
                        } else {
                            relationName = _linkMoML(container, moml, failmoml,
                                    linkHead, oldHead);
                        }
                    } else if (oldTail instanceof Vertex) {
                        // Link the new link with oldTail
                        // create moml to make the new links.
                        if (isHead) {
                            relationName = _linkMoML(container, moml, failmoml,
                                    oldTail, linkTail);
                        } else {
                            relationName = _linkMoML(container, moml, failmoml,
                                    linkHead, oldTail);
                        }
                    } else {
                        // Remove the old link, create a new vertex, link the
                        // head and tail of the existing link with the
                        // vertex and link the new link with the vertex.

                        NamedObj oldHeadSemantic = (NamedObj) getSemanticObject(oldHead);
                        NamedObj oldTailSemantic = (NamedObj) getSemanticObject(oldTail);

                        // In case the head is a port of an actor in the current composite
                        // actor the head will be an IOPort, if it is a port of the current
                        // composite actor it will be a Locatable
                        boolean headIsActorPort = oldHeadSemantic != null ? oldLink
                                .getHead() instanceof IOPort
                                : linkTail instanceof IOPort;
                        boolean tailIsActorPort = oldLink.getTail() instanceof IOPort;

                        final NamedObj toplevel = getPtolemyModel();
                        String newRelationName = toplevel
                                .uniqueName("relation");

                        double[] newLocation = _getNewLocation(
                                oldHeadSemantic != null ? oldHeadSemantic
                                        : (NamedObj) getSemanticObject(linkTail),
                                oldTailSemantic, headIsActorPort,
                                tailIsActorPort);

                        relationName = newRelationName;

                        addNewVertexToLink(moml, failmoml, container, oldLink,
                                newRelationName, newLocation[0], newLocation[1]);

                        if (isHead) {
                            _linkWithRelation(moml, failmoml, container,
                                    (NamedObj) getSemanticObject(linkTail), -1,
                                    newRelationName);
                        } else {
                            _linkWithRelation(moml, failmoml, container,
                                    (NamedObj) getSemanticObject(linkHead), -1,
                                    newRelationName);
                        }

                        failmoml.append("<deleteRelation name=\""
                                + newRelationName + "\"/>\n");

                        appendedMoML = true;
                    }
                } else {
                    // create moml to make the new links.
                    if (isHead) {
                        relationName = _linkMoML(container, moml, failmoml,
                                (NamedObj) newLinkHeadOrTail, linkTail);
                    } else {
                        relationName = _linkMoML(container, moml, failmoml,
                                linkHead, (NamedObj) newLinkHeadOrTail);
                    }
                }

                // FIXME: Above can return an empty name, so the following
                // test is not quite right.
                appendedMoML = appendedMoML || relationName != null;
            } catch (Exception ex) {
                // The link is bad... remove it.
                _linkSet.remove(link);
                link.setHead(null);
                link.setTail(null);
                dispatchGraphEvent(new GraphEvent(ActorGraphModel.this,
                        GraphEvent.STRUCTURE_CHANGED, getRoot()));
            }

            moml.append("</group>\n");
            failmoml.append("</group>\n");

            final String relationNameToAdd = relationName;
            final boolean nonEmptyMoML = appendedMoML;

            // Here the source IS the graph model, because we need to
            // handle the event dispatch specially:  An event is only
            // dispatched if both the head and the tail are attached.
            // This rather obnoxious hack is here because edge creation
            // is tricky and we can't rerender the edge while we are dragging
            // it.
            MoMLChangeRequest request = new MoMLChangeRequest(
                    ActorGraphModel.this, container, moml.toString()) {
                @Override
                protected void _execute() throws Exception {
                    // If nonEmptyMoML is false, then the MoML code is empty.
                    // Do not execute it, as this will put spurious empty
                    // junk on the undo stack.
                    if (nonEmptyMoML) {
                        super._execute();
                    }

                    // It is possible to link with an existing link.
                    // If this existing link has a vertex as head or tail,
                    // we will connect with the vertex, otherwise we will
                    // remove the old link, create a new vertex, link the
                    // head and tail of the existing link with the
                    // vertex and link the new link with the vertex.

                    if (!(newLinkHeadOrTail instanceof Link)) {
                        if (isHead) {
                            link.setHead(newLinkHeadOrTail);
                        } else {
                            link.setTail(newLinkHeadOrTail);
                        }
                    } else {
                        // Make sure that the model is updated. We made structural
                        // changes that impose an update of this ActorGraphModel.
                        _forceUpdate = true;

                        // Set head/tail equal to newly created vertex.
                        if (isHead) {
                            link.setHead(_getLocation(container
                                    .getRelation(relationNameToAdd)));
                        } else {
                            Object relation = container
                                    .getRelation(relationNameToAdd);
                            if (relation == null) {
                                throw new NullPointerException(
                                        "Getting the relation \""
                                                + relationNameToAdd + "\" in "
                                                + container.getFullName()
                                                + " returned null?");
                            }
                        }
                    }

                    if (relationNameToAdd != null) {
                        ComponentRelation relation = container
                                .getRelation(relationNameToAdd);

                        if (relation == null) {
                            throw new InternalErrorException(
                                    "Tried to find relation with name "
                                            + relationNameToAdd
                                            + " in context " + container);
                        }

                        link.setRelation(relation);
                    } else {
                        link.setRelation(null);
                    }
                }
            };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new LinkChangeListener(link, container,
                    failmoml));

            request.setUndoable(true);
            container.requestChange(request);
        }

        /** Append moml to the given buffer that disconnects a link with the
         *  given head, tail, and relation. Names in the returned moml will be
         *  relative to the given container. If either linkHead or linkTail
         *  is null, then nothing will be appended to the moml buffer.
         *  @return True if any MoML is appended to the moml argument.
         */
        private boolean _unlinkMoML(NamedObj container, StringBuffer moml,
                NamedObj linkHead, NamedObj linkTail, Relation relation) {
            // If the link is already connected, then create a bit of MoML
            // to unlink the link.
            if (linkHead != null && linkTail != null) {
                NamedObj head = (NamedObj) getSemanticObject(linkHead);
                NamedObj tail = (NamedObj) getSemanticObject(linkTail);

                if (head instanceof ComponentPort
                        && tail instanceof ComponentPort) {
                    ComponentPort headPort = (ComponentPort) head;
                    ComponentPort tailPort = (ComponentPort) tail;

                    // Unlinking two ports with an anonymous relation.
                    moml.append("<unlink port=\"" + headPort.getName(container)
                            + "\" relation=\"" + relation.getName(container)
                            + "\"/>\n");
                    moml.append("<unlink port=\"" + tailPort.getName(container)
                            + "\" relation=\"" + relation.getName(container)
                            + "\"/>\n");
                    moml.append("<deleteRelation name=\""
                            + relation.getName(container) + "\"/>\n");
                } else if (head instanceof ComponentPort
                        && linkTail instanceof Vertex) {
                    // Unlinking a port from an existing relation.
                    moml.append("<unlink port=\"" + head.getName(container)
                            + "\" relation=\"" + tail.getName(container)
                            + "\"/>\n");
                } else if (tail instanceof ComponentPort
                        && linkHead instanceof Vertex) {
                    // Unlinking a port from an existing relation.
                    moml.append("<unlink port=\"" + tail.getName(container)
                            + "\" relation=\"" + head.getName(container)
                            + "\"/>\n");
                } else if (linkHead instanceof Vertex
                        && linkTail instanceof Vertex) {
                    moml.append("<unlink relation1=\""
                            + tail.getName(container) + "\" relation2=\""
                            + head.getName(container) + "\"/>\n");
                } else {
                    throw new RuntimeException("Unlink failed: " + "Head = "
                            + head + ", Tail = " + tail);
                }

                return true;
            } else {
                // No unlinking to do.
                return false;
            }
        }

        /** This change listener is responsible for dispatching graph events
         *  when an edge is moved.  It works the same for heads and tails.
         */
        public class LinkChangeListener implements ChangeListener {
            /** Construct a link change listener.
             *  @param link The link.
             *  @param container The container.
             *  @param failMoML MoML that cleans up the model if the
             *  change request fails.
             */
            public LinkChangeListener(Link link, CompositeEntity container,
                    StringBuffer failMoML) {
                _link = link;
                _container = container;
                _failMoML = failMoML;
            }

            /** Handled a failed change request.
             *  @param change  The change request.
             *  @param exception The exception.
             */
            @Override
            public void changeFailed(ChangeRequest change, Exception exception) {
                // If we fail here, then we remove the link entirely.
                _linkSet.remove(_link);
                _link.setHead(null);
                _link.setTail(null);
                _link.setRelation(null);

                // and queue a new change request to clean up the model
                // Note: JDK1.2.2 requires that this variable not be
                // called request or we get a compile error.
                // Note the source is NOT the graph model
                MoMLChangeRequest changeRequest = new MoMLChangeRequest(this,
                        _container, _failMoML.toString());

                // fail moml not undoable
                _container.requestChange(changeRequest);
            }

            /** Called after the change has been executed.
             *  @param change The change request.
             */
            @Override
            public void changeExecuted(ChangeRequest change) {
                // modification to the linkset HAS to occur in the swing
                // thread.
                if (GraphUtilities.isPartiallyContainedEdge(_link, getRoot(),
                        ActorGraphModel.this)) {
                    _linkSet.add(_link);
                } else {
                    _linkSet.remove(_link);
                }

                // Note that there is no GraphEvent dispatched here
                // if the edge is not fully connected.  This is to
                // prevent rerendering while
                // an edge is being created.
                if (_link.getHead() != null && _link.getTail() != null) {
                    dispatchGraphEvent(new GraphEvent(ActorGraphModel.this,
                            GraphEvent.STRUCTURE_CHANGED, getRoot()));
                }
            }

            private Link _link;

            private CompositeEntity _container;

            private StringBuffer _failMoML;
        }
    }

    /** The model for ports that are contained in icons in this graph.
     */
    public class PortModel extends NamedObjNodeModel {
        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model. This assumes that the context is the container
         *  of the port.
         *  @param node The node.
         *  @return A valid MoML string.
         */
        @Override
        public String getDeleteNodeMoML(Object node) {
            NamedObj deleteObj = ((Locatable) node).getContainer();
            NamedObj container = deleteObj.getContainer();
            ;

            String moml = "<deletePort name=\"" + deleteObj.getName(container)
                    + "\"/>\n";
            return moml;
        }

        /** Return the graph parent of the given node.
         *  @param node The node, which is assumed to be a port.
         *  @return The (presumably unique) icon contained in the port's
         *   container.
         */
        @Override
        public Object getParent(Object node) {
            ComponentPort port = (ComponentPort) node;
            Entity entity = (Entity) port.getContainer();

            if (entity == null) {
                return null;
            }

            List<?> locationList = entity.attributeList(Locatable.class);

            if (locationList.size() > 0) {
                return locationList.get(0);
            } else {
                try {
                    // NOTE: We need the location right away, so we go ahead
                    // and create it and handle the propagation locally.
                    Location location = new Location(entity, "_location");
                    location.propagateExistence();
                    return location;
                } catch (Exception e) {
                    throw new InternalErrorException("Failed to create "
                            + "location, even though one does not exist:"
                            + e.getMessage());
                }
            }
        }

        /** Return an iterator over the edges coming into the given node.
         *  This method first ensures that there is a link
         *  object for every link.  Then the iterator is constructed by
         *  removing any links that do not have the given node as head.
         *  @param node The node, which is assumed to be a port contained in
         *   the root of this graph model.
         *  @return An iterator of Link objects, all of which have their
         *   head as the given node.
         */
        @Override
        public Iterator inEdges(Object node) {
            ComponentPort port = (ComponentPort) node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List<Link> portLinkList = new LinkedList<Link>();

            for (Link link : _linkSet) {
                Object head = link.getHead();

                if (head != null && head.equals(port)) {
                    portLinkList.add(link);
                }
            }

            return portLinkList.iterator();
        }

        /** Return an iterator over the edges coming out of the given node.
         *  This iterator is constructed by looping over all the relations
         *  that the port is connected to, and ensuring that there is a link
         *  object for every link.  Then the iterator is constructed by
         *  removing any links that do not have the given node as tail.
         *  @param node The node, which is assumed to be a port contained in
         *   the root of this graph model.
         *  @return An iterator of Link objects, all of which have their
         *   tail as the given node.
         */
        @Override
        public Iterator outEdges(Object node) {
            ComponentPort port = (ComponentPort) node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List<Link> portLinkList = new LinkedList<Link>();
            for (Link link : _linkSet) {
                Object tail = link.getTail();

                if (tail != null && tail.equals(port)) {
                    portLinkList.add(link);
                }
            }

            return portLinkList.iterator();
        }

        /** Remove the given node from the model.  The node is assumed
         *  to be a port.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param eventSource The source of the event that will be dispatched,
         *   e.g. the view that made this call.
         *  @param node The node.
         */
        @Override
        public void removeNode(final Object eventSource, Object node) {
            ComponentPort port = (ComponentPort) node;
            NamedObj container = port.getContainer();

            // Delete the port.
            String moml = "<deletePort name=\"" + port.getName() + "\"/>\n";

            // Note: The source is NOT the graph model.
            MoMLChangeRequest request = new MoMLChangeRequest(this, container,
                    moml);
            request.setUndoable(true);
            container.requestChange(request);
        }
    }

    /** The model for vertexes that are contained within the relations of the
     *  ptolemy model.
     */
    public class VertexModel extends NamedObjNodeModel {
        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model. This assumes that the context is the container
         *  of the vertex.
         *  @param node The node.
         *  @return A valid MoML string.
         */
        @Override
        public String getDeleteNodeMoML(Object node) {
            ComponentRelation deleteObj = (ComponentRelation) ((Vertex) node)
                    .getContainer();
            String moml = "<deleteRelation name=\"" + deleteObj.getName()
                    + "\"/>\n";
            return moml;
        }

        /** Return the graph parent of the given node.
         *  @param node The node, which is assumed to be a Vertex.
         *  @return The container of the vertex's container, which is
         *   presumably the root of the graph model.
         */
        @Override
        public Object getParent(Object node) {
            // Undo: If we use automatic layout, then we need to check to
            // see if the container is null here.
            if (((Vertex) node).getContainer() == null) {
                return null;
            }

            return ((Vertex) node).getContainer().getContainer();
        }

        /** Return an iterator over the edges coming into the given node.
         *  This method ensures that there is a link object for
         *  every link to the relation contained by the vertex.
         *  Then the iterator is constructed by
         *  removing any links that do not have the given node as head.
         *  @param node The node, which is assumed to be a vertex contained in
         *   a relation.
         *  @return An iterator of Link objects, all of which have their
         *   head as the given node.
         */
        @Override
        public Iterator inEdges(Object node) {
            Vertex vertex = (Vertex) node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List<Link> vertexLinkList = new LinkedList<Link>();

            for (Link link : _linkSet) {
                Object head = link.getHead();

                if (head != null && head.equals(vertex)) {
                    vertexLinkList.add(link);
                }
            }

            return vertexLinkList.iterator();
        }

        /** Return an iterator over the edges coming into the given node.
         *  This method ensures that there is a link object for
         *  every link to the relation contained by the vertex.
         *  Then the iterator is constructed by
         *  removing any links that do not have the given node as head.
         *  @param node The node, which is assumed to be a vertex contained in
         *   a relation.
         *  @return An iterator of Link objects, all of which have their
         *   tail as the given node.
         */
        @Override
        public Iterator outEdges(Object node) {
            Vertex vertex = (Vertex) node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List<Link> vertexLinkList = new LinkedList<Link>();

            for (Link link : _linkSet) {
                Object tail = link.getTail();

                if (tail != null && tail.equals(vertex)) {
                    vertexLinkList.add(link);
                }
            }

            return vertexLinkList.iterator();
        }

        /** Remove the given node from the model.  The node is assumed
         *  to be a vertex contained by a relation.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param eventSource The source of the event that will be dispatched,
         *   e.g. the view that made this call.
         *  @param node The node.
         */
        @Override
        public void removeNode(final Object eventSource, Object node) {
            ComponentRelation relation = (ComponentRelation) ((Vertex) node)
                    .getContainer();
            NamedObj container = relation.getContainer();

            // Delete the relation.
            String moml = "<deleteRelation name=\"" + relation.getName()
                    + "\"/>\n";

            // Note: The source is NOT the graph mode.
            MoMLChangeRequest request = new MoMLChangeRequest(this, container,
                    moml);
            request.setUndoable(true);
            container.requestChange(request);
        }
    }
}
