/* A base class for applets that use the SDF domain.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.sdf.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

// FIXME: Trim this list.
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.util.PtolemyApplet;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// SDFApplet
/**
A base class for applets that use the SDF domain.
It provides a "Go" button to run the model.

@author Edward A. Lee
@version $Id$
*/
public class SDFApplet extends PtolemyApplet {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.  After invoking the base class init() method,
     *  Create a "Go" button.  The number of iterations is given by an
     *  applet parameter "iterations".  If no such parameter is given,
     *  then the Go button triggers exactly one iteration. This method
     *  creates a manager, top-level composite actor, and director for
     *  that composite.  All three are accessible via protected members
     *  to derived classes.
     */
    public void init() {
        super.init();

        // Process the iterations parameter.
        int iterations = 1;
        try {
            String iterspec = getParameter("iterations");
            if (iterspec != null) {
                iterations = (Integer.decode(iterspec)).intValue();
            }
        } catch (Exception ex) {
            report("Warning: iteration parameter failed: ", ex);
        }

        try {
            // Initialization
            _goButton = new Button("Go");

            _manager = new Manager();
            _toplevel = new TypedCompositeActor();
            _toplevel.setName("ComSystem");
            _director = new SDFDirector();
            Parameter iterparam =
                    (Parameter)_director.getAttribute("Iterations");
            iterparam.setToken(new IntToken(iterations));
            SDFScheduler scheduler = new SDFScheduler();

            _toplevel.setDirector(_director);
            _toplevel.setManager(_manager);
            _director.setScheduler(scheduler);
            _director.setScheduleValid(false);

            // Add a control panel in the main panel.
            Panel controlPanel = new Panel();
            add(controlPanel);
            controlPanel.add(_goButton);

            _goButton.addActionListener(new GoButtonListener());
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** The manager, created in the init() method. */
    protected Manager _manager;

    /** The top-level composite actor, created in the init() method. */
    protected TypedCompositeActor _toplevel;

    /** The director for the top-level composite actor, created in the
     *  init() method. */
    SDFDirector _director;

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Button _goButton;

    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _manager.startRun();
        }
    }
}
