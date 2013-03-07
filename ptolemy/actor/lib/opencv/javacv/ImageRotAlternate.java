/* An actor that reads in a java.awt.Image and rotates it a certain number of
 degrees

 @Copyright (c) 2001-2013 The Regents of the University of California.
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
package ptolemy.actor.lib.opencv.javacv;

import static name.audet.samuel.javacv.jna.cv.CV_INTER_LINEAR;
import static name.audet.samuel.javacv.jna.cv.CV_WARP_FILL_OUTLIERS;
import static name.audet.samuel.javacv.jna.cv.cv2DRotationMatrix;
import static name.audet.samuel.javacv.jna.cv.cvWarpAffine;
import static name.audet.samuel.javacv.jna.cxcore.CV_32FC1;
import static name.audet.samuel.javacv.jna.cxcore.cvCloneImage;
import static name.audet.samuel.javacv.jna.cxcore.cvCreateMat;
import static name.audet.samuel.javacv.jna.cxcore.cvScalarAll;
import name.audet.samuel.javacv.jna.cxcore.CvMat;
import name.audet.samuel.javacv.jna.cxcore.CvPoint2D32f;
import name.audet.samuel.javacv.jna.cxcore.CvScalar;
import name.audet.samuel.javacv.jna.cxcore.IplImage;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageRotate

/**
 This actor reads an ObjectToken that is a java.awt.Image from the input,
 rotates it a certain number of degrees and writes the resulting
 image to the output port as an ObjectToken that is a java.awt.Image.

 @author  Christopher Hylands
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ImageRotAlternate extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageRotAlternate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        input1 = new TypedIOPort(this, "input1", true, false);
        input1.setTypeEquals(BaseType.INT);
        output.setTypeEquals(BaseType.OBJECT);
        rotationInDegrees = new Parameter(this, "rotationInDegrees",
                new IntToken(90));
        rotationInDegrees.setTypeEquals(BaseType.INT);
        scaleParam = new Parameter(this, "scale", new DoubleToken(1.0));
        scaleParam.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount of of rotation in degrees.
     *  This parameter contains an IntegerToken, initially with value 90.
     */
    public Parameter rotationInDegrees;

    public TypedIOPort input1;

    public Parameter scaleParam;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one java.awt.Image from each channel and rotate each Image
     *  the number of degrees indicated by the rotationInDegrees parameter.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        double scale = ((DoubleToken) (scaleParam.getToken())).doubleValue();
        int rotation = ((IntToken) (rotationInDegrees.getToken())).intValue();

        if (input1.hasToken(0)) {
            rotation = ((IntToken) input1.get(0)).intValue();
        }
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);

            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof IplImage)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }

            IplImage my_image = (IplImage) inputObject;
            IplImage my_dest = cvCloneImage(my_image);

            CvPoint2D32f my_center = new CvPoint2D32f(my_image.width / 2,
                    my_image.height / 2);
            int flags = CV_INTER_LINEAR + CV_WARP_FILL_OUTLIERS;
            CvScalar fillval = cvScalarAll(0);
            CvMat map_matrix = cvCreateMat(2, 3, CV_32FC1);
            cv2DRotationMatrix(my_center.byValue(), rotation, scale, map_matrix);
            cvWarpAffine(my_image, my_dest, map_matrix, flags,
                    fillval.byValue());
            output.send(0, new ObjectToken(my_dest));

        }

    }
}
