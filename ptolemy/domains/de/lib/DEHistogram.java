/* An actor that creates a histogram based on the input datas.

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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.plot.*;
import java.awt.*;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// DEHistogram
/**
Describe me!

@author Lukito Muliadi
@version $Id$
*/
public class DEHistogram extends DEActor {

    /** Construct a plot actor with a new plot window. The default Y-range is
     *  [-1, 1]. The default X-range is the start time to the stop time.
     */
    public DEHistogram(CompositeActor container, String name, double binWidth)
            throws NameDuplicationException, IllegalActionException  {

        super(container, name);

        // create the input port and make it a multiport.
        input = new IOPort(this, "input", true, false);
        input.makeMultiport(true);

        // FIXME: Consolidate code with next constructor.
        PlotFrame plotFrame = new PlotFrame(getName());
        _plot = plotFrame.plot;
        _plot.setBars(0.98*binWidth , 0.02*binWidth);

        // FIXME: This is not the right way to handle this...
        _yMin = (double)-1;
        _yMax = (double)1;

        // Set the parameters
        _binWidth = new Parameter(this,"bin width",new DoubleToken(binWidth));
        
    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     */
    public DEHistogram(CompositeActor container, String name, double binWidth, Plot plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new IOPort(this, "input", true, false);
        input.makeMultiport(true);

        _plot = plot;
        _plot.setBars(0.95*binWidth, 0.05*binWidth);

        // FIXME: This is not the right way to handle this...
        _yMin = (double)-1;
        _yMax = (double)1;

        // Set the parameters
        _binWidth = new Parameter(this, "bin width", new DoubleToken(binWidth));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the plot window.
     */
    public void initialize() throws IllegalActionException {


        _plot.clear(false);

        // FIXME: Should create and maintain a legend

        // Initialization of the frame X-range is deferred until the fire()
        // phase, because the director doesn't know the start time until
        // some stars enqueue an event.

        _dataCount = new Vector();
    }

    /** Add new input data to the plot.
     */
    public void fire() throws IllegalActionException{

        // Gather datas from input port.
        // FIXME: make it one channel first.
        while (input.hasToken(0)) {
            double dataIn = ((DoubleToken)input.get(0)).doubleValue();
            
            double width = ((DoubleToken)_binWidth.getToken()).doubleValue();

            int binNum = (int) (dataIn/width);

            if (_dataCount.size() < binNum + 1) {
                _dataCount.setSize(binNum+1);
            }
            
            Object d = _dataCount.elementAt(binNum);
            
            if (d == null) {
                _dataCount.setElementAt(new Integer(1), binNum);
            } else {
                Integer dd = (Integer) d;
                _dataCount.setElementAt(new Integer(dd.intValue() + 1), binNum);               
            }
            
        }
    }

    /** Rescale the plot so that all the data plotted is visible.
     */
    public void wrapup() throws IllegalActionException {

        double binWidth = ((DoubleToken)_binWidth.getToken()).doubleValue();

        for (int i = 0; i < _dataCount.capacity(); i++) {
            
            Object d = _dataCount.elementAt(i);
            if (d != null) {
                Integer y = (Integer)d;
                _plot.addPoint(0, i*binWidth+.5*binWidth, y.doubleValue(), false);
            }
        }

	_plot.fillPlot();
        super.wrapup();
    }

    // FIXME: This is not the right way to handle this.
    public void setYRange(double ymin, double ymax) {
        _yMin = ymin;
        _yMax = ymax;
    }

    public double getYMin() {
        return _yMin;
    }

    public double getYMax() {
        return _yMax;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    public IOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Plot _plot;

    private double _yMin;
    private double _yMax;

    private boolean[] _firstPoint;

    private boolean _firstFiring = true;
    
    private Parameter _binWidth;

    // access with: setElementAt(Object, int), elementAt(int), and setSize()
    private Vector _dataCount;
}







