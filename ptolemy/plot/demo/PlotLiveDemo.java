/* Demo for a live signal plotter.

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
package plot;

import java.awt.*;
import java.applet.Applet;

//////////////////////////////////////////////////////////////////////////
//// PlotLiveDemo
/**
 * Dynamically plot a test signal, illustrating how to use the
 * <code>PlotLive</code> class.
 *
 * @author: Edward A. Lee
 * @version: $Id$
 */
public class PlotLiveDemo extends PlotLive {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
   
   /** Add points to the plot.  This is called by the base class
     * <code>run()</code> method while live plotting is enabled.
     */
    public void addPoints() {
        // Plot 6 points at a time for faster response.
        for (int i = 0; i < 6; i++) {
            addPoint(0, Math.sin(Math.PI*_count/25),
		     Math.cos(Math.PI * _count/100), false);
            addPoint(0, Math.sin(Math.PI*_count/45),
		     Math.cos(Math.PI * _count/70), true);
            addPoint(1, Math.sin(Math.PI*_count/45),
		     Math.cos(Math.PI * _count/70), !__first);
            _first = false;
            _count += 1.0;
        }
    }

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotLiveDemo 1.0: Demo of PlotLive.\n" +
	    "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
	    "($Id$)";
    }

    /** 
     * Define static properties of the plot, such as the title and
     * axis labels.  This also calls the base class
     * <code>init()</code>, which performs various initialization
     * functions and reads commands from a file given by a URL, if the
     * <i>dataurl</i> applet parameter is given.  Since the base class
     * <code>init()</code> is called after the static plot parameters
     * are defined, then the commands from the file will override the
     * static ones given here.  This method also creates start and
     * stop buttons to control the plot.
     */
    public void init () {
        setTitle("Live Plot Demo");
        setYRange(-1,1);
        setXRange(-1,1);
        setNumSets(2);
        setMarksStyle("none");
        setPointsPersistence(60);
        
        // Give the user direct control over starting and stopping.
        makeButtons();
        
        super.init();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                       private variables                          ////
    
    private boolean _first = true;
    private double _count = 0.0;
}
