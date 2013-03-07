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

import static name.audet.samuel.javacv.jna.cv.cvSobel;
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
//// ImageSobel

/**
 * Apply sovel filter to image
  * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ImageSobel extends Transformer {
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
    public ImageSobel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        xorderParam = new Parameter(this, "xorder", new IntToken(1));
        yorderParam = new Parameter(this, "yorder", new IntToken(1));
        apertureSizeParam = new Parameter(this,
                "aperture size (must be 1, 3, 5 or 7)", new IntToken(3));
        xorderParam.setTypeEquals(BaseType.INT);
        yorderParam.setTypeEquals(BaseType.INT);
        apertureSizeParam.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The parameters for sobel filter.
     */
    public Parameter xorderParam;
    public Parameter yorderParam;
    public Parameter apertureSizeParam;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    public void fire() throws IllegalActionException {
        int xorder = ((IntToken) (xorderParam.getToken())).intValue();
        int yorder = ((IntToken) (yorderParam.getToken())).intValue();
        int apertureSize = ((IntToken) (apertureSizeParam.getToken()))
                .intValue();
        if (!(apertureSize == 1 || apertureSize == 3 || apertureSize == 5 || apertureSize == 7)) {
            throw new IllegalActionException(this,
                    "apertureSize must be 1, 3, 5, or 7.");
        }

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof IplImage)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            _frame = (IplImage) inputObject;
            //_frame = cvCloneImage((IplImage)inputObject);
            cvSobel(_frame, _frame, xorder, yorder, apertureSize);
            output.send(0, new ObjectToken(_frame));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IplImage _frame;
}
