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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.kernel.util.*;
import ptolemy.vergil.toolbox.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.moml.*;
import ptolemy.vergil.ExceptionHandler;
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
public class FSMGraphModel extends ModularGraphModel {

    /**
     * Construct a new graph model whose root is the given composite entity.
     * Create graphical representations of objects in the entity, if 
     * necessary.
     */
    public FSMGraphModel(CompositeEntity toplevel) {
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
	} else {
	    return null;
	}
    }

    /** 
     * Return the model for the given edge object.  If the object is not
     * an edge, then return null.
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
     */
    public NodeModel getNodeModel(Object node) {
	if(node instanceof Icon) {
	    return _stateModel;
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
	if(element instanceof Icon) {
	    return ((Icon)element).getContainer();
	} else if(element instanceof Arc) {
	    return ((Arc)element).getRelation();
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
	throw new UnsupportedOperationException("Ptolemy FSM Graph Model does" +
						" not allow semantic objects" +
						" to be changed");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public class ArcModel implements EdgeModel {
	/** Return true if the head of the given edge can be attached to the
	 *  given node.
	 *  @param edge The edge to attach.
	 *  @param node The node to attach to.
	 */
	public boolean acceptHead(Object edge, Object node) {
	    if (node instanceof Icon) {
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
	    if (node instanceof Icon) {
		return true;
	    } else
		return false;
	}

	/** Return the head node of the given edge.
	 *  @param edge The edge.
	 *  @return The head node.
	 */
	public Object getHead(Object edge) {
	    return ((Arc)edge).getHead();
	}

	/** Return the tail node of the specified edge.
	 *  @param edge The edge.
	 *  @return The node that is the tail of the specified edge.
	 */
	public Object getTail(Object edge) {
	    return ((Arc)edge).getTail();
	}	

	/** Return true if this edge is directed.  
	 *  All transitions are directed, so this always returns true.
	 *  @return True.
	 */
	public boolean isDirected(Object edge) {
	    return true;
	}	

	/** Connect the given edge to the given head node.
	 *  @param edge The edge.
	 *  @param head The new head for the edge.
	 */
	public void setHead(final Object edge, final Object head) {
	    _toplevel.requestChange(new ChangeRequest(
	        FSMGraphModel.this,
		"move head of link" +  ((Arc)edge).getFullName()) {
		protected void _execute() throws Exception {
		    ((Arc)edge).unlink();		
		    ((Arc)edge).setHead(head);
		    ((Arc)edge).link();
		}
	    });
	}	
	
	/** Connect the given edge to the given tail node.
	 *  @param edge The edge.
	 *  @param tail The new tail for the edge.
	 */
	public void setTail(final Object edge, final Object tail) {
	    _toplevel.requestChange(new ChangeRequest(
	        FSMGraphModel.this, 
		"move tail of link" +  ((Arc)edge).getFullName()) {
		protected void _execute() throws Exception {
		    ((Arc)edge).unlink();		
		    ((Arc)edge).setTail(tail);
		    ((Arc)edge).link();
		}
	    });
	}	
    }
    
    public class StateModel implements NodeModel {
	/**
	 * Return an iterator over the edges coming into the given node.
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
	    List linkList = _toplevel.attributeList(Arc.class);
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
	 * Return an iterator over the edges coming out of the given node.
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
	    List linkList = _toplevel.attributeList(Arc.class);
	    Iterator links = linkList.iterator();
	    while(links.hasNext()) {
		Arc link = (Arc)links.next();
		Object tail = link.getTail();
		if(tail != null && tail.equals(entity)) {
		    stateLinkList.add(link);
		}
	    }
	    
	    return stateLinkList.iterator();
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
	        FSMGraphModel.this, 
		"Set Parent of Icon " +  ((Icon)node).getFullName()) {
		protected void _execute() throws Exception {
		    ComponentEntity entity = 
                        (ComponentEntity)((Icon)node).getContainer();
		    entity.setContainer((CompositeEntity)parent);
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
	    int count = entity.entityList().size();
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
	    if(change.getOriginator() == FSMGraphModel.this) {
                return;
            }
	    // This has to happen in the swing thread, because Diva assumes
	    // that everything happens in the swing thread.  We invoke later
	    // because the changeRequest that we are listening for often
	    // occurs in the execution thread of the ptolemy model.
	    SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    // Otherwise notify any graph listeners 
		    // that the graph might have
		    // completely changed.
		    dispatchGraphEvent(new GraphEvent(
			FSMGraphModel.this, 
			GraphEvent.STRUCTURE_CHANGED, getRoot()));
		}
	    });
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

    // Check to make sure that there is a Link object representing
    // the given relation.
    private void _updateLinks(ComponentRelation relation) {
	// Find the link for this relation
	List linkList = _toplevel.attributeList(Arc.class);
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
	Port port2 = (Port)linkedPortList.get(1);
	Arc link;
	try {
	    link = new Arc(_toplevel, _toplevel.uniqueName("arc"));
	} 
	catch (Exception e) {
	    throw new InternalErrorException(
		"Failed to create " +
		"new link, even though one does not " +
		"already exist:" + e.getMessage());
	}
	link.setRelation(relation);
	// FIXME which is which?
	link.setHead(port1);
	link.setTail(port2);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The root of the graph contained by this model.
    private CompositeEntity _toplevel = null;
    private ArcModel _arcModel = new ArcModel();
    private ToplevelModel _toplevelModel = new ToplevelModel();
    private StateModel _stateModel = new StateModel();
}

