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
import ptolemy.kernel.event.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.Token;
import ptolemy.data.ObjectToken;
import ptolemy.vergil.ExceptionHandler;
import ptolemy.vergil.toolbox.EditorIcon;

import diva.graph.AbstractGraphModel;
import diva.graph.GraphEvent;
import diva.graph.GraphException;
import diva.graph.GraphUtilities;
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
//// AbstractPtolemyGraphModel
/**
This class defines some useful things that help to create new visual notations
for ptolemy models.  It handles the things that almost all such models need
to handle.  It assumes that all the objects in the graph are ptolemy named
objects.  It also assumes that the semantic object of a particular
graph object is fixed.
<p>
This model uses a ptolemy change listener to detect changes to the model that
do not originate from this model.  These changes are propagated
as structure changed graph events to all graphListeners registered with this
model.  This mechanism allows a graph visualization of a ptolemy model to
remain synchronized with the state of a mutating model.

@author Steve Neuendorffer
@version $Id$
*/
public abstract class AbstractPtolemyGraphModel extends ModularGraphModel {

    public AbstractPtolemyGraphModel(CompositeEntity toplevel) {
	super(toplevel);
	_toplevel = toplevel;
	toplevel.addChangeListener(new GraphChangeListener());
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event.  
     * This method overrides the base class to 
     * perform the operation in a ptolemy change request.
     */
    public void addNode(final Object eventSource, 
			final Object node, final Object parent) {
	ChangeRequest request = new ChangeRequest(
            AbstractPtolemyGraphModel.this,
	    "Add Node " + node + " to composite " + parent) {
		protected void _execute() throws Exception {
		    AbstractPtolemyGraphModel.super.
		    addNode(eventSource, node, parent);
		}
	    };
	_toplevel.requestChange(request);
    }

    /**
     * Connect the given edge to the given tail and head nodes,
     * then dispatch events to the listeners.
     * This method overrides the base class to perform the operation
     * in a ptolemy change request.
     */
    public void connectEdge(final Object eventSource, final Object edge, 
			    final Object tailNode, final Object headNode) {
	ChangeRequest request = new ChangeRequest(
	    AbstractPtolemyGraphModel.this,
	    "Connect Edge " + edge + " from " + tailNode +
	    " to " + headNode) {
		protected void _execute() throws Exception {
		    AbstractPtolemyGraphModel.super.
		    connectEdge(eventSource, edge, tailNode, headNode);
		}
	    };
	_toplevel.requestChange(request);
    }

    /**
     * Connect the given edge to the given tail and head nodes,
     * then dispatch events to the listeners.
     * This method overrides the base class to perform the operation
     * in a ptolemy change request.
      */
    public void disconnectEdge(final Object eventSource, final Object edge) {
	ChangeRequest request = new ChangeRequest(
	    AbstractPtolemyGraphModel.this,
	    "Disconnect Edge " + edge) {
		protected void _execute() throws Exception {
		    AbstractPtolemyGraphModel.super.
		    disconnectEdge(eventSource, edge);
		}
	    };
	_toplevel.requestChange(request);
    }

    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.  This first removes all the
     * edges that are connected to the given node, or some subnode of that
     * node, and then sets the parent of the node to null.
     * This method overrides the base class to perform the operation
     * in a ptolemy change request.
     */
    public void removeNode(final Object eventSource, final Object node) {
	ChangeRequest request = new ChangeRequest(
	    AbstractPtolemyGraphModel.this,
	    "Remove Node " + node) {
		protected void _execute() throws Exception {
		    // Remove the edges.
		    Iterator i = GraphUtilities.partiallyContainedEdges(node, 
		        AbstractPtolemyGraphModel.this);
		    while(i.hasNext()) {
			Object edge = i.next();
			AbstractPtolemyGraphModel.super.
			disconnectEdge(eventSource, edge);
		    }

		    // remove the node.
		    Object prevParent = getNodeModel(node).getParent(node);
		    getNodeModel(node).setParent(node, null);
		    GraphEvent e = new GraphEvent(eventSource, 
						  GraphEvent.NODE_REMOVED,
						  node, prevParent);
		    dispatchGraphEvent(e);
		}
	    };
	_toplevel.requestChange(request);
    }

    /**
     * Return the property of the object associated with
     * the given property name.  In this implementation
     * properties are stored in variables of the graph object (which is
     * always a Ptolemy NamedObj).  If no variable with the given name 
     * exists in the object, then return null.  Otherwise retrieve the
     * token from the variable.  If the token is an instance of ObjectToken, 
     * then get the value from the token and return it.  Otherwise, return 
     * the result of calling toString on the token.
     * @param object The graph object, which is assumed to be an instance of
     * NamedObj.
     * @param propertyName The name of the new property.
     */
    public Object getProperty(Object object, String propertyName) {
	try {
	    NamedObj namedObject = (NamedObj)object;
	    Attribute a = namedObject.getAttribute(propertyName);
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
    
    /**
     * Return the toplevel composite entity of this ptolemy model.
     */
    public CompositeEntity getToplevel() {
	return _toplevel;
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event.
     * This method overrides the base class to perform the operation
     * in a ptolemy change request.
      */
    public void setEdgeHead(final Object eventSource, 
			    final Object edge, final Object head) {
	ChangeRequest request = new ChangeRequest(
	    AbstractPtolemyGraphModel.this,
	    "Set Edge Head " + edge + " to " + head) {
		protected void _execute() throws Exception {
		    AbstractPtolemyGraphModel.super.
		    setEdgeHead(eventSource, edge, head);
		}
	    };
	_toplevel.requestChange(request);
    }

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_TAIL_CHANGED event.
     * This method overrides the base class to perform the operation
     * in a ptolemy change request.
      */
    public void setEdgeTail(final Object eventSource, 
			    final Object edge, final Object tail) {
	ChangeRequest request = new ChangeRequest(
	    AbstractPtolemyGraphModel.this,
	    "Set Edge Tail " + edge + " to " + tail) {
		protected void _execute() throws Exception {
		    AbstractPtolemyGraphModel.super.
		    setEdgeTail(eventSource, edge, tail);
		}
	    };
	_toplevel.requestChange(request);
    }
	
    /**
     * Set the property of the given graph object associated with
     * the given property name to the given value.  In this implementation
     * properties are stored in variables of the graph object (which is
     * always a Ptolemy NamedObj).  If no variable with the given name exists
     * in the graph object, then create a new variable contained
     * by the graph object with the given name.
     * If the value is a string, then set the expression of the variable 
     * to that string. Otherwise create a new object token contained the
     * value and place that in the variable instead.
     * The operation is performed in a ptolemy change request.
     * @param object The graph object.
     * @param propertyName The property name.
     * @param value The new value of the property.
     */
    public void setProperty(final Object object, 
			    final String propertyName,
			    final Object value) {
	ChangeRequest request = new ChangeRequest(
	    AbstractPtolemyGraphModel.this,
	    "Set Property " + propertyName + " to " + value) {
		protected void _execute() throws Exception {
		    NamedObj namedObject = (NamedObj)object;
		    Attribute a = namedObject.getAttribute(propertyName);
		    if(a == null) {
			a = new Variable(namedObject, propertyName);
		    } 
		    Variable v = (Variable)a;
		    if(value instanceof String) {
			v.setExpression((String)value);
			v.getToken();
		    } else {
			v.setToken(new ObjectToken(value));
		    }
		}
	    };
	_toplevel.requestChange(request);

    }
    
    /**
     * Set the semantic object correspoding to the given node, edge,
     * or composite.  The semantic objects in this graph model are 
     * fixed, so this method throws an UnsupportedOperationException.
     * @param object The graph object that represents a node or an edge.
     * @param semantic The semantic object to associate with the given
     * graph object.
     */
    public void setSemanticObject(Object object, Object semantic) {
	throw new UnsupportedOperationException("Ptolemy Graph Model does" +
						" not allow semantic objects" +
						" to be changed");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * Mutations may happen to the ptolemy model without the knowledge of
     * this model.  This change listener listens for those changes
     * and when they occur, issues a GraphEvent so that any views of
     * this graph model can come back and update themselves.  Note that
     * although the graph model uses mutations to make changes to the ptolemy
     * model, those graph events are not handled here.  
     * Instead, they are handled in the base class since they can be easily
     * propagated at a finer level of granularity than is possible here.
     */
    public class GraphChangeListener implements ChangeListener {

        /** Notify the listener that a change has been successfully executed.
	 *  If the originator of this change is not this graph model, then 
	 *  issue a graph event to indicate that the structure of the graph
	 *  has changed.
	 *  @param change The change that has been executed.
	 */
	public void changeExecuted(ChangeRequest change) {
	    // Ignore anything that comes from this graph model.  
	    // the other methods take care of issuing the graph event in
	    // that case.
	    if(change.getOriginator() == AbstractPtolemyGraphModel.this) {
                return;
            }
	    // This has to happen in the swing thread, because Diva assumes
	    // that everything happens in the swing thread.  We invoke later
	    // because the changeRequest that we are listening for often
	    // occurs in the execution thread of the ptolemy model.
	    // FIXME this causes a threading bug apparently.
//	    SwingUtilities.invokeLater(new Runnable() {
//	        public void run() {
		    // Otherwise notify any graph listeners 
		    // that the graph might have
		    // completely changed.
		    dispatchGraphEvent(new GraphEvent(
			AbstractPtolemyGraphModel.this, 
			GraphEvent.STRUCTURE_CHANGED, getRoot()));
//		}
//	    });
	}

        /** Notify the listener that the change has failed with the
         *  specified exception.
 	 *  @param change The change that has failed.
         *  @param exception The exception that was thrown.
         */
        public void changeFailed(ChangeRequest change, Exception exception) {
	    // FIXME? Hmm... I believe that this is (or should be) 
	    // handled elsewhere?
	    ExceptionHandler.show("Change failed", exception);
	    
	    // Just in case something happened to the graph.
	    dispatchGraphEvent(new GraphEvent(AbstractPtolemyGraphModel.this, 
					      GraphEvent.STRUCTURE_CHANGED, 
					      getRoot()));
	}
    }

    // The root of this graph model, as a CompositeEntity.
    private CompositeEntity _toplevel;
}
