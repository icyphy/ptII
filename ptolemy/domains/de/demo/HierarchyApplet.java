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
//// Hierarchy
/** 
An applet that demonstrate a hierarchy of DE inside DE.

@author Lukito
@version $Id$
*/
public class HierarchyApplet extends Applet {

    public static final boolean DEBUG = true;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */	
    public void init() {

        // Initialization

        _stopTimeBox = new TextField("10.0", 10);
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _goButton = new Button("Go");

        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
	Plot plotPanel = new Plot();
	add(plotPanel, "Center");

        
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
        _goButton.addActionListener(new GoButtonListener());        
        // Done adding go button
        

        // Creating the topology.
        try {
            TypedCompositeActor sys = new TypedCompositeActor();
            sys.setName("DE");
            _manager = new Manager("Manager");
            _manager.addExecutionListener(new MyExecutionListener());
            sys.setManager(_manager);
            _localDirector = new DECQDirector("TopLocalDirector");
            sys.setDirector(_localDirector);
            
            // Set up block A

            TypedCompositeActor blockA = new TypedCompositeActor(sys, "BlockA");
            blockA.setDirector(new DECQDirector("Director A"));
            
            DEClock clock = new DEClock(blockA, "Clock", 1.0, 1.0);
            Ramp ramp1 = new Ramp(blockA, "Ramp1", 0, 2);
            DEPoisson poisson = new DEPoisson(blockA, "Poisson", 1.0, 0.5);

            DEIOPort A1 = new DEIOPort(blockA, "A1", false, true);
            A1.setDeclaredType(DoubleToken.class);
            A1.makeMultiport(true);
            DEIOPort A2 = new DEIOPort(blockA, "A2", false, true);
            A2.setDeclaredType(DoubleToken.class);

            Relation r1 = blockA.connect(clock.output, ramp1.input);
            Relation r2 = blockA.connect(ramp1.output, A1);
            ((IORelation)r2).setWidth(2);
            Relation r3 = blockA.connect(poisson.output, A2);
            
            // Set up block B
            
            TypedCompositeActor blockB = new TypedCompositeActor(sys, "BlockB");
            blockB.setDirector(new DECQDirector("Director B"));

            Ramp ramp2 = new Ramp(blockB, "Ramp2", -2, 2);
            DESampler sampler2 = new DESampler(blockB, "Sampler2");

            DEIOPort B1 = new DEIOPort(blockB, "B1", true, false);
            B1.setDeclaredType(DoubleToken.class);
            DEIOPort B2 = new DEIOPort(blockB, "B2", true, false);
            B2.setDeclaredType(DoubleToken.class);
            DEIOPort B3 = new DEIOPort(blockB, "B3", false, true);
            B3.setDeclaredType(DoubleToken.class);

            Relation r6 = blockB.connect(B1, ramp2.input);
            Relation r7 = blockB.connect(ramp2.output, sampler2.input);
            Relation r8 = blockB.connect(B2, sampler2.clock);
            Relation r11 = blockB.connect(sampler2.output, B3);
            
            // Set up block C

            TypedCompositeActor blockC = new TypedCompositeActor(sys, "BlockC");
            blockC.setDirector(new DECQDirector("Director C"));
            
            DEPlot plot = new DEPlot(blockC, "Plot", plotPanel);
            
            DEIOPort C1 = new DEIOPort(blockC, "C1", true, false);
            C1.setDeclaredType(DoubleToken.class);
            DEIOPort C2 = new DEIOPort(blockC, "C2", true, false);
            C2.setDeclaredType(DoubleToken.class);
            DEIOPort C3 = new DEIOPort(blockC, "C3", true, false);
            C3.setDeclaredType(DoubleToken.class);

            Relation r13 = blockC.connect(C1, plot.input);
            Relation r14 = blockC.connect(C2, plot.input);
            Relation r15 = blockC.connect(C3, plot.input);

            // Set up block interconnections.

            DESampler sampler1 = new DESampler(sys, "Sampler1");
            
            Relation r4 = sys.connect(A1, sampler1.input);
            Relation r5 = sys.connect(A2, sampler1.clock);
            B1.link(r5);
            B2.link(r5);
            Relation r9 = sys.connect(A1, C1);
            Relation r10 = sys.connect(sampler1.output, C2);
            Relation r12 = sys.connect(B3, C3);

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
    private Label _currentTimeLabel;

    
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
                
                _manager.go();
                                
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
                
        }
    }

}



