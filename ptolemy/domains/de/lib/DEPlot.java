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
public class DEPlot extends DEActor {

    private static final boolean DEBUG = false;

    /** Construct a plot actor with a new plot window. The default Y-range is
     *  [-1, 1]. The default X-range is the start time to the stop time.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public DEPlot(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {

        this(container, name, (new PlotFrame(name)).plot);

    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public DEPlot(TypedCompositeActor container, String name, Plot plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setDeclaredType(Token.class);
        input.setMultiport(true);

        _plot = plot;
        _plot.setButtons(true);

        // FIXME: This is not the right way to handle this...
        _yMin = (double)-1;
        _yMax = (double)1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the plot window.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {

        _plot.clear(true);
        _plot.setButtons(true);
        _plot.setMarksStyle("dots");
        _plot.setImpulses(true);
        _plot.setConnected(false);
        _plot.setTitle(getName());

	for (int i = 0; i < input.getWidth(); i++) {
            if (_legends != null && i < _legends.length && _legends[i].length() != 0) {
                _plot.addLegend(i, _legends[i]);
            } else {
                _plot.addLegend(i, "Data " + i);

            }
        }

        // Initialization of the frame X-range is deferred until the fire()
        // phase, because the director doesn't know the start time until
        // some stars enqueue an event.
        _rangeInitialized = false;
        _yMin = -1;
        _yMax = 1;
    }

    /** Add new input data to the plot.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{

        if (!_rangeInitialized) {
            _plot.setXRange(getStartTime(), getStopTime());
            _plot.setYRange(getYMin(), getYMax());
            _plot.init();
            _plot.repaint();
            _rangeInitialized = true;
        }

        double curTime = ((DECQDirector)getDirector()).getCurrentTime();

        int numEmptyChannel = 0;

        int width = input.getWidth();
        for (int i = 0; i<width; i++) {
            // check channel i.
            if (input.hasToken(i)) {

                // channel i is not empty, get all the tokens in it.
                while (input.hasToken(i)) {
                    Token curToken = input.get(i);
                    
                    if (curToken instanceof DoubleToken) {
                        _processDoubleToken(i, curTime, (DoubleToken)curToken);
                    } else {
                        _processPureToken(i, curTime, curToken);
                    }
                }
            } else {
                // Empty channel. Ignore
                // But keep track of the number of empty channel,
                // because this actor shouldn't be fired if all channels
                // are empty..
                numEmptyChannel++;
            }
        }
        // If all channels are empty, then the scheduler is wrong.
        if (numEmptyChannel == width) {
            throw new InternalErrorException(
                "Discrete event scheduling error. DEPlot fired, but there "
                + "is no input data.");
        }
    }

    /** Rescale the plot so that all the data plotted is visible.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
	_plot.fillPlot();
        super.wrapup();
    }

    /** Set the legends.
     */
    public void setLegend(String[] legends) {
        _legends = legends;
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

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Process input tokens that are instances of DoubleToken.
     *  
     * @param channel The channel number
     * @param curTime The current time of the simulation
     * @param token The input token that is an instance of DoubleToken
     */	
    protected void _processDoubleToken(int channel, 
            double curTime, 
            DoubleToken token) {
        
        double curValue = token.doubleValue();

        // update the y range
        boolean yRangeChanged = false;
        if (curValue < _yMin) {
            yRangeChanged = true;
            _yMin = curValue;
        }
        if (curValue > _yMax) {
            yRangeChanged = true;
            _yMax = curValue;
        }
        if (yRangeChanged) {
            _plot.setYRange(_yMin, _yMax);
            _plot.repaint();
        }

        // add the point
        if (DEBUG) {
            System.out.print(this.getFullName() + ":");
            System.out.println("Dataset = " + channel +
                    ", CurrentTime = " + curTime +
                    ", CurrentValue = " + curValue + ".");
        }
        _plot.addPoint(channel, curTime, curValue, false);
        
    }

    /** Process pure token, i.e. typeless token.
     *  
     * @param channel The channel number
     * @param curTime The current time of the simulation
     * @param token The input token
     */	
    protected void _processPureToken(int channel, 
            double curTime, 
            Token token) {
        
        double curValue = channel;

        // update the y range
        boolean yRangeChanged = false;
        if (curValue < _yMin) {
            yRangeChanged = true;
            _yMin = curValue;
        }
        if (curValue > _yMax) {
            yRangeChanged = true;
            _yMax = curValue;
        }
        if (yRangeChanged) {
            _plot.setYRange(_yMin, _yMax);
            _plot.repaint();
        }

        // add the point
        if (DEBUG) {
            System.out.print(this.getFullName() + ":");
            System.out.println("Dataset = " + channel +
                    ", CurrentTime = " + curTime +
                    ", CurrentValue = " + curValue + ".");
        }
        _plot.addPoint(channel, curTime, curValue, false);
        
    }   

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String[] _legends;


    private Plot _plot;

    private double _yMin;
    private double _yMax;

    private boolean[] _firstPoint;

    private boolean _rangeInitialized = false;

}
