/* An actor that rotates a javax.media.jai.RenderedOp around a point.

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
//// JAIRotate
/**
   Rotate an image around a given point.  The amount of rotation is in
   degrees.  If the output is displayed, the image will be displayed the
   same, regardless of the point it was rotated around.  The difference
   shows up in operations like adding (when two images are added together,
   a region of intersection is found; this region is effected by rotation).

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAIRotate extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIRotate(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xOrigin = new Parameter(this, "xOrigin", new IntToken(0));
        yOrigin = new Parameter(this, "yOrigin", new IntToken(0));

        degrees = new Parameter(this, "degrees", new DoubleToken(0.0F));

        interpolationType = new StringAttribute(this, "interpolationType");
        interpolationType.setExpression("bilinear");
        _interpolationType = _BILINEAR;

        subSampleBits =
            new Parameter(this, "subSampleBits", new IntToken(8));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of degrees to rotate.  The default value of this
     *  parameter is the double value 0.
     */
    public Parameter degrees;

    /** The type of interpolation to use.  This is a string valued
     *  attribute that defaults to type "bilinear"
     */
    public StringAttribute interpolationType;

    /** The subsample precision.  The default value of this parameter
     *  is the integer value 8.
     */
    public Parameter subSampleBits;

    /** The point to rotate around.  The default value of both these
     *  parameters is the integer value 0.
     */
    public Parameter xOrigin;
    public Parameter yOrigin;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the origin, the rotating angle
     *  and the type of interpolation to use.
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
        } else if (attribute == subSampleBits) {
            _subSampleBits =
                ((IntToken)subSampleBits.getToken()).intValue();
        } else if (attribute == xOrigin) {
            _xOrigin = ((IntToken)xOrigin.getToken()).intValue();
        } else if (attribute == yOrigin) {
            _yOrigin = ((IntToken)yOrigin.getToken()).intValue();
        } else if (attribute == degrees) {
            _degrees = ((DoubleToken)degrees.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the rotated RenderedOp.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        parameters.addSource(oldImage);

        if (_debugging) {
            _debug("oldimage width " + oldImage.getWidth());
            _debug("oldimage height " + oldImage.getHeight());
            _debug("oldimage min x " + oldImage.getMinX());
            _debug("oldimage max x " + oldImage.getMaxX());
            _debug("oldimage min y " + oldImage.getMinY());
            _debug("oldimage max y " + oldImage.getMaxY());
        }

        parameters.add((float)_xOrigin);
        parameters.add((float)_yOrigin);
        float angle = (float)(_degrees * (Math.PI/180.0F));
        parameters.add(angle);
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
        RenderedOp newImage = JAI.create("Rotate", parameters);
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

    /** The number of degrees to rotate */
    private double _degrees;

    /** An indicator for the type of interpolation to use */
    private int _interpolationType;

    /** The subsample precision */
    private int _subSampleBits;

    /** The x value of the origin */
    private int _xOrigin;

    /** The y value of the origin */
    private int _yOrigin;

    //Constants used for more efficient execution
    private static final int _BICUBIC = 0;
    private static final int _BICUBIC2 = 1;
    private static final int _BILINEAR = 2;
    private static final int _NEARESTNEIGHBOR = 3;
}
