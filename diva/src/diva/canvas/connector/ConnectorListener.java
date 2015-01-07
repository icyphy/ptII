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
 * @author John Reekie
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
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
