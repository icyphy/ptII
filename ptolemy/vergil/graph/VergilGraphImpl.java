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

package ptolemy.vergil.graph;

import ptolemy.vergil.toolbox.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import diva.graph.model.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// VergilGraphImpl
/**
The graph implementation for ptolemy graphs.  This class represents the
ptolemy clustered graphs within basic graphs of the diva.graph.model
package.  Currently, only flat graphs are supported.
@author Steve Neuendorffer
@version $Id$
*/
public class VergilGraphImpl extends BasicGraphImpl {
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
	if(object instanceof Port) {
	    Port port = (Port)object;
	    try {
		port.setContainer(container);
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new GraphException(ex.getMessage());
	    }
	} else if(object instanceof Icon) {
	    Icon icon = (Icon)object;
	    ComponentEntity entity = (ComponentEntity)icon.getContainer();
	    try {
		entity.setContainer(container);
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new GraphException(ex.getMessage());
	    }
	} else if(object instanceof Vertex) {
            Vertex vertex = (Vertex) object;
            ComponentRelation relation =
                (ComponentRelation)vertex.getContainer();
	    try {
		relation.setContainer(container);
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
	if(object instanceof Port) {
	    Port port = (Port)object;
	    try {
                port.unlinkAll();
		port.setContainer(null);
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new GraphException(ex.getMessage());
	    }
	} else if(object instanceof Icon) {
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
	} else if(object instanceof Vertex) {
            Vertex vertex = (Vertex) object;
            ComponentRelation relation =
                (ComponentRelation)vertex.getContainer();
	    try {
                relation.unlinkAll();
		relation.setContainer(null);
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
	    Iterator ports = entity.portList().iterator();
	    while(ports.hasNext()) {
		Port port = (Port) ports.next();
	        BasicNode portNode = (BasicNode) createNode(port);
		n.add(portNode);
	    }
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
		// Create a node for each vertex.
		Iterator attributes = relation.attributeList().iterator();
		Node rootVertex = null;
		while(attributes.hasNext()) {
		    Attribute a = (Attribute)attributes.next();
		    if(a instanceof Vertex) {
			Node n = createNode(a);
			if(((Vertex)a).getLinkedVertex() == null) {
			    rootVertex = n;
			}
			addNode(n, g);
		    }
		}
		// If there are no vertecies, then give the relation a vertex
		if(rootVertex == null) {
		    try {
			Vertex v = new Vertex(relation,
                                relation.uniqueName("Vertex"));
			rootVertex = createNode(v);
		    }
		    catch (Exception e) {
			throw new InternalErrorException("Failed to create " +
                                "new vertex, even though one does not " +
                                "already exist:" + e.getMessage());
		    }
		    addNode(rootVertex, g);
		}

		// FIXME connect everything to the root for now.
		Enumeration links = relation.linkedPorts();
		while(links.hasMoreElements()) {
                    Port port = (Port)links.nextElement();
                    // Figure out which node to put the edge to.
		    // this is a little ugly.
		    Node foundNode = null;
		    Iterator nodes = g.nodes();
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
		    Edge newEdge = createEdge(null);
		    setEdgeHead(newEdge, foundNode);
                    setEdgeTail(newEdge, rootVertex);
		}
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
	    _findObjectsAndUnlink(currentHead, currentTail);
	}
	if((currentHead != null || currentTail != null) && head != null) {
	    _findObjectsAndLink(head, currentTail);
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
	    _findObjectsAndUnlink(currentHead, currentTail);
	}
	if((currentHead != null || currentTail != null) && tail != null) {
	    _findObjectsAndLink(currentHead, tail);
	}
	super.setEdgeTail(e, tail);
    }

    public String createLinkName(NamedObj container) {
	String root = "link_";
	int ID = 0;
	String name = null;
	// This is unimportant..  it just gets the loop started.
	Object obj = root;
	while(obj != null) {
	    name = root + ID++;
	    obj = container.getAttribute(name);
	}
	return name;
    }

    private void _findObjectsAndUnlink(Node head, Node tail) {
	Port port;
	Relation relation;
	Vertex vertex;
        if(head == null || tail == null) return;
	if(tail.getSemanticObject() instanceof Port &&
                head.getSemanticObject() instanceof Vertex) {
	    vertex = (Vertex)head.getSemanticObject();
	    port = (Port)tail.getSemanticObject();
	    relation = (Relation)vertex.getContainer();
	} else if(tail.getSemanticObject() instanceof Vertex &&
                head.getSemanticObject() instanceof Port) {
	    vertex = (Vertex)tail.getSemanticObject();
	    port = (Port)head.getSemanticObject();
	    relation = (Relation)vertex.getContainer();
	} else {
	    throw new GraphException("Trying to link port to relation, " +
                    "but head is " + head.getSemanticObject() +
                    " and tail is " + tail.getSemanticObject());
	}
	port.unlink(relation);

    }

    private void _findObjectsAndLink(Node head, Node tail) {
	Port port;
	Relation relation;
	Vertex vertex;
        if(head == null || tail == null) return;
	if(tail.getSemanticObject() instanceof Port &&
                head.getSemanticObject() instanceof Vertex) {
	    vertex = (Vertex)head.getSemanticObject();
	    port = (Port)tail.getSemanticObject();
	    relation = (Relation)vertex.getContainer();
	} else if(tail.getSemanticObject() instanceof Vertex &&
                head.getSemanticObject() instanceof Port) {
	    vertex = (Vertex)tail.getSemanticObject();
	    port = (Port)head.getSemanticObject();
	    relation = (Relation)vertex.getContainer();
	} else {
	    throw new GraphException("Trying to link port to relation, " +
                    "but head is " + head.getSemanticObject() +
                    " and tail is " + tail.getSemanticObject());
	}
	try {
	    port.link(relation);
	}
	catch (IllegalActionException ex) {
	    ex.printStackTrace();
	    throw new GraphException(ex.getMessage());
	}
    }
}
