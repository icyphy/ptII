/* An actor that computes an affine transformation on a RenderedOp.

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
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import java.awt.geom.AffineTransform;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIAffineTransform
/**
Compute an affine transformation on an image.  The parameter
affineMatrix must be a two by three matrix.  If the matrix is
<pre>
 a b c
 d e f
</pre>
the input (x', y') and output (x, y) is determined by the following two
equations.
<pre>
 x = a*x' + b*y' + c;
 y = d*x' + e*y' + f;
</pre>
Note that the c and f terms do not show up when the output is directly
displayed.  The c and f terms are translation terms, and only show up
when operated on with another image.

@author James Yeh
@version $Id$
@since Ptolemy II 3.0
*/
public class JAIAffineTransform extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIAffineTransform(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        affineMatrix =
            new Parameter(this, "affineMatrix",
                    new DoubleMatrixToken(_initialMatrix));

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

    /** The affine transformation matrix.  This is a doubleMatrixToken
     * valued parameter that defaults to
     * 1 0 0
     * 0 1 0
     */
    public Parameter affineMatrix;

    /** The type of interpolation to use.  This is a string valued
     *  attribute that defaults to type "bilinear"
     */
    public StringAttribute interpolationType;

    /** The subsample precision.  The default value of this parameter
     *  is the integer value 8.
     */
    public Parameter subSampleBits;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the factors.
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
                ((IntToken) subSampleBits.getToken()).intValue();
        } else if (attribute == affineMatrix) {
            DoubleMatrixToken affineMatrixToken =
                (DoubleMatrixToken) affineMatrix.getToken();
            if (affineMatrixToken.getColumnCount() == 3
                    && affineMatrixToken.getRowCount() == 2) {
                _matrixValue = affineMatrixToken.doubleMatrix();
                _affineTransform =
                    new AffineTransform(_matrixValue[0][0],
                            _matrixValue[1][0], _matrixValue[0][1],
                            _matrixValue[1][1], _matrixValue[0][2],
                            _matrixValue[1][2]);
            } else {
                throw new IllegalActionException(this,
                        "Matrix must have two rows and three "
                        + "columns");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }


    /** Fire this actor.
     *  Output the affine transformed RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();

        switch (_interpolationType) {
        case _BICUBIC:
            _interpolation = new InterpolationBicubic(_subSampleBits);
            break;
        case _BICUBIC2:
            _interpolation = new InterpolationBicubic2(_subSampleBits);
            break;
        case _BILINEAR:
            _interpolation = new InterpolationBilinear(_subSampleBits);
            break;
        case _NEARESTNEIGHBOR:
            _interpolation = new InterpolationNearest();
            break;
        default:
            throw new IllegalActionException(
                    "Invalid value for interpolationType");
        }
        RenderedOp newImage =
            JAI.create("affine", oldImage,
                    _affineTransform, _interpolation);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private AffineTransform _affineTransform;

    private double[][] _initialMatrix =
    {{1.0F, 0.0F, 0.0F}, {0.0F, 1.0F, 0.0F}};

    private Interpolation _interpolation;

    /** An indicator for the type of interpolation to use */
    private int _interpolationType;

    private double[][] _matrixValue;

    /** The subsample precision */
    private int _subSampleBits;

    //Constants used for more efficient execution
    private static final int _BICUBIC = 0;
    private static final int _BICUBIC2 = 1;
    private static final int _BILINEAR = 2;
    private static final int _NEARESTNEIGHBOR = 3;
}
