/* Convert an ImageToken into a JMFImageToken.

 @Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jmf;

import java.awt.Image;

import javax.media.Buffer;
import javax.media.util.ImageToBuffer;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ImageToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageToJMF

/**
 Convert an ImageToken into a JMFImageToken.

 @author Christopher Hylands Brooks
 @version $Id$
 @since Ptolemy II 3.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ImageToJMF extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageToJMF(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        frameRate = new Parameter(this, "frameRate");
        frameRate.setExpression("15");

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The frame rate in frames per second at which the images are
     *  being generated.  If video is being generated, then the frame
     *  rate is usually between 1 and 60.  The initial default is
     *  an integer with value 15, meaning 15 frames per second
     */
    public Parameter frameRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and determine the data type to format
     *  the data to, as well as whether to scale the data.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the base class throws it,
     *  or if the data type is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == frameRate) {
            _frameRate = ((IntToken) frameRate.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the JAIImageToken constructed from the matrix of doubles.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (input.hasToken(0)) {
            ImageToken imageToken = (ImageToken) input.get(0);
            Image image = imageToken.asAWTImage();
            Buffer buffer = ImageToBuffer.createBuffer(image, _frameRate);
            output.send(0, new JMFImageToken(buffer));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The frame rate in frames per second.
    private int _frameRate;
}
