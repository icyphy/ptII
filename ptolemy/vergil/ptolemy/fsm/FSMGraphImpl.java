/* The graph implementation for ptolemy graphs.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.domains.fsm.kernel.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import diva.graph.model.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// FSMGraphImpl
/**
The graph implementation for ptolemy graphs.  This class represents the
ptolemy clustered graphs within basic graphs of the diva.graph.model
package.  Currently, only flat graphs are supported.
@author Steve Neuendorffer
@version $Id$
*/
public class FSMGraphImpl extends BasicGraphImpl {
    /**
     * Add a node to the given graph.
     */
    public void addNode(Node n, Graph parent) {
	super.addNode(n, parent);
	CompositeEntity container =
	    (CompositeEntity)parent.getSemanticObject();
	NamedObj object = (NamedObj)n.getSemanticObject();
	if((object == null) || (container == null))
	    return;
        if(object instanceof Icon) {
	    Icon icon = (Icon)object;
	    ComponentEntity entity = (ComponentEntity)icon.getContainer();
	    try {
		entity.setContainer(container);
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new GraphException(ex.getMessage());
	    }
	}
    }

    /**
     * Remove a node from the graph that contains it.
     */
    public void removeNode(Node n) {
        super.removeNode(n);
	NamedObj object = (NamedObj)n.getSemanticObject();
	if((object == null))
	    return;
	if(object instanceof Icon) {
	    Icon icon = (Icon)object;
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
		throw new GraphException(ex.getMessage());
	    }
	} else {
            throw new GraphException("removing unknown object:" + object);
        }
    }


    /**
     * Return a new instance of a BasicCompositeNode with the given
     * semantic object.
     */
    public CompositeNode createCompositeNode(Object semanticObject) {
	BasicCompositeNode n =
	    (BasicCompositeNode) super.createCompositeNode(semanticObject);
	if(semanticObject instanceof Icon) {
	    Icon icon = (Icon) semanticObject;
	    Entity entity = (Entity) icon.getContainer();
	    if(entity == null)
		throw new GraphException("Icon must be contained " +
                        "in an entity");	  
	}
        return n;
    }

    /**
     * Return a new instance of a BasicGraph with the given semantic
     * object.
     */
    public Graph createGraph(Object semanticObject) {
	Graph g = new BasicGraph();
        g.setSemanticObject(semanticObject);
	if(semanticObject instanceof CompositeEntity) {
	    CompositeEntity toplevel = (CompositeEntity) semanticObject;
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
		addNode(createCompositeNode(icon), g);
	    }

	    Iterator relations = toplevel.relationList().iterator();
	    while(relations.hasNext()) {
		Relation relation = (Relation)relations.next();
		
		// Count the linked ports.
		int count = 0;
		Enumeration links = relation.linkedPorts();
		while(links.hasMoreElements()) {
		    links.nextElement();
		    count++;
		}
		   		
		// If there are no verticies, and the relation has
		// two connections, then create a direct link.
                /*		if(rootVertex == null && count == 2) {
		    links = relation.linkedPorts();
		    Port port1 = (Port)links.nextElement();
		    Port port2 = (Port)links.nextElement();
		    Node node1 = _findNode(g, port1);
		    Node node2 = _findNode(g, port2);
		    Edge newEdge = createEdge(relation);
		    super.setEdgeHead(newEdge, node1);
                    super.setEdgeTail(newEdge, node2);
		} else {		  
		   throw new GraphException("FSM only allows binary transitions.");
                   }*/
	    }
	}
        return g;
    }

    /**
     * Set the edge's head to the given node.
     */
    public void setEdgeHead(Edge e, Node head) {
  	Node currentHead = (BasicNode)e.getHead();
	Node currentTail = (BasicNode)e.getTail();
	if(currentHead != null) {
	    _findObjectsAndUnlink(e, currentHead, currentTail);
	}
	if((currentHead != null || currentTail != null) && head != null) {
	    _findObjectsAndLink(e, head, currentTail);
	}
	super.setEdgeHead(e, head);
    }

    /**
     * Set the edge's tail to the given node.
     */
    public void setEdgeTail(Edge e, Node tail) {
   	Node currentHead = (BasicNode)e.getHead();
	Node currentTail = (BasicNode)e.getTail();
	if(currentTail != null) {
	    _findObjectsAndUnlink(e, currentHead, currentTail);
	}
	if((currentHead != null || currentTail != null) && tail != null) {
	    _findObjectsAndLink(e, currentHead, tail);
	}
	super.setEdgeTail(e, tail);
    }

    private void _findObjectsAndUnlink(Edge e, Node head, Node tail) {
	Port port;
	ComponentRelation relation;
	Vertex vertex;       
        if(head == null || tail == null) return;
        Object headObject = head.getSemanticObject();
        Object tailObject = tail.getSemanticObject();
        // In FSM, it looks like two states are getting connected, but
        // really we make attachments to the appropriate ports.
        if(headObject instanceof Icon && tailObject instanceof Icon) {
            relation = (ComponentRelation)e.getSemanticObject();
            State headState = (State)((Icon)headObject).getContainer();
            State tailState = (State)((Icon)tailObject).getContainer();
            port = headState.incomingPort;
            port.unlink(relation);
            port = tailState.outgoingPort;
            port.unlink(relation);
            // blow the relation away.
            try {
                relation.setContainer(null);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new GraphException(ex.getMessage());
            }             
            return;
	} else {
	    throw new GraphException("Trying to unlink, " +
                    "but head is " + headObject +
                    " and tail is " + tailObject);
	}
    }

    private void _findObjectsAndLink(Edge e, Node head, Node tail) {
	Port port;
	Relation relation;
	Vertex vertex;
        if(head == null || tail == null) return;
        Object headObject = head.getSemanticObject();
        Object tailObject = tail.getSemanticObject();
        // In FSM, it looks like two states are getting connected, but
        // really we make attachments to the appropriate ports.
        if(headObject instanceof Icon && tailObject instanceof Icon) {
            // This may break when we start to deal with ports of composite
            // entity.
            Graph graph = head.getParent();
            while(graph instanceof Node) {
                graph = ((Node)graph).getParent();
            }
            CompositeEntity container = 
                (CompositeEntity) graph.getSemanticObject();
            try {
                relation = 
                    container.newRelation(container.uniqueName("relation"));
                e.setSemanticObject(relation);
                State headState = (State)((Icon)headObject).getContainer();
                State tailState = (State)((Icon)tailObject).getContainer();
                port = headState.incomingPort;
                port.link(relation);
                port = tailState.outgoingPort;
                port.link(relation);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                throw new GraphException(ex.getMessage());
            }
            return;
	} else {
	    throw new GraphException("Trying to link, " +
                    "but head is " + headObject +
                    " and tail is " + tailObject);
	}
    }

    /** Return the node in the graph whose semantic object is the given port
     */
    private Node _findNode(Graph graph, Port port) {
	// This is a bit ugly.
	Node foundNode = null;
	Iterator nodes = graph.nodes();
	while(nodes.hasNext() && foundNode == null) {
	    Node node = (Node)nodes.next();
	    if(node.getSemanticObject().equals(port)) {
		foundNode = node;
	    }
	    if(node instanceof CompositeNode) {
		Iterator portNodes = ((CompositeNode)node).nodes();
		while(portNodes.hasNext() && foundNode == null) {
		    Node portNode = (Node)portNodes.next();
		    if(portNode.getSemanticObject().equals(port))
			foundNode = portNode;
		}
	    }
	}
	return foundNode;
    }
}
