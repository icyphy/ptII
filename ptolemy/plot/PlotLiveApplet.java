/* A live signal plotter applet

@Author: Edward A. Lee and Christopher Hylands

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
package ptplot;

import java.applet.Applet;
import java.awt.*;
import java.net.*;              // Need URL

//////////////////////////////////////////////////////////////////////////
//// PlotLiveDemoApplet
/** 
 */
public class PlotLiveDemoApplet extends Applet implements Runnable {

    public void init() {
        if (_debug > 8) System.out.println("PlotLiveDemoApplet: init");
        int width,height;
        setLayout(new BorderLayout());

        _myPlotLiveDemo = new PlotLiveDemo();
        add("Center",_myPlotLiveDemo);
        //        show();

        _myPlotLiveDemo.resize(300,300);
        _myPlotLiveDemo.init();
        super.init();
    }

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotLiveDemoApplet 1.1: Demo of PlotLive.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
            "    Christopher Hylands, @eecs.berkeley.edu\n" +
            "($Id$)";
    }

    /** 
     */
    public void paint(Graphics graphics) {
        if (_debug > 8) System.out.println("PlotLiveDemoApplet: paint");
        _myPlotLiveDemo.paint(graphics);
    }

    public void resize(int width, int height) {
        if (_debug > 8)
            System.out.println("PlotLiveDemoApplet: resize"+width+" "+height);
        super.resize(width,height);
    }
    public void run () {
        if (_debug > 8) System.out.println("PlotLiveDemoApplet: run");
// 	while (true) {
// 	    try {
// 		Thread.currentThread().sleep(speed);
// 	    } catch (InterruptedException e) {
// 	    }
        if (_debug > 10) System.out.println("PlotLiveDemoApplet: run calling repaint");
	repaint();
    }

    public void start () {
        if (_debug > 8) System.out.println("PlotLiveDemoApplet: start");
	_plotThread = new Thread(this);
        _plotThread.start();
        _myPlotLiveDemo.start();
        super.start();
    }

    public void stop () {
        if (_debug > 8) System.out.println("PlotLiveDemoApplet: stop");
        _plotThread.stop();
        super.stop();
    }

//     public void update (Graphics graphics) {
//         if (_debug > 8) System.out.println("PlotLiveDemoApplet: update");
//         paint(graphics);
//         super.update(graphics);
//     }

    protected int _debug = 0;
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private PlotLiveDemo _myPlotLiveDemo;
    private Thread _plotThread;

}

