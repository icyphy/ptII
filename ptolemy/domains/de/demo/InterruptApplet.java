/* An applet that uses Ptolemy II DE domain.

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

package ptolemy.domains.de.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// InterruptApplet
/**
An applet that uses Ptolemy II DE domain.

@author Lukito Muliadi
@version $Id$
*/
public class InterruptApplet extends Applet {

    public static final boolean DEBUG = true;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {

        // Process the background parameter.
        Color background = Color.white;
        try {
            String colorspec = getParameter("background");
            if (colorspec != null) {
                background = Color.decode(colorspec);
            }
        } catch (Exception ex) {}
        setBackground(background);

        // Initialization

        _stopTimeBox = new TextField("30.0", 10);
        _mstBox = new TextField("0.9", 10);
        _istBox = new TextField("0.1", 10);
        _lambdaBox = new TextField("2.0", 10);
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _goButton = new Button("Go");
        _pauseButton = new Button(" Pause ");
        _finishButton = new Button("Finish");
        _terminateButton = new Button("Terminate");

        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
        Plot appletPanel = new Plot();
        add(appletPanel, "Center");
        
        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding simulation parameter panel in the control panel.
        Panel simulationParam = new Panel();
        simulationParam.setLayout(new GridLayout(3,2));
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

        // Adding MST (minimum service time) in the simulation panel
        Panel mstPanel = new Panel();
        simulationParam.add(mstPanel);
        mstPanel.add(new Label("MST:"));
        mstPanel.add(_mstBox);
        // done adding MST

        // Adding Stop time in the simulation panel.
        Panel subSimul = new Panel();
        simulationParam.add(subSimul);
        subSimul.add(new Label("Stop time:"));
        subSimul.add(_stopTimeBox);
        // Done adding stop time.

        // Adding IST (interrupt service time) in the simulation panel
        Panel istPanel = new Panel();
        simulationParam.add(istPanel);
        istPanel.add(new Label("IST:"));
        istPanel.add(_istBox);
        // done adding IST

        // Adding current time in the sub panel.
        simulationParam.add(_currentTimeLabel);
        // Done adding average wait time.

        // Adding lambda (interrupt service time) in the simulation panel
        Panel lambdaPanel = new Panel();
        simulationParam.add(lambdaPanel);
        lambdaPanel.add(new Label("lambda:"));
        lambdaPanel.add(_lambdaBox);
        // done adding lambda
        
        // Adding go button in the control panel.
        controlPanel.add(_goButton);
        controlPanel.add(_pauseButton);
        controlPanel.add(_finishButton);
        controlPanel.add(_terminateButton);

        _goButton.addActionListener(new GoButtonListener());
        _pauseButton.addActionListener(new PauseButtonListener());
        _finishButton.addActionListener(new FinishButtonListener());
        _terminateButton.addActionListener(new TerminateButtonListener());
        // Done adding go button


        // Creating the topology.
        try {
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName("DEDemo");

            // Set up the top level composite actor, director and manager
            _localDirector = new DECQDirector("DE Director");
            sys.setDirector(_localDirector);
            _manager = new Manager("Manager");
            _manager.addExecutionListener(new MyExecutionListener());
            sys.setManager(_manager);

            // ---------------------------------
            // Create the actors.
            // ---------------------------------

            DEClock clock = new DEClock(sys, "Clock", 1.0, 1.0);
            Ramp ramp = new Ramp(sys, "Ramp", 0.0, 1.0);

            // create a processor with min service time = 1.0
            // interrupt service time = 0.1
            // mean interarrival time = 3.0
            DEProcessor processor = new DEProcessor(sys, 
                    "processor",
                    0.8, 0.1, 3.0);
            DEPlot plot = new DEPlot(sys, "Processor Input v.s. Output", 
                    appletPanel);
            
            DESampler sampler = new DESampler(sys, "Sampler");
            
            // -----------------------
            // Creating connections
            // -----------------------

            Relation r1 = sys.connect(clock.output, ramp.input);
            Relation r2 = sys.connect(ramp.output, processor.input);
            Relation r3 = sys.connect(processor.output, sampler.input);
            Relation r4 = sys.connect(sampler.output, plot.input);

            sampler.clock.link(r1);

            // setting up parameters.
            _minimumServiceTime = (Parameter)processor.getAttribute("MST");
            _interruptServiceTime = (Parameter)processor.getAttribute("IST");
            _lambda = (Parameter)processor.getAttribute("lambda");

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The thread that runs the simulation.
    private boolean _isSimulationRunning;

    // FIXME: Under jdk 1.2, the following can (and should) be private
    private DECQDirector _localDirector;
    private Manager _manager;

    private TextField _stopTimeBox;
    private TextField _mstBox;
    private TextField _istBox;
    private TextField _lambdaBox;
    private double _stopTime = 100.0;
    private Button _goButton;
    private Button _pauseButton;
    private Button _finishButton;
    private Button _terminateButton;
    

    private Label _currentTimeLabel;
    private boolean _isSimulationPaused = false;

    // Parameters of DEProcessor that we want to change.
    private Parameter _minimumServiceTime;
    private Parameter _interruptServiceTime;
    private Parameter _lambda;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////


    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    // Show simulation progress.
    private class CurrentTimeThread extends Thread {
        public void run() {
            while (_isSimulationRunning) {
                // get the current time from director.
                double currenttime = _localDirector.getCurrentTime();
                _currentTimeLabel.setText("Current time = "+currenttime);
                try {
                    sleep(500);
                } catch (InterruptedException e) {}
            }
        }
    }

    private class MyExecutionListener extends DefaultExecutionListener {
        public void executionFinished(ExecutionEvent e) {
            super.executionFinished(e);
            _isSimulationRunning = false;
        }

    }

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {

            if (_isSimulationRunning) {
                System.out.println("Simulation still running.. hold on..");
                return;
            }

            try {

                // The simulation is started non-paused (of course :-) )
                _isSimulationPaused = false;
                _pauseButton.setLabel(" Pause ");

                // Set the stop time.
                String timespec = _stopTimeBox.getText();
                try {
                    Double spec = Double.valueOf(timespec);
                    _stopTime = spec.doubleValue();
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid stop time: " + ex.getMessage());
                    return;
                }

                // Set the minimum service time.
                try {
                    String s = _mstBox.getText();
                    Double d = Double.valueOf(s);
                    _minimumServiceTime.setToken(new DoubleToken(d.doubleValue()));
                    
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid minimum service time: " + 
                                       ex.getMessage());
                }

                // Set the interrupt service time.
                try {
                    String s = _istBox.getText();
                    Double d = Double.valueOf(s);
                    _interruptServiceTime.setToken(new DoubleToken(d.doubleValue()));
                    
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid interrupt service time: " + 
                                       ex.getMessage());
                }

                // Set lambda.
                try {
                    String s = _lambdaBox.getText();
                    Double d = Double.valueOf(s);
                    _lambda.setToken(new DoubleToken(d.doubleValue()));
                    
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid lambda: " + 
                                       ex.getMessage());
                }

                _localDirector.setStopTime(_stopTime);

                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                _isSimulationRunning = true;
                ctt.start();

                _manager.startRun();

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    private class PauseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {

            if (_isSimulationPaused) {
                _isSimulationPaused = false;
                _manager.resume();
                _pauseButton.setLabel(" Pause ");

            } else {
                _isSimulationPaused = true;
                _manager.pause();
                _pauseButton.setLabel("Resume");

            }

        }
    }

    private class FinishButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _manager.finish();
        }
    }

    private class TerminateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _manager.terminate();
        }
    }
}



