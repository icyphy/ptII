/* An applet that uses Ptolemy II PN domains.

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
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;

import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;

import java.net.URL;

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

    ////////////////////////////////////////////////////////////////////////
////                         public methods                         ////

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

        _s2m = new StreamToMatrix(_toplevel,"StreamToMatrix");
        _matrixViewer = new MatrixViewer(_toplevel,"MatrixViewer");
        _matrixViewer.place(getContentPane());
        _matrixViewer.setSize(1,1);

        // Connect the network
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

        // Split the output in two.
        Relation t = _toplevel.connect(_ND_86.out0, _eventplot.input);
        _s2m.input.link( t );
        _toplevel.connect(_s2m.output, _matrixViewer.input);

        _initCompleted = true;

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

    /** Changing of a parameter has occured. First check if the
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
                    _ND_6.parameter_K.
                        setToken(new IntToken(k));
                    _ND_14.parameter_K.
                        setToken(new IntToken(k));
                    _ND_66.parameter_K.
                        setToken(new IntToken(k));
                    _ND_36.parameter_K.
                        setToken(new IntToken(k));
                    _ND_86.parameter_K.
                        setToken(new IntToken(k));
                    done = true;
                } else {
                    report("Please select a value between 1 < K < 500 ");
                }
            } else {
                if ( name == "N" ) {
                    int n = (int)_query.intValue("N");
                    if ( (1<n) && ( n<17) ) {
                        _s2m.dimension.
                            setToken(new IntToken(n));
                        _ND_6.parameter_N.
                            setToken(new IntToken(n));
                        _ND_14.parameter_N.
                            setToken(new IntToken(n));
                        _ND_66.parameter_N.
                            setToken(new IntToken(n));
                        _ND_36.parameter_N.
                            setToken(new IntToken(n));
                        _ND_86.parameter_N.
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
    private ND_6     _ND_6;
    private ND_66    _ND_66;
    private ND_14    _ND_14;
    private ND_36    _ND_36;
    private ND_86    _ND_86;

    private MatrixViewer   _matrixViewer;
    private StreamToMatrix _s2m;

    private SequencePlotter   _eventplot;
    private Query             _query;

}



