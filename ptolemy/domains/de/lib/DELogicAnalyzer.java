/* An actor that plots the digital signal data.

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
//// DELogicAnalyzer
/**
A plotter for digital signals.

@author Lukito Muliadi
@version $Id$
*/
public class DELogicAnalyzer extends DEActor {

    /** Construct a plot actor with a new plot window. The default Y-range is
     *  [-1, 1]. The default X-range is the start time to the stop time.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public DELogicAnalyzer(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.makeMultiport(true);
        
        // FIXME: Consolidate code with next constructor.
        LogicAnalyzerFrame logicAnalyzerFrame = new LogicAnalyzerFrame(getName());
        _logicAnalyzer = logicAnalyzerFrame.logicAnalyzer;
        
    }

    /** Construct a plot actor that uses the specified plot object.
     *  This can be used to create applets that plot the results of
     *  DE simulations.
     *
     *  @exception NameDuplicationException If the parent class throws it.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public DELogicAnalyzer(TypedCompositeActor container, 
            String name, LogicAnalyzer plot)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.makeMultiport(true);

        _logicAnalyzer = plot;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the plot window.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {

        _logicAnalyzer.clear(false);

        // FIXME: Should create and maintain a legend

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
            _logicAnalyzer.setXRange(getStartTime(), getStopTime());
            _logicAnalyzer.init();
            _logicAnalyzer.repaint();
            _rangeInitialized = true;
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
                    curToken = (DoubleToken)input.get(i);
                    double curValue = curToken.doubleValue();
                    
                    
                    // add the point
                    if (curValue == 1.0) {
                        _logicAnalyzer.addPoint(i, curTime, LogicAnalyzer.ONE);
                    } else if (curValue == 0.0) {
                        _logicAnalyzer.addPoint(i, curTime, LogicAnalyzer.ZERO);
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
                "Discrete event scheduling error. DELogicAnalyzer fired, but there "
                + "is no input data.");
        }
    }

    /** Rescale the plot so that all the data plotted is visible.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
	_logicAnalyzer.fillPlot();
        super.wrapup();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LogicAnalyzer _logicAnalyzer;

    private boolean[] _firstPoint;

    private boolean _rangeInitialized = false;

}
