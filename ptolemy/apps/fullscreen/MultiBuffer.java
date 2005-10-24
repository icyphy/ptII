/* Multiple Buffer, Multiple Screen and Full Screen static helper methods

@Copyright (c) 2001-2005 The Regents of the University of California.
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

PT_COPYRIGHT_VERSION 2
COPYRIGHTENDKEY
@ProposedRating Red (cxh)
@AcceptedRating Red (cxh)
*/
package ptolemy.apps.fullscreen;

import java.awt.DisplayMode; // JDK1.4
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;


//////////////////////////////////////////////////////////////////////////
//// MultiBuffer

/**

Java 1.4 can display images multiple buffers on multiple screens using
the full screen of the display adapter.  This class contains static
methods that can be use to set up the full screen display

<p>Some of the code in this file is based on code from
http://java.sun.com/docs/books/tutorial/extra/fullscreen/example.html

@see ptolemy.domains.sdf.lib.image.FullScreeImageDisplay
@author  Christopher Hylands
@version $Id$ */
public class MultiBuffer {
    /** Query the GraphicsDevice for the best DisplayMode and
     *  set the display to that mode.  If there is no
     *  best DisplayMode then do nothing.
     *  @see getBestDisplayMode(GraphicsDevice)
     */
    public static void chooseBestDisplayMode(GraphicsDevice device) {
        DisplayMode best = getBestDisplayMode(device);

        if (best != null) {
            device.setDisplayMode(best);
        }
    }

    /** Enter full screen mode on particular device using the best
     *        display mode.
     *  Usually, this method is called in a try block, and
     *  device.setFullScreenWindow(null) is called in the finally block.
     *
     *  @param device The device to enter full screen mode on.
     *  @param numberOfBuffers The number of buffers to create a strategy
     *  for.
     *  @return A Frame that can be used to display images.
     */
    public static Frame enterFullScreenMode(GraphicsDevice device,
        int numberOfBuffers) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();

        //device = graphicsEnvironment.getDefaultScreenDevice();
        GraphicsConfiguration gc = device.getDefaultConfiguration();
        Rectangle graphicsConfigurationBounds = gc.getBounds();
        Frame mainFrame = new Frame(gc);
        mainFrame.setUndecorated(true);
        mainFrame.setIgnoreRepaint(true);
        mainFrame.enableInputMethods(false);
        device.setFullScreenWindow(mainFrame);

        if (device.isDisplayChangeSupported()) {
            MultiBuffer.chooseBestDisplayMode(device);
        }

        mainFrame.setLocation(graphicsConfigurationBounds.x,
            graphicsConfigurationBounds.y);

        mainFrame.createBufferStrategy(numberOfBuffers);

        return mainFrame;
    }

    /** Enter full screen mode on the default device using the best
     *        display mode.
     *  Usually, this method is called in a try block, and
     *  device.setFullScreenWindow(null) is called in the finally block.
     *
     *  @param numberOfBuffers The number of buffers to create a strategy
     *  for.
     *  @return A Frame that can be used to display images.
     */
    public static Frame enterFullScreenMode(int numberOfBuffers) {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
        GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
        return enterFullScreenMode(device, numberOfBuffers);
    }

    /** Exit full screen mode in the defaultScreenDevice
     *  Usually, this method is called in a finally clause after
     *  enterFullScreenMode is called in a try block.
     */
    public static void exitFullScreenMode() {
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
        GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();

        // The GraphicsDevice.setFullScreenWindow() docs say:
        // "When returning to windowed mode from an exclusive full-screen
        // window, any display changes made by calling"
        // <code>setDisplayMode</code> are automatically restored to their
        // original state."
        device.setFullScreenWindow(null);
    }

    /** Return the best display mode of a graphics device.
     *  Query a graphics device for an array of possible modes and
     *  sequentially compare each possible mode with each element of an array
     *  of best display modes and return the first match.
     *
     *  @param device The graphics devices to query for possible display modes.
     *  @param bestDisplayModes An array of DisplayModes that are sequentially
     *  compared with the possible display modes
     *  @return The first DisplayMode in the bestDisplayModes array that
     *  matches a possible displayMode.  If there are no matches, return null.
     *  @see getBestDisplayMode(GraphicsDevice)
     */
    public static DisplayMode getBestDisplayMode(GraphicsDevice device,
        DisplayMode[] bestDisplayModes) {
        // FIXME java.awt.GraphicsConfigTemplate might be another solution?
        // This method is based on a method from the Sun website.
        DisplayMode[] modes = device.getDisplayModes();

        for (int i = 0; i < bestDisplayModes.length; i++) {
            for (int j = 0; j < modes.length; j++) {
                // One tricky aspect is that we do not check for different
                // refresh rates here, since the refresh rate could be
                // anything.
                //System.out.println("MultiBuffer.getBestDisplayMode(): "
                //                       + displayModeToString(modes[j]));
                if ((bestDisplayModes[i].getWidth() == modes[j].getWidth())
                            && (bestDisplayModes[i].getHeight() == modes[j]
                            .getHeight())
                            && (bestDisplayModes[i].getBitDepth() == modes[j]
                            .getBitDepth())) {
                    //System.out.println("MultiBuffer.getBestDisplayMode(): "
                    //                       + "returning: "
                    //                       + displayModeToString(modes[j]));
                    // Here, we return the modes[] value, which has
                    // the refresh rate set.
                    return modes[j];
                }
            }
        }

        return null;
    }

    /** Return the best display mode of a graphics device.
     *  Query a graphics device for an array of possible modes and
     *  sequentially compare each possible mode with each element of an array
     *  of predefined best display modes and return the first match.
     *
     *  @param device The graphics devices to query for possible display modes.
     *  @return The first DisplayMode in the predefined best DisplayModes array
     *  that matches a possible displayMode.
     *  If there are no matches, return null.
     *  @see getBestDisplayMode(GraphicsDevice, DisplayMode[])
     */
    public static DisplayMode getBestDisplayMode(GraphicsDevice device) {
        return getBestDisplayMode(device, _BEST_DISPLAY_MODES);
    }

    /** Return the predefined array of best display modes */
    public static DisplayMode[] getBestDisplayModes() {
        return _BEST_DISPLAY_MODES;
    }

    // Return a string representation of the DisplayMode.
    // This method is used for debugging.
    public static String displayModeToString(DisplayMode mode) {
        return new String(mode.getWidth() + " x " + mode.getHeight() + ", "
            + ((mode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI)
            ? "BIT_DEPTH_MULTI" : (new Integer(mode.getBitDepth())).toString())
            + ", "
            + ((mode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN)
            ? "REFRESH_RATE_UNKNOWN"
            : (new Integer(mode.getRefreshRate())).toString()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // DisplayModes to compare with the array of possible DisplayModes
    // for a GraphicsDevice
    private static DisplayMode[] _BEST_DISPLAY_MODES = new DisplayMode[] {
            new DisplayMode(1024, 768, 24, 0),
            new DisplayMode(1024, 768, 16, 0),
            new DisplayMode(1024, 768, 8, 0),
            new DisplayMode(640, 480, 32, 0),
            new DisplayMode(640, 480, 16, 0),
            new DisplayMode(640, 480, 8, 0)
        };
}
