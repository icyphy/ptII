/* Display an Black and White image on the screen using the Picture class.

@Copyright (c) 1998-1999 The Regents of the University of California.
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
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import ptolemy.media.Picture;

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

public final class ImageDisplay extends SDFAtomicActor {
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
        
	input = (SDFIOPort) newPort("input");
        input.setInput(true);
        input.setTypeEquals(IntMatrixToken.class);

        _oldxsize = 0;
        _oldysize = 0;
        _frame = null;
        _panel = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            ImageDisplay newobj = (ImageDisplay)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /**
     * Initialize this actor.
     * If setPanel has not been called, then create a frame to display the
     * image in.
     * @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _oldxsize = 0;
        _oldysize = 0;
        if(_panel == null) {
            _frame = new _PictureFrame("ImageDisplay");
            _panel = _frame.getPanel();
        } else {
            _frame = null;
        }
    }

    /**
     * Fire this actor.
     * Consume an IntMatrixToken from the input port.  If the image is not
     * the same size as the previous image, or this is the first image, then
     * create a new Picture object to represent the image, and put it in the
     * appropriate panel (either the panel set using setPanel, or the frame
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
            _RGBbuffer = new int[xsize*ysize];
            if(_panel == null) {
                _frame = new _PictureFrame("ImageDisplay");
                _panel = _frame.getPanel();
            } else {
                _frame = null;
            }
            if(_picture != null)
                _panel.remove(_picture);
            _panel.setSize(xsize, ysize);
            _picture = new Picture(xsize, ysize);
            _picture.setImage(_RGBbuffer);
            _panel.add("Center", _picture);
            _panel.validate();

            Container c = _panel.getParent();
            while(c.getParent() != null) {
                c = c.getParent();
            }
            if(c instanceof Window) {
                ((Window) c).pack();
            } else {
                c.validate();
            }

            _panel.invalidate();
            _panel.repaint();

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
    }

    /** Set the panel that this actor should display data in.  If setPanel
     * is not called, then the actor will create its own frame for display.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
    }

    /** This inner class provides a convenient way to create a Frame for the
     *  picture when it becomes necessary.
     */
    private class _PictureFrame extends Frame {
        public _PictureFrame(String title) {
            super(title);
            this.setLayout(new BorderLayout(15, 15));
            this.show();
            _panel = new Panel();
            this.add("Center", _panel);
            this.pack();
            this.validate();
        }
        public Panel getPanel() {
            return _panel;
        }
        private Panel _panel;
    }

    private Picture _picture;
    private _PictureFrame _frame;
    private Panel _panel;
    private int _oldxsize, _oldysize;
    private int _RGBbuffer[] = null;
}
