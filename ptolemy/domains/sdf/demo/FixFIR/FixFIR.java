/* An FixFIR applet that uses Ptolemy II SDF domain.

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

package ptolemy.domains.sdf.demo.FixFIR;

import javax.swing.BoxLayout;

import java.applet.Applet;
import java.util.Enumeration;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import ptolemy.data.*;
import ptolemy.data.Token;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.*;

import ptolemy.actor.*;
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
//// FixFIR System

/**
@author Bart Kienhuis
@version $Id$
*/

public class FixFIR extends SDFApplet implements QueryListener {

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

	    if ( name == "taps_1" ) {
                _fir.taps.setExpression(_query.stringValue("taps_1"));
            } else {
                if ( name == "taps_2" ) {
                    _fir_quantize.taps.setExpression(_query.stringValue("taps_2"));
                } else {
                    if ( name == "taps_3" ) {
                        _fir_fix.taps.setExpression(_query.stringValue("taps_3"));
                    }
                }

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

            _query = new Query();
            _query.setBackground(getBackground());
            _query.addLine("taps_1", "Regular double Taps", "[ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ]");
            _query.addLine("taps_2", "Quantized Taps", "quantize([ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ],6,2)");
            _query.addLine("taps_3", "FixPoint Taps", "fix([ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ],8,2)");
            _query.addQueryListener(this);
            getContentPane().add( _query );

            // Create and Impuls
            int pvalues[][] = {{1}};
            int pindexes[][] = {{0}};
            _pulse = new Pulse(_toplevel, "ramp");
            _pulse.indexes.setToken(new IntMatrixToken(pindexes));
            _pulse.values.setToken(new IntMatrixToken(pvalues));

            // FIR Filter Double
            double taps[][] = {{-.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609}};
            _fir = new FIRDouble(_toplevel, "FIR_double");
            _fir.interpolation.setToken(new IntToken(1));
            _fir.taps.setToken(new DoubleMatrixToken(taps));

            // FIR Filter Quantize
            _fir_quantize = new FIR(_toplevel, "FIR_quantize");
            _fir_quantize.interpolation.setToken(new IntToken(1));
            _fir_quantize.taps.setExpression("quantize( [ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ], 8, 2)");

            // FIR Filter Fixed
            _fir_fix = new FIR(_toplevel, "FIR_fix");
            _fir_fix.interpolation.setToken(new IntToken(1));
            _fir_fix.taps.setExpression("fix( [ -.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -.040609 ], 6, 2)");

            // Convert explicitly the stream of doubles into fixpoint
            // numbers
            _d2f = new DoubleToFix(_toplevel, "DoubleToFix");
            _d2f.precision.setToken(new StringToken("(2.14)"));
            _d2f.quantizer.setToken(new IntToken(0));

            _f2d = new FixToDouble(_toplevel, "FixToDouble");
            _f2d.precision.setToken(new StringToken("(2.14)"));
            _f2d.quantizer.setToken(new IntToken(0));

            // Create the Sequence plotter
            _myplot = new SequencePlotter(_toplevel, "plot");
            _myplot.place( getContentPane() );
            _myplot.plot.setOpaque(false);
            _myplot.plot.setGrid(false);
            _myplot.plot.setTitle("Fixed-point FIR Filter Transer Function");
            _myplot.plot.setXRange( -Math.PI/2, Math.PI/2 );
            _myplot.plot.setYRange( 1, -100);
            _myplot.plot.setYLabel("amplitude");
            _myplot.plot.setXLabel("angular frequency");
            _myplot.plot.setSize(500, 300);
            _myplot.xInit.setToken(new DoubleToken( -Math.PI/2 ));
            _myplot.xUnit.setToken(new DoubleToken( Math.PI/256 ));


            // Create a Hierarchical Composite Actor that
            // describes the FFT transform.

            _transform = new TypedCompositeActor(_toplevel,"transform");
            TypedIOPort input_transform =
                new TypedIOPort(_transform, "input", true, false);
            TypedIOPort output_transform  =
                new TypedIOPort(_transform, "output", false, true);


            // Multiply the incoming stream with the alternating
            // sequence of the Pulse
            _mult = new MultiplyDivide(_transform, "Multiply");

            // Multiply the incoming stream with alternating
            // 1, -1.
            _waveform = new Pulse(_transform, "Waveform");
            int values[][] = {{1,-1}};
            int indexes[][] = {{0,1}};
            _waveform.indexes.setToken(new IntMatrixToken(indexes));
            _waveform.values.setToken(new IntMatrixToken(values));
            _waveform.repeat.setToken(new BooleanToken(true));

            // Perform an FFT
            _fft = new FFT(_transform, "FFT");

            // Go from Complex numbers to Real Number
            _ctor = new ComplexToReal(_transform, "ComplexToReal");

            // Go from Rectangular coordinates to Polar coordinates
            _rtop = new RectangularToPolar(_transform,"RecToPolar");

            // Go from Real to Decible
            _rtod = new dB(_transform, "dB");

            // Connect the actors inside the FFT transform
            _transform.connect( input_transform, _mult.multiply );
            _transform.connect( _waveform.output, _mult.multiply );
            _transform.connect( _mult.output,  _fft.input );
            _transform.connect( _fft.output, _ctor.input);
            _transform.connect( _ctor.realOutput, _rtop.xInput );
            _transform.connect( _ctor.imagOutput, _rtop.yInput );
            _transform.connect( _rtop.magnitudeOutput, _rtod.input);
            _transform.connect( _rtod.output, output_transform );



            // Clone the FFT Transform two more times.
            _transform_1 = (TypedCompositeActor)_transform.clone();
            TypedIOPort input_transform_1
                = (TypedIOPort) _transform_1.getPort("input");
            TypedIOPort output_transform_1
                = (TypedIOPort) _transform_1.getPort("output");
            _transform_1.setName("transform_1");
            _transform_1.setContainer( _toplevel );

            _transform_2 = (TypedCompositeActor)_transform.clone();
            TypedIOPort input_transform_2
                = (TypedIOPort) _transform_2.getPort("input");
            TypedIOPort output_transform_2
                = (TypedIOPort) _transform_2.getPort("output");
            _transform_2.setName("transform_2");
            _transform_2.setContainer( _toplevel );

            // Connect the actors at the toplevel
            Relation r = _toplevel.connect( _pulse.output, _fir.input );
            _fir_quantize.input.link( r );
            _d2f.input.link( r );

            _toplevel.connect( _fir.output, input_transform );
            _toplevel.connect( _fir_quantize.output, input_transform_1 );
            _toplevel.connect( _d2f.output,     _fir_fix.input );
            _toplevel.connect( _fir_fix.output, _f2d.input );
            _toplevel.connect( _f2d.output, input_transform_2 );

            _toplevel.connect( output_transform,   _myplot.input);
            _toplevel.connect( output_transform_1, _myplot.input);
            _toplevel.connect( output_transform_2, _myplot.input);


	    // DEBUG items
	    StreamListener sa = new StreamListener();
	    _fir_fix.addDebugListener(sa);
	    _fir.addDebugListener(sa);
	    _fir_quantize.addDebugListener(sa);

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



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Actors in the model.
    // toplevel
    private Ramp _ramp;
    private Pulse _pulse;

    private FIRDouble _fir;
    private FIR _fir_quantize;
    private FIR _fir_fix;

    private DoubleToFix _d2f;
    private FixToDouble _f2d;
    private SequencePlotter _myplot;

    // Transform
    private Pulse _waveform;
    private MultiplyDivide _mult;
    private FFT _fft;
    private ComplexToReal _ctor;
    private RectangularToPolar _rtop;
    private dB _rtod;

    // Copies of the FFT transform composite actor
    private TypedCompositeActor _transform;
    private TypedCompositeActor _transform_1;
    private TypedCompositeActor _transform_2;

    private Query _query;

    // Flag to prevent spurious exception being thrown by _go() method.
    // If this flag is not true, the _go() method will not execute the model.
    private boolean _initCompleted = false;

}





