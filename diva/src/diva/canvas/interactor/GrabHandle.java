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
package diva.canvas.interactor;

import diva.canvas.Figure;
import diva.canvas.Site;

/**
 * A grab handle for manipulating figures and so on. Grab-handles
 * are attached to Sites.
 *
 * @author John Reekie
 * @author Michael Shilman
 * @version        $Id$
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
    public float getSize();

    /**
     * Reposition the grab-handle to its site
     */
    public void relocate();

    /**
     * Set the site to which this grab-handle is attached.
     */
    public void setSite(Site s);

    /**
     * Set the "size" of the grab-handle.  The size is some dimension
     * that approximately represents the distance from the
     * attachment point to the edge.
     */
    public void setSize(float size);
}
