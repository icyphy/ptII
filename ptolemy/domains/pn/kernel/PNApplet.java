/* A base class for applets that use the PN domain.

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

@ProposedRating Red (mudit@eecs.berkeley.edu)
@AcceptedRating Red (mudit@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.kernel;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

import ptolemy.actor.*;
import ptolemy.actor.util.PtolemyApplet;

//////////////////////////////////////////////////////////////////////////
//// PNApplet
/**
A base class for applets that use the PN domain.
It provides a "Go" button to run the model.

@author Edward A. Lee, Mudit Goel
@version $Id$
*/
public class PNApplet extends PtolemyApplet {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.  After invoking the base class init() method,
     *  Create a "Go" button. This method
     *  creates a manager, top-level composite actor, and director for
     *  that composite.  All three are accessible via protected members
     *  to derived classes.
     */
    public void init() {
        super.init();
        try {

            _manager = new Manager();
            _toplevel = new CompositeActor();
            _toplevel.setName("ComSystem");
            _director = new BasePNDirector();
            _toplevel.setDirector(_director);
            _toplevel.setManager(_manager);

            // Add a control panel in the main panel.
            // Initialization
            //_goButton = new Button("Go");
            //Panel controlPanel = new Panel();
            //add("North",_goButton);
            //controlPanel.add(_goButton);
            //_goButton.addActionListener(new GoButtonListener());
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** The manager, created in the init() method. */
    protected Manager _manager;

    /** The top-level composite actor, created in the init() method. */
    protected CompositeActor _toplevel;

    /** The director for the top-level composite actor, created in the
     *  init() method. */
    protected BasePNDirector _director;

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





