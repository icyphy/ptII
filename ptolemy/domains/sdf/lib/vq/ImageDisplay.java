/* Display an Black and White image on the screen using the Picture class.

@Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
*/
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.gui.Placeable;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.media.Picture;

import java.io.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

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
*/

public final class ImageDisplay extends Sink implements Placeable {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageDisplay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        input.setTypeEquals(BaseType.INT_MATRIX);

        _oldxsize = 0;
        _oldysize = 0;
        _frame = null;
        _container = null;
    }

    /**
     * Initialize this actor.
     * If place has not been called, then create a frame to display the
     * image in.
     * @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _oldxsize = 0;
        _oldysize = 0;
        if(_container == null) {
            _frame = new _PictureFrame("ImageDisplay");
            _container = _frame.getContentPane();
        }
	if(_frame != null) {
	    _frame.setVisible(true);
	}
    }

    /**
     * Fire this actor.
     * Consume an IntMatrixToken from the input port.  If the image is not
     * the same size as the previous image, or this is the first image, then
     * create a new Picture object to represent the image, and put it in the
     * appropriate container (either the container set using place, or the frame
     * created during the initialize phase).
     * Convert the pixels from greyscale to RGBA triples (setting the
     * image to be opaque) and update the picture.
     * @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        IntMatrixToken message = (IntMatrixToken)
            input.get(0);
        int frame[][] = message.intMatrix();
        int xsize = message.getColumnCount();
        int ysize = message.getRowCount();

        // If the image changes size, then we have to go through some
        // trouble to resize the window.
        if((_oldxsize != xsize) || (_oldysize != ysize)) {
            _oldxsize = xsize;
            _oldysize = ysize;
            _RGBbuffer = new int[xsize * ysize];

	    if(_picture != null)
                _container.remove(_picture);

            _picture = new Picture(xsize, ysize);
            _picture.setImage(_RGBbuffer);
            _container.add("Center", _picture);
            _container.validate();
	    _container.invalidate();
            _container.repaint();
	    _container.doLayout();
	    Container c = _container.getParent();
	    while(c.getParent() != null) {
		c.invalidate();
		c.validate();
		c = c.getParent();
	    }
	    if(_frame != null) {
		_frame.pack();
	    }
        }

        // convert the B/W image to a packed RGB image.  This includes
        // flipping the image upside down.  (When it is drawn, it gets
        // drawn from bottom up).
        int i, j, index = 0;
        for(j = ysize - 1; j >= 0; j--) {
            for(i = 0; i < xsize; i++, index++)
                _RGBbuffer[index] = (255 << 24) |
                    ((frame[j][i] & 255) << 16) |
                    ((frame[j][i] & 255) << 8) |
                    (frame[j][i] & 255);
        }

        // display it.
        _picture.displayImage();
        _picture.repaint();

        Runnable painter = new Runnable() {
            public void run() {
                _container.paint(_container.getGraphics());
            }
        };
        try {
            // Make sure the image gets updated.
            SwingUtilities.invokeAndWait(painter);
        } catch(Exception e) {
            System.out.println("interrupted");
        }
    }

    /** Set the container that this actor should display data in.  If place
     * is not called, then the actor will create its own frame for display.
     */
    public void place(Container container) {
        _container = container;
	Container c = _container.getParent();
	while(c.getParent() != null) {
	    c = c.getParent();
	}
	if(c instanceof JFrame) {
	    _frame = (JFrame)c;
	}
    }

    /** This inner class provides a convenient way to create a JFrame for the
     *  picture when it becomes necessary.
     */
    private class _PictureFrame extends JFrame {
        public _PictureFrame(String title) {
            super(title);
            this.getContentPane().setLayout(new BorderLayout(15, 15));
            this.show();
	    this.pack();
            this.validate();
        }
    }

    private Picture _picture;
    private JFrame _frame;
    private Container _container;
    private int _oldxsize, _oldysize;
    private int _RGBbuffer[] = null;
}
