/* A graph model for ptolemy fsm models.

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
import ptolemy.vergil.graph.AbstractPtolemyGraphModel;
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

//////////////////////////////////////////////////////////////////////////
//// FSMGraphModel
/**
a graph model for graphically manipulating ptolemy FSM models.

@author Steve Neuendorffer
@version $Id$
*/
public class FSMGraphModel extends AbstractPtolemyGraphModel {

    /** Construct a new graph model whose root is the given composite entity.
     *  @param toplevel The top-level composite entity for the model.
     */
    public FSMGraphModel(CompositeEntity toplevel) {
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

    /** 
     * Return the node model for the given object.  If the object is not
     * a node, then return null.
     * @param node An object which is assumed to be in this graph model.
     * @return An instance of StateModel if the object is an icon.
     * Otherwise return null.
     */
    public NodeModel getNodeModel(Object node) {
	if(node instanceof Icon) {
	    return _stateModel;
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
	if(element instanceof Icon) {
	    return ((Icon)element).getContainer();
	} else if(element instanceof Arc) {
	    return ((Arc)element).getRelation();
	} else {
	    return null;
	}       
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The model for arcs between states.
     */
    public class ArcModel implements EdgeModel {
	/** Return true if the head of the given edge can be attached to the
	 *  given node.
	 *  @param edge The edge to attach, which is assumed to be an arc.
	 *  @param node The node to attach to.
	 *  @return True if the node is an icon.
	 */
	public boolean acceptHead(Object edge, Object node) {
	    if (node instanceof Icon) {
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
	    if (node instanceof Icon) {
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

	/** Connect the given edge to the given head node.
	 *  This class queues a new change request with the ptolemy model
	 *  to make this modification.
	 *  @param edge The edge, which is assumed to be an arc.
	 *  @param head The new head for the edge, which is assumed to
	 *  be an icon.
	 */
	public void setHead(final Object edge, final Object head) {
	    try {
		((Arc)edge).unlink();		
		((Arc)edge).setHead(head);
		((Arc)edge).link();
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}
	
	/** Connect the given edge to the given tail node.
	 *  This class queues a new change request with the ptolemy model
	 *  to make this modification.
	 *  @param edge The edge, which is assumed to be an arc.
	 *  @param head The new head for the edge, which is assumed to
	 *  be an icon.
	 */
	public void setTail(final Object edge, final Object tail) {
	    try {
		((Arc)edge).unlink();		
		((Arc)edge).setTail(tail);
		((Arc)edge).link();
	    } catch (Exception ex) {
		throw new GraphException(ex);
	    }
	}	
    }
    
    /** The model for an icon that represent states.
     */
    public class StateModel implements NodeModel {
	/**
	 * Return the graph parent of the given node.
	 * @param node The node, which is assumed to be an icon contained in
	 * this graph model.
	 * @return The container of the icon's container, which should
	 * be the root of this graph model.
	 */
	public Object getParent(Object node) {
	    return ((Icon)node).getContainer().getContainer();
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
	    Icon icon = (Icon)node;
	    Entity entity = (Entity)icon.getContainer();
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME could be more efficient.
	    Iterator ports = entity.portList().iterator();
	    while(ports.hasNext()) {
		ComponentPort port = (ComponentPort)ports.next();
		List relationList = port.linkedRelationList();
		Iterator relations = relationList.iterator();
		while(relations.hasNext()) {
		    ComponentRelation relation = 
			(ComponentRelation)relations.next();
		    _updateLinks(relation);
		}
	    }
	    
	    // Go through all the links, creating a list of
	    // those we are connected to.
	    List stateLinkList = new LinkedList();
	    List linkList = getToplevel().attributeList(Arc.class);
	    Iterator links = linkList.iterator();
	    while(links.hasNext()) {
		Arc link = (Arc)links.next();
		Object head = link.getHead();
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
	    Icon icon = (Icon)node;
	    Entity entity = (Entity)icon.getContainer();
	    // make sure that the links to relations that we are connected to
	    // are up to date.
	    // FIXME could be more efficient.
	    Iterator ports = entity.portList().iterator();
	    while(ports.hasNext()) {
		ComponentPort port = (ComponentPort)ports.next();
		List relationList = port.linkedRelationList();
		Iterator relations = relationList.iterator();
		while(relations.hasNext()) {
		    ComponentRelation relation = 
			(ComponentRelation)relations.next();
		    _updateLinks(relation);
		}
	    }
	    
	    // Go through all the links, creating a list of 
	    // those we are connected to.
	    List stateLinkList = new LinkedList();
	    List linkList = getToplevel().attributeList(Arc.class);
	    Iterator links = linkList.iterator();
	    while(links.hasNext()) {
		Arc link = (Arc)links.next();
		Object tail = link.getTail();
		if(tail != null && tail.equals(icon)) {
		    stateLinkList.add(link);
		}
	    }
	    
	    return stateLinkList.iterator();
	}
	
	/**
	 * Set the graph parent of the given node.  
	 * This class queues a new change request with the ptolemy model
	 * to make this modification.
	 * @param node The node, which is assumed to be an icon contained in
	 * this graph model.
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
	    
	    return nodes.iterator();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Check to make sure that there is a Link object representing
    // the given relation.
    private void _updateLinks(ComponentRelation relation) {
	// Find the link for this relation
	List linkList = getToplevel().attributeList(Arc.class);
	Iterator links = linkList.iterator();
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
	    throw new GraphException("A transition was found connecting more "
				     + "thwn two states.");
	}
	Port port1 = (Port)linkedPortList.get(0);
	Icon icon1 = _getIcon(port1);
	Port port2 = (Port)linkedPortList.get(1);
	Icon icon2 = _getIcon(port2);

	Arc link;
	try {
	    link = new Arc(getToplevel(), getToplevel().uniqueName("arc"));
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
	    link.setHead(icon1);
	    link.setTail(icon2);
	} else {
	    link.setHead(icon2);
	    link.setTail(icon1);
	}
    }

    // Return the icon for the state that contains the given port.
    public Icon _getIcon(Port port) {
	Entity entity = (Entity)port.getContainer();
	List iconList = entity.attributeList(Icon.class);
	if(iconList.size() > 0) {
	    return (Icon)iconList.get(0);
	} else {
	    throw new InternalErrorException(
		"Found an entity that does not contain an icon.");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The models of the different types of nodes and edges.
    private ArcModel _arcModel = new ArcModel();
    private ToplevelModel _toplevelModel = new ToplevelModel();
    private StateModel _stateModel = new StateModel();
}

