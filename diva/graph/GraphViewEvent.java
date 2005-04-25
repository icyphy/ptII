/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package diva.graph;


/**
 * A graph view event that is emitted when anything interesting happens
 * inside a graph view. The source of a event
 * is the GraphController that issued the event.
 *
 * <p> Each graph event contains an ID and a previous value for all
 * changes and deletions, the specifics of which are described on a
 * case-by-case basis below.
 *
 * @see GraphController
 * @author Michael Shilman
 * @author Steve Neuendorffer
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class GraphViewEvent extends java.util.EventObject {
    /**
     * The figure representing a node was moved.
     */
    public static final int NODE_MOVED = 11;

    /**
     * The connector representing an edge was just routed.
     */
    public static final int EDGE_ROUTED = 12;

    /**
     * The given node was just drawn.
     */
    public static final int NODE_DRAWN = 20;

    /**
     * The given edge was just drawn.
     */
    public static final int EDGE_DRAWN = 21;

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
     * Construct a GraphViewEvent with the given source and target
     * and a <i>null</i> previous value.
     */
    public GraphViewEvent(Object source, int id, Object target) {
        this(source, id, target, null);
    }

    /**
     * Construct a GraphViewEvent with the given
     * source, target, and previous value.
     */
    public GraphViewEvent(Object source, int id, Object target, Object oldValue) {
        super(source);
        _id = id;
        _target = target;
        _oldValue = oldValue;
    }

    /**
     * Return the type id for this event.
     */
    public int getID() {
        return _id;
    }

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
        switch (getID()) {
        case NODE_MOVED:
            return "NODE_MOVED";

        case EDGE_ROUTED:
            return "EDGE_ROUTED";

        case NODE_DRAWN:
            return "NODE_DRAWN";

        case EDGE_DRAWN:
            return "EDGE_DRAWN";

        default:
            return "Invalid event ID";
        }
    }

    /**
     * Return a string representation of this event.
     */
    public String toString() {
        return "GraphViewEvent[" + idToString() + ", " + getTarget() + ", "
        + getOldValue() + "]";
    }
}
