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
public class Link extends Attribute {

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
    public Link(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
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
    public void link() throws IllegalActionException, 
            NameDuplicationException {
	ComponentPort port;
	ComponentRelation relation;
	Vertex vertex;
	CompositeEntity container = (CompositeEntity) getContainer();

        if(_head == null || _tail == null) return;
        if(_head instanceof ComponentPort && _tail instanceof ComponentPort) {
	    relation = 
                container.newRelation(container.uniqueName("relation"));
	    setRelation(relation);
          
	    port = (ComponentPort)_head;
            port.link(relation);
	    _checkReceivers(container, port);

	    port = (ComponentPort)_tail;
            port.link(relation);
	    _checkReceivers(container, port);
	    _checkSchedule(container);
	    return;
        }

	if(_tail instanceof ComponentPort && _head instanceof Vertex) {
	    vertex = (Vertex)_head;
	    port = (ComponentPort)_tail;
	    relation = (ComponentRelation)vertex.getContainer();
	} else if(_tail instanceof Vertex && _head instanceof ComponentPort) {
	    vertex = (Vertex)_tail;
	    port = (ComponentPort)_head;
	    relation = (ComponentRelation)vertex.getContainer();
	} else {
	    throw new RuntimeException("Trying to link port to relation, " +
                    "but head is " + _head +
                    " and tail is " + _tail);
	}
        setRelation(relation);
        port.link(relation);
	_checkReceivers(container, port);
	_checkSchedule(container);
    }

    /** Remove the connections between the head and the tail of this
     * object so that the connectivity of the Ptolemy graph no longer 
     * corresponds with the connection that this link represents.
     * This method is called by the Ptolemy graph model just prior to 
     * changing the head or tail.
     */
    public void unlink() throws IllegalActionException,
        NameDuplicationException {
	ComponentPort port;
	Vertex vertex;
	ComponentRelation relation;
	CompositeEntity container = (CompositeEntity) getContainer();

	if(_head == null || _tail == null) return;
        if(_head instanceof ComponentPort && _tail instanceof ComponentPort) {
            relation = (ComponentRelation)getRelation();
            port = (ComponentPort)_head;
            port.unlink(relation);
	    _checkReceivers(container, port);
	    
	    port = (ComponentPort)_tail;
	    port.unlink(relation);
	    _checkReceivers(container, port);
	    
	    // blow the relation away.
            relation.setContainer(null);
	    setRelation(null);
	    _checkSchedule(container);
	    
	    return;
        }
	if(_tail instanceof ComponentPort && _head instanceof Vertex) {
	    vertex = (Vertex)_head;
	    port = (ComponentPort)_tail;
	    relation = getRelation();
	} else if(_tail instanceof Vertex && _head instanceof ComponentPort) {
	    vertex = (Vertex)_tail;
	    port = (ComponentPort)_head;
	    relation = getRelation();
	} else {
	    throw new RuntimeException(
		    "Trying to unlink port from relation, " +
                    "but head is " + _head +
                    " and tail is " + _tail);
	}
	port.unlink(relation);
	setRelation(null);
	_checkReceivers(container, port);
	_checkSchedule(container);
    }

    /** Don't write anything for links.
     */
    public void exportMoML(Writer output, int depth) 
	throws IOException {
	return;
    }

    /** Return a string representation of this link.
     */
    public String toString() {
	return "Link(" 
	    + _head + ", " 
	    + _tail + ", " 
	    + _relation + ")";
    }

    // If the container has a director, then invalidate its schedule and
    // rerun type resolution
    private void _checkSchedule(CompositeEntity container) {
	if (container instanceof Actor) {
	    Director director = ((Actor)container).getDirector();
	    if (director != null) {
		director.invalidateSchedule();
		director.invalidateResolvedTypes();
	    }
	}
    }

    // If receivers need to be created for the port, then create them.
    // If the container has a director, and the port is an IOPort, then
    //  we create receivers, even if the model is not executing
    private void _checkReceivers(CompositeEntity container, Port port) 
	throws IllegalActionException {
	if (container instanceof Actor) {
	    Director director = ((Actor)container).getDirector();
	    if (director != null && port instanceof IOPort) {
		IOPort ioPort = (IOPort) port;
		if(ioPort.isInput())
		    ioPort.createReceivers();
	    }
	}
    }

    private Object _head; 
    private Object _tail;
    private ComponentRelation _relation;
}
