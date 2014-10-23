/* A Graphical Message Handler Applet

 Copyright (c) 2009-2014 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */

package ptolemy.gui.test;

import javax.swing.UIManager;

import ptolemy.gui.BasicJApplet;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.VergilApplication;

///////////////////////////////////////////////////////////////////
//// GraphicalMessageHandlerApplet
/** An applet that bring up a toplevel, standalone Vergil frame.

 @author Christopher Brooks.
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class GraphicalMessageHandlerApplet extends BasicJApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Cleanup after execution of the model.  This method is called
     *  by the browser or appletviewer to inform this applet that
     *  it should clean up.
     */
    @Override
    public void destroy() {
        super.destroy();
        // Note: we used to call manager.terminate() here to get rid
        // of a lingering browser problem
        System.out
        .println("FIXME: Need to destroy GraphicalMessageHandlerApplet");
        stop();
    }

    /** Return a string describing this applet.
     *  @return A string describing the applet.
     */
    @Override
    public String getAppletInfo() {
        return "Ptolemy applet that brings up an error message "
                + VersionAttribute.CURRENT_VERSION
                + "\nPtolemy II comes from UC Berkeley, Department of EECS.\n"
                + "See http://ptolemy.eecs.berkeley.edu/ptolemyII"
                + "\n(Build: $Id$)";
    }

    /** Initialize the applet. This method is called by the browser
     *  or applet viewer to inform this applet that it has been
     *  loaded into the system. It is always called before
     *  the first time that the start() method is called.
     *  In this class, this invokes {@link VergilApplication#main(String[])}
     */
    @Override
    public void init() {
        super.init();
        try {
            // Setting the look and feel causes problems with applets
            // under JDK1.6.0_02 -> JDK1.6.0_13.
            // The exception is: Exception in thread "AWT-EventQueue-1" java.security.AccessControlException: access denied (java.io.FilePermission C:\WINDOWS\Fonts\TAHOMA.TTF read)
            // Unfortunately, it occurs well *after* the call below.
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to set look and feel.",
                    throwable);
        }
        try {
            java.util.Locale.setDefault(java.util.Locale.US);
        } catch (java.security.AccessControlException accessControl) {
            System.err.println("Warning, failed to set locale");
            accessControl.printStackTrace();
            // FIXME: If the application is run under Web Start, then this
            // exception will be thrown.
        }
        GraphicalMessageHandler handler = new GraphicalMessageHandler();
        MessageHandler.setMessageHandler(handler);
        Exception exception = new Exception("My Test Exception");
        MessageHandler.error("My Error Message.", exception);
    }

    /** Stop execution of the model. This method is called by the
     *  browser or applet viewer to inform this applet that it should
     *  stop its execution. It is called when the Web page
     *  that contains this applet has been replaced by another page,
     *  and also just before the applet is to be destroyed.
     *  In this base class, this method calls the stop() method
     *  of the manager. If there is no manager, do nothing.
     */
    @Override
    public void stop() {
        super.stop();
        System.out.println("FIXME: Need to stop GraphicalMessageHandlerApplet");
    }
}
