/* An actor that capture image from camera by using OpenCV

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

import static ptolemy.actor.lib.opencv.jna.cxcore.CxcoreLib.cvReleaseImage;
import static ptolemy.actor.lib.opencv.jna.highgui.HighguiLib.cvLoadImage;
import ptolemy.actor.lib.Source;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

///////////////////////////////////////////////////////////////////
//// ImageReader

/**
 * Read an image using the Open Computer Vision (OpenCV) Library.
 * @author Tatsuaki Iwata, Edward A. Lee, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ImageReader extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        pathName = new StringAttribute(this, "pathName");
        pathName.setExpression("test.png");

        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** The name of the file to write to. The default
     *  value of this parameter is "test.png"
     */
    public StringAttribute pathName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    @Override
    public void fire() throws IllegalActionException {
        output.send(0, new ObjectToken(_image));
    }

    /** Load image from file
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        String pathNameString = pathName.getExpression();
        if (_image == null) {
            _image = cvLoadImage(pathNameString, 1);
            if (_image == null) {
                throw new IllegalActionException(this, "Fail to load image "
                        + pathNameString);
            }

        }
    }

    /** Stop the capture.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        cvReleaseImage(new PointerByReference(_image));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Pointer _image;
}
