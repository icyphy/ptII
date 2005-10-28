/* An actor that displays a java.awt.Image using a full screen.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Sink;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;


//////////////////////////////////////////////////////////////////////////
//// FullScreenImageDisplay

/**
   This actor reads an Object token that is a java.awt.Image from the input
   and displays it in full screen mode on a GraphicsDevice that is
   determined from another input port.  The delayInMillis input port
   controls the delay between images.

   @see ptolemy.apps.fullscreen.ImageReader
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

        delayInMillis = new TypedIOPort(this, "delayInMillis", true, false);
        delayInMillis.setTypeEquals(BaseType.INT);

        exitFullScreenModeInWrapup = new Parameter(this,
                "exitFullScreenModeInWrapup", new BooleanToken(true));
        exitFullScreenModeInWrapup.setTypeEquals(BaseType.BOOLEAN);

        graphicsDevice = new TypedIOPort(this, "graphicsDevice", true, false);
        graphicsDevice.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The image delay port of type IntegerToken.
     *        This input port determines the amount of delay in milliseconds
     *  between images.
     */
    public TypedIOPort delayInMillis;

    /** If true, then exit full screen mode in the wrapup() method.
     *  This parameter contains a BooleanToken, initially with true.
     *  Set this parameter to false for a slide show effect.
     */
    public Parameter exitFullScreenModeInWrapup;

    /** This input port of type ObjectToken contains a
     *  java.awt.GraphicsDevice that is used to display the images.
     *  This port is only consulted during prefire(), so changing
     *  this port while firing will have no affect.
     */
    public TypedIOPort graphicsDevice;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one ObjectToken from each input channel and display
     *  it in full screen mode and then delay.  If the delayInMillis
     *  port has no tokens, then delay for 1000 milliseconds.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int inputWidth = input.getWidth();
        int delay = 1000;

        if (delayInMillis.hasToken(0)) {
            delay = ((IntToken) (delayInMillis.get(0))).intValue();
        }

        for (int i = 0; i < inputWidth; i++) {
            ObjectToken objectToken = (ObjectToken) input.get(i);
            Image image = (Image) objectToken.getValue();

            if ((image.getWidth(null) == -1) || (image.getHeight(null) == -1)) {
                System.out.println("FullScreenImageDisplay.fire(): "
                    + "Warning: width and/or height was -1. "
                    + "This usually indicates that the "
                    + "pathname to the file was incorrect");
            }

            Graphics2D graphics2D = (Graphics2D) _bufferStrategy
                        .getDrawGraphics();
            graphics2D.setColor(Color.black);
            graphics2D.fillRect(0, 0, _bounds.width, _bounds.height);

            // Scale the image, assuming landscape displays.
            BufferedImage scaledImage = null;

            if (image.getWidth(null) > image.getHeight(null)) {
                scaledImage = (BufferedImage) Transform.scale(image,
                        (int) _bounds.getWidth());
            } else {
                scaledImage = (BufferedImage) Transform.scale(image,
                        (int) _bounds.getHeight());
            }

            if (scaledImage != null) {
                image = scaledImage;
            }

            int width = image.getWidth(null);
            int height = image.getHeight(null);
            int xOffset = 0;
            int yOffset = 0;

            if (width < _bounds.width) {
                xOffset = (_bounds.width - width) / 2;
            }

            if (height < _bounds.height) {
                yOffset = (_bounds.height - height) / 2;
            }

            graphics2D.drawImage(image, xOffset, yOffset, width, height, null);
            _bufferStrategy.show();
            graphics2D.dispose();

            if (1 == 0) {
                // Loop through different alpha values.
                // We draw a rectangle of the same color over and over
                // again, which gives us a fast fade.
                // We could try different functions on alpha here.
                AlphaComposite alphaComposite = null;
                float alpha = 0.05f;

                for (int m = 0; m < 100; m++) {
                    alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                            alpha);
                    graphics2D = (Graphics2D) _bufferStrategy.getDrawGraphics();
                    graphics2D.setComposite(alphaComposite);
                    graphics2D.fillRect(_bounds.x, _bounds.y, _bounds.width,
                        _bounds.height);
                    graphics2D.dispose();
                    _bufferStrategy.show();
                }
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
        }
    }

    /** Enter full screen mode and do some initialization.
     *  @exception IllegalActionException If an IO error occurs or
     *  the display adapter cannot go into full screen mode.
     */
    public boolean prefire() throws IllegalActionException {
        _graphicsDeviceValue = (GraphicsDevice) ((ObjectToken) graphicsDevice
                    .get(0)).getValue();

        if (!_inFullScreenMode) {
            _frame = MultiBuffer.enterFullScreenMode(_graphicsDeviceValue,
                    input.getWidth());
            _bounds = _frame.getBounds();
            _bufferStrategy = _frame.getBufferStrategy();
            _inFullScreenMode = true;
        }

        return super.prefire();
    }

    /** Exit full screen mode if the exitFullScreeModeInWrapup
     *  parameter is true.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        // If exitFullScreenModeInWrapup is true, then call
        // exitFullScreenMode().
        if (((BooleanToken) (exitFullScreenModeInWrapup.getToken()))
                    .booleanValue()) {
            // The GraphicsDevice.setFullScreenWindow() docs say:
            // "When returning to windowed mode from an exclusive full-screen
            // window, any display changes made by calling"
            // <code>setDisplayMode</code> are automatically restored to their
            // original state."
            if (_graphicsDeviceValue != null) {
                _graphicsDeviceValue.setFullScreenWindow(null);
            }

            _inFullScreenMode = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Buffer strategy to use to display the images.
    private BufferStrategy _bufferStrategy;

    // Frame to display the images in.
    private Frame _frame;

    // java.awt.GraphicsDevice to display the images on.
    private GraphicsDevice _graphicsDeviceValue;

    // True if we are in full scree mode
    private boolean _inFullScreenMode = false;

    // Bounds of the Frame that displays the image.
    private Rectangle _bounds;
}
