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
		 _doubleToFix.precision.setToken(new StringToken(_query.stringValue("precision")));
	    }
	    if ( name=="rounding" ) {
		_doubleToFix.mode.setToken(new StringToken(_query.stringValue("rounding")));
	    }
            _go();
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

	    String[] options = {"Saturate", "Rounding", "Truncate"};
	    
	    _query = new Query();
            _query.setBackground(_getBackground());
            _query.addLine("precision", "Precision of the FixTokens", "2.1");
	    _query.addRadioButtons("rounding","Rounding Mode",options,"Saturate");
            _query.addQueryListener(this);
            getContentPane().add( _query );

            // Create and configure data source
	    _ramp = new Ramp(_toplevel, "ramp");
	    _ramp.step.setToken(new DoubleToken(0.1));
	    _ramp.init.setToken(new IntToken(-1));

            // Create and configure data source
            _ramp1 = new Ramp(_toplevel, "ramp1");
	    _ramp1.step.setToken(new DoubleToken(0.1));
	    _ramp1.init.setToken(new IntToken(-1));

            
            // Create and configure coder
            _doubleToFix = new DoubleToFix(_toplevel, "tofix");
	    _doubleToFix.precision.setToken(new StringToken("(2.1)"));
            _doubleToFix.mode.setToken(new StringToken("SATURATE"));	    
            
            // Create and configure coder
            _fixToDouble = new FixToDouble(_toplevel, "todouble");
            
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

            _toplevel.connect( _ramp.output, _doubleToFix.input);
            _toplevel.connect( _doubleToFix.output, _fixToDouble.input);
            _toplevel.connect( _fixToDouble.output, _myplot.input);          
            _toplevel.connect( _ramp1.output, _myplot.input);

            // We initialize the model correctly.
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

        // The superclass sets the stop time of the director based on
        // the value in the entry box on the screen.  Then it starts
        // execution of the model in its own thread, leaving the user
        // interface of this applet live.
        super._go();
    }

    // Actors in the model.
    private Query _query;
    private DoubleToFix _doubleToFix;
    private FixToDouble _fixToDouble;
    private SequencePlotter _myplot;
    private SequencePlotter _myplot1;
    private Ramp _ramp;
    private Ramp _ramp1;

    // Flag to prevent spurious exception being thrown by _go() method.
    // If this flag is not true, the _go() method will not execute the model.
    private boolean _initCompleted = false;
}





