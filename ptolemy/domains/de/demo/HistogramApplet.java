/* An applet containing a discrete-event simulation.

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

//////////////////////////////////////////////////////////////////////////
//// HistogramApplet
/** 
A very simple applet containing a discrete-event simulation.

@author Lukito
@version $Id$
*/
public class HistogramApplet extends Applet {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */	
    public void init() {
        // The applet has two panels, stacked vertically
        setLayout(new BorderLayout());
        Panel appletPanel = new Panel();
        appletPanel.setLayout(new BorderLayout());
        
        add(appletPanel, "Center");

        // _plot is the drawing panel for DEPlot actor.
        _plot = new Plot();
        _plot.setSize(new Dimension(450, 150));
        _plot.setTitle("DE Demo");
        _plot.setButtons(true);
        _plot.addLegend(0, "Bus");
        _plot.addLegend(1, "Passenger");
        _plot.addLegend(2, "Wait Time");

        // _hist is the drawing panel for the DEHist actor.
        _hist = new Plot();
        _hist.setSize(new Dimension(450, 150));
        _hist.setTitle("Waiting Time");
        _hist.setButtons(true);
        _hist.addLegend(0, "Wait Time");

        Panel controlPanel = new Panel();
        controlPanel.setLayout(new GridLayout(10, 1));
        add(controlPanel, "West");

        controlPanel.add(_goButton);
        _goButton.addActionListener(new GoButtonListener());

        controlPanel.add(_stopTimeBox);
        _stopTimeBox.addActionListener(new StopTimeListener());

        controlPanel.add(_averageTextField);

        Panel checkboxPanel = new Panel();
        checkboxPanel.setLayout(new GridLayout(2,1));
        checkboxPanel.add(new Checkbox("one", _cbg, true));
        checkboxPanel.add(new Checkbox("two", _cbg, false));
        controlPanel.add(checkboxPanel);

        
        
        appletPanel.add(_plot, "North");
        appletPanel.add(_hist, "South");
        

        try {
            topLevel = new CompositeActor();
            topLevel.setName("Top");
        
            // Set up the directors
            _localDirector = new DECQDirector("DE Director");
            topLevel.setDirector(_localDirector);
            _executiveDirector = new Director("Executive Director");
            topLevel.setExecutiveDirector(_executiveDirector);
            
            // Set up the actors and connections
            DEClock clock = new DEClock(topLevel, "Clock", 1.0, 1.0);
            DEPoisson poisson = new DEPoisson(topLevel, "Poisson",-1.0,1.0);
            DEWaitingTime waitingTime = new DEWaitingTime(topLevel, "Wait");
            DEPlot plot = new DEPlot(topLevel, "Plot", _plot);
            DEHistogram hist = new DEHistogram(topLevel, "Histogram", 0.1, _hist);
            _stat = new DEStatistics(topLevel, "Stat");

            Relation r1 = topLevel.connect(clock.output, plot.input);
            Relation r2 = topLevel.connect(poisson.output, plot.input);
            waitingTime.waitee.link(r1);
            waitingTime.waiter.link(r2);
            Relation r3 = topLevel.connect(waitingTime.output, plot.input);
            hist.input.link(r3);
            _stat.input.link(r3);
        } catch (Exception ex) {
            System.err.println("Setup failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Plot _plot;
    private Plot _hist;
    private CompositeActor topLevel;
    private DEStatistics _stat;
    private CheckboxGroup _cbg = new CheckboxGroup();

    // FIXME: Under jdk 1.2, the following can (and should) be private
    public DECQDirector _localDirector;
    public Director _executiveDirector;

    public TextField _stopTimeBox = new TextField("10.0", 10);
    double _stopTime = 10.0;
    private Button _goButton = new Button("Go");
    
    private TextField _intervalTextField = new TextField("10.0", 10);
    double _interval = 10.0;
    
    private TextField _averageTextField = new TextField("0.0", 10);

    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    private class StopTimeListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            String timespec = _stopTimeBox.getText();
            try {
                Double spec = Double.valueOf(timespec);
                _stopTime = spec.doubleValue();
            } catch (NumberFormatException ex) {
                System.err.println("Invalid stop time: " + ex.getMessage());
            }
        }
    }

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                _localDirector.setStopTime(_stopTime);
                _executiveDirector.go();
                double average = _stat.getAverage();
                _averageTextField.setText("Ave: "+average);
            } catch (Exception ex) {
                System.err.println("Run failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
