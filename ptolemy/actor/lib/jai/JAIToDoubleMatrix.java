/* Takes a JAIImageToken, and outputs the data as a DoubleMatrixToken.

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
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.lang.Byte;
import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Short;

import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIToDoubleMatrix
/**
   This actor takes a single banded image and outputs a DoubleMatrixToken
   containing the data.

   To convert multiple banded images (for instance, color images or the
   output of a Discrete Fourier Transform), use either the BandSelect or
   BandCombine actors to seperate the bands.

   The normalize parameter allows non floating point data types to be
   normalized when the token is converted.  The normalization that
   occurs is a mapping between the lowest and highest value's of the
   data type into the double values of 0 and 1.

   @see DoubleMatrixToJAI
   @see JAIBandSelect
   @see JAIBandCombine
   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAIToDoubleMatrix extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIToDoubleMatrix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        normalize = new Parameter(this, "normalize");
        normalize.setTypeEquals(BaseType.BOOLEAN);
        normalize.setToken(BooleanToken.TRUE);

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This parameter indicates whether to normalize or not.  This
     *  only should be checked for non-floating point data-types.
     *  The default value is true.
     */
    public Parameter normalize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and determine whether the user wants to
     *  normalize the data.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the base class throws it.
     */

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == normalize) {
            _normalize = ((BooleanToken)normalize.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the data contained inside the image as a DoubleMatrixToken.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp jaiImage = jaiImageToken.getValue();
        int height = jaiImage.getHeight();
        int width = jaiImage.getWidth();
        Raster raster = jaiImage.getData();
        DataBuffer dataBuffer = raster.getDataBuffer();
        double data[][] = new double[width][height];
        _type = dataBuffer.getDataType();
        if (_normalize) {
            switch(_type) {
            case DataBuffer.TYPE_BYTE:
                _maxValue = (double)Byte.MAX_VALUE - (double)Byte.MIN_VALUE;
                _minValue = 0;
                break;
            case DataBuffer.TYPE_INT:
                _maxValue = (double)Integer.MAX_VALUE;
                _minValue = (double)Integer.MIN_VALUE;
                break;
            case DataBuffer.TYPE_SHORT:
                _maxValue = (double)Short.MAX_VALUE;
                _minValue = (double)Short.MIN_VALUE;
                break;
            case DataBuffer.TYPE_USHORT:
                _maxValue = (double)Short.MAX_VALUE - (double)Short.MIN_VALUE;
                _minValue = 0;
                break;
            case DataBuffer.TYPE_FLOAT:
                _maxValue = (double)Float.MAX_VALUE;
                break;
            case DataBuffer.TYPE_DOUBLE:
                _maxValue = (double)Double.MAX_VALUE;
                break;
            default:
                throw new IllegalActionException("Data type not suitable for "
                        + "normalizing");
            }
            if (_debugging) {
                _debug("max value is " + _maxValue);
                _debug("min value is " + _minValue);
            }
            if (_type == DataBuffer.TYPE_DOUBLE ||
                    _type == DataBuffer.TYPE_FLOAT) {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        data[i][j] = dataBuffer.getElemDouble(i*height + j);
                        data[i][j] = data[i][j]/_maxValue;
                        data[i][j] = data[i][j]/2;
                        data[i][j] = data[i][j] + 0.5D;
                    }
                }
            } else {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        data[i][j] =
                            (dataBuffer.getElemDouble(i*height + j) -
                                    _minValue)/
                            (_maxValue - _minValue);
                    }
                }
            }
        } else {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    data[i][j] = dataBuffer.getElemDouble(i*height + j);
                }
            }
        }
        DoubleMatrixToken matrixToken = new DoubleMatrixToken(data);
        output.send(0, new DoubleMatrixToken(data));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Double representation of the highest value possible for the
     *  internal data type.
     */
    private double _maxValue;

    /** Double representation of the lowest value possible for the
     *  internal data type.
     */
    private double _minValue;

    /** Flag determining whether or not to normalize. */
    private boolean _normalize;

    /** Type determinator for the internal data. */
    private int _type;
}




