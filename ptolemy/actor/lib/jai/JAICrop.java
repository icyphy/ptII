/* An actor that crops an image.

@Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAICrop
/**
   Crop an image, given a point of origin, and the dimensions to crop. In
   most images (those that have not been translated or transformed under
   similar operations) have their origin, (0, 0), at the top left corner.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAICrop extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAICrop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xOrigin = new Parameter(this, "xOrigin", new IntToken(0));
        yOrigin = new Parameter(this, "yOrigin", new IntToken(0));

        width = new Parameter(this, "width", new IntToken(0));
        height = new Parameter(this, "height", new IntToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The vertical distance from the origin.  A positive value
     *  indicates that the final image will start downwards from the
     *  origin.  A negative value indicates that the final image will
     *  start upwards from the origin.
     */
    public Parameter height;

    /** The horizontal distance from the origin.  A positive value
     *  indicates that the final image will start to the right of the
     *  origin.  A negative value indicates that the final image will
     *  start to the left of the origin.
     */
    public Parameter width;

    /** The point of origin for cropping.  The default value of both
     *  these parameters is the integer value 0.
     */
    public Parameter xOrigin;
    public Parameter yOrigin;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the origin, the width, and the
     *  height of the cropped image.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == xOrigin) {
            _xOrigin = ((IntToken)xOrigin.getToken()).intValue();
        } else if (attribute == yOrigin) {
            _yOrigin = ((IntToken)yOrigin.getToken()).intValue();
        } else if (attribute == width) {
            _width = ((IntToken)width.getToken()).intValue();
        } else if (attribute == height) {
            _height = ((IntToken)height.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the cropped RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();

        parameters.addSource(oldImage);
        parameters.add((float)_xOrigin);
        parameters.add((float)_yOrigin);
        parameters.add((float)_width);
        parameters.add((float)_height);
        RenderedOp newImage = JAI.create("Crop", parameters);
        int width = newImage.getWidth();
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The height of the cropped image*/
    private int _height;

    /** The width of the cropped image*/
    private int _width;

    /** The x value of the origin */
    private int _xOrigin;

    /** The y value of the origin */
    private int _yOrigin;
}
