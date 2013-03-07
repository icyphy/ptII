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

import static name.audet.samuel.javacv.jna.cv.CV_INTER_LINEAR;
import static name.audet.samuel.javacv.jna.cv.CV_WARP_FILL_OUTLIERS;
import static name.audet.samuel.javacv.jna.cv.cv2DRotationMatrix;
import static name.audet.samuel.javacv.jna.cv.cvWarpAffine;
import static name.audet.samuel.javacv.jna.cxcore.CV_32FC1;
import static name.audet.samuel.javacv.jna.cxcore.cvCloneImage;
import static name.audet.samuel.javacv.jna.cxcore.cvCreateMat;
import static name.audet.samuel.javacv.jna.cxcore.cvPoint2D32f;
import static name.audet.samuel.javacv.jna.cxcore.cvScalarAll;
import name.audet.samuel.javacv.jna.cxcore.CvMat;
import name.audet.samuel.javacv.jna.cxcore.CvPoint2D32f;
import name.audet.samuel.javacv.jna.cxcore.CvScalar;
import name.audet.samuel.javacv.jna.cxcore.IplImage;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Image Rotate by using Affine Transform

/**
 * Roatate image
  * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 7.1 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ImageRotate extends Transformer {
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
    public ImageRotate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        angleParam = new Parameter(this, "angle", new DoubleToken(40.0));
        scaleParam = new Parameter(this, "scale", new DoubleToken(1.0));
        angleParam.setTypeEquals(BaseType.DOUBLE);
        scaleParam.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameters for rotation.
     *  This parameter contains an DobuleToken, initially with value 40.0 and 1.0.
     */
    public Parameter angleParam;
    public Parameter scaleParam;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    public void fire() throws IllegalActionException {
        double angle = ((DoubleToken) (angleParam.getToken())).doubleValue();
        double scale = ((DoubleToken) (scaleParam.getToken())).doubleValue();

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof IplImage)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            _src_frame = (IplImage) inputObject;
            _dst_frame = cvCloneImage(_src_frame);

            // FIXME: this sample use the center of entire image
            CvPoint2D32f center = cvPoint2D32f(_src_frame.width / 2,
                    _src_frame.height / 2);

            // interpolation param
            int flags = CV_INTER_LINEAR + CV_WARP_FILL_OUTLIERS;
            CvScalar fillval = cvScalarAll(0);

            cv2DRotationMatrix(center.byValue(), angle, scale, map_matrix);

            cvWarpAffine(_src_frame, _dst_frame, map_matrix, flags,
                    fillval.byValue());
            output.send(0, new ObjectToken(_dst_frame));
        }
    }

    /** Initialize internal frame and matrix.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        map_matrix = cvCreateMat(2, 3, CV_32FC1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IplImage _src_frame, _dst_frame;
    private CvMat map_matrix;
}
