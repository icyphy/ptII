/* A graph model for basic ptolemy models.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.actor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.NamedObjNodeModel;
import ptolemy.vergil.kernel.Link;
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

//////////////////////////////////////////////////////////////////////////
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

@author Steve Neuendorffer, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
 */
public class ActorGraphModel extends AbstractBasicGraphModel {

    /** Construct a new graph model whose root is the given composite entity.
     *  @param composite The top-level composite entity for the model.
     */
    public ActorGraphModel(NamedObj composite) {
        super(composite);
        _linkSet = new HashSet();
        _update();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Disconnect an edge from its two enpoints and notify graph
     *  listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     *  event whose source is the given source.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @exception GraphException If the operation fails.
     */
    public void disconnectEdge(Object eventSource, Object edge) {
        if (!(getEdgeModel(edge) instanceof MutableEdgeModel)) return;
        MutableEdgeModel model = (MutableEdgeModel)getEdgeModel(edge);
        Object head = model.getHead(edge);
        Object tail = model.getTail(edge);
        model.setTail(edge, null);
        model.setHead(edge, null);
        if (head != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_HEAD_CHANGED,
                    edge, head);
            dispatchGraphEvent(e);
        }
        if (tail != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_TAIL_CHANGED,
                    edge, tail);
            dispatchGraphEvent(e);
        }
    }

    /** Return a MoML String that will delete the given edge from the
     *  Ptolemy model.
     *  @return A valid MoML string.
     */
    public String getDeleteEdgeMoML(Object edge) {
        // Note: the abstraction here is rather broken.  Ideally this
        // should look like getDeleteNodeMoML()
        if (!(getEdgeModel(edge) instanceof LinkModel)) return "";
        LinkModel model = (LinkModel)getEdgeModel(edge);
        return model.getDeleteEdgeMoML(edge);
    }

    /** Return a MoML String that will delete the given node from the
     *  Ptolemy model.
     *  @return A valid MoML string.
     */
    public String getDeleteNodeMoML(Object node) {
        if (!(getNodeModel(node) instanceof NamedObjNodeModel)) return "";
        NamedObjNodeModel model = (NamedObjNodeModel)getNodeModel(node);
        return model.getDeleteNodeMoML(node);
    }

    /** Return the model for the given composite object.
     *  In this class, return an instance of CompositePtolemyModel
     *  if the object is the root object of this graph model, and return
     *  an instance of IconModel if the object is a location contained
     *  by an entity.  Otherwise return null.
     *  @param composite A composite object.
     *  @return A model of a composite node.
     */
    public CompositeModel getCompositeModel(Object composite) {
        CompositeModel result = super.getCompositeModel(composite);
        if (result == null
                && composite instanceof Locatable
                && ((Locatable)composite).getContainer() instanceof Entity) {
            return _iconModel;
        }
        return result;
    }

    /** Return the model for the given edge object.  If the object is not
     *  an edge, then return null.
     *  @param edge An object which is assumed to be in this graph model.
     *  @return An instance of LinkModel if the object is a Link.
     *   Otherwise return null.
     */
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
    public NodeModel getNodeModel(Object node) {
        if (node instanceof Port) {
            return _portModel;
        } else if (node instanceof Vertex) {
            return _vertexModel;
        } else if (node instanceof Locatable) {
            Object container = ((Locatable)node).getContainer();
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
    public Object getSemanticObject(Object element) {
        if (element instanceof Vertex) {
            return ((Vertex)element).getContainer();
        } else if (element instanceof Link) {
            return ((Link)element).getRelation();
        }
        return super.getSemanticObject(element);
    }

    /** Delete a node from its parent graph and notify
     *  graph listeners with a NODE_REMOVED event.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @exception GraphException If the operation fails.
     */
    public void removeNode(Object eventSource, Object node) {
        if (!(getNodeModel(node) instanceof NamedObjNodeModel)) return;
        NamedObjNodeModel model = (NamedObjNodeModel)getNodeModel(node);

        model.removeNode(eventSource, node);
    }

    // FIXME: The following methods are probably inappropriate.
    // They make it impossible to have customized models for
    // particular links or icons. getLinkModel() and
    // getNodeModel() should be sufficient.
    // Big changes needed, however to make this work.
    // The huge inner classes below should be factored out as
    // separate classes.  EAL
    public IconModel getIconModel() {
        return _iconModel;
    }
    public PortModel getPortModel() {
        return _portModel;
    }
    public ExternalPortModel getExternalPortModel() {
        return _externalPortModel;
    }
    public VertexModel getVertexModel() {
        return _vertexModel;
    }
    // End of FIXME.

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the graph model.  This is called whenever a change request is
     *  executed.  In this class the internal set of link objects is
     *  verified to be correct.  This method is usually called just before
     *  issuing a graph event.  If this method returns false, then the graph
     *  event should not be issued, because further changes are necessary.
     *  @return true if the model was successfully updated, or false if
     *  further change requests were queued.
     */
    protected boolean _update() {

        // Go through all the links that currently exist, and remove
        // any that don't have both ends in the model.
        Iterator links = _linkSet.iterator();
        while (links.hasNext()) {
            Link link = (Link)links.next();
            Relation relation = link.getRelation();
            if (relation == null) continue;
            // Undo needs this: Check that the relation hasn't been removed
            if (relation.getContainer() == null) {
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
                if (headObj instanceof Port && tailObj instanceof Port &&
                        relation.getContainer() != null) {
                    NamedObj container =
                        _getChangeRequestParent(getPtolemyModel());
                    // remove the relation  This should trigger removing the
                    // other link.  This avoids turning a direct connection
                    // into a half connection with a diamond.
                    // Note that the source is NOT the graphmodel, so this
                    // will trigger the changerequest listener to
                    // redraw the graph again.
                    MoMLChangeRequest request = new MoMLChangeRequest(
                            container, container,
                            "<deleteRelation name=\""
                            + relation.getName(container)
                            + "\"/>\n");
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
            Iterator relations = ((CompositeEntity)ptolemyModel)
                    .relationList().iterator();
            while (relations.hasNext()) {
                _updateLinks((ComponentRelation)relations.next());
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check to make sure that there is a Link object representing every
    // link connected to the given relation.  In some cases, it may
    // be necessary to create a vertex to represent the relation as well.
    private void _updateLinks(ComponentRelation relation) {

        List linkedPortList = relation.linkedPortList();
        int allPortCount = linkedPortList.size();

        //  System.out.println("updating links for relation " + relation.getFullName());
        // System.out.println("linkedPorts = " + linkedPortList);
        // Go through all the links that currently exist, and remove ports
        // from the linkedPortList that already have a Link object.
        // Also remove links that link to ports which shouldn't be linked to.
        // FIXME this could get expensive
        Iterator links = new LinkedList(_linkSet).iterator();
        while (links.hasNext()) {
            Link link = (Link)links.next();
            // only consider links that are associated with this relation.
            if (link.getRelation() != relation) {
                continue;
            }

            // remove any ports that this link is linked to.  We don't need
            // to manufacture those links.
            Object tail = link.getTail();
            Object tailObj = getSemanticObject(tail);
            if (tailObj != null && linkedPortList.contains(tailObj)) {
                linkedPortList.remove(tailObj);
            } else if (tailObj != relation) {
                //  System.out.println("removing link = " + link);
                link.setHead(null);
                link.setTail(null);
                _linkSet.remove(link);
            }

            Object head = link.getHead();
            Object headObj = getSemanticObject(head);
            if (headObj != null && linkedPortList.contains(headObj)) {
                linkedPortList.remove(headObj);
            } else if (headObj != relation) {
                // System.out.println("removing link = " + link);
                link.setHead(null);
                link.setTail(null);
                _linkSet.remove(link);
            }
        }

        // Count the linked ports.
        int unlinkedPortCount = linkedPortList.size();

        // If there are no links left to create, then just return.
        if (unlinkedPortCount == 0) return;

        Iterator vertexes = relation.attributeList(Vertex.class).iterator();
        // get the Root vertex.  This is where we will manufacture links.
        Vertex rootVertex = null;
        while (vertexes.hasNext()) {
            Vertex v = (Vertex)vertexes.next();
            if (v.getLinkedVertex() == null) {
                rootVertex = v;
            }
        }

        // If there are no vertecies, and the relation has exactly
        // two connections, neither of which has been made yet, then
        // create a link without a vertex for the relation.
        if (rootVertex == null && allPortCount == 2 && unlinkedPortCount == 2) {
            Port port1 = (Port)linkedPortList.get(0);
            Port port2 = (Port)linkedPortList.get(1);
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
            }
            catch (Exception e) {
                throw new InternalErrorException("Failed to create " +
                        "new link, even though one does not " +
                        "already exist:" + e.getMessage());
            }
            link.setRelation(relation);
            link.setHead(head);
            link.setTail(tail);
        } else {
            // A regular relation with a diamond.
            // Create a vertex if one is not found
            if (rootVertex == null) {
                try {
                    rootVertex = new Vertex(relation,
                            relation.uniqueName("vertex"));
                }
                catch (Exception e) {
                    throw new InternalErrorException(
                            "Failed to create " +
                            "new vertex, even though one does not " +
                            "already exist:" + e.getMessage());
                }
            }
            // Connect all the links for that relation.
            Iterator ports = linkedPortList.iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                Object head = null;
                if (port.getContainer().equals(getRoot())) {
                    head = _getLocation(port);
                } else {
                    head = port;
                }

                Link link;
                try {
                    link = new Link();
                    _linkSet.add(link);
                }
                catch (Exception e) {
                    throw new InternalErrorException(
                            "Failed to create " +
                            "new link, even though one does not " +
                            "already exist:" + e.getMessage());
                }
                link.setRelation(relation);
                link.setHead(head);
                link.setTail(rootVertex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of all links in the model.
    private Set _linkSet;

    // The models of the different types of nodes and edges.
    private ExternalPortModel _externalPortModel = new ExternalPortModel();
    private IconModel _iconModel = new IconModel();
    private LinkModel _linkModel = new LinkModel();
    private PortModel _portModel = new PortModel();
    private VertexModel _vertexModel = new VertexModel();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The model for ports that make external connections to this graph.
     *  These ports are always contained by the root of this graph model.
     */
    public class ExternalPortModel extends NamedObjNodeModel {
        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model.
         *  @return A valid MoML string.
         */
        public String getDeleteNodeMoML(Object node) {
            Locatable location = (Locatable)node;
            ComponentPort port = (ComponentPort)location.getContainer();

            NamedObj container = _getChangeRequestParent(getPtolemyModel());

            StringBuffer moml = new StringBuffer();
            moml.append("<deletePort name=\"" +
                    port.getName(container) +
                    "\"/>\n");
            return moml.toString();
        }

        /**
         * Return the graph parent of the given node.
         * @param node The node, which is assumed to be a port contained in
         * the root of this graph model.
         * @return The root of this graph model.
         */
        public Object getParent(Object node) {
            return ((Locatable)node).getContainer().getContainer();
        }

        /**
         * Return an iterator over the edges coming into the given node.
         * This method first ensures that there is a link
         * object for every link.
         * Then the iterator is constructed by
         * removing any links that do not have the given node as head.
         * @param node The node, which is assumed to be a port contained in
         * the root of this graph model.
         * @return An iterator of Link objects, all of which have
         * the given node as their head.
         */
        public Iterator inEdges(Object node) {
            Locatable location = (Locatable)node;
            ComponentPort port = (ComponentPort)location.getContainer();
            // make sure that the links to relations that we are connected to
            // are up to date.

            // Go through all the links, creating a list of
            // those we are connected to.
            List portLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Link link = (Link)links.next();
                Object head = link.getHead();
                if (head != null && head.equals(location)) {
                    portLinkList.add(link);
                }
            }

            return portLinkList.iterator();
        }

        /**
         * Return an iterator over the edges coming out of the given node.
         * This iterator is constructed by looping over all the relations
         * that the port is connected to, and ensuring that there is a link
         * object for every link.  Then the iterator is constructed by
         * removing any links that do not have the given node as tail.
         * @param node The node, which is assumed to be a port contained in
         * the root of this graph model.
         * @return An iterator of Link objects, all of which have their
         * tail as the given node.
         */
        public Iterator outEdges(Object node) {
            Locatable location = (Locatable)node;
            ComponentPort port = (ComponentPort)location.getContainer();
            // make sure that the links to relations that we are connected to
            // are up to date.

            // Go through all the links, creating a list of
            // those we are connected to.
            List portLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Link link = (Link)links.next();
                Object tail = link.getTail();
                if (tail != null && tail.equals(location)) {
                    portLinkList.add(link);
                }
            }

            return portLinkList.iterator();
        }

        /** Remove the given edge from the model
         */
        public void removeNode(final Object eventSource, Object node) {
            Locatable location = (Locatable)node;
            ComponentPort port = (ComponentPort)location.getContainer();

            NamedObj container = _getChangeRequestParent(port);

            StringBuffer moml = new StringBuffer();
            moml.append("<deletePort name=\"" +
                    port.getName(container) +
                    "\"/>\n");

            // Note: The source is NOT the graph model.
            MoMLChangeRequest request =
                new MoMLChangeRequest(this,
                        container,
                        moml.toString());
            request.setUndoable(true);
            container.requestChange(request);
        }
    }

    /** The model for an icon that contains ports.
     */
    public class IconModel extends NamedObjNodeModel
        implements CompositeNodeModel {
        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model.
         *  @return A valid MoML string.
         */
        public String getDeleteNodeMoML(Object node) {
            NamedObj deleteObj = (NamedObj)((Locatable)node).getContainer();

            NamedObj container = _getChangeRequestParent(getPtolemyModel());

            String moml = "<deleteEntity name=\""
                + deleteObj.getName(container) + "\"/>\n";
            return moml;
        }

        /**
         * Return the number of nodes contained in
         * this graph or composite node.
         * @param composite The composite, which is assumed to be an icon.
         * @return The number of ports contained in the container of the icon.
         */
        public int getNodeCount(Object composite) {
            Locatable location = (Locatable) composite;
            return ((ComponentEntity)location.getContainer()).portList().size();
        }

        /**
         * Return the graph parent of the given node.
         * @param node The node, which is assumed to be an icon.
         * @return The container of the Icon's container, which should be
         * the root of the graph.
         */
        public Object getParent(Object node) {
            return ((Locatable)node).getContainer().getContainer();
        }

        /**
         * Return an iterator over the edges coming into the given node.
         * @param node The node, which is assumed to be an icon.
         * @return A NullIterator, since no edges are attached to icons.
         */
        public Iterator inEdges(Object node) {
            return new NullIterator();
        }

        /**
         * Provide an iterator over the nodes in the
         * given graph or composite node. The nodes are ports, so if the
         * container of the node is not an entity, then an empty iterator
         * is returned.  This iterator
         * does not necessarily support removal operations.
         * @param composite The composite, which is assumed to be an icon.
         * @return An iterator over the ports contained in the container
         * of the icon.
         */
        public Iterator nodes(Object composite) {
            Locatable location = (Locatable) composite;
            Nameable container = location.getContainer();
            if (container instanceof ComponentEntity) {
                ComponentEntity entity = (ComponentEntity)container;
                return entity.portList().iterator();
            } else {
                return new NullIterator();
            }
        }

        /**
         * Return an iterator over the edges coming out of the given node.
         * @param node The node, which is assumed to be an icon.
         * @return A NullIterator, since no edges are attached to icons.
         */
        public Iterator outEdges(Object node) {
            return new NullIterator();
        }

        /** Remove the given node from the model.  The node is assumed
         *  to be an icon.
         */
        public void removeNode(final Object eventSource, Object node) {
            NamedObj deleteObj = (NamedObj)((Locatable)node).getContainer();
            String elementName = null;
            if (deleteObj instanceof ComponentEntity) {
                // Object is an entity.
                elementName = "deleteEntity";
            } else {
                throw new InternalErrorException(
                        "Attempt to remove a node that is not an Entity. " +
                        "node = " + node);
            }

            // Make the request in the context of the container.
            NamedObj container = _getChangeRequestParent(deleteObj);
            // System.out.println("Queueing Change request with: " + container);

            String moml = "<" + elementName + " name=\""
                + deleteObj.getName(container) + "\"/>\n";


            // Note: The source is NOT the graph model.
            MoMLChangeRequest request =
                new MoMLChangeRequest(
                        this, container, moml);
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
        public boolean acceptHead(Object edge, Object node) {
            if (node instanceof Port ||
                    node instanceof Vertex ||
                    (node instanceof Locatable &&
                            ((Locatable)node).getContainer() instanceof Port)) {
                return true;
            } else
                return false;
        }

        /** Return true if the tail of the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be a link.
         *  @param node The node to attach to.
         *  @return True if the node is a port or a vertex, or a location
         *  representing a port.
         */
        public boolean acceptTail(Object edge, Object node) {
            if (node instanceof Port ||
                    node instanceof Vertex ||
                    (node instanceof Locatable &&
                            ((Locatable)node).getContainer() instanceof Port)) {
                return true;
            } else
                return false;
        }

        /** Return a MoML String that will delete the given edge from the
         *  Ptolemy model.
         *  @return A valid MoML string.
         */
        public String getDeleteEdgeMoML(Object edge) {
            final Link link = (Link)edge;
            NamedObj linkHead = (NamedObj)link.getHead();
            NamedObj linkTail = (NamedObj)link.getTail();
            Relation linkRelation = (Relation)link.getRelation();
            // This moml is parsed to execute the change
            StringBuffer moml = new StringBuffer();
            // Make the request in the context of the container.
            // JDK1.2.2 fails to compile the next line.
            NamedObj container =
                (CompositeEntity)_getChangeRequestParent(getPtolemyModel());

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
         */
        public Object getHead(Object edge) {
            return ((Link)edge).getHead();
        }

        /** Return the tail node of the specified edge.
         *  @param edge The edge, which is assumed to be a link.
         *  @return The node that is the tail of the specified edge.
         */
        public Object getTail(Object edge) {
            return ((Link)edge).getTail();
        }

        /** Return true if this edge is directed.
         *  In this model, none of edges
         *  are directed, so this always returns false.
         *  @param edge The edge, which is assumed to be a link.
         *  @return False.
         */
        public boolean isDirected(Object edge) {
            return false;
        }

        /** Append moml to the given buffer that disconnects a link with the
         *  given head, tail, and relation. Names in the returned moml will be
         *  relative to the given container. If either linkHead or linkTail
         *  is null, then nothing will be appended to the moml buffer.
         *  @return True if any MoML is appended to the moml argument.
         */
        private boolean _unlinkMoML(
                NamedObj container,
                StringBuffer moml,
                NamedObj linkHead,
                NamedObj linkTail,
                Relation relation) throws Exception {
            // If the link is already connected, then create a bit of MoML
            // to unlink the link.
            if (linkHead != null && linkTail != null) {
                NamedObj head = (NamedObj)getSemanticObject(linkHead);
                NamedObj tail = (NamedObj)getSemanticObject(linkTail);
                if (head instanceof ComponentPort &&
                        tail instanceof ComponentPort) {
                    ComponentPort headPort = (ComponentPort)head;
                    ComponentPort tailPort = (ComponentPort)tail;
                    // Unlinking two ports with an anonymous relation.
                    moml.append("<unlink port=\"" +
                            headPort.getName(container) +
                            "\" relation=\"" +
                            relation.getName(container) +
                            "\"/>\n");
                    moml.append("<unlink port=\"" +
                            tailPort.getName(container) +
                            "\" relation=\"" +
                            relation.getName(container) +
                            "\"/>\n");
                    moml.append("<deleteRelation name=\"" +
                            relation.getName(container) +
                            "\"/>\n");
                } else if (head instanceof ComponentPort &&
                        linkTail instanceof Vertex) {
                    // Unlinking a port from an existing relation.
                    moml.append("<unlink port=\"" +
                            head.getName(container) +
                            "\" relation=\"" +
                            tail.getName(container) +
                            "\"/>\n");
                } else if (tail instanceof ComponentPort &&
                        linkHead instanceof Vertex) {
                    // Unlinking a port from an existing relation.
                    moml.append("<unlink port=\"" +
                            tail.getName(container) +
                            "\" relation=\"" +
                            head.getName(container) +
                            "\"/>\n");
                } else {
                    throw new RuntimeException(
                            "Unlink failed: " +
                            "Head = " + head + ", Tail = " + tail);
                }
                return true;
            } else {
                // No unlinking to do.
                return false;
            }
        }

        /** Append moml to the given buffer that connects a link with the
         *  given head, tail, and relation.  Names in the returned moml will be
         *  relative to the given container.  This may require adding an
         *  anonymous relation to the ptolemy model.
         *  If no relation need be added, then return null.
         */
        private String _linkMoML(
                NamedObj container,
                StringBuffer moml,
                StringBuffer failmoml,
                NamedObj linkHead,
                NamedObj linkTail) throws Exception {
            if (linkHead != null && linkTail != null) {
                NamedObj head = (NamedObj)getSemanticObject(linkHead);
                NamedObj tail = (NamedObj)getSemanticObject(linkTail);
                if (head instanceof ComponentPort &&
                        tail instanceof ComponentPort) {
                    ComponentPort headPort = (ComponentPort)head;
                    ComponentPort tailPort = (ComponentPort)tail;
                    NamedObj ptolemyModel = getPtolemyModel();
                    // Linking two ports with a new relation.
                    String relationName =
                        ptolemyModel.uniqueName("relation");
                    // If the context is not the entity that we're editing,
                    // then we need to set the context correctly.
                    if (ptolemyModel != container) {
                        String contextString = "<entity name=\"" +
                            ptolemyModel.getName(container) +
                            "\">\n";
                        moml.append(contextString);
                        failmoml.append(contextString);
                    }
                    // Note that we use no class so that we use the container's
                    // factory method when this gets parsed
                    moml.append("<relation name=\"" + relationName + "\"/>\n");
                    moml.append("<link port=\"" +
                            headPort.getName(ptolemyModel) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
                    moml.append("<link port=\"" +
                            tailPort.getName(ptolemyModel) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");

                    // Record moml so that we can blow away these
                    // links in case we can't create them
                    failmoml.append("<unlink port=\"" +
                            headPort.getName(ptolemyModel) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
                    failmoml.append("<unlink port=\"" +
                            tailPort.getName(ptolemyModel) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
                    failmoml.append("<deleteRelation name=\"" +
                            relationName + "\"/>\n");
                    // close the context
                    if (ptolemyModel != container) {
                        moml.append("</entity>");
                        failmoml.append("</entity>");
                    }

                    // Ugh this is ugly.
                    if (ptolemyModel != container) {
                        return ptolemyModel.getName(container) + "." +
                            relationName;
                    } else {
                        return relationName;
                    }
                } else if (head instanceof ComponentPort &&
                        linkTail instanceof Vertex) {
                    // Linking a port to an existing relation.
                    moml.append("<link port=\"" +
                            head.getName(container) +
                            "\" relation=\"" +
                            tail.getName(container) +
                            "\"/>\n");
                    return tail.getName(container);
                } else if (tail instanceof ComponentPort &&
                        linkHead instanceof Vertex) {
                    // Linking a port to an existing relation.
                    moml.append("<link port=\"" +
                            tail.getName(container) +
                            "\" relation=\"" +
                            head.getName(container) +
                            "\"/>\n");
                    return head.getName(container);
                } else {
                    throw new RuntimeException(
                            "Link failed: " +
                            "Head = " + head + ", Tail = " + tail);
                }
            } else {
                // No Linking to do.
                return null;
            }
        }

        /** Connect the given edge to the given head node.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be a link.
         *  @param newLinkHead The new head for the edge, which is assumed to
         *  be a location representing a port, a port or a vertex.         */
        public void setHead(final Object edge, final Object newLinkHead) {
            final Link link = (Link)edge;
            final NamedObj linkHead = (NamedObj)link.getHead();
            final NamedObj linkTail = (NamedObj)link.getTail();
            Relation linkRelation = (Relation)link.getRelation();
            // This moml is parsed to execute the change
            final StringBuffer moml = new StringBuffer();
            // This moml is parsed in case the change fails.
            final StringBuffer failmoml = new StringBuffer();
            moml.append("<group>\n");
            failmoml.append("<group>\n");
            // Make the request in the context of the container.
            // JDK1.2.2 fails to compile the next line.
            final CompositeEntity container =
                (CompositeEntity)_getChangeRequestParent(getPtolemyModel());

            String relationName = "";

            // Flag specifying whether we have actually created any MoML.
            boolean appendedMoML = false;
            try {
                // create moml to unlink any existing.
                appendedMoML = _unlinkMoML(
                        container, moml, linkHead, linkTail, linkRelation);

                // create moml to make the new links.
                relationName = _linkMoML(
                        container, moml, failmoml,
                        (NamedObj)newLinkHead, linkTail);
                        
                appendedMoML = appendedMoML || (relationName != null);
            } catch (Exception ex) {
                // The link is bad... remove it.
                _linkSet.remove(link);
                link.setHead(null);
                link.setTail(null);
                dispatchGraphEvent(new GraphEvent(
                        ActorGraphModel.this,
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
            MoMLChangeRequest request =
                new MoMLChangeRequest(ActorGraphModel.this,
                        container,
                        moml.toString()) {
                        protected void _execute() throws Exception {
                            // If nonEmptyMoML is false, then the MoML code is empty.
                            // Do not execute it, as this will put spurious empty
                            // junk on the undo stack.
                            if (nonEmptyMoML) {
                                super._execute();
                            }
                            link.setHead(newLinkHead);
                            if (relationNameToAdd != null) {
                                ComponentRelation relation =
                                    (ComponentRelation)container.getRelation(
                                            relationNameToAdd);
                                if (relation == null)
                                    throw new InternalErrorException(
                                            "Tried to find relation with name "
                                            + relationNameToAdd
                                            + " in context "
                                            + container);
                                link.setRelation(relation);
                            } else {
                                link.setRelation(null);
                            }
                        }
                    };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new LinkChangeListener(link,
                    container,
                    failmoml));

            request.setUndoable(true);
            container.requestChange(request);
        }

        /** Connect the given edge to the given tail node.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be a link.
         *  @param newLinkTail The new tail for the edge, which is
         *  assumed to be a location representing a port, a port or a
         *  vertex.
         */
        public void setTail(final Object edge, final Object newLinkTail) {
            final Link link = (Link)edge;
            final NamedObj linkHead = (NamedObj)link.getHead();
            final NamedObj linkTail = (NamedObj)link.getTail();
            Relation linkRelation = (Relation)link.getRelation();
            // This moml is parsed to execute the change
            final StringBuffer moml = new StringBuffer();
            // This moml is parsed in case the change fails.
            final StringBuffer failmoml = new StringBuffer();
            moml.append("<group>\n");
            failmoml.append("<group>\n");

            // Make the request in the context of the container.
            // JDK1.2.2 fails to compile the next line.
            final CompositeEntity container =
                (CompositeEntity)_getChangeRequestParent(getPtolemyModel());

            String relationName = "";

            // Flag specifying whether we have actually created any MoML.
            boolean appendedMoML = false;
            try {
                // create moml to unlink any existing.
                appendedMoML = _unlinkMoML(
                        container, moml, linkHead, linkTail, linkRelation);

                // create moml to make the new links.
                relationName = _linkMoML(
                        container, moml, failmoml,
                        linkHead, (NamedObj)newLinkTail);

                appendedMoML = appendedMoML || (relationName != null);
            } catch (Exception ex) {
                // The link is bad... remove it.
                _linkSet.remove(link);
                link.setHead(null);
                link.setTail(null);
                dispatchGraphEvent(new GraphEvent(
                        ActorGraphModel.this,
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
            MoMLChangeRequest request =
                new MoMLChangeRequest(ActorGraphModel.this,
                        container,
                        moml.toString()) {
                        protected void _execute() throws Exception {
                            // If nonEmptyMoML is false, then the MoML code is empty.
                            // Do not execute it, as this will put spurious empty
                            // junk on the undo stack.
                            if (nonEmptyMoML) {
                                super._execute();
                            }
                            link.setTail(newLinkTail);
                            if (relationNameToAdd != null) {
                                ComponentRelation relation =
                                    (ComponentRelation)container.getRelation(
                                            relationNameToAdd);
                                if (relation == null)
                                    throw new InternalErrorException(
                                            "Tried to find relation with name "
                                            + relationNameToAdd
                                            + " in context "
                                            + container);
                                link.setRelation(relation);
                            } else {
                                link.setRelation(null);
                            }
                        }
                    };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new LinkChangeListener(link,
                    container,
                    failmoml));

            request.setUndoable(true);
            container.requestChange(request);
        }

        /** This change listener is responsible for dispatching graph events
         *  when an edge is moved.  It works the same for heads and tails.
         */
        public class LinkChangeListener implements ChangeListener {
            public LinkChangeListener(Link link, CompositeEntity container,
                    StringBuffer failMoML) {
                _link = link;
                _container = container;
                _failMoML = failMoML;
            }

            public void changeFailed(ChangeRequest change,
                    Exception exception) {
                // If we fail here, then we remove the link entirely.
                _linkSet.remove(_link);
                _link.setHead(null);
                _link.setTail(null);
                _link.setRelation(null);

                // and queue a new change request to clean up the model
                // Note: JDK1.2.2 requires that this variable not be
                // called request or we get a compile error.
                // Note the source is NOT the graph model
                MoMLChangeRequest changeRequest =
                    new MoMLChangeRequest(this,
                            _container,
                            _failMoML.toString());
                // fail moml not undoable
                _container.requestChange(changeRequest);
            }

            public void changeExecuted(ChangeRequest change) {
                // modification to the linkset HAS to occur in the swing
                // thread.
                if (GraphUtilities.isPartiallyContainedEdge(_link,
                        getRoot(),
                        ActorGraphModel.this)) {
                    _linkSet.add(_link);
                } else {
                    _linkSet.remove(_link);
                }

                // Note that there is no GraphEvent dispatched here
                // if the edge is not fully connected.  This is to
                // prevent rerendering while
                // an edge is being created.
                if (_link.getHead() != null && _link.getTail() != null)
                    dispatchGraphEvent(
                            new GraphEvent(ActorGraphModel.this,
                                    GraphEvent.STRUCTURE_CHANGED,
                                    getRoot()));
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
         *  Ptolemy model.
         *  @return A valid MoML string.
         */
        public String getDeleteNodeMoML(Object node) {
            NamedObj deleteObj = (NamedObj)((Locatable)node).getContainer();

            NamedObj container = _getChangeRequestParent(getPtolemyModel());

            String moml = "<deletePort name=\""
                + deleteObj.getName(container) + "\"/>\n";
            return moml;
        }

        /**
         * Return the graph parent of the given node.
         * @param node The node, which is assumed to be a port.
         * @return The (presumably unique) icon contained in the port's
         * container.
         */
        public Object getParent(Object node) {
            ComponentPort port = (ComponentPort)node;
            Entity entity = (Entity)port.getContainer();
            if (entity == null) return null;
            List locationList = entity.attributeList(Locatable.class);
            if (locationList.size() > 0) {
                return locationList.get(0);
            } else {
                throw new InternalErrorException(
                        "Found an entity that does not contain an icon.");
            }
        }

        /**
         * Return an iterator over the edges coming into the given node.
         * This method first ensures that there is a link
         * object for every link.  Then the iterator is constructed by
         * removing any links that do not have the given node as head.
         * @param node The node, which is assumed to be a port contained in
         * the root of this graph model.
         * @return An iterator of Link objects, all of which have their
         * head as the given node.
         */
        public Iterator inEdges(Object node) {
            ComponentPort port = (ComponentPort)node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List portLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Link link = (Link)links.next();
                Object head = link.getHead();
                if (head != null && head.equals(port)) {
                    portLinkList.add(link);
                }
            }
            return portLinkList.iterator();
        }

        /**
         * Return an iterator over the edges coming out of the given node.
         * This iterator is constructed by looping over all the relations
         * that the port is connected to, and ensuring that there is a link
         * object for every link.  Then the iterator is constructed by
         * removing any links that do not have the given node as tail.
         * @param node The node, which is assumed to be a port contained in
         * the root of this graph model.
         * @return An iterator of Link objects, all of which have their
         * tail as the given node.
         */
        public Iterator outEdges(Object node) {
            ComponentPort port = (ComponentPort)node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List portLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Link link = (Link)links.next();
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
         */
        public void removeNode(final Object eventSource, Object node) {
            ComponentPort port = (ComponentPort)node;

            NamedObj container = _getChangeRequestParent(port);

            // Delete the port.
            StringBuffer moml = new StringBuffer();
            moml.append("<deletePort name=\"" +
                    port.getName(container) +
                    "\"/>\n");

            // Note: The source is NOT the graph model.
            MoMLChangeRequest request =
                new MoMLChangeRequest(this,
                        container,
                        moml.toString());
            request.setUndoable(true);
            container.requestChange(request);
        }
    }

    /** The model for vertexes that are contained within the relations of the
     *  ptolemy model.
     */
    public class VertexModel extends NamedObjNodeModel {
        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model.
         *  @return A valid MoML string.
         */
        public String getDeleteNodeMoML(Object node) {
            ComponentRelation deleteObj =
                (ComponentRelation)((Vertex)node).getContainer();

            NamedObj container = _getChangeRequestParent(getPtolemyModel());
            //  System.out.println("container = " + container.getFullName());
            String moml = "<deleteRelation name=\""
                + deleteObj.getName(container) + "\"/>\n";
            return moml;
        }

        /**
         * Return the graph parent of the given node.
         * @param node The node, which is assumed to be a Vertex.
         * @return The container of the vertex's container, which is
         * presumably the root of the graph model.
         */
        public Object getParent(Object node) {
            // Undo: If we use automatic layout, then we need to check to
            // see if the container is null here.
            if (((Vertex)node).getContainer() == null) {
                return null;
            }
            return ((Vertex)node).getContainer().getContainer();
        }

        /**
         * Return an iterator over the edges coming into the given node.
         * This method ensures that there is a link object for
         * every link to the relation contained by the vertex.
         * Then the iterator is constructed by
         * removing any links that do not have the given node as head.
         * @param node The node, which is assumed to be a vertex contained in
         * a relation.
         * @return An iterator of Link objects, all of which have their
         * head as the given node.
         */
        public Iterator inEdges(Object node) {
            Vertex vertex = (Vertex) node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List vertexLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Link link = (Link)links.next();
                Object head = link.getHead();
                if (head != null && head.equals(vertex)) {
                    vertexLinkList.add(link);
                }
            }
            return vertexLinkList.iterator();
        }

        /**
         * Return an iterator over the edges coming into the given node.
         * This method ensures that there is a link object for
         * every link to the relation contained by the vertex.
         * Then the iterator is constructed by
         * removing any links that do not have the given node as head.
         * @param node The node, which is assumed to be a vertex contained in
         * a relation.
         * @return An iterator of Link objects, all of which have their
         * tail as the given node.
         */
        public Iterator outEdges(Object node) {
            Vertex vertex = (Vertex) node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List vertexLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Link link = (Link)links.next();
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
         */
        public void removeNode(final Object eventSource, Object node) {
            ComponentRelation relation =
                (ComponentRelation)((Vertex)node).getContainer();

            NamedObj container = _getChangeRequestParent(relation);

            // Delete the relation.
            StringBuffer moml = new StringBuffer();
            moml.append("<deleteRelation name=\"" +
                    relation.getName(container) +
                    "\"/>\n");

            // Note: The source is NOT the graph mode.
            MoMLChangeRequest request =
                new MoMLChangeRequest(this,
                        container, moml.toString());
            request.setUndoable(true);
            container.requestChange(request);
        }
    }
}
