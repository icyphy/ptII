/* A graph model for ptolemy fsm models.

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

package ptolemy.vergil.fsm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.TypedActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.basic.NamedObjNodeModel;
import diva.graph.GraphEvent;
import diva.graph.GraphUtilities;
import diva.graph.modular.EdgeModel;
import diva.graph.modular.MutableEdgeModel;
import diva.graph.modular.NodeModel;
import diva.util.NullIterator;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphModel
/**
A graph model for graphically manipulating ptolemy FSM models.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class FSMGraphModel extends AbstractBasicGraphModel {

    /** Construct a new graph model whose root is the given composite entity.
     *  @param composite The top-level composite entity for the model.
     */
    public FSMGraphModel(CompositeEntity composite) {
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
        if (!(getEdgeModel(edge) instanceof ArcModel)) return;
        ArcModel model = (ArcModel)getEdgeModel(edge);
        Object head = model.getHead(edge);
        Object tail = model.getTail(edge);
        model.removeEdge(edge);
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
        if (!(getEdgeModel(edge) instanceof ArcModel)) return "";
        ArcModel model = (ArcModel)getEdgeModel(edge);
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

    /** Return the model for the given edge object.  If the object is not
     *  an edge, then return null.
     *  @param edge An object which is assumed to be in this graph model.
     *  @return An instance of ArcModel if the object is an Arc.
     *   Otherwise return null.
     */
    public EdgeModel getEdgeModel(Object edge) {
        if (edge instanceof Arc) {
            return _arcModel;
        } else {
            return null;
        }
    }

    /** Return the node model for the given object.  If the object is not
     *  a node, then return null.
     *  @param node An object which is assumed to be in this graph model.
     *  @return The node model for the specified node, or null if there
     *   is none.
     */
    public NodeModel getNodeModel(Object node) {
        if (node instanceof Locatable) {
            Object container = ((Locatable)node).getContainer();
            if (container instanceof ComponentEntity) {
                return _stateModel;
            } else if (container instanceof ComponentPort) {
                return _portModel;
            }
        }
        return super.getNodeModel(node);
    }

    /** Return the semantic object corresponding to the given node, edge,
     *  or composite.  A "semantic object" is an object associated with
     *  a node in the graph.  In this case, if the node is icon, the
     *  semantic object is the entity containing the icon.  If it is
     *  an arc, then the semantic object is the arc's relation.
     *  @param element A graph element.
     *  @return The semantic object associated with this element, or null
     *  if the object is not recognized.
     */
    public Object getSemanticObject(Object element) {
        if (element instanceof Arc) {
            return ((Arc)element).getRelation();
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
    public PortModel getPortModel() {
        return _portModel;
    }
    public StateModel getStateModel() {
        return _stateModel;
    }
    public ArcModel getArcModel() {
        return _arcModel;
    }
    // End of FIXME.

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the graph model.  This is called whenever a change request is
     *  executed.  In this class the internal set of link objects is
     *  verified to be correct.
     */
    protected boolean _update() {
        // Go through all the links that currently exist, and remove
        // any that don't have both ends in the model.
        Iterator links = _linkSet.iterator();
        while (links.hasNext()) {
            Arc link = (Arc)links.next();
            Relation relation = link.getRelation();
            if (relation == null) continue;
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
                Object headObj = getSemanticObject(link.getHead());
                Object tailObj = getSemanticObject(link.getTail());
                link.setHead(null);
                link.setTail(null);
                link.setRelation(null);
                links.remove();
                NamedObj container =
                    (NamedObj)_getChangeRequestParent(getPtolemyModel());
                // remove the relation  This should trigger removing the
                // other link. This will only happen when we've deleted
                // the state at one end of the model.
                // Note that the source is NOT the graph model, so this
                // will trigger the ChangeRequest listener to
                // redraw the graph again.
                MoMLChangeRequest request = new MoMLChangeRequest(
                        container, container,
                        "<deleteRelation name=\""
                        + relation.getName(container)
                        + "\"/>\n");
                // Need to merge the undo for this request in with one that
                // triggered it
                request.setMergeWithPreviousUndo(true);
                request.setUndoable(true);
                container.requestChange(request);
                return false;
            }
        }

        // Now create Links for links that may be new
        Iterator relations = ((CompositeEntity)getPtolemyModel())
                .relationList().iterator();
        while (relations.hasNext()) {
            _updateLinks((ComponentRelation)relations.next());
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check to make sure that there is an Arc object representing
    // the given relation.
    private void _updateLinks(ComponentRelation relation) {
        Iterator links = _linkSet.iterator();
        Arc foundLink = null;
        while (links.hasNext()) {
            Arc link = (Arc)links.next();
            // only consider links that are associated with this relation.
            if (link.getRelation() == relation) {
                foundLink = link;
                break;
            }
        }

        // A link exists, so there is nothing to do.
        if (foundLink != null) return;

        List linkedPortList = relation.linkedPortList();
        if (linkedPortList.size() != 2) {
            // Do nothing...  somebody else should take care of removing this,
            // because we have no way of representing it in this editor.
            return;
        }
        Port port1 = (Port)linkedPortList.get(0);
        Locatable location1 = _getLocation((NamedObj)port1.getContainer());
        Port port2 = (Port)linkedPortList.get(1);
        Locatable location2 = _getLocation((NamedObj)port2.getContainer());

        Arc link;
        try {
            link = new Arc();
        }
        catch (Exception e) {
            throw new InternalErrorException(
                    "Failed to create " +
                    "new link, even though one does not " +
                    "already exist:" + e.getMessage());
        }
        link.setRelation(relation);
        // We have to get the direction of the arc correct.
        if (((State)port1.getContainer()).incomingPort.equals(port1)) {
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

    // The set of all links in the model.
    private Set _linkSet;

    // The models of the different types of nodes and edges.
    private ArcModel _arcModel = new ArcModel();
    private PortModel _portModel = new PortModel();
    private StateModel _stateModel = new StateModel();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The model for arcs between states.
     */
    public class ArcModel implements MutableEdgeModel {

        /** Return true if the head of the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be an arc.
         *  @param node The node to attach to.
         *  @return True if the node is an icon.
         */
        public boolean acceptHead(Object edge, Object node) {
            if (node instanceof Locatable) {
                return true;
            } else
                return false;
        }

        /** Return true if the tail of the given edge can be attached to the
         *  given node.
         *  @param edge The edge to attach, which is assumed to be an arc.
         *  @param node The node to attach to.
         *  @return True if the node is an icon.
         */
        public boolean acceptTail(Object edge, Object node) {
            if (node instanceof Locatable) {
                return true;
            } else
                return false;
        }

        /** Return the head node of the given edge.
         *  @param edge The edge, which is assumed to be an instance of Arc.
         *  @return The node that is the head of the specified edge.
         */
        public Object getHead(Object edge) {
            return ((Arc)edge).getHead();
        }

        /** Return a MoML String that will delete the given edge from the
         *  Ptolemy model.
         *  @return A valid MoML string.
         */
        public String getDeleteEdgeMoML(Object edge) {
            final Arc link = (Arc)edge;
            NamedObj linkHead = (NamedObj)link.getHead();
            NamedObj linkTail = (NamedObj)link.getTail();
            Relation linkRelation = (Relation)link.getRelation();
            // This moml is parsed to execute the change
            StringBuffer moml = new StringBuffer();
            // Make the request in the context of the container.
            // JDK1.2.2 fails to compile the next line.
            NamedObj container =
                (CompositeEntity)_getChangeRequestParent(getPtolemyModel());

            moml.append(_deleteRelation(container, linkRelation));

            // See whether refinement(s) need to be removed.
            CompositeEntity master
                = (CompositeEntity)linkRelation.getContainer();
            // Nothing to do if there is no container.
            if (master != null) {
                // Remove any referenced refinements that are not also
                // referenced by other states.
                TypedActor[] refinements = null;
                try {
                    refinements = ((Transition)linkRelation).getRefinement();
                } catch (IllegalActionException e) {
                    // Ignore, no refinement to remove.
                }
                if (refinements != null) {
                    for (int i = 0; i < refinements.length; i++) {
                        TypedActor refinement = refinements[i];
                        // By default, if no other state or transition refers
                        // to this refinement, then we will remove it.
                        boolean removeIt = true;
                        Iterator states
                            = master.entityList(State.class).iterator();
                        while (removeIt && states.hasNext()) {
                            State state = (State)states.next();
                            TypedActor[] stateRefinements = null;
                            try {
                                stateRefinements = state.getRefinement();
                            } catch (IllegalActionException e1) {
                                // Ignore, no refinement to check.
                            }
                            if (stateRefinements == null) continue;
                            for (int j = 0; j < stateRefinements.length; j++) {
                                if (stateRefinements[j] == refinement) {
                                    removeIt = false;
                                    break;
                                }
                            }
                        }
                        
                        // Next check transitions.
                        Iterator transitions = master.relationList().iterator();
                        while (removeIt && transitions.hasNext()) {
                            Relation transition = (Relation)transitions.next();
                            if (transition == linkRelation
                                    || !(transition instanceof Transition)) {
                                continue;
                            }
                            TypedActor[] transitionRefinements = null;
                            try {
                                transitionRefinements
                                        = ((Transition)transition).getRefinement();
                            } catch (IllegalActionException e1) {
                                // Ignore, no refinement to check.
                            }
                            if (transitionRefinements == null) continue;
                            for (int j = 0; j < transitionRefinements.length;
                                    j++) {
                                if (transitionRefinements[j] == refinement) {
                                    removeIt = false;
                                    break;
                                }
                            }
                        }
                        if (removeIt) {
                            moml.append("<deleteEntity name=\""
                                    + ((NamedObj)refinement).getName(container)
                                    + "\"/>\n");
                        }
                    }
                }
            }
             
            return moml.toString();
        }

        /** Return the tail node of the specified edge.
         *  @param edge The edge, which is assumed to be an instance of Arc.
         *  @return The node that is the tail of the specified edge.
         */
        public Object getTail(Object edge) {
            return ((Arc)edge).getTail();
        }

        /** Return true if this edge is directed.
         *  All transitions are directed, so this always returns true.
         *  @param edge The edge, which is assumed to be an arc.
         *  @return True.
         */
        public boolean isDirected(Object edge) {
            return true;
        }

        /** Remove the given edge and delete its associated relation.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be an arc.
         */
        public void removeEdge(final Object edge) {
            final Arc link = (Arc)edge;
            Relation linkRelation = (Relation)link.getRelation();
            // This moml is parsed to execute the change
            final StringBuffer moml = new StringBuffer();
            // Make the request in the context of the container.
            final CompositeEntity container =
                (CompositeEntity)_getChangeRequestParent(getPtolemyModel());
            moml.append(_deleteRelation(container, linkRelation));
            MoMLChangeRequest request =
                new MoMLChangeRequest(FSMGraphModel.this,
                        container,
                        moml.toString()) {
                        protected void _execute() throws Exception {
                            super._execute();
                            link.setHead(null);
                            link.setTail(null);
                            link.setRelation(null);
                        }
                    };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new ChangeListener() {
                    public void changeFailed(ChangeRequest change,
                            Exception exception) {
                        // Ignore... nothing we can do about it anyway.
                    }

                    public void changeExecuted(ChangeRequest change) {
                        _linkSet.remove(edge);
                    }
                });
            request.setUndoable(true);
            container.requestChange(request);
        }

        /** Connect the given edge to the given head node. If the specified
         *  head is null, then any pre-existing relation associated with
         *  this edge will be deleted.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be an arc.
         *  @param newArcHead The new head for the edge, which is assumed to
         *   be an icon.
         */
        public void setHead(final Object edge, final Object newArcHead) {
            final Arc link = (Arc)edge;
            NamedObj linkHead = (NamedObj)link.getHead();
            NamedObj linkTail = (NamedObj)link.getTail();
            Relation linkRelation = (Relation)link.getRelation();
            // This moml is parsed to execute the change
            final StringBuffer moml = new StringBuffer();
            // This moml is parsed in case the change fails.
            final StringBuffer failmoml = new StringBuffer();
            moml.append("<group>\n");
            failmoml.append("<group>\n");

            // Make the request in the context of the container.
            final NamedObj container =
                (CompositeEntity)_getChangeRequestParent(getPtolemyModel());

            // If there is a previous connection, remove it.
            if (linkRelation != null) {
                if (newArcHead == null) {
                    // There will be no further connection, so just
                    // delete the relation.
                    moml.append(_deleteRelation(container, linkRelation));
                } else {
                    // There will be a further connection, so preserve
                    // the relation.
                    moml.append(_unlinkHead(container, linkHead,
                            linkRelation));
                }
            }

            String relationName = null;
            if (newArcHead != null) {
                // create moml to make the new link.
                relationName = _linkHead(container, moml, failmoml,
                        (NamedObj)newArcHead, linkTail, linkRelation);
            }
            moml.append("</group>\n");
            failmoml.append("</group>\n");
            final String relationNameToAdd = relationName;
            MoMLChangeRequest request =
                new MoMLChangeRequest(FSMGraphModel.this,
                        container,
                        moml.toString()) {
                        protected void _execute() throws Exception {
                            super._execute();
                            link.setHead(newArcHead);
                            if (relationNameToAdd != null) {
                                ComponentRelation relation = (ComponentRelation)
                                    ((CompositeEntity)getPtolemyModel())
                                    .getRelation(relationNameToAdd);
                                link.setRelation(relation);
                            }
                        }
                    };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new ChangeListener() {
                    public void changeFailed(ChangeRequest change,
                            Exception exception) {
                        // If we fail here, then we remove the link entirely.
                        _linkSet.remove(link);
                        link.setHead(null);
                        link.setTail(null);
                        link.setRelation(null);
                        // and queue a new change request to clean up the model
                        // Note: JDK1.2.2 requires that this variable not be
                        // called request or we get a compile error.
                        MoMLChangeRequest requestChange =
                            new MoMLChangeRequest(FSMGraphModel.this,
                                    container,
                                    failmoml.toString());
                        // Fail moml execution not undoable
                        container.requestChange(requestChange);
                    }

                    public void changeExecuted(ChangeRequest change) {
                        if (GraphUtilities.isPartiallyContainedEdge(edge,
                                getRoot(),
                                FSMGraphModel.this)) {
                            _linkSet.add(edge);
                        } else {
                            _linkSet.remove(edge);
                        }
                    }
                });
            request.setUndoable(true);
            container.requestChange(request);
        }

        /** Connect the given edge to the given tail node. If the specified
         *  tail is null, then any pre-existing relation associated with
         *  this edge will be deleted.
         *  This class queues a new change request with the ptolemy model
         *  to make this modification.
         *  @param edge The edge, which is assumed to be an arc.
         *  @param newArcTail The new tail for the edge, which is assumed to
         *  be an icon.
         */
        public void setTail(final Object edge, final Object newArcTail) {
            final Arc link = (Arc)edge;
            NamedObj linkHead = (NamedObj)link.getHead();
            NamedObj linkTail = (NamedObj)link.getTail();
            Relation linkRelation = (Relation)link.getRelation();
            // This moml is parsed to execute the change
            final StringBuffer moml = new StringBuffer();
            // This moml is parsed in case the change fails.
            final StringBuffer failmoml = new StringBuffer();
            moml.append("<group>\n");
            failmoml.append("<group>\n");

            // Make the request in the context of the container.
            final NamedObj container =
                (CompositeEntity)_getChangeRequestParent(getPtolemyModel());

            // If there is a previous connection, remove it.
            if (linkRelation != null) {
                if (newArcTail == null) {
                    // There will be no further connection, so just
                    // delete the relation.
                    moml.append(_deleteRelation(container, linkRelation));
                } else {
                    // There will be a further connection, so preserve
                    // the relation.
                    moml.append(_unlinkTail(container, linkTail,
                            linkRelation));
                }
            }

            String relationName = null;
            if (newArcTail != null) {
                // create moml to make the new links.
                relationName = _linkTail(container, moml, failmoml,
                        linkHead, (NamedObj)newArcTail, linkRelation);
            }
            moml.append("</group>\n");
            failmoml.append("</group>\n");

            final String relationNameToAdd = relationName;

            MoMLChangeRequest request =
                new MoMLChangeRequest(FSMGraphModel.this,
                        container,
                        moml.toString()) {
                        protected void _execute() throws Exception {
                            super._execute();
                            link.setTail(newArcTail);
                            if (relationNameToAdd != null) {
                                link.setRelation(
                                        ((CompositeEntity)getPtolemyModel())
                                        .getRelation(relationNameToAdd));
                            }
                        }
                    };

            // Handle what happens if the mutation fails.
            request.addChangeListener(new ChangeListener() {
                    public void changeFailed(ChangeRequest change,
                            Exception exception) {
                        // If we fail here, then we remove the link entirely.
                        _linkSet.remove(link);
                        link.setHead(null);
                        link.setTail(null);
                        link.setRelation(null);
                        // and queue a new change request to clean up the model
                        // Note: JDK1.2.2 requires that this variable not be
                        // called request or we get a compile error.
                        MoMLChangeRequest requestChange =
                            new MoMLChangeRequest(FSMGraphModel.this,
                                    container,
                                    failmoml.toString());
                        // fail moml execution not undaoble
                        container.requestChange(requestChange);
                    }

                    public void changeExecuted(ChangeRequest change) {
                        if (GraphUtilities.isPartiallyContainedEdge(edge,
                                getRoot(),
                                FSMGraphModel.this)) {
                            _linkSet.add(edge);
                        } else {
                            _linkSet.remove(edge);
                        }
                    }
                });
            request.setUndoable(true);
            container.requestChange(request);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Return moml to remove a relation in the specified container.
         */
        private String _deleteRelation(NamedObj container, Relation relation) {
            return "<deleteRelation name=\""
                + relation.getName(container)
                + "\"/>\n";
        }

        /** Append moml to the given buffer that connects a link with the
         *  given head.  If the relation argument is non-null, then that
         *  relation is used, and null is returned.  Otherwise, a new
         *  relation is created, and its name is returned. Names in the
         *  generated moml will be relative to the specified container.
         *  The failmoml argument is also populated, but with MoML code
         *  to execute if the link fails.  This disconnects any partially
         *  constructed link.
         */
        private String _linkHead(NamedObj container, StringBuffer moml,
                StringBuffer failmoml, NamedObj linkHead, NamedObj linkTail,
                Relation linkRelation) {
            if (linkHead != null && linkTail != null) {
                NamedObj head = (NamedObj)getSemanticObject(linkHead);
                NamedObj tail = (NamedObj)getSemanticObject(linkTail);
                if (head instanceof State && tail instanceof State) {
                    // When we connect two states, we actually connect the
                    // appropriate ports.
                    State headState = (State)head;
                    State tailState = (State)tail;
                    ComponentPort headPort =
                        (ComponentPort)headState.incomingPort;
                    ComponentPort tailPort =
                        (ComponentPort)tailState.outgoingPort;
                    NamedObj ptolemyModel = getPtolemyModel();
                    // If the context is not the entity that we're editing,
                    // then we need to set the context correctly.
                    if (ptolemyModel != container) {
                        String contextString = "<entity name=\"" +
                            ptolemyModel.getName(container) +
                            "\">\n";
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
                        // Set the exitAngle based on other relations.
                        // Count the number of connections between the
                        // headPort and the tailPort.
                        Iterator ports = tailPort.deepConnectedPortList()
                            .iterator();
                        int count = 0;
                        while (ports.hasNext()) {
                            if (ports.next() == headPort) count++;
                        }
                        // Increase the angle as the count increases.
                        // Any function of "count" will work here that starts
                        // at PI/5 and approaches something less than PI
                        // as count gets large.  Unfortunately, self-loops
                        // again have to be handled specially.
                        double angle;
                        if (headPort.getContainer()
                                != tailPort.getContainer()) {
                            angle = Math.PI/5.0 + 1.5* Math.atan(0.3*count);
                        } else {
                            angle = Math.PI/3.0 - 0.75 * Math.atan(0.3*count);
                        }

                        // Create the new relation.
                        // Note that we specify no class so that we use the
                        // container's factory method when this gets parsed
                        moml.append("<relation name=\""
                                + relationName
                                + "\"><property name=\"exitAngle\" value=\""
                                + angle
                                + "\"/></relation>\n");
                        moml.append("<link port=\""
                                + tailPort.getName(ptolemyModel)
                                + "\" relation=\""
                                + relationName
                                + "\"/>\n");
                    }
                    moml.append("<link port=\""
                            + headPort.getName(ptolemyModel)
                            + "\" relation=\""
                            + relationName
                            + "\"/>\n");
                    // Record moml so that we can blow away these
                    // links in case we can't create them
                    failmoml.append("<unlink port=\"" +
                            headPort.getName(ptolemyModel) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
                    if (linkRelation == null) {
                        failmoml.append("<unlink port=\""
                                + tailPort.getName(ptolemyModel)
                                + "\" relation=\""
                                + relationName
                                + "\"/>\n");
                        failmoml.append("<deleteRelation name=\""
                                + relationName
                                + "\"/>\n");
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
                            "Attempt to create a link between non-states: " +
                            "Head = " + head + ", Tail = " + tail);
                }
            } else {
                // No Linking to do.
                return null;
            }
        }

        /** Append moml to the given buffer that connects a link with the
         *  given tail.  If the relation argument is non-null, then that
         *  relation is used, and null is returned.  Otherwise, a new
         *  relation is created, and its name is returned. Names in the
         *  generated moml will be relative to the specified container.
         *  The failmoml argument is also populated, but with MoML code
         *  to execute if the link fails.  This disconnects any partially
         *  constructed link.
         */
        private String _linkTail(NamedObj container, StringBuffer moml,
                StringBuffer failmoml, NamedObj linkHead, NamedObj linkTail,
                Relation linkRelation) {
            // NOTE: This method is almost identical to the previous
            // one, but just enough different that it isn't obvious
            // how to combine them into one method.
            if (linkHead != null && linkTail != null) {
                NamedObj head = (NamedObj)getSemanticObject(linkHead);
                NamedObj tail = (NamedObj)getSemanticObject(linkTail);
                if (head instanceof State && tail instanceof State) {
                    // When we connect two states, we actually connect the
                    // appropriate ports.
                    State headState = (State)head;
                    State tailState = (State)tail;
                    ComponentPort headPort =
                        (ComponentPort)headState.incomingPort;
                    ComponentPort tailPort =
                        (ComponentPort)tailState.outgoingPort;
                    NamedObj ptolemyModel = getPtolemyModel();
                    // If the context is not the entity that we're editing,
                    // then we need to set the context correctly.
                    if (ptolemyModel != container) {
                        String contextString = "<entity name=\""
                            + ptolemyModel.getName(container)
                            + "\">\n";
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
                        // Note that we specify no class so that we use the
                        // container's factory method when this gets parsed
                        moml.append("<relation name=\""
                                + relationName
                                + "\"/>\n");
                        moml.append("<link port=\""
                                + headPort.getName(ptolemyModel)
                                + "\" relation=\""
                                + relationName
                                + "\"/>\n");
                    }
                    moml.append("<link port=\""
                            + tailPort.getName(ptolemyModel)
                            + "\" relation=\""
                            + relationName
                            + "\"/>\n");
                    // Record moml so that we can blow away these
                    // links in case we can't create them
                    failmoml.append("<unlink port=\"" +
                            tailPort.getName(ptolemyModel) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
                    if (linkRelation == null) {
                        failmoml.append("<unlink port=\""
                                + headPort.getName(ptolemyModel)
                                + "\" relation=\""
                                + relationName
                                + "\"/>\n");
                        failmoml.append("<deleteRelation name=\""
                                + relationName
                                + "\"/>\n");
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
                            "Attempt to create a link between non-states: " +
                            "Head = " + head + ", Tail = " + tail);
                }
            } else {
                // No Linking to do.
                return null;
            }
        }

        /** Return moml to unlink a relation with the given head in the
         *  specified container.
         */
        private String _unlinkHead(NamedObj container, NamedObj linkHead,
                Relation relation) {
            NamedObj head = (NamedObj)getSemanticObject(linkHead);
            State headState = (State)head;
            ComponentPort headPort = (ComponentPort)headState.incomingPort;
            return "<unlink port=\""
                + headPort.getName(container)
                + "\" relation=\""
                + relation.getName(container)
                + "\"/>\n";
        }

        /** Return moml to unlink a relation with the given tail in the
         *  specified container.
         */
        private String _unlinkTail(NamedObj container, NamedObj linkTail,
                Relation relation) {
            NamedObj tail = (NamedObj)getSemanticObject(linkTail);
            State tailState = (State)tail;
            ComponentPort tailPort = (ComponentPort)tailState.outgoingPort;
            return "<unlink port=\""
                + tailPort.getName(container)
                + "\" relation=\""
                + relation.getName(container)
                + "\"/>\n";
        }
    }

    /** The model for external ports.
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
         * @param node The node, which is assumed to be an icon contained in
         * this graph model.
         * @return The container of the icon's container, which should
         * be the root of this graph model.
         */
        public Object getParent(Object node) {
            return ((Locatable)node).getContainer().getContainer();
        }

        /**
         * Return an iterator over the edges coming into the given node.
         * This method first ensures that there is an arc
         * object for every link.
         * The iterator is constructed by
         * removing any arcs that do not have the given node as head.
         * @param node The node, which is assumed to be an icon contained in
         * this graph model.
         * @return An iterator of Arc objects, all of which have
         * the given node as their head.
         */
        public Iterator inEdges(Object node) {
            return new NullIterator();
        }

        /**
         * Return an iterator over the edges coming into the given node.
         * This method first ensures that there is an arc
         * object for every link.
         * The iterator is constructed by
         * removing any arcs that do not have the given node as tail.
         * @param node The node, which is assumed to be an icon contained in
         * this graph model.
         * @return An iterator of Arc objects, all of which have
         * the given node as their tail.
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
            if (deleteObj instanceof ComponentPort) {
                // Object is an entity.
                elementName = "deletePort";
            } else {
                throw new InternalErrorException(
                        "Attempt to remove a node that is not an Entity. " +
                        "node = " + node);
            }

            String moml = "<" + elementName + " name=\""
                + ((NamedObj)deleteObj).getName() + "\"/>\n";

            // Make the request in the context of the container.
            NamedObj container = (NamedObj)_getChangeRequestParent(deleteObj);
            MoMLChangeRequest request =
                new MoMLChangeRequest(
                        FSMGraphModel.this, container, moml);
            request.setUndoable(true);
            request.addChangeListener(new ChangeListener() {
                    public void changeFailed(ChangeRequest change,
                            Exception exception) {
                        // If we fail, then issue structureChanged.
                        dispatchGraphEvent(new GraphEvent(eventSource,
                                GraphEvent.STRUCTURE_CHANGED,
                                getRoot()));
                    }

                    public void changeExecuted(ChangeRequest change) {
                        // If we succeed, then issue structureChanged, since
                        // this is likely connected to something.
                        dispatchGraphEvent(new GraphEvent(eventSource,
                                GraphEvent.STRUCTURE_CHANGED,
                                getRoot()));
                    }
                });
            request.setUndoable(true);
            container.requestChange(request);
        }
    }


    /** The model for an icon that represent states.
     */
    public class StateModel extends NamedObjNodeModel {
        /** Return a MoML String that will delete the given node from the
         *  Ptolemy model.
         *  @return A valid MoML string.
         */
        public String getDeleteNodeMoML(Object node) {
            NamedObj deleteObj = (NamedObj)((Locatable)node).getContainer();

            NamedObj container = _getChangeRequestParent(getPtolemyModel());

            StringBuffer moml = new StringBuffer("<group>\n");

            moml.append("<deleteEntity name=\""
                    + deleteObj.getName(container) + "\"/>\n");

            CompositeEntity master = (CompositeEntity)deleteObj.getContainer();
            // Nothing to do if there is no container.
            if (master != null) {
                // Remove any referenced refinements that are not also
                // referenced by other states.
                TypedActor[] refinements = null;
                try {
                    refinements = ((State)deleteObj).getRefinement();
                } catch (IllegalActionException ex) {
                    // Ignore, no refinement to remove.
                }
                if (refinements != null) {
                    for (int i = 0; i < refinements.length; i++) {
                        TypedActor refinement = refinements[i];
                        // By default, if no other state or transition refers
                        // to this refinement, then we will remove it.
                        boolean removeIt = true;
                        Iterator states
                            = master.entityList(State.class).iterator();
                        while (removeIt && states.hasNext()) {
                            State state = (State)states.next();
                            if (state == deleteObj) continue;
                            TypedActor[] stateRefinements = null;
                            try {
                                stateRefinements = state.getRefinement();
                            } catch (IllegalActionException ex) {
                                // Ignore, no refinement to check
                            }
                            if (stateRefinements == null) continue;
                            for (int j = 0; j < stateRefinements.length; j++) {
                                if (stateRefinements[j] == refinement) {
                                    removeIt = false;
                                    break;
                                }
                            }
                        }
                        // Next check transitions.
                        Iterator transitions = master.relationList().iterator();
                        while (removeIt && transitions.hasNext()) {
                            Relation transition = (Relation)transitions.next();
                            if (!(transition instanceof Transition)) continue;
                            TypedActor[] transitionRefinements = null;
                            try {
                                transitionRefinements
                                        = ((Transition)transition).getRefinement();
                            } catch (IllegalActionException e) {
                                // Ignore, no refinement to check.
                            }
                            if (transitionRefinements == null) continue;
                            for (int j = 0;
                                 j < transitionRefinements.length;
                                 j++) {
                                if (transitionRefinements[j] == refinement) {
                                    removeIt = false;
                                    break;
                                }
                            }
                        }
                        if (removeIt) {
                            moml.append("<deleteEntity name=\""
                                    + ((NamedObj)refinement).getName(container)
                                    + "\"/>\n");
                        }
                    }
                }
            }

            moml.append("</group>\n");
            return moml.toString();
        }

        /** Return the graph parent of the given node.
         *  @param node The node, which is assumed to be a location.
         *  @return The container of the icon's container, which should
         *   be the root of this graph model.
         */
        public Object getParent(Object node) {
            return ((Locatable)node).getContainer().getContainer();
        }

        /** Return an iterator over the edges coming into the given node.
         *  This method first ensures that there is an arc
         *  object for every link. The iterator is constructed by
         *  removing any arcs that do not have the given node as head.
         *  @param node The node, which is assumed to be a location.
         *  @return An iterator of Arc objects, all of which have
         *   the given node as their head.
         */
        public Iterator inEdges(Object node) {
            Locatable icon = (Locatable)node;
            Entity entity = (Entity)icon.getContainer();

            // Go through all the links, creating a list of
            // those we are connected to.
            List stateLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Arc link = (Arc)links.next();
                NamedObj head = (NamedObj)link.getHead();

                if (head != null && head.equals(icon)) {
                    stateLinkList.add(link);
                }
            }
            return stateLinkList.iterator();
        }

        /** Return an iterator over the edges coming into the given node.
         *  This method first ensures that there is an arc
         *  object for every link. The iterator is constructed by
         *  removing any arcs that do not have the given node as tail.
         *  @param node The node, which is assumed to be a location.
         *  @return An iterator of Arc objects, all of which have
         *   the given node as their tail.
         */
        public Iterator outEdges(Object node) {
            Locatable icon = (Locatable)node;
            Entity entity = (Entity)icon.getContainer();

            // Go through all the links, creating a list of
            // those we are connected to.
            List stateLinkList = new LinkedList();
            Iterator links = _linkSet.iterator();
            while (links.hasNext()) {
                Arc link = (Arc)links.next();
                Object tail = link.getTail();
                if (tail != null && tail.equals(icon)) {
                    stateLinkList.add(link);
                }
            }

            return stateLinkList.iterator();
        }

        /** Remove the given node from the model.  The node is assumed
         *  to be a Locatable belonging to an entity.
         */
        public void removeNode(final Object eventSource, Object node) {
            NamedObj deleteObj = (NamedObj)((Locatable)node).getContainer();

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
            if (deleteObj instanceof ComponentEntity) {
                // Object is an entity.
                elementName = "deleteEntity";
            } else {
                throw new InternalErrorException(
                        "Attempt to remove a node that is not an Entity. " +
                        "node = " + node);
            }

            String moml = "<" + elementName + " name=\""
                + ((NamedObj)deleteObj).getName() + "\"/>\n";

            // Make the request in the context of the container.
            NamedObj container = (NamedObj)_getChangeRequestParent(deleteObj);
            MoMLChangeRequest request =
                new MoMLChangeRequest(
                        FSMGraphModel.this, container, moml);
            request.addChangeListener(new ChangeListener() {
                    public void changeFailed(ChangeRequest change,
                            Exception exception) {
                        // If we fail, then issue structureChanged.
                        dispatchGraphEvent(new GraphEvent(eventSource,
                                GraphEvent.STRUCTURE_CHANGED,
                                getRoot()));
                    }

                    public void changeExecuted(ChangeRequest change) {
                        // If we succeed, then issue structureChanged, since
                        // this is likely connected to something.
                        dispatchGraphEvent(new GraphEvent(eventSource,
                                GraphEvent.STRUCTURE_CHANGED,
                                getRoot()));
                    }
                });
            request.setUndoable(true);
            container.requestChange(request);
        }
    }
}
