
package ptolemy.vergil.graph;

import ptolemy.kernel.util.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import diva.graph.*;
import diva.graph.toolbox.*;
import diva.util.*;
import java.util.*;

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
     * Create graphical representations of objects in the entity, if 
     * necessary.
     */
    public PtolemyGraphModel(CompositeEntity toplevel) {
	_root = toplevel;

	Iterator entities = toplevel.entityList().iterator();
	while(entities.hasNext()) {
	    Entity entity = (Entity)entities.next();
	    Icon icon = (Icon)entity.getAttribute("_icon");
	    if(icon == null) {
		// FIXME this is pretty minimal
		try {
		    icon = new EditorIcon(entity);
		}
		catch (Exception e) {
		    throw new InternalErrorException("Failed to create " +
			"icon, even though one does not exist:" +
						     e.getMessage());
		}
	    }
	}
	
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
	    relation.setContainer(parent);
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
	    entity.setContainer(parent);
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
	    port.setContainer(parent);
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
    public void disconnectEdge(Link edge) {
	/*  Edge edgePeer = (Edge)edge;
        Node headPeer = edgePeer.getHead();
        Node tailPeer = edgePeer.getTail();
        if(headPeer != null) {
            headPeer.removeInEdge(edgePeer);
            GraphEvent e = new GraphEvent(GraphEvent.EDGE_HEAD_CHANGED,
                    this, edgePeer, headPeer);
            dispatchGraphEvent(e);
        }
        if(tailPeer != null) {
            tailPeer.removeOutEdge(edgePeer);
            GraphEvent e = new GraphEvent(GraphEvent.EDGE_TAIL_CHANGED,
                    this, edgePeer, tailPeer);
            dispatchGraphEvent(e);
        }
	*/
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
	try {
	    port.unlinkAll();
	    port.setContainer(null);
	} catch (Exception ex) {
            throw new GraphException(ex);
	}
    }
	
    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Icon icon) {
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	try {
	    Iterator ports = entity.portList().iterator();
	    while(ports.hasNext()) {
		Port port = (Port) ports.next();
		port.unlinkAll();
	    }
	    entity.setContainer(null);
	} catch (Exception ex) {
	    throw new GraphException(ex);
	}
    }  

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Vertex vertex) {
	ComponentRelation relation =
	    (ComponentRelation)vertex.getContainer();
	try {
	    relation.unlinkAll();
	    relation.setContainer(null);
	} catch (Exception ex) {
	    throw new GraphException(ex);
	}
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
    public void setEdgeHead(Link link, Object head) {
        try {
            link.unlink();
        } catch (Exception ex) {
            throw new GraphException(ex);
        }
        
        link.setHead(head);
        try {
            link.link();
        } catch (Exception ex) {
            throw new GraphException(ex);
        }
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
    public void setEdgeTail(Link link, NamedObj tail) {
        try {
            link.unlink();
        } catch (Exception ex) {
            throw new GraphException(ex);
        }
	link.setTail(tail);
        try {
            link.link();
        } catch (Exception ex) {
            throw new GraphException(ex);
        }
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
    public void setVisualObject(NamedObj o, Object visual) {
	try {
	    FigureAttribute a = 
		new FigureAttribute(o, o.uniqueName("figure"));
	    a.setFigure(visual);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new GraphException(ex.getMessage());
	}
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
    
    /**
     * The root of the graph contained by this model.
     */
    private CompositeEntity _root = null;
}

