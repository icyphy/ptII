/* An applet that uses Ptolemy II DE domain engine to simulate passengers
   and buses arrivals and then calculate the wait time.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// HistogramApplet
/**
An applet that uses Ptolemy II DE domain engine to simulate passengers
and buses arrivals and then calculate the wait time.

@author Lukito
@version $Id$
*/
public class HistogramApplet extends Applet implements Runnable {

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

        _cbg = new CheckboxGroup();
        _stopTimeBox = new TextField("100.0", 10);
        _goButton = new Button("Go");
        _intervalTextField = new TextField("10.0", 10);
        _averageWaitTimeLabel = new Label("Mean wait time = 0.0    ");
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _clockCheckbox = new Checkbox("Clock", _cbg, true);
        _poissonCheckbox = new Checkbox("Poisson", _cbg, false);


        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(2,1));
        add(appletPanel, "Center");

        // _plot is the drawing panel for DEPlot actor.
        Plot plot = new Plot();
        plot.setSize(new Dimension(450, 150));
        //plot.setTitle("Buses, Passengers and Wait Time");
        plot.setButtons(true);
        plot.addLegend(0, "Bus");
        plot.addLegend(1, "Passenger");
        plot.addLegend(2, "Wait Time");
        plot.setXLabel("Time");
        plot.setYLabel("Wait time");
        appletPanel.add(plot, "North");

        // _hist is the drawing panel for the DEHist actor.
        Plot hist = new Plot();
        hist.setSize(new Dimension(450, 150));
        //hist.setTitle("Distribution of Wait Time");
        hist.setButtons(true);
        hist.addLegend(0, "Wait Time");
        hist.setYLabel("# People");
        hist.setXLabel("Wait Time");
        appletPanel.add(hist, "South");

        // Adding a control panel in the applet panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.

        // Adding check box in the control panel.
        Panel checkboxPanel = new Panel();
        checkboxPanel.setLayout(new GridLayout(2,1));
        checkboxPanel.add(_clockCheckbox);
        checkboxPanel.add(_poissonCheckbox);
        controlPanel.add(checkboxPanel);
        // Done adding check box.

        // Adding simulation parameter panel in the control panel.
        Panel simulationParam = new Panel();
        simulationParam.setLayout(new GridLayout(2,1));
        controlPanel.add(simulationParam);
        // Done adding simulation parameter panel.

        // Adding Stop time in the simulation panel.
        Panel st = new Panel();
        simulationParam.add(st);
        st.add(new Label("Stop time:"));
        st.add(_stopTimeBox);
        // Done adding stop time.

       // Adding scrollbar and label in the simulation panel.
        Panel sb = new Panel();
        sb.setLayout(new BorderLayout());
        simulationParam.add(sb);
        _meanIntervalLabel = new Label("Mean interarrival time = 1");
        sb.add(_meanIntervalLabel, "South");
        _meanIntervalScrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1,10);
        sb.add(_meanIntervalScrollbar, "North");
        _meanIntervalScrollbar.addAdjustmentListener(new IntervalSbListener());
        // Done adding scroll bar and label in the applet panel.

        // Add a sub panel in the control panel
        Panel subPanel = new Panel();
        subPanel.setLayout(new GridLayout(2,1));
        controlPanel.add(subPanel);
        // Done adding a sub panel.


        // Adding average wait time in the sub panel.
        subPanel.add(_averageWaitTimeLabel);
        // Done adding average wait time.

        // Adding current time in the sub panel.
        subPanel.add(_currentTimeLabel);
        // Done adding current time.

        // Adding go button in the control panel.
        controlPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());
        // Done adding go button

        try {
            TypedCompositeActor topLevel = new TypedCompositeActor();
            topLevel.setName("Top");

            // Set up the directors
            _clock = new DEClock(topLevel, "Clock Bus", 1.0, 1.0);
            _poisson = new DEPoisson(topLevel, "Poisson Bus", 1.0, 1.0);
            _localDirector = new DEDirector("DE Director");
            topLevel.setDirector(_localDirector);
            _manager = new Manager("Executive Director");
            _manager.addExecutionListener(new MyExecutionListener());
            topLevel.setManager(_manager);

            // Create the actors.
            // The connections are created after appropriate source is chosen.
            DEPoisson dePoisson = new DEPoisson(topLevel, "Poisson",-1.0,1.0);
            DEWaitingTime deWaitingTime = new DEWaitingTime(topLevel, "Wait");
            DEPlot dePlot = new DEPlot(topLevel, "Plot", plot);
            String[] legends = {"Bus", "Passenger", "Wait time"} ;
            dePlot.setLegend(legends);
            DEHistogram deHist = new DEHistogram(topLevel, "Histogram", 0.1, hist);
            _stat = new DEStatistics(topLevel, "Stat");

            // Create the connections.
            _bus = _clock;
            r1 = topLevel.connect(_clock.output, dePlot.input);
            Relation r2 = topLevel.connect(dePoisson.output, dePlot.input);
            deWaitingTime.waitee.link(r1);
            deWaitingTime.waiter.link(r2);
            Relation r3 = topLevel.connect(deWaitingTime.output, dePlot.input);
            deHist.input.link(r3);
            _stat.input.link(r3);

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Run the simulation.
     */
    public void run() {

        String timespec = _stopTimeBox.getText();
        try {
            Double spec = Double.valueOf(timespec);
                _stopTime = spec.doubleValue();
        } catch (NumberFormatException ex) {
            System.err.println("Invalid stop time: " + ex.getMessage());
            return;
        }

        try {
                Checkbox selected = _cbg.getSelectedCheckbox();

                if (selected == _clockCheckbox) {
                    _meanInterval = (Parameter)_clock.getAttribute("interval");
                    // Use DEClock for the bus arrival
                    if (_bus != _clock) {
                        _poisson.output.unlink(r1);
                        _clock.output.link(r1);
                        _bus = _clock;
                        _meanInterval = (Parameter)_clock.getAttribute("interval");
                    }
                } else {
                    _meanInterval = (Parameter)_poisson.getAttribute("lambda");
                    // Use DEPoisson for the bus arrival
                    if (_bus != _poisson) {
                        _clock.output.unlink(r1);
                        _poisson.output.link(r1);
                        _bus = _poisson;
                    }
                }

                _localDirector.setStopTime(_stopTime);

                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                _isSimulationRunning = true;
                ctt.start();

                // Update the mean inter arrival time.
                int value = _meanIntervalScrollbar.getValue();
                _meanInterval.setToken(new DoubleToken((double)value));

                // initialize mean wait time text
                _averageWaitTimeLabel.setText("Calculating...");



                // Start the simulation.
                _manager.run();

                double average = _stat.getAverage();
                _averageWaitTimeLabel.setText("Mean wait time = "+average);
        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        _meanInterval = null;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // FIXME: I changed all of this to 'friendly' from 'private' (lmuliadi)

    // The thread that runs the simulation.
    Thread simulationThread;
    //private boolean isSimulationRunning = false;


    // The actors involved in the topology.
    /*private*/ DEStatistics _stat;
    /*private*/ CheckboxGroup _cbg;
    /*private*/ Checkbox _clockCheckbox;
    /*private*/ Checkbox _poissonCheckbox;
    /*private*/ DEClock _clock;
    /*private*/ DEPoisson _poisson;
    /*private*/ DEActor _bus;
    /*private*/ Relation r1;


    // FIXME: Under jdk 1.2, the following can (and should) be private
    /*private*/ DEDirector _localDirector;
    /*private*/ Manager _manager;

    /*private*/ TextField _stopTimeBox;
    /*private*/ double _stopTime = 100.0;
    /*private*/ Button _goButton;

    /*private*/ TextField _intervalTextField;
    /*private*/ double _interval = 10.0;

    /*private*/ Label _averageWaitTimeLabel;
    /*private*/ Label _currentTimeLabel;

    // Some parameters that we want to change during simulation.
    /*private*/ Parameter _meanInterval;
    /*private*/ Scrollbar _meanIntervalScrollbar;
    /*private*/ Label _meanIntervalLabel;

    /*private*/ boolean _isSimulationRunning = false;

    // END FIXME

    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    // Show simulation progress.
    // FIXME: due to an applet bug, I changed these inner class to public.
    public class CurrentTimeThread extends Thread {
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

    public class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            
            if (_isSimulationRunning) {
                System.out.println("Simulation still running.. hold on..");
                return;
            }

            try {
                if (simulationThread == null) {
                    simulationThread = new Thread(HistogramApplet.this);
                }
                if (!(simulationThread.isAlive())) {
                    simulationThread = new Thread(HistogramApplet.this);
                    // start() will eventually call the run() method.
                    simulationThread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new InternalErrorException("Error in GoButton" + 
                       "Listener class : " + e.getMessage()); 
            }

        }
    }

    public class IntervalSbListener implements AdjustmentListener {
        public void adjustmentValueChanged(AdjustmentEvent e) {
            int value = e.getValue();
            _meanIntervalLabel.setText("Mean interarrival time = " + value);
            if (_meanInterval != null) {
                _meanInterval.setToken(new DoubleToken((double)value));
            }
        }
    }

    private class MyExecutionListener extends DefaultExecutionListener {
        public void executionFinished(ExecutionEvent e) {
            super.executionFinished(e);
            _isSimulationRunning = false;
        }

    }
}



