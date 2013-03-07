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

import static java.lang.Math.round;
import static name.audet.samuel.javacv.jna.cxcore.cvCircle;
import static name.audet.samuel.javacv.jna.cxcore.cvGetSeqElem;
import name.audet.samuel.javacv.jna.cxcore.CvPoint;
import name.audet.samuel.javacv.jna.cxcore.CvRect;
import name.audet.samuel.javacv.jna.cxcore.CvScalar;
import name.audet.samuel.javacv.jna.cxcore.CvSeq;
import name.audet.samuel.javacv.jna.cxcore.IplImage;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// Draw circles for the result sequence data

/**
 * Draw result
  * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks, Dorsa Sadigh, Steve Bako
 * @version
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DrawResultSeq extends Transformer {
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
    public DrawResultSeq(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        rot_input = new TypedIOPort(this, "rotation", true, false);
        rot_input.setTypeEquals(BaseType.INT);

        pathName = new StringAttribute(this, "pathName");
        pathName.setExpression("haarcascade_frontalface_default.xml");

        seq = new TypedIOPort(this, "sequence", true, false);
        seq.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The name of the file to write to. The default
     *  value of this parameter is "haarcascade_frontalface_default.xml"
     */
    public StringAttribute pathName;

    /** The input sequence. */
    public TypedIOPort seq;
    public TypedIOPort rot_input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    public void fire() throws IllegalActionException {
        rotation = 0;
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof IplImage)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            _srcFrame = (IplImage) inputObject;
            if (rot_input.hasToken(0)) {
                rotation = ((IntToken) rot_input.get(0)).intValue();
            }
            if (seq.hasToken(0)) {
                ObjectToken seqToken = (ObjectToken) seq.get(0);
                Object seqObject = seqToken.getValue();
                if (!(seqObject instanceof CvSeq)) {
                    throw new IllegalActionException(this,
                            "Input is required to be an instance of IplImage. Got "
                                    + seqObject.getClass());
                }
                _objectSeq = (CvSeq) seqObject;

                draw_object_circle(_objectSeq, _srcFrame);
                output.send(0, new ObjectToken(_srcFrame));
            }
        }
    }

    /** Initialize internal frame.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _dstFrame = null;
    }

    /** Release image.
    *  @exception IllegalActionException If thrown by the super class.
    */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_dstFrame != null) {
            _dstFrame.release();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void draw_object_circle(CvSeq objs, IplImage _image)
            throws IllegalActionException {
        int i = 0;
        int objTotal = 0;
        if (objs != null) {
            objTotal = objs.total;
        }

        for (i = 0; i < objTotal; i++) {
            Pointer r = cvGetSeqElem(objs, i);
            CvRect rect = new CvRect(r);
            CvPoint center = new CvPoint(0, 0);
            int radius;
            center.x = (int) round(rect.x + rect.width * 0.5);
            center.y = (int) round(rect.y + rect.height * 0.5);
            if (rotation != 0) {
                double xcord = (_srcFrame.width * .5) - center.x;
                double ycord = (_srcFrame.height * .5) - center.y;
                double altx = _srcFrame.width * .5;
                double alty = _srcFrame.height * .5;
                if (xcord < 0) {
                    if (ycord < 0) {
                        center.x = (int) (altx + Math
                                .abs((Math.cos(rotation) * Math.abs(xcord))));
                        center.y = (int) (alty + Math
                                .abs((Math.sin(rotation) * Math.abs(ycord))));
                    } else {
                        center.x = (int) (altx + Math
                                .abs((Math.cos(rotation) * Math.abs(xcord))));
                        center.y = (int) (alty - Math
                                .abs((Math.sin(rotation) * Math.abs(ycord))));
                    }

                } else {
                    if (ycord > 0) {
                        center.x = (int) (altx - Math
                                .abs((Math.cos(rotation) * Math.abs(xcord))));
                        center.y = (int) (alty - Math
                                .abs((Math.sin(rotation) * Math.abs(ycord))));
                    } else {
                        center.x = (int) (altx - Math
                                .abs((Math.cos(rotation) * Math.abs(xcord))));
                        center.y = (int) (alty + Math
                                .abs((Math.sin(rotation) * Math.abs(ycord))));
                    }
                }
            }

            radius = (int) round((rect.width + rect.height) * 0.25);
            cvCircle(_image, center.byValue(), radius, colors[i % 8], 3, 8, 0);

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IplImage _srcFrame;
    private IplImage _dstFrame;
    private CvSeq _objectSeq;
    private int rotation;

    private CvScalar.ByValue[] colors = { CvScalar.RED, CvScalar.BLUE,
            CvScalar.GREEN, CvScalar.CYAN, CvScalar.YELLOW, CvScalar.MAGENTA,
            CvScalar.WHITE, CvScalar.GRAY };
}
