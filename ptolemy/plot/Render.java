/* FIXME

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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/
package ptolemy.plot;

import java.awt.Graphics;

//////////////////////////////////////////////////////////////////////////
//// Render
/**
 * FIXME
 *
 * @author Neil Turner
 * @version $Id$
 */
public class Render extends PlotBox {

    /** Construct an instance.
     */
    public Render() {
        setGrid(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a vertical stripe.
     *  @param x The x value for the vertical stripe.
     *  @param colors The colors of the pixels in the vertical stripe.
     */
    public synchronized void addStripe(double x, int[] colors) {
        // FIXME
    }

    /** Create a sample image.
     */
    public synchronized void samplePlot() {
        // Create a sample plot.
        clear(true);
        setGrid(false);

        setTitle("Sample image");
        setXRange(-1.0, 1.0);
        setYRange(-1.0, 1.0);

        // FIXME fill in.

        repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Render the data.
     *  @param graphics The graphics context.
     *  @param clearfirst If true, clear the image before proceeding.
     */
    protected synchronized void _drawPlot(Graphics graphics,
            boolean clearfirst) {

        // We must call PlotBox._drawPlot() before calling _drawPlotPoint
        // so that _xscale and _yscale are set.
        super._drawPlot(graphics, clearfirst);

        // Draw an oval centered at zero, with width 1 and height 1/2.
        int ulyOval = _lry - (int)((0.5 - _yMin) * _yscale);
        int ulxOval = _ulx + (int) ((-1 - _xMin) * _xscale);
        int ovalWidth = (int)(2 * _xscale);
        int ovalHeight = (int)(1 * _yscale);

        graphics.setColor(java.awt.Color.magenta);
        graphics.drawOval(ulxOval,ulyOval,ovalWidth, ovalHeight);

        // Indicate that the plot is showing.
        _showing = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial Set by _drawPlot(), and reset by clear(). */
    private boolean _showing = false;
}
