/* Converts a matrix of doubles into a single-banded JAIImageToken.

@Copyright (c) 2003 The Regents of the University of California.
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
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_ProfileGray;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.ComponentSampleModelJAI;
import javax.media.jai.DataBufferDouble;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

//////////////////////////////////////////////////////////////////////////
//// DoubleMatrixToJAI
/**
Converts a DoubleMatrix to a JAIImageToken.  This JAIImageToken is a
single-banded grayscale image.  To assemble multiple band's into one
image, use the BandCombine operator on each image and add them 
together.  

<p> If the data was previously normalized, then the data can be rescaled
to whichever non-floating data type is chosen.

@see JAIBandCombine
@see JAIDataConvertern
@see JAIToDoubleMatrix
@author James Yeh
@version $Id$
@since Ptolemy II 3.1
*/
public class DoubleMatrixToJAI extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoubleMatrixToJAI(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        dataFormat = new StringAttribute(this, "dataFormat");
        dataFormat.setExpression("byte");
        _dataFormat = _BYTE;

        scale = new Parameter(this, "scale");
        scale.setTypeEquals(BaseType.BOOLEAN);
        scale.setToken(BooleanToken.TRUE);
        
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The type to cast the data to.  This is a string valued
     *  attribute that defaults to "byte".
     */
    public StringAttribute dataFormat;
    
    /** This parameter indicates whether to scale the data or not.
     *  This should only be checked if the data was normalized in
     *  the first place.  The default value is true.
     */
    public Parameter scale;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and determine the data type to format
     *  the data to, as well as whether to scale the data.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the base class throws it,
     *  or if the data type is not recognized.
     */
    
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == dataFormat) {
            String dataFormatName = dataFormat.getExpression();
            if (dataFormatName.equals("byte")) {
                _dataFormat = _BYTE;
            } else if (dataFormatName.equals("double")) {
                _dataFormat = _DOUBLE;
            } else if (dataFormatName.equals("float")) {
                _dataFormat = _FLOAT;
            } else if (dataFormatName.equals("int")) {
                _dataFormat = _INT;
            } else if (dataFormatName.equals("short")) {
                _dataFormat = _SHORT;
            } else if (dataFormatName.equals("ushort")) {
                _dataFormat = _USHORT;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized data type: " + dataFormatName);
            }
        } else if (attribute == scale) {
            _scale = ((BooleanToken)scale.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the JAIImageToken constructed from the matrix of doubles.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        DoubleMatrixToken doubleMatrixToken = (DoubleMatrixToken) input.get(0);
        double data[][] = doubleMatrixToken.doubleMatrix();
        int width = doubleMatrixToken.getRowCount();
        int height = doubleMatrixToken.getColumnCount();
        double newdata[] = new double[width*height];
        _maxValue = 1;
        _minValue = 0;
        if(_scale) {
            switch(_dataFormat) {
            case _BYTE:
                _maxValue = (double)Byte.MAX_VALUE - (double)Byte.MIN_VALUE;
                _minValue = 0;
                break;
            case _INT:
                _maxValue = (double)Integer.MAX_VALUE;
                _minValue = (double)Integer.MIN_VALUE;
                break;
            case _SHORT:
                _maxValue = (double)Short.MAX_VALUE;
                _minValue = (double)Short.MIN_VALUE;
                break;
            case _USHORT:
                _maxValue = (double)Short.MAX_VALUE - (double)Short.MIN_VALUE;
                _minValue = 0;
                break;
            case _FLOAT:
                _maxValue = (double)Float.MAX_VALUE;
                break;
            case _DOUBLE:
                _maxValue = (double)Double.MAX_VALUE;
                break;
            default:
                throw new InternalErrorException(this, null,
                        "Invalid value for _dataFormat private variable. "
                        + "DoubleMatrixToJAI actor (" + getFullName()
                        + ") on data type " + _dataFormat);
            }
            if (_dataFormat == _DOUBLE || _dataFormat == _FLOAT) {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        newdata[i*height + j] = data[i][j];
                        newdata[i*height + j] = newdata[i*height + j] - 0.5D;
                        newdata[i*height + j] = newdata[i*height + j]*2;
                        newdata[i*height + j] = newdata[i*height + j]*_maxValue; 
                    }
                }
            } else {
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        newdata[i*height + j] = 
                            data[i][j]*(_maxValue - _minValue) + _minValue;
                    }
                }
            }
        } else {
            // Convert the matrix of doubles into an array of doubles
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    newdata[i*height + j] = data[i][j];
                }
            }
        }
        // Create a new dataBuffer from the array of doubles
        DataBufferDouble dataBuffer =
            new DataBufferDouble(newdata, width*height);

        // The length of the bandOffset array indicates how many bands
        // there are.  Since we are just dealing with a single
        // DoubleMatrixToken, the length of this array will be one.
        // The values of the array indicate the offset to be added
        // To the bands.  This is set to 0.
        int bandOffset[] = new int[1];
        bandOffset[0] = 0;

        // Create a ComponentSampleModel, with type double, the same width
        // and height as the matrix, a pixel stride of one (the final image
        // is single-banded), and a scanline stride equal to the width.
        ComponentSampleModelJAI sampleModel =
            new ComponentSampleModelJAI(DataBuffer.TYPE_DOUBLE,
                    width, height, 1, width, bandOffset);

        // Create a new raster that has its origin at (0, 0).
        Raster raster =
            Raster.createWritableRaster(sampleModel, dataBuffer, new Point());

        // Create a grayscale colormodel.
        ComponentColorModel colorModel =
            new ComponentColorModel(
                    new ICC_ColorSpace(
                            ICC_ProfileGray.getInstance(ColorSpace.CS_GRAY)),
                    false, false,
                    ComponentColorModel.OPAQUE, DataBuffer.TYPE_DOUBLE);
        TiledImage tiledImage =
            new TiledImage(0, 0, width, height, 0, 0, sampleModel, colorModel);
        tiledImage.setData(raster);
        ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(tiledImage);
        switch(_dataFormat) {
        case _BYTE:
            parameters.add(DataBuffer.TYPE_BYTE);
            break;
        case _DOUBLE:
            parameters.add(DataBuffer.TYPE_DOUBLE);
            break;
        case _FLOAT:
            parameters.add(DataBuffer.TYPE_FLOAT);
            break;
        case _INT:
            parameters.add(DataBuffer.TYPE_INT);
            break;
        case _SHORT:
            parameters.add(DataBuffer.TYPE_SHORT);
            break;
        case _USHORT:
            parameters.add(DataBuffer.TYPE_USHORT);
            break;
        default:
            throw new InternalErrorException(this, null,
                    "Invalid value for _dataFormat private variable. "
                    + "DoubleMatrixToJAI actor (" + getFullName()
                    + ") on data type " + _dataFormat);
        }
        RenderedOp newImage = JAI.create("format", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An indicator for the data type to format to. */
    private int _dataFormat;
    
    /** Double representation of the highest value possible for the
     *  internal data type.
     */
    private double _maxValue;
    
    /** Double representation of the lowest value possible for the
     *  internal data type.
     */    
    private double _minValue;

    /** Flag determining whether or not to scale the data */
    private boolean _scale;

    // Constants used for more efficient execution.
    private static final int _BYTE = 0;
    private static final int _DOUBLE = 1;
    private static final int _FLOAT = 2;
    private static final int _INT = 3;
    private static final int _SHORT = 4;
    private static final int _USHORT = 5;
}






