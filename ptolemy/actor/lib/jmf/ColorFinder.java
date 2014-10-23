/* An actor that looks for a color in a Buffer.

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
package ptolemy.actor.lib.jmf;

import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ColorFinder

/**
 An actor that searches for a color in a Buffer.

 @author Paul Yang, David Lee, James Yeh
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ColorFinder extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ColorFinder(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        outputX = new TypedIOPort(this, "outputX", false, true);
        outputY = new TypedIOPort(this, "outputY", false, true);
        input.setTypeEquals(BaseType.OBJECT);
        outputX.setTypeEquals(BaseType.DOUBLE);
        outputY.setTypeEquals(BaseType.DOUBLE);

        /** The default values correspond to the color green */
        yLowValue = new Parameter(this, "yLowValue", new IntToken("110"));
        yHighValue = new Parameter(this, "yHighValue", new IntToken("210"));
        uLowValue = new Parameter(this, "uLowValue", new IntToken("85"));
        uHighValue = new Parameter(this, "uHighValue", new IntToken("110"));
        vLowValue = new Parameter(this, "vLowValue", new IntToken("120"));
        vHighValue = new Parameter(this, "vHighValue", new IntToken("130"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The upper bound on the U range. */
    public Parameter uHighValue;

    /** The lower bound on the U range. */
    public Parameter uLowValue;

    /** The upper bound on the V range. */
    public Parameter vHighValue;

    /** The lower bound on the V range. */
    public Parameter vLowValue;

    /** The upper bound on the Y range. */
    public Parameter yHighValue;

    /** The lower bound on the Y range. */
    public Parameter yLowValue;

    /** The port for the input, which has type ObjectToken. */
    public TypedIOPort input;

    /** The output port for the horizontal component, which has type
     *  DoubleToken.
     */
    public TypedIOPort outputX;

    /** The output port for the vertical component, which has type
     *  DoubleToken.
     */
    public TypedIOPort outputY;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new attribute
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ColorFinder newObject = (ColorFinder) super.clone(workspace);
        newObject.YArray = new byte[newObject._frameWidth
                                    * newObject._frameHeight];
        newObject.UArray = new byte[newObject._frameWidth / 2
                                    * newObject._frameHeight / 2];
        newObject.VArray = new byte[newObject._frameWidth / 2
                                    * newObject._frameHeight / 2];
        newObject._yClass = new int[newObject._histSize];
        newObject._uClass = new int[newObject._histSize];
        newObject._vClass = new int[newObject._histSize];

        return newObject;
    }

    /** Fire this actor.
     *  Output the X and Y coordinates if it finds the color in the
     *  Buffer.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (input.hasToken(0)) {
            JMFImageToken jmfImageToken = (JMFImageToken) input.get(0);
            Buffer in = jmfImageToken.getValue();
            VideoFormat videoFormat = (VideoFormat) in.getFormat();
            YUVFormat yuvFormat = null;

            try {
                yuvFormat = (YUVFormat) videoFormat;
            } catch (ClassCastException ex) {
                throw new IllegalActionException(this, ex, "Failed to cast "
                        + videoFormat.getClass() + ":\n" + videoFormat
                        + "\nto YUVFormat\n" + "in.getFormat() was: "
                        + in.getFormat().getClass()
                        + "\nPerhaps the video source is not generating YUV?");
            }

            byte[] data = (byte[]) in.getData();

            if (data != null) {
                System.arraycopy(data, yuvFormat.getOffsetY(), YArray, 0,
                        YArray.length);
                System.arraycopy(data, yuvFormat.getOffsetU(), UArray, 0,
                        UArray.length);
                System.arraycopy(data, yuvFormat.getOffsetV(), VArray, 0,
                        VArray.length);

                for (int x = 0; x < _frameWidth; x += 1) {
                    for (int y = 0; y < _frameHeight; y += 1) {
                        int yComp = _getYComponent(x, y);
                        int uComp = _getUComponent(x, y);
                        int vComp = _getVComponent(x, y);

                        int compInClass = _yClass[yComp] & _uClass[uComp]
                                & _vClass[vComp];

                        if (compInClass == 1) {
                            _sumX += x;
                            _sumY += y;
                            _inCount += 1;
                        }
                    }
                }

                if (_inCount > 0) {
                    double xLocation = (double) _sumX / _inCount;
                    double yLocation = _frameHeight - (double) _sumY / _inCount;
                    outputX.send(0, new DoubleToken(xLocation));
                    outputY.send(0, new DoubleToken(yLocation));

                    if (_debugging) {
                        _debug("just sent " + (int) xLocation + "and "
                                + (int) yLocation);
                    }
                }

                _inCount = 0;
                _sumX = 0;
                _sumY = 0;
            }
        }
    }

    /** Initialize this actor.
     *  Set the color to search for in the YUV domain.
     *  @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _yLow = ((IntToken) yLowValue.getToken()).intValue();
        _yHigh = ((IntToken) yHighValue.getToken()).intValue();
        _uLow = ((IntToken) uLowValue.getToken()).intValue();
        _uHigh = ((IntToken) uHighValue.getToken()).intValue();
        _vLow = ((IntToken) vLowValue.getToken()).intValue();
        _vHigh = ((IntToken) vHighValue.getToken()).intValue();

        for (int i = 0; i < _histSize; i += 1) {
            if (i > _yLow && i < _yHigh) {
                _yClass[i] = 1;
            } else {
                _yClass[i] = 0;
            }

            if (i > _uLow && i < _uHigh) {
                _uClass[i] = 1;
            } else {
                _uClass[i] = 0;
            }

            if (i > _vLow && i < _vHigh) {
                _vClass[i] = 1;
            } else {
                _vClass[i] = 0;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Convert a byte into an unsigned int */
    private int _bts(byte b) {
        return b & 0xFF;
    }

    /** Return the int representing the U band at this pixel*/
    private int _getUComponent(int point) {
        return _bts(UArray[point]);
    }

    /** Return the int representing the U band at this pixel*/
    private int _getUComponent(int x, int y) {
        return _getUComponent((x >> 1) + (y >> 1) * (_frameWidth / 2));
    }

    /** Return the int representing the V band at this pixel*/
    private int _getVComponent(int point) {
        return _bts(VArray[point]);
    }

    /** Return the int representing the V band at this pixel*/
    private int _getVComponent(int x, int y) {
        return _getVComponent((x >> 1) + (y >> 1) * (_frameWidth / 2));
    }

    /** Return the int representing the Y band at this pixel*/
    private int _getYComponent(int point) {
        return _bts(YArray[point]);
    }

    /** Return the int representing the Y band at this pixel*/
    private int _getYComponent(int x, int y) {
        return _getYComponent(x + _frameWidth * y);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // FIXME should be parameters and moved to initialize.
    private int _frameWidth = 320;

    private int _frameHeight = 240;

    //public YUVFormat videoFormat = new YUVFormat();
    // FIXME This also should be moved to initialize.
    private byte[] YArray = new byte[_frameWidth * _frameHeight];

    private byte[] UArray = new byte[_frameWidth / 2 * _frameHeight / 2];

    private byte[] VArray = new byte[_frameWidth / 2 * _frameHeight / 2];

    private int _histSize = 256;

    private int _inCount = 0;

    private int _sumX = 0;

    private int _sumY = 0;

    private int _yLow;

    private int _yHigh;

    private int _uLow;

    private int _uHigh;

    private int _vLow;

    private int _vHigh;

    private int[] _yClass = new int[_histSize];

    private int[] _uClass = new int[_histSize];

    private int[] _vClass = new int[_histSize];
}
