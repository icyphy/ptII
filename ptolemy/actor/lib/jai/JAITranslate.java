/* An actor that scales a javax.media.jai.RenderedOp

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
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAITranslate
/**
   Moves the origin of an image.  Typically, images in JAI, when created,
   have as their origin, (0, 0) in the top left corner.  This actor moves
   the origin.  When displayed, there is no noticeable difference between
   an original image, and a translated image.  The difference shows up
   in operations like adding (when two images are added together, a
   region of intersection is found; this region is effected by
   translation).
   <p>

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAITranslate extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAITranslate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xShift = new Parameter(this, "xShift", new DoubleToken("0.0F"));
        yShift = new Parameter(this, "yShift", new DoubleToken("0.0F"));

        interpolationType = new StringAttribute(this, "interpolationType");
        interpolationType.setExpression("bilinear");
        _interpolationType = _BILINEAR;

        subSampleBits =
            new Parameter(this, "subSampleBits", new IntToken(8));

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The type of interpolation to use.  This is a string valued
     *  attribute that defaults to type "bilinear"
     */
    public StringAttribute interpolationType;

    /** The subsample precision.  The default value of this parameter
     *  is the integer value 8.
     */

    public Parameter subSampleBits;

    /** The shift amount in the horizontal direction.  A positive
     *  value causes the origin to be moved to the right.  A negative
     *  value causes the origin to be moved to the left.
     */
    public Parameter xShift;

    /** The shift amount in the vertical direction.  A positive
     *  value causes the origin to be moved to the down.  A negative
     *  value causes the origin to be moved to the up.
     */
    public Parameter yShift;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the translation values and the
     *  interpolation type to use.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == interpolationType) {
            String typeName = interpolationType.getExpression();
            if (typeName.equals("bicubic")) {
                _interpolationType = _BICUBIC;
            } else if (typeName.equals("bicubic2")) {
                _interpolationType = _BICUBIC2;
            } else if (typeName.equals("bilinear")) {
                _interpolationType = _BILINEAR;
            } else if (typeName.equals("nearestNeighbor")) {
                _interpolationType = _NEARESTNEIGHBOR;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized interpolation type: " + typeName);
            }
        } else if (attribute == xShift) {
            _xShift = ((DoubleToken)xShift.getToken()).doubleValue();
        } else if (attribute == yShift) {
            _yShift = ((DoubleToken)yShift.getToken()).doubleValue();
        } else if (attribute == subSampleBits) {
            _subSampleBits =
                ((IntToken)subSampleBits.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the translated RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();

        if (_debugging) {
            _debug("oldImage width " + oldImage.getWidth());
            _debug("oldImage height " + oldImage.getHeight());
            _debug("oldImage min x " + oldImage.getMinX());
            _debug("oldImage max x " + oldImage.getMaxX());
            _debug("oldImage min y " + oldImage.getMinY());
            _debug("oldImage max y " + oldImage.getMaxY());
        }

        parameters.addSource(oldImage);
        parameters.add((float)_xShift);
        parameters.add((float)_yShift);

        switch(_interpolationType) {
        case _BICUBIC:
            parameters.add(new InterpolationBicubic(_subSampleBits));
            break;
        case _BICUBIC2:
            parameters.add(new InterpolationBicubic2(_subSampleBits));
            break;
        case _BILINEAR:
            parameters.add(new InterpolationBilinear(_subSampleBits));
            break;
        case _NEARESTNEIGHBOR:
            parameters.add(new InterpolationNearest());
            break;
        default:
            throw new IllegalActionException(
                    "Invalid value for interpolationType");
        }
        RenderedOp newImage = JAI.create("translate", parameters, null);

        if (_debugging) {
            _debug("newImage width " + newImage.getWidth());
            _debug("newImage height " + newImage.getHeight());
            _debug("newImage min x " + newImage.getMinX());
            _debug("newImage max x " + newImage.getMaxX());
            _debug("newImage min y " + newImage.getMinY());
            _debug("newImage max y " + newImage.getMaxY());
        }

        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An indicator for the type of interpolation to use */
    private int _interpolationType;

    /** The subsample precision */
    private int _subSampleBits;

    /** The horizontal scaling factor. */
    private double _xShift;

    /** The vertical scaling factor. */
    private double _yShift;

    //Constants used for more efficient execution
    private static final int _BICUBIC = 0;
    private static final int _BICUBIC2 = 1;
    private static final int _BILINEAR = 2;
    private static final int _NEARESTNEIGHBOR = 3;

}
