/* An applet that uses Ptolemy II DE domain.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.de.demo.Inspection;

import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.BoxLayout;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.Manager;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.domains.de.gui.DEApplet;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// InspectionApplet
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
according to a Poisson process with mean interarrival time equal to
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
public class InspectionApplet extends DEApplet implements QueryListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the string "regular", then set the
     *  variable that controls whether bus arrivals will be regular
     *  or Poisson.  If the argument is anything else, update the
     *  parameters of the model from the values in the query boxes.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
        if (name == "regular") {
            _regular = _query.booleanValue("regular");
        }
        try {
            if (_regular) {
                _regularBus.period.setToken
                    (new DoubleToken(_query.doubleValue("busmean")));
            } else {
                _poissonBus.meanTime.setToken
                    (new DoubleToken(_query.doubleValue("busmean")));
                _passenger1.meanTime.setToken
                    (new DoubleToken(_query.doubleValue("passmean")));
            }
            _go();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Override the base class to display the recorded average.
     *
     */
    public void executionFinished(Manager manager) {
        super.executionFinished(manager);
        _query.setDisplay("average", _recorder.getLatest(0).toString());
    }

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            getContentPane().setLayout(
                    new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

            _query = new Query();
            _query.setBackground(getBackground());
            _query.addLine("busmean", "Bus mean interarrival time", "1.0");
            _query.addLine("passmean",
                    "Passenger mean interarrival time", "1.0");
            _query.addCheckBox("regular", "Regular bus arrivals", false);
            _query.addDisplay("average",
                    "Average waiting time of passengers", "");
            _query.addQueryListener(this);
            getContentPane().add(_query);

            if (_regular) {
                // Create regular bus source.
                _regularBus = new Clock(_toplevel, "regularBus");
                // Create Poisson bus source, but then remove from container,
                // since it won't be used unless the user toggles the button.
                _poissonBus = new Poisson(_toplevel,"poissonBus");
                _poissonBus.setContainer(null);
            } else {
                // Create Poisson bus source.
                _poissonBus = new Poisson(_toplevel, "poissonBus");
                // Create regular bus source, but then remove from container,
                // since it won't be used unless the user toggles the button.
                _regularBus = new Clock(_toplevel,"regularBus");
                _regularBus.setContainer(null);
            }
            // Set default parameters for both sources.
            _regularBus.values.setExpression("[2]");
            _regularBus.period.setToken(new DoubleToken(1.0));
            _regularBus.offsets.setExpression("[0.0]");
            _poissonBus.values.setExpression("[2]");
            _poissonBus.meanTime.setToken(new DoubleToken(1.0));

            // Create and configure passenger source
            _passenger1 = new Poisson(_toplevel, "passenger");
            _passenger1.values.setExpression("[1]");
            _passenger1.meanTime.setToken(new DoubleToken(1.0));

            // Waiting time
            _wait = new WaitingTime(_toplevel, "waitingTime");

            // Average actor
            Average average = new Average(_toplevel, "average");

            // Record the average
            _recorder = new Recorder(_toplevel, "recorder");

            // Create and configure plotter
            _eventplot = new TimedPlotter(_toplevel, "plot");
            _eventplot.place(getContentPane());
            _eventplot.plot.setBackground(getBackground());
            _eventplot.plot.setGrid(false);
            _eventplot.plot.setTitle("Events");
            _eventplot.plot.addLegend(0, "Bus");
            _eventplot.plot.addLegend(1, "Passengers");
            _eventplot.plot.addLegend(2, "Wait Time");
            _eventplot.plot.setXLabel("Time");
            _eventplot.plot.setXRange(0.0, _getStopTime());
            _eventplot.plot.setYRange(0.0, 4.0);
            _eventplot.plot.setConnected(false);
            _eventplot.plot.setImpulses(true);
            _eventplot.plot.setMarksStyle("dots");
            _eventplot.fillOnWrapup.setToken(new BooleanToken(false));

            // Create and configure histogram
            _histplot = new HistogramPlotter(_toplevel, "histplot");
            _histplot.place(getContentPane());
            _histplot.histogram.setBackground(getBackground());
            _histplot.histogram.setGrid(false);
            _histplot.histogram.setTitle("Histogram of Waiting Times");
            _histplot.histogram.setXLabel("Waiting Time");
            _histplot.histogram.setXRange(0.0, 6.0);
            _histplot.histogram.setYRange(0.0, 20.0);
            _histplot.histogram.addLegend(0, "Passengers");
            _histplot.histogram.setBinWidth(0.2);
            _histplot.fillOnWrapup.setToken(new BooleanToken(false));

            // Connections, except the bus source, which is postponed.
            _busRelation =
                _toplevel.connect(_wait.waitee, _eventplot.input);
            ComponentRelation rel2 =
                _toplevel.connect(_passenger1.output, _eventplot.input);
            _wait.waiter.link(rel2);
            ComponentRelation rel3 =
                _toplevel.connect(_wait.output, _eventplot.input);
            _histplot.input.link(rel3);
            average.input.link(rel3);
            _toplevel.connect(average.output, _recorder.input);
            _initCompleted = true;

            // The 2 argument requests a go and stop button.
            getContentPane().add(_createRunControls(2));

        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the model.  This overrides the base class to read the
     *  values in the query box first and set parameters.
     *  @exception IllegalActionException If topology changes on the
     *   model or parameter changes on the actors throw it.
     */
    protected void _go() throws IllegalActionException {
        // If an exception occurred during initialization, then we don't
        // want to run here.  The model is probably not complete.
        if (!_initCompleted) return;

        // If the manager is not idle then either a run is in progress
        // or the model has been corrupted.  In either case, we do not
        // want to run.
        if (_manager.getState() != _manager.IDLE) return;

        // Depending on the state of the radio button, we may want
        // either regularly spaced bus arrivals, or Poisson arrivals.
        // Here, we alter the model topology to implement one or the other.
        if (_regular) {
            try {
                _poissonBus.setContainer(null);
                _regularBus.setContainer(_toplevel);
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(ex.toString());
            }
            _regularBus.period.setToken
                (new DoubleToken(_query.doubleValue("busmean")));
            _regularBus.output.link(_busRelation);
        } else {
            try {
                _regularBus.setContainer(null);
                _poissonBus.setContainer(_toplevel);
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(ex.toString());
            }
            _poissonBus.meanTime.setToken
                (new DoubleToken(_query.doubleValue("busmean")));
            _passenger1.meanTime.setToken
                (new DoubleToken(_query.doubleValue("passmean")));
            _poissonBus.output.link(_busRelation);
        }

        // The the X range of the plotter to show the full run.
        // The method being called is a protected member of DEApplet.
        _eventplot.plot.setXRange(0.0, _getStopTime());

        // Clear the average display.
        _query.setDisplay("average", "");

        // The superclass sets the stop time of the director based on
        // the value in the entry box on the screen.  Then it starts
        // execution of the model in its own thread, leaving the user
        // interface of this applet live.
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Actors in the model.
    private Query _query;
    private Poisson _poissonBus;
    private Clock _regularBus;
    private Poisson _passenger1;
    private TimedPlotter _eventplot;
    private HistogramPlotter _histplot;
    private WaitingTime _wait;

    // An indicator of whether regular or Poisson bus arrivals are
    // desired.
    private boolean _regular = false;

    // The relation to which links are made and unmade in response to
    // changes in the radio button state that selects regular or Poisson
    // bus arrivals.
    private ComponentRelation _busRelation;

    // Flag to prevent spurious exception being thrown by _go() method.
    // If this flag is not true, the _go() method will not execute the model.
    private boolean _initCompleted = false;

    // The observer of the average.
    private Recorder _recorder;
}
