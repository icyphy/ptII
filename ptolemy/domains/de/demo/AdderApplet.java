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
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// AdderApplet
/**
An applet that uses Ptolemy II DE domain engine to add two binary numbers.

@author Lukito
@version $Id$
*/
public class AdderApplet extends Applet implements Runnable {

    public static final boolean DEBUG = false;

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

        _ATextField = new TextField("1 0 0 1 1 0 1 1 0 1 1 0 0 1 1 1 0 1 1 0 1 1 1 1 0 0 1 1 1 0 0 0 1 1 1 1 1 0 1 0 1 0 1 1 0 0 1 1 1 0 0 1 0 0 0 0", 50);
        _BTextField = new TextField("0 0 1 0 1 1 1 1 0 1 1 0 1 0 1 0 0 1 1 1 0 1 0 1 0 1 1 0 0 1 1 1 0 0 1 1 1 0 0 0 1 1 1 1 1 0 1 0 1 0 1 1 1 1 1 1", 50);
        _stopTimeBox = new TextField("30.0", 10);
        _currentTimeLabel = new Label("Current time = 0.0      ");
        _goButton = new Button("Go");

        // Add the drawing panel for DELogicAnalyzer actor in the middle.
        setLayout(new BorderLayout());
        LogicAnalyzer la = new LogicAnalyzer();
        la.setSize(new Dimension(550, 200));
        la.setTitle("Logic Analyzer");
        la.addLegend(0, "A");
        la.addLegend(1, "B");
        la.addLegend(2, "S");
        la.addLegend(3, "Cout");
        la.addLegend(4, "Cin");
        la.addLegend(5, "RegS");
        //la.setXLabel("time");
        add(la, "Center");
        la.repaint();

        // Adding a control panel in the main panel.
        Panel controlPanel = new Panel();
        add(controlPanel, "South");
        // Done adding a control panel.


        // Adding A and B in the control panel.
        Panel checkboxPanel = new Panel();
        checkboxPanel.setLayout(new GridLayout(2,1));
        Panel AEntry = new Panel();
        Panel BEntry = new Panel();
        checkboxPanel.add(AEntry);
        checkboxPanel.add(BEntry);
        AEntry.add(new Label("A:"));
        BEntry.add(new Label("B:"));
        AEntry.add(_ATextField);
        BEntry.add(_BTextField);
        controlPanel.add(checkboxPanel);
        // Done adding A and B


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
            TypedCompositeActor topLevel = new TypedCompositeActor();
            topLevel.setName("Top");

            // Set up the top level composite actor, director and manager
            _localDirector = new DEDirector("DE Director");
            topLevel.setDirector(_localDirector);
            _manager = new Manager("Executive Director");
            topLevel.setManager(_manager);

            // ---------------------------------
            // Create the actors.
            // ---------------------------------
            // _clock supplies the global tick.
            DEClock _clock = new DEClock(topLevel, "Clock", 1.0, 1.0);
            // a full adder with propagation delay equal to 0.1
            DEFullAdder _fullAdder = new DEFullAdder(topLevel, "Fulladder", 0.1);

            // These wave form generators output the given array once, then
            // zeros..
            //double[] a = {0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0};
            DEWaveForm _A = new DEWaveForm(topLevel, "WaveForm A", null, false, false, 0);
            //double[] b = {0, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0};

            DEWaveForm _B = new DEWaveForm(topLevel, "WaveForm B", null, false, false, 0);

            _valueOfA = (Parameter)_A.getAttribute("value");
            _valueOfB = (Parameter)_B.getAttribute("value");

            // The registers to store results.
            DERegister _S = new DERegister(topLevel, "Reg S", null);
            DERegister _Cout = new DERegister(topLevel, "Reg Cout", null);

            // actor to display the result.
            DELogicAnalyzer _logicAnalyzer = new DELogicAnalyzer(topLevel, "Logic Analyzer", la);

            // -----------------------
            // Creating connections
            // -----------------------

            // clocking connections.
            Relation Rclock = topLevel.connect(_clock.output, _A.input);
            _B.input.link(Rclock);
            _S.clock.link(Rclock);
            _Cout.clock.link(Rclock);

            // interface to the full adder.
            Relation RA = topLevel.connect(_A.output, _fullAdder.A);
            Relation RB = topLevel.connect(_B.output, _fullAdder.B);
            Relation RS = topLevel.connect(_fullAdder.S, _S.input);
            Relation RCout = topLevel.connect(_fullAdder.Cout, _Cout.input);

            // the feedback loop for the carries.
            Relation RCin = topLevel.connect(_Cout.output, _fullAdder.Cin);

            // Connection to the DELogicAnalyzer
            _logicAnalyzer.input.link(RA);
            _logicAnalyzer.input.link(RB);
            _logicAnalyzer.input.link(RS);
            _logicAnalyzer.input.link(RCout);
            _logicAnalyzer.input.link(RCin);
            topLevel.connect(_logicAnalyzer.input, _S.output);


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

        String term = _ATextField.getText();
        double[] array = _string2DoubleArray(term);
        double[][] array2d = new double[1][];
        array2d[0] = array;
        DoubleMatrixToken t = new DoubleMatrixToken(array2d);
        _valueOfA.setToken(t);


        term = _BTextField.getText();
        array = _string2DoubleArray(term);
        array2d[0] = array;
        t = new DoubleMatrixToken(array2d);
        _valueOfB.setToken(t);




        try {

                _localDirector.setStopTime(_stopTime);

                // Start the CurrentTimeThread.
                Thread ctt = new CurrentTimeThread();
                ctt.start();

                /*
                // Start the simulation.
                // This won't start a thread.
                // FIXME: A BIG & UGLY HACK
                int beforeCount = Thread.activeCount(); // HACK
                Thread[] before = new Thread[beforeCount]; // HACK
                Thread.enumerate(before);  // HACK
                _manager.go(); //NON-HACK
                int afterCount = Thread.activeCount();  // HACK
                Thread[] after = new Thread[afterCount]; // HACK
                Thread.enumerate(after); // HACK
                for (int i = 0; i < afterCount; i++) { // HACK
                    Thread suspect = after[i]; //HACK
                    // find suspect in the before list.
                    boolean found = false; //HACK
                    for (int j = 0; j < beforeCount; j++) { //HACK
                        if (suspect == before[i]) { //HACK
                            found = true; //HACK
                            break; //HACK
                        } //HACK
                    } //HACK
                    if (!found) { //HACK
                        suspect.join(); //HACK
                        break; //HACK
                    } //HACK
                } //HACK
                */
                _manager.run();

        } catch (Exception ex) {
            System.err.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The thread that runs the simulation.
    private Thread simulationThread;

    // The actors involved in the topology.
    private DEClock _clock;


    // FIXME: Under jdk 1.2, the following can (and should) be private
    private DEDirector _localDirector;
    private Manager _manager;

    private TextField _stopTimeBox;
    private double _stopTime = 100.0;
    private Button _goButton;
    private TextField _ATextField;
    private TextField _BTextField;
    private Label _currentTimeLabel;

    // Attributes to be changed.
    private Parameter _valueOfA;
    private Parameter _valueOfB;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Given a string of the form "x1 x2 x3 x4 ..." where xi's are double
    // values, return an array of double.
    private double[] _string2DoubleArray(String s) {

        LinkedList temp = new LinkedList();
        s = s.trim();
        while (s.length() != 0) {
            int firstSpace = s.indexOf(' ');
            if (firstSpace == -1) {
                firstSpace = s.length();
            }
            String head = s.substring(0, firstSpace);
            Double xi = Double.valueOf(head);
            temp.insertLast(xi);
            s = s.substring(firstSpace);
            s = s.trim();
        }
        double[] retVal = new double[temp.size()];
        int index = 0;
        while (!temp.isEmpty()) {
            retVal[index] = ((Double)temp.take()).doubleValue();

            index++;
        }
        return retVal;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    // Show simulation progress.
    private class CurrentTimeThread extends Thread {
        public void run() {
            while (simulationThread.isAlive()) {
                // get the current time from director.
                double currenttime = _localDirector.getCurrentTime();
                _currentTimeLabel.setText("Current time = "+currenttime);
                try {
                    sleep(500);
                } catch (InterruptedException e) {}
            }
        }
    }


    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                if (simulationThread == null) {
                    simulationThread = new Thread(AdderApplet.this);
                }
                if (!(simulationThread.isAlive())) {
                    simulationThread = new Thread(AdderApplet.this);
                    // start() will eventually call the run() method.
                    simulationThread.start();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

}



