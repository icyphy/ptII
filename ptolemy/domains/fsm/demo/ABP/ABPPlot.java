/* An actor that plots the input data.

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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.demo.ABP;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.plot.*;
import java.awt.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DEPlot
/**
A plotter for discrete-event signals.

@author Lukito Muliadi, Edward A. Lee, Xiaojun Liu
@version $Id$
*/
public class ABPPlot extends DEActor {

    /** Construct a plot actor with a new plot window. The default Y-range is
     *  [-1, 1]. The default X-range is the start time to the stop time.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public ABPPlot(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {

        this(container, name, null);

    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public ABPPlot(TypedCompositeActor container, String name, Plot plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.GENERAL);
        input.setMultiport(true);

        _plot = plot;

        // FIXME: This is not the right way to handle this...
        String legends = new String("");
        _yMin = (double)-1.0;
        _yMax = (double)1.0;
        _paramLegends = new Parameter(this, "Legends",
                new StringToken(legends));
        _paramYMin = new Parameter(this, "Y_Min", new DoubleToken(_yMin));
        _paramYMax = new Parameter(this, "Y_Max", new DoubleToken(_yMax));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the plot window.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (_plot == null) {
            _plot = new Plot();
            new PlotFrame(getName(), _plot);
        }

        _plot.clear(true);
        _plot.setButtons(true);
        _plot.setMarksStyle("dots");
        _plot.setImpulses(true);
        _plot.setConnected(false);
        _plot.setTitle(getName());
        // parameters
        _yMin = ((DoubleToken)_paramYMin.getToken()).doubleValue();
        _yMax = ((DoubleToken)_paramYMax.getToken()).doubleValue();
        String legs = ((StringToken)_paramLegends.getToken()).toString();
        if(!legs.equals("")) {
            StringTokenizer stokens = new StringTokenizer(legs);
            int index = 0;
            _legends = new String[stokens.countTokens()];
            while(stokens.hasMoreTokens()) {
                _legends[index++] = stokens.nextToken();
            }
        }

	for (int i = 0; i < input.getWidth(); i++) {
            if (_legends != null &&
                    i < _legends.length &&
                    legends[i].length() != 0) {
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
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException{
        DEDirector dir = (DEDirector) getDirector();
        if (!_rangeInitialized) {
            _plot.setXRange(dir.getStartTime(), dir.getStopTime());
            _plot.setYRange(getYMin(), getYMax());
            _plot.repaint();
            _rangeInitialized = true;
        }

        double curTime = ((DEDirector)getDirector()).getCurrentTime();

        int numEmptyChannel = 0;

        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            // check channel i.
            if (input.hasToken(i)) {

                // channel i is not empty, get all the tokens in it.
                while (input.hasToken(i)) {
                    Token curToken = input.get(i);

                    if (curToken instanceof DoubleToken) {
                        _processDoubleToken(i, curTime, (DoubleToken)curToken);
                    } else if (curToken instanceof IntToken) {
                        _processIntToken(i, curTime, (IntToken)curToken);
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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

    protected void _processIntToken(int channel,
            double curTime,
            IntToken token) {

        int curValue = token.intValue();

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

    /** @serial The input port. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static final boolean DEBUG = false;

    /** @serial array of legends. */
    private String[] _legends;

    /** @serial Legend*/
    private Parameter _paramLegends;

    /** @serial Minimum Y value. */
    private double _yMin;

    /** @serial Minimum Y Parameter value. */
    private Parameter _paramYMin;

    /** @serial Maximum Y value. */
    private double _yMax;

    /** @serial Maximum Y Parameter value. */
    private Parameter _paramYMax;

    //private double _xMin;
    //private Parameter _paramXMin;
    //private double _xMax;
    //private Parameter _paramXMax;

    /** @serial The plot*/
    private Plot _plot;

    /** @serial True if this is the first point for this dataset. */
    private boolean[] _firstPoint;

    /** @serial True if the range has been initialized. */
    private boolean _rangeInitialized = false;

}
