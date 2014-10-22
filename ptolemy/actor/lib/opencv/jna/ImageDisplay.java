/* An actor that reads from images using Open Computer Vision (OpenCV)

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

package ptolemy.actor.lib.opencv.jna;

import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvDestroyWindow;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvNamedWindow;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvShowImage;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvWaitKey;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// OpenCVToAWTImage

public class ImageDisplay extends Sink {
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

        // FIXME: This is required to be an ImageToken, but
        // we don't see to have that class.
        input.setTypeEquals(BaseType.OBJECT);

        //cvNamedWindow ("Display", 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        cvNamedWindow("Display", 1);
    }

    /** Display IplImage.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            //if (!(inputObject instanceof IplImage)) {
            if (!(inputObject instanceof Pointer)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            //_frame = (IplImage)inputObject;
            _frame = (Pointer) inputObject;
            //_frame = cvCloneImage((IplImage)inputObject);
            // Read the next frame.

            cvShowImage("Display", _frame);
            //_frameNum++;
            //cvSaveImage("c:/temp/test_cap" + _frameNum + ".png",_frame);
            cvWaitKey(2);
            //if (c == 0x1b) _frameNum+=10000;
            //System.out.println(_frameNum);
        }
        return super.postfire();
    }

    /** Close window.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        //cvReleaseImage(new PointerByReference(_frame));
        cvDestroyWindow("Display");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //private IplImage _frame;
    private Pointer _frame;
}
