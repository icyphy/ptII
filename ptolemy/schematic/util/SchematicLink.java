/* A SchematicLink represents a link in a relation.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.schematic.util;

import java.util.*;
import diva.util.*;
import diva.graph.model.*;

//////////////////////////////////////////////////////////////////////////
//// SchematicLink
/**

A SchematicLink represents a connection between two SchematicTerminals. 
This link 


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicLink extends BasicPropertyContainer
    implements diva.graph.model.Edge {

    public SchematicLink () {
        // FIXME: is this right?
        this(null, null);
    }

    /**
     * Create a new SchematicLink object connecting the two ports.
     */
    public SchematicLink (SchematicTerminal to, SchematicTerminal from) {
        super();
	_to = to;
	_from = from;
    }

    /**
     * Return the terminal that this link connects from.
     */
    public SchematicTerminal getFrom() {
	return _from;
    }

    /**
     * Return the terminal that this link connects to.
     */
    public SchematicTerminal getTo() {
	return _to;
    }

    /** 
     * Set the from port of this Link to the given port.
     */
    public void setFrom(SchematicTerminal port) {
    	if(_from != null) {
	    _from.getOutEdgeSet().remove(this);
	}
	_from = port;
        if(_from != null) {
    	    _from.getOutEdgeSet().add(this);
        }
    }

    /** 
     * Set the to port of this Link to the given port.
     */
    public void setTo(SchematicTerminal port) {
    	if(_to != null) {
	    _to.getInEdgeSet().remove(this);
	}
	_to = port;
        if(_to != null) {
    	    _to.getInEdgeSet().add(this);
        }
    }
   
    public String toString() {
        String s = getClass().getName();
        s += " {" + getFrom().getFullName();
        s += "->" + getTo().getFullName();
        s += "}";
        return s;
    }

    /**
     * Return a string this representing Entity.
     */
    protected String _description(int indent, int bracket) {
        String result = _getIndentPrefix(indent);
        if (bracket == 1 || bracket == 2) result += "{";
        result += getClass().getName() + "\n";
        result += _getIndentPrefix(indent) + " to {\n";
        if(_to == null) 
            result += _getIndentPrefix(indent + 1) + "null\n";
        else
            result += _to._description(indent + 1, 0) + "\n";
	result += _getIndentPrefix(indent) + "} from {\n";
        if(_from == null) 
            result += _getIndentPrefix(indent + 1) + "null\n";
        else
            result += _from._description(indent + 1, 0) + "\n";

        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";

        return result;
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        String result = "";
        for (int i = 0; i < level; i++) {
            result += "    ";
        }
        return result;
    }    

    //diva.graph.model.Edge
    /*    public void attach(BasicNode tail, BasicNode head) {
        setTail(tail);
        setHead(head);
    }

    public void detach() {
        setTail(null);
        setHead(null);
    }
    */

    public Node getHead() { return getTo(); }
    
    public Node getTail() { return getFrom(); }
    
    /* Get the visual object of this edge. Generally this
     * is used when this edge has a visual representation.
     */
    public Object getVisualObject() {
	return _visualObject;
    }
    
    //public double getWeight() { return _weight; }
    
    public Object getSemanticObject() { return _semanticObject; }

    public boolean isDirected() { return false; }
    
    public void setDirected(boolean val) {
        // Do nothing.  Edges in ptolemy are always directed.
    }

    /*
    public void setHead(BasicNode n) {
        BasicNode prevHead = (BasicNode)getHead();
	if(prevHead != null) {
	    prevHead.getInEdgeSet().remove(this);
	}

	_head = n;
        if(_head != null) {
    	    _head.getInEdgeSet().add(this);
        }
    }

    public void setTail(BasicNode n) {
        BasicNode prevTail = (BasicNode)getTail();
    	if(prevTail != null) {
	    prevTail.getOutEdgeSet().remove(this);
	}

	_tail = n;
        if(_tail != null) {
    	    _tail.getOutEdgeSet().add(this);
        }
    }
    */

    /**  Set the visual object of this edge.
     */
    public void setVisualObject(Object o) {
	_visualObject = o;
    }

    // public void setWeight(double weight ) { _weight = weight; }

    public void setSemanticObject(Object o) { _semanticObject = o;  }


    private SchematicTerminal _to;
    private SchematicTerminal _from;

    /**
     * The visual representation.
     */
    private Object _visualObject = null;

   /**
     * The underlying semantic object.
     */
    private Object _semanticObject = null;
    
}

