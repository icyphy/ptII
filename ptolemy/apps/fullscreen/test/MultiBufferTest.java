/* Multiple Buffer Test

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

package ptolemy.apps.fullscreen.test;

import ptolemy.apps.fullscreen.MultiBuffer;
import ptolemy.apps.fullscreen.Transform;


import java.awt.Color;
import java.awt.DisplayMode;	// JDK1.4
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.io.File;
import javax.swing.ImageIcon;


//////////////////////////////////////////////////////////////////////////
//// MultiBufferTest
/**
Java 1.4 can display images using the full screen of the display
adapter.  This class contains static methods that can be use
to set up the full screen display

<p>Some of the code in this file is based on code from
http://java.sun.com/docs/books/tutorial/extra/fullscreen/example.html    

@see ptolemy.domains.sdf.lib.image.MultiBuffer
@author  Christopher Hylands
@version $Id$
 */

public class MultiBufferTest {

    /** This test takes a number up to 13 as an argument (assumes 2 by
     * default) and creates a multiple buffer strategy with the number
     * of buffers given.  This application enters full-screen mode, if
     * available, and flips back and forth between each buffer.
     */
    public MultiBufferTest(int numberOfBuffers) {
	// Look for $PTII
	// NOTE: This property is set by the vergil startup script
	// or by running java -Dptolemy.ptII.dir=c:/ptII . . .
	String ptIIDirectoryProperty =
	    System.getProperty("ptolemy.ptII.dir");
	if (ptIIDirectoryProperty == null) {
	    ptIIDirectoryProperty = "";
	}

	// Amount of time to delay in milliseconds. 
	double delay = 1000.0;

	GraphicsEnvironment graphicsEnvironment =
	    GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice graphicsDevices[] =
	    graphicsEnvironment.getScreenDevices();
	GraphicsDevice device =
	    graphicsDevices[graphicsDevices.length - 1];
	System.out.println("Device: " + device);


        try {
	    Frame mainFrame = MultiBuffer.enterFullScreenMode(device,
	    						      numberOfBuffers);
	    /*
	    GraphicsConfiguration graphicsConfiguration =
		device.getDefaultConfiguration();
	    System.out.println("GraphicsConfiguration: "
			       + graphicsConfiguration);
	    System.out.println("  ColorModel: "
			       + graphicsConfiguration.getColorModel());
	    Rectangle graphicsConfigurationBounds =
		graphicsConfiguration.getBounds();
	    System.out.println("  Bounds: "
			       + graphicsConfigurationBounds);

	    Frame mainFrame = new Frame(graphicsConfiguration);
	    mainFrame.setUndecorated(true);
	    mainFrame.setIgnoreRepaint(true);
	    mainFrame.enableInputMethods(false);
	    device.setFullScreenWindow(mainFrame);

	    if (device.isDisplayChangeSupported()) {
		//MultiBuffer.chooseBestDisplayMode(device);
		DisplayMode best = MultiBuffer.getBestDisplayMode(device);
		if (best != null) {
		    System.out.println("Best DisplayMode: "
				       + MultiBuffer.displayModeToString(best));
		    device.setDisplayMode(best);
		}
	    }
	    //mainFrame.createBufferStrategy(numberOfBuffers);
	    */

	    Rectangle bounds = mainFrame.getBounds();
	    System.out.println("Bounds: " + bounds.x + " " + bounds.y + " "
			       + bounds.width + " " + bounds.height);

	    //mainFrame.setLocation(graphicsConfigurationBounds.x,
	    //			  graphicsConfigurationBounds.y);


	    //BufferStrategy bufferStrategy = mainFrame.getBufferStrategy();

	    for (int i = 0; i < numberOfBuffers; i++) {
		//Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
		Graphics2D g = (Graphics2D) mainFrame.getGraphics();
		System.out.println("buffer: " + i);
		//if (!bufferStrategy.contentsLost()) {
		  if(true) {

		    g.setColor(_COLORS[i]);
		    System.out.println("after setColor");
		    //g.fillRect(0,0,bounds.width, bounds.height);
		    System.out.println("Bounds: before fillRectangle"
				       + bounds.x + " " + bounds.y + " "
				       + bounds.width + " " + bounds.height);

		    g.fillRect(bounds.x, bounds.y,
			       bounds.width*2, bounds.height);
		    String imageName =  ptIIDirectoryProperty
			+ "/" + _IMAGES[i];
		    System.out.println("Reading in " + imageName);
		    ImageIcon originalImageIcon =
			new ImageIcon(imageName);
		    Image originalImage =
			originalImageIcon.getImage();

		    if (_ROTATE[i]) {
			System.out.println("Rotating");
			BufferedImage rotatedImage = (BufferedImage)
			    Transform.rotate(originalImage, 90);
			originalImage = rotatedImage;
		    }
			
		    int maximumDimension = bounds.width;
		    if (originalImage.getHeight(null)
		      > originalImage.getWidth(null)) {
		      maximumDimension = bounds.height;
		      }
		    // Assume landscape instead of portrait orientation.
		    BufferedImage scaledImage = (BufferedImage)
			Transform.scale((Image)originalImage,
					maximumDimension);


		    int width = scaledImage.getWidth();
		    int height = scaledImage.getHeight();

		    int xOffset = 0;
		    int yOffset = 0;

		    if (width < bounds.width) {
			xOffset = (bounds.width - width)/2;
		    }

		    if (height < bounds.height) {
			yOffset = (bounds.height - height)/2;
		    }
			    
		    System.out.println("About to drawImage");
		    g.drawImage(scaledImage, xOffset, yOffset,
		    		width, height,
		    		_COLORS[i],
		    		originalImageIcon.getImageObserver());
		    /*g.drawImage(originalImage, 
				bounds.x, bounds.y,
			        bounds.width, bounds.height,
		    		_COLORS[i],
		    		originalImageIcon.getImageObserver());
		    */
		    System.out.println("About to show");
		    //bufferStrategy.show();
		    mainFrame.show();
		    //System.out.println("About to dispose");
		    g.dispose();
	        }
		try {
		    Thread.sleep((int)delay);
		} catch (InterruptedException e) {}
	    }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
	    //MultiBuffer.exitFullScreenMode();
	    device.setFullScreenWindow(null);
        }
    }
    
    public static void main(String[] args) {
        try {
            int numberOfBuffers = 2;
            if (args != null && args.length > 0) {
                numberOfBuffers = Integer.parseInt(args[0]);
                if (numberOfBuffers < 2 || numberOfBuffers > _COLORS.length ||
		    numberOfBuffers > _IMAGES.length ) {
                    System.err.println("Must specify between 2 and "
				       + ((_COLORS.length < _IMAGES.length) ?
					  _COLORS.length : _IMAGES.length)
				       + " buffers");
                    System.exit(1);
                }
            }
            MultiBufferTest test = new MultiBufferTest(numberOfBuffers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private static Color[] _COLORS = new Color[] {
        Color.red, Color.blue, Color.green, Color.white, Color.black,
        Color.yellow, Color.gray, Color.cyan, Color.pink, Color.lightGray,
        Color.magenta, Color.orange, Color.darkGray };
    private static String[] _IMAGES = new String [] {
	"doc/img/PtolemyII.jpg",
	"doc/img/PtolemyII.jpg",
	"doc/img/PtolemyIISmall.gif",
	"doc/img/ptIIbanner3.gif",
	"doc/img/ptIIexample.gif",
	"doc/img/ptIIexample.gif"
    };
    private static boolean[] _ROTATE = new boolean [] {
	false,
	false,
	false,
	false,
	false,
	false
    };
}
