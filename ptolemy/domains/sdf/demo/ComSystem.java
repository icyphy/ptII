/* An applet that uses Ptolemy II SDF domain.

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
//// ComSystem
/**
An applet that uses Ptolemy II SDF domain.

@author Edward A. Lee
@version $Id$
*/
public class ComSystem extends PtolemyApplet {

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            // Initialization
            // FIXME: Trim this.
            _stopTimeBox = new TextField("30.0", 10);
            _goButton = new Button("Go");
            
            // FIXME: Use longer names.
            _manager = new Manager();
            TypedCompositeActor c = new TypedCompositeActor();
            c.setName("ComSystem");
            SDFDirector d = new SDFDirector();
            SDFScheduler s = new SDFScheduler();
            IORelation r;
            Parameter p = (Parameter) d.getAttribute("Iterations");
            p.setToken(new IntToken(1000));
            
            c.setDirector(d);
            c.setManager(_manager);
            d.setScheduler(s);
            d.setScheduleValid(false);
            
            // Create and configure ramp
            Ramp ramp = new Ramp(c, "ramp");
            ramp.init.setToken(new DoubleToken(1.0));
            ramp.step.setToken(new DoubleToken(1.0));

            // Create and configure expr
            Expression expr = new Expression(c, "expr");
            TypedIOPort exprinput = new TypedIOPort(expr,"input");
            expr.expression.setToken(new StringToken("3.0*input"));

            // Create and configure plotter
            TimePlot myplot = new TimePlot(c, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(false);
            myplot.plot.setMarksStyle("dots");
            myplot.plot.setTitle("Eye Diagram");
            myplot.plot.setXRange(0.0, 10.0);
            myplot.plot.setWrap(true);
            myplot.plot.setYRange(-100.0, 1.0);
            myplot.plot.setPointsPersistence(100);

            myplot.timed.setToken(new BooleanToken(false));

            c.connect(ramp.output, exprinput, "R1");
            c.connect(expr.output, myplot.input, "R2");

            // Add a control panel in the main panel.
            Panel controlPanel = new Panel();
            add(controlPanel);
            controlPanel.add(_goButton);
            
            _goButton.addActionListener(new GoButtonListener());
        } catch (Exception ex) {
            report("Setup failed: ", ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    private TextField _stopTimeBox;
    private double _stopTime = 10.0;
    private Button _goButton;
    private Manager _manager;

    //////////////////////////////////////////////////////////////////////////
    ////                       inner classes                              ////

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _manager.startRun();
        }
    }
}
