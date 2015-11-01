/* A graph model for graphically manipulating ontology models.

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
package ptolemy.vergil.ontologies;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptRelation;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Flowable;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.NamedObjNodeModel;
import ptolemy.vergil.kernel.Link;
import diva.graph.GraphEvent;
import diva.graph.GraphUtilities;
import diva.graph.modular.EdgeModel;
import diva.graph.modular.MutableEdgeModel;
import diva.graph.modular.NodeModel;

///////////////////////////////////////////////////////////////////
//// OntologyGraphModel

/** A graph model for graphically manipulating ontology models. Most of this
 *  code is duplicated from {@link ptolemy.vergil.modal.FSMGraphModel} but
 *  many features of the Modal Model
 *  editors are not used, so it doesn't make sense to inherit directly from
 *  that class.

    @author Charles Shelton, Edward A. Lee
    @version $Id$
    @since Ptolemy II 10.0
    @Pt.ProposedRating Red (cshelton)
    @Pt.AcceptedRating Red (cshelton)
 */
// FIXME: Create a base class for both OntologyGraphModel and FSMGraphModel
// that captures most of the redundant code.
public class OntologyGraphModel extends AbstractBasicGraphModel {

    /** Construct a new graph model whose root is the given composite entity.
     *  This should always be an {@link Ontology} composite entity.
     *  @param composite The top-level composite entity for the model.
     */
    public OntologyGraphModel(CompositeEntity composite) {
        super(composite);
        _linkSet = new HashSet();
        _update();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Disconnect an edge (a {@link Link} object which contains a {@link
     *  ConceptRelation} in the ontology model) from its two endpoints and
     *  notify graph listeners with an EDGE_HEAD_CHANGED and an
     *  EDGE_TAIL_CHANGED event whose source is the given source.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @param edge The edge to be disconnected.
     */
    @Override
    public void disconnectEdge(Object eventSource, Object edge) {
        Object head = _relationModel.getHead(edge);
        Object tail = _relationModel.getTail(edge);
        _relationModel.removeEdge(edge);

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

    /** Get the concept model which maps all nodes in the graph to the
     *  {@link Concept} elements in the
     *  ontology model.
     *  @return The concept model.
     */
    public ConceptModel getConceptModel() {
        return _conceptModel;
    }

    /** Return a MoML String that will delete the given edge from the
     *  Ptolemy model.
     *  @param edge The edge.
     *  @return A valid MoML string.
     */
    @Override
    public String getDeleteEdgeMoML(Object edge) {
        if (!(getEdgeModel(edge) instanceof RelationModel)) {
            return "";
        }

        RelationModel model = (RelationModel) getEdgeModel(edge);
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
     *  @return An instance of RelationModel if the object is a Link.
     *   Otherwise return null.
     */
    @Override
    public EdgeModel getEdgeModel(Object edge) {
        if (edge instanceof Link) {
            return _relationModel;
        } else {
            return null;
        }
    }

    /** Return the node model for the given object.  If the object is not
     *  a node, then return null. The nodes in an ontology model should be
     *  either {@link Concept Concepts} or Ptolemy
     *  annotation
     *  {@link ptolemy.vergil.kernel.attributes.TextAttribute TextAttributes}.
     *  @param node An object which is assumed to be in this graph model.
     *  @return The node model for the specified node, or null if there
     *   is none.
     */
    @Override
    public NodeModel getNodeModel(Object node) {
        if (node instanceof Locatable) {
            Object container = ((Locatable) node).getContainer();

            if (container instanceof Concept) {
                return _conceptModel;
            }
        }

        return super.getNodeModel(node);
    }

    /** Get the relation model. The only relations represented in the ontology
     *  should be {@link ConceptRelation ConceptRelations}.
     *  @return The relation model.
     */
    public RelationModel getRelationModel() {
        return _relationModel;
    }

    /** Return the semantic object corresponding to the given node, edge,
     *  or composite.  A "semantic object" is an object associated with
     *  a node in the graph.  If the argument is an instance of Locatable,
     *  then return the container of the Locatable, which should be a Concept
     *  or Attribute in the ontology. If it is a Link, then the semantic object
     *  is the link's ConceptRelation.
     *  @param element A graph element.
     *  @return The semantic object associated with this element, or null
     *   if the object is not recognized.
     */
    @Override
    public Object getSemanticObject(Object element) {
        if (element instanceof Link) {
            return ((Link) element).getRelation();
        }

        return super.getSemanticObject(element);
    }

    /** Delete a node from its parent graph and notify
     *  graph listeners with a NODE_REMOVED event.
     *  @param eventSource The source of the event that will be dispatched,
     *   e.g. the view that made this call.
     *  @param node The node to be removed.
     */
    @Override
    public void removeNode(Object eventSource, Object node) {
        if (!(getNodeModel(node) instanceof NamedObjNodeModel)) {
            return;
        }

        NamedObjNodeModel model = (NamedObjNodeModel) getNodeModel(node);
        model.removeNode(eventSource, node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the graph model.  This is called whenever a change request is
     *  executed.  In this class the internal set of link objects is
     *  verified to be correct.
     *  @return True if the model was successfully updated, or false if
     *   further change requests were queued.
     */
    @Override
    protected boolean _update() {
        // Go through all the links that currently exist, and remove
        // any that don't have both ends in the model.
        Iterator links = _linkSet.iterator();

        while (links.hasNext()) {
            Link link = (Link) links.next();
            ConceptRelation relation = (ConceptRelation) link.getRelation();

            if (relation == null) {
                continue;
            }

            // Check that the relation hasn't been removed.
            // If we do not do this, then redo will thrown an exception
            // if we open ct/demo/BouncingBall/BouncingBall.xml, look
            // inside the Ball Model composite actor,
            // delete the stop actor and then do undo and then redo.
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
                //Object headObj = getSemanticObject(link.getHead());
                //Object tailObj = getSemanticObject(link.getTail());
                link.setHead(null);
                link.setTail(null);
                link.setRelation(null);
                links.remove();

                NamedObj container = getPtolemyModel();

                // remove the relation  This should trigger removing the
                // other link. This will only happen when we've deleted
                // the concept at one end of the model.
                // Note that the source is NOT the graph model, so this
                // will trigger the ChangeRequest listener to
                // redraw the graph again.
                MoMLChangeRequest request = new MoMLChangeRequest(container,
                        container, "<deleteRelation name=\""
                                + relation.getName(container) + "\"/>\n");

                // Need to merge the undo for this request in with one that
                // triggered it
                request.setMergeWithPreviousUndo(true);
                request.setUndoable(true);
                container.requestChange(request);
                return false;
            }
        }

        // Now create Links for links that may be new
        Iterator relations = ((CompositeEntity) getPtolemyModel())
                .relationList().iterator();

        while (relations.hasNext()) {
            _updateLinks((ConceptRelation) relations.next());
        }

        return super._update();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check to make sure that there is a Link object representing
     *  the given relation.
     *  @param relation The relation to be checked.
     */
    private void _updateLinks(ConceptRelation relation) {
        Iterator links = _linkSet.iterator();
        Link foundLink = null;

        while (links.hasNext()) {
            Link link = (Link) links.next();

            // only consider links that are associated with this relation.
            if (link.getRelation() == relation) {
                foundLink = link;
                break;
            }
        }

        // A link exists, so there is nothing to do.
        if (foundLink != null) {
            return;
        }

        List linkedPortList = relation.linkedPortList();

        if (linkedPortList.size() != 2) {
            // Do nothing...  somebody else should take care of removing this,
            // because we have no way of representing it in this editor.
            return;
        }

        ComponentPort port1 = (ComponentPort) linkedPortList.get(0);
        Locatable location1 = _getLocation(port1.getContainer());
        ComponentPort port2 = (ComponentPort) linkedPortList.get(1);
        Locatable location2 = _getLocation(port2.getContainer());

        Link link;

        try {
            link = new Link();
        } catch (Exception e) {
            throw new InternalErrorException("Failed to create "
                    + "new link, even though one does not " + "already exist:"
                    + e.getMessage());
        }

        link.setRelation(relation);

        // We have to get the direction of the relation correct.
        if (((Flowable) port1.getContainer()).getIncomingPort().equals(port1)) {
            link.setHead(location1);
            link.setTail(location2);
        } else {
            link.setHead(location2);
            link.setTail(location1);
        }

        _linkSet.add(link);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The set of all links in the model. */
    private Set<Link> _linkSet;

    /** The model of concepts in the ontology model. */
    private ConceptModel _conceptModel = new ConceptModel();

    /** The model of relations in the ontology model. */
    private RelationModel _relationModel = new RelationModel();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The model for an icon that represents concepts in the ontology model.
     */
    public class ConceptModel extends NamedObjNodeModel {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return a MoML String that will delete the given node from the
         *  Ontology model. This assumes that the context is the container
         *  of the concept.
         *  @param node The node to be deleted.
         *  @return A valid MoML string.
         */
        @Override
        public String getDeleteNodeMoML(Object node) {
            NamedObj deleteObj = ((Locatable) node).getContainer();
            NamedObj container = deleteObj.getContainer();
            return "<deleteEntity name=\"" + deleteObj.getName(container)
                    + "\"/>\n";
        }

        /** Return the graph parent of the given node.
         *  @param node The node, which is assumed to be a location.
         *  @return The container of the icon's container, which should
         *   be the root of this graph model.
         */
        @Override
        public Object getParent(Object node) {
            return ((Locatable) node).getContainer().getContainer();
        }

        /** Return an iterator over the edges coming into the given Concept
         *  node.
         *  @param node The node, which is assumed to be a location attribute
         *   of a Concept in the ontology model.
         *  @return An iterator of Link objects, all of which have
         *   the given node as their head.
         */
        @Override
        public Iterator inEdges(Object node) {
            return _getEdgeIterator(node, true);
        }

        /** Return an iterator over the edges coming out of the given Concept
         *  node.
         *  @param node The node, which is assumed to be a location attribute
         *   of a Concept in the ontology model.
         *  @return An iterator of Link objects, all of which have
         *   the given node as their tail.
         */
        @Override
        public Iterator outEdges(Object node) {
            return _getEdgeIterator(node, false);
        }

        /** Remove the given node from the model.  The node is assumed
         *  to be a Locatable belonging to an entity.
         *  @param eventSource The source of the event directing the removal of
         *   the node.
         *  @param node The node to be removed.
         */
        @Override
        public void removeNode(final Object eventSource, Object node) {
            NamedObj deleteObj = ((Locatable) node).getContainer();

            // First remove all the in and out edges.
            // This isn't done automatically by the Ptolemy kernel, because
            // the kernel only removes the links to the relations. The
            // relations themselves are left in place.  But in the FSM
            // domain, it makes no sense to have a relation with only one
            // link to it.  So we remove the relations.
            Iterator inEdges = inEdges(node);

            while (inEdges.hasNext()) {
                disconnectEdge(eventSource, inEdges.next());
            }

            Iterator outEdges = outEdges(node);

            while (outEdges.hasNext()) {
                disconnectEdge(eventSource, outEdges.next());
            }

            String elementName = null;

            if (deleteObj instanceof Concept) {
                // Concept Object is an entity.
                elementName = "deleteEntity";
            } else {
                throw new InternalErrorException(
                        "Attempt to remove a node that is not a Concept. "
                                + "node = " + node);
            }

            String moml = "<" + elementName + " name=\"" + deleteObj.getName()
                    + "\"/>\n";

            // Make the request in the context of the container.
            NamedObj container = deleteObj.getContainer();
            MoMLChangeRequest request = new MoMLChangeRequest(
                    OntologyGraphModel.this, container, moml);
            request.addChangeListener(new ChangeListener() {
                @Override
                public void changeFailed(ChangeRequest change,
                        Exception exception) {
                    // If we fail, then issue structureChanged.
                    dispatchGraphEvent(new GraphEvent(eventSource,
                            GraphEvent.STRUCTURE_CHANGED, getRoot()));
                }

                @Override
                public void changeExecuted(ChangeRequest change) {
                    // If we succeed, then issue structureChanged, since
                    // this is likely connected to something.
                    dispatchGraphEvent(new GraphEvent(eventSource,
                            GraphEvent.STRUCTURE_CHANGED, getRoot()));
                }
            });
            request.setUndoable(true);
            container.requestChange(request);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Return an iterator over the edges coming into or out of the given
         *  Concept node. The iterator is constructed by removing any links
         *  that do not have the given node as either head or tail, depending
         *  on the value of the inEdges parameter.
         *  @param node The node, which is assumed to be a location attribute
         *   of a Concept in the ontology model.
         *  @param inEdges True if the method should return all the edges going
         *   into the node, false if it should return all the edges going out of
         *   the node.
         *  @return An iterator of Link objects, all of which have
         *   the given node as their head or tail.
         */
        private Iterator _getEdgeIterator(Object node, boolean inEdges) {
            Locatable nodeLocation = (Locatable) node;

            // Go through all the links, creating a list of
            // those we are connected to.
            List conceptLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();

            while (links.hasNext()) {
                Link link = (Link) links.next();
                Object endPoint = null;
                if (inEdges) {
                    endPoint = link.getHead();
                } else {
                    endPoint = link.getTail();
                }

                if (endPoint != null && endPoint.equals(nodeLocation)) {
                    conceptLinkList.add(link);
                }
            }
            return conceptLinkList.iterator();
        }
    }

    /** The model for relations between concepts in the ontology.
     */
    public class RelationModel implements MutableEdgeModel {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return true if the head of the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be a Link object.
         *  @param head The node to attach to.
         *  @return True if the node can be attached to the head of the edge.
         */
        @Override
        public boolean acceptHead(Object edge, Object head) {
            return _acceptConnection(edge, head);
        }

        /** Return true if the tail of the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be a Link object.
         *  @param tail The node to attach to.
         *  @return True if the node can be attached to the tail of the edge.
         */
        @Override
        public boolean acceptTail(Object edge, Object tail) {
            return _acceptConnection(edge, tail);
        }

        /** Return a MoML String that will delete the given edge from the
         *  Ptolemy model.
         *  @param edge The edge to be removed.
         *  @return A valid MoML string.
         */
        public String getDeleteEdgeMoML(Object edge) {
            final Link relationLink = (Link) edge;
            ConceptRelation ontologyRelation = (ConceptRelation) relationLink
                    .getRelation();

            // This moml is parsed to execute the change
            StringBuffer moml = new StringBuffer();

            // Make the request in the context of the container.
            NamedObj container = getPtolemyModel();
            moml.append(_deleteRelation(container, ontologyRelation));
            return moml.toString();
        }

        /** Return the head node of the given edge.
         *  @param edge The edge, which is assumed to be an instance of Link.
         *  @return The node that is the head of the specified edge.
         *  @see #getTail(Object)
         *  @see #setHead(Object, Object)
         */
        @Override
        public Object getHead(Object edge) {
            return ((Link) edge).getHead();
        }

        /** Return the tail node of the specified edge.
         *  @param edge The edge, which is assumed to be an instance of Link.
         *  @return The node that is the tail of the specified edge.
         *  @see #getHead(Object)
         *  @see #setTail(Object, Object)
         */
        @Override
        public Object getTail(Object edge) {
            return ((Link) edge).getTail();
        }

        /** Return true if this edge is directed.
         *  All ontology relations are directed, so this always returns true.
         *  @param edge The edge, which is assumed to be a Link object.
         *  @return True.
         */
        @Override
        public boolean isDirected(Object edge) {
            return true;
        }

        /** Remove the given edge and delete its associated relation.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be an arc.
         */
        public void removeEdge(final Object edge) {
            final Link relationLink = (Link) edge;
            NamedObj container = getPtolemyModel();
            String removeEdgeMoML = getDeleteEdgeMoML(relationLink);

            MoMLChangeRequest request = new MoMLChangeRequest(
                    OntologyGraphModel.this, container, removeEdgeMoML) {
                @Override
                protected void _execute() throws Exception {
                    super._execute();
                    relationLink.setHead(null);
                    relationLink.setTail(null);
                    relationLink.setRelation(null);
                }
            };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new ChangeListener() {
                @Override
                public void changeFailed(ChangeRequest change,
                        Exception exception) {
                    // Ignore... nothing we can do about it anyway.
                }

                @Override
                public void changeExecuted(ChangeRequest change) {
                    _linkSet.remove(relationLink);
                }
            });
            request.setUndoable(true);
            container.requestChange(request);
        }

        /** Connect the given edge to the given head node. If the specified
         *  head is null, then any pre-existing relation associated with
         *  this edge will be deleted.
         *  @param edge The edge, which is assumed to be an arc.
         *  @param head The new head for the edge, which is assumed to
         *   be an icon.
         *  @see #setTail(Object, Object)
         *  @see #getHead(Object)
         */
        @Override
        public void setHead(final Object edge, final Object head) {
            _setHeadOrTail(edge, head, true);
        }

        /** Connect the given edge to the given tail node. If the specified
         *  tail is null, then any pre-existing relation associated with
         *  this edge will be deleted.
         *  @param edge The edge, which is assumed to be an arc.
         *  @param tail The new tail for the edge, which is assumed to
         *   be an icon.
         *  @see #setHead(Object, Object)
         *  @see #getTail(Object)
         */
        @Override
        public void setTail(final Object edge, final Object tail) {
            _setHeadOrTail(edge, tail, false);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Return true if the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be a Link.
         *  @param node The node to attach to.
         *  @return True if the node is an instance of Locatable.
         */
        private boolean _acceptConnection(Object edge, Object node) {
            if (node instanceof Locatable) {
                return true;
            } else {
                return false;
            }
        }

        /** Return MoML to remove a relation in the specified container.
         *  @param container The direct container of the ontology relation in
         *   the Ptolemy model, which should be the Ontology.
         *  @param relation The relation to be deleted from the model.
         *  @return The MoML string that removes the relation.
         */
        private String _deleteRelation(NamedObj container,
                ConceptRelation relation) {
            return "<deleteRelation name=\"" + relation.getName(container)
                    + "\"/>\n";
        }

        /** Return either the head or tail object depending on the value of the
         *  isHead parameter.
         *  @param linkHead The head object of the relation.
         *  @param linkTail The tail object of the relation.
         *  @param isHead True to select linkHead, false to select linkTail.
         *  @return Either linkHead or linkTail, depending on the value of isHead.
         */
        private NamedObj _getHeadOrTail(NamedObj linkHead, NamedObj linkTail,
                boolean isHead) {
            if (isHead) {
                return linkHead;
            } else {
                return linkTail;
            }
        }

        /** Return either the head or tail port depending on the value of the
         *  isHead parameter.
         *  @param conceptHead The concept head object of the relation.
         *  @param conceptTail The concept tail object of the relation.
         *  @param isHead True to select linkHead, false to select linkTail.
         *  @return Either linkHead or linkTail, depending on the value of isHead.
         */
        private ComponentPort _getHeadOrTailPort(Flowable conceptHead,
                Flowable conceptTail, boolean isHead) {
            Flowable concept = (Flowable) _getHeadOrTail(
                    (NamedObj) conceptHead, (NamedObj) conceptTail, isHead);

            if (isHead) {
                return concept.getIncomingPort();
            } else {
                return concept.getOutgoingPort();
            }
        }

        /** Append moml to the given buffer that connects a link with the
         *  given head or tail.  If the relation argument is non-null, then that
         *  relation is used, and null is returned.  Otherwise, a new
         *  relation is created, and its name is returned. Names in the
         *  generated moml will be relative to the specified container.
         *  The failmoml argument is also populated, but with MoML code
         *  to execute if the link fails.  This disconnects any partially
         *  constructed link.
         *  @param container The container of the ontology relation which should
         *   be the Ontology model.
         *  @param moml The string buffer containing the moml to which the link
         *   moml code is appended.
         *  @param failmoml The string buffer containing the moml to which the
         *   unlink moml code is appended if the link operation fails.
         *  @param linkHead The model object connected to the head of the relation.
         *  @param linkTail The model object connected to the tail of the relation.
         *  @param linkRelation The relation being linked.
         *  @param isHead True if the head of the relation is being linked,
         *   false if the tail of the relation is being linked.
         *  @return The name of the relation being linked.
         */
        private String _linkHeadOrTail(NamedObj container, StringBuffer moml,
                StringBuffer failmoml, NamedObj linkHead, NamedObj linkTail,
                ConceptRelation linkRelation, boolean isHead) {
            if (linkHead != null && linkTail != null) {
                NamedObj head = (NamedObj) getSemanticObject(linkHead);
                NamedObj tail = (NamedObj) getSemanticObject(linkTail);

                if (head instanceof Flowable && tail instanceof Flowable) {
                    // When we connect two concepts, we actually connect the
                    // appropriate ports.
                    Flowable headConcept = (Flowable) head;
                    Flowable tailConcept = (Flowable) tail;
                    ComponentPort newPortToConnect = _getHeadOrTailPort(
                            headConcept, tailConcept, isHead);
                    ComponentPort existingPortToConnect = _getHeadOrTailPort(
                            headConcept, tailConcept, !isHead);
                    NamedObj ptolemyModel = getPtolemyModel();

                    // If the context is not the entity that we're editing,
                    // then we need to set the context correctly.
                    if (ptolemyModel != container) {
                        String contextString = "<entity name=\""
                                + ptolemyModel.getName(container) + "\">\n";
                        moml.append(contextString);
                        failmoml.append(contextString);
                    }

                    boolean createdNewRelation = false;
                    String relationName = null;

                    if (linkRelation != null) {
                        // Pre-existing relation. Use it.
                        relationName = linkRelation.getName(ptolemyModel);
                    } else {
                        createdNewRelation = true;

                        // Linking two ports with a new relation.
                        relationName = ptolemyModel.uniqueName("relation");

                        // Create the new relation.
                        // Note that we specify no class so that we use the
                        // container's factory method when this gets parsed
                        moml.append("<relation name=\"" + relationName
                                + "\"/>\n");
                        moml.append("<link port=\""
                                + existingPortToConnect.getName(ptolemyModel)
                                + "\" relation=\"" + relationName + "\"/>\n");
                    }

                    moml.append("<link port=\""
                            + newPortToConnect.getName(ptolemyModel)
                            + "\" relation=\"" + relationName + "\"/>\n");

                    // Record moml so that we can blow away these
                    // links in case we can't create them
                    failmoml.append("<unlink port=\""
                            + newPortToConnect.getName(ptolemyModel)
                            + "\" relation=\"" + relationName + "\"/>\n");

                    if (linkRelation == null) {
                        failmoml.append("<unlink port=\""
                                + existingPortToConnect.getName(ptolemyModel)
                                + "\" relation=\"" + relationName + "\"/>\n");
                        failmoml.append("<deleteRelation name=\""
                                + relationName + "\"/>\n");
                    }

                    // close the context
                    if (ptolemyModel != container) {
                        moml.append("</entity>");
                        failmoml.append("</entity>");
                    }

                    if (createdNewRelation) {
                        return relationName;
                    } else {
                        return null;
                    }
                } else {
                    throw new RuntimeException(
                            "Attempt to create a link between non-concepts: "
                                    + "Head = " + head + ", Tail = " + tail);
                }
            } else {
                // No Linking to do.
                return null;
            }
        }

        /** Connect the given edge to the given head or tail node.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be a link.
         *  @param newHeadOrTail The new head or tail for the edge,
         *   which is assumed to be a location representing a port,
         *   a port or a vertex.
         *  @param isHead True when newLinkHeadOrTail represents the head
         *  @see #setHead(Object, Object)
         *  @see #setTail(Object, Object)
         */
        private void _setHeadOrTail(final Object edge,
                final Object newHeadOrTail, final boolean isHead) {
            final Link relationLink = (Link) edge;
            NamedObj linkHead = (NamedObj) relationLink.getHead();
            NamedObj linkTail = (NamedObj) relationLink.getTail();
            ConceptRelation ontologyRelation = (ConceptRelation) relationLink
                    .getRelation();

            // This moml is parsed to execute the change
            final StringBuffer moml = new StringBuffer();

            // This moml is parsed in case the change fails.
            final StringBuffer failmoml = new StringBuffer();
            moml.append("<group>\n");
            failmoml.append("<group>\n");

            // Make the request in the context of the container.
            final NamedObj container = getPtolemyModel();

            // If there is a previous connection, remove it.
            if (ontologyRelation != null) {
                if (newHeadOrTail == null) {
                    // There will be no further connection, so just
                    // delete the relation.
                    moml.append(_deleteRelation(container, ontologyRelation));
                } else {
                    // There will be a further connection, so preserve
                    // the relation.
                    NamedObj headOrTailToBeUnlinked = _getHeadOrTail(linkHead,
                            linkTail, isHead);
                    moml.append(_unlinkHeadOrTail(container,
                            headOrTailToBeUnlinked, ontologyRelation, isHead));
                }
            }

            String relationName = null;

            if (newHeadOrTail != null) {
                // create moml to make the new link.
                NamedObj head = null;
                NamedObj tail = null;
                if (isHead) {
                    head = (NamedObj) newHeadOrTail;
                    tail = linkTail;
                } else {
                    head = linkHead;
                    tail = (NamedObj) newHeadOrTail;
                }
                relationName = _linkHeadOrTail(container, moml, failmoml, head,
                        tail, ontologyRelation, isHead);
            }

            moml.append("</group>\n");
            failmoml.append("</group>\n");

            final String relationNameToAdd = relationName;
            MoMLChangeRequest request = new MoMLChangeRequest(
                    OntologyGraphModel.this, container, moml.toString()) {
                @Override
                protected void _execute() throws Exception {
                    super._execute();
                    if (isHead) {
                        relationLink.setHead(newHeadOrTail);
                    } else {
                        relationLink.setTail(newHeadOrTail);
                    }

                    if (relationNameToAdd != null) {
                        ConceptRelation relation = (ConceptRelation) ((Ontology) getPtolemyModel())
                                .getRelation(relationNameToAdd);
                        relationLink.setRelation(relation);
                    }
                }
            };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new ChangeListener() {
                @Override
                public void changeFailed(ChangeRequest change,
                        Exception exception) {
                    // If we fail here, then we remove the link entirely.
                    _linkSet.remove(relationLink);
                    relationLink.setHead(null);
                    relationLink.setTail(null);
                    relationLink.setRelation(null);

                    // and queue a new change request to clean up the model
                    // Note: JDK1.2.2 requires that this variable not be
                    // called request or we get a compile error.
                    MoMLChangeRequest requestChange = new MoMLChangeRequest(
                            OntologyGraphModel.this, container, failmoml
                            .toString());

                    // Fail moml execution not undoable
                    container.requestChange(requestChange);
                }

                @Override
                public void changeExecuted(ChangeRequest change) {
                    if (GraphUtilities.isPartiallyContainedEdge(relationLink,
                            getRoot(), OntologyGraphModel.this)) {
                        _linkSet.add(relationLink);
                    } else {
                        _linkSet.remove(relationLink);
                    }
                }
            });
            request.setUndoable(true);
            container.requestChange(request);
        }

        /** Return MoML to unlink a relation with the given head or tail in the
         *  specified container.
         *  @param container The container that holds the ontology relation,
         *   which should be an Ontology model.
         *  @param headOrTail The head or tail model object to be unlinked.
         *  @param relation The relation to be unlinked.
         *  @param isHead True if the hearOrTail input is the head of the
         *   relation, and false if it is the tail of the relation.
         *  @return The MoML string to unlink the relation.
         */
        private String _unlinkHeadOrTail(NamedObj container,
                NamedObj headOrTail, ConceptRelation relation, boolean isHead) {
            NamedObj headOrTailObj = (NamedObj) getSemanticObject(headOrTail);
            Flowable conceptHeadOrTail = (Flowable) headOrTailObj;
            ComponentPort headOrTailPort = null;
            if (isHead) {
                headOrTailPort = conceptHeadOrTail.getIncomingPort();
            } else {
                headOrTailPort = conceptHeadOrTail.getOutgoingPort();
            }
            return "<unlink port=\"" + headOrTailPort.getName(container)
                    + "\" relation=\"" + relation.getName(container) + "\"/>\n";
        }
    }
}
