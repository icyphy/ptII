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
import ptolemy.moml.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.Token;
import ptolemy.data.ObjectToken;
import ptolemy.vergil.ExceptionHandler;
import ptolemy.vergil.toolbox.EditorIcon;

import diva.graph.AbstractGraphModel;
import diva.graph.GraphEvent;
import diva.graph.GraphException;
import diva.graph.MutableGraphModel;
import diva.graph.toolbox.*;
import diva.graph.modular.ModularGraphModel;
import diva.graph.modular.CompositeModel;
import diva.graph.modular.NodeModel;
import diva.graph.modular.EdgeModel;
import diva.graph.modular.CompositeNodeModel;
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
public class PtolemyGraphModel extends ModularGraphModel {

    /** Construct a new graph model whose root is the given composite entity.
     *  @param toplevel The top-level composite entity for the model.
     */
    public PtolemyGraphModel(CompositeEntity toplevel) {
	super(toplevel);
	_toplevel = toplevel;
	toplevel.addChangeListener(new GraphChangeListener());
    }

    /** 
     * Return the model for the given composite object.  If the object is not
     * a composite, meaning that it does not contain other nodes, 
     * then return null.
     */
    public CompositeModel getCompositeModel(Object composite) {
	if(composite.equals(getRoot())) {
	    return _toplevelModel;
	} else if(composite instanceof Icon) {
	    return _iconModel;
	} else {
	    return null;
	}
    }

    /** 
     * Return the model for the given edge object.  If the object is not
     * an edge, then return null.
     */
    public EdgeModel getEdgeModel(Object edge) {
	if(edge instanceof Link) {
	    return _linkModel;
	} else {
	    return null;
	}
    }

    /** 
     * Return the node model for the given object.  If the object is not
     * a node, then return null.
     */
    public NodeModel getNodeModel(Object node) {
	if(node instanceof Icon) {
	    return _iconModel;
	} else if(node instanceof Port && 
		  ((Port)node).getContainer().equals(getRoot())) {
	    return _externalPortModel;
	} else if(node instanceof Port) {
	    return _portModel;
	} else if(node instanceof Vertex) {
	    return _vertexModel;
	} else {
	    return null;
	}
    }

    /**
     * Return the property of the object associated with
     * the given property name.
     */
    public Object getProperty(Object o, String propertyName) {
	try {
	    NamedObj object = (NamedObj)o;
	    Attribute a = object.getAttribute(propertyName);
	    Token t = ((Variable)a).getToken();
	    if(t instanceof ObjectToken) {
		return ((ObjectToken)t).getValue();
	    } else {
		return t.toString();
	    }
	} catch (Exception ex) {
	    return null;
	}
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
	    return ((Vertex)element).getContainer();
	} else if(element instanceof Icon) {
	    return ((Icon)element).getContainer();
	} else if(element instanceof Link) {
	    return ((Link)element).getRelation();
	} else {
	    return null;
	}       
    }

    /**
     * Set the property of the object associated with
     * the given property name.
     */
    public void setProperty(Object o, String propertyName, Object value) {
	try {
	    NamedObj object = (NamedObj)o;
	    Attribute a = object.getAttribute(propertyName);
	    if(a == null) {
		a = new Variable(object, propertyName);
	    } 
	    Variable v = (Variable)a;
	    if(value instanceof String) {
		v.setExpression((String)value);
		v.getToken();
	    } else {
		v.setToken(new ObjectToken(value));
	    }
	}
	catch (Exception ex) {
	    // Ignore any errors, which will just result in the property
	    // not being set.
	}
    }
    
    /**
     * Set the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public void setSemanticObject(Object o, Object sem) {
	throw new UnsupportedOperationException("Ptolemy Graph Model does" +
						" not allow semantic objects" +
						" to be changed");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public class LinkModel implements EdgeModel {
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

	/** Return the head node of the given edge.
	 *  @param edge The edge.
	 *  @return The head node.
	 */
	public Object getHead(Object edge) {
	    return ((Link)edge).getHead();
	}

	/** Return the tail node of the specified edge.
	 *  @param edge The edge.
	 *  @return The node that is the tail of the specified edge.
	 */
	public Object getTail(Object edge) {
	    return ((Link)edge).getTail();
	}	

	/** Return true if this edge is directed.  
	 * In this model, none of edges
	 *  are directed, so this always returns false.
	 *  @return False.
	 */
	public boolean isDirected(Object edge) {
	    return false;
	}	

	/** Connect the given edge to the given head node.
	 *  @param edge The edge.
	 *  @param head The new head for the edge.
	 */
	public void setHead(final Object edge, final Object head) {
	    _toplevel.requestChange(new ChangeRequest(
	        PtolemyGraphModel.this,
		"move head of link" +  ((Link)edge).getFullName()) {
		protected void _execute() throws Exception {
		    ((Link)edge).unlink();		
		    ((Link)edge).setHead(head);
		    ((Link)edge).link();
		}
	    });
	}	
	
	/** Connect the given edge to the given tail node.
	 *  @param edge The edge.
	 *  @param tail The new tail for the edge.
	 */
	public void setTail(final Object edge, final Object tail) {
	    _toplevel.requestChange(new ChangeRequest(
	        PtolemyGraphModel.this, 
		"move tail of link" +  ((Link)edge).getFullName()) {
		protected void _execute() throws Exception {
		    ((Link)edge).unlink();		
		    ((Link)edge).setTail(tail);
		    ((Link)edge).link();
		}
	    });
	}	
    }
    
    public class IconModel implements CompositeNodeModel {
	/**
	 * Return an iterator over the edges coming into the given node.
	 */
	public Iterator inEdges(Object node) {
	    return new NullIterator();
	}
	
	/**
	 * Return an iterator over the edges coming out of the given node.
	 */
	public Iterator outEdges(Object node) {
	    return new NullIterator();
	}
	
	/**
	 * Return the graph parent of the given node.
	 */
	public Object getParent(Object node) {
	    return ((Icon)node).getContainer().getContainer();
	}
	
	/**
	 * Set the graph parent of the given node.  Implementors of this method
	 * are also responsible for insuring that it is set properly as
	 * the child of the graph in the graph.
	 */
	public void setParent(final Object node, final Object parent) {
	    _toplevel.requestChange(new ChangeRequest(
	        PtolemyGraphModel.this, 
		"Set Parent of Icon " +  ((Icon)node).getFullName()) {
		protected void _execute() throws Exception {
		    ComponentEntity entity = 
                        (ComponentEntity)((Icon)node).getContainer();
		    entity.setContainer((CompositeEntity)parent);
		}
	    });
	}

	/**
	 * Return the number of nodes contained in
	 * this graph or composite node.
	 */
	public int getNodeCount(Object composite) {
	    Icon icon = (Icon) composite;
	    return ((ComponentEntity)icon.getContainer()).portList().size();
	}
	
	/**
	 * Provide an iterator over the nodes in the
	 * given graph or composite node.  This iterator
	 * does not necessarily support removal operations.
	 */
	public Iterator nodes(Object composite) {
	    Icon icon = (Icon) composite;
	    ComponentEntity entity = (ComponentEntity)icon.getContainer();
	    return entity.portList().iterator();
	}
    }

    public class ExternalPortModel implements NodeModel {
	/**
	 * Return an iterator over the edges coming into the given node.
	 */
       	public Iterator inEdges(Object node) {
	    ComponentPort port = (ComponentPort)node;
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME inefficient if we are conneted to the same relation more
	    // than once.
	    List relationList = port.linkedRelationList();
	    Iterator relations = relationList.iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
		_updateLinks(relation);
	    }
	    
	    // Go through all the links, creating a list of
	    // those we are connected to.
	    List portLinkList = new LinkedList();
	    List linkList = _toplevel.attributeList(Link.class);
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
	
	/**
	 * Return an iterator over the edges coming out of the given node.
	 */
	public Iterator outEdges(Object node) {
	    ComponentPort port = (ComponentPort)node;
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME inefficient if we are conneted to the same relation more
	    // than once.
	    List relationList = port.linkedRelationList();
	    Iterator relations = relationList.iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
		_updateLinks(relation);
	    }
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List portLinkList = new LinkedList();
	    List linkList = _toplevel.attributeList(Link.class);
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
	
	/**
	 * Return the graph parent of the given node.
	 */
	public Object getParent(Object node) {
	    return getRoot();
	}
	
	/**
	 * Set the graph parent of the given node.  Implementors of this method
	 * are also responsible for insuring that it is set properly as
	 * the child of the graph in the graph.
	 */
	public void setParent(final Object node, final Object parent) {
	    _toplevel.requestChange(new ChangeRequest(
	        PtolemyGraphModel.this, 
		"Set Parent of external port " +  ((Port)node).getFullName()) {
		protected void _execute() throws Exception {
		    ((Port)node).setContainer((CompositeEntity)parent);
		}
	    });
	}	
    }

    public class PortModel implements NodeModel {
	/**
	 * Return an iterator over the edges coming into the given node.
	 */
       	public Iterator inEdges(Object node) {
	    ComponentPort port = (ComponentPort)node;
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME inefficient if we are conneted to the same relation more
	    // than once.
	    List relationList = port.linkedRelationList();
	    Iterator relations = relationList.iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
		_updateLinks(relation);
	    }
	    
	    // Go through all the links, creating a list of
	    // those we are connected to.
	    List portLinkList = new LinkedList();
	    List linkList = _toplevel.attributeList(Link.class);
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
	
	/**
	 * Return an iterator over the edges coming out of the given node.
	 */
	public Iterator outEdges(Object node) {
	    ComponentPort port = (ComponentPort)node;
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME inefficient if we are conneted to the same relation more
	    // than once.
	    List relationList = port.linkedRelationList();
	    Iterator relations = relationList.iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
		_updateLinks(relation);
	    }
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List portLinkList = new LinkedList();
	    List linkList = _toplevel.attributeList(Link.class);
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
	
	/**
	 * Return the graph parent of the given node.
	 */
	public Object getParent(Object node) {
	    ComponentPort port = (ComponentPort)node;
	    Entity entity = (Entity)port.getContainer();
	    List iconList = entity.attributeList(Icon.class);
	    if(iconList.size() > 0) {
		return iconList.get(0);
	    } else {
		throw new InternalErrorException(
		    "Found an entity that does not contain an icon.");
	    }
	}
	
	/**
	 * Set the graph parent of the given node.  Implementors of this method
	 * are also responsible for insuring that it is set properly as
	 * the child of the graph in the graph.
	 */
	public void setParent(final Object node, final Object parent) {
	    _toplevel.requestChange(new ChangeRequest(
	        PtolemyGraphModel.this, 
		"Set Parent of port " +  ((Port)node).getFullName()) {
		protected void _execute() throws Exception {
		    ((Port)node).setContainer((CompositeEntity)parent);
		}
	    });
	}	
    }

    public class VertexModel implements NodeModel {
	/**
	 * Return an iterator over the edges coming into the given node.
	 */
       	public Iterator inEdges(Object node) {
	    Vertex vertex = (Vertex) node;
	    ComponentRelation relation = 
                (ComponentRelation)vertex.getContainer();
	    _updateLinks(relation);
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List vertexLinkList = new LinkedList();
	    List linkList = _toplevel.attributeList(Link.class);
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
	
	/**
	 * Return an iterator over the edges coming out of the given node.
	 */
	public Iterator outEdges(Object node) {
	    Vertex vertex = (Vertex) node;
	    ComponentRelation relation = 
                (ComponentRelation)vertex.getContainer();
	    _updateLinks(relation);
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List vertexLinkList = new LinkedList();
	    List linkList = _toplevel.attributeList(Link.class);
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
	
	/**
	 * Return the graph parent of the given node.
	 */
	public Object getParent(Object node) {
	    return ((Vertex)node).getContainer().getContainer();
	}
	
	/**
	 * Set the graph parent of the given node.  Implementors of this method
	 * are also responsible for insuring that it is set properly as
	 * the child of the graph in the graph.
	 */
	public void setParent(final Object node, final Object parent) {
	    _toplevel.requestChange(new ChangeRequest(
	        PtolemyGraphModel.this, 
		"Set Parent of vertex " +  ((Vertex)node).getFullName()) {
		protected void _execute() throws Exception {
		    ComponentRelation relation =
                        (ComponentRelation)((Vertex)node).getContainer();
		    relation.setContainer((CompositeEntity)parent);
		}
	    });
	}	
    }

    public class ToplevelModel implements CompositeModel {
	/**
	 * Return the number of nodes contained in
	 * this graph or composite node.
	 */
	public int getNodeCount(Object composite) {
	    CompositeEntity entity = (CompositeEntity)composite;
	    int count = entity.entityList().size() + entity.portList().size();
	    Iterator relations = entity.relationList().iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
		count += relation.attributeList(Vertex.class).size();
	    }
	    return count;
	}
	
	/**
	 * Provide an iterator over the nodes in the
	 * given graph or composite node.  This iterator
	 * does not necessarily support removal operations.
	 */
	public Iterator nodes(Object composite) {
	    Set nodes = new HashSet();
	    Iterator entities = _toplevel.entityList().iterator();
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
	    
	    Iterator ports = _toplevel.portList().iterator();
	    while(ports.hasNext()) {
		ComponentPort port = (ComponentPort)ports.next();
		nodes.add(port);
	    }
	    
	    Iterator relations = _toplevel.relationList().iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
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
	    // FIXME this causes a threading bug apparently.
//	    SwingUtilities.invokeLater(new Runnable() {
	//	public void run() {
		    // Otherwise notify any graph listeners 
		    // that the graph might have
		    // completely changed.
		    dispatchGraphEvent(new GraphEvent(
			PtolemyGraphModel.this, 
			GraphEvent.STRUCTURE_CHANGED, getRoot()));
//		}
//	    });
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
	List linkList = _toplevel.attributeList(Link.class);
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
		link = new Link(_toplevel, _toplevel.uniqueName("link"));
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
		    link = new Link(_toplevel, 
				    _toplevel.uniqueName("link"));
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

    private CompositeEntity _toplevel;
    private LinkModel _linkModel = new LinkModel();
    private ToplevelModel _toplevelModel = new ToplevelModel();
    private IconModel _iconModel = new IconModel();
    private PortModel _portModel = new PortModel();
    private VertexModel _vertexModel = new VertexModel();
    private ExternalPortModel _externalPortModel = new ExternalPortModel();
}
