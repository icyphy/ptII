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

import static ptolemy.actor.lib.opencv.jna.cv.CvLib.CV_GAUSSIAN;
import static ptolemy.actor.lib.opencv.jna.cv.CvLib.cvSmooth;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// ImageSmooth

/**
 * Smooth an image using the Open Computer Vision (OpenCV) Library.
 * @author Tatsuaki Iwata, Edward A. Lee, Jan Reineke, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ImageSmooth extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageSmooth(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        size1Param = new Parameter(this, "size1", new IntToken(3));
        size2Param = new Parameter(this, "size2", new IntToken(3));
        size1Param.setTypeEquals(BaseType.INT);
        size2Param.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount of of rotation in degrees.
     *  This parameter contains an IntegerToken, initially with value 90.
     */
    public Parameter size1Param;
    public Parameter size2Param;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    @Override
    public void fire() throws IllegalActionException {
        int size1 = ((IntToken) size1Param.getToken()).intValue();
        int size2 = ((IntToken) size2Param.getToken()).intValue();

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken) input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof Pointer)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of IplImage. Got "
                                + inputObject.getClass());
            }
            _frame = (Pointer) inputObject;
            cvSmooth(_frame, _frame, CV_GAUSSIAN, size1, size2, 0, 0);
            output.send(0, new ObjectToken(_frame));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Pointer _frame;
}
