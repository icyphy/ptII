/* Demo for a live signal plotter.

@Copyright (c) 1997-2000 The Regents of the University of California.
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
@ProposedRating red (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.plot.demo;

import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// PlotLiveDemo
/**
 * Dynamically plot a test signal, illustrating how to use the
 * PlotLive class.
 *
 * @author Edward A. Lee
 * @version $Id$
 */
public class PlotLiveDemo extends PlotLive {

    /** Construct a plot for live, animated signal display.
     *  Configure the title, axes, points style, and persistence.
     */
    public PlotLiveDemo() {
        setYRange(-1, 1);
        setXRange(-1, 1);
        setPointsPersistence(60);
        setMarksStyle("dots");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add points to the plot.  This is called by the base class
     *  run() method when the plot is live.
     */
    public synchronized void addPoints() {
        // You could plot multiple points at a time here
        // for faster response, but in our case, we really need
        // to slow down the response for visual aesthetics.
        addPoint(0, Math.sin(Math.PI*_count/25),
                Math.cos(Math.PI * _count/100), false);
        addPoint(0, Math.sin(Math.PI*_count/45),
                Math.cos(Math.PI * _count/70), true);
        addPoint(1, Math.sin(Math.PI*_count/45),
                Math.cos(Math.PI * _count/70), false);
        addPoint(2, Math.sin(Math.PI*_count/20),
                Math.cos(Math.PI * _count/100), false);
        addPoint(3, Math.sin(Math.PI*_count/50),
                Math.cos(Math.PI * _count/70), false);
        _first = false;
        _count += 1.0;
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial True if this is the first time through. */
    private boolean _first = true;

    /** @serial Value being plotted */
    private double _count = 0.0;
}
