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
import java.lang.Math;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.util.*;
import ptolemy.actor.util.PtolemyApplet;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// ExpressionApplet
/**
An applet that uses Ptolemy II SDF domain.

@author Edward A. Lee
@version $Id$
*/
public class ExpressionApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            Panel controlpanel = new Panel();
            controlpanel.setLayout(new BorderLayout());
            add(controlpanel);

            _query = new Query();
            _query.addQueryListener(new ParameterListener());
            controlpanel.add("West", _query);
            _query.line("expr", "Expression", "cos(slow) + cos(fast)", 30);

            // Create a "Go" button.
            Panel runcontrols = new Panel();
            controlpanel.add("East", runcontrols);
            runcontrols.add(_createRunControls(1));

            // Create and configure ramp1
            Ramp ramp1 = new Ramp(_toplevel, "ramp1");
            ramp1.init.setToken(new DoubleToken(0.0));
            ramp1.step.setToken(new DoubleToken(0.01*Math.PI));

            // Create and configure ramp2
            Ramp ramp2 = new Ramp(_toplevel, "ramp2");
            ramp2.init.setToken(new DoubleToken(0.0));
            ramp2.step.setToken(new DoubleToken(0.1*Math.PI));

            // Create and configure expr
            _expr = new Expression(_toplevel, "expr");
            TypedIOPort slow = new TypedIOPort(_expr, "slow", true, false);
            TypedIOPort fast = new TypedIOPort(_expr, "fast", true, false);

            // Create and configure plotter
            TimePlot myplot = new TimePlot(_toplevel, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(false);
            myplot.plot.setXRange(0.0, 200.0);
            myplot.plot.setYRange(-2.0, 2.0);
            myplot.plot.setSize(500, 300);
            myplot.timed.setToken(new BooleanToken(false));

            _toplevel.connect(ramp1.output, slow);
            _toplevel.connect(ramp2.output, fast);
            _toplevel.connect(_expr.output, myplot.input);
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the system.  This overrides the base class to read the
     *  values in the query box first.
     */
    protected void _go() {
        _expr.expression.setToken(new StringToken(_query.stringValue("expr")));
        super._go();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    private Query _query;
    private Expression _expr;

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
