/* A SchematicRelation represents a relation in a schematic.

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
import ptolemy.kernel.util.*;
import collections.CircularList;
import diva.util.*;
import diva.graph.model.*;

//////////////////////////////////////////////////////////////////////////
//// SchematicRelation
/**

A SchematicRelation represents a relation in a Ptolemy II schematic.
It contains links, which specify the topology of the schematic.
Every link has a name that specifies the unique port within a schematic that
it is connected to.   A link name is formed by period concatenating the
entity name and the port name that the link is connected to, such as
"entity.port".
<!-- schematic relations will be parsed into class SchemticRelation -->
<!ELEMENT relation (link)*>
<!ATTLIST relation
name ID #REQUIRED>
<!ELEMENT link EMPTY>
<!ATTLIST link
name CDATA #REQUIRED>


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicRelation extends PTMLObject 
    implements diva.graph.model.Node {

    /**
     * Create a new SchematicRelation object.
     */
    public SchematicRelation () {
        this("relation");
    }

    /**
     * Create a new SchematicRelation object.
     */
    public SchematicRelation (String name) {
        super(name);
        _links = (CircularList) new CircularList();
	_terminals = (NamedList) new NamedList();
	setWidth(1);
    }

    /**
     * Add a new link to this relation. 
     */
    public void addLink (SchematicLink link) {
        _links.insertLast(link);
    }

    /**
     * Add a new port to this relation. 
     *  @exception IllegalActionException If the port has no name.
     *  @exception NameDuplicationException If the name of the port
     *  coincides with the name of another port 
     *  contained in this relation.
     */
    public void addTerminal (SchematicTerminal terminal) 
        throws IllegalActionException, NameDuplicationException {
        _terminals.append(terminal);
    }

    /**
     * Test if this relation contains the given link.
     */
    public boolean containsLink (SchematicLink link) {
        return _links.includes(link);
    }

   /**
     * Test if this relation contains the given port.
     */
    public boolean containsTerminal (SchematicTerminal terminal) {
        return _terminals.includes(terminal);
    }

    /**
     * @return The width of this relation.
     */
    public int getWidth() {
        return _width;
    }

    /**
     * Return an enumeration over the links in this relation. \
     *
     * @return An Enumeration of SchematicLink
     */
    public Enumeration links () {
        return _links.elements();
    }

    /**
     * Return an enumeration over the ports in this relation. \
     *
     * @return An Enumeration of SchematicTerminal
     */
    public Enumeration terminals () {
        return _terminals.elements();
    }

    /**
     * Remove the given link from this relation.
     */
    public void removeLink(SchematicLink link) {
        _links.removeOneOf(link);
    }

    /**
     * Remove the given link from this relation.
     */
    public void removeTerminal(SchematicTerminal terminal) {
        _terminals.remove(terminal);
    }

    /**
     * Set the width of this relation.
     */
    public void setWidth(int width) {
	_width = width;
    }

    /**
     * Return a string representing this relation.
     */
    public String toString() {
        Enumeration enumterminals = terminals();
        String str = getName() + "({";
        while(enumterminals.hasMoreElements()) {
            SchematicTerminal terminal = 
		(SchematicTerminal) enumterminals.nextElement();
            str += "\n..." + terminal.toString();
        }
        str += "}{";
	Enumeration enumlinks = links();
        while(enumlinks.hasMoreElements()) {
	    SchematicLink link = 
		(SchematicLink) enumlinks.nextElement();
	    str += "\n..." + link.toString();
	}
	str += "})";
	return str;
    }

    // methods from diva.graph.Node.
    /**
     * Return the parent graph of this node.
     */
    public Graph getParent() {
	return _parent;
    }

    /* Get the semantic object of this node. Generally this
     * is used when this node is a "wrapper" for some other object
     * or model with deeper meaning.
     */
    public Object getSemanticObject() {
	return _semanticObject;
    }
    
    /* Get the visual object of this node. Generally this
     * is used when this node has a visual representation.
     */
    public Object getVisualObject() {
	return _visualObject;
    }
    
    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges() {
	// FIXME: how to find this?
	return null;
    }

    /**
     * Return the visited flag for this node.  This is typically used
     * by graph traversal algorithms.
     */
    public boolean isVisited() {
	return _visited;
    }
		
    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges() {
	// FIXME: how to find this?
	return null;
    }

    /** Set the parent of this node, that is, the graph in
     * which it is contained.
     */
    public void setParent(Graph g) {
        _parent = g;
    }

    /**  Set the semantic object of this node. Generally this
     * is used when this node is a "wrapper" for some other object
     * or model with deeper meaning.
     */
    public void setSemanticObject(Object o) {
	_semanticObject = o;
    }

    /**  Set the visual object of this node.
     */
    public void setVisualObject(Object o) {
	_visualObject = o;
    }

    /**
     * Set the visited flag for this node.  Algorithms that use this
     * flag are responsible for setting the visited flag to "false"
     * before they begin a traversal (in other words,
     * they cannot expect that a previous traversal has
     * left the nodes unmarked).
     */
    public void setVisited(boolean val) {
	_visited = val;
    }

    /**
     * The graph to which this node belongs.
     */
    private Graph _parent = null;

    /**
     * Whether or not this node has been visited.
     */
    private boolean _visited = false;

    /**
     * The underlying semantic object.
     */
    private Object _semanticObject = null;

    /**
     * The visual representation.
     */
    private Object _visualObject = null;

    private int _width;
    private CircularList _links;
    private NamedList _terminals;
}

