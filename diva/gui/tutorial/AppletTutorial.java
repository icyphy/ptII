/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.tutorial;

import diva.gui.AppletContext;

/**
 * A graph editor that runs as an applet.
 *
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class AppletTutorial extends AppletContext {
    public AppletTutorial() {
       new ApplicationTutorial(this);
    }
}







