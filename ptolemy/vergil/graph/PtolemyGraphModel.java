/* A graph model for basic ptolemy models.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.graph;

// FIXME: Trim this list and replace with explict (per class) imports.
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.event.*;
import ptolemy.moml.*;
import ptolemy.vergil.ExceptionHandler;
import ptolemy.vergil.toolbox.EditorIcon;

import diva.graph.AbstractGraphModel;
import diva.graph.GraphEvent;
import diva.graph.GraphException;
import diva.graph.MutableGraphModel;
import diva.graph.toolbox.*;
import diva.util.*;

import java.util.*;
import javax.swing.SwingUtilities;

// FIXME: This class throws InternalErrorException on type-valid
// uses of public methods.  This is probably not the right exception
// to throw.  Also, the error messages are quite poor.

// FIXME: Many of these methods have multiple versions, but it seems
// unnecessary.  Avoid the instanceof tests, just do a cast, and document
// that a ClassCastException will occur if the classes aren't right.

//////////////////////////////////////////////////////////////////////////
//// PtolemyGraphModel
/**
This class represents one level of hierarchy of a Ptolemy II model.
The graph model represents ports, entities and relations as nodes.  Entities
are represented in the model by the icon that is used to visually
depict them.  Relations are represented in the model by its vertices
(which are visual elements that generally exist in multiple
places in a visual rendition).  Ports represent themselves in the model.
In the terminology of diva, the graph elements are "nodes" (icons,
vertices, and ports), and the "edges" that link them.  Edges
are represented in the model by instances of the Link class.
<p>
Edges may link a port and a vertex, or a port and
another port.  For visual simplicity, both types of edges are represented by
an instance of the Link class.  If an edge is placed between a port 
and a vertex then the Link represents a Ptolemy II link between
the port and the vertex's Relation.  However, if an edge is placed between
two ports, then it represents a Relation (with no vertex) and links from
the relation to each port (in Ptolemy II, this is called a "connection").

@author Steve Neuendorffer
@version $Id$
 */
public class PtolemyGraphModel extends AbstractGraphModel 
    implements MutableGraphModel {
    
    /** Construct an empty graph model whose root is a new CompositeEntity.
     */
    public PtolemyGraphModel() {
        this(new CompositeEntity());
    }

    /** Construct a new graph model whose root is the given composite entity.
     *  @param toplevel The top-level composite entity for the model.
     */
    public PtolemyGraphModel(CompositeEntity toplevel) {
	_root = toplevel;
	_root.addChangeListener(new GraphChangeListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the head of the given edge can be attached to the
     *  given node.
     *  @param edge The edge to attach.
     *  @param node The node to attach to.
     */
    public boolean acceptHead(Object edge, Object node) {
	if (node instanceof Port ||
	    node instanceof Vertex) {
	    return true;
	} else
	    return false;
    }

    /** Return true if the tail of the given edge can be attached to the
     *  given node.
     *  @param edge The edge to attach.
     *  @param node The node to attach to.
     */
    public boolean acceptTail(Object edge, Object node) {
	if (node instanceof Port ||
	    node instanceof Vertex) {
	    return true;
	} else
	    return false;
    }

    /** Notify any graph listeners with a NODE_ADDED event that the specified
     *  node has been added.
     *  @param eventSource The source of the event.
     *  @param node The node that has been added.
     *  @param parent The parent to which it has been added.
     */
    public void addNode(Object eventSource, Object node, Object parent) {
        GraphEvent e = new GraphEvent(
                eventSource, GraphEvent.NODE_ADDED, node, parent);
        dispatchGraphEvent(e);
    }

    /** Connect the specified edge to the given tail and head nodes,
     *  then dispatch events to the listeners.  One of the tail or head
     *  must be an instance of ComponentPort and the other must be an instance
     *  of Vertex or an InternalErrorException will be thrown.
     *  @param eventSource The source of the request.
     *  @param link The link to change, which must be an instance of Link
     *   or an InternalErrorException will be thrown.
     *  @param tail The new tail.
     *  @param head The new head.
     */
    public void connectEdge(
            Object eventSource, Object link, Object tail, Object head) {
	if(link instanceof Link) {
	    if(tail instanceof ComponentPort &&
	       head instanceof Vertex) {
		connectEdge(eventSource, (Link)link, 
			    (ComponentPort)tail,
			    (Vertex)head);
	    } else if(head instanceof ComponentPort &&
		      tail instanceof Vertex) {
		connectEdge(eventSource, (Link)link, 
			    (Vertex)tail,
			    (ComponentPort)head);
	    } else {
                throw new InternalErrorException(
                        "PtolemyGraphModel does not recognize the " +
                        "given graph objects. link = " + link +
                        "tail = " + tail + " head = " + head);
	    }
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link = " + link +
                    "tail = " + tail + " head = " + head);
	}
    }

    /** Connect the specified link to the given vertex (as tail) and 
     *  port (as head), and then dispatch events to the listeners.
     *  @param eventSource The source of the request.
     *  @param link The link to change.
     *  @param vertex The vertex to connect.
     *  @param port The port to connect.
     */
    public void connectEdge(
            Object eventSource, Link link, Vertex vertex, ComponentPort port) {
	setEdgeTail(eventSource, link, vertex);
	setEdgeHead(eventSource, link, port);
    }

    /** Connect the specified edge to the given port (as tail) and
     *  vertex (as head), and then dispatch events to the listeners.
     *  @param eventSource The source of the request.
     *  @param link The link to change.
     *  @param port The port to connect.
     *  @param vertex The vertex to connect.
     */
    public void connectEdge(
             Object eventSource, Link link, ComponentPort port, Vertex vertex) {
	setEdgeTail(eventSource, link, port);
	setEdgeHead(eventSource, link, vertex);
    }

    /** Return true if the specified composite node contains the given node.
     *  Both arguments must be instances of NamedObj or an
     *  InternalErrorException will be thrown.
     *  @param composite A container.
     *  @param node A node that may be contained by the container.
     *  @return True if the container of the node is equal to the composite.
     */
    public boolean containsNode(Object composite, Object node) {
	if(composite instanceof NamedObj && node instanceof NamedObj) {
	    return containsNode((NamedObj)composite, (NamedObj)node);
	} else {
	    throw new InternalErrorException(
		    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. composite = " +
                    composite + "node = " + node);
	}
    }

    /** Return true if this composite node contains the given node.
     *  @param composite A container.
     *  @param node A node that may be contained by the container.
     *  @return True if the container of the node is equal to the composite.
     */
    public boolean containsNode(NamedObj composite, NamedObj node) {
        return composite.equals(getParent(node));
    }

    /**
     * Disconnect an edge from its two enpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event.
     */
    public void disconnectEdge(Object eventSource, Object edge) {
	if(edge instanceof Link) {
	    disconnectEdge(eventSource, (Link)edge);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. edge = " + edge);
	}
    }

    /** Disconnect an edge from its two endpoints and notify graph
     *  listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     *  event.
     *  @param eventSource The source of the event.
     *  @param link The link to delete.
     */
    public void disconnectEdge(Object eventSource, final Link link) {
	Object head = link.getHead();
	Object tail = link.getTail();
	_root.requestChange(new ChangeRequest(this,
		"disconnect link" + link.getFullName()) {
	    protected void _execute() throws Exception {
                link.unlink();
                link.setHead(null);
                link.setTail(null);
	    }
	});
	GraphEvent e;
	e = new GraphEvent(eventSource, GraphEvent.EDGE_HEAD_CHANGED,
			   link, head);
	dispatchGraphEvent(e);
	e = new GraphEvent(eventSource, GraphEvent.EDGE_TAIL_CHANGED,
			   link, tail);
	dispatchGraphEvent(e);
    }

    /**
     * Send an graph event to all of the graph listeners.  This
     * allows manual control of sending graph graph events, or
     * allows the user to send a STRUCTURE_CHANGED after some
     * inner-loop operations.  This class overrides the base class to 
     * ensure that the notification happens in the event thread.
     * 
     * @see setDispatchEnabled(boolean)
     */
    public void dispatchGraphEvent(final GraphEvent e) {
	// This has to happen in the swing thread, because Diva assumes
	// that everything happens in the swing thread.  We invoke later
	// because the changeRequest that we are listening for often
	// occurs in the execution thread of the ptolemy model.
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		// Otherwise notify any graph listeners 
		// that the graph might have
		// completely changed.
	        PtolemyGraphModel.super.dispatchGraphEvent(e);
	    }
	});
    }

    /** Return the head node of the given edge.
     *  @param link The edge, which must be an instance of Link or an
     *   InternalErrorException will be thrown.
     *  @return The head node.
     */
    public Object getHead(Object link) {
	if(link instanceof Link) {
	    return getHead((Link)link);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link = " + link);
	}       
    }
		
    /** Return the head node of the given edge.
     *  @param link The edge.
     *  @return The head node.
     */
    public Object getHead(Link link) {
	return link.getHead();
    }

    /** Return the number of nodes contained in the specified composite node.
     *  If the argument is a composite entity, then return the number of
     *  ports, plus the number of entities, plus the number of vertices
     *  associated with relations.  If the argument is an instance of Icon,
     *  then return the number of ports it contains.
     *  @param composite A composite node, which must be either an instance
     *   of Icon or of CompositeEntity, or an InternalErrorException will be
     *   thrown.
     *  @return The number of nodes that it contains.
     */
    public int getNodeCount(Object composite) {
	if(composite instanceof CompositeEntity) {
	    return getNodeCount((CompositeEntity)composite);
	} else if(composite instanceof Icon) {
	    return getNodeCount((Icon)composite);
       	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. composite = " + composite);
	}
    }
	
    /** Return the number of nodes contained in the specified entity, which
     *  is the number of ports, plus the number of entities, plus the
     *  number of vertices associated with relations.
     *  @param entity A composite entity.
     *  @return The number of nodes contained.
     */
    public int getNodeCount(CompositeEntity entity) {
	int count = entity.entityList().size() + entity.portList().size();
	Iterator relations = entity.relationList().iterator();
	while(relations.hasNext()) {
	    ComponentRelation relation = (ComponentRelation)relations.next();
	    count += relation.attributeList(Vertex.class).size();
	}
	return count;
    }
	
    /** Return the number of ports contained by the specified icon.
     *  @param icon A node in the graph.
     *  @return The number of ports it contains.
     */
    public int getNodeCount(Icon icon) {
	return ((ComponentEntity)icon.getContainer()).portList().size();
    }
	
    /** Return the parent node of this node, or null if there is no parent.
     *  @return The node that contains the specified node.
     */
    public Object getParent(Object node) {	
	if(node == getRoot()) {
	    return null;
	} else if(node instanceof Icon) {
	    return getParent((Icon)node);
	} else if(node instanceof Vertex) {
	    return getParent((Vertex)node);
	} else if(node instanceof Port) {
	    return getParent((Port)node);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. node = " + node);
	}       
    }

    /** Return the container of the container of the specified icon, or null
     *  if there is none.
     *  @return The node that contains the specified node.
     */
    public Object getParent(Icon icon) {	
        return icon.getContainer().getContainer();
    }

    /** Return the container of the container of the specified vertex, or null
     *  if there is none.
     *  @return The node that contains the specified node. 
     */
    public Object getParent(Vertex vertex) {
        return vertex.getContainer().getContainer();
    }

    /** If the specified port belongs to the root node, then return the
     *  composite entity that is the root node.  Otherwise, return the
     *  the icon belonging to the entity that contains the port.
     *  If there is no such icon, then throw an InternalErrorException.
     *  @return The node that contains the specified node. 
     */
    public Object getParent(Port port) {	
        ComponentEntity entity = (ComponentEntity)port.getContainer();
	if(entity.equals(getRoot())) 
	    return entity;
	else {
	    List iconList = entity.attributeList(Icon.class);
	    if(iconList.size() > 0) {
		return iconList.get(0);
	    } else {
		throw new InternalErrorException(
                        "entity does not contain an icon.");
	    }
	}
    }

    /** Return the root graph of this graph model.
     *  @return The root of this graph model, which is an instance of
     *   CompositeEntity.
     */
    public Object getRoot() {
        return _root;
    }

    /** Return the tail node of the specified edge, which must be
     *  an instance of Link or an InternalErrorException will be thrown.
     *  @param link A link representing an edge in the graph.
     *  @return The node that is the tail of the specified edge.
     */
    public Object getTail(Object link) {
	if(link instanceof Link) {
	    return getTail((Link)link);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link =" + link);
	}       
    }

    /** Return the tail node of the specified edge.
     *  @param link A link representing an edge in the graph.
     *  @return The node that is the tail of the specified edge.
     */
    public Object getTail(Link link) {
	return link.getTail();
    }

    /** Return the semantic object correspoding to the given node, edge,
     *  or composite.  A "semantic object" is an object associated with
     *  a node in the graph.  In this case, if the node is icon, the
     *  semantic object is an entity.  If it is a vertex or a link, the
     *  semantic object is a relation.  If it is a port, then the
     *  semantic object is the port itself.
     *  @param element A graph element.
     *  @return The semantic object associated with this element.
     */
    public Object getSemanticObject(Object element) {
	if(element instanceof Port) {
	    return element;
	} else if(element instanceof Vertex) {
	    return getSemanticObject((Vertex)element);
	} else if(element instanceof Icon) {
	    return getSemanticObject((Icon)element);
	} else if(element instanceof Link) {
	    return getSemanticObject((Link)element);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. object= " + element);
	}       
    }

    /** Return the container of the vertex, which is a relation.
     *  @param vertex A vertex in the graph.
     *  @return An instance of ComponentRelation.
     */
    public Object getSemanticObject(Vertex vertex) {
	return vertex.getContainer();
    }

    /** Return the container of the icon, which is a component entity.
     *  @param An icon.
     *  @return An instance of ComponentEntity.
     */
    public Object getSemanticObject(Icon icon) {
	return icon.getContainer();
    }

    /** Return the relation associated with the specified link.
     *  @param link A link representing an edge in the graph.
     *  @return An instance of ComponentRelation.
     */
    public Object getSemanticObject(Link link) {
	return link.getRelation();
    }
    
    /** Return an iterator over the links representing <i>in</i> edges
     *  of the given node.  An <i>in</i> edge is one for which this node is
     *  the head. If there are no in-edges, return an iterator with no 
     *  elements. The returned iterator does not support removal operations.
     *  @param node A node in the graph.
     *  @return An iterator over <i>in</i> edges of the node.
     */
    public Iterator inEdges(Object node) {
	if(node instanceof Port) {
	    return inEdges((Port)node);
	} else if(node instanceof Vertex) {
	    return inEdges((Vertex)node);
	} else if(node instanceof Icon) {
	    return new NullIterator();
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph object. object = " + node);
	}       
    }
    
    /** Return an iterator over the links representing <i>in</i> edges
     *  of the given port.  An <i>in</i> edge is one for which this port is
     *  the head. If there are no in-edges, return an iterator with no 
     *  elements. The returned iterator does not support removal operations.
     *  @param port A port.
     *  @return An iterator over <i>in</i> edges of the node.
     */
    public Iterator inEdges(Port port) {
	// make sure that the links to relations that we are connected to
	// are up to date.
	// FIXME inefficient if we are conneted to the same relation more
	// than once.
	List relationList = port.linkedRelationList();
	Iterator relations = relationList.iterator();
	while(relations.hasNext()) {
	    ComponentRelation relation = (ComponentRelation)relations.next();
	    _updateLinks(relation);
	}

	// Go through all the links, creating a list of those we are connected
	// to.
	List portLinkList = new LinkedList();
	List linkList = _root.attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object head = link.getHead();
	    if(head != null && head.equals(port)) {
		portLinkList.add(link);
	    }
	}

	return portLinkList.iterator();
    }	

    /** Return an iterator over the links representing <i>in</i> edges
     *  of the given vertex.  An <i>in</i> edge is one for which this vertex is
     *  the head. If there are no in-edges, return an iterator with no 
     *  elements. The returned iterator does not support removal operations.
     *  @param vertex A vertex in the graph.
     *  @return An iterator over <i>in</i> edges of the node.
     */
    public Iterator inEdges(Vertex vertex) {
	ComponentRelation relation = (ComponentRelation)vertex.getContainer();
	_updateLinks(relation);

	// Go through all the links, creating a list of those we are connected
	// to.
	List vertexLinkList = new LinkedList();
	List linkList = _root.attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object head = link.getHead();
	    if(head != null && head.equals(vertex)) {
		vertexLinkList.add(link);
	    }
	}
	
	return vertexLinkList.iterator();
    }	

    /** Return true if this edge is directed.  In this model, none of edges
     *  are directed, so this always returns false.
     *  @return False.
     */
    public boolean isDirected(Object edge) {
        return false;
    }

    /** Return true if the given object is a composite node in this model.
     *  An object is a composite if it is an instance of Icon or is the
     *  root node.
     *  @param node The node that may be composite.
     *  @return True if the node is composite.
     */
    public boolean isComposite(Object node) {
        return(node instanceof Icon) || getRoot().equals(node);
    }

    /** Return true if the given object is an edge in this model.
     *  That is, return true if is an instance of Link.
     *  @param element An object.
     *  @return True if is a link representing an edge.
     */
    public boolean isEdge(Object element) {
        // FIXME: This doesn't seem like a correct implementation.
        // What if it's a link that has nothing to do with this model?
        return (element instanceof Link);
    }

    /** Return true if the given object is an element of this model.
     *  This method returns true if the specified element is an instance
     *  of Icon, Vertex, or Port.
     *  @param element An object.
     *  @return True if the object is of the right type to be an element
     *   of this model.
     */
    public boolean isNode(Object element) {
        // FIXME: This doesn't seem like a correct implementation.
        // What if it has nothing to do with this model?
        return (element != null) && 
	    ((element instanceof Icon) || 
	     (element instanceof Vertex) ||
	     (element instanceof Port));
    }

    /** Return an iterator over the nodes in the given node.
     *  If the specified node is an Icon, then the iterator contains ports.
     *  If the specified node is a composite entity, then the iterator
     *  contains icons.
     *  @param object A node in the graph.
     *  @return An iterator over the nodes contained by the specified node.
     */
    public Iterator nodes(Object object) {
	if(object instanceof CompositeEntity) {
	    return nodes((CompositeEntity)object);
	} else if(object instanceof Icon) {
	    return nodes((Icon)object);
	} else {
	    throw new InternalErrorException(
		    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. icon = " + object);
	}       
    }

    /** Return an iterator over the ports in the entity represented
     *  by the specified icon.
     *  @param icon A icon in the graph.
     *  @return An iterator over ports.
     */
    public Iterator nodes(Icon icon) {
	ComponentEntity entity = (ComponentEntity) icon.getContainer();
	return entity.portList().iterator();
    }

    /** Return an iterator over the icons of entities inside the
     *  specified composite entity.  Normally, the specified composite
     *  entity is the top level, since that is the only entity in
     *  the graph that is represented as a composite entity
     *  rather than an icon.
     *  @param toplevel A composite entity.
     *  @return An iterator over icons.
     */
    public Iterator nodes(CompositeEntity toplevel) {
	Set nodes = new HashSet();
	Iterator entities = toplevel.entityList().iterator();
	while(entities.hasNext()) {
	    ComponentEntity entity = (ComponentEntity)entities.next();
	    List icons = entity.attributeList(Icon.class);
	    if(icons.size() > 0) {
		nodes.add(icons.get(0));
	    } else {
		// FIXME this is pretty minimal for an icon.
		try {
		    Icon icon = new EditorIcon(entity, "_icon");
		    nodes.add(icon);
		}
		catch (Exception e) {
		    throw new InternalErrorException("Failed to create " +
			"icon, even though one does not exist:" +
						     e.getMessage());
		}
	    }
	}

	Iterator ports = toplevel.portList().iterator();
	while(ports.hasNext()) {
	    ComponentPort port = (ComponentPort)ports.next();
	    nodes.add(port);
	}

	Iterator relations = toplevel.relationList().iterator();
	while(relations.hasNext()) {
	    ComponentRelation relation = (ComponentRelation)relations.next();
	    List vertexList = relation.attributeList(Vertex.class);
	    
	    if(vertexList.size() != 0) {		
		// Add in all the vertexes.
		Iterator vertexes = vertexList.iterator();
		while(vertexes.hasNext()) {
		    Vertex v = (Vertex)vertexes.next();
		    nodes.add(v);
		}
	    } else {
		// See if we need to create a vertex.
		// Count the linked ports.
		int count = relation.linkedPortList().size();
		if(count != 2) {
		    // Then there must be a vertex, so create one.
		    try {
			Vertex vertex = new Vertex(relation, 
						relation.uniqueName("vertex"));
			nodes.add(vertex);
		    }
		    catch (Exception e) {
			throw new InternalErrorException(
			"Failed to create " +
			"new vertex, even though one does not " +
			"already exist:" + e.getMessage());
		    }
		}
	    }
	}
	return nodes.iterator();
    }

    /** Return an iterator over the links representing <i>out</i> edges
     *  of the given node.  An <i>out</i> edge is one for which the given node is
     *  the tail. If there are no out edges, return an iterator with no 
     *  elements. The returned iterator does not support removal operations.
     *  @param node A node in the graph.
     *  @return An iterator over <i>out</i> edges (instances of Link) of the node.
     */
    public Iterator outEdges(Object node) {
	if(node instanceof Port) {
	    return outEdges((Port)node);
	} else if(node instanceof Vertex) {
	    return outEdges((Vertex)node);
	} else if(node instanceof Icon) {
	    return new NullIterator();
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph object. object = " + node);
	}       
    }

    /** Return an iterator over the links representing <i>out</i> edges
     *  of the given port.  An <i>out</i> edge is one for which the given port is
     *  the tail. If there are no out edges, return an iterator with no 
     *  elements. The returned iterator does not support removal operations.
     *  @param port A port.
     *  @return An iterator over instances of Link.
     */
    public Iterator outEdges(Port port) {
	// make sure that the links to relations that we are connected to
	// are up to date.
	// FIXME inefficient if we are conneted to the same relation more
	// than once.
	List relationList = port.linkedRelationList();
	Iterator relations = relationList.iterator();
	while(relations.hasNext()) {
	    ComponentRelation relation = (ComponentRelation)relations.next();
	    _updateLinks(relation);
	}

	// Go through all the links, creating a list of those we are connected
	// to.
	List portLinkList = new LinkedList();
	List linkList = _root.attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object tail = link.getTail();
	    if(tail != null && tail.equals(port)) {
		portLinkList.add(link);
	    }
	}

	return portLinkList.iterator();
    }	

    /** Return an iterator over the links representing <i>out</i> edges
     *  of the given vertex.  An <i>out</i> edge is one for which the given vertex is
     *  the tail. If there are no out edges, return an iterator with no 
     *  elements. The returned iterator does not support removal operations.
     *  @param vertex A vertex.
     *  @return An iterator over instances of Link.
     */
    public Iterator outEdges(Vertex vertex) {
	ComponentRelation relation = (ComponentRelation)vertex.getContainer();
	_updateLinks(relation);

	// Go through all the links, creating a list of those we are connected
	// to.
	List vertexLinkList = new LinkedList();
	List linkList = _root.attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object tail = link.getTail();
	    if(tail != null && tail.equals(vertex)) {
		vertexLinkList.add(link);
	    }
	}
	
	return vertexLinkList.iterator();
    }	

    /** Delete a node from its parent and notify
     *  graph listeners with a NODE_REMOVED event.
     *  This method generates a change request.
     *  @param eventSource The source of the event.
     *  @param node The node to remove, which must be an instance of
     *   ComponentPort, Icon, or Vertex.
     */
    public void removeNode(Object eventSource, Object node) {
	Object parent = getParent(node);
	if(node instanceof ComponentPort) {
	    removeNode(eventSource, (ComponentPort)node);
	} else if(node instanceof Icon) {
	    removeNode(eventSource, (Icon)node);
	} else if(node instanceof Vertex) {
	    removeNode(eventSource, (Vertex)node);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. node = " + node);
	}
	GraphEvent e = new GraphEvent(
                eventSource, GraphEvent.NODE_REMOVED, node, parent);
	dispatchGraphEvent(e);
    }
    
    /** Delete a port from its parent entity and notify
     *  graph listeners with a NODE_REMOVED event.
     *  This method generates a change request.
     *  @param eventSource The source of the event.
     *  @param node The port to remove.
     */
    public void removeNode(Object eventSource, ComponentPort port) {
	// remove any connected edges first.
        _removeConnectedEdges(eventSource, port);

	ComponentEntity container = (ComponentEntity)port.getContainer();
        String moml = "<deletePort name=\"" + port.getName() + "\"/>";
        ChangeRequest request = new MoMLChangeRequest(this, container, moml);
        _root.requestChange(request);
    }
	
    /** Delete an entity from its parent entity and notify
     *  graph listeners with a NODE_REMOVED event.
     *  This method generates a change request.
     *  @param eventSource The source of the event.
     *  @param icon The icon of the entity to remove.
     */
    public void removeNode(Object eventSource, Icon icon) {
	// remove all the edges connected to each port of the icon.
	Iterator nodes = nodes(icon);
	while(nodes.hasNext()) {
	    _removeConnectedEdges(eventSource, (ComponentPort)nodes.next());
	}
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	CompositeEntity container = (CompositeEntity)entity.getContainer();
        String moml = "<deleteEntity name=\"" + entity.getName() + "\"/>";
        ChangeRequest request = new MoMLChangeRequest(this, container, moml);
        _root.requestChange(request);
    }  

    /** Delete a vertex from its parent and notify
     *  graph listeners with a NODE_REMOVED event.
     *  This method generates a change request.
     *  @param eventSource The source of the event.
     *  @param icon The icon of the entity to remove.
     */
    public void removeNode(Object eventSource, final Vertex vertex) {
	ComponentRelation relation = (ComponentRelation)vertex.getContainer();
	CompositeEntity container = (CompositeEntity)relation.getContainer();
        String moml = "<deleteRelation name=\"" + relation.getName() + "\"/>";
        ChangeRequest request = new MoMLChangeRequest(this, container, moml);
        _root.requestChange(request);
    }

    /** Connect an edge to the given head node and notify listeners
     *  with an EDGE_HEAD_CHANGED event.
     *  @param eventSource The source of the request.
     *  @param link The edge to change, which must be an instance of Link
     *   or an InternalErrorException will be thrown.
     *  @param head The new head for the edge, which must be an instance
     *   of NamedObj or a ClassCastException will be thrown.
     */
    public void setEdgeHead(Object eventSource, Object link, Object object) {
	if(link instanceof Link) {
	    setEdgeHead(eventSource, (Link)link, (NamedObj)object);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link = " + link + 
                    "object = " + object);
	}
    }

    /** Connect an edge to the given head node and notify listeners
     *  with an EDGE_HEAD_CHANGED event.
     *  @param eventSource The source of the request.
     *  @param link The edge to change.
     *  @param head The new head for the edge.
     */
    public void setEdgeHead(
            Object eventSource, final Link link, final Object head) {
	_root.requestChange(new ChangeRequest(
                this, "move head of link" + link.getFullName()) {
	    protected void _execute() throws Exception {
                link.unlink();		
		link.setHead(head);
                link.link();
	    }
	});
	GraphEvent e = new GraphEvent(
                eventSource, GraphEvent.EDGE_HEAD_CHANGED, link, head);
        dispatchGraphEvent(e);
    }

    /** Connect an edge to the given head node and notify listeners
     *  with an EDGE_HEAD_CHANGED event.
     *  @param eventSource The source of the request.
     *  @param link The link to change, which must be an instance of Link or
     *   an InternalErrorException will be thrown.
     *  @param tail The new tail for the link, which must be an instance of
     *   NamedObj or a class cast exception will be thrown.
     */
    public void setEdgeTail(Object eventSource, Object link, Object tail) {
	if(link instanceof Link) {
	    setEdgeTail(eventSource, (Link)link, (NamedObj)tail);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link = " + link +
                    "tail = " + tail);
	}
    }

    /** Connect an edge to the given tail node and notify listeners
     *  with an EDGE_TAIL_CHANGED event.
     *  @param eventSource The source of the request.
     *  @param link The link to change.
     *  @param tail The new tail for the link.
     */
    public void setEdgeTail(
            Object eventSource, final Link link, final NamedObj tail) {
	_root.requestChange(new ChangeRequest(
                this, "move head of link" + link.getFullName()) {
	    protected void _execute() throws Exception {
                link.unlink();
		link.setTail(tail);
                link.link();
	    }
	});
	GraphEvent e = new GraphEvent(
                eventSource, GraphEvent.EDGE_TAIL_CHANGED, link, tail);
        dispatchGraphEvent(e);
    }

    /** Set the semantic object corresponding to the given node, edge,
     *  or composite.  The "semantic object" is simply some object
     *  associated with the node.  In this class, the semantic object
     *  is always an entity, a port, or a relation, and it does not
     *  make sense to set it directly.  Thus, this method throws
     *  an InternalErrorException (which is admittedly the wrong
     *  exception to throw).
     */
    public void setSemanticObject(Object o, Object sem) {
	throw new InternalErrorException(
                "PtolemyGraphModel does not support" + 
                " setting semantic objects.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Mutations may happen to the ptolemy model without the knowledge of
     *  this model.  This change listener listens for those changes
     *  and when they occur, issues a GraphEvent so that any views of
     *  this graph model can come back and update themselves.
     */
    public class GraphChangeListener implements ChangeListener {

        /** Notify the listener that a change has been successfully executed.
	 *  @param change The change that has been executed.
	 */
	public void changeExecuted(ChangeRequest change) {
	    // Ignore anything that comes from this graph model.  
	    // the other methods take care of issuing the graph event in
	    // that case.
	    if(change.getOriginator() == PtolemyGraphModel.this) {
                return;
            }
	    // This has to happen in the swing thread, because Diva assumes
	    // that everything happens in the swing thread.  We invoke later
	    // because the changeRequest that we are listening for often
	    // occurs in the execution thread of the ptolemy model.
	    // SwingUtilities.invokeLater(new Runnable() {
	    //public void run() {
		    // Otherwise notify any graph listeners 
		    // that the graph might have
		    // completely changed.
		    dispatchGraphEvent(new GraphEvent(
			this, GraphEvent.STRUCTURE_CHANGED, getRoot()));
		    //}
		    //});
	}

        /** Notify the listener that the change has failed with the
         *  specified exception.
 	 *  @param change The change that has failed.
         *  @param exception The exception that was thrown.
         */
        public void changeFailed(ChangeRequest change, Exception exception) {
            ExceptionHandler.show("Change failed", exception);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Remove all the edges connected to the given port.
    private void _removeConnectedEdges(Object eventSource, 
				       ComponentPort port) {
	for(Iterator edges = outEdges(port); edges.hasNext(); ) {
	    Object edge = edges.next();
	    disconnectEdge(eventSource, edge);
	}
	for(Iterator edges = inEdges(port); edges.hasNext(); ) {
	    Object edge = edges.next();
	    disconnectEdge(eventSource, edge);
	}
    }

    // Check to make sure that there is a Link object representing every
    // link connected to the given relation.  In some cases, it may
    // be necessary to create a vertex to represent the relation as well.
    private void _updateLinks(ComponentRelation relation) {
	List linkedPortList = relation.linkedPortList();
	int allPortCount = linkedPortList.size();

	// Go through all the links that currently exist, and remove ports
	// from the linkedPortList that already have a Link object.
	// FIXME this could get expensive 
	List linkList = _root.attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    // only consider links that are associated with this relation.
	    if(link.getRelation() != relation) continue;
	    // remove any ports that this link is linked to.  We don't need
	    // to manufacture those links.
	    Object tail = link.getTail();
	    if(tail != null && linkedPortList.contains(tail) ) {
		linkedPortList.remove(tail);
	    }
	    Object head = link.getHead();
	    if(head != null && linkedPortList.contains(head)) {
		linkedPortList.remove(head);
	    }
	}

	// Count the linked ports.
	int unlinkedPortCount = linkedPortList.size();
	
	// If there are no links left to create, then just return.
	if(unlinkedPortCount == 0) return;

	Iterator vertexes =
	    relation.attributeList(Vertex.class).iterator();
	// get the Root vertex.  This is where we will manufacture links.
	Vertex rootVertex = null;
	while(vertexes.hasNext()) {
	    Vertex v = (Vertex)vertexes.next();
	    if(v.getLinkedVertex() == null) {
		rootVertex = v;
	    }
	}
	
	// If there are no vertecies, and the relation has exactly
	// two connections, neither of which has been made yet, then 
	// create a link without a vertex for the relation.
	if(rootVertex == null && allPortCount == 2 && unlinkedPortCount == 2) {
	    Port port1 = (Port)linkedPortList.get(0);
	    Port port2 = (Port)linkedPortList.get(1);
	    Link link;
	    try {
		link = new Link(_root, _root.uniqueName("link"));
	    } 
	    catch (Exception e) {
		throw new InternalErrorException(
		    "Failed to create " +
		    "new link, even though one does not " +
		    "already exist:" + e.getMessage());
	    }
	    link.setRelation(relation);
	    link.setHead(port1);
	    link.setTail(port2);
	} else {		  
	    // A regular relation with a diamond.
	    // Create a vertex if one is not found
	    if(rootVertex == null) {
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
	    while(ports.hasNext()) {
		Port port = (Port)ports.next();
		Link link;
		try {
		    link = new Link(_root, 
				    _root.uniqueName("link"));
		}
		catch (Exception e) {
		    throw new InternalErrorException(
			    "Failed to create " +
			    "new Link, even though one does not " +
			    "already exist:" + e.getMessage());
		}
		link.setRelation(relation);
		link.setHead(port);
		link.setTail(rootVertex);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The root of the graph contained by this model.
    private CompositeEntity _root = null;
}
