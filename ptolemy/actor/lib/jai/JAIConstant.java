/* An actor that produces a JAIImageToken with constant bands.

 @Copyright (c) 2003-2014 The Regents of the University of California.
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

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Source;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
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
//// JAIConstant

/**
 Produce an image with a uniform color.

 @author James Yeh, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JAIConstant extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIConstant(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.OBJECT);
        width = new Parameter(this, "width", new IntToken(0));
        height = new Parameter(this, "height", new IntToken(0));

        IntToken[] defaultValues = { IntToken.ZERO };

        bandValues = new Parameter(this, "bandValues", new ArrayToken(
                BaseType.INT, defaultValues));
        _values = ((ArrayToken) bandValues.getToken()).arrayValue();

        dataFormat = new StringAttribute(this, "dataFormat");
        dataFormat.setExpression("byte");
        _dataFormat = _BYTE;

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The band values of the image.  The default value is an Array
     *  of 1 integer of value 0.
     */
    public Parameter bandValues;

    /** The height of the image in pixels.  The default value is
     *  and integer with a value of 0.
     */
    public Parameter height;

    /** The width of the image in pixels.  The default value is
     *  and integer with a value of 0.
     */
    public Parameter width;

    /** The type to cast the data to.  This is a string valued
     *  attribute that defaults to "byte".  Other valid types
     *  are "double", "float", "int" and "short".
     */
    public StringAttribute dataFormat;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == bandValues) {
            _values = ((ArrayToken) bandValues.getToken()).arrayValue();
        } else if (attribute == height) {
            _height = ((IntToken) height.getToken()).intValue();
        } else if (attribute == width) {
            _width = ((IntToken) width.getToken()).intValue();
        } else if (attribute == dataFormat) {
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
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized data type: " + dataFormatName);
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
        JAIConstant newObject = (JAIConstant) super.clone(workspace);
        newObject._values = null;
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ParameterBlock parameters = new ParameterBlock();
        parameters.add((float) _width);
        parameters.add((float) _height);

        //parameters.add(_bandValues);
        if (_dataFormat == _BYTE) {
            Byte[] byteValues = new Byte[_values.length];

            for (int i = 0; i < _values.length; i++) {
                byteValues[i] = Byte.valueOf((byte) ((ScalarToken) _values[i])
                        .intValue());
            }

            parameters.add(byteValues);
        } else if (_dataFormat == _DOUBLE) {
            Double[] doubleValues = new Double[_values.length];

            for (int i = 0; i < _values.length; i++) {
                doubleValues[i] = Double.valueOf(((ScalarToken) _values[i])
                        .doubleValue());
            }

            parameters.add(doubleValues);
        } else if (_dataFormat == _FLOAT) {
            Float[] floatValues = new Float[_values.length];

            for (int i = 0; i < _values.length; i++) {
                floatValues[i] = Float
                        .valueOf((float) ((ScalarToken) _values[i])
                                .doubleValue());
            }

            parameters.add(floatValues);
        } else if (_dataFormat == _INT) {
            Integer[] intValues = new Integer[_values.length];

            for (int i = 0; i < _values.length; i++) {
                intValues[i] = Integer.valueOf(((ScalarToken) _values[i])
                        .intValue());
            }

            parameters.add(intValues);
        } else if (_dataFormat == _SHORT) {
            Short[] shortValues = new Short[_values.length];

            for (int i = 0; i < _values.length; i++) {
                shortValues[i] = Short
                        .valueOf((short) ((ScalarToken) _values[i]).intValue());
            }

            parameters.add(shortValues);
        }

        RenderedOp newImage = JAI.create("constant", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //private Double[] _bandValues;

    private int _height;

    private int _width;

    private Token[] _values;

    private int _dataFormat;

    // Constants used for more efficient execution.
    private static final int _BYTE = 0;

    private static final int _DOUBLE = 1;

    private static final int _FLOAT = 2;

    private static final int _INT = 3;

    private static final int _SHORT = 4;
}
