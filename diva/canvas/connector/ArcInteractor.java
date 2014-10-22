/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 *
 */
package diva.canvas.connector;

import java.awt.geom.Point2D;

import diva.canvas.Site;
import diva.canvas.event.LayerEvent;

/** An interactor for dragging either end of an arc connector and for
 *  for altering the shape of the arc by dragging a midpoint grab handle.
 *  This class is designed for use in conjunction with ArcManipulator.
 *
 * @version $Id$
 * @author Edward A. Lee
 */
public class ArcInteractor extends ConnectorInteractor {
    /** Create a new interactor to be used with the given manipulator.
     */
    public ArcInteractor(ArcManipulator m) {
        super(m);
    }

    /** Fire a connector event to all connector listeners.
     */
    @Override
    protected void fireConnectorEvent(int id) {
        // NOTE: The following cast is safe because the method that
        // creates grab handles in ArcManipulator ensures that the
        // connector is an instance of ArcConnector.
        ArcConnector connector = (ArcConnector) getConnector();
        Site site = getHandle().getSite();
        int end;

        if (site == connector.getTailSite()) {
            end = ConnectorEvent.TAIL_END;
        } else if (site == connector.getMidpointSite()) {
            end = ConnectorEvent.MIDPOINT;
        } else {
            // Default is head site.
            end = ConnectorEvent.HEAD_END;
        }

        ConnectorEvent event = new ConnectorEvent(id, connector.getLayer(),
                getTarget(), connector, end);
        _notifyConnectorListeners(event, id);
    }

    /** Respond to translation of the grab-handle. Move the
     *  grab-handle, and adjust the connector accordingly,
     *  snapping it to a suitable target if possible.
     */
    @Override
    public void translate(LayerEvent e, double dx, double dy) {
        // NOTE: The following cast is safe because the method that
        // creates grab handles in ArcManipulator ensures that the
        // connector is an instance of ArcConnector.
        ArcConnector connector = (ArcConnector) getConnector();
        Site site = getHandle().getSite();

        // Process movement in one of the end manipulators
        if (site != connector.getMidpointSite()) {
            super.translate(e, dx, dy);
        } else {
            // Process movement of the mid-point manipulator. The
            // distance we want to tell the connector to move, is
            // the difference between where it is now, and where
            // we think it should be -- this is because of the
            // inexactness of ArcConnector.translateMidpoint().
            //
            double targetX = getX() + dx;
            double targetY = getY() + dy;

            Point2D mid = connector.getArcMidpoint();

            double newdx = targetX - mid.getX();
            double newdy = targetY - mid.getY();

            // Apply a couple of limiting functions to this, to avoid
            // "yoyo-ing"
            if (newdx > 0 && dx < 0 || newdx < 0 && dx > 0) {
                newdx = 0;
            }

            if (newdy > 0 && dy < 0 || newdy < 0 && dy > 0) {
                newdy = 0;
            }

            double limit = 25.0;

            if (newdx > limit) {
                newdx = limit;
            } else if (newdx < -limit) {
                newdx = -limit;
            }

            if (newdy > limit) {
                newdy = limit;
            } else if (newdy < -limit) {
                newdy = -limit;
            }

            // Tell the connector to move its midpoint
            connector.translateMidpoint(newdx, newdy);

            //connector.translateMidpoint(dx, dy); // This one is "open-loop"
            connector.reroute();
        }

        fireConnectorEvent(ConnectorEvent.CONNECTOR_DRAGGED);
    }
}
