/* An applet that uses the Ptolemy II PN domain

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.domains.pn.demo.QR;

import javax.swing.BoxLayout;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.*;
import ptolemy.actor.lib.gui.MatrixViewer;
import ptolemy.actor.lib.gui.SequencePlotter;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

import java.net.URL;

import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.demo.QR.*;
import ptolemy.domains.pn.gui.PNApplet;

import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
////  QR
/**
An applet that models the compiled QR algorithm.

@author Bart Kienhuis
@version $Id$
*/
public class QR extends PNApplet implements QueryListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {

            setSize(600, 600);

            getContentPane().setLayout(
                    new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

            _query = new Query();
            _query.setBackground(getBackground());
            _query.addQueryListener(this);
            _query.addLine("K", "Number of QR update iterations", "10");
            _query.addLine("N", "Number of Antenna's", "6");

            getContentPane().add( _query );

            _eventplot = new SequencePlotter(_toplevel, "plot");
            _eventplot.place(getContentPane());
            _eventplot.plot.setBackground(getBackground());
            _eventplot.plot.setGrid(false);
            _eventplot.plot.setTitle("Matrix R");
            _eventplot.plot.addLegend(0, "Matrix R");
            _eventplot.plot.setXLabel("Position");
            _eventplot.plot.setYLabel("R Values");
            _eventplot.plot.setXRange(0.0, 21.0);
            _eventplot.plot.setYRange(-1.0, 1000.0);
            _eventplot.plot.setSize(450, 200);
            _eventplot.plot.setConnected(false);
            _eventplot.plot.setImpulses(true);
            _eventplot.plot.setMarksStyle("dots");
            _eventplot.fillOnWrapup.setToken(new BooleanToken(false));
            // _eventplot.timed.setToken(new BooleanToken(false));

            // Construct the Ptolemy kernel topology
            _ND_1 = new ND_1(_toplevel,"ND_1");
            _ND_2 = new ND_2(_toplevel,"ND_2");
            _ND_3 = new ND_3(_toplevel,"ND_3");
            _ND_4 = new ND_4(_toplevel,"ND_4");
            _ND_5 = new ND_5(_toplevel,"ND_5");

            _s2m = new StreamToMatrix(_toplevel,"StreamToMatrix");
            _matrixViewer = new MatrixViewer(_toplevel,"MatrixViewer");
            _matrixViewer.height.setToken(new IntToken(10));
            _matrixViewer.width.setToken(new IntToken(10));;
            _matrixViewer.place(getContentPane());;


            // Connect the network
            _toplevel.connect(_ND_1.WP_2, _ND_3.RP_2);
            _toplevel.connect(_ND_1.WP_6, _ND_4.RP_6);
            _toplevel.connect(_ND_2.WP_4, _ND_3.RP_4);

            _toplevel.connect(_ND_2.WP_8, _ND_4.RP_8);

            _toplevel.connect(_ND_3.WP_1,  _ND_3.RP_1);
            _toplevel.connect(_ND_3.WP_11, _ND_5.RP_11);
            _toplevel.connect(_ND_3.WP_10, _ND_4.RP_10);

            _toplevel.connect(_ND_4.WP_5,  _ND_4.RP_5);
            _toplevel.connect(_ND_4.WP_12, _ND_5.RP_12);
            _toplevel.connect(_ND_4.WP_3,  _ND_3.RP_3);
            _toplevel.connect(_ND_4.WP_7,  _ND_4.RP_7);
            _toplevel.connect(_ND_4.WP_9,  _ND_4.RP_9);

            // Split the output in two.
            Relation t = _toplevel.connect(_ND_5.out, _eventplot.input);
            _s2m.input.link( t );
            _toplevel.connect(_s2m.output, _matrixViewer.input);

            // Propagate the parameter settings
	    changed("N");
	    changed("K");

            _initCompleted = true;

            System.out.println(_toplevel.exportMoML());

            // The 2 argument requests a go and stop button.
            getContentPane().add(_createRunControls(2));

        } catch (Exception ex) {
            report("Setup failed:", ex);
        }

        return;
    }

    /** Execute the model. This overrides the base class to read the
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

        super._go();

    }

    /** Changing of a parameter has occurred. First check if the
     *  parameter (K or N) falls within the range of allowed
     *  values. Then propagate the new parameter value to the various
     *  actors in the model. If a valid change of a parameter took
     *  place, execute the model again.
     *  @param name The name of the entry that changed.
     */
    public void changed(String name) {

        try{

            boolean done = false;
            if ( name == "K" ) {
                int k = (int)_query.intValue("K");
                if ( (0<k) && (k<501) ) {
                    _ND_1.parameter_K.
                        setToken(new IntToken(k));
                    _ND_2.parameter_K.
                        setToken(new IntToken(k));
                    _ND_3.parameter_K.
                        setToken(new IntToken(k));
                    _ND_4.parameter_K.
                        setToken(new IntToken(k));
                    _ND_5.parameter_K.
                        setToken(new IntToken(k));
                    done = true;
                } else {
                    report("Please select a value between 1 < K < 500 ");
                }
            } else {
                if ( name == "N" ) {
                    int n = (int)_query.intValue("N");
                    if ( (0<n) && ( n<17) ) {
                        _s2m.dimension.
                            setToken(new IntToken(n));
                        _ND_1.parameter_N.
                            setToken(new IntToken(n));
                        _ND_2.parameter_N.
                            setToken(new IntToken(n));
                        _ND_3.parameter_N.
                            setToken(new IntToken(n));
                        _ND_4.parameter_N.
                            setToken(new IntToken(n));
                        _ND_5.parameter_N.
                            setToken(new IntToken(n));
                        done = true;
                    } else {
                        report("Please select a value between 1 < N < 16 ");
                    }
                }
            }
            if ( done ) {
                _go();
            }
        } catch (IllegalActionException ex ) {
            throw new InternalErrorException(ex.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag to prevent spurious exception being thrown by _go() method.
    // If this flag is not true, the _go() method will not execute the model.
    private boolean _initCompleted = false;

    // The actors in the QR process network
    private ND_1     _ND_1;
    private ND_2    _ND_2;
    private ND_3    _ND_3;
    private ND_4    _ND_4;
    private ND_5    _ND_5;

    private MatrixViewer   _matrixViewer;
    private StreamToMatrix _s2m;

    private SequencePlotter   _eventplot;
    private Query             _query;

}
