/*
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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


 */
package diva.graph;

/**
 * A graph event that is emitted when anything interesting happens
 * inside a graph by way of a GraphModel.  The source of a graph event
 * is the object which caused the change to the graph model, such as a
 * particular controller of the graph or a user algorithm.
 *
 * <p> Each graph event contains an ID and a previous value for all
 * changes and deletions, the specifics of which are described on a
 * case-by-case basis below.
 *
 * @see GraphModel
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Id$
 * @rating Yellow
 */
public class GraphEvent extends java.util.EventObject {
    /**
     * Edge head is changed; target is the edge, old value is the
     * previous value of the edge head.
     */
    public static final int EDGE_HEAD_CHANGED   = 11;

    /**
     * Edge tail is changed; target is the edge, old value is the
     * previous value of the edge tail.
     */
    public static final int EDGE_TAIL_CHANGED   = 12;

    /**
     * A node is added to a graph; target is the
     * node, old value is the previous parent of the graph.
     */
    public static final int NODE_ADDED          = 20;

    /**
     * A node is deleted from a graph; target is the
     * node, old value is the previous parent of the graph.
     */
    public static final int NODE_REMOVED        = 21;

    /**
     * Graph is modified signficantly and should
     * be fully "refreshed"; target is the
     * graph, old value is <i>null</i>.
     */
    public static final int STRUCTURE_CHANGED   = 30;

    /**
     * @serial
     * @see #getID()
     */
    private int _id;

    /**
     * The value of the event.
     * @serial
     */
    private Object _target;

    /**
     * @serial
     * @see #getOldValue()
     */
    private Object _oldValue;

    /**
     * Construct a GraphEvent with the given source and target
     * and a <i>null</i> previous value.
     */
    public GraphEvent(Object source, int id, Object target) {
        this(source, id, target, null);
    }

    /**
     * Construct a GraphEvent with the given
     * source, target, and previous value.
     */
    public GraphEvent(Object source, int id, Object target, Object oldValue) {
        super(source);
        _id = id;
        _target = target;
        _oldValue = oldValue;
    }

    /**
     * Return the type id for this event.
     */
    public int getID() { return _id; }

    /**
     * Return the target value, which is event-specific.
     */
    public Object getTarget() {
        return _target;
    }

    /**
     * Return the old value, which is event-specific.
     */
    public Object getOldValue() {
        return _oldValue;
    }

    /**
     * Return a string representation of the ID.
     */
    private String idToString() {
        switch(getID()) {
        case EDGE_HEAD_CHANGED:
            return "EDGE_HEAD_CHANGED";
        case EDGE_TAIL_CHANGED:
            return "EDGE_TAIL_CHANGED";
        case NODE_ADDED:
            return "NODE_ADDED";
        case NODE_REMOVED:
            return "NODE_REMOVED";
        case STRUCTURE_CHANGED:
            return "STRUCTURE_CHANGED";
        default:
            return "Invalid event ID";
        }
    }

    /**
     * Return a string representation of this event.
     */
    public String toString() {
        return "GraphEvent[" + idToString() + ", "
            + getTarget() + ", " + getOldValue() + "]";
    }
}


