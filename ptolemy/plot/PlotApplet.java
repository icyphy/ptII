/* Appletable Plotter

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
//// PlotApplet
/** 
 * Create an Applet that can plot data from a URL.
 * The URL can be specified using either the
 * <code>dataurl</code> or <code>pxgraphargs</code> applet parameter.  
 * <p> The <code>dataurl</code> parameter contains a single URL
 * that refers to the data to be plotted.
 * <p> The <code>pxgraphargs</code> parameter contains a list
 * of commmand line style arguments, ending in one or more URLs.
 * See the <code>Pxgraph</code> documentation for the format of
 * these arguments.
 *
 * @author Edward A. Lee, Christopher Hylands
 * @version $Id$
 * @see Pxgraph
 */
public class PlotApplet extends Applet implements Runnable {

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotApplet 1.1: A flexible data plotter.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu and\n " +
            "Christopher Hylands, cxh@eecs.berkeley.edu\n" +
            "($Id$)";
    }

    /**
     * Return information about parameters.
     */
    public String[][] getParameterInfo () {
        String pinfo[][] = {
            {"background", "hexcolor value", "background color"},
            {"foreground", "hexcolor value", "foreground color"},
            {"dataurl",   "url",     "the URL of the data to plot"},
            {"pxgraphargs",   "args",    
             "pxgraph style command line arguments"}
        };
        return pinfo;
    }

    /**
     * Initialize the applet.  Read the applet parameters.
     */
    public void init() {
        if (_debug > 8) System.out.println("PlotApplet: init");
        int width,height;
        setLayout(new BorderLayout());

        _myPlot = new Plot();
        add("Center",_myPlot);
        //show();

        // Process the documentBase applet parameter.
        // Need the catch here because applets used as components have
        // no parameters. 
        try {
            _myPlot.setDocumentBase(getDocumentBase());
        } catch (NullPointerException e) {
            System.err.println("PlotApplet: init: NullPointerException while"+
                    "handling getDocumentBase" + e);
        }

        // Process the width and height applet parameters
        try {
            width = Integer.valueOf(getParameter("width")).intValue();
        } catch (NullPointerException e) {
            width = 400;
        }
        try {
            height = Integer.valueOf(getParameter("height")).intValue();
        } catch (NullPointerException e) {
            height = 400;
        }
        if (_debug > 8)
            System.out.println("PlotApplet: init: about to resize"+width);
        _myPlot.resize(width,height);

        // Process the dataurl parameter.
        String dataurl = null;
        try {
            dataurl = getParameter("dataurl");
            _myPlot.setDataurl(dataurl);
        } catch (NullPointerException e) {}


        // Process the pxgraphargs parameter.
        String pxgraphargs = null;
        try {
            pxgraphargs = getParameter("pxgraphargs");
            _myPlot.parsePxgraphargs(pxgraphargs);
        } catch (NullPointerException e) {
        } catch (CmdLineArgException e) {
            System.err.println("Plot: failed to parse `"+pxgraphargs+
                    "': " +e);
        }

        super.init();
        _myPlot.init();

    }

    /**
     * Paint the screen with our plot.
     */
    public void paint(Graphics graphics) {
        if (_debug > 8) System.out.println("PlotApplet: paint");
        _myPlot.paint(graphics);
    }

    public void run () {
        if (_debug > 8) System.out.println("PlotApplet: run");
	repaint();
    }

    /**
     * Start the plot.
     */
    public void start () {
        if (_debug > 8) System.out.println("PlotApplet: start");
	_plotThread = new Thread(this);
        _plotThread.start();
        super.start();
    }

    /**
     * Stop the plot
     */
    public void stop () {
        if (_debug > 8) System.out.println("PlotApplet: stop");
        _plotThread.stop();
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    // If non-zero, print out debugging messages.
    protected int _debug = 0;

    // The Plot component we are running.
    protected Plot _myPlot;

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // Thread for this applet.
    private Thread _plotThread;
}
