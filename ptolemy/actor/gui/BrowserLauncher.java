/* Launch the user's default web browser.

 Copyright (c) 2002-2015 The Regents of the University of California.
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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import ptolemy.util.StringUtilities;

/**
 BrowserLauncher is a class that provides one static method, openURL,
 which opens the default web browser for the current user of the system
 to the given URL.  It may support other protocols depending on the
 system -- mailto, ftp, etc. -- but that has not been rigorously tested
 and is not guaranteed to work.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class BrowserLauncher {
    /** Launch the browser on the first argument.  If there is
     *  no first argument, then open http://ptolemy.eecs.berkeley.edu.
     *  Second and subsequent arguments are ignored.
     *  It is best if the first argument is an absolute URL
     *  as opposed to a relative URL.
     *
     *  <p> For example, to open the user's default browser on
     *  http://www.eecs.berkeley.edu
     *  <pre>
     *  java -classpath $PTII ptolemy.actor.gui.BrowserLauncher http://www.eecs.berkeley.edu
     *  </pre>
     *  @param args An array of command line arguments.  The first
     *  argument names a URL to be opened.  If there is no first
     *  argument, then open http://ptolemy.eecs.berkeley.edu.  Second
     *  and subsequent arguments are ignored.
     *  @exception Exception If there is a problem launching the browser.
     */
    public static void main(String[] args) throws Exception {
        if (args.length >= 1) {
            // Ignore any arguments after the first one.
            BrowserLauncher.openURL(args[0]);
        } else {
            BrowserLauncher.openURL("http://ptolemy.eecs.berkeley.edu");
        }

        if (BrowserLauncher.delayExit) {
            System.out.println("Delaying exit for 10 seconds because we"
                    + "may have copied a jar: file");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }

            StringUtilities.exit(0);
        }
    }

    /** Set to true if we copied a file out of a jar file so that the
     *  browser could display it.  The reason we need this flag is
     *  that the system will delete the temporary file on exit, and
     *  after openURL() is called, this Java process will exit unless
     *  we delay.
     */
    public static boolean delayExit = false;

    /**
     * Attempts to open the default web browser to the given URL.
     *
     * <p> We use the following strategy to find URLs that may be inside
     * jar files:
     * <br> If the string does not start with "http": see if it is a
     * file.
     * <br> If the file cannot be found, look it up in the classpath.
     * <br> If the file can be found in the classpath then use the
     * found file instead of the given URL.
     * <br>If the file cannot be found in the classpath, then pass the
     * original given URL to the browser.
     * <p>If the ptolemy.ptII.browser property is set, then its value
     * is used as the value of the browser.
     * <br>To always use Internet Explorer, one might invoke Ptolemy
     * with:
     * <pre>
     * java -classpath $PTII -Dptolemy.ptII.browser=c:\\Program\ Files\\Internet\ Explorer\\iexplore.exe ptolemy.vergil.VergilApplication
     * </pre>
     * <p>To always use Firefox:
     * <pre>
     * java -classpath $PTII -Dptolemy.ptII.browser=c:\\Program\ Files\\Mozilla\ Firefox\\firefox ptolemy.vergil.VergilApplication
     * </pre>
     *
     * @param url The URL to open.
     *  It is best if the first argument is an absolute URL
     *  as opposed to a relative URL.
     * @exception IOException If the web browser could not be located or
     * does not run
     */
    public static void openURL(String url) throws IOException {
        if (!url.startsWith("http:") && !url.startsWith("https:")) {
            // If the url does not start with http:, then look it up
            // as a regular file and then possibly in the classpath.
            File urlFile = null;

            try {
                urlFile = new File(url);
            } catch (Exception ex) {
                // Ignored, we try to fix this below.
                urlFile = null;
            }

            if (urlFile == null || !urlFile.exists()) {
                // The file could not be found, so try the search path mambo.
                // We might be in the Swing Event thread, so
                // Thread.currentThread().getContextClassLoader()
                // .getResource(entry) probably will not work.
                String refClassName = "ptolemy.kernel.util.NamedObj";

                try {
                    Class refClass = Class.forName(refClassName);
                    URL entryURL = refClass.getClassLoader().getResource(url);

                    if (entryURL != null && !url.startsWith("jar:")) {
                        System.out.println("BrowserLauncher: Could not "
                                + "find '" + url + "', but '" + entryURL
                                + "' was found.");
                        url = entryURL.toString();
                    } else {
                        if (url.startsWith("jar:")) {
                            // If the URL begins with jar: then we are
                            // inside Web Start and we should get the
                            // resource, write it to a temporary file
                            // and pass that value to the browser
                            // Save the jar file as a temporary file
                            // in the default platform dependent
                            // directory with the same suffix as that
                            // of the jar URL
                            // FIXME: we should probably cache this
                            // copy somehow.
                            String old = url;
                            String temporaryURL = JNLPUtilities
                                    .saveJarURLInClassPath(url);

                            if (temporaryURL != null) {
                                url = temporaryURL;
                            } else {
                                url = JNLPUtilities.saveJarURLAsTempFile(url,
                                        "tmp", null, null);
                                delayExit = true;
                            }

                            System.out.println("BrowserLauncher: Could not "
                                    + "find '" + old + "', but jar url'" + url
                                    + "' was found.");
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    System.err.println("BrowserLauncher: Internal error, "
                            + " Could not find " + refClassName);
                }
            }
        }

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            URI uri = null;
            try {
                uri = new URI(url);
            } catch (Throwable throwable) {
                IOException exception = new IOException("Failed to convert url \"" + url
                                                        + "\" to a uri.");
                exception.initCause(throwable);
                throw exception;
            } 
	    boolean invokeByHand = false;
            try {
                desktop.browse(uri);
                return;
            } catch (IOException ex) {
		invokeByHand = true;
            } catch (UnsupportedOperationException ex2) {
		System.out.println("BrowserLauncher: UnsupportedOperation: " + uri + ": " + ex2);

		invokeByHand = true;
            } 

	    if (invokeByHand) {
                String errorMessage = "";
                try {
                    // Under Linux, desktop.browse() may fail with:

                    // java.lang.UnsupportedOperationException: The
                    // BROWSE action is not supported on the current
                    // platform!

                    // So, we start up Firefox.

		    String browser = "firefox";
		    String args[] = null;
		    String osName = System.getProperty("os.name");
		    if (osName.startsWith("Windows")) {
			browser = "cmd.exe";
			args = new String[] {
			    browser,
			    "/c",
			    "start",
			    "\"\"",
			    '"' + url + '"'
			};
			Process process = Runtime.getRuntime().exec(args);			
			errorMessage = "Command was: " + args[0] + " " + args[1] + " "
			    + args[2] + " " + args[3] + " " + args[4] + ". "
			    + "Under Windows check that the file named by " + url
			    + " is executable.";
			int exitCode = 0;
			try {
			    exitCode = process.waitFor();
			    process.exitValue();
			} catch (InterruptedException ie) {
			    throw new IOException("InterruptedException while "
						  + "launching " + browser + ": " + ie);
			}

			if (exitCode != 0) {
			    throw new IOException("Process exec'd by BrowserLauncher returned "
						  + exitCode + ". \nUrl was: " + url
						  + "\nBrowser was: " + errorMessage);
			}
			  
		    } else {
			browser = "firefox";
			args = new String[] {
			    browser,
			    "-remote",
			    "'openURL(" + url + ")'"
			};
			Process process = Runtime.getRuntime().exec(args);

			errorMessage = "Command was: " + args[0] + " " + args[1] + " "
			    + args[2];
                
			try {
			    // If Firefox is not open then try invoking the browser.
			    if (process.waitFor() != 0) {
				if (osName.startsWith("Mac OS X")) {
				    browser = "safari";
				}

				Runtime.getRuntime().exec(new String[] { browser, url });
			    }
			} catch (InterruptedException ie) {
			    throw new IOException("InterruptedException while "
						  + "launching " + browser + ": " + ie);
			}
			return;
		    }
		} catch (Throwable throwable) {
                    IOException exception = new IOException("Failed to open \"" + uri
                                                        + "\".  " + errorMessage);
                    exception.initCause(throwable);
                    throw exception;
                }

	    }
        } else {
            throw new IOException("java.awt.Desktop is not supported on this platform, so we can't open \""
                                  + url + "\"");
        }

    }

    /**
     * This class should be never be instantiated; this just ensures so.
     */
    private BrowserLauncher() {
    }
}
