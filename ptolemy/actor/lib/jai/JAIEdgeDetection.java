/* One line description of file.

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
   Description of the class
   @author yourname
   @version $Id$
   @since Ptolemy II 0.2
   @see classname
   @see full-classname
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
        
        firstMaskChoice =
            new Parameter(this, "firstMaskChoice", new IntToken(1));        
        secondMaskChoice =
            new Parameter(this, "secondMaskChoice", new IntToken(2));           
        userSpecifiedFirstMask =
            new Parameter(this, "userSpecifiedFirstMask", new ArrayToken(_doubleArrayToken));
        userSpecifiedFirstXDim = 
            new Parameter(this, "userSpecifiedFirstXDim", new IntToken(3));
        userSpecifiedFirstYDim = 
            new Parameter(this, "userSpecifiedFirstYDim", new IntToken(3));                         
        userSpecifiedSecondMask =
            new Parameter(this, "userSpecifiedSecondMask", new ArrayToken(_doubleArrayToken));
        userSpecifiedSecondXDim = 
            new Parameter(this, "userSpecifiedSecondXDim", new IntToken(3));
        userSpecifiedSecondYDim = 
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
    public Parameter firstMaskChoice;    
    public Parameter secondMaskChoice;

    /** The first user specified mask, and its corresponding x and
     *  y dimensions.  The Default mask is the transparent mask,
     *  and the default x and y dimensions is 3.
     */
    public Parameter userSpecifiedFirstMask;
    public Parameter userSpecifiedFirstXDim;
    public Parameter userSpecifiedFirstYDim;

    /** The second user specified mask, and its corresponding x and
     *  y dimensions.  The Default mask is the transparent mask,
     *  and the default x and y dimensions is 3.
     */
    public Parameter userSpecifiedSecondMask;
    public Parameter userSpecifiedSecondXDim;
    public Parameter userSpecifiedSecondYDim;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Output the scaled RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        RenderedOp newImage = JAI.create("gradientmagnitude", oldImage, 
                _firstKernelJAI, _secondKernelJAI);
        output.send(0, new JAIImageToken(newImage));        
    }
    
    /** Initialize this actor.
     *  Set the horizontal and vertical scaling values.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _firstMaskChoice = ((IntToken)firstMaskChoice.getToken()).intValue();
        _secondMaskChoice = ((IntToken)secondMaskChoice.getToken()).intValue();
        if (_firstMaskChoice == 0) {
            _firstMaskData = ((ArrayToken)userSpecifiedFirstMask.getToken());
            _userSpecifiedFirstXDim = ((IntToken)userSpecifiedFirstXDim.getToken()).intValue();
            _userSpecifiedFirstYDim = ((IntToken)userSpecifiedFirstYDim.getToken()).intValue();
            _firstKernelJAI = _maskFiller(_firstMaskData, _userSpecifiedFirstXDim, _userSpecifiedFirstYDim);
        }
        if (_secondMaskChoice == 0) {
            _secondMaskData = ((ArrayToken)userSpecifiedSecondMask.getToken());
            _userSpecifiedSecondXDim = ((IntToken)userSpecifiedSecondXDim.getToken()).intValue();
            _userSpecifiedSecondYDim = ((IntToken)userSpecifiedSecondYDim.getToken()).intValue();
            _secondKernelJAI = _maskFiller(_secondMaskData, _userSpecifiedSecondXDim, _userSpecifiedSecondYDim);
        }
        if (_firstMaskChoice > 0 && _firstMaskChoice <= _highestChoice) {
            _firstKernelJAI = _maskAssigner(_firstMaskChoice);
        }
        if (_firstMaskChoice > 0 && _firstMaskChoice <= _highestChoice) {
            _secondKernelJAI = _maskAssigner(_secondMaskChoice);
        }        
        if ((_firstMaskChoice < 0) || (_secondMaskChoice < 0) ||
                (_firstMaskChoice > _highestChoice) || 
                (_secondMaskChoice > _highestChoice)) {    
            throw new IllegalActionException("Invalid Mask Choice");
        } 
    }   

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    private KernelJAI _maskAssigner(int choice) 
            throws IllegalActionException {
        switch (choice) {
        case 1:
            return new KernelJAI(3,3,_sobelHorizontalFilter);
        case 2:
            return new KernelJAI(3,3,_sobelVerticalFilter);
        case 3:
            return new KernelJAI(3,3,_robertsHorizontalFilter);
        case 4:
            return new KernelJAI(3,3,_robertsVerticalFilter);
        case 5:
            return new KernelJAI(3,3,_prewittHorizontalFilter);
        case 6:
            return new KernelJAI(3,3,_prewittVerticalFilter);
        case 7:
            return new KernelJAI(3,3,_freiAndChenHorizontalFilter);
        case 8:
            return new KernelJAI(3,3,_freiAndChenVerticalFilter);    
        case 9:
            return new KernelJAI(3,3,_diagonalFilter);
        case 10:
            return new KernelJAI(3,3,_backDiagonalFilter);
        case 11:
            return new KernelJAI(3,3,_transparentFilter);
        case 12:
            return new KernelJAI(3,3,_zeroFilter);
        default:
            throw new IllegalActionException("Could not assign mask");
        }
    }
    
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

    private ArrayToken _firstMaskData;
    private ArrayToken _secondMaskData;

    private int _userSpecifiedFirstXDim;
    private int _userSpecifiedFirstYDim;
    private int _userSpecifiedSecondXDim;
    private int _userSpecifiedSecondYDim;

    private KernelJAI _firstKernelJAI;
    private KernelJAI _secondKernelJAI;

    private int _userSpecifiedXDim;
   
    private int _firstMaskChoice;
    private int _secondMaskChoice;
    private final int _highestChoice = 12;
    private DoubleToken _zero = new DoubleToken("0.0F");
    private DoubleToken _halfRootTwo = new DoubleToken("0.707F");
    private DoubleToken _doubleArrayToken[] = {_zero, _zero, _zero,
                                               _zero, _halfRootTwo, _zero,
                                               _zero, _zero, _zero};
    
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
}
