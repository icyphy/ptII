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

import java.awt.Container;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.data.AWTImageToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.media.Picture;

//////////////////////////////////////////////////////////////////////////
//// ImageDisplay

/**
 Display an image on the screen using the ptolemy.media.Picture
 class.  For a sequence of images that are all the same size, this
 class will continually update the picture with new data.  If the
 size of the input image changes, then a new Picture object is
 created.  This class will only accept a IntMatrixToken on its
 input, and assumes that the input image contains greyscale pixel
 intensities between 0 and 255 (inclusive).  The token is
 read in postfire().

 <p>Note that this actor really should be replaced by a conversion
 actor that converts IntMatrixTokens to ImageTokens.  However,
 there is no easy way to do that without accessing the graphical
 context of the actor.  An alternative would be to use the
 Java Advanced Imaging package and create an actor like
 $PTII/ptolemy/actor/lib/jai/DoubleMatrixToJAI.java.

 @author Steve Neuendorffer, Christopher Brooks
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
    ////                         private methods                   ////

    /** Convert an IntMatrixToken to a Packed RGB Image.
     *  @param token An IntMatrixToken defining the black and white image.
     *  @return A packed RGB array of integers
     */
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
                        | ((frame[j][i] & 255) << 8) | (frame[j][i] & 255);
            }
        }

        return RGBbuffer;
    }

    /** Display the specified token. This must be called in the Swing
     *  event thread.
     *  @param in The token to display.
     */
    protected void _display(Token in) {
        // FIXME: lots of code duplication with parent and
        // with ptolemy/actor/lib/image/ImageTableau.java
        // We could try to refactor this, but it would get tricky
        int xSize = ((IntMatrixToken) in).getColumnCount();
        int ySize = ((IntMatrixToken) in).getRowCount();

        if (_frame != null) {
            // We have a frame already, so just create an AWTImageToken
            // and tell the effigy about it.
            MemoryImageSource imageSource = new MemoryImageSource(xSize, ySize,
                    ColorModel.getRGBdefault(),
                    _convertBWImageToPackedRGBImage((IntMatrixToken) in), 0,
                    xSize);
            imageSource.setAnimated(true);

            // Sadly, we can't easily create an image without some sort
            // of graphical context here.  This is a huge shortcoming of
            // awt.  Just because I'm operating on images, I should not
            // need a frame.
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
                _picture
                        .setImage(_convertBWImageToPackedRGBImage((IntMatrixToken) in));
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
                // The _frame is null, the size has not changed, set the image.
                _picture
                        .setImage(_convertBWImageToPackedRGBImage((IntMatrixToken) in));
                _picture.displayImage();
                _picture.repaint();
            }
        }
    }
}
