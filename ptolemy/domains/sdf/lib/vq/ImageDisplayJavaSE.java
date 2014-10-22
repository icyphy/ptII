/* An AWT and Swing implementation of the the ImageDisplayInterface
 that displays a Black and White image on the screen using the Picture class.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
import javax.swing.SwingUtilities;

import ptolemy.data.AWTImageToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.media.Picture;

///////////////////////////////////////////////////////////////////
////ImageDisplayJavaSE

/**
<p>
ImageDisplayJavaSE is the implementation of the ImageDisplayInterface that uses AWT and Swing
classes.</p>

@author Jianwu Wang, Based on code by James Yeh, Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating
@Pt.AcceptedRating
 */

public class ImageDisplayJavaSE extends
        ptolemy.actor.lib.image.ImageDisplayJavaSE implements
        ptolemy.domains.sdf.lib.vq.ImageDisplayInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Display the specified token. This must be called in the Swing
     *  event thread.
     *  @param in The token to display
     */
    @Override
    public void display(final Token in) {
        // Display probably to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
            @Override
            public void run() {
                _display(in);
            }
        };

        SwingUtilities.invokeLater(doDisplay);
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
                RGBbuffer[index] = 255 << 24 | (frame[j][i] & 255) << 16
                        | (frame[j][i] & 255) << 8 | frame[j][i] & 255;
            }
        }

        return RGBbuffer;
    }

    /** Display the specified token. This must be called in the Swing
     *  event thread.
     *  @param in The token to display.
     */
    private void _display(Token in) {
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
            if (_oldXSize != xSize || _oldYSize != ySize) {
                _oldXSize = xSize;
                _oldYSize = ySize;

                Container container = _picture.getParent();

                container.remove(_picture);

                _picture = new Picture(xSize, ySize);

                //_picture.setImage(image);
                _picture.setImage(_convertBWImageToPackedRGBImage((IntMatrixToken) in));
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
                _picture.setImage(_convertBWImageToPackedRGBImage((IntMatrixToken) in));
                _picture.displayImage();
                _picture.repaint();
            }
        }
    }

}
