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
import ptolemy.domains.ct.kernel.*;
import ptolemy.domains.ct.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// SigmaDeltaApplet
/**
An applet that uses Ptolemy II DE domain.

@author Lukito Muliadi
@version $Id$
*/
public class SigmaDeltaApplet extends Applet {

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

        _stopTimeBox = new TextField("15.0", 10);
        _mstBox = new TextField("0.01", 10);
        _istBox = new TextField("0.001", 10);
        _lambdaBox = new TextField("2.0", 10);
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _goButton = new Button("Go");
        _pauseButton = new Button(" Pause ");
        _finishButton = new Button("Finish");
        _terminateButton = new Button("Terminate");

        

        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new GridLayout(2,1));
        add(appletPanel, "Center");
        
        // _la is the drawing panel for DELogicAnalyzer actor.
        Plot ctPanel = new Plot();
        Plot dePanel = new Plot();
        appletPanel.add(ctPanel);
        appletPanel.add(dePanel);

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
            sys.setName("DESystem");

            // Set up the top level composite actor, director and manager
            _localDirector = new DEDirector("DELocalDirector");
            sys.setDirector(_localDirector);
            _manager = new Manager("Manager");
            _manager.addExecutionListener(new MyExecutionListener());
            sys.setManager(_manager);

            // CT subsystem
            TypedCompositeActor ctsub = new TypedCompositeActor(sys, 
                    "CTSubsystem");
            TypedIOPort subin = new TypedIOPort(ctsub, "Pin");
            subin.setInput(true);
            
            TypedIOPort subout = new TypedIOPort(ctsub, "Pout");
            subout.setOutput(true);

            CTMixedSignalDirector ctdir = 
                new CTMixedSignalDirector("CTEmbDir");
            ctsub.setDirector(ctdir);
            

            // ---------------------------------
            // Create the actors.
            // ---------------------------------

            // CTActors

            CTSin sine = new CTSin(ctsub, "Sin");
            CTZeroOrderHold hold = new CTZeroOrderHold(ctsub, "Hold");
            CTAdd add1 = new CTAdd(ctsub, "Add1");
            
            CTIntegrator intgl1 = new CTIntegrator(ctsub, "Integrator1");
            CTIntegrator intgl2 = new CTIntegrator(ctsub, "Integrator2");
            CTGain gain0 = new CTGain(ctsub, "Gain0");
            CTGain gain1 = new CTGain(ctsub, "Gain1");
            CTGain gain2 = new CTGain(ctsub, "Gain2");
            CTGain gain3 = new CTGain(ctsub, "Gain3");

            CTPlot ctPlot = new CTPlot(ctsub, "CTPlot", ctPanel);

            String[] ctLegends = {"Position","Input","Control"};
            ctPlot.setLegend(ctLegends);

            CTPeriodicalSampler sampler = 
                new CTPeriodicalSampler(ctsub, "Sample");
            
            // CTPorts
            IOPort sineout = (IOPort)sine.getPort("output");
            IOPort add1in = (IOPort)add1.getPort("input");
            IOPort add1out = (IOPort)add1.getPort("output");

            IOPort intgl1in = (IOPort)intgl1.getPort("input");
            IOPort intgl1out = (IOPort)intgl1.getPort("output");
            IOPort intgl2in = (IOPort)intgl2.getPort("input");
            IOPort intgl2out = (IOPort)intgl2.getPort("output");
            IOPort gain0in = (IOPort)gain0.getPort("input");
            IOPort gain0out = (IOPort)gain0.getPort("output");
            IOPort gain1in = (IOPort)gain1.getPort("input");
            IOPort gain1out = (IOPort)gain1.getPort("output");
            IOPort gain2in = (IOPort)gain2.getPort("input");
            IOPort gain2out = (IOPort)gain2.getPort("output");
            IOPort gain3in = (IOPort)gain3.getPort("input");
            IOPort gain3out = (IOPort)gain3.getPort("output");
            IOPort plotin = (IOPort)ctPlot.getPort("input");
            IOPort sampin = (IOPort)sampler.getPort("input");
            IOPort sampout = (IOPort)sampler.getPort("output");
            IOPort holdin = (IOPort)hold.getPort("input");
            IOPort holdout = (IOPort)hold.getPort("output");

            // CTConnections
            Relation cr0 = ctsub.connect(sineout, gain0in, "CR0");
            Relation cr1 = ctsub.connect(gain0out, add1in, "CR1");
            Relation cr2 = ctsub.connect(add1out, intgl1in, "CR2");
            Relation cr3 = ctsub.connect(intgl1out, intgl2in, "CR3");
            Relation cr4 = ctsub.connect(intgl2out, plotin, "CR4");
            gain1in.link(cr3);
            gain2in.link(cr4);
            sampin.link(cr4);
            TypedIORelation cr5 = new TypedIORelation(ctsub, "CR5");
            sampout.link(cr5);
            subout.link(cr5);
            Relation cr6 = ctsub.connect(gain1out, add1in, "CR6");
            Relation cr7 = ctsub.connect(gain2out, add1in, "CR7");
            Relation cr8 = ctsub.connect(gain3out, add1in, "CR8");
            TypedIORelation cr9 = new TypedIORelation(ctsub, "CR9");
            holdin.link(cr9);
            subin.link(cr9);
            Relation cr10 = ctsub.connect(holdout, gain3in, "CR10");
            plotin.link(cr0);
            plotin.link(cr10);

            // DE System
            // approximate the FIR filter by a delay and a gain.
            DEFIRfilter fir = new DEFIRfilter(sys, "FIR", "0.7 0.3");
            Parameter firdelay = (Parameter)fir.getAttribute("Delay");
            firdelay.setExpression("0.02");
            firdelay.parameterChanged(null);
            
            DETestLevel quan = new DETestLevel(sys, "Quantizer");
            DEStatistics accu = new DEStatistics(sys, "Accumulator");
            DEClock clk = new DEClock(sys, "ADClock", 1, 1);
            DEPlot deplot = new DEPlot(sys, "DEPlot", dePanel);
            String[] deLegends = {"Accumulator", "Quantizer"};
            deplot.setLegend(deLegends);
            DEFIRfilter mav = new DEFIRfilter(sys, "MAV", "0.1 0.1 0.1 0.1" + 
                    " 0.1 0.05 0.05 0.05 0.05 0.05 0.05 0.05 0.05 0.05 0.05");
            DEProcessor processor = new DEProcessor(sys, "processor",
                    0.8, 0.1, 3.0);

            
            // DE ports
            IOPort firin = (IOPort)fir.getPort("input");
            IOPort firout = (IOPort)fir.getPort("output");
            IOPort quanin = (IOPort)quan.getPort("input");
            IOPort quanout = (IOPort)quan.getPort("output");
            IOPort accin = (IOPort)accu.getPort("input");
            IOPort accout = (IOPort)accu.getPort("average");
            IOPort demand = (IOPort)accu.getPort("demand");
            IOPort reset = (IOPort)accu.getPort("reset");
            IOPort clkout = (IOPort)clk.getPort("output");
            IOPort mavin = (IOPort)mav.getPort("input");
            IOPort mavout = (IOPort)mav.getPort("output");
            IOPort deplotin = (IOPort)deplot.getPort("input");
            IOPort processorIn = processor.input;
            IOPort processorOut = processor.output;
            
            // DE connections.
            Relation dr0 = sys.connect(subout, processorIn, "DR0");
            Relation dr1 = sys.connect(processorOut, firin, "DR1");
            Relation dr2 = sys.connect(firout, quanin, "DR2");
            Relation dr3 = sys.connect(quanout, subin, "DR3");
            Relation dr4 = sys.connect(clkout, demand, "DR4");
            reset.link(dr4);
            mavin.link(dr3);
            Relation dr5 = sys.connect(accin, mavout, "DR5");
            Relation dr6 = sys.connect(deplotin, accout, "DR6");
            deplotin.link(dr3);

            // CT Director parameters
            Parameter initstep = 
                (Parameter)ctdir.getAttribute("InitialStepSize");
            initstep.setExpression("0.000001");
            initstep.parameterChanged(null);
            Parameter minstep =
                (Parameter)ctdir.getAttribute("MinimumStepSize");
            minstep.setExpression("1e-6");
            minstep.parameterChanged(null);
            
            Parameter solver1 =
                (Parameter)ctdir.getAttribute("BreakpointODESolver");
            StringToken token1 = new StringToken("ptolemy.domains.ct.kernel.solver.BackwardEulerSolver");
            solver1.setToken(token1);
            solver1.parameterChanged(null);

            Parameter solver2 =
                (Parameter)ctdir.getAttribute("ODESolver");
            StringToken token2 = new StringToken("ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver");
            solver2.setToken(token2);
            solver2.parameterChanged(null);

            // CTActorParameters
            
            Parameter freq = (Parameter)sine.getAttribute("AngleFrequency");
            freq.setExpression("0.5");
            freq.parameterChanged(null);
            
            Parameter g0 = (Parameter)gain0.getAttribute("Gain");
            g0.setExpression("50.0");
            g0.parameterChanged(null);
            
            Parameter g1 = (Parameter)gain1.getAttribute("Gain");
            g1.setExpression("-2.50");
            g1.parameterChanged(null);
            
            Parameter g2 = (Parameter)gain2.getAttribute("Gain");
            g2.setExpression("-250.0");
            g2.parameterChanged(null);

            Parameter g3 = (Parameter)gain3.getAttribute("Gain");
            g3.setExpression("-20.0");
            g3.parameterChanged(null);

            Parameter ts = (Parameter)sampler.getAttribute("SamplePeriod");
            ts.setExpression("0.02");
            ts.parameterChanged(null);
            
            // Setting up parameters.
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
    private DEDirector _localDirector;
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



