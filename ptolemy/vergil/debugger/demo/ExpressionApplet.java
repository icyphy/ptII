/* An applet that uses Ptolemy II SDF domain.

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

package ptolemy.domains.sdf.demo.Expression;

import java.awt.BorderLayout;
import java.util.Enumeration;
import java.lang.Math;
import javax.swing.JPanel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.sdf.gui.SDFApplet;
import ptolemy.domains.sdf.demo.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// ExpressionApplet
/**
A demonstration of the Expression actor.  This applet feeds two ramp
signals, one slowly rising and one quickly rising, into two inputs
named "slow" and "fast" of an Expression actor.  That actor evaluates
whatever expression you give it in the on-screen entry box and sends
the result to a plotter.

@author Edward A. Lee
@version $Id$
*/
public class ExpressionApplet extends TypedCompositeActor {

    TypedIOPort sortie;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** After invoking super.init(), create and connect the actors.
     *  Also, create an on-screen entry box for the expression to evaluate.
     */
    public ExpressionApplet() {
	super("CompActor");
	sortie = new TypedIOPort(this, "sortie", true, false);

        try {
            // Create and configure ramp1
            Ramp ramp1 = new Ramp(this, "ramp1");
            ramp1.init.setToken(new DoubleToken(0.0));
            ramp1.step.setToken(new DoubleToken(0.01*Math.PI));

            // Create and configure ramp2
            Ramp ramp2 = new Ramp(this, "ramp2");
            ramp2.init.setToken(new DoubleToken(0.0));
            ramp2.step.setToken(new DoubleToken(0.1*Math.PI));

            // Create and configure expr
            _expr = new Expression(this, "expr");
            TypedIOPort slow = new TypedIOPort(_expr, "slow", true, false);
            TypedIOPort fast = new TypedIOPort(_expr, "fast", true, false);

            // Create and configure plotter
	    //           SequencePlotter plotter = new SequencePlotter(_toplevel, "plot");

            // Place the plotter in the applet in such a way that it fills
            // the available space.
	    //            plotter.place(getContentPane());

	    //            plotter.plot.setBackground(getBackground());
	    //            plotter.plot.setGrid(false);
	    //            plotter.plot.setXRange(0.0, 200.0);
	    //            plotter.plot.setYRange(-2.0, 2.0);

            this.connect(ramp1.output, slow);
            this.connect(ramp2.output, fast);
            this.connect(_expr.output, this.sortie.input);
	    
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }


   
}
