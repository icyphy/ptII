/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.connector;

/**
 * An adapter for connector listeners. It contains empty methods,
 * to make it easier to implement ConnectorListener.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class ConnectorAdapter implements ConnectorListener {
    /**
     * Do nothing when a connector end is dragged.
     */
    public void connectorDragged(ConnectorEvent e) {}

    /**
     * Do nothing when a connector end is dropped.
     */
    public void connectorDropped(ConnectorEvent e) {}

    /**
     * Do nothing when a connector end is snapped to
     * a possible target.
     */
    public void connectorSnapped(ConnectorEvent e) {}

    /**
     * Do nothing when a connector end is unsnapped from
     * the site that it was originally attached to,
     * or a possible target.
     */
    public void connectorUnsnapped(ConnectorEvent e) {}
}



