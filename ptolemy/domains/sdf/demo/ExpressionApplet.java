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
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
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

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            // Create and configure ramp
            Ramp ramp = new Ramp(_toplevel, "ramp");
            ramp.init.setToken(new DoubleToken(1.0));
            ramp.step.setToken(new DoubleToken(1.0));

            // Create and configure expr
            Expression expr = new Expression(_toplevel, "expr");
            TypedIOPort exprinput = new TypedIOPort(expr,"input");
            exprinput.setInput(true);
            expr.expression.setToken(new StringToken("3.0*input"));

            // Create and configure plotter
            TimePlot myplot = new TimePlot(_toplevel, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(false);
            myplot.plot.setMarksStyle("dots");
            myplot.plot.setTitle("Eye Diagram");
            myplot.plot.setXRange(0.0, 10.0);
            myplot.plot.setWrap(true);
            myplot.plot.setYRange(-100.0, 1.0);
            myplot.plot.setPointsPersistence(100);

            myplot.timed.setToken(new BooleanToken(false));

            _toplevel.connect(ramp.output, exprinput, "R1");
            _toplevel.connect(expr.output, myplot.input, "R2");
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }
}
