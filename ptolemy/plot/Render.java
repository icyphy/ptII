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

        // Get the graphics context.

        Graphics graphics = getGraphics();

        // Draw the stripe if the graphics context ... ISTHERE(?)...
        if (graphics != null) {
            _drawStripe(graphics, newStripe, x);
        }
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

    /** Set a colormap.
    * The user needs to give a 3-by-256 integer array as a colormap.
    */

    public void setColormap(int[][] colormap){
        _colormap = colormap;
    }

    /** Get current colormap.
    */

    public int[][] getColormap() {
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


        // Draw a few filled in rectangles alternating colors (e.g. black and white).

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
        System.arraycopy(stripe1, 0, stripe2, 1, stripe2.length - 1);
        stripe2[0] = _LOWCOLOR;

        int length = stripe1.length;


        // Draw the stripe one patch (data element) at a time.
        int y = (_uly + 1);
        int x = (_ulx + 1);
        int width = (int)((_xMax - _xMin) * _xscale) - 1; // divided by the number of stripes (i.e _imageData.size())
        int height = (int)(((_yMax - _yMin) * _yscale) / length); 

        for (int i = 0; i < length; i++) {
            
            // Convert the color value into it's r, g, and b.
            int r = _colormap[0][stripe1[i]];
            int g = _colormap[1][stripe1[i]];
            int b = _colormap[2][stripe1[i]];

            // Set the color.
            graphics.setColor(new Color(r, g, b));

            // Draw the patch.
            graphics.fillRect(x, y, width, height);

            // Increment the y value so the next patch is printed in the right place.
            y += height;
        }

        // Indicate that the plot is showing.
        _showing = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Draw the specified stripe.
     */

     private void _drawStripe(Graphics graphics, int[] stripe, double x) {

         // Draw the stripe one patch at a time.

         int length = stripe.length;
         for (int i = 0; i < length; i++) {
             _drawPatch(graphics, (int)x, i, 5, 5, stripe[i]);
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

}
