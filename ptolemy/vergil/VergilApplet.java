/* A Vergil Applet

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

package ptolemy.vergil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ptolemy.actor.gui.Configuration;
import ptolemy.gui.BasicJApplet;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// VergilApplet
/** An applet that bring up a toplevel, standalone Vergil frame.

 @author Christopher Brooks.
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class VergilApplet extends BasicJApplet {
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
        stop();
    }

    /** Return a string describing this applet.
     *  @return A string describing the applet.
     */
    @Override
    public String getAppletInfo() {
        return "Ptolemy applet that displays Ptolemy II models using Vergil "
                + VersionAttribute.CURRENT_VERSION
                + "\nPtolemy II comes from UC Berkeley, Department of EECS.\n"
                + "See http://ptolemy.eecs.berkeley.edu/ptolemyII"
                + "\n(Build: $Id$)";
    }

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    @Override
    public String[][] getParameterInfo() {
        String[][] newinfo = { { "commandLineArguments", "",
        "Command Line Arguments suitable for VergilApplication" }, };
        return _concatStringArrays(super.getParameterInfo(), newinfo);
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
        String commandLineArguments = getParameter("commandLineArguments");
        String[] vergilArguments = new String[0];
        if (commandLineArguments != null) {
            try {
                vergilArguments = StringUtilities
                        .tokenizeForExec(commandLineArguments);
            } catch (IOException ex) {
                report("Failed to parse \"" + commandLineArguments + "\"", ex);
            }
        }
        int i = 0;
        for (; i < vergilArguments.length; i++) {
            if (vergilArguments[i].endsWith(".xml")) {
                URL docBase = getDocumentBase();
                try {
                    URL xmlFile = new URL(docBase, vergilArguments[i]);

                    try {
                        // Try to open the URL, if it can't be opened, try from the codebase.
                        URLConnection connection = xmlFile.openConnection();
                        if (connection instanceof HttpURLConnection) {
                            HttpURLConnection httpConnection = (HttpURLConnection) connection;
                            httpConnection.setRequestMethod("HEAD");
                            if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                xmlFile = new URL(vergilArguments[i]);
                            }
                        } else {
                            if (xmlFile.getProtocol().equals("file")) {
                                File urlFile = new File(xmlFile.getPath());
                                if (!urlFile.exists()) {
                                    xmlFile = new URL(getCodeBase(),
                                            vergilArguments[i]);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("Failed to open "
                                + vergilArguments[i]);
                        ex.printStackTrace();
                    }
                    vergilArguments[i] = xmlFile.toExternalForm();
                } catch (MalformedURLException ex) {
                    report("Failed to open \"" + vergilArguments[i] + "\"", ex);
                }
            }
        }
        VergilApplication.main(vergilArguments);
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
        try {
            Configuration.closeAllTableaux();
        } catch (IllegalActionException ex) {
            ex.printStackTrace();
        }
    }
}
