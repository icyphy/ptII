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

package ptolemy.domains.sdf.demo.ComSystem;

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
import ptolemy.domains.sdf.demo.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// ComSystem
/**
This applet displays the eye diagram of a simple basedband communication
system.  The transmit pulse is a 100% excess-bandwidth square-root
raised-cosine pulse.  The channel adds Gaussian noise.  The receiver
consists of a matched filter.  The eye diagram for the signal after
the matched filter is shown.

@author Edward A. Lee
@version $Id$
*/
public class ComSystem extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** After invoking super.init(), create and connect the actors.
     */
    public void init() {
        super.init();
        // The 1 argument requests a go and a stop button.
        add(_createRunControls(2));
        try {
            // Create and configure data source
            CoinFlip data = new CoinFlip(_toplevel, "data");

            // Create and configure coder
            LineCoder coder = new LineCoder(_toplevel, "coder");

            // Create and configure noise source
            Gaussian noise = new Gaussian(_toplevel, "noise");
            noise.stddev.setToken(new DoubleToken(0.1));

            // Create the adder.
            Add add = new Add(_toplevel, "add");

            // Create the pulse-shaping filter.
            RaisedCosine shaper = new RaisedCosine(_toplevel, "shaper");
            shaper.interpolation.setToken(new IntToken(16));
            shaper.root.setToken(new BooleanToken(true));

            // Create the matched filter.
            RaisedCosine matched = new RaisedCosine(_toplevel, "matched");
            matched.interpolation.setToken(new IntToken(1));
            matched.root.setToken(new BooleanToken(true));

            // Create and configure plotter
            PlotActor myplot = new PlotActor(_toplevel, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(false);
            myplot.plot.setTitle("Eye Diagram");
            myplot.plot.setXRange(0.0, 32.0);
            myplot.plot.setWrap(true);
            myplot.plot.setYRange(-1.3, 1.3);
            myplot.plot.setMarksStyle("none");
            myplot.plot.setPointsPersistence(512);
            myplot.plot.setSize(500, 300);
            myplot.timed.setToken(new BooleanToken(false));

            _toplevel.connect(data.output, coder.input);
            _toplevel.connect(coder.output, shaper.input);
            _toplevel.connect(shaper.output, add.input);
            _toplevel.connect(noise.output, add.input);
            _toplevel.connect(add.output, matched.input);
            _toplevel.connect(matched.output, myplot.input);
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }
}
