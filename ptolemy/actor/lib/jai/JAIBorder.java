/* An actor that adds a border to an image.

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

import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderConstant;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// JAIBorder

/**
 Adds a border to an image.  The amount to pad must be specified for all
 four sides.  There are five different borders to choose from.
 <p>
 Constant - A constant border adds constant values to the sides of the
 image.  The user may specify either one constant to be applied to all
 bands, or one constant for each band.
 <p>
 Copy - This border copys the edges of the original image, and uses it
 to fill in the border values.
 <p>
 Reflect - This border reflects the edge of the image, and keeps
 flipping until it reaches the edge of the new image.
 <p>
 Wrap - This border periodically repeats the image and clamps the size
 to only include what is specified.
 <p>
 Zero - This border fills in the borders with zeros in each band.

 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JAIBorder extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIBorder(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        leftPadding = new Parameter(this, "leftPadding", new IntToken(0));
        rightPadding = new Parameter(this, "rightPadding", new IntToken(0));
        topPadding = new Parameter(this, "topPadding", new IntToken(0));
        bottomPadding = new Parameter(this, "bottomPadding", new IntToken(0));

        borderType = new StringAttribute(this, "borderType");
        borderType.setExpression("Zero");
        _borderType = _BORDER_ZERO;

        // An initial array that simply copies a three banded image.
        DoubleToken[] initialArray = { new DoubleToken(0) };

        constants = new Parameter(this, "constants", new ArrayToken(
                BaseType.DOUBLE, initialArray));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The type of border to use. */
    public StringAttribute borderType;

    /** The constants to use if the Constant border type is chosen. */
    public Parameter constants;

    /** The amount of pixels to pad the bottom with.  The default is
     *  the integer value 0.
     */
    public Parameter bottomPadding;

    /** The amount of pixels to pad the left side with.  The default is
     *  the integer value 0.
     */
    public Parameter leftPadding;

    /** The amount of pixels to pad the right side with.  The default is
     *  the integer value 0.
     */
    public Parameter rightPadding;

    /** The amount of pixels to pad the top with.  The default is
     *  the integer value 0.
     */
    public Parameter topPadding;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the border type and size.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized,
     *  or if a contained method throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == borderType) {
            String name = borderType.getExpression();

            if (name.equals("Constant")) {
                _borderType = _BORDER_CONSTANT;
            } else if (name.equals("Copy")) {
                _borderType = _BORDER_COPY;
            } else if (name.equals("Reflect")) {
                _borderType = _BORDER_REFLECT;
            } else if (name.equals("Wrap")) {
                _borderType = _BORDER_WRAP;
            } else if (name.equals("Zero")) {
                _borderType = _BORDER_ZERO;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized Border Name: " + name);
            }
        } else if (attribute == leftPadding) {
            _leftPadding = ((IntToken) leftPadding.getToken()).intValue();
        } else if (attribute == rightPadding) {
            _rightPadding = ((IntToken) rightPadding.getToken()).intValue();
        } else if (attribute == topPadding) {
            _topPadding = ((IntToken) topPadding.getToken()).intValue();
        } else if (attribute == bottomPadding) {
            _bottomPadding = ((IntToken) bottomPadding.getToken()).intValue();
        } else if (attribute == constants) {
            Token[] data = ((ArrayToken) constants.getToken()).arrayValue();
            _constantValues = new double[data.length];

            for (int i = 0; i < data.length; i++) {
                _constantValues[i] = ((DoubleToken) data[i]).doubleValue();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new attribute
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JAIBorder newObject = (JAIBorder) super.clone(workspace);
        newObject._constantValues = null;
        return newObject;
    }

    /** Fire this actor.
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
        parameters.add(_leftPadding);
        parameters.add(_rightPadding);
        parameters.add(_topPadding);
        parameters.add(_bottomPadding);

        switch (_borderType) {
        case _BORDER_CONSTANT:
            parameters.add(new BorderExtenderConstant(_constantValues));
            break;

        case _BORDER_COPY:
            parameters.add(BorderExtender
                    .createInstance(BorderExtender.BORDER_COPY));
            break;

        case _BORDER_REFLECT:
            parameters.add(BorderExtender
                    .createInstance(BorderExtender.BORDER_REFLECT));
            break;

        case _BORDER_WRAP:
            parameters.add(BorderExtender
                    .createInstance(BorderExtender.BORDER_WRAP));
            break;

        case _BORDER_ZERO:
            parameters.add(BorderExtender
                    .createInstance(BorderExtender.BORDER_ZERO));
            break;

        default:
            throw new IllegalActionException("Could not assign border");
        }

        RenderedOp newImage = JAI.create("Border", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The constant values to use if a constant border is desired. */
    private double[] _constantValues;

    /** The type of border to use. */
    private int _borderType;

    /** The amount to pad on the four sides. */
    private int _bottomPadding;

    private int _leftPadding;

    private int _rightPadding;

    private int _topPadding;

    // Constants for more efficient execution.
    private static final int _BORDER_CONSTANT = 0;

    private static final int _BORDER_COPY = 1;

    private static final int _BORDER_REFLECT = 2;

    private static final int _BORDER_WRAP = 3;

    private static final int _BORDER_ZERO = 4;
}
