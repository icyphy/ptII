/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.connector;

import diva.canvas.Figure;

/**
 * A graph event that is emitted when anything
 * interesting happens inside a graph by way of a GraphModel.
 * Each graph event contains an ID, a source, and a
 * previous value for all changes and deletions,
 * the specifics of which are described on a case-by-case
 * basis below.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @contributor Edward A. Lee (eal@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class ConnectorEvent extends java.util.EventObject {

    /**
     * Signifies that a connector is dragged some distance
     */
    public static final int CONNECTOR_DRAGGED = 12;

    /**
     * Signifies that a connector end is dropped. If the connector
     * is currently snapped to a target, the target can be obtained
     * from the event as the source field.
     */
    public static final int CONNECTOR_DROPPED = 13;

    /**
     * Signifies that a connector end is snapped to
     * a possible target.
     */
    public static final int CONNECTOR_SNAPPED = 14;

    /**
     * Signifies that a connector end is unsnapped from
     * the original site that it was attached to, or
     * from a possible target.
     */
    public static final int CONNECTOR_UNSNAPPED = 15;

    /**
     * This event concerns the head end of the connector.
     *
     * @see #getEnd()
     */
    public static final int HEAD_END = 21;

    /**
     * This event concerns the tail end of the connector.
     *
     * @see #getEnd()
     */
    public static final int TAIL_END = 22;

    /**
     * This event is concerns both ends of the connector.
     *
     * @see #getEnd()
     */
    public static final int BOTH_ENDS = 23;

    /**
     * This event is concerns the midpoint of the connector.
     *
     * @see #getEnd()
     */
    public static final int MIDPOINT = 24;

    /**
     * The event ID.
     *
     * @see #getID()
     * @serial
     */
    private int _id;

    /**
     * The end this event concerns.
     *
     * @see #getEnd()
     * @serial
     */
    private int _end;

    /**
     * The target figure
     *
     * @see #getTarget()
     * @serial
     */
    private Figure _target;

    /**
     * The connector that is being operated on.
     *
     * @see #getConnector()
     * @serial
     */
    private Connector _connector;

    /**
     * Construct a ConnectorEvent with the given source, target,
     * connector, and "end" flag. The source is the layer in which
     * the connector exists, while the target is the figure that the
     * connector is snapped to or unsnapped from.
     */
    public ConnectorEvent(
            int id, Object source,
            Figure target,
            Connector connector, int end) {
        super(source);
        _target = target;
        _id = id;
        _connector = connector;
        _end = end;
    }

    /**
     * Return the connector that this event concerns.
     */
    public Connector getConnector() {
        return _connector;
    }

    /**
     * Return the end of the connector that this
     * event concerns.
     */
    public int getEnd() { return _end; }

    /**
     * Return the type id for this event.
     */
    public int getID() { return _id; }

    /**
     * Return the target that the connector is snapped to
     * or unsnapped from.
     */
    public Figure getTarget() { return _target; }

    /**
     * Return a string representation of the ID.
     */
    private String idToString() {
        switch(getID()) {
        case CONNECTOR_DRAGGED:
            return "CONNECTOR_DRAGGED";
        case CONNECTOR_DROPPED:
            return "CONNECTOR_DROPPED";
        case CONNECTOR_SNAPPED:
            return "CONNECTOR_SNAPPED";
        case CONNECTOR_UNSNAPPED:
            return "CONNECTOR_UNSNAPPED";
        default:
            return "Invalid event ID";
        }
    }

    /**
     * Return a string representation of the endpoint.
     */
    private String endToString() {
        switch(getEnd()) {
        case HEAD_END:
            return "HEAD_END";
        case TAIL_END:
            return "TAIL_END";
        case BOTH_ENDS:
            return "BOTH_ENDS";
        default:
            return "Invalid end.";
        }
    }

    public String toString() {
        return "ConnectorEvent[" + idToString() + ", " + getConnector() + ", " + endToString() + "]";
    }
}


