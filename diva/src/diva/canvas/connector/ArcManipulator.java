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
 * @author John Reekie
 * @author Edward A. Lee
 * @version $Id$
 */
public class ArcManipulator extends ConnectorManipulator {
    /** The handle at the midpoint of the arc.
     */
    private GrabHandle _midpointHandle;

    /**
     * Construct a new manipulator that uses rectangular grab-handles.
     */
    public ArcManipulator() {
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
    @Override
    public FigureDecorator newInstance(Figure f) {
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
    @Override
    protected void _createGrabHandles(Connector connector) {
        super._createGrabHandles(connector);

        if (!(connector instanceof ArcConnector)) {
            throw new IllegalArgumentException(
                    "ArcConnector required by ArcManipulator");
        }

        GrabHandleFactory factory = getGrabHandleFactory();
        _midpointHandle = factory.createGrabHandle(((ArcConnector) connector)
                .getMidpointSite());
        _midpointHandle.setParent(this);
        _midpointHandle.setInteractor(getHandleInteractor());
        addGrabHandle(_midpointHandle);
    }
}
