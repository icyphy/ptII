/* Base class for swing applets.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.gui;

// Java imports.
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import javax.swing.JApplet;
import javax.swing.JOptionPane;

//////////////////////////////////////////////////////////////////////////
//// BasicJApplet
/**
Base class for swing applets.  This class provides basic management
for background colors, a standardized mechanism for reporting errors
and exceptions, and a minimal amount of information about the
applet.

@author  Edward A. Lee
@version $Id$
*/
public class BasicJApplet extends JApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return generic applet information.
     *  @return A string giving minimal information about Ptolemy II.
     */
    public String getAppletInfo() {
        return "Ptolemy II swing-based applet.\n" +
            "Ptolemy II comes from UC Berkeley, Department of EECS.\n" +
            "See http://ptolemy.eecs.berkeley.edu/ptolemyII";
    }

    /** Describe the applet parameters. Derived classes should override
     *  this and append their own parameters.  The protected method
     *  _concatStringArrays() is provided to make this easy to do.
     *  @return An array describing the applet parameters.
     */
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            {"background",    "#RRGGBB",    "color of the background"},
        };
        return pinfo;
    }

    /** Initialize the applet. This method is called by the browser
     *  or applet viewer to inform this applet that it has been
     *  loaded into the system. It is always called before
     *  the first time that the start() method is called.
     *  In this base class, this method reads the background color parameter.
     *  If the background color parameter has not been set, then the
     *  background color is set to white.
     */
    public void init() {
        // Process the background parameter.
        _background = Color.white;
        try {
            String colorSpecification = getParameter("background");
            if (colorSpecification != null) {
                _background = Color.decode(colorSpecification);
            }
        } catch (Exception ex) {
            report("Warning: background parameter failed: ", ex);
        }
        setBackground(_background);
    }

    /** Report an exception.  This prints a message to the standard error
     *  stream, followed by the stack trace, but displays on the screen
     *  only the error message associated with the exception.
     */
    public void report(Exception ex) {
        String msg = "Exception thrown by applet.";
        System.err.println(msg);
        ex.printStackTrace();
        showStatus("exception occurred.");

        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Ptolemy II Error Message", JOptionPane.ERROR_MESSAGE);
    }

    /** Report a message to the user.
     *  This shows the message on the browser's status bar.
     *  @param message The message to report.
     */
    public void report(String message) {
	showStatus(message);
    }

    /** Report an exception with an additional message.
     *  This prints a message to standard error, followed by the stack trace,
     *  and pops up a window with the message and the message of the
     *  exception.
     */
    public void report(String message, Exception ex) {
        String msg = "Exception thrown by applet.\n" + message;
        System.err.println(msg);
        ex.printStackTrace();
        showStatus("exception occurred.");
        JOptionPane.showMessageDialog(this,
               message + "\n" + ex.getMessage(),
               "Ptolemy II Error Message",
               JOptionPane.ERROR_MESSAGE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Concatenate two parameter info string arrays and return the result.
     *  This is provided to make it easy for derived classes to override
     *  the getParameterInfo() method. The returned array has length equal
     *  to the sums of the lengths of the two arguments, and containing
     *  the arrays contained by the arguments.
     *
     *  @param first The first string array.
     *  @param second The second string array.
     *  @return A concatenated string array.
     */
    protected String[][] _concatStringArrays(
            String[][] first, String[][] second) {
        String[][] newInfo = new String[first.length + second.length][];
        System.arraycopy(first, 0, newInfo, 0, first.length);
        System.arraycopy(second, 0, newInfo, first.length, second.length);
        return newInfo;
    }

    /** Get the background color as set by the "background" applet parameter.
     *  This is protected so that derived classes can find out what the
     *  background color is. Derived classes may wish to know the
     *  color so they can match it in some of their components.
     *  @deprecated Use the public method getBackground() instead.
     */
    protected Color _getBackground() {
        return _background;
    }

    /** Get the stack trace and return as a string.
     *  @param ex The exception for which we want the stack trace.
     *  @return The stack trace.
     */
    protected String _getStackTrace(Exception ex) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(stream);
        ex.printStackTrace(printWriter);
        printWriter.flush();
        return stream.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /*  The background color as set by the "background" applet parameter.
     *  This is protected so that derived classes can find out what the
     *  background color is. Derived classes may wish to know the
     *  color so they can match it in some of their components.
     */
    protected Color _background;
}
