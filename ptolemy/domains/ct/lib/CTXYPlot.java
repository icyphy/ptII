/* An actor that plots the input data.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.lib;

import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.plot.*;
import java.awt.*;
import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// CTPlot
/**
A XY-plotter for continuous signals.

@author Jie Liu, Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class  CTXYPlot extends TypedAtomicActor {

    private static  boolean DEBUG = false;

    /** Construct a plot actor with a new plot window. The default 
     *  X-range and Y-range are both 
     *  [-1, 1]. 
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public CTXYPlot  (TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        this(container, name, null);

    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     *
     */
    public CTXYPlot (TypedCompositeActor container, String name, Plot plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        
        inputX = new TypedIOPort(this, "inputX");
        inputX.setInput(true);
        inputX.setMultiport(false);
        inputX.setDeclaredType(DoubleToken.class);
    
        inputY = new TypedIOPort(this, "inputY");
        inputY.setInput(true);
        inputY.setMultiport(false);
        inputY.setDeclaredType(DoubleToken.class);
        _plot = plot;

        // FIXME: This is not the right way to handle this...
        String legends = new String("");
        _yMin = (double)-1.0;
        _yMax = (double)1.0;
        _xMin = (double)-1.0;
        _xMax = (double)1.0;
        _paramLegends = new Parameter(this, "Legends", 
                new StringToken(legends));
        _paramYMin = new Parameter(this, "Y_Min", new DoubleToken(_yMin));
        _paramYMax = new Parameter(this, "Y_Max", new DoubleToken(_yMax));
        _paramXMin = new Parameter(this, "X_Min", new DoubleToken(_xMin));
        _paramXMax = new Parameter(this, "X_Max", new DoubleToken(_xMax));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

        /** Return the minimum value of the Y axis.
     *  @return The minimum value of the Y axis.
     */
    public double getYMin() {
        return _yMin;
    }

    /** Return the maximal value of the Y axis.
     *  @return The maximal value of the Y axis.
     */
    public double getYMax() {
        return _yMax;
    }
    
    /** Return the minimum value of the X axis.
     *  @return The minimum value of the X axis.
     */
    public double getXMin() {
        return _xMin;
    }

    /** Return the maximal value of the X axis.
     *  @return The maximal value of the X axis.
     */
    public double getXMax() {
        return _xMax;
    }

    /** Clear the plot window, adjust the right size as set in the
     *  parameters.
     */
    public void initialize()  {

        if (_plot == null) {
            _plot = new Plot();
            new PlotFrame(getName(), _plot);
        }
        _plot.clear(true);
        _plot.setButtons(true);
	_plot.setPointsPersistence(0);
        _plot.setImpulses(false);
        _plot.setConnected(true);
        _plot.setTitle(getName());

        // parameters
        _yMin = ((DoubleToken)_paramYMin.getToken()).doubleValue();
        _yMax = ((DoubleToken)_paramYMax.getToken()).doubleValue();
        _xMin = ((DoubleToken)_paramXMin.getToken()).doubleValue();
        _xMax = ((DoubleToken)_paramXMax.getToken()).doubleValue();
        
        String legs = ((StringToken)_paramLegends.getToken()).stringValue();
        //System.out.println(legs);
        if(!legs.equals("")) {
            StringTokenizer stokens = new StringTokenizer(legs);
            int index = 0;
            _legends = new String[stokens.countTokens()];
            while(stokens.hasMoreTokens()) {
                 _legends[index++]= stokens.nextToken();
                 //System.out.println(_legends[index-1]);
            }
        }
        _firstPoint = true;
        if (_legends != null  && _legends[0].length() != 0) {
            _plot.addLegend(0, _legends[0]);
        } else {
            _plot.addLegend(0, "Data ");    
        }
        
        // Initialization of the frame X-range is deferred until the fire()
        // phase, because the director doesn't know the start time until
        // some stars enqueue an event.
        _rangeInitialized = false;
    }

    /** Add new input data to the plot.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{

        if (!_rangeInitialized) {
            //_plot.setXRange(getStartTime(), getStopTime());
            _plot.setYRange(getYMin(), getYMax());
            _plot.setXRange(getXMin(), getXMax());
            _plot.init();
            _plot.repaint();
            _rangeInitialized = true;
        }

        if (inputX.hasToken(0) && inputY.hasToken(0)) {
            double xvalue =((DoubleToken)inputX.get(0)).doubleValue();
            double yvalue =((DoubleToken)inputY.get(0)).doubleValue();

            // update the ranges
            boolean yRangeChanged = false;
            boolean xRangeChanged = false;
            if (yvalue < _yMin) {
                yRangeChanged = true;
                _yMin = yvalue;
            }
            if (yvalue > _yMax) {
                yRangeChanged = true;
                _yMax = yvalue;
            }
            if (yRangeChanged) {
                _plot.setYRange(_yMin, _yMax);
                _plot.repaint();
            }
            if (xvalue < _xMin) {
                xRangeChanged = true;
                _xMin = xvalue;
            }
            if (xvalue > _xMax) {
                xRangeChanged = true;
                _xMax = xvalue;
            }
            if (xRangeChanged) {
                _plot.setXRange(_xMin, _xMax);
                _plot.repaint();
            }
            // draw the point
            if(_firstPoint) {
                _plot.addPoint(0, xvalue, yvalue, false);
                _firstPoint = false;
            } else {
                _plot.addPoint(0, xvalue, yvalue, true);
            }
        } else {
            throw new InternalErrorException(
                    "CT scheduling error. CTXYPlot fired, but there "
                    + "is no input data.");
        }
    }

    /** set legend.
     */
    public void setLegend(String[] legends) {
        _legends = legends;
    }

    /** Rescale the plot so that all the data plotted is visible.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
	_plot.fillPlot();
        super.wrapup();
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    public TypedIOPort inputX;
    public TypedIOPort inputY;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Plot _plot;

    private String[] _legends;
    private Parameter _paramLegends;

    private double _yMin;
    private Parameter _paramYMin;
    private double _yMax;
    private Parameter _paramYMax;

    private double _xMin;
    private Parameter _paramXMin;
    private double _xMax;
    private Parameter _paramXMax;

    private boolean _rangeInitialized = false;

    private boolean _firstPoint;
}
