/* An actor that reads from images using Open Computer Vision (OpenCV)

 Copyright (c) 2010-2013 The Regents of the University of California.
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

package ptolemy.actor.lib.opencv.javacv;

import static name.audet.samuel.javacv.jna.cv.CV_INTER_CUBIC;
import static name.audet.samuel.javacv.jna.cv.cvResize;
import static name.audet.samuel.javacv.jna.cxcore.cvCreateImage;
import static name.audet.samuel.javacv.jna.cxcore.cvReleaseImage;
import name.audet.samuel.javacv.jna.cxcore.CvSize;
import name.audet.samuel.javacv.jna.cxcore.IplImage;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageResize

/**
 * Resize image
  * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ImageResize extends Transformer {
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
    public ImageResize(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        widthParam = new Parameter(this, "Width", new IntToken(160));
        heightParam = new Parameter(this, "Height", new IntToken(120));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The size of resized image
     *  This parameter contains an IntegerToken, initially with value (320,240).
     */
    public Parameter widthParam;
    public Parameter heightParam;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    public void fire() throws IllegalActionException {
        int width = ((IntToken) (widthParam.getToken())).intValue();
        int height = ((IntToken) (heightParam.getToken())).intValue();
        CvSize size = new CvSize(width, height);

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof IplImage)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            _frame = (IplImage) inputObject;

            if (_resizedFrame == null) {
                _resizedFrame = cvCreateImage(size.byValue(), _frame.depth,
                        _frame.nChannels);
            } else if (_resizedFrame.width != width
                    || _resizedFrame.height != height) {
                cvReleaseImage(_resizedFrame.pointerByReference());
                _resizedFrame = cvCreateImage(size.byValue(), _frame.depth,
                        _frame.nChannels);
            }
            cvResize(_frame, _resizedFrame, CV_INTER_CUBIC);

            output.send(0, new ObjectToken(_resizedFrame));
        }
    }

    /** Initialize internal frame.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _resizedFrame = null;
    }

    /** Release image.
    *  @exception IllegalActionException If thrown by the super class.
    */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // FIXME If releasing when the following actor using this frame, it causes fatal error.
        //        if (_copyFrame != null) {
        //            _copyFrame.release();
        //        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IplImage _frame;
    private IplImage _resizedFrame;
}
