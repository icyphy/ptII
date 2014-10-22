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

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import diva.canvas.CanvasPane;
import diva.canvas.Figure;
import diva.canvas.FigureContainer;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.Manipulator;
import diva.util.Filter;
import diva.util.java2d.ShapeUtilities;

/** An interactor for dragging one end of a connector. This is a
 * utility class designed for use in conjunction with
 * ConnectorManipulator.
 *
 * @version $Id$
 * @author John Reekie
 * @author Steve Neuendorffer
 */
public class ConnectorInteractor extends DragInteractor {
    /** The connector
     */
    private Connector _connector = null;

    /** The handle being grabbed
     */
    private GrabHandle _handle = null;

    /** The manipulator this interactor belongs to
     */
    private ConnectorManipulator _manipulator;

    /** The current target object if we are over one, else null
     */
    private Figure _target = null;

    /** The list of connector listeners
     */
    private ArrayList<ConnectorListener> _connectorListeners = new ArrayList<ConnectorListener>();

    /** Create a new interactor to be used with the given
     * manipulator
     */
    public ConnectorInteractor(ConnectorManipulator m) {
        _manipulator = m;
    }

    /**
     * Add a connector listener. The listener will be notified
     * on all significant events.
     */
    public void addConnectorListener(ConnectorListener l) {
        _connectorListeners.add(l);
    }

    /** Detach the connector from its current site and attach
     * it to the given site.
     */
    private void attach(Site site) {
        if (_handle.getSite() == _connector.getHeadSite()) {
            _connector.setHeadSite(site);
        } else {
            _connector.setTailSite(site);
        }

        _handle.setSite(site);
        _handle.repaint();
    }

    /** Print debug message
     */
    //private void debug(Object o) {
    //    System.out.println("ConnectorInteractor: " + o);
    //}
    /** Detach the connector from its current site and attach
     * it to a new autonomous site at the given coordinates
     */
    private void detach(double x, double y) {
        Site newSite = new AutonomousSite(_connector.getTransformContext(), x,
                y);

        if (_handle.getSite() == _connector.getHeadSite()) {
            _connector.setHeadSite(newSite);
        } else {
            _connector.setTailSite(newSite);
        }

        _handle.setSite(newSite);
        _handle.repaint();
    }

    /** Utility function to find a site. Takes account of whether
     * we are dragging the head or the tail. Don't find a site if
     * the figure is the same as at the already-connected end of
     * the connector. (<b>Note</b>: this needs to be parameterized, as
     * sometimes snapping to a site on the same figure is useful.)
     */
    private Site findSite(Figure f, double x, double y) {
        Site ret = null;

        if (_handle.getSite() == _connector.getHeadSite()) {
            ret = _manipulator._connectorTarget
                    .getHeadSite(_connector, f, x, y);
        } else {
            ret = _manipulator._connectorTarget
                    .getTailSite(_connector, f, x, y);
        }

        return ret;
    }

    /** Utility function to find a site. Takes account of whether
     * we are dragging the head or the tail.
     */
    private Site findSite(Site s, double x, double y) {
        if (_handle.getSite() == _connector.getHeadSite()) {
            return _manipulator._connectorTarget.getHeadSite(_connector, s, x,
                    y);
        } else {
            return _manipulator._connectorTarget.getTailSite(_connector, s, x,
                    y);
        }
    }

    /** Fire a connector event to all connector listeners.
     */
    protected void fireConnectorEvent(int id) {
        int end;

        if (_handle.getSite() == _connector.getHeadSite()) {
            end = ConnectorEvent.HEAD_END;
        } else {
            end = ConnectorEvent.TAIL_END;
        }

        ConnectorEvent event = new ConnectorEvent(id, _connector.getLayer(),
                _target, _connector, end);
        _notifyConnectorListeners(event, id);
    }

    /** Get the current connector. If there isn't one, return
     * null.
     */
    public Connector getConnector() {
        return _connector;
    }

    /** Get the current grab handle.
     */
    public GrabHandle getHandle() {
        return _handle;
    }

    /** Get the current target figure. If there isn't one, return
     * null.
     */
    public Figure getTarget() {
        return _target;
    }

    /** Handle a mouse-released event. This overrides the inherited
     * method to generate a connector-dropped event.
     */
    @Override
    public void mouseReleased(LayerEvent event) {
        super.mouseReleased(event);
        fireConnectorEvent(ConnectorEvent.CONNECTOR_DROPPED);
    }

    /**
     * Remove a connector listener.
     */
    public void removeConnectorListener(ConnectorListener l) {
        _connectorListeners.remove(l);
    }

    /** Initialize the interactor when a grab-handle
     * is grabbed.
     */
    @Override
    public void setup(LayerEvent e) {
        _handle = (GrabHandle) e.getFigureSource();

        // Get the "target" which is the figure this end of
        // the connector is connected to
        _target = _handle.getSite().getFigure();

        // This is really nasty...
        _connector = (Connector) ((Manipulator) _handle.getParent()).getChild();
    }

    /** Pick a site using the connector target and
     *  then snap to it.  Container is the container to
     *  pick in; x, y are the coordinates of the drag
     *  point in the transform context of that container.
     */
    public void snapToSite(final FigureContainer container,
            final Rectangle2D hitRect) {
        //debug("SNAPPING TO SITE IN: " + container);
        Figure figure = container.pick(hitRect, new Filter() {
            @Override
            public boolean accept(Object o) {
                // debug("checking = " + o);
                if (!(o instanceof Figure)) {
                    return false;
                }

                if (o instanceof ConnectorManipulator) {
                    return false;
                }

                Figure f = (Figure) o;

                if (f.getInteractor() == null) {
                    return false;
                }

                TransformContext figureContext = f.getParent()
                        .getTransformContext();
                TransformContext containerContext = container
                        .getTransformContext();
                AffineTransform transform;

                try {
                    transform = figureContext.getTransform(containerContext)
                            .createInverse();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }

                Rectangle2D bounds = ShapeUtilities.transformBounds(hitRect,
                        transform);

                if (findSite(f, bounds.getCenterX(), bounds.getCenterY()) == null) {
                    return false;
                }

                return true;
            }
        });

        if (figure != null) {
            Site snap = findSite(figure, hitRect.getCenterX(),
                    hitRect.getCenterY());

            if (snap != null) {
                _target = figure;
                attach(snap);
                fireConnectorEvent(ConnectorEvent.CONNECTOR_SNAPPED);
            }
        }
    }

    /** Respond to translation of the grab-handle. Move the
     * grab-handle, and adjust the connector accordingly,
     * snapping it to a suitable target if possible.
     */
    @Override
    public void translate(LayerEvent e, double dx, double dy) {
        double x = e.getLayerX();
        double y = e.getLayerY();

        double h = _manipulator.getSnapHalo();
        Rectangle2D mouseRect = new Rectangle2D.Double(x - h, y - h, 2 * h,
                2 * h);

        // If we were over a target, see if we still are
        if (_target != null) {
            // set up affine transform between the target
            // and our current transform context, so that
            // all tests { intersects(), findSite() }
            // are done in proper coordinates
            TransformContext manipulatorContext = e.getLayerSource()
                    .getTransformContext();
            TransformContext targetContext = _target.getParent()
                    .getTransformContext();
            AffineTransform transform = targetContext
                    .getTransform(manipulatorContext);
            Rectangle2D bounds = _target.getBounds();
            bounds = ShapeUtilities.transformBounds(bounds, transform);

            if (bounds.intersects(x - h, y - h, 2 * h, 2 * h)) {
                // We're still over, so snap to the nearest site -- FIXME
                //                debug("STILL OVER SAME TARGET: " + _target);
                Site current = _handle.getSite();

                // FIXME: shouldn't this be x,y?
                Site snap = findSite(current, bounds.getCenterX(),
                        bounds.getCenterY());

                //debug("SNAP: " + snap);
                if (snap != null && snap != current) {
                    attach(snap);
                    fireConnectorEvent(ConnectorEvent.CONNECTOR_SNAPPED);
                }
            } else {
                // Not over any more, so leave
                //  debug("NOT OVER SAME TARGET: " + _target);
                detach(x, y);
                fireConnectorEvent(ConnectorEvent.CONNECTOR_UNSNAPPED);
                _target = null;
            }
        } else {
            //debug("NOT OVER ANY TARGET: " + _target);
            // Look for a target. The container we need to pick
            // in is the one that has a transform context
            // FIXME: this doesn't deal properly with hierarchy
            FigureContainer container;
            Object component = _connector.getTransformContext().getComponent();

            if (component instanceof CanvasPane) {
                container = (FigureContainer) _connector.getLayer();
            } else {
                container = (FigureContainer) component;
            }

            snapToSite(container, mouseRect);
        }

        // If the target if still null, we didn't get one, so
        // move the (autonomous) site, but signal a mouse drag.
        if (_target == null) {
            _handle.getSite().translate(dx, dy);
            _connector.reroute();
            fireConnectorEvent(ConnectorEvent.CONNECTOR_DRAGGED);
        }
    }

    /** Notify registered connector listeners of the specified event.
     *  @param event The event.
     *  @param id The id of the event (dragged, dropped, etc.).
     */
    protected void _notifyConnectorListeners(ConnectorEvent event, int id) {
        for (ConnectorListener l : _connectorListeners) {
            switch (id) {
            case ConnectorEvent.CONNECTOR_DRAGGED:
                l.connectorDragged(event);
                break;

            case ConnectorEvent.CONNECTOR_DROPPED:
                l.connectorDropped(event);
                break;

            case ConnectorEvent.CONNECTOR_SNAPPED:
                l.connectorSnapped(event);
                break;

            case ConnectorEvent.CONNECTOR_UNSNAPPED:
                l.connectorUnsnapped(event);
                break;
            }
        }
    }
}
