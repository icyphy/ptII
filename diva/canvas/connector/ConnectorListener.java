/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.connector;

/**
 * A listener for changes in a connector's connectivity.
 * Listeners register with the ConnectorManipulator (?)
 * and get called back as the user manipulates a connector.
 *
 * <p>
 * Calls for disconnecting and reconnecting:
 *
 * <ol>
 * <li> unsnapped
 * <li> drag drag drag
 * <li> snapped
 * <li> released
 * </ol>
 *
 * or for disconnecting:
 * <ol>
 * <li> unsnapped
 * <li> drag drag drag
 * <li> released
 * </ol>
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface ConnectorListener extends java.util.EventListener {
    /**
     * Called when a connector end is dragged. If currently
     * over a target (and regardless of whether the connector
     * is snapped to that target), the source field will be
     * non-null.
     */
    public void connectorDragged(ConnectorEvent e);

    /**
     * Called when a connector end is dropped.   If the connector
     * is currently snapped to a target, the target can be obtained
     * from the event as the source field.
     */
    public void connectorDropped(ConnectorEvent e);

    /**
     * Called when a connector end is snapped to
     * a possible target. The target can be obtained
     * as the source field of the event.
     */
    public void connectorSnapped(ConnectorEvent e);

    /**
     * Called when a connector end is unsnapped from
     * the site that it was originally attached to,
     * or a possible target. The figure that it was
     * unsnapped from is the source field of the event.
     */
    public void connectorUnsnapped(ConnectorEvent e);
}



