/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */
package ptolemy.schematic.editor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import diva.graph.model.*;
import java.util.*;

/**
 * 
 *
 * @author Steve Neuendorffer
 * @version $Id$
 * @rating red
 */
public class EditorGraphImpl extends BasicGraphImpl {
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
	} else if(object instanceof ComponentEntity) {
	    ComponentEntity entity = (ComponentEntity)object;
	    try {
		entity.setContainer(container);
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new GraphException(ex.getMessage());
	    }
	}    
    }

    /**
     * Return a new instance of a BasicCompositeNode with the given
     * semantic object.
     */
    public CompositeNode createCompositeNode(Object semanticObject) {
	BasicCompositeNode n = 
	    (BasicCompositeNode) super.createCompositeNode(semanticObject);
	if(semanticObject instanceof Entity) {
	    Entity entity = (Entity) semanticObject;
	    Enumeration ports = entity.getPorts();
	    while(ports.hasMoreElements()) {
		Port port = (Port) ports.nextElement();
	        BasicNode portNode = (BasicNode) createNode(port);
		n.add(portNode);
	    }
	}	
        return n;
    }

    /**
     * Set the edge's head to the given node.
     */
    public void setEdgeHead(Edge e, Node head) {
        // First set the head.
	super.setEdgeHead(e, head);
        System.out.println("Setting head");
        try {
            BasicNode bh = (BasicNode)e.getHead();
            BasicNode bt = (BasicNode)e.getTail();
            LinkAttribute link = (LinkAttribute)e.getSemanticObject();
            
            // If the edge is not connected to two vertecies, then throw away
            // the link.
            if((bh == null) || (bt == null)) {
                System.out.println("destroying link");        
                if(link != null) {
                    Port port = link.getPort(); 
                    VertexAttribute vertex = link.getVertex();
                    if(port != null && vertex != null) {
                        port.unlink((Relation)vertex.getContainer());
                    } else {
                        throw new RuntimeException("Found" +
                                " a partially unlinked port.");
                        //ARGH this shouldn't happen
                    }
                    // Finish blowing away the link.
                    link.setPort(null);
                    link.setVertex(null);
                    link.setContainer(null);
                    e.setSemanticObject(null);
                }
            } else {
                NamedObj headObject = (NamedObj)bh.getSemanticObject();
                NamedObj tailObject = (NamedObj)bt.getSemanticObject();
                // Ensure that there is a link connecting the port to the vertex.
                if(link != null) {
                    System.out.println("Moving link");
                    // HMM.. there is already a link...  make sure it's
                    // connected right.
                    Port port = link.getPort(); 
                    VertexAttribute vertex = link.getVertex();
                    // Fix the head.  This may involve relinking to the same port
                    // if the connector didn't move.  This is OK.
                    port.unlink((Relation)vertex.getContainer());
                    if(tailObject instanceof VertexAttribute &&
                            headObject instanceof Port) {
                        port = (Port)headObject;
                        link.setPort(port);
                    } else if(tailObject instanceof Port &&
                            headObject instanceof VertexAttribute) {
                        vertex = (VertexAttribute)headObject;
                        link.setVertex(vertex);
                    } else {
                        throw new InternalErrorException("Edge must be between " + 
                                "Port and VertexAttribute");
                    }                
                    port.link((Relation)vertex.getContainer());
                } else {
                    // There was no link, so create it from scratch
                    System.out.println("Creating link");
                    Port port;
                    VertexAttribute vertex;
                    if(tailObject instanceof VertexAttribute &&
                            headObject instanceof Port) {
                        port = (Port)headObject;
                        vertex = (VertexAttribute)tailObject;
                    } else if(tailObject instanceof Port &&
                            headObject instanceof VertexAttribute) {
                        vertex = (VertexAttribute)headObject;
                        port = (Port)tailObject;
                    } else {
                        throw new InternalErrorException("Edge must be between " + 
                                "Port and VertexAttribute");
                    }   
                    Relation relation = (Relation)vertex.getContainer();
                    link = new LinkAttribute(relation, 
					     createLinkName(relation));
                    e.setSemanticObject(link);
                    link.setPort(port);
                    link.setVertex(vertex);
                    port.link(relation);
                }
            }
        } catch (IllegalActionException ex) {
            ex.printStackTrace();
            throw new GraphException(ex.getMessage());
        } catch (NameDuplicationException ex) {
            ex.printStackTrace();
            throw new GraphException(ex.getMessage());
        }
    }

    /**
     * Set the edge's tail to the given node.
     */
    public void setEdgeTail(Edge e, Node tail) {
	super.setEdgeTail(e, tail);
        System.out.println("Setting tail");
        try {
            BasicNode bh = (BasicNode)e.getHead();
            BasicNode bt = (BasicNode)e.getTail();
            LinkAttribute link = (LinkAttribute)e.getSemanticObject();
            
            // If the edge is not connected to two vertecies, then throw away
            // the link.
            if((bh == null) || (bt == null)) {
                if(link != null) {
                    System.out.println("destroying link");        
                    Port port = link.getPort(); 
                    VertexAttribute vertex = link.getVertex();
                    if(port != null && vertex != null) {
                        port.unlink((Relation)vertex.getContainer());
                    } else {
                        throw new RuntimeException("Found" +
                                " a partially unlinked port.");
                        //ARGH this shouldn't happen
                    }
                    // Finish blowing away the link.
                    link.setPort(null);
                    link.setVertex(null);
                    link.setContainer(null);
                    e.setSemanticObject(null);
                }
            } else {
                NamedObj headObject = (NamedObj)bh.getSemanticObject();
                NamedObj tailObject = (NamedObj)bt.getSemanticObject();
                // Ensure that there is a link connecting the port to the vertex.
                if(link != null) {
                    // HMM.. there is already a link...  make sure it's
                    // connected right.
                    System.out.println("Moving link");
                    Port port = link.getPort(); 
                    VertexAttribute vertex = link.getVertex();
                    // Fix the head.  This may involve relinking to the same port
                    // if the connector didn't move.  This is OK.
                    port.unlink((Relation)vertex.getContainer());
                    if(tailObject instanceof VertexAttribute &&
                            headObject instanceof Port) {
                        vertex = (VertexAttribute)tailObject;
                        link.setVertex(vertex);
                    } else if(tailObject instanceof Port &&
                            headObject instanceof VertexAttribute) {
                        port = (Port)tailObject;
                        link.setPort(port);
                    } else {
                        throw new InternalErrorException("Edge must be between " + 
                                "Port and VertexAttribute");
                    }                
                    port.link((Relation)vertex.getContainer());
                } else {
                    // There was no link, so create it from scratch
                    System.out.println("Creating link");
                    Port port;
                    VertexAttribute vertex;
                    if(tailObject instanceof VertexAttribute &&
                            headObject instanceof Port) {
                        port = (Port)headObject;
                        vertex = (VertexAttribute)tailObject;
                    } else if(tailObject instanceof Port &&
                            headObject instanceof VertexAttribute) {
                        vertex = (VertexAttribute)headObject;
                        port = (Port)tailObject;
                    } else {
                        throw new InternalErrorException("Edge must be between " + 
                                "Port and VertexAttribute");
                    }   
                    Relation relation = (Relation)vertex.getContainer();
                    link = new LinkAttribute(relation, 
					     createLinkName(relation));
                    e.setSemanticObject(link);
                    link.setPort(port);
                    link.setVertex(vertex);
                    port.link(relation);
                }
            }
        } catch (IllegalActionException ex) {
            ex.printStackTrace();
            throw new GraphException(ex.getMessage());
        } catch (NameDuplicationException ex) {
            ex.printStackTrace();
            throw new GraphException(ex.getMessage());
        }
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
}
