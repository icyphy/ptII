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
    implements MutableGraphModel {
    
    /**
     * Construct an empty graph model whose
     * root is a new CompositeEntity.
     */
    public PtolemyGraphModel() {
	_root = new CompositeEntity();
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
	_root = toplevel;

	// First create icons for entities that don't have one.
	Iterator entities = toplevel.entityList().iterator();
	while(entities.hasNext()) {
	    Entity entity = (Entity)entities.next();
	    Icon icon = (Icon)entity.getAttribute("_icon");
	    if(icon == null) {
		// FIXME this is pretty minimal
		try {
		    icon = new EditorIcon(entity, "_icon");
		}
		catch (Exception e) {
		    throw new InternalErrorException("Failed to create " +
			"icon, even though one does not exist:" +
						     e.getMessage());
		}
	    }
	}
	
	// Create Links or Vertexes for relations that don't have them.
	Iterator relations = toplevel.relationList().iterator();
	while(relations.hasNext()) {
	    ComponentRelation relation = (ComponentRelation)relations.next();
	    Iterator vertexes =
		relation.attributeList(Vertex.class).iterator();
	    // get the Root vertex.
	    Vertex rootVertex = null;
	    while(vertexes.hasNext()) {
		Vertex v = (Vertex)vertexes.next();
		if(v.getLinkedVertex() == null) {
		    rootVertex = v;
		}
	    }

	    // Count the linked ports.
	    int count = 0;
	    Enumeration links = relation.linkedPorts();
	    while(links.hasMoreElements()) {
		links.nextElement();
		count++;
	    }
	    
	    // If there are no verticies, and the relation has
	    // two connections, then create a direct link.
	    if(rootVertex == null && count == 2) {
		links = relation.linkedPorts();
		Port port1 = (Port)links.nextElement();
		Port port2 = (Port)links.nextElement();
		Link link;
		try {
		    link = new Link(toplevel, toplevel.uniqueName("link"));
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
		links = relation.linkedPorts();
		while(links.hasMoreElements()) {
		    Port port = (Port)links.nextElement();
		    Link link;
		    try {
			link = new Link(toplevel, 
					     toplevel.uniqueName("link"));
		    }
		    catch (Exception e) {
			throw new InternalErrorException(
				"Failed to create " +
			        "new link, even though one does not " +
			        "already exist:" + e.getMessage());
		    }
		    link.setRelation(relation);
		    link.setHead(port);
		    link.setTail(rootVertex);
		}
	    }
	}
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
    public void addNode(Object node, Object parent) {
	if(node instanceof ComponentPort &&
	   parent instanceof Icon) {
	    addNode((ComponentPort)node, (Icon)parent);
	} else if (node instanceof Icon &&
		   parent instanceof CompositeEntity) {
	    addNode((Icon)node, (CompositeEntity)parent);
	} else if (node instanceof Port &&
		   parent instanceof CompositeEntity) {
	    addNode((ComponentPort)node, (CompositeEntity)parent);
	} else if (node instanceof Vertex &&
		   parent instanceof CompositeEntity) {
	    addNode((Vertex)node, (CompositeEntity)parent);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. node = " + node + 
                    "parent = " + parent);
	}
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Vertex vertex, CompositeEntity parent) {
	ComponentRelation relation = 
	    (ComponentRelation)vertex.getContainer();
	try {
	    _doChangeRequest(new PlaceRelation(vertex, relation, parent));
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(GraphEvent.NODE_ADDED,
                this, vertex, parent);
        dispatchGraphEvent(e);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Icon icon, CompositeEntity parent) {
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	try {
	    _doChangeRequest(new PlaceEntity(icon, entity, parent));
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(GraphEvent.NODE_ADDED,
                this, icon, parent);
        dispatchGraphEvent(e);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(ComponentPort port, CompositeEntity parent) {
	try {
	    _doChangeRequest(new PlacePort(port, port, parent));
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(GraphEvent.NODE_ADDED,
                this, port, parent);
        dispatchGraphEvent(e);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(ComponentPort port, Icon icon) {
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	try {
	    port.setContainer(entity);
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
        GraphEvent e = new GraphEvent(GraphEvent.NODE_ADDED,
                this, port, icon);
        dispatchGraphEvent(e);
    }

    /**
     * Connect the given edge to the given tail and head nodes,
     * then dispatch events to the listeners.
     */
    public void connectEdge(Object link,
			    Object tail,
			    Object head) {
	if(link instanceof Link) {
	    if(tail instanceof ComponentPort &&
	       head instanceof Vertex) {
		connectEdge((Link)link, 
			    (ComponentPort)tail,
			    (Vertex)head);
	    } else if(head instanceof ComponentPort &&
		      tail instanceof Vertex) {
		connectEdge((Link)link, 
			    (Vertex)tail,
			    (ComponentPort)head);
	    }
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. link = " + link +
                    "tail = " + tail + " head = " + head);
	}
    }

    public void connectEdge(Link link, 
			    Vertex vertex, 
			    ComponentPort port) {
	setEdgeTail(link, vertex);
	setEdgeHead(link, port);
    }

    /**
     * Connect the given edge to the given tail and head nodes,
     * then dispatch events to the listeners.
     */
    public void connectEdge(Link link, 
			    ComponentPort port,
			    Vertex vertex) {
	setEdgeTail(link, port);
	setEdgeHead(link, vertex);
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
                    "Ptolemy Graph Model only handles " +
                    "named objects. composite = " +
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
     * Disconnect an edge from its two enpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event.
     */
    public void disconnectEdge(Object edge) {
	if(edge instanceof ComponentRelation) {
	    disconnectEdge((ComponentRelation)edge);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. edge = " + edge);
	}
    }

    /**
     * Disconnect an edge from its two enpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event.
     */
    public void disconnectEdge(final Link link) {
	Object head = link.getHead();
	Object tail = link.getTail();
	_doChangeRequest(new ChangeRequest(link, 
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
	e = new GraphEvent(GraphEvent.EDGE_HEAD_CHANGED,
			   this, link, head);
	dispatchGraphEvent(e);
	e = new GraphEvent(GraphEvent.EDGE_TAIL_CHANGED,
			   this, link, tail);
	dispatchGraphEvent(e);
    }

    /**
     * Return the root graph of this graph model.
     */
    public Object getRoot() {
        return _root;
    }

    /**
     * Return the head node of the given edge.
     */
    public Object getHead(Object link) {
	if(link instanceof Link) {
	    return getHead((Link)link);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. link = " + link);
	}       
    }
		
    /**
     * Return the head node of the given edge.
     */
    public Object getHead(Link link) {
	return link.getHead();
    }
		
    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    public int getNodeCount(Object composite) {
	if(composite instanceof CompositeEntity) {
	    return getNodeCount((CompositeEntity)composite);
	} else if(composite instanceof Icon) {
	    return getNodeCount((Icon)composite);
       	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. composite = " + composite);
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
                    "Ptolemy Graph Model only handles " +
                    "named objects. node = " + node);
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
                    "Ptolemy Graph Model only handles " +
                    "named objects. link =" + link);
	}       
    }

    /**
     * Return the tail node of this edge.
     */
    public Object getTail(Link link) {
	return link.getTail();
    }

    /**
     * Return the visual object correspoding
     * to the given node, edge, or composite.
     */
    public Object getVisualObject(Object o) {
	if(o instanceof NamedObj) {
	    return getVisualObject((NamedObj)o);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. object =" + o);
	}       	
    }

    /**
     * Return the visual object correspoding
     * to the given node, edge, or composite.
     */
    public Object getVisualObject(NamedObj o) {
	List list = o.attributeList(FigureAttribute.class);
	Iterator i = list.iterator();
	if(i.hasNext()) {
	    FigureAttribute a = (FigureAttribute)list.iterator().next();
	    return a.getFigure();
	} else {
	    return null;
	}
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
                    "Ptolemy Graph Model only handles " +
                    "named objects. object= " + o);
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
	if(node instanceof Port || node instanceof Vertex) {
	    return inEdges((NamedObj)node);
	} else if(node instanceof Icon) {
	    return new NullIterator();
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. icon = " + node);
	}       
    }
    
    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges(NamedObj object) {
	List linkList = _root.attributeList(Link.class);
	// filter out the links that aren't attached to this port.
	List portLinks = new LinkedList();
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object head = link.getHead();
	    if(head != null && head.equals(object)) {
		portLinks.add(link);
	    }
	}
	return portLinks.iterator();
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
        return(o instanceof Icon);
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
                    "Ptolemy Graph Model only handles " +
                    "named objects. icon = " + object);
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
	    List vertexes = relation.attributeList(Vertex.class);
	    if(vertexes.size() > 0) {
		nodes.add(vertexes.get(0));
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
	if(node instanceof Port || node instanceof Vertex) {
	    return outEdges((NamedObj)node);
	} else if(node instanceof Icon) {
	    return new NullIterator();
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. icon = " + node);
	}       
    }

    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges(NamedObj object) {
	List linkList = _root.attributeList(Link.class);
	// filter out the links that aren't attached to this port.
	List portLinks = new LinkedList();
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    Object tail = link.getTail();
	    if(tail != null && tail.equals(object)) {
		portLinks.add(link);
	    }
	}
	return portLinks.iterator();
    }	

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Object node) {
	Object parent = getParent(node);
	if(node instanceof ComponentPort) {
	    removeNode((ComponentPort)node);
	} else if(node instanceof Icon) {
	    removeNode((Icon)node);
	} else if(node instanceof Vertex) {
	    removeNode((Vertex)node);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. node = " + node);
	}
	GraphEvent e = new GraphEvent(GraphEvent.NODE_REMOVED,
				      this, node, parent);
	dispatchGraphEvent(e);
    }
    
    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(ComponentPort port) {
	_doChangeRequest(new RemovePort(port, port));
    }
	
    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Icon icon) {
	final ComponentEntity entity = (ComponentEntity)icon.getContainer();
	_doChangeRequest(new RemoveActor(icon, entity));

    }  

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(final Vertex vertex) {
	final ComponentRelation relation =
	    (ComponentRelation)vertex.getContainer();
	_doChangeRequest(new RemoveRelation(vertex, relation));
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeHead(Object link, Object object) {
	if(link instanceof Link) {
	    setEdgeHead((Link)link,
			(NamedObj)object);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. link = " + link + 
                    "object = " + object);
	}
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeHead(final Link link, final Object head) {
	_doChangeRequest(new ChangeRequest(link, "move head of link" + link.getFullName()) {
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
	GraphEvent e = new GraphEvent(GraphEvent.EDGE_HEAD_CHANGED,
                this, link, head);
        dispatchGraphEvent(e);
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeTail(Object link, Object object) {
	if(link instanceof Link) {
	    setEdgeTail((Link)link,
			(NamedObj)object);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. link = " + link +
                    "object = " + object);
	}
    }

    /**
     * Connect an edge to the given tail node and notify listeners
     * with an EDGE_TAIL_CHANGED event.
     */
    public void setEdgeTail(final Link link, final NamedObj tail) {
	_doChangeRequest(new ChangeRequest(link, "move head of link" + link.getFullName()) {
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
	GraphEvent e = new GraphEvent(GraphEvent.EDGE_TAIL_CHANGED,
				      this, link, tail);
        dispatchGraphEvent(e);
    }

    /**
     * Set the visual object correspoding
     * to the given node, edge, or composite.
     */
    public void setVisualObject(Object o, Object visual) {
	if(o instanceof NamedObj) {
	    setVisualObject((NamedObj)o, visual);
	} else {
	    throw new InternalErrorException(
                    "Ptolemy Graph Model only handles " +
                    "named objects. object = " + o);
	}       
    }

    /**
     * Set the visual object correspoding
     * to the given node, edge, or composite.
     */
    public void setVisualObject(final NamedObj o, final Object visual) {
	_doChangeRequest(new ChangeRequest(o, 
	       "setting visual object of " + o.getFullName()) {
	    public void execute() throws ChangeFailedException {
		try {
		    FigureAttribute a = 
			new FigureAttribute(o, o.uniqueName("figure"));
		    a.setFigure(visual);
		} catch (Exception ex) {
		    throw new ChangeFailedException(this, ex.getMessage());
		}
	    }
	});
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

    // Perform the desired change request.  Queue the request with the
    // root entity.  If the change fails, then throw a graph exception. 
    public void _doChangeRequest(ChangeRequest request) {
	try {
	    _root.requestChange(request);
	} 
	catch (Exception ex) {
	    ex.printStackTrace();
	    throw new GraphException(ex);
	}
    }
    
    /**
     * The root of the graph contained by this model.
     */
    private CompositeEntity _root = null;
}

