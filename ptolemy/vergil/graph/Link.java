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

    public Object getHead() {
	return _head;
    }

    public ComponentRelation getRelation() {
	return _relation;
    }

    public Object getTail() {
	return _tail;
    } 

    public void setHead(Object head) {
	_head = head;
    }

    public void setRelation(ComponentRelation relation) {
	_relation = relation;
    }

    public void setTail(Object tail) {
	_tail = tail;
    } 

    public void link() throws IllegalActionException, 
            NameDuplicationException {
	ComponentPort port;
	ComponentRelation relation;
	Vertex vertex;

        if(_head == null || _tail == null) return;
        if(_head instanceof ComponentPort && _tail instanceof ComponentPort) {
            CompositeEntity container = (CompositeEntity) getContainer();
            relation = 
                container.newRelation(container.uniqueName("relation"));
            port = (ComponentPort)_head;
            port.link(relation);
            port = (ComponentPort)_tail;
            port.link(relation);
            setRelation(relation);
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
    }

    public void unlink() throws IllegalActionException,
        NameDuplicationException {
	ComponentPort port;
	Vertex vertex;
	ComponentRelation relation;

	if(_head == null || _tail == null) return;
        if(_head instanceof ComponentPort && _tail instanceof ComponentPort) {
            relation = (ComponentRelation)getRelation();
            port = (ComponentPort)_head;
            port.unlink(relation);
            port = (ComponentPort)_tail;
            port.unlink(relation);
            // blow the relation away.
            relation.setContainer(null);
	    setRelation(null);
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
	    throw new RuntimeException("Trying to unlink port from relation, " +
                    "but head is " + _head +
                    " and tail is " + _tail);
	}
	port.unlink(relation);
	setRelation(null);
    }

    public void exportMoML(Writer output, int depth) 
	throws IOException {
	return;
    }

    private Object _head; 
    private Object _tail;
    private ComponentRelation _relation;
}
