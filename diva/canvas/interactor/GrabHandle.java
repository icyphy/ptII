/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.interactor;

import diva.canvas.Site;
import diva.canvas.Figure;

/**
 * A grab handle for manipulating figures and so on. Grab-handles
 * are attached to Sites.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version        $Revision$
 */
public interface GrabHandle extends Figure {
    /**
     * Get the site to which this grab-handle is attached.
     */
    public Site getSite();

    /**
     * Get the "size" of the grab-handle. The size is some dimension
     * that approximately represents the distance from the
     * attachment point to the edge.
     */
    public float getSize ();

    /**
     * Reposition the grab-handle to its site
     */
    public void relocate ();

    /**
     * Set the site to which this grab-handle is attached.
     */
    public void setSite (Site s);

    /**
     * Set the "size" of the grab-handle.  The size is some dimension
     * that approximately represents the distance from the
     * attachment point to the edge.
     */
    public void setSize (float size);
}


