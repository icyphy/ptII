/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;

/** A connector target that returns sites on the perimeter of a figure.
 *
 * @version $Revision$
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 */
public class PerimeterTarget extends AbstractConnectorTarget {
    /** Return the nearest site on the figure if the figure
     * is not a connector
     */
    public Site getHeadSite (Figure f, double x, double y) {
        if (!(f instanceof Connector)) {
            // FIXME: Need to generate unique ID per figure
            // FIXME: Need to actually return a useful site!
            return new PerimeterSite(f, 0);
        } else {
            return null;
        }
    }
}


