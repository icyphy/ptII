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

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.ListIterator;


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
     * The initial colormap used to render images is 256 grayscale.
     */
    public Render() {
        setGrid(false);
        
	 _setPadding(0.0);

        int[] rgb = new int[256];
        for (int i = 0; i <= 255; i++) {
            rgb[i] = i;
        }
        _colormap[0] = rgb;
        _colormap[1] = rgb;
        _colormap[2] = rgb;
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a vertical stripe.
     *  @param colors The colors of the pixels in the vertical stripe.
     */
    public synchronized void addStripe(int[] colors) {

        // Make a copy of the array given to addStripe().

        int[] newStripe = new int[colors.length];
        System.arraycopy(colors, 0, newStripe, 0, newStripe.length);

        // Add the stripe (the copied array) to the data structure.

        _imageData.add(newStripe);

//         // Get the graphics context.
// 
//         Graphics graphics = getGraphics();
// 
//         // Draw the stripe if the graphics context ... ISTHERE(?)...
//         if (graphics != null) {
//             _drawStripe(graphics, newStripe, x);
//         }
    }

    /** Get the current colormap.
     */
    public synchronized int[][] getColormap() {
        return _colormap;
    }
    
    /** Get the x increment.
     */
    public synchronized double getXIncrement() {
	return _xIncrement;
    }

    /** Get the x offset.
     */
    public synchronized double getXOffset() {
	return _xOffset;
    }

    /** Get the y increment.
     */
    public synchronized double getYIncrement() {
	return _yIncrement;
    }

    /** Get the y offset.
     */
    public synchronized double getYOffset() {
	return _yOffset;
    }

    /** Create a sample image.
     */
    public synchronized void samplePlot() {
        // This needs to be done in the event thread.
        Runnable sample = new Runnable() {
            public void run() {
                synchronized (Render.this) {

                    // Create a sample plot.
                    clear(true);
                    setGrid(false);

                    setTitle("Sample image");

                    // Create the stripes in data form (arrays).
                    int[] stripe1 = new int[100];
                    int colorValue = _HIGHCOLOR;
                    for (int i = 0; i < stripe1.length; i++) {
                        stripe1[i] = colorValue;
                        if (colorValue == _LOWCOLOR) {
                            colorValue = _HIGHCOLOR;
                        } else {
                            colorValue = _LOWCOLOR;
                        }
                    }

                    int[] stripe2 = new int[100];
                    System.arraycopy(stripe1, 0, stripe2, 1,
                                     stripe2.length - 1);
                    stripe2[0] = _LOWCOLOR;
                    
		    // Reset the data structure.
		    _imageData = new LinkedList();

                    // Add the stripes to the data structure.
                    for (int i = 1; i <= 50; i++) {
                        addStripe(stripe1);
                        addStripe(stripe2);
                    }

		    setXIncrement(0.05);
		    setXOffset(0.0);
		    setYIncrement(40.0);
		    setYOffset(0.0);

		    // These variables are set to make the fill mechanism work.
		    _xTop = _imageData.size() * getXIncrement();
		    _xBottom = getXOffset();
		    _yTop = ((int[])_imageData.getFirst()).length *
			getYIncrement();
		    _yBottom = getYOffset();

		    // Set the x and y ranges according to the data.  The
		    // x-range start value is getXOffset().  Similarly for the
		    // y-range start value.  To calculate the high end of the 
		    // x-range take the number of stripes in the data 
		    // structure and multiply it by the horizontal increment.
		    // For the high value of the y-range take the number of
		    // data elements per stripe and multiply it by the
		    // verticle increment.
                    setXRange(getXOffset(), 
			      _imageData.size() * getXIncrement());
                    setYRange(getYOffset(),
			      ((int[])_imageData.getFirst()).length *
			      getYIncrement());
                }
            }
        };
        _deferIfNecessary(sample);
        repaint();
    }

    /** Set the colormap.
     *  The user needs to give a 3-by-256 integer array as a colormap.
     *  @param colormap The colormap used to render images.
     */
    public synchronized void setColormap(int[][] colormap){
        _colormap = colormap;
    }

    /** Set the x increment.
     *  @param xIncrement The increment in units of the x-axis of each stripe.
     */
    public synchronized void setXIncrement(double xIncrement) {
	_xIncrement = xIncrement;
    }

    /** Set the x offset.
     *  @param xOffset The starting value of the x-axis.
     */
    public synchronized void setXOffset(double xOffset) {
	_xOffset = xOffset;
    }

    /** Set the y increment.
     *  @param yIncrement The increment in units of the y-axis of each patch
     *  within each stripe.
     */
    public synchronized void setYIncrement(double yIncrement) {
	_yIncrement = yIncrement;
    }

    /** Set the y offset.
     *  @param yOffset The starting value of the y-axis.
     */
    public synchronized void setYOffset(double yOffset) {
	_yOffset = yOffset;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Render the data.
     *  @param graphics The graphics context.
     *  @param clearfirst If true, clear the image before proceeding.
     */
    protected synchronized void _drawPlot(Graphics graphics,
            boolean clearfirst) {

        // We must call PlotBox._drawPlot() before calling _drawStripe
        // so that _xscale and _yscale are set.
        super._drawPlot(graphics, clearfirst);

	double x1 = (double)(_ulx + ((double)(_originalXlow - _xMin) * _xscale)
			     + 1.0);

        double width = _xIncrement * _xscale;

	double x2 = x1 + width;

        ListIterator imageDataIterator = _imageData.listIterator(0);

        while (imageDataIterator.hasNext()) {
            int[] currentStripe = (int[])imageDataIterator.next();
            _drawStripe(graphics, currentStripe, x1, (int)x2 - (int)x1);
	    x1 = x2;
	    x2 = x1 + width;
        }

	// Indicate that the plot is showing.
	_showing = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Clip the patch to the visible x-range.
     *  @param x The proposed starting x-value of the patch.
     *  @param width The proposed width of the patch.
     */
    private int[] _clipXWidth(int x, int width) {
	int[] retrn = new int[2];

	// if the patch extends across the entire width of the plot rectangle
	if (x < _ulx + 1 && x + width > _lrx - 1) {
	    retrn[0] = _ulx + 1;
	    retrn[1] = _lrx - _ulx - 1;
	} // if the patch is on the left border line
	else if (x < _ulx + 1 && x + width > _ulx + 1) {
	    retrn[0] = _ulx + 1;
	    retrn[1] = width - (_ulx - x);
	} // if the patch is within the left and right borders
	else if (x >= _ulx + 1 && x + width <= _lrx - 1) {
	    retrn[0] = x;
	    retrn[1] = width;
	} // if the patch is on the right border
	else if (x <= _lrx - 1 && x + width > _lrx - 1) {
	    retrn[0] = x;
	    retrn[1] = _lrx - x;
	} // if the patch is outside of either the left or right border
	else {
	    retrn[0] = _NOTVISIBLE;
	}
	return retrn;
    }

    /** Clip the patch to the visible y-range.
     *  @param y The proposed starting y-value of the patch.
     *  @param height The proposed height of the patch.
     */
    private int[] _clipYHeight(int y, int height) {
	int[] retrn = new int[2];

	// if the patch extends across the entire height of the plot rectange
	if (y < _uly + 1 && y + height > _lry - 1) {
	    retrn[0] = _uly + 1;
	    retrn[1] = _lry - _uly - 1;
	} // if the patch is on the top border line
	else if (y < _uly + 1 && y + height >= _uly + 1) {
	    retrn[0] = _uly + 1;
	    retrn[1] = height - (_uly - y);
	} // if the patch is within the top and bottom borders
	else if (y >= _uly + 1 && y + height <= _lry - 1) {
	    retrn[0] = y;
	    retrn[1] = height;
	} // if the patch is on the bottom border
	else if (y <= _lry - 1 && y + height > _lry - 1) {
	    retrn[0] = y;
	    retrn[1] = _lry - y;
	} // if the patch is outside of either the top or bottom border
	else {
	    retrn[0] = _NOTVISIBLE;
	}
	return retrn;
    }

    /** Draw a stripe.
     *  @param graphics The graphics context.
     *  @param stripe The stripe to be drawn.
     *  @param x The x coordinate in pixels of the left edge of the stripe
     *  to be drawn.
     *  @param width The width of the stripe to be drawn.
     */
     private void _drawStripe(Graphics graphics, int[] stripe,
                              double x, double width) {

         // The stripe needs to be at least one pixel wide.
         if (width < 1.0) {
             width = 1.0;
         }

         // Draw the stripe one patch (data element) at a time.
         double y1 = _lry - (int)((_originalYhigh - _yMin) * _yscale) + 1.0;

	 double height = _yIncrement * _yscale;

	 double y2 = y1 + height;

         for (int i = 0; i < stripe.length; i++) {
             _drawPatch(graphics, (int)x, (int)y1, (int)width,
			(int)y2 - (int)y1, stripe[i]);

             // Increment the y values so the next patch is printed in the
             // right place.
             y1 = y2;
	     y2 = y1 + height;
         }
     }


    /** Draw a rectangular piece of an image.
     *  @param graphics The graphics context.
     *  @param x The x coordinate of the left edge of the rectangle to be
     *  drawn.
     *  @param y The y coordinate of the top edge of the rectangle to be drawn.
     *  @param width The width of the rectangle to be drawn.
     *  @param height The height of the rectangle to be drawn.
     *  @param colorValue The index into the colormap of the color to be drawn.
     */

    private void _drawPatch(Graphics graphics, int x, int y,
                             int width, int height, int colorValue) {

	// The height of the patch must be at least one pixel.
	if (height < 1){
	    height = 1;
	}
	 
	// Convert the colorValue into it's r, g, and b.
	int r = _colormap[0][colorValue];
	int g = _colormap[1][colorValue];
	int b = _colormap[2][colorValue];
 
	// Set the color.
	graphics.setColor(new Color(r, g, b));

	// Clip the patch to the visible range.
	int[] xAndWidth = _clipXWidth(x, width);
	x = xAndWidth[0];
	width = xAndWidth[1];

	// If the patch is not visible return without having drawn anything.
	if (x == _NOTVISIBLE) {
	    return;
	}

	int[] yAndHeight = _clipYHeight(y, height);
	y = yAndHeight[0];
	height = yAndHeight[1];

	// If the patch is not visible return without having drawn anything.
	if (y == _NOTVISIBLE) {
	    return;
	}

	// Draw the patch.
	graphics.fillRect(x, y, width, height);
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    /** The colormap used in rendering the image.
     */
    private int[][] _colormap = new int[3][256];

    /** A test color value from 0 through 255.
     */
    private static final int _HIGHCOLOR = 225;

    /** Stores the image data to be rendered.
     */
    private LinkedList _imageData = new LinkedList();

    /** A test color value from 0 through 255.
     */
    private static final int _LOWCOLOR = 175;

    /** The flag indicating whether the patch to be drawn is visible.
     */
    private static final int _NOTVISIBLE = -999;

    /** Set by _drawPlot(), and reset by clear(). */
    private boolean _showing = false;

    /** The increment in units of the x-axis of each stripe along the x-axis.
     *  Each stripe will be _xIncrement wide.
     */
    private double _xIncrement = 0.0;

    /** The starting point of the x-axis.  The x-axis will start counting from
     *  _xOffset.
     */
    private double _xOffset = 0.0;

    /** The increment in units of the y-axis of each patch within the stripe.
     *  Each patch will be _yIncrement tall.
     */
    private double _yIncrement = 0.0;

    /** The starting point of the y-axis.  The y-axis will start counting from
     *  _yOffset.
     */
    private double _yOffset = 0.0;
}
