/* An actor that reads in a java.awt.Image and rotates it a certain number of
 degrees

 @Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.image;

import java.awt.Image;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ImageToken;
import ptolemy.data.IntToken;
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
public class ImageRotate extends Transformer {
    /** Construct an actor with the given container and name.
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
        rotationInDegrees = new Parameter(this, "rotationInDegrees",
                new IntToken(90));
        rotationInDegrees.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount of of rotation in degrees.
     *  This parameter contains an IntegerToken, initially with value 90.
     */
    public Parameter rotationInDegrees;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read one java.awt.Image from each channel and rotate each Image
     *  the number of degrees indicated by the rotationInDegrees parameter.
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int width = input.getWidth();
        int rotation = ((IntToken) rotationInDegrees.getToken()).intValue();

        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                ImageToken imageToken = (ImageToken) input.get(i);
                Image image = imageToken.asAWTImage();
                Image rotatedImage = Transform.rotate(image, rotation);
                output.broadcast(new AWTImageToken(rotatedImage));
            }
        }
    }
}
