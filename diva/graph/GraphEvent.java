/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
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
 * @version $Revision$
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


