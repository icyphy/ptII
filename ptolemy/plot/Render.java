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
     *  @param x The x value for the vertical stripe.
     *  @param colors The colors of the pixels in the vertical stripe.
     */
    public synchronized void addStripe(double x, int[] colors) {

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

                    // I thought that this would remove the padding from the
                    // plot rectangle, but it didn't.
//                     _topPadding = 0;
//                     _bottomPadding = 0;
//                     _rightPadding = 0;
//                    _leftPadding = 0;

                    // Create the stripes in data form (arrays).
                    int[] stripe1 = new int[10];
                    int colorValue = _HIGHCOLOR;
                    for (int i = 0; i < stripe1.length; i++) {
                        stripe1[i] = colorValue;
                        if (colorValue == _LOWCOLOR) {
                            colorValue = _HIGHCOLOR;
                        } else {
                            colorValue = _LOWCOLOR;
                        }
                    }
                    
                    int[] stripe2 = new int[10];
                    System.arraycopy(stripe1, 0, stripe2, 1,
                                     stripe2.length - 1);
                    stripe2[0] = _LOWCOLOR;
                    

                    // Add the stripes to the data structure.
                    for (int i = 1; i <= 5; i++) {
                        addStripe((double)i - .33, stripe1);
                        addStripe((double)i + .33, stripe2);
                    }

		    setXIncrement(0.5);
		    setXOffset(0.0);
		    setYIncrement(400.0);
		    setYOffset(0.0);

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
    * The user needs to give a 3-by-256 integer array as a colormap.
    */
    public synchronized void setColormap(int[][] colormap){
        _colormap = colormap;
    }

    /** Set the x increment.
     */
    public synchronized void setXIncrement(double xIncrement) {
	_xIncrement = xIncrement;
    }

    /** Set the x offset.
     */
    public synchronized void setXOffset(double xOffset) {
	_xOffset = xOffset;
    }

    /** Set the y increment.
     */
    public synchronized void setYIncrement(double yIncrement) {
	_yIncrement = yIncrement;
    }

    /** Set the y offset.
     */
    public synchronized void setYOffset(double yOffset) {
	_yOffset = yOffset;
    }

    // The thought here is to override setYRange() then just call the super
    // method with slightly smaller ranges that take into account the extra
    // padding that will be added.  The complications I foresee are the
    // getYRange() method will have to be overridden as well, and the values
    // might be a little off by having to multiply a smaller number by a
    // padding factor to get the slightly larger desired size plot rectangle.
    // I left this unfinished because of the complications.
    // Instead I uncommented some code in PlotBox that disallows padding if
    // the ranges have been specified by the setYRange().
    //    public synchronized void setYRange(double min, double max) {
    //	      super.setYRange( ) 
    //    }


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

        double x = (double)_ulx + (double)((_originalXlow - _xMin) * _xscale) +
	    1.0;
        //(_ulx + 1);

        double width = (double)((_originalXhigh - _originalXlow) * _xscale) / 
	    (double)_imageData.size();
        // divided by the
        // number of stripes (i.e _imageData.size())

        ListIterator imageDataIterator = _imageData.listIterator(0);

        while (imageDataIterator.hasNext()) {
            int[] currentStripe = (int[])imageDataIterator.next();
            _drawStripe(graphics, currentStripe, x, width);
            // System.out.println("x = " + x);
            x += width;
        }
         
        // Draw an oval centered at zero, with width 1 and height 1/2.
//         int ulyOval = _lry - (int)((0.5 - _yMin) * _yscale);
//         int ulxOval = _ulx + (int) ((-1 - _xMin) * _xscale);
//         int ovalWidth = (int)(2 * _xscale);
//         int ovalHeight = (int)(1 * _yscale);
// 
//         graphics.setColor(java.awt.Color.magenta);
//         graphics.drawOval(ulxOval,ulyOval,ovalWidth, ovalHeight);


  
            // Indicate that the plot is showing.
            _showing = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Draw the specified stripe.
     */

     private void _drawStripe(Graphics graphics, int[] stripe,
                              double x, double width) {

         // The stripe needs to be at least one pixel wide.
         if (width < 1.0) {
             width = 1.0;
         }

         // Draw the stripe one patch at a time.

         int length = stripe.length;

 
         // Draw the stripe one patch (data element) at a time.
         double y = _lry - (int)((_originalYhigh - _yMin) * _yscale) + 1;
	 //(_uly + 1);

         double height = (double)((_originalYhigh - _originalYlow) * _yscale) /
 	     (double)length;

         for (int i = 0; i < length; i++) {
             _drawPatch(graphics, (int)x, (int)y, (int)width, (int)height,
                        stripe[i]);

             // Increment the y value so the next patch is printed in the
             // right place.
             y += height;
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

        // Convert the colorValue into it's r, g, and b.
        int r = _colormap[0][colorValue];
        int g = _colormap[1][colorValue];
        int b = _colormap[2][colorValue];

        // Set the color.
        graphics.setColor(new Color(r, g, b));

        // Draw the patch.
        graphics.fillRect(x, y, width, height);

    }



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    /** The colormap used in rendering the image.
    */
    private int[][] _colormap = new int[3][256];

    /** The given height of the stripes in units of the y axis.
    */
    // I set _height to 2.2 for testing only.  This should be set by the
    // application.
    // private double _height = _yhighgiven - _ylowgiven;

    // I discovered that _height is already used in PlotBox.  I'm waiting on
    // this (commenting it out) to see what i need to do.

    /** A test color value from 0 through 255.
    */
    private static final int _HIGHCOLOR = 225;

    /** Stores the image data to be rendered. */
    private LinkedList _imageData = new LinkedList();

    /** A test color value from 0 through 255.
    */
    private static final int _LOWCOLOR = 175;

    /** @serial Set by _drawPlot(), and reset by clear(). */
    private boolean _showing = false;

    /** The increment in units of the x-axis of each stripe along the x-axis.
     * Each stripe will be _xIncrement wide.
     */
    private double _xIncrement = 0.0;

    /** The starting point of the x-axis.  The x-axis will start counting from
     * _xOffset.
     */
    private double _xOffset = 0.0;

    /** The increment in units of the y-axis of each patch within the stripe.
     * Each patch will be _yIncrement tall.
     */
    private double _yIncrement = 0.0;

    /** The starting point of the y-axis.  The y-axis will start counting from
     * _yOffset.
     */
    private double _yOffset = 0.0;
}
