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
import java.util.Enumeration;
import collections.HashedMap;


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
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public DEHistogram(TypedCompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {

        this(container, name, 0.1);
    }

    /** Construct a plot actor with a new plot window. The default Y-range is
     *  [-1, 1]. The default X-range is the start time to the stop time.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public DEHistogram(TypedCompositeActor container,
            String name, double binWidth)
            throws NameDuplicationException, IllegalActionException  {

        this(container, name, binWidth, null);
    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public DEHistogram(TypedCompositeActor container,
            String name, double binWidth, Plot plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        _plot = plot;

        // FIXME: This is not the right way to handle this...
        _yMin = (double)-1;
        _yMax = (double)1;

        // Set the parameters
        _binWidth = new Parameter(this, "bin width", new DoubleToken(binWidth));
        // FIXME: Hardwire binZeroOffset.
        _binZeroOffset = binWidth / 2.0;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the plot window by clearing it, and also clear the map
     *  collection.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {

        if (_plot == null) {
            _plot = new Plot();
            new PlotFrame(getName(), _plot);
        }

        double binWidth = ((DoubleToken)_binWidth.getToken()).doubleValue();

        _plot.setBars((1-_percentGap)*binWidth, 0.25*_percentGap*binWidth);

        _plot.clear(false);

        // FIXME: Should create and maintain a legend

        // Initialization of the frame X-range is deferred until the fire()
        // phase, because the director doesn't know the start time until
        // some stars enqueue an event.

        _map.clear();
        _maxBin = Integer.MIN_VALUE;
        _minBin = Integer.MAX_VALUE;


    }

    /** Add new input data to the plot.
     *  @exception IllegalActionException If the input port is disconnected.
     */
    public void fire() throws IllegalActionException{

        // Gather datas from input port.
        // FIXME: make it one channel first.
        while (input.hasToken(0)) {

            double dataIn = ((DoubleToken)input.get(0)).doubleValue();

            // perform the translation from data value to bin number.
            // Also, keep track of the max and min bin we've seen so far.
            int bin = _valueToBin(dataIn);
            if (bin > _maxBin) _maxBin = bin;
            if (bin < _minBin) _minBin = bin;


            Integer binObject = new Integer(bin);

            if (_map.includesKey(binObject)) {
                // increase the count
                int oldVal = ((Integer)_map.at(binObject)).intValue();
                _map.putAt(binObject, new Integer(oldVal+1));
            } else {
                // start a new entry.
                _map.putAt(binObject, new Integer(1));
            }


        }
    }

    /** Rescale the plot so that all the data plotted is visible.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {

        double binWidth = ((DoubleToken)_binWidth.getToken()).doubleValue();

        // enumerate through all the keys (bind index) and create a bar
        // for each.
        Enumeration keys = _map.keys();
        while (keys.hasMoreElements()) {
            Integer bin = (Integer)keys.nextElement();
            Integer height = (Integer)_map.at(bin);

            // calculate the center x-coordinate of the bin.
            double binCenter = bin.intValue() * binWidth + _binZeroOffset;

            _plot.addPoint(0, binCenter, height.intValue(), false);
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

    public TypedIOPort input;


    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Given a double value, figure out which bin it should end up into.
    // The current implementation does this by performing an integer divide
    // with respect to the bin width. FIXME: Tweak this method, if we want
    // to limit the max number of bins.
    // This bin number should be a function of only the data value, the
    // bin width and the binZeroOffset.
    private int _valueToBin(double value) {

        double binWidth = ((DoubleToken)_binWidth.getToken()).doubleValue();
        double offset = (value - _binZeroOffset) / binWidth;



        // Now round offset to the nearest integer.
        // If offset is equal to xxx.5, then round up.
        int retval =  (int)(offset+0.5);
        return retval;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Plot object where
    private Plot _plot;

    private double _yMin;
    private double _yMax;

    private Parameter _binWidth;
    // The x-coordinate of the center of the first bin.
    private double _binZeroOffset;

    // A map to store the (bin, height) paired element.
    private HashedMap _map = new HashedMap();

    // Keep track of this, so we know from and until where we need to
    // draw the bins.
    private int _minBin;
    private int _maxBin;

    // The ratio of the gap between bins and the width of the bins.
    // FIXME: currently it's set here.. and can't be changed.
    private double _percentGap = 0.005;

}







