/* An application that reads one or more files specified on the command line.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import javax.swing.UIManager;

import ptolemy.moml.ErrorHandler;
import ptolemy.moml.SimpleErrorHandler;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// MoMLApplication

/**
 An application that sets the look and feel to the native look
 and feel and then reads one or more
 files specified on the command line, or instantiates one or
 more Java classes specified by the -class option.

 <p>This class sets the look and feel of the user interface (UI) to
 the native look and feel.  Thus, this class invokes Java's user
 interface code which means that this class should be run in an
 environment that has a display.  To run in a environment
 that has no display, see the {@link ptolemy.actor.gui.ConfigurationApplication}
 parent class.

 <p>For complete usage, see the {@link ptolemy.actor.gui.ConfigurationApplication}
 parent class.

 @author Edward A. Lee and Steve Neuendorffer, Contributor: Christopher Hylands
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see Configuration
 */
public class MoMLApplication extends ConfigurationApplication {
    /** Parse the specified command-line arguments, instantiating classes
     *  and reading files that are specified.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public MoMLApplication(String[] args) throws Exception {
        this("ptolemy/configs", args);
        MessageHandler.setMessageHandler(new ActorGraphicalMessageHandler());
    }

    /** Parse the specified command-line arguments, instantiating classes
     *  and reading files that are specified.
     *  @param basePath The basePath to look for configurations
     *  in, usually "ptolemy/configs", but other tools might
     *  have other configurations in other directories
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public MoMLApplication(String basePath, String[] args) throws Exception {
        this(basePath, args, new ActorGraphicalMessageHandler(),
                new SimpleErrorHandler());
    }

    /** Parse the specified command-line arguments, instantiating classes
     *  and reading files that are specified.
     *  @param basePath The basePath to look for configurations
     *  in, usually "ptolemy/configs", but other tools might
     *  have other configurations in other directories
     *  @param args The command-line arguments.
     *  @param messageHandler The message handler.
     *  @param errorHandler The MoML error handler.
     *  @exception Exception If command line arguments have problems.
     */
    public MoMLApplication(String basePath, String[] args,
            MessageHandler messageHandler, ErrorHandler errorHandler)
            throws Exception {
        super(basePath, args, messageHandler, errorHandler);
    }

    /** Set the look and feel to the native look and feel.
     *  This method is called by early in the constructor,
     *  so the object may not be completely constructed, so
     *  derived classes should not access fields from a
     *  parent class.
     */
    @Override
    protected void _initializeApplication() {
        // The Java look & feel is pretty lame, so we use the native
        // look and feel of the platform we are running on.
        // NOTE: This creates the only dependence on Swing in this
        // class.  Should this be left to derived classes?
        try {
            // Setting the look and feel causes problems with applets
            // under JDK1.6.0_02 -> JDK1.6.0_13.
            // The exception is: Exception in thread "AWT-EventQueue-1" java.security.AccessControlException: access denied (java.io.FilePermission C:\WINDOWS\Fonts\TAHOMA.TTF read)
            // Unfortunately, it occurs well *after* the l&f is set.
            String javaVersion = StringUtilities.getProperty("java.version");
            if (javaVersion.compareTo("1.6.0") > 0
                    && javaVersion.compareTo("1.6.0_14") < 0
                    && StringUtilities.inApplet()) {
                System.out.println("Warning: skipping setting the look and "
                        + "feel in Java version " + javaVersion
                        + " because it causes problems under applets under "
                        + "Java 1.6.0_02 through 1.6.0_13.");
            } else {
                UIManager.setLookAndFeel(UIManager
                        .getSystemLookAndFeelClassName());
            }
        } catch (Throwable throwable) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }
    }
}
