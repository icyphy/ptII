/* A base class for applets that use the SDF domain.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.guidemo;

import java.awt.*;
import java.awt.event.*;

import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.util.PtolemyApplet;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// SDFApplet
/**
A base class for applets that use the SDF domain.
It creates and configures a director.
It defines an applet parameter, "iterations", which it uses to set
the iterations parameter of the director.  If that parameter is not
set by the applet, then when (and if) the applet requests run controls,
it creates a dialog box on the screen in which the applet user can
enter the number of iterations.  If the applet parameter "defaultiterations"
is given, then the value of this parameter is the default value in the
dialog box.

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
        String basepinfo[][] = super.getParameterInfo();
        String[][] pinfo = new String[basepinfo.length + 2][];
        for (int i = 0; i < basepinfo.length; i++) {
            pinfo[i] = basepinfo[i];
        }
        String newinfo[][] = {
            {"iterations", "", "number of iterations"},
            {"defaultiterations", "1", "default number of iterations"}
        };
        pinfo[basepinfo.length] = newinfo[0];
        pinfo[basepinfo.length+1] = newinfo[1];
        return pinfo;
    }

    /** Initialize the applet. The number of iterations is given by an
     *  applet parameter "iterations".  If this parameter is not given,
     *  then a dialog is created on screen to query the user for the number
     *  of iterations.
     *  After calling the base class init() method,
     *  this method creates a top-level composite actor
     *  and director for that composite.  Both are accessible
     *  to derived classes via protected members.
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
            _director = new SDFDirector();
            Parameter iterparam =
                (Parameter)_director.getAttribute("Iterations");
            iterparam.setToken(new IntToken(iterations));
            SDFScheduler scheduler = new SDFScheduler();

            _toplevel.setDirector(_director);
            _director.setScheduler(scheduler);
            _director.setScheduleValid(false);
        } catch (Exception ex) {
            report("Failed to setup director and scheduler:\n", ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** In addition to creating the buttons provided by the base class,
     *  if the number of iterations has not been specified, then create
     *  a dialog box for that number to be entered.  The panel containing
     *  the buttons and the entry box is returned.
     *  @param numbuttons The number of buttons to create.
     */
    protected Panel _createRunControls(int numbuttons) {
        Panel panel = super._createRunControls(numbuttons);
        if (!_iterationsgiven) {
            // To keep the label and entry box together, put them
            // in a new panel.
            Panel iterpanel = new Panel();
            iterpanel.add(new Label("Number of iterations:"));
            
            // Process the defaultiterations parameter.
            String defiterspec = getParameter("defaultiterations");
            if (defiterspec == null) {
                defiterspec = "1";
            }

            _iterationsbox = new TextField(defiterspec, 10);
            _iterationsbox.addActionListener(new IterBoxListener());
            iterpanel.add(_iterationsbox);
            panel.add(iterpanel);
        }
        return panel;
    }

    /** Get the number of iterations from the entry box, if there is one,
     *  or from the director, if not.
     */
    protected int _getIterations() {
        int result = 1;
        if(_director != null) {
            Parameter iterparam =
                    (Parameter)_director.getAttribute("Iterations");
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

    /** Get the number of iterations from the entry box, if there is one,
     *  and then execute the system.
     */
    protected void _go() {
        try {
            int iterations = _getIterations();
            Parameter iterparam =
                    (Parameter)_director.getAttribute("Iterations");

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
     *  parameter.  Note that this is set by the init() method.
     */
    protected boolean _iterationsgiven = false;
    
    /** The entry box containing the number of iterations, or null if
     *  there is none.
     */
    protected TextField _iterationsbox;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for the iterations box.  When the applet user hits
     *  return, the model executes.
     */
    class IterBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            _go();
        }
    }
}
