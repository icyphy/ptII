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
package diva.gui;


/**
 * This class makes it easy for an application to trap exceptions in AWT
 * event handlers.  It makes use of an unpublished hook in the sun event
 * dispatcher to trap those exceptions and call the application's showError
 * method.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class ApplicationExceptionHandler {
    // The application that needs exception handling.
    static Application _application = null;

    public void handle(Throwable t) {
        if (t instanceof Exception) {
            _application.showError("Exception Caught", (Exception) t);
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
