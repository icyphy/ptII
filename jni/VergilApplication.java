/* An application for editing ptolemy models visually.

Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package jni;

// Ptolemy imports
import java.net.URL;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.VergilErrorHandler;

// Java imports

//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
   This application opens run control panels for models specified on the
   command line.  The exact facilities that are available are determined
   by the configuration file ptolemy/configs/vergilConfiguration.xml,
   which is loaded before any command-line arguments are processed.
   If there are no command-line arguments at all, then the configuration
   file is augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.

   @author Edward A. Lee and Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 1.0
   @see ptolemy.actor.gui.ModelFrame
   @see ptolemy.actor.gui.RunTableau
*/
public class VergilApplication extends MoMLApplication {

    /** Parse the specified command-line arguments, creating models
     *  and frames to interact with them.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public VergilApplication(String args[]) throws Exception {
        super(args);

        // Create register an error handler with the parser so that
        // MoML errors are tolerated more than the default.
        MoMLParser.setErrorHandler(new VergilErrorHandler());
        java.util.Locale.setDefault(java.util.Locale.US);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            new VergilApplication(args);
        } catch (Exception ex) {
            MessageHandler.error("Command failed", ex);
            System.exit(0);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a default Configuration, which in this case is given by
     *  the MoML file ptolemy/configs/vergilConfiguration.xml.
     *  @return A default configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createDefaultConfiguration() throws Exception {
        URL configurationURL =
            new URL("ptolemy/configs/jni/configuration.xml");
        return _readConfiguration(configurationURL);
    }

    /** Return a default Configuration to use when there are no command-line
     *  arguments, which in this case is given by the default configuration
     *  augmented by the MoML file ptolemy/configs/vergilWelcomeWindow.xml.
     *  @return A configuration for when there no command-line arguments.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        Configuration configuration = _createDefaultConfiguration();

        // FIXME: This code is Dog slow for some reason.
        URL inurl = specToURL("ptolemy/configs/vergilWelcomeWindow.xml");
        _parser.reset();
        _parser.setContext(configuration);
        _parser.parse(inurl, inurl);
        Effigy doc = (Effigy)configuration.getEntity("directory.doc");
        URL idurl = specToURL("ptolemy/configs/intro.htm");
        doc.identifier.setExpression(idurl.toExternalForm());
        return configuration;
    }

    /** Parse the command-line arguments. This overrides the base class
     *  only to set the usage information.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(final String args[]) throws Exception {
        _commandTemplate = "vergil [ options ] [file ...]";
        // NOTE: Java superstition dictates that if you want something
        // to work, you should invoke it in event thread.  Otherwise,
        // weird things happens at the user interface level.  This
        // seems to prevent occasional errors rending HTML.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        VergilApplication.super._parseArgs(args);
                    } catch (Exception ex) {
                        MessageHandler.error("Command failed", ex);
                        System.exit(0);
                    }
                }
            });
    }
}
