/* An actor that writes to a PNG file.

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.PNGEncodeParam;

//////////////////////////////////////////////////////////////////////////
//// JAIPNGWriter
/**
Write a javax.media.jai.RenderedOp to a specified PNG file.

<p>The file is specified by the <i>fileName</i> attribute
using any form acceptable to FileAttribute.

<p>If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
then this actor will overwrite the specified file if it exists without
asking.  If <i>true</i> (the default), then if the file exists, then
this actor will ask for confirmation before overwriting.

@see ptolemy.kernel.attributes.FileAttribute
@author James Yeh, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/

public class JAIPNGWriter extends JAIWriter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIPNGWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        adam7Interlacing = new Parameter(this, "adam7Interlacing");
        adam7Interlacing.setTypeEquals(BaseType.BOOLEAN);
        adam7Interlacing.setToken(BooleanToken.TRUE);

        bitDepth = new Parameter(this, "bitDepth", new IntToken(8));

        fileName.setExpression("file.png");

        setGamma = new Parameter(this, "setGamma");
        setGamma.setTypeEquals(BaseType.BOOLEAN);
        setGamma.setToken(BooleanToken.FALSE);

        gamma = new Parameter(this, "gamma", new DoubleToken(0.455F));

        setBackground = new Parameter(this, "setBackground");
        setBackground.setTypeEquals(BaseType.BOOLEAN);
        setBackground.setToken(BooleanToken.FALSE);

        background = new Parameter(this, "background",
                new ArrayToken(_initialArray));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If the Adam7 Interlacing option is false, pixels are stored
     *  left to right and from top to bottom. If it is true (the
     *  default), seven distinct passes are made over the image, each
     *  transmitting a subset of the pixels.
     */
    public Parameter adam7Interlacing;

    /** If the setBackground parameter is false, then this parameter
     *  is ignored.  If it is true, this parameter sets the
     *  background color of the image.  Note that this will only show
     *  up in images with transparency.  If the image is a grayscale
     *  image, only the first value of the array is used.  The value
     *  is an integer that should range from 0 to (2^bitdepth - 1).
     *  The bitdepth is defined by the bitDepth parameter in this
     *  actor.  If the image is an RGB image, then the first 3 values
     *  of the array are read.  Each of these also range from
     *  0 to (2^bitdepth - 1).
     */
    public Parameter background;

    public Parameter bitDepth;

    /** If <i>false</i>, then overwrite the specified file if it exists
     *  without asking.  If <i>true</i> (the default), then if the file
     *  exists, ask for confirmation before overwriting.
     */
    public Parameter confirmOverwrite;

    public Parameter gamma;

    public Parameter setBackground;

    public Parameter setGamma;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and attempt set either the fileName,
     *  whether to overwrite an existing file or not, or how the data
     *  will be written.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // We use attributeChanged here to avoid code duplication
        // in postfire().
        if (attribute == adam7Interlacing) {
            _adam7Interlacing =
                ((BooleanToken)adam7Interlacing.getToken()).booleanValue();
        } else if (attribute == setGamma) {
            _setGamma = ((BooleanToken)setGamma.getToken()).booleanValue();
        } else if (attribute == gamma) {
            _gamma = ((DoubleToken)gamma.getToken()).doubleValue();
        } else if (attribute == bitDepth) {
            _bitDepth = ((IntToken)bitDepth.getToken()).intValue();
        } else if (attribute == setBackground) {
            _setBackground =
                ((BooleanToken)setBackground.getToken()).booleanValue();
        } else if (attribute == background) {
            Token data[] = ((ArrayToken)background.getToken()).arrayValue();
            for (int i = 0; i < data.length; i++) {
                _valueArray = new int[data.length];
                _valueArray[i] = ((IntToken)(data[i])).intValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read an input JAIImageToken and write it to the file.
     *  If the file does not exist then create it.  If the file
     *  already exists, then query the user for overwrite.
     *  @exception IllegalActionException If the file cannot be opened
     *  or created, if the user refuses to overwrite an existing file,
     *  of if the image in unable to be encoded.
     *  @return True if the execution can continue.
     */
    public boolean postfire() throws IllegalActionException {
        // This class is different from most classes derived from
        // JAIWriter in that it reads the input in the derived class
        // and changes the output format depending on the input.
        // This makes the parent class less clean.
        if (input.hasToken(0)) {
            _jaiImageToken = (JAIImageToken) input.get(0);
            _alreadyReadImageToken = true;
            _image = _jaiImageToken.getValue();

            _imageEncoderName = "PNG";

            PNGEncodeParam parameters =
                PNGEncodeParam.getDefaultEncodeParam(_image);
            if (parameters instanceof PNGEncodeParam.Gray) {
                PNGEncodeParam.Gray parametersGray = new PNGEncodeParam.Gray();
                parametersGray.setBitDepth(_bitDepth);
                parametersGray.setInterlacing(_adam7Interlacing);
                if (_setGamma) {
                    parametersGray.setGamma((float)_gamma);
                }
                if (_setBackground) {
                    if (_valueArray.length < 1) {
                        throw new IllegalActionException("Need "
                                + "one value to set Transparency");
                    } else {
                        parametersGray.setBackgroundGray(_valueArray[0]);
                    }
                }
                _imageEncodeParam = parametersGray;
            } else if (parameters instanceof PNGEncodeParam.RGB) {
                PNGEncodeParam.RGB parametersRGB = new PNGEncodeParam.RGB();
                parametersRGB.setBitDepth(_bitDepth);
                parametersRGB.setInterlacing(_adam7Interlacing);
                if (_setGamma) {
                    parametersRGB.setGamma((float)_gamma);
                }
                if (_setBackground) {
                    if (_valueArray.length < 3) {
                        throw new IllegalActionException("Need "
                                + "three values to set transparency");
                    } else {
                        int RGBvalues[] = new int[3];
                        for (int i = 0; i < 3; i++) {
                            RGBvalues[i] = _valueArray[i];
                        }
                        parametersRGB.setBackgroundRGB(RGBvalues);
                    }
                }
                _imageEncodeParam = parametersRGB;
            }
        }
        return super.postfire();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _bitDepth;

    /** The value of the confirmOverwrite parameter. */
    private boolean _confirmOverwriteValue;

    /** The File to be saved to. */
    private File _file;

    /** The above file as a String. */
    private String _fileRoot;

    /** The value of the storeTopDown parameter. */
    private boolean _adam7Interlacing;

    /** The FileOutputStream for file writing. */
    private FileOutputStream _stream;

    private boolean _setGamma;

    private double _gamma;

    private boolean _setBackground;

    private IntToken _initialArray[] = {new IntToken(0),
                                        new IntToken(0),
                                        new IntToken(0)};

    private int _valueArray[];
}









