/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.FigureDecorator;
import diva.canvas.interactor.BasicGrabHandleFactory;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.GrabHandleFactory;

/**
 * A manipulator for arc connectors. In addition to the grab handles
 * at the ends of the connector, it attaches a handle in the center
 * of the connector so that the connector can be reshaped.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Edward A. Lee    (eal@eecs.berkeley.edu)
 * @version $Revision$
 */
public class ArcManipulator extends ConnectorManipulator {

    /** The handle at the midpoint of the arc.
     */
    private GrabHandle _midpointHandle;

    /**
     * Construct a new manipulator that uses rectangular grab-handles.
     */
    public ArcManipulator () {
        this(new BasicGrabHandleFactory());
    }

    /**
     * Construct a new manipulator using the given grab-handle factory.
     */
    public ArcManipulator(GrabHandleFactory f) {
        super(f);
        // Override the interactor set in the base class with a new one.
        setHandleInteractor(new ArcInteractor(this));
    }

    /** Create a new instance of this manipulator. The new
     * instance will have the same grab handle, and interactor
     * for grab-handles, as this one.
     */
    public FigureDecorator newInstance (Figure f) {
        ArcManipulator m = new ArcManipulator();
        m.setGrabHandleFactory(this.getGrabHandleFactory());
        m.setHandleInteractor(this.getHandleInteractor());
        return m;
    }

    /** Clear the current grab handles and create one for each of
     *  the head and tail sites, plus an additional one for the center
     *  of the arc.
     *  @param connector The connector.
     */
    protected void _createGrabHandles(Connector connector) {
        super._createGrabHandles(connector);
        if (!(connector instanceof ArcConnector)) {
            throw new IllegalArgumentException(
                    "ArcConnector required by ArcManipulator");
        }
        GrabHandleFactory factory = getGrabHandleFactory();
        _midpointHandle = factory.createGrabHandle(
                ((ArcConnector)connector).getMidpointSite());
        _midpointHandle.setParent(this);
        _midpointHandle.setInteractor(getHandleInteractor());
        addGrabHandle(_midpointHandle);
    }
}
