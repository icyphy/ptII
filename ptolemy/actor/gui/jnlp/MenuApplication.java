/* Wrapper to start up Ptolemy from a Menu choice.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.actor.gui.jnlp;

import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// MenuApplication

/** A wrapper that starts up Vergil from a menu.

 <p>MenuApplication is primarily for use with the
 Java Network Launching Protocol
 (JNLP) aka Web Start, but it can be used with any system that invokes
 applications via a menu choice such as the InstallAnywhere lax tool.

 <p>If MenuApplication detects that it was invoked from a menu, then
 it sets the current directory to the value of user's home directory
 so that when the user opens a file chooser to safe a file then
 the initial default directory is the user's home directory
 instead of the application directory

 <p>The security manager is set to null for two reasons:
 <ol>
 <li> Get rid of the following message when we open the file browser:
 <br> "There is no disk in the drive. Please insert a disk into drive A"
 with the standard Abort/Retry/Ignore buttons.
 See:
 <a href="http://forum.java.sun.com/thread.jsp?forum=38&thread=71610">http://forum.java.sun.com/thread.jsp?forum=38&thread=71610</a>

 <li> Speed things up, see
 <a href="http://forums.java.sun.com/thread.jsp?forum=38&thread=134393">http://forums.java.sun.com/thread.jsp?forum=38&thread=134393</a>
 </ol>


 <p>Note that in Web Start 1.0.1, it is necessary to sign the application
 if it is to have access to the local disk etc.  The way that this is
 handled is that the .jnlp file that defines the application
 is copied to the .jar file that defines the main() method for
 the application and the .jar file is signed.  Unfortunately, this means
 that two Web Start applications cannot share one jar file, so
 we create wrappers that call the appropriate main method.

 <p>For more information about JNLP, see $PTII/mk/jnlp.in.

 <p>Each JNLP Application should extend MenuApplication and simply
 have its main() call this main().  The makefile will need
 to be extended to create a jar file that includes
 <pre>
 MenuApplication.class
 <i>Foo</i>Application.class
 </pre>

 @see ptolemy.vergil.VergilApplication

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class MenuApplication {
    /** Main method that sets user.dir as necessary.
     * @param args Arguments to be passed on to VergilApplication.main()
     */
    public static void main(final String[] args) {
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
            } catch (SecurityException ex) {
                System.out
                        .println("Warning: MenuApplication: Could not get user.home property "
                                + "or set user.dir property. (-sandbox always causes this)");
            } catch (Exception ex) {
                // Don't crash here, just print a message and move on
                ex.printStackTrace();
            }
        }

        // Note that VergilApplication.main() invokes the VergilApplication
        // constructor in the Swing Event thread.
        ptolemy.vergil.VergilApplication.main(args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
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
