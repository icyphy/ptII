/* Wrapper class to start up the DSP version of Vergil

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.applet;

import ptolemy.actor.gui.PtExecuteApplication;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// JNLPApplication
/** A wrapper class that calls eventually calls
ptolemy.actor.gui.PtExecuteApplicatio for use with Java Network
Launching Protocol (JNLP) aka Web Start.

<p>Note that under Web Start, it is easiest if each application needs
to have its own class and jar file containing the application class file.

<p>In Web Start 1.0.1, it is necessary to sign the application
if it is to have access to the local disk etc.  The way that this is
handled is that the .jnlp file that defines the application
is copied to the .jar file that defines the main() method for
the application and the .jar file is signed.  Unfortunately, this means
that two Web Start applications cannot share one jar file, so
we create these wrapper classes that call the appropriate main class.
<p>For more information about JNLP, see $PTII/mk/jnlp.in.

@see ptolemy.actor.gui.jnlp.MenuApplication

@author Christopher Hylands
@version $Id$
@since Ptolemy II 3.1
*/
public class JNLPApplication {
    public static void main(final String [] args) {
        // See the class comment of MenuApplication
        // about why we set the security manager to null.
        System.setSecurityManager(null);

        // If we were started from a menu choice instead of a command
        // line, then the current working directory is likely
        // somewhere odd, so set the current working directory (the
        // user.dir property) to the home directory of the user (the
        // user.home property)

        // Note that Java has a very poor notion of the current
        // directory and that changing user.dir will not necessarily
        // change the current directory for all aspects of Java.
        // In particular, the File class does not seems to always respect
        // the value of user.dir.  In general, changing user.dir
        // is frowned upon, but we do what we can here.

        if (_invokedFromAMenu()) {
            try {
                System.setProperty("user.dir",
                        StringUtilities.getProperty("user.home"));
            } catch (Exception ex) {
                // Don't crash here, just print a message and move on
                System.out.println("Warning, could not get user.home property "
                        + "or set user.dir property:");
                ex.printStackTrace();
            }
        }

	try {
            // NOTE: If there are problems with updates in the HTML widget,
            // see the comment in VergilApplication about threads.
            PtExecuteApplication application = new PtExecuteApplication(args);
            application.runModels();
            application.waitForFinish();
                            System.exit(0);
        } catch (Throwable throwable2) {
            // We are not likely to get here, but just to be safe
            // we try to print the error message and display it in a
            // graphical widget.
            _errorAndExit("Command failed", args, throwable2);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Print out an error message and stack trace on stderr and then
    // display a dialog box.  This method is used as a fail safe
    // in case there are problems with the configuration
    // We use a Throwable here instead of an Exception because
    // we might get an Error or and Exception. For example, if we
    // are using JNI, then we might get a java.lang.UnsatistifiedLineError,
    // which is an Error, not and Exception.
    private static void _errorAndExit(String message,
            String [] args, Throwable throwable) {
        StringBuffer argsBuffer =
            new StringBuffer("Command failed");

        if (args.length > 0) {
            argsBuffer.append("\nArguments: " + args[0]);
            for (int i = 1; i < args.length; i++) {
                argsBuffer.append(" " + args[i]);
            }
            argsBuffer.append("\n");
        }

        // First, print out the stack trace so that
        // if the next step fails the user has
        // a chance of seeing the message.
        System.out.println(argsBuffer.toString());
        throwable.printStackTrace();

        // Display the error message in a stack trace
        // If there are problems with the configuration,
        // then there is a chance that we have not
        // registered the GraphicalMessageHandler yet
        // so we do so now so that we are sure
        // the user can see the message.
        // One way to test this is to run vergil -conf foo

        MessageHandler.setMessageHandler(new GraphicalMessageHandler());

        MessageHandler.error(argsBuffer.toString(), throwable);

        System.exit(0);
    }

    // Return true if this command was invoked from a menu.
    private static boolean _invokedFromAMenu() {
        // Check for Web Start
        if (StringUtilities.getProperty("javawebstart.version").length() > 0) {
            return true;
        }
        if (StringUtilities.getProperty("lax.user.dir").length() > 0) {
            // If we are running under ZeroG's InstallAnywhere, then this
            // property will be present.
            return true;
        }
        return false;
    }
}
