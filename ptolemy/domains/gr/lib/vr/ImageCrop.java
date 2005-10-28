/* An actor that crops images.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.gr.lib.vr;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ImageCrop

/**
 Describe your class here, in complete sentences.
 What does it do?  What is its intended use?

 @author Tiffany Crawford
 @version $Id$
 @see classname (refer to relevant classes, but not the base class)
 @since Ptolemy II x.x
 @Pt.ProposedRating Red (yourname)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class ImageCrop extends TypedAtomicActor {
    /** Create an instance with ... (describe the properties of the
     *  instance). Use the imperative case here.
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public ImageCrop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        imageInput = new TypedIOPort(this, "imageInput");
        imageInput.setInput(true);
        imageInput.setTypeEquals(BaseType.OBJECT);

        roi = new TypedIOPort(this, "roi");
        roi.setInput(true);
        roi.setTypeEquals(BaseType.OBJECT);

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.OBJECT);

        stack = new Parameter(this, "stack");
        stack.setExpression("true");
        stack.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Desription of the variable. */
    public TypedIOPort imageInput;

    public TypedIOPort roi;

    public TypedIOPort output;

    public Parameter stack;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do something... (Use the imperative case here, such as:
     *  "Return the most recently recorded event.", not
     *  "Returns the most recently recorded event."
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public void fire() throws IllegalActionException {
        super.fire();
        //Get input values from respective tokens
        ObjectToken imageToken = (ObjectToken) imageInput.get(0);
        _imagePlus = (ImagePlus) imageToken.getValue();

        // FIXME: why read this token and discard it?
        /*(ObjectToken roiToken = (ObjectToken) */roi.get(0);
        //Roi roi = (Roi) roiToken.getValue();

        //Do cropping
        ImageProcessor imageProcessor = _imagePlus.getProcessor();
        imageProcessor.setRoi(_roi);

        ImageProcessor croppedProcessor = imageProcessor.crop();
        _croppedImage = new ImagePlus("Cropped Image", croppedProcessor);
        output.broadcast(new ObjectToken(_croppedImage));
    }

    public boolean prefire() throws IllegalActionException {
        //Check for proper inputs and if available return true
        if (imageInput.hasToken(0) && roi.hasToken(0)) {
            return true;
        } else {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ImagePlus _croppedImage;

    private ImagePlus _imagePlus;

    private Roi _roi;
}
