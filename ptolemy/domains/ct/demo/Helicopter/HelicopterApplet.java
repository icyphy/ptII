/* Applet that displays the helicopter plot properly.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.demo.Helicopter;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.ModelPane;
import ptolemy.actor.gui.PtolemyApplet;

import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// HelicopterApplet
/**
This applet extends the PtolemyApplet to use the HelicopterModelPane
instead of the default ModelPane. The HelicopterModelPane organizes
the plots in a 2x2 grid.

@author  Jie Liu
@version $Id$
@since Ptolemy II 0.4
*/
public class HelicopterApplet extends PtolemyApplet {


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to create a HelicopterModelPane.
     */
    protected void _createView() {
        // Parse applet parameters that determine visual appearance.

        // Start with orientation.
        String orientationSpec = getParameter("orientation");
        // Default is vertical
        int orientation = ModelPane.VERTICAL;
        if (orientationSpec != null) {
            if (orientationSpec.trim().toLowerCase().equals("horizontal")) {
                orientation = ModelPane.HORIZONTAL;
            } else if (orientationSpec.trim().toLowerCase()
                    .equals("controls_only")) {
                orientation = ModelPane.CONTROLS_ONLY;
            }
        }

        // Next do controls.
        String controlsSpec = getParameter("controls");
        // Default has only the buttons.
        int controls = ModelPane.BUTTONS;
        if (controlsSpec != null) {
            // If controls are given, then buttons need to be explicit.
            controls = 0;
            StringTokenizer tokenizer = new StringTokenizer(controlsSpec, ",");
            while (tokenizer.hasMoreTokens()) {
                String controlSpec = tokenizer.nextToken().trim().toLowerCase();
                if (controlSpec.equals("buttons")) {
                    controls = controls | ModelPane.BUTTONS;
                } else if (controlSpec.equals("topparameters")) {
                    controls = controls | ModelPane.TOP_PARAMETERS;
                } else if (controlSpec.equals("directorparameters")) {
                    controls = controls | ModelPane.DIRECTOR_PARAMETERS;
                } else if (controlSpec.equals("none")) {
                    controls = 0;
                } else {
                    report("Warning: unrecognized controls: " + controlSpec);
                }
            }
        }

        ModelPane pane = new HelicopterModelPane(
                (CompositeActor)_toplevel, orientation, controls);
        pane.setBackground(null);
        getContentPane().add(pane);
    }
}


