/* An actor that changes an image to be a grayscaled version of itself

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

package ptolemy.actor.lib.opencv.optimize;

import hypermedia.video.OpenCV;
import processing.core.PImage;
import ptolemy.actor.lib.opencv.OpenCVImageObject;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.optimize.SharedBufferTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////GrayScale

/**
 * Produce a grayscaled version of an image.
 * @author Marc Geilen, based on code by Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class GrayScale extends SharedBufferTransformer {

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
    public GrayScale(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Output an OpenCV Object.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    @Override
    protected void _fireCopying() throws IllegalActionException {
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof OpenCVImageObject)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of OpenCVImageObject. Got "
                                + inputObject.getClass());
            }
            OpenCVImageObject oio = (OpenCVImageObject) inputObject;
            OpenCV openCV = oio.openCV;
            openCV.copy(oio.img, 0, 0, oio.img.width, oio.img.height, 0, 0,
                    oio.img.width, oio.img.height);
            openCV.convert(OpenCV.GRAY);
            PImage buf = openCV.image(OpenCV.BUFFER);
            PImage newImg = new PImage(buf.width, buf.height, buf.format);
            newImg.copy(buf, 0, 0, buf.width, buf.height, 0, 0, buf.width,
                    buf.height);
            // restore mode to RGB
            openCV.convert(OpenCV.RGB);
            OpenCVImageObject noio = new OpenCVImageObject(openCV, newImg);
            output.send(0, new ObjectToken(noio));
        }
    }

    @Override
    protected void _fireExclusive() throws IllegalActionException {
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof OpenCVImageObject)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of OpenCVImageObject. Got "
                                + inputObject.getClass());
            }

            OpenCVImageObject oio = (OpenCVImageObject) inputObject;
            OpenCV openCV = oio.openCV;
            openCV.copy(oio.img, 0, 0, oio.img.width, oio.img.height, 0, 0,
                    oio.img.width, oio.img.height);
            openCV.convert(OpenCV.GRAY);
            PImage buf = openCV.image(OpenCV.BUFFER);
            oio.img.copy(buf, 0, 0, buf.width, buf.height, 0, 0, buf.width,
                    buf.height);
            // restore mode to RGB
            openCV.convert(OpenCV.RGB);
            output.send(0, new ObjectToken(oio));
        }
    }
}
