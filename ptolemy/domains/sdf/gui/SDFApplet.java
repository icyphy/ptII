/* A base class for applets that use the SDF domain.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.gui;

import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFApplet
/**
A base class for applets that use the SDF domain. This is provided
for convenience, in order to promote certain common elements among
SDF applets. It is by no means required in order to create an applet
that uses the SDF domain. In particular, it creates and configures a
director. It defines two applet parameters, "iterations" and
"defaultIterations", which it uses to set the iterations parameter
of the director. If "iterations" is set, then that defines the number
of iterations. Otherwise, then when (and if) the applet requests on-screen
controls for model execution, it creates an entry box on the screen in
which the applet user can enter the number of
iterations. If the applet parameter "defaultIterations" is given,
then the value of this parameter is the default value in the entry
box.

@author Edward A. Lee
@version $Id$
*/
public class SDFApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    public String[][] getParameterInfo() {
        String newinfo[][] = {
            {"iterations", "", "number of iterations"},
            {"defaultIterations", "1", "default number of iterations"}
        };
        return _concatStringArrays(super.getParameterInfo(), newinfo);
    }

    /** Initialize the applet. After calling the base class init() method,
     *  this method creates a director which is accessible
     *  to derived classes via a protected member.
     *  If the applet parameter "iterations" has been given, then it
     *  sets the iterations parameter of the director.  This method
     *  also creates a scheduler associated with the director.
     */
    public void init() {
        super.init();

        // Process the iterations parameter.
        int iterations = 1;
        try {
            String iterspec = getParameter("iterations");
            if (iterspec != null) {
                iterations = (Integer.decode(iterspec)).intValue();
                _iterationsgiven = true;
            }
        } catch (Exception ex) {
            report("Warning: iteration parameter failed: ", ex);
        }

        try {
            // Initialization
            _director = new SDFDirector(_toplevel, "SDFDirector");
            Parameter iterparam = _director.iterations;
            iterparam.setToken(new IntToken(iterations));
            SDFScheduler scheduler = new SDFScheduler(_workspace);

            _director.setScheduler(scheduler);
            _director.setScheduleValid(false);
        } catch (Exception ex) {
            report("Failed to setup director and scheduler:\n", ex);
            _setupOK = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create run controls in a panel and return that panel.
     *  The argument controls how many buttons are
     *  created.  If its value is greater than zero, then a "Go" button
     *  created.  If its value is greater than one, then a "Stop" button
     *  is also created. In addition,
     *  if the number of iterations has not been specified by the applet
     *  parameter "iterations," then create
     *  an entry box for that number to be entered.
     *  Derived classes may override this method to add additional controls.
     *  @param numberOfButtons The number of buttons to create.
     *  @return The panel containing the controls.
     */
    protected JPanel _createRunControls(int numberOfButtons) {
        JPanel panel = super._createRunControls(numberOfButtons);
        if (!_iterationsgiven) {
            // To keep the label and entry box together, put them
            // in a new panel.
            JPanel iterpanel = new JPanel();
            // Despite Sun's documentation, the default is that a panel
            // is opaque, so the background doesn't come through.
            // Change that...
            iterpanel.setOpaque(false);
            iterpanel.add(new JLabel("Number of iterations:"));

            // Process the defaultIterations parameter.
            String defiterspec = getParameter("defaultIterations");
            if (defiterspec == null) {
                defiterspec = "1";
            }

            _iterationsbox = new JTextField(defiterspec, 10);
            _iterationsbox.addActionListener(new IterBoxListener());
            iterpanel.add(_iterationsbox);
            panel.add(iterpanel);
        }
        return panel;
    }

    /** Get the number of iterations from the entry box, if there is one,
     *  or from the director, if not.
     *  @return The number of iterations to execute.
     *  @exception IllegalActionException If the expression of the iteration
     *   parameter of the SDF director is not valid.
     */
    protected int _getIterations() throws IllegalActionException {
        int result = 1;
        if(_director != null) {
            Parameter iterparam =
                _director.iterations;
            result = ((IntToken)(iterparam.getToken())).intValue();
        }
        if(_iterationsbox != null) {
            try {
                result = (new Integer(_iterationsbox.getText())).intValue();
            } catch (NumberFormatException ex) {
                report("Error in number of iterations:\n", ex);
            }
        }
        return result;
    }

    /** Execute the system for the number of iterations given by the
     *  _getIterations() method.
     *  @throws IllegalActionException Not thrown.
     */
    protected void _go() throws IllegalActionException {
        try {
            int iterations = _getIterations();
            Parameter iterparam = _director.iterations;

            iterparam.setToken(new IntToken(iterations));
        } catch (Exception ex) {
            report("Unable to set number of iterations:\n", ex);
        }
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The director for the top-level composite actor, created in the
     *  init() method.
     */
    protected SDFDirector _director;

    /** True if the number of iterations has been given via an applet
     *  parameter.  Note that this is set by the init() method.  This
     *  is protected in case a derived class overrides _createRunControls()
     *  and wants access to this variable.
     */
    protected boolean _iterationsgiven = false;

    /** The entry box containing the number of iterations, or null if
     *  there is none. This
     *  is protected in case a derived class overrides _createRunControls()
     *  and wants access to this variable.
     */
    protected JTextField _iterationsbox;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for the iterations box.  When the applet user hits
     *  return, the model executes.
     */
    class IterBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                _go();
            } catch (Exception ex) {
                report(ex);
            }
        }
    }
}
