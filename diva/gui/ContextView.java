/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

/**
 * A Context view is a view that knows about an app context that it exists.
 * This context allows the view to represent itself as a toplevel frame
 * complete with menu bars, tool bars, etc.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public interface ContextView {
    /** Return the component that implements the display of this view.
     */
    public AppContext getContext ();
}


