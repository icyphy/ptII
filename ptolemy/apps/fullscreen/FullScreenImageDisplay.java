/* An actor that displays a java.awt.Image on the full screen

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

import ptolemy.actor.lib.Sink;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.ObjectToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.awt.Rectangle;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// FullScreenImageDisplay
/**
This actor reads an Object token that is a java.awt.Image from the input
and displays it in full screen mode

@see ptolemy.domains.sdf.lib.image.ImageReader
@author  Christopher Hylands
@version $Id$
 */
public class FullScreenImageDisplay extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FullScreenImageDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Set the type of the input port.
	input.setTypeEquals(BaseType.OBJECT);

        delayInMillis = new Parameter(this, "delayInMillis",
                new IntToken(1000));
	delayInMillis.setTypeEquals(BaseType.INT);

        exitFullScreenModeInWrapup =
	    new Parameter(this, "exitFullScreenModeInWrapup",
			  new BooleanToken(true));
	exitFullScreenModeInWrapup.setTypeEquals(BaseType.BOOLEAN);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount of time in milliseconds to delay between images
     *  This parameter contains an IntegerToken, initially with value 1000
     */
    public Parameter delayInMillis; 

    /** If true, then exit full screen mode in the wrapup() method.
     *  This parameter contains a BooleanToken, initially with true.
     *  Set this parameter to false for a slide show effect.
     */
    public Parameter exitFullScreenModeInWrapup; 

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one token from each input channel and discard it.
     *  If there is no input on a channel, then skip that channel, doing
     *  nothing with it.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int inputWidth = input.getWidth();
	int delay = ((IntToken)(delayInMillis.getToken())).intValue();
        for (int i = 0; i < inputWidth; i++) {
	    ObjectToken objectToken = (ObjectToken) input.get(i);
	    _image = (Image) objectToken.getValue();
	    Graphics2D graphics2D =
		(Graphics2D) _bufferStrategy.getDrawGraphics();
	    graphics2D.setColor(Color.black);
	    graphics2D.fillRect(0, 0, _bounds.width, _bounds.height);

	    System.out.println("FullScreenImageDisplay.prefire(): "
			       + i + " "
			       + _image.getWidth(null) + " x "
			       + _image.getHeight(null));

	    // Scale the image, assuming landscape displays
	    BufferedImage scaledImage = null;
	    if ( _image.getWidth(null) > _image.getHeight(null)) {
		System.out.println(" _image is landscape. "
				   + _bounds.getWidth());
		scaledImage = (BufferedImage)
		    Transform.scale(_image, (int)_bounds.getWidth());
	    } else {
		System.out.println(" _image is portrait. "
				   + _bounds.getHeight());
		scaledImage = (BufferedImage)
		    Transform.scale(_image, (int)_bounds.getHeight());
	    }

	    if (scaledImage != null) {
		_image = scaledImage;
		System.out.println("FullScreenImageDisplay.prefire()2: "
				   + _image.getWidth(null) + " x "
				   + _image.getHeight(null));
	    }

	    int width = _image.getWidth(null);
	    int height = _image.getHeight(null);
	    int xOffset = 0;
	    int yOffset = 0;

	    if (width < _bounds.width) {
		xOffset = (_bounds.width - width)/2;
	    }

	    if (height < _bounds.height) {
		yOffset = (_bounds.height - height)/2;
	    }
	    System.out.println("FullScreenImageDisplay.prefire()3: ("
			       + xOffset + ", " + yOffset
			       + ") (" + width + ", " + height + ")");

	    graphics2D.drawImage(_image, xOffset, yOffset,
				 width, height,
				 null);

	    _bufferStrategy.show();
	    graphics2D.dispose();
	    try {
		Thread.sleep(delay);
	    } catch (InterruptedException e) {}
        }
    }

    /** Enter full screen mode and do some initialization.
     *  @exception IllegalActionException If an IO error occurs or
     *  the display adapter cannot go into full screen mode.
     */
    public boolean prefire() throws IllegalActionException {
	System.out.println("FullScreenImageDisplay.prefire(): "
			   + input.getWidth());
	_frame = MultiBuffer.enterFullScreenMode(input.getWidth());
	_bounds = _frame.getBounds();
	_bufferStrategy = _frame.getBufferStrategy();
	return super.prefire();
    }

    /** Exit full screen mode
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void wrapup() throws IllegalActionException {
	// If exitFullScreenModeInWrapup is true, then call
	// exitFullScreenMode().
	if (((BooleanToken)(exitFullScreenModeInWrapup.getToken()))
	    .booleanValue()) {
	    MultiBuffer.exitFullScreenMode();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private BufferStrategy _bufferStrategy;

    private Frame _frame;

    // Image that is read in.
    private Image _image;

    private Rectangle _bounds;
}
