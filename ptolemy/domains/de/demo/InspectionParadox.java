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
An applet that uses Ptolemy II DE domain to illustrate the inpection
paradox.  The inspection paradox deals with Poisson arrivals of events.

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
            _query.line("mean", "Mean interarrival time", "0.2");
            _query.onoff("regular", "Regular arrivals", false);
            add(_query);

            // The 2 argument requests a go and stop button.
            add(_createRunControls(2));

            // Create and configure Poisson source
            _poisson = new DEPoisson(_toplevel, "poisson");
            _poisson.outputvalue.setToken(new DoubleToken(1.0));
            _poisson.meantime.setToken(new DoubleToken(0.1));

            // Create and configure plotter
            _eventplot = new TimePlot(_toplevel, "plot");
            _eventplot.setPanel(this);
            _eventplot.plot.setGrid(false);
            _eventplot.plot.setTitle("Transmit Pulse Shapes");
            _eventplot.addLegend(0, "Bus");
            _eventplot.addLegend(1, "Passenger");
            _eventplot.addLegend(2, "Wait Time");
            _eventplot.setXLabel("Time");
            _eventplot.setYLabel("Wait time");
            _eventplot.plot.setXRange(0.0, _getStopTime());
            _eventplot.plot.setYRange(0.0, 2.0);
            _eventplot.plot.setSize(450,150);
            _eventplot.plot.setConnected(false);
            _eventplot.plot.setImpulses(true);
            _eventplot.plot.setMarksStyle("dots");

            _toplevel.connect(_poisson.output, _eventplot.input);

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
        _poisson.meantime.setToken
                (new DoubleToken(_query.doubleValue("mean")));
        _eventplot.plot.setXRange(0.0, _getStopTime());
        super._go();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Query _query;
    private DEPoisson _poisson;
    private TimePlot _eventplot;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener executes the system when any parameter is changed.
     */
    class ParameterListener implements QueryListener {
        public void changed(String name) {
            _go();
        }
    }
}
