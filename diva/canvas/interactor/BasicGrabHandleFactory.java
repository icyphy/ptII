/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.interactor;

import diva.canvas.Site;

/**
 * A factory that creates basic grab-handles.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version        $Revision$
 */
public class BasicGrabHandleFactory implements GrabHandleFactory {
    /** Create a new basic grab-handle. This is a default
     * instance of BasicGrabHandle.
     */
    public GrabHandle createGrabHandle(Site s) {
        return new BasicGrabHandle(s);
    }
}


