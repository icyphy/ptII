/* An applet that uses Ptolemy II DE domain.

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

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import ptolemy.kernel.*;
import ptolemy.data.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.util.*;
import ptolemy.actor.util.PtolemyApplet;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// InspectionParadox
/**
An applet that uses Ptolemy II DE domain to illustrate the inspection
paradox.  The inspection paradox concerns Poisson arrivals of events.
The metaphor used in this applet is that of busses and passengers.
Passengers arrive according to a Poisson process.
Busses arrive either at regular intervals or according to a Poisson
process.  The user selects from these two options by clicking the
appropriate on-screen control.  The user can also control the mean
interarrival time of both busses and passengers.
<p>
The inspection paradox concerns the average time that a passenger
waits for a bus (more precisely, the expected value).  If the busses
arrive at regular intervals with interarrival time equal to <i>T</i>,
then the expected waiting time is <i>T</i>/2, which is perfectly
intuitive.  Counterintuitively, however, if the busses arrive
according to a Poisson process with mean interarrival time equal
<i>T</i>, the expected waiting time is <i>T</i>, not <i>T</i>/2.
These expected waiting times are approximated in this applet by
the average waiting time.  The applet also shows that actual
arrival times for both passengers and busses, and the waiting
time of each passenger.
<p>
The intuition that resolves the paradox is as follows.
If the busses are arriving according to a Poisson process,
then some intervals between busses are larger than other intervals.
A particular passenger is more likely to arrive at the bus stop
during one of these larger intervals than during one of the smaller
intervals.  Thus, the expected waiting time is larger if the bus
arrival times are irregular.
<p>
This paradox is called the <i>inspection paradox</i> because the
passengers are viewed as inspecting the Poisson process of
bus arrivals.

@author Edward A. Lee and Lukito Muliadi
@version $Id$
*/
public class InspectionParadox extends DEApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            _query = new Query();
            _query.addQueryListener(new ParameterListener());
            _query.line("busmean", "Bus mean interarrival time", "1.0");
            _query.line("passmean", "Passenger mean interarrival time", "1.0");
            _query.onoff("regular", "Regular bus arrivals", false);
            add(_query);

            // The 2 argument requests a go and stop button.
            add(_createRunControls(2));

            // Create and configure bus source
            _bus = new DEPoisson(_toplevel, "bus");
            _bus.outputvalue.setToken(new DoubleToken(1.0));
            _bus.meantime.setToken(new DoubleToken(1.0));

            // Create and configure passenger source
            _passenger1 = new DEPoisson(_toplevel, "passenger");
            _passenger1.outputvalue.setToken(new DoubleToken(0.5));
            _passenger1.meantime.setToken(new DoubleToken(1.0));

            // Waiting time
            _wait = new DEWaitingTime(_toplevel, "waitingTime");

            // Average actor
            Average average = new Average(_toplevel, "average");

            // Display of average
            Show show = new Show(_toplevel, "show");
            show.setPanel(this);
            show.labels.setToken(new StringToken("Average waiting time"));

            // Create and configure plotter
            _eventplot = new PlotActor(_toplevel, "plot");
            _eventplot.setPanel(this);
            _eventplot.plot.setGrid(false);
            _eventplot.plot.setTitle("Events");
            _eventplot.plot.addLegend(0, "Bus");
            _eventplot.plot.addLegend(1, "Passenger");
            _eventplot.plot.addLegend(2, "Wait Time");
            _eventplot.plot.setXLabel("Time");
            _eventplot.plot.setYLabel("Wait time");
            _eventplot.plot.setXRange(0.0, _getStopTime());
            _eventplot.plot.setYRange(-1.0, 4.0);
            _eventplot.plot.setSize(450,200);
            _eventplot.plot.setConnected(false);
            _eventplot.plot.setImpulses(true);
            _eventplot.plot.setMarksStyle("dots");
            _eventplot.fillOnWrapup.setToken(new BooleanToken(false));

            // Create and configure histogram
            _histplot = new HistogramActor(_toplevel, "histplot");
            _histplot.setPanel(this);
            _histplot.histogram.setGrid(false);
            _histplot.histogram.setTitle("Histogram of Waiting Times");
            _histplot.histogram.setXLabel("Waiting Time");
            _histplot.histogram.setYLabel("Passengers");
            _histplot.histogram.setXRange(0.0, 7.0);
            _histplot.histogram.setYRange(0.0, 15.0);
            _histplot.histogram.setSize(450,200);
            _histplot.histogram.setBinWidth(0.2);
            _histplot.fillOnWrapup.setToken(new BooleanToken(false));

            // Connections
            ComponentRelation rel1 = 
                   _toplevel.connect(_bus.output, _eventplot.input);
            ComponentRelation rel2 = 
                   _toplevel.connect(_passenger1.output, _eventplot.input);
            _wait.waitee.link(rel1);
            _wait.waiter.link(rel2);
            ComponentRelation rel3 = 
                   _toplevel.connect(_wait.output, _eventplot.input);
            _histplot.input.link(rel3);
            average.input.link(rel3);
            _toplevel.connect(average.output, show.input);

            // Get one iteration right away.
            _go();
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first and set parameters.
     */
    protected void _go() {
        _bus.meantime.setToken
                (new DoubleToken(_query.doubleValue("busmean")));
        _passenger1.meantime.setToken
                (new DoubleToken(_query.doubleValue("passmean")));
        _eventplot.plot.setXRange(0.0, _getStopTime());
        super._go();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Query _query;
    private DEPoisson _bus;
    private DEPoisson _passenger1;
    private PlotActor _eventplot;
    private HistogramActor _histplot;
    private DEWaitingTime _wait;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener executes the system when any parameter is changed.
     */
    class ParameterListener implements QueryListener {
        public void changed(String name) {
            // _go();
        }
    }
}
