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

import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.event.*;
import ptolemy.moml.*;
import diva.graph.*;
import diva.graph.toolbox.*;
import diva.util.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// PtolemyGraphModel
/**
A graph for basic Ptolemy II models.  This model is useful for visual notations
which expose all of the kernel objects (ports, relations, links and entities).

This graph model represents ports, entities and relations as nodes.  Entities
are proxied in the model by the icon that represents them.  Relations are
proxied in the model by its vertecies (which generally exist in different
places!)  Ports represent themselves in the model.  

Edges may be connected between a port and a vertex, or between a port and
another port.  For visual simplicity, both types of edges are represented by
an instance of the Link class.  If an edge is placed between a port 
and a vertex
then the Link represents a proxy for a single link between the port and the 
vertex's Relation.  However, if an edge is placed between two ports, then 
it proxies a Relation (with no vertex) and links from the relation to each 
port.
@author Steve Neuendorffer
@version $Id$
 */
public class PtolemyGraphModel extends AbstractGraphModel 
    implements MutableGraphModel, Nameable{
    
    /**
     * Construct an empty graph model whose
     * root is a new CompositeEntity.
     */
    public PtolemyGraphModel() {
	_changeListener = new GraphChangeListener();
	_root = new CompositeEntity();
	_root.addChangeListener(_changeListener);
	_visualObjectMap = new HashMap();
    }

    /**
     * Construct a new graph model whose root is the given composite entity.
     * If an entity exists in the given composite that does not have an 
     * icon, then create a default icon for it.  If a relation exists in the
     * given composite that does not have a vertex and it is connected to 
     * exactly two ports, then create a link to represent the relation.
     * otherwise if a Relation exists without a vertex, create a Vertex to 
     * represent it.
     */
    public PtolemyGraphModel(CompositeEntity toplevel) {
	_changeListener = new GraphChangeListener();
	_root = toplevel;
	_root.addChangeListener(_changeListener);
	_visualObjectMap = new HashMap();
    }
    	
    /**
     * Return true if the head of the given edge can be attached to the
     * given node.
     */
    public boolean acceptHead(Object edge, Object node) {
	if (node instanceof Port ||
	    node instanceof Vertex) {
	    return true;
	} else
	    return false;
    }

    /**
     * Return true if the tail of the given edge can be attached to the
     * given node.
     */
    public boolean acceptTail(Object edge, Object node) {
	if (node instanceof Port ||
	    node instanceof Vertex) {
	    return true;
	} else
	    return false;
    }
    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, Object node, Object parent) {
	if(node instanceof ComponentPort &&
	   parent instanceof Icon) {
	    addNode(eventSource, (ComponentPort)node, (Icon)parent);
	} else if (node instanceof Icon &&
		   parent instanceof CompositeEntity) {
	    addNode(eventSource, (Icon)node, (CompositeEntity)parent);
	} else if (node instanceof Port &&
		   parent instanceof CompositeEntity) {
	    addNode(eventSource, (ComponentPort)node, (CompositeEntity)parent);
	} else if (node instanceof Vertex &&
		   parent instanceof CompositeEntity) {
	    addNode(eventSource, (Vertex)node, (CompositeEntity)parent);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. node = " + node + 
                    "parent = " + parent);
	}
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, Vertex vertex, 
			CompositeEntity parent) {
	ComponentRelation relation = 
	    (ComponentRelation)vertex.getContainer();
	try {
	    _doChangeRequest(new PlaceRelation(this, relation, parent));
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_ADDED,
				      vertex, parent);
        dispatchGraphEvent(e);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, Icon icon, 
			CompositeEntity parent) {
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	try {
	    _doChangeRequest(new PlaceEntity(this, entity, parent));
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_ADDED,
				      icon, parent);
        dispatchGraphEvent(e);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, ComponentPort port, 
			CompositeEntity parent) {
	try {
	    _doChangeRequest(new PlacePort(this, port, parent));
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_ADDED,
				      port, parent);
        dispatchGraphEvent(e);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, ComponentPort port, Icon icon) {
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	try {
	    port.setContainer(entity);
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_ADDED,
				      port, icon);
        dispatchGraphEvent(e);
    }

    /**
     * Connect the given edge to the given tail and head nodes,
     * then dispatch events to the listeners.
     */
    public void connectEdge(Object eventSource, 
			    Object link,
			    Object tail,
			    Object head) {
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
	    }
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link = " + link +
                    "tail = " + tail + " head = " + head);
	}
    }

    public void connectEdge(Object eventSource, 
			    Link link, 
			    Vertex vertex, 
			    ComponentPort port) {
	setEdgeTail(eventSource, link, vertex);
	setEdgeHead(eventSource, link, port);
    }

    /**
     * Connect the given edge to the given tail and head nodes,
     * then dispatch events to the listeners.
     */
    public void connectEdge(Object eventSource,
			    Link link, 
			    ComponentPort port,
			    Vertex vertex) {
	setEdgeTail(eventSource, link, port);
	setEdgeHead(eventSource, link, vertex);
    }

    /** Return the container. */
    public Nameable getContainer() {
	return null;
    }

    /**
     * Return true if this composite node contains the given node.
     */
    public boolean containsNode(Object composite, Object node) {
	if(composite instanceof NamedObj &&
	   node instanceof NamedObj) {
	    return containsNode((NamedObj)composite, (NamedObj)node);
	} else {
	    throw new InternalErrorException(
		    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. composite = " +
                    composite + "node = " + node);
	}
    }

    /**
     * Return true if this composite node contains the given node.
     */
    public boolean containsNode(NamedObj composite, NamedObj node) {
        return composite.equals(getParent(node));
    }

    /** 
     * Return a string representation of this graph model.
     */
    public String description() {
	return "PtolemyGraphModel {" + _root.description() + "}";
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

    /**
     * Disconnect an edge from its two endpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event.
     */
    public void disconnectEdge(Object eventSource, final Link link) {
	Object head = link.getHead();
	Object tail = link.getTail();
	_doChangeRequest(new ChangeRequest(this, 
		"disconnect link" + link.getFullName()) {
	    public void execute() throws ChangeFailedException {
		try {
		    link.unlink();
		    link.setHead(null);
		    link.setTail(null);
		} catch (IllegalActionException ex) {
		    throw new ChangeFailedException(this, ex.getMessage());
		} catch (NameDuplicationException ex) {
		    throw new ChangeFailedException(this, ex.getMessage());
		}
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
     * Return the root graph of this graph model.
     */
    public Object getRoot() {
        return _root;
    }

    /** Return the full name, which reflects the container object, if there
     *  is one. For example the implementation in NamedObj concatenates the
     *  full name of the container objects with the name of the this object,
     *  separated by periods.
     *  @return The full name of the object.
     */
    public String getFullName() {
	return "PtolemyGraphModel." + _root.getFullName();
    }

    /**
     * Return the head node of the given edge.
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
		
    /**
     * Return the head node of the given edge.
     */
    public Object getHead(Link link) {
	return link.getHead();
    }
	
    /** Return the name of the object.
     *  @return The name of the object.
     */
    public String getName() {
	return "PtolemyGraphModel";
    }
	
    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    public int getNodeCount(Object composite) {
	if(!isComposite(composite)) {
	    throw new InternalErrorException("object " + composite + 
					     " is not a composite node in " + 
					     "this graph model.");
	}
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
	
    /**
     * Return the number of nodes contained in
     * this graph or composite node.
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
	
    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    public int getNodeCount(Icon icon) {
	return ((ComponentEntity)icon.getContainer()).portList().size();
    }
	
    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
     */
    public Object getParent(Object node) {	
	if(node instanceof Icon) {
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

    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
     */
    public Object getParent(Icon icon) {	
        return icon.getContainer().getContainer();
    }

    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
     */
    public Object getParent(Vertex vertex) {
        return vertex.getContainer().getContainer();
    }

    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
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

    /**
     * Return the tail node of this edge.
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

    /**
     * Return the tail node of this edge.
     */
    public Object getTail(Link link) {
	return link.getTail();
    }

    /**
     * Return the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public Object getSemanticObject(Object o) {
	if(o instanceof Port) {
	    return o;
	} else if(o instanceof Vertex) {
	    return getSemanticObject((Vertex)o);
	} else if(o instanceof Icon) {
	    return getSemanticObject((Icon)o);
	} else if(o instanceof Link) {
	    return getSemanticObject((Link)o);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. object= " + o);
	}       
    }

    /**
     * Return the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public Object getSemanticObject(Vertex vertex) {
	return vertex.getContainer();
    }

    /**
     * Return the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public Object getSemanticObject(Icon icon) {
	return icon.getContainer();
    }

    /**
     * Return the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public Object getSemanticObject(Link link) {
	return link.getRelation();
    }
    
    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
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
    
    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges(Port object) {
	// make sure that the links to relations that we are connected to
	// are up to date.
	// FIXME inefficient if we are conneted to the same relation more
	// than once.
	List relationList = object.linkedRelationList();
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
	    if(head != null && head.equals(object)) {
		//System.out.println("OutEdges(" + object +
		//		   ") includes " + link);
		portLinkList.add(link);
	    }
	}

	return portLinkList.iterator();
    }	

    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges(Vertex object) {
	ComponentRelation relation = (ComponentRelation)object.getContainer();
	_updateLinks(relation);

	// Go through all the links, creating a list of those we are connected
	// to.
	List vertexLinkList = new LinkedList();
	List linkList = _root.attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object head = link.getHead();
	    if(head != null && head.equals(object)) {
		vertexLinkList.add(link);
	    }
	}
	
	return vertexLinkList.iterator();
    }	

    /**
     * Return whether or not this edge is directed.
     */
    public boolean isDirected(Object edge) {
        return false;
    }

    /**
     * Return true if the given object is a composite
     * node in this model, i.e. it contains children.
     */
    public boolean isComposite(Object o) {
        return(o instanceof Icon) || getRoot().equals(o);
    }

    /**
     * Return true if the given object is a 
     * edge in this model.
     */
    public boolean isEdge(Object o) {
        return (o != null) && (o instanceof Link);
    }

    /**
     * Return true if the given object is a 
     * node in this model.
     */
    public boolean isNode(Object o) {
        return (o != null) && 
	    ((o instanceof Icon) || 
	     (o instanceof Vertex) ||
	     (o instanceof Port));
    }

    /**
     * Provide an iterator over the nodes in the
     * given graph or composite node.  This iterator
     * does not necessarily support removal operations.
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

    /**
     * Provide an iterator over the nodes in the
     * given graph or composite node.  This iterator
     * does not necessarily support removal operations.
     */
    public Iterator nodes(Icon icon) {
	ComponentEntity entity = (ComponentEntity) icon.getContainer();
	return entity.portList().iterator();
    }

    /**
     * Provide an iterator over the nodes in the
     * given graph or composite node.  This iterator
     * does not necessarily support removal operations.
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
		// FIXME this is pretty minimal
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


    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
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

    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges(Port object) {
	// make sure that the links to relations that we are connected to
	// are up to date.
	// FIXME inefficient if we are conneted to the same relation more
	// than once.
	List relationList = object.linkedRelationList();
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
	    if(tail != null && tail.equals(object)) {
		//System.out.println("OutEdges(" + object + 
		//		   ") includes " + link);
		portLinkList.add(link);
	    }
	}

	return portLinkList.iterator();
    }	

    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges(Vertex object) {
	ComponentRelation relation = (ComponentRelation)object.getContainer();
	_updateLinks(relation);

	// Go through all the links, creating a list of those we are connected
	// to.
	List vertexLinkList = new LinkedList();
	List linkList = _root.attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object tail = link.getTail();
	    if(tail != null && tail.equals(object)) {
		vertexLinkList.add(link);
	    }
	}
	
	return vertexLinkList.iterator();
    }	

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
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
	GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_REMOVED,
				      node, parent);
	dispatchGraphEvent(e);
    }
    
    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Object eventSource, ComponentPort port) {
	// remove any connected edges first.
	
	_removeConnectedEdges(eventSource, port);
	
	
	_doChangeRequest(new RemovePort(this, port));
    }
	
    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Object eventSource, Icon icon) {
	// remove all the edges connected to each port of the icon.
	Iterator nodes = nodes(icon);
	while(nodes.hasNext()) {
	    _removeConnectedEdges(eventSource, (ComponentPort)nodes.next());
	}
	
	final ComponentEntity entity = (ComponentEntity)icon.getContainer();
	_doChangeRequest(new RemoveActor(this, entity));

    }  

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Object eventSource, final Vertex vertex) {
	final ComponentRelation relation =
	    (ComponentRelation)vertex.getContainer();
	_doChangeRequest(new RemoveRelation(this, relation));
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeHead(Object eventSource, Object link, Object object) {
	if(link instanceof Link) {
	    setEdgeHead(eventSource, 
			(Link)link,
			(NamedObj)object);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link = " + link + 
                    "object = " + object);
	}
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeHead(Object eventSource, 
			    final Link link, final Object head) {
	_doChangeRequest(new ChangeRequest(this, "move head of link" + 
					   link.getFullName()) {
	    public void execute() throws ChangeFailedException {
		System.out.println("executing change request");
		try {
		    link.unlink();
		} catch (Exception ex) {
		    throw new ChangeFailedException(this, ex.getMessage());
		}
		
		link.setHead(head);
		try {
		    link.link();
		} catch (Exception ex) {
		    throw new ChangeFailedException(this, ex.getMessage());
		}
		System.out.println("finished change request");
	    }
	});
	GraphEvent e = new GraphEvent(eventSource, 
				      GraphEvent.EDGE_HEAD_CHANGED,
				      link, head);
        dispatchGraphEvent(e);
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeTail(Object eventSource, Object link, Object object) {
	if(link instanceof Link) {
	    setEdgeTail(eventSource, (Link)link,
			(NamedObj)object);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model does not recognize the " +
		    "given graph objects. link = " + link +
                    "object = " + object);
	}
    }

    /**
     * Connect an edge to the given tail node and notify listeners
     * with an EDGE_TAIL_CHANGED event.
     */
    public void setEdgeTail(Object eventSource, final Link link,
			    final NamedObj tail) {
	_doChangeRequest(new ChangeRequest(this, "move head of link" + 
					   link.getFullName()) {
	    public void execute() throws ChangeFailedException {
		System.out.println("executing change request");
		try {
		    link.unlink();
		} catch (Exception ex) {
		    throw new ChangeFailedException(this, ex.getMessage());
		}
		link.setTail(tail);
		try {
		    link.link();
		} catch (Exception ex) {
		    throw new ChangeFailedException(this, ex.getMessage());
		}
	    }
	});
	GraphEvent e = new GraphEvent(eventSource, 
				      GraphEvent.EDGE_TAIL_CHANGED,
				      link, tail);
        dispatchGraphEvent(e);
    }

    /** Set or change the name. By convention, if the argument is null,
     *  the name should be set to an empty string rather than to null.
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period.
     *  @exception NameDuplicationException If the container already
     *   contains an object with this name.
     */
    public void setName(String name)
	throws IllegalActionException, NameDuplicationException {
	throw new IllegalActionException(
	     "The name of a graph model cannot be changed");
    }

    /**
     * Set the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public void setSemanticObject(Object o, Object sem) {
	throw new InternalErrorException(
                "PtolemyGraphModel does not support" + 
                " setting semantic objects.");
    }

    /** Mutations may happen to the ptolemy model without the knowledge of
     * this model.  This change listener listens for those changes
     * and when they occur, issues a GraphEvent so that any views of
     * this graph model can come back and update themselves.
     */
    public class GraphChangeListener implements ChangeListener {
        /** Notify the listener that a change has been successfully executed.
	 *  @param change The change that has been executed.
	 */
	public void changeExecuted(ChangeRequest change) {
	    // Ignore anything that comes from this graph model.  
	    // the other methods take care of issuing the graph event in
	    // that case.
	    if(change.getOriginator() == PtolemyGraphModel.this) return;
	    // Otherwise notify any graph listeners that the graph might have
	    // completely changed.
	    dispatchGraphEvent(new GraphEvent(this, 
				  GraphEvent.STRUCTURE_CHANGED,
				  getRoot()));
	}
    }    

    // Perform the specified change request.  Queue the request with the
    // root entity.  If the change fails, then throw a graph exception. 
    private void _doChangeRequest(ChangeRequest request) {
	try {
	    _root.requestChange(request);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new GraphException(ex);
	}
    }
    
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

    /**
     * The root of the graph contained by this model.
     */
    private CompositeEntity _root = null;

    private Map _visualObjectMap;

    private GraphChangeListener _changeListener;
}

