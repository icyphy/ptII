/* An applet that uses Ptolemy II SDF domain.

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

package ptolemy.domains.sdf.demo;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import ptolemy.kernel.*;
import ptolemy.data.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.util.*;
import ptolemy.actor.util.PtolemyApplet;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// Pulses
/**
An applet that uses Ptolemy II SDF domain.

@author Edward A. Lee
@version $Id$
*/
public class Pulses extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            _query = new Query();
            _query.addQueryListener(new ParameterListener());
            _query.line("exbw", "Excess bandwidth (%)", "100");
            _query.line("symint", "Symbol interval", "16");
            _query.onoff("sqrt", "Square root pulse", false);
            add(_query);

            // The 1 argument requests only a go button.
            add(_createRunControls(1));

            // Create and configure impulse source
            Pulse impulse = new Pulse(_toplevel, "impulse");

            // configurable pulse
            _yours = new RaisedCosine(_toplevel, "yours");
            _yours.interpolation.setToken(new IntToken(64));
            _yours.root.setToken(new BooleanToken(false));

            // first filter
            RaisedCosine pulse1 = new RaisedCosine(_toplevel, "pulse1");
            pulse1.interpolation.setToken(new IntToken(64));

            // second filter
            RaisedCosine pulse2 = new RaisedCosine(_toplevel, "pulse2");
            pulse2.interpolation.setToken(new IntToken(64));
            pulse2.excessBW.setToken(new DoubleToken(0.5));

            // third filter
            RaisedCosine pulse3 = new RaisedCosine(_toplevel, "pulse3");
            pulse3.interpolation.setToken(new IntToken(64));
            pulse3.excessBW.setToken(new DoubleToken(0.25));

            // fourth filter
            RaisedCosine pulse4 = new RaisedCosine(_toplevel, "pulse4");
            pulse4.interpolation.setToken(new IntToken(64));
            pulse4.excessBW.setToken(new DoubleToken(0.0));

            // Create and configure plotter
            TimePlot myplot = new TimePlot(_toplevel, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(false);
            myplot.plot.setTitle("Transmit Pulse Shapes");
            myplot.plot.addLegend(0, "Yours");
            myplot.plot.addLegend(1, "100%");
            myplot.plot.addLegend(2, "50%");
            myplot.plot.addLegend(3, "25%");
            myplot.plot.addLegend(4, "0%");
            myplot.plot.setXRange(0.0, 64.0);
            myplot.plot.setYRange(-0.3, 1.0);
            myplot.plot.setSize(500, 300);
            myplot.timed.setToken(new BooleanToken(false));

            ComponentRelation r1 =
                _toplevel.connect(impulse.output, pulse1.input);
            pulse2.input.link(r1);
            pulse3.input.link(r1);
            pulse4.input.link(r1);
            _yours.input.link(r1);
            _toplevel.connect(_yours.output, myplot.input);
            _toplevel.connect(pulse1.output, myplot.input);
            _toplevel.connect(pulse2.output, myplot.input);
            _toplevel.connect(pulse3.output, myplot.input);
            _toplevel.connect(pulse4.output, myplot.input);

            // Get one iteration right away.
            _manager.run();
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
        _yours.excessBW.setToken
                (new DoubleToken(((double)_query.intValue("exbw"))/100.0));
        _yours.symbolInterval.setToken
                (new IntToken(_query.intValue("symint")));
        _yours.root.setToken
                (new BooleanToken(_query.booleanValue("sqrt")));
        super._go();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private Query _query;
    RaisedCosine _yours;

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
