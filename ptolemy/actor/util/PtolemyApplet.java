/* A base class for Ptolemy applets.

 Copyright (c) 1999 The Regents of the University of California.
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

package ptolemy.actor.util;

import java.applet.Applet;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// PtolemyApplet
/**
A base class for Ptolemy applets.  This is provided for convenience,
in order to promote certain common elements among applets.  It is by
no means required in order to create an applet that uses Ptolemy II.

@author Edward A. Lee
@version $Id$
*/
public class PtolemyApplet extends Applet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return generic applet information.
     *  @return A string giving minimal information about Ptolemy II.
     */
    public String getAppletInfo() {
        return "Ptolemy II applet.\n"
            + "Ptolemy II comes from UC Berkeley, Department of EECS.";
    }

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            {"background",    "#RRGGBB",    "color of the background"},
        };
        return pinfo;
    }

    /** Initialize the applet.  This processes a background color parameter.
     *  If the background color parameter has not been set, then the
     *  background color is set to white.
     */
    public void init() {
        // Process the background parameter.
        _background = Color.white;
        try {
            String colorspec = getParameter("background");
            if (colorspec != null) {
                _background = Color.decode(colorspec);
            }
        } catch (Exception ex) {
            report("Warning: background parameter failed: ", ex);
        }
        setBackground(_background);
    }

    /** Report an exception.  This prints a message to the standard error
     *  stream, followed by the stack trace.
     */
    public void report(Exception ex) {
        System.err.println("Exception thrown by applet.\n"
                + ex.getMessage() + "\nStack trace:\n");
        ex.printStackTrace();
    }

    /** Report an exception with an additional meesage.  This prints a
     *  message to standard error, followed by the stack trace.
     */
    public void report(String msg, Exception ex) {
        System.err.println("Exception thrown by applet.\n" + msg + "\n"
                + ex.getMessage() + "\nStack trace:\n");
        ex.printStackTrace();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** The background color set as a parameter.
     */
    protected Color _background;
}
