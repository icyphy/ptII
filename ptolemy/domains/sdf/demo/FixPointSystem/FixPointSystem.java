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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.FixPointSystem;

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
import ptolemy.actor.gui.*;
import ptolemy.domains.sdf.gui.SDFApplet;
import ptolemy.domains.sdf.demo.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

//////////////////////////////////////////////////////////////////////////
//// FixPoint System
/**
@author Bart Kienhuis
@version $Id$
*/
public class FixPointSystem extends SDFApplet implements QueryListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the string "regular", then set the
     *  variable that controls whether bus arrivals will be regular
     *  or Poisson.  If the argument is anything else, update the
     *  parameters of the model from the values in the query boxes.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
	System.out.println(" QUERY CHANGED TO: " + name);
    }

    /** After invoking super.init(), create and connect the actors.
     */
    public void init() {
        super.init();
        // The 1 argument requests a go and a stop button.
        add(_createRunControls(2));
        try {            

	    _query = new Query();
            _query.setBackground(_getBackground());
            _query.addLine("precision", "Precision of the FixTokens", "16/2");
            add(_query);
            _query.addQueryListener(this);

	    
            // Create and configure data source
            Ramp ramp = new Ramp(_toplevel, "ramp");
	    ramp.step.setToken(new DoubleToken(0.1));

            // Create and configure coder
            DoubleToFix doubleToFix_A = new DoubleToFix(_toplevel, "tofix_A");
	    doubleToFix_A.precision.setToken(new StringToken("(2.1)"));
	    doubleToFix_A.mode.setToken(new StringToken("SATURATE"));	    

            DoubleToFix doubleToFix_B = new DoubleToFix(_toplevel, "tofix_B");
	    doubleToFix_B.precision.setToken(new StringToken("(2.2)"));
	    doubleToFix_A.mode.setToken(new StringToken("TRUNCATE"));	    

            // Create and configure coder
            FixToDouble fixToDouble_A = new FixToDouble(_toplevel, "todoubleA");
            FixToDouble fixToDouble_B = new FixToDouble(_toplevel, "todoubleB");

            // Create and configure plotter
            SequencePlotter myplot = new SequencePlotter(_toplevel, "plot");
            myplot.setPanel(this);
            myplot.plot.setGrid(false);
            myplot.plot.setTitle("Eye Diagram");
            myplot.plot.setXRange(0.0, 32.0);
            myplot.plot.setWrap(true);
            myplot.plot.setYRange(-1.3, 1.3);
            myplot.plot.setMarksStyle("dots");
            myplot.plot.setPointsPersistence(1512);
            myplot.plot.setSize(500, 300);

            _toplevel.connect(ramp.output, doubleToFix_A.input);
            _toplevel.connect(ramp.output, doubleToFix_B.input);

            _toplevel.connect(doubleToFix_A.output, fixToDouble_A.input);
            _toplevel.connect(doubleToFix_B.output, fixToDouble_B.input);

            _toplevel.connect(fixToDouble_A.output, myplot.input);
            _toplevel.connect(fixToDouble_B.output, myplot.input);
            _toplevel.connect(ramp.output, myplot.input);

        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }

    // Actors in the model.
    private Query _query;

}
