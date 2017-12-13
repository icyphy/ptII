/* Helper for the imageDisplay JavaScript module.

   Copyright (c) 2017 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

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

*/
package ptolemy.actor.lib.jjs.modules.imageDisplay;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.data.AWTImageToken;
import ptolemy.media.Picture;


/** Helper for the imageDisplay JavaScript module.

 *  @author Christopher Brooks, based on ImageDisplayJavaSE by Jianwu Wang which in turne was based on code by James Yeh, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class ImageDisplayHelper {

    public ImageDisplayHelper() {
        // This has to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
                @Override
                public void run() {
                    _createOrShowWindow();
                }
            };

        SwingUtilities.invokeLater(doDisplay);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Display an image.
     *
     *  @param imageToken The image to be displayed.
     */
    public void displayImage(AWTImageToken imageToken) {
        // Display probably to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
                @Override
                public void run() {
                    _display(imageToken);
                }
            };

        SwingUtilities.invokeLater(doDisplay);
    }

    private void _createOrShowWindow() {
        if (_frame == null) {
            _frame = new JFrame();
            _frame.setDefaultCloseOperation
                (JFrame.DISPOSE_ON_CLOSE);

            if (_picture == null) {
                // Create the pane.
                _picture = new Picture(_oldXSize, _oldYSize);
            }

            _frame.add(_picture, BorderLayout.CENTER);

            _frame.pack();
            _frame.setVisible(true);
            _frame.toFront();
        }
    }

    private void _display(AWTImageToken imageToken) {

        Image image = imageToken.asAWTImage();
        int xSize = image.getWidth(null);
        int ySize = image.getHeight(null);

        // If the size has changed, have to recreate the Picture object.
        if (_oldXSize != xSize || _oldYSize != ySize) {
            _oldXSize = xSize;
            _oldYSize = ySize;

            Container container = _picture.getParent();
            container.remove(_picture);
            _picture = new Picture(xSize, ySize);
            _picture.setImage(image);
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
            _picture.setImage(image);
            _picture.displayImage();
            _picture.repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The horizontal size of the previous image. */
    protected int _oldXSize = -1;

    /** The vertical size of the previous image. */
    protected int _oldYSize = -1;

    /** The picture panel. */
    protected Picture _picture = null;

    private JFrame _frame;
}
