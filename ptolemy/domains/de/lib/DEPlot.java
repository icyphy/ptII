/* An actor that plots the input data.

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
import ptolemy.plot.*;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// DEPlot
/**
A plotter for discrete-event signals.

@author Lukito Muliadi, Edward A. Lee
@version $Id$
*/
public class DEPlot extends AtomicActor {

    /** Construct a plot actor with a new plot window. The default Y-range is
     *  [-1, 1]. The default X-range is the start time to the stop time.
     */
    public DEPlot(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new IOPort(this, "input", true, false);
        input.makeMultiport(true);

        // FIXME: Consolidate code with next constructor.
        PlotFrame plotFrame = new PlotFrame(getName());
        _plot = plotFrame.plot;
        _plot.setMarksStyle("dots");
        _plot.setImpulses(true);
        _plot.setConnected(false);

        // FIXME: This is not the right way to handle this...
        _yMin = (double)-1;
        _yMax = (double)1;
    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     */
    public DEPlot(CompositeActor container, String name, Plot plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new IOPort(this, "input", true, false);
        input.makeMultiport(true);

        _plot = plot;
        _plot.setMarksStyle("dots");
        _plot.setImpulses(true);
        _plot.setConnected(false);

        // FIXME: This is not the right way to handle this...
        _yMin = (double)-1;
        _yMax = (double)1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the plot window.
     */
    public void initialize()
            throws CloneNotSupportedException, IllegalActionException {

        _plot.clear(false);

        // FIXME: Should create and maintain a legend

        // Initialization of the frame X-range is deferred until the fire()
        // phase, because the director doesn't know the start time until
        // some stars enqueue an event.
    }

    /** Add new input data to the plot.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException{

	if (_firstFiring) {
            DECQDirector dir = ((DECQDirector)getDirector());
            if (dir == null) {
                throw new IllegalActionException(this, "No director available");
            }
            _plot.setXRange(dir.getStartTime(), dir.getStopTime());
            _plot.setYRange(getYMin(), getYMax());
            _firstFiring = false;
        }

        int numEmptyChannel = 0;

        int width = input.getWidth();
        for (int i = 0; i<width; i++) {
            // check channel i.
            if (input.hasToken(i)) {
                double curTime =((DECQDirector)getDirector()).getCurrentTime(); 
                // channel i is not empty, get all the tokens in it.
                while (input.hasToken(i)) {
                    DoubleToken curToken = null;
                    try {
                        curToken = (DoubleToken)input.get(i);
                    } catch (NoSuchItemException e) {}
                    double curValue = curToken.doubleValue();
                    _plot.addPoint(i, curTime, curValue, false);
                }
            } else {
                // Empty channel. Ignore
                // But keep track of the number of empty channel,
                // because this actor shouldn't be fired if all channels
                // are empty..
                numEmptyChannel++;
            }
            /*







            try {
                // the following statement might throw an exception.
                DoubleToken curToken = (DoubleToken)input.get(i);

                // if an exception is not thrown then continue below.
                double curValue = curToken.doubleValue();
                double curTime = ((DECQDirector)getDirector()).getCurrentTime();
                _plot.addPoint(i, curTime, curValue, false);
            } catch (NoSuchItemException e) {
                // Empty channel.  Ignore.
                numEmptyChannel++;
            }

            */





        }
        // If all channels are empty, then the scheduler is wrong.
        if (numEmptyChannel == width) {
            throw new InternalErrorException(
                "Discrete event scheduling error. DEPlot fired, but there "
                + "is no input data.");
        }
    }

    /** Rescale the plot so that all the data plotted is visible.
     */
    public void wrapup() {
	_plot.fillPlot();
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
}
