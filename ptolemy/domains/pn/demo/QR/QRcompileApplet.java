/* An applet that uses Ptolemy II PN domains.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.pn.sbf.demo;

import javax.swing.BoxLayout;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.Manager;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;

import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
import ptolemy.domains.pn.demo.QR.*;
import ptolemy.domains.pn.gui.PNApplet;

//////////////////////////////////////////////////////////////////////////
////  QRcompileApplet
/**
An applet that models the compiled QR algorithm.

@author Bart Kienhuis
@version $Id$
*/
public class QRcompileApplet extends PNApplet implements QueryListener {

    // Flag to prevent spurious exception being thrown by _go() method.
    // If this flag is not true, the _go() method will not execute the model.
    private boolean _initCompleted = false;

    public  boolean DEBUG = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Initialize the applet.
     */
    public void init() {
	super.init();
	try {

	    // setSize(600, 600);


            getContentPane().setLayout(
                    new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

	    _query = new Query();
            _query.setBackground(_getBackground());
            _query.addQueryListener(this);
            _query.addLine("K", "Number of QRupdate Iterations", "10");
            _query.addLine("N", "Number of Antenna's", "6");

            getContentPane().add( _query );

            /*
              Parameter param =
              (Parameter)_director.getAttribute("Initial_queue_capacity");
              param.setToken(new IntToken(10));
	    */

	    _printplot = new Print(_toplevel, "print");
            _printplot.place(getContentPane());

	    _eventplot = new SequencePlotter(_toplevel, "plot");
            _eventplot.place(getContentPane());

	    _eventplot.plot.setGrid(false);
	    _eventplot.plot.setTitle("Events");
	    _eventplot.plot.addLegend(0, "Matrix R");
	    _eventplot.plot.setXLabel("Position");
	    _eventplot.plot.setYLabel("R Values");
	    _eventplot.plot.setXRange(0.0, 21.0);
	    _eventplot.plot.setYRange(-1.0, 1000.0);
	    _eventplot.plot.setSize(450,200);
	    _eventplot.plot.setConnected(false);
	    _eventplot.plot.setImpulses(true);
	    _eventplot.plot.setMarksStyle("dots");
	    _eventplot.fillOnWrapup.setToken(new BooleanToken(false)); 	    
	    // _eventplot.timed.setToken(new BooleanToken(false)); 

	    // Construct the Ptolemy kernel topology
	    _ND_6  = new ND_6(_toplevel,"ND_6");
	    _ND_66 = new ND_66(_toplevel,"ND_66");
	    _ND_14 = new ND_14(_toplevel,"ND_14");
	    _ND_36 = new ND_36(_toplevel,"ND_36");
	    _ND_86 = new ND_86(_toplevel,"ND_86");

	    _copy = new Pass12(_toplevel,"Copy");
	    _copy2 = new Pass12(_toplevel,"Copy_AGAIN");

            _s2m = new StreamToMatrix(_toplevel,"StreamToMatrix");

            _matrixViewer = new MatrixViewer(_toplevel,"MatrixViewer");
            _matrixViewer.place(getContentPane());

	    System.out.println(" -- 1.) Process Nodes instantiated -- ");

	    _toplevel.connect(_ND_6.out0, _ND_36.in1);
	    _toplevel.connect(_ND_6.out1, _ND_66.in1);

	    _toplevel.connect(_ND_14.out0, _ND_36.in3);
	    _toplevel.connect(_ND_14.out1, _ND_66.in3);

	    _toplevel.connect(_ND_36.out0, _ND_36.in0);
	    _toplevel.connect(_ND_36.out1, _ND_86.in0);
	    _toplevel.connect(_ND_36.out2, _ND_66.in5);

	    _toplevel.connect(_ND_66.out0, _ND_66.in0);
	    _toplevel.connect(_ND_66.out1, _ND_86.in1);
	    _toplevel.connect(_ND_66.out2, _ND_36.in2);
	    _toplevel.connect(_ND_66.out3, _ND_66.in2);
	    _toplevel.connect(_ND_66.out4, _ND_66.in4);

	    _toplevel.connect(_ND_86.out0, _copy.in0);


	    _toplevel.connect(_copy.out0, _eventplot.input);
	    _toplevel.connect(_copy.out1, _copy2.in0);
	    _toplevel.connect(_copy2.out0, _printplot.input);

	    System.out.println(" -- 1a.) Process Nodes instantiated -- ");
	    _toplevel.connect(_copy2.out1, _s2m.input);
	    System.out.println(" -- 1b.) Process Nodes instantiated -- ");
	    _toplevel.connect(_s2m.output, _matrixViewer.input);
	    System.out.println(" -- 1c.) Process Nodes instantiated -- ");

            _initCompleted = true;

	    System.out.println(" -- 2.) Connections made - DONER - ");

	    
	    // _query = new Query();
            // _query.addQueryListener(new ParameterListener());
            // add(_query);

            // The 2 argument requests a go and stop button.
            getContentPane().add(_createRunControls(2));
            
	    // String ds = _toplevel.description();
	    // System.err.print(ds);
            // _manager.execute();

	    System.out.println("-- 3.) QRupdate installed properly -- ");

	    System.out.println("-- 4.) Call the manager -- ");
            
            // _manager.run();

            // super._go();
	
	} catch (Exception ex) {
            report("Setup failed:", ex);
        }

	return;
    }

    protected void _go() throws IllegalActionException {
        
        // If an exception occurred during initialization, then we don't
        // want to run here.  The model is probably not complete.
        if (!_initCompleted) return;

        // If the manager is not idle then either a run is in progress
        // or the model has been corrupted.  In either case, we do not
        // want to run.
        if (_manager.getState() != _manager.IDLE) return;
        
        super._go();

    }
        
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the string "regular", then set the
     *  variable that controls whether bus arrivals will be regular
     *  or Poisson.  If the argument is anything else, update the
     *  parameters of the model from the values in the query boxes.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {

        try {
            _ND_6.parameter_K.
                setToken(new IntToken((int)_query.intValue("K")));
            _ND_14.parameter_K.
                setToken(new IntToken((int)_query.intValue("K")));
            _ND_66.parameter_K.
                setToken(new IntToken((int)_query.intValue("K")));
            _ND_36.parameter_K.
                setToken(new IntToken((int)_query.intValue("K")));
            _ND_86.parameter_K.
                setToken(new IntToken((int)_query.intValue("K")));

            _s2m.dimension.
                setToken(new IntToken((int)_query.intValue("N")));

            _ND_6.parameter_N.
                setToken(new IntToken((int)_query.intValue("N")));
            _ND_14.parameter_N.
                setToken(new IntToken((int)_query.intValue("N")));
            _ND_66.parameter_N.
                setToken(new IntToken((int)_query.intValue("N")));
            _ND_36.parameter_N.
                setToken(new IntToken((int)_query.intValue("N")));
            _ND_86.parameter_N.
                setToken(new IntToken((int)_query.intValue("N")));


            System.out.println(" -- parameter values changed, calculating new results -- ");
            _go();

        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ND_6     _ND_6;
    private ND_66    _ND_66;
    private ND_14    _ND_14;
    private ND_36    _ND_36;
    private ND_86    _ND_86;
    private Pass12   _copy;
    private Pass12   _copy2;
    private MatrixViewer   _matrixViewer;
    private StreamToMatrix _s2m;
    
    private Print             _printplot;    
    private SequencePlotter   _eventplot;    
    private Query             _query;

}



