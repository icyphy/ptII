/* A base class for applets that use the DE domain.

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

package ptolemy.domains.de.demo;

import java.awt.*;
import java.awt.event.*;

import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.util.PtolemyApplet;
import ptolemy.domains.de.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// DEApplet
/**
A base class for applets that use the DE domain.
It creates and configures a director.
If the applet parameter "stoptime" has been set, then it uses that
parameter to define the duration of the execution of the model.
Otherwise, it creates an entry box in the applet for specifying the
stop time.  If the applet parameter "defaultstoptime" has been set,
then the entry box is initialized with the value given by that parameter.


@author Edward A. Lee
@version $Id$
*/
public class DEApplet extends PtolemyApplet {

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
            {"stoptime", "", "when to stop"},
            {"defaultstoptime", "1.0", "default value for when to stop"}
        };
        pinfo[basepinfo.length] = newinfo[0];
        pinfo[basepinfo.length+1] = newinfo[1];
        return pinfo;
    }

    /** Initialize the applet. The stop time is given by an
     *  applet parameter "stoptime".  If this parameter is not given,
     *  then a dialog is created on screen to query the user for the
     *  stop time.
     *  After calling the base class init() method,
     *  this method creates a top-level composite actor
     *  and director for that composite.  Both are accessible
     *  to derived classes via protected members.
     */
    public void init() {
        super.init();

        // Process the stoptime parameter.
        double stoptime = 1.0;
        try {
            String stopspec = getParameter("stoptime");
            if (stopspec != null) {
                stoptime = (new Double(stopspec)).doubleValue();
                _stoptimegiven = true;
            }
        } catch (Exception ex) {
            report("Warning: stop time parameter failed: ", ex);
        }

        try {
            // Initialization
            _director = new DEDirector();
            _director.setStopTime(stoptime);
            _toplevel.setDirector(_director);
        } catch (Exception ex) {
            report("Failed to setup director:\n", ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** In addition to creating the buttons provided by the base class,
     *  if the stop time has not been specified, then create
     *  a dialog box for that number to be entered.  The panel containing
     *  the buttons and the entry box is returned.
     *  @param numbuttons The number of buttons to create.
     */
    protected Panel _createRunControls(int numbuttons) {
        Panel panel = super._createRunControls(numbuttons);
        if (!_stoptimegiven) {
            // To keep the label and entry box together, put them
            // in a new panel.
            Panel stoptimepanel = new Panel();
            stoptimepanel.add(new Label("Stop time:"));

            // Process the defaultiterations parameter.
            String defstopspec = getParameter("defaultstoptime");
            if (defstopspec == null) {
                defstopspec = "1.0";
            }

            _stoptimebox = new TextField(defstopspec, 10);
            _stoptimebox.addActionListener(new StopTimeBoxListener());
            stoptimepanel.add(_stoptimebox);
            panel.add(stoptimepanel);
        }
        return panel;
    }

    /** Get the stop time from the entry box, if there is one,
     *  or from the director, if not.
     */
    protected double _getStopTime() {
        double result = 1.0;
        if(_director != null) {
            result = _director.getStopTime();
        }
        if(_stoptimebox != null) {
            try {
                result = (new Double(_stoptimebox.getText())).doubleValue();
            } catch (NumberFormatException ex) {
                report("Error in stop time:\n", ex);
            }
        }
        return result;
    }

    /** Get the stop time from the entry box, if there is one,
     *  and then execute the system.
     */
    protected void _go() {
        try {
            _director.setStopTime(_getStopTime());
        } catch (Exception ex) {
            report("Unable to set the stop time:\n", ex);
        }
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The director for the top-level composite actor, created in the
     *  init() method.
     */
    protected DEDirector _director;

    /** True if the stop time has been given via an applet
     *  parameter.  Note that this is set by the init() method.
     */
    protected boolean _stoptimegiven = false;

    /** The entry box containing the stop time, or null if
     *  there is none.
     */
    protected TextField _stoptimebox;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for the stop time box.  When the applet user hits
     *  return, the model executes.
     */
    class StopTimeBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            _go();
        }
    }
}
