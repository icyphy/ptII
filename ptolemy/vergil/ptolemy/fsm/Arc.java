/* An object representing an arc between two states.

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

package ptolemy.vergil.ptolemy.fsm;

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
import ptolemy.domains.fsm.kernel.*;
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
//// Arc
/**

@author Steve Neuendorffer
@version $Id$
*/
public class Arc {

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
    public Arc() {
	super();
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

    public void link(Object requestor) throws IllegalActionException {
	ComponentPort port;
	ComponentRelation relation;
	Vertex vertex;

        if(_head == null || _tail == null) return;
	// In FSM, it looks like two states are getting connected, but
	// really we make attachments to the appropriate ports.
	if(_head instanceof Icon && _tail instanceof Icon) {        
	    State headState = (State)((Icon)_head).getContainer();
	    State tailState = (State)((Icon)_tail).getContainer();
	    final CompositeEntity container = 
		(CompositeEntity)headState.getContainer();
	    // Linking two ports with a new relation.
	    StringBuffer moml = new StringBuffer();
	    moml.append("<group>\n");
	    final String relationName = container.uniqueName("relation");
	    // Note that we use no class so that we use the container's
	    // factory method when this gets parsed
	    moml.append("<relation name=\"" + relationName + 
			"\" class=\"ptolemy.domains.fsm.kernel.Transition\"/>\n");
	    moml.append("<link port=\"" + 
			headState.incomingPort.getName(container) + 
			"\" relation=\"" + relationName + "\"/>\n");
	    moml.append("<link port=\"" + 
			tailState.outgoingPort.getName(container) + 
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
	} else {
	    throw new RuntimeException("Trying to link, " +
                    "but head is " + _head +
                    " and tail is " + _tail);
	}
    }

    public void unlink(Object requestor) throws IllegalActionException {
	ComponentPort port;
	Vertex vertex;
	ComponentRelation relation;

	if(_head == null || _tail == null) return;
        if(_head instanceof Icon && _tail instanceof Icon) {
	    State headState = (State)((Icon)_head).getContainer();
	    State tailState = (State)((Icon)_tail).getContainer();
	    final CompositeEntity container = 
		(CompositeEntity)headState.getContainer();
	    // Linking two ports with a new relation.
	    StringBuffer moml = new StringBuffer();
	    moml.append("<group>\n");
	    final String relationName = getRelation().getName(container);
	    // Note that we use no class so that we use the container's
	    // factory method when this gets parsed
	    moml.append("<unlink port=\"" + 
			headState.incomingPort.getName(container) + 
			"\" relation=\"" + relationName + "\"/>\n");
	    moml.append("<unlink port=\"" + 
			tailState.outgoingPort.getName(container) + 
			"\" relation=\"" + relationName + "\"/>\n");
	    moml.append("<deleteRelation name=\"" + 
			relationName + "\"/>\n");
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
