
package ptolemy.vergil.ptolemy.fsm;

import ptolemy.kernel.util.*;
import ptolemy.vergil.toolbox.*;

import ptolemy.vergil.graph.FigureAttribute;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import diva.graph.*;
import diva.graph.toolbox.*;
import diva.util.*;
import java.util.*;

public class FSMGraphModel extends AbstractGraphModel 
    implements MutableGraphModel {
    
    /**
     * Construct an empty graph model whose
     * root is a new CompositeEntity.
     */
    public FSMGraphModel() {
	_root = new CompositeEntity();
	try {
	    new PtolemyFSMNotation(_root, "notation");
	} catch (Exception ex) {
	    throw new InternalErrorException("Couldn't create FSM notation");
	}
    }

    /**
     * Construct a new graph model whose root is the given composite entity.
     * Create graphical representations of objects in the entity, if 
     * necessary.
     */
    public FSMGraphModel(CompositeEntity toplevel) {
	_root = toplevel;

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
		Arc link;
		try {
		    link = new Arc(toplevel, toplevel.uniqueName("link"));
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
		throw new RuntimeException("Found a relation without two" +
					   " links! " + relation.exportMoML());
	    }
	}
    }
    	
    /**
     * Return true if the head of the given edge can be attached to the
     * given node.
     */
    public boolean acceptHead(Object edge, Object node) {
	if (edge instanceof Arc &&
	    node instanceof Icon) {
	    return true;
	} else
	    return false;
    }

    /**
     * Return true if the tail of the given edge can be attached to the
     * given node.
     */
    public boolean acceptTail(Object edge, Object node) {
	if (edge instanceof Arc &&
	    node instanceof Icon) {
	    return true;
	} else
	    return false;
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, Object node, Object parent) {
	 if (node instanceof Icon &&
		   parent instanceof CompositeEntity) {
	    addNode(eventSource, (Icon)node, (CompositeEntity)parent);
	} else if (node instanceof Port &&
		   parent instanceof CompositeEntity) {
	    addNode(eventSource, (ComponentPort)node, (CompositeEntity)parent);
	} else {
	    throw new RuntimeException("FSMGraphModel only handles " +
				       "named objects. node = " + node + 
				       "parent = " + parent);
	}
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, Icon icon, CompositeEntity parent) {
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	try {
	    entity.setContainer(parent);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex.getMessage());
	}
        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_ADDED,
                icon, parent);
        dispatchGraphEvent(e);
    }

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event. <p>
     */
    public void addNode(Object eventSource, ComponentPort port, CompositeEntity parent) {
	try {
	    port.setContainer(parent);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex.getMessage());
	}
        GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_ADDED,
                port, parent);
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
	if(link instanceof Arc) {
	    if(tail instanceof Icon &&
	       head instanceof Icon) {
		connectEdge(eventSource, 
			    (Arc)link, 
			    (Icon)tail,
			    (Icon)head);
	    }
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
				       "named objects. link = " + link +
				       "tail = " + tail + " head = " + head);
	}
    }

    public void connectEdge(Object eventSource, 
			    Arc link, 
			    Icon tail,
			    Icon head) {
	setEdgeTail(eventSource, link, tail);
	setEdgeHead(eventSource, link, head);
    }

    /**
     * Return true if this composite node contains the given node.
     */
    public boolean containsNode(Object composite, Object node) {
        return composite.equals(getParent(node));
    }

    /**
     * Disconnect an edge from its two enpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event.
     */
    public void disconnectEdge(Object eventSource, Object edge) {
	if(edge instanceof Arc) {
	    disconnectEdge(eventSource, (Arc) edge);
	} else {
	    throw new RuntimeException("FSMGraphModel only handles " +
				       "named objects. edge = " + edge);
	}
    }

    /**
     * Disconnect an edge from its two enpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event.
     */
    public void disconnectEdge(Object eventSource, Arc edge) {
	/*  FIXME
	    Edge edgePeer = (Edge)edge;
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
	if(link instanceof Arc) {
	    return getHead((Arc)link);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
				       "named objects. link = " + link);
	}       
    }
		
    /**
     * Return the head node of the given edge.
     */
    public Object getHead(Arc link) {
	return link.getHead();
    }
		
    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    public int getNodeCount(Object composite) {
	if(composite instanceof CompositeEntity) {
	    return getNodeCount((CompositeEntity)composite);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
				       "named objects. composite = " + 
				       composite);
	}
    }
	
    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    public int getNodeCount(CompositeEntity entity) {
	int count = entity.entityList().size();
	return count;
    }
	
    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
     */
    public Object getParent(Object node) {	
	if(node instanceof Icon) {
	    return getParent((Icon)node);
	} else if(node instanceof ComponentPort) {
	    return getParent((ComponentPort)node);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
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
    public Object getParent(ComponentPort port) {	
        ComponentEntity entity = (ComponentEntity)port.getContainer();
	return entity;
    }

    /**
     * Return the tail node of this edge.
     */
    public Object getTail(Object link) {
	if(link instanceof Arc) {
	    return getTail((Arc)link);
	} else {
	    throw new RuntimeException("FSMGraphModel only handles " +
				       "named objects. link =" + link);
	}       
    }

    /**
     * Return the tail node of this edge.
     */
    public Object getTail(Arc link) {
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
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
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
	} else if(o instanceof Icon) {
	    return getSemanticObject((Icon)o);
	} else if(o instanceof Arc) {
	    return getSemanticObject((Arc)o);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
				       "named objects. object= " + o);
	}       
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
    public Object getSemanticObject(Arc link) {
	return link.getRelation();
    }
    
    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges(Object node) {
	if(node instanceof ComponentPort || node instanceof Icon) {
	    return inEdges((NamedObj) node);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
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
	List linkList = _root.attributeList(Arc.class);
	// filter out the links that aren't attached to this port.
	List portLinks = new LinkedList();
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Arc link = (Arc)links.next();
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
        return false;
    }

    /**
     * Return true if the given object is a 
     * edge in this model.
     */
    public boolean isEdge(Object o) {
        return (o != null) && (o instanceof Arc);
    }

    /**
     * Return true if the given object is a 
     * node in this model.
     */
    public boolean isNode(Object o) {
        return (o != null) && 
	    ((o instanceof Icon) || 
	     (o instanceof ComponentPort));
    }

    /**
     * Provide an iterator over the nodes in the
     * given graph or composite node.  This iterator
     * does not necessarily support removal operations.
     */
    public Iterator nodes(Object object) {
	if(object instanceof CompositeEntity) {
	    return nodes((CompositeEntity)object);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
				       "named objects. icon = " + object);
	}       
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

	return nodes.iterator();
    }


    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges(Object node) {
	if(node instanceof Port || node instanceof Icon) {
	    return outEdges((NamedObj)node);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
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
	List linkList = _root.attributeList(Arc.class);
	// filter out the links that aren't attached to this port.
	List portLinks = new LinkedList();
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Arc link = (Arc)links.next();
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
    public void removeNode(Object eventSource, Object node) {
	Object parent = getParent(node);
	if(node instanceof Icon) {
	    removeNode(eventSource, (Icon)node);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
				       "named objects. node = " + node);
	}
	GraphEvent e = new GraphEvent(eventSource, GraphEvent.NODE_REMOVED,
				      node, parent);
	dispatchGraphEvent(e);
    }

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     */
    public void removeNode(Object eventSource, Icon icon) {
	ComponentEntity entity = (ComponentEntity)icon.getContainer();
	try {
	    Iterator ports = entity.portList().iterator();
	    while(ports.hasNext()) {
		Port port = (Port) ports.next();
		port.unlinkAll();
	    }
	    entity.setContainer(null);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex.getMessage());
	}
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeHead(Object eventSource, Object link, Object object) {
	if(link instanceof Arc) {
	    setEdgeHead(eventSource, (Arc)link,
			(NamedObj)object);
	    } else {
		throw new RuntimeException("FSMGraphModel only handles " +
					   "named objects. link = " + link + 
					   "object = " + object);
	}
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeHead(Object eventSource, Arc link, Object head) {
	link.unlink();
	link.setHead(head);
	link.link();
        GraphEvent e = 
	    new GraphEvent(eventSource, GraphEvent.EDGE_HEAD_CHANGED,
			   link, head);
        dispatchGraphEvent(e);
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     */
    public void setEdgeTail(Object eventSource, Object link, Object object) {
	if(link instanceof Arc) {
	    setEdgeTail(eventSource, (Arc)link,
			(NamedObj)object);
	} else {
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
				       "named objects. link = " + link +
				       "object = " + object);
	}
    }

    /**
     * Connect an edge to the given tail node and notify listeners
     * with an EDGE_TAIL_CHANGED event.
     */
    public void setEdgeTail(Object eventSource, Arc link, NamedObj tail) {
	link.unlink();
	link.setTail(tail);
	link.link();
        GraphEvent e = 
	    new GraphEvent(eventSource, GraphEvent.EDGE_TAIL_CHANGED,
			   link, tail);
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
	    throw new RuntimeException("Ptolemy Graph Model only handles " +
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
	    throw new RuntimeException(ex.getMessage());
	}
    }

    /**
     * Set the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public void setSemanticObject(Object o, Object sem) {
	throw new RuntimeException("FSMGraphModel does not support" + 
				   " setting semantic objects.");
    }
    
    /**
     * The root of the graph contained by this model.
     */
    private CompositeEntity _root = null;
}

