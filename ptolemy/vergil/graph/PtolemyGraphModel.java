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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.graph;

// FIXME: Trim this list and replace with explict (per class) imports.
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.gui.MessageHandler;
import ptolemy.moml.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.Token;
import ptolemy.data.ObjectToken;
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

// FIXME External ports need a location attribute.

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
<p>
This model uses a ptolemy change listener to detect changes to the model that
do not originate from this model.  These changes are propagated
as structure changed graph events to all graphListeners registered with this
model.  This mechanism allows a graph visualization of a ptolemy model to
remain synchronized with the state of a mutating model.

@author Steve Neuendorffer
@version $Id$
 */
public class PtolemyGraphModel extends AbstractPtolemyGraphModel {

    /** Construct a new graph model whose root is the given composite entity.
     *  @param toplevel The top-level composite entity for the model.
     */
    public PtolemyGraphModel(CompositeEntity toplevel) {
	super(toplevel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     * Return the model for the given composite object.  If the object is not
     * a composite, meaning that it does not contain other nodes, 
     * then return null.  
     * @param composite An object which is assumed to be a node object in
     * this graph model.
     * @return An instance of ToplevelModel if the object is the root 
     * object of this graph model or an instance of IconModel if the
     * object is an icon.  Otherwise return null.
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
     * @param edge An object which is assumed to be in this graph model.
     * @return An instance of LinkModel if the object is a Link.
     * Otherwise return null.
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
     * @param node An object which is assumed to be in this graph model.
     * @return An instance of IconModel if the object is an icon, an instance
     * of ExternalPortModel if the object is a port contained in the root of
     * this graph model, an instance of PortModel for all other ports 
     * (which are presumably contained in Icons), and an instance of
     * VertexModel for vertexes.  Otherwise return null.
     */
    public NodeModel getNodeModel(Object node) {
	if(node instanceof Icon) {
	    return _iconModel;
	} else if(node instanceof Location && 
		  ((Location)node).getContainer() instanceof Port) {
	    return _externalPortModel;
	} else if(node instanceof Port) {
	    return _portModel;
	} else if(node instanceof Vertex) {
	    return _vertexModel;
	} else {
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
     *  @return The semantic object associated with this element, or null
     *  if the object is not recognized.
     */
    public Object getSemanticObject(Object element) {
	if(element instanceof Port) {
	    return element;
	} else if(element instanceof Location && 
		  ((Location)element).getContainer() instanceof Port) {
	    return ((Location)element).getContainer();
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

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The model for ports that make external connections to this graph.
     *  These ports are always contained by the root of this graph model.
     */
    private class ExternalPortModel implements NodeModel {
	/**
	 * Return the graph parent of the given node.
	 * @param node The node, which is assumed to be a port contained in
	 * the root of this graph model.
	 * @return The root of this graph model.
	 */
	public Object getParent(Object node) {
	    return getRoot();
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
	    Location location = (Location)node;
	    ComponentPort port = (ComponentPort)location.getContainer();
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME inefficient if we are conneted to the same relation more
	    // than once.
	    List relationList = port.insideRelationList();
	    Iterator relations = relationList.iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
		_updateLinks(relation);
	    }
	    
	    // Go through all the links, creating a list of
	    // those we are connected to.
	    List portLinkList = new LinkedList();
	    List linkList = getToplevel().attributeList(Link.class);
	    Iterator links = linkList.iterator();
	    while(links.hasNext()) {
		Link link = (Link)links.next();
		Object head = link.getHead();
		if(head != null && head.equals(location)) {
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
	    Location location = (Location)node;
	    ComponentPort port = (ComponentPort)location.getContainer();
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME inefficient if we are conneted to the same relation more
	    // than once.
	    List relationList = port.insideRelationList();
	    Iterator relations = relationList.iterator();
	    while(relations.hasNext()) {
		ComponentRelation relation = 
                    (ComponentRelation)relations.next();
		_updateLinks(relation);
	    }
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List portLinkList = new LinkedList();
	    List linkList = getToplevel().attributeList(Link.class);
	    Iterator links = linkList.iterator();
	    while(links.hasNext()) {
		Link link = (Link)links.next();
		Object tail = link.getTail();
		if(tail != null && tail.equals(location)) {
		    portLinkList.add(link);
		}
	    }
	    
	    return portLinkList.iterator();
	}
	
	/**
	 * Set the graph parent of the given node.  
	 * @param node The node, which is assumed to be a port contained in
	 * the root of this graph model.
	 * @param parent The parent, which is assumed to be a composite entity.
	 */
	public void setParent(Object node, final Object parent) {
	    try {
		Location location = (Location)node;
		ComponentPort port = (ComponentPort)location.getContainer();
		port.setContainer((CompositeEntity)parent);
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}	
    }

    /** The model for an icon that contain ports.
     */
    private class IconModel implements CompositeNodeModel {
	/**
	 * Return the number of nodes contained in
	 * this graph or composite node.
	 * @param composite The composite, which is assumed to be an icon.
	 * @return The number of ports contained in the container of the icon.
	 */
	public int getNodeCount(Object composite) {
	    Icon icon = (Icon) composite;
	    return ((ComponentEntity)icon.getContainer()).portList().size();
	}
	
	/**
	 * Return the graph parent of the given node.
	 * @param node The node, which is assumed to be an icon.
	 * @return The container of the Icon's container, which should be
	 * the root of the graph.
	 */
	public Object getParent(Object node) {
	    return ((Icon)node).getContainer().getContainer();
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
	 * given graph or composite node.  This iterator
	 * does not necessarily support removal operations.
	 * @param composite The composite, which is assumed to be an icon.
	 * @return An iterator over the ports contained in the container
	 * of the icon.
	 */
	public Iterator nodes(Object composite) {
	    Icon icon = (Icon) composite;
	    ComponentEntity entity = (ComponentEntity)icon.getContainer();
	    return entity.portList().iterator();
	}

	/**
	 * Return an iterator over the edges coming out of the given node.
	 * @param node The node, which is assumed to be an icon.
	 * @return A NullIterator, since no edges are attached to icons.
	 */
	public Iterator outEdges(Object node) {
	    return new NullIterator();
	}
	
	/**
	 * Set the graph parent of the given node.  
	 * This class queues a new change request with the ptolemy model
	 * to make this modification.
	 * @param node The node, which is assumed to be an icon.
	 * @param parent The parent, which is assumed to be a composite entity.
	 */
	public void setParent(final Object node, final Object parent) {
	    try {
		ComponentEntity entity = 
		    (ComponentEntity)((Icon)node).getContainer();
		entity.setContainer((CompositeEntity)parent);
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}
    }

    /** The model for links that connect two ports, or a port and a vertex.
     */
    private class LinkModel implements EdgeModel {
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
		(node instanceof Location && 
		 ((Location)node).getContainer() instanceof Port)) {
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
		(node instanceof Location && 
		 ((Location)node).getContainer() instanceof Port)) {
		return true;
	    } else
		return false;
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

	/** Connect the given edge to the given head node.
	 *  This class queues a new change request with the ptolemy model
	 *  to make this modification.
	 *  @param edge The edge, which is assumed to be a link.
	 *  @param head The new head for the edge, which is assumed to
	 *  be a location representing a port, a port or a vertex.
	 */
	public void setHead(final Object edge, final Object head) {
	    try {
		((Link)edge).unlink();		
		((Link)edge).setHead(head);
		((Link)edge).link();
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}	
	
	/** Connect the given edge to the given tail node.
	 *  This class queues a new change request with the ptolemy model
	 *  to make this modification.
	 *  @param edge The edge, which is assumed to be a link.
	 *  @param tail The new tail for the edge, which is assumed to
	 *  be a location representing a port, a port or a vertex.
	 */
	public void setTail(final Object edge, final Object tail) {
	    try {
		((Link)edge).unlink();		
		((Link)edge).setTail(tail);
		((Link)edge).link();
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}	
    }
    
    /** The model for ports that are contained in icons in this graph.
     */
    private class PortModel implements NodeModel {
	/**
	 * Return the graph parent of the given node.
	 * @param node The node, which is assumed to be a port.
	 * @return The (presumably unique) icon contained in the port's 
	 * container.
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
	    List linkList = getToplevel().attributeList(Link.class);
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
	    List linkList = getToplevel().attributeList(Link.class);
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
	 * Set the graph parent of the given node.  
	 * This class queues a new change request with the ptolemy model
	 * to make this modification.
	 * @param node The node, which is assumed to be a port in this model.
	 * @param parent The parent, which is assumed to be a composite entity.
	 */
	public void setParent(final Object node, final Object parent) {
	    try {
		((Port)node).setContainer((CompositeEntity)parent);
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}	
    }

    /** A model for the toplevel composite of this graph model.
     */
    private class ToplevelModel implements CompositeModel {
	/**
	 * Return the number of nodes contained in
	 * this graph or composite node.
	 * @param composite The composite, which is assumed to be
	 * the root composite entity.
	 * @return The number of ports contained in the composite, plus the
	 * number of entities contained in the composite, plus the number of 
	 * vertexes contained in relations contained in the composite.
	 */
	public int getNodeCount(Object composite) {
	    // FIXME count is wrong if vertexes need ot be manufactured.
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
	 * Return an iterator over all the nodes contained in
	 * the given composite.  This method ensures that all the entities
	 * have an icon, and all the relations that don't connect exactly
	 * two ports have a vertex.
	 * @param composite The composite, which is assumed to be
	 * the root composite entity.
	 * @return An iterator containing ports, vertexes, and icons.
	 */
	public Iterator nodes(Object composite) {
	    // FIXME change request.
	    Set nodes = new HashSet();
	    Iterator entities = getToplevel().entityList().iterator();
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
	    
	    Iterator ports = getToplevel().portList().iterator();
	    while(ports.hasNext()) {
		ComponentPort port = (ComponentPort)ports.next();
		nodes.add(_getLocation(port));
	    }
	    
	    Iterator relations = getToplevel().relationList().iterator();
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

    /** The model for vertexes that are contained within the relations of the
     *  ptolemy model.
     */
    private class VertexModel implements NodeModel {
	/**
	 * Return the graph parent of the given node.
	 * @param node The node, which is assumed to be a Vertex.
	 * @return The container of the vertex's container, which is
	 * presumably the root of the graph model.
	 */
	public Object getParent(Object node) {
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
	    ComponentRelation relation = 
                (ComponentRelation)vertex.getContainer();
	    _updateLinks(relation);
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List vertexLinkList = new LinkedList();
	    List linkList = getToplevel().attributeList(Link.class);
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
	    ComponentRelation relation = 
                (ComponentRelation)vertex.getContainer();
	    _updateLinks(relation);
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List vertexLinkList = new LinkedList();
	    List linkList = getToplevel().attributeList(Link.class);
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
	 * Set the graph parent of the given node.  
	 * This class queues a new change request with the ptolemy model
	 * to make this modification.
	 * @param node The node, which is assumed to be a vertex.
	 * @param parent The parent, which is assumed to be a 
	 * component relation.
	 */
	public void setParent(final Object node, final Object parent) {
	    try {
		ComponentRelation relation =
		    (ComponentRelation)((Vertex)node).getContainer();
		relation.setContainer((CompositeEntity)parent);
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}	
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check to make sure that there is a Link object representing every
    // link connected to the given relation.  In some cases, it may
    // be necessary to create a vertex to represent the relation as well.
    private void _updateLinks(ComponentRelation relation) {
	List linkedPortList = relation.linkedPortList();
	int allPortCount = linkedPortList.size();
	
	// Go through all the links that currently exist, and remove ports
	// from the linkedPortList that already have a Link object.
	// FIXME this could get expensive 
	List linkList = getToplevel().attributeList(Link.class);
	Iterator links = linkList.iterator();
	while(links.hasNext()) {
	    Link link = (Link)links.next();
	    // only consider links that are associated with this relation.
	    if(link.getRelation() != relation) continue;
	    // remove any ports that this link is linked to.  We don't need
	    // to manufacture those links.
	    Object tail = link.getTail();
	    if(tail instanceof Location) {
		tail = ((Location)tail).getContainer();
	    }
	    if(tail != null && linkedPortList.contains(tail) ) {
		linkedPortList.remove(tail);
	    }
	    Object head = link.getHead();
	    if(head instanceof Location) {
		head = ((Location)head).getContainer();
	    }
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
	    Object head = null;
	    Object tail = null;

	    if(port1.getContainer().equals(getRoot())) {
		head = _getLocation(port1);
	    } else {
		head = port1;
	    }
		
	    if(port2.getContainer().equals(getRoot())) {
		tail = _getLocation(port2);
	    } else {
		tail = port2;
	    }

	    Link link;
	    try {
		link = new Link(getToplevel(), getToplevel().uniqueName("link"));
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
		Object head = null;
		if(port.getContainer().equals(getRoot())) {
		    head = _getLocation(port);
		} else {
		    head = port;
		}
		
		Link link;
		try {
		    link = new Link(getToplevel(), 
				    getToplevel().uniqueName("link"));
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
	
    // Return the location contained in the given port, or
    // a new location contained in the given port if there was no location.
    private Location _getLocation(Port port) {
	List locations = port.attributeList(Location.class);
	if(locations.size() > 0) {
	    return (Location)locations.get(0);
	} else {
	    try {
		Location location = new Location(port, "_location");
		return location;
	    }
	    catch (Exception e) {
		throw new InternalErrorException("Failed to create " +
		    "location, even though one does not exist:" +
		    e.getMessage());
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The models of the different types of nodes and edges.
    private LinkModel _linkModel = new LinkModel();
    private ToplevelModel _toplevelModel = new ToplevelModel();
    private IconModel _iconModel = new IconModel();
    private PortModel _portModel = new PortModel();
    private VertexModel _vertexModel = new VertexModel();
    private ExternalPortModel _externalPortModel = new ExternalPortModel();
}
