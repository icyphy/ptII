/* An actor that does edge detection on a javax.media.jai.RenderedOp.

 Copyright (c) 2002-2003 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIEdgeDetection
/**
   An actor that does edge detection on a image.  This is done by taking
   the image and seperately convolving it with two different masks.  The
   two results are squared, summed together, and square rooted to give the
   final image.  The user may specify one, or both masks.  A series of
   predefined masks are available for the user to use.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/
public class JAIEdgeDetection extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIEdgeDetection(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        firstMask =
            new StringAttribute(this, "firstMask");
        firstMask.setExpression("Sobel Horizontal");
        _firstMask = _SOBEL_HORIZONTAL;

        secondMask =
            new StringAttribute(this, "secondMask");
        secondMask.setExpression("Sobel Vertical");
        _secondMask = _SOBEL_VERTICAL;

        specifiedFirstMask =
            new Parameter(this, "userSpecifiedFirstMask",
                    new DoubleMatrixToken(_initialMatrix));
        specifiedSecondMask =
            new Parameter(this, "userSpecifiedSecondMask",
                    new DoubleMatrixToken(_initialMatrix));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The following two parameters are used to specify the masks used
     *  for edge detection.  Traditionally one mask is used for
     *  horizontal edge detection, and one mask is used for vertical
     *  edge detection.
     *  The following predefined masks are available:
     *  Sobel horizontal mask.
     *  Sobel vertical mask.
     *  Roberts horizontal mask.
     *  Roberts vertical mask.
     *  Prewitt horizontal mask.
     *  Prewitt vertical mask.
     *  Frei and Chen horizontal mask.
     *  Frei and Chen vertical mask.
     *  A diagonal mask, which finds edges in the direction of a slash.
     *  A back diagonal mask, which finds edges in the direction of a
     *  backslash.
     *  A Transparent mask.  Using this mask allows you to find edges in
     *  one direction and add them back to the original image.
     *  A mask of zeros.
     *  The default mask for the first choice is a Sobel horizontal
     *  mask.  The default mask for the second choice is a Sobel
     *  vertical mask.
     *  The user can also specify mask(s).  The dimensions of the mask(s)
     *  must be specified if the user chooses to do so.
     */
    public StringAttribute firstMask;
    public StringAttribute secondMask;

    /** The first user specified mask, and its corresponding x and
     *  y dimensions.  The Default mask is the transparent mask,
     *  and the default x and y dimensions is 3.
     */
    public Parameter specifiedFirstMask;
    //public Parameter specifiedFirstXDim;
    //public Parameter specifiedFirstYDim;

    /** The second user specified mask, and its corresponding x and
     *  y dimensions.  The Default mask is the transparent mask,
     *  and the default x and y dimensions is 3.
     */
    public Parameter specifiedSecondMask;
    //public Parameter specifiedSecondXDim;
    //public Parameter specifiedSecondYDim;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set change private variables if the
     *  attribute corresponding to it has changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized,
     *  or if a contained method throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == firstMask) {
            String firstName = firstMask.getExpression();
            _firstMask = _maskNumberer(firstName);
        } else if (attribute == secondMask) {
            String secondName = secondMask.getExpression();
            _secondMask = _maskNumberer(secondName);
        } else if (attribute == specifiedFirstMask) {
            _firstMaskData
                = ((DoubleMatrixToken)specifiedFirstMask.getToken());
        } else if (attribute == specifiedSecondMask) {
            _secondMaskData
                = ((DoubleMatrixToken)specifiedSecondMask.getToken());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the edge detected image.
     *  @exception IllegalActionException If a contained method throws
     *  it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        if (_firstMask == _USER_SPECIFIED) {
            _firstKernelJAI =
                _maskFiller(_firstMaskData);
        } else {
            _firstKernelJAI = _filterAssigner(_firstMask);
        }
        if (_secondMask == _USER_SPECIFIED) {
            _secondKernelJAI =
                _maskFiller(_secondMaskData);
        } else {
            _secondKernelJAI = _filterAssigner(_secondMask);
        }
        RenderedOp newImage = JAI.create("gradientmagnitude", oldImage,
                _firstKernelJAI, _secondKernelJAI);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the user chooses to use a prespecified mask, then this
     *  method will assign the mask values to a KernelJAI used in edge
     *  detection.
     *  @exception IllegalActionException If the choice value is out of
     *  range.
     */
    private KernelJAI _filterAssigner(int choice)
            throws IllegalActionException {
        switch (choice) {
        case _BACKDIAGONAL:
            return new KernelJAI(3, 3, _backDiagonalFilter);
        case _DIAGONAL:
            return new KernelJAI(3, 3, _diagonalFilter);
        case _FREICHEN_HORIZONTAL:
            return new KernelJAI(3, 3, _freiAndChenHorizontalFilter);
        case _FREICHEN_VERTICAL:
            return new KernelJAI(3, 3, _freiAndChenVerticalFilter);
        case _PREWITT_HORIZONTAL:
            return new KernelJAI(3, 3, _prewittHorizontalFilter);
        case _PREWITT_VERTICAL:
            return new KernelJAI(3, 3, _prewittVerticalFilter);
        case _ROBERTS_HORIZONTAL:
            return new KernelJAI(3, 3, _robertsHorizontalFilter);
        case _ROBERTS_VERTICAL:
            return new KernelJAI(3, 3, _robertsVerticalFilter);
        case _SOBEL_HORIZONTAL:
            return new KernelJAI(3, 3, _sobelHorizontalFilter);
        case _SOBEL_VERTICAL:
            return new KernelJAI(3, 3, _sobelVerticalFilter);
        case _TRANSPARENT:
            return new KernelJAI(3, 3, _transparentFilter);
        case _ZERO_FILTER:
            return new KernelJAI(3, 3, _zeroFilter);
        default:
            throw new IllegalActionException("Could not assign filter");
        }
    }

    /** If a user decides not to use a prespecified mask, this method
     *  will return a KernalJAI filled with user specified values.
     */
    private KernelJAI _maskFiller(DoubleMatrixToken matrix) {
        double[][] matrixValue = matrix.doubleMatrix();
        int height = matrix.getRowCount();
        int width = matrix.getColumnCount();
        float[] floatArray = new float[width*height];
        int count = 0;
        for (int i = 0; i < height; i = i+1) {
            for (int j = 0; j < width; j = j+1) {
                floatArray[count] = (float)matrixValue[i][j];
                count = count + 1;
            }
        }
        return new KernelJAI(width, height, floatArray);
    }

    /** A convenience method to help in assingning masks.  */
    private int _maskNumberer(String maskName)
            throws IllegalActionException {
        if (maskName.equals("Backdiagonal")) {
            return _BACKDIAGONAL;
        } else if (maskName.equals("Diagonal")) {
            return _DIAGONAL;
        } else if (maskName.equals("Frei and Chen Horizontal")) {
            return _FREICHEN_HORIZONTAL;
        } else if (maskName.equals("Frei and Chen Vertical")) {
            return _FREICHEN_VERTICAL;
        } else if (maskName.equals("Prewitt Horizontal")) {
            return _PREWITT_HORIZONTAL;
        } else if (maskName.equals("Prewitt Vertical")) {
            return _PREWITT_VERTICAL;
        } else if (maskName.equals("Roberts Horizontal")) {
            return _ROBERTS_HORIZONTAL;
        } else if (maskName.equals("Roberts Vertical")) {
            return _ROBERTS_VERTICAL;
        } else if (maskName.equals("Sobel Horizontal")) {
            return _SOBEL_HORIZONTAL;
        } else if (maskName.equals("Sobel Vertical")) {
            return _SOBEL_VERTICAL;
        } else if (maskName.equals("Transparent")) {
            return _TRANSPARENT;
        } else if (maskName.equals("User Specified")) {
            return _USER_SPECIFIED;
        } else if (maskName.equals("Zero")) {
            return _ZERO_FILTER;
        } else {
            throw new IllegalActionException(this,
                    "Unrecognized Mask type: " + maskName);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The ArrayTokens contained the the User Specified Mask Fields */
    private DoubleMatrixToken _firstMaskData;
    private DoubleMatrixToken _secondMaskData;

    /** The KernalJAI's that contain the masks to be used in edge
     *  detection.
     */
    private KernelJAI _firstKernelJAI;
    private KernelJAI _secondKernelJAI;

    /** The choice of mask. */
    private int _firstMask;
    private int _secondMask;

    private double[][] _initialMatrix = {{0.0F, 0.0F, 0.0F},
                                         {0.0F, 0.707F, 0.0F},
                                         {0.0F, 0.0F, 0.0F}};

    /** Prespecified masks that the user may use */
    private final float _sobelHorizontalFilter[] = {1.0F, 0.0F, -1.0F,
                                                    2.0F, 0.0F, -2.0F,
                                                    1.0F, 0.0F, -1.0F};

    private final float _sobelVerticalFilter[] = {-1.0F, -2.0F, -1.0F,
                                                  0.0F, 0.0F, 0.0F,
                                                  1.0F, 2.0F, 1.0F};

    private final float _robertsHorizontalFilter[] = {0.0F, 0.0F, -1.0F,
                                                      0.0F, 1.0F, 0.0F,
                                                      0.0F, 0.0F, 0.0F};

    private final float _robertsVerticalFilter[] = {-1.0F, 0.0F, 0.0F,
                                                    0.0F, 1.0F, 0.0F,
                                                    0.0F, 0.0F, 0.0F};

    private final float _prewittHorizontalFilter[] = {1.0F, 0.0F, -1.0F,
                                                      1.0F, 0.0F, -1.0F,
                                                      1.0F, 0.0F, -1.0F};

    private final float _prewittVerticalFilter[] = {-1.0F, -1.0F, -1.0F,
                                                    0.0F, 0.0F, 0.0F,
                                                    1.0F, 1.0F, 1.0F};

    private final float _freiAndChenHorizontalFilter[] = {1.0F, 0.0F, -1.0F,
                                                          1.414F, 0.0F, -1.414F,
                                                          1.0F, 0.0F, -1.0F};

    private final float _freiAndChenVerticalFilter[] = {-1.0F, -1.414F, -1.0F,
                                                        0.0F, 0.0F, 0.0F,
                                                        1.0F, 1.414F, 1.0F};

    private final float _transparentFilter[] = {0.0F, 0.0F, 0.0F,
                                                0.0F, 0.707F, 0.0F,
                                                0.0F, 0.0F, 0.0F};

    private final float _zeroFilter[] = {0.0F, 0.0F, 0.0F,
                                         0.0F, 0.0F, 0.0F,
                                         0.0F, 0.0F, 0.0F};

    private final float _diagonalFilter[] = {1.0F, 1.0F, 0.0F,
                                             1.0F, 0.0F, -1.0F,
                                             0.0F, -1.0F, -1.0F};

    private final float _backDiagonalFilter[] = {0.0F, 1.0F, 1.0F,
                                                 -1.0F, 0.0F, 1.0F,
                                                 -1.0F, -1.0F, 0.0F};

    //Constants used for more efficient execution
    private final int _BACKDIAGONAL = 0;
    private final int _DIAGONAL = 1;
    private final int _FREICHEN_HORIZONTAL = 2;
    private final int _FREICHEN_VERTICAL = 3;
    private final int _PREWITT_HORIZONTAL = 4;
    private final int _PREWITT_VERTICAL = 5;
    private final int _ROBERTS_HORIZONTAL = 6;
    private final int _ROBERTS_VERTICAL = 7;
    private final int _SOBEL_HORIZONTAL = 8;
    private final int _SOBEL_VERTICAL = 9;
    private final int _TRANSPARENT = 10;
    private final int _USER_SPECIFIED = 11;
    private final int _ZERO_FILTER = 12;
}
