/* A live signal plotter.

@Author: Edward A. Lee
@Version: $Id$

@Copyright (c) 1997 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
/* Confuses metrowerks java: package plot; */

import java.awt.*;
import java.util.*;
import java.applet.Applet;

//////////////////////////////////////////////////////////////////////////
//// PlotLive
/** 
 * Plot signals dynamically, where points can be added at any time and the
 * the display will be updated.  This should be normally used with some
 * finite persistence so that old points are erased as new points are added.
 * Unfortunately, the most efficient way to erase old points is to draw graphics
 * using the "exclusive or" mode, which introduces quite a number of artifacts.
 * The most obvious is that color is lost (a bug in the AWT?).  Also, when lines
 * are drawn between points, where they overlap the points the line becomes white.
 * Moreover, if two lines overlap, they disappear.
 *
 * This class is abstract, so it must be used by creating a derived class.
 * To use it, create a derived class with <code>init()</code> and
 * <code>run()</code> methods. The <code>init()</code> method can call methods in the
 * <code>Plot</code> or <code>PlotBox</code> classes
 * (both of which are base classes) to set the static properties of
 * the graph, such as the title, axis ranges, and axis labels. 
 * The <code>run()</code> method should call <code>addPoint()</code>
 * of the <code>Plot</code> class to dynamically add points to the plot.
 * This method runs in its own thread with a priority slightly lower than
 * that of the default thread, to avoid hogging resources.
 *
 * The <code>init()</code> method <i>must</i> call
 * <code>super.init()</code> somewhere in its body;  along with general initialization,
 * this reads a file given by a URL if the dataurl applet parameter is specified.
 */
public abstract class PlotLive extends Plot implements Runnable {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
   
    /**
     * Handle button presses to start or stop the separate thread that generates
     * points to plot.
     */
    public boolean action (Event evt, Object arg) {
        if (evt.target == _startButton) {
            startPlot();
            return true;
        } else if (evt.target == _stopButton) {
            stopPlot();
            return true;
        } else {
            return super.action (evt, arg);
        }
    }

    /**
     * Stop the thread when the applet goes away.
     */
    public void destroy() {
        if (_plotThread != null && _plotThread.isAlive()) {
            _plotThread.stop();
            _plotThread = null;
        }
    }

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotLive 1.0: A live data plotter. By: Edward A. Lee, eal@eecs.berkeley.edu";
    }

    /**
     * Create a start and stop buttons, by which the user can invoke
     * <code>startPlot()</code> and <code>stopPlot</code>.  Alternatively, a derived
     * might invoke these directly and dispense with the buttons.  This should be
     * called within the <code>init()</code> method in derived classes.
     */
    public synchronized void makeButtons () {
        // So that the buttons appear at the upper left...
        setLayout(new FlowLayout(FlowLayout.LEFT));
        _startButton = new Button("start");
        add(_startButton);
        _stopButton = new Button("stop");
        add(_stopButton);
        
        // allow a little extra room for the buttons.
        topPadding += 5;
    }
    
    /**
     * Start the plotting thread if it has not been started.
     * Resume it if it has been started but is not active.
     * Note that this is not named "start" so that starting and
     * stopping the thread is under the control of the user rather
     * than the applet viewer.
     */
    public void startPlot() {
        if (_plotThread != null) {
            _plotThread.resume();
        } else if (_plotThread == null) {
            // start the update thread.
            _plotThread = new Thread(this, "Plot Thread");
            // set priority just below default.
            _plotThread.setPriority(4);
            _plotThread.start();
        }
    }

    /**
     * Suspend the plotting thread.  This is not named "stop" so that
     * starting and stopping of the thread is under the control of the
     * user rather than the applet viewer.
     */
    public void stopPlot() {
        if (_plotThread != null && _plotThread.isAlive()) {
            _plotThread.suspend();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                       private variables                          ////
    
    private Thread _plotThread;
   
    private Button _startButton, _stopButton;
}
