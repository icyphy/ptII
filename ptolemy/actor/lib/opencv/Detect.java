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

package ptolemy.actor.lib.opencv;

import hypermedia.video.OpenCV;

import java.awt.Rectangle;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Detect

/**
 * An actor that uses OpenCV to detect objects in a video stream.
 * The input is an OpenCV object wrapped in an ObjectToken.
 * The output is an array of arrays of ints.
 * @author Dorsa Sadigh, Steve Bako, Edward A. Lee, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Detect extends Transformer {
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
    public Detect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: create a separate type for OpenCV.
        input.setTypeEquals(BaseType.OBJECT);
        ArrayType rectangleType = new ArrayType(BaseType.INT, 4);
        ArrayType arrayOfRectanglesType = new ArrayType(rectangleType);
        output.setTypeEquals(arrayOfRectanglesType);
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
            if (!(inputObject instanceof OpenCV)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of OpenCV. Got "
                                + inputObject.getClass());
            }
            OpenCV openCV = (OpenCV) inputObject;
            if (_openCV != openCV) {
                _openCV = openCV;
                _openCV.cascade(OpenCV.CASCADE_FRONTALFACE_ALT);
            }
            Rectangle[] rectangles = _openCV.detect();
            ArrayToken[] results = new ArrayToken[rectangles.length];
            for (int i = 0; i < rectangles.length; i++) {
                IntToken[] rectangle = new IntToken[4];
                rectangle[0] = new IntToken(rectangles[i].x);
                rectangle[1] = new IntToken(rectangles[i].y);
                rectangle[2] = new IntToken(rectangles[i].width);
                rectangle[3] = new IntToken(rectangles[i].height);

                results[i] = new ArrayToken(rectangle);
            }
            if (results.length > 0) {
                ArrayToken result = new ArrayToken(results);
                output.send(0, result);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The OpenCV object from which we read. */
    private OpenCV _openCV;
}
