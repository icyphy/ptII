/* Multiple Buffer and Full Screen static helper methods

@Copyright (c) 2001 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.apps.fullscreen;


import java.awt.DisplayMode;	// JDK1.4
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice; // JDK1.4
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.awt.geom.AffineTransform;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

//////////////////////////////////////////////////////////////////////////
//// MultiBuffer
/**
Java 1.4 can display images using the full screen of the display
adapter.  This class contains static methods that can be use
to set up the full screen display

<p>Some of the code in this file is based on code from
http://java.sun.com/docs/books/tutorial/extra/fullscreen/example.html    

@see ptolemy.domains.sdf.lib.image.FullScreeImageDisplay
@author  Christopher Hylands
@version $Id$
 */

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
    
    /** Enter full screen mode, draw an image and exit full screen mode.
     *  @param image The image to draw.
     */	
    public static void drawImage(Image image) {
	try {
	    Frame mainFrame = enterFullScreenMode(1);
	    Rectangle bounds = mainFrame.getBounds();
	    BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();
	    Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
	    g.drawImage(image, 0, 0, 
			bounds.width, bounds.height,
			null);
	    bufferStrategy.show();
	    g.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
	    exitFullScreenMode();
        }
    }

    /** Enter full screen mode.  The current DisplayMode is saved,
     *  and the best display mode is chosen.  Usually, this method
     *  is called in a try{} block, and exitFullScreenMode() is called
     *  in a finally{} clause.
     *  @param numberOfBuffers The number of buffers to create a strategy
     *  for.
     *  @return A Frame that can be used to display images.  
     */
    public static Frame enterFullScreenMode(int numberOfBuffers) {
	GraphicsEnvironment graphicsEnvironment = 
	    GraphicsEnvironment.getLocalGraphicsEnvironment();
	_device = graphicsEnvironment.getDefaultScreenDevice();

	GraphicsConfiguration gc = _device.getDefaultConfiguration();
	Frame mainFrame = new Frame(gc);
	mainFrame.setUndecorated(true);
	mainFrame.setIgnoreRepaint(true);
	mainFrame.enableInputMethods(false);
	_device.setFullScreenWindow(mainFrame);
	if (_device.isDisplayChangeSupported()) {
	    MultiBuffer.chooseBestDisplayMode(_device);
	}
	mainFrame.createBufferStrategy(numberOfBuffers);

	return mainFrame;
    }


    /** Exit full screen mode and reset the DisplayMode to the 
     *	DisplayMode that was in use before we went to full screen mode.
     *  Usually, this method is called in a finally clause after
     *  enterFullScreenMode is called in a try block. 
     */
    public static void exitFullScreenMode() {
	// The GraphicsDevice.setFullScreenWindow() docs say:
	// "When returning to windowed mode from an exclusive full-screen
	// window, any display changes made by calling"
	// <code>setDisplayMode</code> are automatically restored to their
	// original state."
	_device.setFullScreenWindow(null);	
    }

    /** Return the best display mode of a graphics device.
     *  Query a graphics device for an array of possible modes and
     *  sequentially compare each possible mode with each element of an array
     *  of best display modes and return the first match.
     *  @see getBestDisplayMode(GraphicsDevice)
     *  @param device The graphics devices to query for possible display modes.
     *  @param bestDisplayModes An array of DisplayModes that are sequentially
     *  compared with the possible display modes
     *  @return The first DisplayMode in the bestDisplayModes array that
     *  matches a possible displayMode.  If there are no matches, return null.
     */
    public static DisplayMode
	getBestDisplayMode(GraphicsDevice device,
			   DisplayMode[] bestDisplayModes) {
        DisplayMode[] modes = device.getDisplayModes();

        for (int i = 0; i < bestDisplayModes.length; i++) {
	    //System.out.println("MultiBuffer.getBestDisplayMode(): checking " 
	    //		       + _displayModeToString(bestDisplayModes[i]));
            for (int j = 0; j < modes.length; j++) {
		//System.out.println("MultiBuffer.getBestDisplayMode(): " 
		//	       + _displayModeToString(modes[j]));
		// One tricky bit is that we do not check for different
		// refresh rates here, since the refresh rate could be
		// anything.
                if (bestDisplayModes[i].getWidth() == modes[j].getWidth()
                   && bestDisplayModes[i].getHeight() == modes[j].getHeight()
                   && bestDisplayModes[i].getBitDepth()
		    == modes[j].getBitDepth()
		    ) {
		    System.out.println("MultiBuffer.getBestDisplayMode(): " 
				       + "returning: "
				       + _displayModeToString(modes[j]));
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
     *  @param device The graphics devices to query for possible display modes.
     *  @return The first DisplayMode in the predefined best DisplayModes array
     *  that matches a possible displayMode.
     *  If there are no matches, return null.
     *  @see getBestDisplayMode(GraphicsDevice, DisplayMode[])
     */
    public static DisplayMode
	getBestDisplayMode(GraphicsDevice device) {
	return getBestDisplayMode(device, _BEST_DISPLAY_MODES);
    }

    /** Return the predefined array of best display modes */
    public static DisplayMode [] getBestDisplayModes() {
	return _BEST_DISPLAY_MODES;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return a string representation of the DisplayMode.
    private static String _displayModeToString(DisplayMode mode) {
	return new String( mode.getWidth() + " x "
			 + mode.getHeight() + ", "
			 + (mode.getBitDepth()
			    == DisplayMode.BIT_DEPTH_MULTI
			    ? "BIT_DEPTH_MULTI" 
			    : (new Integer(mode.getBitDepth())).toString())
			 + ", "
			 + (mode.getRefreshRate()
			    == DisplayMode.REFRESH_RATE_UNKNOWN
			    ? "REFRESH_RATE_UNKNOWN" 
			    : (new Integer(mode.getRefreshRate())).toString())
			  );
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

    // The DisplayMode that was being used before we entered the full
    // DisplayMode.
    private static GraphicsDevice _device = null;
}
