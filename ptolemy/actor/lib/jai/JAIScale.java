/* An actor that scales a javax.media.jai.RenderedOp

 @Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jai;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// JAIScale

/**
 Scale a RenderedOp using the javax.media.jai.JAI class.

 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JAIScale extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIScale(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        specifySize = new Parameter(this, "specifySize");
        specifySize.setTypeEquals(BaseType.BOOLEAN);
        specifySize.setToken(BooleanToken.TRUE);

        width = new Parameter(this, "width", new IntToken(800));
        height = new Parameter(this, "height", new IntToken(600));

        xScaleFactor = new Parameter(this, "xScaleFactor", new DoubleToken(
                "1.0F"));
        yScaleFactor = new Parameter(this, "yScaleFactor", new DoubleToken(
                "1.0F"));

        interpolationType = new StringAttribute(this, "interpolationType");
        interpolationType.setExpression("bilinear");
        _interpolationType = _BILINEAR;

        subSampleBits = new Parameter(this, "subSampleBits", new IntToken(8));

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The type of interpolation to use.  This is a string valued
     *  attribute that defaults to type "bilinear"
     */
    public StringAttribute interpolationType;

    /** If <i>true</i> (the default), then the image will be scaled to
     *  the dimensions provided in width and height.  If <i>false</i>,
     *  then the width and height of the image will be scaled by the
     *  respective amounts provided in xScaleFactor and yScaleFactor.
     */
    public Parameter specifySize;

    /** The subsample precision.  The default value of this parameter
     *  is the integer value 8.
     */
    public Parameter subSampleBits;

    /** The scaling factor in the horizontal direction.  The default
     *  value of this parameter is the double value 1.0
     */
    public Parameter xScaleFactor;

    /** The scaling factor in the vertical direction.  The default
     *  value of this parameter is the double value 1.0
     */
    public Parameter yScaleFactor;

    /** The desired width in pixels.  The default value of this
     *  parameter is the integer 800.
     */
    public Parameter width;

    /** The desired height in pixels.  The default value of this
     *  parameter is the integer 600.
     */
    public Parameter height;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the scaling factors.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
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
        } else if (attribute == xScaleFactor) {
            _xScaleFactor = ((DoubleToken) xScaleFactor.getToken())
                    .doubleValue();
        } else if (attribute == yScaleFactor) {
            _yScaleFactor = ((DoubleToken) yScaleFactor.getToken())
                    .doubleValue();
        } else if (attribute == width) {
            _width = ((IntToken) width.getToken()).intValue();
        } else if (attribute == height) {
            _height = ((IntToken) height.getToken()).intValue();
        } else if (attribute == specifySize) {
            _specifySize = ((BooleanToken) specifySize.getToken())
                    .booleanValue();
        } else if (attribute == subSampleBits) {
            _subSampleBits = ((IntToken) subSampleBits.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the scaled RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        parameters.addSource(oldImage);

        if (_specifySize) {
            int oldImageWidth = oldImage.getWidth();
            int oldImageHeight = oldImage.getHeight();
            parameters.add((float) _width / (float) oldImageWidth);
            parameters.add((float) _height / (float) oldImageHeight);
        } else {
            parameters.add((float) _xScaleFactor);
            parameters.add((float) _yScaleFactor);
        }

        parameters.add(0.0F);
        parameters.add(0.0F);

        switch (_interpolationType) {
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

        RenderedOp newImage = JAI.create("scale", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag determining whether to scale by double values, or scale to
     *  a specified width and height.
     */
    private boolean _specifySize;

    /** The subsample precision */
    private int _subSampleBits;

    /** The horizontal scaling factor. */
    private double _xScaleFactor;

    /** The vertical scaling factor. */
    private double _yScaleFactor;

    /** An indicator for the type of interpolation to use */
    private int _interpolationType;

    //Constants used for more efficient execution
    private static final int _BICUBIC = 0;

    private static final int _BICUBIC2 = 1;

    private static final int _BILINEAR = 2;

    private static final int _NEARESTNEIGHBOR = 3;

    /** The desired width in pixels. */
    private int _width;

    /** The desired height in pixels. */
    private int _height;

    /** The type of Interpolation being used is specified using this
     *  variable.
     */

    //private Interpolation _interpolation;
}
