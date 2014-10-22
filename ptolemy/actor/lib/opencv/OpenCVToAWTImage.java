/* An actor that gets an image from an openCV object

 Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.actor.lib.opencv;

import java.awt.Frame;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import processing.core.PImage;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// OpenCVToAWTImage

/**
 * Convert an input OpenCV object into an AWT image.
 * FIXME: What does this really mean? Current frame? What's "current"?
 * @author Dorsa Sadigh, Steve Bako, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class OpenCVToAWTImage extends Transformer {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OpenCVToAWTImage(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: create a separate type for OpenCV Frames or
        // create a type for ptolemy.data.AWTImageToken.
        // Same should probably be true of an AWT Image.
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        // FIXME: HACK! There must be a better way to get an
        // AWT frame from OpenCV.
        _dummyFrame = new Frame();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof OpenCVImageObject)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of OpenCVImageObject. Got "
                                + inputObject.getClass());
            }
            OpenCVImageObject oio = (OpenCVImageObject) inputObject;

            PImage my_image = oio.img;
            Image output_image;
            MemoryImageSource mis = new MemoryImageSource(my_image.width,
                    my_image.height, my_image.pixels, 0, my_image.width);
            output_image = _dummyFrame.createImage(mis);
            output.send(0, new AWTImageToken(output_image));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The dummy frame, needed to create an image.
     *  FIXME: can't we create an image without this?
     *  Otherwise, this actor might not work in a headless environment.
     *  Since we are reading from a camera, this might be moot, but
     *  a good design would not have this dependency on Frame.   If
     *  this dependency persists, then this actor should be movedd
     *  to actor.lib.gui.opencv.
     */
    private Frame _dummyFrame;
}
