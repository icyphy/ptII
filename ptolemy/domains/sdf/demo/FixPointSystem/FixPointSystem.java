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

import javax.swing.BoxLayout;

import java.applet.Applet;
import java.util.Enumeration;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import ptolemy.data.*;
import ptolemy.data.expr.*;

import ptolemy.actor.Manager;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.conversions.*;
import ptolemy.actor.gui.*;

import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.gui.SDFApplet;
import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// FixPoint System

/**
@author Bart Kienhuis
@version $Id$
*/

public class FixPointSystem extends SDFApplet implements QueryListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the string "regular", then set the
     *  variable that controls whether bus arrivals will be regular
     *  or Poisson.  If the argument is anything else, update the
     *  parameters of the model from the values in the query boxes.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {
	// System.out.println(" QUERY CHANGED TO: " + name);
        try {
	    if ( name == "precision" ) {
		 _doubleToFix_A.precision.setToken(new StringToken(_query.stringValue("precision")));
	    }
	    if ( name=="rounding" ) {
		_doubleToFix_A.mode.setToken(new StringToken(_query.stringValue("rounding")));
	    }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }

    }

    /** After invoking super.init(), create and connect the actors.
     */
    public void init() {
        super.init();

        // The 1 argument requests a go and a stop button.
        // add(_createRunControls(2));

        try {            

            getContentPane().setLayout(
                    new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	    String[] options = {"SATURATE", "TRUNCATE", "ZERO_SATURATE"};
	    
	    _query = new Query();
            _query.setBackground(_getBackground());
            _query.addLine("precision", "Precision of the FixTokens", "2.1");
	    _query.addRadioButtons("rounding","Rounding Mode",options,"Saturate");
            _query.addQueryListener(this);
            getContentPane().add( _query );

            // Pass12 copy = new Pass12(_toplevel,"copy");
	    
            // Create and configure data source
	    _ramp = new Ramp(_toplevel, "ramp");
	    _ramp.step.setToken(new DoubleToken(0.1));
	    _ramp.init.setToken(new IntToken(-1));

            // Create and configure data source
            _ramp1 = new Ramp(_toplevel, "ramp1");
	    _ramp1.step.setToken(new DoubleToken(0.1));
	    _ramp1.init.setToken(new IntToken(-1));

            
            // Create and configure coder
            _doubleToFix_A = new DoubleToFix(_toplevel, "tofix_A");
	    _doubleToFix_A.precision.setToken(new StringToken("(2.1)"));
            _doubleToFix_A.mode.setToken(new StringToken("SATURATE"));	    
            
            /*
            _doubleToFix_B = new DoubleToFix(_toplevel, "tofix_B");
	    _doubleToFix_B.precision.setToken(new StringToken("(2.0)"));
	    _doubleToFix_A.mode.setToken(new StringToken("SATURATE"));	    
            */

            
            // Create and configure coder
            _fixToDouble_A = new FixToDouble(_toplevel, "todoubleA");
            
            /*
            _fixToDouble_B = new FixToDouble(_toplevel, "todoubleB");
            */

            // Create and configure plotter
            _myplot = new SequencePlotter(_toplevel, "plot");
            _myplot.place( getContentPane() );
            _myplot.plot.setGrid(false);
            _myplot.plot.setTitle("Ramp Diagram");
            _myplot.plot.setXRange( 0, 200 );
            _myplot.plot.setWrap(true);
            _myplot.plot.setYRange(-10, 10);
            _myplot.plot.setMarksStyle("dots");
            // _myplot.plot.setPointsPersistence(1512);
            _myplot.plot.setSize(500, 300);

            /*
            // Create and configure plotter
            _myplot1 = new SequencePlotter(_toplevel, "plot1");
            _myplot1.place( getContentPane() );
            _myplot1.plot.setGrid(false);
            _myplot1.plot.setTitle("Ramp Diagram");
            _myplot1.plot.setXRange(-50.0, 50.0);
            _myplot1.plot.setWrap(true);
            _myplot1.plot.setYRange(-10, 10);
            _myplot1.plot.setMarksStyle("dots");
            // _myplot.plot.setPointsPersistence(1512);
            _myplot1.plot.setSize(500, 300);
            */

            // _toplevel.connect(ramp.output, doubleToFix_A.input);
            // _toplevel.connect(doubleToFix_A.output, fixToDouble_A.input);
            // _toplevel.connect(fixToDouble_A.output, myplot.input);
            // _toplevel.connect(ramp.output, copy.in0);
            // _toplevel.connect(copy.out0, myplot.input);
            // _toplevel.connect(copy.out1, myplot.input);


            _toplevel.connect( _ramp.output, _doubleToFix_A.input);
            _toplevel.connect( _doubleToFix_A.output, _fixToDouble_A.input);
            _toplevel.connect( _fixToDouble_A.output, _myplot.input);
           
            _toplevel.connect( _ramp1.output, _myplot.input);
         
            
            // _toplevel.connect(ramp1.output, doubleToFix_B.input);
            // _toplevel.connect(doubleToFix_B.output, fixToDouble_B.input);
            // _toplevel.connect(fixToDouble_B.output, myplot1.input);
       
            // _toplevel.connect(ramp1.output, myplot.input);


            // The 2 argument requests a go and stop button.
            getContentPane().add(_createRunControls(2));
            
        } catch (Exception ex) {
            report("Setup failed:", ex);
        }
    }

    // Actors in the model.
    private Query _query;
    private DoubleToFix _doubleToFix_A;
    private DoubleToFix _doubleToFix_B;
    private FixToDouble _fixToDouble_A;
    private FixToDouble _fixToDouble_B;
    private SequencePlotter _myplot;
    private SequencePlotter _myplot1;
    private Ramp _ramp;
    private Ramp _ramp1;

}





