/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

/**
 * This class makes it easy for an application to trap exceptions in AWT
 * event handlers.  It makes use of an unpublished hook in the sun event
 * dispatcher to trap those exceptions and call the application's showError
 * method.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 */
public class ApplicationExceptionHandler {
    // The application that needs exception handling.
    static Application _application = null;

    public void handle(Throwable t) {
        if(t instanceof Exception) {
            _application.showError("Exception Caught", (Exception)t);
        }
    }

    public static void setApplication(Application application) {
        _application = application;
        try {
            System.setProperty("sun.awt.exception.handler",
                               "diva.gui.ApplicationExceptionHandler");
        } catch (Exception ex) {
            // Ignore an errors that we get here..  which mean that we
            // aren't allowed to set that property.  Applets will just have
            // to deal with crappy messages.
        }
    }

    public static Application getApplication() {
        return _application;
    }
}


