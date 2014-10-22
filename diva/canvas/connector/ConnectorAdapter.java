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

/**
 * An adapter for connector listeners. It contains empty methods,
 * to make it easier to implement ConnectorListener.
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class ConnectorAdapter implements ConnectorListener {
    /**
     * Do nothing when a connector end is dragged.
     */
    @Override
    public void connectorDragged(ConnectorEvent e) {
    }

    /**
     * Do nothing when a connector end is dropped.
     */
    @Override
    public void connectorDropped(ConnectorEvent e) {
    }

    /**
     * Do nothing when a connector end is snapped to
     * a possible target.
     */
    @Override
    public void connectorSnapped(ConnectorEvent e) {
    }

    /**
     * Do nothing when a connector end is unsnapped from
     * the site that it was originally attached to,
     * or a possible target.
     */
    @Override
    public void connectorUnsnapped(ConnectorEvent e) {
    }
}
