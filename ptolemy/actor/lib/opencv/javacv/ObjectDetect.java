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

import static name.audet.samuel.javacv.jna.cv.cvEqualizeHist;
import static name.audet.samuel.javacv.jna.cv.cvHaarDetectObjects;
import static name.audet.samuel.javacv.jna.cxcore.IPL_DEPTH_8U;
import static name.audet.samuel.javacv.jna.cxcore.cvClearMemStorage;
import static name.audet.samuel.javacv.jna.cxcore.cvCreateImage;
import static name.audet.samuel.javacv.jna.cxcore.cvCreateMemStorage;
import static name.audet.samuel.javacv.jna.cxcore.cvLoad;
import name.audet.samuel.javacv.jna.cv.CvHaarClassifierCascade;
import name.audet.samuel.javacv.jna.cxcore.CvMemStorage;
import name.audet.samuel.javacv.jna.cxcore.CvSeq;
import name.audet.samuel.javacv.jna.cxcore.CvSize;
import name.audet.samuel.javacv.jna.cxcore.IplImage;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// ImageFlip

/**
 * Flip image
  * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ObjectDetect extends Transformer {
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
    public ObjectDetect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        pathName = new StringAttribute(this, "pathName");
        pathName.setExpression("C:/Program Files/OpenCV/data/haarcascades/haarcascade_frontalface_default.xml");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The name of the file to write to. The default
     *  value of this parameter is "haarcascade_frontalface_default.xml"
     */
    public StringAttribute pathName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    public void fire() throws IllegalActionException {

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof IplImage)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            _srcFrame = (IplImage) inputObject;
            CvSize size = new CvSize(40, 40);
            _objectSeq = cvHaarDetectObjects(_srcFrame, _cascade, _storage,
                    1.11, 4, 0, size.byValue());

            output.send(0, new ObjectToken(_objectSeq));

            //            draw_object_circle(_objectSeq,_srcFrame);
            //            output.send(0, new ObjectToken(_srcFrame));
        }
    }

    /** Initialize internal frame.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        //FIXME: dummy call for CvHaarClassifierCascade.
        //       That causes crash before calling a function of cv library.
        IplImage dummy_img = cvCreateImage(new CvSize(60, 60).byValue(),
                IPL_DEPTH_8U, 1);
        cvEqualizeHist(dummy_img, dummy_img);
        dummy_img.release();

        String cascade_name = pathName.getExpression();
        //String cascade_name = "C:/temp/haarcascade_frontalface_default.xml";
        _cascade = new CvHaarClassifierCascade(cvLoad(cascade_name, null, null,
                null));
        if (_storage == null) {
            _storage = cvCreateMemStorage(0);
        }
        cvClearMemStorage(_storage);
        //       _dstFrame = null;
    }

    /** Release image.
    *  @exception IllegalActionException If thrown by the super class.
    */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        //        if (_cascade != null) {
        //            _cascade.release();
        //        }
        //        if (_storage != null) {
        //            _storage.release();
        //        }
        //        if (_objectSeq != null) {
        //            _storage.release();
        //        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    //    private void draw_object_circle(CvSeq objs, IplImage _image )throws IllegalActionException {
    //        int i = 0;
    //        int objTotal = 0;
    //        if (objs != null) objTotal = objs.total;
    //
    //        for (i = 0; i < objTotal; i++) {
    //            Pointer r = cvGetSeqElem (objs, i);
    //            CvRect rect = new CvRect(r);
    //            CvPoint center = new CvPoint(0,0);
    //            int radius;
    //            center.x = (int)round (rect.x + rect.width * 0.5);
    //            center.y = (int)round (rect.y + rect.height * 0.5);
    //            radius = (int)round ((rect.width + rect.height) * 0.25);
    //            cvCircle (_image, center.byValue(), radius, CvScalar.RED , 3, 8, 0);
    //          }
    //
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IplImage _srcFrame;
    //    private IplImage _dstFrame;
    private CvHaarClassifierCascade _cascade;
    private CvMemStorage _storage;
    private CvSeq _objectSeq;
}
