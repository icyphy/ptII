/*
 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.homer;

import java.io.IOException;
import java.net.URL;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.homer.gui.HomerMainFrame;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.VergilErrorHandler;

///////////////////////////////////////////////////////////////////
//// HomerApplication

/** The UI designer application responsible for handling configuration and
 *  opening the main frame container.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class HomerApplication extends MoMLApplication {

    /** Initialize the platform injection framework.
     */
    static {
        ActorModuleInitializer.initializeInjector();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Parse the command-line arguments and configuration.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public HomerApplication(String[] args) throws Exception {
        super("ptolemy/configs", args);
        MoMLParser.setErrorHandler(new VergilErrorHandler());

        _frame = new HomerMainFrame(this);
        _frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(final String[] args) {
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        new HomerApplication(args);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });
        } catch (Throwable ex) {
            MessageHandler.error("Command failed", ex);
            StringUtilities.exit(0);
        }
    }

    /** Get the local configuration of the application.
     *  @return Get the default configuration.
     */
    public Configuration getConfiguration() {
        return _configuration;
    }

    /** Return the HomerMainFrame.
     *  @return The HomerMainFrame.
     */
    public HomerMainFrame getHomerMainFrame() {
        return _frame;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the full default application configuration.
     *  @return The default application configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    @Override
    protected Configuration _createDefaultConfiguration() throws Exception {
        URL configurationURL = null;
        try {
            configurationURL = specToURL(_basePath + "/full/configuration.xml");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Configuration configuration = super._createDefaultConfiguration();
        try {
            configuration = readConfiguration(configurationURL);
        } catch (Exception ex) {
            throw new Exception("Failed to read configuration '"
                    + configurationURL + "'", ex);
        }

        return configuration;
    }

    /** Return the full default configuration.
     *  @return The default application configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    @Override
    protected Configuration _createEmptyConfiguration() throws Exception {
        return _createDefaultConfiguration();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The frame that is created by this class. */
    HomerMainFrame _frame;
}
