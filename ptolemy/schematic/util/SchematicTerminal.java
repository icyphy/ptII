/* An Terminal represents a point that can be connected to.

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

import ptolemy.kernel.util.*;
import java.util.*;
import collections.*;
import ptolemy.schematic.xml.XMLElement;
import diva.util.*;
import diva.graph.model.*;

//////////////////////////////////////////////////////////////////////////
//// SchematicTerminal
/**

An SchematicTerminal is the graphical representation of a schematic entity.
SchematicTerminals are connected by SchematicLinks.  SchematicTerminals
are contained within schematics, schematic relations, and schematic entities.
Usually terminals may be freely moved about.  However, some terminals (notably,
those that are contained in entities with a template) may have a template.  
In these cases, the terminal is fixed to the position of the template.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicTerminal extends PTMLTemplateObject 
implements diva.graph.model.Node {

    //FIXME: hack to get unique names.
    private static int _instance = 0;
    /**
     * Create a new SchematicTerminal with the name "SchematicTerminal",
     * and no template. 
     */
    public SchematicTerminal () {
        this("SchematicTerminal" + _instance, null);
        _instance++;
    }

    /** 
     * Create a new SchematicTerminal with the given name, and no template.
     */
    public SchematicTerminal (String name) {
        super(name, null);
    }

    /**
     * Create a new SchematicTerminal with the given template, and the
     * name of the template. 
     */
    public SchematicTerminal (Terminal template) {
        this(template.getName(), template);
    }

    /** 
     * Create a new SchematicTerminal with the given name and template.
     */
    public SchematicTerminal (String name, Terminal template) {
        super(name, template);
        if(template != null) {
            _x = template.getX();
            _y = template.getY();
        } else {
            _x = 0;
            _y = 0;
        }
    }

    /**
     * Return the X position of this SchematicTerminal
     */
    public double getX() {
        return _x;
    }

    /**
     * Return the Y position of this SchematicTerminal
     */
    public double getY() {
        return _y;
    }

    /**
     * Return true only if this SchematicTerminal can be moved.
     */
    public boolean isMoveable() {
        return !hasTemplate();
    }

   /**
     * Set the template object of this object.   If the template is not
     * null, then set the position of the terminal to the position of the
     * template terminal.
     */
    public void setTemplate(PTMLObject obj) {
        super.setTemplate(obj);
        if(hasTemplate()) {
            _x = ((Terminal)getTemplate()).getX();
            _y = ((Terminal)getTemplate()).getY();
        }
    }

   /**
     * Set the X location of this Terminal.
     */
    public void setX(double x) {
        if(!isMoveable())
            throw new InternalErrorException("Terminal " + getFullName() +
                    "has a template, and so can't be moved.");
        _x = x;
    }

   /**
     * Set the Y location of this Terminal.
     */
    public void setY(double y) {
        if(!isMoveable())
            throw new InternalErrorException("Terminal " + getFullName() +
                    "has a template, and so can't be moved.");
        _y = y;
    }

    // methods from diva.graph.model.Node.
    /** Get the object containing the in edges.
     */
    protected BasicEdgeSet getInEdgeSet() {
        return _in;
    }

    /** Get the object containing the out edges
     */
    public BasicEdgeSet getOutEdgeSet() {
        return _out;
    }

    /**
     * Return the parent graph of this node.
     */
    public Graph getParent() {
	return (Graph)getContainer();
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
	return getInEdgeSet().edges();
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
	return getOutEdgeSet().edges();
    }

    /** Set the parent of this node, that is, the graph in
     * which it is contained.
     */
    public void setParent(Graph g) {
	try {
	    setContainer((PTMLObject)g);
	} catch (Exception e) {
	    throw new GraphException(e.getMessage());
	}
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
     * Return a string representation of the terminal
     */
    protected String _description(int indent) {
        String result = super._description(indent);
        result += _getIndentPrefix(indent) + "X=" + _x + "\n";
        result += _getIndentPrefix(indent) + "Y=" + _y + "\n";
        return result;
    }

    private double _x, _y;

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

    /**
     * The edges <b>into</b> this node.
     */
    private BasicEdgeSet _in = new BasicEdgeSet();

    /**
     * The edges <b>out of</b> this node.
     */
    private BasicEdgeSet _out = new BasicEdgeSet();

}

