/* Display an Black and White image on the screen using the Picture class.

@Copyright (c) 1998-2005 The Regents of the University of California.
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
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.Sink;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.AWTImageToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.media.Picture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// ImageDisplay

/**
   Display an image on the screen using the ptolemy.media.Picture
   class.  For a sequence of images that are all the same size, this class
   will continually update the picture with new data.   If the size of the
   input image changes, then a new Picture object is created.  This class
   will only accept a IntMatrixToken on its input, and assumes that the
   input image contains greyscale pixel intensities between 0 and 255 (inclusive).

   @author Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (neuendor)
   @Pt.AcceptedRating Red
*/
public class ImageDisplay extends ptolemy.actor.lib.image.ImageDisplay {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.INT_MATRIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then removes association with graphical objects
     *  belonging to the original class.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ImageDisplay newObject = (ImageDisplay) super.clone(workspace);
        return newObject;
    }

    /**
     * Fire this actor.
     * Consume an IntMatrixToken from the input port.  If the image is
     * not the same size as the previous image, or this is the first
     * image, then create a new Picture object to represent the image,
     * and put it in the appropriate container (either the container
     * set using place, or the frame created during the initialize
     * phase).
     * Convert the pixels from greyscale to RGBA triples (setting the
     * image to be opaque) and update the picture.
     * @exception IllegalActionException If a contained method throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            final Token in = input.get(0);

            // Display probably to be done in the Swing event thread.
            Runnable doDisplay = new Runnable() {
                    public void run() {
                        _display2(in);
                    }
                };

            SwingUtilities.invokeLater(doDisplay);
        }

        return true;

//         IntMatrixToken message = (IntMatrixToken) input.get(0);
//         int[][] frame = message.intMatrix();
//         int xSize = message.getColumnCount();
//         int ySize = message.getRowCount();

//         // If the image changes size, then we have to go through some
//         // trouble to resize the window.
//         if ((_oldXSize != xSize) || (_oldYSize != ySize)) {
//             _oldXSize = xSize;
//             _oldYSize = ySize;
//             _RGBbuffer = new int[xSize * ySize];

//             if (_picture != null) {
//                 _container.remove(_picture);
//             }

//             _picture = new Picture(xSize, ySize);
//             _picture.setImage(_RGBbuffer);
//             _picture.setBackground(null);

//             _container.add("Center", _picture);
//             _container.validate();
//             _container.invalidate();
//             _container.repaint();
//             _container.doLayout();

//             Container c = _container.getParent();

//             while (c.getParent() != null) {
//                 c.invalidate();
//                 c.validate();
//                 c = c.getParent();
//             }

//             if (_frame != null) {
//                 _frame.pack();
//             }
//         }

//         // convert the B/W image to a packed RGB image.  This includes
//         // flipping the image upside down.  (When it is drawn, it gets
//         // drawn from bottom up).
//         int i;

//         // convert the B/W image to a packed RGB image.  This includes
//         // flipping the image upside down.  (When it is drawn, it gets
//         // drawn from bottom up).
//         int j;

//         // convert the B/W image to a packed RGB image.  This includes
//         // flipping the image upside down.  (When it is drawn, it gets
//         // drawn from bottom up).
//         int index = 0;

//         for (j = ySize - 1; j >= 0; j--) {
//             for (i = 0; i < xSize; i++, index++) {
//                 _RGBbuffer[index] = (255 << 24) | ((frame[j][i] & 255) << 16)
//                     | ((frame[j][i] & 255) << 8)
//                     | (frame[j][i] & 255);
//             }
//         }

//         // display it.
//         _picture.displayImage();
//         _picture.repaint();

//         Thread.yield();
    }

    /** Display the specified token. This must be called in the Swing
     *  event thread.
     *  @param in The token to display
     */
    private void _display2(Token in) {
        if (_frame != null) {
            int xSize = ((IntMatrixToken) in).getColumnCount();
            int ySize = ((IntMatrixToken) in).getRowCount();
            MemoryImageSource imageSource = 
                new MemoryImageSource(xSize, ySize,
                        ColorModel.getRGBdefault(),
                        _convertBWImageToPackedRGBImage((IntMatrixToken) in),
                        0, xSize);
            imageSource.setAnimated(true);
            Image image = _frame.getContentPane().createImage(imageSource);
            AWTImageToken token = new AWTImageToken(image);
            List tokens = new LinkedList();
            tokens.add(token);

            try {
                _effigy.setTokens(tokens);
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        } else if (_picture != null) {
            //Image image = ((ImageToken) in).asAWTImage();
            
            int xSize = ((IntMatrixToken) in).getColumnCount();
            int ySize = ((IntMatrixToken) in).getRowCount();

            // If the size has changed, have to recreate the Picture object.
            if ((_oldXSize != xSize) || (_oldYSize != ySize)) {
                if (_debugging) {
                    _debug("Image size has changed.");
                }

                _oldXSize = xSize;
                _oldYSize = ySize;

                Container container = _picture.getParent();

                if (_picture != null) {
                    container.remove(_picture);
                }

                _picture = new Picture(xSize, ySize);
                //_picture.setImage(image);
                _picture.setImage(
                        _convertBWImageToPackedRGBImage((IntMatrixToken) in));
                _picture.setBackground(null);
                container.add("Center", _picture);
                container.validate();
                container.invalidate();
                container.repaint();
                container.doLayout();

                Container c = container.getParent();

                while (c.getParent() != null) {
                    c.invalidate();
                    c.validate();
                    c = c.getParent();

                    if (c instanceof JFrame) {
                        ((JFrame) c).pack();
                    }
                }
            } else {
                //_picture.setImage(((ImageToken) in).asAWTImage());
                _picture.setImage(
                        _convertBWImageToPackedRGBImage((IntMatrixToken) in));
                _picture.displayImage();
                _picture.repaint();
            }
        }
    }

    private int[] _convertBWImageToPackedRGBImage(IntMatrixToken token) {
        int[][] frame = token.intMatrix();
        int xSize = token.getColumnCount();
        int ySize = token.getRowCount();

        int[] RGBbuffer = new int[xSize * ySize];

        // convert the B/W image to a packed RGB image.  This includes
        // flipping the image upside down.  (When it is drawn, it gets
        // drawn from bottom up).
        int i;

        // convert the B/W image to a packed RGB image.  This includes
        // flipping the image upside down.  (When it is drawn, it gets
        // drawn from bottom up).
        int j;

        // convert the B/W image to a packed RGB image.  This includes
        // flipping the image upside down.  (When it is drawn, it gets
        // drawn from bottom up).
        int index = 0;

        for (j = ySize - 1; j >= 0; j--) {
            for (i = 0; i < xSize; i++, index++) {
                RGBbuffer[index] = (255 << 24) | ((frame[j][i] & 255) << 16)
                    | ((frame[j][i] & 255) << 8)
                    | (frame[j][i] & 255);
            }
        }

        return RGBbuffer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //private int[] _RGBbuffer = null;
}
