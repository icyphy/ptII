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
        // FIXME


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
                    setXRange(-1.0, 1.0);
                    setYRange(-1.0, 1.0);


                    // I thought that this would remove the padding from the
                    // plot rectangle, but it didn't.
//                     _topPadding = 0;
//                     _bottomPadding = 0;
//                     _rightPadding = 0;
//                    _leftPadding = 0;

//                     System.out.println();
//                     System.out.println("_xlowgiven = " + _xlowgiven);
//                     System.out.println("_xhighgiven = " + _xhighgiven);
//                     System.out.println("_ylowgiven = " + _ylowgiven);
//                     System.out.println("_yhighgiven = " + _yhighgiven);
//                     System.out.println();
//                     System.out.println("yhigh - ylow = " +
//                                        (_yhighgiven - _ylowgiven));
//                     System.out.println();
//                     System.out.println("_height = " + _height);
// 
//                     System.out.println("_xMin = " + _xMin); 

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
                    for (int i = 1; i <= 500; i++) {
                        addStripe((double)i - .5, stripe1);
                        addStripe((double)i + .5, stripe2);
                    }

                    // FIXME fill in.

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

    /** Get current colormap.
    */

    public synchronized int[][] getColormap() {
        return _colormap;
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


//        System.out.println();
//        System.out.println("_xlowgiven = " + _xlowgiven);
//        System.out.println("_xhighgiven = " + _xhighgiven);
//        System.out.println("  _ylowgiven = " + _ylowgiven);
//        System.out.println("       _yMin = " + _yMin);
//        System.out.println(" _yhighgiven = " + _yhighgiven);
//        System.out.println("       _yMax = " + _yMax);
//        System.out.println();
//        System.out.println("yhigh - ylow = " +
//                           (_yhighgiven - _ylowgiven));
//        System.out.println(" yMax - yMin = " +
//                           (_yMax - _yMin));
//        System.out.println();
//        System.out.println("_height = " + _height);



        ListIterator imageDataIterator = _imageData.listIterator(0);

        double x = (double)_ulx + (double)((-1.1 - _xMin) * _xscale) + 1.0;
        //(_ulx + 1);

//         System.out.println("The width of the area is " + (_xMax - _xMin) +
//                            " units.");
//         System.out.println("The width of the area is " +
//                            ((_xMax - _xMin) * _xscale) +
//                            " pixels according to the scaling factor.");
//         System.out.println("But, the width is " +
//                            (int)(_lrx - _ulx) +
//                            " pixels according to the actual values.");
//         System.out.println("The number of stripes to be rendered is " +
//                            _imageData.size() + ".");
//         System.out.println("Therefore, there are ~" +
//                             (int)((_xMax - _xMin) * _xscale) /
//                             _imageData.size()
//                            + " pixels per stripe.");


        double width = (double)(2.2 * _xscale) / (double)_imageData.size();
        // divided by the
        // number of stripes (i.e _imageData.size())


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
         double y = _lry - (int)((1.1 - _yMin) * _yscale) + 1; //(_uly + 1);

         double height = (double)(2.2 * _yscale) / (double)length; 

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
     *  @param x The x coordinate of the left edge of the rectangle to be drawn.
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

    /** @serial Set by _drawPlot(), and reset by clear(). */
    private boolean _showing = false;

    /** Stores the image data to be rendered. */
    private LinkedList _imageData = new LinkedList();



    /** The colormap used in rendering the image.
    */
    private int[][] _colormap = new int[3][256];

    /** A test color value.
    */
    private static final int _HIGHCOLOR = 225;

    /** A test color value.
    */
    private static final int _LOWCOLOR = 175;

    /** The given height of the stripes in units of the y axis.
    */
    // I set _height to 2.2 for testing only.  This should be set by the
    // application.
    // private double _height = _yhighgiven - _ylowgiven;

    // I discovered that _height is already used in PlotBox.  I'm waitng on
    // this (commenting it out) to see what i need to do.

}
