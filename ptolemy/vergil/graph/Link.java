/* An object representing a link between a port and a relation.

 Copyright (c) 2000 The Regents of the University of California.
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

import ptolemy.actor.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.*;
import ptolemy.vergil.ptolemy.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.URL;
import java.io.Writer;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// Link
/**
Instances of this class represent a link between a port and a
relation, OR a binary connection between two ports.  In the first
case, the relation is represented by an explicit node in the graph.  In the
second case, there is no explicit node representing the relation and
the edge runs directly from one port to the other.   This class
dynamically determines how to make and break connections based on
which of the above contexts the link is being used in.  This class
is an attribute of the container which contains the relation that the
link represents, as in the case of a binary connection, or the relation that
the link is connected to, as in the case of a relation that is explicitly
represented as a node.

@author Steve Neuendorffer
@version $Id$
*/
public class Link {

    /** Construct an attribute with the specified container and name.
     *  The location contained by the attribute is initially null,
     *  but can be set using the setLocation() method.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Link() {
        super();
    }

    /** Return the head of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public Object getHead() {
	return _head;
    }

    /** Return the relation that this link represents.  If the link goes
     *  from a port to a port, then this is the only way to get at the 
     *  relation.  If the link goes from a vertex to a port, then the
     *  relation will be the container of the vertex.
     */
    public ComponentRelation getRelation() {
	return _relation;
    }

   /** Return the tail of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public Object getTail() {
	return _tail;
    } 

    /** Set the head of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public void setHead(Object head) {
	_head = head;
    }
    
    /** Set the relation for this link.  
     */
    public void setRelation(ComponentRelation relation) {
	_relation = relation;
    }

    /** Set the tail of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public void setTail(Object tail) {
	_tail = tail;
    } 

    /** Create the necessary connections between the head and the tail of this
     * object so that the connectivity of the Ptolemy graph corresponds
     * with the connection that this link represents.  This method is called
     * by the Ptolemy graph model in response to changing the head or the
     * tail.
     */
    public void link(Object requestor) throws IllegalActionException, 
            NameDuplicationException {
	ComponentPort port;
	ComponentRelation relation;
	Vertex vertex;
		
        if(_head == null || _tail == null) return;
	// Deal with the fact that we might be trying to attach to an 
	// external port.
	Object head;
	final CompositeEntity container;
	if(_head instanceof Location &&
	   ((Location)_head).getContainer() instanceof ComponentPort) {
	    head = ((Location)_head).getContainer();
	    // The container of the external port
	    container = (CompositeEntity)((NamedObj)head).getContainer();
	} else {
	    head = _head;
	    // head is either a vertex in a relation or an internal port in 
	    // a component entity.
	    Object temp = ((NamedObj)head).getContainer();
	    temp = ((NamedObj)temp).getContainer();
	    container = (CompositeEntity)temp;
	}
	Object tail;
	if(_tail instanceof Location &&
	   ((Location)_tail).getContainer() instanceof ComponentPort) {
	    tail = ((Location)_tail).getContainer();
	} else {
	    tail = _tail;
	}

        if(head instanceof ComponentPort && tail instanceof ComponentPort) {
	    ComponentPort headPort = (ComponentPort)head;
	    ComponentPort tailPort = (ComponentPort)tail;
	    // Linking two ports with a new relation.
	    StringBuffer moml = new StringBuffer();
	    moml.append("<group>\n");
	    final String relationName = container.uniqueName("relation");
	    // Note that we use no class so that we use the container's
	    // factory method when this gets parsed
	    moml.append("<relation name=\"" + relationName + 
			"\" class=\"ptolemy.actor.TypedIORelation\"/>\n");
	    moml.append("<link port=\"" + headPort.getName(container) + 
			"\" relation=\"" + relationName + "\"/>\n");
	    moml.append("<link port=\"" + tailPort.getName(container) + 
			"\" relation=\"" + relationName + "\"/>\n");
	    moml.append("</group>\n");
	    ChangeRequest request = 
		new MoMLChangeRequest(requestor, container, moml.toString()) {
                    protected void _execute() throws Exception {
			super._execute();
			// Set the relation that this link is going to modify.
			// I'm not sure if this asynchrony will cause
			// things to break.
			setRelation(container.getRelation(relationName));
		    }
		};
	    container.requestChange(request);
	    try {
		request.waitForCompletion();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    return;
        }
	    
	if(tail instanceof ComponentPort && head instanceof Vertex) {
	    vertex = (Vertex)head;
	    port = (ComponentPort)tail;
	    relation = (ComponentRelation)vertex.getContainer();
	} else if(tail instanceof Vertex && head instanceof ComponentPort) {
	    vertex = (Vertex)tail;
	    port = (ComponentPort)head;
	    relation = (ComponentRelation)vertex.getContainer();
	} else {
	    throw new RuntimeException("Trying to link port to relation, " +
                    "but head is " + head +
                    " and tail is " + tail);
	}
	// Linking a port to an existing relation.
	StringBuffer moml = new StringBuffer();
	final ComponentRelation finalRelation = relation;
	moml.append("<link port=\"" + port.getName(container) + 
		    "\" relation=\"" + relation.getName(container) + "\"/>\n");
	ChangeRequest request = 
	    new MoMLChangeRequest(requestor, container, moml.toString()) {
		protected void _execute() throws Exception {
		    super._execute();
		    // Set the relation that this link is going to modify.
		    // I'm not sure if this asynchrony will cause
		    // things to break.
		    setRelation(finalRelation);
		}
	    };
	container.requestChange(request);
    }

    /** Remove the connections between the head and the tail of this
     * object so that the connectivity of the Ptolemy graph no longer 
     * corresponds with the connection that this link represents.
     * This method is called by the Ptolemy graph model just prior to 
     * changing the head or tail.
     */
    public void unlink(Object requestor) throws IllegalActionException, 
    NameDuplicationException {
	ComponentPort port;
	Vertex vertex;
	ComponentRelation relation;

	if(_head == null || _tail == null) return;
	// Deal with the fact that we might be trying to attach to an 
	// external port.
	Object head;
	CompositeEntity container;
	if(_head instanceof Location &&
	   ((Location)_head).getContainer() instanceof ComponentPort) {
	    head = ((Location)_head).getContainer();
	    // The container of the external port
	    container = (CompositeEntity)((NamedObj)head).getContainer();
	} else {
	    head = _head;
	    // head is either a vertex in a relation or an internal port in 
	    // a component entity.
	    Object temp = ((NamedObj)head).getContainer();
	    temp = ((NamedObj)temp).getContainer();
	    container = (CompositeEntity)temp;
	}
	Object tail;
	if(_tail instanceof Location &&
	   ((Location)_tail).getContainer() instanceof ComponentPort) {
	    tail = ((Location)_tail).getContainer();
	} else {
	    tail = _tail;
	}

        if(head instanceof ComponentPort && tail instanceof ComponentPort) {
	    ComponentPort headPort = (ComponentPort)head;
	    ComponentPort tailPort = (ComponentPort)tail;
	    relation = (ComponentRelation)getRelation();
	    // Linking two ports with a new relation.
	    StringBuffer moml = new StringBuffer();
	    moml.append("<group>\n");
	    // Note that we use no class so that we use the container's
	    // factory method when this gets parsed
	    moml.append("<unlink port=\"" + headPort.getName(container) + 
			"\" relation=\"" + relation.getName(container) + 
			"\"/>\n");
	    moml.append("<unlink port=\"" + tailPort.getName(container) + 
			"\" relation=\"" + relation.getName(container) + 
			"\"/>\n");
	    moml.append("<deleteRelation name=\"" + 
			relation.getName(container) + 
			"\"/>\n");
	    moml.append("</group>\n");
	    ChangeRequest request = 
		new MoMLChangeRequest(requestor, container, moml.toString()) {
                    protected void _execute() throws Exception {
			super._execute();
			// Set the relation that this link is going to modify.
			// I'm not sure if this asynchrony will cause
			// things to break.
			setRelation(null);
		    }
		};
	    container.requestChange(request);
	    try {
		request.waitForCompletion();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    
	    return;
        }
	if(tail instanceof ComponentPort && head instanceof Vertex) {
	    vertex = (Vertex)head;
	    port = (ComponentPort)tail;
	    relation = getRelation();
	} else if(tail instanceof Vertex && head instanceof ComponentPort) {
	    vertex = (Vertex)tail;
	    port = (ComponentPort)head;
	    relation = getRelation();
	} else {
	    throw new RuntimeException(
		    "Trying to unlink port from relation, " +
                    "but head is " + head +
                    " and tail is " + tail);
	}
	// Unlinking a port from an existing relation.
	StringBuffer moml = new StringBuffer();
	final ComponentRelation finalRelation = relation;
	System.out.println("container = " + container);
	moml.append("<unlink port=\"" + port.getName(container) + 
		    "\" relation=\"" + relation.getName(container) + "\"/>\n");
	ChangeRequest request = 
	    new MoMLChangeRequest(requestor, container, moml.toString()) {
		protected void _execute() throws Exception {
		    super._execute();
		    // Set the relation that this link is going to modify.
		    // I'm not sure if this asynchrony will cause
		    // things to break.
		    setRelation(null);
		}
	    };
	container.requestChange(request);
	try {
	    request.waitForCompletion();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    /** Return a string representation of this link.
     */
    public String toString() {
	return "Link(" 
	    + _head + ", " 
	    + _tail + ", " 
	    + _relation + ")";
    }

    private Object _head; 
    private Object _tail;
    private ComponentRelation _relation;
}
