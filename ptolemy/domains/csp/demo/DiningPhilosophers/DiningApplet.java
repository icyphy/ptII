/* An applet containing CSP Dining Philosophers demo.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.demo.DiningPhilosophers;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import ptolemy.domains.csp.kernel.CSPDirector;
import ptolemy.domains.csp.lib.*;
import ptolemy.domains.csp.gui.*;
import ptolemy.actor.*;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.plot.*;
import java.text.NumberFormat;

///////////////////////////////////////////////////////////////
//// DiningApplet
/**
   Applet containing Dining Philosophers demo. This demo uses the
   both the time and conditional communication constructs of the CSP
   domain in Ptolemy II. It represents the classic concurrency problem,
   first described by Dijkstra in 1965, which has 5 philosophers sitting
   around a table with one chopstick between each pair of philosophers. To
   eat a philosopher must have both chopsticks beside it. Each philosopher
   thinks for a while, then grabs one chopstick, then the other, eats for
   a while and puts the chopsticks down. This cycle continues.
   <p>
   @author Neil Smyth
   @version $Id$
*/

public class DiningApplet extends Applet
    implements Runnable, PhilosopherListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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


        try {
            univ = new TypedCompositeActor();
            univ.setName("Top");

            // Set up the directors
            _localDirector = new CSPDirector(univ, "CSP Director");
            _manager = new Manager("Manager");
            univ.setManager(_manager);

            Parameter eatingRate = new Parameter(univ, "eatingRate");
            eatingRate.setExpression("1.0");
            eatingRate.getToken();

            Parameter thinkingRate = new Parameter(univ, "thinkingRate");
            thinkingRate.setExpression("1.0");
            thinkingRate.getToken();

            // Set up the actors and connections
            Philosopher p1 = new Philosopher(univ, "Aristotle");
            Philosopher p2 = new Philosopher(univ, "Plato");
            Philosopher p3 = new Philosopher(univ, "Sartre");
            Philosopher p4 = new Philosopher(univ, "Descartes");
            Philosopher p5 = new Philosopher(univ, "Socrates");

            _philosophers[0] = p1;
            _philosophers[1] = p2;
            _philosophers[2] = p3;
            _philosophers[3] = p4;
            _philosophers[4] = p5;

            Parameter p = null;
            for (int i = 0; i< 5; i++) {
                _philosophers[i].addPhilosopherListener(this);
                p = (Parameter)_philosophers[i].getAttribute("eatingRate");
                if (p != null) {
                    p.setExpression("eatingRate");
                }
                p = (Parameter)_philosophers[i].getAttribute("thinkingRate");
                if (p != null) {
                    p.setExpression("thinkingRate");
                }
            }

            Chopstick f1 = new Chopstick(univ, "Chopstick1");
            Chopstick f2 = new Chopstick(univ, "Chopstick2");
            Chopstick f3 = new Chopstick(univ, "Chopstick3");
            Chopstick f4 = new Chopstick(univ, "Chopstick4");
            Chopstick f5 = new Chopstick(univ, "Chopstick5");

            // Now connect up the Actors
            TypedIORelation r1 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p1.getPort("leftIn"),
                        (TypedIOPort)f5.getPort("rightOut"));
            TypedIORelation r2 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p1.getPort("leftOut"),
                        (TypedIOPort)f5.getPort("rightIn"));
            TypedIORelation r3 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p1.getPort("rightIn"),
                        (TypedIOPort)f1.getPort("leftOut"));
            TypedIORelation r4 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p1.getPort("rightOut"),
                        (TypedIOPort)f1.getPort("leftIn"));

            TypedIORelation r5 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p2.getPort("leftIn"),
                        (TypedIOPort)f1.getPort("rightOut"));
            TypedIORelation r6 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p2.getPort("leftOut"),
                        (TypedIOPort)f1.getPort("rightIn"));
            TypedIORelation r7 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p2.getPort("rightIn"),
                        (TypedIOPort)f2.getPort("leftOut"));
            TypedIORelation r8 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p2.getPort("rightOut"),
                        (TypedIOPort)f2.getPort("leftIn"));

            TypedIORelation r9  =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p3.getPort("leftIn"),
                        (TypedIOPort)f2.getPort("rightOut"));
            TypedIORelation r10 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p3.getPort("leftOut"),
                        (TypedIOPort)f2.getPort("rightIn"));
            TypedIORelation r11 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p3.getPort("rightIn"),
                        (TypedIOPort)f3.getPort("leftOut"));
            TypedIORelation r12 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p3.getPort("rightOut"),
                        (TypedIOPort)f3.getPort("leftIn"));

            TypedIORelation r13 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p4.getPort("leftIn"),
                        (TypedIOPort)f3.getPort("rightOut"));
            TypedIORelation r14 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p4.getPort("leftOut"),
                        (TypedIOPort)f3.getPort("rightIn"));
            TypedIORelation r15 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p4.getPort("rightIn"),
                        (TypedIOPort)f4.getPort("leftOut"));
            TypedIORelation r16 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p4.getPort("rightOut"),
                        (TypedIOPort)f4.getPort("leftIn"));

            TypedIORelation r17 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p5.getPort("leftIn"),
                        (TypedIOPort)f4.getPort("rightOut"));
            TypedIORelation r18 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p5.getPort("leftOut"),
                        (TypedIOPort)f4.getPort("rightIn"));
            TypedIORelation r19 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p5.getPort("rightIn"),
                        (TypedIOPort)f5.getPort("leftOut"));
            TypedIORelation r20 =
                (TypedIORelation)univ.connect(
                        (TypedIOPort)p5.getPort("rightOut"),
                        (TypedIOPort)f5.getPort("leftIn"));

        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }

        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());

        _table = new TablePanel(_philosophers);
        add(_table, "Center");

        // Now create the panel that controls the applet
        Panel controlPanel = new Panel();
	/*
          add( _createRunControls(3), "West" );
	*/

        add(controlPanel, "West");

        // Add a time display and go/stop buttons to control panel
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _goButton = new Button("Go");
        _stopButton = new Button("Stop");
        controlPanel.add(_currentTimeLabel, "North");
        controlPanel.add(_goButton, "North");
        controlPanel.add(_stopButton, "North");

        // Add the listeners for the go and stop buttons
        _goButton.addActionListener(new GoButtonListener());
        _stopButton.addActionListener(new StopButtonListener());

        // Add fields for editing the rate at which the
        // philosophers think and eat.
        _eatingRateBox = new TextField("1.0", 10);
        _thinkingRateBox = new TextField("1.0", 10);
        //controlPanel.add(_eatingRateBox, "South");
        //controlPanel.add(_thinkingRateBox, "South");

        // Add the listeners for the go and stop buttons
        _eatingRateBox.addActionListener(new EatingRateListener());
        _thinkingRateBox.addActionListener(new ThinkingRateListener());
    }

    public synchronized void philosopherChanged() {
        if (simulationThread.isAlive()) {
            // repaint the table for the current state
            _table.repaint();
            /* To slow down the model, uncomment this...
               try {
               Thread.currentThread().sleep(100);
               } catch (InterruptedException e) {}
            */
        }
    }

    public void run() {
        System.out.println("DiningApplet.run()");
        try {
            // Start the CurrentTimeThread.
            ctt = new CurrentTimeThread();
            //ctt = new CurrentTimeThread(this);
            ctt.start();

            // Start the simulation.
            _manager.run();
        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Override the baseclass start method so that the model
     *  does not immediately begin executing as soon as the
     *  the applet page is displayed. Execution begins once
     *  the "Go" button is depressed.
     */
    public void start() {
        _table._initialize( _philosophers );
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // The thread that runs the simulation.
    Thread simulationThread;

    // The thread that updates the time display
    Thread ctt;

    // The buttons and text fields on the control panel.
    public Label _currentTimeLabel;
    public Button _goButton;
    public Button _stopButton;
    public TextField _eatingRateBox;
    public TextField _thinkingRateBox;

    // the panel containing the animation for the applet.
    public TablePanel _table;

    public Philosopher[] _philosophers = new Philosopher[5];

    public TypedCompositeActor univ;
    public CSPDirector _localDirector;
    public Manager _manager;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////


    // Show simulation progress.
    // NOTE: due to an applet bug, these inner classes are public.
    public class CurrentTimeThread extends Thread {
        NumberFormat nf = NumberFormat.getNumberInstance();
        public void run() {
            while( true ) {
            	if( simulationThread == null ) {
                    return;
                } else if( !simulationThread.isAlive() ) {
                    return;
                }
                // get the current time from director.
                double currentTime = _localDirector.getCurrentTime();
                _currentTimeLabel.setText("Current time = " +
                        nf.format(currentTime));
                try {
                    sleep(100);
                } catch (InterruptedException e) {}
            }
        }
    }

    public class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                if (simulationThread == null) {
                    simulationThread = new Thread(DiningApplet.this);
                    simulationThread.start();
                }
                if (!(simulationThread.isAlive())) {
                    simulationThread = new Thread(DiningApplet.this);
                    // start() will eventually call the run() method.
                    simulationThread.start();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    public class StopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                _manager.finish();
                // Force creation of a new thread on next execution.
                // Is this really necessary?
                simulationThread = null;
            } catch (Exception ex) {
                System.err.println("Run failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public class EatingRateListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Parameter p = (Parameter)univ.getAttribute("eatingRate");
            if ( p!= null) {
                String timespec = _eatingRateBox.getText();
                double spec = 1.0;
                try {
                    spec = (Double.valueOf(timespec)).doubleValue();
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid eating rate: " +
                            ex.getMessage() + ", defaulting to 1.0");
                }
                try {
                    p.setToken(new DoubleToken(spec));
                } catch (IllegalActionException ex) {
                    // Should not occur since there are no type constraints
                    throw new InternalErrorException(ex.getMessage());
                }
            }
        }
    }

    public class ThinkingRateListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Parameter p = (Parameter)univ.getAttribute("thinkingRate");
            if ( p!= null) {
                String timespec = _thinkingRateBox.getText();
                double spec = 1.0;
                try {
                    spec = (Double.valueOf(timespec)).doubleValue();
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid thinking rate: " +
                            ex.getMessage() + ", defaulting to 1.0");
                }
                try {
                    p.setToken(new DoubleToken(spec));
                } catch (IllegalActionException ex) {
                    // Should not be thrown
                    System.err.println("Unexpected error setting token.");
                }
            }
        }
    }


}
