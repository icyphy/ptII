/* An actor that does edge detection on a javax.media.jai.RenderedOp.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

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
            new Parameter(this, "userSpecifiedFirstMask", new ArrayToken(_doubleArrayToken));
        specifiedFirstXDim = 
            new Parameter(this, "userSpecifiedFirstXDim", new IntToken(3));
        specifiedFirstYDim = 
            new Parameter(this, "userSpecifiedFirstYDim", new IntToken(3));                         
        specifiedSecondMask =
            new Parameter(this, "userSpecifiedSecondMask", new ArrayToken(_doubleArrayToken));
        specifiedSecondXDim = 
            new Parameter(this, "userSpecifiedSecondXDim", new IntToken(3));
        specifiedSecondYDim = 
            new Parameter(this, "userSpecifiedSecondYDim", new IntToken(3));                        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The following two parameters are used to specify the masks used
     *  for edge detection.  Traditionally one mask is used for 
     *  horizontal edge detection, and one mask is used for vertical
     *  edge detection.
     *  A value of 0 corresponds to a user specified mask.  If the user
     *  decides to input his own mask, then he must also specify the 
     *  width and height of the mask.
     *  A value of 1 corresponds to the Sobel horizontal mask.
     *  A value of 2 corresponds to the Sobel vertical mask.
     *  A value of 3 corresponds to the Roberts horizontal mask.
     *  A value of 4 corresponds to the Roberts vertical mask.
     *  A value of 5 corresponds to the Prewitt horizontal mask.
     *  A value of 6 corresponds to the Prewitt vertical mask.
     *  A value of 7 corresponds to the Frei and Chen horizontal mask.
     *  A value of 8 corresponds to the Frei and Chen vertical mask.
     *  A value of 9 corresponds to a diagonal mask.  This mask finds
     *  edges in the direction of a slash.
     *  A value of 10 corresponds to the back diagonal mask.  This mask
     *  finds edges in the direction of a backslash.
     *  A value of 11 corresponds to a Transparent mask.  Using this 
     *  mask allows you to find edges in one direction and add them
     *  back to the original image.
     *  A value of 12 corresponds to a mask of zeros.
     *  The default mask for the first choice is a Sobel horizontal
     *  mask.  The default mask for the second choice is a Sobel
     *  vertical mask.
     */    
    public StringAttribute firstMask;    
    public StringAttribute secondMask;

    /** The first user specified mask, and its corresponding x and
     *  y dimensions.  The Default mask is the transparent mask,
     *  and the default x and y dimensions is 3.
     */
    public Parameter specifiedFirstMask;
    public Parameter specifiedFirstXDim;
    public Parameter specifiedFirstYDim;

    /** The second user specified mask, and its corresponding x and
     *  y dimensions.  The Default mask is the transparent mask,
     *  and the default x and y dimensions is 3.
     */
    public Parameter specifiedSecondMask;
    public Parameter specifiedSecondXDim;
    public Parameter specifiedSecondYDim;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the scaling factors.
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
            _firstMaskData = ((ArrayToken)specifiedFirstMask.getToken());            
        } else if (attribute == specifiedFirstXDim){
            _specifiedFirstXDim = 
                ((IntToken)specifiedFirstXDim.getToken()).intValue();
        } else if (attribute == specifiedFirstYDim) {
            _specifiedFirstYDim = 
                ((IntToken)specifiedFirstYDim.getToken()).intValue();            
        } else if (attribute == specifiedSecondMask) {
            _secondMaskData = ((ArrayToken)specifiedSecondMask.getToken());            
        } else if (attribute == specifiedSecondXDim){
            _specifiedSecondXDim = 
                ((IntToken)specifiedSecondXDim.getToken()).intValue();
        } else if (attribute == specifiedSecondYDim) {
            _specifiedSecondYDim = 
                ((IntToken)specifiedSecondYDim.getToken()).intValue();            
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
                _maskFiller(_firstMaskData, _specifiedFirstXDim, _specifiedFirstYDim);
        } else {
            _firstKernelJAI = _filterAssigner(_firstMask);
        }
        if (_secondMask == _USER_SPECIFIED) {
            _secondKernelJAI = 
                _maskFiller(_secondMaskData, _specifiedSecondXDim, _specifiedSecondYDim);
        } else {
            _secondKernelJAI = _filterAssigner(_secondMask);
        } 
        RenderedOp newImage = JAI.create("gradientmagnitude", oldImage, 
                _firstKernelJAI, _secondKernelJAI);
        output.send(0, new JAIImageToken(newImage));        
    }
     
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
            return new KernelJAI(3,3,_backDiagonalFilter);
        case _DIAGONAL:
            return new KernelJAI(3,3,_diagonalFilter);
        case _FREICHEN_HORIZONTAL:
            return new KernelJAI(3,3,_freiAndChenHorizontalFilter);
        case _FREICHEN_VERTICAL:
            return new KernelJAI(3,3,_freiAndChenVerticalFilter);
        case _PREWITT_HORIZONTAL:
            return new KernelJAI(3,3,_prewittHorizontalFilter);
        case _PREWITT_VERTICAL:
            return new KernelJAI(3,3,_prewittVerticalFilter);
        case _ROBERTS_HORIZONTAL:
            return new KernelJAI(3,3,_robertsHorizontalFilter);
        case _ROBERTS_VERTICAL:
            return new KernelJAI(3,3,_robertsVerticalFilter);    
        case _SOBEL_HORIZONTAL:
            return new KernelJAI(3,3,_sobelHorizontalFilter);
        case _SOBEL_VERTICAL:
            return new KernelJAI(3,3,_sobelVerticalFilter);
        case _TRANSPARENT:
            return new KernelJAI(3,3,_transparentFilter);
        case _ZERO_FILTER:
            return new KernelJAI(3,3,_zeroFilter);
        default:
            throw new IllegalActionException("Could not assign filter");
        }
    }

    
    /** If a user decides not to use a prespecified mask, this method
     *  will return a KernalJAI filled with user specified values.
     *  @exception IllegalActionException If the dimensions of the mask
     *  and the number of entries do not agree.
     */
    private KernelJAI _maskFiller(ArrayToken array, int width, int height) 
            throws IllegalActionException {
        
        if((array.arrayValue()).length != width*height) {
            throw new IllegalActionException("Dimensions do not agree");
        }
        else {
            Token tokenArray[] = array.arrayValue();
            float floatArray[] = new float[tokenArray.length];
            for (int i = 0; i < tokenArray.length; i = i+1) {
                double _value = ((DoubleToken)(tokenArray[i])).doubleValue();
                floatArray[i] = (float)_value;
            }
            return new KernelJAI(width,height,floatArray);
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The ArrayTokens contained the the User Specified Mask Fields */
    private ArrayToken _firstMaskData;
    private ArrayToken _secondMaskData;

    /** The dimensions of the user specified masks.  */
    private int _specifiedFirstXDim;
    private int _specifiedFirstYDim;
    private int _specifiedSecondXDim;
    private int _specifiedSecondYDim;

    /** The KernalJAI's that contain the masks to be used in edge
     *  detection.
     */
    private KernelJAI _firstKernelJAI;
    private KernelJAI _secondKernelJAI;

    /** The choice of mask.  If it is 0, then it is a user specified
     *  mask.  Otherwise, choices 1-12 are masks that are specified
     *  below.
     */
    private int _firstMask;
    private int _secondMask;

    /** The bound on the number of prespecified masks */
    private final int _highestChoice = 12;
    
    /** DoubleToken's representing zero and the square root of two */
    private final DoubleToken _zero = new DoubleToken("0.0F");
    private final DoubleToken _halfRootTwo = new DoubleToken("0.707F");

    /** The default for a user specified mask */
    private DoubleToken _doubleArrayToken[] = {_zero, _zero, _zero,
                                               _zero, _halfRootTwo, _zero,
                                               _zero, _zero, _zero};

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
