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
//// QueueApplet
/**
An applet that uses Ptolemy II DE domain.

@author Lukito Muliadi
@version $Id$
*/
public class QueueApplet extends Applet {

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
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _goButton = new Button("Go");
        _pauseButton = new Button(" Pause ");
        _finishButton = new Button("Finish");
        _terminateButton = new Button("Terminate");

        // The applet has four panels, shown in a 2x2 grid
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(2,2));
        add(appletPanel, "Center");

        // _la is the drawing panel for DELogicAnalyzer actor.
        Plot panel1 = new Plot();
        Plot panel2 = new Plot();
        Plot panel3 = new Plot();
        Plot panel4 = new Plot();
        appletPanel.add(panel1);
        appletPanel.add(panel2);
        appletPanel.add(panel3);
        appletPanel.add(panel4);

        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding simulation parameter panel in the control panel.
        Panel simulationParam = new Panel();
        simulationParam.setLayout(new GridLayout(2,1));
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

        // Adding Stop time in the simulation panel.
        Panel subSimul = new Panel();
        simulationParam.add(subSimul);
        subSimul.add(new Label("Stop time:"));
        subSimul.add(_stopTimeBox);
        // Done adding stop time.

        // Adding current time in the sub panel.
        simulationParam.add(_currentTimeLabel);
        // Done adding average wait time.

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
            sys.setName("DE Demo");

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
            Ramp ramp = new Ramp(sys, "Ramp", 0, 1.0);

            DEFIFOQueue fifo1 = new DEFIFOQueue(sys, "FIFO1", 1, true, 10);
            DEPlot plot1 = new DEPlot(sys, "Queue 1 Size", panel1);

            DEServerAlt server1 = new DEServerAlt(sys, "Server1", 1.0);
            DEPassGate passgate = new DEPassGate(sys, "PassGate");
            DEDelay delta = new DEDelay(sys, "DEDelay", 0.0);

            DEFIFOQueue fifo2 = new DEFIFOQueue(sys, "FIFO2", 1, true, 1000);
            DEPlot plot2 = new DEPlot(sys, "Queue 2 Size", panel2);

            TestLevel testlevel = new TestLevel(sys, "TestLevel", true, 4);
            Not not = new Not(sys, "Not");

            DEServerAlt server2 = new DEServerAlt(sys, "Server2", 3.0);

            DEPlot plot3 = new DEPlot(sys, "Blocking signal", panel3);
            DEPlot plot4 = new DEPlot(sys, "Dispositions of inputs", panel4);

            // -----------------------
            // Creating connections
            // -----------------------

            Relation r1 = sys.connect(clock.output, ramp.input);
            Relation r2 = sys.connect(ramp.output, fifo1.inData);
            Relation r3 = sys.connect(fifo1.queueSize, plot1.input);

            Relation r4 = sys.connect(passgate.output, fifo1.demand);
            fifo2.inData.link(r4);

            Relation r5 = sys.connect(fifo1.outData, server1.input);
            Relation r6 = sys.connect(fifo1.overflow, plot4.input);

            Relation r7 = sys.connect(server1.output, passgate.input);
            Relation r8 = sys.connect(delta.output, passgate.gate);

            Relation r9 = sys.connect(not.output, delta.input);

            Relation r14 = sys.connect(testlevel.output, not.input);
            plot3.input.link(r14);

            Relation r10 = sys.connect(server2.output, plot4.input);
            fifo2.demand.link(r10);

            Relation r12 = sys.connect(fifo2.queueSize, testlevel.input);
            plot2.input.link(r12);

            Relation r13 = sys.connect(fifo2.outData, server2.input);

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
    private double _stopTime = 100.0;
    private Button _goButton;
    private Button _pauseButton;
    private Button _finishButton;
    private Button _terminateButton;


    private Label _currentTimeLabel;
    private boolean _isSimulationPaused = false;

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



