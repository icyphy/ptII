/* Demo for a live signal plotter.

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
import java.applet.Applet;

//////////////////////////////////////////////////////////////////////////
//// PlotDemo
/** 
 * Dynamically plot a test signal, illustrating how to use the <code>PlotLive</code> class.
 */
public class PlotLiveDemo extends PlotLive {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
   
    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotLiveDemo 1.0: Demo of PlotLive. By: Edward A. Lee, eal@eecs.berkeley.edu";
    }

    /**
     * Define static properties of the plot, such as the title and axis labels.
     * This also calls the base class <code>init()</code>, which performs various
     * initialization functions and reads commands from a file given by a URL,
     * if the <i>dataurl</i> applet parameter is given.  Since the base class
     * <code>init()</code> is called after the static plot parameters are
     * defined, then the commands from the file will override the static ones
     * given here.  This method also creates start and stop buttons to control
     * the plot.
     */
    public synchronized void init () {
        setTitle("Live Plot Demo");
        setYRange(-1,1);
        setXRange(-1,1);
        setNumSets(2);
        setMarksStyle("none");
        setPointsPersistence(100);
        
        // Give the user direct control over starting and stopping.
        makeButtons();
        
        super.init();
    }
    
	/**
     * Draw points.  This runs in a separate thread, and will run forever if
     * allowed to.
     */
    public synchronized void run() {
        boolean first = true;
        double count = 0.0;
        while (true) {
            addPoint(0, Math.sin(Math.PI*count/25), Math.cos(Math.PI * count/100), !first);
            addPoint(1, Math.sin(Math.PI*count/45), Math.cos(Math.PI * count/70), !first);
            first = false;
            count += 1.0;
        }
    }
}
