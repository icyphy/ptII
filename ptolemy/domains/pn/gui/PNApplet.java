/* A base class for applets that use the PN domain.

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

@ProposedRating Red (mudit@eecs.berkeley.edu)
@AcceptedRating Red (mudit@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.gui;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.actor.*;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.data.expr.Parameter;
import ptolemy.data.*;

///////////////////////////////////////////////////////////////
//// PNApplet
/**
A base class for applets that use the PN domain.
It provides a "Go" button to run the model.

@author Edward A. Lee, Mudit Goel
@version $Id$
*/
public class PNApplet extends PtolemyApplet {

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
            {"initial_queue_capacity", "", "Capacity of the queues in PN"},
            {"defaultcapacity", "1", "default capacity of queues"}

        };
        pinfo[basepinfo.length] = newinfo[0];
        pinfo[basepinfo.length+1] = newinfo[1];
        return pinfo;
    }

    /** Initialize the applet.  After invoking the base class init() method,
     *  Create a "Go" button. This method
     *  creates a manager, top-level composite actor, and director for
     *  that composite.  All three are accessible via protected members
     *  to derived classes.
     */
    public void init() {
        super.init();

        // Process the initial queue size parameter.
        int capacity = 1;
        try {
            String iterspec = getParameter("initial_queue_capacity");
            if (iterspec != null) {
                capacity = (Integer.decode(iterspec)).intValue();
                _queuesizegiven = true;
            }
        } catch (Exception ex) {
            report("Warning: queue capacity parameter failed: ", ex);
        }

        try {
            // initialize
            _director = new BasePNDirector( _toplevel, "PNDirector");
            Parameter param =
                (Parameter)_director.getAttribute("Initial_queue_capacity");
            param.setToken(new IntToken(capacity));

        } catch (Exception ex) {
            report("Setup failed:", ex);
            _setupOK = false;
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** In addition to creating the buttons provided by the base class,
     *  if the number of iterations has not been specified, then create
     *  a dialog box for that number to be entered.  The panel containing
     *  the buttons and the entry box is returned.
     *  @param numbuttons The number of buttons to create.
     */
    protected JPanel _createRunControls(int numbuttons) {
        JPanel panel = super._createRunControls(numbuttons);
        if (!_queuesizegiven) {
            // To keep the label and entry box together, put them
            // in a new panel.
            JPanel queuepanel = new JPanel();
            queuepanel.add(new JLabel("Initial capacity of FIFO channels:"));
	    queuepanel.setBackground(getBackground());

            // Process the defaultiterations parameter.
            String defqueuespec = getParameter("defaultcapacity");
            if (defqueuespec == null) {
                defqueuespec = "1";
            }

            _queuesizebox = new TextField(defqueuespec, 10);
            _queuesizebox.addActionListener(new QueueSizeBoxListener());
            queuepanel.add(_queuesizebox);
            panel.add(queuepanel);
        }
        return panel;
    }


    /** Get the initial capacity of the channels from the entry box,
     *  if there is one, or from the director, if not.
     *  @exception IllegalActionException If the expression of the
     *   parameter Initial_queue_capacity is not valid.
     */
    protected int _getQueueSize() throws IllegalActionException {
        int result = 1;
        if(_director != null) {
            Parameter param =
                (Parameter)_director.getAttribute(
                        "Initial_queue_capacity");
            result = ((IntToken)(param.getToken())).intValue();
        }
        if(_queuesizebox != null) {
            try {
                result = (new Integer(_queuesizebox.getText())).intValue();
            } catch (NumberFormatException ex) {
                report("Error in the queue capacity format:\n", ex);
            }
        }
        return result;
    }


    /** Get the initial queue capacity from the entry box, if there is one,
     *  and then execute the system.
     *  @exception IllegalActionException If a type conflict occurs in the
     *   parameters.
     */
    protected void _go() throws IllegalActionException {
        try {
            int queuesize = _getQueueSize();
            Parameter param =
                (Parameter)_director.getAttribute(
                        "Initial_queue_capacity");

            param.setToken(new IntToken(queuesize));
        } catch (Exception ex) {
            report("Unable to get the queuesize for the queue:\n", ex);
        }
        super._go();
    }

    /** Pause the simulation */
    protected void _pause() {
        if (!_isSimulationRunning) {
            System.out.println("Simulation not running.. cannot pause..");
            return;
        }
        try {
            _director.pause();
        } catch (Exception ex) {
            report("Unable to pause the simulation:\n", ex);
        }
        return;
    }

    /** Resume the simulation */
    protected void _resume() {
        if (!_isSimulationRunning) {
            System.out.println("Simulation not running.. cannot resume..");
            return;
        }
        _director.resume();
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The top-level composite actor, created in the init() method. */
    // protected CompositeActor _toplevel;

    /** The director for the top-level composite actor, created in the
     *  init() method. */
    protected BasePNDirector _director;

    /** True if the initial queue capacity has been given via an applet
     *  parameter.  Note that this is set by the init() method.
     */
    protected boolean _queuesizegiven = false;

    /** The entry box containing the number of iterations, or null if
     *  there is none.
     */
    protected TextField _queuesizebox;

    /** True if the current simulation is running */
    protected boolean _isSimulationRunning = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Button _goButton;


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// QueueSizeBoxListener

    /** Listener for the iterations box.  When the applet user hits
     *  return, the model executes.
     */
    class QueueSizeBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                _go();
            } catch (Exception ex) {
                report(ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PauseListener


    public class PauseListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _pause();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ResumeListener


    public class ResumeListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _resume();
        }
    }
}
