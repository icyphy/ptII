/* A DE actor that calls ptplot to plot the result. Multi-input sink.

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
A DE Star that calls ptplot to plot the result. Single input only.
@author Lukito Muliadi
@version $Id$
*/
public class DEPlot extends AtomicActor{
    /** Construct a DEPlot star in a CT universe. Default Y-range is
     *  [-1, 1]. Default X-range is from the startTime to the stopTime.
     */	
    public DEPlot(CompositeActor container, String name) 
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create the input port and make it a multiport.
        _input = new IOPort(this, "input", true, false);
        _input.makeMultiport(true);
        // FIXME: design it to be single width first
        //_input.makeMultiport(true);
        _yMin = (double)-1;
        _yMax = (double)1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Initalize.
     */
    public void initialize() 
            throws CloneNotSupportedException, IllegalActionException {
        System.out.println("Initializing "+description(CLASSNAME|FULLNAME));
   
        _exe = (DECQDirector) getDirector();
        if (_exe == null) {
            throw new IllegalActionException(this, "No director available");
        } 
        _inputWidth = _input.getWidth();
        if (_inputWidth == 0) {
            throw new InvalidStateException("DEPlot.initialize(), bug in "+
                    "scheduler, input width is equal to zero, therefore "+
                    "shoudn't be scheduled.");
        }
        _firstPoint = new boolean[_inputWidth];
        for (int i = 0; i<_inputWidth; i++) {
            _firstPoint[i] = true;
        }
        // Initialization of the frame X-range is deferred until the fire()
        // phase, because the director doesn't know the startTime() until
        // some stars enqueue it.
    }

    /** fire: consume the input tokens. In the first fire() round, 
     *  print out the data.
     */
    public void fire() 
            throws CloneNotSupportedException, IllegalActionException{
        
        System.out.println("Firing " + description(CLASSNAME|FULLNAME));
        
        if (_frame == null) {
            _frame = new DEPlotFrame(); 
            _frame.resize(800, 400);
            _frame.setXRange(_exe.startTime(), _exe.stopTime());
            _frame.setYRange(getYMin(), getYMax());
            // set the number of sets in the plot according to input port width
            _frame.setNumSets(_inputWidth);
            // initialize the plot.
            _frame.init();
        }
        
        int numEmptyChannel = 0;

        for (int i = 0; i<_inputWidth; i++) {
        
            try {
                // the following statement might throw an exception.
                DEToken curToken = (DEToken)_input.get(i);
                
                // if an exception is not thrown then continue below.
                double curValue = curToken.doubleValue();
                double curTime = ((DETag)curToken.getTag()).timeStamp();
                
                if (_firstPoint[i]) {
                    _frame.addPoint(i, curTime, curValue, false);
                    _firstPoint[i] = false;
                } else {
                    _frame.addPoint(i, curTime, curValue, true);
                }
                
            } catch (NoSuchItemException e) {
                // Some channels might have no token in them, and it's okay.
                numEmptyChannel++;
            }
            
        } 
        // If all channels are empty, then the scheduler is wrong.
        if (numEmptyChannel == _inputWidth) {
            throw new InvalidStateException("DEPlot.fire(), no tokens in the "+
                                        "input port, bad scheduler ?");
        }
        
    }
    
    /*    
          public void setParam(String name, String valueString) {
          double value = (new Double(valueString)).doubleValue();
          if(name.equals("YMin")) {
          _yMin = value;
          }else if(name.equals("YMax")) {
          _yMax = value;
          }
          setParamChanged(true);
          }
    */

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
    ////                         private inner class               ////

    public class DEPlotFrame extends Frame {

        // constructors
                
        public DEPlotFrame() {
            super("DE Plot");
            _plotter = new Plot();
            _plotter.setPointsPersistence(0);
            pack();
            
            add("Center", _plotter);
            
        }

        // public methods

        public void init() {   
            show();
            _plotter.setMarksStyle("dots");
            _plotter.init();
        }

        public void addPoint(int numSet, double xValue, double yValue,
                boolean isConnected) {
            _plotter.addPoint(numSet, xValue, yValue, isConnected);
        }

        public void setXRange(double xMin, double xMax) {
            _plotter.setXRange(xMin, xMax);
        }

        public void setYRange(double yMin, double yMax) {
            _plotter.setYRange(yMin, yMax);
        }

        public void setNumSets(int numSet) {
            _plotter.setNumSets(numSet) ;
        }

        public boolean handleEvent(java.awt.Event event) {
            Object pEvtSource = event.target;
            if( pEvtSource == this && 
                    event.id == java.awt.Event.WINDOW_DESTROY ) {
                hide();
                dispose();
                return false;
            } else {
                return super.handleEvent( event );
            }
        }
        
        // private variables
        private Plot _plotter;

    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    private IOPort _input;

    private DECQDirector _exe;
    private DEPlotFrame _frame;
    private double _yMin;
    private double _yMax;

    private boolean[] _firstPoint;

    private int _inputWidth;

}
