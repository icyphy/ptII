/* A graph model for ptolemy fsm models.

 Copyright (c) 1999-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.kernel.util.*;
import ptolemy.vergil.toolbox.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.moml.*;
import ptolemy.vergil.ptolemy.AbstractPtolemyGraphModel;
import diva.graph.AbstractGraphModel;
import diva.graph.GraphEvent;
import diva.graph.GraphException;
import diva.graph.GraphUtilities;
import diva.graph.toolbox.*;
import diva.graph.modular.ModularGraphModel;
import diva.graph.modular.CompositeModel;
import diva.graph.modular.NodeModel;
import diva.graph.modular.EdgeModel;
import diva.graph.modular.MutableEdgeModel;
import diva.graph.modular.CompositeNodeModel;
import diva.util.*;
import java.util.*;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphModel
/**
a graph model for graphically manipulating ptolemy FSM models.

@author Steve Neuendorffer
@version $Id$
*/
public class FSMGraphModel extends AbstractPtolemyGraphModel {
    // FIXME: support visible attributes.
    /** Construct a new graph model whose root is the given composite entity.
     *  @param toplevel The top-level composite entity for the model.
     */
    public FSMGraphModel(CompositeEntity toplevel) {
	super(toplevel);
	_linkSet = new HashSet();
        _update();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Disconnect an edge from its two enpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event whose source is the given source.
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void disconnectEdge(Object eventSource, Object edge) {
	if(!(getEdgeModel(edge) instanceof MutableEdgeModel)) return;
	MutableEdgeModel model = (MutableEdgeModel)getEdgeModel(edge);
	Object head = model.getHead(edge);
	Object tail = model.getTail(edge);
        model.setTail(edge, null);
        model.setHead(edge, null);
        if(head != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_HEAD_CHANGED,
                    edge, head);
            dispatchGraphEvent(e);
        }
        if(tail != null) {
            GraphEvent e = new GraphEvent(eventSource,
                    GraphEvent.EDGE_TAIL_CHANGED,
                    edge, tail);
            dispatchGraphEvent(e);
        }
    }

    /**
     * Return the model for the given composite object.  If the object is not
     * a composite, meaning that it does not contain other nodes,
     * then return null.
     * @param composite An object which is assumed to be a node object in
     * this graph model.
     * @return An instance of ToplevelModel if the object is the root
     * object of this graph model.  Otherwise return null.
     */
    public CompositeModel getCompositeModel(Object composite) {
	if(composite.equals(getRoot())) {
	    return _toplevelModel;
	} else {
	    return null;
	}
    }

    /**
     * Return the model for the given edge object.  If the object is not
     * an edge, then return null.
     * @param edge An object which is assumed to be in this graph model.
     * @return An instance of ArcModel if the object is an Arc.
     * Otherwise return null.
     */
    public EdgeModel getEdgeModel(Object edge) {
	if(edge instanceof Arc) {
	    return _arcModel;
	} else {
	    return null;
	}
    }

    public PortModel getPortModel() {
	return _portModel;
    }

    public StateModel getStateModel() {
	return _stateModel;
    }

    public ArcModel getArcModel() {
	return _arcModel;
    }

    /**
     * Return the node model for the given object.  If the object is not
     * a node, then return null.
     * @param node An object which is assumed to be in this graph model.
     * @return An instance of StateModel if the object is an icon.
     * Otherwise return null.
     */
    public NodeModel getNodeModel(Object node) {
	if(node instanceof Location) {
            Location location = (Location)node;
            if(location.getContainer() instanceof ComponentEntity)
                return _stateModel;
            else if(location.getContainer() instanceof ComponentPort)
                return _portModel;
            else
                return null;
	} else {
	    return null;
	}
    }

    /** Return the semantic object correspoding to the given node, edge,
     *  or composite.  A "semantic object" is an object associated with
     *  a node in the graph.  In this case, if the node is icon, the
     *  semantic object is the entity containing the icon.  If it is
     *  an arc, then the semantic object is the arc's relation.
     *  @param element A graph element.
     *  @return The semantic object associated with this element, or null
     *  if the object is not recognized.
     */
    public Object getSemanticObject(Object element) {
	if(element instanceof Location) {
	    return ((Location)element).getContainer();
	} else if(element instanceof Arc) {
	    return ((Arc)element).getRelation();
	} else {
	    return null;
	}
    }

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void removeNode(Object eventSource, Object node) {
	if(!(getNodeModel(node) instanceof RemoveableNodeModel)) return;
	RemoveableNodeModel model = (RemoveableNodeModel)getNodeModel(node);
        model.removeNode(eventSource, node);
    }

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
	    if (node instanceof Location) {
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
	    if (node instanceof Location) {
		return true;
	    } else
		return false;
	}

	/** Return the head node of the given edge.
	 *  @param edge The edge, which is assumed to be an arc.
	 *  @return The node that is the head of the specified edge.
	 */
	public Object getHead(Object edge) {
	    return ((Arc)edge).getHead();
	}

	/** Return the tail node of the specified edge.
	 *  @param edge The edge, which is assumed to be an arc.
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


	/** Append moml to the given buffer that disconnects a link with the
	 *  given head, tail, and relation.  Names in the returned moml will be
         *  relative to the iven container.
	 */
	private void _unlinkMoML(
                NamedObj container,
                StringBuffer moml,
                NamedObj linkHead,
                NamedObj linkTail,
                Relation relation) throws Exception {
	    // If the link is already connected, then create a bit of MoML
	    // to unlink the link.
 	    if(linkHead != null && linkTail != null) {
		NamedObj head = (NamedObj)getSemanticObject(linkHead);
		NamedObj tail = (NamedObj)getSemanticObject(linkTail);
	        if(head instanceof State &&
                        tail instanceof State) {
		    // When we connect two states, we actually connect the
		    // appropriate ports.
		    State headState = (State)head;
		    State tailState = (State)tail;
		    ComponentPort headPort =
			(ComponentPort)headState.incomingPort;
		    ComponentPort tailPort =
			(ComponentPort)tailState.outgoingPort;
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
		} else {
		    throw new RuntimeException(
                            "Unlink failed: " +
                            "Head = " + head + ", Tail = " + tail);
		}
	    } else {
		// No unlinking to do.
	    }
	}

	/** Append moml to the given buffer that connects a link with the
	 *  given head, tail, and relation.  Names in the returned moml will be
         *  relative to the iven container.  This may require addinging an
	 *  anonymous relation to the ptolemy model.  If this is required,
	 *  the name of the relation <b>relative to the toplevel object
         *  of this graph model</b> is returned.
	 *  If no relation need be added, then
	 *  null is returned.
	 */
	private String _linkMoML(
                NamedObj container,
                StringBuffer moml,
                StringBuffer failmoml,
                NamedObj linkHead,
                NamedObj linkTail) throws Exception {
	    if(linkHead != null && linkTail != null) {
		NamedObj head = (NamedObj)getSemanticObject(linkHead);
		NamedObj tail = (NamedObj)getSemanticObject(linkTail);
		if(head instanceof State &&
                        tail instanceof State) {
		    // When we connect two states, we actually connect the
		    // appropriate ports.
		    State headState = (State)head;
		    State tailState = (State)tail;
		    ComponentPort headPort =
			(ComponentPort)headState.incomingPort;
		    ComponentPort tailPort =
			(ComponentPort)tailState.outgoingPort;
		    // Linking two ports with a new relation.
		    String relationName =
			getToplevel().uniqueName("relation");
                    // If the context is not the entity that we're editing,
                    // then we need to set the context correctly.
                    if(getToplevel() != container) {
                        String contextString = "<entity name=\"" +
                            getToplevel().getName(container) +
                            "\">\n";
                        moml.append(contextString);
                        failmoml.append(contextString);
                    }
		    // Note that we use no class so that we use the container's
		    // factory method when this gets parsed
		    moml.append("<relation name=\"" + relationName + "\"/>\n");
		    moml.append("<link port=\"" +
                            headPort.getName(getToplevel()) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
		    moml.append("<link port=\"" +
                            tailPort.getName(getToplevel()) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
		    // Record moml so that we can blow away these
		    // links in case we can't create them
		    failmoml.append("<unlink port=\"" +
                            headPort.getName(getToplevel()) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
		    failmoml.append("<unlink port=\"" +
                            tailPort.getName(getToplevel()) +
                            "\" relation=\"" + relationName +
                            "\"/>\n");
		    failmoml.append("<deleteRelation name=\"" +
                            relationName +
                            "\"/>\n");
                    // close the context
                    if(getToplevel() != container) {
                        moml.append("</entity>");
                        failmoml.append("</entity>");
                    }
		    return relationName;
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
	 *  @param edge The edge, which is assumed to be an arc.
	 *  @param head The new head for the edge, which is assumed to
	 *  be an icon.
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
            final CompositeEntity container =
                (CompositeEntity)_getChangeRequestParent(getToplevel());

	    String relationName = "";

	    try {
		// create moml to unlink any existing.
		_unlinkMoML(container, moml, linkHead, linkTail, linkRelation);

		// create moml to make the new links.
		relationName =
		    _linkMoML(container, moml, failmoml,
                            (NamedObj)newArcHead, linkTail);
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }

	    moml.append("</group>\n");
	    failmoml.append("</group>\n");

	    final String relationNameToAdd = relationName;

	    ChangeRequest request =
		new MoMLChangeRequest(FSMGraphModel.this,
                        container,
                        moml.toString()) {
                protected void _execute() throws Exception {
                    super._execute();
                    link.setHead(newArcHead);
                    if(relationNameToAdd != null) {
                        ComponentRelation relation =
                        (ComponentRelation)getToplevel().getRelation(
                                relationNameToAdd);
                        link.setRelation(relation);
                    } else {
                        link.setRelation(null);
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
		    ChangeRequest requestChange =
			new MoMLChangeRequest(FSMGraphModel.this,
                                container,
                                failmoml.toString());
		    container.requestChange(requestChange);
		}

		public void changeExecuted(ChangeRequest change) {
		    if(GraphUtilities.isPartiallyContainedEdge(edge,
                            getRoot(),
                            FSMGraphModel.this)) {
			_linkSet.add(edge);
		    } else {
			_linkSet.remove(edge);
		    }
		}
	    });

	    container.requestChange(request);

	}

	/** Connect the given edge to the given tail node.
	 *  This class queues a new change request with the ptolemy model
	 *  to make this modification.
	 *  @param edge The edge, which is assumed to be an arc.
	 *  @param head The new head for the edge, which is assumed to
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
            final CompositeEntity container =
                (CompositeEntity)_getChangeRequestParent(getToplevel());

	    String relationName = "";

	    try {
		// create moml to unlink any existing.
		_unlinkMoML(container, moml, linkHead, linkTail, linkRelation);

		// create moml to make the new links.
		relationName =
		    _linkMoML(container, moml, failmoml,
                            linkHead, (NamedObj)newArcTail);
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }

	    moml.append("</group>\n");
	    failmoml.append("</group>\n");

	    final String relationNameToAdd = relationName;

	    ChangeRequest request =
		new MoMLChangeRequest(FSMGraphModel.this,
                        container,
                        moml.toString()) {
                protected void _execute() throws Exception {
                    super._execute();
                    link.setTail(newArcTail);
                    if(relationNameToAdd != null) {
                        link.setRelation(
                                getToplevel().getRelation(relationNameToAdd));
                    } else {
                        link.setRelation(null);
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
		    ChangeRequest requestChange =
			new MoMLChangeRequest(FSMGraphModel.this,
                                container,
                                failmoml.toString());
		    container.requestChange(requestChange);
		}

		public void changeExecuted(ChangeRequest change) {
		    if(GraphUtilities.isPartiallyContainedEdge(edge,
                            getRoot(),
                            FSMGraphModel.this)) {
			_linkSet.add(edge);
		    } else {
			_linkSet.remove(edge);
		    }
		}
	    });

	    container.requestChange(request);
	}
    }

    /** The model for external ports.
     */
    public class PortModel implements RemoveableNodeModel {
	/**
	 * Return the graph parent of the given node.
	 * @param node The node, which is assumed to be an icon contained in
	 * this graph model.
	 * @return The container of the icon's container, which should
	 * be the root of this graph model.
	 */
	public Object getParent(Object node) {
	    return ((Location)node).getContainer().getContainer();
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
            NamedObj deleteObj = (NamedObj)((Location)node).getContainer();
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
            ChangeRequest request =
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
            container.requestChange(request);
	}
    }


    /** The model for an icon that represent states.
     */
    public class StateModel implements RemoveableNodeModel {
	/**
	 * Return the graph parent of the given node.
	 * @param node The node, which is assumed to be an icon contained in
	 * this graph model.
	 * @return The container of the icon's container, which should
	 * be the root of this graph model.
	 */
	public Object getParent(Object node) {
	    return ((Location)node).getContainer().getContainer();
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
	    Location icon = (Location)node;
	    Entity entity = (Entity)icon.getContainer();

	    // Go through all the links, creating a list of
	    // those we are connected to.
	    List stateLinkList = new LinkedList();
	    Iterator links = _linkSet.iterator();
	    while(links.hasNext()) {
		Arc link = (Arc)links.next();
		NamedObj head = (NamedObj)link.getHead();

		if(head != null && head.equals(icon)) {
		    stateLinkList.add(link);
		}
	    }

	    return stateLinkList.iterator();
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
	    Location icon = (Location)node;
	    Entity entity = (Entity)icon.getContainer();

	    // Go through all the links, creating a list of
	    // those we are connected to.
	    List stateLinkList = new LinkedList();
	    Iterator links = _linkSet.iterator();
	    while(links.hasNext()) {
		Arc link = (Arc)links.next();
		Object tail = link.getTail();
		if(tail != null && tail.equals(icon)) {
		    stateLinkList.add(link);
		}
	    }

	    return stateLinkList.iterator();
	}

	/** Remove the given node from the model.  The node is assumed
	 *  to be an icon.
	 */
	public void removeNode(final Object eventSource, Object node) {
            NamedObj deleteObj = (NamedObj)((Location)node).getContainer();
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
            ChangeRequest request =
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
            container.requestChange(request);
	}
    }

    /** A model for the toplevel composite of this graph model.
     */
    public class ToplevelModel implements CompositeModel {
	/**
	 * Return the number of nodes contained in
	 * this graph or composite node.
	 * @param composite The composite, which is assumed to be
	 * the root composite entity.
	 * @return The number of entities contained in the composite.
	 */
	public int getNodeCount(Object composite) {
	    CompositeEntity entity = (CompositeEntity)composite;
	    int count = entity.entityList().size();
            count += entity.portList().size();
	    return count;
	}

	/**
	 * Return an iterator over all the nodes contained in
	 * the given composite.  This method ensures that all the entities
	 * have an icon.
	 * @param composite The composite, which is assumed to be
	 * the root composite entity.
	 * @return An iterator containing icons.
	 */
	public Iterator nodes(Object composite) {
	    // FIXME visible attributes?
	    Set nodes = new HashSet();
	    CompositeEntity toplevel = (CompositeEntity)composite;
	    Iterator entities = toplevel.entityList().iterator();
	    while(entities.hasNext()) {
		ComponentEntity entity = (ComponentEntity)entities.next();
		nodes.add(_getLocation(entity));
	    }
            Iterator ports = toplevel.portList().iterator();
            while(ports.hasNext()) {
                ComponentPort port = (ComponentPort)ports.next();
                nodes.add(_getLocation(port));
            }

	    return nodes.iterator();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Update the graph model.  This is called whenever a change request is
     *  executed.  In this class the internal set of link objects is
     *  verified to be correct.
     */
    protected boolean _update() {
     	// Go through all the links that currently exist, and remove
        // any that don't have both ends in the model.
	Iterator links = _linkSet.iterator();
	while(links.hasNext()) {
 	    Arc link = (Arc)links.next();
            Relation relation = link.getRelation();
            if(relation == null) continue;
            boolean headOK = GraphUtilities.isContainedNode(link.getHead(),
                    getRoot(), this);
            boolean tailOK = GraphUtilities.isContainedNode(link.getTail(),
                    getRoot(), this);
            // If the head or tail has been removed, then remove this link.
            if(!(headOK && tailOK)) {
                Object headObj = getSemanticObject(link.getHead());
                Object tailObj = getSemanticObject(link.getTail());
                link.setHead(null);
                link.setTail(null);
                links.remove();
                NamedObj container =
                    (NamedObj)_getChangeRequestParent(relation);
                // remove the relation  This should trigger removing the
                // other link. This will only happen when we've deleted
                // the state at one end of the model.
                // Note that the source is NOT the graphmodel, so this
                // will trigger the changerequest listener to
                // redraw the graph again.
                ChangeRequest request = new MoMLChangeRequest(
                        container, container,
                        "<deleteRelation name=\""
                        + relation.getName(container)
                        + "\"/>\n");
                container.requestChange(request);
                return false;
            }
        }

        // Now create Links for links that may be new
        Iterator relations = getToplevel().relationList().iterator();
        while(relations.hasNext()) {
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
	while(links.hasNext()) {
	    Arc link = (Arc)links.next();
	    // only consider links that are associated with this relation.
	    if(link.getRelation() == relation) {
		foundLink = link;
		break;
	    }
	}

	// A link exists, so there is nothing to do.
	if(foundLink != null) return;

	List linkedPortList = relation.linkedPortList();
	if(linkedPortList.size() != 2) {
            // Do nothing...  somebody else should take care of removing this,
            // because we have no way of representing it in this editor.
            return;
	}
	Port port1 = (Port)linkedPortList.get(0);
	Location location1 = _getLocation((NamedObj)port1.getContainer());
	Port port2 = (Port)linkedPortList.get(1);
	Location location2 = _getLocation((NamedObj)port2.getContainer());

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
	if(((State)port1.getContainer()).incomingPort.equals(port1)) {
	    link.setHead(location1);
	    link.setTail(location2);
	} else {
	    link.setHead(location2);
	    link.setTail(location1);
	}
        _linkSet.add(link);
    }

    // Return the location contained in the given object, or
    // a new location contained in the given object if there was no location.
    private Location _getLocation(NamedObj object) {
	List locations = object.attributeList(Location.class);
	if(locations.size() > 0) {
	    return (Location)locations.get(0);
	} else {
	    try {
		Location location = new Location(object, "_location");
		return location;
	    }
	    catch (Exception e) {
		throw new InternalErrorException("Failed to create " +
                        "location, even though one does not exist:" +
                        e.getMessage());
	    }
	}
    }

    private interface RemoveableNodeModel extends NodeModel {
	/** Remove the given edge from the model
	 */
	public void removeNode(Object eventSource, Object node);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of all links in the model.
    private Set _linkSet;

    // The models of the different types of nodes and edges.
    private ArcModel _arcModel = new ArcModel();
    private ToplevelModel _toplevelModel = new ToplevelModel();
    private PortModel _portModel = new PortModel();
    private StateModel _stateModel = new StateModel();
}

